 package overtjaguar.powerconverters.rpmodule;
 
 import overtjaguar.powerconverters.rpmodule.power.BlockPowerConverterRedPower;
 import overtjaguar.powerconverters.rpmodule.power.ItemBlockPowerConverterRedPower;
 import overtjaguar.powerconverters.rpmodule.power.TileEntityRedPowerConsumer;
 import overtjaguar.powerconverters.rpmodule.power.TileEntityRedPowerProducer;
 import overtjaguar.powerconverters.rpmodule.proxy.CommonProxy;
 import powercrystals.powerconverters.power.PowerSystem;
 import net.minecraft.block.Block;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraftforge.common.Configuration;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.PreInit;
 import cpw.mods.fml.common.Mod.Init;
 import cpw.mods.fml.common.Mod.PostInit;
 import cpw.mods.fml.common.SidedProxy;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPostInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 
@Mod(modid = RPModule.modID, name = RPModule.modName, version = RPModule.modVersion, dependencies = "required-after:PowerConverters;required-after:PowerCrystalsCore")
 @NetworkMod(clientSideRequired = true, serverSideRequired = true)
 public class RPModule
 {
 	public static final String modID = "PCRPModule";
	public static final String modName = "PC: RP Module";
 	public static final String modVersion = "1.4.7R2.1.1";
 
 	public static Block converterBlockRedPower;
 
 	public static int blockIdRedPower;
 
 	public static PowerSystem powerSystemRedPower;
 
 	@SidedProxy(clientSide="overtjaguar.powerconverters.rpmodule.proxy.ClientProxy", serverSide="overtjaguar.powerconverters.rpmodule.proxy.CommonProxy")
 	public static CommonProxy proxy;
 
 	public static String textureFile = "/overtjaguar/powerconverters/rpmodule/textures/terrain.png";
 
 	@PreInit
 	public void preInit(FMLPreInitializationEvent e)
 	{
 		powerSystemRedPower = new PowerSystem("RedPower", "RP2", 7000, 7000, null, null, "W");
 
 		PowerSystem.registerPowerSystem(powerSystemRedPower);
 
 		Configuration config = new Configuration(e.getSuggestedConfigurationFile());
 		loadConfig(config);
 	}
 
 
 	private void loadConfig(Configuration config)
     {
 		blockIdRedPower = config.getBlock("blockIdRedPower", 2855).getInt();
 
 		config.save();
     }
 
 
 	@Init
 	public void Init(FMLInitializationEvent e)
 	{
 		converterBlockRedPower = new BlockPowerConverterRedPower(blockIdRedPower);
 		GameRegistry.registerBlock(converterBlockRedPower, ItemBlockPowerConverterRedPower.class, "blockPowerConverterRedPower");
 		GameRegistry.registerTileEntity(TileEntityRedPowerConsumer.class, "powerConverterRP2Consumer");
 		GameRegistry.registerTileEntity(TileEntityRedPowerProducer.class, "powerConverterRP2Producer");
 		LanguageRegistry.addName(new ItemStack(converterBlockRedPower, 1, 0), "RP2 Consumer");
 		LanguageRegistry.addName(new ItemStack(converterBlockRedPower, 1, 1), "RP2 Producer");
 
 		GameRegistry.addRecipe(new ItemStack(converterBlockRedPower, 1, 0),
 				"G G", "ROR", "G G",
 				Character.valueOf('G'), Item.ingotGold,
 				Character.valueOf('R'), Item.redstone,
 				Character.valueOf('O'), Block.obsidian);
 
 		GameRegistry.addShapelessRecipe(new ItemStack(converterBlockRedPower, 1, 1), new ItemStack(converterBlockRedPower, 1, 0));
 		GameRegistry.addShapelessRecipe(new ItemStack(converterBlockRedPower, 1, 0), new ItemStack(converterBlockRedPower, 1, 1));
 
 
 
 		proxy.load();
 	}
 
 	@PostInit
 	public void postInit(FMLPostInitializationEvent e)
 	{
 
 	}
 }
