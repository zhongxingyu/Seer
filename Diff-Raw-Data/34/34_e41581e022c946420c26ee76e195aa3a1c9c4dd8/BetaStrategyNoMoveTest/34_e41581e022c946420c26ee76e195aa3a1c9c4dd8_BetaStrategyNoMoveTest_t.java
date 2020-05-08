 package strategy.game.version.beta;
 
 import static org.junit.Assert.assertFalse;
 import static strategy.common.PlayerColor.BLUE;
 import static strategy.common.PlayerColor.RED;
 import static strategy.game.common.PieceType.FLAG;
 
 import java.util.ArrayList;
 import java.util.Collection;
 
 import org.junit.Test;
 
 import strategy.common.PlayerColor;
 import strategy.common.StrategyException;
 import strategy.game.common.Location2D;
 import strategy.game.common.Piece;
 import strategy.game.common.PieceLocationDescriptor;
 import strategy.game.common.PieceType;
 import strategy.game.version.beta.board.BetaBoardRuleSet;
 import strategy.game.version.beta.combat.BetaCombatRuleSet;
 import strategy.game.version.beta.move.BetaMoveRuleSet;
 import strategy.game.version.move.MoveRuleSet;
 
 public class BetaStrategyNoMoveTest {
 
 	private Collection<PieceLocationDescriptor> redConfiguration;
 	private Collection<PieceLocationDescriptor> blueConfiguration;
 
 	@Test
 	public void noMoreMoves() throws StrategyException{
 		//Red configuration
 		redConfiguration = new ArrayList<PieceLocationDescriptor>();
 
 		//Red pieces
 		addToConfiguration(FLAG, RED, 0, 1);
 		addToConfiguration(FLAG, RED, 0, 0);
 		addToConfiguration(FLAG, RED, 1, 0);
 		addToConfiguration(FLAG, RED, 2, 0);
 		addToConfiguration(FLAG, RED, 3, 0);
 		addToConfiguration(FLAG, RED, 4, 0);
 		addToConfiguration(FLAG, RED, 5, 0);
 		addToConfiguration(FLAG, RED, 1, 1);
 		addToConfiguration(FLAG, RED, 2, 1);
 		addToConfiguration(FLAG, RED, 3, 1);
 		addToConfiguration(FLAG, RED, 4, 1);
 		addToConfiguration(FLAG, RED, 5, 1);
 
 		//Blue configuration
 		blueConfiguration = new ArrayList<PieceLocationDescriptor>();
 
 		//Blue pieces
 		addToConfiguration(FLAG, BLUE, 5, 4);
 		addToConfiguration(FLAG, BLUE, 0, 5);
 		addToConfiguration(FLAG, BLUE, 1, 5);
 		addToConfiguration(FLAG, BLUE, 2, 5);
 		addToConfiguration(FLAG, BLUE, 3, 5);
 		addToConfiguration(FLAG, BLUE, 4, 5);
 		addToConfiguration(FLAG, BLUE, 5, 5);
 		addToConfiguration(FLAG, BLUE, 1, 4);
 		addToConfiguration(FLAG, BLUE, 2, 4);
 		addToConfiguration(FLAG, BLUE, 3, 4);
 		addToConfiguration(FLAG, BLUE, 4, 4);
 		addToConfiguration(FLAG, BLUE, 0, 4);
 
 		MoveRuleSet moveRules = new BetaMoveRuleSet(new BetaCombatRuleSet(), new BetaBoardRuleSet());
 
		boolean canMove = moveRules.canMove(redConfiguration, blueConfiguration);
		assertFalse("canMove is actually <" + canMove + ">", canMove);
 	}	
 	
 	// Helper methods
 	private void addToConfiguration(PieceType type, PlayerColor color, int x, int y)
 	{
 		final PieceLocationDescriptor confItem = new PieceLocationDescriptor(
 				new Piece(type, color),
 				new Location2D(x, y));
 		if (color == PlayerColor.RED) { 
 			redConfiguration.add(confItem);
 		} else {
 			blueConfiguration.add(confItem);
 		}
 	}
 
 }
