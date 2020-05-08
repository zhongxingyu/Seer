 package net.omnivr.ocd;
 
 import java.io.File;
 import java.util.Arrays;
 import java.util.Comparator;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.TreeMap;
 import net.omnivr.olib.Constants;
 import net.omnivr.olib.ItemDB;
 import net.omnivr.olib.Util;
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.block.ContainerBlock;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.util.BlockIterator;
 
 /**
  * OChestDump for Bukkit
  *
  * @author Nayruden
  */
 public class OChestDump extends JavaPlugin {
 
     private final int MAX_CHEST_DISTANCE = 4;
     private final OCDPlayerListener playerListener = new OCDPlayerListener(this);
 
     public void onEnable() {
         // Register our events
         PluginManager pm = getServer().getPluginManager();
         pm.registerEvent(Event.Type.PLAYER_INTERACT, playerListener, Priority.Monitor, this);
 
         // Setup configs
         getDataFolder().mkdirs(); // Make sure dir exists
         File config_file = new File(getDataFolder(), "config.yml");
         if (!config_file.isFile()) {
             Util.extractResourceTo("/config.yml", config_file.getPath());
         }
 
         PluginDescriptionFile pdfFile = this.getDescription();
         System.out.println(pdfFile.getName() + " version " + pdfFile.getVersion() + " has been loaded.");
     }
 
     public void onDisable() {
     }
 
     @Override
     public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
         if (command.getName().equalsIgnoreCase("ocd")) {
             if (!(sender instanceof Player)) {
                 sender.sendMessage("Only players can use this command");
                 return false;
             }
 
             Player player = (Player) sender;
             if (args.length < 1 || args.length > 2 || !(args[0].equalsIgnoreCase("stash") || args[0].equalsIgnoreCase("loot") || args[0].equalsIgnoreCase("swap") || args[0].equalsIgnoreCase("sort"))) {
                 player.sendMessage(ChatColor.RED + "/ocd <stash|loot|swap> [item]");
                 player.sendMessage(ChatColor.RED + "OR /ocd sort [<name|amount|id>]");
                 player.sendMessage(ChatColor.RED + "stash - Puts your items in the container you're looking at");
                 player.sendMessage(ChatColor.RED + "loot - Put all items in the container into your inventory");
                 player.sendMessage(ChatColor.RED + "swap - Exhange items between your inventory and container");
                 player.sendMessage(ChatColor.RED + "sort - Sort items by name, amount, or id, defaults to name");
                 player.sendMessage(ChatColor.RED + "Adding item ID/name will exchange only that item for stash/loot");
                 return false;
             }
 
             Block block = player.getTargetBlock(null, MAX_CHEST_DISTANCE);
 
             if (block == null || !(block.getType() == Material.CHEST || block.getType() == Material.DISPENSER)) {
                 player.sendMessage(ChatColor.RED + "You need to look at a chest or dispenser to use this command");
                 return false;
             }
 
             ContainerBlock container1 = (ContainerBlock) block.getState();
             ContainerBlock container2 = null;
             ItemStack[] container1_contents = container1.getInventory().getContents();
             ItemStack[] container2_contents = null;
 
             Util.DoubleChest double_chest = Util.getDoubleChestIfExists(block);
             if (double_chest != null) {
                 block = (Block) double_chest.primary_chest;
                 container1 = (ContainerBlock) double_chest.primary_chest.getState();
                 container2 = (ContainerBlock) double_chest.secondary_chest.getState();
                 container1_contents = container1.getInventory().getContents();
                 container2_contents = container2.getInventory().getContents();
             }
 
             if (!player.isOp() && getConfiguration().getBoolean("require-chest-open", false) && !OCDProtectionInfo.isOwner(block.getLocation(), player.getName())) {
                 player.sendMessage(ChatColor.RED + "You must prove you own this container by opening it first");
                 return false;
             }
 
             ItemStack[] container_contents = container1_contents;
             if (container2 != null) {
                 container_contents = Util.concat(container1_contents, container2_contents);
             }
 
             ItemStack[] player_contents = player.getInventory().getContents();
 
             if (args[0].equalsIgnoreCase("sort")) {
                 Comparator<ItemStack> comparator;
                 if (args.length == 1 || args[1].equalsIgnoreCase("name")) {
                     comparator = new orderByName();
                 } else if (args[1].equalsIgnoreCase("amount")) {
                     comparator = new orderByAmount(container_contents);
                 } else if (args[1].equalsIgnoreCase("id")) {
                     comparator = new orderByID();
                 } else {
                     player.sendMessage(ChatColor.RED + "Unknown sort: " + args[1]);
                     return false;
                 }
                 compactInventory(container_contents);
                 Arrays.sort(container_contents, comparator);
             } else { // loot, stash, swap
 
                 ItemDB.ItemIDAndDurability item = null;
                 if (args.length == 2) {
                     item = ItemDB.nameOrIDToID(args[1]);
                     if (item == null) {
                         player.sendMessage(ChatColor.RED + "Unknown item name/id: " + args[1]);
                         return false;
                     }
                 }
 
                 if (args[0].equalsIgnoreCase("stash")) {
                     tryFill(player_contents, container_contents, item);
                 } else if (args[0].equalsIgnoreCase("loot")) {
                     tryFill(container_contents, player_contents, item);
                 } else {
                     ItemStack[] new_chest_contents = new ItemStack[container_contents.length];
                     tryFill(player_contents, new_chest_contents, null);
                     tryFill(container_contents, player_contents, null);
                     tryFill(container_contents, new_chest_contents, null);
                     container_contents = new_chest_contents;
                 }
             }
 
             if (container2 == null) {
                 container1_contents = container_contents;
             } else {
                 System.arraycopy(container_contents, 0, container1_contents, 0, container1_contents.length);
                 System.arraycopy(container_contents, container1_contents.length, container2_contents, 0, container2_contents.length);
             }
 
             container1.getInventory().setContents(container1_contents);
             if (container2 != null) {
                 container2.getInventory().setContents(container2_contents);
             }
             player.getInventory().setContents(player_contents);
 
         }
 
         return true;
     }
 
     private void tryFill(ItemStack[] inventory_from, ItemStack[] inventory_to, ItemDB.ItemIDAndDurability restricted_item) {
         int from_size = inventory_from.length;
         int to_size = inventory_to.length;
         for (int from_slot = 0; from_slot < from_size; from_slot++) {
             if (inventory_from[from_slot] == null) {
                 continue;
             }
 
             ItemStack from_stack = inventory_from[from_slot];
             if (from_stack.getAmount() == 0 || (restricted_item != null && (from_stack.getTypeId() != restricted_item.getID() || restricted_item.getDurability() == -1 || from_stack.getDurability() != restricted_item.getDurability()))) {
                 continue;
             }
 
             for (int to_slot = 0; to_slot < to_size; to_slot++) {
                 ItemStack to_stack = inventory_to[to_slot];
                 if (to_stack == null || to_stack.getAmount() == 0) {
                     inventory_to[to_slot] = from_stack;
                     inventory_from[from_slot] = null;
                     break;
                 }
                 compactStack(from_stack, to_stack); // Compact if possible
             }
         }
     }
 
     private void compactStack(ItemStack from_stack, ItemStack to_stack) {
        if (from_stack.getType() != Material.AIR && from_stack.getType() == to_stack.getType() && from_stack.getDurability() == to_stack.getDurability()) {
             int max = to_stack.getMaxStackSize();
             int diff = Math.min(from_stack.getAmount() + to_stack.getAmount(), max) - to_stack.getAmount();
             to_stack.setAmount(to_stack.getAmount() + diff);
             from_stack.setAmount(from_stack.getAmount() - diff);
         }
     }
 
     private void compactInventory(ItemStack[] stacks) {
         for (int from = 1; from < stacks.length; from++) {
             for (int to = 0; to < from; to++) {
                 compactStack(stacks[from], stacks[to]);
             }
         }
     }
 
     private class orderDefaults implements Comparator<ItemStack> {
 
         public int compare(ItemStack a, ItemStack b) {
             if (a == null || a.getAmount() == 0) {
                 return 1;
             }
             if (b == null || b.getAmount() == 0) {
                 return -1;
             }
             if (a.getType() == b.getType()) {
                 return a.getDurability() - b.getDurability();
             }
             return 0;
         }
     }
 
     private class orderByName extends orderDefaults {
 
         @Override
         public int compare(ItemStack a, ItemStack b) {
             int result = super.compare(a, b);
             if (result != 0) {
                 return result;
             }
             return a.getType().toString().compareToIgnoreCase(b.getType().toString());
         }
     }
 
     private class orderByAmount extends orderDefaults {
 
         Map<Integer, Integer> amounts = new TreeMap<Integer, Integer>();
 
         public orderByAmount(ItemStack[] stacks) {
             for (ItemStack stack : stacks) {
                 Integer amount = amounts.get(stack.getTypeId());
                 if (amount == null) {
                     amount = Integer.valueOf(0);
                 }
                 amounts.put(stack.getTypeId(), amount + stack.getAmount());
             }
         }
 
         @Override
         public int compare(ItemStack a, ItemStack b) {
             int result = super.compare(a, b);
             if (result != 0) {
                 return result;
             }
             return amounts.get(b.getTypeId()) - amounts.get(a.getTypeId());
         }
     }
 
     private class orderByID extends orderDefaults {
 
         @Override
         public int compare(ItemStack a, ItemStack b) {
             int result = super.compare(a, b);
             if (result != 0) {
                 return result;
             }
             return a.getTypeId() - b.getTypeId();
         }
     }
 }
