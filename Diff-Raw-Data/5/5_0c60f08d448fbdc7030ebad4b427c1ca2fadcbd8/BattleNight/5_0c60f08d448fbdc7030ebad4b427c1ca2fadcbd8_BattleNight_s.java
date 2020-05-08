 package me.limebyte.battlenight.core;
 
 import java.io.IOException;
 import java.util.logging.Level;
 
 import me.limebyte.battlenight.api.BattleNightAPI;
 import me.limebyte.battlenight.api.BattleNightPlugin;
 import me.limebyte.battlenight.api.battle.Arena;
 import me.limebyte.battlenight.api.battle.Waypoint;
 import me.limebyte.battlenight.core.commands.CommandManager;
 import me.limebyte.battlenight.core.hooks.Metrics;
 import me.limebyte.battlenight.core.hooks.Nameplates;
 import me.limebyte.battlenight.core.listeners.CheatListener;
 import me.limebyte.battlenight.core.listeners.CommandBlocker;
 import me.limebyte.battlenight.core.listeners.DeathListener;
 import me.limebyte.battlenight.core.listeners.DisconnectListener;
 import me.limebyte.battlenight.core.listeners.HealthListener;
 import me.limebyte.battlenight.core.listeners.InteractListener;
 import me.limebyte.battlenight.core.listeners.NameplateListener;
 import me.limebyte.battlenight.core.listeners.RespawnListener;
 import me.limebyte.battlenight.core.listeners.SignChanger;
 import me.limebyte.battlenight.core.listeners.SignListener;
 import me.limebyte.battlenight.core.managers.ArenaManager;
 import me.limebyte.battlenight.core.managers.ClassManager;
 import me.limebyte.battlenight.core.old.OldBattle;
 import me.limebyte.battlenight.core.util.Messenger;
 import me.limebyte.battlenight.core.util.SafeTeleporter;
 import me.limebyte.battlenight.core.util.config.ConfigManager;
 import me.limebyte.battlenight.core.util.config.ConfigManager.Config;
 
 import org.bukkit.configuration.serialization.ConfigurationSerialization;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class BattleNight extends JavaPlugin implements BattleNightPlugin {
 
     /** Variables **/
 
     public static BattleNight instance;
     private BattleNightAPI api;
 
     private static OldBattle oldBattle;
 
     /** Events **/
 
     @Override
     public void onEnable() {
         instance = this;
         api = new API();
         api.setBattle(new ClassicBattle());
 
         oldBattle = new OldBattle();
 
        ConfigManager.initConfigurations();

         // Metrics
         try {
             Metrics metrics = new Metrics(this);
             metrics.start();
         } catch (IOException e) {
             // Failed to submit the stats :-(
         }
 
         PluginManager pm = getServer().getPluginManager();
 
         Nameplates.init(this, pm);
 
         // Event Registration
         pm.registerEvents(new CheatListener(), this);
         pm.registerEvents(new CommandBlocker(), this);
         pm.registerEvents(new HealthListener(), this);
         pm.registerEvents(new DeathListener(), this);
         pm.registerEvents(new DisconnectListener(), this);
         pm.registerEvents(new NameplateListener(), this);
         pm.registerEvents(new InteractListener(), this);
         pm.registerEvents(new RespawnListener(), this);
         pm.registerEvents(new SafeTeleporter(), this);
         pm.registerEvents(new SignChanger(), this);
         pm.registerEvents(new SignListener(), this);
 
         // Commands
         getCommand("battlenight").setExecutor(new CommandManager());
 
         // Setup Managers
         ConfigurationSerialization.registerClass(Arena.class);
         ConfigurationSerialization.registerClass(Waypoint.class);
         ClassManager.reloadClasses();
         ArenaManager.loadArenas();
 
         // Debugging
         if (ConfigManager.get(Config.MAIN).getBoolean("UsePermissions", false)) {
             Messenger.debug(Level.INFO, "Permissions Enabled.");
         } else {
             Messenger.debug(Level.INFO, "Permissions Disabled, using Op.");
         }
         String loadedClasses = ClassManager.getClassNames().keySet().toString();
         Messenger.debug(Level.INFO, "Loaded Classes: " + loadedClasses.replaceAll("\\[|\\]", "") + ".");
 
         // Enable Message
         Messenger.log(Level.INFO, "Version " + getDescription().getVersion() + " enabled successfully.");
         Messenger.log(Level.INFO, "Made by LimeByte.");
     }
 
     @Override
     public void onDisable() {
         if (getBattle().isInProgress() || getBattle().isInLounge()) {
             Messenger.log(Level.INFO, "Ending current Battle...");
             oldBattle.stop();
         }
         SignListener.cleanSigns();
 
         // Stop the current Battle
         getAPI().getBattle().stop();
 
         // Save arenas
         ArenaManager.saveArenas();
 
         // Disable message
         PluginDescriptionFile pdfFile = getDescription();
         Messenger.log(Level.INFO, "Version " + pdfFile.getVersion() + " has been disabled.");
     }
 
     @Override
     public BattleNightAPI getAPI() {
         return api;
     }
 
     public static OldBattle getBattle() {
         return oldBattle;
     }
 }
