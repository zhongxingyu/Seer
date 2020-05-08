 package com.github.soniex2.endermoney.trading.helper.inventory;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Set;
 import java.util.Map.Entry;
 
 import net.minecraft.inventory.IInventory;
 import net.minecraft.inventory.InventoryBasic;
 import net.minecraft.item.ItemStack;
 
 import com.github.soniex2.endermoney.trading.helper.item.ItemStackMapKey;
 
 // TODO javadocs
 public class InventoryHelper {
 
 	private InventoryHelper() {
 	}
 
 	public static IInventory itemStackArrayToInventory(ItemStack[] array) {
 		return itemStackArrayToInventory(array, 0, array.length - 1);
 	}
 
 	public static IInventory itemStackArrayToInventory(ItemStack[] array, int start, int end) {
 		InventoryBasic inv = new InventoryBasic("", true, end - start + 1);
 		for (int i = start; i <= end; i++) {
 			inv.setInventorySlotContents(i - start, array[i]);
 		}
 		return inv;
 	}
 
 	public static ItemStack[] hashMapToItemStackArray(HashMap<ItemStackMapKey, Integer> map) {
 		ArrayList<ItemStack> list = new ArrayList<ItemStack>();
 		Set<Entry<ItemStackMapKey, Integer>> entrySet = map.entrySet();
 		Iterator<Entry<ItemStackMapKey, Integer>> iterator = entrySet.iterator();
 		while (iterator.hasNext()) {
 			Entry<ItemStackMapKey, Integer> entry = iterator.next();
 			ItemStackMapKey itemData = entry.getKey();
 			ItemStack item = new ItemStack(itemData.itemID, 1, itemData.damage);
 			item.stackTagCompound = itemData.getTag();
 			Integer amount = entry.getValue();
 			if (amount == 0 || amount < 0) {
 				continue;
 			}
 			int stacks = amount / item.getMaxStackSize();
 			int extra = amount % item.getMaxStackSize();
 			ItemStack newItem = item.copy();
 			newItem.stackSize = item.getMaxStackSize();
 			for (int i = 0; i < stacks; i++) {
				list.add(newItem.copy());
 			}
 			if (extra != 0) {
 				newItem = item.copy();
 				newItem.stackSize = extra;
				list.add(newItem.copy());
 			}
 		}
 		return list.toArray(new ItemStack[list.size()]);
 	}
 
 	public static boolean itemStackArrayIntoInventory(IInventory inventory, ItemStack[] array) {
 		return itemStackArrayIntoInventory(inventory, array, 0, inventory.getSizeInventory());
 	}
 
 	public static boolean itemStackArrayIntoInventory(IInventory inventory, ItemStack[] array,
 			int start, int end) {
 		ItemStack[] oldInv = inventoryToItemStackArray(inventory, start, end);
 		ItemStack[] arrayCopy = new ItemStack[array.length];
 		for (int i = 0; i < array.length; i++) {
 			arrayCopy[i] = array[i] == null ? null : array[i].copy();
 		}
 		for (int a = start; a <= end; a++) {
 			ItemStack is = inventory.getStackInSlot(a);
 			for (int b = 0; b < array.length; b++) {
 				if (is != null && array[b] != null && is.isItemEqual(array[b])
 						&& ItemStack.areItemStackTagsEqual(is, array[b])) {
 					if (is.isStackable()) {
 						if (is.stackSize < is.getMaxStackSize()) {
 							if (is.stackSize + array[b].stackSize > is.getMaxStackSize()) {
 								int newStackSize = array[b].stackSize + is.stackSize;
 								if (newStackSize > is.getMaxStackSize()) {
 									newStackSize = newStackSize - is.getMaxStackSize();
 								}
 								array[b].stackSize = newStackSize;
 								is.stackSize = is.getMaxStackSize();
 							} else {
 								is.stackSize = is.stackSize + array[b].stackSize;
 								array[b] = null;
 							}
 						}
 					}
 				} else if (is == null && array[b] != null) {
 					inventory.setInventorySlotContents(a, array[b]);
 					is = inventory.getStackInSlot(a);
 					array[b] = null;
 				}
 				if (array[b] != null && array[b].stackSize <= 0) {
 					array[b] = null;
 				}
 			}
 		}
 		for (int a = 0; a < array.length; a++) {
 			if (array[a] != null) {
 				for (int b = 0; b < oldInv.length; b++) {
 					inventory.setInventorySlotContents(b + start, oldInv[b]);
 				}
 				for (int i = 0; i < array.length; i++) {
 					array[i] = arrayCopy[i];
 				}
 				return false;
 			}
 		}
 		return true;
 	}
 
 	public static ItemStack[] inventoryToItemStackArray(IInventory inventory) {
 		if (inventory == null) return null;
 		return inventoryToItemStackArray(inventory, 0, inventory.getSizeInventory() - 1);
 	}
 
 	public static ItemStack[] inventoryToItemStackArray(IInventory inventory, int start, int end) {
 		if (inventory == null) return null;
 		ItemStack[] array = new ItemStack[end - start + 1];
 		for (int i = start; i <= end; i++) {
 			array[i - start] = inventory.getStackInSlot(i);
 		}
 		return array;
 	}
 
 	public static HashMap<ItemStackMapKey, Integer> inventoryToHashMap(IInventory inventory) {
 		return inventoryToHashMap(inventory, 0, inventory.getSizeInventory() - 1);
 	}
 
 	public static HashMap<ItemStackMapKey, Integer> inventoryToHashMap(IInventory inventory,
 			int startSlot, int endSlot) {
 		HashMap<ItemStackMapKey, Integer> map = new HashMap<ItemStackMapKey, Integer>();
 		for (int i = startSlot; i <= endSlot; i++) {
 			ItemStack is = inventory.getStackInSlot(i);
 			if (is == null) {
 				continue;
 			}
 			ItemStackMapKey index = new ItemStackMapKey(is);
 			if (map.containsKey(index)) {
 				map.put(index, is.stackSize + map.get(index));
 			} else {
 				map.put(index, is.stackSize);
 			}
 		}
 		return map;
 	}
 
 	/**
 	 * Inserts a {@link HashMap<ItemStackMapKey,Integer>} into an
 	 * {@link IInventory}.
 	 * Please note that, in case of failure, this method resets the IInventory
 	 * to its original state. Also note that the map stays unchanged.
 	 * 
 	 * @param inventory
 	 *            the IInventory to insert into
 	 * @param map
 	 *            the map to insert from
 	 * @return true if the insert was successful or false if it ran out of space
 	 */
 	public static boolean hashMapIntoInventory(IInventory inventory,
 			HashMap<ItemStackMapKey, Integer> map) {
 		return hashMapIntoInventory(inventory, map, 0, inventory.getSizeInventory() - 1);
 	}
 
 	/**
 	 * Inserts a {@link HashMap<ItemStackMapKey,Integer>} into an
 	 * {@link IInventory}.
 	 * Please note that, in case of failure, this method resets the IInventory
 	 * to its original state. Also note that the map stays unchanged.
 	 * 
 	 * @param inventory
 	 *            the IInventory to insert into
 	 * @param map
 	 *            the map to insert from
 	 * @param start
 	 *            the inventory slot to start at
 	 * @param end
 	 *            the inventory slot to stop at
 	 * @return true if the insert was successful or false if it ran out of space
 	 */
 	public static boolean hashMapIntoInventory(IInventory inventory,
 			HashMap<ItemStackMapKey, Integer> map, int start, int end) {
 		return itemStackArrayIntoInventory(inventory, hashMapToItemStackArray(map), start, end);
 	}
 
 	public static boolean removeFromHashMap(HashMap<ItemStackMapKey, Integer> removeFrom,
 			HashMap<ItemStackMapKey, Integer> toRemove) {
 		@SuppressWarnings("unchecked")
 		HashMap<ItemStackMapKey, Integer> backup = (HashMap<ItemStackMapKey, Integer>) removeFrom
 				.clone();
 		Set<Entry<ItemStackMapKey, Integer>> set = toRemove.entrySet();
 		Iterator<Entry<ItemStackMapKey, Integer>> i = set.iterator();
 		while (i.hasNext()) {
 			Entry<ItemStackMapKey, Integer> entry = i.next();
 			ItemStackMapKey item = entry.getKey();
 			Integer amount = entry.getValue();
 			Integer available = removeFrom.get(item);
 			// Compare
 			if (available == null) {
 				removeFrom.putAll(backup);
 				return false;
 			}
 			// Compare
 			if (available < amount) {
 				removeFrom.putAll(backup);
 				return false;
 			}
 			if (available - amount == 0) {
 				removeFrom.remove(item);
 				continue;
 			}
 			// Overwrite
 			removeFrom.put(item, available - amount);
 		}
 		return true;
 	}
 
 	public static void addToHashMap(HashMap<ItemStackMapKey, Integer> addTo,
 			HashMap<ItemStackMapKey, Integer> toAdd) {
 		Set<Entry<ItemStackMapKey, Integer>> set = toAdd.entrySet();
 		Iterator<Entry<ItemStackMapKey, Integer>> i = set.iterator();
 		while (i.hasNext()) {
 			Entry<ItemStackMapKey, Integer> entry = i.next();
 			ItemStackMapKey item = entry.getKey();
 			Integer amount = entry.getValue();
 			if (addTo.containsKey(item)) {
 				addTo.put(item, amount + addTo.get(item));
 			} else {
 				addTo.put(item, amount);
 			}
 		}
 	}
 
 	public static boolean canRemoveFromHashMap(HashMap<ItemStackMapKey, Integer> removeFrom,
 			HashMap<ItemStackMapKey, Integer> toRemove) {
 		Set<Entry<ItemStackMapKey, Integer>> itemsRequired = toRemove.entrySet();
 		Iterator<Entry<ItemStackMapKey, Integer>> i = itemsRequired.iterator();
 		while (i.hasNext()) {
 			Entry<ItemStackMapKey, Integer> entry = i.next();
 			ItemStackMapKey item = entry.getKey();
 			Integer amount = entry.getValue();
 			Integer available = removeFrom.get(item);
 			if (available == null) {
 				return false;
 			}
 			if (available < amount) {
 				return false;
 			}
 		}
 		return true;
 	}
 }
