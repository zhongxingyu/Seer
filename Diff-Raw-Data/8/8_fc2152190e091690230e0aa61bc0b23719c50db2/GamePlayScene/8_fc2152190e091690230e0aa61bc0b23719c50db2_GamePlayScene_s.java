 package com.secondhand.view.scene;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 
 import org.anddev.andengine.engine.Engine;
 import org.anddev.andengine.engine.camera.hud.HUD;
 import org.anddev.andengine.extension.physics.box2d.PhysicsWorld;
 
 import android.content.Context;
 
 import com.badlogic.gdx.math.Vector2;
 import com.secondhand.debug.MyDebug;
 import com.secondhand.model.Enemy;
 import com.secondhand.model.Entity;
 import com.secondhand.model.GameWorld;
 import com.secondhand.model.Obstacle;
 import com.secondhand.model.Planet;
 import com.secondhand.model.Player;
 import com.secondhand.model.powerup.PowerUp;
 import com.secondhand.view.entities.EnemyView;
 import com.secondhand.view.entities.EntityView;
 import com.secondhand.view.entities.ObstacleView;
 import com.secondhand.view.entities.PlanetView;
 import com.secondhand.view.entities.PlayerView;
 import com.secondhand.view.entities.PowerUpView;
 import com.secondhand.view.entity.FadingNotifierText;
 import com.secondhand.view.entity.ScoreLivesText;
 import com.secondhand.view.opengl.StarsBackground;
 import com.secondhand.view.physics.MyPhysicsWorld;
 import com.secondhand.view.resource.Sounds;
 
 public class GamePlayScene extends GameScene implements PropertyChangeListener {
 
 	private HUD hud;
 
 	private ScoreLivesText scoreLivesText;
 
 	private GameWorld gameWorld;
 
 	private PhysicsWorld physicsWorld;
 
 	private Vector2 initialCameraPos;
 	
 	
 	private StarsBackground[] starsBackgrounds = new StarsBackground[3];
 
 	public GamePlayScene(final Engine engine, final Context context) {
 		super(engine, context);
 	}
 
 	public GameWorld getGameWorld() {
 		return this.gameWorld;
 	}
 
 	public PhysicsWorld getPhysicsWorld() {
 		return this.physicsWorld;
 	}
 
 	public void registerNewLevel() {
 		
 		this.smoothCamera.setZoomFactor(1.0f);
 	
 		final float width = gameWorld.getLevelWidth();
 		final float height = gameWorld.getLevelHeight();
 		
 		
 		// clear the old background.
 		if(starsBackgrounds[0] != null) {
 			this.detachChild(starsBackgrounds[0]);
 			this.detachChild(starsBackgrounds[1]);
 			this.detachChild(starsBackgrounds[2]);
 
 		}
 		
 		// starry sky
 
 		attachChild(starsBackgrounds[0] =new StarsBackground(50 * this.gameWorld.getLevelNumber(), 1.5f, width, height));
 		attachChild(starsBackgrounds[1] =new StarsBackground(100 * this.gameWorld.getLevelNumber(), 1.3f, width, height));
 		attachChild(starsBackgrounds[2] =new StarsBackground(130 * this.gameWorld.getLevelNumber(), 1.0f, width, height));
 
 		this.smoothCamera.setBounds(0, width, 0, height);
 
 		for (final Entity entity : gameWorld.getEntityManager().getEntityList()) {
 
 			EntityView entityView = null;
 			if (entity instanceof Planet) {
 				entityView = new PlanetView(this.physicsWorld, (Planet) entity);
 			} else if (entity instanceof Enemy) {
 				entityView = new EnemyView(this.physicsWorld, (Enemy) entity);
 			} else if (entity instanceof Obstacle) {
 				entityView = new ObstacleView(this.physicsWorld,
 						(Obstacle) entity);
 			} else if (entity instanceof PowerUp) {
 				entityView = new PowerUpView(this.physicsWorld,
 						(PowerUp) entity);
 			} else {
 				MyDebug.e("invalid entity found in entityList");
 				System.exit(1);
 			}	
 
 			this.attachChild(entityView.getShape());
 		}
 	}
 
 	private void setupView() {
 		final float width = gameWorld.getLevelWidth();
 		final float height = gameWorld.getLevelHeight();
 		this.smoothCamera.setBounds(0, width, 0, height);
 		this.smoothCamera.setBoundsEnabled(true);
 		// setup the player
 
 		// create player view.
 		final PlayerView playerView = new PlayerView(physicsWorld,
 				gameWorld.getPlayer());
 
 		attachChild(playerView.getShape());
 		engine.getCamera().setChaseEntity(playerView.getShape());
 
 		initialCameraPos = new Vector2(smoothCamera.getCenterX(),
 				smoothCamera.getCenterY());
 
 		registerUpdateHandler(physicsWorld);
 
 		// setup the HUD
 		final Player player = gameWorld.getPlayer();
 		hud = new HUD();
 		this.scoreLivesText = new ScoreLivesText(new Vector2(10, 10),
 				player.getScore(), player.getLives(), 0f );
 		hud.attachChild(scoreLivesText);
 	}
 
 	@Override
 	public void loadScene() {
 		super.loadScene();
 
 		physicsWorld = new PhysicsWorld(new Vector2(), true);
 
 		this.gameWorld = new GameWorld(new MyPhysicsWorld(physicsWorld));
 		
 		// receive gameworld property change.
 		gameWorld.addListener(this);
 		
 		// we'll need to be able to restore the camera when returning to the
 		// menu.
 
 		setupView();
 		registerNewLevel();
 		engine.getCamera().setHUD(hud);
 	}
 
 	// reset camera before the menu is shown
 	public void resetCamera() {
 		// stopping chasing player.
 		smoothCamera.setChaseEntity(null);
 		// reset zoom
 		smoothCamera.setZoomFactor(1.0f);
 
 		// camera.setCenter(camera.getWidth()/2, camera.getHeight()/2);
 
 		smoothCamera.setBoundsEnabled(false);
 		this.smoothCamera.setBounds(0, this.smoothCamera.getWidth(), 0,
 				this.smoothCamera.getHeight());
 		smoothCamera.setCenter(initialCameraPos.x,initialCameraPos.y);
 
 		// don't show the HUD in the menu.
 		hud.setCamera(null);
 	}
 
 	@Override
 	public void onSwitchScene() {
 		super.onSwitchScene();
 		resetCamera();
 
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
 				- 0.05f * oldRadius / newRadius);
 		/*
 		 * if(this.smoothCamera.getZoomFactor() < 0.0) {
 		 * this.smoothCamera.setZoomFactor(0); }
 		 */
 	}
 
 	public void showFadingTextNotifier(final String str, final Vector2 position) {
 
 		// convert positon to camera coordinates.
 		final Vector2 cameraPosition = new Vector2(position.x
 				- this.smoothCamera.getMinX(), position.y
 				- this.smoothCamera.getMinY());
 
 		this.hud.attachChild(new FadingNotifierText(str, cameraPosition));
 	}
 
 	public void newLevelStarted() {
 		Sounds.getInstance().winSound.play();
 
 		smoothCamera.setZoomFactorDirect(1.0f);
 
 		registerNewLevel();
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
 
 	@Override
 	public void propertyChange(final PropertyChangeEvent event) {
 
 		final String eventName = event.getPropertyName();
 
 		if (eventName.equals(Player.INCREASE_SCORE)) {
 			updateScore((Integer) event.getNewValue());
 		} else if (eventName.equals(Player.INCREASE_LIFE)) {
 			updateLives((Integer) event.getNewValue());
 		} else if (eventName.equals("radius")) {
 			apaptCameraToGrowingPlayer(
 					(Float) event.getNewValue(),
 					(Float) event.getOldValue());
 			
 			
 			final float newRadius = (Float) event.getNewValue();
 			
			float completion =2*(
 					newRadius -GameWorld.PLAYER_STARTING_SIZE) / 
 					(float)gameWorld.getPlayer().getMaxSize();
 			
 			this.scoreLivesText.setCompletionRatio(completion);
 			
 		} else if (eventName.equals("NextLevel")) {
 			newLevelStarted();
 		} else if (eventName.equals("NextLevel")) {
 			newLevelStarted();
 		} else if (eventName.equals("PlayerWallCollision")) {
 			onPlayerWallCollision();
 		} 
 		
 		else if (eventName.equals(Player.ADD_POWER_UP)) {
 			PowerUp powerUp = (PowerUp) event.getNewValue();
 			if(powerUp.hasText()) {
 				showFadingTextNotifier(powerUp.getText(),
 						new Vector2(powerUp.getCenterX(), powerUp.getCenterY()));
 			}
 	
 		}
 		
 	}
 }
