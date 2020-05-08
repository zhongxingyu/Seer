 package com.cheesymountain.woe;
 
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 
 public class MainActivity extends Activity {
 	
 	private Everbie everbie;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         //menu_feed
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.main_menu, menu);
         return true;
     }
     
     public boolean onOptionsItemSelected(MenuItem item){
     	switch(item.getItemId()){
     		case R.id.menu_feed:
     			this.setContentView(R.menu.feed_menu);
     			return true;
     	}
 		return false;
     }
     
     public void change(View view){
     	everbie = Everbie.getEverbie();
     	((TextView)findViewById(R.id.charmText)).setText(everbie.getCharm() + "");
     	((TextView)findViewById(R.id.cuteText)).setText(everbie.getCuteness() + "");
     	((TextView)findViewById(R.id.levelText)).setText(everbie.getLevel() + "");
     	((TextView)findViewById(R.id.strengthText)).setText(everbie.getStrength() + "");
     	((TextView)findViewById(R.id.staminaText)).setText(everbie.getStamina() + "");
     	((TextView)findViewById(R.id.intelligenceText)).setText(everbie.getIntelligence() + "");
     	
     	((ProgressBar)findViewById(R.id.fullnessBar)).setProgress(everbie.getFullness());
     	((ProgressBar)findViewById(R.id.happinessBar)).setProgress(everbie.getHappiness());
     	((ProgressBar)findViewById(R.id.toxicityBar)).setProgress(everbie.getToxicity());
     	((ProgressBar)findViewById(R.id.healthBar)).setProgress(everbie.getHealth());
     	((ProgressBar)findViewById(R.id.healthBar)).setMax(everbie.getMaxHealth());
    
     	
    	this.setContentView(R.layout.activity_stats);
     }
     
     public void back(View view){
     	this.setContentView(R.layout.activity_main);
     }
 }
