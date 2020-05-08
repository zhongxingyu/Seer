 package com.secondhand.view.scene;
 
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
 
 public class GamePlayScene extends GameScene {
 
 	private HUD hud;
 
 	private ScoreLivesText scoreLivesText;
 
 	private GameWorld gameWorld;
 
 	private PhysicsWorld physicsWorld;
 	
 	private Vector2 initialCameraPos;
 
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
		
		MyDebug.d("registering new level");
 
 		final float width = gameWorld.getLevelWidth();
 		final float height = gameWorld.getLevelHeight();
 
 		// TODO: get this background to work.
 		/*
 		 * final List<TextureRegion> starsTextures = new
 		 * ArrayList<TextureRegion>();
 		 * starsTextures.add(TextureRegions.getInstance().starsTexture);
 		 * this.attachChild(new RandomRepeatingBackground(starsTextures, width,
 		 * height));
 		 */
 
 		// starry sky
 		attachChild(new StarsBackground(50, 5.0f, width, height));
 		attachChild(new StarsBackground(100, 3.0f, width, height));
 		this.attachChild(new StarsBackground(130, 1.0f, width, height));
 
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
		
		MyDebug.d("resigsted new level");
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
 				player.getScore(), player.getLives());
 		hud.attachChild(scoreLivesText);
 	}
 
 	@Override
 	public void loadScene() {
 		super.loadScene();
 		
 		physicsWorld = new PhysicsWorld(new Vector2(), true);
 
 		this.gameWorld = new GameWorld(new MyPhysicsWorld(physicsWorld));
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
 }
