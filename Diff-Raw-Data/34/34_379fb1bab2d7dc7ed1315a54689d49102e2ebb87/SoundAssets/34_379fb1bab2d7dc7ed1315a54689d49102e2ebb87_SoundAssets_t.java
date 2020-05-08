 package com.icbat.game.tradesong.assetReferences;
 
 public enum SoundAssets {
    GATHER_CLINK("gathering2"),
     SUCCESS("success"),
     CHURCHBELL("churchbell"),
     PORTAL("portal"),
     SCREEN_SWAP("swapping"),;
 
     private String path;
 
     private SoundAssets(String pathVal) {
         this.path = "sounds/" + pathVal + ".ogg";
     }
 
     public String getPath() {
         return path;
     }
 }
 
