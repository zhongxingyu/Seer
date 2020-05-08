 package com.pix.mind.levels;
 
 
 import com.badlogic.gdx.graphics.Color;
 import com.pix.mind.PixMindGame;
 import com.pix.mind.actors.PlatformActivatorActor;
 import com.pix.mind.actors.StaticPlatformActor;
 import com.pix.mind.box2d.bodies.PlatformActivator;
 import com.pix.mind.box2d.bodies.StaticPlatform;
 import com.pix.mind.world.PixMindWorldRenderer;
 
 public class LevelOne extends PixMindLevel {
 
 	public String levelTitle = "LevelOne";
 	PixMindGame game;
 	private static final int nActiveColors = 2;
 	public LevelOne(PixMindGame game) {
 		super(game, 1333, 1000, 210, 4, 4, nActiveColors);
 		this.game = game;
 
 		// TODO Auto-generated constructor stub
 	}
 
 	@Override
 	public void show() {
 		// TODO Auto-generated method stub
 
 		super.show();
 		super.setNextLevel(game.getLevelTwo());
 		super.setActiveLevel(this);
 		// CREANDO EL NIVEL
 
 		// platform Actors and Activator Actors List
 
 		// Creating All Static Platforms
 
 		// Box2D platforms
 		StaticPlatform sPlatform = new StaticPlatform(box2D.getWorld(), 8, 5,
 				1f, 0.1f);
 		StaticPlatform s2Platform = new StaticPlatform(box2D.getWorld(), 3, 2,
 				1, 0.1f);
 		StaticPlatform s3Platform = new StaticPlatform(box2D.getWorld(), 5, 3,
 				1, 0.1f);
 		StaticPlatform s4Platform = new StaticPlatform(box2D.getWorld(), 6, 4,
 				1, 0.1f);
 		StaticPlatform s5Platform = new StaticPlatform(box2D.getWorld(), 1, 1f,
 				1, 0.1f);
 		StaticPlatform s6Platform = new StaticPlatform(box2D.getWorld(), 2, 3,
 				1, 0.1f);
 		StaticPlatform s7Platform = new StaticPlatform(box2D.getWorld(), 1.5f,
 				4.2f, 1, 0.1f);
 		StaticPlatform s8Platform = new StaticPlatform(box2D.getWorld(), -5, 0,
 				35, 0.1f);
 
 		// Actor Platforms
 		StaticPlatformActor s1Skin = new StaticPlatformActor(sPlatform,
 				Color.RED, false);
 		StaticPlatformActor s2Skin = new StaticPlatformActor(s2Platform,
 				Color.BLUE, true);
 		StaticPlatformActor s3Skin = new StaticPlatformActor(s3Platform,
 				Color.GREEN, false);
 		StaticPlatformActor s4Skin = new StaticPlatformActor(s4Platform,
 				Color.BLACK, true);
 		StaticPlatformActor s5Skin = new StaticPlatformActor(s5Platform,
 				Color.RED, false);
 		StaticPlatformActor s6Skin = new StaticPlatformActor(s6Platform,
 				Color.BLACK, true);
 		StaticPlatformActor s7Skin = new StaticPlatformActor(s7Platform,
 				Color.BLACK, true);
 		StaticPlatformActor s8Skin = new StaticPlatformActor(s8Platform,
 				Color.BLACK, true);
 
 		// Add platforms to Stage
 
 		scene2D.getGroupStage().addActor(s1Skin);
 		scene2D.getGroupStage().addActor(s2Skin);
 		scene2D.getGroupStage().addActor(s3Skin);
 		scene2D.getGroupStage().addActor(s4Skin);
 		scene2D.getGroupStage().addActor(s5Skin);
 		scene2D.getGroupStage().addActor(s6Skin);
 		scene2D.getGroupStage().addActor(s7Skin);
 		scene2D.getGroupStage().addActor(s8Skin);
 
 		// Add to platform list
 
 		box2D.getPlatformList().add(s1Skin);
 		box2D.getPlatformList().add(s2Skin);
 		box2D.getPlatformList().add(s3Skin);
 		box2D.getPlatformList().add(s4Skin);
 		box2D.getPlatformList().add(s5Skin);
 		box2D.getPlatformList().add(s6Skin);
 		box2D.getPlatformList().add(s7Skin);
 		box2D.getPlatformList().add(s8Skin);
 
 		// Creating All Activator
 
 		// Box2D Activator
 
 		PlatformActivator pActivator = new PlatformActivator(box2D.getWorld(),
 				0, 5, 0.2f);
 		PlatformActivator p2Activator = new PlatformActivator(box2D.getWorld(),
 				8, 6, 0.2f);
 		PlatformActivator p3Activator = new PlatformActivator(box2D.getWorld(),
 				0, 2, 0.2f);
 		PlatformActivator p4Activator = new PlatformActivator(box2D.getWorld(),
 				2, 5, 0.2f);
 		PlatformActivator p5Activator = new PlatformActivator(box2D.getWorld(),
 				1, 2, 0.2f);
 
 		// Actor Activator
 
 		PlatformActivatorActor a1Skin = new PlatformActivatorActor(pActivator,
 				Color.RED, false);
 		PlatformActivatorActor a2Skin = new PlatformActivatorActor(p2Activator,
 				Color.BLACK, true);
 		PlatformActivatorActor a3Skin = new PlatformActivatorActor(p3Activator,
 				Color.GREEN, false);
 		PlatformActivatorActor a4Skin = new PlatformActivatorActor(p4Activator,
 				Color.BLUE, true);
 		PlatformActivatorActor a5Skin = new PlatformActivatorActor(p5Activator,
 				Color.BLUE, true);
 
 		// Add activators to Stage
 
 		scene2D.getGroupStage().addActor(a1Skin);
 		scene2D.getGroupStage().addActor(a2Skin);
 		scene2D.getGroupStage().addActor(a3Skin);
 		scene2D.getGroupStage().addActor(a4Skin);
 		scene2D.getGroupStage().addActor(a5Skin);
 
 		// Add to activator list
 
 		box2D.getActivatorList().add(a1Skin);
 		box2D.getActivatorList().add(a2Skin);
 		box2D.getActivatorList().add(a3Skin);
 		box2D.getActivatorList().add(a4Skin);
 		box2D.getActivatorList().add(a5Skin);
 
 		// add to stage the group of actors
 
 		// Active colors
 
 		// Rendering the game
//		box2D.addActivatedColor(Color.BLUE);
 		worldRenderer = new PixMindWorldRenderer(scene2D, box2D, gui);
 
 		
 		
 
 	}
 
 	@Override
 	public void render(float delta) {
 		super.render(delta);
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.pix.mind.levels.PixMindLevel#resize(int, int)
 	 */
 	@Override
 	public void resize(int width, int height) {
 		// TODO Auto-generated method stub
 		super.resize(width, height);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.pix.mind.levels.PixMindLevel#hide()
 	 */
 	@Override
 	public void hide() {
 		// TODO Auto-generated method stub
 		super.hide();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.pix.mind.levels.PixMindLevel#pause()
 	 */
 	@Override
 	public void pause() {
 		// TODO Auto-generated method stub
 		super.pause();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.pix.mind.levels.PixMindLevel#resume()
 	 */
 	@Override
 	public void resume() {
 		// TODO Auto-generated method stub
 		super.resume();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.pix.mind.levels.PixMindLevel#dispose()
 	 */
 	@Override
 	public void dispose() {
 		// TODO Auto-generated method stub
 	}
 
 }
