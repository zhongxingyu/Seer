 package com.jverkamp.brong.thing;
 
 import java.awt.Dimension;
 import java.awt.Graphics2D;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.geom.Point2D;
 import java.util.*;
 
 import sun.reflect.generics.reflectiveObjects.NotImplementedException;
 
 /**
  * Store the world and everything in it.
  */
 public class World extends Thing implements KeyListener {
 	public List<Thing> Things;
 	public Dimension Bounds;
 	boolean[] Keys = new boolean[256]; 
 	
 	/**
 	 * Create a new world.
 	 * @param size The world's size.
 	 */
 	public World(Dimension size) {
 		super(new Point2D.Double(size.width / 2, size.height / 2));
 		
 		Things = new ArrayList<Thing>();
 		Bounds = size;
 		
 		Things.add(new Ball(Center));
 		Things.add(new Paddle(new Point2D.Double(size.width / 2, 25), Paddle.MOVE_ON_AD));
 		Things.add(new Paddle(new Point2D.Double(size.width / 2, size.height - 25), Paddle.MOVE_ON_ARROWS));
 	}
 	
 	/**
 	 * Update this world
 	 * @param time Seconds since last update
 	 */
 	public void update(double time) {
 		for (Thing t : Things) 
 			t.update(this, time);
 	}	
 	
 	/**
 	 * Update the world
 	 * @param time The amount of time that has passed. 
 	 */
 	@Override
 	public void update(World world, double time) {
 		throw new NotImplementedException();
 	}
 	
 	/**
 	 * Draw this ball.
 	 * @param g2d Draw with me.
 	 */
 	@Override
 	public void draw(Graphics2D g2d) {
 		for (Thing t : Things) 
 			t.draw(g2d);
 	}
 
 	/**
 	 * Track when a key is pressed.
 	 */
 	@Override
 	public void keyPressed(KeyEvent e) {
 		int code = e.getKeyCode();
		
		if (code == KeyEvent.VK_A)
			System.out.println("A pressed");
		else if (code == KeyEvent.VK_D)
			System.out.println("D pressed");
		else if (code == KeyEvent.VK_LEFT)
			System.out.println("Left pressed");
		else if (code == KeyEvent.VK_RIGHT)
			System.out.println("Right pressed");
		
 		if (code >= 0 && code < Keys.length)
 			Keys[code] = true; 
 	}
 
 	/**
 	 * Track when a key is released.
 	 */
 	@Override
 	public void keyReleased(KeyEvent e) {
 		int code = e.getKeyCode();
 		if (code >= 0 && code < Keys.length)
 			Keys[code] = false;
 	}
 
 	/**
 	 * Ignore when a key is typed.
 	 */
 	@Override
 	public void keyTyped(KeyEvent e) {
 		
 	}
 }
