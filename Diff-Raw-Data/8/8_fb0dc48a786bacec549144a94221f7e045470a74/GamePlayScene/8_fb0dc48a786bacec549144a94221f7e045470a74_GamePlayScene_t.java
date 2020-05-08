 package com.secondhand.scene;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 
 import org.anddev.andengine.engine.Engine;
 import org.anddev.andengine.engine.camera.hud.HUD;
 import org.anddev.andengine.entity.shape.IShape;
 
 import android.content.Context;
 import android.view.KeyEvent;
 
 import com.badlogic.gdx.math.Vector2;
 import com.secondhand.controller.CollisionContactListener;
 import com.secondhand.debug.MyDebug;
 import com.secondhand.model.Entity;
 import com.secondhand.model.GameWorld;
 import com.secondhand.model.Player;
 import com.secondhand.model.powerup.PowerUp;
 import com.secondhand.opengl.StarsBackground;
 
 public class GamePlayScene extends GameScene implements PropertyChangeListener, IGamePlaySceneView {
 
 	private HUD hud;
 	
 	private ScoreLivesText scoreLivesText;
 	
 	private final GameWorld gameWorld;
 	
 	public GamePlayScene(final Engine engine, final Context context) {
 		super(engine, context);
 		this.gameWorld = new GameWorld();
 	}
 	
 	public GameWorld getGameWorld() {
 		return this.gameWorld; 
 	}
 
 
 	public void registerNewLevel() {
 	
 		final float width = gameWorld.getLevelWidth();
 		final float height = gameWorld.getLevelHeight();
 		
 		this.smoothCamera.setBounds(0, width, 0, height);
 		
 		for (final Entity entity : gameWorld.getEntityList()) {
			final IShape shape = entity.getShape();
 			shape.detachSelf();
 			attachChild(shape);
 		}
 		
 		// TODO: get this background to work.
 		/*final List<TextureRegion> starsTextures = new ArrayList<TextureRegion>();
 		starsTextures.add(TextureRegions.getInstance().starsTexture);
 		this.attachChild(new RandomRepeatingBackground(starsTextures, width, height));*/
 		
 		// starry sky
 		this.attachChild(new StarsBackground(50, 5.0f, width, height));
 		this.attachChild(new StarsBackground(100, 3.0f, width, height));
         this.attachChild(new StarsBackground(130, 1.0f, width, height));
 	}
 	
 	// should be called ONCE in the program.
 	private void setupView() {
 	
 		final float width = gameWorld.getLevelWidth();
 		final float height = gameWorld.getLevelHeight();
 		
 		this.smoothCamera.setBounds(0, width, 0, height);
 		this.smoothCamera.setBoundsEnabled(true);	
 		// setup the player
 		
 		final Player player = gameWorld.getPlayer();
 		  player.getShape().detachSelf();
 			attachChild(player.getShape());
 		gameWorld.getPlayer().addListener(this);
 		engine.getCamera().setChaseEntity(player.getShape());
 			
 		// setup the physicsworld the
 		registerUpdateHandler(gameWorld.getPhysicsWorld());
 		gameWorld.getPhysicsWorld().setContactListener(new CollisionContactListener(gameWorld));
 		gameWorld.setView(this);
 
 		// setup the HUD
 		hud = new HUD();
 		this.scoreLivesText = new ScoreLivesText(new Vector2(10,10), player.getScore(), player.getLives()); 
 		hud.attachChild(scoreLivesText);
 		engine.getCamera().setHUD(hud);	
 	}
 
 	@Override
 	public void loadScene() {
 		setupView();
 		registerNewLevel();
 	}
 	
 	// Undo camera lock on player
 	public void resetCamera() {
 		smoothCamera.setChaseEntity(null);
 		engine.getCamera().setCenter(0, 0);
 		
 		// don't show the HUD in the menu.	
 		hud.setCamera(null);
 	}
 	
 	@Override
 	public boolean onKeyDown(final int pKeyCode, final KeyEvent pEvent) {
 		if (pKeyCode == KeyEvent.KEYCODE_BACK
 				&& pEvent.getAction() == KeyEvent.ACTION_DOWN) {
 			final AllScenes parent = getParentScene();
 			if (parent != null) {
 				setScene(parent);
 				resetCamera();
 				return true;
 			}
 			else
 				return false;
 		} else {
 			return false;
 		}
 	}
 	
 	@Override
 	public AllScenes getParentScene() {
 		return AllScenes.MAIN_MENU_SCENE;
 	}
 	
 	@Override
 	protected void onManagedUpdate(final float pSecondsElapsed){
 		super.onManagedUpdate(pSecondsElapsed);
 		if(gameWorld.isGameOver()){
 			MyDebug.d("GameOver");
 			resetCamera();
 			setScene(AllScenes.GAME_OVER_SCENE);
 		}
 		gameWorld.onManagedUpdate(pSecondsElapsed);
 		
 	}
 
 	@Override
 	public void propertyChange(final PropertyChangeEvent event) {
 		final Player player = gameWorld.getPlayer();
 		final PowerUp powerUp = ((PowerUp)event.getNewValue());
 		engine.registerUpdateHandler(powerUp.getTimer(player));
 	}
 
 	@Override
 	public void showFadingTextNotifier(final String str, final Vector2 position) {
 		this.attachChild(new FadingNotifierText(str, position));
 	}
 
 	@Override
 	public void newLevelStarted() {
 		MyDebug.d("new level!");
 		registerNewLevel();
 	}
 
 	@Override
 	public void updateScore(final int newScore) {
 		this.scoreLivesText.setScore(newScore);
 	}
 
 	@Override
 	public void updateLives(final int newLives) {
 		this.scoreLivesText.setLives(newLives);
 	}
 }
