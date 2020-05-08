 package laughing_wight.participants;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import laughing_wight.model.Card;
 import laughing_wight.model.Deck;
 import laughing_wight.model.Hand;
 import laughing_wight.model.Round;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class Dealer implements GameState {
 	private static final int SMALL_BLIND = 1;
 
 	private static final int BIG_BLIND = 2;
 
 	private static Logger logger = LoggerFactory.getLogger(Dealer.class);
 
 	private List<Player> queuedPlayers;
 	private List<Player> players;
 	
 	protected Deck deck;
 	private Round round;
 	private Card[] communalCards;
 	private Map<Player,Integer> bets;
 	private Map<Player,Boolean> active;
 	private Map<Player, Card[]> holeCards;
 	private Player lastFolder;
 
 
 
 
 	public Dealer(){
 		queuedPlayers = new ArrayList<Player>();
 		communalCards = new Card[5];
 	}
 
 	public void addPlayer(Player player){
 		queuedPlayers.add(player);
 	}
 
 	public void removePlayer(Player player){
 		queuedPlayers.remove(player);
 	}
 
 	public GameResult runGame(int gameNumber){
 		setupGame(gameNumber);
 
 		for(Round chosenRound : Round.values()){
 			this.round = chosenRound;
 			
 			//Deal cards, if any.
 			switch(round){
 			case FLOP:
 				Card flop1 = deck.draw();
 				Card flop2 = deck.draw();
 				Card flop3 = deck.draw();
 				logger.trace("Flop was {} {} {}.", flop1, flop2, flop3);
 				communalCards[0] = flop1;
 				communalCards[1] = flop2;
 				communalCards[2] = flop3;
 				break;
 			case TURN:
 				Card turn = deck.draw();
 				logger.trace("Turn was {}.", turn);
 				communalCards[3] = turn;
 				break;
 			case RIVER:
 				Card river = deck.draw();
 				logger.trace("River was {}.",river);
 				communalCards[4] = river;
 				break;
 			default:
 				//Do nothing.
 			}
 			
 			//Do betting
 			doBetting(gameNumber);
 			
 			//Check for victory by folding.
 			if(getActivePlayerCount()== 1){
 				for(Entry<Player,Boolean> entry : active.entrySet()){
 					if(entry.getValue() == true){
 						List<Player> winners = new ArrayList<Player>();
 						winners.add(entry.getKey());
 						
 						return new GameResult(bets, winners);
 					}
 				}
 			}else if(getActivePlayerCount() == 0){ //This works around simplistic players who might fold despite being the only player in the game.
 				List<Player> winners = new ArrayList<Player>();
 				winners.add(lastFolder);
 				
 				return new GameResult(bets, winners);
 			}
 		}
 		
 		//We've reached showdown. Compare active players's cards.
 		//Get the active players.
 		List<PlayerAndHand> activePlayers = new ArrayList<PlayerAndHand>();
 		for(Entry<Player,Boolean> entry : active.entrySet()){
 			if(entry.getValue() == true){
 				PlayerAndHand plyAndHand = new PlayerAndHand(entry.getKey(),Hand.getBestHand(holeCards.get(entry.getKey()), communalCards));
 				activePlayers.add(plyAndHand);
 			}
 		}
 		//Edge case: Poor players might fold when they're the only player around.
 		assert activePlayers.size() > 0;
 		//Compare their hands.a
 		Collections.sort(activePlayers);
 		
 		//Get the winner, or tied winners.
 		List<Player> winners = new ArrayList<Player>();
 		winners.add(activePlayers.get(activePlayers.size()-1).player);
 		int i = activePlayers.size() - 2;
 		while(activePlayers.get(i).compareTo(activePlayers.get(activePlayers.size()-1)) == 0 && i > 0){
 			winners.add(activePlayers.get(i).player);
 			i--;
 		}
 		GameResult ret = new GameResult(bets, winners);
 		return ret;
 	}
 
 	
 
 	private void setupGame(int gameNumber) {
 		//Copy the canonical playerlist from the editable list.
 		players = new ArrayList<Player>();
 		for(Player ply : queuedPlayers){
 			players.add(ply);
 		}
 		lastFolder = null;
 		//Shuffle the deck and clear the game state.
 		deck = new Deck();
 		bets = new HashMap<Player,Integer>();
 		active = new HashMap<Player,Boolean>();
 		holeCards = new HashMap<Player,Card[]>();
 		for (Player ply : players) {
 			bets.put(ply, 0);
 			active.put(ply, true);
 		}
 		logger.debug("Starting game {}.",gameNumber);
 		
 		//Blinds are posted.
 		int firstMover = (0 + gameNumber) % players.size();
 		int bigBlind = (firstMover + players.size() - 1) % players.size();
 		int smallBlind = (firstMover + players.size() - 2) % players.size();
 		bets.put(players.get(bigBlind), BIG_BLIND);
 		bets.put(players.get(smallBlind), SMALL_BLIND);
 
 		//Draw hole cards.
 		for(int i = 0; i < players.size(); i++){
 			Player ply = players.get(i);
 			ply.reset();
 			Card hole1 = deck.draw();
 			Card hole2 = deck.draw();
 			logger.trace("Player {} drew hole cards {} and {}", i,hole1, hole2);
 			ply.dealHoleCards(hole1, hole2);
 			holeCards.put(ply,new Card[]{hole1,hole2});
 		}
 
 	}
 
 	private void doBetting(int gameNumber) {
 		logger.trace("Betting round begins; bets stand at {}.",bets);
 		for(int i = 0; i < players.size(); i++){
 			int j = (i + gameNumber) % players.size();
 			Player ply = players.get(j);
 			if(!active.get(ply))continue;
 			Action action = ply.getAction(this);
 			logger.trace("Player {} chose action {}.",j,action);
 			updateState(ply,action);
 		}
 		logger.trace("Betting round ends; bets stand at {}.",bets);
 	}
 
 	private void updateState(Player ply, Action action) {
 		switch(action){
 		case FOLD:
 			active.put(ply,false);
 			lastFolder = ply;
 			break;
 		case CALL:
 			call(ply);
 			break;
 		case BET:
 			bet(ply);
 			break;
 		default:
 			assert false;
 		}
 
 	}
 
 	private void bet(Player ply) {
 		bets.put(ply, Collections.max(bets.values())+1);
 	}
 
 	private void call(Player ply) {
 		bets.put(ply, Collections.max(bets.values()));
 	}
 
 
 	@Override
 	public Round getRound() {
 		return round;
 	}
 
 
 	@Override
 	public int getBets(Player player) {
 		return bets.get(player);
 	}
 
 
 	@Override
 	public int getPot(){
 		int ret = 0;
 		for(Integer i : bets.values()){
 			ret += i;
 		}
 		return ret;
 	}
 
 
 	@Override
 	public List<Player> getPlayers() {
 		return queuedPlayers;
 	}
 
 
 	@Override
 	public Card[] getCommunalCards() {
 		return communalCards.clone();
 	}
 
 	@Override
 	public boolean isPlayerActive(Player player) {
 		return active.get(player);
 	}
 
 	@Override
 	public int getActivePlayerCount() {
 		int ret = 0;
 		for(Boolean bool : active.values()){
 			ret += (bool ? 1 : 0);
 		}
 		return ret;
 	}
 	
 	private class PlayerAndHand implements Comparable<PlayerAndHand>{
 		Player player;
 		Hand hand;
 		public PlayerAndHand(Player player, Hand hand) {
 			this.player = player;
 			this.hand = hand;
 		}
 		@Override
 		public int compareTo(PlayerAndHand o) {
 			if(hand == null || o.hand == null){
 				return -1;
 			}
 			return hand.compareTo(o.hand);
 		}
 		
 	}
 }
