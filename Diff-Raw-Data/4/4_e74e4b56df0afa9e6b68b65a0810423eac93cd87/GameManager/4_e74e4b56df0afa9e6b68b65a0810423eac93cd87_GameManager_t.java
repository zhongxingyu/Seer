 package manager;
 
 import java.util.Collections;
 import java.util.List;
 import java.util.ArrayList;
 import cards.*;
 import player.*;
 
 public class GameManager {
 	
 	private List<Player> players;
 	private Pile deck;
 	private Pile communityCards;
 	
 	
 	private int pot;
 	private int bet;
 	
 	public GameManager(){
 		players = new ArrayList<Player>();
 		deck = Deck.fullDeck();
 	}
 	
 	public void addPlayer(Player p){
 		players.add(p);
 	}
 	
 	public void playGames(int hands) {
 		for(int i = 0; i < hands; i++) {
 			playHand(i % players.size());
 		}
 	}
 	
 	private void playHand(int button){
 //		ArrayList<Player> activePlayers = players; 
 //		this will make activePlayers a reference to players, if we run remove on activePlayers, 
 //		they will be removed from players too
 		
 		List<Player> activePlayers = new ArrayList<Player>();
 		Collections.copy(activePlayers,players);
 		
 		bet = 0;
		int raises = 0;
 		dealHoleCards();
 		
 		
 		// Preflop	
 		int index = button;
 		
 		// Small blind
 		index = (index + 1) % activePlayers.size(); 
 		activePlayers.get(index).updateStack(-1);
 		updatePot(1);
 		
 		// Big blind
 		index = (index + 1) % activePlayers.size(); 
 		activePlayers.get(index).updateStack(-21);
 		updatePot(2);
 		
 		
 		
 		for(; activePlayers.size() > 1; index = (index + 1) % activePlayers.size()) {
 			Player player = players.get(index);
			Action action = player.act(Round.PREFLOP,communityCards, bet, raises, pot);
 			switch(action.getType()) {
 			case FOLD: 
 				activePlayers.remove(player);
 				break;
 			case CALL:
 				//TODO: This implementation forces the player to pay max bet and doesn't give him any discounts so he can call (max bet - his previous bet)
 				player.updateStack(-bet);
 				updatePot(bet);
 				break;
 			case RAISE:
 				//TODO: implement this with a solution to the problem in CALL.
 				break;
 			case BET:
 				activePlayers.get(index).updateStack(-bet);
 				updatePot(bet);
 				bet = action.getBet();
 				break;
 				
 			}
 		}
 		communityCards.clear();
 	}
 	
 	private void dealHoleCards() {
 		for(Player player : players) {
 			player.addCard(deck.pop());
 			player.addCard(deck.pop());
 		}
 		
 	}
 	
 	private void updatePot(int delta) {
 		this.pot += delta;
 		
 	}
 }
