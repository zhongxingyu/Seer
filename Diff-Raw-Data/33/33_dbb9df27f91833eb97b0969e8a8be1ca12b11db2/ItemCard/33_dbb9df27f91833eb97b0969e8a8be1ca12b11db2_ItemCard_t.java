 package mtshaw113.juli.items;
 
 import java.util.List;
 
import mtshaw113.juli.Juli;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.util.Icon;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 public class ItemCard extends Item{
 
 	@SideOnly(Side.CLIENT)
 	private Icon[] icons;
 	
 	
 	public ItemCard(int id) {
 		super(id);
		setCreativeTab(Juli.tabCustom);
 		setHasSubtypes(true);
 	}
 	
 	@Override
 	public String getUnlocalizedName(ItemStack itemstack) {
 		return ItemInfo.CARD_UNLOCALIZED_NAME + itemstack.getItemDamage();
 	}
 	
 	@Override
 	@SideOnly(Side.CLIENT)
 	public void registerIcons(IconRegister register) {
 		icons = new Icon[ItemInfo.CARD_ICONS.length];
 		for (int i = 0; i < icons.length; i++) {
 			icons[i] = register.registerIcon(ItemInfo.TEXTURE_LOCATION + ":" + ItemInfo.CARD_ICONS[i]);
 		}
 	}
 	
 	@Override
 	@SideOnly(Side.CLIENT)
 	public Icon getIconFromDamage(int dmg) {
 		return icons[dmg];
 	}
 	
 	@Override
 	@SideOnly(Side.CLIENT)
 	public void getSubItems(int id, CreativeTabs tab, List list) {
 		for (int i = 0; i < ItemInfo.CARD_NAMES.length; i++) {
 			ItemStack stack = new ItemStack(id, 1, i);
 			list.add(stack);
 		}
 	}
 	
 }
