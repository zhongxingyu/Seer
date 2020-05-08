 package com.feedme.activity;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Matrix;
 import android.net.Uri;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 import com.feedme.R;
 import com.feedme.dao.BabyDao;
 import com.feedme.model.Baby;
 
 import java.io.File;
 
 /**
  * User: dayel.ostraco
  * Date: 1/16/12
  * Time: 4:34 PM
  */
 public class ViewBabyActivity extends Activity {
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.view_baby);
 
         final BabyDao babyDao = new BabyDao(getApplicationContext());
 
         String babyName = "";
         if (getIntent().getExtras() != null && getIntent().getExtras().getString("babyName") != null) {
             babyName = getIntent().getExtras().getString("babyName");
         }
 
         Baby baby = null;
         if (!babyName.equals("")) {
             baby = babyDao.getBabyByName(babyName);
         }
 
         final int babyId = baby.getID();
 
         // Populate Baby Data
         if (baby != null)
         {
             final TextView tBabyName = (TextView) findViewById(R.id.babyName);
             tBabyName.setText(baby.getName());
 
             final TextView babySex = (TextView) findViewById(R.id.babySex);
             babySex.setText(baby.getSex());
 
             final RelativeLayout topBanner = (RelativeLayout) findViewById(R.id.topBanner);
             final RelativeLayout bottomBanner = (RelativeLayout) findViewById(R.id.bottomBanner);
             
             final Button familyButton = (Button) findViewById(R.id.familyButton);
            if ((baby.getSex()).equals("male")) {
                 topBanner.setBackgroundColor(0xFF7ED0FF);
                 bottomBanner.setBackgroundColor(0xFF7ED0FF);
             } else {
                 topBanner.setBackgroundColor(0xFFFF99CC);
                 bottomBanner.setBackgroundColor(0xFFFF99CC);
             }
 
             final TextView babyHeight = (TextView) findViewById(R.id.babyHeight);
             babyHeight.setText(baby.getHeight());
 
             final TextView babyWeight = (TextView) findViewById(R.id.babyWeight);
             babyWeight.setText(baby.getWeight());
 
             final TextView babyDob = (TextView) findViewById(R.id.babyDob);
             babyDob.setText(baby.getDob());
 
             final ImageView babyImage = (ImageView) findViewById(R.id.babyPicture);
             if (baby.getPicturePath() != null && !baby.getPicturePath().equals("")) {
                 BitmapFactory.Options options = new BitmapFactory.Options();
                 options.inSampleSize = 12;
                 Bitmap bmImg = BitmapFactory.decodeFile(baby.getPicturePath(), options);
                 babyImage.setImageBitmap(getResizedBitmap(bmImg, 75, 75, 90));
             }
         }
 
         //Add Family Button`
         Button familyButton = (Button) findViewById(R.id.familyButton);
         familyButton.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 startActivity(new Intent(ViewBabyActivity.this,
                         FamilyHomeActivity.class));
             }
         });
 
         //Add Bottle Feeding Button`
         Button bottleButton = (Button) findViewById(R.id.bottleButton);
         bottleButton.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v)
             {
                 Intent intent = new Intent(ViewBabyActivity.this, AddBottleFeedActivity.class);
                 Bundle b = new Bundle();
                 b.putString("babyId", String.valueOf(babyId));
                 intent.putExtras(b);
                 startActivity(intent);
             }
         });
 
         //Add Breast Feeding Button`
         Button breastfeedButton = (Button) findViewById(R.id.breastfeedButton);
         breastfeedButton.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v)
             {
                 Intent intent = new Intent(ViewBabyActivity.this, AddBreastFeedActivity.class);
                 Bundle b = new Bundle();
                 b.putInt("babyId", babyId);
                 intent.putExtras(b);
                 startActivity(intent);
             }
         });
 
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.menu, menu);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
 
         switch (item.getItemId()) {
             case R.id.family:
                 startActivity(new Intent(ViewBabyActivity.this,
                         FamilyHomeActivity.class));
                 break;
             case R.id.home:
                 startActivity(new Intent(ViewBabyActivity.this,
                         HomeActivity.class));
                 break;
         }
         return true;
     }
 
     /**
      * Resizes a Bitmap based on the passed in newHeight and newWidth and rotates the image by rotateInDegrees.
      *
      * @param bitMap
      * @param newHeight
      * @param newWidth
      * @param rotateInDegrees
      * @return
      */
     private Bitmap getResizedBitmap(Bitmap bitMap, int newHeight, int newWidth, int rotateInDegrees) {
 
         int width = bitMap.getWidth();
         int height = bitMap.getHeight();
         float scaleWidth = ((float) newWidth) / width;
         float scaleHeight = ((float) newHeight) / height;
 
         // create a matrix for the manipulation
         Matrix matrix = new Matrix();
 
         // resize the bit map
         matrix.postScale(scaleWidth, scaleHeight);
         matrix.postRotate(rotateInDegrees);
 
         // recreate the new Bitmap
         Bitmap resizedBitmap = Bitmap.createBitmap(bitMap, 0, 0, width, height, matrix, false);
 
         return resizedBitmap;
     }
 }
