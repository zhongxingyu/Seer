 package com.herocraftonline.dev.heroes.skill.skills;
 
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event.Type;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockListener;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.util.config.ConfigurationNode;
 
 import com.herocraftonline.dev.heroes.HBlockListener;
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.effects.EffectType;
 import com.herocraftonline.dev.heroes.hero.Hero;
 import com.herocraftonline.dev.heroes.skill.PassiveSkill;
 import com.herocraftonline.dev.heroes.skill.SkillType;
 import com.herocraftonline.dev.heroes.util.Util;
 
 public class SkillMining extends PassiveSkill {
 
     public SkillMining(Heroes plugin) {
         super(plugin, "Mining");
         setDescription("You understand mining and ores!");
         setEffectTypes(EffectType.BENEFICIAL);
         setTypes(SkillType.KNOWLEDGE, SkillType.EARTH, SkillType.BUFF);
         
         registerEvent(Type.BLOCK_BREAK, new SkillBlockListener(), Priority.Monitor);
     }
 
     @Override
     public ConfigurationNode getDefaultConfig() {
         ConfigurationNode node = super.getDefaultConfig();
         node.setProperty("chance-per-level", .01);
         node.setProperty("chance-from-stone", .0005);
         return node;
     }
 
     public class SkillBlockListener extends BlockListener {
 
         @Override
         public void onBlockBreak(BlockBreakEvent event) {
             if (event.isCancelled())
                 return;
 
             Block block = event.getBlock();
             if (HBlockListener.placedBlocks.containsKey(block.getLocation()))
                 return;
 
             Material dropMaterial = null;
             boolean isStone = false;
             switch (block.getType()) {
                 case DIAMOND_ORE:
                 case COAL_ORE:
                 case IRON_ORE:
                 case GOLD_ORE:
                     dropMaterial = block.getType();
                     break;
                 case REDSTONE_ORE:
                     dropMaterial = Material.REDSTONE;
                 case LAPIS_BLOCK:
                     dropMaterial = Material.INK_SACK;
                     break;
                 case STONE:
                     isStone = true;
                     break;
                 default:
                     return;
             }
 
             Hero hero = plugin.getHeroManager().getHero(event.getPlayer());
            if (!hero.hasEffect("Mining") || dropMaterial == null)
                 return;
 
            if (Util.rand.nextDouble() >= getSetting(hero.getHeroClass(), "chance-from-stone", .0005) * hero.getLevel())
                return;
            
            if (isStone) {
                 block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(getMatFromHeight(block), 1));
                 return;
             } else if (dropMaterial == Material.INK_SACK) {
                 block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(dropMaterial, 1, (short) 0, (byte) 4));
             } else {
                 block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(dropMaterial, 1));
             }
         }
 
         private Material getMatFromHeight(Block block) {
             int y = block.getY();
 
             if (y < 20)
                 return Material.DIAMOND;
             else if (y < 40)
                 return Material.GOLD_ORE;
             else if (y < 60)
                 return Material.IRON_ORE;
             else
                 return Material.COAL_ORE;
         }
     }
 }
