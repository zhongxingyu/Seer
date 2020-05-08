 package teamwork.goodVibrations;
 
 import teamwork.goodVibrations.persistence.PersistentStorage;
 import teamwork.goodVibrations.triggers.*;
 import teamwork.goodVibrations.functions.*;
 
 import android.app.*;
 import android.content.Context;
 import android.content.Intent;
 import android.media.AudioManager;
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.IBinder;
 import android.util.Log;
 
 public class GoodVibrationsService extends Service
 {
   private static String TAG = "GoodVibrationsService";
 
   private volatile TriggerQueue triggers; // Queue that holds all of the
                                           // triggers
   private volatile FunctionList functions; // List that holds all of the
                                            // functions
   private int maxFunctionID = 0;
   private int maxTriggerID = 0;
 
   private SettingsChanger changer;
   
   public static Context c;
   
   private class SettingsChanger extends Thread
   {
     public void run()
     {
       Trigger t = null;
       while(!Thread.currentThread().isInterrupted())
       {
         try
         {
           synchronized(triggers)
           {
             t = triggers.getNextTrigger();
           }
           if(t != null) // Make sure we have a trigger to process
           {
             // Sleep for the time until the trigger will execute
             Log.d(TAG, "Sleeping for " + t.getSleepTime());
             Thread.sleep(t.getSleepTime());
             // Execute all of the functions for this trigger
             if(t.canExecute())
             {
               Log.d(TAG, "Executing trigger: " + t.id + "  " + t.name);
               synchronized(triggers)
               {
                 for(Integer fID : t.getFunctions())
                 {
                   functions.get(fID.intValue()).execute();
                 }
                 triggers.switchState(t.id);
               }
             }
           }
           else
           // t is null because no triggers are in system
           {
             try
             {
               Log.d(TAG, "No triggers Sleeping for 10000");
               Thread.sleep(10000);
             }
             catch(InterruptedException e)
             {
               Log.d(TAG, "Sleep while no triggers interrupted");
              synchronized(this)
              {
                this.wait();
              }
             }
           }
         }
         catch(InterruptedException e)
         {
           Log.d(TAG, "Sleep while waiting for trigger was interrupted");
         }
       } // End While
     } // End run()
   }
 
   @Override
   public void onCreate()
   {
     Log.d(TAG, "Calling onCreate()");
     
     c = getApplicationContext();
     
 
 
     //triggers = new TriggerQueue();
     //functions = new FunctionList();
     
     functions = new FunctionList(PersistentStorage.loadFunctions());
     triggers = new TriggerQueue(PersistentStorage.loadTriggers());
 
 
     // Only samples, need to be removed
     Bundle b = new Bundle();
     b.putInt(Constants.INTENT_KEY_VOLUME, 0);
     b.putBoolean(Constants.INTENT_KEY_VIBRATE, true);
     b.putString(Constants.INTENT_KEY_NAME, "Volume 0");
     b.putByte(Constants.INTENT_KEY_VOLUME_TYPES, (byte)1);
     functions.add(new SetVolumeFunction(b, maxFunctionID++));
     b.putInt(Constants.INTENT_KEY_VOLUME, 100);
     b.putBoolean(Constants.INTENT_KEY_VIBRATE, true);
     b.putString(Constants.INTENT_KEY_NAME, "Volume 7");
     b.putByte(Constants.INTENT_KEY_VOLUME_TYPES, (byte)1);
     functions.add(new SetVolumeFunction(b, maxFunctionID++));
 
     Log.d(TAG, "Added Function");
 
     changer = new SettingsChanger();
     changer.start();
 
     Log.d(TAG, "Settings Changer Started");
 
     Log.d(TAG, "Finished onCreate");
   }
 
   @Override
   public int onStartCommand(Intent intent, int flags, int startId)
   {
     Log.d(TAG, "Starting onStartCommand");
     
     
     Log.d(TAG,"MANUALLY MAKING TRIGGER");
 //    TimeTrigger tt = new TimeTrigger("name@12@1#2#3#@4#5#6#@1234567890@2345678901@28");
 //    LocationTrigger tt = new LocationTrigger(getApplicationContext(),"name@13@1#2#3#@4#5#6#@-41.324@713.234@40.2");
     Log.d(TAG,"DONE MANUALLY MAKING TRIGGER");
     
 
     Bundle b = intent.getExtras();
     final int intentType = b.getInt(Constants.INTENT_TYPE);
     final int type = b.getInt(Constants.INTENT_KEY_TYPE);
 
     Log.d(TAG, "Bundle Created");
 
     if(intentType == Constants.FUNCTION_TYPE)
     {
       switch(type)
       {
       // Add a new volume function
         case Constants.FUNCTION_TYPE_VOLUME:
           functions.add(new SetVolumeFunction(b, maxFunctionID++));
           break;
 
         // Add a new ring tone function
         case Constants.FUNCTION_TYPE_RINGTONE:
           Log.d(TAG, "New Ringtone Function");
           functions.add(new RingtoneFunction(b, maxFunctionID++));
           break;
           
         case Constants.FUNCTION_TYPE_WALLPAPER:
           Log.d(TAG, "New Wallpaper Function");
           functions.add(new WallpaperFunction(b, maxFunctionID++));
           break;
 
         default:
           Log.d(TAG, "Default Function");
           break;
       }
       PersistentStorage.saveFunctions(functions.functions);
     }
     else if(intentType == Constants.TRIGGER_TYPE)
     {
       Trigger t = null;
       switch(type)
       {
         case Constants.TRIGGER_TYPE_TIME:
           t = new TimeTrigger(b, maxTriggerID++);
           break;
 
         case Constants.TRIGGER_TYPE_LOCATION:
           t = new LocationTrigger(this, b, maxTriggerID++);
           break;
 
         default:
           // Should never happen
       }
 
       Log.d(TAG, "Submitting TimeTrigger To queue");
       // Submit the trigger into the queue
       changer.interrupt();
       synchronized(triggers)
       {
         triggers.add(t);
       }
 
       // Restart the settings changer
       SettingsChanger.interrupted();
      synchronized(changer)
      {
        changer.notify();
      }
       
       PersistentStorage.saveTriggers(triggers.getTriggers());
       Log.d(TAG, "Trigger submitted");
     }
     else if(intentType == Constants.GET_DATA)
     {
       Intent i;
       switch(type)
       {
         case Constants.INTENT_KEY_FUNCTION_LIST:
           i = new Intent(Constants.SERVICE_DATA_FUNCTION_MESSAGE);
           i.putExtra(Constants.INTENT_KEY_NAME, Constants.INTENT_KEY_FUNCTION_LIST);
           i.putExtra(Constants.INTENT_KEY_DATA_LENGTH, functions.size());
           i.putExtra(Constants.INTENT_KEY_FUNCTION_NAMES, functions.getNames());
           i.putExtra(Constants.INTENT_KEY_FUNCTION_IDS, functions.getIDs());
           sendBroadcast(i);
           Log.d(TAG, "GET FUNCTION LIST");
           break;
 
         case Constants.INTENT_KEY_TRIGGER_LIST:
           i = new Intent(Constants.SERVICE_DATA_TRIGGER_MESSAGE);
           i.putExtra(Constants.INTENT_KEY_NAME, Constants.INTENT_KEY_TRIGGER_LIST);
           i.putExtra(Constants.INTENT_KEY_DATA_LENGTH, triggers.size());
           Log.d(TAG, "NT: " + triggers.size());
           i.putExtra(Constants.INTENT_KEY_TRIGGER_NAMES, triggers.getNames());
           i.putExtra(Constants.INTENT_KEY_TRIGGER_IDS, triggers.getIDs());
           sendBroadcast(i);
           Log.d(TAG, "GET TRIGGER LIST");
           break;
       }
     }
 
     /*
      * // TODO Remove the following block. It is for testing only
      * if(functions.size() == 2) { Log.d(TAG,"Adding Trigger"); // Making a
      * trigger // TODO Build trigger from parsed message
      * 
      * LocationManager LM = (LocationManager)
      * getSystemService(Context.LOCATION_SERVICE); Criteria criteria = new
      * Criteria(); String bestProvider = LM.getBestProvider(criteria, false);
      * 
      * Location l = new Location(bestProvider);
      * 
      * l.setLatitude(0); l.setLongitude(0);
      * 
      * b.putParcelable(Constants.INTENT_KEY_LOCATION, l);
      * b.putFloat(Constants.INTENT_KEY_RADIUS, (float)10.0);
      * 
      * LocationTrigger t = new LocationTrigger(getApplicationContext(),b);
      * t.addFunction(LocationTrigger.ENTERFUNCTION, new Integer(0));
      * t.addFunction(LocationTrigger.EXITFUNCTION, new Integer(1));
      * 
      * msg.obj = t; msg.arg2 = Constants.TRIGGER_TYPE;
      * mServiceHandler.sendMessage(msg); }
      */
     /*
      * else { TimeTrigger t = new TimeTrigger(15000,20000,(byte)127);
      * t.addFunction(TimeTrigger.STATE.ACTIVE, new Integer(2));
      * t.addFunction(TimeTrigger.STATE.INACTIVE, new Integer(3)); msg.obj = t;
      * //TODO: fix this //msg.obj = new NULLFunction(); }
      */
 
     Log.d(TAG, "onStartCommand() Finished");
 
     // If we get killed, after returning from here, restart
     return START_STICKY;
   }
 
   @Override
   public IBinder onBind(Intent arg0)
   {
     return null;
   }
 
 }
