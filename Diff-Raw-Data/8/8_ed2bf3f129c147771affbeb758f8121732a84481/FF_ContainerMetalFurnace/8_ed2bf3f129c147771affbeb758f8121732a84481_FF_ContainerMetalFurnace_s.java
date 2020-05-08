 package shadow.mods.metallurgy.fantasy;
 
 import net.minecraft.src.*;
 
 public class FF_ContainerMetalFurnace extends Container
 {
     private FF_TileEntityMetalFurnace furnace;
     private int lastCookTime = 0;
     private int lastBurnTime = 0;
     private int lastItemBurnTime = 0;
 
     public FF_ContainerMetalFurnace(InventoryPlayer par1InventoryPlayer, FF_TileEntityMetalFurnace par2TileEntityMetalFurnace)
     {
         this.furnace = par2TileEntityMetalFurnace;
         this.addSlotToContainer(new Slot(par2TileEntityMetalFurnace, 0, 56, 17));
         this.addSlotToContainer(new Slot(par2TileEntityMetalFurnace, 1, 56, 53));
         this.addSlotToContainer(new FF_SlotMetalFurnace(par1InventoryPlayer.player, par2TileEntityMetalFurnace, 2, 116, 35));
         int var3;
 
         for (var3 = 0; var3 < 3; ++var3)
         {
             for (int var4 = 0; var4 < 9; ++var4)
             {
                 this.addSlotToContainer(new Slot(par1InventoryPlayer, var4 + var3 * 9 + 9, 8 + var4 * 18, 84 + var3 * 18));
             }
         }
 
         for (var3 = 0; var3 < 9; ++var3)
         {
             this.addSlotToContainer(new Slot(par1InventoryPlayer, var3, 8 + var3 * 18, 142));
         }
     }
 
     /**
      * Updates crafting matrix; called from onCraftMatrixChanged. Args: none
      */
     public void updateCraftingResults()
     {
         super.updateCraftingResults();
 
         for (int var1 = 0; var1 < this.crafters.size(); ++var1)
         {
             ICrafting var2 = (ICrafting)this.crafters.get(var1);
 
             if (this.lastCookTime != this.furnace.furnaceCookTime)
             {
                 var2.updateCraftingInventoryInfo(this, 0, this.furnace.furnaceCookTime);
             }
 
             if (this.lastBurnTime != this.furnace.furnaceBurnTime)
             {
                 var2.updateCraftingInventoryInfo(this, 1, this.furnace.furnaceBurnTime);
             }
 
             if (this.lastItemBurnTime != this.furnace.currentItemBurnTime)
             {
                 var2.updateCraftingInventoryInfo(this, 2, this.furnace.currentItemBurnTime);
             }
         }
 
         this.lastCookTime = this.furnace.furnaceCookTime;
         this.lastBurnTime = this.furnace.furnaceBurnTime;
         this.lastItemBurnTime = this.furnace.currentItemBurnTime;
     }
 
     public void updateProgressBar(int par1, int par2)
     {
         if (par1 == 0)
         {
             this.furnace.furnaceCookTime = par2;
         }
 
         if (par1 == 1)
         {
             this.furnace.furnaceBurnTime = par2;
         }
 
         if (par1 == 2)
         {
             this.furnace.currentItemBurnTime = par2;
         }
     }
 
     public boolean canInteractWith(EntityPlayer par1EntityPlayer)
     {
         return this.furnace.isUseableByPlayer(par1EntityPlayer);
     }
 
     /**
      * Called to transfer a stack from one inventory to the other eg. when shift clicking.
      */
     public ItemStack transferStackInSlot(int par1)
     {
         ItemStack var2 = null;
         Slot var3 = (Slot)this.inventorySlots.get(par1);
 
         if (var3 != null && var3.getHasStack())
         {
             ItemStack var4 = var3.getStack();
             var2 = var4.copy();
 
             if (par1 == 2)
             {
                 if (!this.mergeItemStack(var4, 3, 39, true))
                 {
                     return null;
                 }
 
                 var3.onSlotChange(var4, var2);
             }
             else if (par1 != 1 && par1 != 0)
             {
                 if (FF_EssenceRecipes.essence().getEssenceResult(var4) != 0)
                 {
                     if (!this.mergeItemStack(var4, 0, 1, false))
                     {
                         return null;
                     }
                 }
                else if (TileEntityFurnace.isItemFuel(var4))
                 {
                     if (!this.mergeItemStack(var4, 1, 2, false))
                     {
                         return null;
                     }
                 }
                 else if (par1 >= 3 && par1 < 30)
                 {
                     if (!this.mergeItemStack(var4, 30, 39, false))
                     {
                         return null;
                     }
                 }
                 else if (par1 >= 30 && par1 < 39 && !this.mergeItemStack(var4, 3, 30, false))
                 {
                     return null;
                 }
             }
             else if (!this.mergeItemStack(var4, 3, 39, false))
             {
                 return null;
             }
 
             if (var4.stackSize == 0)
             {
                 var3.putStack((ItemStack)null);
             }
             else
             {
                 var3.onSlotChanged();
             }
 
             if (var4.stackSize == var2.stackSize)
             {
                 return null;
             }
 
             var3.onPickupFromSlot(var4);
         }
 
         return var2;
     }
 }
