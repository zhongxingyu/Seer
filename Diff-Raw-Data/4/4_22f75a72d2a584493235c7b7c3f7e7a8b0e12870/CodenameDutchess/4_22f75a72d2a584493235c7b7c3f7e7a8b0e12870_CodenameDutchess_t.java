 package is.nord.dutchess;
 
 import org.anddev.andengine.audio.music.Music;
 import org.anddev.andengine.audio.music.MusicFactory;
 import org.anddev.andengine.engine.Engine;
 
 import org.anddev.andengine.engine.camera.Camera;
 import org.anddev.andengine.engine.camera.hud.controls.AnalogOnScreenControl;
 import org.anddev.andengine.engine.camera.hud.controls.BaseOnScreenControl;
 import org.anddev.andengine.engine.camera.hud.controls.AnalogOnScreenControl.IAnalogOnScreenControlListener;
 import org.anddev.andengine.engine.handler.IUpdateHandler;
 import org.anddev.andengine.engine.options.EngineOptions;
 import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
 import org.anddev.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
 import org.anddev.andengine.entity.layer.ILayer;
 import org.anddev.andengine.entity.primitive.Rectangle;
 import org.anddev.andengine.entity.scene.Scene;
 import org.anddev.andengine.entity.scene.background.ColorBackground;
 import org.anddev.andengine.entity.shape.Shape;
 import org.anddev.andengine.entity.shape.modifier.LoopShapeModifier;
 import org.anddev.andengine.entity.shape.modifier.ParallelShapeModifier;
 import org.anddev.andengine.entity.shape.modifier.RotationModifier;
 import org.anddev.andengine.entity.sprite.Sprite;
 import org.anddev.andengine.entity.util.FPSLogger;
 import org.anddev.andengine.extension.physics.box2d.FixedStepPhysicsWorld;
 import org.anddev.andengine.extension.physics.box2d.PhysicsConnector;
 import org.anddev.andengine.extension.physics.box2d.PhysicsFactory;
 import org.anddev.andengine.extension.physics.box2d.PhysicsWorld;
 import org.anddev.andengine.opengl.font.Font;
 import org.anddev.andengine.opengl.font.FontFactory;
 import org.anddev.andengine.opengl.texture.Texture;
 import org.anddev.andengine.opengl.texture.TextureOptions;
 import org.anddev.andengine.opengl.texture.region.TextureRegion;
 import org.anddev.andengine.opengl.texture.region.TextureRegionFactory;
 import org.anddev.andengine.ui.activity.BaseGameActivity;
 import org.anddev.andengine.util.Debug;
 import org.anddev.andengine.util.MathUtils;
 import org.anddev.andengine.sensor.accelerometer.AccelerometerData;
 import org.anddev.andengine.sensor.accelerometer.IAccelerometerListener;
 
 
 import android.graphics.Color;
 import android.hardware.SensorManager;
 import android.view.KeyEvent;
 import android.widget.Toast;
 
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.Body;
 import com.badlogic.gdx.physics.box2d.FixtureDef;
 import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
 
 import java.io.IOException;
 import java.util.Random;
 /**
  * @author Hopur eitt
  * FIXME: Using font texture doesn't work
  * FIXME: Repeating sprites for the wood bitte 
  */
 public class CodenameDutchess extends BaseGameActivity implements IAccelerometerListener {
 	// ===========================================================
 	// Constants
 	// ===========================================================
 	private static final int CAMERA_WIDTH = 480;
 	private static final int CAMERA_HEIGHT = 320;
 
 
 
 	// ===========================================================
 	// Fields
 	// ===========================================================
 	private Camera mCamera;
 	private Scene scene;
 	
 	private Music mMusic;
 
 	private PhysicsWorld mPhysicsWorld;
 
 	// Texture and region for the agent
 	private Texture mAgentTexture;
 	private TextureRegion mAgentTextureRegion;
 	private Sprite mAgent;
 	private Body mAgentBody;
 	
 	// Test wood texture
 	private Texture mWoodTexture;
 	private TextureRegion mWoodTextureRegion;
 	
 	// Font texture
 	private Texture mFontTexture;
 	private Font mFont;
 	
 	// Texture and region for the rewards to be collected
 	private Texture mRewTexture;
 	private TextureRegion mRewTextureRegion;
 	
 	// Traps, lines and the endgoal, rewards
 	final Shape[] lines = new Rectangle[10];
 	private Sprite[] rewards = new Sprite[6];
 	private Rectangle endRect;
 
 	SceneFactory sf;
 	GameObjectRegistry gor;
 	
 
 	@Override
 	public Engine onLoadEngine() {
 		this.mCamera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
 		return new Engine(new EngineOptions(true, ScreenOrientation.LANDSCAPE, new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), this.mCamera).setNeedsMusic(true));
 	}
 
 	@Override
 	public void onLoadResources() {
 		/* Load Font/Textures. */
 		this.mFontTexture = new Texture(256, 256, TextureOptions.BILINEAR);
 
 		FontFactory.setAssetBasePath("font/");
 		this.mFont = FontFactory.createFromAsset(this.mFontTexture, this, "UnrealTournament.ttf", 48, true, Color.WHITE);
 		this.mEngine.getTextureManager().loadTexture(this.mFontTexture);
 		this.mEngine.getFontManager().loadFont(this.mFont);
 		
 		/* Load Sprite Textures */
 		TextureRegionFactory.setAssetBasePath("gfx/");
 		// Agent
 		this.mAgentTexture = new Texture(64, 64, TextureOptions.BILINEAR);
 		this.mAgentTextureRegion = TextureRegionFactory.createFromAsset(this.mAgentTexture, this, "pokese6.png", 0, 0);	
 		this.mRewTexture = new Texture(64, 64, TextureOptions.BILINEAR);
 		this.mRewTextureRegion = TextureRegionFactory.createFromAsset(this.mRewTexture, this, "coin.png", 0, 0);	
 		// Wood
 		this.mWoodTexture = new Texture(64, 8, TextureOptions.REPEATING);
 		this.mWoodTextureRegion = TextureRegionFactory.createFromAsset(this.mWoodTexture, this, "wood_small.png", 0, 0);
 		
 		this.mEngine.getTextureManager().loadTextures(this.mAgentTexture, this.mRewTexture, this.mWoodTexture);
 		
 		/* Game Music */	
 		MusicFactory.setAssetBasePath("mfx/");
 		
 			try {
 				//Last one seems to start? how about creating playlist?
 				this.mMusic = MusicFactory.createMusicFromAsset(this.mEngine.getMusicManager(), this, "zelda.ogg");				
 				//this.mMusic = MusicFactory.createMusicFromAsset(this.mEngine.getMusicManager(), this, "Acid_techno.ogg");
 			} catch (IllegalStateException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (Exception e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			//this.mMusic.setLooping(true);
 	
 		
 		// Accelero-support
 		this.enableAccelerometerSensor(this);
 
 	}
 
 	@Override
 	public Scene onLoadScene() {
 		this.mEngine.registerUpdateHandler(new FPSLogger());
 
 		scene = new Scene(1);
 		scene.setBackground(new ColorBackground(0.09804f, 0.6274f, 0.8784f));
 		this.mPhysicsWorld = new PhysicsWorld(new Vector2(0, SensorManager.GRAVITY_JUPITER), false);
 		this.gor = new GameObjectRegistry(this.mPhysicsWorld);
 		
 		// Append our textures and stuff to our game object registry
 		this.gor.setAgentTextureRegion(this.mAgentTextureRegion);
 		this.gor.setCoinTextureRegion(this.mRewTextureRegion);
 		this.gor.setWallTextureRegion(this.mWoodTextureRegion);
 				
 		scene.registerUpdateHandler(this.mPhysicsWorld);
		
		//Stop playing
		//this.mMusic.play();
 				
 		sf = new SceneFactory(this.mCamera, this.mFont, this.scene, this.gor);
 		return sf.createWelcomeScene();
 	}
 
 	@Override
 	public void onLoadComplete() {
 
 	}
 	
 	@Override
 	public void onAccelerometerChanged(AccelerometerData pAccelerometerData) {
         this.mPhysicsWorld.setGravity(new Vector2(pAccelerometerData.getY(), pAccelerometerData.getX()));		
 	}
 	
 	public boolean onKeyDown(final int pKeyCode, final KeyEvent pEvent) {
 		if(pKeyCode == KeyEvent.KEYCODE_MENU && pEvent.getAction() == KeyEvent.ACTION_DOWN) {
 			if(this.scene.hasChildScene()) {
 				/* Remove the menu and reset it. */
 				this.scene.back();
 			} else {
 				/* Attach the menu. */
 				this.scene.setChildScene(this.sf.createMenuScene(), false, true, true);
 			}
 			return true;
 		} else {
 			return false;//super.onKeyDown(pKeyCode, pEvent);
 		}
 	}
 
 	
 	// ===========================================================
 	// Methods which should belong to some scene factory class
 	// ===========================================================
 
 	private void initAgent(final Scene pScene) {
 		this.mAgent = new Sprite(0, 0, this.mAgentTextureRegion);
 		this.mAgent.setScale(0.65f);
 		final FixtureDef carFixtureDef = PhysicsFactory.createFixtureDef(1, 0.5f, 0.5f);
 		this.mAgentBody = PhysicsFactory.createBoxBody(this.mPhysicsWorld, this.mAgent, BodyType.DynamicBody, carFixtureDef);
 		this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(this.mAgent, this.mAgentBody, true, false, true, false));
 		pScene.getTopLayer().addEntity(this.mAgent);
 	}
 	
 
 	private void initRandomLevel(final Scene pScene) {
 		Random gaur = new Random();
 		
 		lines[0] = new Rectangle( 0, 0.15f*this.CAMERA_HEIGHT,0.8f*this.CAMERA_WIDTH, 3);
 		lines[1] = new Rectangle( 0.5f*this.CAMERA_WIDTH, 0.3f*this.CAMERA_HEIGHT, 0.5f*this.CAMERA_WIDTH, 3);
 		lines[2] = new Rectangle( 0.3f * this.CAMERA_WIDTH, 0.15f*this.CAMERA_HEIGHT, 3, 0.5f*this.CAMERA_HEIGHT);
 		lines[3] = new Rectangle( 0, 0.4f*this.CAMERA_HEIGHT, 0.16f*this.CAMERA_WIDTH, 3 );
 		lines[4] = new Rectangle( 0.4f*this.CAMERA_WIDTH, 0.5f*this.CAMERA_HEIGHT, 3,this.CAMERA_HEIGHT );
 		lines[5] = new Rectangle( 0.65f*this.CAMERA_WIDTH, 0.3f*this.CAMERA_HEIGHT, 3, 0.4f*this.CAMERA_HEIGHT );
 		lines[6] = new Rectangle( 0.75f*this.CAMERA_WIDTH, 0.6f*this.CAMERA_HEIGHT, 3, 0.4f*this.CAMERA_HEIGHT );
 
 		// Moving guys
 		lines[7] = new Rectangle( 0.85f*this.CAMERA_WIDTH, 0.15f*this.CAMERA_HEIGHT, 0.10f*this.CAMERA_WIDTH, 3 );
 		lines[8] = new Rectangle( 0.81f*this.CAMERA_WIDTH, 0.55f*this.CAMERA_HEIGHT, 0.13f*this.CAMERA_WIDTH, 3 );
 		lines[9] = new Rectangle( 0.11f*this.CAMERA_WIDTH, 0.65f*this.CAMERA_HEIGHT, 0.13f*this.CAMERA_WIDTH, 3 );
 		
 		// Let us create a box body for some of the lines, and connect them with our physics this.mPhysicsWorld. Others are traps. 
 		final FixtureDef wallFixtureDef = PhysicsFactory.createFixtureDef(0, 0.5f, 0.5f);
 		for(int i=0; i<7; i++) 
 		{
 			if(gaur.nextBoolean()) 
 			{
 				PhysicsFactory.createBoxBody(this.mPhysicsWorld, lines[i], BodyType.StaticBody, wallFixtureDef); 
 			} else {
 				lines[i].setColor( 0.5f, 0.7f, 0.1f);
 				//lines[i].setScaleX(1.5f);
 				lines[i].setRotation(0.5f);
 			}
 		}
 		
 		// Rotation for the line-traps
 		for(int i=7; i<10; i++) 
 		{ 
 			lines[i].addShapeModifier(new LoopShapeModifier(new ParallelShapeModifier(new RotationModifier(6, 0, 360)))); 
 			lines[i].setColor(0.5f, 0.7f, 0.1f);
 		}
 		
 		for(int i=0;i<10;i++) { pScene.getTopLayer().addEntity(lines[i]); }
 
 		// Let's prepare the end goal
 		final int centerX = (CAMERA_WIDTH - 32);
 		final int centerY = (CAMERA_HEIGHT - 32);		
 		endRect = new Rectangle(centerX, centerY, 32, 32);
 		endRect.setColor(0, 0, 1);
 		pScene.getTopLayer().addEntity(endRect);
 
 		// Distribute rewards over the level	
 		for(int i=0; i<6; i++)
 		{
 			rewards[i] = new Sprite(CodenameDutchess.randomNumber(5, this.CAMERA_WIDTH), 
 					CodenameDutchess.randomNumber(5, this.CAMERA_HEIGHT), 
 					this.mRewTextureRegion);
 			
 			rewards[i].setScale(0.9f);
 			pScene.getTopLayer().addEntity(rewards[i]);			
 		}
 		
 		// Let's spawn our test texture
 		//WallSprite wallie = new WallSprite(0, 0, this.mWoodTextureRegion, this.mPhysicsWorld);
 
 		// Collision has been outsourced to SceneFactory! 
 		
 	}
 	
 	public static int randomNumber(int min, int max) { return min + (new Random()).nextInt(max-min); }
 }
