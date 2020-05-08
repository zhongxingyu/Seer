 package tetris.common;
 
 import java.applet.Applet;
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
 import java.lang.reflect.Type;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.util.Map;
 
 import netscape.javascript.JSObject;
 
 import com.google.gson.Gson;
 import com.google.gson.reflect.TypeToken;
 
 public class ServerConnector {
 	Applet applet;
 	String path;
 	String game_session;
 	
	public ServerConnector(Applet applet, String path) {
 		this.applet = applet;
		this.path = path;
 		try {
 			game_session = getGameSession();
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		if(game_session == null)
 			applet.stop();
 	}
 
 	private String getCookie() {
 		/*
 		 * * get all cookies for a document
 		 */
 		try {
 			JSObject myBrowser = (JSObject) JSObject.getWindow(applet);
 			JSObject myDocument = (JSObject) myBrowser.getMember("document");
 			String myCookie = (String) myDocument.getMember("cookie");
 			if (myCookie.length() > 0)
 				return myCookie;
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return "?";
 	}
 
 	private String getSessionCookie() {
 		String myCookie = getCookie();
 		String search = "gamesmoon" + "=";
 		if (myCookie.length() > 0) {
 			int offset = myCookie.indexOf(search);
 			if (offset != -1) {
 				offset += search.length();
 				int end = myCookie.indexOf(";", offset);
 				if (end == -1)
 					end = myCookie.length();
 				return myCookie.substring(offset, end);
 			}
 		}
 		return "";
 	}
 
 	private String auth(String session) throws Exception {
 		String body = "session_key=" + URLEncoder.encode(session, "UTF-8")
 				+ "&" + "game_name=" + URLEncoder.encode("tetris", "UTF-8");
 
 		URL url = new URL(path + "game_auth.php");
 		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
 		connection.setRequestMethod("POST");
 		connection.setDoInput(true);
 		connection.setDoOutput(true);
 		connection.setUseCaches(false);
 		connection.setRequestProperty("Content-Type",
 				"application/x-www-form-urlencoded");
 		connection.setRequestProperty("Content-Length",
 				String.valueOf(body.length()));
 
 		OutputStreamWriter writer = new OutputStreamWriter(
 				connection.getOutputStream());
 		writer.write(body);
 		writer.flush();
 
 		BufferedReader reader = new BufferedReader(new InputStreamReader(
 				connection.getInputStream()));
 
 		String back = new String();
 		for (String line; (line = reader.readLine()) != null;) {
 			back += line;
 		}
 
 		writer.close();
 		reader.close();
 
 		return back;
 	}
 	
 	private String getGameSession() throws Exception {
 		String json = auth(getSessionCookie());
 		Gson gson = new Gson();
 		Type type = new TypeToken<Map<String, String>>(){}.getType();
 		Map<String, String> values = gson.fromJson(json, type);
 		return values.get("game_session");
 	}
 	
 	public boolean sendScore(int score) throws Exception {
 		String body = "game_session=" + URLEncoder.encode(game_session, "UTF-8")
 				+ "&" + "score=" + URLEncoder.encode(Integer.toString(score), "UTF-8");
 
 		URL url = new URL(path + "game_score.php");
 		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
 		connection.setRequestMethod("POST");
 		connection.setDoInput(true);
 		connection.setDoOutput(true);
 		connection.setUseCaches(false);
 		connection.setRequestProperty("Content-Type",
 				"application/x-www-form-urlencoded");
 		connection.setRequestProperty("Content-Length",
 				String.valueOf(body.length()));
 
 		OutputStreamWriter writer = new OutputStreamWriter(
 				connection.getOutputStream());
 		writer.write(body);
 		writer.flush();
 
 		BufferedReader reader = new BufferedReader(new InputStreamReader(
 				connection.getInputStream()));
 
 		String back = new String();
 		for (String line; (line = reader.readLine()) != null;) {
 			back += line;
 		}
 
 		writer.close();
 		reader.close();
 		
 		if(back.length() > 0)
 			return true;
 		else
 			return false;
 	}
 }
