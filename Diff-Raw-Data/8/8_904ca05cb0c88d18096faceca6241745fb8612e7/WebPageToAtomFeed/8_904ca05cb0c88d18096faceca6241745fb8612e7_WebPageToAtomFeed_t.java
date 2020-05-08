 package com.rackspace.webpage2atomfeed;
 
 import org.apache.abdera.Abdera;
 import org.apache.abdera.model.Document;
 import org.apache.abdera.model.Entry;
 import org.apache.abdera.model.Feed;
 import org.apache.abdera.parser.Parser;
 import org.apache.abdera.writer.Writer;
 import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.HttpException;
 import org.apache.commons.httpclient.HttpStatus;
 import org.apache.commons.httpclient.URI;
 import org.apache.commons.httpclient.URIException;
 import org.apache.commons.httpclient.methods.GetMethod;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import static java.lang.String.format;
 import static org.apache.commons.httpclient.cookie.CookiePolicy.IGNORE_COOKIES;
 import static org.apache.commons.httpclient.params.HttpMethodParams.RETRY_HANDLER;
 
 /**
  * Take any web page and turn it into an Atom feed.
  * </p>
  * All configuration is done via src/main/resources/WebPageToAtomFeed.properties
  * </p>
  * See the <a href="https://github.com/rackerlabs/WebPageToAtomFeed">GitHub repo</a>.
  */
 public class WebPageToAtomFeed {
     private final Logger logger = LoggerFactory.getLogger(WebPageToAtomFeed.class);
     private boolean dryRunMode;
 
     public static void main(String[] args) {
         WebPageToAtomFeed webPageToAtomFeed = new WebPageToAtomFeed();
         webPageToAtomFeed.generateFeeds();
     }
 
     /**
      * Generate all of the feeds.
      */
     private void generateFeeds() {
         try {
             Properties props = getProps();
             setDryRunMode(Boolean.valueOf((props.getProperty("dry.run.mode", "false"))));
 
             if (!dryRunMode) logger.info("BEGIN Generating Feeds");
 
             List<Map<FeedProperty, String>> feedProps = getFeedProps(props);
             Map<String, String> titleToWebPage = getWebPages(feedProps);
             Map<String, Feed> titleToFeed = getFeeds(feedProps, titleToWebPage);
             writeFeeds(feedProps, titleToFeed);
         }
         catch (Exception e) {
             e.printStackTrace();
         }
         finally {
             if (!dryRunMode) logger.info("END Generating Feeds");
         }
     }
 
     /**
      * Load the Properties that will control the generation of the feeds. File defaults to
      * "src/main/resources/WebPageToAtomFeed.properties" but can be overridden by setting props.filename
      *
      * @return Properties for all of the feeds.
      */
     protected Properties getProps() throws IOException {
         String propsFilename = System.getProperty("props.filename", "src/main/resources/WebPageToAtomFeed.properties");
         File propsFile = new File(propsFilename);
 
        if (dryRunMode) System.out.format("Loading property file %s%n", propsFile.getAbsolutePath());
 
         Properties props = new Properties();
         props.load(new FileInputStream(propsFile));
 
         return props;
     }
 
     /**
      * Get the properties for all of the feeds.
      *
      * @param props Properties for all the feeds.
      * @return A List of feeds. Each feed is a Map of their feed properties to the values.
      */
     protected List<Map<FeedProperty, String>> getFeedProps(Properties props) {
         List<Map<FeedProperty, String>> feedProps = new ArrayList<>();
         int feedIndex = 0;
 
         while (hasTitle(feedIndex, props)) {
             Map<FeedProperty, String> tempFeedProps = new HashMap<>();
 
             for (FeedProperty feedAttribute : FeedProperty.values()) {
                 String key = format("feed.%s.%s", feedIndex, feedAttribute);
                 String value = props.getProperty(key);
 
                 tempFeedProps.put(feedAttribute, value);
             }
 
             feedProps.add(tempFeedProps);
             feedIndex++;
         }
 
         if (dryRunMode) System.out.format("Loaded properties for %s feeds%n", feedIndex);
 
         return feedProps;
     }
 
     private boolean hasTitle(int feedIndex, Properties props) {
         return props.getProperty(format("feed.%s.%s", feedIndex, FeedProperty.FEED_TITLE)) != null;
     }
 
     /**
      * Get the source code of web pages.
      *
      * @param feedProps A List of feeds.
      * @return A Map of feed titles to web page source code.
      */
     protected Map<String, String> getWebPages(List<Map<FeedProperty, String>> feedProps)
             throws IOException {
         Map<String, String> titleToPage = new HashMap<>(feedProps.size());
 
         for (Map<FeedProperty, String> feedProp : feedProps) {
             String pageSource = getWebPageSource(feedProp.get(FeedProperty.FEED_URL));
             titleToPage.put(feedProp.get(FeedProperty.FEED_TITLE), pageSource);
         }
 
         return titleToPage;
     }
 
     /**
      * Get the source code of a web page.
      *
      * @param url URL of the web page to get.
      * @return The web page source code (with all new lines removed).
      */
     protected String getWebPageSource(String url) throws IOException {
         String pageSource = "";
 
         HttpClient client = new HttpClient();
         client.getParams().setCookiePolicy(IGNORE_COOKIES);
 
         GetMethod getMethod = new GetMethod(url);
         getMethod.getParams().setParameter(RETRY_HANDLER, new DefaultHttpMethodRetryHandler(3, false));
 
         if (dryRunMode) System.out.format("Loading web page at %s%n", url);
 
         try {
             int statusCode = client.executeMethod(getMethod);
             byte[] responseBody = getMethod.getResponseBody();
             pageSource = new String(responseBody);
 
             if (statusCode != HttpStatus.SC_OK) {
                 String message = format("%s%n%s", getMethod.getStatusLine(), pageSource);
                 throw new HttpException(message);
             }
         } finally {
             getMethod.releaseConnection();
         }
 
         return pageSource.replaceAll("\\r\\n|\\r|\\n", " ");
     }
 
     /**
      * Turn web page source code into Atom feeds.
      *
      * @param feedProps A List of feeds.
      * @param titleToPage A Map of feed titles to web page source code.
      * @return A Map of feed titles to Feeds
      */
     protected Map<String, Feed> getFeeds(List<Map<FeedProperty, String>> feedProps, Map<String, String> titleToPage)
             throws IOException {
         Map<String, Feed> feeds = new HashMap<>(feedProps.size());
         Abdera abdera = new Abdera();
 
         for (Map<FeedProperty, String> feedProp : feedProps) {
             Feed feed = abdera.newFeed();
             feed.setId(feedProp.get(FeedProperty.FEED_TITLE));
             feed.setTitle(feedProp.get(FeedProperty.FEED_TITLE));
             feed.setSubtitle(feedProp.get(FeedProperty.FEED_DESCRIPTION));
             feed.setUpdated(new Date());
             feed.addAuthor(feedProp.get(FeedProperty.FEED_AUTHOR));
             feed.addLink(feedProp.get(FeedProperty.FEED_URL), "self");
             feed.addLink(feedProp.get(FeedProperty.FEED_URL_HOME));
 
             if (dryRunMode) System.out.format("Parsing feed for %s%n", feed.getTitle());
 
             String pageSource = titleToPage.get(feedProp.get(FeedProperty.FEED_TITLE));
 
             Pattern pagePattern = Pattern.compile(feedProp.get(FeedProperty.PAGE_PATTERN));
             Matcher pageMatcher = pagePattern.matcher(pageSource);
 
             if (pageMatcher.find()) {
                 if (dryRunMode) System.out.format("Matched page pattern %s%n", pagePattern);
 
                 pageSource = pageMatcher.group(1).trim();
             }
 
             Pattern itemPattern = Pattern.compile(feedProp.get(FeedProperty.ENTRY_PATTERN));
             Matcher itemMatcher = itemPattern.matcher(pageSource);
             int maxEntries = Integer.valueOf(feedProp.get(FeedProperty.ENTRY_MAX));
 
             while (itemMatcher.find() && feed.getEntries().size() < maxEntries) {
                 if (dryRunMode) System.out.format("Matched item pattern %s%n", itemPattern);
 
                 Entry entry = feed.addEntry();
                 entry.setUpdated(new Date());
 
                 int titleGroup = Integer.valueOf(feedProp.get(FeedProperty.ENTRY_TITLE_GROUP));
                 String title = itemMatcher.group(titleGroup).trim();
                 entry.setTitle(title);
 
                 if (dryRunMode) System.out.format("  title = %s%n", title);
 
                 int linkGroup = Integer.valueOf(feedProp.get(FeedProperty.ENTRY_URL_GROUP));
                 String link = itemMatcher.group(linkGroup).trim();
                 String absoluteLink = getAbsoluteLink(feedProp.get(FeedProperty.FEED_URL), link);
                 entry.setId(absoluteLink);
                 entry.addLink(absoluteLink);
 
                 if (dryRunMode) System.out.format("  link = %s%n", absoluteLink);
 
                 if (!"".equals(feedProp.get(FeedProperty.ENTRY_CONTENT_GROUP))) {
                     int contentGroup = Integer.valueOf(feedProp.get(FeedProperty.ENTRY_CONTENT_GROUP));
                     String content = itemMatcher.group(contentGroup).trim();
                     entry.setSummaryAsHtml(content);
 
                     if (dryRunMode) System.out.format("  content = %s%n", content);
                 }
             }
 
             feeds.put(feedProp.get(FeedProperty.FEED_TITLE), feed);
         }
 
         return feeds;
     }
 
     private String getAbsoluteLink(String feedLink, String link) throws URIException {
         String absoluteLink = link;
 
         if (link.startsWith("#")) {
             absoluteLink = feedLink + link;
         } else if (link.startsWith("/")) {
             URI feedURI = new URI(feedLink, true);
             absoluteLink = format("%s://%s%s", feedURI.getScheme(), feedURI.getHost(), link);
         }
 
         return absoluteLink;
     }
 
     /**
      * Write the feeds to disk.
      *
      * @param feedProps A List of feeds.
      * @param titleToFeed A Map of feed titles to web page source code.
      */
     protected void writeFeeds(List<Map<FeedProperty, String>> feedProps, Map<String, Feed> titleToFeed)
             throws IOException {
         Abdera abdera = new Abdera();
         Parser parser = abdera.getParser();
 
         for (Map<FeedProperty, String> feedProp : feedProps) {
             File feedFile = new File(feedProp.get(FeedProperty.FEED_FILE));
             Feed feedFromWebPage = titleToFeed.get(feedProp.get(FeedProperty.FEED_TITLE));
 
             if (dryRunMode) {
                 System.out.format("File: %s%n%n", feedFile.getAbsolutePath());
 
                 Writer writer = abdera.getWriterFactory().getWriter("prettyxml");
                 writer.writeTo(feedFromWebPage, System.out);
 
                 System.out.format("%n%n");
             }
             else if (!feedFile.exists()) {
                 feedFromWebPage.writeTo(new FileWriter(feedFile));
 
                 if (!dryRunMode) {
                    logger.info(format("Created new Atom file %s (%d new entries)",
                             feedFile.getAbsolutePath(), feedFromWebPage.getEntries().size()));
                 }
             }
             else {
                 Document<Feed> doc = parser.parse(new FileReader(feedFile));
                 Feed feedFromFilesystem = doc.getRoot();
 
                 String firstEntryIdFromWebpage = feedFromWebPage.getEntries().get(0).getId().toString();
                 String firstEntryIdFromFilesystem = feedFromFilesystem.getEntries().get(0).getId().toString();
 
                 if (!firstEntryIdFromFilesystem.equals(firstEntryIdFromWebpage)) {
                     int newEntryCount = 0;
 
                     for (int i = 0; i < feedFromWebPage.getEntries().size(); i++) {
                         Entry entryFromWebpage = feedFromWebPage.getEntries().get(i);
                         String entryIdFromWebpage = entryFromWebpage.getId().toString();
 
                         if (!entryIdFromWebpage.equals(firstEntryIdFromFilesystem)) {
                             feedFromFilesystem.insertEntry(Entry.class.cast(entryFromWebpage.clone()));
                             newEntryCount++;
                         }
                         else {
                             break;
                         }
                     }
 
                     feedFromFilesystem.setUpdated(new Date());
                     feedFromFilesystem.writeTo(new FileWriter(feedFile));
 
                     if (!dryRunMode) {
                        logger.info(format("Appended to Atom file %s (%d new entries)",
                                 feedFile.getAbsolutePath(), newEntryCount));
                     }
                 }
             }
         }
     }
 
     /**
      * Dry run mode will cause nothing to be written to disk and all output sent to stdout.
      *
      * @param dryRunMode Set to true to enable dry run mode.
      */
     public void setDryRunMode(boolean dryRunMode) {
         this.dryRunMode = dryRunMode;
     }
 }
