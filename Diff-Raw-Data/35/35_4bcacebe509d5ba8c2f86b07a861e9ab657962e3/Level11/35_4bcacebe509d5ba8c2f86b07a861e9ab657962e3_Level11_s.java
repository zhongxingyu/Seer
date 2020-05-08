 package com.pix.mind.levels;
 
 import com.badlogic.gdx.graphics.Color;
 import com.pix.mind.PixMindGame;
 import com.pix.mind.actors.PlatformActivatorActor;
 import com.pix.mind.actors.StaticPlatformActor;
 import com.pix.mind.world.PixMindWorldRenderer;
 
 public class Level11 extends PixMindLevel {
 	public String levelTitle = "Level11";
 	PixMindGame game;
 	private static final int nActiveColors = 1;
 	public Level11(PixMindGame game) {
        super(game, 1280, 1024, 600, 14, 6, nActiveColors);		this.game = game;
 		levelNumber = 11;
 	}
 
 	@Override	public void show() {		super.show();		super.setNextLevel(game.getLevel12());		super.setActiveLevel(this);		// platform Actors and Activator Actors List		// Creating All Static Platforms		float platW = 1f;		float platH = 0.1f;
 
 		// Active colors		// Box2D platforms		// Add to platform list		// Black StaticPlatforms		box2D.getPlatformList().add(                new StaticPlatformActor(box2D.getWorld(), 13, 4, platW, platH, Color.BLACK, true));		box2D.getPlatformList().add(                new StaticPlatformActor(box2D.getWorld(), 13, 6, platW, platH, Color.BLACK, true));		box2D.getPlatformList().add(                new StaticPlatformActor(box2D.getWorld(), 13, 8, platW, platH, Color.BLACK, true));		box2D.getPlatformList().add(                new StaticPlatformActor(box2D.getWorld(), 10, 5, platW, platH, Color.BLACK, true));		box2D.getPlatformList().add(                new StaticPlatformActor(box2D.getWorld(), 10, 7, platW, platH, Color.BLACK, true));		box2D.getPlatformList().add(                new StaticPlatformActor(box2D.getWorld(), 10, 9, platW, platH, Color.BLACK, true));        box2D.getPlatformList().add(                new StaticPlatformActor(box2D.getWorld(), 4, 5, platW, platH, Color.BLACK, true));        box2D.getPlatformList().add(                new StaticPlatformActor(box2D.getWorld(), 2, 6, platW, platH, Color.BLACK, true));        box2D.getPlatformList().add(                new StaticPlatformActor(box2D.getWorld(), 2, 8, platW, platH, Color.BLACK, true));
 
		// Black StaticPlatforms		box2D.getPlatformList().add(                new StaticPlatformActor(box2D.getWorld(), 6, 4, 3*platW/4, platH, Color.RED, false));        box2D.getPlatformList().add(                new StaticPlatformActor(box2D.getWorld(), 0, 7, platW, platH, Color.GREEN, false));        box2D.getPlatformList().add(                new StaticPlatformActor(box2D.getWorld(), 4, 9, platW/2, platH, Color.BLUE, false));
 		// Creating All Activator		// Box2D Activator adding to activator list        box2D.getActivatorList().add(            new PlatformActivatorActor(box2D.getWorld(), 6, 8, Color.BLACK, true));
         box2D.getActivatorList().add(            new PlatformActivatorActor(box2D.getWorld(), 2, 8.5f, Color.BLUE, false));
         box2D.getActivatorList().add(            new PlatformActivatorActor(box2D.getWorld(), 13, 9, Color.RED, false));
         box2D.getActivatorList().add(            new PlatformActivatorActor(box2D.getWorld(), 3, 6.5f, Color.GREEN, false));
  		// Add platforms to Stage		for (StaticPlatformActor Sskin : box2D.getPlatformList()) {			scene2D.getGroupStage().addActor(Sskin);		}		worldRenderer = new PixMindWorldRenderer(scene2D, box2D, gui);
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
