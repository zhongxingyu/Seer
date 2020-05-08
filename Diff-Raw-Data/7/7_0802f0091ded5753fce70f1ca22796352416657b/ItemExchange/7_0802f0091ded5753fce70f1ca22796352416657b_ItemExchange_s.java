 package us.azkedar;
 
 import java.io.File;
 import java.lang.reflect.Field;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.SortedSet;
 
 import org.bukkit.command.PluginCommand;
 import org.bukkit.command.SimpleCommandMap;
 import org.bukkit.event.Event.Type;
 import org.bukkit.permissions.Permission;
 import org.bukkit.plugin.InvalidDescriptionException;
 import org.bukkit.plugin.InvalidPluginException;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.RegisteredListener;
 import org.bukkit.plugin.SimplePluginManager;
 import org.bukkit.plugin.UnknownDependencyException;
 
 import org.blockface.virtualshop.events.ServerEvents;
 import org.blockface.virtualshop.managers.EconomyManager;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.event.Event;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.util.config.Configuration;
 
 import java.util.logging.Logger;
 
 public class ItemExchange extends JavaPlugin {
     private static Configuration config;
     private static String itemName;
     private static Logger logger;
     
     public void onDisable() {
         ItemExchangeDB.Close();
     }
 
     public void onEnable() {
         logger = Logger.getLogger("minecraft");
         logger.info("ItemExchange is loading");
 
         // Load configuration settings
 	config = getConfiguration();
         ItemExchangeDB.Initialize(this);
         itemName = config.getString("item-name","Gold");
         config.save();
         RegisterEvents();
     }
 
     private void RegisterEvents()
     {
         PluginManager pm = this.getServer().getPluginManager();
         pm.registerEvent(Event.Type.PLUGIN_ENABLE, new ServerEvents(), Event.Priority.Normal, this);
     }
 
     @Override
     public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
     {
         if(label.equalsIgnoreCase("exch")) {
             if( args.length > 0) {
                 if (args[0].equalsIgnoreCase("history")) {
                     if (args.length == 2) {
                         ItemExchangeDB.history(sender,Integer.parseInt(args[1]));
                         return true;
                     }
                     else {
                         ItemExchangeDB.history(sender,1);
                         return true;
                     }
                 }
                 if (args[0].equalsIgnoreCase("buy")) {
                     if (args.length == 2) {
                         ItemExchangeDB.buy(sender,Integer.parseInt(args[1]));
                         return true;
                     }
                     else {
                         sender.sendMessage("Usage: /exch buy <quantity>");
                         return true;
                     }  
                 }
                 if (args[0].equalsIgnoreCase("sell")) {
                     if (args.length == 2) {
                         ItemExchangeDB.sell(sender,Integer.parseInt(args[1]));
                         return true;
                     }
                     else {
                         sender.sendMessage("Usage: /exch sell <quantity>");
                         return true;
                     }
                 }
                 if (args[0].equalsIgnoreCase("rate")) {
                     showRate(sender);
                     return true;
                 }
                 if (args[0].equalsIgnoreCase("restart")) {
                     if(!(sender instanceof Player) || sender.hasPermission("itemexchange.restart"))
                     {
                         try {
                             unloadPlugin("ItemExchange");
                             loadPlugin("ItemExchange");
                         } catch (Exception e) {
                             sender.sendMessage("Unknown error during plugin restart: " + e.toString());
                         }
                         sender.sendMessage("Reloaded ItemExchange");
                         return true;
                     }
                     else {
                         sender.sendMessage("You don't have permission to do that.");
                         return true;
                     }
                 }
             }
             helpCommand(sender);
         }
         return true;
     }
     
     private void showRate(CommandSender sender) {
         double price = ItemExchangeDB.current_rate();
        sender.sendMessage(ChatColor.BLUE + " [" + ChatColor.WHITE + "Current exchange rate: " + ChatColor.YELLOW +  EconomyManager.getMethod().format(price) + ChatColor.GRAY + " per " + ChatColor.LIGHT_PURPLE + itemName + ChatColor.BLUE + "]" );
     }
     
     private void helpCommand(CommandSender sender) {
         sender.sendMessage(ChatColor.BLUE + "|------------- " + ChatColor.LIGHT_PURPLE + itemName + " Exchange" + ChatColor.BLUE + " -------------|");
         sender.sendMessage(ChatColor.GREEN + "/exch" + ChatColor.WHITE + " - Show this message and current rate");
         sender.sendMessage(ChatColor.GREEN + "/exch buy " + ChatColor.GOLD + "<amount> " + ChatColor.WHITE + " - buy this many " + itemName);
         sender.sendMessage(ChatColor.GREEN + "/exch sell " + ChatColor.GOLD + "<amount> " + ChatColor.WHITE + " - sell this many " + itemName); 
         sender.sendMessage(ChatColor.GREEN + "/exch history " + ChatColor.AQUA + "[page]" + ChatColor.WHITE + " - show exchange history");
         sender.sendMessage(ChatColor.GREEN + "/exch rate " + ChatColor.WHITE + " - show current exchange rate");
         showRate(sender);
     }
     
     @SuppressWarnings("unchecked")
     private void unloadPlugin(String pluginName) throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
         PluginManager manager = getServer().getPluginManager();
 
         SimplePluginManager spm = (SimplePluginManager) manager;
 
         List<Plugin> plugins = null;
         Map<String, Plugin> lookupNames = null;
         Map<Event.Type, SortedSet<RegisteredListener>> listeners = null;
         SimpleCommandMap commandMap = null;
         Map<String, Command> knownCommands = null;
 
         if (spm != null) {
 // this is fucking ugly
 // as there is no public getters for these, and no methods to properly unload plugins
 // I have to fiddle directly in the private attributes of the plugin manager class
             Field pluginsField = spm.getClass().getDeclaredField("plugins");
             Field lookupNamesField = spm.getClass().getDeclaredField("lookupNames");
             Field listenersField = spm.getClass().getDeclaredField("listeners");
             Field commandMapField = spm.getClass().getDeclaredField("commandMap");
 
             pluginsField.setAccessible(true);
             lookupNamesField.setAccessible(true);
             listenersField.setAccessible(true);
             commandMapField.setAccessible(true);
 
             plugins = (List<Plugin>) pluginsField.get(spm);
             lookupNames = (Map<String, Plugin>) lookupNamesField.get(spm);
             listeners = (Map<Type, SortedSet<RegisteredListener>>) listenersField.get(spm);
             commandMap = (SimpleCommandMap) commandMapField.get(spm);
 
             Field knownCommandsField = commandMap.getClass().getDeclaredField("knownCommands");
 
             knownCommandsField.setAccessible(true);
 
             knownCommands = (Map<String, Command>) knownCommandsField.get(commandMap);
         }
 
 // in case the same plugin is loaded multiple times (could happen)
         for (Plugin pl : manager.getPlugins()) {
             if (pl.getDescription().getName().equalsIgnoreCase(pluginName)) {
 // disable the plugin itself
                 manager.disablePlugin(pl);
 
 // removing all traces of the plugin in the private structures (so it won't appear in the plugin list twice)
                 if (plugins != null && plugins.contains(pl)) {
                     plugins.remove(pl);
                 }
 
                 if (lookupNames != null && lookupNames.containsKey(pluginName)) {
                     lookupNames.remove(pluginName);
                 }
 
 // removing registered listeners to avoid registering them twice when reloading the plugin
                 if (listeners != null) {
                     for (SortedSet<RegisteredListener> set : listeners.values()) {
                         for (Iterator<RegisteredListener> it = set.iterator(); it.hasNext();) {
                             RegisteredListener value = it.next();
 
                             if (value.getPlugin() == pl) {
                                 it.remove();
                             }
                         }
                     }
                 }
 
 // removing registered commands, if we don't do this they can't get re-registered when the plugin is reloaded
                 if (commandMap != null) {
                     for (Iterator<Map.Entry<String, Command>> it = knownCommands.entrySet().iterator(); it.hasNext();) {
                         Map.Entry<String, Command> entry = it.next();
 
                         if (entry.getValue() instanceof PluginCommand) {
                             PluginCommand c = (PluginCommand) entry.getValue();
 
                             if (c.getPlugin() == pl) {
                                 c.unregister(commandMap);
 
                                 it.remove();
                             }
                         }
                     }
                 }
 
                 try {
                     List<Permission> permissionlist = pl.getDescription().getPermissions();
                     Iterator<Permission> p = permissionlist.iterator();
                     while (p.hasNext()) {
                         manager.removePermission(p.next().toString());
                     }
                 } catch (NoSuchMethodError e) {
 // Do nothing
                 }
 
 // ta-da! we're done (hopefully)
 // I don't know if there are more things that need to be reset
 // I'll take a more in-depth look into the bukkit source if it doesn't work well
             }
         }
     }
 
     private void loadPlugin(String pluginName) throws InvalidPluginException, InvalidDescriptionException, UnknownDependencyException {
 // loading a plugin on the other hand is way simpler
 
         PluginManager manager = getServer().getPluginManager();
 
         Plugin plugin = manager.loadPlugin(new File("plugins", pluginName + ".jar"));
 
         if (plugin == null) {
             return;
         }
 
         manager.enablePlugin(plugin);
 
         List<Permission> permissionlist = plugin.getDescription().getPermissions();
         Iterator<Permission> p = permissionlist.iterator();
         try {
             while (p.hasNext()) {
                 manager.addPermission(p.next());
             }
         } catch (NoSuchMethodError e) {
 // Do nothing
         } catch (java.lang.IllegalArgumentException e) {
             // Ignore this too
         }
     }
     
 }
