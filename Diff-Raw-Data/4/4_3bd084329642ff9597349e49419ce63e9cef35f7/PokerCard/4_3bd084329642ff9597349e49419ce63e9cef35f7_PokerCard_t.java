 package com.github.kpacha.jkata.pokerhand;
 
 import java.util.HashMap;
 import java.util.Map;
 
 public class PokerCard {
 
     private String card;
     private Map<Character, Integer> specialCards = new HashMap<Character, Integer>(
 	    4) {
 	{
 	    put('A', 13);
 	    put('K', 12);
 	    put('Q', 11);
 	    put('J', 10);
 	}
     };
 
     public PokerCard(String card) {
 	this.card = card;
     }
 
     public int getNumericValue() {
 	if (specialCards.containsKey(card.charAt(0)))
 	    return specialCards.get(card.charAt(0));
 	return Integer.parseInt(card.substring(0, 1));
     }

    public int compareTo(PokerCard card) {
	return getNumericValue() - card.getNumericValue();
    }
 }
