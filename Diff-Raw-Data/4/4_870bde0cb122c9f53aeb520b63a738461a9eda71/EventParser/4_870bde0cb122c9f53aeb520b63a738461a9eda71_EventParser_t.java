 package parser;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Arrays;
 import java.util.Date;
 
 import com.google.appengine.api.datastore.DatastoreService;
 import com.google.appengine.api.datastore.DatastoreServiceFactory;
 import com.google.appengine.api.datastore.Entity;
 import com.google.appengine.api.datastore.Text;
 import com.google.gson.Gson;
 import com.google.gson.JsonArray;
 import com.google.gson.JsonParser;
 
 public class EventParser {
 	
 	private static final String BASE_URL = "https://graph.facebook.com/";
 	private static final String ACCESS_TOKEN = "356870157756071%7CZKRXp2JMaj8T6jg1nGTvRiDa1Xs";
 	private static final String FIELDS = "id,owner,name,description,start_time,end_time,location,venue,privacy,picture.type(large)";
 	private static final String QUERY = "fql?q=SELECT+all_members_count,+attending_count+FROM+event+WHERE+eid=";
 	
 	public static void parse(String eventURL, String[] tags) throws MalformedURLException, IOException {
 		// parse event ID
 		String eventID = parseEventID(eventURL);
 		if (eventID == null) {
 			return;
 		}
 		
 		// make Facebook Graph API call
 		String graphURL = buildGraphURL(eventID);
 		Event tempEvent = sendGraphRequest(graphURL);;
 	    if (!tempEvent.getPrivacy().equalsIgnoreCase("OPEN")) {
 	    	return;
 	    }
 	    
 	    // make Facebook Query Language call
 	    String queryURL = buildQueryURL(eventID);
 	    EventSummary tempSummary = sendQueryRequest(queryURL);
 	    
 	    // parse JSON graph response into event entity using GSON
 	    Entity event = createEntity(eventURL, tempEvent, tempSummary, tags);
 
 		// store event entity in datastore
         DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
         datastore.put(event);
 	}
 	
 	private static String parseEventID(String eventURL) {
 		String[] parts = eventURL.split("/");
 		for (int i = 0; i < parts.length; i++) {
 			if (parts[i].equalsIgnoreCase("events")) {
 				return parts[i+1];
 			}
 		}
 		return null;
 	}
 	
 	private static String buildGraphURL(String eventID) {
 		StringBuilder url = new StringBuilder(BASE_URL);
 		url.append(eventID);
 		url.append("?access_token=");
 		url.append(ACCESS_TOKEN);
 		url.append("&fields=");
 		url.append(FIELDS);
 		return url.toString();
 	}
 	
 	private static String buildQueryURL(String eventID) {
 		StringBuilder url = new StringBuilder(BASE_URL);
 		url.append(QUERY);
 		url.append(eventID);
 		url.append("&access_token=");
 		url.append(ACCESS_TOKEN);
 		return url.toString();
 	}
 	
 	private static Event sendGraphRequest(String graphURL) throws MalformedURLException, IOException {
 		InputStream response = new URL(graphURL).openStream();
 	    BufferedReader br = new BufferedReader(new InputStreamReader(response, "UTF-8"));
 	    Gson gson = new Gson();
 	    Event tempEvent = gson.fromJson(br, Event.class);
 	    br.close();
 	    return tempEvent;
 	}
 	
 	private static EventSummary sendQueryRequest(String queryURL) throws MalformedURLException, IOException {
 		InputStream response = new URL(queryURL).openStream();
 	    BufferedReader br = new BufferedReader(new InputStreamReader(response, "UTF-8"));
 	    JsonArray array = new JsonParser().parse(br).getAsJsonObject().get("data").getAsJsonArray();
 	    Gson gson = new Gson();
 	    EventSummary tempEvent = gson.fromJson(array.get(0), EventSummary.class);
 	    br.close();
 	    return tempEvent;
 	}
 	
 	private static Entity createEntity(String eventURL, Event tempEvent, EventSummary tempSummary, String[] tags) {
         Entity event = new Entity("event", tempEvent.getID());
         
         // store URL and timestamp of datastore transaction
         event.setProperty("url", eventURL);
         event.setProperty("dateAdded", new Date().toString());
         
         // store event properties
         event.setProperty("id", tempEvent.getID());
         event.setProperty("owner",  tempEvent.getOwner());
         event.setProperty("name",  tempEvent.getName());
         event.setProperty("description",  new Text(tempEvent.getDescription()));
         event.setProperty("start_time",  tempEvent.getStart_time());
         event.setProperty("end_time",  tempEvent.getEnd_time());
         event.setProperty("location",  tempEvent.getLocation());
         event.setProperty("venue",  tempEvent.getVenue());
         event.setProperty("privacy",  tempEvent.getPrivacy());
         event.setProperty("picture",  tempEvent.getPicture());
         
         // store event summary properties
         event.setProperty("all_members_count", tempSummary.getAllMembersCount());
         event.setProperty("attending_count", tempSummary.getAttendingCount());
         
         // store tags
        if (tags != null && tags.length > 0) {
        	event.setProperty("tags", Arrays.asList(tags));
        }
 
         return event;
 	}
 }
