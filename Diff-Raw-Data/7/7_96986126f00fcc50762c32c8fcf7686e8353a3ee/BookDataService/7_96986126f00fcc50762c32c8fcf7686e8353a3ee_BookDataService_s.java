 package com.sismics.books.core.service;
 
 import java.io.InputStream;
 import java.net.URL;
 import java.net.URLConnection;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.nio.file.Paths;
 import java.nio.file.StandardCopyOption;
 import java.util.Iterator;
 import java.util.Locale;
 import java.util.UUID;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.FutureTask;
 import java.util.concurrent.TimeUnit;
 
 import org.codehaus.jackson.JsonNode;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.codehaus.jackson.node.ArrayNode;
 import org.joda.time.format.DateTimeFormat;
 import org.joda.time.format.DateTimeFormatter;
 import org.joda.time.format.DateTimeFormatterBuilder;
 import org.joda.time.format.DateTimeParser;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.util.concurrent.AbstractIdleService;
 import com.google.common.util.concurrent.RateLimiter;
 import com.neovisionaries.i18n.LanguageCode;
 import com.sismics.books.core.constant.ConfigType;
 import com.sismics.books.core.model.jpa.Book;
 import com.sismics.books.core.util.ConfigUtil;
 import com.sismics.books.core.util.DirectoryUtil;
 import com.sismics.books.core.util.TransactionUtil;
 
 /**
  * Service to fetch book informations. 
  *
  * @author bgamard
  */
 public class BookDataService extends AbstractIdleService {
     /**
      * Logger.
      */
     private static final Logger log = LoggerFactory.getLogger(BookDataService.class);
     
     /**
      * Google Books API Search URL.
      */
     private static final String GOOGLE_BOOKS_SEARCH_FORMAT = "https://www.googleapis.com/books/v1/volumes?q=isbn:%s&key=%s";
     
     /**
      * Open Library API URL.
      */
     private static final String OPEN_LIBRARY_FORMAT = "http://openlibrary.org/api/volumes/brief/isbn/%s.json";
     
     /**
      * Executor for book API requests.
      */
     private ExecutorService executor;
 
     /**
      * Google API rate limiter.
      */
     private RateLimiter googleRateLimiter = RateLimiter.create(20);
     
     /**
      * Open Library API rate limiter.
      */
     private RateLimiter openLibraryRateLimiter = RateLimiter.create(0.33332);
     
     /**
      * API key Google.
      */
     private String apiKeyGoogle = null;
     
     /**
      * Parser for multiple date formats;
      */
     private static DateTimeFormatter formatter;
     
     static {
         // Initialize date parser
         DateTimeParser[] parsers = { 
                 DateTimeFormat.forPattern("yyyy").getParser(),
                 DateTimeFormat.forPattern("yyyy-MM").getParser(),
                 DateTimeFormat.forPattern("yyyy-MM-dd").getParser(),
                 DateTimeFormat.forPattern("MMM d, yyyy").getParser()};
         formatter = new DateTimeFormatterBuilder().append( null, parsers ).toFormatter();
     }
     
     @Override
     protected void startUp() throws Exception {
         initConfig();
         executor = Executors.newSingleThreadExecutor(); 
         if (log.isInfoEnabled()) {
             log.info("Book data service started");
         }
     }
     
     /**
      * Initialize service configuration.
      */
     public void initConfig() {
         TransactionUtil.handle(new Runnable() {
             @Override
             public void run() {
                 apiKeyGoogle = ConfigUtil.getConfigStringValue(ConfigType.API_KEY_GOOGLE);
             }
         });
     }
 
     /**
      * Search a book by its ISBN.
      * 
      * @return Book found
      * @throws Exception
      */
     public Book searchBook(String rawIsbn) throws Exception {
         // Sanitize ISBN (keep only digits)
         final String isbn = rawIsbn.replaceAll("[^\\d]", "");
 
         Callable<Book> callable = new Callable<Book>() {
             
             @Override
             public Book call() throws Exception {
                 try {
                     return searchBookWithGoogle(isbn);
                 } catch (Exception e) {
                     log.warn("Book not found with Google: " + isbn + " with error: " + e.getMessage());
                     try {
                         return searchBookWithOpenLibrary(isbn);
                     } catch (Exception e0) {
                         log.warn("Book not found with Open Library: " + isbn + " with error: " + e0.getMessage());
                         log.error("Book not found with any API: " + isbn);
                         throw e0;
                     }
                 }
             }
         };
         FutureTask<Book> futureTask = new FutureTask<Book>(callable);
         executor.submit(futureTask);
         
         return futureTask.get();
     }
 
     /**
      * Search a book by its ISBN using Google Books.
      * 
      * @return Book found
      * @throws Exception
      */
     private Book searchBookWithGoogle(String isbn) throws Exception {
         googleRateLimiter.acquire();
         
         URL url = new URL(String.format(Locale.ENGLISH, GOOGLE_BOOKS_SEARCH_FORMAT, isbn, apiKeyGoogle));
         URLConnection connection = url.openConnection();
         connection.setRequestProperty("Accept-Charset", "utf-8");
         connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/29.0.1547.62 Safari/537.36");
         connection.setConnectTimeout(10000);
         connection.setReadTimeout(10000);
         InputStream inputStream = connection.getInputStream();
         ObjectMapper mapper = new ObjectMapper();
         JsonNode rootNode = mapper.readValue(inputStream, JsonNode.class);
         ArrayNode items = (ArrayNode) rootNode.get("items");
         if (rootNode.get("totalItems").getIntValue() <= 0) {
             throw new Exception("No book found for ISBN: " + isbn);
         }
         JsonNode item = items.get(0);
         JsonNode volumeInfo = item.get("volumeInfo");
         
         // Build the book
         Book book = new Book();
         book.setId(UUID.randomUUID().toString());
         book.setTitle(volumeInfo.get("title").getTextValue());
         book.setSubtitle(volumeInfo.has("subtitle") ? volumeInfo.get("subtitle").getTextValue() : null);
         ArrayNode authors = (ArrayNode) volumeInfo.get("authors");
         if (authors.size() <= 0) {
             throw new Exception("Author not found");
         }
         book.setAuthor(authors.get(0).getTextValue());
         book.setDescription(volumeInfo.has("description") ? volumeInfo.get("description").getTextValue() : null);
         ArrayNode industryIdentifiers = (ArrayNode) volumeInfo.get("industryIdentifiers");
         Iterator<JsonNode> iterator = industryIdentifiers.getElements();
         while (iterator.hasNext()) {
             JsonNode industryIdentifier = iterator.next();
             if ("ISBN_10".equals(industryIdentifier.get("type").getTextValue())) {
                 book.setIsbn10(industryIdentifier.get("identifier").getTextValue());
             } else if ("ISBN_13".equals(industryIdentifier.get("type").getTextValue())) {
                 book.setIsbn13(industryIdentifier.get("identifier").getTextValue());
             }
         }
         book.setLanguage(volumeInfo.get("language").getTextValue());
         book.setPageCount(volumeInfo.has("pageCount") ? volumeInfo.get("pageCount").getLongValue() : null);
         book.setPublishDate(formatter.parseDateTime(volumeInfo.get("publishedDate").getTextValue()).toDate());
         
         // Download the thumbnail
         JsonNode imageLinks = volumeInfo.get("imageLinks");
         if (imageLinks != null && imageLinks.has("thumbnail")) {
             String imageUrl = imageLinks.get("thumbnail").getTextValue();
             URLConnection imageConnection = new URL(imageUrl).openConnection();
             imageConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/29.0.1547.62 Safari/537.36");
             imageConnection.setConnectTimeout(10000);
             imageConnection.setReadTimeout(10000);
             InputStream imageInputStream = imageConnection.getInputStream();
             Path imagePath = Paths.get(DirectoryUtil.getBookDirectory().getPath(), book.getId());
             Files.copy(imageInputStream, imagePath, StandardCopyOption.REPLACE_EXISTING);
         }
         
         return book;
     }
     
     /**
      * Search a book by its ISBN using Open Library.
      * 
      * @return Book found
      * @throws Exception
      */
     private Book searchBookWithOpenLibrary(String isbn) throws Exception {
         openLibraryRateLimiter.acquire();
         
         URL url = new URL(String.format(Locale.ENGLISH, OPEN_LIBRARY_FORMAT, isbn));
         URLConnection connection = url.openConnection();
         connection.setRequestProperty("Accept-Charset", "utf-8");
         connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/29.0.1547.62 Safari/537.36");
         connection.setConnectTimeout(10000);
         connection.setReadTimeout(10000);
         InputStream inputStream = connection.getInputStream();
         ObjectMapper mapper = new ObjectMapper();
         JsonNode rootNode = mapper.readValue(inputStream, JsonNode.class);
         if (rootNode instanceof ArrayNode) {
             throw new Exception("No book found for ISBN: " + isbn);
         }
         
         JsonNode bookNode = rootNode.get("records").getElements().next();
         JsonNode details = bookNode.get("details").get("details");
         
         // Build the book
         Book book = new Book();
         book.setId(UUID.randomUUID().toString());
         book.setTitle(details.get("title").getTextValue());
         book.setSubtitle(details.has("subtitle") ? details.get("subtitle").getTextValue() : null);
        if (!details.has("authors") || details.get("authors").size() == 0) {
             throw new Exception("Book without author for ISBN: " + isbn);
         }
        book.setAuthor(details.get("authors").get(0).get("name").getTextValue());
         book.setDescription(details.has("first_sentence") ? details.get("first_sentence").get("value").getTextValue() : null);
         book.setIsbn10(details.has("isbn_10") && details.get("isbn_10").size() > 0 ? details.get("isbn_10").get(0).getTextValue() : null);
         book.setIsbn13(details.has("isbn_13") && details.get("isbn_13").size() > 0 ? details.get("isbn_13").get(0).getTextValue() : null);
         if (details.has("languages") && details.get("languages").size() > 0) {
             String language = details.get("languages").get(0).get("key").getTextValue();
             LanguageCode languageCode = LanguageCode.getByCode(language.split("/")[2]);
             book.setLanguage(languageCode.name());
         }
         book.setPageCount(details.has("number_of_pages") ? details.get("number_of_pages").getLongValue() : null);
         if (!details.has("publish_date")) {
             throw new Exception("Book without publication date for ISBN: " + isbn);
         }
         book.setPublishDate(details.has("publish_date") ? formatter.parseDateTime(details.get("publish_date").getTextValue()).toDate() : null);
         
         // Download the thumbnail
         if (details.has("covers") && details.get("covers").size() > 0) {
             String imageUrl = "http://covers.openlibrary.org/b/id/" + details.get("covers").get(0).getLongValue() + "-M.jpg";
             URLConnection imageConnection = new URL(imageUrl).openConnection();
             imageConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/29.0.1547.62 Safari/537.36");
             imageConnection.setConnectTimeout(10000);
             imageConnection.setReadTimeout(10000);
             InputStream imageInputStream = imageConnection.getInputStream();
             Path imagePath = Paths.get(DirectoryUtil.getBookDirectory().getPath(), book.getId());
             Files.copy(imageInputStream, imagePath, StandardCopyOption.REPLACE_EXISTING);
         }
         
         return book;
     }
     
     @Override
     protected void shutDown() throws Exception {
         executor.shutdown();
         executor.awaitTermination(10, TimeUnit.SECONDS);
         if (log.isInfoEnabled()) {
             log.info("Book data service stopped");
         }
     }
 }
