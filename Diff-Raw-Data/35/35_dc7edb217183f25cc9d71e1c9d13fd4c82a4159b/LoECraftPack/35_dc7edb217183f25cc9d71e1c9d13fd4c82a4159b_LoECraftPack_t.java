 package loecraftpack;
 
 import java.util.Iterator;
 import java.util.List;
 
 import loecraftpack.blocks.ColoredBedBlock;
 import loecraftpack.blocks.ProtectionMonolithBlock;
 import loecraftpack.blocks.te.ColoredBedTileEntity;
 import loecraftpack.blocks.te.ProtectionMonolithTileEntity;
 import loecraftpack.enums.Dye;
 import loecraftpack.items.Bits;
 import loecraftpack.items.ColoredBedItem;
 import loecraftpack.logic.handlers.EventHandler;
 import loecraftpack.logic.handlers.GuiHandler;
 import loecraftpack.packethandling.ClientPacketHandler;
 import loecraftpack.packethandling.ServerPacketHandler;
 import loecraftpack.proxies.CommonProxy;
 import net.minecraft.block.Block;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.item.crafting.CraftingManager;
 import net.minecraft.item.crafting.IRecipe;
 import net.minecraftforge.common.MinecraftForge;
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
 import cpw.mods.fml.common.network.NetworkMod.SidedPacketHandler;
 import cpw.mods.fml.common.network.NetworkRegistry;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 
 
 @Mod(modid = "loecraftpack", name = "LoECraft Pack", version = "1.0")
 
 @NetworkMod(clientSideRequired=true, serverSideRequired=false, clientPacketHandlerSpec = @SidedPacketHandler(channels = {"loecraftpack" }, packetHandler = ClientPacketHandler.class),
 serverPacketHandlerSpec = @SidedPacketHandler(channels = {"loecraftpack" }, packetHandler = ServerPacketHandler.class))
 public class LoECraftPack
 {
 	/***************************/
 	/**Variable Initialization**/
 	/***************************/
 	
 	//Create a singleton
 	@Instance
     public static LoECraftPack instance = new LoECraftPack();
 	
 	//Instantiate bed item variables
 	
 	
 	
 	//Register proxies
 	@SidedProxy(clientSide = "loecraftpack.proxies.ClientProxy", serverSide = "loecraftpack.proxies.CommonProxy")
     public static CommonProxy proxy;
 	
 	//Create our own creative tab
 	public static CreativeTabs LoECraftTab = new CreativeTabs("LoECraftTab")
 	{
 		//Set the icon - TODO: ADD NEW ITEM WITH CUSTOM ICON FOR USE HERE 
         public ItemStack getIconItemStack()
         {
                 return new ItemStack(Item.writableBook, 1, 0);
         }
 	};
 	
 	//Declare immutable items and blocks - TODO: INITIALIZE THESE IN PREINIT BASED ON CONFIG IDS
 	public static final Bits bits = new Bits(667);
 	public static final ColoredBedItem bedItems = new ColoredBedItem(670);
 	public static final ProtectionMonolithBlock monolith = new ProtectionMonolithBlock(666);
 	public static final ColoredBedBlock bedBlock = new ColoredBedBlock(670);
 	
 	/****************************/
 	/**Forge Pre-Initialization**/
 	/****************************/
 	
 	@PreInit
     public void preInit(FMLPreInitializationEvent event)
 	{
 		/***************/
 		/**Load Config**/
 		/***************/
 		
 		//TODO: LOAD CONFIG HERE
 		
 		/************************/
 		/**Initialize Variables**/
 		/************************/
     }
 	
 	/************************/
 	/**Forge Initialization**/
 	/************************/
 	
 	@Init
 	public void load(FMLInitializationEvent e)
 	{
 		/****************************/
 		/**Register everything else**/
 		/****************************/
 		
 		//Creative tab
 		LanguageRegistry.instance().addStringLocalization("itemGroup.LoECraftTab", "LoECraft");
 		
 		//Items
 		for(int i = 0; i < Bits.names.length; i++ )
 			LanguageRegistry.instance().addStringLocalization("item.itemBits." + Bits.iconNames[i] + ".name", Bits.names[i]);
 		
 		//Blocks
 		GameRegistry.registerBlock(monolith, "ProtectionMonolithBlock");
 		LanguageRegistry.addName(monolith, "Protection Monolith");
 		
 		//Bed items and blocks
 		GameRegistry.registerBlock(bedBlock, "ColoredBed");
		for(int i = 0; i < ColoredBedBlock.bedTypes; i++)
			LanguageRegistry.instance().addStringLocalization("item.coloredBed." + Dye.values()[i] + ".name", "Bed : " + Dye.values()[i]);
 		
 		//Tile Entities
 		GameRegistry.registerTileEntity(ProtectionMonolithTileEntity.class, "ProtectionMonolithTileEntity");
 		GameRegistry.registerTileEntity(ColoredBedTileEntity.class, "ColoredBedTileEntity");
 		
 		//Handlers
 		NetworkRegistry.instance().registerGuiHandler(this, new GuiHandler());
 		MinecraftForge.EVENT_BUS.register(new EventHandler());
 		
 		/******************/
 		/**Do Proxy Stuff**/
 		/******************/
 		
 		//Schtuff
 		proxy.doProxyStuff();
 		
 		/******************/
 		/**Update Recipes**/
 		/******************/
         
         //get CraftingManager
     	CraftingManager cmi = CraftingManager.getInstance();
     	
     	//locate and remove old bed recipe
     	List recipes = cmi.getRecipeList();
     	Iterator r = recipes.iterator();
     	while (r.hasNext())
     	{
     		IRecipe ir = (IRecipe)r.next();
     		//if the recipe outputs a bed, remove it
 			if(ir.getRecipeOutput() != null && ir.getRecipeOutput().itemID == Item.bed.itemID )
 			{
 				r.remove();
 				break; //there really should only be one vanilla bed to remove, so stop once we find it
 			}
     	}
     	
     	//add the new bed recipes to replace the old one we just removed
     	for (int i = 0; i < 16; i++)
     	{
     		cmi.addRecipe(new ItemStack(bedItems, 1, i), "###", "XXX", '#', new ItemStack(Block.cloth, 1, i), 'X', Block.planks);
     	}
 	}
 	
 	/*****************************/
 	/**Forge Post-Initialization**/
 	/*****************************/
 	
 	@PostInit
 	public void postLoad(FMLPostInitializationEvent e)
 	{
 		//TODO: POST-LOAD STUFF
 	}
 }
