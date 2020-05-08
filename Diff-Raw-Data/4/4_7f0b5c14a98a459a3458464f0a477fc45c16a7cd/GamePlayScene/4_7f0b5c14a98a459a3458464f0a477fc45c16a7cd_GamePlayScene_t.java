 package com.secondhand.view.scene;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 
 import org.anddev.andengine.audio.music.Music;
 import org.anddev.andengine.engine.Engine;
 import org.anddev.andengine.engine.camera.hud.HUD;
 import org.anddev.andengine.extension.physics.box2d.PhysicsWorld;
 
 import android.content.Context;
 
 import com.badlogic.gdx.math.Vector2;
 import com.secondhand.model.entity.BlackHole;
 import com.secondhand.model.entity.Enemy;
 import com.secondhand.model.entity.Entity;
import com.secondhand.model.entity.GameSettings;
 import com.secondhand.model.entity.GameWorld;
 import com.secondhand.model.entity.IGameWorld;
 import com.secondhand.model.entity.Obstacle;
 import com.secondhand.model.entity.Planet;
 import com.secondhand.model.entity.Player;
 import com.secondhand.model.entity.PowerUpList;
 import com.secondhand.model.powerup.PowerUp;
 import com.secondhand.model.powerup.PowerUpFactory;
 import com.secondhand.view.andengine.entity.FadingNotifierText;
 import com.secondhand.view.andengine.entity.RocketEmitter;
 import com.secondhand.view.andengine.entity.ScoreLivesText;
 import com.secondhand.view.entity.EnemyView;
 import com.secondhand.view.entity.EntityView;
 import com.secondhand.view.entity.ObstacleView;
 import com.secondhand.view.entity.PlanetView;
 import com.secondhand.view.entity.PlayerView;
 import com.secondhand.view.entity.PowerUpView;
 import com.secondhand.view.opengl.StarsBackground;
 import com.secondhand.view.physics.MyPhysicsWorld;
 import com.secondhand.view.resource.Sounds;
 import com.secondhand.view.resource.loader.MusicLoader;
 
 public class GamePlayScene extends GameScene implements PropertyChangeListener {
 
 	private HUD hud;
 
 	private ScoreLivesText scoreLivesText;
 
 	private IGameWorld gameWorld;
 
 	private PhysicsWorld physicsWorld;
 
 	private Vector2 initialCameraPos;
 
 	private StarsBackground[] starsBackgrounds = new StarsBackground[3];
 
 	private final Music music;
 	
 	public GamePlayScene(final Engine engine, final Context context) {
 		super(engine, context);
 
 		this.music = MusicLoader.getInstance().getMainTheme();
 		this.music.setLooping(true);
 	}
 
 	public IGameWorld getGameWorld() {
 		return this.gameWorld;
 	}
 
 	public PhysicsWorld getPhysicsWorld() {
 		return this.physicsWorld;
 	}
 
 	public void registerNewLevel(final boolean music) {
 
 		this.smoothCamera.setZoomFactor(1.0f);
 
 		final float width = gameWorld.getLevelWidth();
 		final float height = gameWorld.getLevelHeight();
 
 		// clear the old background.
 		if (starsBackgrounds[0] != null) {
 			this.detachChild(starsBackgrounds[0]);
 			this.detachChild(starsBackgrounds[1]);
 			this.detachChild(starsBackgrounds[2]);
 
 		}
 
 		// starry sky
 
 		attachChild(starsBackgrounds[0] = new StarsBackground(
 				50 * this.gameWorld.getLevelNumber(), 1.5f, width, height));
 		attachChild(starsBackgrounds[1] = new StarsBackground(
 				100 * this.gameWorld.getLevelNumber(), 1.3f, width, height));
 		attachChild(starsBackgrounds[2] = new StarsBackground(
 				130 * this.gameWorld.getLevelNumber(), 1.0f, width, height));
 
 		this.smoothCamera.setBounds(0, width, 0, height);
 
 		for (final Entity entity : gameWorld.getEntityList()) {
 
 			EntityView entityView;
 
 			entityView = null;
 			if (entity instanceof Planet) {
 				entityView = new PlanetView(physicsWorld, (Planet) entity);
 
 			} else if (entity instanceof Enemy) {
 				entityView = new EnemyView(physicsWorld, (Enemy) entity);
 			} else if (entity instanceof Obstacle) {
 				entityView = new ObstacleView(physicsWorld, (Obstacle) entity);
 			} else if (entity instanceof PowerUp) {
 				entityView = new PowerUpView(physicsWorld, (PowerUp) entity);
 
 			} else {
 				System.exit(1);
 			}
 
 			this.attachChild(entityView.getShape());
 
 		}
 
 		if (music) {
 			this.music.play();
 		} else {
 			this.music.stop();
 		}
 	}
 
 	private void setupView() {
 		this.smoothCamera.setBoundsEnabled(true);
 
 		final PlayerView playerView = new PlayerView(physicsWorld,
 				gameWorld.getPlayer());
 		attachChild(playerView.getShape());
 
 		// we want to restore the camera when returning to the menu.
 
 		registerUpdateHandler(physicsWorld);
 
 		// setup the HUD
 		final Player player = gameWorld.getPlayer();
 		hud = new HUD();
 		this.scoreLivesText = new ScoreLivesText(new Vector2(10, 10),
 				player.getScore(), player.getLives(), 0f);
 
 		hud.attachChild(scoreLivesText);
 		engine.getCamera().setChaseEntity(playerView.getShape());
 	}
 
 	public void loadLevel(final int levelNumber, final int playerLives,
 			final int playerScore, final boolean music) {
 		physicsWorld = new PhysicsWorld(new Vector2(), true);
 
 		this.gameWorld = new GameWorld(new MyPhysicsWorld(physicsWorld),
 				levelNumber, playerLives, playerScore, new PowerUpFactory());
 
 		gameWorld.getPlayer().addListener(this);
 
 		registerNewLevel(music);
 		setupView();
 
 		engine.getCamera().setHUD(hud);
 	}
 
 	@Override
 	public void loadScene() {
 		super.loadScene();
 		initialCameraPos = new Vector2(smoothCamera.getCenterX(),
 				smoothCamera.getCenterY());
 
		this.loadLevel(GameWorld.STARTING_LEVEL, Player.STARTING_LIVES, 0, GameSettings.getInstance().hasMusic);
 	}
 
 	// reset camera before the menu is shown
 	public void resetCamera() {
 		// stopping chasing player.
 		smoothCamera.setChaseEntity(null);
 		// reset zoom
 		smoothCamera.setZoomFactor(1.0f);
 
 		smoothCamera.setBoundsEnabled(false);
 		this.smoothCamera.setBounds(0, this.smoothCamera.getWidth(), 0,
 				this.smoothCamera.getHeight());
 		smoothCamera.setCenter(initialCameraPos.x, initialCameraPos.y);
 
 		// don't show the HUD in the menu.
 		hud.setCamera(null);
 	}
 
 	@Override
 	public void onSwitchScene() {
 		super.onSwitchScene();
 		this.unregisterScene();
 		resetCamera();
 		this.music.stop();
 	}
 
 	@Override
 	public AllScenes getParentScene() {
 		return AllScenes.MAIN_MENU_SCENE;
 	}
 
 	// zoom out when player grows.
 	// I think we may need to reset sizes when going to new level, otherwise it
 	// will look bad
 	public void apaptCameraToGrowingPlayer(final float newRadius,
 			final float oldRadius) {
 		this.smoothCamera.setZoomFactor(this.smoothCamera.getZoomFactor()
 				- 0.2f * (1f - oldRadius / newRadius));
 	}
 
 	public void showFadingTextNotifier(final String str, final Vector2 position) {
 
 		// convert positon to camera coordinates.
 		final Vector2 cameraPosition = new Vector2(position.x
 				- this.smoothCamera.getMinX(), position.y
 				- this.smoothCamera.getMinY());
 
 		this.hud.attachChild(new FadingNotifierText(str, cameraPosition));
 	}
 
 	// unregister all update handlers and such from the scene.
 	public void unregisterScene() {
 		// remove the listeners and stuff
 		this.detachChildren();
 
 		gameWorld.getPlayer().removeListener(this);
 
 		this.unregisterUpdateHandler(this.physicsWorld);
 
 	}
 
 	public void newLevelStarted(final boolean music) {
 
 		unregisterScene();
 
 		Sounds.getInstance().winSound.play();
 
 		this.loadLevel(gameWorld.getLevelNumber(), this.gameWorld.getPlayer()
 				.getLives(), this.gameWorld.getPlayer().getScore(), music);
 	}
 
 	public void updateScore(final int newScore) {
 		this.scoreLivesText.setScore(newScore);
 	}
 
 	public void updateLives(final int newLives) {
 		this.scoreLivesText.setLives(newLives);
 	}
 
 	public void onPlayerWallCollision() {
 		Sounds.getInstance().obstacleCollisionSound.play();
 	}
 
 	public RocketEmitter attachRocketEmitter(final Vector2 touchPosition) {
 
 		final Player player = this.gameWorld.getPlayer();
 		final com.secondhand.model.physics.Vector2 convertedTouchPosition = new com.secondhand.model.physics.Vector2(
 				touchPosition.x, touchPosition.y);
 		final com.secondhand.model.physics.Vector2 surfacePosition = player
 				.getSurfacePosition(convertedTouchPosition,
 						player.isMirroredMovement());
 
 		final RocketEmitter emitter = new RocketEmitter(surfacePosition.x,
 				surfacePosition.y, player.getCenterX(), player.getCenterY());
 		attachChild(emitter);
 
 		return emitter;
 	}
 
 	public void detachRocketEmitter(final RocketEmitter emitter) {
 		emitter.setParticlesSpawnEnabled(false);
 		detachChild(emitter);
 	}
 
 	@Override
 	public void propertyChange(final PropertyChangeEvent event) {
 
 		final String eventName = event.getPropertyName();
 
 		if (eventName.equals(Player.INCREASE_SCORE)) {
 			updateScore((Integer) event.getNewValue());
 		} else if (eventName.equals(Player.INCREASE_LIFE)) {
 			updateLives((Integer) event.getNewValue());
 		} else if (eventName.equals(BlackHole.RADIUS)) {
 			apaptCameraToGrowingPlayer((Float) event.getNewValue(),
 					(Float) event.getOldValue());
 
 			
 			
 			this.scoreLivesText.setCompletionRatio(gameWorld.getCompletion());
 
 		} else if (eventName.equals("PlayerWallCollision")) {
 			onPlayerWallCollision();
 		}
 
 		else if (eventName.equals(PowerUpList.ADD_POWERUP)) {
 			final PowerUp powerUp = (PowerUp) event.getNewValue();
 			if (powerUp.hasText()) {
 				showFadingTextNotifier(powerUp.getText(),
 						new Vector2(powerUp.getCenterX(), powerUp.getCenterY()));
 			}
 
 		}
 
 	}
 }
