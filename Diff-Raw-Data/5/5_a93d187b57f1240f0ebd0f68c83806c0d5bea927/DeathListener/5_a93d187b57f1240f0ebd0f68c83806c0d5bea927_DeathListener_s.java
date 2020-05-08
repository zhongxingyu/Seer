 package me.limebyte.battlenight.core.listeners;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.Level;
 
 import me.limebyte.battlenight.api.BattleNightAPI;
 import me.limebyte.battlenight.api.battle.Battle;
 import me.limebyte.battlenight.api.event.BattleDeathEvent;
import me.limebyte.battlenight.api.util.PlayerData;
 import me.limebyte.battlenight.core.util.Messenger;
 
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.player.PlayerRespawnEvent;
 
 public class DeathListener extends APIRelatedListener {
 
     protected static Map<String, BattleDeathEvent> queue = new HashMap<String, BattleDeathEvent>();
 
     public DeathListener(BattleNightAPI api) {
         super(api);
     }
 
     @EventHandler(priority = EventPriority.HIGHEST)
     public void onPlayerDeath(PlayerDeathEvent event) {
         Player player = event.getEntity();
         Battle battle = getAPI().getBattle();
 
         if (battle.containsPlayer(player)) {
             event.getDrops().clear();
             event.setDeathMessage("");
 
             Messenger.debug(Level.INFO, "PlayerDeathEvent for " + player.getName());
 
             if (battle.isInProgress()) {
                 Messenger.killFeed(player, player.getKiller());
             }
 
             BattleDeathEvent apiEvent = new BattleDeathEvent(battle, player);
             Bukkit.getServer().getPluginManager().callEvent(apiEvent);
 
             Messenger.debug(Level.INFO, "BattleDeathEvent in Death for " + player.getName() + " canceled=" + apiEvent.isCancelled());
 
             if (!apiEvent.isCancelled()) {
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
             Messenger.debug(Level.INFO, "PlayerRespawnEvent for " + player.getName());
             BattleDeathEvent apiEvent = queue.get(name);
             queue.remove(name);
 
             Messenger.debug(Level.INFO, "BattleDeathEvent in Respawn for " + name + " canceled=" + apiEvent.isCancelled());
 
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
