 package mods.anotherWorld.common.basicBlocks;
 
 import java.util.Random;
 
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 import mods.anotherWorld.AnotherWorld;
 import net.minecraft.block.Block;
 import net.minecraft.block.BlockContainer;
 import net.minecraft.block.material.Material;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.entity.item.EntityItem;
 import net.minecraft.item.ItemStack;
 import net.minecraft.world.IBlockAccess;
 import net.minecraft.world.World;
 
 public class BlockPlaceholder extends Block{
 
 	public BlockPlaceholder(int par1) {
 		super(par1, Material.glass);
 		this.setUnlocalizedName("How did you even obtain this? Haxor!");
 		this.setCreativeTab(AnotherWorld.TabAW);
 		GameRegistry.registerBlock(this, "Placeholder");
 		LanguageRegistry.addName(this, "How did you even obtain this? Haxor!");
 	}
 	
     public int quantityDropped(Random par1Random)
     {
         return 0;
     }
     
     @Override
     public void registerIcons(IconRegister ir) {
     	this.blockIcon = ir.registerIcon("anotherWorld:empty");
     }
 	
     
 	//Called when this block has been broken
     public void breakBlock(World world, int x, int y, int z, int par5, int par6)
     {
     	//If the block below is a Space Cactus
     	if (world.getBlockId(x, y - 1, z) == BasicBlocks.SpaceCactusID ) {
     		//Delete the block below
     		world.setBlock(x, y - 1, z, 0, 0, 2);
     		//Spawn a Space Cactus item where the block was.
     		world.spawnEntityInWorld(
     				new EntityItem(world, x, y, z, 
     				new ItemStack(BasicBlocks.SpaceCactus, 1, 0)));
     	}
      }
 	
    public void onNeighborBlockChange(World world, int x, int y, int z, int par5) {
    	if(!(world.getBlockId(x, y - 1, z) == BasicBlocks.SpaceCactusID)) {
    		world.setBlock(x, y, z, 0, 0, 2);
    	}
    }

 	
 	@Override
 	public void setBlockBoundsBasedOnState(IBlockAccess par1IBlockAccess, int x, int y, int z) {
         
 		//If the block below this block is a space cactus block.
 		if (par1IBlockAccess.getBlockId(x, y - 1, z) == BasicBlocks.SpaceCactusID)
         {
     		//Change this block's properties to match the space cactus'
 			this.setBlockBounds(0.025F, 0.025F, 0.0F, 0.975F, 0.5F, 0.975F);
     		this.blockHardness = 0.5F;
     		this.setStepSound(Block.soundSnowFootstep);
         }
 		//Else be a normal block.
         else {
         	this.setBlockBounds(0.00F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
     		this.blockHardness = 0.1F;
     		this.setStepSound(Block.soundStoneFootstep);
         }
 	}
 
 	
 
     
     
 	public boolean isOpaqueCube()
 	{
 		return false;
 	}
 
 	
     public boolean renderAsNormalBlock()
     {
         return false;
     }
 
 	
 
 }
