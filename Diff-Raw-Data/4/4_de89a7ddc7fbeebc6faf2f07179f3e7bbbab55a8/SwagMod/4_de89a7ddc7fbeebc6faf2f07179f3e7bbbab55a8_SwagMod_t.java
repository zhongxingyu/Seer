 package jack_1197.swag.common;
 
 import jack_1197.swag.client.ClientPacketHandler;
 import net.minecraft.block.Block;
 import net.minecraft.block.material.Material;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.item.EnumToolMaterial;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemAxe;
 import net.minecraft.item.ItemHoe;
 import net.minecraft.item.ItemPickaxe;
 import net.minecraft.item.ItemSpade;
 import net.minecraft.item.ItemStack;
 import net.minecraft.item.ItemSword;
 import net.minecraftforge.common.EnumHelper;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.Init;
 import cpw.mods.fml.common.Mod.Instance;
 import cpw.mods.fml.common.Mod.PreInit;
 import cpw.mods.fml.common.SidedProxy;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPostInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.common.network.NetworkMod.SidedPacketHandler;
 import cpw.mods.fml.common.network.NetworkRegistry;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 
 /*
  * Swag mod by jack_1197 as this is one of my first mods some parts may be little modified tutorial code snippets
  */
 // annotations, you dont say?
 @Mod(modid = "SwagMod", name = "Swag Mod", version = "0.0.6 Pre-Alpha")
 @NetworkMod(clientSideRequired = true, serverSideRequired = false, clientPacketHandlerSpec = @SidedPacketHandler(channels = { "SwagMod" }, packetHandler = ClientPacketHandler.class),
 		serverPacketHandlerSpec = @SidedPacketHandler(channels = { "SwagMod" }, packetHandler = ServerPacketHandler.class))
 public class SwagMod {
 	// main declarations and stuffs
 
 	public static enum packetType {
 		SWAG_YOLO_CONVERTER_UPDATE, FUSER_UPDATE, FUSER_BUTTON_CLICK
 	}
 
 	// Tool Materials
 	static EnumToolMaterial swagToolMaterial = EnumHelper.addToolMaterial("swag", 3, 3000, 12.5F, 16, 15);
 	static EnumToolMaterial yoloToolMaterial = EnumHelper.addToolMaterial("yolo", 3, 1000, 18.0F, 22, 30);
 
 	static EnumToolMaterial swagYoloToolMaterial = EnumHelper.addToolMaterial("swagYolo", 3, 2300, 15.0F, 19, 30);
 
 	// Block declarations
 	public static final Block swagOreBlock = new Block(500, 0, Material.rock).setHardness(2.0f).setStepSound(Block.soundStoneFootstep).setLightValue(0.3f)
 			.setCreativeTab(CreativeTabs.tabBlock).setBlockName("SwagOre").setTextureFile(CommonProxy.BLOCKS);
 
 	public static final Block yoloOreBlock = new Block(501, 1, Material.rock).setHardness(2.0f).setStepSound(Block.soundStoneFootstep).setLightValue(0.4f)
 			.setCreativeTab(CreativeTabs.tabBlock).setBlockName("YoloOre").setTextureFile(CommonProxy.BLOCKS);
 	public static final Block swagYoloConverterBlock = new SwagYoloConverterBlock(510, 5, Material.rock).setHardness(2.0f).setStepSound(Block.soundStoneFootstep)
 			.setCreativeTab(CreativeTabs.tabDecorations).setBlockName("SwagYoloConverter");
 
 	public static final Block swagYoloConverterFueledBlock = new SwagYoloConverterBlock(511, 7, Material.rock).setHardness(2.0f).setStepSound(Block.soundStoneFootstep)
 			.setCreativeTab(CreativeTabs.tabDecorations).setBlockName("SwagYoloConverterLit").setLightValue(0.9F);
 
 	public static final Block fuserBlock = new FuserBlock(520, 7, Material.rock).setHardness(2.0f).setStepSound(Block.soundStoneFootstep).setCreativeTab(CreativeTabs.tabDecorations)
 			.setBlockName("Fuser").setLightValue(0.9F);
 
	public static final Item swagfoodItem = ((CustomFood)new CustomFood(6000, 1, true).setMaxStackSize(32).setIconCoord(0, 0).setItemName("SwagEssence").setCreativeTab(CreativeTabs.tabMaterials)
			.setTextureFile(CommonProxy.ITEMS)).setHasNetherFX(true);
 	// Item Declarations
 	public static final Item swagEssenceItem = new Item(5000).setMaxStackSize(32).setIconCoord(0, 0).setItemName("SwagEssence").setCreativeTab(CreativeTabs.tabMaterials)
 			.setTextureFile(CommonProxy.ITEMS);
 	public static final Item swagDropItem = new Item(5001).setMaxStackSize(32).setIconCoord(0, 1).setItemName("SwagDrop").setCreativeTab(CreativeTabs.tabMaterials)
 			.setTextureFile(CommonProxy.ITEMS);
 
 	public static final Item swagOrbItem = new Item(5002).setMaxStackSize(16).setIconCoord(0, 2).setItemName("SwagOrb").setCreativeTab(CreativeTabs.tabMaterials)
 			.setTextureFile(CommonProxy.ITEMS);
 	public static final Item yoloEssenceItem = new Item(5010).setMaxStackSize(32).setIconCoord(1, 0).setItemName("YoloEssence").setCreativeTab(CreativeTabs.tabMaterials)
 			.setTextureFile(CommonProxy.ITEMS);
 	public static final Item yoloEssenceDenseItem = new Item(5011).setMaxStackSize(16).setIconCoord(1, 3).setItemName("YoloEssenceDense").setCreativeTab(CreativeTabs.tabMaterials)
 			.setTextureFile(CommonProxy.ITEMS);
 	public static final Item yoloDropItem = new Item(5012).setMaxStackSize(32).setIconCoord(1, 1).setItemName("YoloDrop").setCreativeTab(CreativeTabs.tabMaterials)
 			.setTextureFile(CommonProxy.ITEMS);
 	public static final Item yoloDropDenseItem = new Item(5013).setMaxStackSize(16).setIconCoord(1, 4).setItemName("YoloDropDense").setCreativeTab(CreativeTabs.tabMaterials)
 			.setTextureFile(CommonProxy.ITEMS);
 
 	public static final Item yoloOrbItem = new Item(5014).setMaxStackSize(32).setIconCoord(1, 2).setItemName("YoloOrb").setCreativeTab(CreativeTabs.tabMaterials)
 			.setTextureFile(CommonProxy.ITEMS);
 
 	public static final Item yoloSwagIngotItem = new Item(5020).setIconCoord(2, 0).setItemName("YoloSwagIngot").setCreativeTab(CreativeTabs.tabMaterials).setTextureFile(CommonProxy.ITEMS);
 
 	// Tool declarations
 	public static final Item swagSwordItem = new ItemSword(5100, swagToolMaterial).setIconCoord(0, 5).setItemName("SwagSword").setCreativeTab(CreativeTabs.tabCombat).setTextureFile(CommonProxy.ITEMS);
 	public static final Item swagShovelItem = new ItemSpade(5101, swagToolMaterial).setIconCoord(0, 6).setItemName("SwagSpade").setCreativeTab(CreativeTabs.tabTools).setTextureFile(CommonProxy.ITEMS);
 	public static final Item swagPickaxeItem = new ItemPickaxe(5102, swagToolMaterial).setIconCoord(0, 7).setItemName("SwagPickaxe").setCreativeTab(CreativeTabs.tabTools).setTextureFile(CommonProxy.ITEMS);
 	public static final Item swagAxeItem = new ItemAxe(5103, swagToolMaterial).setIconCoord(0, 8).setItemName("SwagAxe").setCreativeTab(CreativeTabs.tabTools).setTextureFile(CommonProxy.ITEMS);
 	public static final Item swagHoeItem = new ItemHoe(5104, swagToolMaterial).setIconCoord(0, 9).setItemName("SwagHoe").setCreativeTab(CreativeTabs.tabTools).setTextureFile(CommonProxy.ITEMS);// I have absolutely no
 																																									// idea
 																																									// why either...
 
 	public static final Item yoloSwordItem = new ItemSword(5110, yoloToolMaterial).setIconCoord(1, 5).setItemName("YoloSword").setCreativeTab(CreativeTabs.tabCombat).setTextureFile(CommonProxy.ITEMS);
 	public static final Item yoloShovelItem = new ItemSpade(5111, yoloToolMaterial).setIconCoord(1, 6).setItemName("YoloSpade").setCreativeTab(CreativeTabs.tabTools).setTextureFile(CommonProxy.ITEMS);
 	public static final Item yoloPickaxeItem = new ItemPickaxe(5112, yoloToolMaterial).setIconCoord(1, 7).setItemName("YoloPickaxe").setCreativeTab(CreativeTabs.tabTools).setTextureFile(CommonProxy.ITEMS);
 	public static final Item yoloAxeItem = new ItemAxe(5113, yoloToolMaterial).setIconCoord(1, 8).setItemName("YoloAxe").setCreativeTab(CreativeTabs.tabTools).setTextureFile(CommonProxy.ITEMS);
 	public static final Item yoloHoeItem = new ItemHoe(5114, yoloToolMaterial).setIconCoord(1, 9).setItemName("YoloHoe").setCreativeTab(CreativeTabs.tabTools).setTextureFile(CommonProxy.ITEMS);// oh well, YOLO!!
 
 	// technical declarations
 	private GuiHandler guiHandler = new GuiHandler();
 	private ClientPacketHandler clientPacketHandler = new ClientPacketHandler();
 	private ServerPacketHandler serverPacketHandler = new ServerPacketHandler();
 	public static SwagModOreGenerator swagModOreGenerator = new SwagModOreGenerator();
 
 	protected static boolean swagYoloConvCanExplode = true;;
 
 	@Instance("SwagMod")
 	public static SwagMod Instance;
 
 	@SidedProxy(clientSide = "jack_1197.swag.client.ClientProxy", serverSide = "jack_1197.swag.common.CommonProxy")
 	public static CommonProxy proxy;
 
 	static public int getSwagValue(ItemStack item) {
 		if (item.getItem() == SwagMod.swagEssenceItem) {
 			return 600;
 		} else if (item.getItem() == SwagMod.swagDropItem) {
 			return 4200;
 		} else if (item.getItem() == new ItemStack(SwagMod.swagOreBlock).getItem()) {
 			return 200;
 		} else if (item.getItem() == SwagMod.swagSwordItem) {
 			return 24000;
 		} else if (item.getItem() == SwagMod.yoloSwagIngotItem) {
 			return 24000;
 		} else if (item.getItem() == SwagMod.swagOrbItem) {
 			return 18000;
 		} else if (item.getItem() == Item.diamond) {
 			return 3000;
 		}
 		return 0;
 	}
 
 	static public int getYoloValue(ItemStack item) {
 		if (item.getItem() == Item.cookie) {
 			return 16;
 		}
 		if (item.getItem() == Item.ingotGold) {
 			return 128;
 		}
 		if (item.getItem() == Item.hoeDiamond) {
 			return 1024;
 		}
 		if (item.getItem() == Item.swordWood) {
 			return 16;
 		}
 		if (item.getItem() == Item.enderPearl) {
 			return 128;
 		}
 		if (item.getItem() == Item.pickaxeGold) {
 			return 256;
 		}
 		if (item.getItem() == new ItemStack(Block.tnt).getItem()) {
 			return 128;
 		}
 		if (item.getItem() == new ItemStack(Block.woodenButton).getItem()) {
 			return 16;
 		}
 		return 1;
 	}
 
 	@PreInit
 	public void postInit(FMLPostInitializationEvent event) {
 
 	}
 
 	@Init
 	public void preInit(FMLInitializationEvent event) {
 		// smelting
 		GameRegistry.addSmelting(swagOreBlock.blockID, new ItemStack(swagEssenceItem), 0.6f);
 		GameRegistry.addSmelting(yoloOreBlock.blockID, new ItemStack(yoloEssenceItem), 1.0f);
 
 		// recipies
 		// swag crafting
 		GameRegistry.addRecipe(new ItemStack(swagDropItem), " E ", "EEE", "EEE", 'E', new ItemStack(swagEssenceItem));
 		GameRegistry.addRecipe(new ItemStack(swagOrbItem), "GDG", "DID", "GDG", 'G', new ItemStack(Block.glass), 'D', new ItemStack(swagDropItem), 'I', new ItemStack(Item.diamond));
 
 		// yolo crafting
 		GameRegistry.addShapelessRecipe(new ItemStack(yoloEssenceDenseItem), new ItemStack(yoloEssenceItem), new ItemStack(yoloEssenceItem), new ItemStack(yoloEssenceItem), new ItemStack(
 				yoloEssenceItem));
 		GameRegistry.addShapelessRecipe(new ItemStack(yoloDropDenseItem), new ItemStack(yoloDropItem), new ItemStack(yoloDropItem), new ItemStack(yoloDropItem), new ItemStack(yoloDropItem));
 		GameRegistry.addShapelessRecipe(new ItemStack(yoloDropItem), new ItemStack(yoloEssenceDenseItem), new ItemStack(yoloEssenceDenseItem), new ItemStack(yoloEssenceDenseItem),
 				new ItemStack(yoloEssenceDenseItem), new ItemStack(yoloEssenceDenseItem), new ItemStack(yoloEssenceDenseItem), new ItemStack(yoloEssenceDenseItem), new ItemStack(
 						yoloEssenceDenseItem));
 		GameRegistry.addShapelessRecipe(new ItemStack(yoloOrbItem), new ItemStack(yoloDropDenseItem), new ItemStack(yoloDropDenseItem), new ItemStack(yoloDropDenseItem), new ItemStack(
 				yoloDropDenseItem), new ItemStack(yoloDropDenseItem), new ItemStack(yoloDropDenseItem), new ItemStack(yoloDropDenseItem), new ItemStack(yoloDropDenseItem));
 		GameRegistry.addShapelessRecipe(new ItemStack(yoloDropDenseItem, 8), new ItemStack(yoloOrbItem));
 		GameRegistry.addShapelessRecipe(new ItemStack(yoloDropItem, 4), new ItemStack(yoloDropDenseItem));
 		GameRegistry.addShapelessRecipe(new ItemStack(yoloEssenceDenseItem, 8), new ItemStack(yoloDropItem));
 		GameRegistry.addShapelessRecipe(new ItemStack(yoloEssenceItem, 4), new ItemStack(yoloEssenceDenseItem));
 
 		// swag tools
 		GameRegistry.addRecipe(new ItemStack(swagSwordItem), "EDE", "ETE", "IOI", 'E', new ItemStack(swagEssenceItem), 'D', new ItemStack(swagDropItem), 'T',
 				new ItemStack(Item.swordDiamond), 'I', new ItemStack(Item.diamond), 'O', new ItemStack(swagOrbItem));
 		GameRegistry.addRecipe(new ItemStack(swagShovelItem), "EDE", "ETE", "IOI", 'E', new ItemStack(swagEssenceItem), 'D', new ItemStack(swagDropItem), 'T', new ItemStack(
 				Item.shovelDiamond), 'I', new ItemStack(Item.diamond), 'O', new ItemStack(swagOrbItem));
 		GameRegistry.addRecipe(new ItemStack(swagPickaxeItem), "EDE", "ETE", "IOI", 'E', new ItemStack(swagEssenceItem), 'D', new ItemStack(swagDropItem), 'T', new ItemStack(
 				Item.pickaxeDiamond), 'I', new ItemStack(Item.diamond), 'O', new ItemStack(swagOrbItem));
 		GameRegistry.addRecipe(new ItemStack(swagAxeItem), "EDE", "ETE", "IOI", 'E', new ItemStack(swagEssenceItem), 'D', new ItemStack(swagDropItem), 'T', new ItemStack(Item.axeDiamond),
 				'I', new ItemStack(Item.diamond), 'O', new ItemStack(swagOrbItem));
 		GameRegistry.addRecipe(new ItemStack(swagHoeItem), "EDE", "ETE", "IOI", 'E', new ItemStack(swagEssenceItem), 'D', new ItemStack(swagDropItem), 'T', new ItemStack(Item.hoeDiamond),
 				'I', new ItemStack(Item.diamond), 'O', new ItemStack(swagOrbItem));
 
 		// YOLO tools
 		GameRegistry.addRecipe(new ItemStack(yoloSwordItem), "EDE", "ETE", "IOI", 'E', new ItemStack(yoloEssenceItem), 'D', new ItemStack(yoloDropItem), 'T',
 				new ItemStack(Item.swordDiamond), 'I', new ItemStack(Item.diamond), 'O', new ItemStack(yoloOrbItem));
 		GameRegistry.addRecipe(new ItemStack(yoloShovelItem), "EDE", "ETE", "IOI", 'E', new ItemStack(yoloEssenceItem), 'D', new ItemStack(yoloDropItem), 'T', new ItemStack(
 				Item.shovelDiamond), 'I', new ItemStack(Item.diamond), 'O', new ItemStack(yoloOrbItem));
 		GameRegistry.addRecipe(new ItemStack(yoloPickaxeItem), "EDE", "ETE", "IOI", 'E', new ItemStack(yoloEssenceItem), 'D', new ItemStack(yoloDropItem), 'T', new ItemStack(
 				Item.pickaxeDiamond), 'I', new ItemStack(Item.diamond), 'O', new ItemStack(yoloOrbItem));
 		GameRegistry.addRecipe(new ItemStack(yoloAxeItem), "EDE", "ETE", "IOI", 'E', new ItemStack(yoloEssenceItem), 'D', new ItemStack(yoloDropItem), 'T', new ItemStack(Item.axeDiamond),
 				'I', new ItemStack(Item.diamond), 'O', new ItemStack(yoloOrbItem));
 		GameRegistry.addRecipe(new ItemStack(yoloHoeItem), "EDE", "ETE", "IOI", 'E', new ItemStack(yoloEssenceItem), 'D', new ItemStack(yoloDropItem), 'T', new ItemStack(Item.hoeDiamond),
 				'I', new ItemStack(Item.diamond), 'O', new ItemStack(yoloOrbItem));
 
 		// registration
 		GameRegistry.registerBlock(swagOreBlock, "swagOre");
 		GameRegistry.registerBlock(yoloOreBlock, "yoloOre");
 		GameRegistry.registerBlock(swagYoloConverterBlock, "swagYoloConverter");
 		GameRegistry.registerBlock(fuserBlock, "fuser");
 
 		GameRegistry.registerTileEntity(SwagYoloConverterTileEntity.class, "swagYoloConverter");
 		GameRegistry.registerTileEntity(FuserTileEntity.class, "fuser");
 
 		GameRegistry.registerWorldGenerator(swagModOreGenerator);
 
 		// launguage registry
 		LanguageRegistry.addName(swagOreBlock, "Swagite Ore");
 		LanguageRegistry.addName(yoloOreBlock, "YOLO Ore");
 
 		LanguageRegistry.addName(swagYoloConverterBlock, "YOLO-o-Matic");
 		LanguageRegistry.addName(fuserBlock, "Fuser");
 
 		LanguageRegistry.addName(swagEssenceItem, "Swag Essence");
 		LanguageRegistry.addName(swagDropItem, "Swag Drop");
 		LanguageRegistry.addName(swagOrbItem, "Swag Orb");
 
 		LanguageRegistry.addName(yoloEssenceItem, "Teir 1 YOLO");
 		LanguageRegistry.addName(yoloEssenceDenseItem, "Teir 1 YOLO X4");
 		LanguageRegistry.addName(yoloDropItem, "Teir 2 YOLO");
 		LanguageRegistry.addName(yoloDropDenseItem, "Teir 2 YOLO X4");
 		LanguageRegistry.addName(yoloOrbItem, "Teir 3 YOLO");
 
 		LanguageRegistry.addName(yoloSwagIngotItem, "SwagYOLO Alloy");
 
 		LanguageRegistry.addName(swagSwordItem, "Swag Sword");
 		LanguageRegistry.addName(swagShovelItem, "Swag Spade");
 		LanguageRegistry.addName(swagPickaxeItem, "Swag Pickaxe");
 		LanguageRegistry.addName(swagAxeItem, "Swag Axe");
 		LanguageRegistry.addName(swagHoeItem, "Swag Hoe");
 		LanguageRegistry.addName(yoloSwordItem, "YOLO Sword");
 		LanguageRegistry.addName(yoloShovelItem, "YOLO Spade");
 		LanguageRegistry.addName(yoloPickaxeItem, "YOLO Pickaxe");
 		LanguageRegistry.addName(yoloAxeItem, "YOLO Axe");
 		LanguageRegistry.addName(yoloHoeItem, "YOLO Hoe");
 
 		// misc
 		proxy.registerRenderers();
 		NetworkRegistry.instance().registerGuiHandler(this, guiHandler);
 	}
 
 	@PreInit
 	public void preInit(FMLPreInitializationEvent event) {
 
 	}
 }
