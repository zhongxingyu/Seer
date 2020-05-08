 package com.example.com.andrey.app;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import android.R.bool;
 import android.net.wifi.WpsInfo;
 import android.net.wifi.p2p.WifiP2pDevice;
 import android.net.wifi.p2p.WifiP2pManager;
 import android.net.wifi.p2p.WifiP2pManager.ActionListener;
 import android.net.wifi.p2p.WifiP2pManager.Channel;
 import android.net.wifi.p2p.WifiP2pManager.ChannelListener;
 import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
 import android.os.Bundle;
 import android.os.Handler;
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.app.ActivityManager;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.util.TypedValue;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.Switch;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TextView;
 
 @SuppressLint("NewApi")
 public class MainActivity extends Activity
 {
     Button button ,btnDisconnect , btnP2pConnect,btnP2pDiscover, btnExit,
     	btnProcessList ,btnClear,btnPreintP2pDevices;
     public TextView text, txtGo, txtDevicesCount,txtDiscoverStatus;
     TableLayout tblDevices;
     Coordinates coor ;
     WifiP2pManager mManager;
     Channel mChannel;
     WiFiDirectBroadcastReceiver mReceiver;
     IntentFilter mIntentFilter;
     Boolean enableOrientationPrint = false;
     PeerListListener myPeerListListener;
     tools tls = new tools();
 
     
     private void setGuiObjects()
     {
 		coor 			= new Coordinates();
 	    button 			= (Button) findViewById(R.id.btnCpuInfo);
 	    btnProcessList 	= (Button) findViewById(R.id.btnShowProcessList);
 	    txtGo 			= (TextView) findViewById(R.id.txtGo);
 	    text 			= (TextView) findViewById(R.id.textView1);
 	    txtDevicesCount = (TextView) findViewById(R.id.txtDevicesCount);
 	    txtDiscoverStatus = (TextView) findViewById(R.id.txtDiscoverStatus);
 	    btnP2pConnect	= (Button)  findViewById(R.id.P2PConnect);
 	    btnP2pDiscover  = (Button)  findViewById(R.id.P2PDiscover);
 	    btnClear  		= (Button)  findViewById(R.id.btnClear);
 	    btnDisconnect	= (Button)  findViewById(R.id.btnDisconnect);
 	    btnPreintP2pDevices = (Button)  findViewById(R.id.btnPreintP2pDevices);
 	    tblDevices 		= (TableLayout) findViewById(R.id.tblDevices);
 	    btnExit			= (Button)  findViewById(R.id.btnExit);
     }
     
     private void initGuiListeners()
     {
     	
     	
         button.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
             	//
             	text.setText(tls.ReadCPUinfo());
             }
         });
         btnExit.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
             	CloseApp();
             }
         });
         btnProcessList.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
             	text.setText(tls.getProcessList((ActivityManager) getSystemService(ACTIVITY_SERVICE)));
             }
         });  
         
         btnPreintP2pDevices.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
             	tblDevices.removeAllViews();
         		Collection<WifiP2pDevice> devs = mReceiver.getP2PDevices();
         		for (WifiP2pDevice dev : devs){
         			TableRow tr = new TableRow(getBaseContext());
         			TextView dev_addr = new TextView(getBaseContext());
         			TextView dev_name = new TextView(getBaseContext());
 
         			//AppendToText(dev.deviceAddress + " " + dev.deviceName +  " ");
         			dev_addr.setText(dev.deviceAddress);
        			dev_addr.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
         			dev_addr.setWidth(380);
         			dev_addr.setOnClickListener( new ConnectToPeerByMax());
         			
         			dev_name.setPadding(20, 0, 0, 0);
         			dev_name.setText(dev.deviceName);
         			dev_name.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
         			dev_name.setTextIsSelectable(true);
         			tr.addView(dev_addr);
         			tr.addView(dev_name);
         			tblDevices.addView(tr);
         		}
         		
             }
         }); 
         
         btnClear.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
             	text.setText("");
             }
         });  
         btnDisconnect.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
             	mReceiver.APIP2pRemoveGroup();
             }
         });
         btnP2pConnect.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
             		mReceiver.APIP2PConnect("0c:8b:fd:5f:13:89");// "CE:3A:61:B7:D7:B2");
             }
         });
         
         btnP2pDiscover.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
             	mReceiver.APIP2PDiscoverPeers();
             }
         });
     }
     
     private void initP2p()
     {
 		try
 		{
 			mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
 		    mChannel = mManager.initialize(this, getMainLooper(), new ChannelListener() {
 				
 				public void onChannelDisconnected() {
 					AppendToText("On Channel Disconnected");
 				}
 			});
 		    mReceiver = new WiFiDirectBroadcastReceiver(mManager, mChannel, this);
 		    mReceiver.SetDeviceMacAddress("CE:3A:61:B7:D7:B2".toLowerCase());
 		    mIntentFilter = new IntentFilter();
 		    mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
 		    mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
 		    mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
 		    mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
 		}
 		catch(Exception ex)
 		{
 			AppendToText("Error:" + ex.getMessage());
 		}
     }
 	@Override
 	protected void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		this.initP2p();
 		setContentView(R.layout.activity_main);
 		
 		this.setGuiObjects();
 		this.initGuiListeners();
 		txtDiscoverStatus.setText(tls.getMACAddress("p2p0"));
 		
 	    final Handler handler = new Handler();
 	    Timer timer = new Timer();
 
 	    timer.scheduleAtFixedRate(new TimerTask() {
 	        @Override
 	        public void run() {
 	            handler.post(new Runnable() {
 	                public void run() {
 	                	txtGo.setText(mReceiver.getConnectionStatus());
 	                	txtDevicesCount.setText("Devices count: " + mReceiver.APIP2PgetDevicesCount());
 	                }
 	            });
 	        }
 	    },100,100);
 	}
 
 	public void CloseApp()
 	{
 		this.finish();
 	    System.exit(0);
 	}
 	
     private void PrintToText(String string)
     {
     	text.setText(string);
     }
     
     public void AppendToText(String string)
     {
     	text.append("\n" + string);
     }
     
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
     @Override
     protected void onResume() {
         // Ideally a game should implement onResume() and onPause()
         // to take appropriate action when the activity looses focus
         super.onResume();
         coor.Start();
         registerReceiver(mReceiver, mIntentFilter);
 
 
     }
 
     @Override
     protected void onPause() {
         // Ideally a game should implement onResume() and onPause()
         // to take appropriate action when the activity looses focus
         super.onPause();
         coor.Stop();
         mReceiver.clearLocalServices();
         unregisterReceiver(mReceiver);
 
     }
     
 
 	class ConnectToPeerByMax implements OnClickListener
 	{
 
 		@Override
 		public void onClick(View v) {
 			TextView txt = (TextView)v;
 			mReceiver.APIP2PConnect((String) txt.getText());
 		}
 		
 	}
 
 	class Coordinates implements SensorEventListener
 	{
 	    private SensorManager mSensorManager;
 	    // Create a constant to convert nanoseconds to seconds.
 	    private static final float NS2S = 1.0f / 1000000000.0f;
 	    private final float[] deltaRotationVector = new float[4];
 	    private float timestamp;
 	    private Sensor mRotationVectorSensor;
 	    
 	    public void Stop()
 	    {
 	    	mSensorManager.unregisterListener(this);
 	    }
 	    public void Start()
 	    {
 	    	mSensorManager.registerListener(this, mRotationVectorSensor,SensorManager.SENSOR_ORIENTATION);
 	    }
 	    public Coordinates()
 	    {
 			mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
 			mRotationVectorSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
 			mSensorManager.registerListener(this, mRotationVectorSensor,SensorManager.SENSOR_ORIENTATION);
 	    }
 	    
 		@Override
 		public void onAccuracyChanged(Sensor sensor, int accuracy) {
 			// TODO Auto-generated method stub
 			
 		}
 
 		@Override
 		public void onSensorChanged(SensorEvent event) {
 			 float axisX = 0 ,axisY=0,axisZ=0;
 			 axisX = event.values[0];
 			 axisY = event.values[1];
 			 axisZ = event.values[2];
 			 if(enableOrientationPrint)
 			 {
 				 text.setText(
 						 "\nAxis X: " + (double)Math.round(axisX*100)/100 +
 						 "\nAxis Y: " + (double)Math.round(axisY*100)/100 +
 						 "\nAxis Z: " + (double)Math.round(axisZ*100)/100
 						 );
 			 }
 			 return;
 			 /*
 			 // This timestep's delta rotation to be multiplied by the current rotation
 			 // after computing it from the gyro sample data.
 			 if (timestamp != 0)
 			 {
 				 final float dT = (event.timestamp - timestamp) * NS2S;
 				 // Axis of the rotation sample, not normalized yet.
 				 axisX = event.values[0];
 				 axisY = event.values[1];
 				 axisZ = event.values[2];
 
 				 // Calculate the angular speed of the sample
 				 float omegaMagnitude = (float) Math.sqrt(axisX*axisX + axisY*axisY + axisZ*axisZ);
 
 				 // Normalize the rotation vector if it's big enough to get the axis
 				 // (that is, EPSILON should represent your maximum allowable margin of error)
 				 if (omegaMagnitude > 0.000000000001)
 				 {
 					 axisX /= omegaMagnitude;
 					 axisY /= omegaMagnitude;
 					 axisZ /= omegaMagnitude;
 				 }
 
 				// Integrate around this axis with the angular speed by the timestep
 				// in order to get a delta rotation from this sample over the timestep
 				// We will convert this axis-angle representation of the delta rotation
 				// into a quaternion before turning it into the rotation matrix.
 				float thetaOverTwo = omegaMagnitude * dT / 2.0f;
 				float sinThetaOverTwo = (float) Math.sin(thetaOverTwo);
 				float cosThetaOverTwo = (float) Math.cos(thetaOverTwo);
 				deltaRotationVector[0] = sinThetaOverTwo * axisX;
 				deltaRotationVector[1] = sinThetaOverTwo * axisY;
 				deltaRotationVector[2] = sinThetaOverTwo * axisZ;
 				deltaRotationVector[3] = cosThetaOverTwo;
 			 }
 			 
 			 timestamp = event.timestamp;
 			 float[] deltaRotationMatrix = new float[9];
 			 SensorManager.getRotationMatrixFromVector(deltaRotationMatrix, deltaRotationVector);
 			    // User code should concatenate the delta rotation we computed with the current rotation
 			    // in order to get the updated rotation.
 			    // rotationCurrent = rotationCurrent * deltaRotationMatrix;
 			 text.setText("\n0: " + deltaRotationVector[0] + 
 					 "\n1: " + deltaRotationVector[1] + 
 					 "\n2: " + deltaRotationVector[2] + 
 					 "\n3: " + deltaRotationVector[3] +
 					 "\nAxis X: " + axisX +
 					 "\nAxis Y: " + axisY +
 					 "\nAxis Z: " + axisZ +
 					 "\nDR 1 :: " + deltaRotationMatrix[0] + " " + deltaRotationMatrix[1] + " " + deltaRotationMatrix[2] +
 					 "\nDR 2 :: " + deltaRotationMatrix[3] + " " + deltaRotationMatrix[4] + " " + deltaRotationMatrix[5] +
 	 				 "\nDR 3 :: " + deltaRotationMatrix[6] + " " + deltaRotationMatrix[7] + " " + deltaRotationMatrix[8]);
 			 text.append(event.toString());*/
 			
 		}
 		
 
 	}
 }
