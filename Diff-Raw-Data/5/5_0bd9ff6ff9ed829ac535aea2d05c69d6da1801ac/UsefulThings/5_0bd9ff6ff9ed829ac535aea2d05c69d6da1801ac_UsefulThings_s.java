 package zh.usefulthings;
 
 //internal imports
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Properties;
 import java.util.Random;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import powercrystals.minefactoryreloaded.api.FarmingRegistry;
 import powercrystals.minefactoryreloaded.api.IFactoryHarvestable;
 import powercrystals.minefactoryreloaded.api.IFactoryPlantable;
 
 import scala.Console;
 import thermalexpansion.api.crafting.CraftingManagers;
 
 import zh.usefulthings.blocks.ZHBlock;
 import zh.usefulthings.blocks.ZHCactus;
 import zh.usefulthings.blocks.ZHClayBlock;
 import zh.usefulthings.blocks.ZHCoalOre;
 import zh.usefulthings.blocks.ZHMud;
 import zh.usefulthings.blocks.ZHMultiOreItemBlock;
 import zh.usefulthings.blocks.ZHMultiOreBlock;
 import zh.usefulthings.blocks.ZHTradePost;
 import zh.usefulthings.blocks.ZHWebBlock;
 import zh.usefulthings.client.ClientProxy;
 import zh.usefulthings.compatibility.minefactoryreloaded.ZHCactusFruitHarvestHandler;
 import zh.usefulthings.compatibility.minefactoryreloaded.ZHFlaxFertilizerHandler;
 import zh.usefulthings.compatibility.minefactoryreloaded.ZHFlaxHarvestHandler;
 import zh.usefulthings.compatibility.minefactoryreloaded.ZHFlaxPlantHandler;
 import zh.usefulthings.compatibility.minefactoryreloaded.ZHMeatCropHarvestHandler;
 import zh.usefulthings.compatibility.minefactoryreloaded.ZHMeatCropPlantHandler;
 import zh.usefulthings.crops.ZHCactusFlower;
 import zh.usefulthings.crops.ZHCactusFruit;
 import zh.usefulthings.crops.ZHCactusItemBlock;
 import zh.usefulthings.crops.ZHFlaxCrop;
 import zh.usefulthings.crops.ZHMeatCrop;
 import zh.usefulthings.crops.ZHSeeds;
 import zh.usefulthings.crops.ZHTreeShroom;
 import zh.usefulthings.food.*;
 import zh.usefulthings.gui.ZHCreativeTab;
 import zh.usefulthings.handlers.ZHBonemealHandler;
 import zh.usefulthings.handlers.ZHFuelHandler;
 import zh.usefulthings.handlers.ZHGUIHandler;
 import zh.usefulthings.handlers.ZHPacketHandler;
 import zh.usefulthings.handlers.ZHUsefulDropsEvent;
 import zh.usefulthings.handlers.ZHVillageTradeHandler;
 import zh.usefulthings.items.ZHDye;
 import zh.usefulthings.items.ZHItem;
 import zh.usefulthings.items.ZHMultiItem;
 import zh.usefulthings.tileentities.ZHTradePostEntity;
 import zh.usefulthings.tools.ZHAthame;
 import zh.usefulthings.tools.ZHAxe;
 import zh.usefulthings.tools.ZHHoe;
 import zh.usefulthings.tools.ZHPickaxe;
 import zh.usefulthings.tools.ZHScythe;
 import zh.usefulthings.tools.ZHShovel;
 import zh.usefulthings.tools.ZHSword;
 import zh.usefulthings.worldgen.ZHWorldGen;
 //forge/minecraft imports
 import net.minecraft.block.Block;
 import net.minecraft.block.BlockCrops;
 import net.minecraft.block.BlockFlower;
 import net.minecraft.block.BlockNetherStalk;
 import net.minecraft.block.material.Material;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.item.EnumToolMaterial;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemPickaxe;
 import net.minecraft.item.ItemSeeds;
 import net.minecraft.item.ItemStack;
 import net.minecraft.item.crafting.FurnaceRecipes;
 import net.minecraft.item.crafting.IRecipe;
 import net.minecraft.item.crafting.ShapedRecipes;
 import net.minecraft.item.crafting.ShapelessRecipes;
 import net.minecraft.potion.Potion;
 import net.minecraft.src.ModLoader;
 import net.minecraft.util.WeightedRandomChestContent;
 import net.minecraftforge.common.ChestGenHooks;
 import net.minecraftforge.common.Configuration;
 import net.minecraftforge.common.EnumHelper;
 import net.minecraftforge.common.EnumPlantType;
 import net.minecraftforge.common.MinecraftForge;
 import net.minecraftforge.common.Property;
 import net.minecraftforge.liquids.LiquidDictionary;
 import net.minecraftforge.oredict.OreDictionary;
 import net.minecraftforge.oredict.ShapedOreRecipe;
 import net.minecraftforge.oredict.ShapelessOreRecipe;
 import net.minecraft.item.crafting.CraftingManager;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.Init;
 import cpw.mods.fml.common.Mod.Instance;
 import cpw.mods.fml.common.Mod.PostInit;
 import cpw.mods.fml.common.Mod.PreInit;
 import cpw.mods.fml.common.Mod.ServerStarting;
 import cpw.mods.fml.common.SidedProxy;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPostInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.event.FMLServerStartingEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.common.network.NetworkRegistry;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 import cpw.mods.fml.common.registry.VillagerRegistry;
 
 //TODO: Remote villager trading
 //TODO: Automated villager trading...? urrrgh...
 
 //TODO: Compress items into fewer item IDs < SAVE FOR 1.6 update...
 //	All Meats -> 1 id
 //	All other foods -> 1 id (wolves?)
 //	All seeds -> 1 id
 
 @Mod(modid="ZHUsefulThings", name="Useful Things", version="0.2.1", dependencies = "after:MineFactoryReloaded")
 @NetworkMod(clientSideRequired=true, serverSideRequired=true, packetHandler=ZHPacketHandler.class, channels={"ZH_Trade"})
 public class UsefulThings 
 {	
 	public static final String tradeChannel = "ZH_Trade";
 	
 	//new foods
 	//All below drop bones when eaten
 	public static Item ribsRaw = null;
 	public static Item ribsCooked = null;
 	public static Item drumstickRaw = null;
 	public static Item drumstickCooked = null;
 	
 	//Generic meats
 	public static Item muttonRaw = null;
 	public static Item muttonCooked = null;
 	
 	//Generic foods
 	//Below are not done!
 	public static Item cookedEgg = null;
 	public static Item zombieJerky = null;
 	public static Item jerky = null;
 	
 	//Special foods
 	//TODO: public static Item magicFruitSalad = null;
 	//TODO: public static Item magicCake = null;
 	//TODO: public static Item soups = null;
 	
 	//ingredients
 	public static Item flour = null;
 	
 	//new plants/seeds/fruit/whatever
 	public static Item cactusFruit = null;
 	public static ItemSeeds flaxSeeds = null;
 	public static ItemSeeds meatSeeds = null;
 
 	//new dyes
 	public static Item zhDyes = null;
 		
 	//public static Item tar = null;
 	public static Item sapphireGem = null;
 	public static Item salt = null;
 	
 	public static Block zhOres = null;
 	public static Block tradeCenter = null;
 	public static BlockFlower flaxCrop = null;
 	public static BlockNetherStalk meatCrop = null;
 	public static Block cactusFruitBlock = null;
 	public static Block zhCoalOre = null;
 	public static Block zhCactus = null;
 	public static Block zhMud = null;
 	public static Block sapphireBlock = null;
 	//blocks below are not done!
 	//public static Block treeShroom = null;
 
 	//Tools
 	public static Item sapphirePick = null;
 	public static Item sapphireShovel = null;
 	public static Item sapphireHoe = null;
 	public static Item sapphireAxe = null;
 	public static Item sapphireSword = null;
 	public static Item flintPick = null;
 	public static Item flintShovel = null;
 	public static Item flintHoe = null;
 	public static Item flintAxe = null;
 	public static Item flintSword = null;
 	public static Item cactusPick = null;
 	public static Item cactusShovel = null;
 	public static Item cactusHoe = null;
 	public static Item cactusAxe = null;
 	public static Item cactusSword = null;
 	public static Item swordAthame = null;
 	
 	public static Item scytheIron = null;
 	public static Item scytheWood = null;
 	public static Item scytheStone = null;
 	public static Item scytheGold = null;
 	public static Item scytheDiamond = null;
 	public static Item scytheFlint = null;
 	public static Item scytheCactus = null;
 	public static Item scytheSapphire = null;
 		
 	public static Configuration config = null;
 	
 	public static EnumToolMaterial sapphireMaterial = null;
 	public static EnumToolMaterial cactusMaterial = null;
 	public static EnumToolMaterial flintMaterial = null;
 	
 	public static ZHWorldGen zhWorldGen;
 	
 	private static Property curID;
 	private static Property curItemID;
 	private static int blockID = 410;
 	private static int itemID = 5521;
 	
 	// The instance of your mod that Forge uses.
     @Instance("ZHUsefulThings")
     public static UsefulThings instance;
     
     // Says where the client and server 'proxy' code is loaded.
     @SidedProxy(clientSide="zh.usefulthings.client.ClientProxy", serverSide="zh.usefulthings.CommonProxy")
     public static CommonProxy proxy;
 
     //default info about the ores
   	public static final int numOres = 3;
   	public static final String[] subNames = {"slimeOre", "sapphireOre", "saltOre"};
   	public static final String[] multiBlockName = {"Slime Encrusted Stone", "Sapphire Ore", "Salt Ore"};
   	public static int[] minY = {0,0,50};
   	public static int[] maxY = {20,45,128};
   	public static float[] numVeins = {1,5,10};
   	public static int[] veinSize = {24,9,20};
   	private static int[] harvestLevel = {0,2,0};
     
     //Properties!
     public static Property[] generateOre = new Property[numOres];
     public static Property[] oreMinY = new Property[numOres];
 	public static Property[] oreMaxY = new Property[numOres];
 	public static Property[] oreNumVeins = new Property[numOres];
 	public static Property[] oreVeinSize = new Property[numOres];
 	
 	//Vanilla changes
 	public static Property replaceCoal;
 	public static Property replaceWeb;
 	public static Property replaceCactus;
 	public static Property replaceClayBlock;
 	public static Property potionsStack;
 	public static Property maxPotionStack;
 	public static Property durableShovels;
 	public static Property durableAxes;
 	public static Property moreSeeds;
 	public static Property morePassiveDrops;
 	public static Property moreEnemyDrops;
 	public static Property flintTools;
 	public static Property cactusTools;
 	public static Property betterStarterLoot;
 	public static Property removeFoodLoot;
 	public static Property enableNewRecipes;
 	public static Property enableBetterWheat;
 	public static Property enableConvenienceRecipes;
 	public static Property enableMoreEmeraldLoot;
 	public static Property enableBetterVillagerTrades;
 	
 	//Mod element disablers
 	public static Property enableMud;
 	public static Property enableOres;
 	public static Property enableTradePost;
 	public static Property enableCactusFruit;
 	public static Property enableFlax;
 	public static Property enableMeatBush;
 	public static Property enableMoreFoodFromAnimals;
 	public static Property enableNewFoods;
 	public static Property enableNewTools;
 	
 	public static Property funMud;
 	
 	public static Property tradePostRange;
 	public static Property tradePostMaxTrades;
 	public static Property displayEmptyTrades;
 	
 	public static Property generateCactusFruit;
 	public static Property generateClay;
 	public static Property generateMelon;
 	public static Property generateEmeraldOre;
 	public static Property generateNiceSurprises;
 	
 	public static Property generateFlax;
 	public static Property generateMeatBushes;
 	//private static Property generateMud;
 	    
 	private static Property dimensionBlacklist;
 	private static Property scytheRadius;
 	private static Property scytheCropsList;
 	private static Property scytheCropsBlackList;
 
 	public static List<Integer> dimBlacklist = new ArrayList<Integer>();
     public static List<String> worldTypeBlacklist = new ArrayList<String>();
     public static HashMap<Integer,Integer> scytheWhiteList = new HashMap<Integer,Integer>();
     public static List<Integer> scytheBlackList = new ArrayList<Integer>();
     
     public static Logger logger;
     public static File configFolder;
 		
     @PreInit
     public void preInit(FMLPreInitializationEvent event) 
     {
     	
     	configFolder = new File(event.getModConfigurationDirectory().getAbsolutePath() + "/zh/VariousThings/");
     	
     	logger = event.getModLog();
     	
     	try
     	{
 			config = new Configuration(new File(configFolder.getAbsolutePath() + "/config.cfg"));
 			//Console.println("Loading config from: " + new File(configFolder.getAbsolutePath() + "/config.cfg").getAbsolutePath());
 			config.load();
 			
 			curID = config.get("ID Assignment", "Block ID:", blockID);
 			curID.comment = "The block ID to use as a starting point for assignment. Delete all block IDs to reassign";
 			
 			curItemID = config.get("ID Assignment", "Item ID", itemID);
 			curItemID.comment = "The item ID to use as a starting point for assignment. Delete all item IDs to reassign.";
 			
 			dimensionBlacklist = config.get("World Generation", "Dimension Blacklist", "-1,1");
 	        dimensionBlacklist.comment = "A comma-separated list of dimension IDs to disable worldgen in.";
 			
 	        enableMud = config.get("New Blocks/Items", "Enable Mud block", false);
 	        enableMud.comment = "You will sink completely through a mud block if there is water or another mudblock below it. Otherwise, you sink about half-way through the block. Doesn't spawn, can only be crafted.";
 			enableOres = config.get("New Blocks/Items", "Enable new Ores", true);
 			enableOres.comment = "Slime Encrusted Stone, Sapphire Ore, and Salt Ore";
 			enableCactusFruit = config.get("New Blocks/Items", "Enable Cactus Berries", true);
 			enableCactusFruit.comment = "Adds special fruiting cactus blocks that grow berries similarly to cocoa beans";
 			enableFlax = config.get("New Blocks/Items", "Enable Flax plant", true);
 			enableFlax.comment = "A growable source of string and blue dye";
 			enableMeatBush = config.get("New Blocks/Items", "Enable Nether Meat Bush", true);
 			enableMeatBush.comment = "A growable source of rotten flesh and bones";
 			enableNewFoods = config.get("New Blocks/Items", "Enable new Foods", true);
 			enableMoreFoodFromAnimals = config.get("New Blocks/Items", "More food from vanilla animals", true);
 			enableMoreFoodFromAnimals.comment = "Adds mutton, chicken wings, and ribs (from pigs). Also makes squid drop fish and eggs cookable";
 			enableNewTools = config.get("New Blocks/Items", "Enable new tools", true);
 			enableNewTools.comment = "Scythes and Athame";
 			enableTradePost = config.get("New Blocks/Items", "Enable Villager Trade Center", true);
 			enableTradePost.comment = "A block that enables a very handy GUI for display/search information about all trades of Villagers within a number of blocks. Quickly allows finding the right villager to trade with.";
 			
 			tradePostRange = config.get("Trade Center", "Trade Center Range", 32);
 			tradePostRange.comment = "Maximum range (in blocks) the Trade Center will look for villagers";
 			tradePostMaxTrades = config.get("Trade Center", "Max Villagers to find", 1000);
 			tradePostMaxTrades.comment = "Maximum number of Villagers the trade center will look for before stopping";
 			displayEmptyTrades = config.get("Trade Center", "Display trades that have no more uses left", false);
 						
 			generateCactusFruit = config.get("World Generation","Generate Cactus Berry pods",true);
 			generateCactusFruit.comment = "Will generate a single fruit-bearing cactus in about 1 in 15 Desert chunks";
 			generateFlax = config.get("World Generation", "Generate wild Flax plants", true);
 			generateFlax.comment = "Will rarely spawn in Plains, Forest, and Extreme Hills biomes. Note: Flax will spawn from bonemeal and drops as seeds from grass even if this is false. As long as it is enabled, of course";
 			generateMeatBushes = config.get("World Generation", "Generate Meat Bushes in Nether", true);
 			generateMeatBushes.comment = "Make the nether a meat paradise. Well, a rotten meat paradise at any rate...";
 			
 			generateClay = config.get("World Generation", "Generate Clay deposits", true);
 			generateClay.comment = "Adds some clay about 2 layers of dirt down across all biomes (except deserts/oceans/rivers). Clay will also replace some sand in Swamps.";
 			generateEmeraldOre = config.get("World Generation", "Generate more emerald ore", true);
 			generateEmeraldOre.comment = "Generates in all biomes, Extreme Hills get even more";
 			generateNiceSurprises = config.get("World Generation", "Generate some nice surprises", true);
 			generateNiceSurprises.comment = "It's a surprise!";
 			generateMelon = config.get("World Generation", "Generate wild Melons", true);
 			generateMelon.comment = "Will rarely spawn in Ocean, Jungle, Plains, and Forest biomes";
 			
 			//Vanilla replacement blocks
 			replaceCoal = config.get("Vanilla Enhanced", "Coal Ore drops more Coal", true);
 			replaceCoal.comment = "Drops 1-3 coal instead of only 1";
 			replaceWeb = config.get("Vanilla Enhanced", "Webs drop more loot", true);
 			replaceWeb.comment = "Drops more string and a chance at dropping a spider eye";
 			replaceCactus = config.get("Vanilla Enhanced", "Replace Cactus", true);
 			replaceCactus.comment = "Cactuses can be built up easier (right click any side with another cactus in hand) and will not destroy items that touch them";
 			replaceClayBlock = config.get("Vanilla Enhanced", "Replace Clay Block", true);
 			replaceClayBlock.comment = "Clay blocks will drop themselves instead of 4 clay balls, you can craft 1 clay block into 4 clay balls";
 			
 			enableNewRecipes = config.get("Vanilla Enhanced", "Enable recipes for some vanilla items", true);
 			enableNewRecipes.comment = "Adds recipes for saddles and sponges";
 			enableBetterWheat = config.get("Vanilla Enhanced", "Enable better wheat recipes", true);
 			enableConvenienceRecipes = config.get("Vanilla Enhanced", "Enable a few convenience recipes", true);
 			enableConvenienceRecipes.comment = "Sapling -> Sticks, Gravel -> Flint, smelt Rotten Flesh -> Leather";
 			
 			potionsStack = config.get("Vanilla Enhanced", "Potions stack", true);
 			maxPotionStack = config.get("Vanilla Enhanced", "Potion Stack Size", 3);
 			maxPotionStack.comment = "Recommended to be a multiple of 3 due to how potion brewing works!";
 			
 			durableShovels = config.get("Vanilla Enhanced", "Shovels more durable", true);
 			durableShovels.comment = "Doubles the durability of Shovels";
 			durableAxes = config.get("Vanilla Enhanced", "Axes more durable", true);
 			durableAxes.comment = "Doubles the durability of Axes";
 			
 			moreSeeds = config.get("Vanilla Enhanced", "Grass drops more vanilla seed items", true);
 			moreSeeds.comment = "Adds melon, pumpkin, carrots, potatoes, and poison potatoes to the grass drop list";
 			
 			morePassiveDrops = config.get("Vanilla Enhanced", "Passive Mobs drop +1 loot", true);
 			morePassiveDrops.comment = "All passive mobs will *always* drop +1 more of whatever loot they drop";
 			moreEnemyDrops = config.get("Vanilla Enhanced", "Enemy Mobs drop +1 loot", true);
 			morePassiveDrops.comment = "All aggressive mobs will *always* drop +1 more of whatever loot they drop";
 			
 			flintTools = config.get("Vanilla Enhanced", "Enable Flint Tools", true);
 			flintTools.comment = "If Better Start chest loot is also enabled, flint tools will replace Stone tools generated";
 			cactusTools = config.get("Vanilla Enhanced", "Enable Cactus Tools", true);
 			cactusTools.comment = "If Better Start chest loot is also enabled, cactus tools will replace cactus tools generated";
 			
 			betterStarterLoot = config.get("Vanilla Enhanced", "Better Starter Chest loot", true);
 			betterStarterLoot.comment = "Attempts to add more wood, some seeds, some string, and a few saplings to the bonus chest";
 			removeFoodLoot = config.get("Vanilla Enhanced", "Remove Food from Dungeon Chests", true);
 			removeFoodLoot.comment = "Will remove melon/pumpkin seeds, bread, wheat, cocoa beans, etc from dungeon loot tables. Golden apples remain.";
 			enableMoreEmeraldLoot = config.get("Vanilla Enhanced", "More emeralds in dungeon chests", true);
 			enableBetterVillagerTrades = config.get("Vanilla Enhanced", "Better Villager Trades", true);
 			enableBetterVillagerTrades.comment = "Skews trades towards the most profitable (to you!) and adds additional items that can be traded";
 			
 			funMud = config.get("Misc", "Enable Super-Fun Mud", false);
 			funMud.comment = "Build a 2x2x2 pit of mud, jump in, then jump out... not recommended for hardcore mode";
 			scytheRadius = config.get("Misc", "Scythe Search Radius", 3);
 			scytheRadius.comment = "Radius scythes will search for plants/leaves to harves. Default = 3 blocks";
 	
 	        if(enableOres.getBoolean(true))
 	        {
 				for (int i = 0; i < numOres; i++)
 				{
 					generateOre[i] = config.get("World Generation","Generate " + multiBlockName[i],true);
 					oreMinY[i] = config.get("World Generation",multiBlockName[i] + "Min Y",minY[i]);
 					oreMaxY[i] = config.get("World Generation",multiBlockName[i] + "Max Y",maxY[i]);
 					oreVeinSize[i] = config.get("World Generation",multiBlockName[i] + "Vein Size",veinSize[i]);
 					oreNumVeins[i] = config.get("World Generation",multiBlockName[i] + "Num Veins",numVeins[i]);
 				}
 	        }
     	}
     	catch (Exception e)
     	{
     		logger.log(Level.SEVERE, "UsefulThings couldn't load the config file");
             e.printStackTrace();   		
     	}
     	finally
     	{
     		config.save();
     	}
 
     	setDimBlackList();
     	extractLang(new String[] {"en_US"});
         loadLang();
         
         //ClientProxy.init();
         //CommonProxy.init();
     }
     
     private void setCropsList() 
     {
     	 String cropsList = scytheCropsList.getString().trim();
          
     	 //logger.warning(cropsList);
     	 
     	 String[] block = null;
     	 
          for (String crop : cropsList.split(","))
          {
              try
              {
             	 block = crop.split(":");
             	 
                  Integer cropID = Integer.parseInt(block[0]);
                  Integer cropMeta = Integer.parseInt(block[1]);
                  
                  if (Block.blocksList[cropID] != null)
                  {
                 	 if (!scytheWhiteList.containsKey(cropID))
                     	 scytheWhiteList.put(cropID, cropMeta);
                  }
                  
              }
              catch (Exception e)
              {
             	 logger.warning("Invalid format in scythe crops whitelist: " + block + "!");
              }
          }
          
          cropsList = scytheCropsBlackList.getString().trim();
          
          for(String crop : cropsList.split(","))
          {
         	 try
         	 {
         		 Integer cropID = Integer.parseInt(crop);
         		 
         		 if (Block.blocksList[cropID] != null)
         		 {
         			 if (!scytheBlackList.contains(cropID))
         				 scytheBlackList.add(cropID);
         		 }
         		 
         	 }
         	 catch (Exception e)
         	 {
         		 logger.warning("Invalid format in scythe crops blacklist!");
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
     
     public static void extractLang(String[] languages)
     {
         String langResourceBase =  "/zh/usefulthings/lang/";
         for (String lang : languages)
         {
         	//Console.println("Load language file: " + langResourceBase + lang + ".lang");
             InputStream is = UsefulThings.instance.getClass().getResourceAsStream(langResourceBase + lang + ".lang");
            // if (is == null)
             	//Console.println("Load language file failed!");
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
     
     public static void loadLang()
     {
         File f = new File(configFolder.getAbsolutePath() + "/lang/");
         //Console.println("Load language file: " + f.getAbsolutePath());
         
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
     
     private static void setDimBlackList()
     {
         String blacklist = dimensionBlacklist.getString().trim();
         
         for (String dim : blacklist.split(","))
         {
             try
             {
                 Integer dimID = Integer.parseInt(dim);
                 if (!dimBlacklist.contains(dimID))
                     dimBlacklist.add(dimID);
             }
             catch (Exception e)
             {
             	logger.warning("Invalid format in dimension blacklist!");
             }
         }
     }
     
     @Init
     public void load(FMLInitializationEvent event) 
     {	
         //Registers new Drop Event - handles new mob drops
         MinecraftForge.EVENT_BUS.register(new ZHUsefulDropsEvent());
         MinecraftForge.EVENT_BUS.register(new ZHBonemealHandler());
         //Registers new Fuel handler - handles improved fuel values
         //GameRegistry.registerFuelHandler(new ZHFuelHandler()); 
         NetworkRegistry.instance().registerGuiHandler(this, new ZHGUIHandler());
         
         for(int i = 0; i < 6; i++)
         	VillagerRegistry.instance().registerVillageTradeHandler(i, new ZHVillageTradeHandler());
         
     	
 		//New Tool Material
 		sapphireMaterial = EnumHelper.addToolMaterial("Sapphire", 2, 500, 12.0f, 3, 18);
 		cactusMaterial = EnumHelper.addToolMaterial("Cactus", 0, 131, 2.0f, 2, 15);
 		flintMaterial = EnumHelper.addToolMaterial("Flint", 2, 131, 6.0f, 2, 5);
 		
 		//Create actual block/item instances, register the blocks
 		createItems();
 		createBlocks();
 		registerBlocks();
 		
 		zhWorldGen = new ZHWorldGen();
 		
 		//Add items to MFR registry...
 		try
 		{
 			Class<?> registry = Class.forName("powercrystals.minefactoryreloaded.MFRRegistry");
 			if (registry != null)
 			{
 				if(enableCactusFruit.getBoolean(true))
 				{
 					FarmingRegistry.registerFruit(new ZHCactusFruitHarvestHandler());
 					FarmingRegistry.registerFruitLogBlockId(Block.cactus.blockID);
 					FarmingRegistry.registerFruitLogBlockId(zhCactus.blockID);
 				}
 				if(enableFlax.getBoolean(true))
 				{
 					FarmingRegistry.registerHarvestable(new ZHFlaxHarvestHandler());
 					FarmingRegistry.registerPlantable(new ZHFlaxPlantHandler());
 					FarmingRegistry.registerFertilizable(new ZHFlaxFertilizerHandler());
 				}
 				if(enableMeatBush.getBoolean(true))
 				{
 					FarmingRegistry.registerHarvestable(new ZHMeatCropHarvestHandler());
 					FarmingRegistry.registerPlantable(new ZHMeatCropPlantHandler());
 				}
 				if(enableOres.getBoolean(true))
 				{
 					FarmingRegistry.registerLaserOre(10, new ItemStack(zhOres,1,0));
 					FarmingRegistry.registerLaserOre(10, new ItemStack(zhOres,1,1));
 					FarmingRegistry.registerLaserOre(5, new ItemStack(zhOres,1,2));
 				}
 				if(enableMud.getBoolean(true))
 				{
 					FarmingRegistry.registerSludgeDrop(10, new ItemStack(zhMud,1));
 				}
 			}
 		}
 		catch (Exception e)
 		{
 			
 		}
 		
 		//Thermal Expansion integration
 		try
 		{
 			if(enableBetterWheat.getBoolean(true))
 				CraftingManagers.pulverizerManager.addRecipe(40, new ItemStack(Item.wheat), new ItemStack(flour,2,0), new ItemStack(flour,1,0),25,false);
 			
 			if(enableOres.getBoolean(true))
 			{
 				CraftingManagers.pulverizerManager.addRecipe(40, new ItemStack(zhOres,1,1), new ItemStack(sapphireGem));
 				CraftingManagers.pulverizerManager.addRecipe(40, new ItemStack(zhOres,1,2), new ItemStack(salt,5));
 			}
 			
 			if(replaceCoal.getBoolean(true))
 			{
 				CraftingManagers.pulverizerManager.addRecipe(60, new ItemStack(Block.oreCoal), new ItemStack(Item.coal,3,1));
 			}
 			
 			//TODO: sawmill recipes
 			//if(enableTradePost.getBoolean(true))
 				//CraftingManagers.sawmillManager.addRecipe(240, input, primaryOutput, secondaryOutput)
 		}
 		catch (Exception e)
 		{
 			
 		}
 		
 		//Some modifiers based on config
 		modifyVanilla();
 		
 		//Add new recipes
 		addRecipes();
 		
 		//Add some additional seeds to drop from grass
 		addGrassSeeds();
 		//Add some additional plants to grow when bonemeal is used on grass
 		addGrassPlants();
 
 		//Add some more items to the various chest loots
 		addChestLoot();
 		
 		//Register my items to the ore dictionary as needed
 		addOreDictionary();
 			
 		//Add World Generation
 		GameRegistry.registerWorldGenerator(zhWorldGen);
 				
 		//Something that's required, I imagine?
 		proxy.registerRenderers();
     }
 
     private void modifyVanilla() 
     {
     	if(replaceCoal.getBoolean(true))
 		{
     		int id = Block.oreCoal.blockID;
     		
 			Block.blocksList[id] = null;
 			
 			Block.blocksList[id] = new ZHCoalOre(id).setHardness(3.0F).setResistance(5.0F).setStepSound(Block.soundStoneFootstep).setUnlocalizedName("oreCoal");
 		}
     	
     	if(replaceWeb.getBoolean(true))
     	{
     		int id = Block.web.blockID;
     		
     		Block.blocksList[id] = null;
     		
     		Block.blocksList[id] = new ZHWebBlock(id).setLightOpacity(1).setHardness(4.0F).setUnlocalizedName("web");
     	}
     	
     	if(replaceCactus.getBoolean(true))
     	{
     		int id = Block.cactus.blockID;
     		
     		Block.blocksList[id] = null;
     		
     		Block.blocksList[id] = new ZHCactus(id).setHardness(0.4F).setStepSound(Block.soundClothFootstep).setUnlocalizedName("cactus");
     	}
     	
     	if(replaceClayBlock.getBoolean(true))
     	{
     		int id = Block.blockClay.blockID;
     		
 			Block.blocksList[id] = null;
 			
 			Block.blocksList[id] = new ZHClayBlock(id).setHardness(0.6F).setStepSound(Block.soundGravelFootstep).setUnlocalizedName("clay");
     		
     	}
 		
 		//Lets potions stack up to 3
     	if(potionsStack.getBoolean(true))
     		Item.potion.setMaxStackSize(maxPotionStack.getInt(3));
     	
     	if(durableShovels.getBoolean(true))
     	{
     		Item.shovelDiamond.setMaxDamage(Item.shovelDiamond.getMaxDamage() * 2);
     		Item.shovelGold.setMaxDamage(Item.shovelGold.getMaxDamage() * 2);
     		Item.shovelIron.setMaxDamage(Item.shovelIron.getMaxDamage() * 2);
     		Item.shovelStone.setMaxDamage(Item.shovelStone.getMaxDamage() * 2);
     		Item.shovelWood.setMaxDamage(Item.shovelWood.getMaxDamage() * 2);
     		
     		if(enableOres.getBoolean(true))
     			sapphireShovel.setMaxDamage(sapphireShovel.getMaxDamage() * 2);
     		if(cactusTools.getBoolean(true))
     			cactusShovel.setMaxDamage(cactusShovel.getMaxDamage() * 2);
     		if(flintTools.getBoolean(true))
     			flintShovel.setMaxDamage(flintShovel.getMaxDamage() * 2);
     	}
     	
     	if(durableAxes.getBoolean(true))
     	{
     		Item.axeDiamond.setMaxDamage(Item.axeDiamond.getMaxDamage() * 2);
     		Item.axeGold.setMaxDamage(Item.shovelGold.getMaxDamage() * 2);
     		Item.axeIron.setMaxDamage(Item.axeIron.getMaxDamage() * 2);
     		Item.axeStone.setMaxDamage(Item.axeStone.getMaxDamage() * 2);
     		Item.axeWood.setMaxDamage(Item.axeWood.getMaxDamage() * 2);
     		
     		if(enableOres.getBoolean(true))
     			sapphireAxe.setMaxDamage(sapphireAxe.getMaxDamage() * 2);
     		if(cactusTools.getBoolean(true))
     			cactusAxe.setMaxDamage(cactusAxe.getMaxDamage() * 2);
     		if(flintTools.getBoolean(true))
     			flintAxe.setMaxDamage(flintAxe.getMaxDamage() * 2);
     	}
 		
 	}
 
 	private void addChestLoot() 
     {
 		if(betterStarterLoot.getBoolean(true))
 		{
 			//Adds more wood the bonus chest...
 			ChestGenHooks.getInfo(ChestGenHooks.BONUS_CHEST).removeItem(new ItemStack(Block.wood));
 			ChestGenHooks.getInfo(ChestGenHooks.BONUS_CHEST).addItem(new WeightedRandomChestContent(new ItemStack(Block.wood),6,11,15));
 			
 			//Replaces planks with string
 			ChestGenHooks.getInfo(ChestGenHooks.BONUS_CHEST).removeItem(new ItemStack(Block.planks));
 			ChestGenHooks.getInfo(ChestGenHooks.BONUS_CHEST).addItem(new WeightedRandomChestContent(new ItemStack(Item.silk),12,23,5));
 			
 			//Adds some saplings to the bonus chest...
 			ChestGenHooks.getInfo(ChestGenHooks.BONUS_CHEST).addItem(new WeightedRandomChestContent(new ItemStack(Block.sapling),1,3,15));
 			
 			//Adds some wheat seeds to the bonus chest...
 			ChestGenHooks.getInfo(ChestGenHooks.BONUS_CHEST).addItem(new WeightedRandomChestContent(new ItemStack(Item.seeds),3,6,15));
 			
 			if(flintTools.getBoolean(true))
 			{
 				//add some flint tools to the starter chest instead of stone tools...
 				ChestGenHooks.getInfo(ChestGenHooks.BONUS_CHEST).removeItem(new ItemStack(Item.axeStone));
 				ChestGenHooks.getInfo(ChestGenHooks.BONUS_CHEST).removeItem(new ItemStack(Item.pickaxeStone));
 				ChestGenHooks.getInfo(ChestGenHooks.BONUS_CHEST).addItem(new WeightedRandomChestContent(new ItemStack(flintAxe),1,1,3));
 				ChestGenHooks.getInfo(ChestGenHooks.BONUS_CHEST).addItem(new WeightedRandomChestContent(new ItemStack(flintPick),1,1,3));
 			}
 			
 			if(cactusTools.getBoolean(true))
 			{
 				//add some cactus tools to the starter chest instead of wooden tools...
 				ChestGenHooks.getInfo(ChestGenHooks.BONUS_CHEST).removeItem(new ItemStack(Item.axeWood));
 				ChestGenHooks.getInfo(ChestGenHooks.BONUS_CHEST).removeItem(new ItemStack(Item.pickaxeWood));
 				ChestGenHooks.getInfo(ChestGenHooks.BONUS_CHEST).addItem(new WeightedRandomChestContent(new ItemStack(cactusAxe),1,1,5));
 				ChestGenHooks.getInfo(ChestGenHooks.BONUS_CHEST).addItem(new WeightedRandomChestContent(new ItemStack(cactusPick),1,1,5));
 			}
 		}
 		
 		if(enableOres.getBoolean(true))
 		{
 			//add some sapphires to all loot chests
 			ChestGenHooks.getInfo(ChestGenHooks.DUNGEON_CHEST).addItem(new WeightedRandomChestContent(new ItemStack(sapphireGem),0,3,5));
 			ChestGenHooks.getInfo(ChestGenHooks.MINESHAFT_CORRIDOR).addItem(new WeightedRandomChestContent(new ItemStack(sapphireGem),0,3,5));
 			ChestGenHooks.getInfo(ChestGenHooks.PYRAMID_DESERT_CHEST).addItem(new WeightedRandomChestContent(new ItemStack(sapphireGem),0,3,5));
 			ChestGenHooks.getInfo(ChestGenHooks.PYRAMID_JUNGLE_CHEST).addItem(new WeightedRandomChestContent(new ItemStack(sapphireGem),0,3,5));
 			ChestGenHooks.getInfo(ChestGenHooks.STRONGHOLD_CORRIDOR).addItem(new WeightedRandomChestContent(new ItemStack(sapphireGem),0,3,5));
 			ChestGenHooks.getInfo(ChestGenHooks.STRONGHOLD_CROSSING).addItem(new WeightedRandomChestContent(new ItemStack(sapphireGem),0,3,5));
 			ChestGenHooks.getInfo(ChestGenHooks.VILLAGE_BLACKSMITH).addItem(new WeightedRandomChestContent(new ItemStack(sapphireGem),0,3,5));
 		}
 		
 		if(enableMoreEmeraldLoot.getBoolean(true))
 		{
 			//add some (more?) emeralds to all loot chests
 			ChestGenHooks.getInfo(ChestGenHooks.DUNGEON_CHEST).addItem(new WeightedRandomChestContent(new ItemStack(Item.emerald,3),3,5,10));
 			ChestGenHooks.getInfo(ChestGenHooks.MINESHAFT_CORRIDOR).addItem(new WeightedRandomChestContent(new ItemStack(Item.emerald,3),3,5,10));
 			ChestGenHooks.getInfo(ChestGenHooks.PYRAMID_DESERT_CHEST).addItem(new WeightedRandomChestContent(new ItemStack(Item.emerald,3),3,5,10));
 			ChestGenHooks.getInfo(ChestGenHooks.PYRAMID_JUNGLE_CHEST).addItem(new WeightedRandomChestContent(new ItemStack(Item.emerald,3),3,5,10));
 			ChestGenHooks.getInfo(ChestGenHooks.STRONGHOLD_CORRIDOR).addItem(new WeightedRandomChestContent(new ItemStack(Item.emerald,3),3,5,10));
 			ChestGenHooks.getInfo(ChestGenHooks.STRONGHOLD_CROSSING).addItem(new WeightedRandomChestContent(new ItemStack(Item.emerald,3),3,5,10));
 			ChestGenHooks.getInfo(ChestGenHooks.VILLAGE_BLACKSMITH).addItem(new WeightedRandomChestContent(new ItemStack(Item.emerald,3),3,5,10));
 		}
 		
 		if(enableNewTools.getBoolean(true))
 		{
 			//add athames to some loot chests
 			ChestGenHooks.getInfo(ChestGenHooks.DUNGEON_CHEST).addItem(new WeightedRandomChestContent(new ItemStack(swordAthame),1,1,7));
 			ChestGenHooks.getInfo(ChestGenHooks.PYRAMID_DESERT_CHEST).addItem(new WeightedRandomChestContent(new ItemStack(swordAthame),1,1,7));
 			ChestGenHooks.getInfo(ChestGenHooks.PYRAMID_JUNGLE_CHEST).addItem(new WeightedRandomChestContent(new ItemStack(swordAthame),1,1,7));
 		}
 		
 		if(generateNiceSurprises.getBoolean(true))
 		{
 			ChestGenHooks.getInfo(ChestGenHooks.PYRAMID_JUNGLE_DISPENSER).addItem(new WeightedRandomChestContent(new ItemStack(Item.fireballCharge,3),5,10,25));
 			//TODO: Rockets?
 		}
 		
 		if(removeFoodLoot.getBoolean(true))
 		{
 			//Mineshafts
 			ChestGenHooks.getInfo(ChestGenHooks.MINESHAFT_CORRIDOR).removeItem(new ItemStack(Item.melonSeeds));
 			ChestGenHooks.getInfo(ChestGenHooks.MINESHAFT_CORRIDOR).removeItem(new ItemStack(Item.pumpkinSeeds));
 			ChestGenHooks.getInfo(ChestGenHooks.MINESHAFT_CORRIDOR).removeItem(new ItemStack(Item.bread));
 			
 			//Blacksmith
 			ChestGenHooks.getInfo(ChestGenHooks.VILLAGE_BLACKSMITH).removeItem(new ItemStack(Item.appleRed));
 			ChestGenHooks.getInfo(ChestGenHooks.VILLAGE_BLACKSMITH).removeItem(new ItemStack(Item.bread));
 			
 			//Dungeons
 			ChestGenHooks.getInfo(ChestGenHooks.DUNGEON_CHEST).removeItem(new ItemStack(Item.wheat));
 			ChestGenHooks.getInfo(ChestGenHooks.DUNGEON_CHEST).removeItem(new ItemStack(Item.dyePowder,1,3));
 			ChestGenHooks.getInfo(ChestGenHooks.DUNGEON_CHEST).removeItem(new ItemStack(Item.bread));
 			
 			//Strongholds
 			ChestGenHooks.getInfo(ChestGenHooks.STRONGHOLD_CORRIDOR).removeItem(new ItemStack(Item.appleRed));
 			ChestGenHooks.getInfo(ChestGenHooks.STRONGHOLD_CROSSING).removeItem(new ItemStack(Item.appleRed));
 			ChestGenHooks.getInfo(ChestGenHooks.STRONGHOLD_LIBRARY).removeItem(new ItemStack(Item.appleRed));
 		}
 	}
 
 	private void addGrassPlants() 
     {
 		if(enableFlax.getBoolean(true))
 			MinecraftForge.addGrassPlant(flaxCrop, 7, 8);
 	}
 
 	private void createItems() 
     {
 		//New foods
 		if(enableMoreFoodFromAnimals.getBoolean(true))
 		{
 			ribsRaw = new ZHRibsItem(config.getItem("rawRibs", itemID).getInt(),3,0.3f,true).setMaxStackSize(64).setCreativeTab(ZHCreativeTab.tab).setUnlocalizedName("ribsRaw");
 			ribsCooked = new ZHRibsItem(config.getItem("cookedRibs", itemID + 1).getInt(),8,0.8f,true).setMaxStackSize(64).setCreativeTab(ZHCreativeTab.tab).setUnlocalizedName("ribsCooked");
 			drumstickRaw = new ZHRibsItem(config.getItem("rawDrumstick",itemID + 2).getInt(),1,0.2f,true).setPotionEffect(Potion.hunger.id, 30, 0, 0.3F).setMaxStackSize(64).setCreativeTab(ZHCreativeTab.tab).setUnlocalizedName("drumstickRaw");
 			drumstickCooked = new ZHRibsItem(config.getItem("cookedDrumstick",itemID + 3).getInt(),4,0.6f,true).setMaxStackSize(64).setCreativeTab(ZHCreativeTab.tab).setUnlocalizedName("drumstickCooked");
 			muttonRaw = new ZHFood(config.getItem("rawMutton", itemID + 4).getInt(),3,0.3f,true).setMaxStackSize(64).setCreativeTab(ZHCreativeTab.tab).setUnlocalizedName("muttonRaw");
 			muttonCooked = new ZHFood(config.getItem("cookedMutton", itemID + 5).getInt(),8,0.8f,true).setMaxStackSize(64).setCreativeTab(ZHCreativeTab.tab).setUnlocalizedName("muttonCooked");
 			cookedEgg = new ZHFood(config.getItem("cookedEgg", itemID + 29).getInt(),6,0.6f,false).setCreativeTab(ZHCreativeTab.tab).setUnlocalizedName("eggCooked");
 		}
 		
 		//New Dyes
 		if(enableFlax.getBoolean(true))
 			zhDyes = new ZHDye(config.getItem("dyes", itemID + 6).getInt(),0).setMaxStackSize(64).setCreativeTab(ZHCreativeTab.tab).setUnlocalizedName("zhDye");
 		
 		//New items
 		if(enableOres.getBoolean(true))
 		{
 			//tar = new ZHItem(config.getItem("tar", itemID + 7).getInt()).setUnlocalizedName("tar").setCreativeTab(CreativeTabs.tabMaterials);
 			sapphireGem = (ZHItem) new ZHItem(config.getItem("sapphire", itemID + 8).getInt()).setUnlocalizedName("gemSapphire").setCreativeTab(ZHCreativeTab.tab);
 		}
 		
 		//Salt is needed in a few recipes even if ores aren't enabled...
 		if(enableNewFoods.getBoolean(true) || enableOres.getBoolean(true) || enableBetterWheat.getBoolean(true))
 			salt = new ZHItem(config.getItem("salt", itemID + 9).getInt()).setUnlocalizedName("salt").setCreativeTab(ZHCreativeTab.tab);
 		
 		//New tools
 		if(enableOres.getBoolean(true))
 		{
 			sapphirePick = new ZHPickaxe(config.getItem("sapphirePick", itemID + 10).getInt(), sapphireMaterial).setUnlocalizedName("pickaxeSapphire").setCreativeTab(ZHCreativeTab.tab);
 			sapphireAxe = new ZHAxe(config.getItem("sapphireHatchet", itemID + 11).getInt(), sapphireMaterial).setUnlocalizedName("hatchetSapphire").setCreativeTab(ZHCreativeTab.tab);
 			sapphireHoe = new ZHHoe(config.getItem("sapphireHoe", itemID + 12).getInt(), sapphireMaterial).setUnlocalizedName("hoeSapphire").setCreativeTab(ZHCreativeTab.tab);
 			sapphireShovel = new ZHShovel(config.getItem("sapphireShovel", itemID + 13).getInt(), sapphireMaterial).setUnlocalizedName("shovelSapphire").setCreativeTab(ZHCreativeTab.tab);
 			sapphireSword = new ZHSword(config.getItem("sapphireSword", itemID + 14).getInt(), sapphireMaterial).setUnlocalizedName("swordSapphire").setCreativeTab(ZHCreativeTab.tab);
 		}
 		
 		//flaxSeeds = itemID + 15
 		//cactusFruit = itemID + 16
 		
 		if(flintTools.getBoolean(true))
 		{
 			flintPick = new ZHPickaxe(config.getItem("flintPick", itemID + 17).getInt(), flintMaterial).setUnlocalizedName("pickaxeFlint").setCreativeTab(ZHCreativeTab.tab);
 			flintAxe = new ZHAxe(config.getItem("flintHatchet", itemID + 18).getInt(), flintMaterial).setUnlocalizedName("hatchetFlint").setCreativeTab(ZHCreativeTab.tab);
 			flintHoe = new ZHHoe(config.getItem("flintHoe", itemID + 19).getInt(), flintMaterial).setUnlocalizedName("hoeFlint").setCreativeTab(ZHCreativeTab.tab);
 			flintShovel = new ZHShovel(config.getItem("flintShovel", itemID + 20).getInt(), flintMaterial).setUnlocalizedName("shovelFlint").setCreativeTab(ZHCreativeTab.tab);
 			flintSword = new ZHSword(config.getItem("flintSword", itemID + 21).getInt(), flintMaterial).setUnlocalizedName("swordFlint").setCreativeTab(ZHCreativeTab.tab);	
 		}
 		
 		if(cactusTools.getBoolean(true))
 		{
 			cactusPick = new ZHPickaxe(config.getItem("cactusPick", itemID + 22).getInt(), cactusMaterial).setUnlocalizedName("pickaxeCactus").setCreativeTab(ZHCreativeTab.tab);
 			cactusAxe = new ZHAxe(config.getItem("cactusHatchet", itemID + 23).getInt(), cactusMaterial).setUnlocalizedName("hatchetCactus").setCreativeTab(ZHCreativeTab.tab);
 			cactusHoe = new ZHHoe(config.getItem("cactusHoe", itemID + 24).getInt(), cactusMaterial).setUnlocalizedName("hoeCactus").setCreativeTab(ZHCreativeTab.tab);
 			cactusShovel = new ZHShovel(config.getItem("cactusShovel", itemID + 25).getInt(), cactusMaterial).setUnlocalizedName("shovelCactus").setCreativeTab(ZHCreativeTab.tab);
 			cactusSword = new ZHSword(config.getItem("cactusSword", itemID + 26).getInt(), cactusMaterial).setUnlocalizedName("swordCactus").setCreativeTab(ZHCreativeTab.tab);
 		}
 		
 		if(enableNewTools.getBoolean(true))
 			swordAthame = new ZHAthame(config.getItem("athame", itemID + 27).getInt()).setUnlocalizedName("swordAthame").setCreativeTab(ZHCreativeTab.tab);
 		//meatSeeds = itemID + 28
 		
 		//more new foods
 		if(enableNewFoods.getBoolean(true))
 		{
 			zombieJerky = new ZHFood(config.getItem("zombieJerky", itemID + 30).getInt(),4,0.1f,true).setCreativeTab(ZHCreativeTab.tab).setUnlocalizedName("jerkyZombie");
 			jerky = new ZHFood(config.getItem("jerky", itemID + 31).getInt(),4,0.4f,true).setAlwaysEdible().setPotionEffect(11, 3, 1, 1.0f).setCreativeTab(ZHCreativeTab.tab).setUnlocalizedName("jerky");
 		}
 		
 		if(enableNewTools.getBoolean(true))
 		{
 			scytheWood = new ZHScythe(config.getItem("scytheWood", itemID + 32).getInt(), scytheRadius.getInt(),EnumToolMaterial.WOOD).setUnlocalizedName("scytheWood").setCreativeTab(ZHCreativeTab.tab);
 			scytheStone = new ZHScythe(config.getItem("scytheStone", itemID + 33).getInt(), scytheRadius.getInt(),EnumToolMaterial.STONE).setUnlocalizedName("scytheStone").setCreativeTab(ZHCreativeTab.tab);
 			scytheGold = new ZHScythe(config.getItem("scytheGold", itemID + 34).getInt(), scytheRadius.getInt(),EnumToolMaterial.GOLD).setUnlocalizedName("scytheGold").setCreativeTab(ZHCreativeTab.tab);
 			scytheIron = new ZHScythe(config.getItem("scytheIron", itemID + 35).getInt(), scytheRadius.getInt(),EnumToolMaterial.IRON).setUnlocalizedName("scytheIron").setCreativeTab(ZHCreativeTab.tab);
 			scytheDiamond = new ZHScythe(config.getItem("scytheDiamond", itemID + 36).getInt(), scytheRadius.getInt(),EnumToolMaterial.EMERALD).setUnlocalizedName("scytheDiamond").setCreativeTab(ZHCreativeTab.tab);
 			
 			if(cactusTools.getBoolean(true))
 				scytheCactus = new ZHScythe(config.getItem("scytheCactus", itemID + 37).getInt(), scytheRadius.getInt(),cactusMaterial).setUnlocalizedName("scytheCactus").setCreativeTab(ZHCreativeTab.tab);
 			if(flintTools.getBoolean(true))
 				scytheFlint = new ZHScythe(config.getItem("scytheFlint", itemID + 38).getInt(), scytheRadius.getInt(),flintMaterial).setUnlocalizedName("scytheFlint").setCreativeTab(ZHCreativeTab.tab);
 			if(enableOres.getBoolean(true))
 				scytheSapphire = new ZHScythe(config.getItem("scytheSapphire", itemID + 39).getInt(), scytheRadius.getInt(),sapphireMaterial).setUnlocalizedName("scytheSapphire").setCreativeTab(ZHCreativeTab.tab);
 		}
 
 		if(enableBetterWheat.getBoolean(true))
 		{
 			flour = new ZHMultiItem(config.getItem("flour", itemID + 40).getInt(), new String[] {"flour", "dough"}).setUnlocalizedName("ingredients").setCreativeTab(ZHCreativeTab.tab);
 			//dough is subtype...
 		}
 		
 		//next id = itemID + 41
 	}
 	
 	private void createBlocks()
 	{
 		//New ores
 		if(enableOres.getBoolean(true))
 			zhOres = new ZHMultiOreBlock(config.getBlock("ores", blockID).getInt(),Material.rock).setCreativeTab(ZHCreativeTab.tab);
 
 		//Flax plant
 		if(enableFlax.getBoolean(true))
 		{
 			flaxCrop = new ZHFlaxCrop(config.getBlock("flaxCrop", blockID+1).getInt());
 			flaxSeeds = (ZHSeeds) new ZHSeeds(config.getItem("flaxSeeds",itemID + 15).getInt(),flaxCrop.blockID,new int[] {Block.dirt.blockID, Block.grass.blockID, Block.tilledField.blockID},EnumPlantType.Plains).setUnlocalizedName("flaxSeeds").setCreativeTab(ZHCreativeTab.tab);
 		}
 		
 		//Cactus Fruit
 		if(enableCactusFruit.getBoolean(true))
 		{
 			cactusFruit = new ZHFood(config.getItem("cactusFruit", itemID + 16).getInt(), 3, 0.3f, false).setUnlocalizedName("cactusFruit").setCreativeTab(ZHCreativeTab.tab);
 			cactusFruitBlock = new ZHCactusFruit(config.getBlock("cactusFruitPod",blockID+2).getInt()).setUnlocalizedName("cactusFruitBlock");
 			zhCactus = new ZHCactusFlower(config.getBlock("fruitingCactus",blockID+3).getInt()).setHardness(0.4F).setStepSound(Block.soundClothFootstep).setUnlocalizedName("zhCactusFlower");
 		}
 		
 		if(enableMud.getBoolean(true))
 			zhMud = new ZHMud(config.getBlock("mud", blockID + 4).getInt()).setUnlocalizedName("mud").setCreativeTab(ZHCreativeTab.tab);
 		
 		if(enableMeatBush.getBoolean(true))
 		{
 			meatCrop = new ZHMeatCrop(config.getBlock("meatCrop", blockID + 5).getInt());
 			meatSeeds = (ZHSeeds) new ZHSeeds(config.getItem("meatSeeds", itemID + 28).getInt(),meatCrop.blockID,new int[] {Block.netherrack.blockID,Block.slowSand.blockID},EnumPlantType.Nether).setUnlocalizedName("meatSeeds").setCreativeTab(ZHCreativeTab.tab);
 		}
 		
 		if(enableOres.getBoolean(true))
 			sapphireBlock = new ZHBlock(config.getBlock("sapphireBlock", blockID + 6).getInt(), Material.iron).setCreativeTab(CreativeTabs.tabBlock).setHardness(5.0F).setResistance(10.0F).setStepSound(Block.soundMetalFootstep).setUnlocalizedName("sapphireBlock").setCreativeTab(ZHCreativeTab.tab);
 		
 		//next block id = blockID + 7
 		
 		if(enableTradePost.getBoolean(true))
 		{
 			tradeCenter = new ZHTradePost(config.getBlock("tradePost",blockID + 7).getInt()).setCreativeTab(CreativeTabs.tabBlock).setHardness(1.0f).setResistance(10.0f).setStepSound(Block.soundWoodFootstep).setUnlocalizedName("tradePost").setCreativeTab(ZHCreativeTab.tab);
 		}
 		//Treeshroom
 		//treeShroom = new ZHTreeShroom(config.getBlock("treeShroom",blockID+3).getInt()).setUnlocalizedName("treeShroom");
 	}
 
 	private void registerBlocks() 
     {
 		//Register blocks
 		if(enableOres.getBoolean(true))
 		{
 			GameRegistry.registerBlock(zhOres, ZHMultiOreItemBlock.class, "zhMultiOres");
 			for(int i = 0; i < numOres; i++)
 				MinecraftForge.setBlockHarvestLevel(zhOres, i, "pickaxe", harvestLevel[i]);
 		}
 		if(enableFlax.getBoolean(true))
 			GameRegistry.registerBlock(flaxCrop, "zhFlaxCrop");
 		
 		if(enableCactusFruit.getBoolean(true))
 		{
 			GameRegistry.registerBlock(cactusFruitBlock,"zhCactusFruit");
 			GameRegistry.registerBlock(zhCactus,"zhCactus");
 		}
 		if(enableMud.getBoolean(true))
 		{
 			GameRegistry.registerBlock(zhMud,"zhMud");
 			MinecraftForge.setBlockHarvestLevel(zhMud,"shovel",0);
 		}
 		
 		if(enableOres.getBoolean(true))
 		{
 			GameRegistry.registerBlock(sapphireBlock,"zhSapphireBlock");
 			MinecraftForge.setBlockHarvestLevel(sapphireBlock, "pickaxe", 2);
 		}
 		
 		if(enableTradePost.getBoolean(true))
 		{
 			GameRegistry.registerBlock(tradeCenter,"zhTradeCenter");
 			GameRegistry.registerTileEntity(ZHTradePostEntity.class,"zhTradeCenter");
 			MinecraftForge.setBlockHarvestLevel(tradeCenter, "axe", 0);
 		}
 		
 		//GameRegistry.registerBlock(treeShroom,"zhTreeShroom");
 	}
 
 	private void addGrassSeeds() 
     {
 		//Adds more vanilla seeds to Grass drops
 		if(moreSeeds.getBoolean(true))
 		{
 			MinecraftForge.addGrassSeed(new ItemStack(Item.melonSeeds),8);
 			MinecraftForge.addGrassSeed(new ItemStack(Item.pumpkinSeeds),8);
 			MinecraftForge.addGrassSeed(new ItemStack(Item.carrot),6);
 			MinecraftForge.addGrassSeed(new ItemStack(Item.potato),6);
 			MinecraftForge.addGrassSeed(new ItemStack(Item.poisonousPotato),2);
 		}
 		//Adds my crops to Grass drops
 		if(enableFlax.getBoolean(true))
 			MinecraftForge.addGrassSeed(new ItemStack(flaxSeeds),8);
 	}
 	private void addOreDictionary() 
     {
 		//Register items/blocks to OreDictionary as needed
 		if(enableFlax.getBoolean(true))
 		{
 			OreDictionary.registerOre("dyeBlack", new ItemStack(zhDyes,1,0));
 			OreDictionary.registerOre("dyeBlue", new ItemStack(zhDyes,1,1));
 			OreDictionary.registerOre("dyeBrown", new ItemStack(zhDyes,1,2));
 			OreDictionary.registerOre("dyeGreen", new ItemStack(zhDyes,1,3));
 			OreDictionary.registerOre("dyeWhite", new ItemStack(zhDyes,1,4));
 		}
 
 		if(enableOres.getBoolean(true))
 		{
 			OreDictionary.registerOre("oreSlime", new ItemStack(zhOres,1,0));
 			OreDictionary.registerOre("oreSapphire", new ItemStack(zhOres,1,1));
 			OreDictionary.registerOre("oreSalt", new ItemStack(zhOres,1,2));
 			
 			OreDictionary.registerOre("gemSapphire", sapphireGem);
 			OreDictionary.registerOre("blockSapphire", sapphireBlock);
 			OreDictionary.registerOre("itemSalt", salt);
 		}
 		
 		if(enableMud.getBoolean(true))
 			OreDictionary.registerOre("blockMud", zhMud);
 		
 		//MEAT
 		if(enableNewFoods.getBoolean(true))
 		{
 			OreDictionary.registerOre("anyCookedMeat", Item.beefCooked);
 			OreDictionary.registerOre("anyCookedMeat", Item.porkCooked);
 			OreDictionary.registerOre("anyCookedMeat", Item.chickenCooked);
 			OreDictionary.registerOre("anyCookedMeat", Item.fishCooked);
 		
 			if(enableMoreFoodFromAnimals.getBoolean(true))
 			{
 				OreDictionary.registerOre("anyCookedMeat", muttonCooked);
 				OreDictionary.registerOre("anyCookedMeat", ribsCooked);
 			}
 		}
 	}
 
 	@PostInit
     public void postInit(FMLPostInitializationEvent event) 
     {
 		String defaultCropsWhitelist = "" + Block.crops.blockID + ":7," + Block.carrot.blockID + ":7," + Block.potato.blockID + ":7," + Block.netherStalk.blockID + ":3," + flaxCrop.blockID + ":7," + meatCrop.blockID + ":7";
 		//logger.warning("Default crops whitelist: " + defaultCropsWhitelist);
 		scytheCropsList = config.get("Misc", "Scythe Crops: ", defaultCropsWhitelist);
 		scytheCropsList.comment = "Comma seperated list of <blockID>:<min meta> of crops the scythe will harvest. 0 meta = always harvested";
 		
 		String defaultCropsBlacklist = Block.melonStem.blockID + "," + Block.pumpkinStem.blockID;
 		//logger.warning("Default crops blacklist: " + defaultCropsBlacklist);
 		scytheCropsBlackList = config.get("Misc", "Scythe Blacklist: ", defaultCropsBlacklist);
 		scytheCropsBlackList.comment = "Comma seperated list of blockID's the scythe will ignore when looking for plants to harvest";
 		
     	setCropsList();
 		
     	config.save();
     }
     
     public void addRecipes()
     {
 		//Create some references to some common Items used in below recipes
 		ItemStack cactus = new ItemStack(Block.cactus);
 		ItemStack leather = new ItemStack(Item.leather);
 		ItemStack ironIngot = new ItemStack(Item.ingotIron);
 		ItemStack bone = new ItemStack(Item.bone);
 		ItemStack stick = new ItemStack(Item.stick);
 		                   
 		if(enableConvenienceRecipes.getBoolean(true))
 		{
 			//Get two sticks from a sapling
 			//Using addRecipe since that seemed to be the only one that accepted any (not just 0) metadata!
 			GameRegistry.addRecipe(new ItemStack(Item.stick,3), new Object [] {
 				"x",
 				'x', Block.sapling
 			});
 			
 			//3x Gravel -> 2x Flint
 			GameRegistry.addShapelessRecipe(new ItemStack(Item.flint,2),new ItemStack(Block.gravel),new ItemStack(Block.gravel),new ItemStack(Block.gravel));
 			
 			//Get one leather by smelting rotten flesh, gives about as much experience as smelting stone
 			GameRegistry.addSmelting(Item.rottenFlesh.itemID, leather, 0.1f);
 		}
 		
 		if(enableTradePost.getBoolean(true))
 		{
 			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(tradeCenter), new Object[]{
 				"xxx",
 				"xyx",
 				"xxx",
				'x',"woodPlank",
 				'y', new ItemStack(Item.book)
 			}));
 		}
 		
 		if(enableNewTools.getBoolean(true))
 		{
 			GameRegistry.addRecipe(new ItemStack(scytheIron), new Object [] {
 				"xxy",
 				" y ",
 				"y  ",
 				'x', ironIngot,
 				'y', stick
 			});
 			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(scytheWood), new Object[]{
 				"xxy",
 				" y ",
 				"y  ",
				'x',"woodPlank",
 				'y',stick
 			}));
 			GameRegistry.addRecipe(new ItemStack(scytheStone), new Object [] {
 				"xxy",
 				" y ",
 				"y  ",
 				'x', new ItemStack(Block.cobblestone),
 				'y', stick
 			});
 			GameRegistry.addRecipe(new ItemStack(scytheGold), new Object [] {
 				"xxy",
 				" y ",
 				"y  ",
 				'x', new ItemStack(Item.ingotGold),
 				'y', stick
 			});
 			GameRegistry.addRecipe(new ItemStack(scytheDiamond), new Object [] {
 				"xxy",
 				" y ",
 				"y  ",
 				'x', new ItemStack(Item.diamond),
 				'y', stick
 			});
 			
 			if(cactusTools.getBoolean(true))
 			{
 				GameRegistry.addRecipe(new ItemStack(scytheCactus), new Object [] {
 					"xxy",
 					" y ",
 					"y  ",
 					'x', new ItemStack(Block.cactus),
 					'y', stick
 				});
 			}
 			
 			if(enableOres.getBoolean(true))
 			{
 				GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(scytheSapphire), new Object[]{
 					"xxy",
 					" y ",
 					"y  ",
 					'x',"gemSapphire",
 					'y',stick
 				}));
 			}
 			
 			if(flintTools.getBoolean(true))
 			{
 				GameRegistry.addRecipe(new ItemStack(scytheFlint), new Object [] {
 					"xxy",
 					" y ",
 					"y  ",
 					'x', new ItemStack(Item.flint),
 					'y', stick
 				});
 			}
 			
 			if(!OreDictionary.getOres("ingotSilver").isEmpty())
 			{
 				GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(swordAthame), new Object[]{
 					"x",
 					"y",
 					"z",
 					'x', "ingotSilver",
 					'y', new ItemStack(Item.emerald),
 					'z', new ItemStack(Item.ingotGold)
 				}));
 			}
 		}
 		
 		if(replaceClayBlock.getBoolean(true) || enableConvenienceRecipes.getBoolean(true))
 		{
 			GameRegistry.addShapelessRecipe(new ItemStack(Item.clay, 4), new ItemStack(Block.blockClay));
 		}
 		
 		//Cactus tools
 		if(cactusTools.getBoolean(true))
 		{
 			//Get 4 sticks from two cactuses on top of each other (ie: like sticks from planks)
 			GameRegistry.addShapedRecipe(new ItemStack(Item.stick,4),
 				"x",
 				"x",
 				'x',cactus
 			);
 			
 			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(cactusSword), new Object[]{
 				"x",
 				"x",
 				"y",
 				'x',cactus,
 				'y',stick
 			}));
 			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(cactusShovel), new Object[]{
 				"x",
 				"y",
 				"y",
 				'x',cactus,
 				'y',stick
 			}));
 			
 			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(cactusAxe), new Object[]{
 				"xx",
 				"xy",
 				" y",
 				'x',cactus,
 				'y',stick
 			}));
 			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(cactusHoe), new Object[]{
 				"xx",
 				"y ",
 				"y ",
 				'x',cactus,
 				'y',stick
 			}));
 			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(cactusAxe), new Object[]{
 				"xx",
 				"yx",
 				"y ",
 				'x',cactus,
 				'y',stick
 			}));
 			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(cactusHoe), new Object[]{
 				"xx",
 				" y",
 				" y",
 				'x',cactus,
 				'y',stick
 			}));
 			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(cactusPick), new Object[]{
 				"xxx",
 				" y ",
 				" y ",
 				'x',cactus,
 				'y',stick
 			}));
 		}
 		
 		//flint tools
 		if(flintTools.getBoolean(true))
 		{
 			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(flintSword), new Object[]{
 				"x",
 				"x",
 				"y",
 				'x',new ItemStack(Item.flint),
 				'y',stick
 			}));
 			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(flintShovel), new Object[]{
 				"x",
 				"y",
 				"y",
 				'x',new ItemStack(Item.flint),
 				'y',stick
 			}));
 			
 			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(flintAxe), new Object[]{
 				"xx",
 				"xy",
 				" y",
 				'x',new ItemStack(Item.flint),
 				'y',stick
 			}));
 			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(flintHoe), new Object[]{
 				"xx",
 				"y ",
 				"y ",
 				'x',new ItemStack(Item.flint),
 				'y',stick
 			}));
 			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(flintAxe), new Object[]{
 				"xx",
 				"yx",
 				"y ",
 				'x',new ItemStack(Item.flint),
 				'y',stick
 			}));
 			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(flintHoe), new Object[]{
 				"xx",
 				" y",
 				" y",
 				'x',new ItemStack(Item.flint),
 				'y',stick
 			}));
 			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(flintPick), new Object[]{
 				"xxx",
 				" y ",
 				" y ",
 				'x',new ItemStack(Item.flint),
 				'y',stick
 			}));
 		}
 		
 		//Saddle from 5 leather/3 iron (a la the horse saddle recipe that is/was in 1.6)
 		if(enableNewRecipes.getBoolean(true))
 		{
 			GameRegistry.addShapedRecipe(new ItemStack(Item.saddle),
 				"xxx",
 				"xyx",
 				"y y",
 				'x',leather,
 				'y',ironIngot
 			);
 			
 			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(Block.sponge), new Object [] {
 				"sys",
 				"bwb",
 				"sys",
 				's', Block.sand,
 				'y', "dyeYellow",
 				'b', "dyeBlack",
 				'w', Block.cloth
 			}));
 		}
 		    
 		if(enableOres.getBoolean(true))
 		{
 			//Sapphire tools
 			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(sapphireSword), new Object[]{
 				"x",
 				"x",
 				"y",
 				'x',"gemSapphire",
 				'y',stick
 			}));
 			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(sapphireShovel), new Object[]{
 				"x",
 				"y",
 				"y",
 				'x',"gemSapphire",
 				'y',stick
 			}));
 			
 			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(sapphireAxe), new Object[]{
 				"xx",
 				"xy",
 				" y",
 				'x',"gemSapphire",
 				'y',stick
 			}));
 			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(sapphireHoe), new Object[]{
 				"xx",
 				"y ",
 				"y ",
 				'x',"gemSapphire",
 				'y',stick
 			}));
 			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(sapphireAxe), new Object[]{
 				"xx",
 				"yx",
 				"y ",
 				'x',"gemSapphire",
 				'y',stick
 			}));
 			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(sapphireHoe), new Object[]{
 				"xx",
 				" y",
 				" y",
 				'x',"gemSapphire",
 				'y',stick
 			}));
 			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(sapphirePick), new Object[]{
 				"xxx",
 				" y ",
 				" y ",
 				'x',"gemSapphire",
 				'y',stick
 			}));
 			
 			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(sapphireBlock), new Object[]{
 				"xxx",
 				"xxx",
 				"xxx",
 				'x',"gemSapphire"
 			}));
 			
 			GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(sapphireGem, 9), new Object[] {
 				"blockSapphire"
 			}));
 		}
 
 		//Mud Recipes
 		if(enableMud.getBoolean(true))
 		{
 			GameRegistry.addShapelessRecipe(new ItemStack(zhMud,3),new ItemStack(Block.dirt),new ItemStack(Block.dirt),new ItemStack(Block.dirt),new ItemStack(Item.bucketWater));
 			GameRegistry.addShapelessRecipe(new ItemStack(zhMud,8),new ItemStack(Block.dirt),new ItemStack(Block.dirt),new ItemStack(Block.dirt),new ItemStack(Block.dirt),new ItemStack(Block.dirt),new ItemStack(Block.dirt), new ItemStack(Block.dirt), new ItemStack(Block.dirt), new ItemStack(Item.bucketWater));
 			GameRegistry.addShapelessRecipe(new ItemStack(zhMud,1),new ItemStack(Block.dirt),new ItemStack(Item.bucketWater));
 		}
 		
 
 		if(enableMoreFoodFromAnimals.getBoolean(true))
 		{
 			//New meats -> bone
 			GameRegistry.addShapelessRecipe(bone,new ItemStack(ribsRaw,1));
 			GameRegistry.addShapelessRecipe(bone,new ItemStack(drumstickRaw,1));
 			
 			//Cooking recipe for new meats
 			GameRegistry.addSmelting(ribsRaw.itemID,new ItemStack(ribsCooked),0.35f);
 			GameRegistry.addSmelting(muttonRaw.itemID,new ItemStack(muttonCooked),0.35f);
 			GameRegistry.addSmelting(drumstickRaw.itemID,new ItemStack(drumstickCooked),0.35f);
 			
 			//Cooking recipes for other new foods
 			GameRegistry.addSmelting(Item.egg.itemID, new ItemStack(cookedEgg), 0.35f);
 		}
 		
 		//Meat Seeds -> bone
 		if(enableMeatBush.getBoolean(true))
 			GameRegistry.addShapelessRecipe(bone, new ItemStack(meatSeeds,1));
 		
 		if(enableBetterWheat.getBoolean(true))
 		{
 			//Wheat -> Seeds
 			GameRegistry.addShapelessRecipe(new ItemStack(Item.seeds,3),new ItemStack(Item.wheat), new ItemStack(Item.wheat));
 			//Wheat -> Flour
 			GameRegistry.addShapelessRecipe(new ItemStack(flour,1,0), new ItemStack(Item.wheat));
 			//TODO: Add pulverizer recipe for extra flour
 			
 			//Flour + Salt + Water -> Dough
 			GameRegistry.addShapelessRecipe(new ItemStack(flour,1,1), new ItemStack(flour,1,0), new ItemStack(salt), new ItemStack(Item.bucketWater));
 			//Smelt Dough -> Bread
 			FurnaceRecipes.smelting().addSmelting(flour.itemID, 0, new ItemStack(Item.bread), 0.2f);
 			//Cookies using dough
 			GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(Item.cookie,8), new Object[] {
 				new ItemStack(Item.dyePowder,1,3),
 				new ItemStack(flour, 1, 1)
 			}));
 			
 			//Cake using flour
 			GameRegistry.addShapedRecipe(new ItemStack(Item.cake), new Object[] {
 				"xxx",
 				"yzy",
 				"aba",
 				'x', new ItemStack(Item.bucketMilk),
 				'y', new ItemStack(Item.sugar),
 				'a', new ItemStack(flour,1,0),
 				'b', new ItemStack(salt)
 			});
 			//Disables Normal Cookie/Bread/Cake recipes
 			removeRecipes(new ItemStack(Item.cookie,8));
 			removeRecipes(new ItemStack(Item.cake,1));
 		}
 		
 		//Uses for Tar
 		//GameRegistry.addShapelessRecipe(new ItemStack(Block.pistonStickyBase),new ItemStack(tar),new ItemStack(Block.pistonBase));
 		//GameRegistry.addShapelessRecipe(new ItemStack(Item.magmaCream), new ItemStack(tar), new ItemStack(Item.blazePowder));
 		
 
 		if(enableNewFoods.getBoolean(true))
 		{
 			GameRegistry.addShapelessRecipe(new ItemStack(zombieJerky,2), new ItemStack(Item.rottenFlesh), new ItemStack(salt));
 			GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(jerky,4), new Object[] {
 				"anyCookedMeat",
 				new ItemStack(Item.sugar),
 				new ItemStack(salt)
 			}));
 
 			if(enableMoreFoodFromAnimals.getBoolean(true))
 				GameRegistry.addShapelessRecipe(new ItemStack(jerky,1), new ItemStack(drumstickCooked),new ItemStack(Item.sugar), new ItemStack(salt));
 		}
 		
 		//Slimy Ore -> Slime ball
 		if(enableOres.getBoolean(true))
 			FurnaceRecipes.smelting().addSmelting(zhOres.blockID, 0, new ItemStack(Item.slimeBall,4,0), 0.7f);
 		
 		//Salt
 		if(enableNewFoods.getBoolean(true) || enableOres.getBoolean(true) || enableBetterWheat.getBoolean(true))
 		{
 			//Liquid Dictionary? Possible to add smelting instead?
 			GameRegistry.addShapelessRecipe(new ItemStack(salt,3), new ItemStack(Item.bucketWater));
 		}
     }
     
     //will remove ALL crafting (NOT SMELTING) recipes that result in the given ItemStack...
 	private static void removeRecipes(ItemStack resultItem) 
 	{
 		List<IRecipe> recipes = CraftingManager.getInstance().getRecipeList();
 		for(int i = 0; i < recipes.size(); i++)
 		{
 			IRecipe recipe = recipes.get(i);
 			if(recipe instanceof ShapedRecipes)
 			{
 				ItemStack recipeResult = ((ShapedRecipes)recipe).getRecipeOutput();
 				
 				if(ItemStack.areItemStacksEqual(resultItem, recipeResult))
 					recipes.remove(i--); //decrement i to make sure we do not miss the next recipe
 			}
 			else if(recipe instanceof ShapelessRecipes)
 			{
 				ItemStack recipeResult = ((ShapelessRecipes)recipe).getRecipeOutput();
 				
 				if(ItemStack.areItemStacksEqual(resultItem, recipeResult))
 					recipes.remove(i--); //decrement i to make sure we do not miss the next recipe
 			}
 		}
 	}
 	
 	//TODO: is this possible?
 	//private static void removeSmeltingRecipes(ItemStack resultItem) 
 
         
 }
