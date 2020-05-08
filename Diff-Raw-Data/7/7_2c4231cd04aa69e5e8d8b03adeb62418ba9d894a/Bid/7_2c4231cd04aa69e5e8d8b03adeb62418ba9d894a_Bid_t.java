 package com.madavan.bridge.cards;
 
 public class Bid implements Comparable<Bid> {
 
   private Rank _rank;
   private Suit _suit;
 
   public Bid(Rank rank, Suit suit) {
     _rank = rank;
     _suit = suit;
   }
 
   public Rank getRank() {
     return _rank;
   }
 
   public Suit getSuit() {
     return _suit;
   }
 
   @Override
   public int compareTo(Bid oth) {
     if(_rank.compareTo(oth.getRank()) > 0 ||
        _rank.compareTo(oth.getRank()) == 0 &&
        _suit.compareTo(oth.getSuit()) >  0)
       return 1;
    else if(_rank.compareTo(oth.getRank()) == 0 &&
            _suit.compareTo(oth.getSuit()) == 0)
       return 0;
     else
       return -1;
   }
 
   @Override
   public String toString() {
    return _rank.toString() + "," + _suit.toString();
   }
   
   public static Bid fromString(String str) {
     String[] val = str.split(",");
     return new Bid(Rank.valueOf(val[0]), Suit.valueOf(val[1]));
   }
 }
