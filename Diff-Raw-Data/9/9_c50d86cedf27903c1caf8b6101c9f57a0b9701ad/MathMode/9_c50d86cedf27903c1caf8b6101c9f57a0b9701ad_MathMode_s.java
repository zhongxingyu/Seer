 package cs554.proj.slidingtiles;
 
 import java.util.ArrayList;
 import java.util.Random;
 
 import android.graphics.Rect;
 import android.os.Bundle;
 import android.support.v4.app.NavUtils;
 import android.view.GestureDetector;
 import android.view.GestureDetector.SimpleOnGestureListener;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.widget.Button;
 import android.widget.TextView;
 
 /**
  * @author me
  *
  * The MathMode class generates a valid game layout for the math mode game for
  * sliding tiles. This class contains all of the support functions needed for this
  * mode.
  */
 public class MathMode extends SlidingGrid {
 
 	/**
 	 * Gesture detector for swipes across the user grid
 	 */
 	private GestureDetector gDetector;
 	
 	private int points = 0;
 	private ArrayList<String> formattedEquations;
 	
     /**
      * Generate a valid grid for the math mode game
      * 
      * @see cs554.proj.slidingtiles.SlidingGrid#onCreate(android.os.Bundle)
      */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState, 5);
         // Generate a valid game layout and then scramble the grid so the
         // user can play.
         generateGame();
         userGrid.scrambleGrid(25);
         
         // Initialize formattedEquations
         formattedEquations = new ArrayList<String>();
         
         // Create the gesture detector for swipes across the user grid
         gDetector = new GestureDetector(this.getApplicationContext(), new GesturesForMathMode());
         
         // Tell the linear layout containing the user grid to pass swipe events to our
         // gesture detector algorithm
         View v = findViewById(R.id.llForPlayingGame);
         v.setOnTouchListener(new View.OnTouchListener() {
 			
 			public boolean onTouch(View v, MotionEvent event) {
 				if(gDetector.onTouchEvent(event))
 					return true;
 				return false;
 			}
 		});
         
         // Have each button pass the motion event to the gesture detector to see if
         // it was a swipe that just started on a button. Otherwise process button
         // press
         for(int i = 1; i <= userGrid.getGridSize(); i++) {
         	for(int j = 1; j <= userGrid.getGridSize(); j++) {
                 Button b = (Button) findViewById(userGrid.getButtonID(i, j));
                 b.setOnTouchListener(new View.OnTouchListener() {
                 	public boolean onTouch(View v, MotionEvent event) {
                 		gDetector.onTouchEvent(event);
                 		return false;
                 	}
                 });
         	}
         }
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_math_mode, menu);
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
      * This function fills out a row in the grid
      * 
      * @param index Which row to fill out (1 to gridSize)
      * @param v1 The value for the first tile in the row
      * @param v2 The value for the second tile in the row
      * @param v3 The value for the third tile in the row
      * @param v4 The value for the fourth tile in the row
      * @param v5 The value for the fifth tile in the row
      */
     private void setRow(int index, String v1, String v2, String v3, String v4, String v5) {
     	userGrid.setButtonText(index, 1, v1);
     	userGrid.setButtonText(index, 2, v2);
     	userGrid.setButtonText(index, 3, v3);
     	userGrid.setButtonText(index, 4, v4);
     	userGrid.setButtonText(index, 5, v5);
     }
     
     /**
      * The function fills out a column in the grid.
      * 
      * @param index Which column to fill out (1 to gridSize)
      * @param v1 The value for the first tile in the column
      * @param v2 Value for the second tile
      * @param v3 Value for the third tile
      * @param v4 Value for the fourth tile
      * @param v5 Value for the fifth tile
      */
     private void setCol(int index, String v1, String v2, String v3, String v4, String v5) {
     	userGrid.setButtonText(1, index, v1);
     	userGrid.setButtonText(2, index, v2);
     	userGrid.setButtonText(3, index, v3);
     	userGrid.setButtonText(4, index, v4);
     	userGrid.setButtonText(5, index, v5);
     }
     
     
     /**
      * Randomly chooses a tile value from the set of valid values.
      * 
      * @return Text for the tile
      */
     private String generateTile() {
     	Random gen = new Random();
     	int val = gen.nextInt(14);
     	if(val < 10)
     		return Integer.toString(val);
     	else if(val == 10)
     		return "+";
     	else if(val == 11)
     		return "-";
     	else if(val == 12)
     		return "x";
     	else
     		return "=";
     }
     
     /**
      * Generates a valid game layout with one valid equation in either
      * a row or column. The direction of the equation and it's values
      * are chosen uniformly at random.
      */
     private void generateGame() {
     	Random gen = new Random();
     	// Whether we go across a row or down a column
     	int dir = gen.nextInt(2);
     	// Which row or column we'll use
     	int index = gen.nextInt(userGrid.getGridSize())+1;
     	// Will the equals sign be on the second or fourth tile
     	int eqLoc = gen.nextInt(2);
     	// The first number used
     	int i = gen.nextInt(10);
     	// The second number used
     	int j = gen.nextInt(10);
     	// Which operator we'll use
     	String op;
     	// Operation result
     	int k;
     	
     	// Choose which operator we'll use and calculate the value for the equation
     	if(i*j < 10) {
     		op = "x";
     		k = i*j;
     	} else if(i+j < 10) {
     		op = "+";
     		k = i+j;
     	} else {
     		op = "-";
     		if(i-j < 0) {
     			int temp = i;
     			i = j;
     			j = temp;
     		}
     		k = i-j;
     	}
     	
     	// Enter the equation in the grid
     	if(dir == 0) {
     		// We're using a row. Fill in the equation based on location of the equals sign
     		if(eqLoc == 0)
     			setRow(index, Integer.toString(i), op, Integer.toString(j), "=", Integer.toString(k));
     		else
     			setRow(index, Integer.toString(k), "=", Integer.toString(i), op, Integer.toString(j));
     		//Fill out the rest of the grid
     		for(int x = 1; x <= userGrid.getGridSize(); x++) {
     			if(x == index)
     				continue;
     			setRow(x, generateTile(), generateTile(), generateTile(), generateTile(), generateTile());
     		}
     		// Choose which button to hide
     		int hr = gen.nextInt(userGrid.getGridSize()-1)+1;
     		if(hr >= index)
     			hr++;
     		int hc = gen.nextInt(userGrid.getGridSize())+1;
     		userGrid.hideButton(hr, hc);
     	} else {
     		// We're filling out a column. Fill out equation based on location of equals sign
     		if(eqLoc == 0)
     			setCol(index, Integer.toString(i), op, Integer.toString(j), "=", Integer.toString(k));
     		else
     			setCol(index, Integer.toString(k), "=", Integer.toString(i), op, Integer.toString(j));
     		// Fill out the rest of the grid
     		for(int x = 1; x <= userGrid.getGridSize(); x++) {
     			if(x == index)
     				continue;
     			setCol(x, generateTile(), generateTile(), generateTile(), generateTile(), generateTile());
     		}
     		// Choose a button to hide
     		int hr = gen.nextInt(userGrid.getGridSize())+1;
     		int hc = gen.nextInt(userGrid.getGridSize()-1)+1;
     		if(hc >= index)
     			hc++;
     		userGrid.hideButton(hr, hc);
     	}
     }
     
     /**
      * Checks if the tiles swipped over form a valid equation
      * 
      * @param tileTexts The text on each tile in LR or TB order
      * @return True if equation is valid, false otherwise
      */
    private boolean validateEquation(String[] tileTexts) {
     	int v1 = -1, v2 = -1, v3 = -1;
     	
 		String equation = "";
 		int eqPoints = 0;
     	
     	try {
     		v1 = Integer.parseInt(tileTexts[0]);
     		v2 = Integer.parseInt(tileTexts[2]);
     		v3 = Integer.parseInt(tileTexts[4]);
     	} catch(NumberFormatException e) {
     		return false;
     	}
     	if((v1 == -1) || (v2 == -1) || (v3 == -1))
     		return false;
     	
     	if(tileTexts[1].equals("=")) {
     		if(tileTexts[3].equals("+") && (v1 == v2 + v3)) {
     			equation = Integer.toString(v2) + "+" + Integer.toString(v3) + "=" + Integer.toString(v1);
     			eqPoints = v1;
     		} else if(tileTexts[3].equals("-") && (v1 == v2 - v3)) {
     			equation = Integer.toString(v2) + "-" + Integer.toString(v3) + "=" + Integer.toString(v1);
     			eqPoints = v1;
     		} else if(tileTexts[3].equals("x") && (v1 == v2 * v3)) {
     			equation = Integer.toString(v2) + "x" + Integer.toString(v3) + "=" + Integer.toString(v1);
     			eqPoints = v1;
     		} else {
     			return false;
     		}
     	} else if(tileTexts[3].equals("=")) {
     		if(tileTexts[1].equals("+") && (v1 + v2 == v3)) {
     			equation = Integer.toString(v1) + "+" + Integer.toString(v2) + "=" + Integer.toString(v3);
     			eqPoints = v3;
     		} else if(tileTexts[1].equals("-") && (v1 - v2 == v3)) {
     			equation = Integer.toString(v1) + "-" + Integer.toString(v2) + "=" + Integer.toString(v3);
     			eqPoints = v3;
     		} else if(tileTexts[1].equals("x") && (v1 * v2 == v3)) {
     			equation = Integer.toString(v1) + "x" + Integer.toString(v2) + "=" + Integer.toString(v3);
     			eqPoints = v3;
     		} else {
     			return false;
     		}
     	} else {
     		return false;
     	}
     	
     	for(int i = 0; i < formattedEquations.size(); i++) {
     		if(formattedEquations.get(i).equals(equation)) {
     			return false;
     		}
     	}
     	
     	formattedEquations.add(equation);
     	points += eqPoints;
     	
     	return true;
     }
     
     /**
      * Extends necessary functions to detect swipes over user grid and process
      * the swipped equation
      * 
      * @author me
      *
      */
     class GesturesForMathMode extends SimpleOnGestureListener {
     	/**
     	 * Need to return true so swipe function is called
     	 * 
     	 * @param me The event to process
     	 * @return True
     	 */
     	@Override
     	public boolean onDown(MotionEvent me) {
     		return true;
     	}
     	
     	/**
     	 * Process the user's swipe to see if it was across a single row or column on
     	 * the grid. Swipes across multiple rows and columns are ignored. If it was a 
     	 * valid swipe, process the tiles swipped across.
     	 * 
     	 * @param start Where the swipe started
     	 * @param finish Where the swipe ended
     	 * @param xVelocity
     	 * @param yVelocity
     	 * @return True if this was a swipe across a single row or column, false otherwise
     	 */
     	@Override
     	public boolean onFling(MotionEvent start, MotionEvent finish, float xVelocity, float yVelocity) {
     		// Stores which row or column was swipped across
     		int row = -1;
     		int col = -1;
     		
     		// Starting coordinates of the swipe
     		float sx = start.getRawX();
     		float sy = start.getRawY();
     		
     		// Ending coordinates of the swipe 
     		float ex = finish.getRawX();
     		float ey = finish.getRawY();
     	
     		// Check and see if swipe was down a column
     		for(int i = 1; i <= userGrid.getGridSize(); i++) {
     			// Get the width of column i
     			Button b = (Button) findViewById(userGrid.getButtonID(1, i));
     			Rect r = new Rect();
     			b.getGlobalVisibleRect(r);
     			
     			// Check and see if swipe stayed within this column
     			if((r.left <= sx) && (sx <= r.right) && (r.left <= ex) && (ex <= r.right))
     				col = i;
     		}
     		
     		// Check if swipe was across a row
     		for(int i = 1; i <= userGrid.getGridSize(); i++) {
     			// Get the height of row i
     			Button b = (Button) findViewById(userGrid.getButtonID(i,1));
     			Rect r = new Rect();
     			b.getGlobalVisibleRect(r);
     			
     			// Check and see if swipe stayed within this row 
     			if((r.top <= sy) && (sy <= r.bottom) && (r.top <= ey) && (ey <= r.bottom))
     				row = i;
     		}
     		
     		// Make sure swipe stayed within a row or column
     		if((row == -1) && (col == -1))
     			return false;
     		
     		// Make sure hidden button isn't in swipe
     		int[] hbloc = userGrid.getHiddenButtonLocation();
     		if((hbloc[0] == row) || (hbloc[1] == col))
     			return true;
     		
     		// Get the text from the tiles swiped across
     		String[] tileTexts = new String[userGrid.getGridSize()];
     		for(int i = 1; i <= userGrid.getGridSize(); i++) {
     			if(row != -1)
     				tileTexts[i-1] = userGrid.getButtonText(row, i);
     			else
     				tileTexts[i-1] = userGrid.getButtonText(i, col);
     		}
     	
     		// Check if we have a valid equation
     		if(validateEquation(tileTexts)) {
     			// Add equation to screen
     			TextView tv = (TextView) findViewById(R.id.userTextArea);
     			String currentText = (String) tv.getText();
     			currentText += formattedEquations.get(formattedEquations.size()-1) + "\n";
     			tv.setText(currentText);
     		}
     		
     		return true;
     	}
     }
 
 }
