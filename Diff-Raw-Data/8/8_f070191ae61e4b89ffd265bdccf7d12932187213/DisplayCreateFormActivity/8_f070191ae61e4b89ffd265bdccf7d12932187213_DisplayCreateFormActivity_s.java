 package com.herokuapp.maintainenator;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.net.Authenticator;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.PasswordAuthentication;
 import java.net.URL;
 import java.util.Arrays;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.app.FragmentManager;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.net.wifi.WifiInfo;
 import android.net.wifi.WifiManager;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class DisplayCreateFormActivity extends Activity implements LocationListener, OnClickListener {
 
     private TextView bssidView;
     private TextView latView;
     private TextView longView;
     private Button submitButton;
     private EditText descriptionView;
     private EditText addressView;
 
     private BroadcastReceiver broadcastReceiver;
     private IntentFilter intentFilter;
     private LocationManager locationManager;
 
     private String cachedAddress;
     private Location cachedGPSLocation;
     private Location cachedNetworkLocation;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_display_create_form);
 
         bssidView = (TextView) findViewById(R.id.bssid);
         latView = (TextView) findViewById(R.id.latitude);
         longView = (TextView) findViewById(R.id.longitude);
 
         submitButton = (Button) findViewById(R.id.submit);
         submitButton.setOnClickListener(this);
 
         descriptionView = (EditText) findViewById(R.id.description);
         addressView = (EditText) findViewById(R.id.address);
 
         broadcastReceiver = new WifiBroadcastReceiver();
         intentFilter = new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION);
         // Monitering signal strength changes
         intentFilter.addAction(WifiManager.RSSI_CHANGED_ACTION);
         locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
 
         Authenticator.setDefault(new Authenticator() {
             protected PasswordAuthentication getPasswordAuthentication() {
                 return new PasswordAuthentication("forkloop", "494718489".toCharArray());
               }
         });
     }
 
     @Override
     public void onClick(View v) {
         if (v.getId() == R.id.submit) {
             //TODO Check input.
             new Thread(new Runnable() {
                 @Override
                 public void run() {
                     postData();
                 }
             }).start();
         }
     }
 
     private void postData() {
         URL url = null;
         HttpURLConnection urlConnection = null;
         OutputStream out = null;
         String data = generateJsonData();
         try {
             url = new URL(getString(R.string.local_url));
         } catch (MalformedURLException mue) {
             Log.e(getClass().getSimpleName(), mue.getMessage());
             return ;
         }
         try {
             urlConnection = (HttpURLConnection) url.openConnection();
             urlConnection.setDoOutput(true);
             urlConnection.setRequestProperty("Content-Type", "application/json");
             urlConnection.setRequestProperty("Accept", "application/json");
             urlConnection.connect();
             out = urlConnection.getOutputStream();
             out.write(data.getBytes("UTF-8"));
             out.flush();
             out.close();
             Log.d(getClass().getSimpleName(), "Responding: " + urlConnection.getResponseMessage());
         } catch (IOException ioe) {
             Log.e(getClass().getSimpleName(), ioe.getMessage());
             ioe.printStackTrace();
         } finally {
             urlConnection.disconnect();
         }
     }
 
     private String generateJsonData() {
         JSONObject json = new JSONObject();
         try {
             json.put("description", descriptionView.getText().toString());
             json.put("location", addressView.getText().toString());
         } catch (JSONException je) {
             Log.e(getClass().getSimpleName(), je.toString());
             return null;
         }
         Log.d(getClass().getSimpleName(), "JSON: " + json.toString());
         return json.toString();
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_main, menu);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case R.id.clip:
                 FragmentManager fm = getFragmentManager();
                 AttachmentDialog dialog = new AttachmentDialog();
                 dialog.show(fm, "fragment");
                 return true;
             default:
                     return super.onOptionsItemSelected(item);
         }
     }
 
     @Override
     protected void onResume() {
         super.onResume();
         registerReceiver(broadcastReceiver, intentFilter);
         Log.d(getClass().getSimpleName(), "Requesting location...");
         Log.d(getClass().getSimpleName(), Arrays.toString(locationManager.getAllProviders().toArray()));
         Log.d(getClass().getSimpleName(), "Network enabled: " + locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER));
         Log.d(getClass().getSimpleName(), "GPS enabled: " + locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER));
         locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
         cachedNetworkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
         Log.d(getClass().getSimpleName(), "" + cachedNetworkLocation);
         locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
         cachedGPSLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
         Log.d(getClass().getSimpleName(), "" + cachedGPSLocation);
         Log.d(getClass().getSimpleName(), "Location updating...");
     }
 
     @Override
     protected void onPause() {
         super.onPause();
         unregisterReceiver(broadcastReceiver);
     }
 
     @Override
     protected void onStop() {
         super.onStop();
         locationManager.removeUpdates(this);
     }
 
     @Override
     public void onLocationChanged(Location location) {
         Log.d(getClass().getSimpleName(), "" + location);
         latView.setText(String.format("%.5f", location.getLatitude()));
         longView.setText(String.format("%.5f", location.getLongitude()));
 //        geoView.setText("Lat: " + String.format("%.2f", location.getLatitude()) + ", Long: " + String.format("%.2f", location.getLongitude()));
        if (location.getLatitude() != cachedGPSLocation.getLatitude() || location.getLongitude() != cachedGPSLocation.getLongitude()) {
             new ReverseGeoTask().execute(new Location[] {location});
         }
     }
 
     @Override
     public void onProviderDisabled(String provider) {
         Log.d(getClass().getSimpleName(), provider + ": DISABLED!");
     }
 
     @Override
     public void onProviderEnabled(String provider) {
         Log.d(getClass().getSimpleName(), provider + ": ENABLED!");
     }
 
     @Override
     public void onStatusChanged(String provider, int status, Bundle extra) {
         Log.d(getClass().getSimpleName(), provider + ": " + status);
     }
 
     public void toast(String message) {
         Toast.makeText(this, message, Toast.LENGTH_LONG).show();
     }
 
     /**
      * Listen for WIFI status and other intents.
      */
     private class WifiBroadcastReceiver extends BroadcastReceiver {
 
         @Override
         public void onReceive(Context context, Intent intent) {
 
             Log.d(getClass().getSimpleName(), intent.getAction());
             if (intent.getAction().equals(WifiManager.WIFI_STATE_CHANGED_ACTION) || intent.getAction().equals(WifiManager.RSSI_CHANGED_ACTION)) {
                 ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                 NetworkInfo networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                 if (networkInfo.isConnected()) {
                     WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                     WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                     bssidView.setText(wifiInfo.getBSSID() + ": " + wifiInfo.getRssi());
                 }
             }
         }
     }
 
     private class ReverseGeoTask extends AsyncTask<Location, Integer, String> {
 
         @Override
         protected void onPostExecute(String result) {
            if (!result.equals(cachedAddress)) {
                 cachedAddress = result;
                 addressView.setText(result);
             }
         }
 
         private String getJsonResponse(InputStream in) {
             BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
             StringBuilder sb = new StringBuilder();
             String line = null;
             try {
                 while ((line = bufferedReader.readLine()) != null) {
                     sb.append(line + "\n");
                 }
                 bufferedReader.close();
             } catch (IOException ioe) {
                 ioe.printStackTrace();
                 return null;
             }
             return sb.toString();
         }
 
         @Override
         protected String doInBackground(Location... params) {
             if (params.length > 0) {
                 Location location = params[0];
                 HttpURLConnection urlConnection = null;
                 URL url = null;
                 try {
                    url = new URL(getString(R.string.google_map_url ) + location.getLatitude() + "," + location.getLongitude() + "&sensor=true");
                     Log.d(getClass().getSimpleName(), "Requesting " + url.toString());
                 } catch (MalformedURLException mue) {
                     Log.e(getClass().getSimpleName(), mue.getMessage());
                     return null;
                 }
                 try {
                     urlConnection = (HttpURLConnection) url.openConnection();
                     if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                         String jsonResponseString = getJsonResponse(urlConnection.getInputStream());
                         Log.d(getClass().getSimpleName(), jsonResponseString);
                         JSONObject jsonResponse = new JSONObject(jsonResponseString);
                         String address = jsonResponse.getJSONArray(getString(R.string.json_array_tag)).getJSONObject(0).getString(getString(R.string.json_address_tag));
                         Log.d(getClass().getSimpleName(), "Address: " + address);
                         return address;
                     }
                     Log.d(getClass().getSimpleName(), "Response Code: " + urlConnection.getResponseCode());
                 } catch (IOException ioe) {
                     ioe.printStackTrace();
                     Log.e(getClass().getSimpleName(), ioe.getMessage());
                 } catch (JSONException je) {
                     Log.e(getClass().getSimpleName(), je.getMessage());
                 } finally {
                     urlConnection.disconnect();
                 }
             }
             return null;
         }
     }
 
 }
