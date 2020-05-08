 package com.gmail.zariust.LightVote;
 
 
 import java.io.IOException;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.gmail.zariust.LightVote.config.ConfigManager;
 import com.gmail.zariust.LightVote.metrics.Metrics;
 
 import fr.crafter.tickleman.RealPlugin.RealTranslationFile;
 
 
 /**
  * LightVote for Bukkit
  *
  * @author XUPWUP
  */
 public class LightVote extends JavaPlugin {
     public static Metrics metrics = null;
     public static RealTranslationFile translate;
     private LVTPlayerListener playerListener;
    public Log log = new Log();
     public static LVTConfig config;
     public ConfigManager configManager;
 
     @Override
     public void onEnable() { 
         loadConfig();
         loadLanguageFile();
         registerListeners();
         setPermanentTimeIfConfigured();
         enableMetricsIfConfigured();
     }
 
     @Override
     public void onDisable() {
         if (PermanentTime.timer != null)
             PermanentTime.timer.cancel();
     }
 
     public void registerListeners() {
         playerListener = new LVTPlayerListener(this);
         // Register event for beds
         PluginManager pm = this.getServer().getPluginManager();
         pm.registerEvents(playerListener, this);
     }
 
     public void loadConfig() {
         ConfigManager.load(this);
     }
 
     public void loadLanguageFile() {
         LightVote.translate = new RealTranslationFile(this, config.language).load();
         log.sM(this, LightVote.translate.tr("Language is " + config.language));
     }
 
     public void setPermanentTimeIfConfigured() {
         if (config.perma)
             PermanentTime.setReset();
     }
 
     public void enableMetricsIfConfigured() {
         if (config.enableMetrics) {
             try {
                 metrics = new Metrics(this);
                 metrics.start();
             } catch (IOException e) {
                 // Failed to submit the stats :-(
             }
         }
     }
 
     @Override
     public boolean onCommand(CommandSender sender, Command command,
             String label, String[] args) {
 
         return playerListener.onPlayerCommand(sender, command, label, args);
     }
 }
