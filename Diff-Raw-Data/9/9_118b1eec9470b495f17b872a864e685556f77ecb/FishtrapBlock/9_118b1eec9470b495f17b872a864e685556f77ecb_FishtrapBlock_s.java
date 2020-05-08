 package mod.industrialscience.modules.fishing;
 
 import cpw.mods.fml.common.registry.LanguageRegistry;
 import net.minecraft.block.BlockContainer;
 import net.minecraft.block.material.Material;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.util.Icon;
 import net.minecraft.world.World;
 
 public class FishtrapBlock extends BlockContainer {
 	private Icon texture;
 	private static Material material=Material.wood;
 	public FishtrapBlock(int id) {
 		super(id, material);
 		setUnlocalizedName("Basic Fishtrap");
 		LanguageRegistry.addName(this, "Basic Fishtrap");
 	}
 	@Override
 	public TileEntity createNewTileEntity(World world) {
 		return new Fishtraptile();
 	}
 	public boolean hasTileEntity(int metadata)
 	{
 	    return true;
 	}
     public boolean isOpaqueCube()
     {
        return true;
     }
 	public void func_94332_a(IconRegister par1IconRegister)
 	{
	texture= par1IconRegister.func_94245_a("trapdoor");
 	}
 
 }
