 package se.chalmers.towerdefence.model;
 
 
 public class GameBoard {
 
 	private final ISquare[][] gameBoard;
 	
 	public GameBoard(ISquare[][] gameBoard){
 		this.gameBoard=gameBoard;		
 	}
 	
 	public ISquare[][] getGameBoard(){
 		//TODO;
 		return gameBoard;
 	}
 
 	public int getTile(int mouseX) {		
 		return mouseX/40;
 	}
 	public boolean isBlocked(int x, int y) {
 		return gameBoard[x][y].isBlocked();
 	}
 	public void addTower(int x, int y, AbstractTower tower) {
 			gameBoard[x][y] = new TowerSquare(x, y, tower);
 	}
 	
 	public ISquare getSquare(int x, int y) {
 		return gameBoard[x][y];
 	}
 
 }
