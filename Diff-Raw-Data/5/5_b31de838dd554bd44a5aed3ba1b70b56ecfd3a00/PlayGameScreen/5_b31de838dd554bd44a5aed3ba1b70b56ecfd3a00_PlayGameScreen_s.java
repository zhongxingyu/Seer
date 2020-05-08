 package uw.cse403.nonogramfun;
 
 
 /**
  * CSE 403 AA
  * Project Nonogram: Frontend
  * @author  Xiaoxia Jian, Huiqi Wang, Renhao Xie, Alan Loh
  * @version v1.0, University of Washington 
  * @since   Spring 2013 
  */
 
 
 import java.io.IOException;
 import java.net.UnknownHostException;
 
 import org.json.JSONException;
 
 import uw.cse403.nonogramfun.enums.Difficulty;
 import uw.cse403.nonogramfun.network.NonoClient;
 import uw.cse403.nonogramfun.nonogram.NonoPuzzle;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.os.Handler;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.view.animation.AlphaAnimation;
 import android.view.animation.Animation;
 import android.view.animation.Animation.AnimationListener;
 import android.view.animation.LinearInterpolator;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.HorizontalScrollView;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TextView;
 
 public class PlayGameScreen extends Activity implements OnClickListener{
 	private int dimension;         //dimension is size for the clickable cells
 	private Integer[][] gameArray; //store the gameArray get back from the server
 	private View[][] buttons;      //button arrays. first number is the row number, second number is column number
 	private String[] rowHint;
 	private String[] columnHint;
 	private TextView timedisplay;
 	long starttime; 
 	Handler timerHandle;
 	Runnable timerRun;
 	boolean stopTimer = false;
 	Difficulty puzzleDifficulty;
 	
 	// IMPORTANT: X and Y axis are FLIPPED in both gameArray and buttons[][].
 	// For debugging purpose, given buttons[x][y], x denotes the ROW NUMBER, y denotes the COLUMN number
 	// so x is the vertical axis, and y is the horizontal axis
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_play_game_screen);
 		
 		Bundle bundle = getIntent().getExtras();
 		dimension = bundle.getInt("size");
 		gameArray = new Integer[dimension][dimension];
 		
 		if(dimension == 5){
 			setTitle("Small (5x5)");
 		}else if(dimension == 10){
 			setTitle("Medium (10x10)");
 		}else if(dimension == 14){
 			setTitle("Large (14x14");
 		}
 		
 		fetchPuzzle();
 		parseGameRow();
 		parseGameColumn();
 		
 		TableLayout layout = new TableLayout (this);
 		layout.setLayoutParams( new TableLayout.LayoutParams());
 		//layout.setPadding(50,50,50,50);
 		
 		//timer 
 		starttime = System.currentTimeMillis();
 	    //this posts a message to the main thread from our timertask and updates the textfield
 	    timerHandle = new Handler();
 	    timerRun = new Runnable() {
 			@Override
 			public void run() {
 				timeHasPassed();
 			}
 	    };
	   	timedisplay = new TextView(this);
 	   	timedisplay.setText("0:00");
 	   	timedisplay.setTextSize(30);
 	   	timedisplay.setTextColor(Color.BLUE);
		layout.addView(timedisplay);
 		
 		// dimension + 1 for the number field at the top and left sides
 		buttons = new View[dimension + 1][dimension + 1];
 		layout = createGameTable(layout);
 
 		HorizontalScrollView scrollView = (HorizontalScrollView) findViewById(R.id.nonogram_gameboard);
 		scrollView.addView(layout);	
 	
 		Button hintButton = (Button) findViewById(R.id.playgamehint);
 		hintButton.setOnClickListener(new HintButtonListener()); 
 		
 		Button submitButton = (Button) findViewById(R.id.playgamesubmit);
 		
 		submitButton.setOnClickListener(new SubmitButtonListener()); 
 		
 //		editText = (EditText)findViewById(R.id.search);
 //		editText.setVisibility(View.INVISIBLE);
 		
 		timerRun.run();
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.play_game_screen, menu);
 		return true;
 	}
 	
 	//pull a puzzle from the server database
 	private void fetchPuzzle(){
 		Thread thread = new Thread(new Runnable(){
 			@Override
 			public void run() {
 				
 				try {
 					if (dimension == 5){
 						puzzleDifficulty = Difficulty.EASY;
 					} else if (dimension == 10) {
 						puzzleDifficulty = Difficulty.MEDIUM;
 					} else if (dimension == 14) {
 						puzzleDifficulty = Difficulty.HARD;
 					} else {
 						puzzleDifficulty = Difficulty.UNDEFINED;
 					}
 					
 					NonoPuzzle puzzle = NonoClient.getPuzzle(puzzleDifficulty);
 					for(int i = 0; i < puzzle.getNonoPicColSize(); i++){
 						for(int j = 0; j < puzzle.getNonoPicRowSize(); j++){
 							Log.i("gameArrayLength", String.valueOf(gameArray[i].length));
 							gameArray[i][j] = puzzle.getColor(i, j);
 						}
 					}	
 				} catch (UnknownHostException e) {
 					
 				} catch (IOException e) {
 					
 				} catch (JSONException e) {
 					
 				} catch (Exception e) {
 
 				}
 
 			}
 		});
 		thread.start();
 		try {
 			thread.join();
 		} catch (InterruptedException e) {
 			
 		}
 	}
 	
 	private void parseGameRow(){
 		rowHint = new String[dimension];
 		for(int x = 0; x < dimension; x++){
 			boolean emptyCell = false, start = true;
 			rowHint[x] = "";
 			int count = 0;
 			for(int y = 0; y < dimension; y++){
 				// If the game cell is filled in...
 				Log.i("PlayAGameScreen",gameArray[x][y].toString());
 				if (gameArray[x][y].equals(Color.BLACK)){
 					emptyCell = false;
 					count++;
 					start = false;
 					//if already at the last cell of the row store the count anyway
 					if (y == dimension - 1){
 						rowHint[x] += count;
 					}
 					
 				// If the game cell is not filled in...
 				} else {
 					// If we reached the end of a set of filled cells and 
 					// it's not the first cell in the row...
 					if (emptyCell == false && start == false){
 						rowHint[x] += count + " ";
 						count = 0;
 						emptyCell = true;
 					}
 					// Otherwise we do nothing
 				}
 			}
 		}
 	}
 	
 	private void parseGameColumn(){
 		columnHint = new String[dimension];
 		for(int y = 0; y < dimension; y++){
 			boolean emptyCell = false, start = true;
 			columnHint[y] = "";
 			int count = 0;
 			for(int x = 0; x < dimension; x++){
 				// If the game cell is filled in...
 				if (gameArray[x][y].equals(Color.BLACK)){
 					emptyCell = false;
 					count++;
 					start = false;
 					//if already at the last cell of the row store the count anyway
 					if (x == dimension - 1){
 						columnHint[y] += count;
 					}
 				// If the game cell is not filled in...
 				} else {
 					// If we reached the end of a set of filled cells and 
 					// it's not the first cell in the column...
 					if (emptyCell == false && start == false){
 						columnHint[y] += count + "\n";
 						count = 0;
 						emptyCell = true;
 					}
 					// Otherwise we do nothing
 				}
 			}
 		}
 	}
 	
 	
 	private TableLayout createGameTable(TableLayout layout) {
 		//create the empty game board with the number fields
 		for (int i = 0; i < dimension + 1; i++) {
 			TableRow tr = new TableRow(this);
 			for (int j = 0; j < dimension + 1; j++) {
 				
 				if (i == 0 || j == 0) {
 					buttons[i][j] = new TextView(this);
 				} else {
 					buttons[i][j] = new Cell(this);
 				}
 				
 				if (i == 0 && j == 0) {
 					TextView textview = (TextView) buttons[i][j];
 					textview.setBackgroundColor(Color.TRANSPARENT);
 					tr.addView(buttons[i][j],50,50);
 				} else if(j == 0) {
 					// horizontal number field
 					TextView textview = (TextView) buttons[i][j];
 		        	textview.setBackgroundColor(Color.TRANSPARENT);
 		        	textview.setText(rowHint[i-1]);
 		        	textview.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
 		        	tr.addView(buttons[i][j],150,50);
 				} else if(i == 0) {
 					// vertical number field
 					TextView textview = (TextView) buttons[i][j];
 					textview.setBackgroundColor(Color.TRANSPARENT);
 					textview.setText(columnHint[j-1]);
 					textview.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
 					textview.setLayoutParams(new ViewGroup.LayoutParams(-1, -2));
 					tr.addView(buttons[i][j],50,200);
 				} else {
 					Cell c = (Cell) buttons[i][j];
 					c.setText(Integer.toString(i)+Integer.toString(j));
 		        	if((i % 2 == j % 2)){
 		        		c.setOriginColor(Color.LTGRAY);
 		        		c.setColor(Color.LTGRAY);
 		        		//c.setTextColor(Color.LTGRAY);
 		        	}
 		        	else{
 		        		c.setOriginColor(Color.WHITE);
 		        		c.setColor(Color.WHITE);
 		        		//c.setTextColor(Color.WHITE);
 		        	}
 		        	
 		        	c.setOnClickListener(this);
 		        	tr.addView(buttons[i][j],50,50);
 				}
 				
 			}
 			layout.addView(tr);
 		}
 		
 		return layout;
 		
 	}
 	
 	
 	@Override
 	public void onClick(View view) {
 		Cell cell = (Cell)view;
 		cell.setState();
 		cell.setStateColor();
 	}
 	
 	//helper function to calculate the time has passed
 	public void timeHasPassed(){
         long millis = System.currentTimeMillis() - starttime;
         int seconds = (int) (millis / 1000);
         int minutes = seconds / 60;
         seconds     = seconds % 60;
         timedisplay.setText(String.format("%d:%02d", minutes, seconds));
         if (!stopTimer)
         	timerHandle.postDelayed(timerRun, 1000);
 	}
 	
 	
 	public void returnMainScreen(View view) {
 		Intent i = new Intent(this, MainActivity.class);
 		i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 		startActivity(i);
 	}
 	
 	
 	private class SubmitButtonListener implements OnClickListener {
 		@Override
 		public void onClick(View v) {
 			//once click submit, stop the timer
 			stopTimer = true;
 			boolean correctAnswer = compareSolution();
 			if (correctAnswer) {
 				showAlertDialog(v, "Congratulations!", "You've complete the puzzle correctly! Would you like to submit your time?", correctAnswer);
 			} else {
 				showAlertDialog(v, "Try Again", "Your answer doesn't match the solution.", correctAnswer);
 			}
 
 		}
 		
 		private boolean compareSolution(){
 			for (int i = 0; i < dimension; i++) {
 				for (int j = 0; j < dimension; j++) {
 					// the solution
 					Integer sol = gameArray[i][j];
 					// the answer given by the user
 					int state = ((Cell)buttons[i + 1][j + 1]).getState();
 					if (state != 1 && sol.equals(Color.BLACK)) {
 						return false;
 					}
 				}
 			}
 			return true;
 		}
 		
 		
 		private void showAlertDialog(final View v, String title, String message, final boolean answer){
 			final AlertDialog alertDialog = new AlertDialog.Builder(v.getContext()).create();
 			
 			final EditText input = new EditText(v.getContext());
 			input.setHint("enter name");
 			alertDialog.setView(input);
 			input.setVisibility(View.INVISIBLE);
 			if (answer) {
 				input.setVisibility(View.VISIBLE);
 				final String name = input.getText().toString();
 				
 				// get the time in second, as for score
 				String time = (String) timedisplay.getText();
 				String[] splitTime = time.split(":");
 				int minToSec = 60 * Integer.parseInt(splitTime[0]);
 				final int score = minToSec + Integer.parseInt(splitTime[1]);
 				
 				alertDialog.setButton(-2, "Yes", new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int which) {
 						// submit score
 						try {
 							if (dimension == 5){
 								puzzleDifficulty = Difficulty.EASY;
 							} else if (dimension == 10) {
 								puzzleDifficulty = Difficulty.MEDIUM;
 							} else if (dimension == 14) {
 								puzzleDifficulty = Difficulty.HARD;
 							} else {
 								puzzleDifficulty = Difficulty.UNDEFINED;
 							}
 							saveScore(name, score);
 							//NonoClient.saveScore(name, puzzleDifficulty, score);
 						} catch (Exception e) {
 							
 						}
 						// dialog: show score submitted
 						
 						returnMainScreen(v);
 					}
 				});
 				alertDialog.setButton(-1, "No", new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int which) {
 						returnMainScreen(v);
 					}
 				});
 			} else {
 				alertDialog.setButton(-3, "Okay", new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int which) {
 						returnMainScreen(v);
 					}
 				});
 			}
 			
 			alertDialog.setTitle(title);
 			alertDialog.setMessage(message);
 			alertDialog.show();
 		}
 		
 	}
 	
 	
 	private class HintButtonListener implements OnClickListener {
 		@Override
 		public void onClick(View v) {
 			boolean diff = false;
 			for (int i = 0; i < dimension; i++) {
 				for (int j = 0; j < dimension; j++) {
 					if(buttons[i+1][j+1] instanceof Cell){
 						int cellColor = ((Cell) buttons[i+1][j+1]).getColor();
 						Integer cellColor_sol = gameArray[i][j];
 						int cellState = ((Cell) buttons[i+1][j+1]).getState();
 						
 						// the solution doesn't match current cell when:
 						// 1. solution cell is black and current cell is not marked
 						// 2. solution cell is white and current cell is marked (black/question mark)
 						diff = (cellState == 0 && cellColor_sol.equals(Color.BLACK)) || (cellState != 0 && cellColor_sol.equals(Color.WHITE));
 						
 						// current cell color doesn't match the solution
 						if (diff) {
 							// cell flashes
 							flashCell(i, j, cellColor, cellColor_sol);
 							break;
 						}
 					}
 				}
 				
 				if (diff){
 					break;
 				}
 			}
 		}
 		
 		private void flashCell(int i, int j, int cellColor, Integer cellColor_sol) {
 			final Animation animation = new AlphaAnimation(1, 0);
 			animation.setDuration(500);
 			animation.setInterpolator(new LinearInterpolator());
 			animation.setRepeatCount(1);
 			((Cell) buttons[i+1][j+1]).startAnimation(animation);
 			
 			hintActionListener listener = new hintActionListener(((Cell) buttons[i+1][j+1]), cellColor, cellColor_sol);
 			animation.setAnimationListener(listener);
 			
 		}
 	}
 	
 	
 	// a listener class to give a hint, and set back to the original cell color after hint is given
 	private class hintActionListener implements AnimationListener {
 		Cell cell;
 		int cellState;
 		int cellState_sol;
 		
 		private hintActionListener(Cell cell, int cellState, int cellState_sol){
 			this.cell = cell;
 			this.cellState = cellState;
 			this.cellState_sol = cellState_sol;
 		}
 		
 		@Override
 		public void onAnimationEnd(Animation arg0) {
 			// set back to the original cell color
 			cell.setColor(cellState);	
 			cell.setEnabled(true);
 		}
 
 		@Override
 		public void onAnimationStart(Animation arg0) {
 			// gives the correct cell color as for the hint
 			cell.setColor(cellState_sol);
 			cell.setEnabled(false);
 		}
 
 		@Override
 		public void onAnimationRepeat(Animation animation) {
 			// nothing to do here
 		}
 		
 	}
 	
 	
 	private void saveScore(final String name, final int score){
 		Thread thread = new Thread(new Runnable(){
 			@Override
 			public void run() {
 				
 				try {
 					if (dimension == 5){
 						puzzleDifficulty = Difficulty.EASY;
 					} else if (dimension == 10) {
 						puzzleDifficulty = Difficulty.MEDIUM;
 					} else if (dimension == 14) {
 						puzzleDifficulty = Difficulty.HARD;
 					} else {
 						puzzleDifficulty = Difficulty.UNDEFINED;
 					}
 					
 					NonoClient.saveScore(name, puzzleDifficulty, score);
 					
 				} catch (UnknownHostException e) {
 					
 				} catch (IOException e) {
 					
 				} catch (JSONException e) {
 					
 				} catch (Exception e) {
 
 				}
 
 			}
 		});
 		thread.start();
 		try {
 			thread.join();
 		} catch (InterruptedException e) {
 			
 		}
 	}
 	
 }
