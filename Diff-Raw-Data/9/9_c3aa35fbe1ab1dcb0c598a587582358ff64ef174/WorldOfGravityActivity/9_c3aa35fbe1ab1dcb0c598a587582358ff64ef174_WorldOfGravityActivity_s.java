 package org.levasoft.worldofgravity;
 
 import static org.anddev.andengine.extension.physics.box2d.util.constants.PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.Vector;
 
 import org.anddev.andengine.engine.Engine;
 import org.anddev.andengine.engine.camera.SmoothCamera;
 import org.anddev.andengine.engine.handler.IUpdateHandler;
 import org.anddev.andengine.engine.options.EngineOptions;
 import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
 import org.anddev.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
 import org.anddev.andengine.entity.primitive.Rectangle;
 import org.anddev.andengine.entity.scene.Scene;
 import org.anddev.andengine.entity.scene.Scene.IOnSceneTouchListener;
 import org.anddev.andengine.entity.scene.background.SpriteBackground;
 import org.anddev.andengine.entity.shape.Shape;
 import org.anddev.andengine.entity.sprite.Sprite;
 import org.anddev.andengine.entity.text.ChangeableText;
 import org.anddev.andengine.extension.physics.box2d.PhysicsConnector;
 import org.anddev.andengine.extension.physics.box2d.PhysicsFactory;
 import org.anddev.andengine.extension.physics.box2d.PhysicsWorld;
 import org.anddev.andengine.input.touch.TouchEvent;
 import org.anddev.andengine.opengl.font.Font;
 import org.anddev.andengine.opengl.texture.TextureOptions;
 import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
 import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
 import org.anddev.andengine.opengl.texture.region.TextureRegion;
 import org.anddev.andengine.ui.activity.BaseGameActivity;
 
 import android.graphics.Color;
 import android.graphics.Typeface;
 import android.os.Bundle;
 import android.os.Environment;
 import android.view.KeyEvent;
 import android.widget.Toast;
 
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.Body;
 import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
 import com.badlogic.gdx.physics.box2d.Contact;
 import com.badlogic.gdx.physics.box2d.ContactImpulse;
 import com.badlogic.gdx.physics.box2d.ContactListener;
 import com.badlogic.gdx.physics.box2d.FixtureDef;
 import com.badlogic.gdx.physics.box2d.Manifold;
 
 public class WorldOfGravityActivity extends BaseGameActivity implements IOnSceneTouchListener {
 	// ===========================================================
 	// Constants
 	// ===========================================================
 
 	static final int CAMERA_WIDTH = 720;
 	static final int CAMERA_HEIGHT = 480;
 	
 	static final int BOX_WIDTH = 40;
 	static final int BOX_HEIGHT = 40;
 	
 	private static final FixtureDef BOX_FIXTURE_DEF = PhysicsFactory.createFixtureDef(0.1f, 0.1f, 5f);
 	private static final FixtureDef BALL_FIXTURE_DEF = PhysicsFactory.createFixtureDef(0.1f, 0.1f, 5f);
 	private static final FixtureDef WALL_FIXTURE_DEF = PhysicsFactory.createFixtureDef(0, 0.5f, 0.5f);
 	
 	// Gravity ratios
 	private static final float GRAVITY_INITIAL_FORCE_RATIO = 4f;
 	private static final float GRAVITY_TOUCH_FORCE_RATIO = 20f;
 
	private static final String LEVEL_PATH = "/Android/data/org.levasoft.worldofgravity";
	
 	// Wall color
 	private static final float WALL_COLOR_BLUE = 0;
 	private static final float WALL_COLOR_RED = 0;
 	private static final float WALL_COLOR_GREEN = 0;
 	
 	static final int ACTIVITY_RESULT_SELECT_LEVEL = 0;
 	static final int ACTIVITY_RESULT_NEXT_LEVEL = 1;
 	private static final int LEVEL_TEXT_COLOR = Color.WHITE;
 	private static final float LEVEL_TEXT_TOP = 5;
 	private static final float MAGNET_ALPHA_STEP = 0.05f;
 	private static final float MAGNET_ALPHA_MAX = 0.7f;
 		
 	
 	
 	// ===========================================================
 	// Fields
 	// ===========================================================
 
 	private Scene m_scene = null;
 
 	private PhysicsWorld m_physicsWorld;
 	private final SmoothCamera m_camera = new SmoothCamera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT, 1000, 1000, 10f);
 	
 	private TextureRegion m_backgroundTextureRegion = null;
 	private TextureRegion m_boxTextureRegion = null;
 	private TextureRegion m_magnetTextureRegion = null;
 	private TextureRegion m_ballTextureRegion = null;
 	private Sprite m_ballSprite = null;
 	private Sprite m_ballLightSprite = null;
 	Body m_ballBody = null;
 	private Vector<PhisObject> m_magnets = new Vector<PhisObject>();
 	private Vector<Sprite> m_stars = new Vector<Sprite>();
 
 
 	// 0: nothing
 	// 1: box
 	// 2: magnet
 	// 3: start
 	// 4: exit
 	// 18x12
 	private int[][] m_map = {
 			{3, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 1},
 			{0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 1},
 			{0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 1},
 			{0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 2, 4, 0, 0, 1, 0, 1},
 			{0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 1},
 			{1, 1, 2, 1, 1, 1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 1},
 			{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
 			{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
 			{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 2, 1, 1, 0, 0, 0},
 			{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
 			{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
 			{1, 1, 1, 1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 1}
 	};
 	private float m_forceRatio = GRAVITY_INITIAL_FORCE_RATIO;
 	private TextureRegion m_exitBoxTextureRegion;
 	private PhisObject m_exitBox;
 	private Vector2 m_gravityForce = null;
 	TextureRegion m_menuTextureRegion;
 	TextureRegion m_restartTextureRegion;
 	private float m_initialBoxX = 0;
 	private float m_initialBoxY = 0;
 	private TextureRegion m_ballLightTextureRegion;
 	private TextureRegion m_starTextureRegion;
 	private TextureRegion m_tutorialImageTextureRegion;
 	TextureRegion m_completeDialogTextureRegion;
 	TextureRegion m_nextButtonTextureRegion;
 	TextureRegion m_marketButtonTextureRegion;
 	TextureRegion m_starBigTextureRegion;
 	TextureRegion m_starBigBwTextureRegion;
 
 	private int m_starsCollected = 0;
 	private boolean m_levelComplete = false;
 	private int m_levelNo;
 	private Font m_font;
 	private ChangeableText m_levelText;
 	private float m_magnetAlpha = 1f;
 
 	// ===========================================================
 	// Constructors
 	// ===========================================================
 
 	// ===========================================================
 	// Getter & Setter
 	// ===========================================================
 
 	// ===========================================================
 	// Methods for/from SuperClass/Interfaces
 	// ===========================================================
 
 	@Override
 	public Engine onLoadEngine() {
 		final EngineOptions engineOptions = new EngineOptions(true, ScreenOrientation.LANDSCAPE, new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), m_camera);
 		engineOptions.getTouchOptions().setRunOnUpdateThread(true);
 		return new Engine(engineOptions);
 	}
 	
 	@Override
 	public void onLoadResources() {
 		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
 
 		/* Textures. */
 		{
 			BitmapTextureAtlas bitmapTextureAtlas = new BitmapTextureAtlas(1024, 2048, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 			int y = 0;
 			m_magnetTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(bitmapTextureAtlas, this, "magnet.png", 0, y += 0); // 40x40
 			m_exitBoxTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(bitmapTextureAtlas, this, "exit.png", 0, y += 40); // 40x40
 			m_ballTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(bitmapTextureAtlas, this, "ball.png", 0, y += 40); // 40x40 max
 			m_ballLightTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(bitmapTextureAtlas, this, "ball_light.png", 0, y += 40); // 40x40 max
 			m_starTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(bitmapTextureAtlas, this, "star.png", 0, y += 40); // 40x40
 			m_starBigTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(bitmapTextureAtlas, this, "star_big.png", 0, y += 40); // 80x80
 			m_starBigBwTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(bitmapTextureAtlas, this, "star_big_bw.png", 0, y += 80); // 80x80
 			m_menuTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(bitmapTextureAtlas, this, "button_menu.png", 0, y += 80); // 80x80
 			m_nextButtonTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(bitmapTextureAtlas, this, "button_next.png", 0, y += 80); // 80x80
 			m_restartTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(bitmapTextureAtlas, this, "button_restart.png", 0, y += 80); // 80x80
 			m_marketButtonTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(bitmapTextureAtlas, this, "button_maket_feedback.png", 0, y += 80); // 80x80
 			m_completeDialogTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(bitmapTextureAtlas, this, "dialog.png", 0, y += 80); // 720x480
 			m_tutorialImageTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(bitmapTextureAtlas, this, "tutorial.png", 0, y += 480); // 360x200
 			m_backgroundTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(bitmapTextureAtlas, this, "back.png", 0, y += 200); // 720x480
 			mEngine.getTextureManager().loadTexture(bitmapTextureAtlas);
 		}
 
 		// Box
 		{
 			// Using REPEATING_NEAREST_PREMULTIPLYALPHA because the box is repeating object
 			BitmapTextureAtlas boxBitmapTextureAtlas = new BitmapTextureAtlas(64, 64, TextureOptions.REPEATING_NEAREST_PREMULTIPLYALPHA);
 			m_boxTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(boxBitmapTextureAtlas, this, "box.png", 0, 0); // 40x40
 			mEngine.getTextureManager().loadTexture(boxBitmapTextureAtlas);
 		}
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		
 		//m_scene.unregisterUpdateHandler(m_physicsWorld);
 		//m_physicsWorld.dispose();
 	}
 
 	@Override
 	public Scene onLoadScene() {
 		m_scene = new Scene();
 
 		m_scene.setOnSceneTouchListener(this);
 		
 		// Set sky background
 		Sprite backSprite = new Sprite(0, 0, m_backgroundTextureRegion);
 		m_scene.setBackground(new SpriteBackground(backSprite));
 
 		m_physicsWorld = //PhysicsWorldManager.getWorld(); 
 				new PhysicsWorld(new Vector2(0, 0.0001f), false);
 				//new FixedStepPhysicsWorld(30, new Vector2(0, 0), false, 3, 2);
 
 		createRoom();
 
 		createBall();
 
 		loadLevel();
 		
 		createBoxes();
 		
 		restoreStars();
 
 		initLevelText();
 
 		
 		//m_camera.setZoomFactor(2);
 		
 		m_scene.registerUpdateHandler(this.m_physicsWorld);
 		
 		IUpdateHandler updateHandler = new IUpdateHandler() {
 
 			@Override
 			public void reset() { }
 
 			@Override
 			public void onUpdate(final float pSecondsElapsed) {
 				Vector2 force = new Vector2(0, 0);
 				for (int i = 0; i < m_magnets.size(); ++i) {
 					PhisObject magnet = m_magnets.get(i);
 					final float distance = magnet.getBody().getWorldCenter().dst(m_ballBody.getWorldCenter());
 					m_gravityForce = magnet.getBody().getWorldCenter().sub(m_ballBody.getWorldCenter()).nor().mul(
 							GRAVITY_INITIAL_FORCE_RATIO*m_forceRatio/(distance*distance));
 					force.add(m_gravityForce);
 				}
 				m_ballBody.applyForce(force, m_ballBody.getWorldCenter());
 				
 				updateBallLightSpritePosition();
 				
 				checkStarCollision();
 				
 				checkIfLevelComplete();
 				
 				animateMagnets();
 			}
 		};
 		m_scene.registerUpdateHandler(updateHandler);
 
 		m_physicsWorld.setContactListener(new ContactListener() {
 			@Override
 			public void beginContact(final Contact pContact) {
 				
 				if (pContact == null || 
 						pContact.getFixtureA() == null || pContact.getFixtureA().getBody() == null ||
 						pContact.getFixtureB() == null || pContact.getFixtureB().getBody() == null) {
 					return;
 				}
 				
 				if (pContact.getFixtureA().getBody() == m_exitBox.getBody() ||
 						pContact.getFixtureB().getBody() == m_exitBox.getBody()) {
 					m_levelComplete = true;
 				} 
 				
 			}
 			
 			@Override
 			public void endContact(final Contact pContact) {
 			}
 
 
 			@Override
 			public void preSolve(Contact contact, Manifold oldManifold) {
 				// TODO Auto-generated method stub
 				
 			}
 
 			@Override
 			public void postSolve(Contact contact, ContactImpulse impulse) {
 				// TODO Auto-generated method stub
 				
 			}
 		});
 		
 		Sprite menuSprite = new Sprite(5, 5, m_menuTextureRegion) {
 			@Override
 			public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
 				if (pSceneTouchEvent.isActionDown()) {
 					// exit to select level activity
 					setResult(ACTIVITY_RESULT_SELECT_LEVEL);
 					finish();
 					return  true;
 				}
 				return false;
 			}
 		};
 		m_scene.registerTouchArea(menuSprite);
 		menuSprite.setAlpha(0.8f);
 		m_scene.attachChild(menuSprite);
 
 		Sprite restartSprite = new Sprite(635, 5, m_restartTextureRegion) {
 			@Override
 			public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
 				if (pSceneTouchEvent.isActionDown()) {
 					// restart level
 					restartLevel();
 					return true;
 				}
 				return false;
 			}
 		};
 		m_scene.registerTouchArea(restartSprite);
 		restartSprite.setAlpha(0.8f);
 		m_scene.attachChild(restartSprite);
 		
 		showTutorialImage();		
 
 		return m_scene;
 	}
 	
 	private void showTutorialImage() {
 		if (m_levelNo != 1) {
 			return;
 		}
 		Sprite img = new Sprite(0, 200, m_tutorialImageTextureRegion);
 		m_scene.attachChild(img);
 		img.setAlpha(0.5f);
 	}
 
 	private void initLevelText() {
 		BitmapTextureAtlas fontTexture = new BitmapTextureAtlas(256, 256, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 
 		m_font = new Font(fontTexture, Typeface.create(Typeface.DEFAULT, Typeface.BOLD), 
 				32, true, LEVEL_TEXT_COLOR);
 
 		mEngine.getTextureManager().loadTexture(fontTexture);
 		mEngine.getFontManager().loadFont(m_font);
 		
 		m_levelText = new ChangeableText(0, 0, m_font, "Level 000");
 		m_levelText.setPosition(CAMERA_WIDTH/2 - m_levelText.getWidth()/2, LEVEL_TEXT_TOP);
 		m_scene.attachChild(m_levelText);
 		m_levelText.setText("Level " + m_levelNo);
 	}
 
 	protected void checkStarCollision() {
 		for (int i = 0; i < m_stars.size(); ++i) {
 			Sprite star = m_stars.get(i);
 			if (star.isVisible() && star.collidesWith(m_ballSprite)) {
 				// collision detected
 				star.setVisible(false);
 				++m_starsCollected;
 			}
 		}
 	}
 
 	protected void restartLevel() {
 		m_ballBody.setTransform(m_initialBoxX/PIXEL_TO_METER_RATIO_DEFAULT, 
 				m_initialBoxY/PIXEL_TO_METER_RATIO_DEFAULT, 0);
 		m_ballBody.setLinearVelocity(0, 0);
 		m_ballBody.setAngularVelocity(0);
 		m_magnetAlpha = 1;
 		
 		restoreStars();
 		
 		m_levelComplete = false;
 	}
 
 	private void restoreStars() {
 		m_starsCollected = 0;
 		for (int i = 0; i < m_stars.size(); ++i) {
 			Sprite star = m_stars.get(i);
 			star.setVisible(true);
 		}
 	}
 
 	private void loadLevel() {
 		Bundle bun = getIntent().getExtras();
 		int levelNo = bun.getInt("level", 0);
 		m_levelNo = levelNo; // 1-based level number
 
 		if (LevelMap.getLevel(levelNo - 1, m_map)) {
 			// we got it from the static
 			return;
 		}
 	}
 
 	private void createBoxes() {
 		m_exitBox = new PhisObject(
 				m_exitBoxTextureRegion, m_physicsWorld, m_scene, BOX_FIXTURE_DEF);
 		
 		m_stars.clear();
 
 		for (int row = 0; row < m_map.length; ++row) {
 			int[] aRow = m_map[row];
 			for (int col = 0; col < aRow.length; ++col) {
 				int cell = aRow[col];
 				if (cell == 0) {
 					continue;
 				}
 				final int x = col*BOX_WIDTH + BOX_WIDTH/2;
 				final int y = row*BOX_HEIGHT + BOX_HEIGHT/2;
 				if (cell == 1 || cell == 2) {
 					PhisObject box = new PhisObject(
 							cell == 1 ? m_boxTextureRegion : m_magnetTextureRegion, 
 							m_physicsWorld, m_scene, BOX_FIXTURE_DEF);
 					box.move(x, y);
 					if (cell == 2) {
 						m_magnets.add(box);
 					}
 				}
 				
 				if (cell == 3) {
 					// start position
 					m_initialBoxX = x;
 					m_initialBoxY = y;
 					m_ballBody.setTransform(x/PIXEL_TO_METER_RATIO_DEFAULT, y/PIXEL_TO_METER_RATIO_DEFAULT, 0);
 				}
 				
 				if (cell == 4) {
 					// exit position 
 					m_exitBox.move(x, y);
 				}
 				
 				if (cell == 5) {
 					Sprite star = new Sprite(0, 0, m_starTextureRegion);
 					m_scene.attachChild(star);
 					star.setPosition(x - star.getWidth()/2, y - star.getBaseHeight()/2);
 					m_stars.add(star);
 				}
 			}
 		}
 	}
 
 	private void createRoom() {
 		final Shape ground = new Rectangle(0, CAMERA_HEIGHT - 2, CAMERA_WIDTH, 2);
 		final Shape roof = new Rectangle(0, 0, CAMERA_WIDTH, 2);
 		final Shape left = new Rectangle(0, 0, 2, CAMERA_HEIGHT);
 		final Shape right = new Rectangle(CAMERA_WIDTH - 2, 0, 2, CAMERA_HEIGHT);
 
 		PhysicsFactory.createBoxBody(this.m_physicsWorld, ground, BodyType.StaticBody, WALL_FIXTURE_DEF);
 		PhysicsFactory.createBoxBody(this.m_physicsWorld, roof, BodyType.StaticBody, WALL_FIXTURE_DEF);
 		PhysicsFactory.createBoxBody(this.m_physicsWorld, left, BodyType.StaticBody, WALL_FIXTURE_DEF);
 		PhysicsFactory.createBoxBody(this.m_physicsWorld, right, BodyType.StaticBody, WALL_FIXTURE_DEF);
 
 		m_scene.attachChild(ground);
 		m_scene.attachChild(roof);
 		m_scene.attachChild(left);
 		m_scene.attachChild(right);
 		
 		ground.setColor(WALL_COLOR_RED, WALL_COLOR_GREEN, WALL_COLOR_BLUE);
 		roof.setColor(WALL_COLOR_RED, WALL_COLOR_GREEN, WALL_COLOR_BLUE);
 		left.setColor(WALL_COLOR_RED, WALL_COLOR_GREEN, WALL_COLOR_BLUE);
 		right.setColor(WALL_COLOR_RED, WALL_COLOR_GREEN, WALL_COLOR_BLUE);
 	}
 
 	private void createBall() {
 		
 		m_ballSprite = new Sprite(10, 10, m_ballTextureRegion);
 		m_ballBody = PhysicsFactory.createCircleBody(m_physicsWorld, m_ballSprite, BodyType.DynamicBody, BALL_FIXTURE_DEF);
 		m_scene.attachChild(m_ballSprite);
 		m_physicsWorld.registerPhysicsConnector(new PhysicsConnector(m_ballSprite, m_ballBody, true, true));
 		
 		m_ballLightSprite = new Sprite(0, 0, m_ballLightTextureRegion);
 		m_scene.attachChild(m_ballLightSprite);
 	}
 
 	@Override
 	public boolean onKeyDown(final int pKeyCode, final KeyEvent pEvent) {
 		// Handle menu button
 		//
 		return super.onKeyDown(pKeyCode, pEvent);
 	}
 
 	@Override
 	public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent) {
 		if (pSceneTouchEvent.isActionDown()) {
 			m_forceRatio = GRAVITY_TOUCH_FORCE_RATIO;
 			return true;
 		} else if (pSceneTouchEvent.isActionUp()) {
 			m_forceRatio = GRAVITY_INITIAL_FORCE_RATIO;
 			return true;
 		} 
 		return false;
 	}
 
 	private void updateBallLightSpritePosition() {
 		m_ballLightSprite.setPosition(m_ballSprite);
 	}
 
 
 	protected void nextLevel() {
 		setResult(ACTIVITY_RESULT_NEXT_LEVEL);
 		restartLevel();
 		finish();
 	}
 
 	public Scene getScene() {
 		return m_scene;
 	}
 
 	private void checkIfLevelComplete() {
 		if (!m_levelComplete) {
 			return;
 		}
 		
 		StarsAchivedManager.saveAchivement(this, m_levelNo, m_starsCollected);
 		
 		LevelCompleteDialog dialog = new LevelCompleteDialog(this);
 		dialog.showDialog(m_starsCollected);
 		m_levelComplete = false;
 		
 		StarsAchivedManager.setLevelPassed(this, m_levelNo);
 	}
 	
 	private void animateMagnets() {
 		if (m_forceRatio == GRAVITY_INITIAL_FORCE_RATIO) {
 			m_magnetAlpha = 1f;
 		} else {
 			if (m_magnetAlpha > MAGNET_ALPHA_MAX) {
 				m_magnetAlpha -= MAGNET_ALPHA_STEP;
 			}
 		}
 
 		for (int i = 0; i < m_magnets.size(); ++i) {
 			Sprite magnet = m_magnets.get(i).getSprite();
 			magnet.setAlpha(m_magnetAlpha);
 		}
 	}
 	
 
 	// ===========================================================
 	// Inner and Anonymous Classes
 	// ===========================================================
 }
