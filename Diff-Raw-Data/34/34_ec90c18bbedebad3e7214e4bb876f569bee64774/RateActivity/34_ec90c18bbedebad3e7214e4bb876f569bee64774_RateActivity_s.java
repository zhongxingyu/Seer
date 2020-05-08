 package net.fhtagn.zoobeditor.browser;
 
 import java.io.IOException;
 
 import net.fhtagn.zoobeditor.EditorConstants;
 import net.fhtagn.zoobeditor.R;
 import net.fhtagn.zoobeditor.Series;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 import android.content.ContentUris;
 import android.content.ContentValues;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.util.Log;
 
 public class RateActivity extends ServerRequestActivity {
 	static final String TAG = "RateActivity";
 	
 	private long communityID = -1;
 	private float rating;
 
 	@Override
 	public void onCreate (Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		Intent intent = getIntent();
 		if (!intent.hasExtra("community_id") || !intent.hasExtra("rating")) {
 			Log.e(TAG, "no community_id or rating in intent");
 			setResult(EditorConstants.RESULT_ERROR);
 			finish();
 		}
 		communityID = intent.getExtras().getLong("community_id");
 		rating = intent.getExtras().getFloat("rating");
 	}
 	
 	@Override
   protected boolean doRequest(DefaultHttpClient httpClient) {
 	  try {
 	  	if (communityID == -1) {
 	  		Log.e(TAG, "rate with communityID == -1 || rating == -1");
 	  		setResult(EditorConstants.RESULT_ERROR);
 	  		return false;
 	  	}
 	  	
 	  	HttpGet httpGet = new HttpGet(EditorConstants.getRateUrl(communityID, rating));
 	  	HttpResponse response = httpClient.execute(httpGet);
 	  	if (response.getStatusLine().getStatusCode() != 200) {
	  		errorDialog("Error deleting  : " + response.getStatusLine().getReasonPhrase());
 	  		return false;
 	  	}
 	  	
 	  	//store updated my_rating in db
 	  	Uri updateUri = ContentUris.withAppendedId(Series.CONTENT_URI, communityID);
 	  	ContentValues values = new ContentValues();
 	  	values.put(Series.MY_RATING, rating);
 	  	getContentResolver().update(updateUri, values, null, null);
 	  	
 	  	return true;
 	  } catch (IOException e) {
 	  	e.printStackTrace();
 	  }
 	  return false;
   }
 
 }
