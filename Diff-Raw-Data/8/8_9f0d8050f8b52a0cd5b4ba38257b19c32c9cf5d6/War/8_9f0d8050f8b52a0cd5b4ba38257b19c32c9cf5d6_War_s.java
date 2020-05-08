 package cards.war;
 
 import java.security.SecureRandom;
 
 import cards.api.Card;
 import cards.api.Deck;
 import cards.api.TypeCardComparator;
 
 public class War {
 	
 	public static void main(String[] args) {
		new War();
 	}
 	
 	private SecureRandom random = new SecureRandom();
 	private Deck deckDealer = new Deck();
 	private Deck deckOne = new Deck();
 	private Deck deckTwo = new Deck();
 	private Deck deckOneRisk = new Deck();
 	private Deck deckTwoRisk = new Deck();
 	
 	public void execute() {
 		deckDealer.fill();
 		deckDealer.shuffle(random);
 		System.out.println("Original Dealer Shuffled: " + deckDealer.toString());
 		
 		deckDealer.dealTop(deckOne, deckTwo);
 		System.out.println("Original Deck One: " + deckOne.toString());
 		System.out.println("Original Deck Two: " + deckTwo.toString());
 		System.out.println("----------------------------------------------------------");
 		while(!deckOne.isEmpty() && !deckTwo.isEmpty()) {
 			Card card1 = deckOne.removeTop();
 			Card card2 = deckTwo.removeTop();
 			int comparison = card1.compare(card2, TypeCardComparator.instance);
 			// if the comparison is in deckOne's favor
 			if(comparison == 1) {
 				// show the risk, and deal it, if there is any
 				if(deckOneRisk.isEmpty() && deckTwoRisk.isEmpty()) {
 					System.out.println(card1.toString() + " vs " + card2.toString() + ": Deck One wins the battle!");
 				} else {
 					System.out.println(card1.toString() + " (" + deckOneRisk.toString() + ") vs " + card2.toString() + " (" + deckTwoRisk.toString() + "): Deck One wins the battle!");
 					deckOneRisk.dealBottom(deckOne);
 					deckTwoRisk.dealBottom(deckOne);
 				}
 				// give the cards to the winner
 				deckOne.putBottom(card1);
 				deckOne.putBottom(card2);
 			// if the comparison is in deckTwo's favor
 			} else if(comparison == -1) {
 				// show the risk, and deal it, if there is any
 				if(deckOneRisk.isEmpty() && deckTwoRisk.isEmpty()) {
 					System.out.println(card1.toString() + " vs " + card2.toString() + ": Deck Two wins the battle!");
 				} else {
 					System.out.println(card1.toString() + " (" + deckOneRisk.toString() + ") vs " + card2.toString() + " (" + deckTwoRisk.toString() + "): Deck Two wins the battle!");
 					deckOneRisk.dealBottom(deckTwo);
 					deckTwoRisk.dealBottom(deckTwo);
 				}
 				// give the cards to the winner
 				deckTwo.putBottom(card1);
 				deckTwo.putBottom(card2);
 			// if the comparison is neutral
 			} else if(comparison == 0) {
 				// show the risk if there is any
 				if(deckOneRisk.isEmpty() && deckTwoRisk.isEmpty()) {
 					System.out.println(card1.toString() + " vs " + card2.toString() + ": Clash!");
 				} else {
 					System.out.println(card1.toString() + " (" + deckOneRisk.toString() + ") vs " + card2.toString() + " (" + deckTwoRisk.toString() + "): Clash!");
 				}
 				// create a risk for the next hand
 				deckOneRisk.putBottom(card1);
 				deckTwoRisk.putBottom(card2);
 				int remaining = 3;
 				if(deckOne.getRemaining() <= remaining) {
 					remaining = deckOne.getRemaining() - 1;
 				}
 				if(deckTwo.getRemaining() <= remaining) {
 					remaining = deckTwo.getRemaining() - 1;
 				}
 				for(int i = 0; i < remaining; i++) {
 					deckOneRisk.putBottom(deckOne.removeTop());
 					deckTwoRisk.putBottom(deckTwo.removeTop());
 				}
 			}
 		}
 		System.out.println("----------------------------------------------------------");
 		if(deckOne.isEmpty()) {
			System.out.println("Deck Two has succeded in war: " + deckTwo.toString());
 		} else if(deckTwo.isEmpty()) {
			System.out.println("Deck One has succeded in war: " + deckOne.toString());
 		} else {
 			throw new RuntimeException();
 		}
 	}
 	
 }
