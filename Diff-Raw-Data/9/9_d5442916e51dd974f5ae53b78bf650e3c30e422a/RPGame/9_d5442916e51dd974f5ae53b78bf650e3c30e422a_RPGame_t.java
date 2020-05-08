 package app;
 
 import inventory.Item;
 
 import java.awt.Graphics2D;
 import java.util.Comparator;
 
 import level.Level;
 import player.Player;
 import quest.Quest;
 import quest.QuestGiver;
 import quest.QuestJournal;
 import utils.JsonUtil;
 
 import com.golden.gamedev.GameEngine;
 import com.golden.gamedev.GameObject;
 import com.golden.gamedev.object.PlayField;
 import com.golden.gamedev.object.Sprite;
 import com.google.gson.JsonObject;
 
 
 public class RPGame extends GameObject {
 
	static String gameURL = "rsc/config/game.json";
 
 	public PlayField field = new PlayField();
 
 	private Player player;
 	private Level level;
 	private QuestJournal myJournal;
 	String lower, upper;
 	boolean pausedForInventory = false;
 	boolean pausedForStore = false;
 
 	public RPGame(GameEngine parent) {
 		super(parent);
 	}
 
 	public void initResources() {
 		JsonObject gameJson = JsonUtil.getJSON(gameURL);
 		
 		level = new Level(bsLoader, bsIO, this, gameJson.get("level").getAsString());
 
 		field.setComparator(new Comparator<Sprite>() {
 			public int compare(Sprite o1, Sprite o2) {
 				if (o1 instanceof Item)
 					return -1;
 				else if (o2 instanceof Item)
 					return 1;
 				return (int) (o1.getY() - o2.getY());
 			}
 		});
 		field.setBackground(level);
 	}
 
 	public void render(Graphics2D g) {
 		field.render(g);
 	      if (isPausedForInventory()){
 	            player.getCharacter().getInventory().render(g);
 	            return;
 	            }
 	     
 	}
 
 	public void update(long elapsed) {
 	    if (isPausedForInventory()){
 	        player.getCharacter().getInventory().update(elapsed);
 	        return;
 	        }
 	    
 	    
 		field.update(elapsed);
 		player.update(elapsed);
 	}
 
 	public Player getPlayer() {
 		return player;
 	}
 
 	public Level getLevel() {
 		return level;
 	}
 
 	public void setPlayer(Player player) {
 		this.player = player;
 	}
 	
 	public void addQuest(Quest qu, QuestGiver qg) {
 		myJournal.addQuest(qu, qg);
 	}
 
 	public PlayField getField() {
 		return field;
 	}
 
 	public void pauseGameForInventory() {
 		pausedForInventory = true;
 	}
 
 	public void unPauseGameForInventory() {
 		pausedForInventory = false;
 	}
 	
 	public boolean isPausedForInventory(){
 	    return pausedForInventory;
 	}
 	
 	public boolean isPausedForStore(){
 	    return pausedForStore;
 	}
 	
 	public void pauseGameForStore() {
 		pausedForStore = true;
 	}
 
 	public void unPauseGameForStore() {
 		pausedForStore = false;
 	}
 
 
 }
