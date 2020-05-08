 /**
  *
  * @author Indivisible0
  */
 package com.github.Indiv0.BookDupe;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.logging.Level;
 
 import org.bukkit.Material;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.ShapelessRecipe;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;

import com.github.Indiv0.BookDupe.util.Metrics;
 
 public class BookDupe extends JavaPlugin {
 
     // Initializes an ItemCraftListener.
     public final ItemCraftListener blockListener = new ItemCraftListener(this);
 
     private File mainDataFolder;
     private File configFile;
     private FileConfiguration settings;
 
     @Override
     public void onLoad() {
         // Gets the main folder for data to be stored in.
         mainDataFolder = getDataFolder();
 
         // Checks to make sure that the main folder for data stored in exists.
         // If not, creates it.
         checkFolderAndCreate(mainDataFolder);
 
         // Creates/loads configuration and settings.
         loadConfig(configFile);
 
         // Enable PluginMetrics.
         enableMetrics();
     }
 
     @Override
     public void onEnable() {
         // Retrieves an instance of the PluginManager.
         PluginManager pm = getServer().getPluginManager();
 
         // Registers the blockListener with the PluginManager.
         pm.registerEvents(blockListener, this);
 
         // Creates a new recipe to serve as the basis for the book duplication
         // feature.
         ItemStack craftable = new ItemStack(Material.BOOK_AND_QUILL);
         ShapelessRecipe recipe = new ShapelessRecipe(craftable);
         recipe.addIngredient(Material.WRITTEN_BOOK);
         recipe.addIngredient(Material.BOOK_AND_QUILL);
 
         // Adds the recipe to the server.
         getServer().addRecipe(recipe);
 
         // Creates another recipe to serve as a secondary recipe for book
         // duplication.
         recipe = new ShapelessRecipe(craftable);
         recipe.addIngredient(Material.WRITTEN_BOOK);
         recipe.addIngredient(Material.INK_SACK);
         recipe.addIngredient(Material.FEATHER);
         recipe.addIngredient(Material.BOOK);
 
         // Adds the recipe to the server.
         getServer().addRecipe(recipe);
 
         // Prints a message to the server confirming successful initialization
         // of the plugin.
         PluginDescriptionFile pdfFile = getDescription();
         getLogger()
                 .info(pdfFile.getName() + " " + pdfFile.getVersion()
                         + " is enabled.");
     }
 
     private void enableMetrics()
     {
         try {
            Metrics metrics = new Metrics(this);
             metrics.start();
         } catch (IOException ex) {
             System.out.println("An error occured while attempting to connect to PluginMetrics.");
         }
     }
 
     private void loadConfig(File configFile) {
         configFile = new File(mainDataFolder, "config.yml");
 
         try {
             // Creates the configuration file if it doesn't exist.
             if (!configFile.exists()) {
                 getLogger().log(Level.INFO, "No default config file exists, creating one.");
                 createDefaultConfigFile(configFile);
             }
 
             // Initializes the configuration and populates it with settings.
             settings = new YamlConfiguration();
             settings.load(configFile);
         } catch (Exception ex) {
             logException(ex, Level.WARNING, "Failed to load configuration.");
         }
     }
 
     public void createDefaultConfigFile(File configFile) {
         BufferedReader bReader = null;
         BufferedWriter bWriter = null;
         String line;
 
         try {
             // Opens a stream in order to access the config.yml stored in the
             // jar.
             bReader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/config.yml")));
             bWriter = new BufferedWriter(new FileWriter(configFile));
 
             // Writes all of the lines from the built in config.yml to the new
             // one.
             while ((line = bReader.readLine()) != null) {
                 bWriter.write(line);
                 bWriter.newLine();
             }
         } catch (Exception ex) {
             logException(ex, Level.WARNING, "Failed to create default config.yml");
         } finally {
             try {
                 // Confirm the streams are closed.
                 if (bReader != null) bReader.close();
                 if (bWriter != null) bWriter.close();
             } catch (Exception ex) {
                 logException(ex, Level.WARNING, "Failed to close buffers while writing default config.yml");
             }
         }
     }
 
     public boolean checkFolderAndCreate(File toCheck) {
         // Check to see if the directory exists, creating it if it doesn't.
         if (!toCheck.exists()) try {
             if (toCheck.mkdirs()) return true;
         } catch (Exception ex) {
             logException(ex, Level.WARNING, "Data folder could not be created.");
         }
         return false;
     }
 
     public boolean getSetting(String setting)
     {
         return settings.getBoolean(setting);
     }
 
     public void logException(Exception ex, Level level, String message) {
         ex.printStackTrace(System.out);
         getLogger().log(level, message);
     }
 }
