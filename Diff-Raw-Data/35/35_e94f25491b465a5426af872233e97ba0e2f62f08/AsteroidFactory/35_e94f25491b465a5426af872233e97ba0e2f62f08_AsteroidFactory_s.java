 package com.calumgilchrist.ld23.tinyworld.core;
 
 import static playn.core.PlayN.graphics;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.Random;
 
 import org.jbox2d.common.Vec2;
 import org.jbox2d.dynamics.Body;
 import org.jbox2d.dynamics.BodyDef;
 import org.jbox2d.dynamics.BodyType;
 import org.jbox2d.dynamics.World;
 
 import playn.core.GroupLayer;
 import playn.core.Image;
 
 public class  AsteroidFactory{
 
 	static World world;
 	static Image img;
 	GroupLayer astrLayer;
 	
 	private static ArrayList<Asteroid> instances = new ArrayList<Asteroid>();
 	private ArrayList<Body> destroyList;
 	
 	public AsteroidFactory(World world, Image img, GroupLayer layer) {
 		AsteroidFactory.world = world;
 		AsteroidFactory.img = img;
 		astrLayer = layer;
 		
 		destroyList = new ArrayList<Body>();
 	}
 	
 	public Asteroid getAsteroid(float forceFactor) {
 		
 		//TODO: Randomise Force
 		
 		//Start Vector off screen (This should be random)
 		Vec2 astrStart = genStartPos(graphics().width(), graphics().height());
 		
 		// Set up an asteroid
 		BodyDef astrBodyDef = new BodyDef();
 		astrBodyDef.type= BodyType.DYNAMIC;
 		
 		//Initial Position
 		astrBodyDef.position.set(astrStart.mul(1/Globals.PHYS_RATIO));
 		
 		Asteroid astr = new Asteroid(astrStart, new Sprite((int) astrStart.x, (int) astrStart.y, img), astrBodyDef, world);
 		
 		astr.applyThrust(astr.getThrustForce(forceFactor));
 		
 		instances.add(astr);
 		astrLayer.add(astr.getSprite().getImageLayer());
 		
 		return astr;
 	}
 
 	public void update() {		
 		//Destroy bodies to be destroyed
 		for (Body body: destroyList) {
 			world.destroyBody(body);
 		}
 		
 		// For every planetoid update it's sprite
 		for (Planetoid p : instances) {
 			p.update();		
 			
 		}
 		
 		destroyList.clear();
 	}
 	
 	/**
 	 * Get the starting position of an object
 	 * 
 	 * @param screenWidth
 	 * @param screenHeight
 	 * @return
 	 */
 	private static Vec2 genStartPos(int width, int height) {

		float x = getSpawnBound(-width, width);
		float y = getSpawnBound(-height, height);
 
 		Vec2 pos = new Vec2(x, y);
 
 		return pos;
 	}
 
 	/**
 	 * Randomly choose a number NOT between 0 and limit
 	 * 
 	 * @param max
 	 * @return
 	 */
 	private static int getSpawnBound(int min, int max) {
 
 		Random rand = new Random();
 
 		int spawnBound = 100;
 		// Set x
 		boolean belowBound = rand.nextBoolean();
 
 		int pos = rand.nextInt(spawnBound) + 50;
 
 		if (belowBound) {
 			pos = -pos + min;
 		} else {
 			pos += max;
 		}
 
 		return pos;
 	}
 	
 //	/**
 //	 * Take a body, destroy it and replace it.
 //	 * @param body
 //	 */
 //	public void replaceAsteroid(Body body){
 //		
 //		Vec2 astrStart = genStartPos(graphics().width(), graphics().height());
 //		
 //
 //		world.destroyBody(body);
 ////		planet.newBody(astrBodyDef, world);
 //		
 //		//Set up an asteroid
 //		BodyDef astrBodyDef = new BodyDef();
 //		astrBodyDef.type= BodyType.DYNAMIC;
 //		
 //		//Initial Position
 //		astrBodyDef.position.set(astrStart.mul(1/Constants.PHYS_RATIO));
 //				
 //
 //	}
 
 	/**
 	 * Find the Asteroid then tell it to be removed
 	 * @param body - Child body to be destroyed
 	 */
 	public void removeAstrByBody(Body body) {
 		Asteroid astr = getAstrFromBody(body);
 		removeAstr(astr);
 	}
 	
 	/**
 	 * Find the parent Asteroid from the given body
 	 * @param body
 	 * @return
 	 */
 	public Asteroid getAstrFromBody(Body body) {
 		boolean found = false;
 		Asteroid planet = null;
 				
 		Iterator<Asteroid> it = instances.iterator();
 		
 		/*
 		 * Slow and awful
 		 * TODO: Use something better than O(n)
 		 */
 		while (it.hasNext()) {
 			planet = it.next();
 			if (planet.getBody().equals(body)) {
 				
 				found = true;
 			}
 		}
 		
 		if (found) {
 			return planet;
 		} else {
 			return null;
 		}
 	}
 		
 	/**
 	 * Remove all instances of Asteroid
 	 * @param astr
 	 */
 	private void removeAstr(Asteroid astr) {
 		
 		instances.remove(astr);
 		
 		//Destroy the planet
 		destroyList.add(astr.getBody());
 		astr.getSprite().getImageLayer().destroy();
 	}
 }
