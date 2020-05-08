 package roma;
 
 import java.util.*;
 
 import cards.*;
 
 public class Field {
 	private Card[][] fieldData;
 	
 	public Field() {
 		fieldData = new Card[Game.MAX_PLAYERS][Game.FIELD_SIZE];
 	}
 	
 	/**
 	 * Sets a card in the specified position.
 	 * @param player Which player
 	 * @param position Which dice disc (0..max) to place it next to
 	 * @param c the card
 	 * @return The card that was replaced, if applicable
 	 */
 	public Card setCard (int player, int position, Card c) {
 		Card replacedCard = fieldData[player][position];		
 		fieldData[player][position] = c;
 		return replacedCard;
 	}
 	
 	public Card getCard (int player, int position) {
 		return fieldData[player][position];
 	}
 	
 	public List<Card> getSideAsList (int player) {
 		List<Card> side = new ArrayList<Card>();
 		for (Card cardOnSide : fieldData[player]) {
			if (cardOnSide != null) {
				side.add(cardOnSide);
			}
 		}
 		return side;
 	}
 }
