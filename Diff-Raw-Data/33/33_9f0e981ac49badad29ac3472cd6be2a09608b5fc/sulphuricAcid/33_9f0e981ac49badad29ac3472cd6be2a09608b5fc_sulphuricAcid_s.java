 package assets.electrolysm.electro.oreProccessing;
 
 import java.util.Random;
 
 import net.minecraft.block.Block;
 import net.minecraft.block.material.Material;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.util.Icon;
 import net.minecraft.world.World;
 import net.minecraftforge.fluids.BlockFluidClassic;
 import assets.electrolysm.electro.electrolysmCore;
 import assets.electrolysm.electro.advAtomics.liquids.ModFluidPlasma;
 import assets.electrolysm.electro.advAtomics.liquids.ModFluidSulphuricAcid;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 public class sulphuricAcid extends BlockFluidClassic {
 
 	@SideOnly(Side.CLIENT)
 	public Icon flowing;
 	@SideOnly(Side.CLIENT)
 	public Icon still;
 
     public sulphuricAcid(int id) {
             super(id, new ModFluidSulphuricAcid(), Material.water);
             this.setCreativeTab(electrolysmCore.TabElectrolysm);
             this.setUnlocalizedName("fluidSulpuricAcid");
     }
     @Override
 	@SideOnly(Side.CLIENT)
 	public Icon getIcon( int side, int meta )
 	{
 		if( side <= 1 )
 		{
 			return this.still;
 		}
 		else
 		{
 			return this.flowing;
 		}
 	}
     
     
     @SideOnly(Side.CLIENT)
     public void registerIcons(IconRegister register)
     {
        this.flowing = register.registerIcon("electrolysm:" + "sulpuric_Acid");
        this.still = register.registerIcon("electrolysm:" + "sulpuric_Acid_still");
     }
     
 	private void erodeWorld(World world, int x, int y, int z, Random rand) 
 	{
 		world.scheduleBlockUpdate(x, y, z, blockID, tickRate);
     	if(rand.nextInt(50) == 1)
     	{
         	if(world.getBlockId(x, y - 1, z) == Block.dirt.blockID ||
        			world.getBlockId(x, y - 1, z) == Block.stone.blockID ||
         				world.getBlockId(x, y - 1, z) == Block.grass.blockID ||
         				world.getBlockMaterial(x, y - 1, z) == Material.ground ||
         				world.getBlockMaterial(x, y - 1, z) == Material.rock ||
        				world.getBlockMaterial(x, y - 1, z) == Material.wood)
         	{
        		world.setBlock(x, y - 1, z, this.blockID);
         	}
     	}		
 	}	
 	
 	
 	public void updateTick(World world, int x, int y, int z, Random rand)
     {
     	this.erodeWorld(world, x, y, z, rand);
         int quantaRemaining = quantaPerBlock - world.getBlockMetadata(x, y, z);
         int expQuanta = -101;
 
         // check adjacent block levels if non-source
         if (quantaRemaining < quantaPerBlock)
         {
             int y2 = y - densityDir;
 
             if (world.getBlockId(x,     y2, z    ) == blockID || 
                 world.getBlockId(x - 1, y2, z    ) == blockID || 
                 world.getBlockId(x + 1, y2, z    ) == blockID ||
                 world.getBlockId(x,     y2, z - 1) == blockID ||
                 world.getBlockId(x,     y2, z + 1) == blockID)
             {
                 expQuanta = quantaPerBlock - 1;
             }
             else
             {
                 int maxQuanta = -100;
                 maxQuanta = getLargerQuanta(world, x - 1, y, z,     maxQuanta);
                 maxQuanta = getLargerQuanta(world, x + 1, y, z,     maxQuanta);
                 maxQuanta = getLargerQuanta(world, x,     y, z - 1, maxQuanta);
                 maxQuanta = getLargerQuanta(world, x,     y, z + 1, maxQuanta);
 
                 expQuanta = maxQuanta - 1;
             }
 
             // decay calculation
             if (expQuanta != quantaRemaining)
             {
                 quantaRemaining = expQuanta;
 
                 if (expQuanta <= 0)
                 {
                     world.setBlockToAir(x, y, z);
                 }
                 else
                 {
                     world.setBlockMetadataWithNotify(x, y, z, quantaPerBlock - expQuanta, 3);
                     world.scheduleBlockUpdate(x, y, z, blockID, tickRate);
                     world.notifyBlocksOfNeighborChange(x, y, z, blockID);
                 }
             }
         }
         else if (quantaRemaining > quantaPerBlock)
         {
             world.setBlockMetadataWithNotify(x, y, z, 0, 3);
         }
 
         // Flow vertically if possible
         if (canDisplace(world, x, y + densityDir, z))
         {
             flowIntoBlock(world, x, y + densityDir, z, 1);
             return;
         }
 
         // Flow outward if possible
         int flowMeta = quantaPerBlock - quantaRemaining + 1;
         if (flowMeta >= quantaPerBlock)
         {
             return;
         }
 
         if (isSourceBlock(world, x, y, z) || !isFlowingVertically(world, x, y, z))
         {
             if (world.getBlockId(x, y - densityDir, z) == blockID)
             {
                 flowMeta = 1;
             }
             boolean flowTo[] = getOptimalFlowDirections(world, x, y, z);
 
             if (flowTo[0]) flowIntoBlock(world, x - 1, y, z,     flowMeta);
             if (flowTo[1]) flowIntoBlock(world, x + 1, y, z,     flowMeta);
             if (flowTo[2]) flowIntoBlock(world, x,     y, z - 1, flowMeta);
             if (flowTo[3]) flowIntoBlock(world, x,     y, z + 1, flowMeta);
         }
     }
 
 }
