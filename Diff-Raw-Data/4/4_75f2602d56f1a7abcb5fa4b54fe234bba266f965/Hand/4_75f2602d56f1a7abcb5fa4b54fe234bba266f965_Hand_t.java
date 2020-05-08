 
 
 package poker;
 
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.TreeSet;
 /**
  * A Hand object represents a poker hand containing at most seven cards.
  * @see Card
  */
 public class Hand 
 {
     private Set<Card> cards;
     
     /**
      * Class constructor.
      */
     public Hand()
     {
         this.cards = new HashSet();
     }
     /**
      * Creates a hand containing the specified cards.
      * @param cards Specifies up to seven card objects to be included in the hand.
      */
    public Hand(Card... cards)
     {
        this();
         if (cards.length > 7)
         {
             throw new IllegalArgumentException("A hand can contain at most seven cards.");
         }
         if (cards == null)
         {
             throw new NullPointerException("Card must not be null");
         }
         this.cards.addAll(Arrays.asList(cards));
     }            
     
     /**
      * Adds a card to the Hand. At most seven cards can be added to the hand.
      * @param c the card to be added
      * @return Returns true if the card was successfully added, false otherwise.
      */
     public boolean addCard(Card c)
     {
         if (c == null)
         {
             throw new NullPointerException("Card must not be null.");
         }
         if (this.cards.size() == 7)
         {
             return false;
         }
         return this.cards.add(c);        
     }
     
     /**
      * Returns the cards in the hand.
      * @return A shallow copy of the set of cards.
      */
     public TreeSet<Card> getCards()
     {
         return new TreeSet<>(this.cards);
     }  
     
     /**
      * Returns number of cards in hand.
      * @return An integer in the interval [0,7]. 
      */
     public int size()
     {
         return this.cards.size();
     }
 }
