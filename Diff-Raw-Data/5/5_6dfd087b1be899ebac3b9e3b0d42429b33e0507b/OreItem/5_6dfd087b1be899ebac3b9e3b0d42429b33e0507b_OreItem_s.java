 package bstramke.OresDropMores2.Items;
 
 import java.util.List;
 
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.util.Icon;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 public class OreItem extends Item {
 
 	public static int iron = 0;
 	public static int gold = 1;
 	public static String[] itemNames = new String[] { "IronOre", "GoldOre" };
 	public static String[] itemDisplayNames = new String[] { "Raw Iron Ore", "Raw Gold Ore" };
 	private Icon icoGoldOre;
 	
 	public OreItem(int par1) {
 		super(par1);
 		this.setMaxDamage(0);
 		this.setCreativeTab(CreativeTabs.tabMaterials);
 		this.setHasSubtypes(true);
 	}
 	
 	@Override
 	public void func_94581_a(IconRegister iconRegister)
 	{
 		iconIndex = iconRegister.func_94245_a("OresDropMores2:IronOre");
 		icoGoldOre = iconRegister.func_94245_a("OresDropMores2:GoldOre");
 	}
 
 	@Override
 	public Icon getIconFromDamage(int par1) {
 		if(par1 == iron)
 			return iconIndex;
 		else
 			return icoGoldOre;
 	}
 
 	public static int getMetadataSize() {
 		return itemNames.length;
 	}
 
 	@Override
 	public String getItemDisplayName(ItemStack is) {
 		if (is.getItemDamage() < getMetadataSize() && is.getItemDamage() >= 0)
			return itemNames[is.getItemDamage()];
 		else
			return itemNames[0];
 	}
 
 	@SideOnly(Side.CLIENT)
 	@Override
 	public void getSubItems(int par1, CreativeTabs tab, List list) {
 		for (int metaNumber = 0; metaNumber < getMetadataSize(); metaNumber++) {
 			list.add(new ItemStack(par1, 1, metaNumber));
 		}
 	}
 	
 }
