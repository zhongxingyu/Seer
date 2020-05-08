 package me.cmesh.DreamLand;
 
 
 import org.bukkit.block.BlockFace;
 //import org.bukkit.block.Block;
 import java.io.OutputStreamWriter;
 import org.bukkit.entity.Player;
 import org.bukkit.event.block.Action;
 //import org.bukkit.event.player.PlayerBedLeaveEvent;
 import org.bukkit.event.player.PlayerBedEnterEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerListener;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.event.player.PlayerPortalEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.util.Vector;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 import org.bukkit.Location;
 import org.bukkit.World;
 
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.BufferedReader;
 import java.util.logging.Logger;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 
 import java.io.BufferedWriter;
 
 public class DreamLandPlayerListener extends PlayerListener
 {
 	public static DreamLand plugin;
 	public static final Logger log = Logger.getLogger("Minecraft");
 	
     public DreamLandPlayerListener(DreamLand instance)
     {
         plugin = instance;
     }
 
     
     //Main Functions
     public void onPlayerPortal(PlayerPortalEvent event)
     {
     	if (playerInDreamLand(event.getPlayer()))
     	{
     		event.setCancelled(true);
     	}
     }
   
     public void onPlayerInteract(PlayerInteractEvent event)
     {
     	if (plugin.dreamFly)
     	{
     		Player player = event.getPlayer();
     		if (plugin.checkpermissions(player,"dreamland.fly",true))
     		{
     			if (playerInDreamLand(player))
     			{
     				if (event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK))
     				{
     		    		boolean tool = false;
     		    		if (plugin.flyTool.contains("-1"))
     		    		{
     		    			tool = true;
     		    		}
     		    		if (plugin.flyTool.contains(Integer.toString(event.getPlayer().getItemInHand().getTypeId())))
     		    		{
     		    			tool = true;
     		    		}
 
     		    		if (tool)
     					{
     		    			Vector dir = player.getLocation().getDirection().multiply(plugin.flySpeed);
     		    			dir.setY(0.75);
     		    			player.setVelocity(dir);
     			        	player.setFallDistance(0);
     					}
     				}
     			}
     		}
     	}
     }
     
     public void onPlayerMove(PlayerMoveEvent event)
     {
     	Player player = event.getPlayer();
     	if (plugin.portalExplode)
     	{
     		if (playerInDreamLand(player))
     		{
     			Location portal = event.getTo();
             	if (portal.getBlock().getTypeId() == 90)
             	{
             		portal.getWorld().createExplosion(portal.getBlock().getRelative(BlockFace.UP).getLocation(),5);
             	}
             	else if (portal.getBlock().getRelative(BlockFace.UP).getTypeId() == 90)
             	{
             		portal.getWorld().createExplosion(portal.getBlock().getRelative(BlockFace.UP).getLocation(),5);
             	}
     		}
     	}
     	if (playerInDreamLand(player))
     	{
     		noWeather(player);
     		if (event.getTo().getY() < 0)
     		{
     			Location loc = null;
     			try
     			{
 	    			player.setFallDistance(0);
 	    			loc = loadLocation(player);
 	    			loc.setY(loc.getY()+1.5);
 	    			
 	    			player.setFallDistance(0);
 	    			player.teleport(loc);
 	    			player.setFallDistance(0);
     			}
     			catch (java.lang.NullPointerException e)
     			{
 					loc = plugin.getServer().getWorlds().get(0).getSpawnLocation();
 					player.setFallDistance(0);
 					player.teleport(loc);
 					player.setFallDistance(0);
     			}
     			if(plugin.seperateInv)
 				{
 					savePlayerInv(player, dreamWorld());
 					player.getInventory().clear();
 					loadPlayerInv(player, loc.getWorld());
 				}
     			log.info(player.getName() + " left DreamLand");
     		}
     		if(playerSpawn(player))
     		{
     			try
     			{
     				player.teleport(getSpawn());
     			}
     			catch (java.lang.NullPointerException e)
     			{
     				player.teleport(dreamWorld().getSpawnLocation());
     			}
     		}
     	}
     }
 	
     public void onPlayerBedEnter(PlayerBedEnterEvent event)
     {
     	Player player = event.getPlayer();
     	if (plugin.checkpermissions(player,"dreamland.goto",true) && !getLock(event.getPlayer()))
     	{
     		if (new Random().nextInt(plugin.chance) == 0)
     		{
 	    		createLock(player);
 	    		
 				saveLocation(player, event.getBed().getLocation());
 
 				if(plugin.seperateInv)
 				{
 					savePlayerInv(player, player.getWorld());
 					loadPlayerInv(player, dreamWorld());
 				}
 				
 				Location loc = getSpawn();
 				
 			
 				try
 				{
 					player.teleport(loc);
 					playerSetSpawn(player);
 				}
 				catch (java.lang.NullPointerException e)
 				{
 					loc = dreamWorld().getSpawnLocation();					
 					player.teleport(loc);
 					saveSpawn(player);
 					playerSetSpawn(player);
 				}
 				
 				
 				
 				
 				removeLock(event.getPlayer());
 				
 		    	log.info(event.getPlayer().getName() + " went to Dream Land");
     		}
     	}
     }
 
     
     //helper functions
     private World dreamWorld()
     {
     	return plugin.getServer().getWorld(plugin.getServer().getWorlds().get(0).getName()+"_skylands");
     }
 
     private Boolean playerInDreamLand(Player player)
     {
     	return player.getWorld().getName().equalsIgnoreCase(dreamWorld().getName());
     }
 
     private void noWeather(Player player)
     {
     	World world = player.getWorld();
     	world.setStorm(false);
     	world.setThundering(false);
     	world.setWeatherDuration(0);
     	//TODO have this happen less often
     }
 
     
     //Inventory store/switcher
     private File playerInv(Player player, World world)
     {
     	File invFolder = new File(plugin.getDataFolder().getAbsolutePath() + File.separator + "Inventories");
 		if (!invFolder.exists()) 
 		{
 			invFolder.mkdir();
 		}
 		return new File(invFolder + File.separator + player.getName() + "." + world.getName());
     }
     
     private void savePlayerInv(Player player, World world)
     {
     	BufferedWriter bw;
 		try 
 		{
 			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(playerInv(player, world))));
 			
 			ItemStack [] inv =player.getInventory().getContents();
 			
 	    	for(int i = 0; i<inv.length; i++)
 	    	{
 	    		ItemStack item = inv[i];
 	    		String temp = "Empty";
 	    		try
 	    		{
 	    			temp = i + " " + item.getTypeId() + " " + item.getAmount() + " "+ item.getDurability();
 	    			bw.write(temp);
 	    			bw.newLine();
 	    		}
 	    		catch (java.lang.NullPointerException e)
 	    		{
 	    			//log.info("Exception");
 	    		}
 	    	}
 	    	bw.close();
 		}
 		catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 		catch (IOException e) {
 			e.printStackTrace();
 		}
     }
 
 
     @SuppressWarnings("deprecation")
 	private void stringToInv(Player player, List<String> inv)
     {
     	player.getInventory().clear();
     	
 		for(String item : inv)
 		{
 			try
 	    	{
     			String [] split = item.split(" ", 4);
     			
     			int spot = Integer.parseInt(split[0]);
     			int itemId = Integer.parseInt(split[1]);
     			int ammount = Integer.parseInt(split[2]);
     			short damage = (short)Integer.parseInt(split[3]);
     			
     			player.getInventory().setItem(spot, new ItemStack(itemId, ammount, damage));
 
 	    	}
 	    	catch (java.lang.NumberFormatException e)
 	    	{
 	    		player.sendMessage("There was an issue loading your inventory");
 	    	}
 		}
 		//only way to get inv to update 
 		//TODO Do this properly!
 		player.updateInventory();
     }
 
     private void loadPlayerInv(Player player, World world)
     {
 		File save = playerInv(player, world);
 		if (!save.exists()) 
 		{
 			if(plugin.kit && world.getName() == dreamWorld().getName())
 			{
 				save = new File(plugin.getDataFolder().getAbsolutePath() + File.separator + "kit.txt");
 				if (!save.exists()) 
 				{
 					return;
 				}
 			}
 			else
 			{
 				return;
 			}
 		}
 		try 
 		{
 			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(save)));
 			
 			List<String> inv = new ArrayList<String>();
 			String inputLine = br.readLine();
 			
 			while (inputLine != null)
 			{
 				inv.add(inputLine);
 				inputLine = br.readLine();
 			}
 			
 			stringToInv(player, inv);
 		}
 		catch (IOException e) 
 		{
 			e.printStackTrace();
 		}
 		catch (java.lang.NumberFormatException e)
 		{
 			e.printStackTrace();
 		}
     }
     
     
     //used to tp 2 extra times (accounts for chunk loading time)
 	private File spawnFile(Player player)
 	{
 		File lockFolder = new File(plugin.getDataFolder().getAbsolutePath() + File.separator + "Spawning");
 		if (!lockFolder.exists()) 
 		{
 			lockFolder.mkdir();
 		}
 		return new File(lockFolder + File.separator + player.getName());
 	}
     
 	private Boolean playerSpawn(Player player)
 	{
 		File file1 = new File(spawnFile(player) + ".1");
 		File file2 = new File(spawnFile(player) + ".2");
 		if((file2).exists())
 		{
 			file2.delete();
 			return false;
 		}
 		if((file1).exists())
 		{
 			file1.delete();
 			return true;
 		}
 		return false;
 	}
 	
 	private void playerSetSpawn(Player player)
 	{
 		File file1 = new File(spawnFile(player) + ".1");
 		File file2 = new File(spawnFile(player) + ".2");
 		try
 		{
 			file1.createNewFile();
 			file2.createNewFile();
 		}
 		catch (IOException e)
 		{
 			e.toString();
 		}
 	}
 
 	
 	//saves bed locations of players
 
 	//used to manage bed location
 	private Location loadLocation(Player player) 
 	{
 		File save = getBedFile(player);
 		if (!save.exists()) 
 		{
 			return null;
 		}
 		
 		try 
 		{
 			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(save)));
 			
 			String world = br.readLine();
 			String inputLine = br.readLine();
 			
 			if (inputLine == null || world == null) 
 			{
 				return null;
 			}
 			
			inputLine.replace(',', '.');
			
 			String splits[] = inputLine.split(" ", 3);
 			return new Location(plugin.getServer().getWorld(world), Double.parseDouble(splits[0]), Double.parseDouble(splits[1]), Double.parseDouble(splits[2]));
 		}
 		catch (IOException e) 
 		{
 			e.printStackTrace();
 		}
 		catch (java.lang.NumberFormatException e)
 		{
 			return null;
 		}
 
 		return null;
 	}
 	
 	public void saveLocation(Player player, Location location) 
 	{
 		BufferedWriter bw;
 		try 
 		{
 			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(getBedFile(player))));
 			bw.write(player.getWorld().getName());
 			bw.newLine();
 			bw.write(String.format("%f %f %f", location.getX(), location.getY(), location.getZ()));
 			bw.close();
 		}
 		catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 		catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	private File getBedFile(Player player)
 	{
 		File bedFolder = new File(plugin.getDataFolder().getAbsolutePath() + File.separator + "BedLocations");
 		if (!bedFolder.exists()) 
 		{
 			bedFolder.mkdir();
 		}
 		return new File(bedFolder + File.separator + player.getName());
 	}
 
 
 	//used to prevent concurrent modification exceptions
 
 	//used to manage lock file to prevent concurrent modification exception
 	private File lockFile(Player player)
 	{
 		File lockFolder = new File(plugin.getDataFolder().getAbsolutePath() + File.separator + "Lock");
 		if (!lockFolder.exists()) 
 		{
 			lockFolder.mkdir();
 		}
 		return new File(lockFolder + File.separator + player.getName() + ".lock");
 	}
 	
 	private void createLock(Player player)
 	{
 		try
 		{
 			lockFile(player).createNewFile();
 		}
 		catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	private Boolean getLock(Player player)
 	{
 		return lockFile(player).exists();
 	}
 	
 	private void removeLock(Player player)
 	{
 		lockFile(player).delete();
 	}
 
 	
 	//used to save the spawn location of a skylands world
 
 	//used to manage world spawn
 	private File spawnWorldFile(World world)
 	{
 		File spawnFolder = new File(plugin.getDataFolder().getAbsolutePath() + File.separator + "WorldSpawn");
 		if (!spawnFolder.exists()) 
 		{
 			spawnFolder.mkdir();
 		}
 		return new File(spawnFolder + File.separator + world.getName() + ".spawn");
 	}
 	
     private Location getSpawn() 
 	{
 		File save = spawnWorldFile(dreamWorld());
 		if (!save.exists()) 
 		{
 			return null;
 		}
 		
 		try 
 		{
 			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(save)));
 			String inputLine = br.readLine();
 			if (inputLine == null) 
 			{
 				return null;
 			}
			inputLine.replace(',', '.');
 			String splits[] = inputLine.split(" ", 3);
 			return new Location(dreamWorld(), Double.parseDouble(splits[0]), Double.parseDouble(splits[1]), Double.parseDouble(splits[2]));
 		}
 		catch (IOException e) 
 		{
 			e.printStackTrace();
 		}
 		catch (java.lang.NumberFormatException e)
 		{
 			return null;
 		}
 
 		return null;
 	}
 
     public void saveSpawn(Player player) 
 	{
 		if(plugin.checkpermissions(player,"dreamland.setdreamspawn",true) && playerInDreamLand(player) && player.getLocation().getY() > 0)
  		{
 			Location location = player.getLocation();
 			location.setY(location.getY() + 5);
 			File save = spawnWorldFile(dreamWorld());
 			BufferedWriter bw;
 			try {
 				bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(save)));
 				bw.write(String.format("%f %f %f", location.getX(), location.getY(), location.getZ()));
 				bw.close();
 			}
 			catch (FileNotFoundException e) {
 				e.printStackTrace();
 			}
 			catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 }   
