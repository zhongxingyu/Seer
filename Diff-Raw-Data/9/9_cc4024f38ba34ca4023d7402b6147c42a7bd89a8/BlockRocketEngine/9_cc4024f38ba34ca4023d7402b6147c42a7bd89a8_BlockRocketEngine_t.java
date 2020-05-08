 package spazzysmod.blocks;
 
 import java.util.Random;
 
 import net.minecraft.block.Block;
 import net.minecraft.block.material.Material;
 import net.minecraft.client.Minecraft;
 import net.minecraft.client.entity.EntityClientPlayerMP;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.util.Icon;
 import net.minecraft.world.IBlockAccess;
 import net.minecraft.world.World;
 import spazzysmod.client.gui.GuiPlanets;
 import spazzysmod.creativetab.SpazzysTabs;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 public class BlockRocketEngine extends Block 
 {
 	@SideOnly(Side.CLIENT)
 	private Icon bottom;
 	@SideOnly(Side.CLIENT)
 	private Icon top;
 
 	Minecraft mc;
 
 	public BlockRocketEngine(int id, Material par2Material) {
 		super(id, par2Material);
 		this.setCreativeTab(SpazzysTabs.tabSolarSystem);
 	    this.setHardness(1F);
         this.setResistance(1F);
 	}
 
 	/**
 	 * A randomly called display update to be able to add particles or other items for display
 	 */
 	public void randomDisplayTick(World par1World, int par2, int par3, int par4, Random par5Random)
 	{
 		Random random = par1World.rand;
 		double d0 = 0.0625D;
 
 		for (int l = 0; l < 6; ++l)
 		{
 			double d1 = (double)((float)par2 + random.nextFloat());
 			double d2 = (double)((float)par3 + random.nextFloat());
 			double d3 = (double)((float)par4 + random.nextFloat());
 
 			if (l == 0 && !par1World.isBlockOpaqueCube(par2, par3 + 1, par4))
 			{
 				d2 = (double)(par3 + 1) + d0;
 			}
 
 			if (l == 1 && !par1World.isBlockOpaqueCube(par2, par3 - 1, par4))
 			{
 				d2 = (double)(par3 + 0) - d0;
 			}
 
 			if (l == 2 && !par1World.isBlockOpaqueCube(par2, par3, par4 + 1))
 			{
 				d3 = (double)(par4 + 1) + d0;
 			}
 
 			if (l == 3 && !par1World.isBlockOpaqueCube(par2, par3, par4 - 1))
 			{
 				d3 = (double)(par4 + 0) - d0;
 			}
 
 			if (l == 4 && !par1World.isBlockOpaqueCube(par2 + 1, par3, par4))
 			{
 				d1 = (double)(par2 + 1) + d0;
 			}
 
 			if (l == 5 && !par1World.isBlockOpaqueCube(par2 - 1, par3, par4))
 			{
 				d1 = (double)(par2 + 0) - d0;
 			}
 
 			if (d1 < (double)par2 || d1 > (double)(par2 + 1) || d2 < 0.0D || d2 > (double)(par3 + 1) || d3 < (double)par4 || d3 > (double)(par4 + 1))
 			{
 				par1World.spawnParticle("flame", d1, d2, d3, 0.0D, 0.0D, 0.0D);
 			}
 		}
 
 	}
 
 
 	/**
 	 * Called upon block activation (right click on the block.)
 	 */
 	public boolean onBlockActivated(World par1World, int x, int y, int z, EntityPlayer par5EntityPlayer, int par6, float par7, float par8, float par9)
 	{
 		if (par1World.getBlockId(x, y + 1, z) == Block.blockIron.blockID && par1World.getBlockId(x, y + 2, z) == Block.glass.blockID
 				&& par1World.getBlockId(x, y + 3, z) == Block.blockIron.blockID && par1World.getBlockId(x + 1, y, z) == Block.fence.blockID
 				&& par1World.getBlockId(x, y, z + 1) == Block.fence.blockID && par1World.getBlockId(x - 1, y, z) == Block.fence.blockID
 				&& par1World.getBlockId(x, y, z - 1) == Block.fence.blockID && par1World.getBlockId(x + 1, y - 1, z) == Block.fence.blockID
 				&& par1World.getBlockId(x, y - 1, z + 1) == Block.fence.blockID && par1World.getBlockId(x - 1, y - 1, z) == Block.fence.blockID
 				&& par1World.getBlockId(x, y - 1, z - 1) == Block.fence.blockID)
 		{	
 			if(par5EntityPlayer.dimension == -1)
 			{
 				par5EntityPlayer.addChatMessage("Can't lift off from the Nether!");
 			}
 			else
 			{
				if(par5EntityPlayer.getClass() != EntityClientPlayerMP.class)
 				{
					Minecraft.getMinecraft().displayGuiScreen ( new GuiPlanets( par5EntityPlayer ) );
 					par1World.setBlockToAir(x, y, z);
 					par1World.setBlockToAir(x, y + 1, z);
 					par1World.setBlockToAir(x, y + 2, z);
 					par1World.setBlockToAir(x, y + 3, z);
 					par1World.setBlockToAir(x, y, z + 1);
 					par1World.setBlockToAir(x + 1, y, z);
 					par1World.setBlockToAir(x, y, z - 1);
 					par1World.setBlockToAir(x - 1, y, z);
 					par1World.setBlockToAir(x, y - 1, z + 1);
 					par1World.setBlockToAir(x + 1, y - 1, z);
 					par1World.setBlockToAir(x, y - 1, z - 1);
 					par1World.setBlockToAir(x - 1, y - 1, z);
 				}
 			}
 		}
 		return true;
 	}
 
 	@SideOnly(Side.CLIENT)
 	/**
 	 * Retrieves the block texture to use based on the display side. Args: iBlockAccess, x, y, z, side
 	 */
 	public Icon getBlockTexture(IBlockAccess par1IBlockAccess, int par2, int par3, int par4, int par5)
 	{
 		if (par5 == 1)
 			return this.top;
 		else if (par5 == 0)
 			return this.bottom;
 		else {
 			Material material = par1IBlockAccess.getBlockMaterial(par2, par3 + 1, par4);
 			return material != Material.snow && material != Material.craftedSnow ? this.blockIcon : this.blockIcon;
 		}
 	}
 
 	/**
 	 * From the specified side and block metadata retrieves the blocks texture. Args: side, metadata
 	 */
 	public Icon getIcon(int par1, int par2) {
 		return par1 == 1 ? this.top : (par1 == 0 ? this.bottom : this.blockIcon);
 	}
 
 	@Override
 	@SideOnly(Side.CLIENT)
 	public void registerIcons(IconRegister par1IconRegister) {
 		this.blockIcon = par1IconRegister.registerIcon("spazzysmod:" + this.getUnlocalizedName().substring(5));
 		this.bottom =  par1IconRegister.registerIcon("spazzysmod:"
 				+ "bottom" + this.getUnlocalizedName().substring(5));
 
 		this.top =  par1IconRegister.registerIcon("spazzysmod:"
 				+ "top" + this.getUnlocalizedName().substring(5));
 	}
 }
