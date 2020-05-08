 package net.sctgaming.baconpancakes;
 
 import net.minecraft.block.Block;
 import net.minecraft.item.Item;
 import net.sctgaming.baconpancakes.block.BlockKitchenTile;
 import net.sctgaming.baconpancakes.block.BlockOven;
 import net.sctgaming.baconpancakes.gui.GuiHandler;
 import net.sctgaming.baconpancakes.item.Bacon;
 import net.sctgaming.baconpancakes.item.ItemHandMixer;
 import net.sctgaming.baconpancakes.item.PancakeHelmet;
 import net.sctgaming.baconpancakes.item.Pancakes;
 import net.sctgaming.baconpancakes.tile.TileEntityOven;
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
 
@Mod(modid = "BaconPancakes", name = "Bacon Pancakes", useMetadata = true, version = "1.0")
 @NetworkMod(serverSideRequired = false, clientSideRequired = true)
 public class BaconPancakes {
 	
 	@Instance("BaconPancakes")
 	public static BaconPancakes instance;
 	
 	@SidedProxy(clientSide="net.sctgaming.baconpancakes.ClientProxy", serverSide="net.sctgaming.baconpancakes.CommonProxy")
 	public static CommonProxy proxy;
 	
 	public final static Item pancakes = new Pancakes(23000);
 	public final static Item bacon = new Bacon(23001);
 	public final static Item pancakeHelmet = new PancakeHelmet(23002);
 	public final static Item handMixer = new ItemHandMixer(23003);
 	
 	public final static Block kitchenTile = new BlockKitchenTile(2300);
 	public final static Block blockOven = new BlockOven(2301);
 	
 	@PreInit
 	public void preInit(FMLPreInitializationEvent event) {
 		
 	}
 	
 	@Init
 	public void load(FMLInitializationEvent event) {
 		GameRegistry.registerItem(pancakes, "sctpancakes");
 		GameRegistry.registerItem(bacon, "sctbacon");
 		GameRegistry.registerItem(pancakeHelmet, "sctpancakehelmet");
 		GameRegistry.registerItem(handMixer, "sct.handmixer");
 		
 		GameRegistry.registerBlock(kitchenTile, "sctkitchentile");
 		GameRegistry.registerBlock(blockOven, "sct.oven");
 		
 		GameRegistry.registerTileEntity(TileEntityOven.class, "entityOven");
 		
 		LanguageRegistry.addName(pancakes, "Pancakes");
 		LanguageRegistry.addName(bacon, "Bacon");
 		LanguageRegistry.addName(pancakeHelmet, "Pancake Helmet");
 		LanguageRegistry.addName(handMixer, "Hand Mixer");
 		
 		LanguageRegistry.addName(kitchenTile, "Kitchen Tile");
 		LanguageRegistry.addName(blockOven, "Oven");
 		
 		NetworkRegistry.instance().registerGuiHandler(this, new GuiHandler());
 	}
 	
 	@PostInit
 	public void postInit(FMLPostInitializationEvent event) {
 		
 	}
 }
