 /**
  * DropHelpers.java
  * Purpose: Handles all entity dropping for the plugin.
  * 
  * @version 1.2.0 11/5/12
  * @author Scott Woodward
  */
 package com.gmail.scottmwoodward.headhunter.helpers;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.entity.Item;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.meta.SkullMeta;
 
 public class DropHelper {
 	/**
 	 * Called when the plugin is enabled upon server startup. Registers all
 	 * events and commands for the plugin
 	 * 
 	 * @param head
 	 *            is the type of head to be dropped
 	 * @param loc
 	 *            is the location to drop the head
 	 * @param name
 	 *            is the player name to be put on a human skull (null if not a
 	 *            human skull)
 	 * @param world
 	 *            is the world for the drop to occur in
 	 */
 	public static void drop(HeadType head, Location loc, String name, World world, Player killer) {
 		int amount = 0;
 
 		// Check for killers weapon of choice. If it is looting, determine level
 		// and then give user that level
 		if (ConfigHelper.getLooting() == true) {
 			ItemStack itemKill = killer.getItemInHand();
 			if (itemKill != null) {
 				if (itemKill.containsEnchantment(Enchantment.LOOT_BONUS_MOBS)) {
 					int totalLevelOfEnchantments = itemKill.getEnchantmentLevel(Enchantment.LOOT_BONUS_MOBS);
 					for (int i = 0; i < totalLevelOfEnchantments; i++) {
 						if (shouldDrop(head)) {
 							amount++;
 						}
 					}
 				}
 			}
 		}
 		// Check for normally applied drop.
 		if (shouldDrop(head))
 			amount++;
 		if (amount != 0) {
 			ItemStack itemStack = new ItemStack(Material.SKULL_ITEM, amount, (short) head.getValue());
			Item drop = world.dropItemNaturally(loc, itemStack);
 			if (name != null) {
 				drop.setItemStack(setSkin(new ItemStack(Material.SKULL_ITEM, 1, (byte) 3), name));
 			}
 		}
 	}
 
 	private static boolean shouldDrop(HeadType head) {
 		double fraction = Math.random();
 		double chance = 0;
 		if (head.getValue() == 0 && ConfigHelper.getBoolean("Skeleton")) {
 			chance = ConfigHelper.getDropChance("Skeleton");
 		} else if (head.getValue() == 1 && ConfigHelper.getBoolean("WitherSkeleton")) {
 			chance = ConfigHelper.getDropChance("WitherSkeleton");
 		} else if (head.getValue() == 2 && ConfigHelper.getBoolean("Zombie")) {
 			chance = ConfigHelper.getDropChance("Zombie");
 		} else if (head.getValue() == 3 && ConfigHelper.getBoolean("Player")) {
 			chance = ConfigHelper.getDropChance("Player");
 		} else if (head.getValue() == 4 && ConfigHelper.getBoolean("Creeper")) {
 			chance = ConfigHelper.getDropChance("Creeper");
 		}
 		if (chance >= 100) {
 			return true;
 		} else if (chance <= 0) {
 			return false;
 		} else {
 			return ((fraction * 100) <= chance);
 		}
 	}
 
 	private static ItemStack setSkin(ItemStack item, String nick) {
 		SkullMeta meta = (SkullMeta) item.getItemMeta();
 		meta.setOwner(nick);
 		item.setItemMeta(meta);
 		return item;
 	}
 }
