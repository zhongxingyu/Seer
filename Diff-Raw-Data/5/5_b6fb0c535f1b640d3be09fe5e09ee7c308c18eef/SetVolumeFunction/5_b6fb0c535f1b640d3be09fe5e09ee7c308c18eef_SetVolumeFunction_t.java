 package teamwork.goodVibrations.functions;
 
 import teamwork.goodVibrations.Constants;
 import teamwork.goodVibrations.GoodVibrationsService;
 import android.content.Context;
 import android.media.AudioManager;
 import android.os.Bundle;
 import android.util.Log;
 
 public class SetVolumeFunction extends Function
 {
 
   private static String TAG = "SetVolumeFunction";
 
   private AudioManager AM;
   private int volume;
   private boolean vibrate;
   private byte volumeTypes;
   
   // SetVolumeFunction
   // Constructor for making volume functions when added through GUI
   public SetVolumeFunction(Bundle b, int newID)
   {
     volume = b.getInt(Constants.INTENT_KEY_VOLUME);
     Log.d(TAG, "Volume: " + volume);
     vibrate = b.getBoolean(Constants.INTENT_KEY_VIBRATE);
     name = b.getString(Constants.INTENT_KEY_NAME);
     volumeTypes = b.getByte(Constants.INTENT_KEY_VOLUME_TYPES);
     AM = (AudioManager) GoodVibrationsService.c.getSystemService(Context.AUDIO_SERVICE);
     id = newID;
     type = Function.FunctionType.RING_VOLUME;
   }
   
   // SetVolumeFunction
   // Constructor for making volume functions when loaded from a persistent file
   public SetVolumeFunction(String s)
   {
     AM = (AudioManager) GoodVibrationsService.c.getSystemService(Context.AUDIO_SERVICE);
     String[] categories = s.split(Constants.CATEGORY_DELIM);
     name = categories[0];
     id = new Integer(categories[1]).intValue();
     volume = new Integer(categories[2]).intValue();
     vibrate = new Boolean(categories[3]).booleanValue();
     Log.d(TAG,"cat4: " + categories[4]);
     volumeTypes = new Byte(categories[4]).byteValue();
     type = Function.FunctionType.RING_VOLUME;
     
     Log.d(TAG,"NAME: " + name);
     Log.d(TAG,"ID: " + id);
     
   }
   
   public SetVolumeFunction(SetVolumeFunction f)
   {
     AM = (AudioManager) GoodVibrationsService.c.getSystemService(Context.AUDIO_SERVICE);
     
   }
 
   // execute
   // Does the actual changing of the volume
   @Override
   public SetVolumeFunction execute()
   {
     Log.d(TAG, "EXECUTING " + name);
     Log.d(TAG,"Volume " + volume);
     
     SetVolumeFunction inverse = getInverse();
     
     // If the volume will be up, we must set the ringer mode to normal
     if(volume > 0)
     {
       AM.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
     }
     //If the volume will be all the way down, and the vibrate button was checked, then set the mode to vibrate
     else if(volume == 0 && vibrate)
     {
       AM.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
     }
     // The volume was all the way down, and the vibrate button was not checked
     else  
     {
       AM.setRingerMode(AudioManager.RINGER_MODE_SILENT);
     }
     
     // A temporary variable to hold the flags for setStreamVolume
     byte flags = AudioManager.FLAG_SHOW_UI | AudioManager.FLAG_ALLOW_RINGER_MODES | AudioManager.FLAG_VIBRATE;
     
     // Set the ring volume
     if((volumeTypes & (byte)1) != 0)
     {
       Log.d(TAG,"RING" + scaleVolume(AudioManager.STREAM_RING));
       AM.setStreamVolume(AudioManager.STREAM_RING, scaleVolume(AudioManager.STREAM_RING), flags);
     }
     // Set the media volume
     if((volumeTypes & (byte)2) != 0)
     {
       Log.d(TAG,"MUSIC" + scaleVolume(AudioManager.STREAM_MUSIC));
       AM.setStreamVolume(AudioManager.STREAM_MUSIC, scaleVolume(AudioManager.STREAM_MUSIC), flags);
     }
     // Set the alarm volume
     if((volumeTypes & (byte)4) != 0)
     {
       Log.d(TAG,"ALARM" + scaleVolume(AudioManager.STREAM_ALARM));
       AM.setStreamVolume(AudioManager.STREAM_ALARM,scaleVolume(AudioManager.STREAM_ALARM),flags);
     }
     // Set the notification volume
     if((volumeTypes & (byte)8) != 0)
     {
       Log.d(TAG,"NOTIFICATION" + scaleVolume(AudioManager.STREAM_NOTIFICATION));
       AM.setStreamVolume(AudioManager.STREAM_NOTIFICATION,scaleVolume(AudioManager.STREAM_NOTIFICATION),flags);
     }
 
     // Set the vibrate setting appropriately
     if(vibrate)
     {
       AM.setVibrateSetting(AudioManager.STREAM_RING, AudioManager.VIBRATE_SETTING_ON);
     }
     else
     {
       AM.setVibrateSetting(AudioManager.STREAM_RING, AudioManager.VIBRATE_SETTING_OFF);
     }
 
     Log.d(TAG, "execute() - Setting to " + volume);
     
     return inverse;
   }
   
   private SetVolumeFunction getInverse()
   {
     Bundle b = new Bundle();
    float maxVol = (float) AM.getStreamMaxVolume(AudioManager.STREAM_RING);
    int currentVolume = Math.round((float)(AM.getStreamVolume(AudioManager.STREAM_RING)*100.0)/maxVol);
    Log.d(TAG, "CURRENTVOL: " + currentVolume);
     int currentVibrate = AM.getVibrateSetting(AudioManager.STREAM_RING);
     boolean currentVibrateBool = false;
     currentVibrateBool = (currentVibrate == AudioManager.VIBRATE_SETTING_ON);
     
     b.putInt(Constants.INTENT_KEY_VOLUME, currentVolume);
     b.putBoolean(Constants.INTENT_KEY_VIBRATE, currentVibrateBool);
     b.putString(Constants.INTENT_KEY_NAME, name+"inv");
     b.putByte(Constants.INTENT_KEY_VOLUME_TYPES,volumeTypes);
     
     SetVolumeFunction inverse = new SetVolumeFunction(b,-1*id);
     return inverse;
   }
   
   // scaleVolume
   // Takes the 0-100 value from the slider and rounds is to the nearest volume setting for the proper stream
   private int scaleVolume(int stream)
   {
     float maxVol = (float) AM.getStreamMaxVolume(stream);
     return (int) Math.round(maxVol * (volume / 100.0));
   }
 
   // getInternalSaveString
   // Builds the string that will be used to store this function
   @Override
   public String getInternalSaveString()
   {
     //name
     //id
     //volume
     //vibrate
     //volumeTypes
     
     String saveString = new String();
     saveString = name + Constants.CATEGORY_DELIM;
     saveString += id  + Constants.CATEGORY_DELIM;
     saveString += new Integer(volume).toString();
     saveString += Constants.CATEGORY_DELIM;
     saveString += new Boolean(vibrate).toString();
     saveString += Constants.CATEGORY_DELIM;
     saveString += new Byte(volumeTypes).toString();
     saveString += Constants.CATEGORY_DELIM;
     
     return saveString;
   }
 }
