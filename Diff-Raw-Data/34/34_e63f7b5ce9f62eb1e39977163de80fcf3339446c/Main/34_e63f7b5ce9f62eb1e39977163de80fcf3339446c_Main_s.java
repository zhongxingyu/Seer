 package com.authorwjf.bounce;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.widget.TextView;
 import com.facebook.*;
 import com.facebook.model.*;
 
 public class Main extends Activity {
 	public static String fbid = null;
 	Activity act;
 
 	public static String macAddress = null;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		act = this;
 		if (fbid == null) {
 			setContentView(R.layout.login);     
 		}
 		//setContentView(R.layout.main);
 		//Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.example.android.BluetoothChat");
 		//startActivity(launchIntent);
 		//   
 		// start Facebook Login
 		Session.openActiveSession(this, true, new Session.StatusCallback() {
 
 			// callback when session changes state
 			@Override
 			public void call(Session session, SessionState state, Exception exception) {
 				if (session.isOpened()) {
 
 					// make request to the /me API
 					Request.executeMeRequestAsync(session, new Request.GraphUserCallback() {
 
 						// callback after Graph API response with user object
 						@Override
 						public void onCompleted(GraphUser user, Response response) {
 							if (user != null) {
 								macAddress = null;
 								Intent serverIntent = new Intent(act, DeviceListActivity.class);
 								startActivity(serverIntent);
 								fbid = user.getId();
 
 								System.out.println("Acquired fbid of " + fbid);
 								System.out.println("Now attempting to start BT service");
								while(macAddress == null)	{
 									Thread.yield();
								}
 								startService(new Intent(new BluetoothChat(), BluetoothChat.class));
 
 								System.out.println("Service successfully started?");
 
 								setContentView(R.layout.main);
 
 							}
 						}
 					});
 				} 
 			}
 		});
 	}
 
 	@Override
 	public void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 		Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
 	}
 }
