 package com.carlgo11.simpleautomessage;
 
 import com.carlgo11.simpleautomessage.commands.*;
 import com.carlgo11.simpleautomessage.updater.*;
 import com.carlgo11.simpleautomessage.language.*;
 import com.carlgo11.simpleautomessage.metrics.*;
 import com.carlgo11.simpleautomessage.player.*;
 import java.io.File;
 import java.io.IOException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class Main extends JavaPlugin {
 
     public int tick = 1; // msg+<tick> int
     public int time = 0; // the delay
     public final static Logger logger = Logger.getLogger("Minecraft");
     public static YamlConfiguration LANG;
     public static File LANG_FILE;
     public boolean update = false;
 
     public void onEnable()
     {
         reloadConfig();
         getServer().getPluginManager().registerEvents(new Time(this), this);
         checkVersion();
         checkConfig();
         checkMetrics();
         getServer().getPluginManager().registerEvents(new loadLang(this), this);
         getServer().getPluginManager().registerEvents(new Broadcast(this), this);
         getServer().getPluginManager().registerEvents(new PlayerJoin(this), this);
         commands();
         getLogger().log(Level.INFO, "{0} {1} {2}", new Object[]{getDescription().getName(), getDescription().getVersion(), Lang.ENABLED});
 
     }
 
     public void onDisable()
     {
         getLogger().log(Level.INFO, "{0} {1} {2}", new Object[]{getDescription().getName(), getDescription().getVersion(), Lang.DISABLED});
     }
 
     public void commands()
     {
         getCommand("simpleautomessage").setExecutor(new SimpleautomessageCommand(this));
     }
 
     public void checkVersion()
     {
         if (getDescription().getVersion().startsWith("dev-")) {
             getLogger().warning("You are using a development build! Keep in mind development builds may contain bugs!");
             getLogger().warning("If you want a fully working version please use a recommended build!");
            getLogger().warning("Type /simpleautomessage update to download the latest recommended build.");
         }
 
         if (getConfig().getBoolean("auto-update") == true) {
             onDebug("Calling Updater.java");
             Updater updater = new Updater(this, "simpleautomessage/", getFile(), Updater.UpdateType.DEFAULT, true);
             update = updater.getResult() == Updater.UpdateResult.UPDATE_AVAILABLE;
 
         } else {
             onDebug("auto-update: is set to false!");
         }
     }
 
     public void checkMetrics()
     {
         try {
             Metrics metrics = new Metrics(this);
             graphs(metrics);
             metrics.start();
         } catch (IOException e) {
             System.out.println("[" + getDescription().getName() + "] " + Lang.STATS_ERROR);
         }
     }
 
     public void checkConfig()
     {
         File config = new File(getDataFolder(), "config.yml");
         if (!config.exists()) {
             saveDefaultConfig();
             System.out.println("[" + getDescription().getName() + "] " + "No config.yml detected, config.yml created.");
         }
         if (!getConfig().getString("version").equals(getDescription().getVersion())) {
             if (!getDescription().getVersion().startsWith("dev-")) {
                 config.renameTo(new File(getDataFolder(), "config.version-" + getConfig().getString("version") + ".yml"));
                 saveDefaultConfig();
             } else {
                 onDebug("The plugin's version is a dev-build. Will not try to reload the config.");
             }
         }
     }
 
     public YamlConfiguration getLang()
     {
         return LANG;
     }
 
     public File getLangFile()
     {
         return LANG_FILE;
     }
 
     public void onDebug(String s)
     { // Debug message method
         if (getConfig().getBoolean("debug")) {
             getLogger().log(Level.INFO, "[" + "Debug" + "]" + " {0}", s);
         }
     }
 
     public void forceUpdate(Player p, String sender0)
     {
         String up = Lang.UPDATING.toString().replaceAll("%prefix%", getDescription().getName());
         String updone = Lang.UPDATED.toString().replaceAll("%prefix%", getDescription().getName());
         p.sendMessage(sender0 + " " + up);
         Updater updater = new Updater(this, "simpleautomessage/", getFile(), Updater.UpdateType.NO_VERSION_CHECK, true);
         p.sendMessage(sender0 + " " + updone);
     }
 
     public void graphs(Metrics metrics)
     { // Custom Graphs. Sends data to mcstats.org
         try {
             //Graph1
             Metrics.Graph graph1 = metrics.createGraph("Messages"); //Sends data about how many msg strings the user has.
             int o = 0;
             for (int i = 1; getConfig().contains("msg" + i); i++) {
                 o = i;
             }
             graph1.addPlotter(new SimplePlotter("" + o));
 
             //graph2
             Metrics.Graph graph2 = metrics.createGraph("auto-update"); //Sends auto-update data. if auto-update: is true it returns 'enabled'.
             if (getConfig().getBoolean("auto-update") == true) {
                 graph2.addPlotter(new SimplePlotter("enabled"));
             } else {
                 graph2.addPlotter(new SimplePlotter("disabled"));
             }
 
             //Graph3
             Metrics.Graph graph3 = metrics.createGraph("language");
             if (getConfig().getString("language").equalsIgnoreCase("EN") || getConfig().getString("language").isEmpty()) {
                 graph3.addPlotter(new SimplePlotter("English"));
             }
             if (getConfig().getString("language").equalsIgnoreCase("FR")) {
                 graph3.addPlotter(new SimplePlotter("French"));
             }
             if (getConfig().getString("language").equalsIgnoreCase("NL")) {
                 graph3.addPlotter(new SimplePlotter("Dutch"));
             }
             if (getConfig().getString("language").equalsIgnoreCase("SE")) {
                 graph3.addPlotter(new SimplePlotter("Swedish"));
             }
             if (!getConfig().getString("language").equalsIgnoreCase("EN") && !getConfig().getString("language").equalsIgnoreCase("FR") && !getConfig().getString("language").equalsIgnoreCase("NL") && !getConfig().getString("language").equalsIgnoreCase("SE")) {
                 graph3.addPlotter(new SimplePlotter("Other"));
             }
             Metrics.Graph graph4 = metrics.createGraph("min-players");
             graph4.addPlotter(new SimplePlotter("" + getConfig().getInt("min-players")));
             onDebug("Sending metrics data...");
             metrics.start();
         } catch (Exception e) {
             Main.logger.warning(e.getMessage());
         }
     }
 
     public class SimplePlotter extends Metrics.Plotter {
 
         public SimplePlotter(final String name)
         {
             super(name);
         }
 
         @Override
         public int getValue()
         {
             return 1;
         }
     }
 }
