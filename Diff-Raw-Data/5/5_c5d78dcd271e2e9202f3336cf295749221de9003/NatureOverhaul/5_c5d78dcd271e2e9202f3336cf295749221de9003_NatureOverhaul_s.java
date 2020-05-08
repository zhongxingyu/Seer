 package natureoverhaul;
 
 import java.lang.reflect.Method;
 import java.util.*;
 
 import cpw.mods.fml.common.FMLCommonHandler;
 import cpw.mods.fml.common.eventhandler.SubscribeEvent;
 import cpw.mods.fml.common.gameevent.TickEvent;
 import cpw.mods.fml.common.registry.GameData;
 import natureoverhaul.behaviors.BehaviorFire;
 import natureoverhaul.handlers.*;
 import net.minecraft.block.*;
 import net.minecraft.init.Blocks;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemAxe;
 import net.minecraft.world.ChunkCoordIntPair;
 import net.minecraft.world.World;
 import net.minecraft.world.WorldServer;
 import net.minecraft.world.biome.BiomeGenBase;
 import net.minecraft.world.chunk.Chunk;
 import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
 import net.minecraftforge.common.config.Configuration;
 import net.minecraftforge.common.MinecraftForge;
 
 import com.google.common.collect.ImmutableMap;
 
 import cpw.mods.fml.common.Loader;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.EventHandler;
 import cpw.mods.fml.common.Mod.Instance;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPostInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 
 /**
  * From Clinton Alexander idea.
  * @author Olivier
  *
  */
 @Mod(modid = "natureoverhaul", name = "Nature Overhaul", version = "0.8", dependencies = "after:mod_MOAPI")
 public class NatureOverhaul {
 	private enum GrowthType {
 		NEITHER, LEAFGROWTH, LEAFDECAY, BOTH
 	}
 
 	@Instance("natureoverhaul")
 	public static NatureOverhaul instance;
 	private static boolean autoSapling = true, autoFarming = true, lumberjack = true, moddedBonemeal = true, killLeaves = true, biomeModifiedRate = true;
 	public static boolean useStarvingSystem = true, decayLeaves = true, mossCorruptStone = true;
 	private static boolean customDimension = true, wildAnimalsBreed = true;
 	private static int wildAnimalBreedRate = 0, wildAnimalDeathRate = 0;
 	public static int growthType = 0, fireRange = 2;
 	private int updateLCG = (new Random()).nextInt();
 	private static Map<Block, NOType> IDToTypeMapping = new HashMap<Block, NOType>();
 	private static Map<Block, Boolean> IDToGrowingMapping = new HashMap<Block, Boolean>(), IDToDyingMapping = new HashMap<Block, Boolean>();
 	private static Map<Block, Block> LogToLeafMapping = new HashMap<Block, Block>();
     private static Map<Block, Integer> IDToFireCatchMapping = new HashMap<Block, Integer>(), IDToFirePropagateMapping = new HashMap<Block, Integer>();
     private static Map<Block, Block> LeafToSaplingMapping = new HashMap<Block, Block>();
 	private static Map<Block, String[]> TreeIdToMeta = new HashMap<Block, String[]>();
 	private static String[] names = new String[] { "Sapling", "Tree", "Plants", "Netherwort", "Grass", "Reed", "Cactus", "Mushroom", "Mushroom Tree", "Leaf", "Crops", "Moss", "Cocoa", "Fire" };
 	private static boolean[] dieSets = new boolean[names.length], growSets = new boolean[names.length + 1];
 	private static float[] deathRates = new float[names.length], growthRates = new float[names.length + 1];
 	private static String[] optionsCategory = new String[names.length + 1];
 	static {
 		for (int i = 0; i < names.length; i++) {
 			optionsCategory[i] = names[i] + " Options";
 		}
 		optionsCategory[names.length] = "Misc Options";
 	}
 	private static Configuration config;
 	private static Class<?> api;
 	private static boolean API;
 	private static BonemealEventHandler bonemealEvent;
 	private static AnimalEventHandler animalEvent;
 	private static PlayerEventHandler lumberEvent;
 	private static AutoSaplingEventHandler autoEvent;
 	private static AutoFarmingEventHandler farmingEvent;
 	private static org.apache.logging.log4j.Logger logger;
 
 	@EventHandler
 	public void load(FMLInitializationEvent event) {
 		FMLCommonHandler.instance().bus().register(this);
 	}
 
     //Register blocks with config values and NOType, and log/leaf couples
 	@EventHandler
 	public void modsLoaded(FMLPostInitializationEvent event) {
 		if (Loader.isModLoaded("mod_MOAPI")) {//We can use reflection to load options in MOAPI
 			try {
 				api = Class.forName("moapi.ModOptionsAPI");
 				Method addMod = api.getMethod("addMod", String.class);
 				//"addMod" is static, we don't need an instance
 				Object option = addMod.invoke(null, "Nature Overhaul");
 				Class<?> optionClass = addMod.getReturnType();
 				//Set options as able to be used on a server,get the instance back
 				option = optionClass.getMethod("setServerMode").invoke(option);
 				//"addBooleanOption" and "addSliderOption" aren't static, we need options class and an instance
 				Method addBoolean = optionClass.getMethod("addBooleanOption", new Class[] { String.class, boolean.class });
 				Method addSlider = optionClass.getMethod("addSliderOption", new Class[] { String.class, int.class, int.class });
 				Method addMap = optionClass.getMethod("addMappedOption", new Class[] { String.class, String[].class, int[].class });
 				Method setSliderValue = Class.forName("moapi.ModOptionSlider").getMethod("setValue", int.class);
 				//To create a submenu
 				Method addSubOption = optionClass.getMethod("addSubOption", String.class);
 				//Create "General" submenu and options
 				Object subOption = addSubOption.invoke(option, "General");
 				Object slidOption;
 				for (int i = 0; i < names.length; i++) {
 					addBoolean.invoke(subOption, names[i] + " grow", true);
 					addBoolean.invoke(subOption, names[i] + " die", true);
 					slidOption = addSlider.invoke(subOption, names[i] + " growth rate", 0, 10000);
 					setSliderValue.invoke(slidOption, 1200);
 					slidOption = addSlider.invoke(subOption, names[i] + " death rate", 0, 10000);
 					setSliderValue.invoke(slidOption, 1200);
 				}
 				addBoolean.invoke(subOption, "Apple grows", true);
 				slidOption = addSlider.invoke(subOption, "Apple growth rate", 0, 10000);
 				setSliderValue.invoke(slidOption, 3000);
 				//Create "LumberJack" submenu and options
 				Object lumberJackOption = addSubOption.invoke(option, "LumberJack");
 				addBoolean.invoke(lumberJackOption, "Enable", true);
 				addBoolean.invoke(lumberJackOption, "Kill leaves", true);
 				//Create "Misc" submenu and options
 				Object miscOption = addSubOption.invoke(option, "Misc");
 				addMap.invoke(miscOption, "Sapling drops on", new String[] { "Both", "LeafDecay", "LeafGrowth", "Neither" }, new int[] { 3, 2, 1, 0 });
 				addBoolean.invoke(miscOption, "AutoSapling", true);
 				addBoolean.invoke(miscOption, "Plant seeds on player drop", true);
 				addBoolean.invoke(miscOption, "Leaves decay on tree death", true);
 				addBoolean.invoke(miscOption, "Moss growing on stone", true);
 				addBoolean.invoke(miscOption, "Starving system", true);
 				addBoolean.invoke(miscOption, "Biome specific rates", true);
 				addBoolean.invoke(miscOption, "Modded Bonemeal", true);
 				addBoolean.invoke(miscOption, "Custom dimensions", true);
 				//Create "Animals" submenu and options
 				Object animalsOption = addSubOption.invoke(option, "Animals");
 				addBoolean.invoke(animalsOption, "Wild breed", true);
 				slidOption = addSlider.invoke(animalsOption, "Breeding rate", 1, 10000);
 				setSliderValue.invoke(slidOption, 10000);
 				slidOption = addSlider.invoke(animalsOption, "Death rate", 1, 10000);
 				setSliderValue.invoke(slidOption, 10000);
 				//Create "Fire" submenu and options
 				Object fireOption = addSubOption.invoke(option, "Fire");
 				slidOption = addSlider.invoke(fireOption, "Propagation range", 0, 20);
 				setSliderValue.invoke(slidOption, 2);
 				//Loads and saves values
 				option = optionClass.getMethod("loadValues").invoke(option);
 				option = optionClass.getMethod("saveValues").invoke(option);
 				//We have saved the values, we can start to get them back
 				getMOAPIValues(optionClass, subOption, lumberJackOption, miscOption, animalsOption, fireOption);
 				//We successfully get all options !
 				API = true;
 				logger.info("NatureOverhaul found MOAPI and loaded all options correctly.");
 			} catch (SecurityException s) {
 				API = false;
 			} catch (ClassNotFoundException c) {
 				API = false;
 				logger.info("NatureOverhaul couldn't use MOAPI, continuing with values in config file.");
 			} catch (ReflectiveOperationException n) {
 				API = false;
 				logger.warn("NatureOverhaul failed to use MOAPI, please report to NO author:", n);
 			}//Even if it fails, we can still rely on settings stored in Forge recommended config file.
 		}
 		//Now we can register every available blocks at this point.
 		ArrayList<Block> logID = new ArrayList<Block>(), leafID = new ArrayList<Block>(), saplingID = new ArrayList<Block>();
 		//If a block is registered after, it won't be accounted for.
         Block i = null;
 		for (Iterator itr=GameData.blockRegistry.iterator();itr.hasNext(); i = (Block)itr.next()) {
 			if (i != null) {
 				if (i instanceof IGrowable && i instanceof IBlockDeath) {//Priority to Blocks using the API
 					addMapping(i, true, ((IGrowable) i).getGrowthRate(), true, ((IBlockDeath) i).getDeathRate(), -1.0F, -1.0F, NOType.CUSTOM);
 				} else if (i instanceof IGrowable) {
 					addMapping(i, true, ((IGrowable) i).getGrowthRate(), false, -1, -1.0F, -1.0F, NOType.CUSTOM);
 				} else if (i instanceof IBlockDeath) {
 					addMapping(i, false, -1, true, ((IBlockDeath) i).getDeathRate(), -1.0F, -1.0F, NOType.CUSTOM);
 				} else if (i instanceof BlockSapling) {
 					saplingID.add(i);
 				} else if (i instanceof BlockLog) {
 					logID.add(i);
 				} else if (i instanceof BlockNetherWart) {//In the Nether, we don't use biome dependent parameter
 					addMapping(i, growSets[3], growthRates[3], dieSets[3], deathRates[3], 0.0F, 0.0F, NOType.NETHERSTALK);
 				} else if (i instanceof BlockGrass || i instanceof BlockMycelium) {
 					addMapping(i, growSets[4], growthRates[4], dieSets[4], deathRates[4], 0.7F, 0.5F, NOType.GRASS);
 				} else if (i instanceof BlockReed) {
 					addMapping(i, growSets[5], growthRates[5], dieSets[5], deathRates[5], 0.8F, 0.8F, NOType.REED);
 				} else if (i instanceof BlockCactus) {
 					addMapping(i, growSets[6], growthRates[6], dieSets[6], deathRates[6], 1.5F, 0.2F, NOType.CACTUS);
 				} else if (i instanceof BlockMushroom) {
 					addMapping(i, growSets[7], growthRates[7], dieSets[7], deathRates[7], 0.9F, 1.0F, NOType.MUSHROOM);
 				} else if (i instanceof BlockHugeMushroom) {
 					addMapping(i, growSets[8], growthRates[8], dieSets[8], deathRates[8], 0.9F, 1.0F, NOType.MUSHROOMCAP);
 				} else if (i instanceof BlockLeaves) {
 					leafID.add(i);
 				} else if (i instanceof BlockCrops || i instanceof BlockStem) {
 					addMapping(i, growSets[10], growthRates[10], dieSets[10], deathRates[10], 1.0F, 1.0F, NOType.FERTILIZED);
 				} else if (i instanceof BlockFlower) {//Flower ,deadbush, lilypad, tallgrass
 					addMapping(i, growSets[2], growthRates[2], dieSets[2], deathRates[2], 0.6F, 0.7F, NOType.PLANT, 100, 60);
 				} else if (i == Blocks.mossy_cobblestone) {
 					addMapping(i, growSets[11], growthRates[11], dieSets[11], deathRates[11], 0.7F, 1.0F, NOType.MOSS);
 				} else if (i instanceof BlockCocoa) {
 					addMapping(i, growSets[12], growthRates[12], dieSets[12], deathRates[12], 1.0F, 1.0F, NOType.COCOA);
 				} else if (i instanceof BlockFire) {
 					addMapping(i, growSets[13], 0, dieSets[13], 0, 0.0F, 0.0F, NOType.CUSTOM);
 					BehaviorManager.setBehavior(i, new BehaviorFire().setData(growthRates[13], deathRates[13]));
 				}
 				if (i.func_149688_o().isOpaque() && i.func_149686_d() && !i.func_149744_f()) {
 					IDToFirePropagateMapping.put(
 							i,
 							config.get("Fire Options", i.func_149739_a().substring(5) + " chance to encourage fire",
 									IDToFirePropagateMapping.containsKey(i) ? IDToFirePropagateMapping.get(i) : 0).getInt());
 					IDToFireCatchMapping.put(
 							i,
 							config.get("Fire Options", i.func_149739_a().substring(5) + " chance to catch fire",
 									IDToFireCatchMapping.containsKey(i) ? IDToFireCatchMapping.get(i) : 0).getInt());
 				}
 			}
 		}
 		String option = "", sData = "(", gData = "(", fData = "(";
 		Set<Integer> sapData = new HashSet<Integer>(), logData = new HashSet<Integer>(), leafData = new HashSet<Integer>();
 		for (int index = 0; index < Math.min(Math.min(logID.size(), leafID.size()), saplingID.size()); index++) {
 			for (int meta = 0; meta < 16; meta++) {
 				sapData.add(saplingID.get(index).func_149692_a(meta));
 				logData.add(logID.get(index).func_149692_a(meta));
 				leafData.add(leafID.get(index).func_149692_a(meta));
 			}
 			for (int meta : sapData) {
 				sData = sData.concat(meta + ",");
 			}
 			for (int meta : logData) {
 				gData = gData.concat(meta + ",");
 			}
 			for (int meta : leafData) {
 				fData = fData.concat(meta + ",");
 			}
 			option = option.concat(GameData.blockRegistry.func_148750_c(saplingID.get(index)) + sData + ")-" + GameData.blockRegistry.func_148750_c(logID.get(index)) + gData + ")-" + GameData.blockRegistry.func_148750_c(leafID.get(index)) + fData + ");");
 		}
 		String[] ids = config.get(optionsCategory[names.length], "Sapling-Log-Leaves names", option, "Separate groups with ;").getString().split(";");
 		String[] temp;
 		for (String param : ids) {
 			if (param != null && !param.equals("")) {
 				temp = param.split("-");
 				if (temp.length == 3) {
 					Block idSaplin, idLo, idLef;
 					try {
 						idSaplin = GameData.blockRegistry.get(temp[0].split("\\(")[0]);
 						idLo = GameData.blockRegistry.get(temp[1].split("\\(")[0]);
 						idLef = GameData.blockRegistry.get(temp[2].split("\\(")[0]);
 					} catch (Exception e) {
 						continue;
 					}
 					//Make sure user input is valid
 					if (idSaplin!=null && idLo!=null && idLef!=null) {
 						addMapping(idSaplin, growSets[0], 0, dieSets[0], deathRates[0], 0.8F, 0.8F, NOType.SAPLING);
 						TreeIdToMeta.put(idSaplin, temp[0].split("\\(")[1].replace("\\)", "").trim().split("\\,"));
 						addMapping(idLo, growSets[1], growthRates[1], dieSets[1], deathRates[1], 1.0F, 1.0F, NOType.LOG, 5, 5);
 						TreeIdToMeta.put(idLo, temp[1].split("\\(")[1].replace("\\)", "").trim().split("\\,"));
 						addMapping(idLef, growSets[9], growthRates[9], dieSets[9], deathRates[9], 1.0F, 1.0F, NOType.LEAVES, 60, 10);
 						TreeIdToMeta.put(idLef, temp[2].split("\\(")[1].replace("\\)", "").trim().split("\\,"));
 						LogToLeafMapping.put(idLo, idLef);
 						LeafToSaplingMapping.put(idLef, idSaplin);
 					}
 				}
 			}
 		}
 		option = "";
         Item it = null;
 		for (Iterator itr = GameData.itemRegistry.iterator();itr.hasNext(); it=(Item)itr.next()) {
 			if (it instanceof ItemAxe) {
				option = option.concat(it + ",");
 			}
 		}
 		ids = config.get(optionsCategory[1], "Lumberjack compatible items", option, "Separate item names with comma").getString().split(",");
 		for (String param : ids) {
 			if (param != null && !param.equals("")) {
 				try {
 					PlayerEventHandler.ids.add(GameData.itemRegistry.get(param));
				} catch (NumberFormatException e) {
 					continue;
 				}
 			}
 		}
 		//Saving Forge recommended config file.
 		if (config.hasChanged()) {
 			config.save();
 		}
 		for (Block b : IDToFirePropagateMapping.keySet()) {
 			Blocks.fire.setFireInfo(i, IDToFirePropagateMapping.get(b), IDToFireCatchMapping.get(b));
 		}
 		//Registering event listeners.
 		bonemealEvent = new BonemealEventHandler(moddedBonemeal);
 		MinecraftForge.EVENT_BUS.register(bonemealEvent);
 		animalEvent = new AnimalEventHandler(wildAnimalsBreed, wildAnimalBreedRate, wildAnimalDeathRate);
 		MinecraftForge.EVENT_BUS.register(animalEvent);
 		lumberEvent = new PlayerEventHandler(lumberjack, killLeaves);
 		MinecraftForge.EVENT_BUS.register(lumberEvent);
 		farmingEvent = new AutoFarmingEventHandler(autoFarming);
 		MinecraftForge.EVENT_BUS.register(farmingEvent);
 		autoEvent = new AutoSaplingEventHandler(autoSapling);
 		MinecraftForge.EVENT_BUS.register(autoEvent);
 		if (growthType % 2 == 0)
 			MinecraftForge.EVENT_BUS.register(new SaplingGrowEventHandler());
 	}
 
 	@EventHandler
 	public void preInit(FMLPreInitializationEvent event) {
 		logger = event.getModLog();
 		config = new Configuration(event.getSuggestedConfigurationFile(), true);
 		config.load();
 		for (String name : optionsCategory) {
 			config.addCustomCategoryComment(name, "The lower the rate, the faster the changes happen.");
 		}
 		config.addCustomCategoryComment(optionsCategory[2], "Plants are flower, deadbush, lilypad and tallgrass");
 		config.addCustomCategoryComment(optionsCategory[13], "");
 		autoSapling = config.get(optionsCategory[0], "AutoSapling", true).getBoolean(true);
 		for (int i = 0; i < names.length; i++) {
 			dieSets[i] = config.get(optionsCategory[i], names[i] + " Die", true).getBoolean(true);
 			growSets[i] = config.get(optionsCategory[i], names[i] + " Grow", true).getBoolean(true);
 			deathRates[i] = config.get(optionsCategory[i], names[i] + " Death Rate", 1200).getInt(1200);
 			growthRates[i] = config.get(optionsCategory[i], names[i] + " Growth Rate", 1200).getInt(1200);
 		}
 		//Toggle between alternative time of growth for sapling
 		growthType = GrowthType.valueOf(config.get(optionsCategory[0], "Sapling drops on", "Both", "Possible values are Neither,LeafGrowth,LeafDecay,Both").getString().toUpperCase()).ordinal();
 		//Toggle for lumberjack system on trees
 		lumberjack = config.get(optionsCategory[1], "Enable lumberjack", true).getBoolean(true);
 		killLeaves = config.get(optionsCategory[1], "Lumberjack kill leaves", true).getBoolean(true);
 		//Apples don't have a dying system, because it is only an item
 		growSets[names.length] = config.get(optionsCategory[9], "Apple Grows", true).getBoolean(true);
 		growthRates[names.length] = config.get(optionsCategory[9], "Apple Growth Rate", 3000).getInt(3000);
 		//Force remove leaves after killing a tree, instead of letting Minecraft doing it
 		decayLeaves = config.get(optionsCategory[9], "Enable leaves decay on tree death", true).getBoolean(true);
 		//Toggle so Stone can turn into Mossy Cobblestone
 		mossCorruptStone = config.get(optionsCategory[11], "Enable moss growing on stone", true).getBoolean(true);
 		//Misc options
 		useStarvingSystem = config.get(optionsCategory[names.length], "Enable starving system", true).getBoolean(true);
 		biomeModifiedRate = config.get(optionsCategory[names.length], "Enable biome specific rates", true).getBoolean(true);
 		moddedBonemeal = config.get(optionsCategory[names.length], "Enable modded Bonemeal", true).getBoolean(true);
 		customDimension = config.get(optionsCategory[names.length], "Enable custom dimensions", true).getBoolean(true);
 		wildAnimalsBreed = config.get(optionsCategory[names.length], "Enable wild animals Breed", true).getBoolean(true);
 		wildAnimalBreedRate = config.get(optionsCategory[names.length], "Wild animals breed rate", 16000).getInt(16000);
 		wildAnimalDeathRate = config.get(optionsCategory[names.length], "Wild animals death rate", 16000).getInt(16000);
 		autoFarming = config.get(optionsCategory[names.length], "Plant seeds on player drop", true).getBoolean(true);
 	}
 
     /**
      * Core method. We make vanilla-like random ticks in loaded chunks.
      */
 	@SubscribeEvent
 	public void tickStart(TickEvent.WorldTickEvent event) {
         if(event.side.isServer()){
             if (event.phase == TickEvent.Phase.START && API) {
                 try {
                     Method getMod = api.getMethod("getModOptions", String.class);
                     //"getMod" is static, we don't need an instance
                     Object option = getMod.invoke(null, "Nature Overhaul");
                     Class<?> optionClass = getMod.getReturnType();
                     //To get a submenu
                     Method getSubOption = optionClass.getMethod("getOption", String.class);
                     Object subOption = getSubOption.invoke(option, "General");
                     //Get "LumberJack" submenu
                     Object lumberJackOption = getSubOption.invoke(option, "LumberJack");
                     //Get "Misc" submenu
                     Object miscOption = getSubOption.invoke(option, "Misc");
                     //Get "Animals" submenu
                     Object animalsOption = getSubOption.invoke(option, "Animals");
                     //Get "Fire submenu
                     Object fireOption = getSubOption.invoke(option, "Fire");
                     //We can start to get the values back
                     getMOAPIValues(optionClass, subOption, lumberJackOption, miscOption, animalsOption, fireOption);
                 } catch (SecurityException s) {
                     API = false;
                 } catch (ReflectiveOperationException i) {
                     API = false;
                 }
                 BehaviorFire fire = ((BehaviorFire) BehaviorManager.getBehavior(Blocks.fire));
                 if (fire.getRange() != fireRange) {
                     fire.setRange(fireRange);
                 }
                 bonemealEvent.set(moddedBonemeal);
                 animalEvent.set(wildAnimalsBreed, wildAnimalBreedRate, wildAnimalDeathRate);
                 lumberEvent.set(lumberjack, killLeaves);
                 autoEvent.set(autoSapling);
                 farmingEvent.set(autoFarming);
                 int index = -1;
                 for (Block i : IDToTypeMapping.keySet()) {
                     index = IDToTypeMapping.get(i).getIndex();
                     if (index > -1) {
                         if (growSets[index] != IDToGrowingMapping.get(i))
                             IDToGrowingMapping.put(i, growSets[index]);
                         if (dieSets[index] != IDToDyingMapping.get(i))
                             IDToDyingMapping.put(i, dieSets[index]);
                         IBehave behav = BehaviorManager.getBehavior(i);
                         if (growthRates[index] != behav.getGrowthRate()) {
                             behav.setGrowthRate(growthRates[index]);
                         }
                         if (deathRates[index] != behav.getDeathRate()) {
                             behav.setDeathRate(deathRates[index]);
                         }
                     }
                 }
             }
             if (event.phase == TickEvent.Phase.END) {
                 WorldServer world = (WorldServer) event.world;
                 if ((world.provider.dimensionId == 0 || (customDimension && world.provider.dimensionId != 1)) && !world.activeChunkSet.isEmpty()) {
                     Iterator<?> it = world.activeChunkSet.iterator();
                     while (it.hasNext()) {
                         ChunkCoordIntPair chunkIntPair = (ChunkCoordIntPair) it.next();
                         int k = chunkIntPair.chunkXPos * 16;
                         int l = chunkIntPair.chunkZPos * 16;
                         Chunk chunk = null;
                         if (world.getChunkProvider().chunkExists(chunkIntPair.chunkXPos, chunkIntPair.chunkZPos)) {
                             chunk = world.getChunkFromChunkCoords(chunkIntPair.chunkXPos, chunkIntPair.chunkZPos);
                         }
                         if (chunk != null && chunk.isChunkLoaded && chunk.isTerrainPopulated) {
                             int i2, k2, l2, i3;
                             Block j3;//Vanilla like random ticks for blocks
                             for (ExtendedBlockStorage blockStorage : chunk.getBlockStorageArray()) {
                                 if (blockStorage != null && !blockStorage.isEmpty() && blockStorage.getNeedsRandomTick()) {
                                     for (int j2 = 0; j2 < 3; ++j2) {
                                         this.updateLCG = this.updateLCG * 3 + 1013904223;
                                         i2 = this.updateLCG >> 2;
                                         k2 = i2 & 15;
                                         l2 = i2 >> 8 & 15;
                                         i3 = i2 >> 16 & 15;
                                         j3 = blockStorage.func_150819_a(k2, i3, l2);
                                         if (j3!=Blocks.air && isRegistered(j3)) {
                                             onUpdateTick(world, k2 + k, i3 + blockStorage.getYLocation(), l2 + l, j3);
                                         }
                                     }
                                 }
                             }
                         }
                     }
                 }
             }
         }
 	}
 
 	/**
 	 * The death general method. Called by
 	 * {@link #onUpdateTick(World, int, int, int, Block)} when conditions are
 	 * fulfilled.
 	 **/
 	public static void death(World world, int i, int j, int k, Block id) {
 		if (id instanceof IBlockDeath) {
 			((IBlockDeath) id).death(world, i, j, k, id);
 		} else {
 			BehaviorManager.getBehavior(id).death(world, i, j, k, id);
 		}
 	}
 
 	/**
 	 * Special case for apples, since they don't have a corresponding block
 	 * 
 	 * @return apple growth probability at given coordinates
 	 */
 	public static float getAppleGrowthProb(World world, int i, int j, int k) {
 		float freq = growSets[names.length] ? growthRates[names.length] * 1.5F : -1F;
 		if (biomeModifiedRate && freq > 0) {
 			BiomeGenBase biome = world.getBiomeGenForCoords(i, k);
 			if (biome.rainfall == 0 || biome.temperature > 1.5F) {
 				return 0.01F;
 			} else {
 				freq *= Utils.getOptValueMult(biome.rainfall, 0.8F, 4.0F);
 				freq *= Utils.getOptValueMult(biome.temperature, 0.7F, 4.0F);
 			}
 		}
 		if (freq > 0)
 			return 1F / freq;
 		else
 			return -1F;
 	}
 
 	/**
 	 * Get the growth probability. Called by
 	 * {@link #onUpdateTick(World, int, int, int, Block)}.
 	 * 
 	 * @return growth probability for given blockid and NOType at given
 	 *         coordinates
 	 */
 	public static float getGrowthProb(World world, int i, int j, int k, Block id, NOType type) {
 		float freq = getGrowthRate(id);
 		if (biomeModifiedRate && freq > 0 && type != NOType.NETHERSTALK) {
 			BiomeGenBase biome = world.getBiomeGenForCoords(i, k);
 			if (type != NOType.CACTUS && ((biome.rainfall == 0) || (biome.temperature > 1.5F))) {
 				return 0.01F;
 			} else if (type != NOType.CUSTOM) {
 				freq *= Utils.getOptValueMult(biome.rainfall, getOptRain(id), type.getRainGrowth());
 				freq *= Utils.getOptValueMult(biome.temperature, getOptTemp(id), type.getTempGrowth());
 			}
 		}
 		if (freq > 0)
 			return 1F / freq;
 		else
 			return -1F;
 	}
 
 	public static Map<Block, NOType> getIDToTypeMapping() {
 		return ImmutableMap.copyOf(IDToTypeMapping);
 	}
 
 	public static Map<Block, Block> getLeafToSaplingMapping() {
 		return ImmutableMap.copyOf(LeafToSaplingMapping);
 	}
 
 	public static Map<Block, Block> getLogToLeafMapping() {
 		return ImmutableMap.copyOf(LogToLeafMapping);
 	}
 
 	public static Map<Block, String[]> getTreeIDMeta() {
 		return ImmutableMap.copyOf(TreeIdToMeta);
 	}
 
 	/**
 	 * The general growing method. Called by
 	 * {@link #onUpdateTick(World, int, int, int, Block)}. when conditions are
 	 * fulfilled.
 	 **/
 	public static void grow(World world, int i, int j, int k, Block id) {
 		if (id instanceof IGrowable) {
 			((IGrowable) id).grow(world, i, j, k, id);
 		} else {
 			BehaviorManager.getBehavior(id).grow(world, i, j, k, id);
 		}
 	}
 
 	/**
 	 * Check if given block id is registered as growing
 	 * 
 	 * @param id
 	 *            the block id to check
 	 * @return true if block can grow
 	 */
 	public static boolean isGrowing(Block id) {
 		return IDToGrowingMapping.get(id);
 	}
 
 	/**
 	 * Check if given block id is registered as a log (may be part of a tree)
 	 * 
 	 * @param id
 	 *            the block id to check
 	 * @return true if block is a log
 	 */
 	public static boolean isLog(Block id) {
 		return LogToLeafMapping.containsKey(id) || IDToTypeMapping.get(id) == NOType.MUSHROOMCAP;
 	}
 
 	public static boolean isRegistered(Block id) {
 		return BehaviorManager.isRegistered(id);
 	}
 
 	/**
 	 * Registers all mappings simultaneously.
 	 * 
 	 * @param id
 	 *            The id the block is registered with.
 	 * @param isGrowing
 	 *            Whether the block can call
 	 *            {@link #grow(World, int, int, int, Block)} on tick.
 	 * @param growthRate
 	 *            How often the {@link #grow(World, int, int, int, Block)}
 	 *            method will be called.
 	 * @param isMortal
 	 *            Whether the block can call
 	 *            {@link #death(World, int, int, int, Block)} method on
 	 *            tick.
 	 * @param deathRate
 	 *            How often the
 	 *            {@link #death(World, int, int, int, Block)} method will
 	 *            be called.
 	 * @param optTemp
 	 *            The optimal temperature parameter for the growth.
 	 * @param optRain
 	 *            The optimal humidity parameter for the growth.
 	 * @param type
 	 *            {@link NOType} Decides which growth and/or death to use, and
 	 *            tolerance to temperature and humidity.
 	 */
 	private static void addMapping(Block id, boolean isGrowing, float growthRate, boolean isMortal, float deathRate, float optTemp, float optRain, NOType type) {
 		IDToGrowingMapping.put(id, isGrowing);
 		IDToDyingMapping.put(id, isMortal);
 		IDToTypeMapping.put(id, type);
 		BehaviorManager.setBehavior(id, BehaviorManager.getBehavior(type).setData(growthRate, deathRate, optRain, optTemp));
 	}
 
 	/**
 	 * Registers all mappings simultaneously. Overloaded method with fire parameters.
 	 * 
 	 * @param fireCatch
 	 *            Related to 3rd parameter in {@link
 	 *            BlockFire#setFireInfo(Block,int,int)}.
 	 * @param firePropagate
 	 *            Related to 2nd parameter in {@link
 	 *            BlockFire#setFireInfo(Block,int,int)}.
 	 */
 	private static void addMapping(Block id, boolean isGrowing, float growthRate, boolean isMortal, float deathRate, float optTemp, float optRain, NOType type, int fireCatch, int firePropagate) {
 		addMapping(id, isGrowing, growthRate, isMortal, deathRate, optTemp, optRain, type);
 		IDToFireCatchMapping.put(id, fireCatch);
 		IDToFirePropagateMapping.put(id, firePropagate);
 	}
 
 	/**
 	 * Helper reflection method for booleans
 	 */
 	private static boolean getBooleanFrom(Method meth, Object option, String name) throws ReflectiveOperationException {
 		return Boolean.class.cast(meth.invoke(option, name)).booleanValue();
 	}
 
 	/**
 	 * Get the death probability. Called by
 	 * {@link #onUpdateTick(World, int, int, int, Block)}.
 	 * 
 	 * @return Death probability for given blockid and NOType at given
 	 *         coordinates
 	 */
 	private static float getDeathProb(World world, int i, int j, int k, Block id, NOType type) {
 		float freq = getDeathRate(id);
 		if (biomeModifiedRate && freq > 0 && type != NOType.NETHERSTALK) {
 			BiomeGenBase biome = world.getBiomeGenForCoords(i, k);
 			if (type != NOType.CACTUS && ((biome.rainfall == 0) || (biome.temperature > 1.5F))) {
 				return 1F;
 			} else if (type != NOType.CUSTOM) {
 				freq *= Utils.getOptValueMult(biome.rainfall, getOptRain(id), type.getRainDeath());
 				freq *= Utils.getOptValueMult(biome.temperature, getOptTemp(id), type.getTempDeath());
 			}
 		}
 		if (freq > 0)
 			return 1F / freq;
 		else
 			return -1F;
 	}
 
 	private static float getDeathRate(Block id) {
 		if (id instanceof IBlockDeath) {
 			return ((IBlockDeath) id).getDeathRate();
 		} else {
 			return BehaviorManager.getBehavior(id).getDeathRate();
 		}
 	}
 
 	private static float getGrowthRate(Block id) {
 		if (id instanceof IGrowable) {
 			return ((IGrowable) id).getGrowthRate();
 		} else {
 			return BehaviorManager.getBehavior(id).getGrowthRate();
 		}
 	}
 
 	private static int getIntFrom(Method meth, Object obj, String name) throws ReflectiveOperationException {
 		return Integer.class.cast(meth.invoke(obj, name)).intValue();
 	}
 
 	private static void getMOAPIValues(Class<?> optionClass, Object subOption, Object lumberJackOption, Object miscOption, Object animalsOption, Object fireOption) throws SecurityException,
 			ReflectiveOperationException {
 		Method getBoolean = optionClass.getMethod("getBooleanValue", String.class);
 		Method getSlider = optionClass.getMethod("getSliderValue", String.class);
 		Method getMap = optionClass.getMethod("getMappedValue", String.class);
 		for (int i = 0; i < names.length; i++) {
 			growSets[i] = getBooleanFrom(getBoolean, subOption, names[i] + " grow");
 			dieSets[i] = getBooleanFrom(getBoolean, subOption, names[i] + " die");
 			growthRates[i] = getIntFrom(getSlider, subOption, names[i] + " growth rate");
 			deathRates[i] = getIntFrom(getSlider, subOption, names[i] + " death rate");
 		}
 		growSets[names.length] = getBooleanFrom(getBoolean, subOption, "Apple grows");
 		growthRates[names.length] = getIntFrom(getSlider, subOption, "Apple growth rate");
 		lumberjack = getBooleanFrom(getBoolean, lumberJackOption, "Enable");
 		killLeaves = getBooleanFrom(getBoolean, lumberJackOption, "Kill leaves");
 		growthType = getIntFrom(getMap, miscOption, "Sapling drops on");
 		autoSapling = getBooleanFrom(getBoolean, miscOption, "AutoSapling");
 		autoFarming = getBooleanFrom(getBoolean, miscOption, "Plant seeds on player drop");
 		decayLeaves = getBooleanFrom(getBoolean, miscOption, "Leaves decay on tree death");
 		mossCorruptStone = getBooleanFrom(getBoolean, miscOption, "Moss growing on stone");
 		useStarvingSystem = getBooleanFrom(getBoolean, miscOption, "Starving system");
 		biomeModifiedRate = getBooleanFrom(getBoolean, miscOption, "Biome specific rates");
 		moddedBonemeal = getBooleanFrom(getBoolean, miscOption, "Modded Bonemeal");
 		customDimension = getBooleanFrom(getBoolean, miscOption, "Custom dimensions");
 		wildAnimalsBreed = getBooleanFrom(getBoolean, animalsOption, "Wild breed");
 		wildAnimalBreedRate = getIntFrom(getSlider, animalsOption, "Breeding rate");
 		wildAnimalDeathRate = getIntFrom(getSlider, animalsOption, "Death rate");
 		fireRange = getIntFrom(getSlider, fireOption, "Propagation range");
 	}
 
 	private static float getOptRain(Block id) {
 		return BehaviorManager.getBehavior(id).getOptRain();
 	}
 
 	private static float getOptTemp(Block id) {
 		return BehaviorManager.getBehavior(id).getOptTemp();
 	}
 
 	/**
 	 * Called by {@link #onUpdateTick(World, int, int, int, Block)}. Checks
 	 * whether this block has died on this tick for any reason
 	 * 
 	 * @return True if plant has died
 	 */
 	private static boolean hasDied(World world, int i, int j, int k, Block id) {
 		if (id instanceof IBlockDeath) {
 			return ((IBlockDeath) id).hasDied(world, i, j, k, id);
 		} else {
 			return BehaviorManager.getBehavior(id).hasDied(world, i, j, k, id);
 		}
 	}
 
 	private static boolean isMortal(Block id) {
 		return IDToDyingMapping.get(id);
 	}
 
 	/**
 	 * Called from the world tick {@link #tickStart(TickEvent.WorldTickEvent)} with a
 	 * {@link #isRegistered(Block)} block. Checks with {@link #isGrowing(Block)} or
 	 * {@link #isMortal(Block)} booleans, and probabilities with
 	 * {@link #getGrowthProb(World, int, int, int, Block, NOType)} or
 	 * {@link #getDeathProb(World, int, int, int, Block, NOType)} then call
 	 * {@link #grow(World, int, int, int, Block)} or
 	 * {@link #death(World, int, int, int, Block)}.
 	 */
 	private static void onUpdateTick(World world, int i, int j, int k, Block id) {
 		NOType type = Utils.getType(id);
 		if (isGrowing(id) && world.rand.nextFloat() < getGrowthProb(world, i, j, k, id, type)) {
 			grow(world, i, j, k, id);
 			return;
 		}
 		if (isMortal(id) && (hasDied(world, i, j, k, id) || world.rand.nextFloat() < getDeathProb(world, i, j, k, id, type))) {
 			death(world, i, j, k, id);
 		}
 	}
 }
