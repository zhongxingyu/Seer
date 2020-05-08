 package infinitealloys.tile;
 
 import infinitealloys.util.Funcs;
 import infinitealloys.util.MachineHelper;
 import infinitealloys.util.Point;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.tileentity.TileEntity;
 import org.apache.commons.lang3.ArrayUtils;
 import cpw.mods.fml.common.network.PacketDispatcher;
 
 public class TEMEnergyStorage extends TileEntityMachine {
 
 	/** The amount of time, in ticks (20 ticks = 1 second), between each regular search for new machines to connect to. */
 	private final int SEARCH_INTERVAL = 200;
 
 	/** The maximum amount of RK that this machine can store */
 	private int maxRK;
 
 	/** The amount of RK currently stored in the unit */
 	private int currentRK;
 
 	/** The range that is searched for new machines */
 	private int autoSearchRange;
 
 	/** The max range that machines can be added at with the Internet Wand */
 	private int maxRange;
 
 	/** A list of the locations of machines that provide power to or consume power from this storage unit */
 	public final List<Point> connectedMachines = new ArrayList<Point>();
 
 	/** How many ticks have passed since the last search for machines. When this reaches {@link #SEARCH_INTERVAL the search interval time}, it resets to 0 and a
 	 * search begins. */
 	private int currentSearchTicks;
 
 	/** The last point that was checked for a machine in the previous iteration of {@link #search}. The coords are relative to this TE block. */
 	private Point lastSearch;
 
 	/** Should searching continue, or is it complete. Set this to true to begin a search. */
 	public boolean shouldSearch;
 
 	public TEMEnergyStorage(byte front) {
 		this();
 		this.front = front;
 	}
 
 	public TEMEnergyStorage() {
 		super(1);
 	}
 
 	@Override
 	public int getID() {
 		return MachineHelper.ENERGY_STORAGE;
 	}
 
 	@Override
 	public void updateEntity() {
 		super.updateEntity();
 
 		// Increment the ticks since last search and if it reaches the interval, reset it and run a search
 		if(++currentSearchTicks >= SEARCH_INTERVAL) {
 			currentSearchTicks = 0;
 			shouldSearch = true;
 		}
 
 		// Run a search
 		if(shouldSearch && Funcs.isServer())
 			search();
 
 		// If a connected machine no longer exists, remove it from the network
 		for(Iterator iterator = connectedMachines.iterator(); iterator.hasNext();) {
 			Point p = (Point)iterator.next();
 			if(worldObj.getBlockTileEntity(p.x, p.y, p.z) == null || !(worldObj.getBlockTileEntity(p.x, p.y, p.z) instanceof TileEntityElectric))
 				iterator.remove();
 		}
 	}
 
 	@Override
 	public void readFromNBT(NBTTagCompound tagCompound) {
 		super.readFromNBT(tagCompound);
 		currentRK = tagCompound.getInteger("currentRK");
 	}
 
 	@Override
 	public void writeToNBT(NBTTagCompound tagCompound) {
 		super.writeToNBT(tagCompound);
 		tagCompound.setInteger("currentRK", currentRK);
 	}
 
 	@Override
 	public Object[] getSyncDataToClient() {
 		List<Object> coords = new ArrayList<Object>();
 		for(Point point : connectedMachines) {
 			coords.add(point.x);
 			coords.add((short)point.y);
 			coords.add(point.z);
 		}
		return ArrayUtils.addAll(super.getSyncDataToClient(), (byte)connectedMachines.size(), coords.toArray());
 	}
 
 	public void handlePacketDataFromServer(int currentRK) {
 		this.currentRK = currentRK;
 	}
 
 	/** Perform a search for machines that produce/consume power. This checks {@link infinitealloys.util.MachineHelper#SEARCH_PER_TICK a set amount of} blocks in
 	 * a tick, then saves its place and picks up where it left off next tick, which eliminates stutter during searches. */
 	private void search() {
 		// The amount of blocks that have been iterated over this tick. When this reaches TEHelper.SEARCH_PER_TICK, the loops break
 		int blocksSearched = 0;
 
 		// Iterate over each block that is within the given range in all three dimensions. The searched area will be a cube with each side being (2 * range + 1)
 		// blocks long.
 		for(int x = lastSearch.x; x <= autoSearchRange; x++) {
 			for(int y = lastSearch.y; y <= autoSearchRange; y++) {
 				for(int z = lastSearch.z; z <= autoSearchRange; z++) {
 
 					// If the block at the given coords (which have been converted to absolute coordinates) is a machine and it is not already connected to a
 					// energy storage unit, add it to the power network.
 					TileEntity te = worldObj.getBlockTileEntity(xCoord + x, yCoord + y, zCoord + z);
 					if(te instanceof TileEntityElectric && ((TileEntityElectric)te).energyStorage == null) {
 						connectedMachines.add(new Point(xCoord + x, yCoord + y, zCoord + z));
						((TileEntityElectric)te).energyStorage = this;
 					}
 
 					// If the amounts of blocks search this tick has reached the limit, save our place and end the function. The search will be
 					// continued next tick.
 					if(++blocksSearched >= MachineHelper.SEARCH_PER_TICK) {
 						lastSearch.set(x, y, z);
 						return;
 					}
 				}
 				lastSearch.z = -autoSearchRange; // If we've search all the z values, reset the z position.
 			}
 			lastSearch.y = -autoSearchRange; // If we've search all the y values, reset the y position.
 		}
 		lastSearch.x = -autoSearchRange; // If we've search all the x values, reset the x position.
 
 		PacketDispatcher.sendPacketToAllPlayers(getDescriptionPacket());
 		shouldSearch = false; // The search is done. Stop running the function until another search is initiated.
 	}
 
 	public boolean addMachine(EntityPlayer player, int temX, int temY, int temZ) {
 		for(Point coords : connectedMachines) {
 			if(coords.x == temX && coords.y == temY && coords.z == temZ) {
 				if(worldObj.isRemote)
 					player.addChatMessage("Error: Machine is already in network");
 				return false;
 			}
 		}
 		if(worldObj.getBlockTileEntity(temX, temY, temZ) == null || !(worldObj.getBlockTileEntity(temX, temY, temZ) instanceof TileEntityElectric)) {
 			if(worldObj.isRemote)
 				player.addChatMessage("Error: Only machines can be powered");
 		}
 		else if(temX == xCoord && temY == yCoord && temZ == zCoord) {
 			if(worldObj.isRemote)
 				player.addChatMessage("Error: Cannot add self to network");
 		}
 		else if(new Point(temX, temY, temZ).distanceTo(xCoord, yCoord, zCoord) > maxRange) {
 			if(worldObj.isRemote)
 				player.addChatMessage("Error: Machine out of range");
 		}
 		else if(!((TileEntityMachine)worldObj.getBlockTileEntity(temX, temY, temZ)).hasUpgrade(MachineHelper.WIRELESS)) {
 			if(worldObj.isRemote)
 				player.addChatMessage("Error: Block does not have a networking upgrade");
 		}
 		else {
 			connectedMachines.add(new Point(temX, temY, temZ));
 			return true;
 		}
 		return false;
 	}
 
 	/** Will the unit support the specified change in RK, i.e. if changeInRK is added to currentRK, will the result be less than zero or overflow the machine? If
 	 * this condition is true, make said change, i.e. actually add changeInRK to currentRK
 	 * 
 	 * @param changeInRK the specified change in RK
 	 * @return True if changeInRK plus currentRK is between 0 and maxRK, False otherwise */
 	public boolean changeRK(int changeInRK) {
 		if(0 <= currentRK + changeInRK && currentRK + changeInRK <= maxRK) {
 			currentRK += changeInRK;
 			return true;
 		}
 		return false;
 	}
 
 	public int getCurrentRK() {
 		return currentRK;
 	}
 
 	public int getCurrentRKScaled(int scale) {
 		return currentRK * scale / maxRK;
 	}
 
 	public int getMaxRK() {
 		return maxRK;
 	}
 
 	@Override
 	protected void updateUpgrades() {
 		if(hasUpgrade(MachineHelper.CAPACITY2))
 			maxRK = 400000000; // 400,000,000 (400 million)
 		else if(hasUpgrade(MachineHelper.CAPACITY1))
 			maxRK = 200000000; // 200,000,000 (200 million)
 		else
 			maxRK = 100000000; // 100,000,000 (100 million)
 
 		if(hasUpgrade(MachineHelper.RANGE2)) {
 			autoSearchRange = 20;
 			maxRange = 60;
 		}
 		else if(hasUpgrade(MachineHelper.RANGE1)) {
 			autoSearchRange = 15;
 			maxRange = 45;
 		}
 		else {
 			autoSearchRange = 10;
 			maxRange = 30;
 		}
 		if(lastSearch == null)
 			lastSearch = new Point(-autoSearchRange, 0, -autoSearchRange);
 		else
 			lastSearch.set(-autoSearchRange, 0, -autoSearchRange);
 	}
 
 	@Override
 	protected void populateValidUpgrades() {
 		validUpgrades.add(MachineHelper.CAPACITY1);
 		validUpgrades.add(MachineHelper.CAPACITY2);
 		validUpgrades.add(MachineHelper.RANGE1);
 		validUpgrades.add(MachineHelper.RANGE2);
 	}
 }
