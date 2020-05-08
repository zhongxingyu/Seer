 package SE_spring2013_g8.hal.Main;
 
 import lash.halapp.ViewDevActivity;
 import SE_spring2013_g8.hal.R;
 import SE_spring2013_g8.hal.Climate.ClimateControl;
 import SE_spring2013_g8.hal.Intercom.HomeView;
 import SE_spring2013_g8.hal.Lights.LightControl;
 import SE_spring2013_g8.hal.Surveillance.SurveillanceMainActivity;
import SE_spring2013_g8.hal.audio.audio_home;
 import SE_spring2013_g8.hal.emerlight.EmergencyLighting;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.GridView;
 import android.widget.Toast;
 
 /**
  * MainActivity class
  * 
  *  MainActivity is an activity class that creates and displays the home view of the 
  *  home automation system.
  * 
  * @author Theophilus Mensah
  *
  */
 public class MainActivity extends Activity {
 	
 	/**
 	 * onCreate prepares and displays the list of modules in the MainActivity view.
 	 * It listens and redirects the user to the view module clicked.
 	 *   
 	 * @param savedInstanceState an instance of the MainActivity class
 	 * 
 	 */
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 
 	    GridView gridview = (GridView) findViewById(R.id.gridview);
 	    gridview.setAdapter(new ImageAdapter(this));
 
 	    gridview.setOnItemClickListener(new OnItemClickListener() {
 	        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
 	            //Toast.makeText(MainActivity.this, "" + position, Toast.LENGTH_SHORT).show();
 	            if (position == 7) {
 	            	Intent intent = new Intent(MainActivity.this, LightControl.class);
 	            	startActivity(intent);
 	            }
 	            if(position ==5){
 	            	Intent intent = new Intent(MainActivity.this, HomeView.class);
 	            	startActivity(intent);
 	            }
 	            if (position == 6) {
 	            	Intent intent = new Intent(MainActivity.this, SurveillanceMainActivity.class);
 	            	startActivity(intent);
 	            }
 	            if (position == 4) {
 	            	Intent intent = new Intent(MainActivity.this, EmergencyLighting.class);
 	            	startActivity(intent);
 	            }
 	            if (position == 0) {
 	            	Intent intent = new Intent(MainActivity.this, ClimateControl.class);
 	            	startActivity(intent);
 	            }
 	            if (position == 3) {
 	            	Intent intent = new Intent(MainActivity.this, audio_home.class);
 	            	startActivity(intent);
 	            }
 	            if (position == 2) {
 	            	Intent intent = new Intent(MainActivity.this, ViewDevActivity.class);
 	            	startActivity(intent);
 	            }
 	        }
 
 			
 	    });
 	}
 
 
 	
 	
 	
 }
