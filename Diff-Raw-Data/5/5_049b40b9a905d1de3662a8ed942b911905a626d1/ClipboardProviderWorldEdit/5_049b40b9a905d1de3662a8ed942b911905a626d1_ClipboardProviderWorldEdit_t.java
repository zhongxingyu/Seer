 package ch.k42.metropolis.WorldEdit;
 
 import ch.k42.metropolis.generator.MetropolisGenerator;
 import ch.k42.metropolis.minions.Nimmersatt;
 import ch.k42.metropolis.model.enums.Direction;
 import ch.k42.metropolis.model.enums.ContextType;
 import ch.k42.metropolis.model.enums.RoadType;
 import com.sk89q.worldedit.WorldEdit;
 import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import net.minecraft.util.org.apache.commons.io.FileUtils;
 import org.bukkit.Bukkit;
 import org.bukkit.craftbukkit.libs.com.google.gson.Gson;
 import org.bukkit.craftbukkit.libs.com.google.gson.GsonBuilder;
 import org.bukkit.plugin.PluginManager;
 
 import java.io.*;
 import java.nio.file.Files;
 import java.nio.file.Paths;
 import java.util.*;
 
 /**
  * This class loads, manages and provides schematics.
  *
  * @author Thomas Richner
  */
 public class ClipboardProviderWorldEdit implements ClipboardProvider {
 
     private class ClipboardKey {
         private int chunkSizeX;
         private int chunkSizeZ;
         private boolean roadFacing;
         private ContextType context;
         private RoadType roadType;
         private Direction roadDir;
 
         private ClipboardKey(int chunkSizeX, int chunkSizeZ, ContextType context, RoadType roadType, Direction roadDir, boolean roadFacing) {
             this.chunkSizeX = chunkSizeX;
             this.chunkSizeZ = chunkSizeZ;
             this.context = context;
             this.roadType = roadType;
             this.roadDir = roadDir;
             this.roadFacing = roadFacing;
         }
 
         @Override
         public boolean equals(Object obj) {
             if (obj == null) return false;
             if (!(obj instanceof ClipboardKey)) return false;
 
             ClipboardKey k = (ClipboardKey) obj;
 
             if (roadDir.equals(Direction.WEST) || roadDir.equals(Direction.EAST)) {
                 if (chunkSizeX != k.chunkSizeZ && chunkSizeZ != k.chunkSizeX) return false;
             } else {
                 if (chunkSizeX != k.chunkSizeX && chunkSizeZ != k.chunkSizeZ) return false;
             }
 
             if (roadFacing && !k.roadFacing) return false;
 
             if (!roadFacing && k.roadFacing) return false;
 
             if (!context.equals(k.context)) return false;
 
             if ((context.equals(ContextType.STREET) || context.equals(ContextType.HIGHWAY)) && !roadType.equals(k.roadType))
                 return false;
 
             return true;    //To change body of overridden methods use File | Settings | File Templates.
         }
 
         @Override
         public int hashCode() {
             int result = chunkSizeX;
             result = 31 * result + chunkSizeZ;
             result = 31 * result + (roadFacing ? 1 : 0);
             result = 31 * result + context.hashCode();
             result = 31 * result + roadType.hashCode();
             result = 31 * result + roadDir.hashCode();
             return result;
         }
 
         @Override
         public String toString() {
             String info = "ClipboardKey" +
                     " chunkSizeX:" + chunkSizeX +
                     " chunkSizeZ:" + chunkSizeZ +
                     " roadFacing:" + roadFacing +
                     " context:" + context +
                     " roadDir:" + roadDir +
                     " roadType:" + roadType;
             return info;
         }
     }
 
     private static final String pluginName = "WorldEdit";
     private static final String foldername = "schematics";
     private static final String cachename = "cache";
     private static final String settingsname = "/global_settings.json";
 
     private File schematicsFolder;
     private File cacheFolder;
     private List<SchematicConfig> batchedConfigs = new ArrayList<SchematicConfig>();
     private Map<ClipboardKey, List<Clipboard>> clipboards = new HashMap<ClipboardKey, List<Clipboard>>();
     private Map<String, Clipboard> clipboardsByName = new HashMap<String, Clipboard>();
 
     private GlobalSchematicConfig globalSettings;
 
     public ClipboardProviderWorldEdit(MetropolisGenerator generator) throws Exception {
         super();
         //==== First load the plugin
         WorldEditPlugin worldEditPlugin = null;
         generator.reportDebug("looking for WorldEdit");
         PluginManager pm = Bukkit.getServer().getPluginManager();
         worldEditPlugin = (WorldEditPlugin) pm.getPlugin(pluginName);
 
         // not there? darn
         if (worldEditPlugin == null) {
             Bukkit.getLogger().warning("No WorldEdit found!");
             throw new Exception("Couldn't find WorldEdit plugin.");
         }
 
 
 //            // got the right version?
 //            if (!isPlugInVersionOrBetter(generator, worldEditPlugin, pluginMinVersion))
 //
 //                // Use it anyway?
 //                if (generator.settings.forceLoadWorldEdit) {
 //                    generator.reportMessage("'" + CityWorldSettings.tagForceLoadWorldEdit + "' setting enabled!");
 //
 //                    // Well that didn't work... let's tell the user about a potential workaround
 //                } else {
 //                    generator.reportMessage("[PasteProvider] Cannot use the installed WorldEdit. ",
 //                            "See the '" + CityWorldSettings.tagForceLoadWorldEdit + "' setting for possible workaround.");
 //                    return null;
 //                }
 
         // make sure it is enabled
         if (!pm.isPluginEnabled(worldEditPlugin))
             pm.enablePlugin(worldEditPlugin);
 
         WorldEdit worldEdit = worldEditPlugin.getWorldEdit();
 
         // Yay! found it!
         generator.reportMessage("[ClipboardProvider] Found WorldEdit, enabling its schematics");
 
         // find the files
         File pluginFolder = generator.getPlugin().getDataFolder();
         generator.reportDebug("looking for PluginFolder");
 
         if (!pluginFolder.isDirectory()) {
             pluginFolder.mkdir();
         }
 
         if (pluginFolder.isDirectory()) {
             generator.reportDebug("found PluginFolder");
             // forget all those shape and ore type and just go for the world name
             schematicsFolder = findFolder(pluginFolder, foldername);
             cacheFolder = findFolder(pluginFolder, cachename);
 
            // Delete all files in the Cache folder
            FileUtils.cleanDirectory(cacheFolder);

 //			// shape folder (normal, floating, etc.)
 //			File shapeFolder = findFolder(pluginFolder, generator.shapeProvider.getCollectionName());
 //
 //			// finally ores are used to figure out the collection folder (normal, nether, theend, etc.)
 //			schematicsFolder = findFolder(shapeFolder, generator.oreProvider.getCollectionName());
             generator.reportDebug("loading clips");
             loadClips(generator);
             generator.reportDebug("loaded clips");
         }
     }
 
     private File findFolder(File parent, String name) throws Exception {
         name = toCamelCase(name);
         File result = new File(parent, name);
         if (!result.isDirectory())
             if (!result.mkdir())
                 throw new UnsupportedOperationException("[WorldEdit] Could not create/find the folder: " + parent.getAbsolutePath() + File.separator + name);
         return result;
     }
 
     private String toCamelCase(String text) {
         return text.substring(0, 1).toUpperCase() + text.substring(1, text.length()).toLowerCase();
     }
 
     private FilenameFilter matchSchematics() {
         return new FilenameFilter() {
             @Override
             public boolean accept(File dir, String name) {
                 return name.endsWith(".schematic");
             }
         };
     }
 
     private FilenameFilter matchConfigs() {
         return new FilenameFilter() {
             @Override
             public boolean accept(File dir, String name) {
                 return name.endsWith(".json");
             }
         };
     }
 
     private FileFilter isDirectory() {
         return new FileFilter() {
             @Override
             public boolean accept(File pathname) {
                 return pathname.isDirectory();  //To change body of implemented methods use File | Settings | File Templates.
             }
         };
     }
 
     public void loadClips(MetropolisGenerator generator) throws Exception {
 
         if (schematicsFolder != null && cacheFolder != null) {
 
             loadConfigOrDefault(schematicsFolder.getPath() + settingsname);  // load global config
 
             //---- load all config files
             List<File> configFiles = findAllConfigsRecursively(schematicsFolder, new ArrayList<File>());
             Gson gson = new Gson();
 
             for (File configFile : configFiles) {
                 try {
                     String json = new String(Files.readAllBytes(configFile.toPath()));
                     json = Nimmersatt.friss(json);
                     SchematicConfig config = gson.fromJson(json, SchematicConfig.class);
                     config.setPath(configFile.getPath());
                     if (config.getSchematics().size() > 0) {
                         batchedConfigs.add(config);
                         generator.reportMessage("[ClipboardProvider] BatchConfiguation " + configFile.getName() + " added.");
                     }
                 } catch (Exception e) {
                     generator.reportException("[ClipboardProvider] BatchConfiguation " + configFile.getName() + " could NOT be loaded", e);
                 }
             }
 
             //---- load all schematic files
             List<File> schematicFiles = findAllSchematicsRecursively(schematicsFolder, new ArrayList<File>());
 
             for (File schematicFile : schematicFiles) {
                 try {
                     Clipboard clip = null;
                     clip = new ClipboardWorldEdit(generator, schematicFile, cacheFolder, globalSettings, batchedConfigs);
                     clipboardsByName.put(clip.getName(), clip);
                     for (ContextType c : clip.getContextTypes()) { // add to all possible directions and contexts
                         for (Direction dir : Direction.getDirections()) {
                             ClipboardKey key = new ClipboardKey(clip.chunkSizeX, clip.chunkSizeZ, c, clip.getSettings().getRoadType(), dir, clip.getSettings().getRoadFacing());
                             List<Clipboard> list = clipboards.get(key);
                             if (list == null) {
                                 list = new ArrayList();
                             }
                             // add the clip to the result
                             list.add(clip);
                             clipboards.put(key, list);
                         }
                     }
 
                     generator.reportMessage("[ClipboardProvider] Schematic " + schematicFile.getName() + " successfully loaded.");
                 } catch (Exception e) {
                     generator.reportException("[ClipboardProvider] Schematic " + schematicFile.getName() + " could NOT be loaded", e);
                 }
             }
 
         } else {
             throw new FileNotFoundException("Couldn't find schematics folder!");
         }
     }
 
     private List<File> findAllSchematicsRecursively(File path, List<File> schematics) {
         File[] schematicFiles = path.listFiles(matchSchematics());
         schematics.addAll(Arrays.asList(schematicFiles));
         File[] subfolders = path.listFiles(isDirectory());
         for (File folder : subfolders) {              // recursively search in all subfolders
             findAllSchematicsRecursively(folder, schematics); // this could lead to a endless loop, maybe a max_depth would be clever...
         }
         return schematics;
     }
 
     private List<File> findAllConfigsRecursively(File path, List<File> configs) {
         File[] configFiles = path.listFiles(matchConfigs());
         configs.addAll(Arrays.asList(configFiles));
         File[] subfolders = path.listFiles(isDirectory());
         for (File folder : subfolders) {              // recursively search in all subfolders
             findAllConfigsRecursively(folder, configs); // this could lead to a endless loop, maybe a max_depth would be clever...
         }
         return configs;
     }
 
     /**
      * Returns a list containing all available clipboards that match the size and context
      *
      * @param chunkX      chunksize in X direction
      * @param chunkZ      chunksize in Z direction
      * @param contextType context of the structure
      * @param roadType    defines the type of the road, only applies if context is STREET
      * @return list containing all matching clipboards, might be empty but never null
      */
     public List<Clipboard> getRoadFit(int chunkX, int chunkZ, ContextType contextType, RoadType roadType) {
         List<Clipboard> list = clipboards.get(new ClipboardKey(chunkX, chunkZ, contextType, roadType, Direction.NORTH, false));
         if (list == null) list = new LinkedList<Clipboard>();
         return list;
     }
 
     /**
      * Returns a list containing all available clipboards that match the size and context
      *
      * @param chunkX      chunksize in X direction
      * @param chunkZ      chunksize in Z direction
      * @param contextType context of the structure
      * @param roadDir     direction of the road
      * @param roadFacing  road facing clipboards
      * @return list containing all matching clipboards, might be empty but never null
      */
     public List<Clipboard> getFit(int chunkX, int chunkZ, ContextType contextType, Direction roadDir, boolean roadFacing) {
         ClipboardKey key = new ClipboardKey(chunkX, chunkZ, contextType, RoadType.NONE, roadDir, roadFacing);
         List<Clipboard> list = clipboards.get(key);
         if (list == null) list = new LinkedList<Clipboard>();
         Bukkit.getServer().getLogger().info(list.toString());
         return list;
     }
 
     public Clipboard getByName(String name) {
         return clipboardsByName.get(name);
     }
 
 
     private void loadConfigOrDefault(String path) {
         if (!loadConfig(path)) { // did we succeed?
             Bukkit.getServer().getLogger().warning("Unable to load global schematics settings file");
             if (!storeConfig(path)) { // no, so just storeConfig the default config
                 Bukkit.getLogger().severe("Unable to load global schematics settings file");
             }
         } else {
             Bukkit.getServer().getLogger().info("Successfully loaded global schematic settings file: " + path);
         }
     }
 
     private boolean loadConfig(String path) {
         Gson gson = new Gson();
         try {
             String json = new String(Files.readAllBytes(Paths.get(path)));
             json = Nimmersatt.friss(json);
             globalSettings = gson.fromJson(json, GlobalSchematicConfig.class);
             return true;
         } catch (Exception e) { // catch all exceptions, inclusive any JSON fails
             globalSettings = new GlobalSchematicConfig(); // couldn't read config file? use default
             Bukkit.getLogger().throwing(this.getClass().getName(), "loadConfig", e);
             return false;
         }
     }
 
     private boolean storeConfig(String path) {
         try {
             Gson gson = new GsonBuilder().setPrettyPrinting().create();
             String file = gson.toJson(globalSettings);
             Files.write(Paths.get(path), file.getBytes()); //overwrite existing stuff
             return true;
         } catch (IOException e) {
             Bukkit.getLogger().throwing(this.getClass().getName(), "storeConfig config", e);
             return false;
         }
     }
 
 }
