 package com.syd.expskills;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.logging.Logger;
 
 import net.milkbowl.vault.economy.Economy;
 
 import org.bukkit.Server;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.nijikokun.register.payment.Method;
 import com.nijikokun.register.payment.Methods;
 
 public class ExpSkills extends JavaPlugin
 {
     protected static Server server;
     protected static ExpSkills plugin;
     protected static FileConfiguration config;
     protected static YamlConfiguration skilltree;
     protected static YamlConfiguration lang;
     protected final static Logger log = Logger.getLogger("Minecraft");
     public final ServerEntityListener entityListener = new ServerEntityListener(this);
     public final ServerPlayerListener playerListener = new ServerPlayerListener(this);
     private CommandManager command = new CommandManager(this);
     public final PermissionsSystem permSys = new PermissionsSystem();
     protected static Method method;
     protected static Economy economy = null;
    PluginDescriptionFile pdffile = this.getDescription();
 
     public void onEnable()
     {
         server = getServer();
 
         // start of config.yml
         config = getConfig();
 
         String configversion = config.getString("version");
         String version = pdffile.getVersion();
 
         if (configversion.equalsIgnoreCase(version))
         {
             if (configversion.equals("0.7.0_RC2"))
             {
                 config.addDefault("general.updatetime", 6000);
             }
             else if (configversion.equals("0.6.4"))
             {
                 config.addDefault("general.formula", 0);
                 config.addDefault("general.formula_a", 0);
                 config.addDefault("general.formula_b", 0);
                 config.addDefault("general.formula_c", 0);
                 config.addDefault("general.formula_d", 0);
                 config.addDefault("general.formula_e", 0);
                 config.addDefault("general.skill_cap", 0);
                 config.addDefault("general.updatetime", 6000);
             }
         }
 
         config.addDefault("version", pdffile.getVersion());
         config.set("version", pdffile.getVersion());
         config.options().copyDefaults(true);
         saveConfig();
         // end of config.yml
 
         // start of lang.yml
         File langfile = new File(this.getDataFolder() + File.separator + "lang.yml");
 
         if (!langfile.exists())
         {
             lang = YamlConfiguration.loadConfiguration(getResource("lang.yml"));
             try
             {
                 lang.save(langfile);
             }
             catch (IOException e)
             {
                 e.printStackTrace();
             }
         }
         else
             lang = YamlConfiguration.loadConfiguration(langfile);
         // end of lang.yml
 
         // start skilltree
         if (config.getBoolean("general.use_skilltree", false) == true)
             skilltree = FileManager.loadSkilltree();
 
         // start rented timer
         long delay = config.getLong("general.updatetime", 6000);
 
         server.getScheduler().scheduleAsyncRepeatingTask(this, new Runnable()
         {
             public void run()
             {
                 RentingManager.update();
             }
         }, 0, delay);
 
         // initialize events and commands
         getCommand("exp").setExecutor(command);
         PluginManager pm = getServer().getPluginManager();
         pm.registerEvents(playerListener, this);
         pm.registerEvents(entityListener, this);
 
         // start permissions section
         permSys.start();
 
         // start economy section
         if ((config.getBoolean("general.use_economy", false)))
         {
             if (ExpSkills.server.getPluginManager().getPlugin("Vault") != null)
             {
                 RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
                 if (economyProvider != null)
                 {
                     economy = economyProvider.getProvider();
 
                     log.info("[ExpSkills] " + economy.getName() + " hooked");
                 }
             }
             if (ExpSkills.server.getPluginManager().getPlugin("Register") != null)
             {
                 // is a Runnable needed? Scheduled for removal!
                 this.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable()
                 {
                     public void run()
                     {
                         boolean a = Methods.setMethod(getServer().getPluginManager());
                         if (a)
                         {
                             method = Methods.getMethod();
                             log.info("[ExpSkills] " + method.getName() + " hooked");
                         }
                         else
                             log.severe("[ExpSkills] Hooking Economy Failed");
                     }
                 }, 0L);
             }
         }
         // end economy section
 
         log.info("[ExpSkills] " + pdffile.getName() + " " + pdffile.getVersion() + " enabled");
     }
 
     public void onDisable()
     {
         log.info("[ExpSkills] " + pdffile.getName() + " " + pdffile.getVersion() + " disabled");
     }
 }
