 /** 
  * @author Nick Solberg
  * @author Ian Paterson
  * @author Austin Langohorn
  */
 
 package bteam.capstone.risk;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Random;
 import java.util.Scanner;
 import java.util.Stack;
 
 public class Play {
 	//nicks using this baby
 	ArrayList<player> playerStack = new ArrayList<player>();
 	private int numOfDice;
 	private boolean isAttacking;
 	public ArrayList<Integer> missionAvail = new ArrayList<Integer>();
 	private boolean newMissionCard = true;
 	private int missionInt = -1;
 	
 	/**
 	 * @author Ian Paterson
 	 * @param isDraft: Determines whether or not the various draft cards are active
 	 * @param numPlayers: Self explanatory, how many players are playing
 	 * @param players: an array of players
 	 * @param whosTurn: Array list for keepng track of who's turn it is
 	 * @param world: a Map which is the game board itself
 	 */
 	private boolean isDraft; // Weather or not draft cards are active
 	private int numPlayers;
 	private player[] players;
 	private ArrayList<Integer> whosTurn;
 	private Map world;
 	
 	
 	/**
 	 * @author Ian Paterson
 	 * 
 	 * @param legacy: Determines weatehr or not it is standard or legacy
 	 * @description The purpose of this method is to set up the first turn of 
 	 * any Risk standard or Risk Legacy game. Its first function is it takes 
 	 * in the number of players that will be participating and add's them
 	 * too two arrayList's that keep track of who's turn it is and whosPlace which
 	 * keeps track of whos "turn" it is to place troops. If draft cards are active 
 	 * this method becomes far more complicates as we have to go through the 
 	 * various types of draft cards which determine placement order, the amount
 	 * of troops you can place, the turn order for the game to be played, the number
 	 * of "coins" you will receive. Draft cards are selected by random assignment after
 	 * the arraylist of cards is shuffled and they are "dealed out". After a card has
 	 * been assigned it is removed from the array list. IF draft cards are not active,
 	 * then these values are all assigned to their standard values as prescribed by the
 	 * game rules. If it is standard risk then this is far simpler and most of the values 
 	 * are not even needed, they are simply initialized to their standard values. 
 	 * 
 	 *@TODO The client/server communication needs to be implimented by austin.
 	 *@TODO Implementations of faction assighnment 
 	 *@TODO Other faction ablilites need to be added 
 	 */
 	public void firstTurnSetup(boolean legacy) {
 		
 		
 		ArrayList<Integer> whosPlace = new ArrayList<Integer>();
 		for (int i = 0; i < numPlayers; i++) {
 			whosTurn.add(i);
 			whosPlace.add(i);
 		}
 		if (legacy) {
 			if (isDraft) {
 				ArrayList<Integer> placement = new ArrayList<Integer>();
 				placement.add(1);
 				placement.add(2);
 				placement.add(3);
 				if (numPlayers > 3) {
 					placement.add(4);
 					if (numPlayers > 4)
 						placement.add(5);
 				}
 
 				ArrayList<Integer> turnOrder = new ArrayList<Integer>();
 				turnOrder.add(1);
 				turnOrder.add(2);
 				turnOrder.add(3);
 				if (numPlayers > 3) {
 					turnOrder.add(4);
 					if (numPlayers > 4)
 						turnOrder.add(5);
 				}
 				ArrayList<Integer> troops = new ArrayList<Integer>();
 				troops.add(6);
 				troops.add(8);
 				troops.add(10);
 				if (numPlayers > 3) {
 					troops.add(8);
 					if (numPlayers > 4)
 						troops.add(10);
 				}
 				ArrayList<Integer> coins = new ArrayList<Integer>();
 				coins.add(0);
 				coins.add(1);
 				coins.add(2);
 				if (numPlayers > 3) {
 					coins.add(0);
 					if (numPlayers > 4)
 						coins.add(1);
 				}
 				// Temproray order of players for choosing draft cards
 				ArrayList<Integer> tempOrder = new ArrayList<Integer>();
 				for (int i = 0; i < numPlayers; i++) {
 					tempOrder.add(i);
 				}
 				Random ran = new Random();
 				// Randomizes temporary order
 				for (int i = 0; i < numPlayers; i++) {
 					int c1 = i;
 					int c2 = ran.nextInt(numPlayers);
 					int temp = tempOrder.get(c1);
 					tempOrder.set(c1, tempOrder.get(c2));
 					tempOrder.set(c2, temp);
 				}
 				while (troops.size() != 0 || coins.size() != 0
 						|| turnOrder.size() != 0 || placement.size() != 0) {
 					int p = tempOrder.remove(0);
 					// Send to player p: "Choose draft card"
 					// Get choice from player
 					String choice = "p 0";
 					int val = Integer.parseInt(choice.charAt(2) + "");
 					if (choice.charAt(0) == 'p') {
 						val = placement.remove(val);
 						whosPlace.set(val, p);
 					} else if (choice.charAt(0) == 't') {
 						players[p].setTroops(troops.remove(val));
 					} else if (choice.charAt(0) == 'c') {
 						players[p].setCoin(coins.remove(val));
 					} else if (choice.charAt(0) == 'o') {
 						val = turnOrder.remove(val);
 						whosTurn.set(val, p);
 					}
 				}
 
 			} else {
 				// non draft
 				Random ran = new Random();
 				for (int i = 0; i < numPlayers; i++) {
 					int c1 = i;
 					int c2 = ran.nextInt(numPlayers);
 					int temp = whosTurn.get(c1);
 					whosTurn.set(c1, whosTurn.get(c2));
 					whosTurn.set(c2, temp);
 					players[i].setTroops(8);
 					players[i].setCoin(0);
 				}
 				whosPlace = whosTurn;
 			}
	
 			int i = 0;
 			int p;
 			while (i < numPlayers) {
 				p = whosPlace.remove(0);
 				whosPlace.add(p);
 				i++;
 				boolean valid = false;
 				// ask player and place troops
 				while (!valid) {
 					sendDataToClient(p, "Choose a Valid Starting Continent");
 					int choice = getDataFromBuffer();
 					if (world.getCountry(choice).getControllingFaction()
 							.equals("\\NONE")) {
 						if (world.getCountry(choice).getCityType() == 0
 								&& world.getCountry(choice).getScarType() == 0
 								|| world.getCountry(choice).getCityType() == 2) {
 							valid = true;
 							world.placeHQ(choice, players[p].getFaction()
 									.getName());
 							world.placeTroops(choice, players[p].getTroops(),
 									players[p].getFaction().getName());
 							players[p].setTroops(0);
 							sendDataToClient(p, "Accept");
 						}
 					}
 				}
 			}
 		} else {
 			// non legacy
 			Random ran = new Random();
 			for (int i = 0; i < numPlayers; i++) {
 				int c1 = i;
 				int c2 = ran.nextInt(numPlayers);
 				int temp = whosTurn.get(c1);
 				whosTurn.set(c1, whosTurn.get(c2));
 				whosTurn.set(c2, temp);
 				players[i].setTroops(35 - 5 * (numPlayers - 3));
 			}
 			int p;
 			// AUSTIN GET ON YOUR SHIT
 			while (world.hasFreeCountry()) {
 				p = whosPlace.remove(0);
 				whosPlace.add(p);
 				boolean valid = false;
 				// ask player to place troop in country
 				while (!valid) {
 					sendDataToClient(p, "Choose a Valid Continent");
 					int choice = getDataFromBuffer();
 					if(world.getCountry(choice).getControllingFaction().equals("\\NONE")){
 						world.placeTroops(choice, 1, players[p].getName());
 						players[p].setTroops(players[p].getTroops()-1);
 						valid = true;
 						sendDataToClient(p,"Accept");
 					}
 				}
 			}
 		}
 	}
 	/**
 	 * @author Nick Solberg
 	 * @param atkCountry represents the attackers country
 	 * @param defCountry represents the defenders country
 	 * 
 	 * This method will resolve an attack phase between two players
 	 * Given the attacking country and defending country the method
 	 * will retrieve the number of troops each side has.
 	 * The attacking player will be allowed to choose how many dice 
 	 * will be rolled in their phase, given the attacker appropriate 
 	 * has available resources. The attacker is also given the option
 	 * to cancel the attack after any round. The loop will stop when the
 	 * attacking player has 1 troop or the denfeder has 0. Given a player
 	 * has missles. They are offered the chance to use one and pick the dice
 	 * they would like to apply it too. Each player will be asked twice
 	 * whether they would like to or not. The second time being a double check.
 	 * 
 	 * TODO 
 	 * Later modify this method to work with a gui and client
 	 * City and Fortifications.
 	 */
 	//playTestGui gui = new playTestGui();
 	public void attack(Country atkCountry, Country defCountry, Map map) {
 		Scanner inScan = new Scanner(System.in);
 		String missleTemp = "";
 		int loopUntil = 0;
 		int numAtkDice = 0;
 		int numDefDice = 0;
 		int misslesUsed = 0;
 		boolean missleOnDefender = false;
 		boolean isAttacking = true;
 		if (atkCountry.getCountryBorders().contains(defCountry.id()) == false) {
 			System.out.println("This will be an invalid attack");
 			isAttacking = false;
 		}
 		// else if (isAttacking == true) {
 		while (isAttacking == true && defCountry.getTroopQuantity() > 0
 				&& atkCountry.getTroopQuantity() > 1) {
 			System.out.println("Attack continues, valid arguments");
 			int switchVal = 0;
 			if (atkCountry.getTroopQuantity() >= 4) {
 
 				switchVal += 10;
 			} else if (atkCountry.getTroopQuantity() == 3) {
 
 				switchVal += 5;
 			} else if (atkCountry.getTroopQuantity() == 2) {
 
 				switchVal += 2;
 			}
 			if (atkCountry.getTroopQuantity() == 1) {
 				System.out
 						.println("Not enough troops to attack, must be greater than one");
 				break;
 			}
 
 			if (defCountry.getTroopQuantity() >= 2) {
 				switchVal += 2;
 			} else if (defCountry.hasHQ() == true) {
 				switchVal += 2;
 			}
 
 			System.out.println(switchVal);
 			System.out.println("Current attackers troops: "
 					+ atkCountry.getTroopQuantity());
 			System.out.println("Current defenders troops: "
 					+ defCountry.getTroopQuantity());
 			Scanner defInScanner = new Scanner(System.in);
 			defInScanner.reset();
 			String temp;
 			String temp2;
 			if (switchVal == 4) {
 				//sendToClient(defCountry.getOwner(), "Would you like to roll one or two dice");
 				//temp = getClientResponse();
 				System.out.println("Defender would you like to roll one or two dice?");
 				temp = defInScanner.next();
 				if (temp.equals("one")) {
 					switchVal = 2;
 					System.out.println("1v1");
 				} else if (temp.equals("two")) {
 					switchVal = 4;
 					System.out.println("1v2");
 				}
 			}
 			if (switchVal == 5) {
 				//sendToClient(atkCountry.getOwner(), "Would you like to roll one or two dice");
 				//temp = getClientResponse();
 				System.out.println("Attacker would you like to roll one or two dice?");
 				temp = defInScanner.next();
 				if (temp.equals("one")) {
 					switchVal = 2;
 					System.out.println("1v1");
 				} else if (temp.equals("two")) {
 					switchVal = 5;
 					System.out.println("2v1");
 				}
 			}
 
 			if (switchVal == 7) {
 				//sendToClient(atkCountry.getOwner(), "Would you like to roll one or two dice");
 				//temp = getClientResponse();
 				System.out.println("Attacker would you like to roll one or two dice?");
 				temp = defInScanner.next();
 				//sendToClient(defCountry.getOwner(), "Would you like to roll one or two dice");
 				//temp = getClientResponse();
 				System.out.println("Defender would you like to roll one or two dice?");
 				temp2 = defInScanner.next();
 				if (temp.equals("one") && temp2.equals("two")) {
 					switchVal = 4;
 					System.out.println("1v2");
 				} else if (temp.equals("two") && temp2.equals("two")) {
 					switchVal = 7;
 					System.out.println("2v2");
 				}
 				else if (temp.equals("one") && temp2.equals("one")) {
 					switchVal = 2;
 					System.out.println("1v1");
 				}
 				else if (temp.equals("two") && temp2.equals("one")) {
 					switchVal = 5;
 					System.out.println("2v1");
 				}
 			}
 
 			if (switchVal == 10) {
 				//sendToClient(atkCountry.getOwner(), "Would you like to roll one, two, or three dice");
 				//temp = getClientResponse();
 				System.out.println("Attacker would you like to roll one, two, or three dice?");
 				temp = defInScanner.next();
 				if (temp.equals("one")) {
 					switchVal = 2;
 					System.out.println("1v1");
 				} else if (temp.equals("two")) {
 					switchVal = 5;
 					System.out.println("2v1");
 				} else if (temp.equals("three")) {
 					switchVal = 10;
 					System.out.println("3v1");
 				}
 			}
 
 			if (switchVal == 12) {
 				//sendToClient(atkCountry.getOwner(), "Would you like to roll one or two dice");
 				//temp = getClientResponse();
 				System.out.println("Attacker would you like to roll one, two, or three dice?");
 				temp = defInScanner.next();
 				//sendToClient(defCountry.getOwner(), "Would you like to roll one or two dice");
 				//temp = getClientResponse();
 				System.out.println("Defender would you like to roll one or two dice?");
 				temp2 = defInScanner.next();
 				if (temp.equals("one") && temp2.equals("two")) {
 					switchVal = 4;
 					System.out.println("1v2");
 				}
 				else if (temp.equals("two") && temp2.equals("two")) {
 					switchVal = 7;
 					System.out.println("2v2");
 				}
 				else if (temp.equals("three") && temp2.equals("two")) {
 					switchVal = 12;
 					System.out.println("3v2");
 				}
 				else if (temp.equals("one") && temp2.equals("one")) {
 					switchVal = 2;
 					System.out.println("1v1");
 				}
 				else if (temp.equals("two") && temp2.equals("one")) {
 					switchVal = 5;
 					System.out.println("2v1");
 				}
 				else if (temp.equals("three") && temp2.equals("one")) {
 					switchVal = 10;
 					System.out.println("3v1");
 				}
 			}
 
 			switch (switchVal) {
 			case 12:
 				System.out.println("3v2");
 				/*
 				 * If fortified
 				 * defender adds 1 to both of his dice. 
 				 * If the attacker attacks with 3 troops, the fortification gets worn down
 				 */
 				int[] attack6 = { randomDice(), randomDice(), randomDice() };
 				Arrays.sort(attack6);
 				int[] defense6 = { randomDice(), randomDice() };
 				Arrays.sort(defense6);
 				numOfDice = 4;
 				System.out.println();
 				System.out.print("attack die from lowest to highest ");
 				for (int i = 0; i < 3; i++) {
 					System.out.print(attack6[i] + " ");
 				}
 				System.out.println();
 				System.out.print("defense die from lowest to highest ");
 				for (int i = 0; i < 2; i++) {
 					System.out.print(defense6[i] + " ");
 				}
 				//Missles
 				inScan = new Scanner(System.in);
 				missleTemp = "";
 				loopUntil = 2;
 				numAtkDice = 2;
 				numDefDice = 2;
 				misslesUsed = 0;
 				missleOnDefender = false;
 				while(loopUntil > 0){
 					for(int i = playerStack.size()-1; i > -1; i--){
 						if(misslesUsed == numOfDice){
 							break;
 						}
 						if(playerStack.get(i).getMissles()  > 0){
 							System.out.println();
 							//sendToClient(playerStack.get(i), "Would you like to use a missile?");
 							//missletemp = getClientResponse();
 							System.out.println(playerStack.get(i).getName()+ "would you like to use a missile? 'yes' or 'no'");
 							missleTemp = inScan.next();
 							if(missleTemp.equals("yes")){
 								//sendToClient(playerStack.get(i), "Defender or Attackers Dice?");
 								//missletemp = getClientResponse();
 								System.out.println("Defender or Attackers Dice? 'd' or 'f'");
 								missleTemp = inScan.next();
 								if(missleTemp.equals("d") && numDefDice > 0){
 									//sendToClient(playerStack.get(i), "Which dice toll would you like to apply the missile to?");
 									//missletemp = getClientResponse();
 									System.out.println("Which dice roll would you like to apply the missile to? " + 
 									"first or second (first being lowest)");
 									missleTemp = inScan.next();
 									if(missleTemp.equals("first")){
 										System.out.println("Changed " + defense6[0] +" to a 6");
 										defense6[0] = 6;
 										missleOnDefender = true;
 										playerStack.get(i).setMissles(playerStack.get(i).getMissles()-1);
 										misslesUsed++;
 										numDefDice--;
 									} 
 									else if(missleTemp.equals("second")){
 										System.out.println("Changed " + defense6[1] +" to a 6");
 										defense6[1] = 6;
 										playerStack.get(i).setMissles(playerStack.get(i).getMissles()-1);
 										misslesUsed++;
 										numDefDice--;
 									}
 									
 								} else if(missleTemp.equals("f") && numAtkDice > 0){
 									//sendToClient(playerStack.get(i), "Which dice roll would you like to apply the missile to?");
 									//missleTemp = getClientResponse();
 									System.out.println("Which dice roll would you like to apply the missile to? second or third(second being lowest)");
 									missleTemp = inScan.next();
 									if(missleTemp.equals("second")){
 										System.out.println("Changed " + attack6[1] +" to a 6");
 										attack6[1] = 6;
 										playerStack.get(i).setMissles(playerStack.get(i).getMissles()-1);
 										misslesUsed++;
 										numDefDice--;
 									} 
 									else if(missleTemp.equals("third")){
 										System.out.println("Changed " + attack6[2] +" to a 6");
 										attack6[2] = 6;
 										playerStack.get(i).setMissles(playerStack.get(i).getMissles()-1);
 										misslesUsed++;
 										numDefDice--;
 									}
 								}
 							}
 						}
 					}
 					loopUntil--;
 						
 				}
 				System.out.println();
 				if(defCountry.getScarType() == 1 && missleOnDefender == false){
 					defense6[1] -= 1;
 				}
 				if(defCountry.getScarType() == 2 && missleOnDefender == false){
 					if(defense6[1] < 6){
 						defense6[1] += 1;
 					}
 				}
 				
 				if(defCountry.isCityFortified() == true){
 					if(defense6[1] < 6){
 						defense6[1] += 1;
 					}
 					if(defense6[0] < 6){
 						defense6[0] += 1;
 					}
 					defCountry.setFortifyEnergy(defCountry.getFortifyEnergy()-1);
 				}
 				
 				if (attack6[2] > defense6[1]) {
 					defCountry
 							.setTroopQuantity(defCountry.getTroopQuantity() - 1);
 					System.out.println(attack6[2] + " is greater than "
 							+ defense6[1]);
 				}
 				if (attack6[2] <= defense6[1]) {
 					atkCountry
 							.setTroopQuantity(atkCountry.getTroopQuantity() - 1);
 					System.out.println(attack6[2] + " is less than or equal "
 							+ defense6[1]);
 				}
 				if (attack6[1] > defense6[0]) {
 					defCountry
 							.setTroopQuantity(defCountry.getTroopQuantity() - 1);
 					System.out.println(attack6[1] + " is greater than "
 							+ defense6[0]);
 				} else {
 					atkCountry
 							.setTroopQuantity(atkCountry.getTroopQuantity() - 1);
 					System.out.println(attack6[1] + " is less than or equal "
 							+ defense6[0]);
 				}
 				System.out.println("Attackers troops remaining: "
 						+ atkCountry.getTroopQuantity());
 				System.out.println("Defeners troops remaining: "
 						+ defCountry.getTroopQuantity());
 				switchVal = 0;
 				break;
 			case 10:
 				System.out.println("3v1");
 				int[] attack5 = { randomDice(), randomDice(), randomDice() };
 				Arrays.sort(attack5);
 				int[] defense5 = { randomDice() };
 				Arrays.sort(defense5);
 				numOfDice = 2;
 				System.out.println();
 				System.out.print("attack die from lowest to highest ");
 				for (int i = 0; i < 3; i++) {
 					System.out.print(attack5[i] + " ");
 				}
 				System.out.println();
 				System.out.print("defense die from lowest to highest ");
 				System.out.print(defense5[0] + " ");
 				System.out.println();
 				
 				//Missles
 				inScan = new Scanner(System.in);
 				missleTemp = "";
 				loopUntil = 2;
 				numAtkDice = 1;
 				numDefDice = 1;
 				misslesUsed = 0;
 				missleOnDefender = false;
 				while(loopUntil > 0){
 					for(int i = playerStack.size()-1; i > -1; i--){
 						if(misslesUsed == numOfDice){
 							break;
 						}
 						if(playerStack.get(i).getMissles()  > 0){
 							System.out.println();
 							//sendToClient(playerStack.get(i), "Would you like to use a missle?");
 							//missletemp = getClientResponse();
 							System.out.println(playerStack.get(i).getName()+ "would you like to use a missle? 'yes' or 'no'");
 							missleTemp = inScan.next();
 							if(missleTemp.equals("yes")){
 								//sendToClient(playerStack.get(i), "Defender or Attackers dice?");
 								//missletemp = getClientResponse();
 								System.out.println("Defender or Attackers Dice? 'd' or 'f'");
 								missleTemp = inScan.next();
 								if(missleTemp.equals("d") && numDefDice > 0){
 									System.out.println("Missle used on defenders dice");
 									System.out.println("Changed " + defense5[0] +" to a 6");
 									defense5[0] = 6;
 									missleOnDefender = true;
 									playerStack.get(i).setMissles(playerStack.get(i).getMissles()-1);
 									misslesUsed++;
 									numDefDice--;
 								} else if(missleTemp.equals("f") && numAtkDice > 0){
 									System.out.println("Missle used on attackers dice");
 									System.out.println("Changed " + attack5[2] +" to a 6");
 									attack5[2] = 6;
 									playerStack.get(i).setMissles(playerStack.get(i).getMissles()-1);
 									misslesUsed++;
 									numAtkDice--;
 								}
 							}
 						}
 					}
 					loopUntil--;
 						
 				}
 				if(defCountry.getScarType() == 1 && missleOnDefender == false){
 					defense5[0] -= 1;
 				}
 				if(defCountry.getScarType() == 2 && missleOnDefender == false){
 					if(defense5[0] < 6){
 						defense5[0] += 1;
 					}
 				}
 				
 				if(defCountry.isCityFortified() == true){
 					if(defense5[0] < 6){
 						defense5[0] += 1;
 					}
 					defCountry.setFortifyEnergy(defCountry.getFortifyEnergy()-1);
 				}
 				
 				
 				if (attack5[2] > defense5[0]) {
 					defCountry
 							.setTroopQuantity(defCountry.getTroopQuantity() - 1);
 					System.out.println(attack5[2] + " is greater than "
 							+ defense5[0]);
 				}
 				if (attack5[2] <= defense5[0]) {
 					atkCountry
 							.setTroopQuantity(atkCountry.getTroopQuantity() - 1);
 					System.out.println(attack5[2] + " is less than or equal "
 							+ defense5[0]);
 				}
 				System.out.println("Attackers troops remaining: "
 						+ atkCountry.getTroopQuantity());
 				System.out.println("Defeners troops remaining: "
 						+ defCountry.getTroopQuantity());
 				switchVal = 0;
 
 				break;
 			case 7:
 				System.out.println("2v2");
 				int[] attack4 = { randomDice(), randomDice() };
 				Arrays.sort(attack4);
 				int[] defense4 = { randomDice(), randomDice() };
 				Arrays.sort(defense4);
 				numOfDice = 4;
 				System.out.println();
 				System.out.print("attack die from lowest to highest ");
 				for (int i = 0; i < 2; i++) {
 					System.out.print(attack4[i] + " ");
 				}
 				System.out.println();
 				System.out.print("defense die from lowest to highest ");
 				for (int i = 0; i < 2; i++) {
 					System.out.print(defense4[i] + " ");
 				}
 				//Missles
 				inScan = new Scanner(System.in);
 				missleTemp = "";
 				loopUntil = 2;
 				numAtkDice = 2;
 				numDefDice = 2;
 				misslesUsed = 0;
 				missleOnDefender = false;
 				while(loopUntil > 0){
 					for(int i = playerStack.size()-1; i > -1; i--){
 						if(misslesUsed == numOfDice){
 							break;
 						}
 						if(playerStack.get(i).getMissles()  > 0){
 							System.out.println();
 							//sendToClient(playerStack.get(i), "Would you like to use a missle?");
 							//missletemp = getClientResponse();
 							System.out.println(playerStack.get(i).getName()+ "would you like to use a missle? 'yes' or 'no'");
 							missleTemp = inScan.next();
 							if(missleTemp.equals("yes")){
 								//sendToClient(playerStack.get(i), "Attacker or defenders dice?");
 								//missletemp = getClientResponse();
 								System.out.println("Defender or Attackers Dice? 'd' or 'f'");
 								missleTemp = inScan.next();
 								if(missleTemp.equals("d") && numDefDice > 0){
 									//sendToClient(playerStack.get(i), "Which dice toll would you like to apply the missile to?");
 									//missletemp = getClientResponse();
 									System.out.println("Which dice roll would you like to apply the missle to? " + 
 									"first or second (first being lowest)");
 									missleTemp = inScan.next();
 									if(missleTemp.equals("first")){
 										System.out.println("Changed " + defense4[0] +" to a 6");
 										defense4[0] = 6;
 										missleOnDefender = true;
 										playerStack.get(i).setMissles(playerStack.get(i).getMissles()-1);
 										misslesUsed++;
 										numDefDice--;
 									} 
 									else if(missleTemp.equals("second")){
 										System.out.println("Changed " + defense4[1] +" to a 6");
 										defense4[1] = 6;
 										playerStack.get(i).setMissles(playerStack.get(i).getMissles()-1);
 										misslesUsed++;
 										numDefDice--;
 									}
 									
 								} else if(missleTemp.equals("f") && numAtkDice > 0){
 									//sendToClient(playerStack.get(i), "Which dice toll would you like to apply the missile to?");
 									//missletemp = getClientResponse();
 									System.out.println("Which dice roll would you like to apply the missle to? first or second(first being lowest)");
 									missleTemp = inScan.next();
 									if(missleTemp.equals("first")){
 										System.out.println("Changed " + attack4[1] +" to a 6");
 										attack4[0] = 6;
 										playerStack.get(i).setMissles(playerStack.get(i).getMissles()-1);
 										misslesUsed++;
 										numDefDice--;
 									} 
 									else if(missleTemp.equals("second")){
 										System.out.println("Changed " + attack4[2] +" to a 6");
 										attack4[1] = 6;
 										playerStack.get(i).setMissles(playerStack.get(i).getMissles()-1);
 										misslesUsed++;
 										numDefDice--;
 									}
 								}
 							}
 						}
 					}
 					loopUntil--;
 						
 				}
 				System.out.println();
 				if(defCountry.getScarType() == 1 && missleOnDefender == false){
 					defense4[1] -= 1;
 				}
 				if(defCountry.getScarType() == 2 && missleOnDefender == false){
 					if(defense4[1] < 6){
 						defense4[1] += 1;
 					}
 				}
 				
 				if(defCountry.isCityFortified() == true){
 					if(defense4[1] < 6){
 						defense4[1] += 1;
 					}
 					if(defense4[0] < 6){
 						defense4[0] += 1;
 					}
 				}
 				
 				if (attack4[1] > defense4[1]) {
 					defCountry
 							.setTroopQuantity(defCountry.getTroopQuantity() - 1);
 					System.out.println(attack4[1] + " is greater than "
 							+ defense4[1]);
 				}
 				if (attack4[1] <= defense4[1]) {
 					atkCountry
 							.setTroopQuantity(atkCountry.getTroopQuantity() - 1);
 					System.out.println(attack4[1] + " is less than or equal "
 							+ defense4[1]);
 				}
 				if (attack4[0] > defense4[0]) {
 					defCountry
 							.setTroopQuantity(defCountry.getTroopQuantity() - 1);
 					System.out.println(attack4[0] + " is greater than "
 							+ defense4[0]);
 				} else {
 					atkCountry
 							.setTroopQuantity(atkCountry.getTroopQuantity() - 1);
 					System.out.println(attack4[0] + " is less than or equal "
 							+ defense4[0]);
 				}
 				System.out.println("Attackers troops remaining: "
 						+ atkCountry.getTroopQuantity());
 				System.out.println("Defeners troops remaining: "
 						+ defCountry.getTroopQuantity());
 				switchVal = 0;
 
 				break;
 			case 5:
 				System.out.println("2v1");
 				int[] attack3 = { randomDice(), randomDice() };
 				Arrays.sort(attack3);
 				int[] defense3 = { randomDice() };
 				Arrays.sort(defense3);
 				numOfDice = 3;
 				System.out.println();
 				System.out.print("attack die from lowest to highest ");
 				for (int i = 0; i < 2; i++) {
 					System.out.print(attack3[i] + " ");
 				}
 				System.out.println();
 				System.out.print("defense die from lowest to highest ");
 
 				System.out.print(defense3[0] + " ");
 				
 				//Missles
 				inScan = new Scanner(System.in);
 				missleTemp = "";
 				loopUntil = 2;
 				numAtkDice = 2;
 				numDefDice = 1;
 				misslesUsed = 0;
 				missleOnDefender = false;
 				while(loopUntil > 0){
 					for(int i = playerStack.size()-1; i > -1; i--){
 						if(misslesUsed == numOfDice){
 							break;
 						}
 						if(playerStack.get(i).getMissles()  > 0){
 							//sendToClient(playerStack.get(i), "Would you like to use a missle?");
 							//missletemp = getClientResponse();
 							System.out.println(playerStack.get(i).getName()+ "would you like to use a missle? 'yes' or 'no'");
 							missleTemp = inScan.next();
 							if(missleTemp.equals("yes")){
 								System.out.println("Defender or Attackers Dice? 'd' or 'f'");
 								missleTemp = inScan.next();
 								if(missleTemp.equals("d") && numDefDice > 0){
 										System.out.println("Changed " + defense3[0] +" to a 6");
 										defense3[0] = 6;
 										missleOnDefender = true;
 										playerStack.get(i).setMissles(playerStack.get(i).getMissles()-1);
 										misslesUsed++;
 										numDefDice--;
 									
 								} else if(missleTemp.equals("f") && numAtkDice > 0){
 									//sendToClient(playerStack.get(i), "Which dice toll would you like to apply the missile to?");
 									//missletemp = getClientResponse();
 									System.out.println("Which dice roll would you like to apply the missle to? first or second(first being the lowest)");
 									missleTemp = inScan.next();
 									if(missleTemp.equals("first")){
 										System.out.println("Changed " + defense3[0] +" to a 6");
 										attack3[0] = 6;
 										missleOnDefender = true;
 										playerStack.get(i).setMissles(playerStack.get(i).getMissles()-1);
 										misslesUsed++;
 										numDefDice--;
 									} 
 									else if(missleTemp.equals("second")){
 										System.out.println("Changed " + attack3[1] +" to a 6");
 										attack3[1] = 6;
 										playerStack.get(i).setMissles(playerStack.get(i).getMissles()-1);
 										misslesUsed++;
 										numDefDice--;
 									}
 								}
 							}
 						}
 					}
 					loopUntil--;
 						
 				}
 				System.out.println();
 				if(defCountry.getScarType() == 1 && missleOnDefender == false){
 					defense3[0] -= 1;
 				}
 				if(defCountry.getScarType() == 2 && missleOnDefender == false){
 					if(defense3[0] < 6){
 						defense3[0] += 1;
 					}
 				}
 				
 				if(defCountry.isCityFortified() == true){
 					
 					if(defense3[0] < 6){
 						defense3[0] += 1;
 					}
 				}
 				if (attack3[1] > defense3[0]) {
 					defCountry
 							.setTroopQuantity(defCountry.getTroopQuantity() - 1);
 					System.out.println(attack3[1] + " is greater than "
 							+ defense3[0]);
 				} else {
 					atkCountry
 							.setTroopQuantity(atkCountry.getTroopQuantity() - 1);
 					System.out.println(attack3[1] + " is less than or equal "
 							+ defense3[0]);
 				}
 				System.out.println("Attackers troops remaining: "
 						+ atkCountry.getTroopQuantity());
 				System.out.println("Defeners troops remaining: "
 						+ defCountry.getTroopQuantity());
 				switchVal = 0;
 
 				break;
 			case 4:
 				// 1vs2
 				System.out.println("1v2");
 				int[] attack1 = { randomDice() };
 				Arrays.sort(attack1);
 				int[] defense1 = { randomDice(), randomDice() };
 				Arrays.sort(defense1);
 				numOfDice = 3;
 				System.out.println();
 				System.out.print("attack die from lowest to highest ");
 				System.out.print(attack1[0] + " ");
 				System.out.println();
 				System.out.print("defense die from lowest to highest ");
 				
 				//Missles
 				inScan = new Scanner(System.in);
 				missleTemp = "";
 				loopUntil = 2;
 				numAtkDice = 1;
 				numDefDice = 2;
 				misslesUsed = 0;
 				missleOnDefender = false;
 				while(loopUntil > 0){
 					for(int i = playerStack.size()-1; i > -1; i--){
 						if(misslesUsed == numOfDice){
 							break;
 						}
 						if(playerStack.get(i).getMissles()  > 0){
 							//sendToClient(playerStack.get(i), "Would you like to use a misslie?");
 							//missletemp = getClientResponse();
 							System.out.println(playerStack.get(i).getName()+ "would you like to use a missle? 'yes' or 'no'");
 							missleTemp = inScan.next();
 							if(missleTemp.equals("yes")){
 								//sendToClient(playerStack.get(i), "Attacker or defenders missile?");
 								//missletemp = getClientResponse();
 								System.out.println("Defender or Attackers Dice? 'd' or 'f'");
 								missleTemp = inScan.next();
 								if(missleTemp.equals("d") && numDefDice > 0){
 									//sendToClient(playerStack.get(i), "Which dice toll would you like to apply the missile to?");
 									//missletemp = getClientResponse();
 									System.out.println("Which dice roll would you like to apply the missle to? first or second(first being the lowest)");
 									missleTemp = inScan.next();
 									if(missleTemp.equals("first")){
 										System.out.println("Changed " + defense1[0] +" to a 6");
 										defense1[0] = 6;
 										missleOnDefender = true;
 										playerStack.get(i).setMissles(playerStack.get(i).getMissles()-1);
 										misslesUsed++;
 										numDefDice--;
 									} else if(missleTemp.equals("second")){
 										System.out.println("Changed " + defense1[1] +" to a 6");
 										defense1[1] = 6;
 										playerStack.get(i).setMissles(playerStack.get(i).getMissles()-1);
 										misslesUsed++;
 										numDefDice--;
 									}
 									
 								} else if(missleTemp.equals("f") && numAtkDice > 0){
 									System.out.println("Missle used on attackers dice");
 									attack1[0] = 6;
 									playerStack.get(i).setMissles(playerStack.get(i).getMissles()-1);
 									misslesUsed++;
 									numAtkDice--;
 								}
 							}
 						}
 					}
 					loopUntil--;
 						
 				}
 				if(defCountry.getScarType() == 1 && missleOnDefender == false){
 					defense1[1] -= 1;
 				}
 				if(defCountry.getScarType() == 2 && missleOnDefender == false){
 					if(defense1[1] < 6){
 						defense1[1] += 1;
 					}
 				}
 				
 				if(defCountry.isCityFortified() == true){
 					if(defense1[1] < 6){
 						defense1[1] += 1;
 					}
 					if(defense1[0] < 6){
 						defense1[0] += 1;
 					}
 				}
 				for (int i = 0; i < 2; i++) {
 					System.out.print(defense1[i] + " ");
 				}
 				System.out.println();
 
 				if (attack1[0] > defense1[1]) {
 					defCountry
 							.setTroopQuantity(defCountry.getTroopQuantity() - 1);
 					System.out.println(attack1[0] + " is greater than "
 							+ defense1[1]);
 				} else {
 					atkCountry
 							.setTroopQuantity(atkCountry.getTroopQuantity() - 1);
 					System.out.println(attack1[0] + " is less than or equal "
 							+ defense1[1]);
 				}
 				System.out.println("Attackers troops remaining: "
 						+ atkCountry.getTroopQuantity());
 				System.out.println("Defeners troops remaining: "
 						+ defCountry.getTroopQuantity());
 				switchVal = 0;
 				break;
 			case 2:
 				// 1vs1
 				System.out.println("1v1");
 				int[] attack2 = { randomDice()};
 				Arrays.sort(attack2);
 				int[] defense2 = { randomDice() };
 				Arrays.sort(defense2);
 				numOfDice = 2;
 				System.out.println();
 				System.out.print("attack die from lowest to highest ");
 				System.out.print(attack2[0] + " ");
 				System.out.println();
 				System.out.print("defense die from lowest to highest ");
 				System.out.print(defense2[0] + " ");
 				System.out.println();
 				
 				//Missles
 				inScan = new Scanner(System.in);
 				missleTemp = "";
 				loopUntil = 2;
 				numAtkDice = 1;
 				numDefDice = 1;
 				misslesUsed = 0;
 				missleOnDefender = false;
 				while(loopUntil > 0){
 					for(int i = playerStack.size()-1; i > -1; i--){
 						if(misslesUsed == numOfDice){
 							break;
 						}
 						if(playerStack.get(i).getMissles()  > 0){
 							//sendToClient(playerStack.get(i), "Would you like to use a missile?");
 							//missletemp = getClientResponse();
 							System.out.println(playerStack.get(i).getName()+ "would you like to use a missle? 'yes' or 'no'");
 							missleTemp = inScan.next();
 							if(missleTemp.equals("yes")){
 								//sendToClient(playerStack.get(i), "Attacker or Defender?");
 								//missletemp = getClientResponse();
 								System.out.println("Defender or Attackers Dice? 'd' or 'f'");
 								missleTemp = inScan.next();
 								if(missleTemp.equals("d") && numDefDice > 0){
 									System.out.println("Missle used on defenders dice");
 									System.out.println("Changed " + defense2[0] +" to a 6");
 									defense2[0] = 6;
 									missleOnDefender = true;
 									playerStack.get(i).setMissles(playerStack.get(i).getMissles()-1);
 									misslesUsed++;
 									numDefDice--;
 								} else if(missleTemp.equals("f") && numAtkDice > 0){
 									System.out.println("Missle used on attackers dice");
 									attack2[0] = 6;
 									playerStack.get(i).setMissles(playerStack.get(i).getMissles()-1);
 									misslesUsed++;
 									numAtkDice--;
 								}
 							}
 						}
 					}
 					loopUntil--;
 						
 				}
 				
 				
 				if(defCountry.getScarType() == 1 && missleOnDefender == false){
 					defense2[0] -= 1;
 				}
 				if(defCountry.getScarType() == 2 && missleOnDefender == false){
 					if(defense2[0] < 6){
 						defense2[0] += 1;
 					}
 				}
 				
 				if(defCountry.isCityFortified() == true){
 					
 					if(defense2[0] < 6){
 						defense2[0] += 1;
 					}
 				}
 				if (attack2[0] > defense2[0]) {
 					defCountry
 							.setTroopQuantity(defCountry.getTroopQuantity() - 1);
 					System.out.println(attack2[0] + " is greater than "
 							+ defense2[0]);
 				} else {
 					atkCountry
 							.setTroopQuantity(atkCountry.getTroopQuantity() - 1);
 					System.out.println(attack2[0] + " is less than or equal "
 							+ defense2[0]);
 				}
 				System.out.println("Attackers troops remaining: "
 						+ atkCountry.getTroopQuantity());
 				System.out.println("Defeners troops remaining: "
 						+ defCountry.getTroopQuantity());
 				switchVal = 0;
 				break;
 			}
 
 			// ignore for now
 			Scanner in = new Scanner(System.in);
 			// isAttacking = false;
 			if (atkCountry.getTroopQuantity() > 1
 					&& defCountry.getTroopQuantity() > 0) {
 				//sendToClient(atkCountry.getOwner(), "Do you wish to continue combat?");
 				//missletemp = getClientResponse();
 				System.out
 						.println("Attacker! Do you wish to continue? type 'yes' to Continue");
 				String temp3 = in.next();
 				// in.close();
 				if (temp3.equals("yes")) {
 					isAttacking = true;
 				} else {
 					isAttacking = false;
 					if (atkCountry.getTroopQuantity() == 1) {
 						System.out.println("Not enough troops");
 					} else {
 						System.out.println("Combat stopped");
 
 					}
 					break;
 				}
 
 				// }
 				// System.out.println("Victory, " + defCountry.getCountryName()
 				// +" is now yours");
 			} if(defCountry.getTroopQuantity() == 0){
 				System.out.println(defCountry.getOwner().getName() + " you have been defeated, " + defCountry.getCountryName()
 						+ " Now belongs to " + atkCountry.getOwner().getName());
 				atkCountry.getOwner().addCountry(defCountry.id());
 				atkCountry.getOwner().addConquered(defCountry.id());
 			}
 
 		}
 
 	}
 	public void drawEventCard(){
 		/*
 		 * x3 Event CardWhomever has the largest population EITHER recieves 3 troops that can be placed into
 		 * one or more citty terrotories he controls OR he may choose a new mission from the mission
 		 * deck to replace the current one
 		 */
 	}
 
 	public void drawMissionCard() {
 
 		if (newMissionCard == true) {
 			Random generator = new Random();
 			if (missionAvail.isEmpty() != true) {
 				missionInt = generator.nextInt(missionAvail.size());
 				missionInt = missionAvail.get(missionInt);
 				newMissionCard = false;
 			} else {
 				missionInt = -1;
 			}
 		}
 		/*
 		 * SUPERIOR INFRASTRUCTURE: Control6+ Cities REIGN OF TERROR: Conquer 9+
 		 * territories this turn URBAN ASSAULT: Conquer 4+ Cities this turn
 		 * AMPHIBIOUS ONSLAUGHT: Conquer 4+ territories over sea lines this turn
 		 * UNEXPECTED ATTACK: Conquer all the territories in one continent this
 		 * turn IMPERIAL MIGHT: Have a current total continent bonus of 7+
 		 */
 		switch (missionInt) {
 		case -1:
 			break;
 
 		case 0:
 			System.out.println("Active mission Card 0");
 			for (int i = 0; i < playerStack.size(); i++) {
 				if (checkSuperior(playerStack.get(i)) == true) {
 					missionAvail.remove(missionAvail.indexOf(0));
 					newMissionCard = true;
 					playerStack.get(i).setRedstar(
 							playerStack.get(i).getRedstar() + 1);
 					System.out
 							.println("SUPERIOR INFRASTRUCTURE: Control6+ Cities");
 					System.out.println(playerStack.get(i).getName()
 							+ " was awarded a redstar");
 					break;
 				}
 			}
 			break;
 
 		case 1:
 			System.out.println("Active mission Card 1");
 			for (int i = 0; i < playerStack.size(); i++) {
 				if (checkReign(playerStack.get(i)) == true) {
 					missionAvail.remove(missionAvail.indexOf(1));
 					newMissionCard = true;
 					playerStack.get(i).setRedstar(
 							playerStack.get(i).getRedstar() + 1);
 					System.out
 							.println("REIGN OF TERROR: Conquer 9+ territories this turn");
 					System.out.println(playerStack.get(i).getName()
 							+ " was awarded a redstar");
 					break;
 				}
 			}
 			break;
 
 		case 2:
 			System.out.println("Active mission Card 2");
 			for (int i = 0; i < playerStack.size(); i++) {
 				if (checkUrban(playerStack.get(i)) == true) {
 					missionAvail.remove(missionAvail.indexOf(2));
 					newMissionCard = true;
 					playerStack.get(i).setRedstar(
 							playerStack.get(i).getRedstar() + 1);
 					System.out
 							.println("URBAN ASSAULT: Conquer 4+ Cities this turn");
 					System.out.println(playerStack.get(i).getName()
 							+ " was awarded a redstar");
 					break;
 				}
 			}
 			break;
 
 		case 3:
 			System.out.println("Active mission Card 3");
 			for (int i = 0; i < playerStack.size(); i++) {
 				if (checkAmphib(playerStack.get(i)) == true) {
 					missionAvail.remove(missionAvail.indexOf(3));
 					newMissionCard = true;
 					playerStack.get(i).setRedstar(
 							playerStack.get(i).getRedstar() + 1);
 					System.out
 							.println("AMPHIBIOUS ONSLAUGHT: Conquer 4+ territories over sea lines this turn");
 					System.out.println(playerStack.get(i).getName()
 							+ " was awarded a redstar");
 					break;
 				}
 			}
 			break;
 
 		case 4:
 			System.out.println("Active mission Card 4");
 			for (int i = 0; i < playerStack.size(); i++) {
 				if (checkUnexpectedA(playerStack.get(i)) == true) {
 					missionAvail.remove(missionAvail.indexOf(4));
 					newMissionCard = true;
 					playerStack.get(i).setRedstar(
 							playerStack.get(i).getRedstar() + 1);
 					System.out
 							.println("UNEXPECTED ATTACK: Conquer all the territories in one continent this turn");
 					System.out.println(playerStack.get(i).getName()
 							+ " was awarded a redstar");
 					break;
 				}
 			}
 			break;
 
 		case 5:
 			System.out.println("Active mission Card 5");
 			for (int i = 0; i < playerStack.size(); i++) {
 				if (checkImpMight(playerStack.get(i)) == true) {
 					missionAvail.remove(missionAvail.indexOf(5));
 					newMissionCard = true;
 					playerStack.get(i).setRedstar(
 							playerStack.get(i).getRedstar() + 1);
 					System.out
 							.println("IMPERIAL MIGHT: Have a current total continent bonus of 7+");
 					System.out.println(playerStack.get(i).getName()
 							+ " was awarded a redstar");
 					break;
 				}
 			}
 			break;
 
 		}
 	}
 
 	/*
 	 * Solberg Checks the conditions for various mission cards an whether they
 	 * have been met or not
 	 */
 	public boolean checkSuperior(player aPlayer) {
 		boolean val = false;
 		int count = 0;
 		ArrayList<Integer> countryList = aPlayer.getCountrys();
 		for (int i = 0; i < countryList.size(); i++) {
 			if (world2.getCountry(countryList.get(i)).getCityType() != 0
 					|| world2.getCountry(countryList.get(i)).getCityType() != 4) {
 
 				count++;
 			}
 		}
 		if (count >= 6) {
 			val = true;
 		}
 		return val;
 
 	}
 
 	public boolean checkReign(player aPlayer) {
 		boolean val = false;
 		ArrayList<Integer> countryList = aPlayer.getConquered();
 		// reset list to empty at the end of a turn
 		if (countryList.size() >= 9) {
 			val = true;
 		}
 
 		return val;
 	}
 
 	public boolean checkUrban(player aPlayer) {
 		boolean val = false;
 		int count = 0;
 		ArrayList<Integer> countryList = aPlayer.getConquered();
 		for (int i = 0; i < countryList.size(); i++) {
 			if (world2.getCountry(countryList.get(i)).getCityType() != 0) {
 				count++;
 			}
 		}
 		if (count >= 4) {
 			val = true;
 		}
 		return val;
 	}
 
 	public boolean checkAmphib(player aPlayer) {
 		boolean val = false;
 		// AMPHIBIOUS ONSLAUGHT: Conquer 4+ territories over sea lines this turn
 		return val;
 	}
 
 	public boolean checkUnexpectedA(player aPlayer) {
 		boolean val = false;
 		ArrayList<Integer> countryList = new ArrayList<Integer>();
 		for (int i = 0; i < aPlayer.getConquered().size(); i++) {
 			countryList.add(aPlayer.getConquered().get(i));
 		}
 
 		if (countryList.containsAll(world2.getContinent(0).getCountries())) {
 			val = true;
 		}
 		if (countryList.containsAll(world2.getContinent(1).getCountries())) {
 			val = true;
 		}
 		if (countryList.containsAll(world2.getContinent(2).getCountries())) {
 			val = true;
 		}
 		if (countryList.containsAll(world2.getContinent(3).getCountries())) {
 			val = true;
 		}
 		if (countryList.containsAll(world2.getContinent(4).getCountries())) {
 			val = true;
 		}
 		if (countryList.containsAll(world2.getContinent(5).getCountries())) {
 			val = true;
 		}
 		// UNEXPECTED ATTACK: Conquer all the territories in one continent this
 		// turn
 		return val;
 	}
 
 	public boolean checkImpMight(player aPlayer) {
 
 		boolean val = false;
 		ArrayList<Integer> countryList = aPlayer.getConquered();
 		int bonus = 0;
 		if (countryList.containsAll(world2.getContinent(0).getCountries()) == true) {
 			bonus += world2.getContinent(0).getBonus();
 			System.out.println("0America");
 		}
 		if (countryList.containsAll(world2.getContinent(1).getCountries()) == true) {
 			bonus += world2.getContinent(1).getBonus();
 			System.out.println("1SouthAmerica");
 		}
 		if (countryList.containsAll(world2.getContinent(2).getCountries()) == true) {
 			bonus += world2.getContinent(2).getBonus();
 			System.out.println("2");
 		}
 		if (countryList.containsAll(world2.getContinent(3).getCountries()) == true) {
 			bonus += world2.getContinent(3).getBonus();
 			System.out.println("3");
 		}
 		if (countryList.containsAll(world2.getContinent(4).getCountries()) == true) {
 			bonus += world2.getContinent(4).getBonus();
 			System.out.println("4");
 		}
 		if (countryList.containsAll(world2.getContinent(5).getCountries()) == true) {
 			bonus += world2.getContinent(5).getBonus();
 			System.out.println("5");
 		}
 
 		if (bonus >= 7) {
 			System.out.println(bonus);
 			val = true;
 		}
 		// IMPERIAL MIGHT: Have a current total continent bonus of 7+
 		return val;
 	}
 
 	public static int randomDice() {
 		int dice;
 		Random generator = new Random();
 		dice = generator.nextInt(6) + 1;
 		return dice;
 	}
 		
 	String data = "6" + "\n" + 
 
 	"NorthAmerica 	title NONE 0 8	 0 1 2 3 4 5 6 7 8" + "\n" +
 	"SouthAmerica 	title NONE 0 4	 9 10 11 12" + "\n" +
 	"Africa 		title NONE 0 3	 13 14 15 16 17 18 19" + "\n" +
 	"Europe 	 	title NONE 0 5	 20 21 22 23 24 25" + "\n" +
 	"Asia 			title NONE 0 9	 26 27 28 29 30 31 32 33 34 35 36 37" + "\n" +
 	"Australia 		title NONE 0 2	 38 39 40 41" + "\n" +
 
 	"0Alaska 				\\NONE 0 false 0 faction 0 \\NONE 0 false  34 1 5 " + "\n" +
 	"1NorthwestTerritory 	\\NONE 0 false 0 faction 0 \\NONE 0 false  0 2 4 5" + "\n" +
 	"2GreenLand 			\\NONE 0 false 0 faction 0 \\NONE 0 false  1 4 3 22" + "\n" +
 	"3EasternCanada 		\\NONE 0 false 0 faction 0 \\NONE 0 false  2 4 7" + "\n" +
 	"4Ontario 				\\NONE 0 false 0 faction 0 \\NONE 0 false  1 5 6 7 3 2" + "\n" +
 	"5Alberta 				\\NONE 0 false 0 faction 0 \\NONE 0 false  1 0 6 3" + "\n" +
 	"6WesternUS 			\\NONE 0 false 0 faction 0 \\NONE 0 false  5 4 8 7" + "\n" +
 	"7EasternUS 			\\NONE 0 false 0 faction 0 \\NONE 0 false  3 4 6 8" + "\n" +
 	"8CentralAmerica 		\\NONE 0 false 0 faction 0 \\NONE 0 false  7 6 9" + "\n" +
 	"9Venezuala				\\NONE 0 false 0 faction 0 \\NONE 0 false  8 12 10" + "\n" +
 	"10Peru 				\\NONE 0 false 0 faction 0 \\NONE 0 false  9 11 12" + "\n" +
 	"11Argentina 			\\NONE 0 false 0 faction 0 \\NONE 0 false  10 12" + "\n" +
 	"12Brazil 				\\NONE 0 true 0 faction 50 \\NONE 50 false  9 10 11 13" + "\n" +
 	"13NorthAfrica 			\\NONE 0 true 0 faction 50 \\NONE 50 false  12 14 17 18 19 20" + "\n" +
 	"14CentralAfrica 		\\NONE 0 true 0 faction 50 \\NONE 50 false  13 15 17" + "\n" +
 	"15SouthAfrica 			\\NONE 0 true 0 faction 50 \\NONE 50 false  16 14 17" + "\n" +
 	"16Madagascar 			\\NONE 0 true 0 faction 50 \\NONE 50 false  15 17" + "\n" +
 	"17EastAfrica 			\\NONE 0 true 0 faction 50 \\NONE 50 false  16 15 14 13 18 28" + "\n" +
 	"18Egypt 				\\NONE 0 true 0 faction 50 \\NONE 50 false  19 13 17 28" + "\n" +
 	"19SouthernEurope 		\\NONE 0 true 0 faction 50 \\NONE 50 false  13 18 28 25 24 20" + "\n" +
 	"20WesternEurope 		\\NONE 0 true 0 faction 50 \\NONE 50 false  21 24 19 13" + "\n" +
 	"21GreatBritain 		\\NONE 0 true 0 faction 50 \\NONE 50 false 22 23 24 20" + "\n" +
 	"22IceLand 				\\NONE 0 true 0 faction 50 \\NONE 50 false  2 21 23" + "\n" +
 	"23Scandinavia 			\\NONE 0 true 0 faction 50 \\NONE 50 false  22 21 24 25" + "\n" +
 	"24NorthernEurope 		\\NONE 0 true 0 faction 50 \\NONE 50 false  21 23 20 19 25" + "\n" +
 	"25Russia 				\\NONE 0 true 0 faction 50 \\NONE 50 false  23 24 19 28 27 26" + "\n" +
 	"26Ural 				\\NONE 0 true 0 faction 50 \\NONE 50 false  25 32 31 27" + "\n" +
 	"27Afghanistan 			\\NONE 0 true 0 faction 50 \\NONE 50 false  25 26 31 29 28" + "\n" +
 	"28MiddleEast 			\\NONE 0 true 0 faction 50 \\NONE 50 false  25 19 18 17 29 27" + "\n" +
 	"29India 				\\NONE 0 true 0 faction 50 \\NONE 50 false  28 27 31 30" + "\n" +
 	"30SoutheastAsia		\\NONE 0 true 0 faction 50 \\NONE 50 false  39 31 29" + "\n" +
 	"31China 				\\NONE 0 true 0 faction 50 \\NONE 50 false  30 29 27 26 32 36" + "\n" +
 	"32Siberia 				\\NONE 0 true 0 faction 50 \\NONE 50 false  26 31 36 37 33" + "\n" +
 	"33Yakutsk 				\\NONE 0 true 0 faction 50 \\NONE 50 false  34 37 32" + "\n" +
 	"34Kamchatka 			\\NONE 0 true 0 faction 50 \\NONE 50 false  0 35 33 37 36" + "\n" +
 	"35Japan 				\\NONE 0 true 0 faction 50 \\NONE 50 false  36 34" + "\n" +
 	"36Mongolia 			\\NONE 0 true 0 faction 50 \\NONE 50 false  35 34 37 32 31" + "\n" +
 	"37Irkutsk 				\\NONE 0 true 0 faction 50 \\NONE 50 false 33 32 36 34" + "\n" +
 	"38NewGuinea 			\\NONE 0 true 0 faction 50 \\NONE 50 false  41 40 39" + "\n" +
 	"39Indonesia 			\\NONE 0 true 0 faction 50 \\NONE 50 false  30 38 40" + "\n" +
 	"40WesternAustralia 	\\NONE 0 true 0 faction 50 \\NONE 50 false  41 38 39" + "\n" +
 	"41EasternAustralia 	\\NONE 0 true 0 faction 50 \\NONE 50 false 38 40";
 	Map world2 = new Map(data);
 }
