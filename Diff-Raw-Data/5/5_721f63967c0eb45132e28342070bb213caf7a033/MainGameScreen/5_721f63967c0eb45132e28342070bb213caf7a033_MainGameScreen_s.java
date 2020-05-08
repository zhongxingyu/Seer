 package com.gradugation;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Random;
 
 import org.andengine.audio.music.Music;
 import org.andengine.audio.music.MusicFactory;
 import org.andengine.engine.camera.BoundCamera;
 import org.andengine.engine.camera.SmoothCamera;
 import org.andengine.engine.camera.hud.HUD;
 import org.andengine.engine.handler.IUpdateHandler;
 import org.andengine.engine.options.EngineOptions;
 import org.andengine.engine.options.ScreenOrientation;
 import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
 import org.andengine.entity.IEntity;
 import org.andengine.entity.modifier.MoveModifier;
 import org.andengine.entity.primitive.Rectangle;
 import org.andengine.entity.scene.CameraScene;
 import org.andengine.entity.scene.IOnSceneTouchListener;
 import org.andengine.entity.scene.Scene;
 import org.andengine.entity.sprite.Sprite;
 import org.andengine.entity.text.Text;
 import org.andengine.entity.util.FPSLogger;
 import org.andengine.extension.tmx.TMXLayer;
 import org.andengine.extension.tmx.TMXLoader;
 import org.andengine.extension.tmx.TMXLoader.ITMXTilePropertiesListener;
 import org.andengine.extension.tmx.TMXProperties;
 import org.andengine.extension.tmx.TMXTile;
 import org.andengine.extension.tmx.TMXTileProperty;
 import org.andengine.extension.tmx.TMXTiledMap;
 import org.andengine.extension.tmx.util.exception.TMXLoadException;
 import org.andengine.input.touch.TouchEvent;
 import org.andengine.opengl.font.Font;
 import org.andengine.opengl.font.StrokeFont;
 import org.andengine.opengl.texture.EmptyTexture;
 import org.andengine.opengl.texture.ITexture;
 import org.andengine.opengl.texture.TextureOptions;
 import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
 import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
 import org.andengine.opengl.texture.bitmap.AssetBitmapTexture;
 import org.andengine.opengl.texture.region.ITextureRegion;
 import org.andengine.opengl.texture.region.TextureRegion;
 import org.andengine.opengl.texture.region.TextureRegionFactory;
 import org.andengine.opengl.texture.region.TiledTextureRegion;
 import org.andengine.opengl.vbo.VertexBufferObjectManager;
 import org.andengine.ui.activity.SimpleBaseGameActivity;
 import org.andengine.util.Constants;
 import org.andengine.util.debug.Debug;
 
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.sqlite.SQLiteDatabase;
 import android.graphics.Color;
 import android.graphics.Typeface;
 import android.os.Bundle;
 import android.os.CountDownTimer;
 import android.os.Handler;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.View;
 import android.widget.Toast;
 
 import com.coordinates.MapCoordinate;
 import com.coordinates.SpriteCoordinate;
 
 public class MainGameScreen extends SimpleBaseGameActivity implements
 		IOnSceneTouchListener {
 	// ===========================================================
 	// Constants
 	// ===========================================================
 
 	private static final int CAMERA_WIDTH = 480;
 	private static final int CAMERA_HEIGHT = 320;
 	
 	//private static final int MAX_CHARACTER_MOVEMENT = 3;
 	public static final int CHARACTER_WIDTH = 32;
 	private static final String MAP_NAME = "map_text_file.txt";
 
 	// ===========================================================
 	// Fields
 	// ===========================================================
 
 	// Need handler for callbacks to the UI thread
     final Handler mHandler = new Handler();
 
     // Create runnable for posting
     final Runnable mUpdateResults = new Runnable() {
         public void run() {
             askDirection();
         }
     };
     
     private AlertDialog.Builder alertDialogBuilder;
 	private AlertDialog alertDialog;
 
 	private BoundCamera mCamera;
 
 	private ITexture mPlayerTexture;
 	private TiledTextureRegion mPlayerTextureRegion;
 
 	private TMXTiledMap mTMXTiledMap;
 	protected int mCactusCount;
 	private int CREDITS_NEEDED_GRADUATE = 15;
 
 	//private BitmapTextureAtlas characterTextureAtlas,characterTextureAtlas2,characterTextureAtlas3,characterTextureAtlas4;
 	//public ITextureRegion character,character2,character3,character4;
 
 	private BitmapTextureAtlas[] characterTextureAtlas;
 	private ITextureRegion[] character;
 
 	private BitmapTextureAtlas diceTextureAtlas;
 	private TextureRegion diceTextureRegion;
 	private BitmapTextureAtlas finishTurnTextureAtlas;
 	private TextureRegion finishTurnTextureRegion;
 	private BitmapTextureAtlas musicTextureAtlas;
 	private ITextureRegion mMusicTextureRegion;
 	
 	private ITexture mFaceTexture;
 	private ITextureRegion mFaceTextureRegion;
 	private ITexture mPausedTexture, mResumeTexture, mMainMenuTexture, mSaveTexture;
 	private ITextureRegion mPausedTextureRegion, mResumeTextureRegion,
 			mMainMenuTextureRegion, mSaveTextureRegion;
 	
 
 	private CameraScene mPauseScene;
 	private Scene scene;
 	private Event mainMapEvent;
 
 	private HUD mHUD;
 
 	private Font mFont;
 	private StrokeFont mStrokeFont, mStrokeFontLarge;
 	
 	private Music mMusic;
 
 	static ArrayList<Character> thePlayers;
 	
 	private Text[] textStrokes;
 	final private SpriteCoordinate[] textStrokeCoordinates = {
 	        new SpriteCoordinate(60,280), new SpriteCoordinate(420,280), 
 	        new SpriteCoordinate(60,40), new SpriteCoordinate(420,40) };
 
 	private boolean turnDone;
 	private boolean eventCompleted;
 	private boolean moving;
 	public int turnNum;
 	public int currentCharacter;
 	public int currentCharacterYear;
 	public int ranNumb;
 	private int numCharacters;
 	private int movementCount;
 
 	private Random random;
     private int diceRoll = 0;
     private int maxRoll;
     
 	private boolean gameDone = false;
 	private boolean hasGraduated = false;
 
 	float initX;
 	float initY;
 	float finalX;
 	float finalY;
 	
 	float variable;
 	
 	private boolean finishTurn = false; 
 	private boolean diceDone = false;
 
 	boolean swipeDone = false;
 	private DbHelper dbhelper;
 
 	@Override
 	public EngineOptions onCreateEngineOptions() {
 		// Toast.makeText(this,
 		// "The tile the player is walking on will be highlighted.",
 		// Toast.LENGTH_LONG).show();
 
 		final float maxVelocityX = 150;
 		final float maxVelocityY = 150;
 		final float maxZoomFactorChange = 5;
 		this.mCamera = new SmoothCamera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT,
 				maxVelocityX, maxVelocityY, maxZoomFactorChange);
 		this.mCamera.setBoundsEnabled(false);
 		
         final EngineOptions engineOptions = new EngineOptions(true, ScreenOrientation.LANDSCAPE_SENSOR, new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), this.mCamera);
         engineOptions.getAudioOptions().setNeedsMusic(true);
 
 		return engineOptions;
 	}
 
 	@Override
 	public void onCreateResources() throws IOException {
 		mainMapEvent = new Event(this, R.raw.map_text_file);
 
         Intent intent = getIntent();
         Bundle bundle = intent.getExtras();
 		thePlayers = (ArrayList<Character>) bundle.getSerializable(ChooseCharacterActivity.THE_PLAYERS);
 		numCharacters = thePlayers.size();
 		
 		//Create all four character sprites
 		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
 		
 		characterTextureAtlas = new BitmapTextureAtlas[numCharacters];
 		character = new ITextureRegion[numCharacters];
 		
 		for (int i = 0; i < numCharacters; i++) {
 			characterTextureAtlas[i] = new BitmapTextureAtlas(
 					this.getTextureManager(), 1000, 1000, TextureOptions.BILINEAR);
 			character[i] = BitmapTextureAtlasTextureRegionFactory.createFromAsset(characterTextureAtlas[i],
 					this, thePlayers.get(i).getCharacterImage(), 0 , 0);   //NameToImageName(characterTypes[i]), 0, 0);
 			characterTextureAtlas[i].load();
 		}
 
 		this.diceTextureAtlas = new BitmapTextureAtlas(
                 this.getTextureManager(), 170, 90, TextureOptions.BILINEAR);
 		this.diceTextureRegion = BitmapTextureAtlasTextureRegionFactory
                 .createFromAsset(diceTextureAtlas, this, "dice.png",
                                 0, 0);
 		this.diceTextureAtlas.load();
 		
 		this.finishTurnTextureAtlas = new BitmapTextureAtlas(
                 this.getTextureManager(), 170, 90, TextureOptions.BILINEAR);
 		this.finishTurnTextureRegion = BitmapTextureAtlasTextureRegionFactory
                 .createFromAsset(finishTurnTextureAtlas, this, "finish_button.png",
                                 0, 0);
 		this.finishTurnTextureAtlas.load();
 		
 		// Pause Assets
 		this.mFaceTexture = new AssetBitmapTexture(this.getTextureManager(),
 				this.getAssets(), "gfx/menu.png", TextureOptions.BILINEAR);
 		this.mFaceTextureRegion = TextureRegionFactory
 				.extractFromTexture(this.mFaceTexture);
 		this.mFaceTexture.load();
 
 		this.mPausedTexture = new AssetBitmapTexture(this.getTextureManager(),
 				this.getAssets(), "gfx/paused.png", TextureOptions.BILINEAR);
 		this.mPausedTextureRegion = TextureRegionFactory
 				.extractFromTexture(this.mPausedTexture);
 		this.mPausedTexture.load();
 
 		this.mResumeTexture = new AssetBitmapTexture(this.getTextureManager(),
 				this.getAssets(), "gfx/resume.png", TextureOptions.BILINEAR);
 		this.mResumeTextureRegion = TextureRegionFactory
 				.extractFromTexture(this.mResumeTexture);
 		this.mResumeTexture.load();
 
 		this.mMainMenuTexture = new AssetBitmapTexture(
 				this.getTextureManager(), this.getAssets(), "gfx/mainmenu.png",
 				TextureOptions.BILINEAR);
 		this.mMainMenuTextureRegion = TextureRegionFactory
 				.extractFromTexture(this.mMainMenuTexture);
 		this.mMainMenuTexture.load();
 
 		this.mSaveTexture = new AssetBitmapTexture(this.getTextureManager(),
 				this.getAssets(), "gfx/savegame.png", TextureOptions.BILINEAR);
 		this.mSaveTextureRegion = TextureRegionFactory
 				.extractFromTexture(this.mSaveTexture);
 		this.mSaveTexture.load();
 
 		// UI Fonts
 		final ITexture fontTexture = new EmptyTexture(this.getTextureManager(),
 				256, 256, TextureOptions.BILINEAR);
 		final ITexture strokeFontTexture = new EmptyTexture(
 				this.getTextureManager(), 256, 256, TextureOptions.BILINEAR);
 
 		this.mFont = new Font(this.getFontManager(), fontTexture,
 				Typeface.create(Typeface.DEFAULT, Typeface.BOLD), 18, true,
 				Color.BLACK);
 		this.mFont.load();
 
 		this.mStrokeFont = new StrokeFont(this.getFontManager(),
 				strokeFontTexture, Typeface.create(Typeface.DEFAULT,
 						Typeface.BOLD), 18, true, Color.WHITE, 1, Color.BLACK);
 		this.mStrokeFont.load();
 
 		this.mStrokeFontLarge = new StrokeFont(this.getFontManager(),
 				strokeFontTexture, Typeface.create(Typeface.DEFAULT,
 						Typeface.BOLD), 32, true, Color.WHITE, 1, Color.BLACK);
 		this.mStrokeFontLarge.load();
 		
 		this.musicTextureAtlas = new BitmapTextureAtlas(this.getTextureManager(), 128, 128, TextureOptions.BILINEAR);
         this.mMusicTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.musicTextureAtlas, this, "notes.png", 0, 0);
         this.musicTextureAtlas.load();
         
 		MusicFactory.setAssetBasePath("mfx/");
         try {
                 this.mMusic = MusicFactory.createMusicFromAsset(this.mEngine.getMusicManager(), this, "wagner_the_ride_of_the_valkyries.ogg");
                 this.mMusic.setLooping(true);
         } catch (final IOException e) {
                 Debug.e(e);
         }
 
 	}
 
 	@Override
 	public Scene onCreateScene() {
 		
 		this.mEngine.registerUpdateHandler(new FPSLogger());
 
 		final Scene scene = new Scene();
 
 		/*
 		 * User Interface HUD At each corner of the screen, will display
 		 * player's information Include number of credits There will be a button
 		 * to roll dice and turn number will be displayed.
 		 */
 		VertexBufferObjectManager vertexBufferObjectManager = this
 				.getVertexBufferObjectManager();
 
 		textStrokes = new Text[numCharacters];
 
 
 		/*
 		 * To update text, use [text].setText("blah blah"); In which "blah blah"
 		 * is whatever you want to change the text to. You can use variables.
 		 */
 		for (int i = 0; i < numCharacters; i++) {
 			SpriteCoordinate coord = textStrokeCoordinates[i];
 			textStrokes[i] = new Text(coord.getX(), coord.getY(), this.mStrokeFont,
 					thePlayers.get(i).getName()   +"\nCredits: " + thePlayers.get(i).getCredits()
 				    + "\nCoins: " + thePlayers.get(i).getCoins(), vertexBufferObjectManager); 
 		}
 		final Text textStroke5 = new Text(180, 20, this.mStrokeFont,
                 " " + diceRoll, vertexBufferObjectManager);
 
 		textStroke5.setScale((float) .7 ); 
 		mHUD = new HUD();
 		mHUD.attachChild(scene);
 
 		/*
 		 * Where the button should go A fancier button should go here, but to
 		 * test the randomizer, I believe this should suffice.
 		 */
 
 		final Sprite diceButton = new Sprite(180, CAMERA_HEIGHT/10, this.diceTextureRegion,
                 this.getVertexBufferObjectManager()) {
 
 	        public boolean onAreaTouched(TouchEvent touchEvent, float X, float Y) {
 	                /*
 	                 * Here, you can update the randomizer when the user presses the
 	                 * button. Disregard the effect, just lets me know that the
 	                 * button is being pressed.
 	                 */
 	                //generate random number [1,3]
	        	currentCharacterYear = (thePlayers.get(currentCharacter).getCredits()%CREDITS_NEEDED_GRADUATE) + 1;
 	        	switch(currentCharacterYear) {
 	        	case 1: maxRoll = 3;
 	        			break;
 	        	case 2: maxRoll = 4;
 	        			break;
 	        	case 3: maxRoll = 5;
 	        			break;
 	        	case 4: maxRoll = 6;
 	        			break;
 	        	}
 	                random = new Random();
 	                diceRoll = random.nextInt(maxRoll) + 1;
 	                if (diceDone == false) {
 	                	swipeDone = false;
 	                	if (touchEvent.isActionUp()) {
 	                		this.setColor(Color.GRAY);
 	                		textStroke5.setText(" " + diceRoll);
 	                		diceDone = true;
 	                		
 	                	}
 	                	if (touchEvent.isActionDown()) {
 	                		this.setColor(Color.WHITE);
 	                	}
 	                }
 	                return true;
 	        };
 		};
 		diceButton.setScale((float) .5);	
 		
 		final Sprite finishTurnButton = new Sprite(310, CAMERA_HEIGHT/10, this.finishTurnTextureRegion,
                 this.getVertexBufferObjectManager()) {
 
 	        public boolean onAreaTouched(TouchEvent touchEvent, float X, float Y) {
 	                
 	                if (touchEvent.isActionUp()) {
 	                        this.setColor(Color.GRAY);
 	                        finishTurn = true;
 	                        }
 	                if (touchEvent.isActionDown()) {
 	                        this.setColor(Color.GRAY);
 	                }
 	                return true;
 	        };
 		};
 		finishTurnButton.setScale((float) .5);
 		finishTurnButton.setColor(Color.GRAY);
 
 		// Load the Pause Scene
 		this.mPauseScene = new CameraScene(this.mCamera);
 		final float cX = (CAMERA_WIDTH - this.mPausedTextureRegion.getWidth())
 				/ 2 + (this.mPausedTextureRegion.getWidth() / 3);
 		final float cY = (CAMERA_HEIGHT - this.mPausedTextureRegion.getHeight()) / 5;
 
 		// Music Button
 		// Default - Music on.
 		mMusic.play();
 		
         final Sprite musicButton = new Sprite(CAMERA_WIDTH / 12, cY - (CAMERA_HEIGHT / 13), this.mMusicTextureRegion,
 				this.getVertexBufferObjectManager()) {
 			public boolean onAreaTouched(TouchEvent touchEvent, float X, float Y) {
 				switch (touchEvent.getAction()) {
 				case TouchEvent.ACTION_DOWN:
 					if (mMusic.isPlaying()) {
 						mMusic.pause();
 					} else {
 						mMusic.play();
 					}
 					break;
 				case TouchEvent.ACTION_MOVE:
 					break;
 				case TouchEvent.ACTION_UP:
 					break;
 				}
 				return true;
 			};
 		};
         
         this.mPauseScene.registerTouchArea(musicButton);
 		this.mPauseScene.attachChild(musicButton);
         
 		// Resume Button
 
 		final Sprite resumeButton = new Sprite(cX + (CAMERA_WIDTH / 10), cY
 				+ (CAMERA_HEIGHT / 2), this.mResumeTextureRegion,
 				this.getVertexBufferObjectManager()) {
 			public boolean onAreaTouched(TouchEvent touchEvent, float X, float Y) {
 				switch (touchEvent.getAction()) {
 				case TouchEvent.ACTION_DOWN:
 					mHUD.clearChildScene();
 					scene.setIgnoreUpdate(false);
 					break;
 				case TouchEvent.ACTION_MOVE:
 					break;
 				case TouchEvent.ACTION_UP:
 					break;
 				}
 				return true;
 			};
 		};
 		this.mPauseScene.registerTouchArea(resumeButton);
 		this.mPauseScene.attachChild(resumeButton);
 
 		// Return to main menu button
 
 		final Sprite mainMenuButton = new Sprite(cX + (CAMERA_WIDTH / 10), cY
 				+ (CAMERA_HEIGHT / 3), this.mMainMenuTextureRegion,
 				this.getVertexBufferObjectManager()) {
 			public boolean onAreaTouched(TouchEvent touchEvent, float X, float Y) {
 				switch (touchEvent.getAction()) {
 				case TouchEvent.ACTION_DOWN:
 					scene.detachChildren();
 					scene.clearEntityModifiers();
 					scene.clearTouchAreas();
 					scene.clearUpdateHandlers();
 					mHUD.detachChildren();
 					mPauseScene.detachChildren();
 					this.detachSelf();
 					onClick(mRenderSurfaceView);
 					finish();
 					break;
 				default:
 					break;
 				}
 				return true;
 			};
 		};
 		
 		this.mPauseScene.registerTouchArea(mainMenuButton);
 		this.mPauseScene.attachChild(mainMenuButton);
 		//save game button
 		
 		final Sprite saveButton = new Sprite(cX + (CAMERA_WIDTH / 10), cY
 				+ (CAMERA_HEIGHT / 6), this.mSaveTextureRegion,
 				this.getVertexBufferObjectManager()) {
 			public boolean onAreaTouched(TouchEvent touchEvent, float X, float Y) {
 				switch (touchEvent.getAction()) {
 				case TouchEvent.ACTION_DOWN:
 					//update database here
 					saveGame();
 					mHUD.clearChildScene();
 					scene.setIgnoreUpdate(false);
 					//save to database
 					break;
 				case TouchEvent.ACTION_MOVE:
 					break;
 				case TouchEvent.ACTION_UP:
 					break;
 				}
 				return true;
 			};
 		};
 		this.mPauseScene.registerTouchArea(saveButton);
 		this.mPauseScene.attachChild(saveButton);
 		
 //		final Sprite saveGameButton = new Sprite(cX+(CAMERA_WIDTH/10), cY + (CAMERA_HEIGHT/4),
 //				this.mSaveGameTextureRegion, this.getVertexBufferObjectManager()) {
 //			public boolean onAreaTouched(TouchEvent touchEvent, float X, float Y) {
 //				switch (touchEvent.getAction()) {
 //				case TouchEvent.ACTION_DOWN:
 //					String[] key1 = {"128"};
 //					String[] key2 = {"128"};
 //					String[] key3 = {"128","1"};
 //					String[] key4 = {"128"};
 //					String[] key5 = {"128","1"};
 //					String[] key6 = {"128"};
 //					String[] key7 = {"128"};
 //					String[] key8 = {"128"};
 //					String[] key9 = {"128","1","2"};
 //			        
 //					String[] newTable1Values = {"128","1","2"};
 //			        String[] newTable2Values = {"128","1","2","4"};
 //			        String[] newTable3Values = {"128","1","2","3","4","5"};
 //			        String[] newTable4Values = {"128","1","4","3","4"};
 //			        String[] newTable5Values = {"128","1","9"};
 //			        String[] newTable6Values = {"128","1","5","3","4"};
 //			        String[] newTable7Values = {"128","1","5","1","4","5","6"};
 //			        String[] newTable8Values = {"128","0"};
 //			        String[] newTable9Values = {"128","1","2","3"};
 //					//db.updateRow(tableNum, table1Values, newTable1Values);
 //			        dbhelper.updateRow(1,key1,newTable1Values);
 //			        dbhelper.updateRow(2,key2,newTable2Values);
 //			        dbhelper.updateRow(3,key3,newTable3Values);
 //			        dbhelper.updateRow(4,key4,newTable4Values);
 //			        dbhelper.updateRow(5,key5,newTable5Values);
 //			        dbhelper.updateRow(6,key6,newTable6Values);
 //			        dbhelper.updateRow(7,key7,newTable7Values);
 //			        dbhelper.updateRow(8,key8,newTable8Values);
 //			        dbhelper.updateRow(9,key9,newTable9Values);
 //					break;
 //				case TouchEvent.ACTION_MOVE:
 //					break;
 //				case TouchEvent.ACTION_UP:
 //					break;
 //				}
 //				return true;
 //			};
 //		};
 //		this.mPauseScene.registerTouchArea(saveGameButton);
 //		this.mPauseScene.attachChild(saveGameButton);
 
 		/* Makes the paused Game look through. */
 		this.mPauseScene.setBackgroundEnabled(false);
 
 		// Main Menu Button on HUD
 		final Sprite pauseSprite = new Sprite(mCamera.getWidth()
 				- (mCamera.getWidth() / 2), 300, this.mFaceTextureRegion,
 				this.getVertexBufferObjectManager()) {
 			public boolean onAreaTouched(TouchEvent touchEvent, float X, float Y) {
 				switch (touchEvent.getAction()) {
 				case TouchEvent.ACTION_DOWN:
 					scene.setIgnoreUpdate(true);
 					mHUD.setChildScene(mPauseScene, false, true, true);
 					break;
 				default:
 					break;
 				}
 				return true;
 			};
 		};
 
 		for (int i = 0; i < numCharacters; i++) {
 			mHUD.attachChild(textStrokes[i]);
 		}
 
 		mHUD.registerTouchArea(diceButton);
 		mHUD.attachChild(diceButton);	
 		mHUD.registerTouchArea(finishTurnButton);
 		mHUD.attachChild(finishTurnButton);
 		mHUD.attachChild(textStroke5);
 
 
 		mHUD.registerTouchArea(pauseSprite);
 		mHUD.attachChild(pauseSprite);
 
 		mCamera.setHUD(mHUD);
 
 		/* Main Map Scene */
 
 		scene.setOnSceneTouchListener(this);
 
 		try {
 			final TMXLoader tmxLoader = new TMXLoader(this.getAssets(),
 					this.mEngine.getTextureManager(),
 					TextureOptions.BILINEAR_PREMULTIPLYALPHA,
 					this.getVertexBufferObjectManager(),
 					new ITMXTilePropertiesListener() {
 						@Override
 						public void onTMXTileWithPropertiesCreated(
 								final TMXTiledMap pTMXTiledMap,
 								final TMXLayer pTMXLayer,
 								final TMXTile pTMXTile,
 								final TMXProperties<TMXTileProperty> pTMXTileProperties) {
 							/*
 							 * We are going to count the tiles that have the
 							 * property "cactus=true" set.
 							 */
 							// if(pTMXTileProperties.containsTMXProperty("cactus",
 							// "true")) {
 							// MainGameScreen.this.mCactusCount++;
 							// }
 						}
 					});
 			this.mTMXTiledMap = tmxLoader
 					.loadFromAsset("tmx/MultiGator14Layers.tmx");
 			this.mTMXTiledMap.setOffsetCenter(0, 0);
 
 			// this.toastOnUiThread("Cactus count in this TMXTiledMap: " +
 			// MainGameScreen.this.mCactusCount, Toast.LENGTH_LONG);
 		} catch (final TMXLoadException e) {
 			Debug.e(e);
 		}
 		moving = false;
 		turnDone = false;
 		turnNum = 1;
 		//ranNumb = 1 + (int) (Math.random() * ((6 - 1) + 1));
 
 		scene.attachChild(this.mTMXTiledMap);
 
 		/* Make the camera not exceed the bounds of the TMXEntity. */
 		this.mCamera.setBoundsEnabled(false);
 		this.mCamera.setBounds(0, 0, this.mTMXTiledMap.getWidth(),
 				this.mTMXTiledMap.getHeight());
 		this.mCamera.setBoundsEnabled(true);
 
 		
 		final Sprite[] spriteList = new Sprite[numCharacters];
 		
 		for (int i = 0; i < numCharacters; i++) {
 			SpriteCoordinate loc = thePlayers.get(i).getSpriteLocation();
 			spriteList[i] = new Sprite(loc.getX(), loc.getY(), character[i], 
 					this.getVertexBufferObjectManager());
 		}
 
 		/* Create the sprite and add it to the scene. */
 		// final AnimatedSprite player = new AnimatedSprite(centerX, centerY,
 		// this.character, this.getVertexBufferObjectManager());
 
 		//Open Database
         dbhelper = new DbHelper(this);
         SQLiteDatabase db = dbhelper.openDB();
         Log.d("TEST", "Database has been opened");
         
         
         String[] gameKey = { Integer.toString(thePlayers.get(0).gameId) };
       //Grab Game info
       		ArrayList gameList = dbhelper.getRow(1, gameKey);
       		
       		if (gameList.size()>0){
       			currentCharacter = Integer.valueOf((String)gameList.get(3));
       		}
       		
       	dbhelper.close();
         
 		this.mCamera.setChaseEntity(spriteList[currentCharacter]);
 
 		// final Path path = new Path(5).to(50, 740).to(50, 1000).to(820,
 		// 1000).to(820, 740).to(0);
 
 		// player.registerEntityModifier(new LoopEntityModifier(new
 		// PathModifier(30, path, null, new IPathModifierListener() {
 		// @Override
 		// public void onPathStarted(final PathModifier pPathModifier, final
 		// IEntity pEntity) {
 		//
 		// }
 		//
 		// @Override
 		// public void onPathWaypointStarted(final PathModifier pPathModifier,
 		// final IEntity pEntity, final int pWaypointIndex) {
 		// switch(pWaypointIndex) {
 		// case 0:
 		// player.animate(new long[] { 200, 200, 200 }, 0, 2, true);
 		// break;
 		// case 1:
 		// player.animate(new long[] { 200, 200, 200 }, 3, 5, true);
 		// break;
 		// case 2:
 		// player.animate(new long[] { 200, 200, 200 }, 6, 8, true);
 		// break;
 		// case 3:
 		// player.animate(new long[] { 200, 200, 200 }, 9, 11, true);
 		// break;
 		// }
 		// }
 		//
 		// @Override
 		// public void onPathWaypointFinished(final PathModifier pPathModifier,
 		// final IEntity pEntity, final int pWaypointIndex) {
 		//
 		// }
 		//
 		// @Override
 		// public void onPathFinished(final PathModifier pPathModifier, final
 		// IEntity pEntity) {
 		//
 		// }
 		// })));
 
 		/*
 		 * Now we are going to create a rectangle that will always highlight the
 		 * tile below the feet of the pEntity.
 		 */
 //		final Rectangle currentTileRectangle = new Rectangle(0, 0,
 //				this.mTMXTiledMap.getTileWidth(),
 //				this.mTMXTiledMap.getTileHeight(),
 //				this.getVertexBufferObjectManager());
 //		/* Set the OffsetCenter to 0/0, so that it aligns with the TMXTiles. */
 //		currentTileRectangle.setOffsetCenter(0, 0);
 		//currentTileRectangle.setColor(1, 0, 0, 0.25f);
 		//scene.attachChild(currentTileRectangle);
 
 		/* The layer for the player to walk on. */
 		final TMXLayer tmxLayer = this.mTMXTiledMap.getTMXLayers().get(0);
 
 		scene.registerUpdateHandler(new IUpdateHandler() {
 			@Override
 			public void reset() {
 			}
 
 			@Override
 			public void onUpdate(final float pSecondsElapsed) {
 				// /* Get the scene-coordinates of the players feet. */
 				float[] localCoord = new float[2];
 				localCoord[0] = spriteList[currentCharacter].getWidth() * .5f;
 				localCoord[1] = spriteList[currentCharacter].getHeight() * .5f;
 				final float[] playerFootCordinates = spriteList[currentCharacter]
 						.convertLocalCoordinatesToSceneCoordinates(localCoord);
 				//
 				/* Get the tile the feet of the player are currently waking on. */
 				final TMXTile tmxTile = tmxLayer.getTMXTileAt(
 						playerFootCordinates[Constants.VERTEX_INDEX_X],
 						playerFootCordinates[Constants.VERTEX_INDEX_Y]);
 //				if (tmxTile != null) {
 //					// tmxTile.setTextureRegion(null); <-- Eraser-style removing
 //					// of tiles =D
 //					currentTileRectangle.setPosition(
 //							tmxLayer.getTileX(tmxTile.getTileColumn()),
 //							tmxLayer.getTileY(tmxTile.getTileRow()));
 //				}
 
 				if (move && !turnDone && diceDone) {
 					movementFunction(spriteList[currentCharacter]);
 					MainGameScreen.this.mCamera.updateChaseEntity();
 					finishTurnButton.setColor(Color.WHITE);
 				}
 				
 				
 
 				if (turnDone && finishTurn) { //&& swipeDone == false
 					moving = false;
 					move = false;
 					turnDone = false;
 					diceDone = false;
 					currentCharacter = (currentCharacter + 1) % (numCharacters);
 					saveGame();
 					// consider a delay here so the camera doesn't switch back and forth so fast
 					MainGameScreen.this.mCamera
 							.setChaseEntity(spriteList[currentCharacter]);
 					finishTurn = false;
 					diceButton.setColor(Color.WHITE);
 					finishTurnButton.setColor(Color.GRAY);					
 					checkCredits(currentCharacter);					
 
 				}
 				
 				
 
 				if (swipeDone == false) {
 					//ranNumb = (1 + (int) (Math.random() * ((MAX_CHARACTER_MOVEMENT - 1) + 1))) * CHARACTER_WIDTH;
 					ranNumb = diceRoll;// * CHARACTER_WIDTH;
 				}
 
 			}
 		});
 		
 		for (int i = (numCharacters-1); i>=0 ; i--) {
 			spriteList[i].setScale(.1f);
 			scene.attachChild(spriteList[i]);
 		}
 
 		return scene;
 	}
 
 	boolean move = false;
 
 	protected void movementFunction(Sprite mySprite) {
 		if (!moving && swipeDone) {
 			//int thisCurrent = currentCharacter;
 			SpriteCoordinate offset = new SpriteCoordinate();
 
 			if (finalY - initY > 40) {
 				offset.setY(CHARACTER_WIDTH);
 			} else if (finalY - initY < -40) {
 				offset.setY(-CHARACTER_WIDTH);
 			} else if (finalX - initX > 40) {
 				offset.setX(CHARACTER_WIDTH);
 			} else if (finalX - initX < -40) {
 				offset.setX(-CHARACTER_WIDTH);
 			} else {
 				return;
 			}
 
 			moving = true;
 
 			SpriteCoordinate characterLocation = thePlayers.get(currentCharacter).getSpriteLocation();
 			SpriteCoordinate newPosition = offset.add(characterLocation);
 			
 			newPosition = this.mainMapEvent.checkBoundaries(characterLocation, newPosition);
 			
 			moveSprite(ranNumb-1, newPosition, offset, mySprite);		
 		}
 	}
 	
 	public void moveSprite(final int moves, final SpriteCoordinate newPosition,
 			final SpriteCoordinate offset, final Sprite mySprite) {
 		
 		SpriteCoordinate characterLocation = thePlayers.get(currentCharacter).getSpriteLocation();
 		
 			mySprite.registerEntityModifier(new MoveModifier(0.5f,
 					characterLocation.getX(), characterLocation.getY(),
 					newPosition.getX(), newPosition.getY()) {
 				
 				@Override
 				protected void onModifierStarted(IEntity pItem) {
 					super.onModifierStarted(pItem);
 					move = false;
 					gameDone = false;
 					eventCompleted = false;
 				}
 				
 				@Override
 				protected void onModifierFinished(IEntity pItem) {
 					thePlayers.get(currentCharacter).setLocation(mySprite.getX(), mySprite.getY());
 					super.onModifierFinished(pItem);
 					checkMiniGameHotSpots(currentCharacter);
 					if (moves == 0) {
 						swipeDone = false;
 						turnDone = true;
 						moving = false;
 					} else if (thePlayers.get(currentCharacter).getSpriteLocation().compareTo(newPosition) == 0) {
 						SpriteCoordinate newPos = newPosition.add(offset);
 						newPos = mainMapEvent.checkBoundaries(thePlayers.get(currentCharacter).getSpriteLocation(), newPos);
 						int numMoves = moves - 1;
 						// if not a valid move, newPos = newPosition, and we need to show options
 						if (newPos.compareTo(newPosition) == 0) {
 							getNewMove(mainMapEvent.getPossiblePath(newPos), numMoves, mySprite);
 							return;
 						}
 						
 						moveSprite(numMoves, newPos, offset, mySprite);
 					}
 				}
 			});
 	}
 
 	private void getNewMove(SpriteCoordinate[] pathOptions, final int moves, 
 			final Sprite mySprite) {
 		StringBuilder options = new StringBuilder();
 		StringBuilder choices = new StringBuilder();
 		
 		for (Event.DIRECTION dir : Event.DIRECTION.values()) {
 			if (pathOptions[dir.getIndex()] != null) {
 				options.append(dir.getName());
 				options.append(",");
 				choices.append(dir.name());
 				choices.append(",");
 			}
 		}
 		
 		CharSequence[] dialogOptions = options.toString().split(",");
 		final String[] dialogChoices = choices.toString().split(",");
 
 		
 		alertDialogBuilder = new AlertDialog.Builder(this);
 
 		// set title and message
 		alertDialogBuilder.setTitle("Choose a direction:");
 		//alertDialogBuilder.setMessage("Please select the direction you want to go.");
 		alertDialogBuilder.setCancelable(false);
 
 		// create continue button
 		alertDialogBuilder.setItems(dialogOptions, 
 				new DialogInterface.OnClickListener() {
 					
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						dialog.dismiss();
 						SpriteCoordinate offset = Event.getPositionFromDirection(dialogChoices[which]);
 						SpriteCoordinate newPos = offset.add(thePlayers.get(currentCharacter).getSpriteLocation());
 						moveSprite(moves, newPos, offset, mySprite);
 					}
 				});
 
 		mHandler.post(mUpdateResults);
 		
 	}
 
 	public void askDirection() {
 		// create alert dialog
 		alertDialog = alertDialogBuilder.create();
 
 		// show it
 		alertDialog.show();
 		
 	}
 	// Checks the hot spots for the minigames
 	protected void checkMiniGameHotSpots(int current) {
 		Event.getEvent(thePlayers.get(current).getSpriteLocation(), this, thePlayers.get(current).getName(), hasGraduated);
 		
 		if (!(move || gameDone)) {
 			gameDone = true;
 		}
 		
 		swipeDone = false;
 		turnDone = true;
 	}
 
 	@Override
 	public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent) {
 		// if (pSceneTouchEvent.getAction() == MotionEvent.ACTION_UP)
 		// {
 		// move = true;
 		//
 		// }
 		if (pSceneTouchEvent.getAction() == MotionEvent.ACTION_DOWN) {
 			initX = pSceneTouchEvent.getX();
 			initY = pSceneTouchEvent.getY();
 			move = false;
 		}
 		if (pSceneTouchEvent.getAction() == MotionEvent.ACTION_MOVE) {
 			move = true;
 			finalX = pSceneTouchEvent.getX();
 			finalY = pSceneTouchEvent.getY();
 			swipeDone = true;
 
 		}
 		return false;
 	}
 
 	public void onClick(View view) {
 		Intent intent = new Intent(this, MainActivity.class);
 		startActivity(intent);
 	}
 
 	@Override
 	public synchronized void onResumeGame() {
 		if (this.mEngine != null)
 			super.onResumeGame();
 	}
 
 	/*void gameOver(){
         runOnUiThread(new Runnable() {                  
             @Override
             public void run() {
             	Toast.makeText(getApplicationContext(), "You have won! Please head to the O'Connoll Center for gradugation.",
             			   Toast.LENGTH_LONG).show();
                 }                  
             });
                 }*/
 	// ===========================================================
 	// Methods
 	// ===========================================================
 	public void onActivityResult (int requestCode, int resultCode, Intent data) {
 		if (!(move || gameDone)) {
 			gameDone = true;
 		}
 		swipeDone = false;
 		turnDone = true;
 
 		if (resultCode != RESULT_OK || data == null) {
 			return;
 		}
 
 		int result = data.getIntExtra(requestCode+"", 0);
 		addCredits(currentCharacter, result);
 		Log.d("MINIGAME", result+", "+resultCode);
 		
 		checkCredits(currentCharacter);
 	}
 	
 	private void addCredits(int character, int creditsToAdd) {
 		thePlayers.get(character).addCredits(creditsToAdd);
 		textStrokes[character].setText(thePlayers.get(character).getName()
 				+ "\nCredits: " + thePlayers.get(character).getCredits()
 				+ "\nCoins: " + thePlayers.get(character).getCoins());
 	}
 	
 	// Get the character names and credits for game over screen
 	public static ArrayList<Character> getPlayers() {
 		return thePlayers;
 	}
 	
 	private void checkCredits(final int character) {
 		if (thePlayers.get(character).getCredits() >= CREDITS_NEEDED_GRADUATE) {
 			runOnUiThread(new Runnable() {                  
 	            @Override
 	            public void run() {
 	            	hasGraduated = true;
 	            	Toast.makeText(getApplicationContext(), getString(R.string.ready_to_graduate, thePlayers.get(character).getName(), thePlayers.get(character).getCredits()),
 	            			   Toast.LENGTH_SHORT).show();
 	            	GameOverScreen.setPlayers(thePlayers);
 	            	mMusic.pause();
 	                }                  
 	           
 	            	            	
 	            });
 			
 			
 		}
 	}
 	
 	private void addCoins(int character, int coinsToAdd) {
 		thePlayers.get(character).addCoins(coinsToAdd);
 		textStrokes[character].setText(thePlayers.get(character).getName()
 				+ "\nCredits: " + thePlayers.get(character).getCredits()
 				+ "\nCoins: " + thePlayers.get(character).getCoins());
 	}
 	
 	public void saveGame(){
 		int numPlayers = thePlayers.size();
 		
 		DbHelper dbhelper = new DbHelper(this);
 		SQLiteDatabase db = dbhelper.openDB();
     
 		int gameId = thePlayers.get(0).getGameId();  
         
 		String[] table1Values = {Integer.toString(gameId), "0", Integer.toString(numCharacters), Integer.toString(currentCharacter)};
 		Log.d("debug,",Integer.toString(gameId) + "0" + Integer.toString(numCharacters) + Integer.toString(currentCharacter));
 		
         String[] table1Key = {Integer.toString(gameId)};
         dbhelper.updateRow(1, table1Key, table1Values); //Update game table
         
         //insert numPlayers rows into table 3
         for (int i = 0; i < numPlayers; i++)
         {
         	String[] characterKey = {Integer.toString((gameId<<2) + i)};
         	String[] newCharacter = {characterKey[0], thePlayers.get(i).getType(), thePlayers.get(i).getName(), Float.toString(thePlayers.get(i).getMapLocation().getX()), Float.toString(thePlayers.get(i).getMapLocation().getY()), Integer.toString(thePlayers.get(i).getCredits()), Integer.toString(thePlayers.get(i).getCoins()), Integer.toString(i)};
         	Log.d("debug,", characterKey[0]+ thePlayers.get(i).getType()+thePlayers.get(i).getName()+Float.toString(thePlayers.get(i).getMapLocation().getX())+Float.toString(thePlayers.get(i).getMapLocation().getY())+Integer.toString(thePlayers.get(i).getCredits())+Integer.toString(thePlayers.get(i).getCoins())+Integer.toString(i+1));
         	dbhelper.updateRow(2, characterKey, newCharacter);
         }
         
         dbhelper.close();
 		
 	}
 	
 	
 }
 
 
