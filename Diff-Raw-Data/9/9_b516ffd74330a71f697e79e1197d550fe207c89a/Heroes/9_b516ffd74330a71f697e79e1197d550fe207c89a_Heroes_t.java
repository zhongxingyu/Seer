 package com.herocraftonline.dev.heroes;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.bukkit.Bukkit;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event.Type;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.herocraftonline.dev.heroes.classes.ClassManager;
 import com.herocraftonline.dev.heroes.classes.HeroClass;
 import com.herocraftonline.dev.heroes.command.BaseCommand;
 import com.herocraftonline.dev.heroes.command.CommandManager;
 import com.herocraftonline.dev.heroes.command.commands.AdminClassCommand;
 import com.herocraftonline.dev.heroes.command.commands.AdminExpCommand;
 import com.herocraftonline.dev.heroes.command.commands.ArmorCommand;
 import com.herocraftonline.dev.heroes.command.commands.BindSkillCommand;
 import com.herocraftonline.dev.heroes.command.commands.ChooseCommand;
 import com.herocraftonline.dev.heroes.command.commands.ConfigReloadCommand;
 import com.herocraftonline.dev.heroes.command.commands.HelpCommand;
 import com.herocraftonline.dev.heroes.command.commands.HeroSaveCommand;
 import com.herocraftonline.dev.heroes.command.commands.LeaderboardCommand;
 import com.herocraftonline.dev.heroes.command.commands.LevelInformationCommand;
 import com.herocraftonline.dev.heroes.command.commands.ManaCommand;
 import com.herocraftonline.dev.heroes.command.commands.PartyAcceptCommand;
 import com.herocraftonline.dev.heroes.command.commands.PartyCreateCommand;
 import com.herocraftonline.dev.heroes.command.commands.PartyInviteCommand;
 import com.herocraftonline.dev.heroes.command.commands.PartyLeaveCommand;
 import com.herocraftonline.dev.heroes.command.commands.PartyModeCommand;
 import com.herocraftonline.dev.heroes.command.commands.PartyWhoCommand;
 import com.herocraftonline.dev.heroes.command.commands.PathsCommand;
 import com.herocraftonline.dev.heroes.command.commands.RecoverItemsCommand;
 import com.herocraftonline.dev.heroes.command.commands.SkillListCommand;
 import com.herocraftonline.dev.heroes.command.commands.SpecsCommand;
 import com.herocraftonline.dev.heroes.command.commands.SuppressCommand;
 import com.herocraftonline.dev.heroes.command.commands.ToolsCommand;
 import com.herocraftonline.dev.heroes.command.commands.VerboseCommand;
 import com.herocraftonline.dev.heroes.command.commands.WhoCommand;
 import com.herocraftonline.dev.heroes.inventory.BukkitContribInventoryListener;
 import com.herocraftonline.dev.heroes.inventory.HeroesInventoryListener;
 import com.herocraftonline.dev.heroes.inventory.InventoryChecker;
 import com.herocraftonline.dev.heroes.party.PartyCustomListener;
 import com.herocraftonline.dev.heroes.party.PartyEntityListener;
 import com.herocraftonline.dev.heroes.party.PartyManager;
 import com.herocraftonline.dev.heroes.persistence.Hero;
 import com.herocraftonline.dev.heroes.persistence.HeroManager;
 import com.herocraftonline.dev.heroes.skill.OutsourcedSkill;
 import com.herocraftonline.dev.heroes.skill.Skill;
 import com.herocraftonline.dev.heroes.skill.SkillLoader;
 import com.herocraftonline.dev.heroes.util.ConfigManager;
 import com.herocraftonline.dev.heroes.util.DebugLog;
 import com.nijiko.permissions.PermissionHandler;
 import com.nijikokun.bukkit.Permissions.Permissions;
 import com.nijikokun.register.payment.Method;
 
 /**
  * Heroes Plugin for Herocraft
  * 
  * @author Herocraft's Plugin Team
  */
 public class Heroes extends JavaPlugin {
 
     // Using this instead of getDataFolder(), getDataFolder() uses the File
     // Name. We wan't a constant folder name.
     public static final File dataFolder = new File("plugins" + File.separator + "Heroes");
 
     // Simple hook to Minecraft's logger so we can output to the console.
     private static final Logger log = Logger.getLogger("Minecraft");
     private static DebugLog debugLog;
 
     // Setup the Player and Plugin listener for Heroes.
     private final HPlayerListener playerListener = new HPlayerListener(this);
     private final HPluginListener pluginListener = new HPluginListener(this);
     private final HEntityListener entityListener = new HEntityListener(this);
     private final HBlockListener blockListener = new HBlockListener(this);
 
     // Various data managers
     private ConfigManager configManager;
     private CommandManager commandManager = new CommandManager();
     private ClassManager classManager;
     private HeroManager heroManager;
     private PartyManager partyManager;
 
     // Variable for the Permissions plugin handler.
     public static PermissionHandler Permissions;
     public Method Method = null;
 
     // Variable for BukkitContrib.
     public static boolean useBukkitContrib = false;
 
     // Inventory Event listeners for both Heroes and BukkitContrib
     private final HeroesInventoryListener heroesInventoryListener = new HeroesInventoryListener(this);
     private BukkitContribInventoryListener bukkitContribInventoryListener;
     
     //Party Listener
     private PartyEntityListener partyEntityListener = new PartyEntityListener(this);
     private PartyCustomListener partyCustomListener = new PartyCustomListener(this);
     
     // Inventory Checker Class -- This class has the methods to check a players inventory and
     // restrictions.
     private final InventoryChecker inventoryChecker = new InventoryChecker(this);
 
     @Override
     public void onLoad() {
         dataFolder.mkdirs(); // Create the Heroes Plugin Directory.
         configManager = new ConfigManager(this);
         heroManager = new HeroManager(this);
         partyManager = new PartyManager(this);
         debugLog = new DebugLog("Heroes", dataFolder + File.separator + "debug.log");
     }
 
     @Override
     public void onEnable() {
         log(Level.INFO, "version " + getDescription().getVersion() + " is enabled!");
 
         // Check for BukkitContrib
         setupBukkitContrib();
 
         // Setup the Property for Levels * Exp
         getConfigManager().getProperties().calcExp();
 
         // Skills Loader
         loadSkills();
 
         // Attempt to load the Configuration file.
         try {
             configManager.load();
         } catch (Exception e) {
             e.printStackTrace();
         }
 
         blockListener.init();
 
         final Player[] players = getServer().getOnlinePlayers();
         for (Player player : players) {
             switchToHNSH(player);
             heroManager.loadHeroFile(player);
             getInventoryChecker().checkInventory(player);
         }
 
         // Call our function to register the events Heroes needs.
         registerEvents();
         // Call our function to setup Heroes Commands.
         registerCommands();
 
         // Perform the Permissions check.
         setupPermissions();
     }
 
     /**
      * Perform a Permissions check and setup Permissions if found.
      */
     public void setupPermissions() {
         Plugin test = this.getServer().getPluginManager().getPlugin("Permissions");
         if (Heroes.Permissions == null) {
             if (test != null) {
                 Heroes.Permissions = ((Permissions) test).getHandler();
                 log(Level.INFO, "Permissions found.");
                 final Player[] players = getServer().getOnlinePlayers();
                 for (Player player : players) {
                     Hero hero = heroManager.getHero(player);
                     HeroClass heroClass = hero.getHeroClass();
 
                     for (BaseCommand cmd : commandManager.getCommands()) {
                         if (cmd instanceof OutsourcedSkill) {
                             ((OutsourcedSkill) cmd).tryLearningSkill(hero);
                         }
                     }
 
                     if (Heroes.Permissions != null && heroClass != classManager.getDefaultClass()) {
                         if (!Heroes.Permissions.has(player, "heroes.classes." + heroClass.getName().toLowerCase())) {
                             hero.setHeroClass(classManager.getDefaultClass());
                         }
                     }
                 }
             }
         }
     }
 
     /**
      * Check to see if BukkitContrib is enabled on the server, if so inform Heroes to use BukkitContrib instead.
      */
     public void setupBukkitContrib() {
         Plugin test = this.getServer().getPluginManager().getPlugin("BukkitContrib");
         if (test != null) {
             Heroes.useBukkitContrib = true;
             bukkitContribInventoryListener = new BukkitContribInventoryListener(this);
             Bukkit.getServer().getPluginManager().registerEvent(Type.CUSTOM_EVENT, bukkitContribInventoryListener, Priority.Monitor, this);
         } else {
             Heroes.useBukkitContrib = false;
             bukkitContribInventoryListener = null;
         }
     }
 
     /**
      * Handle Heroes Commands, in this case we send them straight to the commandManager.
      */
     @Override
     public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
         return commandManager.dispatch(sender, command, label, args);
     }
 
     /**
      * Register the Events which Heroes requires.
      */
     private void registerEvents() {
         PluginManager pluginManager = getServer().getPluginManager();
         pluginManager.registerEvent(Type.PLAYER_QUIT, playerListener, Priority.Normal, this);
         pluginManager.registerEvent(Type.PLAYER_JOIN, playerListener, Priority.Normal, this);
         pluginManager.registerEvent(Type.PLAYER_INTERACT, playerListener, Priority.Lowest, this);
         pluginManager.registerEvent(Type.PLAYER_ITEM_HELD, playerListener, Priority.Monitor, this);
         pluginManager.registerEvent(Type.PLAYER_PICKUP_ITEM, playerListener, Priority.Monitor, this);
         pluginManager.registerEvent(Type.PLAYER_TELEPORT, playerListener, Priority.Monitor, this);
         
         pluginManager.registerEvent(Type.ENTITY_DAMAGE, partyEntityListener, Priority.Highest, this);
         pluginManager.registerEvent(Type.ENTITY_DAMAGE, entityListener, Priority.Monitor, this);
         pluginManager.registerEvent(Type.ENTITY_DEATH, entityListener, Priority.Monitor, this);
         pluginManager.registerEvent(Type.ENTITY_TARGET, entityListener, Priority.Normal, this);
 
         pluginManager.registerEvent(Type.BLOCK_BREAK, blockListener, Priority.Monitor, this);
         pluginManager.registerEvent(Type.BLOCK_PLACE, blockListener, Priority.Monitor, this);
 
         pluginManager.registerEvent(Type.PLUGIN_ENABLE, pluginListener, Priority.Monitor, this);
         pluginManager.registerEvent(Type.PLUGIN_DISABLE, pluginListener, Priority.Monitor, this);
 
         pluginManager.registerEvent(Type.CUSTOM_EVENT, new HLevelListener(this), Priority.Monitor, this);
         pluginManager.registerEvent(Type.CUSTOM_EVENT, new HPermissionsListener(this), Priority.Monitor, this);
        pluginManager.registerEvent(Type.CUSTOM_EVENT, partyCustomListener, Priority.Highest, this);
         
         // Inventory Event Listeners
         pluginManager.registerEvent(Type.CUSTOM_EVENT, heroesInventoryListener, Priority.Monitor, this);
     }
 
     /**
      * Register Heroes commands to DThielke's Command Manager.
      */
     private void registerCommands() {
         // Page 1
         commandManager.addCommand(new PathsCommand(this));
         commandManager.addCommand(new SpecsCommand(this));
         commandManager.addCommand(new ChooseCommand(this));
         commandManager.addCommand(new LevelInformationCommand(this));
         commandManager.addCommand(new SkillListCommand(this));
         commandManager.addCommand(new BindSkillCommand(this));
         commandManager.addCommand(new ArmorCommand(this));
         commandManager.addCommand(new ToolsCommand(this));
 
         // Page 2
         commandManager.addCommand(new ManaCommand(this));
         commandManager.addCommand(new VerboseCommand(this));
         commandManager.addCommand(new SuppressCommand(this));
         commandManager.addCommand(new WhoCommand(this));
         commandManager.addCommand(new PartyAcceptCommand(this));
         commandManager.addCommand(new PartyCreateCommand(this));
         commandManager.addCommand(new PartyInviteCommand(this));
         commandManager.addCommand(new PartyWhoCommand(this));
 
         // Page 3
         commandManager.addCommand(new PartyLeaveCommand(this));
         commandManager.addCommand(new PartyModeCommand(this));
         commandManager.addCommand(new RecoverItemsCommand(this));
         commandManager.addCommand(new ConfigReloadCommand(this));
         commandManager.addCommand(new HelpCommand(this));
         commandManager.addCommand(new AdminExpCommand(this));
         commandManager.addCommand(new AdminClassCommand(this));
         commandManager.addCommand(new LeaderboardCommand(this));
 
         // Page 4
         commandManager.addCommand(new HeroSaveCommand(this));
     }
 
     /**
      * What to do during the Disabling of Heroes -- Likely save data and close connections.
      */
     @Override
     public void onDisable() {
         heroManager.stopTimers();
         final Player[] players = getServer().getOnlinePlayers();
         for (Player player : players) {
             heroManager.saveHeroFile(player);
             switchToBNSH(player);
         }
 
         this.Method = null; // When it Enables again it performs the checks anyways.
         Heroes.Permissions = null; // When it Enables again it performs the checks anyways.
         log.info(getDescription().getName() + " version " + getDescription().getVersion() + " is disabled!");
         debugLog.close();
     }
 
     public ClassManager getClassManager() {
         return classManager;
     }
 
     public void setClassManager(ClassManager classManager) {
         this.classManager = classManager;
     }
 
     /**
      * Print messages to the server Log as well as to our DebugLog. 'debugLog' is used to seperate Heroes information from the Servers Log Output.
      * 
      * @param level
      * @param msg
      */
     public void log(Level level, String msg) {
         log.log(level, "[Heroes] " + msg);
         debugLog.log(level, "[Heroes] " + msg);
     }
 
     /**
      * Print messages to the Debug Log, if the servers in Debug Mode then we also wan't to print the messages to the standard Server Console.
      * 
      * @param level
      * @param msg
      */
     public void debugLog(Level level, String msg) {
         if (this.configManager.getProperties().debug) {
             log.log(level, "[Debug] " + msg);
         }
         debugLog.log(level, "[Debug] " + msg);
     }
 
     /**
      * Load all the external classes.
      */
     public void loadSkills() {
         File dir = new File(getDataFolder(), "skills");
         ArrayList<String> skNo = new ArrayList<String>();
         dir.mkdir();
         boolean added = false;
         for (String f : dir.list()) {
             if (f.contains(".jar")) {
                 Skill skill = SkillLoader.loadSkill(new File(dir, f), this);
                 if (skill != null) {
                     commandManager.addCommand(skill);
                     if (!added) {
                         log(Level.INFO, "Collecting and loading skills");
                         added = true;
                     }
                     skNo.add(skill.getName());
                     debugLog.log(Level.INFO, "Skill " + skill.getName() + " Loaded");
                 }
             }
         }
         log(Level.INFO, "Skills loaded: " + skNo.toString());
     }
 
     public HeroManager getHeroManager() {
         return heroManager;
     }
 
     public ConfigManager getConfigManager() {
         return configManager;
     }
 
     public CommandManager getCommandManager() {
         return commandManager;
     }
 
     public PartyManager getPartyManager() {
         return partyManager;
     }
 
     public InventoryChecker getInventoryChecker() {
         return inventoryChecker;
     }
 
     public void switchToHNSH(Player player) {
         if (!(Heroes.useBukkitContrib)) {
             // Swap NSH to Heroes NSH.
         }
         // CraftPlayer craftPlayer = (CraftPlayer) player;
         // CraftServer server = (CraftServer) Bukkit.getServer();
         //
         // Location loc = player.getLocation();
         // HNetServerHandler handler = new HNetServerHandler(server.getHandle().server,
         // craftPlayer.getHandle().netServerHandler.networkManager, craftPlayer.getHandle());
         // handler.a(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
     }
 
     public void switchToBNSH(Player player) {
         if (!(Heroes.useBukkitContrib)) {
             // Swap NSH to Bukkit NSH.
         }
         // CraftPlayer craftPlayer = (CraftPlayer) player;
         // CraftServer server = (CraftServer) Bukkit.getServer();
         //
         // Location loc = player.getLocation();
         // NetServerHandler handler = new NetServerHandler(server.getHandle().server,
         // craftPlayer.getHandle().netServerHandler.networkManager, craftPlayer.getHandle());
         // handler.a(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
     }
 }
