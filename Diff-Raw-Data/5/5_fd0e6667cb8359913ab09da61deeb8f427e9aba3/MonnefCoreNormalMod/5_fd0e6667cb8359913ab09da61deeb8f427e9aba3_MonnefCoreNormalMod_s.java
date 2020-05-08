 /*
  * Jaffas and more!
  * author: monnef
  */
 
 package monnef.core.mod;
 
 import cpw.mods.fml.common.FMLCommonHandler;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.ModContainer;
 import cpw.mods.fml.common.ModMetadata;
 import cpw.mods.fml.common.SidedProxy;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPostInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.event.FMLServerStartingEvent;
 import cpw.mods.fml.common.network.NetworkRegistry;
 import cpw.mods.fml.server.FMLServerHandler;
 import monnef.core.Config;
 import monnef.core.MonnefCorePlugin;
 import monnef.core.Reference;
 import monnef.core.client.ExporterTickHandler;
 import monnef.core.command.CommandMC;
 import monnef.core.common.CommonProxy;
 import monnef.core.common.GuiHandler;
 import monnef.core.network.CorePacketHandlerMC17;
 import monnef.core.network.CorePacketHandlerTrait;
 import monnef.core.utils.BreakableIronMaterial;
 import net.minecraft.command.ICommandManager;
 import net.minecraft.command.ServerCommandManager;
 import net.minecraft.server.MinecraftServer;
 
 @Mod(modid = Reference.ModIdHelper, name = Reference.ModNameHelper, version = Reference.Version, dependencies = "required-after:monnef-core")
 public class MonnefCoreNormalMod {
     @SidedProxy(clientSide = "monnef.core.client.ClientProxy", serverSide = "monnef.core.common.CommonProxy")
     public static CommonProxy proxy;
 
     @Mod.Instance(Reference.ModIdHelper)
     public static MonnefCoreNormalMod instance;
     public static int renderID;
 
     public static CorePacketHandlerTrait packetHandler = CorePacketHandlerMC17.instance();
 
     @Mod.EventHandler
     public void preLoad(FMLPreInitializationEvent evt) {
         handleMetadata();
         proxy.registerContainers();
     }
 
     @Mod.EventHandler
     public void load(FMLInitializationEvent evt) {
         if (Config.isExporterEnabled() && Config.isCommandEnabled()) {
             NetworkRegistry.INSTANCE.registerGuiHandler(this, new GuiHandler());
             FMLCommonHandler.instance().bus().register(new ExporterTickHandler());
             MonnefCorePlugin.Log.printInfo("Exporter initialized.");
         }
 
         proxy.registerClientStuff();
         packetHandler.onLoad();
     }
 
     @Mod.EventHandler
     public void postLoad(FMLPostInitializationEvent evt) {
         BreakableIronMaterial.onPostLoad();
         packetHandler.onPostLoad();
     }
 
     @Mod.EventHandler
     public void serverStarting(FMLServerStartingEvent event) {
         if (Config.isCommandEnabled()) {
            MinecraftServer server = FMLServerHandler.instance().getServer();
            ICommandManager commandManager = server.getCommandManager();
            ServerCommandManager serverCommandManager = ((ServerCommandManager) commandManager);
            serverCommandManager.registerCommand(new CommandMC());
         }
     }
 
     private void handleMetadata() {
         ModContainer container = FMLCommonHandler.instance().findContainerFor(this);
         ModMetadata metaData = container.getMetadata();
         metaData.parent = Reference.ModId;
     }
 }
