 package de.nordakademie.nakjava.gamelogic.shared.playerstate;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 
 public class CardSet implements Serializable {
 
 	// TODO questionable return-types: boolean (operation worked) or
 	// List<>(performed for those cards) or void
 	private List<String> deck = new ArrayList<>();
 	private List<String> hand = new ArrayList<>();
 	private List<String> cemetry = new ArrayList<>();
 
 	public void shuffle() {
 		deck.addAll(cemetry);
 	}
 
 	public CardSet() {
 
 	}
 
 	public CardSet(Set<String> cards) {
 		deck.addAll(cards);
 	}
 
 	public void addOneCard(String card) {
 		deck.add(card);
 	}
 
 	public void addAllCards(Set<String> cards) {
 		deck.addAll(cards);
 	}
 
 	public boolean discardCardFromHand(String card) {
 		if (hand.remove(card)) {
 			cemetry.add(card);
 			return true;
 		}
 		return false;
 	}
 
 	public void discardRandomCardFromHand() {
 		String card = hand
 				.remove((int) (Math.random() * getNumberOfCardsOnHand()));
 		cemetry.add(card);
 	}
 
 	public int getNumberOfCardsOnHand() {
 		return hand.size();
 	}
 
 	public List<String> getCardsOnHand() {
 		return hand;
 	}
 
 	public String drawCardFromDeck() {
 		if (deck.size() != 0) {
 			String card = deck.remove((int) (Math.random() * deck.size()));
 			hand.add(card);
 			return card;
 		} else if (getCardSetSize() > hand.size()) {
 			shuffle();
 			return drawCardFromDeck();
 		} else {
 			return null;
 
 		}
 	}
 
 	public boolean drawUntilNCardsOnHand(int n) {
 		if (n <= getCardSetSize()) {
 			int numberOfDrawnCards = hand.size();
 			while (numberOfDrawnCards < n) {
 				drawCardFromDeck();
 				numberOfDrawnCards++;
 			}
 			return true;
 		}
 		return false;
 	}
 
 	public List<String> drawNCardsFromDeck(int n) {
 		List<String> list = new ArrayList<>();
 		int i = 1;
 		String card = drawCardFromDeck();
 		if (card != null) {
 			list.add(card);
 		}
 		while (i < n && card != null) {
 			drawCardFromDeck();
 			list.add(card);
 			i++;
 		}
 		return list;
 	}
 
 	public void discardAllCardsFromHand() {
 		for (String card : hand) {
 			discardCardFromHand(card);
 		}
 	}
 
 	public int getCardSetSize() {
 		return hand.size() + deck.size() + cemetry.size();
 	}
 }
