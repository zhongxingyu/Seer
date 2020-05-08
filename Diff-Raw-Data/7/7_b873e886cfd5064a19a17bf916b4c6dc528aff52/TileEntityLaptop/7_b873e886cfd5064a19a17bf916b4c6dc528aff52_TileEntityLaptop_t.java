 package cortexmodders.atomtech.tileentity;
 
 import cortexmodders.atomtech.blocks.BlockLaptop;
 import cortexmodders.atomtech.blocks.ModBlocks;
 import cortexmodders.atomtech.item.ModItems;
 import net.minecraft.entity.item.EntityItem;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.inventory.IInventory;
 import net.minecraft.item.ItemStack;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.nbt.NBTTagList;
 import net.minecraft.network.INetworkManager;
 import net.minecraft.network.packet.Packet;
 import net.minecraft.network.packet.Packet132TileEntityData;
 import net.minecraft.util.AxisAlignedBB;
 
 public class TileEntityLaptop extends TilePoweredBase implements IInventory
 {
     
     public static AxisAlignedBB test = AxisAlignedBB.getAABBPool().getAABB( 1, 0, 0.3125, 1, 0.125, 0.375);
     
     private byte data = 0b100;
     private ItemStack[] inv;
     
     private float lidAngleX = -180.0F;
     public static final float LID_ANGLE_OPEN = -276.0F;
     public static final float LID_ANGLE_CLOSED = -180.0F;
     
     // local coords
     public final static AxisAlignedBB flashDriveHitPosition = AxisAlignedBB.getAABBPool().getAABB(1, 0, 0.3125, 1, 0.125, 0.375);
     
     public TileEntityLaptop()
     {
         super(20);
         inv = new ItemStack[1];
     }
     
     @Override
     public void updateEntity()
     {
     	if(!worldObj.isRemote)
     	{
     		if(worldObj.getBlockId(xCoord, yCoord, zCoord) != ModBlocks.laptop.blockID)
     		{
     			worldObj.removeBlockTileEntity(xCoord, yCoord, zCoord);
     		}
     	}
     	if(!isBroken())
     	{
    		if(isLidClosed() && lidAngleX != LID_ANGLE_CLOSED)
     		{
     			lidAngleX += 4.0F;
     		}
    		else if(!isLidClosed() && lidAngleX != LID_ANGLE_OPEN)
     		{
     			lidAngleX -= 4.0F;
     		}
     	}
     	else
     	{
     		if(hasFlashDrive() && !worldObj.isRemote)
     		{
     			BlockLaptop.ejectFlashDrive(worldObj, xCoord, yCoord, zCoord);
     		}
     	}
     }
     
     public float getLidAngle() {
         return this.lidAngleX;
     }
     
     @Override
     public Packet getDescriptionPacket()
     {
         NBTTagCompound tag = new NBTTagCompound();
         this.writeToNBT(tag);
         return new Packet132TileEntityData(xCoord, yCoord, zCoord, 0, tag);
     }
     
     @Override
     public void onDataPacket(INetworkManager net, Packet132TileEntityData pkt)
     {
         NBTTagCompound tag = pkt.customParam1;
         this.readFromNBT(tag);
     }
     
     @Override
     public void readFromNBT(NBTTagCompound tag)
     {
         super.readFromNBT(tag);
         NBTTagList tagList = tag.getTagList("Inventory");
 		for (int i = 0; i < tagList.tagCount(); i++) {
 			NBTTagCompound tag2 = (NBTTagCompound) tagList.tagAt(i);
 			byte slot = tag2.getByte("Slot");
 			if (slot >= 0 && slot < inv.length) {
 				inv[slot] = ItemStack.loadItemStackFromNBT(tag2);
 			}
 		}
         data = tag.getByte("data");
         lidAngleX = tag.getFloat("lidAngle");
     }
     
     @Override
     public void writeToNBT(NBTTagCompound tag)
     {
         super.writeToNBT(tag);
         tag.setByte("data", data);
         tag.setFloat("lidAngle", lidAngleX);
 		NBTTagList itemList = new NBTTagList();
 		for (int i = 0; i < inv.length; i++) {
 			ItemStack stack = inv[i];
 			if (stack != null) {
 				NBTTagCompound tag2 = new NBTTagCompound();
 				tag2.setByte("Slot", (byte) i);
 				stack.writeToNBT(tag2);
 				itemList.appendTag(tag2);
 			}
 		}
 		tag.setTag("Inventory", itemList);
     }
     
     public boolean isLidClosed()
     {
     	return (data & 0b100) != 0;
     }
     
     public void setLidClosed(boolean closeLid) 
     {
     	if(closeLid)
     	{
     		data |= 0b100;
     	}
     	else
     	{
     		data &= ~0b100;
     	}
     }
     
     public void toggleLid()
     {
         setLidClosed(!isLidClosed());
     }
     
     /**
      * condition of laptop. 0 = best, 1 = ok, 2 = broken.
      */
     public byte getCondition()
     {
     	return (byte) (data & 0b11);
     }
     
     public void setCondition(byte condition)
     {
     	data &= ~0b11;
     	data |= condition & 0b11;
     }
     
     /**
      * makes the laptop more broken.
      * 
      */
     public void degradeCondition()
     {
         if(getCondition() < 2)
         	data++;
     }
     
     /**
      * returns if laptop is broken or not.
      * 
      * @return
      */
     public boolean isBroken()
     {
         return (data & 0b11) >= 2;
     }
     
     public boolean hasFlashDrive()
     {
     	return isItemValidForSlot(0, getStackInSlot(0));
     }
     
     public byte getData()
     {
     	return data;
     }
     
     public void setData(byte data)
     {
     	this.data = data;
     }
     
     public void fix()
     {
     	data &= ~0b11;
     }
     
     @Override
     public boolean canRecievePower()
     {
         return !isBroken();
     }
 	
 	@Override
 	public int getSizeInventory() {
 		return inv.length;
 	}
 	
 	@Override
 	public ItemStack getStackInSlot(int slot) {
 		return inv[slot];
 	}
 	
 	@Override
 	public void setInventorySlotContents(int slot, ItemStack stack) {
 		inv[slot] = stack;
 		if (stack != null && stack.stackSize > getInventoryStackLimit()) {
 			stack.stackSize = getInventoryStackLimit();
 		}
 	}
 	
 	@Override
 	public ItemStack decrStackSize(int slot, int amt) {
 		ItemStack stack = getStackInSlot(slot);
 		if (stack != null)
 		{
 			if (stack.stackSize <= amt)
 			{
 				setInventorySlotContents(slot, null);
 			}
 			else
 			{
 				stack = stack.splitStack(amt);
 				if (stack.stackSize == 0)
 				{
 					setInventorySlotContents(slot, null);
 				}
 			}
 		}
 		return stack;
 	}
 	
 	@Override
 	public ItemStack getStackInSlotOnClosing(int slot) {
 		ItemStack stack = getStackInSlot(slot);
 		if (stack != null) {
 			setInventorySlotContents(slot, null);
 		}
 		return stack;
 	}
 	
 	@Override
 	public int getInventoryStackLimit() {
 		return 1;
 	}
 	
 	@Override
 	public boolean isUseableByPlayer(EntityPlayer player) {
 		return worldObj.getBlockTileEntity(xCoord, yCoord, zCoord) == this &&
 				player.getDistanceSq(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5) < 64;
 	}
 	
 	@Override
 	public void openChest() {}
 	
 	@Override
 	public void closeChest() {}
 	
 	@Override
 	public String getInvName()
 	{
 		return "tileentitylaptop";
 	}
 	
 	@Override
 	public boolean isInvNameLocalized()
 	{
 		return false;
 	}
 	
 	@Override
 	public boolean isItemValidForSlot(int slot, ItemStack stack)
 	{
 		if(stack != null && stack.getItem().equals(ModItems.flashDrive) && slot == 0)
 		{
 			return true;
 		}
 		return false;
 	}
 	
 	public static AxisAlignedBB getFlashDriveBoundingBoxByMeta(int meta) {
 	    AxisAlignedBB box = getBoundingBox();
 	    switch(meta) {
 	    default: return AxisAlignedBB.getAABBPool().getAABB( box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ);
 	    case 1: return AxisAlignedBB.getAABBPool().getAABB( 0, box.minY, box.minZ, 0, box.maxY, box.maxZ);
 	    case 2: return AxisAlignedBB.getAABBPool().getAABB( box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ);
 	    case 3: return AxisAlignedBB.getAABBPool().getAABB( box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ);
 	    }
 	}
 	
 	public static AxisAlignedBB getBoundingBox() {
 	    return AxisAlignedBB.getAABBPool().getAABB( 1, 0, 0.3125, 1, 0.125, 0.375);
 	}
 }
