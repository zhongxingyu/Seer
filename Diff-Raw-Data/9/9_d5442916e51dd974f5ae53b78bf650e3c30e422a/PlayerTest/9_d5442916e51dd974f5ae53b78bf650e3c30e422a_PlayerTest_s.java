 package testing;
 
 import java.awt.Dimension;
 
 import app.Main;
 import app.RPGame;
 
 import com.golden.gamedev.GameEngine;
 import com.golden.gamedev.GameLoader;
 import com.golden.gamedev.GameObject;
 
 public class PlayerTest extends GameEngine {
 	
 	public static final int GAME_MODE = 1;
	  
 	public void initResources() {
 		nextGameID = GAME_MODE;
 	}
 	
 	public GameObject getGame(int GameID) {
 		switch (GameID) {
 			case GAME_MODE : return new RPGame(this);
 		}
 		return null;
 	}
 	
 	public static void main(String[] args) {
 		GameLoader game = new GameLoader();
 		game.setup(new Main(), new Dimension(480, 320), false);
 		game.start();
 	}
 }
