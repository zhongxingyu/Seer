 package game;
 
 import java.util.ArrayList;
 
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.geom.Vector2f;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
import org.newdawn.slick.state.transition.FadeInTransition;
 import org.newdawn.slick.state.transition.FadeOutTransition;
 
 import components.*;
 
 public class InGameState extends BasicGameState {
 
 	public static final int ID = 1;
 	private ArrayList<Entity> entities;
 
 	@Override
 	public void init(GameContainer gc, StateBasedGame sb) throws SlickException {
 		Controller.init();
 		entities = new ArrayList<Entity>();
 
 		Entity background = new Entity("background");
 		background.AddComponent(new ImageRenderComponent("BackgroundRender",
 				new Image("/Sprites/background.png")));
 		entities.add(background);
 
 		Entity player = new Entity("player");
 		player.AddComponent(new ImageRenderComponent("PlayerRender", new Image(
 				"/Sprites/Character.png")));
 		player.AddComponent(new PlayerMovementComponent("PlayerMovement"));
 		player.setPosition(new Vector2f(400, 300));
 		entities.add(player);
 
 	}
 
 	@Override
 	public void render(GameContainer gc, StateBasedGame sb, Graphics g)
 			throws SlickException {
 		for (Entity e : entities) {
 			e.render(gc, sb, g);
 		}
 
 	}
 
 	@Override
 	public void update(GameContainer gc, StateBasedGame sb, int delta)
 			throws SlickException {
 		for (Entity e : entities) {
 			e.update(gc, sb, delta);
 		}
 
 		Input input = gc.getInput();
 		if (Controller.isShortcutPressed("Exit", input))
 			System.exit(0);
 		if (Controller.isShortcutPressed("Fullscreen", input))
 			Game.app.setFullscreen(!Game.app.isFullscreen());
 		if (Controller.isShortcutPressed("Menu", input)) {
 			//sb.enterState(MenuState.ID);
 			sb.enterState(MenuState.ID, new FadeOutTransition(Color.black, 250), null);
 		}
 	}
 
 	@Override
 	public int getID() {
 		return ID;
 	}
 
 }
