 package se.kudomessage.jessica;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.io.PrintWriter;
 import java.net.Socket;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.util.Log;
 
 public class PushController {
 	private static Socket socket = null;
 	private static BufferedReader in = null;
 	private static PrintWriter out = null;
 	
 	private static boolean openConnection() {	
 		try {
 			socket = new Socket(Globals.getServer(), CONSTANTS.SERVER_PORT);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 		if (socket != null) {
 			in = null;
 			out = null;
 			
 			try {
 				in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
 				
 				OutputStreamWriter outstream = new OutputStreamWriter(socket.getOutputStream(), "UTF-8");
 				out = new PrintWriter(outstream, true);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 			
 			if (in != null && out != null) {
				Log.i(CONSTANTS.TAG, "Connected to server " + Globals.getServer() + ":" + CONSTANTS.SERVER_PORT);
 			
 				out.println("GATEWAY");
 				out.flush();
 				
 				return true;
 			}
 		}
 		
 		return false;
 	}
 	
 	private static void closeConnection() {
 		try {
 			out.println("CLOSE");
 			out.flush();
 			
 			in.close();
 			out.close();
 			socket.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 		in = null;
 		out = null;
 		socket = null;
 	}
 	
 	public static void registerDevice() {
 		new Thread(new Runnable() {
 			public void run() {
 				if (openConnection()) {
 					try {
 						JSONObject output = new JSONObject();
 						output.put("action", "register-device");
 						output.put("token", Globals.getAccessToken());
 						
 						output.put("gcm", Globals.getGCM());
 						
 						out.println(output.toString());
 						out.flush();
 					} catch (Exception e) {
 						e.printStackTrace();
 					}
 				}
 				
 				closeConnection();
 			}
 		}).start();
 	}
 	
 	public static void pushMessage(final String content, String origin, String receiver) {
		//TODO: Fix for internationalization
 		final String _receiver = receiver.replace(" ", "").replace("+46", "0").replace("-", "");
 		final String _origin = origin.replace(" ", "").replace("+46", "0").replace("-", "");
 		
 		new Thread(new Runnable() {
 			public void run() {
 				if (openConnection()) {
 					try {
 						JSONObject output = new JSONObject();
 						output.put("action", "push-message");
 						output.put("token", Globals.getAccessToken());
 						
 						JSONObject message = new JSONObject();
 						message.put("content", content);
 						message.put("origin", _origin);
 						message.put("receiver", _receiver);
 						
 						output.put("message", message);
 						
 						out.println(output.toString());
 						out.flush();
 					} catch (Exception e) {
 						e.printStackTrace();
 					}
 				}
 				
 				closeConnection();
 			}
 		}).start();
 	}
 
 	public static void pushMessage(KudoMessage message) {
 		pushMessage(message.content, message.origin, message.getFirstReceiver());
 	}
 }
