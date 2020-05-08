 package com.herocraftonline.dev.heroes.effects;
 
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 
 import com.herocraftonline.dev.heroes.hero.Hero;
 import com.herocraftonline.dev.heroes.skill.Skill;
 
 public class StunEffect extends PeriodicExpirableEffect {
 
     private static final long period = 100;
     private final String stunApplyText = "$1 is stunned!";
     private final String stunExpireText = "$1 is no longer stunned!";
     private Location loc;
 
     public StunEffect(Skill skill, long duration) {
         super(skill, "Stun", period, duration);
         this.types.add(EffectType.STUN);
         this.types.add(EffectType.HARMFUL);
         this.types.add(EffectType.PHYSICAL);
         this.types.add(EffectType.DISABLE);
         addMobEffect(9, (int) (duration / 1000) * 20, 127, false);
     }
 
     @Override
     public void apply(Hero hero) {
         super.apply(hero);
         Player player = hero.getPlayer();
        loc = hero.getPlayer().getLocation();
         broadcast(player.getLocation(), stunApplyText, player.getDisplayName());
     }
 
     @Override
     public void remove(Hero hero) {
         super.remove(hero);
         Player player = hero.getPlayer();
         broadcast(player.getLocation(), stunExpireText, player.getDisplayName());
     }
 
     @Override
     public void tick(Hero hero) {
         super.tick(hero);
         Location location = hero.getPlayer().getLocation();
        if (location == null)
            return;
         if (location.getX() != loc.getX() || location.getY() != loc.getY() || location.getZ() != loc.getZ()) {
             loc.setYaw(location.getYaw());
             loc.setPitch(location.getPitch());
             hero.getPlayer().teleport(loc);
         }
     }
 }
