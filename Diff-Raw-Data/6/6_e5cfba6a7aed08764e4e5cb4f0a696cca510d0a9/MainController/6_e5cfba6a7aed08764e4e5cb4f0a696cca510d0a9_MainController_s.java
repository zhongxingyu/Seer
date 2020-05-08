 package se.kudomessage.jessica.controllers;
 
 import se.kudomessage.jessica.CONSTANTS;
 import se.kudomessage.jessica.Globals;
 import se.kudomessage.jessica.R;
 import se.kudomessage.jessica.models.PushModel;
 import se.kudomessage.jessica.models.SMSSentObserver;
 
 import com.google.android.gcm.GCMRegistrar;
 
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.util.Log;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.Toast;
 import android.app.Activity;
 import android.content.Intent;
 
 public class MainController extends Activity {
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.view_main);
 		
 		Globals.setActivity(this);
 		
 		GCMRegistrar.checkDevice(this);
 		GCMRegistrar.checkManifest(this);
 		
 		// TODO: Remove in final release.
 		// GCMRegistrar.unregister(this);
 		
 		String GCMKey = GCMRegistrar.getRegistrationId(this);	
 		
 		if (GCMKey.equals("")) {
 			GCMRegistrar.register(this, CONSTANTS.SENDER_ID);
 		} else {
 			Globals.setGCM(GCMKey);
 			//No longer needed as there's an button for the request now
 			//OAuthController.requestAccessToken();
 		}
 		
 		if(Globals.getGCM() != null && 
 		   Globals.getEmail() != null && 
 		   Globals.getAccessToken() != null &&
 		   Globals.getRegStatus()
 		){
 			this.registered();
 		}
 	}
 	
 	public void init() {
 		Log.i(CONSTANTS.TAG, "GCMKey: " + Globals.getGCM());
 		Log.i(CONSTANTS.TAG, "TOKEN: " + Globals.getAccessToken());
 	
 		PushModel.registerDevice();
 		getContentResolver().registerContentObserver(Uri.parse("content://sms/"), true, new SMSSentObserver(new Handler(), this));
 	}
 	
 	/** Button listener **/
 
 	/**
 	 * Triggered when the user clicks the "Sign in with Google" button
 	 * 
 	 * @param view required for button listeners 
 	 */
 	public void requestOAuth(View view) {
 		OAuthController.requestAccessToken();
 	}
 	
 	public void registered(){
 	    Intent intent = new Intent(this, RegisteredServerController.class);
 	    //intent.putExtra(EXTRA_MESSAGE, message);
 	    startActivity(intent);
 	}
 	
 	/**
 	 * Triggered when the users clicks the "Continue" button
 	 * 
 	 * @param view required for button listeners 
 	 */
 	public void registerUser(View view){
 		// TODO: Add more error checking on the server address.
		String server = ((EditText) findViewById(R.id.edit_message)).toString();
		int port = Integer.parseInt(((EditText) findViewById(R.id.edit_port)).toString());
 		if(server != null){
 			Log.d("registerUser", "Setting server to: \""+server+"\"");
 			Globals.setServer(server);
 		}
 		if(port > 0 && port < 65535){
 			Log.d("registerUser", "Setting port to: \""+port+"\"");
 			Globals.setPort(port);
 		}
 		if(Globals.getAccessToken() == null ){
 			//Error stuff for accesstoken
 			Log.e("registerUser", "Accesstoken not granted");
 			Toast.makeText(this, 
 					"You haven't granted access to Google", 
 					Toast.LENGTH_SHORT).show();
 		}else if(PushModel.testServer() == false){
 			//If the server isn't a valid KudoMessage server.
 			Log.e("registerUser", "The message server wasn't valid");
 			Toast.makeText(this, 
 					"The server you specified isn't a valid KudoMessage-server.", 
 					Toast.LENGTH_SHORT).show();
 		}else if(Globals.getGCM() == null){
 			//Please wait for GCM something message
 			Log.e("registerUser", "GCM wasn't fetched yet");
 			Toast.makeText(this, 
 					"Waiting for Google repsone, please try again in a few seconds.", 
 					Toast.LENGTH_SHORT).show();
 		}else{
 			//Success
 			Log.d("registerUser", "Successfully");
 			init();
 			Globals.setRegStatus(true);
 			Toast.makeText(this, 
 					"Successfully registered to KudoMessage", 
 					Toast.LENGTH_SHORT).show();
 			this.registered();
 		}
 	}
 }
