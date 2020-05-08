 package me.muffinjello.visionflow;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Listener;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.potion.PotionEffect;
 import org.bukkit.potion.PotionEffectType;
 
 public class VisionFlow extends JavaPlugin implements Listener {
     //Startup
     @Override
     public void onEnable(){
         PluginDescriptionFile pdfFile = this.getDescription();
         this.getLogger().info(pdfFile.getName() + " version " + pdfFile.getVersion() + " has been enabled!");
     }
     public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args){
         if (command.getName().equalsIgnoreCase("vision")){
             if (sender.hasPermission("visionflow.vision") || sender.isOp()){
                 Player player = Bukkit.getPlayer(sender.getName());
                 if (args.length >= 1){
                     String argtwo = args[0].trim();
                     if (argtwo.equalsIgnoreCase("on")){
                         player.removePotionEffect(PotionEffectType.NIGHT_VISION);
                         player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 1));
                         sender.sendMessage(ChatColor.YELLOW + "Turned vision " + ChatColor.GOLD + "on");
                     } else if(argtwo.equalsIgnoreCase("off")){
                         sender.sendMessage(ChatColor.YELLOW + "Turned vision " + ChatColor.RED + "off");
                         player.removePotionEffect(PotionEffectType.NIGHT_VISION);
                     }
                } else {
                    sender.sendMessage(ChatColor.YELLOW + "You need to type /vision <on/off> !");
                }
             } else {
                sender.sendMessage(ChatColor.RED + "You don't have permission to do that!");
             }
             return true;
         }
         return false;
     }
 }
