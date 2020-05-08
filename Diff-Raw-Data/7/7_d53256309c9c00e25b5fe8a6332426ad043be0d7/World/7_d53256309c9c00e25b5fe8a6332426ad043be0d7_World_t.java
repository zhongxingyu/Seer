 package com.picotech.lightrunnerlibgdx;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Random;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
 import com.badlogic.gdx.math.Intersector;
 import com.badlogic.gdx.math.MathUtils;
 import com.badlogic.gdx.math.Rectangle;
 import com.badlogic.gdx.math.Vector2;
 import com.picotech.lightrunnerlibgdx.DialogBox.DialogBoxSituation;
 import com.picotech.lightrunnerlibgdx.DialogBox.DialogBoxType;
 import com.picotech.lightrunnerlibgdx.GameScreen.GameState;
 import com.picotech.lightrunnerlibgdx.Powerup.Type;
 
 /**
  * The World class holds all of the players, enemies and environment objects. It
  * handles collisions and drawing methods, as well as loading content.
  */
 
 public class World {
 
 	/*
 	 * enum MenuState { PLAY, CHOOSESIDE }
 	 * 
 	 * MenuState menuState = MenuState.PLAY;
 	 */
 
 	Player player;
 	Mirror mirror;
 	Light light;
 	DebugOverlay debug;
 	Menu menu;
 
 	DialogBox db;
 
 	float deltaTime;
 	public static float totalTime;
 	float spawnEnemyTime;
 	boolean enemySpawnInit = true;
 	float loadedContentPercent;
 
 	public static int enemiesKilled;
 	public static int score;
 	int level;
 	int powerupf = 2000;
 
 	Vector2 ENEMY_VEL;
 	Vector2 LightSource;
 
 	Rectangle pauseButton;
 
 	boolean playSelected;
 	boolean controlsSelected;
 	boolean isClearScreen = false;
 	boolean slowActivated = false;
 	boolean oneHit = false;
 	boolean isSpawning = true;
 	public static boolean debugMode = false;
 
 	ArrayList<Enemy> enemies;
 	ArrayList<Enemy> enemiesDead;
 	ArrayList<Magnet> magnets;
 	ArrayList<Magnet> inactiveMagnets;
 	ArrayList<Powerup> powerups;
 	ArrayList<Powerup> inactivePowerups;
 	public static HashMap<Type, Float> powerupHM = new HashMap<Type, Float>();
 
 	Color healthBar;
 
 	Random r = new Random();
 
 	/**
 	 * There are two types of worlds, the menu world and the in-game world. The
 	 * behavior of the light depends on whether the game is in the menu or
 	 * playing state.
 	 * 
 	 * @param isMenu
 	 */
 	public World() {
 		level = 1;
 		totalTime = 0;
 
 		pauseButton = new Rectangle(GameScreen.width - GameScreen.defS.x * 100,
 				GameScreen.height - GameScreen.defS.y * 100,
 				GameScreen.defS.x * 100, GameScreen.defS.y * 100);
 
 		enemies = new ArrayList<Enemy>();
 		enemiesDead = new ArrayList<Enemy>();
 		powerups = new ArrayList<Powerup>();
 		inactivePowerups = new ArrayList<Powerup>();
 
 		// menuScreen = isMenu;
 		player = new Player(new Vector2(0, 300), "characterDirection0.png");
 		mirror = new Mirror(new Vector2(100, 300), "mirror.png");
 		magnets = new ArrayList<Magnet>();
 		inactiveMagnets = new ArrayList<Magnet>();
 		menu = new Menu();
 
 		debug = new DebugOverlay();
 		healthBar = new Color();
 
 		if (isMenu()) {
 			setupMenu();
 		} else {
 			setLight();
 		}
 
 		// Spawning enemies
 		for (int i = 0; i < level; i++) {
 			enemies.add(new Enemy(new Vector2(r.nextInt(950) + 300, r
 					.nextInt(700)), 50, 50, level));
 			enemies.get(enemies.size() - 1).loadContent();
 		}
 
 		// Power-ups
 		if (!isMenu()) {
 			for (Powerup pu : powerups)
 				pu.loadContent();
 			powerupf = r.nextInt(500) + 1500;
 		}
 		// HashMap values
 		powerupHM.put(Powerup.Type.CLEARSCREEN, 3.5f);
 		powerupHM.put(Powerup.Type.ENEMYSLOW, 9.5f);
 		powerupHM.put(Powerup.Type.ONEHITKO, 11f);
 		powerupHM.put(Powerup.Type.PRISMPOWERUP, 6f);
 		powerupHM.put(Powerup.Type.SPAWNSTOP, 8f);
 		powerupHM.put(Powerup.Type.SPAWNMAGNET, 5f);
 	}
 
 	public void setupMenu() {
 		player = new Player(new Vector2(-100, -100), "characterDirection0.png");
 		light = new Light(true);
 		level = 10;
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
 		menu.loadContent();
 
 		for (Powerup pu : powerups) {
 			pu.loadContent();
 		}
 
 		if (debugMode)
 			debug.loadContent();
 	}
 
 	/**
 	 * Updates the entire World. Includes light, enemy movement, and enemy
 	 * destruction. Also updates the time functions for frame rate-independent
 	 * functions deltaTime and totalTime (which are all in seconds).
 	 */
 	public void update() {
 		// Miscellaneous time updating functions.
 		deltaTime = Gdx.graphics.getDeltaTime();
 		if (GameScreen.state == GameScreen.GameState.PLAYING
 				|| (isMenu() && (menu.menuState == Menu.MenuState.MAIN || menu.menuState == Menu.MenuState.OPTIONS))) {
 			totalTime += deltaTime;
 
 			if ((debug.nothingSelected && debugMode) || !debugMode) {
 				player.update();
 				mirror.rotateAroundPlayer(
 						player.getCenter(),
 						(player.bounds.width / 2) + 2
 								+ (light.getOutgoingBeam().isPrism ? 40 : 0));
 			}
 			// Updating light, player, and the mirror.
 			light.update(mirror, player);
 
 			// Updates all enemies in "enemies".
 			Assets.playedSound = false;
 			for (Enemy e : enemies) {
 				e.update();
 				for (int beam = 1; beam < light.beams.size(); beam++) {
 					if (Intersector.overlapConvexPolygons(
 							light.beams.get(beam).beamPolygon, e.p)) {
 						if (oneHit) {
 							e.alive = false;
 							Assets.playSound(Assets.died);
 						}
 						if (e.alive) {
 							e.health--;
 							e.losingHealth = true;
 							Assets.playSound(Assets.hit);
 							Assets.playedSound = true;
 						} else {
 							enemiesKilled++;
 						}
 
 					}
 					if (GameScreen.state == GameScreen.GameState.PLAYING
 							&& Intersector.overlapConvexPolygons(player.p, e.p)) {
 						if (player.alive)
 							player.health--;
 					}
 				}
 				// adds the number of enemies still alive to a new ArrayList
 				if (e.alive) {
 					e.isSlow = slowActivated;
 				} else {
 					enemiesDead.add(e);
 				}
 
 				// magnets
 				for (Magnet magnet : magnets) {
 					if (e.getCenter().dst(magnet.getCenter()) < 500) {
 						e.velocity.set(magnet.getPull(e.getCenter()));
 					}
 				}
 			}
 
 			for (Magnet magnet : magnets) {
 				magnet.update();
 				if (magnet.position.x < -100) {
 					inactiveMagnets.add(magnet);
 				}
 			}
 
 			// removes the "dead" enemies from the main ArrayList
 			enemies.removeAll(enemiesDead);
 			enemiesDead.clear();
 
 			magnets.removeAll(inactiveMagnets);
 			inactiveMagnets.clear();
 
 			// temporarily spawns new enemies, which get progressively faster
 			if ((isSpawning && spawnEnemyTime <= totalTime) || enemySpawnInit) {
 				enemies.add(new Enemy(new Vector2(1280, r.nextInt(700)), 50,
 						50, level));
 				enemies.get(enemies.size() - 1).isSlow = slowActivated;
 				enemies.get(enemies.size() - 1).loadContent();
 				enemySpawnInit = false;
 				spawnEnemyTime = totalTime + (4f / level);
 			}
 
 			// Time-wise level changing
 			if (!isMenu())
 				if (totalTime > 5 * level)
 					level++;
 
 			setScore();
 
 			// Powerups.
 			updatePowerups();
 		}
 
 		// Depending on the MenuState, it will either show the Play
 		// button or the Top-Right-Bottom buttons.
 		float dstX = light.getOutgoingBeam().dst.x;
 		if (isMenu() && menu.menuState == Menu.MenuState.MAIN) {
 			if (dstX > menu.playButton.x - 100
 					&& dstX < menu.playButton.x + menu.playButton.width + 100) {
 				playSelected = true;
 			} else {
 				playSelected = false;
 				Assets.playedSound = false;
 			}
 		}
 
 		// Debugging overlay.
 		if (debugMode) {
 			debug.update();
 			if (debug.selectedButtons[0]) {
 				System.out.println("Changed mirror.");
 				changeMirrors();
 			} else if (debug.selectedButtons[1]) {
 				System.out.println("Added magnet.");
 				addMagnet(.1f);
 			} else if (debug.selectedButtons[2]) {
 				System.out.println("Added powerup.");
 				addPowerup();
 			} else if (debug.selectedButtons[3]) {
 				player.alive = false;
 			} else if (debug.selectedButtons[4]) {
 				StatLogger2.reset();
 			}
 			debug.resetButtons();
 		}
 	}
 
 	public void changeMirrors() {
 		if (mirror.type == Mirror.Type.CONVEX)
 			mirror.type = Mirror.Type.FLAT;
 		else if (mirror.type == Mirror.Type.FLAT)
 			mirror.type = Mirror.Type.FOCUS;
 		else if (mirror.type == Mirror.Type.FOCUS)
 			mirror.type = Mirror.Type.CONVEX;
 	}
 
 	public void selectControls() {
 		// Randomized light-source choosing.
 		int schemeN = r.nextInt(3) + 1;
 		GameScreen.scheme = GameScreen.selectedScheme = GameScreen.LightScheme
 				.values()[schemeN];
 		controlsSelected = true;
 		Assets.playedSound = true;
 		GameScreen.state = GameScreen.GameState.READY;
 	}
 
 	// writes to StatLogger
 	public void updateStatLogger(/* StatLogger sl */StatLogger2 sl) {
 		// sl.update(score, (int) totalTime, enemiesKilled);
 		sl = new StatLogger2();
 	}
 
 	public void setScore() {
 		// Score algorithm, changed as of 8/20/13
 		score = (int) (totalTime * 2 + enemiesKilled * 5);
 	}
 
 	public boolean collide(Sprite2 pu, Sprite2 player) {
 		return pu.position.x < player.position.x + player.bounds.width
 				&& pu.position.y + pu.bounds.height > player.position.y
 				&& pu.position.y < player.position.y + player.bounds.height;
 	}
 
 	/**
 	 * Handles all the power-up logic.
 	 */
 	private int demoPowerup = 0;
 
 	private void updatePowerups() {
 		// Randomizing spawns
 		/*
 		 * if ((int) (totalTime * 25) % powerupf == 0 && demoPowerup < 6) {
 		 * addPowerup(demoPowerup); demoPowerup++; } if (demoPowerup == 6) {
 		 * demoPowerup = 0; }
 		 */
 		if ((int) (totalTime * 25) % powerupf == 0) {
 			addPowerup();
 		}
 
 		for (int i = 0; i < powerups.size(); i++) {
 			Powerup pu = powerups.get(i);
 			pu.update(deltaTime);
 			// Collision with player
 			if (collide(pu, player) && pu.position.x >= 0) {
 				player.addPowerup(pu);
 				pu.position = new Vector2(10000, -500);
 				Assets.playSound(Assets.powerups[r.nextInt(3)]);
 			} else if (pu.position.x < -100) {
 				inactivePowerups.add(pu);
 			}
 			Assets.playedSound = false;
 			// Ending power-ups
 			if (pu.timeActive > pu.timeOfEffect) {
 				pu.end();
 
 				switch (pu.type) {
 				case ONEHITKO:
 					oneHit = false;
 					break;
 				case PRISMPOWERUP:
 					GameScreen.scheme = GameScreen.selectedScheme;
 					setLight();
 					light.getOutgoingBeam().setWidth(Light.L_WIDTH);
 					light.getOutgoingBeam().isPrism = false;
 					mirror.setType(Mirror.Type.FLAT, "mirror.png");
 					break;
 				case ENEMYSLOW:
 					slowActivated = false;
 					for (Enemy e : enemies)
 						e.isSlow = false;
 					break;
 				case CLEARSCREEN:
 					isClearScreen = false;
 					break;
 				case SPAWNSTOP:
 					isSpawning = true;
 					break;
 				case SPAWNMAGNET:
 					// do nothing, it goes all the way to the end of the screen
 					break;
 				default:
 					break;
 				}
 				Assets.playSound(Assets.blip);
 				powerups.remove(i);
 			}
 		}
 		powerups.removeAll(inactivePowerups);
 		inactivePowerups.clear();
 
 		if (isClearScreen) {
 			enemiesDead.clear();
 			enemies.clear();
 		}
 	}
 
 	public void usePowerup(Powerup pu) {
 		switch (pu.type) {
 		case ONEHITKO:
 			oneHit = true;
 			Assets.playSound(Assets.oneHit);
 			break;
 		case PRISMPOWERUP:
 			GameScreen.scheme = GameScreen.LightScheme.LEFT;
 			light.getOutgoingBeam().setWidth(Powerup.P_WIDTH);
 			mirror.setType(Mirror.Type.PRISM, "prism.png");
 			Assets.playSound(Assets.prism);
 			break;
 		case ENEMYSLOW:
 			slowActivated = true;
 			for (Enemy e : enemies)
 				e.isSlow = true;
 			Assets.playSound(Assets.blip);
 			break;
 		case CLEARSCREEN:
 			isClearScreen = true;
 			for (int j = 0; j < enemies.size(); j++) {
 				if (enemies.get(j).alive)
 					enemiesKilled++;
 			}
 			setScore();
 			Assets.playSound(Assets.clearScreen);
 			break;
 		case SPAWNSTOP:
 			isSpawning = false;
 			Assets.playSound(Assets.blip);
 			break;
 		case SPAWNMAGNET:
 			addMagnet(0.05f);
 			Assets.playSound(Assets.spawnMagnet);
 		}
 		pu.isActive = true;
 		pu.isAura = true;
 		pu.position = new Vector2(10000, 10000);
 	}
 
 	public void addMagnet(float strength) {
 		magnets.add(new Magnet(new Vector2(1280, MathUtils.random(100, 700)),
 				48, 48, "magnet.png", strength));
 		magnets.get(magnets.size() - 1).loadContent();
 	}
 
 	public void addPowerup() {
 		int x = r.nextInt(Powerup.Type.values().length);
 		powerups.add(new Powerup(new Vector2(1300, r.nextInt(600) + 50),
 				Powerup.Type.values()[x]));
 		powerups.get(powerups.size() - 1).loadContent();
 		powerupf = r.nextInt(500) + 2500;
 	}
 
 	public void addPowerup(int powerupNumber) {
 		powerups.add(new Powerup(new Vector2(1300, r.nextInt(600) + 50),
 				Powerup.Type.values()[powerupNumber]));
 		powerups.get(powerups.size() - 1).loadContent();
 		powerupf = 600;
 	}
 
 	public void showDisplayBox(DialogBoxType type) {
 		Rectangle r = new Rectangle(GameScreen.width / 2 - 200,
 				GameScreen.height / 2 - 100, 400, 200);
 		if (type == DialogBox.DialogBoxType.YESNO) {
 			if (GameScreen.situation == DialogBoxSituation.GAMERESTART) {
 				db = new YesNoBox(r, "Restart?");
 			} else if (GameScreen.situation == DialogBoxSituation.GAMEQUIT
 					|| GameScreen.situation == DialogBoxSituation.MAINQUIT) {
 				db = new YesNoBox(r, "Quit?");
 			}
 		} else
 			db = new DialogBox(r, 1, "You broke the game. Nice.",
 					new String[] { "OK" });
 		GameScreen.dialogBoxActive = true;
 	}
 
 	/**
 	 * Draws the entire world.
 	 * 
 	 * @param batch
 	 *            the SpriteBatch from WorldRenderer
 	 * @param sr
 	 *            the ShapeRenderer to render light and enemies
 	 */
 	public void draw(SpriteBatch batch, ShapeRenderer sr) {
 		if (!(GameScreen.state == GameState.MENU
 				&& menu.menuState == Menu.MenuState.GAMEOVER)) {
 			for (Enemy e : enemies)
 				e.draw(batch);
 
 			light.draw(sr);
 
 			batch.begin();
 			player.draw(batch, mirror.angle - 90);
 			mirror.draw(batch);
 			for (Magnet magnet : magnets)
 				magnet.draw(batch);
 
 			batch.end();
 			player.drawInventory(batch);
 
 			// powerups
 			for (int i = 0; i < powerups.size(); i++)
 				powerups.get(i).draw(batch);
 
 			if (GameScreen.state == GameState.PLAYING) {
 				batch.begin();
 				batch.draw(Assets.pauseButton, pauseButton.x, pauseButton.y,
 						Assets.pauseButton.getWidth(),
 						Assets.pauseButton.getHeight());
 				batch.end();
 
 				healthBar.set(1 - player.health / 100, player.health / 100, 0,
 						1);
 
 				// drawing health bar
 				/*
 				 * Style 1: ShapeRenderer sr.begin(ShapeType.FilledRectangle);
 				 * sr.setColor(healthBar); sr.filledRect(100, 20, player.health
 				 * * 10, 10); sr.end();
 				 */
				
				float barWidth = (player.health/100f) * (GameScreen.width - 200 * GameScreen.defS.x);
				
 				// Style 2: SpriteBatch
 				Assets.drawByPixels(batch, new Rectangle(100 * GameScreen.defS.x, 20 * GameScreen.defS.y,
						/*player.health * 10 * GameScreen.defS.x*/barWidth, 10), healthBar);
 
 				if (debugMode) {
 					debug.draw(batch, sr);
 					String powerupString = "";
 					for (Powerup p : powerups) {
 						powerupString += (p.timeActive);
 						powerupString += "\n";
 					}
 					Assets.textWhite(batch, "pu: " + powerupString, 550, 720);
 					// Assets.textWhite(batch, "" + GameScreen.state, 400, 400);
 				}
 
 				Assets.textWhite(batch, "Score: " + score, 0, 720);
 				Assets.textWhite(batch, "Enemies Killed: " + enemiesKilled,
 						225, 720);
 				Assets.textWhite(batch, "Level: " + level, 1000, 720);
 
 			}
 		}
 
 		if (isMenu())
 			menu.draw(batch);
 
 		if (GameScreen.dialogBoxActive) {
 			Assets.setTextScale(2f);
 			db.draw(batch);
 		}
 		if (debugMode) {
 			int x = 24;
 			for (int i = 0; i < (GameScreen.height / x); i++) {
 				Assets.textWhite(batch, "S " + x * i, 0, x * i);
 			}
 		}
 	}
 
 	public boolean isMenu() {
 		return GameScreen.state == GameScreen.GameState.MENU;
 	}
 }
