 package tinf11bc.kbs.yavalath.logic;
 
 import javax.crypto.spec.PSource;
 
 import tinf11bc.kbs.yavalath.logic.AI;
 import tinf11bc.kbs.yavalath.logic.Player;
 import tinf11bc.kbs.yavalath.logic.YavalathException;
 import tinf11bc.kbs.yavalath.gui.GuiFactory;
 
 /**
  * @author Chris, Stephan
  *
  */
 public class Yavalath {
 			
 	static int[][] setUpBoard(){
 		int[][] board = {{ -1, -1, -1, -1, 0, 0, 0, 0, 0},
 			 	            { -1, -1, -1, 0, 0, 0, 0, 0, 0},
 				               { -1, -1, 0, 0, 0, 0, 0, 0, 0},
 				                  { -1, 0, 0, 0, 0, 0, 0, 0, 0},
 				                     { 0, 0, 0, 0, 0, 0, 0, 0, 0},
 				                      { 0, 0, 0, 0, 0, 0, 0, 0, -1},
 				                        { 0, 0, 0, 0, 0, 0, 0, -1, -1},
 				                          { 0, 0, 0, 0, 0, 0, -1, -1, -1},
 				                            { 0, 0, 0, 0, 0, -1, -1, -1, -1}};
 		return board;
 	}
 	
 	public static void newGame(int[] newPlayers) throws YavalathException{
 
 		Player[] player = new Player[3];
 		int numberOfPlayers = 0;	
 		int numberOfMoves = 0;
 		
 		int[][] board = setUpBoard();
 		
 		for(int n = 0; n <=2; n++){
 			switch(newPlayers[n]){
 			case(1):
 				player[n] = new Player(n+1);
 				numberOfPlayers++;
 				break;
 			case(2):
 				player[n] = new AI(n+1);
 				numberOfPlayers++;
 				break;
 			default:
 				break;
 			}
 		}
 
 		drawBoard(board);
 		
 		playGame(board, numberOfPlayers, player, numberOfMoves);
 	}
 	
 	public static void playGame(int[][] board, int numberOfPlayers, Player[] player, int numberOfMoves) throws YavalathException {
 		int gameState = 0; //0: playing; 1: Player 1 won; 2: Player 2 won; 3: Player 3 won;	
 		   //10: draw; 11:Player 1 out; 12: Player 2 out; 13: Player 3 out; 
 		
 		while(gameState == 0){
 			for(int i = 0; i < numberOfPlayers;i++){
 				
 				System.out.println("GameState: " + gameState + "\nNumber of Moves: " + numberOfMoves + "\nPlayer's Turn: " + player[i].getPlayerNumber());
 				
 				gameState = addStone(board, player[i].getPlayerNumber(), player[i].makeMove(board));
 				numberOfMoves++;
 
 				
 				drawBoard(board);
 				
 				if(numberOfMoves == 61){
 					gameState = 10;
 					System.out.println("It's a draw!");
 					break;
 				}
 				
 				if(gameState > 10) {
 					numberOfPlayers--;
 					if(numberOfPlayers == 1) {
 						if(gameState - 10 == player[0].getPlayerNumber()) {
 							gameState = player[1].getPlayerNumber();
 						}
 						else {
 							gameState = player[0].getPlayerNumber();
 						}
 						break;
 					}
 					else {
 						for(int j = 1; j < 3; j++) {
 							if(player[j].getPlayerNumber() > gameState - 10) {
 								player[j-1] = player[j];
 							}
 						}
 						System.out.println("Player " + (gameState - 10) + " is out!");
 						gameState = 0;
 						i--;
 					}
 				}
 			}
 		}
 		if(gameState != 10){
 			System.out.println("Player " + gameState + " won!");
 		}
 	}
 	
 	private static void drawBoard(int[][] board){
 		for(int i = 0; i < 9; i++){
 			for(int j = 0; j < 9; j++){
 				System.out.print(board[i][j]+" ");
 			}	
 			System.out.println();
 		}
 	}
 	
 	private static int addStone(int[][] board, int playerNumber,int position) throws YavalathException{		
 		if(board[position/10][position%10] == 0){
 			board[position/10][position%10] = playerNumber;
 		}else{
			System.err.println("NOOOOOOOOOO!");
			System.err.println(position);
 			throw new YavalathException();
 		}
 		 return checkGameState(board, playerNumber, position);	
 	}
 	
 	private static int checkGameState(int[][] board, int playerNumber, int position) throws YavalathException {
 				
 		int x = position%10;
 		int y = position/10;
		System.out.println(x+"/"+y);
 		for(int i = 0; i+2 < 9; i++){
 				try{
 					if(board[x][i] == playerNumber){						
 						if(	i < 6 && board[x][i] == board[x][i+1] &&
 							board[x][i] == board[x][i+2] &&
 							board[x][i] == board[x][i+3]){
 								return playerNumber;
 						}else{
 							if(board[x][i] == board[x][i+1] &&
 								board[x][i] == board[x][i+2] && i+3 < 9){
 								return playerNumber+10;
 							}
 						}
 					}
 					if(board[i][y] == playerNumber){
 						if(	i < 6 && board[i][y] == board[i+1][y] &&
 							board[i][y] == board[i+2][y] &&
 							board[i][y] == board[i+3][y]){
 								return playerNumber;
 						}else{
 							if(board[i][y] == board[i+1][y] &&
 								board[i][y] == board[i+2][y] && i+3 < 9){
 								return playerNumber+10;
 							}
 						}
 					}
 				}catch(Exception e){
					System.out.println("x: "+x+";i: "+i);
 					System.err.println("Error in Win/Lose detection (h/v)!");
 					throw new YavalathException();
 				}
 			}
 			
 			int t = 0,r = x+y;
 			if(r > 8){
 				t = r - 8;
 				r = 8;
 			}
 			try{
 				for(; t < 8 && r-2 >= 0; t++,r--){
 					if(r-3 <= 0 && t+3 <= 8 && board[t][r] > 0){
 						if(	board[t][r] == board[t+1][r-1] &&
 							board[t][r] == board[t+2][r-2]){
 							if(r-3>=0 && board[t][r] == board[t+3][r-3]){
 								return playerNumber;
 							}else{
 								return playerNumber+10;
 							}
 						}
 					}
 				}
 			}catch(Exception e){
 				System.err.println("Error in Win/Lose detection (diagonal)!");
 				throw new YavalathException();
 			}
 			return 0;
 	}
 
 
 	/**
 	 * @param args
 	 * @throws YavalathException 
 	 */
 	public static void main(String[] args) throws YavalathException {
 		boolean playing = true;
 		while(playing  == true){
 			//menu with choosing:
 			//					-how many players? (2/3)
 			//					-player/AI 
 			int[] players = {2,2,2};
 			
 			newGame(players);
 			playing = false;
 		}
 	}
 
 }
