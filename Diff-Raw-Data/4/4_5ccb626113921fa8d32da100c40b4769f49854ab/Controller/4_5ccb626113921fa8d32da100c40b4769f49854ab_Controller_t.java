 package controller;
 
 import java.io.Serializable;
 import java.util.Random;
 import java.util.ArrayList;
 import gameplay.Logic;
 import player.*;
 import units.*;
 import terrain.Tile;
 
 public class Controller implements Serializable
 {
 
 	private final int MOVE = 0;
 	private final int ATTACK = 1;
 	private final int CAPTURE = 2;
 
 	Player firstPlayer;
 	Player secondPlayer;
 	boolean aiOn;
 	Logic log;
 	int playerTurn;
 	Store store;
 	int x, y;
 
 	char selected = 'n';
 
 	public Controller(Player p1, Player p2, boolean isAIOn, String mapName) {
 		firstPlayer = p1;
 		secondPlayer = p2;
 		aiOn = isAIOn;
 		store = new Store();
 
 		if (aiOn) {
 			secondPlayer = new AI(secondPlayer.getPName(), 2, secondPlayer.getFact());
 		}
 
 		log = new Logic (mapName, firstPlayer.getFact(), secondPlayer.getFact(), p1.getPName(),
 				secondPlayer.getPName(), aiOn);
 
		//playerTurn = whoGoesFirst();	
		//player one always goes first!!!!!!!!!!!1
		playerTurn = 0; 
 	}
 	
 	public void aiTurn() {
 		(AI) secondPlayer.getLogic(log);
 		(AI) secondPlayer.statTurn();
 		log.setUB((AI)secondPlayer.getNewUBoard());
 		log.setTBoard((AI)secondPlayer.getNewTBoard());
 		endTurn();
 	}
 	
 	/**
 	 * This is a simple method that simply tells the GUI whos turn it is. 
 	 * 
 	 * @return
 	 */
 	
 	public int whosTurn() {
 		return playerTurn;
 	}
 
 	/**
 	 * You chose a tile and the controller will check to see if its a unit first if it is not a
 	 * unit then it will check to see if it's a production building. If it is not a production 
 	 * building then it will say there are no options for that particular tile. 
 	 * 
 	 * @param x
 	 * @param y
 	 * @return
 	 */
 	
 	public ArrayList<String> selectCoordinates(int x, int y) {
 		ArrayList<String> actions = new ArrayList<String>();
 
 		this.x = x;
 		this.y = y;
 
 		if (log.getUB()[x][y] != null)
 			actions = unitActions(actions);
 
 		else if (log.getTBoard()[x][y].getType() == 'q' && playerTurn == 0)
 			actions = produceActions(actions);
 		else if (log.getTBoard()[x][y].getType() == 'Q' && playerTurn == 1) 
 			actions = produceActions(actions); 
 
 		else
 			return null;
 
 		return actions;
 	}
 
 
 	/**
 
 	 * This method produces all of the actions for the unit producing buildings. At the moment
 	 * this is limited to only ground forces. 
 	 * 
 	 * @param pActions
 	 * @return
 	 */
 	
 	private ArrayList<String> produceActions(ArrayList<String> pActions) {
 		Tile[][] board = log.getTBoard();
 		ArrayList<String> actions = new ArrayList<String>();
 
 		Tile tempTile = board[x][y];
 		if(tempTile.getType() == 'p' || tempTile.getType() == 'q' || tempTile.getType() == 'Q'){
 			if(tempTile.getHasProduced() == false){
 				if(tempTile.getOwner() == playerTurn){
 					actions.add("Build Unit");
 					selected = 'p';
 				}
 			}
 		}
 
 		return prodUnit();
 	}
 
 	/**
 	 * This method is called at the end of a turn to update money. 
 	 */
 	
 	public void endTurn() {
 		if (playerTurn == 0) {
 			log.econDay(secondPlayer);
 			playerTurn = 1;
 		}
 		else {
 			log.econDay(firstPlayer);
 			playerTurn = 0;
 		}
 	}
 
 	public char[][] unitTakeAction(int action)
 	{
 		char[][] canDo = null;
 		
 		if (action == MOVE) {
 			canDo = move();
 		} else if (action == ATTACK) {
 			canDo = attack();
 		} else if (action == CAPTURE) {
 			capture();
 		} else {
 			return null;
 		}
 		return canDo;
 	}
 
 	private char[][] attack() {
 		char[][] move = move();
 		Unit[][] ub = log.getUB();
 		char[][] canAttack = new char[log.getTBoard().length][log.getTBoard().length];
 		
 		for (int r = 0; r < canAttack.length; r++) {
 			for (int c = 0; c < canAttack.length; c++) {
 				
 				//if there is a unit there that does not belong to you and you can move there...
 				//TODO move[r][c] isn't correct you should say if you can move within attack range
 				//of that unit NOT if you can move to the same tile as that unit!
 				
 				if (ub[r][c] != null && playerTurn != ub[r][c].getOwner() && move[r][c] == 'x') {
 					canAttack[r][c] = 'a';
 				}
 			}
 		}
 		
 		return canAttack;
 	}
 	
 	public void attackUnit(int r, int c) {
 		Unit toAttack = log.getUnit(r, c);
 		Unit attacker = log.getUnit(x, y);
 		
 		if (playerTurn == 0) 
 			log.battle(attacker, toAttack, playerTurn);
 		else
 			log.battle(toAttack, attacker, playerTurn);
 		
 	}
 
 	public void capture() {
 		if (playerTurn == 1)
 			log.captureBuilding(log.getP1(), x, y);
 		else
 			log.captureBuilding(log.getP2(), x, y);
 	}
 
 //	/**
 //	 * Returns x on a char board of what buildings are in reach to capture
 //	 *
 //	 * @return
 //	 */
 //	private char[][] capture() {
 //		char[][] moves = move();
 //		Tile[][] tiles = log.getTBoard();
 //
 //		char[][] canCapture = new char[tiles.length][tiles.length];
 //
 //		for (int r = 0; r < tiles.length; r++) {
 //			for (int c = 0; c < tiles.length;c++) {
 //				canCapture[r][c] = '-';
 //			}
 //		}
 //
 //		for (int r = 0; r < tiles.length; r++) {
 //			for (int c = 0; c < tiles.length; c++) {
 //				if (playerTurn == 0) {
 //					if (tiles[r][c].getType() == 'H' || tiles[r][c].getType() == 'b' ||
 //							tiles[r][c].getType() == 'p' || tiles[r][c].getType() == 'Q' ||
 //							tiles[r][c].getType() == 'X') {
 //						if (moves[r][c] == 'x') //legal move
 //							canCapture[r][c] = 'c';
 //					}
 //				} else {
 //					if (tiles[r][c].getType() == 'h' || tiles[r][c].getType() == 'b' ||
 //							tiles[r][c].getType() == 'p' || tiles[r][c].getType() == 'q' ||
 //							tiles[r][c].getType() == 'x') {
 //						if (moves[r][c] == 'x') //legal move
 //							canCapture[r][c] = 'c';
 //					}
 //				}
 //			}
 //		}
 //
 //		return canCapture;
 //	}
 
 	//so the player decides to produce a unit, so we call this method and we send back an array
 	//of strings so that the GUI can display array of strings in a menu as possible buys. 
 	//then the GUI will send back a String or something that will tell us which unit to 
 	//produce.
 	public ArrayList<String> prodUnit() {
 		ArrayList<String> toProduce = new ArrayList<String>();
 
 		int munny = 0; 
 		if (playerTurn == 0) {
 			munny = log.getP1().getCash();
 		} else {
 			munny = log.getP2().getCash();
 		}
 
 		ArrayList<Unit> canBuild = new ArrayList<Unit>();
 		canBuild = store.buyGroundUnit(munny);
 
 		for (int i = 0; i < canBuild.size(); i++) {
 			toProduce.add(canBuild.get(i).getName());
 		}
 
 		return toProduce;
 	}
 
 	/**
 	 * Last part of the producing unit method. Pretty much this is called by the GUI once it knows
 	 * what unit it wants to make. 
 	 * 
 	 * @param toProd
 	 */
 	
 	public void produceUnit(String toProd) {
 		Tile tile = log.getTile(x, y);
 		Unit prod = store.whatUnit(toProd);
 
 		if (playerTurn == 0) 
 			log.produceUnit(log.getP1(), prod, tile);
 		else 
 			log.produceUnit(log.getP2(), prod, tile);
 	}
 
 	/**
 	 * This gives you the actions in a string ArrayList that are for the 
 	 * 
 	 * @param actions
 	 * @return
 	 */
 	
 	private ArrayList<String> unitActions(ArrayList<String> actions) {
 		Unit selUnit = log.getUnit(x, y);
 		Logic tempLog = log;
 		char[][] possibleMoves = tempLog.getMoves(selUnit);
 		boolean canMove = false;
 
 		for (int c = 0; c < possibleMoves.length; c++) {
 			for(int r = 0; r < possibleMoves.length; r++) {
 				if (canMove != true) {
 					if (!(possibleMoves[r][c] == '-') && !selUnit.getHasMoved()) {
 						actions.add("Move");
 						canMove = true;
 						selected = 'u';
 					}
 				} else
 					break;
 			}
 			if (canMove == true) 
 				break;
 		}
 
 		Unit[][] unitBoard = log.getUB();
 
 		for (int c = 0; c < unitBoard.length && c < selUnit.getAtkRange(); c++) {
 			for (int r = 0; r < unitBoard.length; r++) {
 				if (unitBoard[r][c] != null) {
 					Unit otherUnit = unitBoard[r][c];
 					if (otherUnit.getOwner() != playerTurn && !selUnit.getHasAttacked()) {
 						actions.add("Attack");
 					}
 				}
 			}
 		}
 
 		Tile[][] tileBoard = log.getTBoard();
 
 		if (playerTurn == 0) {
 			if (tileBoard[x][y].getType() == 'q' || tileBoard[x][y].getType() == 'p') {
 				actions.add("Capture");
 			}
 		} else {
 			if (tileBoard[x][y].getType() == 'Q' || tileBoard[x][y].getType() == 'P') {
 
 				actions.add("Capture");
 			}
 		}
 
 		return actions;
 	}
 
 	public int getMunny(int pNum) {
 		int munny = 0;
 		
 		if (pNum == 0) 
 			munny = log.getP1().getCash();
 		else
 			munny = log.getP2().getCash();
 		
 		return munny;
 	}
 	
 	/**
 	 * Randomly decides who gets to go first.
 	 * 
 	 * @return
 	 */
 	private int whoGoesFirst() {
 		Random rand = new Random();
 		int answer = 0;
 
 		answer = rand.nextInt(10);
 		answer = answer % 2;
 
 		return answer;
 	}
 	
 	/**
 	 * I really think this method should be called when they click on a unit in the first
 	 * place as with the game.   So if they were to click on a unit, all possible moves that
 	 * unit can take are highlighted on screen before they choose to move
 	 * @return
 	 */
 	private char[][] move(){
 		Unit[][] temp = log.getUB();
 		return log.getMoves(temp[x][y]);
 	}
 	
 	public char[][] getBoard()
 	{
 		int size = log.getTBoard().length;
 		Tile[][] tiles = log.getTBoard();
 		char[][] tileBoard = new char[size][size];
 		
 		for(int i = 0; i < size; i++)
 		{
 			for(int j = 0; j < size; j++)
 			{
 				tileBoard[i][j] = tiles[i][j].getType();
 			}
 		}
 		
 		return tileBoard;
 	}
 	
 	public Unit[][] getUnitBoard()
 	{
 		return log.getUB();
 	}
 
 	public ArrayList<String> getUnitInfo() {
 		Unit sUnit = log.getUnit(x, y);
 		int hp = sUnit.getHP();
 		int move = sUnit.getMove();
 		int armor = sUnit.getArmor();
 		int ammo = sUnit.getAmmo();
 		String name = sUnit.getName();
 		int attack = sUnit.getAttack();
 		int fuel = sUnit.getFuel();
 		int atkRange = sUnit.getAtkRange();
 
 		String nameString = "Name: " + name;		
 		String hpString = "HP: " + hp;
 		String moveString = "Move: " + move;
 		String attackString = "Attack: " + attack;
 		String attkRangeString = "Range: " + atkRange;
 		String armorString = "Armor: " + armor;
 		String ammoString = "Ammo: " + ammo;
 		String fuelString = "Fuel: " + fuel;
 		
 		ArrayList<String> toSend = new ArrayList<String>();
 
 		toSend.add(nameString);
 		toSend.add(hpString);	
 		toSend.add(moveString);
 		toSend.add(attackString);
 		toSend.add(attkRangeString);
 		toSend.add(armorString);
 		toSend.add(ammoString);
 		toSend.add(fuelString);
 
 		return toSend;
 	}
 		
 	public int[][] getConvertedUnits(int pNum){
 		Unit[][] uB = log.getUB();
 		int[][] retBoard = new int[log.getSize()][log.getSize()];
 		
 		for(int i = 0; i < log.getSize(); i++)
 			for(int j = 0; j < log.getSize(); j++){
                 if(uB[i][j] == null)    {
                     retBoard[i][j] = -1;
                     continue;
                 }
 				String name = uB[i][j].getName();
 				
 				if (uB[i][j].getOwner() == pNum) {
 					if(name.equals("Anti-Air"))
 						retBoard[i][j] = 0;
 					else if(name.equals("Artillery"))
 						retBoard[i][j] = 1;
 					else if(name.equals("HeavyTank"))
 						retBoard[i][j] = 2;
 					else if(name.equals("Infantry"))
 						retBoard[i][j] = 3;
 					else if(name.equals("Mech"))
 						retBoard[i][j] = 4;
 					else if(name.equals("Medium Tank"))
 						retBoard[i][j] = 5;
 					else if(name.equals("Recon"))
 						retBoard[i][j] = 6;
 					else if(name.equals("Rocket"))
 						retBoard[i][j] = 7;
 					else if(name.equals("Tank"))
 						retBoard[i][j] = 8;
 				}
 			}
 		
 		
 		return retBoard;
 	}
 }
