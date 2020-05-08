 package Server.gameModule;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 /**
  * A wrapper class of a Poker Hand. All of the algorithms used to determine the
  * value of a hand are original and no outside sources were consulted. Moreover,
  * these algorithms are pretty efficient.
  * 
  * @author mouhyi
  */
 public class Hand implements Comparable<Hand> {
 
 	private final static int HAND_SIZE = 5;
 	private ArrayList<Card> cards;
 
 	/**
 	 * Constructor
 	 * 
 	 * @author mouhyi
 	 */
 	public Hand() {
 		// ArrayList with initial capacity = size of a poker hand;
 		cards = new ArrayList<Card>(HAND_SIZE);
 	}
 
 	/**
 	 * Adds new card to this hand
 	 * 
 	 * @param card
 	 *            to be added
 	 * @author mouhyi
 	 */
 	public void add(Card card) {
 		cards.add(card);
 	}
 
 	/**
 	 * Adds list of card to this hand
 	 * 
 	 * @param list
 	 *            : cards to be added
 	 * @author mouhyi
 	 */
 	public void addAll(ArrayList<Card> list) {
 		cards.addAll(list);
 	}
 
 	/**
 	 * Removes a card from this hand
 	 * 
 	 * @return top card of the hand
 	 * @author mouhyi
 	 */
 	public Card remove() {
 		return cards.remove(cards.size() - 1);
 	}
 
 	/**
 	 * Returns the number of cards in this hand
 	 * 
 	 * @author mouhyi
 	 */
 	public int getSize() {
 		return cards.size();
 	}
 
 	/**
 	 * Determines the value of this hand. notes: If there is a flush, there
 	 * cannot be pairings and vice versa. If there is a pairing, there cannot be
 	 * straight and vice versa.
 	 * 
 	 * @return the value of this hand
 	 * @author mouhyi
 	 */
 	public HandType getValue() {
 		if (isStraightFlush()) {
 			return HandType.STRAIGHT_FLUSH;
 		}
 		if (isFlush()) {
 			return HandType.FLUSH;
 		}
 		if (isStraight()) {
 			return HandType.STRAIGHT;
 		}
 		return this.getPairing();
 	}
 
 	/**
 	 * Test if all the cards in this hand are of the same suit.
 	 * 
 	 * @return Boolean: true if flush, false if not
 	 * @author mouhyi
 	 */
 	public boolean isFlush() {
 		if(cards.size() < HAND_SIZE){
 			return false;
 		}
 		Suit s = cards.get(0).getSuit();
 		for (int i = 1; i < cards.size(); i++) {
 			if (cards.get(i).getSuit() != s)
 				return false;
 		}
 
 		return true;
 	}
 
 	/**
 	 * Test if this hand contains five cards in sequence,
 	 * 
 	 * @return Boolean: true if straight, false if not
 	 * @author mouhyi
 	 */
 	public boolean isStraight() {
 		ArrayList<Card> cardsCpy = new ArrayList<Card>(cards);
 		Collections.sort(cardsCpy);
 		
 		// if size<5
 		if(cardsCpy.size() < HAND_SIZE){
 			return false;
 		}
 		
 		// if there two cards have the same rank return false
 		for (Card c: cardsCpy){
 			if (Collections.frequency(cardsCpy, c) >1){
 				return false;
 			}
 		}
 		
 		int n = cardsCpy.size();
 		// if hand does not contain an Ace
 		if (cardsCpy.get(n - 1).getRank() != Rank.Ace) {
 			int diff = cardsCpy.get(n - 1).getRank().ordinal()
 					- cardsCpy.get(0).getRank().ordinal();
 			return (diff == n - 1);
 		}
 		// if hand contains Ace (at index 4)
 		else {
 			// check for: J, Q, K, Ace
 			int diff = cardsCpy.get(n - 1).getRank().ordinal()
 					- cardsCpy.get(0).getRank().ordinal();
 			if (diff == n - 1) {
 				return true;
 			}
 			// check for: Ace, 2, 3, 4, 5
 			diff = cardsCpy.get(n - 2).getRank().ordinal()
 					- cardsCpy.get(0).getRank().ordinal();
 			if (diff != n - 2)
 				return false;
 			return (cardsCpy.get(0).getRank() == Rank.Deuce);
 		}
 	}
 
 	/**
 	 * Test if this hand contains five cards in sequence, all of the same Suit
 	 * 
 	 * @return Boolean: true if StraightFlush, false if not
 	 * @author mouhyi
 	 */
 	public boolean isStraightFlush() {
 		return (isStraight() && isFlush());
 	}
 
 	/**
 	 * Determine the pairing category of this hand.Call this method if this hand
 	 * is not Flush nor Straight
 	 * 
 	 * @return HandType
 	 * @author mouhyi
 	 */
 	public HandType getPairing() {
 		HashMap<Rank, Integer> table = new HashMap<Rank, Integer>(13);
 		for (Rank rank : Rank.values()) {
 			table.put(rank, 0);
 		}
 		for (Card c : cards) {
 			Rank r = c.getRank();
 			int freq = table.get(r);
 			table.put(r, freq + 1);
 		}
 		if (table.containsValue(4)) {
 			return HandType.FOUR_OF_A_KIND;
 		}
 		if (table.containsValue(3) && table.containsValue(2)) {
 			return HandType.FULL_HOUSE;
 		}
 		if (table.containsValue(3)) {
 			return HandType.THREE_OF_A_KIND;
 		}
 		if (table.containsValue(2)) {
 			Collection<Integer> col = table.values();
 			int freq = 0;
 			for (int i : col) {
 				if (i == 2)
 					freq++;
 			}
 			if (freq == 2)
 				return HandType.TWO_PAIR;
 			else
 				return HandType.ONE_PAIR;
 		}
 		return HandType.HIGH_CARD;
 	}
 
 	/**
 	 * Implement the comparable interface. IT defines a natural ordering of
 	 * hands.
 	 * 
 	 * @param h
 	 *            :Hand
 	 * @return A negative integer if this hand is less than the Hand argument;
 	 *         zero if this Hand is equal to the Hand argument; a positive
 	 *         number if this Hand is greater than the Hand argument.
 	 * @author mouhyi
 	 */
 	public int compareTo(Hand h) {
 		int comp = this.getValue().compareTo(h.getValue());
 		if (comp != 0) {
 			return comp;
 		}
 		if (this.getValue() == HandType.HIGH_CARD
 				|| this.getValue() == HandType.FLUSH) {
 			return breakTieHighCard(new ArrayList<Card>(this.cards),
 					new ArrayList<Card>(h.cards));
 		}
 		if (this.getValue() == HandType.STRAIGHT
 				|| this.getValue() == HandType.STRAIGHT_FLUSH) {
 			return breakTieStraight(new ArrayList<Card>(this.cards),
 					new ArrayList<Card>(h.cards));
 		}
 
 		List<Map.Entry<Rank, Integer>> thisList = this.sortCards();
 		List<Map.Entry<Rank, Integer>> list = h.sortCards();
 		Rank thisRk, rk;
 
 		while (true) {
 			thisRk = thisList.get(thisList.size() - 1).getKey();
 			rk = list.get(list.size() - 1).getKey();
 			comp = thisRk.compareTo(rk);
 
 			if (comp != 0) {
 				return comp;
 			}
 			thisList.remove(thisList.size() - 1);
 			list.remove(list.size() - 1);
 
 			if (list.size() < 1) {
 				return 0;
 			}
 
 		}
 	}
 
 	/**
 	 * Helper method to compare two set of cards based on highest individual
 	 * card. Only useful when the two hands have the same category
 	 * 
 	 * @author mouhyi
 	 */
 	public static int breakTieHighCard(ArrayList<Card> c1, ArrayList<Card> c2) {
 		Collections.sort(c1);
 		Collections.sort(c2);
 		int i = c1.size() - 1;
 		while (i >= 0 && (c1.get(i).compareTo(c2.get(i)) == 0)) {
 			i--;
 		}
 		return ((i == -1) ? 0 : c1.get(i).compareTo(c2.get(i)));
 	}
 
 	/**
 	 * Tie breaker for STRAIGHT_FLUSH or STRAIGHT
 	 * 
 	 * @param c1
 	 * @param c2
 	 * @return
 	 * @author mouhyi
 	 */
 	public static int breakTieStraight(ArrayList<Card> c1, ArrayList<Card> c2) {
 		boolean aceLow1 = false, aceLow2 = false, ace1 = false, ace2 = false;
 		for (Card card : c1) {
			//ace1 = false;
 			if (card.getRank() == Rank.Ace) {
 				ace1 = true;
 			}
 			if (card.getRank() == Rank.Deuce) {
 				aceLow1 = true;
 			}
 		}
 		aceLow1 = aceLow1 && ace1;
 
 		for (Card card : c2) {
			//ace1 = false;
 			if (card.getRank() == Rank.Ace) {
 				ace2 = true;
 			}
 			if (card.getRank() == Rank.Deuce) {
 				aceLow2 = true;
 			}
 		}
 		aceLow2 = aceLow2 && ace2;
 
 		if (aceLow1 && !aceLow2) {
 			return -1;
 		}
 		if (aceLow2 && !aceLow1) {
 			return 1;
 		}
 
 		Collections.sort(c1);
 		Collections.sort(c2);
 		int i = c1.size() - 1;
 		while (i >= 0 && (c1.get(i).compareTo(c2.get(i)) == 0)) {
 			i--;
 		}
 		return ((i == -1) ? 0 : c1.get(i).compareTo(c2.get(i)));
 	}
 
 	/**
 	 * Returns the Highest n-tuple in this hand where n=1..4
 	 * 
 	 * @author mouhyi
 	 */
 	public Rank getHighestTuple() {
 		List<Map.Entry<Rank, Integer>> list = this.sortCards();
 		return list.get(list.size() - 1).getKey();
 	}
 
 	/**
 	 * Returns the second Highest n-tuple in this hand where n=1..4 pre:
 	 * hand.size > 3
 	 * 
 	 * @author mouhyi
 	 */
 	public Rank getSecHighestTuple() {
 		List<Map.Entry<Rank, Integer>> list = this.sortCards();
 		return list.get(list.size() - 2).getKey();
 	}
 
 	/**
 	 * Sorts hands into a list <Map.Entry<Rank, Integer= freq>>
 	 * 
 	 * @return sorted list
 	 * @author mouhyi
 	 */
 	// tested Apr 2, 11:15 pm
 	public List<Map.Entry<Rank, Integer>> sortCards() {
 		HashMap<Rank, Integer> table = new HashMap<Rank, Integer>(HAND_SIZE);
 		for (Card c : cards) {
 			Rank r = c.getRank();
 			int freq = (table.containsKey(r)) ? table.get(r) : 0;
 			table.put(r, freq + 1);
 		}
 		List<Map.Entry<Rank, Integer>> sortedEntries = sortByValue(table);
 
 		return sortedEntries;
 
 	}
 
 	// Helper method to sort a hashmap by value
 	public static List<Map.Entry<Rank, Integer>> sortByValue(
 			HashMap<Rank, Integer> map) {
 		List<Map.Entry<Rank, Integer>> list = new LinkedList(map.entrySet());
 		Collections.sort(list, new Comparator<Map.Entry<Rank, Integer>>() {
 			public int compare(Map.Entry<Rank, Integer> m1,
 					Map.Entry<Rank, Integer> m2) {
 				if (m1.getValue() > m2.getValue()) {
 					return 1;
 				} else if (m1.getValue() < m2.getValue()) {
 					return -1;
 				} else {
 					return (m1.getKey().compareTo(m2.getKey()));
 				}
 			}
 		});
 
 		return list;
 
 	}
 
 	// ----> methods up to here tested on Apr 2, 02:00am
 
 }
