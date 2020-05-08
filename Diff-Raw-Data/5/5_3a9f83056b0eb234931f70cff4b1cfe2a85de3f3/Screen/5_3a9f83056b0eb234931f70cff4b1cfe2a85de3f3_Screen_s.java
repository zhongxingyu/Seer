 package cc.game.SteampunkZelda.screen;
 
 import cc.game.SteampunkZelda.SteampunkZelda;
 import org.lwjgl.util.vector.Vector2f;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.SlickException;
 
import java.awt.*;

 /**
  * Created with IntelliJ IDEA.
  * User: Calv
  * Date: 24/02/13
  * Time: 14:29
  * To change this template use File | Settings | File Templates.
  */
 public abstract class Screen {
     public final SteampunkZelda game;
     private final Vector2f mapSize;
 
     protected Screen(SteampunkZelda game, int WIDTH, int HEIGHT) {
         this.game = game;
        this.mapSize = new Vector2f(HEIGHT,WIDTH);
     }
 
     public void onStart() throws SlickException {
     }
 
     public void onStop() throws SlickException {
     }
 
     public void update(GameContainer gameContainer, int deltaTime) throws SlickException {
     }
 
     public abstract void render(GameContainer paramGameContainer);
 
     public Vector2f getMapSize() {
         return this.mapSize;
     }
 }
