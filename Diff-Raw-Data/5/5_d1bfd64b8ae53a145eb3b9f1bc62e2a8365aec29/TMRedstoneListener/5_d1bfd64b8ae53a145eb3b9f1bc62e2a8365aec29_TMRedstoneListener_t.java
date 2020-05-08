 package me.thommy101.TMinecart;
 
 import java.util.HashMap;
 
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.block.Chest;
 import org.bukkit.block.Sign;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.StorageMinecart;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockRedstoneEvent;
 import org.bukkit.inventory.ItemStack;
 
 public class TMRedstoneListener implements Listener
 {
 @SuppressWarnings("unused")
 private static TMinecart plugin;
 	
 	public TMRedstoneListener(TMinecart instance)
 	{
 		plugin = instance;
 	}
 	
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onRedstoneChange(BlockRedstoneEvent event)
 	{
 		Block dRail = event.getBlock();
 		//If block isn't a detectorrail || new current isn't 1(ON) -> return
 		if(dRail.getTypeId()!=28 || event.getNewCurrent()!=1) return;
 		Location loc = new Location(dRail.getWorld(), dRail.getX(), dRail.getY()-2, dRail.getZ());
 		Sign sign = null;
 		if(loc.getBlock().getTypeId()==63||loc.getBlock().getTypeId()==68)
 		{
 			sign = (Sign)loc.getBlock().getState();
 		}
 		if(sign != null)
 		{
 			int[] blockIds;
 			String line1 = sign.getLine(1);
 			if(line1.equals("[collect]"))
 			{
 				blockIds=readBlocks(sign);
 				findCart(dRail, true, blockIds);
 			}
 			else if (line1.equals("[deposit]"))
 			{
 				blockIds=readBlocks(sign);
 				findCart(dRail, false, blockIds);
 			}
 		}
 	}
 	
 	/**
 	 * Finds a cart close to the detector rail.
 	 * Checks the inventory of the cart/chest.
 	 * Executes the fill/emptying of the cart/chest.
 	 * 
 	 * @param dRail		Detector Rail block.
 	 * @param fill		Need to fill cart or not?
 	 * @param blocks	Array of blocks needed to check.
 	 */
 	private void findCart(Block dRail, Boolean fill, int[] blocks)
 	{
 		//Find a minecart with chest
 		Entity entities[] = dRail.getChunk().getEntities();
 		for(Entity entity:entities)
 		{
 			if(isValidStorageCart(entity, dRail.getLocation()))
 			{
 				//Find a chest next to detector rail
 				World world = dRail.getWorld();
 				int intX = dRail.getX();
 				int intY = dRail.getY();
 				int intZ = dRail.getZ();
 				Location locations[]={
 						new Location(world, intX+1, intY, intZ),
 						new Location(world, intX+1, intY-1, intZ),
 						new Location(world, intX-1, intY, intZ),
 						new Location(world, intX-1, intY-1, intZ),
 						new Location(world, intX, intY, intZ+1),
 						new Location(world, intX, intY-1, intZ+1),
 						new Location(world, intX, intY, intZ-1),
 						new Location(world, intX, intY-1, intZ-1)};
 				for(Location location:locations)
 				{
					if(location.getBlock().getTypeId()!=54) continue;
 					for(int blockid:blocks)
 					{
						if(blockid==0) continue;
  						if(fill)
  						{
  							int amount = checkChestInv(location, blockid);
 	 						//fill cart with amount in chest found
 	 						if(amount!=0)
 	 						{
 	 							int leftover = modCart(entity, blockid, true, amount);
 	 							amount -= leftover;
 	 							//empty chest with items that fit in the cart
 	 							modChest(location, blockid, false, amount);
 	 						}
  						}else{
 							int amount = checkCartInv(entity, blockid);
 							//fill chest with amount in chest found
 							if(amount!=0)
 							{
 								int leftover = modChest(location, blockid, true, amount);
 								amount -= leftover;
 								//empty cart with items that fit in the chest
 								modCart(entity, blockid, false, amount);
 							}
  						}
 					}
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Checks if entity is a valid storage minecart, which is close to the detector rail.
 	 * 
 	 * @param entity			The entity needed to be checked.
 	 * @param dRailLocation		The location of the detector rail.
 	 * @return					True if vallid storage minecart.
 	 */
 	private boolean isValidStorageCart(Entity entity, Location dRailLocation)
 	{
 		if(!(entity instanceof StorageMinecart)) return false;
 		double distanceToRail=entity.getLocation().distance(dRailLocation);
 		return distanceToRail <= 1.5;
 	}
 	
 	/**
 	 * Reads the sign to check the information.
 	 * 
 	 * @param sign	The sign needed to be checked.
 	 * @return		An array of block ID's need to be checked in next part.
 	 */
 	//TODO Make an "*" option to get all blocks out of chest/cart
 	private int[] readBlocks(Sign sign)
 	{
 		int blocks[] = {0, 0, 0, 0, 0, 0, 0, 0};
 		String line2=sign.getLine(2);
 //		if(line2.equals("*") || line2.equalsIgnoreCase("all"))
 //		{
 //			blocks[0] = -1;
 //			return blocks;
 //		}
 		String splitLine[] = line2.split(",", 8);
 		for(int i=0; i<splitLine.length ; i++)
 		{
 			try
 			{
 				blocks[i]=Integer.parseInt(splitLine[i].trim());
 			}
 			catch(NumberFormatException ex){}
 		}
 		return blocks;
 	}
 	
 	/**
 	 * Checks if param "blockid" is in storage minecart "entity".
 	 * 
 	 * @param entity	The storage minecart which inventory needs to be checked.
 	 * @param blockid	The ID of the block that need to be found.
 	 * @return			The amount of blockid found.
 	 */
 	private int checkCartInv(Entity entity, int blockid)
 	{
 		StorageMinecart storageMinecart=(StorageMinecart) entity;
 		ItemStack[] items = storageMinecart.getInventory().getContents();
 		int amount = 0;
 		for(int i=0;i<items.length;i++)
 		{
 			ItemStack item = items[i];
 			if(item!=null && item.getAmount()>0 && item.getTypeId()==blockid)
 			{
 				amount += item.getAmount();
 			}
 		}
 		return amount;
 	}
 	
 	/**
 	 * Checks if param "blockid" is in chest at "location".
 	 * 
 	 * @param location	The location of the chest.
 	 * @param blockid	The ID of the block that need to be found.
 	 * @return			The amount of blockid found.
 	 */
 	private int checkChestInv(Location location, int blockid)
 	{
 		Chest chest=(Chest)location.getBlock().getState();
 		ItemStack[] items = chest.getInventory().getContents();
 		int amount = 0;
 		for(int i=0; i<items.length; i++)
 		{
 			ItemStack item = items[i];
 			if(item!=null && item.getAmount()>0 && item.getTypeId()==blockid)
 			{
 				amount += item.getAmount();
 			}
 		}
 		return amount;
 	}
 	
 	/**
 	 * Modifies a storage minecart.
 	 * Fills/empties a cart with given "amount" of "blockid".
 	 * 
 	 * @param entity	The entity that is the storage minecart.
 	 * @param blockid	The block id that need to be put in/get out of the storage minecart.
 	 * @param fill		True if fill, False if empty.
 	 * @param amount	Amount of blockid that need to be processed.
 	 * @return			Amount that didn't fit in the cart.
 	 */
 	private int modCart(Entity entity, int blockid, boolean fill, int amount)
 	{
 		StorageMinecart storageMinecart=(StorageMinecart) entity;
 		if(fill)
 		{
 			ItemStack itemstack = new ItemStack(blockid, amount);
 			HashMap<Integer, ItemStack> hmLeftover = storageMinecart.getInventory().addItem(itemstack);
 			int leftover = 0;
 			if(!(hmLeftover.isEmpty()) || hmLeftover==null)
 			{
 				leftover = hmLeftover.get(0).getAmount();
 			}
 			return leftover;
 		}else{
 			//empty
 			ItemStack itemstack = new ItemStack(blockid, amount);
 			storageMinecart.getInventory().removeItem(itemstack);
 		}
 		return 0;
 	}
 	
 	/**
 	 * Modifies a chest.
 	 * Fills/empties a chest with given "amount" of "blockid".
 	 * 
 	 * @param location	Location of the chest.
 	 * @param blockid	THe block id that need to be put in/get out of the chest.
 	 * @param fill		True if full, False if empty.
 	 * @param amount	Amount of blockid that need to be processed.
 	 * @return			Amount that didn't fit in the cart.
 	 */
 	private int modChest(Location location, int blockid, boolean fill, int amount)
 	{
 		Chest chest = (Chest)location.getBlock().getState();
 		if(fill)
 		{
 			ItemStack itemstack = new ItemStack(blockid, amount);
 			HashMap<Integer, ItemStack> hmLeftover = chest.getInventory().addItem(itemstack);
 			int leftover = 0;
 			if(!(hmLeftover.isEmpty()) || hmLeftover==null)
 			{
 				leftover = hmLeftover.get(0).getAmount();
 			}
 			return leftover;
 		}else{
 			//empty
 			ItemStack itemstack = new ItemStack(blockid, amount);
 			chest.getInventory().removeItem(itemstack);
 		}
 		return 0;
 	}
 }
