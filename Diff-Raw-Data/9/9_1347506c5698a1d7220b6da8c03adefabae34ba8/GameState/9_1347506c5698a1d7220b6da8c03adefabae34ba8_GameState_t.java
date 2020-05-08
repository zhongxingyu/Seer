 package org.ojim.logic.state;
 
 import org.ojim.logic.accounting.Bank;
 import org.ojim.iface.Rules;
 
 public class GameState {
 
 	private final int FIELDS_AMOUNT = 40;
 	private Player[] players;
 	private Bank bank;
 	private Field[] fields;
 	private Rules rules;
 	private DiceSet dices;
 	private Player activePlayer;
 	private int bankHouses;
 	private int bankHotels;
 	
 	
 	public GameState(int maxPlayerCount) {
 		this.players = new Player[maxPlayerCount];
 		this.fields = new Field[FIELDS_AMOUNT];
 		this.bank = new Bank();
 		this.rules = new Rules();//30000, 2000, true, true, false, true);
 		this.dices = new OjimDiceSet(1337);
 		
 		//TODO Add bankHouses / bankHotels somewhere in the Rules
 		this.bankHouses = 40;
 		this.bankHotels = 20;
 	}
 	
 	public int getBankHouses() {
 		return this.bankHouses;
 	}
 	
 	public int getBankHotels() {
 		return this.bankHotels;
 	}
 	
 	public boolean changeBankHouses(int changeAmount) {
 		if(this.bankHouses - changeAmount < 0) {
 			return false;
 		}
 		this.bankHouses += changeAmount;
 		return true;
 	}
 	
 	public boolean changeBankHotels(int changeAmount) {
 		if(this.bankHotels - changeAmount < 0) {
 			return false;
 		}
 		this.bankHotels += changeAmount;
 		return true;
 	}
 	
	public DiceSet getDices() {
 		return this.dices;
 	}
 	
 	public Rules getRules() {
 		return rules;
 	}
 	
 	public void setPlayer(int id, Player player) {
 		players[id] = player;
 	}
 	
 	public Player getPlayerByID(int playerID) {
 		if(playerID >= players.length) {
 			return null;
 		}
 		return players[playerID];
 	}
 	
 	public Field getFieldByID(int fieldID) {
 		if(fieldID >= FIELDS_AMOUNT) {
 			return null;
 		}
 		return fields[fieldID];
 	}
 	
 	public Player[] getPlayers() {
 		return this.players.clone();
 	}
 	
 	public Bank getBank() {
 		return this.bank;
 	}
 	
 	//TODO: Add possibility to return the number of fields.
 	public int getNumberOfFields() {
 		return FIELDS_AMOUNT;
 	}
 	
 	//TODO: Does this class support this? If so: finish!s
 	/* xZise: Implement this as buffer for some actions, can be modified if incorrect!*/
 	public Player getActivePlayer() {
 		return null;
 	}
 }
