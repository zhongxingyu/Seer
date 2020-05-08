 package com.ttu.picasaalbumnextgeneration;
 
 import java.io.File;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import android.app.Activity;
 import android.content.ContentResolver;
 import android.content.Intent;
import android.database.Cursor;
 import android.graphics.Bitmap;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.provider.MediaStore;
 import android.util.Log;
 import android.view.View;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 // http://stackoverflow.com/questions/2729267/android-camera-intent
 public class CameraActivity extends Activity {
 
 	private static final String JPEG_FILE_PREFIX = "Img_";
 	private static final String JPEG_FILE_SUFFIX = "JPG";
 	private static int TAKE_PICTURE = 1;
 	private Uri imageUri;
 	
 	public void takePhoto() {
 	    Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
 	    // Save photo from intent to this File
 	    File photo = new File(getAlbumDir(),  getImageName());
 	    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photo));
 	    imageUri = Uri.fromFile(photo);
 	    startActivityForResult(intent, TAKE_PICTURE);
 	}
 	
 	private String getImageName() {
 	    // Create an image file name
 	    String timeStamp = 
 	        new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
 	    String imageFileName = JPEG_FILE_PREFIX + timeStamp;
 	
 	    return imageFileName + "." + JPEG_FILE_SUFFIX;
 	}
 	
 	private String getAlbumDir(){
		return Environment.getExternalStorageDirectory() + File.separator + "DCIM/Camera/";		
 	}
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_camera);
 		
 		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) == false)
 		{
 			 Toast.makeText(this, "Storage not mounted", Toast.LENGTH_SHORT).show();
 		}
 		
 		//Creating Button variable
         Button button = (Button) findViewById(R.id.btn_upload);     
       
        //Adding Listener to button
        button.setOnClickListener(new View.OnClickListener() {
           
             @Override
             public void onClick(View v) {
                 // TODO: Upload current photo
             }
         });
          
		takePhoto();
 	}
 	
 	@Override
 	public void onActivityResult(int requestCode, int resultCode, Intent data) {
 	    super.onActivityResult(requestCode, resultCode, data);
 	   
 	    switch (requestCode) {
 	    case 1:
 	        if (resultCode == Activity.RESULT_OK) {
 	            Uri selectedImage = imageUri;
 	            getContentResolver().notifyChange(selectedImage, null);
 	            ImageView imageView = (ImageView) findViewById(R.id.ImageView);
 	            ContentResolver cr = getContentResolver();
 	            Bitmap bitmap;
 	            try {
 	                 bitmap = android.provider.MediaStore.Images.Media
 	                 .getBitmap(cr, selectedImage);
 
 	                imageView.setImageBitmap(bitmap);
 	                Toast.makeText(this, selectedImage.toString(),
 	                        Toast.LENGTH_LONG).show();
 	            } catch (Exception e) {
 	                Toast.makeText(this, "Failed to load", Toast.LENGTH_SHORT)
 	                        .show();
 	                Log.e("Camera", e.toString());
 	            }
 	        }
 	    }
 	}
 }
