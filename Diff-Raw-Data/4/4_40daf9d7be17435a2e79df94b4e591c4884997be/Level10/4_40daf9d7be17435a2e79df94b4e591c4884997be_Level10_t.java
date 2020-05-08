 package com.pix.mind.levels;
 
 import com.badlogic.gdx.graphics.Color;
 import com.pix.mind.PixMindGame;
 import com.pix.mind.actors.PlatformActivatorActor;
 import com.pix.mind.actors.StaticPlatformActor;
 import com.pix.mind.world.PixMindWorldRenderer;
 
 public class Level10 extends PixMindLevel {
 	public String levelTitle = "Level17";
 	PixMindGame game;
 	private static final int nActiveColors = 2;
 	public Level10(PixMindGame game) {
         super(game, 1280, 1024, 600, 6, 5, nActiveColors);
 		this.game = game;
 		levelNumber = 10;
 	}
 
 	@Override
 	public void show() {
 		super.show();
 		super.setNextLevel(game.getLevel11());
 		super.setActiveLevel(this);
 
 		// platform Actors and Activator Actors List
 		// Creating All Static Platforms
 		float platW = 1f;
 		float platH = 0.1f;
 
 		// Active colors
 		// Box2D platforms
 		// Add to platform list
 		// Black StaticPlatforms
 		box2D.getPlatformList().add(
                 new StaticPlatformActor(box2D.getWorld(), 1, 5, platW, platH, Color.BLACK, true));
 		box2D.getPlatformList().add(
                 new StaticPlatformActor(box2D.getWorld(), 1, 7, platW, platH, Color.BLACK, true));
 		box2D.getPlatformList().add(
                 new StaticPlatformActor(box2D.getWorld(), 12, 5, platW, platH, Color.BLACK, true));
 		box2D.getPlatformList().add(
                 new StaticPlatformActor(box2D.getWorld(), 12, 7, platW, platH, Color.BLACK, true));
 
 		// Coloured StaticPlatforms
 		box2D.getPlatformList().add(
                 new StaticPlatformActor(box2D.getWorld(), 6.5f, 4, 3.5f, platH, Color.BLUE, true));
 		box2D.getPlatformList().add(
                 new StaticPlatformActor(box2D.getWorld(), -2, 6, platW, platH, Color.GREEN, false));
 		box2D.getPlatformList().add(
                 new StaticPlatformActor(box2D.getWorld(), 15, 6, platW, platH, Color.GREEN, false));
 		box2D.getPlatformList().add(
                 new StaticPlatformActor(box2D.getWorld(), 4, 8, platW, platH, Color.RED, false));
 		box2D.getPlatformList().add(
                 new StaticPlatformActor(box2D.getWorld(), 9, 8, platW, platH, Color.GREEN, false));
	//	box2D.getPlatformList().add(
       //         new StaticPlatformActor(box2D.getWorld(), 15, 6, platW, platH, Color.GREEN, false));
 
 		// Creating All Activator
 		// Box2D Activator adding to activator list
 		box2D.getActivatorList().add(
             new PlatformActivatorActor(box2D.getWorld(), 6, 3, Color.BLACK, true));
 		box2D.getActivatorList().add(
             new PlatformActivatorActor(box2D.getWorld(), 3, 5.5f, Color.GREEN, false));
 		box2D.getActivatorList().add(
             new PlatformActivatorActor(box2D.getWorld(), 10, 5.5f, Color.RED, false));
 		box2D.getActivatorList().add(
             new PlatformActivatorActor(box2D.getWorld(), 6, 7, Color.BLUE, true));
 
 		// Add activators to Stage
 		for (PlatformActivatorActor Sskin : box2D.getActivatorList()) {
 			scene2D.getGroupStage().addActor(Sskin);
 		}
 
 		// Add platforms to Stage
 		for (StaticPlatformActor Sskin : box2D.getPlatformList()) {
 			scene2D.getGroupStage().addActor(Sskin);
 		}
 
 		// Rendering the game
 		box2D.addActivatedColor(Color.BLUE);
 		worldRenderer = new PixMindWorldRenderer(scene2D, box2D, gui);
 	}
 
 
 	@Override
 	public void render(float delta) {
 		super.render(delta);
 
 	}
 
 	@Override
 	public void resize(int width, int height) {
 		super.resize(width, height);
 	}
 
 	@Override
 	public void hide() {
 		super.hide();
 	}
 
 	@Override
 	public void pause() {
 		super.pause();
 	}
 
 	@Override
 	public void resume() {
 		super.resume();
 	}
 
 	@Override
 	public void dispose() {
 	}
 }
