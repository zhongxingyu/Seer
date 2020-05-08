 /*
  * Created by Lolay, Inc.
  * Copyright 2011 Lolay, Inc. All rights reserved.
  */
 package com.lolay.android.tracker;
 
 import java.util.Hashtable;
 import java.util.Map;
 
 import android.app.Application;
 
 import com.omniture.AppMeasurement;
 
 public class LolayOmnitureTracker extends LolayBaseTracker {
 	private AppMeasurement s;
 	private Hashtable<String,String> globalParameters = null;
 	private Delegate delegate = null;
 	
 	public LolayOmnitureTracker(Application application, String trackingServer, String account) {
 		s = new AppMeasurement(application);
 		s.trackingServer = trackingServer;
 		s.account = account;
 	}
 	
 	public AppMeasurement getAppMeasurement() {
 		return s;
 	}
 	
 	public void setDelegate(Delegate delegate) {
 		this.delegate = delegate;
 	}
 	
 	public void setDebug(boolean debug) {
 		s.debugTracking = debug;
 	}
 	
 	public void setCurrency(String currency) {
 		s.currencyCode = currency;
 	}
 	
     @Override
     public void setIdentifier(String identifier) {
     	s.visitorID = identifier;
     	if (delegate != null) {
     		delegate.identifierWasSet(this, identifier);
     	}
     }
 
     @Override
     public void setVersion(String version) {
     	if (delegate != null) {
     		delegate.setVersion(this, version);
     	}
     }
 
     @Override
     public void setAge(int age) {
     	if (delegate != null) {
     		delegate.setAge(this, age);
     	}
     }
 
     @Override
     public void setGender(LolayTrackerGender gender) {
     	if (delegate != null) {
     		delegate.setGender(this, gender);
     	}
     }
 
     @Override
     public void setState(String state) {
     	s.state = state;
     	if (delegate != null) {
     		delegate.stateWasSet(this, state);
     	}
     }
 
     @Override
     public void setZip(String zip) {
     	s.zip = zip;
     	if (delegate != null) {
     		delegate.zipWasSet(this, zip);
     	}
     }
 
     @Override
     public void setCampaign(String campaign) {
     	s.campaign = campaign;
     	if (delegate != null) {
     		delegate.campaignWasSet(this, campaign);
     	}
     }
     
     private Hashtable<String,String> stringHashtable(Map<Object,Object> objectMap) {
    	Hashtable<String,String> stringMap = new Hashtable<String,String>(globalParameters.size());
     	for (Map.Entry<Object, Object> entry : objectMap.entrySet()) {
     		Object keyObject = entry.getKey();
     		Object valueObject = entry.getValue();
     		
     		String key = keyObject instanceof String ? (String) keyObject : keyObject.toString();
     		String value = valueObject instanceof String ? (String) valueObject : valueObject.toString();
     		stringMap.put(key, value);
     	}
     	return stringMap;
     }
 
     @Override
     public void setChannel(String channel) {
     	s.channel = channel;
     	if (delegate != null) {
     		delegate.channelWasSet(this, channel);
     	}
     }
     
     @Override
     public void setGlobalParameters(Map<Object, Object> globalParameters) {
     	this.globalParameters = stringHashtable(globalParameters);
     	if (delegate != null) {
     		delegate.globalParametersWasSet(this, globalParameters);
     	}
     }
     
     @Override
     public void logEvent(String name) {
     	s.pageURL = name;
     	s.track();
     }
     
     public Hashtable<String,String> mergeParameters(Map<Object,Object> parameters) {
     	Hashtable<String,String> mergedParameters;
     	if (this.globalParameters != null) {
     		mergedParameters = new Hashtable<String, String>(this.globalParameters.size() + parameters.size());
     		mergedParameters.putAll(this.globalParameters);
     		mergedParameters.putAll(stringHashtable(parameters));
     	} else {
     		mergedParameters = stringHashtable(parameters);
     	}
     	return mergedParameters;
     }
 
     @Override
     public void logEventWithParams(String name, Map<Object,Object> parameters) {
     	s.pageURL = name;
     	s.track(mergeParameters(parameters));
     }
 
     @Override
     public void logPage(String name) {
     	s.pageURL = name;
     	s.track();
     }
 
     @Override
     public void logPageWithParams(String name, Map<Object,Object> parameters) {
     	s.pageURL = name;
     	s.track(mergeParameters(parameters));
     }
 
     @Override
     public void logException(Throwable throwable) {
     	s.pageURL = "Exception";
     	s.track();
     }
 
     @Override
     public void logException(String errorId, String message, Throwable throwable) {
     	s.pageURL = "Exception";
     	s.track();
     }
     
     public static interface Delegate {
     	public void identifierWasSet(LolayOmnitureTracker tracker, String identifier);
     	public void setVersion(LolayOmnitureTracker tracker, String version);
     	public void setAge(LolayOmnitureTracker tracker, int age);
     	public void setGender(LolayOmnitureTracker tracker, LolayTrackerGender gender);
     	public void stateWasSet(LolayOmnitureTracker tracker, String state);
     	public void zipWasSet(LolayOmnitureTracker tracker, String zip);
     	public void campaignWasSet(LolayOmnitureTracker tracker, String campaign);
     	public void globalParametersWasSet(LolayOmnitureTracker tracker, Map<Object, Object> globalParameters);
     	public void channelWasSet(LolayOmnitureTracker tracker, String channel);
     }
 }
