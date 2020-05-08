 package mods.mod_nw;
 
 import java.util.logging.Level;
 
 import mods.mod_nw.Block.*;
 import mods.mod_nw.Config.BLOCK_ID;
 import mods.mod_nw.Config.ITEM_ID;
 import mods.mod_nw.Item.*;
 import mods.mod_nw.WorldGen.OreWorldGenerator;
 import net.minecraft.block.Block;
 import net.minecraft.block.BlockStone;
 import net.minecraft.block.material.Material;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraftforge.common.Configuration;
 import net.minecraftforge.common.MinecraftForge;
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
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 
@Mod(modid = "NordWest", name = "Mod for mcnw.ru", version = "1.0.0")
 // @NetworkMod(channels = { "NordWest" }, clientSideRequired = true,
 // serverSideRequired = false, packetHandler = PacketHandler.class)
 public class NordWest {
 	@Instance("mod_nw")
 	public static NordWest instance;
 	@SidedProxy(clientSide = "mods.mod_nw.client.ClientProxy", serverSide = "mods.mod_nw.CommonProxy")
 	public static CommonProxy proxy;
 	public static CreativeTabs tabNord = new CreativeTabs("tabNord") {
 		public ItemStack getIconItemStack() {
 			return new ItemStack(Item.eyeOfEnder, 1, 0);
 		}
 	};
 	public static Config cfg = null;
 
 	public static Item lexpiItem;
 	public static Item mingotItem;
 	public static Item cingotItem;
 
 	@PreInit
 	public void preInit(FMLPreInitializationEvent event) {
 		cfg = new Config(event.getSuggestedConfigurationFile());
 		proxy.registerRenderers();
 		/** Item **/
 		lexpiItem = new BaseItem(Config.ITEM_ID.lexpi_id).setUnlocalizedName("lexpi");
 		mingotItem = new BaseItem(Config.ITEM_ID.mingot_id).setUnlocalizedName("mingot");
 		cingotItem = new BaseItem(Config.ITEM_ID.cingot_id).setMaxStackSize(64).setUnlocalizedName("cingot");
 		/** Block **/
 
 		NWBlock.init();
		Recipes.init();
 	}
 
 // LanguageRegistry language = LanguageRegistry.instance();
 
 	@Init
 	public void load(FMLInitializationEvent event) {
 		proxy.registerEvent();
 		
 		
 		/** Рецепты печки **/
 		GameRegistry.addSmelting(NWBlock.coperore.blockID, new ItemStack(cingotItem), 0.2f);
 		GameRegistry.addSmelting(NWBlock.mithrilore.blockID, new ItemStack(mingotItem), 0.2f);
 		GameRegistry.addSmelting(NWBlock.customTreeWood.blockID, new ItemStack(Item.coal, 1, 1), 0.2f);
 		/** Генераторы **/
 		GameRegistry.registerWorldGenerator(new OreWorldGenerator());
 	}
 
 	public static final String LanguageDirectory = "/mods/mod_nw/lang/";
 
 	@PostInit
 	public void postInit(FMLPostInitializationEvent event) {
 
 	}
 
 }
