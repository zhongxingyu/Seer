 package com.space.piano;
 
 
 public class Game implements Runnable {
 
     private App mApp;
     private boolean mRunning;
     private Thread mThread;
     private int mScore;
 
     public Game(App app) {
         mApp = app;
     }
 
     public App getApp() {
         return mApp;
     }
 
     public boolean isRunning() {
         return mRunning;
     }
 
     public void start() {
         reset();
        setScore(mScore);
         mRunning = true;
         mThread = new Thread(this);
         mThread.start();
     }
 
     public void stop() {
         mThread = null;
         mRunning = false;
     }
 
     @Override
     public void run() {
         while (mThread != null) {
             tick();
             try {
                 Thread.sleep(33);
             } catch (InterruptedException e) {
                 e.printStackTrace();
             }
         }
 
     }
 
     protected void tick() {
         // NOP
     }
 
     public void onKey(int note, boolean on) {
         // NOP
     }
 
     private void setScore(int score) {
         mScore = score;
         getApp().setScore(score);
     }
 
     protected void addScore(int score) {
         System.out.println("+ " + score);
         setScore(mScore + score);
     }
 
     public void restart() {
        mScore = 0;
         mApp.getSongView().restart();
         reset();
     }
 
     protected void reset() {
        // NOP
     }
 
 }
