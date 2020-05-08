 package player_utils;
 
 import java.util.HashMap;
 import java.util.LinkedList;
 
 import board_utils.GoPlayingBoard;
 
 /**
  * A singleton to keep the history of all boards that have been played
  */
 public class BoardHistory {
 	private static BoardHistory instance;
 	private static HashMap<Integer, LinkedList<GoPlayingBoard>> boards;
 
 	/**
 	 * Private constructor for the singleton
 	 */
 	private BoardHistory() {
 		boards = new HashMap<Integer, LinkedList<GoPlayingBoard>>();
 	}
 
 	/**
 	 * @return the instance of the singleton
 	 */
 	public static BoardHistory getSingleton() {
 		if (instance == null) {
 			instance = new BoardHistory();
 		}
 		return instance;
 	}
 
 	/**
 	 * Adding the given board to the board history by making a deep copy of it.
 	 * @param board the board to be added
 	 */
 	public void add(GoPlayingBoard board) {
 		if (!boards.containsKey(board.getCountPiecesOnBoard())) {
 			LinkedList<GoPlayingBoard> list = new LinkedList<GoPlayingBoard>();
 			list.add(board.clone());
 			boards.put(board.getCountPiecesOnBoard(), list);
 		} else {
 			LinkedList<GoPlayingBoard> list = boards.get(board
 					.getCountPiecesOnBoard());
 			list.add(board.clone());
 		}
 	}
 
 	/**
	 * Adding the given board to the board history by making a deep copy of it.
	 * @param board the board to be added
 	 */
 	public void remove(GoPlayingBoard board) {
 		if (!boards.containsKey(board.getCountPiecesOnBoard())) {
 			return;
 		} else {
 			LinkedList<GoPlayingBoard> list = boards.get(board
 					.getCountPiecesOnBoard());
 			list.remove(board);
 		}
 	}
 
 	/**
 	 * A method to determine if the given board is already in the history
 	 * @param board a board to be checked
 	 * @return true if the given board is in the history, false - otherwise
 	 */
 	public boolean hasBeenPlayed(GoPlayingBoard board) {
 		if (!boards.containsKey(board.getCountPiecesOnBoard())) {
 			return false;
 		}
 		for (GoPlayingBoard one : boards.get(board.getCountPiecesOnBoard())) {
 			if (one.equals(board)) {
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	public static void wipeHistory() {
 		instance = null;
 	}
 
 }
