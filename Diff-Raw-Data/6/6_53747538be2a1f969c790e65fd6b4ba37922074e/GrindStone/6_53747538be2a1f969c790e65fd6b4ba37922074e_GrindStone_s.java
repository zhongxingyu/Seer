 package net.digidownloads.FFmine.item;
 
 import cpw.mods.fml.common.registry.GameRegistry;
 import net.digidownloads.FFmine.lib.Reference;
 import net.digidownloads.FFmine.lib.Strings;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 
 public class GrindStone {
 
 	public static void Item() {
 		
 		//Grinding wheels
 		
 		final Item StoneGrindWheel = new Item((Reference.StoneGrindWheelID)-256).setUnlocalizedName(Strings.StoneGrindWheelName).setCreativeTab(CreativeTabs.tabMaterials);
 		final Item IronGrindWheel = new Item((Reference.IronGrindWheelID)-256).setUnlocalizedName(Strings.IronGrindWheelName).setCreativeTab(CreativeTabs.tabMaterials);
 		final Item GoldGrindWheel = new Item((Reference.GoldGrindWheelID)-256).setUnlocalizedName(Strings.GoldGrindWheelName).setCreativeTab(CreativeTabs.tabMaterials);
 		final Item DiamondGrindWheel = new Item((Reference.DiamondGrindWheelID)-256).setUnlocalizedName(Strings.DiamondGrindWheelName).setCreativeTab(CreativeTabs.tabMaterials);
 				
 		
 		GameRegistry.addShapedRecipe(new ItemStack(StoneGrindWheel),new Object[] {"SSS","S S","SSS",Character.valueOf('S'),net.minecraft.block.Block.stone});
		//GameRegistry.addShapedRecipe(new ItemStack(IronGrindWheel),new Object[] {"III","S","III",Character.valueOf('I'),net.minecraft.item.Item.ingotIron,Character.valueOf('S'),StoneGrindWheel});
		//GameRegistry.addShapedRecipe(new ItemStack(GoldGrindWheel),new Object[] {"GGG","GIG","GGG",Character.valueOf('G'),net.minecraft.item.Item.ingotGold,Character.valueOf('I'),IronGrindWheel});
		//GameRegistry.addShapedRecipe(new ItemStack(DiamondGrindWheel),new Object[] {"DDD","DGD","DDD",Character.valueOf('D'),net.minecraft.item.Item.diamond,Character.valueOf('G'),GoldGrindWheel});
 	}
 	
 	
 	
 	
 	
 }
