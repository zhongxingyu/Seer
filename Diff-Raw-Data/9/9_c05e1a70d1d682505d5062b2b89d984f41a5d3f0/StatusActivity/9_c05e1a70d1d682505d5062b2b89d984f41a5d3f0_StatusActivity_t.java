 package dk.lang.android.yamba;
 
 import com.android.debug.hv.ViewServer;
 
 import winterwell.jtwitter.Twitter;
 import winterwell.jtwitter.TwitterException;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Color;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class StatusActivity extends Activity implements OnClickListener, TextWatcher {
 	private static final String TAG = "StatusActivity";
 	EditText editText;
 	Button updateButton;
 	TextView textCount;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		ViewServer.get(this).addWindow(this); /* only used for debugging */
 		setContentView(R.layout.activity_status);
 		
 		// Find views
 		editText = (EditText) findViewById(R.id.editText);
 		updateButton = (Button) findViewById(R.id.buttonUpdate);
 		updateButton.setOnClickListener(this);
 
 		textCount = (TextView) findViewById(R.id.textCount);
 		textCount.setText(Integer.toString(140));
 		textCount.setTextColor(Color.GREEN);
 		editText.addTextChangedListener(this);
 
 	}
 	public void onDestroy() { /* only used for debugging */
 		super.onDestroy();
 		ViewServer.get(this).removeWindow(this);
 	}
 	public void onResume() { /* only used for debugging */
 		super.onResume();
 		ViewServer.get(this).setFocusedWindow(this);
 	}
 	
 	// Asynchronously post to twitter 
 	// (without this, we will get an android.os.NetworkOnMainThreadException)
 	// Gingerbread let you do networking on the UI threat, from Honeycomb this is no longer possible
 	class PostToTwitter extends AsyncTask<String, Integer, String> {
 		// Called to initiate the background activity
 		@Override
 		protected String doInBackground(String... statuses) {
 			try {
 				YambaApplication yamba = ((YambaApplication) getApplication());
 				Twitter.Status status = yamba.getTwitter().updateStatus(statuses[0]);
 				return status.text;
 			} catch (TwitterException e) {
 				Log.e(TAG, e.toString());
 				e.printStackTrace();
 				return "Failed to post";
 			}
 		}
 		// Called when there is a status to be updated
 		@Override
 		protected void onProgressUpdate(Integer... values) {
 			super.onProgressUpdate(values);
 			// Not used in this case (but java demands that it is implemented anyway .. )
 		}
 		
 		// Called once the background activity has completed
 		@Override
 		protected void onPostExecute(String result) {
 			Toast.makeText(StatusActivity.this, result, Toast.LENGTH_LONG).show();
 		}
 		
 	}
 	// Called when button is clicked //
 	public void onClick(View v) {
 		String status = editText.getText().toString();
 		new PostToTwitter().execute(status);
 		Log.d(TAG, "onClicked");
 	}
	/*
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.menu, menu);
 		return true;
 	}
	*/
	// TODO: this has been moved to the BaseActivity
	/*
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch(item.getItemId()) {
 		case R.id.itemServiceStart:
 			startService(new Intent(this, UpdaterService.class));
 		break;
 		case R.id.itemServiceStop:
 			stopService(new Intent(this, UpdaterService.class));			
 		break;
 		case R.id.itemPrefs:
 			startActivity(new Intent(this, PrefsActivity.class));
 		break;
 		}
 		return true;
 	}
	*/
 	
 	// TextWatcher methods
 	public void afterTextChanged(Editable statusText) {
 		int count = 140 - statusText.length();
 		textCount.setText(Integer.toString(count));
 		textCount.setTextColor(Color.GREEN);
 		if (count < 10) 
 			textCount.setTextColor(Color.YELLOW);
 		if (count < 0)
 			textCount.setTextColor(Color.RED);
 	}
 	public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
 	public void onTextChanged(CharSequence s, int start, int before, int count) {}
 }
