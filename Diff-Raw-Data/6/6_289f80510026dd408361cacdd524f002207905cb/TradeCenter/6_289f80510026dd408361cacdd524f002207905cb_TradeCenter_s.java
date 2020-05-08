 package zh.tradecenter;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.Properties;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import net.minecraft.block.Block;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraftforge.common.Configuration;
 import net.minecraftforge.common.MinecraftForge;
 import net.minecraftforge.common.Property;
 import net.minecraftforge.oredict.ShapedOreRecipe;
 import zh.tradecenter.blocks.ZHTradePost;
 import zh.tradecenter.handlers.ZHTradeCenterGUIHandler;
 import zh.tradecenter.handlers.ZHTradeCenterPacketHandler;
 import zh.tradecenter.handlers.ZHTradeCenterVillagerHandler;
 import zh.tradecenter.tileentities.ZHTradePostEntity;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.Init;
 import cpw.mods.fml.common.Mod.PostInit;
 import cpw.mods.fml.common.Mod.PreInit;
 import cpw.mods.fml.common.Mod.ServerStarting;
 import cpw.mods.fml.common.SidedProxy;
 import cpw.mods.fml.common.Mod.Instance;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPostInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.event.FMLServerStartingEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.common.network.NetworkRegistry;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 import cpw.mods.fml.common.registry.VillagerRegistry;
 
 // TODO: Remote villager trading
 // TODO: Automated villager trading...? urrrgh...
 
 @Mod(modid = "ZHTradeCenter", name = "Villager Trade Center", version = "0.2.2")
 @NetworkMod(clientSideRequired = true, serverSideRequired = true, packetHandler = ZHTradeCenterPacketHandler.class, channels = {"ZH_Trade"})
 public class TradeCenter
 {
     @Instance("ZHTradeCenter")
     public static TradeCenter instance;
     
     @SidedProxy(clientSide = "zh.tradecenter.client.TradeCenterClientProxy", serverSide = "zh.tradecenter.TradeCenterCommonProxy")
     public static TradeCenterCommonProxy proxy;
     
     public static final String tradeChannel = "ZH_Trade";
     
     public static Property tradePostRange;
     public static Property tradePostMaxTrades;
     public static Property displayEmptyTrades;
     public static Property enableBetterVillagerTrades;
     
     public static Configuration config = null;
     public static Logger logger;
     public static File configFolder;
     
     public static Block tradeCenter = null;
     
     @PreInit
     public void preInit(FMLPreInitializationEvent event)
     {
         configFolder = new File(event.getModConfigurationDirectory().getAbsolutePath() + "/zh/TradeCenter/");
         
         logger = event.getModLog();
         
         try
         {
             config = new Configuration(new File(configFolder.getAbsolutePath() + "/config.cfg"));
             config.load();
             
             tradePostRange = config.get("Trade Center", "Trade Center Range", 32);
             tradePostRange.comment = "Maximum range (in blocks) the Trade Center will look for villagers";
             tradePostMaxTrades = config.get("Trade Center", "Max Villagers to find", 1000);
             tradePostMaxTrades.comment = "Maximum number of Villagers the trade center will look for before stopping";
             displayEmptyTrades = config.get("Trade Center", "Display trades that have no more uses left", false);
             enableBetterVillagerTrades = config.get("Trade Center", "Better Villager Trades", true);
             enableBetterVillagerTrades.comment = "Skews trades towards the most profitable (to you!) and adds additional items (mod drops and nether loot) that can be traded";
         }
         catch (Exception ex)
         {
             logger.log(Level.SEVERE, "UsefulThings couldn't load the config file");
             ex.printStackTrace();
         }
         finally
         {
             config.save();
         }
         
         //probably COMPLETELY unnecessary to handle the display names this way, but...
         extractLang(new String[] {"en_US"});
         loadLang();
     }
     
     @Init
     public void load(FMLInitializationEvent event)
     {
         NetworkRegistry.instance().registerGuiHandler(this, new ZHTradeCenterGUIHandler());
         
         for (int i = 0; i < 6; i++)
             VillagerRegistry.instance().registerVillageTradeHandler(i, new ZHTradeCenterVillagerHandler());
         
        tradeCenter = new ZHTradePost(config.getBlock("tradePost", 417).getInt()).setCreativeTab(CreativeTabs.tabBlock).setHardness(1.0f).setResistance(10.0f).setStepSound(Block.soundWoodFootstep).setUnlocalizedName("tradePost");
         GameRegistry.registerBlock(tradeCenter, "zhTradeCenter");
         GameRegistry.registerTileEntity(ZHTradePostEntity.class, "zhTradeCenter");
         MinecraftForge.setBlockHarvestLevel(tradeCenter, "axe", 0);
         
         GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(tradeCenter), new Object[] {
         "xxx", "xyx", "xxx", 'x', "plankWood", 'y', new ItemStack(Item.book) }));
     }
 
     private void extractLang(String[] languages)
     {
         String langResourceBase = "/zh/tradecenter/lang/";
         for (String lang : languages)
         {
             InputStream is = TradeCenter.instance.getClass().getResourceAsStream(langResourceBase + lang + ".lang");
             try
             {
                 File f = new File(configFolder.getAbsolutePath() + "/lang/" + lang + ".lang");
                 if (!f.exists())
                     f.getParentFile().mkdirs();
                 OutputStream os = new FileOutputStream(f);
                 byte[] buffer = new byte[1024];
                 int read = 0;
                 while ((read = is.read(buffer)) != -1)
                 {
                     os.write(buffer, 0, read);
                 }
                 is.close();
                 os.flush();
                 os.close();
             }
             catch (IOException e)
             {
                 logger.log(Level.SEVERE, "Couldn't load language file: " + langResourceBase + lang + ".lang");
                 e.printStackTrace();
             }
         }
     }
     
     public static void initClient(FMLPreInitializationEvent evt)
     {
         
     }
     
     @ServerStarting
     public void serverStarting(FMLServerStartingEvent evt)
     {
         
     }
     
     @PostInit
     public void postInit(FMLPostInitializationEvent event)
     {
  
     }
     
     private void loadLang()
     {
         File f = new File(configFolder.getAbsolutePath() + "/lang/");
                 
         for (File langFile : f.listFiles(new FilenameFilter()
             {
                 @Override
                 public boolean accept(File dir, String name)
                 {
                     return name.endsWith(".lang");
                 }
             }))
         {
             try
             {
                 Properties langPack = new Properties();
                 langPack.load(new FileInputStream(langFile));
                 String lang = langFile.getName().replace(".lang", "");
                 LanguageRegistry.instance().addStringLocalization(langPack, lang);
             }
             catch (FileNotFoundException x)
             {
                 x.printStackTrace();
             }
             catch (IOException x)
             {
                 x.printStackTrace();
             }
         }
     }
     
     
 }
