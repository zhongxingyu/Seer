 package com.handicapable.asltutor;
 
 import android.annotation.TargetApi;
 import android.app.Activity;
 import android.content.Intent;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Build;
 import android.os.Bundle;
 import android.provider.MediaStore;
 import android.support.v4.app.NavUtils;
 import android.view.*;
 import android.widget.ImageView;
 import android.widget.Toast;
 
 @TargetApi(11)
 public class AddSignActivity extends Activity {
 
 	public static final int SELECT_IMAGE = 1;
 
 	private ImageView mImageView;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_add_sign);
 		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
 			getActionBar().setDisplayHomeAsUpEnabled(true);
 		}
 
 		mImageView = (ImageView) findViewById(R.id.newImage);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.activity_add_sign, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			NavUtils.navigateUpFromSameTask(this);
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if (resultCode == RESULT_OK && requestCode == SELECT_IMAGE) {
 			Uri resultUri = data.getData();
 			if (resultUri != null) {
 				Cursor cursor = getContentResolver().query(resultUri,
 						new String[] { MediaStore.Images.ImageColumns.DATA }, null, null, null);
 				cursor.moveToFirst();
 
 				String imagePath = cursor.getString(0);
 				mImageView.setImageURI(Uri.parse(imagePath));
 			}
 		}
 	}
 
 	public void addSign(View view) {
 		NavUtils.navigateUpFromSameTask(this);
 		Toast toast = Toast.makeText(getApplicationContext(), "You have added a new sign to your dictionary!",
 				Toast.LENGTH_SHORT);
 		toast.show();
 	}
 
 	public void selectImage(View view) {
 		Intent pickImageIntent = new Intent(Intent.ACTION_GET_CONTENT);
 		pickImageIntent.setType("image/*");
 
 		// Code for taking pictures, which does not work atm
 		// Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
 		//
 		// String pickTitle = "Select or Take a new Picture";
 		// Intent chooseIntent = Intent.createChooser(pickImageIntent, pickTitle);
 		// chooseIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] { takePhotoIntent });
 
 		startActivityForResult(pickImageIntent, SELECT_IMAGE);
 	}
 
 }
