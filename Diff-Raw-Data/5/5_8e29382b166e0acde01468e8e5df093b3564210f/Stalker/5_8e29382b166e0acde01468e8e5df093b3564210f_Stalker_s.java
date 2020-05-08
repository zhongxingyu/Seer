 /* *******************************************************************************
  * LOCO - Localizes the position of you mobile.
  * Copyright (C) 2012  Manuel Huber
  * 
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
  * *******************************************************************************/
 package org.booncode.android.loco;
 
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.app.AlarmManager;
 import android.content.Context;
 import android.content.Intent;
 import android.location.Location;
 import android.location.LocationManager;
 import android.location.LocationListener;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.os.Binder;
 import android.os.PowerManager;
 import android.os.SystemClock;
 import android.telephony.SmsManager;
 import android.telephony.TelephonyManager;
 import android.telephony.NeighboringCellInfo;
 import android.telephony.gsm.GsmCellLocation;
 import android.telephony.cdma.CdmaCellLocation;
 import android.util.Log;
 import java.lang.*;
 import java.util.List;
 import java.util.Vector;
 import java.util.LinkedList;
 import java.util.ListIterator;
 import java.util.regex.*;
 
 
 /*! \brief This is the main class that controlls the locating service.
  * 
  *  This service is used to:
  *  \li Locate the running device and respond by sms (\c CMD_LOCATE)
  *  \li Parse response sms and show data (\c CMD_RECEIVE_RESULT_POSITION, 
  *      \c CMD_RECEIVE_RESULT_CELLS)
  * 
  *  \todo Handle theft mode somehow...
  * */
 public class Stalker extends Service implements LocationListener
 {
   //! Intent extra arguments to start a specific command.
   public static final String EXTRA_KEY_CMD = "cmd";
   //! Intent extra arguments to identify the source of the command (telephone number).
   public static final String EXTRA_KEY_NUMBER = "number";
   //! Intent extra arguments: SMS content
   public static final String EXTRA_KEY_MESSAGE = "msg";
   
   //! Illegal command see #onStartCommand
   public static final int CMD_ILLEGAL = -1;
   //! This command initiates that the LocationListener is turned on.
   public static final int CMD_LOCATE = 1;
   //! This command signals data the geo-location has been received.
   public static final int CMD_RECEIVE_RESULT_POSITION = 2;
   //! This command signals that only cell information has been received.
   public static final int CMD_RECEIVE_RESULT_CELLS = 3;
   /*! \brief This command is used internally to wake the service periodically
    * 
    *  I thought that it's a good idea to wake the device to retrieve 
    *  positional updates. Also, the location algorithm now works with
    *  a timeout (#WAKE_UP_TIMEOUT_FAST, #WAKE_UP_TIMEOUT_FAST).
    *  So it's essential to wake the device on a periodic basis and 
    *  check #m_best_location whether the location could be retrieved or
    *  only cell information can be sent to the requestor.
    * */
   public static final int CMD_WAKE = 4;
   
   //! TAG used to identify debug messages from this service.
   protected static final String TAG = "loco.Stalker";
   
   /*! \brief After this period of time (ms), locating will return the 
    *         location.
    * 
    *  If the location couldn't be found tue to disabled internet connection
    *  the cell ids will be returned.
    *  
    *  \todo If the cell id('s) couldn't be found a message
    *  should be written to the address the request came from.
    * */
   protected static final long LOCATING_TIMEOUT = 1000 * 60;
   //! After this period of time (ms) locating will be shut off.
   protected static final long LOCATING_SHUTOFF_TIME = 3 * LOCATING_TIMEOUT + 1000 * 60 * 5;
   //! Maximum time (ms) a location is valid (Currently 1 minute).
   protected static final long LOCATING_MAX_AGE = 2 * 60 * 1000;
   /*! \brief This is the normal wake-up period (ms) after a request.
    * 
    *  Note that there are still unhandled requests while the device
    *  uses this wake-up period.
    * */
   protected static final long WAKE_UP_TIMEOUT_FAST = 5 * 1000;
   /*! \brief This wake-up period (ms) is used after all requests have been 
    *         handled
    * 
    *  The device will be woken up for a fixed period of time 
    *  (#LOCATING_SHUTOFF_TIME) to get better results for repeated
    *  requests (f.e.: GPS receiver needs some time to start...).
    * */
   protected static final long WAKE_UP_TIMEOUT_SLOW = 30 * 1000;
   
   //! Maximum number of different Notifications (of this application)
   protected static final int MAX_NOTIFY_ID_COUNT = 20;
   //! Title of all notifications
   protected static final String NOTIFY_TITLE = "Stalker";
   //! Text of notifications that show the position directly.
   protected static final String POS_NOTIFY_TEXT = "Position of %s";
   //! Ticker if the position could be retrieved.
   protected static final String POS_NOTIFY_TICKER = "Stalked %s";
   //! Text of notifications that only got cell info.
   protected static final String CELL_NOTIFY_TEXT = "Cell-id of %s";
   //! Ticker if only cell information could be gathered.
   protected static final String CELL_NOTIFY_TICKER = "Got Cell-id of %s";
   
   //! Regular expression to extract location from sms.
   protected static final Pattern RE_GEO = Pattern.compile("^\\s*(\\d+.\\d*)\\s*,\\s*(\\d+.\\d*)\\s*$");
   //! Regular expression to extract cell information (gsm).
   protected static final Pattern RE_GSM_CELL = Pattern.compile("^-gsm:\\s*(\\d+)\\s*,\\s*(\\d+)\\s*,\\s*(\\d+)\\s*,\\s*(\\d+)\\s*$");
   //! Regular expression to extract cell information (cdma).
   protected static final Pattern RE_CDMA_CELL = Pattern.compile("^-cdma:\\s*(\\d+)\\s*,\\s*(\\d+)\\s*,\\s*(\\d+)\\s*,\\s*(\\d+)\\s*,\\s*(\\d+)\\s*$");
   
   //! Template text to create an response-sms (#CMD_LOCATE)
   protected static final String POSITION_SMS = MsgReceiver.LOCO_CMD_VIEW_POSITION + "%s, %s";
   
   //! Binder object (not used)
   protected final IBinder         m_binder = new PrivateBinder();
   protected LocationManager       m_loc_man;
   protected AlarmManager          m_alarm_man;
   protected NotificationManager   m_notify_man;
   protected TelephonyManager      m_tel_man;
   //! Database of all persons and there privileges in conjunction with this service.
   protected StalkerDatabase       m_db;
   //! Object to retrieve other settings of this application.
   protected ApplicationSettings   m_settings;
   //! WakeLock to keep processor running. \see #onStartCommand
   protected PowerManager.WakeLock m_lock;
   //! List of requests to handle.
   protected List<LocRequest>      m_requests = new LinkedList<LocRequest>();
   //! Currently best location response (can be null).
   protected Location              m_best_location = null;
   //! This Pending Intent is used to wake the service.
   protected PendingIntent         m_wake_intent;
   //! This member holds timestamp when locating will be turned off.
   protected long                  m_loc_shutoff_time;
   //! \c true if Stalker is registered (as LocationListener).
   protected boolean               m_loc_is_listening;
   //! Current notification identifier (last used one).
   protected int                   m_current_notify_id = 0;
   
   
   /*! \brief Helper class for binding.
    * 
    *  Currently bindings aren't used by this application.
    * */
   public class PrivateBinder extends Binder
   {
     
     Stalker getStalker()
     {
       return Stalker.this;
     }
   }
   
   /*! \brief Helper class to hold information about one request.
    * 
    *  Used to handle multiple requests...
    * */
   protected class LocRequest
   {
     //! The telephone number the request came from.
     public String number;
     //! The timestamp when a response has to be sent.
     public long   timeout;
     
     /*! \brief Creates a new request
      * 
      *  #timeout will automatically be set to 
      *  the current time + #LOCATING_TIMEOUT.
      * 
      *  \param num telephone number the response will be sent to
      * */
     public LocRequest(String num)
     {
       this.number = num;
       this.timeout = SystemClock.elapsedRealtime() + LOCATING_TIMEOUT;
     }
   }
   
   
   /*! \brief This method is called when this service has been created
    * 
    *  Basically initializes all references to all sorts of Manager classes
    *  (LocationManager, AlarmManager, NotificationManager, TelephonyManager)
    *  and creates instances to access the stalker-database and settings.
    * */
   @Override
   public void onCreate()
   {
     m_loc_man = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
     m_alarm_man = (AlarmManager)getSystemService(ALARM_SERVICE);
     m_notify_man = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
     m_tel_man = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
     
     m_db = new StalkerDatabase(getApplicationContext());
     
     PowerManager power_man = (PowerManager)getSystemService(POWER_SERVICE);
     m_lock = power_man.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
     
     Intent intent = new Intent(this, Stalker.class);
     intent.putExtra(EXTRA_KEY_CMD, CMD_WAKE);
     m_wake_intent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
     m_loc_shutoff_time = SystemClock.elapsedRealtime();
     
     m_settings = new ApplicationSettings(this);
     m_current_notify_id = m_settings.getNotifyID();
   }
   
   /*! \brief Callback method (LocationListener), called on location
    *         update...
    * 
    *  Added some additional tests because android os (2.2) calls
    *  this method with an outdated location but the current time
    *  even if the mobile data-connection is disabled. So if location
    *  updates happen and the location-provider isn't gps I consider
    *  it as false update and ignore it.
    *  
    *  If #m_best_location is outdated, or the accuracy of the update
    *  is better (it's considered to be better if the update has got
    *  an accuracy indicator and the old location hasn't got one)
    *  #m_best_location will be updated.
    * 
    *  \param location The (new) location of the device. New updates can 
    *         be outdated (at least this is the case in android 2.2) 
    *         and they can be worse than older ones)
    * */
   @Override
   public void onLocationChanged(Location location)
   {
     String provider = location.getProvider();
     if (provider == null)
     {
       provider = "null";
     }
     
     Log.d(TAG, String.format("Location update: lat=%s, long=%s (%s)",
         String.valueOf(location.getLatitude()),
         String.valueOf(location.getLongitude()),
         provider));
     
     if (!provider.equals(LocationManager.GPS_PROVIDER))
     {
       if (m_tel_man.getDataState() != TelephonyManager.DATA_CONNECTED)
       {
         Log.d(TAG, "onLocationChanged: Stop lying to me, android-bitch!");
         return;
       }
       else
       {
         Log.d(TAG, "onLocationChanged: Data connection on...");
       }
     }
     
     if (m_best_location != null)
     {
       // m_best_location is too old
       boolean old_outdated = (m_best_location.getTime() + LOCATING_MAX_AGE < location.getTime());
       
       boolean old_accuracy = m_best_location.hasAccuracy();
       boolean new_accuracy = location.hasAccuracy();
       
       // location accuracy isn't worse than old one.
       boolean accuracy_not_worse;
       
       // check accuracy:
       if (old_accuracy && new_accuracy)
       {
         accuracy_not_worse = (location.getAccuracy() <= m_best_location.getAccuracy());
       }
       else if(old_accuracy)
       {
         accuracy_not_worse = false;
       }
       else
       {
         accuracy_not_worse = true;
       }
       
       // decide:
       if (old_outdated || accuracy_not_worse)
       {
         Log.d(TAG, "Replacing best-location...");
         m_best_location = location;
       }
       else
       {
         Log.d(TAG, "Keep old location...");
       }
     }
     else
     {
       Log.d(TAG, "Initializing best-location");
       m_best_location = location;
     }
   }
   
   /*! \brief Callback method (LocationListener) that informs about
    *         disabled location provider.
    * 
    *  \param provider Name of the provider that has been disabled.
    * */
   @Override
   public void onProviderDisabled(String provider)
   {
     Log.d(TAG, String.format("Provider %s disabled", provider));
   }
   
   /*! \brief Callback method (LocationListener) that informs about
    *         enabled location provider.
    * 
    *  \param provider Name of the provider that has been enabled.
    * */
   @Override
   public void onProviderEnabled(String provider)
   {
     Log.d(TAG, String.format("Provider %s enabled", provider));
   }
   
   //! Callback method (LocationListener), don't know what this method is about.
   @Override
   public void onStatusChanged(String provider, int status, Bundle extras)
   {
     Log.d(TAG, String.format("Provider %s status changed: %d", provider, status));
   }
   
   /*! \brief Helper method to enable listening for location updates.
    * 
    *  This method uses #m_loc_is_listening to keep track of whether 
    *  the service is currently listening for location updates or not.
    *  It's safe to call this method even if listening has already been 
    *  turned on (nothing will be done).
    * 
    *  \see disableLocationListener to turn off location listening.
    * */
   private void enableLocationListener()
   {
     if (!m_loc_is_listening)
     {
       Log.d(TAG, "Turning on LocationListener");
       m_loc_man.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0.0f, this);
       m_loc_man.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0.0f, this);
       m_loc_is_listening = true;
     }
   }
   
   /*! \brief Helper method to disable listening for location updates.
    * 
    *  This method uses #m_loc_is_listening to keep track of whether
    *  the service is currently listening for location updates or not.
    *  In either case it will turn off the listener (nothing happens if
    *  listener has been turned off).
    * 
    *  \see enableLocationListener to turn on location listening
    * */
   private void disableLocationListener()
   {
     if (m_loc_is_listening)
     {
       Log.d(TAG, "Turning off LocationListener");
       m_loc_man.removeUpdates(this);
       m_loc_is_listening = false;
     }
   }
   
   /*! \brief Helper method to release the wake-lock of this service.
    * 
    *  The wake-lock (#m_lock) is used to prevent the cpu from going into
    *  idle state. After this method has been called, cpu is allowed to
    *  go into idle state again.
    * 
    *  \see acquireLock to enable wake-lock.
    * */
   protected void releaseLock()
   {
     if (m_lock.isHeld())
     {
       Log.d(TAG, "Release WakeLock...");
       m_lock.release();
     }
     else
     {
       Log.d(TAG, "Request to release WakeLock (has not been acquired)...");
     }
   }
   
   /*! \brief Helper method to acquire the wake-lock of this service.
    * 
    *  The wake-lock (#m_lock) is used to prevent the cpu from going into
    *  idle state. After this method has been called, cpu is not allowed
    *  to go into idle state.
    * 
    *  \see releaseLock to release wake-lock.
    * */
   protected void acquireLock()
   {
     if (!m_lock.isHeld())
     {
       Log.d(TAG, "Aquire WakeLock...");
       m_lock.acquire();
     }
     else
     {
       Log.d(TAG, "Request to acquire WakeLock (but has already been done) ...");
     }
   }
   
   /*! \brief This method is used to enqueue a new request
    * 
    *  If the phone number of the requestor is authorised, the 
    *  request will be enqueued in #m_requests array and this service
    *  will start listening for location updates.
    *  This method will set up an alarm which will wake the device
    *  and check on timeout of requests (and send appropriate response).
    * 
    *  \param bundle A bundle which should contain the phone number of
    *         the person how sent the #CMD_LOCATE request.
    *  
    *  \see setNextAlarm to set wake-up alarm.
    *  \see wakeStalker will be called on #CMD_WAKE.
    * */
   protected void startLocating(Bundle bundle)
   {
     String number = bundle.getString(EXTRA_KEY_NUMBER);
     
     if (number != null)
     {
       if (m_db.isAuthorisedNumber(number))
       {
         LocRequest req = new LocRequest(number);
         enableLocationListener();
         m_requests.add(req);
         m_loc_shutoff_time = SystemClock.elapsedRealtime() + LOCATING_SHUTOFF_TIME;
         setNextAlarm(true);
       }
       else
       {
         Log.w(TAG, "Unauthorised Request " + number);
       }
       
     }
     else
     {
       Log.w(TAG, "Couldn't start locating -> number == null");
     }
   }
   
   /*! \brief This method set up a new wake-alarm.
    * 
    *  \param fast If set to \c true, alarm will be set to fire in 
    *         #WAKE_UP_TIMEOUT_FAST, else #WAKE_UP_TIMEOUT_SLOW 
    *         will be used.
    * */
   private void setNextAlarm(boolean fast)
   {
     long next_time = SystemClock.elapsedRealtime();
     
     if (fast)
     {
       next_time += WAKE_UP_TIMEOUT_FAST;
       Log.d(TAG, String.format("Setting up next alarm (+%d)", WAKE_UP_TIMEOUT_FAST));
     }
     else
     {
       next_time += WAKE_UP_TIMEOUT_SLOW;
       Log.d(TAG, String.format("Setting up next alarm (+%d)", WAKE_UP_TIMEOUT_SLOW));
     }
     
     m_alarm_man.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, next_time, m_wake_intent);
   }
   
   /*! \brief Helper method to check #m_best_location.
    * 
    *  \return \c true if location is not outdated, else \c false.
    * */
   private boolean isLocationUseable()
   {
     if (m_best_location != null)
     {
       if (m_best_location.getTime() + LOCATING_MAX_AGE >= System.currentTimeMillis())
       {
         return true;
       }
     }
     
     return false;
   }
   
   /*! \brief Helper method that sends a response message.
    * 
    *  This method checks if #m_best_location is up to date and if 
    *  not, it's tried to retrieve gsm or cdma information that could
    *  be sent.
    * 
    *  \todo Maybe send extra response message if no information (not
    *        even cell info) could be retrieved.
    *  
    *  \param number The phone number the response message will be
    *         sent to.
    * */
   private void respondPosition(String number)
   {
     if (isLocationUseable())
     {
       String latitude = Double.toString(m_best_location.getLatitude());
       String longitude = Double.toString(m_best_location.getLongitude());
       m_db.increaseSMSCount(number);
       
       Utils.sendSMS(number, String.format(POSITION_SMS, latitude, longitude));
     }
     else
     {
       String data = null;
       try
       {
         GsmCellLocation gsm_loc = (GsmCellLocation)m_tel_man.getCellLocation();
         String net_op = m_tel_man.getNetworkOperator();
         String mcc = net_op.substring(0, 3);
         String mnc = net_op.substring(3);
         Log.d(TAG, String.format("GSM-Cell: mcc=%s mnc=%s cid=%d lac=%d",
             mcc, mnc, gsm_loc.getCid(), gsm_loc.getLac()));
         
         data = String.format("%s-gsm: %s, %s, %d, %d", MsgReceiver.LOCO_CMD_VIEW_CELLS,
             mcc, mnc, gsm_loc.getCid(), gsm_loc.getLac());
       }
       catch(Exception ex)
       {
         Log.d(TAG, "Couldn't retrieve gsm-cell location", ex);
       }
       
       try
       {
         CdmaCellLocation cdma_loc = (CdmaCellLocation)m_tel_man.getCellLocation();
         Log.d(TAG, String.format("CDMA-Cell: bid=%d blat=%d blong=%d nid=%d sid=%d", 
             cdma_loc.getBaseStationId(), cdma_loc.getBaseStationLatitude(),
             cdma_loc.getBaseStationLongitude(), cdma_loc.getNetworkId(),
             cdma_loc.getSystemId()));
         data = String.format("%s-cdma: %d, %d, %d, %d, %d", MsgReceiver.LOCO_CMD_VIEW_CELLS,
             cdma_loc.getBaseStationId(), cdma_loc.getBaseStationLatitude(),
             cdma_loc.getBaseStationLongitude(), cdma_loc.getNetworkId(),
             cdma_loc.getSystemId());
       }
       catch(Exception ex)
       {
         Log.d(TAG, "Couldn't retrieve cdma-cell-data", ex);
       }
       
       if (data != null)
       {
         m_db.increaseSMSCount(number);
         Utils.sendSMS(number, data);
       }
       else
       {
         Log.w(TAG, "Couldn't retrieve cell-information...");
       }
     }
   }
   
   /*! \brief Helper method that checks if requests have expired. If so, 
    *         proper respons is sent.
    * 
    *  \return The number of requests that have not timed out.
    * 
    *  \see respondPosition Sends a proper response message...
    * */
   private int handleRequests()
   {
     long current_time = SystemClock.elapsedRealtime();
     ListIterator<LocRequest> iter = m_requests.listIterator();
     
     Log.d(TAG, String.format("Entering handleRequests: size=%d", m_requests.size()));
     
     while(iter.hasNext())
     {
       LocRequest req = iter.next();
       if (req.timeout <= current_time)
       {
         respondPosition(req.number);
         iter.remove();
       }
     }
     
     int size = m_requests.size();
     Log.d(TAG, String.format("Leaving handleRequests: size=%d", size));
     return size;
   }
   
   /*! \brief This method gets called if the service has been woken up
    *         by #CMD_WAKE.
    * 
    *  Currently it only checks for unhandled requests.
    *  If some period of time (#LOCATING_SHUTOFF_TIME) has passed 
    *  (therefore #m_loc_shutoff_time is used) this service will be 
    *  stopped.
    * 
    *  \see shutdownEventually Method that will be used to stop the
    *       service.
    *  \see handleRequests Method that is used to handle requests.
    *  */
   protected void wakeStalker()
   {
     Log.d(TAG, "wake Stalker...");
     
     int open_reqs = handleRequests();
     long timestamp = SystemClock.elapsedRealtime();
     
     if ((open_reqs <= 0) && (m_loc_shutoff_time < timestamp))
     {
       // no alarm; stop service...
       Log.d(TAG, "Stopping alarm -> all work is done...");
       shutdownEventually();
     }
     else
     {
       Log.d(TAG, String.format("Current Timestamp %d, Timeout: %d", timestamp, m_loc_shutoff_time));
       setNextAlarm((open_reqs > 0));
     }
   }
   
   /*! \brief Helper method that increases the current notification id 
    *         and returns it.
    * 
    *  This method is used to simplify the handling of the id's used
    *  by \c NotificationManager.notify.
    * 
    *  \note This method simply increments #m_current_notify_id, counts
    *        up to #MAX_NOTIFY_ID_COUNT - 1 and then starts over at 0.
    *        It's totally possible that an id is reused. However, this is
    *        not fatal, because this simply causes an old notification
    *        to be replaced by a new one and therefore limits the
    *        maximum number of notifications to #MAX_NOTIFY_ID_COUNT.
    * 
    *  \return The current notification id.
    * */
   private int increaseNotifyID()
   {
     m_current_notify_id = (m_current_notify_id + 1) % MAX_NOTIFY_ID_COUNT;
     m_settings.setNotifyID(m_current_notify_id);
     return m_current_notify_id;
   }
   
   /*! \brief Adds a notification which opens up a map and adds a pin
    *         with the name of the person.
    * 
    *  \param person The person that has been located
    *  \param geodata The position of the person.
    * */
   private void addPositionNotify(StalkerDatabase.Person person, String geodata)
   {
     final int icon = R.drawable.hut_stalker_icon;
     int notify_id = increaseNotifyID();
     Uri uri = Uri.parse(geodata);
     Intent intent = new Intent(android.content.Intent.ACTION_VIEW, uri);
     
     /* TRICKY: notify_id will be used for the PendingIntent and the
      *         Notification. Since the notification will be replaced 
      *         (if a notification with same id is currently queued)
      *         the pending intent should be replaced as well.
      * */
     PendingIntent pintent = PendingIntent.getActivity(this, notify_id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
     Notification notify = new Notification(icon,
                                            String.format(POS_NOTIFY_TICKER, person.name),
                                            System.currentTimeMillis());
     
     notify.setLatestEventInfo(this, NOTIFY_TITLE,
                               String.format(POS_NOTIFY_TEXT, person.name),
                               pintent);
     notify.flags = Notification.FLAG_SHOW_LIGHTS | Notification.FLAG_AUTO_CANCEL;
     m_notify_man.notify(notify_id, notify);
   }
   
   /*! \brief This method checks the supplied data (of command 
    *  #CMD_RECEIVE_RESULT_POSITION) and processes it.
    * 
    *  \param bundle The bundle that should contain all necessary keys
    *         (#EXTRA_KEY_MESSAGE and #EXTRA_KEY_NUMBER).
    *  
    *  \see addPositionNotify Method that really adds the notification.
    * */
   protected void notifyPosition(Bundle bundle)
   {
     String msg = bundle.getString(EXTRA_KEY_MESSAGE);
     String number = bundle.getString(EXTRA_KEY_NUMBER);
     
     if ((msg != null) && (number != null))
     {
       Matcher m = RE_GEO.matcher(msg);
       StalkerDatabase.Person person = m_db.getPersonFromNumber(number);
       if (m.matches() && (person != null))
       {
         MatchResult result = m.toMatchResult();
         String geo = Utils.formatGeoData(result.group(1), result.group(2), person.name);
         addPositionNotify(person, geo);
       }
       else
       {
         if(person == null)
         {
           Log.w(TAG, String.format("notifyPosition: Number (%s) not in database", number));
         }
         else
         {
           Log.w(TAG, String.format("notifyPosition: Msg is wrong (%s) %s", number, msg));
         }
       }
     }
     else
     {
       Log.d(TAG, "notifyPosition failed: missing bundle-keys...");
     }
   }
   
   /*! \brief Adds a notification which opens up an activity that shows
    *         all information that could be gathered from gsm cell.
    * 
    *  \param person The person that sent the response message.
    *  \param info All information that has been received from the gsm 
    *              cell (numbers).
    * */
   private void addGsmCellNotify(StalkerDatabase.Person person, String[] info)
   {
    final int icon = R.drawable.prog_icon;
     int notify_id = increaseNotifyID();
     Intent intent = new Intent(this, GsmCellActivity.class);
     intent.setFlags(Intent.FLAG_FROM_BACKGROUND | Intent.FLAG_ACTIVITY_SINGLE_TOP);
     intent.putExtra(GsmCellActivity.EXTRA_KEY_GSM_DATA, info);
     intent.putExtra(GsmCellActivity.EXTRA_SHOW_NAME, person.name);
     intent.putExtra(GsmCellActivity.EXTRA_SHOW_NUMBER, person.number);
     
     /* TRICKY: notify_id will be used for the PendingIntent and the
      *         Notification. Since the notification will be replaced 
      *         (if a notification with same id is currently queued)
      *         the pending intent should be replaced as well.
      * */
     PendingIntent pintent = PendingIntent.getActivity(this, notify_id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
     Notification notify = new Notification(icon,
                                            String.format(CELL_NOTIFY_TICKER, person.name),
                                            System.currentTimeMillis());
     
     notify.setLatestEventInfo(this, NOTIFY_TITLE,
                               String.format(CELL_NOTIFY_TEXT, person.name),
                               pintent);
     notify.flags = Notification.FLAG_SHOW_LIGHTS | Notification.FLAG_AUTO_CANCEL;
     m_notify_man.notify(notify_id, notify);
   }
   
   /*! \brief Adds a notification which opens up an activity that shows
    *         all information that could be gathered from cdma cell.
    * 
    *  \param person The person that sent the response message.
    *  \param info All information that has been received from the cdma 
    *              cell (numbers).
    * */
   private void addCdmaCellNotify(StalkerDatabase.Person person, String[] info)
   {
    final int icon = R.drawable.prog_icon;
     int notify_id = increaseNotifyID();
     Intent intent = new Intent(this, CdmaCellActivity.class);
     intent.setFlags(Intent.FLAG_FROM_BACKGROUND | Intent.FLAG_ACTIVITY_SINGLE_TOP);
     intent.putExtra(CdmaCellActivity.EXTRA_KEY_CDMA_DATA, info);
     intent.putExtra(CdmaCellActivity.EXTRA_SHOW_NAME, person.name);
     intent.putExtra(CdmaCellActivity.EXTRA_SHOW_NUMBER, person.number);
     
     /* TRICKY: notify_id will be used for the PendingIntent and the
      *         Notification. Since the notification will be replaced 
      *         (if a notification with same id is currently queued)
      *         the pending intent should be replaced as well.
      * */
     PendingIntent pintent = PendingIntent.getActivity(this, notify_id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
     Notification notify = new Notification(icon,
                                            String.format(CELL_NOTIFY_TICKER, person.name),
                                            System.currentTimeMillis());
     
     notify.setLatestEventInfo(this, NOTIFY_TITLE,
                               String.format(CELL_NOTIFY_TEXT, person.name),
                               pintent);
     notify.flags = Notification.FLAG_SHOW_LIGHTS | Notification.FLAG_AUTO_CANCEL;
     m_notify_man.notify(notify_id, notify);
   }
   
   /*! \brief This method checks the supplied data (of command 
    *  #CMD_RECEIVE_RESULT_CELLS) and processes it.
    * 
    *  \param bundle The bundle that should contain all necessary keys
    *         (#EXTRA_KEY_MESSAGE and #EXTRA_KEY_NUMBER).
    *  
    *  \see addGsmCellNotify Method that adds the notification for
    *       gsm information.
    *  \see addCdmaCellNotify Method that adds for cdma-cell.
    * */
   protected void notifyCell(Bundle bundle)
   {
     String msg = bundle.getString(EXTRA_KEY_MESSAGE);
     String number = bundle.getString(EXTRA_KEY_NUMBER);
     
     if ((msg != null) && (number != null))
     {
       Matcher match_gsm = RE_GSM_CELL.matcher(msg);
       Matcher match_cdma = RE_CDMA_CELL.matcher(msg);
       StalkerDatabase.Person person = m_db.getPersonFromNumber(number);
       
       if (person != null)
       {
         if (match_gsm.matches())
         {
           MatchResult result = match_gsm.toMatchResult();
           addGsmCellNotify(person, new String[]{result.group(1), result.group(2),
               result.group(3), result.group(4)});
           Log.v(TAG, "notifyCell: gsm: " + msg);
         }
         else if(match_cdma.matches())
         {
           MatchResult result = match_cdma.toMatchResult();
           addCdmaCellNotify(person, new String[]{result.group(1), result.group(2),
               result.group(3), result.group(4), result.group(5)});
           Log.v(TAG, "notifyCell: cdma: " + msg);
         }
         else
         {
           Log.w(TAG, String.format("notifyCell: Number (%s) illegal command: %s", number, msg));
         }
       }
       else
       {
         Log.w(TAG, String.format("notifyCell: Number (%s) not in database", number));
       }
       
     }
     else
     {
       Log.d(TAG, "notifyCells failed: no message or number...");
     }
   }
   
   /*! \brief Helper method to safely shutdown this service.
    * 
    *  Currently this method only shuts down the service if all requests
    *  have been handled. It ensures that location updates are stopped
    *  before \c stopSelf is called.
    * 
    *  \return \c true if service is about to stop, else \c false.
    * */
   private boolean shutdownEventually()
   {
     if ((m_requests.size() <= 0) && (m_loc_shutoff_time < SystemClock.elapsedRealtime()))
     {
       disableLocationListener();
       Log.d(TAG, "Stopping Stalker...");
       this.stopSelf();
       return true;
     }
     else
     {
       Log.d(TAG, "Request to stop stalker (but not finished yet)...");
       return false;
     }
   }
   
   /*! \brief Callback method (Service), called if some other component 
    *   (f.e.: an Activity tries to bind to this service.
    * 
    *  \param intent The intent of the binding.
    *  \return The binding-object #m_binder.
    * 
    *  \see PrivateBinder The class that implements the \c IBinder 
    *       interface.
    * */
   @Override
   public IBinder onBind(Intent intent)
   {
     return m_binder;
   }
   
   /*! \brief Callback method (Service), the entry point that gets called
    *         if the service has been started with a specific intent 
    *         (command).
    * 
    *  Basically checks the intent for valid commands (#EXTRA_KEY_CMD)
    *  and starts appropriate method that implement the commands.
    *  For now, 3 external commands are implemented:
    *  \li #CMD_LOCATE: Locates this device
    *  \li #CMD_RECEIVE_RESULT_POSITION: Shows position of tracked device.
    *  \li #CMD_RECEIVE_RESULT_CELLS: Shows cell information of tracked device.
    *  
    *  There is a fourth command (#CMD_WAKE) but it should only be used
    *  by this service (to wake the device on a periodic basis).
    *  #CMD_ILLEGAL is used to indicate an invalid command, this is not
    *  a command, and shouldn't be used by external components.
    *  
    *  \param intent The intent that started this service.
    *  \param flags Additional flags...
    *  \param startId Some id.
    *  \return A flag that determines whether the service should be
    *          restarted after it got killed by the os...
    *  
    *  \see startLocating Method that registers a new request to locate
    *       the device
    *  \see notifyPosition Method that adds a notification with the
    *       position of some other device.
    *  \see notifyCell Method that adds a notification with some cell
    *       information that could be retrieved.
    * */
   @Override
   public int onStartCommand(Intent intent, int flags, int startId)
   {
     int cmd = CMD_ILLEGAL;
     
     acquireLock();
     Log.d(TAG, "Stalker::onStartCommand");
     
     Bundle bundle = intent.getExtras();
     if (bundle != null)
     {
       cmd = bundle.getInt(EXTRA_KEY_CMD, CMD_ILLEGAL);
     }
     // else: cmd = CMD_ILLEGAL
     
     switch(cmd)
     {
       case CMD_ILLEGAL:
         shutdownEventually();
         break;
       
       case CMD_LOCATE:
         startLocating(bundle);
         break;
       
       case CMD_RECEIVE_RESULT_POSITION:
         notifyPosition(bundle);
         shutdownEventually();
         break;
       
       case CMD_RECEIVE_RESULT_CELLS:
         notifyCell(bundle);
         shutdownEventually();
         break;
       
       case CMD_WAKE:
         wakeStalker();
         break;
       
       default:
         Log.d(TAG, "Unknown command " + cmd);
         shutdownEventually();
     }
     
     releaseLock();
     
     return START_NOT_STICKY;
   }
   
   /*! \brief Callback method (Service), called if service is about to
    *         beeing stopped.
    * 
    *  \note This method won't be called in certain situations
    *        (f.e.: os killed the vm).
    * */
   @Override
   public void onDestroy()
   {
     Log.d(TAG, "Stalker::onDestroy()");
     disableLocationListener();
     releaseLock();
     m_db.close();
     super.onDestroy();
   }
 }
