 package com.github.kpacha.jkata.pokerhand;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
 
 import com.github.kpacha.jkata.pokerhand.hand.Flush;
 import com.github.kpacha.jkata.pokerhand.hand.FourOfAKind;
 import com.github.kpacha.jkata.pokerhand.hand.FullHouse;
 import com.github.kpacha.jkata.pokerhand.hand.HigherCard;
 import com.github.kpacha.jkata.pokerhand.hand.Pair;
 import com.github.kpacha.jkata.pokerhand.hand.Straight;
 import com.github.kpacha.jkata.pokerhand.hand.StraightFlush;
 import com.github.kpacha.jkata.pokerhand.hand.ThreeOfAKind;
 import com.github.kpacha.jkata.pokerhand.hand.TwoPairs;
 
 public class PokerHand {
 
     private List<PokerCard> hand;
     private List<AbstractPokerHandArchetype> archetypes = new LinkedList<AbstractPokerHandArchetype>() {
 	{
 	    add(new StraightFlush());
 	    add(new FourOfAKind());
 	    add(new FullHouse());
 	    add(new Flush());
 	    add(new Straight());
 	    add(new ThreeOfAKind());
 	    add(new TwoPairs());
 	    add(new Pair());
 	    add(new HigherCard());
 	}
     };
     private AbstractPokerHandArchetype handArchetype;
 
     public PokerHand(String card1, String card2, String card3, String card4,
 	    String card5) {
 	hand = new ArrayList<PokerCard>(5);
 	hand.add(new PokerCard(card1));
 	hand.add(new PokerCard(card2));
 	hand.add(new PokerCard(card3));
 	hand.add(new PokerCard(card4));
 	hand.add(new PokerCard(card5));
 	Collections.sort(hand);
 	handArchetype = findHandArchetype();
     }
 
     public String findHand() {
 	return handArchetype.getName() + " : "
 		+ handArchetype.getHandDescription();
     }
 
     private AbstractPokerHandArchetype findHandArchetype() {
 	AbstractPokerHandArchetype result = null;
 	for (AbstractPokerHandArchetype handArchetype : archetypes) {
 	    handArchetype.processHand(this);
 	    if (handArchetype.match()) {
 		result = handArchetype;
 		break;
 	    }
 	}
 	return result;
     }
 
     public List<PokerCard> getCards() {
 	return hand;
     }
 
    public AbstractPokerHandArchetype getArchetype() {
	return handArchetype;
     }
 
     public int compareTo(PokerHand hand) {
	return handArchetype.compareTo(hand.getArchetype());
     }
 }
