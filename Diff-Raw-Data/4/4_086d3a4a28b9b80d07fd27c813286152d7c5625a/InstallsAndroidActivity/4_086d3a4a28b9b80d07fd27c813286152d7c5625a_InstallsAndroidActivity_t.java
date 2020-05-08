 /*---------------------------------------------------------------------------*\
  |  InstallsAndroidActivity.java                                             |
  |                                                                           |
  |  Copyright (c) 2012, CrowdMob, Inc., original authors.                    |
  |                                                                           |
  |      File created/modified by:                                            |
  |          Raj Shah <raj@crowdmob.com>                                      |
 \*---------------------------------------------------------------------------*/
 
 package com.crowdmob.installs;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Handler;
 import android.util.Log;
 import android.view.View;
 import android.view.Window;
 import android.view.WindowManager.LayoutParams;
import android.webkit.WebChromeClient;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.TextView;
 import android.widget.ToggleButton;
 
 public class InstallsAndroidActivity extends Activity {
 	private static final String SECRET_KEY = "d2ef7da8a45891f2fee33747788903e7";
 	private static final String PERMALINK = "the-impossible-game";
 
 	private static final String SANDBOX_URL = "http://www.mobstaging.com";
 	private static final String PRODUCTION_URL = "https://deals.crowdmob.com";
 
 	// All UI elements:
 	private Button offerwallButton;
 	private Button buttonIpOffer;
 	private Button buttonGpsOffer;
 	private EditText apiKeyText;
 	private EditText apiSecretText;
 	private TextView textOfferName;
 	private TextView textOfferDiscount;
 	private TextView textOfferCurrency;
 	private TextView textOfferLocation;
 	private TextView textOfferExpires;
 	private ToggleButton toggleSandbox;
 	private Button buttonBuyOffer;
 
 	// Location management:
 	private LocationManager locationManager;
 	private LocationListener locationListener;
 	private Location currentLocation;
 	private boolean requestingLocation;
 
 	// The best offer fetched for this user:
 	private JSONObject currentOffer;
 
 	// JavaScript hook back to main thread:
 	private Handler purchaseConfirmedHandler = new Handler();
 
 	/** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
 
         currentLocation = null;
         requestingLocation = false;
         currentOffer = null;
 
         // Wire up the UI elements.
         offerwallButton = (Button) findViewById(R.id.buttonOfferwall);
         buttonIpOffer = (Button) findViewById(R.id.buttonIpOffer);
         buttonGpsOffer = (Button) findViewById(R.id.buttonGpsOffer);
         apiKeyText = (EditText) findViewById(R.id.editApiKey);
         apiSecretText = (EditText) findViewById(R.id.editApiSecret);
         toggleSandbox = (ToggleButton) findViewById(R.id.toggleSandbox);
         textOfferName = (TextView) findViewById(R.id.textOfferName);
         textOfferDiscount = (TextView) findViewById(R.id.textOfferDiscount);
         textOfferCurrency = (TextView) findViewById(R.id.textOfferCurrency);
         textOfferLocation = (TextView) findViewById(R.id.textOfferLocation);
         textOfferExpires = (TextView) findViewById(R.id.textOfferExpires);
         buttonBuyOffer = (Button) findViewById(R.id.buttonBuyOffer);
 
         // Open the offerwall.
         offerwallButton.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
             	showWebview("/m/offerwall?api_access_key=" + apiKeyText.getText());
             }
         });
 
         // Request the best offer for the user and display it.
         buttonIpOffer.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
             	_clearOffer();
             	new RequestOfferTask().execute();
             }
         });
         
         // Request GPS location, pass it to the API, and get the best offer for the user
         buttonGpsOffer.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
             	_clearOffer();
             	fetchGPSOffer();
             }
         });
         
 
         // Listen for a GPS location, and once found, fetch an offer from the service
         locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
         locationListener = new LocationListener() {
             public void onLocationChanged(Location location) {
             	currentLocation = location;
             	if (requestingLocation) {
             		fetchGPSOffer();
             		requestingLocation = false;
             	}
             }
             public void onStatusChanged(String provider, int status, Bundle extras) {}
             public void onProviderEnabled(String provider) {}
             public void onProviderDisabled(String provider) {}
           };
 
           // Launch the webview for the current offer.
           buttonBuyOffer.setOnClickListener(new View.OnClickListener() {
               public void onClick(View v) {
             	  if (currentOffer != null) {
             		  try {
             			  showWebview("/m/offers/" + currentOffer.getString("id") + "?api_access_key=" + apiKeyText.getText());
             		  }
             		  catch (JSONException e) {
             			  _alert("Error getting currentOffer's ID: " + e.getMessage());
             		  }
             	  }
             	  else {
             		  _alert("No offer fetched yet; please select one of the buttons above");
             	  }
               }
           });
 
         RegisterWithCrowdMob.trackAppInstallation(this, SECRET_KEY, PERMALINK);
     }
 
     /**
      * Returns the URL for the desired environment, based on the toggle in the UI.
      */
     private String baseUrl() {
     	if (toggleSandbox.isChecked()) {
     		return SANDBOX_URL;
     	} else {
     		return PRODUCTION_URL;
     	}
     }
 
     /**
      * Calls RequestOfferTask, but only after currentLocation has been set.  Starts off
      * the requesting of location from the phone.
      */
     private void fetchGPSOffer() {
         if (currentLocation == null) {
         	if (!locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
         		_alert("This device doesn't have GPS, so I'll show you an IP based offer.");
         		new RequestOfferTask().execute();
         		return;
         	}
             // Register the listener with the Location Manager to receive location updates
             locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
         	requestingLocation = true;
         }
         else {
         	new RequestOfferTask().execute();
         }
     }
     
     /**
      * If and only if there is a resulting offer to display, parses out the JSON and sets
      * its text into the TextViews in the UI
      * @param result the JSONObject returned by the MobDeals server
      */
     private void renderOffer(JSONObject result) {
     	if (result == null) { return; }
 		
     	// Test if there was an error message, if not, we'll get an exception we can ignore.
     	try { 
     		_alert("Error fetching deal: " + result.get("error_message")); 
     		return;
     	}
     	catch(JSONException ignored) {}
     	
     	try {
     		JSONObject offers = result.getJSONObject("offers");
         	currentOffer = offers.getJSONObject((String) offers.keys().next());
  
     		textOfferName.setText(currentOffer.getJSONObject("provider").getString("name"));
     		textOfferDiscount.setText(currentOffer.getString("discount") + "% off");
     		textOfferCurrency.setText(currentOffer.getString("coins") + " Free Coins!");
     		textOfferLocation.setText(currentOffer.getJSONObject("closest_location").getString("street_address"));
     		textOfferExpires.setText("Sale ends on: " + currentOffer.getString("expires"));
     	}
     	catch(JSONException e) {
 			  _alert("Error rendering currentOffer into labels: " + e.getMessage());
     	}
     }
     
     /**
      * All Android web requests must be asyncronous.  This task requests of the offer,
      * possibly appending the geolocation of the user if it's set.  
      * 
      * It calls the renderOffer when done to draw out the offer into the UI.
      */
     private class RequestOfferTask extends AsyncTask<String, Integer, JSONObject> {
         protected JSONObject doInBackground(String... ignored) {
         	String path = "/offerwall?count=1&api_access_key=" + apiKeyText.getText();
         	path += (currentLocation != null ? "&location=" + currentLocation.getLatitude() + "," + currentLocation.getLongitude() : "");
         	
         	HttpClient httpclient = new DefaultHttpClient();
             HttpGet httpget = new HttpGet(baseUrl() + path);
             HttpResponse response;
             
             try {
             	response = httpclient.execute(httpget);
             	HttpEntity entity = response.getEntity();
             	if (entity != null) {
             		InputStream instream = entity.getContent();
             		String result = InstallsAndroidActivity._convertStreamToString(instream);
             		instream.close();
             		
             		return new JSONObject(result);
                }
             }
             catch (JSONException e) {
                 Log.e("MOBDEALS", "There was an error parsing the JSON", e);
                 _alert("Couldn't load offer JSON: " + e.getMessage());
             }
             catch (ClientProtocolException e) {
             	Log.e("MOBDEALS", "There was a protocol based error", e);
                 _alert("Couldn't load offer because of a protocol problem: " + e.getMessage());
             }
             catch (IOException e) {
             	Log.e("MOBDEALS", "There was an IO Stream related error", e);
                 _alert("Couldn't load offer because of an IO Stream error: " + e.getMessage());
             }
             catch(Exception e) {
                 _alert("Couldn't load offer for an unkown reason: " + e.getMessage());
             }
             return null;
         }
 
         protected void onProgressUpdate(Integer... progress) {
             // setProgressPercent(progress[0]);
         }
 
         protected void onPostExecute(JSONObject result) {
         	renderOffer(result);
         }
     }
     
     /**
      * Make sure the webview doesn't open the browser application on 
      * Android, by loading each url back into the same view.
      */
     private class SingleWindowWebViewClient extends WebViewClient {
         @Override
         public boolean shouldOverrideUrlLoading(WebView view, String url) {
         	view.loadUrl(url);
         	return true;
         }
     }
     
     /**
      * Pops up a mobdal dialog, designed by webviewdialog.xml, with a WebView in in to 
      * display the MobDeals checkout process, or the offerwall.
      * @param path The URI path to display in the browser (not including the base server url)
      */
     private void showWebview(String path) {
     	// Context mContext = getApplicationContext(); 
     	final Dialog dialog = new Dialog(this); // this was swapped with mContext in the android sample code, but doesn't work.
     	dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
     	dialog.setContentView(R.layout.webviewdialog);
     	
     	// Make sure to expand the view to the whole screen
     	dialog.getWindow().setLayout(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
     	
     	// Make sure javascript and geolocation are enabled in the browser.
     	WebView webview = (WebView) dialog.findViewById(R.id.DialogWebView);
     	webview.getSettings().setJavaScriptEnabled(true);
     	webview.getSettings().setGeolocationEnabled(true);
     	
     	// Make sure that we don't open the native browser application
     	webview.setWebViewClient(new SingleWindowWebViewClient());
     	
     	// Add the hook back into the application from Javascript for purchase completion
     	webview.addJavascriptInterface(new MobDealsJavaScriptInterface(), "mobdeals_native");
 
     	// This gives permission to send the phone's location via the browser
     	webview.setWebChromeClient(new WebChromeClient() {
    		public void onGeolocationPermissionsShowPrompt(String origin, android.webkit.GeolocationPermissions.Callback callback) {
     			callback.invoke(origin, true, true);
     		}
     	});
 
     	webview.loadUrl(baseUrl() + path);
     	dialog.show();
     	
     	// Set the close button at the top left to close this window.
     	ImageButton cancelButton = (ImageButton) dialog.findViewById(R.id.cancelButton);
     	cancelButton.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) { dialog.cancel(); }
         });
     }
     
     /**
      * The interface class so that javascript can communicate confirmed purchases
      * back to the native app.
      */
     final class MobDealsJavaScriptInterface {
         public void purchaseConfirmed(final String confirmation) {
         	purchaseConfirmedHandler.post(new Runnable() {
                 public void run() {
                     handlePurchaseConfirmation(confirmation);
                 }
             });
 
         }
     }
     
     /**
      * Retrieve the purchase confirmation JSON, verify that the signature 
      * matches what we expect it to, and if it does, show the user!
      *  
      * This is where the developer can extract how many coins to credit
      * the user.
      * 
      * @param confirmationJson the json that represents information about the
      * transaction, including the signature.
      */
     private void handlePurchaseConfirmation(String confirmationJson) {
     	try {
     		JSONObject purchase = new JSONObject(confirmationJson);
     		int coins = purchase.getInt("coins");
     		String coinsStr = purchase.getString("coins");
     		String timestamp = purchase.getString("timestamp");
     		String transaction = purchase.getString("transaction");
     		String signature = purchase.getString("signature");
     		
     		if (verifySignature(coinsStr, timestamp, transaction, signature)) {
             	_alert("SUCCESS!  User should now be granted " + coins + " coins.");
             	// TODO Developer to put in hook here to add virtual currency.
     		}
     		else {
             	_alert("ERROR!  Possible cheating attempt to grant " + coins + " coins on MobDeals transaction: "+transaction+".");
     		}
     	}
     	catch(JSONException e) {
         	_alert("Error processing purchase confirmation: " + e.getMessage());
     		Log.e("MOBDEALS", "Couldn't process purchase confirmation: " + confirmationJson);
     		Log.e("MOBDEALS", e.getMessage());
     	}
     }
 
     /**
      * Verifies whether or not the signature on the report of the purchase matches what we expect it to,
      * using the secret key for this application (stored in an input).
      * 
      * @param coinsStr String representation of the number of coins credited.
      * @param timestamp The timestamp of the request, so that a hacker/cheater can't re-run requests
      * @param transaction The transaction number of the transaction.
      * @param deliveredSignature The signature that came back with the request
      * @return Whether or not the deliveredSignature matched what we expected it to.
      */
     private boolean verifySignature(String coinsStr, String timestamp, String transaction, String deliveredSignature) {
     	String msg = "" + coinsStr + timestamp + transaction + apiSecretText.getText();
     	try {
     		// Create MD5 Hash
     	    MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
     	    digest.update(msg.getBytes());
     	    byte messageDigest[] = digest.digest();
     	        
     	    // Create Hex String
     	    StringBuffer hexString = new StringBuffer();
     	    for (int i=0; i<messageDigest.length; i++) {
     	        String h = Integer.toHexString(0xFF & messageDigest[i]);
                 while (h.length() < 2) h = "0" + h;
                 hexString.append(h);
     	    }
     	    return hexString.toString().equals(deliveredSignature);
     	        
     	} 
     	catch (NoSuchAlgorithmException e) {
         	_alert("Error checking purchase confirmation signature: " + e.getMessage());
     	    e.printStackTrace();
     	}
     	return false;
     }
     
     /**
      * Utility function that just displays an alert popup to the user.
      * @param msg The message to display in the alert dialog.
      */
     private void _alert(String msg) {
     	AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setMessage(msg)
 				.setCancelable(true)
 		        .setNegativeButton("Ok", new DialogInterface.OnClickListener() {
 		        	public void onClick(DialogInterface dialog, int id) {
 		        		dialog.cancel();
 		            }
 		        });
 		AlertDialog alert = builder.create();
 		alert.show();
     }
 
     /**
      * Utility function to clear all the text labels for the offer.
      */
     private void _clearOffer() {
     	currentOffer = null;
 		textOfferName.setText("");
 		textOfferDiscount.setText("");
 		textOfferCurrency.setText("");
 		textOfferLocation.setText("");
 		textOfferExpires.setText("");
     }
     
     /**
      * Turns an InputStream into a string
      * @param is an InputStream to read into a string
      * @return a string that is the full contents of the InputStream
      */
     private static String _convertStreamToString(InputStream is) {
         BufferedReader reader = new BufferedReader(new InputStreamReader(is));
         StringBuilder sb = new StringBuilder();
  
         String line = null;
         try {
             while ((line = reader.readLine()) != null) {
                 sb.append(line + "\n");
             }
         } 
         catch (IOException e) { e.printStackTrace(); } 
         finally {
             try { is.close(); } 
             catch (IOException e) { e.printStackTrace(); }
         }
         return sb.toString();
     }
 }
