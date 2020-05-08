 package com.rperce.compactstuff;
 
 import java.util.List;
 
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.util.Icon;
 
 import com.rperce.compactstuff.client.CSIcons;
 
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 public class ItemStuff extends Item {
 	public static final int
 		GLASS_SLAG	= 0,
 		GLASS_FIBER	= 1,
 		DIAMOND_PLATE=2,
 		ALLOY_PLATE	= 3,
 		IRON_PLATE	= 4,
 		STEEL_PLATE	= 5,
 		STEEL_INGOT	= 6,
 		GOLD_PLATE	= 7,
 		TMOG_CRYSTAL= 8,
 		BLAZE_EMERALD=9,
 		GOLD_ALLOYED= 10,
 		BUTTER		= 11,
 		BUTTERBREAD	= 12,
 		LEMBAS		= 12,
 		MAXDMG		= 13;
 	
 	public static final String[] names =
 		{"Glass Slag", "Glass Fiber","Diamond Plate",
 		 "Carbon Alloy Plate","Iron Plate","CS Steel Plate",
 		 "CS Steel Ingot", "Gold Plate", "Transmogrifier Crystal",
 		 "Blaze Emerald", "Gilded Carbon Alloy Plate", "CS Butter",
 		 "CS Buttered Bread","Lembas"};
 	
 	public static ItemStack stack(int i) {
 		return stack(i, 1);
 	} public static ItemStack stack(int i, int size) {
 		if(i>MAXDMG) return null;
 		return new ItemStack(CompactStuff.itemStuff, size, i);
 	}
 	private static Icon[] icons = new Icon[MAXDMG+1];
 	public ItemStuff(int id) {
 		super(id);
 		setHasSubtypes(true);
 		setMaxStackSize(64);
 		setCreativeTab(CompactStuff.compactTab);
 		setUnlocalizedName("itemstuff");
 	}
 	@Override public void registerIcons(IconRegister ir) {
 		for(int i=0; i<=MAXDMG; i++) {
 			icons[i] = ir.registerIcon(CSIcons.PREFIX+"itemstuff"+i);
 		}
 	}
 	
	@Override public boolean hasEffect(ItemStack stack) {
		return stack.getItemDamage() == TMOG_CRYSTAL;
 	}
 		
 	@Override public Icon getIconFromDamage(int dmg) {
 		return icons[dmg];
 	}
 	
 	@Override public String getUnlocalizedName(ItemStack i) {
 		try { return names[i.getItemDamage()]; }
 		catch(NullPointerException e) { return "Stuff"; }
 	}
 	
 	/** Warnings suppressed due to override constraints */
 	@SuppressWarnings({ "rawtypes", "unchecked" })
 	@Override
 	@SideOnly(Side.CLIENT)
 	public void getSubItems(int id, CreativeTabs tab, List list) {
 		for(int i=0; i<names.length; i++) {
 			list.add(new ItemStack(id,1,i));
 		}
 	}
 }
