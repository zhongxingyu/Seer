 package assets.fyresmodjam;
 
 import java.io.BufferedReader;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.util.Properties;
 import java.util.Random;
 
 import net.minecraft.block.Block;
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
 import net.minecraft.item.ItemAxe;
 import net.minecraft.item.ItemBow;
 import net.minecraft.item.ItemStack;
 import net.minecraft.item.ItemSword;
 import net.minecraft.stats.Achievement;
 import net.minecraft.util.WeightedRandomChestContent;
 import net.minecraftforge.common.AchievementPage;
 import net.minecraftforge.common.ChestGenHooks;
 import net.minecraftforge.common.MinecraftForge;
 import net.minecraftforge.event.ForgeSubscribe;
 import net.minecraftforge.event.entity.player.PlayerEvent;
 import assets.fyresmodjam.EntityStatHelper.EntityStat;
 import assets.fyresmodjam.EntityStatHelper.EntityStatTracker;
 import assets.fyresmodjam.ItemStatHelper.ItemStat;
 import assets.fyresmodjam.ItemStatHelper.ItemStatTracker;
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
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 import cpw.mods.fml.common.registry.TickRegistry;
 import cpw.mods.fml.relauncher.Side;
 
 @Mod(modid = "fyresmodjam", name = "Fyres ModJam Mod", version = "0.0.1c")
 @NetworkMod(clientSideRequired = true, serverSideRequired = false, channels = {"FyresModJamMod"}, packetHandler = PacketHandler.class)
 public class ModjamMod extends CommandHandler implements IPlayerTracker {
 	
 	@SidedProxy(clientSide = "assets.fyresmodjam.ClientProxy", serverSide = "assets.fyresmodjam.CommonProxy")
     public static CommonProxy proxy;
     
     @Instance("fyresmodjam")
     public static ModjamMod instance;
     
     public static Random r = new Random();
     
     public static int itemID = 2875, blockID = 2875, achievementID = 2500;
     public static boolean pillarGlow = true, spawnTraps = true, versionChecking = true;
     
     public static Block blockPillar;
     public static Block blockTrap;
     
     public static Item itemPillar;
     public static Item mysteryPotion;
     public static Item itemTrap;
     
     public static Achievement startTheGame; 
     public static Achievement losingIsFun;
     public static Achievement whoops;
     public static AchievementPage page;
     
     public static String version = "v0.0.1c";
     public static String foundVersion = "v0.0.1c";
 	
     public static void loadProperties() {
 		Properties prop = new Properties();
 		
 		try {
             prop.load(ModjamMod.class.getResourceAsStream("/FyresModJamMod.properties"));
         } catch (Exception e) {e.printStackTrace();}
 		
 		itemID = Integer.parseInt(prop.getProperty("itemID", "" + itemID));
 		blockID = Integer.parseInt(prop.getProperty("blockID", "" + blockID));
 		//achievementID = Integer.parseInt(prop.getProperty("achievementID", "" + achievementID));
 		pillarGlow = Boolean.parseBoolean(prop.getProperty("pillarGlow", "" + pillarGlow));
 		spawnTraps = Boolean.parseBoolean(prop.getProperty("spawnTraps", "" + spawnTraps));
 		versionChecking = Boolean.parseBoolean(prop.getProperty("versionChecking", "" + versionChecking));
     }
     
     public static ItemStack losingIsFunStack = new ItemStack(Item.bow, 1);
     public static ItemStack whoopsStack = new ItemStack(Item.flintAndSteel, 1, 1);
     
 	@EventHandler
 	public void preInit(FMLPreInitializationEvent event) {
 		loadProperties();
 		
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
 
             if(!version.equals(foundVersion)) {
                 System.out.println("A newer version of the \"You Will Die\" Mod has been found (" + foundVersion + ").");
             } else {
                 System.out.println("No newer version of the \"You Will Die\" Mod has been found.");
             }
         } else {System.out.println("\"You Will\" Die Mod version checking disabled.");}
 		
 		startTheGame = getNewAchievement(achievementID, 0, 0, new ItemStack(Item.swordIron, 1), "startTheGame", "You Will Die", "Join a world with this mod installed", null, true);
 		losingIsFun = getNewAchievement(achievementID + 1, -2, 0, losingIsFunStack, "losingIsFun", "Losing Is Fun", "Experience \"fun\"", startTheGame, false);
 		whoops = getNewAchievement(achievementID + 2, 2, 0, whoopsStack, "whoops", "Whoops", "Fail to disarm a trap", startTheGame, false);
 		page = new AchievementPage("The \"You Will Die\" Mod", startTheGame, losingIsFun, whoops);
 		
 		AchievementPage.registerAchievementPage(page);
 	}
 
 	@EventHandler
 	public void init(FMLInitializationEvent event) {
 		
 		//Registering
 		
 		proxy.register();
 		
 		TickRegistry.registerTickHandler(new CommonTickHandler(), Side.SERVER);
 		
 		MinecraftForge.EVENT_BUS.register(this);
 		GameRegistry.registerPlayerTracker(this);
 		
 		new ItemStatHelper().register();
 		new EntityStatHelper().register();
 		
 		NetworkRegistry.instance().registerGuiHandler(this, new GUIHandler());
 		
 		GameRegistry.registerWorldGenerator(new PillarGen());
 		
 		//if(spawnTraps) {
 			GameRegistry.registerWorldGenerator(new WorldGenTrapsAndTowers());
 		//}
 		
 		for(int i = 0; i < 3; i++) {GameRegistry.registerWorldGenerator(new WorldGenMoreDungeons());}
 		
 		//Item and Block loading
 		
 		blockPillar = new BlockPillar(blockID).setBlockUnbreakable().setResistance(6000000.0F);
 		blockTrap = new BlockTrap(blockID + 1).setBlockUnbreakable().setResistance(6000000.0F);
 		
 		itemPillar = new ItemPillar(itemID).setUnlocalizedName("blockPillar");
 		mysteryPotion = new ItemMysteryPotion(itemID + 1).setUnlocalizedName("mysteryPotion").setCreativeTab(CreativeTabs.tabBrewing);
 		itemTrap = new ItemTrap(itemID + 2).setUnlocalizedName("itemTrap").setCreativeTab(CreativeTabs.tabBlock);
 		
 		GameRegistry.registerBlock(blockPillar, "blockPillar");
 		GameRegistry.registerTileEntity(TileEntityPillar.class, "Pillar Tile Entity");
 		
 		GameRegistry.registerBlock(blockTrap, "blockTrap");
 		GameRegistry.registerTileEntity(TileEntityTrap.class, "Trap Entity");
 		
 		LanguageRegistry.addName(blockPillar, "Pillar Block");
 		LanguageRegistry.addName(blockTrap, "Trap");
 		
 		LanguageRegistry.addName(itemPillar, "Pillar");
 		LanguageRegistry.addName(mysteryPotion, "Mystery Potion");
 		LanguageRegistry.addName(itemTrap, "Trap");
 		
 		LanguageRegistry.instance().addStringLocalization("commands.currentBlessing.usage", "/currentBlessing - used to check your current blessing");
 		LanguageRegistry.instance().addStringLocalization("commands.currentDisadvantage.usage", "/currentDisadvantage - used to check your current world disadvantage");
 		LanguageRegistry.instance().addStringLocalization("commands.currentGoal.usage", "/currentGoal - used to check your current world goal");
		LanguageRegistry.instance().addStringLocalization("fyresmodjam.newVersion", "\u00A7eA newer version of the \"You Will Die\" Mod has been found (" + foundVersion + ").");
 		
 		//Entity Trackers
 		
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
 				
 				int healthGain = (level - 1) * 5 + (level == 5 ? 5 : 0);
 				
 				if(healthGain != 0) {
 					((EntityLivingBase) entity).func_110148_a(SharedMonsterAttributes.field_111267_a).func_111128_a(((EntityLivingBase) entity).func_110138_aP() + healthGain);
 					((EntityLivingBase) entity).setEntityHealth(((EntityLivingBase) entity).func_110143_aJ() + healthGain);
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
 		
 		weaponTracker.addStat(new ItemStat("Prefix", "") {
 			public String[] prefixes = new String[] {"Old", "Sharp", "Average"};
 			public String[] prefixesBow = new String[] {"Old", "Long", "Average"};
 			
 			public Object getNewValue(ItemStack stack, Random r) {return stack.getItem() instanceof ItemBow ? prefixesBow[r.nextInt(prefixesBow.length)] : prefixes[r.nextInt(prefixes.length)];}
 			public String getAlteredStackName(ItemStack stack) {return "\u00A7f" + stack.getTagCompound().getString(name) + " " + stack.getDisplayName();}
 		});
 		
 		weaponTracker.addStat(new ItemStat("Rank", "") {
 			public Object getNewValue(ItemStack stack, Random r) {
 				int i = 1;
 				for(; i < 5; i++) {if(ModjamMod.r.nextInt(5) < 3) {break;}}
 				return i;
 			}
 			
 			public void modifyStack(ItemStack stack) {
 				int rank = Integer.parseInt(stack.getTagCompound().getString(name));
 				int bonusDamage = (rank - 1)/2 + (int) (ModjamMod.r.nextInt(rank + 1));
 				
 				ItemStatHelper.giveStat(stack, "BonusDamage", bonusDamage);
 				ItemStatHelper.addLore(stack, bonusDamage != 0 ? "\u00A77\u00A7o  " + (bonusDamage > 0 ? "+" : "") + bonusDamage + " bonus damage" : null);
 				
 				ItemStatHelper.addLore(stack, "\u00A7eRank: "+ rank);
 			}
 		});
 		
 		ItemStatHelper.addStatTracker(weaponTracker);
 		
 		//ItemStatTracker foodTracker = new ItemStatTracker(ItemFood.class, -1);
 		//foodTracker.addStat(new ItemStat("Spoiled", false));
 		//ItemStatHelper.addStatTracker(foodTracker);
 		
 		//Other
 		
 		losingIsFunStack.itemID = itemTrap.itemID;
 		whoopsStack.itemID = itemTrap.itemID;
 		
 		for(int i = 0; i < 13; i++) {
 			ChestGenHooks.getInfo(ChestGenHooks.DUNGEON_CHEST).addItem(new WeightedRandomChestContent(mysteryPotion.itemID, i, 1, 5, 5));
 		}
 	}
 	
 	@EventHandler
 	public void postInit(FMLPostInitializationEvent event) {
 		
 	}
 
 	@Override
 	public void onPlayerLogin(EntityPlayer player) {
 		if(!player.worldObj.isRemote) {
 			PacketDispatcher.sendPacketToPlayer(PacketHandler.newPacket(PacketHandler.UPDATE_WORLD_DATA, new Object[] {CommonTickHandler.worldData.potionValues, CommonTickHandler.worldData.potionDurations, CommonTickHandler.worldData.currentDisadvantage, CommonTickHandler.worldData.currentTask, CommonTickHandler.worldData.currentTaskID, CommonTickHandler.worldData.currentTaskAmount, CommonTickHandler.worldData.progress, CommonTickHandler.worldData.tasksCompleted, CommonTickHandler.worldData.enderDragonKilled, ModjamMod.spawnTraps}), (Player) player);
 			
 			int index = -1;
 			for(int i = 0; i < CommonTickHandler.worldData.validDisadvantages.length; i++) {if(CommonTickHandler.worldData.validDisadvantages[i].equals(CommonTickHandler.worldData.currentDisadvantage)) {index = i; break;}}
 			PacketDispatcher.sendPacketToPlayer(PacketHandler.newPacket(PacketHandler.SEND_MESSAGE, new Object[] {"\u00A7eWorld disadvantage: " + CommonTickHandler.worldData.currentDisadvantage + (index == -1 ? "" : " (" + CommonTickHandler.worldData.disadvantageDescriptions[index] + ")")}), (Player) player);
 			
 			PacketDispatcher.sendPacketToPlayer(PacketHandler.newPacket(PacketHandler.SEND_MESSAGE, new Object[] {"\u00A7eWorld goal: " + CommonTickHandler.worldData.currentTask + " " + CommonTickHandler.worldData.currentTaskAmount + " " + (CommonTickHandler.worldData.currentTask.equals("Kill") ? CommonTickHandler.worldData.validMobNames[CommonTickHandler.worldData.currentTaskID] : new ItemStack(Item.itemsList[CommonTickHandler.worldData.currentTaskID], 1).getDisplayName()) + (CommonTickHandler.worldData.currentTaskAmount > 1 ? "s" : "") + ". (" + CommonTickHandler.worldData.progress + " " + CommonTickHandler.worldData.currentTask + "ed)"}), (Player) player);
 			//PacketDispatcher.sendPacketToPlayer(PacketHandler.newPacket(PacketHandler.SEND_MESSAGE, new Object[] {"\u00A7eGoals completed: " + CommonTickHandler.worldData.tasksCompleted}), (Player) player);
 		
 			if(!player.getEntityData().hasKey("Blessing")) {
 				player.getEntityData().setString("Blessing", TileEntityPillar.validBlessings[ModjamMod.r.nextInt(TileEntityPillar.validBlessings.length)]);
 				while(player.getEntityData().getString("Blessing").equals("Inferno")) {player.getEntityData().setString("Blessing", TileEntityPillar.validBlessings[ModjamMod.r.nextInt(TileEntityPillar.validBlessings.length)]);}
 				PacketDispatcher.sendPacketToPlayer(PacketHandler.newPacket(PacketHandler.SEND_MESSAGE, new Object[] {"\u00A72You've been granted the Blessing of the " + player.getEntityData().getString("Blessing") + ". (Use /currentBlessing to check effect)"}), (Player) player);
 			}
 			
 			PacketDispatcher.sendPacketToPlayer(PacketHandler.newPacket(PacketHandler.UPDATE_BLESSING, new Object[] {player.getEntityData().getString("Blessing")}), (Player) player);
 			
 			if(!player.getEntityData().hasKey("PotionKnowledge")) {player.getEntityData().setIntArray("PotionKnowledge", new int[] {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1});}
 			PacketDispatcher.sendPacketToPlayer(PacketHandler.newPacket(PacketHandler.UPDATE_POTION_KNOWLEDGE, new Object[] {player.getEntityData().getIntArray("PotionKnowledge")}), (Player) player);
		} else if(versionChecking && !version.equals(foundVersion)) {player.addChatMessage("fyresmodjam.newVersion");}
 		
 		//player.triggerAchievement(startTheGame);
 	}
 
 	@Override
 	public void onPlayerLogout(EntityPlayer player) {
 		
 	}
 
 	@Override
 	public void onPlayerChangedDimension(EntityPlayer player) {
 		
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
