 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package mygame;
 
 import com.jme3.app.SimpleApplication;
 import com.jme3.input.KeyInput;
 import com.jme3.input.controls.AnalogListener;
 import com.jme3.input.controls.KeyTrigger;
 import com.jme3.light.AmbientLight;
 import com.jme3.light.DirectionalLight;
 import com.jme3.math.Vector3f;
 import com.jme3.renderer.RenderManager;
 import com.jme3.scene.Spatial;
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.Socket;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import mygame.Networking.ClientConnection;
 
 /**
  * test
  * @author Kuchinawa
  */
 public class Main extends SimpleApplication {
     
     ClientConnection client;
     List<List<Float>> updateList;
     
     List<Spatial> ships;
     List<Spatial> containers;
     List<Spatial> AGVs;
     
     
     public static void main(String[] args) {
         Main app = new Main();
         
         app.start();
     }
 
     @Override
     public void simpleInitApp() {
         Logger.getLogger("").setLevel(Level.SEVERE);
         
         client = new ClientConnection();
         updateList = new ArrayList<List<Float>>();
         
         setPauseOnLostFocus(false);
         flyCam.setMoveSpeed(100);
         
         //Load Ships
         ships = new ArrayList<Spatial>();
         
         Spatial ship = assetManager.loadModel("Models/Vlag/Ship.mesh.xml");
         ship.scale(0.05f, 0.05f, 0.05f);
         ship.rotate(0.0f, 0.0f, 0.0f);
         ship.setLocalTranslation(0.0f, 0.0f, 0.f);
         ship.move(25,-25,-50);
         ship.addLight(new AmbientLight());
         ships.add(ship);
         
         for(Spatial sShip : ships)
             rootNode.attachChild(sShip);
         
         //Load Containers
         containers = new ArrayList<Spatial>();
         
         for(int i = 0; i < 21; i++)
             for(int j = 0; j < 6; j++)
                 for(int k = 0; k < 16; k++)
                 {
                     Spatial container = assetManager.loadModel("Models/Container/Container.mesh.xml");
                     container.scale(0.05f, 0.05f, 0.05f);
                     container.rotate(0.0f, 0.0f, 0.0f);
                     container.setLocalTranslation(0.0f, 0.0f, 0.f);
                     container.move(ship.getLocalTranslation().x + 2*i ,ship.getLocalTranslation().y +  2*j , ship.getLocalTranslation().z + 8*k);
                     container.addLight(new AmbientLight());
                     containers.add(container);
                 }
         
         for(Spatial cont : containers)
             rootNode.attachChild(cont);
         
         
         //Load AGVs
         AGVs = new ArrayList<Spatial>();
         
         for(int i = 0; i < 150; i++)
         {
             Spatial AGV = assetManager.loadModel("Models/AGV/AGV.mesh.xml");
             AGV.scale(0.05f, 0.05f, 0.05f);
             AGV.rotate(0,0,0);
             AGV.setLocalTranslation(0.0f, 0.0f, 0.0f);
             AGV.move((-10 -(2*i)), 0, 0);
             AGV.addLight(new AmbientLight());
             AGVs.add(AGV);
         }
 
         for(Spatial AGV : AGVs)
             rootNode.attachChild(AGV);
 
         initKeys();
         // You must add a light to make the model visible
         DirectionalLight sun = new DirectionalLight();
         sun.setDirection(new Vector3f(-0.1f, -0.7f, -1.0f));
         rootNode.addLight(sun);
     }
     
     /** Custom Keybinding: Map named actions to inputs. */
     private void initKeys() {
         
         // You can map one or several inputs to one named action
         inputManager.addMapping("Up",  new KeyTrigger(KeyInput.KEY_I));
         inputManager.addMapping("Left",   new KeyTrigger(KeyInput.KEY_J));
         inputManager.addMapping("Right",  new KeyTrigger(KeyInput.KEY_L));
         inputManager.addMapping("Down", new KeyTrigger(KeyInput.KEY_K));
         
         // Add the names to the action listener.
         inputManager.addListener(analogListener, new String[]{"Up", "Left", "Right", "Down"});
     }
     
     private AnalogListener analogListener = new AnalogListener() {
         public void onAnalog(String name, float value, float tpf) {
             if (name.equals("Up")) {
               //ship.move(new Vector3f(0,0,1));
               //for(Spatial container : containers)
               //    container.move(new Vector3f(0,0,1));
             }
             if (name.equals("Right")) {
               //ship.move(new Vector3f(1,0,0));
             }
             if (name.equals("Left")) {
               //ship.move(new Vector3f(-1,0,0));
             }
             if (name.equals("Down")) {
               //ship.move(new Vector3f(0,0,-1));
             }
         }
     };
     
     float updateTimeElapsed = 0;
     long systemTimeElapsed = 0;
     long previousUpdate = -1;
     int frameCount = 0;
     
     @Override
     public void simpleUpdate(float tpf) {
         //TODO: add update code
         
         updateTimeElapsed += tpf;
         
         if(updateTimeElapsed > 1)
         {
            List<String> testList = client.getServerUpdate(updateList.size());
         
             if(!testList.isEmpty())
                 for(Spatial container : containers)
                    container.move(0,0, Integer.parseInt(testList.get(0)));
             
             updateTimeElapsed = 0;
         }
     }
 
     
     
     
     @Override
     public void simpleRender(RenderManager rm) {
         //TODO: add render code
     }
 }
