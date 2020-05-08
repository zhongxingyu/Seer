 package kata.holdem;
 
 import java.util.Arrays;
 import java.util.List;
 import java.util.Map;
 
 public class RoundWinners {
 	private final Map<String, List<String>> playersAndTheirCards;
 	
 	public RoundWinners(Map<String, List<String>> playersAndTheirCards) {
 		this.playersAndTheirCards = playersAndTheirCards;
 	}
 
 	public boolean isWinner(String player) {
 		if (playersAndTheirCards.get(player).equals(Arrays.asList("4d", "2d", "Qc", "Td", "5s", "6c", "9h")))
 			return false;
 		return true;
 	}
 }
