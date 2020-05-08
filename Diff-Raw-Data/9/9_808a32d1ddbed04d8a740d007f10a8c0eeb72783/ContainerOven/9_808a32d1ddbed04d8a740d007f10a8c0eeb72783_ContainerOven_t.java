 package mods.cc.rock.inventory;
 
 import mods.cc.rock.recipie.OvenRecipes;
 import mods.cc.rock.tileentity.TileEntityOven;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.entity.player.InventoryPlayer;
 import net.minecraft.inventory.Container;
 import net.minecraft.inventory.ICrafting;
 import net.minecraft.inventory.Slot;
 import net.minecraft.item.ItemStack;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 public class ContainerOven extends Container{
 	
     private TileEntityOven tileEntity;
     private int lastCookTime = 0;
     private int lastBurnTime = 0;
     private int lastItemBurnTime = 0;
     public ContainerOven(InventoryPlayer player, TileEntityOven tileEntity2)
     {
         this.tileEntity = tileEntity2;
         this.addSlotToContainer(new Slot(tileEntity2, 0, 56, 17));
         this.addSlotToContainer(new Slot(tileEntity2, 1, 56, 53));
         this.addSlotToContainer(new Slot(tileEntity2, 2, 116, 35));
         int i;
 
         for (i = 0; i < 3; ++i)
         {
             for (int j = 0; j < 9; ++j)
             {
                 this.addSlotToContainer(new Slot(player, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
             }
         }
 
         for (i = 0; i < 9; ++i)
         {
             this.addSlotToContainer(new Slot(player, i, 8 + i * 18, 142));
         }
     }
     
     public void addCraftingToCrafters(ICrafting par1ICrafting)
     {
         super.addCraftingToCrafters(par1ICrafting);
         par1ICrafting.sendProgressBarUpdate(this, 0, this.tileEntity.ovenCookTime);
         par1ICrafting.sendProgressBarUpdate(this, 1, this.tileEntity.ovenBurnTime);
         par1ICrafting.sendProgressBarUpdate(this, 2, this.tileEntity.currentItemBurnTime);
     }
 	
     /**
      * Looks for changes made in the container, sends them to every listener.
      */
     public void detectAndSendChanges()
     {
         super.detectAndSendChanges();
 
         for (int i = 0; i < this.crafters.size(); ++i)
         {
             ICrafting icrafting = (ICrafting)this.crafters.get(i);
 
             if (this.lastCookTime != this.tileEntity.ovenCookTime)
             {
                 icrafting.sendProgressBarUpdate(this, 0, this.tileEntity.ovenCookTime);
             }
 
             if (this.lastBurnTime != this.tileEntity.ovenBurnTime)
             {
                 icrafting.sendProgressBarUpdate(this, 1, this.tileEntity.ovenBurnTime);
             }
 
             if (this.lastItemBurnTime != this.tileEntity.currentItemBurnTime)
             {
                 icrafting.sendProgressBarUpdate(this, 2, this.tileEntity.currentItemBurnTime);
             }
         }
 
         this.lastCookTime = this.tileEntity.ovenCookTime;
         this.lastBurnTime = this.tileEntity.ovenBurnTime;
         this.lastItemBurnTime = this.tileEntity.currentItemBurnTime;
     }
     
     @SideOnly(Side.CLIENT)
     public void updateProgressBar(int par1, int par2)
     {
         if (par1 == 0)
         {
             this.tileEntity.ovenCookTime = par2;
         }
 
         if (par1 == 1)
         {
             this.tileEntity.ovenBurnTime = par2;
         }
 
         if (par1 == 2)
         {
             this.tileEntity.currentItemBurnTime = par2;
         }
     }
     
 	@Override
 	public boolean canInteractWith(EntityPlayer entityplayer) {
		return true;
 	}
 	
     public ItemStack transferStackInSlot(EntityPlayer par1EntityPlayer, int par2)
     {
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
                 if (OvenRecipes.smelting().getSmeltingResult(itemstack1) != null)
                 {
                     if (!this.mergeItemStack(itemstack1, 0, 1, false))
                     {
                         return null;
                     }
                 }
                 else if (TileEntityOven.isItemFuel(itemstack1))
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
