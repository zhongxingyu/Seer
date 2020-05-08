 package com.github.offby1.twodifferentactivities;
 
 import android.os.Bundle;
 import android.content.Intent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.Toast;
 
 public class MainActivity extends HasASettingsMenuActivity {
     Button b;
 
     MainActivity this_activity;
     ListView lv;
 
     String[] data = { "item1", "item2", "item3" };
     ArrayAdapter<String> adapter;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
 
         this_activity = this;
         lv = (ListView)this.findViewById (R.id.list);
 
         Toast.makeText(this_activity, String.format("lv ID is %d",
                                                     R.id.list), Toast.LENGTH_LONG).show();
 
         adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, data);
         lv.setAdapter (adapter);
 
         lv.setOnItemClickListener(new OnItemClickListener() {
 
                 public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
 					long arg3) {
                     Toast.makeText(this_activity, String.format("Someone clicked on %d",
                                                                arg2),
                                   Toast.LENGTH_SHORT).show()
                        ;
 
                 }
             });
 
         b = (Button)this.findViewById(R.id.button1);
         b.setOnClickListener(new OnClickListener()
             {
                 public void onClick(View v) {
                     Intent i = new Intent(this_activity, OtherActivity.class);
                     this_activity.startActivity(i);
                 }
             });
     }
 }
