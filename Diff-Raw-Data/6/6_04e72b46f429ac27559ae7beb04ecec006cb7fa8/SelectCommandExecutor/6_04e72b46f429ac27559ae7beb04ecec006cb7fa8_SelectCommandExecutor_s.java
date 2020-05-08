 package me.ase34.citylanterns.executor;
 
 import java.util.Arrays;
 
 import me.ase34.citylanterns.CityLanterns;
 import me.ase34.citylanterns.Lantern;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.meta.ItemMeta;
 
 import com.sk89q.worldedit.bukkit.WorldEditPlugin;
 import com.sk89q.worldedit.bukkit.selections.CuboidSelection;
 import com.sk89q.worldedit.bukkit.selections.Selection;
 
 public class SelectCommandExecutor implements CommandExecutor {
 
     private CityLanterns plugin;
 
     public SelectCommandExecutor(CityLanterns plugin) {
         this.plugin = plugin;
     }
 
     @Override
     public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
         if (!(sender instanceof Player)) {
             sender.sendMessage("This command can only be used as a player!");
             return true;
         }
         
         String group = (args.length < 1) ? "main" : args[0];
         boolean worldedit = (args.length < 2) ? false : args[1].equalsIgnoreCase("we");
         
         Player player = (Player) sender;
         
         if (worldedit) {
             addLanternsInRegion(player, group);
         } else {
             giveSelectionItem(player, group);
         }
         return true;
     }
 
     private void addLanternsInRegion(Player player, String group) {
         WorldEditPlugin worldEdit = (WorldEditPlugin) Bukkit.getServer()
                 .getPluginManager().getPlugin("WorldEdit");
         if (worldEdit == null) {
             player.sendMessage(ChatColor.RED + "WorldEdit seems not to be enabled on this server!");
             return;
         }
         
         Selection selection = worldEdit.getSelection(player);
         if (selection != null) {
             if (!(selection instanceof CuboidSelection)) {
                 player.sendMessage(ChatColor.RED + "Currently only support for cuboid regions!");
                 return;
             }
             int affected = 0;
             
             Location min = selection.getMinimumPoint();
             Location max = selection.getMaximumPoint();
 
             int minX = min.getBlockX();
             int minY = min.getBlockY();
             int minZ = min.getBlockZ();
             int maxX = max.getBlockX();
             int maxY = max.getBlockY();
             int maxZ = max.getBlockZ();
 
             for (int x = minX; x <= maxX; ++x) {
                 for (int y = minY; y <= maxY; ++y) {
                     for (int z = minZ; z <= maxZ; ++z) {
                         Location loc = new Location(selection.getWorld(), x, y, z);
                         Block block = loc.getBlock();
 
                         if (block.getType() == Material.REDSTONE_LAMP_ON
                                 || block.getType() == Material.REDSTONE_LAMP_OFF) {
                             Lantern lantern = plugin.getLanterns().getLantern(loc);
                             if (lantern != null) {
                                 lantern.setGroup(group);
                             } else {
                                 plugin.getLanterns().add(new Lantern(loc, group));
                             }
                             affected++;
                         }
                     }
                 }
             }
            player.sendMessage(ChatColor.GRAY + String.valueOf(affected) + ChatColor.GOLD + " lanterns were added to group "
                     + ChatColor.GRAY + group + ChatColor.GOLD + "!");
         } else {
             player.sendMessage(ChatColor.RED +  "You didn't have selected anything!");
         }
     }
 
     private void giveSelectionItem(Player player, String group) {
         ItemStack tool = constructSelectionTool(group);
         ItemStack selected = player.getItemInHand();
         
         if (selected == null || selected.getAmount() == 0) {
             player.setItemInHand(tool);
         } else {
             for (int i = 0; i < player.getInventory().getSize(); i++) {
                 if (i == player.getInventory().getHeldItemSlot()) {
                     continue;
                 }
                 ItemStack stack = player.getInventory().getItem(i);
                 if (stack == null || stack.getAmount() == 0) {
                     player.getInventory().setItem(i, selected);
                     player.setItemInHand(tool);
                     return;
                 }
             }
 
             player.sendMessage(ChatColor.RED + "You don't have any free space in your "
                     + "inventory for the selection tool!");
         }
     }
     
     private ItemStack constructSelectionTool(String group) {
         ItemStack stack = new ItemStack(Material.WOOD_AXE);
         
         ItemMeta meta = stack.getItemMeta();
         meta.setDisplayName("Lantern Selection Tool for group '" + group + "'");
         meta.setLore(Arrays.asList("Left-click to add/remove lantern", 
                 "Right-click to get current state of lantern"));
         stack.setItemMeta(meta);
         
         return stack;
     }
 
 }
