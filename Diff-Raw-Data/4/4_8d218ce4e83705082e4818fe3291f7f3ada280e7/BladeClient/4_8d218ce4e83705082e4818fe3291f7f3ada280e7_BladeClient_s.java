 /*
  * The Init terrain and init materials functions were both taken from the JME example code
  * and modified. The rest of the code is almost entirely written from scratch.
  */ 
 
 /*
  * Copyright (c) 2009-2010 jMonkeyEngine
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are
  * met:
  *
  * * Redistributions of source code must retain the above copyright
  *   notice, this list of conditions and the following disclaimer.
  *
  * * Redistributions in binary form must reproduce the above copyright
  *   notice, this list of conditions and the following disclaimer in the
  *   documentation and/or other materials provided with the distribution.
  *
  * * Neither the name of 'jMonkeyEngine' nor the names of its contributors
  *   may be used to endorse or promote products derived from this software
  *   without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
  * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
  * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
  * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
  * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
  * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
  * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
  * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package mygame;
 
 import com.jme3.animation.AnimChannel;
 import com.jme3.animation.AnimControl;
 import com.jme3.animation.AnimEventListener;
 import com.jme3.animation.Bone;
 import mygame.messages.InputMessages;
 import com.jme3.audio.AudioNode;
 import com.jme3.bounding.BoundingVolume;
 import com.jme3.bullet.BulletAppState;
 import com.jme3.bullet.control.CharacterControl;
 import com.jme3.input.KeyInput;
 import com.jme3.input.MouseInput;
 import com.jme3.input.RawInputListener;
 import com.jme3.input.event.JoyAxisEvent;
 import com.jme3.input.event.JoyButtonEvent;
 import com.jme3.input.event.KeyInputEvent;
 import com.jme3.input.event.MouseButtonEvent;
 import com.jme3.input.event.MouseMotionEvent;
 import com.jme3.material.Material;
 import com.jme3.math.FastMath;
 import com.jme3.math.Vector3f;
 import com.jme3.network.connection.Client;
 import com.jme3.network.events.ConnectionListener;
 import com.jme3.network.events.MessageListener;
 import com.jme3.network.message.Message;
 import com.jme3.scene.Geometry;
 import com.jme3.scene.Node;
 import java.io.IOException;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.Iterator;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import mygame.messages.CharCreationMessage;
 import mygame.messages.CharDestructionMessage;
 import com.jme3.bullet.collision.shapes.BoxCollisionShape;
 import com.jme3.bullet.collision.shapes.CompoundCollisionShape;
 import com.jme3.bullet.control.RigidBodyControl;
 import com.jme3.bullet.util.CollisionShapeFactory;
 import com.jme3.light.DirectionalLight;
 import com.jme3.material.RenderState.BlendMode;
 import com.jme3.math.ColorRGBA;
 import com.jme3.math.Matrix3f;
 import com.jme3.math.Quaternion;
 import com.jme3.niftygui.NiftyJmeDisplay;
 import com.jme3.post.FilterPostProcessor;
 import com.jme3.renderer.queue.RenderQueue.Bucket;
 import com.jme3.scene.debug.SkeletonDebugger;
 import com.jme3.system.AppSettings;
 import com.jme3.terrain.geomipmap.TerrainQuad;
 import com.jme3.water.WaterFilter;
 import de.lessvoid.nifty.Nifty;
 import java.util.Map;
 import java.util.concurrent.Callable;
 import java.util.concurrent.Future;
 import mygame.messages.CharStatusMessage;
 import mygame.messages.ClientReadyMessage;
 import mygame.messages.SwordBodyCollisionMessage;
 import mygame.messages.SwordSwordCollisionMessage;
 import mygame.ui.LifeDisplay;
 import mygame.ui.LoginScreen;
 import mygame.util.IOLib;
 
 /**
  *
  * @author blah
  */
 
 
 public class BladeClient extends BladeBase implements MessageListener, RawInputListener, ConnectionListener, AnimEventListener {
     ConcurrentHashMap<Long, AnimChannel> animChannelMap = new ConcurrentHashMap();
     ConcurrentHashMap<Long, Float> charLifeMap = new ConcurrentHashMap();
     ConcurrentHashMap<Long, LifeDisplay> lifeDisplayMap = new ConcurrentHashMap();
     
     private final boolean debug = false;
     //WATER VARIABLES
     private Vector3f lightDir = new Vector3f(-4.9236743f, -1.27054665f, 5.896916f);
     private float time = 0.0f;
     private float waterHeight = -10.0f;
     private float initialWaterHeight = -9.2f;
     
     private Node sceneNodes;
     CompoundCollisionShape collisionShape;
     BoundingVolume ballBound;
     Geometry block;
     BoxCollisionShape leftShoulder;
     static BladeClient app;
     boolean readyToStart=false;
     boolean started=false;
     Nifty ui;
 
     Client client;
     boolean clientSet = false;
     private long playerID = 0;
 
     public static void main(String[] args) {
         app = new BladeClient();
         AppSettings appSettings=new AppSettings(true);
         appSettings.setFrameRate(30);
   //      app.setPauseOnLostFocus(false);
         app.setSettings(appSettings);
         app.start();
     }
     
     public AppSettings getSettings(){
         return this.settings;
     }
 
     public void isReadyToStart(){
         System.out.println("Ready to start called");
         readyToStart=true;
     }
     
     public void setClient(Client client){
         this.client=client;
     }
     
     
     
     @Override
     public void simpleInitApp() {
         super.simpleInitApp();
         
         Map<String,String> ipAddressMap=IOLib.getIpAddressMap();
 
         NiftyJmeDisplay niftyDisplay = new NiftyJmeDisplay(assetManager,inputManager,audioRenderer,guiViewPort);
         ui=niftyDisplay.getNifty();
         ui.fromXml("Interface/UI.xml","start",new LoginScreen(ipAddressMap,client,BladeMain.port,this));
         
         guiViewPort.addProcessor(niftyDisplay);
         flyCam.setDragToRotate(true);
         app.setDisplayStatView(false);
         sceneNodes = new Node("Scene");
         rootNode.attachChild(sceneNodes);
         
         flyCam.setMoveSpeed(50);
         DirectionalLight sun = new DirectionalLight();
         sun.setDirection(lightDir);
         sun.setColor(ColorRGBA.White.clone().multLocal(1.7f));
         sceneNodes.addLight(sun);
         
         flyCam.setEnabled(false);
         
         if (debug) {
             bulletAppState.getPhysicsSpace().enableDebug(this.getAssetManager());
         }
         //music.setStatus(AudioNode.Status.Playing);
     }
     private boolean mouseCurrentlyStopped = true;
  
     @Override
     public void simpleUpdate(float tpf) {
         //music.setStatus(AudioNode.Status.Playing);
         super.simpleUpdate(tpf);
         time += tpf;
         waterHeight = (float) Math.cos(((time * 0.6f) % FastMath.TWO_PI)) * 1.5f;
         water.setWaterHeight(initialWaterHeight + waterHeight);
 
         
         if (readyToStart && !started){
             System.out.println("starting");
             if(client==null){
                 System.out.println("Client is null");
             }
             client.addConnectionListener(this);
             InputMessages.addInputMessageListeners(client, this);
             client.addMessageListener(this, SwordSwordCollisionMessage.class,SwordBodyCollisionMessage.class,CharCreationMessage.class, CharDestructionMessage.class, CharStatusMessage.class,ClientReadyMessage.class);
            
             try {
                 client.send(new ClientReadyMessage());
                 System.err.println("Sent ClientReadyMessage");
             } catch (IOException ex) {
                 Logger.getLogger(BladeClient.class.getName()).log(Level.SEVERE, null, ex);
             }
             app.setDisplayStatView(true);
             started=true;
             System.out.println("started");            
         }
 
         if (clientSet) {
             updateCharacters(tpf);
             if ((System.currentTimeMillis() - timeOfLastMouseMotion) > mouseMovementTimeout && !mouseCurrentlyStopped) {
                 try {
 
                     client.send(new InputMessages.StopMouseMovement(playerID));
                 } catch (IOException ex) {
                     Logger.getLogger(BladeClient.class.getName()).log(Level.SEVERE, null, ex);
                 }
                 currentMouseEvents = 0;
                 timeOfLastMouseMotion = System.currentTimeMillis();
                 mouseCurrentlyStopped = true;
             }
         }
     }
     
     public void updateCharacters(float tpf) {
         for (Iterator<Long> playerIterator = playerSet.iterator(); playerIterator.hasNext();) {
             long nextPlayerID = playerIterator.next();
             
             Character character=charMap.get(nextPlayerID);
             character.update(tpf,false);
 
             if(nextPlayerID==playerID){
                 cam.setDirection(character.charControl.getViewDirection());
                 cam.setLocation(character.bodyModel.getLocalTranslation().add(new Vector3f(0,4,0)).subtract(character.charControl.getViewDirection().mult(8)));
             }
 
             if (lifeDisplayMap.get(nextPlayerID) != null) {
                 lifeDisplayMap.get(nextPlayerID).setLifeDisplayValue(charLifeMap.get(nextPlayerID));
                 if(playerID!=nextPlayerID){
                     lifeDisplayMap.get(nextPlayerID).setLocalTranslation(character.bodyModel.getLocalTranslation());
                     lifeDisplayMap.get(nextPlayerID).lookAt(charMap.get(playerID).bodyModel.getLocalTranslation().subtract(character.charControl.getViewDirection()).mult(1), new Vector3f(0,1,0));
                } 
             }
 
             // first, get rotation and position of hand
             Bone hand = character.bodyModel.getControl(AnimControl.class).getSkeleton().getBone("swordHand");
             Matrix3f rotation = hand.getModelSpaceRotation().toRotationMatrix();
             Vector3f position = hand.getModelSpacePosition();
 
             // set the position of the sword to the position of the hand
             Node swordNode = character.swordModel;
             Bone swordBone = swordNode.getControl(AnimControl.class).getSkeleton().getBone("swordBone");
             swordNode.setLocalRotation(rotation);
             swordNode.setLocalTranslation(position);
 
             if (debug) {
                 // Adjust the sword collision shape in accordance with arm movement.
                 // adjust for difference in rotation
                 Quaternion swordRot = swordBone.getModelSpaceRotation();
                 Quaternion adjust = (new Quaternion()).fromAngles(FastMath.HALF_PI, 0, 0);
                 Matrix3f swordRotMat = swordRot.mult(adjust).toRotationMatrix();
                 
                 // adjust for difference in position of wrist and middle of sword
                 Vector3f shiftPosition = swordRot.mult(new Vector3f(0f, 1.8f, 0f));
                 
                 // build new collision shape
                 CompoundCollisionShape cShape = new CompoundCollisionShape();
                 Vector3f boxSize = new Vector3f(.1f, .1f, 2.25f);
                 cShape.addChildShape(new BoxCollisionShape(boxSize), Vector3f.ZERO, swordRotMat);
                 CollisionShapeFactory.shiftCompoundShapeContents(cShape, shiftPosition);
                 
                 // remove GhostControl from PhysicsSpace, apply change, put in PhysicsSpace
                 SwordControl sword = character.swordControl;
                 bulletAppState.getPhysicsSpace().remove(sword);
                 sword.setCollisionShape(cShape);
                 bulletAppState.getPhysicsSpace().add(sword);
             }
         }
     }
 
     public void registerInput() {
         inputManager.addRawInputListener(this);
         inputManager.setCursorVisible(false);
     }
 
     
 
     public void messageReceived(Message message) {
         if (message instanceof CharDestructionMessage){
             CharDestructionMessage destroMessage=(CharDestructionMessage)message;
             final long destroyedPlayerID=destroMessage.playerID;
             System.out.println("my id is "+playerID+", and destroID is "+destroyedPlayerID);
             playerSet.remove(destroyedPlayerID);
             final Node destroyedModel=charMap.get(destroyedPlayerID).bodyModel;
             bulletAppState.getPhysicsSpace().remove(destroyedModel.getChild("sword").getControl(SwordControl.class));
             bulletAppState.getPhysicsSpace().remove(destroyedModel.getControl(BodyControl.class));
             bulletAppState.getPhysicsSpace().remove(destroyedModel.getControl(CharacterControl.class));

             Future action = app.enqueue(new Callable() {
 
                 public Object call() throws Exception {
                     rootNode.detachChild(destroyedModel);
                     charMap.remove(destroyedPlayerID);
                     return null;
                 }
             });
             //to retrieve return value (waits for call to finish, fire&forget otherwise):
             //action.get();
         }
         else if (message instanceof CharCreationMessage) {
             System.out.println("Creating character");
             CharCreationMessage creationMessage = (CharCreationMessage) message;
             final long newPlayerID = creationMessage.playerID;
             Character character=new Character(newPlayerID,bulletAppState,assetManager);
             final Node newModel=character.bodyModel;//= Character.createCharacter("Models/Female.mesh.j3o", "Models/sword.mesh.j3o", assetManager, bulletAppState, true, newPlayerID);
             
             if (debug) {
                 AnimControl control = newModel.getControl(AnimControl.class);
                 SkeletonDebugger skeletonDebug = new SkeletonDebugger("skeleton", control.getSkeleton());
                 Material mat2 = new Material(assetManager, "Common/MatDefs/Misc/WireColor.j3md");
                 mat2.setColor("Color", ColorRGBA.Green);
                 mat2.getAdditionalRenderState().setDepthTest(false);
                 skeletonDebug.setMaterial(mat2);
                 newModel.attachChild(skeletonDebug);
                 
                 Node newSword = character.swordModel;//(Node)newModel.getChild("sword");
                 
                 AnimControl control1 = newSword.getControl(AnimControl.class);
                 SkeletonDebugger skeletonDebug1 = new SkeletonDebugger("skeleton1", control1.getSkeleton());
                 Material mat21 = new Material(assetManager, "Common/MatDefs/Misc/WireColor.j3md");
                 mat21.setColor("Color", ColorRGBA.Green);
                 mat21.getAdditionalRenderState().setDepthTest(false);
                 skeletonDebug1.setMaterial(mat21);
                 newSword.attachChild(skeletonDebug1);
             }
 
             
            
             if (creationMessage.controllable) {
                 playerID = newPlayerID;
                 System.out.println("claiming player id " + playerID);
                 
                 /**** Testing transparency of model ****/
                 // Lower is more transparent
                 float alpha = 0.1f;
 
                 Material mat = new Material(assetManager, "Common/MatDefs/Misc/SolidColor.j3md");
                 mat.setColor("m_Color", new ColorRGBA(153, 153, 153, alpha));
                 mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
                 newModel.setMaterial(mat);
                 newModel.setQueueBucket(Bucket.Transparent);
 
                 Node sword = (Node) newModel.getChild("sword");
                 Material mat1 = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
                 sword.setMaterial(mat1);
 
                 /******* End Transparency test *******/
 /*
                 chaseCam = new ChaseCamera(cam, model, inputManager);
                 chaseCam.setSmoothMotion(true);
                 chaseCam.setDefaultVerticalRotation(FastMath.HALF_PI / 4f);
                 chaseCam.setLookAtOffset(new Vector3f(0.0f, 4.0f, 0.0f));
  */
                 registerInput();
                 
                 clientSet = true;
             }
             
             playerSet.add(newPlayerID);
             animChannelMap.put(newPlayerID, character.bodyModel.getControl(AnimControl.class).createChannel());
             animChannelMap.get(newPlayerID).setAnim("stand");
             charLifeMap.put(newPlayerID, 1f);
             charMap.put(newPlayerID, character); 
             
             final boolean controllable=creationMessage.controllable;
             Future action = app.enqueue(new Callable() {
 
                 public Object call() throws Exception {
                     LifeDisplay lifeDisplay;
                     if (controllable) {
                         lifeDisplay = new LifeDisplay(assetManager.loadFont("Interface/Fonts/Default.fnt"), true);
                         lifeDisplayMap.put(newPlayerID, lifeDisplay);
                         lifeDisplay.setLocalTranslation(0,app.getSettings().getHeight(),0);
                         guiNode.attachChild(lifeDisplay);
                     }
                     else{
                         lifeDisplay = new LifeDisplay(assetManager.loadFont("Interface/Fonts/Default.fnt"), false);
                         lifeDisplayMap.put(newPlayerID, lifeDisplay);
                         rootNode.attachChild(lifeDisplay);
                     }
                     rootNode.attachChild(newModel);
                     return null;
                 }
             });
             //to retrieve return value (waits for call to finish, fire&forget otherwise):
             //action.get();
             
         } else if (message instanceof CharStatusMessage) {
             if (clientSet) {
                 
                 CharStatusMessage charStatus = (CharStatusMessage) message;
                 long messagePlayerID = charStatus.playerID;
                 Character character=charMap.get(messagePlayerID);
  
                 character.upperArmAngles=charStatus.upperArmAngles.clone();
                 character.upperArmVels=charStatus.upperArmVels.clone();
                 character.elbowWristAngle=charStatus.elbowWristAngle;
                 character.elbowWristVel=charStatus.elbowWristVel;
                 character.position=charStatus.charPosition.clone();
                 character.velocity=charStatus.charVelocity.clone();
                 character.charAngle=charStatus.charAngle;
                 character.turnVel=charStatus.charTurnVel;
                 if (animChannelMap.get(messagePlayerID) != null) {
                     if (character.velocity.z < 0 ){
            //             System.out.println("back");
                         if (!(animChannelMap.get(messagePlayerID).getAnimationName().equals("backWalk"))) 
                             animChannelMap.get(messagePlayerID).setAnim("backWalk");
                     }else if(character.velocity.z > 0){
            //             System.out.println("forward");
                         if (!(animChannelMap.get(messagePlayerID).getAnimationName().equals("walk"))) 
                             animChannelMap.get(messagePlayerID).setAnim("walk");
                     }else if(character.velocity.x < 0){
             //            System.out.println("right");
                         if (!(animChannelMap.get(messagePlayerID).getAnimationName().equals("sideR"))) 
                             animChannelMap.get(messagePlayerID).setAnim("sideR");
                     }else if(character.velocity.x > 0){
             //            System.out.println("left");
                         if (!(animChannelMap.get(messagePlayerID).getAnimationName().equals("sideL"))) 
                             animChannelMap.get(messagePlayerID).setAnim("sideL");
                     }else if(character.turnVel < 0){
             //            System.out.println("rotateR");
                         if (!(animChannelMap.get(messagePlayerID).getAnimationName().equals("rotateR"))) 
                             animChannelMap.get(messagePlayerID).setAnim("rotateR");
                     }else if(character.turnVel > 0){
             //            System.out.println("rotateL");
                         if (!(animChannelMap.get(messagePlayerID).getAnimationName().equals("rotateL"))) 
                             animChannelMap.get(messagePlayerID).setAnim("rotateL");
                     }else{
              //           System.out.println("stand");
                         if (!(animChannelMap.get(messagePlayerID).getAnimationName().equals("stand"))) 
                             animChannelMap.get(messagePlayerID).setAnim("stand");
                     } //animation
 
                 }
                 
                 charLifeMap.put(messagePlayerID,charStatus.life);
             }
         } else if (message instanceof SwordSwordCollisionMessage){
             SwordSwordCollisionMessage collisionMessage=(SwordSwordCollisionMessage)message;
             System.out.println("Received sword-sword collision at "+collisionMessage.coordinates);
             createEffect(collisionMessage.coordinates,clankMat);
 
         } else if (message instanceof SwordBodyCollisionMessage) {
             SwordBodyCollisionMessage collisionMessage = (SwordBodyCollisionMessage) message;
             System.out.println("Received sword-body collision" + collisionMessage.coordinates);
             
             createEffect(collisionMessage.coordinates,bloodMat);
         }
     }
 
     public void messageSent(Message message) {
     }
 
     public void objectReceived(Object object) {
     }
 
     public void objectSent(Object object) {
     }
 
     public void beginInput() {
     }
 
     public void endInput() {
     }
 
     public void onJoyAxisEvent(JoyAxisEvent evt) {
     }
 
     public void onJoyButtonEvent(JoyButtonEvent evt) {
     }
     private final int eventsPerPacket = 3; // how many events should happen before next packet is sent
     private final long mouseMovementTimeout = 20;// 100; // how long until we propose to send a StopMouseMovement message
     private long timeOfLastMouseMotion = 0; // how long since last movement
     private int currentMouseEvents = 0;
     private int currentDX = 0;
     private int currentDY = 0;
     private int prevDeltaWheel = 0;
 
     public void onMouseMotionEvent(MouseMotionEvent evt) {
 
         float dy = evt.getDY(), dx = evt.getDX();
         if (dy != 0 || dx != 0) {
             currentMouseEvents++;
             currentDX += dx;
             currentDY += dy;
 
             if (currentMouseEvents >= eventsPerPacket) {
                 try {
                     float angle = FastMath.atan2(currentDY, currentDX);
                     if (angle < 0) {
                         angle = FastMath.TWO_PI + angle;
                     }
                     client.send(new InputMessages.MouseMovement(angle, playerID));
                 } catch (IOException ex) {
                     Logger.getLogger(BladeClient.class.getName()).log(Level.SEVERE, null, ex);
                 }
 
                 currentMouseEvents = 0;
                 currentDX = 0;
                 currentDY = 0;
             }
 
             timeOfLastMouseMotion = System.currentTimeMillis();
             mouseCurrentlyStopped = false;
         }
 
         try {
             if (evt.getDeltaWheel() > 0) {
                 if (prevDeltaWheel < 0 && !(charMap.get(playerID).elbowWristAngle == CharMovement.Constraints.lRotMax)) {
                     client.send(new InputMessages.StopLArm(playerID));
                 } else {
                     client.send(new InputMessages.LArmDown(playerID));
                 }
                 prevDeltaWheel = 1;
             } else if (evt.getDeltaWheel() < 0) {
                 if (prevDeltaWheel > 0 && !(charMap.get(playerID).elbowWristAngle == CharMovement.Constraints.lRotMin)) {
                     client.send(new InputMessages.StopLArm(playerID));
                 } else {
                     client.send(new InputMessages.LArmUp(playerID));
                 }
                 prevDeltaWheel = -1;
             }
         } catch (IOException ex) {
             Logger.getLogger(BladeClient.class.getName()).log(Level.SEVERE, null, ex);
         }
 
     }
     private boolean prevPressed = false;
 
     public void onMouseButtonEvent(MouseButtonEvent evt) {
         if (evt.isPressed() != prevPressed) {
             if (evt.isPressed()) {
                 if (evt.getButtonIndex() == MouseInput.BUTTON_LEFT) {
                     try {
                         client.send(new InputMessages.RotateUArmCC(playerID));
                     } catch (IOException ex) {
                         Logger.getLogger(BladeClient.class.getName()).log(Level.SEVERE, null, ex);
                     }
                 } else if (evt.getButtonIndex() == MouseInput.BUTTON_RIGHT) {
                     try {
                         client.send(new InputMessages.RotateUArmC(playerID));
                     } catch (IOException ex) {
                         Logger.getLogger(BladeClient.class.getName()).log(Level.SEVERE, null, ex);
                     }
                 }
             } else {
                 try {
                     client.send(new InputMessages.StopRotateTwist(playerID));
                 } catch (IOException ex) {
                     Logger.getLogger(BladeClient.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
         }
 
         prevPressed = evt.isPressed();
         inputManager.setCursorVisible(false);
 
         if (evt.getButtonIndex() == MouseInput.BUTTON_MIDDLE) {
             try {
                 client.send(new InputMessages.StopLArm(playerID));
             } catch (IOException ex) {
                 Logger.getLogger(BladeClient.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
     }
 
     public void onKeyEvent(KeyInputEvent evt) {
         try {
             int key = evt.getKeyCode();
             if (!evt.isRepeating()) {
                 switch (key) {
                     case KeyInput.KEY_E:
                         if (evt.isPressed()) {
                             client.send(new InputMessages.MoveCharForward(playerID));
                         } else {
                             client.send(new InputMessages.StopForwardMove(playerID));
                         }
                         break;
                     case KeyInput.KEY_S:
                         if (evt.isPressed()) {
                             client.send(new InputMessages.MoveCharLeft(playerID));
                         } else {
                             client.send(new InputMessages.StopLeftRightMove(playerID));
                         }
                         break;
                     case KeyInput.KEY_D:
                         if (evt.isPressed()) {
                             client.send(new InputMessages.MoveCharBackword(playerID));
                         } else {
                             client.send(new InputMessages.StopForwardMove(playerID));
                         }
                         break;
                     case KeyInput.KEY_F:
                         if (evt.isPressed()) {
                             client.send(new InputMessages.MoveCharRight(playerID));
                         } else {
                             client.send(new InputMessages.StopLeftRightMove(playerID));
                         }
                         break;
                     case KeyInput.KEY_W:
                         if (evt.isPressed()) {
                             client.send(new InputMessages.TurnCharLeft(playerID));
                         } else {
                             client.send(new InputMessages.StopCharTurn(playerID));
                         }
                         break;
                     case KeyInput.KEY_R:
                         if (evt.isPressed()) {
                             client.send(new InputMessages.TurnCharRight(playerID));
                         } else {
                             client.send(new InputMessages.StopCharTurn(playerID));
                         }
                         break;
                 }
             }
         } catch (IOException ex) {
             Logger.getLogger(BladeClient.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     @Override
     public void destroy() {
         super.destroy();
         try {
             client.disconnect();
         } catch (Throwable ex) {
             Logger.getLogger(BladeClient.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     public void clientConnected(Client client) {
     }
 
     public void clientDisconnected(Client client) {
     }
 
     public void onAnimCycleDone(AnimControl control, AnimChannel channel, String animName) {
 
     }
 
     public void onAnimChange(AnimControl control, AnimChannel channel, String animName) {
          
     }
 }
 
 
