 package fyresmodjam;
 
 import java.awt.Color;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.lang.reflect.Method;
 import java.net.URL;
 import java.util.Properties;
 import java.util.Random;
 
 import org.lwjgl.input.Keyboard;
 
 import net.minecraft.block.Block;
 import net.minecraft.block.BlockContainer;
 import net.minecraft.block.material.Material;
 import net.minecraft.command.CommandHandler;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.entity.Entity;
 import net.minecraft.entity.EntityLiving;
 import net.minecraft.entity.EntityLivingBase;
 import net.minecraft.entity.IRangedAttackMob;
 import net.minecraft.entity.SharedMonsterAttributes;
 import net.minecraft.entity.monster.EntityCreeper;
 import net.minecraft.entity.monster.EntityMob;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemArmor;
 import net.minecraft.item.ItemAxe;
 import net.minecraft.item.ItemBow;
 import net.minecraft.item.ItemStack;
 import net.minecraft.item.ItemSword;
 import net.minecraft.stats.Achievement;
 import net.minecraft.util.WeightedRandomChestContent;
 import net.minecraftforge.common.AchievementPage;
 import net.minecraftforge.common.ChestGenHooks;
 import net.minecraftforge.common.Configuration;
 import net.minecraftforge.common.MinecraftForge;
 import net.minecraftforge.event.ForgeSubscribe;
 import net.minecraftforge.event.entity.player.PlayerEvent;
 import cpw.mods.fml.common.IPlayerTracker;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.EventHandler;
 import cpw.mods.fml.common.Mod.Instance;
 import cpw.mods.fml.common.SidedProxy;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPostInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.event.FMLServerStartingEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.common.network.NetworkRegistry;
 import cpw.mods.fml.common.network.PacketDispatcher;
 import cpw.mods.fml.common.network.Player;
 import cpw.mods.fml.common.registry.EntityRegistry;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 import cpw.mods.fml.common.registry.TickRegistry;
 import cpw.mods.fml.relauncher.Side;
 import fyresmodjam.blocks.BlockMysteryMushroom;
 import fyresmodjam.blocks.BlockPillar;
 import fyresmodjam.blocks.BlockTrap;
 import fyresmodjam.commands.CommandCurrentBlessing;
 import fyresmodjam.commands.CommandCurrentDisadvantage;
 import fyresmodjam.commands.CommandCurrentWorldTask;
 import fyresmodjam.commands.CommandKillStats;
 import fyresmodjam.commands.CommandWeaponStats;
 import fyresmodjam.entities.EntityMysteryPotion;
 import fyresmodjam.handlers.CommonTickHandler;
 import fyresmodjam.handlers.GUIHandler;
 import fyresmodjam.handlers.PacketHandler;
 import fyresmodjam.items.ItemMysteryMushroom;
 import fyresmodjam.items.ItemMysteryPotion;
 import fyresmodjam.items.ItemObsidianSceptre;
 import fyresmodjam.items.ItemPillar;
 import fyresmodjam.items.ItemTrap;
 import fyresmodjam.misc.CreativeTabModjamMod;
 import fyresmodjam.misc.EntityStatHelper;
 import fyresmodjam.misc.ItemStatHelper;
 import fyresmodjam.misc.EntityStatHelper.EntityStat;
 import fyresmodjam.misc.EntityStatHelper.EntityStatTracker;
 import fyresmodjam.misc.ItemStatHelper.ItemStat;
 import fyresmodjam.misc.ItemStatHelper.ItemStatTracker;
 import fyresmodjam.tileentities.TileEntityPillar;
 import fyresmodjam.tileentities.TileEntityTrap;
 import fyresmodjam.worldgen.FyresWorldData;
 import fyresmodjam.worldgen.PillarGen;
 import fyresmodjam.worldgen.WorldGenMoreDungeons;
 import fyresmodjam.worldgen.WorldGenTrapsTowersAndMore;
 
 @Mod(modid = "fyresmodjam", name = "Fyres ModJam Mod", version = "0.0.3b")
 @NetworkMod(clientSideRequired = true, serverSideRequired = false, channels = {"FyresModJamMod"}, packetHandler = PacketHandler.class)
 public class ModjamMod extends CommandHandler implements IPlayerTracker {
 	
 	@SidedProxy(clientSide = "fyresmodjam.ClientProxy", serverSide = "fyresmodjam.CommonProxy")
     public static CommonProxy proxy;
     
     @Instance("fyresmodjam")
     public static ModjamMod instance;
     
     public static Random r = new Random();
     
    public static int itemID = 2875, blockID = 2875, achievementID = 2500, examineKey = Keyboard.KEY_X, blessingKey = Keyboard.KEY_K;
     public static int pillarGenChance = 75, maxPillarsPerChunk = 3, towerGenChance = 225, trapGenChance = 300, mushroomReplaceChance = 15;
     public static boolean pillarGlow = true, spawnTraps = true, spawnTowers = true, spawnRandomPillars = true, disableDisadvantages = false, versionChecking = true, trapsBelowGroundOnly = false, showAllPillarsInCreative = false, enableWeaponKillStats = true, enableMobKillStats = true;
     
     public static CreativeTabs tabModjamMod = new CreativeTabModjamMod(CreativeTabs.getNextID(), "The \"You Will Die\" Mod");
     
     public static Block blockPillar;
     public static Block blockTrap;
     public static Block mysteryMushroomBlock;
     
     public static Item itemPillar;
     public static Item mysteryPotion;
     public static Item itemTrap;
     public static Item mysteryMushroom;
     public static Item sceptre;
     
     public static Achievement startTheGame; 
     public static Achievement losingIsFun;
     public static Achievement whoops;
     
     public static Achievement theHunt;
     public static Achievement jackOfAllTrades;
     
     public static AchievementPage page;
     
     public static String version = "v0.0.3b";
     public static String foundVersion = "v0.0.3b";
 	
     /*public static void loadProperties() {
 		Properties prop = new Properties();
 		
 		try {
             prop.load(ModjamMod.class.getResourceAsStream("/FyresModJamMod.properties"));
         } catch (Exception e) {e.printStackTrace();}
 		
 		itemID = Integer.parseInt(prop.getProperty("itemID", "" + itemID));
 		blockID = Integer.parseInt(prop.getProperty("blockID", "" + blockID));
 		//achievementID = Integer.parseInt(prop.getProperty("achievementID", "" + achievementID));
 		pillarGlow = Boolean.parseBoolean(prop.getProperty("pillarGlow", "" + pillarGlow));
 		
 		pillarGenChance = Integer.parseInt(prop.getProperty("pillarGenChance", "" + pillarGenChance));
 		maxPillarsPerChunk = Integer.parseInt(prop.getProperty("maxPillarsPerChunk", "" + maxPillarsPerChunk));
 		towerGenChance = Integer.parseInt(prop.getProperty("towerGenChance", "" + towerGenChance));
 		trapGenChance = Integer.parseInt(prop.getProperty("trapGenChance", "" + trapGenChance));
 		mushroomReplaceChance = Integer.parseInt(prop.getProperty("mushroomReplaceChance", "" + mushroomReplaceChance));
 		
 		spawnTraps = !Boolean.parseBoolean(prop.getProperty("disableTraps", "" + (!spawnTraps)));
 		trapsBelowGroundOnly = Boolean.parseBoolean(prop.getProperty("trapsBelowGroundOnly", "" + trapsBelowGroundOnly));
 		versionChecking = Boolean.parseBoolean(prop.getProperty("versionChecking", "" + versionChecking));
 		
 		showAllPillarsInCreative = Boolean.parseBoolean(prop.getProperty("showAllPillarsInCreative", "" + showAllPillarsInCreative));
     }*/
     
     public static ItemStack losingIsFunStack = new ItemStack(Item.bow, 1);
     public static ItemStack whoopsStack = new ItemStack(Item.flintAndSteel, 1, 1);
     
     public static String configPath = null;
     
     public static final String versionOrder = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
     
 	@EventHandler
 	public void preInit(FMLPreInitializationEvent event) {
 		//loadProperties();
 		
 		File old = new File(event.getSuggestedConfigurationFile().getAbsolutePath().replace("fyresmodjam", "YouWillDieMod"));
 		if(old.exists()) {old.delete(); System.out.println(true);}
 		
 		configPath = event.getSuggestedConfigurationFile().getAbsolutePath().replace("fyresmodjam", "TheYouWillDieMod");
 		
 		Configuration config = new Configuration(new File(configPath));
 		
 		config.load();
 		
 		proxy.loadFromConfig(config);
 		
 		config.save();
 		
 		if(versionChecking) {
             InputStream in = null;
             BufferedReader reader = null;
 
             try {
                 in = new URL("https://dl.dropboxusercontent.com/s/n30va53f6uh2mki/versions.txt?token_hash=AAE89oZXZUV7Khx4mAbLhJS1Q4UuMZW2CXAO52yW1Ef9fw").openStream();
                 reader = new BufferedReader(new InputStreamReader(in));
                 
                 String inputLine;
                 while((inputLine = reader.readLine()) != null && !inputLine.startsWith("YWDMod")) {}
                 if(inputLine != null) {foundVersion = inputLine.split("=")[1];}
             } catch (Exception e) {
             	e.printStackTrace();
             } finally {
                 try {
                     if(reader != null) {reader.close();}
 
                     if(in != null) {in.close();}
                 } catch (Exception e) {e.printStackTrace();}
             }
             
             String[] versionSplit = version.replace("v", "").split("\\."), foundSplit = foundVersion.replace("v", "").split("\\.");
 
             if(!version.equals(foundVersion) && (Integer.parseInt(versionSplit[0]) < Integer.parseInt(foundSplit[0]) ? (Integer.parseInt(versionSplit[1]) < Integer.parseInt(foundSplit[1]) ? (Integer.parseInt("" + versionSplit[2].charAt(0)) < Integer.parseInt("" + foundSplit[2].charAt(0)) ? (versionOrder.indexOf(versionSplit[2].charAt(1)) < versionOrder.indexOf(foundSplit[2].charAt(1))) : false): false) : false)) {
                 System.out.println("A newer version of the \"You Will Die\" Mod has been found (" + foundVersion + ").");
             } else {
                 System.out.println("No newer version of the \"You Will Die\" Mod has been found.");
             }
         } else {System.out.println("\"You Will\" Die Mod version checking disabled.");}
 		
 		startTheGame = getNewAchievement(achievementID, 0, 0, new ItemStack(Item.swordIron, 1), "startTheGame", "You Will Die", "Join a world with this mod installed", null, true);
 		losingIsFun = getNewAchievement(achievementID + 1, -2, 0, losingIsFunStack, "losingIsFun", "Losing Is Fun", "Experience \"fun\"", startTheGame, false);
 		whoops = getNewAchievement(achievementID + 2, 2, 0, whoopsStack, "whoops", "Whoops", "Fail to disarm a trap", startTheGame, false);
 		
 		theHunt = getNewAchievement(achievementID + 3, 0, -2, new ItemStack(Item.bow, 1), "theHunt", "The Hunt", "Become a competent slayer of 5 or more different creatures", startTheGame, false);
 		jackOfAllTrades = getNewAchievement(achievementID + 4, 0, 2, new ItemStack(Block.workbench, 1), "jackOfAllTrades", "Jack of All Trades", "Become a novice user of at least 10 different weapons", startTheGame, false);
 		
 		page = new AchievementPage("The \"You Will Die\" Mod", startTheGame, losingIsFun, whoops, theHunt, jackOfAllTrades);
 		
 		AchievementPage.registerAchievementPage(page);
 	}
 
 	@EventHandler
 	public void init(FMLInitializationEvent event) {
 		
 		//Registering
 		
 		TickRegistry.registerTickHandler(new CommonTickHandler(), Side.SERVER);
 		
 		MinecraftForge.EVENT_BUS.register(this);
 		GameRegistry.registerPlayerTracker(this);
 		
 		new ItemStatHelper().register();
 		new EntityStatHelper().register();
 		
 		NetworkRegistry.instance().registerGuiHandler(this, new GUIHandler());
 		
 		GameRegistry.registerWorldGenerator(new PillarGen());
 		
 		//if(spawnTraps) {
 			GameRegistry.registerWorldGenerator(new WorldGenTrapsTowersAndMore());
 		//}
 		
 		for(int i = 0; i < 3; i++) {GameRegistry.registerWorldGenerator(new WorldGenMoreDungeons());}
 		
 		EntityRegistry.registerGlobalEntityID(EntityMysteryPotion.class, "MysteryPotion", EntityRegistry.findGlobalUniqueEntityId());
         EntityRegistry.registerModEntity(EntityMysteryPotion.class, "MysteryPotion", 0, instance, 128, 1, true);
         LanguageRegistry.instance().addStringLocalization("entity.MysteryPotion.name", "en_US", "Mystery Potion");
 		
 		//Item and Block loading
 		
 		blockPillar = new BlockPillar(blockID).setBlockUnbreakable().setResistance(6000000.0F);
 		blockTrap = new BlockTrap(blockID + 1).setBlockUnbreakable().setResistance(6000000.0F);
 		mysteryMushroomBlock = new BlockMysteryMushroom(blockID + 2).setHardness(0.0F).setStepSound(Block.soundGrassFootstep).setLightValue(0.125F);
 		
 		itemPillar = new ItemPillar(itemID).setUnlocalizedName("blockPillar");
 		mysteryPotion = new ItemMysteryPotion(itemID + 1).setUnlocalizedName("mysteryPotion").setCreativeTab(CreativeTabs.tabBrewing);
 		itemTrap = new ItemTrap(itemID + 2).setUnlocalizedName("itemTrap").setCreativeTab(CreativeTabs.tabBlock);
 		mysteryMushroom = new ItemMysteryMushroom(itemID + 3).setUnlocalizedName("mysteryMushroom").setCreativeTab(CreativeTabs.tabBrewing);
 		sceptre = new ItemObsidianSceptre(itemID + 4).setUnlocalizedName("sceptre").setCreativeTab(CreativeTabs.tabTools).setFull3D();
 		
 		GameRegistry.registerBlock(blockPillar, "blockPillar");
 		GameRegistry.registerTileEntity(TileEntityPillar.class, "Pillar Tile Entity");
 		
 		GameRegistry.registerBlock(blockTrap, "blockTrap");
 		GameRegistry.registerTileEntity(TileEntityTrap.class, "Trap Entity");
 		
 		LanguageRegistry.addName(blockPillar, "Pillar Block");
 		LanguageRegistry.addName(blockTrap, "Trap");
 		LanguageRegistry.addName(mysteryMushroomBlock, "Mystery Mushroom");
 		
 		LanguageRegistry.addName(itemPillar, "Pillar");
 		LanguageRegistry.addName(mysteryPotion, "Mystery Potion");
 		LanguageRegistry.addName(itemTrap, "Trap");
 		LanguageRegistry.addName(mysteryMushroom, "Mystery Mushroom");
 		LanguageRegistry.addName(sceptre, "Obsidian Sceptre");
 		
 		LanguageRegistry.instance().addStringLocalization("commands.currentBlessing.usage", "/currentBlessing - used to check your current blessing");
 		LanguageRegistry.instance().addStringLocalization("commands.currentDisadvantage.usage", "/currentDisadvantage - used to check your current world disadvantage");
 		LanguageRegistry.instance().addStringLocalization("commands.currentGoal.usage", "/currentGoal - used to check your current world goal");
 		LanguageRegistry.instance().addStringLocalization("commands.creatureKnowledge.usage", "/creatureKnowledge [page] - used to check your current creature knowledge stats");
 		LanguageRegistry.instance().addStringLocalization("commands.weaponKnowledge.usage", "/weaponKnowledge [page] - used to check your current weapon knowledge stats");
 		LanguageRegistry.instance().addStringLocalization("fyresmodjam.newVersion", "\u00A7bA newer version of the \"You Will Die\" Mod has been found (" + foundVersion + ").");
 		
 		GameRegistry.addShapelessRecipe(new ItemStack(itemTrap, 1, 0), new Object[] {Block.pressurePlateIron, Block.cactus});
 		GameRegistry.addShapelessRecipe(new ItemStack(itemTrap, 1, 1), new Object[] {Block.pressurePlateIron, Block.torchWood});
 		GameRegistry.addShapelessRecipe(new ItemStack(itemTrap, 1, 2), new Object[] {Block.pressurePlateIron, new ItemStack(Item.dyePowder, 1, 0)});
 		
 		for(int i = 0; i < 13; i++) {
 			GameRegistry.addShapelessRecipe(new ItemStack(mysteryPotion, 1, i + 13), new Object[] {new ItemStack(mysteryPotion, 1, i), Item.gunpowder});
 			GameRegistry.addShapelessRecipe(new ItemStack(mysteryPotion, 1, i), new Object[] {new ItemStack(Item.potion, 1, 0), Item.leather, new ItemStack(mysteryMushroom, 1, i)});
 		}
 		
 		GameRegistry.addRecipe(new ItemStack(sceptre, 1, 0), "X", "Y", "X", 'X', Block.obsidian, 'Y', Block.whiteStone);
 		GameRegistry.addShapelessRecipe(new ItemStack(sceptre, 1, 1), new Object[] {new ItemStack(sceptre, 1, 0), Item.enderPearl, Item.book});
 		
 		proxy.register();
 		
 		//Entity Trackers
 		
 		EntityStatTracker playerTracker = new EntityStatTracker(EntityPlayer.class, true);
 		
 		playerTracker.addStat(new EntityStat("BlessingCooldown", "" + 0));
 		playerTracker.addStat(new EntityStat("BlessingCounter", "" + 0));
 		
 		EntityStatTracker mobTracker = new EntityStatTracker(EntityMob.class, true);
 		
 		mobTracker.addStat(new EntityStat("Level", "") {
 			public Object getNewValue(Random r) {
 				int i = 1;
 				for(; i < 5; i++) {if(ModjamMod.r.nextInt(5) < 3) {break;}}
 				return i;
 			} 
 			
 			public String getAlteredEntityName(EntityLiving entity) {
 				int level = 1;
 				
 				try {
 					level = Integer.parseInt(entity.getEntityData().getString(name));
 				} catch (Exception e) {e.printStackTrace();}
 				
 				return (level == 5 ? "\u00A7c" : "") + entity.getEntityName() + ", Level " + level;
 			}
 			
 			public void modifyEntity(Entity entity) {
 				int level = 1;
 				
 				try {
 					level = Integer.parseInt(entity.getEntityData().getString(name));
 				} catch (Exception e) {e.printStackTrace();}
 				
 				int healthGain = (int) ((level - 1) * (((EntityLivingBase) entity).getMaxHealth()/4) + (level == 5 ? ((EntityLivingBase) entity).getMaxHealth()/4 : 0));
 				
 				if(healthGain != 0) {
 					((EntityLivingBase) entity).getEntityAttribute(SharedMonsterAttributes.maxHealth).setAttribute(((EntityLivingBase) entity).getMaxHealth() + healthGain);
 					((EntityLivingBase) entity).setHealth(((EntityLivingBase) entity).getMaxHealth() + healthGain);
 				}
 				
 				if(level == 5) {
 					switch(r.nextInt(4)) {
 					
 						case 0:
 							if(entity instanceof IRangedAttackMob) {entity.getEntityData().setString("Blessing", "Hunter");}
 							else {entity.getEntityData().setString("Blessing", "Warrior");}
 						break;
 						
 						case 1: entity.getEntityData().setString("Blessing", "Swamp"); break;
 						case 2: entity.getEntityData().setString("Blessing", "Guardian"); break;
 						case 3: entity.getEntityData().setString("Blessing", "Vampire"); break;
 						
 						default: break;
 						
 					}
 					
 					if(entity instanceof EntityCreeper) {
 						((EntityCreeper) entity).getDataWatcher().updateObject(17, (byte) 1);
 						((EntityCreeper) entity).getEntityData().setBoolean("powered", true);
 					}
 				}
 			}
 		});
 		
 		EntityStatHelper.addStatTracker(mobTracker);
 		
 		//Item Trackers
 		
 		ItemStatTracker weaponTracker = new ItemStatTracker(new Class[] {ItemSword.class, ItemAxe.class, ItemBow.class}, null, true);
 		
 		weaponTracker.addStat(new ItemStat("Rank", "") {
 			
 			public String[][] prefixesByRank = {
 					{"Old", "Dull", "Broken", "Worn"},
 					{"Average", "Decent", "Modest", "Ordinary"},
 					{"Strong", "Sharp", "Polished", "Refined"},
 					{"Powerful", "Ruthless", "Elite", "Astonishing"},
 					{"Godly", "Divine", "Fabled", "Legendary"}
 			};
 			
 			public Object getNewValue(ItemStack stack, Random r) {
 				int i = 1;
 				for(; i < 5; i++) {if(ModjamMod.r.nextInt(10) < 7) {break;}}
 				return i;
 			}
 			
 			public void modifyStack(ItemStack stack, Random r) {
 				int rank = Integer.parseInt(stack.getTagCompound().getString(name));
 				float bonusDamage = ((float) rank - 1)/2 + (r.nextInt(rank + 1) * r.nextFloat());
 				
 				//ItemStatHelper.giveStat(stack, "BonusDamage", bonusDamage);
 				//ItemStatHelper.addLore(stack, bonusDamage != 0 ? "\u00A77\u00A7o  " + (bonusDamage > 0 ? "+" : "") + bonusDamage + " bonus damage" : null);
 				
 				ItemStatHelper.giveStat(stack, "BonusDamage", String.format("%.2f", bonusDamage));
 				ItemStatHelper.addLore(stack, !String.format("%.2f", bonusDamage).equals("0.00") ? "\u00A77\u00A7o  " + (bonusDamage > 0 ? "+" : "") + String.format("%.2f", bonusDamage) + " bonus damage" : null);
 				
 				ItemStatHelper.addLore(stack, "\u00A7eRank: "+ rank);
 			}
 			
 			public String getAlteredStackName(ItemStack stack, Random r) {
 				String[] list = prefixesByRank[Integer.parseInt(stack.getTagCompound().getString(name)) - 1];
 				String prefix = list[r.nextInt(list.length)];
 				
 				if(prefix.equals("Sharp") && stack.getItem() instanceof ItemBow) {prefix = "Long";}
 				
 				return "\u00A7f" + prefix + " " + stack.getDisplayName();
 			}
 			
 		});
 		
 		ItemStatHelper.addStatTracker(weaponTracker);
 		
 		ItemStatTracker armorTracker = new ItemStatTracker(new Class[] {ItemArmor.class}, null, true);
 		
 		armorTracker.addStat(new ItemStat("Rank", "") {
 			
 			public String[][] prefixesByRank = {
 					{"Old", "Broken", "Worn", "Weak"},
 					{"Average", "Decent", "Modest", "Ordinary"},
 					{"Polished", "Tough", "Hardened", "Durable"},
 					{"Elite", "Astonishing", "Reinforced", "Resilient"},
 					{"Godly", "Divine", "Fabled", "Legendary"}
 			};
 			
 			public Object getNewValue(ItemStack stack, Random r) {
 				int i = 1;
 				for(; i < 5; i++) {if(ModjamMod.r.nextInt(10) < 7) {break;}}
 				return i;
 			}
 			
 			public void modifyStack(ItemStack stack, Random r) {
 				int rank = Integer.parseInt(stack.getTagCompound().getString(name));
 				float damageReduction = (rank - 1) + r.nextFloat() * 0.5F;
 				
 				ItemStatHelper.giveStat(stack, "DamageReduction", String.format("%.2f", damageReduction));
 				ItemStatHelper.addLore(stack, !String.format("%.2f", damageReduction).equals("0.00") ? "\u00A77\u00A7o  " + (damageReduction > 0 ? "+" : "") + String.format("%.2f", damageReduction) + "% damage reduction" : null);
 				
 				ItemStatHelper.addLore(stack, "\u00A7eRank: "+ rank);
 			}
 			
 			public String getAlteredStackName(ItemStack stack, Random r) {
 				String[] list = prefixesByRank[Integer.parseInt(stack.getTagCompound().getString(name)) - 1];
 				String prefix = list[r.nextInt(list.length)];
 				
 				if(prefix.equals("Sharp") && stack.getItem() instanceof ItemBow) {prefix = "Long";}
 				
 				return "\u00A7f" + prefix + " " + stack.getDisplayName();
 			}
 			
 		});
 		
 		ItemStatHelper.addStatTracker(armorTracker);
 		
 		//ItemStatTracker foodTracker = new ItemStatTracker(ItemFood.class, -1);
 		//foodTracker.addStat(new ItemStat("Spoiled", false));
 		//ItemStatHelper.addStatTracker(foodTracker);
 		
 		//Other
 		
 		losingIsFunStack.itemID = itemTrap.itemID;
 		whoopsStack.itemID = itemTrap.itemID;
 		
 		for(int i = 0; i < 13; i++) {
 			ChestGenHooks.getInfo(ChestGenHooks.DUNGEON_CHEST).addItem(new WeightedRandomChestContent(mysteryPotion.itemID, i, 1, 3, 2));
 			WorldGenTrapsTowersAndMore.chestGenInfo.addItem(new WeightedRandomChestContent(mysteryPotion.itemID, i, 1, 3, 2));
 		}
 	}
 	
 	@EventHandler
 	public void postInit(FMLPostInitializationEvent event) {
 		
 	}
 
 	@Override
 	public void onPlayerLogin(EntityPlayer player) {
 		if(!player.worldObj.isRemote) {
 			PacketDispatcher.sendPacketToPlayer(PacketHandler.newPacket(PacketHandler.UPDATE_WORLD_DATA, new Object[] {CommonTickHandler.worldData.potionValues, CommonTickHandler.worldData.potionDurations, CommonTickHandler.worldData.getDisadvantage(), CommonTickHandler.worldData.currentTask, CommonTickHandler.worldData.currentTaskID, CommonTickHandler.worldData.currentTaskAmount, CommonTickHandler.worldData.progress, CommonTickHandler.worldData.tasksCompleted, CommonTickHandler.worldData.enderDragonKilled, ModjamMod.spawnTraps, CommonTickHandler.worldData.rewardLevels, CommonTickHandler.worldData.mushroomColors}), (Player) player);
 			
 			String name = CommonTickHandler.worldData.currentTask.equals("Kill") ? FyresWorldData.validMobNames[CommonTickHandler.worldData.currentTaskID] : new ItemStack(Item.itemsList[CommonTickHandler.worldData.currentTaskID], 1).getDisplayName();
 			
 			if(CommonTickHandler.worldData.currentTaskAmount > 1) {
 				if(name.contains("Block")) {name = name.replace("Block", "Blocks").replace("block", "blocks");}
 				else {name += "s";}
 			}
 			
 			int index = -1;
 			for(int i = 0; i < CommonTickHandler.worldData.validDisadvantages.length; i++) {if(CommonTickHandler.worldData.validDisadvantages[i].equals(CommonTickHandler.worldData.getDisadvantage())) {index = i; break;}}
 			PacketDispatcher.sendPacketToPlayer(PacketHandler.newPacket(PacketHandler.SEND_MESSAGE, new Object[] {"\u00A7eWorld disadvantage: " + CommonTickHandler.worldData.getDisadvantage() + (index == -1 ? "" : " (" + CommonTickHandler.worldData.disadvantageDescriptions[index] + ")")}), (Player) player);
 			
 			PacketDispatcher.sendPacketToPlayer(PacketHandler.newPacket(PacketHandler.SEND_MESSAGE, new Object[] {"\u00A7eWorld goal: " + CommonTickHandler.worldData.currentTask + " " + CommonTickHandler.worldData.currentTaskAmount + " " + name + ". (" + CommonTickHandler.worldData.progress + " " + CommonTickHandler.worldData.currentTask + "ed)"}), (Player) player);
 			//PacketDispatcher.sendPacketToPlayer(PacketHandler.newPacket(PacketHandler.SEND_MESSAGE, new Object[] {"\u00A7eReward: " + CommonTickHandler.worldData.rewardLevels + " levels"}), (Player) player);
 			//PacketDispatcher.sendPacketToPlayer(PacketHandler.newPacket(PacketHandler.SEND_MESSAGE, new Object[] {"\u00A7eGoals completed: " + CommonTickHandler.worldData.tasksCompleted}), (Player) player);
 		
 			if(!player.getEntityData().hasKey("Blessing")) {
 				player.getEntityData().setString("Blessing", TileEntityPillar.validBlessings[ModjamMod.r.nextInt(TileEntityPillar.validBlessings.length)]);
 				while(player.getEntityData().getString("Blessing").equals("Inferno")) {player.getEntityData().setString("Blessing", TileEntityPillar.validBlessings[ModjamMod.r.nextInt(TileEntityPillar.validBlessings.length)]);}
 				PacketDispatcher.sendPacketToPlayer(PacketHandler.newPacket(PacketHandler.SEND_MESSAGE, new Object[] {"\u00A72You've been granted the Blessing of the " + player.getEntityData().getString("Blessing") + ". (Use /currentBlessing to check effect)"}), (Player) player);
 			}
 			
 			PacketDispatcher.sendPacketToPlayer(PacketHandler.newPacket(PacketHandler.UPDATE_BLESSING, new Object[] {player.getEntityData().getString("Blessing")}), (Player) player);
 			
 			if(!player.getEntityData().hasKey("PotionKnowledge")) {player.getEntityData().setIntArray("PotionKnowledge", new int[] {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1});}
 			PacketDispatcher.sendPacketToPlayer(PacketHandler.newPacket(PacketHandler.UPDATE_POTION_KNOWLEDGE, new Object[] {player.getEntityData().getIntArray("PotionKnowledge")}), (Player) player);
 		}
 		
 		if(versionChecking && !version.equals(foundVersion)) {player.addChatMessage("fyresmodjam.newVersion");}
 		
 		//player.triggerAchievement(startTheGame);
 	}
 
 	@Override
 	public void onPlayerLogout(EntityPlayer player) {
 		
 	}
 
 	@Override
 	public void onPlayerChangedDimension(EntityPlayer player) {
 		PacketDispatcher.sendPacketToPlayer(PacketHandler.newPacket(PacketHandler.UPDATE_WORLD_DATA, new Object[] {CommonTickHandler.worldData.potionValues, CommonTickHandler.worldData.potionDurations, CommonTickHandler.worldData.getDisadvantage(), CommonTickHandler.worldData.currentTask, CommonTickHandler.worldData.currentTaskID, CommonTickHandler.worldData.currentTaskAmount, CommonTickHandler.worldData.progress, CommonTickHandler.worldData.tasksCompleted, CommonTickHandler.worldData.enderDragonKilled, ModjamMod.spawnTraps, CommonTickHandler.worldData.rewardLevels, CommonTickHandler.worldData.mushroomColors}), (Player) player);
 		
 		PacketDispatcher.sendPacketToPlayer(PacketHandler.newPacket(PacketHandler.UPDATE_BLESSING, new Object[] {player.getEntityData().getString("Blessing")}), (Player) player);
 		
 		if(!player.getEntityData().hasKey("PotionKnowledge")) {player.getEntityData().setIntArray("PotionKnowledge", new int[] {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1});}
 		PacketDispatcher.sendPacketToPlayer(PacketHandler.newPacket(PacketHandler.UPDATE_POTION_KNOWLEDGE, new Object[] {player.getEntityData().getIntArray("PotionKnowledge")}), (Player) player);
 	}
 
 	@Override
 	public void onPlayerRespawn(EntityPlayer player) {
 		
 	}
 	
 	@ForgeSubscribe
     public void checkBreakSpeed(PlayerEvent.BreakSpeed event) {
     	if(event.entityPlayer != null && event.entityPlayer.getEntityData().hasKey("Blessing")) {
     		String blessing = event.entityPlayer.getEntityData().getString("Blessing");
     		
     		if(blessing.equals("Miner")) {
     			if(event.block.blockMaterial == Material.rock || event.block.blockMaterial == Material.iron) {event.newSpeed = event.originalSpeed * 1.25F;}
     		} else if(blessing.equals("Lumberjack")) {
     			if(event.block.blockMaterial == Material.wood) {event.newSpeed = event.originalSpeed * 1.25F;}
     		}
     	}
 	}
 
 	@EventHandler
 	public void serverStarting(FMLServerStartingEvent event) {
 		this.initCommands(event);
 	}
 
 	public void initCommands(FMLServerStartingEvent event) {
 		event.registerServerCommand(new CommandCurrentBlessing());
 		event.registerServerCommand(new CommandCurrentDisadvantage());
 		event.registerServerCommand(new CommandCurrentWorldTask());
 		event.registerServerCommand(new CommandKillStats());
 		event.registerServerCommand(new CommandWeaponStats());
 	}
 
 	public static Achievement getNewAchievement(int id, int x, int y, ItemStack stack, String name, String displayName, String desc, Achievement prereq, boolean independent) {
 		Achievement achievement = new Achievement(id, name, x, y, stack, prereq);
 		if(independent) {achievement = achievement.setIndependent();}
 		LanguageRegistry.instance().addStringLocalization("achievement." + name, "en_US", displayName);
 		LanguageRegistry.instance().addStringLocalization("achievement." + name + ".desc", "en_US", desc);
 		achievement.registerAchievement();
 		return achievement;
 	}
 }
