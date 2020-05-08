 package gieraffe.bdc.tile;
 
 import gieraffe.bdc.lib.BlockIDs;
 import gieraffe.bdc.lib.Channels;
 import gieraffe.bdc.lib.PacketData;
 import gieraffe.bdc.network.CustomBDCPacket;
 import cpw.mods.fml.common.FMLCommonHandler;
 import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
 import cpw.mods.fml.relauncher.Side;
 import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
 import net.minecraft.inventory.IInventory;
 import net.minecraft.item.ItemStack;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.nbt.NBTTagList;
 import net.minecraft.tileentity.TileEntity;
 
 
 public class TileDetector extends TileEntity implements IInventory {
 
 	private ItemStack[] detectorItemStacks = new ItemStack[4];
 	
 	private String inventoryName;
 	
 	private boolean powerState = false;		// machine status, false for now
 	   
 	
 	/**
 	 * Handle button click, call corresponding method(s). Gets called from PacketHandler
 	 * @param buttonID ID of the button that the player clicked
 	 * @param data message assigned to this action
 	 */
 	public void buttonClicked(int[] message) {
 
 		if (message.length < 1)
 			return;
 					
 		switch (message[0]) {
 		case (PacketData.BUTTON_POWER_CLICKED):
 			buttonPowerSwitchClicked();
 			break;
 		}
 	}
 	
 	public void buttonPowerSwitchClicked() {
 		/** check if all preconditions are met for turning on the machine */
 		// TODO: add stuff
 		
 		switchPowerState();		
 	}
 	
 	/**
 	 * switch PowerState and notify client if on the server
 	 */
 	private void switchPowerState() {
 		
 		// create message that will be sent to the client, later add the state it has to switch to
 		int[] message = new int[2];
 		message[0] = PacketData.CHANGE_POWER_STATE;
 		
 		// do the 'ol switcheroo
 		if (this.powerState) {
 			this.powerState = false;
 			message[1] = PacketData.POWER_STATE_OFF;
 		}
 		else {
			this.powerState = false;
 			message[1] = PacketData.POWER_STATE_ON;
 		}
 		
 		
 		//create & send package to client
     	CustomBDCPacket packet = new CustomBDCPacket(Channels.CHANNEL_DETECTOR_CLIENT, BlockIDs.BLOCK_DETECTOR, 
     			 										this.xCoord, this.yCoord, this.zCoord, message);
    	PacketDispatcher.sendPacketToPlayer(packet.getPacket(), );
 
 	}
 	
 	/**
 	 * Manual override Power State, used by PacketHandler.java. Works only on client side (only used for graphic display)
 	 * @param state new Power State
 	 */
 	public void setPowerState(boolean state) {
 		Side side = FMLCommonHandler.instance().getEffectiveSide();
 		if (side == Side.CLIENT)
 			this.powerState = state;		
 	}
 	
 	@Override
     public boolean isUseableByPlayer(EntityPlayer player) {
     	/** same as in minecraft source */
     	if (this.worldObj.getBlockTileEntity(this.xCoord, this.yCoord, this.zCoord) == this)
     			return player.getDistanceSq((double)this.xCoord + 0.5D, (double)this.yCoord + 0.5D, (double)this.zCoord + 0.5D) <= 64.0D;
     	return false;
     }
 	
 	
     /** returns the number of slots in the inventory */
 	@Override
 	public int getSizeInventory() {
 		return this.detectorItemStacks.length;
 	}
 	
 
 	/*
 	 * ItemStack handling
 	 */
 	@Override
 	public ItemStack getStackInSlot(int i) {
 		return this.detectorItemStacks[i];
 	}
 
 	/** i = slot, j = size */
 	@Override
 	public ItemStack decrStackSize(int i, int j) {
 		if (this.detectorItemStacks[i] != null) {
             ItemStack itemstack;
 
             if (this.detectorItemStacks[i].stackSize <= j) {
                 itemstack = this.detectorItemStacks[i];
                 this.detectorItemStacks[i] = null;
                 return itemstack;
             }
             else {
                 itemstack = this.detectorItemStacks[i].splitStack(j);
 
                 if (this.detectorItemStacks[i].stackSize == 0)
                     this.detectorItemStacks[i] = null;
                 
                 return itemstack;
             }
         }
         else
         	return null;
 	}
 	
 	@Override
 	public ItemStack getStackInSlotOnClosing(int i) {
 		if (this.detectorItemStacks[i] != null) {
             ItemStack itemstack = this.detectorItemStacks[i];
             this.detectorItemStacks[i] = null;
             return itemstack;
         }
         else
             return null;
 	}
 
 	@Override
 	public void setInventorySlotContents(int i, ItemStack itemstack) {
 		this.detectorItemStacks[i] = itemstack;
 
         if (itemstack != null && itemstack.stackSize > this.getInventoryStackLimit())
         	itemstack.stackSize = this.getInventoryStackLimit();		
 	}
 
 	/**
 	 * IventoryName stuff
 	 */
 	public String getInvName() {
 		return this.isInvNameLocalized() ? this.inventoryName : "container.detector";
 	}
 
 	public boolean isInvNameLocalized() {
 		return this.inventoryName != null && this.inventoryName.length() > 0;
 	}
 	
 	public void setInventoryName(String par1Str)
     {
         this.inventoryName = par1Str;
     }
 
 	public void func_94047_a(String par1Str)
     {
         this.inventoryName = par1Str;
     }
 	
 	
 	@Override
 	public int getInventoryStackLimit() {
 		return 64;
 	}
 
 	@Override
 	public void openChest() {}
 
 	@Override
 	public void closeChest() {}
 	
 	@Override
 	public boolean isStackValidForSlot(int i, ItemStack itemstack) {
 		return true;
 	}
 
 	/**
 	 * NBT data handling routines
 	 */
 	@Override
 	public void writeToNBT(NBTTagCompound nbt) {
 		super.writeToNBT(nbt);
 		NBTTagList nbtTagList = new NBTTagList();
 		
 		for (int i = 0; i < this.detectorItemStacks.length; i++) 
 		{
 			if (this.detectorItemStacks[i] != null) 
 			{
 				NBTTagCompound nbttagcompound1 = new NBTTagCompound();
 				nbttagcompound1.setByte("Slot", (byte)i);
 				this.detectorItemStacks[i].writeToNBT(nbttagcompound1);
 				nbtTagList.appendTag(nbttagcompound1);	
 			}
 		}
 		
 	    nbt.setTag("Items", nbtTagList);
 	    
 	    nbt.setBoolean("PowerState", this.powerState);
 
 	    if (this.isInvNameLocalized())
 	        nbt.setString("CustomName", this.inventoryName);
 	}
 	
 	@Override
 	public void readFromNBT(NBTTagCompound nbt) {
         super.readFromNBT(nbt);
         NBTTagList nbtTagList = nbt.getTagList("Items");
         
         this.detectorItemStacks = new ItemStack[this.getSizeInventory()];
 
         if (nbt.hasKey("CustomName"))
             this.inventoryName = nbt.getString("CustomName");
         
         this.powerState = nbt.getBoolean("PowerState");
 
         for (int i = 0; i < nbtTagList.tagCount(); ++i)
         {
             NBTTagCompound nbttagcompound1 = (NBTTagCompound)nbtTagList.tagAt(i);
             byte b0 = nbttagcompound1.getByte("Slot");
 
             if (b0 >= 0 && b0 < this.detectorItemStacks.length) 
                 this.detectorItemStacks[b0] = ItemStack.loadItemStackFromNBT(nbttagcompound1);
         }
     }
 }
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
