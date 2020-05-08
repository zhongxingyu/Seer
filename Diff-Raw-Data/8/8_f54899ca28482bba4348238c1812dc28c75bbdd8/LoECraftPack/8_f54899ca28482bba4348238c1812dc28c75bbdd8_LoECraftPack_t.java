 package loecraftpack;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import loecraftpack.blocks.BlockBedColor;
 import loecraftpack.blocks.ProtectionMonolithBlock;
 import loecraftpack.blocks.te.ProtectionMonolithTileEntity;
 import loecraftpack.items.Bits;
 import loecraftpack.items.ItemBedColor;
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
 	@Instance
     public static LoECraftPack instance = new LoECraftPack();
 	
 	
   ///Colored Beds///
 	
 	private static ItemBedColor iBedWhite;
     private static ItemBedColor iBedOrange;
     private static ItemBedColor iBedMagenta;
     private static ItemBedColor iBedLightBlue;
     private static ItemBedColor iBedYellow;
     private static ItemBedColor iBedLime;
     private static ItemBedColor iBedPink;
     private static ItemBedColor iBedGray;
     private static ItemBedColor iBedLightGray;
     private static ItemBedColor iBedCyan;
     private static ItemBedColor iBedPurple;
     private static ItemBedColor iBedBlue;
     private static ItemBedColor iBedBrown;
     private static ItemBedColor iBedGreen;
     private static ItemBedColor iBedRed;
     private static ItemBedColor iBedBlack;
     
     private static BlockBedColor bBedWhite;
     private static BlockBedColor bBedOrange;
     private static BlockBedColor bBedMagenta;
     private static BlockBedColor bBedLightBlue;
     private static BlockBedColor bBedYellow;
     private static BlockBedColor bBedLime;
     private static BlockBedColor bBedPink;
     private static BlockBedColor bBedGray;
     private static BlockBedColor bBedLightGray;
     private static BlockBedColor bBedCyan;
     private static BlockBedColor bBedPurple;
     private static BlockBedColor bBedBlue;
     private static BlockBedColor bBedBrown;
     private static BlockBedColor bBedGreen;
     private static BlockBedColor bBedRed;
     private static BlockBedColor bBedBlack;
     
   ///Colored-Beds END///
 	
 	
 	@SidedProxy(clientSide = "loecraftpack.proxies.ClientProxy", serverSide = "loecraftpack.proxies.CommonProxy")
     public static CommonProxy proxy;
 	
 	public static CreativeTabs LoECraftTab = new CreativeTabs("LoECraftTab")
 	{
         public ItemStack getIconItemStack()
         {
                 return new ItemStack(Item.writableBook, 1, 0);
         }
 	};
 	
 	public static final Bits bits = new Bits(667);
 	public static final ProtectionMonolithBlock monolith = new ProtectionMonolithBlock(666);
 	
 	@PreInit
     public void preInit(FMLPreInitializationEvent event)
 	{
 		///Colored Beds///
 		
     	iBedWhite     = new ItemBedColor(670);
         iBedOrange    = new ItemBedColor(671);
         iBedMagenta   = new ItemBedColor(672);
         iBedLightBlue = new ItemBedColor(673);
         iBedYellow    = new ItemBedColor(674);
         iBedLime      = new ItemBedColor(675);
         iBedPink      = new ItemBedColor(676);
         iBedGray      = new ItemBedColor(677);
         iBedLightGray = new ItemBedColor(678);
         iBedCyan      = new ItemBedColor(679);
         iBedPurple    = new ItemBedColor(680);
         iBedBlue      = new ItemBedColor(681);
         iBedBrown     = new ItemBedColor(682);
         iBedGreen     = new ItemBedColor(683);
         iBedRed       = new ItemBedColor(684);
         iBedBlack     = new ItemBedColor(685);
         
         bBedWhite     = new BlockBedColor(670);
         bBedOrange    = new BlockBedColor(671);
         bBedMagenta   = new BlockBedColor(672);
         bBedLightBlue = new BlockBedColor(673);
         bBedYellow    = new BlockBedColor(674);
         bBedLime      = new BlockBedColor(675);
         bBedPink      = new BlockBedColor(676);
         bBedGray      = new BlockBedColor(677);
         bBedLightGray = new BlockBedColor(678);
         bBedCyan      = new BlockBedColor(679);
         bBedPurple    = new BlockBedColor(680);
         bBedBlue      = new BlockBedColor(681);
         bBedBrown     = new BlockBedColor(682);
         bBedGreen     = new BlockBedColor(683);
         bBedRed       = new BlockBedColor(684);
         bBedBlack     = new BlockBedColor(685);
         
         //Link relation information directly
         iBedWhite.block = bBedWhite; bBedWhite.item = iBedWhite;
         iBedOrange.block = bBedOrange; bBedOrange.item = iBedOrange;
         iBedMagenta.block = bBedMagenta; bBedMagenta.item = iBedMagenta;
         iBedLightBlue.block = bBedLightBlue; bBedLightBlue.item = iBedLightBlue;
         iBedYellow.block = bBedYellow; bBedYellow.item = iBedYellow;
         iBedLime.block = bBedLime; bBedLime.item = iBedLime;
         iBedPink.block = bBedPink; bBedPink.item = iBedPink;
         iBedGray.block = bBedGray; bBedGray.item = iBedGray;
         iBedLightGray.block = bBedLightGray; bBedLightGray.item = iBedLightGray;
         iBedCyan.block = bBedCyan; bBedCyan.item = iBedCyan;
         iBedPurple.block = bBedPurple; bBedPurple.item = iBedPurple;
         iBedBlue.block = bBedBlue; bBedBlue.item = iBedBlue;
         iBedBrown.block = bBedBrown; bBedBrown.item = iBedBrown;
         iBedGreen.block = bBedGreen; bBedGreen.item = iBedGreen;
         iBedRed.block = bBedRed; bBedRed.item = iBedRed;
         iBedBlack.block = bBedBlack; bBedBlack.item = iBedBlack;
         
       ///Colored-Beds END///
         
     }
 	
 	@Init
 	public void load(FMLInitializationEvent e)
 	{
 		LanguageRegistry.instance().addStringLocalization("itemGroup.LoECraftTab", "en_US", "LoECraft");
 		
 		for(int i = 0; i < Bits.names.length; i++ )
 			LanguageRegistry.instance().addStringLocalization("item.itemBits." + Bits.iconNames[i] + ".name", "en_US", Bits.names[i]);
 		
 		GameRegistry.registerBlock(monolith, "ProtectionMonolithBlock");
 		LanguageRegistry.addName(monolith, "Protection Monolith");
 		
 		GameRegistry.registerTileEntity(ProtectionMonolithTileEntity.class, "ProtectionMonolithTileEntity");
 		
 		NetworkRegistry.instance().registerGuiHandler(this, new GuiHandler());
 		MinecraftForge.EVENT_BUS.register(new EventHandler());
 		
 		proxy.doProxyStuff();
 		
 		///Colored Beds///
 		
 		//Bed Items
         LanguageRegistry.addName(iBedWhite,     "Bed : White");
         LanguageRegistry.addName(iBedOrange,    "Bed : Orange");
         LanguageRegistry.addName(iBedMagenta,   "Bed : Magenta");
         LanguageRegistry.addName(iBedLightBlue, "Bed : Light Blue");
         LanguageRegistry.addName(iBedYellow,    "Bed : Yellow");
         LanguageRegistry.addName(iBedLime,      "Bed : Lime");
         LanguageRegistry.addName(iBedPink,      "Bed : Pink");
         LanguageRegistry.addName(iBedGray,      "Bed : Gray");
         LanguageRegistry.addName(iBedLightGray, "Bed : Light Gray");
         LanguageRegistry.addName(iBedCyan,      "Bed : Cyan");
         LanguageRegistry.addName(iBedPurple,    "Bed : Purple");
         LanguageRegistry.addName(iBedBlue,      "Bed : Blue");
         LanguageRegistry.addName(iBedBrown,     "Bed : Brown");
         LanguageRegistry.addName(iBedGreen,     "Bed : Green");
         LanguageRegistry.addName(iBedRed,       "Bed : Red");
         LanguageRegistry.addName(iBedBlack,     "Bed : Black");
         
         //Bed Blocks
         LanguageRegistry.addName(  bBedWhite,     "Bed : White");
         GameRegistry.registerBlock(bBedWhite,     "bedWhite");
         LanguageRegistry.addName(  bBedOrange,    "Bed : Orange");
         GameRegistry.registerBlock(bBedOrange,    "bedOrange");
         LanguageRegistry.addName(  bBedMagenta,   "Bed : Magenta");
         GameRegistry.registerBlock(bBedMagenta,   "bedMagenta");
         LanguageRegistry.addName(  bBedLightBlue, "Bed : Light Blue");
         GameRegistry.registerBlock(bBedLightBlue, "bedLightBlue");
         LanguageRegistry.addName(  bBedYellow,    "Bed : Yellow");
         GameRegistry.registerBlock(bBedYellow,    "bedYellow");
         LanguageRegistry.addName(  bBedLime,      "Bed : Lime");
         GameRegistry.registerBlock(bBedLime,      "bedLime");
         LanguageRegistry.addName(  bBedPink,      "Bed : Pink");
         GameRegistry.registerBlock(bBedPink,      "bedPink");
         LanguageRegistry.addName(  bBedGray,      "Bed : Gray");
         GameRegistry.registerBlock(bBedGray,      "bedGray");
         LanguageRegistry.addName(  bBedLightGray, "Bed : Light Gray");
         GameRegistry.registerBlock(bBedLightGray, "bedLightGray");
         LanguageRegistry.addName(  bBedCyan,      "Bed : Cyan");
         GameRegistry.registerBlock(bBedCyan,      "bedCyan");
         LanguageRegistry.addName(  bBedPurple,    "Bed : Purple");
         GameRegistry.registerBlock(bBedPurple,    "bedPurple");
         LanguageRegistry.addName(  bBedBlue,      "Bed : Blue");
         GameRegistry.registerBlock(bBedBlue,      "bedBlue");
         LanguageRegistry.addName(  bBedBrown,     "Bed : Brown");
         GameRegistry.registerBlock(bBedBrown,     "bedBrown");
         LanguageRegistry.addName(  bBedGreen,     "Bed : Green");
         GameRegistry.registerBlock(bBedGreen,     "bedGreen");
         LanguageRegistry.addName(  bBedRed,       "Bed : Red");
         GameRegistry.registerBlock(bBedRed,       "bedRed");
         LanguageRegistry.addName(  bBedBlack,     "Bed : Black");
         GameRegistry.registerBlock(bBedBlack,     "bedBlack");
         
         ///Colored-Beds END///
 		
 		
 		///Colored Beds///
 		
         ///Update Recipes
         
         //bed list by wool color
         Item[] bedItems = new Item[16];
         bedItems [0]  = iBedWhite;
         bedItems [1]  = iBedOrange;
         bedItems [2]  = iBedMagenta;
         bedItems [3]  = iBedLightBlue;
         bedItems [4]  = iBedYellow;
         bedItems [5]  = iBedLime;
         bedItems [6]  = iBedPink;
         bedItems [7]  = iBedGray;
         bedItems [8]  = iBedLightGray;
         bedItems [9]  = iBedCyan;
         bedItems [10] = iBedPurple;
         bedItems [11] = iBedBlue;
         bedItems [12] = iBedBrown;
        bedItems [15] = iBedGreen;
         bedItems [14] = iBedRed;
        bedItems [13] = iBedBlack;
         
         //get CraftingManager
     	CraftingManager cmi = CraftingManager.getInstance();
     	
     	//locate and remove old bed recipe
     	List recipes = cmi.getRecipeList();
     	Iterator r = recipes.iterator();
     	while (r.hasNext())
     	{
     		//test if recipe creates a bed
     		IRecipe ir = (IRecipe)r.next();
 			if( ir != null && ir.getRecipeOutput() != null && ir.getRecipeOutput().itemID == Item.bed.itemID )
 			{
 				//clear old recipe and move on
 				r.remove();
 				break;//there really should only be one vanilla bed to remove
 			}
     	}
     	
     	//add new recipes to replace the old one
     	for (int i = 0; i < 16; i++)
    		cmi.addRecipe(new ItemStack(bedItems[i]), "###", "XXX", '#', new ItemStack(Block.cloth, 1, i), 'X', Block.planks);
         
       ///Colored-Beds END///
 		
 	}
 	
 	@PostInit
 	public void postLoad(FMLPostInitializationEvent e)
 	{
 		List recipeList = new ArrayList();
 		recipeList.addAll(CraftingManager.getInstance().getRecipeList());
 		
 		for(int i = 0; i < recipeList.size(); i++)
 		{
 			IRecipe recipe = (IRecipe)recipeList.get(i);
 			ItemStack output = recipe.getRecipeOutput();
 			if (output != null && output.itemID == 130)
 				CraftingManager.getInstance().getRecipeList().remove(recipe);
 		}
 	}
 }
