 package mods.cc.rock.inventory;
 
 
 
 import mods.cc.rock.tileentity.TileEntityRefridgerator;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.entity.player.InventoryPlayer;
 import net.minecraft.inventory.Container;
 import net.minecraft.item.ItemStack;
 
 public class ContainerRefridgerator extends Container{
 	protected TileEntityRefridgerator tileEntity;
 	private int sn = 0;
     public ContainerRefridgerator (InventoryPlayer inventoryPlayer, TileEntityRefridgerator te){
         tileEntity = te;
 
         
         
         //the Slot constructor takes the IInventory and the slot number in that it binds to
         //and the x-y coordinates it resides on-screen
         
         //crafting grid
         for (int x = 0; x < 9; x++) {
                 for (int y = 0; y < 8; y++) {
                 		
                         addSlotToContainer(new SlotRefrigerator(tileEntity, sn, 8 + x * 18, 17 + y * 18,false));
                         sn++;
                 }
         }
         
 
         //commonly used vanilla code that adds the player's inventory
         	
         bindPlayerInventory(inventoryPlayer);
     }
     protected void bindPlayerInventory(InventoryPlayer inventoryPlayer) {
     	//player main inv
     	for (int i = 0; i < 3; i++) {
     		for (int j = 0; j < 9; j++) {
     			
     			addSlotToContainer(new SlotRefrigerator(inventoryPlayer, j + i * 9 + 9, 8 + j * 18, 174 + i * 18,true));
     			
     		}
         }
     	//player hotbar
         for (int j = 0; j < 9; ++j)
         {
             this.addSlotToContainer(new SlotRefrigerator(inventoryPlayer, j, 8 + j * 18, 232,true));
         }
     }
 	@Override
 	public boolean canInteractWith(EntityPlayer entityplayer) {
 		
		return true;
 	}
 	@Override
     public ItemStack transferStackInSlot(EntityPlayer player, int slot) {
         ItemStack stack = null;
         SlotRefrigerator slotObject = (SlotRefrigerator) inventorySlots.get(slot);
         slotObject.playerinv = false;
 
         //null checks and checks if the item can be stacked (maxStackSize > 1)
         if (slotObject != null && slotObject.getHasStack()) {
                 ItemStack stackInSlot = slotObject.getStack();
                 stack = stackInSlot.copy();
 
                 //merges the item into player inventory since its in the tileEntity
                 if (slot < TileEntityRefridgerator.INVENTORY_SIZE) {
                 		
 	                        if (!this.mergeItemStack(stackInSlot, TileEntityRefridgerator.INVENTORY_SIZE, this.inventorySlots.size(), true)) {
 	                                return null;
 	                        }
                 		
                 }
                 //places it into the tileEntity is possible since its in the player inventory
                 else{
                 	if(((SlotRefrigerator)slotObject).isItemValid(stack)){
 	                	if (!this.mergeItemStack(stackInSlot, 0, TileEntityRefridgerator.INVENTORY_SIZE, false)) {
 	                
 	                        return null;
 	                	}
                 	}else{
                 		return null;
                 	}
                 }
 
                 if (stackInSlot.stackSize == 0) {
                         slotObject.putStack((ItemStack)null);
                 } else {
                         slotObject.onSlotChanged();
                 }
 
                 /*if (stackInSlot.stackSize == stack.stackSize) {
                         return null;
                 }
                 slotObject.onPickupFromSlot(player, stackInSlot);*/
         }
         return stack;
 }
 
 }
