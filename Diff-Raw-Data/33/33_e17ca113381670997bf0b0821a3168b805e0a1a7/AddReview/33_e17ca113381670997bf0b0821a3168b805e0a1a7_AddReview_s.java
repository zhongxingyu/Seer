 package com.lyu.inoodles.presentation;
 
 import java.io.ByteArrayOutputStream;
 
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.RatingBar;
 import android.widget.TextView;
 
 import com.lyu.inoodles.R;
 import com.lyu.inoodles.logic.Review;
 
 public class AddReview extends GlobalActivity {
 
     private static final int CAMERA_PIC_REQUEST = 1337;
 
     private ProgressDialog mPd;
     private String mBarcode;
     private byte[] mPicture;
     private float mFlavour;
     private float mSpicy;
     private float mOverall;
     private String mComment;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.add_review);
 
         String barcode = getIntent().getStringExtra("NoodlesBarcode");
         TextView tb = (TextView) findViewById(R.id.textBarcode);
         tb.setText(barcode);
     }
 
     public void onTakePictureClick(View v) {
         if (NO_MONEY_FOR_A_SMARTPHONE) {
             // TODO: take a picture from drawable and put it into mPicture
             /*
              * Drawable d = ImagesArrayList.get(0); mPicture =
              * ((BitmapDrawable)d).getBitmap();
              */
         } else {
            mPd = ProgressDialog.show(this,
                    "Please wait...", "Setting up the camera.", true);
 
            new takePicture().execute();            
         }
     }
    
     private class takePicture extends AsyncTask<Void, Void, Boolean> {
         @Override
         protected Boolean doInBackground(Void... params) {
             Intent cameraIntent = new Intent(
                     android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
             startActivityForResult(cameraIntent, CAMERA_PIC_REQUEST);
             return true;
         }
 
         @Override
         protected void onPostExecute(final Boolean success) {
             takePictureCallback();
         }
     }
 
    public void takePictureCallback()
    {
         mPd.dismiss();
     }
 
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         if (requestCode == CAMERA_PIC_REQUEST) {
            Bitmap bmap = (Bitmap) data.getExtras().get("data");
            ImageView image = (ImageView) findViewById(R.id.photoResultView);
            image.setImageBitmap(bmap);

            ByteArrayOutputStream bao = new ByteArrayOutputStream();
            bmap.compress(Bitmap.CompressFormat.JPEG, 90, bao);
            mPicture = bao.toByteArray();
         }
     }
 
     public void onSendClick(View v) {
         /*
          * barcode flavour spicy overall comment
          */
 
         TextView tBarcode = (TextView) findViewById(R.id.textBarcode);
         mBarcode = tBarcode.getText().toString();
 
         mFlavour = ((RatingBar) findViewById(R.id.ratingFlavour)).getRating();
 
         mSpicy = ((RatingBar) findViewById(R.id.ratingSpicy)).getRating();
 
         mOverall = ((RatingBar) findViewById(R.id.ratingOverall)).getRating();
 
         mComment = ((EditText) findViewById(R.id.editComment)).getText()
                 .toString();
 
         mPd = ProgressDialog.show(this, "Please wait...", "Uploading review.",
                 true);
 
         new sendData().execute();
     }
 
     private class sendData extends AsyncTask<Void, Void, Boolean> {
         @Override
         protected Boolean doInBackground(Void... params) {
             Review.AddReview(mBarcode, mPicture, mFlavour, mSpicy, mOverall,
                     mComment);
             return true;
         }
 
         @Override
         protected void onPostExecute(final Boolean success) {
             sendDataCallback();
         }
     }
 
     public void sendDataCallback() {
         mPd.dismiss();
 
         Intent intentViewReview = new Intent();
         intentViewReview.setClass(this, Main.class);
         intentViewReview.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
         this.startActivity(intentViewReview);
     }
 
 }
