 package com.teammetallurgy.agriculture.crops;
 
 import java.util.Random;
 
 import net.minecraft.block.Block;
 import net.minecraft.block.BlockFlower;
 import net.minecraft.block.material.Material;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.item.ItemStack;
 import net.minecraft.util.Icon;
 import net.minecraft.util.MathHelper;
 import net.minecraft.world.World;
 import net.minecraftforge.common.ForgeDirection;
 import net.minecraftforge.common.IPlantable;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 public class BlockCrop extends BlockFlower {
 
     private final ItemStack drop;
     private final float growthRate = 0.5f;
 
     private Icon[] iconArray;
 
     public BlockCrop(final int par1, final ItemStack drop)
     {
         super(par1);
         this.drop = drop;
     }
 
     public BlockCrop(final int par1, final Material par2Material, final ItemStack drop)
     {
         super(par1, par2Material);
         this.drop = drop;
     }
 
     @Override
     public boolean canPlaceBlockAt(final World par1World, final int par2, final int par3, final int par4)
     {
         final int id = par1World.getBlockId(par2, par3 - 1, par4);
         return id == Block.tilledField.blockID;
     }
 
     @Override
     public boolean canSustainPlant(final World world, final int x, final int y, final int z, final ForgeDirection direction, final IPlantable plant)
     {
         return true;
     }
 
     @Override
     public int damageDropped(final int par1)
     {
         return drop.getItemDamage();
     }
 
     public boolean fertilize(final World par1World, final int par2, final int par3, final int par4)
     {
         final int meta = par1World.getBlockMetadata(par2, par3, par4);
 
         if (meta >= 6) { return false; }
 
         int l = meta + MathHelper.getRandomIntegerInRange(par1World.rand, 2, 5);
 
        if (l > 6)
         {
             l = 6;
         }
 
         par1World.setBlockMetadataWithNotify(par2, par3, par4, l, 2);
 
         return true;
     }
 
     @SideOnly(Side.CLIENT)
     @Override
     public Icon getIcon(final int par1, int par2)
     {
         if (par2 < 0 || par2 > 7)
         {
             par2 = 7;
         }
 
         return iconArray[par2];
     }
 
     @Override
     public int idDropped(final int par1, final Random par2Random, final int par3)
     {
         return drop.itemID;
     }
 
     @Override
     public int quantityDropped(final Random par1Random)
     {
         return 2 + par1Random.nextInt(3);
     }
 
     @Override
     @SideOnly(Side.CLIENT)
     public void registerIcons(final IconRegister par1IconRegister)
     {
         iconArray = new Icon[8];
 
         for (int i = 0; i < iconArray.length; ++i)
         {
             iconArray[i] = par1IconRegister.registerIcon(getTextureName() + "_stage_" + i);
         }
     }
 
     @Override
     public void updateTick(final World par1World, final int par2, final int par3, final int par4, final Random par5Random)
     {
         super.updateTick(par1World, par2, par3, par4, par5Random);
 
         if (par1World.getBlockLightValue(par2, par3 + 1, par4) >= 9)
         {
             int l = par1World.getBlockMetadata(par2, par3, par4);
             if (l < 6)
             {
                 if (par5Random.nextInt((int) (25.0F / growthRate) + 1) == 0)
                 {
                     ++l;
                     par1World.setBlockMetadataWithNotify(par2, par3, par4, l, 2);
                 }
             }
         }
     }
 
 }
