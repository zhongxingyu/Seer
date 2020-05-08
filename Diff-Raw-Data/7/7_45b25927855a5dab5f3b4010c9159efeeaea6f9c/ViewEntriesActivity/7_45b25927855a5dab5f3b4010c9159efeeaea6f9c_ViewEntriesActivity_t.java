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
 import com.feedme.dao.SettingsDao;
 import com.feedme.model.Baby;
 import com.feedme.model.Journal;
 import com.feedme.model.Settings;
 
 import java.util.List;
 
 
 /**
  * User: dayel.ostraco
  * Date: 1/16/12
  * Time: 12:29 PM
  */
 public class ViewEntriesActivity extends BaseActivity
 {
     
     public static final int VIEW_ENTRIES_ACTIVITY_ID = 50;
 
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.entries_home);
 
         Bundle bundle = getIntent().getExtras();
         final int babyId = bundle.getInt("babyId");
         final String babyGender = bundle.getString("babyGender");
 
         styleActivity(babyGender);
         
         handleButtons(babyId);
 
         final JournalDao journalDao = new JournalDao(getApplicationContext());
         List<Journal> lsJournal = journalDao.getLastFeedingsByChild(babyId, 10);
 
         // prepare settings data
         final SettingsDao settingsDao = new SettingsDao(getApplicationContext());
         Settings setting = settingsDao.getSetting(1);
 
         Log.d("BABYID ENTRIES:", String.valueOf(babyId));
         
         TableLayout tl = (TableLayout) findViewById(R.id.myTableLayout);
 
         if (lsJournal.size()>0) {
             int j = 0;
             for (Journal journal : lsJournal)
             {
                 final int entryID = journal.getID();
                 final String entryDate = journal.getDate();
                 final String entryStartTime = journal.getStartTime();
                 final String entryEndTime = journal.getEndTime();
                 final String entryOunces = journal.getOunces();
                 final String entrySide = journal.getSide();
                 final int entryChildID = journal.getChild();
 
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
 
                 if (journal.getSide().trim().isEmpty())
                 {
                     imageView.setImageResource(R.drawable.icon_bottle);
                 }
                 else
                 {
                     imageView.setImageResource(R.drawable.icon_breastfeed);
                 }
 
                 linearLayoutHorizontal.addView(imageView);
 
                 LinearLayout linearLayoutVertical = new LinearLayout(this);
 
                 linearLayoutVertical.setOrientation(LinearLayout.VERTICAL);
 
                 linearLayoutVertical.setPadding(5,0,0,0);
 
                 TextView bottleBreast = new TextView(this);
                 bottleBreast.setTextColor(0xFF000000);
                 final String side = journal.getSide();
                 if (side.trim().isEmpty())
                 {
                     bottleBreast.setText("Bottle");
                 }
                 else
                 {
                     bottleBreast.setText("Breastfeeding - " + journal.getSide());
                 }
 
                 linearLayoutVertical.addView(bottleBreast);
 
     //            TextView entryId = new TextView(this);
     //            entryId.setTextColor(0xFF000000);
     //            entryId.setText("Entry ID: " + journal.getID());
     //
     //            linearLayoutVertical.addView(entryId);
 
                 TextView feedAmount = new TextView(this);
                 feedAmount.setTextColor(0xFF000000);
                 feedAmount.setText(journal.getDate());
 
                 linearLayoutVertical.addView(feedAmount);
 
                 TextView babyWake = new TextView(this);
                 babyWake.setTextColor(0xFF000000);
                 //if not bottle feed, don't display "oz"
                 if (side.trim().isEmpty()) {
                     babyWake.setText(journal.getOunces() + " " + setting.getLiquid());
                 }
                 else {
                     babyWake.setText(journal.getOunces());
                 }
 
                 linearLayoutVertical.addView(babyWake);
 
                 TextView babyDiaper = new TextView(this);
                 babyDiaper.setTextColor(0xFF000000);
                 babyDiaper.setText(journal.getStartTime() + " - " + journal.getEndTime());
 
                 linearLayoutVertical.addView(babyDiaper);
 
                 linearLayoutHorizontal.addView(linearLayoutVertical);
 
                 tr1.addView(linearLayoutHorizontal);
 
                 final BabyDao babyDao = new BabyDao(getApplicationContext());
                 final Baby baby = babyDao.getBaby(journal.getChild());
 
                 tr1.setOnClickListener(new View.OnClickListener()
                 {
                     public void onClick(View v)       {
 
                         if (side.trim().isEmpty())
                         {
                             Intent intent = new Intent(v.getContext(), EditBottleFeedActivity.class);
                             intent.putExtra("entryID", entryID);
                             intent.putExtra("entryDate", entryDate);
                             intent.putExtra("entryStartTime", entryStartTime);
                             intent.putExtra("entryEndTime", entryEndTime);
                             intent.putExtra("entrySide", entrySide);
                             intent.putExtra("entryOunces", entryOunces);
                             intent.putExtra("entryChild", entryChildID);
                             intent.putExtra("babyGender", baby.getSex());
                             startActivityForResult(intent, 3);
                         }
                         else
                         {
                             Intent intent = new Intent(v.getContext(), EditBreastFeedActivity.class);
                             intent.putExtra("entryID", entryID);
                             intent.putExtra("entryDate", entryDate);
                             intent.putExtra("entryStartTime", entryStartTime);
                             intent.putExtra("entryEndTime", entryEndTime);
                             intent.putExtra("entrySide", entrySide);
                             intent.putExtra("entryOunces", entryOunces);
                             intent.putExtra("entryChild", entryChildID);
                             intent.putExtra("babyGender", baby.getSex());
                             startActivityForResult(intent, 3);
                         }
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
 
     public void handleButtons(final int babyId)
     {
         Button childButton = (Button) findViewById(R.id.childScreen);
         childButton.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
 
                 final BabyDao babyDao = new BabyDao(getApplicationContext());
 
                 Intent intent = new Intent(v.getContext(), ViewBabyActivity.class);
                 intent.putExtra("babyName", babyDao.getBaby(babyId).getName());
                 startActivityForResult(intent, VIEW_ENTRIES_ACTIVITY_ID);
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
                 startActivity(new Intent(ViewEntriesActivity.this,
                         HomeActivity.class));
                break;
             case R.id.settings:
                 startActivity(new Intent(ViewEntriesActivity.this,
                         SettingsActivity.class));
                 break;
         }
         return true;
     }
 
 }
