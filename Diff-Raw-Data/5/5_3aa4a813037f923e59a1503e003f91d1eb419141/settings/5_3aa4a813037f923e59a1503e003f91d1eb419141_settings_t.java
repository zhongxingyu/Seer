 package com.zwad3.wifijoy;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.Socket;
 import java.util.Properties;
 
 import android.net.wifi.WifiInfo;
 import android.net.wifi.WifiManager;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.Message;
 import android.os.Messenger;
 import android.os.RemoteException;
 import android.app.Activity;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.ContextWrapper;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.content.SharedPreferences;
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.text.Editable;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.support.v4.app.NavUtils;
 import android.inputmethodservice.InputMethodService;
 
 
 public class settings extends Activity {
 	String ip;
 	FileServer fs;
 
     public void print(String s) {
     	Log.d("Wifi Joystick",s);
     }
     @Override
     public void onCreate(Bundle savedInstanceState) {
     	//print("hello");
     	//print(LoadPreferences("ip"));
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_settings);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_settings, menu);
         displayText();
         fillText();
         //KeyEvent k = new KeyEvent(KeyEvent.ACTION_UP,102);
         WifiJoy wj = new WifiJoy();
         return true;
     }
     private void fillText() {
     	if (LoadPreferences("port")==""&&true&&!false) {
     		return;
     	}
     	String lp = LoadPreferences("port").toString().trim();
     	print("1)"+lp);
     	EditText et = (EditText)findViewById(R.id.port);
     	et.setText(""+Integer.parseInt(lp));
     }
     public void buttonConnect(View v) {
     	print("Button Connected");
     	EditText port = (EditText)findViewById(R.id.port);
     	int a;
     	try {
     		a = Integer.parseInt(port.getText().toString());
     	} catch (Exception e) {
     		a = 8887;
     		print(e.toString());
     		failIp(-1);
     	}
     	SavePreferences("port",""+a);
     	
     	//openSocket(ip);hi
     	Intent i = new Intent("com.zwad3.wifijoy.GET_IP");
     	i.putExtra("port",""+a);
     	//i.setAction("com.zwad3.wifi")
     	sendBroadcast(i);
     }
     public void httpChange(View v) {
     	print("Changing HTTP state");
     	CheckBox cb = (CheckBox)findViewById(R.id.http);
     	if (cb.isChecked()) {
     		if (fs==null) {
     			try {
 					fs = new FileServer(8081, new File("file:///android_asset/"),this);
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
     		}
     	}
     	else if(true){
     		fs.stop();
     		fs = null;
     	}
     }
     private Messenger mService;
     private ServiceConnection mConnection = new ServiceConnection() {
         public void onServiceConnected(ComponentName className, IBinder service) {
             mService = new Messenger(service);
             //textStatus.setText("Attached.");
             try {
                 Message msg = new Message();//Message.obtain(null, MyService.MSG_REGISTER_CLIENT);
                 //msg.replyTo = mMessenger;
                 mService.send(msg);
             } catch (RemoteException e) {
                 // In this case the service has crashed before we could even do anything with it
             }
         }
         public void onServiceDisconnected(ComponentName className) {
             // This is called when the connection with the service has been unexpectedly disconnected - process crashed.
             mService = null;
             //textStatus.setText("Disconnected.");
         }
     };
 
     private void doBindService() {
         bindService(new Intent(this, WifiJoy.class), mConnection, Context.BIND_AUTO_CREATE);
         //mIsBound = true;
         //textStatus.setText("Binding.");;;;
     }
     private void failIp(int i,EditText e) {
     	print("Bad ip address: "+i);
     	e.setText("");
     }
     private void failIp(int i) {
     	print("Bad ip address. Please fill in all spaces");
     }
     public void displayText() {
     	TextView editText = (TextView) findViewById(R.id.ipaddr);
     	WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
     	WifiInfo wifiInfo = wifiManager.getConnectionInfo();
 
     	int ipAddress = wifiInfo.getIpAddress();
 
     	String ip = null;
 
     	ip = String.format("%d.%d.%d.%d",
     	(ipAddress & 0xff),
     	(ipAddress >> 8 & 0xff),
     	(ipAddress >> 16 & 0xff),
     	(ipAddress >> 24 & 0xff));
     	//print(ip);
     	if (!(ip.equals("0.0.0.0"))) {
     		editText.setText(ip);
     	} else {
     		editText.setText("Not Connected to the Interwebs");
     	}
     }
 
     /*private void openSocket(String s) {
     	ip = s;
     	//new Thread(new AsciiSocketController(ip,1234,new LogShell((TextView)findViewById(R.id.loger)))).start();
     }*/
     private void SavePreferences(String key, String value){
         SharedPreferences sharedPreferences = getPreferences(0);
         SharedPreferences.Editor editor = sharedPreferences.edit();
         editor.putString(key, value);
         editor.commit();
        }
       
     private String LoadPreferences(String mem){
       try {
     	  SharedPreferences sharedPreferences = getPreferences(0);
    	  String s = sharedPreferences.getString(mem, "8887");
     	  print(s);
     	  print("1");
     	  return s;
       } catch (Exception e) {
     	  print("2");
    	  return "8887";
       }
     }
 }
