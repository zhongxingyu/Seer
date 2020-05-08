 // Football.java - control Arduino over Bluetooth to play ball
 // (c) Tero Karvinen & Kimmo Karvinen http://sulautetut.fi
 
 package fi.sulautetut.android.football;
 
 import fi.sulautetut.android.football.DeviceListActivity;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.pm.ActivityInfo;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Vibrator;
 import android.util.Log;
 import android.view.WindowManager;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.graphics.RectF;
 import android.view.View;
 
 public class Football extends Activity implements SensorEventListener {
 	
    String robotBtAddress=null; // Change this 
     TextView statusTv;
     TextView messagesTv;
     TBlue tBlue; 
     SensorManager sensorManager;
     Sensor sensor;
     float g=9.81f; // m/s**2
     float x, y, z, l, r;
     boolean kick; 
     int skipped; // continuously skipped sending because robot not ready
     Handler timerHandler; 
     Runnable sendToArduino; 
     
     private StatusView tStatus;
     
     private static final int REQUEST_CONNECT_DEVICE = 1;
     private static final int MENU_SCAN = 1;
     private static final int MENU_CONNECT = 2;
     private static final int MENU_DISCONNECT = 3;
     
     /*** Main - automatically called methods ***/
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
         initGUI();
         timerHandler = new Handler();
         sendToArduino = new Runnable() {
             public void run() {
                 sendLR();
                 timerHandler.postDelayed(this, 250); 
             }
         };
     }
 
     @Override
     public void onResume() 
     {
         super.onResume(); 
         initAccel();
 


     } 
 
     @Override
     public void onPause() {
         super.onPause();
         stopController();
         msg("Paused. \n");
     }
 
     @Override
     public void onSensorChanged(SensorEvent event) {
         x=event.values[1]/g;    // earth gravity along axis results 1.0
         y=event.values[2]/g;
         z=event.values[0]/g;
         updateLR();
     }
 
     @Override
     public void onAccuracyChanged(Sensor sensor, int accuracy) {
         // Must have when Activity implements SensorEventListener. 
     }
 
 
 
     /*** User interface ***/
     @Override
     public boolean onCreateOptionsMenu(Menu menu) { 
     	menu.add(Menu.NONE, MENU_SCAN, 0, "Choose Device"); 
        	menu.add(Menu.NONE, MENU_CONNECT, 0, "Connect"); 
         menu.add(Menu.NONE, MENU_DISCONNECT, 0, "Disconnect"); 
 
         
     	return true;
     }
     
     @Override
     public boolean onPrepareOptionsMenu (Menu menu) {
     	if (robotBtAddress == null) {
     		menu.findItem(MENU_CONNECT).setEnabled(false);
     	} else {
     		menu.findItem(MENU_CONNECT).setEnabled(true);
     		
     	}
     	if (tBlue == null) {
     		menu.findItem(MENU_DISCONNECT).setEnabled(false);
     	} else {
     		menu.findItem(MENU_DISCONNECT).setEnabled(true);
     	}
         return true;
     }
     
     void initGUI()
     {
         // Window
         setRequestedOrientation(
                 ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
         getWindow().setFlags(
                 WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, 
                 WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
         // Contents 
         LinearLayout container=new LinearLayout(this);
         container.setOrientation(android.widget.LinearLayout.VERTICAL);
         statusTv = new TextView(this);
         statusTv.setBackgroundColor(Color.WHITE);
         statusTv.setTextColor(Color.BLACK);
         setStatus();
         
         Log.i("FB", "User interface half way.. ");
         container.addView(statusTv);
         //msg("statusTv added. ");  
         messagesTv = new TextView(this);
         messagesTv.setText("");
         container.addView(messagesTv);
         
         tStatus = new StatusView(this);
         container.addView(tStatus);
         
         
         setContentView(container); 
     }
     
     void setStatus() {
     	
     	if (tBlue != null) {
             statusTv.setText("Connected. To disconnect, press Menu and disconnect.");
 
     	} else {
     		if (robotBtAddress == null) {
     			statusTv.setText("Not connected. Press Menu, then choose a device.");
     		} else {
     			statusTv.setText("Not connected. Press Menu, then connect.");
     		}
     	}
     	
     }
 
     
     public class StatusView extends View {
     	private int l = 3;
     	private int r = 3;
     	
     	public void setL(int newval) {
     		if (newval < 0) {
     			newval = Math.abs(newval);
     			l = (newval * 0xFF/100) << 16;
     		
     		} else {
     			l = (newval * 0xFF/100) << 8;
     		}
     	}
     	public void setR(int newval) {
     		Log.i("BOT", "New R: " + newval );
     		if (newval < 0) {
     			newval = Math.abs(newval);
     			r = (newval * 0xFF/100) << 16;
     		
     		} else {
     			r = (newval * 0xFF/100) << 8;
     		}
     		Log.i("BOT", "Set r to: " + r );
     	}
     	public StatusView(Context context) {
     		super(context);
     	}
     	
     	public void onDraw(Canvas canvas) {
     		Paint paint = new Paint();
     		
     		int basecolor = 0xFF000000;
     		RectF oval = new RectF(0, 0,
     								canvas.getWidth(),
     								canvas.getHeight()/2);
     		
     		paint.setColor(l | basecolor);
     		canvas.drawArc(oval, 90, 180, true, paint); 
     		paint.setColor(r | basecolor);
     		canvas.drawArc(oval, 270, 180, true, paint); 
     		
     	}
     	
     }
 
     public void msg(String s)
     {
         Log.i("FB", s);
         //if (2<=messagesTv.getLineCount()) messagesTv.setText("");
         messagesTv.setText(s);
     }
 
     void vibrate()
     {
         Vibrator vibra = (Vibrator) getSystemService(
                 Context.VIBRATOR_SERVICE);
         vibra.vibrate(200); 
     } 
 
 
 
     /*** Accelerometer ***/
 
     void initAccel()
     {
         msg("Accelerometer initialization... ");
         sensorManager=(SensorManager) getSystemService(SENSOR_SERVICE);
         sensor=sensorManager.getDefaultSensor(
                 Sensor.TYPE_ACCELEROMETER);
         sensorManager.registerListener(
                 this, 
                 sensor, 
                 SensorManager.SENSOR_DELAY_NORMAL);
     }
 
     void closeAccel()
     {
         msg("Accelerometer closing... ");
         sensorManager.unregisterListener(this, sensor);
     }
 
 
 
     /*** Bluetooth ***/
 
     public void onActivityResult(int requestCode, int resultCode, Intent data) {
         switch (requestCode) {
         case REQUEST_CONNECT_DEVICE:
             // When DeviceListActivity returns with a device to connect
             if (resultCode == Activity.RESULT_OK) {
                 // Get the device MAC address
             	robotBtAddress = data.getExtras()
                                      .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
             	setStatus();
             }
             break;
         }
     }
 
     public void startController() {
         msg("Preparing... ");
         timerHandler.postDelayed(sendToArduino, 1000); 
         skipped=9999; // force Bluetooth reconnection
         msg("Attempting connection... ");
 
     }
     public void stopController() {
 
     	closeAccel();
         
     	r = 0; 
         l = 0;
         if (tBlue != null) {
         	sendLR();
         	closeBluetooth();
         }
 
         timerHandler.removeCallbacks(sendToArduino);    
     }
 
     public boolean onOptionsItemSelected(MenuItem item) {
     	switch (item.getItemId()) {
     	case MENU_CONNECT:
     		startController();
     		return true;
     	case MENU_DISCONNECT:
     		stopController();
     		return true;
     	case MENU_SCAN:
             Intent serverIntent = new Intent(this, DeviceListActivity.class);
             startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
             return true;
     	}
     	return false;
     }
     
     void initBluetooth()
     {
         msg("Bluetooth initialization... ");
         skipped=0; 
         tBlue=new TBlue(robotBtAddress);
         if (tBlue.streaming()) {
             setStatus();
         } else {
             msg("Bluetooth connection failed. Is Bluetooth on? Are you paired with the module?");
         }
     }
 
     void closeBluetooth()
     {
         msg("Bluetooth closing...");
         tBlue.close();
         tBlue = null;
         msg("Ready...");
         setStatus();
     }
 
 
     /*** Motor calculations for left and right ***/
 
     void updateLR()
     {
         kick=false;
         if (1.5<Math.abs(y)) kick=true; 
         l=y;
         r=l;
         l+=x;
         r-=x;
 
         if (l+r<0) { // make reverse turn work like in a car
             float tmp=l;
             l=r;
             r=tmp;
         }
 
         l=constrain(l);
         r=constrain(r);
     }
 
     float constrain(float f) 
     {
         if (f<-1) f=-1;
         if (1<f) f=1;
         return f;
     }
 
     void sendLR()
     {
         if ( (skipped>20) ) {
             closeAccel();
             initAccel();
             initBluetooth();
         }
         if (!tBlue.streaming()) {
             //msg("0");
             skipped++;
             return;
         }
 
         String s="";
         s+="S";
         s+=(char) Math.floor(l*5 + 5);    // 0 <= l <= 10
         s+=(char) Math.floor(r*5 + 5);    // 0 <= l <= 10
         if (kick) s+="k"; else s+="-";
         s+="U";
 
         //statusTv.setText(String.format(
         //        "%s    left: %3.0f%% right: %3.0f%%, kick: %b.", 
         //        s, Math.floor(l*100), Math.floor(r*100), 
         //        kick));
         
        tStatus.setL( (int) Math.floor(l*100) );
         tStatus.setR( (int) Math.floor(r * 100) );
         tStatus.invalidate();
 
         tBlue.write("?");
         String in=tBlue.read(); 
         //msg(""+in);
         if (in.startsWith("L") && tBlue.streaming()) {
             Log.i("fb", "Clear to send, sending... ");
             tBlue.write(s); 
             skipped=0;
             msg("Bluetooth transmissions OK.");
         } else {
             Log.i("fb", "Not ready, skipping send. in: \""+ in+"\"");
             skipped++;
             msg("Not ready. Skipping transmission.");
         }
         if (kick) vibrate();
     }
 
 }
