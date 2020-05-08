 package se.chalmers.dryleafsoftware.androidrally.model.robots;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import se.chalmers.dryleafsoftware.androidrally.model.cards.Card;
 import se.chalmers.dryleafsoftware.androidrally.model.cards.TurnType;
 import se.chalmers.dryleafsoftware.androidrally.model.gameBoard.GameBoard;
 
 /**
  * The "piece" that the player or CPU moves and plays the game with.
  */
 public class Robot {
 	private static final int STARTING_HEALTH = 9;
 	private static final int STARTING_LIFE = 3;
 	
 	private int positionX;
 	private int positionY;
 	private int robotDirection = GameBoard.NORTH;
 	private List<Card> cards;
 	private Card[] chosenCards;
 	private int damage = 0;
 	private int life = STARTING_LIFE;
 	private int spawnPointX;
 	private int spawnPointY;
 	private int checkpoint;
 	private boolean sentCards;
 	
 	public Robot(int startX, int startY) {
 		positionX = startX;
 		positionY = startY;
 		newSpawnPoint();
 		cards = new ArrayList<Card>();
 		chosenCards = new Card[5];
 		checkpoint = 0;
 	}
 	
 	/**
 	 * Move the robot a number of steps in the given direction.
 	 * @param distance The number of step the robot will take
 	 * @param direction use int constants in Robot class
 	 */
 	public void move(int distance, int direction){
 		if(direction == GameBoard.NORTH){
 			this.positionY -= distance;
 		}else if(direction == GameBoard.EAST){
 			this.positionX += distance;
 		}else if(direction == GameBoard.SOUTH){
 			this.positionY += distance;
 		}else if(direction == GameBoard.WEST){
 			this.positionX -= distance;
 		}
 	}
 	
 	/**
 	 * Make the robot turn according to the given TurnType.
 	 * @param turn Given a TurnType the robot will turn that way
 	 * @see TurnType
 	 */
 	public void turn(TurnType turn){
 		if (turn == TurnType.LEFT){
 			robotDirection += 3;
 		} else if(turn == TurnType.RIGHT){
 			robotDirection += 1;
 		} else if(turn == TurnType.UTURN){
 			robotDirection += 2;
 		}
 		robotDirection %= 4;
 	}
 	
 	/**
 	 * The cards given from the Deck is put to the robot with this method.
 	 * @param cards the cards from the Deck
 	 * @see Deck
 	 */
 	public void addCards(List<Card> cards){
 		this.cards = cards;
 	}
 	
 	/**
 	 * The cards that isn't locked to a register will be sent back to the deck.
 	 */
 	public List<Card> returnCards(){
 		List<Card> returnCards= new ArrayList<Card>();
 		
 		returnCards.addAll(cards);
 		for(Card card : chosenCards){
 			returnCards.remove(card);
 		}
 		for(int i = 0; i < damage - 4; i++){
 			returnCards.remove(chosenCards[4-i]);
 			chosenCards[i] = null;
 		}
 		cards.clear();
 		setSentCards(false);
 		return returnCards;
 	}
 	
 	/**
 	 * Return how much health the robot have.
 	 * @return Starting health - damage
 	 */
 	public int getHealth(){
 		return STARTING_HEALTH - damage;
 	}
 	
 	/**
 	 * Increase the damage with the value of the parameter.
 	 * <p>
 	 * If the damage rate is higher than the starting health
 	 * the method die() is called. 
 	 * @param damage the amount of damage
 	 */
 	public void damage(int damage){
 		this.damage += damage;
 		if (damage > STARTING_HEALTH) {
 			die();
 		}
 	}
 	
 	/**
 	 * Called when damage is higher than starting health.
 	 * Decreases life with 1 and sets the robot's position
 	 * to the last visited spawnpoint.
 	 */
 	public void die(){
 		life--;
 		positionX = spawnPointX;
 		positionY = spawnPointY;
 	}
 	
 	/**
 	 * Updates the spawnpoint that the robot will get if it dies
 	 * (die() is called).
 	 */
 	public void newSpawnPoint(){
 		spawnPointX = positionX;
 		spawnPointY = positionY;
 	}
 	
 	/**
 	 * If the next checkpoint is reached the "next checkpoint" value
 	 * will be increased with 1. Damage will decrease with 1 and
 	 * newSpawnPoint() will be called so that the robot spawns at the
 	 * specific checkpoint if it dies (die() is called).
 	 * @param checkpoint
 	 */
 	public void reachCheckPoint(int checkpoint){
 		if(checkpoint == this.checkpoint + 1){
 			this.checkpoint++;
 			newSpawnPoint();
 			damage--;
 		}
 	}
 	
 	public int getLastCheckPoint(){
 		return checkpoint;
 	}
 	
 	public int getX(){
 		return positionX;
 	}
 	
 	public int getY(){
 		return positionY;
 	}
 	
 	public String getXAsString(){
		if(positionX > 10){
 			return positionX + "";
 		}else{
 			return "0" + positionX;
 		}
 	}
 	
 	public String getYAsString(){
		if(positionY > 10){
 			return positionY + "";
 		}else{
 			return "0" + positionY;
 		}
 	}
 
 	public void setX(int x){
 		positionX = x;
 	}
 	public void setY(int y){
 		positionY = y;
 	}
 	
 	public int getDirection() {
 		return robotDirection;
 	}
 
 	public int getLife() {
 		return life;
 	}
 
 	public Card[] getChosenCards() {
 		fillEmptyCardRegisters();
 		return chosenCards;
 	}
 	
 	/**
 	 * Sets which of the drawn cards that is supposed to be the chosen cards.
 	 * Fills the empty registers with cards from the drawn cards if the amount of cards
 	 * is not enough. If the chosen cards is not part of the drawn cards all registers
 	 * will recieve randomized cards from the drawn cards.
 	 * @param chosenCards the cards from the robots card list that the player/CPU has chosen
 	 */
 	public void setChosenCards(List<Card> chosenCards){
 		if(this.cards.containsAll(chosenCards)){
 			for(int i = 0; i<5; i++){
 				if(this.chosenCards[i] == null){
 					this.chosenCards[i] = chosenCards.get(i);
 				}
 			}
 		}		
 	}
 	
 	private void fillEmptyCardRegisters(){
 		Random random = new Random();
 		List<Card> tempCards = new ArrayList<Card>();
 		tempCards.addAll(cards);
 		for(int i = 0; i<5; i++){
 			if(this.chosenCards[i] == null){
 				this.chosenCards[i] = tempCards.remove(random.nextInt(tempCards.size()));
 			}
 		}
 	}
 	
 	public List<Card> getCards(){
 		return cards;
 	}
 
 	/**
 	 * Method that is used to know if the Robot has chosen his cards or not.
 	 * @return false as default. Else the last value set from method setSentCards
 	 */
 	public boolean haveSentCards() {
 		return sentCards;
 	}
 
 	public void setSentCards(boolean sentCards) {
 		this.sentCards = sentCards;
 	}
 }
