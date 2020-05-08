 package Access;
 
 //Original code at http://www.java-tutorial.ch/framework/twitter-with-java-tutorial
 import java.io.BufferedReader;
 import java.util.ArrayList;
 import java.util.List;
 import java.io.BufferedWriter;
 import java.io.DataInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.Writer;
 
 import twitter4j.Twitter;
 import twitter4j.TwitterException;
 import twitter4j.TwitterFactory;
 import twitter4j.auth.AccessToken;
 //import twitter4j.auth.OAuthToken;
 import twitter4j.auth.RequestToken;
 import twitter4j.Status;
 
 import java.net.URISyntaxException;
 import java.awt.Desktop;
 import java.net.URI;
 
 import com.temboo.Library.Twitter.Timelines.HomeTimeline;
 import com.temboo.Library.Twitter.Timelines.HomeTimeline.HomeTimelineInputSet;
 import com.temboo.Library.Twitter.Timelines.HomeTimeline.HomeTimelineResultSet;
 import com.temboo.Library.Twitter.Tweets.StatusesUpdate;
 import com.temboo.Library.Twitter.Tweets.StatusesUpdate.StatusesUpdateInputSet;
 import com.temboo.Library.Twitter.Tweets.StatusesUpdate.StatusesUpdateResultSet;
 import com.temboo.core.TembooException;
 import com.temboo.core.TembooSession;
 
 
 
 public class Tweet {
 	
 	public Tweet()throws TembooException{
 	}
 	
 	String token; String tokenSecret;long name;
     String line; 
 	private final static String CONSUMER_KEY = "hyL303lpgZpSt6cMmilBw";
 	private final static String CONSUMER_KEY_SECRET = "EqgkdjEPuhP4KyVm3PEV926YuDrPcZAG249FxwXE9Q";
 	private final static String APP_KEY_NAME = "myFirstApp";
 	private final static String APP_KEY_VALUE = "231248aa-6da1-4f21-8";
 	File file;Writer output = null;
 	Twitter twitter = new TwitterFactory().getInstance();
 	ArrayList<String> list = new ArrayList<String>();
 
 	public void start() throws TwitterException, IOException,URISyntaxException {
 		file = new File("db.txt");
 		twitter.setOAuthConsumer(CONSUMER_KEY, CONSUMER_KEY_SECRET);
 		RequestToken requestToken = twitter.getOAuthRequestToken();
 		System.out.println("Going to "+ requestToken.getAuthorizationURL());
 		String web = requestToken.getAuthorizationURL();
 
 		this.openBrowser(web);
 		//Desktop.getDesktop().browse(new URI(web));
 
 		AccessToken accessToken = null;
 
 		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
 		while (null == accessToken) {
 			try {
 				System.out.print("Input PIN here: ");
 				String pin = br.readLine();
 
 				accessToken = twitter.getOAuthAccessToken(requestToken, pin);
 
 			} catch (TwitterException te) {
 
 				System.out.println("Failed to get access token, caused by: "
 						+ te.getMessage()); 
 
 				System.out.println("Pin Error");
 			}
 
 		}
 		System.out.println(accessToken.getToken());
 		System.out.println(accessToken.getTokenSecret());
 		System.out.println("Id is: "+ twitter.getId());
 		token = accessToken.getToken();
 		tokenSecret = accessToken.getTokenSecret();
 		name = twitter.getId();
 		this.storeAccessToken();
 	}
 
 
 	public void homeTime() throws TwitterException{
 		List<Status> statuses = twitter.getHomeTimeline();
 		System.out.println("Timeline Loading...");
 		for (Status status : statuses) {
 			System.out.println(status.getUser().getName() + ":" +
 					status.getText());
 		}
 	}
 
 	public void update() throws TwitterException{    
 		twitter.updateStatus("Hello Twitter");
 	}
 
 	public void post() throws TembooException{
 
 		TembooSession session = new TembooSession("phalax4", APP_KEY_NAME, APP_KEY_VALUE);
 		StatusesUpdate statusesUpdateChoreo = new StatusesUpdate(session);
 		StatusesUpdateInputSet statusesUpdateInputs = statusesUpdateChoreo.newInputSet();
 
 		statusesUpdateInputs.set_AccessToken(token);
 		statusesUpdateInputs.set_AccessTokenSecret(tokenSecret);
 		statusesUpdateInputs.set_ConsumerSecret(CONSUMER_KEY_SECRET);
 		statusesUpdateInputs.set_StatusUpdate("Temboo2");
 		statusesUpdateInputs.set_ConsumerKey(CONSUMER_KEY);
 
 		StatusesUpdateResultSet statusesUpdateResults = statusesUpdateChoreo.execute(statusesUpdateInputs);
 		System.out.println("Tweet Posted");
 	}
 	
 	public void homeTimeLn(ArrayList<String> list1) throws TembooException{
 		TembooSession session = new TembooSession("phalax4", APP_KEY_NAME, APP_KEY_VALUE);
 		HomeTimeline homeTimelineChoreo = new HomeTimeline(session);
 		HomeTimelineInputSet homeTimelineInputs = homeTimelineChoreo.newInputSet();
 
 		homeTimelineInputs.set_AccessToken(list1.get(1));
 		homeTimelineInputs.set_AccessTokenSecret(list1.get(2));
 		homeTimelineInputs.set_ConsumerSecret(CONSUMER_KEY_SECRET);
 		homeTimelineInputs.set_ConsumerKey(CONSUMER_KEY);
 
 		HomeTimelineResultSet homeTimelineResults = homeTimelineChoreo.execute(homeTimelineInputs);
 		System.out.println("Timeline");
		System.out.println(homeTimelineResults);
 	}
 
 	public void storeAccessToken() throws IOException{
 
 		output = new BufferedWriter(new FileWriter(file));
 		output.write(name+"\r\n");
 		
 		output.write(token+"\r\n");
 		
 		output.write(tokenSecret);
 
 		output.close();
 		System.out.println("Tokens Saved");
 	}
 	public void openBrowser(String myUrl){
 		try {
 			Desktop desktop = java.awt.Desktop.getDesktop();
 			URI oURL = new URI(myUrl);
 			desktop.browse(oURL);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public void getCreds(){
 		try{
 			FileInputStream fstream = new FileInputStream("db.txt");
 			DataInputStream in = new DataInputStream(fstream);
 			BufferedReader br = new BufferedReader(new InputStreamReader(in));
 			
 			while ((line = br.readLine()) != null)   {
 				list.add(line);
 			}			
 			in.close();
 		}catch (Exception e){
 			System.err.println("Error: " + e.getMessage());
 		}
 	}
 
 	public void runF() throws TembooException{
 		homeTimeLn(list);
 	}
 
 }
