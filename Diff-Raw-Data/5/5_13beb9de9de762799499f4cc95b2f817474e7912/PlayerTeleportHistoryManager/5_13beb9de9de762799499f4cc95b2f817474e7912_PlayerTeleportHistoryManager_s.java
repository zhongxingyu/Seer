 package me.krotn.ServerWarp.utils.history;
 
 import java.util.ArrayList;
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 
 public class PlayerTeleportHistoryManager {
     private Player player;
     private ArrayList<Location> history;
     private int locationPointer = 0;
     private int size;
     
     public PlayerTeleportHistoryManager(Player player,int size){
         this.player = player;
         history = new ArrayList<Location>();
         history.add(player.getLocation());
         this.size = size;
     }
     
     protected void ensureSize(){
         if(history.size()>size){
             int toSubtract = history.size()-size;
             while(history.size()>size){
                 history.remove(history.size()-1);
             }
             locationPointer -= toSubtract;
         }
     }
     
     public Location back(){
         if(locationPointer <= 0){
             ensureSize();
             return history.get(0);
         }
         else{
             locationPointer--;
             ensureSize();
             return history.get(locationPointer);
         }
     }
     
     public Location forward(){
         if(locationPointer >= history.size()){
             ensureSize();
             return history.get(history.size()-1);
         }
         else{
             locationPointer++;
             ensureSize();
             return history.get(locationPointer);
         }
     }
     
     public void addToHistory(Location location){
         if(locationPointer<history.size()){
             while(history.size()>locationPointer){
                 history.remove(locationPointer);
             }
             history.add(location);
             locationPointer++;
         }
         else{
             locationPointer++;
             history.add(location);
         }
         ensureSize();
         System.out.println(history.size());
     }
     
     public Player getPlayer(){
         return player;
     }
     
     public void clear(){
         this.history.clear();
         locationPointer = 0;
         ensureSize();
     }
 }
