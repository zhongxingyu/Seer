 package mods.themike.modjam.inventory;
 
 import mods.themike.modjam.items.ItemRune;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.inventory.IInventory;
 import net.minecraft.item.ItemStack;
 import net.minecraft.nbt.NBTTagCompound;
 
 public class InventoryStaff implements IInventory {
 	
 	public ItemStack[] inventory = new ItemStack[1];
 
 	EntityPlayer player = null;
 	
 	public InventoryStaff(EntityPlayer par1) {
 		player = par1;
 		
 	}
 	
 	@Override
 	public int getSizeInventory() {
 		return 1;
 	}
 
 	@Override
 	public ItemStack getStackInSlot(int i) {
 		return inventory[i];
 	}
 
 	@Override
 	public ItemStack decrStackSize(int i, int j) {
 		return null;
 	}
 
 	@Override
 	public ItemStack getStackInSlotOnClosing(int i) {
 		return inventory[i];
 	}
 
 	@Override
 	public void setInventorySlotContents(int i, ItemStack itemstack) {
 		inventory[i] = itemstack;
 		
 		ItemStack staff = player.getHeldItem();
 
 		if(staff != null) {
 			ItemStack rune = getStackInSlot(0);
 			if(rune != null) {
 				NBTTagCompound tag = new NBTTagCompound();
 				rune.writeToNBT(tag);
 				staff.getTagCompound().setTag("item", tag);
 				player.inventory.setInventorySlotContents(player.inventory.currentItem, staff);
 			}
 		}
 	}
 
 	@Override
 	public String getInvName() {
 		return "Staff";
 	}
 
 	@Override
 	public boolean isInvNameLocalized() {
 		return true;
 	}
 
 	@Override
 	public int getInventoryStackLimit() {
 		return 1;
 	}
 
 	@Override
 	public void onInventoryChanged() {
 
 	}
 
 	@Override
 	public boolean isUseableByPlayer(EntityPlayer entityplayer) {
 		return true;
 	}
 
 	@Override
 	public void openChest() {
 
 	}
 	
 
 	@Override
 	public void closeChest() {
 
 	}
 
 	@Override
 	public boolean isStackValidForSlot(int i, ItemStack itemstack) {
 		return true;
 	}
 
}
