 package infinitealloys.item;
 
 import infinitealloys.util.Consts;
 import java.util.List;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.item.ItemStack;
 import net.minecraft.util.Icon;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 public class ItemMulti extends ItemIA {
 
 	public ItemMulti(int id) {
 		super(id);
 	}
 
 	@Override
 	@SideOnly(Side.CLIENT)
 	public void registerIcons(IconRegister iconRegister) {
 		for(int i = 0; i < Consts.MULTI_ITEM_COUNT; i++)
 			Items.multiIcons[i] = iconRegister.registerIcon(Consts.TEXTURE_PREFIX + Consts.MULTI_ITEM_NAMES[i]);
 	}
 
 	@Override
 	@SideOnly(Side.CLIENT)
 	public Icon getIconFromDamage(int damage) {
 		return Items.multiIcons[damage];
 	}
 
 	@Override
 	@SideOnly(Side.CLIENT)
 	public void getSubItems(int id, CreativeTabs creativetabs, List list) {
 		for(int i = 0; i < Consts.MULTI_ITEM_COUNT; i++)
 			list.add(new ItemStack(id, 1, i));
 	}
 }
