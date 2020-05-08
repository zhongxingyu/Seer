 package com.tware.anexter;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.bluetooth.BluetoothAdapter;
 import android.content.Context;
 import android.content.Intent;
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.net.wifi.WifiManager;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Display;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TextView;
 
 public class ANexterActivity extends Activity{
     /** Called when the activity is first created. */
 	private Button bPass;
 	private WifiManager mWifi;
 	private BluetoothAdapter bAdapt;
 	private String TAG = "aNexter";
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         this.setTitle("aNexter");
         
         mWifi = (WifiManager)getSystemService(Context.WIFI_SERVICE);
         bAdapt= BluetoothAdapter.getDefaultAdapter();
         
         TextView vVer = (TextView)findViewById(R.id.view_version);
         try {
 			PackageInfo info = this.getPackageManager().getPackageInfo(this.getPackageName(), 0);
 			vVer.setText("aNexter v"+info.versionName);
 		} catch (NameNotFoundException e) {
 			vVer.setText("aNexter vnull");
 		}
         
         bPass = (Button)findViewById(R.id.btn_pass);       
         bPass.setOnClickListener(new OnClickListener(){
         	@Override
 			public void onClick(View v)
         	{
         		Intent i = new Intent();
         		i.putExtra("LOG", "|");
         		i.setClass(ANexterActivity.this , virtualKeys.class);
 //        		i.setClass(ANexterActivity.this, uHDMI.class);  // debug...
          		startActivity(i);
         		ANexterActivity.this.finish();
         	}
         });
         
         /**
          * Start a thread to enable WiFi and Bluetooth
          * */
         new Thread(new Runnable(){
 			@Override
 			public void run() {
 				if (mWifi != null){
 					Log.i(TAG, "Start to enable WiFi...");
 					mWifi.setWifiEnabled(true);
 				}
 				if (bAdapt != null){
 					Log.i(TAG, "Start to enable Bluetooth");
 					bAdapt.enable();
 				}
 			}
         }).start();
         
     }
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         super.onCreateOptionsMenu(menu);
         menu.add(0, 0, 0, "Quit");
         menu.add(0, 1, 1, "Configuration");
         menu.add(0, 2, 2, "Logs");
         menu.add(0, 3, 3, "About");
         return true;
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         super.onOptionsItemSelected(item);
         switch(item.getItemId())
         {
     		case 0:
    			finish();
     			return true;
     		case 1:
     			Display display = getWindowManager().getDefaultDisplay();
     			long memSize;
     			String mMCU_version;
 
     	        try {
     	        	File f = new File("/proc/meminfo");
     	        	BufferedReader r = new BufferedReader(new InputStreamReader( new FileInputStream( f )),32);
     				String line = r.readLine();
     				memSize =  Long.parseLong(line.substring(line.indexOf(":")+1,
     										line.lastIndexOf("k")).trim())/1024;
     	        }
     			catch (IOException e) {
     				memSize =  0;
     			}
     	        
     			try
     		    {
     		      FileReader fr = new FileReader("/proc/shmcu_version");
     		      BufferedReader br = new BufferedReader(fr);
     		      mMCU_version = br.readLine();
     		      br.close();
     		    }
     		    catch (FileNotFoundException e){
     		    	mMCU_version = "No MCU File";
     		    } catch (IOException e) {
     		    	mMCU_version = "Read MCU Err";
     			}
     			new AlertDialog.Builder(this)
 					.setTitle("System Configuration")
 					.setIcon(R.drawable.icon)
 					.setMessage("MCU Version: " + mMCU_version +
 								"\nMemory Size: " + memSize +" Mb" +
 								"\nResolution: " + display.getWidth() + " x " + display.getHeight())
 					.setPositiveButton("Ok", null)
 					.show();
     			return true;
     			
     		case 2:
         		Intent i = new Intent();
         		i.putExtra("LOG", "HISTORY");
         		i.setClass(ANexterActivity.this , getResults.class);
         		startActivity(i);
     			return true;
 
     		case 3:
     			new AlertDialog.Builder(this)
     				.setIcon(R.drawable.icon)
 					.setMessage("aNexter\n\n\t(c) Tody 2012")
 					.setTitle("About aNexter")
 					.setPositiveButton("Ok", null)
 					.show();
     			return true;
     			
         }
         return true;
     }
     
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		if (keyCode == KeyEvent.KEYCODE_BACK) {
 			return true;
 		}
 		return super.onKeyDown(keyCode, event);
 	}
 }
