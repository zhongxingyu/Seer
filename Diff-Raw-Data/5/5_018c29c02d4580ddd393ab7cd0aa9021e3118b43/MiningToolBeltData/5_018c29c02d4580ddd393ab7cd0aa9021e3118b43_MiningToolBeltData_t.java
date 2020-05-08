 package slimevoid.tmf.data;
 
 import slimevoid.tmf.lib.DataLib;
 import slimevoid.tmf.lib.NamingLib;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.inventory.IInventory;
 import net.minecraft.item.ItemStack;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.nbt.NBTTagList;
 import net.minecraft.world.World;
 import net.minecraft.world.WorldSavedData;
 
 public class MiningToolBeltData extends WorldSavedData implements IInventory {
 	private static final int TOOL_BELT_MAX_SIZE = 4;
 	private ItemStack[] miningTools;
 
 	public MiningToolBeltData(String dataString) {
 		super(dataString);
 		miningTools = new ItemStack[TOOL_BELT_MAX_SIZE];
 	}
 
 	@Override
     public void readFromNBT(NBTTagCompound nbttagcompound) {
		System.out.println("Read");
 		NBTTagList toolsTag = nbttagcompound.getTagList("Tools");
 		this.miningTools = new ItemStack[this.getSizeInventory()];
 		for (int i = 0; i < toolsTag.tagCount(); i++) {
 			NBTTagCompound tagCompound = (NBTTagCompound) toolsTag.tagAt(i);
 			byte slot = tagCompound.getByte("Slot");
 			if (slot >= 0 && slot < this.miningTools.length) {
 				this.miningTools[slot] = ItemStack.loadItemStackFromNBT(tagCompound);
 			}
 		}
 	}
 
     @Override
     public void writeToNBT(NBTTagCompound nbttagcompound) {
		System.out.println("Write");
     	NBTTagList toolsTag = new NBTTagList();
     	for (int i = 0; i < this.miningTools.length; i++) {
     		if (miningTools[i] != null) {
     			NBTTagCompound tagCompound = new NBTTagCompound();
     			tagCompound.setByte("Slot", (byte) i);
     			this.miningTools[i].writeToNBT(tagCompound);
         		toolsTag.appendTag(tagCompound);
     		}
     	}
 		nbttagcompound.setTag("Tools", toolsTag);
     }
 
 	@Override
 	public int getSizeInventory() {
 		return this.miningTools.length;
 	}
 
 	@Override
 	public ItemStack getStackInSlot(int slot) {
 		return this.miningTools[slot];
 	}
 
 	@Override
 	public ItemStack decrStackSize(int par1, int par2) {
 		return null;
 	}
 
 	@Override
 	public ItemStack getStackInSlotOnClosing(int slot) {
 		return this.miningTools[slot];
 	}
 
 	@Override
 	public void setInventorySlotContents(int slot, ItemStack itemstack) {
         this.miningTools[slot] = itemstack;
 
         if (itemstack != null && itemstack.stackSize > this.getInventoryStackLimit()) {
         	itemstack.stackSize = this.getInventoryStackLimit();
         }
 
         this.onInventoryChanged();
 	}
 
 	@Override
 	public String getInvName() {
 		return NamingLib.MINING_TOOL_BELT;
 	}
 
 	@Override
 	public int getInventoryStackLimit() {
 		return 1;
 	}
 
 	@Override
 	public void onInventoryChanged() {
		this.markDirty();
 	}
 
 	@Override
 	public boolean isUseableByPlayer(EntityPlayer entityplayer) {
 		return true;
 	}
 
 	@Override
 	public void openChest() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void closeChest() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public static MiningToolBeltData getToolBeltData(EntityPlayer player, World world, ItemStack heldItem) {
 		MiningToolBeltData data = (MiningToolBeltData)world.loadItemData(MiningToolBeltData.class, getWorldIndex(heldItem));
 		return data;
 	}
 
 	public static String getWorldIndex(ItemStack heldItem) {
 		return DataLib.TOOL_BELT_INDEX.replaceAll("#", Integer.toString(heldItem.getItemDamage()));
 	}
 
 	public static MiningToolBeltData getNewToolBeltData(
 			EntityPlayer entityplayer, World world, ItemStack itemstack) {
 		return new MiningToolBeltData(getWorldIndex(itemstack));
 	}
 
 }
