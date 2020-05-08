 package com.herocraftonline.dev.heroes.command.skill.skills;
 
 import java.util.HashMap;
 
 import org.bukkit.Material;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.command.skill.TargettedSkill;
 import com.herocraftonline.dev.heroes.persistence.Hero;
 
 public class SkillBandages extends TargettedSkill {
     public HashMap<Player, Integer> playerSchedulers = new HashMap<Player, Integer>();
 
     public SkillBandages(Heroes plugin) {
         super(plugin);
         name = "Bandage";
         description = "Skill - Bandage";
         usage = "/cast bandage";
         minArgs = 0;
         maxArgs = 0;
         identifiers.add("cast bandage");
 
     }
 
     @Override
     public boolean use(Hero hero, LivingEntity target, String[] args) {
         Player player = hero.getPlayer();
         if(target instanceof Player){
             final Player tPlayer = (Player) target;
             if (!player.getItemInHand().equals(Material.PAPER)) {
                 plugin.getMessager().send(player, "You need paper to perform this");
                 return false;
             }
 
             playerSchedulers.put(tPlayer, plugin.getServer().getScheduler().scheduleAsyncRepeatingTask(plugin, new Runnable() {
                 public int timesRan = 0;
 
                 @Override
                 public void run() {
                     if (timesRan == 10) {
                         playerSchedulers.remove(tPlayer);
                        plugin.getServer().getScheduler().cancelTask(playerSchedulers.get(tPlayer));
                     } else {
                         timesRan++;
                         tPlayer.setHealth(tPlayer.getHealth() + 1);
                     }
                 }
             }, 20L, 20L));
 
             return true;
         }
     }
 }
