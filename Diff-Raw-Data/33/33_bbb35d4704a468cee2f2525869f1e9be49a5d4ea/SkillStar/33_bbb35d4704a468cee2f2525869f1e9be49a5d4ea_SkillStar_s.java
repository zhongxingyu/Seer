 package com.herocraftonline.dev.heroes.skill.skills;
 
import java.util.LinkedHashMap;
import java.util.Map;

 import org.bukkit.entity.Egg;
 import org.bukkit.entity.Entity;
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
 
    private LinkedHashMap<Integer, Integer> eggs;

    @SuppressWarnings("serial")
     public SkillStar(Heroes plugin) {
         super(plugin);
         name = "Star";
         description = "Throw eggs for damage";
         minArgs = 0;
         maxArgs = 0;
 
         registerEvent(Type.PLAYER_EGG_THROW, new SkillPlayerListener(), Priority.Normal);
         registerEvent(Type.ENTITY_DAMAGE, new SkillEntityListener(), Priority.Normal);

        eggs = new LinkedHashMap<Integer, Integer>() {
            private static final int MAX_ENTRIES = 1000;

            protected boolean removeEldestEntry(Map.Entry<Integer, Integer> eldest) {
                return size() > MAX_ENTRIES;
            }
        };
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
                eggs.put(event.getEgg().getEntityId(), getSetting(hero.getHeroClass(), "damage", 4));
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
                    if (eggs.containsKey(projectile.getEntityId())) {
                        event.setDamage(eggs.get(projectile));
                        eggs.remove(projectile);
                     }
                 }
             }
         }
 
     }
 
 }
