 package to.joe.j2mc.core;
 
 import java.util.logging.Level;
 
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.kitteh.vanish.staticaccess.VanishNoPacket;
 
 import to.joe.j2mc.core.MySQL.MySQL;
 import to.joe.j2mc.core.log.CommandLogger;
 import to.joe.j2mc.core.permissions.Permissions;
 import to.joe.j2mc.core.visibility.Visibility;
 
 public class J2MC_Core extends JavaPlugin {
 
     /**
      * Combine string array with specified separator
      * 
      * @param startIndex
      * @param string
      * @param seperator
      * @return
      */
     public static String combineSplit(int startIndex, String[] string, String seperator) {
         final StringBuilder builder = new StringBuilder();
         for (int i = startIndex; i < string.length; i++) {
             builder.append(string[i]);
             builder.append(seperator);
         }
         builder.deleteCharAt(builder.length() - seperator.length());
         return builder.toString();
     }
 
     /**
      * Send message to those with j2mc.message.receive.admin
      * and to the server log at INFO level
      * 
      * @param message
      */
     public void adminAndLog(String message) {
        this.getServer().broadcast(message, "j2mc.message.receive.admin");
         this.getLogger().info(message);
     }
 
     /**
      * OMG SHUT THIS DOWN
      * Do not use this from outside core.
      * 
      * @param reason
      */
     public void buggerAll(String reason) {
         this.buggerAll(reason, null);
     }
 
     /**
      * OMG SHUT THIS DOWN (with added exception print)
      * Do not use this from outside core.
      * 
      * @param reason
      * @param exception
      */
     public void buggerAll(String reason, Exception exception) {
         if (exception != null) {
             this.getLogger().log(Level.SEVERE, "Shutdown caused by: " + reason, exception);
         } else {
             this.getLogger().severe("Shutdown caused by: " + reason);
         }
         this.getServer().getPluginManager().disablePlugin(this);
     }
 
     /**
      * Send a message to users with specified permission
      * 
      * @param permission
      * @param message
      */
     public void messageByNoPermission(String message, String permission) {
         for (final Player player : this.getServer().getOnlinePlayers()) {
             if ((player != null) && !player.hasPermission(permission)) {
                 player.sendMessage(message);
             }
         }
     }
 
     public void messageNonAdmin(String message) {
         this.messageByNoPermission(message, "j2mc.message.receive.admin");
     }
 
     @Override
     public void onDisable() {
         J2MC_Manager.getPermissions().shutdown();
     }
 
     @Override
     public void onEnable() {
         J2MC_Manager.getInstance().setCore(this);
         final String mySQLUsername = this.getConfig().getString("MySQL.username");
         if (mySQLUsername == null) {
             this.buggerAll("Config is empty. I repeat, config is derp");
             return;
         }
         final String mySQLPassword = this.getConfig().getString("MySQL.password");
         final String mySQLDatabase = this.getConfig().getString("MySQL.database");
         J2MC_Manager.getInstance().setMySQL(new MySQL(mySQLDatabase, mySQLUsername, mySQLPassword));
 
         J2MC_Manager.getInstance().setServerID(this.getConfig().getInt("General.server-id"));
 
         J2MC_Manager.getInstance().setPermissions(new Permissions(this));
 
         J2MC_Manager.getInstance().setVisibility(new Visibility());
         this.getServer().getPluginManager().registerEvents(new CommandLogger(this), this);
         this.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
             @Override
             public void run() {
                 if (!J2MC_Core.this.getServer().getPluginManager().isPluginEnabled("VanishNoPacket")) {
                     J2MC_Core.this.buggerAll("VanishNoPacket required.");
                     return;
                 }
                 try {
                     VanishNoPacket.numVanished();
                 } catch (final Exception e) {
                     J2MC_Core.this.buggerAll("VanishNoPacket required.");
                 }
             }
 
         });
     }
 }
