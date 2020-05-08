 package com.picotech.lightrunnerlibgdx;
 
 import java.util.ArrayList;
 import java.util.Random;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.g2d.BitmapFont;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
 import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
 import com.badlogic.gdx.math.Intersector;
 import com.badlogic.gdx.math.MathUtils;
 import com.badlogic.gdx.math.Rectangle;
 import com.badlogic.gdx.math.Vector2;
 import com.picotech.lightrunnerlibgdx.GameScreen.GameState;
 
 /**
  * The World class holds all of the players, enemies and environment objects. It
  * handles collisions and drawing methods, as well as loading content.
  */
 
 public class World {
 
 	enum MenuState {
 		PLAY, CHOOSESIDE
 	}
 
 	MenuState menuState = MenuState.PLAY;
 	Player player;
 	Mirror mirror;
 	Light light;
 
 	BitmapFont bf;
 
 	float deltaTime, totalTime;
 	float loadedContentPercent;
 
 	int enemiesKilled;
 	int score;
 	int level;
 	int powerupf = 2000;
 
 	Vector2 ENEMY_VEL;
 	Vector2 LightSource;
 
 	Rectangle playButton;
 	Rectangle topButton, rightButton, bottomButton;
 
 	boolean menuScreen;
 	boolean playSelected;
 	boolean controlsSelected;
 	boolean isClearScreen = false;
 	boolean slowActivated = false;
 	boolean playedSound = false;
 	ArrayList<Enemy> enemies;
 	ArrayList<Enemy> enemiesAlive;
 
 	ArrayList<Powerup> powerups;
 
 	Color healthBar;
 	/**
 	 * There are two types of worlds, the menu world and the in-game world. The
 	 * behavior of the light depends on whether the game is in the menu or
 	 * playing state.
 	 * 
 	 * @param isMenu
 	 */
 	public World(boolean isMenu) {
 		level = 1;
 		totalTime = 0;
 
 		playButton = new Rectangle(390, 100, 500, 100);
 
 		topButton = new Rectangle(190, 100, 300, 100);
 		rightButton = new Rectangle(490, 100, 300, 100);
 		bottomButton = new Rectangle(790, 100, 300, 100);
 
 		enemies = new ArrayList<Enemy>();
 		enemiesAlive = new ArrayList<Enemy>();
 		powerups = new ArrayList<Powerup>();
 
 		menuScreen = isMenu;
 		player = new Player(new Vector2(0, 300), "characterDirection0.png");
 		mirror = new Mirror(new Vector2(100, 300), "mirror.png");
 
 		if (menuScreen) {
 			player = new Player(new Vector2(-100, -100), "characterDirection0.png");
 			light = new Light(true);
 			level = 40;
 		} else {
 			setLight();
 			healthBar = new Color();
 		}
 			
 
 		// Spawning enemies
 		for (int i = 0; i < level; i++)
 			enemies.add(new Enemy(new Vector2(MathUtils.random(300, 1250),
 					MathUtils.random(0, 700)), 50, 50, level));
 
 		// Power-ups
 		if (!menuScreen) {
 			powerups.add(new Powerup(new Vector2(1200, 400),
 					Powerup.Type.ENEMYSLOW, 10));
 			for (Powerup pu : powerups)
 				pu.loadContent();
 			powerupf = MathUtils.random(15, 20);
 		}
 	}
 
 	private void setLight() {
 		Random r = new Random();
 		if (GameScreen.scheme == GameScreen.LightScheme.TOP) {
 			LightSource = new Vector2(r.nextInt(640) + 420, 720);
 		} else if (GameScreen.scheme == GameScreen.LightScheme.RIGHT) {
 			LightSource = new Vector2(1280, r.nextInt(700 + 10));
 		} else if (GameScreen.scheme == GameScreen.LightScheme.BOTTOM) {
 			LightSource = new Vector2(r.nextInt(640) + 420, 0);
 		}
 
 		light = new Light(LightSource, mirror.getCenter());
 	}
 
 	/**
 	 * Loads all the content of the World.
 	 */
 	public void loadContent() {
 		player.loadContent();
 		mirror.loadContent();
 
 		for (Powerup pu : powerups) {
 			pu.loadContent();
 		}
 
 		bf = new BitmapFont();
 		bf.scale(1);
 		bf.setColor(Color.WHITE);
 	}
 
 	/**
 	 * Updates the entire World. Includes light, enemy movement, and enemy
 	 * destruction. Also updates the time functions for frame rate-independent
 	 * functions deltaTime and totalTime (which are all in seconds).
 	 */
 	public void update() {
 		// Miscellaneous time updating functions.
 		deltaTime = Gdx.graphics.getDeltaTime();
 		totalTime += deltaTime;
 
 		// Updating light, player, and the mirror.
 		light.update(mirror, player);
 		player.update();
 		mirror.rotateAroundPlayer(player.getCenter(), (player.bounds.width / 2)
 				+ 2 + (light.getOutgoingBeam().isPrism ? 40 : 0));
 
 		// Updates all enemies in "enemies".
 		for (Enemy e : enemies) {
 			e.update();
 			for(int beam = 1; beam < light.beams.size(); beam++){
 				if (Intersector.overlapConvexPolygons(
 						light.beams.get(beam).beamPolygon, e.p)) {
 					if (e.alive) {
 						e.health--;
 						e.losingHealth = true;
 						Assets.hit.play(.1f);
 					} else {
 						enemiesKilled++;
 					}
 				}
 				if( Intersector.overlapConvexPolygons(player.p, e.p)){
 					if(player.alive)
 						player.health--;
 				}
 			}
 			// adds the number of enemies still alive to a new ArrayList
 			if (e.alive) {
 				enemiesAlive.add(e);
 				e.isSlow = slowActivated;
 			}
 
 		}
 
 		// Depending on the MenuState, it will either show the Play
 		// button or the Top-Right-Bottom buttons.
 		float dstX = light.getOutgoingBeam().dst.x;
 		if (menuState == MenuState.CHOOSESIDE) {
 			if (dstX > 17 && dstX < 433) {
 				GameScreen.scheme = GameScreen.LightScheme.TOP;
 				GameScreen.selectedScheme = GameScreen.LightScheme.TOP;
 				controlsSelected = true;
				if (!playedSound) {
					Assets.blip.play(1.0f);
					playedSound = true;
				}
 			} else if (dstX > 465 && dstX < 815) {
 				GameScreen.scheme = GameScreen.LightScheme.RIGHT;
 				GameScreen.selectedScheme = GameScreen.LightScheme.RIGHT;
 				controlsSelected = true;
				if (!playedSound) {
					Assets.blip.play(1.0f);
					playedSound = true;
				}
 			} else if (dstX > 847 && dstX < 1200) {
 				GameScreen.scheme = GameScreen.LightScheme.BOTTOM;
 				GameScreen.selectedScheme = GameScreen.LightScheme.BOTTOM;
 				controlsSelected = true;
				if (!playedSound) {
					Assets.blip.play(1.0f);
					playedSound = true;
				}
 			} else {
 				controlsSelected = false;
 				playedSound = false;
 			}
 		}
 		if (menuState == MenuState.PLAY) {
 			if (dstX > playButton.x - 100
					&& dstX < playButton.x + playButton.width + 100)
 				playSelected = true;
			else
 				playSelected = false;
 		}
 
 		// removes the "dead" enemies from the main ArrayList
 		enemies.retainAll(enemiesAlive);
 		enemiesAlive.clear();
 
 		// temporarily spawns new enemies, which get progressively faster
 		if (enemies.size() < level) {
 			enemies.add(new Enemy(new Vector2(1280, MathUtils.random(0, 700)),
 					50, 50, level));
 			enemies.get(enemies.size() - 1).isSlow = slowActivated;
 		}
 
 		// Time-wise level changing
 		if (totalTime > 5 * level)
 			level++;
 
 		setScore();
 
 		// Tried out Intersector, didn't work.
 		// if (Intersector.overlapConvexPolygons(pu.p, player.p)) {
 		// Trying out manual checks.
 		updatePowerups();
 
 	}
 
 	public void setScore() {
 		// Score algorithm
 		score = (int) (totalTime * 10 + enemiesKilled * 5);
 	}
 
 	/**
 	 * Handles all the power-up logic.
 	 */
 	private void updatePowerups() {
 		// Randomizing spawns
 		if ((int) (totalTime * 100) % powerupf == 0) {
 			powerups.add(new Powerup(new Vector2(1300,
 					MathUtils.random(600) + 50), Powerup.Type.CLEARSCREEN, 10));
 			powerups.get(powerups.size() - 1).loadContent();
 			powerupf = MathUtils.random(1500, 2000);
 		}
 
 		for (int i = 0; i < powerups.size(); i++) {
 			Powerup pu = powerups.get(0);
 			pu.update(deltaTime);
 
 			// Collision with player
 			if (pu.position.x < player.position.x + player.bounds.width
 					&& pu.position.y + pu.bounds.height > player.position.y
 					&& pu.position.y < player.position.y + player.bounds.height) {
 
 				switch (pu.type) {
 				case LIGHTMODIFIER:
 					light.getOutgoingBeam().setWidth(Powerup.LM_WIDTH);
 					break;
 				case PRISMPOWERUP:
 					GameScreen.scheme = GameScreen.LightScheme.LEFT;
 					light.getOutgoingBeam().setWidth(Powerup.P_WIDTH);
 					light.getOutgoingBeam().isPrism = true;
 
 					mirror.asset = "prism.png";
 					mirror.loadContent();
 					break;
 				case ENEMYSLOW:
 					slowActivated = true;
 					for (Enemy e : enemies) {
 						e.isSlow = true;
 					}
 					for (Enemy e : enemiesAlive) {
 						e.isSlow = true;
 					}
 					break;
 				case CLEARSCREEN:
 					isClearScreen = true;
 					for (int j = 0; j < enemies.size(); j++) {
 						if (enemies.get(j).alive)
 							enemiesKilled++;
 					}
 					setScore();
 					break;
 				}
 				pu.isActive = true;
 				pu.position = new Vector2(10000, 10000);
 			}
 
 			// Ending power-ups
 			if (pu.timeActive > pu.timeOfEffect) {
 				pu.end();
 
 				switch (pu.type) {
 				case LIGHTMODIFIER:
 					light.getOutgoingBeam().setWidth(Light.L_WIDTH);
 					break;
 				case PRISMPOWERUP:
 					GameScreen.scheme = GameScreen.selectedScheme;
 					setLight();
 					light.getOutgoingBeam().setWidth(Light.L_WIDTH);
 					light.getOutgoingBeam().isPrism = false;
 					mirror.asset = "mirror.png";
 					mirror.loadContent();
 					break;
 				case ENEMYSLOW:
 					slowActivated = false;
 					for (Enemy e : enemies) {
 						e.isSlow = false;
 					}
 					for (Enemy e : enemiesAlive) {
 						e.isSlow = false;
 					}
 					break;
 				case CLEARSCREEN:
 					isClearScreen = false;
 					break;
 				}
 
 				powerups.remove(i);
 			}
 		}
 		if (isClearScreen) {
 			enemiesAlive.clear();
 			enemies.clear();
 		}
 	}
 
 	/**
 	 * Draws the entire world. This includes:
 	 * <ul>
 	 * <li>menu screen
 	 * <li>all enemies
 	 * <li>player
 	 * <li>mirror
 	 * <li>text
 	 * <li>light
 	 * <li>powerups
 	 * </ul>
 	 * 
 	 * @param batch
 	 * @param sr
 	 */
 	public void draw(SpriteBatch batch, ShapeRenderer sr) {
 
 		for (Enemy e : enemies)
 			e.draw(sr);
 
 		if (menuScreen) { // this draws all the graphics for the menu
 			if (menuState == MenuState.PLAY) {
 				sr.begin(ShapeType.FilledRectangle);
 				if (playSelected)
 					sr.setColor(Color.WHITE);
 				else
 					sr.setColor(Color.LIGHT_GRAY);
 				sr.filledRect(playButton.x, playButton.y, playButton.width,
 						playButton.height);
 				sr.end();
 				batch.begin();
 				bf.setColor(Color.BLACK);
 				bf.draw(batch, "Play", 610, 160);
 				batch.end();
 			} else if (menuState == MenuState.CHOOSESIDE) {
 				sr.begin(ShapeType.FilledRectangle);
 				sr.setColor(Color.LIGHT_GRAY);
 				sr.filledRect(topButton.x, topButton.y, topButton.width,
 						topButton.height);
 				sr.filledRect(rightButton.x, rightButton.y, rightButton.width,
 						rightButton.height);
 				sr.filledRect(bottomButton.x, bottomButton.y,
 						bottomButton.width, bottomButton.height);
 				if (GameScreen.scheme != GameScreen.LightScheme.NONE) {
 					sr.setColor(Color.WHITE);
 					if (GameScreen.scheme == GameScreen.LightScheme.TOP) {
 						sr.filledRect(topButton.x, topButton.y,
 								topButton.width, topButton.height);
 					} else if (GameScreen.scheme == GameScreen.LightScheme.RIGHT) {
 						sr.filledRect(rightButton.x, rightButton.y,
 								rightButton.width, rightButton.height);
 					} else if (GameScreen.scheme == GameScreen.LightScheme.BOTTOM) {
 						sr.filledRect(bottomButton.x, bottomButton.y,
 								bottomButton.width, bottomButton.height);
 					}
 				}
 				sr.end();
 
 				batch.begin();
 				bf.setColor(Color.BLACK);
 				bf.draw(batch, "Top", 290, 160);
 				bf.draw(batch, "Right", 590, 160);
 				bf.draw(batch, "Bottom", 890, 160);
 				batch.end();
 			}
 		} else { // this draws everything needed in game
 			batch.begin();
 			player.draw(batch, mirror.angle - 90);
 			mirror.draw(batch);
 
 			// Text drawing
 			bf.setColor(Color.WHITE);
 			bf.draw(batch, "Score: " + score, 0, 720);
 			bf.draw(batch, "Enemies Killed: " + enemiesKilled, 225, 720);
 			bf.draw(batch, "Level: " + level, 1000, 720);
 
 			// testing
 			bf.draw(batch, "pu: "
 					+ (powerups.size() > 0 ? powerups.get(0).timeActive
 							: "No powerups."), 550, 720);
 			batch.end();
 			
 			healthBar.set(1 - player.health/100, player.health/100, 0, 1);
 			
 			// drawing health bar
 			sr.begin(ShapeType.FilledRectangle);
 			sr.setColor(healthBar);
 			sr.filledRect(100, 20, player.health * 10, 10);
 			sr.end();
 		}
 
 		light.draw(sr);
 
 		// testing powerups
 		if (!menuScreen)
 			for (int i = 0; i < powerups.size(); i++)
 				powerups.get(i).draw(batch);
 	}
 }
