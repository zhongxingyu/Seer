 package steamcraft;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Random;
 
 import cpw.mods.fml.common.*;
 import cpw.mods.fml.common.eventhandler.SubscribeEvent;
 import cpw.mods.fml.common.gameevent.PlayerEvent;
 import cpw.mods.fml.common.registry.GameData;
 import net.minecraft.block.Block;
 import net.minecraft.block.BlockOre;
 import net.minecraft.block.material.MapColor;
 import net.minecraft.block.material.Material;
 import net.minecraft.block.material.MaterialLogic;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.entity.EntityLivingBase;
 import net.minecraft.entity.EnumCreatureType;
 import net.minecraft.init.Blocks;
 import net.minecraft.init.Items;
 import net.minecraft.item.*;
 import net.minecraft.stats.Achievement;
 import net.minecraft.stats.AchievementList;
 import net.minecraft.world.World;
 import net.minecraft.world.biome.BiomeGenBase;
 import net.minecraft.world.chunk.IChunkProvider;
 import net.minecraft.world.gen.feature.WorldGenMinable;
 import net.minecraft.world.gen.feature.WorldGenerator;
 import net.minecraftforge.common.AchievementPage;
 import net.minecraftforge.common.MinecraftForge;
 import net.minecraftforge.common.config.Configuration;
 import net.minecraftforge.common.util.EnumHelper;
 import net.minecraftforge.event.entity.living.LivingHurtEvent;
 import net.minecraftforge.oredict.OreDictionary;
 import net.minecraftforge.oredict.ShapedOreRecipe;
 import net.minecraftforge.oredict.ShapelessOreRecipe;
 import steamcraft.blocks.*;
 import steamcraft.items.*;
 import cpw.mods.fml.common.Mod.EventHandler;
 import cpw.mods.fml.common.Mod.Instance;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.network.NetworkRegistry;
 import cpw.mods.fml.common.registry.EntityRegistry;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 @Mod(modid = "steamcraft", name = "SteamCraft", useMetadata = true)
 public class Steamcraft implements IWorldGenerator, IFuelHandler {
 	@Instance(value = "steamcraft")
 	public static Steamcraft instance;
 	@SidedProxy(clientSide = "steamcraft.ClientProxy", serverSide = "steamcraft.CommonProxy")
 	public static CommonProxy proxy;
 	public static Map<String, Achievement> achs = new HashMap<String, Achievement>();
 	public static final int[] REDUCTION_AMOUNTS = new int[] { 3, 8, 6, 3 };
 	public static final ItemArmor.ArmorMaterial ARMORBRASS = EnumHelper.addArmorMaterial("BRASS", 5, REDUCTION_AMOUNTS, 0);
 	public static final ItemArmor.ArmorMaterial ARMORETHERIUM = EnumHelper.addArmorMaterial("ETHERIUM", -1, REDUCTION_AMOUNTS, 5);
 	public static final ItemArmor.ArmorMaterial ARMOROBSIDIAN = EnumHelper.addArmorMaterial("OBSIDIAN", 20, REDUCTION_AMOUNTS, 10);
 	public static final String[] ARMOR_NAMES = { "etherium", "brass", "obsidian" };
 	//harvestLevel, maxUses,efficiencyOnProperMaterial,damageVsEntity,enchantability;
 	public static final Item.ToolMaterial TOOLETHERIUM = EnumHelper.addToolMaterial("ETHERIUM", 6, -1, 8F, 3, 8);
 	public static final Item.ToolMaterial TOOLOBSIDIAN = EnumHelper.addToolMaterial("OBSIDIAN", 5, 210, 7F, 4, 5);
 	public static final Item.ToolMaterial TOOLSTEAM = EnumHelper.addToolMaterial("STEAM", 2, 321, 12F, 5, 0);
 	private static final String[] FIREARM_PARTS = { "musketcartridge", "percussioncap", "percussionlock", "smoothbarrel", "rifledbarrel", "woodenstock" };
 	private static final String[] MATERIALS = { "etherium", "sulphur", "copper", "obsidianslate", "ingotbrass", "ingotcastiron", "lightbulb", "phosphorus", "uraniumstone", "uraniumpellet",
 			"reactorcore", "coredrillbase", "ingotzinc" };
 	public static final Material solidcircuit = new MaterialLogic(MapColor.airColor);
 	public static final Material staticcircuit = new StaticMaterial(MapColor.airColor);
 	public static final CreativeTabs steamTab = new CreativeTabs("Steamcraft") {
 		@Override
 		@SideOnly(Side.CLIENT)
 		public Item getTabIconItem() {
 			return HandlerRegistry.getItem("steamcraft:coreDrill").get();
 		}
 	};
 	public static Map<ItemStack, String> data = new HashMap<ItemStack, String>();
 	public static ItemStack flintlockMusket, matchlockMusket, percussionCapMusket;
 	public static ItemStack flintlockRifle, matchlockRifle, percussionCapRifle;
 	public static Item spanner, firearm, part, material;
 	private static final WorldGenerator netherGen = new WorldGenNetherTrees();
 	private static final WorldGenerator hideoutGen = new WorldGenHighwaymanHideout();
 	private static WorldGenerator brimstoneGen, zincGen, bornGen, phosphGen, uranGen, volucGen;
 	public static int genNetherTree = 20, genHideout = 8, genBrin = 12, genZinc = 6, genBorn = 20, genPhos = 3, genUr = 2, genVol = 1;
 
 	@Override
 	public void generate(Random random, int chunkX, int chunkZ, World world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider) {
 		if (world.provider.isHellWorld)
 			generateNether(world, random, chunkX * 16, chunkZ * 16);
 		else
 			generateSurface(world, random, chunkX * 16, chunkZ * 16);
 	}
 
 	public void generateNether(World world, Random rand, int k, int l) {
 		if (genNetherTree > 0) {
 			for (int i = 0; i < genNetherTree; i++) {
 				int x = k + rand.nextInt(16);
 				int y = rand.nextInt(128);
 				int z = l + rand.nextInt(16);
 				netherGen.generate(world, rand, x, y, z);
 			}
 		}
 	}
 
 	public void generateSurface(World world, Random rand, int k, int l) {
 		int x, y, z;
 		if (genBrin > 0) {
 			for (int i = 0; i < genBrin; i++) {
 				x = k + rand.nextInt(16);
 				y = rand.nextInt(64);
 				z = l + rand.nextInt(16);
 				brimstoneGen.generate(world, rand, x, y, z);
 			}
 		}
 		if (genZinc > 0) {
 			for (int i = 0; i < genZinc; i++) {
 				x = k + rand.nextInt(16);
 				y = rand.nextInt(64);
 				z = l + rand.nextInt(16);
 				zincGen.generate(world, rand, x, y, z);
 			}
 		}
 		if (genBorn > 0) {
 			for (int i = 0; i < genBorn; i++) {
 				x = k + rand.nextInt(16);
 				y = rand.nextInt(48);
 				z = l + rand.nextInt(16);
 				bornGen.generate(world, rand, x, y, z);
 			}
 		}
 		if (genPhos > 0) {
 			for (int i = 0; i < genPhos; i++) {
 				x = k + rand.nextInt(16);
 				y = rand.nextInt(36);
 				z = l + rand.nextInt(16);
 				phosphGen.generate(world, rand, x, y, z);
 			}
 		}
 		if (genUr > 0) {
 			for (int i = 0; i < genUr; i++) {
 				x = k + rand.nextInt(16);
 				y = rand.nextInt(24);
 				z = l + rand.nextInt(16);
 				uranGen.generate(world, rand, x, y, z);
 			}
 		}
 		if (genVol > 0) {
 			for (int i = 0; i < genVol; i++) {
 				x = k + rand.nextInt(16);
 				y = rand.nextInt(16) + 12;
 				z = l + rand.nextInt(16);
 				volucGen.generate(world, rand, x, y, z);
 			}
 		}
 		if (genHideout > 0) {
 			for (int i = 0; i < genHideout; i++) {
 				x = k + rand.nextInt(16) + 8;
 				y = rand.nextInt(128);
 				z = l + rand.nextInt(16) + 8;
 				hideoutGen.generate(world, rand, x, y, z);
 			}
 		}
 	}
 
 	@Override
 	public int getBurnTime(ItemStack fuel) {
 		if (TileEntityChemFurnace.fuels.containsKey(fuel.getItem())) {
 			return TileEntityChemFurnace.fuels.get(fuel.getItem());
 		} else if (fuel.getItem() == material) {
 			switch (fuel.getItemDamage()) {
 			case 1:
 				return 1000;
 			case 2:
 				return 200;
 			case 7:
 				return 1600;
 			case 9:
 				return 3200;
 			}
 		}
 		return 0;
 	}
 
 	@EventHandler
 	public void load(FMLPreInitializationEvent event) {
         if(event.getSourceFile().getName().endsWith(".jar") && event.getSide().isClient()){
             try {
                 Class.forName("mods.mud.ModUpdateDetector").getDeclaredMethod("registerMod", ModContainer.class, String.class, String.class).invoke(null,
                         FMLCommonHandler.instance().findContainerFor(this),
                        "https://raw.github.com/GotoLink/Steamcraft/master/update.xml",
                        "https://raw.github.com/GotoLink/Steamcraft/master/changelog.md"
                 );
             } catch (Throwable e) {
             }
         }
 
 		Configuration config = new Configuration(event.getSuggestedConfigurationFile());
 		config.load();
 		config.addCustomCategoryComment("Generation", "TPC:Tries per chunk");
 		genNetherTree = config.get("Generation", "Nether_Trees_TPC", genNetherTree).getInt();
 		genHideout = config.get("Generation", "Highwayman_Hideout_TPC", genHideout).getInt();
 		genBrin = config.get("Generation", "Brimstone_TPC", genBrin).getInt();
 		genZinc = config.get("Generation", "ZincOre_TPC", genZinc).getInt();
 		genBorn = config.get("Generation", "Bornite_TPC", genBorn).getInt();
 		genPhos = config.get("Generation", "PhosphateOre_TPC", genPhos).getInt();
 		genUr = config.get("Generation", "Uranite_TPC", genUr).getInt();
 		genVol = config.get("Generation", "Volucite_TPC", genVol).getInt();
 		material = new ItemHandler(new MultiItem(MATERIALS), "steamcraft:mat", "steamcraft:").get();
 		new ItemHandler(new Item().setFull3D().setMaxDamage(64).setMaxStackSize(1), "steamcraft:chisel", "steamcraft:tools/chisel", "chisel").addRecipe(true, "#", "#",
 				"X",'#', Items.iron_ingot,'X', "ingotBrass").register();
 		new BlockHandler(new BlockCopperWire().setStepSound(Block.soundTypeStone), "copperwire", "redstone_dust", "wireCopper").setValues(0.0F)
 				.setOutput(4, 0).addRecipe(false, "dustCopper").register();
 		new BlockHandler(new BlockInverter(false).setStepSound(Block.soundTypeWood), "steamcraft:inverteridle", "steamcraft:inverteridle").setValues(
 				0.0F).register();
 		new BlockHandler(new BlockInverter(true).setStepSound(Block.soundTypeWood), "steamcraft:inverteractive", "steamcraft:inverteractive",
 				"torchCopper").setValues(0.0F, 0.5F).setOutput(4, 0)
 				.addRecipe(true, "X", "#", "I",'#', "stickWood",'X', "wireCopper",'I', "battery").register();
 		new BlockHandler(new BlockDiode(false).setStepSound(Block.soundTypeWood), "steamcraft:diodeidle", "steamcraft:diodeidle").setValues(0.0F)
 				.addRecipe(true, "#X#", "IRI",'#', "torchCopper",'X', "wireCopper",'I', "stone",'R', Items.quartz)
 				.register();
 		new BlockHandler(new BlockDiode(true).setStepSound(Block.soundTypeWood), "steamcraft:diodeactive", "steamcraft:diodeactive").setValues(0.0F,
 				0.625F).register();
 		new BlockHandler(new BlockPoweredRail(true).setStepSound(Block.soundTypeMetal), "steamcraft:rail", "steamcraft:rail").setValues(0.7F).register();
 		new BlockHandler(new BlockNetherSapling(), "steamcraft.nethersapling", "steamcraft:nethersapling", "saplingNether").register();
 		new BlockHandler(new BlockElectricLamp(TileEntityLamp.class, false), "steamcraft:electricLampOff", "steamcraft:electriclamp").setValues(0.0F)
 				.register();
 		new BlockHandler(new BlockElectricLamp(TileEntityLamp.class, true), "steamcraft:electricLampOn", "steamcraft:electriclamp").setValues(0.0F,
 				1.0F).register();
 		new BlockHandler(new BlockTeslaCoil(false), "steamcraft:teslaCoil", "steamcraft:teslaidle")
 				.setValues(0.0F)
 				.addRecipe(true, " X ", "I#I", "ITI",'#', Items.gold_ingot,'X', "itemLightBulb",'I', "wireCopper",'T',
 						Items.quartz).register();
 		new BlockHandler(new BlockTeslaCoil(true), "steamcraft:teslaCoilOn", "steamcraft:teslaactive").setValues(0.0F, 0.625F)
 				.addAchievement("itsalive", 1, 2, AchievementList.acquireIron).register();
 		new BlockHandler(new BlockTeslaReceiver().setStepSound(Block.soundTypeMetal), "steamcraft:receiver", "steamcraft:receiver", "teslaReceiver")
 				.setValues(0.5F)
 				.addRecipe(true, "#X#", "ITI",'#', "ingotCastIron",'X', Items.gold_ingot,'I', "wireCopper",'T',
 						Items.quartz).register();
 		new BlockHandler(new BlockOre().setResistance(5F).setStepSound(Block.soundTypeStone), "steamcraft:orezinc", "steamcraft:zincore", "oreZinc")
 				.setValues(2.5F).setHarvest("pickaxe", 1).setHarvest("drill", 1).addSmelt(new ItemStack(material, 1, 12), 1.0F).register();
 		new BlockHandler(new BlockTeslaReceiver().setStepSound(Block.soundTypeMetal), "steamcraft:receiverOn", "steamcraft:receiveractive").setValues(
 				0.5F, 0.625F).register();
 		new BlockHandler(new BlockSteamFurnace(false).setStepSound(Block.soundTypeMetal), "steamcraft:steamFurnace", "steamcraft:steamfurnaceidle",
 				"furnaceSteam").setValues(4F)
 				.addRecipe(true, "# #", "#X#", "#I#",'#', "ingotBrass",'X', Items.bucket,'I', Blocks.furnace).register();
 		new BlockHandler(new BlockSteamFurnace(true).setStepSound(Block.soundTypeMetal), "steamcraft:steamFurnaceOn",
 				"steamcraft:steamfurnaceactive").setValues(4F, 0.875F).register();
 		new BlockHandler(new BlockChemFurnace(false).setStepSound(Block.soundTypeMetal), "steamcraft:chemFurnace", "steamcraft:chemfurnaceidle",
 				"furnaceChemical").setValues(4.5F)
 				.addRecipe(true, "###", "#X#", "#I#",'#', "ingotCastIron",'X', Items.diamond,'I', "furnaceSteam").register();
 		new BlockHandler(new BlockChemFurnace(true).setStepSound(Block.soundTypeMetal), "steamcraft:chemFurnaceOn",
 				"steamcraft:chemfurnaceactive").setValues(4.5F, 0.875F).register();
 		new BlockHandler(new BlockNukeFurnace(false).setStepSound(Block.soundTypeMetal), "steamcraft:nukeFurnace", "steamcraft:nukefurnaceidle",
 				"furnaceNuke").setValues(5F)
 				.addRecipe(true, "#I#", "#X#", "#I#",'#', Items.iron_ingot,'X', "itemReactorCore",'I', "gemEtherium")
 				.addAchievement("fallout", 0, 1, AchievementList.acquireIron).register();
 		new BlockHandler(new BlockNukeFurnace(true).setStepSound(Block.soundTypeMetal), "steamcraft:nukeFurnaceOn",
 				"steamcraft:nukefurnaceactive").setValues(5F, 0.9375F).register();
 		new BlockHandler(new BlockBattery().setStepSound(Block.soundTypeMetal), "steamcraft:battery", "steamcraft:battery", "battery")
 				.setValues(0.5F, 0.625F).addRecipe(true, "###", "IXI",'#', Items.iron_ingot,'X', Items.quartz,'I', "wireCopper")
 				.register();
 		new BlockHandler(new BlockOre().setResistance(5F).setStepSound(Block.soundTypeStone), "steamcraft:brimstone", "steamcraft:brimstone",
 				"oreBrimstone").setValues(3F).setHarvest("pickaxe", 2).addSmelt(new ItemStack(material, 1, 1), 1.0F).register();
 		new BlockHandler(new BlockOre().setResistance(5F).setStepSound(Block.soundTypeStone), "steamcraft:orePhosphate", "steamcraft:phosphate",
 				"orePhosphate").setValues(2.5F, 0.75F).setHarvest("pickaxe", 2).addSmelt(new ItemStack(material, 1, 7), 1.0F).register();
 		new BlockHandler(new BlockUraniteOre().setResistance(6F).setStepSound(Block.soundTypeStone), "steamcraft:oreUranite", "steamcraft:uranite",
 				"oreUranite").setValues(10F, 0.625F).setHarvest("pickaxe", 2).addSmelt(new ItemStack(material, 1, 8), 1.0F).register();
 		new BlockHandler(new BlockOre().setResistance(5F).setStepSound(Block.soundTypeStone), "steamcraft:oreBornite", "steamcraft:bornite",
 				"oreCopper").setValues(3F).setHarvest("pickaxe", 2).addSmelt(new ItemStack(material, 1, 2), 1.0F).register();
 		new BlockHandler(new BlockSCOre().setResistance(6000000F).setStepSound(Block.soundTypeStone), "steamcraft:oreVolucite",
 				"steamcraft:voluciteore", "oreVolucite").setValues(50F).setHarvest("pickaxe", 5).addSmelt(new ItemStack(material, 1, 0), 1.0F).register();
 		new BlockHandler(new BlockTorchPhosphorus().setStepSound(Block.soundTypeWood), "steamcraft:torchPhosphorus", "steamcraft:torchphosphorus")
 				.setValues(0.0F, 1.0F).setOutput(4, 0).addRecipe(true, "X", "#",'#', "stickWood",'X', "ingotPosphate").register();
 		BlockHandler roofTile = new BlockHandler(new BlockTile(), "steamcraft:roofTile", "steamcraft:slatetiles", "stairFlint")
                 .setValues(2F).setHarvest("pickaxe", 0).setHarvest("drill", 0);
 		roofTile.setOutput(4, 0).addRecipe(true, "###", "###", "###",'#', Items.flint).register();
 		BlockHandler decor =(BlockHandler) new BlockHandler(new BlockDecor(), ItemBlockDecor.class, "steamcraft:decor", "steamcraft:decor").setHarvest("pickaxe", 0)
 				.setHarvest("drill", 0).addAchievement("mastercraftsman", 1, 3, AchievementList.acquireIron);
 		int[] dmgs = { 5, 0, 4, 8 };
 		ItemStack in = new ItemStack(material, 9);
 		for (int i = 0; i < 4; i++) {
 			in.setItemDamage(dmgs[i]);
 			decor.setOutput(1, i).addRecipe(true, "###", "###", "###",'#', in);
 			GameRegistry.addRecipe(in, "#",'#', new ItemStack(decor.get(), 1, i));
 		}
 		ItemStack[] in2 = { new ItemStack(Blocks.iron_block), new ItemStack(Blocks.gold_block), new ItemStack(Blocks.diamond_block), new ItemStack(decor.get(), 1, 0), new ItemStack(decor.get(), 1, 1),
 				new ItemStack(decor.get(), 1, 2), new ItemStack(Blocks.lapis_block), new ItemStack(Blocks.stone), new ItemStack(decor.get(), 1, 3) };
 		for (int i = 4; i < 13; i++) {
 			decor.setOutput(1, i);
 			decor.addRecipe(false, in2[i - 4], "chisel");
 			decor.addSmelt(in2[i - 4], i, 1.0F);
 		}
 		decor.register();
 		BlockHandler fencegate = new BlockHandler(new BlockSCFenceGate().setResistance(20F).setStepSound(Block.soundTypeMetal),
 				"steamcraft:gateCastIron", "steamcraft:castironblock", "gateCastIron").setValues(7F);
 		fencegate.addRecipe(true, "#X#", "#X#",'#', "ingotCastIron",'X', "railCastIron").register();
 		new BlockHandler(new BlockSCFence(Material.iron, fencegate.get(), true).setResistance(20F).setStepSound(Block.soundTypeMetal),
 				"steamcraft:railingCastIron", "steamcraft:castironblock", "railCastIron").setValues(7F).setHarvest("pickaxe", 0).setHarvest("drill", 0).setOutput(2, 0)
 				.addRecipe(true, "###", "###",'#', "ingotCastIron").register();
 		new BlockHandler(new BlockLamp(true), "steamcraft:lampOn", "steamcraft:lampblock").setHarvest("pickaxe", 0).setHarvest("drill", 0)
 				.addRecipe(true, "#X#", "XIX", "#X#",'#', "ingotCastIron",'X', Blocks.glass,'I', Items.glowstone_dust).register();
 		new BlockHandler(new BlockLamp(false), "steamcraft:lamp", "steamcraft:lampblock").register();
 		new BlockHandler(new BlockBrassLog().setStepSound(Block.soundTypeMetal), "steamcraft:logBrass", "steamcraft:brasslog").setValues(5F)
 				.setHarvest("pickaxe", 2).setHarvest("drill", 2).setOutput(4, 0).addRecipe(true, "###", "#I#", "###",'#', "ingotBrass",'I', "logWood")
 				.register();
 		new BlockHandler(new BlockNetherLeaves(Material.wood).setLightOpacity(1).setStepSound(Block.soundTypeGlass), "steamcraft:leavesLamp",
 				"steamcraft:brassleaves").setValues(2F, 0.9375F).setHarvest("pickaxe", 0).setHarvest("drill", 0).setOutput(4, 0)
 				.addRecipe(true, "#X#", "XIX", "#X#",'#', "ingotBrass",'X', Blocks.glass,'I', Items.glowstone_dust).register();
 		new BlockHandler(new BlockWirelessLamp(TileEntityLamp.class, false), "steamcraft:wirelessLampOff", "steamcraft:wirelesslamp").setValues(0.0F)
 				.register();
 		new BlockHandler(new BlockWirelessLamp(TileEntityLamp.class, true), "steamcraft:wirelessLampOn", "steamcraft:wirelesslamp").setValues(0.0F,
 				1.0F).register();
 		new BlockHandler(new BlockSCStairs(roofTile.get(), 0, Items.flint, 2), "steamcraft:stairsRoof", "steamcraft:slatetiles").setOutput(4, 0)
 				.addRecipe(true, "#  ", "## ", "###",'#', Items.flint).register();
 		new BlockHandler(new BlockTeaPlant().setStepSound(Block.soundTypeGrass), "steamcraft:teaplant", "steamcraft:teaplant", "plantTea")
 				.setValues(0.0F).register();
 		new ItemHandler(new ItemCoreDrill(), "steamcraft:coreDrill", "steamcraft:coredrill")
 				.addRecipe(true, "X", "#", "I",'#', "ingotPosphate",'X', "drillGold",'I', "itemDrillBase")
 				.addAchievement("heavenpiercing", 3, -1, achs.get("spiralnemesis")).register();
 		new ItemHandler(new ItemSCPickaxe(TOOLOBSIDIAN), "steamcraft:pickaxeObsidian", "steamcraft:tools/obsidianpick").setTool("pickaxe", 5)
 				.addAchievement("blackmagic", 3, 2, AchievementList.buildBetterPickaxe).addPick("slateObsidian").register();
 		new ItemHandler(new ItemSpade(TOOLOBSIDIAN), "steamcraft:shovelObsidian", "steamcraft:tools/obsidianspade").setTool("shovel", 5).addShovel(
 				"slateObsidian");
 		new ItemHandler(new ItemSCAxe(TOOLOBSIDIAN), "steamcraft:hatchetObsidian", "steamcraft:tools/obsidianaxe").setTool("axe", 5).addAxe(
 				"slateObsidian");
 		new ItemHandler(new ItemHoe(TOOLOBSIDIAN), "steamcraft:hoeObsidian", "steamcraft:tools/obsidianhoe").addHoe("slateObsidian");
 		new ItemHandler(new ItemSCSword(TOOLOBSIDIAN), "steamcraft:swordObsidian", "steamcraft:tools/obsidiansword").addSword("slateObsidian");
 		new ItemHandler(new ItemSCDrill(TOOLOBSIDIAN), "steamcraft:drillObsidian", "steamcraft:tools/obsidiandrill", "drillObsidian").setTool("drill",
 				5).addDrill("slateObsidian");
 		new ItemHandler(new ItemSCArmor(ARMOROBSIDIAN, 2, 0), "steamcraft:helmetObsidian", "steamcraft:armour/obsidianhelmet")
 				.addHelmet("slateObsidian");
 		new ItemHandler(new ItemSCArmor(ARMOROBSIDIAN, 2, 1), "steamcraft:chestplateObsidian", "steamcraft:armour/obsidianplate")
 				.addPlate("slateObsidian");
 		new ItemHandler(new ItemSCArmor(ARMOROBSIDIAN, 2, 2), "steamcraft:leggingsObsidian", "steamcraft:armour/obsidianlegs")
 				.addLegs("slateObsidian");
 		new ItemHandler(new ItemSCArmor(ARMOROBSIDIAN, 2, 3), "steamcraft:bootsObsidian", "steamcraft:armour/obsidianboots")
 				.addBoots("slateObsidian");
 		new ItemHandler(new ItemSCArmor(ARMORBRASS, 1, 0), "steamcraft:brassGoggles", "steamcraft:armour/brassgoggles").addRecipe(true,
 				"X#X", "# #",'X', Blocks.glass,'#', "ingotBrass").register();
 		new ItemHandler(new ItemSCArmor(ARMORBRASS, 1, 1), "steamcraft:aqualung", "steamcraft:armour/aqualung")
 				.addRecipe(true, "XTX", "X X", "X#X",'X', "ingotBrass",'#', Blocks.piston,'T', Blocks.glass)
 				.addAchievement("jethrotull", 0, 3, AchievementList.acquireIron).register();
 		new ItemHandler(new ItemSCArmor(ARMORBRASS, 1, 3), "steamcraft:rollerSkates", "steamcraft:armour/rollerskates").addRecipe(true,
 				"X X", "X X", "# #",'X', "ingotBrass",'#', Items.iron_ingot).register();
 		new ItemHandler(new ItemSCArmor(ARMORBRASS, 1, 2), "steamcraft:legBraces", "steamcraft:armour/pneumaticbraces").addRecipe(
 				true, "XXX", "X X", "# #",'X', "ingotBrass",'#', Blocks.piston).register();
 		new ItemHandler(new ItemSCPickaxe(TOOLETHERIUM), "steamcraft:pickaxeEtherium", "steamcraft:tools/etheriumpick").setTool("pickaxe", 6).addPick(
 				"gemEtherium");
 		new ItemHandler(new ItemSpade(TOOLETHERIUM), "steamcraft:shovelEtherium", "steamcraft:tools/etheriumspade").setTool("shovel", 6).addShovel(
 				"gemEtherium");
 		new ItemHandler(new ItemSCAxe(TOOLETHERIUM), "steamcraft:hatchetEtherium", "steamcraft:tools/etheriumaxe").setTool("axe", 6).addAxe(
 				"gemEtherium");
 		new ItemHandler(new ItemHoe(TOOLETHERIUM), "steamcraft:hoeEtherium", "steamcraft:tools/etheriumhoe").addHoe("gemEtherium");
 		new ItemHandler(new ItemSCSword(TOOLETHERIUM), "steamcraft:swordEtherium", "steamcraft:tools/etheriumsword").addSword("gemEtherium");
 		new ItemHandler(new ItemSCDrill(TOOLETHERIUM), "steamcraft:drillEtherium", "steamcraft:tools/etheriumdrill", "drillEtherium").setTool("drill",
 				6).addDrill("gemEtherium");
 		new ItemHandler(new ItemSCArmor(ARMORETHERIUM, 0, 0), "steamcraft:helmetEtherium", "steamcraft:armour/etheriumhelmet")
 				.addHelmet("gemEtherium");
 		new ItemHandler(new ItemSCArmor(ARMORETHERIUM, 0, 1), "steamcraft:chestplateEtherium", "steamcraft:armour/etheriumplate")
 				.addPlate("gemEtherium");
 		new ItemHandler(new ItemSCArmor(ARMORETHERIUM, 0, 2), "steamcraft:leggingsEtherium", "steamcraft:armour/etheriumlegs")
 				.addLegs("gemEtherium");
 		new ItemHandler(new ItemSCArmor(ARMORETHERIUM, 0, 3), "steamcraft:bootsEtherium", "steamcraft:armour/etheriumboots")
 				.addBoots("gemEtherium");
 		new ItemHandler(new ItemSCPickaxe(TOOLSTEAM), "steamcraft:pickaxeSteam", "steamcraft:tools/steampick").setTool("pickaxe", 7).addRecipe(true,
 				"XIX", " # ", " # ",'#', "stickWood",'X', "ingotBrass",'I', "furnaceSteam");
 		new ItemHandler(new ItemSCSpade(TOOLSTEAM), "steamcraft:shovelSteam", "steamcraft:tools/steamspade").setTool("shovel", 7).addRecipe(true, "X",
 				"#", "I",'#', "stickWood",'X', "ingotBrass",'I', "furnaceSteam");
 		new ItemHandler(new ItemSCAxe(TOOLSTEAM), "steamcraft:hatchetSteam", "steamcraft:tools/steamaxe").setTool("axe", 7).addRecipe(true, "X ", "XI",
 				"# ",'#', "stickWood",'X', "ingotBrass",'I', "furnaceSteam");
 		new ItemHandler(new ItemSCHoe(TOOLSTEAM), "steamcraft:hoeSteam", "steamcraft:tools/steamhoe").addRecipe(true, "XI", " #", " #",
 				'#', "stickWood",'X', "ingotBrass",'I', "furnaceSteam");
 		new ItemHandler(new ItemSCSword(TOOLSTEAM), "steamcraft:swordSteam", "steamcraft:tools/steamsword").addRecipe(true, "X", "I", "#",
 				'#', "stickWood",'X', "ingotBrass",'I', "furnaceSteam");
 		new ItemHandler(new ItemSCDrill(TOOLSTEAM), "steamcraft:drillSteam", "steamcraft:tools/steamdrill", "drillSteam").setTool("drill", 7).addRecipe(
 				true, "XXX", "XIX", "XX#",'#', "stickWood",'X', "ingotBrass",'I', "furnaceSteam");
 		new ItemHandler(new ItemSCDrill(Item.ToolMaterial.IRON), "steamcraft:drillIron", "steamcraft:tools/irondrill", "drillIron").setTool("drill", 2)
 				.addDrill(Items.iron_ingot).addAchievement("spiralnemesis", 2, -1, AchievementList.buildWorkBench);
 		new ItemHandler(new ItemSCDrill(Item.ToolMaterial.WOOD), "steamcraft:drillWood", "steamcraft:tools/wooddrill", "drillWood").setTool("drill", 0)
 				.addDrill("plankWood");
 		new ItemHandler(new ItemSCDrill(Item.ToolMaterial.STONE), "steamcraft:drillStone", "steamcraft:tools/stonedrill", "drillStone")
 				.setTool("drill", 1).addDrill("cobblestone");
 		new ItemHandler(new ItemSCDrill(Item.ToolMaterial.EMERALD), "steamcraft:drillDiamond", "steamcraft:tools/diamonddrill", "drillDiamond").setTool(
 				"drill", 3).addDrill(Items.diamond);
 		new ItemHandler(new ItemSCDrill(Item.ToolMaterial.GOLD), "steamcraft:drillGold", "steamcraft:tools/golddrill", "drillGold").setTool("drill", 0)
 				.addDrill(Items.gold_ingot);
 		spanner = new ItemHandler(new Item().setFull3D().setMaxDamage(3).setMaxStackSize(1), "steamcraft:spanner"
 				, "steamcraft:tools/spanner", "spanner").get();
 		part = new ItemHandler(new MultiItem(FIREARM_PARTS), "steamcraft:part", "steamcraft:").get();
 		firearm = new ItemHandler(new ItemFirearm(), "steamcraft:firearm", "steamcraft:tools/").get();
 
         flintlockMusket = new ItemStack(firearm, 1, 0);
 		ItemFirearm.setFirePower(flintlockMusket, 8);
 		ItemFirearm.setMaxDamage(flintlockMusket, 100);
 		matchlockMusket = new ItemStack(firearm, 1, 0);
 		ItemFirearm.setFirePower(matchlockMusket, 6);
 		ItemFirearm.setMaxDamage(matchlockMusket, 200);
 		percussionCapMusket = new ItemStack(firearm, 1, 0);
 		ItemFirearm.setFirePower(percussionCapMusket, 10);
 		ItemFirearm.setMaxDamage(percussionCapMusket, 50);
 		ItemFirearm.setPercussion(percussionCapMusket);
 		flintlockRifle = new ItemStack(firearm, 1, 0);
 		ItemFirearm.setFirePower(flintlockRifle, 10);
 		ItemFirearm.setMaxDamage(flintlockRifle, 120);
 		ItemFirearm.setRifled(flintlockRifle);
 		matchlockRifle = new ItemStack(firearm, 1, 0);
 		ItemFirearm.setFirePower(matchlockRifle, 8);
 		ItemFirearm.setMaxDamage(matchlockRifle, 240);
 		ItemFirearm.setRifled(matchlockRifle);
 		percussionCapRifle = new ItemStack(firearm, 1, 0);
 		ItemFirearm.setFirePower(percussionCapRifle, 12);
 		ItemFirearm.setMaxDamage(percussionCapRifle, 60);
 		ItemFirearm.setRifled(percussionCapRifle);
 		ItemFirearm.setPercussion(percussionCapRifle);
 		EntityHighwayman.heldItems[0] = flintlockMusket;
 		EntityHighwayman.heldItems[1] = flintlockRifle;
 		EntityHighwayman.heldItems[2] = matchlockMusket;
 		EntityHighwayman.heldItems[3] = matchlockRifle;
 		EntityHighwayman.heldItems[4] = percussionCapMusket;
 		EntityHighwayman.heldItems[5] = percussionCapRifle;
 		new ItemHandler(new ItemElectricLamp(HandlerRegistry.getBlock("steamcraft:electricLampOff").get()), "steamcraft:electricLamp",
 				"steamcraft:electriclamp", "lampElectric").addRecipe(true, "I", "#", "X",'#', "dustCopper",'X', "ingotCastIron",'I',
 				"itemLightBulb").register();
 		new ItemHandler(new ItemElectricLamp(HandlerRegistry.getBlock("steamcraft:wirelessLampOff").get()), "steamcraft:wirelessLamp",
 				"steamcraft:wirelesslamp").addRecipe(true, "#", "X",'#', "lampElectric",'X', "teslaReceiver").register();
 		new ItemHandler(new ItemSeeds(HandlerRegistry.getBlock("steamcraft:teaplant").get(), Blocks.farmland), "steamcraft:teaSeeds",
 				"steamcraft:teaseed", "seedTea").addSeed(5).register();
 		new ItemHandler(new Item(), "steamcraft:teaLeaf", "steamcraft:tealeaves", "teaLeaves").register();
 		ItemHandler kettle = new ItemHandler(new ItemKettle(), "steamcraft:kettleHot", "steamcraft:kettle", "kettleHot");
 		kettle.register();
 		new ItemHandler(new ItemKettle(), "steamcraft:kettle", "steamcraft:kettle", "kettleFull").addSmelt(new ItemStack(kettle.get()), 1.0F)
 				.addRecipe(false, "kettle", Items.water_bucket, "teaLeaves").register();
 		new ItemHandler(new ItemTeacup(0), "steamcraft:teacupEmpty", "steamcraft:teacupempty", "teacup").addRecipe(true, "# #", " # ",
 				'#', Items.clay_ball).register();
 		new ItemHandler(new ItemTeacup(4), "steamcraft:teacup", "steamcraft:teacupfull", "teacupFull").addAchievement("timeforacuppa", -1, 2,
 				AchievementList.acquireIron).register();
 		new ItemHandler(new ItemKettle(), "steamcraft:kettleempty", "steamcraft:kettle", "kettle").addRecipe(true, "#  ", "###", " ##",
 				'#', "ingotCastIron").register();
 		if (config.hasChanged())
 			config.save();
 		brimstoneGen = new WorldGenMinable(HandlerRegistry.getBlock("steamcraft:brimstone").get(), 8);
 		zincGen = new WorldGenMinable(HandlerRegistry.getBlock("steamcraft:orezinc").get(), 16);
 		bornGen = new WorldGenMinable(HandlerRegistry.getBlock("steamcraft:oreBornite").get(), 8);
 		phosphGen = new WorldGenMinable(HandlerRegistry.getBlock("steamcraft:orePhosphate").get(), 6);
 		uranGen = new WorldGenMinable(HandlerRegistry.getBlock("steamcraft:oreUranite").get(), 4);
 		volucGen = new WorldGenMinable(HandlerRegistry.getBlock("steamcraft:oreVolucite").get(), 3);
 		data.put(new ItemStack(material, 1, 0), "gemEtherium");
 		data.put(new ItemStack(material, 1, 1), "oreSulphur");
 		data.put(new ItemStack(material, 1, 2), "dustCopper");
 		data.put(new ItemStack(material, 1, 3), "slateObsidian");
 		data.put(new ItemStack(material, 1, 4), "ingotBrass");
 		data.put(new ItemStack(material, 1, 5), "ingotCastIron");
 		data.put(new ItemStack(material, 1, 6), "itemLightBulb");
 		data.put(new ItemStack(material, 1, 7), "ingotPosphate");
 		data.put(new ItemStack(material, 1, 8), "oreUranium");
 		data.put(new ItemStack(material, 1, 9), "ingotUranium");
 		data.put(new ItemStack(material, 1, 10), "itemReactorCore");
 		data.put(new ItemStack(material, 1, 11), "itemDrillBase");
 		data.put(new ItemStack(material, 1, 12), "ingotZinc");
 		data.put(new ItemStack(part, 1, 0), "cartridgeMusket");
 		data.put(new ItemStack(part, 1, 1), "percussionCap");
 		data.put(new ItemStack(part, 1, 2), "percussionLock");
 		data.put(new ItemStack(part, 1, 3), "barrel");
 		data.put(new ItemStack(part, 1, 4), "barrelRifled");
 		data.put(new ItemStack(part, 1, 5), "woodenStock");
 		data.put(flintlockMusket, "musketFlintlock");
 		data.put(matchlockMusket, "musketMatchlock");
 		data.put(percussionCapMusket, "musketPercussion");
 		data.put(flintlockRifle, "rifleFlintlock");
 		data.put(matchlockRifle, "rifleMatchLock");
 		data.put(percussionCapRifle, "riflePercussion");
 	}
 
     @SubscribeEvent
     public void livingHurt(LivingHurtEvent event){
         if(event.source.getEntity() instanceof EntityLivingBase){
             ItemStack weapon = ((EntityLivingBase) event.source.getEntity()).getHeldItem();
             if(weapon!=null && weapon.getItem() instanceof ItemSCTool){
                 if(((ItemSCTool)weapon.getItem()).func_150913_i() == TOOLSTEAM){
                     event.ammount-=(float) weapon.getItemDamage() * 10 / 320;
                 }
             }
         }
     }
 
 	@SubscribeEvent
 	public void notifyPickup(PlayerEvent.ItemPickupEvent event) {
 		if (event.pickedUp.getEntityItem().isItemEqual(new ItemStack(material))) {
 			event.player.triggerAchievement(achs.get("carryingyou"));
 		}
 	}
 
 	@SubscribeEvent
 	public void onCrafting(PlayerEvent.ItemCraftedEvent event) {
 		if (event.crafting.getItem() == HandlerRegistry.getItem("steamcraft:pickaxeObsidian").get()) {
 			event.player.triggerAchievement(achs.get("blackmagic"));
 			return;
 		}
 		if (HandlerRegistry.drills.contains(event.crafting.getItem())) {
             event.player.triggerAchievement(achs.get("spiralnemesis"));
 			return;
 		}
 		if (event.crafting.getItem() == HandlerRegistry.getItem("steamcraft:coreDrill").get()) {
             event.player.triggerAchievement(achs.get("heavenpiercing"));
 			return;
 		}
 		if (event.crafting.getItem() == Item.getItemFromBlock(BlockTeslaCoil.getTeslaIdle())) {
             event.player.triggerAchievement(achs.get("itsalive"));
 			return;
 		}
 		if (event.crafting.getItem() == Item.getItemFromBlock(HandlerRegistry.getBlock("steamcraft:decor").get())) {
             event.player.triggerAchievement(achs.get("mastercraftsman"));
 			return;
 		}
 		if (event.crafting.getItem() == ItemSCArmor.getAqualung()) {
             event.player.triggerAchievement(achs.get("jethrotull"));
 			return;
 		}
 		if (event.crafting.getItem() == firearm) {
             event.player.triggerAchievement(achs.get("lockstockbarrel"));
 			return;
 		}
 		int repairDmg = -1;
 		ItemStack spaner = null;
 		for (int i = 0; i < event.craftMatrix.getSizeInventory(); i++) {
 			ItemStack itemstack1 = event.craftMatrix.getStackInSlot(i);
 			if (itemstack1 != null) {
 				if (itemstack1.getItem() != spanner && itemstack1.isItemStackDamageable()) {
 					repairDmg = itemstack1.getItemDamage();
 				}
 				if (itemstack1.getItem() == HandlerRegistry.getItem("steamcraft:chisel").get() && itemstack1.getItemDamage() + 1 < itemstack1.getMaxDamage()) {
 					if (event.crafting.isItemEqual(new ItemStack(part, 1, 4))) {
                         event.player.inventory.addItemStackToInventory(new ItemStack(itemstack1.getItem(), 1, itemstack1.getItemDamage() + 16));
 					} else {
                         event.player.inventory.addItemStackToInventory(new ItemStack(itemstack1.getItem(), 1, itemstack1.getItemDamage() + 1));
 					}
 				}
 				if (itemstack1.getItem() == spanner) {
 					if (spaner != null) {
 						return;
 					}
 					spaner = itemstack1.copy();
 				}
 			}
 		}
 		if (spaner != null && repairDmg >= 0 && event.crafting.getItem().isRepairable()) {
 			ItemStack stack;
 			if (repairDmg == 0) {
 				stack = spaner;
 				if (!event.player.inventory.addItemStackToInventory(stack))
                     event.player.dropPlayerItemWithRandomChoice(stack, false);
 			} else if (spaner.getItemDamage() + 1 < spaner.getMaxDamage()) {
 				stack = new ItemStack(spanner, 1, spaner.getItemDamage() + 1);
 				if (!event.player.inventory.addItemStackToInventory(stack))
                     event.player.dropPlayerItemWithRandomChoice(stack, false);
 			}
 		}
 	}
 
 	@EventHandler
 	public void registering(FMLInitializationEvent event) {
 		proxy.registerRenderers();
 		NetworkRegistry.INSTANCE.registerGuiHandler(this, proxy);
 		GameRegistry.registerFuelHandler(this);
 		GameRegistry.registerWorldGenerator(this, 1);
         FMLCommonHandler.instance().bus().register(proxy);
         FMLCommonHandler.instance().bus().register(this);
         MinecraftForge.EVENT_BUS.register(this);
 		for (ItemStack obj : data.keySet()) {
             OreDictionary.registerOre(data.get(obj), obj);
 		}
 		String[] toolsets = { "pickaxe", "drill" };
         Blocks.obsidian.setHarvestLevel(toolsets[0], 3);
         Blocks.quartz_block.setHarvestLevel(toolsets[0], 2);
         Blocks.stonebrick.setHarvestLevel(toolsets[0], 0);
 		Block[] blocks = { Blocks.stonebrick, Blocks.dirt, Blocks.sand, Blocks.gravel, Blocks.grass, Blocks.cobblestone, Blocks.double_stone_slab, Blocks.stone_slab, Blocks.stone, Blocks.sandstone,
 				Blocks.mossy_cobblestone, Blocks.coal_ore, Blocks.ice, Blocks.netherrack, Blocks.rail, Blocks.detector_rail, Blocks.golden_rail, Blocks.activator_rail };
 		for (Block block : blocks) {
             block.setHarvestLevel(toolsets[1], 0);
 		}
 		blocks = new Block[] { Blocks.emerald_ore, Blocks.diamond_block, Blocks.diamond_ore, Blocks.gold_ore, Blocks.gold_block, Blocks.quartz_block, Blocks.redstone_ore, Blocks.lit_redstone_ore };
 		for (Block block : blocks) {
             block.setHarvestLevel(toolsets[1], 2);
 		}
 		blocks = new Block[] { Blocks.iron_ore, Blocks.iron_block, Blocks.lapis_ore, Blocks.lapis_block };
 		for (Block block : blocks) {
             block.setHarvestLevel(toolsets[1], 1);
 		}
 		EntityRegistry.registerModEntity(EntityMusketBall.class, "MusketBall", 1, this, 120, 1, true);
 		EntityRegistry.registerModEntity(EntityHighwayman.class, "Highwayman", 2, this, 120, 1, true);
         BiomeGenBase[] biomes = new BiomeGenBase[BiomeGenBase.explorationBiomesList.size()];
         BiomeGenBase.explorationBiomesList.toArray(biomes);
 		EntityRegistry.addSpawn(EntityHighwayman.class, 5, 1, 5, EnumCreatureType.monster, biomes);
 		GameRegistry.addSmelting(Items.iron_ingot, new ItemStack(material, 1, 5), 1.0F);
 		GameRegistry.addSmelting(Blocks.gravel, new ItemStack(Items.flint), 1.0F);
 		GameRegistry.addSmelting(Blocks.stonebrick, new ItemStack(Blocks.stone), 1.0F);
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(material, 4, 3), "#", "#",'#', Blocks.obsidian));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(Blocks.obsidian), "##", "##",'#', "slateObsidian"));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(Items.redstone, 8), "###",'#', "dustCopper"));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(material, 4, 6), "I", "#", "X",'#', "wireCopper",'X', Items.iron_ingot,'I',
 				Blocks.glass));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(material, 1, 10), "###", "#X#", "###",'#', Blocks.obsidian,'X', "furnaceChemical"));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(material, 1, 11), "#X#", "# #", " # ",'#', Items.iron_ingot,'X', "gemEtherium"));
 		GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(material, 1, 4), "ingotZinc", "dustCopper"));
 		GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(material, 2, 9), "oreUranium"));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(spanner), "# #", "#X#", " # ",'#', "ingotBrass",'X', "gemEtherium"));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(Blocks.jukebox), "###", "#X#", "###",'#', "plankWood",'X', Items.quartz));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(Blocks.golden_rail, 6), "XRX", "X#X", "XRX",'X', Items.gold_ingot,'R', "wireCopper",
 				'#', "stickWood"));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(Blocks.detector_rail, 6), "XRX", "X#X", "XRX",'X', Items.iron_ingot,'R', "wireCopper",
 				'#', Blocks.stone_pressure_plate));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(Blocks.lever), "X", "#", "I",'#', "cobblestone",'X', "stickWood",'I',
 				"battery"));
 		GameRegistry.addRecipe(new ItemStack(Items.clock), " # ", "#X#", " # ",'#', Items.gold_ingot,'X', Items.quartz);
 		GameRegistry.addRecipe(new ItemStack(Items.compass), " # ", "#X#", " # ",'#', Items.iron_ingot,'X', Items.quartz);
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(Blocks.stone_button), "#", "#", "X",'#', "stone",'X', "battery"));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(Blocks.stone_pressure_plate), "##", "X ",'#', "stone",'X', "battery"));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(Blocks.wooden_pressure_plate), "##", "X ",'#', "plankWood",'X', "battery"));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(Blocks.dispenser), "###", "#X#", "#R#",'#', "cobblestone",'X', Items.bow,
 				'R', Items.quartz));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(Items.gunpowder), "#X#",'#', "oreSulphur",'X', Items.coal));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(Items.glowstone_dust, 4), "X#X", "#I#", "X#X",'#', "ingotPosphate",'X', "oreSulphur",
 				'I', "oreUranium"));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(Blocks.piston), "TTT", "#X#", "#R#",'#', "cobblestone",'X', Items.iron_ingot,
 				'R', "dustCopper",'T', "plankWood"));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(part, 1, 2), "X ", "##",'#', Items.iron_ingot,'X', "ingotBrass"));
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(part, 1, 5), "#  ", " # ", "  #",'#', "plankWood"));
 		GameRegistry.addRecipe(new ItemStack(part, 1, 3), "#  ", " # ", "  #",'#', Items.iron_ingot);
 		GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(part, 1, 4), "barrel", "chisel"));
 		flintlockMusket.setItemDamage(flintlockMusket.getMaxDamage() - 1);
 		GameRegistry.addRecipe(new ShapedOreRecipe(flintlockMusket, "X", "#", "T",'#', "barrel",'X', new ItemStack(Items.flint_and_steel, 1,
 				OreDictionary.WILDCARD_VALUE),'T', "woodenStock"));
 		matchlockMusket.setItemDamage(matchlockMusket.getMaxDamage() - 1);
 		GameRegistry.addRecipe(new ShapedOreRecipe(matchlockMusket, "X", "#", "T",'#', "barrel",'X', Items.string,'T', "woodenStock"));
 		percussionCapMusket.setItemDamage(percussionCapMusket.getMaxDamage() - 1);
 		GameRegistry.addRecipe(new ShapedOreRecipe(percussionCapMusket, "X", "#", "T",'#', "barrel",'X', "percussionLock",'T',
 				"woodenStock"));
 		flintlockRifle.setItemDamage(flintlockRifle.getMaxDamage() - 1);
 		GameRegistry.addRecipe(new ShapedOreRecipe(flintlockRifle, "X", "#", "T",'#', "barrelRifled",'X', new ItemStack(Items.flint_and_steel, 1,
 				OreDictionary.WILDCARD_VALUE),'T', "woodenStock"));
 		matchlockRifle.setItemDamage(matchlockRifle.getMaxDamage() - 1);
 		GameRegistry.addRecipe(new ShapedOreRecipe(matchlockRifle, "X", "#", "T",'#', "barrelRifled",'X', Items.string,'T',
 				"woodenStock"));
 		percussionCapRifle.setItemDamage(percussionCapRifle.getMaxDamage() - 1);
 		GameRegistry.addRecipe(new ShapedOreRecipe(percussionCapRifle, "X", "#", "T",'#', "barrelRifled",'X', "percussionLock",'T',
 				"woodenStock"));
 		GameRegistry.addRecipe(new ItemStack(part, 8), "X", "#", "T",'#', Items.gunpowder,'X', Items.iron_ingot,'T', Items.paper);
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(part, 8, 1), "T", "#", "X",'#', Items.gunpowder,'X', "ingotBrass",'T',
 				Items.paper));
 		GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(Items.slime_ball), Items.water_bucket, "oreSulphur"));
 		addSpannerRecipes();
 	    addAchievements();
 	}
 
 	private void addSpannerRecipes() {
         Item item = null;
 		for (Iterator itr = GameData.itemRegistry.iterator(); itr.hasNext(); item = (Item) itr.next()) {
 			if (item != null && item.isRepairable()) {
 				ItemStack itemstack = new ItemStack(item, 1, OreDictionary.WILDCARD_VALUE);
 				GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(item), itemstack, "spanner"));
 			}
 		}
 	}
 
 	public static void addAchievements() {
 		achs.put("carryingyou", new Achievement("carryingyou", "carryingyou", 4, 2, new ItemStack(material,1,0), achs.get("blackmagic")).registerStat());
 		achs.put("ruinedeverything", new Achievement("ruinedeverything", "ruinedeverything", 0, 0, new ItemStack(material, 1, 8), achs.get("fallout")).registerStat());
 		achs.put("lockstockbarrel", new Achievement("lockstockbarrel", "lockstockbarrel", -1, 3, flintlockMusket, AchievementList.acquireIron).registerStat());
 		AchievementPage.registerAchievementPage(new AchievementPage("Steamcraft", achs.values().toArray(new Achievement[achs.size()])));
 	}
 }
