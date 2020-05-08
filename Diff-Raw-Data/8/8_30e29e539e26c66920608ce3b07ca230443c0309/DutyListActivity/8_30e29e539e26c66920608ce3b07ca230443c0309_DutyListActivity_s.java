 package org.bxmy.shiftclock;
 
import java.text.SimpleDateFormat;
 import java.util.ArrayList;
import java.util.Date;
 import java.util.HashMap;
 
 import org.bxmy.shiftclock.shiftduty.Duty;
 import org.bxmy.shiftclock.shiftduty.ShiftDuty;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemLongClickListener;
 import android.widget.ListView;
 import android.widget.SimpleAdapter;
 
 public class DutyListActivity extends Activity {
     private Duty[] mDuties;
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.duty_list);
 
         ListView list = (ListView) findViewById(R.id.list_duty);
         list.setOnItemLongClickListener(new OnItemLongClickListener() {
 
             @Override
             public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                     int position, long arg3) {
                 if (mDuties == null || mDuties.length == 0) {
                     if (position == 0) {
                         openDutyEditor(-1);
                     }
                 } else {
                     if (position < mDuties.length)
                         openDutyEditor(position);
                     else if (position == mDuties.length)
                         openDutyEditor(-1);
                 }
 
                 return true;
             }
 
         });
 
         loadDuties();
     }
 
     @Override
     public void onResume() {
         super.onResume();
         loadDuties();
     }
 
     private void loadDuties() {
         ListView list = (ListView) findViewById(R.id.list_duty);
         SimpleAdapter adapter = new SimpleAdapter(this, initDuties(),
                 R.layout.duty_list_item, new String[] { "1", "2", "3" },
                 new int[] { R.id.text_dutyName, R.id.time_dutyStart,
                         R.id.time_dutyEnd });
         list.setAdapter(adapter);
     }
 
     private ArrayList<HashMap<String, Object>> initDuties() {
         ArrayList<HashMap<String, Object>> items = new ArrayList<HashMap<String, Object>>();
         HashMap<String, Object> map = new HashMap<String, Object>();
         mDuties = ShiftDuty.getInstance().getDuties();
         for (Duty duty : mDuties) {
             map = new HashMap<String, Object>();
             map.put("1", duty.getName());
             int start = duty.getStartSecondsInDay();
             map.put("2", formatSecondsInDay(start));
             int end = duty.getStartSecondsInDay() + duty.getDurationSeconds();
             map.put("3", formatSecondsInDay(end));
             items.add(map);
         }
 
         boolean appendNewItem = true;
         if (appendNewItem) {
             map = new HashMap<String, Object>();
             map.put("1", "<新增班种>");
             items.add(map);
         }
 
         return items;
     }
 
     private String formatSecondsInDay(int seconds) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        return sdf.format(new Date(seconds * 1000L));
     }
 
     private void openDutyEditor(int position) {
         Intent intent = new Intent();
         intent.putExtra("index", position);
         intent.setClass(this, EditDutyActivity.class);
 
         startActivity(intent);
     }
 }
