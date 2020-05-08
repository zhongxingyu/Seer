 package view;
 
 import controller.*;
 
 import org.newdawn.slick.GameContainer;
 
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 import org.newdawn.slick.Image;
 
 
 public class VictoryState extends BasicGameState {
 
     private int stateID = -1;
 
     Image background = null;
     Image menu = null;
     int mouseX = 0;
     int mouseY = 0;
 
     public VictoryState(int stateID) {
 	this.stateID = stateID;
     }
 
     @Override
     public int getID() {
 	return stateID;
     }
 
     @Override
     public void init(GameContainer gc, StateBasedGame sbg)
 	    throws SlickException {
 	background = new Image("res/victoire/victoire.png");
	menu = new Image("res/menu/menu.png");
     }
 
     @Override
     public void update(GameContainer gc, StateBasedGame sbg, int delta)
 	    throws SlickException {
 	
 	getPosClicked(gc);
 	boolean insideMenu = false;
 	
 	if ((mouseX > 530 && mouseX < menu.getWidth() + 530)
 		&& (mouseY >= 500 && mouseY <= menu.getHeight() + 500))
 	    insideMenu = true;
 
 	if(insideMenu)
 	    sbg.enterState(ViewController.MAINMENUSTATE);
     }
 
     @Override
     public void render(GameContainer arg0, StateBasedGame arg1, Graphics arg2)
 	    throws SlickException {
 	background.draw(0, 0);
 	menu.draw(530, 500);
 	
     }
 
     private void getPosClicked(GameContainer gc) {
 	Input input = gc.getInput();
 	if (input.isMousePressed(0)) {
 	    mouseX = input.getMouseX();
 	    mouseY = input.getMouseY();
 	}
     }
 }
