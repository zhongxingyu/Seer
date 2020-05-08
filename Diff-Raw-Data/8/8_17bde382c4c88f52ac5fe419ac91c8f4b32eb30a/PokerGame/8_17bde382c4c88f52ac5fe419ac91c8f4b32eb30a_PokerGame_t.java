 package de.pokerdude;
 
 import java.util.ArrayList;
 
 import org.apache.log4j.Logger;
 
 public class PokerGame {
 	
 	static final Logger logger = Logger.getLogger(PokerGame.class);
 	
 	private ArrayList<Player> players = new ArrayList<Player>();
 	
 	private Deck deck = new Deck();
 	
 	private ArrayList<Card> flop = new ArrayList<Card>();
 	private Card turn = null;
 	private Card river = null;
 	
 	
 	private int pot;
 	private int smallblind;
 	private int bigblind;
 	
 	public PokerGame() {
 		logger.setLevel(PokerDude.DEBUGLEVEL);
 		logger.info("Game generated with " + deck.size() + " cards!");
 	}
 	
 	public boolean addPlayer(Player player) {
 		if(players.size() < 10) {
 			return players.add(player);
 		}
 		return false;
 	}
 	
 	public ArrayList<Player> getPlayers() {
 		return players;
 	}
 	
 	
 	public int getSmallBlind() {
 		return smallblind;
 	}
 	
 	public int getBigBlind() {
 		return bigblind;
 	}
 	
 	public Card getTurn() {
 		return turn;
 	}
 	
 	public Card getRiver() {
 		return river;
 	}
 	
 	public Deck getDeck() {
 		return deck;
 	}
 	
 	public ArrayList<Card> getFlop() {
 		return flop;
 	}
 	
 	public ArrayList<Card> getCommonCards() {
 		ArrayList<Card> commonCards = new ArrayList<Card>();
 		if(this.getRiver() != null) commonCards.add(this.getRiver());
 		if(this.getTurn() != null) commonCards.add(this.getTurn());
 		commonCards.addAll(this.getFlop());
 		return commonCards;
 	}
 	
 	public void resetGame() {
 		deck.generateNewDeck();
 		this.flop.clear();
 		this.river = null;
 		this.pot = 0;
 		this.turn = null;
 	}
 	
 	public void playRound() {
 		resetGame();
 		giveCards();
 		showTable();
 		for(Player player: players) {
 			int bet = player.getBetPreFlop();
 			pot += bet;
 			player.credits -= bet;
 		}
 		fillFlop();
 		showTable();
 		for(Player player: players) {
 			int bet = player.getBetPreTurn();
 			pot += bet;
 			player.credits -= bet;
 		}
 		fillTurn();
 		showTable();
 		for(Player player: players) {
 			int bet = player.getBetPreRiver();
 			pot += bet;
 			player.credits -= bet;
 		}
 		fillRiver();
 		showTable();
 		logger.debug("Pot at: " + pot);
 		showResults();
 		//showCredits();
 	}
 	
 	private void showResults() {
 		ArrayList<Card> allCards = new ArrayList<Card>(this.flop);
 		allCards.add(this.river);
 		allCards.add(this.turn);
 		Player winner = null;
 		Powerrating winningResult = null;
 		
 		for(Player player: players) {
 			logger.debug("Player:" + player.name);
 			logger.debug("Cards:");
 			logger.debug(player.getCards());
 			Powerrating result = PokerUtils.evaluateCards(player.getCards(), flop, turn, river);
 			if(winningResult == null || result.compareTo(winningResult) == 1) {
 				winningResult = result;
 				winner = player;
 			}
 			logger.debug("Results:" );
 			logger.debug(result);
 		}
 		logger.debug("=======================");
 		logger.debug("Winner: " + winner.name);
 		logger.debug("He wins with: " + winningResult);
 		logger.debug("He wins the pot: " + pot);
 		logger.debug("=======================");
 		winner.credits += pot;
 	}
 	
 	public void showCredits() {
 		logger.info("=======================");
 		logger.info("Player credits:");
 		for(Player player: players) {
 			logger.info(player.name + ":" + player.credits);
 		}
 		logger.info("=======================");
 	}
 
 	private void giveCards() {
 		for(Player player: players) {
 			ArrayList<Card> hand = new ArrayList<Card>();
 			hand.add(deck.getCard());
 			hand.add(deck.getCard());
 			player.setCards(hand);
 		}
 	}
 	
 	private void fillFlop() {
 		flop.add(deck.getCard());
 		flop.add(deck.getCard());
 		flop.add(deck.getCard());
 	}
 	
 	private void fillRiver() {
 		river = deck.getCard();
 	}
 	
 	private void fillTurn() {
 		turn = deck.getCard();
 	}
 	
 	public void showTable() {
 		logger.debug("Current table:");
 		logger.debug("Flop: ");
 		for(Card card: flop) {
 			logger.debug(card);
 		}
 		logger.debug("Turn:  " + turn);
 		logger.debug("River: " + river);
 	}
 }
