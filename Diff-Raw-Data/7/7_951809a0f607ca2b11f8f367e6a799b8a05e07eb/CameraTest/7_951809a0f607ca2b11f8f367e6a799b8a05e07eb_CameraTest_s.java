 package edu.chl.codenameg.model.entity;
 
import static org.junit.Assert.*;

import org.junit.Test;

 import edu.chl.codenameg.model.Camera;
 import edu.chl.codenameg.model.Position;
 import edu.chl.codenameg.model.Vector2D;
 import edu.chl.codenameg.model.World;
import edu.chl.codenameg.model.entity.PlayerCharacter;
 
 public class CameraTest {
 
 	public void testUpdate() {
 		World w = new World();
 		PlayerCharacter pc1 = new PlayerCharacter(w);
 		PlayerCharacter pc2 = new PlayerCharacter(w);
 		w.add(pc1);
 		w.add(pc2);
 		Camera c = new Camera(w);
 		pc1.setPosition(new Position(0,50));
 		pc2.setPosition(new Position(pc1.getHitbox().getWidth() +1, 50));
 		while(pc2.getPosition().getX()<c.getMaxWidth()-pc2.getHitbox().getWidth()){
 			pc2.addVector2D(new Vector2D(1,0));
 			w.update(10);
 			c.update(10);
 		}
 		// pc2 should already be standing next to the wall, hence next addvector2D() shouldn't change pc2's position in X-direction
 		pc2.addVector2D(new Vector2D(1,0));
 		w.update(10);
 		c.update(10);
 		boolean case1 = pc2.getPosition().getX() ==c.getMaxWidth()-pc2.getHitbox().getWidth();
 		boolean case2 = c.getWidth() == 700;
 		pc2.setPosition(new Position(pc1.getHitbox().getWidth() +1, 50));
 		w.update(10);
 		c.update(10);
 		// Camera shouldnt be bigger than 700 pixels
 		boolean case3 = c.getWidth() == 500;
 		assertTrue(case1 && case2 && case3);
 		
 		
 	}
 
 }
