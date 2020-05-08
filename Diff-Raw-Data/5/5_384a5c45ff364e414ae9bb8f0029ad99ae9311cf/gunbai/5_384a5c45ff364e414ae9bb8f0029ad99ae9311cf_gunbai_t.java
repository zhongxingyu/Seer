 package NarutoStyleItems;
 
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.item.EnumToolMaterial;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemSword;
 
public class Gunbai extends ItemSword {
 
	public Gunbai(int id, EnumToolMaterial gunbaiTool ) {
 		super(id, gunbaiTool);
 		this.setCreativeTab(CreativeTabs.tabCombat);
 		this.maxStackSize = 1;
 	}
 	
 	
 	
 
 	public void registerIcons(IconRegister IR)
 	{
 		this.itemIcon = IR.registerIcon("NarutoStyle:gunbai");
 	}
 }
