 package com.herocraftonline.dev.heroes.damage;
 
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.Level;
 
 import org.bukkit.Material;
 import org.bukkit.entity.CreatureType;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.entity.CreatureSpawnEvent;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageByProjectileEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 import org.bukkit.event.entity.EntityListener;
 import org.bukkit.event.entity.EntityRegainHealthEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.persistence.Hero;
 import com.herocraftonline.dev.heroes.util.Properties;
 
 // import org.bukkit.entity.Projectile;
 // import com.herocraftonline.dev.heroes.damage.DamageManager.ProjectileType;
 
 public class HeroesDamageListener extends EntityListener {
 
     private Heroes plugin;
     private DamageManager damageManager;
 
     private static final Map<Material, Integer> armorPoints;
 
     static {
         Map<Material, Integer> aMap = new HashMap<Material, Integer>();
         aMap.put(Material.LEATHER_HELMET, 3);
         aMap.put(Material.LEATHER_CHESTPLATE, 8);
         aMap.put(Material.LEATHER_LEGGINGS, 6);
         aMap.put(Material.LEATHER_BOOTS, 3);
 
         aMap.put(Material.GOLD_HELMET, 3);
         aMap.put(Material.GOLD_CHESTPLATE, 8);
         aMap.put(Material.GOLD_LEGGINGS, 6);
         aMap.put(Material.GOLD_BOOTS, 3);
 
         aMap.put(Material.CHAINMAIL_HELMET, 3);
         aMap.put(Material.CHAINMAIL_CHESTPLATE, 8);
         aMap.put(Material.CHAINMAIL_LEGGINGS, 6);
         aMap.put(Material.CHAINMAIL_BOOTS, 3);
 
         aMap.put(Material.IRON_HELMET, 3);
         aMap.put(Material.IRON_CHESTPLATE, 8);
         aMap.put(Material.IRON_LEGGINGS, 6);
         aMap.put(Material.IRON_BOOTS, 3);
 
         aMap.put(Material.DIAMOND_HELMET, 3);
         aMap.put(Material.DIAMOND_CHESTPLATE, 8);
         aMap.put(Material.DIAMOND_LEGGINGS, 6);
         aMap.put(Material.DIAMOND_BOOTS, 3);
         armorPoints = Collections.unmodifiableMap(aMap);
     }
 
     public HeroesDamageListener(Heroes plugin, DamageManager damageManager) {
         this.plugin = plugin;
         this.damageManager = damageManager;
     }
 
     @Override
     public void onCreatureSpawn(CreatureSpawnEvent event) {
         LivingEntity entity = (LivingEntity) event.getEntity();
         CreatureType type = event.getCreatureType();
         Integer maxHealth = damageManager.getCreatureHealth(type);
         if (maxHealth != null) {
             entity.setHealth(maxHealth);
         }
     }
 
     @Override
     public void onEntityRegainHealth(EntityRegainHealthEvent event) {
         if (event.isCancelled()) {
             return;
         }
 
         Entity entity = event.getEntity();
         int amount = event.getAmount();
 
         if (entity instanceof Player) {
             Player player = (Player) entity;
             Hero hero = plugin.getHeroManager().getHero(player);
             double newHeroHealth = hero.getHealth() + amount;
             int newHealth = (int) (newHeroHealth / hero.getMaxHealth() * 20);
             int newAmount = newHealth - player.getHealth();
             hero.setHealth(newHeroHealth);
             event.setAmount(newAmount);
         }
     }
 
     private int calculateArmorReduction(PlayerInventory inventory, int damage) {
         ItemStack[] armorContents = inventory.getArmorContents();
 
         int missingDurability = 0;
         int maxDurability = 0;
         int baseArmorPoints = 0;
         boolean hasArmor = false;
 
         for (ItemStack armor : armorContents) {
             Material armorType = armor.getType();
             if (armorType != Material.AIR) {
                 short armorDurability = armor.getDurability();
                 missingDurability += armorDurability;
                 maxDurability += armorType.getMaxDurability();
                 baseArmorPoints += armorPoints.get(armorType);
                 hasArmor = true;
             }
         }
 
         if (!hasArmor) {
             return 0;
         }
 
         double armorPoints = (double) baseArmorPoints * (maxDurability - missingDurability) / maxDurability;
         double damageReduction = 0.04 * armorPoints;
         return (int) (damageReduction * damage);
     }
 
     @Override
     public void onEntityDamage(EntityDamageEvent event) {
         // System.out.println("HDL   cancelled: " + event.isCancelled() + "   damage: " + event.getDamage() +
         // "   entity: " + event.getEntity() + "   type: " + event.getClass().getSimpleName());
 
         if (event.isCancelled()) return;
 
         Entity entity = event.getEntity();
         DamageCause cause = event.getCause();
         int damage = event.getDamage();
         if (damage == 0) return;
         if (damageManager.getSpellTargets().contains(entity)) { // Start of skill -> listener communication
             damageManager.getSpellTargets().remove(entity);
         } else {
             if (event instanceof EntityDamageByEntityEvent) {
                 if (event instanceof EntityDamageByProjectileEvent) {
                     // Projectile projectile = ((EntityDamageByProjectileEvent) event).getProjectile();
                     // DamageManager.ProjectileType type = DamageManager.ProjectileType.valueOf(projectile);
                     // Integer tmpDamage = damageManager.getProjectileDamage(type, (HumanEntity)
                     // projectile.getShooter());
                     // if (tmpDamage != null) {
                     // damage = tmpDamage;
                     // }
                 } else {
                     Entity attacker = ((EntityDamageByEntityEvent) event).getDamager();
                     if (attacker instanceof Player) {
                         Player attackingPlayer = (Player) attacker;
                         ItemStack weapon = attackingPlayer.getItemInHand();
                         Material weaponType = weapon.getType();
 
                         Integer tmpDamage = damageManager.getItemDamage(weaponType, attackingPlayer);
                         if (tmpDamage != null) {
                             damage = tmpDamage;
                         }
                     } else {
                         CreatureType type = Properties.getCreatureFromEntity(attacker);
                         if (type != null) {
                             Integer tmpDamage = damageManager.getCreatureDamage(type);
                             if (tmpDamage != null) {
                                 damage = tmpDamage;
                             }
                         }
                     }
                 }
             } else if (cause != DamageCause.CUSTOM) {
                 Integer tmpDamage = damageManager.getEnvironmentalDamage(cause);
                 if (tmpDamage != null) {
                     damage = tmpDamage;
                     if (cause == DamageCause.FALL) {
                         damage += damage / 3 * (event.getDamage() - 3);
                     }
                 }
             }
         } // End of skill -> listener communication
 
         if (entity instanceof Player) {
             Player player = (Player) entity;
             if ((float) player.getNoDamageTicks() > (float) player.getMaximumNoDamageTicks() / 2.0f) {
                 return;
             }

             Hero hero = plugin.getHeroManager().getHero(player);
             int damageReduction = calculateArmorReduction(player.getInventory(), damage);
             damage -= damageReduction;
             if (damage < 0) {
                 damage = 0;
             }
 
             double iHeroHP = hero.getHealth();
             double fHeroHP = iHeroHP - damage;
             int fPlayerHP = (int) (fHeroHP / hero.getMaxHealth() * 20);
             plugin.debugLog(Level.INFO, "Damage: " + iHeroHP + " -> " + fHeroHP + "   |   " + player.getHealth() + " -> " + fPlayerHP);
 
             hero.setHealth(fHeroHP);
             player.setHealth(fPlayerHP + damage);
             event.setDamage(damage + damageReduction);
         } else if (entity instanceof LivingEntity) {
             event.setDamage(damage);
         }
     }
 }
