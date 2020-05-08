 /**
  * 
  */
 package bohnanza.standard.core;
 
 import java.util.ArrayList;
 import java.util.Collections;
 
 import bohnanza.standard.core.states.TurnState;
 
 /**
  * @author Damiaan
  * @uml.dependency supplier="main.GameFactory" stereotypes="Standard::Call"
  */
 public class Game {
 
 	/**
 	 * @uml.property name="currentState"
 	 * @uml.associationEnd inverse="context:main.TurnState"
 	 */
 	private TurnState currentState;
 
 	/**
 	 * @uml.property name="players"
 	 * @uml.associationEnd multiplicity="(0 -1)" inverse="game:main.Player"
 	 */
 	private ArrayList<Player> players = new ArrayList<Player>();
 	
 	private int activePlayerIndex = 0;
 	
 	/**
 	 * @uml.property name="drawDeck"
 	 * @uml.associationEnd multiplicity="(0 -1)" inverse="game:main.Card"
 	 */
 	private ArrayList<Card> drawDeck = new ArrayList<Card>();
 
 	/**
 	 * @uml.property name="discardPile"
 	 * @uml.associationEnd multiplicity="(0 -1)" inverse="game:main.Card"
 	 */
 	private ArrayList<Card> discardPile = new ArrayList<Card>();
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		// TODO Auto-generated method stub
 	
 	}
 
 	/**
 		 */
 	public Game() {
 		GameFactory factory = GameFactory.getInstance();
 		drawDeck = factory.getGameDeck();
 		shuffleCards();
 		//GameFactory.getInstance().createGameStates(this);
 	}
 
 	
 	public Player addPlayer(String name) throws IllegalActionException{
 		for(Player player: players){
 			if(player.getName().equals(name))
 				throw new IllegalActionException("Username already taken."); 
 		}
 		Player result = new Player(name);
 		players.add(result);
 		return result;
 	}
 	
 	private void shuffleCards() {
 		Collections.shuffle(drawDeck);
 	}
 
 	/**
 	 * Getter of the property <tt>currentState</tt>
 	 * 
 	 * @return Returns the currentState.
 	 * @uml.property name="currentState"
 	 */
 	public TurnState getCurrentState() {
 		return currentState;
 	}
 
 	/**
 	 * @param currentState the currentState to set
 	 */
 	public void setCurrentState(TurnState currentState) {
 		this.currentState = currentState;
 	}
 
 	/**
 	 * Getter of the property <tt>players</tt>
 	 * 
 	 * @return Returns the players.
 	 * @uml.property name="players"
 	 */
 	public ArrayList<Player> getPlayers() {
 		return players;
 	}
 	
 	public boolean addCardToDiscardPile(Card card){
 		return this.discardPile.add(card);
 	}
 
 	/**
 	 * Draw the top card from the drawdeck
 	 * @require !drawDeck.isEmpty()
 	 * @return the card that was drawn
 	 */
 	public Card drawCard() {
 		//TODO: reshuffle deck if empty
 		Card drawnCard = drawDeck.remove(0);
 		if(drawDeck.isEmpty()) {
 			drawDeck.addAll(discardPile);
 			discardPile.clear();
 			shuffleCards();
 		}
 		return drawnCard;
 	}
 
 	public Player getActivePlayer() {
 		return players.get(activePlayerIndex);
 	}
 	
 	public int goToNextPlayer() {
 		return goToNextPlayer(0);
 	}
 
 	/**
 	 * @param skip
 	 * @requires skip >= 0
 	 */
 	public int goToNextPlayer(final int skip) {
 		if (skip < 0)
 			throw new IllegalArgumentException("skip must be >= 0");
 		
 		return activePlayerIndex = (activePlayerIndex + 1 + skip) % players.size();
 	}
 	
 	public Player getNextPlayer() {
 		return players.get(activePlayerIndex+1);
 	}
 }
