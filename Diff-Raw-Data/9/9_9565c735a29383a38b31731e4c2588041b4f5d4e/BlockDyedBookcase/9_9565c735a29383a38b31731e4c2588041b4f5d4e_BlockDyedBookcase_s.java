 package gwydion0917.gwycraft.blocks;
 
 import java.util.List;
 
 import net.minecraft.block.BlockBookshelf;
 import net.minecraft.block.material.Material;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.item.ItemStack;
 import net.minecraft.util.Icon;
 import net.minecraft.world.World;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 public class BlockDyedBookcase extends BlockBookshelf {
 
 	@SideOnly(Side.CLIENT)
 	private Icon[] iconArray;
 	private Icon[] iconArray1;
 
 	public BlockDyedBookcase(int id, Material wood) {
 		super(id);
 	}
 
 	@Override
 	public Icon getIcon(int par1, int par2) {
 		int l = par2 & 15;
 		return (par1 == 1 || par1 == 0) ? this.iconArray1[l]
 				: this.iconArray[l];
 
 	}
 
 	@Override
 	public int damageDropped(int metadata) {
 		return metadata;
 	}
 
 	@SideOnly(Side.CLIENT)
 	@Override
 	public void getSubBlocks(int par1, CreativeTabs par2CreativeTabs,
 			List par3List) {
 		for (int j = 0; j < 16; ++j) {
 			par3List.add(new ItemStack(par1, 1, j));
 		}
 	}
 
 	@SideOnly(Side.CLIENT)
 	@Override
 	public void registerIcons(IconRegister par1IconRegister) {
 		this.iconArray = new Icon[16];
 		this.iconArray1 = new Icon[16];
 
 		for (int i = 0; i < this.iconArray.length; ++i) {
 			this.iconArray1[i] = par1IconRegister
 					.registerIcon("gwycraft:plank_" + i);
 			this.iconArray[i] = par1IconRegister
 					.registerIcon("gwycraft:bookcase_" + i);
 		}
 	}
 	
     /**
      * Determines the amount of enchanting power this block can provide to an enchanting table.
      * @param world The World
      * @param x X position
      * @param y Y position
      * @param z Z position
      * @return The amount of enchanting power this block produces.
      */
	//FIXME: Fix bookshelf power
	//@Override
    public int getEnchantPower(World world, int x, int y, int z)
     {
        return 1;
     }
 
 }
