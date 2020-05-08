 package com.herocraftonline.dev.heroes.skill.skills;
 
 import java.util.HashSet;
 import java.util.List;
 
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
import org.bukkit.material.Button;
import org.bukkit.material.Lever;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.persistence.Hero;
 import com.herocraftonline.dev.heroes.skill.ActiveSkill;
 
 public class SkillTelekinesis extends ActiveSkill {
 
     public SkillTelekinesis(Heroes plugin) {
         super(plugin, "Telekinesis");
         setDescription("Activate levers, buttons and other interactable objects from afar!");
         setUsage("/skill telekinesis");
         setArgumentRange(0, 0);
         setIdentifiers(new String[]{"skill telekinesis"});
     }
 
     @Override
     public boolean use(Hero hero, String[] args) {
         Player player = hero.getPlayer();
         HashSet<Byte> transparent = new HashSet<Byte>();
         transparent.add((byte) Material.AIR.getId());
         transparent.add((byte) Material.WATER.getId());
         List<Block> lineOfSight = player.getLineOfSight(transparent, 15);
         Block block = lineOfSight.get(lineOfSight.size() - 1);
        if (block.getType() == Material.LEVER) {
            Lever lever = (Lever) block.getState().getData();
            lever.setPowered(!lever.isPowered());
            block.getState().update();
            broadcastExecuteText(hero);
        } else if (block.getType() == Material.STONE_BUTTON) {
            Button button = (Button) block.getState().getData();
            button.setPowered(!button.isPowered());
            block.getState().update();
             broadcastExecuteText(hero);
        } else {
            return false;
        }
        return true;
     }
 
 }
