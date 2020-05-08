 package gwydion0917.gwycraft.blocks;
 
 import net.minecraft.item.ItemBlock;
 import net.minecraft.item.ItemStack;
 
 public class ItemGlowyDyedGlass extends ItemBlock {
 
 	private final static String[] subNames = { "white", "orange", "magenta",
 			"lightBlue", "yellow", "lightGreen", "pink", "darkGrey",
 			"lightGrey", "cyan", "purple", "blue", "brown", "green", "red",
 			"black" };
 
 	public ItemGlowyDyedGlass(int id) {
 		super(id);
 		setHasSubtypes(true);
		setUnlocalizedName("dyedGlass");
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
