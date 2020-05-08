 package com.bugfullabs.qube;
 
 import java.io.IOException;
 
 import org.anddev.andengine.audio.music.Music;
 import org.anddev.andengine.audio.music.MusicFactory;
 import org.anddev.andengine.engine.handler.timer.ITimerCallback;
 import org.anddev.andengine.engine.handler.timer.TimerHandler;
 import org.anddev.andengine.entity.scene.Scene;
 import org.anddev.andengine.entity.scene.background.SpriteBackground;
 import org.anddev.andengine.entity.sprite.Sprite;
 import org.anddev.andengine.entity.util.FPSLogger;
 import org.anddev.andengine.extension.texturepacker.opengl.texture.util.texturepacker.TexturePack;
 import org.anddev.andengine.extension.texturepacker.opengl.texture.util.texturepacker.TexturePackLoader;
 import org.anddev.andengine.extension.texturepacker.opengl.texture.util.texturepacker.exception.TexturePackParseException;
 import org.anddev.andengine.input.touch.TouchEvent;
 import org.anddev.andengine.opengl.font.StrokeFont;
 import org.anddev.andengine.opengl.texture.TextureOptions;
 import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
 import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
 import org.anddev.andengine.opengl.texture.region.TextureRegion;
 import org.anddev.andengine.util.HorizontalAlign;
 import org.anddev.andengine.util.VerticalAlign;
 
 
 import android.content.SharedPreferences;
 import android.graphics.Color;
 import android.graphics.Typeface;
 import android.util.Log;
 import com.bugfullabs.qube.level.Level;
 import com.bugfullabs.qube.level.LevelFileReader;
 import com.bugfullabs.qube.level.LevelSceneFactory;
 import com.bugfullabs.qube.util.AlignedText;
 import com.bugfullabs.qube.util.Button;
 
 import com.bugfullabs.qube.hud.ItemsHUD;
 
 import com.bugfullabs.qube.game.CubeEntity;
 import com.bugfullabs.qube.game.GameValues;
 import com.bugfullabs.qube.game.ScoreReader;
 import com.openfeint.api.resource.Achievement;
 import com.openfeint.api.resource.Achievement.UnlockCB;
 import com.openfeint.api.resource.Achievement.UpdateProgressionCB;
 import com.openfeint.api.resource.Leaderboard;
 import com.openfeint.api.resource.Score;
 
 /**
  * 
  * @author Bugful Labs
  * @author Grushenko
  * @email  wojciech@bugfullabs.pl
  *
  */
 
 public class GameActivity extends LoadingActivity{
 	
 	public static Level level;
 	
 	private Scene gameScene;
 	private Scene scoreScene;
 	
 	
 	private TimerHandler updateTimer;
 	
 	private BitmapTextureAtlas mAtlas;
 	private BitmapTextureAtlas mFontTexture;
 	private BitmapTextureAtlas starAtlas;
 	private BitmapTextureAtlas mBigFontTexture;
 	
 	private TextureRegion buttonTexture;
 	private TextureRegion starFull;
 	private TextureRegion starBlank;
 	
 	private TexturePack levelItemsPack;
 	private TexturePack levelPack;
 	
 	private StrokeFont Stroke;
 	private StrokeFont bigFont;
 	
 	SharedPreferences settings;
 	SharedPreferences.Editor editor;
 	private static final String SETTINGS_FILE = "Settings";
 	
 	
 	private Music gameMusic; 
 	
 	private ItemsHUD mItemsHUD;
 	
 	private int stars = 0;
 	private int cubesFinished = 0;
 	
 	private float gameTime = 0;
 	
 	@Override
 	protected void assetsToLoad() {
 			
 		super.setLoadingProgress(10);
 		this.mAtlas = new BitmapTextureAtlas(256, 256, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/game/");
 		super.setLoadingProgress(20);
 		this.buttonTexture = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mAtlas, this, "button.png", 0, 0);
 		
 		this.mFontTexture = new BitmapTextureAtlas(256, 256, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 		this.mBigFontTexture = new BitmapTextureAtlas(256, 256, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 		
 	    super.setLoadingProgress(50);
 		
 		Typeface typeface =  Typeface.createFromAsset(getAssets(), "font/FOO.ttf");
 	    this.Stroke = new StrokeFont(mFontTexture, typeface, 26, true, Color.WHITE, 2, Color.BLACK);
 	    this.bigFont = new StrokeFont(mBigFontTexture, typeface, 42, true, Color.WHITE, 2, Color.BLACK);
 		
 	    super.setLoadingProgress(60);
 	    
 	    this.starAtlas = new BitmapTextureAtlas(256, 256, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 	    this.starFull = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.starAtlas, this, "star_full.png", 0, 0);
 	    this.starBlank = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.starAtlas, this, "star_blank.png", 128, 0);
 	    
 	    super.setLoadingProgress(70);
 	    
 	    try {
 			levelPack = new TexturePackLoader(this, "gfx/game/").loadFromAsset(this, level.getLevelTexture()+".xml");
 		} catch (TexturePackParseException e) {
 			e.printStackTrace();
 		}
 	    
 	    try {
 			levelItemsPack = new TexturePackLoader(this, "gfx/game/").loadFromAsset(this, level.getLevelTexture()+"_items.xml");
 		} catch (TexturePackParseException e) {
 			e.printStackTrace();
 		}	    
 	    
 	    super.setLoadingProgress(80);
 	    
 	    MusicFactory.setAssetBasePath("music/");
         try {
                 this.gameMusic = MusicFactory.createMusicFromAsset(this.mEngine.getMusicManager(), this, "game.ogg");
                 this.gameMusic.setLooping(true);
                 this.gameMusic.setVolume(10.0f);
         } catch (final IOException e) {
                 Log.e("Error", e.toString());
         }
 	    
 	    this.mEngine.getTextureManager().loadTextures(this.mAtlas, this.mFontTexture, this.mBigFontTexture,this.starAtlas, this.levelPack.getTexture(), this.levelItemsPack.getTexture());
 	    this.mEngine.getFontManager().loadFonts(Stroke, bigFont);      
 	    super.setLoadingProgress(90);
 		
 	    createScoreScene();
 	    
 		settings = getSharedPreferences(SETTINGS_FILE, 0);
 		editor = settings.edit();		
 	
 	    super.setLoadingProgress(100);
 	    
 	}
 	
 	
 	
 	@Override
 	protected Scene onAssetsLoaded() {
 		this.mEngine.registerUpdateHandler(new FPSLogger());
 		
 		if(settings.getBoolean("music", true)){
 		this.gameMusic.play();
 		}
 		gameScene = LevelSceneFactory.createScene(level, levelPack);
 		
 		mItemsHUD = new ItemsHUD(this.mCamera, levelItemsPack){
 			@Override
 			protected void onPlay(){
 				GameActivity.this.start();
 			}
 			@Override
 			protected void onStop(){
 				GameActivity.this.stop();
 			}
 			
 			@Override 
 			protected void onButtonTouchEvent(final int id, final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY){
 				Log.i("TOUCH EVENT","Game");
 				this.getButton(id).setItemScale(1.33f, 1.33f);
 				this.getButton(id).setItemPosition(pSceneTouchEvent.getX() - getButton(id).getItemWidth() / 2, pSceneTouchEvent.getY() - getButton(id).getItemHeight() / 2);
 			
 				if(pSceneTouchEvent.isActionUp()){
 					
 					int dropX;
 					int dropY;
 					
 					int tableX;
 					int tableY;
 					
 					this.getButton(id).reset();
 					
 					tableX = (int) ((pSceneTouchEvent.getX())/32);
 					dropX = tableX*32;
 					tableY = (int) ((pSceneTouchEvent.getY())/32);
 					dropY = tableY*32;
 				
 					switch(id){
 					
 					case GameValues.HUD_ITEM_1:	
 						level.setItem(tableX, tableY, GameValues.ITEM_SOLID);
 						gameScene.attachChild(new Sprite(dropX, dropY, GameActivity.this.levelPack.getTexturePackTextureRegionLibrary().get(GameValues.SOLID_ID)));
 					break;
 					
 					case GameValues.HUD_ITEM_2:
 						
 						level.setItem(tableX, tableY, GameValues.ITEM_SOLID);
 						level.setItem(tableX, tableY+1, GameValues.ITEM_SOLID);
 						gameScene.attachChild(new Sprite(dropX, dropY, GameActivity.this.levelPack.getTexturePackTextureRegionLibrary().get(GameValues.SOLID_ID)));
 						gameScene.attachChild(new Sprite(dropX, dropY+32, GameActivity.this.levelPack.getTexturePackTextureRegionLibrary().get(GameValues.SOLID_ID)));
 					
 					break;
 						
 					case GameValues.HUD_ITEM_2_2:
 						level.setItem(tableX, tableY+1, GameValues.ITEM_SOLID);
 						level.setItem(tableX+1, tableY, GameValues.ITEM_SOLID);
 						gameScene.attachChild(new Sprite(dropX, dropY+32, GameActivity.this.levelPack.getTexturePackTextureRegionLibrary().get(GameValues.SOLID_ID)));
 						gameScene.attachChild(new Sprite(dropX+32, dropY, GameActivity.this.levelPack.getTexturePackTextureRegionLibrary().get(GameValues.SOLID_ID)));
 							
 					break;
 					
 					case GameValues.HUD_ITEM_3:
 						level.setItem(tableX, tableY+1, GameValues.ITEM_SOLID);
 						level.setItem(tableX+1, tableY+1, GameValues.ITEM_SOLID);
 						level.setItem(tableX+1, tableY, GameValues.ITEM_SOLID);
 						gameScene.attachChild(new Sprite(dropX, dropY+32, GameActivity.this.levelPack.getTexturePackTextureRegionLibrary().get(GameValues.SOLID_ID)));
 						gameScene.attachChild(new Sprite(dropX+32, dropY+32, GameActivity.this.levelPack.getTexturePackTextureRegionLibrary().get(GameValues.SOLID_ID)));
 						gameScene.attachChild(new Sprite(dropX+32, dropY, GameActivity.this.levelPack.getTexturePackTextureRegionLibrary().get(GameValues.SOLID_ID)));
 						
 					break;
 					
 					case GameValues.HUD_ITEM_4:
 						level.setItem(tableX, tableY+1, GameValues.ITEM_SOLID);
 						level.setItem(tableX+1, tableY+1, GameValues.ITEM_SOLID);
 						level.setItem(tableX+1, tableY, GameValues.ITEM_SOLID);
 						level.setItem(tableX, tableY, GameValues.ITEM_SOLID);
 						gameScene.attachChild(new Sprite(dropX, dropY+32, GameActivity.this.levelPack.getTexturePackTextureRegionLibrary().get(GameValues.SOLID_ID)));
 						gameScene.attachChild(new Sprite(dropX+32, dropY+32, GameActivity.this.levelPack.getTexturePackTextureRegionLibrary().get(GameValues.SOLID_ID)));
 						gameScene.attachChild(new Sprite(dropX+32, dropY, GameActivity.this.levelPack.getTexturePackTextureRegionLibrary().get(GameValues.SOLID_ID)));
 						gameScene.attachChild(new Sprite(dropX, dropY, GameActivity.this.levelPack.getTexturePackTextureRegionLibrary().get(GameValues.SOLID_ID)));						
 					break;
 						
 					
 					}
 				}
 			}
 		};
 
 		mItemsHUD.show();
 		this.mCamera.setHUD(mItemsHUD);
 		
 		updateTimer = new TimerHandler(0.2f, true, new ITimerCallback(){
 		@Override
 		public void onTimePassed(TimerHandler arg0) {
 		GameActivity.this.gameTime += 0.2f;
 		GameActivity.this.onTimerUpdate();	
 		}		
 		});
 		
 		return gameScene;
 	}
 	
 	
 	protected void start() {
 		Log.i("HUD Item selected: ", "PLAY");
 		mItemsHUD.setType(ItemsHUD.GAME);
 		GameActivity.this.mEngine.registerUpdateHandler(updateTimer);
 	}
 
 
 
 	protected void stop() {
 		Log.i("HUD Item selected: ", "STOP");
 		
 		for(int i = 0; i < level.getNumberOfCubes(); i++){
 		level.getCube(i).moveToInitPosition();
 		}
 		
 		
 		GameActivity.this.mEngine.unregisterUpdateHandler(updateTimer);
 		mItemsHUD.setType(ItemsHUD.ITEMS);
 	}
 
 
 
 	public static void setLevel(Level pLevel){
 		level = pLevel;
 	}
 	
 	private void onTimerUpdate(){
 		
 		checkCollisions();
 		
 		if(cubesFinished == level.getNumberOfCubes()){
 			loadScoreScene();
 		}
 		
 		for(int i = 0; i < level.getNumberOfCubes(); i++){
 		
 		switch(level.getCube(i).getDirection()){
 	
 		case CubeEntity.DIRECTION_FORWARD:
 		
 			level.getCube(i).setPosition(level.getCube(i).getX(), level.getCube(i).getY()-32);
 			
 		break;
 	
 		case CubeEntity.DIRECTION_RIGHT:
 			
 			level.getCube(i).setPosition(level.getCube(i).getX()+32, level.getCube(i).getY());
 			
 		break;
 		
 		case CubeEntity.DIRECTION_LEFT:
 			
 			level.getCube(i).setPosition(level.getCube(i).getX()-32, level.getCube(i).getY());
 			
 		break;
 		
 		case CubeEntity.DIRECTION_BACKWARD:
 			
 			level.getCube(i).setPosition(level.getCube(i).getX(), level.getCube(i).getY()+32);
 			
 		break;
 		
 		}
 		
		checkCollisions();
		
 		}
 	}
 	
 	
 	private void checkCollisions(){
 	
 		for(int i = 0; i < level.getNumberOfCubes(); i++){
 		
 			
 		if(!level.getCube(i).isFinished()){	
 			
 		switch(level.getCube(i).getDirection()){
 		
 		case CubeEntity.DIRECTION_FORWARD:	
 			proceedCollision(level.getItemNumber((int)level.getCube(i).getX()/32, (int)(level.getCube(i).getY()/32)-1), i, CubeEntity.DIRECTION_RIGHT);
 			break;
 			
 		case CubeEntity.DIRECTION_BACKWARD:
 			proceedCollision(level.getItemNumber((int)level.getCube(i).getX()/32, (int)(level.getCube(i).getY()/32)+1), i, CubeEntity.DIRECTION_LEFT);
 			break;
 			
 		case CubeEntity.DIRECTION_LEFT:
 			proceedCollision(level.getItemNumber((int)(level.getCube(i).getX()/32)-1, (int)level.getCube(i).getY()/32), i, CubeEntity.DIRECTION_FORWARD);
 			break;
 			
 		case CubeEntity.DIRECTION_RIGHT:
 			proceedCollision(level.getItemNumber((int)(level.getCube(i).getX()/32)+1, (int)level.getCube(i).getY()/32), i, CubeEntity.DIRECTION_BACKWARD);
 			break;
 		}
 		}
 		}
 		
 	}
 	
 	
 	private void proceedCollision(int id, int cubeId, int nextDirection){
 		
 		switch(id){
 		
 		case GameValues.ITEM_SOLID:
 			level.getCube(cubeId).setDirection(nextDirection);
 			break;
 
 		case GameValues.ITEM_STAR:
 
 			//TODO: FIX - STAR HAS TO DISAPEAR
			mItemsHUD.setStars(stars);
 			stars++;
 			
 			break;			
 		
 		
 		default:
 			break;
 		
 		}	
 		
 		
 		 if(id >= GameValues.ITEM_END_0 &&  id <= GameValues.ITEM_END_6){
 			 if(id-GameValues.ITEM_END_0 == level.getCube(cubeId).getColor()){
 				 
 				 level.removeCube(cubeId, gameScene);
 				 
 				 this.cubesFinished++;
 				 
 				 Log.i("WIN", Integer.toString(id));
 			 }
 			 
 			 
    	  	 }
 		
 		
 	}
 	
 	
 	private void nextLevel(){
 		
 		this.stars = 0;
 		this.cubesFinished = 0;
 		this.gameTime = 0;
 		this.mItemsHUD.show();
 		
 		LevelFileReader lvReader = new LevelFileReader(this, "level_"+Integer.toString(level.getLevelpackId())+"_"+Integer.toString(level.getLevelId()+1));
 		level = lvReader.getLevel();
 		
 		this.mEngine.unregisterUpdateHandler(updateTimer);
 		
 		this.mEngine.setScene(onAssetsLoaded());
 		
 	}
 	
 	
 	private void resetLevel(){
 		
 		this.stars = 0;
 		this.cubesFinished = 0;
 		this.gameTime = 0;
 		this.mItemsHUD.show();
 		
 		LevelFileReader lvReader = new LevelFileReader(this, "level_"+Integer.toString(level.getLevelpackId())+"_"+Integer.toString(level.getLevelId()));
 		level = lvReader.getLevel();
 		
 		this.mEngine.unregisterUpdateHandler(updateTimer);
 		
 		this.mEngine.setScene(onAssetsLoaded());
 		
 	}
 	
 	private void createScoreScene(){
 		
 		scoreScene = new Scene() ;
 		
 		scoreScene.setBackground(new SpriteBackground(new Sprite(0,0,this.levelPack.getTexturePackTextureRegionLibrary().get(LevelSceneFactory.BG_ID))));
 		
 		final AlignedText text = new AlignedText(0, 50, bigFont, "CONGRATULATIONS!!!", HorizontalAlign.CENTER, VerticalAlign.CENTER, 800, 60);
 		
 		scoreScene.attachChild(text);
 		
 		new Button(scoreScene, 150, 300, 250, 75, getString(R.string.nextlevel), buttonTexture, Stroke){
 			@Override
 			public boolean onButtonPressed(){
 				nextLevel();
 				return true;
 			}
 		};
 		new Button(scoreScene, 400, 300, 250, 75, getString(R.string.reset), buttonTexture, Stroke){
 			@Override
 			public boolean onButtonPressed(){
 				resetLevel();
 				return true;
 			}
 		};
 		new Button(scoreScene, 275, 375, 250, 75, getString(R.string.mainmenu), buttonTexture, Stroke){
 			@Override
 			public boolean onButtonPressed(){
 				GameActivity.this.finish();
 				return true;
 			}
 		};
 	
 	}
 	
 	private void loadScoreScene(){
 		this.mEngine.unregisterUpdateHandler(updateTimer);
 		
 		this.mItemsHUD.hide();
 			
 		if(ScoreReader.getStars(level) < stars){
 		ScoreReader.setStars(level, stars);
 		}
 		
 		ScoreReader.addTotalCubes(cubesFinished);
 
 		Log.i("TOTALCUBES", Integer.toString(ScoreReader.getTotalCubes()));
 		
 		checkAchievements();
 		
 		for(int i = 1; i <= stars; i++){
 		final Sprite star = new Sprite(80+(i*128), 175, starFull);	
 		scoreScene.attachChild(star);
 		}
 		
 		for(int i = stars+1; i <= 3; i++){
 		final Sprite star = new Sprite(80+(i*128), 175, starBlank);	
 		scoreScene.attachChild(star);	
 		}
 		
 		scoreScene.attachChild(new AlignedText(0, 0, Stroke, Integer.toString(this.calculateScore()), HorizontalAlign.LEFT, VerticalAlign.CENTER, 800, 40));
 		
 		this.mEngine.setScene(scoreScene);
 	}
 	
 	private void checkAchievements(){
 		
 		ScoreReader.setScore(level, calculateScore());
 		ScoreReader.setAllScores(level, 15);
 		new Score(ScoreReader.getLevelpackScore(level), null).submitTo(new Leaderboard(PrivateValues.OFLeaderboardProject), new Score.SubmitToCB(){
 
 			@Override
 			public void onSuccess(boolean arg0) {
				// TODO Auto-generated method stub
 				
 			}
 		});
 
 		new Achievement(PrivateValues.OFAchievementMasterOfCubes).updateProgression((ScoreReader.getTotalCubes()/PrivateValues.OFAchievementMasterOfCubesValue)*100, new UpdateProgressionCB()
 		{
 			@Override
 			public void onSuccess(boolean arg0) {
 			}	
 		});
 		
 
 		
 		if(level.getLevelId() == 15){
 			
 			new Achievement(PrivateValues.OFAchievementProjectPackFinished).unlock(new UnlockCB(){
 				@Override
 				public void onSuccess(boolean arg0) {
 					
 				}
 			});
 			
 		}
 	
 	}
 	
 	
 	
 	private int calculateScore(){
 		return (int) ((this.stars+1)* 1000 + (1/this.gameTime)*(50000.0f));
 	}
 }
 
 
 
 
 
 
 
 
 
 
 
