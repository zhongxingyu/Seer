 
 import java.io.*;
 import java.net.*;
 import java.sql.*;
 import java.util.*;
 
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.safety.Whitelist;
 import org.jsoup.select.Elements;
 
 public class Crawler implements Runnable
 {
    public final static String TABLE_URLS = "URLS";
    public final static String TABLE_IMAGES = "IMAGES";
    public final static String TABLE_WORD = "WORD";
    public final static String TABLE_IMGWORD = "IMGWORD";
    Connection connection;
    int NextURLID, NextImageURLID, NextURLIDScanned, urlIndex;
    public String domain;
    ArrayList<String> urlList;
    int MaxURLs;
    public Properties props;
    public static final int nThread = 1;
    public boolean reset = true;
    
    final Object monitor = new Object();
 
    Crawler(int MaxURLs, String domain, boolean reset, ArrayList<String> urlList) throws IOException
    {
       readProperties();
       setVariables();
       this.MaxURLs = MaxURLs;
       this.domain = domain;
       this.urlList = urlList;
       this.reset = reset;
    }
    
    Crawler(Connection connection)
    {
       this.connection = connection;
    }
    
    public void run()
    {
       try
       {
          crawl();
       }
       catch (Exception e) {}
    }
 
    public void readProperties() throws IOException
    {
       props = new Properties();
       FileInputStream in = new FileInputStream("WebContent/WEB-INF/database.properties");
       props.load(in);
       in.close();
    }
    
    public synchronized void  setProperties() throws IOException
    {
       props.setProperty("crawler.NextURLID",""+NextURLID);
       props.setProperty("crawler.NextURLIDScanned",""+NextURLIDScanned);
       props.setProperty("crawler.NextImageURLID",""+NextImageURLID);
       FileOutputStream out = new FileOutputStream("WebContent/WEB-INF/database.properties");
       props.store(out, null);
    }
    
    public synchronized void setVariables()
    {
       NextURLID = Integer.parseInt(props.getProperty("crawler.NextURLID"));
       NextURLIDScanned = Integer.parseInt(props.getProperty("crawler.NextURLIDScanned"));
       NextImageURLID = Integer.parseInt(props.getProperty("crawler.NextImageURLID"));
    }
 
    public void openConnection() throws SQLException, IOException
    {
       String drivers = props.getProperty("jdbc.drivers");
       if (drivers != null)
          System.setProperty("jdbc.drivers", drivers);
 
       String url = props.getProperty("jdbc.url");
       String username = props.getProperty("jdbc.username");
       String password = props.getProperty("jdbc.password");
 
       connection = DriverManager.getConnection(url, username, password);
    }
 
    public void createDB() throws SQLException, IOException
    {
       System.out.println("Connecting to Database...");
       openConnection();
 
       Statement stat = connection.createStatement();
 
       // Delete the tables first if any
       try
       {
          System.out.println("Removing Previously Created Tables...");
          stat.executeUpdate("DROP TABLE URLS");
          stat.executeUpdate("DROP TABLE IMAGES");
          stat.executeUpdate("DROP TABLE WORD");
          stat.executeUpdate("DROP TABLE IMGWORD");
       }
       catch (Exception e)
       {
       }
 
       // Create the tables
       System.out.println("Creating Tables...");
       stat.executeUpdate("CREATE TABLE URLS (urlid INT, url VARCHAR(20000), rank INT, title VARCHAR(200), description VARCHAR(200))");
       stat.executeUpdate("CREATE TABLE IMAGES (urlid INT, url VARCHAR(20000), rank INT)");
       stat.executeUpdate("CREATE TABLE WORD (word VARCHAR(2000), urllist VARCHAR(18000))");
       stat.executeUpdate("CREATE TABLE IMGWORD (word VARCHAR(2000), urllist VARCHAR(18000))");
       stat.close();
    }
 
    public synchronized boolean urlInDB(String urlFound, String table) throws SQLException, IOException
    {
       PreparedStatement pstmt = connection
             .prepareStatement("SELECT * FROM " + table + " WHERE url LIKE ?");
       pstmt.setString(1, urlFound);
       ResultSet result = pstmt.executeQuery();
 
       if (result.next())
       {
          //System.out.println("URL " + urlFound + " already in DB");
          pstmt.close();
          return true;
       }
       // System.out.println("URL "+urlFound+" not yet in DB");
       pstmt.close();
       return false;
    }
 
    public synchronized void insertURLInDB(String url, int urlid, String table)
          throws SQLException, IOException
    {
       PreparedStatement pstmt;
       if (table.equals(TABLE_URLS))
          pstmt = connection.prepareStatement("INSERT INTO " + table
                + " VALUES (?, ?, 1, '', '')");
       else
          pstmt = connection.prepareStatement("INSERT INTO " + table
                + " VALUES (?, ?, 1)");
       pstmt.setInt(1, urlid);
       pstmt.setString(2, url);
       pstmt.executeUpdate();
       pstmt.close();
    }
 
    public synchronized boolean wordInDB(String word, String table) throws SQLException, IOException
    {  
       PreparedStatement pstmt = connection
             .prepareStatement("SELECT * FROM " + table + " WHERE word LIKE ?");
       pstmt.setString(1, word);
       ResultSet result = pstmt.executeQuery();
 
       if (result.next())
       {
          //System.out.println("WORD " + word + " already in DB");
          pstmt.close();
          return true;
       }
       // System.out.println("URL "+urlFound+" not yet in DB");
       pstmt.close();
       return false;
    }
    
    public synchronized void insertWordInDB(String word, String urllist, String table) throws SQLException, IOException
    {  
       PreparedStatement pstmt = connection
             .prepareStatement("INSERT INTO " + table + " VALUES (?, ?)");
       pstmt.setString(1, word);
       pstmt.setString(2, urllist);
       pstmt.executeUpdate();
       pstmt.close();
    }
    
    public synchronized String getURLListFromDB(String word, String table) throws SQLException, IOException
    {  
       PreparedStatement pstmt = connection
             .prepareStatement("SELECT * FROM " + table + " WHERE word LIKE ?");
       pstmt.setString(1, word);
       ResultSet result = pstmt.executeQuery();
       result.next();
       //System.out.println("WORD " + word + ": " + result.getString(2));
       String list = result.getString(2);
       pstmt.close();
       return list;
    }
    
    public synchronized int getURLRankFromDB(String url, String table) throws SQLException, IOException
    {  
       PreparedStatement pstmt = connection
             .prepareStatement("SELECT * FROM " + table + " WHERE url LIKE ?");
       pstmt.setString(1, url);
       ResultSet result = pstmt.executeQuery();
       result.next();
       //System.out.println("WORD " + word + ": " + result.getString(2));
       int rank = result.getInt(3);
       pstmt.close();
       return rank;
    }
    
    public boolean isHTML(String link)
    {
       URL url;
       HttpURLConnection urlc = null;
       try
       {
          url = new URL(link);
          urlc = (HttpURLConnection) url.openConnection();
          urlc.setAllowUserInteraction(false);
          urlc.setDoInput(true);
          urlc.setDoOutput(false);
          urlc.setUseCaches(true);
          urlc.setRequestMethod("HEAD");
          urlc.connect();
          String mime = urlc.getContentType();
          if (mime.contains("text/html"))
          {
             return true;
          }
       }
       catch (Exception e)
       {
          //e.printStackTrace();
       }
       finally
       {
          if(urlc != null)
             urlc.disconnect();
       }
 
       return false;
    }
    
    public boolean imageExists(String url){
       
       HttpURLConnection con = null;
       
       try {
         HttpURLConnection.setFollowRedirects(false);
         // note : you may also need
         //        HttpURLConnection.setInstanceFollowRedirects(false)
         con =
            (HttpURLConnection) new URL(url).openConnection();
         con.setRequestMethod("HEAD");
         return (con.getResponseCode() == HttpURLConnection.HTTP_OK);
       }
       catch (Exception e) {
          //e.printStackTrace();
          return false;
       }
       finally {
          if(con != null)
             con.disconnect();
       }
     }
 
    public String getMetaTag(Document document, String attr)
    {
       Elements elements = document.select("meta[name=" + attr + "]");
       for (Element element : elements)
       {
          final String s = element.attr("content");
          if (s != null)
             return s;
       }
       elements = document.select("meta[property=" + attr + "]");
       for (Element element : elements)
       {
          final String s = element.attr("content");
          if (s != null)
             return s;
       }
       return null;
    }
 
    public void startCrawl() throws SQLException, IOException
    {
       if(!reset && NextURLIDScanned > 0)
       {
          openConnection();
          return;
       }
       
       createDB();
       
       NextURLID = 0;
       NextURLIDScanned = 0;
       NextImageURLID = 0;
       
       int urlID;
       for(String url : urlList)
       {
          urlID = NextURLID;
          insertURLInDB(url, urlID, TABLE_URLS);
          NextURLID++;
       }
       
       setProperties();
       
       return;
       
    }
    
    public synchronized String getNextURL(String table) throws SQLException, IOException
    {
       setVariables();
       urlIndex = NextURLIDScanned;
       Statement stat = connection.createStatement();
       ResultSet result = stat
             .executeQuery("SELECT * FROM " + table + " WHERE urlid = " + urlIndex);
       result.next();
       String url1 = result.getString(2);
       stat.close();
       
       NextURLIDScanned++;
       
       setProperties();
       
       return url1;
    }
    
    public synchronized Document parseURL(String url1) throws IOException
    {
       InputStream in = null;
       Document doc = null;
       try
       {
          System.out.println("[" + Thread.currentThread().getName() +"] [" + urlIndex + "] " + url1);
          in = new URL(url1).openStream();
          doc = Jsoup.parse(in, "ISO-8859-1", url1);
       }
       catch (Exception e)
       {
          //e.printStackTrace();
          System.out.println("Could Not Obtain Document...");
          System.out.println("Skipped.");
          System.out.println("-------------------------------------------");
          doc = null;
       }
       finally
       {
          if (in != null)
             in.close();
       }
       
       return doc;
    }
 
    public synchronized void extractDocumentURLs(Document doc)
          throws SQLException, IOException
    {
       Elements links;
       PreparedStatement pstmt;
 
       System.out.println("Extracting URLs...");
       links = doc.select("a");
 
       for (Element e : links)
       {
          String urlFound = e.attr("abs:href");
          urlFound = urlFound.trim();
          boolean found = urlInDB(urlFound, TABLE_URLS);
 
          if (found)
          {
             int rank = getURLRankFromDB(urlFound, TABLE_URLS);
             rank++;
 
             // Increment rank in url found
             pstmt = connection
                   .prepareStatement("UPDATE urls SET rank = ? WHERE url = ?");
             pstmt.setInt(1, rank);
             pstmt.setString(2, urlFound);
             pstmt.executeUpdate();
             pstmt.close();
          }
 
          if (!found && urlFound.contains("http://")
                && urlFound.contains(domain) && !urlFound.contains("#")
                && isHTML(urlFound))
          {
             insertURLInDB(urlFound, NextURLID, TABLE_URLS);
 
             NextURLID++;
             setProperties();
             // setVariables();
 
          }
       }
 
    }
 
    public synchronized void extractDocumentImages(Document doc)
          throws SQLException, IOException
    {
       Elements images;
       PreparedStatement pstmt;
 
       System.out.println("Extracting Images...");
       images = doc.select("img");
 
       for (Element e : images)
       {
          String imageFound = e.attr("abs:src");
          imageFound = imageFound.trim();
          boolean found = urlInDB(imageFound, TABLE_IMAGES);
 
          if (found)
          {
             int rank = getURLRankFromDB(imageFound, TABLE_IMAGES);
             rank++;
 
             // Increment rank in url found
             pstmt = connection
                   .prepareStatement("UPDATE images SET rank = ? WHERE url = ?");
             pstmt.setInt(1, rank);
             pstmt.setString(2, imageFound);
             pstmt.executeUpdate();
             pstmt.close();
          }
 
          if (!found && imageFound.contains("http://") && imageExists(imageFound))
          {
             insertURLInDB(imageFound, NextImageURLID, TABLE_IMAGES);
             NextImageURLID++;
             setProperties();
          }
       }
    }
 
    public void crawl() throws SQLException, IOException, InterruptedException
    {
       
       Document doc;
       String text, urllist;
       int imgIndexStart = 0;
       int imgIndexEnd = 0;
       String[] words;
       PreparedStatement pstmt;
       setVariables();
             
       // Let the first thread do some work before starting the rest of the threads.
       if(!Thread.currentThread().getName().equals("0"))
       {
          while(NextURLIDScanned < nThread)
             Thread.sleep(100);
       }
          
             
       while(NextURLIDScanned < NextURLID)
       {
          String url1 = getNextURL(TABLE_URLS);
          doc = parseURL(url1);
          if(doc == null)
             continue;
          
          synchronized (monitor)
          {
             // Get title
             String title = doc.title();
             
             // Get Description
             String description = getMetaTag(doc, "description");
             if (description == null && doc.body() != null)
                description = doc.body().text();
             else if(description == null)
                description = doc.text();
             if (description.length() > 100)
                description = description.substring(0, 100);
             
             System.out.println(description);
 
             // Update database with description and title
             pstmt = connection
                   .prepareStatement("UPDATE urls SET title = ?, description = ? WHERE url like ?");
             pstmt.setString(1, title);
             pstmt.setString(2, description);
             pstmt.setString(3, url1);
             pstmt.executeUpdate();
             pstmt.close();
             
             if(url1.contains("#") || url1.contains("&oldid=") || url1.contains("calendar/webevent.cgi?"))
                continue;
          }
 
          // Get all link elements
          if (NextURLID < MaxURLs)
          {
             extractDocumentURLs(doc);
          }
         
         imgIndexStart = NextImageURLID;
         extractDocumentImages(doc);
         imgIndexEnd = NextImageURLID;
  
          // Get document parsed text (no tags)
          System.out.println("Parsing Document...");
          doc.select(":containsOwn(\u00a0)").remove();
          text = doc.text();
          text = Jsoup.clean(text, Whitelist.relaxed());
          
          // Get each word of the document
          words = text.split("\\s+"); // split the string by white spaces to get individual words
          for(String word : words)
          {
             word = word.toLowerCase(); // Lower case all words
             word = word.replaceAll("[^A-Za-z0-9]", ""); // Remove punctuation
             if (word.matches("[a-zA-Z0-9]+")) // If the word is letters and numbers only
             {
                // Add words to urls table
                if (!wordInDB(word, TABLE_WORD))
                {
                   // If the word is not in the table, create a new entry
                   insertWordInDB(word, urlIndex + "", TABLE_WORD);           
                }
                else
                {
                   // Else, update the current entry with url1
                   urllist = getURLListFromDB(word, TABLE_WORD);
                   if(!urllist.contains(String.valueOf(urlIndex))) // Check to not add duplicates
                      urllist += "," + urlIndex;
                   synchronized (monitor)
                   {
                      pstmt = connection
                            .prepareStatement("UPDATE word SET urllist = ? WHERE word = ?");
                      pstmt.setString(1, urllist);
                      pstmt.setString(2, word);
                      pstmt.executeUpdate();
                      pstmt.close();
                   }
                }
                
                // Add words to image table
                if (!wordInDB(word, TABLE_IMGWORD))
                {
                   if(imgIndexStart != imgIndexEnd)
                   {
                      urllist = imgIndexStart + "";
                      for (int i = imgIndexStart + 1; i < imgIndexEnd; i++)
                         urllist = urllist + "," + i;
                      insertWordInDB(word, urllist, TABLE_IMGWORD);
                   }
                }
                else
                {            
                   // add the words to the imgWord table as well for each image found in the current doc
                   urllist = getURLListFromDB(word, TABLE_IMGWORD);
                   for (int i = imgIndexStart + 1; i < imgIndexEnd; i++)
                   {
                      if (!urllist.contains(String.valueOf(i))) // Check to not add duplicates
                         urllist += "," + i;
                   }
                   synchronized (monitor)
                   {
                      pstmt = connection
                            .prepareStatement("UPDATE imgword SET urllist = ? WHERE word = ?");
                      pstmt.setString(1, urllist);
                      pstmt.setString(2, word);
                      pstmt.executeUpdate();
                      pstmt.close();
                   }
                }
             }
          }
          
          System.out.println("Done.");
          System.out.println("-------------------------------------------");
          
          setProperties();
          //setVariables();
       }
    }
    
    public static Crawler crawlerWithArguments(String[] args) throws IOException
    {
       // Default values
       String domain = "cs.purdue.edu";
       String root = "http://www.cs.purdue.edu/";
       int MaxURLs = 1000;
       boolean reset = false;
       ArrayList<String> urlList = new ArrayList<String>();
       
       // Parse arguments
       for(int i = 0; i < args.length; i++)
       {
          if(args[i].equals("-u"))
          {
             MaxURLs = Integer.parseInt(args[++i]);
          }
          else if(args[i].equals("-d"))
          {
             domain = args[++i];
          }
          else if(args[i].equals("-r"))
          {
             reset = true;
          }
          else
          {
             urlList.add(args[i]);
          }
       }
       
       if(urlList.size() == 0)
          urlList.add(root);
       
       System.out.println("ARGS: webcrawl -u " + MaxURLs + " -d " + " -r " + reset + " " + domain + " " + urlList);
       Crawler crawler = new Crawler(MaxURLs, domain, reset, urlList);
       
       return crawler;
    }
    
    public static Crawler crawlerWithProperties() throws IOException
    {
       Properties props = new Properties();
       FileInputStream in = new FileInputStream("WebContent/WEB-INF/database.properties");
       props.load(in);
       in.close();
       
       String domain = props.getProperty("crawler.domain");
       String root = props.getProperty("crawler.root");
       int MaxURLs = Integer.parseInt(props.getProperty("crawler.maxurls"));
       boolean reset;
       if(props.getProperty("crawler.reset").equals("YES"))
          reset = true;
       else
          reset = false;
       ArrayList<String> urlList = new ArrayList<String>();
       urlList.add(root);
       
       System.out.println("PROPS: webcrawl -u " + MaxURLs + " -d " + " -r " + reset + " " + domain + " " + urlList);
       Crawler crawler = new Crawler(MaxURLs, domain, reset, urlList);
       
       return crawler;
       
    }
    
    public static Crawler createCrawler(String[] args) throws IOException
    {
       // Default Values
       String domain = "cs.purdue.edu";
       String root = "http://www.cs.purdue.edu/";
       int MaxURLs = 1000;
       boolean reset = true;
       ArrayList<String> urlList = new ArrayList<String>();
       
       File propertiesFile = new File("WebContent/WEB-INF/database.properties");
       Crawler crawler;
       
       // Get the necessary variables from args, props file, or defaults 
       
       if(args.length > 0)
          crawler = crawlerWithArguments(args);
       else if(args.length == 0 || propertiesFile.exists())
          crawler = crawlerWithProperties();
       else
       {
          urlList.add(root);
          System.out.println("DEFAULT: webcrawl -u " + MaxURLs + " -d " + " -r " + reset + " " + domain + " " + urlList);
          crawler = new Crawler(MaxURLs, domain, reset, urlList);
       }  
             
       return crawler;
    }
 
    public static void main(String[] args) throws IOException
    {     
       Crawler crawler = createCrawler(args);
       
       
       //Create the threads
       Thread threads[] = new Thread[nThread];
       for(int i = 0; i < threads.length; i++)
       {
          threads[i] = new Thread(crawler, "" + i);
       }      
       
       // Crawl that sucker
       try
       {
          System.out.println("Initializing Crawl Sequence...");
          crawler.startCrawl();
          System.out.println("Crawling...");
          System.out.println("-------------------------------------------");
          for(int i = 0; i < threads.length; i++)
          {
             threads[i].start();
          }
          
          for(int i = 0; i < threads.length; i++)
             threads[i].join();                          
       }
       catch (Exception e)
       {
          e.printStackTrace();
       }
       System.out.println("Crawling Complete.");
    }
 
 }
