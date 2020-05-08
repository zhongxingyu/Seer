 package com.example.tomatroid;
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.util.Log;
 
 public class DialogManager implements OnClickListener {
 
 	int tag = 0;
 	MainActivity mA;
 	AlertDialog dialog;
 	boolean longBreak = false;
 
 	public DialogManager(MainActivity mA) {
 		this.mA = mA;
 	}
 
 	public void show(int tag, boolean longBreak) {
 		// Cancel previous Dialog
 		if (dialog != null) {
 			dialog.cancel();
 		}
 
 		this.tag = tag;
 		this.longBreak = longBreak;
 
		AlertDialog.Builder builder = new AlertDialog.Builder(mA);
 		builder.setNegativeButton("Stop", this);
 		builder.setNeutralButton("Skip!", this);
 		
 		// Start new Dialog
 		switch (tag) {
 		// Pomodoro
 		case 0:			
 			if (longBreak) {		
 				builder.setTitle(mA.getString(R.string.longbreak));	
 			} else {
 				builder.setTitle(mA.getString(R.string.shortbreak));
 			}
 			
 			builder.setMessage("You are done with your Pomodoro, lets take a long break\n" +
 					  		   "(you will be rememberd in "+mA.rememberTime+"min)");
 			builder.setPositiveButton("Take the break!", this);
 			break;
 		// After Short of Long break
 		default:
 			builder.setTitle("Pomodoro Time!");
 			builder.setMessage("Your break is over, lets do some work\n" +
 					  		   "(you will be rememberd in "+mA.rememberTime+"min)");
 			builder.setPositiveButton("Do a pomodoro!", this);
 			break;
 		}
 		
 		dialog = builder.create();
 		dialog.show();
 	}
 
 	@Override
 	public void onClick(DialogInterface dialog, int which) {
		Log.e("Dialog", "Which " + which);
 		switch (which) {
 		
 		// Stop
 		case -2:
 			mA.controlListener.end(tag);
 			break;
 		// Button Skip
 			
 		case -3:
 			mA.controlListener.restart();
 			break;
 			
 		// Button Take
 		case -1:
 			switch (tag) {
 			// Is pomodoro, start a break
 			case 0:
 				// Start Long or Short
 				if (longBreak) {
 					mA.controlListener.start(2);
 				} else {
 					mA.controlListener.start(1);
 				}
 				break;
 			// Is break, start pomodoro
 			default:
 				mA.controlListener.start(0);
 				break;
 			}
 			break;
 		}
 	}
 }
