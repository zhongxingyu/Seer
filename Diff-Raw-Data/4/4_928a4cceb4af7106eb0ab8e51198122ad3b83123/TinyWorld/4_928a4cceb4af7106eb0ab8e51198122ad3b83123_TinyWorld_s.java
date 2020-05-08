 package com.calumgilchrist.ld23.tinyworld.core;
 
 import static playn.core.PlayN.*;
 
 import java.util.ArrayList;
 import java.util.Random;
 
 import org.jbox2d.common.Vec2;
 import org.jbox2d.dynamics.BodyDef;
 import org.jbox2d.dynamics.BodyType;
 import org.jbox2d.dynamics.World;
 
 import playn.core.Game;
 import playn.core.Image;
 import playn.core.ImageLayer;
 import playn.core.Keyboard;
 import playn.core.Pointer;
 import playn.core.Keyboard.Event;
 import playn.core.Keyboard.TypedEvent;
 
 public class TinyWorld implements Game, Keyboard.Listener, Pointer.Listener {
 	ArrayList<Planetoid> planetoids;
 	
 	Player player;
 	
 	World world;
 	Vec2 mousePos;
 
 	@Override
 	public void init() {
 		planetoids = new ArrayList<Planetoid>();
 		
 		pointer().setListener(this);
 		keyboard().setListener(this);
 	
 		// create and add background image layer
 		Image bgImage = assets().getImage("images/bg.png");
 		ImageLayer bgLayer = graphics().createImageLayer(bgImage);
 		graphics().rootLayer().add(bgLayer);
 		
 		// Load grey asteroid image asset
 		Image asteroid = assets().getImage("images/grey-asteroid.png");
 
 		Vec2 pStart = new Vec2(20,20);
 		mousePos = new Vec2(60,60);
 		
 		//Set up the world
 		world = new World(new Vec2(), false);
 		
 		for (int i=0; i < 20; i++) {
 			createAsteroid(asteroid);
 		}
 		
 		BodyDef playerBodyDef = new BodyDef();
 		playerBodyDef.type = BodyType.DYNAMIC;
 		playerBodyDef.position.set(mousePos.mul(1/Constants.PHYS_RATIO));
 		
 		player = new Player(mousePos,new Sprite(100,100), playerBodyDef, world);
 		graphics().rootLayer().add(player.getSprite().getImageLayer());
 		player.getSprite().addFrame(asteroid);
 	}
 
 	public void createAsteroid(Image asteroid) {
 
 		//Start Vector off screen (This should be random)
 		Vec2 astrStart = genStartPos(graphics().width(), graphics().height());
 		
 		//Set up an asteroid
 		BodyDef astrBodyDef = new BodyDef();
 		astrBodyDef.type= BodyType.DYNAMIC;
 		
 		//Initial Position
 		astrBodyDef.position.set(astrStart.mul(1/Constants.PHYS_RATIO));
 		
 		Asteroid astr = new Asteroid(astrStart, new Sprite((int) astrStart.x, (int) astrStart.y), astrBodyDef, world);
 		astr.getSprite().addFrame(asteroid);
 		
 		//Apply a force to the asteroid
 		Vec2 forceDir = astr.getStartDirVec(graphics().width(), graphics().height());
 		astr.applyThrust(forceDir.mul(0.1f));
 		
 		planetoids.add(astr);
 		graphics().rootLayer().add(astr.getSprite().getImageLayer());
 	}
 	
 	
 	@Override
 	public void paint(float alpha) {
 		// the background automatically paints itself, so no need to do anything
 		// here!
 		
 	}
 
 	@Override
 	public void update(float delta) {
 		//Values need playing with, and to be stored
 		world.step(60, 30, 30);
 		world.clearForces();
 		
 		//player.setPos(mousePos);
 		
 		player.update();
 		
 		// For every planetoid update it's sprite
 		for (Planetoid p : planetoids) {
 			p.update();
 		}
 	}
 
 	@Override
 	public int updateRate() {
 		return 25;
 	}
 
 	@Override
 	public void onKeyDown(Event event) {
 		
 		switch (event.key()) {
 		case ESCAPE:
 			//Quit
 		}
 		
 	}
 
 	@Override
 	public void onKeyTyped(TypedEvent event) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void onKeyUp(Event event) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void onPointerStart(playn.core.Pointer.Event event) {
 		System.out.println(event.x() + "," +event.y());
 		
 		
 	}
 
 	@Override
 	public void onPointerEnd(playn.core.Pointer.Event event) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void onPointerDrag(playn.core.Pointer.Event event) {
 		mousePos = new Vec2(event.x(),event.y());
 		player.applyThrust(new Vec2(100,100));
 	}
 
 	/**
 	 * Get the starting position of an object
 	 * @param screenWidth
 	 * @param screenHeight
 	 * @return
 	 */
 	public Vec2 genStartPos(float screenWidth, float screenHeight) {
 		float x = getSpawnBound((int) screenWidth);
 		float y = getSpawnBound((int) screenHeight);
 		
 		Vec2 pos = new Vec2(x, y);
 
 		return pos;
 	}
 	
 	/**
 	 * Randomly choose a number NOT between 0 and limit
 	 * @param limit
 	 * @return
 	 */
 	private int getSpawnBound(int limit) {
 		
 		Random rand = new Random();
 		
 		int spawnBound = 100;
 		//Set x
 		boolean belowBound = rand.nextBoolean();
 		
 		int pos = rand.nextInt(spawnBound);
 		
 		if (belowBound) {
 			pos = -pos;
		} 
 		
 		return pos;
 	}
 }
