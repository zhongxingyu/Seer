 package clashsoft.mods.morefood.block;
 
 import java.util.List;
 import java.util.Random;
 
 import clashsoft.mods.morefood.MoreFoodMod;
 import clashsoft.mods.morefood.food.Food;
 import clashsoft.mods.morefood.world.WorldGenFruitTree;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 import net.minecraft.block.BlockSapling;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.util.Icon;
 import net.minecraft.world.World;
 import net.minecraftforge.event.terraingen.TerrainGen;
 
 public class BlockFruitSapling extends BlockSapling
 {
 	public String[]	saplingTypes;
 	@SideOnly(Side.CLIENT)
 	private Icon[]	saplingIcon;
 	
 	public BlockFruitSapling(int par1, String[] types)
 	{
 		super(par1);
 		this.saplingTypes = types;
 		
 		float f = 0.4F;
 		this.setBlockBounds(0.5F - f, 0.0F, 0.5F - f, 0.5F + f, f * 2.0F, 0.5F + f);
 		this.setCreativeTab(CreativeTabs.tabDecorations);
 	}
 	
 	/**
 	 * Ticks the block if it's been scheduled
 	 */
 	@Override
 	public void updateTick(World par1World, int par2, int par3, int par4, Random par5Random)
 	{
 		if (!par1World.isRemote)
 		{
 			super.updateTick(par1World, par2, par3, par4, par5Random);
 			
 			if (par1World.getBlockLightValue(par2, par3 + 1, par4) >= 9 && par5Random.nextInt(7) == 0)
 			{
 				this.markOrGrowMarked(par1World, par2, par3, par4, par5Random);
 			}
 		}
 	}
 	
 	@Override
 	@SideOnly(Side.CLIENT)
 	/**
 	 * From the specified side and block metadata retrieves the blocks texture. Args: side, metadata
 	 */
 	public Icon getIcon(int par1, int par2)
 	{
 		par2 &= 3;
 		return this.saplingIcon[par2];
 	}
 	
 	@Override
 	public void markOrGrowMarked(World par1World, int par2, int par3, int par4, Random par5Random)
 	{
 		int l = par1World.getBlockMetadata(par2, par3, par4);
 		
 		if ((l & 8) == 0)
 		{
 			par1World.setBlockMetadataWithNotify(par2, par3, par4, l | 8, 4);
 		}
 		else
 		{
 			this.growTree(par1World, par2, par3, par4, par5Random);
 		}
 	}
 	
 	/**
 	 * Attempts to grow a sapling into a tree
 	 */
 	@Override
 	public void growTree(World par1World, int par2, int par3, int par4, Random par5Random)
 	{
 		if (!TerrainGen.saplingGrowTree(par1World, par5Random, par2, par3, par4))
 			return;
 		
 		int l = par1World.getBlockMetadata(par2, par3, par4) & 3;
 		Object object = null;
 		int i1 = 0;
 		int j1 = 0;
 		boolean flag = false;
 		
 		par1World.setBlock(par2, par3, par4, 0, 0, 4);
 		
		int woodBlock = this.blockID == MoreFoodMod.fruitSaplingsID ? MoreFoodMod.fruitLogsID : MoreFoodMod.fruitLogsID2;
		int leafBlock = this.blockID == MoreFoodMod.fruitSaplingsID ? MoreFoodMod.fruitLeavesID : MoreFoodMod.fruitLeavesID2;
		if (!(new WorldGenFruitTree(true, 4 + par5Random.nextInt(4), woodBlock, leafBlock, l, l)).generate(par1World, par5Random, par2 + i1, par3, par4 + j1))
 		{
 			if (flag)
 			{
 				par1World.setBlock(par2 + i1, par3, par4 + j1, this.blockID, l, 4);
 				par1World.setBlock(par2 + i1 + 1, par3, par4 + j1, this.blockID, l, 4);
 				par1World.setBlock(par2 + i1, par3, par4 + j1 + 1, this.blockID, l, 4);
 				par1World.setBlock(par2 + i1 + 1, par3, par4 + j1 + 1, this.blockID, l, 4);
 			}
 			else
 			{
 				par1World.setBlock(par2, par3, par4, this.blockID, l, 4);
 			}
 		}
 	}
 	
 	/**
 	 * Determines if the same sapling is present at the given location.
 	 */
 	@Override
 	public boolean isSameSapling(World par1World, int par2, int par3, int par4, int par5)
 	{
 		return par1World.getBlockId(par2, par3, par4) == this.blockID && (par1World.getBlockMetadata(par2, par3, par4) & 3) == par5;
 	}
 	
 	@Override
 	public int quantityDropped(Random par1Random)
 	{
 		return 1;
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
 		par1 &= 3;
		if (this.blockID == MoreFoodMod.fruitSaplingsID)
			switch (par1)
			{
			case 0: return Food.orange.getID();
			case 1: return Food.pear.getID();
			case 2: return Food.cherry.getID();
			case 3: return Food.plum.getID();
			}
		else
			switch (par1)
			{
			case 0: return Food.banana.getID();
			}
		return 0;
 	}
 	
 	@Override
 	@SideOnly(Side.CLIENT)
 	/**
 	 * returns a list of blocks with the same ID, but different meta (eg: wood returns 4 blocks)
 	 */
 	public void getSubBlocks(int par1, CreativeTabs par2CreativeTabs, List par3List)
 	{
 	}
 	
 	@Override
 	@SideOnly(Side.CLIENT)
 	/**
 	 * When this method is called, your block should register all the icons it needs with the given IconRegister. This
 	 * is the only chance you get to register icons.
 	 */
 	public void registerIcons(IconRegister par1IconRegister)
 	{
 		this.saplingIcon = new Icon[saplingTypes.length];
 		
 		for (int i = 0; i < this.saplingIcon.length; ++i)
 		{
 			this.saplingIcon[i] = par1IconRegister.registerIcon(this.getTextureName() + "_" + saplingTypes[i]);
 		}
 	}
 }
