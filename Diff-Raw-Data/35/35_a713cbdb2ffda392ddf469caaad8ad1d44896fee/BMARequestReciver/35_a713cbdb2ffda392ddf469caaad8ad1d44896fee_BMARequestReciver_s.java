 package com.android_prod;
 
 import java.util.ArrayList;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 
 
 
 
 /**
  * reciver for information comming to the intent service and he call static method inside the activity
  * @author ronan
  *
  */
 public  class BMARequestReciver extends BroadcastReceiver {
 
 
 
 			public BMARequestReciver()
 			{
 				
 			}
 	
 
 			public void onReceive(Context context, Intent intent) {
 				
				Log.d("BACK DATA", "TEST ");
			
 			      MapViewClass. majData(intent.getStringExtra("bikeData"),intent.getStringExtra("busData"),intent.getStringExtra("metroData"),intent.getStringExtra("borneData"),intent.getStringExtra("trainData"));
 				
 						
 			}
 
 	    
 
 }
