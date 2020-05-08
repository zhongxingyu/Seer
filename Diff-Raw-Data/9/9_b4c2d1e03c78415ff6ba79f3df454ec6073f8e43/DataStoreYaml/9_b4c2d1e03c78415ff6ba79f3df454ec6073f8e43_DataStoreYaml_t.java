 /* Copyright 2013 Kevin Seiden. All rights reserved.
 
  This works is licensed under the Creative Commons Attribution-NonCommercial 3.0
 
  You are Free to:
     to Share: to copy, distribute and transmit the work
     to Remix: to adapt the work
 
  Under the following conditions:
     Attribution: You must attribute the work in the manner specified by the author (but not in any way that suggests that they endorse you or your use of the work).
     Non-commercial: You may not use this work for commercial purposes.
 
  With the understanding that:
     Waiver: Any of the above conditions can be waived if you get permission from the copyright holder.
     Public Domain: Where the work or any of its elements is in the public domain under applicable law, that status is in no way affected by the license.
     Other Rights: In no way are any of the following rights affected by the license:
         Your fair dealing or fair use rights, or other applicable copyright exceptions and limitations;
         The author's moral rights;
         Rights other persons may have either in the work itself or in how the work is used, such as publicity or privacy rights.
 
  Notice: For any reuse or distribution, you must make clear to others the license terms of this work. The best way to do this is with a link to this web page.
  http://creativecommons.org/licenses/by-nc/3.0/
  */
 
 package io.github.alshain01.flags;
 
 import io.github.alshain01.flags.area.*;
 import io.github.alshain01.flags.economy.EconomyPurchaseType;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.*;
 
 import io.github.alshain01.flags.sector.Sector;
 import me.ryanhamshire.GriefPrevention.GriefPrevention;
 
 import org.bukkit.Bukkit;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.bekvon.bukkit.residence.Residence;
 import org.bukkit.scheduler.BukkitRunnable;
 import org.bukkit.scheduler.BukkitTask;
 
 /**
  * Class for managing YAML Database Storage
  */
 final class DataStoreYaml extends DataStore {
     private final static String DATA_FILE = "data.yml";
     private final static String WILDERNESS_FILE = "wilderness.yml";
     private final static String DEFAULT_FILE = "default.yml";
     private final static String BUNDLE_FILE = "bundle.yml";
     private final static String PRICE_FILE = "price.yml";
     private final static String SECTOR_FILE = "sector.yml";
 
     private final static String DATABASE_VERSION_PATH = "Default.Database.Version";
     private final static String BUNDLE_PATH = "Bundle";
     private final static String TRUST_PATH = "Trust";
     private final static String VALUE_PATH = "Value";
     private final static String MESSAGE_PATH = "Message";
     private final static String INHERIT_PATH = "InheritParent";
     private final static String PRICE_PATH = "Price";
     private final static String SECTOR_PATH = "Sectors";
 
     private final static String DATA_FOOTER = "Data";
 
     private final File dataFolder;
     private final Plugin plugin;
     private final int saveInterval;
 
     // Auto-Save manager
     private BukkitTask as;
 
 	private YamlConfiguration data;
 	private YamlConfiguration def;
 	private YamlConfiguration wilderness;
 	private YamlConfiguration bundle;
 	private YamlConfiguration price;
     private YamlConfiguration sectors;
 
     private boolean saveData = false;
 
     /*
      * Constructor
      */
 	DataStoreYaml(Plugin plugin, int autoSaveInterval) {
         this.dataFolder = plugin.getDataFolder();
         this.plugin = plugin;
         this.saveInterval = autoSaveInterval;
 
         // Upgrade sequence from older versions
         File worldFile = new File(dataFolder, "world.yml");
         File wildFile = new File(dataFolder, WILDERNESS_FILE);
         if(worldFile.exists() && !wildFile.exists()) {
             if(!worldFile.renameTo(wildFile)) {
                 Logger.error("Failed to rename world.yml to wilderness.yml");
             }
         }
 
         reload();
 	}
 
     private class AutoSave extends BukkitRunnable {
         @Override
         public void run() {
             save();
         }
     }
 
     /*
      * Interface Methods
      */
     @Override
     public void create(JavaPlugin plugin) {
         // Don't change the version here, not needed (will change in update)
         if (notExists(plugin)) {
             writeVersion(new DataStoreVersion(1, 0, 0));
         }
     }
 
     @Override
     public void reload() {
         wilderness = YamlConfiguration.loadConfiguration(new File(dataFolder, WILDERNESS_FILE));
         def = YamlConfiguration.loadConfiguration(new File(dataFolder, DEFAULT_FILE));
         data = YamlConfiguration.loadConfiguration(new File(dataFolder, DATA_FILE));
 
         // Check to see if the file exists and if not, write the defaults
         File bundleFile = new File(dataFolder, BUNDLE_FILE);
         if (!bundleFile.exists()) {
             plugin.saveResource(BUNDLE_FILE, false);
         }
         bundle = YamlConfiguration.loadConfiguration(bundleFile);
 
         // Check to see if the file exists and if not, write the defaults
         File priceFile = new File(dataFolder, PRICE_FILE);
         if (!priceFile.exists()) {
             plugin.saveResource(PRICE_FILE, false);
         }
         price = YamlConfiguration.loadConfiguration(priceFile);
 
         if(CuboidType.getActive() == CuboidType.FLAGS) {
             sectors = YamlConfiguration.loadConfiguration(new File(dataFolder, SECTOR_FILE));
         }
 
         // Remove old auto-saves
         if(as != null ) {
             as.cancel();
             as = null;
         }
 
         // Set up autosave
         if (saveInterval > 0) {
             as = new AutoSave().runTaskTimer(plugin, saveInterval * 1200, saveInterval * 1200);
         }
     }
 
     public void save() {
         if(!saveData) { return; }
         try {
             wilderness.save(new File(dataFolder, WILDERNESS_FILE));
             def.save(new File(dataFolder, DEFAULT_FILE));
             data.save(new File(dataFolder, DATA_FILE));
             bundle.save(new File(dataFolder, BUNDLE_FILE));
             price.save(new File(dataFolder, PRICE_FILE));
             if(CuboidType.getActive() == CuboidType.FLAGS) {
                 sectors.save(new File(dataFolder, SECTOR_FILE));
             }
             saveData = false;
         } catch (IOException ex) {
             Logger.error("Faled to write to data files. " + ex.getMessage());
         }
     }
 
     @Override
     public void close() {
         save();
     }
 
     @Override
     public DataStoreVersion readVersion() {
         final YamlConfiguration versionConfig = getYml(DATABASE_VERSION_PATH);
         if (!versionConfig.isSet(DATABASE_VERSION_PATH)) {
             return new DataStoreVersion(0, 0, 0);
         }
 
         final String[] ver = versionConfig.getString(DATABASE_VERSION_PATH).split("\\.");
         return new DataStoreVersion(Integer.valueOf(ver[0]), Integer.valueOf(ver[1]),
                 Integer.valueOf(ver[2]));
     }
 
     @Override
     public DataStoreType getType() {
         return DataStoreType.YAML;
     }
 
 
     @Override
     public void update(JavaPlugin plugin) {
         final DataStoreVersion ver = readVersion();
         if (ver.getMajor() == 1 && ver.getMinor() == 4 && ver.getBuild() == 2) { return; }
         if (ver.getMajor() <= 1 && ver.getMinor() <= 4 && ver.getBuild() <= 2) {
             if (ver.getMajor() <= 1 && ver.getMinor() <= 2 && ver.getBuild() < 2) {
                 YamlConfiguration dataConfig = getYml("data");
                 ConfigurationSection cSec;
                 CuboidType system = CuboidType.getActive();
                 if (dataConfig.isConfigurationSection(CuboidType.getActive().toString() + DATA_FOOTER)) {
                     if (system == CuboidType.GRIEF_PREVENTION || CuboidType.getActive() == CuboidType.RESIDENCE) {
                         cSec = dataConfig.getConfigurationSection(CuboidType.getActive().toString() + DATA_FOOTER);
 
                         final Set<String> keys = cSec.getKeys(true);
                         for (final String k : keys) {
 
                             if (k.contains(VALUE_PATH) || k.contains(MESSAGE_PATH)
                                     || k.contains(TRUST_PATH) || k.contains(INHERIT_PATH)) {
 
                                 final String id = k.split("\\.")[0];
                                 String world;
 
                                 if (CuboidType.getActive() == CuboidType.GRIEF_PREVENTION) {
                                     world = GriefPrevention.instance.dataStore
                                             .getClaim(Long.valueOf(id))
                                             .getGreaterBoundaryCorner().getWorld()
                                             .getName();
                                 } else {
                                     world = Residence.getResidenceManager()
                                             .getByName(id).getWorld();
                                 }
 
                                 cSec.set(world + "." + k, cSec.get(k));
                             }
                         }
                         // Remove the old
                         for (final String k : keys) {
                             if (k.split("\\.").length == 1
                                     && Bukkit.getWorld(k.split("\\.")[0]) == null) {
                                 cSec.set(k, null);
                             }
                         }
 
                     }
 
                     //Convert values to boolean instead of string
                     final String[] fileArray = {"data", "default", "world"};
                     for (final String s : fileArray) {
                         dataConfig = getYml(s);
                         Set<String> keys = dataConfig.getKeys(true);
                         for (final String k : keys) {
                             if (k.contains("Value") || k.contains("InheritParent")) {
                                 dataConfig.set(k, Boolean.valueOf(dataConfig.getString(k)));
                             }
                         }
                     }
 
                     //Remove "Data" from the root heading.
                     dataConfig = getYml("data");
                     cSec = getYml("data").getConfigurationSection(CuboidType.getActive().toString() + "Data");
                     ConfigurationSection newCSec = getYml("data").createSection(CuboidType.getActive().toString());
                     Set<String> keys = cSec.getKeys(true);
                     for (final String k : keys) {
                         if (k.contains(VALUE_PATH) || k.contains(MESSAGE_PATH)
                                 || k.contains(TRUST_PATH) || k.contains(INHERIT_PATH)) {
                             newCSec.set(k, cSec.get(k));
                             cSec.set(k, null);
                         }
                     }
                     dataConfig.set(CuboidType.getActive().toString() + "Data", null);
                 }
                 writeVersion(new DataStoreVersion(1, 2, 2));
             }
             // Upgrade to 1.4.2
             YamlConfiguration cYml = getYml("wilderness");
             if(cYml.isConfigurationSection("World")) {
                 ConfigurationSection cSec = cYml.getConfigurationSection("World");
                 ConfigurationSection newCSec = cYml.createSection("Wilderness");
                 for (final String k : cSec.getKeys(true)) {
                     if (k.contains(VALUE_PATH) || k.contains(MESSAGE_PATH)
                             || k.contains(TRUST_PATH) || k.contains(INHERIT_PATH)) {
                         newCSec.set(k, cSec.get(k));
                     }
                 }
 
             }
             cYml.set("World", null);
             writeVersion(new DataStoreVersion(1, 4, 2));
             saveData = true;
         }
     }
 
     @Override
     public final Set<String> readBundles() {
         if(bundle.isConfigurationSection(BUNDLE_PATH)) {
             return bundle.getConfigurationSection(BUNDLE_PATH).getKeys(false);
         }
         return new HashSet<String>();
     }
 
     @Override
 	public final Set<Flag> readBundle(String bundleName) {
 		final HashSet<Flag> flags = new HashSet<Flag>();
         if(!bundle.isConfigurationSection(BUNDLE_PATH)) { return flags; }
 		final List<?> list = bundle.getConfigurationSection(BUNDLE_PATH).getList(bundleName, new ArrayList<String>());
 
 		for (final Object o : list) {
 			if (Flags.getRegistrar().isFlag((String) o)) {
 				flags.add(Flags.getRegistrar().getFlag((String) o));
 			}
 		}
 		return flags;
 	}
 
     @Override
     public final void writeBundle(String name, Set<Flag> flags) {
         ConfigurationSection bundleConfig = getCreatedSection(bundle, BUNDLE_PATH);
 
         if (flags == null || flags.size() == 0) {
             // Delete the bundle
             bundleConfig.set(name, null);
             return;
         }
 
         final List<String> list = new ArrayList<String>();
         for (final Flag f : flags) {
             list.add(f.getName());
         }
 
         bundleConfig.set(name, list);
         saveData = true;
     }
 
 	@Override
 	public Boolean readFlag(Area area, Flag flag) {
         final String path = getAreaPath(area) + "." + flag.getName();
         if(!getYml(path).isConfigurationSection(path)) { return null; }
         final ConfigurationSection flagConfig = getYml(path).getConfigurationSection(path);
 		return flagConfig.isSet(VALUE_PATH) ? flagConfig.getBoolean(VALUE_PATH) : null;
 	}
 
     @Override
     public void writeFlag(Area area, Flag flag, Boolean value) {
         final String path = getAreaPath(area) + "." + flag.getName();
         final ConfigurationSection flagConfig = getCreatedSection(getYml(path), path);
         flagConfig.set(VALUE_PATH, value);
         saveData = true;
     }
 
 	@Override
 	public String readMessage(Area area, Flag flag) {
         final String path = getAreaPath(area) + "." + flag.getName();
         if(!getYml(path).isConfigurationSection(path)) { return null; }
 		return getYml(path).getConfigurationSection(path).getString(MESSAGE_PATH);
 	}
 
     @Override
     public void writeMessage(Area area, Flag flag, String message) {
         final String path = getAreaPath(area) + "." + flag.getName();
         final ConfigurationSection dataConfig = getCreatedSection(getYml(path), path);
         dataConfig.set(MESSAGE_PATH, message);
         saveData = true;
     }
 
 	@Override
 	public double readPrice(Flag flag, EconomyPurchaseType type) {
 		final String path = PRICE_PATH + "." + type.toString();
         if(!price.isConfigurationSection(path)) { return 0; }
         final ConfigurationSection priceConfig = price.getConfigurationSection(path);
 		return priceConfig.isSet(flag.getName()) ? priceConfig.getDouble(flag.getName()) : 0;
 	}
 
     @Override
     public void writePrice(Flag flag, EconomyPurchaseType type, double newPrice) {
         final String path = PRICE_PATH + "." + type.toString();
         final ConfigurationSection priceConfig = getCreatedSection(price, path);
         priceConfig.set(flag.getName(), newPrice);
         saveData = true;
     }
 
 	@Override
 	public Set<String> readTrust(Area area, Flag flag) {
 		final String path = getAreaPath(area) + "." + flag.getName();
         final Set<String> stringData = new HashSet<String>();
         if(!getYml(path).isConfigurationSection(path)) { return stringData; }
 		final List<?> setData = getYml(path).getConfigurationSection(path).getList(TRUST_PATH, new ArrayList<String>());
 
 		for (final Object o : setData) {
 			stringData.add((String) o);
 		}
 		return stringData;
 	}
 
     @Override
     public Set<String> readPlayerTrust(Area area, Flag flag) {
         final String path = getAreaPath(area) + "." + flag.getName();
         final Set<String> stringData = new HashSet<String>();
         if(!getYml(path).isConfigurationSection(path)) { return stringData; }
         final List<?> setData = getYml(path).getConfigurationSection(path).getList(TRUST_PATH, new ArrayList<String>());
 
         for (final Object o : setData) {
             if(!((String)o).contains(".")) {
                 stringData.add((String) o);
             }
         }
         return stringData;
     }
 
     @Override
     public Set<String> readPermissionTrust(Area area, Flag flag) {
         final String path = getAreaPath(area) + "." + flag.getName();
         final Set<String> stringData = new HashSet<String>();
         if(!getYml(path).isConfigurationSection(path)) { return stringData; }
         final List<?> setData = getYml(path).getConfigurationSection(path).getList(TRUST_PATH, new ArrayList<String>());
 
         for (final Object o : setData) {
             if(((String)o).contains(".")) {
                 stringData.add((String) o);
             }
         }
         return stringData;
     }
 
     @Override
     public void writeTrust(Area area, Flag flag, Set<String> players) {
         final String path = getAreaPath(area) + "." + flag.getName() + ".Trust";
         final ConfigurationSection trustConfig = getCreatedSection(getYml(path), path);
         final List<String> list = new ArrayList<String>();
 
         for (final String s : players) {
             list.add(s);
         }
 
         trustConfig.set(path, list);
         saveData = true;
     }
 
     @Override
     public boolean readInheritance(Area area) {
         if (!(area instanceof Subdivision) || !((Subdivision)area).isSubdivision()) {
             return true;
         }
 
        String path = area.getCuboidType().toString() + "." + area.getWorld().getName() + "."
                + area.getSystemID() + "." + ((Subdivision) area).getSystemSubID();
 
         if(!getYml(path).isConfigurationSection(path)) { return true; }
         ConfigurationSection inheritConfig = getYml(path).getConfigurationSection(path);
         return !inheritConfig.isSet(INHERIT_PATH) || inheritConfig.getBoolean(INHERIT_PATH);
     }
 
     @Override
     public void writeInheritance(Area area, boolean value) {
         if (!(area instanceof Subdivision) || !((Subdivision) area).isSubdivision()) {
             return;
         }
 
        String path = area.getCuboidType().toString() + "." + area.getWorld().getName() + "."
                + area.getSystemID() + "." + ((Subdivision) area).getSystemSubID();
 
         ConfigurationSection inheritConfig = getCreatedSection(getYml(path), path);
         inheritConfig.set(path, value);
         saveData = true;
     }
 
     @Override
     public Map<UUID, Sector> readSectors() {
         Map<UUID, Sector> sectors = new HashMap<UUID, Sector>();
         File file = new File(plugin.getDataFolder(), SECTOR_FILE);
         if(!file.exists()) { return sectors; }
         YamlConfiguration sector = YamlConfiguration.loadConfiguration(file);
         if(!sector.isConfigurationSection(SECTOR_PATH)) { return sectors; }
         ConfigurationSection sectorConfig = sector.getConfigurationSection(SECTOR_PATH);
 
         for(String s : sectorConfig.getKeys(false)) {
             UUID sID = UUID.fromString(s);
             sectors.put(sID, new Sector(sID, sectorConfig.getConfigurationSection(s).getValues(false)));
         }
 
         return sectors;
     }
 
     @Override
     public void writeSector(Sector sector) {
         ConfigurationSection sectorConfig = getCreatedSection(sectors, SECTOR_PATH);
         sectorConfig.set(sector.getID().toString(), sector.serialize());
         saveData = true;
     }
 
     @Override
     public void deleteSector(UUID sID) {
         ConfigurationSection sectorConfig = getCreatedSection(sectors, SECTOR_PATH);
         sectorConfig.set(sID.toString(), null);
         saveData = true;
     }
 
 	@Override
 	public void remove(Area area) {
         String path = getAreaPath(area);
         getYml(path).set(path, null);
 	}
 
     /*
      * Used in SQL Import/Export
      */
     @SuppressWarnings("unused") // Future Use
     protected Set<String> getAreaIDs() {
         ConfigurationSection cSec = getYml("data").getConfigurationSection(CuboidType.getActive().toString() + "Data");
         if(cSec == null) { return new HashSet<String>(); }
         return cSec.getKeys(false);
     }
 
     @SuppressWarnings("unused") // Future Use
     protected Set<String> getAreaSubIDs(String id) {
         ConfigurationSection cSec = getYml("data").getConfigurationSection(CuboidType.getActive().toString() + "Data." + id);
         Set<String> subIDs = new HashSet<String>();
         if(cSec == null) { return subIDs; }
 
         for(String i : cSec.getKeys(true)) {
             String[] nodes = i.split("\\.");
             if(nodes.length == 4) { // All keys for the parent should have 3 nodes.
                 subIDs.add(nodes[0]);
             }
         }
         return subIDs;
     }
 
     Set<String> readKeys() {
         return data.getKeys(true);
     }
 
     Boolean getBoolean(String dataLocation) {
         if(data.isBoolean(dataLocation)) {
             return data.getBoolean(dataLocation);
         }
         return null;
     }
 
     String getString(String dataLocation) {
         return data.getString(dataLocation);
     }
 
     List<?> getList(String dataLocation) {
         return data.getList(dataLocation);
     }
 
     /*
      * Private
      */
     private void writeVersion(DataStoreVersion version) {
         final YamlConfiguration cYml = getYml(DATABASE_VERSION_PATH);
         cYml.set(DATABASE_VERSION_PATH, version.getMajor() + "." + version.getMinor() + "." + version.getBuild());
     }
 
     private boolean notExists(JavaPlugin plugin) {
         final File fileObject = new File(plugin.getDataFolder(), "default.yml");
         return !fileObject.exists();
     }
 
     private String getAreaPath(Area area) {
         String path = area.getCuboidType().toString() + "." + area.getWorld().getName();
 
         if (!(area instanceof Wilderness || area instanceof Default)) {
             path += "." + area.getSystemID();
         }
 
         if (area instanceof Subdivision && !readInheritance(area)) {
             path += "." + ((Subdivision) area).getSystemSubID();
         }
 
         return path;
     }
 
     private ConfigurationSection getCreatedSection(YamlConfiguration config, String path) {
         if(config.isConfigurationSection(path)) {
             return config.getConfigurationSection(path);
         } else {
             return config.createSection(path);
         }
     }
 
     private YamlConfiguration getYml(String path) {
         final String[] pathList = path.split("\\.");
 
         if (pathList[0].equalsIgnoreCase("wilderness")) {
             return wilderness;
         } else if (pathList[0].equalsIgnoreCase("default")) {
             return def;
         } else {
             return data;
         }
     }
 }
