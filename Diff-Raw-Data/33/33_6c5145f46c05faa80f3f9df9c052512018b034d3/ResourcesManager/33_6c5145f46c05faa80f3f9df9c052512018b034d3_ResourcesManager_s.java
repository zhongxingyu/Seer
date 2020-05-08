 package gamedev.game;
 
 import gamedev.hud.SceneHUD;
 import gamedev.objects.Player;
 
 import org.andengine.engine.Engine;
 import org.andengine.engine.camera.BoundCamera;
 import org.andengine.extension.physics.box2d.FixedStepPhysicsWorld;
 import org.andengine.extension.physics.box2d.PhysicsWorld;
 import org.andengine.opengl.font.Font;
 import org.andengine.opengl.font.FontFactory;
 import org.andengine.opengl.font.FontManager;
 import org.andengine.opengl.texture.ITexture;
 import org.andengine.opengl.texture.TextureManager;
 import org.andengine.opengl.texture.TextureOptions;
 import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
 import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
 import org.andengine.opengl.texture.region.ITextureRegion;
 import org.andengine.opengl.texture.region.ITiledTextureRegion;
 import org.andengine.opengl.vbo.VertexBufferObjectManager;
 import org.andengine.ui.activity.BaseActivity;
 
 import android.graphics.Color;
import android.graphics.Typeface;
 
 import com.badlogic.gdx.math.Vector2;
 
 /**
  * @author Mateusz Mysliwiec
  * @author www.matim-dev.com
  * @version 1.0
  */
 public class ResourcesManager {
 	// ---------------------------------------------
 	// VARIABLES
 	// ---------------------------------------------
 
 	private static final ResourcesManager INSTANCE = new ResourcesManager();
 
 	public Engine engine;
 	public BaseActivity activity;
 	public BoundCamera camera;
 	public VertexBufferObjectManager vbom;
 	public TextureManager textureManager;
 	public Player player;
 	public SceneHUD hud;
 
 	// ---------------------------------------------
 	// TEXTURES & TEXTURE REGIONS
 	// ---------------------------------------------
 
 	public BitmapTextureAtlas playerAtlas;
 	public ITiledTextureRegion playerRegion;
 	public BitmapTextureAtlas dinosaurGreenAtlas;
 	public ITiledTextureRegion dinosaurGreenRegion;
 	public BitmapTextureAtlas landscapeAtlas;
 
 	// Texture for splash scene
 	public ITextureRegion splash_region;
 	private BitmapTextureAtlas splashTextureAtlas;
 
 	// Textures for menu scene
 	public ITextureRegion menu_background_region;
 	public ITiledTextureRegion buttons_region;
 	private BitmapTextureAtlas menuTextureAtlas;
 	public Font font;
 	private FontManager fontManager;
 
 	// ---------------------------------------------
 	// Physic
 	// ---------------------------------------------
 
 	public PhysicsWorld physicsWorld;
 
 	// ---------------------------------------------
 	// CLASS LOGIC
 	// ---------------------------------------------
 
 	public void loadPlayerGraphics() {
 	}
 
 	public void loadMenuResources() {
 		loadMenuGraphics();
 		// loadMenuAudio();
 		loadMenuFonts();
 	}
 
 	public void loadGameResources() {
 		this.physicsWorld = new FixedStepPhysicsWorld(30, new Vector2(0, 0),
 				false, 8, 1);
 		loadGameGraphics();
 		loadHUD();
 		// loadGameFonts();
 		// loadGameAudio();
 	}
 
 	private void loadHUD() {
 		this.hud = new SceneHUD();
 		this.camera.setHUD(this.hud);
 	}
 
 	private void loadMenuGraphics() {
 		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/menu/");
 
 		this.menuTextureAtlas = new BitmapTextureAtlas(textureManager, 800,
 				600, TextureOptions.DEFAULT);
 
 		this.menu_background_region = BitmapTextureAtlasTextureRegionFactory
 				.createTiledFromAsset(this.menuTextureAtlas,
 						getInstance().activity, "menubackground.png", 0, 0, 1,
 						1);
 		this.buttons_region = BitmapTextureAtlasTextureRegionFactory
 				.createTiledFromAsset(this.menuTextureAtlas,
 						getInstance().activity, "menubuttons.png", 0, 0, 1, 3);
 
 		this.menuTextureAtlas.load();
 
 	}
 
 	private void loadMenuAudio() {
 
 	}
 
 	private void loadMenuFonts() {
 		FontFactory.setAssetBasePath("font/");
 		final ITexture mainFontTexture = new BitmapTextureAtlas(
 				getInstance().textureManager, 256, 256,
 				TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 
 		fontManager = new FontManager();
		this.font = new Font(fontManager, mainFontTexture, Typeface.create(
				Typeface.DEFAULT, Typeface.BOLD), 32, true, Color.WHITE);
		// font = FontFactory.createFromAsset(fontManager, mainFontTexture,
		// activity.getAssets(), "font.ttf", 10f, true, Color.WHITE);
		// font.load();
 		// this.textureManager.loadTexture(mainFontTexture);
 		this.fontManager.loadFont(font);
 	}
 
 	public void unloadMenuTextures() {
 		menuTextureAtlas.unload();
 	}
 
 	public void loadMenuTextures() {
 		menuTextureAtlas.load();
 	}
 
 	private void loadGameGraphics() {
 		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
 
 		// Player
 		this.playerAtlas = new BitmapTextureAtlas(getInstance().textureManager,
 				1056, 960, TextureOptions.DEFAULT);
 
 		this.playerRegion = BitmapTextureAtlasTextureRegionFactory
 				.createTiledFromAsset(this.playerAtlas, getInstance().activity,
 						"grey_caveman_0.5_asc.png", 0, 0, 22, 20);
 		this.playerAtlas.load();
 
 		this.dinosaurGreenAtlas = new BitmapTextureAtlas(
 				getInstance().textureManager, 1664, 2048,
 				TextureOptions.DEFAULT);
 
 		this.dinosaurGreenRegion = BitmapTextureAtlasTextureRegionFactory
 				.createTiledFromAsset(this.dinosaurGreenAtlas,
 						getInstance().activity, "green_dino_0.5_asc.png", 0, 0, 26,
 						32);
 
 		this.dinosaurGreenAtlas.load();
 
 		// this.landscapeAtlas = new BitmapTextureAtlas(
 		// getInstance().textureManager, 512, 1204);
 		// this.landscapeAtlas.load();
 	}
 
 	public ITextureRegion getRandomTreeTexture() {
 		return BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(
 				this.landscapeAtlas, getInstance().activity, "trees.png", 0, 0,
 				8, 4);
 	}
 
 	private void loadGameFonts() {
 
 	}
 
 	private void loadGameAudio() {
 
 	}
 
 	public void loadSplashScreen() {
 		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
 		splashTextureAtlas = new BitmapTextureAtlas(
 				getInstance().textureManager, 257, 25, TextureOptions.BILINEAR);
 		splash_region = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
				splashTextureAtlas, activity, "splash.png", 0, 0);
 		splashTextureAtlas.load();
 	}
 
 	public void unloadSplashScreen() {
 		splashTextureAtlas.unload();
 		splash_region = null;
 	}
 
 	/**
 	 * @param engine
 	 * @param activity
 	 * @param camera
 	 * @param vbom
 	 * <br>
 	 * <br>
 	 *            We use this method at beginning of game loading, to prepare
 	 *            Resources Manager properly, setting all needed parameters, so
 	 *            we can latter access them from different classes (eg. scenes)
 	 */
 	public static void prepareManager(Engine engine, BaseActivity activity,
 			BoundCamera camera, VertexBufferObjectManager vbom,
 			TextureManager textureManager) {
 		getInstance().engine = engine;
 		getInstance().activity = activity;
 		getInstance().camera = camera;
 		getInstance().vbom = vbom;
 		getInstance().textureManager = textureManager;
 
 		// We also load physics and player.. //TODO Move out from here, since a
 		// Menu scene does not require them in memory
 		// getInstance().loadPhysics();
 		// getInstance().loadGameResources();
 	}
 
 	// ---------------------------------------------
 	// GETTERS AND SETTERS
 	// ---------------------------------------------
 
 	public static ResourcesManager getInstance() {
 		return INSTANCE;
 	}
 }
