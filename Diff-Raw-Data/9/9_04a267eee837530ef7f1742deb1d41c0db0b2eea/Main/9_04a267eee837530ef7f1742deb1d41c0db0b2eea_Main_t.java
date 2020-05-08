 package mygame;
 
 import com.jme3.app.SimpleApplication;
 import com.jme3.asset.TextureKey;
 import com.jme3.bullet.BulletAppState;
 import com.jme3.bullet.collision.PhysicsCollisionEvent;
 import com.jme3.bullet.collision.PhysicsCollisionListener;
 import com.jme3.bullet.collision.PhysicsCollisionObject;
 import com.jme3.bullet.collision.shapes.SphereCollisionShape;
 import com.jme3.bullet.control.RigidBodyControl;
 import com.jme3.effect.ParticleEmitter;
 import com.jme3.effect.ParticleMesh;
 import com.jme3.font.BitmapText;
 import com.jme3.input.KeyInput;
 import com.jme3.input.controls.ActionListener;
 import com.jme3.input.controls.AnalogListener;
 import com.jme3.input.controls.KeyTrigger;
 import com.jme3.light.AmbientLight;
 import com.jme3.light.DirectionalLight;
 import com.jme3.material.Material;
 import com.jme3.math.ColorRGBA;
 import com.jme3.math.Vector3f;
 import com.jme3.renderer.RenderManager;
 import com.jme3.scene.Geometry;
 import com.jme3.scene.Spatial;
 import com.jme3.scene.shape.Box;
 import com.jme3.scene.shape.Sphere;
 import com.jme3.texture.Texture;
 import com.jme3.util.TangentBinormalGenerator;
 import java.math.BigInteger;
 
 
 public class Main extends SimpleApplication {
   
     private Boolean isRunning = true;
     private Boolean isBall1Alive = true;
     private Boolean isBall2Alive = true;   
     private Boolean isBall3Alive = true;
     private Boolean isBall4Alive = true;
     private Boolean gameEnd = false;    
     
     private BulletAppState bulletAppState;
 
     private Material mat_lit;
     private Material mat_rock;
     private Material mat_dirt;
     private Material mat_road;
     private Material mat_red;
     private final float gax = (float) 0.5;       
     private Sphere c;   
     private ParticleEmitter fire;    
     private BitmapText ch;
     private DirectionalLight sun;       
     private int abilitySize = 2;
     BigInteger[] p1Abilities = new BigInteger[abilitySize];
     BigInteger[] p2Abilities = new BigInteger[abilitySize]; 
     private int counter = 0;
      
     //board parameter
     private int boardLength;
     private int boardWidth;
     private int currentmap;
     private int[] maptexture;
     private int[] mapobjects;    
         
     //Destory Map stuff
     private BigInteger deathClk;
     private Spatial[] mapObj;
     private int objNum;
     
     //Map data
     private int[] map1 = {0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
         0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 
         0, 0, 0, 0, 0, 3, 3, 3, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 
         0, 0, 0, 0, 0, 3, 3, 3, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 2, 2, 0,
         0, 2, 2, 2, 0, 0};
     private int[] map2 = {2, 3, 3, 3, 3, 3, 3, 3, 3, 3, 2, 2, 2, 2, 2, 2, 2, 2, 
         3, 3, 3, 3, 3, 3, 3, 3, 3, 2, 2, 2, 2, 2, 2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 
         0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 0, 
         0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
     private int[] map3 = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
         0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
         0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
         0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
         0, 0, 0, 0, 0, 0};
     private int[] map4 = {3, 0, 0, 0, 3, 3, 3, 0, 0, 0, 3, 0, 0, 3, 3, 0, 0, 3, 
         0, 0, 0, 3, 3, 3, 0, 0, 0, 3, 0, 0, 3, 3, 0, 0, 0, 3, 0, 0, 0, 0, 0, 3, 
         0, 3, 0, 0, 3, 0, 3, 0, 0, 0, 0, 0, 3, 0, 3, 0, 0, 3, 0, 0, 0, 3, 0, 0, 
         0, 0, 0, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0, 3, 3, 3, 0, 0, 3, 3, 3, 0};
     private int[] map5 = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
         0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
         0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
         0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
     
     //map objects
     private int[] obj1 = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
         0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
         0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,  
         0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
         0, 0, 0, 0, 0, 0};
     private int[] obj2 = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
         0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
         0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
         0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
     private int[] obj3 = {5, 0, 0, 0, 0, 0, 0, 0, 0, 0, 5, 0, 0, 0, 0, 0, 0, 5, 
         0, 0, 0, 0, 0, 0, 0, 0, 0, 5, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
         0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8, 0, 0, 0, 0, 0, 
         8, 0, 0, 8, 0, 0, 0, 0, 0, 8, 0, 0, 0, 7, 0, 6, 0, 0, 6, 0, 7, 0, 0};
     private int[] obj4 = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
         0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
         0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4, 0, 0, 0, 4, 
         0, 4, 4, 0, 4, 0, 0, 0, 4, 0, 4, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
     private int[] obj5 = {0, 0, 0, 0, 7, 0, 6, 0, 0, 0, 0, 0, 0, 7, 7, 0, 0, 0, 
         0, 0, 0, 6, 0, 7, 0, 0, 0, 0, 0, 0, 6, 6, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
         0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 7, 0, 0, 0, 6, 
         0, 0, 0, 0, 6, 0, 0, 0, 7, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
     
     // array of ball instances
     private RigidBodyControl [] ball_phy;
     private SphereCollisionShape [] ballShape;
     private Geometry [] ball;
     
     // Hardcoded number of players for testing
     private int numberPlayer = 4;
     
     public static void main(String[] args) {
         Main app = new Main();
         app.start();
 
     }
     /*floor stuff*/   
  
     
     @Override
     public void simpleInitApp() {        
           
         /** Set up Physics Game */
         bulletAppState = new BulletAppState();
         stateManager.attach(bulletAppState);
         bulletAppState.getPhysicsSpace().addCollisionListener(physicsCollisionListener);               
         //Selected map
         currentmap = 0;
         
         //Load Map texture and object arraies
         maptexture = selectedMapTexture(currentmap);
         mapobjects = selectedMapObject(currentmap);        
         
         // Initialize map and keys
         variableInit();
         setCam();
         setUpLight();
         setUpKeys();        
         initMaterials();        
         InitObj();
         initAbilities(p1Abilities);
         initAbilities(p2Abilities);
         
     }
     
     // initialize balls, obstacle and pwrups
     private void InitObj(){
         variableInit();
         createFloor(currentmap);
         createPowerUp();
         createBallArray(numberPlayer);
     }
     
     private void variableInit(){
         isRunning = true;
         isBall1Alive = true;
         isBall2Alive = true;  
         isBall3Alive = true;
         isBall4Alive = true;
         gameEnd = false;      
         deathClk = new BigInteger(String.valueOf(
             System.nanoTime())).add(new BigInteger("5000000000"));        
     }
     
     // set camera position and light
     private void setCam(){
        
         viewPort.setBackgroundColor(new ColorRGBA(0.7f, 0.8f, 1f, 1f));
         flyCam.setEnabled(false);        
        cam.setLocation(new Vector3f(11f,20f,25f)); 
        //cam.setLocation(new Vector3f(0f, 25f,35f));
         cam.setAxes(new Vector3f(0f,0f,0f),new Vector3f(0f,0f,0f),new Vector3f(-100f,0f,0f));
        cam.lookAt(new Vector3f(11f,-25f,-10f), cam.getUp());
         sun = new DirectionalLight();
         sun.setDirection(new Vector3f(1,0,-2).normalizeLocal());
         sun.setColor(ColorRGBA.White);
         rootNode.addLight(sun);             
     }
     
     //Initialize power-up objects
     private void createPowerUp(){        
         //create and edit properties
         fire = 
            new ParticleEmitter("Emitter", ParticleMesh.Type.Triangle, 30);
         mat_red = new Material(assetManager, 
            "Common/MatDefs/Misc/Particle.j3md");
         mat_red.setTexture("Texture", assetManager.loadTexture(
            "Effects/Explosion/flame.png"));
         fire.setMaterial(mat_red);
         fire.setImagesX(2); 
         fire.setImagesY(2); // 2x2 texture animation
         fire.setEndColor(new ColorRGBA(1f, 0f, 1f, 1f));   // red
         fire.setStartColor(new ColorRGBA(1f, 0f, 2f, 0.5f)); // yellow
         fire.getParticleInfluencer().setInitialVelocity(new Vector3f(0, 2, 0));
         fire.setStartSize(2f);
         fire.setEndSize(0.1f);
         fire.setGravity(0, 0, 0);
         fire.setLowLife(1f);
         fire.setHighLife(2f);
         fire.getParticleInfluencer().setVelocityVariation(0.1f);
         fire.setLocalTranslation(new Vector3f(boardWidth+2f,0.1f,boardLength-3f));        
         //place on the map
         rootNode.attachChild(fire);         
     }
     
     
     private void createBallArray(int number){
         
         c = new Sphere(20, 20, 0.5f, true, false);
         c.setTextureMode(Sphere.TextureMode.Projected);
         TangentBinormalGenerator.generate(c);  
         ball = new Geometry [number];
         ballShape = new SphereCollisionShape[number];
         ball_phy = new RigidBodyControl[number];
         for(int i = 0; i < number; i++){
             ball[i] = new Geometry("Ball "+i, c);
             ballShape[i] = new SphereCollisionShape(0.5f);
             if(i==0)
             {
             ball[i].setMaterial(mat_lit);
             ball[i].setLocalTranslation(1,gax,1);
             }
             else if(i==1)
             {
                 ball[1].setMaterial(mat_rock);
                 ball[i].setLocalTranslation(1,gax,2*boardWidth-3);
             }
             else if(i==2)
             {
                 ball[i].setMaterial(mat_dirt);
                 ball[i].setLocalTranslation(2*boardLength-3,gax,2*boardWidth-3);
             }
             else if(i==3)
             {
                 ball[i].setMaterial(mat_road);
                 ball[i].setLocalTranslation(2*boardLength-3,gax,1);
             }
                         
             rootNode.attachChild(ball[i]);
             ball_phy[i] = new RigidBodyControl(ballShape[i],0.9f);
             ball[i].addControl(ball_phy[i]); 
             bulletAppState.getPhysicsSpace().add(ball_phy[i]);
             ball_phy[i].setCollisionGroup(PhysicsCollisionObject.COLLISION_GROUP_01);
             ball_phy[i].addCollideWithGroup(PhysicsCollisionObject.COLLISION_GROUP_01);
             ball_phy[i].setRestitution(1f);            
         }        
     }       
     
     //Initialize material and texture
     public void initMaterials(){
 
         //Defining Ball 1 material
         mat_lit = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
         mat_lit.setTexture("DiffuseMap", assetManager.loadTexture("Textures/Terrain/Pond/Pond.jpg"));
         mat_lit.setTexture("NormalMap", assetManager.loadTexture("Textures/Terrain/Pond/Pond_normal.png"));
         mat_lit.setBoolean("UseMaterialColors",true);    
         mat_lit.setColor("Specular",ColorRGBA.White);
         mat_lit.setColor("Diffuse",ColorRGBA.White);
         mat_lit.setFloat("Shininess", 5f); // [1,128]   
         
         //Defining Ball 2 material
         mat_rock = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
         mat_rock.setTexture("DiffuseMap", assetManager.loadTexture("Textures/Terrain/Rock/Rock.PNG"));
         mat_rock.setTexture("NormalMap", assetManager.loadTexture("Textures/Terrain/Rock/Rock_normal.png"));
         mat_rock.setBoolean("UseMaterialColors",true);    
         mat_rock.setColor("Specular",ColorRGBA.White);
         mat_rock.setColor("Diffuse",ColorRGBA.White);
         mat_rock.setFloat("Shininess", 5f); // [1,128] 
         
          //Defining Ball 3 material
         mat_dirt = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
         mat_dirt.setTexture("DiffuseMap", assetManager.loadTexture("Textures/Terrain/splat/dirt.jpg"));
         mat_dirt.setTexture("NormalMap", assetManager.loadTexture("Textures/Terrain/splat/dirt.jpg"));
         mat_dirt.setBoolean("UseMaterialColors",true);    
         mat_dirt.setColor("Specular",ColorRGBA.White);
         mat_dirt.setColor("Diffuse",ColorRGBA.White);
         mat_dirt.setFloat("Shininess", 5f); // [1,128]
         
         //Defining Ball 4 material
         mat_road = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
         mat_road.setTexture("DiffuseMap", assetManager.loadTexture("Textures/Terrain/splat/road.jpg"));
         mat_road.setTexture("NormalMap", assetManager.loadTexture("Textures/Terrain/splat/road.jpg"));
         mat_road.setBoolean("UseMaterialColors",true);    
         mat_road.setColor("Specular",ColorRGBA.White);
         mat_road.setColor("Diffuse",ColorRGBA.White);
         mat_road.setFloat("Shininess", 5f); // [1,128]
 
     }
 
     @Override
     public void simpleRender(RenderManager rm) {
         //TODO: add render code
     }
     
     //Initialize lighting
     private void setUpLight() {
         // We add light so we see the scene and attach to map
         AmbientLight al = new AmbientLight();
         al.setColor(ColorRGBA.White.mult(1.3f));
         rootNode.addLight(al);
     }    
 
     private void setUpKeys() {
         
         //System Keys        
         inputManager.addMapping("Reset",  new KeyTrigger(KeyInput.KEY_P));
         inputManager.addListener(actionListener, "Reset");
         
         
         // Ability keybindings and listener
         inputManager.addMapping("Jump", new KeyTrigger(KeyInput.KEY_SPACE));
         inputManager.addMapping("Dash", new KeyTrigger(KeyInput.KEY_M));
         inputManager.addListener(actionListener, "Jump");
         inputManager.addListener(actionListener, "Dash");
         
         // Player 1 keybindings and listener
         
         inputManager.addMapping("Left", new KeyTrigger(KeyInput.KEY_A));
         inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_D));
         inputManager.addMapping("Up", new KeyTrigger(KeyInput.KEY_W));
         inputManager.addMapping("Down", new KeyTrigger(KeyInput.KEY_S));        
         
         inputManager.addListener(analogListener,new String[]{ "Left","Right", "Up", "Down"});
       
         // Player 2 keybindings and listener
         inputManager.addMapping("Left2", new KeyTrigger(KeyInput.KEY_F));
         inputManager.addMapping("Right2", new KeyTrigger(KeyInput.KEY_H));
         inputManager.addMapping("Up2", new KeyTrigger(KeyInput.KEY_T));
         inputManager.addMapping("Down2", new KeyTrigger(KeyInput.KEY_G));
         
         inputManager.addListener(analogListener,new String[]{ "Left2","Right2", "Up2", "Down2"});        
         
         // Player 3 keybindings and listener
         inputManager.addMapping("Left3", new KeyTrigger(KeyInput.KEY_J));
         inputManager.addMapping("Right3", new KeyTrigger(KeyInput.KEY_L));
         inputManager.addMapping("Up3", new KeyTrigger(KeyInput.KEY_I));
         inputManager.addMapping("Down3", new KeyTrigger(KeyInput.KEY_K));
          
         inputManager.addListener(analogListener,new String[]{ "Left3","Right3", "Up3", "Down3"});        
         
         // Player 4 keybindings and listener
         inputManager.addMapping("Left4", new KeyTrigger(KeyInput.KEY_NUMPAD4));
         inputManager.addMapping("Right4", new KeyTrigger(KeyInput.KEY_NUMPAD6));
         inputManager.addMapping("Up4", new KeyTrigger(KeyInput.KEY_NUMPAD8));
         inputManager.addMapping("Down4", new KeyTrigger(KeyInput.KEY_NUMPAD5));
         
         inputManager.addListener(analogListener,new String[]{ "Left4","Right4", "Up4", "Down4"});
         
     }
     
     private AnalogListener analogListener = new AnalogListener() {
         public void onAnalog(String binding, float value, float tpf) {
            
             // New code: maps ball array (HARDCODED)
             // player 1 action listener
             if (binding.equals("Left")) {                
                     Vector3f v = ball[0].getLocalTranslation();
                     ball[0].setLocalTranslation(v.x + value*speed*3, v.y, v.z);
                     ball[0].rotate(-value*speed*3, 0, 0);
                     ball_phy[0].applyForce(new Vector3f(-3f, 0, 0),new Vector3f(-3f, 0, 0)); // push the ball foward
                 } else if (binding.equals("Right")) {
                     Vector3f v = ball[0].getLocalTranslation();
                     ball[0].setLocalTranslation(v.x - value*speed*3, v.y, v.z);
                     ball[0].rotate(value*speed*3, 0, 0);
                     ball_phy[0].applyForce(new Vector3f(3f, 0, 0),new Vector3f(3f, 0, 0));
                 } else if (binding.equals("Up")) {
                     Vector3f v = ball[0].getLocalTranslation();
                     ball[0].setLocalTranslation(v.x , v.y , v.z + value*speed*3);
                     ball[0].rotate(0, 0, -value*speed*3);
                     ball_phy[0].applyForce(new Vector3f(0, 0, -3f),new Vector3f(0, 0, -3f));
                 } else if (binding.equals("Down")) {
                     Vector3f v = ball[0].getLocalTranslation();
                     ball[0].setLocalTranslation(v.x , v.y, v.z - value*speed*3);
                     ball[0].rotate(0, 0, value*speed*3);
                     ball_phy[0].applyForce(new Vector3f(0, 0, 3f),new Vector3f(0, 0, 3f));
                 }           
             
                 //player 2 action listener
            
               if (binding.equals("Left2")) {                
                     Vector3f v = ball[1].getLocalTranslation();
                     ball[1].setLocalTranslation(v.x + value*speed*3, v.y, v.z);
                     ball[1].rotate(-value*speed*3, 0, 0);
                     ball_phy[1].applyForce(new Vector3f(-3f, 0, 0),new Vector3f(-3f, 0, 0)); // push the ball foward
                 } else if (binding.equals("Right2")) {
                     Vector3f v = ball[1].getLocalTranslation();
                     ball[1].setLocalTranslation(v.x - value*speed*3, v.y, v.z);
                     ball[1].rotate(value*speed*3, 0, 0);
                     ball_phy[1].applyForce(new Vector3f(3f, 0, 0),new Vector3f(3f, 0, 0));
                 } else if (binding.equals("Up2")) {
                     Vector3f v = ball[1].getLocalTranslation();
                     ball[1].setLocalTranslation(v.x , v.y , v.z + value*speed*3);
                     ball[1].rotate(0, 0, -value*speed*3);
                     ball_phy[1].applyForce(new Vector3f(0, 0, -3f),new Vector3f(0, 0, -3f));
                 } else if (binding.equals("Down2")) {
                     Vector3f v = ball[1].getLocalTranslation();
                     ball[1].setLocalTranslation(v.x , v.y, v.z - value*speed*3);
                     ball[1].rotate(0, 0, value*speed*3);
                     ball_phy[1].applyForce(new Vector3f(0, 0, 3f),new Vector3f(0, 0, 3f));
                 }      
               
               //player 3 action listener
               if (binding.equals("Left3")) {                
                     Vector3f v = ball[2].getLocalTranslation();
                     ball[2].setLocalTranslation(v.x + value*speed*3, v.y, v.z);
                     ball[2].rotate(-value*speed*3, 0, 0);
                     ball_phy[2].applyForce(new Vector3f(-3f, 0, 0),new Vector3f(-3f, 0, 0)); // push the ball foward
                 } else if (binding.equals("Right3")) {
                     Vector3f v = ball[2].getLocalTranslation();
                     ball[2].setLocalTranslation(v.x - value*speed*3, v.y, v.z);
                     ball[2].rotate(value*speed*3, 0, 0);
                     ball_phy[2].applyForce(new Vector3f(3f, 0, 0),new Vector3f(3f, 0, 0));
                 } else if (binding.equals("Up3")) {
                     Vector3f v = ball[2].getLocalTranslation();
                     ball[2].setLocalTranslation(v.x , v.y , v.z + value*speed*3);
                     ball[2].rotate(0, 0, -value*speed*3);
                     ball_phy[2].applyForce(new Vector3f(0, 0, -3f),new Vector3f(0, 0, -3f));
                 } else if (binding.equals("Down3")) {
                     Vector3f v = ball[2].getLocalTranslation();
                     ball[2].setLocalTranslation(v.x , v.y, v.z - value*speed*3);
                     ball[2].rotate(0, 0, value*speed*3);
                     ball_phy[2].applyForce(new Vector3f(0, 0, 3f),new Vector3f(0, 0, 3f));
                 } 
                //player 4 action listener
               if (binding.equals("Left4")) {                
                     Vector3f v = ball[3].getLocalTranslation();
                     ball[3].setLocalTranslation(v.x + value*speed*3, v.y, v.z);
                     ball[3].rotate(-value*speed*3, 0, 0);
                     ball_phy[3].applyForce(new Vector3f(-3f, 0, 0),new Vector3f(-3f, 0, 0)); // push the ball foward
                 } else if (binding.equals("Right4")) {
                     Vector3f v = ball[3].getLocalTranslation();
                     ball[3].setLocalTranslation(v.x - value*speed*3, v.y, v.z);
                     ball[3].rotate(value*speed*3, 0, 0);
                     ball_phy[3].applyForce(new Vector3f(3f, 0, 0),new Vector3f(3f, 0, 0));
                 } else if (binding.equals("Up4")) {
                     Vector3f v = ball[3].getLocalTranslation();
                     ball[3].setLocalTranslation(v.x , v.y , v.z + value*speed*3);
                     ball[3].rotate(0, 0, -value*speed*3);
                     ball_phy[3].applyForce(new Vector3f(0, 0, -3f),new Vector3f(0, 0, -3f));
                 } else if (binding.equals("Down4")) {
                     Vector3f v = ball[3].getLocalTranslation();
                     ball[3].setLocalTranslation(v.x , v.y, v.z - value*speed*3);
                     ball[3].rotate(0, 0, value*speed*3);
                     ball_phy[3].applyForce(new Vector3f(0, 0, 3f),new Vector3f(0, 0, 3f));
                 }
 
         }
     };     
     
     private ActionListener actionListener = new ActionListener() {
     public void onAction(String name, boolean keyPressed, float tpf) {
       if (name.equals("Pause") && !keyPressed) {
             isRunning = !isRunning;
       } else if (name.equals("Reset") && !keyPressed){
             System.out.println("RESET");
           
       } else if (name.equals("Jump") && !keyPressed && !onCooldown(p1Abilities,1)) {
           ball_phy[0].applyImpulse(new Vector3f(0, 7f, 0), Vector3f.ZERO );               
       
       } else if(name.equals("Dash") && !keyPressed && !onCooldown(p2Abilities,1)){                       
             Vector3f v = ball_phy[1].getLinearVelocity();
             if(v.x != 0 && v.z!=0){
                 ball_phy[1].applyImpulse(new Vector3f(v.x*20,0,v.z*20), Vector3f.ZERO);
             }
       }
     }
   };    
 
     
     // Listens to collision between objects
     private PhysicsCollisionListener physicsCollisionListener = new PhysicsCollisionListener(){        
         public void collision(PhysicsCollisionEvent event) {
             if (event.getNodeA().getName().equals("Ball1") || event.getNodeB().getName().equals("Ball1l")) {                
             if (event.getNodeA().getName().equals("Ball2") || event.getNodeB().getName().equals("Ball2")) {
                 System.out.println("Ouch my balls!!!!!!!!"); 
             }    
             }
         }
     };  
 
     
     @Override
     public void simpleUpdate(float tpf) {
       
       //Check if ball is within the map; if not, destroy the ball
 
       Vector3f loc1 = ball_phy[0].getPhysicsLocation();
       Vector3f loc2 = ball_phy[1].getPhysicsLocation();
       Vector3f loc3 = ball_phy[2].getPhysicsLocation();
       Vector3f loc4 = ball_phy[3].getPhysicsLocation();
       
       if(loc1.y < 0 && isBall1Alive){
           System.out.println("Ball 1 DEAD");   
           isBall1Alive = false;           
 
               if(!isBall2Alive || !isBall3Alive || !isBall4Alive){
                 ch.detachAllChildren();
               }
 
           ch = new BitmapText(guiFont, false);  
           ch.setSize(guiFont.getCharSet().getRenderedSize() * 5);
           ch.setText("Ball 1 DEAD");       
           ch.setColor(ColorRGBA.Red);
           ch.setLocalTranslation(
           settings.getWidth() / 3f,
           settings.getHeight() / 4f, 0);
           guiNode.attachChild(ch);
           bulletAppState.getPhysicsSpace().remove(ball_phy[0]);
           rootNode.detachChild(ball[0]);
       } else if(loc2.y < 0 && isBall2Alive){
           System.out.println("Ball 2 Dead");
           isBall2Alive = false;            
 
               if(!isBall1Alive || !isBall3Alive || !isBall4Alive){
                 ch.detachAllChildren();
               }
 
           ch = new BitmapText(guiFont, false); 
           ch.setSize(guiFont.getCharSet().getRenderedSize() * 5);
           ch.setText("Ball 2 DEAD");        
           ch.setColor(ColorRGBA.Red);
           ch.setLocalTranslation(
           settings.getWidth() / 3f,
           settings.getHeight() / 4f, 0);
           guiNode.attachChild(ch);
           bulletAppState.getPhysicsSpace().remove(ball_phy[1]);
           rootNode.detachChild(ball[1]);
       } else if(loc3.y < 0 && isBall3Alive){
           System.out.println("Ball 3 Dead");
           isBall3Alive = false;            
 
               if(!isBall2Alive || !isBall1Alive || !isBall4Alive){
                 ch.detachAllChildren();
               }
 
           ch = new BitmapText(guiFont, false); 
           ch.setSize(guiFont.getCharSet().getRenderedSize() * 5);
           ch.setText("Ball 3 DEAD");        
           ch.setColor(ColorRGBA.Red);
           ch.setLocalTranslation(
           settings.getWidth() / 3f,
           settings.getHeight() / 4f, 0);
           guiNode.attachChild(ch);
           bulletAppState.getPhysicsSpace().remove(ball_phy[2]);
           rootNode.detachChild(ball[2]);
       
          } else if(loc4.y < 0 && isBall4Alive){
           System.out.println("Ball 4 Dead");
           isBall4Alive = false;      
 
             if(!isBall2Alive || !isBall1Alive || !isBall3Alive){
                 ch.detachAllChildren();
             }
 
           ch = new BitmapText(guiFont, false); 
           ch.setSize(guiFont.getCharSet().getRenderedSize() * 5);
           ch.setText("Ball 4 DEAD");        
           ch.setColor(ColorRGBA.Red);
           ch.setLocalTranslation(
           settings.getWidth() / 3f,
           settings.getHeight() / 4f, 0);
           guiNode.attachChild(ch);
           bulletAppState.getPhysicsSpace().remove(ball_phy[3]);
           rootNode.detachChild(ball[3]);
 
          }      
       
       if(!isBall1Alive && !isBall2Alive && !isBall3Alive && !isBall4Alive && !gameEnd){
           gameEnd = true;
           destroyObj();
           InitObj();
       }
        deathTimer();
   }
     
     // destroy objects
     private void destroyObj(){
         rootNode.detachAllChildren();
         counter = 0;
         ch.detachAllChildren();        
     }    
     
      public void initAbilities(BigInteger[] temp) {
         for (int i = 0; i < abilitySize; i++) {
             temp[i] = new BigInteger(String.valueOf(System.nanoTime()));
         }
     }
     
     public boolean onCooldown(BigInteger[] temp, int i) {
         if (temp[i].compareTo(new BigInteger(String.valueOf(System.nanoTime()))) < 0) {
             temp[i] = new BigInteger(String.valueOf(System.nanoTime())).add(
                     new BigInteger("5000000000"));
             return false;
         }
         else {
             return true;
         }
     }
       public void createFloor(int j) {
         int i = 0;
         int x = boardLength-1;
         int y = boardWidth-1;
         int dx = x;
         int dy = y - 1;
         int xlim = 0;
         int ylim = 0;
         int check = 0;
         Vector3f coord = new Vector3f(0,0,0);
         
         //populate populate map in a spiral fashion
         while (i < boardLength*boardWidth) {
             //Create a Tile
             makeMapTile(coord.set((boardLength-x-1), -.1f, 
                     (boardWidth-y-1)), i++);
         
         //Create Obj array if current map has objects to load    
         if (currentmap > 1) {
             mapObj = new Spatial[20];
             objNum = 0;
         }
             
             /*Algorithm for spiral: Move straight until 
              * it reaches a corner, then turn Right and continue 
              * until all blocks filled*/
             if (check == 0) {
                 if (x == xlim) {
                     check = 1;
                     y--;
                     continue;
                 }
                 x--;
             }
             else if (check == 1) {
                 if (y == ylim){
                     check = 2;
                     x++;
                     continue;
                 }
                 y--;
             }
             else if (check == 2) {
                 if (x == dx) {
                     check = 3;
                     y++;
                     dx--;
                     xlim++;
                     continue;
                 }
                 x++;
             }
             else if (check == 3) {
                 if (y == dy) {
                     check = 0;
                     x--;
                     dy--;
                     ylim++;
                     continue;
                 }
                 y++;
             }
         }
         objNum = 0;
     }
     
     //create a Map tile with randomly generated terrain to attach to Map
     public void makeMapTile(Vector3f loc, int i) {
         Box box2 = new Box( Vector3f.ZERO, 1f,.1f,1f);
         Geometry tile = new Geometry("Box", box2);
         Material mat2 = new Material(assetManager,
                 "Common/MatDefs/Misc/Unshaded.j3md");
         TextureKey key = loadTexture(maptexture[i]);
         key.setGenerateMips(true);
         Texture tex = assetManager.loadTexture(key);
         tex.setWrap(Texture.WrapMode.Repeat);
         mat2.setTexture("ColorMap",tex);
         tile.setMaterial(mat2);
         tile.setLocalTranslation(loc.mult(2));
         tile.addControl(new RigidBodyControl(0.0f));
         tile.getControl(RigidBodyControl.class).setKinematic(false);
         bulletAppState.getPhysicsSpace().add(tile);
         rootNode.attachChildAt(tile, i);
         Vector3f objcoord = new Vector3f(loc.x,loc.y,loc.z);
         loadObject(loc,mapobjects[i],tile);
 
     }
     
     //Load the map we want
     public int[] selectedMapTexture(int i) {
         int[] temp;
         if (i == 0) {
             temp = map1;
             boardLength = 12;
             boardWidth = 8;
         }
         else if (i == 1) {
             temp = map2;
             boardLength = 11;
             boardWidth = 8;
         }
         else if (i == 2) {
             temp = map3;
             boardLength = 11;
             boardWidth = 8;
         }
         else if (i == 3) {
             temp = map4;
             boardLength = 11;
             boardWidth = 8;
         }
         else {
             temp = map5;
             boardLength = 11;
             boardWidth = 8;
         }
         return temp;
     }
     
     //Load the object map we want
     public int[] selectedMapObject(int i) {
         int[] temp;
         if (i == 0) {
             temp = obj1;
         }
         else if (i == 1) {
             temp = obj2;
         }
         else if (i == 2) {
             temp = obj3;
         }
         else if (i == 3) {
             temp = obj4;
         }
         else {
             temp = obj5;
         }
         return temp;
     }
     
     //Return the texture for the map
     public TextureKey loadTexture(int i) {
         TextureKey key;
         if (i == 0) {
             key = new TextureKey("Textures/Terrain/BrickWall/BrickWall.jpg");
         }
         else if (i == 1) {
             key = new TextureKey("Textures/Terrain/Pond/Pond.jpg");
         }
         else if (i == 2) {
             key = new TextureKey("Textures/lava.png");            
         }
         else {
             key = new TextureKey("Textures/Terrain/splat/dirt.jpg");
         }
         return key;
     }
     
         //Attach an object to the if it exsist on the selected map
     public void loadObject(Vector3f loc, int i, Geometry j) {
         Spatial temp;
         if (i == 4) {
             temp = assetManager.loadModel("Models/boulder_02.j3o");
             temp.scale(.4f);
         }
         else if (i == 5) {
             temp = assetManager.loadModel("Models/ground_pallisade.j3o");
             temp.scale(2f);
         }
         else if (i == 6) {
             temp = assetManager.loadModel("Models/ramp.j3o");
             temp.rotate(0, 29.8f, 0);
         }
         else if (i == 7) {
             temp = assetManager.loadModel("Models/ramp.j3o");
             temp.rotate(0, 45.55f, 0);
         }
         else if (i == 8) {
             temp = assetManager.loadModel("Models/barrel.j3o");
             temp.scale(2);
         }
         else {
             return;
         }
         
         temp.setLocalTranslation(loc.mult(2));
         mapObj[objNum++] = temp;
         rootNode.attachChild(temp);
         temp.addControl(new RigidBodyControl(0f));
         bulletAppState.getPhysicsSpace().add(temp.getControl(RigidBodyControl.class));
     }
     
     //Destory Object on the map when tile is destoryed
     public void onDestroy(int i) {
         if (mapobjects[i] > 0 && currentmap > 1) {
             mapObj[objNum].getControl(RigidBodyControl.class).setMass(1f);
             mapObj[objNum].getControl(RigidBodyControl.class).setGravity(
                 new Vector3f(0f,-10f,0f));
             mapObj[objNum].getControl(RigidBodyControl.class).activate();
             objNum++;
         }
     }
     
     //Destory a piece of the Map
     public void destroyMap(int i) {
         rootNode.getChild(i).getControl(RigidBodyControl.class).setMass(1f);
         rootNode.getChild(i).getControl(RigidBodyControl.class).setGravity(
                 new Vector3f(0f,-10f,0f));
         rootNode.getChild(i).getControl(RigidBodyControl.class).activate();
         onDestroy(i);
     }
     
     //Timer to destory the Map
     public void deathTimer() {
         if (deathClk.compareTo(new BigInteger(String.valueOf(
                 System.nanoTime()))) < 0 && counter < boardLength*boardWidth) {
             destroyMap(counter);
             deathClk = new BigInteger(String.valueOf(System.nanoTime())).add(
                     new BigInteger("5000000000"));
             System.out.println(counter);
             counter++;
             
         }
     }
 
 }
