 package net.daboross.bukkitdev.playerdata;
 
 import java.util.ArrayList;
 import java.util.Random;
 import org.bukkit.Bukkit;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.event.player.PlayerTeleportEvent;
 
 /**
  *
  * @author daboross
  */
 public class PlayerDataEventListener implements Listener, CommandExecutor {
 
     private PlayerData pDataMain;
 
     protected PlayerDataEventListener(PlayerData main) {
         pDataMain = main;
     }
 
     /**
      *
      * @param evt
      */
     @EventHandler(priority = EventPriority.MONITOR)
     public void onPlayerJoin(PlayerJoinEvent evt) {
         PData pData = pDataMain.getPDataHandler().getPDataFromUsername(evt.getPlayer().getName());
         if (pData == null) {
            evt.getPlayer().performCommand("espawn");
         }
         pDataMain.getPDataHandler().getPData(evt.getPlayer()).loggedIn();
 
     }
 
     /**
      *
      * @param evt
      */
     @EventHandler(priority = EventPriority.MONITOR)
     public void onPlayerQuit(PlayerQuitEvent evt) {
         pDataMain.getPDataHandler().getPData(evt.getPlayer()).loggedOut();
     }
     protected ArrayList<Player> pvpP = new ArrayList<>();
 
     /**
      *
      * @param evt
      */
     @EventHandler(priority = EventPriority.HIGHEST)
     public void onPlayerTeleport(PlayerTeleportEvent evt) {
         if (!pvpP.contains(evt.getPlayer())) {
             if (evt.getTo().getWorld().getName().equalsIgnoreCase("pvpworld")) {
                 makeExtraThread(evt.getPlayer());
                 pvpP.add(evt.getPlayer());
                 Random r = new Random();
                 int n = r.nextInt(4);
                 n += 1;
                 evt.getPlayer().performCommand("ewarp PvP" + n);
                 evt.getPlayer().sendMessage(ColorList.MAIN + "PVP!");
                 evt.setCancelled(true);
             }
         }
     }
 
     protected void pvp(Player p) {
         makeExtraThread(p);
         pvpP.add(p);
         Random r = new Random();
         int n = r.nextInt(4);
         n += 1;
         p.performCommand("ewarp PvP" + n);
         p.sendMessage(ColorList.MAIN + "PVP!");
     }
 
     private void makeExtraThread(Player p) {
         PlayerDataEventListenerExtraThread pDEVLET = new PlayerDataEventListenerExtraThread(this, p);
         Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(PlayerData.getCurrentInstance(), pDEVLET, 20L);
     }
 
     @Override
     public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
         if (cmd.getName().equalsIgnoreCase("pvp")) {
             if (sender instanceof Player) {
                 pvp((Player) sender);
             } else {
                 sender.sendMessage(ColorList.MAIN + "You have to be a player to run this command!");
             }
             return true;
         }
         return false;
     }
 }
