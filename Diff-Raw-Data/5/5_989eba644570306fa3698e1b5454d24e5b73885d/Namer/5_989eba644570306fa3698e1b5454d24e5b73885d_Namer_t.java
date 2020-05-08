 package com.stirante.PrettyScaryLib;
 
 import net.minecraft.server.v1_4_5.NBTTagCompound;
 import net.minecraft.server.v1_4_5.NBTTagList;
 import net.minecraft.server.v1_4_5.NBTTagString;
 
 import org.bukkit.ChatColor;
 import org.bukkit.craftbukkit.v1_4_5.inventory.CraftItemStack;
 import org.bukkit.inventory.ItemStack;
 
 /**
  * Class, that allows setting and getting name and lore of item.
  */
 public class Namer {
 	
 	/** craft stack. */
 	private static CraftItemStack							craftStack;
 	
 	/** item stack. */
 	private static net.minecraft.server.v1_4_5.ItemStack	itemStack;
 	
 	/**
 	 * Sets name.
 	 * 
 	 * @param item
 	 *            item
 	 * @param name
 	 *            name
 	 * @return item stack
 	 */
 	public static ItemStack setName(ItemStack item, String name) {
 		if (item instanceof CraftItemStack) {
 			craftStack = (CraftItemStack) item;
 			itemStack = craftStack.getHandle();
 		}
 		else if (item instanceof ItemStack) {
 			craftStack = new CraftItemStack(item);
 			itemStack = craftStack.getHandle();
 		}
 		itemStack.c(ChatColor.RESET + name);
 		return craftStack;
 	}
 	
 	/**
 	 * Gets name.
 	 * 
 	 * @param item
 	 *            item
 	 * @return name
 	 */
 	public static String getName(ItemStack item) {
 		if (item instanceof CraftItemStack) {
 			craftStack = (CraftItemStack) item;
 			Namer.itemStack = craftStack.getHandle();
 		}
 		else if (item instanceof ItemStack) {
 			craftStack = new CraftItemStack(item);
 			Namer.itemStack = craftStack.getHandle();
 		}
 		return itemStack.r();
 	}
 	
 	/**
 	 * Sets lore.
 	 * 
 	 * @param item
 	 *            item
 	 * @param lore
 	 *            lore
 	 * @return item stack
 	 */
 	public ItemStack setLore(ItemStack item, String... lore) {
 		if (item instanceof CraftItemStack) {
 			craftStack = (CraftItemStack) item;
 			Namer.itemStack = craftStack.getHandle();
 		}
 		else if (item instanceof ItemStack) {
 			craftStack = new CraftItemStack(item);
 			Namer.itemStack = craftStack.getHandle();
 		}
 		NBTTagCompound tag = itemStack.tag;
 		if (tag == null) {
 			tag = new NBTTagCompound();
 			tag.setCompound("display", new NBTTagCompound());
 			itemStack.tag = tag;
 		}
 		tag = itemStack.tag.getCompound("display");
 		NBTTagList list = new NBTTagList();
 		for (String l : lore)
			list.add(new NBTTagString("", ChatColor.RESET + l));
 		tag.set("Lore", list);
 		itemStack.tag.setCompound("display", tag);
 		return craftStack;
 	}
 	
 	/**
 	 * Adds lore.
 	 * 
 	 * @param item
 	 *            item
 	 * @param lore
 	 *            lore
 	 * @return item stack
 	 */
 	public static ItemStack addLore(ItemStack item, String lore) {
 		if (item instanceof CraftItemStack) {
 			craftStack = (CraftItemStack) item;
 			Namer.itemStack = craftStack.getHandle();
 		}
 		else if (item instanceof ItemStack) {
 			craftStack = new CraftItemStack(item);
 			Namer.itemStack = craftStack.getHandle();
 		}
 		NBTTagCompound tag = itemStack.tag;
 		if (tag == null) {
 			tag = new NBTTagCompound();
 			tag.setCompound("display", new NBTTagCompound());
 			tag.getCompound("display").set("Lore", new NBTTagList());
 			itemStack.tag = tag;
 		}
 		
 		tag = itemStack.tag.getCompound("display");
 		NBTTagList list = tag.getList("Lore");
		list.add(new NBTTagString("", ChatColor.RESET + lore));
 		tag.set("Lore", list);
 		itemStack.tag.setCompound("display", tag);
 		return craftStack;
 	}
 	
 	/**
 	 * Gets lore.
 	 * 
 	 * @param item
 	 *            item
 	 * @return lore
 	 */
 	public static String[] getLore(ItemStack item) {
 		if (item instanceof CraftItemStack) {
 			craftStack = (CraftItemStack) item;
 			Namer.itemStack = craftStack.getHandle();
 		}
 		else if (item instanceof ItemStack) {
 			craftStack = new CraftItemStack(item);
 			Namer.itemStack = craftStack.getHandle();
 		}
 		NBTTagCompound tag = itemStack.tag;
 		if (tag == null) {
 			tag = new NBTTagCompound();
 			tag.setCompound("display", new NBTTagCompound());
 			tag.getCompound("display").set("Lore", new NBTTagList());
 			itemStack.tag = tag;
 		}
 		tag = itemStack.tag;
 		NBTTagList list = tag.getCompound("display").getList("Lore");
 		String[] lores = new String[list.size()];
 		for (int i = 0; i < list.size(); i++)
 			lores[i] = ((NBTTagString) list.get(i)).data;
 		return lores;
 	}
 }
