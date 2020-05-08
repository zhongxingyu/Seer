 package controllers;
 
 import play.*;
 import play.mvc.*;
 
 import static play.libs.Json.toJson;
 
 import org.codehaus.jackson.JsonNode;
 
 import views.html.*;
 import models.*;
 
 import java.util.*;
 
 import com.google.code.morphia.Datastore;
 import com.google.code.morphia.Morphia;
 import com.google.code.morphia.mapping.Mapper;
 import com.google.code.morphia.query.UpdateOperations;
 import com.mongodb.Mongo;
 import com.mongodb.MongoURI;
 import com.mongodb.DB;
 import java.net.*;
 import java.lang.RuntimeException;
 
 public class Application extends Controller {
   
   public static Result index() {
     return ok(index.render("Your new application is ready."));
   }
 
   public static Result books() {
     /*
     List<Book> books = new ArrayList<Book>();
     try {
       Morphia morphia = new Morphia();
       Mongo mongo = new Mongo();
       Datastore ds = morphia.createDatastore(mongo, "test");
       for(Book book : ds.find(Book.class)) {
         books.add(book);
       }
     } catch (UnknownHostException e) {
       throw new RuntimeException();
     }*/
     return ok(toJson(getBooks()));
   }
 
   public static Result book(String id) {
     return ok(toJson(getBook(id)));
   }
 
   public static Result booksByTitle(String title) {
     return ok(toJson(getBooksByTitle(title)));
   }
 
   public static Result searchBooks(String query) {
     return ok(toJson(findBooks(query)));
   }
 
   public static Result booksByCampus(String campus) {
     return ok(toJson(getBooksByCampus(campus)));
   }
 
   public static Result addBook() {
     JsonNode json = request().body().asJson();
     //System.out.println(request().body());
     //System.out.println(json.toString());
     Book book = new Book();
     book.setTitle(json.findPath("title").getTextValue());
     book.setAuthor(json.findPath("author").getTextValue());
     book.setPublisher(json.findPath("publisher").getTextValue());
     book.setListPrice(json.findPath("listPrice").getDoubleValue());
     book.setIsbn(json.findPath("isbn").getTextValue());
     book.setImage(json.findPath("image").getTextValue());
     saveBook(book);
     return ok();
   }
 
   private static List<Book> getBooksByCampus(String campus) {
     List<Book> booksAtCampus = new ArrayList<Book>();
     for(User user : getAllUsers()) {
       if(user.getCampus().equalsIgnoreCase(campus)) {
         booksAtCampus.addAll(user.getBooks());
       }
     }
     return booksAtCampus;
   }
 
   private static List<User> getAllUsers() {
     List<User> users = new ArrayList<User>();
     User user = new User();
     user.setCampus("Drake");
     user.setBooks(getBooks());
     users.add(user);
 
     User isuUser = new User();
     isuUser.setCampus("Iowa State");
     isuUser.setBooks(getBooks());
     users.add(isuUser);
 
     return users;
   }
 
   private static Book getBook(String id) {
     List<Book> books = getBooks();
     Book returnedBook = null;
     for (Book book : books) {
      if(book.getId().toString().equalsIgnoreCase(id)) {
         returnedBook = book;
       }
     }
     return returnedBook;
   }
       
 
   private static List<Book> getBooks() {
     List<Book> booksList = new ArrayList<Book>();
     /*
     Book book = new Book();
     book.setTitle("The Clean Coder");
     book.setAuthor("Robert C. Martin");
     book.setListPrice(39.99);
     book.setPublisher("Prentice Hall");
     book.setIsbn("0-13-708107-3");
     book.setOldId(1);
     booksList.add(book);
     Book otherBook = new Book();
     otherBook.setTitle("The Pragmatic Programmer");
     otherBook.setAuthor("Andy Hunt");
     otherBook.setListPrice(49.99);
     otherBook.setPublisher("Prag Prog");
     otherBook.setIsbn("1-22-222202-4");
     otherBook.setOldId(2);
     booksList.add(otherBook);
     */    
 
     try {
       Morphia morphia = new Morphia();
       String mongo_url = System.getenv("MONGOHQ_URL");
       if (mongo_url == null ) {
         mongo_url  = System.getProperty("MONGOHQ_URL");
       }
       MongoURI mongoUri = new MongoURI(mongo_url);
       DB connectedDB = mongoUri.connectDB();
       if (mongoUri.getUsername() != null) {
         connectedDB.authenticate(mongoUri.getUsername(), mongoUri.getPassword());
       }
         
       //Mongo mongo = mongoUri.connect();
       Mongo mongo = connectedDB.getMongo();
       Datastore ds = morphia.createDatastore(mongo, "app9665938");
       for(Book book : ds.find(Book.class)) {
         booksList.add(book);
       }
     } catch (UnknownHostException e) {
       //fuck it.  it's a hack-a-thon
       throw new RuntimeException(e);
     }    
 
     return booksList;
   }
 
   private static void saveBook(Book book) {
     try {
        Morphia morphia = new Morphia();
       String mongo_url = System.getenv("MONGOHQ_URL");
       if (mongo_url == null ) {
         mongo_url  = System.getProperty("MONGOHQ_URL");
       }
       MongoURI mongoUri = new MongoURI(mongo_url);
       DB connectedDB = mongoUri.connectDB();
       if (mongoUri.getUsername() != null) {
         connectedDB.authenticate(mongoUri.getUsername(), mongoUri.getPassword());
       }
 
       //Mongo mongo = mongoUri.connect();
       Mongo mongo = connectedDB.getMongo();
       Datastore ds = morphia.createDatastore(mongo, "app9665938");
       ds.save(book);
     } catch (UnknownHostException e) {
       throw new RuntimeException(e);
     }
   } 
 
   private static List<Book> getBooksByTitle(String title) {
     List<Book> books = getBooks();
     List<Book> returnedBooks = new ArrayList<Book>();
     for (Book book : books) {
       if(book.getTitle().indexOf(title) != -1) {
         returnedBooks.add(book);
       }
     }
     return returnedBooks;
   }
 
   private static List<Book> findBooks(String query) {
     List<Book> books = getBooks();
     List<Book> returnedBooks = new ArrayList<Book>();
     for (Book book : books) {
       if(book.getTitle().indexOf(query) != -1) {
         returnedBooks.add(book);
       }
     }
     for (Book book : books) {
       if(book.getAuthor().indexOf(query) != -1) {
         returnedBooks.add(book);
       }
     }
     for (Book book : books) {
       if(book.getIsbn().indexOf(query) != -1) {
         returnedBooks.add(book);
       }
     }
     return returnedBooks;
   }
 }
