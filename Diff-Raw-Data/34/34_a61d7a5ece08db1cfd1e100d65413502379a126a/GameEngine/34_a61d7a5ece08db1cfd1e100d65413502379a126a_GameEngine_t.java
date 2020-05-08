 package gameEngine;
 
 import game.Blackjack;
 import game.Observer;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.InputMismatchException;
 import java.util.Scanner;
 
 import participant.Chips;
 import participant.Player;
 import table.Table;
 import cards.BlackjackHand;
 import cards.Deck;
 
 public class GameEngine {
 	private Blackjack myGame;
 
 	private Table gameTable;
 
 	private ArrayList<Observer> observerList = new ArrayList<Observer>();
 
 	private BlackjackHand dealerHand = new BlackjackHand();
 
 	private HashMap<Player, BlackjackHand> playersAndHands;
 
 	private HashMap<Player, Chips> playersAndChips;
 
 	private final int DEALER_WON = 1;
 
 	private final int PLAYER_WON = -1;
 
 	private final int DRAW = 0;
 
 	private final int WIN_CONSTANT = 2;
 
 	private final int BLACKJACK_PAYOUT_CONSTANT = 3;
 
 	
 	public GameEngine(Player player) {
 
 		gameTable = new Table(player);
 
 	}
 	/* Currently, storage/blackjackStorage can only save and load one hand for each player
 	 * so players.size() must equal to hand.size() */
 
 	public GameEngine(Deck deck){
 		myGame=new Blackjack();
 		myGame.setDeck(deck);
 	}
 	
 	public void reset() {
 		myGame = new Blackjack();
 		playersAndHands = new HashMap<Player, BlackjackHand>();
 		playersAndChips = new HashMap<Player, Chips>();
 		dealerHand = new BlackjackHand();
 //		this.notifyObservers();
 	}
 	
 	public void resetKeepPlayers() {
 		myGame = new Blackjack();
 //		this.notifyObservers();
 	}
 	
 	public void loadPlayers() {
 
 		for ( int i = 0 ; i < gameTable.getAllPlayers().size() ; i++ ) {
 			Player tmpPlayer = gameTable.getAllPlayers().get(i);
 			if (!playersAndHands.containsKey(tmpPlayer)) {
 				playersAndHands.put(tmpPlayer, new BlackjackHand());
 			} 
 		}
 	}
 	
 	public void loadChips() {
 		for ( int i = 0 ; i < gameTable.getAllPlayers().size() ; i++ ) {
 			Player tmpPlayer = gameTable.getAllPlayers().get(i);
 			if (!playersAndChips.containsKey(tmpPlayer)) {
 				playersAndChips.put(tmpPlayer, new Chips(tmpPlayer.getChips()));
 			} 
 		}
 	}
 	
 //	public ArrayList<Player> getAllPlayers() {
 //		return this.players;
 //	}
 	
 	private void setAllBets(Player player) {
 		boolean invalid = false;
		System.out.println(player.getUsername() + ", you currently have " + player.getChips() + " chips.");
 		System.out.println("How much do you want to bet?");
 		int betValue = 0;
 		Scanner keyboard = new Scanner(System.in);
 		
 		while (invalid == false) {
 			try { 
 				betValue = keyboard.nextInt();
 				playersAndChips.get(player).setBet(betValue);
 				playersAndChips.get(player).addChips(player, -betValue);
 
				System.out.println(player.getUsername() + ", you want to bet " + betValue);
 				invalid = true;
 			} catch (IllegalArgumentException e) {
 				System.out.println("Please enter a valid bet amount. Bets must be multiples of 10, between 10 and 100");
 			} catch (InputMismatchException IE) {
                 System.out.println("This is not a valid integer!");
                 System.out.println("You currently have " + player.getChips() + " chips.");
                 System.out.println("Please try again.");
                 keyboard.nextLine();
 			}
 		}
 			
 		
 		//return betValue;
 	}
 	
 	private void playThroughEachPlayer(Player player, BlackjackHand hand) {
 		Scanner keyboard = new Scanner(System.in);
		
 		while (hand.isPlayable() && !hand.isBust()){
			System.out.println(player.getUsername() + ", your current hand is: " + hand.toString());
			System.out.println("Do you want to hit, stand, doubledown or split?");
 			String s = keyboard.next();
 
 			if (s.equals("hit")){
 				myGame.hit(hand);
				System.out.println("hand update: " + hand.toString());
 			}
 			else if (s.equals("stand")){
 				myGame.stand(hand);
				System.out.println("final hand: "+ hand.toString());
				break;
 			}
 			else if (s.equals("doubledown")){
 				if (player.getChips() < 2*playersAndChips.get(player).getBet()) {
 					System.out.print("Sorry not enough chips, please enter a different move" );
 					s = "";
 				}
 				else { 
 					if(hand.getNumberCards() > 2){
 						System.out.println("Cannot double down, you have already hit, please hit again or stand");
 					}else{
 					playersAndChips.get(player).addChips(player, -(playersAndChips.get(player).getBet()));
 				    int doubleBet =((playersAndChips.get(player)).getBet())*2;
 				    playersAndChips.get(player).setBet(doubleBet);
 					myGame.doubleDown(hand);
 					System.out.println("bet update: "+ doubleBet);
 					System.out.println("hand update: "+hand.toString());
 					}
 				}
 
 			}
 			else if (s.equals("split")){
 				ArrayList<BlackjackHand> bothHands = myGame.split(hand);
 
 		        if(bothHands.size() != 0){
 		         myGame.playsSplitHands(bothHands);
 		         //System.out.println("split to be implemented: "+b.get(j).toString());
 		        } else {
 		        System.out.println("please enter something valid");
 		        s = "";
 		       }
 		      } else {
 				System.out.println("please enter something valid");
 				s = "";
 			}
 		}
 	}
 	
 	private void showDealerFinalHand() {
 		//shows dealer's final hand
 		System.out.println("Dealer's new hand: " + dealerHand.toString());	
 		if (dealerHand.isBust()) 
 				System.out.println("Dealer bust!");
 	}
 	
 	private int processWinner(BlackjackHand hand) {
 
 		if ((hand.isBust()) && !(dealerHand.isBust()))
 
 			return DEALER_WON;
 
 		if (!(hand.isBust()) && !(dealerHand.isBust()) && (dealerHand.getBlackjackValue() > hand.getBlackjackValue()))
 
 			return DEALER_WON;
 		
 		if ((hand.isBust()) && (dealerHand.isBust()))
 
 			return DEALER_WON;
 
 		if (!(hand.isBust()) && (dealerHand.isBust()))
 
 			return PLAYER_WON;
 
 		if (!(hand.isBust()) && !(dealerHand.isBust()) && dealerHand.getBlackjackValue() < hand.getBlackjackValue()) {
 
 			return PLAYER_WON;
 			
 		}
 
 			return DRAW;
 
 	}
 	
 	private void computeWinner(Player player) {
 
 			BlackjackHand hand = playersAndHands.get(player);
 
 				if (processWinner(hand) == 1) {
 					
 					player.addChips(playersAndChips.get(player).getBet());
 					player.addLoss(playersAndChips.get(player).getBet());
 					System.out.println("Dealer wins!");
 					
 				}
 				if (processWinner(hand) == -1) {
 					
 					if (hand.checkBlackjack()) {
 						
 						player.addWin(BLACKJACK_PAYOUT_CONSTANT * playersAndChips.get(player).getBet());
 						System.out.println("BLACKJACK! X2 PAYOUT! " + player.getUsername()+ " wins!");
 						
 					} else {
 						
 						player.addWin(WIN_CONSTANT * playersAndChips.get(player).getBet());
 						System.out.println(player.getUsername()+ " wins!");
 					}
 					
 				if (processWinner(hand) == 0) {
 					playersAndChips.get(player).addChips(player, playersAndChips.get(player).getBet());
 					System.out.println("Draw!");
 				}
 
 		}
 	}
 	
 	public void gameStart() {
 		reset();
 		myGame.deal(dealerHand); 
 		loadPlayers();
 		loadChips();
 
//		Player player;
//		Scanner keyboard = new Scanner(System.in);
 		ArrayList<Player> players = gameTable.getAllPlayers();
 		
 		for (int i=0; i<players.size(); i++) {
 			this.setAllBets(players.get(i));
 		}
 		
 		System.out.println("Dealing everyone...");
		
 		for (int i=0; i<players.size(); i++) {
 			System.out.println(players.get(i).getUsername());
 		}
		this.dealEveryonesHand(players);
 		
		System.out.println("Dealer's current hand : " + dealerHand);
 		for (int i=0; i<players.size(); i++) {
 			this.dealPlayers(players.get(i));
 		}
 		
 		for (int i=0; i<players.size(); i++) {
 			BlackjackHand hand = playersAndHands.get(players.get(i));
			//System.out.println("hand= " + hand.toString());
 			this.playThroughEachPlayer(players.get(i), hand);
 		}
 		
 		this.showDealerFinalHand();
 		
 		for (int i=0;i<players.size();i++){ 
 			this.computeWinner(players.get(i));
 		}
 	}
 	
 	private void dealEveryonesHand(ArrayList<Player> players) {
 		System.out.println("Dealing everyone...");
 		for (int i=0; i<players.size(); i++){
 			System.out.println(players.get(i).getUsername());
 		}
 
 //		for (BlackjackHand hand : playersAndHands.values()) {
 //			//BlackjackHand tmpHand = new BlackjackHand();
 //			BlackjackHand tmpHand = myGame.dealHand(new BlackjackHand());
 //			//hand.add(tmpHand);
 //		}
 
 		for (int i=0; i < players.size(); i++){
 			BlackjackHand pHand = new BlackjackHand();
 			playersAndHands.put(players.get(i), myGame.dealHand(pHand));
 			BlackjackHand hand = playersAndHands.get(players.get(i));
 			//BlackjackHand tmpHand = playersAndHands.get(players.get(i));
 			System.out.println(players.get(i).getUsername() + ", your hand is currently: " + hand.toString());
 		}
 	}
 	
 	private void dealPlayers(Player player) {
 
 		for (BlackjackHand hand : playersAndHands.values()) {
 			//BlackjackHand tmpHand = new BlackjackHand();
 			myGame.deal(new BlackjackHand());
 			playersAndHands.put(player, hand);
 			//hand.add(tmpHand);
 		}
 	}
 	
 
 	/**
 	 * Returns the current game's Table object
 	 * @return Table object
 	 */
 	public Table getTable() {
 		return gameTable;
 	}
 	
 	/**
 	 * Returns the current game's Deck object
 	 * @return Deck object
 	 */
 	public Deck getDeck() {
 		return myGame.getDeck();
 	}
 
 	public void addPlayer(Player player) {
 		BlackjackHand hb = new BlackjackHand();
 		ArrayList<BlackjackHand> hbb = new ArrayList<BlackjackHand>();
 		hbb.add(hb);
 		gameTable.requestJoin(player);
 	}
 }
