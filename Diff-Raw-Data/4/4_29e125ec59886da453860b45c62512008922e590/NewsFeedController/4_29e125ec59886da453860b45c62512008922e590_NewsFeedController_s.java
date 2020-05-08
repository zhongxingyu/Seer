 
 package controllers;
 
 import static java.util.concurrent.TimeUnit.SECONDS;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.ScheduledFuture;
 import java.util.concurrent.TimeUnit;
 
 import net.htmlparser.jericho.Attribute;
 import net.htmlparser.jericho.Attributes;
 import net.htmlparser.jericho.Element;
 import net.htmlparser.jericho.HTMLElementName;
 import net.htmlparser.jericho.MasonTagTypes;
 import net.htmlparser.jericho.MicrosoftConditionalCommentTagTypes;
 import net.htmlparser.jericho.PHPTagTypes;
 import net.htmlparser.jericho.Segment;
 import net.htmlparser.jericho.Source;
 
 import org.codehaus.jackson.JsonNode;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.codehaus.jackson.node.ObjectNode;
 
 import play.Logger;
 import play.libs.F.Callback;
 import play.libs.F.Callback0;
 import play.libs.Json;
 import play.mvc.Controller;
 import play.mvc.WebSocket;
 import play.mvc.WebSocket.Out;
 /**
  * @author romanelm
  */
 public class NewsFeedController extends Controller {
 
 	public static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
 	final static Runnable beeper = new Runnable() {
 		public void run() { 
 			updatePools();
 			Logger.info("\n ---------------------- \n"+
 					"HOT: " + HOT_POOL.size() + "\n" + 
 					"TECH: " + TECH_POOL.size() + "\n" +
 					"SPORT: " + SPORT_POOL.size() + "\n" +
 					"CULTURE: " + CULTURE_POOL.size() + "\n ----------------------");
 		}
 	};
 
 
 	/**
 	 * Hashmap that given an ID of a Display, returns 
 	 * a Sockets object containing 2 websockets: one for the small view
 	 * and one for the big one.	
 	 */
 	public static HashMap<String, Sockets> sockets = new HashMap<String, Sockets>();
 
 
 	public static HashMap<String, Status> statuses = new HashMap<String, NewsFeedController.Status>();
 
 	/**
 	 * POOLS: hot, tech, sport, culture
 	 */
 	public static Integer HOT_ID = 0;
 	public static String[] HOT_SRC = {"http://ansa.feedsportal.com/c/34225/f/621689/index.rss", "http://rss.cnn.com/rss/edition.rss"};
 	public static ArrayList<ObjectNode> HOT_POOL = new ArrayList<ObjectNode>();
 
 	public static Integer TECH_ID = 0;
 	public static String[] TECH_SRC = {"http://www.engadget.com/rss.xml", "http://feeds.feedburner.com/ispazio", "http://feeds.wired.com/wired/index?format=xml"};
 	public static ArrayList<ObjectNode> TECH_POOL = new ArrayList<ObjectNode>();
 
 	public static Integer SPORT_ID = 0;
 	public static String[] SPORT_SRC = {"http://www.gazzetta.it/rss/Home.xml", "http://sports.espn.go.com/espn/rss/news"};
 	public static ArrayList<ObjectNode> SPORT_POOL = new ArrayList<ObjectNode>();
 
 	public static Integer CULTURE_ID = 0;
 	public static String[] CULTURE_SRC = {"http://feeds.feedburner.com/ilblogdeilibri?format=xml", "http://feeds2.feedburner.com/slashfilm"};
 	public static ArrayList<ObjectNode> CULTURE_POOL = new ArrayList<ObjectNode>();
 
 	public static boolean STARTED = false;
 
 	public static WebSocket<JsonNode> webSocket() {
 		return new WebSocket<JsonNode>() {
 
 			// Called when the Websocket Handshake is done.
 			public void onReady(WebSocket.In<JsonNode> in, final WebSocket.Out<JsonNode> out) {
 
 				in.onMessage(new Callback<JsonNode>() {
 					public void invoke(JsonNode event) {
 
 						Logger.info("INCOMING MESSAGE ON NEWSFEED WS:\n" 
 								+ event.toString());
 
 						String messageKind = event.get("kind").asText();						
 						String displayID = event.get("displayID").asText();
 
 						if(!sockets.containsKey(displayID)){
 							sockets.put(displayID, new Sockets(null, null));
 							Logger.info("DisplayID " + displayID + " was added to the system.");
 						}
 
 						if(messageKind.equals("appReady")){
 
 							if(!STARTED){
 								STARTED = true;
 								final ScheduledFuture<?> beeperHandle = 
 										scheduler.scheduleAtFixedRate(beeper, 10, 60, SECONDS);
 								scheduler.schedule(new Runnable() {
 									public void run() { beeperHandle.cancel(true); }
 								}, 1, TimeUnit.DAYS);
 							}
 
 							// Can be either small or big
 							String size = event.get("size").asText();
 
 							if(size.equals("small")){
 								// Set the socket
 								sockets.get(displayID).small = out;
 								// Initialize the status of the screen
 								statuses.put(displayID, new Status());
 							} else if(size.equals("big")) {
 								sockets.get(displayID).big  = out;
 							}
 
 							Logger.info(
 									"\n ******* MESSAGE RECIEVED *******" +
 											"\n The "+ size + " view of \n" +
 											"newsfeed app is now available on displayID: " + displayID +
 											"\n*********************************"
 									);
 
 						} else if(messageKind.equals("mobileRequest")){
 
 
 							//								String username = event.get("username").asText();
 							JsonNode pref = Json.toJson(event.get("preference"));
 							
 							Sockets displaySockets = sockets.get(displayID);
 							Status displayStatus = statuses.get(displayID);
 
 							updateStatus(displayStatus,pref);
 							ObjectNode response = createResponse(displayStatus, pref);
 							displaySockets.small.write(response);
 							displaySockets.big.write(response);
 
 							Logger.info("JSON SENT TO THE DISPLAY!");
 
 						} else if(messageKind.equals("more")){
 
 							Status displayStatus = statuses.get(displayID);
 							Sockets displaySockets = sockets.get(displayID);
 							
 							ObjectNode temp = Json.newObject();
 							temp.put("hot", displayStatus.hot ? true : false);
 							temp.put("tech", displayStatus.tech ? true : false);
 							temp.put("sport", displayStatus.sport ? true : false);
 							temp.put("culture", displayStatus.culture ? true : false);
 							Logger.info(temp.toString());
 							ObjectNode response = createResponse(displayStatus, temp);
							response.put("top",event.get("top").asText());
 							
 							displaySockets.small.write(response);
 							displaySockets.big.write(response);
 							
 							Logger.info("JSON SENT TO THE DISPLAY!");
 
 						} else {
 							Logger.info("WTF: " + event.toString());
 						}
 
 					}
 
 
 
 				});
 
 				// When the socket is closed.
 				in.onClose(new Callback0() {
 					public void invoke() {
 						Logger.info("\n ******* MESSAGE RECIEVED *******" +
 								"\n A weather tile on " + "TODO" +
 								"\n is now disconnected." +
 								"\n*********************************"
 								);
 					}
 
 
 				});
 
 			}
 
 		};
 	}
 
 	public static void updateStatus(Status status, JsonNode pref) {
 		if(pref.get("hot").asBoolean() && !status.hot){
 			status.hot = true;
 		}
 
 		if(pref.get("tech").asBoolean() && !status.tech){
 			status.tech = true;
 		}
 
 		if(pref.get("sport").asBoolean() && !status.sport){
 			status.sport = true;
 		}
 
 		if(pref.get("culture").asBoolean() && !status.culture){
 			status.culture = true;
 		}
 	}
 
 	public static int findLastIndex(ArrayList<ObjectNode> pool, int start, int qty){
 		try {
 			int end = start+qty;
 			pool.subList(start, end);
 			return end;
 		} catch (IndexOutOfBoundsException e) {
 			return pool.size();
 		}
 	}
 
 	public static ObjectNode createResponse(Status status, JsonNode pref){
 		
 		ObjectNode response = Json.newObject();
 
 		if(pref.get("hot").asBoolean() && (HOT_POOL.size() > status.last_hot) ){
 			int lastIndex = findLastIndex(HOT_POOL, status.last_hot, 10);
 			response.put("hot",Json.toJson(HOT_POOL.subList(status.last_hot, lastIndex)));
 			status.last_hot = lastIndex;
 		}
 
 		if(pref.get("tech").asBoolean()  && (TECH_POOL.size() > status.last_tech) ){
 			int lastIndex = findLastIndex(TECH_POOL, status.last_tech, 10);
 			response.put("tech",Json.toJson(TECH_POOL.subList(status.last_tech, lastIndex)));
 			status.last_tech = lastIndex;
 		}
 
 		if(pref.get("sport").asBoolean()  && (SPORT_POOL.size() > status.last_sport) ){
 			int lastIndex = findLastIndex(SPORT_POOL, status.last_sport, 10);
 			response.put("sport",Json.toJson(SPORT_POOL.subList(status.last_sport, lastIndex)));
 			status.last_sport = lastIndex;
 		}
 		if(pref.get("culture").asBoolean()  && (CULTURE_POOL.size() > status.last_culture) ){
 			int lastIndex = findLastIndex(CULTURE_POOL, status.last_culture, 10);
 			response.put("culture",Json.toJson(CULTURE_POOL.subList(status.last_culture, lastIndex)));
 			status.last_culture = lastIndex;
 		}
 
 		return response;
 	}
 
 	public static JsonNode xmlToJSON(String feedURL) {
 		String baseURL = "https://ajax.googleapis.com/ajax/services/feed/load?v=1.0&q=" + feedURL + "&num=50";
 		try {
 			URL url = new URL(baseURL);
 			URLConnection connection = url.openConnection();
 
 			String line;
 			StringBuilder builder = new StringBuilder();
 			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
 			while((line = reader.readLine()) != null) {
 				builder.append(line);
 			}
 
 			ObjectMapper mapper = new ObjectMapper();
 			JsonNode df = mapper.readValue(builder.toString(), JsonNode.class);
 			return df;
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 
 
 	/**
 	 * Given a JSON containing a set of feeds sources,
 	 * it returns a new JSON containing the titles of the various
 	 * news.
 	 * @param json of feeds recieved from the mobile 
 	 * @return
 	 */
 	public static void updatePools() {		
 		try {
 			extractInformations(HOT_SRC, HOT_POOL);
 			extractInformations(TECH_SRC, TECH_POOL);
 			extractInformations(SPORT_SRC, SPORT_POOL);
 			extractInformations(CULTURE_SRC, CULTURE_POOL);
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Check if the current news in already present
 	 * in the pool
 	 * @param pool
 	 * @param newsContent
 	 * @return
 	 */
 	public static boolean isNew(ArrayList<ObjectNode> pool, String newsContent){
 		for(ObjectNode currentNews: pool){
 			String currentNewsContent = currentNews.get("content").asText();
 			if(newsContent.equals(currentNewsContent)) return false;
 		}
 		return true;
 	}
 
 	public static void extractInformations(String[] feeds, ArrayList<ObjectNode> pool) throws MalformedURLException {		
 
 		for (String feed : feeds){
 			JsonNode jsonFeed = xmlToJSON(feed).get("responseData").get("feed");
 			String newsSource = jsonFeed.get("title").asText();
 			Logger.info("PROCESSING: " + newsSource);
 			Iterator<JsonNode> entries = jsonFeed.get("entries").getElements();
 			while(entries.hasNext()){
 				JsonNode currentEntry = entries.next();
 				ObjectNode currentNews = Json.newObject();
 				currentNews.put("source", newsSource);
 
 				String content = currentEntry.get("content").asText();
 				if(content == null){
 					continue;
 				}
 
 				if(!isNew(pool, content)){
 					continue;
 				}
 //				Logger.info("new item of " + newsSource + " is being processed...");
 
 				String link = currentEntry.get("link").asText();
 				MicrosoftConditionalCommentTagTypes.register();
 				PHPTagTypes.register();
 				PHPTagTypes.PHP_SHORT.deregister();
 				MasonTagTypes.register();
 				Source source;
 				ArrayList<String> imgs = new ArrayList<String>();
 				try {
 					source = new Source(new URL(link));
 					List<Element> elementList = source.getAllElements(HTMLElementName.IMG);
 					for (Segment segment : elementList) {
 						Attributes tagAttr = segment.getFirstStartTag().getAttributes(); 
 						if(tagAttr == null) continue;
 
 						final Attribute alt = tagAttr.get("alt");
 						if(alt == null) continue;
 
 						Integer width = 0;
 						if(tagAttr.getValue("width") != null){
 							width = new Integer(tagAttr.getValue("width"));
 						} 
 
 						Integer height = 0;
 						if(tagAttr.getValue("height") != null){
 							height = new Integer(tagAttr.getValue("height"));
 						}
 
 
 						if (alt!=null && 
 								(		
 										(width > 100 && height > 100) || 
 										(width > 400 && height == 0)  ||
 										(height > 400 && width == 0)
 										)
 								)
 						{
 							imgs.add(segment.toString());
 						} else {
 							//Logger.info("NOT APPROPRIATE \n" + "------------------------------------------------- \n");
 						}
 					}
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 
 				currentNews.put("link", link);
 				String title = currentEntry.get("title").asText();
 				if(title == null) continue;
 				currentNews.put("title", title);
 				currentNews.put("content", content);				
 				currentNews.put("imgs", Json.toJson(imgs));
 
 				pool.add(currentNews);
 
 			}
 		}
 	}
 
 	public static class Sockets {
 		public WebSocket.Out<JsonNode> small;
 		public WebSocket.Out<JsonNode> big;
 
 		public Sockets(Out<JsonNode> small, Out<JsonNode> big) {
 			this.small = small;
 			this.big = big;
 		}
 	}
 
 
 	// IF THE POOL IS MODIFIED
 	public static class Status {
 
 		public boolean hot, tech, sport, culture;
 
 		public int last_hot, last_tech,last_sport,last_culture;
 
 		public Status() {
 			this.hot = this.tech = this.sport = this.culture = false;
 			this.last_hot = this.last_tech = this.last_sport = this.last_culture = 0;
 		}
 	} 
 }
