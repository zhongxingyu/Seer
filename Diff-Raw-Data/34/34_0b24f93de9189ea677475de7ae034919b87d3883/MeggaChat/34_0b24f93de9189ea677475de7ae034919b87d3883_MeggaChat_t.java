 package com.meggawatts.MeggaChat;
 
 import java.io.File;
 import java.util.HashMap;
 import java.util.logging.Logger;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerChatEvent;
 
 public class MeggaChat extends JavaPlugin implements Listener {
 
     public static final Logger log = Logger.getLogger("Minecraft");
     HashMap adminschatting = new HashMap();
     File PEX = new File("plugins//PermissionsEx.jar");
     boolean PEXexists = PEX.exists();
    StringBuilder out = new StringBuilder();
 
     @Override
     public void onEnable() {
         // Check for PEX
         if (PEXexists) {
             getServer().getPluginManager().registerEvents(new ColoredListListener(), this);
             log.info("[MeggaChat] Found PEX, colored list enabled.");
         } else {
             log.info("[MeggaChat] PEX not found, colored list disabled.");
         }
         // Register events
         getServer().getPluginManager().registerEvents(this, this);
         getServer().getPluginManager().registerEvents(new PipeListener(), this);
         getServer().getPluginManager().registerEvents(new PlayerListener(), this);
        setupStringBuilder();
     }
 
     @Override
     public void onDisable() {
         // Clear hash map on disable
         adminschatting.clear();
     }
 
     @Override
     public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
         String msg = "";
         if ((args.length > 0) && ((sender instanceof Player))) {
             Player player = (Player) sender;
             if (player.hasPermission("meggachat.admin") && label.equalsIgnoreCase("a")) {
                 if (args[0].equalsIgnoreCase("on")) {
                     adminschatting.put(sender, true);
                     sender.sendMessage(ChatColor.DARK_GREEN + "Admin Chat enabled");
                 }
                 if (args[0].equalsIgnoreCase("off")) {
                     adminschatting.remove(sender);
                     sender.sendMessage(ChatColor.DARK_RED + "Admin Chat disabled");
                 }
                 if (args[0].equalsIgnoreCase("?")) {
                     sender.sendMessage(ChatColor.DARK_RED + "/a on " + ChatColor.GREEN + "will toggle AdminChat mode on.");
                     sender.sendMessage(ChatColor.DARK_RED + "/a off " + ChatColor.GREEN + "will toggle AdminChat mode off.");
                     sender.sendMessage(ChatColor.DARK_RED + "/a <message> " + ChatColor.GREEN + "will send the message to all who have access to adminchat.");
                 }
                 msg = args[0];
                 if (args.length > 1) {
                     for (int i = 1; i < args.length; i++) {
                         msg += " " + args[i];
                     }
                     sendToAdmins(msg, player);
                 }
                 if (args[0].equalsIgnoreCase("whofly")) {
                     for (Player query : getServer().getOnlinePlayers()) {
                         if (query.isFlying()) {
                             out.append(ChatColor.RED);
                             out.append(query.getName());
                             out.append(", ");
                         }
                         String[] lines = out.toString().split("\n");
                         for (String line : lines) {
                             sender.sendMessage(line);
                         }
                        clearStrings();

                     }
 
                 } else if (args.length == 1 && !(args[0].equalsIgnoreCase("?") || args[0].equalsIgnoreCase("on") || args[0].equalsIgnoreCase("off"))) {
                     sendToAdmins(args[0], player);
                 }
             } else if (!(sender.hasPermission("meggachat.admin"))) {
                 sender.sendMessage(ChatColor.DARK_RED + "No Permissions!");
             }
         }
         return true;
     }
 
     public void sendToAdmins(String Message, Player chatter) {
         for (Player player : getServer().getOnlinePlayers()) {
             if (player.hasPermission("meggachat.admin")) {
                 player.sendMessage("[" + ChatColor.DARK_AQUA + "AdminChat" + ChatColor.WHITE + "] " + chatter.getName() + ": " + ChatColor.GREEN + Message);
             }
         }
     }
 
     @EventHandler(priority = EventPriority.NORMAL)
     public void onPlayerChat(PlayerChatEvent event) {
         Player chatter = event.getPlayer();
 
         if (adminschatting.containsKey(chatter)) {
             sendToAdmins(event.getMessage(), event.getPlayer());
             event.setCancelled(true);
         }
     }

    private void setupStringBuilder() {
        out.append(ChatColor.GREEN);
        out.append("Players Flying: ");
        out.append("\n");
    }
    
    private void clearStrings() {
        out.delete(0, out.length());
        setupStringBuilder();
    }
 }
