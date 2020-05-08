 package com.pix.mind.levels;
 
 import com.badlogic.gdx.graphics.Color;
 import com.pix.mind.PixMindGame;
 import com.pix.mind.actors.PlatformActivatorActor;
 import com.pix.mind.actors.StaticPlatformActor;
 import com.pix.mind.world.PixMindWorldRenderer;
 
 public class Level16 extends PixMindLevel {
 	public String levelTitle = "Level21";
 	PixMindGame game;
 	private static final int nActiveColors = 3;
 
 	public Level16(PixMindGame game) {
 		super(game, 950, 1235, 1350, 6, 13.5f, nActiveColors);
 		this.game = game;
 		levelNumber = 16;
 	}
 
 	@Override
 	public void show() {
 
 		super.show();
 		super.setNextLevel(game.getLevel17());
 		super.setActiveLevel(this);
 		// platform Actors and Activator Actors List
 		// Creating All Static Platforms
 		float platW = 1f;
 		float platH = 0.1f;
         float deltaX = 0;
         float deltaY = 0;
 
 		// Active colors
 		// Box2D platforms
 		// Add to platform list
 		// Black StaticPlatforms
 
 		// Coloured StaticPlatforms
         // 1 = Blue
         // 2 = red
         // 3 = Green
         // 4 = Orange
         // from top to bottom
         // upper 4 small
         box2D.getPlatformList().add(
                 new StaticPlatformActor(box2D.getWorld(), 4f + deltaX, 11 + deltaY, platW/4, platH, Color.ORANGE, false));
         // upper 1 small
         box2D.getPlatformList().add(
                 new StaticPlatformActor(box2D.getWorld(), 4f + deltaX, 10 + deltaY, platW/4, platH, Color.BLUE, false));
         // upper 2 small
         box2D.getPlatformList().add(
                 new StaticPlatformActor(box2D.getWorld(), 5f + deltaX, 9.2f + deltaY, platW/4, platH, Color.RED, false));
         // middle big 3
         box2D.getPlatformList().add(
                new StaticPlatformActor(box2D.getWorld(), 6f + deltaX, 9 + deltaY, platW * 2, platH, Color.GREEN, false));
         // middle small 1 first 
         box2D.getPlatformList().add(
                 new StaticPlatformActor(box2D.getWorld(), 5f + deltaX, 8.2f + deltaY, platW/4, platH, Color.BLUE, false));
         // middle small 1 first inside
         box2D.getPlatformList().add(
                 new StaticPlatformActor(box2D.getWorld(), 4f + deltaX, 7f + deltaY, platW/4, platH/2, Color.BLUE, false));
         // middle medium 2 
         box2D.getPlatformList().add(
                 new StaticPlatformActor(box2D.getWorld(), 3.5f + deltaX, 7f + deltaY, platW, platH, Color.RED, false));
         // middle small 1 between 2 and 4
         box2D.getPlatformList().add(
                 new StaticPlatformActor(box2D.getWorld(), 5f + deltaX, 6f + deltaY, platW/4, platH, Color.BLUE, false));
         // middle small 1 second inside
         box2D.getPlatformList().add(
                 new StaticPlatformActor(box2D.getWorld(), 5.8f + deltaX, 5f + deltaY, platW/4, platH/2, Color.BLUE, false));
         // middle small 4 second cover
         box2D.getPlatformList().add(
                 new StaticPlatformActor(box2D.getWorld(), 6.5f + deltaX, 5f + deltaY, platW, platH, Color.ORANGE, false));
         // middle small 1 between 4 and 3
         box2D.getPlatformList().add(
                 new StaticPlatformActor(box2D.getWorld(), 5f + deltaX, 4f + deltaY, platW/4, platH, Color.BLUE, false));
         // bottom medium 3
         box2D.getPlatformList().add(
                 new StaticPlatformActor(box2D.getWorld(), 3.5f + deltaX, 3f + deltaY, platW, platH, Color.GREEN, false));
         // bottom small 1 inside 
         box2D.getPlatformList().add(
                 new StaticPlatformActor(box2D.getWorld(), 4f + deltaX, 3f + deltaY, platW/4, platH/2, Color.BLUE, false));
         // bottom small 1 middle
         box2D.getPlatformList().add(
                 new StaticPlatformActor(box2D.getWorld(), 5f + deltaX, 2f + deltaY, platW/4, platH, Color.BLUE, false));
         // bottom small 1 middle
         box2D.getPlatformList().add(
                 new StaticPlatformActor(box2D.getWorld(), 4f + deltaX, 1f + deltaY, platW/4, platH, Color.BLUE, false));
         // bottom thin 4 bottom
         box2D.getPlatformList().add(
                 new StaticPlatformActor(box2D.getWorld(), 3.5f + deltaX, 2.2f + deltaY, platW, platH/2, Color.ORANGE, false));
         // bottom thin 2 bottom
 //        box2D.getPlatformList().add(
 //                new StaticPlatformActor(box2D.getWorld(), 3.5f + deltaX, 2f + deltaY, platW, platH/2, Color.RED, false));
         // bottom thin 1 bottom
         box2D.getPlatformList().add(
                 new StaticPlatformActor(box2D.getWorld(), 3.5f + deltaX, 1.8f + deltaY, platW, platH/2, Color.BLUE, false));
 
 		// Creating All Activator
         box2D.getActivatorList().add(
             new PlatformActivatorActor(box2D.getWorld(), 4f + deltaX, 12.0f + deltaY, Color.BLACK, true));
         // 3 top
         box2D.getActivatorList().add(
            new PlatformActivatorActor(box2D.getWorld(), 6f + deltaX, 12.0f + deltaY, Color.GREEN, false));
         // 2 upper
         box2D.getActivatorList().add(
             new PlatformActivatorActor(box2D.getWorld(), 3f + deltaX, 9.2f + deltaY, Color.RED, false));
         // 4 middle
         box2D.getActivatorList().add(
             new PlatformActivatorActor(box2D.getWorld(), 6f + deltaX, 6f + deltaY, Color.ORANGE, false));
         // 3 middle left
         box2D.getActivatorList().add(
             new PlatformActivatorActor(box2D.getWorld(), 4f + deltaX, 4.0f + deltaY, Color.GREEN, false));
         // 1 bottom
         box2D.getActivatorList().add(
             new PlatformActivatorActor(box2D.getWorld(), 4f + deltaX, 1.5f + deltaY, Color.BLUE, false));
 
 		// Add activators to Stage
 		for (PlatformActivatorActor Sskin : box2D.getActivatorList()) {
 			scene2D.getGroupStage().addActor(Sskin);
 		}
 
 		// Add platforms to Stage
 		for (StaticPlatformActor Sskin : box2D.getPlatformList()) {
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
