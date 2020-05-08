 package poker.hand_card;
 
 
 import java.util.List;
 
 /**
  * A Hand encapsulates a number of cards as held by a player
  *
  */
 public interface Hand extends Iterable<Card>{
 
 	/**
 	 * Gets a list of the cards in the hand
 	 * @return the cards held in the hand
 	 */
 	List<Card> getCards();
 
 	/**
 	 * Adds a list of cards t the hand
 	 * @param cards the cards to be added to the hand
 	 */
 	void addCards(List<Card> cards);
 	
 	/**
 	 * Sorts the cards in the hand by rank and then returns the sorted hand
 	 * @return the hand after the cards have been sorted by rank
 	 */
 	Hand sortByRank();
 
 	/**
 	 * TO REMOVE...?
 	 * @param i
	 * @return card
 	 */
 	Card getCardAt(int i);
 
 	/**
 	 * Removes each of a list of cards from the hand
 	 * @param cardsToRemove the list of cards to remove
 	 */
 	void removeCards(List<Card> cardsToRemove);
 	
 	/**
 	 * Removes all cards from the hand, ready for the next round
 	 */
 	void clearHand();
 
 	/**
 	 * THINK ABOUT THIS
 	 * @param cardsToMove
	 * @return hand
 	 */
 	Hand moveCardsToStartOthersRankOrder(List<Card> cardsToMove);
 
 
 }
