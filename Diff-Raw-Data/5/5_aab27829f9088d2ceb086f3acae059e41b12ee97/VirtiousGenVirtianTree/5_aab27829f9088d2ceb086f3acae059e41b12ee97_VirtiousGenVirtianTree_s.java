 package teamm.mods.virtious.world.gen;
 
 import java.util.Random;
 
 import teamm.mods.virtious.lib.VirtiousBlocks;
 import net.minecraft.block.Block;
 import net.minecraft.block.BlockSapling;
 import net.minecraft.util.Direction;
 import net.minecraft.world.World;
 import net.minecraft.world.gen.feature.WorldGenerator;
 import net.minecraftforge.common.ForgeDirection;
 import teamm.mods.virtious.block.VirtiousFlower;
 
 public class VirtiousGenVirtianTree extends WorldGenerator
 {
 	int leavesId,logId;
 	boolean fromSapling;
 	
 	public VirtiousGenVirtianTree(int leavesId, int logId, boolean fromSapling)
 	{
 		super(fromSapling);
 		this.leavesId = leavesId;
 		this.logId = logId;
 		this.fromSapling = fromSapling;
 	}
 	
 	@Override
 	public boolean generate(World world, Random random, int x, int y, int z)
 	{
 		int cap = random.nextInt(2) + 2;
 		int trunk = random.nextInt(3) + 3;
 		int minTreeHeight = 8;
 		
 		int treeHeight = trunk + minTreeHeight;
 		
 		if(!((VirtiousFlower)VirtiousBlocks.saplingVirtian).canThisPlantGrowOnThisBlockID(world.getBlockId(x, y-1, z)))
 			return false;
 		
 		if(this.fromSapling)
 		{
 			for(int h = 1; h <= treeHeight; h++){
			    Block block = Block.blocksList[world.getBlockId(i, j + h, k)];
 				if(block != null)
 				{
					if(!block.canBeReplacedByLeaves(world, i, j + h, k))
 						return false;
 				}
 			}
 		}
 		else
 		{
 			for(int i = -2; i <= 2; i++)
 			{
 				for(int k = -2; k <= 2; k++)
 				{
 					for(int j = 1; j <= treeHeight; j++)
 					{
 						if(!world.isAirBlock(x+i, y+j, z+k))
 							return false;
 					}
 				}
 	        }
 		}
 		
 		int h1 = treeHeight - random.nextInt(2);
 		int h2 = treeHeight - random.nextInt(2);
 		int h3 = treeHeight - random.nextInt(2);
 		int h4 = treeHeight - random.nextInt(2);
 
 		int rand1 = random.nextInt(2);
 		int rand2 = random.nextInt(2);
 		int rand3 = random.nextInt(2);
 		int rand4 = random.nextInt(2);
 		
 		for(int i = 0; i < treeHeight + cap; i++)
 		{
 			
 			if(i < treeHeight)
 			{
 				this.setBlock(world, x, y + i, z, logId);
 			}
 			
 			if(i >= trunk && i < treeHeight + 1)
 			{
 		    	addLeaves(world, x + 1, y+i, z);
 		    	addLeaves(world, x - 1, y+i, z);
 		    	addLeaves(world, x, y+i, z + 1);
 		    	addLeaves(world, x, y+i, z - 1);
 			}
 			
 			if(i > trunk && i < h1)
 		    	addLeaves(world, x + 1, y+i, z + 1);
 			if(i > trunk && i < h2)
 				addLeaves(world, x - 1, y+i, z - 1);
 			if(i > trunk && i < h3)
 				addLeaves(world, x - 1, y+i, z + 1);
 			if(i > trunk && i < h4)
 				addLeaves(world, x + 1, y+i, z - 1);
 
 			///
 //			if(i + h1 > trunk && i < h1 * 2)
 //		    	addLeaves(world, x + 2, y+i, z + 2);
 //			if(i + h2> trunk && i < h2 * 2)
 //				addLeaves(world, x - 2, y+i, z - 2);
 //			if(i + h3> trunk && i < h3 * 2)
 //				addLeaves(world, x - 2, y+i, z + 2);
 //			if(i + h4> trunk && i < h4 * 2)
 //				addLeaves(world, x + 2, y+i, z - 2);
 			////
 			
 			
 			if(i >= trunk + rand3 && i < treeHeight - rand1 * 2)
 		    	addLeaves(world, x + 2, y+i, z);
 			if(i >= trunk + rand2 && i < treeHeight - rand2 * 2)
 				addLeaves(world, x - 2, y+i, z);
 			if(i >= trunk + rand4 && i < treeHeight - rand3 * 2)
 				addLeaves(world, x, y+i, z + 2);
 			if(i >= trunk + rand1 && i < treeHeight - rand4 * 2)
 				addLeaves(world, x, y+i, z - 2);
 			
 			
 			
 			
 			
 			if(i >= treeHeight)
 			{
 				addLeaves(world, x, y+i, z);
 			}
 		}
 		
 		return true;
 	}
 	
 	private boolean addLeaves(World world, int x, int y, int z)
 	{
 		int id = world.getBlockId(x, y, z);
 	    Block block = Block.blocksList[id];
 		if(block == null || block.canBeReplacedByLeaves(world, x, y, z))
 		{
 			world.setBlock(x, y, z, leavesId);
 			return true;
 		}
 		return false;
 	}
 }
