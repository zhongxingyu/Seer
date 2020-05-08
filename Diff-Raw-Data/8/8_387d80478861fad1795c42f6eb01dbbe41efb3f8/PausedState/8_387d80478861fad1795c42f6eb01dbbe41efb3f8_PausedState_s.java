 
 package com.teamawesome.testing;
 
 import com.jme3.app.Application;
 import com.jme3.app.SimpleApplication;
 
 import com.jme3.app.state.AbstractAppState;
 import com.jme3.app.state.AppStateManager;
 import com.jme3.asset.AssetManager;
 import com.jme3.font.BitmapText;
 import com.jme3.input.InputManager;
 import com.jme3.input.KeyInput;
 import com.jme3.input.controls.ActionListener;
 import com.jme3.input.controls.KeyTrigger;
 import com.jme3.renderer.ViewPort;
 import com.jme3.scene.Node;
 import com.jme3.scene.Spatial.CullHint;
 import com.jme3.system.AppSettings;
 import com.jme3.ui.Picture;
 
 /**
  *
  * @author kaizokuace
  */
 public class PausedState extends AbstractAppState {
     
     private SimpleApplication app;
     private Node              rootNode;
     private AssetManager      assetManager;
     private AppStateManager   stateManager;
     private InputManager      inputManager;
     private ViewPort          viewPort;
     private Node              guiNode;
     private AppSettings       settings;
 
     public void setSettings(AppSettings s) {
         this.settings = s;
     }
     
 
     public PausedState() {
     }
     
     @Override
     public void cleanup() {
         super.cleanup();
     }
 
     @Override
     public void initialize(AppStateManager stateManager, Application app) {
         super.initialize(stateManager, app);
         this.app = (SimpleApplication) app; // can cast Application to something more specific
         this.rootNode     = this.app.getRootNode();
         this.assetManager = this.app.getAssetManager();
         this.stateManager = this.app.getStateManager();
         this.inputManager = this.app.getInputManager();
         this.viewPort     = this.app.getViewPort();
         this.guiNode      = this.app.getGuiNode();
         
         stateManager.getState(PausedState.class).setEnabled(false);
         System.out.println("PausedState Initialized");
     }
 
     @Override
     public void setEnabled(boolean enabled) {
         super.setEnabled(enabled);
         if(enabled){
             System.out.println("PausedState enabled");
             
             guiNode.detachAllChildren();
             Picture pic = new Picture("HUD Picture");
             pic.setImage(assetManager, "Interface/background.png", true);
             pic.setWidth(settings.getWidth()/2);
             pic.setHeight(settings.getHeight()/2);
             pic.setPosition(settings.getWidth()/4, settings.getHeight()/4);
             guiNode.attachChild(pic);
             
 
         
         ActionListener actionListener = new ActionListener() {
             float x = 0;
             float y = 0;
             public void onAction(String name, boolean keyPressed, float tpf) {
                  if ("Pause Game".equals(name) && !keyPressed) {
                      PausedState.this.stateManager.getState(PausedState.class).setEnabled(false);
                      PausedState.this.stateManager.getState(RunningState.class).setEnabled(true);
                      guiNode.detachAllChildren();
                      System.out.println("PausedState disabled");
                      inputManager.removeListener(this);
                  
                  }
                  if ("Drop Block".equals(name) && !keyPressed) System.out.println("Drop pushed");
                 if ("Move Block Left".equals(name) && !keyPressed)   rootNode.getChild("A Textured Box").setLocalTranslation(x -= 2.5, y, 0);
                 if ("Move Block Right".equals(name) && !keyPressed)   rootNode.getChild("A Textured Box").setLocalTranslation(x += 2.5, y, 0);
                 if ("Move Block Up".equals(name) && !keyPressed)   rootNode.getChild("A Textured Box").setLocalTranslation(x, y += 2.5, 0);
                 if ("Move Block Down".equals(name) && !keyPressed)   rootNode.getChild("A Textured Box").setLocalTranslation(x, y -= 2.5, 0);
               }
             };
         
         inputManager.addListener(actionListener, new String[]{"Pause Game","Drop Block","Move Block Right", 
                                                             "Move Block Left", "Move Block Up", "Move Block Down"});
         
         }
         else{
         
         }
     }
 
     @Override
     public void update(float tpf) {
         super.update(tpf);
     }
     
 }
