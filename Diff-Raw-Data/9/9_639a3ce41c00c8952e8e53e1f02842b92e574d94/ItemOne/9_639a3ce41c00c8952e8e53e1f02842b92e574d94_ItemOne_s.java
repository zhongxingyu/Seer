 package trs.someluigi.tslblocks.item;
 
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.util.Icon;
 import trs.someluigi.tslblocks.ref.ModInst;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 public class ItemOne extends Item {

 	public ItemOne(int par1) {
 		super(par1);
 		this.setHasSubtypes(true);
 		this.setCreativeTab(ModInst.modCtab);
 	}
 	
 	@Override
 	public String getUnlocalizedName(ItemStack i) {
 		return "sl_extramisc:itemone_" + i.getItemDamage();
 	}
 
     @Override
     public int getMetadata(int par1) {
         return par1;
     }
 
     @Override
     @SideOnly(Side.CLIENT)
     public Icon getIconFromDamage(int par1) {
     	// TODO add icon
     	return null;
     }
 	
 	
 }
