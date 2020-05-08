 package se.mickelus.modjam;
 
 
 import se.mickelus.modjam.gen.GenerationHandler;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.EventHandler;
 import cpw.mods.fml.common.Mod.Instance;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLServerStartingEvent;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 
 @Mod (modid = Constants.MOD_ID, name = Constants.MOD_NAME, version = Constants.VERSION)
 public class Modjam {
 	
 	@Instance("modjam - layernine")
     public static Modjam instance;
 	
 	@EventHandler
     public void init(FMLInitializationEvent event) {
         System.out.println("PRINT INIT");
         new GenerationHandler();
     }
 	
 	@EventHandler
 	public void serverStart(FMLServerStartingEvent event){
 		System.out.println("SERVER START");
 		MinecraftServer server = MinecraftServer.getServer();
 		ICommandManager command = server.getCommandManager();
 		ServerCommandManager serverCommand = (ServerCommandManager) command;
 		serverCommand.registerCommand(new SaveCommand());
 		serverCommand.registerCommand(new CreateBlockCommand());
 		serverCommand.registerCommand(new LoadCommand());
 	}
 
 }
