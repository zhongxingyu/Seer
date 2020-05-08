 package com.datakom.POIObjects;
 
 import java.util.ArrayList;
 
 import org.haggle.Attribute;
 import org.haggle.DataObject;
 import org.haggle.EventHandler;
 import org.haggle.Node;
 import org.haggle.Handle;
 import org.haggle.DataObject.DataObjectException;
 
 import android.util.Log;
 
 
 /**
  * @author aa a
  * 
  * Haggle controller implementing interface
  */
 public class HaggleConnector implements EventHandler {
 	private static final String HAGGLE_TAG = "HagglePOI"; 
 	public static final String STORAGE_PATH = "/sdcard/HagglePOI";
 	
 	private Handle hh;
 	
 	private static final int STATUS_OK = 0;
 	private static final int STATUS_REG_FAILED = -2;
 	private static final int STATUS_SPAWN_DAEMON_FAILED = -3;
 	
 	private static  final int NUM_RETRIES = 2;
 	
 	//Singleton instance
 	private static HaggleConnector uniqueInstance;
 	
 	/* Such that several Activities in Front end can share the same HaggleConnector object*/
 	public static synchronized HaggleConnector getInstance() {
 		if (uniqueInstance == null) {
 			uniqueInstance = new HaggleConnector();
 			uniqueInstance.initHaggle();
 		}
 		return uniqueInstance;
 	}
 	/* this could be private later on */
 	public Handle getHaggleHandle() {
 		if (hh == null) {
 			Log.e(getClass().getSimpleName(), "Haggle Handle is null!");
 		}
 		
 		return hh;
 	}
 	
 	private int initHaggle() {
 		if (hh != null)
 			return STATUS_OK; 
 
 		Log.d(getClass().getSimpleName(), "Trying to Spawn haggle daemon");
 		
 		if (!Handle.spawnDaemon()) {
 			Log.d(getClass().getSimpleName(), "Spawning failed");
 			return STATUS_SPAWN_DAEMON_FAILED;
 		}
 		
 		long pid = Handle.getDaemonPid(); 
 		Log.d(getClass().getSimpleName(), "Haggle daemon pid is: " + pid);
 
 		
 		for (int tries = 0; tries < NUM_RETRIES; tries++) {
 			try {
 				hh = new Handle(HAGGLE_TAG);
 				
 				hh.registerEventInterest(EVENT_NEIGHBOR_UPDATE, this);
 				hh.registerEventInterest(EVENT_NEW_DATAOBJECT, this);
 				hh.registerEventInterest(EVENT_INTEREST_LIST_UPDATE, this);
 				hh.registerEventInterest(EVENT_HAGGLE_SHUTDOWN, this);
 				
 				hh.eventLoopRunAsync();
 				hh.getApplicationInterestsAsync();
 				
 				Log.d(getClass().getSimpleName(), "Haggle event loop started");
 				
 				return STATUS_OK;
 				 
 			} catch (Handle.RegistrationFailedException e) {
 				Log.e(getClass().getSimpleName(), "Registration failed: " + e.getMessage());
 				
 				Handle.unregister(HAGGLE_TAG);
 			}
 		}
 
 		Log.e(getClass().getSimpleName(), "Registration failed, after retries");
 		return STATUS_REG_FAILED;
 	}
 	
 	/* drop current haggle connection? */
 	public synchronized void finiHaggle() {
 		if (hh != null) {
 			hh.eventLoopStop();
 			hh.dispose();
 			hh = null;
 		}
 	}
 	
 	public int shutdownHaggle() {
 		if (hh != null) {
 			return hh.shutdown();
 		}
 		return 1;
 	}
 	
 	@Override
 	public void onInterestListUpdate(Attribute[] arr) {
 		Log.d(getClass().getSimpleName() + ":onInterestListUpdate", "got new interests, size: " +arr.length);
 		for (Attribute a : arr) {
 			Log.d(getClass().getSimpleName(), "Attr: " + a.getName() + ", value:" + a.getValue());
 		}
 	}
 
 	@Override
 	public void onNeighborUpdate(Node[] neighbors) {
 		Log.d(getClass().getSimpleName(), "");
 		for (Node n : neighbors) {
 			Log.d(getClass().getSimpleName(), "NeighborUpdate: " + n.getName());
 		}
 	}
 
 	@Override
 	public void onNewDataObject(DataObject dObj) {
 		if (dObj == null) {
 			Log.e(getClass().getSimpleName() + ":onNewDataObject", "dObj null");
 		}
 		
 		Log.d(getClass().getSimpleName() + ":onNewDataObject", "Got new data!");
 		
 		Attribute[] all = dObj.getAttributes();
 		
 		if (all != null) {
 			for (Attribute a : all) {
 				Log.d(getClass().getSimpleName() + ":onNewDataObject", a.getName() + ", " + a.getValue());
 			}
 		} else {
 			Log.e(getClass().getSimpleName() + "onNewDataObject", "ALL IS NULL");
 		}
 		
 		if (dObj.getAttribute("Picture", 0) == null) {
 			Log.d(getClass().getSimpleName() + ":onNewDataObject", "no picture!");
 		}
 	}
 
 	@Override
 	public void onShutdown(int reason) {
 		Log.e(getClass().getSimpleName(), "Haggle Shutdown, reason: " + reason);
 		
 	}
 
 	//@Override
 	public ArrayList<POIObject> getAllObjects() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	//@Override
 	public POIObject getPOIObject(int id) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 	
 	public ArrayList<String> getAllObjectNames(){
 		ArrayList<String> result = new ArrayList<String>();
 		ArrayList<POIObject> collection = getAllObjects();
 		
 		if (getAllObjects() == null) {
 			Log.d(getClass().getSimpleName(), "getAllObjects returned null");
 			return null;
 		} 
 		for (POIObject p : collection) {
 			result.add(p.getName());
 		}
 		return result;
 	}
 	
 	public POIObject getPOIObjectByName(String name){
 		return null;
 	}
 
 	//@Override
 	public int pushPOIObject(POIObject o) {
 		try {
 			DataObject dObj = new DataObject(o.getPicPath());
 			
 			//bygga bitmap, kra en output mot haggleobj
 			//stta thumbnail
 			dObj.addAttribute("Time", Long.toString(System.currentTimeMillis()), 1); //making haggleObj unique.
 		
 			dObj.addAttribute("Type", Integer.toString(o.getType()), 1);
 			dObj.addAttribute("Name", o.getName(), 1);
 			dObj.addAttribute("Desc", o.getDescription(), 1);
 			dObj.addAttribute("Rating", Double.toString(o.getRating()), 1);
 			dObj.addAttribute("Latitude", Integer.toString(o.getPoint().getLatitudeE6()), 1);
 			dObj.addAttribute("Longitude", Integer.toString(o.getPoint().getLongitudeE6()), 1);
 			
 			getHaggleHandle().publishDataObject(dObj);
 			
 		} catch (DataObjectException e) {
 			Log.e(getClass().getSimpleName(), "Could not create object for: " + o.getName());
			Log.e(getClass().getSimpleName(), e.getMessage());
 		}
 		
 		return 0;
 	}
 }
