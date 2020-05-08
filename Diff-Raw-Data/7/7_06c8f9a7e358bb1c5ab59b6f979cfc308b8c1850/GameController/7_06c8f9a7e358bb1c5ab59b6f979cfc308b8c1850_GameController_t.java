 package edu.touro.cooptetris;
 
 import java.util.ArrayList;
 
 import javax.inject.Inject;
 
 import edu.touro.cooptetris.pieces.Piece;
 import edu.touro.cooptetris.pieces.Square;
 
 public class GameController {
 
 	private Board board;
 	private PiecesList list;
 	private PieceFactory pieceFactory;
 	private GameStateListener gameStateListener;
 	private DropTimer timer;
 	private ArrayList<Level> levels;
 	private int score;
 	private int currLevel;
 	private Piece nextPiece;
 
 	@Inject
 	public GameController(Board board, PiecesList list,
 			PieceFactory pieceFactory) {
 		this.board = board;
 		this.list = list;
 		this.pieceFactory = pieceFactory;
 		setNextPiece();
 		levels = new ArrayList<Level>();
 		for (int i = 0; i < 10; i++) {
 			levels.add(new Level(i, 1000 - (i * 100)));
 		}
 		currLevel = 1;
 		timer = new DropTimer(400);
 	}
 
 	public void increaseSpeed() {
 		int currIncrement = timer.getTimeIncrement();
 		timer.setTimeIncrement(currIncrement - 30);
 	}
 
 	public void lineCompleted(int numLines) {
 		switch (numLines) {
 		case 1:
 			setScore(score + 10);
 			break;
 		case 2:
 			setScore(score + 25);
 			break;
 		case 3:
 			setScore(score + 50);
 			break;
 		case 4:
 			setScore(score + 100);
 			break;
 		}
 
 	}
 
 	public void movePieces() {
 
 		if (timer.isTimeToDrop()) {
 			boolean landed = false;
 
 			for (Piece p : list) {
 
 				if (!board.willCollideWithFloorVertical(p)
 						&& !board.willCollideWithLandedPieceVertical(p)) {
 					p.moveDown();
 				} else {
 					board.landPiece(p);
 					gameStateListener.onHitFloor();
 					int numRows = board.checkFullRowsOfPiece(p);
 					landed = true;
 					if (numRows > 0) {
 						gameStateListener.onCompleteLine(numRows);
 					}
 				}
 			}
 			if (landed) {
 				list.clear();
 				if (!board.isFull() && score < 9999) {
 					addNewPiece();
 				} else {
 					gameStateListener.onGameOver();
 				}
 			}
 
 		}
 	}
 
 	public void addNewPiece() {
 
 		list.add(nextPiece);
 		Piece tempPiece = nextPiece;
 		setNextPiece();
 		gameStateListener.onNewPiece(tempPiece);
 
 	}
 
 	public void setNextPiece() {
 		this.nextPiece = pieceFactory.getNextPiece(Board.NUM_COLUMNS
 				* Square.SIDE / 2, 0);
 	}
 
 	public Piece getNextPiece() {
 		return nextPiece;
 	}
 
 	public int getCurrLevel() {
 		return currLevel;
 	}
 
 	public void setCurrLevel(int currLevel) {
 		this.currLevel = currLevel;
 	}
 
 	public int getScore() {
 		return score;
 	}
 
 	public void setScore(int score) {
 		this.score = score;
 
 	}
 
 	public void setGameStateListener(GameStateListener gameStateListener) {
 		this.gameStateListener = gameStateListener;
 	}
 
 	public Board getBoard() {
 		return board;
 	}
 
 }
