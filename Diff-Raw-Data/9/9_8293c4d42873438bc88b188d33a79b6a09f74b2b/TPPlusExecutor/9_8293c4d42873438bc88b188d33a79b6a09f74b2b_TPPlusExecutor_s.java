 package net.mdcreator.tpplus;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.PluginDescriptionFile;
 
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.net.URL;
 import java.nio.channels.Channels;
 import java.nio.channels.ReadableByteChannel;
 import java.util.Arrays;
 
 public class TPPlusExecutor implements CommandExecutor{
 
     TPPlus plugin;
     private String title = ChatColor.DARK_GRAY + "[" + ChatColor.BLUE + "TP+" + ChatColor.DARK_GRAY + "] " + ChatColor.GRAY;
 
     public TPPlusExecutor(TPPlus plugin){
         this.plugin = plugin;
     }
 
     public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
         PluginDescriptionFile pdf = plugin.getDescription();
         if(args.length==0){
             sender.sendMessage(new String[] {
                     title + "info",
                     ChatColor.DARK_GRAY + "by " + ChatColor.GRAY + pdf.getAuthors().get(0),
                     ChatColor.DARK_GRAY + "vers " + ChatColor.GRAY + pdf.getVersion(),
                     ChatColor.GRAY + pdf.getDescription()
             });
             return true;
         } else if(args.length==1){
             if(args[0].equals("update")){
                 if(sender instanceof Player && !sender.isOp()){
                     sender.sendMessage(title + ChatColor.RED + "You can't update the plugin!");
                     return true;
                 }
                 try {
                     sender.getServer().broadcastMessage(title + "Updating plugin, expect lag");
                     URL onlinePlugin = new URL("https://github.com/Gratimax/TPPlus/blob/master/deploy/TPPlus.jar?raw=true");
                     sender.sendMessage(title + "Using https://github.com/Gratimax/TPPlus/blob/master/deploy/TPPlus.jar?raw=true");
                     plugin.getLogger().info(title + "Updating plugin https://github.com/Gratimax/TPPlus/blob/master/deploy/TPPlus.jar?raw=true");
                     ReadableByteChannel rbc = Channels.newChannel(onlinePlugin.openStream());
                     FileOutputStream fos = new FileOutputStream(plugin.getDataFolder().getParentFile().getPath() + "\\TPPlus.jar");
                     fos.getChannel().transferFrom(rbc, 0, 1 << 24);
                     sender.getServer().broadcastMessage(title + "Reloading server");
                     plugin.getServer().reload();
                     sender.getServer().broadcastMessage(title + "Finished updating");
                 } catch (IOException e) {
                     sender.sendMessage(title + ChatColor.RED + "An error occurred while updating:" + Arrays.toString(e.getStackTrace()));
                     e.printStackTrace();
                 }
                 return true;
             } else return false;
         } else if(args.length==2){
             if(sender instanceof Player && !sender.isOp()){
                 sender.sendMessage(title + ChatColor.RED + "You can't update the plugin!");
                 return true;
             }
             if(args[0].equals("op-color")){
                 plugin.configYML.set("op-color", args[1]);
                 plugin.saveConfig();
                 sender.sendMessage(title + "Op color set to " + ChatColor.translateAlternateColorCodes('&', args[1] + "this"));
                 return true;
             } else if(args[0].equals("owner-color")){
                 plugin.configYML.set("owner-color", args[1]);
                 sender.sendMessage(title + "Owner color set to " + ChatColor.translateAlternateColorCodes('&', args[1] + "this"));
                 plugin.saveConfig();
                 return true;
             }  else if(args[0].equals("remove")){
                String player = args[1];
                 for(String possible : plugin.homesManager.homes.keySet()){
                    if(possible.toLowerCase().startsWith(player.toLowerCase())) player = possible;
                 }
                 plugin.icons.findMarker(player).deleteMarker();
                 plugin.homesManager.homes.remove(player);
                 plugin.homesYML.set(player, null);
                 plugin.saveHomes();
                 sender.sendMessage(title + "Home for " + player + " deleted.");
                 return true;
             } else return false;
         }
         return false;
     }
 }
