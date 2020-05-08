 package com.herocraftonline.dev.heroes.skill.skills;
 
 import java.util.List;
 
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 import org.bukkit.util.config.ConfigurationNode;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.persistence.Hero;
 import com.herocraftonline.dev.heroes.skill.ActiveSkill;
 import com.herocraftonline.dev.heroes.util.Setting;
 
 public class SkillPulse extends ActiveSkill {
 
     public SkillPulse(Heroes plugin) {
         super(plugin, "Pulse");
         setDescription("Damages everyone around you");
         setUsage("/skill pulse");
         setArgumentRange(0, 0);
         setIdentifiers(new String[] { "skill pulse" });
     }
 
     @Override
     public ConfigurationNode getDefaultConfig() {
         ConfigurationNode node = super.getDefaultConfig();
         node.setProperty(Setting.DAMAGE.node(), 1);
         node.setProperty(Setting.RADIUS.node(), 5);
         return node;
     }
 
     @Override
     public boolean use(Hero hero, String[] args) {
         Player player = hero.getPlayer();
         int radius = getSetting(hero.getHeroClass(), Setting.RADIUS.node(), 5);
         List<Entity> entities = hero.getPlayer().getNearbyEntities(radius, radius, radius);
         for (Entity entity : entities) {
             if (!(entity instanceof LivingEntity)) {
                 continue;
             }
             LivingEntity target = (LivingEntity) entity;
             if (target.equals(player)) {
                 continue;
             }
 
             if (target instanceof Player) {
                 EntityDamageEvent damageEvent = new EntityDamageEvent(player, DamageCause.CUSTOM, 0);
                 Bukkit.getServer().getPluginManager().callEvent(damageEvent);
                 if (damageEvent.isCancelled()) {
                    return false;
                 }
             }
 
             int damage = getSetting(hero.getHeroClass(), "damage", 1);
             addSpellTarget(target, hero);
             target.damage(damage, player);
         }
         broadcastExecuteText(hero);
         return true;
     }
 
 }
