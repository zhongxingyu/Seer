 package edu.usf.eng.pie.avatars4change.wallpaper;
 
 import java.util.TimeZone;
 
 import edu.usf.eng.pie.avatars4change.avatar.Avatar;
 import edu.usf.eng.pie.avatars4change.dataInterface.activityMonitor;
 import edu.usf.eng.pie.avatars4change.dataInterface.userData;
 import edu.usf.eng.pie.avatars4change.notifier.Notifier;
 
 import android.content.Context;
 import android.os.SystemClock;
 import android.text.format.Time;
 import android.util.Log;
 
 public class sceneBehaviors {
 	private static final String TAG = "sceneBehavior";
 	public static final String[] behaviors = {
 		"constant",
 		"Proteus Effect Study",
 		"IEEE VR demo"
 	};
     private static boolean   activeOnEvens       = true;	//active on even days?
     
     public static boolean getActiveOnEvens(){
     	return activeOnEvens;
     }
 
     public static void setActiveOnEvens(final boolean val){	
         activeOnEvens = val;
         avatarWallpaper.theAvatar.lastActivityChange = -avatarWallpaper.theAvatar.lastActivityChange;// triggers a scene update 
     }
 	
     //this method gets a behavior from the Avatar's behavior string (which has been set in the settings)
     public static void runBehavior(Context context, Avatar theAvatar){
 		long now = SystemClock.elapsedRealtime();
 		long timeTillWarning = 1000 * 60 * 60 * 24;		// time until app posts a data failure notification
 		long timeTillReminder= 1000 * 60 * 60 * 2;		// time until avatar tries to get user attention
 		long timeSinceLog = now - avatarWallpaper.lastLogTime;
         if( timeSinceLog > timeTillWarning){   
         	Notifier.addNotification(context,"no view data in past 24hrs; contact PIE-Lab staff.");
         } else if (timeSinceLog > timeTillReminder){
     		if(!theAvatar.isAsleep()){
     			Notifier.addNotification(context, theAvatar.getRandomMessage());
     		}
         }
 		//Log.d(TAG,"timeTilDataErr="+Long.toString(timeSinceLog));
         if( timeSinceLog > timeTillWarning){   
         	Notifier.addNotification(context,"no view data in past 24hrs; contact PIE-Lab staff.");
         }
     	if( theAvatar.behaviorSelectorMethod == null){
         	Log.e(TAG,"behaviorSelectorMethod = null; cannot run Behavior");
     		return;
     	}	//implied ELSE
     	; //Log.d(TAG,"updating scene via " + theAvatar.behaviorSelectorMethod);
     	if ( theAvatar.behaviorSelectorMethod.equalsIgnoreCase("constant") ){
     		constant(theAvatar);
     	}else if( theAvatar.behaviorSelectorMethod.equalsIgnoreCase("Proteus Effect Study")){
     		proteusStudy(theAvatar);
     	}else if( theAvatar.behaviorSelectorMethod.equalsIgnoreCase("IEEE VR demo")){
     		VRDemo(theAvatar,context);
     	}else{
     		Log.e(TAG, "unrecognized scene behavior " + theAvatar.behaviorSelectorMethod);
     		debug(theAvatar);	//default method
     	}
     }
     
     // avatar behavior does not change; it stays constant as it has been set
     private static void constant(Avatar theAvatar){
     	//do nothing to update the behavior, it stays your choice
     }
     
     //
     private static String getDesiredProteusLevel(Avatar theAvatar){
         if(theAvatar.isAsleep()){
         	return"sleeping";
         } else {	//awake
         	int today = Time.getJulianDay(System.currentTimeMillis(), (long) (TimeZone.getDefault().getRawOffset()/1000.0) ); 	//(new Time()).toMillis(false)
         	// Log.d(TAG,"time:"+System.currentTimeMillis()+"\ttimezone:"+TimeZone.getDefault().getRawOffset()+"\ttoday:"+today);
         	//set active or passive, depending on even or odd julian day
         	if(today%2 == 0){	//if today is even
         		if(activeOnEvens){
         			return"active";
         		}else{
         			return "passive";
         		}
         	}else{	//today is odd
         		if(!activeOnEvens){	//if active on odd days
         			return "active";
         		}else{
         			return "passive";
         		}
         	}
         }
     }
     
     // avatar behavior designed for use in the Proteus Effect study
 	private static void proteusStudy(Avatar theAvatar){		
 		avatarWallpaper.desiredFPS = 8;//update frameRate from PA level
 	    theAvatar.UPDATE_FREQUENCY = 60*60*1000;	//desired time between activity level updates [ms]
 		//check for enough time to change animation
     	//TODO: change this next if issue#5 persists
 		long now = SystemClock.elapsedRealtime();		//TODO: ensure that this works even if phone switched off. 
         if((now - theAvatar.lastActivityChange) > theAvatar.UPDATE_FREQUENCY || //if time elapsed > desired time
         		getDesiredProteusLevel(theAvatar)!=theAvatar.getActivityLevel()){ //OR if level is not what it should be
         	Log.v(TAG,"updating avatar activity");
         	
         	theAvatar.setActivityLevel(getDesiredProteusLevel(theAvatar));
         	
         	//avatar changes activity 
         	theAvatar.randomActivity(theAvatar.getActivityLevel());
        	 	theAvatar.lastActivityChange = now;
         }
         ; //Log.d(TAG,Long.toString(theAvatar.UPDATE_FREQUENCY-(now-theAvatar.lastActivityChange))+"ms to activity change");
 	}
 
 	// avatar behavior cycles through all behaviors in order on a short interval
 	private static void debug(Avatar theAvatar){
 		//TODO make this happen...
 		constant(theAvatar);
 	}
 
 	// avatar shows sedentary behavior for sitting, slow active behavior for walking, fast active behavior for running
 	private static void VRDemo(Avatar theAvatar,Context contx){
 		avatarWallpaper.desiredFPS = (int)Math.round( (Math.exp(userData.recentAvgActivityLevel))*0.05f + 1.0f );//update frameRate from PA level
 		String activLvl = userData.getPAlevelName();
 		if(! activLvl.equalsIgnoreCase(theAvatar.getActivityLevel())){	//if user level does not match avatar
 			theAvatar.setActivityLevel(activLvl);	//set new avatar level
 		}
 		if(fftBroken()){
 			fixFFT(contx);
 		}
 	}
 	private static boolean fftBroken(){
 	// Checks if the fft getter is still working and returns true if broken.
 	// This must be checked periodically b/c the sensor service has a bad habit of 
 	// stopping without warning or reason.
 	// Checking is performed using a simple timeout.
 		final int TIME_TILL_BROKE = 10000;	// time without change until fft is considered broken
 		long now = SystemClock.elapsedRealtime();		//TODO: ensure that this works even if phone switched off. 
         if((now - userData.lastFFTupdate) > TIME_TILL_BROKE){
         	return true;
         } else {
         	return false;
         }
 	}
 	private static void fixFFT(Context contx){
 	// attempts to fix a broken FFT accelerometer parser
 		Log.i(TAG,"FFT stalling. attempting to fix.");
 		activityMonitor.resetActivityMonitor(contx);
 	}
 }
