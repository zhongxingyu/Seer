 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Model;
 
 /**
  *
  * @author Oliver
  */
 public class Hand {
 
     private Card[] cards;
 
     public Hand(Deck d) {
 
         cards = new Card[5];
         for (int i = 0; i < cards.length; i++) {
             cards[i] = d.drawFromDeck();
         }
     }
 
     public void showCards() {
         for (int i = 0; i < cards.length; i++) {
             System.out.println(cards[i]);
         }
     }
 
     public static void main(String[] args) {
         Deck d = new Deck();
         d.shuffle();
         Hand h = new Hand(d);
         h.showCards();
        
     }
 }
