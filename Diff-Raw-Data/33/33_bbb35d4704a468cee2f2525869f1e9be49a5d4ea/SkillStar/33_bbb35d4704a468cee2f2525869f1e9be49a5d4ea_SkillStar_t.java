 package com.herocraftonline.dev.heroes.skill.skills;
 
 import org.bukkit.entity.Egg;
 import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event.Type;
 import org.bukkit.event.entity.EntityDamageByProjectileEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityListener;
 import org.bukkit.event.player.PlayerEggThrowEvent;
 import org.bukkit.event.player.PlayerListener;
 import org.bukkit.util.config.ConfigurationNode;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.persistence.Hero;
 import com.herocraftonline.dev.heroes.persistence.HeroEffects;
 import com.herocraftonline.dev.heroes.skill.PassiveSkill;
 
 public class SkillStar extends PassiveSkill {
 
     public SkillStar(Heroes plugin) {
         super(plugin);
         name = "Star";
         description = "Throw eggs for damage";
         minArgs = 0;
         maxArgs = 0;
 
         registerEvent(Type.PLAYER_EGG_THROW, new SkillPlayerListener(), Priority.Normal);
         registerEvent(Type.ENTITY_DAMAGE, new SkillEntityListener(), Priority.Normal);
     }
 
     @Override
     public ConfigurationNode getDefaultConfig() {
         ConfigurationNode node = super.getDefaultConfig();
         node.setProperty("damage", 4);
         return node;
     }
 
     public class SkillPlayerListener extends PlayerListener {
 
         public void onPlayerEggThrow(PlayerEggThrowEvent event) {
             Player player = event.getPlayer();
             Hero hero = plugin.getHeroManager().getHero(player);
             HeroEffects effects = hero.getEffects();
             if (effects.hasEffect(name)) {
                 event.setHatching(false);
             }
 
         }
     }
 
     public class SkillEntityListener extends EntityListener {
 
         @Override
         public void onEntityDamage(EntityDamageEvent event) {
             if (event.isCancelled()) {
                 return;
             }
 
             if (event instanceof EntityDamageByProjectileEvent) {
                 EntityDamageByProjectileEvent subEvent = (EntityDamageByProjectileEvent) event;
                 Entity projectile = subEvent.getProjectile();
                 if (projectile instanceof Egg) {
                    Egg egg = (Egg) projectile;
                    LivingEntity shooter = egg.getShooter();
                    if (shooter instanceof Player) {
                        Player shootingPlayer = (Player) shooter;
                        Hero hero = plugin.getHeroManager().getHero(shootingPlayer);
                        if (hero.getEffects().hasEffect(name)) {
                            int damage = getSetting(hero.getHeroClass(), "damage", 4);
                            event.setDamage(damage);
                        }
                     }
                 }
             }
         }
 
     }
 
 }
