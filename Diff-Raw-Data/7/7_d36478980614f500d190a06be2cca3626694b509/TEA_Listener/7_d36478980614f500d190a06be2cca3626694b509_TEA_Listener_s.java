 package com.github.ribesg.tea;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map.Entry;
 import java.util.Random;
 import java.util.SortedMap;
 import java.util.TreeMap;
 import java.util.UUID;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Chunk;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockState;
 import org.bukkit.entity.EnderDragon;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Projectile;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.CreatureSpawnEvent;
 import org.bukkit.event.entity.EntityCreatePortalEvent;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityDeathEvent;
 import org.bukkit.event.entity.EntityRegainHealthEvent;
 import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
 import org.bukkit.event.world.ChunkLoadEvent;
 import org.bukkit.inventory.ItemStack;
 
 import com.github.ribesg.tea.util.EndWorldConfig;
 import com.github.ribesg.tea.util.ExtendedChunk;
 
 
 public class TEA_Listener implements Listener {
 
     // Player with less than that damages done to dead ED can't receive the DragonEgg
     private final static float threshold = 0.15f;
 
     private final TheEndAgain  plugin;
 
     public TEA_Listener(final TheEndAgain instance) {
         this.plugin = instance;
     }
 
     @EventHandler(priority = EventPriority.NORMAL)
     public void rewardOnEDDeath(final EntityDeathEvent event) {
         final EndWorldConfig config = this.plugin.mainEndConfig;
         if (!(event.getEntity() instanceof EnderDragon)) {
             return;
         }
         final EnderDragon ed = (EnderDragon) event.getEntity();
         final HashMap<String, Double> edData = this.plugin.data.get(ed.getUniqueId());
         if (edData != null) {
             // Compute total damages done to ED
             double totalDmg = 0.0;
             for (final String s : edData.keySet()) {
                 totalDmg += edData.get(s);
             }
 
             // Substracte damages from offline players & remove them from the list
             final Iterator<Entry<String, Double>> it = edData.entrySet().iterator();
             while (it.hasNext()) {
                 final Entry<String, Double> e = it.next();
                 final Player p = this.plugin.getServer().getPlayerExact(e.getKey());
                 if (p == null) {
                     totalDmg -= e.getValue();
                     it.remove();
                 }
             }
 
             if (config.getXpRewardingType() == 1) {
                 event.setDroppedExp(0);
                 // Now we can compute our percentages and give the exp to players
                 for (final String s : edData.keySet()) {
                     final Player p = this.plugin.getServer().getPlayerExact(s);
                     if (p != null) { // Just to be sure (Should always be true at this point)
                         final double expToGive = config.getXpReward() * (edData.get(s) / totalDmg);
                         p.giveExp((int) expToGive);
                         for (int i = 0; i < config.getExpMessage1().length - 1; i++) {
                             p.sendMessage(this.plugin.header + ChatColor.GREEN + this.plugin.toColor(config.getExpMessage1()[i]));
                         }
                         p.sendMessage(this.plugin.header + ChatColor.GREEN + this.plugin.toColor(config.getExpMessage1()[config.getExpMessage1().length - 1]) + (int) expToGive
                                 + this.plugin.toColor(config.getExpMessage2()[0]));
                         for (int i = 1; i < config.getExpMessage2().length; i++) {
                             p.sendMessage(this.plugin.header + ChatColor.GREEN + this.plugin.toColor(config.getExpMessage2()[i]));
                         }
                     }
                 }
             } else {
                 event.setDroppedExp(config.getXpReward());
             }
             if (config.getCustomEggHandling() == 1 && config.getPreventPortals() != 2) {
                 final SortedMap<String, Float> ratioMap = new TreeMap<String, Float>();
                 for (final String s : edData.keySet()) {
                     ratioMap.put(s, (float) (edData.get(s) / totalDmg));
                 }
                 final Iterator<Entry<String, Float>> it2 = ratioMap.entrySet().iterator();
                 while (it2.hasNext()) {
                     if (it2.next().getValue() < TEA_Listener.threshold) {
                         // Remove players who did little damages
                         it2.remove();
                     }
                 }
 
                 // Update ratio according to removed parts of total
                 float remainingRatioTotal = 0f;
                 for (final float f : ratioMap.values()) {
                     // Computing new total (should be <=1)
                     remainingRatioTotal += f;
                 }
                 if (remainingRatioTotal != 1) {
                     // Updating values
                     for (final String s : ratioMap.keySet()) {
                         ratioMap.put(s, ratioMap.get(s) * (1 / remainingRatioTotal));
                     }
                 }
 
                 // Now we will take a random player, the best fighter has the best chance to be choosen
                 float rand = new Random().nextFloat();
                 String playerName = null;
                 for (final Entry<String, Float> e : ratioMap.entrySet()) {
                     if (rand < e.getValue()) {
                         playerName = e.getKey();
                         break;
                     }
                     rand -= e.getValue();
                 }
                 if (playerName == null) { // Wtf ?
                     ed.getWorld().dropItem(ed.getLocation(), new ItemStack(Material.DRAGON_EGG));
                 } else {
                     final Player p = Bukkit.getServer().getPlayerExact(playerName);
                     if (p == null) {
                         ed.getWorld().dropItem(ed.getLocation(), new ItemStack(Material.DRAGON_EGG));
                     } else {
                         final HashMap<Integer, ItemStack> given = p.getInventory().addItem(new ItemStack(Material.DRAGON_EGG));
                         if (given.size() > 0) {
                             p.getWorld().dropItem(p.getLocation(), new ItemStack(Material.DRAGON_EGG));
                         }
                         p.sendMessage(this.plugin.header + ChatColor.GREEN + this.plugin.toColor(config.getEggMessage()));
                     }
                 }
             }
         }
         config.setNbEd(config.getNbEd() - 1);
         this.plugin.data.remove(ed.getUniqueId());
     }
 
     @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
     public void onPlayerHitED(final EntityDamageEvent event) {
         final EndWorldConfig config = this.plugin.mainEndConfig;
         if (!(event.getEntity() instanceof EnderDragon)) {
             return;
         }
         final EnderDragon ed = (EnderDragon) event.getEntity();
         final UUID edId = ed.getUniqueId();
         HashMap<String, Double> edData;
 
         if (!(event instanceof EntityDamageByEntityEvent)) {
             return;
         }
         final EntityDamageByEntityEvent eventByEntity = (EntityDamageByEntityEvent) event;
 
         // Handle custom ED health
         if (!this.plugin.edHealth.containsKey(edId)) {
             this.plugin.edHealth.put(edId, config.getEnderDragonHealth());
             if (config.getEnderDragonHealth() < 200) {
                 ed.setHealth(config.getEnderDragonHealth());
             }
         }
         final int oldHealth = this.plugin.edHealth.get(edId);
        int newHealth = oldHealth - event.getDamage();
        if (newHealth < 0) {
            // Consider only real damages
            event.setDamage(oldHealth);
            newHealth = 0;
        }
         this.plugin.edHealth.put(edId, newHealth);
         if (oldHealth > 200 + event.getDamage()) {
             event.setDamage(0);
         } else if (oldHealth > 200) {
             event.setDamage(200 - newHealth);
         }
 
         Player p;
         if (eventByEntity.getDamager() instanceof Player) {
             p = (Player) eventByEntity.getDamager();
         } else if (eventByEntity.getDamager() instanceof Projectile) {
             final LivingEntity e = ((Projectile) eventByEntity.getDamager()).getShooter();
             if (e instanceof Player) {
                 p = (Player) e;
             } else {
                 return;
             }
         } else {
             return;
         }
         final String playerName = p.getName();
 
         if (this.plugin.data.containsKey(edId)) {
             edData = this.plugin.data.get(edId);
         } else {
             edData = new HashMap<String, Double>();
         }
         double totalDmg = eventByEntity.getDamage();
         if (edData.containsKey(playerName)) {
             totalDmg += edData.get(playerName);
         }
 
         edData.put(playerName, totalDmg);
         this.plugin.data.put(edId, edData);
         System.out.println(event.getDamage());
     }
 
     @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
     public void onEDCreatePortal(final EntityCreatePortalEvent event) {
         final EndWorldConfig config = this.plugin.mainEndConfig;
         if (event.getEntityType().equals(EntityType.ENDER_DRAGON)) {
             if (config.getPreventPortals() == 1 && config.getCustomEggHandling() == 0) {
                 Block egg = null;
                 for (final BlockState b : event.getBlocks()) {
                     if (!b.getType().equals(Material.DRAGON_EGG)) {
                         b.setType(Material.AIR);
                     } else {
                         egg = b.getBlock();
                     }
                 }
                 if (egg != null) {
                     final int chunkX = egg.getChunk().getX();
                     final int chunkZ = egg.getChunk().getZ();
                     for (int x = chunkX - 2; x <= chunkX + 2; x++) {
                         for (int z = chunkZ - 2; z <= chunkZ + 2; z++) {
                             this.plugin.mainEndWorld.refreshChunk(x, z);
                         }
                     }
                 }
             } else if (config.getPreventPortals() == 2) {
                 event.setCancelled(true);
             } else if (config.getCustomEggHandling() == 1) {
                 for (final BlockState b : event.getBlocks()) {
                     if (b.getType().equals(Material.DRAGON_EGG)) {
                         b.setType(Material.AIR);
                         break;
                     }
                 }
             }
         }
     }
 
     @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
     public void onEndChunkLoad(final ChunkLoadEvent event) {
         final Chunk c = event.getChunk();
         if (c.getWorld().equals(this.plugin.mainEndWorld)) {
             ExtendedChunk chunk = this.plugin.endChunks.getChunk(c);
             if (chunk == null) {
                 chunk = this.plugin.endChunks.addChunk(c);
             } else if (chunk.hasToBeRegen()) {
                 // Prevent existing EnderDragons to be deleted by regen
                 int teleportDestX = 0;
                 int teleportDestZ = 0;
                 if (chunk.getX() == 0 && chunk.getZ() == 0) {
                     teleportDestX = 20;
                     teleportDestZ = 20;
                 }
                 final Location teleportDest = new Location(this.plugin.mainEndWorld, teleportDestX, 90, teleportDestZ);
                 for (final Entity e : c.getEntities()) {
                     if (e.getType() == EntityType.ENDER_DRAGON && ((EnderDragon) e).getHealth() > 0) {
                         e.teleport(teleportDest);
                     } else if (e.getType() != EntityType.ENDER_DRAGON) {
                         e.remove();
                     }
                 }
                 // Now regen
                 this.plugin.mainEndWorld.regenerateChunk(c.getX(), c.getZ());
                 this.plugin.mainEndWorld.refreshChunk(c.getX(), c.getZ());
                 chunk.setToBeRegen(false);
             }
         }
     }
 
     @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
     public void onEnderDragonSpawn(final CreatureSpawnEvent event) {
         final EndWorldConfig config = this.plugin.mainEndConfig;
         if (event.getEntityType() == EntityType.ENDER_DRAGON) {
             if (config.getNbEd() >= config.getActualNbMaxEnderDragon()) {
                 event.setCancelled(true);
             } else {
                 final EnderDragon ed = (EnderDragon) event.getEntity();
                 this.plugin.edHealth.put(ed.getUniqueId(), config.getEnderDragonHealth());
                 if (config.getEnderDragonHealth() < 200) {
                     ed.setHealth(config.getEnderDragonHealth());
                 }
                 config.setNbEd(config.getNbEd() + 1);
             }
         }
     }
 
     @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
     public void onEnderDragonRegainHealth(final EntityRegainHealthEvent event) {
         final EndWorldConfig config = this.plugin.mainEndConfig;
         if (event.getEntityType() == EntityType.ENDER_DRAGON && event.getRegainReason() == RegainReason.ENDER_CRYSTAL) {
             final EnderDragon ed = (EnderDragon) event.getEntity();
             if (ed.getHealth() > config.getEnderDragonHealth()) {
                 event.setCancelled(true);
                 ed.setHealth(config.getEnderDragonHealth());
             } else if (ed.getHealth() == config.getEnderDragonHealth()) {
                 event.setCancelled(true);
             } else if (ed.getHealth() + event.getAmount() > config.getEnderDragonHealth()) {
                 event.setAmount(config.getEnderDragonHealth() - ed.getHealth());
             }
             Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable() {
 
                 @Override
                 public void run() {
                     TEA_Listener.this.plugin.edHealth.put(ed.getUniqueId(), ed.getHealth());
                 }
             });
         }
     }
 }
