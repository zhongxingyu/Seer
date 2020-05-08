 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package at.rcraft.minecoupon;
 
 import java.io.BufferedInputStream;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerChatEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginDescriptionFile;
 
 /**
  *
  * @author Thomas Raith
  */
class MineCouponListener implements Listener {
 
     Plugin plugin;
     
     public MineCouponListener(MineCoupon p) {
         plugin = p;
     }
     
     @EventHandler
     public void checkUpdate(PlayerJoinEvent event){
         if(permCheck(event.getPlayer(), "minecoupon.checkupdate")){   
                 checkVersion(event.getPlayer());
         }
     }
 
     private void checkVersion(Player player){
         
         player.sendMessage(ChatColor.GREEN+"[MineCoupon] " + plugin.getConfig().getString("config.update.message.check"));
         System.out.println("[MineCoupon] Check for updates ...");   
         
         PluginDescriptionFile descFile = plugin.getDescription();
         URL url = null;
         BufferedInputStream bufferedInput = null;
         byte[] buffer = new byte[1024];
         try {
         url = new URL("http://rcraft.at/plugins/minecoupon/VERSION");
         } catch (MalformedURLException ex) {
             System.out.println("[MineCoupon] Check for updates failed.");
             player.sendMessage(ChatColor.RED+"[MineCoupon] " + plugin.getConfig().getString("config.update.message.error"));
         }
         try 
         {
             bufferedInput = new BufferedInputStream(url.openStream());
             int bytesRead = 0;
              while ((bytesRead = bufferedInput.read(buffer)) != -1) {
                 
                 String version= new String(buffer, 0, bytesRead);
                 if (Float.valueOf(version) > Float.valueOf(descFile.getVersion()))
                 {
                     player.sendMessage(ChatColor.GOLD+"[MineCoupon] " + plugin.getConfig().getString("config.update.message.newupdate"));
                     System.out.println("[MineCoupon] A newer Version is available.");
                 }
                 else{
                     if (version.equals(descFile.getVersion())){
                         player.sendMessage(ChatColor.GREEN+"[MineCoupon] " + plugin.getConfig().getString("config.update.message.noupdate"));
                         System.out.println("[MineCoupon] Measurement Tools is up to date.");
                     }
                     else{
                         player.sendMessage(ChatColor.RED+"[MineCoupon] " + plugin.getConfig().getString("config.update.message.developementbuild"));
                         System.out.println("[MineCoupon] You are using a developementbuild."); 
                     }
                 }
              }
                 bufferedInput.close();
             
         } 
         catch (IOException ex) 
         {
             System.out.println("[MineCoupon] Check for updates failed!");
             player.sendMessage(ChatColor.RED+"[MineCoupon] " + plugin.getConfig().getString("config.update.message.error"));
         }
     }
     
     
     private boolean permCheck(Player player, String permission){
         if(player.isOp() || player.hasPermission(permission)){
             return true;
         }
         return false;
     }
     
 }
