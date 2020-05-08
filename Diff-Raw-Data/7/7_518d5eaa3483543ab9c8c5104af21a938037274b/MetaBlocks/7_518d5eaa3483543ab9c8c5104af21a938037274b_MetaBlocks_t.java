 package com.metatechcraft.block;
 
 import net.minecraft.item.ItemStack;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 
 public class MetaBlocks {
 
 	public static InventoryLinkBlock inventoryLinkBlock;
 	public static InventoryLinkMk2Block inventoryLinkMk2Block;
 	public static MetaOreBlock metaOreBlock;
 
 	public static void initize() {
 		MetaBlocks.inventoryLinkBlock = new InventoryLinkBlock(2666);
 		GameRegistry.registerBlock(MetaBlocks.inventoryLinkBlock, "InventoryLink");
 		LanguageRegistry.addName(MetaBlocks.inventoryLinkBlock, "Inventory Link");
 
 		MetaBlocks.inventoryLinkMk2Block = new InventoryLinkMk2Block(2667);
 		GameRegistry.registerBlock(MetaBlocks.inventoryLinkMk2Block, "InventoryLinkMk2");
 		LanguageRegistry.addName(MetaBlocks.inventoryLinkMk2Block, "Inventory Link Mk2");
 		
 		MetaBlocks.metaOreBlock = new MetaOreBlock(2805);
 		GameRegistry.registerBlock(MetaBlocks.metaOreBlock, MetaOreItem.class, "MetaOreBlock");
 		LanguageRegistry.addName(MetaBlocks.metaOreBlock, "MetaOre Block");
 		
		for (int ix = 0; ix < MetaOreBlock.ORE_NUMBER; ix++) {
 			ItemStack multiBlockStack = new ItemStack(MetaBlocks.metaOreBlock, 1, ix);
 			LanguageRegistry.addName(multiBlockStack, MetaOreBlock.getItemDisplayName(multiBlockStack));
		}
 		
 	}
 }
