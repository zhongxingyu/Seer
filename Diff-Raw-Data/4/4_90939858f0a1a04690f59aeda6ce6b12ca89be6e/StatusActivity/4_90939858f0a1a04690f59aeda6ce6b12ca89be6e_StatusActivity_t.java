 package com.example.xom.marakana.yamba1;
 
 import winterwell.jtwitter.Twitter;
 import winterwell.jtwitter.TwitterException;
 
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.app.Activity;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.Menu;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Toast;
 
 public class StatusActivity extends Activity implements OnClickListener {
 	
 	private final static String TAG = "StatusActivity";
 	
 	private EditText mEditText;
 	private Twitter mTwitter;
 	private Button mBtnUpdate;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		
 		mEditText = (EditText) findViewById(R.id.inputText);
 		mBtnUpdate = (Button) findViewById(R.id.btnUpdate);
 		mBtnUpdate.setOnClickListener(this);
 		
 		mTwitter = new Twitter("student","password");
 		mTwitter.setAPIRootUrl("http://yamba.marakana.com/api");
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
 	@Override
 	public void onClick(View v) {
 		String status = mEditText.getText().toString();
 		
 		(new PostToTwitter()).execute(status);
 		
 		Log.d(TAG,"onClicked");
 	}
 	
 	private class PostToTwitter extends AsyncTask<String,Integer,String> {
 
 		@Override
 		protected String doInBackground(String... params) {
 			try {
				Twitter.Status status = mTwitter.updateStatus(params[0]);
 				return status.text;
 			}
 			catch(TwitterException e) {
 				Log.e(TAG, e.toString());
 				return "Failed to post";
 			}
 		}
 
 		@Override
 		protected void onPostExecute(String result) {
 			super.onPostExecute(result);
 			
 			Toast.makeText(StatusActivity.this, result, Toast.LENGTH_LONG).show();
 		}
 	}
 
 }
