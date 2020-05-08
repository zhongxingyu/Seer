 package net.erickelly.score;
 
 import java.util.ArrayList;
 
 import net.erickelly.score.R;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences.Editor;
 import android.os.Bundle;
 import android.os.Handler;
 import android.content.SharedPreferences;
 import android.content.res.Resources;
 import android.graphics.drawable.Drawable;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnLongClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class Score extends Activity implements OnClickListener, OnLongClickListener {
     /** Called when the activity is first created. */
 
 	// Reference variables
 	TextView current_score;
 	TextView player;
 	AlertDialog.Builder alert;
 	EditText input;
 	static SharedPreferences pref;
 	Resources res;
 	Drawable shape;
 	RelativeLayout ll;
 	Handler h;
 	static String background;
 	static String separater = "<>";
 	Handler handler;
 	
 	// Button Reference Variables
 	// Names do not refer to actual values, but their positions
 	Button plus1;
 	Button plus2;
 	Button plus3;
 	Button min1;
 	Button min2;
 	Button min3;
 		
 	// Number Sets
 	static Integer set1;
 	static Integer set2;
 	static Integer set3;
 	
 	// Player variables
 	static Integer id;
 	static String name;
 	static Integer score;
 	static ArrayList<Player> players = new ArrayList<Player>();
 		
     @Override
     public void onCreate(Bundle savedInstanceState) {
     	    	
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         
     	ll = (RelativeLayout) findViewById(R.id.window);
         
         pref = getSharedPreferences("preferences",MODE_WORLD_WRITEABLE);
         res = getResources();
     	       	
     	restorePlayers();
         
         // Set the on screen handlers
         current_score = (TextView) findViewById(R.id.score);
         player = (TextView) findViewById(R.id.player);
         
         // Set up click listeners for all the buttons
         plus1 = (Button) findViewById(R.id.plus1);
         plus1.setOnClickListener(this);
         plus2 = (Button) findViewById(R.id.plus2);
         plus2.setOnClickListener(this);
         plus3 = (Button) findViewById(R.id.plus3);
         plus3.setOnClickListener(this);
         min1 = (Button) findViewById(R.id.min1);
         min1.setOnClickListener(this);
         min2 = (Button) findViewById(R.id.min2);
         min2.setOnClickListener(this);
         min3 = (Button) findViewById(R.id.min3);
         min3.setOnClickListener(this);
         
         // Long click listeners
         current_score.setOnLongClickListener(this);
         player.setOnLongClickListener(this);
         plus1.setOnLongClickListener(this);
         plus2.setOnLongClickListener(this);
         plus3.setOnLongClickListener(this);
         min1.setOnLongClickListener(this);
         min2.setOnLongClickListener(this);
         min3.setOnLongClickListener(this);
         		
 		handler = new Handler();
     	
     }
     
     private void restorePlayers() {
     	String ps = pref.getString("players", (new Player("Player",0)).serialize());
 		String a[] = ps.split(separater);
 		if (players.size() == 0) {
 			for(int i = 0; i < a.length; i++) {
 				players.add(Player.unpack(a[i]));
 			}
 		}
 		Log.d("Got this far", "hi");
     }
     
     private void restoreBackground() {
     	background = pref.getString("background", "black");
     	if(background.equals("blue")) {
     		shape = res.getDrawable(R.drawable.bgradient);
     	} else if(background.equals("green")) {
     		shape = res.getDrawable(R.drawable.ggradient);
     	} else if(background.equals("red")) {
     		shape = res.getDrawable(R.drawable.rgradient);
     	} else {
     		shape = res.getDrawable(R.drawable.nogradient);
     	}
     	shape.setDither(true);
     	ll.setBackgroundDrawable(shape);
     }
     
     private void switchBackground() {
     	if(background.equals("blue")) {
     		shape = res.getDrawable(R.drawable.ggradient);
     		background = "green";
     		Log.d("background = ", "blue");
     	} else if(background.equals("green")) {
     		shape = res.getDrawable(R.drawable.rgradient);
     		background = "red";
     		Log.d("background = ", "green");
     	} else if(background.equals("red")) {
     		shape = res.getDrawable(R.drawable.nogradient);
     		background = "black";
     		Log.d("background = ", "red");
     	} else {
     		shape = res.getDrawable(R.drawable.bgradient);
     		background = "blue";
     		Log.d("background = ", "black");
     	}
     	ll.setBackgroundDrawable(shape);		
 	}
 
     public void updateScore(int i) {
 		players.get(id).updateScore(i);
 		score = score + i;
 		current_score.setText((CharSequence) score.toString());
 	}
     
     @Override
     public void onResume() {
     	super.onResume();
     	
     	// Get stored value of items
     	id = pref.getInt("current_player", 0);
     	score = players.get(id).score;
         name = players.get(id).name;
     	restoreBackground();
         setSets();
         
         // display the variables
         player.setText(name);
     	current_score.setText((CharSequence) players.get(id).score.toString());
     }
     
     @Override
     public void onPause() {
     	super.onPause();
     	save();
     }
     
     @Override
     public void onClick(View v) {
     	switch(v.getId()) {
 	    	case R.id.min1:
 	    		updateScore((set1 * -1));
 	    		break;
 	    	case R.id.min2:
 	    		updateScore((set2 * -1));
 	    		break;
 	    	case R.id.min3:
 	    		updateScore((set3 * -1));
 	    		break;
 	    	case R.id.plus1:
 	    		updateScore(set1);
 	    		break;
 	    	case R.id.plus2:
 	    		updateScore(set2);
 	    		break;
 	    	case R.id.plus3:
 	    		updateScore(set3);
 	    		break;
     	}
     }
 
     @Override
     public boolean onLongClick(View v) {
     	switch(v.getId()) {
     		case R.id.score:
 	    		createDialog("0", findViewById(R.id.score));
     			break;
     		case R.id.player:
     			createDialog(name, findViewById(R.id.player));
     			break;
     		case R.id.min1:
        			createDialog("1", min1);
        			break;
     		case R.id.min2:
        			createDialog("5", min2);
        			break;
     		case R.id.min3:
        			createDialog("10", min3);
        			break;
     		case R.id.plus1:
        			createDialog("1", plus1);
        			break;
     		case R.id.plus2:
        			createDialog("5", plus2);
        			break;
     		case R.id.plus3:
        			createDialog("10", plus3);
        			break;
     	}
     	
     	return true;
     }
     
     public void createDialog(String startText, final View v) {
     	
     	// Setup dialog
         alert = new AlertDialog.Builder(this);
 		input = new EditText(this);
 		input.setMinWidth(250);
 		alert.setView(input);
 		input.setText(startText);
 		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int whichButton) {
 				String result = input.getText().toString().trim();
 
 				switch(v.getId()) {
 					case R.id.score:
 						setScore(result);
 						break;
 					case R.id.player:
 						setPlayer(result);
 						break;
 					case R.id.min1:
 						setButton(result, plus1, min1, 1);
 						break;
 					case R.id.min2:
 						setButton(result, plus2, min2, 2);
 						break;
 					case R.id.min3:
 						setButton(result, plus3, min3, 3);
 						break;
 					case R.id.plus1:
 						setButton(result, plus1, min1, 1);
 						break;
 					case R.id.plus2:
 						setButton(result, plus2, min2, 2);
 						break;
 					case R.id.plus3:
 						setButton(result, plus3, min3, 3);
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
     
     @Override
     public boolean onPrepareOptionsMenu(Menu menu) {
     	if(players.size() == 1) {
      	   menu.findItem(R.id.next).setEnabled(false);
      	   menu.findItem(R.id.prev).setEnabled(false);
      	   menu.findItem(R.id.sub_player).setEnabled(false);
         } else {
         	menu.findItem(R.id.next).setEnabled(true);
       	   menu.findItem(R.id.prev).setEnabled(true);
       	   menu.findItem(R.id.sub_player).setEnabled(true);
         }
     	return true;
     }
 
 	@Override
     public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);       
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
     	switch(item.getItemId()) {
     		case R.id.set_backgound:
     			switchBackground();
     			break;
     		case R.id.view_all:
     			Intent intent = new Intent(this, PlayerList.class);
     			intent.putExtra("playernum", id);
     			startActivityForResult(intent, 0);
     			break;
     		case R.id.add_player:
     			Integer i = players.size();
     			i++;
     			addPlayer("Player " + i.toString());
     			break;
     		case R.id.sub_player:
     			subPlayer();
     			break;
     		case R.id.next:
     			nextPlayer();
     			break;
     		case R.id.prev:
     			prevPlayer();
     			break;
     	}
     	   return true;
     }
 
 	private void subPlayer() {
 		if(!(players.size() < 2)) {
 			players.remove((int) id);
 			if(!(id == 0)) {
 				id = id - 1;
 			} else {
 				id = players.size() - 1;
 			}
 			getPlayer(id);
 		}
 	}
 
 	public void addPlayer(String name) {
 		players.add(new Player(name, 0));
 		savePlayer();
 		getPlayer((players.size() - 1));
 	}
 	
 	public void prevPlayer() {
 		if(id == 0) {
 			savePlayer();
 			getPlayer(players.size() - 1);
 		} else {
 			savePlayer();
 			getPlayer(id - 1);
 		}
 	}
 
 	public void nextPlayer() {
 		if(players.size() == (id + 1)) {
 			savePlayer();
 			getPlayer(0);
 		} else {
 			savePlayer();
 			getPlayer(id + 1);
 		}
 	}
 
 	public void getPlayer(int player_id) {
 		// Get new stuff
 		name = players.get(player_id).name;
 		score = players.get(player_id).score;
 		id = player_id;
 		
 		// display stuff
 		current_score.setText(score.toString());
 		player.setText(name);
 	}
 	
 	public void savePlayer() {
 		// Save current stuff
 		players.get(id).name = name;
 		players.get(id).score = score;
 	}
 
 	public void setScore(String s) {
     	if(s.equalsIgnoreCase("")) {
     		Toast toast = Toast.makeText(getApplicationContext(), "Empty values are not allowed.",Toast.LENGTH_SHORT);
 			toast.setGravity(Gravity.BOTTOM, 0, 100);
 			toast.show();
 			createDialog(score.toString(),findViewById(R.id.score));
     	} else {
     		try {
     			int value = Integer.parseInt(s);
     			score = value;
     			current_score.setText((CharSequence) score.toString());
     			players.get(id).score = value;
     		} catch(NumberFormatException nfe) {
     			Log.d("setScore","Could not parse " + nfe);
     			Toast toast = Toast.makeText(getApplicationContext(), "Must be an integer.",Toast.LENGTH_SHORT);
     			toast.setGravity(Gravity.BOTTOM, 0, 100);
     			toast.show();
     			createDialog(score.toString(),findViewById(R.id.score));
     		}
     	}
     }
     
     public void setButton(String s, Button pos, Button neg, Integer set) {
     	try {
 			int value = Integer.parseInt(s);
 			if (!(value == 0)) {
 				value = Math.abs(value);
 				pos.setText("+" + ((Integer) value).toString());
 				neg.setText("-" + ((Integer) value).toString());
 	    		switch(set) {
 	    		case 1:
 	    			set1 = value;
 	    			break;
 	    		case 2:
 	    			set2 = value;
 	    			break;
 	    		case 3:
 	    			set3 = value;
 	    			break;
 	    		}
 				pref.edit().putInt(set.toString(), value).commit();
 			} else {
 				Toast toast = Toast.makeText(getApplicationContext(), "0 is not allowed.",Toast.LENGTH_SHORT);
 				toast.setGravity(Gravity.BOTTOM, 0, 100);
 				toast.show();
 				View v;
 				switch(set) {
 	    		case 1:
 	    			v = findViewById(R.id.min1);
 	    			s = "1";
 	    			break;
 	    		case 2:
 	    			v = findViewById(R.id.min2);
 	    			s = "5";
 	    			break;
 	    		default:
 	    			v = findViewById(R.id.min3);
 	    			s = "10";
 	    			break;
 	    		}
 				createDialog(s,v);
 			}
 		} catch(NumberFormatException nfe) {
 			Log.d("setButton", "Could not parse " + nfe);
 			Toast toast = Toast.makeText(getApplicationContext(), "Must be an integer.",Toast.LENGTH_SHORT);
 			toast.setGravity(Gravity.BOTTOM, 0, 100);
 			toast.show();
 			View v;
 			switch(set) {
 				case 1:
 					v = findViewById(R.id.min1);
 					break;
 				case 2:
 					v = findViewById(R.id.min2);
 					break;
 				default:
 					v = findViewById(R.id.min3);
 					break;
 			}
 			createDialog(s,v);
 		}
     }
     
     public void setPlayer(String p) {
     	
     	if((!p.contains(separater)) && 
 				(!p.contains(Player.separater)) &&
 				(!p.equals(""))) {
     		player.setText((CharSequence) p);
         	name = p;
         	players.get(id).name = p;
 		} else {
 			Toast toast = Toast.makeText(getApplicationContext(), "Empty names and the characters " +
 					separater + " and " + Player.separater + " are not allowed.",Toast.LENGTH_LONG);
 			toast.setGravity(Gravity.BOTTOM, 0, 100);
 			toast.show();
 			createDialog(p,findViewById(R.id.player));
 		}
     }
     
     public void setSets() {
     	set1 = pref.getInt("set1", 1);
         set2 = pref.getInt("set2", 5);
         set3 = pref.getInt("set3", 10);
         plus1.setText("+" + set1.toString());
         plus2.setText("+" + set2.toString());
         plus3.setText("+" + set3.toString());
         min1.setText("-" + set1.toString());
         min2.setText("-" + set2.toString());
         min3.setText("-" + set3.toString());
     }
     
     static public void save() {
     	
     	Log.d("Trying to save","");
     	
     	String pstring = savePlayers();
     	
     	Editor prefedit = pref.edit();
     	prefedit.putString("players", pstring);
 		prefedit.putInt("set1", set1);
 		prefedit.putInt("set2", set2);
 		prefedit.putInt("set3", set3);
 		prefedit.putString("background",background);
 		prefedit.putInt("current_player", id);
 		prefedit.commit();
 		
     }
     
     private static String savePlayers() {
 //    	players.get(id).name = name;
 //    	players.get(id).score = score;
 
     	String acc = "";
     	for(int index = 0; index < players.size(); index++) {
         	acc = acc + players.get(index).serialize() + separater;
     	}
     	return acc;
 	}
 	
 	@Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         super.onActivityResult(requestCode, resultCode, data);
         
        final Integer i = data.getExtras().getInt("playerid");
         
         if(resultCode==RESULT_OK) {            
             
         	handler.post(new Runnable() {
             	@Override
             	public void run() {
             		getPlayer(i);
             	}
             });
                       
         } else {
             Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG).show();
         }
     }
 
 }
