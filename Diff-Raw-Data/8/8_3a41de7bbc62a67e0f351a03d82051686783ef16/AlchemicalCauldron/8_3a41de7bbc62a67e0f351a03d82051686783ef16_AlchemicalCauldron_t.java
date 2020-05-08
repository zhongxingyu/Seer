 package com.circuitlocution.alchemicalcauldron;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.logging.Logger;
 import java.util.logging.Level;
 
 import org.bukkit.DyeColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.Server;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.material.Dye;
 import org.bukkit.material.MaterialData;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginLoader;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 /**
  * 
  * @author georgedorn
  */
 public class AlchemicalCauldron extends JavaPlugin
 {
 	private final AlchemicalCauldronPlayerListener playerListener = new AlchemicalCauldronPlayerListener(this);
 	private final Logger log = Logger.getLogger("Minecraft_alchemical_cauldron");
 	public AlchemicalCauldron(PluginLoader pluginLoader, Server instance, PluginDescriptionFile desc, File folder, File plugin, ClassLoader cLoader)
 	{
 		super(pluginLoader, instance, desc, folder, plugin, cLoader);
 		folder.mkdirs();
 
 	}
 
 	public void onDisable()
 	{
 		log.info(getDescription().getName() + " " + getDescription().getVersion() + " unloaded.");
 	}
 
 	public void onEnable()
 	{
 
 		log.info(getDescription().getName() + " " + getDescription().getVersion() + " loaded.");
 		//reload config
 		File yml = new File(getDataFolder(), "config.yml");
 		if (!yml.exists())
 		{
 			try
 			{
 				yml.createNewFile();
 			}
 			catch (IOException ex)
 			{
 			}
 		}
 
		String log_level = getConfiguration().getString("log_level", "debug").toString().toUpperCase();
		log.info("Setting AlchemicalCauldron logging to " + log_level);
		log.setLevel(Level.parse(log_level));

 		PluginManager pm = getServer().getPluginManager();
 		pm.registerEvent(Event.Type.PLAYER_ITEM, playerListener, Priority.Normal, this);
 	}
 
 	protected void process_event(Event event, Block reagent2, ItemStack reagent3, Player p){
 		World world = reagent2.getWorld();
 		Location loc = new Location(world, reagent2.getX(), reagent2.getY(), reagent2.getZ());
 		//check to see if the block was just placed in a cauldron
 		if (!is_on_cauldron(loc)){
 			log.info("Doesn't look like a cauldron to me.");
 			return;
 		}
 
 		log.info("Click! " + p.getDisplayName() + " clicked on a " + reagent2.getType().name() + " with a " + reagent3.toString() + " which has material: " + reagent3.getType().name());
 
 		Material reagent3_type = reagent3.getType();
 		MaterialData reagent3_data = reagent3.getData();
 		//this would be where we'd check for this type being a possible recipe starter
 		if (reagent3_type != Material.INK_SACK){
 			return;
 		}
 		
 		//this would be where, if the type is one that supports data, we'd look up the data as well. 
 		//for now, only green dye is supported
 		
 		if (reagent3_type != Material.INK_SACK){
 			log.info("Clicked with something other than ink");
 			return;
 		}
 		if ( 15 - reagent3.getDurability() != (int)DyeColor.GREEN.getData()){ //holy crap this is ugly
 			log.info("Dye color isn't green.  Expected " + (int)DyeColor.GREEN.getData() + " but got " + (15 - reagent3.getDurability()));
 			return;
 		}
 
 		Block reagent1 = world.getBlockAt(reagent2.getX(), reagent2.getY()-1, reagent2.getZ());
 		log.info("Block under the block clicked is a " + reagent1.getType().name());
 		
 		//figure out if this matches a recipe, and if so, what should we do with it?
 		if (reagent2.getType() != Material.COBBLESTONE || reagent1.getType() != Material.SNOW_BLOCK){
 			log.info("Types of blocks in cauldron are wrong, should be cobblestone on snow, is " + reagent2.getType().name() + " on " + reagent1.getType().name());
 			return;
 		}
 		log.info("Hey, it's a cauldron and everything checks out!  Spawning lapis.");		
 
 		//looks good, spawn a lapis and delete those blocks
 		reagent2.setType(Material.AIR);
 		reagent1.setType(Material.AIR);
 		world.dropItemNaturally(loc, new ItemStack(Material.INK_SACK, 1, (byte) 4));
 		
 	}
 	
 	protected boolean is_on_cauldron(Location loc){
 		/**
 		 * Checks to see if the location is one block above an obsidian cauldron.
 		 * Cauldrons look like this:
 		 * Top:  Mid:  Bottom:
 		 * ***   ooo   ooo
 		 * *2*   o1o   ooo
 		 * ***   ooo   ooo
 		 * 
 		 * Key:	* = anything, probably air
 		 * 		2 = reagent 2, a placed block
 		 * 		1 = reagent 1, a placed block
 		 * 		o = obsidian
 		 */
 		int y = loc.getBlockY(); // y is used for height in minecraft
 		if (y <2 ){
 			return false;  //can't place a cauldron inside bedrock; also crashing is bad
 		}
 		int x = loc.getBlockX();
 		int z = loc.getBlockZ();
 	
 		World world = loc.getWorld();
 		
 		//begin ugly hard-coded checks.
 		//maybe there's a more graceful way to do this, but this should be fast
 		
 		//two blocks under; this should be the fastest disqualifier in most cases
 		if (world.getBlockAt(x, y-2, z).getType() != Material.OBSIDIAN){
 			return false;
 		}
 		
 		//around the ring for tier2
 		if (world.getBlockAt(x-1, y-1, z-1).getType() != Material.OBSIDIAN){
 			return false;
 		}
 		if (world.getBlockAt(x, y-1, z-1).getType() != Material.OBSIDIAN){
 			return false;
 		}
 		if (world.getBlockAt(x+1, y-1, z-1).getType() != Material.OBSIDIAN){
 			return false;
 		}
 		if (world.getBlockAt(x-1, y-1, z+1).getType() != Material.OBSIDIAN){
 			return false;
 		}
 		if (world.getBlockAt(x, y-1, z+1).getType() != Material.OBSIDIAN){
 			return false;
 		}
 		if (world.getBlockAt(x+1, y-1, z+1).getType() != Material.OBSIDIAN){
 			return false;
 		}
 	
 		//We don't check x,y-1,z, because that should be the location of reagent 1
 		if (world.getBlockAt(x-1, y-1, z).getType() != Material.OBSIDIAN){
 			return false;
 		}
 		if (world.getBlockAt(x+1, y-1, z).getType() != Material.OBSIDIAN){
 			return false;
 		}
 		
 		//Bottom tier
 		if (world.getBlockAt(x-1, y-2, z-1).getType() != Material.OBSIDIAN){
 			return false;
 		}
 		if (world.getBlockAt(x, y-2, z-1).getType() != Material.OBSIDIAN){
 			return false;
 		}
 		if (world.getBlockAt(x+1, y-2, z-1).getType() != Material.OBSIDIAN){
 			return false;
 		}
 		if (world.getBlockAt(x-1, y-2, z+1).getType() != Material.OBSIDIAN){
 			return false;
 		}
 		if (world.getBlockAt(x, y-2, z+1).getType() != Material.OBSIDIAN){
 			return false;
 		}
 		if (world.getBlockAt(x+1, y-2, z+1).getType() != Material.OBSIDIAN){
 			return false;
 		}
 	
 		//we don't check x,y-2,z because we already did that
 		
 		if (world.getBlockAt(x-1, y-2, z).getType() != Material.OBSIDIAN){
 			return false;
 		}
 		if (world.getBlockAt(x+1, y-2, z).getType() != Material.OBSIDIAN){
 			return false;
 		}
 		
 		//if we got here, it's a cauldron
 		return true;
 	}
 
 
 }
