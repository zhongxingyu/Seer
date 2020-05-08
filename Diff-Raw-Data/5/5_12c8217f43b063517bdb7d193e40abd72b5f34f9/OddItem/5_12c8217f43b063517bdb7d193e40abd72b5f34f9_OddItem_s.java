 /* This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package info.somethingodd.OddItem;
 
 import info.somethingodd.OddItem.configuration.OddItemAliases;
 import info.somethingodd.OddItem.configuration.OddItemGroup;
 import info.somethingodd.OddItem.configuration.OddItemGroups;
 import info.somethingodd.OddItem.util.AlphanumComparator;
 import info.somethingodd.OddItem.util.ItemStackComparator;
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 /**
  * @author Gordon Pettey (petteyg359@gmail.com)
  */
 public class OddItem {
     protected static OddItemAliases items;
     protected static OddItemGroups groups;
     public static final AlphanumComparator ALPHANUM_COMPARATOR;
     public static final ItemStackComparator ITEM_STACK_COMPARATOR;
 
     static {
         ALPHANUM_COMPARATOR = new AlphanumComparator();
         ITEM_STACK_COMPARATOR = new ItemStackComparator();
     }
 
     @Deprecated
     public static OddItemAliases getItems() {
         return items;
     }
 
     @Deprecated
     public static OddItemGroups getGroups() {
         return groups;
     }
 
     protected static void clear() {
         items = null;
         groups = null;
     }
 
     /**
      * Compares two Inventory for identical contents ignoring order
      *
      * @param a          first Inventory
      * @param b          second Inventory
      * @return Inventories are identical
      */
     public static boolean compare(Inventory a, Inventory b) {
         return compare(a, b, true);
     }
 
     /**
      * Compares two Inventory for identical contents ignoring order, and possibly ignoring quantity
      *
      * @param a          first Inventory
      * @param b          second Inventory
      * @param quantity   whether to check quantity
      * @return Inventories are identical
      */
     public static boolean compare(Inventory a, Inventory b, boolean quantity) {
         return compare(a, b, true, quantity);
     }
 
     /**
      * Compares two Inventory for identical contents ignoring order, and possibly ignoring durability and quantity
      *
      * @param a          first Inventory
      * @param b          second Inventory
      * @param durability whether to check durability
      * @param quantity   whether to check quantity
      * @return Inventories are identical
      */
     public static boolean compare(Inventory a, Inventory b, boolean durability, boolean quantity) {
         List<ItemStack> ia = new ArrayList<ItemStack>();
         List<ItemStack> ib = new ArrayList<ItemStack>();
         for (int i = 0; i < a.getSize(); i++) {
             ia.add(a.getItem(i));
         }
         for (int i = 0; i < b.getSize(); i++) {
             ib.add(b.getItem(i));
         }
         for (int i = 0; i < ia.size(); i++) {
             boolean inequal = true;
             for (int j = 0; j < ib.size(); j++) {
                 if (compare(ia.get(i), ib.get(j), durability, quantity)) {
                     inequal = false;
                     ia.remove(i);
                     ib.remove(j);
                     break;
                 }
             }
             if (inequal) return false;
         }
         return true;
     }
 
     /**
      * Compares two ItemStack material and durability, ignoring quantity
      *
      * @param a ItemStack to compare
      * @param b ItemStack to compare
      * @return ItemStack are equal
      */
     public static boolean compare(ItemStack a, ItemStack b) {
         return compare(a, b, false);
     }
 
     /**
      * Compares two ItemStack material, durability, and quantity
      *
      * @param a        ItemStack to compare
      * @param b        ItemStack to compare
      * @param quantity whether to compare quantity
      * @return ItemStack are equal
      */
     public static boolean compare(ItemStack a, ItemStack b, boolean quantity) {
         return compare(a, b, true, quantity);
     }
 
     /**
      * Compares two ItemStack
      *
      * @param a          ItemStack to compare
      * @param b          ItemStack to compare
      * @param quantity   whether to compare quantity
      * @param durability whether to compare durability
      * @return ItemStack are equal
      */
     public static boolean compare(ItemStack a, ItemStack b, boolean durability, boolean quantity) {
         return compare(a, b, durability, quantity, false);
     }
 
     /**
      * Compares two ItemStack
      *
      * @param a          ItemStack to compare
      * @param b          ItemStack to compare
      * @param quantity   whether to compare quantity
      * @param durability whether to compare durability
      * @param enchantment whether to compare enchantment
      * @return ItemStack are equal
      */
     public static boolean compare(ItemStack a, ItemStack b, boolean durability, boolean quantity, boolean enchantment) {
         Boolean ret = a.getTypeId() == b.getTypeId();
         if (durability) ret &= (a.getDurability() == b.getDurability());
         if (ret && quantity) ret &= (a.getAmount() == b.getAmount());
         if (ret && enchantment) ret &= (a.getEnchantments().equals(b.getEnchantments()));
         return ret;
     }
 
 
 
 
     /**
      * Returns whether player's inventory contains itemStack
      * @param player Player to use inventory
      * @param itemStack ItemStack to look for
      * @return itemStack is contained in inventory
      */
     public static boolean contains(Player player, ItemStack itemStack) {
         return contains(player, itemStack, true);
     }
 
     /**
      * Returns whether inventory contains itemStack, possibly ignoring durability and quantity
      *
      * @param inventory inventory to look in
      * @param itemStack ItemStack to look for
      * @return itemStack is contained in inventory
      */
     public static boolean contains(Inventory inventory, ItemStack itemStack) {
         return contains(inventory, itemStack, true);
     }
 
     /**
      * Returns whether player's inventory contains itemStack, possibly ignoring quantity
      * @param player Player to use inventory
      * @param itemStack ItemStack to look for
      * @param quantity whether to check quantity
      * @return itemStack is contained in inventory
      */
     public static boolean contains(Player player, ItemStack itemStack, boolean quantity) {
         return contains(player, itemStack, true, quantity);
     }
 
     /**
      * Returns whether inventory contains itemStack, possibly ignoring durability and quantity
      * @param inventory inventory to look in
      * @param itemStack ItemStack to look for
      * @param quantity whether to check quantity
      * @return itemStack is contained in inventory
      */
     public static boolean contains(Inventory inventory, ItemStack itemStack, boolean quantity) {
         return contains(inventory, itemStack, true, quantity);
     }
 
     /**
      * Returns whether player's inventory contains itemStack, possibly ignoring durability and quantity
      * @param player Player to use inventory
      * @param itemStack ItemStack to look for
      * @param durability whether to check durability
      * @param quantity whether to check quantity
      * @return itemStack is contained in inventory
      */
     public static boolean contains(Player player, ItemStack itemStack, boolean durability, boolean quantity) {
         return contains(player.getInventory(), itemStack, durability, quantity);
     }
 
     /**
      * Returns whether inventory contains itemStack, possibly ignoring durability and quantity
      *
      * @param inventory  inventory to look in
      * @param itemStack  ItemStack to look for
      * @param durability whether to check durability
      * @param quantity   whether to check quantity
      * @return itemStack is contained in inventory
      */
     public static boolean contains(Inventory inventory, ItemStack itemStack, boolean durability, boolean quantity) {
         return contains(inventory, itemStack, durability, quantity, true);
     }
 
     /**
      * Returns whether inventory contains itemStack, possibly ignoring durability, quantity, and enchantment
      *
      * @param inventory  inventory to look in
      * @param itemStack  ItemStack to look for
      * @param durability whether to check durability
      * @param quantity   whether to check quantity
      * @param enchantment whether to check enchantment
      * @return itemStack is contained in inventory
      */
     public static boolean contains(Inventory inventory, ItemStack itemStack, boolean durability, boolean quantity, boolean enchantment) {
         return contains(inventory, itemStack, durability, quantity, enchantment, true);
     }
 
     /**
      * Returns whether inventory contains itemStack, possibly ignoring durability, quantity, and enchantment, and allows checking for "at least"
      * @param inventory inventory to look in
      * @param itemStack ItemStack to look for
      * @param durability whether to check durability
      * @param quantity whether to check quantity
      * @param enchantment whether to check enchantment
      * @param exact whether to check exact quantity, or only "at least"
      * @return itemStack is contained in inventory
      */
     public static boolean contains(Inventory inventory, ItemStack itemStack, boolean durability, boolean quantity, boolean enchantment, boolean exact) {
         ItemStack[] contents = inventory.getContents();
         if (exact) {
             for (int i = 0; i < contents.length; i++)
                 if (compare(contents[i], itemStack, durability, quantity, enchantment)) return true;
         } else {
             int amount = itemStack.getAmount();
             for (int i = 0; i < contents.length; i++)
                 if (compare(contents[i], itemStack, durability, quantity, enchantment)) amount -= contents[i].getAmount();
             return amount <= 0;
         }
         return false;
     }
 
     /**
      * Gets all aliases for the item represented by an ItemStack
      *
      * @param itemStack the ItemStack to use
      * @return List of aliases
      */
     public static Collection<String> getAliases(ItemStack itemStack) {
         return items.getAliases().get(itemStack);
     }
 
     /**
      * Gets all aliases for an item
      *
      * @param query name of item
      * @return <a href="http://download.oracle.com/javase/6/docs/api/java/util/Collection.html?is-external=true">Collection</a>&lt;<a href="http://download.oracle.com/javase/6/docs/api/java/lang/String.html?is-external=true">String</a>&gt; of aliases
      * @throws IllegalArgumentException if no such item exists
      */
     public static Collection<String> getAliases(String query) throws IllegalArgumentException {
         ItemStack itemStack = items.getItems().get(query);
         if (itemStack == null)
             throw new IllegalArgumentException(items.getSuggestions().findBestWordMatch(query));
         return getAliases(itemStack);
     }
 
     /**
      * Get an OddItemGroup by name
      *
      * @param name name of group
      * @return group with name
      */
     public static OddItemGroup getItemGroup(String name) {
         return groups.getGroup(name);
     }
 
     /**
      * Get an OddItemGroup by data key
      *
      * @param key top-level key
      * @return matching groups
      */
     public static Collection<OddItemGroup> getItemGroups(String key) {
         return groups.getGroups(key);
     }
 
     /**
      * Get an OddItemGroup by second-level data key
      *
      * @param key top-level key
      * @param key2 second-level key
      * @return matching groups
      */
     public static Collection<OddItemGroup> getItemGroups(String key, String key2) {
         return groups.getGroups(key, key2);
     }
 
     /**
      * Returns an ItemStack of quantity 1 of alias query
      *
      * @param query item name
      * @return <a href="http://jd.bukkit.org/apidocs/org/bukkit/inventory/ItemStack.html?is-external=true">ItemStack</a>
      * @throws IllegalArgumentException exception if item not found, message contains closest match
      */
     public static ItemStack getItemStack(String query) throws IllegalArgumentException {
         return getItemStack(query, 1);
     }
 
     @Deprecated
     public static ItemStack getItemStack(String query, Integer quantity) throws IllegalArgumentException {
         return getItemStack(query, quantity.intValue());
     }
 
     /**
      * Returns an ItemStack of specific quantity of alias query
      *
      * @param query item name
      * @param quantity quantity
      * @return <a href="http://jd.bukkit.org/apidocs/org/bukkit/inventory/ItemStack.html?is-external=true">ItemStack</a>
      * @throws IllegalArgumentException exception if item not found, message contains closest match
      */
     public static ItemStack getItemStack(String query, int quantity) throws IllegalArgumentException {
         ItemStack i;
         if (query.startsWith("map")) {
             try {
                 i = new ItemStack(Material.MAP, 1, (query.contains(";") ? Short.valueOf(query.substring(query.indexOf(";") + 1)) : 0));
             } catch (NumberFormatException e) {
                 i = new ItemStack(Material.MAP, 1, (short) 0);
             }
         } else {
             i = items.getItems().get(query);
         }
         if (i != null && !query.startsWith("map")) {
             i.setAmount(quantity);
             return i;
         }
         throw new IllegalArgumentException(items.getSuggestions().findBestWordMatch(query));
     }
 
     /**
      * @return <a href="http://download.oracle.com/javase/6/docs/api/java/util/Collection.html?is-external=true">Collection</a>&lt;<a href="http://jd.bukkit.org/apidocs/org/bukkit/inventory/ItemStack.html?is-external=true">ItemStack</a>&gt; of all defined items
      */
     public static Collection<ItemStack> getItemStacks() {
         return items.getAliases().keySet();
     }
 
     /**
      * Removes itemStack from player's inventory
      *
      * @param player Player to remove itemStack from
      * @param itemStack ItemStack to remove
      * @return amount left over (i.e. player had less than itemStack.getAmount() available)
      */
     public static int removeItem(Player player, ItemStack itemStack) {
         return removeItem(player, itemStack, true);
     }
 
     /**
      * Removes itemStack from player's inventory, possibly ignoring durability
      *
      * @param player Player to remove itemStack from
      * @param itemStack ItemStack to remove
      * @return amount left over (i.e. player had less than itemStack.getAmount() available)
      */
     public static int removeItem(Player player, ItemStack itemStack, boolean durability) {
         ItemStack[] inventory = player.getInventory().getContents();
         int amount = itemStack.getAmount();
         for (int i = 0; i < inventory.length; i++) {
             if (inventory[i] != null && compare(inventory[i], itemStack, durability, false)) {
                 if (amount > inventory[i].getAmount()) {
                     amount -= inventory[i].getAmount();
                    inventory[i].setAmount(0);
                 } else if (amount > 0) {
                     inventory[i].setAmount(inventory[i].getAmount() - amount);
                     amount = 0;
                 } else {
                    inventory[i].setAmount(0);
                 }
             }
             if (amount == 0) break;
         }
         return amount;
     }
 
     /**
      * Removes itemStacks from player's inventory
      *
      * @param player     Player to remove itemStacks from
      * @param itemStacks ItemStacks to remove
      * @return amounts left over (i.e. player had less than itemStack.getAmount() available)
      */
     public static int[] removeItem(Player player, ItemStack... itemStacks) {
         return removeItem(player, true, itemStacks);
     }
 
     /**
      * Removes itemStacks from player's inventory, possibly ignoring durability
      *
      * @param player Player to remove itemStacks from
      * @param durability whether to check durability
      * @param itemStacks ItemStacks to remove
      * @return amounts left over (i.e. player had less than itemStack.getAmount() available)
      */
     public static int[] removeItem(Player player, boolean durability, ItemStack... itemStacks) {
         int[] amount = new int[itemStacks.length];
         for (int i = 0; i < itemStacks.length; i++) {
             amount[i] = removeItem(player, itemStacks[i], durability);
         }
         return amount;
     }
 }
