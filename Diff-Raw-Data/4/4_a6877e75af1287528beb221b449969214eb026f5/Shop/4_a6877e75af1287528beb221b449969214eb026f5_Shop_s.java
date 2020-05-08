 package org.hpiz.ShopAds2.Shop;
 
 import java.io.Serializable;
 import java.util.Date;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.entity.Player;
 import org.hpiz.ShopAds2.ShopAds2;
 
 public class Shop extends ShopAds2 implements Serializable {
 
     private String shopName;
     private AdLocation location;
     private String creator;
     private Date timeToEnd;
     private boolean runsForever;
     private String advertisement;
     private String[] worldsToAdvertiseIn;
     private boolean isAdvertising;
     private boolean isExpired;
     private double moneyEarned;
     private int timesTeleportedTo;
 
     public Shop(String newShopName, Location shopLocation, String newCreator, Date time, boolean forever, World w, String ad, String[] advertiseTo, boolean advertising) {
         this.shopName = newShopName;
         this.location = new AdLocation(shopLocation);
         this.creator = newCreator;
         this.timeToEnd = time;
         this.advertisement = ad;
         this.runsForever = forever;
         this.worldsToAdvertiseIn = advertiseTo;
        this.isExpired = advertising;
         this.moneyEarned = 0.00;
         this.timesTeleportedTo = 0;
     }
 
     public World getWorld() {
         return location.getWorld();
     }
     public boolean advertisesIn (String world){
         for (String s : this.worldsToAdvertiseIn){
             if(s.equalsIgnoreCase(world)){
                 return true;
             }
         }
         return false;
     }
     public World[] getWorldsToAdvertiseIn() {
         World[]worlds = new World [this.worldsToAdvertiseIn.length];
         for (int i=0; i<worlds.length;i++){
             worlds[i]= serverInterface.getWorld(worldsToAdvertiseIn[i]);
         }
         return worlds;
     }
 
     public double getMoneyEarned() {
         return this.moneyEarned;
     }
 
     public String getShopName() {
         return this.shopName;
     }
 
     public int getTimesTeleportedTo() {
         return this.timesTeleportedTo;
     }
 
     public AdLocation getLocation() {
         return this.location;
     }
 
     public String getAd() {
         return this.advertisement;
     }
 
     public Date getTimeToEnd() {
         return this.timeToEnd;
     }
 
     public String getShopOwner() {
         return this.creator;
     }
 
     public boolean shopExpired() {
         return this.isExpired;
     }
 
     public boolean runsForever() {
         return this.runsForever;
     }
 
     public boolean shopAdvertising() {
         return this.isAdvertising;
     }
 
     public void setShopAdvertising(boolean b) {
         this.isAdvertising = b;
     }
 
     public void setShopExpired(boolean b) {
         this.isExpired = b;
     }
 
     public void setTimeToEnd(Date time) {
         this.timeToEnd = time;
     }
 
     public void setWorldsToAdvertiseIn(World[] w) {
         this.worldsToAdvertiseIn = new String[w.length];
         for (int i = 0 ; i < w.length; i++){
             this.worldsToAdvertiseIn[i] = w[i].getName();
         }
     }
 
     public void shopTeleportedTo() {
         this.timesTeleportedTo++;
     }
 
     public void addMoneyEarned(double money) {
         this.moneyEarned = +money;
     }
 
     public void setLocation(Location location) {
         this.location = new AdLocation(location);
     }
 
     public void setAdvertisement(String advertisement) {
         this.advertisement = advertisement;
     }
 
     public void setIsAdvertising(boolean isAdvertising) {
         this.isAdvertising = isAdvertising;
     }
 
     public void setShopName(String shopName) {
         this.shopName = shopName;
     }
 
     public void addWorldToAdvertiseIn(World world) {
         
         for (String w : this.worldsToAdvertiseIn){
             if(world.getName() == w){
                 return;
             }   
         }
         String[] newWorld = new String[this.worldsToAdvertiseIn.length+1];
         for (int i=0; i<this.worldsToAdvertiseIn.length;i++){
             newWorld[i] = this.worldsToAdvertiseIn[i];
         }
         newWorld[newWorld.length-1] = world.getName();
         this.worldsToAdvertiseIn = new String [newWorld.length];
         this.worldsToAdvertiseIn = newWorld;
     }
     
     
 }
