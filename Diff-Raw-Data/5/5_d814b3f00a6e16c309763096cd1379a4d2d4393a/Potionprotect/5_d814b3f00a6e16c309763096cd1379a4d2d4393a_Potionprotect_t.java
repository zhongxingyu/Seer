 package me.tjs238.plugins.potionprotect;
 
 import com.sk89q.minecraft.util.commands.CommandException;
 import com.sk89q.worldedit.BlockVector;
 import com.sk89q.worldedit.IncompleteRegionException;
 import com.sk89q.worldedit.Vector;
 import com.sk89q.worldedit.bukkit.WorldEditPlugin;
 import com.sk89q.worldedit.bukkit.selections.Selection;
 import com.sk89q.worldedit.regions.CuboidRegion;
 import com.sk89q.worldedit.regions.Region;
 import com.sk89q.worldedit.regions.RegionSelector;
 import com.sk89q.worldedit.regions.*;
 import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
 import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
 import com.sk89q.worldguard.protection.regions.ProtectedRegion;
 import java.util.Random;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.Server;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Item;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.PotionSplashEvent;
 import org.bukkit.event.player.PlayerDropItemEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class Potionprotect extends JavaPlugin implements Listener {
     public Server server = Bukkit.getServer();
     private Logger log;
     private PluginDescriptionFile description;
     private String prefix;
     
    @Override
     public void onDisable() {
         // TODO: Place any custom disable code here.
     }
    
    @Override
     public void onEnable() {
         log = Logger.getLogger("Minecraft");
         description = getDescription();
         prefix = "["+description.getName()+"] ";
         log("Starting up...");
         getServer().getPluginManager().registerEvents(this, this);
     }
 
     @EventHandler
     public void onItemDrop(PlayerDropItemEvent event) throws IncompleteRegionException {
         if (event.isCancelled()) {
             return;
         }
         Player player = event.getPlayer();
         Item item = event.getItemDrop();
         WorldGuardPlugin worldGuard = (WorldGuardPlugin) getServer().getPluginManager().getPlugin("WorldGuard");
         WorldEditPlugin worldEdit = (WorldEditPlugin) getServer().getPluginManager().getPlugin("WorldEdit");
         
         if (item.equals(Material.POTION)) {
             event.setCancelled(true);
             Vector vector = new Vector();
             CuboidRegionSelector selector = new CuboidRegionSelector();
             Location pos1 = player.getLocation().add(10, 0, 10);
             Location pos2 = player.getLocation().subtract(10, 0, 10);
             Vector vpos1 = vector.add(pos1.getX(), pos1.getY(), pos1.getZ());
             Vector vpos2 = vector.add(pos2.getX(), pos2.getY(), pos2.getZ());
             selector.selectPrimary(vpos1);
             selector.selectSecondary(vpos2);
             
             Selection sel = worldEdit.getSelection(player);
             int oldSize = sel.getArea();
             Region region = sel.getRegionSelector().getRegion();
             try {
                 sel.getRegionSelector().getRegion().expand(
                         new Vector(0, (player.getWorld().getMaxHeight() + 1), 0),
                         new Vector(0, (player.getWorld().getMaxHeight() + 1), 0));
                 
                 // TODO: Add World Guard Dependancy
             } catch (RegionOperationException ex) {
                 Logger.getLogger(Potionprotect.class.getName()).log(Level.SEVERE, null, ex);
             }
             
             BlockVector min = sel.getNativeMinimumPoint().toBlockVector();
             BlockVector max = sel.getNativeMaximumPoint().toBlockVector();
             ProtectedRegion pr;
             Random generator2 = new Random( 19580427 );
             if (player.hasPermission("dc.protect")) {
                 pr = new ProtectedCuboidRegion(player.getName()+generator2, min, max);
             }
             
         }
     }
     
     public void log(String message){
         log.info(prefix+message);
     }
 }
 
