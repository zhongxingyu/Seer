 package com.cheesymountain.woe;
 /*=============================================================
  * Copyright 2012, Cheesy Mountain Production
  * 
  * This file is part of World of Everbies.
  * 
  * World of Everbies is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * World of Everbies is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with World of Everbies.  If not, see <http://www.gnu.org/licenses/>.
 ================================================================*/
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.app.Dialog;
 import android.app.AlertDialog.Builder;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.ImageButton;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 
 import com.cheesymountain.woe.food.BreadAndWater;
 import com.cheesymountain.woe.food.Melon;
 import com.cheesymountain.woe.food.RibEyeSteak;
 import com.cheesymountain.woe.interact.Chat;
 import com.cheesymountain.woe.interact.Comb;
 import com.cheesymountain.woe.interact.Snuggle;
 import com.cheesymountain.woe.item.Book;
 import com.cheesymountain.woe.item.HealthPotion;
 import com.cheesymountain.woe.item.Kettlebell;
 import com.cheesymountain.woe.item.Ribbon;
 import com.cheesymountain.woe.item.SkippingRope;
 import com.cheesymountain.woe.training.Chess;
 import com.cheesymountain.woe.training.Running;
 import com.cheesymountain.woe.training.WorkingOut;
 import com.cheesymountain.woe.work.Consulting;
 import com.cheesymountain.woe.work.DogWalking;
 import com.cheesymountain.woe.work.MotelCleaning;
 import com.cheesymountain.woe.work.Plumbing;
 import com.cheesymountain.woe.work.SellLemonade;
 
 @SuppressLint("NewApi")
 /**
  * Main Activity that keeps most of the controlling
  * functionality such as saving and loading game and
  * handles events from OptionsMenu. Moreover it controls
  * the exitGame functionality and what buttons can activate it.
  * 
  * @author CheesyMountain
  * 
  */
 public class MainActivity extends Activity {
 
 	private Use use;
 	private static final int DIALOG_EXIT_APP_ID = 1;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
     	super.onCreate(savedInstanceState);
     	
     	//calling back method to remove duplicated code.
         back(null);
         use = new Use();
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.main_menu, menu);
         return true;
     }
     
     @SuppressWarnings("deprecation")
 	@Override
     public void onBackPressed(){
     	showDialog(DIALOG_EXIT_APP_ID);
     }
     
     @Override
     public void onSaveInstanceState(Bundle savedInstanceState){
     	super.onSaveInstanceState(savedInstanceState);
     	savedInstanceState.putString("Name", Everbie.getEverbie().getName());
     	savedInstanceState.putInt("maxHealth", Everbie.getEverbie().getMaxHealth());
     	savedInstanceState.putInt("health", Everbie.getEverbie().getHealth());
     	savedInstanceState.putInt("Strength", Everbie.getEverbie().getStrength());
     	savedInstanceState.putInt("Intelligence", Everbie.getEverbie().getIntelligence());
     	savedInstanceState.putInt("stamina", Everbie.getEverbie().getStamina());
     	savedInstanceState.putInt("charm", Everbie.getEverbie().getCharm());
     	savedInstanceState.putInt("fullness", Everbie.getEverbie().getFullness());
     	savedInstanceState.putInt("happiness", Everbie.getEverbie().getHappiness());
     	savedInstanceState.putInt("toxicity", Everbie.getEverbie().getToxicity());
     	savedInstanceState.putInt("cuteness", Everbie.getEverbie().getCuteness());
     	savedInstanceState.putInt("money", Everbie.getEverbie().getMoney());
     	savedInstanceState.putBoolean("Alive", Everbie.getEverbie().isAlive());
     	savedInstanceState.putInt("imagePath", Everbie.getEverbie().getImageId());
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item){
 		switch(item.getItemId()){
			case R.id.return_menu:
 				Intent intent = new Intent("com.cheesymountain.woe.STARTSCREENACTIVITY");
 				startActivity(intent);
				finish();
 			case R.id.BreadAndWater:
 				use.activate(new BreadAndWater());
 	    		break;
 			case R.id.Melon:
 				use.activate(new Melon());
 	    		break;
 			case R.id.ribEyeStake:
 				use.activate(new RibEyeSteak());
 	    		break;
 			case R.id.chat:
 				use.activate(new Chat());
 	    		break;
 			case R.id.comb:
 				use.activate(new Comb());
 	    		break;
 			case R.id.snuggle:
 				use.activate(new Snuggle());
 	    		break;
 			case R.id.book:
 				use.activate(new Book());
 	    		break;
 			case R.id.healthPotion:
 				use.activate(new HealthPotion());
 	    		break;
 			case R.id.kettleBell:
 				use.activate(new Kettlebell());
 	    		break;
 			case R.id.ribbon:
 				use.activate(new Ribbon());
 	    		break;
 			case R.id.skippingRope:
 				use.activate(new SkippingRope());
 	    		break;
 			case R.id.chess:
 				use.activate(new Chess());
 	    		break;
 			case R.id.running:
 				use.activate(new Running());
 	    		break;
 			case R.id.workingOut:
 				use.activate(new WorkingOut());
 	    		break;
 	    	case R.id.consulting:
 	    		use.activate(new Consulting());
 	    		break;
 			case R.id.dogWalking:
 	    		use.activate(new DogWalking());
 	    		break;
 			case R.id.motelCleaning:
 				use.activate(new MotelCleaning());
 	    		break;
 			case R.id.plumbing:
 				use.activate(new Plumbing());
 	    		break;
 	    	case R.id.sellLemonade:
 	    		use.activate(new SellLemonade());
 	    		break;
 	    	default:
 	    		return false;
 		}
 		back(null);
 		return true;
     }
     
     @Override
 	public Dialog onCreateDialog(int i){
 		switch(i){
 			case DIALOG_EXIT_APP_ID:
 				Builder builder = new Builder(this);
 				builder.setMessage("Are you sure you want to Exit World of Everbies?");
 				builder.setCancelable(true);
 				builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int which) {
 						finish();
 					}
 				});
 				builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int which) {
 						dialog.cancel();
 					}
 				});
 				return builder.create();
 			default:
 				return null;
 		}
 	}
     
     @Override
     public void onOptionsMenuClosed(Menu menu) {
     	super.onOptionsMenuClosed(menu);
     /*	View view = this.getCurrentFocus();
     	this.setContentView(view);*/
     }
     
     //TODO Write More javadoc here
     /**
      * 
      * @param view
      */
     public void change(View view){
     	this.setContentView(R.layout.activity_stats);
     	
     	 ((ImageButton)findViewById(R.id.statsImage)).setImageResource(Everbie.getEverbie().getImageId());
     	
     	((TextView)findViewById(R.id.nameText)).setText(Everbie.getEverbie().getName() + "");
     	((TextView)findViewById(R.id.moneyText)).setText(Everbie.getEverbie().getMoney() + "");
     	((TextView)findViewById(R.id.charmText)).setText(Everbie.getEverbie().getCharm() + "");
     	((TextView)findViewById(R.id.cuteText)).setText(Everbie.getEverbie().getCuteness() + "");
     	((TextView)findViewById(R.id.levelText)).setText(Everbie.getEverbie().getLevel() + "");
     	((TextView)findViewById(R.id.strengthText)).setText(Everbie.getEverbie().getStrength() + "");
     	((TextView)findViewById(R.id.staminaText)).setText(Everbie.getEverbie().getStamina() + "");
     	((TextView)findViewById(R.id.intelligenceText)).setText(Everbie.getEverbie().getIntelligence() + "");
     	
     	((ProgressBar)findViewById(R.id.fullnessBar)).setProgress(Everbie.getEverbie().getFullness());
     	((ProgressBar)findViewById(R.id.happinessBar)).setProgress(Everbie.getEverbie().getHappiness());
     	((ProgressBar)findViewById(R.id.toxicityBar)).setProgress(Everbie.getEverbie().getToxicity());
     	((ProgressBar)findViewById(R.id.healthBar)).setMax(Everbie.getEverbie().getMaxHealth());
     	((ProgressBar)findViewById(R.id.healthBar)).setProgress(Everbie.getEverbie().getHealth());
     }
     
     /**
      * A method called whenever the "back" button is pressed/tapped on the phone.
      * 
      * @param view the view that is currently active when the button is pressed.
      */
     public void back(View view){
     	this.setContentView(R.layout.activity_main);
         ((ImageButton)findViewById(R.id.mainImage)).setImageResource(Everbie.getEverbie().getImageId());
     	updateLog();
     }
     
     /**
      * Rewrites the TextArea with the current version
      * of the Log.
      */
     public void updateLog(){
     	((TextView)findViewById(R.id.log)).setText(Log.getLog().getLogList());
     }    
 }
