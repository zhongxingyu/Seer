 package assets.tacotek.Items;
 
 import java.util.List;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemFood;
 import net.minecraft.item.ItemStack;
 import assets.tacotek.common.tacotek;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 public class Toast extends ItemFood
 {
 	public Toast(int id)
 	{
		super(id, 6, false);
 		this.setCreativeTab(tacotek.tacotekTab);
 	}
 	
 	@SideOnly(Side.CLIENT)
 	public void registerIcons(IconRegister reg)
 	{
 		this.itemIcon = reg.registerIcon(tacotek.modID + ":" + this.getUnlocalizedName() );
 	}
 	
 	@SideOnly(Side.CLIENT)
 	public void addInformation(ItemStack itemStack, EntityPlayer player, List dataList, boolean bool) {
 		dataList.add("Mmmm toast.");
 	}
 }
