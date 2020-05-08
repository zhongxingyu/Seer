 package ca.mcpnet.RailDriver;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import org.bukkit.Effect;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.block.Chest;
 import org.bukkit.block.Furnace;
 import org.bukkit.entity.Item;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.material.Lever;
 import org.bukkit.material.MaterialData;
 import org.bukkit.material.Torch;
 
 import ca.mcpnet.RailDriver.RailDriver.Facing;
 
 public class RailDriverTask implements Runnable {
 
 	private RailDriver plugin;
 	private int x,y,z;
 	private BlockFace direction;
 	private World world;
 	private int taskid;
 	int iteration;
 	boolean nexttorch;
 	ArrayList<ItemStack> collecteditems;
 	Iterator<ItemStack> itemitr;
 	boolean whichdispenser;
 	int smokedir;
 	
 	RailDriverTask(RailDriver instance, Block block) {
 		plugin = instance;
 		x = block.getX();
 		y = block.getY();
 		z = block.getZ();
 		Lever lever = new Lever(block.getType(),block.getData());
 		direction = lever.getAttachedFace();
 		world = block.getWorld();
 		taskid = -1;
 		iteration = 0;
 		nexttorch = false;
 		collecteditems = new ArrayList<ItemStack>();
 		whichdispenser = false;
 		if (direction == BlockFace.EAST) {
 			smokedir = 7;
 		} else if (direction == BlockFace.WEST) {
 			smokedir = 1;
 		} else if (direction == BlockFace.SOUTH) {
 			smokedir = 3;
 		} else if (direction == BlockFace.NORTH) {
 			smokedir = 5;
 		} else {
 			smokedir = 4;
 		}
 
 	}
 	
 	Block getRelativeBlock(int i, int j, int k) {
 		if (direction == BlockFace.NORTH) {
 			return world.getBlockAt(x-i,y-1+k,z+1-j);
 		} else if (direction == BlockFace.EAST) {
 			return world.getBlockAt(x-1+j,y-1+k,z-i);
 		} else if (direction == BlockFace.WEST) {
 			return world.getBlockAt(x+1-j,y-1+k,z+i);
 		} else if (direction == BlockFace.SOUTH) {
 			return world.getBlockAt(x+i,y-1+k,z-1+j);
 		} else {
 			return null;
 		}
 	}
 	void smokePuff() {
 		world.playEffect(getRelativeBlock(1,0,1).getLocation(), Effect.SMOKE, smokedir);
 		world.playEffect(getRelativeBlock(1,2,1).getLocation(), Effect.SMOKE, smokedir);
 	}
 	void setDrillSwitch(boolean on) {
 		Block block = getRelativeBlock(2,1,2);
 		Lever leverblock = new Lever(block.getType(),block.getData());
 		leverblock.setPowered(on);
 		block.setData(leverblock.getData());
 	}
 	void setMainSwitch(boolean on) {
 		Block block = getRelativeBlock(0,1,1);
 		Lever leverblock = new Lever(block.getType(),block.getData());
 		leverblock.setPowered(on);
 		block.setData(leverblock.getData());
 	}
 	public void run() {
 		if (taskid == -1) {
 			setMainSwitch(false);
 			setDrillSwitch(false);
 			world.playEffect(new Location(world,x,y,z), Effect.EXTINGUISH,0);
 			smokePuff();
 			plugin.taskset.remove(this);
 		}
 
 		iteration++;
 		if (iteration == 1) {
 			setDrillSwitch(false);
 			if (plugin.getConfig().getBoolean("requires_fuel")) {
 				if (!burnCoal()) {
 					RailDriver.log("Raildriver has insufficient fuel!");
 					plugin.taskset.remove(taskid);
 					deactivate();
 				}
 				
 			}
 		}
 		if (iteration == 6) {
 			Block leverblock = world.getBlockAt(x, y, z);
 			if (!plugin.isRailDriver(leverblock)) {
 				RailDriver.log("Raildriver malfunction during drill phase!");
 				plugin.taskset.remove(taskid);
 				deactivate();
 				return;
 			}
 			// Remove materials in front of bit
 			if (!excavate()) {
 				RailDriver.log("Raildriver encountered obstruction!");
 				deactivate();
 				return;
 			}
 			setDrillSwitch(true);
 			smokePuff();
 		}
 		if (iteration == 8) {
 			ejectItems();
 		}
 		if (iteration == 12) {
 			setDrillSwitch(false);
 		}
 		if (iteration == 18) {
 			setDrillSwitch(true);
 			smokePuff();
 		}
 		if (iteration == 20) {
 			ejectItems();
 		}
 		if (iteration == 24) {
 			setDrillSwitch(false);
 		}
 		if (iteration == 30) {
 			setDrillSwitch(true);
 			smokePuff();
 		}
 		if (iteration == 32) {
 			ejectItems();
 		}
 		if (iteration == 36) {
 			setDrillSwitch(false);
 		}
 		if (iteration == 40) {
 			world.playEffect(getRelativeBlock(0,1,-1).getLocation(), Effect.STEP_SOUND, Material.LAVA.getId());
 			// Do track laying stuff
 		}
 		if (iteration == 42) {
 			world.playEffect(getRelativeBlock(0,0,-1).getLocation(), Effect.STEP_SOUND, Material.LAVA.getId());
 			world.playEffect(getRelativeBlock(0,2,-1).getLocation(), Effect.STEP_SOUND, Material.LAVA.getId());
 			// Do track laying stuff			
 		}
 		if (iteration == 44) {
 			world.playEffect(getRelativeBlock(0,-1,1).getLocation(), Effect.STEP_SOUND, Material.LAVA.getId());
 			world.playEffect(getRelativeBlock(0,3,1).getLocation(), Effect.STEP_SOUND, Material.LAVA.getId());
 			// Do track laying stuff			
 		}
 		if (iteration == 46) {
 			world.playEffect(getRelativeBlock(1,-1,2).getLocation(), Effect.STEP_SOUND, Material.LAVA.getId());
 			world.playEffect(getRelativeBlock(1,3,2).getLocation(), Effect.STEP_SOUND, Material.LAVA.getId());
 			// Do track laying stuff			
 		}
 		if (iteration == 48) {
 			// Check that it's still a raildriver
 			Block leverblock = world.getBlockAt(x, y, z);
 			if (!plugin.isRailDriver(leverblock)) {
 				RailDriver.log("Raildriver malfunction during advance phase!");
 				plugin.taskset.remove(taskid);
 				deactivate();
 				return;
 			}
 			if (!advance()) {
 				RailDriver.log("Raildriver encountered unstable environment!");
 				deactivate();
 				return;
 			}
 			// RailDriver.log.info("RailDriver "+taskid+" in chunk "+world.getChunkAt(getRelativeBlock(0,1,1)));
 			world.createExplosion(getRelativeBlock(2,1,1).getLocation(), 0);
 			iteration = 0;
 		}
 		
 		// world.playEffect(block.getLocation(), Effect.STEP_SOUND, block.getTypeId());
 		/*
 		Block block = this.getRelativeBlock(1, 0, i);
 		i = (i + 1) % 3;
 		RailDriver.log.info(block.getType().name());
 		*/
 		// world.createExplosion(x, y, z, 0);
 		// Light the fires
 	}
 	
 	private boolean burnCoal() {
 		Block leftblock = getRelativeBlock(1,0,0);
 		Furnace leftfurnace = (Furnace) leftblock.getState();
 		Inventory leftinventory = leftfurnace.getInventory();
 		Block rightblock = getRelativeBlock(1,2,0);
 		Furnace rightfurnace = (Furnace) rightblock.getState();
 		Inventory rightinventory = rightfurnace.getInventory();
 		if (!leftinventory.contains(Material.COAL) || 
 				(!rightinventory.contains(Material.COAL))) {
 			return false;
 		}
		leftinventory.removeItem(new ItemStack(Material.COAL,1));
		rightinventory.removeItem(new ItemStack(Material.COAL,1));
 		return true;
 	}
 
 	private boolean advance() {
 		// Check to make sure ground under is solid
 		for (int lx = 0; lx < 3; lx++) {
 			for (int lz = 0; lz < RailDriver.raildriverblocklist[lx][0].length; lz++) {
 				Block block = getRelativeBlock(lz,lx,-1);
 				if (block.isEmpty() || block.isLiquid()) {
 					return false;
 				}
 			}
 		}
 		// Check to make sure behind has no liquid
 		for (int lx = 0; lx < 3; lx++) {
 			Block block = getRelativeBlock(0,lx,0);
 			if (block.isLiquid()) {
 				return false;
 			}			
 		}
 		
 		for (int lx = 0; lx < 3; lx++) {
 			for (int ly = 0; ly < 3; ly++) {
 				for (int lz = RailDriver.raildriverblocklist[lx][ly].length; lz > 0; lz--) {
 					Block target = getRelativeBlock(lz,lx,ly);
 					Block source = getRelativeBlock(lz-1,lx,ly);
 					// RailDriver.log.info(source.getType().name());
 					if (source.getType() == Material.CHEST) {
 						// Get the old chest info
 						Chest sourcechest = (Chest) source.getState();
 						Inventory sourceinventory = sourcechest.getInventory();
 						ItemStack[] sourceitems = sourceinventory.getContents();
 						sourceinventory.clear();
 						MaterialData sourcedata = sourcechest.getData();
 						// Blow the old chest away
 						source.setType(Material.AIR);
 						
 						target.setType(Material.CHEST);
 						Chest targetchest = (Chest) target.getState();
 						targetchest.setData(sourcedata);
 						Inventory targetinventory = targetchest.getInventory();
 						targetinventory.setContents(sourceitems);
 						targetchest.update();
 					} else if (source.getType() == Material.BURNING_FURNACE) {
 						Furnace sourcefurnace = (Furnace) source.getState();
 						Inventory sourceinventory = sourcefurnace.getInventory();
 						ItemStack[] sourceitems = sourceinventory.getContents();
 						sourceinventory.clear();
 						MaterialData sourcedata = sourcefurnace.getData();
 						// Blow the old chest away
 						source.setType(Material.AIR);
 						
 						target.setType(Material.BURNING_FURNACE);
 						Furnace targetfurnace = (Furnace) target.getState();
 						targetfurnace.setData(sourcedata);
 						Inventory targetinventory = targetfurnace.getInventory();
 						targetinventory.setContents(sourceitems);
 						targetfurnace.update();
 					} else if (source.getType() == Material.DISPENSER || 
 							source.getType() == Material.FURNACE ||
 							source.getType() == Material.LEVER) {
 						byte sourcedata = source.getData();
 						Material sourcematerial = source.getType();
 						source.setType(Material.AIR);
 						target.setType(sourcematerial);
 						target.setData(sourcedata);
 					} else {
 						target.setType(source.getType());
 						target.setData(source.getData());
 					}
 				}
 			}
 		}
 		// Set the floor made of stone
 		for (int lx = 0; lx < 3; lx++)
 			getRelativeBlock(1,lx,-1).setTypeId(98);
 		for (int lx = 0; lx < 3; lx++) {
 			for (int ly = 0; ly < 3; ly++) {
 				if (!(lx == 1 && ly == 1)) {
 					getRelativeBlock(1,lx,ly).setType(Material.AIR);
 				}
 			}
 		}
 		int period = 8;
 		int distance;
 		if (direction == BlockFace.NORTH ||
 				direction == BlockFace.SOUTH) {
 			distance = x;
 		} else {
 			distance = z;
 		}
 		if (distance % period == 0) {
 			for (int ly = 0; ly < 3; ly++) {
 				getRelativeBlock(1,-1,ly-1).setTypeId(98);
 				getRelativeBlock(1,3,ly-1).setTypeId(98);
 			}
 			
 			Block left = getRelativeBlock(1,0,1);
 			if (nexttorch) {
 				left.setType(Material.REDSTONE_TORCH_ON);
 			} else {
 				left.setType(Material.TORCH);				
 			}
 			Torch lefttorch = new Torch(left.getType(),left.getData());
 			lefttorch.setFacingDirection(Facing.RIGHT.translate(direction));
 			left.setData(lefttorch.getData());
 
 			getRelativeBlock(1,1,0).setType(Material.POWERED_RAIL);
 
 			Block right = getRelativeBlock(1,2,1);
 			if (nexttorch) {
 				right.setType(Material.TORCH);				
 			} else {
 				right.setType(Material.REDSTONE_TORCH_ON);
 			}
 			Torch righttorch = new Torch(right.getType(),right.getData());
 			righttorch.setFacingDirection(Facing.LEFT.translate(direction));
 			right.setData(righttorch.getData());
 			
 			nexttorch = !nexttorch;
 		} else { // Regular rail
 			getRelativeBlock(1,1,0).setType(Material.RAILS);
 		}
 		// plugin.getServer().getScheduler().cancelTask(taskid);
 		// plugin.taskset.remove(this);
 		Location newloc = getRelativeBlock(1,1,1).getLocation();
 		x = newloc.getBlockX();
 		y = newloc.getBlockY();
 		z = newloc.getBlockZ();
 		return true;
 	}
 
 	private void ejectItems() {
 		if (!collecteditems.isEmpty()) {
 			world.createExplosion(getRelativeBlock(6,1,1).getLocation(), 0);
 			for (int i = 0;i < 3 && itemitr.hasNext(); i++) {
 				ItemStack curstack = itemitr.next();
 				itemitr.remove();
 				Location loc;
 				if (whichdispenser) {
 					loc = getRelativeBlock(0,0,1).getLocation();
 					whichdispenser = false;
 				} else {
 					loc = getRelativeBlock(0,2,1).getLocation();
 					whichdispenser = true;
 				}
 				world.dropItem(loc, curstack);
 				world.playEffect(loc, Effect.STEP_SOUND, curstack.getTypeId());
 			}
 		}
 	}
 
 	private boolean excavate() {
 		for (int lx = 0; lx < 3; lx++) {
 			for (int ly = 0; ly < 3; ly++) {
 				Block block = getRelativeBlock((RailDriver.raildriverblocklist[lx][ly]).length, lx, ly);
 				if (block.isLiquid()) {					
 					return false; 
 				}
 				if (!block.isEmpty()) {
 					collecteditems.add(new ItemStack(block.getType(),1));
 					block.setType(Material.AIR);
 				}
 			}
 		}
 		itemitr = collecteditems.iterator();
 		return true;
 	}
 
 	public boolean matchBlock(Block block) {
 		if (block.getLocation().getBlockX() == x &&
 				block.getLocation().getBlockY() == y &&
 				block.getLocation().getBlockZ() == z) {
 			return true;
 		}
 		return false;
 	}
 	public void setBlockTypeSaveData(Block block, Material type) {
 		byte data = block.getData();
 		block.setType(type);
 		block.setData(data);		
 	}
 	public void setFurnaceBurning(Block block, boolean on) {
 		if (block.getType() != Material.BURNING_FURNACE &&
 				block.getType()  != Material.FURNACE) {
 			return;
 		}
 		Furnace furnace = (Furnace) block.getState();
 		Inventory inventory = furnace.getInventory();
 		ItemStack[] contents = inventory.getContents();
 		inventory.clear();
 		MaterialData data = furnace.getData();
 
 		if (on) {
 			block.setType(Material.BURNING_FURNACE);
 		} else {
 			block.setType(Material.FURNACE);
 		}
 		furnace = (Furnace) block.getState();
 		furnace.setData(data);
 		inventory = furnace.getInventory();
 		inventory.setContents(contents);
 		furnace.update();
 
 	}
 	public void activate() {
 		if (taskid != -1) {
 			RailDriver.log("Activation requested on already active raildriver "+taskid);
 			return;
 		}
 		taskid = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, this, 10L, 2L);
 		RailDriver.log("Activated "+direction.name()+ "BOUND raildriver "+taskid);
 		// Light the fires
 		setFurnaceBurning(getRelativeBlock(1,0,0),true);
 		setFurnaceBurning(getRelativeBlock(1,2,0),true);
 	}
 	
 	public void deactivate() {
 		if (taskid == -1) {
 			RailDriver.log("Deactivation requested for already inactive raildriver!");
 			return;
 		}
 		plugin.getServer().getScheduler().cancelTask(taskid);
 		RailDriver.log("Deactivated raildriver "+taskid);
 		// Shut off furnaces
 		setFurnaceBurning(getRelativeBlock(1,0,0),false);
 		setFurnaceBurning(getRelativeBlock(1,2,0),false);
 		// Shutdown hiss
 		world.playEffect(new Location(world,x,y,z), Effect.EXTINGUISH,0);
 		taskid = -1;
 		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, this, 10L);
 	}
 }
