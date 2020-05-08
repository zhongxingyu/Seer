 package Core;
 
 import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
 import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
 
 import World.createWorld;
 import com.jme3.system.AppSettings;
import jme3tools.optimize.GeometryBatchFactory;
 
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
