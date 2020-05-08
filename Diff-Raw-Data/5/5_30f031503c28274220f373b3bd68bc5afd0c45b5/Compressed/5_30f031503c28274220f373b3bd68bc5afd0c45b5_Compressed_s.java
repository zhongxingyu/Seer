 package ccm.compresstion.block;
 
 import java.util.ArrayList;
 import java.util.Random;
 
 import net.minecraft.block.Block;
 import net.minecraft.block.BlockContainer;
 import net.minecraft.block.material.Material;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.entity.Entity;
 import net.minecraft.entity.EntityLivingBase;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.ItemStack;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.util.AxisAlignedBB;
 import net.minecraft.util.Icon;
 import net.minecraft.util.MovingObjectPosition;
 import net.minecraft.util.Vec3;
 import net.minecraft.world.Explosion;
 import net.minecraft.world.IBlockAccess;
 import net.minecraft.world.World;
 import ccm.compresstion.client.renderer.block.CompressedBlockRenderer;
 import ccm.compresstion.item.block.CompressedItemBlock;
 import ccm.compresstion.tileentity.CompressedTile;
 import ccm.compresstion.utils.lib.Archive;
 import ccm.nucleum.omnium.utils.handler.TileHandler;
 import ccm.nucleum.omnium.utils.helper.NBTHelper;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 public class Compressed extends BlockContainer
 {
     public static final String name = "Compressed";
 
     public Compressed(final int id)
     {
         super(id, Material.rock);
         setUnlocalizedName(name);
         GameRegistry.registerBlock(this, CompressedItemBlock.class, getUnlocalizedName());
         TileHandler.registerTile(name, CompressedTile.class);
     }
 
     @Override
     public TileEntity createNewTileEntity(final World world)
     {
         return TileHandler.getTileInstance(name);
     }
 
     @Override
     public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z)
     {
         ItemStack stack = new ItemStack(world.getBlockId(x, y, z), 1, world.getBlockMetadata(x, y, z));
         CompressedTile tile = (CompressedTile) world.getBlockTileEntity(x, y, z);
 
         NBTHelper.setInteger(stack, Archive.NBT_COMPRESSED_BLOCK_ID, tile.getBlock().blockID);
         NBTHelper.setByte(stack, Archive.NBT_COMPRESSED_BLOCK_META, tile.getMeta());
         return stack;
     }
 
     @Override
     public void registerIcons(IconRegister register)
     {
         for (CompressedType type : CompressedType.values())
         {
             type.setIcon(register.registerIcon("compresstion:condensedOverlay_" + type.ordinal()));
         }
     }
 
     @Override
     public void onBlockPlacedBy(final World world, final int x, final int y, final int z, final EntityLivingBase entity, final ItemStack item)
     {
         super.onBlockPlacedBy(world, x, y, z, entity, item);
         // createNewTileEntity(world);
         CompressedTile tile = (CompressedTile) world.getBlockTileEntity(x, y, z);
         tile.setBlockID(NBTHelper.getInteger(item, Archive.NBT_COMPRESSED_BLOCK_ID));
         tile.setBlockMeta(NBTHelper.getByte(item, Archive.NBT_COMPRESSED_BLOCK_META));
     }
 
     @Override
     public ArrayList<ItemStack> getBlockDropped(final World world, final int x, final int y, final int z, final int metadata, final int fortune)
     {
         ArrayList<ItemStack> ret = new ArrayList<ItemStack>();
         CompressedTile tile = (CompressedTile) world.getBlockTileEntity(x, y, z);
         int count = quantityDropped(metadata, fortune, world.rand);
         for (int i = 0; i < count; i++)
         {
             int id = idDropped(metadata, world.rand, fortune);
             if (id > 0)
             {
                 ItemStack stack = new ItemStack(id, 1, metadata);
                 NBTHelper.setInteger(stack, Archive.NBT_COMPRESSED_BLOCK_ID, tile.getBlock().blockID);
                 NBTHelper.setByte(stack, Archive.NBT_COMPRESSED_BLOCK_META, tile.getMeta());
                 ret.add(stack);
             }
         }
         return ret;
     }
 
     private static Block getBlock(final IBlockAccess world, final int x, final int y, final int z)
     {
         // TODO: Null checks, rather see sponge then a crash :P
         if (world.getBlockTileEntity(x, y, z) == null)
         {
             return Block.sponge;
         }
 
         Block block = ((CompressedTile) world.getBlockTileEntity(x, y, z)).getBlock();
 
         if (block == null)
         {
             return Block.sponge;
         }
 
         return block;
     }
 
     @Override
     public Icon getBlockTexture(final IBlockAccess world, final int x, final int y, final int z, final int side)
     {
         return getBlock(world, x, y, z).getBlockTexture(world, x, y, z, side);
     }
 
     /**
      * Called when the block is clicked by a player. Args: x, y, z, entityPlayer
      */
     @Override
     public void onBlockClicked(final World world, final int x, final int y, final int z, final EntityPlayer player)
     {
         getBlock(world, x, y, z).onBlockClicked(world, x, y, z, player);
     }
 
     @Override
     @SideOnly(Side.CLIENT)
     /**
      * A randomly called display update to be able to add particles or other items for display
      */
     public void randomDisplayTick(final World world, final int x, final int y, final int z, final Random rand)
     {
         getBlock(world, x, y, z).randomDisplayTick(world, x, y, z, rand);
     }
 
     @Override
     @SideOnly(Side.CLIENT)
     /**
      * Goes straight to getLightBrightnessForSkyBlocks for Blocks, does some fancy computing for Fluids
      */
     public int getMixedBrightnessForBlock(final IBlockAccess world, final int x, final int y, final int z)
     {
         return getBlock(world, x, y, z).getMixedBrightnessForBlock(world, x, y, z);
     }
 
     @Override
     @SideOnly(Side.CLIENT)
     /**
      * How bright to render this block based on the light its receiving. Args: iBlockAccess, x, y, z
      */
     public float getBlockBrightness(final IBlockAccess world, final int x, final int y, final int z)
     {
         return getBlock(world, x, y, z).getBlockBrightness(world, x, y, z);
     }
 
     /**
      * Can add to the passed in vector for a movement vector to be applied to the entity. Args: x, y, z, entity, vec3d
      */
     @Override
     public void velocityToAddToEntity(final World world, final int x, final int y, final int z, final Entity entity, final Vec3 par6Vec3)
     {
         getBlock(world, x, y, z).velocityToAddToEntity(world, x, y, z, entity, par6Vec3);
     }
 
     @Override
     @SideOnly(Side.CLIENT)
     /**
      * Returns the bounding box of the wired rectangular prism to render.
      */
     public AxisAlignedBB getSelectedBoundingBoxFromPool(final World world, final int x, final int y, final int z)
     {
         return getBlock(world, x, y, z).getSelectedBoundingBoxFromPool(world, x, y, z);
     }
 
     /**
      * Called on server worlds only when the block has been replaced by a different block ID, or the same block with a different metadata value, but before the new metadata value
      * is set. Args: World, x, y, z, old block ID, old metadata
      */
     @Override
     public void breakBlock(final World world, final int x, final int y, final int z, final int par5, final int par6)
     {
         getBlock(world, x, y, z).breakBlock(world, x, y, z, par5, par6);
     }
 
     /**
      * Called whenever an entity is walking on top of this block. Args: world, x, y, z, entity
      */
     @Override
     public void onEntityWalking(final World world, final int x, final int y, final int z, final Entity entity)
     {
         getBlock(world, x, y, z).onEntityWalking(world, x, y, z, entity);
     }
 
     /**
      * Ticks the block if it's been scheduled
      */
     @Override
     public void updateTick(final World world, final int x, final int y, final int z, final Random rand)
     {
         getBlock(world, x, y, z).updateTick(world, x, y, z, rand);
     }
 
     /**
      * Called upon block activation (right click on the block.)
      */
     @Override
     public boolean onBlockActivated(final World world, final int x, final int y, final int z, final EntityPlayer player, final int par6, final float par7, final float par8,
             final float par9)
     {
         return getBlock(world, x, y, z).onBlockActivated(world, x, y, z, player, 0, 0.0F, 0.0F, 0.0F);
     }
 
     /**
      * Called upon the block being destroyed by an explosion
      */
     @Override
     public void onBlockDestroyedByExplosion(final World world, final int x, final int y, final int z, final Explosion explosion)
     {
         getBlock(world, x, y, z).onBlockDestroyedByExplosion(world, x, y, z, explosion);
     }
 
     @Override
     public float getExplosionResistance(final Entity entity, final World world, final int x, final int y, final int z, final double explosionX, final double explosionY,
             final double explosionZ)
     {
         int metadata = world.getBlockMetadata(x, y, z);
        return getBlock(world, x, y, z).getExplosionResistance(entity) * ((int) Math.pow(2.0, 1 + metadata));
     }
 
     @Override
     public float getBlockHardness(final World world, final int x, final int y, final int z)
     {
         int metadata = world.getBlockMetadata(x, y, z);
        return getBlock(world, x, y, z).getBlockHardness(world, x, y, z) * ((int) Math.pow(2.0, 1 + metadata));
     }
 
     @Override
     public int getRenderType()
     {
         return CompressedBlockRenderer.id;
     }
 
     @Override
     public Icon getIcon(int side, int meta)
     {
         return Block.slowSand.getBlockTextureFromSide(side);
     }
 }
