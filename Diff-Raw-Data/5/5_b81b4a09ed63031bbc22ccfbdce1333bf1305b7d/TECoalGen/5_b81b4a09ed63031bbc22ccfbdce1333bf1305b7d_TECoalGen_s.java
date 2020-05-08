 package com.github.rossrkk.utilities.tileentities;
 
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.inventory.IInventory;
 import net.minecraft.item.ItemStack;
 import net.minecraft.item.crafting.FurnaceRecipes;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.nbt.NBTTagList;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.tileentity.TileEntityFurnace;
 
 import com.github.rossrkk.utilities.power.IPower;
 
 import cpw.mods.fml.common.registry.GameRegistry;
 
 public class TECoalGen extends TileEntity implements IPower, IInventory {
 
 	public ItemStack inventory;
 
 	public int power;
 	public int maxPower = 1024;
 	public int toTransfer = 16;
 
 	public int currentBurnTime = 0;
 
 	@Override
	public void updateEntity() {		
 		if (currentBurnTime > 0 && power < maxPower) {
 			power += 1;
 			currentBurnTime --;
 		}
 
 		if (currentBurnTime <= 0 && power < maxPower) {
 			burn();
 		}
 		transferPower();
 	}
 
 	public void transferPower() {
 		if (power >= 16) {
 			//Transfer power
 			int randomSide = worldObj.rand.nextInt(6);
 			switch (randomSide) {
 			case 0: transfer(xCoord, yCoord, zCoord + 1);
 			break;
 			case 1: transfer(xCoord - 1, yCoord, zCoord);
 			break;
 			case 2: transfer(xCoord + 1, yCoord, zCoord);
 			break;
 			case 3: transfer(xCoord, yCoord - 1, zCoord);
 			break;
 			case 4: transfer(xCoord, yCoord + 1, zCoord);
 			break;
 			case 5: transfer(xCoord, yCoord, zCoord - 1);
 			break;
 			}
 		}
 	}
 
 	public void burn() {		
 		if (inventory != null && TileEntityFurnace.getItemBurnTime(inventory) > 0) {
			currentBurnTime = TileEntityFurnace.getItemBurnTime(inventory) / 100;
 			decrStackSize(0, 1);
 			onInventoryChanged();
 		}
 	}
 
 	public void transfer(int x, int y, int z) {
 		if (worldObj.getBlockTileEntity(x, y, z) instanceof IPower 
 				&& !((IPower)worldObj.getBlockTileEntity(x, y, z)).isGenerator() 
 				&& power >= toTransfer) {
 			power = power + ((IPower)worldObj.getBlockTileEntity(x, y, z)).incrementPower(toTransfer) - toTransfer;
 		}
 	}
 
 	@Override
 	public int getPower() {
 		return power;
 	}
 
 	@Override
 	public int incrementPower(int count) {
 		if (count + power <= maxPower) {
 			power += count;
 			return 0;
 		} else {
 			int temp = maxPower - (power + count);
 			power = maxPower;
 			return temp;
 		}
 	}
 
 	@Override
 	public boolean isGenerator() {
 		return true;
 	}
 
 	@Override
 	public void writeToNBT(NBTTagCompound compound) {
 		super.writeToNBT(compound);
 
 		NBTTagList items = new NBTTagList();
 
 		ItemStack stack = getStackInSlot(0);
 
 		if (stack != null) {
 			NBTTagCompound item = new NBTTagCompound();
 			stack.writeToNBT(item);
 			items.appendTag(item);
 		}
 
 		compound.setTag("Items", items);
 
 		compound.setShort("power", (short) power);
 	}
 
 	@Override
 	public void readFromNBT(NBTTagCompound compound) {
 		super.readFromNBT(compound);
 
 		NBTTagList items = compound.getTagList("Items");
 		if ((NBTTagCompound)items.tagAt(0) != null) {
 			NBTTagCompound item = (NBTTagCompound)items.tagAt(0);
 			int slot = item.getByte("Slot");
 
 			setInventorySlotContents(slot, ItemStack.loadItemStackFromNBT(item));
 		}
 		power = compound.getShort("power");
 	}
 
 	@Override
 	public int getSizeInventory() {
 		return 1;
 	}
 
 	@Override
 	public ItemStack getStackInSlot(int i) {
 		return inventory;
 	}
 
 	@Override
 	public ItemStack decrStackSize(int i, int count) {
 		ItemStack itemstack = getStackInSlot(i);
 
 		if (itemstack != null) {
 			if (itemstack.stackSize <= count) {
 				setInventorySlotContents(i, null);
 			} else {
 				itemstack = itemstack.splitStack(count);
 				onInventoryChanged();
 			}
 		}
 
 		return itemstack;
 	}
 
 	@Override
 	public ItemStack getStackInSlotOnClosing(int i) {
 		return inventory;
 	}
 
 	@Override
 	public void setInventorySlotContents(int i, ItemStack itemstack) {
 		inventory = itemstack;
 		if (itemstack != null && itemstack.stackSize > getInventoryStackLimit()) {
 			itemstack.stackSize = getInventoryStackLimit();
 		}
 
 		onInventoryChanged();
 	}
 
 	@Override
 	public String getInvName() {
 		return "coalGen";
 	}
 
 	@Override
 	public boolean isInvNameLocalized() {
 		return false;
 	}
 
 	@Override
 	public int getInventoryStackLimit() {
 		return 64;
 	}
 
 	@Override
 	public boolean isUseableByPlayer(EntityPlayer entityplayer) {
 		return entityplayer.getDistanceSq(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5) <= 64;
 	}
 
 	@Override
 	public void openChest() {
 
 	}
 
 	@Override
 	public void closeChest() {
 
 	}
 
 	@Override
 	public boolean isItemValidForSlot(int i, ItemStack itemstack) {
 		return TileEntityFurnace.getItemBurnTime(itemstack) > 0;
 	}
 
 	//	@Override
 	//	public void onInventoryChanged() {
 	//		super.onInventoryChanged();
 	//		if (inventory != null && inventory.stackSize <= 0) {
 	//			inventory = null;
 	//		}
 	//	}
 }
