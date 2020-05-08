 package me.cvenomz.OreGrow;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Properties;
 import java.util.logging.Logger;
 
 import org.bukkit.CoalType;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.event.Event;
 import org.bukkit.event.Event.Type;
 import org.bukkit.event.player.PlayerListener;
 import org.bukkit.inventory.FurnaceRecipe;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.ShapelessRecipe;
 import org.bukkit.material.Coal;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class OreGrow extends JavaPlugin{
 
 	File mainDirectory;
 	File furnacesFile;
 	Properties furnaces = new Properties();
 	PlayerListener playerListener;
 	Logger log = Logger.getLogger("Minecraft");
	double version = 0.4;
 	
 	@Override
 	public void onDisable() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void onEnable() {
 		// TODO Auto-generated method stub
 		mainDirectory = new File("plugins" + File.separator + "OreGrow");
 		if (!mainDirectory.exists())
 			mainDirectory.mkdir();
 		try {
 			furnacesFile = new File(mainDirectory.getCanonicalPath() + File.separator + "furnaces.properties");
 		} catch (IOException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 		if (!furnacesFile.exists())
 			try {
 				furnacesFile.createNewFile();
 			} catch (IOException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			}
 		try {
 			furnaces.load(new FileInputStream(mainDirectory.getCanonicalPath() + File.separator + "furnaces.properties"));
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		playerListener = new OGPlayerListener(this);
 		getServer().getPluginManager().registerEvent(Type.PLAYER_INTERACT, playerListener, Event.Priority.Normal, this);
 		
 		FurnaceRecipe r = new FurnaceRecipe(new ItemStack(Material.COBBLESTONE, 1), Material.COAL_ORE);
 		getServer().addRecipe(r);
 		ShapelessRecipe s1 = new ShapelessRecipe(new ItemStack(Material.COAL_ORE, 1));
 		s1.addIngredient(4, Material.COAL);
 		getServer().addRecipe(s1);
 		ShapelessRecipe s2 = new ShapelessRecipe(new ItemStack(Material.COAL_ORE, 1));
 		Coal coal = new Coal(CoalType.CHARCOAL);
 		s2.addIngredient(4, coal);
 		getServer().addRecipe(s2);
 		
 		
 		
 		OreGrowThread oreGrowThread = new OreGrowThread(this);
 		int taskID = getServer().getScheduler().scheduleSyncRepeatingTask(this, oreGrowThread, 1, 1);
 		//log.info(""+taskID);
 		
 		log.info("[OreGrow] version " + version + " initialized");
 		
 	}
 	
 	public void addFurnace(Block block)
 	{
 		if (furnaces.containsKey(blockToString(block)))
 		{
 			
 		}
 		else
 		{
 			furnaces.put(blockToString(block), "0");
 			writeFurnaces();
 		}
 	}
 	
 	public String blockToString(Block block)
 	{
 		return block.getWorld().getName() + "," + block.getX() + "," + block.getY() + "," + block.getZ() + ",";
 	}
 	
 	/*public Properties getFurnaces()
 	{
 		return furnaces;
 	}*/
 	
 	private void writeFurnaces()
 	{
 		FileOutputStream out;
 		try {
 			out = new FileOutputStream(furnacesFile);
 			furnaces.store(out, "comments");
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 			log.severe("[OreGrow] Could not find furnaces file");
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			log.severe("[OreGrow] Could not write furnaces file");
 		}
 	}
 	
 	public List<Block> getFurnaces()
 	{
 		String str;
 		List<Block> list = new LinkedList<Block>();
 		for (Object obj : furnaces.keySet())
 		{
 			str = (String)obj;
 			Block block = getBlock(str);
 			if (block.getType() == Material.FURNACE || block.getType() == Material.BURNING_FURNACE)
 				list.add(block);
 			else
 				removeFurnace(str);
 		}
 		
 		return list;
 	}
 	
 	private Block getBlock(String str)
 	{
 		String tmp[] = new String[4];
 		int index = 0;
 		for (int i=0; i<4; i++)
 		{
 			tmp[i] = str.substring(index, (str.indexOf(",", index) < 0 ? str.length() : str.indexOf(",", index))); //substring until next occurance of ',' but check that there actually is another occurance
 			//log.info(tmp[i]);
 			index = str.indexOf(",", index)+1;
 		}
 		Block b = getServer().getWorld(tmp[0]).getBlockAt(Integer.parseInt(tmp[1]), Integer.parseInt(tmp[2]), Integer.parseInt(tmp[3]));
 		
 		return b;
 	}
 	
 	private void removeFurnace(String str)
 	{
 		furnaces.remove(str);
 	}
 	
 	public int getFurnaceValue(String str)
 	{
 		//log.info(str);
 		return Integer.parseInt((String)furnaces.get(str));
 	}
 	
 	public void setFurnaceValue(String str, Integer value)
 	{
 		furnaces.setProperty(str, value.toString());
 		writeFurnaces();
 	}
 
 }
