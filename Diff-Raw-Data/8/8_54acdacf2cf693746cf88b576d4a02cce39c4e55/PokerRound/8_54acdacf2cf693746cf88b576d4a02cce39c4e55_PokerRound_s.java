 package kata.holdem;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 public class PokerRound {
 	private final PokerGame game;
 	private Map<String, HoleCards> playerInfo = new HashMap<String, HoleCards>();
	private List<Deal> deals = new ArrayList<Deal>();
 	
 	PokerRound(PokerGame game) {
 		this.game = game;
 	}
 	
 	public PokerRound deal(String player, String holeCard1, String holeCard2) {
 		playerInfo.put(player, new HoleCards(Card.from(holeCard1), Card.from(holeCard2)));
 		return this;
 	}
 		
 	public PokerRound dealFlop(String card1, String card2, String card3) {
 		deals.add(new Deal(Cards.from(card1, card2, card3)));
 		return this;
 	}
 	
 	public PokerRound dealTurn(String card) {
 		deals.add(new Deal(Cards.from(card)));
 		return this;
 	}
 	
 	public PokerRound dealRiver(String card) {
 		deals.add(new Deal(Cards.from(card)));
 		return this;
 	}
 
 	public PokerRound fold(String player) {
 		deals.get(deals.size() - 1).fold(player);
 		return this;
 	}
 
 	@Override
 	public String toString() {
 		RoundWinners winners = new RoundWinners(playerInfo, Deal.communityCards(deals));
 		
 		StringBuilder results = new StringBuilder();
 		for (String player : game.getPlayers()) {
 			if (results.length() > 0) results.append("\n");
 
 			boolean playerFolded = false;
 			
 			results.append(player).append(": ").append(playerInfo.get(player).cardsSummary());
 			for (Deal deal : deals) {
				results.append(' ').append(deal.cardsSummary());
 				if (deal.folded(player)) {
 					results.append(" [folded]");
 					playerFolded = true;
 					break;
 				}
 			}
 			
 			// if player folded, no need to rank their hand as they can't possibly be a winner.
 			if (playerFolded) continue;
 			
 			RankedHand rankedHand = winners.getRankedHand(player);
 			results.append(" [").append(rankedHand.rankDescription());
 			if (!rankedHand.rankedCards().isEmpty())
 				results.append(' ').append(Cards.toString(rankedHand.rankedCards()));
 			if (!rankedHand.kickers().isEmpty())
 				results.append(" Kicker(s) ").append(Cards.toString(rankedHand.kickers()));
 			results.append(']');
 			if (winners.isWinner(player)) results.append(" (Winner)");
 		}
 		return results.toString();
 	}
 }
