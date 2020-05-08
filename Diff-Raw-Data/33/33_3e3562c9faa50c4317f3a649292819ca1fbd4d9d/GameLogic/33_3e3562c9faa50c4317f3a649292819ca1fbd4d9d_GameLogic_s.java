 package api;
 
 import api.struct.Board;
 import api.struct.Move;
 import api.struct.FlipList;
 import api.struct.MoveList;
 import java.awt.Point;
 import java.util.LinkedList;
 import java.util.List;
 import jig.engine.GameClock.Alarm;
 
 
 /**
  *
  * TODO: Allow for passing in playerLogic and auto getting state..?
  *
  * @author ablaine
  */
 public class GameLogic {
 	private static Alarm alarm;
 
 	public static void _init(Alarm alarm) {
 		if (GameLogic.alarm == null) {
 			GameLogic.alarm = alarm;
 		}
 	}
 	
 	public static void makeMove(Board board, Move move) {
 		FlipList list = new Helper(board, move).getFlipped();
 		State[][] grid = board.getGrid();
 		for (Point p : list) {
 			grid[p.x][p.y] = list.getState();
 		}
 		board.notifyObservers(list);
 	}
 	
 	public static MoveList getValidMoves(Board board, State playerState) {
 		MoveList result = new MoveList();
 		for (int x = 0; x < Board.SIZE; x++) {
 			for (int y = 0; y < Board.SIZE; y++) {
 				Move move = new Move(playerState, new Point(x, y));
 				if (isValidMove(board, move)) {
 					result.add(move);
 				}
 			}
 		}
 		return result;
 	}
 
 	public static boolean isValidMove(Board board, Move move) {
 		return new Helper(board, move).getFlipped().size() > 0;
 	}
 
 	// These two seem out of place since they are just always for the current
 	// player. Hmm..
 	/**
 	 * Returns the current player's remaining time to make a move. Result is
 	 * in nanoseconds.
 	 * @return
 	 */
 	public static long getRemainingTime() {
 		return alarm.remainingTime();
 	}
 }
 
 class Helper {
 	private final Point loc;
 	private final State state;
 	private final Board board;
 	private FlipList flipped = null;
 
 	public Helper(Board board, Move move) {
 		this(board, move.getPlayerState(), move.getLocation());
 	}
 
 	public Helper(Board board, State state, Point location) {
 		this.loc = location;
 		this.state = state;
 		this.board = board;
 	}
 
 	public FlipList getFlipped() {
 		if (flipped == null) {
 			flipped = new FlipList(state);
 			if (isWithinBounds() && board.getState(loc) == State.EMPTY) {
 				followPathInit(new Point(-1, +1));//Left	Up
 				followPathInit(new Point( 0, +1));//		Up
 				followPathInit(new Point(+1, +1));//Right	Up
 				followPathInit(new Point(+1,  0));//Right
 				followPathInit(new Point(+1, -1));//Right	Down
 				followPathInit(new Point( 0, -1));//		Down
 				followPathInit(new Point(-1, -1));//Left	Down
 				followPathInit(new Point(-1,  0));//Left
 
 				if (flipped.size() > 0) {
 					flipped.add(loc);
 				}
 			}
 		}
 		return flipped;
 	}
 
 	private void followPathInit(Point dir) {
 		followPath(loc.x + dir.x, loc.y + dir.y, dir, new LinkedList<Point>());
 	}
 
 	private void followPath(int locX, int locY, Point dir, List<Point> potentialFlips) {
 		if (board.isWithinBounds(locX, locY)) {
 			State boardState = board.getState(locX, locY);
 			if (boardState == state.getOpposite()) {
 				potentialFlips.add(new Point(locX, locY));
 				followPath(locX + dir.x, locY + dir.y, dir, potentialFlips);
 			} else if (boardState == state) {
 				flipped.addAll(potentialFlips);
 			}
 		}
 	}
 
 	private boolean isWithinBounds() {
 		return board.isWithinBounds(loc);
 	}
 }
