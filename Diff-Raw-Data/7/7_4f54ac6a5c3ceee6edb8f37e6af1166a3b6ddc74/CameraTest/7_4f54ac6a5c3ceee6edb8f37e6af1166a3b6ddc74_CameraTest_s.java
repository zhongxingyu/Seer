 package edu.chl.codenameg.model;
 
 import static org.junit.Assert.assertTrue;
 
 import java.awt.Dimension;
import java.awt.Point;
 
 import org.junit.Test;
 
 public class CameraTest {
 
 	@Test
 	public void testGetSize() {
 		Camera camera = new Camera();
 		Dimension cameraSize = camera.getSize();
 		assertTrue(cameraSize != null);
 	}
 	
 	@Test
 	public void testGetPosition () {
 		Camera camera = new Camera();
 		Position pos = camera.getPosition();
 		assertTrue(pos.equals(new Position(0,0)));
 	}
 	
 	@Test
 	public void testIfMoving() {
 		Camera camera = new Camera();
 		Position pos = camera.getPosition();
 		camera.update();
 		Position pos2 = camera.getPosition();
 		assertTrue(!pos.equals(pos2));		
 	}
 	
 	@Test
 	public void testIfMovingRight() {
 		Camera camera = new Camera();
 		Position pos = camera.getPosition();
 		camera.update();
 		Position pos2 = camera.getPosition();
 		System.out.println(pos.getX() + "\t" + pos2.getX());
 		assertTrue(pos.getX() < pos2.getX());
 		
 	}
 
 }
