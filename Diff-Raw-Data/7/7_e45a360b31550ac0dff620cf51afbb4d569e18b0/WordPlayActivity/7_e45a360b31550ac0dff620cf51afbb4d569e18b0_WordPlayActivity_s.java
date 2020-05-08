 package edu.stanford.mobisocial.games.wordplay;
 
 import java.util.Calendar;
 import java.util.HashMap;
 
 import mobisocial.socialkit.User;
 import mobisocial.socialkit.musubi.DbObj;
 import mobisocial.socialkit.musubi.Musubi;
 import mobisocial.socialkit.musubi.multiplayer.FeedRenderable;
 import mobisocial.socialkit.musubi.multiplayer.TurnBasedApp;
 
 import org.anddev.andengine.engine.Engine;
 import org.anddev.andengine.engine.camera.ZoomCamera;
 import org.anddev.andengine.engine.camera.hud.HUD;
 import org.anddev.andengine.engine.options.EngineOptions;
 import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
 import org.anddev.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
 import org.anddev.andengine.entity.scene.Scene;
 import org.anddev.andengine.entity.scene.Scene.IOnSceneTouchListener;
 import org.anddev.andengine.entity.scene.background.ColorBackground;
 import org.anddev.andengine.entity.sprite.Sprite;
 import org.anddev.andengine.entity.sprite.TiledSprite;
 import org.anddev.andengine.entity.text.ChangeableText;
 import org.anddev.andengine.input.touch.TouchEvent;
 import org.anddev.andengine.input.touch.detector.ScrollDetector;
 import org.anddev.andengine.input.touch.detector.ScrollDetector.IScrollDetectorListener;
 import org.anddev.andengine.input.touch.detector.SurfaceScrollDetector;
 import org.anddev.andengine.opengl.font.Font;
 import org.anddev.andengine.opengl.texture.TextureOptions;
 import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
 import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
 import org.anddev.andengine.opengl.texture.region.TextureRegion;
 import org.anddev.andengine.opengl.texture.region.TiledTextureRegion;
 import org.anddev.andengine.ui.activity.BaseGameActivity;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.res.Configuration;
 import android.graphics.Color;
 import android.graphics.Point;
 import android.graphics.Typeface;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.Window;
 import android.widget.Toast;
 import edu.stanford.mobisocial.games.wordplay.buttons.Button;
 import edu.stanford.mobisocial.games.wordplay.constants.BoardLayout;
 import edu.stanford.mobisocial.games.wordplay.constants.LetterValues;
 import edu.stanford.mobisocial.games.wordplay.players.Player;
 import edu.stanford.mobisocial.games.wordplay.tiles.BasicTileSpace;
 import edu.stanford.mobisocial.games.wordplay.tiles.DoubleLetterTileSpace;
 import edu.stanford.mobisocial.games.wordplay.tiles.DoubleWordTileSpace;
 import edu.stanford.mobisocial.games.wordplay.tiles.StartTileSpace;
 import edu.stanford.mobisocial.games.wordplay.tiles.Tile;
 import edu.stanford.mobisocial.games.wordplay.tiles.TileSpace;
 import edu.stanford.mobisocial.games.wordplay.tiles.TripleLetterTileSpace;
 import edu.stanford.mobisocial.games.wordplay.tiles.TripleWordTileSpace;
 import edu.stanford.mobisocial.games.wordplay.verifiers.Dictionary;
 import edu.stanford.mobisocial.games.wordplay.verifiers.TWLDictionary;
 
 public class WordPlayActivity extends BaseGameActivity  implements IScrollDetectorListener, IOnSceneTouchListener {
 	
 	// ===========================================================
 	// Constants
 	// ===========================================================
 	static final int CAMERA_WIDTH = 320;
 	static final int CAMERA_HEIGHT = 500;
 	
 	public final static int OFFSET_X = 2;
 	public final static int OFFSET_Y = 40;
 	 
 	private static final String TAG = "WordPlayActivity";
 	private static final boolean DBG = false;
 	// ===========================================================
 	// Fields
 	// ===========================================================
 	public ZoomCamera mCamera;
 	
 	public HUD hud, tilePickerHud;
 	
 	private BitmapTextureAtlas mTexture;
 	private BitmapTextureAtlas  startTile, baseTile, doubleLetterTile, tripleLetterTile, doubleWordTile, tripleWordTile;
 	public TextureRegion startTileRegion, baseTileRegion, doubleLetterTileRegion, tripleLetterTileRegion, doubleWordTileRegion, tripleWordTileRegion;
 	
 	private BitmapTextureAtlas aTile, bTile, cTile, dTile, eTile, fTile, gTile, hTile, iTile, jTile, kTile, lTile, mTile, nTile, oTile, pTile, qTile, rTile, sTile, tTile, uTile, vTile, wTile, xTile, yTile, zTile, blankTile;
 	public HashMap<Character, TextureRegion> letterTileRegions;
 	
 	private BitmapTextureAtlas point0Tile, point1Tile, point2Tile, point3Tile, point4Tile, point5Tile, point6Tile, point7Tile, point8Tile, point9Tile, point10Tile;
 	public TextureRegion pointTileRegions[];
 	
 	private BitmapTextureAtlas tileOverlayTexture, confirmButtonTexture, cancelButtonTexture, playButtonTexture, shuffleButtonTexture, clearButtonTexture, swapButtonTexture, passButtonTexture, homeButtonTexture;
 	private TiledTextureRegion tileOverlayRegion, confirmButtonRegion, cancelButtonRegion, playButtonRegion, shuffleButtonRegion, clearButtonRegion, swapButtonRegion, passButtonRegion, homeButtonRegion;
 	
 	
 	private BitmapTextureAtlas horizontalCrosshairTexture, verticalCrosshairTexture;
 	private TextureRegion horizontalCrosshairRegion, verticalCrosshairRegion;
 	
 	public BitmapTextureAtlas activeOverlayTexture;
 	public TextureRegion activeOverlayRegion;
 	
 	public BitmapTextureAtlas tileCounterTexture;
 	public TextureRegion tileCounterRegion;
 
 	public BitmapTextureAtlas uiSkinTexture, uiSkinDarkenTexture;
 	public TextureRegion uiSkinRegion, uiSkinDarkenRegion;
 	
 	public BitmapTextureAtlas boardBackgroundTexture;
 	public TextureRegion boardBackgroundRegion;
 	
 	public BitmapTextureAtlas player1ScorePlateTexture, player2ScorePlateTexture, player3ScorePlateTexture, player4ScorePlateTexture;
 	public TiledTextureRegion player1ScorePlateRegion, player2ScorePlateRegion, player3ScorePlateRegion, player4ScorePlateRegion;
 	
 	public TiledSprite player1ScorePlate, player2ScorePlate, player3ScorePlate, player4ScorePlate;
 	
     public TileSpace[][] tileSpaces;
     public TileRack tileRack;
 	private SurfaceScrollDetector mScrollDetector;
     
 	public Point startCoordinate;
 	
 	public TileBag bag;
 	
 	public Dictionary dictionary;
 	
 
     private BitmapTextureAtlas mFontTexture, nameFontTexture, scoreFontTexture, tileCountFontTexture;
 	private Font mFont, nameFont, scoreFont, tileCountFont;
 
 	Scene scene;
 	
 	public boolean showingPicker;
 	public boolean gameOver;
 
 	public long lastTap;
 	
 	Button playButton, clearButton, shuffleButton, swapButton, passButton, homeButton;
 
 	static final String OBJ_LAYOUT = "layout";
 	static final String OBJ_LETTER_VALUES = "tvalues";
 	static final String OBJ_BOARD_STATE = "board";
     static final String OBJ_BAG = "bag";
     static final String OBJ_RACKS = "racks";
     static final String OBJ_SCORES = "scores";
 	static final int TILES_PER_RACK = 7;
 	static final int BOARD_SIZE = 15;
 	Player players[];
 	int numPlayers;
 	int passCount;
 	
     Musubi mMusubi;
     //Feed mFeed;
     User musubiMe;
     
     String lastMove = "";
     char[][] boardRep;
     private Sprite xCross, yCross;
     
     private WordPlayMultiplayer mMultiplayer;
     boolean isHost;
     ChangeableText titleText, player1Score, player2Score, player3Score, player4Score, tileCount, player1Name, player2Name, player3Name, player4Name;
 	// ===========================================================
 	// Constructors
 	// ===========================================================
 	 
 	// ===========================================================
 	// Getter &amp; Setter
 	// ===========================================================
 	 
 	// ===========================================================
 	// Methods for/from SuperClass/Interfaces
 	// ===========================================================
 
 	@Override
 	public Engine onLoadEngine() {
 		//this.mCamera = new SmoothCamera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT, 1, 1, 5.0f);
 		hud = new HUD();
 		gameOver = false;
 		showingPicker = false;
 		startCoordinate = null;
 
 		boardRep = new char[16][16];
 		for (int i = 0; i < 16; i++) {
 			for (int j = 0; j < 16; j++) {
 				boardRep[i][j] = '0';
 			}
 		}
 		
 		this.mCamera = new ZoomCamera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);  
 		//final int alturaTotal = CAMERA_HEIGHT*3;
 		this.mCamera.setBounds(0, CAMERA_WIDTH, 0, CAMERA_HEIGHT);
 		this.mCamera.setBoundsEnabled(true);
 		
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		ScreenOrientation deviceOrientation = ScreenOrientation.PORTRAIT;
 		int screenLayout = getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
 		if (screenLayout == Configuration.SCREENLAYOUT_SIZE_LARGE ||
 		        screenLayout == Configuration.SCREENLAYOUT_SIZE_XLARGE) {
 		    deviceOrientation = ScreenOrientation.LANDSCAPE;
 		}
 		return new Engine(new EngineOptions(false, deviceOrientation, new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), this.mCamera));
 		//return new Engine(new EngineOptions(false, ScreenOrientation.PORTRAIT, new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), this.mCamera));
 	}
 
 	@Override
 	public void onLoadResources() {
 		this.mTexture = new BitmapTextureAtlas(64, 64, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 		
 		this.mEngine.getTextureManager().loadTexture(this.mTexture);
 		
 
 		this.startTile = new BitmapTextureAtlas(64, 64, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 		this.baseTile = new BitmapTextureAtlas(64, 64, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 		this.doubleLetterTile = new BitmapTextureAtlas(64, 64, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 		this.tripleLetterTile = new BitmapTextureAtlas(64, 64, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 		this.doubleWordTile = new BitmapTextureAtlas(64, 64, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 		this.tripleWordTile = new BitmapTextureAtlas(64, 64, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 
 		
 		this.aTile = new BitmapTextureAtlas(64, 64, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 		this.bTile = new BitmapTextureAtlas(64, 64, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 		this.cTile = new BitmapTextureAtlas(64, 64, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 		this.dTile = new BitmapTextureAtlas(64, 64, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 		this.eTile = new BitmapTextureAtlas(64, 64, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 		this.fTile = new BitmapTextureAtlas(64, 64, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 		this.gTile = new BitmapTextureAtlas(64, 64, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 		this.hTile = new BitmapTextureAtlas(64, 64, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 		this.iTile = new BitmapTextureAtlas(64, 64, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 		this.jTile = new BitmapTextureAtlas(64, 64, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 		this.kTile = new BitmapTextureAtlas(64, 64, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 		this.lTile = new BitmapTextureAtlas(64, 64, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 		this.mTile = new BitmapTextureAtlas(64, 64, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 		this.nTile = new BitmapTextureAtlas(64, 64, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 		this.oTile = new BitmapTextureAtlas(64, 64, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 		this.pTile = new BitmapTextureAtlas(64, 64, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 		this.qTile = new BitmapTextureAtlas(64, 64, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 		this.rTile = new BitmapTextureAtlas(64, 64, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 		this.sTile = new BitmapTextureAtlas(64, 64, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 		this.tTile = new BitmapTextureAtlas(64, 64, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 		this.uTile = new BitmapTextureAtlas(64, 64, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 		this.vTile = new BitmapTextureAtlas(64, 64, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 		this.wTile = new BitmapTextureAtlas(64, 64, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 		this.xTile = new BitmapTextureAtlas(64, 64, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 		this.yTile = new BitmapTextureAtlas(64, 64, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 		this.zTile = new BitmapTextureAtlas(64, 64, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 		this.blankTile = new BitmapTextureAtlas(64, 64, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 
 		this.point1Tile = new BitmapTextureAtlas(64, 64, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 		this.point0Tile = new BitmapTextureAtlas(64, 64, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 		this.point2Tile = new BitmapTextureAtlas(64, 64, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 		this.point3Tile = new BitmapTextureAtlas(64, 64, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 		this.point4Tile = new BitmapTextureAtlas(64, 64, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 		this.point5Tile = new BitmapTextureAtlas(64, 64, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 		this.point6Tile = new BitmapTextureAtlas(64, 64, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 		this.point7Tile = new BitmapTextureAtlas(64, 64, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 		this.point8Tile = new BitmapTextureAtlas(64, 64, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 		this.point9Tile = new BitmapTextureAtlas(64, 64, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 		this.point10Tile = new BitmapTextureAtlas(64, 64, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 
 		this.startTileRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.startTile, this, "gfx/start_tile.png", 0, 0);
 		this.baseTileRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.baseTile, this, "gfx/tile_base.png", 0, 0);
 		this.doubleLetterTileRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.doubleLetterTile, this, "gfx/double_letter_tile.png", 0, 0);
 		this.tripleLetterTileRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.tripleLetterTile, this, "gfx/triple_letter_tile.png", 0, 0);
 		this.doubleWordTileRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.doubleWordTile, this, "gfx/double_word_tile.png", 0, 0);
 		this.tripleWordTileRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.tripleWordTile, this, "gfx/triple_word_tile.png", 0, 0);
 
 		activeOverlayTexture = new BitmapTextureAtlas(64, 64, TextureOptions.DEFAULT);
 		activeOverlayRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(activeOverlayTexture, this, "gfx/active_tile.png", 0, 0);
 		
 		tileCounterTexture = new BitmapTextureAtlas(64, 64, TextureOptions.DEFAULT);
 		tileCounterRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(tileCounterTexture, this, "gfx/wp-tile-counter.png", 0, 0);
 
 		uiSkinTexture = new BitmapTextureAtlas(512, 512, TextureOptions.DEFAULT);
 		uiSkinRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(uiSkinTexture, this, "gfx/wp-ui-skin.png", 0, 0);
 
 		uiSkinDarkenTexture = new BitmapTextureAtlas(512, 512, TextureOptions.DEFAULT);
 		uiSkinDarkenRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(uiSkinDarkenTexture, this, "gfx/wp-ui-skin-darken.png", 0, 0);
 		
 		boardBackgroundTexture = new BitmapTextureAtlas(512, 512, TextureOptions.DEFAULT);
 		boardBackgroundRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(boardBackgroundTexture, this, "gfx/wp-board-bg.png", 0, 0);
 		
 		horizontalCrosshairTexture = new BitmapTextureAtlas(512, 512, TextureOptions.DEFAULT);
 		horizontalCrosshairRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(horizontalCrosshairTexture, this, "gfx/horizontal_crosshair.png", 0, 0);
 		verticalCrosshairTexture = new BitmapTextureAtlas(512, 512, TextureOptions.DEFAULT);
 		verticalCrosshairRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(verticalCrosshairTexture, this, "gfx/vertical_crosshair.png", 0, 0);
 
 
 		tileOverlayTexture = new BitmapTextureAtlas(256, 256, TextureOptions.DEFAULT);
 		tileOverlayRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(tileOverlayTexture, this, "gfx/tile_overlay.png", 0, 0, 1, 1);
 
 		confirmButtonTexture = new BitmapTextureAtlas(256, 256, TextureOptions.DEFAULT);
 		confirmButtonRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(confirmButtonTexture, this, "gfx/confirm_button.png", 0, 0, 3, 1);
 
 		cancelButtonTexture = new BitmapTextureAtlas(256, 256, TextureOptions.DEFAULT);
 		cancelButtonRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(cancelButtonTexture, this, "gfx/cancel_button.png", 0, 0, 3, 1);
 
 		playButtonTexture = new BitmapTextureAtlas(256, 256, TextureOptions.DEFAULT);
 		playButtonRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(playButtonTexture, this, "gfx/play_button.png", 0, 0, 3, 1);
 		
 		passButtonTexture = new BitmapTextureAtlas(256, 256, TextureOptions.DEFAULT);
 		passButtonRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(passButtonTexture, this, "gfx/pass_button.png", 0, 0, 3, 1);
 		
 		swapButtonTexture = new BitmapTextureAtlas(256, 256, TextureOptions.DEFAULT);
 		swapButtonRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(swapButtonTexture, this, "gfx/swap_button.png", 0, 0, 3, 1);
 		
 		shuffleButtonTexture = new BitmapTextureAtlas(256, 256, TextureOptions.DEFAULT);
 		shuffleButtonRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(shuffleButtonTexture, this, "gfx/shuffle_button.png", 0, 0, 3, 1);
 		
 		clearButtonTexture = new BitmapTextureAtlas(256, 256, TextureOptions.DEFAULT);
 		clearButtonRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(clearButtonTexture, this, "gfx/clear_button.png", 0, 0, 3, 1);
 		
 		homeButtonTexture = new BitmapTextureAtlas(256, 256, TextureOptions.DEFAULT);
 		homeButtonRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(homeButtonTexture, this, "gfx/home_button.png", 0, 0, 3, 1);
 		
 		letterTileRegions = new HashMap<Character, TextureRegion>();
 		letterTileRegions.put('a', BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.aTile, this, "gfx/a_tile.png", 0, 0));
 		letterTileRegions.put('b', BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.bTile, this, "gfx/b_tile.png", 0, 0));
 		letterTileRegions.put('c', BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.cTile, this, "gfx/c_tile.png", 0, 0));
 		letterTileRegions.put('d', BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.dTile, this, "gfx/d_tile.png", 0, 0));
 		letterTileRegions.put('e', BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.eTile, this, "gfx/e_tile.png", 0, 0));
 		letterTileRegions.put('f', BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.fTile, this, "gfx/f_tile.png", 0, 0));
 		letterTileRegions.put('g', BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.gTile, this, "gfx/g_tile.png", 0, 0));
 		letterTileRegions.put('h', BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.hTile, this, "gfx/h_tile.png", 0, 0));
 		letterTileRegions.put('i', BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.iTile, this, "gfx/i_tile.png", 0, 0));
 		letterTileRegions.put('j', BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.jTile, this, "gfx/j_tile.png", 0, 0));
 		letterTileRegions.put('k', BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.kTile, this, "gfx/k_tile.png", 0, 0));
 		letterTileRegions.put('l', BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.lTile, this, "gfx/l_tile.png", 0, 0));
 		letterTileRegions.put('m', BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mTile, this, "gfx/m_tile.png", 0, 0));
 		letterTileRegions.put('n', BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.nTile, this, "gfx/n_tile.png", 0, 0));
 		letterTileRegions.put('o', BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.oTile, this, "gfx/o_tile.png", 0, 0));
 		letterTileRegions.put('p', BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.pTile, this, "gfx/p_tile.png", 0, 0));
 		letterTileRegions.put('q', BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.qTile, this, "gfx/q_tile.png", 0, 0));
 		letterTileRegions.put('r', BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.rTile, this, "gfx/r_tile.png", 0, 0));
 		letterTileRegions.put('s', BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.sTile, this, "gfx/s_tile.png", 0, 0));
 		letterTileRegions.put('t', BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.tTile, this, "gfx/t_tile.png", 0, 0));
 		letterTileRegions.put('u', BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.uTile, this, "gfx/u_tile.png", 0, 0));
 		letterTileRegions.put('v', BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.vTile, this, "gfx/v_tile.png", 0, 0));
 		letterTileRegions.put('w', BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.wTile, this, "gfx/w_tile.png", 0, 0));
 		letterTileRegions.put('x', BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.xTile, this, "gfx/x_tile.png", 0, 0));
 		letterTileRegions.put('y', BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.yTile, this, "gfx/y_tile.png", 0, 0));
 		letterTileRegions.put('z', BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.zTile, this, "gfx/z_tile.png", 0, 0));
 		letterTileRegions.put(' ', BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.blankTile, this, "gfx/blank_tile.png", 0, 0));
 
 		pointTileRegions = new TextureRegion[11];
 		pointTileRegions[0] = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.point0Tile, this, "gfx/point_0.png", 0, 0);
 		pointTileRegions[1] = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.point1Tile, this, "gfx/point_1.png", 0, 0);
 		pointTileRegions[2] = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.point2Tile, this, "gfx/point_2.png", 0, 0);
 		pointTileRegions[3] = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.point3Tile, this, "gfx/point_3.png", 0, 0);
 		pointTileRegions[4] = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.point4Tile, this, "gfx/point_4.png", 0, 0);
 		pointTileRegions[5] = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.point5Tile, this, "gfx/point_5.png", 0, 0);
 		pointTileRegions[6] = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.point6Tile, this, "gfx/point_6.png", 0, 0);
 		pointTileRegions[7] = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.point7Tile, this, "gfx/point_7.png", 0, 0);
 		pointTileRegions[8] = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.point8Tile, this, "gfx/point_8.png", 0, 0);
 		pointTileRegions[9] = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.point9Tile, this, "gfx/point_9.png", 0, 0);
 		pointTileRegions[10] = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.point10Tile, this, "gfx/point_10.png", 0, 0);
 
 		
 		player1ScorePlateTexture = new BitmapTextureAtlas(256, 256, TextureOptions.DEFAULT);
 		player1ScorePlateRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(player1ScorePlateTexture, this, "gfx/wp-score-plate.png", 0, 0, 2, 1);
 		
 		player2ScorePlateTexture = new BitmapTextureAtlas(256, 256, TextureOptions.DEFAULT);
 		player2ScorePlateRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(player2ScorePlateTexture, this, "gfx/wp-score-plate.png", 0, 0, 2, 1);
 		
 		player3ScorePlateTexture = new BitmapTextureAtlas(256, 256, TextureOptions.DEFAULT);
 		player3ScorePlateRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(player3ScorePlateTexture, this, "gfx/wp-score-plate.png", 0, 0, 2, 1);
 		
 		player4ScorePlateTexture = new BitmapTextureAtlas(256, 256, TextureOptions.DEFAULT);
 		player4ScorePlateRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(player4ScorePlateTexture, this, "gfx/wp-score-plate.png", 0, 0, 2, 1);
 		
 
 		this.mEngine.getTextureManager().loadTexture(player1ScorePlateTexture);
 		this.mEngine.getTextureManager().loadTexture(player2ScorePlateTexture);
 		this.mEngine.getTextureManager().loadTexture(player3ScorePlateTexture);
 		this.mEngine.getTextureManager().loadTexture(player4ScorePlateTexture);
 		
 		this.mEngine.getTextureManager().loadTexture(activeOverlayTexture);
 		this.mEngine.getTextureManager().loadTexture(tileCounterTexture);
 		this.mEngine.getTextureManager().loadTexture(uiSkinTexture);
 		this.mEngine.getTextureManager().loadTexture(uiSkinDarkenTexture);
 		this.mEngine.getTextureManager().loadTexture(boardBackgroundTexture);
 
 		this.mEngine.getTextureManager().loadTexture(horizontalCrosshairTexture);
 		this.mEngine.getTextureManager().loadTexture(verticalCrosshairTexture);
 		
 		this.mEngine.getTextureManager().loadTexture(confirmButtonTexture);
 		this.mEngine.getTextureManager().loadTexture(cancelButtonTexture);
 		this.mEngine.getTextureManager().loadTexture(playButtonTexture);
 		this.mEngine.getTextureManager().loadTexture(passButtonTexture);
 		this.mEngine.getTextureManager().loadTexture(shuffleButtonTexture);
 		this.mEngine.getTextureManager().loadTexture(swapButtonTexture);
 		this.mEngine.getTextureManager().loadTexture(clearButtonTexture);
 		this.mEngine.getTextureManager().loadTexture(homeButtonTexture);
 		
 		this.mEngine.getTextureManager().loadTexture(this.startTile);
 		this.mEngine.getTextureManager().loadTexture(this.baseTile);
 		this.mEngine.getTextureManager().loadTexture(this.doubleLetterTile);
 		this.mEngine.getTextureManager().loadTexture(this.tripleLetterTile);
 		this.mEngine.getTextureManager().loadTexture(this.doubleWordTile);
 		this.mEngine.getTextureManager().loadTexture(this.tripleWordTile);
 
 		this.mEngine.getTextureManager().loadTexture(tileOverlayTexture);
 		
 		this.mEngine.getTextureManager().loadTexture(aTile);
 		this.mEngine.getTextureManager().loadTexture(bTile);
 		this.mEngine.getTextureManager().loadTexture(cTile);
 		this.mEngine.getTextureManager().loadTexture(dTile);
 		this.mEngine.getTextureManager().loadTexture(eTile);
 		this.mEngine.getTextureManager().loadTexture(fTile);
 		this.mEngine.getTextureManager().loadTexture(gTile);
 		this.mEngine.getTextureManager().loadTexture(hTile);
 		this.mEngine.getTextureManager().loadTexture(iTile);
 		this.mEngine.getTextureManager().loadTexture(jTile);
 		this.mEngine.getTextureManager().loadTexture(kTile);
 		this.mEngine.getTextureManager().loadTexture(lTile);
 		this.mEngine.getTextureManager().loadTexture(mTile);
 		this.mEngine.getTextureManager().loadTexture(nTile);
 		this.mEngine.getTextureManager().loadTexture(oTile);
 		this.mEngine.getTextureManager().loadTexture(pTile);
 		this.mEngine.getTextureManager().loadTexture(qTile);
 		this.mEngine.getTextureManager().loadTexture(rTile);
 		this.mEngine.getTextureManager().loadTexture(sTile);
 		this.mEngine.getTextureManager().loadTexture(tTile);
 		this.mEngine.getTextureManager().loadTexture(uTile);
 		this.mEngine.getTextureManager().loadTexture(vTile);
 		this.mEngine.getTextureManager().loadTexture(wTile);
 		this.mEngine.getTextureManager().loadTexture(xTile);
 		this.mEngine.getTextureManager().loadTexture(yTile);
 		this.mEngine.getTextureManager().loadTexture(zTile);
 		this.mEngine.getTextureManager().loadTexture(blankTile);
 
 		this.mEngine.getTextureManager().loadTexture(point0Tile);
 		this.mEngine.getTextureManager().loadTexture(point1Tile);
 		this.mEngine.getTextureManager().loadTexture(point2Tile);
 		this.mEngine.getTextureManager().loadTexture(point3Tile);
 		this.mEngine.getTextureManager().loadTexture(point4Tile);
 		this.mEngine.getTextureManager().loadTexture(point5Tile);
 		this.mEngine.getTextureManager().loadTexture(point6Tile);
 		this.mEngine.getTextureManager().loadTexture(point7Tile);
 		this.mEngine.getTextureManager().loadTexture(point8Tile);
 		this.mEngine.getTextureManager().loadTexture(point9Tile);
 		this.mEngine.getTextureManager().loadTexture(point10Tile);
 
 		this.mFontTexture = new BitmapTextureAtlas(256, 256, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 		this.nameFontTexture = new BitmapTextureAtlas(256, 256, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 		this.scoreFontTexture = new BitmapTextureAtlas(256, 256, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 		this.tileCountFontTexture = new BitmapTextureAtlas(256, 256, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 
         this.mFont = new Font(this.mFontTexture, Typeface.create(Typeface.DEFAULT, Typeface.NORMAL), 14, true, Color.WHITE);
         this.nameFont = new Font(this.nameFontTexture, Typeface.create(Typeface.DEFAULT, Typeface.NORMAL), 13, true, Color.WHITE);
         this.scoreFont = new Font(this.scoreFontTexture, Typeface.create(Typeface.DEFAULT, Typeface.BOLD), 24, true, Color.WHITE);
         this.tileCountFont = new Font(this.tileCountFontTexture, Typeface.create(Typeface.DEFAULT, Typeface.NORMAL), 18, true, Color.BLACK);
 
         this.mEngine.getTextureManager().loadTexture(this.mFontTexture);
         this.mEngine.getTextureManager().loadTexture(this.nameFontTexture);
         this.mEngine.getTextureManager().loadTexture(this.scoreFontTexture);
         this.mEngine.getTextureManager().loadTexture(this.tileCountFontTexture);
         this.mEngine.getFontManager().loadFont(this.mFont);
         this.mEngine.getFontManager().loadFont(this.nameFont);
         this.mEngine.getFontManager().loadFont(this.scoreFont);
         this.mEngine.getFontManager().loadFont(this.tileCountFont);
         
         tileSpaces = new TileSpace[BOARD_SIZE][BOARD_SIZE];
         tileRack = new TileRack(this, true);
         dictionary = new TWLDictionary(this);
     	lastTap = 0;
 	}
 
 	@Override
 	public Scene onLoadScene() {
 	    scene = new Scene();
 	    float r = 74f/255f;
 	    float g = 36f/255f;
 	    float b = 22f/255f;
 	    scene.setBackground(new ColorBackground(r, g, b));
         scene.setOnAreaTouchTraversalFrontToBack();
 
 	    mMusubi = Musubi.forIntent(this, getIntent());
         mMultiplayer = new WordPlayMultiplayer(mMusubi.getObj());
         mMultiplayer.enableStateUpdates();
 
         // TODO: this device may have multiple owned identities-- this could even support a localplay game.
         JSONObject state = mMultiplayer.getLatestState();
         bag = new TileBag();
         bag.fromJson(state.optJSONArray(OBJ_BAG));
 
 		this.mScrollDetector = new SurfaceScrollDetector(this);
 		this.mScrollDetector.setEnabled(true);
 		
 		players = new Player[4];
 		players[0] = new Player("Player 1");
 		players[1] = new Player("Player 2");
 		players[2] = new Player("Player 3");
 		players[3] = new Player("Player 4");
 		
 		numPlayers = 0;
 		passCount = 0;       
 
         titleText = new ChangeableText(70, 12, this.mFont, "Player 1 played 'quixotic' for 100 points.", "Player 1 played 'quixotic' for 100 points.".length());
         
         player1Name = new ChangeableText(7, 463, this.nameFont, "USERNAME");
         player2Name = new ChangeableText(87, 463, this.nameFont, "USERNAME");
         player3Name = new ChangeableText(167, 463, this.nameFont, "USERNAME");
         player4Name = new ChangeableText(247, 463, this.nameFont, "USERNAME");
         
         player1Score = new ChangeableText(12, 474, this.scoreFont, players[0].getScore() + "   ");
         player2Score = new ChangeableText(92, 474, this.scoreFont, players[1].getScore() + "   ");
         player3Score = new ChangeableText(172, 474, this.scoreFont, players[2].getScore() + "   ");
         player4Score = new ChangeableText(252, 474, this.scoreFont, players[3].getScore() + "   ");
 
         tileCount = new ChangeableText(183, 413, this.tileCountFont, bag.tilesRemaining() + "") {
             final long LONGPRESS_THRESHOLD = 1500;
             Long lastDown = null;
 
            public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
                if (pSceneTouchEvent.isActionUp()) {
                    long up = System.currentTimeMillis();
                    if (lastDown != null && up - lastDown > LONGPRESS_THRESHOLD) {
                        if (mMultiplayer.isMyTurn()) {
                            try {
                                String alphabet = LetterValues.classicLetterValues;
                                int me = mMultiplayer.getGlobalMemberCursor();
                                JSONObject state = getInitialState(mMultiplayer.getMembers().length);
                                state.put(OBJ_LAYOUT, jsonForBoardLayout(BoardLayout.classicBoard));
                                state.put(OBJ_LETTER_VALUES, alphabet);
                                mMultiplayer.takeTurn(me, state);
                                TileRack newRack = new TileRack(WordPlayActivity.this, false);
                                newRack.fromJson(scene, alphabet, state.optJSONArray(OBJ_RACKS)
                                        .optJSONArray(mMultiplayer.getLocalMemberIndex()));
                                for (int i = 0; i < newRack.numTiles; i++) {
                                    Tile t = newRack.tiles[i];
                                    tileRack.replaceTile(scene, t.getLetter(), t.getPoints(), i);
                                }
                                render();
                                Toast.makeText(WordPlayActivity.this, "Classicist", Toast.LENGTH_SHORT).show();
                            } catch (JSONException e) {
                                Toast.makeText(WordPlayActivity.this, "Bad luck", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                    lastDown = null;
                } else if (pSceneTouchEvent.isActionDown()) {
                    lastDown = System.currentTimeMillis();
                }
                return true;
            }
         };
 
         Sprite uiSkin = new Sprite(0, 0, uiSkinRegion);
         Sprite tileCounter = new Sprite(178, 408, tileCounterRegion);
 
         player1ScorePlate = new TiledSprite(5, 462, player1ScorePlateRegion);
         player2ScorePlate = new TiledSprite(85, 462, player2ScorePlateRegion);
         player3ScorePlate = new TiledSprite(165, 462, player3ScorePlateRegion);
         player4ScorePlate = new TiledSprite(245, 462, player4ScorePlateRegion);
         
         hud.attachChild(uiSkin);
         hud.attachChild(titleText);
 
         hud.attachChild(player1ScorePlate);
         hud.attachChild(player2ScorePlate);
         hud.attachChild(player3ScorePlate);
         hud.attachChild(player4ScorePlate);
         
         hud.attachChild(player1Score);
         hud.attachChild(player2Score);
         hud.attachChild(player3Score);
         hud.attachChild(player4Score);
 
         hud.attachChild(player1Name);
         hud.attachChild(player2Name);
         hud.attachChild(player3Name);
         hud.attachChild(player4Name);
         
         hud.attachChild(tileCounter);
         hud.attachChild(tileCount);
         hud.registerTouchArea(tileCount);
 
         String alphabet = getAlphabet(state);
         tileRack.fromJson(scene, alphabet, 
                 state.optJSONArray(OBJ_RACKS).optJSONArray(mMultiplayer.getLocalMemberIndex()));
 
 		homeButton = new Button(6, 0, homeButtonRegion){
             @Override
             public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
         		if (pSceneTouchEvent.isActionDown()) {
         			this.press();
         		}
         		if (pSceneTouchEvent.isActionUp()) {
         			this.release();
         			Intent intent = new Intent(WordPlayActivity.this, WordPlayHomeActivity.class);
         			WordPlayActivity.this.startActivity(intent);
         		}
         		return true;
             }
         };
         hud.attachChild(homeButton);
         hud.registerTouchArea(homeButton);
         
 		playButton = new Button(270, 408, playButtonRegion){
             @Override
             public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
 				if (!mMultiplayer.isMyTurn() || gameOver) {
 					return true;
 				}
 
             	if (pSceneTouchEvent.isActionDown()) {
         			this.press();
         		}
         		if (pSceneTouchEvent.isActionUp()) {
         			TilePoint wordCoordinates[] = tileRack.placeTiles(scene, isFirstPlay());
         			if (wordCoordinates != null) {
         				Point[][] wordSet = calculateWordSet(wordCoordinates);
         				if (!verifyWordSet(wordSet)) {
         					tileRack.unsetLetters(scene);
 
                 			this.release();
         					return true;
         				}
 
         				int totalPoints = 0;
         				
         				if (tileRack.isBingo()) {
         					totalPoints += 50;
         				}
         				
         				tileRack.finalizeTiles(scene);
         				
         				for (int i = 0; i < wordSet.length; i++) {
         					if (wordSet[i] != null) {
         						totalPoints += calculatePointsForWord(wordSet[i]);
         					}
         				}
         				
         				for (int i = 0; i < wordCoordinates.length; i++) {
         					boardRep[wordCoordinates[i].x][wordCoordinates[i].y] = wordCoordinates[i].letter; 
         					tileSpaces[wordCoordinates[i].x][wordCoordinates[i].y].setUsed();
         				}
         				String alphabet = getAlphabet(mMultiplayer.getLatestState());
         				while(tileRack.numTiles < TILES_PER_RACK && bag.tilesRemaining() > 0) {
         				    char tile = bag.getNextTile();
         					tileRack.addTile(scene, tile, LetterValues.getLetterValue(alphabet, tile));
         				}
         				players[mMultiplayer.getLocalMemberIndex()].incrementScore(totalPoints);
     					player1Score.setText(players[0].getScore() + "");
 
     					String word = "";
     					for (int j = 0; j < wordSet[0].length; j++) {
     						word += tileSpaces[wordSet[0][j].x][wordSet[0][j].y].getLetter();
     					}
     					passCount = 0;
     					lastMove = mMultiplayer.getUser(mMultiplayer.getLocalMemberIndex()).getName() + " played '" + word.toLowerCase() + "' for " + totalPoints + "pts";
     					
     					if (tileRack.numTiles == 0) {
     						//lastMove = players[0].getShortName() + " won the game.";
     						String winners = "";
     						int topScore = 0;
     						for(int i = 0; i < numPlayers; i++) {
     							if(players[i].getScore() > topScore) {
     								topScore = players[i].getScore();
     							}
     						}
     						boolean tieGame = false;
     						for(int i = 0; i < numPlayers; i++) {
     							if(players[i].getScore() == topScore) {
     								if(winners == "") {
     									winners = players[i].getShortName();
     								}
     								else {
     									tieGame = true;
     									winners += ", " + players[i].getScore();
     								}
     							}
     						}
     						if(tieGame) {
     							lastMove = winners + " tied!";
     						}
     						else {
     							lastMove = winners + " won the game!";
     						}
     						gameOver = true;
     					}
     	        		WordPlayActivity.this.takeTurn();
     	        		//mMultiplayer.takeTurn(mMultiplayer.getApplicationState());
         			}
         			this.release();
         		}
         		return true;
             }
         };
         hud.attachChild(playButton);
         hud.registerTouchArea(playButton);
 
 
 		clearButton = new Button(115, 404, clearButtonRegion){
             @Override
             public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
         		if (pSceneTouchEvent.isActionDown()) {
         			this.press();
         		}
         		if (pSceneTouchEvent.isActionUp()) {
         			this.release();
         			tileRack.clearTiles();
         		}
         		return true;
             }
         };
         hud.attachChild(clearButton);
         hud.registerTouchArea(clearButton);
         
 
 		shuffleButton = new Button(7, 404, shuffleButtonRegion){
             @Override
             public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
         		if (pSceneTouchEvent.isActionDown()) {
         			this.press();
         		}
         		if (pSceneTouchEvent.isActionUp()) {
         			this.release();
         			tileRack.clearTiles();
         			tileRack.shuffleTiles(scene);
         		}
         		return true;
             }
         };
         hud.attachChild(shuffleButton);
         hud.registerTouchArea(shuffleButton);
 
 		swapButton = new Button(65, 404, swapButtonRegion){
             @Override
             public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
             	if (!mMultiplayer.isMyTurn() || gameOver) {
 					return true;
 				}
             	if (pSceneTouchEvent.isActionDown()) {
         			this.press();
         		}
         		if (pSceneTouchEvent.isActionUp()) {
         			//Log.w(TAG, "swapping");
         			showingPicker = true;
         			tileRack.clearTiles();
         			
 
         			tilePickerHud = new HUD();
 
         	        Sprite uiSkin = new Sprite(0, 0, uiSkinDarkenRegion);
 
         			final boolean[] selection = new boolean[7];
         			
         			final Button cancelButton = new Button(12, 404, cancelButtonRegion){
         	            @Override
         	            public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
         	        		if (pSceneTouchEvent.isActionDown()) {
         	        			this.press();
         	        		}
         	        		if (pSceneTouchEvent.isActionUp()) {
         	        			this.release();
         	        			mCamera.setHUD(hud);
         	        		}
         	        		return true;
         	            }
         	        };
         	        tilePickerHud.attachChild(uiSkin);
         	        tilePickerHud.attachChild(cancelButton);
         	        tilePickerHud.registerTouchArea(cancelButton);
         	        
         			final Button confirmSwapButton = new Button(65, 404, swapButtonRegion){
         	            @Override
         	            public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
         	        		if (pSceneTouchEvent.isActionDown()) {
         	        			this.press();
         	        		}
         	        		if (pSceneTouchEvent.isActionUp()) {
         	        			this.release();
         	        			
         	        			int numTiles = 0;
         	        			for (int i = 0; i < 7; i++) {
         	        				if (selection[i]) {
         	        					numTiles++;
         	        				}
         	        			}
         	        			
         	        			Character[] swapTiles = new Character[numTiles];
         	        			
         	        			int j = 0;
         	        			for (int i = 0; i < 7; i++) {
         	        				if (selection[i]) {
         	        					swapTiles[j] = tileRack.tiles[i].getLetter();
         	        					j++;
         	        				}
         	        			}
         	        			
         	        			if (numTiles > 0) {
         	        			    String alphabet = getAlphabet(mMultiplayer.getLatestState());
         	        				Character[] newTiles = bag.swapTiles(swapTiles);
         	        				j = 0;
         	        				for (int i = 0; i < 7; i++) {
         	        					if (selection[i]) {
         	        					    char letter = newTiles[j];
         	        						tileRack.replaceTile(scene, letter, LetterValues.getLetterValue(alphabet, letter), i);
         	        						j++;
         	        					}
         	        				}
 
         	                		lastMove = mMultiplayer.getUser(mMultiplayer.getLocalMemberIndex()).getName() + " swapped " + numTiles + " tiles";
         	                		WordPlayActivity.this.takeTurn();
         	    	        		//mMultiplayer.takeTurn(mMultiplayer.getApplicationState());
         	        			}
         	        			mCamera.setHUD(hud);
         	        		}
         	        		return true;
         	            }
         	        };
         	        tilePickerHud.attachChild(confirmSwapButton);
         	        tilePickerHud.registerTouchArea(confirmSwapButton);
         			
         			for (int i = 0; i < 7; i++) {
         				selection[i] = false;
         				if (tileRack.tiles[i] != null) {
         					final int pos = i;
             				int lastX = OFFSET_X+45*i;
             				int lastY = 358;
             				Sprite letterTile = new Sprite(lastX, lastY, letterTileRegions.get(Character.valueOf(tileRack.tiles[i].getLetter())));
             				
             				Button letterSelection = new Button(lastX, lastY, tileOverlayRegion) {
             		            @Override
             		            public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
             		            	if (!mMultiplayer.isMyTurn()) {
             							return true;
             						}
             		            	if(pSceneTouchEvent.isActionDown()) {
             		        			this.press();
             		        		}
             		        		if(pSceneTouchEvent.isActionUp()) {
             		        			this.release();
             		        			if (selection[pos]) {
             		        				this.setAlpha(.7f);
             		        			}
             		        			else {
             		        				this.setAlpha(0);
             		        			}
             		        			selection[pos] = !selection[pos];
             		        		}
             		        		return true;
             		            }
             				};
             				letterSelection.setAlpha(.7f);
             				tilePickerHud.attachChild(letterTile);
             				tilePickerHud.attachChild(letterSelection);
             				tilePickerHud.registerTouchArea(letterSelection);
         				}
         			}
         	        mCamera.setHUD(tilePickerHud);
         			this.release();
         		}
         		return true;
             }
         };
         hud.attachChild(swapButton);
         hud.registerTouchArea(swapButton);
         
 
 		passButton = new Button(220, 408, passButtonRegion){
             @Override
             public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
             	if (!mMultiplayer.isMyTurn() || gameOver) {
 					return true;
 				}
             	if(pSceneTouchEvent.isActionDown()) {
         			this.press();
         		}
         		if(pSceneTouchEvent.isActionUp()) {
         			this.release();
         			
         			AlertDialog.Builder builder = new AlertDialog.Builder(WordPlayActivity.this);
         			builder.setMessage("Are you sure you want to pass?")
         			       .setCancelable(false)
         			       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
         			           public void onClick(DialogInterface dialog, int id) {
         			                
         			        	   passCount++;
         		            		lastMove = mMultiplayer.getUser(mMultiplayer.getLocalMemberIndex()).getName() + " passed";
         		            		if (passCount >= numPlayers) {
         								//lastMove = players[0].getShortName() + " won the game.";
         								String winners = "";
         								int topScore = 0;
         								for(int i = 0; i < numPlayers; i++) {
         									if(players[i].getScore() > topScore) {
         										topScore = players[i].getScore();
         									}
         								}
         								boolean tieGame = false;
         								for(int i = 0; i < numPlayers; i++) {
         									if(players[i].getScore() == topScore) {
         										if(winners == "") {
         											winners = players[i].getShortName();
         										}
         										else {
         											tieGame = true;
         											winners += ", " + players[i].getShortName();
         										}
         									}
         								}
         								if(tieGame) {
         									lastMove = winners + " tied!";
         								}
         								else {
         									lastMove = winners + " won the game!";
         								}
         								gameOver = true;
         							}
         		            		WordPlayActivity.this.takeTurn();
         	    	        		//mMultiplayer.takeTurn(mMultiplayer.getApplicationState());
         			        	   
         			           }
         			       })
         			       .setNegativeButton("No", new DialogInterface.OnClickListener() {
         			           public void onClick(DialogInterface dialog, int id) {
         			                dialog.cancel();
         			           }
         			       });
         			AlertDialog alert = builder.create();
         			alert.show();
         			
         			
         		}
         		return true;
             }
         };
         hud.attachChild(passButton);
         hud.registerTouchArea(passButton);
 
         mCamera.setHUD(hud);
         scene.setOnSceneTouchListener(this);
         scene.setTouchAreaBindingEnabled(true);
 
 		return scene;
 	}
 
 	private String getAlphabet(JSONObject state) {
 	    String alphabet = state.optString(OBJ_LETTER_VALUES);
         if (alphabet == null || alphabet.length() == 0) {
             alphabet = LetterValues.letterValues;
         }
         return alphabet;
     }
 
 	@Override
 	protected void onResume() {
 	    super.onResume();
 	    if (mMultiplayer != null) {
 	        mMultiplayer.enableStateUpdates();
 	    }
 	}
 
 	@Override
 	protected void onPause() {
 	    super.onPause();
 	    if (mMultiplayer != null) {
 	        mMultiplayer.disableStateUpdates();
 	    }
 	}
 
     @Override
 	public void onLoadComplete() {
 		if (!Musubi.isMusubiInstalled(getApplicationContext())) {
 			// TODO: this is never seen because the activity can't be launched without Musubi.
 		    // Move to appropriate home activity
 			AlertDialog.Builder builder = new AlertDialog.Builder(this);
 			builder.setMessage("WordPlay requires the Musubi Social Platform")
 			       .setCancelable(false)
 			       .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
 			           public void onClick(DialogInterface dialog, int id) {
     			   			Intent updateIntent = Musubi.getMarketIntent();
     			   			startActivity(updateIntent); 
 			                WordPlayActivity.this.finish();
 			           }
 			       });
 			AlertDialog alert = builder.create();
 			alert.show();	
 		}
 
         if (mMultiplayer.getLocalMemberIndex() >= 0) {
         	players[mMultiplayer.getLocalMemberIndex()].setName(mMultiplayer.getUser(mMultiplayer.getLocalMemberIndex()).getName());
         }
         else {
         	runOnUpdateThread(new Runnable() {
                 @Override
                 public void run() {
                 	hud.detachChild(playButton);
                 	hud.detachChild(clearButton);
                 	hud.detachChild(shuffleButton);
                 	hud.detachChild(swapButton);
                 	hud.detachChild(passButton);
 
                 	hud.unregisterTouchArea(playButton);
                 	hud.unregisterTouchArea(clearButton);
                 	hud.unregisterTouchArea(shuffleButton);
                 	hud.unregisterTouchArea(swapButton);
                 	hud.unregisterTouchArea(passButton);
                 }
         	});
         }
         for(int i = 0; i < mMultiplayer.getMembers().length; i++) {	
             	players[i].setName(mMultiplayer.getUser(i).getName());
         }
 
         player1Name.setText(players[0].getShortName());
         player2Name.setText(players[1].getShortName());
         player3Name.setText(players[2].getShortName());
         player4Name.setText(players[3].getShortName());
         
 		player1Score.setText(""+players[0].getScore());
 		player2Score.setText(""+players[1].getScore());
 		player3Score.setText(""+players[2].getScore());
 		player4Score.setText(""+players[3].getScore());
         
 		player1Score.setColor(255, 255, 255);
 		player2Score.setColor(255, 255, 255);
 		player3Score.setColor(255, 255, 255);
 		player4Score.setColor(255, 255, 255);
 		
 		player1ScorePlate.setCurrentTileIndex(0);
 		player2ScorePlate.setCurrentTileIndex(0);
 		player3ScorePlate.setCurrentTileIndex(0);
 		player4ScorePlate.setCurrentTileIndex(0);
 		
 		if (mMultiplayer.getGlobalMemberCursor() == 0) {
 			//player1Score.setColor(1f, .647f, .341f);
 			player1ScorePlate.setCurrentTileIndex(1);
         }
 		else if (mMultiplayer.getGlobalMemberCursor() == 1) {
 			//player2Score.setColor(1f, .647f, .341f);
 			player2ScorePlate.setCurrentTileIndex(1);
         }
 		else if (mMultiplayer.getGlobalMemberCursor() == 2) {
 			//player3Score.setColor(1f, .647f, .341f);
 			player3ScorePlate.setCurrentTileIndex(1);
         }
 		else if (mMultiplayer.getGlobalMemberCursor() == 3) {
 			//player4Score.setColor(1f, .647f, .341f);
 			player4ScorePlate.setCurrentTileIndex(1);
         }
 		
 		numPlayers = mMultiplayer.getMembers().length;
 
 		if(numPlayers < 4) {
 			player4Name.setText("");
 			player4Score.setText("");
 			runOnUpdateThread(new Runnable() {
 	            @Override
 	            public void run() {
 	            	hud.detachChild(player4ScorePlate);
 	            }
             });
 		}
 		if(numPlayers < 3) {
 			Log.w(TAG, "detaching player3scoreplate");
 			player3Name.setText("");
 			player3Score.setText("");
 			runOnUpdateThread(new Runnable() {
 	            @Override
 	            public void run() {
 	            	hud.detachChild(player3ScorePlate);
 	            }
             });
 		}
 		
 		if (mMultiplayer.isMyTurn()) {
 			titleText.setText("Your turn");
 		} else {
 			titleText.setText(players[mMultiplayer.getGlobalMemberCursor()].getShortName() + "'s turn");
 		}
 		
 		render();
         
 	}
 	// ===========================================================
 	// Methods
 	// ===========================================================
 
 	public void takeTurn() {
 		mMultiplayer.takeTurn(mMultiplayer.getApplicationState());
 		render();
 	}
 	
 	public Point[][] calculateWordSet(TilePoint[] wordCoordinates) {
 		boolean horizontal = false;
 		
 		//length 1, arbitrarily pick horizontal as alignment
 		if (wordCoordinates.length == 1) {
 			horizontal = true;
 		}
 		//vertical alignment
 		else if (wordCoordinates[0].x == wordCoordinates[1].x) {
 			horizontal = false;
 		}
 		//horizontal alignment
 		else {
 			horizontal = true;
 		}
 		Point[][] wordSet = new Point[wordCoordinates.length+1][];
 		wordSet[0] = wordCoordinates;
 		
 		for (int i = 0; i < wordCoordinates.length; i++) {
 			//get orthogonally aligned word
 			if(wordCoordinates[i].played) {
 				Point[] altWord = getWordCoordinates(wordCoordinates[i], !horizontal);
 				//check that word is atleast 2 letters long
 				if (altWord.length > 1) {
 					wordSet[i+1] = altWord;
 				}
 			}
 		}
 		return wordSet;
 	}
 	
 	public Point[] getWordCoordinates(Point anchor, boolean horizontal) {
 		int maxPos, minPos;
 		Point[] wordCoordinates = null;
 		if (horizontal) {
 			minPos = anchor.x;
 			maxPos = anchor.x;
 			//get earliest letter coordinate
 			for (int i = anchor.x; i >= 0; i--) {
 				if (tileSpaces[i][anchor.y].getLetter() != '0') {
 					minPos = i;
 				}
 				else {
 					break;
 				}
 			}
 			//get latest letter coordinate
 			for (int i = anchor.x; i < BOARD_SIZE; i++) {
 				if (tileSpaces[i][anchor.y].getLetter() != '0') {
 					maxPos = i;
 				}
 				else {
 					break;
 				}
 			}
 			//construct word coordinates
 			wordCoordinates = new Point[maxPos-minPos+1];
 			for (int i = minPos; i <= maxPos; i++) {
 				wordCoordinates[i-minPos] = new Point(i, anchor.y);
 			}
 		}
 		else {
 			minPos = anchor.y;
 			maxPos = anchor.y;
 			//get earliest letter coordinate
 			for (int i = anchor.y; i >= 0; i--) {
 				if (tileSpaces[anchor.x][i].getLetter() != '0') {
 					minPos = i;
 				}
 				else {
 					break;
 				}
 			}
 			//get latest letter coordinate
 			for (int i = anchor.y; i < BOARD_SIZE; i++) {
 				if (tileSpaces[anchor.x][i].getLetter() != '0') {
 					maxPos = i;
 				}
 				else {
 					break;
 				}
 			}
 			//construct word coordinates
 			wordCoordinates = new Point[maxPos-minPos+1];
 			for (int i = minPos; i <= maxPos; i++) {
 				wordCoordinates[i-minPos] = new Point(anchor.x, i);
 			}
 		}
 		return wordCoordinates;
 	}
 	
 	public boolean verifyWordSet(Point[][] wordSet) {
 		for (int i = 0; i < wordSet.length; i++) {
 			if (wordSet[i] == null) {
 				continue;
 			}
 			String word = "";
 			for (int j = 0; j < wordSet[i].length; j++) {
 				word += tileSpaces[wordSet[i][j].x][wordSet[i][j].y].getLetter();
 			}
 			if (!dictionary.isWord(word)) {
 				Toast.makeText(WordPlayActivity.this, word + " is not a word.", Toast.LENGTH_SHORT).show();
 				return false;
 			}
 		}
 		return true;
 	}
 	
 	public int calculatePointsForWord(Point[] wordCoordinates) {
 		int wordModifier = 1;
 		int wordPoints = 0;
 		String word = "";
 		
 		for(int i = 0; i < wordCoordinates.length; i++) {
 			TileSpace tile = tileSpaces[wordCoordinates[i].x][wordCoordinates[i].y];
 			wordPoints += tile.getPoints();
 			wordModifier *= tile.getMultiplier();
 			word += tile.getLetter();
 		}
 		
 		
 		int totalPoints = wordPoints * wordModifier;
 		//Log.w(TAG, word + " for " + totalPoints + " points");
 		return totalPoints;
 	}
 	
 	
 	@Override
 	public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent) {
 		// TODO Auto-generated method stub
 		if(showingPicker) {
 			return true;
 		}
 		this.mScrollDetector.onTouchEvent(pSceneTouchEvent);
 		if(pSceneTouchEvent.getAction() == MotionEvent.ACTION_DOWN)
         {
 	    	Calendar rightNow = Calendar.getInstance();
 			long currentTap = rightNow.getTimeInMillis();
 			if (currentTap - lastTap < 500) {
 				lastTap = 0;
 				if (mCamera.getZoomFactor() == 1) {
         			mCamera.setZoomFactor(2);
         			float centerX = pSceneTouchEvent.getX();
         			float centerY = pSceneTouchEvent.getY();
         			
         			if (centerY < 144) {
         				centerY = 144;
         			}
         			if (centerY > 303) {
         				centerY = 303;
         			}
         			this.mCamera.setCenter(centerX, centerY);
 				}
 				else {
         			mCamera.setZoomFactor(1);
 				}
 			}
 			else {
 				lastTap = currentTap;
 			}
         }
 		return true;
 	}
 
 	@Override
 	public void onScroll(ScrollDetector pScollDetector, TouchEvent pTouchEvent,
 			float pDistanceX, float pDistanceY) {
 		// TODO Auto-generated method stub
 		if (showingPicker) {
 			return;
 		}
 		//Log.d(TAG, "Scroll {x:"+pDistanceX+", y: "+pDistanceY+"}");
 		float centerX = this.mCamera.getCenterX();
 		float centerY = this.mCamera.getCenterY();
 		
 		centerX -= pDistanceX;
 		centerY -= pDistanceY;
 		if (centerY < 144) {
 			centerY = 144;
 		}
 		if (centerY > 303) {
 			centerY = 303;
 		}
 		this.mCamera.setCenter(centerX, centerY);
 		/*this.mCamera.offsetCenter(-pDistanceX, -pDistanceY);
 		if (this.mCamera.getCenterX() < 40) {
 			this.mCamera.
 		}*/
 	}
 	 
 	// ===========================================================
 	// Inner and Anonymous Classes
 	// =========================================================== 
 
     class WordPlayMultiplayer extends TurnBasedApp {
         public WordPlayMultiplayer(DbObj obj) {
             super(obj);
         }
 
         @Override
         protected FeedRenderable getFeedView(JSONObject arg0) {
             StringBuilder html = new StringBuilder("<html>");
             html.append("<body style=\"width:200px\">");
             html.append("<div style=\"font-weight:bold; text-align: center;\">Scoreboard</div>");
             html.append("<div style=\"border: 3px solid black; border-radius: 10px; padding: 5px; background:#4D5157;\">");
             for (int i = 0; i < numPlayers; i++) {
                 String color;
                 if ((getGlobalMemberCursor())%numPlayers == i) {
                     color = "#FF752B";
                 } else {
                     color = "#ffffff";
                 }
                 html.append("<div style=\"width: 100px; margin-left: 10px; float: left; text-align: left; font-weight:bold; color:").append(color).append("\">").append(players[i].getShortName()).append("</div>")
                     .append("<div style=\"width: 50px; margin-left: 10px; float: right; color: #ffffff; text-align: right;\">").append(players[i].getScore()).append(" pts</div>")
                     .append("<div style=\"clear: both;\"></div>");
             }
             html.append("</div><div style=\"text-align: center;\">").append(lastMove).append("</div><div style=\"text-align: center;\">");
             html.append(bag.tilesRemaining()).append(" tiles remaining");
             html.append("</div></body>");
 
             html.append("</html>");
             Log.w(TAG, html.toString());
             return FeedRenderable.fromHtml(html.toString());
         }
 
         @Override
         protected void onStateUpdate(JSONObject state) {
             lastMove = state.optString("lastmove");
             render();
         }
 
         private JSONObject getApplicationState() {
             JSONObject state = getLatestState() == null ? new JSONObject() : getLatestState();
             numPlayers = getMembers().length;
             try {
                 state.put("passcount", passCount);
                 state.put("gameover", gameOver);
                 state.put("lastmove", lastMove);
 
                 JSONArray board = new JSONArray();
                 for (int i = 0; i < 16; i++) {
                     JSONArray row = new JSONArray();
                     for (int j = 0; j < 16; j++) {
                         row.put(boardRep[i][j] + "");
                     }
                     board.put(row);
                 }
                 state.put(OBJ_BOARD_STATE, board);                
 
                 JSONArray racks = new JSONArray();
 
                 TileRack opponentRacks[] = new TileRack[numPlayers];// = new TileRack(this, false);
                 for(int i = 0; i < numPlayers-1; i++) {
                     opponentRacks[i] = new TileRack(WordPlayActivity.this, false);
                 }
                 
                 JSONArray oldRacks = mMultiplayer.getLatestState().getJSONArray(OBJ_RACKS);
                 int j = 0;
                 for(int i = 0; i < numPlayers; i++) {
                     if (i != getLocalMemberIndex()) {
                         opponentRacks[j].fromJson(scene, getAlphabet(state), oldRacks.getJSONArray(i));
                         j++;
                     }
                 }
 
                 tileCount.setText(bag.tilesRemaining() + "");
                 state.put(OBJ_BAG, bag.toJson());
                 
                j = 0;
                 for(int i = 0; i < numPlayers; i++) {
                     if (i == getLocalMemberIndex()) {
                         racks.put(tileRack.toJson());
                    }
                    else {
                        racks.put(opponentRacks[j].toJson());
                     }
                 }
                 state.put(OBJ_RACKS, racks);
                 //Log.w(TAG, racks.toString());
                 
                 JSONArray scores = new JSONArray();
                 scores.put(players[0].getScore());
                 scores.put(players[1].getScore());
                 scores.put(players[2].getScore());
                 scores.put(players[3].getScore());
                 state.put(OBJ_SCORES, scores);
             } catch (JSONException e) {
                 //Log.wtf(TAG, "Failed to get board state", e);
             }
             //Log.d(TAG, "SETTING APP STATE " + state);
             return state;
         }
     }
 
     public void showTentativePoints() {
     	TilePoint wordCoordinates[] = tileRack.placeTiles(scene, isFirstPlay());
 		if (wordCoordinates != null) {
 			Point[][] wordSet = calculateWordSet(wordCoordinates);
 			
 			int totalPoints = 0;
 			
 			if (tileRack.isBingo()) {
 				totalPoints += 50;
 			}
 			
 			for (int i = 0; i < wordSet.length; i++) {
 				if (wordSet[i] != null) {
 					totalPoints += calculatePointsForWord(wordSet[i]);
 				}
 			}
 			titleText.setText("This play is worth " + totalPoints + "pts");
 			tileRack.unsetLetters(scene);
 		}
 		else {
 			//Log.w(TAG, "not a valid placement");
 			titleText.setText(lastMove);
 		}
     }
     
     public void drawCrosshair(final int xPos, final int yPos) {
 
 		final int x = OFFSET_X+xPos*21;
 		final int y = OFFSET_Y+yPos*21;
 		runOnUpdateThread(new Runnable() {
             @Override
             public void run() {
             	if(xCross != null && yCross != null) {
 	        		if (scene.getChildIndex(xCross) >= 0) {
 	                	scene.detachChild(xCross);
 	                }
 	                if (scene.getChildIndex(yCross) >= 0) {
 	                	scene.detachChild(yCross);
 	                }
             	}
                 if (xPos >= 0 && xPos < BOARD_SIZE && yPos >= 0 && yPos < BOARD_SIZE) {
                 	xCross = new Sprite(OFFSET_X, y, horizontalCrosshairRegion);
                 	yCross = new Sprite(x, OFFSET_Y, verticalCrosshairRegion);
                 	//xCross.setAlpha(.01f);
                 	//yCross.setAlpha(.01f);
                 	scene.attachChild(xCross);
                 	scene.attachChild(yCross);
                 }
             }
     	});
     }
     
     public void removeCrosshair() {
 
 		runOnUpdateThread(new Runnable() {
             @Override
             public void run() {
             	if(xCross != null && yCross != null) {
 	        		if (scene.getChildIndex(xCross) >= 0) {
 	                	scene.detachChild(xCross);
 	                }
 	                if (scene.getChildIndex(yCross) >= 0) {
 	                	scene.detachChild(yCross);
 	                }
             	}
             }
     	});
     }
 
     private void render() {
     	JSONObject state = mMultiplayer.getLatestState();
     	if (DBG) Log.w(TAG, "rendering normal state " + mMultiplayer.getLatestState());
         numPlayers = mMultiplayer.getMembers().length;
 
         gameOver = state.optBoolean("gameover");
         passCount = state.optInt("passcount");
 
         // board layout
         JSONArray arr = mMultiplayer.getLatestState().optJSONArray(OBJ_LAYOUT);
         if (arr == null) {
             arr = jsonForBoardLayout(BoardLayout.board);
         }
         renderBoardLayout(arr);
 
         // brand new bag
         bag.fromJson(state.optJSONArray(OBJ_BAG));
 
         // game state
         JSONArray board = state.optJSONArray(OBJ_BOARD_STATE);
         for (int i = 0; i < BOARD_SIZE; i++) {
         	JSONArray row = board.optJSONArray(i);
         	for (int j = 0; j < BOARD_SIZE; j++) {
         		char c = row.optString(j).charAt(0);
         		boardRep[i][j] = c;
         		tileSpaces[i][j].setLetter(c, LetterValues.getLetterValue(getAlphabet(state), c));
         		if (c != '0') {
         			tileSpaces[i][j].setUsed();
             		tileSpaces[i][j].finalizeLetter(WordPlayActivity.this, scene);
         		}
         	}
         }
 
         // scores
         for(int i = 0; i < numPlayers; i++) {
             players[i].setScore(state.optJSONArray(OBJ_SCORES).optInt(i));
         }
 
         lastMove = state.optString("lastmove");
         titleText.setText(lastMove);
 		player1Score.setText(""+players[0].getScore());
 		player2Score.setText(""+players[1].getScore());
 		player3Score.setText(""+players[2].getScore());
 		player4Score.setText(""+players[3].getScore());
 		
 
 		player1Score.setColor(255, 255, 255);
 		player2Score.setColor(255, 255, 255);
 		player3Score.setColor(255, 255, 255);
 		player4Score.setColor(255, 255, 255);
 		player1ScorePlate.setCurrentTileIndex(0);
 		player2ScorePlate.setCurrentTileIndex(0);
 		player3ScorePlate.setCurrentTileIndex(0);
 		player4ScorePlate.setCurrentTileIndex(0);
 		
 		if (mMultiplayer.getGlobalMemberCursor() == 0) {
 			//player1Score.setColor(1f, .647f, .341f);
 			player1ScorePlate.setCurrentTileIndex(1);
         }
 		else if (mMultiplayer.getGlobalMemberCursor() == 1) {
 			//player2Score.setColor(1f, .647f, .341f);
 			player2ScorePlate.setCurrentTileIndex(1);
         }
 		else if (mMultiplayer.getGlobalMemberCursor() == 2) {
 			//player3Score.setColor(1f, .647f, .341f);
 			player3ScorePlate.setCurrentTileIndex(1);
         }
 		else if (mMultiplayer.getGlobalMemberCursor() == 3) {
 			//player4Score.setColor(1f, .647f, .341f);
 			player4ScorePlate.setCurrentTileIndex(1);
         }
 
 		if(numPlayers < 4) {
 			player4Name.setText("");
 			player4Score.setText("");
 			runOnUpdateThread(new Runnable() {
 	            @Override
 	            public void run() {
 	            	hud.detachChild(player4ScorePlate);
 	            }
             });
 		}
 		if(numPlayers < 3) {
 			player3Name.setText("");
 			player3Score.setText("");
 			runOnUpdateThread(new Runnable() {
 	            @Override
 	            public void run() {
 	            	hud.detachChild(player3ScorePlate);
 	            }
             });
 		}
 
         tileCount.setText(bag.tilesRemaining() + "");
         
         if(gameOver) {
         	int topScore = 0;
         	for(int i = 0; i < numPlayers; i++) {
         		if(players[i].getScore() > topScore) {
         			topScore = players[i].getScore();
         		}
         	}
         	int numWinners = 0;
         	for(int i = 0; i < numPlayers; i++) {
         		if(players[i].getScore() == topScore) {
         			numWinners++;
         		}
         	}
         	if (mMultiplayer.getLocalMemberIndex() >= 0) {
 	        	if(players[mMultiplayer.getLocalMemberIndex()].getScore() == topScore) {
 	        		if (numWinners == 1) {
 	        			titleText.setText("You won!");
 	            		Toast.makeText(WordPlayActivity.this, "You won!", Toast.LENGTH_SHORT).show();
 	        			
 	        		}
 	        		else {
 	        			titleText.setText("You tied!");
 	            		Toast.makeText(WordPlayActivity.this, "You tied!", Toast.LENGTH_SHORT).show();
 	        		}
 	        	}
 	        	else {
 	        		titleText.setText("You lost!");
 	        		Toast.makeText(WordPlayActivity.this, "You lost!", Toast.LENGTH_SHORT).show();
 	        	}
         	}
         }
 
     	if (mMultiplayer.isMyTurn() && !gameOver) {
     		Toast.makeText(WordPlayActivity.this, "Your turn!", Toast.LENGTH_SHORT).show();
 		}
     }
 
     void renderBoardLayout(JSONArray layout) {
         if (layout == null || layout.length() < BOARD_SIZE) {
             Toast.makeText(this, "Failed to load board layout.", Toast.LENGTH_LONG).show();
             finish();
         }
 
         for (int i = 0; i < BOARD_SIZE; i++) {
             JSONArray row = layout.optJSONArray(i);
             for (int j = 0; j < BOARD_SIZE; j++) {
                 String square = row.optString(j);
                 if (square.equals("DL")) {
                     tileSpaces[i][j] = new DoubleLetterTileSpace(this, OFFSET_X+i*21, OFFSET_Y+j*21);
                 }
                 else if (square.equals("TL")) {
                     tileSpaces[i][j] = new TripleLetterTileSpace(this, OFFSET_X+i*21, OFFSET_Y+j*21);
                 }
                 else if (square.equals("DW")) {
                     tileSpaces[i][j] = new DoubleWordTileSpace(this, OFFSET_X+i*21, OFFSET_Y+j*21);
                 }
                 else if (square.equals("TW")) {
                     tileSpaces[i][j] = new TripleWordTileSpace(this, OFFSET_X+i*21, OFFSET_Y+j*21);
                 }
                 else if (square.equals("ST")) {
                     tileSpaces[i][j] = new StartTileSpace(this, OFFSET_X+i*21, OFFSET_Y+j*21, 1);
                     startCoordinate = new Point(i, j);
                 }
                 else if (square.equals("S2")) {
                     tileSpaces[i][j] = new StartTileSpace(this, OFFSET_X+i*21, OFFSET_Y+j*21, 2);
                     startCoordinate = new Point(i, j);
                 }
                 else {
                     tileSpaces[i][j] = new BasicTileSpace(this, OFFSET_X+i*21, OFFSET_Y+j*21);
                 }
                 tileSpaces[i][j].draw(scene);
             }
         }
     }
     
     private boolean isFirstPlay() {
     	for(int i = 0; i < BOARD_SIZE; i++) {
     		for(int j = 0; j < BOARD_SIZE; j++) {
     			if (tileSpaces[i][j].getLetter() != '0') {
     				return false;
     			}
     		}
     	}
     	return true;
     }
 
     public static JSONObject getInitialState(int numPlayers) {
         JSONObject state = new JSONObject();
         try {
             JSONArray board = new JSONArray();
             for (int i = 0; i < BOARD_SIZE; i++) {
                 JSONArray row = new JSONArray();
                 for (int j = 0; j < BOARD_SIZE; j++) {
                     row.put("0");
                 }
                 board.put(row);
             }            
 
             TileBag bag = new TileBag();
             assert(bag.tilesRemaining() > numPlayers * 7);
 
             JSONArray racks = new JSONArray();
             JSONArray scores = new JSONArray();
             for (int i = 0; i < numPlayers; i++) {
                 JSONArray rack = new JSONArray();
                 for (int j = 0; j < TILES_PER_RACK; j++) {
                     rack.put("" + bag.getNextTile());
                     scores.put(0);
                 }
                 racks.put(rack);
             }
 
             state.put(OBJ_LAYOUT, jsonForBoardLayout(BoardLayout.board));
             state.put(OBJ_BOARD_STATE, board);
             state.put(OBJ_LETTER_VALUES, LetterValues.letterValues);
             state.put(OBJ_BAG, bag.toJson());
             state.put(OBJ_RACKS, racks);
             state.put(OBJ_SCORES, scores);
             return state;   
         } catch (JSONException e) {
             Log.e(TAG, "impossible exception", e);
             throw new IllegalStateException("error getting initial state", e);
         }
     }
 
     static JSONArray jsonForBoardLayout(String[][] board) {
         JSONArray layout = new JSONArray();
         for (int i = 0; i < BOARD_SIZE; i++) {
             JSONArray row = new JSONArray();
             for (int j = 0; j < BOARD_SIZE; j++) {
                 /**
                  * TODO: An ObjContext supports attaching data in attachment objs.
                  * Attach a named obj called "layout" and attach it to the game instance
                  * once it is determined. This avoids sending out the layout on each turn
                  * while keeping code concise and correct.
                  */
                 row.put(board[i][j]);
             }
             layout.put(row);
         }
         return layout;
     }
 }
