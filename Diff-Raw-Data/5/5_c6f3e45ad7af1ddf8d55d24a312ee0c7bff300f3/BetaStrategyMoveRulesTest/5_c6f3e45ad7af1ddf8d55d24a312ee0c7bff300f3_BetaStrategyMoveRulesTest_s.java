 package strategy.game.version.beta;
 
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 import static strategy.common.PlayerColor.BLUE;
 import static strategy.common.PlayerColor.RED;
 import static strategy.game.common.PieceType.CAPTAIN;
 import static strategy.game.common.PieceType.COLONEL;
 import static strategy.game.common.PieceType.FLAG;
 import static strategy.game.common.PieceType.LIEUTENANT;
 import static strategy.game.common.PieceType.MARSHAL;
 import static strategy.game.common.PieceType.SERGEANT;
 
 import java.util.ArrayList;
 import java.util.Collection;
 
 import org.junit.Before;
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
 
 public class BetaStrategyMoveRulesTest {
 
 	private Collection<PieceLocationDescriptor> redConfiguration;
 	private Collection<PieceLocationDescriptor> blueConfiguration;
 
 	private MoveRuleSet moveRules;
 	
 	@Before
 	public void setup(){
 		moveRules = new BetaMoveRuleSet(new BetaCombatRuleSet(), new BetaBoardRuleSet());
 	}
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
 
		boolean canMove = moveRules.canMove(redConfiguration, blueConfiguration);
 		assertFalse("canMove is actually <" + canMove + ">", canMove);
 	}	
 	
 	@Test
 	public void testCanMove_valid() throws StrategyException{
 		//Red configuration
 		redConfiguration = new ArrayList<PieceLocationDescriptor>();
 
 		//Red pieces
 		addToConfiguration(FLAG, RED, 0, 1);
 		addToConfiguration(MARSHAL, RED, 0, 0);
 		addToConfiguration(COLONEL, RED, 1, 0);
 		addToConfiguration(COLONEL, RED, 2, 0);
 		addToConfiguration(CAPTAIN, RED, 3, 0);
 		addToConfiguration(CAPTAIN, RED, 4, 0);
 		addToConfiguration(LIEUTENANT, RED, 5, 0);
 		addToConfiguration(LIEUTENANT, RED, 1, 1);
 		addToConfiguration(LIEUTENANT, RED, 2, 1);
 		addToConfiguration(SERGEANT, RED, 3, 1);
 		addToConfiguration(SERGEANT, RED, 4, 1);
 		addToConfiguration(SERGEANT, RED, 5, 1);
 
 		//Blue configuration
 		blueConfiguration = new ArrayList<PieceLocationDescriptor>();
 
 		//Blue pieces
 		addToConfiguration(FLAG, BLUE, 5, 4);
 		addToConfiguration(MARSHAL, BLUE, 0, 5);
 		addToConfiguration(COLONEL, BLUE, 1, 5);
 		addToConfiguration(COLONEL, BLUE, 2, 5);
 		addToConfiguration(CAPTAIN, BLUE, 3, 5);
 		addToConfiguration(CAPTAIN, BLUE, 4, 5);
 		addToConfiguration(LIEUTENANT, BLUE, 5, 5);
 		addToConfiguration(LIEUTENANT, BLUE, 1, 4);
 		addToConfiguration(LIEUTENANT, BLUE, 2, 4);
 		addToConfiguration(SERGEANT, BLUE, 3, 4);
 		addToConfiguration(SERGEANT, BLUE, 4, 4);
 		addToConfiguration(SERGEANT, BLUE, 0, 4);
 
		boolean canMove = moveRules.canMove(redConfiguration, blueConfiguration);
 		assertTrue("canMove is actually <" + canMove + ">", canMove);
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
