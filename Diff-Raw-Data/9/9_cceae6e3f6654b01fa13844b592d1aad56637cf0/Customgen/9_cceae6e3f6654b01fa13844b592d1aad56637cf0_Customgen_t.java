 package se.mickelus.customgen;
 
 
 import net.minecraft.command.ICommandManager;
 import net.minecraft.command.ServerCommandManager;
 import net.minecraft.item.Item;
 import net.minecraft.server.MinecraftServer;
 import se.mickelus.customgen.blocks.EmptyBlock;
 import se.mickelus.customgen.blocks.InterfaceBlock;
 import se.mickelus.customgen.items.GenBookItem;
 import se.mickelus.customgen.items.PlaceholderItem;
 import se.mickelus.customgen.network.PacketHandler;
 import se.mickelus.customgen.newstuff.FileHandler;
 import se.mickelus.customgen.newstuff.ForgeGenerator;
 import se.mickelus.customgen.newstuff.Gen;
 import se.mickelus.customgen.newstuff.GenManager;
 import se.mickelus.customgen.proxy.Proxy;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.EventHandler;
 import cpw.mods.fml.common.Mod.Instance;
 import cpw.mods.fml.common.SidedProxy;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.event.FMLServerStartingEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 @Mod (modid = Constants.MOD_ID, name = Constants.MOD_NAME, version = Constants.VERSION)
 @NetworkMod (channels = {Constants.CHANNEL}, serverSideRequired = true, packetHandler = PacketHandler.class)
 public class Customgen {
 	
 	@Instance(Constants.MOD_ID)
     public static Customgen instance;
 	
 	@SidedProxy(clientSide = "se.mickelus.customgen.proxy.ClientProxy", serverSide = "se.mickelus.customgen.proxy.ServerProxy")
 	public static Proxy proxy;
 	
 	
 	
 	
 	@EventHandler
     public void preInit(FMLPreInitializationEvent event) {
 
         ConfigHandler.init(event.getSuggestedConfigurationFile());
                   
     }
 	
 	@EventHandler
     public void init(FMLInitializationEvent event) {  
 		
         setupBlocks();
         setupItems();
         
         //new GuiHandler();
         
         
         
         proxy.init();
     }
 	
 	
 	@EventHandler
 	public void serverStart(FMLServerStartingEvent event){
 		System.out.println("SERVER START");
 		MinecraftServer server = MinecraftServer.getServer();
 		ICommandManager command = server.getCommandManager();
 		ServerCommandManager serverCommand = (ServerCommandManager) command;
 		
 		new ForgeGenerator();
 		
 		GenManager genManager = new GenManager();
         Gen[] gens = FileHandler.parseAllGens();
        if(gens != null) {
        	for (int i = 0; i < gens.length; i++) {
    			genManager.addGen(gens[i]);
    		}
        }
        
 	}
 	
 	private void setupBlocks() {
 		System.out.println(Constants.EMPTY_ID);
 		System.out.println(Constants.INTERFACEBLOCK_ID);
 		EmptyBlock empty = new EmptyBlock(Constants.EMPTY_ID);
 		GameRegistry.registerBlock(empty, Constants.EMPTY_UNLOC_NAME);
 		LanguageRegistry.addName(empty, Constants.EMPTY_NAME);
 		
 		InterfaceBlock interfaceBlock = new InterfaceBlock(Constants.INTERFACEBLOCK_ID);
 		GameRegistry.registerBlock(interfaceBlock, Constants.INTERFACEBLOCK_UNLOC_NAME);
 		LanguageRegistry.addName(interfaceBlock, Constants.INTERFACEBLOCK_NAME);
 	}
 	
 	private void setupItems() {
 		PlaceholderItem placeholder = new PlaceholderItem(Constants.PLACEHOLDERITEM_ID);
 		LanguageRegistry.addName(placeholder, Constants.PLACEHOLDERITEM_NAME);
 		
 		GenBookItem bookItem = new GenBookItem(Constants.BOOKITEM_ID);
 		LanguageRegistry.addName(bookItem, Constants.BOOKITEM_NAME);
 	}
 
 }
