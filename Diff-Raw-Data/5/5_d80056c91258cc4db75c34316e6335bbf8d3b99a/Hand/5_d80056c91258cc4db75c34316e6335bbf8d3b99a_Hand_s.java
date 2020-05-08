 package strain.entity;
 
 import java.util.ArrayList;
 import strain.exception.*;
 import strain.tile.Tile;
 
 /**
  * Simulates the player's hand of cards.
  * @author zeande
  *
  */
 public class Hand {
 	private ArrayList<Tile> tiles;
 	private int capacity = 4;
 	private int size = 0;
 	
 	/**
 	 * Adds a card to the player's hand.
 	 * @param card The card to add.
 	 * @throws FullHandException An exception is thrown if the player's hand
 	 * is already full.
 	 */
 	public void addCard(Tile tile) throws FullHandException {
 		if (size >= capacity)
 			throw new FullHandException("Your hand is already full. In order " +
 					"to draw a card, you must first withdraw down to under " +
 					capacity + " cards.");
 		tiles.add(tile);
 	}
 	
 	/**
 	 * Discards the specified card from the player's hand.
 	 * @param card The card to discard.
 	 * @throws EmptyHandException Thrown if the player has no cards in hand.
 	 * @throws CardNotInHandException Thrown if the specified card is not in
 	 * the player's hand.
 	 */
 	public void discard(Tile tile) throws EmptyHandException, 
			CardNotInHandException {
 		if (size == 0)
 			throw new EmptyHandException("Your hand is empty. You must first " +
 					"draw a card before you can discard one.");
 		if (!tileInHand(tile))
			throw new CardNotInHandException("Error! That card is not in the " +
 					"player's hand!");
 		tiles.remove(tile);
 	}
 
 	/**
 	 * Checks whether the specified card is in the player's hand.
 	 * @param card The card to check.
 	 * @return True if the card is in the player's hand; otherwise, false.
 	 */
 	private boolean tileInHand(Tile tile) {
 		return tiles.contains(tile);
 	}
 }
