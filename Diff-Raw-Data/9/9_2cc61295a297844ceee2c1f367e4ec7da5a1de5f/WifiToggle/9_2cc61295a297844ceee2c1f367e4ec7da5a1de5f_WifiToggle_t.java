 package com.spacecaker.toggles;
 
 
 import android.content.Context;
 import android.content.Intent;
 import android.net.wifi.WifiManager;
 import android.util.AttributeSet;
 import android.view.View;
 import android.widget.ImageView;
import com.spacecaker.toggles.R;
 
 public class WifiToggle extends ImageView {
 
  
  public WifiToggle(final Context context, AttributeSet attrs) {
   super(context, attrs);
   
   ImageView image = (ImageView) findViewById(R.id.settings);
   image.setImageResource(R.drawable.ic_notify_quicksettings);
 	image.setOnClickListener(new View.OnClickListener() {
 		 
 		@Override
 		public void onClick(View v) {
 	        if (v.isSelected()){
 	            v.setSelected(false);
 	            WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
 	            wifi.setWifiEnabled(false);
 	        } else {
 	            v.setSelected(true);
 	            WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
 	            wifi.setWifiEnabled(true);
 	        }		};
 	});
  }
 
 }
