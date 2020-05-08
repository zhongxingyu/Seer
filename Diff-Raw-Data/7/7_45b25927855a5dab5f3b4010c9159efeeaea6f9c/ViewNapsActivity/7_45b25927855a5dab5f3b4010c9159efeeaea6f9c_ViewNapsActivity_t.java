 package com.feedme.activity;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.*;
 import com.feedme.R;
 import com.feedme.dao.BabyDao;
 import com.feedme.dao.JournalDao;
 import com.feedme.dao.NapDao;
 import com.feedme.dao.SettingsDao;
 import com.feedme.model.Baby;
 import com.feedme.model.Journal;
 import com.feedme.model.Nap;
 import com.feedme.model.Settings;
 
 import java.util.List;
 
 
 /**
  * User: dayel.ostraco
  * Date: 1/16/12
  * Time: 12:29 PM
  */
 public class ViewNapsActivity extends BaseActivity
 {
     
     public static final int VIEW_NAPS_ACTIVITY_ID = 50;
 
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.naps_home);
 
         Bundle bundle = getIntent().getExtras();
         final int babyId = bundle.getInt("babyId");
         final String babyGender = bundle.getString("babyGender");
         
         styleActivity(babyGender);
 
         handleButtons(babyId, babyGender);
 
         final NapDao napsDao = new NapDao(getApplicationContext());
         List<Nap> lsNap = napsDao.getLastNapsByChild(babyId, 10);
 
         // prepare settings data
         final SettingsDao settingsDao = new SettingsDao(getApplicationContext());
         Settings setting = settingsDao.getSetting(1);
 
 
         TableLayout tl = (TableLayout) findViewById(R.id.myTableLayout);
 
         if (lsNap.size()>0) {
             int j = 0;
             for (Nap nap : lsNap)
             {
                 final int napID = nap.getID();
                 final String napDate = nap.getDate();
                 final String napStartTime = nap.getStartTime();
                 final String napEndTime = nap.getEndTime();
                 final String napLocation = nap.getLocation();
                 final int napChildID = nap.getChild();
 
                 TableRow tr1 = new TableRow(this);  //create new row
 
                 if (j == 0)
                 {
                     if (babyGender.equals("Male"))
                     {
                         tr1.setBackgroundColor(0xFFD2EDFC);
                     }
                     else
                     {
                         tr1.setBackgroundColor(0xFFFCD2d2);
                     }
                 }
                 else
                 {
                     tr1.setBackgroundColor(0xFFFFFFFF);
                 }
                 TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(
                         TableRow.LayoutParams.FILL_PARENT,
                         TableRow.LayoutParams.WRAP_CONTENT);
                 tr1.setLayoutParams(layoutParams);
                 tr1.setClickable(true);
                 tr1.setPadding(5, 5, 5, 5);
 
                 LinearLayout linearLayoutHorizontal = new LinearLayout(this);
 
                 ImageView imageView = new ImageView(this);
                 imageView.setMinimumHeight(60);
                 imageView.setMinimumWidth(60);
                 imageView.setBackgroundResource(R.drawable.icon_border);
                 imageView.setImageResource(R.drawable.icon_nap);
 
                 linearLayoutHorizontal.addView(imageView);
 
                 LinearLayout linearLayoutVertical = new LinearLayout(this);
 
                 linearLayoutVertical.setOrientation(LinearLayout.VERTICAL);
 
                 linearLayoutVertical.setPadding(5,0,0,0);
 
 
                 //print nap location
                 TextView napLocationText = new TextView(this);
                 napLocationText.setTextColor(0xFF000000);
                 final String location = nap.getLocation();
                 napLocationText.setText("Location - " + nap.getLocation());
                 linearLayoutVertical.addView(napLocationText);
 
                 //print nap date
                 TextView napDateText = new TextView(this);
                 napDateText.setTextColor(0xFF000000);
                 napDateText.setText(nap.getDate());
                 linearLayoutVertical.addView(napDateText);
 
                 //print nap time
                 TextView napTime = new TextView(this);
                 napTime.setTextColor(0xFF000000);
                 napTime.setText(nap.getStartTime() + " - " + nap.getEndTime());
                 linearLayoutVertical.addView(napTime);
 
                 linearLayoutHorizontal.addView(linearLayoutVertical);
 
                 tr1.addView(linearLayoutHorizontal);
 
                 tr1.setOnClickListener(new View.OnClickListener()
                 {
                     public void onClick(View v)       {
 
                         Intent intent = new Intent(v.getContext(), EditNapActivity.class);
                         intent.putExtra("napID", napID);
                         intent.putExtra("napDate", napDate);
                         intent.putExtra("napStartTime", napStartTime);
                         intent.putExtra("napEndTime", napEndTime);
                         intent.putExtra("napLocation", napLocation);
                         intent.putExtra("napChildID", napChildID);
                         intent.putExtra("babyGender", babyGender);
                         startActivityForResult(intent, 3);
                     }
                 });
 
 
                 /* Add row to TableLayout. */
                 tl.addView(tr1, new TableLayout.LayoutParams(
                         TableRow.LayoutParams.FILL_PARENT,
                         TableRow.LayoutParams.WRAP_CONTENT));
 
                 j++;
                 if (j == 2)
                 {
                     j = 0;
                 }
             }
 
         }
 
     }
 
     public void handleButtons(final int babyId, final String babyGender)
     {
         Button childButton = (Button) findViewById(R.id.childScreen);
         childButton.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
 
                 final BabyDao babyDao = new BabyDao(getApplicationContext());
 
                 Intent intent = new Intent(v.getContext(), ViewBabyActivity.class);
                 intent.putExtra("babyName", babyDao.getBaby(babyId).getName());
                 intent.putExtra("babyGender", babyGender);
                 startActivityForResult(intent, VIEW_NAPS_ACTIVITY_ID);
             }
         });
 
         Button addNapButton = (Button) findViewById(R.id.addNapButton);
         addNapButton.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
 
                 final BabyDao babyDao = new BabyDao(getApplicationContext());
 
                 Intent intent = new Intent(v.getContext(), AddNapActivity.class);
                 intent.putExtra("babyId", babyDao.getBaby(babyId).getID());
                 intent.putExtra("babyGender", babyGender);
                 startActivityForResult(intent, VIEW_NAPS_ACTIVITY_ID);
             }
         });
 
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
                 startActivity(new Intent(ViewNapsActivity.this,
                         HomeActivity.class));
                break;
             case R.id.settings:
                 startActivity(new Intent(ViewNapsActivity.this,
                         SettingsActivity.class));
                 break;
         }
         return true;
     }
 
 }
