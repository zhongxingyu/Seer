 package risk.game;
 
 import java.util.Random;
 
 import risk.ai.Agent;
 import risk.gui.Gui;
 
 public class Engine {
 	
 	private Continent northAmerica;
 	private Continent southAmerica;
 	private Continent europe;
 	private Continent africa;
 	private Continent asia;
 	private Continent australia;
 	private Gui gui;
 	private Player[] playerList;
 	private RiscCard[] riskCards;
 	public int playerCount;
 	
 	
 	public Engine(int waitingTime) {
 		gui = new Gui();
 		initCountries();
 		initRiskCards();
 		this.playerCount = waitingTime;
 	}
 
 	public void run() {
 		initPlayers();
 		int currentPlayerIndex = 0;
 		int counter = 0;
 		while (!checkCountries()) {
 			Player currentPlayer = playerList[currentPlayerIndex];			
 			currentPlayer.newArmies(checkContinentsForSingleOwner(currentPlayer));
 			currentPlayer.attack();
 			currentPlayer.repositionArmies();
 			currentPlayerIndex++;
 			if (currentPlayerIndex >= playerList.length) {
 				currentPlayerIndex = 0;
 			}
 		}
 		System.out.println("End of Game.");
 	}
 	
 	private int checkContinentsForSingleOwner(Player p) {
 		int armies = 0;
 		if (p == this.northAmerica.checkIfControledByOnePlayer()) {
 			armies += this.northAmerica.getBonusArmies();
 		}
 		if (p == this.southAmerica.checkIfControledByOnePlayer()) {
 			armies += this.southAmerica.getBonusArmies();
 		}
 		if (p == this.europe.checkIfControledByOnePlayer()) {
 			armies += this.europe.getBonusArmies();
 		}
 		if (p == this.africa.checkIfControledByOnePlayer()) {
 			armies += this.africa.getBonusArmies();
 		}
 		if (p == this.asia.checkIfControledByOnePlayer()) {
 			armies += this.asia.getBonusArmies();
 		}
 		if (p == this.australia.checkIfControledByOnePlayer()) {
 			armies += this.australia.getBonusArmies();
 		}
 		return armies;
 	}
 
 	
 	
 /*	public void insDice(){
 		Dice verteidigung1 = new Dice(2);
 		Dice verteidigung2 = new Dice(2);
 
 		Dice Angriff1 = new Dice(1);
 		Dice Angriff2 = new Dice(1);
 		Dice Angriff3 = new Dice(1);
 		
 	}
 	*/
 	
 	private boolean checkCountries()
 	{
 		for (Player p:playerList) {
 			if (p == this.northAmerica.checkIfControledByOnePlayer()
 					&& p == this.southAmerica.checkIfControledByOnePlayer()
 					&& p == this.europe.checkIfControledByOnePlayer()
 					&& p == this.africa.checkIfControledByOnePlayer()
 					&& p == this.asia.checkIfControledByOnePlayer()
 					&& p == this.australia.checkIfControledByOnePlayer()) {
 				return true;
 			}				
 		} 	
 		return false;
 	}
 	
 	private boolean gameFinished() {
 		for (Player p:playerList) {
			if (p.controlledCountries.size() >= 41) {
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	private void initPlayers() {
 		playerList = new Player[this.playerCount];
 		/*
 		 * Setting the colors for each player
 		 * 
 		 * NOTE: In this version all players are created as Agents
 		 */
 		for (int i = 0; i < playerList.length; i++) {
 			playerList[i] = new Agent(PlayerColor.values()[i]);
 		}
 		
 		/*
 		 * Distributing all countries to the players equally. Distribution is always the same.
 		 */
 		int i = 0;
 		for (int j = 0; j < northAmerica.getCountries().length; j++) {
 			Player tmp = playerList[i % playerList.length];
 			tmp.addCountry(northAmerica.getCountry(j));
 			northAmerica.getCountry(j).setPlayer(tmp);
 			northAmerica.getCountry(j).updateColor();
 			i++;
 		}
 		for (int j = 0; j < southAmerica.getCountries().length; j++) {
 			Player tmp = playerList[i % playerList.length];
 			tmp.addCountry(southAmerica.getCountry(j));
 			southAmerica.getCountry(j).setPlayer(tmp);
 			southAmerica.getCountry(j).updateColor();
 			i++;
 		}
 		for (int j = 0; j < europe.getCountries().length; j++) {
 			Player tmp = playerList[i % playerList.length];
 			tmp.addCountry(europe.getCountry(j));
 			europe.getCountry(j).setPlayer(tmp);
 			europe.getCountry(j).updateColor();
 			i++;
 		}
 		for (int j = 0; j < africa.getCountries().length; j++) {
 			Player tmp = playerList[i % playerList.length];
 			tmp.addCountry(africa.getCountry(j));
 			africa.getCountry(j).setPlayer(tmp);
 			africa.getCountry(j).updateColor();
 			i++;
 		}
 		for (int j = 0; j < asia.getCountries().length; j++) {
 			Player tmp = playerList[i % playerList.length];
 			tmp.addCountry(asia.getCountry(j));
 			asia.getCountry(j).setPlayer(tmp);
 			asia.getCountry(j).updateColor();
 			i++;
 		}
 		for (int j = 0; j < australia.getCountries().length; j++) {
 			Player tmp = playerList[i % playerList.length];
 			tmp.addCountry(australia.getCountry(j));
 			australia.getCountry(j).setPlayer(tmp);
 			australia.getCountry(j).updateColor();
 			i++;
 		}
 	}
 
 	private void initCountries() {
 		
 		/* North America */
 		northAmerica = new Continent("North America", 5, 9);
 		northAmerica.setCountry(new Country(true, 3, "Alaska", northAmerica, 0, 80, 120, 60, "nAmerika_1", 0), 0);
 		northAmerica.setCountry(new Country(false, 4, "Alberta", northAmerica, 80, 90, 200, 100, "nAmerika_2", 1), 1);
 		northAmerica.setCountry(new Country(true, 3, "Central America", northAmerica, 58, 160, 200, 200, "nAmerika_3", 2), 2);
 		northAmerica.setCountry(new Country(false, 4, "Eastern United States", northAmerica, 102, 100, 200, 200, "nAmerika_4", 3), 3);
 		northAmerica.setCountry(new Country(true, 4, "Greenland", northAmerica, 260, 0, 200, 200, "nAmerika_5", 4), 4);
 		northAmerica.setCountry(new Country(false, 4, "Northwest Territory", northAmerica, 15, 0, 300, 200, "nAmerika_6", 5), 5);
 		northAmerica.setCountry(new Country(false, 6, "Ontario", northAmerica, 135, 55, 200, 200, "nAmerika_7", 6), 6);
 		northAmerica.setCountry(new Country(false, 3, "Quebec", northAmerica, 198, 48, 200, 200, "nAmerika_8", 7), 7);
 		northAmerica.setCountry(new Country(false, 4, "Western United States", northAmerica, 66, 90, 200, 200, "nAmerika_9", 8), 8);
 
 		for (int i = 0; i < northAmerica.getCountries().length; i++) {
 			gui.getPnlGame().add(northAmerica.getCountry(i).getLabel());
 		}
 		
 		/* South America */
 		southAmerica = new Continent("South America", 2, 4);
 		southAmerica.setCountry(new Country(false, 2, "Argentina", southAmerica, 130, 380, 200, 200, "sAmerika_1", 0), 0);
 		southAmerica.setCountry(new Country(true, 4, "Brazil", southAmerica, 122, 304, 300, 200, "sAmerika_2", 1), 1);
 		southAmerica.setCountry(new Country(false, 3, "Peru", southAmerica, 147, 346, 200, 100, "sAmerika_3", 2), 2);
 		southAmerica.setCountry(new Country(true, 3, "Venezuela", southAmerica, 158, 238, 200, 200, "sAmerika_4", 3), 3);
 		
 		for (int i = 0; i < southAmerica.getCountries().length; i++) {
 			gui.getPnlGame().add(southAmerica.getCountry(i).getLabel());
 		}
 		
 		/* Europe */
 		europe = new Continent("Europe", 5, 7);
 		europe.setCountry(new Country(false, 4, "Great Britain", europe, 370, 70, 200, 200, "europa_1", 0), 0);
 		europe.setCountry(new Country(true, 3, "Iceland", europe, 350, 70, 200, 100, "europa_2", 1), 1);
 		europe.setCountry(new Country(false, 5, "Northern Europe", europe, 418, 87, 300, 200, "europa_3", 2), 2);
 		europe.setCountry(new Country(false, 4, "Scandinavia", europe, 430, 45, 200, 200, "europa_4", 3), 3);
 		europe.setCountry(new Country(true, 6, "Southern Europe", europe, 435, 116, 200, 200, "europa_5", 4), 4);
 		europe.setCountry(new Country(true, 6, "Ukraine", europe, 470, 30, 300, 300, "europa_6", 5), 5);
 		europe.setCountry(new Country(true, 4, "Western Europe", europe, 380, 110, 200, 200, "europa_7", 6), 6);
 		
 		for (int i = 0; i < europe.getCountries().length; i++) {
 			gui.getPnlGame().add(europe.getCountry(i).getLabel());
 		}
 		
 		/* Africa */
 		africa = new Continent("Africa", 3, 6);
 		africa.setCountry(new Country(false, 3, "Congo", africa,455, 332, 200, 100,"afrika_1", 0), 0);
 		africa.setCountry(new Country(true, 6, "East Africa", africa,480, 270, 200, 200,"afrika_2", 1), 1);
 		africa.setCountry(new Country(true, 4, "Egypt", africa,457, 190, 200, 200,"afrika_3", 2), 2);
 		africa.setCountry(new Country(false, 2, "Madagascar", africa,560, 320, 300, 200,"afrika_4", 3), 3);
 		africa.setCountry(new Country(true, 6, "North Africa", africa,320, 210, 200, 200,"afrika_5", 4), 4);
 		africa.setCountry(new Country(false, 3, "South Africa", africa,450, 291, 300, 300,"afrika_6", 5), 5);
 		
 		for (int i = 0; i < africa.getCountries().length; i++) {
 			gui.getPnlGame().add(africa.getCountry(i).getLabel());
 		}
 		
 		/* Asia */
 		asia = new Continent("Asia", 7, 12);
 		asia.setCountry(new Country(true, 5, "Afghanistan", asia, 555, 107, 200, 200,"asien_1", 0), 0);
 		asia.setCountry(new Country(false, 6, "China", asia,633, 148, 200, 200,"asien_2", 1), 1);
 		asia.setCountry(new Country(false, 4, "India", asia,603, 190, 200, 200,"asien_3", 2), 2);
 		asia.setCountry(new Country(false, 4, "Irkutsk", asia,660, 128, 200, 100,"asien_4", 3), 3);
 		asia.setCountry(new Country(false, 2, "Japan", asia,790, 100, 300, 300,"asien_5", 4), 4);
 		asia.setCountry(new Country(true, 5, "Kamchatka", asia,760, 80, 300, 200,"asien_6", 5), 5);
 		asia.setCountry(new Country(true, 6, "Middle East", asia, 497, 177, 200, 200, "asien_7", 6), 6);
 		asia.setCountry(new Country(false, 5, "Mongolia", asia,655, 120, 200, 200,"asien_8", 7), 7);
 		asia.setCountry(new Country(true, 3, "Siam", asia,707, 220, 200, 200,"asien_8", 8), 8);
 		asia.setCountry(new Country(false, 5, "Siberia", asia,583, 105, 200, 100,"asien_10", 9), 9);
 		asia.setCountry(new Country(true, 4, "Ural", asia,550, 15, 300, 300,"asien_11", 10), 10);
 		asia.setCountry(new Country(false, 3, "Yakutsk", asia, 670, 50, 300, 200, "asien_12", 11), 11);
 		
 		for (int i = 0; i < asia.getCountries().length; i++) {
 			gui.getPnlGame().add(asia.getCountry(i).getLabel());
 		}
 		
 		/* Australia */
 		australia = new Continent("Australia", 2, 4);
 		australia.setCountry(new Country(false, 2, "Eastern Australia", australia,815, 425, 100, 100,"australien_1", 0), 0);
 		australia.setCountry(new Country(true, 3, "Indonesia", australia,682, 265, 200, 200,"australien_2", 1), 1);
 		australia.setCountry(new Country(false, 3, "New Guinea", australia,800, 345, 200, 100,"australien_3", 2), 2);
 		australia.setCountry(new Country(false, 3, "Western Australia", australia,772, 425, 100, 100,"australien_4", 3), 3);
 		
 		for (int i = 0; i < australia.getCountries().length; i++) {
 			gui.getPnlGame().add(australia.getCountry(i).getLabel());
 		}
 		
 		/* Setting neighbors for Alaska */
 		northAmerica.getCountry(0).setNeighbor(northAmerica.getCountry(1), 0);
 		northAmerica.getCountry(0).setNeighbor(northAmerica.getCountry(5), 1);
 		northAmerica.getCountry(0).setNeighbor(asia.getCountry(5), 2);
 		
 		/* Setting neighbors for Alberta */
 		northAmerica.getCountry(1).setNeighbor(northAmerica.getCountry(0), 0);
 		northAmerica.getCountry(1).setNeighbor(northAmerica.getCountry(5), 1);
 		northAmerica.getCountry(1).setNeighbor(northAmerica.getCountry(6), 2);
 		northAmerica.getCountry(1).setNeighbor(northAmerica.getCountry(8), 3);
 		
 		/* Setting neighbors for Central America */
 		northAmerica.getCountry(2).setNeighbor(northAmerica.getCountry(3), 0);
 		northAmerica.getCountry(2).setNeighbor(northAmerica.getCountry(8), 1);
 		northAmerica.getCountry(2).setNeighbor(southAmerica.getCountry(3), 2);
 		
 		/* Setting neighbors for Eastern United States */
 		northAmerica.getCountry(3).setNeighbor(northAmerica.getCountry(2), 0);
 		northAmerica.getCountry(3).setNeighbor(northAmerica.getCountry(6), 1);
 		northAmerica.getCountry(3).setNeighbor(northAmerica.getCountry(7), 2);
 		northAmerica.getCountry(3).setNeighbor(northAmerica.getCountry(8), 3);
 		
 		/* Setting neighbors for Greenland */
 		northAmerica.getCountry(4).setNeighbor(northAmerica.getCountry(5), 0);
 		northAmerica.getCountry(4).setNeighbor(northAmerica.getCountry(6), 1);
 		northAmerica.getCountry(4).setNeighbor(northAmerica.getCountry(7), 2);
 		northAmerica.getCountry(4).setNeighbor(europe.getCountry(1), 3);
 		
 		/* Setting neighbors for Northwest Territory */
 		northAmerica.getCountry(5).setNeighbor(northAmerica.getCountry(0), 0);
 		northAmerica.getCountry(5).setNeighbor(northAmerica.getCountry(1), 1);
 		northAmerica.getCountry(5).setNeighbor(northAmerica.getCountry(6), 2);
 		northAmerica.getCountry(5).setNeighbor(northAmerica.getCountry(4), 3);
 		
 		/* Setting neighbors for Ontario */
 		northAmerica.getCountry(6).setNeighbor(northAmerica.getCountry(1), 0);
 		northAmerica.getCountry(6).setNeighbor(northAmerica.getCountry(3), 1);
 		northAmerica.getCountry(6).setNeighbor(northAmerica.getCountry(4), 2);
 		northAmerica.getCountry(6).setNeighbor(northAmerica.getCountry(5), 3);
 		northAmerica.getCountry(6).setNeighbor(northAmerica.getCountry(7), 4);
 		northAmerica.getCountry(6).setNeighbor(northAmerica.getCountry(8), 5);
 		
 		/* Setting neighbors for Quebec */
 		northAmerica.getCountry(7).setNeighbor(northAmerica.getCountry(3), 0);
 		northAmerica.getCountry(7).setNeighbor(northAmerica.getCountry(4), 1);
 		northAmerica.getCountry(7).setNeighbor(northAmerica.getCountry(6), 2);
 		
 		/* Setting neighbors for Western United States */
 		northAmerica.getCountry(8).setNeighbor(northAmerica.getCountry(1), 0);
 		northAmerica.getCountry(8).setNeighbor(northAmerica.getCountry(2), 1);
 		northAmerica.getCountry(8).setNeighbor(northAmerica.getCountry(3), 2);
 		northAmerica.getCountry(8).setNeighbor(northAmerica.getCountry(6), 3);
 		
 		/* Setting neighbors for Argentina */
 		southAmerica.getCountry(0).setNeighbor(southAmerica.getCountry(1), 0);
 		southAmerica.getCountry(0).setNeighbor(southAmerica.getCountry(2), 1);
 		
 		/* Setting neighbors for Brazil */
 		southAmerica.getCountry(1).setNeighbor(southAmerica.getCountry(0), 0);
 		southAmerica.getCountry(1).setNeighbor(southAmerica.getCountry(2), 1);
 		southAmerica.getCountry(1).setNeighbor(southAmerica.getCountry(3), 2);
 		southAmerica.getCountry(1).setNeighbor(africa.getCountry(4), 3);
 		
 		/* Setting neighbors for Peru */
 		southAmerica.getCountry(2).setNeighbor(southAmerica.getCountry(0), 0);
 		southAmerica.getCountry(2).setNeighbor(southAmerica.getCountry(1), 1);
 		southAmerica.getCountry(2).setNeighbor(southAmerica.getCountry(3), 2);
 		
 		/* Setting neighbors for Venezuela */
 		southAmerica.getCountry(3).setNeighbor(southAmerica.getCountry(1), 0);
 		southAmerica.getCountry(3).setNeighbor(southAmerica.getCountry(2), 1);
 		southAmerica.getCountry(3).setNeighbor(northAmerica.getCountry(2), 2);
 		
 		/* Setting neighbors for Great Britain */
 		europe.getCountry(0).setNeighbor(europe.getCountry(1), 0);
 		europe.getCountry(0).setNeighbor(europe.getCountry(2), 1);
 		europe.getCountry(0).setNeighbor(europe.getCountry(3), 2);
 		europe.getCountry(0).setNeighbor(europe.getCountry(6), 3);
 		
 		/* Setting neighbors for Iceland */
 		europe.getCountry(1).setNeighbor(europe.getCountry(0), 0);
 		europe.getCountry(1).setNeighbor(europe.getCountry(3), 1);
 		europe.getCountry(1).setNeighbor(northAmerica.getCountry(4), 2);
 		
 		/* Setting neighbors for Northern Europe */
 		europe.getCountry(2).setNeighbor(europe.getCountry(0), 0);
 		europe.getCountry(2).setNeighbor(europe.getCountry(3), 1);
 		europe.getCountry(2).setNeighbor(europe.getCountry(4), 2);
 		europe.getCountry(2).setNeighbor(europe.getCountry(5), 3);
 		europe.getCountry(2).setNeighbor(europe.getCountry(6), 4);
 		
 		/* Setting neighbors for Scandinavia */
 		europe.getCountry(3).setNeighbor(europe.getCountry(0), 0);
 		europe.getCountry(3).setNeighbor(europe.getCountry(1), 1);
 		europe.getCountry(3).setNeighbor(europe.getCountry(2), 2);
 		europe.getCountry(3).setNeighbor(europe.getCountry(5), 3);
 		
 		/* Setting neighbors for Southern Europe */
 		europe.getCountry(4).setNeighbor(europe.getCountry(2), 0);
 		europe.getCountry(4).setNeighbor(europe.getCountry(5), 1);
 		europe.getCountry(4).setNeighbor(europe.getCountry(6), 2);
 		europe.getCountry(4).setNeighbor(asia.getCountry(6), 3);
 		europe.getCountry(4).setNeighbor(africa.getCountry(2), 4);
 		europe.getCountry(4).setNeighbor(africa.getCountry(4), 5);
 		
 		/* Setting neighbors for Ukraine */
 		europe.getCountry(5).setNeighbor(europe.getCountry(2), 0);
 		europe.getCountry(5).setNeighbor(europe.getCountry(3), 1);
 		europe.getCountry(5).setNeighbor(europe.getCountry(4), 2);
 		europe.getCountry(5).setNeighbor(asia.getCountry(0), 3);
 		europe.getCountry(5).setNeighbor(asia.getCountry(6), 4);
 		europe.getCountry(5).setNeighbor(asia.getCountry(10), 5);
 		
 		/* Setting neighbors for Western Europe */
 		europe.getCountry(6).setNeighbor(europe.getCountry(0), 0);
 		europe.getCountry(6).setNeighbor(europe.getCountry(2), 1);
 		europe.getCountry(6).setNeighbor(europe.getCountry(4), 2);
 		europe.getCountry(6).setNeighbor(africa.getCountry(4), 3);
 		
 		/* Setting neighbors for Congo */
 		africa.getCountry(0).setNeighbor(africa.getCountry(1), 0);
 		africa.getCountry(0).setNeighbor(africa.getCountry(4), 1);
 		africa.getCountry(0).setNeighbor(africa.getCountry(5), 2);
 		
 		/* Setting neighbors for East Africa */
 		africa.getCountry(1).setNeighbor(africa.getCountry(0), 0);
 		africa.getCountry(1).setNeighbor(africa.getCountry(2), 1);
 		africa.getCountry(1).setNeighbor(africa.getCountry(3), 2);
 		africa.getCountry(1).setNeighbor(africa.getCountry(4), 3);
 		africa.getCountry(1).setNeighbor(africa.getCountry(5), 4);
 		africa.getCountry(1).setNeighbor(asia.getCountry(6), 5);
 		
 		/* Setting neighbors for Egypt */
 		africa.getCountry(2).setNeighbor(africa.getCountry(1), 0);
 		africa.getCountry(2).setNeighbor(africa.getCountry(4), 1);
 		africa.getCountry(2).setNeighbor(europe.getCountry(4), 2);
 		africa.getCountry(2).setNeighbor(asia.getCountry(6), 3);
 		
 		/* Setting neighbors for Madagascar */
 		africa.getCountry(3).setNeighbor(africa.getCountry(1), 0);
 		africa.getCountry(3).setNeighbor(africa.getCountry(5), 1);
 		
 		/* Setting neighbors for North Africa */
 		africa.getCountry(4).setNeighbor(africa.getCountry(0), 0);
 		africa.getCountry(4).setNeighbor(africa.getCountry(1), 1);
 		africa.getCountry(4).setNeighbor(africa.getCountry(2), 2);
 		africa.getCountry(4).setNeighbor(southAmerica.getCountry(1), 3);
 		africa.getCountry(4).setNeighbor(europe.getCountry(6), 4);
 		africa.getCountry(4).setNeighbor(europe.getCountry(4), 5);
 		
 		/* Setting neighbors for South Africa */
 		africa.getCountry(5).setNeighbor(africa.getCountry(0), 0);
 		africa.getCountry(5).setNeighbor(africa.getCountry(1), 1);
 		africa.getCountry(5).setNeighbor(africa.getCountry(3), 2);
 		
 		/* Setting neighbors for Afghanistan */
 		asia.getCountry(0).setNeighbor(asia.getCountry(1), 0);
 		asia.getCountry(0).setNeighbor(asia.getCountry(2), 1);
 		asia.getCountry(0).setNeighbor(asia.getCountry(6), 2);
 		asia.getCountry(0).setNeighbor(asia.getCountry(10), 3);
 		asia.getCountry(0).setNeighbor(europe.getCountry(5), 4);
 		
 		/* Setting neighbors for China */
 		asia.getCountry(1).setNeighbor(asia.getCountry(0), 0);
 		asia.getCountry(1).setNeighbor(asia.getCountry(2), 1);
 		asia.getCountry(1).setNeighbor(asia.getCountry(7), 2);
 		asia.getCountry(1).setNeighbor(asia.getCountry(8), 3);
 		asia.getCountry(1).setNeighbor(asia.getCountry(9), 4);
 		asia.getCountry(1).setNeighbor(asia.getCountry(10), 5);
 		
 		/* Setting neighbors for India */
 		asia.getCountry(2).setNeighbor(asia.getCountry(0), 0);
 		asia.getCountry(2).setNeighbor(asia.getCountry(2), 1);
 		asia.getCountry(2).setNeighbor(asia.getCountry(6), 2);
 		asia.getCountry(2).setNeighbor(asia.getCountry(8), 3);
 		
 		/* Setting neighbors for Irkutsk */
 		asia.getCountry(3).setNeighbor(asia.getCountry(5), 0);
 		asia.getCountry(3).setNeighbor(asia.getCountry(7), 1);
 		asia.getCountry(3).setNeighbor(asia.getCountry(9), 2);
 		asia.getCountry(3).setNeighbor(asia.getCountry(11), 3);
 		
 		/* Setting neighbors for Japan */
 		asia.getCountry(4).setNeighbor(asia.getCountry(5), 0);
 		asia.getCountry(4).setNeighbor(asia.getCountry(7), 1);
 		
 		/* Setting neighbors for Kamchatka */
 		asia.getCountry(5).setNeighbor(asia.getCountry(3), 0);
 		asia.getCountry(5).setNeighbor(asia.getCountry(4), 1);
 		asia.getCountry(5).setNeighbor(asia.getCountry(7), 2);
 		asia.getCountry(5).setNeighbor(asia.getCountry(11), 3);
 		asia.getCountry(5).setNeighbor(northAmerica.getCountry(0), 4);
 		
 		/* Setting neighbors for Middle East */
 		asia.getCountry(6).setNeighbor(asia.getCountry(0), 0);
 		asia.getCountry(6).setNeighbor(asia.getCountry(2), 1);
 		asia.getCountry(6).setNeighbor(europe.getCountry(4), 2);
 		asia.getCountry(6).setNeighbor(europe.getCountry(5), 3);
 		asia.getCountry(6).setNeighbor(africa.getCountry(1), 4);
 		asia.getCountry(6).setNeighbor(africa.getCountry(2), 5);
 		
 		/* Setting neighbors for Mongolia */
 		asia.getCountry(7).setNeighbor(asia.getCountry(1), 0);
 		asia.getCountry(7).setNeighbor(asia.getCountry(3), 1);
 		asia.getCountry(7).setNeighbor(asia.getCountry(4), 2);
 		asia.getCountry(7).setNeighbor(asia.getCountry(5), 3);
 		asia.getCountry(7).setNeighbor(asia.getCountry(9), 4);
 		
 		/* Setting neighbors for Siam */
 		asia.getCountry(8).setNeighbor(asia.getCountry(1), 0);
 		asia.getCountry(8).setNeighbor(asia.getCountry(2), 1);
 		asia.getCountry(8).setNeighbor(australia.getCountry(1), 2);
 		
 		/* Setting neighbors for Siberia */
 		asia.getCountry(9).setNeighbor(asia.getCountry(1), 0);
 		asia.getCountry(9).setNeighbor(asia.getCountry(3), 1);
 		asia.getCountry(9).setNeighbor(asia.getCountry(7), 2);
 		asia.getCountry(9).setNeighbor(asia.getCountry(10), 3);
 		asia.getCountry(9).setNeighbor(asia.getCountry(11), 4);
 		
 		/* Setting neighbors for Ural */
 		asia.getCountry(10).setNeighbor(asia.getCountry(0), 0);
 		asia.getCountry(10).setNeighbor(asia.getCountry(1), 1);
 		asia.getCountry(10).setNeighbor(asia.getCountry(9), 2);
 		asia.getCountry(10).setNeighbor(europe.getCountry(5), 3);
 		
 		/* Setting neighbors for Yakutsk */
 		asia.getCountry(11).setNeighbor(asia.getCountry(3), 0);
 		asia.getCountry(11).setNeighbor(asia.getCountry(5), 1);
 		asia.getCountry(11).setNeighbor(asia.getCountry(9), 2);
 		
 		/* Setting neighbors for Eastern Australia */
 		australia.getCountry(0).setNeighbor(australia.getCountry(2), 0);
 		australia.getCountry(0).setNeighbor(australia.getCountry(3), 1);
 		
 		/* Setting neighbors for Indonesia */
 		australia.getCountry(1).setNeighbor(australia.getCountry(2), 0);
 		australia.getCountry(1).setNeighbor(australia.getCountry(3), 1);
 		australia.getCountry(1).setNeighbor(asia.getCountry(8), 2);
 		
 		/* Setting neighbors for New Guinea */
 		australia.getCountry(2).setNeighbor(australia.getCountry(0), 0);
 		australia.getCountry(2).setNeighbor(australia.getCountry(1), 1);
 		australia.getCountry(2).setNeighbor(australia.getCountry(3), 2);
 		
 		/* Setting neighbors for Western Australia */
 		australia.getCountry(3).setNeighbor(australia.getCountry(0), 0);
 		australia.getCountry(3).setNeighbor(australia.getCountry(1), 1);
 		australia.getCountry(3).setNeighbor(australia.getCountry(2), 2);
 	}
 	
 	private void initRiskCards() {
 		int cSoldier = 0, cCavalry = 0, cCanon = 0, cJoker = 0;
 		this.riskCards = new RiscCard[44];
 		Random r = new Random();
 		int i = 0;
 		while (i < 44) {
 			int t = r.nextInt(4);
 			if (t == 0 && cJoker < 2) {
 				this.riskCards[i] = new RiscCard(t);
 				cJoker++;
 				i++;
 			} else if (t == 1 && cSoldier < 14) {
 				this.riskCards[i] = new RiscCard(t);
 				cSoldier++;
 				i++;
 			} else if (t == 2 && cCavalry < 14) {
 				this.riskCards[i] = new RiscCard(t);
 				cCavalry++;
 				i++;
 			} else if (t == 3 && cCanon < 14) {
 				this.riskCards[i] = new RiscCard(t);
 				cCanon++;
 				i++;
 			}
 		}
 		System.out.println("Joker: " + cJoker + "\nSoldier: " + cSoldier + "\nCavalry: " +cCavalry + "\nCanon: " + cCanon);
 	}
 }
