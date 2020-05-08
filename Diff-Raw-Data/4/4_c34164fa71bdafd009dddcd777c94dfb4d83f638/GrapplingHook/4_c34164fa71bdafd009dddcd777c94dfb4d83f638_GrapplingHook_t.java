 package de.craftlancer.wayofshadows.skills;
 
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.craftbukkit.v1_7_R1.entity.CraftArrow;
 import org.bukkit.entity.Arrow;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.ProjectileHitEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.metadata.FixedMetadataValue;
 import org.bukkit.scheduler.BukkitRunnable;
 
 import de.craftlancer.wayofshadows.WayOfShadows;
 import de.craftlancer.wayofshadows.event.ShadowPullEvent;
 import de.craftlancer.wayofshadows.utils.ValueWrapper;
 
 /**
  * Represents a configuration of the GrapplingHook skill
  */
 public class GrapplingHook extends Skill
 {
     private ValueWrapper blockTime;
     private ValueWrapper maxDistance;
     private ValueWrapper distanceToInitial;
     private ValueWrapper itemsPerBlock;
     private boolean pickupArrow;
     
     private List<String> pullLore;
     private List<Material> pullItems = new LinkedList<Material>();
     private List<String> pullItemNames;
     
     private String distanceMsg;
     private String initialMsg;
     private String errorMsg;
     
     public GrapplingHook(WayOfShadows instance, String key)
     {
         super(instance, key);
         FileConfiguration config = instance.getConfig();
         
         pullLore = config.getStringList(key + ".pullLore");
         pullItemNames = config.getStringList(key + ".pullItemNames");
         
         for (String s : instance.getConfig().getStringList(key + ".pullItems"))
         {
             Material mat = Material.matchMaterial(s);
             
             if (mat == null)
                 instance.getLogger().warning("A pullitem in " + key + " is not a valid Material.");
             else
                 pullItems.add(mat);
         }
         
         distanceMsg = config.getString(key + ".distanceMsg");
         initialMsg = config.getString(key + ".initialMsg");
         errorMsg = config.getString(key + ".errorMsg");
         
         blockTime = new ValueWrapper(config.getString(key + ".blockTime", "0"));
         maxDistance = new ValueWrapper(config.getString(key + ".maxDistance", "0"));
         distanceToInitial = new ValueWrapper(config.getString(key + ".distanceToInitial", "0"));
         itemsPerBlock = new ValueWrapper(config.getString(key + ".itemsPerBlock", "0"));
         pickupArrow = config.getBoolean(key + ".canPickupArrow", false);
     }
     
     /**
      * Constructor for pre 0.5 Updater
      */
     @Deprecated
     public GrapplingHook(WayOfShadows instance, String key, int hook, int pull, long bTime, int maxDist, int initDistance, double ipb, String string, String string2, String string3)
     {
         super(instance, key, String.valueOf(hook));
         
         pullLore = new ArrayList<String>();
         pullItemNames = new ArrayList<String>();
         pullItems.add(Material.getMaterial(pull));
         
         distanceMsg = string3;
         initialMsg = string2;
         errorMsg = string;
         
         blockTime = new ValueWrapper(bTime);
         maxDistance = new ValueWrapper(maxDist);
         distanceToInitial = new ValueWrapper(initDistance);
         itemsPerBlock = new ValueWrapper(ipb);
     }
     
     @EventHandler(priority = EventPriority.HIGHEST)
     public void onHookShoot(PlayerInteractEvent event)
     {
         if (!event.hasItem() || !(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK))
             return;
         
         Player p = event.getPlayer();
         
         if (isSkillItem(event.getItem()) && hasPermission(p, event.getItem()))
         {
             if (isOnCooldown(p))
             {
                 p.sendMessage(getCooldownMsg(p));
                 return;
             }
             
             Arrow arrow = p.launchProjectile(Arrow.class);
             ItemStack item = event.getItem().clone();
             item.setAmount(1);
             
             arrow.setMetadata(getName() + ".playerLocation", new FixedMetadataValue(plugin, p.getLocation()));
             p.setMetadata(getName() + ".hookArrow", new FixedMetadataValue(plugin, arrow));
             
             p.getInventory().removeItem(new ItemStack(item));
         }
     }
     
     @EventHandler(priority = EventPriority.HIGH)
     public void onPull(PlayerInteractEvent e)
     {
         if (!e.hasItem() || !isPullItem(e.getItem()) || !(e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_AIR))
             return;
         
         Player p = e.getPlayer();
         
         if (!p.hasMetadata(getName() + ".hookArrow") || p.hasMetadata(getName() + ".teleportArrow") || !((Arrow) p.getMetadata(getName() + ".hookArrow").get(0).value()).hasMetadata(getName() + ".isHit"))
             return;
         
         Arrow arrow = (Arrow) p.getMetadata(getName() + ".hookArrow").get(0).value();
         Location initLoc = (Location) arrow.getMetadata(getName() + ".playerLocation").get(0).value();
         
         ShadowPullEvent event = new ShadowPullEvent(p, this, arrow);
         Bukkit.getServer().getPluginManager().callEvent(event);
         
         if (event.isCancelled())
             return;
         
         e.setCancelled(true);
         
         Location ploc = p.getEyeLocation();
         Location aloc = arrow.getLocation();
        
        if (!aloc.getWorld().equals(ploc.getWorld()))
            return;
        
         double distance1 = ploc.distance(aloc);
         int level = plugin.getLevel(p, getLevelSys());
         int amount = (int) Math.ceil(distance1 * itemsPerBlock.getValue(level));
         ItemStack item = e.getItem().clone();
         item.setAmount(amount);
         
         if (distance1 > maxDistance.getIntValue(level))
         {
             p.sendMessage(distanceMsg);
             return;
         }
         
         if (ploc.distance(initLoc) > distanceToInitial.getIntValue(level))
         {
             p.sendMessage(initialMsg);
             return;
         }
         
         p.teleport(initLoc);
         Arrow ball = p.launchProjectile(Arrow.class);
         if (!pickupArrow)
             ((CraftArrow) ball).getHandle().fromPlayer = 2; // NMS
         ball.setMetadata(getName() + ".teleportArrow", new FixedMetadataValue(plugin, true));
         
         p.setMetadata(getName() + ".teleportArrow", new FixedMetadataValue(plugin, ball.getEntityId()));
         
         p.getInventory().removeItem(new ItemStack(item));
     }
     
     @EventHandler(priority = EventPriority.HIGHEST)
     public void onHookHit(ProjectileHitEvent e)
     {
         if (!e.getEntity().getType().equals(EntityType.ARROW) || !(e.getEntity().getShooter() instanceof Player))
             return;
         
         Player p = (Player) e.getEntity().getShooter();
         Arrow arrow = (Arrow) e.getEntity();
         
         if (arrow.hasMetadata(getName() + ".playerLocation"))
         {
             arrow.setMetadata(getName() + ".isHit", new FixedMetadataValue(plugin, true));
             return;
         }
         
         if (p.hasMetadata(getName() + ".teleportArrow") && (arrow.getEntityId() == p.getMetadata(getName() + ".teleportArrow").get(0).asInt()))
         {
             Location loc = arrow.getLocation();
             arrow.remove();
             
             if ((loc.getBlock().getType().isSolid() || loc.getBlock().getRelative(0, 1, 0).getType().isSolid() || loc.getBlock().getRelative(0, 2, 0).getType().isSolid()))
             {
                 p.sendMessage(errorMsg);
                 p.removeMetadata(getName() + ".teleportArrow", plugin);
                 p.removeMetadata(getName() + ".hookArrow", plugin);
                 return;
             }
             
             p.teleport(loc.add(0, 0.75, 0));
             
             int level = plugin.getLevel(p, getLevelSys());
             final Block block = p.getLocation().getBlock().getRelative(0, -1, 0);
             
             if (!block.getType().isSolid())
             {
                 final Material type = block.getType();
                 block.setType(Material.GLASS);
                 
                 new BukkitRunnable()
                 {
                     @Override
                     public void run()
                     {
                         block.setType(type);
                     }
                 }.runTaskLater(plugin, blockTime.getIntValue(level) > 0 ? blockTime.getIntValue(level) : 0);
             }
             
             p.setFallDistance(0);
             p.removeMetadata(getName() + ".teleportArrow", plugin);
             p.removeMetadata(getName() + ".hookArrow", plugin);
             setOnCooldown(p);
         }
     }
     
     @EventHandler(priority = EventPriority.LOW)
     public void onDamage(EntityDamageByEntityEvent e)
     {
         if (!e.getDamager().getType().equals(EntityType.ARROW))
             return;
         
         Arrow arrow = (Arrow) e.getDamager();
         
         if (arrow.hasMetadata(getName() + ".teleportArrow") || arrow.hasMetadata(getName() + ".playerLocation"))
             e.setCancelled(true);
     }
     
     private boolean isPullItem(ItemStack item)
     {
         if (pullItems.contains(item.getType()))
             return true;
         
         if (item.hasItemMeta())
             if (item.getItemMeta().hasDisplayName() && pullItemNames.contains(item.getItemMeta().getDisplayName()))
                 return true;
             else if (item.getItemMeta().hasLore())
                 for (String str : pullLore)
                     for (String str2 : item.getItemMeta().getLore())
                         if (str2.contains(str))
                             return true;
         
         return false;
     }
     
     @Override
     public void save(FileConfiguration config)
     {
         super.save(config);
         List<String> mat = new LinkedList<String>();
         for (Material m : pullItems)
             mat.add(m.name());
         
         config.set(getName() + ".type", "grapplinghook");
         config.set(getName() + ".pullLore", pullLore);
         config.set(getName() + ".pullItems", mat);
         config.set(getName() + ".pullItemNames", pullItemNames);
         
         config.set(getName() + ".distanceMsg", distanceMsg);
         config.set(getName() + ".initialMsg", initialMsg);
         config.set(getName() + ".errorMsg", errorMsg);
         
         config.set(getName() + ".blockTime", blockTime.getInput());
         config.set(getName() + ".maxDistance", maxDistance.getInput());
         config.set(getName() + ".distanceToInitial", distanceToInitial.getInput());
         config.set(getName() + ".itemsPerBlock", itemsPerBlock.getInput());
         config.set(getName() + ".canPickupArrow", pickupArrow);
     }
     
     @Override
     public String getType()
     {
         return "grapplinghook";
     }
 }
