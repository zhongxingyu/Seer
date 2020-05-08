 package fr.eurecom.messaging;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.InetAddress;
 import java.net.ServerSocket;
 import java.net.Socket;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.os.AsyncTask;
import android.os.Process;
 import android.util.Log;
 
 public class Receiver extends AsyncTask<String, Void, JSONObject> { //implements Runnable {
 	private static final String TAG = "Receiver";
 	private boolean listening;
 	private Client client;
 	private ServerSocket socket;
 	private JSONObject json;
 	private Socket sender;
 
 	public Receiver(Client client) {
 		Log.d(Receiver.TAG, "Constructor");
 		this.listening = true;
 		this.client = client;
 	}
 	
	/*
	@Override
	public void run() {
		Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
		
		try {
			socket = new ServerSocket(Config.PORT);
			Log.d(Receiver.TAG, "Client: Socket opened!!");
			while (listening) {
				// Wait for incoming message
				Socket sender = socket.accept();
				// Read incoming message
				BufferedReader in = new BufferedReader(new InputStreamReader(sender.getInputStream()));			
				// Transform message to JSON Object
				JSONObject json = new JSONObject(in.readLine());
				// Send message to client
				Log.d("Tekst fra host", "Inputstreamen er: " + json.toString());
				receiveMessage(json, sender.getInetAddress());
				sender.close();
			}	
		} catch (IOException e) {
			return;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return;
	}
	*/
	
	
 	@Override
 	protected void onProgressUpdate(Void... values) {
		// TODO Auto-generated method stub
 		super.onProgressUpdate(values);
 		
 		receiveMessage(json, sender.getInetAddress());
 	}
 	
 	
 	@Override
 	protected JSONObject doInBackground(String... params) {
 		try {
 			socket = new ServerSocket(Config.PORT);
 			Log.d(Receiver.TAG, "Client: Socket opened!!");
 			while (listening) {
 				// Wait for incoming message
 				sender = socket.accept();
 				// Read incoming message
 				BufferedReader in = new BufferedReader(new InputStreamReader(sender.getInputStream()));			
 				// Transform message to JSON Object
 				json = new JSONObject(in.readLine());
 				// Send message to client
 				Log.d("Tekst fra host", "Inputstreamen er: " + json.toString());
 				
 				sender.close();
 				
 				publishProgress();
 			}
 			
 		} catch (IOException e) {
 			
 			return null;
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		return null;
 	}
 	
 	
 	
 	private void receiveMessage(JSONObject json, InetAddress sender){
 		try {
 			Action action = Action.values()[json.getInt("action")];
 			String subject = json.getString("subject");
 			Message message = new Message(action, subject);
 			message.setSender(sender);
 			client.handleMessage(message);
 		} catch (JSONException e){
 			Log.e("ClientInterpreter:receiveMessage", e.getMessage());
 		}
 	}
 	
 	
 	public void stopListening(){
 		this.listening = false;
 		
 		
 		try {
 			this.socket.close();
 		} catch (IOException e) {
 			Log.e("Receiver:stopListening", e.getMessage());
 		}
 		
 	}
 	
 }
