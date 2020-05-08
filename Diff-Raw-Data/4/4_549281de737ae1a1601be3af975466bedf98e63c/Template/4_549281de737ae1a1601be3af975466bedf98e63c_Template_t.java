 package com.bukkit.nicatronTg.template;
 
 import java.io.File;
 import java.util.HashMap;
 import java.util.Iterator;
 
 import org.bukkit.Server;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginLoader;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 /**
  * Template for Bukkit
  *
  * @author
  */
 public class Template extends JavaPlugin {
     private final TemplatePlayerListener playerListener = new TemplatePlayerListener(this);
     private final TemplateBlockListener blockListener = new TemplateBlockListener(this);
     private final HashMap<Player, Boolean> debugees = new HashMap<Player, Boolean>();
     
 
     public Template(PluginLoader pluginLoader, Server instance, PluginDescriptionFile desc, File folder, File plugin, ClassLoader cLoader) {
         super(pluginLoader, instance, desc, folder, plugin, cLoader);
     }
 
     public void onEnable() {
 
         // Register our events
         PluginManager pm = getServer().getPluginManager();
         //pm.registerEvent(Event.Type.BLOCK_RIGHTCLICKED, blockListener, Priority.Normal, this);
         //pm.registerEvent(Event.Type.BLOCK_DAMAGED, blockListener, Priority.Normal, this);
         // EXAMPLE: Custom code, here we just output some info so we can check all is well
         PluginDescriptionFile pdfFile = this.getDescription();
         System.out.println( pdfFile.getName() + " version " + pdfFile.getVersion() + " is now running." );
     }
     public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
         String commandName = command.getName().toLowerCase();
         Player ply = (Player)sender;
         
         return true;
     }
     
     public void onDisable() {
     }
     
     public boolean isDebugging(final Player player) {
         if (debugees.containsKey(player)) {
             return debugees.get(player);
         } else {
             return false;
         }
     }
     
     public void setDebugging(final Player player, final boolean value) {
         debugees.put(player, value);
     }
     
     public void debug(String msg){
        Iterator<Player> iterator = debugees.keySet().iterator();
         while (iterator.hasNext()){
             Player ply = (Player)iterator.next();
             ply.sendMessage(msg);
             
         }
     System.out.println(msg);
     }
     
 }
 
