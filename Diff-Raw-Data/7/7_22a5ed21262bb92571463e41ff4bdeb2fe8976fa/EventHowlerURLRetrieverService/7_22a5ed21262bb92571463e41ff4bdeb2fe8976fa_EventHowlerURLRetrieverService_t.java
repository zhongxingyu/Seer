 package com.onb.eventHowler.service;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.util.List;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.onb.eventHowler.application.EventHowlerApplication;
 import com.onb.eventHowler.application.EventHowlerJSONHelper;
 import com.onb.eventHowler.application.EventHowlerOpenDbHelper;
 import com.onb.eventHowler.application.Status;
 import com.onb.eventHowler.domain.EventHowlerParticipant;
 
 import android.app.Service;
 import android.content.Intent;
 import android.database.Cursor;
 import android.os.IBinder;
 import android.util.Log;
 import android.widget.Toast;
 
 public class EventHowlerURLRetrieverService extends Service{
 
 	private static EventHowlerOpenDbHelper openHelper;
 	private static final String QUERY_URL_FORMAT = "http://%s:%s/EventHowlerApp/query?id=%s&secretKey=%s";
 	private static final String WEB_DOMAIN = "10.10.6.83";
 	private static final String PORT_NO = "8080";
 	private EventHowlerApplication application;
 			
 	@Override
 	public void onCreate() {
 		
 	}
 	
 	@Override
 	public IBinder onBind(Intent intent) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 	
 	@Override
 	public int onStartCommand(Intent intent, int flags, int startId) {
 		
 		Log.d("onStartCommand", "starting URL Retriever");
 		
 		openHelper = new EventHowlerOpenDbHelper(getApplicationContext());
 		application = (EventHowlerApplication)(getApplication());
 		
 		String id = application.getEventId();
 		String secretKey = application.getSecretKey();
 
 		startQuerying(id,secretKey);
 		return Service.START_NOT_STICKY;
 	}
 	
 	public void startQuerying(final String id, final String secretKey)
 	{		
 		Thread queryThread = new Thread( new Runnable() {
 			public void run(){				
 				boolean serviceStarted = false;
 		
 				while(application.hasOngoingEvent()){
 					//application.setEventHowlerURLRetrieverServiceStatus(Status.RUNNING); if errors are present.
 					retrieveAndStoreEventInfoFromIdAndKey(id, secretKey);
 					
 					if(!participantIsEmpty() && !serviceStarted) {
 						Log.d("not empty participant", "here here");
 						startRunning();
 						serviceStarted = true;
 					}
 					threadSleep();
 				}
 			}
 
 			private boolean participantIsEmpty() {
 				Cursor participants = openHelper.getAllParticipants();
 				boolean result = (participants.getCount() == 0);
 				participants.close();
 				return result;
 			}
 		});
 		
 		queryThread.start();
 	}
 	
 	private void threadSleep() {
 		try {
 			Thread.sleep(10000);
 		}
 		catch (Exception e) {Log.d("startQuerying", "UNABLE TO SLEEP");}
 	}
 	
 	/**
 	 * Retrieves participant data from the web application 
 	 * using the corresponding event ID and secret key
 	 * 
 	 * @param id			unique event id
 	 * @param secretKey		corresponding event key
 	 */
 	public void retrieveAndStoreEventInfoFromIdAndKey(String id, String secretKey) {
 		String query = generateQueryURL(id, secretKey);
 		retrieveAndStoreEventInfoFromURL(query);
 	}
 	
 	/**
 	 * Generates a URL for querying from the web application
 	 * 
 	 * @param id			unique event id
 	 * @param secretKey		corresponding event key
 	 * @return				generated query URL
 	 */
 	public String generateQueryURL(String id, String secretKey) {
 		Log.d("generateQueryURL", String.format(QUERY_URL_FORMAT, WEB_DOMAIN, PORT_NO, id, secretKey));
 		return String.format(QUERY_URL_FORMAT, WEB_DOMAIN, PORT_NO, id, secretKey);
 	}
 
 	/**
 	 * Retrieves a JSON formatted String from a specified URL.
 	 * Stores the participant data from the JSON formatted String into
 	 * the local database.
 	 * 
 	 * @param url location of web page to retrieve and store data from
 	 */
 	public void retrieveAndStoreEventInfoFromURL(String url) {
 		List<JSONObject> list;
 		EventHowlerApplication app = (EventHowlerApplication)getApplication();
 		try {
 			list = EventHowlerJSONHelper.extractFromURL(url);
 			for(JSONObject entry: list) {
 
 				extractParticipants(entry);
 				try {
 					extractMessage(entry);
 				} catch (JSONException e) {
 					e.printStackTrace();
 					continue;
 				}			
 			}
 		} catch (MalformedURLException e1) {
 			// TODO 
 			this.stopRunning();
 			app.stopEvent();
 			e1.printStackTrace();
 		} catch (IOException e1) {
 			// TODO
 			this.stopRunning();
 			app.stopEvent();
 			e1.printStackTrace();
 		}
 		
 		
 	}
 	
 	public void stopRunning() {
 		application.setEventHowlerURLRetrieverServiceStatus(Status.STOP);
 	}
 	
 	public void startRunning() {
 		Log.d("make it run", "na change ko na");
 		application.setEventHowlerURLRetrieverServiceStatus(Status.RUNNING);
 	}
 
 	/**
 	 * Extracts and stores all participant data from a single JSONObject 
 	 * representing a JSON formatted query result.
 	 * 
 	 * @param entry				JSONObject representing a single query result
 	 * @throws JSONException
 	 */
 	public void extractParticipants(JSONObject entry) {
 		try{
 			JSONArray participants = entry.getJSONArray(EventHowlerJSONHelper.ATTRIBUTE_CONTENT);
 			
 			for(int i = 0; i < participants.length(); i++) {
 				try {
 					JSONObject participant = participants.getJSONObject(i);
 					storeAsParticipant(participant);
 				} catch (JSONException e) {
 					e.printStackTrace();
 					continue;
 				}
 			}
 		}
 		catch (JSONException e){
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Extracts and stores message from a single JSONObject 
 	 * representing a JSON formatted query.
 	 * 
 	 * @param entry				JSONObject representing a single query result
 	 * @throws JSONException
 	 */
 	public void extractMessage(JSONObject entry) throws JSONException {
 		String message = entry.getString(EventHowlerJSONHelper.ATTRIBUTE_MESSAGE);
 		Log.d("extractMessage", message);
 		
 		openHelper.populateMessages(message);
 	}
 	
 	/**
 	 * Creates an EventHowlerParticipant from a JSONObject and
 	 * stores it in the database.
 	 * 
 	 * @param jObject		JSONObject to be stored as an EventHowlerParticipant
 	 * @throws JSONException
 	 */
 	public void storeAsParticipant(JSONObject jObject) throws JSONException {
 		EventHowlerParticipant participant = EventHowlerJSONHelper.convertJSONObjectToParticipant(jObject);
 
 		Log.d("STORING PARTICIPANT", "Phone: " + participant.getPhoneNumber() 
 				+ "Trans_id: " + participant.getTransactionId());
 		
 		if(openHelper.checkNumberIfExist(participant.getPhoneNumber())) {
 			openHelper.updateStatus(participant, "");
 		}
 		else {
 			openHelper.insertParticipant(participant);	
 		}
 	}
 	
 	@Override
 	public void onDestroy() {
		stopRunning();
 		Toast.makeText(this, "event Howler sending service destroyed",
 				Toast.LENGTH_SHORT).show();
 		openHelper.close();
 		super.onDestroy();
 	}
 }
