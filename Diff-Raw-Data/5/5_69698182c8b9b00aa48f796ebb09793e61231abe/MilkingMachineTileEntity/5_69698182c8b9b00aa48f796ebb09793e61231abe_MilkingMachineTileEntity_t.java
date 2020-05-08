 package biotech.tileentity;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import liquidmechanics.api.IColorCoded;
 import liquidmechanics.api.IReadOut;
 import liquidmechanics.api.helpers.ColorCode;
 import liquidmechanics.api.liquids.IPressure;
 import liquidmechanics.api.liquids.LiquidData;
 import liquidmechanics.api.liquids.LiquidHandler;
 import net.minecraft.entity.EntityLiving;
 import net.minecraft.entity.passive.EntityCow;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.ItemStack;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.nbt.NBTTagList;
 import net.minecraft.network.INetworkManager;
 import net.minecraft.network.packet.Packet;
 import net.minecraft.network.packet.Packet250CustomPayload;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.util.AxisAlignedBB;
 import net.minecraftforge.common.ForgeDirection;
 import net.minecraftforge.liquids.ITankContainer;
 import net.minecraftforge.liquids.LiquidContainerRegistry;
 import universalelectricity.core.UniversalElectricity;
 import universalelectricity.core.electricity.ElectricityNetwork;
 import universalelectricity.core.electricity.ElectricityPack;
 import universalelectricity.core.implement.IItemElectric;
 import universalelectricity.core.vector.Vector3;
 import universalelectricity.prefab.network.IPacketReceiver;
 import universalelectricity.prefab.network.PacketManager;
 import biotech.Biotech;
 
 import com.google.common.io.ByteArrayDataInput;
 
 public class MilkingMachineTileEntity extends BasicMachineTileEntity implements IPacketReceiver, IColorCoded, IPressure, IReadOut
 {
 	protected List<EntityCow> CowList = new ArrayList<EntityCow>();
 
 	// Watts being used per action / idle action
 	public static final double WATTS_PER_TICK = 25;
 	public static final double WATTS_PER_IDLE_TICK = 2.5;
 
 	// Time idle after a tick
 	public static final int IDLE_TIME_AFTER_ACTION = 60;
 	public static final int IDLE_TIME_NO_ACTION = 30;
 
 	// How much power is stored?
 	private double electricityStored = 0;
 	private double electricityMaxStored = 5000;
 
 	private boolean isMilking = false;
 
 	// Is the machine currently powered, and did it change?
 	public boolean prevIsPowered, isPowered = false;
 
 	// Amount of milliBuckets of internal storage
 	private ColorCode color = ColorCode.WHITE;
 
 	private static final int milkMaxStored = 3 * LiquidContainerRegistry.BUCKET_VOLUME;
 
 	private int milkStored = 0;
 
 	private int facing;
 	private int playersUsing = 0;
 	private int idleTicks;
 
 	@Override
 	public void updateEntity()
 	{
 		super.updateEntity();
 		if (!worldObj.isRemote && !this.HasRedstoneSignal())
 		{
 			/* Per Tick Processes */
 			this.setPowered(true);
 			this.chargeUp();
 			this.drainTo(ForgeDirection.DOWN);
 
 			/* SCAN FOR COWS */
 			if (this.ticks % 40 == 0)
 			{
 				scanForCows();
 			}
 
 			/* Milk Cows */
 			if (this.ticks % 100 == 0)
 			{
 				milkCows();
 				this.setPowered(false);
 			}
 
 			/* Update Client */
 			if (this.playersUsing > 0 && this.ticks % 3 == 0)
 			{
 				PacketManager.sendPacketToClients(getDescriptionPacket(), this.worldObj, new Vector3(this), 12);
 			}
 		}
 
 	}
 
 	/**
 	 * Scans for cows for milking
 	 */
 	public void scanForCows()
 	{
 		AxisAlignedBB searchBox = AxisAlignedBB.getBoundingBox(xCoord, yCoord, zCoord, xCoord, yCoord, zCoord).expand(this.getMilkRange(), this.getMilkRange(), this.getMilkRange());
 		this.CowList.clear();
 		this.CowList.addAll(worldObj.getEntitiesWithinAABB(EntityCow.class, searchBox));
 	}
 
 	public void milkCows()
 	{
 		if (CowList.size() != 0 && this.getMilkStored() < this.getMaxMilk())
 		{
 			CowList.remove(0);
 			this.setMilkStored(250, true);
 			this.setElectricityStored(this.electricityStored -= this.WATTS_PER_TICK);
 		}
 	}
 
 	public int getMilkRange()
 	{
 		return 3;
 	}
 
 	public int getScanRange()
 	{
 		if (getStackInSlot(1) != null)
 		{
 			if (inventory[1].isItemEqual(Biotech.bioCircuitRangeUpgrade)) { return (getStackInSlot(1).stackSize + 5); }
 		}
 		return 3;
 	}
 
 	/**
 	 * Drains the contents of the internal tank to a block bellow it
 	 */
 	public void drainTo(ForgeDirection dir)
 	{
		TileEntity ent = worldObj.getBlockTileEntity(xCoord + dir.offsetX, yCoord + dir.offsetY, zCoord + dir.offsetZ);
 		if (ent instanceof ITankContainer)
 		{
 			int filled = ((ITankContainer) ent).fill(dir.getOpposite(), LiquidHandler.getStack(color.getLiquidData(), this.milkStored), true);
 			if (filled > 0)
 			{
 				this.setMilkStored(-filled, true);
 			}
 		}
 	}
 
 	/**
 	 * gets if this block is getting powered by redstone
 	 */
 	public boolean HasRedstoneSignal()
 	{
 		if (worldObj.isBlockGettingPowered(xCoord, yCoord, zCoord) || worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord)) { return true; }
 		return false;
 	}
 
 	@Override
 	public void readFromNBT(NBTTagCompound tagCompound)
 	{
 		super.readFromNBT(tagCompound);
 		// vars
 		this.facing = tagCompound.getShort("facing");
 		this.electricityStored = tagCompound.getDouble("electricityStored");
 		this.milkStored = tagCompound.getInteger("milkStored");
 
 		// inventory
 		NBTTagList tagList = tagCompound.getTagList("Inventory");
 		for (int i = 0; i < tagList.tagCount(); i++)
 		{
 			NBTTagCompound tag = (NBTTagCompound) tagList.tagAt(i);
 			byte slot = tag.getByte("Slot");
 
 			if (slot >= 0 && slot < inventory.length)
 			{
 				inventory[slot] = ItemStack.loadItemStackFromNBT(tag);
 			}
 		}
 	}
 
 	@Override
 	public void writeToNBT(NBTTagCompound tagCompound)
 	{
 		super.writeToNBT(tagCompound);
 		// vars
 		tagCompound.setShort("facing", (short) this.facing);
 		tagCompound.setDouble("electricityStored", this.electricityStored);
 		tagCompound.setInteger("milkStored", this.milkStored);
 
 		// inventory
 		NBTTagList itemList = new NBTTagList();
 		for (int i = 0; i < inventory.length; i++)
 		{
 			ItemStack stack = inventory[i];
 
 			if (stack != null)
 			{
 				NBTTagCompound tag = new NBTTagCompound();
 				tag.setByte("Slot", (byte) i);
 				stack.writeToNBT(tag);
 				itemList.appendTag(tag);
 			}
 		}
 		tagCompound.setTag("Inventory", itemList);
 	}
 
 	@Override
 	public String getInvName()
 	{
 		return "Milking Machine";
 	}
 
 	@Override
 	public void handlePacketData(INetworkManager network, int packetType, Packet250CustomPayload packet, EntityPlayer player, ByteArrayDataInput dataStream)
 	{
 		try
 		{
 			if (this.worldObj.isRemote)
 			{
 				this.isPowered = dataStream.readBoolean();
 				this.facing = dataStream.readInt();
 				this.electricityStored = dataStream.readDouble();
 				this.milkStored = dataStream.readInt();
 			}
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public Packet getDescriptionPacket()
 	{
 		return PacketManager.getPacket(Biotech.CHANNEL, this, this.isPowered, this.facing, this.electricityStored, this.milkStored);
 	}
 
 	public int getFacing()
 	{
 		return facing;
 	}
 
 	public void setFacing(int facing)
 	{
 		this.facing = facing;
 	}
 
 	public double getElectricityStored()
 	{
 		return electricityStored;
 	}
 
 	public void setElectricityStored(double joules)
 	{
 		electricityStored = Math.max(Math.min(joules, getMaxElectricity()), 0);
 	}
 
 	public double getMaxElectricity()
 	{
 		return electricityMaxStored;
 	}
 
 	/**
 	 * Sets the current volume of milk stored
 	 * 
 	 * @param amount - volume sum
 	 * @param add - if true it will add the amount to the current sum
 	 */
 	public void setMilkStored(int amount, boolean add)
 	{
 		if (add)
 		{
 			this.milkStored = Math.max(this.milkMaxStored, Math.min(0, this.milkStored + amount));
 		}
 		else
 		{
 			this.milkStored = Math.max(this.milkMaxStored, Math.min(0, amount));
 		}
 	}
 
 	public int getMilkStored()
 	{
 		return this.milkStored;
 	}
 
 	public int getMaxMilk()
 	{
 		return this.milkMaxStored;
 	}
 
 	@Override
 	public ColorCode getColor()
 	{
 		return ColorCode.WHITE;
 	}
 
 	@Override
 	public void setColor(Object obj)
 	{
 	}
 
 	@Override
 	public int presureOutput(LiquidData type, ForgeDirection dir)
 	{
 		return ((type.getColor() == color || type.getColor() == ColorCode.NONE) ? type.getPressure() : 0);
 	}
 
 	@Override
 	public boolean canPressureToo(LiquidData type, ForgeDirection dir)
 	{
 		return ((type.getColor() == color || type.getColor() == ColorCode.NONE) && dir == ForgeDirection.DOWN.getOpposite());
 	}
 
 	@Override
 	public String getMeterReading(EntityPlayer user, ForgeDirection side)
 	{
 		return "Milk: " + this.milkStored + " Units";
 	}
 }
