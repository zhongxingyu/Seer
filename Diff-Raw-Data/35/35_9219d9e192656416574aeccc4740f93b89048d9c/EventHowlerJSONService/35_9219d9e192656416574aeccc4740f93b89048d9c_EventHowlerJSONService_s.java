 package com.onb.eventHowler.service;
 
 import java.util.List;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.onb.eventHowler.application.EventHowlerJSONHelper;
 import com.onb.eventHowler.application.EventHowlerOpenDbHelper;
 import com.onb.eventHowler.domain.EventHowlerParticipant;
 
 import android.app.Service;
 import android.content.Intent;
 import android.os.IBinder;
 
 public class EventHowlerJSONService extends Service {
 
 	private static EventHowlerOpenDbHelper openHelper;
 	
 	@Override
 	public void onCreate() {
 		openHelper = new EventHowlerOpenDbHelper(getApplicationContext());
 	}
 	
 	@Override
 	public IBinder onBind(Intent intent) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
	public void retrieveAndStoreFromURL(String url)
 	{
 		List<JSONArray> list = EventHowlerJSONHelper.extractFromURL(url);
 		
 		for(JSONArray jArray: list) {
 			for(int i = 0; i < jArray.length(); i++){
 				try {
 					JSONObject jObject = jArray.getJSONObject(i);
					EventHowlerParticipant participant = EventHowlerJSONHelper.convertJSONObjectToParticipant(jObject);
					openHelper.insertParticipant(participant);
 				} catch (JSONException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
				
 			}
 		}
 	}
 }
