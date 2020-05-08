 package com.github.Indiv0.BookDupe;
 
 import org.bukkit.Material;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.ShapelessRecipe;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class BookDupe extends JavaPlugin {
 
     // Initializes an ItemCraftListener.
     public final ItemCraftListener blockListener = new ItemCraftListener();
 
    public void onEnable(){
         // Retrieves an instance of the PluginManager.
         PluginManager pm = getServer().getPluginManager();
 
         // Registers the blockListener with the PluginManager.
         pm.registerEvents(this.blockListener, this);
 
         // Creates a new recipe to serve as the basis for the book duplication feature.
         ItemStack craftable = new ItemStack(Material.FEATHER);
         ShapelessRecipe recipe = new ShapelessRecipe(craftable);
         recipe.addIngredient(Material.WRITTEN_BOOK);
         recipe.addIngredient(Material.BOOK_AND_QUILL);
 
         // Adds the recipe to the server.
         getServer().addRecipe(recipe);
 
         // Prints a message to the server confirming successful initialization of the plugin.
         getLogger().info("BookDupe has initialized successfully.");
     }
 }
