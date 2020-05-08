 package net.minecraft.src.Reactioncraft;
 import java.util.ArrayList;
 import java.util.Random;
 
 import net.minecraft.src.AxisAlignedBB;
 import net.minecraft.src.Block;
 import net.minecraft.src.DamageSource;
 import net.minecraft.src.Entity;
 import net.minecraft.src.ItemStack;
 import net.minecraft.src.Material;
 import net.minecraft.src.World;
 import net.minecraft.src.mod_ReactionCraft;
 import net.minecraft.src.forge.*;
 
public class CoralPlant extends Block implements ITextureProvider
 {
 
    public CoralPlant(int i, int j)
     {
         super(i, Material.water);
         blockIndexInTexture = j;
         float f = 0.375F;
         setBlockBounds(0.5F - f, 0.0F, 0.5F - f, 0.5F + f, 1.0F, 0.5F + f);
         this.setTickRandomly(true);
     }
     
     /**
      * Checks to see if its valid to put this block at the specified coordinates. Args: world, x, y, z
      */
     public boolean canPlaceBlockAt(World par1World, int par2, int par3, int par4)
     {
         return super.canPlaceBlockAt(par1World, par2, par3, par4) && this.canThisPlantGrowOnThisBlockID(par1World.getBlockId(par2, par3 - 1, par4));
     }
     protected final void checkFlowerChange(World par1World, int par2, int par3, int par4)
     {
         if (!this.canBlockStay(par1World, par2, par3, par4))
         {
             this.dropBlockAsItem(par1World, par2, par3, par4, par1World.getBlockMetadata(par2, par3, par4), 0);
             par1World.setBlockWithNotify(par2, par3, par4, 0);
         }
     }
 
     /**
      * Can this block stay at this position.  Similar to canPlaceBlockAt except gets checked often with plants.
      */
     public boolean canBlockStay(World par1World, int par2, int par3, int par4)
     {
         return (par1World.getFullBlockLightValue(par2, par3, par4) >= 8 || par1World.canBlockSeeTheSky(par2, par3, par4)) && this.canThisPlantGrowOnThisBlockID(par1World.getBlockId(par2, par3 - 1, par4));
     }
     public void onEntityCollidedWithBlock(World par1World, int par2, int par3, int par4, Entity par5Entity)
     {
         par5Entity.attackEntityFrom(DamageSource.cactus, 1);
     }
     
     protected boolean canThisPlantGrowOnThisBlockID(int par1)
     {
         return par1 == Block.dirt.blockID || par1 == Block.sand.blockID || par1 == Block.blockClay.blockID || par1 == mod_ReactionCraft.DarkSand.blockID || par1 == mod_ReactionCraft.CoralBlock1.blockID || par1 == mod_ReactionCraft.CoralBlock2.blockID || par1 == mod_ReactionCraft.CoralBlock3.blockID;
     }
 
   public boolean canBlockStay(World world, int i, int j, int k)
     {
         return canPlaceBlockAt(world, i, j, k);
     }
 
     public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int i, int j, int k)
     {
         return null;
     }
 
     public int idDropped(int i, Random random)
     {
         return blockID;
     }
 
     public boolean isOpaqueCube()
     {
         return false;
     }
 
     public boolean renderAsNormalBlock()
     {
         return false;
     }
 
     public int getRenderType()
     {
         return 1;
     }
     
     public void randomDisplayTick(World par1World, int par2, int par3, int par4, Random par5Random)
     {
         int var6 = par1World.getBlockMetadata(par2, par3, par4);
         double var7 = (double)((float)par2 + 0.5F);
         double var9 = (double)((float)par3 + 0.7F);
         double var11 = (double)((float)par4 + 0.5F);
         double var13 = 0.2199999988079071D;
         double var15 = 0.27000001072883606D;
 
         if (var6 == 1)
         {
             par1World.spawnParticle("bubble", var7 - var15, var9 + var13, var11, 0.0D, 0.0D, 0.0D);
         }
         else if (var6 == 2)
         {
             par1World.spawnParticle("bubble", var7 + var15, var9 + var13, var11, 0.0D, 0.0D, 0.0D);
         }
         else if (var6 == 3)
         {
             par1World.spawnParticle("bubble", var7, var9 + var13, var11 - var15, 0.0D, 0.0D, 0.0D);
         }
         else if (var6 == 4)
         {
             par1World.spawnParticle("bubble", var7, var9 + var13, var11 + var15, 0.0D, 0.0D, 0.0D);    
         }
         else
         {
             par1World.spawnParticle("bubble", var7, var9, var11, 0.0D, 0.0D, 0.0D);
         }
     }
     
     public void addCreativeItems(ArrayList itemList)
     {
             itemList.add(new ItemStack(this));
     }
 
 	@Override
 	public String getTextureFile()
 	{
 		return "/reactioncraft/Blocks.png";
 	}
 }
