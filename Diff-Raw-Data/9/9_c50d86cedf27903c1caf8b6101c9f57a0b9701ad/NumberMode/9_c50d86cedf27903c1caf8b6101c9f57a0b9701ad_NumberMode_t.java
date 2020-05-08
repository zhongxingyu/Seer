 package cs554.proj.slidingtiles;
 
 import java.util.Random;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.NavUtils;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewStub;
 import android.widget.Button;
 import android.widget.TextView;
 
 /**
  * NumberMode implements the support functions needed for the number mode version
  * of the sliding grid game.
  * 
  * @author me
  *
  */
 public class NumberMode extends SlidingGrid {
 	/**
 	 * Stores whether or not the user has won the game yet.
 	 * Once the game has been won, the tile buttons are
 	 * disabled so no more moves can be made.
 	 */
 	private boolean won = false;
 	
 	/**
 	 * Stores whether or not the user is playing against an AI
 	 */
 	private boolean aiEnabled = false;
 	
 	/**
 	 * Stores the difficulty level of the AI
 	 */
 	private int aiDifficulty = 1;
 	
 	/**
 	 * The AI's grid
 	 */
 	private Grid aiGrid;
 
     /** 
      * Get the user options from the setup page and generate the game type requested
      * by the user.
      * 
      * @see cs554.proj.slidingtiles.SlidingGrid#onCreate(android.os.Bundle, int)
      */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         Intent intent = getIntent();
         int gridSize = intent.getIntExtra(SetupNumberGame.GRID_SIZE, 5);
         super.onCreate(savedInstanceState, gridSize);
         
         // Generate a winning grid
         generateValidGrid(gridSize);
         
         // Scramble the grid to create a new game for the user. 
         // Having the number of moves made be the square of the number of tiles
         // seems to be sufficient to generate a game.
         if(gridSize == 2)
         	userGrid.scrambleGrid(9);
         else if(gridSize == 3)
         	userGrid.scrambleGrid(64);
         else if(gridSize == 4)
         	userGrid.scrambleGrid(225);
         else
         	userGrid.scrambleGrid(576);
 
         
         // Get the AI settings
         aiEnabled = intent.getBooleanExtra(SetupNumberGame.AI_ENABLED, false);
         if(aiEnabled) {
         	// Show AI grid
         	((ViewStub) findViewById(R.id.ai_stub)).setVisibility(View.VISIBLE);
 
         	// Set up the AI grid with the button IDs and the Button objects from
         	// the UI
         	int[][] ids = {{R.id.gbAI11, R.id.gbAI12, R.id.gbAI13, R.id.gbAI14, R.id.gbAI15},{R.id.gbAI21, R.id.gbAI22, R.id.gbAI23, R.id.gbAI24, R.id.gbAI25},{R.id.gbAI31, R.id.gbAI32, R.id.gbAI33, R.id.gbAI34, R.id.gbAI35},{R.id.gbAI41, R.id.gbAI42, R.id.gbAI43, R.id.gbAI44, R.id.gbAI45},{R.id.gbAI51, R.id.gbAI52, R.id.gbAI53, R.id.gbAI54, R.id.gbAI55}};
         	Button[][] buttons = new Button[maxSize][maxSize];
         	for(int i = 0; i < maxSize; i++)
         		for(int j = 0; j < maxSize; j++)
         			buttons[i][j] = (Button) findViewById(ids[i][j]);
         	aiGrid = new Grid(gridSize, ids, buttons);
         	
         	// Copy the user grid so both grids start with the same layout
         	aiGrid.copyGrid(userGrid);
         	
         	// Start the AI
         	(new Thread(new NumberModeAI(aiGrid, this, aiDifficulty))).start();
         }
         
         // Get the difficulty level for the AI
         aiDifficulty = intent.getIntExtra(SetupNumberGame.AI_DIFFICULTY, 1);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_number_mode, menu);
         return true;
     }
 
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case android.R.id.home:
                 NavUtils.navigateUpFromSameTask(this);
                 return true;
         }
         return super.onOptionsItemSelected(item);
     }
     
     /**
      * Generates a winning grid for the number mode game.
      * The blank tile will either be in the top left or bottom right location.
      * The remaining tiles will be numbered 1 to N where N=gridSize*gridSize-1.
      * 
      * @param gridSize Size of the grid we're numbering
      */
     private void generateValidGrid(int gridSize) {
     	// Decide at random which button to hide
     	Random gen = new Random();
     	int hb = gen.nextInt(2);
     	int count;
     	if(hb == 0) {
     		// Hide the top left. Start the count at 0 so the first visible button
     		// we label is 1.
     		userGrid.hideButton(1,1);
     		count = 0;
     	} else {
     		// Hide the bottom right. Start the count at 1 since the first button is
     		// visible. The hidden tile will be labeled 25.
     		userGrid.hideButton(gridSize, gridSize);
     		count = 1;
     	}
     	
     	// Label each of the buttons, incrementing count by 1 after each labeling
     	for(int i = 1; i <= gridSize; i++) {
     		for(int j = 1; j <= gridSize; j++) {
     			userGrid.setButtonText(i, j, Integer.toString(count));
     			count++;
     		}
     	}
     }
     
     /**
      * Checks whether or not the user has won the game
      * 
      * @return True if grid is in a valid layout
      */
     public boolean checkForWin(Grid grid) {
     	// Get the location of the hidden button
     	int hbLoc[] = grid.getHiddenButtonLocation();
     	int count;
     	
     	// Check where the hidden button is
     	if((hbLoc[0] == 1) && (hbLoc[1] == 1)) {
     		// The hidden button is in the top left corner
     		// We'll start counting on the second tile so 
     		// set count to 0 so we ignore the first tile
     		count = 0;
     	} else if((hbLoc[0] == grid.getGridSize()) && (hbLoc[1] == grid.getGridSize())) {
     		// The hidden button is in the bottom right corner
     		// Start counting on 1 for the first tile
     		count = 1;
     	} else {
     		// The hidden button is not at one of the valid locations
     		// for winning the game so we know the user hasn't won
     		return false;
     	}
     	
     	// Check the grid and make sure the correct number is on each tile
     	for(int i = 1; i <= grid.getGridSize(); i++) {
     		for(int j = 1; j <= grid.getGridSize(); j++) {
     			if((count != 0) && (count != grid.getGridSize()*grid.getGridSize())) {
    				if(!grid.getButtonText(i,j).equals(Integer.toString(count))) {
     					// Wrong number is on the tile, no win
     					return false;
     				}
     			}
     			count++;
     		}
     	}
     	
     	// Winn!
     	return true;
     }
     
     /**
      * Overrides the super class's processing function for button presses. 
      * We want to check and see after the button press if the grid is in a winning
      * position
      * 
      * @see cs554.proj.slidingtiles.SlidingGrid#processButtonPress(android.view.View)
      */
     public void processButtonPress(View view) {
     	// If the user has already won, ignore button presses so the grid stays in
     	// the winning layout
     	if(getWon() == true)
     		return;
     	
     	// Call super function to process move
     	super.processButtonPress(view);
     	
     	// Check if the user has won or not
     	if(checkForWin(userGrid)) {
     		// User won! Add text to screen saying they won and set won to true so
     		// future button presses on the grid are ignored
     		TextView tv = (TextView) findViewById(R.id.userTextArea);
     		tv.setText("Winner!");
     		setWon(true);
     	}
     }
     
     /**
      * Synchronize access to game state across threads
      * 
      * @return Whether the game has been won or not
      */
     public synchronized boolean getWon() {
     	return won;
     }
     
     /**
      * Synchronize setting the game state across threads
      * 
      * @param b True if the game has been won, false otherwise
      */
     public synchronized void setWon(boolean b) {
     	won = b;
     }
 }
