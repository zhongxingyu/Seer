 package state;
 
 import com.jme3.app.Application;
 import com.jme3.app.state.AbstractAppState;
 import com.jme3.app.state.AppStateManager;
 import com.jme3.asset.AssetManager;
 import com.jme3.bullet.BulletAppState;
 import com.jme3.input.InputManager;
 import com.jme3.niftygui.NiftyJmeDisplay;
 import com.jme3.renderer.ViewPort;
 import com.jme3.scene.Node;
 import de.lessvoid.nifty.Nifty;
 import de.lessvoid.nifty.NiftyEventSubscriber;
 import de.lessvoid.nifty.elements.events.NiftyMousePrimaryClickedEvent;
 import main.Main;
 import niftyController.MainMenuScreenController;
 
 /**
  * This class handles all the menu things
  *
  * @author forssenm
  */
 public class InMainMenuState extends AbstractAppState {
 
     private Main app;
     private Node rootNode;
     private AssetManager assetManager;
     private AppStateManager stateManager;
     private InputManager inputManager;
     private ViewPort viewPort;
     private BulletAppState physics;
     private Nifty nifty;
 
     /**
      * This method should initialize the main menu screen
      *
      * @param stateManager
      * @param app
      */
     @Override
     public void initialize(AppStateManager stateManager, Application app) {
         super.initialize(stateManager, app);
         this.app = (Main) app;
         this.rootNode = this.app.getRootNode();
         this.assetManager = this.app.getAssetManager();
         this.stateManager = this.app.getStateManager();
         this.inputManager = this.app.getInputManager();
         this.viewPort = this.app.getViewPort();
         this.physics = this.stateManager.getState(BulletAppState.class);
 //        setEnabled(true);
         loadGui();
     }
 
     @Override
     public void cleanup() {
         super.cleanup();
     }
 
     @Override
     public void setEnabled(boolean enabled) {
         super.setEnabled(enabled);
         if (enabled) {
             //Initiate the things that are needed when the state is active
             System.out.println("InMainMenuState is now active");
            nifty.setIgnoreMouseEvents(false);
             nifty.gotoScreen("gameOverScreen");
         } else {
             //Remove the things not needed when the state is inactive
             System.out.println("InMainMenuState is now inactive");
            nifty.setIgnoreMouseEvents(true); // needed to prevent nullpointer from releasing the click too quickly when closing nifty
            nifty.exit();
         }
     }
 
     @Override
     public void update(float tpf) {
         //this.setEnabled(false);
         //stateManager.attach(new InGameState());
     }
 
     private void loadGui() {
         NiftyJmeDisplay niftyDisplay = new NiftyJmeDisplay(assetManager, inputManager, app.getAudioRenderer(), app.getGuiViewPort());
         nifty = niftyDisplay.getNifty();
         nifty.subscribeAnnotations(this);
         nifty.fromXml("/xmlgui/Screens.xml", "splashScreen",new MainMenuScreenController());
         //nifty.fromXml("/xmlgui/SplashScreen.xml", "splashScreen");
         app.getGuiViewPort().addProcessor(niftyDisplay);
     }
 
     //Might need to move these methods to the controllers
     @NiftyEventSubscriber(id = "playButton")
     public void onPlayClick(String id, NiftyMousePrimaryClickedEvent event) {
         this.app.gameStart();
     }
 
     @NiftyEventSubscriber(id = "settingsButton")
     public void onSettingsClick(String id, NiftyMousePrimaryClickedEvent event) {
         nifty.gotoScreen("settingsScreen");
     }
 
     @NiftyEventSubscriber(id = "highscoreButton")
     public void onSHighscoreClick(String id, NiftyMousePrimaryClickedEvent event) {
         nifty.gotoScreen("highscoreScreen");
     }
 
     @NiftyEventSubscriber(id = "exitButton")
     public void onExitClick(String id, NiftyMousePrimaryClickedEvent event) {
         this.app.stop();
     }
 
     @NiftyEventSubscriber(id = "backButton")
     public void onBackClick(String id, NiftyMousePrimaryClickedEvent event) {
         nifty.gotoScreen("mainMenuScreen");
     }
 }
