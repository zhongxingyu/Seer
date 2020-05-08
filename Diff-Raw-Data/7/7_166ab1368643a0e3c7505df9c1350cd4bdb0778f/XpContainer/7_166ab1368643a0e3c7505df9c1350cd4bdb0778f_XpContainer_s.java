 /**
  * Small plugin to enable the storage of experience points in an item.
  *
  * Fork of the original PearlXP created by Nebual of nebtown.info in March 2012.
  *
  * Fork by: Marex, Zonta.
  *
  * contact us at : plugins@makeitonthe.net
  *
  * Copyright (C) 2012 belongs to their respective owners
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package net.makeitonthe.GemXp;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.meta.ItemMeta;
 
 public class XpContainer extends ItemStack {
 
 	/**
 	 * Maximum storage capacity of a item
 	 */
 	public static int MAX_STORAGE = 32767; // Max of a short
 	/**
 	 * Maximum possible tax
 	 */
 	public static double MAX_TAX = 99;
 
 	/**
 	 * Display name text format
 	 */
 	public static String DISPLAY_NAME_FORMAT = "of"; // remove italic + white
 
 	/**
 	 * Lore text format
 	 */
 	public static String LORE_FORMAT = "r7"; // reset format + gray
 
 	private int itemId;
 	private int imbuedItemId;
 	private int maxExp;
 
 	private String itemName;
 	private String itemHint;
 
 	private double xpTax;
 	private int stackSize;
 
 	private ItemStack itemStack;
 
 	public XpContainer(ItemStack stack) {
 		super(stack);
 		this.itemStack = stack;
 	}
 
 	public XpContainer(int itemId, int imbuedItemId, String itemName,
 			String itemHint, double xpTax, int maxExp, int stackSize) {
 
 		this.itemId = itemId;
 		this.imbuedItemId = imbuedItemId;
 		this.maxExp = setMaxExp(maxExp);
 
 		this.itemName = itemName;
 		this.itemHint = itemHint;
 
 		this.xpTax = xpTax;
 		this.stackSize = stackSize;
 
 		this.itemStack = null;
 	}
 
 	public XpContainer(ItemStack stack, int itemId, int imbuedItemId, String itemName,
 			String itemHint, double xpTax, int maxExp, int stackSize) {
 
 		this(stack);
 		this.itemId = itemId;
 		this.imbuedItemId = imbuedItemId;
 		this.maxExp = setMaxExp(maxExp);
 
 		this.itemName = itemName;
 		this.itemHint = itemHint;
 
 		this.xpTax = xpTax;
 		this.stackSize = stackSize;
 	}
 
 	public XpContainer(ItemStack stack, int itemId, int imbuedItemId, String itemName,
 			String itemHint, double xpTax, int maxExp, int stackSize, int amount) {
 
 		this(stack);
 		this.itemId = itemId;
 		this.imbuedItemId = imbuedItemId;
 		this.maxExp = setMaxExp(maxExp);
 
 		this.itemName = itemName;
 		this.itemHint = itemHint;
 
 		this.xpTax = xpTax;
 		this.stackSize = stackSize;
 
 		setAmount(amount);
 	}
 
 
 
 
 	/**
 	 * Return true if the ItemStack has the capability of storing experience points
 	 * @return true if it can store XP, false otherwise
 	 */
 	public boolean isAnXpContainer(ItemStack stack) {
 		int itemId = 0;
 		boolean container = false;
 
 		if (stack != null) {
 			itemId = stack.getTypeId();
 			container = itemId == imbuedItemId || itemId == this.itemId;
 		}
 
 		return container;
 	}
 
 	/**
 	 * Return true if the ItemStack contains experience points
 	 * @return true if it contain XP, false otherwise
 	 */
 	public boolean isAFilledXpContainer(ItemStack stack) {
 		boolean container = false;
 
 		if (stack != null) {
 			container = (stack.getTypeId() == imbuedItemId && stack.getDurability() > 0);
 		}
 
 		return container;
 	}
 
 	/**
 	 * Return true if the ItemStack has the capability of containing experience points
 	 * @return true if it can contain XP, false otherwise
 	 */
 	public boolean canContainXp() {
 		return getTypeId() == imbuedItemId;
 	}
 
 	/**
 	 * Return true if the ItemStack has the capability of storing experience points
 	 * @return true if it can store XP, false otherwise
 	 */
 	public boolean canStoreXp() {
 		return getTypeId() == itemId;
 	}
 
 
 	/* (non-Javadoc)
 	 * @see org.bukkit.inventory.ItemStack#clone()
 	 */
 	@Override
 	public XpContainer clone() {
 		return new XpContainer( new ItemStack(itemStack), itemId, imbuedItemId, itemName, itemHint, xpTax, maxExp, stackSize );
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.bukkit.inventory.ItemStack#getAmount()
 	 */
 	@Override
 	public int getAmount() {
 		return itemStack.getAmount();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.bukkit.inventory.ItemStack#setAmount(int)
 	 */
 	@Override
 	public void setAmount(int i) {
 		itemStack.setAmount(i);
 	}
 
 	/**
 	 * Return the amount of experience points stored in the item
 	 * @param item
 	 * @return xp experience points
 	 */
 	public int getStoredXp() {
 
 		return getDurability();
 	}
 
 	/**
 	 * Set the amount of stored experience points in the itemstack to xp (mutable)
 	 * @param xp new stored experience points
 	 * @param item ItemStack getting modified
 	 */
 	public void setStoredXp(int xp) {
 		// check for overflow
		if (xp > getmaxExp()) {
			xp = getmaxExp();
 		}
 
 		// Change appearance
 		if (xp == 0) {
 			resetContainer();
 		} else {
 			initContainer(xp);
 		}
 
 		// Change the stored xp
 		setDurability((short) xp);
 	}
 
 
 	/**
 	 * @return the maximum level cap of the containers
 	 */
	public int getmaxExp() {
 		return maxExp;
 	}
 
 
 	/**
 	 * @return the xpTax
 	 */
 	public double getXpTax() {
 		return xpTax;
 	}
 
 
 	/**
 	 * @return the stackSize
 	 */
 	@Override
 	public int getMaxStackSize() {
 		int max;
 		if (canContainXp() && getStoredXp() > 0) {
 			//filled gems
 			max = stackSize;
 		} else {
 			//empty gems
 			max = getType().getMaxStackSize();
 		}
 		return max;
 	}
 
 	/**
 	 * @param maxExp the maximum experience points cap  to set
 	 */
 	private static int setMaxExp(int maxExp) {
 		// check if maxLevel fits in a short (2^15 - 1)
 		if (maxExp > MAX_STORAGE)
 			return MAX_STORAGE;
 		else
 			return maxExp;
 	}
 
 	/**
 	 * Prepare the container by changing is typeid, display name and adding a
 	 * new lore indicating the stored experience points
 	 *
 	 * @param xp experience points stored in the container
 	 */
 	private void initContainer(int xp) {
 		ItemMeta itemMeta = this.getItemMeta();
 		List<String> lores = itemMeta.getLore();
 		String lore = LORE_FORMAT + itemHint + " " + xp + "xp";
 
 		// Change appearance and display name
 		setTypeId(imbuedItemId);
 		itemMeta.setDisplayName(DISPLAY_NAME_FORMAT + itemName);
 
 		// Add description
 		if (lores != null) {
 			lores.add(lore);
 		} else {
 			// If the list does'nt exist we create it
 			lores = new LinkedList<String>();
 			lores.add(lore);
 		}
 
 		itemMeta.setLore(lores);
 		this.setItemMeta(itemMeta);
 	}
 
 	/**
 	 * Reset the appearance of the container
 	 */
 	private void resetContainer() {
 		ItemMeta itemMeta = this.getItemMeta();
 
 		// Reset hints
 		itemMeta.setDisplayName(null);
 		itemMeta.setLore(null);
 
 		this.setItemMeta(itemMeta);
 
 		// Change appearance
 		setTypeId(itemId);
 	}
 
 }
