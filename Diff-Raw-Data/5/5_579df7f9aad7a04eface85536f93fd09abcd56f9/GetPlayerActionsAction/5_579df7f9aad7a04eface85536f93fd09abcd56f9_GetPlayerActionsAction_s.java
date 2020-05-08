 /*******************************************************************************
  * CS544 Computer Networks Spring 2013
  * 5/26/2013 - ShuffleIfNeededAction.java
  * Group Members
  * o Jennifer Lautenschlager
  * o Constantine Lazarakis
  * o Carol Greco
  * o Duc Anh Nguyen
  * 
  * Purpose: This game action loops through the players who are in the game,
  * and prompts them for a game action (hit or stand).
  ******************************************************************************/
 package drexel.edu.blackjack.server.game.driver;
 
 import java.util.logging.Logger;
 
 import drexel.edu.blackjack.cards.DealtCard;
 import drexel.edu.blackjack.cards.Hand;
 import drexel.edu.blackjack.server.game.Game;
 import drexel.edu.blackjack.server.game.GameState;
 import drexel.edu.blackjack.server.game.User;
 import drexel.edu.blackjack.util.BlackjackLogger;
 
 /**
  * This action first checks to make sure there's not a dealer
  * blackjack. If it's not, then it loops through the active players,
  * having them make their game moves until they stand or bust,
  * at which points it moves to the next active player. 
  * 
  * @author Jennifer
  *
  */
 public class GetPlayerActionsAction extends GameAction {
 	
 	// We give someone up to this long to make each play
 	private int PLAY_WAIT_TIME		= 60 * SECOND_IN_MILLISECONDS;
 	
 	// And of course our logger
 	private final static Logger LOGGER = BlackjackLogger.createLogger(GetPlayerActionsAction.class.getName());
 
 	@Override
 	public boolean doAction(Game game) {
 		
 		boolean success = false;
 		
 		if( game != null ) {
 			GameState state = game.getGameState();
 			if( state == null || state.getDealerHand() == null ) {
				LOGGER.severe( "Trying to handle player actions, but there's no game state (or els eno dealer hand)..." );
 			} else if( state.getDealerHand().getIsBlackJack() ) {
 				LOGGER.info( "Skipping player actions as the dealer has blackjack." );
 				success = true;
 			} else {
 				// Loop through whoever needs to bet
 				User player = state.getNextPlayerToPlay();
 				while( player != null ) {
 					promptPlayerForGamePlay( player, state );
 					player = state.getNextPlayerToPlay();
 				}
 				success = true;
 			}
 		}
 		
 		return success;
 	}
 
 	/**
 	 * Prompts the player for their game play. This is done in
 	 * a timer session, like the betting
 	 * @param player
 	 */
 	private void promptPlayerForGamePlay(User player, GameState state ) {
 		
 		// STATEFUL: Need to set them into the 'IN_SESSION_AND_YOUR_TURN' state
 		player.setIsPlayerTurn();
 		
 		// First has to turn over their facedown cards
 		Hand hand = player.getHand();
 		if( hand == null ) {
 			LOGGER.severe( "A user was prompted for gameplay with a null hand!" );
 			return;
 		}
 		for( DealtCard card : hand.getFacedownCards() ) {
 			card.changeToFaceUp();
 		}
 		state.notifyOthersOfUpdatedHand( player, hand );
 
 		// If it's a blackjack, they don't have to play
 		if( hand.getIsBlackJack() ) {
 			state.notifyOthersOfGameAction( player, GameState.BLACKJACK_KEYWORD );
 		} else {
 			// Otherwise they'll need to play
 			boolean idledOut = false;
 			while( !player.getFinishedGamePlayThisRound() && !idledOut ) {
 
 				// This is when we started this whole process
 				long start = System.currentTimeMillis();
 				
 				// This is how long we've been waiting up until now
 				long delta = System.currentTimeMillis() - start;
 
 				state.notifyAllOfGameplayNeeded( player );
 				while( player.getNeedsToMakeAPlay() && delta < PLAY_WAIT_TIME ) {
 					
 					// Sleep a while to give them time to check
 					try {
 						Thread.sleep( SWEEP_DELAY );
 					} catch( InterruptedException e ) {
 						// It's just waking us up
 					}
 					
 					// Recalculate delta before checking again
 					delta = System.currentTimeMillis() - start;
 				}
 				
 				// If they exceeded the limit without making a play, well, too bad for them
 				// If this is true, they'll be marked as still needing to make a play
 				if( player.getNeedsToMakeAPlay() ) {
 					// Force them to idle timeout
 					player.forceTimeoutWhilePlaying();
 					// Remove them from the list of game players
 					state.removePlayer(player);
 					idledOut = true;
 				}
 			}
 		}
 	}
 
 }
