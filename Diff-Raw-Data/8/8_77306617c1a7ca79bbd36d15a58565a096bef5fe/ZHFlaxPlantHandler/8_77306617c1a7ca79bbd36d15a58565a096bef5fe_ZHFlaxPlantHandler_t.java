 package zh.usefulthings.compatibility.minefactoryreloaded;
 
 import net.minecraft.block.Block;
 import net.minecraft.item.ItemStack;
 import net.minecraft.world.World;
 import powercrystals.minefactoryreloaded.api.IFactoryPlantable;
 import zh.usefulthings.UsefulThings;
 
 public class ZHFlaxPlantHandler implements IFactoryPlantable 
 {
 
 	public ZHFlaxPlantHandler() 
 	{
 
 	}
 
 	@Override
 	public int getSeedId() 
 	{
 		return UsefulThings.flaxSeeds.itemID;
 	}
 
 	@Override
 	public int getPlantedBlockId(World world, int x, int y, int z, ItemStack stack) 
 	{
 		return UsefulThings.flaxCrop.blockID;
 	}
 
 	@Override
 	public int getPlantedBlockMetadata(World world, int x, int y, int z, ItemStack stack) 
 	{
 		return 0;
 	}
 
 	@Override
 	public boolean canBePlantedHere(World world, int x, int y, int z, ItemStack stack) 
 	{
		if((world.getBlockId(x, y - 1, z) == Block.dirt.blockID || world.getBlockId(x, y - 1, z) == Block.grass.blockID || world.getBlockId(x, y - 1, z) == Block.tilledField.blockID) && world.isAirBlock(x, y, z))
 			return true;
 		
 		return false;
 	}
 
 	@Override
 	public void prePlant(World world, int x, int y, int z, ItemStack stack) 
 	{
		if(world.getBlockId(x, y - 1, z) == Block.dirt.blockID || world.getBlockId(x, y - 1, z) == Block.grass.blockID)
 		{
			world.setBlock(x, y - 1, z, Block.tilledField.blockID);
 		}
 	}
 
 	@Override
 	public void postPlant(World world, int x, int y, int z, ItemStack stack) 
 	{
 
 	}
 
 }
