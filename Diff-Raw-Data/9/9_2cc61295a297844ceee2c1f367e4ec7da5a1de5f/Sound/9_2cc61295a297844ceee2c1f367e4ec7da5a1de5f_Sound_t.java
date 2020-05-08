 package com.spacecaker.toggles;
 
 import android.content.Context;
 import android.media.AudioManager;
 import android.net.wifi.WifiManager;
 import android.util.AttributeSet;
 import android.view.View;
 import android.widget.TextView;
import com.spacecaker.toggles.R;
 
 public class Sound extends TextView {
 
  
  public Sound(final Context context, AttributeSet attrs) {
   super(context, attrs);
   
   TextView image = (TextView) findViewById(R.id.sound);
   image.setTextSize(0);
   image.setText("sound");
   image.setBackgroundResource(R.drawable.quickpanel_icon_sound);
 	image.setOnClickListener(new View.OnClickListener() {
 		 
 		@Override
 		public void onClick(View v) {
 	        if (v.isSelected()){
 	            v.setSelected(false);
 	            AudioManager am;
 	            am= (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
 	            am.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
 	        } else {
 	            v.setSelected(true);
 	            AudioManager am;
 	            am= (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
 	            am.setRingerMode(AudioManager.RINGER_MODE_SILENT);
 	        }		};
 	});
  }
 
 }
 
