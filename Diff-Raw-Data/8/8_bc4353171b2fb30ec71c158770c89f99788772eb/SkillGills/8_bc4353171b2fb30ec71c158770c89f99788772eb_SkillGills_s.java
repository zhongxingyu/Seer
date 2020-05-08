 package com.herocraftonline.dev.heroes.skill.skills;
 
 import org.bukkit.entity.Player;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityListener;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.persistence.Hero;
 import com.herocraftonline.dev.heroes.skill.ActiveEffectSkill;
 
 public class SkillGills extends ActiveEffectSkill {
 
     public SkillGills(Heroes plugin) {
         super(plugin);
         name = "Gills";
         description = "Negate drowning damage";
         usage = "/skill gills";
         minArgs = 0;
         maxArgs = 0;
         identifiers.add("skill gills");
     }
 
    public class SkillPlayerListener extends EntityListener {
 
         public void onEntityDamage(EntityDamageEvent event) {
             if (event.isCancelled() || !(event.getCause() == DamageCause.DROWNING)) {
                 return;
             }
             if (event.getEntity() instanceof Player) {
                 Player player = (Player) event.getEntity();
                 Hero hero = plugin.getHeroManager().getHero(player);
                 if (hero.getEffects().hasEffect(name)) {
                     event.setCancelled(true);
                 }
             }
         }
     }
 
     @Override
     public boolean use(Hero hero, String[] args) {
         Player player = hero.getPlayer();
         String playerName = player.getName();
         applyEffect(hero);
         notifyNearbyPlayers(player.getLocation(), useText, playerName, name);
         return true;
     }
 }
