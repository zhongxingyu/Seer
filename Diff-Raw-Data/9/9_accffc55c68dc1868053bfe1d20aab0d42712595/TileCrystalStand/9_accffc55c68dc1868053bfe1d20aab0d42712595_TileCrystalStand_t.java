 package Seremis.SoulCraft.tileentity;
 
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.inventory.IInventory;
 import net.minecraft.item.ItemStack;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.nbt.NBTTagList;
 import net.minecraft.network.INetworkManager;
 import net.minecraft.network.packet.Packet;
 import net.minecraft.network.packet.Packet132TileEntityData;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraftforge.common.ForgeDirection;
 import Seremis.SoulCraft.api.plasma.IPlasmaNetwork;
 import Seremis.SoulCraft.api.plasma.block.IPlasmaConnector;
 import Seremis.SoulCraft.block.ModBlocks;
 import Seremis.SoulCraft.items.ModItems;
 
 public class TileCrystalStand extends SCTileEntity implements IInventory, IPlasmaConnector {
 
 	private ItemStack[] inv = new ItemStack[1];
 	private IPlasmaNetwork network = null;
 	
 	@Override
 	public int getSizeInventory() {
 		return inv.length;
 	}
 
 	@Override
 	public ItemStack getStackInSlot(int slot) {
 		return inv[slot];
 	}
 
 	@Override
 	public ItemStack decrStackSize(int slot, int amount) {
 		ItemStack stack = getStackInSlot(slot);
 		if(stack != null)
 		{
 			if(stack.stackSize <= amount)
 			{
 				setInventorySlotContents(slot, null);
 			} 
 			else
 			{
 				stack = stack.splitStack(amount);
 				if(stack.stackSize <= 0)
 				{
 					setInventorySlotContents(slot, null);
 				}
 			}
 		}
 		return stack;
 	}
 
 	@Override
 	public ItemStack getStackInSlotOnClosing(int slot) {
 		return null;
 	}
 
 	@Override
 	public void setInventorySlotContents(int slot, ItemStack stack) {
 		inv[slot] = stack;
 		if(stack != null && stack.stackSize > getInventoryStackLimit()) {
 			stack.stackSize = getInventoryStackLimit();
 		}
 	}
 
 	@Override
 	public String getInvName() {
 		return "Crystal Stand";
 	}
 
 	@Override
 	public int getInventoryStackLimit() {
 		return 1;
 	}
 
 	@Override
 	public void openChest() {}
 
 	@Override
 	public void closeChest() {}
 	
 	@Override
 	public boolean isUseableByPlayer(EntityPlayer player) {
 		return worldObj.getBlockTileEntity(xCoord, yCoord, zCoord) == this && player.getDistanceSq(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5) < 64;
 	}
 	
 	public void readFromNBT(NBTTagCompound par1NBTTagCompound)
     {
         super.readFromNBT(par1NBTTagCompound);
         NBTTagList var2 = par1NBTTagCompound.getTagList("Items");
         this.inv = new ItemStack[this.getSizeInventory()];
 
         for (int var3 = 0; var3 < var2.tagCount(); ++var3)
         {
             NBTTagCompound var4 = (NBTTagCompound)var2.tagAt(var3);
             int var5 = var4.getByte("Slot") & 255;
 
             if (var5 >= 0 && var5 < this.inv.length)
             {
                 this.inv[var5] = ItemStack.loadItemStackFromNBT(var4);
             }
         }
     }
 
 	@Override
 	public void writeToNBT(NBTTagCompound par1NBTTagCompound)
     {
         super.writeToNBT(par1NBTTagCompound);
         NBTTagList var2 = new NBTTagList();
 
         for (int var3 = 0; var3 < this.inv.length; ++var3)
         {
             if (this.inv[var3] != null)
             {
                 NBTTagCompound var4 = new NBTTagCompound();
                 var4.setByte("Slot", (byte)var3);
                 this.inv[var3].writeToNBT(var4);
                 var2.appendTag(var4);
             }
         }
         par1NBTTagCompound.setTag("Items", var2);
     }
 	
 	@Override
     public void onDataPacket(INetworkManager net, Packet132TileEntityData packet) {
     	readFromNBT(packet.customParam1);
     }
     
     @Override
     public Packet getDescriptionPacket()
     {
         NBTTagCompound var1 = new NBTTagCompound();
         writeToNBT(var1);
         return new Packet132TileEntityData(this.xCoord, this.yCoord, this.zCoord, 1, var1);
     }
 
 	@Override
 	public boolean isInvNameLocalized() {
 		return false;
 	}
 
 	@Override
 	public boolean isStackValidForSlot(int i, ItemStack itemstack) {
 		return false;
 	}
 
     @Override
     public TileEntity getTile() {
         return this;
     }
 
     @Override
     public boolean connect(ForgeDirection side) {
        if(side != ForgeDirection.DOWN && inv[0]!=null && inv[0].getItem().itemID == ModBlocks.Crystal.blockID) {
             return true;
         }
         return false;
     }
 
     @Override
     public IPlasmaNetwork getNetwork() {
         return network;
     }
     
     @Override
     public void setNetwork(IPlasmaNetwork network) {
         this.network = network;
     }
 }
