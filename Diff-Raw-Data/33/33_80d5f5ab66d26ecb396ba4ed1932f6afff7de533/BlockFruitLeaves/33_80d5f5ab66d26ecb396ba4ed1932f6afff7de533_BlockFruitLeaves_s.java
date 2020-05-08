 package clashsoft.mods.morefood.block;
 
 import java.util.List;
 import java.util.Random;
 
 import clashsoft.mods.morefood.MoreFoodMod;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 import net.minecraft.block.BlockLeaves;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.item.ItemStack;
 import net.minecraft.util.Icon;
 import net.minecraft.world.IBlockAccess;
 import net.minecraft.world.World;
 
 public class BlockFruitLeaves extends BlockLeaves
 {
 	public String[]	leafTypes;
 	
 	public int		iconType	= 0;
 	public Icon[]	iconArray;
 	
 	public BlockFruitLeaves(int par1, String[] types)
 	{
 		super(par1);
 		this.leafTypes = types;
 		this.iconArray = new Icon[leafTypes.length];
 	}
 	
 	@Override
 	public int getBlockColor()
 	{
 		return 0xFFFFFF;
 	}
 	
 	@Override
 	public int getRenderColor(int par1)
 	{
 		return 0xFFFFFF;
 	}
 	
 	@Override
 	public int colorMultiplier(IBlockAccess par1iBlockAccess, int par2, int par3, int par4)
 	{
 		return 0xFFFFFF;
 	}
 	
 	@Override
 	public void dropBlockAsItemWithChance(World par1World, int par2, int par3, int par4, int par5, float par6, int par7)
 	{
 		if (!par1World.isRemote)
 		{
 			int j1 = 20;
 			
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
 		}
 	}
 	
 	@Override
 	public void registerIcons(IconRegister par1IconRegister)
 	{
 		for (int i = 0; i < leafTypes.length; ++i)
 		{
 			iconArray[i] = par1IconRegister.registerIcon(getTextureName() + "_" + leafTypes[i]);
 		}
 	}
 	
 	@Override
 	@SideOnly(Side.CLIENT)
 	/**
 	 * From the specified side and block metadata retrieves the blocks texture. Args: side, metadata
 	 */
 	public Icon getIcon(int par1, int par2)
 	{
 		return this.iconArray[par2 & 3];
 	}
 	
 	@Override
 	public void getSubBlocks(int par1, CreativeTabs par2CreativeTabs, List par3List)
 	{
 		for (int i = 0; i < leafTypes.length; i++)
 			par3List.add(new ItemStack(this, 1, i));
 	}
 	
 	@Override
 	public int quantityDropped(Random par1Random)
 	{
 		return par1Random.nextInt(10) == 0 ? 1 : 0;
 	}
 	
 	@Override
 	public int idDropped(int par1, Random par2Random, int par3)
 	{
 		return MoreFoodMod.foods.itemID;
 	}
 	
 	@Override
 	public int idPicked(World par1World, int par2, int par3, int par4)
 	{
		return MoreFoodMod.foods.itemID;
 	}
 	
 	@Override
 	public int damageDropped(int par1)
 	{
 		return MoreFoodMod.fruitSaplings.damageDropped(par1);
 	}
 	
 	@Override
 	public boolean isOpaqueCube()
 	{
 		return false;
 	}
 	
 	@Override
 	@SideOnly(Side.CLIENT)
 	/**
 	 * Returns true if the given side of this block type should be rendered, if the adjacent block is at the given
 	 * coordinates.  Args: blockAccess, x, y, z, side
 	 */
 	public boolean shouldSideBeRendered(IBlockAccess par1IBlockAccess, int par2, int par3, int par4, int par5)
 	{
 		return true;
 	}
 }
