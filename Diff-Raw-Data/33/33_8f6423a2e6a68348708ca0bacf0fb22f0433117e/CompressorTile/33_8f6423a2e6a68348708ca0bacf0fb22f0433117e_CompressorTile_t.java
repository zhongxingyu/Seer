 package ccm.compresstion.tileentity;
 
 import net.minecraft.block.Block;
 import net.minecraft.inventory.ISidedInventory;
 import net.minecraft.item.ItemStack;
 import net.minecraft.item.crafting.FurnaceRecipes;
 import net.minecraft.tileentity.TileEntityFurnace;
 import net.minecraftforge.common.ForgeDirection;
 
 import ccm.compresstion.Compresstion;
import ccm.compresstion.block.ModBlocks;
 import ccm.compresstion.utils.lib.Archive;
 import ccm.nucleum.omnium.inventory.container.element.TimedElement;
 import ccm.nucleum.omnium.tileentity.ProgressTE;
 import ccm.nucleum.omnium.utils.helper.CCMLogger;
 import ccm.nucleum.omnium.utils.helper.NBTHelper;
 import ccm.nucleum.omnium.utils.helper.item.ItemHelper;
 
 public class CompressorTile extends ProgressTE implements ISidedInventory
 {
     public static final int progressBar = 0;
     public static final int burnBar = 1;
     private int currentItemBurnTime;
 
     public CompressorTile()
     {
         progresses = new TimedElement[2];
         progresses[progressBar] = new TimedElement();
         progresses[burnBar] = new TimedElement();
     }
 
     @Override
     public void updateEntity()
     {
         super.updateEntity();
         boolean burning = getTimeLeft(burnBar) > 0;
         boolean done = false;
 
         if (getTimeLeft(burnBar) > 0)
         {
             update(burnBar, -1);
         }
 
         if (!worldObj.isRemote)
         {
             if (getTimeLeft(burnBar) == 0 && canRun())
             {
                 int burnTime = TileEntityFurnace.getItemBurnTime(inventory[1]);
                 setTimeLeft(burnBar, burnTime);
                 currentItemBurnTime = burnTime;
 
                 if (getTimeLeft(burnBar) > 0)
                 {
                     done = true;
 
                     if (inventory[1] != null)
                     {
                         --inventory[1].stackSize;
 
                         if (inventory[1].stackSize == 0)
                         {
                             inventory[1] = inventory[1].getItem().getContainerItemStack(inventory[1]);
                         }
                     }
                 }
             }
 
             if (getTimeLeft(burnBar) > 0 && canRun())
             {
                 update(progressBar, 1);
 
                 if (getTimeLeft(progressBar) == 200)
                 {
                     setTimeLeft(progressBar, 0);
                     compressItem();
                     done = true;
                 }
             } else
             {
                 setTimeLeft(burnBar, 0);
             }
 
             if (burning != getTimeLeft(burnBar) > 0)
             {
                 done = true;
             }
         }
 
         if (done)
         {
             onInventoryChanged();
         }
     }
 
     @Override
     public boolean canRun()
     {
         if (inventory[0] != null)
         {
             ItemStack stack = inventory[0];
             Block block = Block.blocksList[stack.itemID - 256 < 0 ? stack.itemID : stack.itemID - 256];
             int meta = stack.getItemDamage();
             if (isNormalBlock(block, meta))
             {
                 if (inventory[2] == null)
                     return true;
                 if (!inventory[2].isItemEqual(stack))
                     return false;
                 int result = inventory[2].stackSize + stack.stackSize;
                 return (result <= getInventoryStackLimit() && result <= stack.getMaxStackSize());
             }
         }
         return false;
     }
 
     /**
     * Turn one item from the compressor source stack into the appropriate compressed item in the compressor result stack
      */
     public void compressItem()
     {
         if (canRun())
         {
             ItemStack stack = inventory[0].copy();
            Block block = Block.blocksList[stack.itemID - 256 < 0 ? stack.itemID : stack.itemID - 256];
            int meta = stack.getItemDamage();
 
             // if the stack is Ours
            if (block.blockID == ModBlocks.compressor.blockID)
             {
                if (meta + 1 <= 15)
                 {
                    stack.setItemDamage(meta + 1);
                 } else
                 {
                     CCMLogger.DEFAULT_LOGGER.severe("WTF!!!!!!");
                     return;
                 }
             } else
             // if it is not
             {
                ItemStack tmp = new ItemStack(ModBlocks.compressor.blockID + 256, 1, 0);
                NBTHelper.setInteger(tmp, Archive.NBT_COMPRESSED_BLOCK_ID, block.blockID);
                NBTHelper.setInteger(stack, Archive.NBT_COMPRESSED_BLOCK_META, meta);
                stack = tmp;
             }
 
             // Below it does all the item checking
             if (inventory[2] == null)
             {
                 inventory[2] = stack.copy();
             } else if (ItemHelper.compare(inventory[2], stack))
             {
                 inventory[2].stackSize += stack.stackSize;
             }
 
             --inventory[0].stackSize;
 
             if (inventory[0].stackSize <= 0)
             {
                 inventory[0] = null;
             }
         }
     }
 
     // TODO Move to Nucleum
     public static boolean isNormalBlock(Block block, int meta)
     {
         return (block.isOpaqueCube() && (block.getRenderType() == 0) && (!block.hasTileEntity(meta)) && block.renderAsNormalBlock() && hasNormalBounds(block));
     }
 
     // TODO Move to Nucleum
     public static boolean hasNormalBounds(Block block)
     {
         return ((block.getBlockBoundsMaxX() == 1) && (block.getBlockBoundsMaxY() == 1) && (block.getBlockBoundsMaxZ() == 1) && (block.getBlockBoundsMinX() == 0)
                 && (block.getBlockBoundsMinY() == 0) && (block.getBlockBoundsMinZ() == 0));
     }
 
     @Override
     public int[] getAccessibleSlotsFromSide(int side)
     {
         if (side == ForgeDirection.UP.ordinal())
         {
             return new int[]
             { 0 };
         } else if (side == ForgeDirection.DOWN.ordinal())
         {
             return new int[]
             { 2 };
         } else
         {
             return new int[]
             { 1 };
         }
     }
 
     @Override
     public boolean canInsertItem(int slot, ItemStack item, int side)
     {
         if (side != ForgeDirection.DOWN.ordinal())
         {
             if (side != ForgeDirection.UP.ordinal())
             {
                 if (TileEntityFurnace.isItemFuel(item))
                 {
                     return true;
                 }
             } else
             {
                 return true;
             }
         }
         return false;
     }
 
     @Override
     public boolean canExtractItem(int slot, ItemStack item, int side)
     {
         return side == ForgeDirection.DOWN.ordinal() ? true : false;
     }
 
     @Override
     public boolean isItemValidForSlot(int slot, ItemStack item)
     {
         if (slot != 2)
         {
             if (slot != 0)
             {
                 if (TileEntityFurnace.isItemFuel(item))
                 {
                     return true;
                 }
             } else
             {
                 return true;
             }
         }
         return false;
     }
 }
