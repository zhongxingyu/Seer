 package sounds;
 
 import handleds.Actor;
 
 public class BackgroundMusicPlayer implements Actor {
     private AePlayWave backgroundMusicThread;
     private boolean alive;
     private boolean active;
     
     
     public BackgroundMusicPlayer(){
         this.alive = true;
         this.active = true;
         this.backgroundMusicThread = new AePlayWave("/data/taustamusiikki.wav");
         this.backgroundMusicThread.start();
     }
     
 
     @Override
     public boolean isActive() {
         return this.active;
     }
 
     @Override
     public boolean activate() {
         if (this.active) 
             return true;
         else {
             this.active = true;
             //this.backgroundMusicThread.interrupt();
             this.backgroundMusicThread = new AePlayWave("/data/taustamusiikki.wav");
             this.backgroundMusicThread.start();
             return true;
         }
     }
 
     @Override
     public boolean inActivate() {
         if (!this.active) 
             return true;
         else
         {
             this.active = false;
            this.backgroundMusicThread.interrupt();
             return true;
         }
     }
 
     @Override
     public boolean isDead() {
         return !this.alive;
     }
 
     @Override
     public boolean kill() {
         if (this.alive)
         {
            this.backgroundMusicThread.interrupt();
             this.alive = false;
             return true;
         }
         else
         	return true;
     }
 
     @Override
     public void act() {
         if (!this.backgroundMusicThread.isAlive()){
             this.backgroundMusicThread = new AePlayWave("/data/taustamusiikki.wav");
             this.backgroundMusicThread.start();
         }
     }
 
 }
