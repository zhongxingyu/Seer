 package com.tappt.android;
 
 import java.nio.charset.Charset;
 
 import com.tappt.android.util.TapptRestClient;
 
 import android.animation.Animator;
 import android.animation.AnimatorListenerAdapter;
 import android.annotation.TargetApi;
 import android.app.Activity;
 import android.nfc.NdefMessage;
 import android.nfc.NdefRecord;
 import android.nfc.NfcAdapter;
 import android.nfc.NfcEvent;
 import android.nfc.NfcAdapter.CreateNdefMessageCallback;
 import android.os.AsyncTask;
 import android.os.Build;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.widget.Toast;
 
 public class Tap extends Activity
 	implements CreateNdefMessageCallback {
 
 	private String pourKey;
 	
 	private AuthorizeToken mAuthTask;
 	
 	private View mLoginStatusView;
 	
 	private NfcAdapter mNfcAdapter;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_tap);
 
 		if (mAuthTask != null){
 			return;
 		}
 		
 		mLoginStatusView = findViewById(R.id.login_status);
 	}
 
 	@Override
 	protected void onStart() {
 		super.onStart();
 		
 		showProgress(true);
 		mAuthTask = new AuthorizeToken();
 		mAuthTask.execute((Void) null);
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.tap, menu);
 		return true;
 	}
 	
 	@Override
     public NdefMessage createNdefMessage(NfcEvent event){
         
         String message = this.pourKey;
         
         NdefMessage msg;
                 
                     msg = new NdefMessage(new NdefRecord[] {
                             createApplicationRecord(message.getBytes())
                     });
                     return msg;
                 
             
     }     
     
     private NdefRecord createApplicationRecord(byte[] payload)
     {    
     	byte[] mimeBytes = new String("application/" + this.getPackageName()).getBytes(Charset.forName("US-ASCII"));  
 
         //return NdefRecord.createMime(mimeType, mimeBytes);
        NdefRecord mimeRecord = new NdefRecord(NdefRecord.TNF_MIME_MEDIA, mimeBytes, null, payload);
         return  mimeRecord;
         
     }
 	
 	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
 	private void showProgress(final boolean show) {
 		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
 		// for very easy animations. If available, use these APIs to fade-in
 		// the progress spinner.
 		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
 			int shortAnimTime = getResources().getInteger(
 					android.R.integer.config_shortAnimTime);
 
 			mLoginStatusView.setVisibility(View.VISIBLE);
 			mLoginStatusView.animate().setDuration(shortAnimTime)
 					.alpha(show ? 1 : 0)
 					.setListener(new AnimatorListenerAdapter() {
 						@Override
 						public void onAnimationEnd(Animator animation) {
 							mLoginStatusView.setVisibility(show ? View.VISIBLE
 									: View.GONE);
 						}
 					});
 		} else {
 			// The ViewPropertyAnimator APIs are not available, so simply show
 			// and hide the relevant UI components.
 			mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
 		}
 	}
 	
 	/**
 	 * Represents an asynchronous login/registration task used to authenticate
 	 * the user.
 	 */
 	public class AuthorizeToken extends AsyncTask<Void, Void, String> {
 		@Override
 		protected String doInBackground(Void... params) {
 			// TODO: attempt authentication against a network service.
 
 			return TapptRestClient.Tap();
 		}
 
 		@Override
 		protected void onPostExecute(final String success) {
 			showProgress(false);
 
 			if (success != null && !success.isEmpty()) {
 				pourKey = success;
 
 				mNfcAdapter = NfcAdapter.getDefaultAdapter(Tap.this);
 		        if (mNfcAdapter == null) {
 		            Toast.makeText(Tap.this, "NFC is not available", Toast.LENGTH_LONG).show();
 		            finish();
 		            return;
 		        }
 		        // Register callback
 		        mNfcAdapter.setNdefPushMessageCallback(Tap.this, Tap.this);
 			} else {
 				// show error
				Toast.makeText(Tap.this, "Could not get authentication key.  The service may be down.", Toast.LENGTH_LONG).show();
				finish();
 			}
 		}
 		
 		@Override
 		protected void onCancelled() {
 			mAuthTask = null;
 			showProgress(false);
 		}
 	}
 }
