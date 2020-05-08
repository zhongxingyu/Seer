 package com.md_5.welog;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Calendar;
 import java.text.SimpleDateFormat;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event.Type;
 import org.bukkit.event.player.PlayerCommandPreprocessEvent;
 import org.bukkit.event.player.PlayerListener;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class WELog extends JavaPlugin {
 
     public static final Logger logger = Bukkit.getServer().getLogger();
     public String path;
     public boolean console;

     public static final String DATE_FORMAT = "MM/dd/yyyy";
     public static final String TIME_FORMAT = "HH:mm:ss";
 
     public static String currDate() {
         Calendar cal = Calendar.getInstance();
         SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
         return sdf.format(cal.getTime());
     }
    
     public static String currTime() {
         Calendar cal = Calendar.getInstance();
         SimpleDateFormat sdf = new SimpleDateFormat(TIME_FORMAT);
         return sdf.format(cal.getTime());
     }
 
     public void onEnable() {
         FileConfiguration conf = getConfig();
         conf.options().copyDefaults(true);
         path = conf.getString("path");
         console = conf.getBoolean("console");
        filetype = conf.getString("type");
         saveConfig();
         getServer().getPluginManager().registerEvent(Type.PLAYER_COMMAND_PREPROCESS, new PlayerListener() {
 
             @Override
             public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
                 if (event.getMessage().startsWith("//")) {
                     File file = new File(path);
                     file.getParentFile().mkdirs();
                     try {
                         file.createNewFile();
                     } catch (IOException ex) {
                         Logger.getLogger(WELog.class.getName()).log(Level.SEVERE, null, ex);
                     }
                     Location l = event.getPlayer().getLocation();
                    
                    /*
                      Log as .csv, .txt, or .log
                    */
                    if(filetype.equals("csv")){
                    String output = event.getMessage() + "," + event.getPlayer().getName()
                            + "," + l.getWorld().getName() + "," + l.getBlockX() + "," + l.getBlockX() + "," + l.getBlockZ() + "\n";
                    }else if(filetype.equals("txt")){
                    String output = "["+currDate()+"]["+currTime()+"] User "+event.getPlayer().getName()+" used command "+event.getMessage()+" at location (X:"+l.getBlockX()+" Y:"+l.getBlockY()+" Z:"+l.getBlockZ()+") in world "+l.getWorld().getName()+"\n";
                    }else if(filetype.equals("log")){
                    String output = "["+currDate()+"]["+currTime()+"] [INFO] User "+event.getPlayer().getName()+" used command "+event.getMessage()+" at location (X:"+l.getBlockX()+" Y:"+l.getBlockY()+" Z:"+l.getBlockZ()+") in world "+l.getWorld().getName()+"\n";    
                     }
                    
                    Worldedit Displays Commands
                     if (console) {
                      getServer().getLogger().info("[WELog] User "+event.getPlayer().getName()+" used command "+event.getMessage()+" at location (X:"+l.getBlockX()+" Y:"+l.getBlockY()+" Z:"+l.getBlockZ()+") in world "+l.getWorld().getName());
                     }
                     try {
                         BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
                         writer.write(output);
                         writer.close();
                     } catch (IOException ex) {
                         Logger.getLogger(WELog.class.getName()).log(Level.SEVERE, null, ex);
                     }
                 }
             }
         }, Priority.Lowest, this);
         logger.info(String.format("[WELog - v%1$s] by md_5 enabled", this.getDescription().getVersion()));
     }
 
     public void onDisable() {
         logger.info(String.format("[WELog - v%1$s] by md_5 disabled", this.getDescription().getVersion()));
     }
 }
