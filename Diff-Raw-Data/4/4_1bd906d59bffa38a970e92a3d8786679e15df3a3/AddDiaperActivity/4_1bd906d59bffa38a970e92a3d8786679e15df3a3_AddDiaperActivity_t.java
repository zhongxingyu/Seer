 package com.feedme.activity;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.Button;
 import android.widget.Spinner;
 import com.feedme.R;
 import com.feedme.dao.DiaperDao;
 import com.feedme.model.Baby;
 import com.feedme.model.Diaper;
 import com.feedme.service.FeedMeLocationService;
 
 import java.text.ParseException;
 import java.util.Calendar;
 
 /**
  * User: dayelostraco
  * Date: 2/4/12
  * Time: 1:34 PM
  */
 public class AddDiaperActivity extends DiaperActivity {
 
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.add_diaper);
 
         googleAnalyticsTracker.startNewSession(TRACKING_ID, this);
         googleAnalyticsTracker.trackPageView("/Add-Diaper");
 
         final FeedMeLocationService feedMeLocationService = FeedMeLocationService.getInstance(getApplicationContext(), null);
         final DiaperDao diaperDao = new DiaperDao(getApplicationContext());
 
         //Get/Set Baby
         final Baby baby = (Baby) getIntent().getSerializableExtra("baby");
         final Bundle bundle = new Bundle();
         bundle.putSerializable("baby", baby);
 
         //Set Layout
         styleActivity(baby.getSex());
 
         //Set Date and Time Buttons
         entryDate = (Button) findViewById(R.id.diaperDate);
         startTime = (Button) findViewById(R.id.diaperTime);
         entryDate.setOnClickListener(showDateDialog());
         startTime.setOnClickListener(showStartTimeDialog());
 
         //Get the Current Time and set the Date and Time Buttons
         final Calendar currentTime = Calendar.getInstance();
         mYear = currentTime.get(Calendar.YEAR);
         mMonth = currentTime.get(Calendar.MONTH);
         mDay = currentTime.get(Calendar.DAY_OF_MONTH);
         startHour = currentTime.get(Calendar.HOUR_OF_DAY);
         startMinute = currentTime.get(Calendar.MINUTE);
         startSecond = currentTime.get(Calendar.SECOND);
 
         updateDateDisplay();
         updateStartDisplay();
         
         //Spinners
         final Spinner diaperType = (Spinner) findViewById(R.id.addDiaperType);
         final Spinner diaperConsistency = (Spinner) findViewById(R.id.addDiaperConsistency);
         final Spinner diaperColor = (Spinner) findViewById(R.id.addDiaperColor);
 
         //Save Diaper Button
         Button addDiaperButton = (Button) findViewById(R.id.addDiaperButton);
         addDiaperButton.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
 
                 Diaper diaper = new Diaper();
                 diaper.setChildId(baby.getId());
                 diaper.setDate(entryDate.getText().toString());
                 diaper.setStartTime(startTime.getText().toString());
                 diaper.setType(diaperType.getSelectedItem().toString());
                 diaper.setConsistency(diaperConsistency.getSelectedItem().toString());
                 diaper.setColor(diaperColor.getSelectedItem().toString());
                diaper.setLatitude(Double.toString(feedMeLocationService.getLatitude()));
                diaper.setLongitude(Double.toString(feedMeLocationService.getLongitude()));
 
                 try {
                     diaperDao.addDiaper(diaper);
                 } catch (ParseException e){
                     Log.d("AddDiaperActivity", "Could not parse Date and StartTime into a ISO8601 format");
                 }
 
                 Intent intent = new Intent(v.getContext(), ViewBabyActivity.class);
                 intent.putExtras(bundle);
                 startActivityForResult(intent, ADD_DIAPER_ACTIVITY_ID);
             }
         });
     }
 }
