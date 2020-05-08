 package implementations.ai;
 
 import api.GameLogic;
 import api.struct.Board;
 import api.PlayerLogic;
 import internal.util.UnitConversion;
 import java.awt.Point;
 import java.util.List;
 import java.util.Random;
 
 /**
  *
  * @author ablaine
  */
 public class SlowRandomPlayer extends PlayerLogic {
 	private static final Random rand = new Random();
 
 	@Override
 	public Point makeMove(Board board, List<Point> validMoves) {
 		while (GameLogic.getRemainingTime() > UnitConversion.secondToNanosecond(1)) {
			//Do nothing
 		}
 		return validMoves.get(rand.nextInt(validMoves.size()));
 	}
 }
