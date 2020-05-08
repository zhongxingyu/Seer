 package com.main.passthedoodle;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.Header;
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 
 import com.main.passthedoodle.LoginActivity.UserLoginTask;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.net.http.AndroidHttpClient;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 import android.widget.Toast;
 
 
 public class TextActivity extends Activity {
 	TextView promptTextView;
     ImageView drawingImageView;
     ProgressBar spinningCircle;
     EditText submitText;
     
     private SubmitTextTask mTextTask = null;
 
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_text);		
 		submitText = (EditText) findViewById(R.id.text);
 		promptTextView = (TextView) findViewById(R.id.prompt_text_view);
 		drawingImageView = (ImageView) findViewById(R.id.drawing_image_view);
 		spinningCircle = (ProgressBar) findViewById(R.id.loading_image_circle);
 		
 		promptTextView.setText(getPromptString());
 		
 		// false is default (launched from main menu)
 		if (getIntent().getBooleanExtra("isLocal", false) == true)
 			new LocalDrawingTask().execute("dummy");
 		else
 			new DownloadDrawingTask().execute(getImageURL());
 	}
 	
 	private String getPromptString() {
 		return "You are guessing ____'s doodle";
 	}
 	
 	private String getImageURL() {
 		return "http://i.imgur.com/t2APljR.gif";
 	}
 
 	private class DownloadDrawingTask extends AsyncTask<String, Void, Bitmap> {
 
 		protected void onPreExecute() {
 			// display spinning progress circle before image loads
 			spinningCircle.setVisibility(View.VISIBLE);
             super.onPreExecute();
 		}
         protected Bitmap doInBackground(String... urls) {
         	// params comes from the execute() call: params[0] is the url.
         	return downloadBitmap(urls[0]);
         }
         
         // onPostExecute displays the results of the AsyncTask.
         @Override
         protected void onPostExecute(Bitmap result) {
         	// hide spinning progress circle once image is finished loading
         	spinningCircle.setVisibility(View.GONE);
             drawingImageView.setImageBitmap(result);
        }
     }
 	
 	private class LocalDrawingTask extends AsyncTask<String, Void, Bitmap> {
 
 		protected void onPreExecute() {
 			// display spinning progress circle before image loads
 			spinningCircle.setVisibility(View.VISIBLE);
             super.onPreExecute();
 		}
         protected Bitmap doInBackground(String... dummy) {
         	// Local play - passed from DrawingActivity
     		Bundle extras = getIntent().getExtras();
     		byte[] byteArray = extras.getByteArray("Image");
 
     		return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
         }
         
         // onPostExecute displays the results of the AsyncTask.
         @Override
         protected void onPostExecute(Bitmap result) {
         	// hide spinning progress circle once image is finished loading
         	spinningCircle.setVisibility(View.GONE);
             drawingImageView.setImageBitmap(result);
        }
     }
 	
 	private static Bitmap downloadBitmap(String url) {
         final AndroidHttpClient client = AndroidHttpClient.newInstance("Android");
         final HttpGet getRequest = new HttpGet(url);
         
         try {
             HttpResponse response = client.execute(getRequest);
             final int statusCode = response.getStatusLine().getStatusCode();
             if (statusCode != HttpStatus.SC_OK) {
                 Log.w("DownloadDrawing", "Error " + statusCode
                         + " while retrieving bitmap from " + url);
                 return null;
             }
  
             final HttpEntity entity = response.getEntity();
             if (entity != null) {
                 InputStream inputStream = null;
                 try {
                     inputStream = entity.getContent();
                     final Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                     return bitmap;
                 } finally {
                     if (inputStream != null) {
                         inputStream.close();
                     }
                     entity.consumeContent();
                 }
             }
         } catch (Exception e) {
             // Could provide a more explicit error message for IOException or
             // IllegalStateException
             getRequest.abort();
             Log.w("DownloadDrawing", "Error while retrieving bitmap from " + url);
         } finally {
             if (client != null) {
                 client.close();
             }
         }
         return null;
     }
 	
 	public void sendText(View view) {
 		
 		// false is default (launched from main menu)
 		if (getIntent().getBooleanExtra("isLocal", false) == true) {
 			Intent intent = new Intent(this, DrawingActivity.class);
 		    EditText passText = (EditText) findViewById(R.id.text);
 		    intent.putExtra("Text", passText.getText().toString());
 		    startActivity(intent);
 		}
 		else {			
 		    mTextTask = new SubmitTextTask();
 		    mTextTask.execute(submitText.getText().toString());
 		}
 		
 
 		
 	}
 	
     public class SubmitTextTask extends AsyncTask<String, Void, Integer> {
         @Override
         protected Integer doInBackground(String... arg0) {
         	HttpClient httpClient = new DefaultHttpClient();
         	HttpPost httpPost = new HttpPost("http://passthedoodle.com/test/mtext.php");
 			BasicNameValuePair descriptionBasicNameValuePair = new BasicNameValuePair("description", arg0[0]);
 			//BasicNameValuePair sessionBasicNameValuePAir = new BasicNameValuePair("PHPSESSID", arg0[1]);
 			
 			List<NameValuePair> nameValuePairList = new ArrayList<NameValuePair>();
 			nameValuePairList.add(descriptionBasicNameValuePair);
 			//nameValuePairList.add(sessionBasicNameValuePAir);
 			
 			int responseCode = 0;
 			
             try {
             	// TODO: secure this or you're fired (https and cert)
                 UrlEncodedFormEntity urlEncodedFormEntity = new UrlEncodedFormEntity(nameValuePairList);
                 httpPost.setEntity(urlEncodedFormEntity);
                 
                 // send POST data
                 HttpResponse response = httpClient.execute(httpPost);
                 
                 // get stuff back
                 responseCode = response.getStatusLine().getStatusCode();
                 
             } catch (UnsupportedEncodingException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (ClientProtocolException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
             
             Log.d("Status", "statusCode: " + responseCode);
             return responseCode;
         }
 
         @Override
         protected void onPostExecute(Integer headerCode) {
             //mAuthTask = null;
             //showProgress(false);
             
             // We could probably be more specific here about what we tell the user
             //but this should be okay for now
             if (headerCode == 202) { //Authorized
             	// TODO: Alter this to send user to the game menu
                 Toast.makeText(getApplicationContext(), "Description submitted!", Toast.LENGTH_LONG).show();
                 Intent intent = new Intent(getApplicationContext(), GameActivity.class);
                 startActivity(intent);
             } else if (headerCode == 401) { //Unauthorized
                 Toast.makeText(getApplicationContext(), "Incorrect username/password!", Toast.LENGTH_LONG).show();
             } else if (headerCode >= 500 && headerCode <= 600) { //Server problem (better fix it!)
                 Toast.makeText(getApplicationContext(), "Server error", Toast.LENGTH_LONG).show();            
             } else { //Some other code (likely broken)
             	Toast.makeText(getApplicationContext(), "Unknown error", Toast.LENGTH_LONG).show();
             }
         }
 
         @Override
         protected void onCancelled() {
             //mAuthTask = null;
             //showProgress(false);
         }
     }
 }
