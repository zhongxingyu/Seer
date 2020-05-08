 package com.deaboy.amber.record;
 
 import java.util.Calendar;
 import java.util.List;
 import java.util.ArrayList;
 import java.util.logging.Level;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.block.BlockState;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.EntityType;
 import org.bukkit.event.Listener;
 import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.craftbukkit.v1_5_R3.CraftWorld;
 
 import com.deaboy.amber.Amber;
 import com.deaboy.amber.util.Constants;
 import com.deaboy.amber.util.Deserializer;
 import com.deaboy.amber.util.Serializer;
 import com.deaboy.amber.util.Util;
 
 public class AmberWorldRecorder implements Listener
 {
 	public final World world;
 	
 	private final AmberWorldRecorderFileOutput output;
 	private final AmberWorldRecorderFileInput input;
 	private final AmberWorldRecorderListener listener;
 	
 	private static final String metadata = "Ambered";
 	private long metavalue;
 	private Status status;
 	
 	private int schedule;
 	private static final int apt = 1000; // ACTIONS PER TICK
 	
 	private String worldData = null;
 
 	private List<TinyBlockLoc> blockLocs = new ArrayList<TinyBlockLoc>();
 	
 	public AmberWorldRecorder(World world)
 	{
 		this.world = world;
 		
 		output = new AmberWorldRecorderFileOutput(world.getName());
 		input = new AmberWorldRecorderFileInput(world.getName());
 		listener = new AmberWorldRecorderListener(world);
 		
 		status = Status.IDLE;
 	}
 
 	/* *************** RECORDING METHODS ************* */
 	
 	public void startRecording()
 	{
 		if (status == Status.IDLE)
 		{
 			status = Status.RECORDING;
 		}
 		else
 		{
 			return;
 		}
 		metavalue = Calendar.getInstance().getTimeInMillis();
 		output.open();
 		listener.startListening();
 		output.write(Serializer.serializeWorld(world));
 		saveAllEntities();
 	}
 
 	public void stopRecording()
 	{
 		if (status == Status.RECORDING)
 		{
 			status = Status.IDLE;
 		}
 		else
 		{
 			return;
 		}
 		output.close();
 		listener.stopListening();
 		blockLocs.clear();
 	}
 	
 	private void saveAllEntities()
 	{
 		if (output == null)
 		{
 			return;
 		}
 		
 		for (Entity e : world.getEntities())
 		{
 			if (e.getType() == EntityType.PLAYER)
 			{
 				continue;
 			}
 			String data = Serializer.serializeEntity(e);
 			output.write(data);
 		}
 	}
 	
 	/* *************** RESTORING METHODS ************* */
 	
 	public void startRestoring()
 	{
 		stopRecording();
 		if (status == Status.IDLE || status == Status.RECORDING)
 		{
 			status = Status.RESTORING;
 		}
 		else
 		{
 			return;
 		}
 		input.open();
 		listener.startListening();
 		
 		for (Entity e : world.getEntities())
 		{
 			if (e.getType() == EntityType.PLAYER)
 			{
 				continue;
 			}
 			e.remove();
 		}
 		((CraftWorld) world).getHandle().isStatic = true;
 		
 		schedule = Bukkit.getScheduler().scheduleSyncRepeatingTask(Amber.getInstance(), new Runnable()
 		{
 			public void run()
 			{
 				restoreStep();
 			}
 		}, 3, 3);
 	}
 	
 	public void stopRestoring()
 	{
 		Bukkit.getLogger().log(Level.INFO, "stopping restoring...");
 		((CraftWorld) world).getHandle().isStatic = false;
 		if (status == Status.RESTORING)
 		{
 			status = Status.IDLE;
 		}
 		else
 		{
 			return;
 		}
 		
 		if (worldData != null)
 		{
 			Deserializer.deserializeWorld(worldData, world);
 			worldData = null;
 		}
 		
 		input.close();
 		listener.stopListening();
 		
 		Bukkit.getScheduler().cancelTask(schedule);
 		Bukkit.getLogger().log(Level.INFO, "restoration complete");
 	}
 	
 	public void restoreStep()
 	{
 		int step = apt;
 		
 		if (!blockLocs.isEmpty())
 		{
 			BlockState block;
 			
 			for (TinyBlockLoc loc : blockLocs)
 			{
 				block = world.getBlockAt(loc.toLocation(world)).getState();
 				if (block.getRawData() == 0)
 				{
 					block.getBlock().setType(Material.AIR);
 				}
 				else if (block.getRawData() == 1)
 				{
 					block.getBlock().setType(Material.WATER);
 				}
 				else if (block.getRawData() == 2)
 				{
 					block.getBlock().setType(Material.LAVA);
 				}
 				else
 				{
 					block.getBlock().setType(Material.AIR);
 				}
 				//step--;
 			}
 			
 			blockLocs.clear();
 		}
 		
 		while (step > 0)
 		{
 			String data = input.read();
 			
 			if (data == null)
 			{
 				stopRestoring();
 				return;
 			}
 			
 			if (data.startsWith(Constants.prefixWorld))
 			{
 				//Bukkit.getLogger().log(Level.INFO, "Restoring World");
 				worldData = data;
 			}
 			else if (data.startsWith(Constants.prefixEntity))
 			{
 				//Bukkit.getLogger().log(Level.INFO, "Restoring entity");
 				Deserializer.deserializeEntity(data);
 				step--;
 			}
 			else if (data.startsWith(Constants.prefixBlock))
 			{
 				//Bukkit.getLogger().log(Level.INFO, "Restoring block");
 				Block block = Deserializer.deserializeBlock(data);
 				step--;
 				
 				if (block.hasMetadata(metadata))
 					block.removeMetadata(metadata, Amber.getInstance());
 				/*
 				if (locationAlreadySaved(block.getLocation()))
 				{
 					TinyBlockLoc loc = null;
 					for (TinyBlockLoc l : blockLocs)
 					{
 						if (l.equals(block.getLocation()))
 						{
 							loc = l;
 							break;
 						}
 					}
 					if (loc != null)
 					{
 						blockLocs.remove(loc);
 					}
 				}
 				*/
 				/*
 				if (block.getType() == Material.SAND || block.getType() == Material.GRAVEL || block.getType() == Material.ANVIL || block.getType() == Material.DRAGON_EGG)
 				{
 					block = block.getRelative(BlockFace.DOWN);
 					
 					if (block.getType() == Material.AIR)
 					{
 						block.setType(Material.SANDSTONE);
 						block.setData((byte) 0);
 						blockLocs.add(new TinyBlockLoc(block.getLocation()));
 					}
 					else if (block.getType() == Material.WATER)
 					{
 						block.setType(Material.SANDSTONE);
 						block.setData((byte) 1);
 						blockLocs.add(new TinyBlockLoc(block.getLocation()));
 					}
 					else if (block.getType() == Material.LAVA)
 					{
 						block.setType(Material.SANDSTONE);
 						block.setData((byte) 2);
 						blockLocs.add(new TinyBlockLoc(block.getLocation()));
 					}
 					else if (block.isLiquid())
 					{
 						block.setType(Material.SANDSTONE);
 						block.setData((byte) 0);
 						blockLocs.add(new TinyBlockLoc(block.getLocation()));
 					}
 				}
 				*/
 			}
 			else
 			{
 				continue;
 			}
 		}
 	}
 
 	public boolean saveBlock(BlockState block)
 	{
 		return saveBlock(block, true);
 	}
 
 	private boolean saveBlock(BlockState block, boolean below)
 	{
 		if (output == null)
 		{
 			return false;
 		}
 		
 		if (blockAlreadySaved(block))
 			return false;
 		else
 		{
 			block.setMetadata(metadata, new FixedMetadataValue(Amber.getInstance(), metavalue));
 		}
 		/*
 		if (locationAlreadySaved(block.getLocation()))
 		{
 			return false;
 		}
 		
 		blockLocs.add(new TinyBlockLoc(block.getLocation()));
 		*/
 		String data = Serializer.serializeBlockState(block);
 		output.write(data);
 		
 		if (block.getType() == Material.CHEST) // Check if double chest, then save the other one too.
 		{
 			BlockState block2;
 			
 			if ((block2 = block.getBlock().getRelative(BlockFace.NORTH).getState()).getType() == Material.CHEST
 					|| (block2 = block.getBlock().getRelative(BlockFace.SOUTH).getState()).getType() == Material.CHEST
 					|| (block2 = block.getBlock().getRelative(BlockFace.EAST).getState()).getType() == Material.CHEST
 					|| (block2 = block.getBlock().getRelative(BlockFace.WEST).getState()).getType() == Material.CHEST)
 				saveBlock(block2);
 		}
 		if (below)
 		{
 			saveTransparentBlocksBelow(block);
 		}
 		
 		return true;
 	}
 	
 	public void saveTransparentBlocksBelow(BlockState block)
 	{
 		BlockState block2;
 		block2 = block.getBlock().getRelative(BlockFace.DOWN).getState();
 		
 		while (!Util.isSolid(block2.getType()) && block2.getY() > 0)
 		{
 			saveBlock(block2, false);
 			block2 = block2.getBlock().getRelative(BlockFace.DOWN).getState();
 		}
 	}
 	
 	public boolean blockAlreadySaved(BlockState block)
 	{
 		/*
 		for (TinyBlockLoc bLoc : blockLocs)
 		{
 			if (bLoc.equals(loc))
 			{
 				return true;
 			}
 		}
 		return false;
 		*/
 		return block.hasMetadata(metadata) && block.getMetadata(metadata).get(0).asLong() == metavalue;
 	}
 	
 	public boolean isIdle()
 	{
 		return status == Status.IDLE;
 	}
 	
 	public boolean isRecording()
 	{
 		return status == Status.RECORDING;
 	}
 	
 	public boolean isRestoring()
 	{
 		return status == Status.RESTORING;
 	}
 	
 	private enum Status
 	{
 		RECORDING, IDLE, RESTORING;
 	}
 	
 }
