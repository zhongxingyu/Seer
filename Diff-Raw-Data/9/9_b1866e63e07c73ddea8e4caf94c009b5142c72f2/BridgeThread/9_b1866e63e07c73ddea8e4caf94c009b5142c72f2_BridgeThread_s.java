 package com.madavan.bridge.server;
 
 
 import java.util.ArrayList;
 import java.io.IOException;
 

 
 public class BridgeThread extends Thread {
 
   private ArrayList<BridgePlayer> _players;
   
   public BridgeThread(ArrayList<BridgePlayer> players) {
     _players = players;
   }
   
   @Override
   public void run() {
     // Tell players game has begun
     // Reset, shuffle, and deal deck
     
     // Bidding
     //  - Pass is the bid CLUBS,0 (or we could do PASS)
     //  - Use Rank, Suit compareTo methods
     
     // Play Game
   }
   
   private int compareCard(Card c1, Card c2, Suit trump, Suit trick) {
     int rankCompare = _rank.compareTo(oth.getRank());
     boolean c1Trump = card1.getSuit().equals(trump);
     boolean c2Trump = card2.getSuit().equals(trump);
 		boolean c1Trick = card1.getSuit().equals(trick);
     boolean c2Trick = card2.getSuit().equals(trick);
     
     if(c1Trump && c2Trump)
       return rankCompare;
     else if(c1Trump)
       return 1;
     else if(c2Trump)
       return -1;
     
     if(c1Trick && !c2Trick)
       return 1;
     else if(!c1Trick && c2Trick)
       return -1;
     else
       return rankCompare;
   }
 }
