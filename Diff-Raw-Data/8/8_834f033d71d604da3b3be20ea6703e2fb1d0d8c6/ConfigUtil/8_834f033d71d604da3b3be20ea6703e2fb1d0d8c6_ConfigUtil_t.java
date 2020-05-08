 package com.miraclem4n.madvanced.configs;
 
 import org.bukkit.configuration.file.YamlConfiguration;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Set;
 
 public class ConfigUtil {
     static YamlConfiguration config;
     static File file;
     static Boolean changed;
 
     static ArrayList<String> whoAliases;
     static ArrayList<String> listAliases;
     static ArrayList<String> afkAliases;
     static ArrayList<String> afkOtherAliases;
 
     static HashMap<String, List<String>> aliasMap;
 
     public static void initialize() {
         load();
     }
 
     public static void dispose() {
         config = null;
         file = null;
         changed = null;
 
         whoAliases = null;
         listAliases = null;
         afkAliases = null;
         afkOtherAliases = null;
         
         aliasMap = null;
     }
 
     public static void load() {
         file = new File("plugins/MAdvanced/config.yml");
 
         config = YamlConfiguration.loadConfiguration(file);
 
         config.options().indent(4);
         config.options().header("MAdvanced Config");
 
         changed = false;
 
         whoAliases = new ArrayList<String>();
         listAliases = new ArrayList<String>();
         afkAliases = new ArrayList<String>();
         afkOtherAliases = new ArrayList<String>();
 
         aliasMap = new HashMap<String, List<String>>();
 
         loadDefaults();
     }
 
     private static void loadDefaults() {
         checkOption("plugin.spout", true);
 
         checkOption("option.eHQAFK", true);
         checkOption("option.useGroupedList", true);
         checkOption("option.listVar", "group");
         checkOption("option.collapsedListVars", "default,Default");
         checkOption("option.AFKTimer", 30);
         checkOption("option.AFKKickTimer", 120);
         checkOption("option.useAFKList", false);
 
         loadAliases();
 
         checkOption("aliases.mchatwho", whoAliases);
         checkOption("aliases.mchatlist", listAliases);
         checkOption("aliases.mchatafk", afkAliases);
         checkOption("aliases.mchatafkother", afkOtherAliases);
 
         setupAliasMap();
 
         save();
     }
 
     public static void set(String key, Object obj) {
         config.set(key, obj);
 
         changed = true;
     }
 
     public static Boolean save() {
         if (!changed)
             return false;
 
         try {
             config.save(file);
             changed = false;
             return true;
         } catch (Exception ignored) {
             return false;
         }
     }
 
     public static YamlConfiguration getConfig() {
         return config;
     }
 
     public static HashMap<String, List<String>> getAliasMap() {
         return aliasMap;
     }
 
     private static void checkOption(String option, Object defValue) {
         if (!config.isSet(option))
             set(option, defValue);
     }
 
     private static void editOption(String oldOption, String newOption) {
         if (config.isSet(oldOption)) {
             set(newOption, config.get(oldOption));
             set(oldOption, null);
         }
     }
 
     private static void removeOption(String option) {
         if (config.isSet(option))
             set(option, null);
     }
 
     private static void loadAliases() {
         whoAliases.add("who");
 
         listAliases.add("list");
         listAliases.add("online");
         listAliases.add("playerlist");
 
         afkAliases.add("afk");
         afkAliases.add("away");
 
         afkOtherAliases.add("afko");
         afkOtherAliases.add("awayother");
         afkOtherAliases.add("awayo");
     }
 
     private static void setupAliasMap() {
         Set<String> keys = config.getConfigurationSection("aliases").getKeys(false);
 
         for (String key : keys)
             aliasMap.put(key, config.getStringList("aliases." + key));
     }
 }
