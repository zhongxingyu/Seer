 /*  Craft Inc. BorderProtection
     Copyright (C) 2013  Paul Schulze, Tobias Ottenweller
 
     This program is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
 package de.craftinc.borderprotection;
 
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.configuration.serialization.ConfigurationSerializable;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 public class Border implements ConfigurationSerializable
 {
     private static final String dataFileName = "borders.yml";
 
 
     private Boolean isActive;
     private static String isActiveKey = "enabled";
 
     private Location rectPoint1;
     private static String rectPoint1Name = "p1";
 
     private Location rectPoint2;
     private static String rectPoint2Name = "p2";
 
 
     private static String rectBordersKey = "rectBorders";
 
     private static final HashMap<World, Border> borders = new HashMap<World, Border>();
 
     private static File              bordersFile     = new File(Plugin.getPlugin().getDataFolder(), dataFileName);
     private static FileConfiguration bordersFileConf = YamlConfiguration.loadConfiguration(bordersFile);
 
     public static HashMap<World, Border> getBorders()
     {
         return borders;
     }
 
     public Location getRectPoint1()
     {
         return rectPoint1;
     }
 
     public Location getRectPoint2()
     {
         return rectPoint2;
     }
 
     public Boolean isActive()
     {
         return isActive;
     }
 
     @SuppressWarnings("unchecked unused")
     public Border( Map<String, Object> map )
     {
         try
         {
             rectPoint1 = LocationSerializer.deserializeLocation((Map<String, Object>) map.get(rectPoint1Name));
             rectPoint2 = LocationSerializer.deserializeLocation((Map<String, Object>) map.get(rectPoint2Name));
 
             isActive = (Boolean) map.get(isActiveKey);
 
             if ( rectPoint1.getWorld().equals(rectPoint2.getWorld()) )
             {
                 borders.put(rectPoint1.getWorld(), this);
             }
             else
             {
                 throw new Exception("Border points are at different worlds.");
             }
         }
         catch ( Exception e )
         {
             Plugin.getPlugin().getLogger().severe(e.getMessage());
         }
     }
 
     public Border( Location p1, Location p2 ) throws Exception
     {
         rectPoint1 = p1;
         rectPoint2 = p2;
 
         // new border is active by default
         isActive = true;
 
         if ( rectPoint1.getWorld().equals(rectPoint2.getWorld()) )
         {
             borders.put(rectPoint1.getWorld(), this);
         }
         else
         {
             throw new Exception("Border points are at different worlds.");
         }
     }
 
 
     @SuppressWarnings("unused")
     public Map<String, Object> serialize()
     {
         Map<String, Object> map = new HashMap<String, Object>();
         map.put(rectPoint1Name, LocationSerializer.serializeLocation(rectPoint1));
         map.put(rectPoint2Name, LocationSerializer.serializeLocation(rectPoint2));
         map.put(isActiveKey, isActive);
 
         return map;
     }
 
     public static void loadBorders()
     {
         bordersFileConf.getList(rectBordersKey);
     }
 
     public static void saveBorders() throws IOException
     {
         bordersFileConf.set(rectBordersKey, new ArrayList<Object>(borders.values()));
         bordersFileConf.save(bordersFile);
     }
 
     public String toString()
     {
         return rectPoint1.getX() + "," + rectPoint1.getZ() + " " + rectPoint2.getX() + "," + rectPoint2.getZ();
     }
 
     public void enable()
     {
         isActive = true;
     }
 
     public void disable()
     {
         isActive = false;
     }
 }
