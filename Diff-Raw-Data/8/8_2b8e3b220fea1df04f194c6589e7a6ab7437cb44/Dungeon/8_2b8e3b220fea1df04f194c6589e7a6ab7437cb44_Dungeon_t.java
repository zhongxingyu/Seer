 package net.virtuallyabstract.minecraft;
 
 import org.bukkit.plugin.java.*;
 import org.bukkit.plugin.*;
 import org.bukkit.event.*;
 import org.bukkit.event.player.*;
 import org.bukkit.entity.*;
 import org.bukkit.inventory.*;
 import org.bukkit.*;
 import org.bukkit.block.*;
 import org.bukkit.command.*;
 import org.bukkit.material.*;
 import org.bukkit.enchantments.*;
 import javax.script.*;
 import java.util.*;
 import java.io.*;
 
 public class Dungeon implements Comparable<Dungeon>
 {
 	private World world;
 	private DungeonBuilder plugin;
 	private String owner, name;
 	private Location start, center, exit, teleporter, exitdest, sphereCenter;
 	private int width, depth, height, lives = -1;
 	private String exp = "-1";
 	private long dungeonCooldown = 0;
 	private ArrayList<BlockInfo> blocks, origBlocks = null;
 	private ArrayList<Entity> liveMonsters;
 	private ArrayList<String> defaultPermissions;
 	private ArrayList<MonsterInfo> savedMonsters;
 	private HashSet<String> invPermits;
 	private HashSet<String> coOwners;
 	private HashMap<String, Long> playerCooldowns;
 	private HashMap<String, LocationWrapper> monsterTriggers, scriptTriggers, savePoints;
 	private HashMap<String, ArrayList<ItemStack>> storedInventories;
 	private volatile boolean published, autoload = true, allowSpawn = false, loading = false;
 	private Object loadingLock;
 	private double reward, sphereRadius;
 	private int partySizeMin = 1, partySizeMax = 1;
 	private ArrayList<Player> currentPlayers = new ArrayList<Player>();
 	private ArrayList<DungeonParty> partyList = new ArrayList<DungeonParty>();
 	private volatile DungeonParty activeParty;
 	private volatile PartyStatus currentStatus;
 	private HashSet<Material> ignoreTypes = new HashSet<Material>();
 	private static int BLOCK_FILE_VERSION = 1;
 
 	public enum PartyStatus
 	{
 		MISSING_PLAYERS, FULL, READY, EMPTY, INQUEUE, TOOLARGE;
 	}
 
 	private void init()
 	{
 		blocks = new ArrayList<BlockInfo>();
 		liveMonsters = new ArrayList<Entity>();
 		savedMonsters = new ArrayList<MonsterInfo>();
 		defaultPermissions = new ArrayList<String>();
 		monsterTriggers = new HashMap<String, LocationWrapper>();
 		scriptTriggers = new HashMap<String, LocationWrapper>();
 		savePoints = new HashMap<String, LocationWrapper>();
 		playerCooldowns = new HashMap<String, Long>();
 		storedInventories = new HashMap<String, ArrayList<ItemStack>>();
 		coOwners = new HashSet<String>();
 		invPermits = new HashSet<String>();
 		currentStatus = PartyStatus.EMPTY;
 		loadingLock = new Object();
 	}
 
 	public Dungeon(String name, String owner, Location center, File dungeonFile, DungeonBuilder plugin)
 		throws Exception
 	{
 		this.name = name;
 		this.owner = owner;
 		this.start = center.clone();
 		this.exit = center.clone();
 		this.center = center;
 		this.published = false;
 		this.world = center.getWorld();
 		this.reward = 0.0;
 		this.plugin = plugin;
 		init();
 		loadFromFile(dungeonFile, null, true);
 		refreshExitBlock();
 		calculateSphere();
 	}
 
 	public Dungeon(String name, String owner, Location center, int width, int depth, int height, DungeonBuilder plugin)
 		throws Exception
 	{
 		this.owner = owner;
 		this.start = center.clone();
 		this.exit = center.clone();
 		this.exit.setX(this.exit.getX()-1);
 		this.center = center;
 		this.width = width;
 		this.depth = depth;
 		this.height = height;
 		this.name = name;
 		this.published = false;
 		this.world = center.getWorld();
 		this.reward = 0.0;
 		this.plugin = plugin;
 		init();
 		refreshExitBlock();
 		calculateSphere();
 		saveDungeon();
 	}
 
 	public Dungeon(String name, String owner, DungeonBuilder plugin)
 		throws Exception
 	{
 		this.owner = owner;
 		this.name = name;
 		this.published = false;
 		this.reward = 0.0;
 		this.plugin = plugin;
 		init();
 		loadFromFile(plugin.server);
 		calculateSphere();
 	}
 
 	public void setOriginalBlocks(ArrayList<BlockInfo> blocks)
 	{
 		origBlocks = blocks;	
 	}
 
 	public boolean canUndo()
 	{
 		return origBlocks != null;
 	}
 
 	public World getWorld()
 	{
 		return world;
 	}
 
 	public int getWidth()
 	{
 		return width;
 	}
 
 	public int getDepth()
 	{
 		return depth;
 	}
 
 	public int getHeight()
 	{
 		return height;
 	}
 
 	public boolean containsLocation(Location l)
 	{
 		return containsLocation(l, 0);
 	}
 
 	public boolean containsLocation(Location l, int buffer)
 	{
 		if(!l.getWorld().getName().equals(world.getName()))
 			return false;
 
 		double x = l.getX();
 		double y = l.getY();
 		double z = l.getZ();
 
 		double halfdepth = (depth / 2) + buffer;
 		double minx = center.getX() - halfdepth;
 		double maxx = center.getX() + halfdepth;
 
 		double halfwidth = (width / 2) + buffer;
 		double minz = center.getZ() - halfwidth;
 		double maxz = center.getZ() + halfwidth;
 
 		double miny = center.getY() - buffer - 1.0;
 		double maxy = center.getY() + height - 1.0 + buffer;
 
 		if(x >= minx && x <= maxx && z >= minz && z <= maxz && y >= miny && y <= maxy)
 			return true;
 
 		return false;
 	}
 	
 	public Location setStartingLocation(Location l)
 	{
 		Location retVal = start;
 		this.start = l;
 
 		//try
 		//{
 		//	saveDungeon();
 		//}
 		//catch(Exception e)
 		//{
 		//	e.printStackTrace();
 		//}
 
 		return retVal;
 	}
 
 	public Location getStartingLocation()
 	{
 		return this.start;
 	}
 
 	public Location getExitLocation()
 	{
 		return this.exit;
 	}
 	
 	public Location setExitLocation(Location l)
 	{
 		Location temp = exit.clone();
 		temp.setY(temp.getY() - 1);
 		Block b = temp.getBlock();
 		Block b2 = b.getRelative(-1, 0, 0);
 		b.setType(b2.getType());
 
 		Location retVal = exit;
 		exit = l;
 
 		refreshExitBlock();
 
 		//try
 		//{
 		//	saveDungeon();
 		//}
 		//catch(Exception e)
 		//{
 		//	e.printStackTrace();
 		//}
 
 		return retVal;
 	}
 
 	public void refreshExitBlock()
 	{
 		Location temp = exit.clone();
 		temp.setY(temp.getY() - 1);
 		Block b = temp.getBlock();
 		b.setType(Material.DIAMOND_BLOCK);
 	}
 
 	public Location getCenterLocation()
 	{
 		return this.center;
 	}
 
 	public String getName()
 	{
 		return name;
 	}
 
 	public boolean rename(String newName)
 	{
 		String baseName = DungeonBuilder.dungeonRoot + "/" + owner + "/" + name;
 		String baseNameNew = DungeonBuilder.dungeonRoot + "/" + owner + "/" + newName;
 
 		File test = new File(baseName);
 		File testDest = new File(baseNameNew);
 		if(test.exists() && !testDest.exists())
 		{
 			boolean result = test.renameTo(testDest);
 			if(!result)
 				return false;
 		}
 
 		test = new File(baseName + ".groovy");
 		testDest = new File(baseNameNew + ".groovy");
 		if(test.exists() && !testDest.exists())
 		{
 			boolean result = test.renameTo(testDest);
 			if(!result)
 				return false;
 		}
 
 		test = new File(baseName + ".js");
 		testDest = new File(baseNameNew + ".js");
 		if(test.exists() && !testDest.exists())
 		{
 			boolean result = test.renameTo(testDest);
 			if(!result)
 				return false;
 		}
 
 		test = new File(baseName + ".previous");
 		testDest = new File(baseNameNew + ".previous");
 		if(test.exists() && !testDest.exists())
 		{
 			boolean result = test.renameTo(testDest);
 			if(!result)
 				return false;
 		}
 
 		test = new File(baseName + ".perms");
 		testDest = new File(baseNameNew + ".perms");
 		if(test.exists() && !testDest.exists())
 		{
 			boolean result = test.renameTo(testDest);
 			if(!result)
 				return false;
 		}
 
 		test = new File(baseName + ".cooldowns");
 		testDest = new File(baseNameNew + ".cooldowns");
 		if(test.exists() && !testDest.exists())
 		{
 			boolean result = test.renameTo(testDest);
 			if(!result)
 				return false;
 		}
 
 		this.name = newName;
 
 		return true;
 	}
 
 	public void permitInventoryItem(String material)
 	{
 		HashSet<String> newInv = new HashSet<String>();
 		for(String item : invPermits)
 		{
 			if(item.startsWith("-"))
 				continue;
 
 			newInv.add(item);
 		}
 
 		newInv.add("+" + material);
 
 		invPermits = newInv;
 	}
 
 	public void restrictInventoryItem(String material)
 	{
 		HashSet<String> newInv = new HashSet<String>();
 		for(String item : invPermits)
 		{
 			if(item.startsWith("+"))
 				continue;
 
 			newInv.add(item);
 		}
 
 		newInv.add("-" + material);
 
 		invPermits = newInv;
 	}
 
 	public void keepInventoryItem(String material)
 	{
 		if(material.equals("NONE") || material.equals("ALL"))
 		{
 			HashSet<String> newInv = new HashSet<String>();
 			for(String item : invPermits)
 			{
 				if(item.startsWith("%"))
 					continue;
 				newInv.add(item);
 			}
 			invPermits = newInv;
 		}
 
 		if(!material.equals("ALL") || !material.equals("NONE"))
 		{
 			invPermits.remove("%ALL");
 			invPermits.remove("%NONE");
 		}
 
 		invPermits.add("%" + material);
 	}
 
 	public Set<String> listInventoryItems()
 	{
 		return new HashSet<String>(invPermits);
 	}
 
 	public void clearInventoryStart(Player p)
 	{
 		if(invPermits.size() == 0)
 			return;
 
 		backupInventory(p);
 
 		if(invPermits.contains("+ALL") || invPermits.contains("-NONE") || invPermits.size() == 0)
 			return;
 
 		PlayerInventory pi = p.getInventory();
 		if(invPermits.contains("-ALL") || invPermits.contains("+NONE"))
 		{
 			pi.setArmorContents(null);
 			pi.clear();
 
 			return;
 		}
 
 		Boolean permit = null;
 		for(String item : invPermits)
 		{
 			if(item.startsWith("+"))
 				permit = true;
 			if(item.startsWith("-"))
 				permit = false;
 
 			if(permit != null)
 				break;
 		}
 	
 		if(permit == null)
 			return;
 
 		ItemStack is = pi.getBoots();
 		if(is != null && !keepItem(permit, is))
 			pi.setBoots(null);	
 		is = pi.getChestplate();
 		if(is != null && !keepItem(permit, is))
 			pi.setChestplate(null);
 		is = pi.getHelmet();
 		if(is != null && !keepItem(permit, is))
 			pi.setHelmet(null);
 		is = pi.getLeggings();
 		if(is != null && !keepItem(permit, is))
 			pi.setLeggings(null);
 
 		ItemStack [] contents = pi.getContents();
 		for(ItemStack item : contents)
 		{
 			if(item == null)
 				continue;
 
 			if(!keepItem(permit, item))
 				pi.removeItem(item);
 		}
 
 	}
 
 	private boolean keepItem(boolean permit, ItemStack is)
 	{
 		if(is == null)
 			return true;
 
 		if(permit)
 		{
 			if(invPermits.contains("+" + is.getType().toString()))
 				return true;
 		}
 		else
 		{
 			if(!invPermits.contains("-" + is.getType().toString()))
 				return true;
 		}
 
 		return false;
 	}
 
 	public void clearInventoryConfig()
 	{
 		invPermits.clear();
 	}
 
 	public void clearInventoryExit(Player p)
 	{
 		if(invPermits.size() == 0)
 			return;
 
 		PlayerInventory pi = p.getInventory();
 		boolean keepAll = false;
 		try
 		{
 			boolean found = false;
 			for(String inv : invPermits)
 			{
 				if(inv.startsWith("%"))
 					found = true;
 			}
 
 			keepAll = !found;
 			if(invPermits.contains("%ALL"))
 				keepAll = true;
 
 			if(invPermits.contains("%NONE"))
 				return;
 
 			ArrayList<ItemStack> saved = storedInventories.get(p.getName());
 			ItemStack is = pi.getBoots();
 			if(is != null && is.getType() != Material.AIR && !invPermits.contains("%" + is.getType().toString()))
 				pi.setBoots(null);
 			else if(!saved.contains(is))
 				saved.add(is);
 
 			is = pi.getChestplate();
 			if(is != null && is.getType() != Material.AIR && !invPermits.contains("%" + is.getType().toString()))
 				pi.setChestplate(null);
 			else if(!saved.contains(is))
 				saved.add(is);
 
 			is = pi.getHelmet();
 			if(is != null && is.getType() != Material.AIR && !invPermits.contains("%" + is.getType().toString()))
 				pi.setHelmet(null);
 			else if(!saved.contains(is))
 				saved.add(is);
 
 			is = pi.getLeggings();
 			if(is != null && is.getType() != Material.AIR && !invPermits.contains("%" + is.getType().toString()))
 				pi.setLeggings(null);
 			else if(!saved.contains(is))
 				saved.add(is);
 
 			ItemStack [] contents = pi.getContents();
 			for(ItemStack item : contents)
 			{
 				if(item == null)
 					continue;
 
 				if(keepAll && !saved.contains(item))
 				{
 					saved.add(item);
 					continue;
 				}
 
 				if(item.getType() != Material.AIR && invPermits.contains("%" + item.getType().toString()) && !saved.contains(item))
 					saved.add(item);
 			}
 		}
 		finally
 		{
 			pi.clear();
 			restoreInventory(p);
 		}
 	}
 
 	public void backupInventory(Player p)
 	{
 		PlayerInventory pi = p.getInventory();
 
 		ArrayList<ItemStack> saved = new ArrayList<ItemStack>();	
 		saved.add(pi.getBoots());
 		saved.add(pi.getChestplate());
 		saved.add(pi.getHelmet());
 		saved.add(pi.getLeggings());
 
 		for(ItemStack is : pi.getContents())
 			saved.add(is);
 
 		storedInventories.put(p.getName(), saved);
 	}
 
 	public void restoreInventory(Player p)
 	{
 		if(!storedInventories.containsKey(p.getName()))
 			return;
 
 		ArrayList<ItemStack> saved = storedInventories.get(p.getName());
 		PlayerInventory pi = p.getInventory();
 
 		//I think this is silly, they return material type "AIR" when I call things like getBoots
 		//But if i try to call setBoots with the same item it blows up
 		ItemStack is = saved.get(0);
 		if(is != null && is.getType() == Material.AIR)
 			is = null;
 		if(!pi.getBoots().equals(is))
 			saved.add(pi.getBoots());
 		pi.setBoots(is);
 
 		is = saved.get(1);
 		if(is != null && is.getType() == Material.AIR)
 			is = null;
 		if(!pi.getChestplate().equals(is))
 			saved.add(pi.getChestplate());
 		pi.setChestplate(is);
 
 		is = saved.get(2);
 		if(is != null && is.getType() == Material.AIR)
 			is = null;
 		if(!pi.getHelmet().equals(is))
 			saved.add(pi.getHelmet());
 		pi.setHelmet(is);
 
 		is = saved.get(3);
 		if(is != null && is.getType() == Material.AIR)
 			is = null;
 		if(!pi.getLeggings().equals(is))
 			saved.add(pi.getLeggings());
 		pi.setLeggings(is);
 
 		boolean overflow = false;
 		savedloop: for(int i = 4; i < saved.size(); i++)
 		{
 			is = saved.get(i);
 
 			if(is == null)
 				continue;
 
 			if(is.getType() == Material.AIR)
 				continue;
 
 			int index = pi.firstEmpty();
 			if(index < 0 || index >= pi.getSize())
 			{
 				overflow = true;
 				world.dropItem(exitdest, is);
 			}
 			else
 			{
 				pi.setItem(index, is);
 			}
 		}
 
 		if(overflow)
 		{
 			p.sendMessage("You are out of inventory space");
 			p.sendMessage("Dropping surplus items at dungeon exit");
 		}
 
 		storedInventories.remove(p.getName());
 	}
 
 	public String getOwner()
 	{
 		return owner;
 	}
 
 	public Set<String> getCoOwners()
 	{
 		return new HashSet<String>(coOwners);
 	}
 
 	public boolean hasAccess(String playername)
 	{
 		if(owner.equals(playername))
 			return true;
 
 		if(coOwners.contains(playername))
 			return true;
 
 		return false;
 	}
 
 	public void addCoOwner(String playername)
 	{
 		coOwners.add(playername);
 	}
 
 	public void removeCoOwner(String playername)
 	{
 		coOwners.remove(playername);
 	}
 
 	public void publish(Location teleport)
 		throws Exception
 	{
 		this.published = true;
 
 		teleporter = teleport;
 
 		if(exitdest == null)
 		{
 			Location temp = teleporter.clone();
 			temp.setX(temp.getX() - 2.0);
 			exitdest = temp;
 		}
 
 		saveDungeon();
 	}
 
 	public void unpublish()
 		throws Exception
 	{
 		this.published = false;
 		//saveDungeon();
 	}
 
 	public boolean isPublished()
 	{
 		return published;
 	}
 
 	public Location getTeleporterLocation()
 	{
 		return teleporter;
 	}
 
 	public Location getExitDestination()
 	{
 		return exitdest;
 	}
 
 	public void setExitDestination(Location dest)
 		throws Exception
 	{
 		exitdest = dest;
 		//saveDungeon();
 	}
 
 	public void addDefaultPermission(String node)
 	{
 		defaultPermissions.add(node);
 	}
 
 	public void removeDefaultPermission(String node)
 	{
 		defaultPermissions.remove(node);
 	}
 
 	public void clearDefaultPermissions()
 	{
 		defaultPermissions.clear();
 	}
 
 	public ArrayList<String> listDefaultPermissions()
 	{
 		return new ArrayList<String>(defaultPermissions);
 	}
 
 	public boolean hasDefaultPermission(String node)
 	{
 		return defaultPermissions.contains(node);
 	}
 
 	public void setCooldown(long cooldown)
 	{
 		dungeonCooldown = cooldown;
 	}
 
 	public long getCooldown()
 	{
 		return dungeonCooldown;
 	}
 
 	public void addPlayerCooldown(Player p)
 	{
 		addPlayerCooldown(p.getName());
 	}
 
 	public void addPlayerCooldown(String playername)
 	{
 		playerCooldowns.put(playername, System.currentTimeMillis());	
 	}
 
 	public void clearPlayerCooldown(String playername)
 	{
 		playerCooldowns.remove(playername);
 	}
 
 	public boolean isPlayerOnCooldown(Player p)
 	{
 		return isPlayerOnCooldown(p.getName());
 	}
 
 	public boolean isPlayerOnCooldown(String playername)
 	{
 		if(!playerCooldowns.containsKey(playername))
 			return false;
 
 		if(dungeonCooldown == -1)
 			return true;
 
 		Long cooldown = playerCooldowns.get(playername);
 		Long current = System.currentTimeMillis();
 
 		if(current - cooldown > dungeonCooldown)
 		{
 			playerCooldowns.remove(playername);
 			return false;
 		}
 		
 		return true;
 	}
 	
 	//In milliseconds
 	public long timeLeftOnCooldown(Player p)
 	{
 		return timeLeftOnCooldown(p.getName());
 	}
 
 	public long timeLeftOnCooldown(String playername)
 	{
 		if(!playerCooldowns.containsKey(playername))
 			return 0;
 
 		if(dungeonCooldown == -1)
 			return -1;
 
 		Long cooldown = playerCooldowns.get(playername);
 		Long current = System.currentTimeMillis();
 
 		Long remaining = dungeonCooldown - (current - cooldown);
 		if(remaining > 0)
 			return remaining;
 		else
 			return 0;
 	}
 
 	public void saveCooldowns()
 		throws Exception
 	{
 		createDirectory();
 
 		File cooldownFile = new File(DungeonBuilder.dungeonRoot + "/" + owner + "/" + name + ".cooldowns");
 		PrintWriter pw = null;
 		try
 		{
 			pw = new PrintWriter(new FileWriter(cooldownFile));
 			for(String name : playerCooldowns.keySet())
 			{
 				Long amount = playerCooldowns.get(name);
 				pw.print(name + ":" + amount + "\n");
 			}
 		}
 		finally
 		{
 			if(pw != null)
 				pw.close();
 		}
 	}
 
 	public void loadCooldowns()
 		throws Exception
 	{
 		File cooldownFile = new File(DungeonBuilder.dungeonRoot + "/" + owner + "/" + name + ".cooldowns");
 		if(!cooldownFile.exists())
 			return;
 
 		BufferedReader br = null;
 		try
 		{
 			br = new BufferedReader(new FileReader(cooldownFile));
 			String line = null;
 			while((line = br.readLine()) != null)
 			{
 				String [] comps = line.split(":");
 				if(comps.length < 2)
 					continue;
 
 				String name = comps[0];
 				Long amount = Long.parseLong(comps[1]);
 				playerCooldowns.put(name, amount);
 			}
 		}
 		finally
 		{
 			if(br != null)
 				br.close();
 		}
 	}
 
 	public void saveDungeon()
 		throws Exception
 	{
 		if(!DungeonBuilder.dontSaveBlocks)
 		{
 			blocks.clear();
 
 			Location newLoc = center.clone();
 
 			int halfdepth = depth / 2;
 			int halfwidth = width / 2;
 
 			double xbase = center.getX();
 			double zbase = center.getZ();
 			double ybase = center.getY() - 1;
 			for(double y = 0.0; y <= height; y += 1.0)
 			{
 				newLoc.setY(ybase + y);
 				for(double x = -halfdepth; x <= halfdepth; x += 1.0)
 				{
 					newLoc.setX(xbase + x);
 					for(double z = -halfwidth; z <= halfwidth; z += 1.0)
 					{
 						newLoc.setZ(zbase + z);	
 						Block b = newLoc.getBlock();
 
 						if(ignoreTypes.contains(b.getType()))
 							continue;
 
 						blocks.add(new BlockInfo(newLoc.getBlock()));
 					}
 				}
 			}
 
 			for(Entity e : world.getEntities())
 			{
 				Location eloc = e.getLocation();
 				if(!containsLocation(eloc))
 					continue;
 
 				Block eblock = eloc.getBlock();
 				BlockInfo bi = new BlockInfo(eblock);
 				bi.setMetaString("ENTITY("+ e.getClass().toString() + ")");
 				bi.setEntity(e);
 				blocks.add(bi);
 			}
 
 			Collections.sort(blocks);
 		}
 
 		saveToFile();
 
 		origBlocks = null;
 	}
 
 	public boolean deleteFromServer(boolean clear)
 	{
 		if(clear)
 		{
 			Collections.reverse(blocks);
 			for(BlockInfo bi : blocks)
 			{
 				Block b = bi.getBlock();
 				b.setType(Material.AIR);
 			}
 		}
 
 		File dungeonDir = new File(DungeonBuilder.dungeonRoot + "/" + owner + "/" + name);
 		if(!dungeonDir.exists())
 			return true;
 
 		return dungeonDir.delete();
 	}
 
 	public void loadDungeon()
 	{
 		loadDungeon(null);
 	}
 
 	public void loadDungeon(final Player player)
 	{
 		if(plugin == null || plugin.scheduler == null)
 		{
 			prepareDungeon();
 			loadDungeonBlocks();
 			return;
 		}
 
 		plugin.scheduler.scheduleSyncDelayedTask(plugin, new Runnable() { @Override public void run() {
 			prepareDungeon();
 		}});
 
 		plugin.scheduler.scheduleSyncDelayedTask(plugin, new Runnable() { @Override public void run() {
 			loadDungeonBlocks(player);	
 		}}, 20);
 	}
 
 	public void prepareDungeon()
 	{
 		clearSpecial(Material.LAVA, Material.STATIONARY_LAVA, Material.WATER, Material.STATIONARY_WATER, Material.REDSTONE_WIRE, Material.REDSTONE_TORCH_OFF, 
 			     Material.REDSTONE_TORCH_ON, Material.REDSTONE, Material.DETECTOR_RAIL, Material.LEVER, Material.STONE_BUTTON, Material.WOOD_PLATE,
 			     Material.STONE_PLATE, Material.POWERED_RAIL, Material.DIODE_BLOCK_OFF, Material.DIODE_BLOCK_ON, Material.SUGAR_CANE_BLOCK);
 	}
 
 	private void loadDungeonBlocks()
 	{
 		loadDungeonBlocks(null);
 	}
 
 	private void loadDungeonBlocks(final Player player)
 	{
 		synchronized(loadingLock)
 		{
 			loading = true;
 		}
 
 		final int chunksize = plugin.loadChunkSize;
 		for(int i = 0; i < blocks.size(); i+=chunksize)
 		{
 			final int start = i;
 			plugin.scheduler.scheduleSyncDelayedTask(plugin, new Runnable() { @Override public void run() {
 				for(int n = 0; n < chunksize; n++)
 				{
 					int index = start + n;
 					if(index >= blocks.size())
 					{
 						synchronized(loadingLock)
 						{
 							loading = false;
 						}
 						if(player != null)
 							player.sendMessage("Dungeon loaded");
 						return;
 					}
 					Chunk c = blocks.get(index).getBlock().getChunk();
 					if(!world.isChunkLoaded(c))
 						world.loadChunk(c);
 					blocks.get(index).setBlock();
 				}
 			}}, (i/chunksize)*10);
 		}
 
 		refreshExitBlock();
 	}
 
 	public boolean isLoading()
 	{
 		synchronized(loadingLock)
 		{
 			return loading;
 		}
 	}
 
 	public void undoDungeon()
 	{
 		if(origBlocks == null)
 			return;
 
 		for(BlockInfo bi : origBlocks)
 		{
 			bi.setBlock();
 		}
 
 		origBlocks = null;
 	}
 
 	public void clearDungeon()
 	{
 		ArrayList<BlockInfo> temp = new ArrayList<BlockInfo>(blocks);
 		Collections.reverse(temp);
 
 		for(BlockInfo bi : temp)
 		{
 			if(bi.getType() != Material.BEDROCK)
 			{
 				Block b = bi.getBlock();
 				BlockState bs = b.getState();
 				if(bs instanceof InventoryHolder)
 				{
 					InventoryHolder cb = (InventoryHolder)bs;
 					cb.getInventory().clear();
 				}
 
 				bi.getBlock().setType(Material.AIR);
 			}
 		}
 
 		refreshExitBlock();
 	}
 
 	public void clearEntities()
 	{
 		for(BlockInfo bi : blocks)
 		{
 			if(bi.getEntity() == null)
 				continue;
 
 			bi.getEntity().remove();
 		}
 	}
 
 	public void clearTorches()
 	{
 		clearSpecial(Material.TORCH);
 	}
 
 	public void clearLiquids()
 	{
 		clearSpecial(Material.LAVA, Material.STATIONARY_LAVA, Material.WATER, Material.STATIONARY_WATER);
 	}
 
 	public void clearSpecial(Material ... types)
 	{
 		ArrayList<Material> materials = new ArrayList<Material>();
 		for(Material m : types)
 			materials.add(m);
 
 		clearSpecial(materials);
 	}
 
 	public void clearSpecial(ArrayList<Material> materials)
 	{
 		Location loc = center.clone();
 
 		int halfdepth = depth / 2;
 		int halfwidth = width / 2;
 
 		double xbase = center.getX();
 		double zbase = center.getZ();
 		double ybase = center.getY() - 1;
 		for(double y = 1.0; y <= height-1; y += 1.0)
 		{
 			loc.setY(ybase + y);
 			for(double x = -halfdepth+1; x <= halfdepth-1; x += 1.0)
 			{
 				loc.setX(xbase + x);
 				for(double z = -halfwidth+1; z <= halfwidth-1; z += 1.0)
 				{
 					loc.setZ(zbase + z);	
 					Block b = loc.getBlock();
 					Material m = b.getType();
 					if(materials.contains(m))
 						b.setType(Material.AIR);
 				}
 			}
 		}
 	}
 
 	public String getFileName()
 	{
 		return DungeonBuilder.dungeonRoot + "/" + owner + "/" + name;
 	}
 
 	private void createDirectory()
 		throws Exception
 	{
 		File playerDir = new File(DungeonBuilder.dungeonRoot + "/" + owner);
 		if(!playerDir.exists())
 		{
 			if(!playerDir.mkdirs())
 				throw new Exception("Failed to make directory: " + playerDir.getPath());
 		}
 	}
 
 	public void saveToFile()
 		throws Exception
 	{
 		createDirectory();
 
 		File dungeonFile = new File(DungeonBuilder.dungeonRoot + "/" + owner + "/" + name);
 		if(dungeonFile.exists())
 		{
 			File backup = new File(DungeonBuilder.dungeonRoot + "/" + owner + "/" + name + ".previous");
 			if(backup.exists())
 			{
 				if(!backup.delete())
 				{
 					throw new Exception("Failed to delete previous backup file for dungeon");
 				}
 			}
 
 			if(!dungeonFile.renameTo(backup))
 				throw new Exception("Failed to backup existing dungeon file");
 		}
 
 		saveToFile(dungeonFile, false);
 	}
 
 	public synchronized void saveToFile(File dungeonFile, boolean relative)
 		throws Exception
 	{
 		PrintWriter pw = null;
 		DataOutputStream dos = null;
 		try
 		{
 			pw = new PrintWriter(new FileWriter(dungeonFile));
 			if(!relative)
 			{
 				dos = new DataOutputStream(new FileOutputStream(dungeonFile.getPath() + ".blocks"));
 				dos.writeInt(BLOCK_FILE_VERSION);
 			}
 
 			pw.print("World:" + world.getName() + "," + world.getEnvironment().getId() + "\n");
 			pw.print("PartySize:" + partySizeMin + "-" + partySizeMax + "\n");
 			pw.print("Reward:" + reward + "\n");
 			pw.print("ExpReward:" + exp + "\n");
 			pw.print("Cooldown:" + dungeonCooldown + "\n");
 			pw.print("Autoload:" + autoload + "\n");
 			pw.print("Lives:" + lives + "\n");
 
 			if(coOwners.size() > 0)
 			{
 				StringBuffer coown = new StringBuffer();
 				for(String co : coOwners)
 				{
 					if(coown.length() > 0)
 						coown.append(",");
 					coown.append(co);
 				}
 				pw.print("Co-Owners:" + coown.toString() + "\n");
 			}
 			
 			if(invPermits.size() > 0)
 			{
 				StringBuffer inv = new StringBuffer();
 				for(String item : invPermits)
 				{
 					if(inv.length() > 0)
 						inv.append(",");
 					inv.append(item);
 				}
 				pw.print("Inventory:" + inv.toString() + "\n");
 			}
 
 			if(ignoreTypes.size() > 0)
 			{
 				StringBuffer ignore = new StringBuffer();
 				for(Material m : ignoreTypes)
 				{
 					if(ignore.length() > 0)
 						ignore.append(",");
 					ignore.append(m.toString());
 				}
 				pw.print("Ignore:" + ignore.toString() + "\n");
 			}
 
 			if(published)
 			{
 				World tworld = teleporter.getWorld();
 				pw.print("T:" + teleporter.getX() + "," + teleporter.getY() + "," + teleporter.getZ() + "," + tworld.getName() + "," + tworld.getEnvironment().getId() + "\n");
 
 				World eworld = exitdest.getWorld();
 				pw.print("E:" + exitdest.getX() + "," + exitdest.getY() + "," + exitdest.getZ() + "," + eworld.getName() + "," + eworld.getEnvironment().getId() + "\n");
 			}
 			else
 			{
 				pw.print("\n");
 				pw.print("\n");
 			}
 			pw.print(center.getX() + "," + center.getY() + "," + center.getZ() + "," + width + "," + depth + "," + height + "\n");
 			pw.print(createLocationString(start, relative) + "\n");
 			pw.print(createLocationString(exit, relative) + "\n");
 
 			if(!DungeonBuilder.dontSaveBlocks)
 			{
 				for(BlockInfo bi : blocks)
 				{
 					Block b = bi.getBlock();
 					Material m = b.getType();
 					Location loc = b.getLocation();
 
 					String metaStr = null;
 					BlockState bs = b.getState();
 					if(bs instanceof InventoryHolder)
 					{
 						metaStr = createInventoryString((InventoryHolder)bs);
 					}
 
 					if(bs instanceof org.bukkit.block.Sign)
 					{
 						metaStr = createSignString((org.bukkit.block.Sign)bs);
 					}
 
 					//For saving entities
 					if(metaStr == null && bi.getMetaString() != null)
 						metaStr = bi.getMetaString();
 
 					if(metaStr == null)
 					{
 						if(!relative)
 							bi.writeData(dos);
 						else
 							pw.print(createLocationString(loc, relative) + "," + m.getId() + "," +  b.getData() + "\n");
 					}
 					else
 					{
 						pw.print(createLocationString(loc, relative) + "," + m.getId() + "," +  b.getData() + "," + metaStr + "\n");
 						bi.setMetaString(metaStr);
 					}
 				}
 			}
 
 			for(MonsterInfo monster : savedMonsters)
 				pw.print(monster.getLine(relative) + "\n");
 
 			for(LocationWrapper lw : monsterTriggers.values())
 				pw.print(lw.toString(relative) + "\n");
 
 			for(LocationWrapper lw : savePoints.values())
 				pw.print(lw.toString(relative) + "\n");
 
 			for(LocationWrapper lw : scriptTriggers.values())
 				pw.print(lw.toString(relative) + "\n");
 
 			saveCooldowns();
 		}
 		finally
 		{
 			if(pw != null)
 				pw.close();
 			if(dos != null)
 				dos.close();
 		}
 
 		File permFile = new File(DungeonBuilder.dungeonRoot + "/" + owner + "/" + name + ".perms");
 
 		pw = null;
 		try
 		{
 			pw = new PrintWriter(new FileWriter(permFile));
 			for(String node : defaultPermissions)
 				pw.print(node + "\n");
 		}
 		finally
 		{
 			if(pw != null)
 				pw.close();
 		}
 	}
 
 	public void loadFromFile(Server s)
 		throws Exception
 	{
 		File dungeonFile = new File(DungeonBuilder.dungeonRoot + "/" + owner + "/" + name);
 		if(!dungeonFile.exists())
 			throw new Exception("Unable to locate dungeon file: " + dungeonFile.getPath());
 
 		loadFromFile(dungeonFile, s, false);
 	}
 
 	public synchronized void loadFromFile(File dungeonFile, Server s, boolean relative)
 		throws Exception
 	{
 		BufferedReader br = new BufferedReader(new FileReader(dungeonFile));
 		String line = br.readLine();
 		if(line == null)
 			throw new Exception("Dungeon file is empty or incorrectly formatted");
 
 		if(line.startsWith("World:"))
 		{
 			String [] comps = line.substring(6).split(",");
 			String wname = comps[0];
 			Integer envid = Integer.parseInt(comps[1]);
 			if(!relative && s != null)
 			{
 				WorldCreator wc = new WorldCreator(wname);
 				wc.environment(World.Environment.getEnvironment(envid));
 				world = s.createWorld(wc);
 			}
 			line = br.readLine();
 		}
 		else if(!relative)
 			world = s.getWorld("world");
 
 		if(line.startsWith("PartySize:"))
 		{
 			String temp = line.substring(10);
 
 			int min = 1, max = 1;
 			try
 			{
 				if(temp.contains("-"))
 				{
 					String [] sizeComps = temp.split("-");
 					min = Integer.parseInt(sizeComps[0]);
 					max = Integer.parseInt(sizeComps[1]);
 				}
 				else
 				{
 					min = Integer.parseInt(temp);
 					max = min;
 				}
 				this.partySizeMin = min;
 				this.partySizeMax = max;
 			}
 			catch(Exception e)
 			{
 				e.printStackTrace();
 				this.partySizeMin = 1;
 				this.partySizeMax = 1;
 			}
 
 			line = br.readLine();
 		}
 		else
 		{
 			partySizeMin = 1;
 			partySizeMax = 1;
 		}
 
 		if(partySizeMin == 1)
 			currentStatus = PartyStatus.READY;
 
 		if(line.startsWith("Reward:"))
 		{
 			String temp = line.substring(7);
 			this.reward = Double.parseDouble(temp);
 
 			line = br.readLine();
 		}
 		else
 			reward = 0.0;
 
 		if(line.startsWith("ExpReward:"))
 		{
 			String temp = line.substring(10);
 			this.exp = temp;
 
 			line = br.readLine();
 		}
 		else
 			exp = "-1";
 
 		if(line.startsWith("Cooldown:"))
 		{
 			String temp = line.substring(9);
 			this.dungeonCooldown = Long.parseLong(temp);
 
 			line = br.readLine();
 		}
 		else
 			dungeonCooldown = 0;
 
 		if(line.startsWith("Autoload:"))
 		{
 			String temp = line.substring(9);
 			this.autoload = Boolean.parseBoolean(temp);
 
 			line = br.readLine();
 		}
 
 		if(line.startsWith("Lives:"))
 		{
 			String temp = line.substring(6);
 			try
 			{
 				this.lives = Integer.parseInt(temp);
 			}
 			catch(Exception e)
 			{
 				e.printStackTrace();
 				this.lives = -1;
 			}
 
 			line = br.readLine();
 		}
 
 		if(line.startsWith("Co-Owners:"))
 		{
 			String temp = line.substring(10);
 			String [] comps = temp.split(",");
 			coOwners.clear();
 			for(String co : comps)
 				coOwners.add(co);
 
 			line = br.readLine();
 		}
 
 		if(line.startsWith("Inventory:"))
 		{
 			String temp = line.substring(10);
 			String [] comps = temp.split(",");
 			invPermits.clear();
 			for(String item : comps)
 				invPermits.add(item);
 
 			line = br.readLine();
 		}
 
 		if(line.startsWith("Ignore:"))
 		{
 			String temp = line.substring(7);
 			String [] comps = temp.split(",");
 			ignoreTypes.clear();
 			for(String mat : comps)
 				ignoreTypes.add(Material.matchMaterial(mat));
 
 			line = br.readLine();
 		}
 
 		if(line.startsWith("T:"))
 		{
 			String temp = line.substring(2);
 			String [] comps = temp.split(",");
 			if(comps.length < 3)
 				throw new Exception("The dungeon file is missing parameters for the teleporter location");
 
 			if(!relative)
 			{
 				if(comps.length == 3)
 					teleporter = createLocation(world, comps[0], comps[1], comps[2]);
 				else if(comps.length == 5)
 					teleporter = createLocation(s, comps[0], comps[1], comps[2], comps[3], comps[4]);
 				published = true;
 			}
 
 			line = br.readLine();
 		}
 		else if(line.trim().length() == 0)
 			line = br.readLine();
 
 		if(line.startsWith("E:"))
 		{
 			String temp = line.substring(2);
 			String [] comps = temp.split(",");
 			if(comps.length < 3)
 				throw new Exception("The dungeon file is missing parameters for the exit destination location");
 			if(!relative)
 			{
 				if(comps.length == 3)
 					exitdest = createLocation(world, comps[0], comps[1], comps[2]);
 				else if(comps.length == 5)
 					exitdest = createLocation(s, comps[0], comps[1], comps[2], comps[3], comps[4]);
 			}
 
 			line = br.readLine();
 		}
 		else if(line.trim().length() == 0)
 			line = br.readLine();
 
 		if(exitdest == null && teleporter != null)
 		{
 			exitdest = teleporter.clone();
 			exitdest.setX(exitdest.getX() - 2.0);
 		}
 
 		String [] dungeonParams = line.trim().split(",");
 		if(dungeonParams.length < 6)
 			throw new Exception("The following line in the dungeon file is missing parameters: " + line.trim());
 		if(!relative)
 			center = createLocation(world, dungeonParams[0], dungeonParams[1], dungeonParams[2]);
 		width = Integer.parseInt(dungeonParams[3]);
 		depth = Integer.parseInt(dungeonParams[4]);
 		height = Integer.parseInt(dungeonParams[5]);
 	
 		line = br.readLine();
 		if(line == null)
 			throw new Exception("The dungeon file is missing the starting location for the dungeon");
 
 		dungeonParams = line.trim().split(",");
 		if(dungeonParams.length < 3)
 			throw new Exception("The following line of the dungeon file is missing parameters: " + line.trim());
 		start = createLocation(world, dungeonParams[0], dungeonParams[1], dungeonParams[2], relative);
 
 		line = br.readLine();
 		if(line == null)
 			throw new Exception("The dungeon file is missing the exit location for the dungeon");
 
 		dungeonParams = line.trim().split(",");
 		if(dungeonParams.length < 3)
 			throw new Exception("The following line of the dungeon file is missing parameters: " + line.trim());
 		exit = createLocation(world, dungeonParams[0], dungeonParams[1], dungeonParams[2], relative);
 		
 		blocks.clear();
 
 		if(relative)
 			origBlocks = new ArrayList<BlockInfo>();
 
 		ArrayList<BlockInfo> attachableBlocks = new ArrayList<BlockInfo>();
 		while((line = br.readLine()) != null)
 		{
 			if(line.startsWith("M:"))
 			{
 				try
 				{
 					MonsterInfo mi = new MonsterInfo(line.trim(), relative);
 					savedMonsters.add(mi);
 					addMonsterTrigger(start, mi.getAlias(), mi.getAlias());
 				}
 				catch(Exception e)
 				{
 					System.out.println("Invalid line: " + line);
 					System.out.println(e.getMessage());
 				}
 
 				continue;
 			}
 
 			if(line.startsWith("MT:"))
 			{
 				try
 				{
 					LocationWrapper lw = new LocationWrapper(this, line.trim(), relative);
 					addMonsterTrigger(lw);
 				}
 				catch(Exception e)
 				{
 					System.out.println(e.getMessage() + ": " + line);
 					e.printStackTrace();
 				}
 
 				continue;
 			}
 
 			if(line.startsWith("SP:"))
 			{
 				try
 				{
 					LocationWrapper lw = new LocationWrapper(this, line.trim(), relative);
 					addSavePoint(lw);
 				}
 				catch(Exception e)
 				{
 					System.out.println(e.getMessage() + ": " + line);
 					e.printStackTrace();
 				}
 
 				continue;
 			}
 
 			if(line.startsWith("ST:"))
 			{
 				try
 				{
 					LocationWrapper lw = new LocationWrapper(this, line.trim(), relative);
 					addScriptTrigger(lw);
 				}
 				catch(Exception e)
 				{
 					System.out.println(e.getMessage() + ": " + line);
 					e.printStackTrace();
 				}
 
 				continue;
 			}
 
 			String [] comps = line.trim().split(",");
 			if(comps.length < 4)
 				throw new Exception("The dungeon file is missing required fields");
 
 			Location loc = createLocation(world, comps[0], comps[1], comps[2], relative);
 
 			int type = Integer.parseInt(comps[3]);
 			Block b = loc.getBlock();
 			if(relative)
 				origBlocks.add(new BlockInfo(b));
 			Material m = Material.getMaterial(type);
 			Byte data = null;
 			if(comps.length == 5)
 			{
 				data = Byte.parseByte(comps[4]);
 			}
 
 			BlockInfo bi = new BlockInfo(b, m, data);
 			if(comps.length == 6)
 			{
 				bi.setMetaString(comps[5]);	
 			}
 
 			blocks.add(bi);
 		}
 
 		File blockFile = new File(dungeonFile.getPath() + ".blocks");
 		if(blockFile.exists())
 		{
 			DataInputStream dis = null;
 			try
 			{
 				dis = new DataInputStream(new FileInputStream(blockFile));
 				int version = dis.readInt();
 				BlockInfo bi = BlockInfo.readData(version, dis, world);
 				while(bi != null)
 				{
 					blocks.add(bi);
 					bi = BlockInfo.readData(version, dis, world);
 				}
 			}
 			catch(Exception e)
 			{
 				e.printStackTrace();
 			}
 			finally
 			{
 				if(dis != null)
 					dis.close();
 			}
 		}
 
 		Collections.sort(blocks);
 
 		br.close();
 
 		loadCooldowns();
 
 		File permissionFile = new File(DungeonBuilder.dungeonRoot + "/" + owner + "/" + name + ".perms");
 		if(permissionFile.exists())
 		{
 			defaultPermissions.clear();
 
 			br = new BufferedReader(new FileReader(permissionFile));
 			line = null;
 			while((line = br.readLine()) != null)
 			{
 				defaultPermissions.add(line.trim());
 			}
 			br.close();
 		}
 
 		if(autoload || relative)
 			loadDungeon();
 	}
 
 	@Override public int compareTo(Dungeon d)
 	{
 		return getName().compareTo(d.getName());
 	}
 
 	private Location createLocation(Server server, String x, String y, String z, String worldname, String worldid)
 		throws Exception
 	{
 		Integer id = Integer.parseInt(worldid);
 		WorldCreator wc = new WorldCreator(worldname);
 		wc.environment(World.Environment.getEnvironment(id));
 		World w = server.createWorld(wc);
 
 		return createLocation(w, x, y, z);
 	}
 
 	public Location createLocation(String x, String y, String z)
 		throws Exception
 	{
 		return createLocation(world, x, y, z);
 	}
 
 	public Location createLocation(String x, String y, String z, boolean relative)
 		throws Exception
 	{
 		return createLocation(world, x, y, z, relative);
 	}
 
 	private Location createLocation(World w, String x, String y, String z)
 		throws Exception
 	{
 		return createLocation(w, x, y, z, false);
 	}
 
 	private Location createLocation(World w, String x, String y, String z, boolean relative)
 		throws Exception
 	{
 		double xd = Double.parseDouble(x);
 		double yd = Double.parseDouble(y);
 		double zd = Double.parseDouble(z);
 
 		if(relative)
 		{
 			double cx = center.getX();
 			double cy = center.getY();
 			double cz = center.getZ();
 
 			xd = cx + xd;
 			yd = cy + yd;
 			zd = cz + zd;
 		}
 
 
 		return new Location(w, xd, yd, zd);
 	}
 
 	public Location createLocationYP(World w, String x, String y, String z, String yaw, String pitch, boolean relative)
 		throws Exception
 	{
 		Location retVal = createLocation(w, x, y, z, relative);
 
 		float fyaw = Float.parseFloat(yaw);
 		float fpitch = Float.parseFloat(pitch);
 
 		retVal.setYaw(fyaw);
 		retVal.setPitch(fpitch);
 
 		return retVal;
 	}
 
 	public String createLocationString(Location loc, boolean relative)
 	{
 		StringBuffer retVal = new StringBuffer();
 		double x = Math.floor(loc.getX());
 		double y = Math.floor(loc.getY());
 		double z = Math.floor(loc.getZ());
 
 		if(relative)
 		{
 			x = x - Math.floor(center.getX());
 			y = y - Math.floor(center.getY());
 			z = z - Math.floor(center.getZ());
 		}
 
 		retVal.append(x);
 		retVal.append("," + y);
 		retVal.append("," + z);
 
 		return retVal.toString();
 	}
 
 	public String createLocationStringYP(Location loc, boolean relative)
 	{
 		String retVal = createLocationString(loc, relative);
 		retVal = retVal + "," + loc.getYaw();
 		retVal = retVal + "," + loc.getPitch();
 
 		return retVal;
 	}
 
 	private String createSignString(org.bukkit.block.Sign s)
 	{
 		StringBuffer retVal = new StringBuffer("text(");
 		for(String line : s.getLines())
 		{
 			retVal.append(line + ";");
 		}
 		retVal.append(")");
 
 		return retVal.toString();
 	}
 
 	public static void parseSignString(org.bukkit.block.Sign s, String metaStr)
 	{
 		metaStr = metaStr.substring(5);
 		metaStr = metaStr.substring(0, metaStr.length() - 2);
 
 		String [] lines = metaStr.split(";");
 		for(int i = 0; i < lines.length; i++)
 		{
 			s.setLine(i, lines[i]);
 		}
		s.update();
 	}
 
 	private String createInventoryString(InventoryHolder cb)
 	{
 		Inventory i = cb.getInventory();
 		StringBuffer retVal = new StringBuffer();
 
 		retVal.append("inv(");
 		for(ItemStack is : i.getContents())
 		{
 			if(is == null)
 				continue;
 
 			Map<Enchantment, Integer> enchantments = is.getEnchantments();
 			String enchs = "";
 			for(Enchantment en : enchantments.keySet())
 			{
 				if(enchs.length() > 0)
 					enchs = enchs + "#";
 				enchs = enchs + en.getId() + "-" + enchantments.get(en);
 			}
 
 			String item = is.getTypeId() + ":" + is.getAmount() + ":" + is.getDurability() + ":" + enchs;
 			retVal.append(item + ";");
 		}
 		retVal.append(")");
 
 		return retVal.toString();
 	}
 
 	public static void parseInventoryString(InventoryHolder cb, String invStr)
 	{
 		Inventory i = cb.getInventory();
 		i.clear();
 
 		invStr = invStr.substring(4);
 		invStr = invStr.substring(0, invStr.length() - 1);
 
 		String [] comps = invStr.split(";");
 		for(String itemStr : comps)
 		{
 			String [] itemData = itemStr.split(":");
 			if(itemData.length < 3)
 				continue;
 
 			try
 			{
 				Integer typeId = Integer.parseInt(itemData[0]);
 				Integer amount = Integer.parseInt(itemData[1]);
 				Short durability = Short.parseShort(itemData[2]);
 				
 				ItemStack is = new ItemStack(typeId);
 				is.setAmount(amount);
 				is.setDurability(durability);
 
 				if(itemData.length == 4)
 				{
 					String enchantmentStr = itemData[3];
 					String [] enchs = enchantmentStr.split("#");
 					for(String ench : enchs)
 					{
 						String [] enchComps = ench.split("-");
 						if(enchComps.length < 2)
 							continue;
 
 						int enchId = Integer.parseInt(enchComps[0]);
 						int enchLevel = Integer.parseInt(enchComps[1]);
 						is.addEnchantment(Enchantment.getById(enchId), enchLevel);
 					}
 				}
 
 				i.addItem(is);
 			}
 			catch(Exception e)
 			{
 				System.out.println("Oops...this line doesn't look right: " + invStr);
 				System.out.println("Guess I'll just skip it.");
 			}
 		}
 	}
 
 	public boolean addMonster(Location l, String key, String type)
 	{
 		return addMonster(l, key , type, 1);
 	}
 
 	public boolean addMonster(Location l, String key, String type, int count)
 	{
 		MonsterInfo mi = new MonsterInfo(l, key, type, count);
 		if(!savedMonsters.contains(mi))
 		{
 			savedMonsters.add(mi);
 			addMonsterTrigger(start, key, key);
 
 			return true;
 		}
 
 		return false;
 	}
 
 	public boolean removeMonster(String key)
 	{
 		for(int i = 0; i < savedMonsters.size(); i++)
 		{
 			if(savedMonsters.get(i).getAlias().equals(key))
 			{
 				savedMonsters.remove(i);
 				for(LocationWrapper lw : monsterTriggers.values())
 				{
 					if(lw.getMetaData().equals(key))
 					{
 						monsterTriggers.remove(lw.getAlias());
 						break;
 					}
 				}
 
 				return true;
 			}
 		}
 
 		return false;
 	}
 
 	public String listMonsters()
 	{
 		StringBuffer sb = new StringBuffer("");
 		for(MonsterInfo monster : savedMonsters)
 		{
 			if(sb.length() > 0)
 				sb.append(", ");
 			sb.append(monster.getAlias());	
 		}
 
 		return sb.toString();
 	}
 
 	public void spawnMonsters()
 		throws Exception
 	{
 		ArrayList<Chunk> loadedChunks = new ArrayList<Chunk>();
 
 		allowSpawn = true;
 		for(MonsterInfo monster : savedMonsters)
 		{
 			monster.killMonsters();
 
 			Location l = monster.getLoc();
 
 			Chunk c = world.getChunkAt(l);
 
 			if(!loadedChunks.contains(c))
 			{
 				world.loadChunk(c);
 				loadedChunks.add(c);
 			}
 
 			monster.spawnMonsters();
 		}
 		allowSpawn = false;
 	}
 
 	public void spawnMonster(String alias)
 	{
 		allowSpawn = true;
 		for(MonsterInfo monster : savedMonsters)
 		{
 			if(!monster.getAlias().equals(alias))
 				continue;
 
 			monster.killMonsters();
 
 			Location l = monster.getLoc();
 
 			String type = monster.getType();
 			int count = monster.getCount();
 
 			monster.spawnMonsters();
 		}
 		allowSpawn = false;
 	}
 
 	public boolean spawnsAllowed()
 	{
 		return allowSpawn;
 	}
 
 	public void killMonsters()
 	{
 		for(MonsterInfo monster : savedMonsters)
 			monster.killMonsters();
 	}
 
 	public void setDungeonReward(double reward)
 	{
 		this.reward = reward;
 	}
 
 	public double getDungeonReward()
 	{
 		return reward;
 	}
 
 	private void calculateSphere()
 	{
 		sphereCenter = center.clone();
 		sphereCenter.setY(sphereCenter.getY() - 1.0 + (height / 2.0));
 
 		Location corner = center.clone();
 		corner.setY(corner.getY() - 1.0);
 		corner.setX(corner.getX() - (depth / 2.0));
 		corner.setZ(corner.getZ() - (width / 2.0));
 
 		double halfheight = height / 2.0;
 		double halfdepth = depth / 2.0;
 		double halfwidth = width / 2.0;
 		sphereRadius = Math.sqrt(halfheight*halfheight + halfdepth*halfdepth + halfwidth*halfwidth);
 	}
 
 	public boolean inDungeon(Location loc)
 	{
 		double xdiff = Math.abs(loc.getX() - sphereCenter.getX());
 		double zdiff = Math.abs(loc.getZ() - sphereCenter.getZ());
 		double ydiff = Math.abs(loc.getY() - sphereCenter.getY());
 
 		double length = Math.sqrt(xdiff*xdiff + ydiff*ydiff + zdiff*zdiff);
 		if(length <= sphereRadius + 10.0)
 		{
 			return containsLocation(loc, 10);	
 		}
 
 		return false;
 	}
 
 	public void setPartySize(int min, int max)
 	{
 		partySizeMin = min;
 		partySizeMax = max;
 	}
 
 	public boolean validPartySize(int size)
 	{
 		return size >= partySizeMin && size <= partySizeMax;
 	}
 
 	public int getMinPartySize()
 	{
 		return partySizeMin;
 	}
 	
 	public int getMaxPartySize()
 	{
 		return partySizeMax;
 	}
 
 	public PartyStatus getStatus()
 	{
 		if(partyList.size() == 0)
 			return PartyStatus.EMPTY;
 
 		if(activeParty != null)
 			return PartyStatus.FULL;
 
 		for(DungeonParty dp : partyList)
 		{
 			if(validPartySize(dp.getSize()))
 				return PartyStatus.READY;
 		}
 
 		return PartyStatus.MISSING_PLAYERS;
 	}
 
 	public DungeonParty addPlayer(Player p)
 	{
 		if(partyList.size() == 0)
 		{
 			DungeonParty party = new DungeonParty(p.getName(), plugin.server);
 			partyList.add(party);
 
 			return party;
 		}
 		else
 		{
 			for(DungeonParty dp : partyList)
 			{
 				if(!dp.containsMember(p))
 					continue;
 
 				return dp;
 			}
 
 			for(int i = 0; i < partyList.size(); i++)
 			{
 				DungeonParty party = partyList.get(i);
 				if(party.getSize() < partySizeMax)
 				{
 					party.addMember(p.getName());
 
 					return party;
 				}
 			}
 
 			DungeonParty party = new DungeonParty(p.getName(), plugin.server);
 			partyList.add(party);
 
 			return party;
 		}
 	}
 
 	public PartyStatus addParty(DungeonParty party)
 	{
 		if(party.getSize() > partySizeMax)
 			return PartyStatus.TOOLARGE;
 
 		checkActiveParty();
 
 		if(!partyList.contains(party))
 			partyList.add(party);
 		else
 		{
 			//Trying to make sure the party reference in the party list is the most recent
 			partyList.remove(party);
 			partyList.add(party);
 		}
 
 		if(activeParty != null && !activeParty.equals(party))
 		{
 			return PartyStatus.INQUEUE;
 		}
 
 		if(validPartySize(party.getSize()))
 			return PartyStatus.READY;
 		else
 			return PartyStatus.MISSING_PLAYERS;
 	}
 
 	public boolean removePlayer(Player p)
 	{
 		boolean retVal = false;
 
 		ArrayList<DungeonParty> toRemove = new ArrayList<DungeonParty>();
 		for(DungeonParty dp : partyList)
 		{
 			if(dp.containsMember(p))
 			{
 				dp.removeMember(p.getName());
 				if(dp.getSize() == 0)
 					toRemove.add(dp);
 				retVal = true;
 			}
 		}
 
 		for(DungeonParty dp : toRemove)
 		{
 			partyList.remove(dp);
 			if(activeParty == dp)
 				activeParty = null;
 		}
 
 		return retVal;
 	}
 
 	public void checkActiveParty(String ... ignorePlayers)
 	{
 		if(activeParty == null)
 			return;
 
 		partyloop: for(String pname : activeParty.listMembers())
 		{
 			for(String name : ignorePlayers)
 			{
 				if(pname.equals(name))
 					continue partyloop;
 			}
 
 			if(plugin.isPlayerIdle(pname))
 				continue partyloop;
 
 			//The player is logged out but hasn't hit the idle timer yet, lets consider that as him being "in" the dungeon still
 			if(plugin.idleTimer.containsKey(pname))
 				return;
 
 			Player p = plugin.server.getPlayer(pname);
 			if(p == null)
 				continue partyloop;
 
 			Location loc = p.getLocation();
 			if(containsLocation(loc))
 			{
 				return;
 			}
 		}
 
 		removeParty(activeParty);
 	}
 
 	public boolean removeParty(DungeonParty dp)
 	{
 		if(partyList.contains(dp))
 		{
 			partyList.remove(dp);
 			if(dp == activeParty)
 			{
 				activeParty = null;
 				for(DungeonParty party : partyList)
 				{
 					for(String pname : party.listMembers())
 					{
 						Player p = plugin.server.getPlayer(pname);
 						if(p != null)
 							p.sendMessage("The dungeon is now available");
 					}
 				}
 			}
 
 			return true;
 		}
 		else
 			return false;
 	}
 
 	public void setActiveParty(DungeonParty party)
 	{
 		activeParty = party;
 	}
 
 	public DungeonParty getActiveParty()
 	{
 		return activeParty;
 	}
 
 	//public PartyStatus addPlayer(Player p)
 	//{
 	//	if(currentPlayers.size() < partySize && !currentPlayers.contains(p))
 	//	{
 	//		currentPlayers.add(p);
 	//		if(currentPlayers.size() == partySize)
 	//		{
 	//			currentStatus = PartyStatus.FULL;
 	//			return PartyStatus.READY;
 	//		}
 	//		else
 	//		{
 	//			currentStatus = PartyStatus.MISSING_PLAYERS;
 	//			return PartyStatus.MISSING_PLAYERS;
 	//		}
 	//	}
 	//	else if(currentPlayers.size() < partySize)
 	//	{
 	//		currentStatus = PartyStatus.MISSING_PLAYERS;
 	//		return currentStatus;
 	//	}
 	//	else if(currentPlayers.size() >= partySize)
 	//	{
 	//		if(currentPlayers.contains(p))
 	//		{
 	//			currentStatus = PartyStatus.FULL;
 	//			return PartyStatus.READY;
 	//		}
 	//		else
 	//		{
 	//			currentStatus = PartyStatus.FULL;
 	//			return currentStatus;
 	//		}
 	//	}
 
 	//	return currentStatus;
 	//}
 
 	//public boolean removePlayer(Player p)
 	//{
 	//	if(currentPlayers.contains(p))
 	//	{
 	//		currentPlayers.remove(p);
 
 	//		if(currentPlayers.size() == 0)
 	//		{
 	//			if(partySize > 1)
 	//				currentStatus = PartyStatus.EMPTY;
 	//			else
 	//				currentStatus = PartyStatus.READY;
 	//		}
 	//		else if(currentPlayers.size() < partySize)
 	//			currentStatus = PartyStatus.MISSING_PLAYERS;
 	//		else if(currentPlayers.size() == partySize)
 	//			currentStatus = PartyStatus.FULL;
 
 	//		return true;
 	//	}
 	//	else
 	//		return false;
 	//}
 
 	//public ArrayList<Player> listPlayers()
 	//{
 	//	return new ArrayList<Player>(currentPlayers);
 	//}
 
 	public void setExpReward(String reward)
 	{
 		this.exp = reward;
 	}
 
 	public String getExpReward()
 	{
 		return exp;
 	}
 
 	public void rewardPlayerExp(Player p)
 	{
 		try
 		{
 			if(exp.endsWith("L"))
 			{
 				String temp = exp.substring(0, exp.length() - 1);
 				int amount = Integer.parseInt(temp);
 				if(amount > 0)
 					p.setLevel(p.getLevel() + amount);
 			}
 			else
 			{
 				int amount = Integer.parseInt(exp);
 				if(amount > 0)
 					p.giveExp(amount);
 			}
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 		}
 	}
 
 	public void toggleAutoload(boolean enabled)
 	{
 		autoload = enabled;
 	}
 
 	public boolean getAutoload()
 	{
 		return autoload;
 	}
 
 	public void addMonsterTrigger(Player p, String alias, String monsterAlias)
 	{
 		addMonsterTrigger(p.getLocation(), alias, monsterAlias, 1);
 	}
 
 	public LocationWrapper addMonsterTrigger(Player p, String alias, String monsterAlias, int blockcount)
 	{
 		return addMonsterTrigger(p.getLocation(), alias, monsterAlias, blockcount);
 	}
 
 	private LocationWrapper addMonsterTrigger(Location l, String alias, String monsterAlias)
 	{
 		return addMonsterTrigger(l, alias, monsterAlias, 1);
 	}
 
 	private LocationWrapper addMonsterTrigger(Location l, String alias, String monsterAlias, int blockcount)
 	{
 		LocationWrapper lw = new LocationWrapper(alias, l, LocationWrapper.LocationType.MONSTER_TRIGGER, this);
 		lw.setMetaData(monsterAlias);
 		lw.setBlockCount(blockcount);
 
 		addMonsterTrigger(lw);
 
 		return lw;
 	}
 
 	private void addMonsterTrigger(LocationWrapper lw)
 	{
 		String monsterAlias = lw.getMetaData();
 		String alias = lw.getAlias();
 
 		ArrayList<String> toRemove = new ArrayList<String>();
 		for(LocationWrapper lw2 : monsterTriggers.values())
 		{
 			if(lw2.getMetaData().equals(monsterAlias))
 			{
 				toRemove.add(lw2.getAlias());
 			}
 		}
 
 		for(String ra : toRemove)
 			monsterTriggers.remove(ra);
 
 		monsterTriggers.put(alias, lw);
 	}
 
 	public boolean removeMonsterTrigger(String alias)
 	{
 		if(!monsterTriggers.containsKey(alias))
 			return false;
 
 		String monsterAlias = monsterTriggers.get(alias).getMetaData();
 
 		monsterTriggers.remove(alias);
 
 		boolean found = false;
 		for(LocationWrapper lw : monsterTriggers.values())
 		{	
 			if(lw.getMetaData().equals(monsterAlias))
 				found = true;
 		}
 		
 		if(!found)
 			addMonsterTrigger(start, monsterAlias, monsterAlias);
 
 		return true;
 	}
 
 	public ArrayList<LocationWrapper> listMonsterTriggers()
 	{
 		return new ArrayList<LocationWrapper>(monsterTriggers.values());
 	}
 
 	public LocationWrapper addSavePoint(Player p, String alias, int blockcount)
 	{
 		LocationWrapper lw = new LocationWrapper(alias, p.getLocation(), LocationWrapper.LocationType.SAVE_POINT, this);
 		lw.setBlockCount(blockcount);
 
 		addSavePoint(lw);
 
 		return lw;
 	}
 
 	public void addSavePoint(LocationWrapper lw)
 	{
 		savePoints.put(lw.getAlias(), lw);
 	}
 
 	public boolean removeSavePoint(String alias)
 	{
 		if(savePoints.containsKey(alias))
 		{
 			savePoints.remove(alias);
 			return true;
 		}
 
 		return false;
 	}
 
 	public ArrayList<LocationWrapper> listSavePoints()
 	{
 		return new ArrayList<LocationWrapper>(savePoints.values());
 	}
 
 	public LocationWrapper addScriptTrigger(Player p, String alias, String methodname, int blockcount)
 	{
 		LocationWrapper lw = new LocationWrapper(alias, p.getLocation(), LocationWrapper.LocationType.SCRIPT_TRIGGER, this);
 		lw.setBlockCount(blockcount);
 		lw.setMetaData(methodname);
 
 		addScriptTrigger(lw);
 
 		return lw;
 	}
 
 	public void addScriptTrigger(LocationWrapper lw)
 	{
 		scriptTriggers.put(lw.getAlias(), lw);
 	}
 
 	public boolean removeScriptTrigger(String alias)
 	{
 		if(scriptTriggers.containsKey(alias))
 		{
 			scriptTriggers.remove(alias);
 			return true;
 		}
 
 		return false;
 	}
 
 	public ArrayList<LocationWrapper> listScriptTriggers()
 	{
 		return new ArrayList<LocationWrapper>(scriptTriggers.values());
 	}
 
 	public void requireMonsterDeaths(ArrayList<String> names)
 	{
 		for(MonsterInfo mi : savedMonsters)
 		{
 			if(!names.contains(mi.getAlias()))
 				continue;
 
 			mi.setRequiresDeath(true);
 		}
 	}
 
 	public void clearDeathRequirements()
 	{
 		for(MonsterInfo mi : savedMonsters)
 		{
 			mi.setRequiresDeath(false);
 		}
 	}
 
 	public ArrayList<String> listRequiredDeaths()
 	{
 		ArrayList<String> retVal = new ArrayList<String>();
 		for(MonsterInfo mi : savedMonsters)
 		{
 			if(mi.requiresDeath())
 				retVal.add(mi.getAlias());
 		}
 
 		return retVal;
 	}
 
 	public boolean areRequiredMonstersDead()
 	{
 		for(MonsterInfo mi : savedMonsters)
 		{
 			if(!mi.requiresDeath())
 				continue;
 
 			if(!mi.areMonstersDead())
 				return false;
 		}
 
 		return true;
 	}
 
 	public boolean addMonsterDeathTrigger(String monster, String func)
 	{
 		for(MonsterInfo mi : savedMonsters)
 		{
 			if(!mi.getAlias().equals(monster))
 				continue;
 			
 			mi.setDeathTrigger(func);
 			return true;
 		}
 
 		return false;
 	}
 
 	public boolean removeMonsterDeathTrigger(String monster)
 	{
 		for(MonsterInfo mi : savedMonsters)
 		{
 			if(!mi.getAlias().equals(monster))
 				continue;
 			
 			mi.setDeathTrigger(null);
 			return true;
 		}
 
 		return false;
 	}
 
 	public ArrayList<String> listMonsterDeathTriggers()
 	{
 		ArrayList<String> retVal = new ArrayList<String>();
 		for(MonsterInfo mi : savedMonsters)
 		{
 			String trigger = mi.getDeathTrigger();
 			if(trigger.length() > 0)
 				retVal.add(mi.getAlias() + "(" + trigger + ")");
 		}
 
 		return retVal;
 	}
 
 	public boolean checkEntityDeath(Entity e)
 	{
 		for(MonsterInfo mi : savedMonsters)
 		{
 			if(!mi.containsMonster(e))
 				continue;
 
 			Player killer = null;
 			if(e instanceof LivingEntity)
 				killer = ((LivingEntity)e).getKiller();
 
 			String script = mi.getScript();
 			if(script.length() > 0)
 				ScriptManager.runMonsterScript(this, plugin.server, killer, plugin, e, script, "monster_death");
 
 			if(!mi.areMonstersDead())
 				return true;
 
 			String func = mi.getDeathTrigger();
 			if(func.length() > 0)
 			{
 				ScriptManager.runScript(this, plugin.server, killer, plugin, func);
 			}
 
 			return true;
 		}
 
 		return false;
 	}
 
 	public void entityDamage(Entity e)
 	{
 		for(MonsterInfo mi : savedMonsters)
 		{
 			if(!mi.containsMonster(e))
 				continue;
 
 			String script = mi.getScript();
 			if(script.length() > 0)
 			{
 				Player target = null;
 				if(e instanceof Creature)
 				{
 					Entity temp = ((Creature)e).getTarget();
 					if(temp instanceof Player)
 						target = (Player)temp;
 				}
 				ScriptManager.runMonsterScript(this, plugin.server, target, plugin, e, script, "monster_damage");
 			}
 
 			return;
 		}
 	}
 
 	public void entityAttack(Entity e, Event event)
 	{
 		for(MonsterInfo mi : savedMonsters)
 		{
 			if(!mi.containsMonster(e))
 				continue;
 
 			String script = mi.getScript();
 			if(script.length() > 0)
 			{
 				Player target = null;
 				if(e instanceof Creature)
 				{
 					Entity temp = ((Creature)e).getTarget();
 					if(temp instanceof Player)
 						target = (Player)temp;
 				}
 
 				HashMap<String, Object> inject = new HashMap<String, Object>();
 				inject.put("event", event);
 				ScriptManager.runMonsterScript(this, plugin.server, target, plugin, e, script, "monster_attack", inject);
 			}
 
 			return;
 		}
 	}
 
 	public void addIgnoreType(Material m)
 	{
 		ignoreTypes.add(m);
 	}
 
 	public void clearIgnoreTypes()
 	{
 		ignoreTypes.clear();
 	}
 
 	public HashSet<Material> listIgnoreTypes()
 	{
 		return new HashSet<Material>(ignoreTypes);
 	}
 
 	public void setMonsterScript(String alias, String script)
 	{
 		for(MonsterInfo mi : savedMonsters)
 		{
 			if(!mi.getAlias().equals(alias))
 				continue;
 
 			mi.setScript(script);
 		}
 	}
 
 	public void setLives(int lives)
 	{
 		this.lives = lives;
 	}
 
 	public int getLives()
 	{
 		return lives;
 	}
 
 	private class MonsterInfo
 	{
 		private Location loc;
 		private String alias, type, deathTrigger, script;
 		private ArrayList<Entity> liveMonsters = new ArrayList<Entity>();
 		private int count = 1;
 		private boolean requiresDeath = false;
 
 		public MonsterInfo(String line)
 			throws Exception
 		{
 			this(line, false);
 		}
 		
 		public MonsterInfo(String line, boolean relative)
 			throws Exception
 		{
 			line = line.substring(2);
 			String [] comps = line.split(",");
 			this.alias = comps[0];
 			this.loc = createLocation(world, comps[1], comps[2], comps[3], relative);
 			this.type = comps[4];
 
 			try
 			{
 				this.script = "";
 				this.deathTrigger = "";
 				if(comps.length >= 6)
 					this.count = Integer.parseInt(comps[5]);
 				if(comps.length >= 7)
 					this.requiresDeath = Boolean.parseBoolean(comps[6]);
 				if(comps.length >= 8)
 					this.deathTrigger = comps[7];
 				if(comps.length >= 9)
 					this.script = comps[8];
 			}
 			catch(Exception e)
 			{
 				System.out.println("Invalid line: " + line);
 			}
 		}
 
 		public MonsterInfo(Location l, String alias, String type, int count)
 		{
 			this.loc = l;
 			this.alias = alias;
 			this.type = type;
 			this.count = count;
 			this.deathTrigger = "";
 			this.script = "";
 		}
 
 		public String getLine(boolean relative)
 		{
 			String retVal = "M:" + alias + "," + createLocationString(loc, relative) + "," + type + "," + count + "," + requiresDeath + "," + deathTrigger + "," + script;
 
 			return retVal;
 		}
 
 		public String getAlias()
 		{
 			return alias;
 		}
 
 		public Location getLoc()
 		{
 			return loc;
 		}
 
 		public void setRequiresDeath(boolean require)
 		{
 			requiresDeath = require;
 		}
 
 		public void setDeathTrigger(String trigger)
 		{
 			deathTrigger = trigger;
 		}
 
 		public String getType()
 		{
 			return type;
 		}
 		
 		public int getCount()
 		{
 			return count;
 		}
 
 		@Override public boolean equals(Object o2)
 		{
 			if(!(o2 instanceof MonsterInfo))
 				return false;
 
 			return this.getAlias().equals(((MonsterInfo)o2).getAlias());
 		}
 
 		public void spawnMonsters()
 		{
 			if(type.equals("pig-zombie"))
 				type = "PIG_ZOMBIE";
 
 			EntityType ct = null;
 
 			try
 			{
 				ct = EntityType.valueOf(type.toUpperCase());
 			}
 			catch(Exception e)
 			{
 				System.out.println("Invalid creature type: " + type);
 				return;
 			}
 
 			if(ct == null)
 			{
 				System.out.println("Null creature type found");
 				return;
 			}
 
 			for(int i = 0; i < count; i++)
 			{
 				Entity e = world.spawnCreature(loc, ct);
 				liveMonsters.add(e);
 				if(script.length() > 0)
 					ScriptManager.runMonsterScript(Dungeon.this, plugin.server, null, plugin, e, script, "monster_spawn");
 			}
 		}
 
 		public void killMonsters()
 		{
 			for(Entity e : liveMonsters)
 				e.remove();
 
 			liveMonsters.clear();
 		}
 
 		public boolean requiresDeath()
 		{
 			return requiresDeath;
 		}
 
 		public boolean areMonstersDead()
 		{
 			for(Entity e : liveMonsters)
 			{
 				if(!e.isDead())
 					return false;
 			}
 
 			return true;
 		}
 
 		public boolean containsMonster(Entity e)
 		{
 			return liveMonsters.contains(e);
 		}
 
 		public String getDeathTrigger()
 		{
 			return deathTrigger;
 		}
 
 		public String getScript()
 		{
 			return script;
 		}
 
 		public void setScript(String script)
 		{
 			this.script = script;
 		}
 	}
 
 }
