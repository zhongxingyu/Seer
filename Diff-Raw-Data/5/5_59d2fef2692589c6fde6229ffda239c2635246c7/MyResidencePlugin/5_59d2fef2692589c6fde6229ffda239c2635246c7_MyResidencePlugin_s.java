 /*
  * MyResidence, Bukkit plugin for managing your towns and residences
  * Copyright (C) 2011, Michael Hohl
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package at.co.hohl.myresidence.bukkit;
 
 import at.co.hohl.myresidence.MyResidence;
 import at.co.hohl.myresidence.Nation;
 import at.co.hohl.myresidence.bukkit.listener.*;
 import at.co.hohl.myresidence.bukkit.persistent.PersistNation;
 import at.co.hohl.myresidence.commands.GeneralCommands;
 import at.co.hohl.myresidence.commands.HomeCommands;
 import at.co.hohl.myresidence.commands.LikeCommands;
 import at.co.hohl.myresidence.commands.MapCommand;
 import at.co.hohl.myresidence.event.EventManager;
 import at.co.hohl.myresidence.exceptions.*;
 import at.co.hohl.myresidence.storage.Configuration;
 import at.co.hohl.myresidence.storage.Session;
 import at.co.hohl.myresidence.storage.SessionManager;
 import at.co.hohl.myresidence.storage.persistent.*;
 import com.nijikokun.register.payment.Method;
 import com.nijikokun.register.payment.Methods;
 import com.sk89q.minecraft.util.commands.*;
 import com.sk89q.worldedit.IncompleteRegionException;
 import com.sk89q.worldedit.bukkit.WorldEditPlugin;
 import com.sk89q.worldedit.commands.InsufficientArgumentsException;
 import org.bukkit.ChatColor;
 import org.bukkit.World;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import javax.persistence.PersistenceException;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Logger;
 
 /**
  * Implementation of MyResidence as a bukkit plugin.
  *
  * @author Michael Home
  */
 public class MyResidencePlugin extends JavaPlugin implements MyResidence {
   /**
    * Manager for all commands bound to this plugin.
    */
   private CommandsManager<Player> commands;
 
   /**
    * Maps loaded configuration to worlds.
    */
   private Map<String, Configuration> configurationMap = new HashMap<String, Configuration>();
 
   /**
    * SessionManager used by this plugin.
    */
   private SessionManager sessionManager;
 
   /**
    * Nation held by this plugin.
    */
   private Nation nation;
 
   /**
    * Logger used by this plugin.
    */
   private final Logger logger = Logger.getLogger("Minecraft.MyResidence");
 
   /**
    * Payment methods.
    */
   private Methods methods;
 
   /**
    * WorldEdit plugin.
    */
   private WorldEditPlugin worldEdit;
 
   /**
    * Manager of the events.
    */
   private EventManager eventManager;
 
   /**
    * Called on enabling this plugin.
    */
   public void onEnable() {
     eventManager = new EventManager(this);
     methods = new Methods();
     nation = new PersistNation(this);
    sessionManager = new SessionManager(nation);
 
     setupDatabase();
     setupListeners();
     setupCommands();
 
     info("version %s enabled!", getDescription().getVersion());
   }
 
   /**
    * Called on disabling this plugin.
    */
   public void onDisable() {
     info("version %s disabled!", getDescription().getVersion());
   }
 
   /**
    * Called when user uses a command bind to this application.
    *
    * @param sender  the sender of the command.
    * @param command the command itself.
    * @param label   the label used for calling the command.
    * @param args    the arguments passed to the command.
    * @return true, if the plugin handles the command.
    */
   @Override
   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
     if (!(sender instanceof Player) && !commands.hasCommand(label)) {
       return false;
     }
 
     Player player = (Player) sender;
     Session session = sessionManager.get(player);
 
     try {
       long start = System.currentTimeMillis();
 
       String[] commandLine = new String[args.length + 1];
       commandLine[0] = label;
       System.arraycopy(args, 0, commandLine, 1, args.length);
 
       try {
         commands.execute(commandLine, player, this, nation, player, session);
       } catch (CommandPermissionsException e) {
         player.sendMessage(ChatColor.RED + "You don't have permission to do this.");
       } catch (MissingNestedCommandException e) {
         player.sendMessage(ChatColor.RED + e.getUsage());
       } catch (CommandUsageException e) {
         player.sendMessage(ChatColor.RED + e.getMessage());
         player.sendMessage(ChatColor.RED + e.getUsage());
       } catch (WrappedCommandException e) {
         throw e.getCause();
       } catch (UnhandledCommandException e) {
         return false;
       } finally {
         if (session.isDebugger()) {
           long time = System.currentTimeMillis() - start;
           player.sendMessage(String.format("%s%d milliseconds elapsed.", ChatColor.LIGHT_PURPLE, time));
         }
       }
     } catch (NumberFormatException e) {
       player.sendMessage(ChatColor.LIGHT_PURPLE + "Number expected; string given.");
     } catch (NoResidenceSelectedException e) {
       player.sendMessage(ChatColor.RED + "You have to be inside a residence or select it by clicking the sign!");
     } catch (NoTownSelectedException e) {
       player.sendMessage(ChatColor.RED + "You need to select a town before!");
       player.sendMessage(ChatColor.RED + "Use /town select <name> to select a town.");
     } catch (ResidenceSignMissingException e) {
       player.sendMessage(ChatColor.LIGHT_PURPLE + "Residence sign is missing!");
     } catch (NotOwnException e) {
       player.sendMessage(ChatColor.RED + "You are not the owner!");
     } catch (IncompleteRegionException e) {
       player.sendMessage(ChatColor.LIGHT_PURPLE + "Make a region selection first.");
     } catch (InsufficientArgumentsException e) {
       player.sendMessage(ChatColor.RED + e.getMessage());
     } catch (MyResidenceException e) {
       player.sendMessage(ChatColor.RED + e.getMessage());
     } catch (Throwable exception) {
       player.sendMessage(ChatColor.RED + "Please report this error: [See console]");
       player.sendMessage(ChatColor.RED + exception.getClass().getName() + ": " + exception.getMessage());
       exception.printStackTrace();
     }
 
     return true;
   }
 
   /**
    * @param world the world to get configuration.
    * @return the main configuration for the plugin.
    */
   public Configuration getConfiguration(World world) {
     if (configurationMap.containsKey(world.getName())) {
       return configurationMap.get(world.getName());
     } else {
       org.bukkit.util.config.Configuration bukkitConfig =
               new org.bukkit.util.config.Configuration(new File(getDataFolder(), world.getName() + ".yml"));
       Configuration configuration = new Configuration(bukkitConfig);
       configurationMap.put(world.getName(), configuration);
       return configuration;
     }
   }
 
   /**
    * Checks if the player has the passed permission.
    *
    * @param player     player to check.
    * @param permission permission to check.
    * @return true, if the players owns the permission.
    */
   public boolean hasPermission(Player player, String permission) {
     //return player.hasPermission(permission);
     if (hasWorldEdit()) {
       return worldEdit.getPermissionsResolver().hasPermission(player.getName(), permission);
     } else {
       return player.isOp();
     }
   }
 
   /**
    * @return the collection of towns and residences.
    */
   public Nation getNation() {
     return nation;
   }
 
   /**
    * @return the SessionManager used by this MyResidence implementation.
    */
   public SessionManager getSessionManager() {
     return sessionManager;
   }
 
   /**
    * @return all available payment methods.
    */
   public Methods getPaymentMethods() {
     return methods;
   }
 
   /**
    * @return world edit plugin.
    */
   public WorldEditPlugin getWorldEdit() {
     if (worldEdit == null) {
       throw new NullPointerException("Miss valid WorldEdit installation!");
     }
 
     return worldEdit;
   }
 
   public void setWorldEdit(WorldEditPlugin worldEdit) {
     this.worldEdit = worldEdit;
   }
 
   public boolean hasWorldEdit() {
     return worldEdit != null;
   }
 
   /**
    * @return the event manager.
    */
   public EventManager getEventManager() {
     return eventManager;
   }
 
   /**
    * Formats the passed amount of money to a localized string.
    *
    * @param money the amount of money.
    * @return a string for the amount of money.
    */
   public String format(double money) {
     Method payment = getPaymentMethods().getMethod();
 
     if (payment == null) {
       return String.format("%.2f", money);
     } else {
       return payment.format(money);
     }
   }
 
   /**
    * Logs an message with the level info.
    *
    * @param message the message to log.
    */
   public void info(String message, Object... args) {
     String formattedMessage = String.format(message, args);
     logger.info(String.format("[%s] %s", getDescription().getName(), formattedMessage));
   }
 
   /**
    * Logs an message with the level warning.
    *
    * @param message the message to log.
    */
   public void warning(String message, Object... args) {
     String formattedMessage = String.format(message, args);
     logger.warning(String.format("[%s] %s", getDescription().getName(), formattedMessage));
   }
 
   /**
    * Logs an message with the level severe.
    *
    * @param message the message to log.
    */
   public void severe(String message, Object... args) {
 
     String formattedMessage = String.format(message, args);
     logger.severe(String.format("[%s] %s", getDescription().getName(), formattedMessage));
   }
 
   /**
    * Setups the listeners for the plugin.
    */
   private void setupListeners() {
     PluginManager pluginManager = getServer().getPluginManager();
 
     // Listen for WorldEdit.
     WorldEditPluginListener worldEditPluginListener = new WorldEditPluginListener(this);
     pluginManager.registerEvent(Event.Type.PLUGIN_ENABLE, worldEditPluginListener, Event.Priority.Monitor, this);
     pluginManager.registerEvent(Event.Type.PLUGIN_DISABLE, worldEditPluginListener, Event.Priority.Monitor, this);
 
     // Listen for Economy Plugins.
     EconomyPluginListener economyPluginListener = new EconomyPluginListener(this);
     pluginManager.registerEvent(Event.Type.PLUGIN_ENABLE, economyPluginListener, Event.Priority.Monitor, this);
     pluginManager.registerEvent(Event.Type.PLUGIN_DISABLE, economyPluginListener, Event.Priority.Monitor, this);
 
     // Listen for player clicking on signs.
     SignClickListener signClickListener = new SignClickListener(this, nation);
     pluginManager.registerEvent(Event.Type.PLAYER_INTERACT, signClickListener, Event.Priority.Normal, this);
 
     // Listen for players broke signs.
     SignBrokeListener signBrokeListener = new SignBrokeListener(this, nation);
     pluginManager.registerEvent(Event.Type.BLOCK_BREAK, signBrokeListener, Event.Priority.Normal, this);
 
     // Listen for residences.
     SignUpdateListener signUpdateListener = new SignUpdateListener(nation, this);
     getEventManager().addListener(signUpdateListener);
   }
 
   /**
    * Setups the commands.
    */
   private void setupCommands() {
     commands = new CommandsManager<Player>() {
       @Override
       public boolean hasPermission(Player player, String permission) {
        hasPermission(player, permission);
       }
     };
 
     commands.register(GeneralCommands.class);
     commands.register(MapCommand.class);
     commands.register(HomeCommands.class);
     commands.register(LikeCommands.class);
   }
 
   /**
    * Creates needed databases.
    */
   private void setupDatabase() {
     try {
       info("test databases for MyResidence...");
       getDatabase().find(Residence.class).findRowCount();
       getDatabase().find(Town.class).findRowCount();
       getDatabase().find(Inhabitant.class).findRowCount();
       getDatabase().find(Major.class).findRowCount();
       getDatabase().find(ResidenceArea.class).findRowCount();
       getDatabase().find(ResidenceSign.class).findRowCount();
       getDatabase().find(ResidenceFlag.class).findRowCount();
       getDatabase().find(ResidenceMember.class).findRowCount();
       getDatabase().find(TownChunk.class).findRowCount();
       getDatabase().find(TownFlag.class).findRowCount();
       getDatabase().find(TownRule.class).findRowCount();
       getDatabase().find(Like.class).findRowCount();
       info("databases ready!");
     } catch (PersistenceException ex) {
       info("Installing database due to first time usage!");
       installDDL();
     }
   }
 
   /**
    * @return all DAOs of this plugin.
    */
   @Override
   public List<Class<?>> getDatabaseClasses() {
     List<Class<?>> list = new ArrayList<Class<?>>();
     list.add(Residence.class);
     list.add(Town.class);
     list.add(HomePoint.class);
     list.add(Inhabitant.class);
     list.add(Major.class);
     list.add(ResidenceArea.class);
     list.add(ResidenceSign.class);
     list.add(ResidenceFlag.class);
     list.add(ResidenceMember.class);
     list.add(TownChunk.class);
     list.add(TownFlag.class);
     list.add(TownRule.class);
     list.add(Like.class);
     return list;
   }
 }
