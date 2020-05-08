 package silentAbyss.enchantment;
 
 import net.minecraft.enchantment.Enchantment;
 import net.minecraft.enchantment.EnumEnchantmentType;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemBook;
 import net.minecraft.item.ItemStack;
 import net.minecraft.util.StatCollector;
 import silentAbyss.lib.Strings;
 import silentAbyss.tool.*;
 
 public class EnchantmentNihil extends Enchantment {
 
 	protected EnchantmentNihil(int par1, int par2, EnumEnchantmentType par3EnumEnchantmentType) {
 		
 		super(par1, par2, par3EnumEnchantmentType);
 		this.setName(Strings.NIHIL_NAME);
 	}
 	
 	@Override
 	public int getMinEnchantability(int par1) {
 		return 15;
 	}
 	
 	@Override
 	public int getMaxEnchantability(int par1) {
 		return super.getMinEnchantability(par1) + 50;
 	}
 	
 	@Override
 	public int getMaxLevel() {
 		return 1;
 	}
 	
 	@Override
 	public boolean canApply(ItemStack stack) {
 		Item item = stack.getItem();
 		if (item instanceof AbyssSword ||
 				item instanceof ItemBook) {
 			return stack.isItemStackDamageable() ? true : super.canApply(stack);
 		}
 		
 		return false;
 	}
 	
 	@Override
 	public String getTranslatedName(int par1) {
 		return Strings.NIHIL_NAME + " " + StatCollector.translateToLocal("enchantment.level." + par1);
 	}
 
 }
