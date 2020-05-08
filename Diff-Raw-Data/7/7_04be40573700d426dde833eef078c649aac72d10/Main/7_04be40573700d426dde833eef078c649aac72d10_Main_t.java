 package Core;
 
 import com.jme3.app.SimpleApplication;
 import com.jme3.renderer.RenderManager;
 
 import World.createWorld;
 import com.jme3.system.AppSettings;
 
 public class Main extends SimpleApplication {
 
     public static void main(String[] args) {
         AppSettings settings = new AppSettings(true);
         settings.setResolution(1440,780);
         Main app = new Main();
         app.setSettings(settings);
         app.setShowSettings(false);
         app.start();
     }
 
     @Override
     public void simpleInitApp() {
         //commit test
         //temp for viewing terrain better
         flyCam.setMoveSpeed(500);
         //you need to pass things like assetmanaber and rootnode =/
         createWorld world = new createWorld("test", assetManager, rootNode);
     }
 
     @Override
     public void simpleUpdate(float tpf) {
         //TODO: add update code
     }
 
     @Override
     public void simpleRender(RenderManager rm) {
         //TODO: add render code
     }
 }
