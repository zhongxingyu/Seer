 package me.limebyte.battlenight.core.listeners;
 
 import me.limebyte.battlenight.core.BattleNight;
 import me.limebyte.battlenight.core.battle.Team;
 import me.limebyte.battlenight.core.util.chat.Messaging;
 import me.limebyte.battlenight.core.util.chat.Messaging.Message;
 import me.limebyte.battlenight.core.util.config.ConfigManager;
 import me.limebyte.battlenight.core.util.config.ConfigManager.Config;
 
 import org.bukkit.ChatColor;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerInteractEvent;
 
 public class InteractListener implements Listener {
     @EventHandler(priority = EventPriority.NORMAL)
     public void onPlayerInteract(PlayerInteractEvent event) {
         Action action = event.getAction();
         Player player = event.getPlayer();
 
         if (action.equals(Action.LEFT_CLICK_BLOCK)) {
             Block block = event.getClickedBlock();
 
             if (block.getTypeId() == ConfigManager.get(Config.MAIN).getInt("ReadyBlock", 42)) {
                 if (BattleNight.getBattle().usersTeam.containsKey(player.getName()) && BattleNight.getBattle().isInLounge()) {
                     String name = player.getName();
                     Team team = BattleNight.getBattle().usersTeam.get(name);
 
                     if (team.isReady()) {
                         Messaging.tellEveryone(false, Message.TEAM_IS_READY, team.getColour() + team.getName());
 
                         if (team.equals(Team.RED)) {
                             BattleNight.redTeamIronClicked = true;
 
                             if (Team.BLUE.isReady() && BattleNight.blueTeamIronClicked) {
                                 BattleNight.getBattle().start();
                             }
                         } else if (team.equals(Team.BLUE)) {
                             BattleNight.blueTeamIronClicked = true;
 
                             if (Team.RED.isReady() && BattleNight.redTeamIronClicked) {
                                 BattleNight.getBattle().start();
                             }
                         }
                     } else {
                         player.sendMessage(ChatColor.GRAY + "[BattleNight] " + ChatColor.WHITE + "Your team have not all picked a class!");
                     }
                 }
             }
         }
 
         if (action.equals(Action.LEFT_CLICK_AIR) || action.equals(Action.LEFT_CLICK_BLOCK)) {
             if (BattleNight.getBattle().spectators.contains(player.getName())) {
                if (player.getItemInHand() != null) {
                    String itemName = player.getItemInHand().getItemMeta().getDisplayName();
                     if (itemName.contains("Previous Player")) {
                         Messaging.tell(player, "Teleporting to previous player.");
                     } else if (itemName.contains("Next Player")) {
                         Messaging.tell(player, "Teleporting to next player.");
                     }
                 }
             }
         }
     }
 }
