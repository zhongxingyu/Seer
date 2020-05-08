 package com.github.Indiv0.ChestEmpty;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map.Entry;
 
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.Chest;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.metadata.FixedMetadataValue;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.mcstats.MetricsLite;
 
 public class ChestEmpty extends JavaPlugin {
     // Stores the players currently emptying chests.
     private ArrayList<String> playersSelecting = new ArrayList<String>();
     // Stores the chests and their last inventories prior to deletion.
     private HashMap<Block, ItemStack[]> lastDeletedItems = new HashMap<Block, ItemStack[]>();
 
     // Initializes an ItemCraftListener.
     public final BlockSelectionListener blockListener = new BlockSelectionListener(this);
 
     public void onEnable() {
         // Retrieves an instance of the PluginManager.
         PluginManager pm = getServer().getPluginManager();
 
         // Registers the blockListener with the PluginManager.
         pm.registerEvents(this.blockListener, this);
 
         // Enable PluginMetrics.
         enableMetrics();
         
         // Prints a message to the server confirming successful initialization of the plugin.
         PluginDescriptionFile pdfFile = this.getDescription();
         getLogger().info(pdfFile.getName() + " " + pdfFile.getVersion() + " is enabled.");
     }
 
     public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
         // Checks to see if the command is the "/chestempty" command.
         if(!cmd.getName().equalsIgnoreCase("chestempty")) return false;
 
         // Checks to make sure a player is performing the command.
         if(!(sender instanceof Player)) {
             sender.sendMessage("This command can only be run by a player.");
             return false;
         }
 
         // Checks to make sure user has proper permissions.
         if(!sender.hasPermission("chestempty.use"))
             return false;
         
         // Makes sure at least one argument has been provided.
         if(args.length == 0) {
             sender.sendMessage("To use ChestEmpty, type \"/chestempty\" followed by: toggle or undo.");
             return false;
         }
 
         // Makes sure the appropriate amount of arguments has been provided.
         if(args.length > 1) {
             sender.sendMessage("Too many arguments. Valid arguments are: toggle or undo.");
             return false;
         }
         
         // Checks to see if the argument(s) provided is not one of the valid options.
         if(!args[0].equals("toggle") && !args[0].equals("undo")) {
             sender.sendMessage("Invalid argument. Valid arguments are: toggle or undo");
             return false;
         }
         
         // Gets the name of the player calling the command.
         String playerName = ((Player)sender).getDisplayName();
         
         // Toggles the chestempty selection feature for the user.
         if(args[0].equals("toggle")) {
             toggleSelecting(playerName, sender);
             return true;
         }
         // Undoes the previous chestempty operation for that user.
         if(args[0].equals("undo")) {
             if(isSelecting(playerName)) {
                 sender.sendMessage("Please exit selection mode first.");
                 return false;
             }
             
             undoDeleteChestContents(sender);
             return true;
         }
         
         return false;
     }
 
     private void enableMetrics()
     {
         try {
             MetricsLite metrics = new MetricsLite(this);
             metrics.start();
         } catch (IOException ex) {
             System.out.println("An error occured while attempting to connect to PluginMetrics.");
         }
     }
 
     private void undoDeleteChestContents(CommandSender sender) {
         Player player = (Player) sender;
         
         // Checks to see if the cache has a backup for that user.
         if (!player.hasMetadata("ChestBackupID")) {
             sender.sendMessage("Player has not emptied any chests.");
             return;
         }
         
         if (lastDeletedItems.isEmpty()) {
             sender.sendMessage("No chest backups exist.");
             player.removeMetadata("ChestBackupID", this);
             return;
         }
         
         // Checks every chest within the cache (this is probably a performance bottleneck).
         Iterator<Entry<Block, ItemStack[]>> iter = lastDeletedItems.entrySet().iterator();
         while (iter.hasNext()) {
            Block block = (Block) iter.next();
             ArrayList<Block> blocksToRemove = new ArrayList<Block>();
             
             // Checks if the player's metadata stores the same hashCode as the hashCode of the block
             // (i.e. the same chest is being referenced).
            if(player.getMetadata("ChestBackupID").get(0).asInt() == block.hashCode())
                 continue;
 
             // Removes the hashcode of the chest from the player's metadata.
             player.removeMetadata("ChestBackupID", this);
             blocksToRemove.add(block);
             
             // Checks to make sure the block has not been converted into another block since
             // the chest was emptied.
             if(block.getType() != Material.CHEST) {
                 sender.sendMessage("Cannot restore inventory: block no longer a chest.");
                 continue;
             }
             
             Chest chest = (Chest) block.getState();
             
             // Returns the deleted items into the chest.
             // This should be tested for what happens when the chest is full and items get
             // restored.
         restore:
             for(ItemStack itemStack : lastDeletedItems.get(block)) {
                 if(itemStack == null)
                     continue restore;
                 
                 chest.getInventory().addItem(itemStack);
             }
 
             // Removes any restored chests from the cache.
             iter.remove();
             sender.sendMessage("Items successfully restored.");
             return;
         }
     }
 
     public void addChestInventoryBackup(Chest chest, Player player)
     {
         Block block = (Block) chest.getBlock();
         
         // Searches for, and removes any previously added inventory backups for that block.
         Iterator<Entry<Block, ItemStack[]>> iter = lastDeletedItems.entrySet().iterator();
         while (iter.hasNext())
             // If the chest has already been backed up, remove the iterator.
             if (iter.next().getKey().hashCode() == block.hashCode()) iter.remove();
         
         // Adds the inventory of a chest into the cache, and stores its hashCode in the player's metadata.
         player.setMetadata("ChestBackupID", new FixedMetadataValue(this, block.hashCode()));
         lastDeletedItems.put(block, chest.getInventory().getContents());
     }
 
     public void toggleSelecting(String playerName, CommandSender sender)
     {
         // Toggles whether or not the player is currently in selection mode.
         if(isSelecting(playerName)) {
             playersSelecting.remove(playerName);
             sender.sendMessage("Selection mode disabled.");
         }
         else {
             playersSelecting.add(playerName);
             sender.sendMessage("Selection mode activated.");
         }
     }
    
     public boolean isSelecting(String playerName) {
         // Checks to see if the player is currently in selection mode.
         return playersSelecting.contains(playerName);
     }
 }
