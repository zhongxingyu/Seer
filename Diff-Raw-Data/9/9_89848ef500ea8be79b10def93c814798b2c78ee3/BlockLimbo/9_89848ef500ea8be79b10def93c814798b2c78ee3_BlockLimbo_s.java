 package com.github.ubiquitousspice.dreamdimension.blocks;
 
 import com.github.ubiquitousspice.dreamdimension.DreamDimension;
 import com.github.ubiquitousspice.dreamdimension.client.ProxyClient;
 import com.github.ubiquitousspice.dreamdimension.handlers.DreamManager;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 import net.minecraft.block.Block;
 import net.minecraft.block.BlockContainer;
 import net.minecraft.block.material.Material;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.entity.Entity;
 import net.minecraft.entity.item.EntityItem;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.entity.player.EntityPlayerMP;
 import net.minecraft.item.ItemStack;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.tileentity.TileEntityNote;
 import net.minecraft.util.AxisAlignedBB;
 import net.minecraft.util.Facing;
 import net.minecraft.util.Icon;
 import net.minecraft.world.IBlockAccess;
 import net.minecraft.world.World;
 
 import java.util.List;
 
 public class BlockLimbo extends BlockContainer
 {
     @SideOnly(Side.CLIENT)
     public static Icon outsideIcon;
     @SideOnly(Side.CLIENT)
     public static Icon topIcon;
     @SideOnly(Side.CLIENT)
     public static Icon insideIcon;
 
     public BlockLimbo(int par1)
     {
         super(par1, Material.iron);
         this.setCreativeTab(DreamDimension.tabDream);
         this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
     }
 
     @Override
     public void setBlockBoundsBasedOnState(IBlockAccess par1IBlockAccess, int par2, int par3, int par4)
     {
         this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
     }
 
     @Override
     public void addCollisionBoxesToList(World par1World, int par2, int par3, int par4, AxisAlignedBB par5AxisAlignedBB, List par6List, Entity par7Entity)
     {
         this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.625F, 1.0F);
         super.addCollisionBoxesToList(par1World, par2, par3, par4, par5AxisAlignedBB, par6List, par7Entity);
         float f = 0.125F;
         this.setBlockBounds(0.0F, 0.0F, 0.0F, f, 1.0F, 1.0F);
         super.addCollisionBoxesToList(par1World, par2, par3, par4, par5AxisAlignedBB, par6List, par7Entity);
         this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, f);
         super.addCollisionBoxesToList(par1World, par2, par3, par4, par5AxisAlignedBB, par6List, par7Entity);
         this.setBlockBounds(1.0F - f, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
         super.addCollisionBoxesToList(par1World, par2, par3, par4, par5AxisAlignedBB, par6List, par7Entity);
         this.setBlockBounds(0.0F, 0.0F, 1.0F - f, 1.0F, 1.0F, 1.0F);
         super.addCollisionBoxesToList(par1World, par2, par3, par4, par5AxisAlignedBB, par6List, par7Entity);
         this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
     }
 
     @Override
     public int onBlockPlaced(World par1World, int par2, int par3, int par4, int par5, float par6, float par7, float par8, int par9)
     {
         int j1 = Facing.oppositeSide[par5];
 
         if (j1 == 1)
         {
             j1 = 0;
         }
 
         return j1;
     }
 
     @Override
     public boolean canPlaceBlockAt(World world, int x, int y, int z)
     {
         return world.provider.dimensionId == DreamDimension.dimensionID;
     }
 
     @Override
     public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity)
     {
         if (entity instanceof EntityItem)
         {
             // suck item.
             TileEntityLimbo te = (TileEntityLimbo) world.getBlockTileEntity(x, y, z);
             te.addItem(((EntityItem) entity).getEntityItem());
         }
     }
 
     @Override
     public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int par6, float par7, float par8, float par9)
     {
         if (world.isRemote)
         {
             return true;
         }
 
         // get spew stuff
         TileEntityLimbo te = (TileEntityLimbo) world.getBlockTileEntity(x, y, z);
         List<ItemStack> list = te.getItems();
 
        // teleport.
        DreamManager.kickDreamer((EntityPlayerMP)player, 300, list);

         //destroy block.
         world.setBlockToAir(x, y, z);
 
         return true;
     }
 
 
     @Override
     @SideOnly(Side.CLIENT)
     public int getRenderType()
     {
         return ProxyClient.renderID;
     }
 
     @Override
     public boolean renderAsNormalBlock()
     {
         return false;
     }
 
     @Override
     public boolean isOpaqueCube()
     {
         return false;
     }
 
     @Override
     @SideOnly(Side.CLIENT)
     public boolean shouldSideBeRendered(IBlockAccess par1IBlockAccess, int par2, int par3, int par4, int par5)
     {
         return true;
     }
 
     @Override
     @SideOnly(Side.CLIENT)
     public Icon getIcon(int par1, int par2)
     {
         return par1 == 1 ? this.topIcon : this.outsideIcon;
     }
 
     @Override
     @SideOnly(Side.CLIENT)
     public void registerIcons(IconRegister par1IconRegister)
     {
         this.outsideIcon = par1IconRegister.registerIcon("limbo_outside");
         this.topIcon = par1IconRegister.registerIcon("limbo_top");
         this.insideIcon = par1IconRegister.registerIcon("limbo_inside");
     }
 
     @Override
     @SideOnly(Side.CLIENT)
     public String getItemIconName()
     {
         return "limbo";
     }
 
     @Override
     public TileEntity createNewTileEntity(World world)
     {
         return new TileEntityLimbo();
     }
 }
