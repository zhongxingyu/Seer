 package backpack.inventory;
 
 import invtweaks.api.ContainerGUI;
 import invtweaks.api.ContainerGUI.ContainerSectionCallback;
 import invtweaks.api.ContainerSection;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.entity.player.InventoryPlayer;
 import net.minecraft.inventory.Container;
 import net.minecraft.inventory.IInventory;
 import net.minecraft.inventory.InventoryCraftResult;
 import net.minecraft.inventory.InventoryCrafting;
 import net.minecraft.inventory.Slot;
 import net.minecraft.item.ItemStack;
 import net.minecraft.item.crafting.CraftingManager;
 import net.minecraft.world.World;
 import backpack.misc.Constants;
 import backpack.util.IBackpack;
 import backpack.util.NBTUtil;
 
 @ContainerGUI
 public class ContainerWorkbenchBackpack extends Container {
     private InventoryCrafting craftMatrix = new InventoryCrafting(this, 3, 3);
     private IInventory craftResult = new InventoryCraftResult();
     private int numRows;
     private ItemStack openedBackpack;
     private World worldObj;
 
     public ContainerWorkbenchBackpack(InventoryPlayer playerInventory, InventoryWorkbenchBackpack backpackInventory, ItemStack backpack) {
         worldObj = playerInventory.player.worldObj;
         numRows = backpackInventory.getSizeInventory() / 9;
         backpackInventory.openChest();
         int offset = numRows == 0 ? 0 : 41;
 
         craftMatrix = new InventoryCraftingAdvanced(this, backpackInventory);
 
         // result slot
         addSlotToContainer(new SlotCraftingAdvanced(playerInventory.player, craftMatrix, craftResult, backpackInventory, 0, 125, 35));
 
         // crafting grid
         for(int row = 0; row < 3; row++) {
             for(int col = 0; col < 3; col++) {
                 addSlotToContainer(new Slot(craftMatrix, col + row * 3, 30 + col * 18, 17 + row * 18));
             }
         }
 
         // inventory
         for(int row = 0; row < 3; row++) {
             for(int col = 0; col < 9; col++) {
                 addSlotToContainer(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + offset + row * 18));
             }
         }
 
         // hotbar
         for(int col = 0; col < 9; col++) {
             addSlotToContainer(new Slot(playerInventory, col, 8 + col * 18, 142 + offset));
         }
 
         // backpack
         for(int row = 0; row < numRows; ++row) {
             for(int col = 0; col < 9; ++col) {
                 addSlotToContainer(new SlotBackpack(backpackInventory, col + row * 9, 8 + col * 18, 75 + row * 18));
             }
         }
 
         onCraftMatrixChanged(craftMatrix);
         openedBackpack = backpack;
     }
 
     @Override
     public void onCraftMatrixChanged(IInventory par1IInventory) {
         craftResult.setInventorySlotContents(0, CraftingManager.getInstance().findMatchingRecipe(craftMatrix, worldObj));
     }
 
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
 
     @Override
     public ItemStack transferStackInSlot(EntityPlayer par1EntityPlayer, int slotPos) {
         ItemStack returnStack = null;
         Slot slot = (Slot) inventorySlots.get(slotPos);
 
         if(slot != null && slot.getHasStack()) {
             ItemStack itemStack = slot.getStack();
             returnStack = itemStack.copy();
 
             if(slotPos == 0) { // from craftingSlot
                 if(!mergeItemStack(itemStack, 37, 46, true)) { // to hotbar
                     if(!mergeItemStack(itemStack, 10, 37, false)) { // to inventory
                         if(!mergeItemStackWithBackpack(itemStack)) { // to backpack inventory
                             return null;
                         }
                     }
                 }
 
                 slot.onSlotChange(itemStack, returnStack);
             } else if(slotPos >= 1 && slotPos < 10) { // from crafting matrix
                 if(mergeItemStackWithBackpack(itemStack)) { // to backpack inventory
                     if(!mergeItemStack(itemStack, 37, 46, true)) { // to hotbar
                         if(!mergeItemStack(itemStack, 10, 37, false)) { // to inventory
                             return null;
                         }
                     }
                 }
             } else if(slotPos >= 10 && slotPos < 37) { // from inventory
                 if(mergeItemStackWithBackpack(itemStack)) { // to backpack inventory
                     if(!mergeItemStack(itemStack, 37, 46, true)) { // to hotbar
                         return null;
                     }
                 }
             } else if(slotPos >= 37 && slotPos < 46) { // from hotbar
                 if(!mergeItemStack(itemStack, 10, 37, false)) { // to inventory
                     if(!mergeItemStackWithBackpack(itemStack)) { // to backpack inventory
                         return null;
                     }
                 }
             } else if(numRows > 0 && slotPos >= 46 && slotPos < 64) { // from backpack inventory
                 if(!mergeItemStack(itemStack, 37, 46, true)) { // to hotbar
                     if(!mergeItemStack(itemStack, 10, 37, false)) { // to inventory
                         return null;
                     }
                 }
             } else if(!mergeItemStack(itemStack, 10, 45, false)) {
                 return null;
             }
 
             if(itemStack.stackSize == 0) {
                 slot.putStack((ItemStack) null);
             } else {
                 slot.onSlotChanged();
             }
 
             if(itemStack.stackSize == returnStack.stackSize) {
                 return null;
             }
 
             slot.onPickupFromSlot(par1EntityPlayer, itemStack);
 
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
 
     protected boolean mergeItemStackWithBackpack(ItemStack itemStack) {
         if(numRows > 0 && !(itemStack.getItem() instanceof IBackpack)) {
             return !mergeItemStack(itemStack, 46, 64, false);
         }
         return true;
     }
 
     @ContainerSectionCallback
     public Map<ContainerSection, List<Slot>> getContainerSections() {
         Map<ContainerSection, List<Slot>> slotRefs = new HashMap<ContainerSection, List<Slot>>();
 
         slotRefs.put(ContainerSection.CRAFTING_OUT, inventorySlots.subList(0, 1));
         slotRefs.put(ContainerSection.CRAFTING_IN_PERSISTENT, inventorySlots.subList(1, 10));
         slotRefs.put(ContainerSection.INVENTORY, inventorySlots.subList(10, 46));
         slotRefs.put(ContainerSection.INVENTORY_NOT_HOTBAR, inventorySlots.subList(10, 37));
         slotRefs.put(ContainerSection.INVENTORY_HOTBAR, inventorySlots.subList(37, 46));
         if(numRows > 0) {
             slotRefs.put(ContainerSection.CHEST, inventorySlots.subList(46, 64));
         }
         return slotRefs;
     }
 }
