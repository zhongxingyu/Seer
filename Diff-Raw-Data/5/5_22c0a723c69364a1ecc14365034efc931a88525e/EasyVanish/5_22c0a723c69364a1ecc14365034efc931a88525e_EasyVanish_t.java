 package org.kitteh.vanish.easy;
 
 import java.util.HashSet;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class EasyVanish extends JavaPlugin implements Listener {
     private final HashSet<String> vanished = new HashSet<String>();
     private final String vanishPerm = "vanish.vanish";
 
     @Override
     public void onEnable() {
         this.getServer().getPluginManager().registerEvents(this, this);
         try {
             new Metrics(this).start();
         } catch (final Exception e) {
         }
     }
 
     @Override
     public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
         if (!(sender instanceof Player)) {
             sender.sendMessage("Can't vanish if not a player!");
             return true;
         }
         if (args.length == 0) {
             boolean vanishing = true;
             String bit;
             if (this.vanished.contains(sender.getName())) {
                 vanishing = false;
                 this.vanished.remove(sender.getName());
                 bit = "unvanished";
             } else {
                 this.vanished.add(sender.getName());
                 bit = "vanished";
             }
             final Player player = (Player) sender;
             for (final Player plr : this.getServer().getOnlinePlayers()) {
                 if (vanishing && !plr.hasPermission(this.vanishPerm)) {
                     plr.hidePlayer(player);
                } else if (!vanishing && !plr.canSee(player)) {
                     plr.showPlayer(player);
                 }
             }
             this.getServer().broadcast(ChatColor.AQUA + player.getName() + " has " + bit, this.vanishPerm);
         } else if (args[0].equalsIgnoreCase("list")) {
             final StringBuilder list = new StringBuilder();
             list.append(ChatColor.AQUA);
             list.append("Vanished (");
             list.append(this.vanished.size());
             list.append("): ");
             for (final String name : this.vanished) {
                 list.append(name);
                list.append(", ");
             }
             list.setLength(list.length() - 2);
             sender.sendMessage(list.toString());
         }
         return true;
     }
 
     @EventHandler
     public void onJoin(PlayerJoinEvent event) {
         if (!event.getPlayer().hasPermission(this.vanishPerm) && (this.vanished.size() > 0)) {
             final Player player = event.getPlayer();
             for (final Player plr : this.getServer().getOnlinePlayers()) {
                 if (this.vanished.contains(plr.getName())) {
                     player.hidePlayer(plr);
                 }
             }
         }
     }
 
     @EventHandler
     public void onQuit(PlayerQuitEvent event) {
         if (this.vanished.contains(event.getPlayer().getName())) {
             final Player player = event.getPlayer();
             this.vanished.remove(event.getPlayer().getName());
             for (final Player plr : this.getServer().getOnlinePlayers()) {
                 if ((plr != null) && !plr.canSee(event.getPlayer())) {
                     plr.showPlayer(player);
                 }
             }
         }
     }
 }
