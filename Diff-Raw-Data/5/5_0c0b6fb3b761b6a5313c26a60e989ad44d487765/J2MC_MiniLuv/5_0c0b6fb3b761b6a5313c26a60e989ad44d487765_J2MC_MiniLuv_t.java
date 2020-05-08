 package to.joe.j2mc.miniluv;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.SignChangeEvent;
 import org.bukkit.event.player.PlayerChatEvent;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import to.joe.j2mc.core.J2MC_Manager;
 import to.joe.j2mc.core.event.MessageEvent;
 
 public class J2MC_MiniLuv extends JavaPlugin implements Listener {
 
     @Override
     public void onEnable() {
         this.getServer().getPluginManager().registerEvents(this, this);
 
         this.getLogger().info("Ministry of love loaded and surveying");
     }
     
     public void sendAlert(String message) {
         this.getServer().getPluginManager().callEvent(new MessageEvent(MessageEvent.compile("ADMININFO"), message));
     }
 
     @EventHandler(priority = EventPriority.MONITOR)
     public void onChat(PlayerChatEvent event) {
         if (event.getMessage().toLowerCase().contains("fag") || event.getMessage().toLowerCase().contains("nigg")) {
             this.sendAlert("Watch " + event.getPlayer().getName() + " for language: " + event.getMessage());
         }
         if (event.getMessage().contains("_____##___##") || event.getMessage().contains("_-_-_-_-_-_-_-''    ''") || event.getMessage().contains("-_-_-_-_-_-_-_,------,") || event.getMessage().contains("##___#######")) {
             if (this.getServer().getPluginManager().isPluginEnabled("Bans")) {
                 final String toSend = "BobTheHAXXXXXXguy:" + event.getPlayer().getName() + ":spam hacks:ammar2";
                 this.getServer().getPluginManager().callEvent(new MessageEvent(MessageEvent.compile("NEWADDBAN"), toSend));
                 this.sendAlert("Banned " + event.getPlayer().getName() + " for using nyancraft");
             } else {
                 this.sendAlert("Check out " + event.getPlayer().getName() + ", he is probably using spam hacks (nyancraft)");
             }
         }
        if(event.getMessage().matches("^(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3}$")) {
             this.sendAlert(event.getPlayer().getName() + " just posted an ip in chat");
         }
     }
     
 
     @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
     public void onSignChange(SignChangeEvent event) {
         String bad = null;
         for (final String line : event.getLines()) {
            if (line.toLowerCase().contains("fag") || (line.toLowerCase().contains("nigg")) || line.matches("^(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3}$")) {
                 bad = line;
                 break;
             }
         }
         if (bad!=null) {
             final Location loc = event.getBlock().getLocation();
             final String msg = event.getPlayer().getName() + " created a bad sign @ X" + loc.getBlockX() + " Y" + loc.getBlockY() + " Z" + loc.getBlockZ() + "! Line: \"" + bad + "\"";
             this.sendAlert(msg);
             J2MC_Manager.getCore().adminAndLog(ChatColor.RED + msg);
         }
     }
 
 }
