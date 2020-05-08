 package com.example.forestgame;
 
 import javax.microedition.khronos.opengles.GL10;
 
 import org.andengine.entity.modifier.AlphaModifier;
 import org.andengine.entity.modifier.ScaleModifier;
 import org.andengine.entity.scene.Scene;
 import org.andengine.entity.scene.background.Background;
 import org.andengine.entity.sprite.Sprite;
 import org.andengine.input.touch.TouchEvent;
 import org.andengine.util.color.Color;
 
 import android.util.Log;
 
 public class MainMenuScene extends Scene {
     
     	Sprite sprite = new Sprite( 0
 	    		          , 0
 	    		          , MainActivity.TEXTURE_WIDTH
 	    		          , MainActivity.TEXTURE_HEIGHT
 	    		          , MainActivity.mainActivity.textureBackground
 	    		          , MainActivity.mainActivity.getVertexBufferObjectManager());
 
     	Sprite Title = new Sprite( MainActivity.TEXTURE_WIDTH / 14
     				 , MainActivity.TEXTURE_HEIGHT / 20
     				 , MainActivity.TEXTURE_WIDTH * 6 / 8
     				 , MainActivity.TEXTURE_HEIGHT / 4
     				 , MainActivity.mainActivity.textureTitle
     				 , MainActivity.mainActivity.getVertexBufferObjectManager());
 
     	Sprite ButtonPlay = new Sprite( MainActivity.TEXTURE_WIDTH / 4
 	    			      , MainActivity.TEXTURE_HEIGHT * 52 / 128
 	    			      , MainActivity.TEXTURE_WIDTH * 2 / 4
 	    			      , MainActivity.TEXTURE_HEIGHT * 12 / 128
 	    			      , MainActivity.mainActivity.texturePlay
 	    			      , MainActivity.mainActivity.getVertexBufferObjectManager()) {
 	    @Override
 	    public boolean onAreaTouched( TouchEvent pSceneTouchEvent
 		    			, float pTouchAreaLocalX
 		    			, float pTouchAreaLocalY) {
 		if (pSceneTouchEvent.isActionDown()) {
 		    Log.d("ButtonPlay", "touch");
 		    this.registerEntityModifier(new ScaleModifier(0.001f, 1.0f, 0.95f));
 		    this.registerEntityModifier(new AlphaModifier(0.001f, 1.0f, 0.5f));
 		} else if (pSceneTouchEvent.isActionUp()) {
 		    Log.d("ButtonPlay", "no touch");
 		    this.registerEntityModifier(new ScaleModifier(0.001f, 0.95f, 1.0f));
 		    this.registerEntityModifier(new AlphaModifier(0.001f, 0.5f, 1.0f));
 		    MainScene.gameScene.slotMatrix.reInit();
		    MainScene.gameScene.prison.clear();
		    MainScene.gameScene.respawn.clear();
		    MainScene.gameScene.respawn.generateElement();
 		    MainScene.showGameScene();
 		}
 		return true;
 	    }
 	};
 	
 	Sprite ButtonScores = new Sprite( MainActivity.TEXTURE_WIDTH / 4
 	    			        , MainActivity.TEXTURE_HEIGHT * 69 / 128
 	    			        , MainActivity.TEXTURE_WIDTH * 2 / 4
 	    			        , MainActivity.TEXTURE_HEIGHT * 12 / 128
 	    			        , MainActivity.mainActivity.textureScores
 	    			        , MainActivity.mainActivity.getVertexBufferObjectManager()) {
 	    @Override
 	    public boolean onAreaTouched( TouchEvent pSceneTouchEvent
 					, float pTouchAreaLocalX
 					, float pTouchAreaLocalY) {
 		if (pSceneTouchEvent.isActionDown()) {
 		    Log.d("ButtonScores", "touch");
 		    this.registerEntityModifier(new ScaleModifier(0.001f, 1.0f, 0.95f));
 		    this.registerEntityModifier(new AlphaModifier(0.001f, 1.0f, 0.5f));
 		} else if (pSceneTouchEvent.isActionUp()) {
 		    Log.d("ButtonScores", "no touch");
 		    this.registerEntityModifier(new ScaleModifier(0.001f, 0.95f, 1.0f));
 		    this.registerEntityModifier(new AlphaModifier(0.001f, 0.5f, 1.0f));
 		    MainScene.showScoresScene();
 		}
 		return true;
 	    }
 	};
 
 	Sprite ButtonCredits = new Sprite( MainActivity.TEXTURE_WIDTH / 4
 				         , MainActivity.TEXTURE_HEIGHT * 86 / 128
 				         , MainActivity.TEXTURE_WIDTH * 2 / 4
 				         , MainActivity.TEXTURE_HEIGHT * 12 / 128
 				         , MainActivity.mainActivity.textureCredits
 				         , MainActivity.mainActivity.getVertexBufferObjectManager()) {
 	    @Override
 	    public boolean onAreaTouched( TouchEvent pSceneTouchEvent
 					, float pTouchAreaLocalX
 					, float pTouchAreaLocalY) {
 		if (pSceneTouchEvent.isActionDown()) {
 		    Log.d("ButtonCredits", "touch");
 		    this.registerEntityModifier(new ScaleModifier(0.001f, 1.0f, 0.95f));
 		    this.registerEntityModifier(new AlphaModifier(0.001f, 1.0f, 0.5f));
 		} else if (pSceneTouchEvent.isActionUp()) {
 		    Log.d("ButtonCredits", "no touch");
 		    this.registerEntityModifier(new ScaleModifier(0.001f, 0.95f, 1.0f));
 		    this.registerEntityModifier(new AlphaModifier(0.001f, 0.5f, 1.0f));
 		    MainScene.showCreditsScene();
 		}
 		return true;
 	    }
 	};
 
 	Sprite ButtonExit = new Sprite( MainActivity.TEXTURE_WIDTH / 4
 				      , MainActivity.TEXTURE_HEIGHT * 103 / 128
 				      , MainActivity.TEXTURE_WIDTH * 2 / 4
 				      , MainActivity.TEXTURE_HEIGHT * 12 / 128
 				      , MainActivity.mainActivity.textureExit
 				      , MainActivity.mainActivity.getVertexBufferObjectManager()) {
 
 	    @Override
 	    public boolean onAreaTouched( TouchEvent pSceneTouchEvent
 					, float pTouchAreaLocalX
 					, float pTouchAreaLocalY) {
 		if (pSceneTouchEvent.isActionDown()) {
 		    Log.d("ButtonExit", "touch");
 		    this.registerEntityModifier(new ScaleModifier(0.001f, 1.0f, 0.95f));
 		    this.registerEntityModifier(new AlphaModifier(0.001f, 1.0f, 0.5f));
 		} else if (pSceneTouchEvent.isActionUp()) {
 		    Log.d("ButtonExit", "no touch");
 		    this.registerEntityModifier(new ScaleModifier(0.001f, 0.95f, 1.0f));
 		    this.registerEntityModifier(new AlphaModifier(0.001f, 0.5f, 1.0f));
 		    MainActivity.mainActivity.finish();
 		    if (MainActivity.mainActivity.isFinishing() == false) {
 			android.os.Process.killProcess(android.os.Process.myPid());
 		    }
 		}
 		return true;
 	    }
 	};
     
     public MainMenuScene() {
 	setBackgroundEnabled(true);
 	setBackground(new Background(new Color(0.1f, 0.1f, 0.0f)));
 	sprite.setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_COLOR);
 	sprite.registerEntityModifier(new AlphaModifier(0.55f, 0.5f, 1.0f));
 	attachChild(sprite);
 	attachChild(Title);
 	attachChild(ButtonPlay);
 	attachChild(ButtonScores);
 	attachChild(ButtonCredits);
 	attachChild(ButtonExit);
 	registerTouchArea(ButtonPlay);
 	registerTouchArea(ButtonScores);
 	registerTouchArea(ButtonCredits);
 	registerTouchArea(ButtonExit);
 	setTouchAreaBindingOnActionDownEnabled(true);
 	setTouchAreaBindingOnActionMoveEnabled(true);	
     }
     
     public void show() {
 	setVisible(true);
 	setIgnoreUpdate(false);
    	sprite.registerEntityModifier(new AlphaModifier(0.55f, 0.5f, 1.0f));	
     }
     
     public void hide() {
    	setVisible(false);
    	sprite.setAlpha(0.5f);
    	setIgnoreUpdate(true);
     }
 }
