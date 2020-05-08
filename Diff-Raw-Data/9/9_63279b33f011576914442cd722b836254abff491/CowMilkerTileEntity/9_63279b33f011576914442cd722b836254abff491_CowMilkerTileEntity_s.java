 package gigaherz.biotech.tileentity;
 
 import gigaherz.biotech.Biotech;
 
 import java.util.ArrayList;
 import java.util.EnumSet;
 import java.util.List;
 import java.util.Random;
 
 import com.google.common.io.ByteArrayDataInput;
 
 import net.minecraft.entity.EntityLiving;
 import net.minecraft.entity.passive.EntityCow;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.inventory.IInventory;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemBlock;
 import net.minecraft.item.ItemStack;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.nbt.NBTTagList;
 import net.minecraft.network.INetworkManager;
 import net.minecraft.network.packet.Packet;
 import net.minecraft.network.packet.Packet250CustomPayload;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.util.AxisAlignedBB;
 import net.minecraft.world.World;
 import net.minecraftforge.common.ForgeDirection;
 import net.minecraftforge.common.ISidedInventory;
 import universalelectricity.core.electricity.ElectricityConnections;
 import universalelectricity.core.electricity.ElectricityNetwork;
 import universalelectricity.core.implement.IConductor;
 import universalelectricity.core.implement.IItemElectric;
 import universalelectricity.core.vector.Vector3;
 import universalelectricity.prefab.network.IPacketReceiver;
 import universalelectricity.prefab.network.PacketManager;
 import universalelectricity.prefab.tile.TileEntityElectricityReceiver;
 
 public class CowMilkerTileEntity extends BasicMachineTileEntity implements IInventory, ISidedInventory, IPacketReceiver
 {
 	private int tickCounter;
 	private int scantickCounter;
 	
 	protected List<EntityLiving> CowList = new ArrayList<EntityLiving>();
 	
 	// Watts being used per action / idle action
 	public static final double WATTS_PER_ACTION = 1500;
 	public static final double WATTS_PER_IDLE_ACTION = 500;
 	
 	// Watts being used per pump action
 	public static final double WATTS_PER_PUMP_ACTION = 250;
 	
 	// Time idle after a tick
 	public static final int IDLE_TIME_AFTER_ACTION = 80;
 	public static final int IDLE_TIME_NO_ACTION = 40;
 
     //How much milk is stored?
    public static int milkStored = 0;
    public static int milkMaxStored = 3000;
    private static int cowMilk = 10;
     
    public static boolean bucketIn = false;
     
     //Is the machine currently powered, and did it change?
     public boolean prevIsPowered, isPowered = false;
 
 	/**
 	 * The ItemStacks that hold the items currently being used in the cow milker
 	 */
 	private ItemStack[] lefttopSlot = new ItemStack[4];
 	private Item[] leftbottomSlot = new Item[1];
 
 	private int facing;
 	private int playersUsing = 0;
 	private int idleTicks;
 	
     public int currentX = 0;
     public int currentZ = 0;
     public int currentY = 0;
 	
     public int minX, maxX;
     public int minZ, maxZ;
 	
 	
 	public CowMilkerTileEntity()
 	{
 		super();
 	}
 	
 	@Override
     public void refreshConnectorsAndWorkArea()
     {
     	super.refreshConnectorsAndWorkArea();
     	
     	ForgeDirection direction = ForgeDirection.getOrientation(getFacing());
     	
         if (direction.offsetZ > 0)
         {
             this.minX = -2;
             this.maxX =  2;
             this.minZ = -5 * direction.offsetZ;
             this.maxZ = -1 * direction.offsetZ;
         }
         else if (direction.offsetZ < 0)
         {
             this.minX = -2;
             this.maxX =  2;
             this.minZ = -1 * direction.offsetZ;
             this.maxZ = -5 * direction.offsetZ;
         }
         else if (direction.offsetX > 0)
         {
             this.minZ = -2;
             this.maxZ =  2;
             this.minX = -5 * direction.offsetX;
             this.maxX = -1 * direction.offsetX;
         }
         else if (direction.offsetX < 0)
         {
             this.minZ = -2;
             this.maxZ =  2;
             this.minX = -1 * direction.offsetX;
             this.maxX = -5 * direction.offsetX;
         }
 
         if (this.currentX < this.minX || this.currentX > this.maxX)
         {
             this.currentX = this.minX;
         }
 
         if (this.currentZ < this.minZ || this.currentZ > this.maxZ)
         {
             this.currentZ = this.minZ;
         }
     }
 
 	public void scanCows()
 	{
 		int xminrange = xCoord- getScanRange();
 		int xmaxrange = xCoord+ getScanRange()+1;
 		int yminrange = yCoord- getScanRange();
 		int ymaxrange = yCoord+ getScanRange()+1;
 		int zminrange = zCoord- getScanRange();
 		int zmaxrange = zCoord+ getScanRange()+1;
 		
 		List<EntityCow> scannedCowslist = worldObj.getEntitiesWithinAABB(EntityCow.class, AxisAlignedBB.getBoundingBox(xminrange, yminrange, zminrange, xmaxrange, ymaxrange, zmaxrange));
 	
 		for(EntityCow Living : scannedCowslist)
 		{
 			if(!CowList.contains(Living))
 			{
 				CowList.add(Living);
 			}
 		}	
 	}
 	
 	public void milkCows()
 	{		
 		
 		if(this.getElectricityStored() >= WATTS_PER_ACTION)
 		{
 			milkStored += cowMilk;
 			if(CowList.size() != 0)
 			{
 				CowList.remove(0);
 				cowMilk = 10;
 	        	this.setElectricityStored(this.getElectricityStored() - this.WATTS_PER_ACTION);
 			}
 		}
 		
 		
 	}
 	
 	public int getScanRange() {
 		if (getStackInSlot(1) != null) 
 		{
 			if (getStackInSlot(1).getItem() == Biotech.rangeUpgrade) 
 			{
 				return (getStackInSlot(1).stackSize+5);
 			}
 		}
 		return 3;
 	}
 	
 	@Override
     public void updateEntity()
     {
         if (!worldObj.isRemote)
         {
 	        if(this.idleTicks > 0)
 	        {
 	            if (this.ticks % 40 == 0)
 	            	this.setElectricityStored(this.getElectricityStored() - this.WATTS_PER_IDLE_ACTION);
 	        	
 	        	--this.idleTicks;
 	        	return;
 	        }
 	        
 	        if(this.isRedstoneSignal())
 	        {
 	        	this.setPowered(true);
 	        	if(scantickCounter >= 40)
 	        	{
 	        		scanCows();
 	        		scantickCounter = 0;
 	        	}
 	        	if(CowList.size() != 0 && tickCounter >= 100)
 	        	{
 	        		milkCows();
 	        		tickCounter = 0;
 	        		this.idleTicks = this.IDLE_TIME_AFTER_ACTION;
 	        		this.setPowered(false);
 	        	}
 	            tickCounter++;
 	            scantickCounter++;
 	        }
 	        if(milkStored >= milkMaxStored)
 	        {
 	        	milkStored = milkMaxStored;
 	        }
 	        if(tickCounter >= 150)
 	        {
 	        	tickCounter = 0;
 	        }
 	        if(scantickCounter >= 100)
 	        {
 	        	tickCounter = 0;
 	        }	        
         }
         super.updateEntity();
     }
 	
 	public boolean isRedstoneSignal(){
 		if(worldObj.isBlockGettingPowered(xCoord,yCoord, zCoord) ||
 		    worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord))
 			return true;
 		return false;
 	}
 
 	@Override
     public void readFromNBT(NBTTagCompound tagCompound)
     {
         super.readFromNBT(tagCompound);
         //this.progressTime = tagCompound.getShort("Progress");
         this.milkStored = tagCompound.getInteger("milkStored");        
     }
 
     @Override
     public void writeToNBT(NBTTagCompound tagCompound)
     {
         super.writeToNBT(tagCompound);
         //tagCompound.setShort("Progress", (short)this.progressTime);
         tagCompound.setInteger("milkStored", (int)this.milkStored);
     }
 
     @Override
     public String getInvName()
     {
         return "Cow Milker";
     }
 }
