 /*******************************************************************************
  * This files was developed for CS4233: Object-Oriented Analysis & Design.
  * The course was taken at Worcester Polytechnic Institute.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *******************************************************************************/
 
 package strategy.game.version.beta;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNull;
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
 import strategy.game.StrategyGameController;
 import strategy.game.StrategyGameFactory;
 import strategy.game.common.Location;
 import strategy.game.common.Location2D;
 import strategy.game.common.MoveResult;
 import strategy.game.common.MoveResultStatus;
 import strategy.game.common.Piece;
 import strategy.game.common.PieceLocationDescriptor;
 import strategy.game.common.PieceType;
 
 /**
  * Description
  * @author rjsmieja, jrspicola
  * @version Sep 13, 2013
  */
 public class BetaStrategyMasterTest
 {
 	private StrategyGameController game;
 	private StrategyGameFactory gameFactory;
 	private Collection<PieceLocationDescriptor> redConfiguration;
 	private Collection<PieceLocationDescriptor> blueConfiguration;
 	
 	@Before
 	public void setup() throws StrategyException
 	{
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
 		
 		gameFactory = StrategyGameFactory.getInstance();
 		
 		game = gameFactory.makeBetaStrategyGame(redConfiguration, blueConfiguration);
 	}
 	
 	@Test(expected=StrategyException.class)
 	public void invalidSetup() throws StrategyException{
 		
 		//Red configuration
 		Collection<PieceLocationDescriptor> invalidConfiguration1 = new ArrayList<PieceLocationDescriptor>();
 		invalidConfiguration1.add(new PieceLocationDescriptor(new Piece(PieceType.FLAG, PlayerColor.RED), new Location2D(0,0)));
 		
 		Collection<PieceLocationDescriptor> invalidConfiguration2 = new ArrayList<PieceLocationDescriptor>();
 		invalidConfiguration2.add(new PieceLocationDescriptor(new Piece(PieceType.FLAG, PlayerColor.BLUE), new Location2D(0,0)));
 
 		
 		gameFactory.makeBetaStrategyGame(invalidConfiguration1, invalidConfiguration2);
 	}
 	
 	@Test(expected=StrategyException.class)
 	public void makeMoveBeforeInitialization() throws StrategyException
 	{
 		PieceLocationDescriptor r = redConfiguration.iterator().next();
 		PieceLocationDescriptor b = blueConfiguration.iterator().next();
 		game.move(r.getPiece().getType(), r.getLocation(), b.getLocation()); //should throw "You must start the game!"
 	}
 	
 	@Test
 	public void makeValidMove() throws StrategyException
 	{
 		game.startGame(); 
 		Location start = new Location2D(1,0);
 		Location moveTo = new Location2D(2,0);
 
 		//moves the lieutenant forward one square
 		MoveResult actualResult = game.move(PieceType.LIEUTENANT, start, moveTo);
 		MoveResult expectedResult = new MoveResult(MoveResultStatus.OK, null);
 		
 		assertEquals(expectedResult.getStatus(), actualResult.getStatus());
 		assertEquals(expectedResult.getBattleWinner(), actualResult.getBattleWinner());
 	}
 	
 	@Test
 	public void makeSixMovesAndDraw() throws StrategyException
 	{
 		game.startGame();
 		
 		//make 5 useless moves
 		assertEquals(MoveResultStatus.OK, game.move(PieceType.LIEUTENANT, new Location2D(1,0), new Location2D(2,0)).getStatus());
 		assertEquals(MoveResultStatus.OK, game.move(PieceType.LIEUTENANT, new Location2D(4,0), new Location2D(3,0)).getStatus());
 		assertEquals(MoveResultStatus.OK, game.move(PieceType.LIEUTENANT, new Location2D(2,0), new Location2D(1,0)).getStatus());
 		assertEquals(MoveResultStatus.OK, game.move(PieceType.LIEUTENANT, new Location2D(3,0), new Location2D(4,0)).getStatus());
 		assertEquals(MoveResultStatus.OK, game.move(PieceType.LIEUTENANT, new Location2D(1,0), new Location2D(2,0)).getStatus());
 		
 		//make 6th move, check to see if draw
 		assertEquals(MoveResultStatus.DRAW, game.move(PieceType.LIEUTENANT, new Location2D(4,0), new Location2D(3,0)).getStatus());
 	}
 	
 	@Test(expected=StrategyException.class)
 	public void testMoveAftergameOver() throws StrategyException
 	{
 		game.startGame();
 		
 		//make 5 useless moves
 		assertEquals(MoveResultStatus.OK, game.move(PieceType.LIEUTENANT, new Location2D(1,0), new Location2D(2,0)).getStatus());
 		assertEquals(MoveResultStatus.OK, game.move(PieceType.LIEUTENANT, new Location2D(4,0), new Location2D(3,0)).getStatus());
 		assertEquals(MoveResultStatus.OK, game.move(PieceType.LIEUTENANT, new Location2D(2,0), new Location2D(1,0)).getStatus());
 		assertEquals(MoveResultStatus.OK, game.move(PieceType.LIEUTENANT, new Location2D(3,0), new Location2D(4,0)).getStatus());
 		assertEquals(MoveResultStatus.OK, game.move(PieceType.LIEUTENANT, new Location2D(1,0), new Location2D(2,0)).getStatus());
 		
 		//make 6th move, check to see if draw
 		assertEquals(MoveResultStatus.DRAW, game.move(PieceType.LIEUTENANT, new Location2D(4,0), new Location2D(3,0)).getStatus());
 		
 		//Try to move after game over
 		game.move(PieceType.LIEUTENANT, new Location2D(2,0), new Location2D(1,0));
 	}
 
 	
 	@Test(expected=StrategyException.class)
 	public void makeMoveWithWrongPiece() throws StrategyException
 	{
 		game.startGame();
 		Location lieutLoc = new Location2D(1,0);
 		Location lieutDest = new Location2D(2,0);
 		
 		game.move(PieceType.CAPTAIN, lieutLoc, lieutDest);
 	}
 
 	@Test(expected=StrategyException.class)
 	public void makeMoveWithOutOfBoundsXCoordinate() throws StrategyException
 	{
 		game.startGame();
 		game.move(PieceType.CAPTAIN, new Location2D(5, 5), new Location2D(6,5)); //should throw a out of board error
 	}
 	
 	@Test(expected=StrategyException.class)
 	public void makeMoveWithOutOfBoundsYCoordinate() throws StrategyException
 	{
 		game.startGame();
 		game.move(PieceType.CAPTAIN, new Location2D(5, 5), new Location2D(5,6)); //should throw an out of board error
 	}
 	
 	@Test(expected=StrategyException.class)
 	public void makeMoveWithIncorrectDistanceX() throws StrategyException
 	{
 		game.startGame();
 		game.move(PieceType.LIEUTENANT, new Location2D(1,0), new Location2D(3, 0)); //should throw a distance error
 	}
 	
 	@Test(expected=StrategyException.class)
 	public void makeMoveWithIncorrectDistanceY() throws StrategyException
 	{
 		game.startGame();
 		game.move(PieceType.LIEUTENANT, new Location2D(1,0), new Location2D(1, 3)); //should throw a distance error
 	}
 	
 	@Test(expected=StrategyException.class)
 	public void makeMoveWithNoDistance() throws StrategyException
 	{
 		game.startGame();
 		game.move(PieceType.LIEUTENANT, new Location2D(1,0), new Location2D(1, 0)); //should throw a distance error
 	}
 	
 	@Test(expected=StrategyException.class)
 	public void makeMoveWithIncorrectDistanceXAndY() throws StrategyException
 	{
 		game.startGame();
 		game.move(PieceType.LIEUTENANT, new Location2D(1,0), new Location2D(2, 1)); //should throw a distance error
 	}
 	
 	@Test(expected=StrategyException.class)
 	public void battleWithAttackerWinning() throws StrategyException
 	{
 		game.startGame();
 		game.move(PieceType.SERGEANT, new Location2D(1, 4), new Location2D(2, 4)); //red
 		game.move(PieceType.LIEUTENANT, new Location2D(4, 3), new Location2D(3, 3)); //blue
 		game.move(PieceType.SERGEANT, new Location2D(2, 4), new Location2D(3, 4)); //red
 		//blue
 		assertEquals(game.move(PieceType.LIEUTENANT, new Location2D(3, 3), new Location2D(3, 4)), new MoveResult(MoveResultStatus.OK,
 				new PieceLocationDescriptor(new Piece(PieceType.LIEUTENANT, PlayerColor.BLUE), new Location2D(3,4))));
 
 	}
 	
 	@Test(expected=StrategyException.class)
 	public void battleWithAttackerLosing() throws StrategyException
 	{
 		game.startGame();
 		game.move(PieceType.LIEUTENANT, new Location2D(1, 3), new Location2D(2, 3)); //red
 		game.move(PieceType.SERGEANT, new Location2D(4, 4), new Location2D(3, 4)); //blue
 		game.move(PieceType.LIEUTENANT, new Location2D(2, 3), new Location2D(3, 3)); //red
 		//blue
 		assertEquals(game.move(PieceType.SERGEANT, new Location2D(3, 4), new Location2D(3, 3)), new MoveResult(MoveResultStatus.OK,
 				new PieceLocationDescriptor(new Piece(PieceType.LIEUTENANT, PlayerColor.RED), new Location2D(3,4))));
 	}
 	
 	@Test
 	public void testGetPieceAt_Red(){
		assertEquals(new Piece(PieceType.FLAG, PlayerColor.RED), game.getPieceAt(new Location2D(0,1)));
 	}
 	
 	@Test
 	public void testGetPieceAt_Blue(){
		assertEquals(new Piece(PieceType.FLAG, PlayerColor.BLUE), game.getPieceAt(new Location2D(5,4)));
 	}
 	
 	@Test
 	public void testGetPieceAt_RedLieutenant(){
		assertEquals(new Piece(PieceType.LIEUTENANT, PlayerColor.RED), game.getPieceAt(new Location2D(1,1)));
 	}
 	
 	@Test
 	public void testGetPieceAt_noPieceAtLocation(){
 		assertNull(game.getPieceAt(new Location2D(3,3)));
 	}
 	
 	@Test
 	public void testGetPieceAt_invalidLocation(){
 		assertNull(game.getPieceAt(new Location2D(7,9)));
 	}
 	
 	@Test(expected=StrategyException.class)
 	public void testRedPlayerFirst() throws StrategyException{
 		game.startGame();
 		game.move(PieceType.LIEUTENANT, new Location2D(4,0), new Location2D(3,0));
 	}
 	
 	@Test(expected=StrategyException.class)
 	public void redPlayerMoveOnBlueTurn() throws StrategyException{
 		game.startGame();
 		assertEquals(MoveResultStatus.OK, game.move(PieceType.LIEUTENANT, new Location2D(1,0), new Location2D(2,0)).getStatus());
 		game.move(PieceType.LIEUTENANT, new Location2D(2,0), new Location2D(3,0));
 	}
 	
 	@Test(expected=StrategyException.class)
 	public void moveOnFriendlyPiece() throws StrategyException{
 		game.startGame();
 		
 		game.move(PieceType.LIEUTENANT, new Location2D(1,0), new Location2D(0,0));
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
