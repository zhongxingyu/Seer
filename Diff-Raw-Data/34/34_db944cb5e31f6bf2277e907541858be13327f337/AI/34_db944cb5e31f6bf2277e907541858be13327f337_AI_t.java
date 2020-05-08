 package afvz.first;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Color;
 import android.media.MediaPlayer;
 import android.os.Bundle;
 import android.view.View;
 import android.view.Window;
 import android.view.WindowManager;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.Toast;
 import android.os.CountDownTimer;
 import android.widget.TextView;
 
 
 public class AI extends Activity {
 
 	BoardConfig board = new BoardConfig();
 	CountDownTimer startWait;
 	CountDownTimer aiWait;
 	boolean aiGoing;
 	
 	// a variable for canceling the timer when the board is solved
 	boolean solved = false;
 	
 	TextView tv;
 	
 	// for setting the size of boards
 	static int size;
 	
 	// array of the buttons to access them directly
 	private Button[][] buttons = new Button[5][5];
 
 	// array of the AI buttons to access them directly
 	private Button[][] aiButtons = new Button[5][5]; 
 	
 	public Integer boardsize = 5;
 
 	// all button resources
 	private Integer[] btn_res_id = {
 		R.drawable.btn1, R.drawable.btn2, R.drawable.btn3, R.drawable.btn4, R.drawable.btn5,
 		R.drawable.btn6, R.drawable.btn7, R.drawable.btn8, R.drawable.btn9, R.drawable.btn10,
 		R.drawable.btn11, R.drawable.btn12, R.drawable.btn13, R.drawable.btn14, R.drawable.btn15,
 		R.drawable.btn16, R.drawable.btn17, R.drawable.btn18, R.drawable.btn19, R.drawable.btn20,
 		R.drawable.btn21, R.drawable.btn22, R.drawable.btn23, R.drawable.btn24, 
 	};
 	
 	// all AI button resources
 	private Integer[] btn_res_id2 = {
 		R.drawable.btn_1, R.drawable.btn_2, R.drawable.btn_3, R.drawable.btn_4, R.drawable.btn_5,
 		R.drawable.btn_6, R.drawable.btn_7, R.drawable.btn_8, R.drawable.btn_9, R.drawable.btn_10,
 		R.drawable.btn_11, R.drawable.btn_12, R.drawable.btn_13, R.drawable.btn_14, R.drawable.btn_15,
 		R.drawable.btn_16, R.drawable.btn_17, R.drawable.btn_18, R.drawable.btn_19, R.drawable.btn_20,
 		R.drawable.btn_21, R.drawable.btn_22, R.drawable.btn_23, R.drawable.btn_24, 			
 	};
 	
 	// all buttons in the layout
 	private Integer[][] btn_id = {
 		{ R.id.gm1_btn1, R.id.gm1_btn2, R.id.gm1_btn3, R.id.gm1_btn4, R.id.gm1_btn5} ,
 		{ R.id.gm1_btn6, R.id.gm1_btn7, R.id.gm1_btn8, R.id.gm1_btn9, R.id.gm1_btn10} ,
 		{ R.id.gm1_btn11, R.id.gm1_btn12, R.id.gm1_btn13, R.id.gm1_btn14, R.id.gm1_btn15} ,
 		{ R.id.gm1_btn16, R.id.gm1_btn17, R.id.gm1_btn18, R.id.gm1_btn19, R.id.gm1_btn20} ,
 		{ R.id.gm1_btn21, R.id.gm1_btn22, R.id.gm1_btn23, R.id.gm1_btn24, R.id.gm1_btn25}
 	};
 	
 	// all buttons in the layout
 	private Integer[][] btn_id2 = {
 		{ R.id.ai_btn1, R.id.ai_btn2, R.id.ai_btn3, R.id.ai_btn4, R.id.ai_btn5} ,
 		{ R.id.ai_btn6, R.id.ai_btn7, R.id.ai_btn8, R.id.ai_btn9, R.id.ai_btn10} ,
 		{ R.id.ai_btn11, R.id.ai_btn12, R.id.ai_btn13, R.id.ai_btn14, R.id.ai_btn15} ,
 		{ R.id.ai_btn16, R.id.ai_btn17, R.id.ai_btn18, R.id.ai_btn19, R.id.ai_btn20} ,
 		{ R.id.ai_btn21, R.id.ai_btn22, R.id.ai_btn23, R.id.ai_btn24, R.id.ai_btn25}
 	};
 	
 	
 	private void play_sound()
 	{
 		MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.sound);
 		mediaPlayer.start();
 	}
 	
 	// when the game is done, freeze the board, let user to start a new one
 	private void freeze_board()	{
 		for(int i=0; i<5; i++)
 			for(int j=0; j<5; j++)
 				buttons[i][j].setEnabled(false);
 	}
 	
 
 	// display the current state of the buttons
 	private void displayGrid() {
 		// i is row index and j is column index
 		// indexing origin is top left corner
 		// loop through all buttons
 		for (int i = 0; i < boardsize; i++) {
 			for (int j = 0; j < boardsize; j++) {
 				int val = board.getGrid(i, j);
 				if ((val > numType.ZERO) && (val <= numType.TWENTYFOUR))
 					buttons[i][j].setBackgroundResource(btn_res_id[val-1]);
 				else if (val == numType.BLANKTILE)
 					buttons[i][j].setBackgroundColor(Color.BLACK);
 			}
 		}
 		
 		if(isBoardValid()){ 
 			Toast.makeText(AI.this, "You Win", Toast.LENGTH_SHORT).show();
 			solved = true;
 			freeze_board();
 		}
 
 	}
 	
 	// display the current state of the buttons in AI
 	private void displayGrid2() {
 		// i is row index and j is column index
 		// indexing origin is top left corner
 		// loop through all buttons
 		for (int i = 0; i < boardsize; i++) {
 			for (int j = 0; j < boardsize; j++) {
 				int val = board.getGrid2(i, j);
 				if ((val > numType.ZERO) && (val <= numType.TWENTYFOUR))
 					aiButtons[i][j].setBackgroundResource(btn_res_id2[val-1]);
 				else if (val == numType.BLANKTILE)
 					aiButtons[i][j].setBackgroundColor(Color.BLACK);
 			}
 		}
 	}
 	
 	
 
 	public Boolean isBoardValid()
 	{
 		int val;
 		Boolean res=true;
 		
 		// if the first one is the tile
 		if (board.getGrid(0, 0) == numType.BLANKTILE) {
 			for (int i = 1; i < boardsize*boardsize; i++) {
 				val = board.getGrid(i/boardsize, i%boardsize);
 				res &= (val == i);
 				if (!res) return false;		//short circuit
 			}
 			return res;
 		}
 		// if the last one is the tile
 		else if (board.getGrid(boardsize-1, boardsize-1) == numType.BLANKTILE) {
 			for (int i = 0; i < boardsize*boardsize-1; i++) {
 				val = board.getGrid(i/boardsize, i%boardsize);
 				res &= (val == i+1);
 				if (!res) return false;		//short circuit
 			}
 			return res;
 		}
 		else return false;
 	}
 	
	public Boolean isAiBoardValid()
	{
		int val;
		Boolean res=true;
		
		// if the first one is the tile
		if (board.getGrid2(0, 0) == numType.BLANKTILE) {
			for (int i = 1; i < boardsize*boardsize; i++) {
				val = board.getGrid2(i/boardsize, i%boardsize);
				res &= (val == i);
				if (!res) return false;		//short circuit
			}
			return res;
		}
		// if the last one is the tile
		else if (board.getGrid2(boardsize-1, boardsize-1) == numType.BLANKTILE) {
			for (int i = 0; i < boardsize*boardsize-1; i++) {
				val = board.getGrid2(i/boardsize, i%boardsize);
				res &= (val == i+1);
				if (!res) return false;		//short circuit
			}
			return res;
		}
		else return false;
	}
	
 	// initialize the board to the original state, the right bottom is the tile 
 	private void initializeBoard() {
 		int accu = 0;
 		for (int i = 0; i < boardsize; i++) {
 			for (int j = 0; j < boardsize; j++) {
 				accu += 1;
 				board.setGrid(i, j, numType.ZERO + accu);
 			}
 		}
 		board.setGrid(boardsize-1, boardsize-1, numType.BLANKTILE);
 	}
 	
 	
 	// setup the values for the tiles
 	private void setupTiles() {
 		initializeBoard(); 		// generate a good board
 		board.randomizeTiles(); // swap tiles around
 		displayGrid();			// display tiles
 		displayGrid2();			//display tiles of AI
 		 
 	}
 
 	// bind buttons in an array so they are easy to access
 	private void bindButtons() {
 		for(int i=0; i<5; i++)
 			for(int j=0; j<5; j++)
 				buttons[i][j] = (Button) this.findViewById(btn_id[i][j]);
 	}
 	
 	// bind buttons of AI in an array so they are easy to access
 	private void bindButtons2() {
 		for(int i=0; i<5; i++)
 			for(int j=0; j<5; j++)
 				aiButtons[i][j] = (Button) this.findViewById(btn_id2[i][j]);
 	}
 
 	// bind row of buttons click events
 	private void bindOnClicks() {
 		// bind each row of buttons on click events
 		bindRow(0);
 		bindRow(1);
 		bindRow(2);
 		bindRow(3);
 		bindRow(4);
 		
         final Button btn_newAIGame = (Button) findViewById(R.id.btn_newAIGame);
         //btn_newAIGame.setBackgroundResource(R.drawable.newaigame);
         btn_newAIGame.setOnClickListener(new OnClickListener() {
             public void onClick(View v) {
                 // Perform action on click
 
             	startWait.cancel();
             	if(aiGoing)
             		aiWait.cancel();
             	
                 Intent intent = new Intent(AI.this, AI.class);
                 startActivity(intent);
             }
         });
         
         final Button btn_newNGame = (Button) findViewById(R.id.btn_newGame);
         btn_newNGame.setOnClickListener(new OnClickListener() {
             public void onClick(View v) {
                 // Perform action on clicks
             	
             	startWait.cancel();
             	if(aiGoing)
             		aiWait.cancel();
             	
                 Intent intent = new Intent(AI.this, Game1.class);
                 startActivity(intent);
             }
         });
 
 	}
 
 
 	// bind a row of buttons on click events
 	private void bindRow(final int i) {
 		// setup so it passes in its row and 0th position on that row
 		buttons[i][0].setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				if(board.getGrid(i,0) != numType.BLANKTILE)
 				{
 					board.doClick(i, 0);
 					displayGrid();
 					play_sound();
 				}
 			}
 		});
 
 		// setup so it passes in its row and 1st position on that row
 		buttons[i][1].setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				if(board.getGrid(i,1) != numType.BLANKTILE)
 				{
 					board.doClick(i, 1);
 					displayGrid();
 					play_sound();
 				}
 			}
 		});
 
 		// setup so it passes in its row and 2nd position on that row
 		buttons[i][2].setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				
 				if(board.getGrid(i,2) != numType.BLANKTILE)
 				{
 					board.doClick(i, 2);
 					displayGrid();
 					play_sound();
 				}
 			}
 		});
 
 		// setup so it passes in its row and 3rd position on that row
 		buttons[i][3].setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				if(board.getGrid(i,3) != numType.BLANKTILE)
 				{
 					board.doClick(i, 3);
 					displayGrid();
 					play_sound();
 				}
 			}
 		});
 
 		// setup so it passes in its row and 4th position on that row
 		buttons[i][4].setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				if(board.getGrid(i,4) != numType.BLANKTILE)
 				{
 					board.doClick(i, 4);
 					displayGrid();
 					play_sound();
 				}
 			}
 		});
 	}
 	
 	
 	
 	public void set_board_size(int size) {
 		boardsize = size;
 		board.boardSize = size;
 		
 		// first reset all of them to visible
 		for (int i = 0; i < 5; i++)
 			for (int j = 0; j < 5; j++)
 				{
 				buttons[i][j].setVisibility(View.VISIBLE);
 				aiButtons[i][j].setVisibility(View.VISIBLE);
 				}
 				
 		
 		switch (size) {
 		case 2:
 			for (int i = 0; i < 3; i++) {
 				buttons[2][i].setVisibility(View.INVISIBLE);
 				buttons[i][2].setVisibility(View.INVISIBLE);
 				aiButtons[2][i].setVisibility(View.INVISIBLE);
 				aiButtons[i][2].setVisibility(View.INVISIBLE);
 				//board.shuffleSize = 8;
 				//board.moves = new int[board.shuffleSize];
 			}
 		case 3:
 			for (int i = 0; i < 4; i++) {
 				buttons[3][i].setVisibility(View.INVISIBLE);
 				buttons[i][3].setVisibility(View.INVISIBLE);
 				aiButtons[3][i].setVisibility(View.INVISIBLE);
 				aiButtons[i][3].setVisibility(View.INVISIBLE);
 				//board.shuffleSize = 150;
 				//board.moves = new int[board.shuffleSize];
 			}
 		case 4:
 			for (int i = 0; i < 5; i++) {
 				buttons[4][i].setVisibility(View.INVISIBLE);
 				buttons[i][4].setVisibility(View.INVISIBLE);
 				aiButtons[4][i].setVisibility(View.INVISIBLE);
 				aiButtons[i][4].setVisibility(View.INVISIBLE);
 				//board.shuffleSize = 250;
 				//board.moves = new int[board.shuffleSize];
 			}
 			break;
 		default:
 			boardsize = 5;
 			board.boardSize = 5;
 			//board.shuffleSize = 400;
 			//board.moves = new int[board.shuffleSize];
 			break;
 		}
 	}
 	
 	@Override
 	public void onBackPressed() {
 	  if (aiGoing)
 			aiWait.cancel();
 	  startWait.cancel();
 	  super.onBackPressed();
 	}
 
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
         requestWindowFeature(Window.FEATURE_NO_TITLE);
         getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
                              WindowManager.LayoutParams.FLAG_FULLSCREEN);
 		setContentView(R.layout.ai);
 		board.boardSize = 5;
 		
 		tv = (TextView)this.findViewById(R.id.TextView1);
 
 		aiGoing = false;
 		
 		bindButtons(); 	// bind the buttons into an array
 		bindButtons2();	// bind the AI buttons into an array
 		bindOnClicks(); // bind their onClick events
 		set_board_size(size);
 		
 		
    	 	if(board.boardSize == 5){
    	 		board.shuffleSize = 400;
    	 		board.moves = new int[board.shuffleSize];}
    	 	else if(board.boardSize == 4){
    	 		board.shuffleSize = 250;
    	 		board.moves = new int[board.shuffleSize];
    	 	}
    	 	else if(board.boardSize == 3){
    	 		board.shuffleSize = 150;
    	 		board.moves = new int[board.shuffleSize];
    	 	}
    	 	else if(board.boardSize == 2){
    	 		board.shuffleSize = 8;
    	 		board.moves = new int[board.shuffleSize];
    	 	}
 		
 		setupTiles(); 	// setup the grid of tiles
 		
 		
 		 
 		// for implementation of AI
 		startWait = new CountDownTimer(3000, 1000) {
 			int totalTime = 0;
 			 
 		     public void onTick(long millisUntilFinished) {
 		    	 
 		     }
 
 		     public void onFinish() {
 	    	 
 				 //new CountDownTimer(23600, 100) {
 		    	 //new CountDownTimer(156000, 1000) {
 		    	 if(board.boardSize == 5)
 		    		 totalTime = 417000;
 		    	 else if(board.boardSize == 4)
 		    		 totalTime = 262000;
 		    	 else if(board.boardSize == 3)
 		    		 totalTime = 158000;    		 
 		    	 else if(board.boardSize == 2)
 		    		 totalTime = 10000;
 		    	 aiGoing = true;
 		    	 
 		    	 aiWait = new CountDownTimer(totalTime, 1000) {
 		    		 
 						// i is total number of shuffles that we made and we want to backtrack in AI
 						int i = board.shuffleSize - 1;
 												 
 					     public void onTick(long millisUntilFinished) {
 					    	 // call solveAI method to go back one step each time on AI 
 					    	 
 					    	 if(solved)
 					    		 this.cancel();
 					    		 
 					    	 else if (i> -1)
 					    	 {	
 					    		 board.solveAI(i);
 					    	 	 i -=1;
 					    	 }
 					    	 // to count down until the player solve the game or times up 
 					    	 if(!solved)
 					    		// tv.setText("" + millisUntilFinished / 1000);
 					    	 
 					    	 // show the AI after backtracking one step
 					    	 displayGrid2();
					    	 
					    	 if(isAiBoardValid())
					    	 {
					    		 Toast.makeText(AI.this, "You Lose", Toast.LENGTH_SHORT).show();
							 	freeze_board();
							 	this.cancel();
					    	 }
 					     }
 
 					     public void onFinish() {
 					    	 if(!solved)
 					    	 {
 						    	tv.setText("0");
 						 		if(!isBoardValid()) 
 									Toast.makeText(AI.this, "You Lose", Toast.LENGTH_SHORT).show();
 						 		freeze_board();
 					    	 }
 					 				
 					     }
 					  }.start();	    	 
 		     }
 		  }.start();	
 
 	}
 }
