 package kata.holdem;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class PokerGame {
 	private List<PokerRound> rounds = new ArrayList<PokerRound>();
 	
 	public PokerRound newRound() {
 		PokerRound round = new PokerRound();
 		rounds.add(round);
 		return round;
 	}
 	
 	public String results() {
 		StringBuilder allResults = new StringBuilder();
 		for (PokerRound round : rounds) {
 			if (allResults.length() > 0)
 				allResults.append('\n');
 			allResults.append(round.results());
 		}
 		return allResults.toString();
 	}
 }
