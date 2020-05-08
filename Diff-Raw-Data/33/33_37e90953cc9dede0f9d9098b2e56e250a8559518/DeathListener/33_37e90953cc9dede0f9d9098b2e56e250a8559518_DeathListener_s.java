 package me.limebyte.battlenight.core.listeners;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import me.limebyte.battlenight.api.BattleNightAPI;
 import me.limebyte.battlenight.api.battle.Battle;
 import me.limebyte.battlenight.api.event.BattleDeathEvent;
 import me.limebyte.battlenight.api.util.PlayerData;
 
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.player.PlayerRespawnEvent;
 
 public class DeathListener implements Listener {
 
     private Map<String, BattleDeathEvent> queue = new HashMap<String, BattleDeathEvent>();
 
     private BattleNightAPI api;
 
     public DeathListener(BattleNightAPI api) {
         this.api = api;
     }
 
     @EventHandler(priority = EventPriority.HIGHEST)
     public void onPlayerDeath(PlayerDeathEvent event) {
         Player player = event.getEntity();
         Battle battle = api.getBattle();
 
         if (battle.containsPlayer(player)) {
             event.getDrops().clear();
             event.setDeathMessage("");
 
             BattleDeathEvent apiEvent = new BattleDeathEvent(battle, player);
             Bukkit.getServer().getPluginManager().callEvent(apiEvent);
 
            if (apiEvent.isCancelled()) {
                 apiEvent.setRespawnLocation(battle.toSpectator(player, true));
             }
 
             queue.put(player.getName(), apiEvent);
         }
     }
 
     @EventHandler(priority = EventPriority.HIGHEST)
     public void onPlayerRespawn(PlayerRespawnEvent event) {
         Player player = event.getPlayer();
         String name = player.getName();
 
         if (queue.containsKey(name)) {
             BattleDeathEvent apiEvent = queue.get(name);
             queue.remove(name);
 
             if (apiEvent.isCancelled()) {
                 apiEvent.getBattle().respawn(player);
             } else {
                 PlayerData.reset(player);
                 PlayerData.restore(player, false, false);
             }
 
             event.setRespawnLocation(apiEvent.getRespawnLocation());
         }
     }
 
 }
