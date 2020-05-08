 package me.limebyte.battlenight.core.listeners;
 
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Set;
 
 import me.limebyte.battlenight.api.BattleNightAPI;
 import me.limebyte.battlenight.api.battle.Lobby;
 import me.limebyte.battlenight.api.util.Message;
 import me.limebyte.battlenight.api.util.Messenger;
 import me.limebyte.battlenight.core.tosort.ConfigManager;
 import me.limebyte.battlenight.core.tosort.ConfigManager.Config;
 import me.limebyte.battlenight.core.tosort.Metadata;
 import me.limebyte.battlenight.core.util.BattlePlayer;
 
 import org.bukkit.Bukkit;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerInteractEvent;
 
 public class InteractListener extends APIRelatedListener {
 
     public InteractListener(BattleNightAPI api) {
         super(api);
     }
 
     @EventHandler(priority = EventPriority.NORMAL)
     public void onPlayerInteract(PlayerInteractEvent event) {
         Action action = event.getAction();
         Player player = event.getPlayer();
         Lobby lobby = getAPI().getLobby();
 
         BattlePlayer bPlayer = BattlePlayer.get(player.getName());
         if (!bPlayer.isAlive()) event.setCancelled(true);
 
         if (action == Action.LEFT_CLICK_BLOCK || action == Action.RIGHT_CLICK_BLOCK) {
             Block block = event.getClickedBlock();
 
             if (block.getTypeId() == ConfigManager.get(Config.MAIN).getInt("ReadyBlock", 42)) {
                 if (lobby.getPlayers().contains(player.getName())) {
                     Messenger messenger = getAPI().getMessenger();
                     if (getAPI().getPlayerClass(player) != null) {
                         if (!Metadata.getBoolean(player, "ready")) {
                             Metadata.set(player, "ready", true);
                             messenger.tellLobby(Message.PLAYER_IS_READY, player);
                         }
 
                         if (lobby.isStarting()) return;
 
                         Set<String> waiting = new HashSet<String>(lobby.getPlayers());
                         Iterator<String> it = waiting.iterator();
                         while (it.hasNext()) {
                             Player p = Bukkit.getPlayerExact(it.next());
                             if (p == null || Metadata.getBoolean(p, "ready")) it.remove();
                         }
 
                         if (waiting.isEmpty()) {
                             try {
                                 lobby.startBattle();
                             } catch (IllegalStateException e) {
                                 messenger.tellLobby(e.getMessage());
                             }
                         } else {
                            String list = waiting.toString().replaceAll("[,]([^,]*)$", " and$1");
                             messenger.tellLobby(Message.WAITING_FOR_PLAYERS, list.replaceAll("\\[|\\]", ""));
                         }
                     } else {
                         messenger.tell(player, Message.NO_CLASS);
                     }
                 }
             }
         }
     }
 }
