 // Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
 // Jad home page: http://www.kpdus.com/jad.html
 // Decompiler options: packimports(3) braces deadcode 
 
 package net.minecraft.src;
 
 import java.util.ArrayList;
 import java.util.Random;
 
 import net.minecraft.src.forge.IShearable;
 import net.minecraft.src.forge.ITextureProvider;
 
 // Referenced classes of package net.minecraft.src:
 //            Block, Material, World, mod_Beer, 
 //            Item
 
 public class BlockCocoaLeaves extends BlockLeavesBase implements ITextureProvider, IShearable
 {
 
     protected BlockCocoaLeaves(int i, int j)
     {
     	
         super(i, j, Material.leaves, false);
         baseIndexInPNG = 6;
         this.setTickRandomly(true);
     }
 
     public int getBlockColor()
     {
         double var1 = 0.5D;
         double var3 = 1.0D;
         return ColorizerFoliage.getFoliageColor(var1, var3);
     }
 
     /**
      * Returns the color this block should be rendered. Used by leaves.
      */
     public int getRenderColor(int par1)
     {
         return (par1 & 3) == 1 ? ColorizerFoliage.getFoliageColorPine() : ((par1 & 3) == 2 ? ColorizerFoliage.getFoliageColorBirch() : ColorizerFoliage.getFoliageColorBasic());
     }
 
     /**
      * Returns a integer with hex for 0xrrggbb with this color multiplied against the blocks color. Note only called
      * when first determining what to render.
      */
     public int colorMultiplier(IBlockAccess par1IBlockAccess, int par2, int par3, int par4)
     {
         int var5 = par1IBlockAccess.getBlockMetadata(par2, par3, par4);
 
         if ((var5 & 3) == 1)
         {
             return ColorizerFoliage.getFoliageColorPine();
         }
         else if ((var5 & 3) == 2)
         {
             return ColorizerFoliage.getFoliageColorBirch();
         }
         else
         {
             int var6 = 0;
             int var7 = 0;
             int var8 = 0;
 
             for (int var9 = -1; var9 <= 1; ++var9)
             {
                 for (int var10 = -1; var10 <= 1; ++var10)
                 {
                     int var11 = par1IBlockAccess.getBiomeGenForCoords(par2 + var10, par4 + var9).getBiomeFoliageColor();
                     var6 += (var11 & 16711680) >> 16;
                     var7 += (var11 & 65280) >> 8;
                     var8 += var11 & 255;
                 }
             }
 
             return (var6 / 9 & 255) << 16 | (var7 / 9 & 255) << 8 | var8 / 9 & 255;
         }
     }
 
     public boolean isOpaqueCube()
     {
         return !this.graphicsLevel;
     }
 
 
 
     /**
      * Pass true to draw this block using fancy graphics, or false for fast graphics.
      */
 //    public void setGraphicsLevel(boolean par1)
 //    {
 //        this.graphicsLevel = par1;
 //        this.blockIndexInTexture = this.baseIndexInPNG + (par1 ? 0 : 1);
 //    }
 
     public boolean shouldSideBeRendered(IBlockAccess iblockaccess, int i, int j, int k, int l)
     {
         int i1 = iblockaccess.getBlockId(i, j, k);
         return true;
     }
 
     public void onBlockRemoval(World world, int i, int j, int k)
     {
         int l = 1;
         int i1 = l + 1;
         if(world.checkChunksExist(i - i1, j - i1, k - i1, i + i1, j + i1, k + i1))
         {
             for(int j1 = -l; j1 <= l; j1++)
             {
                 for(int k1 = -l; k1 <= l; k1++)
                 {
                     for(int l1 = -l; l1 <= l; l1++)
                     {
                         int i2 = world.getBlockId(i + j1, j + k1, k + l1);
                         if(i2 == EasterBlocks.CocoaLeaves.blockID || i2 == EasterBlocks.CocoaLeavesEmpty.blockID)
                         {
                             int j2 = world.getBlockMetadata(i + j1, j + k1, k + l1);
                             world.setBlockMetadata(i + j1, j + k1, k + l1, j2 | 8);
                         }
                     }
 
                 }
 
             }
 
         }
     }
 
     public void updateTick(World par1World, int par2, int par3, int par4, Random par5Random)
     {
         if (!par1World.isRemote)
         {
             int var6 = par1World.getBlockMetadata(par2, par3, par4);
 
             if ((var6 & 8) != 0 && (var6 & 4) == 0)
             {
                 byte var7 = 4;
                 int var8 = var7 + 1;
                 byte var9 = 32;
                 int var10 = var9 * var9;
                 int var11 = var9 / 2;
 
                 if (this.adjacentTreeBlocks == null)
                 {
                     this.adjacentTreeBlocks = new int[var9 * var9 * var9];
                 }
 
                 int var12;
 
                 if (par1World.checkChunksExist(par2 - var8, par3 - var8, par4 - var8, par2 + var8, par3 + var8, par4 + var8))
                 {
                     int var13;
                     int var14;
                     int var15;
 
                     for (var12 = -var7; var12 <= var7; ++var12)
                     {
                         for (var13 = -var7; var13 <= var7; ++var13)
                         {
                             for (var14 = -var7; var14 <= var7; ++var14)
                             {
                                 var15 = par1World.getBlockId(par2 + var12, par3 + var13, par4 + var14);
 
                                 if (var15 == EasterBlocks.CocoaLog.blockID)
                                 {
                                     this.adjacentTreeBlocks[(var12 + var11) * var10 + (var13 + var11) * var9 + var14 + var11] = 0;
                                 }
                                 else if (var15 == EasterBlocks.CocoaLeaves.blockID || var15 == EasterBlocks.CocoaLeavesEmpty.blockID)
                                 {
                                     this.adjacentTreeBlocks[(var12 + var11) * var10 + (var13 + var11) * var9 + var14 + var11] = -2;
                                 }
                                 else
                                 {
                                     this.adjacentTreeBlocks[(var12 + var11) * var10 + (var13 + var11) * var9 + var14 + var11] = -1;
                                 }
                             }
                         }
                     }
 
                     for (var12 = 1; var12 <= 4; ++var12)
                     {
                         for (var13 = -var7; var13 <= var7; ++var13)
                         {
                             for (var14 = -var7; var14 <= var7; ++var14)
                             {
                                 for (var15 = -var7; var15 <= var7; ++var15)
                                 {
                                     if (this.adjacentTreeBlocks[(var13 + var11) * var10 + (var14 + var11) * var9 + var15 + var11] == var12 - 1)
                                     {
                                         if (this.adjacentTreeBlocks[(var13 + var11 - 1) * var10 + (var14 + var11) * var9 + var15 + var11] == -2)
                                         {
                                             this.adjacentTreeBlocks[(var13 + var11 - 1) * var10 + (var14 + var11) * var9 + var15 + var11] = var12;
                                         }
 
                                         if (this.adjacentTreeBlocks[(var13 + var11 + 1) * var10 + (var14 + var11) * var9 + var15 + var11] == -2)
                                         {
                                             this.adjacentTreeBlocks[(var13 + var11 + 1) * var10 + (var14 + var11) * var9 + var15 + var11] = var12;
                                         }
 
                                         if (this.adjacentTreeBlocks[(var13 + var11) * var10 + (var14 + var11 - 1) * var9 + var15 + var11] == -2)
                                         {
                                             this.adjacentTreeBlocks[(var13 + var11) * var10 + (var14 + var11 - 1) * var9 + var15 + var11] = var12;
                                         }
 
                                         if (this.adjacentTreeBlocks[(var13 + var11) * var10 + (var14 + var11 + 1) * var9 + var15 + var11] == -2)
                                         {
                                             this.adjacentTreeBlocks[(var13 + var11) * var10 + (var14 + var11 + 1) * var9 + var15 + var11] = var12;
                                         }
 
                                         if (this.adjacentTreeBlocks[(var13 + var11) * var10 + (var14 + var11) * var9 + (var15 + var11 - 1)] == -2)
                                         {
                                             this.adjacentTreeBlocks[(var13 + var11) * var10 + (var14 + var11) * var9 + (var15 + var11 - 1)] = var12;
                                         }
 
                                         if (this.adjacentTreeBlocks[(var13 + var11) * var10 + (var14 + var11) * var9 + var15 + var11 + 1] == -2)
                                         {
                                             this.adjacentTreeBlocks[(var13 + var11) * var10 + (var14 + var11) * var9 + var15 + var11 + 1] = var12;
                                         }
                                     }
                                 }
                             }
                         }
                     }
                 }
 
                 var12 = this.adjacentTreeBlocks[var11 * var10 + var11 * var9 + var11];
 
                 if (var12 >= 0)
                 {
                     par1World.setBlockMetadata(par2, par3, par4, var6 & -9);
                 }
                 else
                 {
                     this.removeLeaves(par1World, par2, par3, par4);
                 }
             }
         }
     }
 
     private void removeLeaves(World world, int i, int j, int k)
     {
         dropBlockAsItem(world, i, j, k, world.getBlockMetadata(i, j, k), 0);
         world.setBlockWithNotify(i, j, k, 0);
     }
     public int quantityDropped(Random par1Random)
     {
         return 1;
     }
     
     public int idDropped(int i, Random random)
     {
         return EasterBlocks.CocoaSapling.blockID;
     }
     
     /**
      * Drops the block items with a specified chance of dropping the specified items
      */
     public void dropBlockAsItemWithChance(World par1World, int par2, int par3, int par4, int par5, float par6, int par7)
     {
         if (!par1World.isRemote)
         {
         	if(this.blockID == EasterBlocks.CocoaLeaves.blockID){
         		if (par1World.rand.nextInt(8) == 0)
 	            {
 	                this.dropBlockAsItem_do(par1World, par2, par3, par4, new ItemStack(Item.dyePowder, 1, 3));
 	            }
        	} else {
        		if (par1World.rand.nextInt(10) == 0)
 	            {
 	                this.dropBlockAsItem_do(par1World, par2, par3, par4, new ItemStack(EasterBlocks.CocoaSapling, 1));
 	            }
         	}
         }
     }
     
     @Override
 	public String getTextureFile() {
 		return "/easter/EasterBlocks.png";
 	}
     
     private int baseIndexInPNG;
     int adjacentTreeBlocks[];
     
 	@Override
 	public boolean isShearable(ItemStack item, World world, int x, int y, int z) {
 		return true;
 	}
 
 	@Override
     public ArrayList<ItemStack> onSheared(ItemStack item, World world, int x, int y, int z, int fortune) 
     {
         ArrayList<ItemStack> ret = new ArrayList<ItemStack>();
         ret.add(new ItemStack(this, 1, world.getBlockMetadata(x, y, z) & 3));
         return ret;
     }
 }
