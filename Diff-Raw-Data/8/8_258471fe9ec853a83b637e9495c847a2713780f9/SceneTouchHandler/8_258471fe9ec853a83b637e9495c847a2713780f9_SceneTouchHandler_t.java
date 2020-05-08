 package com.twam.boostrunner.handler;
 
 import android.util.Log;
 
 import com.badlogic.gdx.physics.box2d.BodyDef;
 import com.twam.boostrunner.entity.MainScene;
 import com.twam.boostrunner.manager.GameManager;
 
 import org.andengine.entity.scene.IOnSceneTouchListener;
 import org.andengine.entity.scene.Scene;
 import org.andengine.input.touch.TouchEvent;
 
 /**
  * Created by parsia on 7/24/13.
  */
 public class SceneTouchHandler implements IOnSceneTouchListener{
     //============================================================
     // fields
     //============================================================
     private MainScene mainScene;
     private float lastY;
     private boolean swiped = false;
 
     //============================================================
     // constructor
     //============================================================
 
     public SceneTouchHandler(MainScene mainScene) {
         this.mainScene = mainScene;
         this.mainScene.setOnSceneTouchListener(this);
     }
 
     //============================================================
     // overridden methods
     //============================================================
 
     @Override
     public boolean onSceneTouchEvent(Scene scene, TouchEvent event) {
         mainScene.getHoldTouchHandler().getContinuousHoldDetector().onManagedTouchEvent(event);
         if(event.isActionDown()){
             lastY = event.getY();
             swiped = true;
         }
         if(event.isActionMove()){
             if(swiped){
                 if(event.getY() - lastY > 10 ){
                     if(mainScene.getCharacter().isCanJump()){
                         mainScene.getCharacter().jump();
                     }
                     else if( mainScene.getCharacter().getBodyBody().getLinearVelocity().y < 0 ){
                         InputManager.getInstance().setBuffer(true);
                     }
                     swiped = false;
                 }
                 else if(event.getY() - lastY < -10){
                     mainScene.getCharacter().tackle();
                     swiped = false;
                 }
             }
         }
 
         if(event.isActionUp()){
             swiped = false;
         }
         return true;
     }
 }
