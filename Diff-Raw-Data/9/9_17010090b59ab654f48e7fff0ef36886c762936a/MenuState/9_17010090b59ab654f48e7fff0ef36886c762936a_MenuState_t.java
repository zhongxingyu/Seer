 package game;
 
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 import org.newdawn.slick.state.transition.FadeOutTransition;
 
 public class MenuState extends BasicGameState {
 
 	public static final int ID = 2;
 
 	@Override
 	public void init(GameContainer gc, StateBasedGame sb) throws SlickException {
 	}
 
 	@Override
 	public void render(GameContainer gc, StateBasedGame sb, Graphics g)
 			throws SlickException {
 		g.setColor(Color.white);
 		g.drawString("MENU", 350, 100);
 		g.drawString("1. New Game", 350, 200);
 		g.drawString("2. Options", 350, 250);
 		g.drawString("3. Exit", 350, 300);
 
 	}
 
 	@Override
 	public void update(GameContainer gc, StateBasedGame sb, int delta)
 			throws SlickException {
 
 		Input input = gc.getInput();
 		if (input.isKeyPressed(Input.KEY_ESCAPE)) {
 			sb.enterState(InGameState.ID, new FadeOutTransition(Color.black, 250), null);
 		}
 
 	}
 
 	@Override
 	public int getID() {
 		return ID;
 	}
 
 }
