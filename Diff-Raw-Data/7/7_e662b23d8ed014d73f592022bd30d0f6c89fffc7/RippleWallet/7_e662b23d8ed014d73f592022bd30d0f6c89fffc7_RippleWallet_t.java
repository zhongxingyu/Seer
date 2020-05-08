 package org.icehat.ripplewallet;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.content.Intent;
 import android.os.AsyncTask;
 
 import org.json.JSONObject;
 import org.json.JSONException;
 
 /** Starting activity with a login screen. On login, it retrieves a relevant
  *  blob from the blobvault and shifts to the balance activity.
  *
  *  @author Matthías Ragnarsson
  *  @author Pétur Karl Ingólfsson
  *  @author Daniel Eduardo Pinedo Quintero
  *  @author Sigyn Jónsdóttir
  *  @version Thu Oct 17 16:33:37 GMT 2013
  */
 public class RippleWallet extends Activity
 {
     public SharedResources resources;
     public static EditText walletName;
     public static EditText passphrase;
     public static TextView loginMessage; 
     public static GetBlobTask getBlobTask;
     public static String TAG;
     public static boolean isGettingBlob = false;
 
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         TAG = getString(R.string.log_tag);
         resources = (SharedResources) getApplicationContext();
         walletName = (EditText) findViewById(R.id.wallet_name);
         passphrase = (EditText) findViewById(R.id.passphrase);
         loginMessage = (TextView) findViewById(R.id.login_message);
     }
 
     /** Populates wallet name and passphrase for easier demoing.
      */
     public void insertDemo(View v) {
     	walletName.setText("demo");
     	passphrase.setText("demopass1");
     }
 
     /** Attempts login with current wallet name and passphrase.
      *
      *  @throws JSONException
      */
     public void logIn(View v) {
         
         if (!isGettingBlob) {
             getBlobTask = new GetBlobTask();
             String walletName = this.walletName.getText().toString();
             String passphrase = this.passphrase.getText().toString();
             getBlobTask.execute(walletName, passphrase);
             Log.d(TAG, "Getting blob with " + "walletName: " + walletName + ", passphrase: " + passphrase);
         }
     }
     
     /** Shifts to Balance Activity with blob in Intent.
      *
      *  @param balance Balance of the account.
      */
     public void toBalance(JSONObject blob) {
         Intent intent = new Intent(this, Balance.class);
         intent.putExtra("blob", blob.toString()); // Find a way bypass string conversion later.
         intent.putExtra("login", true);
         startActivity(intent);
     }
     
     /** A task to get the blob asynchronously from the blobvault.
      */
     private class GetBlobTask extends AsyncTask<String, String, JSONObject> {
         
         @Override
        protected void onPreExecute() {
            isGettingBlob = true;
        }
        
        @Override
         protected void onPostExecute(final JSONObject blob) {
             getBlobTask = null;
             if (blob == null) {
                 Log.d(TAG, "Failed blob retrieval. Returned null.");
             }
             else {
                 Log.d(TAG, "Successful blob retrieval:\n" + blob.toString());
                 toBalance(blob);
             }
             isGettingBlob = false;
         }
 
         @Override
         protected JSONObject doInBackground(String... credentials) {
             try {
                 return resources.paywardBlobVault.getBlob(credentials[0], credentials[1]);
             } catch (Exception e) {
                 Log.d(TAG, "Invalid wallet name or passphrase.");
                 return null;
             }
         }
     }
 }
