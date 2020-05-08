 package sammko.quantumCraft.blocks;
 
 
 import java.util.List;
 
 import net.minecraft.block.Block;
 import net.minecraft.block.material.Material;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.item.ItemStack;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 import sammko.quantumCraft.core.QuantumCraftSettings;
 import sammko.quantumCraft.items.ItemInitializator;
 import sammko.quantumCraft.resources.BlockTextureMatrix;
 
 public class BlockQuantumBasicBlocks extends Block {
 	public BlockQuantumBasicBlocks(int id, int texture) {
 		super(id, texture, Material.rock);
 		this.setTextureFile(QuantumCraftSettings.BLOCK_PNG);
 		GameRegistry.registerBlock(this, ItemBlockQuantumBasicBlocks.class, "BlockDecoBlocks");
 		setCreativeTab(ItemInitializator.tabQC);
 	}
 	
 	@Override
 	public int getBlockTextureFromSideAndMetadata (int side, int metadata) {
 		return BlockTextureMatrix.getIndex(BlockTextureMatrix.Deco[metadata]);
 	}
 
 	
 	@Override
 	public int damageDropped (int metadata) {
 		return metadata;
 	}
 	
 	@SideOnly(Side.CLIENT)
 	public void getSubBlocks(int par1, CreativeTabs tab, List subItems) {
 		for (int ix = 0; ix < 16; ix++) {
 			subItems.add(new ItemStack(this, 1, ix));
 		}
 	}
 	
 }
