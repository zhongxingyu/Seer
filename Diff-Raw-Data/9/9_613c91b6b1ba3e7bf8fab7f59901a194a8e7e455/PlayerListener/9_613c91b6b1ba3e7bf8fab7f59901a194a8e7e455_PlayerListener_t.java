 package com.minecarts.bouncer.listener;
 
 import com.minecarts.bouncer.helper.LoginStatus;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerLoginEvent.Result;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.event.player.PlayerLoginEvent;
 import org.bukkit.event.player.PlayerKickEvent;
 import org.bukkit.entity.Player;
 import com.minecarts.bouncer.Bouncer;
 
 public class PlayerListener implements Listener {
     private Bouncer plugin;
     private java.util.HashMap<String, Integer> playerFlagged = new java.util.HashMap<String, Integer>();
     
     public PlayerListener(Bouncer plugin){
         this.plugin = plugin;
     }
 
 //Bans and whitelist support
     @EventHandler(priority = EventPriority.LOW)
     public void onPlayerLogin(PlayerLoginEvent e){
         //Development / op debug mode
         if(plugin.getConfig().getBoolean("locked",false)){
             if(!e.getPlayer().hasPermission("bouncer.admin")){
                 e.setKickMessage(ChatColor.GRAY + plugin.getConfig().getString("messages.ADMIN_LOCK"));
                 e.setResult(PlayerLoginEvent.Result.KICK_BANNED);
                 return;
             }
         }
 
         //Do our ban and whitelist queries (sync)
         plugin.doIdentifierCheck(e.getKickMessage(),e.getPlayer().getName());
 
         //Check if this hostname is a hostname which requires a whitelist
         for (String hostnameRegex : plugin.getConfig().getStringList("whitelist")){
            if(e.getHostname().toLowerCase().matches(hostnameRegex)){
                plugin.doHostnameWhitelistCheck(e.getKickMessage(), e.getPlayer().getName(), e.getHostname());
                break; //They have atleast one match, so let the query handle the rest
            }
         }
 
         //Check the status of the bans
         LoginStatus status = plugin.getLoginStatus(e.getPlayer().getName());
         if(status.isBanned){
             e.setResult(PlayerLoginEvent.Result.KICK_BANNED);
            e.setKickMessage(plugin.getConfig().getString("messages.BAN"));
             return;
         }
 
         //Check the status of the whitelist, if whitelisting is enabled
         if(status.whitelistStatus != null){
             switch(status.whitelistStatus){
                 case NOT_ON_LIST:
                     e.setKickMessage(Bouncer.whitelistMissing);
                     e.setResult(PlayerLoginEvent.Result.KICK_BANNED);
                     plugin.clearStatus(e.getPlayer().getName());
                     return;
                 case EXPIRED:
                     e.setKickMessage(Bouncer.whitelistExpired);
                     e.setResult(PlayerLoginEvent.Result.KICK_BANNED);
                     plugin.clearStatus(e.getPlayer().getName());
                     return;
             }
         }
 
         //Check if the server is full if a sub is connecting
         if(e.getResult() == Result.KICK_FULL){
             if(e.getPlayer().hasPermission("subscriber")){
                 Player[] online = Bukkit.getServer().getOnlinePlayers();
                 online[online.length - 1].kickPlayer(Bouncer.fullMessage); //Kick the most recent connecting player
                 e.setResult(Result.ALLOWED); //And let the subscriber connect
                 return;
             }
             e.setKickMessage(Bouncer.fullMessage);
         }
     }
 //Login messages
     @EventHandler(priority = EventPriority.LOW)
     public void onPlayerJoin(final PlayerJoinEvent e){
         e.setJoinMessage(null);
         //Do the login message 1 second later to allow for permissions to be set
         //  on this player
         Bukkit.getScheduler().scheduleSyncDelayedTask(plugin,new Runnable() {
             public void run() {
                 plugin.doLoginMessage(e.getPlayer());
             }
         },10); //This might need to be upped if TPS drops below 10
 
         //TODO Compare locations and teleport them if they're not matching
         plugin.fetchLocation(e.getPlayer());
 
     }
     @EventHandler(priority = EventPriority.LOW)
     public void onPlayerQuit(PlayerQuitEvent e){
       //Always clear the message, because we send it to all players ourselves for ignore list support
         e.setQuitMessage(null);
         plugin.doLogoutMessage(e.getPlayer());
 
       //Store this players logout location in player_mega
         plugin.storeLocation(e.getPlayer());
     }
     @EventHandler(priority = EventPriority.LOW)
     public void onPlayerKick(PlayerKickEvent e){
         e.setLeaveMessage(null);  //Clear any kick or timeout messages
     }
 }
