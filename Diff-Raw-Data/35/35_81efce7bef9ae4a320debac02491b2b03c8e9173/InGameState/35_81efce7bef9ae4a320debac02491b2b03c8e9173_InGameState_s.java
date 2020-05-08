 package gamestate;
 
 import com.jme3.app.Application;
 import com.jme3.app.SimpleApplication;
 import com.jme3.app.state.AbstractAppState;
 import com.jme3.app.state.AppStateManager;
 import com.jme3.asset.AssetManager;
 import com.jme3.bullet.BulletAppState;
 import com.jme3.input.ChaseCamera;
 import com.jme3.input.InputManager;
 import com.jme3.input.KeyInput;
 import com.jme3.input.MouseInput;
 import com.jme3.input.controls.ActionListener;
 import com.jme3.input.controls.KeyTrigger;
 import com.jme3.input.controls.MouseButtonTrigger;
 import com.jme3.light.DirectionalLight;
 import com.jme3.light.SpotLight;
 import com.jme3.material.Material;
 import com.jme3.math.ColorRGBA;
 import com.jme3.math.FastMath;
 import com.jme3.math.Vector3f;
 import com.jme3.renderer.ViewPort;
 import com.jme3.scene.Node;
 import com.jme3.scene.Spatial;
 import com.jme3.scene.control.LightControl;
 import com.jme3.scene.shape.Box;
 import java.util.LinkedList;
 import variables.P;
 
 /**
  * This class handles all the in-game things
  *
  * @author forssenm
  */
 public class InGameState extends AbstractAppState {
 
     private SimpleApplication app;
     private Node inGameRootNode;
     private AssetManager assetManager;
     private AppStateManager stateManager;
     private InputManager inputManager;
     private ViewPort viewPort;
     private BulletAppState physics;
     
     private PlatformController platformController;
     
     private Player player;
     
     private Vector3f playerLightPosition;
     private SpotLight playerSpot;
     private Vector3f directionToPlayer;
 
     /**
      * This method initializes the the InGameState
      *
      * @param stateManager
      * @param app
      */
     @Override
     public void initialize(AppStateManager stateManager, Application app) {
         super.initialize(stateManager, app);
         this.app = (SimpleApplication) app;
         this.inGameRootNode = new Node();
         this.app.getRootNode().attachChild(this.inGameRootNode);
         this.assetManager = this.app.getAssetManager();
         this.stateManager = this.app.getStateManager();
         this.inputManager = this.app.getInputManager();
         this.viewPort = this.app.getViewPort();
         this.physics = new BulletAppState();
         this.stateManager.attach(physics);
         
         setup();
     }
 
     @Override
     public void cleanup() {
         super.cleanup();
         this.app.getRootNode().detachChild(this.inGameRootNode);
     }
 
     @Override
     public void setEnabled(boolean enabled) {
         super.setEnabled(enabled);
         if (enabled) {
             //Initiate the things that are needed when the state is active
             System.out.println("InGameState is now active");
         } else {
             //Remove the things not needed when the state is inactive
             System.out.println("InGameState is now inactive");
         }
     }
 
     @Override
     public void update(float tpf) {
         directionToPlayer = player.getSpatial().getLocalTranslation().
                 subtract(playerLightPosition);
         playerSpot.setDirection(directionToPlayer);
     }
 
     private void setup() {
         PlatformFactory platformFactory = new PlatformFactory(
                 this.assetManager,
                 this.inGameRootNode,
                 this.physics.getPhysicsSpace());
         this.platformController = new PlatformController(platformFactory);
 
 
         // Create player and attach it to the Scene Graph and Physics Space
         this.player = new Player(new PlayerSceneObjectDataSource(this.assetManager));
         this.player.addToNode(inGameRootNode);
         this.player.addToPhysicsSpace(physics.getPhysicsSpace());
         initCamera();
         initInputs();
         initLights();
 
     }
     
     
      private ChaseCamera chaseCam;
      
      private void initCamera() {
         this.app.getFlyByCamera().setEnabled(false);
         this.chaseCam = new ChaseCamera(this.app.getCamera(), this.player.spatial, this.inputManager);
         //this.chaseCam.setSmoothMotion(true);
         this.chaseCam.setTrailingEnabled(false);
         this.chaseCam.setDefaultHorizontalRotation(-FastMath.DEG_TO_RAD * 270);
         this.chaseCam.setDefaultDistance(25);
      }
      
 
     
 
     private void initLights() {
         DirectionalLight sun = new DirectionalLight();
         sun.setColor(ColorRGBA.White);
         sun.setDirection(new Vector3f(-.5f, -.5f, -.5f).normalizeLocal());
         inGameRootNode.addLight(sun);
 
         playerSpot = new SpotLight();
         SpotLight backwardSpot = new SpotLight();
         playerSpot.setSpotRange(1000f);                           // distance
         playerSpot.setSpotInnerAngle(10f * FastMath.DEG_TO_RAD); // inner light cone (central beam)
         playerSpot.setSpotOuterAngle(15f * FastMath.DEG_TO_RAD); // outer light cone (edge of the light)
         playerSpot.setColor(ColorRGBA.White.mult(1.3f));         // light color
 
                 playerLightPosition = new Vector3f(100f, 50f, 0f);
         playerSpot.setPosition(playerLightPosition);
         
         // find the direction to shine in
         directionToPlayer = player.getSpatial().getLocalTranslation().
                 subtract(playerLightPosition);
         playerSpot.setDirection(directionToPlayer);
                 
         inGameRootNode.addLight(playerSpot);
 
         backwardSpot.setSpotRange(100f);                           // distance
         backwardSpot.setSpotOuterAngle(35f * FastMath.DEG_TO_RAD); // outer light cone (edge of the light)
         backwardSpot.setSpotInnerAngle(15f * FastMath.DEG_TO_RAD); // inner light cone (central beam)
         backwardSpot.setColor(ColorRGBA.White.mult(1.3f));         // light color
         backwardSpot.setPosition(new Vector3f(200f, 30f, 0f));    // shine from above end
         backwardSpot.setDirection(new Vector3f(-200f, -30f, 0f));             // shine along path
 
 
         //inGameRootNode.addLight(backwardSpot);
     }
 
     
     /**
      * Sets up the input. Mouseclick jumps the character.
      */
     private void initInputs() {
         inputManager.addMapping("jump",
                 new KeyTrigger(KeyInput.KEY_SPACE));
         
         inputManager.addMapping("jump",
                 new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
         inputManager.addListener(actionListener, "jump");
     }
     
     private ActionListener actionListener = new ActionListener() {
         public void onAction(String binding, boolean value, float tpf) {
             if (binding.equals("jump")) {
                 player.jump();
             }
         }
     };
 }
