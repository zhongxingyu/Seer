 package fr.eurecom.messaging;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.net.InetAddress;
 import java.net.InetSocketAddress;
 import java.net.Socket;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.os.StrictMode;
 import android.util.Log;
 
 public class Sender {
 	
 	public void send(Message message, InetAddress receiver) {
 		
 		//Hack
 	    if (android.os.Build.VERSION.SDK_INT > 9) {
 	        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
 	        StrictMode.setThreadPolicy(policy);
 	    }
 	    
 
 	    String messageToSend = serialize(message);
 	    if (messageToSend.length() == 0) return;
 	    
 		Socket socket = new Socket();
 		
 		try {
 			
 			socket.bind(null);
 			socket.connect((new InetSocketAddress(receiver, Config.PORT)), Config.TIMEOUT_INTERVAL);
 
 			OutputStream outputStream = socket.getOutputStream();
 			outputStream.write(messageToSend.getBytes());
 			outputStream.close();
 			
 		} catch (IOException e) {
			Log.e("Sender:send", e.getMessage());
 		} 
 		
 		finally {
 		    if (socket != null) {
 		        if (socket.isConnected()) {
 		            try {
 		                socket.close();
 		            } catch (IOException e) {
 		                //catch logic
 		            }
 		        }
 		    }
 		}
 	}
 	
 	private String serialize(Message message) {
 		JSONObject json = new JSONObject();
 		try {
 			json.put("what", message.what.ordinal());
 			json.put("about", message.about);
 		} catch (JSONException e) {
 			Log.e("Sender", "JSONError");
 			e.printStackTrace();
 		}
 		return json.toString();
 	}
 	
 }
