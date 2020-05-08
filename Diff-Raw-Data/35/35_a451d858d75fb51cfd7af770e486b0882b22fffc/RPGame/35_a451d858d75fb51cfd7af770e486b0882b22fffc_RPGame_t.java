 package app;
 
 import inventory.Inventory;
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
 import com.google.gson.Gson;
 
 public class RPGame extends GameObject {
 
 	private final String gameURL = "rsc/config/game.json";
 
 	private PlayField field = new PlayField();
 
 	private Player player;
 	private Level level;
 	private Inventory myInventory;
 	private QuestJournal myJournal;
 	String lower, upper;
 	boolean pausedForInventory = false;
 
 	public RPGame(GameEngine parent) {
 		super(parent);
 	}
 
 	public void initResources() {
 		Gson gson = new Gson();
 		JsonUtil.JSONGame gameJson = gson.fromJson(JsonUtil.getJSON(gameURL),
 				JsonUtil.JSONGame.class);
 
 		level = new Level(bsLoader, bsIO, this, gameJson.level);
 		field.setComparator(new Comparator<Sprite>() {
 			public int compare(Sprite o1, Sprite o2) {
 				return (int) (o1.getY() - o2.getY());
 			}
 		});
 	}
 
 	public void render(Graphics2D g) {
 		level.render(g);
 		field.render(g);
 	}
 
 	public void update(long elapsed) {
 		level.update(elapsed);
 		field.update(elapsed);
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
 
 	public void addItems(Item itm) {
 		myInventory.add(itm);
 	}
 	
	public void addQuest(Quest qu, QuestGiver qg)
 	{
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
 
 }
