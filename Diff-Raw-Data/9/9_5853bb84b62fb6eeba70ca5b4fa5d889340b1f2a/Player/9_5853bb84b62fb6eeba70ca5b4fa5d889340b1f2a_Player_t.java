 package cell.g4;
 
 import java.util.ArrayList;
 import java.util.Random;
 
 import cell.g4.movement.MoveAlgo;
 import cell.g4.movement.ShortestPathMove;
 import cell.g4.trade.MergeTrade;
 import cell.g4.trade.TradeAlgo;
 
 public class Player implements cell.sim.Player {
 	private static int versions = 0;
 	public static int version = ++versions;
 	private Random rnd = new Random();
 	
 	// the map
 	private Board board = null;
 	// our sack
 	private Sack sacks;
 	
 	// our location
 	private int[] loc = new int[2];
 	// movement algorithm
 	private MoveAlgo movement;
 	// trading algorithm
 	private TradeAlgo trading;
 	
 	private Game game = null;
 
 	@Override
 	public Direction move(int[][] map, int[] location, int[] sack,
 			int[][] players, int[][] traders) {
 		// create a map object the first time we move 
 		if (board == null) {
 			board = new Board(map);
 //			sacks = new Sack(sack,board);
 			sacks = new DynamicSack(sack,board);
 			
 			trading = new MergeTrade(board, sacks);
 			movement = new ShortestPathMove(board, sacks);
 			
 			game = Game.initGame(location, players);
 		}
 		// routines
 		loc[0] = location[0]; loc[1] = location[1];		
 		sacks.update(sack);
 		game.updateTrades(players, traders);
 		
 		Direction dir = movement.move(location, players, traders);
 		dir = validateMove(dir);
 		
 		int[] new_location = board.nextLoc(location, dir);		
 		int color = board.getColor(new_location);		
 		sacks.decrease(color);
 		return dir;
 	}
 
 	private Direction validateMove(Direction nextdir) {
 		Direction dirs[] =
 			{Direction.W,  Direction.E,
 			 Direction.NW, Direction.N,
 			 Direction.S, Direction.SE};
 		ArrayList<Direction> possible = new ArrayList<Direction>();
 		
 		// check if player can move
 		int possible_moves = 0;
 		for (Direction dir : dirs) {
 			int[] new_location = board.nextLoc(loc, dir);
 			int color = board.getColor(new_location);
 			if (color >= 0 && sacks.getStock(color) > 0) {
 				possible_moves++;
				if (nextdir == dir)
					return nextdir;
				else
					possible.add(dir);
 			}
 		}
 		if (possible_moves == 0) {
 			System.out.println("We are dead");
 			return nextdir;
 		}
 		else
 			return possible.get(rnd.nextInt(possible.size()));
 	}	
 	
 	
 	@Override
 	public void trade(double[] rate, int[] request, int[] give) {
 		trading.trade(rate, request, give);
 	}
 	
 	@Override
 	public String name() {
 		return "G4" + (versions > 1 ? " v" + version : "");
 	}
 }
