 package com.spotlightcoding;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 
 import com.spotlightcoding.components.ImageRenderComponent;
 import com.spotlightcoding.components.LeftRightMovement;
 
 public class World extends BasicGameState{
 	Image worldMap;
 	Image robImg;
 	Entity level;
 	Entity rob;
 	
 	
 	public World(int state){
 		
 	}
 	
 	@Override
 	public void init(GameContainer arg0, StateBasedGame arg1)
 			throws SlickException {
 		worldMap = new Image("res/map.png");		
		worldMap = new Image("res/robber.png");
 		
 		rob = new Entity("Rob");
 		rob.AddComponent(new ImageRenderComponent("robRender",robImg));
 		
 		level = new Entity("level");
 		level.AddComponent(new ImageRenderComponent("levelrender", worldMap));
 		level.AddComponent(new LeftRightMovement("mapLeftRight"));
 		
		
		
		
 	}
 
 	@Override
 	public void render(GameContainer gc, StateBasedGame arg1, Graphics gr)
 			throws SlickException {
 		level.render(gc,null,gr);
 		rob.render(gc,null,gr);
 	}
 
 	@Override
 	public void update(GameContainer gc, StateBasedGame sb, int delta)
 			 throws SlickException {
 			level.update(gc, sb, delta);
 			//rob.update(gc, sb, delta);
 	}
 
 	@Override
 	public int getID() {
 		return 0;
 	}
 	
 	
 }
