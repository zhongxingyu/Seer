 package game;
 
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.TimeUnit;
 
 import actor.ActorSet;
 
 public class GameThread extends Thread {
     private static final long FRAME_RATE = 1000 / 60;
     private static final long COLLISION_TIME = FRAME_RATE * 9 /10; // arbitrarily 90% maximum
     private static final int CORE_SUBSCRIPTION_FACTOR = 4;
     private long lastUpdate ;
     private int maxThreads;
     private physics.CollisionSolverThread[] collisionThreads;
     private ExecutorService pool;
     private ActorSet actors;
 
 
     public GameThread(ActorSet actors){
         this.actors = actors;
         lastUpdate = 0;
         maxThreads = Runtime.getRuntime().availableProcessors() * CORE_SUBSCRIPTION_FACTOR;
         pool = java.util.concurrent.Executors.newFixedThreadPool(maxThreads);
         collisionThreads = new physics.CollisionSolverThread[maxThreads];
     }
 
     public void run() {
         for(int i = 0; i < collisionThreads.length; ++i)
             collisionThreads[i] = new physics.CollisionSolverThread(actors, i, collisionThreads.length);
         while (true) {
             int frames = waitForNextFrame();
 
             // CL - We need to get input even if the game is paused,
             //      that way we can unpause the game.
             game.Game.getInputHandler().update();
 
             // Don't update the game state if we are paused
             if(game.Game.isPaused())
                 continue;
 
             game.Game.getPlayer().updateCamera();
 
 
             for(int i = 0; i < collisionThreads.length; ++i)
                 pool.execute(collisionThreads[i]);
             
             
             // Give the collision threads as much time as we can, but this may cull them prematurely
             try {
                 pool.awaitTermination(COLLISION_TIME, TimeUnit.MILLISECONDS);
             } catch (InterruptedException e) {
                 e.printStackTrace();
             }
 
 
             // Update the actors
             actors.update(frames);
         }
 
     }
 
     /**
      * This acts as a frame rate controller for our game logic
      * @return the number of frames elapsed.
      */
     private int waitForNextFrame() {
         long current = System.currentTimeMillis();
         // handle first frame case
         if (lastUpdate == 0) {
             lastUpdate = current;
             return 1;
         }
 
         long elapsed = current - lastUpdate;
 
         int frames = (int)(elapsed / FRAME_RATE) + 1;
         long delay = FRAME_RATE * frames - elapsed;
 
         try {
             sleep(delay);
         } catch (InterruptedException e) {
             e.printStackTrace();
         }
 
         return frames;
     }
 }
