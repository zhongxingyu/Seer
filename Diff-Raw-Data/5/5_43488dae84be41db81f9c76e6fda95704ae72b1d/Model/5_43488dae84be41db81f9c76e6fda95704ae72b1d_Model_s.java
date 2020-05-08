 package gui;
 
 import java.awt.Color;
 import java.io.File;
 import java.io.FileNotFoundException;
 
 import custom_java_utils.CheckFailException;
 import player_utils.*;
 import board_utils.*;
 
 public class Model {
 	
 	private GoPlayingBoard currentBoard;
 	private LegalMovesChecker checker;
 	private boolean[][] legalMoves;
 	private BoardHistory history;
 	private MinimaxGoSolver minimax;
 	private GuiBoardPlay gui;
 	
 	public Model(GuiBoardPlay g) {
 		gui = g;
 		currentBoard = new GoPlayingBoard();
 		history = BoardHistory.getSingleton();
 		history.add(currentBoard);
 		checker = new LegalMovesChecker(currentBoard);
 	}
 	
 	public Model(File fileName, GuiBoardPlay g) throws FileNotFoundException, CheckFailException {
 		BoardHistory.wipeHistory();
 		gui = g;
 		currentBoard = new GoPlayingBoard(fileName);
 		history = BoardHistory.getSingleton();
 		history.add(currentBoard);
 		checker = new LegalMovesChecker(currentBoard);
 		legalMoves = checker.getLegalityArray();
 		if(currentBoard.getNextPlayer() == Player.COMPUTER)
 			computerMove();
 	}
 	
 	public void addStone(int x, int y) {
 		currentBoard.setCellAt(x, y, new GoCell(currentBoard.toPlayNext(), x, y));
 		currentBoard.oppositeToPlayNext();
		currentBoard.oppositePlayer();
 		checker = new LegalMovesChecker(currentBoard);
 		legalMoves = checker.getLegalityArray();
		if(currentBoard.getNextPlayer() == Player.COMPUTER && !minimax.isPositionTerminal(currentBoard)){
 			computerMove();
 		}
 		removeOpponent(x, y);
 	}
 	
 	public void computerMove(){
 		minimax = new MinimaxGoSolver(currentBoard, currentBoard.getTarget());
 		GoCell decision = null;
 		try {
 			decision = minimax.minimaxDecision();
 			if(decision != null)
 				System.out.println(decision.x() + " " + decision.y() + " " + decision);
 			else
 				System.out.println("null");
 		} catch(CheckFailException e){
 			System.out.println("Game is finished.");
 			e.printStackTrace();
 		}
 		if(decision != null) {
 			addStone(decision.x(), decision.y());
 		}
 		gui.repaint();
 	}
 	
 	public boolean isMoveLegal(int x, int y) {
 		if(legalMoves != null){
 			return legalMoves[x][y];
 		} else 
 			return true;
 	}
 	
 	public void removeOpponent(int x, int y)  {
 		boolean isAnyKilled = false;
 		isAnyKilled = checker.captureOponent(currentBoard.getCellAt(x, y));
 		try {
 			if(isAnyKilled) {
 				currentBoard = checker.getNewBoard();
 				checker = new LegalMovesChecker(currentBoard);
 				legalMoves = checker.getLegalityArray();
 			}
 			else 
 				//this method will be called on each move, so history will be updated each time when
 				//there will be no stones killed.
 				history.add(currentBoard);
 		} catch(Exception e){
 			System.out.println("new board = old board");
 		}
 		
 	}
 	
 	public int getTotalNumberOfStones(){
 		return currentBoard.getCountPiecesOnBoard();
 	}
 	
 	public int getBlackNumberOfStones(){
 		return currentBoard.getNumberofBlackStones();
 	}
 	
 	public int getWhiteNumberOfStones(){
 		return currentBoard.getNumberOfWhiteStones();
 	}
 	
 	public void recountBlackStones() {
 		currentBoard.countAndSetBlackStones();
 	}
 	
 	/**
 	 * Returns the current board Stone layout
 	 * @return A double dimension array of Stones
 	 */
 	public Stone[][] getCurrentBoardLayout() {
 		GoCell[][] currentLayout = getCurrentBoard().getBoard();
 		Stone[][] newLayout = new Stone[getCurrentBoard().getWidth()][getCurrentBoard().getHeight()];
 		for (int i = 0; i < getCurrentBoard().getHeight(); i++)
 			for (int j = 0; j < getCurrentBoard().getWidth(); j++)
 				newLayout[i][j] = currentLayout[i][j].getContent();
 		return newLayout;
 	}
 	
 	/**
 	 * Returns a clone of the current board
 	 * @return a clone of current board
 	 */
 	public GoPlayingBoard getCurrentBoard() {
 		return currentBoard.clone();
 	}
 	
 	public GoCell getTarget() {
 		return currentBoard.getTarget();
 	}
 	/**
 	 * Creates a new file and populates it with current board.
 	 * @param file full path of the file where to save it
 	 * @throws FileNotFoundException 
 	 */
 	public void toFile(File file) throws FileNotFoundException{
 		currentBoard.toFile(file);
 	}
 	
 	public void undoMove() {
 		history.undoMove();
 		GoPlayingBoard last = history.getLastMove();
 		if (last != null) {
 			currentBoard = last;
 			checker = new LegalMovesChecker(currentBoard);
 			legalMoves = checker.getLegalityArray();
 		}
 	}
 	
 	public void redoMove() {
 		history.redoMove();
 		currentBoard = history.getUndoMove();
 	}
 
 }
