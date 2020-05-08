 package poker;
 
 /**
  * Poker
  * 
  * The model for Poker. Does all computations for a poker match. GUI is handled in window.java
  * 
  * @author Paul Steele
  *
  */
 public class Poker implements Runnable{
 	public final int PLAYERS; //The number of players in this instance
 	private Player[] players; //array of players in this instance
 	private final int STARTING_CASH = 500; //amount of cash that each player starts with
 	private Deck deck; //the deck of the game
 	private Hand community; //the community cards
 	private int bid; //the highest bid
 	private int pot; //what's in the pot
 	private int dealer;
 	public static final int BIG_BLIND = 10; //the big blind
 	private static Object lock;
 	public static Object dropbox; //used to store values to be passed between threads
 	private Window win; //the window associated with this instance
 	private int round; //number of betting rounds
 	
 	/**
 	 * main()
 	 * 
 	 * creates window and performs the beginning drawing tasks
 	 * 
 	 * @param args
 	 */
 	public static void main(String[] args) {//Actual game runtime
 		Window window = new Window();
 		beginningDrawTasks(window);
 		
 	}
 	/**
 	 * Constructor
 	 * @param playersd The number of players to be in the instance
 	 * 
 	 */
 	public Poker(int playersd) {
 		Poker.lock = this;
 		this.PLAYERS = playersd;
 		//creates the players making the first one human, the rest AI
 		players = new Player[PLAYERS+1];
 		players[1] = new Human(this);
 		for (int i = 2; i < PLAYERS + 1; i++){
 			players[i] = new AI(this);
 		}
 		//initialization
 		for (int i = 1; i < PLAYERS+1;i++){
 			players[i].changeCash(STARTING_CASH);
 			if (i !=1)
 				players[i].setName("AI " + (i -1));
 			else
 				players[i].setName("Human " + i);
 		}
 		//set initial dealer
 		dealer = 1;
 	}
 	
 	/**
 	 * run()
 	 * 
 	 * calls the poker game to be actually run. Can be called multiple times to simulate a real game
 	 * 
 	 */
 	public void run(){
 		try { //enclosing the method in a try allows it to suppress errors if it is interrupted for some reason
 			
 			int temp; //Reusable temp integer variable
 			round = 0; //used for AI thinking
 			//clears community hand and usable deck
 			deck = new Deck();
 			community = new Hand();
 			
 			//shuffles the deck several times
 			for (int i = 0; i < 20; i++){
 				deck.shuffle();
 			}
 			//clear pot and bid
 			pot = 0;
 			bid = 0;
 			
 			//give each player a brand new hand
 			for (int i = 1; i < PLAYERS + 1; i++){
 				if (players[i].inGame) {
 					players[i].setHand(new Hand());
 				}
 				else {
 					players[i].folding = true;
 				}
 			}
 			
 			
 			//deal each player 2 cards and sets current bids to 0
 			for (int i = 1; i < PLAYERS +1; i++){
 				players[i].getHand().add(deck.draw());
 				players[i].getHand().add(deck.draw());
 				players[i].setCurrentBid(0);
 			}
 
 			//Say that the dealer deals
 			
 			players[dealer].speak("deals two cards to each player");
 			Poker.sleep(400);
 			
 			//print players cards to screen
 			win.clearPlayerCards();
 			win.printToPlayerCards(players[1].getHand().getCard(0).toString()+"\n\n");
 			Poker.sleep(750);
 			
 			//hack to make the UI not spazz on resize
 			win.clearPlayerCards();
 			win.printToPlayerCards(players[1].getHand().getCard(0).toString()+"\n");
 			win.printToPlayerCards(players[1].getHand().getCard(1).toString()+"\n");
 
 			//collects the big blind and adds to pot
 			temp = players[getBlinders()[0]].getBlind(true);
 			pot += temp;
 			bid = temp;
 			win.redrawScore();
 			
 			//collects the small blind and adds to pot
 			temp = players[getBlinders()[1]].getBlind(false);
 			pot += temp;
 			win.redrawScore();
 			
 			//round of bidding
 			beginBid();
 			
 			//place 3 cards in community card
 			players[dealer].speak("deals three cards to the flop");
 			win.clearCommunity();
 			Card tempCard;
 			for (int i = 0; i < 3; i++){
 				tempCard = deck.draw();
 				community.add(tempCard);
 				win.printToCommunity(tempCard.toString()+"\n");
 				Poker.sleep(750);
 			}
 			
 			//bidding round 
 			beginBid();
 			
 			//deal 1 card to community
 			players[dealer].speak("deals a card to the turn");
 			tempCard = deck.draw();
 			community.add(tempCard);
 			win.printToCommunity(tempCard.toString()+"\n");
 			Poker.sleep(750);
 			
 			//bidding round
 			beginBid();
 			
 			//deal final card to community
 			players[dealer].speak("deals a card to the river");
 			tempCard = deck.draw();
 			community.add(tempCard);
 			win.printToCommunity(tempCard.toString()+"\n");
 			Poker.sleep(750);
 			
 			//checks who wins the round
 			int winning = checkRoundWinner();
 			
 			//print all players hands with a delay
 			for (int i = 1; i < PLAYERS + 1; i++) {
 				if (!players[i].isFolding())
 					players[i].speak("Plays has a " + players[i].getScoreName());
 				Poker.sleep(550);
 			}
 			
 			//gives the winner the money in the pot and resets the pot
 			players[winning].changeCash(pot);
 			pot = 0;
 			win.redrawScore();
 			
 			//say who won
 			players[winning].speak("won the round");
 			
 			//kick out players who don't have any money
 			for (int i = 1; i < PLAYERS +1; i++){
 				if (players[i].cash <= 0){
 					players[i].inGame = false;
 				}
 			}
 			
 			//advance dealer
 			while (true) {
 				dealer += 1;
				if (dealer == PLAYERS){
 					dealer = 1;
 				}
 				
 				if (players[dealer].inGame)
 					break;
 			}
 			
 			//begins a new round if no winner, otherwise says who the winner is
 			if (!checkGameWinner()){
 				Poker.sleep(2000);
 				win.clearCommunity();
 				win.clearPlayerCards();
 				run();
 			}
 			else {
 				players[getGameWinner()].speak("won the game!");
 			}
 			
 		}
 		catch (InterruptedException e){
 			//simply end the run if an interruption occurs
 		}
 		
 	}
 	
 	private int checkRoundWinner() {
 		int winning = -1;
 		int winningScore = 0;
 		int lead = -1;
 		for (int i = 1; i < PLAYERS + 1; i++){
 			if ( (( players[i].currentScore() > winningScore ) && !players[i].isFolding()) || 
 					((players[i].currentScore() == winningScore) && ((players[i].getLead() > lead) || players[i].getLead() == 0) && !players[i].isFolding())) {
 				winningScore = players[i].currentScore();
 				winning = i;
 				lead = players[i].getLead();
 			}
 		}
 		return winning;
 	}
 	
 	private boolean checkGameWinner() {
 		int numberStillIn = 0;
 		for (int i = 1; i < PLAYERS +1; i++){
 			if (players[i].inGame){
 				numberStillIn++;
 			}
 		}
 		
 		return (1 == numberStillIn);
 	}
 	
 	private int getGameWinner() {
 		int whoin = 0;
 		for (int i = 1; i < PLAYERS +1; i++){
 			if (players[i].inGame){
 				whoin = i;
 			}
 		}
 		
 		return whoin;
 	}
 	
 	public Player getPlayer(int play){
 		return players[play];
 	}
 	
 	public static void sleep(int i) throws InterruptedException{
 				Thread.sleep(i);
 	}
 	
 	private int[] getBlinders(){
 		int[] ret = new int[2];
 		
 		int big = dealer;
 		while(true){
 			
 			big -= 1;
 			if (big <= 0)
 				big = PLAYERS;
 			if (players[big].inGame)
 				break;
 		}
 		
 		int small = dealer;
 		while (true){
 			small += 1;
 			
			if (dealer >= PLAYERS)
 			small = 1;
 		
 			if (players[small].inGame)
 				break;
 		}
 		
 		ret[0] = big;
 		ret[1] = small;
 		return ret;
 	}
 	
 	private void beginBid() throws InterruptedException {
 		boolean done = false;
 		int temp;
 		boolean oneround = false;
 		
 		while (!done){
 			//gets players' bids
 			for (int i = 1; i < PLAYERS +1; i++){
 				if ((!oneround || !players[i].meetingBid(bid)) && !players[i].folding) {
 					temp = players[i].getBid(bid);
 					pot += temp;
 					win.redrawScore();
 					if (players[i].getCurrentBid() > bid){
 						bid = players[i].getCurrentBid() ;
 					}
 				}
 			}		
 			done = true;
 			//checks to see if all players have called
 			for (int i = 1; i < PLAYERS + 1; i++){
 				if ((players[i].meetingBid(bid) == false) && !players[i].folding)
 					done = false;
 			}
 			oneround = true;
 			
 		}
 		bid = 0;
 		round++;
 		for (int i = 1; i < PLAYERS +1; i++ ) {
 			players[i].setCurrentBid(0);
 		}
 	}
 	
 	public int getPot() {
 		return pot;
 	}
 	
 	/**
 	 * passes the game window to various parts of the program
 	 * @param window
 	 */
 	public void passWindow(Window window){
 		win = window;
 		for (int i = 1; i < PLAYERS + 1; i++){
 			players[i].setWindow(window);
 		}
 	}
 	public static Object getLock() {
 		return lock;
 	}
 	public static void setLock(Object lock) {
 		Poker.lock = lock;
 	}
 	
 	private static void beginningDrawTasks(Window window){
 		window.redrawScore();
 		window.clearCommunity();
 		window.clearPlayerCards();
 		window.printToPlayerCards("\n\n"); //useful for not spazzing out resizing
 	}
 	
 	public Hand getCommunity(){
 		return community;
 	}
 	
 	public int getRound(){
 		return round;
 	}
 }
