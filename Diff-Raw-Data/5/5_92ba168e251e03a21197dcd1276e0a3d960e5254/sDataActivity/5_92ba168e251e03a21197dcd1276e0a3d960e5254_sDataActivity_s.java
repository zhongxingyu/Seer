 package com.themineralpatch.hig_assignment_1;
 
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 import org.apache.http.HttpResponse;
 
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.provider.Settings;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 
 import android.widget.Toast;
 
 public class sDataActivity extends Activity {
 
 	String username;
 	EditText editMessage;
 	Button sDataButton;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.sdata);
 
 		// Makes the edittext object
 		editMessage = (EditText) findViewById(R.id.sDataTextMessage);
 		// Defining the username string
 		username = "Inge";
 		// Makes the button object
 		sDataButton = (Button) findViewById(R.id.sDataButton);

 	}
 
 	public void send(View v) {
 		// Get the message from the message text box
 		String textMessage = editMessage.getText().toString();
 		
         // Checks if the user is connected to the internet
      	ConnectivityManager connMgr = (ConnectivityManager)
      			getSystemService(Context.CONNECTIVITY_SERVICE);
      	NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
      		
      	// If connected, show the picture
      	if(networkInfo != null && networkInfo.isConnected()) {
 
     		// Make sure the fields are not empty
     		if (textMessage.length() > 0) {
     			new SendMessageToWeb().execute();
     		} else {
    			
     			// Display message if textbox is empty
     			Toast.makeText(getApplicationContext(), "Field cannot be empty", Toast.LENGTH_SHORT).show();
     		}
      	} 
      	// else notify user that he is not connected, and open wireless and network settings
      	else { 
              AlertDialog alertDialog = new AlertDialog.Builder(this).create();
              alertDialog.setTitle("Internet is disabled!");
              alertDialog.setMessage("Open settings?");
              alertDialog.setButton(-3,"OK", new DialogInterface.OnClickListener() {
 
             	 public void onClick(DialogInterface dialog, int which) {
              		  startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
              		   
             			// not sure how to refresh the activity, so finish this activity and go back to main
             			//  when the user user clicks back from the settings page
             			finish();
              	  }
              });
              alertDialog.setIcon(R.drawable.ass1_icon);
              alertDialog.show();
      	}
 
 	}
 	
 	private class SendMessageToWeb extends AsyncTask <Void, Void, String> {
 
 		@Override
 		protected String doInBackground(Void...params) {
 	        try  {
 	    		String textMessage = editMessage.getText().toString();
 	        	HttpClient client = new DefaultHttpClient();
 	        	String getURL = "http://gtl.hig.no/mobile/logging.php?user=" + username + "&data=" + textMessage;
 	        	HttpGet get = new HttpGet(getURL);
 	        	HttpResponse res = client.execute(get);
 	        	
 	        	if(res != null) {
 	        		Toast.makeText(getApplicationContext(), "Something is wrong...", Toast.LENGTH_LONG).show();
 	        	}
 	        	
 	        } catch(Exception e) {
 	        	
 	        }
 
 			return null;
 		}
 
 		@Override
 		protected void onPreExecute() {
 			Toast.makeText(getApplicationContext(), "Loading..", Toast.LENGTH_SHORT).show();
 		}
 		
		@Override
 		protected void onPostExectute(String result) {
 			editMessage.setText("");
 			Toast.makeText(getApplicationContext(), "Message sent!", Toast.LENGTH_SHORT).show();
 			finish();
 		}
 	}
 
 }
