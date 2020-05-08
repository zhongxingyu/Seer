 package edu.android.podcast_listener;
 
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.net.URLConnection;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.support.v4.app.NavUtils;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import edu.android.podcast_listener.util.PodcastConstants;
 
 public class FindCastsActivity extends Activity {
 	private EditText urlInput;
 	Button button;
 	ValidateUrlTask urlTask;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_find_casts);
 		// Show the Up button in the action bar.
 		getActionBar().setDisplayHomeAsUpEnabled(true);
 		urlInput = (EditText) findViewById(R.id.editText1);
 		
 		button = (Button) findViewById(R.id.button1);
 		button.setOnClickListener(new OnClickListener() {			
 			@Override
 			public void onClick(View v) {
 				urlTask = new ValidateUrlTask(FindCastsActivity.this);
 				urlTask.execute();
 			}
 		});
 		
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_find_casts, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			// This ID represents the Home or Up button. In the case of this
 			// activity, the Up button is shown. Use NavUtils to allow users
 			// to navigate up one level in the application structure. For
 			// more details, see the Navigation pattern on Android Design:
 			//
 			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
 			//
 			NavUtils.navigateUpFromSameTask(this);
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 	
 	public void toResults(View view) {
		String podcastUrl = getEntredURL();
 		Intent intent = new Intent(this, FindCastsResultsActivity.class);			
 		intent.putExtra(PodcastConstants.EXTRA_MESSAGE, podcastUrl);
 		startActivity(intent);
 	}
 	
 	public String getEntredURL() {
		String urlString = urlInput.getText().toString();
		if (urlString.startsWith("http://") || urlString.startsWith("HTTP://")) {
			return urlString;
		} else {
			return "http://" + urlString;
		}
 	}
 	
 	static class ValidateUrlTask extends AsyncTask<Void, Void, Boolean> {
 		FindCastsActivity activity;
 		ProgressDialog progressDialog;
 		
 		ValidateUrlTask(FindCastsActivity activity) {
 			attach(activity);
 		}
 		
 		@Override
 		protected void onPreExecute() {
 			progressDialog = new ProgressDialog(activity.getApplicationContext());
 			progressDialog = ProgressDialog.show(activity,"","Validating URL.", true,false);
 			super.onPreExecute();
 		}
 		
 		@Override
 		protected Boolean doInBackground(Void... urls) {
 			// TODO Auto-generated method stub
 			boolean isHttpOk = false;
 			HttpURLConnection httpConn = null;
 			
 			try {
 				URL url = new URL(activity.getEntredURL());
 				URLConnection urlConn = url.openConnection();
 				urlConn.setConnectTimeout(5000);
 				httpConn = (HttpURLConnection) urlConn;				
 			    int responseCode = httpConn.getResponseCode();
 			    if (responseCode == HttpURLConnection.HTTP_OK) {
 			    	isHttpOk = true;
 			    }
 				return isHttpOk;
 			} catch (Exception e) {
 				Log.e(PodcastConstants.ERROR_TAG, "Wish I knew why this exception handling isn't working: " + e.getMessage());
 			} finally {
 				if (httpConn != null) {
 					httpConn.disconnect();
				}					
 			}
 			return isHttpOk;
 		}
 		
 		@Override
 		protected void onPostExecute(Boolean valid) {
 			progressDialog.dismiss();
 			if (valid) {
 				activity.toResults(activity.urlInput);
 			} else {
 				podcastNotFoundDialog();
 			}
 			super.onPostExecute(valid);
 		}
 		
 		void podcastNotFoundDialog() {
 			AlertDialog dialog;
 			AlertDialog.Builder builder = new AlertDialog.Builder(activity);
 			builder.setTitle(PodcastConstants.ALERT_TITLE);
 			builder.setMessage(PodcastConstants.ALERT_MESSAGE);
 			builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {					
 				public void onClick(DialogInterface dialog, int id) {
 					dialog.dismiss();
 				}
 			});
 			dialog = builder.create();
 			dialog.show();
 		}
 		
 		void attach(FindCastsActivity activity) {
 		      this.activity=activity;
 	    }
 	}
 }
