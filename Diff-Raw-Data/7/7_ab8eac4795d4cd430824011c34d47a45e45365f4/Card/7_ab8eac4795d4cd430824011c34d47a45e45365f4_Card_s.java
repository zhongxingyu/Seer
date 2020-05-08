 /*******************************************************************************
  * CS544 Computer Networks Spring 2013
  * 5/26/2013 - Card.java
  * Group Members
  * o Jennifer Lautenschlager
  * o Constantine Lazarakis
  * o Carol Greco
  * o Duc Anh Nguyen
  * 
  * Purpose: Object-oriented representation of a blackjack playing card
  ******************************************************************************/
 package drexel.edu.blackjack.cards;
 
 /**
  * A playing card.
  * 
  * @author CLaz
  *
  */
 public class Card {
 	
 	/**Instances of card ranks allowed in Blackjack version.1.0.
 	 * Explanations on HARD and SOFT HANDs:
 	 * HARD HAND: 
 	 * In this hand a player may or may not have an Ace, it may have one, but 
 	 * it is not required. If the player has an Ace, the value of that Ace is always
 	 * one (1) and is inflexible.
 	 * SOFT HAND: 
 	 * Is a hand that has an Ace. In this hand the value of the Ace is 
 	 * eleven (11). In this hand the Ace and its value are key elements.
 	 * This type of hand gives the player an advantage - they choose to 
 	 * add a third card without worrying about going "bust" as the value 
 	 * of the Ace can change to a 1. For example, the term "soft seventeen" 
 	 * is used in the protocol description. In this scenario, the player 
 	 * initially receives an Ace and a six (6) card. Suit is unimportant. 
 	 * The player can choose the HIT command, be dealt a card (say a 10) 
 	 * that would normally bring the card count to over twenty-one (21) - 
 	 * however, in the soft hand the player is safe as the Ace value would 
 	 * change to a one (1).
 	 */
 	public enum RANK {
 		  
 		ACE ("1"),   
 		TWO("2"),		
 		THREE("3"),	
 		FOUR("4"),	
 		FIVE("5"),	
 		SIX("6"),		
 		SEVEN("7"),	
 		EIGHT("8"),	
 		NINE("9"),	
 		TEN("10"),	
 		JACK("J"),	
 		QUEEN("Q"),	
 		KING("K");	
 		
 		//The String value used to concatenate with suit to form a card
 		private final String cardRank;
 		
 		RANK( String cardRank ) {
 			this.cardRank = cardRank;	
 		}
 		
 		/**This getter can be used during the concatenation of a rank with a suit
 		 *in the method this.toString(). E.g. Jack of Diamonds is rank J, suit D
 		 *for a card of JD. */
 		public String getRank() {
 			return cardRank;
 		}
 		
 
 	}
 	
 	/**Instances of card suits*/
 	public enum SUIT {
 		  
 		 CLUBS("C"),		
 		 SPADES("S"),	
 		 HEARTS("H"),	
 		 DIAMONDS("D"), ;
 		 
 		 //The String value used to concatenate with rank to form a card. 
 		 private final String cardSuit;
 		 
 		 SUIT( String cardSuit ) {
 			 this.cardSuit = cardSuit;
 		 }
 		 
 		 /**This getter can be used during the concatenation of a rank with a suit
 		  *in the method this.toString(). E.g. Jack of Diamonds is rank J, suit D
 		 *for a card of JD. */
 		 public String getSuit() {
 			 return cardSuit;
 		 }
 
 	}
 	
 	/******************************
 	* Local variables*/
 	/******************************/
 	
 	private RANK rank = null;
 	private SUIT suit = null;
 	
 	public RANK getRank() {
 		return rank;
 	}
 	
 	public SUIT getSuit() {
 		return suit;
 	}
 	
 	
 	/** Constructor of a card creates a card from a rank and a suit 
 	 * @param rank a valid rank
 	 *  @param suit a valid suit */	
 	public Card( RANK rank, SUIT suit ) {
 		
 		if( rank == null || suit == null) {
 			throw new IllegalArgumentException( "The rank or suit cannot be null");
 		}
 		
 		this.rank = rank;
 		this.suit = suit;	
 	}
 	
 	
 	
 	/**
 	 *Gets the point value of the card. An Array is used since the Ace card, 
 	 * can be take a value of 1 or 11, so need to return an array of those two values .
 	 * 
 	 * Note: To avoid the array, could possibly declare an 
 	 * ACE with a value of 1 and an ACE_SOFT instance with a value of 11 and 
 	 * switch between the two, depending on the choice of the player in 
 	 * specific session instance).
 	  */
 	
 	public int[] getValues(){
 		  
 		  switch( rank) {
 		  case ACE:
 			  int[] value = new int[2];
 			  value[0] = 1;
 			  value[1] = 11;
 			  return value;
 		  case TWO:
 			  return new int[]{2};
 		  case THREE:
 			  return new int[]{3};
 		  case FOUR:
 			  return new int[]{4};
 		  case FIVE:
 			  return new int[]{5};
 		  case SIX:
 			  return new int[]{6};
 		  case SEVEN:
 			  return new int[]{7};
 		  case EIGHT:
 			  return new int[]{8};
 		  case NINE:
 			  return new int[]{9};
 		  case TEN:
 			  return new int[]{10};
 		  case JACK:
 			  return new int[]{10};
 		  case QUEEN:
 			  return new int[]{10};
 		  case KING:
 			  return new int[]{10};	  
 		  default:
 			  return new int[0];
 		  } 
 	}
 
 	/**
 	 * <b>UI:</b> Creates a string representation of the card,
 	 * suitable for showing in a command line client.
 	 * 
 	 * @return Concatenates rank and suit into a string to create a playing card
 	 * For example, 2S, 4C, 5H, AC
 	 */
 	public String toString() {
 		
 		StringBuilder str = new StringBuilder();
 
		str.append(this.rank);
		str.append(this.suit );
 		
 		return str.toString();
 	}
 
 	/* (non-Javadoc)
 	 * @see java.lang.Object#hashCode()
 	 */
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((rank == null) ? 0 : rank.hashCode());
 		result = prime * result + ((suit == null) ? 0 : suit.hashCode());
 		return result;
 	}
 
 	/* (non-Javadoc)
 	 * @see java.lang.Object#equals(java.lang.Object)
 	 */
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		Card other = (Card) obj;
 		if (rank == null) {
 			if (other.rank != null)
 				return false;
 		} else if (!rank.equals(other.rank))
 			return false;
 		if (suit == null) {
 			if (other.suit != null)
 				return false;
 		} else if (!suit.equals(other.suit))
 			return false;
 		return true;
 	}
 	
 }
