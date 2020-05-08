 package mods.cc.rock.inventory;
 
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.inventory.IInventory;
 import net.minecraft.inventory.SlotCrafting;
 import net.minecraft.item.ItemStack;
 
 import net.minecraftforge.common.MinecraftForge;
 import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
 
 import cpw.mods.fml.common.registry.GameRegistry;
 
 import mods.cc.rock.lib.ItemIDs;
 
 public class SlotCraftingCC extends SlotCrafting
 {
     /** The craft matrix inventory linked to this result slot. */
     private final IInventory craftMatrix;
 
     /** The player that is using the GUI where this slot resides. */
     private EntityPlayer thePlayer;
     
     public SlotCraftingCC(EntityPlayer par1EntityPlayer, IInventory par2iInventory, IInventory par3iInventory, int par4, int par5, int par6)
     {
         super(par1EntityPlayer, par2iInventory, par3iInventory, par4, par5, par6);
         this.thePlayer = par1EntityPlayer;
         this.craftMatrix = par2iInventory;
     }
 
     @Override
     public void onPickupFromSlot(EntityPlayer par1EntityPlayer, ItemStack par2ItemStack)
     {
         GameRegistry.onItemCrafted(par1EntityPlayer, par2ItemStack, craftMatrix);
         this.onCrafting(par2ItemStack);
 
         for (int i = 0; i < this.craftMatrix.getSizeInventory(); ++i)
         {
             ItemStack itemstack1 = this.craftMatrix.getStackInSlot(i);
 
             if (itemstack1 != null && i != 8)
             {
                 this.craftMatrix.decrStackSize(i, 1);
 
                 if (itemstack1.getItem().hasContainerItem())
                 {
                     ItemStack itemstack2 = itemstack1.getItem().getContainerItemStack(itemstack1);
 
                     if (itemstack2.isItemStackDamageable() && itemstack2.getItemDamage() > itemstack2.getMaxDamage())
                     {
                         MinecraftForge.EVENT_BUS.post(new PlayerDestroyItemEvent(thePlayer, itemstack2));
                         itemstack2 = null;
                     }
 
                     if (itemstack2 != null && (!itemstack1.getItem().doesContainerItemLeaveCraftingGrid(itemstack1) || !this.thePlayer.inventory.addItemStackToInventory(itemstack2)))
                     {
                         if (this.craftMatrix.getStackInSlot(i) == null)
                             this.craftMatrix.setInventorySlotContents(i, itemstack2);
                         else
                             this.thePlayer.dropPlayerItem(itemstack2);
                     }
                 }
             }
             
            if (i == 8 && itemstack1 != null && (itemstack1.itemID == ItemIDs.ID_ROLLING_PIN_1 || itemstack1.itemID == ItemIDs.ID_ROLLING_PIN_2) && par2ItemStack.itemID == ItemIDs.ID_DOUGH)
                 itemstack1.damageItem(1, par1EntityPlayer);
         }
     }
 }
