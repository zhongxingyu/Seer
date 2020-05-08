 package mrkirby153.MscHouses.block.Container;
 
 import mrkirby153.MscHouses.block.tileEntity.TileEntityHouseGen;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.entity.player.InventoryPlayer;
 import net.minecraft.inventory.Container;
 import net.minecraft.inventory.Slot;
 import net.minecraft.item.ItemStack;
 import net.minecraft.item.crafting.FurnaceRecipes;
 import net.minecraft.tileentity.TileEntityFurnace;
 /**
  * 
  * Msc Houses
  *
  * ContainerBlockBase
  *
  * @author mrkirby153
  * @license Lesser GNU Public License v3 (http://www.gnu.org/licenses/lgpl.html)
  */
 public class ContainerHouseGenerator extends Container{
 	public ContainerHouseGenerator(InventoryPlayer inventoryPlayer, TileEntityHouseGen block_base) {
 
 		// Add the Moduel slot to the base
         this.addSlotToContainer(new Slot(block_base, 0, 81, 18));

        // Add the fuel slot to the Block Base
        this.addSlotToContainer(new Slot(block_base, 1, 81, 64));
         //Add the material modifyer to the block base
        this.addSlotToContainer(new Slot(block_base, 2, 81, 39));
 
 		// Add the player's inventory slots to the container
         for (int inventoryRowIndex = 0; inventoryRowIndex < 3; ++inventoryRowIndex) {
             for (int inventoryColumnIndex = 0; inventoryColumnIndex < 9; ++inventoryColumnIndex) {
                 this.addSlotToContainer(new Slot(inventoryPlayer, inventoryColumnIndex + inventoryRowIndex * 9 + 9, 8 + inventoryColumnIndex * 18, 94 + inventoryRowIndex * 18));
             }
         }
 
         // Add the player's action bar slots to the container
         for (int actionBarSlotIndex = 0; actionBarSlotIndex < 9; ++actionBarSlotIndex) {
             this.addSlotToContainer(new Slot(inventoryPlayer, actionBarSlotIndex, 8 + actionBarSlotIndex * 18, 152));
         }
 	}
 
 	@Override
 	public boolean canInteractWith(EntityPlayer player) {
 
 		return true;
 	}
 
 	@Override
 	public ItemStack transferStackInSlot(EntityPlayer par1EntityPlayer, int par2) {
 
 		ItemStack itemstack = null;
         Slot slot = (Slot)this.inventorySlots.get(par2);
 
         if (slot != null && slot.getHasStack())
         {
             ItemStack itemstack1 = slot.getStack();
             itemstack = itemstack1.copy();
 
             if (par2 == 2)
             {
                 if (!this.mergeItemStack(itemstack1, 3, 39, true))
                 {
                     return null;
                 }
 
                 slot.onSlotChange(itemstack1, itemstack);
             }
             else if (par2 != 1 && par2 != 0)
             {
                 if (FurnaceRecipes.smelting().getSmeltingResult(itemstack1) != null)
                 {
                     if (!this.mergeItemStack(itemstack1, 0, 1, false))
                     {
                         return null;
                     }
                 }
                 else if (TileEntityFurnace.isItemFuel(itemstack1))
                 {
                     if (!this.mergeItemStack(itemstack1, 1, 2, false))
                     {
                         return null;
                     }
                 }
                 else if (par2 >= 3 && par2 < 30)
                 {
                     if (!this.mergeItemStack(itemstack1, 30, 39, false))
                     {
                         return null;
                     }
                 }
                 else if (par2 >= 30 && par2 < 39 && !this.mergeItemStack(itemstack1, 3, 30, false))
                 {
                     return null;
                 }
             }
             else if (!this.mergeItemStack(itemstack1, 3, 39, false))
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
 
             if (itemstack1.stackSize == itemstack.stackSize)
             {
                 return null;
             }
 
             slot.onPickupFromSlot(par1EntityPlayer, itemstack1);
         }
 
         return itemstack;
 	}
 
 }
