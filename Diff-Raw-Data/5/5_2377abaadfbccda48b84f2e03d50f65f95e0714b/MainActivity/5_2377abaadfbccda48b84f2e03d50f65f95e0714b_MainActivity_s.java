 package com.op.cookit;
 
 import com.google.gson.Gson;
 import com.op.cookit.model.Product;
 import com.op.cookit.util.SystemUiHider;
 
 import android.annotation.TargetApi;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Build;
 import android.os.Bundle;
 import android.os.Handler;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.View;
 import android.widget.TextView;
 
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.springframework.http.converter.StringHttpMessageConverter;
 import org.springframework.web.client.RestClientException;
 import org.springframework.web.client.RestTemplate;
 
 import jim.h.common.android.zxinglib.integrator.IntentIntegrator;
 import jim.h.common.android.zxinglib.integrator.IntentResult;
 import android.os.*;
 
 
 /**
  * An example full-screen activity that shows and hides the system UI (i.e.
  * status bar and navigation/system bar) with user interaction.
  *
  * @see SystemUiHider
  */
 public class MainActivity extends Activity {
     /**
      * Whether or not the system UI should be auto-hidden after
      * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
      */
     private static final boolean AUTO_HIDE = true;
 
     private Handler  handler = new Handler();
     private TextView txtScanResult;
	RetreiveFeedTask rt = new RetreiveFeedTask();
     /**
      * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
      * user interaction before hiding the system UI.
      */
     private static final int AUTO_HIDE_DELAY_MILLIS = 3000;
 
     /**
      * If set, will toggle the system UI visibility upon interaction. Otherwise,
      * will show the system UI visibility upon interaction.
      */
     private static final boolean TOGGLE_ON_CLICK = true;
 
     /**
      * The flags to pass to {@link SystemUiHider#getInstance}.
      */
     private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;
 
     /**
      * The instance of the {@link SystemUiHider} for this activity.
      */
     private SystemUiHider mSystemUiHider;
 
     class RetreiveFeedTask extends AsyncTask<String, Void, String> {
 
         private Exception exception;
 		
 		public Product product;
 
         protected String doInBackground(String... urls) {
             String url = "http://cookcloud.jelastic.neohost.net/rest/barcode/" + urls[0];
             // Create a new RestTemplate instance
             RestTemplate restTemplate = new RestTemplate();
             // Add the String message converter
             restTemplate.getMessageConverters().add(new StringHttpMessageConverter());
 
             // Make the HTTP GET request, marshaling the response to a String
             try {
                 String result = (String)restTemplate.getForObject(url, String.class);
                // txtScanResult.setText(result);
               //  ObjectMapper mapper = new ObjectMapper();
               //  product = mapper.readValue(result, Product.class);
                 product =  new Gson().fromJson(result, Product.class);
 
 				Log.d(">>", ""+ result + " prod:" + product);
             } catch (Exception e) {
                 e.printStackTrace();
             }
 
 
             return "";
 
         }
 
         protected void onPostExecute(String result)
         {
             if (product != null){
                 txtScanResult.setText(product.getName());
             } else {
                 txtScanResult.setText("not found");
             }
         }
 
 
     }
 	
 	private static final int UPDATE_IMAGE = 0;
 	
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         setContentView(R.layout.activity_main);
 
 
         txtScanResult = (TextView) findViewById(R.id.scan_result);
         txtScanResult.setText("");
 		View btnScan = findViewById(R.id.scan_button);
         // Scan button
         btnScan.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 // set the last parameter to true to open front light if available
                 IntentIntegrator.initiateScan(MainActivity.this, R.layout.capture,
                         R.id.viewfinder_view, R.id.preview_view, true);
             }
         });
     }
 
     @Override
     protected void onPostCreate(Bundle savedInstanceState) {
         super.onPostCreate(savedInstanceState);
 
     }
 
 
 
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         super.onActivityResult(requestCode, resultCode, data);
         switch (requestCode) {
             case IntentIntegrator.REQUEST_CODE:
                 IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode,
                         resultCode, data);
                 if (scanResult == null) {
                     return;
                 }
                 final String result = scanResult.getContents();
                 if (result != null) {
                     handler.post(new Runnable() {
                         @Override
                         public void run() {
 							
 							
							rt.execute(result);
                           //  txtScanResult.setText(result);
                         }
                     });
                 }
                 break;
             default:
         }
     }
 
 }
