 package com.example.sidescroll;
 
 import java.io.IOException;
 import java.text.DecimalFormat;
 
 import org.andengine.audio.music.Music;
 import org.andengine.audio.music.MusicFactory;
 import org.andengine.audio.sound.Sound;
 import org.andengine.audio.sound.SoundFactory;
 import org.andengine.engine.Engine;
 import org.andengine.engine.camera.Camera;
 import org.andengine.engine.camera.hud.controls.AnalogOnScreenControl;
 import org.andengine.engine.camera.hud.controls.BaseOnScreenControl;
 import org.andengine.engine.camera.hud.controls.AnalogOnScreenControl.IAnalogOnScreenControlListener;
 import org.andengine.engine.handler.IUpdateHandler;
 import org.andengine.engine.handler.physics.PhysicsHandler;
 import org.andengine.engine.handler.timer.ITimerCallback;
 import org.andengine.engine.handler.timer.TimerHandler;
 import org.andengine.engine.options.EngineOptions;
 import org.andengine.engine.options.ScreenOrientation;
 import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
 import org.andengine.entity.IEntity;
 import org.andengine.entity.IEntityMatcher;
 import org.andengine.entity.modifier.LoopEntityModifier;
 import org.andengine.entity.modifier.PathModifier;
 import org.andengine.entity.modifier.ScaleModifier;
 import org.andengine.entity.modifier.SequenceEntityModifier;
 import org.andengine.entity.modifier.PathModifier.IPathModifierListener;
 import org.andengine.entity.modifier.PathModifier.Path;
 import org.andengine.entity.scene.IOnAreaTouchListener;
 import org.andengine.entity.scene.IOnSceneTouchListener;
 import org.andengine.entity.scene.ITouchArea;
 import org.andengine.entity.scene.ITouchArea.ITouchAreaMatcher;
 import org.andengine.entity.scene.Scene;
 import org.andengine.entity.scene.background.Background;
 import org.andengine.entity.scene.background.RepeatingSpriteBackground;
 import org.andengine.entity.shape.IShape;
 import org.andengine.entity.sprite.AnimatedSprite;
 import org.andengine.entity.sprite.Sprite;
 import org.andengine.entity.text.Text;
 import org.andengine.input.touch.TouchEvent;
 import org.andengine.opengl.font.Font;
 import org.andengine.opengl.font.FontFactory;
 import org.andengine.opengl.texture.ITexture;
 import org.andengine.opengl.texture.TextureOptions;
 import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
 import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
 import org.andengine.opengl.texture.atlas.bitmap.BuildableBitmapTextureAtlas;
 import org.andengine.opengl.texture.atlas.bitmap.source.AssetBitmapTextureAtlasSource;
 import org.andengine.opengl.texture.atlas.bitmap.source.IBitmapTextureAtlasSource;
 import org.andengine.opengl.texture.atlas.buildable.builder.BlackPawnTextureAtlasBuilder;
 import org.andengine.opengl.texture.atlas.buildable.builder.ITextureAtlasBuilder.TextureAtlasBuilderException;
 import org.andengine.opengl.texture.region.ITextureRegion;
 import org.andengine.opengl.texture.region.TextureRegion;
 import org.andengine.opengl.texture.region.TiledTextureRegion;
 import org.andengine.opengl.util.GLState;
 import org.andengine.opengl.vbo.VertexBufferObjectManager;
 import org.andengine.opengl.view.RenderSurfaceView;
 import org.andengine.ui.activity.LayoutGameActivity;
 import org.andengine.util.debug.Debug;
 
 import android.graphics.Color;
 import android.graphics.Typeface;
 import android.opengl.GLES20;
 import android.provider.Settings.System;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.widget.Toast;
 
 public class MainActivity extends LayoutGameActivity implements
 		IOnSceneTouchListener, IOnAreaTouchListener {
 
 	// Scene properties
 	public enum SceneType {
 		SPLASH, MENU, OPTIONS, GAME,
 	}
 
 	public SceneType currentScene = SceneType.SPLASH;
 
 	// Camera
 	private static final int CAMERA_WIDTH = 800;
 	private static final int CAMERA_HEIGHT = 480;
 	private Camera mCamera;
 
 	// Scenes
 	// 1. Splash Scene
 	private Scene mSplashScene;
 	private BitmapTextureAtlas mSplashTextureAtlas;
 	private ITextureRegion mSplashTextureRegion;
 	private Sprite mSplash;
 
 	private Sprite mOverlay;
 	private BitmapTextureAtlas mOverlayTextureAtlas;
 	private ITextureRegion mOverlayTextureRegion;
 
 	// 2. Menu Scene
 	private Scene mMenuScene;
 	private TextureRegion mMainMenuBgTex, mPlayTex, mTitleTex, mCreditsTex,
 			mQuitTex, mBackTex;
 	private Sprite mMainMenuBg, mTitle, mPlay, mCredits, mQuit, mBack;
 
 	// 3. Options Scene
 	private Scene mOptionsScene;
 
 	// 4. Game Scene
 	private Scene mGameScene;
 	private AnimatedSprite mPlayer;
 	private AnimatedSprite mPet, mWolf, mBoar, mButterfly;
 	private Sprite mDeadTree, mNiceTree, mCocoTree, mPond;
 
 	// 5. Game Analog control
 	private BitmapTextureAtlas mOnScreenControlTexture;
 	private ITextureRegion mOnScreenControlBaseTextureRegion;
 	private ITextureRegion mOnScreenControlKnobTextureRegion;
 
 	private BuildableBitmapTextureAtlas mBitmapTextureAtlas;
 	private BitmapTextureAtlas mPetTextureAtlas;
 
 	private TiledTextureRegion mPlayerTextureRegion;
 	private TiledTextureRegion mAnimalsTextureRegion;
 	private RepeatingSpriteBackground mGrassBackground;
 	private TextureRegion mDeadTreeTextureRegion, mNiceTreeTextureRegion,
 			mCocoTreeTextureRegion, mPondTextureRegion;
 
 	// 6. Game Elements
 	private GameUpdateHandler mGameUpdateHandler;
 	private Sound mySound;
 	private Music heartBeat;
 	private Music waterSound;
 	private Sound wolfSound;
 	private Music grassWalk;
 	private Music nightSound;
 
 	private boolean movingFwd = true;
 
 	public float pLeftVolume = 1;
 
 	public float pRightVolume = 1;
 
 	// 7. Texts
 	private Font mFont, mWonLostFont;
 	private Text mEatenText, mEngineUpdateText, mEatsText;
 	private VertexBufferObjectManager mVertexBufferObjectManager;
 
 	@Override
 	public EngineOptions onCreateEngineOptions() {
 		mCamera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
 		EngineOptions engineOptions = new EngineOptions(true,
 				ScreenOrientation.LANDSCAPE_FIXED, new RatioResolutionPolicy(
 						CAMERA_WIDTH, CAMERA_HEIGHT), mCamera);
 		engineOptions.getAudioOptions().setNeedsMusic(true);
 		engineOptions.getAudioOptions().setNeedsSound(true);
 		return engineOptions;
 	}
 
 	@Override
 	public void onCreateResources(
 			OnCreateResourcesCallback pOnCreateResourcesCallback)
 			throws Exception {
 		mVertexBufferObjectManager = this.getVertexBufferObjectManager();
 		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
 		SoundFactory.setAssetBasePath("mfx/");
 		MusicFactory.setAssetBasePath("mfx/");
 		loadSplashSceneResources();
 		loadMainMenuResources();
 		loadGameSceneResources();
 		loadFont();
 		loadAudio();
 		pOnCreateResourcesCallback.onCreateResourcesFinished();
 	}
 
 	private void loadAudio() {
 		try {
 			this.heartBeat = MusicFactory.createMusicFromAsset(
 					this.mEngine.getMusicManager(), this, "heartbeat.ogg");
 			this.heartBeat.setLooping(true);
 
 			this.waterSound = MusicFactory.createMusicFromAsset(
 					this.mEngine.getMusicManager(), this, "water.ogg");
 			this.waterSound.setLooping(true);
 
 			this.wolfSound = SoundFactory.createSoundFromAsset(
 					this.getSoundManager(), this, "wolf.ogg");
 			this.wolfSound.setVolume(0.3f);
 
 			this.grassWalk = MusicFactory.createMusicFromAsset(
 					this.mEngine.getMusicManager(), this, "grasswalk.ogg");
 			this.grassWalk.setLooping(true);
 
 			this.nightSound = MusicFactory.createMusicFromAsset(
 					this.mEngine.getMusicManager(), this, "nightsound.ogg");
 			this.nightSound.setLooping(true);
 
 		} catch (final IOException e) {
 			Debug.e(e);
 		}
 
 	}
 
 	private void loadGameSceneResources() {
 		this.mBitmapTextureAtlas = new BuildableBitmapTextureAtlas(
 				this.getTextureManager(), 512, 512, TextureOptions.NEAREST);
 		this.mPlayerTextureRegion = BitmapTextureAtlasTextureRegionFactory
 				.createTiledFromAsset(this.mBitmapTextureAtlas, this,
 						"player.png", 4, 4);
 
 		try {
 			this.mBitmapTextureAtlas
 					.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource, BitmapTextureAtlas>(
 							0, 0, 1));
 			this.mBitmapTextureAtlas.load();
 		} catch (TextureAtlasBuilderException e) {
 			Debug.e(e);
 		}
 
 		this.mGrassBackground = new RepeatingSpriteBackground(CAMERA_WIDTH,
 				CAMERA_HEIGHT, this.getTextureManager(),
 				AssetBitmapTextureAtlasSource.create(this.getAssets(),
 						"gfx/background_grass.png"), mVertexBufferObjectManager);
 		this.mBitmapTextureAtlas.load();
 
 		loadAnimalResources();
 
 		// Analog control resources
 		this.mOnScreenControlTexture = new BitmapTextureAtlas(
 				this.getTextureManager(), 256, 128, TextureOptions.BILINEAR);
 		this.mOnScreenControlBaseTextureRegion = BitmapTextureAtlasTextureRegionFactory
 				.createFromAsset(this.mOnScreenControlTexture, this,
 						"onscreen_control_base.png", 0, 0);
 		this.mOnScreenControlKnobTextureRegion = BitmapTextureAtlasTextureRegionFactory
 				.createFromAsset(this.mOnScreenControlTexture, this,
 						"onscreen_control_knob.png", 128, 0);
 		this.mOnScreenControlTexture.load();
 
 		this.mOverlayTextureAtlas = new BitmapTextureAtlas(
 				this.getTextureManager(), CAMERA_WIDTH * 2, CAMERA_HEIGHT * 2);
 		this.mOverlayTextureRegion = BitmapTextureAtlasTextureRegionFactory
 				.createFromAsset(mOverlayTextureAtlas, this, "overlaybg4.png",
 						0, 0);
 		this.mOverlayTextureAtlas.load();
 
 	}
 
 	private void loadAnimalResources() {
 		// Pet resources
 		this.mPetTextureAtlas = new BitmapTextureAtlas(
 				this.getTextureManager(), 700, 700);
 
 		this.mAnimalsTextureRegion = BitmapTextureAtlasTextureRegionFactory
 				.createTiledFromAsset(this.mPetTextureAtlas, this,
 						"animals.png", 0, 0, 12, 8);
 
 		BitmapTextureAtlas treeTextureAtlas = new BitmapTextureAtlas(
 				this.getTextureManager(), 400, 400);
 		this.mDeadTreeTextureRegion = BitmapTextureAtlasTextureRegionFactory
 				.createFromAsset(treeTextureAtlas, this, "deadtree.png", 0, 0);
 		this.mNiceTreeTextureRegion = BitmapTextureAtlasTextureRegionFactory
 				.createFromAsset(treeTextureAtlas, this, "tree.png",
 						(int) mDeadTreeTextureRegion.getWidth() + 1, 0);
 		this.mCocoTreeTextureRegion = BitmapTextureAtlasTextureRegionFactory
 				.createFromAsset(treeTextureAtlas, this, "cocotree.png",
 						(int) mNiceTreeTextureRegion.getWidth() + 1, 0);
 		this.mPondTextureRegion = BitmapTextureAtlasTextureRegionFactory
 				.createFromAsset(treeTextureAtlas, this, "pond.png", 0,
 						(int) mDeadTreeTextureRegion.getHeight() + 50);
 		treeTextureAtlas.load();
 		this.mPetTextureAtlas.load();
 
 	}
 
 	@Override
 	public void onCreateScene(OnCreateSceneCallback pOnCreateSceneCallback)
 			throws Exception {
 		initSplashScreen();
 		pOnCreateSceneCallback.onCreateSceneFinished(this.mSplashScene);
 	}
 
 	@Override
 	public void onPopulateScene(Scene pScene,
 			OnPopulateSceneCallback pOnPopulateSceneCallback) throws Exception {
 		mEngine.registerUpdateHandler(new TimerHandler(4f,
 				new ITimerCallback() {
 
 					@Override
 					public void onTimePassed(TimerHandler pTimerHandler) {
 						mEngine.unregisterUpdateHandler(pTimerHandler);
 						mSplash.detachSelf();
 						setMenuScene();
 					}
 				}));
 		pOnPopulateSceneCallback.onPopulateSceneFinished();
 
 	}
 
 	public void setMenuScene() {
 		this.mMenuScene = new Scene();
 		mMainMenuBg = new Sprite(0, 0, this.mMainMenuBgTex,
 				mVertexBufferObjectManager);
 		mTitle = new Sprite(40, 60, this.mTitleTex, mVertexBufferObjectManager);
 		mPlay = new Sprite(220, 180, this.mPlayTex, mVertexBufferObjectManager);
 		mCredits = new Sprite(220, 250, this.mCreditsTex,
 				mVertexBufferObjectManager);
 		mQuit = new Sprite(CAMERA_WIDTH - 125, CAMERA_HEIGHT - 128,
 				this.mQuitTex, mVertexBufferObjectManager);
 		mQuit.setTag(501);
 		mBack = new Sprite(CAMERA_WIDTH - 125, CAMERA_HEIGHT - 128,
 				this.mBackTex, mVertexBufferObjectManager);
 		mBack.setTag(502);
 		mMenuScene.attachChild(mMainMenuBg);
 		mMenuScene.attachChild(mTitle);
 		mMenuScene.attachChild(mPlay);
 		mMenuScene.attachChild(mCredits);
 		mMenuScene.attachChild(mQuit);
 		mMenuScene.registerTouchArea(mPlay);
 		mMenuScene.registerTouchArea(mCredits);
 		mMenuScene.registerTouchArea(mQuit);
 		mMenuScene.registerTouchArea(mBack);
 		mMenuScene.setTouchAreaBindingOnActionDownEnabled(true);
 		mMenuScene.setOnAreaTouchListener(this);
 		this.mEngine.setScene(mMenuScene);
 	}
 
 	private void loadMainMenuResources() {
 		BitmapTextureAtlas btmpMenu = new BitmapTextureAtlas(
 				getTextureManager(), 2800, 2480);
 		mMainMenuBgTex = BitmapTextureAtlasTextureRegionFactory
 				.createFromAsset(btmpMenu, this, "menu/menubg.png", 0, 0);
 
 		mTitleTex = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
 				btmpMenu, this, "menu/title.png", 0,
 				(int) mMainMenuBgTex.getHeight() + 1);
 
 		mPlayTex = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
 				btmpMenu, this, "menu/play.png", 0,
 				(int) mMainMenuBgTex.getHeight() + (int) mTitleTex.getHeight());
 
 		mCreditsTex = BitmapTextureAtlasTextureRegionFactory
 				.createFromAsset(
 						btmpMenu,
 						this,
 						"menu/credits.png",
 						0,
 						((int) mMainMenuBgTex.getHeight()
 								+ (int) mTitleTex.getHeight() + (int) mMainMenuBgTex
 								.getHeight()));
 
 		mQuitTex = BitmapTextureAtlasTextureRegionFactory
 				.createFromAsset(
 						btmpMenu,
 						this,
 						"menu/quit.png",
 						0,
 						((int) mMainMenuBgTex.getHeight()
 								+ (int) mTitleTex.getHeight() + (int) mMainMenuBgTex
 								.getHeight())
 								+ (int) mMainMenuBgTex.getHeight());
 
 		mBackTex = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
 				btmpMenu,
 				this,
 				"menu/back.png",
 				0,
 				((int) mMainMenuBgTex.getHeight() + (int) mTitleTex.getHeight()
 						+ (int) mMainMenuBgTex.getHeight() + (int) mQuitTex
 						.getHeight()) + (int) mMainMenuBgTex.getHeight());
 
 		btmpMenu.load();
 	}
 
 	protected void setGameScene() {
 		mGameScene = new Scene();
 		mGameScene.setBackground(new Background(0.09804f, 0.6274f, 0.8784f));
 		mPlayer = new AnimatedSprite(250, 220, mPlayerTextureRegion,
 				mVertexBufferObjectManager);
 		mPlayer.setTag(1);
 		final long[] playerFrmTime = { 200, 200, 200, 200 };
 		final int[] playerFwdFrms = { 8, 9, 10, 11 };
 		final int[] playerBwdFrms = { 4, 5, 6, 7 };
 		mPlayer.animate(playerFrmTime, playerFwdFrms);
 
 		mGameScene.attachChild(mPlayer);
 		final PhysicsHandler physicsHandler = new PhysicsHandler(mPlayer);
 		mPlayer.registerUpdateHandler(physicsHandler);
 
 		// Pet and Background
 		mGameScene.setBackground(this.mGrassBackground);
 
 		/*
 		 * Calculate the coordinates for the face, so its centered on the
 		 * camera.
 		 */
 		final float centerX = (CAMERA_WIDTH - this.mAnimalsTextureRegion
 				.getWidth()) / 2;
 		final float centerY = (CAMERA_HEIGHT - this.mAnimalsTextureRegion
 				.getHeight()) / 2;
 
 		/* Create the sprite and add it to the scene. */
 		// trees
 		mDeadTree = new Sprite(70, 70, this.mDeadTreeTextureRegion,
 				mVertexBufferObjectManager);
 		mNiceTree = new Sprite(300, 40, this.mNiceTreeTextureRegion,
 				mVertexBufferObjectManager);
 		mCocoTree = new Sprite(420, 120, this.mCocoTreeTextureRegion,
 				mVertexBufferObjectManager);
 		mPond = new Sprite(350, 330, this.mPondTextureRegion,
 				mVertexBufferObjectManager);
 		mGameScene.attachChild(mDeadTree);
 		// mGameScene.attachChild(mNiceTree);
 		mGameScene.attachChild(mPond);
 
 		mPet = new AnimatedSprite(centerX, centerY, 24, 32,
 				this.mAnimalsTextureRegion, mVertexBufferObjectManager);
 		mPet.setTag(100);
 
 		mWolf = new AnimatedSprite(70, 160, 50, 70, this.mAnimalsTextureRegion,
 				mVertexBufferObjectManager);
 		mWolf.setTag(101);
 		long[] wolfFrmTime = { 200, 200, 200 };
 		int[] wolfFrm = { 66, 67, 68 };
 		mWolf.animate(wolfFrmTime, wolfFrm);
 
 		mBoar = new AnimatedSprite(centerX + 160, centerY - 50, 60, 80,
 				this.mAnimalsTextureRegion, mVertexBufferObjectManager);
 		mBoar.setTag(102);
 		long[] boarFrmTime = { 200, 200, 200 };
 		int[] boarFrm = { 72, 73, 74 };
 		mBoar.animate(boarFrmTime, boarFrm);
 
 		mButterfly = new AnimatedSprite(CAMERA_WIDTH - 50, centerY, 30, 50,
 				this.mAnimalsTextureRegion, mVertexBufferObjectManager);
 		mButterfly.setTag(103);
 		long[] bflyFrmTime = { 200, 200, 200 };
 		int[] bflyFrm = { 57, 58, 59 };
 		mButterfly.animate(bflyFrmTime, bflyFrm);
 
 		final Path path = new Path(10).to(10, 10).to(10, CAMERA_HEIGHT - 74)
 				.to(CAMERA_WIDTH - 58, CAMERA_HEIGHT - 74)
 				.to(CAMERA_WIDTH - 58, 10).to(CAMERA_WIDTH /2, 10)
 				.to(CAMERA_WIDTH /2, CAMERA_HEIGHT /2)
 				.to(10, 10).to(CAMERA_WIDTH -258, 10).to(CAMERA_WIDTH -258, CAMERA_HEIGHT/2+50).to(10, 10);
 
 		final Path path4 = new Path(5).to(10, 10)
 				.to(10, CAMERA_HEIGHT / 2 - 74)
 				.to(CAMERA_WIDTH - 58, CAMERA_HEIGHT - 74)
 				.to(CAMERA_WIDTH - 58, 10).to(10, 10);
 
 		final Path path5 = new Path(5).to(10, 10).to(10, CAMERA_HEIGHT - 74)
 				.to(CAMERA_WIDTH - 58, CAMERA_HEIGHT / 2 - 74)
 				.to(CAMERA_WIDTH - 58, 10).to(10, 10);
 
 		final Path path6 = new Path(5).to(CAMERA_WIDTH - 58, 10)
 				.to(CAMERA_WIDTH - 58, CAMERA_HEIGHT - 74)
 				.to(10, CAMERA_HEIGHT / 2 - 74).to(10, 10);
 
 		mPet.registerEntityModifier(new LoopEntityModifier(new PathModifier(100,
 				path, null, new IPathModifierListener() {
 					@Override
 					public void onPathStarted(final PathModifier pPathModifier,
 							final IEntity pEntity) {
 
 					}
 
 					@Override
 					public void onPathWaypointStarted(
 							final PathModifier pPathModifier,
 							final IEntity pEntity, final int pWaypointIndex) {
 						switch (pWaypointIndex) {
 						case 0:
 							mPet.animate(new long[] { 200, 200, 200 }, 0, 2,
 									true);
 							break;
 						case 1:
 							mPet.animate(new long[] { 200, 200, 200 }, 24, 26,
 									true);
 							break;
 						case 2:
 							mPet.animate(new long[] { 200, 200, 200 }, 36, 38,
 									true);
 							break;
 						case 3:
 							mPet.animate(new long[] { 200, 200, 200 }, 12, 14,
 									true);
 							break;
 						case 4:
 							mPet.animate(new long[] { 200, 200, 200 }, 0, 2,
 									true);
 							break;
 						case 5:
 							mPet.animate(new long[] { 200, 200, 200 }, 12, 14,
 									true);
 							break;
 						case 6:
 							mPet.animate(new long[] { 200, 200, 200 }, 24, 26,
 									true);
 							break;
 						case 7:
 							mPet.animate(new long[] { 200, 200, 200 }, 0, 2,
 									true);
 							break;
 						case 8:
 							mPet.animate(new long[] { 200, 200, 200 }, 12, 14,
 									true);
 							break;
 						case 9:
 							mPet.animate(new long[] { 200, 200, 200 }, 12, 14,
 									true);
 							break;
 						}
 					}
 
 					@Override
 					public void onPathWaypointFinished(
 							final PathModifier pPathModifier,
 							final IEntity pEntity, final int pWaypointIndex) {
 
 					}
 
 					@Override
 					public void onPathFinished(
 							final PathModifier pPathModifier,
 							final IEntity pEntity) {
 
 					}
 				})));
 		mGameScene.attachChild(mPet);
 
 		final Path path2 = new Path(5).to(70, 160).to(70, CAMERA_HEIGHT - 74)
 				.to(CAMERA_WIDTH / 2 - 58, CAMERA_HEIGHT / 2 - 74)
 				.to(CAMERA_WIDTH / 2 - 58, 160).to(70, 160);
 		mWolf.registerEntityModifier(new LoopEntityModifier(new PathModifier(
 				60, path2, null, new IPathModifierListener() {
 					@Override
 					public void onPathStarted(final PathModifier pPathModifier,
 							final IEntity pEntity) {
 
 					}
 
 					@Override
 					public void onPathWaypointStarted(
 							final PathModifier pPathModifier,
 							final IEntity pEntity, final int pWaypointIndex) {
 						switch (pWaypointIndex) {
 						case 0:
 							mWolf.animate(new long[] { 200, 200, 200 }, 54, 56,
 									true);
 							break;
 						case 1:
 							mWolf.animate(new long[] { 200, 200, 200 }, 78, 80,
 									true);
 							break;
 						case 2:
 							mWolf.animate(new long[] { 200, 200, 200 }, 90, 92,
 									true);
 							break;
 						case 3:
 							mWolf.animate(new long[] { 200, 200, 200 }, 66, 68,
 									true);
 							break;
 						}
 					}
 
 					@Override
 					public void onPathWaypointFinished(
 							final PathModifier pPathModifier,
 							final IEntity pEntity, final int pWaypointIndex) {
 
 					}
 
 					@Override
 					public void onPathFinished(
 							final PathModifier pPathModifier,
 							final IEntity pEntity) {
 
 					}
 				})));
 		mGameScene.attachChild(mWolf);
 
 		final Path path3 = new Path(5).to(centerX + 160, centerY - 50)
 				.to(centerX + 360, 0)
 				.to(CAMERA_WIDTH /2 - 58, CAMERA_HEIGHT / 2 - 74)
 				.to(CAMERA_WIDTH -70 - 58, centerY - 50)
 				.to(centerX + 160, centerY - 50);
 		mBoar.registerEntityModifier(new LoopEntityModifier(new PathModifier(
 				50, path3, null, new IPathModifierListener() {
 					@Override
 					public void onPathStarted(final PathModifier pPathModifier,
 							final IEntity pEntity) {
 
 					}
 
 					@Override
 					public void onPathWaypointStarted(
 							final PathModifier pPathModifier,
 							final IEntity pEntity, final int pWaypointIndex) {
 						switch (pWaypointIndex) {
 						case 0:
 							mBoar.animate(new long[] { 200, 200, 200 }, 84, 86,
 									true);
 							break;
 						case 1:
 							mBoar.animate(new long[] { 200, 200, 200 }, 60, 62,
 									true);
 							break;
 						case 2:
 							mBoar.animate(new long[] { 200, 200, 200 }, 72,74,
 									true);
 							break;
 						case 3:
 							mBoar.animate(new long[] { 200, 200, 200 },60, 62,
 									true);
 							break;
 						}
 					}
 
 					@Override
 					public void onPathWaypointFinished(
 							final PathModifier pPathModifier,
 							final IEntity pEntity, final int pWaypointIndex) {
 
 					}
 
 					@Override
 					public void onPathFinished(
 							final PathModifier pPathModifier,
 							final IEntity pEntity) {
 
 					}
 				})));
 		mGameScene.attachChild(mBoar);
 		mGameScene.attachChild(mCocoTree);
 		// mGameScene.attachChild(mButterfly);
 
 		// attach overlay image
 		float playerCenterX = this.mPlayer.getX() + this.mPlayer.getWidth() / 2;
 		float playerCenterY = this.mPlayer.getY() + this.mPlayer.getHeight()
 				/ 2;
 
 		mOverlay = new Sprite(0, 0, mOverlayTextureRegion,
 				this.getVertexBufferObjectManager());
 
 		float overlayX = playerCenterX - mOverlay.getWidth() / 2;
 		float overlayY = playerCenterY - mOverlay.getHeight() / 2 + 40;
 
 		mOverlay.setX(overlayX);
 		mOverlay.setY(overlayY);
 
 		mGameScene.attachChild(mOverlay);
 
 		final PhysicsHandler physicsHandler2 = new PhysicsHandler(mOverlay);
 		mPlayer.registerUpdateHandler(physicsHandler2);
 
 		// Analog control
 		final AnalogOnScreenControl analogOnScreenControl = new AnalogOnScreenControl(
 				0, CAMERA_HEIGHT
 						- this.mOnScreenControlBaseTextureRegion.getHeight(),
 				this.mCamera, this.mOnScreenControlBaseTextureRegion,
 				this.mOnScreenControlKnobTextureRegion, 0.1f, 200,
 				mVertexBufferObjectManager,
 				new IAnalogOnScreenControlListener() {
 					@Override
 					public void onControlChange(
 							final BaseOnScreenControl pBaseOnScreenControl,
 							final float pValueX, final float pValueY) {
 						float incX = 0, incY = 0;
 
 						if (pValueX > 0 && !movingFwd) {
 							mPlayer.animate(playerFrmTime, playerFwdFrms);
 							movingFwd = true;
 						}
 
 						if (pValueX < 0 && movingFwd) {
 							mPlayer.animate(playerFrmTime, playerBwdFrms);
 							movingFwd = false;
 						}
 
 						// left
 						if (mPlayer.getX() >= 0 && pValueX > 0) {
 							incX = pValueX * 100;
 						}
 
 						if (mPlayer.getX() < 0) {
 							mPlayer.setX(0);
 							mOverlay.setX(mPlayer.getWidth() / 2
 									- mOverlay.getWidth() / 2);
 							incX = 0;
 						}
 
 						// top
 						if (mPlayer.getY() >= 0 && pValueY > 0) {
 							incY = pValueY * 100;
 						}
 
 						if (mPlayer.getY() < 0) {
 							mPlayer.setY(0);
 							mOverlay.setY(mPlayer.getWidth() / 2
 									- mOverlay.getHeight() / 2 + 40);
 							incY = 0;
 						}
 
 						// right
 						if (mPlayer.getX() + mPlayer.getWidth() <= CAMERA_WIDTH
 								&& pValueX < 0) {
 							incX = pValueX * 100;
 						}
 
 						if (mPlayer.getX() + mPlayer.getWidth() > CAMERA_WIDTH - 20) {
 							mPlayer.setX(CAMERA_WIDTH - mPlayer.getWidth() - 20);
 							mOverlay.setX(mPlayer.getX() + mPlayer.getWidth()
 									/ 2 - mOverlay.getWidth() / 2);
 							incX = 0;
 						}
 
 						// bottom
 						if (mPlayer.getY() + mPlayer.getHeight() <= CAMERA_HEIGHT
 								&& pValueY < 0) {
 							incY = pValueY * 100;
 						}
 
 						if (mPlayer.getY() + mPlayer.getHeight() > CAMERA_HEIGHT - 20) {
 							mPlayer.setY(CAMERA_HEIGHT - mPlayer.getHeight()
 									- 20);
 							mOverlay.setY(mPlayer.getY() + mPlayer.getHeight()
 									/ 2 - mOverlay.getHeight() / 2 + 40);
 							incY = 0;
 						}
 
 						physicsHandler.setVelocity(incX, incY);
 						physicsHandler2.setVelocity(incX, incY);
 					}
 
 					@Override
 					public void onControlClick(
 							final AnalogOnScreenControl pAnalogOnScreenControl) {
 						// mPlayer.registerEntityModifier(new
 						// SequenceEntityModifier(
 						// new ScaleModifier(0.25f, 1, 1.5f),
 						// new ScaleModifier(0.25f, 1.5f, 1)));
 					}
 				});
 		analogOnScreenControl.getControlBase().setBlendFunction(
 				GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
 		analogOnScreenControl.getControlBase().setAlpha(0.5f);
 		analogOnScreenControl.getControlBase().setScaleCenter(0, 128);
 		analogOnScreenControl.getControlBase().setScale(1.25f);
 		analogOnScreenControl.getControlKnob().setScale(1.25f);
 		analogOnScreenControl.refreshControlKnobPosition();
 
 		mGameScene.setChildScene(analogOnScreenControl);
 		mGameUpdateHandler = new GameUpdateHandler();
 		mGameScene.registerUpdateHandler(mGameUpdateHandler);
 
 		mEngine.setScene(mGameScene);
 		this.runOnUiThread(new Runnable() {
 
 			@Override
 			public void run() {
 				// TODO Auto-generated method stub
 				Toast.makeText(MainActivity.this,
 						"Its dark our there! You have save your Pet.",
 						Toast.LENGTH_LONG).show();
 
 				Toast.makeText(MainActivity.this,
 						"Listen to your pets heart beating.", Toast.LENGTH_LONG)
 						.show();
 				Toast.makeText(MainActivity.this,
 						"Beware of other deadly animals and Environment.",
 						Toast.LENGTH_LONG).show();
 			}
 		});
 
 		// attach fonts
 		attachFonts(mGameScene);
 
 		// start music
 		this.heartBeat.play();
 		this.heartBeat.setVolume(0);
 
 		this.waterSound.play();
 		this.waterSound.setVolume(0);
 
 		this.grassWalk.play();
 		this.grassWalk.setVolume(0);
 
 		this.nightSound.play();
 		this.nightSound.setVolume(0.3f);
 
 		this.wolfSound.play();
 		this.wolfSound.setVolume(0.3f);
 	}
 
 	@Override
 	protected int getLayoutID() {
 		// TODO Auto-generated method stub
 		return R.layout.activity_main;
 	}
 
 	@Override
 	protected int getRenderSurfaceViewID() {
 		// TODO Auto-generated method stub
 		return R.id.xmllayoutRenderSurfaceView;
 	}
 
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		if (keyCode == KeyEvent.KEYCODE_BACK
 				&& event.getAction() == KeyEvent.ACTION_DOWN) {
 			switch (currentScene) {
 			case OPTIONS:
 			case GAME:
 				// mEngine.setScene(mMenuScene);
 				// currentScene = SceneType.MENU;
 				// break;
 			case MENU:
 			case SPLASH:
 				onBackPressed();
 			default:
 				break;
 			}
 		}
 
 		if (keyCode == KeyEvent.ACTION_UP) {
 			if (mPlayer.isAnimationRunning()) {
 				mPlayer.stopAnimation();
 			}
 		}
 		return false;
 	}
 
 	private void loadSplashSceneResources() {
 		// Splash screen
 		mSplashTextureAtlas = new BitmapTextureAtlas(getTextureManager(),
 				CAMERA_WIDTH, CAMERA_HEIGHT, TextureOptions.DEFAULT);
 		mSplashTextureRegion = BitmapTextureAtlasTextureRegionFactory
 				.createFromAsset(mSplashTextureAtlas, this, "splash.png", 0, 0);
 		mSplashTextureAtlas.load();
 	}
 
 	private void initSplashScreen() {
 		mSplashScene = new Scene();
 		mSplash = new Sprite(0, 0, mSplashTextureRegion,
 				getVertexBufferObjectManager()) {
 			protected void preDraw(GLState pGLState, Camera pCamera) {
 				super.preDraw(pGLState, pCamera);
 				pGLState.enableDither();
 			};
 		};
 		mSplashScene.attachChild(mSplash);
 	}
 
 	public float getCenter(float total, float size) {
 		return (total - size) / 2f;
 	}
 
 	static int i = 0;
 
 	@Override
 	public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent) {
 		Log.d("GAURAV ", "onSceneTouchEvent,pScene = " + pScene);
 		if (pSceneTouchEvent.isActionDown()) {
 			Sprite s = (Sprite) pScene.getChildByTag(mPlayer.getTag());
 			s.setPosition(s.getX() + (i++), s.getY());
 		}
 
 		if (pScene == mMenuScene) {
 
 		}
 
 		return false;
 	}
 
 	float mSeconds = 0.0f;
 	private int atecount = 10;
 
 	private class GameUpdateHandler implements IUpdateHandler {
 
 		@Override
 		public void onUpdate(float pSecondsElapsed) {
 
 			mSeconds = mSeconds + pSecondsElapsed;
 			int time = (int) mSeconds;
 			DecimalFormat format = new DecimalFormat("##.#");
 			String formatted = format.format(mSeconds);
 			mEngineUpdateText.setText(" " + formatted);
 
 			IShape pet = (IShape) mGameScene.getChildByTag(100);
 			if (mPlayer.collidesWith(pet)) {
 				mGameScene.detachChild(mPet);
 			} else if (mPlayer.collidesWith(mBoar)
 					|| mPlayer.collidesWith(mWolf)) {
				final String animal = mPlayer.collidesWith(mBoar) ? " a Boar":" a Wolf";
 				runOnUiThread(new Runnable() {
 
 					@Override
 					public void run() {
 						// TODO Auto-generated method stub
 						Toast.makeText(
 								MainActivity.this,
								"You were killed by "+animal+" . Beware of them in the night.",
 								Toast.LENGTH_LONG).show();
 					}
 				});
 				mGameScene.detachChild(mPlayer);
 			} else if (mPlayer.collidesWith(mPond)) {
 				runOnUiThread(new Runnable() {
 
 					@Override
 					public void run() {
 						// TODO Auto-generated method stub
 						Toast.makeText(
 								MainActivity.this,
 								"You drowned. Do not go near lake. Its dangerous.",
 								Toast.LENGTH_LONG).show();
 					}
 				});
 				mGameScene.detachChild(mPlayer);
 			}
 
 			if (mGameScene.getChildByTag(100) == null) {
 				showGameWon(time);
 			} else if (mGameScene.getChildByTag(1) == null) {
 				showGameOver(time);
 			}
 
 			if (time != 0 && time % 10 == 0) {
 				// mEatenText.setText((--atecount) + "");
 			}
 
 			// playSound
 			playIfNear(heartBeat, mPet, mPlayer, 200);
 
 			playIfNear(waterSound, mPond, mPlayer, 150);
 
 			playIfNear(grassWalk, mBoar, mPlayer, 200);
 
 			playIfNear(grassWalk, mWolf, mPlayer, 200);
 
 		}
 
 		@Override
 		public void reset() {
 			// TODO Auto-generated method stub
 
 		}
 	}
 
 	private void playIfNear(Music music, Sprite source, Sprite receiver,
 			int thresholdDist) {
 		float balance = (1 + (source.getX() - receiver.getX())
 				/ (CAMERA_WIDTH / 2)) / 2;
 		float distance = (float) Math
 				.sqrt((source.getX() - receiver.getX())
 						* (source.getX() - receiver.getX())
 						+ ((source.getY() - receiver.getY()) * (source.getY() - receiver
 								.getY())));
 		if (distance < thresholdDist) {
 			distance = distance / thresholdDist;
 			music.setVolume((pLeftVolume * (1 - balance)) * (1 - distance),
 					(pRightVolume * balance) * (1 - distance));
 		} else {
 			music.setVolume(0, 0);
 		}
 	}
 
 	private void showGameWon(final int time) {
 		int bonus = 0;
 		String congo = "";
 		if (time < 20) {
 			bonus = 100;
 			congo = "Awesome!";
 		} else if (time < 30) {
 			bonus = 50;
 			congo = "Congrats!";
 		} else if (time < 50) {
 			bonus = 30;
 			congo = "Try Harder!";
 		} else {
 			bonus = 10;
 			congo = "Dont you love your pet?";
 		}
 		// toastOnUIThread("Yaay! You saved your Pet. You took " + time
 		// + " secs to save him. Your Bonding is " + (time + bonus)
 		// + " points." + congo);
 		Log.d("GAURAV", "SHOWGAMEOVERCALLED");
 		mGameScene.clearChildScene();
 		mGameScene.detachChildren();
 		mGameScene.unregisterUpdateHandler(mGameUpdateHandler);
 
 		mEatsText = new Text(50, 150, mWonLostFont,
 				"\t\t\t\t\t\t\t\tYaay!\n You saved your Pet.\n You took "
 						+ time + " secs to save him.\n Your Bonding is "
 						+ (time/2 + bonus) + " points.\n " + congo,
 				mVertexBufferObjectManager);
 		mGameScene.attachChild(mEatsText);
 		mGameScene.setOnAreaTouchListener(this);
 		mGameScene.registerTouchArea(mBack);
 		mGameScene.attachChild(mBack);
 	}
 
 	private void showGameOver(final int time) {
 		// toastOnUIThread("Sorry! You were not able save your pet. You took "
 		// + time + " secs to search him. Your Bonding is 0 points.");
 		mGameScene.clearChildScene();
 		mGameScene.detachChildren();
 		mGameScene.unregisterUpdateHandler(mGameUpdateHandler);
 		// Create gameover image and attach it
 		mEatsText = new Text(50, 150, mWonLostFont,
 				"\t\t\t\t\t\t\t\tSorry!\n You were not able save your pet.\n You tried for "
 						+ time + " secs.\n Your Bonding is 0 points.",
 				mVertexBufferObjectManager);
 		mGameScene.attachChild(mEatsText);
 		mGameScene.setOnAreaTouchListener(this);
 		mGameScene.registerTouchArea(mBack);
 		mGameScene.attachChild(mBack);
 	}
 
 	private void loadFont() {
 		FontFactory.setAssetBasePath("font/");
 		final ITexture wonLostFontTexture = new BitmapTextureAtlas(
 				this.getTextureManager(), 400, 600, TextureOptions.BILINEAR);
 		// Font
 		this.mFont = FontFactory.create(getFontManager(), getTextureManager(),
 				100, 100, Typeface.create(Typeface.DEFAULT, Typeface.BOLD), 20,
 				Color.WHITE);
 		this.mFont.load();
 
 		this.mWonLostFont = FontFactory.createFromAsset(this.getFontManager(),
 				wonLostFontTexture, this.getAssets(), "Plok.ttf", 25, true,
 				Color.RED);
 		this.mWonLostFont.load();
 	}
 
 	private void attachFonts(Scene mScene) {
 		// Texts
 		mEatsText = new Text(10, 40, mFont, "Pets Life : ",
 				mVertexBufferObjectManager);
 		mEatenText = new Text(mEatsText.getWidth() + 10, 40, mFont, "10", 100,
 				mVertexBufferObjectManager);
 		Text update = new Text(10, 65, mFont, " Time : ", 100,
 				mVertexBufferObjectManager);
 		mEngineUpdateText = new Text(update.getWidth() + 10, 65, mFont, " 0 ",
 				100, mVertexBufferObjectManager);
 		// mScene.attachChild(mEatsText);
 		// mScene.attachChild(mEatenText);
 		mScene.attachChild(update);
 		mScene.attachChild(mEngineUpdateText);
 
 	}
 
 	@Override
 	public boolean onAreaTouched(TouchEvent pSceneTouchEvent,
 			ITouchArea pTouchArea, float pTouchAreaLocalX,
 			float pTouchAreaLocalY) {
 		if (pSceneTouchEvent.isActionDown() && pTouchArea.equals(mPlay)) {
 			mMenuScene.detachSelf();
 			setGameScene();
 		} else if (pSceneTouchEvent.isActionDown()
 				&& pTouchArea.equals(mCredits)) {
 			mMenuScene.detachChild(mTitle);
 			mMenuScene.detachChild(mPlay);
 			mMenuScene.detachChild(mCredits);
 			mMenuScene.detachChild(mQuit);
 
 			mEatsText = new Text(50, 120, mWonLostFont,
 					"\t\t\t\t\t\t\t\t Pankaj Bambhani\n\n"
 							+ "\t\t\t\t\t\t\t\t Soumik Pal\n\n"
 							+ "\t\t\t\t\t\t\t\t Kapil Ratnani\n\n"
 							+ "\t\t\t\t\t\t\t\t Gaurav Navgire\n\n",
 					mVertexBufferObjectManager);
 			mMenuScene.attachChild(mEatsText);
 			mMenuScene.attachChild(mBack);
 
 		} else if (pSceneTouchEvent.isActionDown() && pTouchArea.equals(mQuit)
 				&& mMenuScene.getChildByTag(501) == mQuit) {
 			finish();
 		} else if (pSceneTouchEvent.isActionDown()
 				&& pTouchArea.equals(mBack)
 				&& (mMenuScene.getChildByTag(502) == mBack || mGameScene
 						.getChildByTag(502) == mBack)) {
 			if (mEngine.getScene() != mMenuScene) {
				mSeconds=0.0f;
 				mGameScene.detachChildren();
 				mGameScene.detachSelf();
 				mEngine.setScene(mMenuScene);
 			} else {
 				mMenuScene.detachChild(mEatsText);
 				mMenuScene.detachChild(mBack);
 			}
 			if (!mTitle.hasParent()) {
 				mMenuScene.attachChild(mTitle);
 			}
 			if (!mPlay.hasParent()) {
 				mMenuScene.attachChild(mPlay);
 			}
 			if (!mCredits.hasParent()) {
 				mMenuScene.attachChild(mCredits);
 			}
 			if (!mQuit.hasParent()) {
 				mMenuScene.attachChild(mQuit);
 			}
 		}
 		return false;
 	}
 }
