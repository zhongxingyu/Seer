 /*Source of how many cards in the deck: http://www.squidoo.com/how-many-cards-in-a-deck */
 
 package practiceUno; 
 
 import java.util.*; 
 
 /**
  *
  * @author mike
  */
 public class Deck
 {
     private final int MAX_CARD = 108; 
     private Stack<Card> deck = new Stack<Card>();  
     private Stack<Card> discardDeck = new Stack<Card>();
     
     
     /**
      *
      */
     public Deck()
     {   
        CreateNumbers(); 
        CreateSpecial(); 
        CreateWild(); 
     }
     
     private void AddToDeck(Card c, int numberOfCards)
     {
         for(int i = 0; i < numberOfCards; i++)
             deck.push(c);
     }
      
     private void CreateNumbers()
     {
         for(Card.cardColor color : Card.cardColor.values())
         {
                 AddToDeck( new NumberCard (0, color), 1);
                 for (int i = 1; i < 10; i++) 
                     AddToDeck( new NumberCard (i, color), 2);
         }      
     }
     
     private void CreateSpecial()
     {
         for(SpecialCard.cardValues sp : SpecialCard.cardValues.values())
         {
                 for(Card.cardColor color : Card.cardColor.values())
                 {
                     AddToDeck(new SpecialCard(sp, color), 2);
                 }
         }
     }
     
     private void CreateWild()
     {
         for(WildCard.cardWild wild : WildCard.cardWild.values())
             AddToDeck(new WildCard(wild), 4);
     }
 
     /**
      *
      * @return
      */
     public Card DrawNext()
     {
         Card c; 
         if(!deck.isEmpty())
             return (Card)deck.pop();
         else
             System.out.println("This deck is done. Need to shuffle and replace deck.");
         return null;
 
     }
     
     /**
      *
      * @param c
      */
     public void AddDiscard(Card c)
     {
        System.out.println("Looking at the card that was added.");
        c.Print();
        
        System.out.print("Card on top of discard");
        discardDeck.push(c);
        discardDeck.peek().Print();
        System.out.println("");
        
        if(discardDeck.empty()) //assuming the start of a game
        {
            discardDeck.push(c);
        }
     }
     
     /**
      *
      * @param deckName
      * @return
      */
     public int getSize(String deckName)
     {
        int size = 0; 
        if(deckName.equals("discard"))
        {
            size = discardDeck.size(); 
        }
        else if(deckName.equals("regular"))
        {
            size = deck.size(); 
        }
         return size; 
     }
     
    public void SetUpDiscard()
     {
         discardDeck.add(DrawNext());
     }
 
     
     /**
      *
      */
     public void PrintDeck(String deckName)
     {  
         Stack<Card> tmpDeck = new Stack<Card>(); 
         if(deckName.equals("discard"))
             tmpDeck = discardDeck; 
         else if(deckName.equals("regular"))
             tmpDeck = deck; 
         
         for(Card c : tmpDeck)
         {
             c.Print();
             System.out.println("");
         }
     }
     
     /**
      *
      * @return
      */
     public Stack<Card> GetDeck() 
    {
        return deck; 
    }
    
 }
