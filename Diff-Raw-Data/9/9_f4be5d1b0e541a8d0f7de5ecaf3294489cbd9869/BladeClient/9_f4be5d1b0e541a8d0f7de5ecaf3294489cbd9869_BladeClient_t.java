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
 import com.jme3.app.SimpleApplication;
 import com.jme3.asset.TextureKey;
 import com.jme3.bounding.BoundingVolume;
 import com.jme3.bullet.BulletAppState;
 import com.jme3.bullet.collision.PhysicsCollisionObject;
 import com.jme3.bullet.control.CharacterControl;
 import com.jme3.bullet.control.RigidBodyControl;
 import com.jme3.input.ChaseCamera;
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
 import com.jme3.network.serializing.Serializer;
 import com.jme3.scene.Geometry;
 import com.jme3.scene.Node;
 import com.jme3.terrain.heightmap.AbstractHeightMap;
 import com.jme3.terrain.geomipmap.TerrainQuad;
 import com.jme3.terrain.heightmap.ImageBasedHeightMap;
 import com.jme3.texture.Texture;
 import com.jme3.texture.Texture.WrapMode;
 import java.io.IOException;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import jme3tools.converters.ImageToAwt;
 import mygame.messages.CharCreationMessage;
 import mygame.messages.CharDestructionMessage;
 import com.jme3.bullet.collision.shapes.BoxCollisionShape;
 import com.jme3.bullet.collision.shapes.CompoundCollisionShape;
 import com.jme3.bullet.util.CollisionShapeFactory;
 import com.jme3.light.AmbientLight;
 import com.jme3.light.DirectionalLight;
 import com.jme3.math.ColorRGBA;
 import com.jme3.math.Matrix3f;
 import com.jme3.math.Quaternion;
 import com.jme3.niftygui.NiftyJmeDisplay;
 import com.jme3.renderer.queue.RenderQueue.ShadowMode;
 import com.jme3.scene.debug.SkeletonDebugger;
 import com.jme3.system.AppSettings;
 import com.jme3.util.SkyFactory;
 import de.lessvoid.nifty.Nifty;
 import java.util.Map;
 import java.util.concurrent.Callable;
 import java.util.concurrent.Future;
 import mygame.messages.CharStatusMessage;
 import mygame.messages.ClientReadyMessage;
 import mygame.ui.HUD;
 import mygame.ui.LoginScreen;
 import mygame.util.IOLib;
 
 /**
  *
  * @author blah
  */
 
 
 public class BladeClient extends SimpleApplication implements MessageListener, RawInputListener, ConnectionListener, AnimEventListener {
 
     private ChaseCamera chaseCam;
     private Node model;
     ConcurrentHashMap<Long, Node> modelMap = new ConcurrentHashMap();
     ConcurrentHashMap<Long, Vector3f> upperArmAnglesMap = new ConcurrentHashMap();
     ConcurrentHashMap<Long, Vector3f> upperArmVelsMap = new ConcurrentHashMap();
     ConcurrentHashMap<Long, Float> elbowWristAngleMap = new ConcurrentHashMap();
     ConcurrentHashMap<Long, Float> elbowWristVelMap = new ConcurrentHashMap();
     HashSet<Long> playerSet = new HashSet();
     ConcurrentHashMap<Long, Vector3f> charPositionMap = new ConcurrentHashMap();
     ConcurrentHashMap<Long, Vector3f> charVelocityMap = new ConcurrentHashMap();
     ConcurrentHashMap<Long, Float> charAngleMap = new ConcurrentHashMap();
     ConcurrentHashMap<Long, Float> charTurnVelMap = new ConcurrentHashMap();
     ConcurrentHashMap<Long, AnimChannel> animChannelMap = new ConcurrentHashMap();
     ConcurrentHashMap<Long, Float> charLifeMap = new ConcurrentHashMap();
 
     private final boolean debug = false;
     private BulletAppState bulletAppState;
     private TerrainQuad terrain;
     Material mat_terrain;
     Material lighting;
     Material wall_mat;
     Material stone_mat;
     Material floor_mat;
     private RigidBodyControl terrain_phy;
     private RigidBodyControl basic_phy;
     private RigidBodyControl body_phy;
     private Node House;
     CharacterControl character;
     CompoundCollisionShape collisionShape;
     BoundingVolume ballBound;
     Geometry block;
     BoxCollisionShape leftShoulder;
     static BladeClient app;
     boolean readyToStart=false;
     boolean started=false;
     Nifty ui;
     HUD hud;
 
     Client client;
     boolean clientSet = false;
     private long playerID = 0;
 
     public static void main(String[] args) {
         app = new BladeClient();
         AppSettings appSettings=new AppSettings(true);
         appSettings.setFrameRate(30);
         app.setPauseOnLostFocus(false);
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
         Serializer.registerClass(CharStatusMessage.class);
         Serializer.registerClass(CharCreationMessage.class);
         Serializer.registerClass(CharDestructionMessage.class);
         Serializer.registerClass(ClientReadyMessage.class);
         InputMessages.registerInputClasses();
         Map<String,String> ipAddressMap=IOLib.getIpAddressMap();
 
         NiftyJmeDisplay niftyDisplay = new NiftyJmeDisplay(assetManager,inputManager,audioRenderer,guiViewPort);
         ui=niftyDisplay.getNifty();
         ui.fromXml("Interface/UI.xml","start",new LoginScreen(ipAddressMap,client,BladeMain.port,this));
         
         guiViewPort.addProcessor(niftyDisplay);
         flyCam.setDragToRotate(true);
         //app.setDisplayStatView(false);
         
         flyCam.setMoveSpeed(50);
         bulletAppState = new BulletAppState();
         stateManager.attach(bulletAppState);
         rootNode.attachChild(SkyFactory.createSky(assetManager, "Textures/Skysphere.jpg", true));
         initMaterials();
         initTerrain();
 
         DirectionalLight sun = new DirectionalLight();
         sun.setDirection(new Vector3f(-0.1f, -0.7f, -1.0f));
         rootNode.addLight(sun);
 
         DirectionalLight sun2 = new DirectionalLight();
         sun2.setDirection(new Vector3f(0.1f, 0.7f, 1.0f));
         rootNode.addLight(sun2);
 
         flyCam.setEnabled(false);
         
         if (debug) {
             bulletAppState.getPhysicsSpace().enableDebug(this.getAssetManager());
         }
 
     }
     private boolean mouseCurrentlyStopped = true;
  
     @Override
     public void simpleUpdate(float tpf) {
         if (readyToStart && !started){
             System.out.println("starting");
             if(client==null){
                 System.out.println("Client is null");
             }
             client.addConnectionListener(this);
             InputMessages.addInputMessageListeners(client, this);
             client.addMessageListener(this, CharCreationMessage.class, CharDestructionMessage.class, CharStatusMessage.class,ClientReadyMessage.class);
             try {
                 client.send(new ClientReadyMessage());
                 System.err.println("Sent ClientReadyMessage");
             } catch (IOException ex) {
                 Logger.getLogger(BladeClient.class.getName()).log(Level.SEVERE, null, ex);
             }
             started=true;
             System.out.println("started");
             hud=new HUD(this);
             hud.enableLifeDisplay();
             app.getGuiNode().attachChild(hud);
             
         }
 
         if (clientSet) {
             characterUpdate(tpf);
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
     
     public void characterUpdate(float tpf) {
         for (Iterator<Long> playerIterator = playerSet.iterator(); playerIterator.hasNext();) {
             long nextPlayerID = playerIterator.next();
             upperArmAnglesMap.put(nextPlayerID, CharMovement.extrapolateUpperArmAngles(upperArmAnglesMap.get(nextPlayerID), upperArmVelsMap.get(nextPlayerID), tpf));
             
             elbowWristAngleMap.put(nextPlayerID, CharMovement.extrapolateLowerArmAngles(elbowWristAngleMap.get(nextPlayerID), elbowWristVelMap.get(nextPlayerID), tpf));
 
             charAngleMap.put(nextPlayerID, CharMovement.extrapolateCharTurn(charAngleMap.get(nextPlayerID), charTurnVelMap.get(nextPlayerID), tpf));
 
     //        Vector3f alternateExtrap=CharMovement.oldExtrapolateCharMovement(charPositionMap.get(nextPlayerID),charVelocityMap.get(nextPlayerID),
       //              charAngleMap.get(nextPlayerID),charTurnVelMap.get(nextPlayerID),tpf);
             charPositionMap.put(nextPlayerID, CharMovement.extrapolateCharMovement(charPositionMap.get(nextPlayerID),
                     charVelocityMap.get(nextPlayerID), charAngleMap.get(nextPlayerID),charTurnVelMap.get(nextPlayerID),tpf));
 
             CharMovement.setUpperArmTransform(upperArmAnglesMap.get(nextPlayerID), modelMap.get(nextPlayerID));
             CharMovement.setLowerArmTransform(elbowWristAngleMap.get(nextPlayerID), modelMap.get(nextPlayerID));
 
         //    Vector3f correctiveVelocity=correctiveVelocityMap.get(nextPlayerID);
             
             
             Vector3f extrapolatedPosition,currentPosition;
             extrapolatedPosition=charPositionMap.get(nextPlayerID);
             currentPosition=modelMap.get(nextPlayerID).getLocalTranslation();
      //       float diffLength=FastMath.sqrt(FastMath.sqr(extrapolatedPosition.x-currentPosition.x)+FastMath.sqr(extrapolatedPosition.z-currentPosition.z));
             CharacterControl control=modelMap.get(nextPlayerID).getControl(CharacterControl.class);
             
             Vector3f diffVect=new Vector3f(extrapolatedPosition.x-currentPosition.x,0,extrapolatedPosition.z-currentPosition.z);
             System.out.println("diffVect:"+diffVect);
             float correctiveConstant=0.2f;
             float correctiveX=0;
             float correctiveZ=0;
             float diffX=extrapolatedPosition.x-currentPosition.x;
             float diffZ=extrapolatedPosition.z-currentPosition.z;
             float diffLength=diffVect.length();/*
             if(diffLength>10){
                 control.setPhysicsLocation(extrapolatedPosition);
             }
             else {
                 if(diffX>1)
                     correctiveX=1.0f/3.0f;
                 else
                     correctiveX=FastMath.abs(diffX)/3;
                 if(diffZ>1)
                     correctiveZ=1.0f/3.0f;
                 else
                     correctiveZ=FastMath.abs(diffZ)/3;
             }*/
                 
             Vector3f correctiveVelocity=new Vector3f(diffVect.x*correctiveConstant,0,diffVect.z*correctiveConstant);
         //    correctiveVelocityMap.put(nextPlayerID, correctiveVelocity);
             
     //        float oldDiffLength=FastMath.sqrt(FastMath.sqr(alternateExtrap.x-currentPosition.x)+FastMath.sqr(alternateExtrap.z-currentPosition.z));
       //      System.out.println("new diff is "+diffLength+", old dif is "+oldDiffLength);
    //         if(diffLength>1.5f){
     //              control.setPhysicsLocation(extrapolatedPosition);//new Vector3f(extrapolatedPosition.x,currentPosition.y+1,extrapolatedPosition.z));
    //               System.out.println("warping");
     //        }
 
             float xDir,zDir;
             zDir=FastMath.cos(charAngleMap.get(nextPlayerID));
             xDir=FastMath.sin(charAngleMap.get(nextPlayerID));
             Vector3f viewDirection=new Vector3f(xDir,0,zDir);
             modelMap.get(nextPlayerID).getControl(CharacterControl.class).setViewDirection(viewDirection);
 
             Vector3f forward,up,left;
             float xVel,zVel;
             xVel=charVelocityMap.get(nextPlayerID).x;
             zVel=charVelocityMap.get(nextPlayerID).z;
             forward=new Vector3f(viewDirection);
             up=new Vector3f(0,1,0);
             left=up.cross(forward);
 
             if(nextPlayerID==playerID){
                 cam.setDirection(viewDirection);
                 cam.setLocation(modelMap.get(nextPlayerID).getLocalTranslation().add(new Vector3f(0,4,0)).subtract(viewDirection.mult(8)));
             }
 
             Vector3f velocity=left.mult(xVel).add(forward.mult(zVel));
             
             control.setWalkDirection(velocity.add(correctiveVelocity));
 
             // first, get rotation and position of hand
             Bone hand = modelMap.get(nextPlayerID).getControl(AnimControl.class).getSkeleton().getBone("swordHand");
             Matrix3f rotation = hand.getModelSpaceRotation().toRotationMatrix();
             Vector3f position = hand.getModelSpacePosition();
 
             // set the position of the sword to the position of the hand
             Node swordNode = (Node) modelMap.get(nextPlayerID).getChild("sword");
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
                 SwordControl sword = modelMap.get(nextPlayerID).getChild("sword").getControl(SwordControl.class);
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
 
     public void initTerrain() {
 
 
         mat_terrain = new Material(assetManager, "Common/MatDefs/Terrain/TerrainLighting.j3md");
         //house_mat = new Material(assetManager,"Common/MatDefs/Water/SimpleWater.j3md");
         mat_terrain.setBoolean("useTriPlanarMapping", false);
         mat_terrain.setBoolean("WardIso", true);
         mat_terrain.setTexture("AlphaMap", assetManager.loadTexture("Textures/alpha1.1.png"));
         
         Texture grass = assetManager.loadTexture("Textures/grass.jpg");
         grass.setWrap(WrapMode.Repeat);
         mat_terrain.setTexture("m_DiffuseMap", grass);
         mat_terrain.setFloat("m_DiffuseMap_0_scale", 64f);
 
         Texture dirt = assetManager.loadTexture("Textures/TiZeta_SmlssWood1.jpg");
         dirt.setWrap(WrapMode.Repeat);
         mat_terrain.setTexture("m_DiffuseMap_1", dirt);
         mat_terrain.setFloat("m_DiffuseMap_1_scale", 16f);
 
         Texture rock = assetManager.loadTexture("Textures/TiZeta_cem1.jpg");
         rock.setWrap(WrapMode.Repeat);
         mat_terrain.setTexture("m_DiffuseMap_2", rock);
         mat_terrain.setFloat("m_DiffuseMap_2_scale", 128f);
 
         Texture normalMap0 = assetManager.loadTexture("Textures/grass_normal.png");
         normalMap0.setWrap(WrapMode.Repeat);
         Texture normalMap1 = assetManager.loadTexture("Textures/dirt_normal.png");
         normalMap1.setWrap(WrapMode.Repeat);
         Texture normalMap2 = assetManager.loadTexture("Textures/road_normal.png");
         normalMap2.setWrap(WrapMode.Repeat);
         mat_terrain.setTexture("NormalMap", normalMap0);
         mat_terrain.setTexture("NormalMap_1", normalMap2);
         mat_terrain.setTexture("NormalMap_2", normalMap2);
         lighting = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
         lighting.setTexture("DiffuseMap", grass);
         lighting.setTexture("NormalMap", normalMap1);
         lighting.setBoolean("WardIso", true);
 
         AbstractHeightMap heightmap = null;
         Texture heightMapImage = assetManager.loadTexture("Textures/flatland.png");
         heightmap = new ImageBasedHeightMap(
                 ImageToAwt.convert(heightMapImage.getImage(), false, true, 0));
         heightmap.load();
         terrain = new TerrainQuad("my terrain", 65, 1025, heightmap.getHeightMap());
         terrain.setMaterial(mat_terrain);
 
         terrain.setLocalTranslation(0, -100, 0);
         terrain.setLocalScale(2f, 2f, 2f);
         
         rootNode.attachChild(terrain);
 
         terrain_phy = new RigidBodyControl(0.0f);
         terrain_phy.addCollideWithGroup(PhysicsCollisionObject.COLLISION_GROUP_02);
         terrain_phy.addCollideWithGroup(PhysicsCollisionObject.COLLISION_GROUP_03);
         terrain.addControl(terrain_phy);
         bulletAppState.getPhysicsSpace().add(terrain_phy);
         
         House = (Node)assetManager.loadModel("Models/Cube.mesh.j3o");
         House.setLocalTranslation(0.0f, 3.0f, 70.0f);
         House.setShadowMode(ShadowMode.CastAndReceive);
         House.setLocalScale(13f);
         House.setMaterial(wall_mat); 
         //Does not work atm house_mat.setTexture("m_Tex1", rock);
         //House.setMaterial(house_mat);
         House.setMaterial(wall_mat);
         rootNode.attachChild(House);
         RigidBodyControl house_phy = new RigidBodyControl(0.0f);
         house_phy.addCollideWithGroup(PhysicsCollisionObject.COLLISION_GROUP_02);
         house_phy.addCollideWithGroup(PhysicsCollisionObject.COLLISION_GROUP_03);
         House.addControl(house_phy);
         bulletAppState.getPhysicsSpace().add(house_phy);
         
          DirectionalLight light = new DirectionalLight();
         light.setDirection((new Vector3f(-0.5f,-1f, -0.5f)).normalize());
         rootNode.addLight(light);
         
         
     }
 
     public void initMaterials() {
         wall_mat = new Material(assetManager, "Common/MatDefs/Misc/SimpleTextured.j3md");
         TextureKey key = new TextureKey("Textures/road.jpg");
         key.setGenerateMips(true);
         Texture tex = assetManager.loadTexture(key);
         wall_mat.setTexture("ColorMap", tex);
 
         stone_mat = new Material(assetManager, "Common/MatDefs/Misc/SimpleTextured.j3md");
         TextureKey key2 = new TextureKey("Textures/road.jpg");
         key2.setGenerateMips(true);
 
         Texture tex2 = assetManager.loadTexture(key2);
         stone_mat.setTexture("ColorMap", tex2);
 
         floor_mat = new Material(assetManager, "Common/MatDefs/Misc/SimpleTextured.j3md");
         TextureKey key3 = new TextureKey("Textures/grass.jpg");
         key3.setGenerateMips(true);
         Texture tex3 = assetManager.loadTexture(key3);
         tex3.setWrap(WrapMode.Repeat);
         floor_mat.setTexture("ColorMap", tex3);
     }
 
     public void messageReceived(Message message) {
         if (message instanceof CharDestructionMessage){
             CharDestructionMessage destroMessage=(CharDestructionMessage)message;
             final long destroyedPlayerID=destroMessage.playerID;
             System.out.println("my id is "+playerID+", and destroID is "+destroyedPlayerID);
             playerSet.remove(destroyedPlayerID);
             Node model=modelMap.get(destroyedPlayerID);
             bulletAppState.getPhysicsSpace().remove(model.getChild("sword").getControl(SwordControl.class));
             bulletAppState.getPhysicsSpace().remove(model.getControl(BodyControl.class));
             bulletAppState.getPhysicsSpace().remove(model.getControl(CharacterControl.class));
             //rootNode.detachChild(modelMap.get(destroyedPlayerID));
             Future action = app.enqueue(new Callable() {
 
                 public Object call() throws Exception {
                     rootNode.detachChild(modelMap.get(destroyedPlayerID));
                     modelMap.remove(destroyedPlayerID);
                     return null;
                 }
             });
             //to retrieve return value (waits for call to finish, fire&forget otherwise):
             //action.get();
         }
         else if (message instanceof CharCreationMessage) {
             System.out.println("Creating character");
             CharCreationMessage creationMessage = (CharCreationMessage) message;
             long newPlayerID = creationMessage.playerID;
            final Node newModel = Character.createCharacter("Models/Female.mesh.j3o", "Models/sword.mesh.j3o", assetManager, bulletAppState, true, newPlayerID);
             //rootNode.attachChild(newModel);
             
             if (debug) {
                 AnimControl control = newModel.getControl(AnimControl.class);
                 SkeletonDebugger skeletonDebug = new SkeletonDebugger("skeleton", control.getSkeleton());
                 Material mat2 = new Material(assetManager, "Common/MatDefs/Misc/WireColor.j3md");
                 mat2.setColor("Color", ColorRGBA.Green);
                 mat2.getAdditionalRenderState().setDepthTest(false);
                 skeletonDebug.setMaterial(mat2);
                 newModel.attachChild(skeletonDebug);
                 
                 Node newSword = (Node)newModel.getChild("sword");
                 
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
                 model = newModel;
                 System.out.println("claiming player id " + playerID);
 /*
                 chaseCam = new ChaseCamera(cam, model, inputManager);
                 chaseCam.setSmoothMotion(true);
                 chaseCam.setDefaultVerticalRotation(FastMath.HALF_PI / 4f);
                 chaseCam.setLookAtOffset(new Vector3f(0.0f, 4.0f, 0.0f));
  */
                 registerInput();
                 clientSet = true;
             }
             modelMap.put(newPlayerID, newModel);
             
             playerSet.add(newPlayerID);
             upperArmAnglesMap.put(newPlayerID, new Vector3f());
             upperArmVelsMap.put(newPlayerID, new Vector3f());
             elbowWristAngleMap.put(newPlayerID, new Float(CharMovement.Constraints.lRotMin));
             elbowWristVelMap.put(newPlayerID, new Float(0f));
             charPositionMap.put(newPlayerID, new Vector3f());
             charVelocityMap.put(newPlayerID, new Vector3f());
             charAngleMap.put(newPlayerID, 0f);
             charTurnVelMap.put(newPlayerID, 0f);
             animChannelMap.put(newPlayerID, modelMap.get(newPlayerID).getControl(AnimControl.class).createChannel());
             animChannelMap.get(newPlayerID).setAnim("stand");
             charLifeMap.put(newPlayerID, 1f);
 
             Future action = app.enqueue(new Callable() {
 
                 public Object call() throws Exception {
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
 
                 upperArmAnglesMap.put(messagePlayerID, charStatus.upperArmAngles.clone());
                 upperArmVelsMap.put(messagePlayerID, charStatus.upperArmVels.clone());
                 elbowWristAngleMap.put(messagePlayerID, charStatus.elbowWristAngle);
                 elbowWristVelMap.put(messagePlayerID, charStatus.elbowWristVel);
                 charPositionMap.put(messagePlayerID, charStatus.charPosition);
                 charVelocityMap.put(messagePlayerID, charStatus.charVelocity);
                 charAngleMap.put(messagePlayerID, charStatus.charAngle);
                 charTurnVelMap.put(messagePlayerID, charStatus.charTurnVel);
                 charLifeMap.put(messagePlayerID, charStatus.life);
                 if (animChannelMap.get(messagePlayerID) != null) {
                     if (charVelocityMap.get(messagePlayerID).equals(new Vector3f(0, 0, 0))) {
                         if (animChannelMap.get(messagePlayerID).getAnimationName().equals("walk")) {
                             animChannelMap.get(messagePlayerID).setAnim("stand");
                         }
                     } else {
                         if (animChannelMap.get(messagePlayerID).getAnimationName().equals("stand")) {
                             animChannelMap.get(messagePlayerID).setAnim("walk");
                         }
                     }
                 }
                 
                 if(messagePlayerID==playerID){
                     hud.setLifeDisplayValue(charStatus.life);
                 }
             }
         }
     }
 
     public void messageSent(Message message) {
     //    System.out.println(message.getClass());
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
     private final int eventsPerPacket = 1; // how many events should happen before next packet is sent
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
                 if (prevDeltaWheel < 0 && !(elbowWristAngleMap.get(playerID) == CharMovement.Constraints.lRotMax)) {
                     client.send(new InputMessages.StopLArm(playerID));
                 } else {
                     client.send(new InputMessages.LArmDown(playerID));
                 }
                 prevDeltaWheel = 1;
             } else if (evt.getDeltaWheel() < 0) {
                 if (prevDeltaWheel > 0 && !(elbowWristAngleMap.get(playerID) == CharMovement.Constraints.lRotMin)) {
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
 
 
