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
	private static final int delay = 1;
 
 	@Override
 	public Point makeMove(Board board, List<Point> validMoves) {
 		while (GameLogic.getRemainingTime() > UnitConversion.secondToNanosecond(1)) {
			if (delay > 0) {
				if (GameLogic.getElapsedTime() >= UnitConversion.secondToNanosecond(delay)) {
					break;
				}
			}
 		}
 		return validMoves.get(rand.nextInt(validMoves.size()));
 	}
 }
