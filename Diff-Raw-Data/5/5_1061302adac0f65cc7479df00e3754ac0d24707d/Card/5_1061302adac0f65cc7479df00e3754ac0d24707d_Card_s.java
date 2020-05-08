 package com.contentanalyst.playingcards;
 
 /**
  * Class that represents cards in a common 52-card playing card deck.
  * An instance of this class is immutable, and therefore thread safe.
  * 
  * @author David Flynt
  *
  */
 public final class Card {
 	/**
 	 * enum of the 13 possible unique card ranks.
 	 * (Note: The fixed ranking below is not correct for all card games such as 
 	 * blackjack which require all face cards to have rank 10 and aces rank 11) 
 	 */
 	public enum Rank {
 		ACE(1, "Ace"), TWO(2, "2"), THREE(3, "3"), FOUR(4, "4"), FIVE(5, "5"), 
 		SIX(6, "6"), SEVEN(7, "7"), EIGHT(8, "8"), NINE(9, "9"), TEN(10, "10"), 
 		JACK(11, "Jack"), QUEEN(12, "Queen"), KING(13, "King");
 		
 		private int rank;
 		private String rankName;
 
 		Rank(int rank, String name) {
 			this.rank = rank;
 			rankName = name;
 		}
 		
 		/**
 		 * @return the {@code int} value of the the card rank:   
 		 */
 		public int getRankValue() {
 			return rank;
 		}
 		
 		/**
 		 * @return the string identification of this card {@code Rank}
 		 */
 		@Override
 		public String toString() { return rankName; }
 	}
 	
 	/**
 	 * @return {@code Rank} enum of this card
 	 */
 	public Rank getRank() {
 		return rank;
 	}
 	
 	/**
 	 * @return Suit enum of this card
 	 */
 	public Suit getSuit() {
 		return suit;
 	}
 	
 	/**
 	 * enum of the four possible suits of a playing card 
 	 */
 	public enum Suit {
 		CLUB("Club"), DIAMOND("Diamond"), HEART("Heart"), SPADE("Spade");
 		
 		private String suitName;
 		
 		Suit(String name) {
 			suitName = name;
 		}
 		
 		/**
 		 * @return the string identification of this {@code Suit}
 		 */
 		@Override
 		public String toString() { return suitName; }
 	}
 	
 	private final Rank rank;
 	private final Suit suit;
 
 	/**
      * Constructs a newly allocated {@code Card} object that represents 
     * the specified {@code Rank} and @{code Suit} of this card.
 	 * @param rank the {@code Card.Rank} of this card
 	 * @param suit the {@code Card.Suit} of this card
 	 */
 	public Card(Rank rank, Suit suit) {
 		this.rank = rank;
 		this.suit = suit;
 	}
 	
 	/**
 	 * Returns the string representation of this card, identifying the 
	 * {@code Rank} and @{code Suit}
 	 * 
 	 * @return the string identification of this card
 	 */
 	@Override
 	public String toString() { return rank + " " + suit; }
 	
     /**
      * Returns a hash code for this {@code Card}.
      *
      * @return  a hash code value for this object
      */
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((rank == null) ? 0 : rank.hashCode());
 		result = prime * result + ((suit == null) ? 0 : suit.hashCode());
 		return result;
 	}
 
     /**
      * Compares this object to the specified object.
      *
      * @param   obj   the object to compare with.
      * @return  {@code true} if the objects are the same;
      *          {@code false} otherwise.
      */
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (!(obj instanceof Card))
 			return false;
 		Card other = (Card) obj;
 		if (rank != other.rank)
 			return false;
 		if (suit != other.suit)
 			return false;
 		return true;
 	}
 }
