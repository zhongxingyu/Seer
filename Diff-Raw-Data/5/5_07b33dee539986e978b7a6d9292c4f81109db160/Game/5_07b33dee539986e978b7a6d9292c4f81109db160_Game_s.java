 package game;
 
 import game.ex.ColumnExceeded;
 import game.ex.NonexistingColumn;
 
 public class Game {
 
 	private int width = 7;
 	private boolean playerOneTurn = true;
 	private boolean emptyBoard = true;
 	private int[] columnCount = new int[width+1];	
 	private int p1AllignedVertical = 0;
 	
 	public boolean isEmpty() {
 		return emptyBoard;
 	}
 
 	public boolean PlayerOnesTurn() {
 		return playerOneTurn;
 	}
 
 	public boolean play(int column) throws ColumnExceeded, NonexistingColumn {
 		
 		isValidColumn(column);
 		emptyBoard = false;
 		
 		if (playerOneTurn) ++p1AllignedVertical;
 		
 		switchTurn();
 		addChipToColumn(column);
 		
 		return playerOneTurn;
 	}
 
 	private void isValidColumn(int column) throws NonexistingColumn {
 		if (column>width || column<1 ) {
 			throw new NonexistingColumn();
 		}
 	}
 
 	private void addChipToColumn(int column) throws ColumnExceeded{
 		columnCount[column] = ++columnCount[column];
 		if (columnCount[column]>6) {
 			throw new ColumnExceeded();
 		}
 	}
 
 	private void switchTurn() {
 		playerOneTurn=!playerOneTurn;
 	}
 
 	public void restart() {
 		emptyBoard = true;
 	}
 
 	public boolean fourOnTheLine() {
 		if (p1AllignedVertical == 4) return true;
 		return false;
 	}
 	
 }
