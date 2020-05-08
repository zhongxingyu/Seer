 package ml.boxes;
 
 import ml.boxes.block.BlockBox;
 import ml.boxes.block.BlockMeta;
 import ml.boxes.block.MetaType;
 import ml.boxes.item.ItemBox;
 import ml.boxes.item.ItemResources;
 import ml.boxes.item.ItemBoxBlocks;
 import ml.boxes.item.ItemType;
 import ml.boxes.network.PacketHandler;
 import ml.boxes.recipe.RecipeBox;
 import ml.boxes.tile.TileEntityBox;
 import ml.boxes.tile.TileEntityCrate;
 import ml.boxes.tile.TileEntitySafe;
 import net.minecraft.block.Block;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.util.WeightedRandomChestContent;
 import net.minecraftforge.common.ChestGenHooks;
 import net.minecraftforge.common.Configuration;
 import net.minecraftforge.oredict.ShapedOreRecipe;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.Init;
 import cpw.mods.fml.common.Mod.Instance;
 import cpw.mods.fml.common.Mod.PreInit;
 import cpw.mods.fml.common.SidedProxy;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.common.network.NetworkRegistry;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 
 @Mod(modid="Boxes", name="Boxes", dependencies="required-after:Forge@[6.5,);required-after:MLCore@[0.4,)")
 @NetworkMod(versionBounds="[0.4,)", clientSideRequired=true, serverSideRequired=false, channels={"Boxes"}, packetHandler=PacketHandler.class)
 public class Boxes {
 	
 	@SidedProxy(serverSide="ml.boxes.CommonProxy", clientSide="ml.boxes.client.ClientProxy")
 	public static CommonProxy proxy;
 	@Instance("Boxes")
 	public static Boxes instance;
 	public static Boolean neiInstalled = false;
 	
 	public static BlockBox BlockBox;
 	public static BlockMeta BlockMeta;
 	public static ItemResources ItemResources;
 	
 	public static int nullRendererID = -1;
 	public static CreativeTabs BoxTab = new BoxesCreativeTab("boxes");
 	
 	public static BoxesConfig config = new BoxesConfig();
 	
 	@PreInit
 	public void PreInit(FMLPreInitializationEvent evt){
 		Configuration cfg = new Configuration(evt.getSuggestedConfigurationFile());
 		config.load(cfg);
 	}
 	
 	@Init
 	public void Init(FMLInitializationEvent evt){
 		GameRegistry.registerTileEntity(TileEntityBox.class, "box");
 		GameRegistry.registerTileEntity(TileEntityCrate.class, "crate");
 		GameRegistry.registerTileEntity(TileEntitySafe.class, "safe");
 		
 		this.BlockBox = new BlockBox(config.boxBlockID);
 		GameRegistry.registerBlock(this.BlockBox, ItemBox.class, "box");
 		this.BlockMeta = new BlockMeta(config.generalBlockID);
 		GameRegistry.registerBlock(this.BlockMeta, ItemBoxBlocks.class, "boxesMeta");
 		
 		this.ItemResources = new ItemResources(config.cardboardItemID-256);
 		GameRegistry.registerItem(ItemResources, "cardboard");
 		
 		LanguageRegistry.instance().addStringLocalization("item.box.name", "en_US", "Box");
 		LanguageRegistry.instance().addStringLocalization("item.cardboard.name", "en_US", "Cardboard Sheet");
 		LanguageRegistry.instance().addStringLocalization("item.label.name", "en_US", "Label");
 		LanguageRegistry.instance().addStringLocalization("itemGroup.boxes", "en_US", "Boxes");
 		
		LanguageRegistry.instance().addStringLocalization("Boxes.safe.name", "en_US", "Lockbox");
 		LanguageRegistry.instance().addStringLocalization("Boxes.crate.name", "en_US", "Crate");
 		
 		NetworkRegistry.instance().registerGuiHandler(instance, proxy);
 		
 		GameRegistry.addRecipe(new ItemStack(ItemResources, 1, 0), "ppp", "sws", "ppp", 'p', Item.paper, 's', Item.silk, 'w', Item.bucketWater);
 		GameRegistry.addRecipe(ItemType.ISFromType(ItemType.Label, 3), "ppp", " s ", 'p', Item.paper, 's', Item.slimeBall);
 		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(BlockMeta, 1, MetaType.Crate.ordinal()), "wsw", "scs", "wsw", 'w', "logWood", 's', "plankWood", 'c', Block.chest));
 		GameRegistry.addRecipe(new RecipeBox());
 		
 		initDungeonLoot();
 		
 		proxy.load();
 	}
 	
 	public void initDungeonLoot(){
 		ChestGenHooks.addItem(ChestGenHooks.DUNGEON_CHEST, new WeightedRandomChestContent(new ItemStack(ItemResources), 1, 3, 100));
 		ChestGenHooks.addItem(ChestGenHooks.VILLAGE_BLACKSMITH, new WeightedRandomChestContent(new ItemStack(ItemResources), 1, 5, 12));
 		ChestGenHooks.addItem(ChestGenHooks.BONUS_CHEST, new WeightedRandomChestContent(new ItemStack(ItemResources), 1, 4, 7));
 	}
 	
 }
