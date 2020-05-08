 package managers;
 
 import com.me.mygdxgame.Mule;
 
 import gameObjects.Map;
 import gameObjects.Player;
 import gameObjects.Resource;
 import gameObjects.Tile;
 
 /**
  * This class will manage the game logic as well as contain the PlayerManager for the entire game.
  * @author antonio
  *
  */
 public class GameManager {
 	
 	public static enum Difficulty {
 		STANDARD, TOURNAMENT 	
 	};
 	
 	public static final int[] FOOD_REQUIREMENTS = {3, 3, 3, 3, 4, 4, 4, 4, 5, 5, 5, 5};
 	public static final int[] ROUND_BONUS = {50, 50, 50, 100, 100, 100, 100, 150, 150, 150, 150, 200};
 	
 	private final Difficulty DEFAULT_DIFFICULTY= Difficulty.STANDARD;
 	
 	private PlayerManager players;
 	
 	private Difficulty difficulty;
 	
 	private Map map;
 	
 	private final int GUI_HEIGHT = 100;
 	
 	private final float MAP_PPU_X = Mule.WIDTH / 9;
 	private final float MAP_PPU_Y = (Mule.HEIGHT - GUI_HEIGHT) / 5;
 	
 	private int currentRound;
 	
 	private float currentPlayerTime;
 	
 	private Mule game;
 	
 	public GameManager(Mule game){
 		super();
 		this.game = game;
 		players = new PlayerManager();
 		currentRound = 0;
 		currentPlayerTime = 0;
 	}
 	
 	public GameManager(Mule game, PlayerManager pm){
 		this(game);
 		players = pm;
 	}
 	
 	public float getCurrentPlayerTime(){
 		return currentPlayerTime;
 	}
 	
 	public void incrementCurrentPlayerTime(float delta){
 		currentPlayerTime += delta;
 	}
 	
 	public int getNumberOfPlayers(){
 		return players.getNumberOfPlayers();
 	}
 	
 	public void setDifficulty(String s){
 		if(s.toUpperCase().equals(Difficulty.TOURNAMENT)){
 			difficulty = Difficulty.TOURNAMENT;
 		} else{
 			difficulty = DEFAULT_DIFFICULTY;
 		}
 	}
 	
 	public void setMap(String s){
 		if(s.toLowerCase().equals("random")){
 			map = new Map(true);
 		} else{
 			map = new Map(false);
 		}
 	}
 	
 	public void setPPU(){
 		map.setPPU(MAP_PPU_X, MAP_PPU_Y);
 	}
 	
 	public void setMap(Map m){
 		map = m;
 	}
 	
 	public Map getMap(){
 		return map;
 	}
 	
 	public String toString(){
 		String s = "";
 		s += "DIFFICULTY : " + difficulty + "\n";
 		s += "MAP : \n" + map.toString() + "\n";
 		s += players.toString();
 		return s;
 	}
 	
 	public boolean buyTile(int x, int y, Player p){
 		Tile t = map.getMouseClickedTile(x, y);
 		if(t == null){
 			return false;
 		}
 		if(t.getTileType() == 2){
 			return false; // can't buy town tile
 		}
 		if(t.isOwned()){
 			return false;
 		}
 		t.setOwner(p);
 		return true;
 	}
 	
 	/**
 	 * This method buys a tile. (NOTE NOT A FREE TILE)
 	 * @param x mouse click x pos
 	 * @param y mouse click y pos
 	 * @param p the player buying
 	 * @param subtractMoney whether the player needs the money for it
 	 * @return
 	 */
 	public boolean buyTile(int x, int y, Player p, boolean subtractMoney){
 		Tile t = map.getMouseClickedTile(x, y);
 		if(t == null){
 			return false;
 		}
 		if(t.getTileType() == 2){
 			return false; // can't buy town tile
 		}
 		if(t.isOwned()){
 			return false;
 		}
 		if( !p.canBuy(t)){
 			return false;
 		}
 		t.setOwner(p, subtractMoney);
 		return true;
 	}
 	
 	public Player getPlayer(int i){
 		return players.getPlayer(i);
 	}
 	
 	public int getRoundTime(){
 		return players.getCurrentPlayerTime(GameManager.FOOD_REQUIREMENTS[currentRound]);
 	}
 	
 	/**
 	 * This ends the current player's turn and therefore will give the player money and goes to the next player
 	 */
 	public void endTurnPub(){
 		int addScore = GameManager.ROUND_BONUS[currentRound];
		if(currentPlayerTime >= 37){
 			addScore += 200;
		} else if(currentPlayerTime >= 25){
 			addScore += 150;
		} else if(currentPlayerTime >= 12){
 			addScore += 100;
 		} else{
 			addScore += 50;
 		}
 		players.getCurrentPlayer().gainResources(Resource.RESOURCE_MONEY, addScore);	
 		nextPlayer();
 	}
 	
 	/**
 	 * This method will switch the current players turn and then call the method which will
 	 * restart the actual turn and reset various variables.
 	 */
 	public void nextPlayer(){
 		//int a = Mule.pm.getCurrentPlayerNumber();
 		//int b = Mule.pm.getNumberOfPlayers();
 		if(Mule.pm.getCurrentPlayerNumber() == Mule.pm.getNumberOfPlayers() - 1){
 			/*
 			 * DO TURN END Round STUFF HERE. Such as random events + reordering players + auction + other stuffs
 			 */
 			System.out.println("ENDING TURN");
 			//AuctionManger am = new AuctionManager();
 			//Mule.swapScreen(AUCTIONSCREEN);
 			Mule.pm.updatePlayerOrder();
 			//startRandomEvent();
 			game.setScreen(Mule.SELECTTILESSCREEN);
 			Mule.pm.nextPlayer();
 		}
 		currentPlayerTime = 0;
 		Mule.pm.nextPlayer();
 	}
 }
