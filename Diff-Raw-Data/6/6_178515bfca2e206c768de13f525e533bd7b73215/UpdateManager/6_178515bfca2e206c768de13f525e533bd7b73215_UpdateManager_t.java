 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package update;
 
 import game.Game;
 import game.GameObject;
 import game.StandardManager;
 import java.util.ArrayList;
 import java.util.Iterator;
 import org.lwjgl.Sys;
 import util.DebugMessages;
 
 /**
  *
  * @author Andy
  */
 public class UpdateManager extends StandardManager implements Runnable {
 
     long lastTime;
     ArrayList<Updateable> entities;
     private long updateTime;
     private Thread updateThread;
     static final int defaultUpdateTime = 1000 / 60;
     
     static UpdateManager instance;
 
     @Override
     public void create() {
         super.create();
         updateThread = new Thread(this);
         this.updateTime = defaultUpdateTime;
         lastTime = getTime();
         entities = new ArrayList<Updateable>();
     }
 
     @Override
     public void destroy() {
         super.destroy();
         try {
             updateThread.join();
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 
     public static UpdateManager getInstance() {
         if(instance == null) {
             instance = new UpdateManager();
         }
         return instance;
     }
         
     public void start() {
         updateThread.start();
     }
     
     @Override
     public void run() {
         while (Game.isRunning()) {
             DebugMessages.getInstance().write("UpdateManager Running");
             long currentTime = getTime();
             long deltaTime = currentTime - lastTime;
             update((int) deltaTime);
             lastTime = currentTime;
             try {
                 Thread.sleep(updateTime);
             } catch (Exception e) {
                 e.printStackTrace();
             }
         }
     }
     
     public void update(int delta) {
         DebugMessages.getInstance().write("Delta: " + delta + "  FPS: " + (1000 / delta));
         DebugMessages.getInstance().write("Updates starting");
         
         input.InputManager.getInstance().processInputs();
         
         ArrayList<Updateable> copy = new ArrayList<Updateable>(entities);
         ArrayList<Updateable> toRemove = new ArrayList<Updateable>();
         for(Updateable u : copy) {
             if(u.update(delta)) {
                 toRemove.add(u);
             }
         }
         for(Updateable u : toRemove) {
             remove(u);
         }
         
         DebugMessages.getInstance().write("Updates finished");
     }
 
     public static long getTime() {
         return (Sys.getTime() * 1000) / Sys.getTimerResolution();
     }
 
     @Override
     public boolean add(GameObject obj) {
         if (obj instanceof Updateable) {
             entities.add((Updateable)obj);
             return true;
         }
         return false;
     }
     
    public boolean add(Updateable u) {

        entities.add(u);
        return true;
    }
    
     @Override
     public void remove(GameObject obj) {
         if(entities.contains(obj)) {
             entities.remove(obj);
         }
     }
     
     public void remove(Updateable obj) {
         if (entities.contains(obj)) {
             entities.remove(obj);
         }
     }
 
 }
