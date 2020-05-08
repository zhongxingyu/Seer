 package com.example.foundit;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.IOException;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.mime.HttpMultipartMode;
 import org.apache.http.entity.mime.MultipartEntity;
 import org.apache.http.entity.mime.content.FileBody;
 import org.apache.http.entity.mime.content.StringBody;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 import android.app.ActionBar;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Color;
 import android.graphics.drawable.ColorDrawable;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.StrictMode;
 import android.provider.MediaStore;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.ImageView;
 
 public class FoundItActivity extends Activity {
 	File f;
 	Boolean tookPhoto = false;
 	EditText nameField;
 	EditText descField;
 	ImageView takenPhoto;
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_found_it);
 		ActionBar actionBar = getActionBar();
 		actionBar.setIcon(R.drawable.foundit_final_small);
 		actionBar.setTitle("");
 		actionBar.setBackgroundDrawable(new ColorDrawable(Color.rgb(117, 150, 194)));
 		takenPhoto = (ImageView) findViewById(R.id.takenPhoto);
 		takenPhoto.setVisibility(8);
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
 		Uri imgUri = null;
 		//this is how to handle the photo upload
 		try {
 			
 			f = File.createTempFile("tmp", ".jpg", Environment.getExternalStorageDirectory());
 			imgUri = Uri.fromFile(f);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
 		takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imgUri);
 		takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
 		startActivityForResult(takePictureIntent, 1);
 	}
 	
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {  
 		if (resultCode == Activity.RESULT_OK) {  
 			tookPhoto = true;
 			takenPhoto.setVisibility(1);
 			Bitmap image = BitmapFactory.decodeFile(f.toString());
 			float width = image.getWidth();
 			float height = image.getHeight();
 			Bitmap compressedImage;
 			
 			
 			if(width > height){
 				compressedImage = Bitmap.createScaledBitmap(image, 600, 400, false);
 			}
 			else{
 				compressedImage = Bitmap.createScaledBitmap(image, 400, 600, false);
 			}
 				
 			
 			//ByteArrayOutputStream out = new ByteArrayOutputStream();
 			System.out.println(image.getByteCount());
 			
 		    /*image.compress(Bitmap.CompressFormat.PNG, 5, out);
 		    Bitmap compressedImage = BitmapFactory.decodeStream(new ByteArrayInputStream(out.toByteArray()));*/
 		    System.out.println(compressedImage.getByteCount());
 			takenPhoto.setImageBitmap(compressedImage);
 	    }  
 	} 
 	
 private class InfoTask extends AsyncTask<Void, Void, String[]> {
 		ProgressDialog progress;
 		Boolean uploadSuccessful = false;
 		@Override 
 		protected void onPostExecute(String[] result) {
 	    	 	progress.dismiss();
                 String message;
 	    	 	if(!tookPhoto){
 	    	 		message = "Please take a photo to upload";		    	 	
 		    	 	
 	    	 		AlertDialog.Builder builder = new AlertDialog.Builder(FoundItActivity.this);
 	    	 		builder.setMessage(message);
 	    	 		builder.setCancelable(false);
 	    	 		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
 	    	 		public void onClick(DialogInterface dialog, int id) {
 	    	 			//do things
 	    	 		    //finish();
 	    	 		  }
 	    	 		});
 	    	 		AlertDialog alert = builder.create();
 	    	 		alert.show();
 	    	 	}
 	    	 	else{	
 		    	 	if (uploadSuccessful){
 		    	 		message = "Your upload was successful!";
 		    	 	}
 		    	 	else {
 		    	 		message = "Your upload failed :(";
 		    	 	}
 	    	 		AlertDialog.Builder builder = new AlertDialog.Builder(FoundItActivity.this);
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
          }
 	     @Override
          protected void onPreExecute() {
 	    	 progress = ProgressDialog.show(FoundItActivity.this, "","Loading...");
          }
 		@Override
 		protected String[] doInBackground(Void... params) {
 			// TODO Auto-generated method stub
 			nameField = (EditText)findViewById(R.id.nameField);
 			String name = nameField.getText().toString();
 			
 			descField = (EditText)findViewById(R.id.descriptionField);
 			String description = descField.getText().toString();
			String noNewLineDescription;
			if((description.length() - (description.replaceAll("\\n","").length())) > 6){
				 noNewLineDescription = description.replaceAll("\\n", " ");	
			}
			else
				noNewLineDescription = description;
 			
 			HttpClient client = new DefaultHttpClient();
 		    HttpPost post = new HttpPost("http://foundit.andrewl.ca/postings");
 		    
 
 		    
 		    try {
 			  MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
 			  entity.addPart("posting[name]", new StringBody(name));
 			  entity.addPart("posting[posting_type]", new StringBody("2"));
			  entity.addPart("posting[description]", new StringBody(noNewLineDescription));
 			  if(tookPhoto){
 				  entity.addPart("posting[photo]", new FileBody(f));
 				  post.setEntity(entity);
 				  HttpResponse response = client.execute(post);
 				     if (response.getStatusLine().getStatusCode()  == 200) {
 				    	  uploadSuccessful = true;
 				      }
 			  }
 			  else
 			  {
 				  
 			  }
 
 		    } catch (IOException e) {
 		      e.printStackTrace();
 		    }
 
 		    
 		    return new String[1];
 		}
 	 }
 
 }
