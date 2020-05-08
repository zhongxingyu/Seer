 package com.tehbeard.BeardStat.containers;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.regex.Pattern;
 
 import org.bukkit.Bukkit;
 
 import me.tehbeard.utils.expressions.VariableProvider;
 
 import com.tehbeard.BeardStat.BeardStat;
 
 /**
  * Represents a collection of statistics bound to an entity Currently only used
  * for Players.
  * 
  * @author James
  * 
  */
 public class EntityStatBlob implements VariableProvider {
 
     private static Map<String, String> dynamics      = new HashMap<String, String>();
 
     private static Map<String, String> dynamicsSaved = new HashMap<String, String>();
 
     public static void addDynamicStat(String stat, String expr) {
         dynamics.put(stat, expr);
     }
 
     public static void addDynamicSavedStat(String stat, String expr) {
         dynamicsSaved.put(stat, expr);
     }
 
     private void addDynamics() {
         if(type.equals(BeardStat.PLAYER_TYPE)){
             for (Entry<String, String> entry : dynamics.entrySet()) {
 
                 String[] parts = entry.getKey().split("\\::");
 
                 String domain = BeardStat.DEFAULT_DOMAIN;
                 String world = BeardStat.GLOBAL_WORLD;
                 String cat = null;
                 String stat = null;
                 if (parts.length == 2) {
                     BeardStat.printDebugCon("Old dynamic stat found, adding to global world");
                     cat = parts[0];
                     stat = parts[1];
                 } else if (parts.length == 4) {
                     domain = parts[0];
                     world = parts[1];
                     cat = parts[2];
                     stat = parts[3];
                 }
                 addStat(new DynamicStat(domain, world, cat, stat, entry.getValue()));
             }
             //Add health status
             addStat(new IStat() {
                 
                 @Override
                 public void setWorld(String world) {
                 }
                 
                 @Override
                 public void setValue(int value) {
                     
                 }
                 
                 @Override
                 public void setOwner(EntityStatBlob playerStatBlob) {
                     
                 }
                 
                 @Override
                 public void setDomain(String domain) {
                     
                 }
                 
                 @Override
                 public boolean isArchive() {
                     return true;
                 }
                 
                 @Override
                 public void incrementStat(int i) {
                 }
                 
                 @Override
                 public String getWorld() {
 
                     return BeardStat.GLOBAL_WORLD;//Bukkit.getPlayer(getName()).getWorld().getName();
                 }
                 
                 @Override
                 public int getValue() {
                    // TODO Auto-generated method stub
                    return Bukkit.getPlayer(getName()).getHealth();
                 }
                 
                 @Override
                 public String getStatistic() {
                     return "health";
                 }
                 
                 @Override
                 public EntityStatBlob getOwner() {
                     return null;
                 }
                 
                 @Override
                 public String getDomain() {
                     return BeardStat.DEFAULT_DOMAIN;
                 }
                 
                 @Override
                 public String getCategory() {
                     return "status";
                 }
                 
                 @Override
                 public void decrementStat(int i) {
                 }
                 
                 @Override
                 public void clearArchive() {
                 }
                 
                 @Override
                 public void archive() {
                 }
                 
                 @Override
                 public IStat clone() {
                     return new StaticStat(getDomain(), getWorld(), getCategory(), getStatistic(), getValue());
                 }
             });
         }
 
         // dynamics that will be saved to database
         for (Entry<String, String> entry : dynamicsSaved.entrySet()) {
 
             String[] parts = entry.getKey().split("\\::");
 
             String domain = BeardStat.DEFAULT_DOMAIN;
             String world = BeardStat.GLOBAL_WORLD;
             String cat = null;
             String stat = null;
             if (parts.length == 2) {
                 BeardStat.printDebugCon("Old dynamic stat found, adding to global world");
                 cat = parts[0];
                 stat = parts[1];
             } else if (parts.length == 4) {
                 domain = parts[0];
                 world = parts[1];
                 cat = parts[2];
                 stat = parts[3];
             }
             addStat(new DynamicStat(domain, world, cat, stat, entry.getValue(), true));
         }
     }
 
     private Map<String, IStat> stats = new ConcurrentHashMap<String, IStat>();
 
     private int                entityId;
     private String             name;
     private String             type;
 
     public String getName() {
         return this.name;
     }
 
     public int getEntityID() {
         return this.entityId;
     }
 
     /**
      * 
      * @param name
      *            Players name
      * @param ID
      *            playerID in database
      */
     public EntityStatBlob(String name, int entityId, String type) {
         this.name = name;
         this.entityId = entityId;
         this.type = type;
         addDynamics();
     }
 
     /**
      * add stat
      * 
      * @param stat
      */
     public void addStat(IStat stat) {
         this.stats.put(stat.getDomain() + "::" + stat.getWorld() + "::" + stat.getCategory() + "::" + stat.getStatistic(),
                 stat);
         stat.setOwner(this);
     }
 
     /**
      * Get a players stat, creates new object if not found.
      * 
      * @param statistic
      * @return
      */
     public IStat getStat(String domain, String world, String category, String statistic) {
         IStat psn = this.stats.get(domain + "::" + world + "::" + category + "::" + statistic);
         if (psn != null) {
             return psn;
         }
         psn = new StaticStat(domain, world, category, statistic, 0);
         addStat(psn);
         return psn;
     }
 
     /**
      * Query this blob for a {@link StatVector}, a {@link StatVector} combines
      * multiple stats into one easy to access object {@link StatVector} supports
      * the use of regex, with the shortcut "*" to denote all possible values
      * (substituted for ".*" in regex engine) Defaults to readonly mode, any
      * mutators called on this {@link StatVector} will throw
      * {@link IllegalStateException} if readOnly is true
      * 
      * @param domain
      * @param world
      * @param category
      * @param statistic
      * @return
      */
     public StatVector getStats(String domain, String world, String category, String statistic) {
         return getStats(domain, world, category, statistic, true);
     }
 
     /**
      * Query this blob for a {@link StatVector}, a {@link StatVector} combines
      * multiple stats into one easy to access object {@link StatVector} supports
      * the use of regex, with the shortcut "*" to denote all possible values
      * (substituted for ".*" in regex engine) Defaults to readonly mode, any
      * mutators called on this {@link StatVector} will throw
      * {@link IllegalStateException} if readOnly is true
      * 
      * @param domain
      * @param world
      * @param category
      * @param statistic
      * @param readOnly
      * @return
      */
     public StatVector getStats(String domain, String world, String category, String statistic, boolean readOnly) {
         String pattern = starToRegex(domain);
         pattern += "\\::" + starToRegex(world);
         pattern += "\\::" + starToRegex(category);
         pattern += "\\::" + starToRegex(statistic);
 
         return getStats(domain, world, category, statistic, pattern, readOnly);
     }
 
     /**
      * Query this blob for a {@link StatVector}, a {@link StatVector} combines
      * multiple stats into one easy to access object {@link StatVector} supports
      * the use of regex, with the shortcut "*" to denote all possible values
      * (substituted for ".*" in regex engine) Defaults to readonly mode, any
      * mutators called on this {@link StatVector} will throw
      * {@link IllegalStateException} if readOnly is true
      * 
      * This method differs from other getStats() as it provides direct control
      * of the final regex expression used, domain,world etc are used to populate
      * the respective fields of the returned StatVector
      * 
      * @param domain
      * @param world
      * @param category
      * @param statistic
      * @param regex
      * @param readOnly
      * @return
      */
     public StatVector getStats(String domain, String world, String category, String statistic, String regex,
             boolean readOnly) {
         StatVector vector = new StatVector(domain, world, category, statistic, readOnly);
         for (Entry<String, IStat> e : this.stats.entrySet()) {
             if (Pattern.matches(regex, e.getKey())) {
                 vector.add(e.getValue());
             }
         }
         return vector;
     }
 
     private String starToRegex(String s) {
         if (s.equals("*")) {
             return "[a-zA-Z0-9_]*";
         }
         return s;
     }
 
     /**
      * Return all the stats!
      * 
      * @return
      */
     public Collection<IStat> getStats() {
         return this.stats.values();
     }
 
     public boolean hasStat(String domain, String world, String category, String statistic) {
         return this.stats.containsKey(domain + "::" + world + "::" + category + "::" + statistic);
 
     }
 
     @Override
     public int resolveVariable(String var) {
         String[] parts = var.split("\\::");
         String domain = BeardStat.DEFAULT_DOMAIN;
         String world = "*";
         String cat = "";
         String stat = "";
         if (parts.length == 4) {
             domain = parts[0];
             world = parts[1];
             cat = parts[2];
             stat = parts[3];
         }
         if (parts.length == 2) {
             cat = parts[0];
             stat = parts[1];
         } else {
             throw new IllegalStateException("Attempt to parse invalid varriable " + var);
         }
         return getStats(domain, world, cat, stat).getValue();
     }
 
     public String getType() {
         return this.type;
     }
 
     public EntityStatBlob cloneForArchive() {
         EntityStatBlob blob = new EntityStatBlob(this.name, this.entityId, this.type);
         blob.stats.clear();
         for (IStat stat : this.stats.values()) {
             if (stat.isArchive()) {
                 BeardStat.printDebugCon("Archiving stat " + stat.getDomain() + "::" + stat.getWorld() + "::"
                         + stat.getCategory() + "::" + stat.getStatistic() + " = " + stat.getValue());
                 IStat is = stat.clone();
                 if (is != null) {
                     BeardStat.printDebugCon("Stat added");
                     blob.addStat(is);
                     stat.clearArchive();
                 }
             }
         }
         BeardStat.printDebugCon("End cloning");
         return blob;
     }
 
 }
