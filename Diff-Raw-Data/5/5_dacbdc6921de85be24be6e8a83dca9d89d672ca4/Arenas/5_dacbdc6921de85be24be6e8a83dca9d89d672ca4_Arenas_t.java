 package fr.aumgn.dac2.arena;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.bukkit.World;
 import org.bukkit.entity.Player;
 
 import fr.aumgn.bukkitutils.geom.Vector;
 import fr.aumgn.bukkitutils.gson.GsonLoadException;
 import fr.aumgn.bukkitutils.gson.GsonLoader;
 import fr.aumgn.dac2.DAC;
 import fr.aumgn.dac2.DACPlugin;
import fr.aumgn.dac2.arena.regions.StartRegion;
 import fr.aumgn.dac2.exceptions.ArenaDeleteException;
 import fr.aumgn.dac2.exceptions.ArenaSaveException;
 import fr.aumgn.dac2.exceptions.ArenasFolderException;
 
 public class Arenas {
 
     private static final String DIRECTORY = "arenas";
 
     private Map<String, Arena> arenas;
 
     public Arenas(DAC dac) {
         load(dac);
     }
 
     private File getFolder(DACPlugin plugin) {
         File folder = new File(plugin.getDataFolder(), DIRECTORY);
 
         if (folder.exists()) {
             if (!folder.isDirectory()) {
                 throw new ArenasFolderException(
                         folder.getPath() + " is not a directory.");
             }
         } else if (!folder.mkdirs()) {
             throw new ArenasFolderException(
                     "Unable to create " + folder.getPath() + " directory.");
         }
 
         return folder;
     }
 
     private String filenameFor(DAC dac, Arena arena) {
         return getFolder(dac.getPlugin()).getName() + File.separator
                 + arena.getName() + ".json";
     }
 
     private String arenaNameFor(File file) {
         String name = file.getName();
         int index = name.lastIndexOf(".");
         return name.substring(0, index);
     }
 
     public void load(DAC dac) {
         arenas = new HashMap<String, Arena>();
 
         GsonLoader loader = dac.getPlugin().getGsonLoader();
         File folder = getFolder(dac.getPlugin());
         for (File file : folder.listFiles()) {
             try {
                 Arena arena = loader.load(file, Arena.class);
                 String nameFromFile = arenaNameFor(file);
                 if (!nameFromFile.equals(arena.getName())) {
                     dac.getLogger().severe("Filename `" + nameFromFile
                             + "` does not match arena's name `"
                             + arena.getName() + "`. Skipping it.");
                     continue;
                 }
                 arenas.put(arena.getName(), arena);
             } catch (GsonLoadException exc) {
                 dac.getLogger().severe(
                         "Unable to read " + file.getName() + " arena's file.");
             }
         }
     }
 
     public void saveArena(DAC dac, Arena arena) {
         saveArena(dac, dac.getPlugin().getGsonLoader(), arena);
     }
 
     private void saveArena(DAC dac, GsonLoader loader, Arena arena) {
         String filename = filenameFor(dac, arena);
         try {
             loader.write(filename, arena);
         } catch (GsonLoadException _) {
             throw new ArenaSaveException("Unable to save " + filename + ".");
         }
     }
 
     public void saveAll(DAC dac) {
         for (Arena arena : arenas.values()) {
             saveArena(dac, dac.getPlugin().getGsonLoader(), arena);
         }
     }
 
     public boolean has(String name) {
         return arenas.containsKey(name);
     }
 
     public Arena get(String name) {
         return arenas.get(name);
     }
 
     /**
      * Gets the arena in whose start region the player is in.
      *
      * If different arena defines start regions which overlap themselves,
      * the result is undefined.
      *
      * @param player
      * @return the arena
      */
     public Arena get(Player player) {
         Vector pt = new Vector(player);
         for (Arena arena : arenas.values()) {
            StartRegion startRegion = arena.getStartRegion();
            if (startRegion != null && startRegion.contains(pt)) {
                 return arena;
             }
         }
 
         return null;
     }
 
     public void create(DAC dac, String name, World world) {
         Arena arena = new Arena(name, world);
         arenas.put(name, arena);
         saveArena(dac, arena);
     }
 
     public void delete(DAC dac, Arena arena) {
         arenas.remove(arena.getName());
         String filename  = filenameFor(dac, arena);
         File file = new File(dac.getPlugin().getDataFolder(), filename);
         if (!file.delete()) {
             throw new ArenaDeleteException("Unable to delete " + filename
                     + " arena's file.");
         }
     }
 
     public List<Arena> all() {
         return Collections.unmodifiableList(new ArrayList<Arena>(
                 arenas.values()));
     }
 
     public int length() {
         return arenas.values().size();
     }
 }
