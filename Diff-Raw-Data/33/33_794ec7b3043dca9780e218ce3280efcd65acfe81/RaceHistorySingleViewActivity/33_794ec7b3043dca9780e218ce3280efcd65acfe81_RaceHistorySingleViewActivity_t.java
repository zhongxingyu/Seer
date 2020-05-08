 /**
  * KartMatch: RaceHistorySingleViewActivity.java
  *
  *   Screen that displays the details of a race.
  *
  * Copyright (C) 2013, Pierre DEJOUE
  * All rights reserved.
  * This software may be modified and distributed under the terms of the BSD license. See the LICENSE file for details. 
  */
 package fr.neuf.perso.pdejoue.kart_match;
 
 import android.annotation.TargetApi;
 import android.app.Activity;
 import android.os.Build;
 import android.os.Bundle;
 import android.text.InputType;
 import android.view.Gravity;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 public class RaceHistorySingleViewActivity extends Activity 
 {
     private CustomApplication main_application = null;
     
     //
     // Overridden methods
     //
     
     @Override
     protected void onCreate(Bundle savedInstanceState) 
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_race_history_single_view);
         main_application = (CustomApplication)getApplication();
         
         // Show the Up button in the action bar.
         setupActionBar();
         
         // Get race Id  and display its information
         Bundle extras = getIntent().getExtras();
         int race_id = extras.getInt("race_id");
         
         TextView text = (TextView)findViewById(R.id.race_view_introduction);
         text.setText(main_application.getRaceHistoryList().get(race_id));
         
         build_scroll_view(main_application.get_race_history(race_id));
     }
 
     /**
      * Set up the {@link android.app.ActionBar}, if the API is available.
      */
     @TargetApi(Build.VERSION_CODES.HONEYCOMB)
     private void setupActionBar() {
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) 
         {
             getActionBar().setDisplayHomeAsUpEnabled(false);
             getActionBar().setTitle(R.string.title_activity_race_history_single_view);
         }
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) 
     {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.options_menu, menu);
         return true;
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) 
     {
         // Handle item selection
         switch (item.getItemId()) 
         {
             case R.id.about_menu:
                 main_application.about_dialog(this);
                 return true;
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
     
     private void build_scroll_view(RaceDetails rd)
     {
         // Erase the content of the scroll view
         LinearLayout pilot_list = (LinearLayout)findViewById(R.id.pilot_list);
         pilot_list.removeAllViews();
         
         for(int index = 0; index < main_application.nb_of_pilots; index++)
         {
             if(rd.isTherePilot(index))
             {
                 LinearLayout new_horiz_layout = new LinearLayout(this);
                 new_horiz_layout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1.0f));
                 new_horiz_layout.setOrientation(LinearLayout.HORIZONTAL);
                 
                 EditText new_text_view = new EditText(this);
                 new_text_view.setText(main_application.getPilotName(index));
                 new_text_view.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1.0f));
                new_text_view.setInputType(InputType.TYPE_NULL);                    // The text cannot be edited with the soft keypad
                new_text_view.setFocusable(false);                                  // The text cannot be edited
                 new_horiz_layout.addView(new_text_view);
                 
                 ImageView new_image_view = new ImageView(this);
                 new_image_view.setImageResource(R.drawable.kart);
                 new_horiz_layout.addView(new_image_view);
                 
                 EditText new_text_view_car_id = new EditText(this);
                 if(rd.pilot_to_car_mapping.matching.indexOfKey(index) >= 0)
                 {
                     new_text_view_car_id.setText(Integer.toString(rd.pilot_to_car_mapping.matching.get(index)));
                 }
                 else
                 {
                     // This pilot wasn't in the maximum matching, he therefore is assigned to a car number he already got
                     // Signal that by writing the car number red.
                     new_text_view_car_id.setText(Integer.toString(rd.pilot_to_car_mapping.unmatched.get(index)));
                     new_text_view_car_id.setTextColor(getResources().getColor(R.color.dark_red));
                 }
                 new_text_view_car_id.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
                new_text_view_car_id.setInputType(InputType.TYPE_NULL);             // The text cannot be edited with the soft keypad
                new_text_view_car_id.setFocusable(false);                           // The text cannot be edited
                 new_text_view_car_id.setEms(3);
                 new_text_view_car_id.setGravity(Gravity.RIGHT);
                 new_horiz_layout.addView(new_text_view_car_id);
                             
                 pilot_list.addView(new_horiz_layout);
             }
         }
         
         // Redraw the scroll view
         pilot_list.invalidate();
     }
     
 
 }
