 package com.innovalley.bt;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.widget.TextView;
 
 import com.innovalley.bluetooth.R;
 
 public class BluetoothSettings extends Activity {
 	private TextView text1;
	//private int maxDevices = 5;
 	
 	
 	
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.bluetooth_settings);
         
 		this.text1=(TextView)findViewById(R.id.textView1);
 		
 		// Restore preferences
 	    SharedPreferences settings = getSharedPreferences("bluetooth_devices_settings", 0);
 	    /*String devices = settings.
 	    String dev0 = settings.getString("device_0_name", "00:00:00:00:00:00");
 	       
 		this.text1.setText(devices);
 		*/
 	    final ArrayList<HashMap<String,String>> LIST = new ArrayList<HashMap<String,String>>();
 	    Map<String, ?> items = settings.getAll();
 	    
 	    String devs="";
 	    
 	    for(String s : items.keySet()){
 	        HashMap<String,String> temp = new HashMap<String,String>();
 	        temp.put("key", s);
 	        temp.put("value", items.get(s).toString());
 	        LIST.add(temp);
 	    }
 		
 		this.text1.setText(devs);
 	}
 
 
 	@Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.bluetooth_devices_menu, menu);
         return true;
     }
 	
 	
 	@Override
     public boolean onOptionsItemSelected(MenuItem item) {
         // Handle item selection
         switch (item.getItemId()) {
         case R.id.goDevices:
         	Intent btDevices = new Intent(getApplicationContext(), BluetoothDevicesSettings.class);
 			startActivity(btDevices);
         	return true;
         default:
             return super.onOptionsItemSelected(item);
         }
     }
 }
