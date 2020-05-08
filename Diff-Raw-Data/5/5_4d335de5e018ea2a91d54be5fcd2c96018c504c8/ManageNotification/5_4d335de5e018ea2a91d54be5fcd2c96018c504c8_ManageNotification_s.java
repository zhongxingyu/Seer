 package net.anei.cadpage;
 
 import java.util.ArrayList;
 
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Color;
 import android.media.AudioManager;
 import android.media.MediaPlayer;
 import android.net.Uri;
 import android.text.SpannableString;
 
 /*
  * This class handles the Notifications (sounds/vibrate/LED)
  */
 public class ManageNotification {
 
   private static final int NOTIFICATION_ALERT = 1337;
   
   private static MediaPlayer mMediaPlayer = null;
 
   /*
    * Class to hold the popup notification elements
    */
   static class PopupNotification {
     public Notification notification;
     public boolean privacyMode;
 
     PopupNotification(Notification n) {
       this.notification = n;
     }
 
     final public void notify(Context context, int notif) {
       NotificationManager myNM =
         (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
 
       // Seems this is needed for the number value to take effect on the Notification
       myNM.cancel(NOTIFICATION_ALERT);
 
       if (Log.DEBUG) Log.v("*** Notify running ***");
       myNM.notify(notif, notification);
     }
   }
 
   /*
    * The main notify method
    */
   public static boolean show(Context context, SmsMmsMessage message) {
     return show(context, message, true);
   }
   
   
   public static boolean show(Context context, SmsMmsMessage message, boolean reminder) {
 
     // Fetch info from the message object
     String call = message.getCall();
     long timestamp = message.getTimestamp();
 
     PopupNotification n = buildNotification(context);
 
     if (n == null) return false;
 
     // The notification title, sub-text and text that will scroll
     String contentTitle;
     String contentText;
     SpannableString scrollText;
 
     // If we're in privacy mode and the keyguard is on then just display
     // the name of the person, otherwise scroll the name and message
     if (n.privacyMode && ManageKeyguard.inKeyguardRestrictedInputMode()) {
       scrollText =
         new SpannableString(context.getString(R.string.notification_scroll_privacy));
     } else {
       scrollText =
         new SpannableString(context.getString(R.string.notification_scroll, call));
     }
 
     // The default intent when the notification is clicked (Inbox)
     Intent smsIntent = CallHistoryActivity.getLaunchIntent(context);
 
     contentTitle = "Alert";
     contentText = call;
 
     /*
      * Ok, let's create our Notification object and set up all its parameters.
      */
 
     // Set the icon, scrolling text and timestamp
     n.notification.icon = R.drawable.stat_notify_sms;
     n.notification.tickerText = scrollText;
     n.notification.when = timestamp;
 
     // Set the PendingIntent if the status message is clicked
     PendingIntent notifIntent = PendingIntent.getActivity(context, 0, smsIntent, 0);
 
     // Set the messages that show when the status bar is pulled down
     n.notification.setLatestEventInfo(context, contentTitle, contentText, notifIntent);
     n.notify(context, NOTIFICATION_ALERT);
 
     // Schedule a reminder notification
     if (reminder) ReminderReceiver.scheduleReminder(context, message);
     return true;
   }
 
   /*
    * Build the notification from user preferences
    */
   private static PopupNotification buildNotification(Context context) {
 
     AudioManager AM = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
     Uri alarmSoundURI;
     
     // Check if notifications are enabled - if not, we're done :)
     if (!ManagePreferences.notifyEnabled() && !ManagePreferences.notifyOverride()) {
       return null;
     }
 
     // Get some preferences: vibrate and vibrate_pattern prefs
     boolean vibrate = ManagePreferences.vibrate();
     String vibrate_pattern_raw = ManagePreferences.vibratePattern();
 
     // Get LED preferences
     boolean flashLed = ManagePreferences.flashLED();
     String flashLedCol = ManagePreferences.flashLEDColor();
     String flashLedPattern = ManagePreferences.flashLEDPattern();
 
     // Try and parse the user ringtone, use the default if it fails
     // If Notifications are On then get ring tone setting
     //if (mPrefs.getBoolean(R.string.pref_notif_enabled_key,Defaults.PREFS_NOTIF_ENABLED,SmsPopupDbAdapter.KEY_ENABLED_NUM)) {
      alarmSoundURI = Uri.parse(ManagePreferences.notifySound());
    //Uri alarmSoundURI=Uri.parse("file:///sdcard/media/audio/notifications/generalquarter.wav");
     if (Log.DEBUG){ Log.v("Sounds URI = " + alarmSoundURI.toString());}
     //}
     // See if user wants some privacy
     boolean privacyMode = ManagePreferences.privacyMode();
 
     /*
      * Ok, let's create our Notification object and set up all its parameters.
      */
     Notification notification = new Notification();
 
     // Set auto-cancel flag
     notification.flags = Notification.FLAG_AUTO_CANCEL;
 
     // Set audio stream to ring
     notification.audioStreamType = Notification.STREAM_DEFAULT;
 
     // Set up LED pattern and color
     if (flashLed) {
       notification.flags |= Notification.FLAG_SHOW_LIGHTS;
 
       /*
        * Set up LED blinking pattern
        */
       int[] led_pattern = null;
 
       if (context.getString(R.string.pref_custom_val).equals(flashLedPattern)) {
         led_pattern = parseLEDPattern(ManagePreferences.flashLEDPatternCustom());
       } else {
         led_pattern = parseLEDPattern(flashLedPattern);
       }
 
 
       // Set to default if there was a problem
       if (led_pattern == null) {
         led_pattern =
           parseLEDPattern(context.getString(R.string.pref_flashled_pattern_default));
       }
       
       /*
        * Set up LED color
        */
       // Check if a custom color is set
       if (context.getString(R.string.pref_custom_val).equals(flashLedCol)) {
         flashLedCol = ManagePreferences.flashLEDColorCustom();
       }
 
       notification.ledOnMS = led_pattern[0];
       notification.ledOffMS = led_pattern[1];
 
       // Default in case the parse fails
       int col = Color.parseColor(context.getString(R.string.pref_flashled_color_default));
 
       // Try and parse the color
       if (flashLedCol != null) {
         try {
           col = Color.parseColor(flashLedCol);
         } catch (IllegalArgumentException e) {
           // No need to do anything here
         }
       }
 
       notification.ledARGB = col;
     }
 
     /*
      * Set up vibrate pattern
      */
     // If vibrate is ON, or if phone is set to vibrate
     if ((vibrate || AudioManager.RINGER_MODE_VIBRATE == AM.getRingerMode())) {
       long[] vibrate_pattern = null;
       if (context.getString(R.string.pref_custom_val).equals(vibrate_pattern_raw)) {
         vibrate_pattern = parseVibratePattern(ManagePreferences.vibratePatternCustom());
       } else {
         vibrate_pattern = parseVibratePattern(vibrate_pattern_raw);
       }
      if (vibrate_pattern != null) {
         notification.vibrate = vibrate_pattern;
       } else {
         notification.defaults = Notification.DEFAULT_VIBRATE;
       }
     }
 
     if ( ManagePreferences.notifyOverride() || ManagePreferences.notifyEnabled()) { 
       if (ManagePreferences.notifyOverride()) {
         if (Log.DEBUG){ Log.v("OVERRIDE ON: running own mediaplayer");}
        alarmSoundURI=Uri.parse("file:///sdcard/media/audio/notifications/generalquarter.wav");
        mMediaPlayer = MediaPlayer.create(context, alarmSoundURI);
         mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
         mMediaPlayer.setLooping(false);
         mMediaPlayer.setVolume(1, 1);
         if (Log.DEBUG){ Log.v("Starting Media Player Sound");}
         mMediaPlayer.start();
       } else {	 
         notification.sound = alarmSoundURI;
       }
     }
  
     // Set intent to execute if the "clear all" notifications button is pressed -
     // basically stop any future reminders.
     Intent deleteIntent = new Intent(new Intent(context, ReminderReceiver.class));
     deleteIntent.setAction(Intent.ACTION_DELETE);
     PendingIntent pendingDeleteIntent = PendingIntent.getBroadcast(context, 0, deleteIntent, 0);
 
     notification.deleteIntent = pendingDeleteIntent;
 
     PopupNotification popupNotification = new PopupNotification(notification);
     popupNotification.privacyMode = privacyMode;
     return popupNotification ;
     
   }
 
   // Clear a single notification
   public static void clear(Context context) {
 
     // Clear any pending reminders
     ReminderReceiver.cancelReminder(context);
 
     NotificationManager myNM =
       (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
     myNM.cancel(NOTIFICATION_ALERT);
     
     // Stop media player if running
     if (mMediaPlayer != null) mMediaPlayer.stop();
   }
 
   /**
    * Parse the user provided custom vibrate pattern into a long[]
    * 
    */
   // TODO: tidy this up
   public static long[] parseVibratePattern(String stringPattern) {
     ArrayList<Long> arrayListPattern = new ArrayList<Long>();
     Long l;
 
     if (stringPattern == null) return null;
 
     String[] splitPattern = stringPattern.split(",");
     int VIBRATE_PATTERN_MAX_SECONDS = 60000;
     int VIBRATE_PATTERN_MAX_PATTERN = 100;
 
     for (int i = 0; i < splitPattern.length; i++) {
       try {
         l = Long.parseLong(splitPattern[i].trim());
       } catch (NumberFormatException e) {
         return null;
       }
       if (l > VIBRATE_PATTERN_MAX_SECONDS) {
         return null;
       }
       arrayListPattern.add(l);
     }
 
     // TODO: can i just cast the whole ArrayList into long[]?
     int size = arrayListPattern.size();
     if (size > 0 && size < VIBRATE_PATTERN_MAX_PATTERN) {
       long[] pattern = new long[size];
       for (int i = 0; i < pattern.length; i++) {
         pattern[i] = arrayListPattern.get(i);
       }
       return pattern;
     }
 
     return null;
   }
 
   /**
    * Parse LED pattern string into int[]
    * 
    * @param stringPattern
    * @return
    */
   public static int[] parseLEDPattern(String stringPattern) {
     int[] arrayPattern = new int[2];
     int on, off;
 
     if (stringPattern == null) return null;
 
     String[] splitPattern = stringPattern.split(",");
 
     if (splitPattern.length != 2) return null;
 
     int LED_PATTERN_MIN_SECONDS = 0;
     int LED_PATTERN_MAX_SECONDS = 60000;
 
     try {
       on = Integer.parseInt(splitPattern[0]);
     } catch (NumberFormatException e) {
       return null;
     }
 
     try {
       off = Integer.parseInt(splitPattern[1]);
     } catch (NumberFormatException e) {
       return null;
     }
 
     if (on >= LED_PATTERN_MIN_SECONDS && on <= LED_PATTERN_MAX_SECONDS
         && off >= LED_PATTERN_MIN_SECONDS && off <= LED_PATTERN_MAX_SECONDS) {
       arrayPattern[0] = on;
       arrayPattern[1] = off;
       return arrayPattern;
     }
 
     return null;
   }
 }
