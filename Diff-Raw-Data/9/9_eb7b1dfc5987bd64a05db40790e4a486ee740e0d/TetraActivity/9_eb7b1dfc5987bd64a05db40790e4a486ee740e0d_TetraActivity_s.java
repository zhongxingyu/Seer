 package ddmp.projecttetra;
 
 import org.andengine.engine.camera.Camera;
 import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.EngineOptions.ScreenOrientation;
 import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
 import org.andengine.entity.scene.IOnSceneTouchListener;
 import org.andengine.entity.scene.Scene;
 import org.andengine.entity.scene.background.Background;
 import org.andengine.entity.sprite.Sprite;
 import org.andengine.entity.util.FPSLogger;
 import org.andengine.extension.physics.box2d.PhysicsFactory;
 import org.andengine.extension.physics.box2d.PhysicsWorld;
 import org.andengine.input.touch.TouchEvent;
 import org.andengine.opengl.texture.TextureOptions;
 import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
 import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
 import org.andengine.opengl.texture.atlas.bitmap.BuildableBitmapTextureAtlas;
 import org.andengine.opengl.texture.atlas.bitmap.source.IBitmapTextureAtlasSource;
 import org.andengine.opengl.texture.atlas.buildable.builder.BlackPawnTextureAtlasBuilder;
 import org.andengine.opengl.texture.atlas.buildable.builder.ITextureAtlasBuilder.TextureAtlasBuilderException;
 import org.andengine.opengl.texture.region.ITextureRegion;
 import org.andengine.ui.activity.SimpleBaseGameActivity;
 import org.andengine.util.debug.Debug;
 
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.Body;
 import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
 import com.badlogic.gdx.physics.box2d.FixtureDef;
 
 public class TetraActivity extends SimpleBaseGameActivity {
 
 	private static final int CAMERA_WIDTH = 720;
 	private static final int CAMERA_HEIGHT = 480;
 
 	private PhysicsWorld mPhysicsWorld;
 	private Camera mCamera;
 	private BuildableBitmapTextureAtlas mTextureAtlas;
 	private ITextureRegion mCometTextureRegion;
 	private ITextureRegion mPlanetTextureRegion;
 	
 	private float velocityModifier;
 
 	@Override
 	public EngineOptions onCreateEngineOptions() {
 		this.mCamera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
 		return new EngineOptions(true, ScreenOrientation.LANDSCAPE_SENSOR,
 				new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), mCamera);
 	}
 
 	@Override
 	protected void onCreateResources() {
 		mTextureAtlas = new BuildableBitmapTextureAtlas(
 				this.getTextureManager(), 1024, 1024,
 				TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
 
 		mCometTextureRegion = BitmapTextureAtlasTextureRegionFactory
 				.createFromAsset(mTextureAtlas, this, "comet.png");
 		mPlanetTextureRegion = BitmapTextureAtlasTextureRegionFactory
 				.createFromAsset(mTextureAtlas, this, "planet_earthlike1.png");
 
 		try {
 			this.mTextureAtlas
 					.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource, BitmapTextureAtlas>(
 							0, 1, 0));
 		} catch (final TextureAtlasBuilderException e) {
 			Debug.e(e);
 		}
 
 		this.mTextureAtlas.load();
 	}
 
 	@Override
 	protected Scene onCreateScene() {
 		this.mEngine.registerUpdateHandler(new FPSLogger());
 
 		final Scene scene = new Scene();
 
 		scene.setBackground(new Background(0f, 0f, 0f));
 
 		scene.setOnSceneTouchListener(new IOnSceneTouchListener() {
 
 			@Override
 			public boolean onSceneTouchEvent(Scene pScene,
 					TouchEvent pSceneTouchEvent) {
 
 				setYVelocityModifierTouch(pSceneTouchEvent.getX(), pSceneTouchEvent.getY());
 
 				return true;
 			}
 		});
 
 		this.mPhysicsWorld = new PhysicsWorld(new Vector2(0, 0), false);
 
 		/*
 		 * Calculate the coordinates for the comet, so its centered on the
 		 * camera.
 		 */
 		final float centerX = (CAMERA_WIDTH - this.mCometTextureRegion
 				.getWidth()) / 2;
 		final float centerY = (CAMERA_HEIGHT - this.mCometTextureRegion
 				.getHeight()) / 2;
 
 		/* Create the comet sprite and add it to the scene. */
 		final Sprite cometSprite = new Sprite(centerX, centerY,
 				0.12f * CAMERA_WIDTH, 0.12f * CAMERA_WIDTH,
 				this.mCometTextureRegion, this.getVertexBufferObjectManager());
 		scene.attachChild(cometSprite);
 
 		/* Create the comet body. */
 		FixtureDef cometFixtureDef = PhysicsFactory.createFixtureDef(1, 0.5f,
 				0.5f);
 		Body cometBody = PhysicsFactory.createCircleBody(this.mPhysicsWorld,
 				cometSprite, BodyType.DynamicBody, cometFixtureDef);
 		Comet comet = new Comet(cometSprite, cometBody);
 		this.mPhysicsWorld.registerPhysicsConnector(comet);
 		scene.registerUpdateHandler(mPhysicsWorld);
 
 		this.mCamera.setChaseEntity(cometSprite);
 
 		PlanetManager pManager = new PlanetManager(this.mEngine,
 				this.mPhysicsWorld);
 		scene.registerUpdateHandler(pManager);
 
 		PlanetSpawner pSpawner = new PlanetSpawner(this.mEngine,
 				this.mPhysicsWorld, pManager, comet, this.mPlanetTextureRegion);
 
 		CometVelocity cVelocity = new CometVelocity(this, cometBody);
 
 		scene.registerUpdateHandler(cVelocity);
 
 		scene.registerUpdateHandler(pSpawner);
 
 		return scene;
 	}
 	
 	public float getYVelocityModifier(){
 		return velocityModifier;
 	}
 	
 	public void setYVelocityModifier(float vMod) {
 		velocityModifier = vMod;
 	}
 	
 	
 	private void setYVelocityModifierTouch(float x, float y){
 		if (x > mCamera.getCenterX()){
 			velocityModifier = velocityModifier + 0.1f;
 		} else {
 			velocityModifier = velocityModifier - 0.1f;
 		}
 	}
 
 }
