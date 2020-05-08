 package Wolfwood.InventorySort;
 
 import com.nijiko.permissions.PermissionHandler;
 import com.nijikokun.bukkit.Permissions.Permissions;
 import java.io.File;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.logging.Logger;
 import org.bukkit.ChatColor;
 import org.bukkit.Server;
 import org.bukkit.World;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginLoader;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.block.*;
 
 /**
  * @version 1.3
  * @author Wolfwood
  */
 // this plugin need's the permission's jar and the bukkit snapshot jar
 public class InventorySort extends JavaPlugin {
 
     public static PermissionHandler Permissions = null;
     private final HashMap<Player, Boolean> debugees = new HashMap<Player, Boolean>();
     public static final Logger log = Logger.getLogger("Minecraft");
 
     public InventorySort(PluginLoader pluginLoader, Server instance, PluginDescriptionFile desc, File folder,
             File plugin, ClassLoader cLoader) {
         super(pluginLoader, instance, desc, folder, plugin, cLoader);
     }
 
     public void onEnable() {
         setupPermissions();
 
         PluginDescriptionFile pdfFile = this.getDescription();
         log.info("[" + pdfFile.getName() + "] version " + pdfFile.getVersion() + " is enabled!");
     }
 
     public void onDisable() {
         PluginDescriptionFile pdfFile = this.getDescription();
         log.info("[" + pdfFile.getName() + "] version " + pdfFile.getVersion() + " is disabled!");
     }
 
     public boolean isDebugging(final Player player) {
         if (debugees.containsKey(player)) {
             return debugees.get(player) != null;
         } else {
             return false;
         }
     }
 
     public void setDebugging(final Player player, final boolean value) {
         debugees.put(player, value);
     }
 
     public void setupPermissions() {
         Plugin test = this.getServer().getPluginManager().getPlugin("Permissions");
 
 
         if (InventorySort.Permissions == null) {
             if (test != null) {
                 InventorySort.Permissions = ((Permissions) test).getHandler();
             } else {
                 log.info("[" + this.getDescription().getName() + "]" + " Permission system not enabled. Disabling plugin.");
                 this.getServer().getPluginManager().disablePlugin(this);
             }
         }
     }
 
     // basically if it's the console sending the command it denies it.
     private boolean anonymousCheck(CommandSender sender) {
         if (!(sender instanceof Player)) {
             sender.sendMessage("Cannot execute that command, I don't know who you are!");
             return true;
         } else {
             return false;
         }
     }
 
     @Override
     public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
         String[] trimmedArgs = args;
         String commandName = command.getName().toLowerCase();
 
         if (commandName.equals("sort")) {
             if (anonymousCheck(sender)) {
                 return false;
             }
             return sortPlyInv(sender, trimmedArgs);
         } else if (commandName.equals("sortchest")) {
             if (anonymousCheck(sender)) {
                 return false;
             }
             if (!InventorySort.Permissions.has((Player)sender, "iSort.basic.chest")) {
                 sender.sendMessage(ChatColor.RED + "You do not have permission to run " + ChatColor.GREEN + "/sortchest");
                 return true;
             }
             if (sortChestInv(sender, trimmedArgs)) {
                 sender.sendMessage(ChatColor.GRAY + "The chest has been sorted.");
                 return true;
             } else {
                return true;
             }
         }
         return false;
     }
 
     private boolean sortPlyInv(CommandSender sender, String[] split) {
         Player player = (Player) sender;
         int end = 36;
         int start = 0;
         if (split.length == 0) {
             return dispSortHelp(sender);
         } else if (split.length == 1) {
             String what = split[0];
             if (what.equalsIgnoreCase("all")) {
                 if (!InventorySort.Permissions.has(player, "iSort.basic.all")) {
                     sender.sendMessage(ChatColor.RED + "You do not have permission to run " + ChatColor.GREEN + "/sort all");
                     return true;
                 }
                 end = 36;
                 start = 0;
             } else if (what.equalsIgnoreCase("top")) {
                 if (!InventorySort.Permissions.has(player, "iSort.basic.top")) {
                     sender.sendMessage(ChatColor.RED + "You do not have permission to run " + ChatColor.GREEN + "/sort top");
                     return true;
                 }
                 end = 36;
                 start = 9;
             } else {
                 return dispSortHelp(sender);
             }
         } else if (split.length == 2 && InventorySort.Permissions.has(player, "iSort.basic.range")) {
             int j = 35;
             int k = 0;
             try {
                 j = Integer.valueOf(split[0]);
                 k = Integer.valueOf(split[1]);
             } catch (NumberFormatException e) {
                 sender.sendMessage("Those were not numbers!");
                 return dispSortHelp(sender);
             }
             if (j <= 35 && j >= 0 && k <= 35 && k >= 0) {
                 if (k > j) {
                     end = k + 1;
                     start = j;
                 } else if (j > k) {
                     end = j + 1;
                     start = k;
                 } else if (j == k) {
                     sender.sendMessage("You just want to slot " + j + " sorted? Ok, done.");
                     return true;
                 } else {
                     return dispSortHelp(sender); //should never get here
                 }
             } else {
                 sender.sendMessage(ChatColor.YELLOW + "Out of range error.");
                 return dispSortHelp(sender);
             }
         } else {
             sender.sendMessage(ChatColor.RED + "You do not have permission to run " + ChatColor.GREEN + "/sort <0-35> <0-35>");
             return true;
         }
         ItemStack[] plyrStack = player.getInventory().getContents();
         plyrStack = sortItemStack(plyrStack, start, end);
 
         player.getInventory().setContents(plyrStack);
         sender.sendMessage(ChatColor.GRAY + "Slots " + start + "-" + (end - 1) + " have been sorted!");
         return true;
     }
 
     private boolean sortChestInv(CommandSender sender, String[] split) {
         Player player = (Player) sender;
         TargetBlock hitBlox = new TargetBlock(player);
         Block target = hitBlox.getTargetBlock();
         int intX = target.getX();
         int intY = target.getY();
         int intZ = target.getZ();
         Block block = player.getWorld().getBlockAt(intX, intY, intZ);
         BlockState state = block.getState();
         if (state instanceof Chest) {
             Chest chest1 = (Chest) state;
             Chest chest2 = isDoubleChest(player.getWorld(), block, 1);
             if (chest2 != null) {
                 return sortDblChst(chest1, chest2);
             } else {
                 chest2 = isDoubleChest(player.getWorld(), block, -1);
                 if (chest2 != null) {
                     return sortDblChst(chest2, chest1);
                 }
             }
             ItemStack[] stack1 = sortItemStack(chest1.getInventory().getContents());
             chest1.getInventory().setContents(stack1);
             chest1.update();
            return true;
         } else {
             sender.sendMessage("You are not looking at a Chest");
            return false;
         }
        
     }
 
     private boolean sortDblChst(Chest chest1, Chest chest2) {
         ItemStack[] s1 = chest1.getInventory().getContents();
         ItemStack[] s2 = chest2.getInventory().getContents();
         ItemStack[] both = sortItemStack(concat(s1, s2));
         s1 = Arrays.copyOfRange(both, 0, s1.length);
         s2 = Arrays.copyOfRange(both, s1.length, s1.length + s2.length);
         chest1.getInventory().setContents(s1);
         chest2.getInventory().setContents(s2);
         chest1.update();
         chest2.update();
         return true;
     }
 
     private ItemStack[] concat(ItemStack[] A, ItemStack[] B) {
         ItemStack[] C = new ItemStack[A.length + B.length];
         System.arraycopy(A, 0, C, 0, A.length);
         System.arraycopy(B, 0, C, A.length, B.length);
 
         return C;
     }
 
     private Chest isDoubleChest(World world, Block chest1, int offset) {
         Block chest2 = world.getBlockAt(chest1.getX() + offset, chest1.getY(), chest1.getZ());
         BlockState state = chest2.getState();
         if (state instanceof Chest) {
             return (Chest) state;
         }
         chest2 = world.getBlockAt(chest1.getX(), chest1.getY(), chest1.getZ() + offset);
         state = chest2.getState();
         if (state instanceof Chest) {
             return (Chest) state;
         }
         return null;
     }
 
     private ItemStack[] sortItemStack(ItemStack[] stack) {
         return sortItemStack(stack, 0, stack.length);
     }
 
     private ItemStack[] sortItemStack(ItemStack[] stack, int start, int end) {
         stack = stackItems(stack, start, end);
         boolean doMore = true;
         int n = end;
         while (doMore) {
             n--;
             doMore = false;  // assume this is our last pass over the array
             for (int i = start; i < n; i++) {
                 if (stack[i].getTypeId() > stack[i + 1].getTypeId()) {
                     // exchange elements
                     ItemStack temp = stack[i];
                     stack[i] = stack[i + 1];
                     stack[i + 1] = temp;
                     doMore = true;  // after an exchange, must look again
                 }
             }
         }
         return stack;
     }
 
     private boolean dispSortHelp(CommandSender sender) {
         Player player = (Player) sender;
         if (InventorySort.Permissions.has(player, "iSort.basic.all")) {
             sender.sendMessage("Example: " + ChatColor.GREEN + "/sort all " + ChatColor.WHITE + "- sorts all slots");
         } else {
             sender.sendMessage(ChatColor.RED + "You do not have permission to run " + ChatColor.GREEN + "/sort all");
         }
         if (InventorySort.Permissions.has(player, "iSort.basic.top")) {
             sender.sendMessage("Example: " + ChatColor.GREEN + "/sort top " + ChatColor.WHITE + "- sorts slots 9 - 35");
         } else {
             sender.sendMessage(ChatColor.RED + "You do not have permission to run " + ChatColor.GREEN + "/sort top");
         }
         if (InventorySort.Permissions.has(player, "iSort.basic.range")) {
             sender.sendMessage("Example: " + ChatColor.GREEN + "/sort 9 35 " + ChatColor.WHITE + "- sorts slots 9 - 35");
             sender.sendMessage("Example: " + ChatColor.GREEN + "/sort 35 9 " + ChatColor.WHITE + "- sorts slots 9 - 35");
             sender.sendMessage("Example: " + ChatColor.GREEN + "/sort 10 15 " + ChatColor.WHITE + "- sorts slots 10 - 15");
         } else {
             sender.sendMessage(ChatColor.RED + "You do not have permission to run " + ChatColor.GREEN + "/sort <0-35> <0-35>");
         }
         if (InventorySort.Permissions.has(player, "iSort.basic.chest")) {
             sender.sendMessage("Example: " + ChatColor.GREEN + "/sortchest " + ChatColor.WHITE + "- sorts the chest your looking at");
         } else {
             sender.sendMessage(ChatColor.RED + "You do not have permission to run " + ChatColor.GREEN + "/sortchest");
         }
         return true;
     }
 
     private ItemStack[] stackItems(ItemStack[] items, int start, int end) {
         for (int i = start; i < end; i++) {
             ItemStack item = items[i];
 
             // Avoid infinite stacks and stacks with durability
             if (item == null || item.getAmount() <= 0
                     || ItemType.shouldNotStack(item.getTypeId())) {
                 continue;
             }
 
             // Ignore buckets
             if (item.getTypeId() >= 325 && item.getTypeId() <= 327) {
                 continue;
             }
 
             if (item.getAmount() < 64) {
                 int needed = 64 - item.getAmount(); // Number of needed items until 64
 
                 // Find another stack of the same type
                 for (int j = i + 1; j < end; j++) {
                     ItemStack item2 = items[j];
 
                     // Avoid infinite stacks and stacks with durability
                     if (item2 == null || item2.getAmount() <= 0
                             || ItemType.shouldNotStack(item.getTypeId())) {
                         continue;
                     }
 
                     // Same type?
                     // Blocks store their color in the damage value
                     if (item2.getTypeId() == item.getTypeId() && (!ItemType.usesDamageValue(item.getTypeId()) || item.getDurability() == item2.getDurability())) {
                         // This stack won't fit in the parent stack
                         if (item2.getAmount() > needed) {
                             item.setAmount(64);
                             item2.setAmount(item2.getAmount() - needed);
                             break;
                             // This stack will
                         } else {
                             item.setAmount(item.getAmount() + item2.getAmount());
                             needed = 64 - item.getAmount();
                             items[j].setTypeId(0);
                         }
                     }
                 }
             }
         }
         return items;
     }
 }
