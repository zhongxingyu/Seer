 package il.ac.huji.chores.server.parse;
 
 import il.ac.huji.chores.Chore;
 import il.ac.huji.chores.ChoreInfo;
 import il.ac.huji.chores.Coins;
 import il.ac.huji.chores.Roommate;
 import il.ac.huji.chores.RoommatesApartment;
 import il.ac.huji.chores.server.ChoresServerMain;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpPut;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.*;
 
 /**
  * Date: 06/09/13
  */
 public class ParseRestClientImpl implements ParseRestClient {
 	private static String BASE_URL = "https://api.parse.com/1/classes/";
 	private static String BASE_USER_URL = "https://api.parse.com/1/users/";
 
 	@Override
 	public List<Roommate> getApartmentRoommates(String apartmentId)
 			throws ClientProtocolException, IOException {
 		Map<String, Object> whereCond = new HashMap<String, Object>();
 		whereCond.put("apartmentID", apartmentId);
 		String result = QueryWhere("_User", whereCond);
 		System.out.println("result = " + result);
 		JSONObject resultJson = new JSONObject(result);
 		JSONArray jsonArr = resultJson.getJSONArray("results");
 		List<Roommate> roommates = JsonConverter
 				.convertArrToRoommatesList(jsonArr);
 		return roommates;
 	}
 
 	@Override
 	public List<RoommatesApartment> getApartmentList()
 			throws ClientProtocolException, IOException {
 		String result = Query("Apartment");
 		System.out.println("result = " + result);
 		JSONObject resultJson = new JSONObject(result);
 		JSONArray jsonArr = resultJson.getJSONArray("results");
 		List<RoommatesApartment> apartmentList = JsonConverter
 				.convertJsonArrayToApartmentList(jsonArr);
 		return apartmentList;
 
 	}
 
 	@Override
 	public List<String> getApartmentIds() throws ClientProtocolException,
 			IOException {
 		String result = Query("Apartment");
 		System.out.println("result = " + result);
 		JSONObject resultJson = new JSONObject(result);
 		JSONArray jsonArr = resultJson.getJSONArray("results");
 		List<String> ids = new ArrayList<String>();
 		for (int i = 0; i < jsonArr.length(); i++) {
 			ids.add(jsonArr.getJSONObject(i).getString("objectId"));
 		}
 		return ids;
 	}
 
 	@Override
 	public List<ChoreInfo> getApartmentChoreInfos(String apartmentId)
 			throws ClientProtocolException, IOException {
 		Map<String, Object> whereConditionsMap = new HashMap<String, Object>();
 		whereConditionsMap.put("apartment", apartmentId);
 		String result = QueryWhere("ChoresInfo", whereConditionsMap);
 		System.out.println("result = " + result);
 		JSONObject resultJson = new JSONObject(result);
 		JSONArray jsonArr = resultJson.getJSONArray("results");
 		List<ChoreInfo> choreInfoList = JsonConverter
 				.convertJsonArrayToChoreInfoList(jsonArr);
 		return choreInfoList;
 	}
 
 	@Override
 	public void sendChores(String apartmentId, List<Chore> assignedChores) {
 		// To change body of implemented methods use File | Settings | File
 		// Templates.
 	}
 
 	public String addChore(String choreJson) throws ClientProtocolException,
 			IOException {
 		return createObject("Chores", choreJson);
 	}
 
 	public String getChoreInfo(String choreInfoId) throws IOException {
 		return getObject("ChoresInfo", choreInfoId);
 	}
 
 	public String getChore(String choreId) throws IOException {
 		return getObject("Chores", choreId);
 	}
 
 	public Chore getChoreObj(String choreId) throws IOException {
 
 		String jsonStr = getObject("Chores", choreId);
 		return JsonConverter.convertJsonToChore(new JSONObject(jsonStr));
 
 	}
 
 	public StringBuilder updateObject(String className, String id,
 			String jsonObject) throws ClientProtocolException, IOException {
 		HttpClient client = new DefaultHttpClient();
 		HttpPut put = new HttpPut(BASE_URL + className + "/" + id);
 		put.setHeader("X-Parse-Application-Id",
 				"oNViNVhyxp6dS0VXvucqgtaGmBMFIGWww0sHuPGG");
 		put.setHeader("X-Parse-REST-API-Key",
 				"Tu5aHmbnn2Bz7AXVfSb2CPOng7LaoGkJHH0YbVXr");
 		put.setHeader("Content-Type", "application/json");
 		StringEntity input = new StringEntity(jsonObject);
 		put.setEntity(input);
 		HttpResponse response = client.execute(put);
 		BufferedReader rd = new BufferedReader(new InputStreamReader(response
 				.getEntity().getContent()));
 		String line = "";
 		StringBuilder result = new StringBuilder();
 		while ((line = rd.readLine()) != null) {
 			System.out.println(line);
 			result.append(line);
 		}
 		return result;
 	}
 
 	/*
 	 * public StringBuilder updateUser(String id, String jsonObject) throws
 	 * ClientProtocolException, IOException { HttpClient client = new
 	 * DefaultHttpClient();
 	 * 
 	 * HttpPut put = new HttpPut(BASE_USER_URL+id);
 	 * put.setHeader("X-Parse-Application-Id",
 	 * "oNViNVhyxp6dS0VXvucqgtaGmBMFIGWww0sHuPGG");
 	 * put.setHeader("X-Parse-REST-API-Key",
 	 * "Tu5aHmbnn2Bz7AXVfSb2CPOng7LaoGkJHH0YbVXr");
 	 * put.setHeader("X-Parse-Session-Token", "pnktnjyb996sj4p156gjtp4im");
 	 * put.setHeader("Content-Type", "application/json"); StringEntity input =
 	 * new StringEntity(jsonObject); put.setEntity(input); HttpResponse response
 	 * = client.execute(put); BufferedReader rd = new BufferedReader(new
 	 * InputStreamReader(response.getEntity().getContent())); String line = "";
 	 * StringBuilder result = new StringBuilder(); while ((line = rd.readLine())
 	 * != null) { System.out.println(line); result.append(line); } return
 	 * result; }
 	 */
 
 	public String createObject(String className, String jsonObject)
 			throws IllegalStateException, IOException {
 
 		return postRequest("https://api.parse.com/1/classes/" + className,
 				jsonObject);
 
 	}
 
 	public String getObject(String className, String id)
 			throws ClientProtocolException, IOException {
 		return getRequest("https://api.parse.com/1/classes/" + className + "/"
 				+ id);
 	}
 
 	public String getRequest(String url) throws ClientProtocolException,
 			IOException {
 		HttpClient client = new DefaultHttpClient();
 		HttpGet request = new HttpGet(url);
 		request.setHeader("X-Parse-Application-Id",
 				"oNViNVhyxp6dS0VXvucqgtaGmBMFIGWww0sHuPGG");
 		request.setHeader("X-Parse-REST-API-Key",
 				"Tu5aHmbnn2Bz7AXVfSb2CPOng7LaoGkJHH0YbVXr");
 
 		HttpResponse response = client.execute(request);
 		BufferedReader rd = new BufferedReader(new InputStreamReader(response
 				.getEntity().getContent()));
 		String line = "";
 		StringBuilder buffer = new StringBuilder();
 		while ((line = rd.readLine()) != null) {
 			System.out.println(line);
 			buffer.append(line);
 		}
 		return buffer.toString();
 	}
 
 	public String getApartment(String id) throws ClientProtocolException,
 			IOException {
 		return getObject("Apartment", id);
 	}
 
 	public String Query(String className) throws ClientProtocolException,
 			IOException {
 		return getRequest("https://api.parse.com/1/classes/" + className);
 	}
 
 	public String postRequest(String url, String body)
 			throws ClientProtocolException, IOException {
 		HttpClient client = new DefaultHttpClient();
 		HttpPost post = new HttpPost(url);
 		post.setHeader("X-Parse-Application-Id",
 				"oNViNVhyxp6dS0VXvucqgtaGmBMFIGWww0sHuPGG");
 		post.setHeader("X-Parse-REST-API-Key",
 				"Tu5aHmbnn2Bz7AXVfSb2CPOng7LaoGkJHH0YbVXr");
 		post.setHeader("Content-Type", "application/json");
 
 		StringEntity input = new StringEntity(body);
 		post.setEntity(input);
 		HttpResponse response = client.execute(post);
 		BufferedReader rd = new BufferedReader(new InputStreamReader(response
 				.getEntity().getContent()));
 		String line = "";
 		StringBuilder result = new StringBuilder();
 		while ((line = rd.readLine()) != null) {
 			System.out.println(line);
 			result.append(line);
 		}
 		return result.toString();
 	}
 
 	public String QueryWhere(String className, Map<String, Object> keyValue)
 			throws ClientProtocolException, IOException {
 		String where = buildWhereStatement(keyValue);
 		return getRequest(BASE_URL + className + "?" + where);
 	}
 
 	public String buildWhereStatement(Map<String, Object> keyValue) {
 
 		return JsonConverter.whereConditionToJson(keyValue);
 
 	}
 
 	@Override
 	public List<RoommatesApartment> getTodaysApartmentList(String day)
 			throws ClientProtocolException, IOException {
 		Map<String, Object> whereConditionsMap = new HashMap<String, Object>();
 		whereConditionsMap.put("divisionDay", day);
 		String result = QueryWhere("Apartment", whereConditionsMap);
 		System.out.println("result = " + result);
 		JSONObject resultJson = new JSONObject(result);
 		JSONArray jsonArr = resultJson.getJSONArray("results");
 		List<RoommatesApartment> apartments = JsonConverter
 				.convertJsonArrayToApartmentList(jsonArr);
 		return apartments;
 	}
 
 	@Override
 	public void addChores(List<Chore> chores) throws ClientProtocolException,
 			IOException {
 
 		Chore chore;
 		String idStr;
 		JSONObject json;
 		for (int i = 0; i < chores.size(); i++) {
 
 			chore = chores.get(i);
 			String jsonChore = JsonConverter.convertChoreToJson(chore)
 					.toString();
 			String jsonResult = addChore(jsonChore);
 			JSONObject result = new JSONObject(jsonResult);
			idStr = getChoreIdFromJson(result);

			json = new JSONObject(idStr);
			chore.setId(json.getString("objectId"));
 			ChoresServerMain.triggerDeadlinePassed(chore);
 		}
 	}
 
 	public void addChoreObj(Chore chore) throws ClientProtocolException,
 			IOException {
 
 		String jsonChore = JsonConverter.convertChoreToJson(chore).toString();
 		addChore(jsonChore);
 	}
 
 	@Override
 	public void updateApartmentLastDivision(String apartmentId, Date today)
 			throws ClientProtocolException, IOException {
 		Map<String, Object> keyValue = new HashMap<String, Object>();
 		keyValue.put("lastDivision", today.getTime());
 		JSONObject json = new JSONObject(keyValue);
 		String update = json.toString();
 		updateObject("Apartment", apartmentId, update);
 	}
 
 	public Coins getRoommateCoins(String username)
 			throws ClientProtocolException, IOException {
 
 		Map<String, Object> whereConditionsMap = new HashMap<String, Object>();
 		whereConditionsMap.put("username", username);
 		String result = QueryWhere("Coins", whereConditionsMap);
 		JSONObject jsonResult = new JSONObject(result);
 		JSONArray jsonArr = jsonResult.getJSONArray("results");
 		JSONObject coinsJson = jsonArr.getJSONObject(0);
 		Coins coins = JsonConverter.convertJsonToCoins(coinsJson);
 		return coins;
 	}
 
 	public void addCoins(Coins coins) throws IllegalStateException, IOException{
 		String coinsJson = JsonConverter.convertCoinsToJson(coins);
 		createObject("Coins", coinsJson);
 	}
 	public void setRommateDebt(String username, int debt)
 			throws ClientProtocolException, IOException {
 		Coins coins = getRoommateCoins(username);
 		Map<String, Object> keyValue = new HashMap<String, Object>();
 		keyValue.put("dept", debt);
 		JSONObject json = new JSONObject(keyValue);
 		String update = json.toString();
 		updateObject("Coins", coins.getId(), update);
 	}
 	public String getChoreIdFromJson(JSONObject json) {
 		String location = json.getString("Location");
 		String[] splits = location.split("/");
 		if (splits.length > 0) {
 			String id = splits[splits.length - 1];
 			return id;
 		}
 		return null;
 	}
 }
