 package com.rogoapp;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.NameValuePair;
 import org.apache.http.message.BasicNameValuePair;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.location.Address;
 import android.location.Geocoder;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class SendRequestActivity extends Activity implements LocationListener {
 
     Button sendRequestButton;
     String userID;
     String trait;
     String location;
     
     String lat;
     String lon;
     
     LocationManager loc;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.send_request);
         postLocation();
         
         String targetID = (String) getIntent().getSerializableExtra("user");
         if(targetID == null){
         	targetID = "4";
         }
     	TextView userText = (TextView) findViewById(R.id.textView1);
     	userText.setText(String.format("User %s", targetID));
     	userText.invalidate();
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.send_request, menu);
         return true;
     }
     
     public void onRequest(View v){
     	//Intent intent = getIntent();
     	//TODO:  FOR THIS TO WORK, VALUES MUST BE PASSED IN FROM OPENING ACTIVITY USING THE FOLLOWING CODE AS A GUIDELINE:
     	/*
     	Intent i=new Intent(context,SendMessage.class); //Create the Intent
 		i.putExtra("id", user.getUserAccountId()+"");	//Use "putExtra" to include bonus info into new activity
 		i.putExtra("name", user.getUserFullName());
 		context.startActivity(i);						//Start Activity
     	 */
     	//String TargetUserID = intent.getStringExtra("TargetUserID"); // or should we be using username?
     	//TODO:  How do we know our current user's username?
     	//String RequestingUserID = intent.getStringExtra("RequestingUserID");
     	
     	//temp
     	String userID = "1234";
     	//temp
     	
     	String targetID = (String) getIntent().getSerializableExtra("user");
     	System.out.println("DEBUG: targetID = " + targetID);
 
     	
         EditText trait = (EditText) findViewById(R.id.request_trait);
         EditText location = (EditText) findViewById(R.id.request_location);
         
         System.out.println(trait.getText().toString());
         System.out.println(location.getText().toString());
         
     	List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
     	
     	//nameValuePairs.add(new BasicNameValuePair("RequestingUser", userID));
     	nameValuePairs.add(new BasicNameValuePair("person_id", targetID));
 
     	
 		nameValuePairs.add(new BasicNameValuePair("characteristic", trait.getText().toString()));
 
 		nameValuePairs.add(new BasicNameValuePair("location_label", location.getText().toString()));
 		
         JSONObject jObj = ServerClient.genericPostRequest("meetrequest", nameValuePairs, this.getApplicationContext());
         String status = null;
         try{
         	status = jObj.getString("status");
         }catch(JSONException e){
         	System.err.print(e);
         }
         //TODO:  Remove this!
         System.out.println("status = " + status);
         
         final Context context = this;
         if(status.equals("success")){
             final Intent start = new Intent(context, MainScreenActivity.class);
             start.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
             startActivity(start);
         }
         
     }
     
     public String getLocation(){
 
         List<Address> user = null;
         double lat;
         double lng;
         Geocoder geocoder;
         String out = "";
         String provider = "";
         
         Location location = null;
         
         loc = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
 
 
         if ( !loc.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
             buildAlertMessageNoGps();
             loc.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0,0,this);
             provider = "Network";
             location = loc.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
         }
         else{
             loc.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,this);
             provider = "GPS";
             location = loc.getLastKnownLocation(LocationManager.GPS_PROVIDER);
         }
 
         if (location == null){
             Toast.makeText(this,"Current Location Not found",Toast.LENGTH_LONG).show();
         }else{
             geocoder = new Geocoder(this);
             try {
                 user = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                 lat=(double)user.get(0).getLatitude();
                 lng=(double)user.get(0).getLongitude();
                 
                 System.out.println("DEBUG:  "+lng+"  "+lat);
                 
                 Toast.makeText(this,provider + " lat: " +lat+",  longitude: "+lng, Toast.LENGTH_LONG).show();
                 System.out.println(provider + " lat: " +lat+",  longitude: "+lng);
                 out = lat+ "," + lng;
 
             }catch (Exception e) {
                 e.printStackTrace();
             }
 
         }
         return out;
     }
     
     private void buildAlertMessageNoGps() {
         final AlertDialog.Builder builder = new AlertDialog.Builder(this);
         builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
         .setCancelable(false)
         .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
             public void onClick(final DialogInterface dialog, final int id) {
                 startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
             }
         })
         .setNegativeButton("No", new DialogInterface.OnClickListener() {
             public void onClick(final DialogInterface dialog, final int id) {
                 dialog.cancel();
             }
         });
         final AlertDialog alert = builder.create();
         alert.show();
     }
 
     public void postLocation(){
     	location = getLocation();
         String[] latLon = location.split(",");
         List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
         
         if(latLon.length == 2){
         	
         	lat = latLon[0];
         	lon = latLon[1];
         	
         	nameValuePairs.add(new BasicNameValuePair("location_lat",lat));
         	nameValuePairs.add(new BasicNameValuePair("location_lon",lon));
         	
        	System.out.println("Latitude: " + latLon[0]);
        	System.out.println("Longitude: " + latLon[1]);
         }
         else{
         	nameValuePairs.add(new BasicNameValuePair("location_lat","0.000000")); //Maybe I'm a bad person, but
         	nameValuePairs.add(new BasicNameValuePair("location_lon","0.000000")); //But the server requires a minimum of 5 decimal places
         	
         	System.out.println("Location not available");
         }
         
         //TODO NEED TO PULL USER INFO
         nameValuePairs.add(new BasicNameValuePair("availability","available"));
         nameValuePairs.add(new BasicNameValuePair("radius","1")); //1 mile
         
         ServerClient.genericPostRequest("availability", nameValuePairs, this.getApplicationContext());
         
 
     }
 /*
     @SuppressWarnings("deprecation")
     public void openRequestPopup(View v){
         AlertDialog alertDialog = new AlertDialog.Builder(this).create();
         alertDialog.setTitle("Send Request");
         alertDialog.setMessage("Send Request to User?");
         alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int which) {
                 //TODO NEED TO UPDATE FOR MEETUP REQUEST
                 List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
                 nameValuePairs.add(new BasicNameValuePair("user_id1", ""));
                 nameValuePairs.add(new BasicNameValuePair("user1_trait", ""));
                 nameValuePairs.add(new BasicNameValuePair("location", ""));
 
                 ServerClient sc = new ServerClient();
 
                 //TODO create server request for this post
                 JSONObject jObj = sc.genericPostRequest("meetup_request", nameValuePairs);
                 String uid = null;
                 String status = null;
 
                 try{
                     //uid = sc.getLastResponse().getString("uid");
                     status = jObj.getString("status");
                 }catch(JSONException e){
                     System.err.print(e);
                 }
                 System.out.println("status = " + status + ", uid = " + uid);
             }
         });
         alertDialog.show();
     }
 */
 
 	@Override
 	public void onLocationChanged(Location location) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void onProviderDisabled(String provider) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void onProviderEnabled(String provider) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void onStatusChanged(String provider, int status, Bundle extras) {
 		// TODO Auto-generated method stub
 		
 	}
 
 
 }
