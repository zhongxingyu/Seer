 package ccm.compression.inventory.container;
 
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.inventory.ICrafting;
 import net.minecraft.inventory.IInventory;
 import net.minecraft.item.ItemStack;
 import net.minecraft.world.World;
 import ccm.compression.tileentity.CompressorTile;
 import ccm.nucleum.omnium.inventory.container.BaseContainer;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 public class ContainerCompressor extends BaseContainer
 {
     private final CompressorTile tile;
     private int lastCompressTime;
     private int lastCompressProgress;
 
     public ContainerCompressor(IInventory inventory)
     {
         super(inventory);
         tile = null;
     }
 
     public ContainerCompressor(EntityPlayer player, IInventory inventory)
     {
         super(inventory, player.inventory, 8, 84);
         tile = (CompressorTile) ((inventory != null) && (inventory instanceof CompressorTile) ? inventory : null);
         drawBoxInventory(inventory, CompressorTile.IN, 67, 35, 1, 1);
         drawBoxInventory(inventory, CompressorTile.FUEL, 23, 44, 1, 1);
         drawOutBoxInventory(inventory, CompressorTile.OUT, 131, 35, 1, 1);
     }
 
     @Override
     public void addCraftingToCrafters(ICrafting crafter)
     {
         super.addCraftingToCrafters(crafter);
         crafter.sendProgressBarUpdate(this, 0, tile.getCompressTime());
         crafter.sendProgressBarUpdate(this, 1, tile.getCompressProgress());
     }
 
     /**
      * Looks for changes made in the container, sends them to every listener.
      */
     @Override
     public void detectAndSendChanges()
     {
         super.detectAndSendChanges();
         if (tile.canRun())
         {
             for (Object o : crafters)
             {
                 ICrafting crafter = (ICrafting) o;
                 if (lastCompressTime != tile.getCompressTime())
                 {
                     crafter.sendProgressBarUpdate(this, 0, tile.getCompressTime());
                 }
                if (lastCompressProgress != tile.getCompressProgress())
                 {
                     crafter.sendProgressBarUpdate(this, 1, tile.getCompressProgress());
                 }
             }
             lastCompressTime = tile.getCompressTime();
             lastCompressProgress = tile.getCompressProgress();
         } else
         {
             lastCompressTime = 0;
             lastCompressProgress = 0;
             for (Object o : crafters)
             {
                 ICrafting crafter = (ICrafting) o;
                 crafter.sendProgressBarUpdate(this, 0, tile.getCompressTime());
                 crafter.sendProgressBarUpdate(this, 1, tile.getCompressProgress());
             }
         }
     }
 
     @Override
     public ItemStack transferStackInSlot(final EntityPlayer entityPlayer, final int slotIndex)
     {
         return null;
     }
 
     @Override
     @SideOnly(Side.CLIENT)
     public void updateProgressBar(final int index, final int progress)
     {
         switch (index)
         {
             case 0:
                 tile.updateCompressTime(progress);
                 break;
             case 1:
                 tile.updateCompressProgress(progress);
                 break;
             default:
                 break;
         }
     }
 
     @Override
     public Object getServerGuiElement(EntityPlayer player, World world, int x, int y, int z)
     {
         return new ContainerCompressor(player, (IInventory) world.getBlockTileEntity(x, y, z));
     }
}
