 package com.team14;
 
 /**
  * TODO
  * 
  * For next release
  * ----------------
  * - Clean up render(), break logical chunks into their own methods 
  * - Clean up or eliminate Razorback.java, turns out we might not need it
  * - Splash, high score, and instruction screens
  * - To do high score screen, we need a scoring mechanism
  * - Implement dash timer
  * - We have jump, now need double jump
  * - Implement sprite animation,
  *   	tutorial: http://code.google.com/p/libgdx/wiki/SpriteAnimation
  * 
  * To implement soon-ish
  * ---------------------
  * - New test map with platforms and gaps to test for losing a life at bottom
  * - Add overlaid killable objects where collidable tiles are, or change
  *   	collision detection for vertical surfaces to notify of life lost?
  *
  *   ... OR OR OR ...
  *   
  * - Detect forward velocity on render. If we go slower than the original
  *   	velocity (or dash velocity in dash mode), we die.
  *      I don't know why I didn't think of this before.
  *
  * - Create class and sprite for breakable object
  * - If we do premapped levels instead of a neverending random level, 
  * 		determine when we reach end of level for victory. 
  * - Tests tests tests tests tests tests
  * 
  * Completed
  * ---------
  * - Fix so camera doesn't change frame of reference, ie razorback appears to
  *		stay in same x coordinate 
  * 
  * Eventually...
  * -------------
  * - Music + sound effects
  * - Create a decent level map, at least 1000 tiles long?
  */
 
 /**
  *   Copyright 2011 David Kirchner dpk@dpk.net
  *
  *   Licensed under the Apache License, Version 2.0 (the "License");
  *   you may not use this file except in compliance with the License.
  *   You may obtain a copy of the License at
  *
  *       http://www.apache.org/licenses/LICENSE-2.0
  *
  *   Unless required by applicable law or agreed to in writing, software
  *   distributed under the License is distributed on an "AS IS" BASIS,
  *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *   See the License for the specific language governing permissions and
  *   limitations under the License.
  * 
  * Source:
  * http://dpk.net/2011/05/08/libgdx-box2d-tiled-maps-full-working-example-part-2/
  */
 
 import com.badlogic.gdx.ApplicationListener;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Input;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.Texture.TextureFilter;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.Body;
 import com.badlogic.gdx.physics.box2d.BodyDef;
 import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
 import com.badlogic.gdx.physics.box2d.FixtureDef;
 import com.badlogic.gdx.physics.box2d.PolygonShape;
 import com.badlogic.gdx.physics.box2d.World;
 
 public class Game implements ApplicationListener {
 	/**
 	 * The time the last frame was rendered, used for throttling framerate
 	 */
 	private long lastRender;
 
 	private TiledMapHelper tiledMapHelper;
 
 	/**
 	 * The libgdx SpriteBatch -- used to optimize sprite drawing.
 	 */
 	private SpriteBatch spriteBatch;
 
 	/**
 	 * This is the main box2d "container" object. All bodies will be loaded in
 	 * this object and will be simulated through calls to this object.
 	 */
 	private World world;
 
 	/**
 	 * This is the player character. It will be created as a dynamic object.
 	 */
     public Razorback razorback;
 
 	/**
 	 * This box2d debug renderer comes from libgdx test code. It draws lines
 	 * over all collision boundaries, so it is immensely useful for verifying
 	 * that the world collisions are as you expect them to be. It is, however,
 	 * slow, so only use it for testing.
 	 */
     // Remove me for final release
 	private Box2DDebugRenderer debugRenderer;
 
 	/**
 	 * Box2d works best with small values. If you use pixels directly you will
 	 * get weird results -- speeds and accelerations not feeling quite right.
 	 * Common practice is to use a constant to convert pixels to and from
 	 * "meters".
 	 */
 	public static final float PIXELS_PER_METER = 60.0f;
 
 	/**
 	 * The screen's width and height. This may not match that computed by
 	 * libgdx's gdx.graphics.getWidth() / getHeight() on devices that make use
 	 * of on-screen menu buttons.
 	 */
 	private int screenWidth;
 	private int screenHeight;
 	
 	/**
 	 * Game variables, states, and constants
 	 */
 	private boolean gameOver = false;
     private int level = 1;
     private int lives;
     private boolean reset = true;
 
 	public Game(int currLevel, int livesLeft) {
 		super();
 
 		// Defer until create() when Gdx is initialized.
 		screenWidth = -1;
 		screenHeight = -1;
 
         level = currLevel;
         lives = livesLeft;
 	}
 
 	public Game(int width, int height, int currLevel, int livesLeft) {
 		super();
 
 		screenWidth = width;
 		screenHeight = height;
 
         level = currLevel;
         lives = livesLeft;
 	}
 
 	@Override
 	public void create() {
 		/**
 		 * If the viewport's size is not yet known, determine it here.
 		 */
 		if (screenWidth == -1) {
 			screenWidth = Gdx.graphics.getWidth();
 			screenHeight = Gdx.graphics.getHeight();
 		}
 
 		tiledMapHelper = new TiledMapHelper();
 		tiledMapHelper = new TiledMapHelper();
 		tiledMapHelper.setPackerDirectory("assets/tiles");
 		tiledMapHelper.loadMap("assets/tiles/testmap.tmx");
 		tiledMapHelper.prepareCamera(screenWidth, screenHeight);
 
 		spriteBatch = new SpriteBatch();
 
 		/**
 		 * You can set the world's gravity in its constructor. Here, the gravity
 		 * is negative in the y direction (as in, pulling things down).
 		 */
 		world = new World(new Vector2(0.0f, -10.0f), true);
 
         razorback = Razorback.getInstance(world, lives);
 
 
 		tiledMapHelper.loadCollisions("assets/collisions.txt", world,
 				PIXELS_PER_METER);
 
 		debugRenderer = new Box2DDebugRenderer();
 
 		lastRender = System.nanoTime();
 	}
 
 	@Override
 	public void resume() {
 	}
 
 	@Override
 	public void render() {
 		long now = System.nanoTime();
 
		if (Gdx.input.isKeyPressed(Input.Keys.DPAD_UP))
             razorback.jump();
 		
		if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT))
            razorback.dash();
 		
 		/**
 		 * Have box2d update the positions and velocities (and etc) of all
 		 * tracked objects. The second and third argument specify the number of
 		 * iterations of velocity and position tests to perform -- higher is
 		 * more accurate but is also slower.
 		 */
 		world.step(Gdx.app.getGraphics().getDeltaTime(), 3, 3);
 
 		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 		/**
 		 * A nice(?), blue backdrop.
 		 */
 		Gdx.gl.glClearColor(0, 0.5f, 0.9f, 0);
 
 		/**
 		 * The camera is now controlled primarily by the position of the main
 		 * character, and secondarily by the map boundaries.
 		 */
 
 		tiledMapHelper.getCamera().position.x = PIXELS_PER_METER
 				* razorback.getXPosition() + 300;
 		
 		/**
 		 * Ensure that the camera is only showing the map, nothing outside.
 		 */
 		if (tiledMapHelper.getCamera().position.x < Gdx.graphics.getWidth() / 2) {
 			tiledMapHelper.getCamera().position.x = Gdx.graphics.getWidth() / 2;
 		}
 		if (tiledMapHelper.getCamera().position.x >= tiledMapHelper.getWidth()
 				- Gdx.graphics.getWidth() / 2) {
 			tiledMapHelper.getCamera().position.x = tiledMapHelper.getWidth()
 					- Gdx.graphics.getWidth() / 2;
 		}
 
 		if (tiledMapHelper.getCamera().position.y < Gdx.graphics.getHeight() / 2) {
 			tiledMapHelper.getCamera().position.y = Gdx.graphics.getHeight() / 2;
 		}
 		if (tiledMapHelper.getCamera().position.y >= tiledMapHelper.getHeight()
 				- Gdx.graphics.getHeight() / 2) {
 			tiledMapHelper.getCamera().position.y = tiledMapHelper.getHeight()
 					- Gdx.graphics.getHeight() / 2;
 		}
 
 		tiledMapHelper.getCamera().update();
 		tiledMapHelper.render();
 
 		/**
 		 * Prepare the SpriteBatch for drawing.
 		 */
 		spriteBatch.setProjectionMatrix(tiledMapHelper.getCamera().combined);
 		spriteBatch.begin();
 
         razorback.move(spriteBatch);
 
        /*
         * - THIS IS NOW TO BE IN razorback.move() - NOT TESTED YET.
		razorback.sprite.setPosition(
				PIXELS_PER_METER * razorback.getXPosition
						- razorback.sprite.getWidth() / 2,
				PIXELS_PER_METER * razorback.getYPosition
						- razorback.sprite.getHeight() / 2);
		razorbackSprite.draw(spriteBatch);
        */

 		/**
 		 * "Flush" the sprites to screen.
 		 */
 		spriteBatch.end();
 
 		/**
 		 * Draw this last, so we can see the collision boundaries on top of the
 		 * sprites and map.
 		 */
 		debugRenderer.render(world, tiledMapHelper.getCamera().combined.scale(
 				Game.PIXELS_PER_METER,
 				Game.PIXELS_PER_METER,
 				Game.PIXELS_PER_METER));
 
 		now = System.nanoTime();
 		if (now - lastRender < 30000000) { // 30 ms, ~33FPS
 			try {
 				Thread.sleep(30 - (now - lastRender) / 1000000);
 			} catch (InterruptedException e) {
 			}
 		}
 
 		lastRender = now;
 		
 		/**
 		 * Now, at the end, did we lose speed by hitting something?
 		 */
 //		if (razorback.getXVelocity() < normalXVelocity)
 //			loseLife();
 		//System.out.println("Lives: " + lives + ", xvel: " + getXVelocity() + ", origxvel: " + normalXVelocity);
 	}
 
 	@Override
 	public void resize(int width, int height) {
 	}
 
 	@Override
 	public void pause() {
 	}
 
 	@Override
 	public void dispose() {
 	}
 
 	/**
 	 * Methods related to game over status.
 	 * There's no need for an outside source to twiddle with our gameover status.
 	 */
 	public boolean isGameOver() {
 		return gameOver;
 	}
 	
 	public float getScore() {
 		return razorback.getXPosition();
 	}
 
 }
