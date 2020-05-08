 /*
  * Copyright (C) 2012 BangL <henno.rickowski@googlemail.com>
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package de.bangl.wgpdf;
 
 import com.mewin.WGCustomFlags.WGCustomFlagsPlugin;
 import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
 import com.sk89q.worldguard.protection.ApplicableRegionSet;
 import com.sk89q.worldguard.protection.managers.RegionManager;
 import com.sk89q.worldguard.protection.regions.ProtectedRegion;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 import org.bukkit.Location;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 import org.bukkit.plugin.Plugin;
 
 /**
  *
  * @author BangL <henno.rickowski@googlemail.com>
  */
 public class Utils {
 
     public static WGCustomFlagsPlugin getWGCustomFlags(WGPlayerDamageFlagsPlugin plugin) {
         final Plugin wgcf = plugin.getServer().getPluginManager().getPlugin("WGCustomFlags");
         if (wgcf == null || !(wgcf instanceof WGCustomFlagsPlugin)) {
             return null;
         }
         return (WGCustomFlagsPlugin)wgcf;
     }
 
     public static WorldGuardPlugin getWorldGuard(WGPlayerDamageFlagsPlugin plugin) {
         final Plugin wg = plugin.getServer().getPluginManager().getPlugin("WorldGuard");
         if (wg == null || !(wg instanceof WorldGuardPlugin)) {
             return null;
         }
         return (WorldGuardPlugin)wg;
     }
 
     public static boolean dmgAllowedAtLocation(WorldGuardPlugin wgp, DamageCause cause, Location loc) {
         RegionManager rm = wgp.getRegionManager(loc.getWorld());
         if (rm == null) {
             return true;
         }
         ApplicableRegionSet regions = rm.getApplicableRegions(loc);
         Iterator<ProtectedRegion> itr = regions.iterator();
         Map<ProtectedRegion, Boolean> regionsToCheck = new HashMap<>();
         Set<ProtectedRegion> ignoredRegions = new HashSet<>();
         
         while(itr.hasNext()) {
             ProtectedRegion region = itr.next();
             
             if (ignoredRegions.contains(region)) {
                 continue;
             }
             
             Object allowed = dmgAllowedInRegion(region, cause);
             
             if (allowed != null) {
                 ProtectedRegion parent = region.getParent();
                 
                 while(parent != null) {
                     ignoredRegions.add(parent);
                     
                     parent = parent.getParent();
                 }
                 
                 regionsToCheck.put(region, (boolean) allowed);
             }
         }
         
         if (regionsToCheck.size() >= 1) {
             Iterator<Map.Entry<ProtectedRegion, Boolean>> itr2 = regionsToCheck.entrySet().iterator();
             
             while(itr2.hasNext()) {
                 Map.Entry<ProtectedRegion, Boolean> entry = itr2.next();
                 
                 ProtectedRegion region = entry.getKey();
                 boolean value = entry.getValue();
                 
                 if (ignoredRegions.contains(region)) {
                     continue;
                 }
                 
                 if (value) { // allow > deny
                     return true;
                 }
             }
             
             return false;
         } else {
            Object allowed = dmgAllowedInRegion(rm.getRegion("__global__"), cause);
            
             if (allowed != null) {
                 return (boolean) allowed;
             } else {
                 return true;
             }
         }
     }
 
     public static Object dmgAllowedInRegion(ProtectedRegion region, DamageCause cause) {
         DmgCause dc = castMaterial(cause);
         
         HashSet<DmgCause> allowedCauses = (HashSet<DmgCause>) region.getFlag(WGPlayerDamageFlagsPlugin.ALLOW_DAMAGE_FLAG);
         HashSet<DmgCause> blockedCauses = (HashSet<DmgCause>) region.getFlag(WGPlayerDamageFlagsPlugin.DENY_DAMAGE_FLAG);
         
         if (allowedCauses != null
                 && (allowedCauses.contains(dc) || allowedCauses.contains(DmgCause.ANY))) {
             return true;
         }
         else if(blockedCauses != null
                 && (blockedCauses.contains(dc) || blockedCauses.contains(DmgCause.ANY))) {
             return false;
         } else {
             return null;
         }
     }
 
     public static DmgCause castMaterial(DamageCause material) {
         try {
             return DmgCause.valueOf(material.name());
         } catch(IllegalArgumentException ex) {
             return null;
         }
     }
 }
