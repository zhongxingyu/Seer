 package com.herocraftonline.dev.heroes.skill.skills;
 
 import java.util.List;
 
 import org.bukkit.block.Block;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.persistence.Hero;
 import com.herocraftonline.dev.heroes.skill.ActiveSkill;
 
 public class SkillBlink extends ActiveSkill {
 
     public SkillBlink(Heroes plugin) {
         super(plugin);
         setName("Blink");
         setDescription("Teleports you 4-5 blocks");
         setUsage("/skill blink");
         setMinArgs(0);
         setMaxArgs(0);
         getIdentifiers().add("skill blink");
     }
 
     @Override
     public boolean use(Hero hero, String[] args) {
         List<Block> blocks = hero.getPlayer().getLineOfSight(null, 6);
        float pitch = hero.getPlayer().getLocation().getPitch();
        float yaw = hero.getPlayer().getLocation().getYaw();
         hero.getPlayer().teleport(blocks.get(blocks.size() - 1).getLocation());
        hero.getPlayer().getLocation().setPitch(pitch);
        hero.getPlayer().getLocation().setYaw(yaw);
         return true;
     }
 
 }
