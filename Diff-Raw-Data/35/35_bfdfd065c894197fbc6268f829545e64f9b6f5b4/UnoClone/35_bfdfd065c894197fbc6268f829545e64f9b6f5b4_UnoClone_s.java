 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package practiceUno;
 
 import java.util.ArrayList;
 
 /**
  *
  * @author mike
  */
 public class UnoClone {
     /**
      *
      * @param args
      */
     public static void main(String[] args)
     { 
         ArrayList<Player> players = new ArrayList<Player>();
         Dealer d = new Dealer(); 
         Deck deck = new Deck(); 
           
         d.Shuffle(deck);
        deck.AddDiscard(deck.DrawNext());
         SetUpPlayers(players, deck);
         
        
        
     }
     
     public static void SetUpPlayers(ArrayList<Player> players, Deck deck)
     {
        for (int i = 0; i < 10; i++) 
            players.add(new Player());
 
         for (int i = 0; i < 7; i++) {
             for (int j = 0; j < 10; j++) {
                 players.get(j).GetCard(deck.DrawNext());
             }
         }
     }
 }
