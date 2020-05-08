 package com.cegeka.nocturne.godgame;
 
 public class Creator {
 
     private long startDayTime;
 
     private boolean paused;
 
     private final World world;
 
     public Creator() {
         this.world = new World(5);
     }
 
     public void start() {
         incrementTime();
     }
 
     private void incrementTime() {
         if(!paused) {
             while (true) {
                 long currentTime = System.currentTimeMillis();
                 if (currentTime - startDayTime < 5 * 1000) {
                     continue;
                 }
                world.daysCounter++;
                 startDayTime = currentTime;
             }
         }
     }
 
 
     public void pause() {
         paused = true;
     }
 
     public void resume() {
         paused = false;
     }
 
     public boolean isPaused() {
         return paused;
     }
 
 
 }
