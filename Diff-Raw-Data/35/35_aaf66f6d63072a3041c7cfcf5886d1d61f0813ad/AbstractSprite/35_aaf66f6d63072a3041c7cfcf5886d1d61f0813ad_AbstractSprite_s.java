 package com.roscopeco.scratch.runtime;
 
 public abstract class AbstractSprite extends MediaScriptable {
   final AbstractStage stage;
   int x, y;
   Costume costume;
   
   protected AbstractSprite(AbstractStage owner) {
     this.stage = owner;    
   }
   
   public AbstractStage stage() {
     return stage;    
   }
   
   public int x() {
     return x;
   }
   
   public void setX(int x) {
     this.x = x;
   }
   
   public int y() {
     return y;
   }
   
   public void setY(int y) {
     this.y = y;
   }
   
   public void setXY(int x, int y) {
     this.x = x;
     this.y = y;
   }
   
   public void forward(int px) {
     // TODO implement this
   }
   
   public void startGlideTo(int x, int y, long millis) {
     
   }
   
   public void setHeading(double degrees) {
     // TODO implement
   }
   
   public void turnLeft(double degrees) {
     // TODO implement
   }
   
   public void turnRight(double degrees) {
     // TODO implement
   }
   
   /**
    * Turn 180 degrees if at edge...
    */
   public void bounceOffEdge() {
     // TODO implement    
   }
   
   public void show() {
    // TODO implement
   }
   
   public void hide() {
    // TODO implement
   }
   
   public Costume costume() {
     return costume;
   }
   
   public void setCostume(Costume costume) {
     this.costume = costume;
   }
   
   public abstract void nextCostume();
   
   public void think(String text, long millis) {
     // TODO implement
   }
   
   public boolean touching(AbstractSprite other) {
     // TODO implement
     return false;
   }
 
   @Override
   public void showVariable(String name) {
     stage.showVariable(name);
   }
 
   @Override
   public void hideVariable(String name) {
     stage.hideVariable(name);
   }
 }
