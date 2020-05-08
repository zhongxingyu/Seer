 package km81m.say_it_right.activities;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import km81m.say_it_right.R;
 
 public class MainActivity extends Activity {
     /**
      * Called when the activity is first created.
      */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
     }
 
     public void startTest(View view) {
         Intent intent = new Intent(this, LevelActivity.class);
         startActivity(intent);
     }
 
 }
