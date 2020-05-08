 package org.tcgframework.resource;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 
 import org.tcgframework.dominion.CellarCard;
 import org.tcgframework.dominion.CopperCard;
 
 import com.google.gson.Gson;
 
 public class DominionGameState implements GameState{
 	
 	public HashMap<String, Card> cardObjSet = new HashMap<String, Card>();
 	
 	public int actions;
 	public int buys;
 	public int money;
 	
 	public int gameID;
 	public int currentPlayer;
 	public int currentPhase;
 	
 	public ArrayList<String> phases = new ArrayList<String>();
 	public ArrayList<String> hand = new ArrayList<String>();
 	public ArrayList<String> inPlay = new ArrayList<String>();
 	public ArrayList<Player> playerObj = new ArrayList<Player>();
 	
 	public DominionGameState(int gameID, HashSet<String> usernames){
 		//put cards into the cardObjSet--------------
 		cardObjSet.put("copper", new CopperCard());
 		cardObjSet.put("cellar", new CellarCard());
 		//-------------------------------------------
 		
 		this.gameID = gameID;
 		
 		//add all the users
 		for (String name : usernames){
 			this.players.add(name);
 			this.playerObj.add(new DominionPlayer(name));
 		}
 		currentPlayer = 0;
 		
 		//add all the phases
 		currentPhase = 0;
 		phases.add("Action Phase");
 		phases.add("Buy Phase");
 		phases.add("Cleanup Phase");
 		actions = 1;
 		buys = 1;
 		money = 0;
 		
 		this.hand = ((DominionPlayer) playerObj.get(currentPlayer)).hand;
 	}
 	
 	public String getCurrentPlayer(){
 		return players.get((currentPlayer % players.size()));
 	}
 	
	public String getNextPlyaer(){
 		return players.get(((currentPlayer + 1) % players.size()));
 	}
 	
 	public void nextPhase(){
 		currentPhase++;
 		currentPhase = currentPhase % phases.size();
 	}
 	
 	public void nextPhase(Long long1) {
 		currentPhase = (int) (long) long1;
 		if (currentPhase == 2) {
 			passTurn();
 		}
 	}
 	
 	public void passTurn(){
 		playerObj.get(currentPlayer).cleanup();
 		currentPhase = 0;
		currentPlayer += 1;
 		actions = 1;
 		buys = 1;
 		money = 0;
 		this.hand = ((DominionPlayer) playerObj.get(currentPlayer)).hand;
 		inPlay.clear();
 	}
 	
 
 	@Override
 	public String toString() {
 		HashMap<String, Object> toReturn = new HashMap<String, Object>();
 		toReturn.put("gameID", this.gameID);
 		toReturn.put("currentPlayer", this.currentPlayer);
 		toReturn.put("currentPhase", this.currentPhase);
 		toReturn.put("phases", this.phases);
 		toReturn.put("players", this.players);
 		toReturn.put("inPlay", this.inPlay);
 		toReturn.put("actions", this.actions);
 		toReturn.put("buys", this.buys);
 		toReturn.put("money", this.money);
 		
 		//TODO: make toString for Cards
 		toReturn.put("hand", this.hand);
 		Gson gson = new Gson();
 		return gson.toJson(toReturn);
 	}
 	
 	public static void main(String[] args){
 		HashSet<String> usernames = new HashSet<String>();
 		usernames.add("jfkdla");
 		usernames.add("fjkdlaja");
 		DominionGameState state = new DominionGameState(0, usernames);
 		System.out.println(state.toString());
 	}
 }
