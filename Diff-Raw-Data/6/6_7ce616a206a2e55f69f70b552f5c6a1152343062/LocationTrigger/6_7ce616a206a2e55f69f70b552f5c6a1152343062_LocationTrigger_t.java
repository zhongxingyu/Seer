 package teamwork.goodVibrations.triggers;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import teamwork.goodVibrations.Constants;
import teamwork.goodVibrations.GoodVibrationsService;
 import android.content.Context;
 import android.location.Criteria;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.util.Log;
 
 public class LocationTrigger extends Trigger
 {
 
   private static String TAG = "LocationTrigger";
   private boolean isInside;
   private LocationManager LM;
   private List<String> providers;
   private Location myLocation;
   private Location center;
   private float radius;
   private ArrayList<Integer> enterFunctionIDs; // The functions that will be
                                                // executed on start
   private ArrayList<Integer> exitFunctionIDs; // The functions that will be
                                               // executed on stop
   private Criteria criteria;
   private String bestProvider;
 
   public static boolean ENTERFUNCTION = true;
   public static boolean EXITFUNCTION = false;
   GPSLocationListener listener;
 
   // LocationTrigger
   // Constructor for creating a location trigger through the GUI
   public LocationTrigger(Context c, Bundle b, int newID)
   {
     initLocationTrigger(c);
     
     isInside = false;
     name = b.getString(Constants.INTENT_KEY_NAME);
     id = newID;
     type = Trigger.TriggerType.LOCATION;
 
     Log.d(TAG, "Made Location Manager");
 
     int[] enterIDs = b.getIntArray(Constants.INTENT_KEY_FUNCTION_IDS);
     int[] exitIDs = {1}; // Hardcoded for Product stakeholder review 1
     for(int i = 0; i < enterIDs.length; i++)
     {
       enterFunctionIDs.add(new Integer(enterIDs[i]));
     }
     for(int i = 0; i < exitIDs.length; i++)
     {
       exitFunctionIDs.add(new Integer(exitIDs[i]));
     }
 
     // radius = b.getFloat(Constants.INTENT_KEY_RADIUS);
     // Constant value of 50 for radius
     radius = 50;
     Location l = new Location("");
     l.setLatitude(b.getDouble(Constants.INTENT_KEY_LATITUDE));
     l.setLongitude(b.getDouble(Constants.INTENT_KEY_LONGITUDE));
     center = l;
   }
   
   // LocationTrigger
   // Constructor for creating location trigger from the persistent storage
  public LocationTrigger(String s)
   {
    Context c = GoodVibrationsService.c;
     initLocationTrigger(c);
     isInside = false;
     
     String[] categories = s.split(Constants.CATEGORY_DELIM);
     name = categories[0];
     id = new Integer(categories[1]).intValue();
     String[] enterIDsString = categories[2].split(Constants.LIST_DELIM);
     for(String stringID : enterIDsString)
     {
       enterFunctionIDs.add(new Integer(stringID).intValue());
     }
     String[] exitIDsString = categories[3].split(Constants.LIST_DELIM);
     for(String stringID : exitIDsString)
     {
       exitFunctionIDs.add(new Integer(stringID).intValue());
     }
     
     Location l = new Location("");
     l.setLatitude(new Double(categories[4]));
     l.setLongitude(new Double(categories[5]));
     center = l;
     
     radius = new Float(categories[6]);
     
     Log.d(TAG,name);
     Log.d(TAG, new Integer(id).toString());
     Log.d(TAG,enterFunctionIDs.toString());
     Log.d(TAG,exitFunctionIDs.toString());
     Log.d(TAG,l.toString());
     Log.d(TAG,new Float(radius).toString());
   }
   
   // initLocationTrigger
   // Called by both constructors to get the GPS started and
   // some variables initialized
   private void initLocationTrigger(Context c)
   {
     type = Trigger.TriggerType.LOCATION;
     // Get the location manager
     LM = (LocationManager) c.getSystemService(Context.LOCATION_SERVICE);
     // List all providers:Log
     providers = new ArrayList<String>();
     providers = LM.getAllProviders();
 
     Log.d(TAG, "Got providers");
     Log.d(TAG, "PROVIDERS:" + providers);
     criteria = new Criteria();
     criteria.setAccuracy(Criteria.ACCURACY_FINE);
     bestProvider = LM.getBestProvider(criteria, true);
     myLocation = new Location(bestProvider);
     myLocation = LM.getLastKnownLocation(bestProvider);
 
     // Define a listener that responds to location updates
     listener = new GPSLocationListener();
     Log.d(TAG, "BEST: " + bestProvider);
     LM.requestLocationUpdates(bestProvider, 10000, 0, listener);
 
     // Location recievedLocation = new Location(bestProvider);
     // recievedLocation.setLatitude(0);
     // recievedLocation.setLongitude(0);
 
     Log.d(TAG, "Got location" + myLocation);
     Log.d(TAG, "Proider: " + bestProvider);
     
     enterFunctionIDs = new ArrayList<Integer>();
     exitFunctionIDs = new ArrayList<Integer>();
   }
 
   // removeFunction
   // Removes a function that is called by this location trigger
   public void removeFunction(Integer id)
   {
     // TODO Auto-generated method stub
   }
 
   // getSleepTime
   // Returns how long the location trigger sleeps before being checked again
   public long getSleepTime()
   {
     // TODO Should sleep time depend on the last time the trigger was called?
     
     // Check location every 5 minutes
     // return 300000;
 
     // Check location every 10 seconds
     return 60000;
   }
 
   // getFunctions
   // Retuns the list of functions that should be executed depending
   // current location
   public ArrayList<Integer> getFunctions()
   {
     if(myLocation.distanceTo(center) > radius)
     {
       return exitFunctionIDs;
     }
     else
     {
       return enterFunctionIDs;
     }
   }
 
   // switchState
   // Not needed?
   public void switchState()
   {
     // TODO Auto-generated method stub
   }
 
   // canExecute
   // Determines if the trigger should execute.  Based on the last exectued
   // location and the current location
   public boolean canExecute()
   {
     Log.d(TAG, "canExecute()");
     if(myLocation != null)
     {
       // Get new location and calculate distance to target
       Log.d(TAG, "LAT/LON " + myLocation.getLatitude() + "/" + myLocation.getLongitude());
       double dist = myLocation.distanceTo(center);
       boolean isNowInside = (dist < radius);
       Log.d(TAG, "center: " + center);
       Log.d(TAG, "dist: " + dist + " Radius: " + radius);
       Log.d(TAG, "IsnowInside: " + isNowInside);
 
       if(isNowInside != isInside)
       {
         isInside = isNowInside;
         return true;
       }
       else
       {
         return false;
       }
 
     }
     else
     {
       return false;
     }
   }
 
   // addFunction
   // Adds a functionID to either the start or stop list
   public boolean addFunction(boolean type, Integer f)
   {
     if(type == ENTERFUNCTION)
     {
       enterFunctionIDs.add(f);
     }
     else if(type == EXITFUNCTION)
     {    
       exitFunctionIDs.add(f);
     }
     return true;
   }
 
   // This class changes the local location variable whenever the 
   // phones location is updated
   private class GPSLocationListener implements LocationListener
   {
     public void onLocationChanged(Location location)
     {
       Log.d(TAG, "Location Listener Called");
       if(location != null)
       {
         myLocation = location;
       }
     }
 
     public void onProviderDisabled(String arg0)
     {}
 
     public void onProviderEnabled(String provider)
     {}
 
     public void onStatusChanged(String provider, int status, Bundle extras)
     {}
   }
 
   // getInternalSaveString
   // Builds the string to save the location trigger
   @Override
   String getInternalSaveString()
   {
     // name
     // id
     // enterFunctionIDs
     // exitFunctionIDs
     // radius
     // center
     
     String saveString;
     saveString = name + Constants.CATEGORY_DELIM;
     saveString += id  + Constants.CATEGORY_DELIM;
     
     // Save the start function ids
     for(Integer i : enterFunctionIDs)
     {
       saveString += i.toString() + Constants.LIST_DELIM;
     }
     saveString += Constants.CATEGORY_DELIM;
     
     // Save the stop function ids
     for(Integer i : exitFunctionIDs)
     {
       saveString += i.toString() + Constants.LIST_DELIM;
     }
     saveString += Constants.CATEGORY_DELIM;
     
     saveString += new Double(center.getLatitude()).toString();
     saveString += Constants.CATEGORY_DELIM;
     saveString += new Double(center.getLongitude()).toString();
     saveString += Constants.CATEGORY_DELIM;
     saveString += new Float(radius).toString();
     saveString += Constants.CATEGORY_DELIM;
     
     return saveString;
   }
 }
