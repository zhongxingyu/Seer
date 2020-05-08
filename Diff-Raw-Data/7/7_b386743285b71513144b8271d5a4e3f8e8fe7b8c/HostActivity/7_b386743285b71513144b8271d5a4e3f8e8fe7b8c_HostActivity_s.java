 package com.battlegame;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.ServerSocket;
 import java.net.Socket;
 
 import android.app.Activity;
 import android.net.wifi.WifiInfo;
 import android.net.wifi.WifiManager;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.TextView;
 import android.widget.ToggleButton;
  
 // code from http://www.edumobile.org/android/android-development/socket-programming/
 public class HostActivity extends Activity {
    ServerSocket ss = null;
    static String mClientMsg = "";
    Thread myCommsThread = null;
    private ToggleButton connectBtn;
    //private EditText port;
 
    private TextView ipAddressText;
    private static TextView tv;
 
    // default ip
    public static String SERVERIP = "192.168.1.36";
 
    // designate a port
    protected static final int MSG_ID = 0x1337;
    public static final int SERVERPORT = 8080;
 
    public boolean firstPress = true;
    
    public String getLocalIpAddress() {
 	   WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
 	   WifiInfo wifiInfo = wifiManager.getConnectionInfo();
 	   int ipAddress = wifiInfo.getIpAddress();
 	   return ( ipAddress         & 0xFF) + "." +
        		  ((ipAddress >>  8 ) & 0xFF) + "." +
               ((ipAddress >> 16 ) & 0xFF) + "." +
               ((ipAddress >> 24 ) & 0xFF);
 	}
  
    @Override
    public void onCreate(Bundle savedInstanceState) {
 	    super.onCreate(savedInstanceState);
 	    setContentView(R.layout.activity_host);
 	    
 	    tv = (TextView) findViewById(R.id.serverStatus);
 	    ipAddressText = (TextView) findViewById(R.id.ipAddress);
 	    connectBtn = (ToggleButton) findViewById(R.id.toggleServer);
 	   
 		try {
 		    SERVERIP = getLocalIpAddress();
 			ipAddressText.setText("Your local IP Address is " + SERVERIP);
 	    }
 	    catch(Exception e) {
 	    	ipAddressText.setText(e.getMessage());
 	    }
 		
 	    tv.setText("Nothing from client yet");
 
 	    connectBtn.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 		        // Is the toggle on?
 		        boolean on = ((ToggleButton) v).isChecked();
 		        
 		        if (on) {
 		            // Enable vibrate
 		        	ipAddressText.setText("toggle on");
 		        	if(firstPress) {
		            	myCommsThread = new Thread(new CommsThread());
 		        	    myCommsThread.start();
 		        	    firstPress = false;
 		        	}
 		        	else {
 			        	myCommsThread.interrupt();
 		        	}
 		        } else {
 		            // Disable vibrate
 		        	ipAddressText.setText("toggle off");
 		        	myCommsThread.interrupt();
 		        }
 		    }
 	    });
    }
  
    @Override
    protected void onStop() {
 	    super.onStop();
 	    try {
 	        // make sure you close the socket upon exiting
 	        ss.close();
 	    } catch (IOException e) {
 	        e.printStackTrace();
 	    }
    }
  
    static Handler myUpdateHandler = new Handler() {
 	    public void handleMessage(Message msg) {
 	        switch (msg.what) {
 	        case MSG_ID:
 	            tv.setText(mClientMsg);
 	            break;
 	        default:
 	            break;
 	        }
 	        super.handleMessage(msg);
 	    }
    };
 
    class CommsThread implements Runnable {
 		public void run() {
 	        Socket s = null;
 	        try {
 	            ss = new ServerSocket(SERVERPORT);
 	        } catch (IOException e) {
 	            e.printStackTrace();
 	        }
 	        while (!Thread.currentThread().isInterrupted()) {
 	            Message m = new Message();
 	            m.what = MSG_ID;
 	            try {
 	                if (s == null)
 	                    s = ss.accept();
 	                BufferedReader input = new BufferedReader(new InputStreamReader(s.getInputStream()));
 	                String st = null;
 	                st = input.readLine();
 	                mClientMsg = st;
 	                myUpdateHandler.sendMessage(m);
 	            } catch (IOException e) {
 	                e.printStackTrace();
 	            }
 	        }
 	    }
     }
 }
