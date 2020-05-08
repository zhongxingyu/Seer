 package net.amunak.bukkit.plugin_DropsToInventory;
 
 /**
  * Copyright 2013 Jiří Barouš
  *
  * This program is free software: you can redistribute it and/or modify it under
  * the terms of the GNU General Public License as published by the Free Software
  * Foundation, either version 3 of the License, or (at your option) any later
  * version.
  *
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU General Public License along with
  * this program. If not, see <http://www.gnu.org/licenses/>.
  */
 import java.util.ArrayList;
 import java.util.List;
 import org.bukkit.GameMode;
 import org.bukkit.Material;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.inventory.ItemStack;
 
 /**
  * block break event listener
  *
  * @author Amunak
  */
 public final class BlockBreakEventListener implements Listener {
 
     protected static Log log;
     public List<String> blockFilter;
     public List<String> safeBlocks;
     private List<Material> durabilityFixAppliedItems = new ArrayList<Material>();
     private List<Material> durabilityFixSwords = new ArrayList<Material>();
     public DropsToInventory plugin;
     public Integer filterMode;
     public Boolean useSafeBlocks;
     public Boolean fixEnchantmentBug;
     public Boolean fixItemDurability;
 
     public BlockBreakEventListener(DropsToInventory p) {
         plugin = p;
         log = new Log(plugin);
 //        log.raiseFineLevel = true;
         log.fine("registering BlockBreakEventListener");
 
         filterMode = BlockFilter.fromString(plugin.config.getString("options.blocks.filterMode"));
         useSafeBlocks = plugin.config.getBoolean("options.blocks.useOnlySafeBlocks");
         fixEnchantmentBug = !plugin.config.getBoolean("options.blocks.ignoreEnchantmentBug");
         fixItemDurability = plugin.config.getBoolean("options.blocks.fixItemDurability");
 
         if (!filterMode.equals(BlockFilter.NONE)) {
             blockFilter = plugin.config.getStringList("lists.blockFilter");
         }
         if (useSafeBlocks) {
             safeBlocks = plugin.config.getStringList("lists.safeBlocks");
         }
         Common.fixEnumLists(blockFilter, safeBlocks);
 
         if (fixItemDurability) {
             log.fine("we will try to fix item durability bug");
             durabilityFixAppliedItems.add(Material.WOOD_SWORD);
             durabilityFixAppliedItems.add(Material.WOOD_PICKAXE);
             durabilityFixAppliedItems.add(Material.WOOD_AXE);
             durabilityFixAppliedItems.add(Material.WOOD_SPADE);
             durabilityFixAppliedItems.add(Material.STONE_SWORD);
             durabilityFixAppliedItems.add(Material.STONE_PICKAXE);
             durabilityFixAppliedItems.add(Material.STONE_AXE);
             durabilityFixAppliedItems.add(Material.STONE_SPADE);
             durabilityFixAppliedItems.add(Material.IRON_SWORD);
             durabilityFixAppliedItems.add(Material.IRON_PICKAXE);
             durabilityFixAppliedItems.add(Material.IRON_AXE);
             durabilityFixAppliedItems.add(Material.IRON_SPADE);
             durabilityFixAppliedItems.add(Material.GOLD_SWORD);
             durabilityFixAppliedItems.add(Material.GOLD_PICKAXE);
             durabilityFixAppliedItems.add(Material.GOLD_AXE);
             durabilityFixAppliedItems.add(Material.GOLD_SPADE);
             durabilityFixAppliedItems.add(Material.DIAMOND_SWORD);
             durabilityFixAppliedItems.add(Material.DIAMOND_PICKAXE);
             durabilityFixAppliedItems.add(Material.DIAMOND_AXE);
             durabilityFixAppliedItems.add(Material.DIAMOND_SPADE);
 
             durabilityFixSwords.add(Material.WOOD_SWORD);
             durabilityFixSwords.add(Material.STONE_SWORD);
             durabilityFixSwords.add(Material.IRON_SWORD);
             durabilityFixSwords.add(Material.GOLD_SWORD);
             durabilityFixSwords.add(Material.DIAMOND_SWORD);
         }
 
         log.fine("BlockBreakEventListener registered");
     }
 
     @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
     public void onBlockBreakEvent(BlockBreakEvent event) {
         log.fine(event.getPlayer().getName() + " broke " + event.getBlock().getType());
         if (event.getPlayer().getGameMode().equals(GameMode.SURVIVAL)
                 && (!useSafeBlocks || safeBlocks.contains(event.getBlock().getType().toString()))
                 && (!fixEnchantmentBug || !enchantBugPresent(event))
                 && (BlockFilter.isEligible(event.getBlock().getType(), blockFilter, filterMode))) {
             log.fine("dropping " + event.getBlock().getType() + " to inventory of " + event.getPlayer().getName());
             plugin.moveDropToInventory(event.getPlayer(), event.getBlock().getDrops(event.getPlayer().getItemInHand()), event.getExpToDrop(), event.getBlock().getLocation());
             /* event.getBlock().getDrops().clear(); //bugged
              * event.setExpToDrop(0);
              */
             event.setCancelled(true);
             event.getBlock().setTypeId(Material.AIR.getId());
             recalculateDurability(event.getPlayer().getItemInHand());
         }
     }
 
     private boolean enchantBugPresent(BlockBreakEvent event) {
         List<Enchantment> buggedEnchants = new ArrayList<Enchantment>();
         buggedEnchants.add(Enchantment.LOOT_BONUS_BLOCKS);
         buggedEnchants.add(Enchantment.SILK_TOUCH);
 
         for (Enchantment enchantment : buggedEnchants) {
             if (event.getPlayer().getInventory().getItemInHand().getEnchantmentLevel(enchantment) > 0) {
                 log.fine(event.getPlayer().getName() + " has enchant bug present");
                 return true;
             }
         }
         return false;
     }
 
     /**
      * Changes durability as if the block was broken normally. Durability
      * enchant is applied if necessary. This works only for BLOCK BREAKS with
      * SWORDS, PICKAXES, AXES AND SHOVELS!
      *
      * @param item the item durability is counter on
      */
     private void recalculateDurability(ItemStack item) {
         Integer enchantLevel = item.getEnchantmentLevel(Enchantment.DURABILITY);
         if (fixItemDurability
                 && durabilityFixAppliedItems.contains(item.getType())
                 //counting with durability enchant:
                 && (enchantLevel == 0 || (plugin.random.nextInt(100) <= 100 / (enchantLevel + 1)))) {
             log.fine("trying to fix durability on " + item.getType() + " with durability enchant " + enchantLevel);
             if (durabilityFixSwords.contains(item.getType())) {
                item.setDurability((short) (item.getDurability() + 2));
             } else {
                item.setDurability((short) (item.getDurability() + 1));
             }
         }
     }
 
     /**
      * Holds filter modes and methods to retreive them
      */
     private static class BlockFilter {
 
         public static final Integer NONE = 0;
         public static final Integer BLACKLIST = 1;
         public static final Integer WHITELIST = 2;
 
         public static String toString(Integer i) {
             if (i.equals(BLACKLIST)) {
                 return "BLACKLIST";
             } else if (i.equals(WHITELIST)) {
                 return "WHITELIST";
             } else {
                 return "NONE";
             }
         }
 
         public static Integer fromString(String s) {
             if (s.equalsIgnoreCase("BLACKLIST")) {
                 return BLACKLIST;
             } else if (s.equalsIgnoreCase("WHITELIST")) {
                 return WHITELIST;
             } else {
                 return NONE;
             }
         }
 
         /**
          * checks whether a block is eligible to be used according to a filter
          * mode
          *
          * @param isInList is block in list
          * @param mode filter mode to check against
          * @return true if elegible, else otherwise
          */
         public static Boolean isEligible(boolean isInList, Integer mode) {
             boolean result;
             result = mode.equals(NONE) || (mode.equals(BLACKLIST) && !isInList) || (mode.equals(WHITELIST) && isInList);
             log.fine("block is eligible: " + result);
             return result;
         }
 
         /**
          * checks a block material against list and if it is eligible to be used
          * according to a filter mode
          *
          * @param mat the material
          * @param list list of materials (strings)
          * @param mode filter mode to check against
          * @return
          */
         public static Boolean isEligible(Material mat, List list, Integer mode) {
             return isEligible(list.contains(mat.toString()), mode);
         }
     }
 }
