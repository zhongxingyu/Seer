 package jgdovin.voidsatchel.items.container;
 
 import jgdovin.voidsatchel.items.InventoryVoidSatchel;
 import jgdovin.voidsatchel.utils.Archive;
 import jgdovin.voidsatchel.utils.FunctionHelper;
 import jgdovin.voidsatchel.utils.NBTHelper;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.entity.player.InventoryPlayer;
 import net.minecraft.inventory.Container;
 import net.minecraft.inventory.IInventory;
 import net.minecraft.inventory.Slot;
 import net.minecraft.item.ItemStack;
 
 public class ContainerVoidSatchel extends Container {
 
     private int rows = 3, cols = 9;
     private InventoryVoidSatchel inv;
    private IInventory plInv = null;
 
     public ContainerVoidSatchel(ItemStack stack, EntityPlayer ep) {
 
         inv = new InventoryVoidSatchel(stack);
        plInv = ep.inventory;
         addInventory(inv, 8, 18);
         addPlayerInventory(ep.inventory, 102);
 
     }
 
     private void addInventory(IInventory in, int x, int y) {
         for (int r = 0; r < rows; r++){
             for (int c = 0; c < cols; c++){
                 addSlotToContainer(new Slot(in, c + (r * cols), x + (c * 18), y + (r * 18)));
             }
         }
     }
 
     private void addPlayerInventory(InventoryPlayer ip, int ystart) {
         // Add the player's action bar slots to the container
         for (int actionBarSlotIndex = 0; actionBarSlotIndex < 9; ++actionBarSlotIndex){
             this.addSlotToContainer(new Slot(ip, actionBarSlotIndex, 8 + (actionBarSlotIndex * 18),
                     58 + ystart));
         }
 
         // Add the player's inventory slots to the container
         for (int inventoryRowIndex = 0; inventoryRowIndex < 3; ++inventoryRowIndex){
             for (int inventoryColumnIndex = 0; inventoryColumnIndex < 9; ++inventoryColumnIndex){
                 this.addSlotToContainer(new Slot(ip, inventoryColumnIndex + (inventoryRowIndex * 9)
                         + 9, 8 + (inventoryColumnIndex * 18), ystart + (inventoryRowIndex * 18)));
             }
         }
     }
 
     @Override
     public boolean canInteractWith(EntityPlayer player) {
         return true;
     }
 
     @Override
     public void onCraftGuiClosed(EntityPlayer player) {
 
         super.onCraftGuiClosed(player);
         ItemStack currBag = FunctionHelper.getCurrentBag(player);
         if (currBag != null){
             // inv.writeBagContents(currBag);
             NBTHelper.removeTag(currBag, Archive.NBT_ITEM_VOID_SATCHEL_GUI_OPEN);
         }
 
     }
 
     public void clearInventory(EntityPlayer player) {
 
         for (int i = 0; i < 27; i++){
 
             Slot currSlot = this.getSlotFromInventory(this.inv, i);
             currSlot.decrStackSize(64);
 
         }
         this.detectAndSendChanges();
        ItemStack currBag = FunctionHelper.getCurrentBag(player);
         //inv.writeBagContents(currBag);
 
     }
 
     @Override
     public ItemStack transferStackInSlot(EntityPlayer entityPlayer, int slotIndex) {
         ItemStack itemstack = null;
         Slot slot = (Slot)this.inventorySlots.get(slotIndex);
 
         if (slot != null && slot.getHasStack())
         {
             ItemStack itemstack1 = slot.getStack();
             itemstack = itemstack1.copy();
 
             if (slotIndex < 4 * 9)
             {
                 if (!this.mergeItemStack(itemstack1, 4 * 9, this.inventorySlots.size(), true))
                 {
                     return null;
                 }
             }
             else if (!this.mergeItemStack(itemstack1, 0, 4 * 9, false))
             {
                 return null;
             }
 
             if (itemstack1.stackSize == 0)
             {
                 slot.putStack((ItemStack)null);
             }
             else
             {
                 slot.onSlotChanged();
             }
         }
 
         return itemstack;
     }
 }
