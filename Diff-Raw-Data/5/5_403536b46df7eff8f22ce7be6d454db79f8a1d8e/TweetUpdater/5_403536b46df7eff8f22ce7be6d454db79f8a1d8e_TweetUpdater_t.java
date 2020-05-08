 package api;
 
 import java.util.HashMap;
 import java.util.logging.Logger;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ResponseHandler;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.BasicResponseHandler;
 import org.json.simple.JSONObject;
 
 import src.Util;
 import web.TweetRepliesReader;
 
 
 public class TweetUpdater extends TwitterApp {
 	private final static Logger logger = Logger.getLogger(TweetSearch.class.getName());
 	private static String REQUESTURL = "https://api.twitter.com/1.1/statuses/show/";
 	private static String TWRTCOUNTER = "retweet_count";
 	private static String TWFVCOUNTER = "favorite_count";
 	private static String TWAUTHOR = "user";
 	private static String TWAUTHORHANDLE = "screen_name";
 	private static String TWID = "id_str";
 	private static String TWITTERURL = "http://twitter.com/";
 	private TweetRepliesReader twRepReader = null;
    private static Integer MAX_ATTEMPTS = 1;
 	
 	
 	public TweetUpdater() {
 		super();
 		twRepReader = new TweetRepliesReader();
 	}
 	
 	public HashMap<String,Long> updateTweetMetrics(String idTweet) 
 	throws Exception 
 	{
 		HashMap<String,Long> newMetrics = new HashMap<String,Long>();
         int attemptCounter = 0;
 		
 		String fullURL = REQUESTURL+idTweet+".json";
 		
 		Util.printMessage("Remaining requests  " + remainingRequests + " in this time window","info",logger);
 		
 		Util.printMessage("Updating metrics of the tweet: " + idTweet,"info",logger);
 		
 		HttpGet httpGet = new HttpGet(fullURL);
 		consumer.sign(httpGet);
 		
 		HttpResponse response = null;
 		if (remainingRequests == 0) {
 			response = pause(httpGet);
 		}
 		else {
 			response = doRequest(httpGet);
 		}
 		
		while (response.getStatusLine().getStatusCode() != 200 && attemptCounter <= MAX_ATTEMPTS) {
 			if (response.getStatusLine().getStatusCode() == 401 ||
 	        	response.getStatusLine().getStatusCode() == 406) {
 	        	Util.printMessage("Ingnoring invalid URL: " + fullURL, "severe",logger);
 	        	return newMetrics;
 	        }
 			else {
                 attemptCounter += 1;
 				Util.printMessage("Wrong Twitter API response code, got: " + 
 						  		  response.getStatusLine().getStatusCode() + 
 						  		  " expected 200","info",logger);
 				Thread.sleep(30000); //Wait for 30 seconds and try again
 				response = doRequest(httpGet);
 			}
 		}
 		
         if (attemptCounter <= MAX_ATTEMPTS) {	
             ResponseHandler<String> handler = new BasicResponseHandler();
             String body = handler.handleResponse(response);
             
             Object obj = parser.parse(body);
             JSONObject tweet = (JSONObject) obj;
             newMetrics.put("retweets", (Long) tweet.get(TWRTCOUNTER));
             newMetrics.put("favorites", (Long) tweet.get(TWFVCOUNTER));
             JSONObject authorObj = (JSONObject) tweet.get(TWAUTHOR);
             String author = (String) authorObj.get(TWAUTHORHANDLE);
             String statusURL = TWITTERURL+author+"/status/"+tweet.get(TWID);
             long replies = twRepReader.getReplies(statusURL);
             newMetrics.put("replies", replies);
         }
         else {
             Util.printMessage("Couldn't find the tweet: " + idTweet,"info",logger);
         }
 		
 		return newMetrics;
 	}
 }
