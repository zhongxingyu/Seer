 import com.mongodb.*;
 
 import com.google.gdata.client.Query;
 import com.google.gdata.client.blogger.BloggerService;
 import com.google.gdata.data.*;
 import com.google.gdata.util.ServiceException;
 
 import java.sql.SQLException;
 import java.sql.Connection;
 import java.sql.Statement;
 import java.sql.ResultSet;
 import java.sql.DriverManager;
 
 import java.util.regex.*;
 import java.util.*;
 import java.net.*;
 import java.io.*;
 import java.text.Normalizer;
 import java.text.SimpleDateFormat;
 import java.util.concurrent.*;
  
 public class ReviewBlogs {
  
 	public static final String myConnString = "jdbc:mysql://localhost/bloganalysis?user=root&password=";
 	public static final int mongoPort = 27017;
 	public static final String mongoHost = "localhost";
 	public static final int numCrawler = 4;
 	public static Mongo mongoConn;
 	public static DB mongoDb;
 	public static DBCollection collPosts;
 	public static Connection mysqlConn;
 	public static Statement myStm;
 	
     public static void main(String[] args) throws Exception {		
 
 		mongoConn = new Mongo( mongoHost , mongoPort );
 		mongoDb = mongoConn.getDB( "blogdb" );
 		
 		try {
 			mongoDb.getCollectionNames();
 		} catch (Exception e) {
 			System.out.println("MongoDB Offline.");
 			System.exit(1);
 		}	
 
 		try {
 			mysqlConn = DriverManager.getConnection(myConnString);
 			myStm = mysqlConn.createStatement();
 			myStm.executeQuery("set wait_timeout = 7200");
 		} catch (Exception e) {
 			System.out.println("MySQL Offline.");
 			System.exit(1);
 		}
 		
 		collPosts = mongoDb.getCollection("posts");
 		collPosts.ensureIndex("postID");
 		collPosts.ensureIndex("blogID");
 		collPosts.ensureIndex("authorID");	
 
 		getBlogs();
 
 		Thread.sleep(1000); //For cleaning mongo cursos 
 
 		mongoConn.close();
         myStm.close();
     }
 	
 	public static void getBlogs() throws Exception 
 	{
 
 		BlockingQueue<String[]> queue = new ArrayBlockingQueue<String[]>(numCrawler*4);
 
 		CrawlerR[] crawler = new CrawlerR[numCrawler];
 		for (int i=0; i<crawler.length; i++) {
 			crawler[i] = new CrawlerR(queue);
 			crawler[i].start();
 		}
 
 		ResultSet rs = null;
 		String[] blogs;
 
 		while(true)
 		{
 			blogs = null;
 			myStm.executeQuery("SELECT CONCAT(profileID, '#' , blogs) as info FROM author WHERE Local = 'BR' and length(Blogs)>2 and retrieve=1 ORDER BY RAND() DESC LIMIT 1");
 			rs = myStm.getResultSet();
 			try {
 				if (rs.first()) {
 					blogs = Pattern.compile("#").split(rs.getString("info"));
 				}
 			} catch (Exception e) {}
 
 			if (blogs==null) break;
 
 			queue.put(blogs);
 
 		}
 
 		queue.clear();
 	    for (int i=0; i<crawler.length; i++)
 	        queue.put(CrawlerR.NO_MORE_WORK);		
 		for (int i=0; i<crawler.length; i++)
 			crawler[i].join();
 		
 	}
 
 }
 
 class CrawlerR extends Thread {
 
 	private BloggerService myService;
 	private Mongo mongoConn;
 	private DB mongoDb;
 	private DBCollection collPosts;	
 	private int r;
 	private String blog;
 	
 	public static Connection mysqlConn;
 	public static Statement myStm;
 
 	static final String[] NO_MORE_WORK = new String[]{};
 
 	BlockingQueue<String[]> q;
 
     CrawlerR(BlockingQueue<String[]> q) {
     	this.q = q;
 		try {
 			Random generator = new Random();
 			r = generator.nextInt(100);
 
 			myService = new BloggerService("Mongo-BlogFeed-"+r);
 			//myService.setReadTimeout(3000);
 
			mysqlConn = DriverManager.getConnection(MongoIterate.myConnString);
 			myStm = mysqlConn.createStatement();
 			myStm.executeQuery("set wait_timeout = 7200");
 
			mongoConn = new Mongo( MongoIterate.mongoHost , MongoIterate.mongoPort );
 			mongoDb = mongoConn.getDB( "blogdb" );
 			collPosts = mongoDb.getCollection("posts");   
 		} catch (Exception e) {
 			System.out.println(r+"bye:" + e.getMessage());
 		}
 		   
     }
     public void run() {
     	while (true) {
 
 	    	try { 
 				String[] info = q.take();
 				String[] blogs = null;
 				String profileID = "";
 
                 if (info == NO_MORE_WORK) {
                     break;
                 }
 
 				if (info.length == 2) {
 					profileID = info[0];
 					blogs = Pattern.compile(",").split(info[1]);
 				} else {
 					blogs = info;
 				}
 
 				Boolean bSet = false;
 	    		for (String blogFind : blogs)
 				{
 					blog = blogFind;
 					String blogID = blog.trim().replace("http:","").replace("/","");
 					
 					if (!mongoExist(blogID)) {
 						myStm.executeUpdate("UPDATE author SET retrieve = 0 WHERE profileID = '" + profileID + "' LIMIT 1");
 						System.out.print(", Dont:"+profileID);
 						bSet = true;
 					} else {
 						if (!bSet) {
 							myStm.executeUpdate("UPDATE author SET retrieve = 2 WHERE profileID = '" + profileID + "' LIMIT 1");
 							System.out.print(", Exist:"+profileID);
 						}
 					}
 
 				}
 
 			} catch (Exception e) {
 				System.out.println(r+"runEx:" + e.getMessage());
 			}
 
 		}
 
 		System.out.println("Bye("+r+")");
 		mongoConn.close();
 		try { myStm.close(); } catch (Exception e) {}
     }
 
 	private boolean mongoExist(final String blogUri) throws Exception {			
 		
 		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
 		
 		URL feedUrl = new URL("http://www.blogger.com/feeds/" + blogUri + "/posts/default");
 		if (!blogUri.matches("\\d+")) {
 			feedUrl = new URL("http://" + blogUri + "/feeds/posts/default");
 		} 
 
 		Query myQuery = new Query(feedUrl);
 		DateTime dtMin = DateTime.parseDate("2011-01-01");
 		myQuery.setPublishedMin(dtMin);	
 		myQuery.setMaxResults(1);
 
 		try {
 			Feed resultFeed = myService.query(myQuery, Feed.class);
 
 			Matcher matcher = Pattern.compile("\\d+").matcher(resultFeed.getSelfLink().getHref());
 			if (matcher.find()) {
 				String blogID = matcher.group();
 				
 				int size = resultFeed.getTotalResults();
 
 				BasicDBObject doc = new BasicDBObject();
 				doc.put("blogID", blogID);
 
 			    if (size > 0 && collPosts.find(doc).size() > 0) {
 					return true;
 			    }
 			}
 		} catch (ServiceException e) {
 			System.out.println(r+"ServcEx: "+ e.getMessage()+">"+blog);
 			if (e.getMessage().matches(".*Bad.*")) return true;
 			if (e.getMessage().matches(".*Not Found.*")) return true;
 		} catch (Exception e) {
 			System.out.println(r+"feedEx: " + e.getMessage()+">"+blog);
 		}
 
 	    return false;
 		
 	}
 	
 	
 
 }
