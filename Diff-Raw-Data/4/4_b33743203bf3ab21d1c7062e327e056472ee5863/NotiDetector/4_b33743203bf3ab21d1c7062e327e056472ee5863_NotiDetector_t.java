 //This Class Is Not Done Yet :P
 
 package com.simpleminds.popbell;
 
 import java.util.List;
 
 import wei.mark.standout.StandOutWindow;
 
 import android.accessibilityservice.AccessibilityService;
 import android.accessibilityservice.AccessibilityServiceInfo;
 import android.app.Notification;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Build;
 import android.os.Bundle;
 import android.os.Parcelable;
 import android.provider.Settings.SettingNotFoundException;
 import android.text.TextUtils;
 import android.util.Log;
 import android.view.accessibility.AccessibilityEvent;
 import android.widget.Toast;
 import android.provider.Settings;
 import android.app.PendingIntent;
 
 public class NotiDetector extends AccessibilityService {
 	@Override
 	public void onAccessibilityEvent(AccessibilityEvent event) {
 	    System.out.println("onAccessibilityEvent");
 	    if (event.getEventType() == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
 	        System.out.println("notification: " + event.getText());
 	        
 	        try {   
 		    	StandOutWindow.closeAll(this, SimpleWindow.class);
 				// show a MultiWindow, SimpleWindow
 
 				
 		        Intent intent = new Intent(this, SimpleWindow.class);  
 		      
		        intent.putExtra("sysnotitext", (CharSequence) event.getText());
		        PendingIntent pendingIntent = PendingIntent.getService(getBaseContext(), 0, intent, 0);
 		    
 		        //Log.e("ALARM", "time of millis: "+System.currentTimeMillis());
 		  
 
 		        } catch (Exception e) {
 		            Log.e("SYSNOTIDETECTOR", "ERROR IN CODE:"+e.toString());
 		        }
 	    }
 	    
 	    }
 	
 	@Override
 	protected void onServiceConnected() {
 	    System.out.println("onServiceConnected");
 	    AccessibilityServiceInfo info = new AccessibilityServiceInfo();
 	    info.eventTypes = AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED;
 	    info.notificationTimeout = 100;
 	    info.feedbackType = AccessibilityEvent.TYPES_ALL_MASK;
 	    setServiceInfo(info);
 	}
 
 	@Override
 	public void onInterrupt() {
 	    System.out.println("onInterrupt");
 	}
 	}
