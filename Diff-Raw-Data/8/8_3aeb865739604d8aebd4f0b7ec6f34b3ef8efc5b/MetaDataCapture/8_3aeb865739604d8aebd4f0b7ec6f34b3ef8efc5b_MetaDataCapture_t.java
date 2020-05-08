 package com.tehbeard.BeardStat.utils;
 
 import java.io.InputStream;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Scanner;
 
 import net.dragonzone.promise.Promise;
 
 import org.bukkit.Material;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Skeleton;
 import org.bukkit.entity.Zombie;
 
import com.tehbeard.BeardStat.BeardStat;
 import com.tehbeard.BeardStat.containers.EntityStatBlob;
 import com.tehbeard.BeardStat.listeners.defer.DelegateIncrement;
 
 /**
  * translates a material/entity to the metadata to capture.
  * 
  * @author james
  * 
  */
 public class MetaDataCapture {
 
     // private static Material[] mats =
     // {Material.WOOD,Material.LOG,Material.SAPLING,Material.INK_SACK,Material.COAL,Material.STEP,Material.WOOL,Material.SMOOTH_BRICK};
 
     public static final Map<Material, EntryInfo> mats = new HashMap<Material, EntryInfo>();
 
     static {
         mats.put(Material.QUARTZ_BLOCK, new EntryInfo(15, 0, 15) {
             @Override
             public int getMetdataValue(int value) {
                 return (value > 2) ? 2 : value;
             }
         });
     }
 
     public static void readData(InputStream is) {
         Scanner s = new Scanner(is);
 
         while (s.hasNext()) {        
             String line = s.nextLine();
             String[] entry = line.split(",");
            try{
             Material mat = Material.getMaterial(Integer.parseInt(entry[0].replaceAll("[^0-9]", "")));
             EntryInfo ei = new EntryInfo(
                     Integer.parseInt(entry[1].replaceAll("0(X|x)", "").replaceAll("[^0-9A-Fa-f]", ""),16), 
                     Integer.parseInt(entry[2].replaceAll("[^0-9A-Fa-f]", "")), 
                     Integer.parseInt(entry[3].replaceAll("[^0-9A-Fa-f]", "")));
             mats.put(mat, ei);
            }catch(Exception e){
                BeardStat.printCon("Failed to load metadata for id: " + entry[0] +", skipping (version mismatch?)");
            }
         }
 
         s.close();
     }
 
     public static void saveMetaDataMaterialStat(Promise<EntityStatBlob> blob, String domain, String world,
             String category, Material material, int dataValue, int value) {
         String matName = material.toString().toLowerCase().replace("_", "");
 
         blob.onResolve(new DelegateIncrement(domain, world, category, matName, value));
         if (mats.containsKey(material)) {
             EntryInfo info = mats.get(material);
             if (info.valid(dataValue)) {
                 String tag = "_" + info.getMetdataValue(dataValue);
                 blob.onResolve(new DelegateIncrement(domain, world, category, matName + tag, value));
             }
         }
         if (material.isRecord()) {
             blob.onResolve(new DelegateIncrement(domain, world, category, "records", value));
 
         }
     }
 
     public static void saveMetaDataEntityStat(Promise<EntityStatBlob> blob, String domain, String world,
             String category, Entity entity, int value) {
         String entityName = entity.getType().toString().toLowerCase().replace("_", "");
         blob.onResolve(new DelegateIncrement(domain, world, category, entityName, value));
 
         if (entity instanceof Skeleton) {
             blob.onResolve(new DelegateIncrement(domain, world, category, ((Skeleton) entity).getSkeletonType()
                     .toString().toLowerCase()
                     + "_" + entityName, value));
         }
 
         if (entity instanceof Zombie) {
             if (((Zombie) entity).isVillager()) {
                 blob.onResolve(new DelegateIncrement(domain, world, category, "villager_zombie", value));
             }
         }
     }
 
     public static boolean hasMetaData(Material mat) {
         return mats.containsKey(mat);
 
     }
 
     public static class EntryInfo {
         public int mask;
         public int min;
         public int max;
 
         public EntryInfo(int mask, int min, int max) {
             super();
             this.mask = mask;
             this.min = min;
             this.max = max;
         }
 
         public boolean valid(int value) {
             return ((value >= this.min) && (value <= this.max));
         }
 
         public int getMetdataValue(int value) {
             return value & this.mask;
         }
 
         @Override
         public String toString() {
             return "EntryInfo [mask=" + mask + ", min=" + min + ", max=" + max + "]";
         }
         
         
     }
 }
