 package controller;
 
 import gameplay.Logic;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 
 import player.AI;
 import player.Player;
 import terrain.Tile;
 import units.Store;
 import units.Unit;
 
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
 			secondPlayer = new AI(secondPlayer.getPName(), Integer.valueOf(1), secondPlayer.getFact());
 		}
 
 		log = new Logic (mapName, firstPlayer.getFact(), secondPlayer.getFact(), p1.getPName(),
 				secondPlayer.getPName(), aiOn);
 
 		//playerTurn = whoGoesFirst();	
 		//player one always goes first!!!!!!!!!!!1
 		playerTurn = 0; 
 	}
 
 	public void aiTurn() {
 		((AI) secondPlayer).getLogic(log);
 		((AI) secondPlayer).startTurn();
 		log.setUB(((AI) secondPlayer).getNewUBoard());
 		log.setTBoard(((AI) secondPlayer).getNewTBoard());
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
 
 		if (log.getUB()[x][y] != null && log.getUB()[x][y].getOwner() == playerTurn)
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
			log.econDay(log.getP2());
 			log.unitNewTurn(playerTurn);
 			playerTurn = 1;
 		}
 		else {
			log.econDay(log.getP1());
 			log.unitNewTurn(playerTurn);
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
 		char[][] attackGrid = new char[log.getSize()][log.getSize()];
 		Unit attacker = log.getUnit(x, y);
 		int attackRange = attacker.getAtkRange();
 
 		for (int r = 0; r < log.getSize(); r++) 
 			for (int c = 0; c < log.getSize(); c++) 
 				attackGrid[r][c] = '-';
 
 		for (int i = 1; i <= attackRange; i++) {
 			//To the right
 			if (x+i < log.getSize() && (attackRange == 1 || i != 1))  
 				attackGrid[x+i][y] = 'x';
 
 			/*Second part of the if just says if the attack range is 1 
 			or if it is not the first time around, meaning it's ranged,
 			then put a x there.*/
 
 			//To the left
 			if (x-i >= 0 && (attackRange == 1 || i != 1)) 
 				attackGrid[x-i][y] = 'x';
 
 			//Up
 			if (y+i < log.getSize() && (attackRange == 1 || i != 1))
 				attackGrid[x][y+i] = 'x';
 
 			//Down
 			if (y-i >= 0 && (attackRange == 1 || i != 1))
 				attackGrid[x][y-i] = 'x';
 
 			if (attackRange > 1 && attacker.getHasMoved() == false) {
 				//Top right
 				if (x+i < log.getSize() && y+i < log.getSize()) 
 					attackGrid[x+i][y+i] = 'x';
 
 				//Bottom right
 				if (x+i < log.getSize() && y-i > 0) 
 					attackGrid[x+i][y-i] = 'x';
 
 				//Top left
 				if (x-i > 0 && y+i < log.getSize()) 
 					attackGrid[x-i][y+i] = 'x';
 
 				//Bottom left
 				if (x-i > 0 && y-i > 0) 
 					attackGrid[x-i][y-i] = 'x';
 			}  
 		}
 
 		return attackGrid;
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
 		else if (playerTurn == 1)
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
 					if (!(possibleMoves[r][c] == '-') && !selUnit.getHasMoved() && 
 							selUnit.getOwner() == playerTurn) {
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
 		boolean canIAttack = false;
 
 		for (int i = 1; i <= selUnit.getAtkRange(); i++) {
 			if (selUnit.getAtkRange() == 1 || 
 					i > 1) { 
 				//check up
 				if (x + i < unitBoard.length && unitBoard[x + i][y] != null) {
 					Unit otherUnit = unitBoard[x + i][y];
 					if (otherUnit.getOwner() != selUnit.getOwner() && !selUnit.getHasAttacked())
 						canIAttack = true;
 				}
 
 				//check the right one
 				if (y + i < unitBoard.length && unitBoard[x][y + i] != null) {
 					Unit otherUnit = unitBoard[x][y + i];
 					if (otherUnit.getOwner() != selUnit.getOwner() && !selUnit.getHasAttacked()) 
 						canIAttack = true;
 				}
 
 				//check the left
 				if (y - i >= 0 && unitBoard[x][y - i] != null) {
 					Unit otherUnit = unitBoard[x][y - i];
 					if (otherUnit.getOwner() != selUnit.getOwner() && !selUnit.getHasAttacked()) 
 						canIAttack = true;						
 				}
 
 				//check the bottom
 				if (x - i >= 0  && unitBoard[x - i][y] != null) {
 					Unit otherUnit = unitBoard[x-i][y];
 					if (otherUnit.getOwner() != selUnit.getOwner() && !selUnit.getHasAttacked()) 
 						canIAttack = true;							
 				}
 			} else {
 				int oC = i - 1;
 
 				//lower left corner
 				if (x - oC > 0 && y - oC > 0 && unitBoard[x - oC][y - oC] != null) {
 					Unit otherUnit = unitBoard[x - oC][y - oC];
 					if (otherUnit.getOwner() != selUnit.getOwner() && !selUnit.getHasAttacked())
 						canIAttack = true;
 				}
 
 				//upper left corner
 				if (x + oC < log.getSize() && y - oC > 0 && unitBoard[x + oC][y - oC] != null) {
 					Unit otherUnit = unitBoard[x + oC][y - oC];
 					if (otherUnit.getOwner() != selUnit.getOwner() && !selUnit.getHasAttacked())
 						canIAttack = true;
 				}
 
 				//bottom right corner
 				if (x - oC > 0 && y + oC < log.getSize() && unitBoard[x - oC][y + oC] != null) {
 					Unit otherUnit = unitBoard[x - oC][y + oC];
 					if (otherUnit.getOwner() != selUnit.getOwner() && !selUnit.getHasAttacked())
 						canIAttack = true;
 				}
 
 				//top right corner
 				if (x + oC < log.getSize() && y + oC < log.getSize() && unitBoard[x + oC][y + oC] != null) {
 					Unit otherUnit = unitBoard[x + oC][y + oC];
 					if (otherUnit.getOwner() != selUnit.getOwner() && !selUnit.getHasAttacked())
 						canIAttack = true; 
 				}
 			}
 
 
 		}
 
 		if (canIAttack) 
 			actions.add("Attack");
 
 		Tile[][] tileBoard = log.getTBoard();
 
 		if ((tileBoard[x][y].getType() == 'q' || tileBoard[x][y].getType() == 'p' ||
 				tileBoard[x][y].getType() == 'Q' || tileBoard[x][y].getType() == 'P' ||
 				tileBoard[x][y].getType() == 'h' || tileBoard[x][y].getType() == 'H' ||
 				tileBoard[x][y].getType() == 'b' || tileBoard[x][y].getType() == 'x' ||
 				tileBoard[x][y].getType() == 'X') && tileBoard[x][y].getOwner() != selUnit.getOwner() &&
 				unitBoard[x][y].getType() == Unit.INFANTRYTYPE) {	
 
 			actions.add("Capture");
 		}	
 
 		actions.add("UnitInfo");
 
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
 		int own = sUnit.getOwner();
 		int atkRange = sUnit.getAtkRange();
 
 		String nameString = "Name: " + name;		
 		String hpString = "HP: " + hp;
 		String moveString = "Move: " + move;
 		String attackString = "Attack: " + attack;
 		String attkRangeString = "Range: " + atkRange;
 		String armorString = "Armor: " + armor;
 		String ammoString = "Ammo: " + ammo;
 		String ownerString;
 
 		if (own == 0)
 			ownerString = "Owner: Player 1";
 		else
 			ownerString = "Owner: Player 2";
 
 		ArrayList<String> toSend = new ArrayList<String>();
 
 		toSend.add(nameString);
 		toSend.add(hpString);	
 		toSend.add(moveString);
 		toSend.add(attackString);
 		toSend.add(attkRangeString);
 		toSend.add(armorString);
 		toSend.add(ammoString);
 		toSend.add(ownerString);
 
 		return toSend;
 	}
 
 	public int[][] getConvertedUnits(int pNum){
 		Unit[][] uB = log.getUB();
 		int[][] retBoard = new int[log.getSize()][log.getSize()];
 
 		for(int i = 0; i < log.getSize(); i++)
 			for(int j = 0; j < log.getSize(); j++){
 				if(uB[i][j] == null)    {
 					retBoard[i][j] = -1;
 					//continue;
 				} else {
 					String name = uB[i][j].getName();
 
 					if (uB[i][j].getOwner() == pNum) {
 						if(name.equalsIgnoreCase("Anti-Air"))
 							retBoard[i][j] = 0;
 						else if(name.equalsIgnoreCase("Artillery"))
 							retBoard[i][j] = 1;
 						else if(name.equalsIgnoreCase("HeavyTank"))
 							retBoard[i][j] = 2;
 						else if(name.equalsIgnoreCase("Infantry"))
 							retBoard[i][j] = 3;
 						else if(name.equalsIgnoreCase("Mech"))
 							retBoard[i][j] = 4;
 						else if(name.equalsIgnoreCase("Medium Tank"))
 							retBoard[i][j] = 5;
 						else if(name.equalsIgnoreCase("Recon"))
 							retBoard[i][j] = 6;
 						else if(name.equalsIgnoreCase("Rocket"))
 							retBoard[i][j] = 7;
 						else if(name.equalsIgnoreCase("Tank"))
 							retBoard[i][j] = 8;
 					} else
 						retBoard[i][j] = -1;
 				}
 
 			}
 
 
 		return retBoard;
 	}
 
 	public void unitMove(int moveX, int moveY) {
 		Unit toMove = log.getUnit(x, y);
 		log.moveUnit(toMove, moveX, moveY);
 	}
 
 	public boolean isValidMove(int moveX, int moveY) {
 		char[][] posMoves = log.getMoves(log.getUnit(x,y));
 		if (posMoves[moveX][moveY] == 'x') 
 			return true;
 
 		return false;
 	}
 	
 	public boolean isValidAttk(int atkX, int atkY) {
 		char[][] posAtk = attack();
 		if (posAtk[atkX][atkY] == 'x')
 			return true;
 		return false;
 	}
 
 	public int getCurrentPlayerMoney()
 	{
 		int munny = 0;
 
 		if (playerTurn == firstPlayer.getPNum())
 			munny = log.getP1().getCash();
 
 		else if (playerTurn == secondPlayer.getPNum())
 			munny = log.getP2().getCash();
 
 		else
 			munny = -1;
 
 
 		return munny;
 	}
 }
