 package com.digitallyanalogue.sgl.MainMenu;
 
 import java.util.ArrayList;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 
 import com.digitallyanalogue.sgl.States;
 import com.digitallyanalogue.sgl.GUI.Button;
 import com.digitallyanalogue.sgl.GUI.GUIElement;
 
 public class MainMenu extends BasicGameState{
 	
 	ArrayList<GUIElement> gui = new ArrayList<GUIElement>();
 
 	@Override
 	public void init(GameContainer arg0, StateBasedGame arg1)
 			throws SlickException {
 		gui.add(new Button("New Game",400, 300));
 	}
 
 	@Override
 	public void render(GameContainer arg0, StateBasedGame arg1, Graphics arg2)
 			throws SlickException {
 		for(GUIElement e: gui){
 			e.render(arg2);
 		}
 	}
 
 	@Override
 	public void update(GameContainer arg0, StateBasedGame arg1, int arg2)
 			throws SlickException {
 		for(GUIElement e: gui){
 			e.update(arg0);
 		}
 	}
 
 	@Override
 	public int getID() {
 		return States.MAIN_MENU;
 	}
 
 }
