 package com.ol.research.photographer;
 
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.NetworkInterface;
 import java.net.ServerSocket;
 import java.net.SocketException;
 import java.util.Enumeration;
 
 import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.util.InetAddressUtils;
 import org.apache.http.impl.client.BasicResponseHandler;
 import org.opencv.android.BaseLoaderCallback;
 import org.opencv.android.LoaderCallbackInterface;
 import org.opencv.android.OpenCVLoader;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.hardware.Camera;
 import android.hardware.Camera.PictureCallback;
 import android.hardware.Camera.ShutterCallback;
 import android.net.http.AndroidHttpClient;
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 import android.view.SurfaceView;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.ol.research.measurement.*;
 
 /**
  * @author Milan Tenk
  * MainActivity of the application (UI thread)
  * It starts the CommsThread, and if picture was taken, the SendImageService
  * 
  * Callbacks:
  * PictureCallback: if the taken photo is ready, here will be the SendImageService started, the picture can be saved to SD card
  * ShutterCallback: right after taking photo
  * BaseLoaderCallback: needed for OpenCV initialization
  * 
  * Handler: handles the messages, that is written to the screen
  * 
  * Methods:
  * onCreate: Will be called at the start of the activity, Commsthread will be started
  * onResume: Called, when the user is interfacing with the application. Contains the initialization of OpenCV
  * onStop: Called, when the activity is stopped
  * getLocalIpAddress: The IP address of the Phone can be determined
  * httpReg: Registration of the phone on the Server
  */
 
 public class MainActivity extends Activity {
 
 	 protected static final int MSG_ID = 0x1337;
 	 protected static final int SERVERPORT = 6000;
 	 protected static final int TIME_ID = 0x1338;
 	 private boolean saveToSD = false;
 	 private static final String  TAG = "TMEAS";
 	 //static ServerSocket ss = null;
 	 static String mClientMsg = "";
 	 static byte[] lastPhotoData;
 	 static long OnShutterEventTimestamp;
 	 
 	 Camera mCamera;
 	 Thread myCommsThread = null;
 	 String current_time = null;
 	 
 	 private PictureCallback mPicture = new PictureCallback() {
 
 	        @Override
 	        public void onPictureTaken(byte[] data, Camera camera) {
 	        	//TempTickCountStorage.OnPictureTakenEvent = TempTickCountStorage.GetTimeStamp();
 	        	CommsThread.TM.Stop(CommsThread.PostProcessJPEGMsID);
 	        	CommsThread.TM.Start(CommsThread.PostProcessPostJpegMsID);
 	        	//Option to save the picture to SD card
 	        	if(saveToSD)
 	        	{
 		        	String pictureFile = Environment.getExternalStorageDirectory().getPath()+"/custom_photos"+"/__1.jpg";
 		            try {
 		                FileOutputStream fos = new FileOutputStream(pictureFile);
 		                fos.write(data);
 		                fos.close();   
 		                
 		            } catch (FileNotFoundException e) {
 		                Log.d("Photographer", "File not found: " + e.getMessage());
 		            } catch (IOException e) {
 		                Log.d("Photographer", "Error accessing file: " + e.getMessage());
 		            }
 		            Log.v("Photographer", "Picture saved at path: " + pictureFile);
 	        	}
 	            
 	            Intent intent = new Intent(MainActivity.this, SendImageService.class);
 				//intent.putExtra("BYTE_ARRAY", data);
 	            lastPhotoData = data;
 				intent.putExtra("TIMESTAMP",OnShutterEventTimestamp);
 				startService(intent);            	            
 	        }
 	    };
 	    
 	    private ShutterCallback mShutter = new ShutterCallback()
 	    {
 	    	@Override
 	    	public void onShutter()
 	    	{
     			//TempTickCountStorage.OnShutterEvent = TempTickCountStorage.GetTimeStamp();
 	    		OnShutterEventTimestamp = TimeMeasurement.getTimeStamp();
 	            current_time = String.valueOf(OnShutterEventTimestamp); 
 	    		CommsThread.TM.Stop(CommsThread.TakePictureMsID);   
 	    		CommsThread.TM.Start(CommsThread.PostProcessJPEGMsID);
 	    		Message timeMessage = new Message();
 	    		timeMessage.what = TIME_ID;
 	            myUpdateHandler.sendMessage(timeMessage);
 	    	}
 	    };
 	       
 	    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
 	        @Override
 	        public void onManagerConnected(int status) {
 	            switch (status) {
 	                case LoaderCallbackInterface.SUCCESS:
 	                {
 	                	TimeMeasurement.isOpenCVLoaded = true;
 	                    Log.i(TAG, "OpenCV loaded successfully");
 	                    
 	                } break;
 	                default:
 	                {
 	                    super.onManagerConnected(status);
 	                    
 	                } break;
 	            }
 	        }
 	    };   
 	    
 		//Handler a socketzenet szmra
 	 	private Handler myUpdateHandler = new Handler() {
 	 		@Override
 	 	    public void handleMessage(Message msg) {
 	 	        switch (msg.what) {
 	 	        case MSG_ID:
 	 	            TextView tv = (TextView) findViewById(R.id.TextView_receivedme);
 	 	            tv.setText(mClientMsg);
 	 	            break;
 	 	       case TIME_ID:
 		        	TextView tv2 = (TextView) findViewById(R.id.TextView_timegot);
 		        	tv2.setText(current_time);
 		        	break;
 	 	        default:
 	 	        	
 	 	            break;
 	 	        }
 	 	        super.handleMessage(msg);
 	 	    }
 	 	   };
 	    
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		
 		final Button btnHttpGet = (Button) findViewById(R.id.btnHttpGet);
 		btnHttpGet.setOnClickListener(new OnClickListener(){
 			@Override
 			public void onClick (View arg0) {
 				new Thread() {
 					public void run() {
 						httpReg();
 					}
 				}.start();
 			}
 		});
 		
 		//take-picture button
 		/*final Button captureButton = (Button) findViewById(R.id.buttonPhoto);
 		captureButton.setOnClickListener(
 				new View.OnClickListener() {
 					
 					@Override
 					public void onClick(View v) {
 						mCamera.takePicture(null, null, mPicture);
 						//captureButton.setEnabled(false);
 					}
 				});	*/
 		
 		mCamera = Camera.open();
 
 // eredeti elnzet
 //		mPreview = new CameraPreview(this, mCamera);
 //		preview=(FrameLayout) findViewById(R.id.cameraPreview);
 //		preview.addView(mPreview);
 		
 		// az elnzeti kpet gy nem ltjuk
 		SurfaceView mview = new SurfaceView(getBaseContext());
 		try {
 			mCamera.setPreviewDisplay(mview.getHolder());
 			mCamera.startPreview();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			Log.e("Creating preview", "Error while creating preview " + e.toString());
 			//e.printStackTrace();
 		}
 		
 		//thread a socket zenetek szmra
 		myCommsThread = new Thread(new CommsThread(myUpdateHandler, mCamera, mPicture, mShutter));
 		myCommsThread.start();	
 	}
 	
     @Override
     public void onResume()
     {
         super.onResume();
         OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
 //        timeMeasurement = new TimeMeasurement();
 //        timeMeasurement.loadOpenCVAsync(this);
     }
 	
 	@Override
 	protected void onStop(){
 		if(mCamera != null)
 			{
 				mCamera.release();
 			}
 		super.onStop();
 		myCommsThread.interrupt(); //a socketkapcsolatra vrakoz thread-et hogyan rdemes kezelni?
 		
 		/*try {
 			ss.close();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}*/
 	}
 	
 	
 	public String getLocalIpAddress() {
 		try {
 			for (Enumeration en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
 				NetworkInterface intf = (NetworkInterface) en.nextElement();
 				for (Enumeration enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
 					InetAddress inetAddress = (InetAddress) enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress() && InetAddressUtils.isIPv4Address(inetAddress.getHostAddress())) {
						// temporary (?) solution to use only ipv4 address
 						return inetAddress.getHostAddress().toString();
 					}
 				}
 			}
 		} catch (SocketException ex) {
 			//Log.e(LOG_TAG, ex.toString());
 		}
 		return null;
 	} 
 	
 	private void httpReg()
 	{
 		AndroidHttpClient httpClient = null;
 		String IpAddress = new String(getLocalIpAddress());
 		try{
 			httpClient = AndroidHttpClient.newInstance("Android");
 			HttpGet httpGet = new HttpGet("http://avalon.aut.bme.hu/~kristof/smeyel/smeyel_reg.php?IP="+IpAddress);
 			final String response = httpClient.execute(httpGet, new BasicResponseHandler());
 			runOnUiThread(new Runnable() {
 				@Override
 				public void run()
 				{
 					Toast.makeText(MainActivity.this, response, Toast.LENGTH_LONG).show();
 				}
 			});
 		} catch (IOException e)
 		{
 			e.printStackTrace();
 		} finally{
 			if (httpClient != null)
 				httpClient.close();
 		}
 	}
 }
