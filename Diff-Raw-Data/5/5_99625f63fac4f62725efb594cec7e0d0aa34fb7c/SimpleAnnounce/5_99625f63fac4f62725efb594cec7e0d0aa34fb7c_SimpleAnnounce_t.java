 package com.evosysdev.bukkit.taylorjb.simpleannounce;
 
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 import java.util.logging.Level;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.evosysdev.bukkit.taylorjb.simpleannounce.message.Message;
 import com.evosysdev.bukkit.taylorjb.simpleannounce.message.RepeatingMessage;
 
 public class SimpleAnnounce extends JavaPlugin
 {
     /**
      * Read in config file and set up scheduled tasks
      */
     public void onEnable()
     {
         loadConfig(); // load messages from config
         
         getLogger().info(getDescription().getName() + " version " + getDescription().getVersion() + " enabled!");
     }
     
     /**
      * Read/load config
      */
     private void loadConfig()
     {
         Set<String> messageNodes; // message nodes
         ConfigurationSection messageSection; // configuration section of the messages
         
         // make sure config has all required things/update if necessary
         validateConfig();
         
         // delete all old tasks if they exist
         getServer().getScheduler().cancelTasks(this);
         
         // load debug mode
         if (getConfig().getBoolean("debug-mode", false))
         {
             getLogger().setLevel(Level.FINER);
         }
         
         // load auto-reload + create task to check again if necessary
         int reloadTime = getConfig().getInt("auto-reloadconfig", 0);
         if (reloadTime != 0)
         {
             getServer().getScheduler().scheduleAsyncDelayedTask(this, new Runnable()
             {
                 public void run()
                 {
                     reloadConfig();
                     loadConfig();
                 }
             }, reloadTime * 60 * 20L);
             getLogger().fine("Will reload config in " + reloadTime + " minutes");
         }
         
         // load message nodes
         messageSection = getConfig().getConfigurationSection("messages");
         messageNodes = messageSection.getKeys(false);
         
         addMessages(messageNodes, messageSection);
     }
     
     /**
      * Validate nodes, if they don't exist or are wrong, set them
      * and resave config
      * 
      * Unfortunately we cannot use defaults because contains will
      * return true if node set in default OR config.
      * (thatssodumb.jpg, rage.mkv, etc etc)
      */
     private void validateConfig()
     {
         boolean updated = false; // track if we've updated config
         
         // settings
         if (!getConfig().contains("auto-reloadconfig") || !getConfig().isInt("auto-reloadconfig"))
         {
             getConfig().set("auto-reloadconfig", 20);
             updated = true;
         }
         
         if (!getConfig().contains("debug-mode") || !getConfig().isBoolean("debug-mode"))
         {
             getConfig().set("debug-mode", false);
             updated = true;
         }
         
         // messages
         if (!getConfig().contains("messages"))
         {
             getConfig().set("messages.default1.message", "This is an automatically generated repeating message!");
             getConfig().set("messages.default1.delay", 15);
             getConfig().set("messages.default1.repeat", 60);
             getConfig().set("messages.default2.message", "This is another automatically generated repeating message for people with build permission!");
             getConfig().set("messages.default2.delay", 30);
             getConfig().set("messages.default2.repeat", 60);
             List<String> df2Includes = new LinkedList<String>();
             df2Includes.add("permissions.build");
             getConfig().set("messages.default2.includesperms", df2Includes);
             getConfig().set("messages.default3.message", "This is an automatically generated one-time message!");
             getConfig().set("messages.default3.delay", 45);
             updated = true;
         }
         
         // if nodes have been updated, update header then save
         if (updated)
         {
             // set header for information
             getConfig().options().header(
                     "Config nodes:\n" +
                     "\n" +
                     "auto-reloadconfig(int): <Time in minutes to check/reload config for message updates(0 for off)>\n" +
                     "    NOTE: When config is reloaded, will reset delays for messages and cause one-time messages to resend\n" +
                     "debug-mode(boolean): <Should we pring debug to server.log(true/false)?>\n" +
                     "    NOTE: Look for fine and finer level log messages in server.log\n" +
                     "messages: Add messages below this, see below\n" +
                     "\n" +
                     "Messages config overview:\n" +
                     "-------------------------\n" +
                     "\n" +
                     "<message label>(String, must be unique):\n" +
                     "    message(String, required): <Message to send>\n" +
                     "    delay(int, optional - default 0): <Delay to send message on in seconds>\n" +
                     "    repeat(int, optional): <time between repeat sendings of the message in seconds>\n" +
                     "    includesperms(String list, optional):\n" +
                     "    - <only send to those with this perm>\n" +
                     "    - <and this one>\n" +
                     "    excludesperms(String list, optional):\n" +
                     "    - <don't send to those with this perm>\n" +
                     "    - <and this one>\n" +
                     "\n" +
                     "-------------------------\n" +
                     "\n" +
                     "add messages you would like under 'messages:' section\n" +
                     "");
             
             // save
             saveConfig();
             getLogger().info(getDescription().getName() + " config file updated, please check settings!");
         }
         
     }
     
     /**
      * Add messages from config to message list
      * 
      * @param nodes
      *            message nodes in config
      * @param section
      *            currnet config section
      */
     @SuppressWarnings("unchecked")
     private void addMessages(Set<String> nodes, ConfigurationSection section)
     {
         ConfigurationSection currentSec; // current message config section
         Message current; // current message we're working with
         String label; // unique message label
         String message; // actual message text
         int delay; // delay of message
         int repeat; // repeat timer of message
         
         // go through all message nodes and get data from it
         for (String messageNode : nodes)
         {
             // set current section
             currentSec = section.getConfigurationSection(messageNode);
             
             // get message info from nodes
             label = messageNode;
             message = currentSec.getString("message");
             delay = currentSec.getInt("delay", 0);
             
             // repeating message
             if (currentSec.contains("repeat"))
             {
                 repeat = currentSec.getInt("repeat"); // repeat specific
                 
                 // create repeating message
                 current = new RepeatingMessage(this, label, message, delay, repeat);
             }
             else
             {
                 // create message
                 current = new Message(this, label, message, delay);
             }
             
             // let's add permission includes for the message now
             if (currentSec.contains("includesperms"))
             {
                current.addPermissionsIncl(currentSec.getStringList("includesperms"));
             }
             
             // let's add permission excludes for the message now
             if (currentSec.contains("excludesperms"))
             {
                current.addPermissionsExcl(currentSec.getStringList("excludesperms"));
             }
             
             // and finally, add the message to our list
             startMessage(current);
         }
     }
     
     /**
      * Kick off/schedule messages
      * 
      * @param message
      *          message we are starting
      */
     private void startMessage(Message message)
     {
         if (message instanceof RepeatingMessage)
         {
             getServer().getScheduler().scheduleAsyncRepeatingTask(
                     this, message, message.getDelay() * 20L, ((RepeatingMessage) message).getPeriod() * 20L);
         }
         else
         {
             getServer().getScheduler().scheduleSyncDelayedTask(this, message, message.getDelay() * 20L);
         }
     }
     
     @Override
     public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
     {
         if (command.getName().equalsIgnoreCase("simpleannounce"))
         {
             if (sender.hasPermission("simpleannounce"))
             {
                 // reload command
                 if (args.length > 0 && (args[0].equalsIgnoreCase("reload") || args[0].equalsIgnoreCase("r")))
                 {
                     if (sender.hasPermission("simpleannounce.reload"))
                     {
                         reloadConfig(); // reload file
                         loadConfig(); // read config
                         
                         getLogger().fine("Config reloaded.");
                         sender.sendMessage("SimpleAnnounce config reloaded");
                     }
                     else
                     {
                         sender.sendMessage(ChatColor.RED + "You do not have permission to do that!");
                     }
                 }
                 // help command
                 else
                 {
                     sender.sendMessage(ChatColor.AQUA + "/" + getCommand("simpleannounce").getName() + ChatColor.WHITE + " | " + ChatColor.BLUE
                             + getCommand("simpleannounce").getDescription());
                     sender.sendMessage("Usage: " + ChatColor.GRAY + getCommand("simpleannounce").getUsage());
                 }
             }
             else
             {
                 sender.sendMessage(ChatColor.RED + "You do not have permission to do that!");
             }
             return true;
         }
         
         return false;
     }
     
     /**
      * plugin disabled
      */
     public void onDisable()
     {
         getLogger().info("SimpleAnnounce disabled.");
     }
 }
