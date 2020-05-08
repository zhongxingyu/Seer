 /*
  * Master-Thesis work: see https://sites.google.com/site/sifthesis/
  */
 package tangible.utils;
 
 /**
  *
  * @author leo
  */
 public abstract class AbsLoopingThread extends Thread implements LoopingThread {
 
     boolean _running;
 
     public AbsLoopingThread() {
     }
 
     @Override
     public void stopASAP() {
         this._running = false;
     }
 
     @Override
     public final void run() {
         this._running = true;
         this.runningSetup();
         while (_running) {
             this.loopingProcess();
         }
         
     }
 
     protected abstract void loopingProcess();
 
     protected void runningSetup() {
     }
 }
