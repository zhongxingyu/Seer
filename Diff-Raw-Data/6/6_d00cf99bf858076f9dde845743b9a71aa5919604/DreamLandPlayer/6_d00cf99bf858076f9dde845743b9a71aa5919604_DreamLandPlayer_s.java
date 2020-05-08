 package me.cmesh.DreamLand;
 
 import java.io.*;
 //import java.util.logging.Logger;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.BlockFace;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 public class DreamLandPlayer
 {
 	public Player self() {return player;}
 	public DreamLandPlayer self(Player player){this.player = player; return this;}
 	
 	public Location getLocation() {return player.getLocation();}
 	public World getWorld() {return player.getWorld();}
 	public String getName() {return player.getName();}
 	public void sendMessage(String message) {player.sendMessage(message);}
 	
 	private bed Bed = new bed();
 	private inventory Inventory = new inventory();
 	private health Health = new health();
 	
 	private Player player;
 	public Long lastFly;
 	
 	private static DreamLand plugin;
 	
 	public DreamLandPlayer(DreamLand instance)
 	{
 		plugin = instance;
 	}
 	
 	public void enterDream(Location bed, Boolean nightmare)
 	{
 		Bed.set(bed);
 		Health.save();
 		
 		DreamLandWorld setting = nightmare ? plugin.nightmare : plugin.dream;
 		
 		Location loc = setting.getWorld().getSpawnLocation();
 		
 		Inventory.save(player.getWorld());
 		Inventory.load(loc.getWorld());
 		
 		if(nightmare)
 		{
 			if(loc.getBlock().getRelative(BlockFace.DOWN).getType() != Material.AIR)
 			{
 				while(loc.getBlock().getRelative(BlockFace.DOWN).getType() != Material.AIR)
 				{
 					loc = loc.getBlock().getRelative(BlockFace.DOWN).getLocation();
 				}
 			}
 		}
 		else
 		{
 			if(loc.getBlock().getRelative(BlockFace.DOWN).getType() == Material.AIR)
 			{
 				while(loc.getBlock().getRelative(BlockFace.DOWN).getType() == Material.AIR)
 				{
 					loc = loc.getBlock().getRelative(BlockFace.NORTH).getLocation();
 				}
 				setting.getWorld().setSpawnLocation(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
 			}
 			
 			setting.getWorld().setTime(100L);
 		}
 
 		loc.getBlock().getChunk().load();
 		
 		player.teleport(loc);
 		
 		if(!plugin.options.message.isEmpty())
 		{
 			player.sendMessage(plugin.options.message);
 		}
 		
 		DreamLand.log.info(player.getName() + " is dreaming");
 		return;
 	}
 
 	public void leaveDream()
 	{
 		player.setFireTicks(0);
 		player.setFallDistance(0);
 		Health.load();
 		
 		Location loc = plugin.world(player.getWorld()).ReturnToBed ? Bed.get() : Bed.get().getWorld().getSpawnLocation();
 
 		Inventory.save(player.getWorld());
 		Inventory.load(loc.getWorld());
 		
 		DreamLand.log.info(player.getName() + " woke up");
 		
 		player.teleport(loc);
 	}

 	public Location respawn()
 	{
 		player.setFireTicks(0);
 		player.setFallDistance(0);
 		Health.load();
 		
 		Location loc = plugin.world(player.getWorld()).ReturnToBed ? Bed.get() : Bed.get().getWorld().getSpawnLocation();
 
 		Inventory.save(player.getWorld());
 		Inventory.load(loc.getWorld());
 		
 		DreamLand.log.info(player.getName() + " woke up");
 		
 		return loc;
 	}
 
 	public Boolean hasPermission(String permission, Boolean expected)
 	{
 		return player.isOp() || player.hasPermission(permission);
 	}
 	
 	private class health
 	{
 		private int health;
 		private int food;
 		private float enchant;
 		
 		public void save()
 		{
 			health = player.getHealth();
 			food = player.getFoodLevel();
 			enchant = player.getExp();
 		}
 	
 		public void load()
 		{
 			player.setHealth(health);
 			player.setFoodLevel(food);
 			player.setExp(enchant);
 		}
 	}
 	
 	public class inventory
 	{				
 		private void WriteInv(File save, ItemStack[] inventory)
 		{
 			try
 			{
 				FileConfiguration out = YamlConfiguration.loadConfiguration(save);
 				
 				for(int i = 0; i < inventory.length; i++)
 				{
 					ItemStack item = inventory[i];
 					if(item != null)
 					{
 						out.set(i+"", item);
 					}
 					else
 					{
 						out.set(i+"", new ItemStack(Material.AIR));
 					}
 				}
 				out.save(save);
 			}
 			catch (Exception e)	{ e.printStackTrace(); }
 		}
 		
 		private void ReadInv(File save, ItemStack[] inventory)
 		{
 			try
 			{
 				FileConfiguration in = YamlConfiguration.loadConfiguration(save);
 				
 				for(int i = 0; i < inventory.length; i++)
 				{
 					inventory[i] = in.getItemStack(i+"");
 				}
 			}
 			catch (Exception e) {e.printStackTrace();}
 		}
 		
 		private File playerInv(World world)
 		{
 			File invFolder = new File(plugin.getDataFolder().getAbsolutePath() + File.separator + "Inventories");
 			if (!invFolder.exists()) 
 			{
 				invFolder.mkdir();
 			}
 			return new File(invFolder + File.separator + player.getName() + "." + world.getName());
 		}
 		
 		private File playerArmor(World world)
 		{
 			File invFolder = new File(plugin.getDataFolder().getAbsolutePath() + File.separator + "Inventories");
 			if (!invFolder.exists()) 
 			{
 				invFolder.mkdir();
 			}
 			return new File(invFolder + File.separator + player.getName() + "." + world.getName() + ".armor");
 		}
 		
 		public void save(World world)
 		{
 			if(plugin.world(world).PersistInventory)
 			{
 				WriteInv(playerInv(world), player.getInventory().getContents());
 				WriteInv(playerArmor(world), player.getInventory().getArmorContents());
 			}
 		}
 		
 		public void load(World world)
 		{
 			File save = playerInv(world);
 			if (save.exists()) 
 			{		
 				if(plugin.world(world).PersistInventory)
 				{
 						player.getInventory().clear();
 						
 						ItemStack[] inventory = player.getInventory().getContents();
 						ReadInv(save, inventory);
 						player.getInventory().setContents(inventory);
 						
 						ItemStack[] armor = new ItemStack[4];
 						ReadInv(playerArmor(world), armor);
 						player.getInventory().setArmorContents(armor);
 				}
 			}
 			else
 			{
 				if(plugin.world(world).InitialInventoryClear)
 				{
 					player.getInventory().clear();
 					player.getInventory().setArmorContents(new ItemStack[4]);
 					if(plugin.options.kit != null && plugin.world(world).Kit)
 					{
 						player.getInventory().setContents(plugin.options.kit);
 					}
 				}
 			}
 		}
 	}
 	
 	private class bed
 	{
 		private Location location;
 		
 		public Location get() 
 		{
 			if(location != null)
 			{
 				return location;
 			}
 			DreamLand.log.info("There was an issue loading a player's bed location");
 			return plugin.getServer().getWorlds().get(0).getSpawnLocation();
 		}
 		
 		public void set(Location location) 
 		{
 			player.setBedSpawnLocation(location);
 			this.location = location;
 		}
 	}
 	
 	public World getBedWorld()
 	{
 		return Bed.get().getWorld();
 	}
 	
 	public Boolean Dreaming()
 	{
 		return player.getWorld().equals(plugin.dream.getWorld())
 		|| (player.getWorld().equals(plugin.nightmare.getWorld()) && plugin.nightmare.Chance != 0);
 	}
 }
