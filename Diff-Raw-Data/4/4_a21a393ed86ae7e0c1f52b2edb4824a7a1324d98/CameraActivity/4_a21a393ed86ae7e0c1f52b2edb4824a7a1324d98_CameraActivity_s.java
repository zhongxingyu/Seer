 package com.qc.camera;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URI;
 import java.net.URISyntaxException;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.ContentValues;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.Matrix;
 import android.media.ExifInterface;
import android.net.Uri;
 import android.os.Bundle;
 import android.provider.MediaStore;
 import android.provider.MediaStore.Images.Media;
 import android.util.Log;
 import android.view.View;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.Toast;
 
 import com.qc.R;
 import com.qc.Util;
 
 public class CameraActivity extends Activity {
     // define the file-name to save photo taken by Camera activity
     String FIRST_FILE_NAME = "muzhigirl1.jpg";
     String SECOND_FILE_NAME = "muzhigirl2.jpg";
 
     // create parameters for Intent with filename
     ContentValues values;
     Button buttonFirstPhoto;
     Button buttonSecondPhoto;
     ImageView firstPhoto;
     ImageView secondPhoto;
     private static final int CAPTURE_FIRST_IMAGE_ACTIVITY_REQUEST_CODE = 0;
     private static final int PICK_FIRST_IMAGE_ACTIVITY_REQUESRT_CODE = 1;
     private static final int CAPTURE_SECOND_IMAGE_ACTIVITY_REQUEST_CODE = 2;
     private static final int PICK_SECOND_IMAGE_ACTIVITY_REQUESRT_CODE = 3;
 
     @Override
     protected void onPause() {
         // TODO Auto-generated method stub
         super.onPause();
         Util.clearImageResource(firstPhoto);
         Util.clearImageResource(secondPhoto);
     }
 
     @Override
     protected void onDestroy() {
         // TODO Auto-generated method stub
         super.onDestroy();
         Util.clearImageResource(firstPhoto);
         Util.clearImageResource(secondPhoto);
     }
 
     class ButtonChoosePhotoClicker implements OnClickListener {
         int whichphoto;
 
         public ButtonChoosePhotoClicker(int whichphoto) {
             super();
             this.whichphoto = whichphoto;
         }
 
         @Override
         public void onClick(DialogInterface dialog, int which) {
             // TODO Auto-generated method stub
             Intent photoPickerIntent = new Intent(Intent.ACTION_PICK,
                     android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
             photoPickerIntent.setType("image/*");
             switch (this.whichphoto) {
             case 1:
                 startActivityForResult(photoPickerIntent, PICK_FIRST_IMAGE_ACTIVITY_REQUESRT_CODE);
                 break;
             case 2:
                 startActivityForResult(photoPickerIntent, PICK_SECOND_IMAGE_ACTIVITY_REQUESRT_CODE);
                 break;
             }
 
         }
     }
 
     class ButtonTakePhotoClicker implements OnClickListener {
         int whichphoto;
 
         public ButtonTakePhotoClicker(int whichphoto) {
             super();
             this.whichphoto = whichphoto;
         }
 
         @Override
         public void onClick(DialogInterface arg0, int arg1) {
             ContentValues values = new ContentValues();
             switch (this.whichphoto) {
             case 1:
                 values.put(Media.TITLE, FIRST_FILE_NAME);
                 values.put(Media.DESCRIPTION, "First image captured by the camera, muzhi");
                 break;
             case 2:
                 values.put(Media.TITLE, SECOND_FILE_NAME);
                 values.put(Media.DESCRIPTION, "Second image captured by the camera, muzhi");
             }
             Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
             intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
             switch (this.whichphoto) {
             case 1:
                 startActivityForResult(intent, CAPTURE_FIRST_IMAGE_ACTIVITY_REQUEST_CODE);
                 break;
             case 2:
                 startActivityForResult(intent, CAPTURE_SECOND_IMAGE_ACTIVITY_REQUEST_CODE);
             }
         }
     }
 
     class ButtonPhotoClicker implements Button.OnClickListener {
         int whichphoto;
 
         public ButtonPhotoClicker(int whichphoto) {
             super();
             this.whichphoto = whichphoto;
         }
 
         @Override
         public void onClick(View v) {
             AlertDialog.Builder builder = new AlertDialog.Builder(CameraActivity.this);
             builder.setMessage("选择相册照片或拍照上传").setCancelable(true)
                     .setPositiveButton("拍照上传", new ButtonTakePhotoClicker(this.whichphoto))
                     .setNegativeButton("相册选择", new ButtonChoosePhotoClicker(this.whichphoto));
             AlertDialog alert = builder.create();
             alert.show();
         }
 
     }
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         Toast toast = Toast.makeText(getApplicationContext(), "onCreate", Toast.LENGTH_SHORT);
         toast.show();
         setContentView(R.layout.camera);
 
         firstPhoto = (ImageView) findViewById(R.id.imageView1);
         secondPhoto = (ImageView) findViewById(R.id.imageView2);
         firstPhoto.setClickable(true);
         secondPhoto.setClickable(true);
         firstPhoto.setOnClickListener(new ButtonPhotoClicker(1));
         secondPhoto.setOnClickListener(new ButtonPhotoClicker(2));
     }
 
     private Bitmap getRotatedBitmap() {

     }
 
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         Toast.makeText(this, "on activity result is called", Toast.LENGTH_SHORT).show();
         int w = 512;
         int h = 384;
         switch (requestCode) {
         case CAPTURE_FIRST_IMAGE_ACTIVITY_REQUEST_CODE:
             if (resultCode == RESULT_OK) {
                 Bitmap original_bitmap = null;
                 Bitmap result_bitmap = null;
                 try {
                     original_bitmap = Util.decodeFile(new File(new URI(data.getDataString())), 750);
                     boolean rotated = original_bitmap.getWidth() > original_bitmap.getHeight();
                     if (!rotated) {
                         result_bitmap = result_bitmap.createScaledBitmap(original_bitmap, w, h,
                                 true);
                         // If rotated, scale it by switching width and height
                         // and then rotated it
                     } else {
                         Bitmap scaled_bitmap = Bitmap.createScaledBitmap(original_bitmap, h, w,
                                 true);
                         Matrix mat = new Matrix();
                         mat.postRotate(90);
                         result_bitmap = Bitmap.createBitmap(scaled_bitmap, 0, 0, h, w, mat, true);
                         // Release image resources
                         scaled_bitmap.recycle();
                         scaled_bitmap = null;
                     }
                     original_bitmap.recycle();
                     original_bitmap = null;
                     firstPhoto.setImageBitmap(result_bitmap);
                 } catch (URISyntaxException e) {
                     Toast.makeText(this, "Unable to locate or decode the image you selected.",
                             Toast.LENGTH_SHORT).show();
                 }
             } else if (resultCode == RESULT_CANCELED) {
                 Toast.makeText(this, "Picture was not taken: result_canceled", Toast.LENGTH_SHORT)
                         .show();
             } else {
                 Toast.makeText(this, "Picture was not taken: else", Toast.LENGTH_SHORT).show();
             }
             break;
 
         case CAPTURE_SECOND_IMAGE_ACTIVITY_REQUEST_CODE:
             if ((resultCode == RESULT_OK) && (data.getData() != null)) {
                 Bitmap bitmap;
                 try {
                     bitmap = Util.decodeFile(new File(new URI(data.getDataString())), 750);
                     Log.i("photo size is ", "" + bitmap.getWidth() + " , " + bitmap.getHeight());
                     secondPhoto.setImageBitmap(bitmap);
                 } catch (URISyntaxException e) {
                     Toast.makeText(this, "Unable to locate or decode the image you selected.",
                             Toast.LENGTH_SHORT).show();
                 }
 
             } else if (resultCode == RESULT_CANCELED) {
 
             } else {
 
             }
             break;
 
         case PICK_FIRST_IMAGE_ACTIVITY_REQUESRT_CODE:
             if ((resultCode == RESULT_OK) && (data.getData() != null)) {
                 // resize the image size to 750 px (either width or height,
                 // whichever comes first)
                 Bitmap bitmap;
                 try {
                     bitmap = Util.decodeFile(new File(new URI(data.getDataString())), 750);
                     try {
                         ExifInterface exif = new ExifInterface(FIRST_FILE_NAME);
                         String exifOrientation = exif.getAttribute(ExifInterface.TAG_ORIENTATION);
                         Toast.makeText(this, exifOrientation, Toast.LENGTH_SHORT);
                     } catch (IOException e) {
                         // TODO Auto-generated catch block
                         e.printStackTrace();
                     }
                     Log.i("photo size is ", "" + bitmap.getWidth() + " , " + bitmap.getHeight());
                     firstPhoto.setImageBitmap(bitmap);
                 } catch (URISyntaxException e) {
                     Toast.makeText(this, "Unable to locate or decode the image you selected.",
                             Toast.LENGTH_SHORT).show();
                 }
             } else {
                 Toast.makeText(this, "Image not been selected!", Toast.LENGTH_SHORT).show();
             }
             break;
 
         case PICK_SECOND_IMAGE_ACTIVITY_REQUESRT_CODE:
             if ((resultCode == RESULT_OK) && (data.getData() != null)) {
                 // resize the image size to 750 px (either width or height,
                 // whichever comes first)
                 Bitmap bitmap;
                 try {
                     bitmap = Util.decodeFile(new File(new URI(data.getDataString())), 750);
                     Log.i("photo size is ", "" + bitmap.getWidth() + " , " + bitmap.getHeight());
                     secondPhoto.setImageBitmap(bitmap);
                 } catch (URISyntaxException e) {
                     Toast.makeText(this, "Unable to locate or decode the image you selected.",
                             Toast.LENGTH_SHORT).show();
                 }
             } else {
                 Toast.makeText(this, "Image not been selected!", Toast.LENGTH_SHORT).show();
             }
         }
     }
 }
