 package de.engineapp;
 
 import de.engine.math.PhysicsEngine2D;
 
 public class Physics
 {
     // interface to recognize repaints caused by finishing of frame calculation
     public interface FinishedCallback
     {
         public void done();
     }
     
     
     private PhysicsEngine2D engine = null;
     private long deltaTime;
     private FinishedCallback finishedCallback;
     
     private Thread worker = null;
     
     
     public Physics(PhysicsEngine2D engine, long deltaTime, FinishedCallback finishedCallback)
     {
         this.engine = engine;
         this.deltaTime = deltaTime;
         this.finishedCallback = finishedCallback;
     }
     
     
     public void start()
     {
         
         worker = new Thread()
         {
             @Override
             public void run()
             {
                 while (!this.isInterrupted())
                 {
                     long t = System.currentTimeMillis();
                     
                     engine.calculateNextFrame(deltaTime);
                     finishedCallback.done();
                     
                     t = System.currentTimeMillis() - t;
                     
                     try
                     {
                         // don't waste cpu power, so sleep rest of the time
                         Thread.sleep(t < deltaTime ? deltaTime - t : 0);
                     }
                     catch (InterruptedException e)
                     {
                         // may occur, but should be no problem, we just ignore it
                         //e.printStackTrace();
                     }
                 }
             }
         };
         
         worker.start();
     }
     
     
     public void pause()
     {
         if (worker != null && worker.isAlive())
         {
             worker.interrupt();
         }
     }
     
     
     public boolean isRunning()
     {
         return worker != null && worker.isAlive();
     }
 }
