 package com.dungeonpaths;
 
 import android.app.ActionBar;
 import android.content.Intent;
 import android.os.Bundle;
 import android.app.Activity;
 import android.view.Menu;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class DungeonActivity extends Activity {
     private Level level;
     private ArrayList<Button> levelButtons;
     private ActionBar actionBar;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.dungeon_main);
 
         int levelId = (int) getIntent().getIntExtra("FILE_ID", 0);
         this.level = Level.getLevel(getResources(), levelId);
         actionBar = getActionBar();
         actionBar.setTitle(Global.getHero().getName() + " in '" + level.getName() +  "'");
         showCurrentEvent();
     }
 
     View.OnClickListener getOnClickChangeEvent(final int eventId) {
         return new View.OnClickListener() {
             public void onClick(View v) {
                 level.setEventById(eventId);
                 showCurrentEvent();
             }
         };
     }
 
     View.OnClickListener getOnClickVictory() {
         return new View.OnClickListener() {
             public void onClick(View v) {
                 showVictory();
             }
         };
     }
 
     private void showVictory() {
         Intent intent = new Intent(this, WinningActivity.class);
         startActivity(intent);
     }
 
     private void showCurrentEvent() {
         if (levelButtons != null) {
             for (Button button : levelButtons) {
                 ViewGroup layout = (ViewGroup) button.getParent();
                 if (null != layout)  layout.removeView(button);
             }
         }
 
         TextView textView = (TextView) findViewById(R.id.main_text);
         Event currentEvent = level.getCurrentEvent();
         textView.setText(currentEvent.getText());
         actionBar.setSubtitle(currentEvent.getPlace());
 
         LinearLayout layout = (LinearLayout) findViewById(R.id.main_layout);
         LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                 LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
         params.setMargins(0, 0, 0, 20);
 
         ItemReward item = currentEvent.getItem();
         TextView itemView = (TextView)findViewById(R.id.item_text);
         if (item != null){
             itemView.setText(item.getText());
             itemView.setVisibility(View.VISIBLE);
             Global.receiveItem(item);
         }else{
             itemView.setVisibility(View.GONE);
         }
 
         ArrayList<Button> buttons = new ArrayList<Button>();
 
         Action[] actions = currentEvent.getActions();
         if (actions != null) {
             for (Action action : actions) {
                 Button button = new Button(this);
                 button.setText(action.getText());
                 button.setLayoutParams(params);
                 button.setPadding(30, 10, 30, 10);
                 button.setTextSize(20);
                 button.setOnClickListener(getOnClickChangeEvent(action.getNextEventId()));
                 layout.addView(button);
                 buttons.add(button);
             }
         }
 
         levelButtons = buttons;
         if (level.isVictory()) {
             Button button = new Button(this);
             button.setText("VICTORY!");
             button.setLayoutParams(params);
             button.setPadding(30, 10, 30, 10);
             button.setOnClickListener(getOnClickVictory());
 
             layout.addView(button);
         }
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.dungeon, menu);
         return true;
     }
 
 }
