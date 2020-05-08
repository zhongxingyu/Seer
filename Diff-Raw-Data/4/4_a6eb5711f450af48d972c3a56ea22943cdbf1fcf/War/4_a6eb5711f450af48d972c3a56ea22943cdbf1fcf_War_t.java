 package cards.war;
 
 import java.util.Random;
 
 import cards.api.Card;
 import cards.api.Deck;
 import cards.api.TypeCardComparator;
 
 public class War {
 
 	// Play a Game
 	public static void main(String[] args) {
 		new War(new Random(), true).execute();
 	}
 
 	// Benchmark
 	/*public static void main(String[] args) {
 		Random random = new Random();
 		long startTime = System.currentTimeMillis();
 		int amount = 0;
 		while(true) {
 			new War(random, false).execute();
 			amount++;
 			if(System.currentTimeMillis() - startTime < 1000L) {
 				continue;
 			}
 			System.out.println(amount + " Games/s");
 			startTime = System.currentTimeMillis();
 			amount = 0;
 		}
 	}*/
 
 	private Deck deckDealer = new Deck();
 	private Deck deckOne = new Deck();
 	private Deck deckTwo = new Deck();
 	private Deck deckOneRisk = new Deck();
 	private Deck deckTwoRisk = new Deck();
 	private Random random;
 	private boolean debug;
 
 	public War(Random random, boolean debug) {
 		this.random = random;
 		this.debug = debug;
 	}
 
 	public void execute() {
 		deckDealer.fill();
 		deckDealer.shuffle(this.random);
 		if(this.debug) System.out.println("Original Dealer Shuffled: " + deckDealer.toString());
 
 		deckDealer.dealTop(deckOne, deckTwo);
 		if(this.debug) System.out.println("Original Deck One: " + deckOne.toString());
 		if(this.debug) System.out.println("Original Deck Two: " + deckTwo.toString());
 		if(this.debug) System.out.println("----------------------------------------------------------");
 		while(!deckOne.isEmpty() && !deckTwo.isEmpty()) {
 			Card card1 = deckOne.removeTop();
 			Card card2 = deckTwo.removeTop();
			int comparison = card1.compare(card2, TypeCardComparator.instance);
 			// if the comparison is in deckOne's favor
 			if(comparison == 1) {
 				// show the risk, and deal it, if there is any
 				if(deckOneRisk.isEmpty() && deckTwoRisk.isEmpty()) {
 					if(this.debug) System.out.println(card1.toString() + " vs " + card2.toString() + ": Deck One wins the battle!");
 				} else {
 					if(this.debug) System.out.println(card1.toString() + " (" + deckOneRisk.toString() + ") vs " + card2.toString() + " (" + deckTwoRisk.toString() + "): Deck One wins the battle!");
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
 					if(this.debug) System.out.println(card1.toString() + " vs " + card2.toString() + ": Deck Two wins the battle!");
 				} else {
 					if(this.debug) System.out.println(card1.toString() + " (" + deckOneRisk.toString() + ") vs " + card2.toString() + " (" + deckTwoRisk.toString() + "): Deck Two wins the battle!");
 					deckOneRisk.dealBottom(deckTwo);
 					deckTwoRisk.dealBottom(deckTwo);
 				}
 				// give the cards to the winner
 				deckTwo.putBottom(card2);
 				deckTwo.putBottom(card1);
 			// if the comparison is neutral
 			} else if(comparison == 0) {
 				// show the risk if there is any
 				if(deckOneRisk.isEmpty() && deckTwoRisk.isEmpty()) {
 					if(this.debug) System.out.println(card1.toString() + " vs " + card2.toString() + ": Clash!");
 				} else {
 					if(this.debug) System.out.println(card1.toString() + " (" + deckOneRisk.toString() + ") vs " + card2.toString() + " (" + deckTwoRisk.toString() + "): Clash!");
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
 		if(this.debug) System.out.println("----------------------------------------------------------");
 		if(deckOne.isEmpty()) {
 			if(this.debug) System.out.println("Deck Two has succeeded in war: " + deckTwo.toString());
 		} else if(deckTwo.isEmpty()) {
 			if(this.debug) System.out.println("Deck One has succeeded in war: " + deckOne.toString());
 		} else {
 			throw new RuntimeException();
 		}
 	}
 
 }
