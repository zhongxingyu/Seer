 package fr.noogotte.useful_commands.command;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import fr.aumgn.bukkitutils.command.Command;
 import fr.aumgn.bukkitutils.command.NestedCommands;
 import fr.aumgn.bukkitutils.command.args.CommandArgs;
 import fr.noogotte.useful_commands.UsefulCommandsPlugin;
 import fr.noogotte.useful_commands.component.AfkComponent;
 import fr.noogotte.useful_commands.component.GodComponent;
 
 @NestedCommands(name = "useful")
 public class PlayerInfoCommand extends UsefulCommands {
 
     private final UsefulCommandsPlugin plugin;
 
     public PlayerInfoCommand(UsefulCommandsPlugin plugin) {
         this.plugin = plugin;
     }
 
     @Command(name = "playerinfo", min = 1, max = 1)
     public void playerInfo(CommandSender sender, CommandArgs args) {
         Player target = args.getPlayer(0).value(sender);
         String yes = "Oui";
         String no = "Non";
 
         sender.sendMessage(ChatColor.GREEN + "" + ChatColor.UNDERLINE
                 + "Info de " + target.getName());
         sender.sendMessage(ChatColor.GREEN + "Vie : " + ChatColor.AQUA
                 + target.getHealth());
         sender.sendMessage(ChatColor.GREEN + "Faim : " + ChatColor.AQUA
                 + target.getFoodLevel());
         sender.sendMessage(ChatColor.GREEN + "IP : " + ChatColor.AQUA
                 + target.getAddress());
         Location loc = target.getLocation();
         sender.sendMessage(ChatColor.GREEN + "Position : (" + ChatColor.AQUA
                 + loc.getBlockX() + ChatColor.GREEN + "," + ChatColor.AQUA
                 + loc.getBlockY() + ChatColor.GREEN + "," + ChatColor.AQUA
                 + loc.getBlockZ() + ChatColor.GREEN + ") dans : "
                 + ChatColor.AQUA + target.getWorld().getName());
         sender.sendMessage(ChatColor.GREEN + "Gamemode : " + ChatColor.AQUA
                 + target.getGameMode());
         sender.sendMessage(ChatColor.GREEN + "Expérience : " + ChatColor.AQUA
                 + target.getLevel());
 
         if (target.isOp()) {
             sender.sendMessage(ChatColor.GREEN + "Op : " + ChatColor.AQUA + yes);
         } else {
             sender.sendMessage(ChatColor.GREEN + "Op : " + ChatColor.AQUA + no);
         }
 
         GodComponent godComponent = plugin.getComponent(GodComponent.class);
         if (godComponent != null) {
             if (godComponent.isGod(target)) {
                 sender.sendMessage(ChatColor.GREEN + "Mode Dieu : "
                         + ChatColor.AQUA + yes);
             } else {
                 sender.sendMessage(ChatColor.GREEN + "Mode Dieu : "
                         + ChatColor.AQUA + no);
             }
         }
 
         AfkComponent afkComponent = plugin.getComponent(AfkComponent.class);
         if (afkComponent != null) {
             if (afkComponent.isAfk(target)) {
                 sender.sendMessage(ChatColor.GREEN + "AFK : " + ChatColor.AQUA
                         + yes);
             } else {
                 sender.sendMessage(ChatColor.GREEN + "AFK : " + ChatColor.AQUA
                         + no);
             }
         }
     }
 
     @Command(name = "online")
     public void onlinePlayers(CommandSender sender, CommandArgs args) {
    	sender.sendMessage(ChatColor.GREEN + "Joueur en ligne :");
     	for(Player onlinePlayer : Bukkit.getOnlinePlayers()) {
     		sender.sendMessage(ChatColor.BLUE + "  - " + onlinePlayer.getName());
     	}
     }
 
     @Command(name = "operatorlist")
     public void opList(CommandSender sender, CommandArgs args) {
    	sender.sendMessage(ChatColor.GREEN + "Joueur OP:");
     	for(OfflinePlayer opPlayer : Bukkit.getOperators()) {
     		sender.sendMessage(ChatColor.BLUE + "  - " + opPlayer.getName());
     	}
     }
 }
