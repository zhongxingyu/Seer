 package fi.mikuz.boarder.service;
 
 import android.content.Intent;
 import android.os.IBinder;
 import android.util.Log;
import fi.mikuz.boarder.app.BoarderService;
 import fi.mikuz.boarder.util.SoundPlayerControl;
 
 /**
  * 
  * @author Jan Mikael Lindlf
  */
public class TogglePlayPauseService extends BoarderService {
 	public static final String TAG = "TogglePlayPauseService";
 	
     @Override
     public void onCreate() {
    	super.onCreate();
    	
     	try {
     		SoundPlayerControl.togglePlayPause();
     	} catch (NullPointerException e) {
     		Log.w(TAG, "Boarder is not running, nothing to do", e);
     	}
 		this.stopSelf();
     }
 
 	@Override
 	public IBinder onBind(Intent intent) {
 		return null;
 	}
 
 }
