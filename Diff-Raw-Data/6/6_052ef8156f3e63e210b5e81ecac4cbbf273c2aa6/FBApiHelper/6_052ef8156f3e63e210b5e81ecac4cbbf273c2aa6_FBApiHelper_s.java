 package terraform.common;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.util.Properties;
 
 import javax.net.ssl.HttpsURLConnection;
 
 public class FBApiHelper {
 	private static String _APP_ID;
 	private static String _APP_SECRET;
 
 	static {
 		try {
 			Properties prop = new Properties();
 			prop.load(FBApiHelper.class.getResourceAsStream("fb.properties"));
 
			_APP_ID = prop.getProperty("APP_ID");
			_APP_SECRET = prop.getProperty("APP_SECRET");
 		} catch (IOException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	public static String getUserToken(String code) {
 		String token = "null";
 		try {
 			URL url = new URL(
 					"https://graph.facebook.com/oauth/access_token?client_id="
 							+ _APP_ID
 							+ "&redirect_uri=http://localhost:8080/login&client_secret="
 							+ _APP_SECRET + "&code=" + code);
 
 			HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
 			String parsedToken = getToken(conn);
 			if (parsedToken != null) {
 				token = parsedToken;
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		return token;
 	}
 
 	private static String getToken(HttpsURLConnection con) {
 		String token = null;
 
 		if (con != null) {
 			HTTPSResponseHolder respHolder = getResponse(con);
 
 			if (respHolder != null && respHolder.isSuccess
 					&& respHolder.content.length() > 0) {
 				String response = respHolder.content;
 				String[] splits = response.split("&");
 				for (int i = 0; i < splits.length; i++) {
 					String[] splitsAgain = splits[i].split("=");
 					if (splitsAgain[0].equalsIgnoreCase("access_token")) {
 						token = splitsAgain[1];
 						break;
 					}
 				}
 			} else {
 				System.out.println("Error Response:" + respHolder.content);
 			}
 		}
 
 		return token;
 	}
 
 	private static HTTPSResponseHolder getResponse(HttpsURLConnection conn) {
 		HTTPSResponseHolder response = null;
 		BufferedReader br = null;
 
 		try {
 			boolean isSuccess = false;
 			int responseCode = conn.getResponseCode();
 
 			if (responseCode == 200) {
 				isSuccess = true;
 				br = new BufferedReader(new InputStreamReader(
 						conn.getInputStream()));
 			} else {
 				br = new BufferedReader(new InputStreamReader(
 						conn.getErrorStream()));
 			}
 
 			// System.out.println("****** Response Contents ********");
 
 			StringBuilder strB = new StringBuilder();
 			String line = null;
 
 			while ((line = br.readLine()) != null) {
 				// System.out.println(line);
 				strB.append(line);
 				strB.append("\n");
 			}
 
 			response = new HTTPSResponseHolder();
 			response.isSuccess = isSuccess;
 			response.content = strB.toString();
 		} catch (Exception e) {
 			e.printStackTrace();
 		} finally {
 			if (br != null) {
 				try {
 					br.close();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 
 		return response;
 	}
 
 	public static String getUserLikes(String token) {
 		try {
 			URL url = new URL(
 					"https://graph.facebook.com/me/likes?fields=category,name&limit=500&method=GET&access_token="
 							+ token);
 
 			HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
 
 			HTTPSResponseHolder respHolder = getResponse(conn);
 			if (respHolder != null) {
 				System.out.println(respHolder.content);
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		return null;
 	}
 
 	private static class HTTPSResponseHolder {
 		private boolean isSuccess;
 		private String content;
 	}
 }
