 package org.zeu.controller;
 
 import org.anddev.andengine.engine.Engine;
 import org.anddev.andengine.engine.camera.Camera;
 import org.anddev.andengine.engine.camera.hud.controls.AnalogOnScreenControl;
 import org.anddev.andengine.engine.options.EngineOptions;
 import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
 import org.anddev.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
 import org.anddev.andengine.entity.scene.Scene;
 import org.anddev.andengine.entity.scene.background.ColorBackground;
 import org.anddev.andengine.entity.util.FPSLogger;
 import org.anddev.andengine.extension.input.touch.controller.MultiTouch;
 import org.anddev.andengine.extension.input.touch.controller.MultiTouchController;
 import org.anddev.andengine.extension.input.touch.exception.MultiTouchException;
 import org.anddev.andengine.opengl.texture.TextureOptions;
 import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
 import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
 import org.anddev.andengine.opengl.texture.region.TextureRegion;
 import org.zeu.controller.model.CircleButton;
 import org.zeu.controller.model.HexagonButton;
 import org.zeu.controller.model.RotationJoystick;
 import org.zeu.controller.model.SquareButton;
 import org.zeu.controller.model.TriangleButton;
 import org.zeu.controller.model.VelocityJoystick;
 import org.zeu.controller.model.base.BaseGame;
 import org.zeu.controller.util.Network;
 import org.zeu.controller.util.Persistency;
 import org.zeu.controller.util.Settings;
 import org.zeu.controller.util.Util;
 
 import android.content.Intent;
 
 public class ZeuControllerActivity extends BaseGame {
 
 	public final int CAMERA_WIDTH = 480;
 	public final int CAMERA_HEIGHT = 320;
 	public final int PADDING = 32;
 
 	private Camera mCamera;
 
 	private BitmapTextureAtlas mBitmapTextureAtlas;
 	private TextureRegion mSquare, mHexagon, mTriangle, mCircle;
 
 	private BitmapTextureAtlas mOnScreenControlTexture;
 	private TextureRegion mOnScreenControlBaseTextureRegion;
 	private TextureRegion mOnScreenControlKnobTextureRegion;
 
 	private boolean mPlaceOnScreenControlsAtDifferentVerticalLocations = false;
 
 	private Network net;
 
 	@Override
 	public Engine onLoadEngine() {
 		// instantiate preferences so settings are initialized
 		new Persistency(getApplicationContext());
 
 		if (Settings.getInstance().showSettingsAtStartup) {
 			Intent settingsIntent = new Intent(this, SettingsActivity.class);
 			startActivity(settingsIntent);
 		}
 
 		mCamera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
 		RatioResolutionPolicy rrp = new RatioResolutionPolicy(CAMERA_WIDTH,
 				CAMERA_HEIGHT);
 		EngineOptions eo = new EngineOptions(true, ScreenOrientation.LANDSCAPE,
 				rrp, mCamera);
 		Engine engine = new Engine(eo);
 
 		try {
 			if (MultiTouch.isSupported(this)) {
 				engine.setTouchController(new MultiTouchController());
 				if (MultiTouch.isSupportedDistinct(this)) {
 				} else {
 					mPlaceOnScreenControlsAtDifferentVerticalLocations = true;
 				}
 			} else {
 				Util.toast(this, "Multitouch not supported");
 			}
 		} catch (final MultiTouchException e) {
 			Util.toast(this, "Multitouch not supported");
 		}
 
 		net = new Network();
		// net.connect(preferences.getUrl());
 
 		return engine;
 	}
 
 	@Override
 	public void onLoadResources() {
 		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
 
 		mBitmapTextureAtlas = new BitmapTextureAtlas(128, 128,
 				TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 		mSquare = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
 				mBitmapTextureAtlas, this, "square.png", 0, 0);
 		mHexagon = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
 				mBitmapTextureAtlas, this, "hexagon.png", 0, 64);
 		mTriangle = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
 				mBitmapTextureAtlas, this, "triangle.png", 64, 0);
 		mCircle = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
 				mBitmapTextureAtlas, this, "circle.png", 64, 64);
 
 		mOnScreenControlTexture = new BitmapTextureAtlas(256, 128,
 				TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 		mOnScreenControlBaseTextureRegion = BitmapTextureAtlasTextureRegionFactory
 				.createFromAsset(mOnScreenControlTexture, this,
 						"onscreen_control_base.png", 0, 0);
 		mOnScreenControlKnobTextureRegion = BitmapTextureAtlasTextureRegionFactory
 				.createFromAsset(mOnScreenControlTexture, this,
 						"onscreen_control_knob.png", 128, 0);
 
 		mEngine.getTextureManager().loadTextures(mBitmapTextureAtlas,
 				mOnScreenControlTexture);
 	}
 
 	@Override
 	public Scene onLoadScene() {
 		mEngine.registerUpdateHandler(new FPSLogger());
 
 		Scene scene = new Scene();
 		scene.setBackground(new ColorBackground(0.0f, 0.0f, 0.0f));
 
 		SquareButton square = new SquareButton(this, net);
 		scene.registerTouchArea(square);
 		scene.attachChild(square);
 
 		TriangleButton triangle = new TriangleButton(this, net);
 		scene.registerTouchArea(triangle);
 		scene.attachChild(triangle);
 
 		HexagonButton hexagon = new HexagonButton(this, net);
 		scene.registerTouchArea(hexagon);
 		scene.attachChild(hexagon);
 
 		CircleButton circle = new CircleButton(this, net);
 		scene.registerTouchArea(circle);
 		scene.attachChild(circle);
 
 		AnalogOnScreenControl velocityJoystick = new VelocityJoystick(this, net)
 				.getVelocityJoystick();
 		scene.setChildScene(velocityJoystick);
 
 		AnalogOnScreenControl rotationOnScreenControl = new RotationJoystick(
 				this, net).getRotationJoystick();
 		velocityJoystick.setChildScene(rotationOnScreenControl);
 
 		return scene;
 	}
 
 	@Override
 	public void onLoadComplete() {
 		net.connect(Settings.getInstance().url);
 	}
 
 	public Camera getCamera() {
 		return mCamera;
 	}
 
 	public boolean placeJoysticksAtDifferentVerticalLocations() {
 		return mPlaceOnScreenControlsAtDifferentVerticalLocations;
 	}
 
 	public TextureRegion getSquareTexture() {
 		return mSquare;
 	}
 
 	public TextureRegion getTriangleTexture() {
 		return mTriangle;
 	}
 
 	public TextureRegion getCircleTexture() {
 		return mCircle;
 	}
 
 	public TextureRegion getHexagonTexture() {
 		return mHexagon;
 	}
 
 	public TextureRegion getJoystickBaseTexture() {
 		return mOnScreenControlBaseTextureRegion;
 	}
 
 	public TextureRegion getJoystickKnobTexture() {
 		return mOnScreenControlKnobTextureRegion;
 	}
 
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
		net.disconnect();
 	}
 }
