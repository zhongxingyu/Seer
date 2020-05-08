 package edu.aueb.cs.uw.core;
 
 public class GameEngine {
 	
 	private GameConfigs gc;
 	private TilePool tp;
 	private Player[] players;
 	private Board board;
 	private int playerTurn;
 	private int gaveUpTurn;
 	
 	
 	public GameEngine(GameConfigs gc){
 		this.gc=gc;
 		tp=new TilePool();
 		players=this.gc.getPlayersList();
 		for(int i=0;i<players.length;i++){
 			players[i].setPlayerID(i);
 			players[i].setPool(tp);
 		}
 		setBoard(new Board());
 		setGaveUpTurn(0);
 	}
 	
 	public void beginGame(){
 		playerTurn=0;
 		for(int i=0;i<players.length;i++)
 			players[i].getTray().fillTray();
 	}
 	
 	public int getNumPlayers()
 	{
 		return gc.getNumPlayers();
 	}
 	
 	public void nextRound(){
 		players[getPlayerTurn()].setScore(players[getPlayerTurn()].getScore()+getBoard().getScore());
 		players[getPlayerTurn()].getTray().fillTray();
 		if(board.haveMadeChanges())
 			gaveUpTurn=0;
		getBoard().endTurn();
 		playerTurn=(getPlayerTurn()+1)%gc.getNumPlayers();
 	}
 	
 	public boolean isEndOfGame(){
 		if(!tp.hasMoreTiles()){
 			if(players[getPlayerTurn()].getTray().getNumUnusedTiles()==0)
 				return true;
 		}
 		if(gaveUpTurn==gc.getNumPlayers())
 			return true;
 		return false;
 	}
 	
 	public Player endGame(){
 		for(int i=0;i<players.length;i++){
 			players[i].setScore(players[i].getScore()-
 						5*players[i].getTray().getNumUnusedTiles());
 		}
 		return declareWinner();
 	}
 	
 	public Player declareWinner(){
 		int highestScore=-36;
 		Player highestPlayer=null;
 		for(int i=0;i<players.length;i++){
 			if(players[i].getScore()>highestScore){
 				highestScore=players[i].getScore();
 				highestPlayer=players[i];
 			} else if(players[i].getScore()==highestScore)
 				highestPlayer=null;
 		}
 		return highestPlayer;
 	}
 	
 	public void giveUpTurn(){
 		undoAll();
 		gaveUpTurn++;
 		if(board.isFirstWord()){
 			nextRound();
 			board.setFirstWord(true);	
		} else
			nextRound();
		
 	}
 	
 	public void makeSwitch(int tilePos){
 		if(switchTile(tilePos)){
 			gaveUpTurn++;
 			nextRound();
 		}
 	}
 	
 	public boolean switchTile(int tilePos){
 		if(board.haveMadeChanges())
 			return false;
 		return players[getPlayerTurn()].getTray().switchTile(tilePos);
 	}
 
 	public void setBoard(Board board) {
 		this.board = board;
 	}
 
 	public Board getBoard() {
 		return board;
 	}
 
 	public void setGaveUpTurn(int gaveUpTurn) {
 		this.gaveUpTurn = gaveUpTurn;
 	}
 
 	public int getGaveUpTurn() {
 		return gaveUpTurn;
 	}
 
 	public Player getPlayer(int playerID){
 		if((playerID+1>players.length)||
 				(playerID<0))
 			return null;
 		return players[playerID];
 	}
 
 	public int getPlayerTurn() {
 		return playerTurn;
 	}
 
 	public void undoAll() {
 		Tile [] trayTiles = board.undoAll();
 		Player p=players[getPlayerTurn()];
 		Tray t= p.getTray();
 		for(int i=0;i<trayTiles.length;i++){
 			t.addTile(trayTiles[i]);
 		}
 	}
 }
