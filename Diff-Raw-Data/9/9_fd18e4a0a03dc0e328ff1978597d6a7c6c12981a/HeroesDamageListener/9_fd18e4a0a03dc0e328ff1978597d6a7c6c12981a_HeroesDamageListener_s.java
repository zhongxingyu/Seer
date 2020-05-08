 package com.herocraftonline.dev.heroes.damage;
 
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.Level;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Material;
 import org.bukkit.entity.CreatureType;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Projectile;
 import org.bukkit.event.entity.CreatureSpawnEvent;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 import org.bukkit.event.entity.EntityListener;
 import org.bukkit.event.entity.EntityRegainHealthEvent;
 import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.api.HeroAttackDamageCause;
 import com.herocraftonline.dev.heroes.api.HeroDamageCause;
 import com.herocraftonline.dev.heroes.api.HeroSkillDamageCause;
 import com.herocraftonline.dev.heroes.api.SkillDamageEvent;
 import com.herocraftonline.dev.heroes.api.SkillUseInfo;
 import com.herocraftonline.dev.heroes.api.WeaponDamageEvent;
 import com.herocraftonline.dev.heroes.damage.DamageManager.ProjectileType;
 import com.herocraftonline.dev.heroes.effects.EffectType;
 import com.herocraftonline.dev.heroes.hero.Hero;
 import com.herocraftonline.dev.heroes.party.HeroParty;
 import com.herocraftonline.dev.heroes.util.Util;
 
 public class HeroesDamageListener extends EntityListener {
 
     private Heroes plugin;
     private DamageManager damageManager;
 
     private static final Map<Material, Integer> armorPoints;
 
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
 
     private void onEntityDamageCore(EntityDamageEvent event) {
         if (event.isCancelled() || plugin.getConfigManager().getProperties().disabledWorlds.contains(event.getEntity().getWorld().getName()))
             return;
 
         if (event.getCause() == DamageCause.SUICIDE) {
             if (event.getEntity() instanceof Player) {
                 Player player = (Player) event.getEntity();
                 plugin.getHeroManager().getHero(player).setHealth(0D);
                 return;
             }
         }
 
         Entity defender = event.getEntity();
         Entity attacker = null;
         HeroDamageCause heroLastDamage = null;
         DamageCause cause = event.getCause();
         int damage = event.getDamage();
 
         if (damageManager.isSpellTarget(defender)) {
             SkillUseInfo skillInfo = damageManager.getSpellTargetInfo(defender);
             damageManager.removeSpellTarget(defender);
             if (event instanceof EntityDamageByEntityEvent) {
                 SkillDamageEvent spellDamageEvent = new SkillDamageEvent(damage, defender, skillInfo);
                 plugin.getServer().getPluginManager().callEvent(spellDamageEvent);
                 if (spellDamageEvent.isCancelled()) {
                     event.setCancelled(true);
                     return;
                 }
                 damage = spellDamageEvent.getDamage();
                 if (defender instanceof Player) {
                     heroLastDamage = new HeroSkillDamageCause(damage, cause, skillInfo.getHero().getPlayer(), skillInfo.getSkill());
                 }
             }
         } else if (damage != 0) {
             if (event instanceof EntityDamageByEntityEvent) {
                 attacker = ((EntityDamageByEntityEvent) event).getDamager();
                 if (attacker instanceof Player) {
                     // Get the damage this player should deal for the weapon they are using
                     damage = getPlayerDamage((Player) attacker, damage);
                 } else if (attacker instanceof LivingEntity) {
                     CreatureType type = Util.getCreatureFromEntity(attacker);
                     if (type != null) {
                         if (type == CreatureType.CREEPER && cause == DamageCause.ENTITY_ATTACK) {
                             // Ghetto fix for creepers throwing two damage events
                             damage = 0;
                             return;
                         } else {
                             Integer tmpDamage = damageManager.getCreatureDamage(type);
                             if (tmpDamage != null) {
                                 damage = tmpDamage;
                             }
                         }
                     }
                 } else if (attacker instanceof Projectile) {
                     Projectile projectile = (Projectile) attacker;
                     if (projectile.getShooter() instanceof Player) {
                         attacker = projectile.getShooter();
                         // Allow alteration of player damage
                         damage = getPlayerProjectileDamage((Player) projectile.getShooter(), projectile, damage);
                         damage = (int) ((damage / 3D) * Math.ceil(projectile.getVelocity().length()));
                     } else {
                         attacker = projectile.getShooter();
                         CreatureType type = Util.getCreatureFromEntity(projectile.getShooter());
                         if (type != null) {
                             Integer tmpDamage = damageManager.getCreatureDamage(type);
                             if (tmpDamage != null) {
                                 damage = tmpDamage;
                             }
                         }
                     }
                 }
                 // Call the custom event to allow skills to adjust weapon damage
                 WeaponDamageEvent weaponDamageEvent = new WeaponDamageEvent(damage, (EntityDamageByEntityEvent) event);
                 plugin.getServer().getPluginManager().callEvent(weaponDamageEvent);
                 if (weaponDamageEvent.isCancelled()) {
                     event.setCancelled(true);
                     return;
                 }
                 damage = weaponDamageEvent.getDamage();
                 heroLastDamage = new HeroAttackDamageCause(damage, cause, attacker);
             } else if (cause != DamageCause.CUSTOM) {
                 Integer tmpDamage = damageManager.getEnvironmentalDamage(cause);
                 if (tmpDamage != null) {
                     damage = tmpDamage;
                     if (cause == DamageCause.FALL) {
                         if (event.getDamage() != 0)
                             damage += damage / 3 * (event.getDamage() - 3);
                     }
                 }
                 heroLastDamage = new HeroDamageCause(damage, cause);
             } else {
                 heroLastDamage = new HeroDamageCause(damage, cause);
             }
         }
 
         if (defender instanceof Player) {
             Player player = (Player) defender;
             if (player.getNoDamageTicks() > player.getMaximumNoDamageTicks() / 2.0f || player.isDead() || player.getHealth() <= 0) {
                 event.setCancelled(true);
                 return;
             }
             final Hero hero = plugin.getHeroManager().getHero(player);
             // If the defender is a player
             if (hero.hasEffectType(EffectType.INVULNERABILITY)) {
                 event.setCancelled(true);
                 return;
             } else if (cause == DamageCause.FALL && hero.hasEffectType(EffectType.SAFEFALL)) {
                 event.setCancelled(true);
                 return;
             }
 
             // Party damage & PvPable test
             if (attacker instanceof Player) {
                 // If the players aren't within the level range then deny the PvP
                 int aLevel = plugin.getHeroManager().getHero((Player) attacker).getLevel();
                 if (Math.abs(aLevel - hero.getLevel()) > plugin.getConfigManager().getProperties().pvpLevelRange) {
                     event.setCancelled(true);
                     return;
                 }
                 HeroParty party = hero.getParty();
                 if (party != null && party.isNoPvp()) {
                     if (party.isPartyMember((Player) attacker)) {
                         event.setCancelled(true);
                         return;
                     }
                 }
             }
 
             if (damage == 0) {
                 event.setDamage(0);
                 return;
             }
 
             int damageReduction = calculateArmorReduction(player.getInventory(), damage);
             damage -= damageReduction;
             if (damage < 0) {
                 damage = 0;
             }
 
             hero.setLastDamageCause(heroLastDamage);
 
             double iHeroHP = hero.getHealth();
             double fHeroHP = iHeroHP - damage;
             // Never set HP less than 0
             if (fHeroHP < 0) {
                 fHeroHP = 0;
             }
 
             // Round up to get the number of remaining Hearts
            int fPlayerHP = (int) Math.ceil(fHeroHP / hero.getMaxHealth() * 20);
             plugin.debugLog(Level.INFO, "Damage done to " + player.getName() + " by " + cause + ": " + iHeroHP + " -> " + fHeroHP + "   |   " + player.getHealth() + " -> " + fPlayerHP);
 
             hero.setHealth(fHeroHP);
 
             // If final HP is 0, make sure we kill the player
             if (fHeroHP == 0) {
                 event.setDamage(200);
             } else {
                 player.setHealth(fPlayerHP + damage);
                 event.setDamage(damage + damageReduction);
 
                 // Make sure health syncs on the next tick
                 Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                     @Override
                     public void run() {
                         hero.syncHealth();
                     }
                 }, 1);
             }
             HeroParty party = hero.getParty();
             if (party != null && event.getDamage() > 0 && !party.updateMapDisplay()) {
                 party.setUpdateMapDisplay(true);
             }
 
             // Do our Damage-Dependant effect removals last
             if (hero.hasEffect("Invisible")) {
                 hero.removeEffect(hero.getEffect("Invisible"));
             }
 
         } else if (defender instanceof LivingEntity) {
             event.setDamage(damage);
         }
     }
 
     @Override
     public void onEntityDamage(EntityDamageEvent event) {
         Heroes.debug.startTask("HeroesDamageListener.onEntityDamage");
         onEntityDamageCore(event);
         Heroes.debug.stopTask("HeroesDamageListener.onEntityDamage");
     }
 
     @Override
     public void onEntityRegainHealth(EntityRegainHealthEvent event) {
         Heroes.debug.startTask("HeroesDamageListener.onEntityRegainHealth");
         if (event.isCancelled() || !(event.getEntity() instanceof Player)) {
             Heroes.debug.stopTask("HeroesDamageListener.onEntityRegainHealth");
             return;
         }
 
         double amount = event.getAmount();
         Player player = (Player) event.getEntity();
         Hero hero = plugin.getHeroManager().getHero(player);
         double maxHealth = hero.getMaxHealth();
         double healPercent = plugin.getConfigManager().getProperties().foodHealPercent;
         // Satiated players regenerate % of total HP rather than 1 HP
         if (event.getRegainReason() == RegainReason.SATIATED) {
             amount = maxHealth * healPercent;
         }
         double newHeroHealth = hero.getHealth() + amount;
        int newPlayerHealth = (int) Math.ceil(newHeroHealth / maxHealth * 20);
         hero.setHealth(newHeroHealth);
         event.setAmount(newPlayerHealth - player.getHealth());
         Heroes.debug.stopTask("HeroesDamageListener.onEntityRegainHealth");
     }
 
     private int calculateArmorReduction(PlayerInventory inventory, int damage) {
         ItemStack[] armorContents = inventory.getArmorContents();
 
         int missingDurability = 0;
         int maxDurability = 0;
         int baseArmorPoints = 0;
         boolean hasArmor = false;
 
         for (ItemStack armor : armorContents) {
             Material armorType = armor.getType();
             if (armorPoints.containsKey(armorType)) {
                 short armorDurability = armor.getDurability();
                 // Ignore non-durable items
                 if (armorDurability == -1) {
                     continue;
                 }
                 missingDurability += armorDurability;
                 maxDurability += armorType.getMaxDurability();
                 baseArmorPoints += armorPoints.get(armorType);
                 hasArmor = true;
             }
         }
 
         if (!hasArmor)
             return 0;
 
         double armorPoints = (double) baseArmorPoints * (maxDurability - missingDurability) / maxDurability;
         double damageReduction = 0.04 * armorPoints;
         return (int) (damageReduction * damage);
     }
 
     private int getPlayerDamage(Player attacker, int damage) {
         ItemStack weapon = attacker.getItemInHand();
         Material weaponType = weapon.getType();
 
         Integer tmpDamage = damageManager.getItemDamage(weaponType, attacker);
         return tmpDamage == null ? damage : tmpDamage;
     }
 
     private int getPlayerProjectileDamage(Player attacker, Projectile projectile, int damage) {
         Integer tmpDamage = damageManager.getProjectileDamage(ProjectileType.valueOf(projectile), attacker);
         return tmpDamage == null ? damage : tmpDamage;
     }
 
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
 }
