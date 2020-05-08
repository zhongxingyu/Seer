 package assets.electrolysm.electro;
 
 import net.minecraft.block.Block;
 import net.minecraft.block.BlockOre;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemTool;
 import net.minecraft.world.biome.BiomeGenBase;
 import assets.electrolysm.api.power.PowerHandler;
 import assets.electrolysm.electro.advAtomics.liquids.fluidStorage;
 import assets.electrolysm.electro.advAtomics.liquids.plasma;
 import assets.electrolysm.electro.biome.EntityZombie_Scientist;
 import assets.electrolysm.electro.biome.diseasedBiome;
 import assets.electrolysm.electro.biome.diseasedGrass;
 import assets.electrolysm.electro.biome.spawnZS;
 import assets.electrolysm.electro.block.advMachines.energiser;
 import assets.electrolysm.electro.block.advMachines.energisingRod;
 import assets.electrolysm.electro.block.advMachines.injector;
 import assets.electrolysm.electro.block.advMachines.injectionArm;
 import assets.electrolysm.electro.block.basic.blastDoor;
 import assets.electrolysm.electro.block.basic.blastGlass;
 import assets.electrolysm.electro.block.basic.blastProof;
 import assets.electrolysm.electro.block.basic.glassModifier;
 import assets.electrolysm.electro.block.basic.hammer;
 import assets.electrolysm.electro.block.basic.modBlastGlass;
 import assets.electrolysm.electro.block.machines.desk;
 import assets.electrolysm.electro.block.machines.researchDesk;
 import assets.electrolysm.electro.block.machines.workBench;
 import assets.electrolysm.electro.client.ClientProxy;
 import assets.electrolysm.electro.handlers.BetaHandler;
 import assets.electrolysm.electro.handlers.Crafting;
 import assets.electrolysm.electro.handlers.GUIHandler;
 import assets.electrolysm.electro.handlers.IDHandler;
 import assets.electrolysm.electro.handlers.Names;
 import assets.electrolysm.electro.handlers.Referance;
 import assets.electrolysm.electro.handlers.Register;
 import assets.electrolysm.electro.handlers.RegisterBlock;
 import assets.electrolysm.electro.handlers.ResearchHandler;
 import assets.electrolysm.electro.handlers.TickHandler;
 import assets.electrolysm.electro.handlers.VersionCheck;
 import assets.electrolysm.electro.item.basic.drillCasing;
 import assets.electrolysm.electro.item.basic.plasmaDrill;
 import assets.electrolysm.electro.powerSystem.TEUDetector;
 import assets.electrolysm.electro.powerSystem.copperwire;
 import assets.electrolysm.electro.powerSystem.wireGold;
 import assets.electrolysm.electro.research.card;
 import assets.electrolysm.electro.research.knowledge;
 import assets.electrolysm.electro.research.researchPaper;
 import assets.electrolysm.electro.world.chunkGraphite;
 import assets.electrolysm.electro.world.copperIngot;
 import assets.electrolysm.electro.world.copperOre;
 import assets.electrolysm.electro.world.graphite;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.EventHandler;
 import cpw.mods.fml.common.Mod.Instance;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.common.network.NetworkRegistry;
 import cpw.mods.fml.common.registry.EntityRegistry;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.TickRegistry;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 
 	@Mod(modid=Referance.MOD_REF.MOD_ID, name=Referance.MOD_REF.MOD_ID, version= Referance.MOD_REF.VERSION)
  
 	@NetworkMod(channels = { Referance.MOD_REF.CHANNEL }, clientSideRequired = true, serverSideRequired = true, packetHandler = packetHandler.class)
 
 	public class electrolysmCore {
 
 		public static CreativeTabs TabElectrolysm = new TabElectrolysm(CreativeTabs.getNextID(),"Electrolysm|Basics of Science");
 
 		public static GUIHandler guiHandler = new GUIHandler();
 		
 		@Instance("Electrolysm")
 		public static electrolysmCore GUIInstance;
 		//Basic Machines
         public static Block workBench = new workBench(IDHandler.machines.basic.workBenchID, null);
         public static Block desk = new desk(IDHandler.machines.basic.deskID, null);
         //Advanced Machines
         public static Block injector = new injector(IDHandler.advMachines.injectorID, null);
         public static Item injectionArm = new injectionArm(IDHandler.advMachines.injectionArmID);
         
         //Research System
         public static Block researchDesk = new researchDesk(IDHandler.machines.basic.researchDeskID, null);
         public static card card = new card(IDHandler.research.cardID);
         public static Item researchPaper = new researchPaper(IDHandler.research.paperID);
         public static Item knowledge = new knowledge(IDHandler.research.knowledgeID);
         
         //World Generation
         public static BlockOre graphite = new graphite(IDHandler.worldGenOres.graphiteID);
         public static Item chunkGraphite = new chunkGraphite(IDHandler.worldGenOres.chuckGraphiteID);
<<<<<<< Updated upstream
         public static Item copperIngot = new copperIngot(IDHandler.worldGenOres.copperIngotID);
         public static BlockOre copperOre = new copperOre(IDHandler.worldGenOres.copperOreID);
=======
        //public static Item copperIngot = new copperIngot(IDHandler.worldGenOres.copperIngotID);
>>>>>>> Stashed changes
         
         //Biome
         public static Item spawnZS = new spawnZS(IDHandler.basic.spawnZSID);
         public static Block diseaseGrass = new diseasedGrass(IDHandler.basic.diseasedGrassID, null);
 		public static final BiomeGenBase diseasedBiomeObj = new diseasedBiome(IDHandler.basic.biomeID);
         public BiomeGenBase diseasedBiome = diseasedBiomeObj;
 
         //Security
         public static Block blastProof = new blastProof(IDHandler.basic.blastProofID, null);
         public static Block blastDoor = new blastDoor(IDHandler.basic.blastDoorID, null);
         private static String[] glassTexture = { "","","","","","","","","","","","","","","","",""};
         public static Block blastGlass = new blastGlass(IDHandler.basic.blastGrassID, null, false, glassTexture);
         public static Block modBlastGlass = new modBlastGlass(IDHandler.basic.modBlastGrassID, null, false, glassTexture);
         public static Item glassModifire = new glassModifier(IDHandler.basic.glassModifierID);
         
         //Tools
         public static Item hammer = new hammer(IDHandler.basic.hammerID);
         
         //Advanced atomics
         //Liquids
         public static Block plasma = new plasma(IDHandler.advAtomics.fluid.plasmaID);
         public static Item fluidStorage = new fluidStorage(IDHandler.advAtomics.fluid.fluidStorageID);
         //Blocks
         //Machines
         //public static Block quantumComp = new quantumComp(IDHandler.advMachines.quantumCompID, null);
         public static Block energiser = new energiser(IDHandler.advMachines.energiserID, null); 
         public static Item energisingRod = new energisingRod(IDHandler.advMachines.energisingRodID);
         //Items 
         //Tools
         public static Item plasmaDrill = new plasmaDrill(IDHandler.basic.plasmaDrillID, 0, null, null);
         public static Item drillCasing = new drillCasing(IDHandler.basic.drillCasingID);
         
         //Power System
         public static Item TEUDetector = new TEUDetector(IDHandler.powerGrid.TEUDetectorID);
         public static Block wireGold = new wireGold(IDHandler.powerGrid.wireGoldID, null);
         public static Item copperwire = new copperwire(IDHandler.powerGrid.copperwireID);
 
         
         /*
 		//Robots	
         //Parts
         public static Item metalSheet = new metalSheet(IDHandler.robotics.metalSheetID);
         public static Item wire = new wire(IDHandler.robotics.wireID);
         public static Item servo = new servo(IDHandler.robotics.servoID);
         public static Item artMuscle = new artMuscle(IDHandler.robotics.artMuscleID);
         public static Item carbonBone = new carbonBone(IDHandler.robotics.carbonBoneID);
         //=====================Adv. Parts================================
         public static Item microController = new microCont(IDHandler.robotics.microContID);
         public static Item upgrade = new upgrade(IDHandler.robotics.upgradeID);
         public static Item silChip = new silChip(IDHandler.robotics.silChipID);
         public static Item ChipDup = new chipDup(IDHandler.robotics.chipDub);
         //===============================================================
         public static Item bionicArm = new bionicArm(IDHandler.robotics.bionicArmID);
         public static Item bionicChest = new bionicChest(IDHandler.robotics.bionicChestID);
         public static Item bionicHead = new bionicHead(IDHandler.robotics.bionicHeadID);
         public static Item bionicLeg = new bionicLeg(IDHandler.robotics.bionicLegID);
         //====================Machines!==================================
         public static Block soldering = new soldering(IDHandler.robotics.machines.solderingID, null);
         public static Block partAssemb = new partAssemb(IDHandler.robotics.machines.partAssembID, null);
 		*/
 /* 
  * ===============================================================================================================
  * ===============================================================================================================
  * 										Config (In game Stuff)
  * ===============================================================================================================
  * ===============================================================================================================
  */
         @EventHandler
         public void preInit(FMLPreInitializationEvent event) {
         	configHandler.init(event.getSuggestedConfigurationFile());
 
         configHandler.init(event.getSuggestedConfigurationFile());
 		VersionCheck.check();
 		BetaHandler.beta();
 		PowerHandler.loadMap();
 
         }
         
 		@EventHandler
 		public void loadConfiguration(FMLPreInitializationEvent evt){
 	    
 		Crafting.addCrafting(); 
 		RegisterBlock.register();
 		Names.addName();
 		Register.addAchievementLocalizations();
 		ResearchHandler.getOnlineResearch();
 	    NetworkRegistry.instance().registerGuiHandler(this, new GUIHandler());
 	    GameRegistry.addBiome(diseasedBiome);
         EntityRegistry.registerModEntity(EntityZombie_Scientist.class,
         		"Zombie Scientist", 2, this, 80, 3, true);
         
         TickRegistry.registerTickHandler(new TickHandler(), Side.CLIENT);
         
 		}
 		
 		@SideOnly(Side.CLIENT)
 		@EventHandler
 	    public void load(FMLInitializationEvent event) {
 	        ClientProxy ClientProxy = new ClientProxy();
 	        ClientProxy.registerRenderThings();
 		} 
 }
 
  				
 	
