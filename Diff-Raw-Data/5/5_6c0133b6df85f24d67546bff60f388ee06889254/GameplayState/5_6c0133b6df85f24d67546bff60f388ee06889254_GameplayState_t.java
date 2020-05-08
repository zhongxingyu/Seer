 /**
  *
  * @author Catt
  * @author Zhengman777
  *
  **/
 package catt.kedavra;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 
 import catt.kedavra.characters.Player;
 import catt.kedavra.components.CoMovement;
 import catt.kedavra.components.CoRender;
 
 
 public class GameplayState extends BasicGameState {
 	
 	private Image imgBackground;
 	Image playerSpr;
 	private int stateID = -1;
 	Player player;
 	
 	//-----SLICK METHODS BELOW---------//
 	public GameplayState(int stateID){
 		this.stateID = stateID;
 	}
 	
 	@Override
 	public int getID(){
 		return stateID;
 	}
 	
 	public void init(GameContainer gc, StateBasedGame sbg) throws SlickException{
 		//Load the background image
 		imgBackground = new Image("img/grass.png");
 		playerSpr = new Image("img/player.png");
		player = new Player(1, 1 , .2f, .3f, .5f, .0005f, .0007f);
 		player.addComponent(new CoRender(1, playerSpr));
		player.addComponent(new CoMovement(1));
 	}
 	
 	public void render(GameContainer gc, StateBasedGame sbg, Graphics gr) throws SlickException{
 		imgBackground.draw(0,0);
 		player.render(gc, sbg, gr);
 	}
 	
 	public void update(GameContainer gc, StateBasedGame sbg, int delta) throws SlickException{
 		player.update(gc, sbg, delta);
 	}
 
 	//-----CUSTOM METHODS BELOW-------//	
 
 	
 }
