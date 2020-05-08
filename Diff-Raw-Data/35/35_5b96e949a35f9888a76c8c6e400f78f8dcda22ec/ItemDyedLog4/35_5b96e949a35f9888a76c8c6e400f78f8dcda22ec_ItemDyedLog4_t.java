 package gwydion0917.gwycraft.blocks;
 
 import net.minecraft.block.Block;
 import net.minecraft.item.ItemBlock;
 import net.minecraft.item.ItemStack;
 
 public class ItemDyedLog4 extends ItemBlock {
 
	private final static String[] subNames = { "brown", "green", "red", "black" };
 
 	public ItemDyedLog4(Block block) {
 		super(block);
 		setHasSubtypes(true);
 		setUnlocalizedName("dyedLog4");
 	}
 
 	@Override
 	public int getMetadata(int damageValue) {
 		return damageValue;
 	}
 
 	@Override
 	public String getUnlocalizedName(ItemStack itemstack) {
 		return getUnlocalizedName() + "." + subNames[itemstack.getItemDamage()];
 	}
 
 }
