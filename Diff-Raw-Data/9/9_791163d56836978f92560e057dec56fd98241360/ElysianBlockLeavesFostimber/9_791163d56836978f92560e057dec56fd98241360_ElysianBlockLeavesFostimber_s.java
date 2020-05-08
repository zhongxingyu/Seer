 package mods.elysium.block;
 
 import java.util.ArrayList;
 import java.util.Random;
 
 import me.dawars.CraftingPillars.CraftingPillars;
 import mods.elysium.BlockItemIDs;
 import mods.elysium.Elysium;
 import mods.elysium.client.particle.ElysianEntityFX;
 import net.minecraft.block.Block;
 import net.minecraft.block.material.Material;
 import net.minecraft.client.Minecraft;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.ItemStack;
 import net.minecraft.src.ModLoader;
 import net.minecraft.util.Icon;
 import net.minecraft.world.IBlockAccess;
 import net.minecraft.world.World;
 import net.minecraftforge.common.IShearable;
 import cpw.mods.fml.client.FMLClientHandler;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 public class ElysianBlockLeavesFostimber extends ElysianBlock implements IShearable
 {
    @SideOnly(Side.CLIENT)
     public Icon[] iconArray = new Icon[2];
 	int[] adjacentTreeBlocks;
 
 	public ElysianBlockLeavesFostimber(int id, Material mat)
 	{
 		super(id, mat);
         this.setTickRandomly(true);
 	}
 
	@Override
 	/**
      * From the specified side and block metadata retrieves the blocks texture. Args: side, metadata
     */
     public Icon getIcon(int par1, int par2)
     {
 		return FMLClientHandler.instance().getClient().isFancyGraphicsEnabled() ? this.iconArray[1] : this.iconArray[0];
     }
 
 	@Override
     /**
      * ejects contained items into the world, and notifies neighbours of an update, as appropriate
      */
     public void breakBlock(World par1World, int par2, int par3, int par4, int par5, int par6)
     {
         byte b0 = 1;
         int j1 = b0 + 1;
 
         if (par1World.checkChunksExist(par2 - j1, par3 - j1, par4 - j1, par2 + j1, par3 + j1, par4 + j1))
         {
             for (int k1 = -b0; k1 <= b0; ++k1)
             {
                 for (int l1 = -b0; l1 <= b0; ++l1)
                 {
                     for (int i2 = -b0; i2 <= b0; ++i2)
                     {
                         int j2 = par1World.getBlockId(par2 + k1, par3 + l1, par4 + i2);
 
                         if (Block.blocksList[j2] != null)
                         {
                             Block.blocksList[j2].beginLeavesDecay(par1World, par2 + k1, par3 + l1, par4 + i2);
                         }
                     }
                 }
             }
         }
     }
 	
 	@Override
     /**
      * Ticks the block if it's been scheduled
      */
     public void updateTick(World par1World, int par2, int par3, int par4, Random par5Random)
     {
         if (!par1World.isRemote)
         {
             int l = par1World.getBlockMetadata(par2, par3, par4);
 
             if ((l & 8) != 0 && (l & 4) == 0)
             {
                 byte b0 = 4;
                 int i1 = b0 + 1;
                 byte b1 = 32;
                 int j1 = b1 * b1;
                 int k1 = b1 / 2;
                 
                 if (this.adjacentTreeBlocks == null)
                 {
                     this.adjacentTreeBlocks = new int[b1 * b1 * b1];
                 }
 
                 int l1;
 
                 if (par1World.checkChunksExist(par2 - i1, par3 - i1, par4 - i1, par2 + i1, par3 + i1, par4 + i1))
                 {
                     int i2;
                     int j2;
                     int k2;
 
                     for (l1 = -b0; l1 <= b0; ++l1)
                     {
                         for (i2 = -b0; i2 <= b0; ++i2)
                         {
                             for (j2 = -b0; j2 <= b0; ++j2)
                             {
                                 k2 = par1World.getBlockId(par2 + l1, par3 + i2, par4 + j2);
 
                                 Block block = Block.blocksList[k2];
 
                                 if (block != null && block.canSustainLeaves(par1World, par2 + l1, par3 + i2, par4 + j2))
                                 {
                                     this.adjacentTreeBlocks[(l1 + k1) * j1 + (i2 + k1) * b1 + j2 + k1] = 0;
                                 }
                                 else if (block != null && block.isLeaves(par1World, par2 + l1, par3 + i2, par4 + j2))
                                 {
                                     this.adjacentTreeBlocks[(l1 + k1) * j1 + (i2 + k1) * b1 + j2 + k1] = -2;
                                 }
                                 else
                                 {
                                     this.adjacentTreeBlocks[(l1 + k1) * j1 + (i2 + k1) * b1 + j2 + k1] = -1;
                                 }
                             }
                         }
                     }
 
                     for (l1 = 1; l1 <= 4; ++l1)
                     {
                         for (i2 = -b0; i2 <= b0; ++i2)
                         {
                             for (j2 = -b0; j2 <= b0; ++j2)
                             {
                                 for (k2 = -b0; k2 <= b0; ++k2)
                                 {
                                     if (this.adjacentTreeBlocks[(i2 + k1) * j1 + (j2 + k1) * b1 + k2 + k1] == l1 - 1)
                                     {
                                         if (this.adjacentTreeBlocks[(i2 + k1 - 1) * j1 + (j2 + k1) * b1 + k2 + k1] == -2)
                                         {
                                             this.adjacentTreeBlocks[(i2 + k1 - 1) * j1 + (j2 + k1) * b1 + k2 + k1] = l1;
                                         }
 
                                         if (this.adjacentTreeBlocks[(i2 + k1 + 1) * j1 + (j2 + k1) * b1 + k2 + k1] == -2)
                                         {
                                             this.adjacentTreeBlocks[(i2 + k1 + 1) * j1 + (j2 + k1) * b1 + k2 + k1] = l1;
                                         }
 
                                         if (this.adjacentTreeBlocks[(i2 + k1) * j1 + (j2 + k1 - 1) * b1 + k2 + k1] == -2)
                                         {
                                             this.adjacentTreeBlocks[(i2 + k1) * j1 + (j2 + k1 - 1) * b1 + k2 + k1] = l1;
                                         }
 
                                         if (this.adjacentTreeBlocks[(i2 + k1) * j1 + (j2 + k1 + 1) * b1 + k2 + k1] == -2)
                                         {
                                             this.adjacentTreeBlocks[(i2 + k1) * j1 + (j2 + k1 + 1) * b1 + k2 + k1] = l1;
                                         }
 
                                         if (this.adjacentTreeBlocks[(i2 + k1) * j1 + (j2 + k1) * b1 + (k2 + k1 - 1)] == -2)
                                         {
                                             this.adjacentTreeBlocks[(i2 + k1) * j1 + (j2 + k1) * b1 + (k2 + k1 - 1)] = l1;
                                         }
 
                                         if (this.adjacentTreeBlocks[(i2 + k1) * j1 + (j2 + k1) * b1 + k2 + k1 + 1] == -2)
                                         {
                                             this.adjacentTreeBlocks[(i2 + k1) * j1 + (j2 + k1) * b1 + k2 + k1 + 1] = l1;
                                         }
                                     }
                                 }
                             }
                         }
                     }
                 }
 
                 l1 = this.adjacentTreeBlocks[k1 * j1 + k1 * b1 + k1];
 
                 if (l1 >= 0)
                 {
                     par1World.setBlockMetadataWithNotify(par2, par3, par4, l & -9, 4);
                 }
                 else
                 {
                     this.removeLeaves(par1World, par2, par3, par4);
                 }
             }
         }
     }
 	
 	@Override
     @SideOnly(Side.CLIENT)
     /**
      * A randomly called display update to be able to add particles or other items for display
      */
     public void randomDisplayTick(World world, int x, int y, int z, Random random)
     {
 		if(random.nextInt(10) == 0)
 		{
 			ElysianEntityFX entityfx = new ElysianEntityFX(world, x+random.nextDouble()*2-0.5D, y+random.nextDouble()*2-0.5D, z+random.nextDouble()*2-0.5D, random.nextDouble()/10-0.05D, random.nextDouble()/10-0.05D, random.nextDouble()/10-0.05D);
 			entityfx.setRBGColorF(1, 1, 1);
 			entityfx.multipleParticleScaleBy(0.25F);
 			entityfx.setBrightness(200);
 			entityfx.setTextureFile("elysium:/textures/misc/particles/fost.png");
 			FMLClientHandler.instance().getClient().effectRenderer.addEffect(entityfx);
 		}
 		
         if(world.canLightningStrikeAt(x, y + 1, z) && !world.doesBlockHaveSolidTopSurface(x, y - 1, z) && random.nextInt(15) == 1)
         {
             double d0 = (double)((float)x + random.nextFloat());
             double d1 = (double)y - 0.05D;
             double d2 = (double)((float)z + random.nextFloat());
             world.spawnParticle("dripWater", d0, d1, d2, 0.0D, 0.0D, 0.0D);
         }
     }
 
     private void removeLeaves(World par1World, int par2, int par3, int par4)
     {
         this.dropBlockAsItem(par1World, par2, par3, par4, par1World.getBlockMetadata(par2, par3, par4), 0);
         par1World.setBlockToAir(par2, par3, par4);
     }
     
     @Override
     /**
      * Returns the quantity of items to drop on block destruction.
      */
     public int quantityDropped(Random par1Random)
     {
         return par1Random.nextInt(20) == 0 ? 1 : 0;
     }
     
     @Override
     /**
      * Returns the ID of the items to drop on destruction.
      */
     public int idDropped(int par1, Random par2Random, int par3)
     {
         return Elysium.blockSaplingFostimber.blockID;
     }
     
     @Override
     /**
      * Drops the block items with a specified chance of dropping the specified items
      */
     public void dropBlockAsItemWithChance(World par1World, int par2, int par3, int par4, int par5, float par6, int par7)
     {
         if (!par1World.isRemote)
         {
             int j1 = 20;
 
             if ((par5 & 3) == 3)
             {
                 j1 = 40;
             }
 
             if (par7 > 0)
             {
                 j1 -= 2 << par7;
 
                 if (j1 < 10)
                 {
                     j1 = 10;
                 }
             }
 
             if (par1World.rand.nextInt(j1) == 0)
             {
                 int k1 = this.idDropped(par5, par1World.rand, par7);
                 this.dropBlockAsItem_do(par1World, par2, par3, par4, new ItemStack(k1, 1, this.damageDropped(par5)));
             }
 
             j1 = 200;
 
             if (par7 > 0)
             {
                 j1 -= 10 << par7;
 
                 if (j1 < 40)
                 {
                     j1 = 40;
                 }
             }
 
 //            if ((par5 & 3) == 0 && par1World.rand.nextInt(j1) == 0)
 //            {
 //                this.dropBlockAsItem_do(par1World, par2, par3, par4, new ItemStack(Item.appleRed, 1, 0));
 //            }
         }
     }
     
     @Override
 	public void beginLeavesDecay(World world, int x, int y, int z)
 	{
 		world.setBlockMetadataWithNotify(x, y, z, world.getBlockMetadata(x, y, z) | 8, 4);
 	}
     
     @Override
     /**
      * Called when the player destroys a block with an item that can harvest it. (i, j, k) are the coordinates of the
      * block and l is the block's subtype/damage.
      */
     public void harvestBlock(World par1World, EntityPlayer par2EntityPlayer, int par3, int par4, int par5, int par6)
     {
         super.harvestBlock(par1World, par2EntityPlayer, par3, par4, par5, par6);
     }
     
     @Override
     /**
      * Determines the damage on the item the block drops. Used in cloth and wood.
      */
     public int damageDropped(int par1)
     {
         return par1 & 3;
     }
     
     @Override
     @SideOnly(Side.CLIENT)
     /**
      * Is this block (a) opaque and (b) a full 1m cube?  This determines whether or not to render the shared face of two
      * adjacent blocks and also whether the player can attach torches, redstone wire, etc to this block.
      */
     public boolean isOpaqueCube()
     {
         return !FMLClientHandler.instance().getClient().isFancyGraphicsEnabled();
     }
     
     @Override
     @SideOnly(Side.CLIENT)
     /**
      * When this method is called, your block should register all the icons it needs with the given IconRegister. This
      * is the only chance you get to register icons.
      */
     public void registerIcons(IconRegister registerIcon)
     {
     	this.iconArray[0] = registerIcon.registerIcon(Elysium.id + ":fostimber_leaves_fast");
 		this.iconArray[1] = registerIcon.registerIcon(Elysium.id + ":fostimber_leaves");
     }
     
     @Override
     /**
      * Returns true if the given side of this block type should be rendered, if the adjacent block is at the given
      * coordinates.  Args: blockAccess, x, y, z, side
      */
     public boolean shouldSideBeRendered(IBlockAccess par1IBlockAccess, int par2, int par3, int par4, int par5)
     {
         int i1 = par1IBlockAccess.getBlockId(par2, par3, par4);
         return !FMLClientHandler.instance().getClient().isFancyGraphicsEnabled() && i1 == this.blockID ? false : super.shouldSideBeRendered(par1IBlockAccess, par2, par3, par4, par5);
     }
     
     @Override
     public boolean isShearable(ItemStack item, World world, int x, int y, int z)
     {
         return true;
     }
     
 
     @Override
     public boolean isLeaves(World world, int x, int y, int z)
     {
         return true;
     }
 
     @Override
     public ArrayList<ItemStack> onSheared(ItemStack item, World world, int x, int y, int z, int fortune)
     {
         ArrayList<ItemStack> ret = new ArrayList<ItemStack>();
         ret.add(new ItemStack(this, 1, world.getBlockMetadata(x, y, z) & 3));
         return ret;
     }
     
     @Override
 	public boolean canBeReplacedByLake()
 	{
 		return false;
 	}
 }
