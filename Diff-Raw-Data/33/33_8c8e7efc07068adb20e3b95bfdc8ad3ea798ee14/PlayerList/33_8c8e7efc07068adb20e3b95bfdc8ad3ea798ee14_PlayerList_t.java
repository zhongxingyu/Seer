 package net.erickelly.score;
 
 import android.app.AlertDialog;
 import android.app.ListActivity;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.Gravity;
 import android.view.KeyEvent;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.Toast;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 
 public class PlayerList extends ListActivity {
 	
 	Player selectedPlayer;
 	AdapterContextMenuInfo info;
 	AlertDialog.Builder alert;
 	EditText input;
	Integer defvalue;
 	
 	/** Called when the activity is first created. */
 	public void onCreate(Bundle bundle) {
 		super.onCreate(bundle);
		defvalue = getIntent().getExtras().getInt("playernum");
 		this.setListAdapter(new PlayerArrayAdapter(this, Score.players));
 		registerForContextMenu(getListView());
 	}
 	
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 	    if ((keyCode == KeyEvent.KEYCODE_BACK)) {
 	    	Intent intent = new Intent();
	    	intent.putExtra("playerid", defvalue);
 	    	setResult(RESULT_OK, intent);
 	    	finish();
 	    	return true;
 	    }
 	    return super.onKeyDown(keyCode, event);
 	}
 	
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
 		super.onCreateContextMenu(menu, v, menuInfo);
 		
 	    // Get the info on which item was selected
 	    info = (AdapterContextMenuInfo) menuInfo;
 
 	    // Retrieve the item that was clicked on
 	    Player selectedPlayer = (Player) getListAdapter().getItem(info.position);
 		
 		menu.setHeaderTitle(selectedPlayer.name + " Actions");
		menu.add(0, 0, 0, "Set Name");
		menu.add(0, 1, 0, "Set Score");
		menu.add(0, 2, 0, "Remove Player");
 		if(Score.players.size() == 1) {
 			menu.getItem(2).setEnabled(false);
 		}
 	}
 	
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 //		AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item 
 //		.getMenuInfo(); 
 		super.onContextItemSelected(item);
 		/* Switch on the ID of the item, to get what the user selected. */
 		switch (item.getItemId()) { 
 		case 0:
 			createDialog(Score.players.get(info.position).name, 0);
 			refreshPlayers();
 			return true;
 		case 1:
 			createDialog(Score.players.get(info.position).score.toString(), 1);
 			refreshPlayers();
 			return true;
 		case 2:  
 			/* Remove it from the list. */ 
			Score.players.remove(info.position);
 			refreshPlayers();
 			return true;
 		}
 		return false; 
 	}
 	
 	private void refreshPlayers() { 
 		this.setListAdapter(new PlayerArrayAdapter(this, Score.players)); 
 	}
 	
 	public void createDialog(String startText, final int itemid) {
 		
 		// Setup dialog
         alert = new AlertDialog.Builder(this);
 		input = new EditText(this);
 		input.setMinWidth(250);
 		alert.setView(input);
 		input.setText(startText);
 		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int whichButton) {
 				String result = input.getText().toString().trim();
 
 				switch(itemid) {
 				case 0:
 					if((!result.contains(Score.separater)) && 
 							(!result.contains(Player.separater)) &&
 							(!result.equals(""))) {
 						Score.players.get(info.position).name = result;
 					} else {
 						Toast toast = Toast.makeText(getApplicationContext(), "Empty names and the characters " +
 								Score.separater + " and " + Player.separater + " are not allowed.",Toast.LENGTH_LONG);
 						toast.setGravity(Gravity.BOTTOM, 0, 100);
 						toast.show();
 						createDialog(result,0);
 					}
 					refreshPlayers();
 					break;
 				case 1:
 					setScore(result);
 					refreshPlayers();
 					break;
 				default:
 					refreshPlayers();
 					break;
 				}
 			}
 		});
 
 		alert.setNegativeButton("Cancel",
 				new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int whichButton) {
 						dialog.cancel();}
 				});
     			
     	alert.show();
 	}
 	
 	public void setScore(String s) {
     	if(s.equalsIgnoreCase("")) {
     		Toast toast = Toast.makeText(getApplicationContext(), "Empty values are not allowed.",Toast.LENGTH_SHORT);
 			toast.setGravity(Gravity.BOTTOM, 0, 100);
 			toast.show();
 			createDialog(s,1);
     	} else {
     		try {
     			int value = Integer.parseInt(s);
     			Score.players.get(info.position).score = value;
     		} catch(NumberFormatException nfe) {
     			Log.d("setScore","Could not parse " + nfe);
     			Toast toast = Toast.makeText(getApplicationContext(), "Must be an integer.",Toast.LENGTH_SHORT);
     			toast.setGravity(Gravity.BOTTOM, 0, 100);
     			toast.show();
     			createDialog(s,1);
     		}
     	}
     }
 	
 	@Override
 	public void onPause() {
 		super.onPause();
 		Score.save();
 	}
 }
