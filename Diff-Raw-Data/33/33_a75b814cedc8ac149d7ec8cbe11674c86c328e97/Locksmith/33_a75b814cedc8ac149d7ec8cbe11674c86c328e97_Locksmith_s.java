 package edgruberman.bukkit.simplelocks;
 
 import java.util.Arrays;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 import java.util.logging.Level;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.block.Chest;
 import org.bukkit.event.Event.Result;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.Plugin;
 
 public class Locksmith implements Listener {
 
    public static int MAXIMUM_SIGN_LINE_LENGTH = 15;
 
     /** text on the first line of the sign that indicates it is a lock */
     public final String title;
     public final Map<String, String> substitutions = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);
 
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
 
     public String getSubstitution(final String name) {
         final String substitution = this.substitutions.get(name);
         if (substitution != null) return substitution;
 
         return name;
     }
 
     @EventHandler(ignoreCancelled = true)
     public void onPlayerInteract(final PlayerInteractEvent interaction) {
 
         // left clicking a lock or locked block will describe the lock
         if (interaction.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
             final Lock lock = this.findLock(interaction.getClickedBlock());
             if (lock == null) return;
 
             Main.courier.send(interaction.getPlayer(), "describe"
                     , lock.getAccess().toString().replaceAll("^\\[|\\]$", ""), lock.hasAccess(interaction.getPlayer())?1:0);
             return;
         }
 
         if (!interaction.getAction().equals(Action.RIGHT_CLICK_BLOCK) && !interaction.getAction().equals(Action.RIGHT_CLICK_AIR)) return;
 
         final Lock lock = this.findLock(interaction.getClickedBlock());
         if (lock != null) {
             // existing lock found
 
             if (!lock.hasAccess(interaction.getPlayer())) {
                 // player does not have access, cancel interaction and notify player
                 interaction.setCancelled(true);
                 Main.courier.send(interaction.getPlayer(), "denied", lock.getAccess().toString().replaceAll("^\\[|\\]$", ""));
                 this.plugin.getLogger().log(Level.FINEST, "Lock access denied to {0} at {1}", new Object[] { interaction.getPlayer().getName(), interaction.getClickedBlock() });
                 return;
             }
 
             // player has access, if they did not click directly on lock, let things proceed as normal
             if (interaction.getClickedBlock().getType() != Material.WALL_SIGN) return;
 
             // player has access and they right clicked on lock, pass through to locked chest
             final Chest chest = (Chest) lock.getLocked().getState();
             interaction.getPlayer().openInventory(chest.getInventory());
             interaction.setUseItemInHand(Result.DENY);
             interaction.setUseInteractedBlock(Result.DENY);
             return;
         }
 
         // no existing lock, check to see if player is requesting a lock be created
         // must be holding a sign and right click on a vertical face of the chest that is adjacent to air
         if (!interaction.getPlayer().hasPermission("simplelocks.create")) return;
         if (interaction.getClickedBlock().getType() != Material.CHEST || interaction.getMaterial() != Material.SIGN) return;
         if (interaction.getBlockFace() == BlockFace.UP || interaction.getBlockFace() == BlockFace.DOWN) return;
         if (interaction.getClickedBlock().getRelative(interaction.getBlockFace()).getType() != Material.AIR) return;
 
         // give other plugins a chance to cancel this sign creation as a standard block place event
         final Block block = interaction.getClickedBlock().getRelative(interaction.getBlockFace());
         final ItemStack remaining = interaction.getPlayer().getItemInHand();
         final BlockPlaceEvent place = new BlockPlaceEvent(block, block.getState(), interaction.getClickedBlock(), remaining, interaction.getPlayer(), true);
         Bukkit.getPluginManager().callEvent(place);
         if (place.isCancelled()) return;
 
         // check for default owner substitute (Long names won't fit on a sign)
         final String owner = this.getSubstitution(interaction.getPlayer().getName());
         if (owner.length() > Locksmith.MAXIMUM_SIGN_LINE_LENGTH) {
             Main.courier.send(interaction.getPlayer(), "nameTooLong", owner, owner.length(), Locksmith.MAXIMUM_SIGN_LINE_LENGTH);
             return;
         }
 
         final BlockFace attached = interaction.getBlockFace().getOppositeFace();
         final LockCreate custom = new LockCreate(block, attached, owner, interaction.getPlayer());
         Bukkit.getPluginManager().callEvent(custom);
         if (custom.isCancelled()) return;
 
         // remove only 1 sign from player's hand
         remaining.setAmount(remaining.getAmount() - 1);
         interaction.getPlayer().setItemInHand(remaining);
 
         this.createLock(block, attached, owner);
         interaction.setUseInteractedBlock(Result.DENY); // don't open the chest
     }
 
     /** cancel block break if lock/locked and not owner */
     @EventHandler(ignoreCancelled = true)
     public void onBlockBreak(final BlockBreakEvent broken) {
         final Lock lock = this.findLock(broken.getBlock());
         if (lock == null) return;
 
         // Only those with lock access can break a lock
         if (lock.hasAccess(broken.getPlayer())) return;
 
         broken.setCancelled(true);
         Main.courier.send(broken.getPlayer(), "denied", lock.getAccess().toString().replaceAll("^\\[|\\]$", ""));
         this.plugin.getLogger().log(Level.FINEST, "Cancelled block break by {0} to protect lock at {1}", new Object[] { broken.getPlayer().getName(), broken.getBlock() });
 
         lock.sign.update();
     }
 
 }
