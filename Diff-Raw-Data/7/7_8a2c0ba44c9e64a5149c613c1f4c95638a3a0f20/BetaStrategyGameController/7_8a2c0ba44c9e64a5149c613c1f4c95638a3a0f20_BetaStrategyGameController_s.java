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
 
 import java.util.ArrayList;
 import java.util.Collection;
 
 import strategy.common.StrategyException;
 import strategy.game.StrategyGameController;
 import strategy.game.common.Location;
 import strategy.game.common.MoveResult;
 import strategy.game.common.Piece;
 import strategy.game.common.PieceLocationDescriptor;
 import strategy.game.common.PieceType;
 import strategy.game.version.beta.combat.BetaCombatRuleSet;
 import strategy.game.version.beta.configuration.BetaConfigurationRuleSet;
 import strategy.game.version.beta.configuration.BetaStartingConfigurationRuleSet;
 import strategy.game.version.beta.move.BetaMoveRuleSet;
 import strategy.game.version.combat.CombatRuleSet;
 import strategy.game.version.configuration.ConfigurationRuleSet;
 import strategy.game.version.move.MoveRuleSet;
 
 /**
  * The BetaStrategyGameController implements the game core for
  * the Beta Strategy version.
  * @author rjsmieja, jrspicola
  * @version Sep 10, 2013
  */
 public class BetaStrategyGameController implements StrategyGameController
 {
 	private boolean gameStarted;
 	private boolean gameOver;
 	
 	private Collection<PieceLocationDescriptor> redConfiguration;
 	private Collection<PieceLocationDescriptor> blueConfiguration;
 	
 	final private Collection<PieceLocationDescriptor> redStartConfiguration;
 	final private Collection<PieceLocationDescriptor> blueStartConfiguration;
 	
 	final private ConfigurationRuleSet startingConfigurationRules;
 	final private ConfigurationRuleSet configurationRules;
 	final private CombatRuleSet combatRules;
 	private MoveRuleSet moveRules;
 	
 	/**
 	 * Constructor for BetaStrategyGameController
 	 * @param redConfiguration Configuration to be used for the red player
 	 * @param blueConfiguration Configuration to be used for the blue player
 	 * @throws StrategyException Thrown if there is an error in the configurations
 	 */
 	public BetaStrategyGameController(Collection<PieceLocationDescriptor> redConfiguration, Collection<PieceLocationDescriptor> blueConfiguration) throws StrategyException
 	{
 		//Rules
 		startingConfigurationRules = new BetaStartingConfigurationRuleSet();
 		configurationRules = new BetaConfigurationRuleSet();
 
 		//State
 		gameStarted = false;
 		gameOver = false;
 //		moveCount = 0;
 		
 		//Validate configurations
 		if (!startingConfigurationRules.validateConfiguration(blueConfiguration)){
 			throw new StrategyException("Blue starting configuration is invalid!");
 		}
 		if (!startingConfigurationRules.validateConfiguration(redConfiguration)){
 			throw new StrategyException("Red starting configuration is invalid!");
 		}
 		
 		//Save configurations
 		redStartConfiguration = redConfiguration;
 		blueStartConfiguration = blueConfiguration;
 		
 		this.redConfiguration = new ArrayList<PieceLocationDescriptor>(redStartConfiguration);
 		this.blueConfiguration = new ArrayList<PieceLocationDescriptor>(blueStartConfiguration);
 		
 		combatRules = new BetaCombatRuleSet();
 		moveRules = new BetaMoveRuleSet(combatRules);
 	}
 	
 	/**
 	 * @throws StrategyException 
 	 * @see strategy.game.StrategyGameController#startGame()
 	 */
 	@Override
 	public void startGame() throws StrategyException
 	{
 		if (gameStarted && !gameOver){
 			throw new StrategyException("Unfinished game currently in progress");
 		}
 		
 		gameStarted = true;
 		gameOver = false;
 
 		redConfiguration = new ArrayList<PieceLocationDescriptor>(redStartConfiguration);
 		blueConfiguration = new ArrayList<PieceLocationDescriptor>(blueStartConfiguration);
 		
 		moveRules.reset();
 	}
 
 	/**
 	 * @see strategy.game.StrategyGameController#move(strategy.game.common.PieceType, strategy.game.common.Location, strategy.game.common.Location)
 	 */
 	@Override
 	public MoveResult move(PieceType piece, Location from, Location to)
 			throws StrategyException
 	{
 		if (gameOver) {
 			throw new StrategyException("The game is over, you cannot make a move");
 		}
 		if (!gameStarted) {
 			throw new StrategyException("You must start the game!");
 		}
 		
 		//Validate locations passed in
 //		checkLocationBoundaries(true, null, to);
 //		checkLocationBoundaries(true, null, from);
 		
 //		PieceType movingPieceType = getPieceAt(from);
 //		if ()
 		
 		final PieceLocationDescriptor movingPiece = new PieceLocationDescriptor(getPieceAt(from), from);
 		final PieceLocationDescriptor targetPiece = new PieceLocationDescriptor(getPieceAt(to), to);
 		
 		MoveResult result = moveRules.doMove(piece, movingPiece, targetPiece, redConfiguration, blueConfiguration);
 //		final Piece movingPiece = getPieceAt(from);
 //		final Piece targetPiece = getPieceAt(to);
 //		
 //		if(movingPiece == null){
 //			throw new StrategyException("No piece at location");
 //		} 
 //		
 //		if (movingPiece.getType() != piece){
 //			throw new StrategyException("Piece type does not match!");
 //		}
 //		
 //		final PlayerColor pieceOwner = movingPiece.getOwner();
 //		
 //		if (pieceOwner != turnPlayer){
 //			throw new StrategyException("Piece does not belong to the current player's turn!");
 //		}
 //		
 //		final int distanceToMove = to.distanceTo(from);
 
 
 //		if (distanceToMove > MAX_MOVE_DISTANCE){
 //			throw new StrategyException("Moving too many spaces");
 //		}
 //		
 //		if (distanceToMove == 0){
 //			throw new StrategyException("Not moving");
 //		}
 		
 
 		
 //		//check 'to' spot. if no piece there, make move. else, check piece color.
 //		if (targetPiece != null){
 //			//if color is friend, throw exception. if color is enemy, battle
 //			if (movingPiece.getOwner() != targetPiece.getOwner()){
 //				if(movingPiece.getType().equals(targetPiece.getType()) && movingPiece.getType() != PieceType.FLAG) {
 //					//remove both pieces
 //					removePiece(new PieceLocationDescriptor(movingPiece, from), redConfiguration, blueConfiguration);
 //					removePiece(new PieceLocationDescriptor(targetPiece, to), redConfiguration, blueConfiguration);
 //				} else {
 //					//Combat
 ////					combatRules.doCombat(new PieceLocationDescriptor(movingPiece, to), new PieceLocationDescriptor(targetPiece, to));
 //				}
 //			} else {
 //				throw new StrategyException("Attempting to do combat on friendly piece!");
 //			}
 //		} else {
 //			movePiece(movingPiece, from, to);
 //		}
 //		
 		//Validate configurations after making the move
 		if(!configurationRules.validateConfiguration(redConfiguration)){
 			throw new StrategyException("Move left red configuration in an invalid state!");
 		}
 		if(!configurationRules.validateConfiguration(blueConfiguration)){
 			throw new StrategyException("Move left blue configuration in an invalid state!");
 		}
 
 		return result;
 //		checkConfigurations(gameStarted, validPieceTypes, redConfiguration, blueConfiguration);
 
 //		return endOfMove(null, null);
 	}
 //	
 //	/**
 //	 * Helper to actually move a piece to an empty location
 //	 * @param piece Piece to move
 //	 * @param from Location to move piece from
 //	 * @param to Location to move piece to
 //	 */
 //	private void movePiece(Piece movingPiece,Location from,Location to){
 //		final PieceLocationDescriptor oldPiece = new PieceLocationDescriptor(movingPiece, from);
 //		final PieceLocationDescriptor newPiece = new PieceLocationDescriptor(movingPiece, to);
 //		
 //		if(movingPiece.getOwner() == PlayerColor.RED){
 //			redConfiguration.remove(oldPiece);
 //			redConfiguration.add(newPiece);
 //		}else if (movingPiece.getOwner() == PlayerColor.BLUE){
 //			blueConfiguration.remove(oldPiece);
 //			blueConfiguration.add(newPiece);
 //		}
 //	}
 ////	
 //	/**
 //	 * Helper to reduce redundant code, used at the end of each move
 //	 * @return the final MoveResult
 //	 */
 //	private MoveResult endOfMove(PlayerColor winner, PieceLocationDescriptor piece){
 //		//Increment counter and return result
 //		moveCount++;
 //		
 //		if (moveCount >= MAX_MOVES){
 //			gameOver = true;
 //			return new MoveResult(MoveResultStatus.DRAW, piece);
 //		}
 //		
 //		//Next player's turn
 //		if (turnPlayer == PlayerColor.RED){
 //			turnPlayer = PlayerColor.BLUE;
 //		} else {
 //			turnPlayer = PlayerColor.RED;
 //		}
 //	
 //		if (winner == PlayerColor.RED){
 //			return new MoveResult(MoveResultStatus.RED_WINS, piece);
 //		} else if (winner == PlayerColor.BLUE) {
 //			return new MoveResult(MoveResultStatus.BLUE_WINS, piece);
 //		}
 //		
 //		return new MoveResult(MoveResultStatus.OK, piece);
 //
 //	}
 //	
 	/**
 	 * @see strategy.game.StrategyGameController#getPieceAt(strategy.game.common.Location)
 	 */
 	@Override
 	public Piece getPieceAt(Location location)
 	{
 		//Loop through all collections
 		for(PieceLocationDescriptor i : redConfiguration){
 			if (i.getLocation().equals(location)){
 				return i.getPiece();
 			}
 		}
 
 		for(PieceLocationDescriptor i : blueConfiguration){
 			if (i.getLocation().equals(location)){
 				return i.getPiece();
 			}
 		}
 
 		return null;
 	}
 	
 //	/**
 //	 * Helper to remove piece from the passed in configurations
 //	 * @param piece the pieces to remove
 //	 * @param configurations configurations remove the piece from
 //	 */
 //	@SafeVarargs
 //	static private void removePiece(PieceLocationDescriptor piece, Collection<PieceLocationDescriptor>... configurations) {
 //		for (Collection<PieceLocationDescriptor> configuration: configurations){
 //			configuration.remove(piece);
 //		}
 //	}
 	
 //	/**
 //	 * Helper to check if moves can be made
 //	 * @throws StrategyException if a move can not be made
 //	 */
 //	private void canMoveCheck() throws StrategyException{
 //		boolean breakFlag = false;
 //
 //		for(int i = 0; i<=5;i++){
 //			if(!breakFlag){
 //				for(int j = 0; j<=5; j++){
 //					if(!breakFlag){
 //						if(getPieceAt(new Location2D(i,j)) != null){
 //							if(getPieceAt(new Location2D(i, j)).getOwner() == turnPlayer){
 //								if(getPieceAt(new Location2D(i, j)).getType() != PieceType.FLAG){
 //									breakFlag = true;
 //								}
 //							}
 //						}
 //					}
 //				}
 //			}
 //		}
 //
 //		if(!breakFlag){
 //			gameOver = true;
 //			throw new StrategyException("There are no movable pieces for the current player.");
 //		}
 //	}
 }
