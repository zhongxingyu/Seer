 package com.twam.boostrunner.entity;
 
 import android.content.EntityIterator;
 import android.util.Log;
 
 import com.badlogic.gdx.physics.box2d.Body;
 import com.badlogic.gdx.physics.box2d.BodyDef;
 import com.badlogic.gdx.physics.box2d.FixtureDef;
 import com.twam.boostrunner.handler.CharacterUpdateHandler;
 
 import org.andengine.entity.Entity;
 import org.andengine.entity.modifier.EntityModifier;
 import org.andengine.entity.modifier.MoveByModifier;
 import org.andengine.entity.modifier.SequenceEntityModifier;
 import org.andengine.entity.sprite.AnimatedSprite;
 import org.andengine.extension.physics.box2d.PhysicsConnector;
 import org.andengine.extension.physics.box2d.PhysicsFactory;
 import org.andengine.extension.physics.box2d.PhysicsWorld;
 import org.andengine.input.touch.detector.ContinuousHoldDetector;
 import org.andengine.opengl.texture.region.ITiledTextureRegion;
 import org.andengine.opengl.vbo.VertexBufferObjectManager;
 
 /**
  * Created by masseeh on 7/17/13.
  */
 public class Character extends Entity{
 
     //============================================================
     // constants
     //============================================================
 
     public final static int HEAD_TILED_ROW = 1;
     public final static int HEAD_TILED_COL = 4;
 
     public final static int BODY_TILED_ROW = 8;
     public final static int BODY_TILED_COL = 7;
 
     private final float WIDTH = 50;
    private final float HEIGHT = 50;
     private final float FIXED_X = 100;
     private final float FIXED_Y = 200;
     private final float HEAD_X = 20;
     private final float HEAD_Y = 80;
     private final float BODY_X = WIDTH*0.5f;
     private final float BODY_Y = HEIGHT*0.5f;
 
 
     //============================================================
     // fields
     //============================================================
 
     //textures
     private ITiledTextureRegion characterHeadTexture;
     private ITiledTextureRegion characterBodyTexture;
 
     //sprites
     private AnimatedSprite head;
     private AnimatedSprite body;
 
     //physics related fields
     private com.badlogic.gdx.physics.box2d.Body bodyBody;
     private com.badlogic.gdx.physics.box2d.Body headBody;
 
     private boolean canJump;
     private boolean jumping;
     private boolean tackling;
     private boolean landed;
     private Body currentPlatformBody;
 
     //============================================================
     //constructor
     //============================================================
 
     public Character(VertexBufferObjectManager vertexBufferObjectManager, ITiledTextureRegion characterHeadTexture, ITiledTextureRegion characterBodyTexture, PhysicsWorld physicsWorld){
         this.setWidth(WIDTH);
         this.setHeight(HEIGHT);
         this.characterHeadTexture = characterHeadTexture;
         this.characterBodyTexture = characterBodyTexture;
         initSprites(vertexBufferObjectManager);
         initBodies(physicsWorld);
         initUpdateHandler();
         this.setPosition(FIXED_X, FIXED_Y);
         canJump = true;
         landed = false;
         tackling = false;
         jumping = false;
     }
 
 
     //============================================================
     // getters & setters
     //============================================================
 
     public AnimatedSprite getBody() {
         return body;
     }
 
     public AnimatedSprite getHead() {
         return head;
     }
 
     public com.badlogic.gdx.physics.box2d.Body getBodyBody() {
         return bodyBody;
     }
 
     public void setBodyBody(com.badlogic.gdx.physics.box2d.Body bodyBody) {
         this.bodyBody = bodyBody;
     }
 
     public float getFIXED_Y() {
         return FIXED_Y;
     }
 
     public float getFIXED_X() {
         return FIXED_X;
     }
 
     public boolean isCanJump() {
         return canJump;
     }
 
     public void setCanJump(boolean canJump) {
         this.canJump = canJump;
     }
 
     public boolean isJumping() {
         return jumping;
     }
 
     public void setJumping(boolean jumping) {
         this.jumping = jumping;
     }
 
     public Body getCurrentPlatformBody() {
         return currentPlatformBody;
     }
 
     public void setCurrentPlatformBody(Body currentPlatformBody) {
         this.currentPlatformBody = currentPlatformBody;
     }
 
     public boolean isLanded() {
         return landed;
     }
 
     public void setLanded(boolean landed) {
         this.landed = landed;
     }
 
     public boolean isTackling() {
         return tackling;
     }
 
     public void setTackling(boolean tackling) {
         this.tackling = tackling;
     }
 
     //============================================================
     // methods
     //============================================================
 
     /**
      * this method initialize the character sub sprites
      * */
     public void initSprites(VertexBufferObjectManager vertexBufferObjectManager){
 //        head = new AnimatedSprite(HEAD_X, HEAD_Y, characterHeadTexture, vertexBufferObjectManager);
         body = new AnimatedSprite(BODY_X, BODY_Y,WIDTH, HEIGHT, characterBodyTexture, vertexBufferObjectManager);
         this.attachChild(body);
     }
 
     public void initBodies(PhysicsWorld physicsWorld){
         FixtureDef fixtureDef = PhysicsFactory.createFixtureDef(20, 0.5f, 0.5f);
         bodyBody = PhysicsFactory.createBoxBody(physicsWorld, this, BodyDef.BodyType.DynamicBody, fixtureDef);
         bodyBody.setTransform((this.FIXED_X + this.getWidth()/2)/32, (this.FIXED_Y + this.getHeight()/2)/32, 0);
         bodyBody.setFixedRotation(true);
         physicsWorld.registerPhysicsConnector(new PhysicsConnector(this, bodyBody, true, false));
         bodyBody.setUserData("character");
     }
 
     public void initUpdateHandler(){
         this.registerUpdateHandler(new CharacterUpdateHandler(this));
     }
 
     public void animateRun(){
         long[] d = {30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30};
         body.animate(d, 0, 12, true);
     }
 
     /**
      * this method used character jumping
      * */
     public void jump(){
 //        if(!canJump);
 //        else {
 //        this.bodyBody.applyForce(new Vector2(0f, 1000f), bodyBody.getPosition());
 //        this.bodyBody.applyLinearImpulse(new Vector2(0f,1000f), bodyBody.getPosition());
         this.bodyBody.setLinearVelocity(0f, 20f);
         this.body.stopAnimation();
         long[] d = {30, 30, 30, 30, 30, 30, 30, 30, 30};
         this.body.animate(d, 26, 34, false, new AnimatedSprite.IAnimationListener() {
             @Override
             public void onAnimationStarted(AnimatedSprite pAnimatedSprite, int pInitialLoopCount) {
 
             }
 
             @Override
             public void onAnimationFrameChanged(AnimatedSprite pAnimatedSprite, int pOldFrameIndex, int pNewFrameIndex) {
 
             }
 
             @Override
             public void onAnimationLoopFinished(AnimatedSprite pAnimatedSprite, int pRemainingLoopCount, int pInitialLoopCount) {
 
             }
 
             @Override
             public void onAnimationFinished(AnimatedSprite pAnimatedSprite) {
 
             }
         });
 
         setCanJump(false);
         setJumping(true);
         setTackling(false);
         setLanded(false);
     }
 
     /**
      * method for tackling
      * */
     public void tackle(){
         if(!this.isLanded())
             fall();
         this.body.stopAnimation();
         long[] d = {30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30};
         this.setTackling(true);
         this.body.animate(d, 35, 52, false, new AnimatedSprite.IAnimationListener() {
             @Override
             public void onAnimationStarted(AnimatedSprite pAnimatedSprite, int pInitialLoopCount) {
                 Character.this.bodyBody.setTransform(Character.this.bodyBody.getPosition(), (float)Math.toRadians(90));
             }
 
             @Override
             public void onAnimationFrameChanged(AnimatedSprite pAnimatedSprite, int pOldFrameIndex, int pNewFrameIndex) {
 
             }
 
             @Override
             public void onAnimationLoopFinished(AnimatedSprite pAnimatedSprite, int pRemainingLoopCount, int pInitialLoopCount) {
 
             }
 
             @Override
             public void onAnimationFinished(AnimatedSprite pAnimatedSprite) {
                 Character.this.animateRun();
                 Character.this.setTackling(false);
                 Character.this.bodyBody.setTransform(Character.this.bodyBody.getPosition(), 0);
             }
         });
     }
 
     /**
      *in this method character fall from any height to ground
      * */
     public void fall(){
         this.bodyBody.setLinearVelocity(0, -30f);
     }
 
      /**
      * this method used when character want to landing
      * */
     public void land(){
         this.setCanJump(true) ;
         this.setJumping(false);
         this.setLanded(true);
         if(!isTackling()){
             this.animateRun();
         }
         this.bodyBody.setLinearVelocity(0, 0);
     }
 
     public void recycle(){
         this.setIgnoreUpdate(true);
         this.setVisible(false);
 
         //bodyBody.setActive(false);
     }
 
     public void setup(){
         this.bodyBody.setTransform(FIXED_X / 32, 1000 / 32, 0);
         this.setVisible(true);
         this.setIgnoreUpdate(false);
 
     }
 
     public void reset(){
         recycle();
         setup();
     }
 
 }
