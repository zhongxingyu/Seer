 package modJam;
 
 import java.util.logging.Level;
 
 import net.minecraft.block.Block;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.item.EnumArmorMaterial;
 import net.minecraft.item.EnumToolMaterial;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.item.crafting.CraftingManager;
 import net.minecraft.item.crafting.FurnaceRecipes;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraftforge.common.Configuration;
 import net.minecraftforge.common.EnumHelper;
 import net.minecraftforge.common.ForgeDirection;
 import net.minecraftforge.oredict.OreDictionary;
 import net.minecraftforge.oredict.ShapedOreRecipe;
 import cpw.mods.fml.common.FMLLog;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.Init;
 import cpw.mods.fml.common.Mod.Instance;
 import cpw.mods.fml.common.Mod.PostInit;
 import cpw.mods.fml.common.Mod.PreInit;
 import cpw.mods.fml.common.SidedProxy;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPostInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.common.network.NetworkRegistry;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 import cpw.mods.fml.relauncher.Side;
 
 @Mod(modid="fuj1n.modJam", name=CommonProxyModJam.modName, version=CommonProxyModJam.version)
 @NetworkMod(clientSideRequired=true, serverSideRequired=false, channels={"fuj1nAMetaPacket"}, packetHandler = PacketHandler.class)
 
 public class ModJam {
 	@SidedProxy(serverSide="modJam.CommonProxyModJam", clientSide="modJam.ClientProxyModJam")
 	public static CommonProxyModJam proxy;
 	public static Configuration config;
 	
 	//Config values
 	//Blocks
 	public static int oreAwesomeID = 1024;
 	public static int[] woodChairIDs = {
 		1025, 1026, 1027, 1028
 	};
 	public static int[] stoneChairIDs = {
 		1029, 1030, 1031, 1032
 	};
 	public static int woodTableID = 1033;
 	public static int stoneTableID = 1034;
 	public static int awesomeBlockID = 1035;
 	public static int awesomeBlockStandardID = 1036;
 	public static int awesomeBlockCreeperID = 1037;
 	public static int lightGeneratorID = 1038;
 	//Items
 	public static int ingotAwesomeID = 3240;
 	public static int woodChairID = 3241;
 	public static int stoneChairID = 3242;
 	public static int awesomeArmorHelmetID = 3243;
 	public static int awesomeArmorChestplateID = 3244;
 	public static int awesomeArmorLeggingsID = 3245;
 	public static int awesomeArmorBootsID = 3246;
 	public static int awesomeToolPickaxeID = 3247;
 	public static int awesomeToolShovelID = 3248;
 	public static int awesomeToolSwordID = 3249;
 	public static int awesomeToolAxeID = 3250;
 	public static int awesomeToolHoeID = 3251;
 	public static int darkExtractID = 3252;
 	public static int rotationToolID = 3253;
 	//End Config values
 	//Blocks
 	public static Block awesomeOre;
 	public static Block woodChairNorth;
 	public static Block woodChairEast;
 	public static Block woodChairSouth;
 	public static Block woodChairWest;
 	public static Block stoneChairNorth;
 	public static Block stoneChairEast;
 	public static Block stoneChairSouth;
 	public static Block stoneChairWest;
 	public static Block woodTable;
 	public static Block stoneTable;
 	public static Block awesomeBlock;
 	public static Block awesomeBlockStandard;
 	public static Block awesomeBlockCreeper;
 	public static Block lightGen;
 	//Items
 	public static Item awesomeIngot;
 	public static Item woodChair;
 	public static Item stoneChair;
 	public static Item awesomeHelmet;
 	public static Item awesomeChestplate;
 	public static Item awesomeLeggings;
 	public static Item awesomeBoots;
 	public static Item awesomePickaxe;
 	public static Item awesomeShovel;
 	public static Item awesomeSword;
 	public static Item awesomeAxe;
 	public static Item awesomeHoe;
 	public static Item darkExtract;
 	public static Item rotationTool;
 	//Materials
 	public static EnumArmorMaterial awesomeArmorMaterial;
 	public static EnumToolMaterial awesomeToolMaterial;
 	//CreativeTabs
 	public static CreativeTabs modJamCreativeTab;
 	//Sub Names
 	private static final String[] awesomeColors = { 
 		"White", "Orange", "Magenta",
 		"Light-Blue", "Yellow", "Lime", "Pink", "Gray", "Light-Gray", "Cyan",
 		"Purple", "Blue", "Brown", "Green", "Red", "Black"
 	};
 	
 	@Instance("fuj1n.modJam")
 	public static ModJam instance;
 	
 	@PreInit
 	public void PreInit(FMLPreInitializationEvent event){
 		proxy.preInit();
 		config = new Configuration(event.getSuggestedConfigurationFile());
 		config.load();
 		//Blocks
 		oreAwesomeID = config.getBlock("Awesome Ore ID", oreAwesomeID).getInt();
 		woodChairIDs[0] = config.getBlock("Wooden Chair ID Set(of 4)", woodChairIDs[0]).getInt();
 		stoneChairIDs[0] = config.getBlock("Stone Chair ID Set(of 4)", stoneChairIDs[0]).getInt();
 		woodTableID = config.getBlock("Wooden Table ID", woodTableID).getInt();
 		stoneTableID = config.getBlock("Stone Table ID", stoneTableID).getInt();
 		refreshChairIDs();
 		awesomeBlockID = config.getBlock("Awesome Block ID", awesomeBlockID).getInt();
 		awesomeBlockStandardID = config.getBlock("Standard Awesome Block ID", awesomeBlockStandardID).getInt();
 		awesomeBlockCreeperID = config.getBlock("Creeper-textured Awesome Block ID", awesomeBlockCreeperID).getInt();
 		lightGeneratorID = config.getBlock("Light Generator ID", lightGeneratorID).getInt();
 		//Items
 		ingotAwesomeID = config.getItem("Awesome Ingot ID", ingotAwesomeID).getInt();
 		woodChairID = config.getItem("Wooden Chair Item ID", woodChairID).getInt();
 		stoneChairID = config.getItem("Stone Chair Item ID", stoneChairID).getInt();
 		awesomeArmorHelmetID = config.getItem("Awesome Helmet ID", awesomeArmorHelmetID).getInt();
 		awesomeArmorChestplateID = config.getItem("Awesome Chestplate ID", awesomeArmorChestplateID).getInt();
 		awesomeArmorLeggingsID = config.getItem("Awesome Leggings ID", awesomeArmorLeggingsID).getInt();
 		awesomeArmorBootsID = config.getItem("Awesome Boots ID", awesomeArmorBootsID).getInt();
 		awesomeToolPickaxeID = config.getItem("Awesome Pickaxe ID", awesomeToolPickaxeID).getInt();
 		awesomeToolShovelID = config.getItem("Awesome Shovel ID", awesomeToolShovelID).getInt();
 		awesomeToolSwordID = config.getItem("Awesome Sword ID", awesomeToolSwordID).getInt();
 		awesomeToolAxeID = config.getItem("Awesome Axe ID", awesomeToolAxeID).getInt();
 		awesomeToolHoeID = config.getItem("Awesome Hoe ID", awesomeToolHoeID).getInt();
 		darkExtractID = config.getItem("Dark Extract ID", darkExtractID).getInt();
 		rotationToolID = config.getItem("Rotation Tool ID", rotationToolID).getInt();
 		config.save();
 	} 
 	
 	public void refreshChairIDs(){
 		woodChairIDs[1] = woodChairIDs[0] + 1;
 		woodChairIDs[2] = woodChairIDs[1] + 1;
 		woodChairIDs[3] = woodChairIDs[2] + 1;
 		stoneChairIDs[1] = stoneChairIDs[0] + 1;
 		stoneChairIDs[2] = stoneChairIDs[1] + 1;
 		stoneChairIDs[3] = stoneChairIDs[2] + 1;
 	}
 	
 	@Init
 	public void Init(FMLInitializationEvent event){
     	if(event.getSide() == Side.CLIENT){
     		new UpdaterClient();
     		registerCreativeTab();
     	}else if(event.getSide() == Side.SERVER){
     		new UpdaterServer();
     	}else{
     		log("Failed to detect current side.", Level.SEVERE);
     	}
     	NetworkRegistry.instance().registerGuiHandler(this, new GuiHandler());
 		proxy.Init();
 		initAllMaterials();
 		initAllItems();
 		initAllBlocks();
 		registerAllBlocks();
 		addAllNames();
 		addAllCrafting();
 		addHeadRecipe();
 		addAllSmelting();
 		registerAllOreDictionary();
 		populateGenChest();
 		registerAllWorldGenerators();
 		BlockGlobalFurniturePlacementHandler.initPlacementWhitelist();
 	}
 	
 	@PostInit
 	public void PostInit(FMLPostInitializationEvent event){
 		proxy.postInit();
 	}
 	
 	public void initAllMaterials(){
 		awesomeArmorMaterial = EnumHelper.addArmorMaterial("AWESOME", 13, new int[]{3, 7, 5, 2}, 25);
 		awesomeToolMaterial = EnumHelper.addToolMaterial("AWESOME", 3, 280, 7.0F, 2, 22);
 	}
 	
 	public void initAllBlocks(){
 		awesomeOre = new BlockAwesomeOre(oreAwesomeID).setHardness(5F).setResistance(5F).setCreativeTab(modJamCreativeTab).setUnlocalizedName("fuj1n.modJam.AwesomeOre");
 		woodChairNorth = new BlockChair(woodChairIDs[0], ForgeDirection.NORTH, Block.planks, woodChair.itemID).setHardness(0.3F).setUnlocalizedName("fuj1n.modJam.tileChair");
 		woodChairEast = new BlockChair(woodChairIDs[1], ForgeDirection.EAST, Block.planks, woodChair.itemID).setHardness(0.3F).setUnlocalizedName("fuj1n.modJam.tileChair");
 		woodChairSouth = new BlockChair(woodChairIDs[2], ForgeDirection.SOUTH, Block.planks, woodChair.itemID).setHardness(0.3F).setUnlocalizedName("fuj1n.modJam.tileChair");
 		woodChairWest = new BlockChair(woodChairIDs[3], ForgeDirection.WEST, Block.planks, woodChair.itemID).setHardness(0.3F).setUnlocalizedName("fuj1n.modJam.tileChair");
 		stoneChairNorth = new BlockChair(stoneChairIDs[0], ForgeDirection.NORTH, Block.stone, stoneChair.itemID).setHardness(0.3F).setUnlocalizedName("fuj1n.modJam.tileChair");
 		stoneChairEast = new BlockChair(stoneChairIDs[1], ForgeDirection.EAST, Block.stone, stoneChair.itemID).setHardness(0.3F).setUnlocalizedName("fuj1n.modJam.tileChair");
 		stoneChairSouth = new BlockChair(stoneChairIDs[2], ForgeDirection.SOUTH, Block.stone, stoneChair.itemID).setHardness(0.3F).setUnlocalizedName("fuj1n.modJam.tileChair");
 		stoneChairWest = new BlockChair(stoneChairIDs[3], ForgeDirection.WEST, Block.stone, stoneChair.itemID).setHardness(0.3F).setUnlocalizedName("fuj1n.modJam.tileChair");
 		woodTable = new BlockTable(woodTableID, Block.planks).setHardness(0.3F).setCreativeTab(modJamCreativeTab).setUnlocalizedName("fuj1n.modJam.woodTable");
 		stoneTable = new BlockTable(stoneTableID, Block.stone).setHardness(0.3F).setCreativeTab(modJamCreativeTab).setUnlocalizedName("fuj1n.modJam.stoneTable");
 		awesomeBlock = new BlockAwesome(awesomeBlockID, "none").setHardness(0.8F).setResistance(5F).setCreativeTab(modJamCreativeTab).setUnlocalizedName("fuj1n.modJam.awesomeBlock");
 		awesomeBlockStandard = new BlockAwesome(awesomeBlockStandardID, "standard").addAdditionalInfo("Standard-Textured").setHardness(0.8F).setResistance(5F).setCreativeTab(modJamCreativeTab).setUnlocalizedName("fuj1n.modJam.awesomeBlock.standard");
 		awesomeBlockCreeper = new BlockAwesome(awesomeBlockCreeperID, "creeper").addAdditionalInfo("Creeper-Textured").setHardness(0.8F).setResistance(10F).setCreativeTab(modJamCreativeTab).setUnlocalizedName("fuj1n.modJam.awesomeBlock.creeper");
 		lightGen = new BlockLightGenerator(lightGeneratorID).setHardness(0.3F).setCreativeTab(modJamCreativeTab).setUnlocalizedName("fuj1n.modJam.lightGenerator");
 	}
 	
 	public void initAllItems(){
 		awesomeIngot = new ItemAwesomeIngot(ingotAwesomeID).setCreativeTab(modJamCreativeTab);
 		woodChair = new ItemChair(woodChairID, 0, this.woodChairIDs[0]).setCreativeTab(modJamCreativeTab).setUnlocalizedName("woodChair");
 		stoneChair = new ItemChair(stoneChairID, 1, this.stoneChairIDs[0]).setCreativeTab(modJamCreativeTab).setUnlocalizedName("stoneChair");
 		awesomeHelmet = new ItemAwesomeArmor(awesomeArmorHelmetID, awesomeArmorMaterial, CommonProxyModJam.awesomeArmorID, 0, "awesomeMod:fuj1n.AwesomeMod.awesomeArmor").setUnlocalizedName("fuj1n.AwesomeMod.awesomeArmor");
 		awesomeChestplate = new ItemAwesomeArmor(awesomeArmorChestplateID, awesomeArmorMaterial, CommonProxyModJam.awesomeArmorID, 1, "awesomeMod:fuj1n.AwesomeMod.awesomeArmor").setUnlocalizedName("fuj1n.AwesomeMod.awesomeArmor");
 		awesomeLeggings = new ItemAwesomeArmor(awesomeArmorLeggingsID, awesomeArmorMaterial, CommonProxyModJam.awesomeArmorID, 2, "awesomeMod:fuj1n.AwesomeMod.awesomeArmor").setUnlocalizedName("fuj1n.AwesomeMod.awesomeArmor");
 		awesomeBoots = new ItemAwesomeArmor(awesomeArmorBootsID, awesomeArmorMaterial, CommonProxyModJam.awesomeArmorID, 3, "awesomeMod:fuj1n.AwesomeMod.awesomeArmor").setUnlocalizedName("fuj1n.AwesomeMod.awesomeArmor");
 		awesomePickaxe = new ItemAwesomePickaxe(awesomeToolPickaxeID, awesomeToolMaterial, "awesomeMod:fuj1n.AwesomeMod.awesomePickaxe").setUnlocalizedName("fuj1n.AwesomeMod.awesomePickaxe");
 		awesomeShovel = new ItemAwesomeShovel(awesomeToolShovelID, awesomeToolMaterial, "awesomeMod:fuj1n.AwesomeMod.awesomeShovel").setUnlocalizedName("fuj1n.AwesomeMod.awesomeShovel");
 		awesomeSword = new ItemAwesomeSword(awesomeToolSwordID, awesomeToolMaterial, "awesomeMod:fuj1n.AwesomeMod.awesomeSword").setUnlocalizedName("fuj1n.AwesomeMod.awesomeSword");
 		awesomeAxe = new ItemAwesomeAxe(awesomeToolAxeID, awesomeToolMaterial, "awesomeMod:fuj1n.AwesomeMod.awesomeAxe").setUnlocalizedName("fuj1n.AwesomeMod.awesomeAxe");
 		awesomeHoe = new ItemAwesomeHoe(awesomeToolHoeID, awesomeToolMaterial, "awesomeMod:fuj1n.AwesomeMod.awesomeHoe").setUnlocalizedName("fuj1n.AwesomeMod.awesomeHoe");
 		darkExtract = new ItemDarkExtract(darkExtractID).setCreativeTab(modJamCreativeTab).setUnlocalizedName("fuj1n.AwesomeMod.darkExtract");
 		rotationTool = new ItemRotationTool(rotationToolID).setCreativeTab(modJamCreativeTab).setUnlocalizedName("fuj1n.AwesomeMod.rotationTool");
 	}
 	
 	public void registerAllBlocks(){
 		GameRegistry.registerBlock(awesomeOre, ItemAwesomeOre.class, "fuj1n.modJam.awesomeOre");
 		GameRegistry.registerBlock(woodChairNorth, "fuj1n.modJam.woodChair.north");
 		GameRegistry.registerBlock(woodChairEast, "fuj1n.modJam.woodChair.east");
 		GameRegistry.registerBlock(woodChairSouth, "fuj1n.modJam.woodChair.south");
 		GameRegistry.registerBlock(woodChairWest, "fuj1n.modJam.woodChair.west");
 		GameRegistry.registerBlock(stoneChairNorth, "fuj1n.modJam.stoneChair.north");
 		GameRegistry.registerBlock(stoneChairEast, "fuj1n.modJam.stoneChair.east");
 		GameRegistry.registerBlock(stoneChairSouth, "fuj1n.modJam.stoneChair.south");
 		GameRegistry.registerBlock(stoneChairWest, "fuj1n.modJam.stoneChair.west");
 		GameRegistry.registerBlock(woodTable, ItemTable.class, "fuj1n.modJam.table.wood");
 		GameRegistry.registerBlock(stoneTable, ItemTable.class, "fuj1n.modJam.table.stone");
 		GameRegistry.registerBlock(awesomeBlock, "fuj1n.modJam.awesomeBlock");
 		GameRegistry.registerBlock(awesomeBlockStandard, ItemAwesomeBlock.class, "fuj1n.modJam.awesomeBlock.standard");
 		GameRegistry.registerBlock(awesomeBlockCreeper, ItemAwesomeBlock.class, "fuj1n.modJam.awesomeBlock.creeper");
 		GameRegistry.registerBlock(lightGen, "fuj1n.modJam.lightGenerator");
 	}
 	
 	public void addAllNames(){
 		for (int i = 0; i < 16; i++) {
 			LanguageRegistry.addName(new ItemStack(awesomeOre, 1, i), awesomeColors[new ItemStack(awesomeOre, 1, i).getItemDamage()] + " Awesome Ore");
 			LanguageRegistry.addName(new ItemStack(awesomeIngot, 1, i), awesomeColors[new ItemStack(awesomeIngot, 1, i).getItemDamage()] + " Awesome Gem");
 			LanguageRegistry.addName(new ItemStack(woodChair, 1, i), awesomeColors[new ItemStack(woodChair, 1, i).getItemDamage()] + " Glowing Wooden Chair");
 			LanguageRegistry.addName(new ItemStack(stoneChair, 1, i), awesomeColors[new ItemStack(stoneChair, 1, i).getItemDamage()] + " Glowing Stone Chair");
 			LanguageRegistry.addName(new ItemStack(woodTable, 1, i), awesomeColors[new ItemStack(woodTable, 1, i).getItemDamage()] + " Glowing Wooden Table");
 			LanguageRegistry.addName(new ItemStack(stoneTable, 1, i), awesomeColors[new ItemStack(stoneTable, 1, i).getItemDamage()] + " Glowing Stone Table");
 			LanguageRegistry.addName(new ItemStack(awesomeBlockStandard, 1, i), awesomeColors[new ItemStack(awesomeBlockStandard, 1, i).getItemDamage()] + " Awesome Block");
 			LanguageRegistry.addName(new ItemStack(awesomeBlockCreeper, 1, i), awesomeColors[new ItemStack(awesomeBlockCreeper, 1, i).getItemDamage()] + " Awesome Block");
 			//LanguageRegistry.instance().addStringLocalization(awesomeHelmet.getUnlocalizedName(ItemAwesomeArmor.getItemStackForNaming(awesomeHelmet.itemID, i)), awesomeColors[i] + " Awesome Helmet");
 		}
 		LanguageRegistry.addName(new ItemStack(awesomeHelmet), "Awesome Helmet");
 		LanguageRegistry.addName(new ItemStack(awesomeChestplate), "Awesome Chestplate");
 		LanguageRegistry.addName(new ItemStack(awesomeLeggings), "Awesome Leggings");
 		LanguageRegistry.addName(new ItemStack(awesomeBoots), "Awesome Boots");
 		LanguageRegistry.addName(awesomePickaxe, "Awesome Pickaxe");
 		LanguageRegistry.addName(awesomeShovel, "Awesome Shovel");
 		LanguageRegistry.addName(awesomeSword, "Awesome Sword");
 		LanguageRegistry.addName(awesomeAxe, "Awesome Axe");
 		LanguageRegistry.addName(awesomeHoe, "Awesome Hoe");
 		LanguageRegistry.addName(darkExtract, "Dark Extract");
 		LanguageRegistry.addName(awesomeBlock, "Awesome Block");
 		LanguageRegistry.addName(rotationTool, "Rotation Tool");
 		LanguageRegistry.addName(lightGen, "Light Generator");
 	}
 	
 	public void registerCreativeTab(){
 		modJamCreativeTab = new CreativeTabModJam("fuj1n.modJam");
 		LanguageRegistry.instance().addStringLocalization("itemGroup." + modJamCreativeTab.getTabLabel(), CommonProxyModJam.modName);
 	}
 	
 	public void addAllCrafting(){
		for(int i = 0; i < 15; i++){
 			CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(woodChair, 1, i), new Object[]{
 				"PXX", "PPP", "PXP", Character.valueOf('P'), Block.planks, Character.valueOf('X'), "ingotAwesome" + awesomeColors[i]
 			}));
 			CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(woodChair, 1, i), new Object[]{
 				"XXP", "PPP", "PXP", Character.valueOf('P'), Block.planks, Character.valueOf('X'), "ingotAwesome" + awesomeColors[i]
 			}));
 			CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(stoneChair, 1, i), new Object[]{
 				"SXX", "SSS", "SXS", Character.valueOf('S'), Block.stone, Character.valueOf('X'), "ingotAwesome" + awesomeColors[i]
 			}));
 			CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(stoneChair, 1, i), new Object[]{
 				"XXS", "SSS", "SXS", Character.valueOf('S'), Block.stone, Character.valueOf('X'), "ingotAwesome" + awesomeColors[i]
 			}));
 			CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(woodTable, 1, i), new Object[]{
 				"XXX", "PPP", "PXP", Character.valueOf('P'), Block.planks, Character.valueOf('X'), "ingotAwesome" + awesomeColors[i]
 			}));
 			CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(stoneTable, 1, i), new Object[]{
 				"XXX", "SSS", "SXS", Character.valueOf('S'), Block.stone, Character.valueOf('X'), "ingotAwesome" + awesomeColors[i]
 			}));
 			CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(awesomeIngot, 8, 15), new Object[]{
 				"XXX", "XDX", "XXX", Character.valueOf('D'), "extractDark", Character.valueOf('X'), "ingotAwesome" + awesomeColors[i]
 			}));
 			CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(awesomeBlockStandard, 1, i), new Object[]{
 				"XXX", "XBX", "XXX", Character.valueOf('B'), awesomeBlock, Character.valueOf('X'), "ingotAwesome" + awesomeColors[i]
 			}));
 			CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(awesomeBlockCreeper, 1, i), new Object[]{
 				"XGX", "XBX", "XGX", Character.valueOf('B'), awesomeBlock, Character.valueOf('G'), Item.gunpowder, Character.valueOf('X'), "ingotAwesome" + awesomeColors[i]
 			}));
 		}
 		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(awesomeHelmet, 1, 0), new Object[]{
 			" X ", "XHX", " X ", Character.valueOf('H'), Item.helmetSteel, Character.valueOf('X'), "ingotAwesomeBlack"
 		}));
 		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(awesomeChestplate, 1, 0), new Object[]{
 			" X ", "XCX", " X ", Character.valueOf('C'), Item.plateSteel, Character.valueOf('X'), "ingotAwesomeBlack"
 		}));
 		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(awesomeLeggings, 1, 0), new Object[]{
 			" X ", "XLX", " X ", Character.valueOf('L'), Item.legsSteel, Character.valueOf('X'), "ingotAwesomeBlack"
 		}));
 		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(awesomeBoots, 1, 0), new Object[]{
 			" X ", "XBX", " X ", Character.valueOf('B'), Item.bootsSteel, Character.valueOf('X'), "ingotAwesomeBlack"
 		}));
 		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(awesomePickaxe, 1, 0), new Object[]{
 			" X ", "XPX", " X ", Character.valueOf('P'), Item.pickaxeSteel, Character.valueOf('X'), "ingotAwesomeBlack"
 		}));
 		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(awesomeShovel, 1, 0), new Object[]{
 			" X ", "XSX", " X ", Character.valueOf('S'), Item.shovelSteel, Character.valueOf('X'), "ingotAwesomeBlack"
 		}));
 		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(awesomeSword, 1, 0), new Object[]{
 			" X ", "XSX", " X ", Character.valueOf('S'), Item.swordSteel, Character.valueOf('X'), "ingotAwesomeBlack"
 		}));
 		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(awesomeAxe, 1, 0), new Object[]{
 			" X ", "XAX", " X ", Character.valueOf('A'), Item.axeSteel, Character.valueOf('X'), "ingotAwesomeBlack"
 		}));
 		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(awesomeHoe, 1, 0), new Object[]{
 			" X ", "XHX", " X ", Character.valueOf('H'), Item.hoeSteel, Character.valueOf('X'), "ingotAwesomeBlack"
 		}));
 		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(awesomeBlock, 1, 0), new Object[]{
 			" X ", "XSX", " X ", Character.valueOf('S'), Block.stone, Character.valueOf('X'), "ingotAwesomeBlack"
 		}));
 		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(rotationTool, 1, 0), new Object[]{
 			"XC ", "CS ", "  S", Character.valueOf('C'), Block.cobblestone, Character.valueOf('S'), Item.stick, Character.valueOf('X'), "ingotAwesomeLime"
 		}));
 	}
 	
 	public static void addHeadRecipe(){
 		ItemStack head = new ItemStack(Item.skull, 1, 3);
 		NBTTagCompound var1 = new NBTTagCompound();
 		var1.setString("SkullOwner", "fuj1n");
 		head.setTagCompound(var1);
 		GameRegistry.addShapedRecipe(head, new Object[]{
 			"OIO", "IEI", "OIO", Character.valueOf('O'), new ItemStack(awesomeOre, 1, 5), Character.valueOf('I'), new ItemStack(awesomeIngot, 1, 5), Character.valueOf('E'), darkExtract
 		});
 	}
 	
 	public void addAllSmelting(){
		for (int i = 0; i < 15; i++){
 			FurnaceRecipes.smelting().addSmelting(oreAwesomeID, i, new ItemStack(awesomeIngot, 8, i), 0.1F);
 		}
 		FurnaceRecipes.smelting().addSmelting(Item.coal.itemID, new ItemStack(darkExtract, 3, 0), 0.1F);
 	}
 	
 	public void registerAllOreDictionary(){
 		for (int i = 0; i < 16; i++) {
 			OreDictionary.registerOre("oreAwesome" + awesomeColors[i], new ItemStack(awesomeOre, 1, i));
 			OreDictionary.registerOre("ingotAwesome" + awesomeColors[i], new ItemStack(awesomeIngot, 1, i));
 		}
 		OreDictionary.registerOre("extractDark", darkExtract);
 		
 	}
 	
 	public void populateGenChest(){
 		ComponentChestContents.addItemGen(awesomeBlock, 0, 2, 5, 20);
 		ComponentChestContents.addItemGen(Item.gunpowder, 0, 3, 5, 5);
 		ComponentChestContents.addItemGen(Block.stone, 0, 1, 2, 2);
 		ComponentChestContents.addItemGen(Item.diamond, 0, 1, 5, 1);
 		ComponentChestContents.addItemGen(Item.emerald, 0, 2, 4, 3);
 		ComponentChestContents.addItemGen(darkExtract, 0, 3, 10, 10);
 		ComponentChestContents.addItemGen(Block.blockSteel, 0, 1, 2, 1);
 		ComponentChestContents.addItemGen(Item.ingotIron, 0, 1, 4, 5);
 		ComponentChestContents.addItemGen(Item.coal, 0, 5, 10, 10);
 		ComponentChestContents.addItemGen(awesomePickaxe, new int[]{0, 10, 20, 200, 280}, 1, 1, 3);
 		ComponentChestContents.addItemGen(awesomeShovel, new int[]{0, 10, 20, 200, 280}, 1, 1, 3);
 		ComponentChestContents.addItemGen(awesomeSword, new int[]{0, 10, 20, 200, 280}, 1, 1, 3);
 		ComponentChestContents.addItemGen(awesomeAxe, new int[]{0, 10, 20, 200, 280}, 1, 1, 3);
 		ComponentChestContents.addItemGen(awesomeHoe, new int[]{0, 10, 20, 200, 280}, 1, 1, 3);
 		ComponentChestContents.addItemGen(awesomeHelmet, new int[]{0, 10, 20}, 1, 1, 3);
 		ComponentChestContents.addItemGen(awesomeChestplate, new int[]{0, 10, 20}, 1, 1, 3);
 		ComponentChestContents.addItemGen(awesomeLeggings, new int[]{0, 10, 20}, 1, 1, 3);
 		ComponentChestContents.addItemGen(awesomeBoots, new int[]{0, 10, 20}, 1, 1, 3);
 		for(int i = 0; i < 15; i++){
 			ComponentChestContents.addItemGen(awesomeBlockStandard, i, 1, 3, 3);
 			ComponentChestContents.addItemGen(awesomeBlockCreeper, i, 1, 3, 2);
 			ComponentChestContents.addItemGen(awesomeOre, i, 1, 4, 10);
 			ComponentChestContents.addItemGen(awesomeIngot, i, 2, 10, 5);
 		}
 	}
 	
 	public void registerAllWorldGenerators(){
 		GameRegistry.registerWorldGenerator(new WorldGeneratorModJam());
 	}
 	
     public static <var> void log(var s, Level level){
         FMLLog.log(level, "[Awesome Mod] %s", s);
     }
 }
