 package edu.ntnu.brunson.manager;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import edu.ntnu.brunson.cards.*;
 import edu.ntnu.brunson.player.Action;
 import edu.ntnu.brunson.player.Player;
 import edu.ntnu.brunson.player.PlayerCycler;
 
 
 public class Game {
 	
 	private PlayerCycler players;
 	private int pot;
 	private Pile community;
 	private Pile deck;
 
 	public Game(List<Player> playerList,int button){
 		players = PlayerCycler.cycler(playerList,button);
 		pot = 0;
 		deck = Deck.fullDeck().shuffle();
 		community = new Pile();
 	}
 		
 	
 	public void playHand(){
 		
 		//Small blind
 		if(players.hasNext())
 			pot += players.next().bet(1);
 		
 		// Big blind
 		if(players.hasNext())
 			pot += players.next().bet(2);
 		
 		dealHoleCards(players);
 		for(Round round : Round.values()){
 			community.add(deck.deal(round.cardsDealt()));	
 			writeToHH(round, null, 0);
 			boolean winner = playRound(round,players,0);
 			if(winner) {
 				players.list().get(0).updateStack(pot);
 				return;
 			}
 			players.reset();
 		}
 		
 		//Determine winner(s) at showdown
 		List<Player> winners = showdown(players.list());
 		int potSlice = pot/winners.size();
 		for(Player player : winners)
 			player.updateStack(potSlice);
 	}
 	
 	
 	private boolean playRound(Round round,Iterator<Player> players,int bet){
 		int raises = 0;
 		//Cycle through each remaining active player, hasNext return false when there's only one player left.
 		while(players.hasNext()) {
 			Player player = players.next();
 			if(player.getAmountWagered() == bet)
 				return false;
 			Action action = player.act(round,community, bet, raises, pot);
 			player.addAction(round,action);
 			switch(action.getType()) {
 			case FOLD: 
 				players.remove();
 				break;
 			case CHECK:
 			case CALL:
 				pot += player.bet(bet);
 				break;
 			case BET:
 			case RAISE:
 				bet = action.getBet();
 				pot += player.bet(bet);
 				raises++;
 				break;
 			}
 		}
 		return true;
 	}
 	
 	private List<Player> showdown(List<Player> playerList) {
 		List<Player> winners = new ArrayList<Player>();
 		writeToHH(null, playerList, 1);
		HandRating best = HandRating.rate(playerList.get(0).getHand());
 		for(Player player : playerList){
 			
			HandRating rating = HandRating.rate(player.getHand());
 			int comp = best.compareTo(rating);
 			if(comp == 0)
 				winners.add(player);
 			else if (comp < 0){
 				winners.clear();
 				winners.add(player);
 			}
 		}
 		writeToHH(null, winners, 2);
 		return winners;
 	}
 	
 	//Need to print the community cards, what happens at showdown and who the winners are.
 	private void writeToHH(Round round, List<Player> players, int i) {
 		if(round != null) {
 			switch(round) {
 		
 			//community cards
 			case FLOP: Output.addToHH("Dealing flop: [" + community.getCard(0).toString() + ", "  + community.getCard(1).toString() + ", "  + community.getCard(2).toString() + "]"); return;
 			case TURN: Output.addToHH("Dealing turn: [ " + community.getCard(3).toString() +"]"); return;
 			case RIVER: Output.addToHH("Dealing river: [ " + community.getCard(4).toString() +"]"); return;
 
 			}
 		}
 		switch(i) {
 		case 0: return;
 		case 1: 
 			//showdown
 			if(players == null) {
 				throw new RuntimeException("This is not good.");
 			}
 			Output.addToHH("Showdown: ");
 			for(Player player : players) {
 				Output.addToHH(player.getName() + " shows " + player.getHand().toString() +".");
 			}
 			
 			return;
 		case 2:
 			Output.addToHH("Final pot is: " +java.lang.Integer.toString(pot));
 			for(Player player : players) {
 				Output.addToHH(player.getName() + " wins " +java.lang.Integer.toString(pot / players.size()) + player.getHand().toString() +".");	
 			}
 			return;
 		default: throw new RuntimeException("Failed to print HH properly.");
 		}
 	}
 	
 	private void dealHoleCards(PlayerCycler pc) {
 		while(true) {
 			Player player = pc.next();
 			if(player.getHand().size() == 2) {
 				return;
 			}
 			player.addCard(deck.pop());
 			player.addCard(deck.pop());
 		}
 	}
 	
 }
