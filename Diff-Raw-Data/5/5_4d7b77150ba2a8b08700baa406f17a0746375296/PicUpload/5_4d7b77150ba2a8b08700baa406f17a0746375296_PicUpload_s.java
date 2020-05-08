 package com.placella.socialconnections;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.view.Menu;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 
 public class PicUpload extends Activity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_pic_upload);
 		
 		final EditText name = (EditText) findViewById(R.id.nameET);
 		final EditText reference_id=(EditText) findViewById(R.id.referenceET);
 		Button uploadBtn = (Button) findViewById(R.id.UploadBtn);
 		Button backBtn = (Button) findViewById(R.id.BackBtn);
 		Bundle extras = getIntent().getExtras();
 		final String path = extras.getString("path");
 		final TextView errorTV = (TextView) findViewById(R.id.errorTV);
 		
 		/*
 		 * Onclick listener for back button 
 		 * Go back to lecturers menu
 		 */
 		
 		backBtn.setOnClickListener(new View.OnClickListener() {
			
 			@Override
 			public void onClick(View v) {
				Intent i = new Intent(getBaseContext(), LecturerMenu.class);
	    		startActivity(i);
 			}
 		});
 		
 		/*
 		 * Onclick listener for the upload button
 		 * try and upload a picture
 		 */
 		uploadBtn.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 			if(name.getText().toString().length() < 1 || reference_id.getText().toString().length() < 1)
 			{
 				errorTV.setText("Please, enter information");
 			}
 			else {
 				errorTV.setText("");
 				Upload uploader = new Upload(path,name.getText().toString(),reference_id.getText().toString());
 			}
 				
 			}
 		});
 	}
 	
 	
 	
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_pic_upload, menu);
 		return true;
 	}
 
 }
