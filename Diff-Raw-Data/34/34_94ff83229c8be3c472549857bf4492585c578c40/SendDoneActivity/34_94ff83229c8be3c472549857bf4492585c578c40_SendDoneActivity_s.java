 package net.juhonkoti.sharetobrowser;
 
import android.os.Bundle;
 import android.app.Activity;
 import android.view.Menu;
 
 public class SendDoneActivity extends Activity {
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_send_done);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_send_done, menu);
         return true;
     }
     
     public void onBackPressed() {
    	
     }
 }
