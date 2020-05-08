 package net.betterverse.spawnerbox;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.CreatureSpawner;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class SpawnerBox extends JavaPlugin implements Listener {
     private final Map<String, SpawnerEditor> editing = new HashMap<String, SpawnerEditor>();
 
     @Override
     public boolean onCommand(CommandSender sender, Command cmd, String cmdLabel, String[] args) {
         if (!(sender instanceof Player)) {
             sender.sendMessage("You must be in game to use /sb!");
             return true;
         }
 
         Player player = (Player) sender;
         if (args.length == 1 && args[0].equals("set")) {
             if (!player.hasPermission("spawnerbox.set")) {
                player.sendMessage(ChatColor.RED + "You do not have permission.");
                 return true;
             }
             // Toggle player's editor
             if (editing.containsKey(player.getName())) {
                 editing.remove(player.getName());
                 player.sendMessage(ChatColor.GREEN + "Exited the spawner editor.");
             } else {
                 SpawnerEditor editor = new SpawnerEditor(player);
                 if (editor.canEdit()) {
                     editing.put(player.getName(), editor);
                     player.sendMessage(ChatColor.GREEN
                             + "Right-click a mob spawner to change the mob that it will spawn.");
                 } else {
                    player.sendMessage(ChatColor.RED + "You do not have permission.");
                 }
             }
         } else {
            player.sendMessage(ChatColor.RED + "Invalid command. /sb set");
         }
 
         return true;
     }
 
     @Override
     public void onDisable() {
         log(toString() + " disabled.");
     }
 
     @Override
     public void onEnable() {
         getConfig().options().copyDefaults(true);
         saveConfig();
 
         getServer().getPluginManager().registerEvents(this, this);
 
         log(toString() + " enabled.");
     }
 
     @Override
     public String toString() {
         StringBuilder builder = new StringBuilder(getDescription().getName() + " v" + getDescription().getVersion()
                 + " [Written by: ");
         for (int i = 0; i < getDescription().getAuthors().size(); i++) {
             builder.append(getDescription().getAuthors().get(i)
                     + (i + 1 != getDescription().getAuthors().size() ? ", " : ""));
         }
         builder.append("]");
 
         return builder.toString();
     }
 
     @EventHandler
     public void onBlockBreak(BlockBreakEvent event) {
         if (!event.isCancelled() && event.getBlock().getType() == Material.MOB_SPAWNER) {
             if (!event.getPlayer().hasPermission("spawnerbox.break")) {
                 event.setCancelled(true);
                 event.getPlayer().sendMessage(ChatColor.RED + "You do not have permission to break monster spawners.");
                 return;
             }
 
             Block block = event.getBlock();
             // Drop item with the proper entity ID
             block.getWorld().dropItemNaturally(
                     block.getLocation(),
                     new ItemStack(Material.MOB_SPAWNER, 1, ((CreatureSpawner) block.getState()).getSpawnedType()
                             .getTypeId()));
         }
     }
 
     @EventHandler
     public void onBlockPlace(final BlockPlaceEvent event) {
         if (!event.isCancelled() && event.getBlock().getType() == Material.MOB_SPAWNER) {
 
             Block block = event.getBlock();
             // Spawner limits as defined in the configuration file
             if (getSpawnersWithinRadiusOfBlock(block) > getConfig().getInt("maximum-spawners-within-radius.spawners")) {
                 event.getPlayer().sendMessage(ChatColor.RED + "This region has reached a maximum amount of spawners.");
                 event.setCancelled(true);
             }
 
             // Set the spawned type
             getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
 
                 @Override
                 public void run() {
                     CreatureSpawner spawner = (CreatureSpawner) event.getBlockPlaced().getState();
                     spawner.setSpawnedType(EntityType.fromId(event.getItemInHand().getData().getData()));
                     // Update the client
                     spawner.update();
                 }
             });
         }
     }
 
     @EventHandler
     public void onPlayerQuit(PlayerQuitEvent event) {
         // Exit editor if player quits the game
         if (editing.containsKey(event.getPlayer().getName())) {
             editing.remove(event.getPlayer().getName());
         }
     }
 
     @EventHandler
     public void onPlayerInteract(PlayerInteractEvent event) {
         Player player = event.getPlayer();
         if (editing.containsKey(player.getName())) {
             editing.get(player.getName()).onInteract(event);
         }
     }
 
     private void log(String message) {
         getServer().getLogger().info("[SpawnerBox] " + message);
     }
 
     private int getSpawnersWithinRadiusOfBlock(Block center) {
         int x = center.getX();
         int y = center.getY();
         int z = center.getZ();
         int radius = getConfig().getInt("maximum-spawners-within-radius.radius");
         int totalSpawners = 0;
         // Loop through all blocks within the radius of the center block
         for (int xx = x - radius; xx < x + radius; xx++) {
             for (int yy = y - radius; yy < y + radius; yy++) {
                 for (int zz = z - radius; zz < z + radius; zz++) {
                     if (center.getWorld().getBlockAt(xx, yy, zz).getType() == Material.MOB_SPAWNER) {
                         totalSpawners++;
                     }
                 }
             }
         }
 
         return totalSpawners;
     }
 }
