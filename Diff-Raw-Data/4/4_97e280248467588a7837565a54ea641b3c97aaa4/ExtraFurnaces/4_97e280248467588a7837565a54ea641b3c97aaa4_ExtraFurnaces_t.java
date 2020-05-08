 package org.dyndns.pamelloes.ExtraFurnaces;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.jar.JarEntry;
 import java.util.jar.JarFile;
 import java.util.logging.Logger;
 
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.dyndns.pamelloes.ExtraFurnaces.block.DiamondFurnace;
 import org.dyndns.pamelloes.ExtraFurnaces.block.GoldFurnace;
 import org.dyndns.pamelloes.ExtraFurnaces.block.IronFurnace;
 import org.dyndns.pamelloes.ExtraFurnaces.data.CustomFurnaceData;
 import org.dyndns.pamelloes.ExtraFurnaces.gui.FurnaceGui;
 import org.dyndns.pamelloes.ExtraFurnaces.gui.InventoryGui;
 import org.getspout.spoutapi.SpoutManager;
 import org.getspout.spoutapi.block.design.Texture;
 import org.getspout.spoutapi.event.input.KeyPressedEvent;
 import org.getspout.spoutapi.event.screen.ScreenCloseEvent;
 import org.getspout.spoutapi.inventory.SpoutItemStack;
 import org.getspout.spoutapi.inventory.SpoutShapedRecipe;
 import org.getspout.spoutapi.material.CustomBlock;
 import org.getspout.spoutapi.material.MaterialData;
 import org.getspout.spoutapi.player.SpoutPlayer;
 
 public class ExtraFurnaces extends JavaPlugin {
	public static final int MINIMUM_SPOUTPLUGIN_VERSION = 1179;
	public static final int MINIMUM_SPOUTCRAFT_VERSION = 1466;
 	
 	public static Map<SpoutPlayer,CustomFurnaceData> datamap = new HashMap<SpoutPlayer, CustomFurnaceData>();
 	public static Map<SpoutPlayer,FurnaceGui> guimap = new HashMap<SpoutPlayer, FurnaceGui>();
 	public static Logger log = Logger.getLogger("minecraft");
 	
 	int[] furnaceoff = {0,1,1,2,1,0};
 	int[] furnaceon  = {0,4,4,3,4,0};
 	int ironfurnaceincr = 0;
 	int goldfurnaceincr = 5;
 	int diamondfurnaceincr = 10;
 	
 	public static CustomBlock ironfurnaceoff, ironfurnaceon, goldfurnaceoff, goldfurnaceon, diamondfurnaceoff, diamondfurnaceon;
 	
 	public void onEnable() {
 		try {
 			if(Integer.parseInt(getServer().getPluginManager().getPlugin("Spout").getDescription().getVersion())<MINIMUM_SPOUTPLUGIN_VERSION) {
 				log.severe("[ExtraFurnaces] Extra Furnaces requires Spout version " + MINIMUM_SPOUTPLUGIN_VERSION + " or newer.");
 				log.severe("[ExtraFurnaces] Extra Furnaces will now disable itself.");
 				getServer().getPluginManager().disablePlugin(this);
 				return;
 			}
 		} catch(NumberFormatException e) {
 			log.warning("[ExtraFurnaces] Could not identify Spout version, incompatibilities may arise.");
 		}
 		extractFile("moreFurnaces.png",true);
 		extractFile("ironfurnace.png",true);
 		extractFile("goldfurnace.png",true);
 		extractFile("diamondfurnace.png",true); 
 		registerItems();
 		registerRecipes();
 		getServer().getPluginManager().registerEvents(new Listener() {
 			@SuppressWarnings("unused")
 			@EventHandler
 			public void onKeyDown(KeyPressedEvent e) {
 				if(!e.getKey().equals(e.getPlayer().getInventoryKey())) return;
 				if(!datamap.keySet().contains(e.getPlayer())) return;
 				datamap.remove(e.getPlayer());
 				guimap.remove(e.getPlayer());
 				e.getPlayer().getMainScreen().closePopup();
 			}
 		}, this);
 		getServer().getPluginManager().registerEvents(new Listener() {
 			@SuppressWarnings("unused")
 			@EventHandler
 			public void onScreenClose(ScreenCloseEvent e) {
 				if(e.isCancelled()) return;
 				if(!datamap.keySet().contains(e.getPlayer())) return;
 				CustomFurnaceData dat = datamap.get(e.getPlayer());
 				if(dat!=null) dat.onPlayerCloseFurnace(e.getPlayer());
 				InventoryGui gui = guimap.get(e.getPlayer());
 				gui.onClose();
 			}
 		}, this);
 		log.info("[ExtraFurnaces] Enabled.");
 	}
 
 	public void onDisable() {
 		log.info("[ExtraFurnaces] Disabled.");
 	}
 	
 	private void registerItems() {
 		Texture texture = new Texture(this,"plugins/ExtraFurnaces/moreFurnaces.png",256,256,16);
 		ironfurnaceoff = new IronFurnace(this, texture, incrementArray(furnaceoff,ironfurnaceincr), false);
 		ironfurnaceon = new IronFurnace(this, texture, incrementArray(furnaceon,ironfurnaceincr), true).setItemDrop(new SpoutItemStack(ironfurnaceoff, 1));
 		goldfurnaceoff = new GoldFurnace(this, texture, incrementArray(furnaceoff,goldfurnaceincr), false);
 		goldfurnaceon = new GoldFurnace(this, texture, incrementArray(furnaceon,goldfurnaceincr), true).setItemDrop(new SpoutItemStack(goldfurnaceoff, 1));
 		diamondfurnaceoff = new DiamondFurnace(this, texture, incrementArray(furnaceoff,diamondfurnaceincr), false);
 		diamondfurnaceon = new DiamondFurnace(this, texture, incrementArray(furnaceon,diamondfurnaceincr), true).setItemDrop(new SpoutItemStack(diamondfurnaceoff, 1));
 	}
 	
 	private void registerRecipes() {
 		ItemStack result = new SpoutItemStack(ironfurnaceoff, 1);
 		SpoutShapedRecipe recipe = new SpoutShapedRecipe(result);
 		recipe.shape("AAA", "ABA", "AAA");
 		recipe.setIngredient('A', MaterialData.ironIngot);
 		recipe.setIngredient('B', MaterialData.furnace);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(recipe);
 		
 		result = new SpoutItemStack(goldfurnaceoff, 1);
 		recipe = new SpoutShapedRecipe(result);
 		recipe.shape("AAA", "ABA", "AAA");
 		recipe.setIngredient('A', MaterialData.goldIngot);
 		recipe.setIngredient('B', ironfurnaceoff);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(recipe);
 		
 		result = new SpoutItemStack(goldfurnaceoff, 1);
 		recipe = new SpoutShapedRecipe(result);
 		recipe.shape("AAA", "ABA", "AAA");
 		recipe.setIngredient('A', MaterialData.goldIngot);
 		recipe.setIngredient('B', ironfurnaceon);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(recipe);
 
 		result = new SpoutItemStack(diamondfurnaceoff, 1);
 		recipe = new SpoutShapedRecipe(result);
 		recipe.shape("AAA", "ABA", "AAA");
 		recipe.setIngredient('A', MaterialData.diamond);
 		recipe.setIngredient('B', goldfurnaceoff);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(recipe);
 
 		result = new SpoutItemStack(diamondfurnaceoff, 1);
 		recipe = new SpoutShapedRecipe(result);
 		recipe.shape("AAA", "ABA", "AAA");
 		recipe.setIngredient('A', MaterialData.diamond);
 		recipe.setIngredient('B', goldfurnaceon);
 		SpoutManager.getMaterialManager().registerSpoutRecipe(recipe);
 	}
 	
 	public static int[] incrementArray(final int[] array,final int increment) {
 		int[] intarray = new int[array.length];
 		for(int i=0;i<array.length;i++) intarray[i]=array[i]+increment;
 		return intarray;
 	}
 	
 	/**
 	 * Extract files from the plugin jar and optionally cache them on the client.
 	 * @param regex a pattern of files to extract
 	 * @param cache if any files found should be added to the Spout cache
 	 * @return if any files were extracted
 	 */
 	public boolean extractFile(String regex, boolean cache) {
 		boolean found = false;
 		try {
 			JarFile jar = new JarFile(getFile());
 			for (Enumeration<JarEntry> entries = jar.entries(); entries.hasMoreElements();) {
 				JarEntry entry = (JarEntry) entries.nextElement();
 				String name = entry.getName();
 				if (name.matches(regex)) {
 					if (!getDataFolder().exists()) {
 						getDataFolder().mkdir();
 					}
 					try {
 						File file = new File(getDataFolder(), name);
 						if (!file.exists()) {
 							InputStream is = jar.getInputStream(entry);
 							FileOutputStream fos = new FileOutputStream(file);
 							while (is.available() > 0) {
 								fos.write(is.read());
 							}
 							fos.close();
 							is.close();
 							found = true;
 						}
 						if (cache && name.matches(".*\\.(txt|yml|xml|png|jpg|ogg|midi|wav|zip)$")) {
 							SpoutManager.getFileManager().addToCache(this, file);
 						}
 					} catch (Exception e) {
 					}
 				}
 			}
 		} catch (Exception e) {
 		}
 		return found;
 	}
 }
