 package com.brekol.scene;
 
 
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.*;
 import com.brekol.manager.SceneManager;
 import com.brekol.model.Player;
 import com.brekol.util.SceneType;
 import org.andengine.engine.camera.hud.HUD;
 import org.andengine.entity.IEntity;
 import org.andengine.entity.modifier.LoopEntityModifier;
 import org.andengine.entity.modifier.ScaleModifier;
 import org.andengine.entity.scene.IOnSceneTouchListener;
 import org.andengine.entity.scene.Scene;
 import org.andengine.entity.scene.background.Background;
 import org.andengine.entity.sprite.Sprite;
 import org.andengine.entity.text.Text;
 import org.andengine.entity.text.TextOptions;
 import org.andengine.extension.physics.box2d.FixedStepPhysicsWorld;
 import org.andengine.extension.physics.box2d.PhysicsConnector;
 import org.andengine.extension.physics.box2d.PhysicsFactory;
 import org.andengine.extension.physics.box2d.PhysicsWorld;
 import org.andengine.input.touch.TouchEvent;
 import org.andengine.util.SAXUtils;
 import org.andengine.util.adt.align.HorizontalAlign;
 import org.andengine.util.adt.color.Color;
 import org.andengine.util.level.EntityLoader;
 import org.andengine.util.level.constants.LevelConstants;
 import org.andengine.util.level.simple.SimpleLevelEntityLoaderData;
 import org.andengine.util.level.simple.SimpleLevelLoader;
 import org.xml.sax.Attributes;
 
 import java.io.IOException;
 
 
 /**
  * User: Breku
  * Date: 01.07.13
  */
 public class GameScene extends BaseScene implements IOnSceneTouchListener {
 
     private HUD gameHUD;
     private Text scoreText;
     private PhysicsWorld physicsWorld;
     private int score = 0;
 
     private static final String TAG_ENTITY = "entity";
     private static final String TAG_ENTITY_ATTRIBUTE_X = "x";
     private static final String TAG_ENTITY_ATTRIBUTE_Y = "y";
     private static final String TAG_ENTITY_ATTRIBUTE_TYPE = "type";
 
     private static final Object TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_PLATFORM1 = "platform1";
     private static final Object TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_PLATFORM2 = "platform2";
     private static final Object TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_PLATFORM3 = "platform3";
     private static final Object TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_COIN = "coin";
 
     private static final Object TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_PLAYER = "player";
     private Player player;
     private boolean firstTouch = false;
     private Text gameOverText;
     private boolean gameOverDisplayed = false;
 
     @Override
     public void createScene() {
         createBackground();
         createHUD();
         createPhysics();
         loadLevel(1);
         createGameOverText();
         setOnSceneTouchListener(this);
     }
 
     @Override
     public void onBackKeyPressed() {
         SceneManager.getInstance().loadMenuScene();
     }
 
     @Override
     public SceneType getSceneType() {
         return SceneType.GAME;
     }
 
     @Override
     public void disposeScene() {
         camera.setHUD(null);
         camera.setCenter(400, 240);
         camera.setChaseEntity(null);
     }
 
     private void createBackground() {
         setBackground(new Background(Color.BLUE));
     }
 
     private void createHUD() {
         gameHUD = new HUD();
 
         scoreText = new Text(120, 20, resourcesManager.getMediumFont(), "Score: 0123456789", new TextOptions(HorizontalAlign.LEFT), vertexBufferObjectManager);
         scoreText.setText("Score: 0");
         gameHUD.attachChild(scoreText);
 
         camera.setHUD(gameHUD);
     }
 
     private void addToScore(int i) {
         score += i;
         scoreText.setText("Score: " + score);
     }
 
 
     private void createPhysics() {
         physicsWorld = new FixedStepPhysicsWorld(60, new Vector2(0, -17), false);
         physicsWorld.setContactListener(contactListener());
         registerUpdateHandler(physicsWorld);
     }
 
     private void loadLevel(int levelID) {
         final SimpleLevelLoader levelLoader = new SimpleLevelLoader(vertexBufferObjectManager);
         final FixtureDef FIXTURE_DEF = PhysicsFactory.createFixtureDef(0, 0.01f, 0.5f);
 
         levelLoader.registerEntityLoader(new EntityLoader<SimpleLevelEntityLoaderData>(LevelConstants.TAG_LEVEL) {
             @Override
             public IEntity onLoadEntity(String pEntityName, IEntity pParent, Attributes pAttributes, SimpleLevelEntityLoaderData pEntityLoaderData) throws IOException {
                 final int width = SAXUtils.getIntAttributeOrThrow(pAttributes, LevelConstants.TAG_LEVEL_ATTRIBUTE_WIDTH);
                 final int height = SAXUtils.getIntAttributeOrThrow(pAttributes, LevelConstants.TAG_LEVEL_ATTRIBUTE_HEIGHT);
 
                 camera.setBounds(0, 0, width, height);
                 camera.setBoundsEnabled(true);
 
                 return GameScene.this;
             }
         });
         levelLoader.registerEntityLoader(new EntityLoader<SimpleLevelEntityLoaderData>(TAG_ENTITY) {
             @Override
             public IEntity onLoadEntity(String pEntityName, IEntity pParent, Attributes pAttributes, SimpleLevelEntityLoaderData pEntityLoaderData) throws IOException {
 
                 final int x = SAXUtils.getIntAttributeOrThrow(pAttributes, TAG_ENTITY_ATTRIBUTE_X);
                 final int y = SAXUtils.getIntAttributeOrThrow(pAttributes, TAG_ENTITY_ATTRIBUTE_Y);
                 final String type = SAXUtils.getAttributeOrThrow(pAttributes, TAG_ENTITY_ATTRIBUTE_TYPE);
 
                 final Sprite levelObject;
 
                 if (type.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_PLATFORM1)) {
                     levelObject = new Sprite(x, y, resourcesManager.getPlatform1Region(), vertexBufferObjectManager);
                     PhysicsFactory.createBoxBody(physicsWorld, levelObject, BodyDef.BodyType.StaticBody, FIXTURE_DEF).setUserData("platform1");
                 } else if (type.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_PLATFORM2)) {
                     levelObject = new Sprite(x, y, resourcesManager.getPlatform2Region(), vertexBufferObjectManager);
                     final Body body = PhysicsFactory.createBoxBody(physicsWorld, levelObject, BodyDef.BodyType.StaticBody, FIXTURE_DEF);
                     body.setUserData("platform2");
                     physicsWorld.registerPhysicsConnector(new PhysicsConnector(levelObject, body, true, false));
                 } else if (type.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_PLATFORM3)) {
                     levelObject = new Sprite(x, y, resourcesManager.getPlatform3Region(), vertexBufferObjectManager);
                     final Body body = PhysicsFactory.createBoxBody(physicsWorld, levelObject, BodyDef.BodyType.StaticBody, FIXTURE_DEF);
                     body.setUserData("platform3");
                     physicsWorld.registerPhysicsConnector(new PhysicsConnector(levelObject, body, true, false));
                 } else if (type.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_COIN)) {
                     levelObject = new Sprite(x, y, resourcesManager.getCoinRegion(), vertexBufferObjectManager) {
                         @Override
                         protected void onManagedUpdate(float pSecondsElapsed) {
                             super.onManagedUpdate(pSecondsElapsed);
 
                             if (player.collidesWith(this)) {
                                 addToScore(10);
                                 this.setVisible(false);
                                 setIgnoreUpdate(true);
                             }
                         }
                     };
 
                     levelObject.registerEntityModifier(new LoopEntityModifier(new ScaleModifier(1, 1, 1.3f)));
                 } else if (type.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_PLAYER)) {
 
                     player = new Player(x, y, vertexBufferObjectManager, camera, physicsWorld) {
                         @Override
                         public void onDie() {
                             if (!gameOverDisplayed) {
                                 displayGameOverText();
                             }
                         }
                     };
                     levelObject = player;
 
                 } else {
                     throw new IllegalArgumentException();
                 }
 
                 levelObject.setCullingEnabled(true);
 
                 return levelObject;
 
             }
         });
 
         levelLoader.loadLevelFromAsset(activity.getAssets(), "level/" + levelID + ".lvl");
 
     }
 
     @Override
     public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent) {
         if (pSceneTouchEvent.isActionDown()) {
             if (!firstTouch) {
                 player.setRunning();
                 firstTouch = true;
             } else {
                 player.jump();
             }
         }
         return false;
     }
 
     private void createGameOverText() {
         gameOverText = new Text(0, 0, resourcesManager.getMediumFont(), "Game Over!", vertexBufferObjectManager);
     }
 
     private void displayGameOverText() {
         camera.setChaseEntity(null);
         gameOverText.setPosition(camera.getCenterX(), camera.getCenterY());
         attachChild(gameOverText);
         gameOverDisplayed = true;
     }
 
     private ContactListener contactListener() {
         ContactListener contactListener = new ContactListener() {
             @Override
             public void beginContact(Contact contact) {
                 final Fixture x1 = contact.getFixtureA();
                 final Fixture x2 = contact.getFixtureB();
 
                 if (x1.getBody().getUserData() != null && x2.getBody().getUserData() != null) {
                     if (x2.getBody().getUserData().equals("player")) {
                         player.increaseFootContacts();
                     }
                 }
             }
 
             @Override
             public void endContact(Contact contact) {
                 final Fixture x1 = contact.getFixtureA();
                 final Fixture x2 = contact.getFixtureB();
 
                 if (x1.getBody().getUserData() != null && x2.getBody().getUserData() != null) {
                     if (x2.getBody().getUserData().equals("player")) {
                         player.decreaseFootContacts();
                     }
                 }
             }
 
             @Override
             public void preSolve(Contact contact, Manifold oldManifold) {
                 //To change body of implemented methods use File | Settings | File Templates.
             }
 
             @Override
             public void postSolve(Contact contact, ContactImpulse impulse) {
                 //To change body of implemented methods use File | Settings | File Templates.
             }
         };
         return contactListener;
     }
 
 }
