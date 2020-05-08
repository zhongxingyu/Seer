 package de.minestar.events;
 
 import org.bukkit.event.Event;
 import org.bukkit.event.HandlerList;
 
 public class PlayerChangedGroupEvent extends Event {
 
     private static final HandlerList handlers = new HandlerList();
     private final String playerName;
     private final String oldGroupName;
     private final String newGroupName;
 
     public PlayerChangedGroupEvent(String playerName, String oldGroupName, String newGroupName) {
         this.playerName = playerName;
         this.oldGroupName = oldGroupName;
         this.newGroupName = newGroupName;
     }
 
     /**
      * @return the playerName
      */
     public String getPlayerName() {
         return playerName;
     }
 
     /**
      * @return the oldGroupName
      */
     public String getOldGroupName() {
         return oldGroupName;
     }
 
     /**
      * @return the newGroupName
      */
     public String getNewGroupName() {
         return newGroupName;
     }
 
     @Override
     public HandlerList getHandlers() {
         return handlers;
     }
 }
