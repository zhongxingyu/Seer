 package egl.positioningsystem;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 import android.location.LocationManager;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.preference.PreferenceManager;
 import android.provider.Settings;
 import android.annotation.SuppressLint;
 import android.app.AlertDialog;
 import android.content.ContentResolver;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.pm.PackageManager;
 import android.content.pm.ResolveInfo;
 import android.support.v4.app.FragmentActivity;
 import android.telephony.TelephonyManager;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import egl.positioningsystem.SensorGPS;
 import egl.positioningsystem.SocketTask;
 
 public class MainActivity extends FragmentActivity {
 	public static final String KEY_PREF_HOST = "prefHost";
 	public static final String KEY_PREF_PORT = "prefPort";
 	//Settings
 	private static final int RESULT_SETTINGS = 1;
 	// Objects to deal with location ------------------
 	private TextView tv_latitude = null;
 	private TextView tv_longitude = null;
 	private String str_longitude;
 	private String str_latitude;
 	private Double latitude;
 	private Double longitude;
 	private Double velocidade;
	private LocationManager lmGPS = null, lmNetwork = null;
 
 	// Objects to deal with acceleration ------------------
 	//private SensorAccel myAccel = null;
 	private SensorGPS myGPS = null;
 	
 	//To create infinite loop
 	private Handler handler_connection = null;
 	private Handler handler_screen = null;
 	private final int CONNECTION_TIME_DELAY = 10000;//10s
 	private final int SCREEN_TIME_DELAY = 1000;//10s
 	
 	//Socket
 	//IP for tests "164.41.65.20:8090"; Rafael
 	private String host;
 	private int port;
     private TextView txtStatus;
     private SocketTask automaticSender;
     // Handle to SharedPreferences for this app
     SharedPreferences mPrefs;
     SharedPreferences.OnSharedPreferenceChangeListener  prefListener;
     
     String device_id;
     TelephonyManager tm;
 	
     @Override
     protected void onCreate(Bundle savedInstanceState) {   	
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
 
         //GPS --------------------------------------------------
         tv_latitude = (TextView) findViewById(R.id.latitude_value);
         tv_longitude = (TextView) findViewById(R.id.longitude_value);
    	lmNetwork = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    	lmGPS = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    	myGPS = new SensorGPS(lmNetwork, lmGPS);
         
     	boolean flag = displayGpsStatus();
     	if(!flag) alertbox("Gps Status!!", "Your GPS is: OFF");
     	//Socket
 
         txtStatus = (TextView) findViewById(R.id.textViewStatus);
     	
         tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
         device_id = tm.getDeviceId();
         
     	//Automation of Sensor Handling
     	handler_connection = new Handler();
     	handler_connection.postDelayed(connection_runnable, 100);
     	handler_screen = new Handler();
     	handler_screen.postDelayed(screen_runnable, 100);
     	
     	//Settings
     	mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
     	settingsListener();
     }
  
     protected void settingsListener(){
     	host = mPrefs.getString(SettingsActivity.KEY_PREF_HOST, "");
     	port = (int) Integer.parseInt(mPrefs.getString(SettingsActivity.KEY_PREF_PORT, ""));
     }
 	protected void onResume() {
     	super.onResume();
     	mPrefs.registerOnSharedPreferenceChangeListener(prefListener);
 	}		
     	
 	protected void onPause() {
 		super.onPause();
 		mPrefs.unregisterOnSharedPreferenceChangeListener(prefListener);
 	}
 	
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.main, menu);
         return true;
     }
     
     @Override
 	protected void onDestroy() {
     	automaticSender.cancel(true);
     	super.onDestroy();
 	}
     
     /**
      * Open map with the location:
      * @param Uri location
      */
     public void openMap(Uri location){
     	//Create a mapIntent:
     	Intent mapIntent = new Intent(Intent.ACTION_VIEW, location);
     	
     	//Verify if has some app to run the Intent:
     	PackageManager packageManager = getPackageManager();
     	List<ResolveInfo> activities = packageManager.queryIntentActivities(mapIntent, 0);
     	
     	if(activities.size() > 0){
     		//Execute Intent
     		startActivity(mapIntent);
     	}else{
     		Toast.makeText(MainActivity.this, "Problems to open a Map", Toast.LENGTH_SHORT).show();
     	}
     }
     
     /**
      * Action to the button Map
      * @param view
      */
     public void openMap(View view){
     	//TODO Check Uri for location.
     	Uri location = Uri.parse("geo: "+latitude+","+longitude);
     	openMap(location);
     }
     
     /**
      * Get Location Data by location provider
      */
     public void getGPSData(){
     	latitude = (Double) myGPS.getLatitude();
     	longitude = (Double) myGPS.getLongitude();
    // 	velocidade = (Double) myGPS.getVelocidade();
     	str_latitude = latitude.toString();
     	str_longitude =longitude.toString();
     }
     
     /**
      * Send message to the last defined host
      */
     public void sendBySocket(){
     	sendBySocket(host,port);
     }
     
     /**
      * Send message to specified host and port by socket
      * @param host IP of server host
      * @param port Port of server host
      */
     @SuppressLint("SimpleDateFormat")
 	public void sendBySocket(String host,int port){
     	try{
     	automaticSender = new SocketTask(host, port, 1500){
 			@Override
             protected void onProgressUpdate(String... progress) {
                 SimpleDateFormat sdf = new SimpleDateFormat(
                         "dd/MM/yyyy HH:mm:ss");
                 // Recupera o retorno
                 txtStatus.setText(sdf.format(new Date()) + " - "
                         + progress[0]);
             }
     	};
     	SimpleDateFormat sdf = new SimpleDateFormat(
             "dd/MM/yyyy HH:mm:ss");
     
     	String sendThis = device_id + "," + sdf.format(new Date()) + "," + latitude + "," +  longitude + "," + velocidade;
     	automaticSender.execute(sendThis);
     	}catch(Exception e){
         	Toast.makeText(MainActivity.this, "CONNECTION ERROR!", Toast.LENGTH_SHORT).show();
         }
     }
    
     private Runnable screen_runnable = new Runnable() {
    	   @SuppressLint("SimpleDateFormat")
    	   @Override
    	   public void run() {
    	    	getGPSData();
    	    	tv_latitude.setText(str_latitude);
    	    	tv_longitude.setText(str_longitude);
    	    	
    	      /* and here comes the "trick" */    		
           handler_screen.postDelayed(this, SCREEN_TIME_DELAY);
        }
     };
     
     private Runnable connection_runnable = new Runnable() {
     	   @SuppressLint("SimpleDateFormat")
     	   @Override
     	   public void run() {
     	      /* do what you need to do */
     		    Boolean flag = haveInternet(getBaseContext());
     		    if(!flag) alertboxNetwork();
     		    settingsListener();
     	    	sendBySocket();    	    	
     	      /* and here comes the "trick" */    		
 
            handler_connection.postDelayed(this, CONNECTION_TIME_DELAY);
         }
      };
      
      /**
       * Checks if we have a valid Internet Connection on the device.
       * @param ctx
       * @return True if device has internet
       *
       * Code from: http://www.androidsnippets.org/snippets/131/
       */
      public static boolean haveInternet(Context ctx) {
 
          NetworkInfo info = (NetworkInfo) ((ConnectivityManager) ctx
                  .getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
 
          if (info == null || !info.isConnected()) {
              return false;
          }
          if (info.isRoaming()) {
              // here is the roaming option you can change it if you want to
              // disable internet while roaming, just return false
              return false;
          }
          return true;
      }
      /**
       * Method to create an AlertBox 
       * @param String title
       * @param String mymessage
       */
  	protected void alertbox(String title, String mymessage) {
  		AlertDialog.Builder builder = new AlertDialog.Builder(this);
  		builder.setMessage("Your Device's GPS is Disabled")
  				.setCancelable(false)
  				.setTitle("** Gps Status **")
  				.setPositiveButton("Gps On",
  						new DialogInterface.OnClickListener() {
  							public void onClick(DialogInterface dialog, int id) {
  								// finish the current activity
  								// AlertBoxAdvance.this.finish();
  								Intent myIntent = new Intent(
  										Settings.ACTION_SECURITY_SETTINGS);
  								startActivity(myIntent);
  								dialog.cancel();
  							}
  						})
  				.setNegativeButton("Cancel",
  						new DialogInterface.OnClickListener() {
  							public void onClick(DialogInterface dialog, int id) {
  								// cancel the dialog box
  								dialog.cancel();
  							}
  						});
  		AlertDialog alert = builder.create();
  		alert.show();
  	}
  	
     /**
      * Method to create an AlertBox for Internet
      * @param String title
      * @param String mymessage
      */
 	protected void alertboxNetwork() {
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setMessage("Your Device's internet is Disabled")
 				.setCancelable(false)
 				.setTitle("** Network Status **")
 				.setPositiveButton("Network On",
 						new DialogInterface.OnClickListener() {
 							public void onClick(DialogInterface dialog, int id) {
 								// finish the current activity
 								// AlertBoxAdvance.this.finish();
 								Intent myIntent = new Intent(
 										Settings.ACTION_SETTINGS);
 								startActivity(myIntent);
 								dialog.cancel();
 							}
 						})
 				.setNegativeButton("Cancel",
 						new DialogInterface.OnClickListener() {
 							public void onClick(DialogInterface dialog, int id) {
 								// cancel the dialog box
 								dialog.cancel();
 							}
 						});
 		AlertDialog alert = builder.create();
 		alert.show();
 	}
 	/**
 	 * Method to Check GPS is enable or disable
 	 * @param null
 	 */
 	private Boolean displayGpsStatus() {
 		ContentResolver contentResolver = getBaseContext().getContentResolver();
 		boolean gpsStatus = Settings.Secure.isLocationProviderEnabled(
 				contentResolver, LocationManager.GPS_PROVIDER);
 		if (gpsStatus) {
 			return true;
 
 		} else {
 			return false;
 		}
 	}
 	
 	/**
 	 * Menu events handler
 	 */
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 	    // Handle item selection
 	    switch (item.getItemId()) {
 	    	//To open a view when Settings menu button is pressed
 	        case R.id.action_settings:
 	        	Intent i = new Intent(this, SettingsActivity.class);
 	            startActivityForResult(i, RESULT_SETTINGS);
 	            return true;
 	        default:
 	            return super.onOptionsItemSelected(item);
 	    }
 	}
 	
 	@Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         super.onActivityResult(requestCode, resultCode, data);
  
         switch (requestCode) {
         case RESULT_SETTINGS:
             showUserSettings();
             break;
  
         }
  
     }
 	
     private void showUserSettings() {
         mPrefs = PreferenceManager
                 .getDefaultSharedPreferences(this);
  
         StringBuilder builder = new StringBuilder();
  
         builder.append("\n Username: "
                 + mPrefs.getString("prefUsername", "NULL"));
  
         builder.append("\n Send report:"
                 + mPrefs.getBoolean("prefSendReport", false));
  
         builder.append("\n Sync Frequency: "
                 + mPrefs.getString("prefSyncFrequency", "NULL"));
 /* 
         TextView settingsTextView = (TextView) findViewById(R.id.textUserSettings);
  
         settingsTextView.setText(builder.toString());*/
     }
 
 }
