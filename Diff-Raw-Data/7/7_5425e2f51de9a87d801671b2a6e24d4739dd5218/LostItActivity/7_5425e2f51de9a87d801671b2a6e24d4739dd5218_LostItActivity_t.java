 package com.example.foundit;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.StrictMode;
 import android.provider.MediaStore;
 import android.view.Menu;
 import android.view.View;
 import android.widget.EditText;
 
 public class LostItActivity extends Activity {
 
 	EditText nameField;
 	EditText descField;
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_found_it);
 		if (android.os.Build.VERSION.SDK_INT > 9) {
 		      StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
 		      StrictMode.setThreadPolicy(policy);
 		    }
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_found_it, menu);
 		return true;
 	}
 	
 	public void uploadItem(View view){
 		
 		InfoTask info = new InfoTask();
 		info.execute();
 	}
 	//called to launch camera application
 	public void uploadPhoto(View view){
 		//this is how to handle the photo upload
 		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
 		startActivityForResult(takePictureIntent, 0);
 	}
 	
 private class InfoTask extends AsyncTask<Void, Void, String[]> {
 		ProgressDialog progress;
 		Boolean uploadSuccessful = false;
 		@Override 
 		protected void onPostExecute(String[] result) {
 	    	 	progress.dismiss();
                 String message;
 	    	 	if (uploadSuccessful){
 	    	 		message = "Your upload was successful!";
 	    	 	}
 	    	 	else {
 	    	 		message = "Your upload failed :(";
 	    	 	}
     	 		AlertDialog.Builder builder = new AlertDialog.Builder(LostItActivity.this);
     	 		builder.setMessage(message);
     	 		builder.setCancelable(false);
     	 		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
     	 		public void onClick(DialogInterface dialog, int id) {
     	 			//do things
     	 		    finish();
     	 		  }
     	 		});
     	 		AlertDialog alert = builder.create();
     	 		alert.show();
          }
 	     @Override
          protected void onPreExecute() {
 	    	 progress = ProgressDialog.show(LostItActivity.this, "","Loading...");
          }
 		@Override
 		protected String[] doInBackground(Void... params) {
 			// TODO Auto-generated method stub
 			nameField = (EditText)findViewById(R.id.nameField);
 			String name = nameField.getText().toString();
 			
 			descField = (EditText)findViewById(R.id.descriptionField);
 			String description = descField.getText().toString();
 			
 			HttpClient client = new DefaultHttpClient();
 		    HttpPost post = new HttpPost("http://foundit.andrewl.ca/postings");
 		    try {
 		      List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
 		      nameValuePairs.add(new BasicNameValuePair("posting[posting_type]", "1"));
 		      nameValuePairs.add(new BasicNameValuePair("posting[name]", name));
 		      nameValuePairs.add(new BasicNameValuePair("posting[description]", description)); 
 		      post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
 		 
 		      HttpResponse response = client.execute(post);
 		     if (response.getStatusLine().getStatusCode()  == 200) {
 		    	  uploadSuccessful = true;
 		      }
 		    } catch (IOException e) {
 		      e.printStackTrace();
 		    }
 		    
 		    return new String[1];
 		}
 	 }
 
 }
