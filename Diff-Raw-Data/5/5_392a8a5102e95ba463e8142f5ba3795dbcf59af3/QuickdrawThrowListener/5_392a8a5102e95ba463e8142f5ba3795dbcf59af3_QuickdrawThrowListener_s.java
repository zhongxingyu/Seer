 package me.eccentric_nz.plugins.quickdraw;
 
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import net.milkbowl.vault.economy.EconomyResponse;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Snowball;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.ProjectileLaunchEvent;
 import org.bukkit.inventory.Inventory;
 
 public class QuickdrawThrowListener implements Listener {
 
     private Quickdraw plugin;
     QuickdrawDatabase service = QuickdrawDatabase.getInstance();
 
     public QuickdrawThrowListener(Quickdraw plugin) {
         this.plugin = plugin;
     }
 
     @EventHandler
     public void onProjectileThrownEvent(ProjectileLaunchEvent event) {
         if (event.getEntity() instanceof Snowball) {
             //Snowball was thrown
             Snowball snowball = (Snowball) event.getEntity();
             LivingEntity shooter = snowball.getShooter();
             if (shooter instanceof Player) {
                 final Player p = (Player) shooter;
                 final String pNameStr = p.getName();
                 if (plugin.challengers.containsKey(pNameStr) || plugin.accepted.containsKey(pNameStr)) {
                     // a quick draw challenge has started
                     plugin.debug("A quick draw challenge has started...");
                     // if after (default) 10 seconds a hit has not been made then restore inventory and remove player from HashMap
                     long unfreeze = plugin.getConfig().getLong("unfreeze_after_miss") * 20;
                     plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                         @Override
                         public void run() {
                             plugin.debug("Closing quick draw challenge...");
                             if (plugin.challengers.containsKey(pNameStr) || plugin.accepted.containsKey(pNameStr)) {
                                 try {
                                     Connection connection = service.getConnection();
                                     Statement statement = connection.createStatement();
                                     ResultSet rsHitInv = statement.executeQuery("SELECT * FROM inventories WHERE player = '" + pNameStr + "'");
                                     if (rsHitInv.next()) {
                                         String base64 = rsHitInv.getString("inventory");
                                         Inventory i = QuickdrawInventory.fromBase64(base64);
                                         p.getInventory().setContents(i.getContents());
                                         String queryDelete = "DELETE FROM inventories WHERE player = '" + pNameStr + "'";
                                         statement.executeUpdate(queryDelete);
                                     }
                                     statement.close();
                                 } catch (SQLException e) {
                                     plugin.debug("Could not get players inventory");
                                 }
                             }
                             String accepted = "", challenger = "";
                             if (plugin.challengers.containsKey(pNameStr)) {
                                 accepted = plugin.challengers.get(pNameStr);
                                 challenger = pNameStr;
                                 plugin.debug("The challenger missed...");
                                 plugin.challengers.remove(pNameStr);
                                 plugin.quicktime.put(pNameStr, Long.MAX_VALUE);
                             }
                             if (plugin.accepted.containsKey(pNameStr)) {
                                 accepted = pNameStr;
                                 challenger = plugin.accepted.get(pNameStr);
                                 plugin.debug("The invited missed...");
                                 plugin.accepted.remove(pNameStr);
                                 plugin.quicktime.put(pNameStr, Long.MAX_VALUE);
                             }
                             if (plugin.quicktime.get(accepted) > plugin.quicktime.get(challenger)) {
                                 plugin.getServer().getPlayer(accepted).sendMessage(QuickdrawConstants.MY_PLUGIN_NAME + challenger + " won!");
                                 plugin.getServer().getPlayer(challenger).sendMessage(QuickdrawConstants.MY_PLUGIN_NAME + challenger + " won!");
                                 if (plugin.purse.containsKey(challenger) && plugin.getServer().getPluginManager().isPluginEnabled("Vault")) {
                                     double d = plugin.purse.get(challenger);
                                     EconomyResponse r = Quickdraw.economy.depositPlayer(challenger, d);
                                     if (r.transactionSuccess()) {
                                        plugin.getServer().getPlayer(challenger).sendMessage(String.format(QuickdrawConstants.MY_PLUGIN_NAME + "You won %s from the QuickDtaw challenge", Quickdraw.economy.format(r.amount)));
                                     } else {
                                         plugin.getServer().getPlayer(challenger).sendMessage(String.format("An error occured: %s", r.errorMessage));
                                     }
                                 }
                             }
                             if (plugin.quicktime.get(accepted) < plugin.quicktime.get(challenger)) {
                                 plugin.getServer().getPlayer(accepted).sendMessage(QuickdrawConstants.MY_PLUGIN_NAME + accepted + " won!");
                                 plugin.getServer().getPlayer(challenger).sendMessage(QuickdrawConstants.MY_PLUGIN_NAME + accepted + " won!");
                                 if (plugin.purse.containsKey(challenger) && plugin.getServer().getPluginManager().isPluginEnabled("Vault")) {
                                     double d = plugin.purse.get(challenger);
                                     EconomyResponse r = Quickdraw.economy.depositPlayer(accepted, d);
                                     if (r.transactionSuccess()) {
                                        plugin.getServer().getPlayer(accepted).sendMessage(String.format(QuickdrawConstants.MY_PLUGIN_NAME + "You won %s from the QuickDtaw challenge", Quickdraw.economy.format(r.amount)));
                                     } else {
                                         plugin.getServer().getPlayer(accepted).sendMessage(String.format("An error occured: %s", r.errorMessage));
                                     }
                                 }
                             }
                         }
                     }, unfreeze);
                 }
             }
         }
     }
 }
