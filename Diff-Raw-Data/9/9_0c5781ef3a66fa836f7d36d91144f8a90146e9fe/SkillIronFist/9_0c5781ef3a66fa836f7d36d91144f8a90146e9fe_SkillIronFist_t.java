 package com.herocraftonline.dev.heroes.skill.skills;
 
 import java.util.List;
 
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 import org.bukkit.util.Vector;
 import org.bukkit.util.config.ConfigurationNode;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.persistence.Hero;
 import com.herocraftonline.dev.heroes.skill.ActiveSkill;
 import com.herocraftonline.dev.heroes.util.Setting;
 
 public class SkillIronFist extends ActiveSkill {
     
     public SkillIronFist(Heroes plugin) {
         super(plugin, "IronFist");
         setDescription("Damages and knocks back nearby enemies");
         setUsage("/skill ironfist");
         setArgumentRange(0, 0);
         setIdentifiers(new String[] { "skill ironfist", "skill ifist" });
     }
 
     @Override
     public ConfigurationNode getDefaultConfig() {
         ConfigurationNode node = super.getDefaultConfig();
         node.setProperty(Setting.DAMAGE.node(), 4);
         node.setProperty(Setting.RADIUS.node(), 3);
         node.setProperty("vertical-power", .25);
         node.setProperty("horizontal-power", .25);
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
 
             //PvP check
             if (target instanceof Player) {
                 EntityDamageEvent damageEvent = new EntityDamageEvent(player, DamageCause.CUSTOM, 0);
                 Bukkit.getServer().getPluginManager().callEvent(damageEvent);
                 if (damageEvent.isCancelled()) {
                    continue;
                 }
             }
             
             //Damage the target
             int damage = getSetting(hero.getHeroClass(), "damage", 1);
             addSpellTarget(target, hero);
             target.damage(damage, player);
             
             //Do our knockback
             float pitch = player.getEyeLocation().getPitch();
             float multiplier = getSetting(hero.getHeroClass(), "horizontal-power", 1) * (90f + pitch) / 40f;
             float vertPower = getSetting(hero.getHeroClass(), "vertical-power", 1);
             Vector v = target.getVelocity().setY(vertPower).add(player.getLocation().getDirection().setY(0).normalize().multiply(multiplier));
             target.setVelocity(v);
         }
         
         broadcastExecuteText(hero);
         return true;
     }
 }
