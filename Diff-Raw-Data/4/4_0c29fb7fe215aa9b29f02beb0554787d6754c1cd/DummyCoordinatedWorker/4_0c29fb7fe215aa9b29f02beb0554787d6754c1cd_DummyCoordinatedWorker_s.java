 package com.meltmedia.cadmium.jgroups;
 
import java.io.File;
 import java.util.Map;
 
 public class DummyCoordinatedWorker implements CoordinatedWorker, ContentService, SiteDownService {
   private boolean pulling = false;
 
   @Override
   public void beginPullUpdates(Map<String, String> properties) {
     pulling = true;
   }
   
   private boolean switched = false;
 
   @Override
  public void switchContent(File newDir) {
     switched = true;
     listener.doneSwitching();
   }
   
   private boolean killed = false;
 
   @Override
   public void killUpdate() {
     killed = true;
   }
 
   @Override
   public void setListener(CoordinatedWorkerListener listener) {
     
   }
 
   public boolean isPulling() {
     return pulling;
   }
 
   public void setPulling(boolean pulling) {
     this.pulling = pulling;
   }
 
   public boolean isSwitched() {
     return switched;
   }
 
   public void setSwitched(boolean switched) {
     this.switched = switched;
   }
 
   public boolean isKilled() {
     return killed;
   }
 
   public void setKilled(boolean killed) {
     this.killed = killed;
   }
 
   private boolean siteDown = false;
   
   @Override
   public void takeSiteDown() {
     siteDown = true;
   }
 
   @Override
   public void bringSiteUp() {
     siteDown = false;
   }
   
   public boolean isSiteDown() {
     return siteDown;
   }
 
   private ContentServiceListener listener;
   
   @Override
   public void setListener(ContentServiceListener listener) {
     this.listener = listener;
   }
 
 }
