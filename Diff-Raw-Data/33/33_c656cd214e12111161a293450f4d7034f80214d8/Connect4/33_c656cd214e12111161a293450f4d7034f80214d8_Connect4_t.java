 package com.gamescrafters.connect4;
 
 import java.util.Stack;
 
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.res.Configuration;
 import android.content.res.Resources;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.view.Window;
 import android.view.WindowManager;
 import android.widget.ImageButton;
 import android.widget.TextView;
 
 import com.gamescrafters.gamesmanmobile.GameActivity;
 import com.gamescrafters.gamesmanmobile.MoveValue;
 import com.gamescrafters.gamesmanmobile.R;
 import com.gamescrafters.gamesmanmobile.RemoteGameValueService;
 
 /**
  * The Connect4 game activity handles the setup of the GUI and internal state of the Connect 4 game.
  * It communicates with the GameValueService to get move values, the GUIGameBoard to handle the board GUI,
  * and will extend GameActivity to interact with the GameController / main frame (and through it, the VVH).
  * @author Royce Cheng-Yue
  * @author Alex Degtiar
  * @author Gayane Vardoyan
  */
 public class Connect4 extends GameActivity {
 	static final String GAME_NAME = "connect4";
 
 	private TextView turnTextView, remoteTextView, gameOverTextView;
 	private ImageButton turnImage;
 	private Drawable bluePiece, redPiece;
 	private CompPlays compPlaying = new CompPlays();
 
 	Game g = null;
 	GUIGameBoard gb = null;
 	MoveValue[] values = null;
 	String previousValue = "win";
 	int delay;
 	AlertDialog.Builder builder;
 	Stack<Integer> previousMoves, nextMoves; // Stacks of previousMoves and nextMoves, which is used to undo and redo moves.
 
 
 	/** Called when the activity is first created. */
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
 
 		setGameView(R.layout.connect4_game);
 
 
 
 
 		//Retrieve data passed from previous activity.
 		Intent myIntent = GameIntent = getIntent();
 		isPlayer1Computer = myIntent.getBooleanExtra("isPlayer1Computer", false);
 		isPlayer2Computer = myIntent.getBooleanExtra("isPlayer2Computer", false);
 		int height = myIntent.getIntExtra("numRows", 6);
 		int width = myIntent.getIntExtra("numCols", 7);
 		String sdelay = myIntent.getStringExtra("numDelay");
 		delay = Integer.parseInt(sdelay);
 
 		/*
 		 * Initialize dialog boxes and popups here
 		 */
 		builder = new AlertDialog.Builder(this);
 		builder.setCancelable(true);
 		builder.setTitle("Warning!");
 		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int which)
 			{
 				dialog.dismiss();
 			}
 		});
 
 		initResources();
		if (isDBAvailable())
 		{
 			setBoard(width, height);
 		} else {
 			playWithoutInternet(width, height);
 			/*
 			 * Play game without database. If any of the players is a computer,
 			 * random moves will be chosen on the turns.
 			 */
 			AlertDialog alert = builder.create();
 			alert.setMessage(getTitle() + " is not currently connected to a database. Move and remoteness values may not be available.");
 			alert.show();
 		}
 	}
 	//Trying to fix the warning dialog from reopening every time the orientation is switched
 	//The code requires that you add  android:configChanges = "orientation" to the android manifest
 	/*public void onConfigurationChanged(Configuration config)
 	{
 		super.onConfigurationChanged(config);
 		setGameView(R.layout.connect4_game);
 		//System.out.println(getResources().getConfiguration().orientation==Configuration.ORIENTATION_PORTRAIT);
 	}*/  
 
 	@Override
 	public void onSaveInstanceState(Bundle savedInstanceState) {
 		//TODO finish this method
 		super.onSaveInstanceState(savedInstanceState);
 		savedInstanceState.putBoolean("isPlayer1Computer", isPlayer1Computer);
 		savedInstanceState.putBoolean("isPlayer2Computer", isPlayer2Computer);
 		//savedInstanceState.put
 	}
 
 	class CompPlays extends Handler {
 		public void handleMessage(Message msg) {
			if (isDBAvailable()) {
 				Connect4.this.updateUI();
 			} else {
 				Connect4.this.updateUI2();
 			}
 		}
 		public void sleep(long delay) {
 			this.removeMessages(0);
 			sendMessageDelayed(obtainMessage(0), delay);
 		}
 	};
 
 	private void updateUI() {
 		if (!g.gameOver) {
 			compPlaying.sleep(delay*1000);
 			if ((g.isBlueTurn() && isPlayer1Computer) || 
 					(!g.isBlueTurn() && isPlayer2Computer)) {
 				doComputerMove();
 			}
 		}
 	}
 
 	private void updateUI2() {
 		if (!g.gameOver) {
 			compPlaying.sleep(delay*1000);
 			if ((g.isBlueTurn() && isPlayer1Computer) || 
 					(!g.isBlueTurn() && isPlayer2Computer)) {
 				playRandom();
 			}
 		}
 	}
 
 	@Override
 	public void newGame() {
 		setBoard(g.width, g.height);
 	}
 
 	public void onRestoreInstanceState(Bundle savedInstanceState) 
 	{// TODO fix!!! very inefficient!
 		super.onRestoreInstanceState(savedInstanceState);
 		isPlayer1Computer = savedInstanceState.getBoolean("isPlayer1Computer");
 		isPlayer2Computer = savedInstanceState.getBoolean("isPlayer2Computer2");
 		Connect4 old = (Connect4) getLastNonConfigurationInstance();
 		isShowValues = old.isShowValues;
 		isShowPrediction = old.isShowPrediction;
 		moveValues = old.moveValues;
 		for (int m : old.previousMoves) {
 			g.doMove(m, false);
 		}
 		nextMoves = old.nextMoves;
 		/*isPlayer1Computer = old.isPlayer1Computer;
 		isPlayer2Computer = old.isPlayer2Computer;
 		table = old.table;
 		turnTextView.setText(old.turnTextView.getText());
 		remoteTextView.setText(old.remoteTextView.getText());
 		gameOverTextView.setText(old.gameOverTextView.getText());
 		turnImage.setImageDrawable(old.turnImage.getDrawable());
 		g = old.g;
 		values = old.values;
 		previousValue = old.previousValue;
 		gb = old.gb;
 		initResources();
 		previousMoves = old.previousMoves;
 		nextMoves = old.nextMoves;
 		moveValues = old.moveValues;
 		 */
 	}
 
 	/**
 	 * Initializes the GUI elements used for the tiles, etc.
 	 */
 	private void initResources(){
 		Resources res = getResources();
 		bluePiece = res.getDrawable(R.drawable.c4_bluepiece);
 		redPiece = res.getDrawable(R.drawable.c4_redpiece);
 
 		turnTextView = (TextView) findViewById(R.id.c4_turn); 
 		turnImage = (ImageButton) findViewById(R.id.c4_turnImage);
 		remoteTextView = (TextView) findViewById(R.id.c4_remoteness);
 		gameOverTextView = (TextView) findViewById(R.id.c4_gameOver);	
 	}
 
 	/**
 	 * Allows user to play Connect4 without Internet.
 	 * No remoteness or game values are available.
 	 * Computers play randomly. If Internet connection comes
 	 * back on, values and remoteness will automatically become
 	 * available and computer players will no longer play random.
 	 */
 	private void playWithoutInternet(int width, int height)
 	{
 		g = new Game(height, width);
 		if (gb == null)
 			gb = new GUIGameBoard(this);
 		else gb.reset(g);
 		gb.initBoard();
 
 		turnTextView.setText("Turn:" );
 		turnImage.setBackgroundDrawable(bluePiece);
 		turnImage.setEnabled(false);
 
 		/*
 		 * API moves
 		 */
 		if (isPlayer1Computer && isPlayer2Computer) {
 			updateUI2();
 		} else if (isPlayer1Computer) {
 			playRandom();
 		}
 	}
 
 	/**
 	 * Initializes the internal state and GUI of the Connect 4 board. 
 	 * @param width The width of the board to construct.
 	 * @param height The height of the board to construct.
 	 */
	private void setBoard(int width, int height) {
		boolean dbAvailable = false;
		
 		g = new Game(height, width);
 		if (gb == null)
 			gb = new GUIGameBoard(this);
 		else
 			gb.reset(g);
 		gb.initBoard();
 
 		turnTextView.setText("Turn: ");
 		turnImage.setBackgroundDrawable(bluePiece);
 		turnImage.setEnabled(false);
 
 		gameOverTextView.setText("");
 		if (getLastNonConfigurationInstance() == null) {
 			values = getNextMoveValues();
 		}
 		g.updateRemoteness();
 		g.updateValues();
 		if (values != null && values.length != 0) {
			dbAvailable = true;
 			previousValue = getBoardValue(values);
 			int remoteness = getRemoteness(previousValue, values);
 			clearVVH();
 			updateVVH(previousValue, remoteness, g.gameOver, g.isBlueTurn(), g.isTie());
 		}
 
 		//computer vs. computer
		if (dbAvailable)
 		{
 			if (isPlayer1Computer && isPlayer2Computer) {
 				updateUI();
 			} else if (isPlayer1Computer) {
 				doComputerMove();
 			}
 		}
 		//g.updateVVH();
 	} 
 
 	@Override
 	public Object onRetainNonConfigurationInstance() {
 		return this;
 	}
 
 	/**
 	 * @return The name of the game, as used in the online db.
 	 */
 	@Override
 	public String getGameName() {
 		return GAME_NAME;
 	}
 
 	@Override
 	public int getNumMovesSoFar() {
 		return g.getNumMovesSoFar();
 	}
 
 	@Override
 	public int getCurrentMove() {
 		return g.currentMove;
 	}
 
 	/**
 	 * @return a string representing the current board.
 	 */
 	@Override
 	public String getBoardString() {
 		int[][] boardRep = g.board;
 		StringBuffer board = new StringBuffer();
 		for (int i=0; i < boardRep.length; i++) { //height
 			for (int j = 0; j < boardRep[0].length; j++) { //width
 				int currentElem = boardRep[i][j];
 				if (currentElem == Connect4.Game.RED) {
 					board.append("O");
 				} else if (currentElem == Connect4.Game.BLUE) {
 					board.append("X");
 				} else {
 					board.append("%20");
 				}
 			}
 		}
 		board.append(";width=");
 		board.append(boardRep[0].length);
 		board.append(";height=");
 		board.append(boardRep.length);
 		board.append(";");
 		board.append("pieces=4");
 		return board.toString();
 	}
 
 	/**
 	 * Given a String move in the database move representation, does the move.
 	 */
 	@Override
 	public void doMove(String move) {
 		g.doMove(Integer.parseInt(move), false);
 	}	
 
 	/**
 	 * Updates whether or not the values will display on the game GUI.
 	 * It is called when a user changes the "Show Values" option. 
 	 */
 	@Override
 	public void updateValuesDisplay() {
 		g.updateValues();
 	}
 
 	/**
 	 * Updates whether or not the prediction will display on the game GUI.
 	 * It is called when a user changes the "Show Prediction" option.
 	 */
 	@Override
 	public void updatePredictionDisplay() {
 		g.updateRemoteness();
 	}
 
 	/**
 	 * Redo a move. If no next moves, does nothing.
 	 */
 	@Override
 	public void redoMove() {
 		if (isPlayer1Computer && isPlayer2Computer) {
 			redoHelper();
 		}
 		else if (isPlayer2Computer || isPlayer1Computer) {
 			redoHelper();
 			redoHelper();
 		}
 		else {
 			redoHelper();
 		}
 	}
 
 	private void redoHelper() {
 		if (!nextMoves.isEmpty()) {
 			int col = nextMoves.pop();
 			g.doMove(col, true);
 		}
 	}
 
 	@Override
 	public void undoMove() {
 		if (isPlayer1Computer && isPlayer2Computer) {
 			g.undoMove();
 		}
 		else if (isPlayer2Computer) {
 			g.undoMove();
 			g.undoMove();
 		}
 		else if (isPlayer1Computer) {
 			if (previousMoves.size() != 1) {
 				g.undoMove();
 				g.undoMove();
 			}
 		}
 		else {
 			g.undoMove();
 		}
 	}
 
 	/**
 	 * A class that contains the internal state of the Connect 4 game,
 	 * as well as accessor and modifier methods.
 	 */
 	class Game {
 		final static boolean BLUE_TURN = false;
 		final static boolean RED_TURN = true;
 		final static int RED = 1;
 		final static int BLUE = 2;
 		final static int BLUEHL = 3;
 		final static int REDHL = 4;
 		final static int EMPTY = 0;
 
 		boolean turn = BLUE_TURN; // first player, blue
 		int board[][]; 	// The internal state of the game. Either RED, BLUE, or EMPTY.
 		int height, width;
 		boolean gameOver = false;
 		int movesSoFar;
 		int currentMove;
 
 		public Game(int height, int width) {
 			this.height = height;
 			this.width = width;
 			board = new int[height][width];
 			previousMoves = new Stack<Integer>();
 			movesSoFar = 0;
 			currentMove = 0;
 
 			nextMoves = new Stack<Integer>();
 
 		}
 
 		/**
 		 * Undoes a move. If no previous moves, does nothing.
 		 */
 		public void undoMove() {
 			if (previousMoves.isEmpty()) return;
 			int col = previousMoves.pop();
 			// Remove the last move from the VisualValueHistory.
 			removeLastVVHNode();
 			for (int i = height - 1; i >= 0; i--) {
 				if (g.board[i][col] != EMPTY) { // find first available piece in col to remove
 					if (g.gameOver) { // primitive position undo
 						g.gameOver = false;
 						turnTextView.setText("Turn: ");
 						turnImage.setBackgroundDrawable(g.getTurn() == BLUE ? bluePiece : redPiece);
 						gameOverTextView.setText("");
 					}
 					board[i][col] = EMPTY;
 					gb.updateTile(EMPTY, i, col);
 					values = getNextMoveValues();
 					updateRemoteness();
 					updateValues();
 					break;
 				}
 			}
 			currentMove--;
 			hSlider.updateProgress(currentMove, movesSoFar);
 			switchTurn();
 			nextMoves.push(col);
 		}
 
 		//		public void goToMoveN(int N) {
 		//			if (0 <= N && N <= movesSoFar) {
 		//				while (N < currentMove) {
 		//					undoMove();
 		//				}
 		//				while (N > currentMove) {
 		//					redoMove();
 		//				}
 		//			}
 		//		}
 
 		/**
 		 * Checks if the move (column number) is valid, and performs the move if it is.
 		 * Assume nextMoves stack is not empty.
 		 * @param move The column number of the piece to drop.
 		 * @param isRedo Boolean if move is a redo move (true) or not (false).
 		 */
 		public void doMove(int move, boolean isRedo) {
 			if (!(isMoveInvalid(move) || gameOver)) {
 				updateGameState(move);
 				checkBoard(findEmptyRowInColumn(move) - 1, move);
 				switchTurn();
				if (isDBAvailable())
 				{
 					values = getNextMoveValues();
 					updateRemoteness();
 					updateValues();
 					if ((values != null) && (values.length != 0)) {
 						previousValue = getBoardValue(values);
 						int remoteness = getRemoteness(previousValue, values);
 						Connect4.this.updateVVH(previousValue, remoteness, gameOver, isBlueTurn(), isTie());
 					}
 				}
 				//updateVVH();
 				previousMoves.push(move);
 				currentMove++;
 				if (!isRedo) {
 					nextMoves.clear();
 					movesSoFar = currentMove;
 				}
 				hSlider.updateProgress(currentMove, movesSoFar);
 			}
 		}
 
 
 
 		/**
 		 * Updates the GUI move values (the values instance variable needs to be up-to-date!).
 		 */
 		private void updateValues() {
 			if ((values != null) && isShowValues()) {
 				boolean has_value[] = new boolean[width];
 				for (MoveValue val : values){ // set values of columns that have values
 					int col = val.getIntMove();
 					gb.setColumnValue(col, val.getValue());
 					has_value[col] = true;
 				}
 				for (int col=0; col<width; col++) { // no fill for columns without values
 					if (!has_value[col]) gb.setColumnValue(col, "noval");
 				}
 			} else {
 				for (int col=0; col<width; col++) {
 					gb.setColumnValue(col, "noval");
 				}
 			}
 		}
 
 		/**
 		 * Updates the remoteness text field based on the "values" variable.
 		 */
 		private void updateRemoteness() {
 
 			if (!isShowPrediction()) {
 				remoteTextView.setText("");
 			} else if ((values != null) && (values.length != 0)) {
 				previousValue = getBoardValue(values);
 
 
 				int remoteness = getRemoteness(previousValue, values);
 
 				if (remoteness != -1)
 					remoteTextView.setText(previousValue + " in " + (remoteness+1) + " moves");
 				else remoteTextView.setText("Prediction not available.");
			} else if (!isDBAvailable()) {
 				remoteTextView.setText("Prediction not available.");
 			} else {
 				remoteTextView.setText((previousValue.equals("win") ? "lose" : "tie") + " in 0 moves");
 			}
 		}
 
 		//		private void updateVVH () {
 		//			VVHNode node;
 		//			if ((values != null) && (values.length != 0)) {
 		//				previousValue = getBoardValue(values);
 		//				
 		//				int remoteness = getRemoteness(previousValue, values);
 		//				if (remoteness == -1) return;
 		//				if (gameOver && isTie())
 		//				node = new VVHNode("gameover-tie", remoteness, !isBlueTurn());
 		//				else if (gameOver)
 		//					node = new VVHNode("gameover", remoteness, !isBlueTurn());
 		//				else node = new VVHNode(previousValue, remoteness, isBlueTurn());
 		//				VVHList.add(node);
 		//			}
 		//		}
 
 		/**
 		 * @return Whether or not it is blue's turn.
 		 */
 		public boolean isBlueTurn() {
 			return turn == BLUE_TURN;
 		}
 
 		/**
 		 * @return Whether or not it is red's turn.
 		 */
 		public boolean isRedTurn() {
 			return !isBlueTurn();
 		}
 
 		/**
 		 * @return Returns the total moves made so far.
 		 */
 		public int getNumMovesSoFar() {
 			return movesSoFar;
 		}
 
 		/**
 		 * @return RED or BLUE, depending on whose turn it is.
 		 */
 		private int getTurn() {
 			return isRedTurn() ? RED : BLUE;
 		}
 
 		/**
 		 * @param row The row of the board to check.
 		 * @param column The column of the board to check.
 		 * @return The piece at that spot, RED, BLUE, or EMPTY.
 		 */
 		public int pieceAt(int row, int column) {
 			return board[row][column];
 		}
 
 
 		/**
 		 * Toggles the turn, and updates the picture for the turn field.
 		 */
 		private void switchTurn() {
 			turn = !turn;
 			if (!gameOver) turnImage.setBackgroundDrawable(isBlueTurn() ? bluePiece : redPiece);
 		}
 
 		/**
 		 * Updates the gamestate given a column in which a piece was placed.
 		 * @param col The column in which the piece was placed.
 		 */
 		private void updateGameState(int col) { 
 			commitCell(findEmptyRowInColumn(col), col);
 		}
 		
 		/**
 		 * Finds the first available row in a given column
 		 * @param column The column of the board to find the first available row
 		 * @return An integer telling where the first available row is in a given column.
 		 */
 		public int findEmptyRowInColumn(int column) {
 			for (int row = height - 1; row >= 0; row--) {
 				if (board[row][column] == BLUE || board[row][column] == RED) {
 					return row + 1;
 				}
 			}
 			return 0;
 		}
 		
 		/**
 		 * Places a highlighted piece into the GUI at the corresponding point.
 		 * The board is not affected.
 		 * @param row The row of the space to place highlighted piece.
 		 * @param column The column of the space to place highlighted piece.
 		 */
 		public void highlightCell(int row, int column){
 			if (isRedTurn()) {
 				gb.updateTile(REDHL, row, column);
 			}
 			else {
 				gb.updateTile(BLUEHL, row, column);
 			}
 		}
 		
 		/**
 		 * Removes the highlighted piece from the GUI.
 		 * The board is not affected.
 		 * @param row The row of the piece currently highlighted.
 		 * @param column The column of the piece currently highlighted.
 		 */
 		public void dehighlightCell(int row, int column) {
 			gb.updateTile(EMPTY, row, column);
 		}
 		
 		/**
 		 * Registers the current cell to be used as the player's move,
 		 * the board and the GUI are updated with the corresponding piece.
 		 * @param row The row of the piece just placed.
 		 * @param column The column of the piece just placed.
 		 */
 		public void commitCell(int row, int column) {
 			if (isRedTurn()) {
 				board[row][column] = RED;
 				gb.updateTile(RED, row, column);
 			}
 			else {
 				board[row][column] = BLUE;
 				gb.updateTile(BLUE, row, column);
 			}
 		}
 
 		/**
 		 * Checks if there are N in a row pieces.
 		 * @param row The row of the piece just placed.
 		 * @param col The column of the piece just placed.
 		 * @param N The number of pieces to check.
 		 * @return Whether there were N in a row or not.
 		 */
 		private boolean isNInRow(int row, int col, int N) {
 			int value = getTurn();
 			int vertInRow = 1, horizInRow = 1, DRInRow = 1, DLInRow = 1;
 
 			for(int y=row-1; y>=0; y--)  { 		//check rows below 
 				if (board[y][col] == value) vertInRow++;
 				else break;
 			}
 			for(int y=row+1; y<height; y++)  { //check rows above 
 				if (board[y][col] == value) vertInRow++;
 				else break;
 			}
 
 			for(int x=col-1; x>=0; x--)  {		//check columns left
 				if (board[row][x] == value) horizInRow++;
 				else break;
 			}
 			for(int x=col+1; x<width; x++)  {	//check columns right
 				if (board[row][x] == value) horizInRow++;
 				else break;
 			}
 
 			for(int y=row+1, x=col-1; y<height && x>= 0; y++, x--) { //check diagonal up-left
 				if (board[y][x] == value) DRInRow++;
 				else break;
 			}
 			for(int y=row-1, x=col+1; y>=0 && x<width; y--, x++) { //check diagonal down-right
 				if (board[y][x] == value) DRInRow++;
 				else break;
 			}
 
 			for(int y=row-1, x=col-1; y>=0 && x>= 0; y--, x--) { //check diagonal down-left
 				if (board[y][x] == value) DLInRow++;
 				else break;
 			}
 			for(int y=row+1, x=col+1; y<height && x<width; y++, x++) { //check diagonal up-right
 				if (board[y][x] == value) DLInRow++;
 				else break;
 			}
 
 			return vertInRow >= N || horizInRow >= N || DRInRow >= N || DLInRow >= N;
 		}
 
 		/**
 		 * Checks the board for a possible primitive position, and modifies gameOver.
 		 * @param row The row of the piece placed.
 		 * @param col The column of the piece placed.
 		 */
 		private void checkBoard(int row, int col) {
 			if (isNInRow(row, col, 4)) {
 				turnTextView.setText("");
 				turnImage.setBackgroundDrawable(null);
 				gameOverTextView.setText("Game over.\n" + (isBlueTurn() ? "Blue" : "Red") + " wins!");
 				gameOver = true;
 				if ((values != null) && (values.length != 0)) {
 					previousValue = getBoardValue(values);
 					int remoteness = getRemoteness(previousValue, values);
 					Connect4.this.updateVVH(previousValue, remoteness, gameOver, isBlueTurn(), isTie());
 				}
 			} else if(isTie()) {
 				turnTextView.setText("");
 				turnImage.setBackgroundDrawable(null);
 				gameOverTextView.setText("Game over.\nIt's a draw!"); 
 				gameOver = true; 
 				if ((values != null) && (values.length != 0)) {
 					previousValue = getBoardValue(values);
 					int remoteness = getRemoteness(previousValue, values);
 					Connect4.this.updateVVH(previousValue, remoteness, gameOver, isBlueTurn(), isTie());
 				}
 			} 
 		}
 
 		/**
 		 * @return Whether or not the game is a tie.
 		 */
 		private boolean isTie() { 
 			for(int j=0; j<width; j++) { 
 				if(board[g.height-1][j]==EMPTY) { 
 					return false; 
 				} 
 			}
 			return true;
 		}
 
 
 	}
 
 	/**
 	 * @param move The column number to check (0->width-1).
 	 * @return A boolean whether a column is full.
 	 */
 	@Override
 	public boolean isMoveInvalid(int move) {	
 		return g.findEmptyRowInColumn(move) == g.width;
 	}
 
 	@Override
 	public void updateUIRandom() {
 		Connect4.this.updateUI2();
 	}
 
 	@Override
 	public void updateUISmart() {
 		Connect4.this.updateUI();
 	}
 
 
 
 } 
