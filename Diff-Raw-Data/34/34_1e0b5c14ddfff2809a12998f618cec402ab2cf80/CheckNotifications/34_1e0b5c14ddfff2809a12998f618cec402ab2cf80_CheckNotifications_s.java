 package com.app.getconnected.notifications;
 
 import java.sql.Timestamp;
 import java.util.HashMap;
 import java.util.List;
 
 import android.content.Intent;
 import android.os.Handler;
import android.util.Log;
 
 import com.app.getconnected.R;
 import com.app.getconnected.activities.BaseActivity;
import com.app.getconnected.activities.JoinRideActivity;
 import com.app.getconnected.activities.JoinRideNotificationsActivity;
 import com.app.getconnected.activities.LoginActivity;
 import com.app.getconnected.activities.MarketplaceActivity;
import com.app.getconnected.activities.RequestRideActivity;
 import com.app.getconnected.sqllite.MarketplaceDatabaseHandler;
 
 public class CheckNotifications{
 	
 	private BaseActivity mpActivity;
 	private Handler mHandler = new Handler();
 	private MarketplaceDatabaseHandler helper;
 	private Timestamp lastCheck = new Timestamp(System.currentTimeMillis());
	//private final static int INTERVAL = 1000 * 60 * 1; //1 minutes
	private final static int INTERVAL = 1000 * 5;
 	
 	//Repeatable Runnable for checking notifications 
 	Runnable mHandlerTask = new Runnable()
 	{
 	     @Override 
 	     public void run() {
 	    	 
 	         checkNotifications();
 	         mHandler.postDelayed(mHandlerTask, INTERVAL);
 	     }
 	};
 	
 	public CheckNotifications(MarketplaceActivity marketplaceActivity){
 		mpActivity = marketplaceActivity;
 		helper = new MarketplaceDatabaseHandler(marketplaceActivity);
 		startRepeatingTask();
 	}
 	
 	/**
 	 * Get latest data from the database and check if there are new notifications available
 	 */
 	private void checkNotifications(){
 		List<HashMap<String, String>> ridesMap = helper.sendQuery("rides", null, "timestamp >= '" + lastCheck.toString() + "'", null, null, null, null, null);
 		List<HashMap<String, String>> joinsMap = helper.sendQuery("joins", null, "timestamp >= '" + lastCheck.toString() + "'", null, null, null, null, null);
 		
 		lastCheck = new Timestamp(System.currentTimeMillis());
 		
 		if (ridesMap != null && ridesMap.size() > 0){
 			
 			for (int i=0; i<ridesMap.size(); i++){
 				String jsonString = ridesMap.get(i).get("json");
 				Intent intent = new Intent(mpActivity, LoginActivity.class);
 				intent.putExtra("jsonString", jsonString);
 				mpActivity.createNotification(intent, R.string.new_ride_available, R.string.new_ride_description);
 			}
 		}
 		
 		if (joinsMap != null){
 		
 			if (joinsMap.size() > 0){
 				for (int i=0; i<joinsMap.size(); i++){
 					String jsonString = joinsMap.get(i).get("json");
 					Intent intent = new Intent(mpActivity, JoinRideNotificationsActivity.class);
 					intent.putExtra("jsonString", jsonString);
 					mpActivity.createNotification(intent, R.string.join_accepted, R.string.join_accepted_description);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Start notifications check
 	 */
 	private void startRepeatingTask()
 	{
 	    mHandlerTask.run(); 
 	}
 
	/**
	 * Stop notifications check
	 */
	private void stopRepeatingTask()
	{
	    mHandler.removeCallbacks(mHandlerTask);
	}

 }
