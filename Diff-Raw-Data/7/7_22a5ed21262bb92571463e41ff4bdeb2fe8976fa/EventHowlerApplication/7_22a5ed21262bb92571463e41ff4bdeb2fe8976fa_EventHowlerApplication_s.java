 package com.onb.eventHowler.application;
 
 import com.onb.eventHowler.service.*;
 import android.app.Application;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.util.Log;
 
 
 public class EventHowlerApplication extends Application{
 	
 	private static boolean withOngoingEvent;
 	private boolean runningLastCycle;
 	private boolean sendingServiceRunning;
 	
 	private Status eventHowlerURLRetrieverServiceStatus;
 
 	private final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
 	private EventHowlerBroadcastReceiver eventHowlerBroadcastReceiver = new EventHowlerBroadcastReceiver();
 	private IntentFilter SMS_RECEIVED_FILTER = new IntentFilter(SMS_RECEIVED);
 	
 	private String eventId, secretKey;
 	
 
 	@Override
 	public void onCreate() {
 		super.onCreate();
 	}
 		
 	public boolean hasOngoingEvent(){
 		return withOngoingEvent;
 	}
 	
 	public static boolean hasOngoingEventGlobal() {
 		return withOngoingEvent;
 	}
 	
 	public void startRetrievingToURL(){
 		sendingServiceRunning = false;
 		withOngoingEvent = true;
 		startService(new Intent(this, EventHowlerURLRetrieverService.class));
 	}
 	
 	public void startEvent(){
 		sendingServiceRunning = true;
 		Log.d("startEvent", "starting event");
 		registerReceiver(eventHowlerBroadcastReceiver, SMS_RECEIVED_FILTER);
 		startService(new Intent(this, EventHowlerSenderService.class));
 	}
 	
 	public void stopEvent(){
 		Log.d("stopEvent", "stopping event");
 		runningLastCycle = true;
 		withOngoingEvent = false;
 		if(sendingServiceRunning){
 			unregisterReceiver(eventHowlerBroadcastReceiver);
 		}
 		else{
 			runningLastCycle = false;
 		}
 	}
 	
 	public boolean isRunning() {
 		return runningLastCycle;
 	}
 
 	public void setRunning(boolean finishing) {
 		this.runningLastCycle = finishing;
 	}
 
 	public String getEventId() {
 		return eventId;
 	}
 
 	public void setEventId(String eventId) {
 		this.eventId = eventId;
 	}
 
 	public String getSecretKey() {
 		return secretKey;
 	}
 
 	public void setSecretKey(String secretKey) {
 		this.secretKey = secretKey;
 	}
 	
 	public Status getEventHowlerURLRetrieverServiceStatus() {
 		return eventHowlerURLRetrieverServiceStatus;
 	}
 
 	public void setEventHowlerURLRetrieverServiceStatus(
 			Status eventHowlerURLRetrieverService) {
 		this.eventHowlerURLRetrieverServiceStatus = eventHowlerURLRetrieverService;
 	}
 }
