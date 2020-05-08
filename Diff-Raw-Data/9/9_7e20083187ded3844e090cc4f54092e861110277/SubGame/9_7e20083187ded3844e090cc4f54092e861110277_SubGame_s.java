 package capstone.game;
 
 public class SubGame {
 	
 	private int[][] board; //2D array representing the placement of player pieces. 0 = empty space, 1 = Player 1(X), 2 = Player 2(O) 
 	private int status; // status of the subgame. 0 = open (no winner, empty spaces available), 1 = subgame won by Player 1 (X), 2 = subgame won by Player 2(O), 3 = Tied (no empty spaces and no winner)
 	
 	/**
 	 * Constructor. 
 	 * Creates a subgame with all empty spaces and sets as an open game
 	 */	
 	public SubGame(){
 		board = new int[3][3];
 		for (int i=0; i<3; i++){
 			for (int j =0; j<3; j++){
 				board[i][j]=0;
 			}
 		}
 		status = 0;
 	}
         
         /**
 	 * Constructor. 
 	 * Wraps an integer matrix into a SubGame.
 	 */	
         public SubGame(int[][] data){
             board = data;
            status=0;
         }
 	
 	/**
 	 * In addition to the usual setter for the board, which sets the entire 2D array, I've included this
 	 * method to easily set individual pieces in the array. 
 	 * @param x Column ( 0 - 2)
 	 * @param y Row (0 - 2)
 	 * @param playerpiece 1 for Player 1 (X), 2 for Player 2 (O)
 	 */
 	public void setGamePiece(int x, int y, int playerpiece){
 		//TODO: Check if placement and piece are both valid
 		board[x][y] = playerpiece;
                 if(GameRules.checkStatusBoard(board) == 1) {
                     setStatus(1);
                 } else if(GameRules.checkStatusBoard(board) == 2) {
                     setStatus(2);
                 } else if(GameRules.checkStatusBoard(board) == 3) {
                     setStatus(3);
                 }
                 
 	}
 	
 	/**
 	 * In addition to the usual getter for the board, which returns the entire 2D array, I've included this
 	 * method to easily get individual pieces from a location on the board. 
 	 * @param x Column (0 - 2)
 	 * @param y Row (0 - 2)
 	 * @return 0 is space is empty; 1 if Player 1 (X) occupies the space; 2 if Player2 (O) occupies the space
 	 */
 	public int getGamePiece(int x, int y){
 		return board[x][y];
 	}
 	
 	public int[][] getBoard() {
 		return board;
 	}
 
 	public void setBoard(int[][] board) {
 		this.board = board;
                 int value = GameRules.checkStatusBoard(board);
                 this.status = value;
 	}
 
 	public int getStatus() {
 		return status;
 	}
 	
 	/**Sets the status of the subgame. 
 	 * NOTE: This is assumed to be valid. Validity should be tested before the method is called.
 	 * @param status 
 	 */
 	public void setStatus(int status) {
 		this.status = status;
 	}
 
 }
