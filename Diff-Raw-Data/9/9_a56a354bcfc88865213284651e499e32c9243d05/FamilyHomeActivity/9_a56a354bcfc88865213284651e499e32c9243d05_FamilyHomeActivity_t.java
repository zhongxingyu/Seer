 package com.feedme.activity;
 
 import android.app.Activity;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Matrix;
 import android.view.*;
 import com.feedme.R;
 import android.content.Intent;
 import android.os.Bundle;
 import android.widget.*;
 import com.feedme.dao.BabyDao;
 import com.feedme.model.Baby;
 
 import java.util.Iterator;
 
 
 /**
  * User: dayel.ostraco
  * Date: 1/16/12
  * Time: 12:29 PM
  */
 public class FamilyHomeActivity extends Activity {
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.family_home);
         handleButtons();
 
         final BabyDao babyDao = new BabyDao(getApplicationContext());
         Baby[] myList = babyDao.getAllBabiesAsStringArray();
 
         TableLayout tl = (TableLayout)findViewById(R.id.myTableLayout);
 
         int j = 0;
         while (j < myList.length) {
             TableRow tr = new TableRow(this);  //create new row
 
             tr.setLayoutParams(new TableRow.LayoutParams(    //set params of row
                     TableRow.LayoutParams.FILL_PARENT,
                     TableRow.LayoutParams.FILL_PARENT));
 
             final Button b = new Button(this);                    //create button for child
             tr.setPadding(5,5,5,5);
 
            if (myList[j].getSex().equals("Male")) {     //change bg color of row and button
                 tr.setBackgroundColor(0xFF7ED0FF);
                 b.setBackgroundColor(0xFF7ED0FF);
             } else {
                 tr.setBackgroundColor(0xFFFF99CC);
                 b.setBackgroundColor(0xFFFF99CC);
             }
 
             b.setText(myList[j].getName());                     //put child's name on button
 
             b.setLayoutParams(new TableRow.LayoutParams(        //set params of button
                     TableRow.LayoutParams.FILL_PARENT,
                     TableRow.LayoutParams.FILL_PARENT));
 
             final ImageView babyImage = new ImageView(this);
 
             if (myList[j].getPicturePath() != null && !myList[j].getPicturePath().equals("")) {
                 BitmapFactory.Options options = new BitmapFactory.Options();
                 options.inSampleSize = 12;
                 Bitmap bmImg = BitmapFactory.decodeFile(myList[j].getPicturePath(), options);
                 babyImage.setImageBitmap(getResizedBitmap(bmImg, 75, 75, 90));
             }
 
             //button listener for each baby
             b.setOnClickListener(new View.OnClickListener() {
                 public void onClick(View v) {
                     Intent intent = new Intent(v.getContext(), ViewBabyActivity.class);
                     intent.putExtra("babyName", b.getText());
                     startActivityForResult(intent, 3);
                 }
             });
 
             /* Add Button to row. */
             tr.addView(b);
 
             /* Add row to TableLayout. */
             tl.addView(tr,new TableLayout.LayoutParams(
                     TableRow.LayoutParams.FILL_PARENT,
                     TableRow.LayoutParams.WRAP_CONTENT));
 
             j++; //iterator
         }
 
     }
 
 
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
 
     public void handleButtons() {
 
         //Add Baby Button
         Button addChild = (Button) findViewById(R.id.addChild);
         addChild.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 Intent intent = new Intent(v.getContext(), AddChildActivity.class);
                 startActivityForResult(intent, 2);
             }
         });
 
         //Add Settings Button
         Button addSettingsButton = (Button) findViewById(R.id.settings);
         addSettingsButton.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 Intent intent = new Intent(v.getContext(), SettingsActivity.class);
                 startActivityForResult(intent, 2);
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
                 break;
             case R.id.home:
                 startActivity(new Intent(FamilyHomeActivity.this,
                         HomeActivity.class));
                 break;
         }
         return true;
     }
 
 }
