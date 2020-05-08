 /**
  * 
  */
 package reversi;
 
 import reversi.Tile.State;
 
 /**
  * @author kAG0
  *
  */
 public class AI {
 	private final static double FORFEIT_WEIGHT = 1;
 	private static final double MOBILITY_WEIGHT = 1;
 	private static final double FRONTIER_WEIGHT = 1;
 	private static final double STABILITY_WEIGHT = 1;
 	private static final double SCORE_WEIGHT = 1;
 	private static final int MAX_DEPTH = 5;
 	/**
 	 * 
 	 * @param board
 	 * @param player
 	 * @return the best move the given player can make on this board
 	 */
 	public static Move getBestMove(Board board, Tile.State player){
 		// TODO Auto-generated method stub
 		return new Move(0,0, Tile.State.BLACK);
 	}
 	/**
 	 * 
 	 * @param board
 	 * @param player
 	 * @return the best move the given player can make on this board
 	 */
 	private static Move getBestMove(Board node, int depth, Move a, Move b, State maximizingPlayer){
 		// TODO Auto-generated method stub
 		return new Move(0,0, Tile.State.BLACK);
 		
 	}
 	private static int rateBoard(Board board){
 		// TODO Auto-generated method stub
 		return 0;
 	}
 	public Coordinate<Integer,Integer> getMove(Tile.State player) {
 		
 		//Search for next best move for player
 		
 		//Coordinate move = new Coordinate<Integer,Integer>(1,2);
 		//return move;
 		
 		return null;
	}public Coordinate<Integer,Integer> getMove(Tile.State player) {
		
		//Search for next best move for player
		
		//Coordinate move = new Coordinate<Integer,Integer>(1,2);
		//return move;
		
		return null;
 	}
 }
