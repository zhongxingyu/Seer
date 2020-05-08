 package com.herocraftonline.dev.heroes.skill.skills;
 
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Snowball;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event.Type;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageByProjectileEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 import org.bukkit.event.entity.EntityListener;
 import org.bukkit.util.config.ConfigurationNode;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.classes.HeroClass;
 import com.herocraftonline.dev.heroes.persistence.Hero;
 import com.herocraftonline.dev.heroes.skill.ActiveSkill;
 
 public class SkillFireball extends ActiveSkill {
 
     public SkillFireball(Heroes plugin) {
         super(plugin, "Fireball");
         setDescription("Shoots a dangerous ball of fire");
         setUsage("/skill fireball");
         setArgumentRange(0, 0);
         setIdentifiers(new String[]{"skill fireball"});
 
         registerEvent(Type.ENTITY_DAMAGE, new SkillEntityListener(), Priority.Normal);
     }
 
     @Override
     public ConfigurationNode getDefaultConfig() {
         ConfigurationNode node = super.getDefaultConfig();
         node.setProperty("damage", 4);
         node.setProperty("fire-ticks", 100);
         return node;
     }
 
     @Override
     public boolean use(Hero hero, String[] args) {
         Player player = hero.getPlayer();
 
         Snowball snowball = player.throwSnowball();
         snowball.setFireTicks(1000);
 
         broadcastExecuteText(hero);
         return true;
     }
 
     public class SkillEntityListener extends EntityListener {
 
         @Override
         public void onEntityDamage(EntityDamageEvent event) {
             if (event.isCancelled()) return;
             if (event instanceof EntityDamageByProjectileEvent) {
                 EntityDamageByProjectileEvent subEvent = (EntityDamageByProjectileEvent) event;
                 Entity projectile = subEvent.getProjectile();
                 if (projectile instanceof Snowball) {
                     if (projectile.getFireTicks() > 0) {
                         Entity entity = subEvent.getEntity();
                         if (entity instanceof LivingEntity) {
                             Entity dmger = subEvent.getDamager();
                             if (dmger instanceof Player) {
                                 Hero hero = getPlugin().getHeroManager().getHero((Player) dmger);
                                 HeroClass heroClass = hero.getHeroClass();
                                LivingEntity livingEntity = (LivingEntity) entity;
                                 // Perform a check to see if any plugin is preventing us from damaging the player.
                                EntityDamageByEntityEvent damageEvent = new EntityDamageByEntityEvent(dmger, livingEntity, DamageCause.ENTITY_ATTACK, 0);
                                 Bukkit.getServer().getPluginManager().callEvent(damageEvent);
                                 if (damageEvent.isCancelled()) return;
                                 // Damage the player and ignite them.
                                 livingEntity.setFireTicks(getSetting(heroClass, "fire-ticks", 100));
 
                                 getPlugin().getDamageManager().addSpellTarget((Entity) entity);
                                 int damage = getSetting(heroClass, "damage", 4);
                                 event.setDamage(damage);
                             }
                         }
                     }
                 }
             }
         }
 
     }
 
 }
