 package morePistons.morePistons;
 
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.item.ItemCoal;
 import net.minecraft.item.ItemStack;
 import net.minecraft.tileentity.TileEntity;
 import morePistons.morePistons.pistons.*;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.Block;
 import cpw.mods.fml.common.Mod.EventHandler;
 import cpw.mods.fml.common.Mod.Init;
 import cpw.mods.fml.common.Mod.Instance;
 import cpw.mods.fml.common.Mod.Item;
 import cpw.mods.fml.common.Mod.PostInit;
 import cpw.mods.fml.common.Mod.PreInit; 
 import cpw.mods.fml.common.SidedProxy;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPostInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 
 
 @Mod(modid="MorePistons", name="MorePistons", version="0.0.1")
 @NetworkMod(clientSideRequired=true, serverSideRequired=false)
 public class MorePistons {
 	
 		public static final int idPistonExtension = 700;
 		public static final int idPistonMoving = 701;
 		public static final int idDoublePiston = 702;
 		public static final int idDoublePistonS = 703;
 		public static final int idTriplePiston = 704;
 		
 		
 		public static final BlockMorePistonExtension pistonExtension = new BlockMorePistonExtension(idPistonExtension);
 		public static final BlockMorePistonMoving pistonMoving = new BlockMorePistonMoving(idPistonMoving);
 		public static final BlockDoublePistonBase doublePiston = new BlockDoublePistonBase(idDoublePiston, false);
 		public static final BlockDoublePistonBase doublePistonS = new BlockDoublePistonBase(idDoublePistonS, true);
 		public static final BlockTriplePistonBase triplePiston = new BlockTriplePistonBase(idTriplePiston, false);
 		//public static final Block triplePistonS = new BlockTriplePistonBase(705, false);
 		
 		
 		
 		  public static CreativeTabs tabPistons = new CreativeTabs("tabPistons") {
               public ItemStack getIconItemStack() {
                       return new ItemStack(net.minecraft.item.Item.appleRed, 1, 0);
               }
       };
 	
         // The instance of your mod that Forge uses.
         @Instance("MorePistons")
         public static MorePistons instance;
         
         // Says where the client and server 'proxy' code is loaded.
         @SidedProxy(clientSide="morePistons.morePistons.ClientProxy", serverSide="morePistons.morePistons.CommonProxy")
         public static CommonProxy proxy;
         
         @EventHandler
         public void preInit(FMLPreInitializationEvent event) {
                 // Stub Method
         }
         
         @EventHandler
         public void load(FMLInitializationEvent event) {
             proxy.registerRenderers();
             
         	LanguageRegistry.addName(pistonExtension, "Piston Extension");
        	GameRegistry.registerBlock(pistonExtension, "Extension");
         	LanguageRegistry.addName(pistonMoving, "Piston Moving");
        	GameRegistry.registerBlock(pistonMoving, "Moving");
         	
         	
         	LanguageRegistry.addName(doublePiston, "Double Piston");
         	GameRegistry.registerBlock(doublePiston, "Double Piston");
         	GameRegistry.addRecipe(new ItemStack(doublePiston), "dd", 'd', ItemCoal.coal);
         	
         	LanguageRegistry.addName(doublePistonS, "Double Sticky Piston");
         	GameRegistry.registerBlock(doublePistonS, "Double Sticky Piston");
         	
         	LanguageRegistry.addName(triplePiston, "Triple Piston");
         	GameRegistry.registerBlock(triplePiston, "Triple Pistons");
         	
         	
         	LanguageRegistry.instance().addStringLocalization("itemGroup.tabPistons", "en_US", "Pistons");
         	
         	
         	addCreativeTab();
         	
         }
         
         
         
         public void addCreativeTab(){
         	pistonExtension.setCreativeTab(tabPistons);
         	pistonMoving.setCreativeTab(tabPistons);
         	
         	doublePiston.setCreativeTab(tabPistons);
         	
         	triplePiston.setCreativeTab(tabPistons);
         	
         }
         
         
         @EventHandler
         public void postInit(FMLPostInitializationEvent event) {
                 // Stub Method
         }
         
         
     	public static TileEntity getTileEntity(int par1, int par2, int par3, boolean par4, boolean par5) {
     		return new TileEntityMorePiston(par1, par2, par3, par4, par5);
     	}
 
 }
