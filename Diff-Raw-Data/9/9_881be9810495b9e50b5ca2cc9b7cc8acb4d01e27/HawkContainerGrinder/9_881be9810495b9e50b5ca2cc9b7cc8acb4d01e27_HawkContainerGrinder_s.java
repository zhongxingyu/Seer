 
 package hawksmachinery;
 
 import universalelectricity.basiccomponents.SlotElectricItem;
 import universalelectricity.extend.IItemElectric;
 import net.minecraft.src.Container;
 import net.minecraft.src.EntityPlayer;
 import net.minecraft.src.FurnaceRecipes;
 import net.minecraft.src.InventoryPlayer;
 import net.minecraft.src.ItemStack;
 import net.minecraft.src.Slot;
 import net.minecraft.src.TileEntity;
 
 /**
  * @author Elusivehawk
  *
  */
 public class HawkContainerGrinder extends Container
 {
 	private HawkTileEntityGrinder tileEntity;
 	
     public HawkContainerGrinder(InventoryPlayer playerInventory, HawkTileEntityGrinder tileEntity)
     {
         this.tileEntity = tileEntity;
         this.addSlotToContainer(new SlotElectricItem(tileEntity, 0, 55, 49));
         this.addSlotToContainer(new Slot(tileEntity, 1, 55, 25));
         this.addSlotToContainer(new SlotProcessorsOutput(playerInventory.player, tileEntity, 2, 108, 25));
         int var3;
 
         for (var3 = 0; var3 < 3; ++var3)
         {
             for (int var4 = 0; var4 < 9; ++var4)
             {
                 this.addSlotToContainer(new Slot(playerInventory, var4 + var3 * 9 + 9, 8 + var4 * 18, 84 + var3 * 18));
             }
         }
 
         for (var3 = 0; var3 < 9; ++var3)
         {
             this.addSlotToContainer(new Slot(playerInventory, var3, 8 + var3 * 18, 142));
         }
     }
     
     @Override
 	public boolean canInteractWith(EntityPlayer var1)
 	{
 		return true;
 	}
     
     public TileEntity getGrinderContainer()
     {
     	return this.tileEntity;
     }
     
     @Override
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
                 if (var4.getItem() instanceof IItemElectric)
                 {
                     if (!this.mergeItemStack(var4, 0, 1, false))
                     {
                         return null;
                     }
                 }
                else if (FurnaceRecipes.smelting().getSmeltingResult(var4) != null)
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
