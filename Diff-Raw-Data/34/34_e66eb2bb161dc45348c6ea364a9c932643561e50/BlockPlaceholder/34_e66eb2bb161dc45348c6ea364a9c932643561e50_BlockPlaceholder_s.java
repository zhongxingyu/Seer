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
		this.setUnlocalizedName("d");
 		this.setCreativeTab(AnotherWorld.TabAW);
		GameRegistry.registerBlock(this, "d");
		LanguageRegistry.addName(this, "d");
         
 
 
 	}
 	
     public void breakBlock(World world, int x, int y, int z, int par5, int par6)
     {
    	if (world.getBlockId(x, y - 1, z) == BasicBlocks.SpaceCactusID) {
     		world.setBlockAndMetadataWithNotify(x, y - 1, z, 0, 0, 2);
     		EntityItem entityItem = new EntityItem(world, x, y, z, new ItemStack(BasicBlocks.SpaceCactus, 1, 0));
 
     		world.spawnEntityInWorld(entityItem);
     	}
      }
 	
 	
 	
 	@Override
 	public void setBlockBoundsBasedOnState(IBlockAccess par1IBlockAccess, int x, int y, int z) {
         
 		//If the block below this block is a space cactus block.
 		if (par1IBlockAccess.getBlockId(x, y - 1, z) == BasicBlocks.SpaceCactusID)
         {
     		//Change this block to match the space cactus
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
 	
     @Override
     public void func_94332_a(IconRegister ir) {
     	this.field_94336_cN = ir.func_94245_a("anotherWorld:empty");
     }
 	
 	public boolean isOpaqueCube()
 	{
 		return false;
 	}
 
     public boolean renderAsNormalBlock()
     {
         return false;
     }
     public int quantityDropped(Random par1Random)
     {
         return 0;
     }
 	
 
 }
