 package com.pix.mind.levels;
 
 import com.badlogic.gdx.graphics.Color;
 import com.pix.mind.PixMindGame;
 import com.pix.mind.actors.PlatformActivatorActor;
 import com.pix.mind.actors.StaticPlatformActor;
 import com.pix.mind.world.PixMindWorldRenderer;
 
 public class Level11 extends PixMindLevel {
 	public String levelTitle = "Level13";
 	PixMindGame game;
 	private static final int nActiveColors = 2;
 
 	public Level11(PixMindGame game) {
 		super(game, 800, 1100, 900, 1, 6, nActiveColors);
 		this.game = game;
 		levelNumber = 11;
 	}
 
 	@Override
 	public void show() {
 
 		super.show();
 		super.setNextLevel(game.getLevel12());
 		super.setActiveLevel(this);
 		// platform Actors and Activator Actors List
 		// Creating All Static Platforms
 		float platW = 1f;
 		float platH = 0.1f;
 
         float deltaX = 0;
         float deltaY = 0;
 
 		// Box2D platforms
 		// Add to platform list
 		// Walls
 		//scene2D.getGroupStage().addActor(new StaticWallActor(box2D.getWorld(), 0, wallH, wallW, wallH));
 		//scene2D.getGroupStage().addActor(new StaticWallActor(box2D.getWorld(), 4, wallH + wallH / 4, wallW, 3 * wallH / 4));
 		//scene2D.getGroupStage().addActor(new StaticWallActor(box2D.getWorld(), 8, wallH, wallW, wallH));
 
 		// Normal StaticPlatforms
 		box2D.getPlatformList().add(
                 new StaticPlatformActor(box2D.getWorld(), 1 + deltaX, 5 + deltaY, platW, platH, Color.BLACK, true));
 		box2D.getPlatformList().add(
                 new StaticPlatformActor(box2D.getWorld(), 8.5f + deltaX, 6 + deltaY, platW, platH, Color.BLACK, true));
 		box2D.getPlatformList().add(
                 new StaticPlatformActor(box2D.getWorld(), 6.5f + deltaX, 5.5f + deltaY, platW/4, platH, Color.BLACK, true));
         
         // Coloured StaticPlatforms
         // 3 center bottom
 		box2D.getPlatformList().add(
                 new StaticPlatformActor(box2D.getWorld(), 5.5f + deltaX, 4 + deltaY, platW * 2, platH, Color.BLUE, false));
         //2 bottom small
 		box2D.getPlatformList().add(
                 new StaticPlatformActor(box2D.getWorld(), 6.5f + deltaX, 3.5f + deltaY, platW/2, platH, Color.RED, false));
         //4 middle center
 		box2D.getPlatformList().add(
                 new StaticPlatformActor(box2D.getWorld(), 6.5f + deltaX, 4.5f + deltaY, platW/2, platH, Color.ORANGE, false));
         // 3 center rightly
 		box2D.getPlatformList().add(
                 new StaticPlatformActor(box2D.getWorld(), 9f + deltaX, 5 + deltaY, platW, platH, Color.BLUE, false));
         //left 1 top
 		box2D.getPlatformList().add(
                 new StaticPlatformActor(box2D.getWorld(), 6.5f + deltaX, 7 + deltaY, platW, platH, Color.GREEN, false));
         //right 2 top
 		box2D.getPlatformList().add(
                 new StaticPlatformActor(box2D.getWorld(), 10.5f + deltaX, 7 + deltaY, platW, platH, Color.RED, false));
         //right 4 final top
 		box2D.getPlatformList().add(
                 new StaticPlatformActor(box2D.getWorld(), 11.5f + deltaX, 8 + deltaY, platW, platH, Color.ORANGE, false));
 
 		// Box2D Activator adding to activator list
         box2D.getActivatorList().add(
             new PlatformActivatorActor(box2D.getWorld(), 12f + deltaX,  9 + deltaY, Color.BLACK, true));
         box2D.getActivatorList().add(
             new PlatformActivatorActor(box2D.getWorld(), 3 + deltaX,  5f + deltaY, Color.BLUE, false));
         box2D.getActivatorList().add(
             new PlatformActivatorActor(box2D.getWorld(), 6.5f + deltaX,  3.8f + deltaY, Color.ORANGE, false));
         box2D.getActivatorList().add(
             new PlatformActivatorActor(box2D.getWorld(), 7f + deltaX,  8 + deltaY, Color.RED, false));
         box2D.getActivatorList().add(
            new PlatformActivatorActor(box2D.getWorld(), 7.7f + deltaX,  7.8f + deltaY, Color.GREEN, false));
 
 		// Add platforms to Stage
 		for (StaticPlatformActor Sskin : box2D.getPlatformList()) {
 			scene2D.getGroupStage().addActor(Sskin);
 		}
 
 		// Creating All Activator
 
 
 		// Add activators to Stage
 		for (PlatformActivatorActor Sskin : box2D.getActivatorList()) {
 			scene2D.getGroupStage().addActor(Sskin);
 		}
 		// Rendering the game
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
