 /*
     This file is part of Email.
 
     Email is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     Email is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with Email.  If not, see <http://www.gnu.org/licenses/>.
 */
 package com.mike724.email;
 
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.mcstats.MetricsLite;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.logging.Logger;
 
 public class Email extends JavaPlugin {
 
     public EmailManager emails;
     public EmailTransfer mailman;
     public EmailAlter alter;
 
     @Override
     public void onDisable() {
         this.getLogger().info("Disabled successfully");
     }
 
     @Override
     public void onEnable() {
         if (!this.getDataFolder().exists()) {
             this.getDataFolder().mkdir();
         }
         FileConfiguration config = this.getConfig();
         config.options().copyHeader(true);
         config.options().copyDefaults(true);
         this.saveConfig();
 
         loadConfig();
         Logger log = this.getLogger();
         //Enable plugin metrics
         try {
             MetricsLite metrics = new MetricsLite(this);
             metrics.start();
         } catch (IOException e) {
             e.printStackTrace();
         }
         this.getCommand("email").setExecutor(new EmailCommands(this));
         log.info("Enabled successfully");
     }
 
     public void loadConfig() {
        //Set it all to null
        this.emails = null;
        this.mailman = null;
        this.alter = null;

        this.reloadConfig();
         FileConfiguration config = this.getConfig();
         Logger log = this.getLogger();
         boolean enableEmailSending = config.getBoolean("email.enable");
         if (enableEmailSending) {
             String typeString = config.getString("email.type");
             List<Map<?, ?>> maps = config.getMapList("providers."+typeString);
             if(maps == null || maps.isEmpty()) {
                 log.severe("Unknown email provider! Disabling");
                 this.getServer().getPluginManager().disablePlugin(this);
                 return;
             }
             HashMap<String, String> props = new HashMap<String, String>();
             for(Map<?, ?> map : maps) {
                 //This part is a bit messy/hacky. Sorry. :)
                 //Nothing should go wrong if the key is a string
                 //The value should be either a string or int, but toString() will take care of that
                 @SuppressWarnings("unchecked")
                 String key = ((Set<String>)map.keySet()).iterator().next();
                 props.put(key, map.get(key).toString());
             }
             EmailProvider type = new EmailProvider(typeString, props);
             String user = config.getString("email.user");
             String pass = config.getString("email.password");
             if (user == null || pass == null) {
                 log.severe("Issue with email configuration section, please fill out everything.");
                 this.getServer().getPluginManager().disablePlugin(this);
                 return;
             }
             mailman = new EmailTransfer(this, type, user, pass);
         } else {
             mailman = null;
         }
         emails = new EmailManager(this);
         String between = config.getString("email.alter.in_between_pages");
         String sub = config.getString("email.alter.subject");
         String con = config.getString("email.alter.content");
         if(between == null || sub == null || con == null) {
             log.severe("Missing important config values in alter section.");
             this.getServer().getPluginManager().disablePlugin(this);
             return;
         }
         this.alter = new EmailAlter(between, sub, con);
         log.info("Configuration loaded");
     }
 }
