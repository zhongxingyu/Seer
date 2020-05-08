 package me.limebyte.battlenight.api.event;
 
 import me.limebyte.battlenight.api.battle.Battle;
import me.limebyte.battlenight.core.util.Metadata;
 
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Cancellable;
 import org.bukkit.event.HandlerList;
 import org.bukkit.event.player.PlayerEvent;
 
 public class BattleDeathEvent extends PlayerEvent implements Cancellable {
 
     private static final HandlerList handlers = new HandlerList();
     private Battle battle;
     private Location respawnLocation;
     private boolean cancel = false;
 
     public BattleDeathEvent(Battle battle, Player player) {
         super(player);
         this.battle = battle;
         this.respawnLocation = battle.getArena().getRandomSpawnPoint().getLocation();

        int lives = Metadata.getInt(player, "lives");
        Metadata.set(player, "lives", --lives);
        if (lives > 0) setCancelled(true);
     }
 
     public Battle getBattle() {
         return battle;
     }
 
     public Location getRespawnLocation() {
         return respawnLocation;
     }
 
     public void setRespawnLocation(Location respawnLocation) {
         this.respawnLocation = respawnLocation;
     }
 
     @Override
     public boolean isCancelled() {
         return cancel;
     }
 
     @Override
     public void setCancelled(boolean cancel) {
         this.cancel = cancel;
     }
 
     @Override
     public HandlerList getHandlers() {
         return handlers;
     }
 
     public static HandlerList getHandlerList() {
         return handlers;
     }
 
 }
