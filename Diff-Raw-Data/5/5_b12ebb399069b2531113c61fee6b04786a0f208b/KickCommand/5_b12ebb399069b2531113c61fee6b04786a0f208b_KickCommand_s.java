 package net.robbytu.banjoserver.bungee.kicks;
 
 import net.md_5.bungee.api.ChatColor;
 import net.md_5.bungee.api.CommandSender;
 import net.md_5.bungee.api.connection.ProxiedPlayer;
 import net.md_5.bungee.api.plugin.Command;
 import net.robbytu.banjoserver.bungee.Main;
 
 public class KickCommand extends Command {
     private final String usage = "/kick [user] [reason]";
 
     public KickCommand() {
         super("kick");
     }
 
     @Override
     public void execute(CommandSender sender, String[] args) {
         if(!sender.hasPermission("bs.admin")) {
             this.failCommand(sender, "You do not have permission to execute this command.");
             return;
         }
 
         if(args.length < 2) {
             this.failCommand(sender, "Missing arguments.");
             return;
         }
 
         if(Main.instance.getProxy().getPlayer(args[0]) == null) {
             this.failCommand(sender, "The specified player seems to be offline.");
             return;
         }
 
         String reasonBody = "";
        for (int i = 1; i < args.length - 1; i++) reasonBody += args[i];
 
        Main.instance.getProxy().getPlayer(args[0]).disconnect("Je werd gekicked: " + reasonBody);
 
         for(ProxiedPlayer player : Main.instance.getProxy().getPlayers()) {
             if(player.hasPermission("bs.admin")) {
                 player.sendMessage("");
                 player.sendMessage(ChatColor.RED + sender.getName() + " heeft " + args[0] + " gekicked:");
                 player.sendMessage(ChatColor.RED + " * " + reasonBody);
                 player.sendMessage("");
             }
         }
 
         Main.instance.getLogger().info("User " + args[0] + " got kicked by " + sender.getName() + ": " + reasonBody);
     }
 
     private void failCommand(CommandSender sender, String message) {
         sender.sendMessage(ChatColor.RED + message);
         sender.sendMessage(ChatColor.GRAY + "Usage: " + ChatColor.ITALIC + this.usage);
     }
 }
