 /**
  * SakuraCmd - Package: net.syamn.sakuracmd.listener
  * Created: 2013/01/11 6:28:51
  */
 package net.syamn.sakuracmd.listener;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Locale;
 
 import net.syamn.sakuracmd.SakuraCmd;
 import net.syamn.sakuracmd.manager.Worlds;
 import net.syamn.sakuracmd.permission.Perms;
 import net.syamn.utils.Util;
 
 import org.bukkit.GameMode;
 import org.bukkit.Material;
 import org.bukkit.World.Environment;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockPhysicsEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.inventory.ItemStack;
 
 /**
  * BlockListener (BlockListener.java)
  * @author syam(syamn)
  */
 public class BlockListener implements Listener{
     private SakuraCmd plugin;
     public BlockListener (final SakuraCmd plugin){
         this.plugin = plugin;
     }
     
     @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
     public void stopObsidianGeneratorInNether(final BlockPhysicsEvent event) {
         final Block block = event.getBlock();
         if (Environment.NETHER.equals(block.getLocation().getWorld().getEnvironment())) {
             if (block.getType() == Material.STATIONARY_LAVA && event.getChangedType() == Material.WATER) {
                 event.setCancelled(true);
             }
         }
     }
     
     @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
     public void onBlockPlaceNetherTop(final BlockPlaceEvent event) {
         final Block block = event.getBlock();
         if (!block.getWorld().getName().equals(Worlds.main_nether)){
             return;
         }
         
         if (block.getY() >= 127 && !Perms.PLACE_NETHER_TOP.has(event.getPlayer())){
             Util.message(event.getPlayer(), "&cメインネザーの127以上には建築できません！");
             event.setCancelled(true);
         }
     }
     
     @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
     public void onBlockBreak(final BlockBreakEvent event) {
         final Block block = event.getBlock();
         final Player player = event.getPlayer();
         
         // Ice to water
         if (block.getTypeId() == 79 && Environment.NETHER.equals(block.getLocation().getWorld().getEnvironment())) {
             if (Perms.ICE_TO_WATER.has(player) && GameMode.SURVIVAL.equals(player.getGameMode())) {
                 plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                     public void run() {
                         block.setTypeId(8, true); // set water
                     }
                 }, 0L);
             }
         }
         // Skull to air
         if (block.getTypeId() == 144){
             event.setCancelled(true);
             block.setType(Material.AIR);
         }
     }
     
     @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
     public void onPumpkinBreak(final BlockBreakEvent event) {
         final Player player = event.getPlayer();
         final Block block = event.getBlock();
         
         // アイテムID 86, 91 以外は返す
         if (!Worlds.isResource(block.getWorld().getName().toLowerCase(Locale.ENGLISH)) || (block.getTypeId() != 86 && block.getTypeId() != 91)) {
             return;
         }
         
         // 手持ちアイテムチェック
         final ItemStack is = player.getItemInHand();
         if (is == null || is.getType() != Material.STICK) {
             return;
         }
         
         // 爆発させる
         block.breakNaturally();
         block.getWorld().createExplosion(block.getLocation(), (float) 0.0, false);
         checkNextTicks(block, true);
     }
    final BlockFace[] oumpkinSearchDirs = new BlockFace[] { BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN };
     private void checkNextTicks(final Block block, final boolean first) {
         if (block == null || (!first && (block.getTypeId() != 86 && block.getTypeId() == 91))) { return; }
         plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
             @Override
             public void run() {
                 List<Block> nexts = new ArrayList<Block>();
                 
                 Block check;
                for (final BlockFace face : oumpkinSearchDirs) {
                     check = block.getRelative(face);
                     if (check.getTypeId() == 86 || check.getTypeId() == 91) {
                         if (Math.random() > 0.5)
                             check.breakNaturally();
                         else
                             check.setTypeId(0);
                         
                         block.getWorld().createExplosion(block.getLocation(), 0.1F, false);
                         nexts.add(check);
                     }
                 }
                 for (final Block next : nexts) {
                     checkNextTicks(next, false);
                 }
             }
         }, 4L);
     }
     
     /*
     @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
     public void onBlockRedstone(final BlockRedstoneEvent event) {
         final Block block = event.getBlock();
         final BlockState state = event.getBlock().getState();
         
         if (state instanceof Sign && event.getNewCurrent() > 0) {
             final Sign sign = (Sign) state;
             
             if (sign.getLine(0).equals("§1[Sound]")) {
                 final Location bloc = block.getLocation();
                 
                 // get sound
                 final String sound = sign.getLine(1) + sign.getLine(2);
                 
                 // get volume, radius
                 String[] line4 = sign.getLine(3).split(":");
                 if (line4.length != 2 || !Util.isFloat(line4[0]) || !Util.isDouble(line4[1])) return;
                 float vol = Float.parseFloat(line4[0]);
                 final double radius = Double.parseDouble(line4[1]);
                 
                 if (sound.length() <= 0 || vol <= 0F || radius <= 0D) return;
                 
                 for (Player player : block.getWorld().getPlayers()) {
                     Location ploc = player.getLocation();
                     if (ploc.distance(bloc) > radius) {
                         continue;
                     }
                     
                     ((CraftPlayer) player).getHandle().playerConnection.sendPacket(new Packet62NamedSoundEffect(sound, ploc.getX(), ploc.getY(), ploc.getZ(), vol, 1.0F));
                     
                 }
             }
         }
     }
      */
 }
