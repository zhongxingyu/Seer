 package com.example.etendance;
 
 import java.io.IOException;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.util.EntityUtils;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Toast;
 
 public class CheckInActivity extends Activity implements OnClickListener {
 
 	private class CheckIn extends AsyncTask<String, Void, String> {
 		
 		@Override
 		protected void onPostExecute(String result) {
 			// TODO Auto-generated method stub
 			super.onPostExecute(result);
 			
			if(result == "checkin_success") {
 				//successful checkin
 				Toast.makeText(getApplicationContext(), R.string.success, Toast.LENGTH_LONG).show();
 			}
			else if(result == "checkin_closed") {
 				//checkin pin expired/closed
 				Toast.makeText(getApplicationContext(), R.string.closed, Toast.LENGTH_LONG).show();
 			}
			else if(result == "already_checked_in") {
 				//user already checked in for that session/class
 				Toast.makeText(getApplicationContext(), R.string.already_checked, Toast.LENGTH_LONG).show();
 			}
 			else {
 				//other error
 				Toast.makeText(getApplicationContext(), R.string.request_error, Toast.LENGTH_LONG).show();
 			}
 		}
 		
 		@Override
 		protected String doInBackground(String... code) {
 			// TODO Auto-generated method stub
 			String checkin_status = "";
 			
 			SharedPreferences settings = getSharedPreferences("LoginInfo",0);
 			String username = settings.getString("username","");
 			String password = settings.getString("password","");
 			
 			String checkin_code = code[0];
 			
 			HttpClient client = new DefaultHttpClient();
 			String checkIn = "http://etendance.kleq.net/API.php?activity=checkin_with_PIN&user=" + username+ "&pass=" + password + "&checkincode=" + checkin_code;
 			HttpPost httppost = new HttpPost(checkIn);
 			
 			try {
 				HttpResponse response = client.execute(httppost);
 				HttpEntity entity = response.getEntity();
 				
 				if (entity != null) {
 					checkin_status = EntityUtils.toString(entity);
 					return checkin_status;
 				}
 			} catch (ClientProtocolException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			
 			return null;
 		}
 	}
 	
 	
 	Button back;
 	Button qr_submit;
 	String qr_enter = "";
 	Button pin_submit;
 	EditText pin_enter;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_checkin);
 		
 		back = (Button) findViewById(R.id.back);
 		back.setOnClickListener(this);
 		
 		qr_submit = (Button) findViewById(R.id.qr_button);
 		qr_submit.setOnClickListener(this);
 		
 		pin_submit = (Button) findViewById(R.id.pin_button);
 		pin_submit.setOnClickListener(this);
 		pin_enter = (EditText) findViewById(R.id.pin_text);
 	}
 	
 	@Override
 	public void onClick(View v) {
 		// TODO Auto-generated method stub
 		if(v == back) {
 			Intent i = new Intent(this, MainActivity.class);
 			startActivity(i);
 		}
 		else if(v == qr_submit) {
 			try {
 				Intent scannerIntent = new Intent("com.google.zxing.client.android.SCAN");
 				scannerIntent.putExtra("SCAN_MODE", "QR_CODE_MODE");
 				scannerIntent.putExtra("SAVE_HISTORY", false);
 				startActivityForResult(scannerIntent, 0);
 			} catch(Exception e) {
 				Uri marketUri = Uri.parse("market://details?id=com.google.zxing.client.android");
 				Intent marketIntent = new Intent(Intent.ACTION_VIEW,marketUri);
 				startActivity(marketIntent);
 			}
 		}
 		else {
 			//pin submit
 			String pin = pin_enter.getText().toString();
 			pin_enter.setText("");
 			//send check in pin
 			new CheckIn().execute(pin);
 		}
 	}
 	
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		// TODO Auto-generated method stub
 		super.onActivityResult(requestCode, resultCode, data);
 		if(requestCode == 0) {
 			if(resultCode == RESULT_OK) {
 				qr_enter = data.getStringExtra("SCAN_RESULT");
 				//send check in pin
 				///Toast.makeText(getApplicationContext(), qr_enter, Toast.LENGTH_LONG).show();
 				new CheckIn().execute(qr_enter);
 			}
 			else if(resultCode == RESULT_CANCELED) {
 				//do nothing
 			}
 		}
 	}
 
 }//end activity
