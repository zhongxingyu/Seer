 //git repository: "git@github.com:codonnell/dnd-hp-tracker.git"
 package com.admteal.dndhp;
 
 import com.admteal.dndhp.R;
 import android.app.Activity;
 import android.graphics.Color;
 import android.view.Gravity;
 import android.view.View;
 import android.widget.*;	
 import android.os.Bundle;
 		
 public class DNDHPActivity extends Activity {
 	public Player player;
 	
 	//Some variables for cleanliness's sake
 	public final String PLUS = "+";
 	public final String MINUS = "-";
 	public final String BLANK = "";
 	public final String COLON_SPACE = ": ";
 	
 	public int currentEntry;
 	
 	//Create the calculator function buttons
 	public Button inputAdd, inputSub, inputClear, inputHS;
 
 	//Create the ongoing function buttons
 	public Button ongoAdd, ongoSub, inputOngo, inputDS;
 
 	//Create the surges function buttons
 	public Button surgesAdd, surgesSub, inputSurges;
 
 	//Create the Death Saves function buttons
 	public Button DSAdd, DSSub;
 	
 	//public Button inputDS	= (Button) findViewById(R.id.inputDS); //This button does nothing right now
 	
 	public TextView currentEntryView, currentHPView;
 	public LinearLayout showWorkLayout;
	public ScrollView showWorkScroller;
 	
     //Called when the activity is first created.
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);        
         setContentView(R.layout.main);
         
     	//Create the new default player
     	if (savedInstanceState == null) {
     		player = new Player();
     	}
     	
         //Set the calculator number buttons and their onClickListeners.  Creates the 10 buttons dynamically
         for (int i = 0; i < 10; i++) {
         	String buttonID = "input" + Integer.toString(i);
         	int resourceID = getResources().getIdentifier(buttonID, "id", "com.admteal.dndhp");
             Button b = (Button) findViewById(resourceID);
             final int j = i; //allows passing i into a new View.OnClickListener's onClick
             b.setOnClickListener(new View.OnClickListener() {
 				public void onClick(View v) {
 					currentEntryViewUpdater(j);					
 				}
 			});
         }
 
     	//Create the calculator function buttons
     	inputAdd	= (Button) findViewById(R.id.inputAdd);
     	inputAdd.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				showWorkUpdater(currentEntry);
 			}
 		});
     	inputSub	= (Button) findViewById(R.id.inputSub);
     	inputSub.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				showWorkUpdater(-currentEntry);
 			}
 		});
     	inputClear	= (Button) findViewById(R.id.inputClear);
     	inputClear.setOnLongClickListener(new View.OnLongClickListener() {			
 			public boolean onLongClick(View v) {	
 				clearEntry();
 				showWorkLayout.removeAllViews();				
 				return true; //stops click event from also being processed
 			}
 		});
     	inputClear.setOnClickListener(new View.OnClickListener() {
 
 			public void onClick(View v) {	
 				clearEntry();
 			}
 		});
     	inputHS		= (Button) findViewById(R.id.inputHS);
     	inputHS.setOnClickListener(new View.OnClickListener() {			
 			public void onClick(View v) {
 				showWorkUpdater(player.getHS());
 			}
 		});
     	// Long click to set currentEntry into Healing surge value, and display it on the button
     	inputHS.setOnLongClickListener(new View.OnLongClickListener() {
 			public boolean onLongClick(View v) {
 				player.setHS(currentEntry);
 				inputHS.setText(getResources().getString(R.string.hs) + COLON_SPACE 
 						+ Integer.toString(currentEntry));
 				clearEntry();
 				return true;
 			}
 		});
 
     	//Create the ongoing function buttons
     	ongoAdd	= (Button) findViewById(R.id.ongoAdd);
     	ongoAdd.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				ongoUpdater(PLUS);
 			}
 		});
     	ongoSub	= (Button) findViewById(R.id.ongoSub);
     	ongoSub.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				ongoUpdater(MINUS);
 			}
 		});
     	inputOngo	= (Button) findViewById(R.id.inputOngo);
     	inputOngo.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				showWorkUpdater(-player.getOngo()); //current ongo stores the number backwards, so it is inverted first
 			}
 		});
 
     	//Create the surges function buttons
     	surgesAdd	= (Button) findViewById(R.id.surgesAdd);
     	surgesAdd.setOnClickListener (new View.OnClickListener() {			
 			public void onClick(View v) {
 				surgesUpdater(PLUS);
 			}
 		});
     	surgesSub	= (Button) findViewById(R.id.surgesSub);
     	surgesSub.setOnClickListener (new View.OnClickListener() {			
 			public void onClick(View v) {
 				surgesUpdater(MINUS);
 			}
 		});
     	inputSurges = (Button) findViewById(R.id.inputSurges);
     	inputSurges.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				if (player.getSurges() == 0) {
 					return;
 				}
 				showWorkUpdater(player.getHS());
 				surgesUpdater(MINUS);
 			}
 		});
 
     	//Create the Death Saves function buttons
     	DSAdd		= (Button) findViewById(R.id.DSAdd);
     	DSAdd.setOnClickListener (new View.OnClickListener() {			
 			public void onClick(View v) {
 				DSUpdater(PLUS);
 			}
 		});
     	DSSub		= (Button) findViewById(R.id.DSSub);
     	DSSub.setOnClickListener (new View.OnClickListener() {			
 			public void onClick(View v) {
 				DSUpdater(MINUS);
 			}
 		});
     	
     	inputDS		= (Button) findViewById(R.id.inputDS);
     	
     	currentEntryView= (TextView) findViewById(R.id.currentEntryView);
    	showWorkScroller= (ScrollView) findViewById(R.id.showWorkScroller);
     	showWorkLayout	= (LinearLayout) findViewById(R.id.showWorkLayout);
     	currentHPView	= (TextView) findViewById(R.id.currentHPView); 
     }
     
     //Refills UI elements after the activity has been killed for some reason, e.g., orientation change
     @Override
     protected void onResume() {
         super.onResume();
     	currentEntryView.setText(Integer.toString(currentEntry));
     	currentHPView.setText(Integer.toString(player.getHP()));
     	inputSurges.setText(getResources().getString(R.string.surges) + COLON_SPACE 
     			+ Integer.toString(player.getSurges()));
     	ongoUpdater(BLANK);
     	DSUpdater(BLANK);
     	surgesUpdater(BLANK);
 		inputHS.setText(getResources().getString(R.string.hs) + COLON_SPACE 
 				+ Integer.toString(player.getHS()));
 		
 		//Because of the way they're created, player.changeHistory and player.HPHistory are always the same length
 		for (int i = 0; i < player.getChangeHistory().size(); i++) {
 			showWorkViewMaker(player.getChangeHistory().get(i),player.getHPHistory().get(i));
 		}
     }
     
     @Override
     public void onSaveInstanceState(Bundle savedInstanceState) {
         super.onSaveInstanceState(savedInstanceState);
     	savedInstanceState.putInt("currentEntry", currentEntry);
     	savedInstanceState.putSerializable("Player", player);
     }
     
     @Override
     public void onRestoreInstanceState(Bundle savedInstanceState) {
     	super.onRestoreInstanceState(savedInstanceState);
     	currentEntry = savedInstanceState.getInt("currentEntry");
     	player = (Player) savedInstanceState.getSerializable("Player");
     }
     
     //Method injures or heals the player based on input, then calls for the view maker to show work
     public void showWorkUpdater(int value) {
     	//First we must pick our operation.
     	if (value > 0) {
         	player.heal(value);
     	} else if (value < 0) {
         	player.injure(-value);
     	}
     	showWorkViewMaker(value);
     	clearEntry();
     	
     	currentHPView.setText(Integer.toString(player.getHP()));
     }
     
     //Use the player's HP if an hpToList was not specified.  Generally, this will be the case
     public void showWorkViewMaker(int value) {
     	showWorkViewMaker(value, player.getHP());
     }
     
     //Method which adds the red or green numbers and new current HP values to showWorkView
     public void showWorkViewMaker(int value, int hpToList) {
     	String operation = BLANK;
     	TextView adjustment = new TextView(this);   
     	TextView sum = new TextView(this); 	
     	if (value > 0) {
         	adjustment.setTextColor(Color.GREEN);
     	} else if (value < 0) {
         	adjustment.setTextColor(Color.RED);   
         	operation = BLANK; //if we do not clear operation at this point, an extra - will appear
     	}
     	//First line shows how much was added or subtracted as +n or -n
     	adjustment.setText(operation + Integer.toString(value));
     		adjustment.setGravity(Gravity.RIGHT);
     		adjustment.setTextSize(14 * getResources().getDisplayMetrics().density + 0.5f); //14 px converted to 16 dip
     	//Second line shows new total number
     	sum.setText(Integer.toString(hpToList));
     		sum.setGravity(Gravity.LEFT);
     		sum.setTextSize(14 * getResources().getDisplayMetrics().density + 0.5f);  //14 px converted to 16 dip
     	//Now commit those lines to the view
     	showWorkLayout.addView(adjustment);
     	showWorkLayout.addView(sum);
     }
     
     //Controls adding and removing death saves to player and updating the button's text to reflect changes
     public void DSUpdater(String how) {
     	if (how.equals(PLUS)) {
     		player.addDeathSave();
     	} else if (how.equals(MINUS)) {
     		player.remDeathSave();
     	}
     	inputDS.setText(getResources().getString(R.string.ds) + COLON_SPACE
     			+ Integer.toString(player.getDeathSaves()));
     }
     
     //Controls adding and removing healing surges to player and updating the button's text to reflect changes
     public void surgesUpdater(String how) {
     	if (how.equals(PLUS)) {
     		player.addSurge();
     	} else if (how.equals(MINUS)) {
     		player.remSurge();
     	}
     	inputSurges.setText(getResources().getString(R.string.surges) + COLON_SPACE 
     			+ Integer.toString(player.getSurges()));
     }
     
     //Controls adding and removing ongoing damage to the player and updating the button's text to reflect changes
     //TODO: Is UX here best?  Turning to regen in negative numbers may be confusing.
     public void ongoUpdater(String how) {
     	String dotOrHot, valueToUse;
     	//Pick operation and adjust currentOngo number
     	if (how.equals(PLUS)) {
     		player.addOngo();
     	} else if (how.equals(MINUS)) {
     		player.remOngo();
     	}
     	//It's a regen if it is under 0, otherwise it is ongoing
     	if (player.getOngo() < 0) {
     		dotOrHot = getResources().getString(R.string.regen);
     		valueToUse = Integer.toString(player.getRegen());
     	} else {
     		dotOrHot = getResources().getString(R.string.ongoing);
     		valueToUse = Integer.toString(player.getOngo());
     	}
     	//Change the button text to reflect variable
     	dotOrHot+= COLON_SPACE; //Add colon space to the end of our word
     	inputOngo.setText(dotOrHot + valueToUse);
     }
     
     //Displays the numbers that represent the current entry based on key presses.
     public void currentEntryViewUpdater(int updateWith) {
     	if (currentEntry * 10 > 999) {return;} //Max 3 digit number in currentEntry
     	currentEntry = (currentEntry * 10) + updateWith;
     	currentEntryView.setText(Integer.toString(currentEntry));
     }
     
     //Zeroes the currentEntry
     public void clearEntry() {
 		currentEntry = 0;
 		currentEntryView.setText(Integer.toString(currentEntry));
     }
 }
