 package com.github.groupENIGMA.journalEgocentrique;
 
 import java.io.File;
 import java.io.FileOutputStream;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.provider.MediaStore;
 import android.view.View;
 import android.widget.ImageView;
 
 import com.github.groupENIGMA.journalEgocentrique.model.DB;
 import com.github.groupENIGMA.journalEgocentrique.model.Day;
 import com.github.groupENIGMA.journalEgocentrique.model.Photo;
 
 public class PhotoActivity extends Activity {
 
 	private static final int CAMERA_REQUEST = 1; 
 	private DB data;
 	private ImageView actualImg;
 	private Bitmap mImageBitmap;
 	private Day day;
 	private File tmpImg;
 	private String tempPath = Environment.getExternalStorageDirectory().getAbsolutePath() +
 			File.separator + AppConstants.EXTERNAL_STORAGE_PHOTO_DIR + "~temp.jpg";
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_photo);
 		actualImg = (ImageView)findViewById(R.id.photo);
 		data = new DB(getApplicationContext());
 		Intent received = getIntent();
 		data.open();
 		final long dayId = received.getLongExtra(
                 MainActivity.EXTRA_PHOTO_ACTIVITY_DayId, 0
         );
 		day = data.getDay(dayId);
 		Photo tmp = day.getPhoto();
 		tmpImg = new File(tempPath);
 		if(tmpImg.exists())
 			actualImg.setImageURI(Uri.parse(tempPath));
 		else if(tmp != null)
 			actualImg.setImageURI(Uri.parse(tmp.getPath()));
 	}
 	
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		handleCameraPhoto(data);
 	}
 	
 	/**
 	 * Takes and displays the camera's photo.
 	 * It also save in a temporary file this photo. 
 	 * @param intent Intent received from the Camera Activity
 	 */
 	private void handleCameraPhoto(Intent intent) {
 	    Bundle extras = intent.getExtras();
 	    mImageBitmap = (Bitmap) extras.get("data");
 	    actualImg.setImageBitmap(mImageBitmap);
 	    
 	       //Gets the path and the directory name where the Photo is going to be saved
         String path = Environment.getExternalStorageDirectory().getAbsolutePath();
         File photoDir = new File(
                 path + File.separator + AppConstants.EXTERNAL_STORAGE_PHOTO_DIR
         );
         //Creates the file
         File file = new File (tempPath);
         //Delete if already exists
         if (file.exists ()) {
             file.delete ();
         }
         if (! photoDir.exists()){
             photoDir.mkdirs();
         }
         //Writes the file with the picture in the selected path
         try {
             file.createNewFile();
             FileOutputStream out = new FileOutputStream(file);
             mImageBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
             out.flush();
             out.close();
 
         } catch (Exception e) {
                e.printStackTrace();
         }
 	}
 	
 	/**
 	 *  Remove the daily photo and sets the default avatar
 	 *  The button must be enabled ONLY IF the actual image isn't the default avatar
 	 */
 	public void removeImage(View view){
		DB data = new DB(getApplicationContext());
		data.open();
 		data.removePhoto(day);
 		actualImg.setImageResource(R.drawable.ic_launcher);
 	}
 	
 	/**
 	 * Accept the displayed photo as the new day-photo.
 	 * @param view
 	 */
 	public void accept(View view){
 		data.setDayPhoto(day, mImageBitmap);
 		Intent intent = new Intent(getApplicationContext(), MainActivity.class);
		data.close();
 		if(tmpImg.exists())
 			tmpImg.delete();
 		startActivity(intent);
 	}
 	
 	/**
 	 * Launch an Intent to the default Camera application
 	 * to take another picture
 	 */
 	public void takePicture(View view){
 		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
 		startActivityForResult(intent, CAMERA_REQUEST);
 	}
 	
 	/**
 	 * Close the connection at the database
 	 */
 	protected void onPause(){
 		super.onPause();
 		data.close();
 	}
 
 }
