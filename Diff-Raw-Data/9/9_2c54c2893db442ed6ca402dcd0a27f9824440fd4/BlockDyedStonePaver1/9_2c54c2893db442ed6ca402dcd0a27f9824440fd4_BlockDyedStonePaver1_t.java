 package gwydion0917.gwycraft.blocks;
 
 import gwydion0917.gwycraft.ConfigGwycraft;
 import java.util.List;
 
 import net.minecraft.block.Block;
 import net.minecraft.block.BlockBasePressurePlate;
 import net.minecraft.block.BlockHalfSlab;
 import net.minecraft.block.BlockPressurePlate;
 import net.minecraft.block.BlockSnow;
 import net.minecraft.block.material.Material;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.entity.Entity;
 import net.minecraft.item.ItemStack;
 import net.minecraft.util.AxisAlignedBB;
 import net.minecraft.util.Icon;
 import net.minecraft.world.IBlockAccess;
 import net.minecraft.world.World;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 import java.util.Random;
 
 public class BlockDyedStonePaver1 extends BlockHalfSlab {
 	/** The type of tree this slab came from. */
 	public static final String[] colorSlab = new String[] { "White", "Orange",
 			"Magenta", "Light Blue", "Yellow", "Light Green", "Pink",
 			"Dark Grey" };
 	public static final String[] slabTextures = new String[] {
 			"Gwycraft:stone_0", "Gwycraft:stone_1", "Gwycraft:stone_2",
 			"Gwycraft:stone_3", "Gwycraft:stone_4", "Gwycraft:stone_5",
 			"Gwycraft:stone_6", "Gwycraft:stone_7", "Gwycraft:stone_0",
 			"Gwycraft:stone_1", "Gwycraft:stone_2", "Gwycraft:stone_3",
 			"Gwycraft:stone_4", "Gwycraft:stone_5", "Gwycraft:stone_6",
 			"Gwycraft:stone_7" };
 	private Icon[] iconArray;
 
 	public BlockDyedStonePaver1(int id) {
 		super(id, false, Material.rock);
 		this.setLightOpacity(0);
 	}
 
     @Override
     public int idDropped(int par1, Random par2Random, int par3) {
        return ConfigGwycraft.blockDyedStonePaver1ID;
     }
 
     @SideOnly(Side.CLIENT)
     @Override
     public void getSubBlocks(int par1, CreativeTabs tab, List subItems) {
         for (int i = 0; i < 8; i++) {
             subItems.add(new ItemStack(this, 1, i));
         }
     }
 
     @Override
     /**
      * Updates the blocks bounds based on its current state. Args: world, x, y, z
      */
     public void setBlockBoundsBasedOnState(IBlockAccess par1IBlockAccess, int par2, int par3, int par4)
     {
         if (this.isDoubleSlab)
         {
             this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
         }
         else
         {
             boolean flag = (par1IBlockAccess.getBlockMetadata(par2, par3, par4) & 8) != 0;
 
             if (flag)
             {
                 this.setBlockBounds(0.0F, 0.9F, 0.0F, 1.0F, 1.0F, 1.0F);
             }
             else
             {
                 this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.1F, 1.0F);
             }
         }
     }
 
     @Override
     /**
      * Sets the block's bounds for rendering it as an item
      */
     public void setBlockBoundsForItemRender()
     {
         if (this.isDoubleSlab)
         {
             this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
         }
         else
         {
             this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.1F, 1.0F);
         }
     }
 
     @SideOnly(Side.CLIENT)
     @Override
     public Icon getIcon(int par1, int par2) {
         par2 = par2 % 8;
         if (par2 == 0)
             return this.iconArray[par2 & 7];
         else if (par2 == 1)
             return this.iconArray[par2 & 7];
         else if (par2 == 2)
             return this.iconArray[par2 & 7];
         else if (par2 == 3)
             return this.iconArray[par2 & 7];
         else if (par2 == 4)
             return this.iconArray[par2 & 7];
         else if (par2 == 5)
             return this.iconArray[par2 & 7];
         else if (par2 == 6)
             return this.iconArray[par2 & 7];
 
         return this.iconArray[par2 & 7];
     }
 
     @SideOnly(Side.CLIENT)
     @Override
     public void registerIcons(IconRegister par1IconRegister) {
         this.iconArray = new Icon[16];
 
         for (int i = 0; i < 16; ++i) {
             this.iconArray[i] = par1IconRegister.registerIcon(slabTextures[i]);
         }
     }
 
     @Override
     public String getFullSlabName(int i) {
         // TODO Auto-generated method stub
         return null;
     }
 }
