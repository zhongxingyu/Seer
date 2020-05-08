 package com.someluigi.slperiph;
 
 
 import java.util.logging.Logger;
 
 import net.minecraft.block.Block;
 import net.minecraft.block.material.Material;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.util.EnumChatFormatting;
 import net.minecraftforge.common.Configuration;
 
 import com.someluigi.slperiph.block.ItemBlockSLP;
 import com.someluigi.slperiph.block.SLPBlock;
 import com.someluigi.slperiph.ccportable.shared.BlockAntenn;
 import com.someluigi.slperiph.ccportable.shared.BlockTransmitter;
 import com.someluigi.slperiph.ccportable.shared.GuiManager;
 import com.someluigi.slperiph.ccportable.shared.ItemPDA;
 import com.someluigi.slperiph.ccportable.shared.ItemQuartz;
 import com.someluigi.slperiph.ccportable.shared.PayloadManager;
 import com.someluigi.slperiph.ccportable.shared.RecipeQuartzMix;
 import com.someluigi.slperiph.ccportable.shared.UpgradeTransmitter;
 import com.someluigi.slperiph.server.SLPCommand;
 import com.someluigi.slperiph.server.SLPHTTPServer;
 import com.someluigi.slperiph.tileentity.TileEntityHTTPD;
 
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.Init;
 import cpw.mods.fml.common.Mod.Instance;
 import cpw.mods.fml.common.Mod.PreInit;
 import cpw.mods.fml.common.Mod.ServerStarting;
 import cpw.mods.fml.common.Mod.ServerStopping;
 import cpw.mods.fml.common.SidedProxy;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.event.FMLServerStartingEvent;
 import cpw.mods.fml.common.event.FMLServerStoppingEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.common.network.NetworkRegistry;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 import dan200.turtle.api.TurtleAPI;
 
 @Mod(modid = "SLPMod", name = "SLs Peripherals", version = "@VERSION@", dependencies = "after:ComputerCraft;after:CCTurtle")
 @NetworkMod(serverSideRequired = true, clientSideRequired = true, channels = { PayloadManager.CHANNEL_ID }, packetHandler = PayloadManager.class)
 
 public class SLPMod {
 
     public Logger l;
     
     @Instance
     public static SLPMod instance;
     
     
     @SidedProxy(
         clientSide = "com.someluigi.slperiph.client.ClientProxy", 
         serverSide = "com.someluigi.slperiph.CommonProxy"
     )
     public static CommonProxy proxy;
     
     
     
     public static class Config {
         public static int blockTransmitterID = 0;   
         public static int blockAntennID = 0;    
         
         public static int itemTerminalID    = 0;
         public static int itemQuartzID      = 0;
         
         public static int turtleRange       = 256;
         
         public static int minTransmitterRange = 128;
         public static int maxTransmitterRange = 640;
     }
     
     
     
     
 
     public static SLPBlock blockSLP;
     public static BlockAntenn blockAntenn;
     public static BlockTransmitter blockTransmitter;
     
     public static Item itemPDA;
     public static Item itemQuartz;
     
     public static int bPeriph;
 
     public static boolean craftHttpd;
     public static boolean debugM;
 
     public static int httpdPort;
     public static boolean httpdEnabled;
     public static String httpdStat;
 
     @PreInit
     public void preinit(FMLPreInitializationEvent evt) {
 
         this.l = Logger.getLogger("SLPMod");
 
         Configuration cfg = new Configuration(
                 evt.getSuggestedConfigurationFile());
 
         cfg.load();
 
         // get Block ID
         bPeriph = cfg.getBlock("block", "peripheral", 2047).getInt(2047);
 
         // enable crafting recipe?
         craftHttpd = cfg.get("crafting", "httpserver", true,
                 "Enable HTTP Server").getBoolean(true);
 
         // http server port
         httpdPort = cfg
                 .get("httpserver",
                         "port",
                         80,
                         "The port that the HTTP Server will run on. Default on the internet is 80, but you may have to change this.")
                 .getInt(80);
         httpdEnabled = cfg
                 .get("httpserver",
                         "enabled",
                         true,
                         "Whether the HTTP Server should actually run. This does not affect the peripherals in game, apart from making the HTTP Server unusable. This should be disabled if you get a crash, so your blocks will not be cleared and restore when it is fixed.")
                 .getBoolean(true);
 
         debugM = cfg.get("debug", "modeEnabled", false, "Debug mode")
                 .getBoolean(false);
         
         
         
         
         
         //Blocks
         Config.blockTransmitterID   = cfg.getBlock( "transmitterBlockID", 980).getInt();
         Config.blockAntennID        = cfg.getBlock( "antennBlockID", 981).getInt();
         // these share the httpd-server blockID
         
         
         //Items
         Config.itemTerminalID   = cfg.getItem("terminalItemID", 4200).getInt();
         Config.itemQuartzID     = cfg.getItem("quartzItemID", 4201).getInt();
         
         //Range
         Config.turtleRange      = cfg.get("general", "turtleRange", Config.turtleRange).getInt();
         
         Config.minTransmitterRange = cfg.get("general", "minRange", Config.minTransmitterRange).getInt();
         Config.maxTransmitterRange = cfg.get("general", "maxRange", Config.maxTransmitterRange).getInt();
         
 
         cfg.save();
     }
 
     // @SidedProxy(clientSide = "com.someluigi.slperiph.client.SLPClientProxy",
     // serverSide = "com.someluigi.slperiph.server.SLPProxy")
     // public static SLPProxy proxy;
 
     @Init
     public void init(FMLInitializationEvent evt) {
 
         /*
          * 1.5.1 Testing *
          * 
          * List tl = TextureManager.instance().createTexture(
          * "com/someluigi/slperiph/res/textures.png"); for (Object o : tl) {
          * System.out.print("Texture: " + o.toString()); }
          * 
          * /* End 1.5.1 Testing
          */
 
         // List tl =
         // TextureManager.instance().createTexture("com/someluigi/slperiph/res/blockhttpserver.png");
 
         this.l = Logger.getLogger("SLPMod");
 
         // proxy.registerRenderInfo(); // do stuff like preloading etc
 
         blockSLP = (SLPBlock) new SLPBlock(bPeriph, 0, Material.rock)
                 .setUnlocalizedName("slpPeriph").setLightValue(1F)
                 .setResistance(30).setHardness(1F);
         GameRegistry.registerBlock(blockSLP, ItemBlockSLP.class, "slp.slpPeriph"); // register
                                                                                // block                                                      // for
                                                                                // ItemBlock
         
         blockAntenn = new BlockAntenn(Config.blockAntennID);
         blockTransmitter = new BlockTransmitter(Config.blockTransmitterID);
         
         GameRegistry.registerBlock(blockAntenn, "slp.ccp.antenn");
         GameRegistry.registerBlock(blockTransmitter, "slp.cpp.trans");
         GameRegistry.registerTileEntity(TileEntityHTTPD.class, "slp-phttpd");
         
         itemPDA = new ItemPDA(Config.itemTerminalID);
         itemQuartz = new ItemQuartz(Config.itemQuartzID);
         
         GameRegistry.registerItem(itemPDA, "slp.pp.itemPDA");
         GameRegistry.registerItem(itemQuartz, "slp.pp.itemQuartz");
         
         
        proxy.unpackResourceFolder("com/someluigi/slperiph/lua", "mods/ComputerCraft/lua/rom/programs");
         proxy.init();
         
         NetworkRegistry.instance().registerGuiHandler(this, new GuiManager());
         
         TurtleAPI.registerUpgrade( new UpgradeTransmitter() );
         
         
         
         
         
         GameRegistry.addRecipe(new ItemStack( blockTransmitter ), 
                 new String[]{ "P", "B", "D" }, 
                 'P', Item.enderPearl,
                 'B', Block.blockSteel,
                 'D', Item.diamond
         );
 
         GameRegistry.addRecipe(new ItemStack( blockAntenn ), 
                 new String[]{ "OIO", "ODO", "OIO" }, 
                 'I', Item.ingotIron,
                 'D', Item.diamond,
                 'O', Block.obsidian
         );
 
         GameRegistry.addRecipe(new ItemStack( itemPDA ), 
                 new String[]{ "P", "I", "D" }, 
                 'P', Item.enderPearl,
                 'I', Item.ingotIron,
                 'D', Item.diamond
         );
         
         // field_94583_ca = netherQuartz
         GameRegistry.addRecipe( new ItemStack( itemQuartz, 1 ), new String[]{ "X", "Q", "X" }, 'X', Block.glass, 'Q', Item.field_94583_ca );
         GameRegistry.addRecipe( new RecipeQuartzMix() );
         
         
         LanguageRegistry.addName(new ItemStack(blockSLP, 1, 0),
                 "HTTP Server Service Module");
         
         LanguageRegistry.addName(new ItemStack(blockTransmitter, 1, 0), "Wireless Transmitter");
         
         LanguageRegistry.addName(new ItemStack(blockAntenn, 1, 0), "Antenna");
         
         LanguageRegistry.addName(new ItemStack(itemPDA, 1, 0), "Wireless Terminal");
         
         LanguageRegistry.addName(new ItemStack(itemQuartz, 1, 0), "Quartz Crystal");
         
         
         if (craftHttpd) {
             this.l.info("Adding recipe for HTTP Server Module");
 
             // HTTP Server Module crafting
             GameRegistry.addRecipe(new ItemStack(blockSLP),
                     new Object[] { "rtr", "gbg", "gig", Character.valueOf('r'),
                             Item.redstone, Character.valueOf('t'),
                             Block.torchRedstoneIdle, Character.valueOf('i'),
                             Item.ingotIron, Character.valueOf('g'),
                             Item.ingotGold, Character.valueOf('b'),
                             Block.blockSteel // this is actually IRON block
                     });
             GameRegistry.addRecipe(new ItemStack(blockSLP),
                     new Object[] { "rtr", "gbg", "gig", Character.valueOf('r'),
                             Item.redstone, Character.valueOf('t'),
                             Block.torchRedstoneActive, Character.valueOf('i'),
                             Item.ingotIron, Character.valueOf('g'),
                             Item.ingotGold, Character.valueOf('b'),
                             Block.blockSteel // this is actually IRON block
                     });
         } else {
             this.l.info("Crafting HTTP Server Module disabled");
         }
 
     }
 
     @ServerStarting
     public void serverstarting(FMLServerStartingEvent evt) {
 
         evt.registerServerCommand(new SLPCommand());
 
         this.l = Logger.getLogger("SLPMod");
 
         if (httpdEnabled) {
 
             this.l.info("Starting Simple HTTP Server NOW! - on port "
                     + httpdPort);
             SLPMod.httpdStat = EnumChatFormatting.GREEN + "ENABLED (port "
                     + httpdPort + ")";
             SLPHTTPServer.start(httpdPort);
             this.l.info("HTTP Server is started.");
             this.l.info("HTTP Server is using simpleframework, simpleframework.org");
 
         } else {
 
             SLPMod.httpdStat = EnumChatFormatting.RED + "DISABLED";
             this.l.info("HTTP Server is disabled in the config, will not start.");
 
         }
 
     }
 
     @ServerStopping
     public void serverstopping(FMLServerStoppingEvent evt) {
         SLPHTTPServer.stop();
     }
 
 }
