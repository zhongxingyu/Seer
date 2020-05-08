 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.mewin.WGRegionEffects;
 
 import com.mewin.WGRegionEffects.flags.PotionEffectDesc;
 import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
 import com.sk89q.worldguard.protection.managers.RegionManager;
 import com.sk89q.worldguard.protection.regions.ProtectedRegion;
 import java.util.AbstractMap.SimpleEntry;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 import org.bukkit.Location;
 import org.bukkit.potion.PotionEffectType;
 
 /**
  *
  * @author mewin<mewin001@hotmail.de>
  */
 public class Util {
     
     public static List<PotionEffectDesc> getEffectsForLocation(WorldGuardPlugin wgp, Location loc)
     {
         Map<PotionEffectType, Entry<ProtectedRegion, PotionEffectDesc>> allEffects = new HashMap<PotionEffectType, Entry<ProtectedRegion, PotionEffectDesc>>();
         Map<PotionEffectType, List<ProtectedRegion>> ignoredRegions = new HashMap<PotionEffectType, List<ProtectedRegion>>();
         
         RegionManager rm = wgp.getRegionManager(loc.getWorld());
         
         if (rm == null)
         {
             return new ArrayList<PotionEffectDesc>();
         }
         
         for (ProtectedRegion region : rm.getApplicableRegions(loc))
         {
             Set<PotionEffectDesc> regionEffects = (Set<PotionEffectDesc>) region.getFlag(WGRegionEffectsPlugin.EFFECT_FLAG);
             
             if (regionEffects == null)
             {
                 continue;
             }
             
             for (PotionEffectDesc effect : regionEffects)
             {
                 ProtectedRegion parent = region.getParent();
                 List<ProtectedRegion> iRegions;
                 
                 if (ignoredRegions.containsKey(effect.getType()))
                 {
                     iRegions = ignoredRegions.get(effect.getType());
                 }
                 else
                 {
                     iRegions = new ArrayList<ProtectedRegion>();
                 }
                 
                 if (iRegions.contains(region))
                 {
                     continue;
                 }
                 
                 while (parent != null)
                 {
                     iRegions.add(parent);
                     
                     parent = parent.getParent();
                 }
                 
                 ignoredRegions.put(effect.getType(), iRegions);
                 
                 if (!allEffects.containsKey(effect.getType()) 
                     || iRegions.contains(allEffects.get(effect.getType()).getKey())
                     || allEffects.get(effect.getType()).getKey().getPriority() < region.getPriority()
                     || (allEffects.get(effect.getType()).getKey().getPriority() == region.getPriority()
                     &&  Math.abs(allEffects.get(effect.getType()).getValue().getAmplifier() + 1) < Math.abs(effect.getAmplifier() + 1)))
                 {
                     allEffects.put(effect.getType(), new SimpleEntry<ProtectedRegion, PotionEffectDesc>(region, effect));
                 }
             }
         }
         
         ArrayList<PotionEffectDesc> effects = new ArrayList<PotionEffectDesc>();
         
         ProtectedRegion global = rm.getRegion("__global__");
         
         if (global != null)
         {
             Set<PotionEffectDesc> gEffects = (Set<PotionEffectDesc>) global.getFlag(WGRegionEffectsPlugin.EFFECT_FLAG);
 
             if (gEffects != null)
             {
                 for (PotionEffectDesc effect : gEffects)
                 {
                     if (!allEffects.containsKey(effect.getType()))
                     {
                         allEffects.put(effect.getType(), new SimpleEntry<ProtectedRegion, PotionEffectDesc>(global, effect));
                     }
                 }
             }
        }
 
        for (Entry<ProtectedRegion, PotionEffectDesc> entry : allEffects.values())
        {
            effects.add(entry.getValue());
         }
         
         return effects;
     }
 }
