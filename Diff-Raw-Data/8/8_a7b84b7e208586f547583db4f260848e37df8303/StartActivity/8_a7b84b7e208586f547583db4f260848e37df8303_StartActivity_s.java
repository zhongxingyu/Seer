 package dk.hotmovinglobster.battleships;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.Window;
 import android.widget.Button;
 import android.widget.Toast;
 
 import com.bumptech.bumpapi.BumpAPI;
 
 import dk.hotmovinglobster.battleships.comm.CommunicationProtocol;
 import dk.hotmovinglobster.dustytuba.api.BtAPI;
 import dk.hotmovinglobster.dustytuba.api.BtConnection;
 
 /**
  * Main activity with welcome screen and option to start a game.
  */
 public class StartActivity extends Activity {
 
 	protected static final int REQUEST_DUSTYTUBA = 20;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		Log.v(BattleshipsApplication.LOG_TAG, "StartActivity: onCreate()");
 		super.onCreate(savedInstanceState);
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		setContentView(R.layout.start);
 		
 		if ( BattleshipsApplication.context().Comm != null ) {
 			BattleshipsApplication.context().Comm.disconnect();
 		}
 				
 		initializeButtons();
 	}
 /*
 	@Override
 	protected void onStart() {
 		super.onStart();
 		Log.v(BattleshipsApplication.LOG_TAG, "StartActivity: onStart()");
 	}
 	@Override
     protected void onRestart() {
 		super.onRestart();
 		Log.v(BattleshipsApplication.LOG_TAG, "StartActivity: onRestart()");
 	}
 	@Override
     protected void onResume() {
 		super.onResume();
 		Log.v(BattleshipsApplication.LOG_TAG, "StartActivity: onResume()");
 	}
 	@Override
     protected void onPause() {
 		super.onPause();
 		Log.v(BattleshipsApplication.LOG_TAG, "StartActivity: onPause()");
 	}
 	@Override
     protected void onStop() {
 		super.onStop();
 		Log.v(BattleshipsApplication.LOG_TAG, "StartActivity: onStop()");
 	}
 	@Override
     protected void onDestroy() {
 		super.onDestroy();
 		Log.v(BattleshipsApplication.LOG_TAG, "StartActivity: onDestroy()");
 	}
 */
 	private void initializeButtons() {
 		
 		((Button)findViewById(R.id.start_btn_find_opponent)).setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				String[] providers = {BtAPI.IDENTITY_PROVIDER_MANUAL, BtAPI.IDENTITY_PROVIDER_BUMP,
                         BtAPI.IDENTITY_PROVIDER_PAIRED};
 		        Bundle b = new Bundle();
 		        // TODO: Send along UUID + SDP_NAME
 		        b.putStringArray(BtAPI.EXTRA_IP_PROVIDERS, providers);
 		        b.putString(BumpAPI.EXTRA_API_KEY, "273a39bb29d342c2a9fcc2e61158cbba");
 		        Intent i = BtAPI.getIntent(StartActivity.this, BtAPI.IDENTITY_PROVIDER_MULTIPLE, b);
 		        startActivityForResult(i, REQUEST_DUSTYTUBA);
 		    }
 		});

 		// TODO: Remove, used for debugging
 		((Button)findViewById(R.id.start_btn_find_opponent_debug)).setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 		        Bundle b = new Bundle();
 		        String other_mac;
 		        String my_mac = BtAPI.getBluetoothAddress();
 		        if (my_mac.equals( "00:23:D4:34:45:D7" )) { // Thomas' Hero
 //		        if (my_mac.equals( "90:21:55:A1:A5:67" )) { // Thomas' Desire
 		        	other_mac = "90:21:55:A1:A5:8D";
 		        } else { // Jesper's Desire
 		        	other_mac = "00:23:D4:34:45:D7";
 //		        	other_mac = "90:21:55:A1:A5:67";
 		        }
 		        b.putString(BtAPI.EXTRA_IP_MAC, other_mac);
 		        Intent i = BtAPI.getIntent(StartActivity.this, BtAPI.IDENTITY_PROVIDER_FAKE, b);
 		        startActivityForResult(i, REQUEST_DUSTYTUBA);
 		    }
 		});
		
 		// TODO: Remove, used for debugging
 		((Button)findViewById(R.id.start_btn_test)).setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				Intent i = new Intent(v.getContext(), LocalPlaceShipsActivity.class);
 				i.putExtra(BattleshipsApplication.EXTRA_END_WINNER, false);
 				startActivity(i);
 			}
 		});
 	}
 	
 	@Override
 	protected void onActivityResult (int requestCode, int resultCode, Intent data) {
 		Log.v(BattleshipsApplication.LOG_TAG, "StartActivity: onActivityResult(): requestCode="+requestCode+", resultCode=" + resultCode);
 	    if (requestCode == REQUEST_DUSTYTUBA) {
 	        if (resultCode == RESULT_OK) {
 	            BattleshipsApplication.context().Comm = new CommunicationProtocol(BtConnection.getConnection());
 				Intent i = new Intent(this, SetupGameActivity.class);
 				startActivity(i);
 	        } else {
 	            Toast.makeText(this, getString( R.string.start_no_opponent),
 	                           Toast.LENGTH_LONG).show();
 	        }
 	    }
 	}
 	
 }
