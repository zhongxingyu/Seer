 package ccm.compresstion.block;
 
 import net.minecraft.block.BlockContainer;
 import net.minecraft.block.material.Material;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.util.Icon;
 import net.minecraft.world.World;
 import net.minecraftforge.common.ForgeDirection;
 import ccm.compresstion.tileentity.CompressorTile;
 import ccm.compresstion.utils.lib.Archive;
 import ccm.nucleum.omnium.tileentity.BaseTE;
 import ccm.nucleum.omnium.tileentity.InventoryTE;
 import ccm.nucleum.omnium.utils.handler.TileHandler;
 import ccm.nucleum.omnium.utils.handler.gui.GuiHandler;
 import ccm.nucleum.omnium.utils.helper.CCMLogger;
 import cpw.mods.fml.common.registry.GameRegistry;
 
 public class Compressor extends BlockContainer
 {
     private Icon top, bottom, sides;
 
     public Compressor(final int id)
     {
         super(id, Material.rock);
         setUnlocalizedName(Archive.COMPRESSOR.toLowerCase());
         GameRegistry.registerBlock(this, getUnlocalizedName());
         TileHandler.registerTile(Archive.COMPRESSOR, CompressorTile.class);
     }
 
     @Override
     public void registerIcons(IconRegister register)
     {
         top = register.registerIcon(Archive.MOD_NAME.toLowerCase() + ":compressorTop");
         sides = register.registerIcon(Archive.MOD_NAME.toLowerCase() + ":compressorSide");
         bottom = register.registerIcon(Archive.MOD_NAME.toLowerCase() + ":compressorBottom");
     }
 
     @Override
     public Icon getIcon(int side, int meta)
     {
         if (side == ForgeDirection.UP.ordinal())
         {
             return top;
         } else if (side == ForgeDirection.DOWN.ordinal())
         {
             return bottom;
         } else
         {
             return sides;
         }
     }
 
     @Override
     public TileEntity createNewTileEntity(final World world)
     {
         return ((InventoryTE) TileHandler.getTileInstance(Archive.COMPRESSOR)).setInventorySize(3);
     }
 
     @Override
     public boolean onBlockActivated(final World world, final int x, final int y, final int z, final EntityPlayer player, final int wut, final float clickX, final float clickY,
             final float clockZ)
     {
         if (world.isRemote)
         {
             return true;
         }
         if (player.isSneaking())
         {
             return false;
         }
         if (hasTileEntity(world.getBlockMetadata(x, y, z)))
         {
             final BaseTE te = (BaseTE) world.getBlockTileEntity(x, y, z);
             if (te == null)
             {
                 CCMLogger.DEFAULT_LOGGER.debug("TileEntity at %s, %s, %s, was null", x, y, z);
                 return false;
             }
             GuiHandler.openGui(Archive.COMPRESSOR, player, x, y, z);
            return true;
         }
         return false;
     }
 }
