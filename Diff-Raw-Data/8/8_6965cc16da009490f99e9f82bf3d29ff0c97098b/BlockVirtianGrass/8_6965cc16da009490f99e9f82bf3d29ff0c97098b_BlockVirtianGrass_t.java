 package teamm.mods.virtious.block;
 
 import java.util.Random;
 
 import teamm.mods.virtious.lib.VirtiousBlocks;
 
 import net.minecraft.block.Block;
 import net.minecraft.block.material.Material;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.util.Icon;
 import net.minecraft.world.World;
 
 public class BlockVirtianGrass extends VirtiousBlock
 {
 	private Icon top;
 	private Icon bottom;
 	
 	public BlockVirtianGrass(int id) 
 	{
 		super(id, Material.grass);
 	}
 	
 	/**
      * Ticks the block if it's been scheduled
      */
     public void updateTick(World par1World, int par2, int par3, int par4, Random par5Random)
     {
         if (!par1World.isRemote)
         {
             if (par1World.getBlockLightValue(par2, par3 + 1, par4) < 4 && par1World.getBlockLightOpacity(par2, par3 + 1, par4) > 2)
             {
                 par1World.setBlock(par2, par3, par4, VirtiousBlocks.virtianSoil.blockID);
             }
             else if (par1World.getBlockLightValue(par2, par3 + 1, par4) >= 9)
             {
                 for (int l = 0; l < 4; ++l)
                 {
                     int i1 = par2 + par5Random.nextInt(3) - 1;
                     int j1 = par3 + par5Random.nextInt(5) - 3;
                     int k1 = par4 + par5Random.nextInt(3) - 1;
                     int l1 = par1World.getBlockId(i1, j1 + 1, k1);
 
                     if (par1World.getBlockId(i1, j1, k1) == VirtiousBlocks.virtianSoil.blockID && par1World.getBlockLightValue(i1, j1 + 1, k1) >= 4 && par1World.getBlockLightOpacity(i1, j1 + 1, k1) <= 2)
                     {
                         par1World.setBlock(i1, j1, k1, VirtiousBlocks.virtianSoil.blockID);
                     }
                 }
             }
         }
     }
     
 	public void registerIcons(IconRegister r)
 	{
 		blockIcon = r.registerIcon("virtious:MossySoilSide");
 		this.top = r.registerIcon("virtious:MossySoilTop");
 		this.bottom = r.registerIcon("virtious:VirtianSoil");
 	}
 	
 	public Icon getIcon(int i, int j)
 	{
 		return i == 1 ? top : i == 0 ? bottom : blockIcon;
 	}
 }
