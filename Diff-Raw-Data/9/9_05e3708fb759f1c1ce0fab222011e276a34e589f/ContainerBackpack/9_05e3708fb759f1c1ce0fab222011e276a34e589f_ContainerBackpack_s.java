 package backpack.inventory;
 
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.inventory.Container;
 import net.minecraft.inventory.IInventory;
 import net.minecraft.inventory.Slot;
 import net.minecraft.item.ItemStack;
 import backpack.misc.Constants;
 import backpack.util.IBackpack;
 import backpack.util.NBTUtil;
 
 public class ContainerBackpack extends Container {
     private int numRows;
     private ItemStack openedBackpack;
 
     public ContainerBackpack(IInventory playerInventory, IInventory backpackInventory, ItemStack backpack) {
         numRows = backpackInventory.getSizeInventory() / 9;
         backpackInventory.openChest();
         int offset = (numRows - 4) * 18;
 
         // backpack
         for(int row = 0; row < numRows; ++row) {
             for(int col = 0; col < 9; ++col) {
                 addSlotToContainer(new SlotBackpack(backpackInventory, col + row * 9, 8 + col * 18, 18 + row * 18));
             }
         }
 
         // inventory
         for(int row = 0; row < 3; ++row) {
             for(int col = 0; col < 9; ++col) {
                 addSlotToContainer(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 103 + row * 18 + offset));
             }
         }
 
         // hot bar
         for(int col = 0; col < 9; ++col) {
             addSlotToContainer(new Slot(playerInventory, col, 8 + col * 18, 161 + offset));
         }
         openedBackpack = backpack;
     }
 
     /**
      * True is the current equipped item is the opened item otherwise false.
      */
     @Override
     public boolean canInteractWith(EntityPlayer player) {
         ItemStack itemStack = null;
         if(NBTUtil.getBoolean(openedBackpack, Constants.WEARED_BACKPACK_OPEN)) {
             itemStack = player.getCurrentArmor(2);
         } else if(player.getCurrentEquippedItem() != null) {
             itemStack = player.getCurrentEquippedItem();
         }
        if(itemStack.getDisplayName() == openedBackpack.getDisplayName()) {
             return true;
         }
         return false;
     }
 
     /**
      * Called when a player shift-clicks on a slot.
      */
     @Override
     public ItemStack transferStackInSlot(EntityPlayer par1EntityPlayer, int slotPos) {
         ItemStack returnStack = null;
         Slot slot = (Slot) inventorySlots.get(slotPos);
 
         if(slot != null && slot.getHasStack()) {
             ItemStack itemStack = slot.getStack();
             if(itemStack.getItem() instanceof IBackpack) {
                 return returnStack;
             }
             returnStack = itemStack.copy();
 
             if(slotPos < numRows * 9) {
                 if(!mergeItemStack(itemStack, numRows * 9, inventorySlots.size(), true)) {
                     return null;
                 }
             } else if(!mergeItemStack(itemStack, 0, numRows * 9, false)) {
                 return null;
             }
 
             if(itemStack.stackSize == 0) {
                 slot.putStack((ItemStack) null);
             } else {
                 slot.onSlotChanged();
             }
         }
 
         return returnStack;
     }
 
     @Override
     public void onCraftGuiClosed(EntityPlayer player) {
         super.onCraftGuiClosed(player);
 
         if(!player.worldObj.isRemote) {
             ItemStack itemStack = player.getCurrentArmor(2);
             if(itemStack != null) {
                 if(NBTUtil.hasTag(itemStack, Constants.WEARED_BACKPACK_OPEN)) {
                     NBTUtil.removeTag(itemStack, Constants.WEARED_BACKPACK_OPEN);
                 }
             }
         }
     }
 }
