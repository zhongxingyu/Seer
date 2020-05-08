 package edgruberman.bukkit.simplelocks;
 
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.List;
 
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event.Result;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.entity.EntityExplodeEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.Plugin;
 
 public class Locksmith implements Listener {
 
     public static int MAXIMUM_SIGN_LINE_LENGTH = 15;
 
     /** text on the first line of the sign that indicates it is a lock */
     public final String title;
 
     private final Plugin plugin;
 
     Locksmith(final Plugin plugin, final String title) {
         this.plugin = plugin;
         this.title = title;
         plugin.getServer().getPluginManager().registerEvents(this, plugin);
     }
 
     /**
      * Create new lock
      *
      * @param block where to create sign containing lock information
      * @param attached face adjacent to lockable
      * @param owner player name or group name
      */
     Lock createLock(final Block block, final BlockFace attached, final String owner) {
         return new Lock(this, block, attached, owner);
     }
 
     /**
      * Indicates if block is a lock for any block
      *
      * @param block block to check
      * @return true if lock; false otherwise
      */
     boolean isLock(final Block block) {
         return this.isLock(block, null);
     }
 
     /**
      * Indicates if block is a lock
      *
      * @param block block to check
      * @param attached face connected to locked object (null to determine if block is a lock for any block)
      * @return true if lock; false otherwise
      */
     private boolean isLock(final Block block, final BlockFace attached) {
         // Locks must be a wall sign
         if (block.getTypeId() != Material.WALL_SIGN.getId()) return false;
 
         // Locks must be directly attached to locked block
         if (attached != null) {
             final org.bukkit.material.Sign material = (org.bukkit.material.Sign) block.getState().getData();
             if (!material.getAttachedFace().equals(attached)) return false;
         }
 
         // First line of sign must contain standard lock title
         if (!((org.bukkit.block.Sign) block.getState()).getLine(0).equals(this.title)) return false;
 
         return true;
     }
 
     /**
      * Indicates if block is actively locked by a lock, but not a lock itself.
      *
      * @param block block to check
      * @return true if block is locked; false otherwise
      */
     boolean isLocked(final Block block) {
         return this.findChestLock(block) != null;
     }
 
     /**
      * Find lock for block, which could be the block itself
      *
      * @param block block to check
      * @return Lock associated with block; null if none
      */
     public Lock findLock(final Block block) {
         if (this.isLock(block)) return new Lock(this, block);
 
         final Block lock = this.findChestLock(block);
         if (lock == null) return null;
 
         return new Lock(this, lock);
     }
 
     /**
      * Returns the lock associated with the chest
      *
      * @param chest block (small or either side of a large)
      * @return Block containing lock associated to chest; null if none
      */
     private Block findChestLock(final Block chest) {
         if (chest.getTypeId() != Material.CHEST.getId()) return null;
 
         final List<BlockFace> cardinals = Arrays.asList(BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST);
 
         // Check directly adjacent blocks for lock
         for (final BlockFace direction : cardinals) {
             final Block relative = chest.getRelative(direction);
             if (this.isLock(relative, direction.getOppositeFace())) return relative;
         }
 
         // Check directly adjacent blocks for second half of double chest
         for (final BlockFace direction : cardinals) {
             final Block relative = chest.getRelative(direction);
             if (relative.getTypeId() == Material.CHEST.getId()) {
 
                 // Found double chest - Check directly adjacent blocks to second half of chest for lock
                 for (final BlockFace direction2 : cardinals) {
                     final Block relative2 = relative.getRelative(direction2);
                     if (this.isLock(relative2, direction2.getOppositeFace())) return relative2;
                 }
 
             }
         }
 
         return null;
     }
 
     String getDefaultOwner(final Player player) {
         final ConfigurationSection defaultOwners = this.plugin.getConfig().getConfigurationSection("defaultOwners");
         if (defaultOwners == null) return player.getName();
 
         return defaultOwners.getString(player.getName(), player.getName());
     }
 
     @EventHandler(ignoreCancelled = true)
     public void onPlayerInteract(final PlayerInteractEvent interaction) {
         if (!interaction.getAction().equals(Action.RIGHT_CLICK_BLOCK)) return;
 
         final Lock lock = this.findLock(interaction.getClickedBlock());
         if (lock != null) {
             // Existing lock found
 
             if (!lock.hasAccess(interaction.getPlayer())) {
                 // Player does not have access, cancel interaction and notify player
                 interaction.setCancelled(true);
                 Main.courier.send(interaction.getPlayer(), "denied");
                 this.plugin.getLogger().finest(
                         "Lock access denied to " + interaction.getPlayer().getName() + " at "
                             + " x:" + interaction.getClickedBlock().getX()
                             + " y:" + interaction.getClickedBlock().getY()
                             + " z:" + interaction.getClickedBlock().getZ()
                 );
                 return;
             }
 
             // Player has access and they right clicked on the lock itself so give them information
             if (this.isLock(interaction.getClickedBlock()))
                 Main.courier.send(interaction.getPlayer(), (lock.isOwner(interaction.getPlayer()) ? "owner" : "access"));
 
             return;
         }
 
         // No existing lock, check to see if player is requesting a lock be created
         if (interaction.getClickedBlock().getType().equals(Material.CHEST)) {
             if (interaction.getMaterial().equals(Material.SIGN)
                     && interaction.getClickedBlock().getRelative(interaction.getBlockFace()).getType().equals(Material.AIR)
                     && !interaction.getBlockFace().equals(BlockFace.UP) && !interaction.getBlockFace().equals(BlockFace.DOWN)) {
 
                 // Right click on a chest with a sign to create lock automatically
                 interaction.setUseInteractedBlock(Result.DENY); // Don't open the chest
 
                 // Check for default owner substitute (Long names won't fit on a sign)
                 final String ownerName = this.getDefaultOwner(interaction.getPlayer());
                 if (ownerName.length() > 15) {
                     Main.courier.send(interaction.getPlayer(), "createNameTooLong");
                     return;
                 }
 
                final ItemStack remaining = interaction.getPlayer().getItemInHand();
                remaining.setAmount(remaining.getAmount() - 1);
                interaction.getPlayer().setItemInHand(remaining);

                 this.createLock(interaction.getClickedBlock().getRelative(interaction.getBlockFace()), interaction.getBlockFace().getOppositeFace(), ownerName);
             }
         }
     }
 
     /** cancel block break if lock/locked and not owner */
     @EventHandler(ignoreCancelled = true)
     public void onBlockBreak(final BlockBreakEvent broken) {
         final Lock lock = this.findLock(broken.getBlock());
         if (lock == null) return;
 
         // Allow lock owner to break lock
         if (lock.isOwner(broken.getPlayer())) return;
 
         broken.setCancelled(true);
         Main.courier.send(broken.getPlayer(), "removeDenied", lock.getOwner());
         this.plugin.getLogger().finest(
                 "Cancelled block break to protect lock at"
                     + " x:" + broken.getBlock().getX()
                     + " y:" + broken.getBlock().getY()
                     + " z:" + broken.getBlock().getZ()
         );
 
         lock.refresh(); // TODO ? add timed refresh so it updates after event processes/reverts
     }
 
     /** remove any locks or locked blocks */
     @EventHandler(ignoreCancelled = true)
     public void onEntityExplode(final EntityExplodeEvent explosion) {
         final Iterator<Block> affected = explosion.blockList().iterator();
         while (affected.hasNext()) {
             final Block block = affected.next();
             if (this.isLock(block) || this.isLocked(block)) {
                 affected.remove();
                 this.plugin.getLogger().finest("Protected lock/locked block from explosion at" + " x:" + block.getX() + " y:" + block.getY() + " z:" + block.getZ());
             }
         }
     }
 
 }
