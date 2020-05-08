 /**
  * @author qiaoyu
  * @date Arp 6, 2013
  */
 
 package edu.columbia.watson.twitter;
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Vector;
 import java.util.concurrent.CountDownLatch;
 import java.util.zip.GZIPOutputStream;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.ParseException;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.concurrent.FutureCallback;
 import org.apache.http.impl.nio.client.DefaultHttpAsyncClient;
 import org.apache.http.nio.client.HttpAsyncClient;
 import org.apache.http.nio.reactor.IOReactorException;
 import org.apache.http.params.CoreConnectionPNames;
 import org.apache.http.util.EntityUtils;
 import org.apache.log4j.Logger;
 
 
 public class TwitterCrawler {
 	private static Logger logger = Logger.getLogger(TwitterCrawler.class);
 	private HttpAsyncClient httpclient;
 	private static Vector<Tweet> resultVector = new Vector<Tweet>();
 	//use to extract texts from HTML 
 	private static final String tweetStartMark = "<p class=\"js-tweet-text tweet-text \">";
 	private static final String tweetEndMark = "</p>";
 	private static final String posStartMark = "<a class=\"tweet-geo-text\"";
 	private static final String posEndMark = "</a>";
 	private static final String dateStartMark = "class=\"tweet-timestamp js-permalink js-nav\" title=";
 	private static final String dateEndMark = ">";
 		
 	private static CountDownLatch latch;
 	
 	public TwitterCrawler()
 	{
 		logger.info("Begin...");
 	}
 	
 	
 	private static class TweetParser implements FutureCallback<HttpResponse>{
 		private static Logger logger = Logger.getLogger(TweetParser.class);
 		private String sourceURL;
 		
 		public TweetParser(String source){
 			sourceURL = source;
 		}
 		
 		private String getContent(String responseString, String startMark, String endMark){
 			int startIndex = responseString.indexOf(startMark);
 			if (startIndex == -1)
 				return "";
 			int endIndex = responseString.indexOf(endMark, startIndex + startMark.length());
 			if (endIndex == -1)
 				return "";
 			return responseString.substring(startIndex + startMark.length(), endIndex);
 		}
 		
         public void completed(final HttpResponse response) {
             latch.countDown();
             try {
 				String responseString = EntityUtils.toString(response.getEntity());	//convert the http response into plain text
 
 				String status = getContent(responseString,tweetStartMark,tweetEndMark);
 				if (status == null || status.length() == 0)
 					return;
 				String date = getContent(responseString, dateStartMark, dateEndMark);
 				String pos = getContent(responseString, posStartMark, posEndMark);
 				String[] splitted = sourceURL.split("/");
 							
 				Tweet result = new Tweet(splitted[5],splitted[3],status,date,pos);
 				resultVector.add(result);
 									
 				if (resultVector.size() % 1000 == 0)		//log once every 1000 tweets
 					logger.info("resultVector size = " + resultVector.size() + ", finished crawling url = " + sourceURL);
 				logger.debug("url = " + sourceURL + "result =" + result.toString());
 				
 			} catch (ParseException e) {
 				logger.error("Error parsing http response, url = " + sourceURL + ", status = " + response.getStatusLine());
 			} catch (IOException e) {
 				logger.error("IO expection, url = ");
 			}                    
         }
 
         public void failed(final Exception ex) {
             latch.countDown();
             logger.error("Failed to get response, url = " + sourceURL + ", error code = " + ex.toString());
         }
 
         public void cancelled() {
             latch.countDown();
             logger.error("Request cancelled for url = " + sourceURL);
         }
 	}
 	
 	private String getUrl(long id, String username) {
 		return String.format("https://twitter.com/%s/status/%d", username, id);
 	}
 	
 	
 	public void crawl(String dataFileName, String outputFileName) throws Exception
 	{		
 		try {
 			httpclient = new DefaultHttpAsyncClient();
 	        httpclient.getParams()
             .setIntParameter(CoreConnectionPNames.SO_TIMEOUT, 3 * 60 * 1000)		//3 min socket timeout
             .setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 60 * 60 * 1000)		// 1 hour connection timeout
             .setIntParameter(CoreConnectionPNames.SOCKET_BUFFER_SIZE, 100 * 1024)
             .setBooleanParameter(CoreConnectionPNames.TCP_NODELAY, true);
 
 		} catch (IOReactorException e) {
 			logger.error("Error creating DefaultHttpAsyncClient!");
 			throw new Exception(e);
 		}
		
 		List<HttpGet> requests = new ArrayList<HttpGet>();
 		
 		int requestSize = 0;
 		try{
 			BufferedReader in = new BufferedReader(new FileReader(dataFileName));
 			while (in.ready()) {
 				  requestSize++;
 				  String line = in.readLine();		//each data file contains ~10000 lines, so it is reasonable to store them entirely in memory
 				  String[] arr = line.split("\t");
 				  long id = Long.parseLong(arr[0]);
		          String username = (arr.length > 1) ? arr[1] : "a";
 		          String url = getUrl(id, username);
 		          requests.add(new HttpGet(url));			//add each url to httpget list
 			}
 			in.close();
 		} catch (FileNotFoundException e){
         	String localhostname = java.net.InetAddress.getLocalHost().getHostName();
         	String subject = "Input file not found, input filename = " + dataFileName + ", error message = " + e.getMessage()
         					+ "\n" + "hostname=" + localhostname + ", time = " + new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date());
 
         	Emailer.sendmail("Twitter-Crawler Error", "twitter-semantic-search@lists.cs.columbia.edu", "qiaoyu.yu@gmail.com", subject);
        	
 			logger.error("Input file not found, filename = " + dataFileName);
 			throw new Exception(e);
 		}
 		
 //		try {
 //        	out = new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(outputFileName + ".gz")), "UTF-8");      
 //        }
 //        catch (FileNotFoundException e){
 //        	logger.error("Error creating output file, output filename = " + outputFileName + ", error message = " + e.getMessage());        	
 //        }	
 //		
         httpclient.start();
         try {
             latch = new CountDownLatch(requests.size());         	//make MAX_CONNECTION concurrent connections
             
             for (final HttpGet request: requests) {
                 httpclient.execute(request, new TweetParser(request.getURI().toString()));
             }
             latch.await();
             logger.info("Done crawling. Request set size = " + requestSize + ", Response set size = " + resultVector.size());
         } finally {
             httpclient.shutdown();
         }
                 
       //  out.close();
         OutputStreamWriter out = null;		//write result to a gzip file
         try {
         	out = new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(outputFileName + ".gz")), "UTF-8");
             for (Tweet tweet : resultVector){
             	 out.write(tweet.toString() + "\n");
             }           
         }
         catch (FileNotFoundException e){
         	String localhostname = java.net.InetAddress.getLocalHost().getHostName();
         	String subject = "Error creating output file, output filename = " + outputFileName + ", error message = " + e.getMessage()
         					+ "\n" + "hostname=" + localhostname + ", time = " + new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date());
 
         	Emailer.sendmail("Twitter-Crawler Error", "twitter-semantic-search@lists.cs.columbia.edu", "qiaoyu.yu@gmail.com", subject);
         	
         	logger.error("Error creating output file, output filename = " + outputFileName + ", error message = " + e.getMessage());
         	
         }
         finally {
             if (out != null) 
             	out.close();
         }
 	}
 		
 	public static void main(String[] args) throws Exception {
 		if (args.length != 2){
 			System.out.println("Usage: run.sh edu.columbia.watson.twitter.TwitterCrawler datfile outputfile");
 			return;
 		}
 
 		TwitterCrawler twitterCrawler = new TwitterCrawler();
 		twitterCrawler.crawl(args[0], args[1]);
 
     }
 }
