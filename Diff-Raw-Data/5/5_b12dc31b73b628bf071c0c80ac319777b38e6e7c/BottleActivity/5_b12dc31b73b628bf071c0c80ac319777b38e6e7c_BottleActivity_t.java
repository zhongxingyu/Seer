 package mamont.bottle;
 
 import java.io.BufferedReader;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.PrintWriter;
 import java.net.HttpURLConnection;
 import java.net.Inet4Address;
 import java.net.InetAddress;
 import java.net.Socket;
 import java.net.URL;
 import java.net.UnknownHostException;
 
 import android.app.Activity;
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.hardware.Camera;
 import android.hardware.Camera.PictureCallback;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.BatteryManager;
 import android.os.Bundle;
 import android.os.Handler;
 import android.util.Log;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 import android.widget.TextView;
 
 
 
 public class BottleActivity extends Activity 
 {
 	static String TAG = "bottle"; 
 	String urlServer = "http://mamont.co/post2.php";
 	String lineEnd = "\r\n";
 	String twoHyphens = "--";
 	String boundary =  "*****";
 	//long periodMs = 15 * 60000;
 	//long periodMs = 15 * 1000;				//...
 	long periodMs = 5 * 60000;
 	
 	TextView log;
 	Camera camera = null;
 	SurfaceView preview = null;
 	SurfaceHolder previewHolder = null;
 	
 	public static final String ACTION_NAME = "mamont.bottle.ALARM"; 
 	private IntentFilter alarmFilter = new IntentFilter(ACTION_NAME); 
 	
 	boolean locationNetworkSent = false;
 	boolean locationGpsSent = false;
 	boolean inPicture = false;
 	
 	int currentBatteryLevel = -1;
     int currentBatteryStatus = -1; 
     int currentBatteryTemperature = -1; 
     int currentBatteryVoltage = -1; 
     
     Location currentNetworkLocation;
     Location currentGpsLocation;
     
     byte[] currentPicture;
     
     int gpsLocationsSent = 0;
     int picturesSent = 0;
     int bytesSent = 0;
     
 	
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) 
 	{
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 		
 		Log.i(TAG, "-> onCreate");
 		
 		log = (TextView)findViewById(R.id.log);
 		log.setText("started\n");
 		
 		
 		// Alarm.
 
 		registerReceiver(alarmReceiverCB, alarmFilter); 
 		
 		final Intent intent = new Intent(ACTION_NAME);
 		final PendingIntent pending = PendingIntent.getBroadcast(this, 0, intent, 0);
 		
 		AlarmManager alarmMgr = (AlarmManager)this.getSystemService(Context.ALARM_SERVICE);
 		alarmMgr.cancel(pending);
 		alarmMgr.setRepeating(
 			AlarmManager.RTC_WAKEUP, 
 			System.currentTimeMillis() + periodMs,
 			periodMs, 
 			pending);
 
 
 		// Camera.
 		
 		preview = (SurfaceView)findViewById(R.id.preview);
 		previewHolder = preview.getHolder();
 		previewHolder.addCallback(surfaceCB);			// --> act.
 		previewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
 
 		
 		// Battery status.
 		
 		IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED); 
 	    registerReceiver(batteryReceiverCB, filter); 
 		
 
 	    // Done.
 	
 		log.append("initialized\n");
 		Log.i(TAG, "<- onCreate");
 	}
 
 	
 	
 	public void act()
 	{
 		Log.i(TAG, "-> act");
 		
 		
 		// Battery status.
 		
 		new Thread(new Runnable() 
 		{
 	        public void run() 
 	        {
 	    		send("battery.txt", (currentBatteryLevel + "," + currentBatteryStatus + "," + currentBatteryTemperature + "," + currentBatteryVoltage).getBytes());
 	        }
 		}).start();
 		
 		
 		// Location.
 		
 		locationNetworkSent = false;
 		locationGpsSent = false;
 		LocationManager locationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
 		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, new LocationListenerNetworkCB());
 		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new LocationListenerGpsCB());
 		
 
 		if (camera == null)
 		{
 			Log.i(TAG, "act - Camera.Open");
 			camera = Camera.open();
 			initCamera();
 		}
 		
 		if (previewHolder.getSurface() == null)
 		{
 			Log.i(TAG, "act - create preview");
 			preview = (SurfaceView)findViewById(R.id.preview);
 			previewHolder = preview.getHolder();
 			previewHolder.addCallback(surfaceCB);			// --> act.
 			previewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
 		}
 		else
 		{
 			if (inPicture)
 			{
 				Log.i(TAG, "act - inPicture");
 			}
 			else
 			{
 				Log.i(TAG, "act - startPreview, takePicture 1");
 				inPicture = true;
 				camera.startPreview();
 				camera.takePicture(null, null, new PictureCB());
 			}
 		}
 		
	    log.append("g=" + gpsLocationsSent + ", p=" + picturesSent + ", b=" + bytesSent + "   ");
 	    
 		Log.i(TAG, "<- act");
 	}
 	
 	
 	
 	public static class BootUpReceiverCB extends BroadcastReceiver
 	{
 		@Override
 		public void onReceive(Context context, Intent intent) 
 		{
 			Log.i(TAG, "-> BootUpReceiverCB::onReceive"); 
 
 			Intent startIntent = new Intent(context, BottleActivity.class);  
 			startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 			context.startActivity(startIntent);  
 
 			Log.i(TAG, "<- BootUpReceiverCB::onReceive"); 
 		}
 	}
 	
 
 
 	BroadcastReceiver alarmReceiverCB = new BroadcastReceiver() 
 	{ 
 		@Override 
 		public void onReceive(Context context, Intent intent) 
 		{ 
 			Log.i(TAG, "-> alarmReceiverCB::onReceive"); 
 			
 			act();
 			
 			Log.i(TAG, "<- alarmReceiverCB::onReceive"); 
 		}
 	}; 
 	
 	
 	
 	
 	BroadcastReceiver batteryReceiverCB = new BroadcastReceiver() 
 	{ 
         public void onReceive(Context context, Intent intent) 
         { 
             Log.i(TAG, "-> batteryReceiverCB::onReceive"); 
             
             int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1); 
             int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1); 
             currentBatteryLevel = 100 * level / scale;
             currentBatteryStatus = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
             currentBatteryTemperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1); 
             currentBatteryVoltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1); 
             
             Log.i(TAG, "<- batteryReceiverCB::onReceive"); 
         } 
     }; 
 	
 	
 	
 	SurfaceHolder.Callback surfaceCB = new SurfaceHolder.Callback() 
 	{
 		public void surfaceCreated(SurfaceHolder holder) {} 
 		
 		public void surfaceChanged(SurfaceHolder holder,
 								   int format, 
 								   int width,
 								   int height) 
 		{
 			act();
 		}
 		
 		public void surfaceDestroyed(SurfaceHolder holder) {}
 	};
 	
 	
 	
 	private void initCamera() 
 	{
 		try
 		{
 			camera.setPreviewDisplay(previewHolder);
 		}
 		catch (Throwable t)
 		{
 			Log.e(TAG, "initCamera: setPreviewDisplay failed", t);
 		}
 		
 		//TODO: Configure camera here.
 	}    
 
 
 
 	public class PictureCB implements PictureCallback 
 	{
 		public void onPictureTaken(byte[] data, Camera camera) 
 		{
 			Log.i(TAG, "-> PictureCB::onReceive"); 
 		
 			currentPicture = data;
 			new Thread(new Runnable() 
 			{
 		        public void run() 
 		        {
 					send("cam.jpg", currentPicture);
 					picturesSent++;
 		        }
 			}).start();
 			
 			inPicture = false;
 			Log.i(TAG, "<- PictureCB::onReceive"); 
 		}
 	};
 	
 	
 	
 	public class LocationListenerNetworkCB implements LocationListener
 	{
 		public void onStatusChanged(String provider, int status, Bundle extras) {}
 		public void onProviderEnabled(String provider) {}
 		public void onProviderDisabled(String provider) {}
 
 		public void onLocationChanged(Location location) 
 		{
 			Log.i(TAG, "-> LocationListenerNetworkCB::onLocationChanged");
 			
 			if (!locationNetworkSent)
 			{
 				locationNetworkSent = true;
 				currentNetworkLocation = location;
 				new Thread(new Runnable() 
 				{
 			        public void run() 
 			        {
 						send(currentNetworkLocation.getProvider() + ".txt", currentNetworkLocation.toString().getBytes());
 			        }
 				}).start();
 			}
 
 			Log.i(TAG, "<- LocationListenerNetworkCB::onLocationChanged"); 
 		}
 	};
 	
 
 	
 	public class LocationListenerGpsCB implements LocationListener
 	{
 		public void onStatusChanged(String provider, int status, Bundle extras) {}
 		public void onProviderEnabled(String provider) {}
 		public void onProviderDisabled(String provider) {}
 
 		public void onLocationChanged(Location location) 
 		{
 			Log.i(TAG, "-> LocationListenerGpsCB::onLocationChanged"); 
 		
 			if (!locationGpsSent)
 			{
 				locationGpsSent = true;
 				currentGpsLocation = location;
 				new Thread(new Runnable() 
 				{
 			        public void run() 
 			        {
 						send(currentGpsLocation.getProvider() + ".txt", currentGpsLocation.toString().getBytes());
 						gpsLocationsSent++;
 			        }
 				}).start();
 			}
 			
 			Log.i(TAG, "<- LocationListenerGpsCB::onLocationChanged"); 
 		}
 	};
 	
 	
 	
 	void send(String fileName, byte[] data)
 	{
 		try 
 		{
 			URL url = new URL(urlServer);
 			
 			HttpURLConnection connection = (HttpURLConnection)url.openConnection();
 			connection.setDoInput(true);
 			connection.setDoOutput(true);
 			connection.setUseCaches(false);
 			connection.setRequestMethod("POST");
 			connection.setRequestProperty("Connection", "Keep-Alive");
 			connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
 			
 			DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
 			outputStream.writeBytes(twoHyphens + boundary + lineEnd);
 			outputStream.writeBytes("Content-Disposition: form-data; name=\"uploadedfile\";filename=\"" + fileName +"\"" + lineEnd);
 			outputStream.writeBytes(lineEnd);
 			
 			outputStream.write(data);
 
 			outputStream.writeBytes(lineEnd);
 			outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
 			outputStream.flush();
 
 			//int serverResponseCode = connection.getResponseCode();
 			String serverResponseMessage = connection.getResponseMessage();
 			
 			//Log.e(TAG, serverResponseCode);
 			Log.e(TAG, serverResponseMessage);
 			
 			outputStream.close();
			
			bytesSent += data.length;
 		}
 		catch (UnknownHostException e) 
 		{
 			Log.e(TAG, "send: server not found");
 		} 
 		catch (IOException e) 
 		{
 			Log.e(TAG, "send: couldn't open socket");
 		}
 		catch (Throwable e) 
 		{
 			Log.e(TAG, "send: unknown exception");
 		}
 	}
 		
 	
 	
 	@Override
 	public void onResume() 
 	{
 		Log.i(TAG, "-> onResume");
 		
 		super.onResume();
 	  
 	  //camera = Camera.open();
 	  //startPreview();
 
 		Log.i(TAG, "<- onResume");
 	}
 	  
 	
 	
 	@Override
 	public void onPause() 
 	{
 		Log.i(TAG, "-> onPause");
 	
 	  //if (inPreview) 
 	  //{
 	//	camera.stopPreview();
 	  //}
 	  
 		camera.release();
 		camera = null;
 	  //inPreview = false;
 			
 		super.onPause();
 	  
 		Log.i(TAG, "<- onPause");
 	}
 }
 
 
 
 
 //camera.setPreviewCallback(null); 
 //camera.release();
 
 
 
 
 /*        
 try 
 {
 	URL url = new URL(urlServer);
 	
 	HttpURLConnection connection = (HttpURLConnection)url.openConnection();
 	connection.setDoInput(true);
 	connection.setDoOutput(true);
 	connection.setUseCaches(false);
 	connection.setRequestMethod("POST");
 	connection.setRequestProperty("Connection", "Keep-Alive");
 	connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
 	
 	DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
 	outputStream.writeBytes(twoHyphens + boundary + lineEnd);
 	outputStream.writeBytes("Content-Disposition: form-data; name=\"uploadedfile\";filename=\"" + "testfile.txt" +"\"" + lineEnd);
 	outputStream.writeBytes(lineEnd);
 	
 	String tmp = "12345-test1\r\n";
 	//outputStream.write(tmp, 0, tmp.length());
 	outputStream.writeBytes(tmp);
 	
 	outputStream.writeBytes(lineEnd);
 	outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
 	outputStream.flush();
 
 	//int serverResponseCode = connection.getResponseCode();
 	String serverResponseMessage = connection.getResponseMessage();
 	
 	//Log.e(TAG, serverResponseCode);
 	Log.e(TAG, serverResponseMessage);
 	
 	outputStream.close();
 } 
 
 catch (UnknownHostException e) 
 {
 	Log.e(TAG, "Server Not Found");
 } 
 
 catch (IOException e) 
 {
 	Log.e(TAG, "Couldn't open socket");
 }
 */    
 
 
 
 
 
 /*        	
 Socket socket = new Socket("mamont.be", 80);
 
 //InetAddress server = Inet4Address.getByName("mamont.be");
 //InetAddress server = Inet4Address.getByAddress(new byte[] { (byte)192, (byte)168, 0, 101 });
 //Socket socket = new Socket(server, 80);
 OutputStream out = socket.getOutputStream();
 
 PrintWriter output = new PrintWriter(out);
 output.print("GET\r\n");
 BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
 
 //String str = input.readLine();
 //Log.e("111", str);
 
 int bytesAvailable = input.available();
 
 buffer = new byte[1000];
 
 input.read(buffer, 0, 1000);
 */        	
 
 
 	/*public class AlarmReceiver extends BroadcastReceiver
 	{
 		public void onReceive(Context context, Intent intent) 
 		{
 			Log.i(TAG, "AlarmReceiver::onReceive");
 		}
 	};*/
 	
 	
 					//Camera.Parameters parameters = camera.getParameters();
 				//Camera.Size size = getBestPreviewSize(width, height, parameters);
 			
 				//if (size != null) 
 				//{
 					//parameters.setPreviewSize(size.width, size.height);
 					//camera.setParameters(parameters);
 					
 					
 					
 					
 					
 					
 /*						private Camera.Size getBestPreviewSize(int width, int height, Camera.Parameters parameters) 
 	{
 		Camera.Size result = null;
 
 		for (Camera.Size size : parameters.getSupportedPreviewSizes()) 
 		{
 			if (size.width <= width && size.height <= height) 
 			{
 				if (result == null) 
 				{
 					result = size;
 				}
 				else 
 				{
 					int resultArea = result.width * result.height;
 					int newArea = size.width * size.height;
 
 					if (newArea > resultArea) 
 					{
 						result = size;
 					}
 				}
 			}
 		}
 
 		return(result);
 	}
 */
 
 /*			Handler handler = new Handler();
 			handler.postDelayed(new Runnable() 
 			{
 			  public void run() 
 			  {
 					//startPreview();
 			  }
 			}, 1000);
 */
