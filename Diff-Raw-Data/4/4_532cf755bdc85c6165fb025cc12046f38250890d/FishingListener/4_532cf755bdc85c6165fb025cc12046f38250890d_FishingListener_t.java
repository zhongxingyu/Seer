 package com.norcode.bukkit.enhancedfishing;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Random;
 
 import org.bukkit.Material;
 import org.bukkit.block.Biome;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.entity.Boat;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Fish;
 import org.bukkit.entity.Item;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.player.PlayerFishEvent;
 import org.bukkit.event.weather.LightningStrikeEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.permissions.Permission;
 
 public class FishingListener implements Listener {
     EnhancedFishing plugin;
     private Random random = new Random();
     private HashMap<String, Fish> playerHooks = new HashMap<String, Fish>();
     public FishingListener(EnhancedFishing plugin) {
         this.plugin = plugin;
         
     }
 
     @EventHandler(ignoreCancelled=true, priority=EventPriority.MONITOR)
     public void onLightningStrike(LightningStrikeEvent event) {
         for (Entity e: event.getLightning().getNearbyEntities(plugin.getLightningRadius(), 4, plugin.getLightningRadius())) {
             double chance;
             if (e instanceof Fish) {
                 chance = plugin.getLightningModifier().apply(((Fish) e).getBiteChance());
                 if (chance > 1.0) chance = 1.0;
                 ((Fish) e).setBiteChance(chance);
             }
         }
     }
 
     @EventHandler(ignoreCancelled=true, priority=EventPriority.HIGH)
     public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
         if (event.getDamager() instanceof Fish) {
             Player player = (Player) ((Fish) event.getDamager()).getShooter(); 
             if (player.getItemInHand() != null && player.getItemInHand().getType().equals(Material.FISHING_ROD)) {
                 int thorns = player.getItemInHand().getEnchantmentLevel(Enchantment.THORNS);
                if (thorns > 0) {
                    event.setDamage(thorns+2);
                }
             }
         }
     }
 
     @EventHandler(ignoreCancelled=true, priority=EventPriority.HIGH)
     public void onPlayerFish(PlayerFishEvent event) {
         final Player player = event.getPlayer();
         ItemStack rod = player.getItemInHand();
         if (event.getState().equals(PlayerFishEvent.State.FISHING)) {
             playerHooks.put(player.getName(), event.getHook());
             plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable() {
                 @Override
                 public void run() {
                     Fish hook = playerHooks.get(player.getName());
                     if (!hook.isDead() && hook.isValid()) {
                         hook.setBiteChance(calculateBiteChance(hook));
                     }
                 }
             }, 40);
         } else if (event.getState().equals(PlayerFishEvent.State.CAUGHT_FISH)) {
              int looting = rod.getEnchantmentLevel(Enchantment.LOOT_BONUS_MOBS);
              int fortune = rod.getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS);
              int fireaspect = rod.getEnchantmentLevel(Enchantment.FIRE_ASPECT);
              Item item = (Item) event.getCaught();
              if (item.getItemStack() != null) {
                  if (looting > 0 && plugin.isLootingEnabled() && random.nextDouble() < Math.min(looting * plugin.getLootingLevelChance(), 1.0)
                          && player.hasPermission("enhancedfishing.enchantment.looting")) {
                      item.setItemStack(plugin.getLootTable().get(looting).getStack().clone());
                  }
     
                  if (fortune > 0 && plugin.isFortuneEnabled() && random.nextDouble() < Math.min(fortune * plugin.getFortuneLevelChance(), 1.0)
                          && player.hasPermission("enhancedfishing.enchantment.fortune")) {
                      ItemStack stack = item.getItemStack().clone();
                      stack.setAmount(random.nextInt(Math.max(1, fortune-1))+2);
                      item.setItemStack(stack);
                  }
     
                  if (fireaspect > 0 && item.getItemStack().getType().equals(Material.RAW_FISH) &&
                          plugin.isFireAspectEnabled() && player.hasPermission("enhancedfishing.enchantment.fireaspect")) {
                      item.setItemStack(new ItemStack(Material.COOKED_FISH, item.getItemStack().getAmount()));
                  }
              }
         } 
     }
 
     protected double calculateBiteChance(Fish hook) {
         Player p = (Player) hook.getShooter();
         ItemStack rod = p.getItemInHand();
         double chance = plugin.getBaseCatchChance();
         for (Permission perm: plugin.getLoadedPermissions()) {
             if (p.hasPermission(perm)) {
                 chance = new DoubleModifier(plugin.getConfig().getString("bite-chance." + perm.getName().substring(28))).apply(chance);
             }
         }
 
         if (hook.getBiteChance() == 1/300.0) {
             chance = plugin.getRainModifier().apply(chance);
         } else if (hook.getWorld().getTime() > plugin.getSunriseStart() && hook.getWorld().getTime() < plugin.getSunriseEnd()) {
             chance = plugin.getSunriseModifier().apply(chance);
         }
 
         if (p.getVehicle() != null && p.getVehicle() instanceof Boat) {
             chance = plugin.getBoatModifier().apply(chance);
         }
 
         if (rod != null && rod.getType().equals(Material.FISHING_ROD) && p.hasPermission("enhancedfishing.enchantment.efficiency")) {
             Map<Enchantment, Integer> enchantments = rod.getEnchantments();
             if (enchantments.containsKey(Enchantment.DIG_SPEED) && plugin.isEfficiencyEnabled()) {
                 for (int i=0;i<enchantments.get(Enchantment.DIG_SPEED);i++) {
                     chance = plugin.getEfficiencyLevelModifier().apply(chance);
                 }
             }
         }
 
         double r = Math.max(plugin.getCrowdingRadius(), plugin.getMobRadius());
         for (Entity e: hook.getNearbyEntities(r, 4, r)) {
             if (e instanceof Fish && e.getLocation().distance(hook.getLocation()) <= plugin.getCrowdingRadius()) {
                 chance = plugin.getCrowdingModifier().apply(chance);
             } else if (e instanceof LivingEntity && !e.equals(hook.getShooter()) && e.getLocation().distance(hook.getLocation()) <= plugin.getMobRadius()) {
                 chance = plugin.getMobsModifier().apply(chance);
             }
         }
         Biome biome = hook.getWorld().getBiome(hook.getLocation().getBlockX(), hook.getLocation().getBlockY());
         chance = plugin.getBiomeModifier(biome).apply(chance);
         return chance;
     }
 }
