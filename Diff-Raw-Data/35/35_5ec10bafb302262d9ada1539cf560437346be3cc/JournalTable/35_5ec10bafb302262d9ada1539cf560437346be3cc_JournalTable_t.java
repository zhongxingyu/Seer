 package com.feedme.ui;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.*;
 import com.feedme.R;
 import com.feedme.activity.EditBottleFeedActivity;
 import com.feedme.activity.EditBreastFeedActivity;
 import com.feedme.activity.EditDiaperActivity;
 import com.feedme.activity.EditNapActivity;
 import com.feedme.dao.SettingsDao;
 import com.feedme.model.*;
 import com.feedme.util.DateUtil;
import com.feedme.util.StringUtils;
 
 import java.util.List;
 
 /**
  * User: steve quick
  * Date: 1/28/12
  * Time: 1:19 PM
  */
 public class JournalTable {
 
     public void buildRows(final Activity activity, List<BaseObject> baseObjects, Baby baby, TableLayout tableLayout) {
         SettingsDao settingsDao = new SettingsDao(activity.getApplicationContext());
 
         Settings setting = settingsDao.getSetting(1);
         int j = 0;
         for (BaseObject baseObject : baseObjects) {
 
             TableRow tr1 = new TableRow(activity.getApplicationContext());  //create new row
 
             if (j == 0) {
                 if (baby.getSex().equals("Male")) {
                     tr1.setBackgroundColor(0xFFD2EDFC);
                 } else {
                     tr1.setBackgroundColor(0xFFFCD2d2);
                 }
             } else {
                 tr1.setBackgroundColor(0xFFFFFFFF);
             }
             TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(
                     TableRow.LayoutParams.FILL_PARENT,
                     TableRow.LayoutParams.WRAP_CONTENT);
             tr1.setLayoutParams(layoutParams);
             tr1.setClickable(true);
             tr1.setPadding(5, 5, 5, 5);
 
             LinearLayout linearLayoutHorizontal = new LinearLayout(activity.getApplicationContext());
 
             /**
              * Image Icon
              */
             ImageView imageView = new ImageView(activity.getApplicationContext());
             imageView.setMinimumHeight(80);
             imageView.setMinimumWidth(80);
             imageView.setBackgroundResource(R.drawable.icon_border);
 
             LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                     LinearLayout.LayoutParams.WRAP_CONTENT,
                     LinearLayout.LayoutParams.WRAP_CONTENT);
             lp.setMargins(10, 10, 10, 10);
             imageView.setLayoutParams(lp);
 
             if(baseObject instanceof Journal){
                 
                 Journal journal = (Journal) baseObject;
                if (StringUtils.isEmpty(journal.getSide().trim())) {
                     imageView.setImageResource(R.drawable.icon_bottle);
                 } else {
                     imageView.setImageResource(R.drawable.icon_breastfeed);
                 }
                 
             } else if(baseObject instanceof Diaper){                
                 imageView.setImageResource(R.drawable.icon_diaper);
                 
             } else if(baseObject instanceof Nap){
                 imageView.setImageResource(R.drawable.icon_nap);
             }
 
             linearLayoutHorizontal.addView(imageView);
             LinearLayout linearLayoutVertical = new LinearLayout(activity.getApplicationContext());
             linearLayoutVertical.setOrientation(LinearLayout.VERTICAL);
             linearLayoutVertical.setPadding(5, 5, 5, 5);
 
             /**
              * Entry Type
              */
             TextView entryType = new TextView(activity.getApplicationContext());
             entryType.setTextColor(0xFF000000);
 
             if(baseObject instanceof Journal){
                 
                 Journal journal = (Journal) baseObject;
                 
                 final String side = journal.getSide();
     
                if (StringUtils.isEmpty(side.trim())) {
                     entryType.setText("Bottle");
                 } else {
                     entryType.setText("Breastfeeding - " + journal.getSide());
                 }
 
             } else if(baseObject instanceof Diaper){
                 entryType.setText("Diaper");
                 
             } else if(baseObject instanceof Nap){
                 Nap nap = (Nap) baseObject;
                 
                 StringBuilder stringBuilder = new StringBuilder();
                 stringBuilder.append("Nap");
                 
                 if(nap.getLocation()!=null && !nap.getLocation().equals("")){
                     stringBuilder.append(" - ");
                     stringBuilder.append(nap.getLocation());
                 }
 
                 entryType.setText(stringBuilder.toString());
             }
 
             //Sets entryType to the linearLayoutVertical
             linearLayoutVertical.addView(entryType);
 
             /**
              * Entry Date
              */
             TextView entryDate = new TextView(activity.getApplicationContext());
             entryDate.setTextColor(0xFF000000);
             entryDate.setText(baseObject.getDate());
             linearLayoutVertical.addView(entryDate);
 
             /**
              * Metrics
              */
             TextView metrics = new TextView(activity.getApplicationContext());
             DateUtil dateUtil = new DateUtil();
             metrics.setTextColor(0xFF000000);
             StringBuilder metricsBuffer = new StringBuilder();
 
             if(baseObject instanceof Journal){
                 
                 Journal journal = (Journal) baseObject;
                 boolean isBottleFed = false;
                if (!StringUtils.isEmpty(journal.getOunces())) {
                     isBottleFed = true;
                    if (StringUtils.isEmpty(journal.getSide().trim())) {
                         metricsBuffer.append(journal.getOunces() + " " + setting.getLiquid());
                     } else {
                         metricsBuffer.append(journal.getOunces());
                     }
                 }
 
                 if (journal.getFeedTime()!=null && !journal.getFeedTime().equals("0") && !journal.getFeedTime().trim().equals("")) {
                     String duration = dateUtil.convertDateLongToTimeString(Long.parseLong(journal.getFeedTime()));
                     metricsBuffer.append(dateUtil.getDurationAsStringMsg(duration));
 
                 } else {
                     if(isBottleFed){
                         metricsBuffer.append(" - ");
                     }
                     metricsBuffer.append(dateUtil.getDurationAsStringMsg(journal.getStartTime(), journal.getEndTime()));
                 }
 
             } else if(baseObject instanceof Diaper){
                 Diaper diaper = (Diaper) baseObject;
                 metricsBuffer.append(diaper.getType());
 
             } else if(baseObject instanceof Nap){
                 Nap nap = (Nap) baseObject;
                 metricsBuffer.append(nap.getLocation());
                 metricsBuffer.append(" - ");
                 metricsBuffer.append(dateUtil.getDurationAsStringMsg(nap.getStartTime(), nap.getEndTime()));
             }
             
             metrics.setText(metricsBuffer.toString());
 
             //Add Metrics text to linearLayoutVertical
             linearLayoutVertical.addView(metrics);
 
             /**
              * Entry Times
              */
             TextView entryTimes = new TextView(activity.getApplicationContext());
             entryTimes.setTextColor(0xFF000000);
             
             if(baseObject instanceof Journal){
                 Journal journal = (Journal) baseObject;
                 entryTimes.setText(baseObject.getStartTime() + " - " + journal.getEndTime());
 
             } else if(baseObject instanceof Diaper){
                 entryTimes.setText(baseObject.getStartTime());
 
             } else if(baseObject instanceof Nap){
                 Nap nap = (Nap) baseObject;
                 entryTimes.setText(baseObject.getStartTime() + " - " + nap.getEndTime());
             }
 
             //Set entryTimes to linearLayoutVertical
             linearLayoutVertical.addView(entryTimes);
 
             //Set linearLayoutVertical to linearLayoutHorizontal
             linearLayoutHorizontal.addView(linearLayoutVertical);
 
             //Add linearLayoutHorizontal to the TableRow
             tr1.addView(linearLayoutHorizontal);
 
             final Bundle jBundle = new Bundle();
             jBundle.putSerializable("baby", baby);
 
             /**
              * Entry Intent
              */
             if(baseObject instanceof Journal){
                 jBundle.putSerializable("journal", baseObject);
                 final Journal journal = (Journal) baseObject;
                 
                 tr1.setOnClickListener(new View.OnClickListener() {
                     public void onClick(View v) {
                         Intent intent;
                        if (StringUtils.isEmpty(journal.getSide().trim())) {
                             intent = new Intent(v.getContext(), EditBottleFeedActivity.class);
                         } else {
                             intent = new Intent(v.getContext(), EditBreastFeedActivity.class);
                         }
                         intent.putExtras(jBundle);
                         activity.startActivityForResult(intent, 3);
                     }
                 });
                 
             } else if(baseObject instanceof Diaper){
                 jBundle.putSerializable("diaper", baseObject);
 
                 tr1.setOnClickListener(new View.OnClickListener() {
                     public void onClick(View v) {
 
                         Intent intent = new Intent(v.getContext(), EditDiaperActivity.class);
                         intent.putExtras(jBundle);
                         activity.startActivityForResult(intent, 3);
                     }
                 });
 
             } else if(baseObject instanceof Nap){
                 jBundle.putSerializable("nap", baseObject);
 
                 tr1.setOnClickListener(new View.OnClickListener() {
                     public void onClick(View v) {
 
                         Intent intent = new Intent(v.getContext(), EditNapActivity.class);
                         intent.putExtras(jBundle);
                         activity.startActivityForResult(intent, 3);
                     }
                 });
             }
 
             /* Add row to TableLayout. */
             tableLayout.addView(tr1, new TableLayout.LayoutParams(
                     TableRow.LayoutParams.FILL_PARENT,
                     TableRow.LayoutParams.WRAP_CONTENT));
 
             j++;
             if (j == 2) {
                 j = 0;
             }
         }
     }
 }
