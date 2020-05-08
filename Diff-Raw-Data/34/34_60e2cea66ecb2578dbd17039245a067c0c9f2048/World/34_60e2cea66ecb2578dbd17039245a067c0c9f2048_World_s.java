 package com.spotlightcoding;
 
 
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.geom.Vector2f;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 
 import java.util.ArrayList;
 
 import com.spotlightcoding.components.Gravity;
 import com.spotlightcoding.components.ImageRenderComponent;
 import com.spotlightcoding.components.LeftRightMovement;
 import com.spotlightcoding.components.Floor;
 import com.spotlightcoding.components.MoveJumping;
 import com.spotlightcoding.components.Hole;
 import com.spotlightcoding.components.SolidObject;
 
 public class World extends BasicGameState{
 	Image worldMap;
 	Image robImg;
 	Image floorImg;
 	Image hole;
 	Image vault;
 	Entity level;
 	Entity rob;
 	
 	ArrayList <Entity>blocks;
 	
 	public static final int GROUND_LEVEL = 500;
 	
 	public World(int state){
 		
 		
 	}
 	
 	@Override
 	public void init(GameContainer arg0, StateBasedGame arg1) throws SlickException {
 
 		worldMap = new Image("res/mapBlank.png");		
 		robImg = new Image("res/robber.png");
 		floorImg = new Image("res/floor.png");
 		hole = new Image("res/hole.png");
 		vault = new Image("res/vault-door.png");
 
 		level = new Entity("level", "environment");
 		level.addComponent(new ImageRenderComponent(worldMap));
 		level.addComponent(new LeftRightMovement(rob));
 		
 		rob = new Entity("Rob", "character");
 		rob.addComponent(new ImageRenderComponent(robImg));
 		rob.addComponent(new Gravity(rob));
 		rob.addComponent(new MoveJumping());
 		rob.setPosition(new Vector2f(400,(int)(GROUND_LEVEL - rob.getSize().getHeight()) +5));
 		
 		blocks = this.getLevelBlocks();
 	}
 
 	@Override
 	public void render(GameContainer gc, StateBasedGame arg1, Graphics gr) throws SlickException {
 		for (Entity block : blocks) {
 			block.render(gc, null, gr);
 		}
 		
 		level.render(gc,null,gr);
 		rob.render(gc,null,gr);
 	}
 
 	@Override
 	public void update(GameContainer gc, StateBasedGame sb, int delta) throws SlickException {
 		for (Entity block : blocks) {
 			block.update(gc, sb, delta);
 		}
 		
 		level.update(gc, sb, delta);
 		rob.update(gc, sb, delta);
 	}
 	
 	private ArrayList<Entity> getLevelBlocks() {
 		ArrayList <Entity>arrBlocks = new ArrayList<Entity>();
 		
 		arrBlocks.add(new Entity("floor1", "floor"));
 		arrBlocks.add(new Entity("hole1", "hole"));
 		arrBlocks.add(new Entity("hole2", "hole"));
 		arrBlocks.add(new Entity("hole3", "hole"));
 		arrBlocks.add(new Entity("floor2", "floor"));
 		arrBlocks.add(new Entity("floor3", "floor"));
 		arrBlocks.add(new Entity("floor4", "floor"));
 		arrBlocks.add(new Entity("floor5", "floor"));
		arrBlocks.add(new Entity("vaultDoor1", "vaultDoor"));
 		arrBlocks.add(new Entity("floor7", "floor"));
 		arrBlocks.add(new Entity("floor8", "floor"));
 		arrBlocks.add(new Entity("floor9", "floor"));
 		arrBlocks.add(new Entity("floor11", "floor"));
 		arrBlocks.add(new Entity("floor10", "floor"));
 		arrBlocks.add(new Entity("floor12", "floor"));
 		arrBlocks.add(new Entity("floor13", "floor"));
 		arrBlocks.add(new Entity("floor14", "floor"));
 		arrBlocks.add(new Entity("floor15", "floor"));
 		arrBlocks.add(new Entity("floor16", "floor"));
 		arrBlocks.add(new Entity("floor17", "floor"));
 		arrBlocks.add(new Entity("floor18", "floor"));
 		arrBlocks.add(new Entity("floor19", "floor"));
 		
 		int count = 0;
 		for (Entity block : arrBlocks) {
 			
 			if (block.getType() == "floor") {
 				block.addComponent(new Floor(rob));
 				block.addComponent(new ImageRenderComponent(floorImg));				
 			}else if (block.getType() == "hole") {
 				block.addComponent(new Hole(rob));
 				block.addComponent(new ImageRenderComponent(hole));
 			}else if (block.getType() == "vaultDoor") {
 				block.addComponent(new SolidObject(rob));
 				block.addComponent(new Floor(rob));
 				block.addComponent(new ImageRenderComponent(vault));
 			}
 			
 			block.addComponent(new LeftRightMovement(rob));
 			block.setPosition(new Vector2f((int)(count*block.getSize().getWidth()), (int)(GROUND_LEVEL - (block.getSize().getHeight() - 32))));
 			
 			count++;
 		}
 		
 		return arrBlocks;
 	}
 
 	@Override
 	public int getID() {
 		return 0;
 	}
 }
