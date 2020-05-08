 /**
  * COMP6721
  * Polarized Ladder Game
  * March 2, 2013 
  */
 package polarizedladder;
 
 import java.awt.Point;
 import java.util.Scanner;
 import java.util.Calendar;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 
 public class PLGame {
 
 	private boolean gameOver;
 	private int playerTurn;
 	SearchLists searchList;
 
 	public PLGame(){
 		searchList = new SearchLists();
 	}
 
 	public void startGame(PLGame game, Board board, Player [] players, WinPatternStrategy detectWin, int gameType){
 		if(gameType == 1)
 			game.startHumanGame(board, players, detectWin);
 		if(gameType == 2)
 			game.startHumanComputerGame(board, players, detectWin);
 		if(gameType == 3)
 			game.startComputerHumanGame(board, players, detectWin);
 	}
 
 	private void startHumanGame(Board board, Player [] players, WinPatternStrategy detectWin) {
 
 		setGameOver(false);	
 		setPlayerTurn(0);
 
 		while (!isGameOver()) {
 
 			board.printBoard();				// display board state
 
 			// TODO: add win condition check
 			if (board.isBoardFull()) {		// check win condition or draw
 
 				System.out.printf("The game ended in a draw!\n");
 				setGameOver(true);
 			}
 
 			if (getPlayerTurn() == 0) {			// player turn
 
 				nextPlayerMove(players, detectWin, board, getPlayerTurn());
 
 			} else {
 
 				nextPlayerMove(players, detectWin, board, getPlayerTurn());
 			}
 		}
 
 		System.out.printf("Game over!\n");
 	}
 
 
 
 	private void startHumanComputerGame(Board board, Player [] players, WinPatternStrategy detectWin){
 
 		setGameOver(false);	
 		setPlayerTurn(0);
 
 		while (!isGameOver()) {
 
 			board.printBoard();				// display board state
 
 			// TODO: add win condition check
 			if (board.isBoardFull()) {		// check win condition or draw
 
 				System.out.printf("The game ended in a draw!\n");
 				setGameOver(true);
 			}
 
 			if (playerTurn == 0) {			// player turn
 
 				
 				nextPlayerMove(players, detectWin, board, getPlayerTurn());
 				
 				////////////////////////////////////////////////////////////////// debug first
 				/*Point playerMove = players[playerTurn].doPlayerTurn(players[0]);
 
 				if ( players[0].setDisc(playerMove.y, playerMove.x) == false) 
 				{
 
 					players[playerTurn].doPlayerTurn(players[0]);
 				}
 
 				if ( detectWin.detectLadder(players[0].getPlayerToken(), 
 						players[1].getPlayerToken(), playerMove) )
 				{
 					printGameOver( players[playerTurn], board);
 					setGameOver(true);
 				}
 
 				playerTurn = 1;*/
 
 			} else {
 
 				Calendar calStartTime = Calendar.getInstance();		// obtain start time
 				
 				nextAIPlayerMove(players, detectWin, board, getPlayerTurn());
 				
 				Calendar calEndTime	= Calendar.getInstance();		// obtain end time
 				System.out.printf("AI Move Duration = %.2f %s\n", 	// output move calculation duration
 						(float) (calEndTime.getTimeInMillis() - calStartTime.getTimeInMillis()) / 1000, 
 						"seconds"); 
 				
                 //////////////////////////////////////////////////////////////////debug first
 				/*	Point AIPlayerMove = ((AIPlayer) players[1]).doAIPlayerTurn(players[1]);
 
 				if ( players[1].setDisc(AIPlayerMove.y, AIPlayerMove.x) == false) 
 				{
 					((AIPlayer) players[1]).doAIPlayerTurn(players[1]);
 				}
 
 				if ( detectWin.detectLadder(players[1].getPlayerToken(), 
 						players[0].getPlayerToken(), AIPlayerMove) )
 				{
 					printGameOver( players[playerTurn], board);
 					setGameOver(true);
 				}
 
 				playerTurn = 0;*/
 			}
 		}
 
 		System.out.printf("Game over!\n");
 	}
 
 	private void startComputerHumanGame(Board board, Player [] players, WinPatternStrategy detectWin){
 
 		setGameOver(false);	
 		setPlayerTurn(0);
 
 		while (!isGameOver()) {
 
 			board.printBoard();				// display board state
 
 			// TODO: add win condition check
 			if (board.isBoardFull()) {		// check win condition or draw
 
 				System.out.printf("The game ended in a draw!\n");
 				setGameOver(true);
 			}
 
 			if (getPlayerTurn() == 0) {	
 
 				Calendar calStartTime = Calendar.getInstance();		// obtain start time
 				
 				nextAIPlayerMove(players, detectWin, board, getPlayerTurn());
 				
 				Calendar calEndTime	= Calendar.getInstance();		// obtain end time
 				System.out.printf("AI Move Duration = %.2f %s\n", 	// output move calculation duration
 						(float) (calEndTime.getTimeInMillis() - calStartTime.getTimeInMillis()) / 1000, 
 						"seconds"); 
 
 			} else {
 				
 				nextPlayerMove(players, detectWin, board, getPlayerTurn());
 			
 			}
 		}
 
 		System.out.printf("Game over!\n");
 	}
 
 	private void nextPlayerMove(Player[] players, WinPatternStrategy detectWin, Board board, int playerTurn){
 		
 		Point playerMove = new Point();
 
 		while (!(players[playerTurn].setDisc(playerMove.y, playerMove.x)) )
 		{
 			playerMove = players[playerTurn].doPlayerTurn(players[playerTurn]);
 		}
 
		int opPlayerTurn = (playerTurn == 0) ? 1 : 0;
 		if ( detectWin.detectLadder(players[playerTurn].getPlayerToken(), 
				players[opPlayerTurn].getPlayerToken(), playerMove) ){
 
 			printGameOver( players[playerTurn], board);
 			setGameOver(true);
 		}
 
 		setNextPlayerTurn(playerTurn);
 
 		// return AIPlayerMove;
 	}    
 
 
 	private void nextAIPlayerMove(Player[] players, WinPatternStrategy detectWin, Board board, int playerTurn){
 		
 		Point AIPlayerMove = ((AIPlayer) players[playerTurn]).doAIPlayerTurn((AIPlayer)players[playerTurn], players[getNextPlayerTurn(playerTurn)], searchList);
 		
 		if ( players[playerTurn].setDisc(AIPlayerMove.y, AIPlayerMove.x) == false) 
 		{
 			((AIPlayer) players[playerTurn]).doAIPlayerTurn((AIPlayer)players[playerTurn], players[getNextPlayerTurn(playerTurn)], searchList);
 		}
 		
		int opPlayerTurn = (playerTurn == 0) ? 1 : 0;
 		if ( detectWin.detectLadder(players[playerTurn].getPlayerToken(), 
				players[opPlayerTurn].getPlayerToken(), AIPlayerMove) )
 		{
 
 			printGameOver( players[playerTurn], board);
 			setGameOver(true);
 		}
 
 		setNextPlayerTurn(playerTurn);
 
 		// return AIPlayerMove;
 	}
 
 	public int displayMainMenu() {
 
 		System.out.println("Welcome to the Polarized Ladders Game!");			
 		System.out.println("Please select game type:");
 		System.out.println("(1) Human vs Human");
 		System.out.println("(2) Human vs AI");
 		System.out.println("(3) AI vs Human");
 		System.out.println("(4) Quit!");
 
 		Scanner input 	= new Scanner(System.in);	// command line input scanner
 		return input.nextInt();
 	}
 
 	Player[] setGameType(int gameType, Board board, WinPatternStrategy detectWin){
 
 		Player[] players = new Player[2];
 
 		if (gameType == 1) {
 
 			// instantiate human players
 			players[0] = new Player("Player One", 'o', 22, board);
 			players[1] = new Player("Player Two", Character.toChars(8226)[0], 22, board);
 		}
 
 		if(gameType == 2){
 
 			// instantiate human player(1) and AIPlayer(2)
 			players[0] = new Player("Player One", 'o', 22, board);
 			players[1] = new AIPlayer("Player Two", Character.toChars(8226)[0], 22, board);
 		}
 
 		if(gameType == 3){
 
 			// instantiate AIPlayer(1) human player(2) 
 			players[0] = new AIPlayer("Player One", 'o', 22, board);
 			players[1] = new Player("Player Two", Character.toChars(8226)[0], 22, board);
 		}
 
 		//if(type == 4){
 		else if (gameType == 4) {
 
 			System.out.println("Quiting Game.");
 			System.exit(0);
 
 		} else if (gameType > 4)  {
 
 			System.out.println("Game type not availabe at this time.");
 			System.exit(0);
 		}
 
 		return players;
 
 	}
 
 	private static void printGameOver(Player player, Board board){
 
 		board.printBoard();
 		System.out.printf( "%s Wins the Game! \n", player.getPlayerName() );
 	}
 
 	public boolean isGameOver() {
 		return gameOver;
 	}
 
 	public void setGameOver(boolean gameOver) {
 		this.gameOver = gameOver;
 	}
 
 	public int getPlayerTurn() {
 		return playerTurn;
 	}
 
 	public void setPlayerTurn(int playerTurn) {
 		this.playerTurn = playerTurn;
 	}
 
 	public void setNextPlayerTurn(int playerTurn){
 		if(playerTurn == 0)
 			setPlayerTurn(1);
 		else 
 			if(playerTurn == 1)
 				setPlayerTurn(0);
 	}
 	public int getNextPlayerTurn(int playerTurn){
 		int nextPlayer=0;
 		if(playerTurn == 0)
 			nextPlayer =1;
 		else 
 			if(playerTurn == 1)
 				nextPlayer =0;
 		
 		return nextPlayer;
 	}
 
 	/*	public static void main(String[] args) {
 
 
     PLGame game = new PLGame();
     Board board		 = new Board();
 	WinPatternStrategy detectWin= new WinPatternStrategy(board);
 	int gameType = game.displayMainMenu();
     Player[] players = game.setGameType(gameType, board, detectWin);
 	game.startGame(board, players, detectWin);
 
 	}*/
 }
