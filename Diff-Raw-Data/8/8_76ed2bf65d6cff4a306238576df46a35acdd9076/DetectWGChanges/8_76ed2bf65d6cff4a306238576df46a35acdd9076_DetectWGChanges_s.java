 package com.geNAZt.RegionShop.Data.Tasks;
 
 import com.geNAZt.RegionShop.Config.ConfigManager;
 import com.geNAZt.RegionShop.Database.Model.Region;
 import com.geNAZt.RegionShop.Events.WGChangeRegionEvent;
 import com.geNAZt.RegionShop.Events.WGNewRegionEvent;
 import com.geNAZt.RegionShop.Events.WGRemoveRegionEvent;
 import com.geNAZt.RegionShop.Listener.WGChanges;
 import com.geNAZt.RegionShop.RegionShopPlugin;
 import com.sk89q.worldedit.BlockVector;
 import com.sk89q.worldguard.bukkit.WGBukkit;
 import com.sk89q.worldguard.protection.regions.ProtectedRegion;
 import org.bukkit.Bukkit;
 import org.bukkit.World;
 import org.bukkit.scheduler.BukkitRunnable;
 
 import java.util.*;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * Created for YEAHWH.AT
  * User: fabian
  * Date: 02.09.13
  */
 public class DetectWGChanges extends BukkitRunnable {
     //Save the State of the last run
     private HashMap<World, ArrayList<String>> lastCheckState = new HashMap<World, ArrayList<String>>();
     //Create the test Pattern
     private Pattern regex = Pattern.compile("(.*)" + ConfigManager.expert.Misc_regexPattern + "(.*)");
     //Hold the instance of the Plugin
     private RegionShopPlugin plugin;
 
     public DetectWGChanges(RegionShopPlugin plugin) {
         this.plugin = plugin;
     }
 
     @Override
     public void run() {
         //Get all worlds
         List<World> worldList = Bukkit.getServer().getWorlds();
         for(World world : worldList) {
             //Check if World is enabled
             if(!ConfigManager.main.World_enabledWorlds.contains(world.getName())) {
                 //Check if old data is in the list
                 if(lastCheckState.containsKey(world)) {
                     lastCheckState.remove(world);
                 }
 
                 continue;
             }
 
             //Create temporary null ArrayList
             ArrayList<String> worldRegions;
 
             //Check if world is in lastCheckState list
             if(!lastCheckState.containsKey(world)) {
                 worldRegions = new ArrayList<String>();
             } else {
                 worldRegions = lastCheckState.get(world);
             }
 
             //Get all Regions currently registered in this World
             Map<String, ProtectedRegion> wgRegions = WGBukkit.getRegionManager(world).getRegions();
 
             //Check all wgRegions
             for(Map.Entry<String, ProtectedRegion> region : wgRegions.entrySet()) {
                 Matcher matcher = regex.matcher(region.getKey());
 
                 //Check if region is a ShopRegion
                 if(matcher.matches()) {
                     //Is this region new ?
                     if(!worldRegions.contains(region.getKey())) {
                         //Region is not in the list but maybe it is invalid ?
                         if(region.getValue().getOwners().getPlayers().isEmpty()) {
                             continue;
                         }
 
                         //This is a new Region
                         worldRegions.add(region.getKey());
 
                         //Generate a new Event
                         final WGNewRegionEvent wgNewRegionEvent = new WGNewRegionEvent(region.getValue(), world);
 
                         //Shedule the change
                         WGChanges.newRegion(wgNewRegionEvent);
                     } else {
                         //Has the region changed ?
                         com.geNAZt.RegionShop.Database.Table.Region region1 = Region.get(region.getValue(), world);
 
                         //Get the position markers
                         BlockVector min = region.getValue().getMinimumPoint();
                         BlockVector max = region.getValue().getMaximumPoint();
 
                         //Check if region changed
                         if(
                             region1.getMinX() != min.getX() ||  //Check minimal points
                             region1.getMinY() != min.getY() ||
                             region1.getMinZ() != min.getZ() ||
                             region1.getMaxX() != max.getX() ||  //Check maximal points
                             region1.getMaxY() != max.getY() ||
                             region1.getMaxZ() != max.getZ() ||
                             region1.getOwners().size() != region.getValue().getOwners().getPlayers().size() || //Size of owners has changed
                             region1.getMembers().size() != region.getValue().getMembers().getPlayers().size() // Size of members has changed
                           ) {
                             //Generate a new Event
                             final WGChangeRegionEvent wgChangeRegionEvent = new WGChangeRegionEvent(region.getValue(), world);
 
                             //Shedule the change
                             WGChanges.changeRegion(wgChangeRegionEvent);
                         }
                     }
                 }
             }
 
             //Check if Regions where removed
             for(String region : worldRegions) {
                 //This region is deleted ?
                 if(!wgRegions.containsKey(region)) {
                     //Remove it in our collection
                     worldRegions.remove(region);
 
                     //Generate a new Event
                     final WGRemoveRegionEvent wgRemoveRegionEvent = new WGRemoveRegionEvent(region, world);
 
                     //Shedule the event
                     WGChanges.removeRegion(wgRemoveRegionEvent);
                 }
             }
 
 
             //Save the HashMap
             lastCheckState.put(world, worldRegions);
         }
     }
 }
