 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package me.xhawk87.Coinage;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.NavigableMap;
 import java.util.TreeMap;
 import me.xhawk87.Coinage.moneybags.MoneyBag;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.block.BlockState;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.meta.ItemMeta;
 
 /**
  *
  * @author XHawk87
  */
 public class Currency {
 
     private Coinage plugin;
     private ConfigurationSection data;
     private Map<String, Denomination> denominations = new HashMap<>();
     private Map<String, Denomination> byPrint = new HashMap<>();
     private NavigableMap<Integer, Denomination> byValue = new TreeMap<>();
 
     public Currency(Coinage plugin, ConfigurationSection data) {
         this.plugin = plugin;
         this.data = data;
         String alias = getAlias();
         if (alias.contains("&")) {
             alias = alias.replace('&', ChatColor.COLOR_CHAR);
             data.set("alias", alias);
         }
         ConfigurationSection denomSection = data.getConfigurationSection("denominations");
         if (denomSection != null) {
             for (String key : denomSection.getKeys(false)) {
                 Denomination denomination = new Denomination(this, denomSection.getConfigurationSection(key));
                 denominations.put(key, denomination);
                 byPrint.put(denomination.getPrint(), denomination);
                 byValue.put(denomination.getValue(), denomination);
             }
         }
     }
 
     /**
      * Deletes this currency. This will cause ALL denominations of this currency
      * to cease being considered legal tender, however none of them will be
      * removed from the game.
      */
     public void delete() {
         plugin.deleteCurrency(this);
     }
 
     /**
      * The name of this currency to be used with commands
      *
      * @return The currency name
      */
     public String getName() {
         return data.getName();
     }
 
     /**
      * The display name for the currency to appear before the denomination text
      * in the item lore
      *
      * @return The currency alias
      */
     public final String getAlias() {
         return data.getString("alias");
     }
 
     @Override
     public String toString() {
         return getAlias() + ChatColor.RESET;
     }
 
     /**
      * Gets a denomination of this currency by its name
      *
      * @param name The name of the denomination
      * @return The denomination
      */
     public Denomination getDenominationByName(String name) {
         return denominations.get(name);
     }
 
     /**
      * Gets a denomination of this currency by the items lore
      *
      * @param lore The items lore
      * @return The denomination
      */
     public Denomination getDenominationByLore(String lore) {
         return byPrint.get(lore.substring(getAlias().length()));
     }
 
     /**
      * Gets a denomination of this currency by its value
      *
      * @param value The value of the denomination
      * @return The denomination with this value
      */
     public Denomination getDenominationByValue(int value) {
         return byValue.get(value);
     }
 
     /**
      * Checks if the items lore matches this currency
      *
      * @param lore The items lore
      * @return True if the item belongs to this currency, False if not
      */
     public boolean matches(String lore) {
         return lore.startsWith(getAlias());
     }
 
     /**
      * Creates a new denomination of this currency
      *
      * The smallest unit of a currency must always be 1 in order for all values
      * of currency to be represented by its denominations.
      *
      * @param name The denomination name to be used with commands
      * @param alias The alias to be used as the display name for the item
      * @param print The print to be used after the currency alias in the item
      * lore
      * @param value The value of the denomination in whole units of the currency
      * @param itemId The item ID to use for this denomination
      * @param itemData The item data to use for this denomination
      * @return The created denomination, or null if the name or alias already
      * exists
      */
     public Denomination createDenomination(String name, String alias, String print, int value, int itemId, short itemData) {
         if (denominations.containsKey(name) || byPrint.containsKey(print)) {
             return null;
         }
 
         ConfigurationSection denomSection = data.createSection("denominations." + name);
         denomSection.set("alias", alias);
         denomSection.set("print", print);
         denomSection.set("value", value);
         denomSection.set("item-id", itemId);
         denomSection.set("item-data", itemData);
         Denomination denomination = new Denomination(this, denomSection);
         denominations.put(name, denomination);
         byPrint.put(print, denomination);
         plugin.saveConfig();
         return denomination;
     }
 
     /**
      * Deletes an existing denomination by its name. This will cause the
      * denomination to cease being considered legal tender, however it will not
      * remove the items from the game
      *
      * @param name The denomination to delete
      * @return True if the denomination was deleted, False if no such
      * denomination existed
      */
     public boolean deleteDenomination(String name) {
         Denomination denomination = getDenominationByName(name);
         if (denomination == null) {
             return false;
         }
         return deleteDenomination(denomination);
     }
 
     /**
      * Deletes an existing denomination. This will cause the denomination to
      * cease being considered legal tender, however it will not remove the items
      * from the game.
      *
      * @param denomination The denomination to delete
      * @return True if the denomination was deleted, false if it does not belong
      * to this currency
      */
     public boolean deleteDenomination(Denomination denomination) {
         String name = denomination.getName();
         String print = denomination.getPrint();
         if (!denominations.containsKey(name)
                 || !byPrint.containsKey(print)) {
             return false;
         }
 
         denominations.remove(name);
         byPrint.remove(print);
         data.set("denominations." + name, null);
         plugin.saveConfig();
         return true;
     }
 
     /**
      * <p>Gives the specified value in coins of this currency to the given
      * player. Any excess coins that cannot be carried will be dropped at the
      * player's feet.</p>
      *
      * <p>This will use larger denominations for preference over smaller
      * denominations in order to reduce the total number of coins held by the
      * player. </p>
      *
      * <p>This will attempt to find and fill moneybags within the player's
      * inventory before it fills the inventory itself</p>
      *
      * <p>The player will not give change, as such an error log will be
      * generated if the exact number of coins cannot be made out. This can only
      * occur if there is no denomination worth a single unit of the
      * currency.</p>
      *
      * <p>The player will be notified of the transaction</p>
      *
      * @param player The player to give coins
      * @param totalValue The value in coins to give
      */
     public boolean give(Player player, int totalValue) {
         if (give(player.getInventory(), totalValue)) {
             player.sendMessage("You received " + totalValue + " in " + toString());
             return true;
         } else {
             return false;
         }
     }
 
     /**
      * <p>Gives the specified value in coins of this currency to the given
      * inventory. Any excess coins that cannot be carried will be dropped at the
      * location of the inventory holder</p>
      *
      * <p>This will attempt to find and fill moneybags within the inventory
      * before it fills the inventory itself</p>
      *
      * <p>This will use larger denominations for preference over smaller
      * denominations in order to reduce the total number of coins contained in
      * the inventory. </p>
      *
      * <p>The inventory will not give change, as such an error log will be
      * generated if the exact number of coins cannot be made out. This can only
      * occur if there is no denomination worth a single unit of the
      * currency.</p>
      *
      * @param inv The inventory to give coins
      * @param totalValue The value in coins to give
      */
     public boolean give(Inventory inv, int totalValue) {
         Map.Entry<Integer, Denomination> entry = byValue.lastEntry();
         int value = totalValue;
         List<ItemStack> coins = new ArrayList<>();
         while (value > 0) {
             int denomValue = entry.getKey();
             int amount = value / denomValue;
             while (amount > 0) {
                 ItemStack coin = entry.getValue().create(amount);
                 value -= coin.getAmount() * denomValue;
                 coins.add(coin);
                 amount -= coin.getAmount();
             }
             entry = byValue.lowerEntry(entry.getKey());
             if (entry == null && value > 0) {
                 plugin.getLogger().warning("Could not give exactly " + totalValue + " in " + getName() + " to " + inv.getHolder().toString() + " as " + value + " remains and no smaller denomination exists. Perhaps a denomination of value 1 should be created?");
                 if (inv.getHolder() instanceof Player) {
                     Player player = (Player) inv.getHolder();
                     player.sendMessage("An error occured while attempting to give " + toString() + ". The time and amount was logged, please report the issue to admin");
                 }
                 return false;
             }
         }
 
         // Attempt to find and fill moneybags first
         for (ItemStack itemStack : inv.getContents()) {
             if (itemStack == null || itemStack.getTypeId() == 0) {
                 continue;
             }
             MoneyBag moneyBag = plugin.getMoneyBag(itemStack);
             if (moneyBag != null) {
                 ItemStack[] coinArray = coins.toArray(new ItemStack[coins.size()]);
                 Map<Integer, ItemStack> remaining = moneyBag.getInventory().addItem(coinArray);
                 moneyBag.save();
                 if (remaining.isEmpty()) {
                     return true;
                 }
                 coins = new ArrayList<>(remaining.values());
             }
         }
 
         // Then attempt to fill inventory
         ItemStack[] coinArray = coins.toArray(new ItemStack[coins.size()]);
         HashMap<Integer, ItemStack> remaining = inv.addItem(coinArray);
         if (!remaining.isEmpty()) {
             Location loc;
             if (inv.getHolder() instanceof Entity) {
                 Entity entity = (Entity) inv.getHolder();
                 loc = entity.getLocation();
             } else if (inv.getHolder() instanceof BlockState) {
                 BlockState state = (BlockState) inv.getHolder();
                 loc = state.getBlock().getLocation().add(0.5, 0.5, 0.5);
             } else {
                 plugin.getLogger().warning("Could not give all coins as there was not enough space in the inventory and " + inv.getHolder().toString() + " has no physical location at which they could be dropped. The following items were lost:");
                 YamlConfiguration serializer = new YamlConfiguration();
                 serializer.set("remaining", new ArrayList<>(remaining.values()));
                 plugin.getLogger().warning(serializer.saveToString());
                 return false;
             }
             for (ItemStack toDrop : remaining.values()) {
                 loc.getWorld().dropItem(loc, toDrop);
             }
         }
 
         return true;
     }
 
     /**
      * <p>Takes the specified value in this currency from the given player.</p>
      *
      * <p>This will take smaller denominations for preference over larger
      * denominations in order to reduce the total number of coins carried by the
      * player. This will give change to the player where appropriate.</p>
      *
      * <p>This will also take money from any money bags if there is insufficient
      * currency in the player's inventory</p>
      *
      * <p>If the player does not have enough, no coins will be taken.</p>
      *
      * <p>The player will be notified of the transaction. To silently give coins
      * use the player's inventory instead.</p>
      *
      * @param player The player to take coins from
      * @param totalValue The total value in this currency to take
      * @return True if the player had enough value in currency, false if not
      */
     public boolean spend(Player player, int totalValue) {
         if (spend(player.getInventory(), totalValue)) {
             player.updateInventory();
             player.sendMessage("You hand over " + totalValue + " in " + toString());
         } else {
             player.sendMessage("You do not have " + totalValue + " in " + toString() + " in your inventory to hand over");
         }
        return true;
     }
 
     private int spendFromInventory(Inventory inv, Denomination denomination, int totalValue) {
         int denomValue = denomination.getValue();
         int maxAmount = (int) Math.ceil((double) totalValue / (double) denomValue);
         int remaining = totalValue;
         int index;
         while ((index = first(inv, denomination)) != -1) {
             ItemStack item = inv.getItem(index);
 
             if (item.getAmount() >= maxAmount) {
                 remaining -= denomValue * maxAmount;
                 if (item.getAmount() == maxAmount) {
                     inv.clear(index);
                 } else {
                     item.setAmount(item.getAmount() - maxAmount);
                 }
             } else {
                 remaining -= denomValue * item.getAmount();
                 inv.clear(index);
             }
         }
         return remaining;
     }
 
     /**
      * Takes the specified value in this currency from the given inventory.
      *
      * <p>This will take smaller denominations for preference over larger
      * denominations in order to reduce the total number of coins in the
      * inventory. This will give change to the inventory where appropriate.</p>
      *
      * <p>This will also take money from any money bags in the inventory</p>
      *
      * <p>If the inventory does not contain enough, no coins will be taken.</p>
      *
      * @param inv The inventory to take coins from
      * @param totalValue The total value in this currency to take
      * @return True if the inventory contained enough value in currency, false
      * if not
      */
     public boolean spend(Inventory inv, int totalValue) {
         int value = totalValue;
         if (getCoinCount(inv) < totalValue) {
             return false;
         }
 
         List<MoneyBag> moneyBags = new ArrayList<>();
         for (ItemStack itemStack : inv.getContents()) {
             if (itemStack == null || itemStack.getTypeId() == 0) {
                 continue;
             }
             MoneyBag moneyBag = plugin.getMoneyBag(itemStack);
             if (moneyBag != null) {
                 moneyBags.add(moneyBag);
             }
         }
 
         Map.Entry<Integer, Denomination> entry = byValue.firstEntry();
         while (value > 0 && entry != null) {
             int denomValue = entry.getKey();
             Denomination denomination = entry.getValue();
             value = spendFromInventory(inv, denomination, value);
             for (MoneyBag moneyBag : moneyBags) {
                 if (value <= 0) {
                     break;
                 }
                 value = spendFromInventory(moneyBag.getInventory(), denomination, value);
                 moneyBag.save();
             }
             entry = byValue.higherEntry(denomValue);
         }
         if (value < 0) {
             give(inv, -value);
         }
         return true;
     }
 
     /**
      * <p>Combine all coins of this currency in the given inventory into higher
      * denominations where possible in order to minimise the number of coins
      * carried.</p>
      *
      * <p>This also combines coins in moneybags within the inventory, attempting
      * to place all coins into the fewest bags possible and the inventory itself
      * as a last resort</p>
      *
      * @param inv The inventory
      */
     public void combine(Inventory inv) {
         int coinCount = getCoinCount(inv);
         spend(inv, coinCount);
         give(inv, coinCount);
     }
 
     /**
      * Get the total value of all coins in the given inventory for this currency
      *
      * <p>This will include coins in any money bags within this inventory</p>
      *
      * @param inv The inventory to count
      * @return The total value
      */
     public int getCoinCount(Inventory inv) {
         int coinCount = 0;
         for (ItemStack item : inv.getContents()) {
             if (item == null) {
                 continue;
             }
             MoneyBag moneyBag = plugin.getMoneyBag(item);
             if (moneyBag != null) {
                 coinCount += getCoinCount(moneyBag.getInventory());
             } else {
                 ItemMeta meta = item.getItemMeta();
                 if (meta.hasLore() && meta.getLore().size() == 1) {
                     String lore = meta.getLore().get(0);
                     if (matches(lore)) {
                         Denomination denomination = getDenominationByLore(lore);
                         if (denomination != null) {
                             coinCount += denomination.getValue() * item.getAmount();
                         }
                     }
                 }
             }
         }
         return coinCount;
     }
 
     /**
      * Get the first index of the given denomination of coin in the specified
      * inventory
      *
      * @param inv The inventory to check
      * @param denomination The denomination of coin
      * @return The first index or -1 if none exist
      */
     public static int first(Inventory inv, Denomination denomination) {
         ItemStack[] contents = inv.getContents();
         for (int i = 0; i < contents.length; i++) {
             ItemStack item = contents[i];
             if (item == null) {
                 continue;
             }
             if (denomination.matches(item)) {
                 return i;
             }
         }
         return -1;
     }
 
     public List<Denomination> getAllDenominations() {
         return new ArrayList<>(denominations.values());
     }
 }
