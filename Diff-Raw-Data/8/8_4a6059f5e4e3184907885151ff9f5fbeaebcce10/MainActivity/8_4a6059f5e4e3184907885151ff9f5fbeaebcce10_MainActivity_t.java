 package km81m.say_it_right.activities;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
 import km81m.say_it_right.R;
 
 public class MainActivity extends Activity {
     /**
      * Called when the activity is first created.
      */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);

        Spinner spinner = (Spinner) findViewById(R.id.settings_level);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.level_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
     }
 
     public void startTest(View view) {
         Intent intent = new Intent(this, LevelActivity.class);
         startActivity(intent);
     }
 
 }
