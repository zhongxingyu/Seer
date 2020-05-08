 package es.dev.game.layer;
 
 import org.cocos2d.actions.instant.CCCallFunc;
 import org.cocos2d.actions.interval.CCDelayTime;
 import org.cocos2d.actions.interval.CCSequence;
 import org.cocos2d.layers.CCColorLayer;
 import org.cocos2d.layers.CCScene;
 import org.cocos2d.menus.CCMenu;
 import org.cocos2d.menus.CCMenuItem;
 import org.cocos2d.menus.CCMenuItemImage;
 import org.cocos2d.nodes.CCDirector;
 import org.cocos2d.nodes.CCLabel;
 import org.cocos2d.nodes.CCLabel.TextAlignment;
 import org.cocos2d.types.CGPoint;
 import org.cocos2d.types.CGSize;
 import org.cocos2d.types.ccColor3B;
 import org.cocos2d.types.ccColor4B;
 
 import android.view.MotionEvent;
 
 import static es.dev.game.config.Constants.*;
 
 
 
 public class GameOverLayer extends CCColorLayer
 {
     protected CCLabel gameOverLabel;
     protected CCLabel scoreLabel;
     protected CCMenuItemImage endGameButton;
     protected CCMenuItemImage restartGameButton;
     protected CCMenu menu;
  
     public static CCScene scene(String message,int score)
     {
         CCScene scene = CCScene.node();
         GameOverLayer layer = new GameOverLayer(ccColor4B.ccc4(0, 0, 0, 0), message,score);
   
         scene.addChild(layer);
  
         return scene;
     }
  
     public CCLabel getLabel()
     {
         return gameOverLabel;
     }
  
     protected GameOverLayer(ccColor4B color,String message, int score)
     {
         super(color);
  
         this.setIsTouchEnabled(true);
  
         CGSize winSize = CCDirector.sharedDirector().displaySize();
  
         gameOverLabel = CCLabel.makeLabel(message, "DroidSans", 32);
         gameOverLabel.setColor(ccColor3B.ccWHITE);
         gameOverLabel.setPosition(winSize.width / 2.0f, winSize.height / 2.0f);
         addChild(gameOverLabel);
         
     	scoreLabel = CCLabel.makeLabel("Score: "+ score, CGSize.make(100.0f, 25.0f), TextAlignment.CENTER, "DEFAULT", 20.0f);
 		scoreLabel.setPosition(CGPoint.make(720, 450));
 		addChild(scoreLabel);
 		
 		endGameButton = CCMenuItemImage.item("endButtonNormal.png", "endButtonSelected.png", this, "callback");
 		endGameButton.setTag(END_GAME_BUTTON);
		endGameButton.setPosition(CGPoint.ccp(170, 0));
 		restartGameButton = CCMenuItemImage.item("restartButtonNormal.png", "restartButtonSelected.png", this, "callback");
 		restartGameButton.setTag(RESTART_GAME_BUTTON);
		restartGameButton.setPosition(CGPoint.ccp(0, 0));
 		
 		menu = CCMenu.menu(endGameButton, restartGameButton);
		menu.setPosition(CGPoint.ccp((winSize.width / 2)-75, 170));
 		addChild(menu);
         
         //this.runAction(CCSequence.actions(CCDelayTime.action(3.0f), CCCallFunc.action(this, "gameOverDone")));
     }
     
     public void callback(Object sender){
 		System.out.println("callback");
 
     	CCMenuItemImage menuItem = (CCMenuItemImage) sender;
     	if (menuItem.getTag() == END_GAME_BUTTON){
     		CCDirector.sharedDirector().getActivity().finish();
     	} else if (menuItem.getTag() == RESTART_GAME_BUTTON){
         	CCDirector.sharedDirector().replaceScene(GameLayer.scene());
 		}
     
 	}
     /*
     public void gameOverDone()
     {
         //CCDirector.sharedDirector().replaceScene(GameLayer.scene());
     }
  
     @Override
     public boolean ccTouchesEnded(MotionEvent event)
     {
         gameOverDone();
  
         return true;
     }
     */
 }
