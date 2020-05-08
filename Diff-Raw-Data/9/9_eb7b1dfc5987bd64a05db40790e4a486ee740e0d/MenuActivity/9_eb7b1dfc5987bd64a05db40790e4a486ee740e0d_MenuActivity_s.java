 package ddmp.projecttetra;
 
 import org.andengine.engine.camera.Camera;
 import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.EngineOptions.ScreenOrientation;
 import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
 import org.andengine.entity.scene.Scene;
 import org.andengine.entity.scene.background.Background;
 import org.andengine.entity.scene.menu.MenuScene;
 import org.andengine.entity.scene.menu.MenuScene.IOnMenuItemClickListener;
 import org.andengine.entity.scene.menu.item.IMenuItem;
 import org.andengine.entity.scene.menu.item.SpriteMenuItem;
 import org.andengine.entity.util.FPSLogger;
 import org.andengine.opengl.texture.TextureOptions;
 import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
 import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
 import org.andengine.opengl.texture.region.ITextureRegion;
 import org.andengine.ui.activity.SimpleBaseGameActivity;
 
 import android.content.Intent;
 import android.opengl.GLES20;
 import android.view.KeyEvent;
 
 public class MenuActivity extends SimpleBaseGameActivity implements
 		IOnMenuItemClickListener {
 	Camera mCamera;
 	private static final int CAMERA_WIDTH = 480;
 	private static final int CAMERA_HEIGHT = 800;
 
 	protected static final int MENU_PLAY = 0;
 	protected static final int MENU_SETTINGS = 1;
 	protected static final int MENU_QUIT = 2;
 	
 	protected static final int SETTINGS_MUTE = 3;
 	protected static final int SETTINGS_BACK = 4;
 
 	private BitmapTextureAtlas mBitmapTextureAtlas;
 
 	protected MenuScene mMenuScene;
 	protected MenuScene mSettingsScene;
 
 	private BitmapTextureAtlas mMenuTexture;
 	protected ITextureRegion mMenuPlayTextureRegion;
 	protected ITextureRegion mMenuQuitTextureRegion;
 	protected ITextureRegion mMenuSettingsTextureRegion;
 	
 	protected ITextureRegion mSettingsMuteTextureRegion;
 	protected ITextureRegion mSettingsBackTextureRegion;
 
 	@Override
 	public EngineOptions onCreateEngineOptions() {
 		this.mCamera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
 
 		return new EngineOptions(true, ScreenOrientation.PORTRAIT_FIXED,
 				new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT),
 				this.mCamera);
 	}
 
 	@Override
 	protected void onCreateResources() {
 		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
 
 		this.mBitmapTextureAtlas = new BitmapTextureAtlas(
 				this.getTextureManager(), 64, 64, TextureOptions.BILINEAR);
 		this.mBitmapTextureAtlas.load();
 
 		this.mMenuTexture = new BitmapTextureAtlas(this.getTextureManager(),
 				200, 750, TextureOptions.BILINEAR);
 		this.mMenuPlayTextureRegion = BitmapTextureAtlasTextureRegionFactory
 				.createFromAsset(this.mMenuTexture, this, "playButton.png", 0,
 						0);
 		this.mMenuSettingsTextureRegion = BitmapTextureAtlasTextureRegionFactory
 				.createFromAsset(this.mMenuTexture, this, "settingsButton.png", 0,
 						150);
 		this.mMenuQuitTextureRegion = BitmapTextureAtlasTextureRegionFactory
 				.createFromAsset(this.mMenuTexture, this, "quitButton.png", 0,
 						300);
 		this.mSettingsMuteTextureRegion = BitmapTextureAtlasTextureRegionFactory
 				.createFromAsset(this.mMenuTexture, this, "muteButton.png", 0,
 						450);
 		this.mSettingsBackTextureRegion = BitmapTextureAtlasTextureRegionFactory
 				.createFromAsset(this.mMenuTexture, this, "backButton.png", 0,
 						600);
 		this.mMenuTexture.load();
 	}
 
 	@Override
 	protected Scene onCreateScene() {
 		this.mEngine.registerUpdateHandler(new FPSLogger());
 		
 		this.mSettingsScene = createSettings();
 
 		this.mMenuScene = new MenuScene(this.mCamera);
 		
 		this.mMenuScene
 		.setBackground(new Background(0.05f, 0.05f, 0.05f));
 
 		this.mMenuScene.setBackgroundEnabled(true);
 		
 		final SpriteMenuItem playMenuItem = new SpriteMenuItem(MENU_PLAY,
 				this.mMenuPlayTextureRegion,
 				this.getVertexBufferObjectManager());
 		playMenuItem.setBlendFunction(GLES20.GL_SRC_ALPHA,
 				GLES20.GL_ONE_MINUS_SRC_ALPHA);
 		this.mMenuScene.addMenuItem(playMenuItem);
 		
 		final SpriteMenuItem settingsMenuItem = new SpriteMenuItem(MENU_SETTINGS,
 				this.mMenuSettingsTextureRegion,
 				this.getVertexBufferObjectManager());
 		playMenuItem.setBlendFunction(GLES20.GL_SRC_ALPHA,
 				GLES20.GL_ONE_MINUS_SRC_ALPHA);
 		this.mMenuScene.addMenuItem(settingsMenuItem);
 
 		final SpriteMenuItem quitMenuItem = new SpriteMenuItem(MENU_QUIT,
 				this.mMenuQuitTextureRegion,
 				this.getVertexBufferObjectManager());
 		quitMenuItem.setBlendFunction(GLES20.GL_SRC_ALPHA,
 				GLES20.GL_ONE_MINUS_SRC_ALPHA);
 		this.mMenuScene.addMenuItem(quitMenuItem);
 
 		this.mMenuScene.buildAnimations();
 
 		this.mMenuScene.setOnMenuItemClickListener(this);
 
 		return this.mMenuScene;
 	}
 
 	@Override
 	public boolean onKeyDown(final int pKeyCode, final KeyEvent pEvent) {
 		if (pKeyCode == KeyEvent.KEYCODE_BACK) {
 			this.finish();
 			return true;
 		} else {
 			
 		}
 		return false;
 	}
 
 	@Override
 	public boolean onMenuItemClicked(final MenuScene pMenuScene,
 			final IMenuItem pMenuItem, final float pMenuItemLocalX,
 			final float pMenuItemLocalY) {
 		switch (pMenuItem.getID()) {
 		case MENU_PLAY:
 			Intent myIntent = new Intent(MenuActivity.this, TetraActivity.class);
 			MenuActivity.this.startActivity(myIntent);
 			return true;
 		case MENU_SETTINGS:
 			this.mMenuScene.setChildScene(mSettingsScene);
 		case SETTINGS_MUTE:
 			return true;
 		case SETTINGS_BACK:
 			this.mSettingsScene.back();
 			this.mMenuScene.reset();
 			return true;
 		case MENU_QUIT:
 			this.finish();
 			return true;
 		default:
 			return false;
 		}
 	}
 	
 	protected MenuScene createSettings() {
 		MenuScene settingsScene = new MenuScene(this.mCamera);
 	
 		settingsScene.setBackground(new Background(0.05f, 0.05f, 0.05f));
 
 		settingsScene.setBackgroundEnabled(true);
 		
 		final SpriteMenuItem muteMenuItem = new SpriteMenuItem(SETTINGS_MUTE,
 				this.mSettingsMuteTextureRegion,
 				this.getVertexBufferObjectManager());
 		muteMenuItem.setBlendFunction(GLES20.GL_SRC_ALPHA,
 				GLES20.GL_ONE_MINUS_SRC_ALPHA);
 		settingsScene.addMenuItem(muteMenuItem);
 
 		final SpriteMenuItem backMenuItem = new SpriteMenuItem(SETTINGS_BACK,
 				this.mSettingsBackTextureRegion,
 				this.getVertexBufferObjectManager());
 		backMenuItem.setBlendFunction(GLES20.GL_SRC_ALPHA,
 				GLES20.GL_ONE_MINUS_SRC_ALPHA);
 		settingsScene.addMenuItem(backMenuItem);
 
 		settingsScene.buildAnimations();
 
 		settingsScene.setOnMenuItemClickListener(this);
 		
 		return settingsScene;
 	}
 
 }
