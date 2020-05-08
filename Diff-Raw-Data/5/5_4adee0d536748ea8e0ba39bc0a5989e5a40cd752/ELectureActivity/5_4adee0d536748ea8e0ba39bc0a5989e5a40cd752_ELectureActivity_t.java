 package com.cs.uwindsor.group.eLecture;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.YuvImage;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TextView;
 
 public class ELectureActivity extends Activity {
 	
 	Button button;
 	Activity mActivity;
 	Bundle extra = new Bundle();
 	TextView text;
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
        
         button = (Button)findViewById(R.id.connectButton);
         button.setOnClickListener(playListener);
        //button.setText("Connect");
         text = (TextView)findViewById(R.id.url);
     }
     
     private OnClickListener playListener = new OnClickListener() {
         public void onClick(View v) {
         	if(button.getText().toString().equals("Connect")){
        		//button.setText("Disconnect"); 
         		extra.putCharSequence("url", text.getText());
         		Intent i = new Intent(getApplicationContext(), StreamPlayer.class);
         		i.putExtras(extra);
         		startActivity(i);
         	}
         	else{
             	button.setText("Connect");
         	}
         }
     };
 
 }
