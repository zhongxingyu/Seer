 package com.herocraftonline.dev.heroes.skill.skills;
 
 import org.bukkit.Material;
 import org.bukkit.TreeType;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
import org.bukkit.util.Callback;
 import org.bukkit.util.config.ConfigurationNode;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.hero.Hero;
 import com.herocraftonline.dev.heroes.skill.ActiveSkill;
 import com.herocraftonline.dev.heroes.skill.SkillType;
 import com.herocraftonline.dev.heroes.util.Messaging;
 import com.herocraftonline.dev.heroes.util.Setting;
 import com.herocraftonline.dev.heroes.util.Util;
 
 public class SkillOvergrowth extends ActiveSkill {
 
     public SkillOvergrowth(Heroes plugin) {
         super(plugin, "Overgrowth");
         setDescription("Turns a sapling into a full grown tree");
         setUsage("/skill overgrowth");
         setArgumentRange(0, 0);
         setIdentifiers("skill overgrowth", "skill ogrowth");
         setTypes(SkillType.SILENCABLE, SkillType.EARTH);
     }
 
     @Override
     public ConfigurationNode getDefaultConfig() {
         ConfigurationNode node = super.getDefaultConfig();
         node.setProperty(Setting.MAX_DISTANCE.node(), 15);
         return node;
     }
 
     @Override
     public boolean use(Hero hero, String[] args) {
         Player player = hero.getPlayer();
         int range = getSetting(hero.getHeroClass(), Setting.MAX_DISTANCE.node(), 15);
        if (player.getTargetBlock((Callback<Boolean, Block>) null, range).getType() == Material.SAPLING) {
            Block targetBlock = player.getTargetBlock((Callback<Boolean, Block>) null, range);
             TreeType tType = null;
 
             switch (targetBlock.getData()) {
                 case 0x0:
                     if (Util.rand.nextInt(2) == 0) {
                         tType = TreeType.TREE;
                     } else {
                         tType = TreeType.BIG_TREE;
                     }
                     break;
                 case 0x1:
                     if (Util.rand.nextInt(2) == 0) {
                         tType = TreeType.REDWOOD;
                     } else {
                         tType = TreeType.TALL_REDWOOD;
                     }
                     break;
                 case 0x2:
                     tType = TreeType.BIRCH;
                     break;
                 default:
                     tType = TreeType.TREE;
             }
             Material sapling = targetBlock.getType();
             byte data = targetBlock.getData();
             targetBlock.setType(Material.AIR);
             if (!player.getWorld().generateTree(targetBlock.getLocation(), tType)) {
                 targetBlock.setType(sapling);
                 targetBlock.setData(data);
                 Messaging.send(player, "The spell fizzled!");
                 return false;
             }
             broadcastExecuteText(hero);
             return true;
         } else {
             Messaging.send(player, "Target is not a sapling!");
             return false;
         }
     }
 
 }
