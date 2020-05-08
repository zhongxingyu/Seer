 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import javax.swing.JFrame;
 import javax.swing.JScrollPane;
 import javax.swing.JTextPane;
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.utils.URLEncodedUtils;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 public class json_prototype_2 {
 
 	private static JFrame frame;
 	private static ArrayList<HashMap<String, String>> eventsList = new ArrayList<HashMap<String, String>>();
 	private static JSONArray events;
 	private static JTextPane response;
 	private static String json;
 	private static JSONObject jsonObj;
 	private static InputStream is;
 
 	public static final String TAG_EVENTS = "message";
 	public static final String TAG_MESSAGE = "message";
 	public static final String TAG_CONTENT = "content";
 	public static final String TAG_TYPE = "type";
 	public static final String TAG_TIME = "time";
 	public static final String TAG_ID = "id";
 	public static final String TAG_DEVICEID = "deviceid";
 	public static final String TAG_EVENTID = "eventid";
 	public static final String TAG_RECEIVERID = "receiverid";
 
	private static final String REGISTER_DEVICE_URL = "http://localhost/register_device.php";
	private static final String CREATE_DEVICE_URL = "http://localhost/create_device.php";
 	private static final String CREATE_URL = "http://192.168.1.92/ubicomp/create_event.php";
 	private static final String GET_ALL_URL = "http://192.168.1.92/ubicomp/get_all_events.php";
 	private static final String UPDATE_URL = "http://192.168.1.92/ubicomp/update_event.php";
 	private static final String DELETE_URL = "http://192.168.1.92/ubicomp/delete_event.php";
 	private static final String DELETE_ALL_URL = "http://192.168.1.92/ubicomp/delete_events.php";
 	private static final String GET_URL = "http://192.168.1.92/ubicomp/get_event.php";
 
 	public json_prototype_2() {
 		frame = new JFrame("JsonPrototype");
 		response = new JTextPane();
 		response.setEditable(false);
 		JScrollPane jsp = new JScrollPane(response);
 		frame.add(jsp);
 		frame.setVisible(true);
		registerDevice();
 		createDeviceTable();
 		// newEvent("666", "BOOP");
 		// editEvent();
 		// getAllEvents();
 		// deleteEvent("1");
 		// updateEvent("1","42","Sinn des Lebens");
 		// getEvent("1");
 	}
 
 	private static String deviceID = "42";
 
 	public static String getDeviceID() {
 		return deviceID;
 	}
 
 	public static void setDeviceID(String deviceID) {
 		json_prototype_2.deviceID = deviceID;
 	}
 
 	public static JSONObject createDeviceTable() {
 		List<NameValuePair> params = new ArrayList<NameValuePair>();
 		params.add(new BasicNameValuePair(TAG_DEVICEID, getDeviceID()));
 		return makeHttpRequest(CREATE_DEVICE_URL, "POST", params);
 	}
 
 	public static JSONObject registerDevice() {
 		List<NameValuePair> params = new ArrayList<NameValuePair>();
 		params.add(new BasicNameValuePair(TAG_DEVICEID, getDeviceID()));
 		return makeHttpRequest(REGISTER_DEVICE_URL, "POST", params);
 	}
 
 	public static void newEvent(String type, String content) {
 		List<NameValuePair> params = new ArrayList<NameValuePair>();
 		params.add(new BasicNameValuePair("type", type));
 		params.add(new BasicNameValuePair("content", content));
 		makeHttpRequest(CREATE_URL, "POST", params);
 	}
 
 	public static void updateEvent(String id, String type, String content) {
 		List<NameValuePair> params = new ArrayList<NameValuePair>();
 		params.add(new BasicNameValuePair(TAG_ID, id));
 		params.add(new BasicNameValuePair(TAG_TYPE, type));
 		params.add(new BasicNameValuePair(TAG_CONTENT, content));
 		makeHttpRequest(UPDATE_URL, "POST", params);
 	}
 
 	public static void getEvent(String id) {
 		List<NameValuePair> params = new ArrayList<NameValuePair>();
 		params.add(new BasicNameValuePair("id", id));
 		makeHttpRequest(GET_URL, "GET", params);
 	}
 
 	public static void deleteEvent(String id) {
 		List<NameValuePair> params = new ArrayList<NameValuePair>();
 		params.add(new BasicNameValuePair("id", id));
 		makeHttpRequest(DELETE_URL, "POST", params);
 	}
 
 	public static void getAllEvents() {
 		List<NameValuePair> params = new ArrayList<NameValuePair>();
 		JSONObject getJsonObj = makeHttpRequest(GET_ALL_URL, "GET", params);
 		try {
 			events = getJsonObj.getJSONArray(TAG_EVENTS);
 			for (int i = 0; i < events.length(); i++) {
 				JSONObject c = events.getJSONObject(i);
 				String id = c.getString(TAG_ID);
 				String type = c.getString(TAG_ID);
 				String time = c.getString(TAG_CONTENT);
 				String content = c.getString(TAG_CONTENT);
 				HashMap<String, String> map = new HashMap<String, String>();
 				map.put(TAG_ID, id);
 				map.put(TAG_TYPE, type);
 				map.put(TAG_TIME, time);
 				map.put(TAG_CONTENT, content);
 				eventsList.add(map);
 			}
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public static JSONObject makeHttpRequest(String url, String method,
 			List<NameValuePair> params) {
 		try {
 			if (method == "POST") {
 				DefaultHttpClient httpClient = new DefaultHttpClient();
 				HttpPost httpPost = new HttpPost(url);
 				httpPost.setEntity(new UrlEncodedFormEntity(params));
 				HttpResponse httpResponse = httpClient.execute(httpPost);
 				HttpEntity httpEntity = httpResponse.getEntity();
 				is = httpEntity.getContent();
 			} else if (method == "GET") {
 				DefaultHttpClient httpClient = new DefaultHttpClient();
 				String paramString = URLEncodedUtils.format(params, "utf-8");
 				url += "?" + paramString;
 				HttpGet httpGet = new HttpGet(url);
 				HttpResponse httpResponse = httpClient.execute(httpGet);
 				HttpEntity httpEntity = httpResponse.getEntity();
 				is = httpEntity.getContent();
 			}
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 		} catch (ClientProtocolException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		try {
 			BufferedReader reader = new BufferedReader(new InputStreamReader(
 					is, "iso-8859-1"), 8);
 			StringBuilder sb = new StringBuilder();
 			String line = null;
 			while ((line = reader.readLine()) != null) {
 				sb.append(line + "\n");
 			}
 			is.close();
 			json = sb.toString();
 			System.out.println(json);
 		} catch (Exception e) {
 			System.out.println("Error converting result " + e.toString());
 		}
 		try {
 			jsonObj = new JSONObject(json.trim());
 		} catch (JSONException e) {
 			System.out.println("Error parsing data " + e.toString());
 		}
 		response.setText(jsonObj.toString(2));
 		return jsonObj;
 	}
 }
