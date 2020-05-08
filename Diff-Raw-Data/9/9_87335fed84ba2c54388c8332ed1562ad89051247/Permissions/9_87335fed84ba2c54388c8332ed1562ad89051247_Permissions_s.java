 package to.joe.j2mc.core.permissions;
 
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.util.HashMap;
 import java.util.HashSet;
 
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerPreLoginEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.permissions.PermissionAttachment;
 
 import to.joe.j2mc.core.J2MC_Core;
 import to.joe.j2mc.core.J2MC_Manager;
 
 public class Permissions implements Listener {
 
     private final J2MC_Core plugin;
 
     /*
      * Flag documentation
      * 
      * p - teleport protection
      * t - trusted
      * 
      */
 
     /*
      * groups: default, admin, srstaff
      */
 
     /*
      * MySQL tables
      * users
      *  user_name
      *  group
      *  flags
      * groups
      *  name
      *  server_id
      *  flags
      * permissions
      *  permission
      *  flag
      * 
      */
 
     private final HashMap<Character, HashSet<String>> permissions;
     private final HashMap<String, PermissionAttachment> attachments;
     private final HashMap<String, HashSet<Character>> playerFlags;
     private final HashMap<String, HashSet<Character>> groupFlags;
     private final HashMap<String, String> playerGroup;
     private final HashSet<String> authenticated;
 
     public Permissions(J2MC_Core plugin) {
         this.plugin = plugin;
         this.permissions = new HashMap<Character, HashSet<String>>();
         this.authenticated = new HashSet<String>();
         this.attachments = new HashMap<String, PermissionAttachment>();
         this.playerFlags = new HashMap<String, HashSet<Character>>();
         this.groupFlags = new HashMap<String, HashSet<Character>>();
         this.playerGroup = new HashMap<String, String>();
         try {
             final PreparedStatement statement = J2MC_Manager.getMySQL().getFreshPreparedStatementHotFromTheOven("SELECT `name`,`flags` FROM `groups` WHERE `server_id`=?");
             statement.setInt(1, J2MC_Manager.getServerID());
             final ResultSet result = statement.executeQuery();
             while (result.next()) {
                 final String flagList = result.getString("flags");
                 final HashSet<Character> flags = new HashSet<Character>();
                 for (final char flag : flagList.toCharArray()) {
                     flags.add(flag);
                 }
                 this.groupFlags.put(result.getString("name"), flags);
             }
             if (!this.groupFlags.containsKey("default")) {
                 throw new Exception();
             }
         } catch (final Exception e) {
             plugin.buggerAll("Could not load SQL groups");
         }
         J2MC_Manager.getCore().getServer().getPluginManager().registerEvents(this, J2MC_Manager.getCore());
     }
 
     /**
      * Temporarily add a flag to a player
      * 
      * @param player
      * @param flag
      */
     public void addFlag(Player player, char flag) {
         this.playerFlags.get(player.getName()).add(flag);
         this.refreshPermissions(player);
     }
 
     /**
      * Remove a flag from a player, temporarily
      * 
      * @param player
      * @param flag
      */
     public void delFlag(Player player, char flag) {
         this.playerFlags.get(player.getName()).remove(flag);
         this.refreshPermissions(player);
     }
 
     /**
      * Check if player has flag
      * 
      * @param player
      *            - Player to check
      * @param flag
      *            - Flag to check
      * 
      * @return Returns true if player has flag, returns false if doesn't.
      */
     public boolean hasFlag(String player, char flag) {
         if (this.playerFlags.get(player).contains(flag)) {
             return true;
         } else {
             return false;
         }
     }
 
     public boolean isAdmin(String name) {
         final String group = this.playerGroup.get(name);
         return group.equals("admin") || group.equals("srstaff");
     }
 
     /**
      * Is the admin authed?
      * 
      * @param player
      * @return
      */
     public boolean isAuthenticated(Player player) {
         return this.authenticated.contains(player.getName());
     }
 
     /**
      * Called before a player joins the game.
      * Do not call this
      * 
      * @param player
      */
     @EventHandler(priority = EventPriority.LOWEST)
     public void playerPreLogin(PlayerPreLoginEvent event){
         final String player = event.getName();
         final HashSet<Character> flags = new HashSet<Character>();
         String group;
         try {
             final PreparedStatement userInfo = J2MC_Manager.getMySQL().getFreshPreparedStatementHotFromTheOven("SELECT `group`,`flags` FROM `users` WHERE `name`=?");
             userInfo.setString(1, player);
             final ResultSet result = userInfo.executeQuery();
             if (result.next()) {
                 group = result.getString("group");
                 final String flagList = result.getString("flags");
                 if (flagList != null) {
                     for (final char flag : flagList.toCharArray()) {
                         flags.add(flag);
                     }
                 }
             } else {
                final PreparedStatement newPlayer = J2MC_Manager.getMySQL().getFreshPreparedStatementHotFromTheOven("INSERT INTO `users` (`name`) VALUES (?)");
                 newPlayer.setString(1, player);
                newPlayer.execute();
                 group = "default";
             }
         } catch (final Exception e) {
             group = "default";
         }
 
         this.playerGroup.put(player, group);
         this.playerFlags.put(player, flags);
     }
     
     /**
      * Called when a player joins the game.
      * Do not call this
      * 
      * @param player
      */
     @EventHandler(priority = EventPriority.LOWEST)
     public void playerJoin(PlayerJoinEvent event) {
         this.refreshPermissions(event.getPlayer());
     }
 
     /**
      * Called when the player quits
      * Do not call this.
      * 
      * @param player
      */
     @EventHandler(priority = EventPriority.HIGHEST)
     public void playerQuit(PlayerQuitEvent event) {
         final Player player = event.getPlayer();
         this.attachments.remove(player.getName());
         this.playerFlags.remove(player.getName());
     }
 
     /**
      * Player has authenticated
      * 
      * @param player
      * @param authenticated
      */
     public void setAuthenticated(Player player, boolean authenticated) {
         if (authenticated) {
             this.authenticated.add(player.getName());
         } else {
             this.authenticated.remove(player.getName());
         }
         this.refreshPermissions(player);
     }
 
     /**
      * GOOD GOD MAN, call this before replacing this class or shutting down
      */
     public void shutdown() {
         for (final String playerName : this.attachments.keySet()) {
             final Player player = this.plugin.getServer().getPlayer(playerName);
             if (player != null) {
                 player.removeAttachment(this.attachments.get(playerName));
             }
         }
         this.plugin.getLogger().info("Unloaded all permissions");
     }
 
     private void refreshPermissions(Player player) {
         final String name = player.getName();
         if (this.attachments.containsKey(name)) {
             player.removeAttachment(this.attachments.remove(name));
         }
         final PermissionAttachment attachment = player.addAttachment(this.plugin);
 
         final HashSet<Character> flags = new HashSet<Character>();
         flags.addAll(this.playerFlags.get(name));
         String group = this.playerGroup.get(name);
         if ((group.equals("admin") || group.equals("srstaff")) && this.isAuthenticated(player)) {
             group = "default";
         }
         flags.addAll(this.groupFlags.get(group));
         final HashSet<Character> completed = new HashSet<Character>();
         for (final Character flag : flags) {
             if (completed.contains(flag)) {
                 continue;
             }
             completed.add(flag);
             if (this.permissions.containsKey(flag)) {
                 for (final String permission : this.permissions.get(flag)) {
                     attachment.setPermission(permission, true);
                 }
             }
         }
         this.attachments.put(name, attachment);
     }
 
 }
