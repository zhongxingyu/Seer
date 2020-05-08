 package org.acl.root;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentMap;
 
 import org.acl.root.utils.InstrumentedConcurrentMap;
 
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Binder;
 import android.os.IBinder;
 import android.os.RemoteException;
 import android.telephony.PhoneStateListener;
 import android.telephony.TelephonyManager;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.android.internal.telephony.ITelephony;
 
 /**
  * This is the service responsible of capturing incoming calls and 
  * perform the required actions.
  * 
  * @author Francisco Prez-Sorrosal (fperez)
  *
  */
 public class IncomingCallScanner extends Service {
 
 	private static final String TAG = "IncomingCallScanner";
 	
 	private static final String FILE = "blacklist.txt";
 	
 	private static final int SVC_STARTED_NOTIFICATION = 0;
 	
     private TelephonyManager tm;
     private com.android.internal.telephony.ITelephony telephonyService;
 
    
 	private ConcurrentMap<String, String> blackList = new InstrumentedConcurrentMap<String, String>(new ConcurrentHashMap<String, String>());
 	
 	private boolean serviceRunning = false;
 	
 	public boolean isServiceRunning() {
 		return serviceRunning;
 	}
 
 	/****************************************
 	 * Binder class
 	 *
 	 ****************************************/
     public class LocalBinder extends Binder {
         IncomingCallScanner getService() {
             return IncomingCallScanner.this;
         }
     }
     
 	private final IBinder mBinder = new LocalBinder();
 	@Override
 	public IBinder onBind(Intent intent) {
 		return mBinder;
 	}	
 	
 	private PhoneStateListener phoneStateListener = new PhoneStateListener() {
 		@Override
 	    public void onCallStateChanged(int state, String incomingNumber) {
 	       super.onCallStateChanged(state, incomingNumber);
 	       
 	       switch (state){
 	       case TelephonyManager.CALL_STATE_RINGING:
 	          Log.i(TAG, "ringing " + incomingNumber);
 	          String plainPhoneNumber = incomingNumber.replaceAll("[\\s\\-()]", "");
 	          if(blackList.containsKey(plainPhoneNumber)) {
 	        	  	if(telephonyService != null) {
 	        	  		try {
 	        	  		
 	        	  				String name = blackList.get(plainPhoneNumber);
 	        	  				CharSequence text = "Fucking " + name + "\nEnding call";
 	        	  				showToastWithImage(text, R.drawable.app_icon);
 							telephonyService.silenceRinger();
 							boolean result =telephonyService.endCall();
 							if(!result){
 								
 								Log.i(TAG, "Telephony Service is bad and does not finish the call");
 							}
							saveLogToFile((new Date()).toGMTString() + " " + name + " " +plainPhoneNumber+"\n");
 					} catch (RemoteException e) {
 				        Log.i(TAG, "Can't access Telephony Service");
 						e.printStackTrace();
 					}
 	        	  	} else {
 	        	  		Log.i(TAG, "Telephony Service not bound");
 	        	  	}
 	          }
 	          break;
 	       
 	       case TelephonyManager.CALL_STATE_IDLE:
 	          Log.d(TAG, "idle");
 	          break;
 
 	       case TelephonyManager.CALL_STATE_OFFHOOK :
 	          Log.d(TAG, "offhook");
 	          break;
 
 	       }
 		}
 	};
 	
 	// ------------------------- Lifecycle -------------------------
 	
 	@Override
 	public void onCreate() {
 		if(!serviceRunning) {
 			Log.d(TAG, "Loading map with blacklist...");
 			loadMapFromFile();
 			tm = ((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE));
 			try {
 				telephonyService = getTelephonyService();
 			} catch (Exception e) {
 				Log.i(TAG, "Can't get Telephony Service");
 				e.printStackTrace();
 			}
 			tm.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
 			
 			serviceRunning = true;			
 			Log.d(TAG, "Created");
 		}
 	}
 	
 	@Override
     public int onStartCommand(Intent intent, int flags, int startId) {
         Log.d(TAG, "Received start id " + startId + ": " + intent);
         showNotification();
         // We want this service to continue running until it is explicitly
         // stopped, so return sticky.
         return START_STICKY;
     }
 	
 	
 	@Override
     public void onDestroy() {
 		if(serviceRunning) {
 	        saveMapToFile();
 	        blackList.clear();
 	        // Stop filtering calls, otherwise they'll continue to be filtered
 			tm.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE); 		
 	        tm = null;
 	        serviceRunning = false;
 	        Log.d(TAG, "Stopped");
 	        // Tell the user we stopped.
 	        Toast.makeText(getApplicationContext(), R.string.ics_service_stopped, Toast.LENGTH_SHORT).show();
 		}
     }
 	
 	// ------------------------- Lifecycle -------------------------
 	
 	// ------------------- Black List Management -------------------
 
 	public ArrayList<CharSequence> getBlackList() {
 		return  new ArrayList<CharSequence>(blackList.values());
 	}
 
 	public String addContactToBlackList(Contact contact) {
 		String name = contact.getName();
 		String phone = contact.getPhoneNumbers().get(0);
 		String previousValue = blackList.putIfAbsent(phone, name + " (" + phone + ")");
         Log.d(TAG, name + " " + phone + " added to Black List in service method");
         return previousValue;
 	}
 	
 	public String removeContactFromBlackList(String key) {
 		String previousValue = blackList.remove(key);
         Log.d(TAG, previousValue + " removed from Black List in service method");
         return previousValue;
 	}
 
 	// ------------------END Black List Management -----------------
 	
 	@SuppressWarnings("unchecked")
     private ITelephony getTelephonyService() throws Exception {
             // Set up communication with the telephony service (thanks to Tedd's Droid Tools!)
             TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
             Class c = Class.forName(tm.getClass().getName());
             Method m = c.getDeclaredMethod("getITelephony");
             m.setAccessible(true);
             return (ITelephony)m.invoke(tm);
     }
 	
 	private void loadMapFromFile(){
 		try {
 			FileInputStream fis = openFileInput(FILE);
 
 			InputStreamReader inputreader = new InputStreamReader(fis);
 			BufferedReader buffreader = new BufferedReader(inputreader);
 
 			String line;
 
 			// read every line of the file into the line-variable, on line at the time
 			while ((line = buffreader.readLine()) != null) {
 				// do something with the settings from the file
 				Log.d(TAG, "Reading " + line);
 				String [] data = line.split("-");	
 				blackList.putIfAbsent(data[0], data[1]);
 			}
 			buffreader.close();
 			inputreader.close();
 			fis.close();
 
 		} catch (FileNotFoundException e) { 
 			Toast.makeText(this, "File Still not created", Toast.LENGTH_SHORT).show();
 		} catch (IOException e) {
 			Toast.makeText(this, "Exception" + e.toString(), Toast.LENGTH_SHORT).show();
 		}
 	}
 
     private void saveMapToFile(){
     	
     		try {
     			FileOutputStream fos = openFileOutput(FILE, Context.MODE_PRIVATE);
     			
     			OutputStreamWriter outputwriter = new OutputStreamWriter(fos);
     			BufferedWriter buffwriter = new BufferedWriter(outputwriter);
     			
     			for (Map.Entry<String, String> entry : blackList.entrySet()) {
     				String data = entry.getKey() + "-" + entry.getValue() + "\n";
     		        Log.d(TAG, "Writting " + data);
     			    buffwriter.write(data);
     		      
     			}
     			buffwriter.close();
     			outputwriter.close();
 
     			fos.close();
 			
 			Toast.makeText(this, "Black List Saved", Toast.LENGTH_SHORT).show();
 		} catch (FileNotFoundException e) {
 			Toast.makeText(this, "FileNotFoundException" + e.toString(), Toast.LENGTH_SHORT).show();
 		} catch (IOException e) {
 			Toast.makeText(this, "IOException" + e.toString(), Toast.LENGTH_SHORT).show();
 		}
 
     }
     
 	private void showNotification() {
 		NotificationManager mNotificationManager = 
 				(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
 		
 		int icon = R.drawable.skullnbones;
 		CharSequence tickerText = "Starting Incoming Call Scanner";
 		long when = System.currentTimeMillis();
 
 		Notification notification = new Notification(icon, tickerText, when);
 		notification.flags |= Notification.DEFAULT_VIBRATE | 
 				Notification.FLAG_NO_CLEAR;
 		long[] vibrate = {0,100,200,300};
 		notification.vibrate = vibrate;
 		
 		Context context = getApplicationContext();
 		CharSequence contentTitle = "Incoming Call Scanner";
 		CharSequence contentText = "The service is filtering inconming calls from jerks!";
 		Intent notificationIntent = new Intent(this, MainActivity.class);
 		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
 
 		notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
 		
 		mNotificationManager.notify(SVC_STARTED_NOTIFICATION, notification);
 	}
 	
 	private void cancelNotification(int notification) {
 		NotificationManager mNotificationManager = 
 				(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
 		mNotificationManager.cancel(notification);
 	}
 
 	private void showToastWithImage(CharSequence textToShow, int imageResourceId) {
 		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		View layout = inflater.inflate(R.layout.toast_layout, null);
 
 		ImageView image = (ImageView) layout.findViewById(R.id.image);
 		image.setImageResource(imageResourceId);
 		TextView text = (TextView) layout.findViewById(R.id.text);
 		text.setText(textToShow);
 
 		Toast toast = new Toast(getApplicationContext());
 		toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
 		toast.setDuration(Toast.LENGTH_LONG);
 		toast.setView(layout);
 		toast.show();
 	}
 	
 	private void saveLogToFile(String data){
     	
   		try {
   			FileOutputStream fos = openFileOutput(ShowLogActivity.LOGFILE,Context.MODE_PRIVATE|Context.MODE_APPEND);
   			
   			OutputStreamWriter outputwriter = new OutputStreamWriter(fos);
   			BufferedWriter buffwriter = new BufferedWriter(outputwriter);			
   		    buffwriter.append(data);	
   	    	buffwriter.close();
   			outputwriter.close();
 
   			fos.close();
 			
 		
 		} catch (FileNotFoundException e) {
 			Toast.makeText(this, "FileNotFoundException" + e.toString(), Toast.LENGTH_SHORT).show();
 		} catch (IOException e) {
 			Toast.makeText(this, "IOException" + e.toString(), Toast.LENGTH_SHORT).show();
 		}
 
   }
 
 
 }
 
