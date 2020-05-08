 package com.kolinkrewinkel.BitLimitTweaks;
 
 import org.bukkit.plugin.Plugin;
 import java.util.*;
 
 import org.bukkit.event.*;
 import org.bukkit.*;
 import org.bukkit.entity.*;
 import org.bukkit.event.entity.CreatureSpawnEvent.*;
 import org.bukkit.event.entity.*;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.metadata.*;
 import org.bukkit.block.Block;
 import org.bukkit.Effect;
 import org.bukkit.World;
 import org.bukkit.Location;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.Material;
 import org.bukkit.ChatColor;
 import org.bukkit.event.block.*;
 import org.bukkit.event.block.BlockPlaceEvent;
 
 import com.sk89q.worldedit.Vector;
 import com.sk89q.worldguard.LocalPlayer;
 import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
 import com.sk89q.worldguard.protection.managers.RegionManager;
 import com.sk89q.worldguard.protection.ApplicableRegionSet;
 import com.sk89q.worldguard.protection.regions.ProtectedRegion;
 import static com.sk89q.worldguard.bukkit.BukkitUtil.*;
 
 public class BitLimitTweaksListener implements Listener {
     private final BitLimitTweaks plugin; // Reference main plugin
 
     /*********************************************
     Initialization: BitLimitTweaksListener(plugin)
     ----------- Designated Initializer ----------
     *********************************************/
 
     public BitLimitTweaksListener(BitLimitTweaks plugin) {
         // Notify plugin manager that this plugin handles implemented events (block place, etc.)
         plugin.getServer().getPluginManager().registerEvents(this, plugin);
         this.plugin = plugin;
     }
 
     /*********************************************
       Event Handler: onCreatureSpawnEvent(Event)
     --------------- Event Handler --------------
     *********************************************/
 
     @EventHandler
     public void onCreatureSpawnEvent(CreatureSpawnEvent event) {
         // CreatureSpawnEvent (Entity spawnee, CreatureType type, Location loc, SpawnReason reason
 
         FileConfiguration config = this.plugin.getConfig();
         if (!config.getBoolean("enabled-slimes"))
             return;
 
         // Gather information to determine if these are the slimes we are looking for.
         EntityType entityType = event.getEntityType();
         SpawnReason reason = event.getSpawnReason();
         if (entityType == EntityType.SLIME && (reason == SpawnReason.NATURAL || reason == SpawnReason.SLIME_SPLIT))  {
             // Pseudo-randomly cancel slime spawns to reduce their numbers.
             boolean shouldCancel = getRandomBoolean();
             event.setCancelled(shouldCancel);
         }
     }
 
     /******************************************
     Event Handler: Block Place(BlockPlaceEvent)
     ----------- Core Event Listener -----------
     ******************************************/
     
     @EventHandler
     public void onBlockPlaceEvent(BlockPlaceEvent event) {
         // Event reference
         // BlockPlaceEvent(Block placedBlock, BlockState replacedBlockState, Block placedAgainst, ItemStack itemInHand, Player thePlayer, boolean canBuild) 
 
         boolean confinementEnabled = this.plugin.getConfig().getBoolean("enabled-tnt");
         if (event.getItemInHand().getTypeId() != 46 || !confinementEnabled)
             return;
 
         WorldGuardPlugin worldGuard = getWorldGuard();
         Block block = event.getBlockPlaced();
         Vector pt = toVector(block.getLocation());
         LocalPlayer localPlayer = worldGuard.wrapPlayer(event.getPlayer());
          
         RegionManager regionManager = worldGuard.getRegionManager(event.getPlayer().getWorld());
         ApplicableRegionSet set = regionManager.getApplicableRegions(pt);
         
         if (set.size() == 0)
             event.setCancelled(true);
         else
             event.setCancelled(!set.isOwnerOfAll(localPlayer));
         
        if (event.isCancelled())
             displaySmokeInWorldAtLocation(block.getWorld(), block.getLocation());
     }
 
     /******************************************
       Event Handler: Explosion Creation (TNT)
     ----------- Core Event Listener -----------
     ******************************************/
     
     @EventHandler
     public void onExplosionPrimeEvent(ExplosionPrimeEvent event) {
         // Event reference
         // ExplosionPrimeEvent (final Entity what, final float radius, final boolean fire)
 
         Entity entity = event.getEntity();
 
         if (entity instanceof TNTPrimed && this.plugin.getConfig().getBoolean("enabled-tnt")) {
             TNTPrimed tnt = (TNTPrimed)entity;
             List <Entity> nearbyEntities = tnt.getNearbyEntities(64, 128, 64); // check if player is horizontally within 4 chunks
             Iterator entityIterator = nearbyEntities.iterator();
 
             boolean playerNearby = false;
             while (entityIterator.hasNext()) {
                 Entity nextEntity = (Entity)entityIterator.next();
                 if (nextEntity instanceof Player) 
                     playerNearby = true;
             }
 
             // Required due to Bukkit's broken implementation of explosion prime - only checks if players are nearby so that *someone* is there to ensure it happened, though this includes the hypothetical griefer as well.
 
             if (!playerNearby) {
                 event.setCancelled(true);
                 displaySmokeInWorldAtLocation(tnt.getWorld(), tnt.getLocation());
                 ItemStack tntItem = new ItemStack(Material.TNT, 1);
                 tnt.getWorld().dropItemNaturally(tnt.getLocation(), tntItem);
 
                 List <Entity> distantEntities = tnt.getNearbyEntities(256, 128, 256); // check if player is horizontally within far render-distance zone
                 Iterator chunkEntities = distantEntities.iterator();
                 while (chunkEntities.hasNext()) {
                     Entity chunkEntity = (Entity)chunkEntities.next();
                     if (chunkEntity instanceof Player) {
                         Player chunkPlayer = (Player)chunkEntity;
                         chunkPlayer.sendMessage(ChatColor.RED + "TNT explosion within area failed - original item dropped at location.");
                         displaySmokeInWorldAtLocation(chunkPlayer.getWorld(), chunkPlayer.getLocation());
                     }
                 }
             }
         }
     }
 
     /******************************************
     External Getter: Returns World Guard Plugin
     ---------- Dependency Conveneince ---------
     ******************************************/
 
     private WorldGuardPlugin getWorldGuard() {
         Plugin plugin = this.plugin.getServer().getPluginManager().getPlugin("WorldGuard");
      
         // WorldGuard may not be loaded
         if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
             return null; // Maybe you want throw an exception instead
         }
      
         return (WorldGuardPlugin) plugin;
     }
 
     /*********************************************
     ------------ Conveinience Methods -----------
     *********************************************/
 
     public boolean getRandomBoolean() {
         Random random = new Random();
         return random.nextBoolean();
     }
 
     private void displaySmokeInWorldAtLocation(World world, Location location) {
           world.playEffect(location, Effect.MOBSPAWNER_FLAMES, 0);
     }
 }
 
