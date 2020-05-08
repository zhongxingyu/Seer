 package gwydion0917.gwycraft.blocks;
 
 import net.minecraft.block.Block;
 import net.minecraft.item.ItemBlock;
 import net.minecraft.item.ItemStack;
 
 public class ItemDyedLog3 extends ItemBlock {
 
	private final static String[] subNames = { "white", "orange", "magenta",
			"lightBlue", "yellow", "lightGreen", "pink", "darkGrey",
			"lightGrey", "cyan", "purple", "blue", "brown", "green", "red",
			"black" };
 
 	public ItemDyedLog3(Block block) {
 		super(block);
 		setHasSubtypes(true);
 		setUnlocalizedName("dyedLog3");
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
