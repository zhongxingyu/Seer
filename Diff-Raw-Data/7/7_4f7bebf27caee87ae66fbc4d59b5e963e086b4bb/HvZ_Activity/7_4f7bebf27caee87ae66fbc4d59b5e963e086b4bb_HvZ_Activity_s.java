 package csci422.final_project;
 
 import java.io.File;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.Window;
 import android.widget.Button;
 import android.widget.Toast;
 import csci422.final_project.profile.Profile;
 
 public class HvZ_Activity extends Activity {
 	private static final String WARNING = "Invalid Player Code.\n" +
 			"Please go to your Profile to update.";
 	
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		requestWindowFeature(Window.FEATURE_LEFT_ICON);
 		setContentView(R.layout.main);
 		setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.ic_hvz);
 
 		// code used to create the activity for retrieving player info
 		final Button playerButton = (Button) findViewById(R.id.player_list);
 		playerButton.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				Intent i = new Intent(HvZ_Activity.this, PlayersActivity.class);
 				startActivity(i);
 			}
 		});
 
 		//code used to create the activity for opening screen to report a kill
 		final Button killButton = (Button) findViewById(R.id.report_kill);
 		killButton.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				Intent i = new Intent(HvZ_Activity.this, ReportActivity.class);
 				startActivity(i);
 			}
 		});
 		
 		// Creates MiniMap Activity
 		final Button miniMapButton = (Button) findViewById(R.id.mini_map);
 		miniMapButton.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				Intent i = new Intent(HvZ_Activity.this, MiniMapActivity.class);
 				startActivity(i);
 			}
 		});
 		
 		// Create MiniMap Activity with Flare
 		final Button flareButton = (Button) findViewById(R.id.shoot_flare);
 		flareButton.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				Log.w("HAI", "flare button says 'ey!");
 				
 				Intent i = new Intent(HvZ_Activity.this, MiniMapActivity.class);
 				
 				Bundle b = new Bundle();
 				b.putBoolean("shootFlare", true);
 				i.putExtras(b);
 				
 				startActivity(i);
 			}
 		});
 		
 //		// Creates Strategy Map Activity
 //		final Button strategyButton = (Button) findViewById(R.id.strategy_map);
 //		strategyButton.setOnClickListener(new View.OnClickListener() {
 //			public void onClick(View v) {
 //				unimplemented();
 //			}
 //		});
 		
 		// Creates Profile Activity
 		final Button profileButton = (Button) findViewById(R.id.profile);
 		profileButton.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				Intent i = new Intent(HvZ_Activity.this, ProfileActivty.class);
 				startActivity(i);
 			}
 		});
 		
 	}
 	
 	public void unimplemented() {
 		System.out.println("Currently unimplemented.");
 		Toast.makeText(getApplicationContext(), "Not currently implemented.", Toast.LENGTH_SHORT).show();
 	}
 	public String getInternalCacheDirectory() {
 	    String cacheDirPath = null;
 	    File cacheDir = getCacheDir();
 	    if (cacheDir != null) {
 	        cacheDirPath = cacheDir.getPath();
 	    }
 	    return cacheDirPath;        
 	}
 	
 	@Override
 	public void onResume() {
 		super.onResume();
 		Profile profile = Profile.getInstance();
 		
 		if (!profile.validId()) {
			Toast.makeText(getApplicationContext(), WARNING, Toast.LENGTH_LONG).show();
 		}
 	}
 }
