 package tk.d3add3d.aputils;
 
 import ic2.api.recipe.*;
 import net.minecraft.block.Block;
 import net.minecraft.block.material.Material;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.item.crafting.FurnaceRecipes;
 import tk.d3add3d.aputils.lib.BlockIDs;
 import tk.d3add3d.aputils.lib.ConfigHandler;
 import tk.d3add3d.aputils.lib.ItemIDs;
 import tk.d3add3d.aputils.lib.Reference;
 import tk.d3add3d.aputils.AputilsElectricItem;
 import tk.d3add3d.aputils.AputilsElectricBreadItem;
 import tk.d3add3d.aputils.AputilsElectricDebugHungerItem;
 import cpw.mods.fml.common.IWorldGenerator;
 import cpw.mods.fml.common.Loader;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.EventHandler;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPostInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 import net.minecraftforge.oredict.*;
 
 @SuppressWarnings("unused")
 @Mod(modid = Reference.MOD_ID, name = Reference.MOD_NAME, version = Reference.VERSION_NUMBER)
 public class AputilsCore {
 	
 	public static Block dreamBlock, slimeBlock, customizerOreBlock;
 	public static Item customizerCrystalItem, customizerCircuitItem, customizerEnergyCoreItem, customizerChargedCrystalItem, customizerEnergyCoreAssemblyItem, customizerBreadItem, customizerBreadAssemblyItem, aputilsDebugHungerItem, aputilsBaconFoodItem, aputilsHotDogFoodItem, aputilsPorkMincedItem, aputilsPorkDoubleMincedItem, aputilsKlobasaFoodItem, aputilsParokFoodItem;
 	public static AputilsCreativeTab tab = new AputilsCreativeTab(AputilsCreativeTab.getNextID(),Reference.MOD_ID);
 	AputilsEventHandler eventhandler = new AputilsEventHandler();
 	
 		
 @EventHandler
 public void preInit(FMLPreInitializationEvent event) {
 	ConfigHandler.init(event.getSuggestedConfigurationFile());
 }
 
 @EventHandler
 public void load(FMLInitializationEvent event) {
 	
 	//init blocks and items
 	dreamBlock = new AputilsBlock(BlockIDs.BLOCK_dream, Material.rock, "dreamBlock").setHardness(0.0F).setResistance(200.0F).setUnlocalizedName("dreamBlock").setStepSound(Block.soundStoneFootstep).setLightValue(1.0F).setCreativeTab(AputilsCore.tab);
 	slimeBlock = new AputilsBlock(BlockIDs.BLOCK_slime, Material.rock, "slimeBlock").setHardness(1.0F).setLightOpacity(4).setUnlocalizedName("slimeBlock").setStepSound(Block.soundSnowFootstep).setLightValue(0.1F).setCreativeTab(AputilsCore.tab);
 	customizerOreBlock = new AputilsBlock(BlockIDs.BLOCK_customizerOre, Material.rock, "customizerOreBlock").setHardness(2.0F).setResistance(1.0F).setUnlocalizedName("customizerOreBlock").setStepSound(Block.soundStoneFootstep).setLightOpacity(7).setCreativeTab(AputilsCore.tab);
 	customizerCrystalItem = new AputilsItem(ItemIDs.ITEM_customizerCrystal).setMaxStackSize(64).setUnlocalizedName("customizerCrystalItem").setCreativeTab(AputilsCore.tab).setTextureName("aputils:customizerCrystalItem");
 	customizerChargedCrystalItem = new AputilsItem(ItemIDs.ITEM_customizerChargedCrystal).setMaxStackSize(64).setUnlocalizedName("customizerChargedCrystalItem").setCreativeTab(AputilsCore.tab).setTextureName("aputils:customizerChargedCrystalItem");
 	customizerCircuitItem = new AputilsItem(ItemIDs.ITEM_customizerCircuit).setMaxStackSize(16).setUnlocalizedName("customizerCircuitItem").setCreativeTab(AputilsCore.tab).setTextureName("aputils:customizerCircuitItem");
 	customizerEnergyCoreItem = new AputilsItem(ItemIDs.ITEM_customizerEnergyCore).setMaxStackSize(1).setUnlocalizedName("customizerEnergyCoreItem").setCreativeTab(AputilsCore.tab).setTextureName("aputils:customizerEnergyCoreItem");
 	customizerEnergyCoreAssemblyItem = new AputilsItem(ItemIDs.ITEM_customizerEnergyCoreAssembly).setMaxStackSize(1).setUnlocalizedName("customizerEnergyCoreAssemblyItem").setCreativeTab(AputilsCore.tab).setTextureName("aputils:customizerEnergyCoreAssemblyItem");
 	customizerBreadItem = new AputilsElectricBreadItem(ItemIDs.ITEM_customizerBread).setMaxStackSize(1).setUnlocalizedName("customizerBreadItem").setCreativeTab(AputilsCore.tab).setTextureName("aputils:customizerBreadItem");
 	customizerBreadAssemblyItem = new AputilsItem(ItemIDs.ITEM_customizerBreadAssembly).setMaxStackSize(16).setUnlocalizedName("customizerBreadAssemblyItem").setCreativeTab(AputilsCore.tab).setTextureName("aputils:customizerBreadAssemblyItem");
 	aputilsDebugHungerItem = new AputilsElectricDebugHungerItem(ItemIDs.ITEM_aputilsDebugHunger).setMaxStackSize(1).setUnlocalizedName("aputilsDebugHungerItem").setCreativeTab(AputilsCore.tab).setTextureName("aputils:customizerEnergyCoreItem");
 	aputilsBaconFoodItem = new AputilsFoodItem(ItemIDs.ITEM_aputilsBaconFood, 7,13.0F).setMaxStackSize(64).setUnlocalizedName("aputilsBaconFoodItem").setCreativeTab(AputilsCore.tab).setTextureName("aputils:aputilsBaconFoodItem");
 	aputilsHotDogFoodItem = new AputilsFoodItem(ItemIDs.ITEM_aputilsHotDogFood, 11, 17.5F).setMaxStackSize(64).setUnlocalizedName("aputilsHotDogFoodItem").setCreativeTab(AputilsCore.tab).setTextureName("aputils:aputilsHotDogFoodItem");
 	aputilsPorkMincedItem = new AputilsItem(ItemIDs.ITEM_aputilsPorkMinced).setMaxStackSize(16).setUnlocalizedName("aputilsPorkMincedItem").setCreativeTab(AputilsCore.tab).setTextureName("aputils:aputilsPorkMincedItem");
 	aputilsPorkDoubleMincedItem = new AputilsItem(ItemIDs.ITEM_aputilsPorkDoubleMinced).setMaxStackSize(16).setUnlocalizedName("aputilsPorkDubleMincedItem").setCreativeTab(AputilsCore.tab).setTextureName("aputils:aputilsPorkDoubleMincedItem");
 	aputilsKlobasaFoodItem = new AputilsFoodItem(ItemIDs.ITEM_aputilsKlobasaFood, 9, 14.0F).setMaxStackSize(64).setUnlocalizedName("aputilsKlobasaFoodItem").setCreativeTab(AputilsCore.tab).setTextureName("aputils:aputilsKlobasaFoodItem");
 	aputilsParokFoodItem = new AputilsFoodItem(ItemIDs.ITEM_aputilsParokFood, 9, 13.0F).setMaxStackSize(64).setUnlocalizedName("aputilsParokFoodItem").setCreativeTab(AputilsCore.tab).setTextureName("aputils:aputilsParokFoodItem");
     	
 	//block registers
 	LanguageRegistry.addName(dreamBlock, "Dream Block");
 	GameRegistry.registerBlock(dreamBlock, "dreamBlock");
 	LanguageRegistry.addName(slimeBlock, "Slime Block");
 	GameRegistry.registerBlock(slimeBlock, "slimeBlock");
 	LanguageRegistry.addName(customizerOreBlock, "Customizer Ore");
 	GameRegistry.registerBlock(customizerOreBlock, "customizerOreBlock");
 	
 	//item registers
 	LanguageRegistry.addName(customizerCrystalItem, "Customizer Crystal");
 	GameRegistry.registerItem(customizerCrystalItem, "customizerCrystalItem");
 	LanguageRegistry.addName(customizerChargedCrystalItem, "Charged Customizer Crystal");
 	GameRegistry.registerItem(customizerChargedCrystalItem, "customizerChargedCrystalItem");
 	LanguageRegistry.addName(customizerCircuitItem, "Customizer Circuit");
 	GameRegistry.registerItem(customizerCircuitItem, "customizerCircuitItem");
 	LanguageRegistry.addName(customizerEnergyCoreItem, "Customizer Energy Core");
 	GameRegistry.registerItem(customizerEnergyCoreItem, "customizerEnergyCoreItem");
 	LanguageRegistry.addName(customizerEnergyCoreAssemblyItem, "Customizer Energy Core Assembly");
 	GameRegistry.registerItem(customizerEnergyCoreAssemblyItem, "customizerEnergyCoreAssemblyItem");
 	LanguageRegistry.addName(customizerBreadItem, "Customizer Bread");
 	GameRegistry.registerItem(customizerBreadItem, "customizerBreadItem");
 	LanguageRegistry.addName(customizerBreadAssemblyItem, "Customizer Bread Assembly");
 	GameRegistry.registerItem(customizerBreadAssemblyItem, "customizerBreadAssemblyItem");
 	LanguageRegistry.addName(aputilsBaconFoodItem, "Cooked Bacon");
 	GameRegistry.registerItem(aputilsBaconFoodItem, "aputilsBaconFoodItem");
 	LanguageRegistry.addName(aputilsHotDogFoodItem, "Hot Dog");
 	GameRegistry.registerItem(aputilsHotDogFoodItem, "aputilsHotDogFoodItem");
 	LanguageRegistry.addName(aputilsPorkMincedItem, "Minced pork meat");
 	GameRegistry.registerItem(aputilsPorkMincedItem, "aputilsPorkMincedItem");
 	LanguageRegistry.addName(aputilsPorkDoubleMincedItem, "Double minced pork meat");
 	GameRegistry.registerItem(aputilsPorkDoubleMincedItem, "aputilsPorkDoubleMincedItem");
 	LanguageRegistry.addName(aputilsKlobasaFoodItem, "Klobasa");
 	GameRegistry.registerItem(aputilsKlobasaFoodItem, "aputilsKlobasaFoodItem");
 	LanguageRegistry.addName(aputilsParokFoodItem, "Parok");
 	GameRegistry.registerItem(aputilsParokFoodItem, "aputilsParokFoodItem");
 	
 	//world gen
 	GameRegistry.registerWorldGenerator(eventhandler);
 	
 	//oredict
 	OreDictionary.registerOre("oreCustomizer", customizerOreBlock);
 	OreDictionary.registerOre("gemCustomizer", new ItemStack(customizerCrystalItem));
 	
	//try to add oreCustomizer to Mystcraft ages.... somehow
	//I have no idea how to do that
	
 }
 
 @EventHandler
 public void postInit(FMLPostInitializationEvent event) {
 	
 	//define ItemStacks
 	ItemStack dreamBlockStack = new ItemStack(dreamBlock);
 	ItemStack slimeBlockStack = new ItemStack(slimeBlock);
 	ItemStack customizerCrystalStack = new ItemStack(customizerCrystalItem);
 	ItemStack customizerOreStack = new ItemStack(customizerOreBlock);
 	ItemStack customizerChargedCrystalStack = new ItemStack(customizerChargedCrystalItem);
 	ItemStack customizerCircuitStack = new ItemStack(customizerCircuitItem);
 	ItemStack customizerEnergyCoreStack = new ItemStack(customizerEnergyCoreItem);
 	ItemStack vanillaDiamondStack = new ItemStack(Item.diamond);
 	ItemStack vanillaRedstoneStack = new ItemStack(Item.redstone);
 	ItemStack vanillaIronStack = new ItemStack(Item.ingotIron);
 	ItemStack vanillaBreadStack = new ItemStack(Item.bread);
 	ItemStack vanillaLimeDyeStack = new ItemStack(Item.dyePowder, 1, 10);
 	ItemStack vanillaPorkStack = new ItemStack(Item.porkRaw);
 	ItemStack aputilsPorkMincedStack = new ItemStack(aputilsPorkMincedItem);
 	ItemStack aputilsPorkDoubleMincedStack = new ItemStack(aputilsPorkDoubleMincedItem);
 	ItemStack aputilsParokStack = new ItemStack(aputilsParokFoodItem);
 	
 	//smelting registers
 	GameRegistry.addSmelting(customizerOreBlock.blockID, new ItemStack(customizerCrystalItem, 2), 0.1F);
 	FurnaceRecipes.smelting().addSmelting(customizerCrystalItem.itemID, 0, new ItemStack(customizerChargedCrystalItem), 0.2F);
 	FurnaceRecipes.smelting().addSmelting(customizerEnergyCoreAssemblyItem.itemID, 0, new ItemStack(customizerEnergyCoreItem), 0.3F);
 	FurnaceRecipes.smelting().addSmelting(customizerBreadAssemblyItem.itemID, 0, new ItemStack(customizerBreadItem), 0.8F);
 	FurnaceRecipes.smelting().addSmelting(Item.rottenFlesh.itemID, 0, new ItemStack(Item.leather), 0.1F);
 	FurnaceRecipes.smelting().addSmelting(Item.porkCooked.itemID, 0, new ItemStack(aputilsBaconFoodItem), 0.1F);
 	
 	
 	//crafting registers
 	GameRegistry.addRecipe(new ItemStack(dreamBlock, 8), " x ", " x ", "   ", 'x', customizerCrystalStack);
 	GameRegistry.addShapelessRecipe(new ItemStack(slimeBlock, 3), customizerCrystalStack, vanillaLimeDyeStack);
 	GameRegistry.addRecipe(new ItemStack(customizerEnergyCoreAssemblyItem), "dxd", "xyx", "dxd", 'x', customizerChargedCrystalStack, 'y', customizerCrystalStack, 'd', vanillaDiamondStack);
 	GameRegistry.addRecipe(new ItemStack(customizerCircuitItem), "rcr", "chc", "rir", 'r', vanillaRedstoneStack, 'c', customizerCrystalStack, 'h', customizerChargedCrystalStack, 'i', vanillaIronStack);
 	GameRegistry.addRecipe(new ItemStack(customizerBreadAssemblyItem), "qrq", "fcf", "bqb", 'q', customizerCircuitStack, 'r', customizerCrystalStack, 'c', customizerEnergyCoreStack, 'f', customizerChargedCrystalStack, 'b', vanillaBreadStack);
 	GameRegistry.addShapelessRecipe(new ItemStack(aputilsHotDogFoodItem), aputilsParokStack, vanillaBreadStack);
 	
 	//ic2 macerating recipes
 	if (Loader.isModLoaded("IC2")) {
 	Recipes.macerator.addRecipe(new RecipeInputItemStack(customizerOreStack, 1), null, new ItemStack(customizerCrystalItem, 3));
 	Recipes.macerator.addRecipe(new RecipeInputItemStack(dreamBlockStack, 2), null, new ItemStack(Item.feather, 1));
 	Recipes.extractor.addRecipe(new RecipeInputItemStack(slimeBlockStack, 16), null, new ItemStack(Item.slimeBall, 4));
 	FurnaceRecipes.smelting().addSmelting(Item.slimeBall.itemID, 0, ic2.api.item.Items.getItem("rubber"), 0.1F);
 	Recipes.macerator.addRecipe(new RecipeInputItemStack(vanillaPorkStack), null, new ItemStack(aputilsPorkMincedItem, 2));
 	Recipes.compressor.addRecipe(new RecipeInputItemStack(aputilsPorkMincedStack, 2), null, new ItemStack(aputilsKlobasaFoodItem));
 	Recipes.macerator.addRecipe(new RecipeInputItemStack(aputilsPorkMincedStack), null, new ItemStack(aputilsPorkDoubleMincedItem));
 	Recipes.compressor.addRecipe(new RecipeInputItemStack(aputilsPorkDoubleMincedStack, 2), null, new ItemStack(aputilsParokFoodItem));
 	}
 	
 }
 
 }
