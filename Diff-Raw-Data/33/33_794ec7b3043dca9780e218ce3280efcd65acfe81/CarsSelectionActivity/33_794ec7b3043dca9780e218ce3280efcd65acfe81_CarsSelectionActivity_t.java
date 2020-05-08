 /**
  * KartMatch: CarsSelectionActivity.java
  *
  *   Screen to select the cars that are actually available for the championship.
  *
  * Copyright (C) 2013, Pierre DEJOUE
  * All rights reserved.
  * This software may be modified and distributed under the terms of the BSD license. See the LICENSE file for details. 
  */
 package fr.neuf.perso.pdejoue.kart_match;
 
 import android.annotation.TargetApi;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Build;
 import android.os.Bundle;
 import android.text.InputType;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 
 public class CarsSelectionActivity extends Activity 
 {
     private CustomApplication main_application = null;
     private int original_text_color;
 
     //
     // Listeners
     //
 
     // Listener for the check buttons
     private CompoundButton.OnCheckedChangeListener car_selector = new CompoundButton.OnCheckedChangeListener() 
     {
         @Override
         public void onCheckedChanged(CompoundButton checkbox,boolean isChecked) 
         {
             PilotTag tag = (PilotTag)checkbox.getTag();
             if(isChecked)
             {
                 main_application.selectCar(tag.index);
             }
             else
             {
                 main_application.unselectCar(tag.index);
             }
             
             display_actual_nb_of_cars();
         }
     };  
     
     //
     // Overridden methods
     //
     
     @Override
     protected void onCreate(Bundle savedInstanceState) 
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_cars_selection);
         main_application = (CustomApplication)getApplication();
         
         // Setup action bar.
         setupActionBar();
         
         // List of cars:
         build_scroll_view();
         
         // Store original text color
         Button submit_button = (Button)findViewById(R.id.submit_button);
         original_text_color = submit_button.getCurrentTextColor();
                 
         // Update text of the submit button
         display_actual_nb_of_cars();
     }
 
     /**
      * Set up the {@link android.app.ActionBar}, if the API is available.
      */
     @TargetApi(Build.VERSION_CODES.HONEYCOMB)
     private void setupActionBar() 
     {
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) 
         {
             getActionBar().setDisplayHomeAsUpEnabled(false);
             getActionBar().setTitle(R.string.title_activity_cars_selection);
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
     
     
     // Normally called just once
     private void build_scroll_view()
     {
         // Erase the content of the scroll view
         LinearLayout cars_list = (LinearLayout)findViewById(R.id.cars_list);
         cars_list.removeAllViews();
        
         for(int car_number = 1; car_number <= main_application.max_nb_of_cars; car_number++)
         {
             PilotTag tag = new PilotTag(car_number);     // Tag attached to the Check buttons (Ok, we cheat by using a PilotTag...)
                     
             LinearLayout new_horiz_layout = new LinearLayout(this);
             new_horiz_layout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1.0f));
             new_horiz_layout.setOrientation(LinearLayout.HORIZONTAL);
             
             ImageView new_image_view = new ImageView(this);
             new_image_view.setImageResource(R.drawable.kart);
             new_horiz_layout.addView(new_image_view);
             
             EditText new_text_view = new EditText(this);
             new_text_view.setText(Integer.toString(car_number));
             new_text_view.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1.0f));
            new_text_view.setInputType(InputType.TYPE_NULL);            // The text cannot be edited (via soft keypad)
            new_text_view.setFocusable(false);                          // The text cannot be edited
             new_horiz_layout.addView(new_text_view);
                      
             CheckBox new_checkbox = new CheckBox(this);
             new_checkbox.setChecked(main_application.isCarSelected(car_number));
             //new_checkbox.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1.0f));
             new_checkbox.setTag((Object)tag);
             new_checkbox.setOnCheckedChangeListener(car_selector);
             new_horiz_layout.addView(new_checkbox);
             
             cars_list.addView(new_horiz_layout);
         }
         
         // Invalidate to force redraw
         cars_list.invalidate();
     }
     
     private void display_actual_nb_of_cars()
     {
         Button submit_button = (Button)findViewById(R.id.submit_button);
         submit_button.setText(getResources().getString(R.string.ok_button) + " (" + Integer.toString(main_application.getActualNbOfCars()) + " " + getResources().getString(R.string.cars) + ")");
         
         // Set text color. (Red if the actual number of cars is zero. In that case access to the next Activity is blocked.)
         if(main_application.getActualNbOfCars() == 0)
         {
             submit_button.setTextColor(getResources().getColor(R.color.dark_red));
         }
         else
         {
             submit_button.setTextColor(original_text_color);
         }
     }
     
     public void gotoNextActivity(View v)
     {
         // Launch next activity if actual number of cars is different from zero
         if(main_application.getActualNbOfCars() != 0)
         {
             Intent intent = new Intent(this, PilotsCarsValidateActivity.class);
             startActivity(intent);
             
             // Do not keep this Activity in memory
             finish();
         }
     }
 
 }
