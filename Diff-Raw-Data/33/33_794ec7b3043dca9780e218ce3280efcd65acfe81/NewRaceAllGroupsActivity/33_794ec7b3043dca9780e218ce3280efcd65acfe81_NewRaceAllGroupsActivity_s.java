 /**
  * KartMatch: NewRaceAllGroupsActivity.java
  *
  *   Screen to associate the pilots with their group. This is in preparation for a new race.
  *
  * Copyright (C) 2013, Pierre DEJOUE
  * All rights reserved.
  * This software may be modified and distributed under the terms of the BSD license. See the LICENSE file for details. 
  */
 package fr.neuf.perso.pdejoue.kart_match;
 
 import java.util.ArrayList;
 
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
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.RadioButton;
 import android.widget.RadioGroup;
 
 public class NewRaceAllGroupsActivity extends Activity 
 {
     public static final int GROUP_BUTTON_BASE_ID = 333;             // A base ID for the group selection button. 
                                                                     // Hopefully there is no ID collision with other views in the same activity
 
     private CustomApplication main_application = null;    
     private int original_text_color;
     
     // Boolean array used to workaround a bug in this activity when the screen orientation changes.
     private enum Tristate { INIT, SPURIOUS, DONE };
     private ArrayList<Tristate> capture_spurious_rg_event = new ArrayList<Tristate>();
 
     //
     // Listeners
     //
     
     // listener for the radio buttons
     private RadioGroup.OnCheckedChangeListener radiogroup_handler = new RadioGroup.OnCheckedChangeListener() 
     {
         public void onCheckedChanged(RadioGroup rg, int group_nb) 
         {
             PilotTag pilot_tag = (PilotTag)rg.getTag();
             
             if(capture_spurious_rg_event.get(pilot_tag.index) == Tristate.SPURIOUS)     // Ignore current event if the previous one was a spurious event (see below)
             {
                 capture_spurious_rg_event.set(pilot_tag.index, Tristate.DONE);
                 rg.check(main_application.pilot_group.get(pilot_tag.index));            // Fix the radiogroup
                 return;
             }
             
             if(group_nb == main_application.pilot_group.get(pilot_tag.index))
             {
                 // Spurious event: the pilot group is already equal to the group number specified by this event
                 // We noticed such events after changing the orientation of the screen. They are followed
                 // by another event that set the pilot's group to the same group as the last pilot in the list!
                 // So in case of such event, ignore the next event on the same RadioGroup, but only during the
                 // init phase of the Activity.
          
                 if(capture_spurious_rg_event.get(pilot_tag.index) == Tristate.INIT) 
                 {
                     capture_spurious_rg_event.set(pilot_tag.index, Tristate.SPURIOUS);
                 }
             }
             else
             {
                 main_application.pilot_group.set(pilot_tag.index, group_nb);        // Update the pilot_group array
                 
                 // Update the submit buttons text and color
                 edit_group_buttons_text_and_colors();
             }           
         }
     };
     
     // Listener for the submit buttons at the bottom of the Activity
     private View.OnClickListener group_select_handler = new View.OnClickListener() 
     {
         public void onClick(View v) 
         {
             int group_nb = v.getId() - GROUP_BUTTON_BASE_ID;
             
             // Block the next Activity if one group is too big (more pilot than there are cars available). 
             if(main_application.allGroupSizesOK())
             {
                 // Generate a random matching of pilots and cars for that group
                 main_application.random_matching = main_application.generate_random_pilot_to_car_mapping(group_nb);
                 
                 // Launch next activity
                 Intent intent = new Intent(getApplicationContext(), NewRaceFinalActivity.class);
                 intent.putExtra("group_nb", group_nb);
                 startActivity(intent);
             }
         }
     };  
     
     //
     // Overridden methods
     //
     
     @Override
     protected void onCreate(Bundle savedInstanceState) 
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_new_race_all_groups);
         main_application = (CustomApplication)getApplication();
         
         // Show the Up button in the action bar.
         setupActionBar();
         
         // Build the scroll view
         build_scroll_view();
         
         // One Button per group
         LinearLayout main_layout = (LinearLayout)findViewById(R.id.main_layout);
         for(int group_nb = 1; group_nb <= main_application.getNbOfGroups(); group_nb++)
         {
             Button new_button = new Button(this);
             new_button.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
             new_button.setText(getResources().getString(R.string.group_capitalize) + " " + Integer.toString(group_nb));
             new_button.setOnClickListener(group_select_handler);
             new_button.setId(GROUP_BUTTON_BASE_ID + group_nb);
             main_layout.addView(new_button);
         }
         
         // Store original text color
         Button submit_button = (Button)findViewById(GROUP_BUTTON_BASE_ID + 1);       // Submit button for group one (there is at least one group ).
         original_text_color = submit_button.getCurrentTextColor();
         
         // Update the submit buttons text and color
         edit_group_buttons_text_and_colors();
     }
     
     @Override
     protected void onDestroy()
     {        
         super.onDestroy();
 
         // Erase the content of the scroll view
         LinearLayout pilot_list = (LinearLayout)findViewById(R.id.pilot_list);
         pilot_list.removeAllViews();
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
             getActionBar().setTitle(R.string.title_activity_new_race_all_groups);
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
     
     //
     // Class specific methods
     //
     
     private void build_scroll_view()
     {
         // Erase the content of the scroll view
         LinearLayout pilot_list = (LinearLayout)findViewById(R.id.pilot_list);
         //pilot_list.removeAllViews();
        
         for(int index = 0; index < main_application.nb_of_pilots; index++)
         {
             PilotTag pilot_tag = new PilotTag(index);       // Tag attached to the EditText and Button views
             
             LinearLayout new_horiz_layout = new LinearLayout(this);
             new_horiz_layout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1.0f));
             new_horiz_layout.setOrientation(LinearLayout.HORIZONTAL);
             
             EditText new_text_view = new EditText(this);
             new_text_view.setText(main_application.getPilotName(index));
             new_text_view.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1.0f));
             new_text_view.setTag((Object)pilot_tag); 
            new_text_view.setInputType(InputType.TYPE_NULL);            // The text cannot be edited
             new_horiz_layout.addView(new_text_view);
             
             // Add radio buttons for group selection
             
             int nb_groups = main_application.getNbOfGroups();
             RadioButton[] rb = new RadioButton[nb_groups];
             RadioGroup rg = new RadioGroup(this);
             rg.setOrientation(RadioGroup.HORIZONTAL);
             rg.setTag((Object)pilot_tag); 
             for(int group_idx = 0; group_idx < nb_groups; group_idx++)
             {
                 rb[group_idx] = new RadioButton(this);
                 rb[group_idx].setText(getResources().getString(R.string.group_first_letter) + Integer.toString(group_idx+1));
                 rb[group_idx].setId(group_idx+1);
                 rg.addView(rb[group_idx]); 
             }
             rg.check(main_application.pilot_group.get(index));      // Check the current group
 
             rg.setOnCheckedChangeListener(radiogroup_handler);
             new_horiz_layout.addView(rg);
             
             pilot_list.addView(new_horiz_layout);
             
             capture_spurious_rg_event.add(Tristate.INIT);
         }
         
         // Redraw the scroll view
         //pilot_list.invalidate();
     }
     
     private void edit_group_buttons_text_and_colors()
     {
         for(int group_nb = 1; group_nb <= main_application.getNbOfGroups(); group_nb++)
         {
             Button submit_button = (Button)findViewById(GROUP_BUTTON_BASE_ID + group_nb);           // Retrieve button view
             submit_button.setText(getResources().getString(R.string.group_capitalize) + " " + group_nb + " (" + main_application.getGroupSize(group_nb) + " " + getResources().getString(R.string.pilots) + ")");
             
             // Text color is red if the group size is greater than the actual number of cars is zero. 
             // In that case access to the next Activity is blocked.
             if(!main_application.isGroupSizeOK(group_nb))
             {
                 submit_button.setTextColor(getResources().getColor(R.color.dark_red));
             }
             else
             {
                 submit_button.setTextColor(original_text_color);
             }
         }
     }
 }
