 package com.geNAZt.RegionShop.Data.Tasks;
 
 import com.geNAZt.RegionShop.Config.ConfigManager;
 import com.geNAZt.RegionShop.Config.Sub.ServerShop;
 import com.geNAZt.RegionShop.Database.Database;
 import com.geNAZt.RegionShop.Database.Model.Item;
 import com.geNAZt.RegionShop.Database.Table.Chest;
 import com.geNAZt.RegionShop.Database.Table.CustomerSign;
 import com.geNAZt.RegionShop.Database.Table.Items;
 import com.geNAZt.RegionShop.RegionShopPlugin;
 import com.geNAZt.RegionShop.Util.NMS;
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.entity.Entity;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.scheduler.BukkitRunnable;
 import org.bukkit.util.Vector;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 
 /**
  * @author geNAZt (fabian.fassbender42@googlemail.com)
  * @date Last changed: 23.10.13 11:56
  */
 public class ShowcaseRefresh extends BukkitRunnable {
     private HashMap<String, Long> lastTimeRenewed = new HashMap<String, Long>();
 
     @Override
     public void run() {
         //Loop through all Worlds
         for (final World world : Bukkit.getWorlds()) {
             //Get all DB Entries which could have a Showcase in this World
             final List<CustomerSign> customerSigns = Database.getServer().find(CustomerSign.class).where().eq("region.world", world.getName()).findList();
             final List<Chest> chests = Database.getServer().find(Chest.class).where().eq("world", world.getName()).findList();
 
             RegionShopPlugin.getInstance().getServer().getScheduler().scheduleSyncDelayedTask(RegionShopPlugin.getInstance(), new BukkitRunnable() {
                 @Override
                 public void run() {
                     //Check if a CustomerSign is above
                     for (final CustomerSign customerSign : customerSigns) {
                         //Load the ServerShop, only CustomerSigns with ServerShop can have Showcases
                         ServerShop serverShop = ConfigManager.servershop.getServerShopByRegion(customerSign.getRegion().getRegion());
                         if (serverShop == null) continue;
 
                         //Check if CustomerSign is in lastTimeRenewed Map
                         if(!lastTimeRenewed.containsKey("cs" + customerSign.getId())) {
                             lastTimeRenewed.put("cs" + customerSign.getId(), System.currentTimeMillis());
                         }
 
                         //Loop through all Entities located in the World
                         boolean found = false;
                         for (final Entity ent : world.getEntities()) {
                             //Get the location of this Entity
                             Location entLocation = ent.getLocation();
                             if (entLocation.getBlockZ() == customerSign.getZ() && entLocation.getBlockY() == customerSign.getY() - 1 && entLocation.getBlockX() == customerSign.getX()) {
                                 found = true;
 
                                 if(System.currentTimeMillis() - lastTimeRenewed.get("cs" + customerSign.getId()) > 60 * 60 * 1000) {
                                     lastTimeRenewed.put("cs" + customerSign.getId(), System.currentTimeMillis());
                                     ent.remove();
                                     found = false;
                                 }
                             }
                         }
 
                         //Check if a new Entity needs to be dropped
                         if (serverShop.Showcase && !found) {
                             //Get the ItemStack out of the Database
                             ItemStack itemStack = Item.fromDBItem(customerSign.getItem());
 
                             //Drop the Item
                             org.bukkit.entity.Item droppedItem = world.dropItem(new Location(world, (double) customerSign.getX() + 0.5, (double) customerSign.getY() - 0.8, (double) customerSign.getZ() + 0.5), itemStack);
                             droppedItem.setVelocity(new Vector(0, 0.1, 0));
                             droppedItem.setPickupDelay(Integer.MAX_VALUE);
                             NMS.safeGuard(droppedItem);
                         }
                     }
 
                     //Check if a Chest is underneath
                     for(final Chest chest : chests) {
                         //Check if Checst is in lastTimeRenewed Map
                         if(!lastTimeRenewed.containsKey("ch" + chest.getId())) {
                             lastTimeRenewed.put("ch" + chest.getId(), System.currentTimeMillis());
                         }
 
                         //Loop through all Entities located in the World
                         boolean found = false;
                         for (final Entity ent : world.getEntities()) {
                             //Get the location of this Entity
                             Location entLocation = ent.getLocation();
                             if (entLocation.getBlockZ() == chest.getChestZ() && entLocation.getBlockY() == chest.getChestY() + 1 && entLocation.getBlockX() == chest.getChestX()) {
                                 found = true;
 
                                 if(System.currentTimeMillis() - lastTimeRenewed.get("ch" + chest.getId()) > 60 * 60 * 1000) {
                                     lastTimeRenewed.put("ch" + chest.getId(), System.currentTimeMillis());
                                     ent.remove();
                                     found = false;
                                 }
                             }
                         }
 
                         //If not found spawn a new one
                         if(!found) {
                             //Check if Iterator is correct
                             Iterator<Items> itemsIterator = chest.getItemStorage().getItems().iterator();
                             if (!itemsIterator.hasNext()) {
                                 RegionShopPlugin.getInstance().getLogger().warning("Found Chest without item. Maybe wrong deletion: " + chest.getId());
                                 continue;
                             }
 
 
                             //Get the ItemStack out of the Database
                             ItemStack itemStack = Item.fromDBItem(itemsIterator.next());
 
                             //Drop the Item
                             org.bukkit.entity.Item droppedItem = world.dropItem(new Location(world, (double) chest.getChestX() + 0.5, (double) chest.getChestY() + 1.2, (double) chest.getChestZ() + 0.5), itemStack);
                             droppedItem.setVelocity(new Vector(0, 0.1, 0));
                             droppedItem.setPickupDelay(Integer.MAX_VALUE);
                             NMS.safeGuard(droppedItem);
                         }
                     }
                 }
             });
         }
     }
}
