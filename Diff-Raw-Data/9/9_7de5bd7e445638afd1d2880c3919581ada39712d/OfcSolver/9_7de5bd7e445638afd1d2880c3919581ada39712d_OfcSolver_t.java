 
 public class OfcSolver {
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		OfcDeck deck = new OfcDeck();
 		deck.initialize();
 		
 		OfcHand player1 = new LongOfcHand();
 		
 		OfcHand player2 = new LongOfcHand();
 //		player1.setHand("/8h4c/AsKsQs", deck);
 //		player2.setHand("Td/JdJh/QdQc", deck);
 	
		player1.setHand("Kd3hKs/As2d9d/JdJc7c7d", deck);
 		player2.setHand("QcAd/6d7sKc/AhKhQhJhTh", deck);
 
 		GameState gs = new GameState(player1, player2, deck);
 		System.out.println(gs);
 
 		long timestamp = System.currentTimeMillis();	
 		System.out.println(gs.getValue(Scorers.getScorers(), true));
 		System.out.println((System.currentTimeMillis() - timestamp) + " ms");
 		gs.shutdown();
 	}
 	
 }
