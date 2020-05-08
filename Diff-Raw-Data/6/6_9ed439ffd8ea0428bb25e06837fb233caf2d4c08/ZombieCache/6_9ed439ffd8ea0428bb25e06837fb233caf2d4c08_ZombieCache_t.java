 package com.runetooncraft.plugins.EasyMobArmory.egghandler;
 
 import org.bukkit.entity.Entity;
 import org.bukkit.inventory.ItemStack;
 
 import com.runetooncraft.plugins.EasyMobArmory.core.Messenger;
 
 public class ZombieCache {
 	ItemStack[] Equip = null;
 	ItemStack handitem = null;
 	Boolean isbaby = null;
 	ZombieCache(ItemStack[] equip, ItemStack handitem, Boolean isbaby) {
 		Messenger.info("DEBUG! " + isbaby);
		this.isbaby = isbaby;
		this.Equip = equip;
		this.handitem = handitem;
 	}
 }
