 
 package net.marcuswhybrow.uni.g52obj.cw1;
 
 import java.util.LinkedList;
 import java.util.Collection;
 import java.util.ArrayList;
 
 /**
  * Contains an ordered collection of cards. Cards can be inserted into the deck
  * in various ways and likewise removed.
  * 
  * @author Marcus Whybrow
  */
 public class Deck
 {
 	// Public Methods
 
 	
 	/**
 	 * Creates an empty new deck
 	 */
 	public Deck()
 	{
 		_cards = new LinkedList<Card>();
 		_sizeOfDeck = 0;
 	}
 
 	/**
 	 * Gets the number of cards contained within the deck
 	 * 
 	 * @return The number of cards in this deck
 	 */
 	public int getSize()
 	{
 		return _sizeOfDeck;
 	}
 
 	// "Adding Cards to the Deck" Methods
 
 	/**
 	 * Adds a card face down to the *top* of the deck.
 	 * 
 	 * @param card The card to add to the top of the deck.
 	 */
 	public void addCardToDeckTop(Card card)
 	{
 		if(card != null)
 		{
 			_cards.addFirst(card);
 			_sizeOfDeck++;
 		}
 	}
 
 	/**
 	 * Adds a card face down to the *bottom* of the deck.
 	 * 
 	 * @param card The card to add to the bottom of the deck.
 	 */
 	public void addCardToDeckBottom(Card card)
 	{
 		if(card != null)
 		{
 			_cards.addLast(card);
 			_sizeOfDeck++;
 		}
 	}
 
 	/**
 	 * Adds *multiple* cards face down to the *top* of the deck
 	 * 
 	 * @param c The collection of cards to add to the top of the deck
 	 */
	public void addCardsToDeckTop(Collection c)
 	{
 		_cards.addAll(0, c);
 		_sizeOfDeck += c.size();
 	}
 
 	/**
 	 * Adds *multiple* cards face down to the *bottom* of the deck
 	 * 
 	 * @param c THe collection of cards to add to the bottom of the deck
 	 */
	public void addCardsToDeckBottom(Collection c)
 	{
 		_cards.addAll(c);
 		_sizeOfDeck += c.size();
 	}
 
 	/**
 	 * Adds a card at a random point in the deck, if all cards are added in this
 	 * fashion all cards will effectively be shuffled.
 	 *
 	 * @param card The card to add into the deck.
 	 */
 	public void addCardToDeckRandomly(Card card)
 	{
 		if(card != null)
 		{
 			_cards.add((int) Math.round((_cards.size() * Math.random())), card);
 			_sizeOfDeck++;
 		}
 	}
 
 	
 	// "Taking cards from the deck" Methods
 
 	/**
 	 * Remove the card on top of the deck.
 	 * 
 	 * @return The card from the top of the deck.
 	 */
 	public Card takeCardFromTop()
 	{
 		_sizeOfDeck--;
 		return _cards.pollFirst();
 	}
 
 	/**
 	 * Remove the card from the bottom of the deck (you sly dog).
 	 * 
 	 * @return The card form the bottom of the deck.
 	 */
 	public Card takeCardFromBottom()
 	{
 		_sizeOfDeck--;
 		return _cards.pollLast();
 	}
 
 	/**
 	 * Returns but does not remove the *top* card of the deck.
 	 * 
 	 * @return The top card of the deck
 	 */
 	public Card lookAtTopCard()
 	{
 		return _cards.peekFirst();
 	}
 
 	/**
 	 * Returns but does not remove the *bottom* card of the deck
 	 * 
 	 * @return The bottom card of the deck
 	 */
 	public Card lookAtBottomCard()
 	{
 		return _cards.peekLast();
 	}
 
 	/**
 	 * Get every card from this deck (removing them from this deck).
 	 *
 	 * @return Every card from this deck
 	 */
 	public ArrayList<Card> takeAllCards()
 	{
		ArrayList cards = new ArrayList<Card>(_cards);
 		_cards.clear();
 		_sizeOfDeck = 0;
 		return cards;
 	}
 
 	/**
 	 * Takes the specified number of cards from the top of the current deck and
 	 * puts them in a new deck in the same order.
 	 *
 	 * @param depth The number of cards to take
 	 * @return The deck with the number of cards requested
 	 */
 	public Deck cutDeck(int depth)
 	{
 		Deck newDeck = new Deck();
 
 		for(; depth > 0; depth--)
 		{
 			newDeck.addCardToDeckBottom(_cards.pollFirst());
 			_sizeOfDeck--;
 		}
 
 		return newDeck;
 	}
 
 	/**
 	 * Takes half the deck, taking the majority if there is a odd number of cards.
 	 * 
 	 * @return The top half of the deck.
 	 */
 	public Deck halfDeck()
 	{
 		return cutDeck((int) Math.ceil(_sizeOfDeck/2));
 	}
 
 
 	// Other Deck Methods
 
 	/**
 	 * Shuffles the deck of cards into a new unguessable order.
 	 */
 	public void shuffleDeck()
 	{
 		int swapIndex;
 		Card temp;
 
 		for(int i = 0; i < _sizeOfDeck; i++)
 		{
 			swapIndex = (int) Math.round(Math.random() * _sizeOfDeck);
 			temp = _cards.get(i);
 			_cards.set(i, _cards.get(swapIndex));
 			_cards.set(swapIndex, temp);
 		}
 	}
 
 	/**
 	 * Determines whether the deck has no cards
 	 *
 	 * @return True if there are no cards left in the deck
 	 */
 	public boolean isEmpty()
 	{
 		return _cards.isEmpty();
 	}
 
 	/**
 	 * Get the string representation of this deck
 	 * 
 	 * @return A representation of every card in the deck
 	 */
 	public String toString()
 	{
 		return _cards.toString();
 	}
 
 
 	// Instance Variables
 
 	/** The cards in this deck */
 	private LinkedList<Card> _cards;
 	/** The number of cards in this deck */
 	private int _sizeOfDeck;
 }
