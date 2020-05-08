 package de.frizzware.pacrun;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.LinearLayout;
 
 public class GameOverActivity extends Activity {
 
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.gamoover);
         
         LinearLayout tmp = (LinearLayout) findViewById(R.id.LinLyout);
         tmp.setOnClickListener(new OnClickListener() {
 			
 			public void onClick(View v) {
 				finish();
 			}
 		});
     }
 }
