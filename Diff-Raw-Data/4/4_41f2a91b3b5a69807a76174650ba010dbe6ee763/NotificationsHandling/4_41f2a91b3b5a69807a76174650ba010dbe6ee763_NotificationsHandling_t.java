 package il.ac.huji.chores.server;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpPut;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 import il.ac.huji.chores.Chore;
 import il.ac.huji.chores.Constants;
 import il.ac.huji.chores.Roommate;
 import il.ac.huji.chores.server.parse.ParseRestClientImpl;
 
 public class NotificationsHandling {
 	
 	private static String PUSH_URL = "https://api.parse.com/1/push";
 
 	public static void notifyNewChores(String apartmentId) throws ClientProtocolException, IOException {
 		ParseRestClientImpl parse = new ParseRestClientImpl();
 		List<Roommate> roommates = parse.getApartmentRoommates(apartmentId);
 		String title = "New chores division";
 		String message = "New chores has been divided";
 		JSONObject data = buildDataJson(title,message,Constants.PARSE_NEW_CHORES_CHANNEL_KEY);
 		JSONObject usersStatement = buildWhereRoommateStatement(roommates);
 		List<String> channelsList= new ArrayList<String>();
 		channelsList.add(Constants.PARSE_STEAL_CHANNEL_KEY);
 		JSONArray channels = new JSONArray(channelsList);
 		sendNotification(usersStatement,data,channels);
 		
 	}
 	
 	//notify parse chore chore was missed (parse then need to notify the roommates)
 	//This method is called after the chore status was updated to missed,
 	public static void notifyMissedChore(Chore chore) throws ClientProtocolException, IOException{
 		//get users list
 		ParseRestClientImpl parse = new ParseRestClientImpl();
 		List<Roommate> roommates = parse.getApartmentRoommates(chore.getApartment());
 		String title = "Chore missed";
 		String message = chore.getAssignedTo() + " has missed the chore '"+chore.getName()+"'";
 		JSONObject data = buildDataJson(title,message,Constants.PARSE_MISSED_CHANNEL_KEY);
 		JSONObject usersStatement = buildWhereRoommateStatement(roommates);
 		List<String> channelsList= new ArrayList<String>();
 		channelsList.add(Constants.PARSE_STEAL_CHANNEL_KEY);
 		JSONArray channels = new JSONArray(channelsList);
 		sendNotification(usersStatement,data,channels);
 	}
 	
 	private static void sendNotification(JSONObject users,JSONObject data,JSONArray channels) throws ClientProtocolException, IOException{
 		HttpClient client = new DefaultHttpClient();
 		HttpPost put = new HttpPost(PUSH_URL);
 		put.setHeader("X-Parse-Application-Id", "oNViNVhyxp6dS0VXvucqgtaGmBMFIGWww0sHuPGG");
 		put.setHeader("X-Parse-REST-API-Key", "Tu5aHmbnn2Bz7AXVfSb2CPOng7LaoGkJHH0YbVXr");
 		put.setHeader("Content-Type", "application/json");
 		JSONObject json = new JSONObject();
 		
 		json.put("channels",channels);
 		json.put("data",data);
 		//json.put("where",users);
 				json.put("username", users);
 		//json.put("owner", "aTvZBFcxmh");
 		System.out.println("FINAL JSON:"+json);
 		StringEntity input = new StringEntity(json.toString());
 	        put.setEntity(input);
 	        HttpResponse response = client.execute(put);
 	        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
 	        String line = "";
 	        StringBuilder result = new StringBuilder();
 	        while ((line = rd.readLine()) != null) {
 	            System.out.println(line);
 	            result.append(line);
 	        }
 	        
 	}
 	
 	public static JSONObject buildWhereRoommateStatement(List<Roommate> roommates){
 		JSONArray jsonRoommates = convertRoommatesToJSonArray(roommates);
 		JSONObject in = new JSONObject();
 		in.put("$in", jsonRoommates);
 		//JSONObject owner = new JSONObject();
 		//owner.put("username", in);
 		//return owner;
 		return in;
 	}
 	public static JSONArray convertRoommatesToJSonArray(List<Roommate> roommates){
 		List<String> usersList = new ArrayList<String>();
 		for(Roommate roommate : roommates){
			usersList.add(roommate.getUsername());
 		}
 		JSONArray jsonArr = new JSONArray(usersList);
 		System.out.println(jsonArr.toString());
 		return jsonArr;
 		
 	}
 	public static JSONObject buildDataJson(String title, String message, String notificationType){
 		JSONObject json = new JSONObject();
 		json.put("alert",message);
 		json.put("title",title);
		json.put("action","il.ac.huji.chores.choresNotification");
 		json.put("notificationType", notificationType);
 		return json;
 		
 	}
 
 }
