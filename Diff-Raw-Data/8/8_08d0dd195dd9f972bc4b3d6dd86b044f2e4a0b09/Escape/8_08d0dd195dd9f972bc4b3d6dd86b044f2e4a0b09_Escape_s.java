 /*****************************************************************************
  * 
  * Copyright 2012-2013 See AUTHORS file.
  * 
  * This file is part of Escape-IR.
  * 
  * Escape-IR is free software: you can redistribute it and/or modify
  * it under the terms of the zlib license. See the COPYING file.
  * 
  *****************************************************************************/
 
 package fr.escape.game;
 
 import java.util.ArrayList;
 
 import org.jbox2d.collision.shapes.PolygonShape;
 import org.jbox2d.common.Vec2;
 import org.jbox2d.dynamics.Body;
 import org.jbox2d.dynamics.BodyDef;
 import org.jbox2d.dynamics.BodyType;
 import org.jbox2d.dynamics.FixtureDef;
 import org.jbox2d.dynamics.World;
 
 import fr.escape.Objects;
 import fr.escape.app.Engine;
 import fr.escape.app.Graphics;
 import fr.escape.app.Input;
 import fr.escape.app.Overlay;
 import fr.escape.app.Screen;
 import fr.escape.game.User.LifeListener;
 import fr.escape.game.entity.CollisionDetector;
 import fr.escape.game.entity.Collisionable;
 import fr.escape.game.entity.EntityContainer;
 import fr.escape.game.entity.ships.Ship;
 import fr.escape.game.entity.ships.ShipFactory;
 import fr.escape.game.entity.weapons.shot.ShotFactory;
 import fr.escape.game.scenario.GameStage;
 import fr.escape.game.screen.Intro;
 import fr.escape.game.screen.Lost;
 import fr.escape.game.screen.Menu;
 import fr.escape.game.screen.Level;
 import fr.escape.game.screen.Error;
 import fr.escape.game.screen.Victory;
 import fr.escape.game.ui.IngameUI;
 import fr.escape.game.ui.UIHighscore;
 import fr.escape.game.ui.UIArmorLife;
 import fr.escape.game.ui.UIWeapons;
 import fr.escape.graphics.RenderListener;
 import fr.escape.graphics.ScrollingTexture;
 import fr.escape.input.Booster;
 import fr.escape.input.Drift;
 import fr.escape.input.EventListener;
 import fr.escape.input.Gesture;
 import fr.escape.input.Loop;
 import fr.escape.input.Slide;
 import fr.escape.resources.Resources;
 import fr.escape.resources.TextureLoader;
 import fr.escape.resources.scenario.ScenarioLoader;
 
 /**
  * <p>
  * Escape Game
  * 
  * <p>
  * This class is the main Controller of the Game that link many components together.
  * 
  * <p>
  * A class which delegates rendering to a {@link Screen}.
  * Allowing multiple screens for a Game.
  * 
  */
 public final class Escape implements LifeListener, RenderListener, EventListener {
 	
 	/**
 	 * Class TAG
 	 */
 	private static final String TAG = Escape.class.getSimpleName();
 	
 	/**
 	 * Screen ID
 	 */
 	public static final int SCREEN_NEW_GAME = 0;
 	public static final int SCREEN_VICTORY = 1;
 	public static final int SCREEN_LOST = 2;
 	public static final int SCREEN_EARTH = 3;
 	public static final int SCREEN_JUPITER = 4;
 	public static final int SCREEN_MOON = 5;
 	public static final int SCREEN_INTRO_EARTH = 6;
 	public static final int SCREEN_INTRO_JUPITER = 7;
 	public static final int SCREEN_INTRO_MOON = 8;
 	public static final int SCREEN_MENU = 9;
 	
 	/**
 	 * Player Model
 	 */
 	private final User user;
 	
 	/**
 	 * Engine Reference
 	 */
 	private Engine engine;
 	
 	/**
 	 * Current Screen
 	 */
 	private Screen screen;
 	
 	/**
 	 * Game's World
 	 */
 	private World world;
 	
 	/**
 	 * Screen
 	 */
 	private Victory victory;
 	private Lost lost;
 	private Menu menu;
 	private Error error;
 	
 	/**
 	 * StageScreen
 	 */
 	private Screen jupiter;
 	private Screen moon;
 	private Screen earth;
 	
 	/**
 	 * IntroScreen 
 	 */
 	private Screen introJupiter;
 	private Screen introMoon;
 	private Screen introEarth;
 	
 	/**
 	 * Overlay used ingame
 	 */
 	private IngameUI ingameUI;
 	
 	/**
 	 * Game Factory
 	 */
 	private ShipFactory shipFactory;
 	private ShotFactory shotFactory;
 	
 	/**
 	 * Game Entity Container
 	 */
 	private EntityContainer entityContainer;
 	
 	/**
 	 * Default Constructor for the Game.
 	 */
 	public Escape() {
 		this.user = new User(this);
 	}
 
 	/**
 	 * Called when {@link Engine} start
 	 */
 	public void create(Engine engine) {
 		try {
 			
 			Engine.debug(TAG, "Creating Escape Environment");
 			
 			// Set Engine
 			setEngine(engine);
 			
 			// Create World
 			World world = new World(new Vec2(0.0f,0.0f), false);
 			world.setContactListener(new CollisionDetector(getUser()));
 			setWorld(world);
 			
 			// Top Wall
 			createWall(world,engine.getConverter().toMeterX(getGraphics().getWidth() / 2),0.0f,false); 
 			// Bottom Wall
 			createWall(world,engine.getConverter().toMeterX(getGraphics().getWidth() / 2),engine.getConverter().toMeterY(getGraphics().getHeight()) + 2.0f,false);
 			// Left Wall
 			createWall(world,-1.0f,engine.getConverter().toMeterY(getGraphics().getHeight() / 2),true);
 			// Right Wall
 			createWall(world,11.0f,engine.getConverter().toMeterY(getGraphics().getHeight() / 2),true);
 			
 			// Create Entity Container
 			entityContainer = new EntityContainer(getEngine(), getWorld(), Math.max((int) (getGraphics().getWidth() * 0.1f), (int) (getGraphics().getHeight() * 0.1f)));
 			
 			// Create Game Components
 			ingameUI = new IngameUI();
 			shotFactory = new ShotFactory(getEngine(), getWorld(), getEntityContainer());
 			shipFactory = new ShipFactory(getEngine(), getEntityContainer(), getShotFactory());
 			
 			// Create Ship
 			createPlayerShip();
 			
 			// Create Gesture
 			createGestures();
 			
 			// Create Overlay
 			createOverlay();
 			
 			// Create Screen
 			createScreen();
 			
 			// Show Entry Screen
 			setScreenID(SCREEN_MENU);
 			
 		} catch(Exception e) {
 			error = new Error(this);
 			Engine.error(TAG, "An error has occurred during create()", e);
 			setScreen(error);
 		}
 	}
 
 	/**
 	 * Create a Hidden Wall for User Ship
 	 * 
 	 * @param world World to use.
 	 * @param x Position X
 	 * @param y Position Y
 	 * @param isRightOrLeft Is Right/Left or Top/Bottom
 	 */
 	private void createWall(World world, float x, float y, boolean isRightOrLeft) {
 		
 		Objects.requireNonNull(world);
 		
 		float shapeX = (isRightOrLeft) ? 1.0f : engine.getConverter().toMeterX(getGraphics().getWidth());
 		float shapeY = (isRightOrLeft) ? engine.getConverter().toMeterY(getGraphics().getHeight()) : 1.0f;
 		
 		
 		BodyDef bodyDef = new BodyDef();
 		bodyDef.position.set(x, y - 1.0f);
 		bodyDef.type = BodyType.STATIC;
 		
 		PolygonShape shape = new PolygonShape();
 		shape.setAsBox(shapeX, shapeY);
 		
 		FixtureDef fixture = new FixtureDef();
 		fixture.shape = shape;
 		fixture.density = 0.5f;
 		fixture.friction = 0.0f;      
 		fixture.restitution = 0.0f;
 		fixture.filter.categoryBits = Collisionable.WALL_TYPE;
 		fixture.filter.maskBits = Collisionable.PLAYER_TYPE;
 		
 		Body body = world.createBody(bodyDef);
 		body.createFixture(fixture);
 	}
 
 	@Override
 	public void render() {
 		if(screen != null) {
 			screen.render(getGraphics().getDeltaTime());
 		}
 		if(ingameUI != null) {
 			ingameUI.render(getGraphics().getDeltaTime());
 		}
 	}
 	
 	/**
 	 * @see Game#touch(Input)
 	 */
 	@Override
 	public boolean touch(Input i) {
 		if(getOverlay().touch(i)) {
 			return true;
 		}
 		return getScreen().touch(i);
 	}
 	
 	/**
 	 * @see Game#move(Input)
 	 */
 	@Override
 	public boolean move(Input i) {
 		if(getOverlay().move(i)) {
 			return true;
 		}
 		return getScreen().move(i);
 	}
 
 	/**
 	 * <p>
 	 * Return the Overlay used as Ingame UI.
 	 * 
 	 * @return {@link Overlay}
 	 */
 	public Overlay getOverlay() {
 		return ingameUI;
 	}
 	
 	/**
 	 * Retrieve the {@link User} in this Game.
 	 * 
 	 * @return {@link User}
 	 */
 	public User getUser() {
 		return user;
 	}
 	
 	/**
 	 * Set a new active {@link Screen} by this ID.
 	 * 
 	 * @param id {@link Screen} ID to display.
 	 */
 	public void setScreenID(int id) {
 		switch (id) {
 			case SCREEN_NEW_GAME: {
 				setScreen(introJupiter);
 				break;
 			}
 			case SCREEN_VICTORY: {
 				setScreen(victory);
 				break;
 			}
 			case SCREEN_LOST: {
 				setScreen(lost);
 				break;
 			}
 			case SCREEN_EARTH: {
 				setScreen(earth);
 				break;
 			}
 			case SCREEN_JUPITER: {
 				setScreen(jupiter);
 				break;
 			}
 			case SCREEN_MOON: {
 				setScreen(moon);
 				break;
 			}
 			case SCREEN_INTRO_EARTH: {
 				setScreen(introEarth);
 				break;
 			}
 			case SCREEN_INTRO_JUPITER: {
 				setScreen(introJupiter);
 				break;
 			}
 			case SCREEN_INTRO_MOON: {
 				setScreen(introMoon);
 				break;
 			}
 			case SCREEN_MENU: {
 				setScreen(menu);
 				break;
 			}
 			default: {
 				Engine.error(TAG, "Unknown Screen ID: "+id);
 				break;	
 			}
 			
 		}
 	}
 	
 	/**
 	 * Set a new active {@link Screen}.
 	 * 
 	 * @param screen {@link Screen} to display.
 	 */
 	public void setScreen(Screen screen) {
 		
 		if(this.screen != null) {
 			this.screen.hide();
 		}
 		
 		this.screen = screen;
 		
 		screen.show();
 	}
 
 	/**
 	 * Return the current active {@link Screen}.
 	 *
 	 * @return active {@link Screen}.
 	 */
 	public Screen getScreen() {
 		return screen;
 	}
 	
 	/**
 	 * Set the Game's {@link Engine}.
 	 * 
 	 * @param engine The Game's Engine
 	 */
 	private void setEngine(Engine engine) {
 		this.engine = engine;
 	}
 	
 	/**
 	 * Return the {@link Engine} which created this Game.
 	 * 
 	 * @return {@link Engine}.
 	 */
 	public Engine getEngine() {
 		return engine;
 	}
 	
 	/**
 	 * Return the {@link Graphics} for the Game.
 	 * 
 	 * @return {@link Graphics}
 	 */
 	public Graphics getGraphics() {
 		return getEngine().getGraphics();
 	}
 
 	/**
 	 * Return the {@link Resources} for the Game.
 	 * 
 	 * @return {@link Resources}
 	 */
 	public Resources getResources() {
 		return getEngine().getResources();
 	}
 	
 	/**
 	 * Return the Game's {@link World}
 	 * 
 	 * @return {@link World}
 	 */
 	public World getWorld() {
 		return world;
 	}
 	
 	/**
 	 * Set the Game's {@link World}
 	 * 
 	 * @param world Game's World
 	 */
 	public void setWorld(World world) {
 		this.world = world;
 	}
 
 	@Override
 	public void restart() {
 		
 		Screen screen = getScreen();
 		
 		// Check if we should display Intro Screen
 		if(screen == jupiter || screen == moon || screen == earth) {
 			
 			// Change the Screen by given ID
 			setScreenID(((Level) screen).getIntroScreen());
 			
 		} else {
 			
 			// Execute a Refresh with hide/show mechanism.
 			setScreen(screen);
 			
 		}
 	}
 
 	@Override
 	public void stop() {
 		
 		float x = engine.getConverter().toMeterX(getGraphics().getWidth() / 2);
 		float y = engine.getConverter().toMeterY(getGraphics().getHeight() - 100);
 		
 		getUser().reset(x, y);
 		setScreen(lost);
 		
 	}
 	
 	/**
 	 * Get the {@link ShipFactory}
 	 * 
 	 * @return {@link ShipFactory}
 	 */
 	public ShipFactory getShipFactory() {
 		return shipFactory;
 	}
 	
 	/**
 	 * Get the {@link ShotFactory}
 	 * 
 	 * @return {@link ShotFactory}
 	 */
 	public ShotFactory getShotFactory() {
 		return shotFactory;
 	}
 
 	/**
 	 * Get the {@link EntityContainer}
 	 * 
 	 * @return {@link EntityContainer}
 	 */
 	public EntityContainer getEntityContainer() {
 		return entityContainer;
 	}
 	
 	/**
 	 * Create a Player Ship
 	 */
 	private void createPlayerShip() {
 		
 		Engine.debug(TAG, "CREATE PLAYER SHIP AT : " + engine.getConverter().toMeterX(getGraphics().getWidth() / 2) + "/" + engine.getConverter().toMeterY(getGraphics().getHeight() - 100));
 		
 		Ship ship = getShipFactory().createPlayer(
 				engine.getConverter().toMeterX(getGraphics().getWidth() / 2), 
 				engine.getConverter().toMeterY(getGraphics().getHeight() - 100)
 		);
 		
 		ship.createBody(getWorld());
 		getUser().setShip(ship);
 	}
 	
 	/**
 	 * Create Gestures
 	 */
 	private void createGestures() {
 		
 		ArrayList<Gesture> gestures = new ArrayList<Gesture>();
 		
 		gestures.add(new Drift());
 		gestures.add(new Slide());
 		gestures.add(new Booster());
 		gestures.add(new Loop());
 		
 		getUser().setGestures(gestures);
 	}
 	
 	/**
 	 * Create Overlay
 	 */
 	private void createOverlay() {
 		
 		UIHighscore uHighscore = new UIHighscore(this);
 		UIArmorLife uLife = new UIArmorLife(this, getUser().getShip(), getUser());
 		UIWeapons uWeapons = new UIWeapons(this, getUser(), getUser().getAllWeapons(), getUser().getActiveWeapon());
 		
 		ingameUI.add(uHighscore);
 		ingameUI.add(uLife);
 		ingameUI.add(uWeapons);
 		
 		getUser().register(uHighscore);
 	}
 	
 	/**
 	 * Create Screen
 	 */
 	private void createScreen() {
 		
 		/**
 		 * Common Screen
 		 */
 		lost = new Lost(this);
 		menu = new Menu(this);
 		victory = new Victory(this);
 		
 		/**
 		 * Intro Screen
 		 */
 		introJupiter = new Intro(this, getResources().getTexture(TextureLoader.INTRO_JUPITER), SCREEN_JUPITER);
 		introMoon = new Intro(this, getResources().getTexture(TextureLoader.INTRO_MOON), SCREEN_MOON);
 		introEarth = new Intro(this, getResources().getTexture(TextureLoader.INTRO_EARTH), SCREEN_EARTH);
 		
 		/**
 		 * Level Screen
 		 */
		jupiter = new Level(this, new GameStage(getEngine(), getWorld(), getEntityContainer(), ScenarioLoader.JUPITER, getShipFactory(), 40, 0), 
 				new ScrollingTexture(getResources().getTexture(TextureLoader.BACKGROUND_JUPITER), true), 
 				SCREEN_INTRO_JUPITER, SCREEN_INTRO_MOON);
 		
		moon = new Level(this, new GameStage(getEngine(), getWorld(), getEntityContainer(), ScenarioLoader.MOON, getShipFactory(), 55, 1),
 				new ScrollingTexture(getResources().getTexture(TextureLoader.BACKGROUND_MOON), true),
 				SCREEN_INTRO_MOON, SCREEN_INTRO_EARTH);
 		
		earth = new Level(this, new GameStage(getEngine(), getWorld(), getEntityContainer(), ScenarioLoader.EARTH, getShipFactory(), 52, 2),
 				new ScrollingTexture(getResources().getTexture(TextureLoader.BACKGROUND_EARTH), true),
 				SCREEN_INTRO_EARTH, SCREEN_VICTORY);
 		
 	}
 	
 }
