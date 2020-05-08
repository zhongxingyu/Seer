 package com.github.MrTwiggy.MachineFactory.Machines;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.block.Chest;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.meta.ItemMeta;
 import org.bukkit.util.Vector;
 
 import com.github.MrTwiggy.MachineFactory.MachineFactoryPlugin;
 import com.github.MrTwiggy.MachineFactory.MachineObject;
 import com.github.MrTwiggy.MachineFactory.Interfaces.Machine;
 import com.github.MrTwiggy.MachineFactory.Properties.CloakerProperties;
 import com.github.MrTwiggy.MachineFactory.SoundCollections.CloakerSoundCollection;
 import com.github.MrTwiggy.MachineFactory.Utility.Dimensions;
 import com.github.MrTwiggy.MachineFactory.Utility.InteractionResponse;
 import com.github.MrTwiggy.MachineFactory.Utility.InteractionResponse.InteractionResult;
 
 /**
  * Cloaker.java
  * Purpose: Functionality for Cloaker objects
  *
  * @author MrTwiggy
  * @version 0.1 1/17/13
  */
 public class Cloaker extends MachineObject implements Machine
 {
	private List<Block> cloakedBlocks; // List of blocks in the cloaking field
	private Map<String, Boolean> cloakedClients; // List of clients within cloaking chunk range
 	
 	public static final MachineType MACHINE_TYPE = MachineType.CLOAKER; // The type this machine is
 
 	private double cloakedDuration; // The duration of the current cloaking
 	private Material cloakingMaterial; // The material used to cloak blocks with
 	
 	/*
 	 ----------CLOAKER CONSTRUCTORS--------
 	 */
 
 	/**
 	 * Constructor
 	 */
 	public Cloaker(Location machineLocation) 
 	{
 		super(machineLocation, new Dimensions(1,1,1), Cloaker.MACHINE_TYPE);
 		cloakedBlocks = new ArrayList<Block>();
 		cloakedClients = new HashMap<String, Boolean>();
 		cloakedDuration = 0;
 		initiateCloaking();
 		CloakerSoundCollection.getCreationSound().playSound(machineLocation);
 	}
 	
 	/**
 	 * Constructor
 	 */
 	public Cloaker(int tierLevel, Location machineLocation) 
 	{
 		super(machineLocation, new Dimensions(1,1,1), tierLevel, Cloaker.MACHINE_TYPE);
 		cloakedBlocks = new ArrayList<Block>();
 		cloakedClients = new HashMap<String, Boolean>();
 		cloakedDuration = 0;
 		initiateCloaking();
 		CloakerSoundCollection.getPlacementSound().playSound(machineLocation);
 	}
 	
 	/**
 	 * Constructor
 	 */
 	public Cloaker(int tierLevel, boolean active, double cloakedDuration, Inventory cloakerInventory,
 			Location machineLocation) 
 	{
 		super(machineLocation, new Dimensions(1,1,1), active, tierLevel, Cloaker.MACHINE_TYPE,
 				cloakerInventory);
 		cloakedBlocks = new ArrayList<Block>();
 		cloakedClients = new HashMap<String, Boolean>();
 		this.cloakedDuration = cloakedDuration;
 		initiateCloaking();
 	}
 	
 	/*
 	 ----------CLOAKER MAINTENENCE LOGIC--------
 	 */
 	
 	/**
 	 * Updates Cloaker logic
 	 */
 	public void update() 
 	{
 		if (upgraded)
 		{
 			initiateCloaking();
 			upgraded = false;
 		}
 		
 		if (active) //If cloaking
 		{
 			cloakBlocks();
 			
 			cloakedDuration += (MachineFactoryPlugin.CLOAKER_UPDATE_CYCLE / MachineFactoryPlugin.TICKS_PER_SECOND);
 			if (cloakedDuration >= getProperties().getFuelTimeDuration())
 			{
 				cloakedDuration = 0;
 				
 				if (fuelAvailable())
 				{
 					removeFuel();	
 				}
 				else
 				{
 					powerOff();
 				}
 			}
 		}
 		else // If not cloaking
 		{
 
 		}
 	}
 
 	/**
 	 * Destroys the Cloaker and drops appropriate items
 	 */
 	public void destroy(ItemStack item) 
 	{
 		setItemMeta(item);
 		machineLocation.getWorld().dropItemNaturally(machineLocation, item);
 		machineLocation.getBlock().setType(Material.AIR);
 		
 		ItemStack[] contents = machineInventory.getContents();
 		for (int i = 0; i < contents.length; i++)
 		{
 			if (contents[i] != null)
 				machineLocation.getWorld().dropItemNaturally(machineLocation, contents[i]);
 		}
 		
 		CloakerSoundCollection.getDestructionSound().playSound(machineLocation);
 	}
 	
 	/**
 	 * Sets the correct name and lore for the item
 	 */
 	public void setItemMeta(ItemStack item)
 	{
 		ItemMeta meta = item.getItemMeta();
 		
 		String name = Cloaker.CloakerName(tierLevel);
 		meta.setDisplayName(name);
 		item.setItemMeta(meta);
 	}
 
 	/*
 	 ----------CLOAKER FUEL LOGIC--------
 	 */
 	
 	/**
 	 * Returns whether there is enough material available for fueling one cloak operation
 	 */
 	public boolean fuelAvailable()
 	{
 		return (isMaterialAvailable(getProperties().getFuelAmount(), getProperties().getFuelMaterial()));
 	}
 	
 	/**
 	 * Attempts to remove materials for fueling one cloaking operation
 	 */
 	public boolean removeFuel()
 	{
 		CloakerSoundCollection.getRefuelSound().playSound(machineLocation);
 		return (removeMaterial(getProperties().getFuelAmount(), getProperties().getFuelMaterial()));
 	}
 	
 	/*
 	 ----------CLOAKING LOGIC--------
 	 */
 
 	/**
 	 * Cloaks all designated blocks
 	 */
 	public void cloakBlocks()
 	{		
 		updateCloakingMaterial();
 		for (Player player : machineLocation.getWorld().getPlayers())
 		{
 			int chunkXDiff = Math.abs(player.getLocation().getChunk().getX() 
 					- machineLocation.getChunk().getX());
 			int chunkZDiff = Math.abs(player.getLocation().getChunk().getZ()
 					- machineLocation.getChunk().getZ());
 			
 			if (chunkXDiff <= MachineFactoryPlugin.CLOAKER_CHUNK_RANGE 
 					&& chunkZDiff <= MachineFactoryPlugin.CLOAKER_CHUNK_RANGE)
 			{
 				double playerDist = player.getLocation().distance(machineLocation);
 				
 				if (!cloakedClients.containsKey(player.getName()))
 				{
 					cloakedClients.put(player.getName(), true);
 				}
 				
 				if (!cloakedClients.get(player.getName())) // If cloaked
 				{
 					if (playerDist <= getProperties().getVisibilityRange())
 					{
 						for (Block block : cloakedBlocks)
 						{
 							player.sendBlockChange(block.getLocation(), block.getType(), block.getData());
 						}
 						cloakedClients.put(player.getName(), true);
 					}
 				}
 				else // If not cloaked
 				{
 					if (playerDist > getProperties().getVisibilityRange())
 					{
 						for (Block block : cloakedBlocks)
 						{
 							player.sendBlockChange(block.getLocation(), cloakingMaterial, (byte)0);
 						}
 						cloakedClients.put(player.getName(), false);
 					}
 				}
 			}
 			else
 			{
 				cloakedClients.remove(player.getName());
 			}
 		}
 		
 		CloakerSoundCollection.getCloakingSound().playSound(machineLocation);
 	}
 	
 	/**
 	 * Uncloaks all cloaked blocks
 	 */
 	public void uncloak()
 	{
 		for (String string : cloakedClients.keySet())
 		{
 			Player player = Bukkit.getServer().getPlayer(string);
 			
 			if (player != null)
 			{
 				int chunkXDiff = Math.abs(player.getLocation().getChunk().getX() 
 						- machineLocation.getChunk().getX());
 				int chunkZDiff = Math.abs(player.getLocation().getChunk().getZ()
 						- machineLocation.getChunk().getZ());
 	
 				if (chunkXDiff <= MachineFactoryPlugin.CLOAKER_CHUNK_RANGE 
 						&& chunkZDiff <= MachineFactoryPlugin.CLOAKER_CHUNK_RANGE)
 				{
 					for (Block block : cloakedBlocks)
 					{
 						player.sendBlockChange(block.getLocation(), block.getType(), block.getData());
 					}
 					player.sendBlockChange(machineLocation, machineLocation.getBlock().getType(), 
 							machineLocation.getBlock().getData());
 				}
 			}
 		}
 		cloakedClients.clear();
 	}
 
 	/**
 	 * Initiates the designated cloaking
 	 */
 	public void initiateCloaking()
 	{
 		cloakedBlocks.clear();
 		
 		BlockFace facing = getDirection(machineLocation.getBlock());
 		Vector miningOffset = getCloakingOffset(facing);
 		
 		Location startingPos = machineLocation.getBlock().getLocation().add(
 				(int)miningOffset.getX(), (int)miningOffset.getY(), (int)miningOffset.getZ());
 		
 		  
 		for (int x = 0; x < Math.max(1, Math.abs(miningOffset.getX()*2)); x++) 
 		{ 
 			for (int y = 0; y < Math.max(1,  Math.abs(miningOffset.getY()*2)); y++)
 			{
 				for (int z = 0; z < Math.max(1,  Math.abs(miningOffset.getZ()*2)); z++)
 				{
 					for (int depth = 1; depth <= getProperties().getDepth(); depth++)
 					{
 						cloakedBlocks.add(startingPos.getBlock().getLocation().add(getBlockOffset(x,y,z,facing)).getBlock().getRelative(facing, depth));
 					}
 				}
 			}
 		}
 		cloakedBlocks.add(machineLocation.getBlock());
 	}
 	
 	/**
 	 * Block Offset for mininge
 	 */
 	public Vector getBlockOffset(double x, double y, double z, BlockFace facing)
 	{
 		
 		if (facing.equals(BlockFace.SOUTH))
 		{
 			x = -x;
 		}
 		else if (facing.equals(BlockFace.WEST))
 		{
 			z = -z;
 		}
 		
 		return new Vector(x, y, z);
 	}
 
 	/**
 	 * Offset for the starting mining position
 	 */
 	public Vector getCloakingOffset(BlockFace facing)
 	{
 		Vector miningOffset = new Vector(0, 0, 0);
 		
 		//Determines the Z or X offset
 		if (getProperties().getWidth() > 1)
 		{
 			if (facing.equals(BlockFace.NORTH))
 			{
 					miningOffset.setX(-(double)getProperties().getWidth() / 2);
 			}
 			else if (facing.equals(BlockFace.SOUTH))
 			{
 					miningOffset.setX((double)getProperties().getWidth() / 2);
 			}
 			else if (facing.equals(BlockFace.EAST))
 			{
 					miningOffset.setZ(-(double)getProperties().getWidth() / 2);
 			}
 			else if (facing.equals(BlockFace.WEST))
 			{
 					miningOffset.setZ((double)getProperties().getWidth() / 2);
 			}
 		}
 		
 		//Determines the Y offset
 		if (getProperties().getHeight() > 1)
 		{
 			miningOffset.setY(-(double)getProperties().getHeight() / 2);
 		}
 		
 		return miningOffset;
 	}
 	
 	/**
 	 * Updates the material used for cloaking
 	 */
 	public void updateCloakingMaterial()
 	{
 		ItemStack cloakingItem = machineInventory.getItem(26);
 		
		if (cloakingItem != null && cloakingItem.getType().isBlock() && !cloakingItem.getType().equals(Material.CHEST))
 		{
 			cloakingMaterial = cloakingItem.getType();
 		}
 		else
 		{
 			cloakingMaterial = Material.AIR;
 		}
 	}
 	
 	/*
 	 ----------CLOAKER POWER LOGIC--------
 	 */
 	
 	/**
 	 * Attempts to activate Cloaker
 	 */
 	public void powerOn()
 	{
 		if (removeFuel())
 		{
 			byte data = machineLocation.getBlock().getData();
 			machineLocation.getBlock().setType(MachineFactoryPlugin.CLOAKER_ACTIVATED);
 			machineLocation.getBlock().setData(data);
 			initiateCloaking();
 			active = true;
 			CloakerSoundCollection.getPowerOnSound().playSound(machineLocation);
 		}
 	}
 	
 	/**
 	 * Attempts to deactivate Cloaker
 	 */
 	public void powerOff()
 	{
 		byte data = machineLocation.getBlock().getData();
 		machineLocation.getBlock().setType(MachineFactoryPlugin.CLOAKER_DEACTIVATED);
 		machineLocation.getBlock().setData(data);
 		active = false;
 		uncloak();
 		CloakerSoundCollection.getPowerOffSound().playSound(machineLocation);
 	}
 	
 	/**
 	 * Attempts to toggle the power state
 	 */
 	public InteractionResponse togglePower() 
 	{
 		if (active)
 		{
 			powerOff();
 			CloakerSoundCollection.getPowerOffSound().playSound(machineLocation);
 			return new InteractionResponse(InteractionResult.FAILURE,
 					"Cloaker has been deactivated!");
 		}
 		else
 		{
 			if (fuelAvailable())
 			{
 				powerOn();
 				CloakerSoundCollection.getPowerOnSound().playSound(machineLocation);
 				return new InteractionResponse(InteractionResult.SUCCESS,
 						"Cloaker has been activated!");
 			}
 			else
 			{
 				CloakerSoundCollection.getErrorSound().playSound(machineLocation);
 				return new InteractionResponse(InteractionResult.FAILURE, 
 						"Missing fuel! " + getRequiredAvailableMaterials(getProperties().getFuelAmount(),
 								getProperties().getFuelMaterial()));
 			}
 		}
 	}
 	
 	/*
 	 ----------CLOAKER INVENTORY MANAGEMENT LOGIC--------
 	 */
 	
 	/**
 	 * Opens the Cloaker Inventory for given player
 	 */
 	public void openInventory(Player player)
 	{
 		player.openInventory(machineInventory);
 	}
 
 	/**
 	 * Returns whether there is enough materials in chest above Cloaker to create
 	 */
 	public boolean createCloaker()
 	{
 		Block blockAbove = machineLocation.getBlock().getRelative(BlockFace.UP);
 		if (blockAbove.getState() instanceof Chest)
 		{
 			Chest chestStorage = (Chest)blockAbove.getState();
 			Inventory chestInventory = chestStorage.getInventory();
 			Inventory tempStorage = machineInventory;
 			
 			machineInventory = chestInventory;
 			
 			if (upgradeMaterialAvailable(1))
 			{
 				removeUpgradeMaterial(1);
 				machineInventory = tempStorage;
 				return true;
 			}
 			else
 			{
 				return false;
 			}
 		}
 		else
 		{
 			return false;
 		}
 	}
 	
 	/*
 	 ----------CLOAKER PUBLIC ACCESSORS--------
 	 */
 	
 	/**
 	 * 'active' public accessor
 	 */
 	public boolean getActive()
 	{
 		return active;
 	}
 	
 	/**
 	 * 'cloakedDuration' public accessor
 	 */
 	public double getCloakedDuration()
 	{
 		return cloakedDuration;
 	}
 	
 	/**
 	 * 'machineLocation' public accessor
 	 */
 	public Location getLocation()
 	{
 		return machineLocation;
 	}
 	
 	/**
 	 * 'tierLevel' public accessor
 	 */
 	public int getTierLevel()
 	{
 		return tierLevel;
 	}
 	
 	/**
 	 * Returns the properties for this machine
 	 */
 	public CloakerProperties getProperties()
 	{
 		return (CloakerProperties)machineProperties;
 	}
 	
 	/*
 	 ----------CLOAKER STATIC METHODS--------
 	 */
 	
 	/**
 	 * Converts byte direction into BlockFace direction.
 	 */
 	public static BlockFace getDirection(Block block)
 	{
 		byte north, east, south, west;
 		
 		if (block.getType().equals(Material.PUMPKIN) || block.getType().equals(Material.JACK_O_LANTERN))
 		{
 			south = 0x0;
 			west = 0x01;
 			north = 0x02;
 			east = 0x03;
 		}
 		else
 		{
 			north = 0x02;
 			south = 0x03;
 			west = 0x04;
 			east = 0x05;
 		}
 		
 		byte direction = block.getData();
 		
 		if (direction == north)
 		{
 			return BlockFace.NORTH;
 		}
 		else if (direction == south)
 		{
 			return BlockFace.SOUTH;
 		}
 		else if (direction == west)
 		{
 			return BlockFace.WEST;
 		}
 		else if (direction == east)
 		{
 			return BlockFace.EAST;
 		}
 		else
 		{
 			return null;
 		}
 	}
 	
 	/**
 	 * Returns whether given material matches cloaker material
 	 */
 	public static boolean isCloakerType(Material material)
 	{
 		return (material.equals(MachineFactoryPlugin.CLOAKER_ACTIVATED) 
 				||material.equals(MachineFactoryPlugin.CLOAKER_DEACTIVATED));
 	}
 	
 	/**
 	 * Returns appropriate display name for Cloaker with given tier level
 	 */
 	public static String CloakerName(int tierLevel)
 	{
 		return "T" + tierLevel + " Cloaker";
 	}
 	
 	/**
 	 * Returns the tier level of the item
 	 */
 	public static int getTierLevel(String name)
 	{
 		for (int i = 1; i <= MachineFactoryPlugin.MAX_OREGIN_TIERS; i++)
 		{
 			if (name.equalsIgnoreCase(Cloaker.CloakerName(i)))
 				return i;
 		}
 			
 		return 0;
 	}
 	
 }
