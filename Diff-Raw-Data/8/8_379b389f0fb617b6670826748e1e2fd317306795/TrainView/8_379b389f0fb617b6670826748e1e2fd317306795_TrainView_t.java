 package com.mobi.badvibes.view;
 
 import aurelienribon.tweenengine.BaseTween;
 import aurelienribon.tweenengine.Tween;
 import aurelienribon.tweenengine.TweenCallback;
 import aurelienribon.tweenengine.equations.Cubic;
 import aurelienribon.tweenengine.equations.Expo;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.math.Vector2;
 
 import com.mobi.badvibes.BadVibes;
 import com.mobi.badvibes.nimators.TrainAccessor;
 
 public class TrainView
 {
 	public enum TrainState {
 		ARRIVAL, BOARDING, DEPARTURE
 	}
 
 
 	
 	
 	
     /*
      * Metrics
      */
 
     public static int            TrainHeight        = 81;
 
     public static int            TrainDoorWidth     = 48;
     public static int            TrainDoorHeight    = 81;
 
     public static int            TrainLeftSideWidth      = 591;
     public static int            TrainRightSideWidth     = 373;
 
     public static int            TrainInteriorWidth = 96;
 
     public static int			 TrainDoorsOffsetFull = 50;
     
     public static int            SpriteSecondRow    = 81;
     
     public static final float 	 TrainArrivalX = -240;
 
     /*
      * Resources
      */
 
     private static Texture       trainTexture;
 
     private static TextureRegion trainPartA;
     private static TextureRegion trainPartB;
 
     private static TextureRegion trainDoorLeft;
     private static TextureRegion trainDoorRight;
 
     private static TextureRegion trainInterior;
     
     public TrainState currentState;
 
     public static void Initialize()
     {
         trainTexture    = new Texture(Gdx.files.internal("data/game/train.png"));
 
         trainPartA      = new TextureRegion(trainTexture, 0, 0, TrainLeftSideWidth, TrainHeight);
         trainPartB      = new TextureRegion(trainTexture, 0, SpriteSecondRow, TrainRightSideWidth, TrainHeight);
 
         trainDoorLeft   = new TextureRegion(trainTexture, 469, SpriteSecondRow, TrainDoorWidth, TrainDoorHeight);
         trainDoorRight  = new TextureRegion(trainTexture, 517, SpriteSecondRow, TrainDoorWidth, TrainDoorHeight);
 
         trainInterior   = new TextureRegion(trainTexture, 373, SpriteSecondRow, TrainInteriorWidth, TrainHeight);
 
         trainPartA.flip(false, true);
         trainPartB.flip(false, true);
 
         trainDoorLeft.flip(false, true);
         trainDoorRight.flip(false, true);
 
         trainInterior.flip(false, true);
     }
 
     
     public Vector2               Position           = new Vector2();
 
     public float                 TrainDoorOffset    = 0;
     
     public float 				 Timer = 0;
     
     public TrainView()
     {
         Position.x      = - (GameDimension.TrainPartA.x + GameDimension.TrainPartInterior.x + GameDimension.TrainPartB.x);
         TrainDoorOffset = 0;
 
         Timer = 1;
     }
 
     // TODO: place this in Train.java
     
     public void arriveTrain()
     {
         currentState = TrainState.ARRIVAL;
         Tween.to(this, TrainAccessor.TRAIN, 4)
         .target(GameDimension.TrainArrivalX)
         .ease(Cubic.OUT)
         .start(BadVibes.tweenManager)
         .setCallback(new TweenCallback()
         {
             @Override
             public void onEvent(int arg0, BaseTween<?> arg1)
             {
                 if (TweenCallback.COMPLETE == arg0){
                     currentState = TrainState.BOARDING;
                     Tween.to(TrainView.this, TrainAccessor.TRAIN_DOORS, 2).target(GameDimension.TrainDoorsOffsetFull).start(BadVibes.tweenManager);
                 }
             }
         });
     }
     
     public void departTrain()
     {
         Tween
             .to(this, TrainAccessor.TRAIN_DOORS, 2)
             .target(0)
             .start(BadVibes.tweenManager)
             .setCallback(new TweenCallback()
             {
                 @Override
                 public void onEvent(int arg0, BaseTween<?> arg1)
                 {
                     if (TweenCallback.COMPLETE == arg0)
                     {
                     	currentState = TrainState.DEPARTURE;
                         Tween
                             .to(TrainView.this, TrainAccessor.TRAIN, 4)
                             .ease(Expo.IN)
                             .target(1600)
                             .setCallback(new TweenCallback()
                             {            
                                 @Override
                                 public void onEvent(int arg0, BaseTween<?> arg1)
                                 {
                                     if (TweenCallback.COMPLETE == arg0)
                                     {
                                         Position.x = - (GameDimension.TrainPartA.x + GameDimension.TrainPartInterior.x + GameDimension.TrainPartB.x);
                                     }
                                 }
                             })
                         .start(BadVibes.tweenManager);
                     }
                 }
             });
     }
     
     public void renderTrain(SpriteBatch spriteBatch, float delta)
     {
         spriteBatch.begin();
 
         spriteBatch.setColor(1, 1, 1, 1);
 
         spriteBatch.draw(trainPartA,
                          Position.x, Position.y,
                          0.0f, 0.0f,
                          GameDimension.TrainPartA.x, GameDimension.TrainPartA.y,
                          1.0f, 1.0f,
                          0.0f);
         spriteBatch.draw(trainPartB,
                          Position.x + GameDimension.TrainPartA.x + GameDimension.TrainPartDoorLeft.x * 2, Position.y,
                          0.0f, 0.0f,
                          GameDimension.TrainPartB.x, GameDimension.TrainPartB.y,
                          1.0f, 1.0f,
                          0.0f);
 
         spriteBatch.draw(trainInterior,
                          Position.x + GameDimension.TrainPartA.x, Position.y,
                          0.0f, 0.0f,
                          GameDimension.TrainPartInterior.x, GameDimension.TrainPartInterior.y,
                          1.0f, 1.0f,
                          0.0f);
         
         spriteBatch.end();
     }
     
     public void renderDoors(SpriteBatch spriteBatch, float delta)
     {
         spriteBatch.begin();
         spriteBatch.draw(trainDoorLeft,
                          Position.x + GameDimension.TrainPartA.x - TrainDoorOffset, Position.y,
                          0.0f, 0.0f,
                          GameDimension.TrainPartDoorLeft.x, GameDimension.TrainPartDoorLeft.y,
                          1.0f, 1.0f,
                          0.0f);
         spriteBatch.draw(trainDoorRight,
         				 Position.x + GameDimension.TrainPartA.x + TrainDoorOffset + GameDimension.TrainPartDoorLeft.x, Position.y,
                          0.0f, 0.0f,
                          GameDimension.TrainPartDoorRight.x, GameDimension.TrainPartDoorRight.y,
                          1.0f, 1.0f,
                          0.0f);
         
         spriteBatch.end();
     }
 }
