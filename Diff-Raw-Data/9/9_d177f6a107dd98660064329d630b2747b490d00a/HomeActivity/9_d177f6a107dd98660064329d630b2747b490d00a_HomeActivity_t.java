 package com.feedme.activity;
 
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.*;
 import com.feedme.R;
 import com.feedme.dao.BabyDao;
 import com.feedme.dao.SettingsDao;
 import com.feedme.model.Baby;
 import com.feedme.model.Settings;
 
 public class HomeActivity extends BaseActivity
 {
 
     private static String TAG = "Feed-Me";
 
     /**
      * Called when the activity is first created.
      *
      * @param savedInstanceState If the activity is being re-initialized after
      *                           previously being shut down then this Bundle contains the data it most
      *                           recently supplied in onSaveInstanceState(Bundle). <b>Note: Otherwise it is null.</b>
      */
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         Log.i(TAG, "onCreate");
         setContentView(R.layout.main);
 
         googleAnalyticsTracker.startNewSession(TRACKING_ID, this);
         googleAnalyticsTracker.trackPageView("/Home");
 
         showBabies();
         initializeSettings();
 
         //Add Settings Button
         Button addSettingsButton = (Button) findViewById(R.id.settings);
         addSettingsButton.setOnClickListener(new View.OnClickListener()
         {
             public void onClick(View v)
             {
                 Intent intent = new Intent(v.getContext(), SettingsActivity.class);
                 startActivityForResult(intent, 2);
             }
         });
 
         // button listener for add child screen button
         Button addChildScreen = (Button) findViewById(R.id.addChildScreen);
         addChildScreen.setOnClickListener(new View.OnClickListener()
         {
             public void onClick(View v)
             {
                 startActivity(new Intent(HomeActivity.this,
                         AddChildActivity.class));
             }
         });
 
     }
 
 
     public void initializeSettings()
     {
         final SettingsDao settingsDao = new SettingsDao(getApplicationContext());
         settingsDao.addSettings(new Settings("oz", "in", "lbs", "F", "off", "off"));
     }
 
 
     public void showBabies()
     {
         final BabyDao babyDao = new BabyDao(getApplicationContext());
         final Bundle bundle = new Bundle();
         
         Baby[] myList = babyDao.getAllBabiesAsStringArray();
 
         LinearLayout ll = (LinearLayout) findViewById(R.id.babyScroll);
 
         int j = 0;
         while (j < myList.length) {
             final Baby baby = myList[j];

             TableLayout tl = new TableLayout(this);
             TableRow tr = new TableRow(this);  //create new row
             TableRow tr2 = new TableRow(this);  //create new row
             TableLayout.LayoutParams tableRowParams = new TableLayout.LayoutParams(
                     TableRow.LayoutParams.WRAP_CONTENT,
                     TableRow.LayoutParams.WRAP_CONTENT);
             tableRowParams.setMargins(5, 5, 5, 5);
             tl.setLayoutParams(tableRowParams);
             tr.setLayoutParams(new TableRow.LayoutParams(    //set params of row
                     TableRow.LayoutParams.WRAP_CONTENT,
                     TableRow.LayoutParams.WRAP_CONTENT));
             tr2.setLayoutParams(new TableRow.LayoutParams(    //set params of row
                     TableRow.LayoutParams.WRAP_CONTENT,
                     TableRow.LayoutParams.WRAP_CONTENT));
 
             final Button b = new Button(this);                    //create button for child
 
             if (baby.getSex().equals("Male")) {     //change bg color of row and button
                 b.setBackgroundColor(0xFF7ED0FF);
             } else {
                 b.setBackgroundColor(0xFFFF99CC);
             }
 
             b.setText(baby.getName());                     //put child's name on button
             b.setTextColor(0xFFFFFFFF);
             b.setLayoutParams(new TableRow.LayoutParams(        //set params of button
                     TableRow.LayoutParams.WRAP_CONTENT,
                     TableRow.LayoutParams.WRAP_CONTENT));
             b.setMinWidth(150);
             b.setMaxWidth(300);
             b.setMinHeight(35);
             b.setMaxHeight(65);
             b.setTextSize(17);
 
 
             final ImageView babyImage = new ImageView(this);
 
             if (baby.getPicturePath() != null && !baby.getPicturePath().equals("")) {
                 BitmapFactory.Options options = new BitmapFactory.Options();
                 options.inSampleSize = 12;
                 Bitmap bmImg = BitmapFactory.decodeFile(baby.getPicturePath(), options);
                 babyImage.setImageBitmap(getResizedBitmap(bmImg, 75, 75, 90));
                 babyImage.setMaxWidth(300);
                 babyImage.setMaxHeight(300);
                 babyImage.setMinimumWidth(150);
                 babyImage.setMinimumHeight(150);
             } else {
                 babyImage.setImageResource(R.drawable.babyicon);
                 babyImage.setMaxWidth(300);
                 babyImage.setMaxHeight(300);
                 babyImage.setMinimumWidth(150);
                 babyImage.setMinimumHeight(150);
             }
 
             //button listener for each baby
             b.setOnClickListener(new View.OnClickListener()
             {
                 public void onClick(View v)
                 {
                     Intent intent = new Intent(v.getContext(), ViewBabyActivity.class);
                    bundle.putSerializable("baby", baby);
                     intent.putExtras(bundle);
                     startActivityForResult(intent, 3);
                 }
             });
             babyImage.setOnClickListener(new View.OnClickListener()
             {
                 public void onClick(View v)
                 {
                     Intent intent = new Intent(v.getContext(), ViewBabyActivity.class);
                    bundle.putSerializable("baby", baby);
                     intent.putExtras(bundle);
                     startActivityForResult(intent, 3);
                 }
             });
 
             /* Add table to linearlayout */
             ll.addView(tl);
 
             /* Add rows to table */
             tr.addView(b);
             tr2.addView(babyImage);
 
             /* Add row to TableLayout. */
             tl.addView(tr, new TableLayout.LayoutParams(
                     TableRow.LayoutParams.FILL_PARENT,
                     TableRow.LayoutParams.WRAP_CONTENT));
             tl.addView(tr2, new TableLayout.LayoutParams(
                     TableRow.LayoutParams.FILL_PARENT,
                     TableRow.LayoutParams.WRAP_CONTENT));
 
             j++; //iterator
         }
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu)
     {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.menu, menu);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item)
     {
 
         switch (item.getItemId()) {
             case R.id.home:
                 startActivity(new Intent(HomeActivity.this,
                         HomeActivity.class));
                 break;
             case R.id.settings:
                 startActivity(new Intent(HomeActivity.this,
                         SettingsActivity.class));
                 break;
             case R.id.report:
                 startActivity(new Intent(HomeActivity.this,
                         ReportBugActivity.class));
                 break;
         }
         return true;
     }
 }
