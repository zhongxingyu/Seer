 package ca.ryerson.scs.rus.socialite;
 
 import ca.ryerson.scs.rus.MenuActivity;
 import ca.ryerson.scs.rus.R;
 import ca.ryerson.scs.rus.SplashActivity;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.Window;
 import android.widget.Button;
 
 public class SocialiteListActivity extends Activity implements OnClickListener {
 
 	private Button btnMapView, btnProfile, btnBack;
 	private final String SOCIALITE_MAP_STRING="ca.ryerson.scs.rus.socialite.SOCIALITE_MAP";
 	private final String PROFILE_STRING = "ca.ryerson.scs.rus.messenger.PROFILE";
 	
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
 		setContentView(R.layout.socialite_list);
 
 		btnMapView = (Button) findViewById(R.id.BtnSocialiteMapView);
		btnProfile = (Button) findViewById(R.id.BtnProfile);
		btnBack = (Button) findViewById(R.id.BtnBack);
 		
 		btnMapView.setFocusable(true);
 		btnProfile.setFocusable(true);
 		btnBack.setFocusable(true);
 		
 		btnMapView.setOnClickListener(this);
 		btnProfile.setOnClickListener(this);
 		btnBack.setOnClickListener(this);
 	}
 
 	@Override
 	public void onClick(View v) {
 		if (v == btnBack) {
 			if (SplashActivity.DEBUG) {
 				Log.i(MenuActivity.TAG, "Back Button");
 			}
 			// TODO: Make it go back to the main page while finishing all other activities
 			
 		} else if (v == btnProfile) {
 			if (SplashActivity.DEBUG) {
 				Log.i(MenuActivity.TAG, "Profile Button");
 			}
 			startActivity(new Intent(PROFILE_STRING));
 		}else if (v == btnMapView) {
 			if (SplashActivity.DEBUG) {
 				Log.i(MenuActivity.TAG, "Map View Button");
 			}
 			finish();
 			startActivity(new Intent(SOCIALITE_MAP_STRING));
 		}
 	}
 
 }
