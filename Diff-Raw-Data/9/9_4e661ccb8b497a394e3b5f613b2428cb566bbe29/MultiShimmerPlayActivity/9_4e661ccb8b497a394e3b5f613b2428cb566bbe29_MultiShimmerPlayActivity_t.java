 package com.shimmerresearch.multishimmer;
 
 import java.io.File;
 import java.lang.ref.WeakReference;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 
 import com.shimmerresearch.driver.FormatCluster;
 import com.shimmerresearch.driver.ObjectCluster;
 import com.shimmerresearch.driver.Shimmer;
 
 import com.shimmerresearch.multishimmerplay.R;
 import com.shimmerresearch.service.MultiShimmerPlayService;
 import com.shimmerresearch.service.MultiShimmerPlayService.LocalBinder;
 
 
 import android.app.Activity;
 import android.app.ActivityManager;
 import android.app.ActivityManager.RunningServiceInfo;
 import android.bluetooth.BluetoothAdapter;
 import android.content.BroadcastReceiver;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.ServiceConnection;
 import android.media.SoundPool;
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.Message;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.WindowManager;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.Toast;
 
 
 
 public class MultiShimmerPlayActivity extends Activity {
   private static final String TAG = "ServicesDemo";
   
   Button buttonStop;
 
 	MultiShimmerPlayService mService;
 	private Context mCtx;
 	private String currentSelectedBluetoothAddress;
 	private String currentSelectedDeviceName;
 	private int mCurrentSelectedSlot=-1;
 	ArrayList<String> Devices= new ArrayList<String>();
 	private String[] devices = new String [] {"Device","Device","Device","Device","Device","Device","Device","All Devices"};
 	boolean mServiceBind=false;
 	static final int REQUEST_ENABLE_BT = 1;
 	public final static int REQUEST_MAIN_COMMAND_SHIMMER=3;
 	public final static int REQUEST_CONNECT_SHIMMER=2;
 	public final static int REQUEST_COMMANDS_SHIMMER=4;
 	public static final int REQUEST_CONFIGURE_SHIMMER = 5;
 	public static final int REQUEST_LOGGING_SHIMMER = 6;
 	public static final int REQUEST_GRAPH_SHIMMER = 7;
 	public static final int REQUEST_CONFIGURE_GRAPH = 8;
 	private boolean mServiceFirstTime=true;
 	private static WeakReference<MultiShimmerPlayActivity> mWeakRef;
 	
 	
 	@Override
   public void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
     setContentView(R.layout.main);
     Log.d("ShimmerH","Oncreate");
     mWeakRef = new WeakReference<MultiShimmerPlayActivity>(this);
     getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
     if (!isMyServiceRunning())
     {
     	Log.d("ShimmerH","Oncreate2");
     	Intent intent=new Intent(this, MultiShimmerPlayService.class);
     	startService(intent);
     	if (mServiceFirstTime==true){
     		Log.d("ShimmerH","Oncreate3");
 			getApplicationContext().bindService(intent,mTestServiceConnection, Context.BIND_AUTO_CREATE);
 			mServiceFirstTime=false;
 		}
     	
     }
     
     
     createDirIfNotExists("multishimmerplay");
     
     
     mCtx=this;
     final ListView listViewDevices = (ListView) findViewById(R.id.listView1);
 
     
 	ArrayList<String> devicesList = new ArrayList<String>();  
 	devicesList.addAll( Arrays.asList(devices) );  
     ArrayAdapter<String> sR = new ArrayAdapter<String>(this, R.layout.commands_name,devicesList);
 	listViewDevices.setAdapter(sR);
 	
     
 	
     BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
     if(mBluetoothAdapter == null) {
     	Toast.makeText(this, "Device does not support Bluetooth\nExiting...", Toast.LENGTH_LONG).show();
     	finish();
     }
         
 	if(!mBluetoothAdapter.isEnabled()) {     	
     	Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
     	startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
 	}
 	
 	
 	
 	listViewDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
 
 		  public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
 
 		    Object o = listViewDevices.getItemAtPosition(position);
 		    Log.d("Shimmer",o.toString());
 		    mCurrentSelectedSlot = position;
 		    Intent mainCommandIntent=new Intent(MultiShimmerPlayActivity.this,MainCommandsActivity.class);
 		    mainCommandIntent.putExtra("LocalDeviceID",o.toString());
 		    mainCommandIntent.putExtra("CurrentSlot", position);
 		    mainCommandIntent.putExtra("requestCode", REQUEST_MAIN_COMMAND_SHIMMER);   
      		startActivityForResult(mainCommandIntent, REQUEST_MAIN_COMMAND_SHIMMER);
 		  }
 		});
   }
 	
 	  
   public void updateListView(){
 	  devices = new String [] {"Device","Device","Device","Device","Device","Device","Device","All Devices"};
 	  HashMap<String, Object> tempHMap=mService.mMultiShimmer;
 		Collection<Object> colS=tempHMap.values();
 		Iterator<Object> iterator = colS.iterator();
 		int i=0;
 		while (iterator.hasNext()) {
 			Shimmer stemp=(Shimmer) iterator.next();
 			devices[Integer.parseInt(stemp.getDeviceName())]=stemp.getBluetoothAddress(); //check name which is a number sequence?
 			i++;
 		}
 	  final ListView listViewDevices = (ListView) findViewById(R.id.listView1);
 	  ArrayList<String> devicesList = new ArrayList<String>();  
 	  devicesList.addAll( Arrays.asList(devices) );  
 	  ArrayAdapter<String> sR = new ArrayAdapter<String>(this, R.layout.commands_name,devicesList);
 	  listViewDevices.setAdapter(sR);
   }
   
   public void onPause(){
 	  super.onPause();
 	  
 	  	  if(mServiceBind == true){
 		  getApplicationContext().unbindService(mTestServiceConnection);
 	  }
 	 }
   
 public void onResume(){
 	super.onResume();
 
 	Intent intent=new Intent(MultiShimmerPlayActivity.this, MultiShimmerPlayService.class);
 	Log.d("ShimmerH","on Resume");
 	getApplicationContext().bindService(intent,mTestServiceConnection, Context.BIND_AUTO_CREATE);
 }
   
   private ServiceConnection mTestServiceConnection = new ServiceConnection() {
 
   	public void onServiceConnected(ComponentName arg0, IBinder service) {
   		// TODO Auto-generated method stub
   		Log.d(TAG, "srvice connected");
   		LocalBinder binder = (LocalBinder) service;
   		mService = binder.getService();
   		mServiceBind = true;
   		mService.setHandler(mHandler);
   		//update the view
 		updateListView();
   		
   	}
 
   	public void onServiceDisconnected(ComponentName arg0) {
   		// TODO Auto-generated method stub
   		mServiceBind = false;
   	}
     };
     
     
     public void onActivityResult(int requestCode, int resultCode, Intent data) {
     	switch (requestCode) {	
     	case REQUEST_MAIN_COMMAND_SHIMMER:
         // When DeviceListActivity returns with a device to connect
     		 if (resultCode == Activity.RESULT_OK) {
 	            String address = data.getExtras()
 	                    .getString("Address");
 	            String currentDeviceName = data.getExtras().getString("CurrentDevice");
 	            int currentSlot = data.getExtras().getInt("CurrentSlot");
 	            Log.d("ShimmerMainActivity",address);
 	            Log.d("ShimmerMainActivity",currentDeviceName);
 	            Intent intent=new Intent(this, MultiShimmerPlayService.class);
 	            currentSelectedBluetoothAddress=address;
 	            currentSelectedDeviceName=currentDeviceName;
 	            if (!isMyServiceRunning())
 	            {	
 	            	intent.putExtra("BluetoothAddress", address);
 	            	intent.putExtra("DeviceName", currentDeviceName);
 	            	intent.putExtra("CurrentSlot",currentSlot);
 	            	startService(intent);
 	            	//getApplicationContext().bindService(intent,mTestServiceConnection, Context.BIND_AUTO_CREATE);
 	            } else {
 	          		mService.connectShimmer(currentSelectedBluetoothAddress, Integer.toString(mCurrentSelectedSlot));
 	          		//getApplicationContext().unbindService(mTestServiceConnection);
 		            //
 		            //getApplicationContext().unbindService(mTestServiceConnection);
 	            }
     		 }
 
         break;
     	}
     }
     
     private boolean isMyServiceRunning() {
         ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
         for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
             if ("com.shimmerresearch.service.MultiShimmerPlayService".equals(service.service.getClassName())) {
                 return true;
             }
         }
         return false;
     }
     
     public static boolean createDirIfNotExists(String path) {
         boolean ret = true;
 
         File file = new File(Environment.getExternalStorageDirectory(), path);
         if (!file.exists()) {
         	Log.d("TravellerLog :: ", "creating file");
             if (!file.mkdirs()) {
                 Log.d("TravellerLog :: ", "Problem creating Image folder");
                 ret = false;
             } else {
             	Log.d("TravellerLog :: ", "file created");
             }
         }
         return ret;
     }
     private static Handler mHandler = new Handler() {
 
 		public void handleMessage(Message msg) {
 	        switch (msg.what) {
 	            case Shimmer.MESSAGE_STATE_CHANGE:
 	           	 switch (msg.arg1) {
 	           	 	case Shimmer.STATE_CONNECTED:
 		           	//check to see if there are other Shimmer Devices which need to be connected
 		           	//sendBroadcast(intent);
 		           	
 		                break;
 		            case Shimmer.STATE_CONNECTING:
  
 		                break;
 		            case Shimmer.STATE_NONE:
 		           	Log.d("Shimmer","NO_State" + ((ObjectCluster)msg.obj).mBluetoothAddress);
 		           	MultiShimmerPlayActivity activity = mWeakRef.get();
 		           	activity.updateListView();
 		           	/*for (ShimmerConfiguration sc:mShimmerConfigurationList){
 		           		if (sc.getBluetoothAddress().equals( ((ObjectCluster)msg.obj).mBluetoothAddress)){
 		           			position=pos+1;
 		           		}
 		           		pos++;
 		           	}
 		        	img = (ImageView) viewArray[position].findViewById(R.id.usericon);
 		        	img.setImageResource(R.drawable.locker_default);*/
 
 		           	//sendBroadcast(intent);
 		                break;
 		            case Shimmer.MSG_STATE_FULLY_INITIALIZED:
 		           	Log.d("ShimmerCA","Fully Initialized");
 		           	/*pos=0;
 		           	position=0;
 		           	for (ShimmerConfiguration sc:mShimmerConfigurationList){
 		           		if (sc.getBluetoothAddress().equals( ((ObjectCluster)msg.obj).mBluetoothAddress)){
 		           			position=pos+1;
 		           		}
 		           		pos++;
 		           	}
 		        	img = (ImageView) viewArray[position].findViewById(R.id.usericon);
 		        	img.setImageResource(R.drawable.locker_selected);*/
 
 		    		
 		        	
 		           		break;
 		            case Shimmer.MSG_STATE_STREAMING:
 			           	Log.d("ShimmerCA","Streaming");
 
 			    		break;
 		            case Shimmer.MSG_STATE_STOP_STREAMING:
 			           	Log.d("ShimmerCA","Streaming");
 
 			    		break;
 	            }
 	           	 break;
 
             }    
 		}
     };
   
   }
