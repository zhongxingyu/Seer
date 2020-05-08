 /**
  * TunderePortMapper - Package: net.tsuttsu305.tundereportmapper
  * Created: 2013/04/23 18:08:55
  */
 package net.tsuttsu305.tundereportmapper;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 
 /**
  * TunderePortMapper (TunderePortMapper.java)
  * @author tsuttsu305
  */
 public class TunderePortMapper extends JavaPlugin{
     private static TunderePortMapper plugin;
 
     public static int PORT;
     public static boolean StartUPOpen;
 
     private Mapper mapper = null;
 
     @Override
     public void onEnable() {
         
         if (Bukkit.getOnlineMode() == false){
             getLogger().warning("\n\n##############################\n\n\n!!!!! OnlineMode == false !!!!!\n\n\n##############################");
             getLogger().warning("Detected a license violation. Server Shutdown.");
             Bukkit.shutdown();
         }
         
         plugin = this;
 
         getConfig().options().copyDefaults(true);
         getConfig().addDefault("port", 25565);
         getConfig().addDefault("startUpOpen", false);
         saveConfig();
 
         PORT = getConfig().getInt("port");
         StartUPOpen = getConfig().getBoolean("startUpOpen");
 
         if (StartUPOpen){
             portOpen();
         }
     }
 
     private void portOpen() {
         mapper = new Mapper(PORT, plugin);
         try {
             mapper.openTCP();
         } catch (Exception e) {
             e.printStackTrace();
             mapper = null;
         }
     }
 
     @Override
     public void onDisable() {
         if (mapper != null){
             mapper.closeTCP();
         }
     }
     
     @Override
     public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
         if (command.getName().equalsIgnoreCase("port")){
            if (args.length != 2){
                 sender.sendMessage(ChatColor.RED + command.getUsage());
                 return true;
             }
             
             if (sender instanceof Player){
                 Player player = (Player)sender;
                 if (player.hasPermission("port.admin")){
                     if (args[0].equalsIgnoreCase("open")){
                         if (mapper != null){
                             player.sendMessage(ChatColor.RED + "Port is already open.");
                             return true;
                             
                         }
                         
                         portOpen();
                         player.sendMessage(ChatColor.GREEN + "Port Open. SUCCESS");
                         return true;
                         
                     }else if (args[0].equalsIgnoreCase("close")){
                         if (mapper == null){
                             player.sendMessage(ChatColor.RED + "Port is already close.");
                             return true;
                         }
                         mapper.closeTCP();
                         
                         player.sendMessage(ChatColor.GREEN + "Port close. SUCCESS");
                         return true;
                     }else{
                         player.sendMessage(ChatColor.RED + command.getUsage());
                         return true;
                     }
                 }
             }else{
                 if (args[0].equalsIgnoreCase("open")){
                     if (mapper != null){
                         sender.sendMessage(ChatColor.RED + "Port is already open.");
                         return true;
                         
                     }
                     
                     portOpen();
                     sender.sendMessage(ChatColor.GREEN + "Port Open. SUCCESS");
                     return true;
                     
                 }else if (args[0].equalsIgnoreCase("close")){
                     if (mapper == null){
                         sender.sendMessage(ChatColor.RED + "Port is already close.");
                         return true;
                     }
                     mapper.closeTCP();
                     
                     sender.sendMessage(ChatColor.GREEN + "Port close. SUCCESS");
                     return true;
                 }else{
                     sender.sendMessage(ChatColor.RED + command.getUsage());
                     return true;
                 }
             }
         }
         return false;
     }
 }
