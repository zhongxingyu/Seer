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
 import com.cheesymountain.woe.R;
 
 import android.app.Activity;
 import android.app.Dialog;
 import android.app.AlertDialog.Builder;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 
 public class StartscreenActivity extends Activity {
 
 	private static final int EVERBIE_ALREADY_ALIVE = 3;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState){
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_startscreen);
 		if(!Everbie.exists()){
 			findViewById(R.id.startContinueText).setClickable(false);
 			findViewById(R.id.startContinueText).setEnabled(false);
 		}
 	}
 
 	public void continueGame(View view){
 		Intent main = new Intent("com.cheesymountain.woe.MAINACTIVITY");
 		startActivity(main);
 		finish();
 	}
 	
	@SuppressWarnings("deprecation")
 	public void newGame(View view){
 		if(Everbie.exists() && Everbie.getEverbie().isAlive()){
 			showDialog(EVERBIE_ALREADY_ALIVE );
 		}else{
 			Intent main = new Intent("com.cheesymountain.woe.NEWEVERBIEACTIVITY");
 			startActivity(main);
 			finish();
 		}
 	}
 	
 	public void exitGame(View view){
 		finish();
 	}
 	
 	public Dialog onCreateDialog(int id){
 		if(id == EVERBIE_ALREADY_ALIVE){
 			Builder builder = new Builder(this);
 			builder.setMessage("Are you sure you want to create a new Everbie? This will erase the current one");
 			builder.setCancelable(true);
 			builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int which) {
 					Intent main = new Intent("com.cheesymountain.woe.NEWEVERBIEACTIVITY");
 					startActivity(main);
 					finish();
 				}
 			});
 			builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int which) {
 					dialog.cancel();
 				}
 			});
 			return builder.create();
 		}
 		return null;
 	}
 	
 }
