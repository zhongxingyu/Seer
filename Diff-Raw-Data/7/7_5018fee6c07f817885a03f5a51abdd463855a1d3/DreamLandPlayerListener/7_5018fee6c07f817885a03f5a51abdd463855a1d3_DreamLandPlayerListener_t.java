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
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.event.player.PlayerRespawnEvent;
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
 						if (plugin.flyTool.contains("-1") || plugin.flyTool.contains(Integer.toString(event.getPlayer().getItemInHand().getTypeId())))
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
 				leaveDreamLand(player);
 			}
 			if(playerSpawn(player))
 			{
 				try
 				{
 					player.teleport(getSpawn());
 				}
 				catch (java.lang.NullPointerException e)
 				{
 					player.teleport(plugin.dreamWorld().getSpawnLocation());
 				}
 			}
 			if(plugin.morningReturn)
 			{
 				long time = loadLocation(player).getWorld().getTime();
 				if(time >=0 && time <= 12000)
 				{
 					player.sendMessage("It is morning, WAKEUP!");
 					leaveDreamLand(player);
 				}
 			}
 			
 		}
 	}
 	
 	public void onPlayerBedEnter(PlayerBedEnterEvent event)
 	{
 		Player player = event.getPlayer();
 		if (plugin.checkpermissions(player,"dreamland.goto",true) && !getLock(event.getPlayer()))
 		{
 			if(plugin.attemptWait == 0 || getWait(player))
 			{
 				if (new Random().nextInt(plugin.chance) == 0)
 				{
 					enterDreamLand(player, event.getBed().getLocation());
 	    		}
     		}
     		if(!attemptFile(player).exists())
     		{
     			setAttemptTime(player);
     		}
     	}
     }
 	
 	public void onPlayerQuit(PlayerQuitEvent event)
 	{
     	Player player = event.getPlayer();
     	
     	if (playerInDreamLand(player))
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
 				savePlayerInv(player, plugin.dreamWorld());
 				player.getInventory().clear();
 				loadPlayerInv(player, loc.getWorld());
 			}
 			log.info(player.getName() + " left DreamLand");
 		}
 	}
 
 	public void onPlayerRespawn(PlayerRespawnEvent event)
 	{
 		Player player = event.getPlayer();
     	if (playerInDreamLand(player))
 		{
     		leaveDreamLand(player);
 		}
 	}
 
 	
 	
 	//helper functions
  
 	private Boolean playerInDreamLand(Player player)
 	{
 		return player.getWorld().getName().equalsIgnoreCase(plugin.dreamWorld().getName());
 	}
 
 	private void noWeather(Player player)
 	{
 		World world = player.getWorld();
 		world.setStorm(false);
 		world.setThundering(false);
 		world.setWeatherDuration(0);
 		//TODO have this happen less often
 	}
 
     private void message(Player player)
     { 
     	File save = new File(plugin.getDataFolder().getAbsolutePath() + File.separator + "message.txt");
 		if (!save.exists()) 
 		{
 			return;
 		}
 		
 		try 
 		{
 			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(save)));
 
 			String inputLine = br.readLine();
 			
 			while(inputLine != null) 
 			{
 				player.sendMessage(inputLine);
 				inputLine = br.readLine();
 			}
 			
 		}
 		catch (IOException e) 
 		{
 			e.printStackTrace();
 		}
     }
 
     private void enterDreamLand(Player player, Location bed)
     {
     	createLock(player);
 		
 		saveLocation(player, bed);
 
 		if(plugin.seperateInv)
 		{
 			savePlayerInv(player, player.getWorld());
 			loadPlayerInv(player, plugin.dreamWorld());
 		}
 		
 		clearAttempt(player);
 		
 		Location loc = getSpawn();
 						
 		try
 		{
 			player.teleport(loc);
 			playerSetSpawn(player);
 		}
 		catch (java.lang.NullPointerException e)
 		{
 			loc = plugin.dreamWorld().getSpawnLocation();					
 			player.teleport(loc);
 			saveSpawn(player);
 			playerSetSpawn(player);
 		}
 		
 		removeLock(player);
 		if(plugin.message)
 		{
 			message(player);
 		}
     	log.info(player.getName() + " went to Dream Land");
     	return;
     }
  
     private void leaveDreamLand(Player player)
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
 			savePlayerInv(player, plugin.dreamWorld());
 			player.getInventory().clear();
 			loadPlayerInv(player, loc.getWorld());
 		}
 		log.info(player.getName() + " left DreamLand");
     }
     
     
     
     //wait time
 	private File attemptFile(Player player)
 	{
 		File attemptFolder = new File(plugin.getDataFolder().getAbsolutePath() + File.separator + "Attempts");
 		if (!attemptFolder.exists()) 
 		{
 			attemptFolder.mkdir();
 		}
 		return new File(attemptFolder + File.separator + player.getName());
 	}
 	
 	private void setAttemptTime(Player player)
 	{
 		BufferedWriter bw;
 		try 
 		{
 			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(attemptFile(player))));
 			Long temp = plugin.getServer().getWorlds().get(0).getTime();
 			bw.write(temp.toString());
 			bw.close();
 		}
 		catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 		catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 	}
 	
 	private void clearAttempt(Player player)
 	{
 		attemptFile(player).delete();
 	}
 	
 	private Long getAttemptTime(Player player)
 	{
 		File save = attemptFile(player);
 		Long retVal = Long.parseLong("0");
 		if (!save.exists()) 
 		{
 			return retVal;
 		}
 		
 		try 
 		{
 			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(save)));
 
 			String inputLine = br.readLine();
 			
 			if (inputLine == null) 
 			{
 				return retVal;
 			}
 			
 			inputLine = inputLine.replace(',', '.');
 			return Long.parseLong(inputLine);
 		}
 		catch (IOException e) 
 		{
 			e.printStackTrace();
 		}
 		catch (java.lang.NumberFormatException e)
 		{
 			return retVal;
 		}
 
 		return retVal;
 	}
 	
 	private Boolean getWait(Player player)
 	{
 		Long time = plugin.getServer().getWorlds().get(0).getTime() - getAttemptTime(player);
 		if(time >= plugin.attemptWait)
 		{
 			setAttemptTime(player);
 			return true;
 		}
    		else
    		{
    			player.sendMessage("Wait " + ((Long)((plugin.attemptWait - time)/30)).toString() + "s before trying again");
 			return false;
    		}
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
 
	@SuppressWarnings("deprecation")
 	private void loadPlayerInv(Player player, World world)
 	{
		player.getInventory().clear();
 		File save = playerInv(player, world);
 		try 
 		{
 			if (!save.exists()) 
 			{
 				if(plugin.kit && world.getName() == plugin.dreamWorld().getName())
 				{
 					save = new File(plugin.getDataFolder().getAbsolutePath() + File.separator + "kit.txt");
 					if (!save.exists()) 
 					{
 						return;
 					}
 					BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(save)));
 					
 					List<String> inv = new ArrayList<String>();
 					String inputLine = br.readLine();
 					
 					int count =0;
 					while (inputLine != null)
 					{
 						inv.add(count + " " + inputLine + " 0");
 						inputLine = br.readLine();
 						count++;
 					}
 					
 					stringToInv(player, inv);
 					return;
 				}
 				else
 				{
 					return;
 				}
 			}
 		
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
		//only way to get inv to update 
		//TODO Do this properly!
		player.updateInventory();
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
 			
 			inputLine = inputLine.replace(',', '.');
 			
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
 		File save = spawnWorldFile(plugin.dreamWorld());
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
 			inputLine = inputLine.replace(',', '.');
 			String splits[] = inputLine.split(" ", 3);
 			return new Location(plugin.dreamWorld(), Double.parseDouble(splits[0]), Double.parseDouble(splits[1]), Double.parseDouble(splits[2]));
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
 			File save = spawnWorldFile(plugin.dreamWorld());
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
