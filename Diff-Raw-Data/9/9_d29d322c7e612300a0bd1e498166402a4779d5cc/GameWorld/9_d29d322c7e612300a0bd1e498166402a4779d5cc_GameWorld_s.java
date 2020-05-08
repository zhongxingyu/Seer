 package Game;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.LinkedList;
 
 import javafx.event.ActionEvent;
 import javafx.event.EventHandler;
 import javafx.scene.control.Button;
 
 public class GameWorld extends GameLevel {
 	public LinkedList<GameLevel> levels;
 	public GameLevel currentLevel;
 	public float offsetX;
 	public float offsetY;
 	public Entity player;
 
 	public GameWorld() {
 		super("Game/menu.jpg");
 		offsetX = 0.0f;
 		offsetY = 0.0f;
 		player = new Creature(30, 80);
 		player.addToWorld(this);
 		levels = new LinkedList<GameLevel>();
 		levels.add(this);
 		currentLevel = levels.getFirst();
 	}
 
 	public synchronized float getOffsetX() {
 		return offsetX;
 	}
 
 	public synchronized void setOffsetX(float offsetX) {
 		this.offsetX = offsetX;
 	}
 
 	public synchronized float getOffsetY() {
 		return offsetY;
 	}
 
 	public synchronized void setOffsetY(float offsetY) {
 		this.offsetY = offsetY;
 	}
 
 	public void setPlayer(Entity entity) {
 		this.player = entity;
 	}
 
 	public void changeLevel(GameLevel level) {
 		if(currentLevel != null){
 			currentLevel.stopAll();
 		}
 		currentLevel = level;
 		player.addToWorld(level);
 		offsetX = 0.0f;
 		offsetY = 0.0f;
 		App.root.getChildren().removeAll(App.root.getChildren());
 		startLevel();
 	}
 	public void startLevel(){
 		currentLevel.time.timeline.playFromStart();
 		currentLevel.time.t = new Thread(currentLevel.time.r);
 		currentLevel.time.key = new Thread(Events.keyThread);
 		currentLevel.time.t.start();
 		currentLevel.time.key.start();
 		App.root.getChildren().add(currentLevel.backGround);
 		for (Entity a : currentLevel.gameElements) {
 			App.root.getChildren().add(a.node);
 		}
 		App.pS.setScene(App.scene);
 		App.pS.show();
 	}
 	public static GameWorld parse(String raw) {
 		String[] levelList = Parse.fragment(raw);
 		GameWorld game = new GameWorld();
 		for (String a : levelList) {
 			try {
 				game.addLevel(GameLevel.parse(Parse.readFromFile(a + ".txt")));
 			} catch (IOException e) {
 				System.out
 						.println("Error: attempted to add a level that doesn't exist");
 				e.printStackTrace();
 			}
 		}
 		return game;
 	}
 
 	public String toString() {
 		String str = "";
		for (GameLevel level : levels) {
			str += level.toString() + Parse.delim;
 		}
 		return str;
 	}
 
 	public void addLevel(GameLevel game) {
 		levels.add(game);
 	}
 }
