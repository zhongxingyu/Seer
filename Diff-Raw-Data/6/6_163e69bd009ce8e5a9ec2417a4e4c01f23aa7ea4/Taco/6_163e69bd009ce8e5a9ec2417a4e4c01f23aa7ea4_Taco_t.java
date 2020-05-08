 package assets.tacotek.Items;
 
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemFood;
 import assets.tacotek.common.tacotek;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
public class Taco extends ItemFood {
 	public Taco(int id)
 	{
		super(id, 8, true);
 		this.setCreativeTab(CreativeTabs.tabFood);
 	}
 	
 	@SideOnly(Side.CLIENT)
 	public void registerIcons(IconRegister reg)
 	{
 		this.itemIcon = reg.registerIcon(tacotek.modID + ":" + this.getUnlocalizedName());
 	}
 }
