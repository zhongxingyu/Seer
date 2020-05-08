 package com.me.tft_02.dynamictextures;
 
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.LinkedList;
 
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 
 import com.sk89q.worldguard.protection.ApplicableRegionSet;
 import com.sk89q.worldguard.protection.managers.RegionManager;
 import com.sk89q.worldguard.protection.regions.ProtectedRegion;
 
 public class RegionUtils {
     public static HashMap<String, String> regionData = new HashMap<String, String>();
 
     public static boolean isTexturedRegion(Location location) {
         if (getRegionTexturePackUrl(getRegion(location)) != null) {
             return true;
         }
         return false;
     }
 
     public static String getRegionTexturePackUrl(String region) {
         String[] worldGuardRegions = DynamicTextures.getInstance().getConfig().getConfigurationSection("WorldGuard_Regions").getKeys(false).toArray(new String[0]);
 
         String url = null;
         for (String name : Arrays.asList(worldGuardRegions)) {
             if (region.equalsIgnoreCase("[" + name + "]")) {
                 String temp_url = DynamicTextures.getInstance().getConfig().getString("WorldGuard_Regions." + name);
                 if ((temp_url.contains("http://") || temp_url.contains("https://")) && temp_url.contains(".zip")) {
                     url = DynamicTextures.getInstance().getConfig().getString("WorldGuard_Regions." + name);
                 }
             }
         }
 
         return url;
     }
 
     public static String getRegion(Location location) {
         RegionManager regionManager = DynamicTextures.getInstance().getWorldGuard().getRegionManager(location.getWorld());
         ApplicableRegionSet set = regionManager.getApplicableRegions(location);
         LinkedList<String> parentNames = new LinkedList<String>();
         LinkedList<String> regions = new LinkedList<String>();
 
         for (ProtectedRegion region : set) {
             String id = region.getId();
             regions.add(id);
             ProtectedRegion parent = region.getParent();
             while (parent != null) {
                 parentNames.add(parent.getId());
                 parent = parent.getParent();
             }
         }
 
         for (String name : parentNames) {
             regions.remove(name);
         }
         return regions.toString();
     }
 
     public static String getPreviousRegion(Player player) {
         String playerName = player.getName();
        String region;
 
         if (regionData.containsKey(playerName)) {
             region = regionData.get(playerName);
         }
        else {
            region = null;
        }
         return region;
     }
 }
