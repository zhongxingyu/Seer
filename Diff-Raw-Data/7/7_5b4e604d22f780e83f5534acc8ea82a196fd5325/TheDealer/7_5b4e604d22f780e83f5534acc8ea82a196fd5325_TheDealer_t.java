 package game_engine;
 
 import game_engine.hand_ranks.HandRank;
 
 public class TheDealer {
 
 	private static CardInHand[] hand;
 	private static DeckOfCards deck;
 	private static long credits;
 	private static long currentBet;
 	private static long currentWin;
 	
 	private static TheDealer instance = new TheDealer();
 
 	private TheDealer() {
 		deck = DeckOfCards.getShuffeledDeckInstance();
		credits = 20;
		currentBet = 3;
 		hand = new CardInHand[5];
 		for (int i=0; i<hand.length; i++) {
 			hand[i] = new CardInHand();
 		}
 	}
 	
 	public static long getCredits() {
 		return credits;
 	}
 
 	public static void setCredits(long credits) {
 		TheDealer.credits = credits;
 	}
 
 	public static long getCurrentBet() {
 		return currentBet;
 	}
 
 	public static void setCurrentBet(long currentBet) {
 		TheDealer.currentBet = currentBet;
 	}
 	
 	public static long getCurrentWin() {
 		return currentWin;
 	}
 	
 	public static void setCurrentWin(long currentWin) {
 		TheDealer.currentWin = currentWin;
 	}
 
 	public static TheDealer getTheDealerInstance() {
 		return instance;
 	}
 	
 	public CardInHand[] drawHand() {
 		setCurrentWin(0);
		if (credits<=0) {
 			throw new RuntimeException("You don't have enough credits.");
 		}
 		if (credits<currentBet && credits>0) {
 			currentBet = credits;
 		}
 		deck = DeckOfCards.getShuffeledDeckInstance();
 		for (int i=0; i<5; i++) {
 			Card newCard = deck.drawNextCardFromDeck();
 			setGameHandCard(i, newCard);
 		}
 //		deck.showDeck();
 //		System.out.println("Credits: "+TheDealer.getCredits()+"   Current bet: "+TheDealer.getCurrentBet());
 		System.out.println();
 		credits = credits - currentBet;
 		return hand;
 	}
 	
 	public CardInHand[] drawHand(GameHand playedHand) {
 //		if (credits<0) {
 //			throw new RuntimeException("You don't have enough credits.");
 //		}
 //		if (credits<currentBet && credits>0) {
 //			currentBet = credits;
 //		}
 		setCurrentWin(0);
 		for (int i=0; i<5; i++) {
 			if (!playedHand.getPlayingCard(i).isHold()) {
 				Card newCard = deck.drawNextCardFromDeck();
 				setGameHandCard(i, newCard);
 			}
 		}
 		return hand;
 	}
 	
 	public static HandRank evaluateGameHand(GameHand h) {
 		HandEvaluator e = new HandEvaluator();
 		HandRank rank = e.evaluateHand(h);
 		setCurrentWin(rank.getWinRatio()*getCurrentBet());
 		setCredits(getCredits()+getCurrentWin());
 		return rank;
 	}
 	
 	private void setGameHandCard(int position, Card newCard) {
 		if (position<0 || position>4) {
 			throw new RuntimeException("Error! Card positin must be in range 0-4.");
 		}
 		if (newCard == null) {
 			throw new RuntimeException("Error! newCard cannot be null.");
 		}
 		hand[position].setCardNumber(newCard.getCardNumber());
 		hand[position].setCardSuit(newCard.getCardSuit());
 		hand[position].setCardImage(newCard.getCardImage());
 		
 		hand[position].setHold(false);
 	}
 }
