 package me.freebuild.superspytx.sc.events;
 
 import me.freebuild.superspytx.sc.SecretWord;
 import me.freebuild.superspytx.sc.database.SecretPlayer;
 
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.inventory.*;
 import org.bukkit.event.player.*;
 
 public class PlayerEvents implements Listener
 {
 
     public SecretWord core = null;
 
     public PlayerEvents(SecretWord instance)
     {
         core = instance;
     }
 
     @EventHandler(priority = EventPriority.HIGHEST)
     public void e(PlayerMoveEvent e)
     {
         if (core.getDB().secplayers.containsKey(e.getPlayer().getName()))
         {
             // if all goes well.
             SecretPlayer player = core.getDB().secplayers.get(e.getPlayer().getName());
             player.setBukkitPlayer(e.getPlayer());
             if (!player.isLoggedIn())
             {
                 e.setTo(e.getFrom()); // this is so plugins recognize that it's "cancelled"
                 e.setCancelled(true); // backwards compatibility.
                 player.teleportBack(); // now teleport back!
             }
         }
         else
         {
             // otherwise something really went wrong. perhaps derp?
             //core.getLogger().log(Level.WARNING, "There's a serious problem, player " + e.getPlayer().getName() + " does not have a SecretPlayer record!");
             return;
         }
     }
 
     @EventHandler(priority = EventPriority.LOWEST)
     public void e(PlayerTeleportEvent e)
     {
         if (core.getDB().secplayers.containsKey(e.getPlayer().getName()))
         {
             // if all goes well.
             SecretPlayer player = core.getDB().secplayers.get(e.getPlayer().getName());
             player.setBukkitPlayer(e.getPlayer());
             if (!player.isLoggedIn())
             {
                player.setInitialLocation(e.getTo());
                return;
             }
         }
     }
 
     @EventHandler(priority = EventPriority.LOWEST)
     public void e(PlayerBedEnterEvent e)
     {
         if (core.getDB().secplayers.containsKey(e.getPlayer().getName()))
         {
             SecretPlayer player = core.getDB().secplayers.get(e.getPlayer().getName());
             if (!player.isLoggedIn())
             {
                 e.setCancelled(true);
             }
         }
     }
 
     @EventHandler(priority = EventPriority.LOWEST)
     public void e(PlayerBucketEmptyEvent e)
     {
         if (core.getDB().secplayers.containsKey(e.getPlayer().getName()))
         {
             SecretPlayer player = core.getDB().secplayers.get(e.getPlayer().getName());
             if (!player.isLoggedIn())
             {
                 e.setCancelled(true);
             }
         }
     }
 
     @EventHandler(priority = EventPriority.LOWEST)
     public void e(PlayerBucketFillEvent e)
     {
         if (core.getDB().secplayers.containsKey(e.getPlayer().getName()))
         {
             SecretPlayer player = core.getDB().secplayers.get(e.getPlayer().getName());
             if (!player.isLoggedIn())
             {
                 e.setCancelled(true);
             }
         }
     }
 
     @EventHandler(priority = EventPriority.LOWEST)
     public void e(PlayerCommandPreprocessEvent e)
     {
         if (core.getDB().secplayers.containsKey(e.getPlayer().getName()))
         {
             SecretPlayer player = core.getDB().secplayers.get(e.getPlayer().getName());
             if (!player.isLoggedIn())
             {
                 e.setCancelled(true);
             }
         }
     }
 
     @EventHandler(priority = EventPriority.LOWEST)
     public void e(PlayerDropItemEvent e)
     {
         if (core.getDB().secplayers.containsKey(e.getPlayer().getName()))
         {
             SecretPlayer player = core.getDB().secplayers.get(e.getPlayer().getName());
             if (!player.isLoggedIn())
             {
                 e.setCancelled(true);
             }
         }
     }
 
     @EventHandler(priority = EventPriority.HIGH)
     public void e(PlayerInteractEvent e)
     {
         if (core.getDB().secplayers.containsKey(e.getPlayer().getName()))
         {
             SecretPlayer player = core.getDB().secplayers.get(e.getPlayer().getName());
             if (!player.isLoggedIn())
             {
                 e.setCancelled(true);
             }
         }
     }
 
     @EventHandler(priority = EventPriority.LOWEST)
     public void e(PlayerFishEvent e)
     {
         if (core.getDB().secplayers.containsKey(e.getPlayer().getName()))
         {
             SecretPlayer player = core.getDB().secplayers.get(e.getPlayer().getName());
             if (!player.isLoggedIn())
             {
                 e.setCancelled(true);
             }
         }
     }
 
     @EventHandler(priority = EventPriority.LOWEST)
     public void e(PlayerGameModeChangeEvent e)
     {
         if (core.getDB().secplayers.containsKey(e.getPlayer().getName()))
         {
             SecretPlayer player = core.getDB().secplayers.get(e.getPlayer().getName());
             if (!player.isLoggedIn())
             {
                 e.setCancelled(true);
             }
         }
     }
 
     @EventHandler(priority = EventPriority.LOWEST)
     public void e(PlayerInteractEntityEvent e)
     {
         if (core.getDB().secplayers.containsKey(e.getPlayer().getName()))
         {
             SecretPlayer player = core.getDB().secplayers.get(e.getPlayer().getName());
             if (!player.isLoggedIn())
             {
                 e.setCancelled(true);
             }
         }
     }
 
     @EventHandler(priority = EventPriority.LOWEST)
     public void e(InventoryOpenEvent e)
     {
         if (core.getDB().secplayers.containsKey(e.getPlayer().getName()))
         {
             SecretPlayer player = core.getDB().secplayers.get(e.getPlayer().getName());
             if (!player.isLoggedIn())
             {
                 e.setCancelled(true);
             }
         }
     }
 
     @EventHandler(priority = EventPriority.LOWEST)
     public void e(PlayerPickupItemEvent e)
     {
         if (core.getDB().secplayers.containsKey(e.getPlayer().getName()))
         {
             SecretPlayer player = core.getDB().secplayers.get(e.getPlayer().getName());
             if (!player.isLoggedIn())
             {
                 e.setCancelled(true);
             }
         }
     }
 
     @EventHandler(priority = EventPriority.LOWEST)
     public void e(PlayerPortalEvent e)
     {
         if (core.getDB().secplayers.containsKey(e.getPlayer().getName()))
         {
             SecretPlayer player = core.getDB().secplayers.get(e.getPlayer().getName());
             if (!player.isLoggedIn())
             {
                 e.setCancelled(true);
             }
         }
     }
 
     @EventHandler(priority = EventPriority.LOWEST)
     public void e(PlayerShearEntityEvent e)
     {
         if (core.getDB().secplayers.containsKey(e.getPlayer().getName()))
         {
             SecretPlayer player = core.getDB().secplayers.get(e.getPlayer().getName());
             if (!player.isLoggedIn())
             {
                 e.setCancelled(true);
             }
         }
     }
 
     @EventHandler(priority = EventPriority.LOWEST)
     public void e(PlayerToggleSneakEvent e)
     {
         if (core.getDB().secplayers.containsKey(e.getPlayer().getName()))
         {
             SecretPlayer player = core.getDB().secplayers.get(e.getPlayer().getName());
             if (!player.isLoggedIn())
             {
                 e.setCancelled(true);
             }
         }
     }
 
     @EventHandler(priority = EventPriority.LOWEST)
     public void e(PlayerToggleSprintEvent e)
     {
         if (core.getDB().secplayers.containsKey(e.getPlayer().getName()))
         {
             SecretPlayer player = core.getDB().secplayers.get(e.getPlayer().getName());
             if (!player.isLoggedIn())
             {
                 e.setCancelled(true);
             }
         }
     }
 
     @EventHandler(priority = EventPriority.LOWEST)
     public void e(PlayerVelocityEvent e)
     {
         if (core.getDB().secplayers.containsKey(e.getPlayer().getName()))
         {
             SecretPlayer player = core.getDB().secplayers.get(e.getPlayer().getName());
             if (!player.isLoggedIn())
             {
                 e.setCancelled(true);
             }
         }
     }
 
 }
