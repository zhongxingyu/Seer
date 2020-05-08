 package is.nord.dutchess;
 
 import org.anddev.andengine.engine.camera.Camera;
 import org.anddev.andengine.engine.handler.IUpdateHandler;
 import org.anddev.andengine.entity.layer.ILayer;
 import org.anddev.andengine.entity.primitive.Rectangle;
 import org.anddev.andengine.entity.scene.Scene;
 import org.anddev.andengine.entity.scene.background.ColorBackground;
 import org.anddev.andengine.entity.scene.menu.MenuScene;
 import org.anddev.andengine.entity.scene.menu.MenuScene.IOnMenuItemClickListener;
 import org.anddev.andengine.entity.scene.menu.item.ColoredTextMenuItem;
 import org.anddev.andengine.entity.scene.menu.item.IMenuItem;
 import org.anddev.andengine.entity.shape.Shape;
 import org.anddev.andengine.entity.sprite.Sprite;
 import org.anddev.andengine.extension.physics.box2d.PhysicsConnector;
 import org.anddev.andengine.extension.physics.box2d.PhysicsFactory;
 import org.anddev.andengine.opengl.font.Font;
 
 import android.text.style.BackgroundColorSpan;
 import android.view.KeyEvent;
 
 import com.badlogic.gdx.physics.box2d.Body;
 import com.badlogic.gdx.physics.box2d.FixtureDef;
 import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
 
 /**
  * 
  * @author gunnarr
  *
  * This class is constructs and manages scenes both for game play and menus. A part of that registering the scene to an 
  * update handler and implementing it's onUpdate method.  
  *
  */
 
 public class SceneFactory implements IOnMenuItemClickListener {
 
 	// Local variables
 	private Scene activeScene;
 	private Camera camera;
 	private Font font;
 	
 	private GameObjectRegistry gor;
 	
 	private static final int MENU_NEWGAME = 0;
 	private static final int MENU_QUIT = MENU_NEWGAME + 1;
 	
 	/*
 	 * Usage:	SceneFactory sf = new SceneFactory(camera, font, scene);
 	 * Pre:		camera is of type Camera, font of type Font, and scene of type Scene, and all three have been set up
 	 * Post:	sf is a SceenFactory object based on the parameters
 	 */
 	public SceneFactory(Camera camera, Font font, Scene scene, GameObjectRegistry gor)
 	{
 		this.camera = camera;
 		this.font = font;
 		this.activeScene = scene;
 		
 		this.gor = gor;
 		
 		this.activeScene.registerUpdateHandler(new IUpdateHandler() {
 
 			@Override
 			public void reset() { }
 
 			@Override
 			public void onUpdate(final float pSecondsElapsed) {
 				// invoke onCollision() on game objects here
 			}
 		});
 		
 		//this.initBorders();
 		
 	}
 
 	/*
 	 * Usage:	gameScene = sf.getGameScene();
 	 * Pre:		sf is a SceneFactory object
 	 * Post:	gameScene represents the latest scene manifactured for the game, that is the level.
 	 */
 	public Scene getGameScene()
 	{
 		return this.activeScene;
 	}
 
 	/*
 	 * Usage:	menuScene = sf.createMenuScene()
 	 * Pre:		sf is a SceneFactory object
 	 * Post:	menuScene holds the main gamemenu 
 	 */
 	public MenuScene createMenuScene() {
 		final MenuScene menuScene = new MenuScene(this.camera);
 
 		menuScene.addMenuItem(new ColoredTextMenuItem(MENU_NEWGAME, this.font, "NEW GAME", 1.0f,0.0f,0.0f, 0.0f,0.0f,0.0f));
 		menuScene.addMenuItem(new ColoredTextMenuItem(MENU_QUIT, this.font, "QUIT", 1.0f,0.0f,0.0f, 0.0f,0.0f,0.0f));
 		menuScene.buildAnimations();
 		
 
		menuScene.setBackgroundEnabled(false);
 
 		menuScene.setOnMenuItemClickListener(this);
 		return menuScene;
 	}
 	
 	/*
 	 * Usage:	welcomeScene = sf.createWelcomeScene();
 	 * Pre:		sf is a SceneFactory object
 	 * Post:	welcomeScene holds the initial scene loaded when the game is started
 	 */
 	public Scene createWelcomeScene()
 	{
 		this.activeScene.setBackground(new ColorBackground(0.09804f, 0.6274f, 0.8784f));
 		this.activeScene.setChildScene(this.createMenuScene(), false, true, true);
 		return this.activeScene;
 	}
 	
 	/*
 	 * Usage:	sf.createDemoScene();
 	 * Pre:		sf is a SceneFactory object
 	 * Post:	the active scene is a demo scene, similar to the one from earlier vesions of the game.
 	 * 			Proof of concept but not a final or definite version of how game levels are constructed
 	 */
 	public void createDemoScene()
 	{
 		/* make the frame */
 		this.initBorders();
 		/* Spawn the agent. ACTHUNG: the agent will be objectified. This codeblock also shows how GameObjectRegistry is used */
 		final Sprite agent = new Sprite(0, 0, this.gor.getAgentTextureRegion());
 		agent.setScale(0.65f);
 		// fixturedef for physics. Can hopefully be enhanced to make ball heavier. 
 		final FixtureDef carFixtureDef = PhysicsFactory.createFixtureDef(1, 0.5f, 0.5f);
 		Body agentBody = PhysicsFactory.createBoxBody(this.gor.getPhysicsWorld(), agent, BodyType.DynamicBody, carFixtureDef);
 		this.gor.getPhysicsWorld().registerPhysicsConnector(new PhysicsConnector(agent, agentBody, true, false, true, false));
 		this.activeScene.getTopLayer().addEntity(agent);
 		
 		
 		this.activeScene.reset();
 	}
 	
 	/*
 	 * Usage:	this.initBorders();
 	 * Pre:		this is a sf object
 	 * Post:	The game level's borders have been initialized to form a frame
 	 */
 	private void initBorders() {
 		final Shape bottomOuter = new Rectangle(0, this.camera.getHeight() - 2, this.camera.getWidth(), 2);
 		final Shape topOuter = new Rectangle(0, 0, this.camera.getWidth(), 2);
 		final Shape leftOuter = new Rectangle(0, 0, 2, this.camera.getHeight());
 		final Shape rightOuter = new Rectangle(this.camera.getWidth() - 2, 0, 2, this.camera.getHeight());
 		
 		final FixtureDef wallFixtureDef = PhysicsFactory.createFixtureDef(0, 0.5f, 0.5f);
 		PhysicsFactory.createBoxBody(this.gor.getPhysicsWorld(), bottomOuter, BodyType.StaticBody, wallFixtureDef);
 		PhysicsFactory.createBoxBody(this.gor.getPhysicsWorld(), topOuter, BodyType.StaticBody, wallFixtureDef);
 		PhysicsFactory.createBoxBody(this.gor.getPhysicsWorld(), leftOuter, BodyType.StaticBody, wallFixtureDef);
 		PhysicsFactory.createBoxBody(this.gor.getPhysicsWorld(), rightOuter, BodyType.StaticBody, wallFixtureDef);
 		
 		final ILayer bottomLayer = this.activeScene.getTopLayer();
 		bottomLayer.addEntity(bottomOuter);
 		bottomLayer.addEntity(topOuter);
 		bottomLayer.addEntity(leftOuter);
 		bottomLayer.addEntity(rightOuter);	
 		
 		//final WallSprite wallie = new WallSprite(10, 100, this.mWoodTextureRegion, this.gor.getPhysicsWorld());	
 		//PhysicsFactory.createBoxBody(this.gor.getPhysicsWorld(), wallie, BodyType.StaticBody, wallFixtureDef);
 		//bottomLayer.addEntity(wallie);
 	}
 	
 	
 	@Override
 	public boolean onMenuItemClicked(MenuScene pMenuScene, IMenuItem pMenuItem, float pMenuItemLocalX, float pMenuItemLocalY) 
 	{
 		switch(pMenuItem.getID()) 
 		{
 			case MENU_QUIT: System.exit(0); // Should also be activity finish something
 			case MENU_NEWGAME: this.createDemoScene();
 			return true;
 		}
 		return false;
 	}
 	
 	public boolean onKeyDown(final int pKeyCode, final KeyEvent pEvent) {
 		if(pKeyCode == KeyEvent.KEYCODE_MENU && pEvent.getAction() == KeyEvent.ACTION_DOWN) {
 			if(this.activeScene.hasChildScene()) {
 				/* Remove the menu and reset it. */
 				this.activeScene.back();
 			} else {
 				/* Attach the menu. */
 				this.activeScene.setChildScene(this.createMenuScene(), false, true, true);
 			}
 			return true;
 		} else {
 			return false;//super.onKeyDown(pKeyCode, pEvent);
 		}
 	}
 
 }
