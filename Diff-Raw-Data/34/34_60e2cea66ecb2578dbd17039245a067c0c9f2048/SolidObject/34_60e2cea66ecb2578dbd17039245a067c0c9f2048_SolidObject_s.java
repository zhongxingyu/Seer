 package com.spotlightcoding.components;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.state.StateBasedGame;
 import org.newdawn.slick.geom.Vector2f;
 
 import com.spotlightcoding.Component;
 import com.spotlightcoding.Entity;
 import com.spotlightcoding.World;
 
 public class SolidObject extends Component {
 	Entity rob;
 	
 	public SolidObject(Entity rob) {
 		this.rob = rob; 
 	}
 	
 	public void update(GameContainer gc, StateBasedGame sb, int delta) {
 		
 		if (((rob.getPosition().getX() + rob.getSize().getWidth()) > (owner.getPosition().getX() + owner.getSize().getWidth())) && ((rob.getPosition().getX()) <= owner.getPosition().getX() + owner.getSize().getWidth()) && ((rob.getPosition().getY() + rob.getSize().getHeight()) >= owner.getPosition().getY() + 6)) {
 			// Stop left movement
 			rob.setBarrier(Entity.BARRIER_LEFT);
 		} else if ((rob.getPosition().getX() < owner.getPosition().getX()) && (rob.getPosition().getX() + rob.getSize().getWidth()) >= owner.getPosition().getX() && ((rob.getPosition().getY() + rob.getSize().getHeight()) >= owner.getPosition().getY() + 6)) {
 			// Stop right movement
 			rob.setBarrier(Entity.BARRIER_RIGHT);
		} else {
 			rob.setBarrier(Entity.BARRIER_NONE);
 		}
 		
 		// System.out.println(rob.getPosition().getY() + " " + rob.getPosition().getX());
 	}
 }
