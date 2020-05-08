 package com.crimezone.sd;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.util.List;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.json.JSONArray;
 import org.json.JSONTokener;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.location.Address;
 import android.location.Geocoder;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Spinner;
 import android.widget.Toast;
 
 public class SDCrimeZoneActivity extends Activity implements View.OnClickListener {
 
   public static Location currLocation;
   public static Address _manualAddress;
   public static String selectedRadius;
   public static String selectedDate;
   public LocationManager locationManager;
   public LocationListener locationListener;
 
   /** Called when the activity is first created. */
   @Override
   public void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
     this.createLocationManager();
     this.initializeApp();
   }
   
   @Override
   public void onPause() {
     super.onPause();
     locationManager.removeUpdates(locationListener);
   }
 
   /**
    * Take user to landing page for SD Crime Zone. Populate the dropdown boxes
    * (Distance and Date)
    */
   public void initializeApp() {
     setContentView(R.layout.main);
     // get current GPS coordinates and set as default for app
     EditText currLocationText = (EditText) this.findViewById(R.id.addressText);
     currLocationText.setText(getString(R.string.defaultLocation));
     // the distance dropdown list
     Spinner distanceList = (Spinner) this.findViewById(R.id.distanceList);
     populateSpinnerWithArray(distanceList, R.array.distanceArray);
     distanceList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
       public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
         selectedRadius = parent.getSelectedItem().toString();
         selectedRadius = selectedRadius.replaceFirst("\\smile", "");
       }
 
       public void onNothingSelected(AdapterView<?> parent) {
         parent.setSelection(0);
         selectedRadius = parent.getSelectedItem().toString();
         selectedRadius = selectedRadius.replaceFirst("\\smile", "");
       }
     });
 
     // the distance dropdown list
     Spinner dateList = (Spinner) this.findViewById(R.id.datesList);
     populateSpinnerWithArray(dateList, R.array.dateArray);
     dateList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
       public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
         selectedDate = parent.getSelectedItem().toString();
       }
 
       public void onNothingSelected(AdapterView<?> parent) {
         parent.setSelection(0);
         selectedDate = parent.getSelectedItem().toString();
       }
     });
     Button submitButton = (Button) this.findViewById(R.id.submitButton);
     submitButton.setOnClickListener(this);
   }
 
   public void createLocationManager() {
     // Acquire a reference to the system Location Manager
     locationManager = (LocationManager) this
         .getSystemService(Context.LOCATION_SERVICE);
 
     // Define a listener that responds to location updates
     locationListener = new LocationListener() {
       public void onLocationChanged(Location location) {
         // Called when a new location is found by the network location
         // provider.
         locationWasDetected(location);
       }
 
       public void onStatusChanged(String provider, int status, Bundle extras) {
       }
 
       public void onProviderEnabled(String provider) {
       }
 
       public void onProviderDisabled(String provider) {
       }
     };
 
     // Register the listener with the Location Manager to receive location
     // updates
    registerForLocationUpdates();
  }

  private void registerForLocationUpdates() {
     locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
   }
 
   public void locationWasDetected(Location location) {
     if (usingLocationDetection()) {
       currLocation = location;
     }
     // this.debug("Location= " + String.valueOf(location.getLatitude())
     // + ", " + String.valueOf(location.getLongitude()));
   }
 
   private boolean usingLocationDetection() {
     EditText addrText = (EditText) this.findViewById(R.id.addressText);
     if (addrText != null
         && addrText.getText().toString().equals(getString(R.string.defaultLocation))
     ) {
       return true;
     } else {
       return false;
     }
   }
   
   @Override
   public void onResume() {
     super.onResume();
     EditText addrText = (EditText) this.findViewById(R.id.addressText);
     addrText.setText(getString(R.string.defaultLocation));
    addrText.selectAll();
    registerForLocationUpdates();
   }
 
   /**
    * populateSpinnerWithArray Populates dropdown boxes with options, based on
    * string arrays.
    * 
    * @param spinner
    * @param stringArrayResId
    */
   public static void populateSpinnerWithArray(Spinner spinner, int stringArrayResId) {
     ArrayAdapter<String> adapter = new ArrayAdapter<String>(spinner.getContext(),
         android.R.layout.simple_spinner_item, spinner.getContext().getResources()
             .getStringArray(stringArrayResId));
     adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
     spinner.setAdapter(adapter);
   }
 
   /**
    * Handles all the apps onclick functions
    * 
    * @param v
    */
   public void onClick(View v) {
     if (v.getId() == R.id.submitButton) {
       ProgressDialog dialog = ProgressDialog.show(this, "", "Loading. Please wait...", true);
       
       try {
         
 
         JSONArray results = this.sendHttpRequestToServer(v);
         Intent intent = new Intent();
         Bundle bun = new Bundle();
 
         bun.putString("results", results.toString()); // add two parameters: a
         // string and a boolean
         EditText addr = (EditText) this.findViewById(R.id.addressText);
         String currentAddress = addr.getText().toString();
         double[] latlong = { 0, 0 };
         
         if (usingLocationDetection()) {
           if ((currLocation == null)) {
             Toast noLocationYet = Toast.makeText(this, "Location Service is still determining your position", 5);
             noLocationYet.show();
             dialog.dismiss();
             return;
           } else { //Location obtained
             SDCrimeZoneApplication.debug(this, "Using detected location");
             latlong[0] = currLocation.getLatitude();
             latlong[1] = currLocation.getLongitude();
             if (!currentAddress.equals(getString(R.string.defaultLocation))) {
               latlong = getLatLong(currentAddress);
             }
           }
         } else { //using manually entered address
           SDCrimeZoneApplication.debug(this, "Using manually entered location");
           latlong[0] = _manualAddress.getLatitude();
           latlong[1] = _manualAddress.getLongitude();
         }
 
         bun.putString("startLat", String.valueOf(latlong[0]));
         bun.putString("startLng", String.valueOf(latlong[1]));
         bun.putString("year", selectedDate);
         bun.putString("radius", selectedRadius);
 
          
         /*
          * Check if the current address entered is actually in San Diego
          * 
          *  Fixing issue where the following lat/long is excluded
          *  http://216.231.132.72/get.php?lat=32.7742488&lng=-117.1411815&rad=1&year=2011
          *  //Apparent lat/long of Mike's House lat=32.74999737739563 lng=-117.23599791526794
          */
         
         /* raw old code*/ 
         /*
         if (latlong[0] >= 33.427045 || latlong[1] <= -117.612003 || latlong[0] <= 32
             || latlong[1] >= -116.0775811) {
          */ 
           
        
         
         /*  old code prettied up*/
         /*
         if (
             latlong[0] >= 33.427045 || 
             latlong[0] <= 32 || 
             
             latlong[1] <= -117.612003 ||
             latlong[1] >= -116.0775811
             ) {
          */
         
         
         /*  Final version that was supposed to work, but now who knows.
         if (
             latlong[0] <= 32 ||
             33.427045 <= latlong[0] || 
             
             latlong[1] <= -117.612003 || 
             -116.0775811 <= latlong[1]
             ) 
         {*/
         
         
         //Ugly answer for ugly  problem
         boolean exclusionTestFailed = false;
         
         if (latlong[0] <= 32) {
           SDCrimeZoneApplication.debug(this, "Failed FIRST lat/long exclusion test");
           exclusionTestFailed = true;
         }
         
         if (33.427045 <= latlong[0]) {
           SDCrimeZoneApplication.debug(this, "Failed SECOND lat/long exclusion test");
           exclusionTestFailed = true;
         }  
         
         if (latlong[1] <= -117.612003) {
           SDCrimeZoneApplication.debug(this, "Failed THIRD lat/long exclusion test");
           exclusionTestFailed = true;
         } 
         
         if (-116.0775811 <= latlong[1]) {
           SDCrimeZoneApplication.debug(this, "Failed FOURTH lat/long exclusion test");
           exclusionTestFailed = true;
         }
         
         if (exclusionTestFailed) {
           //dialog.dismiss();  //Delete line if dialog works now
           Toast notInSD = Toast.makeText(this, "Currently only supporting San Diego locations", 5);
           notInSD.show();
         } else {  //Location obtained and was not excluded
           intent.setClass(this, SDCrimeSummaryActivity.class);
           intent.putExtras(bun);
 
           startActivity(intent);
 
         }
       } catch (Exception e) {
         e.printStackTrace();
         Toast addrNotFound = Toast.makeText(this, "Address Not Found", 5);
         addrNotFound.show();
       }
       dialog.dismiss();
     }
 
   }
 
   /**
    * Sends an HttpRequest to SDCrimeZone-AppEngine with lat, long, radius (and
    * date). Gets the JSON result and returns it. Should ONLY be called from main
    * page.
    * 
    **/
   private JSONArray sendHttpRequestToServer(View v) {
     // get the current GPS coordinates, distance, and dates selected
 
     JSONArray jsonObjs = new JSONArray();
 
     EditText addr = (EditText) this.findViewById(R.id.addressText);
     Spinner dist = (Spinner) this.findViewById(R.id.distanceList);
     String currentAddress = addr.getText().toString();
     try {
 
       double[] latlong = { 0, 0 };
 
       if (currLocation != null) {
         latlong[0] = currLocation.getLatitude();
         latlong[1] = currLocation.getLongitude();
       }
       if (!currentAddress.equals(getString(R.string.defaultLocation))) {
         latlong = getLatLong(currentAddress);
       }
       HttpResponse response;
       HttpClient hc = new DefaultHttpClient();
       
       String httpGetStr = "http://216.231.132.72/get.php?lat=" + String.valueOf(latlong[0])
           + "&lng=" + String.valueOf(latlong[1]) + "&rad=" + selectedRadius + "&year="
           + selectedDate;
       
       SDCrimeZoneApplication.debug(this, httpGetStr);
       HttpGet get = new HttpGet(httpGetStr);
       response = hc.execute(get);
 
       // get the response from the Google Apps Engine server, should be in
       Reader in = new BufferedReader(new InputStreamReader(response.getEntity().getContent(),"UTF-8"));
 
       StringBuilder builder = new StringBuilder();
 
       char[] buf = new char[1000];
 
       int l = 0;
 
       while (l >= 0) {
 
         builder.append(buf, 0, l);
 
         l = in.read(buf);
       }
       JSONTokener tokener = new JSONTokener(builder.toString());
 
       JSONArray finalResult = new JSONArray(tokener);
 
       return finalResult;
     } catch (Exception e) {
       e.printStackTrace();
     }
     return null;
 
   }
 
   private double[] getLatLong(String strAddress) {
     Geocoder coder = new Geocoder(this);
     List<Address> resolvedAddresses;
 
     try {
       resolvedAddresses = coder.getFromLocationName(strAddress, 5);
       if (resolvedAddresses == null) {
         return null;
       }
       
       _manualAddress = resolvedAddresses.get(0); //Hoping the first one works
       double[] results = { _manualAddress.getLatitude(), _manualAddress.getLongitude() };
       
       String strMessage = "Resolved address ["+ strAddress 
       +"] to lat["+ _manualAddress.getLatitude()
       +"] long["+ _manualAddress.getLongitude() +"]";
       
       SDCrimeZoneApplication.debug(this, strMessage);
       
       return results;
     } catch (Exception e) {
       return null;
     }
 
   }
 
 }
