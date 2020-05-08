 package de.feststelltaste.notrick.api.cards.card.suit;
 
 public enum FrenchSuit implements Suit {
 
    CLUB("club", 0), DIAMOND("diamond", 1), HEART("heat", 2), SPADES("Spades", 3);
 
     private String name;
     private int priority;
 
     private FrenchSuit(String name, int priority) {
 	this.name = name;
 	this.priority = priority;
     }
 
     @Override
     public String getName() {
 	return name;
     }
 
     @Override
     public int getPriority() {
 	return priority;
     }
 
}
