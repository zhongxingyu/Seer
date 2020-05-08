 package com.jamesst20.jcommandessentials.Objects;
 
 import com.jamesst20.config.JYamlConfiguration;
 import com.jamesst20.jcommandessentials.JCMDEssentials.JCMDEss;
 import java.io.File;
 
 import org.bukkit.OfflinePlayer;
 
 public class JOfflinePlayerConfig {
 
     OfflinePlayer player;
     JYamlConfiguration playerConfig;
 
     public JOfflinePlayerConfig(OfflinePlayer p) {
         player = p;
         File playersDir = new File(JCMDEss.plugin.getDataFolder(), "players");
         // We do not make file if the player isn't found. We leave stuff to null
         // We also make sure it is case insensitive
         if (playersDir.exists()) {
             for (File file : playersDir.listFiles()) {
                 if (file != null) {
                    if (file.getName().equalsIgnoreCase(player.getName())) {
                         playerConfig = new JYamlConfiguration(JCMDEss.plugin, "players/" + file.getName());
                     }
                 }
             }
         }
     }
 
     public void setBanReason(String reason) {
         if (playerConfig != null) {
             if (reason.isEmpty()) {
                 playerConfig.set("ban.reason", "You are banned.");
                 playerConfig.saveConfig();
             } else {
                 playerConfig.set("ban.reason", "Banned: " + reason);
                 playerConfig.saveConfig();
             }
         }
     }
 
     public String getBanReason() {
         if (playerConfig != null) {
             return playerConfig.getConfig().getString("ban.reason", "You are banned.");
         } else {
             return null;
         }
     }
 
     public void removeBanReason() {
         if (playerConfig != null) {
             playerConfig.set("ban", null);
             playerConfig.saveConfig();
         }
     }
 
     public String getJoinDate() {
         if (playerConfig != null) {
             return playerConfig.getConfig().getString("timestamps.joindate");
         }
         return null;
     }
 
     public String getLastLogin() {
         if (playerConfig != null) {
             return playerConfig.getConfig().getString("timestamps.login");
         }
         return null;
     }
 
     public String getLastLogout() {
         if (playerConfig != null) {
             return playerConfig.getConfig().getString("timestamps.logout");
         }
         return null;
     }
 
     public String getLastIP() {
         if (playerConfig != null) {
             return playerConfig.getConfig().getString("ipAddress");
         }
         return null;
     }
 
     public String getName() {
         return player.getName();
     }
 
     public boolean isBanned() {
         return player.isBanned();
     }

    public boolean playerExist() {
        return playerConfig != null ? true : false;
    }
 }
