 import java.util.*;
 public class Deck{
     ArrayList<Card> deck;
     ArrayList<Card> removedCards;
 
     public Deck(){
         deck = new ArrayList<Card>();
         generateDeck();
     }
     
     public void generateDeck(){
         int i = 2;
         for(; i < 11; i ++){
             generateSuits("" + i, i);
         }
         
         generateSuits("JACK" , 11);
         generateSuits("QUEEN" , 12);
         generateSuits("KING" , 13);
         generateSuits("ACE" , 14);
     }
 
     public void generateSuits(String type , int value){
         for(int j = 0; j < 4; j ++){
             switch (j){
             case 0: deck.add(new Card(type, "CLUB", value));
                 break;
             case 1: deck.add(new Card(type, "SPADE" , value));
                 break;
             case 2: deck.add(new Card(type, "DIAMOND" , value));
                 break;
             case 3: deck.add(new Card(type, "HEART" , value));
             }
         }
     }
 
     public void shuffle(){
         Random r = new Random();
         for(int i = 0; i < deck.size(); i ++){
             Collections.swap(deck , r.nextInt(52) , r.nextInt(52));
         }
     }
     
     public Card draw(){
         Card ret = deck.remove(0);
         removedCards.add(ret);
         return ret;
     }
 
     public ArrayList<Card> removedCards(){
         return removedCards;
     }
     
     public int getNumRemovedCards(){
         return removedCards.size();
     }
     
     public int getNumCards(){
         return deck.size();
     }
 }
     
         
         
             
 
    
