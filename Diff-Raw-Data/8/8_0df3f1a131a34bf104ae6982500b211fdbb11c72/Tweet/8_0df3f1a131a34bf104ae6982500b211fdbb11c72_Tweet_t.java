 package Access;
 //Original code at http://www.java-tutorial.ch/framework/twitter-with-java-tutorial
 import java.io.BufferedReader;
 import java.util.ArrayList;
 import java.util.Scanner;
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
 import twitter4j.auth.RequestToken;
 
 import java.net.URISyntaxException;
 import java.awt.Desktop;
 import java.net.URI;
 
 import com.google.gson.Gson;
 import com.google.gson.JsonArray;
 import com.google.gson.JsonElement;
 import com.google.gson.JsonObject;
 import com.google.gson.JsonParser;
 import com.temboo.Library.Twitter.Timelines.HomeTimeline;
 import com.temboo.Library.Twitter.Timelines.HomeTimeline.HomeTimelineInputSet;
 import com.temboo.Library.Twitter.Timelines.HomeTimeline.HomeTimelineResultSet;
 import com.temboo.Library.Twitter.Tweets.StatusesUpdate;
 import com.temboo.Library.Twitter.Tweets.StatusesUpdate.StatusesUpdateInputSet;
 import com.temboo.Library.Twitter.Tweets.StatusesUpdate.StatusesUpdateResultSet;
 import com.temboo.core.TembooException;
 import com.temboo.core.TembooSession;
 
 import java.util.Properties;
 
 import org.json.JSONArray;
 import org.json.JSONML;
 import org.json.JSONObject;
 
 
 
 public class Tweet {
 	
 	public Tweet()throws TembooException{
 	}
 	Properties prop;
 
 	String token; String tokenSecret;long name;
     String line; Gson gson = new Gson();String au;
     int var1,var2,var3,var4;
     private final static String CONSUMER_KEY = "hyL303lpgZpSt6cMmilBw";
 	private final static String CONSUMER_KEY_SECRET = "EqgkdjEPuhP4KyVm3PEV926YuDrPcZAG249FxwXE9Q";
 	private final static String APP_KEY_NAME = "myFirstApp";
 	private final static String APP_KEY_VALUE = "231248aa-6da1-4f21-8";
 	File file;Writer output = null;
 	Twitter twitter = new TwitterFactory().getInstance();
 	ArrayList<String> list = new ArrayList<String>();
 	ArrayList<String> stringList = new ArrayList<String>();
 	String getter;
 	
 	public void check() throws TwitterException, IOException, URISyntaxException, TembooException{
 		if(getCreds()==null){
 			start();
 		}else{
 			homeTimeLn(list);
 		}
 		
 	}
 	public void start() throws TwitterException, IOException,URISyntaxException {
 
 		 prop = new Properties();
 		 
 
 		file = new File("db.txt");
 		twitter.setOAuthConsumer(CONSUMER_KEY, CONSUMER_KEY_SECRET);
 		RequestToken requestToken = twitter.getOAuthRequestToken();
 		System.out.println("Going to "+ requestToken.getAuthorizationURL());
 		String web = requestToken.getAuthorizationURL();
 
 		this.openBrowser(web);
 		//Desktop.getDesktop().browse(new URI(web));
 
 		AccessToken accessToken = null;
 		Scanner reader = new Scanner(System.in);
 
 
 		//BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
 
 		while (null == accessToken) {
 			try {
 
 				System.out.println("Input pin: ");
 				String pin = reader.nextLine();
 				
 				//System.out.print("Input PIN here: ");
 				//String pin = br.readLine();
 
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
 	
	public String homeTimeLn(ArrayList<String> list1) throws TembooException, IOException, TwitterException{
 		TembooSession session = new TembooSession("phalax4", APP_KEY_NAME, APP_KEY_VALUE);
 		
 		HomeTimeline homeTimelineChoreo = new HomeTimeline(session);
 		HomeTimelineInputSet homeTimelineInputs = homeTimelineChoreo.newInputSet();
 
 		homeTimelineInputs.set_AccessToken(list1.get(2));
 		homeTimelineInputs.set_AccessTokenSecret(list1.get(3));
 		homeTimelineInputs.set_ConsumerSecret(CONSUMER_KEY_SECRET);
 		homeTimelineInputs.set_ConsumerKey(CONSUMER_KEY);
 
 		HomeTimelineResultSet homeTimelineResults = homeTimelineChoreo.execute(homeTimelineInputs);
 		
 		System.out.println(homeTimelineResults.get_Response());
 
 		
 		 /*JsonElement jelement = new JsonParser().parse(homeTimelineResults.get_Response());
 		    JsonObject  rootobj = jelement.getAsJsonObject();*/
 		   
 		 
 		JsonParser jp = new JsonParser();
     	JsonElement root = jp.parse(homeTimelineResults.get_Response());
     	
     	JsonArray statuses = root.getAsJsonArray();
     	
 		
     	//JsonArray statuses = rootobj.get("statuses").getAsJsonArray();
     	
     	for(int i = 0; i < statuses.size(); i++) {
     		JsonObject status = statuses.get(i).getAsJsonObject();
     		
     		String text = status.get("text").getAsString();
     		String screen_name = status.get("user").getAsJsonObject().get("name").getAsString();
     		
     		JsonArray urls = status.get("entities").getAsJsonObject().get("urls").getAsJsonArray();
     		
     		au = urls.toString();
     		var1 = au.indexOf(":")+1;
     		var2 = au.indexOf(":",var1)+1;
     		var3 = au.indexOf(":",var2)+2;
     		var4 = au.indexOf(",",var3)-1;
     	
     		
    		return (screen_name + " said " + text+" with "+au.substring(var3,var4));
     	}
 		//new Reader().parse(result);
 
    	return null;
 	//new Listen().listen(CONSUMER_KEY, CONSUMER_KEY_SECRET,token,tokenSecret);
 	}
 
 	public void storeAccessToken() throws IOException{
 		//store as json file//expiring token?
 		/*try {
     	
     		prop.setProperty("debug", "true");
     		prop.setProperty("oauth.consumerKey", CONSUMER_KEY);
     		prop.setProperty("oauth.consumerSecret", CONSUMER_KEY_SECRET);
     		prop.setProperty("oauth.accessToken", token);
     		prop.setProperty("oauth.accessTokenSecret", "tokenSecret");
     	
     		prop.store(new FileOutputStream("twitter4j.properties"), null);
  
     	} catch (IOException ex) {
     		ex.printStackTrace();
         }*/
 		output = new BufferedWriter(new FileWriter(file));
 		output.write("true"+"\r\n");
 		output.write(name+"\r\n");
 		output.write(token+"\r\n");
 		output.write(tokenSecret+"\r\n");
 
 
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
 	
 	public String getCreds(){
 
 	/*	try {
            
 			InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("twitter4j.properties");
  		prop.load(new FileInputStream("twitter4j.properties"));
 
            
         list.add(prop.getProperty("oauth.accessToken"));
  		list.add(prop.getProperty("oauth.accessTokenSecret"));
  		//System.out.println(prop.getProperty("dbpassword"));
 
  	} catch (IOException ex) {
  		ex.printStackTrace();
      }*/
 
 		
 		
 
 		try{
 			FileInputStream fstream = new FileInputStream("db.txt");
 			DataInputStream in = new DataInputStream(fstream);
 			BufferedReader br = new BufferedReader(new InputStreamReader(in));
 			
 			while ((line = br.readLine()) != null)   {
 				list.add(line);
 			}			
 			in.close();
 			return "SUCCESS";
 		}catch (Exception e){
 			System.err.println("Error: " + e.getMessage());
 			return null;
 		}
 	}
 	//User user = twitter.showUser(screenName);
 
 }
