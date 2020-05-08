 package com.syd.antiugfarm;
 
import java.io.File;
 import java.util.logging.Logger;
 
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class AntiUGFarm extends JavaPlugin
 {
     public static FileConfiguration config;
     public final ServerBlockListener BlockListener = new ServerBlockListener(this);
     public final static Logger log = Logger.getLogger("Minecraft");
 
     public void onEnable()
     {
         PluginDescriptionFile pdffile = this.getDescription();
 
         // initializing config.yml
         config = getConfig();
        if (!new File(this.getDataFolder().getPath() + File.separatorChar + "config.yml").exists())
            saveDefaultConfig();
 
         if (!config.getString("version").equalsIgnoreCase(pdffile.getVersion()))
         {
             config.set("version", pdffile.getVersion());
             saveConfig();
         }
         // end of config.yml
 
         getServer().getPluginManager().registerEvents(BlockListener, this);
 
         log.info("[AntiUGFarm] " + pdffile.getName() + " " + pdffile.getVersion() + " enabled");
     }
 
     public void onDisable()
     {
         PluginDescriptionFile pdfFile = this.getDescription();
         log.info("[AntiUGFarm] " + pdfFile.getName() + " disabled");
     }
 }
