 package blfngl.fallout;
 
 import java.util.Arrays;
 
 import net.minecraft.block.BlockFlower;
 import net.minecraft.block.BlockOre;
 import net.minecraft.block.material.Material;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.item.EnumArmorMaterial;
 import net.minecraft.item.EnumToolMaterial;
 import net.minecraft.item.Item;
 import net.minecraft.potion.Potion;
 import net.minecraft.world.biome.BiomeGenBase;
 import net.minecraftforge.common.EnumHelper;
 import blfngl.fallout.armor.ArmorBaseEnclave;
 import blfngl.fallout.armor.ArmorBaseGecko1;
 import blfngl.fallout.armor.ArmorBaseHellfire;
 import blfngl.fallout.armor.ArmorBaseT45;
 import blfngl.fallout.armor.ArmorBaseT51;
 import blfngl.fallout.armor.ArmorBaseWinter;
 import blfngl.fallout.block.BlockAlien;
 import blfngl.fallout.block.BlockAsbestosDeposit;
 import blfngl.fallout.block.BlockBPGlass;
 import blfngl.fallout.block.BlockCarbonDeposit;
 import blfngl.fallout.block.BlockSaturniteOre;
 import blfngl.fallout.block.BlockSiliconDeposit;
 import blfngl.fallout.block.BlockTechnetiumOre;
 import blfngl.fallout.block.BlockTungstenOre;
 import blfngl.fallout.block.BlockUraniumOre;
 import blfngl.fallout.food.BlockBananaYucca;
 import blfngl.fallout.food.BlockBarrelCactus;
 import blfngl.fallout.food.BlockBrocFlower;
 import blfngl.fallout.food.BlockBuffaloGourd;
 import blfngl.fallout.food.BlockCaveFungus;
 import blfngl.fallout.food.BlockJalapeno;
 import blfngl.fallout.food.BlockXanderRoot;
 import blfngl.fallout.food.ItemFixer;
 import blfngl.fallout.food.ItemHealingPowder;
 import blfngl.fallout.food.ItemHydra;
 import blfngl.fallout.food.ItemJet;
 import blfngl.fallout.food.ItemMedEx;
 import blfngl.fallout.food.ItemNukaQuartz;
 import blfngl.fallout.food.ItemNukaVictory;
 import blfngl.fallout.food.ItemPsycho;
 import blfngl.fallout.food.ItemRocket;
 import blfngl.fallout.food.ItemSlasher;
 import blfngl.fallout.food.ItemStealthBoy;
 import blfngl.fallout.food.ItemStimpak;
 import blfngl.fallout.food.ItemSuperStimpak;
 import blfngl.fallout.food.ItemTurbo;
 import blfngl.fallout.food.ItemWeaponBinding;
 import blfngl.fallout.food.ItembCaveFungus;
 import blfngl.fallout.food.ItembXanderRoot;
 import blfngl.fallout.gun.Gun10mm;
 import blfngl.fallout.gun.Gun127;
 import blfngl.fallout.gun.Gun22;
 import blfngl.fallout.gun.Gun308;
 import blfngl.fallout.gun.Gun357;
 import blfngl.fallout.gun.Gun44;
 import blfngl.fallout.gun.Gun45;
 import blfngl.fallout.gun.Gun50MG;
 import blfngl.fallout.gun.Gun556;
 import blfngl.fallout.gun.Gun5mm;
 import blfngl.fallout.gun.Gun9mm;
 import blfngl.fallout.gun.GunAlien;
 import blfngl.fallout.gun.GunBB;
 import blfngl.fallout.gun.GunECell;
 import blfngl.fallout.gun.GunFuel;
 import blfngl.fallout.gun.GunGauge12;
 import blfngl.fallout.gun.GunGauge20;
 import blfngl.fallout.gun.GunGauss;
 import blfngl.fallout.gun.GunGovt;
 import blfngl.fallout.gun.GunIncinerator;
 import blfngl.fallout.gun.GunMFCell;
 import blfngl.fallout.gun.GunMissile;
 import blfngl.fallout.gun.GunNail;
 import blfngl.fallout.gun.GunRecharger;
 import blfngl.fallout.gun.GunRocket;
 import blfngl.fallout.gun.GunTesla;
 import blfngl.fallout.gun.GunTripleMFCell;
 import blfngl.fallout.handler.ArmorHandler;
 import blfngl.fallout.handler.BiomeHandler;
 import blfngl.fallout.handler.BiomeWasteland;
 import blfngl.fallout.handler.BlockHandler;
 import blfngl.fallout.handler.EntityHandler;
 import blfngl.fallout.handler.FoodHandler;
 import blfngl.fallout.handler.GunHandler;
 import blfngl.fallout.handler.ItemHandler;
 import blfngl.fallout.handler.RecordHandler;
 import blfngl.fallout.handler.ThrowingHandler;
 import blfngl.fallout.handler.WeaponHandler;
 import blfngl.fallout.item.BaseDrink;
 import blfngl.fallout.item.BaseFood;
 import blfngl.fallout.item.BaseItem;
 import blfngl.fallout.item.ItemPipboy;
 import blfngl.fallout.item.ItemSyringe;
 import blfngl.fallout.melee.BaseMelee;
 import blfngl.fallout.melee.WeapBallisticFist;
 import blfngl.fallout.melee.WeapBotE;
 import blfngl.fallout.melee.WeapCosFire;
 import blfngl.fallout.melee.WeapDisplacerGlove;
 import blfngl.fallout.melee.WeapFlamingShish;
 import blfngl.fallout.melee.WeapPowerFist;
 import blfngl.fallout.melee.WeapSatHeatFist;
 import blfngl.fallout.melee.WeapSciGlove;
 import blfngl.fallout.melee.WeapShishkebab;
 import blfngl.fallout.melee.WeapZapGlove;
 import blfngl.fallout.proxy.ClientProxy;
 import blfngl.fallout.proxy.CommonProxy;
 import blfngl.fallout.tab.TabFalloutAmmo;
 import blfngl.fallout.tab.TabFalloutArmor;
 import blfngl.fallout.tab.TabFalloutEnergy;
 import blfngl.fallout.tab.TabFalloutFood;
 import blfngl.fallout.tab.TabFalloutHeavy;
 import blfngl.fallout.tab.TabFalloutMisc;
 import blfngl.fallout.tab.TabFalloutMusic;
 import blfngl.fallout.tab.TabFalloutParts;
 import blfngl.fallout.tab.TabFalloutPistol;
 import blfngl.fallout.tab.TabFalloutRifle;
 import blfngl.fallout.tab.TabFalloutSMG;
 import blfngl.fallout.tab.TabFalloutShotgun;
 import blfngl.fallout.tab.TabFalloutWeap;
 import blfngl.fallout.throwing.ItemThrowingExplosive;
 import cpw.mods.fml.common.Mod;
 import cpw.mods.fml.common.Mod.Init;
 import cpw.mods.fml.common.Mod.Instance;
 import cpw.mods.fml.common.Mod.PostInit;
 import cpw.mods.fml.common.Mod.PreInit;
 import cpw.mods.fml.common.ModMetadata;
 import cpw.mods.fml.common.SidedProxy;
 import cpw.mods.fml.common.event.FMLInitializationEvent;
 import cpw.mods.fml.common.event.FMLPostInitializationEvent;
 import cpw.mods.fml.common.event.FMLPreInitializationEvent;
 import cpw.mods.fml.common.network.NetworkMod;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 
 /**
  * The Fallout Mod
  * 
  * @Author blfngl
  * @License Lesser GNU Public License v3 (http://www.gnu.org/licenses/lgpl.html)
  * The code in this repository, in any form, is the copyright of blfngl
  **/
 
 @Mod(modid = "fallout", name = "The Fallout Mod", version = "MC1.5.2 - TFMv1.7.3")
 @NetworkMod(clientSideRequired = true, serverSideRequired = false)
 
 public class FalloutMain
 {
 
 	@Instance("fallout")
 	public static FalloutMain instance;
 
 	@SidedProxy(clientSide="blfngl.fallout.proxy.ClientProxy", serverSide="blfngl.fallout.proxy.CommonProxy")
 	public static CommonProxy proxy;
 	public static ClientProxy cproxy;
 
 	public static boolean isScoped = false;
 	public static int dimensionAlien = 18;
 
 	public static CreativeTabs TabFalloutArmor = new TabFalloutArmor(CreativeTabs.getNextID(), "TabFalloutArmor");
 	public static CreativeTabs TabFalloutPistol = new TabFalloutPistol(CreativeTabs.getNextID(), "TabFalloutPistol");
 	public static CreativeTabs TabFalloutRifle = new TabFalloutRifle(CreativeTabs.getNextID(), "TabFalloutRifle");
 	public static CreativeTabs TabFalloutSMG = new TabFalloutSMG(CreativeTabs.getNextID(), "TabFalloutSMG");
 	public static CreativeTabs TabFalloutShotgun = new TabFalloutShotgun(CreativeTabs.getNextID(), "TabFalloutShotgun");
 	public static CreativeTabs TabFalloutEnergy = new TabFalloutEnergy(CreativeTabs.getNextID(), "TabFalloutEnergy");
 	public static CreativeTabs TabFalloutHeavy = new TabFalloutHeavy(CreativeTabs.getNextID(), "TabFalloutHeavy");
 	public static CreativeTabs TabFalloutFood = new TabFalloutFood(CreativeTabs.getNextID(), "TabFalloutFood");
 	public static CreativeTabs TabFalloutMisc = new TabFalloutMisc(CreativeTabs.getNextID(), "TabFalloutMisc");
 	public static CreativeTabs TabFalloutWeap = new TabFalloutWeap(CreativeTabs.getNextID(), "TabFalloutWeap");
 	public static CreativeTabs TabFalloutAmmo = new TabFalloutAmmo(CreativeTabs.getNextID(), "TabFalloutAmmo");
 	public static CreativeTabs TabFalloutMusic = new TabFalloutMusic(CreativeTabs.getNextID(), "TabFalloutMusic");
 	public static CreativeTabs TabFalloutParts = new TabFalloutParts(CreativeTabs.getNextID(), "TabFalloutParts");
 
 	//========================================Enums========================================================
 
 	public static EnumToolMaterial SATHEAT = EnumHelper.addToolMaterial("SATHEAT", 0, 595, 10.0F, 8, 27);
 	public static EnumToolMaterial PFIST = EnumHelper.addToolMaterial("PFIST", 0, 395, 10.0F, 7, 25);
 	public static EnumToolMaterial SCI = EnumHelper.addToolMaterial("SCI", 0, 316, 5.0F, 1, 6);
 	public static EnumToolMaterial SAT = EnumHelper.addToolMaterial("SAT", 0, 795, 10.0F, 8, 25);
 	public static EnumToolMaterial CLEAVE = EnumHelper.addToolMaterial("CLEAVE", 0, 995, 10.0F, 3, 27);
 	public static EnumToolMaterial COMKNIFE = EnumHelper.addToolMaterial("COMKNIFE", 0, 995, 5.0F, 4, 27);
 	public static EnumToolMaterial BOTE = EnumHelper.addToolMaterial("BOTE", 0, 1024, 10.0F, 15, 27);
 	public static EnumToolMaterial COS = EnumHelper.addToolMaterial("COS", 0, 995, 10.0F, 10, 27);
 	public static EnumToolMaterial COSFIRE = EnumHelper.addToolMaterial("COSKNIFE", 0, 1024, 10.0F, 12, 27);
 	public static EnumToolMaterial PINT = EnumHelper.addToolMaterial("PINT", 0, 100, 200.0F, 200, 10);
 	public static EnumToolMaterial GUN = EnumHelper.addToolMaterial("GUN", 0, 0, 0.0F, 0, 0);
 
 	public static EnumArmorMaterial T45POWER = EnumHelper.addArmorMaterial("T45POWER", 35, new int[]{2, 6, 5, 2}, 25);
 	public static EnumArmorMaterial ENCLAVE = EnumHelper.addArmorMaterial("ENCLAVE", 37, new int[]{2, 6, 5, 2}, 28);
 	public static EnumArmorMaterial T51POWER = EnumHelper.addArmorMaterial("T51POWER", 39, new int[]{2, 6, 5, 2}, 33);
 	public static EnumArmorMaterial HELLFIRE = EnumHelper.addArmorMaterial("HELLFIRE", 41, new int []{2, 6, 5, 2}, 35);
 	public static EnumArmorMaterial WINTER = EnumHelper.addArmorMaterial("WINTER", 42, new int []{2, 6, 5, 2}, 35);
 
 	//KEY: ID, damage, clip size, reload time, fire time, fire sound, reload sound
 	public static final Item incinerator = new GunIncinerator(509).setUnlocalizedName("Incinerator").setCreativeTab(TabFalloutHeavy);
 	public static final Item rifleLaserRCW = new GunMFCell(510, 3, 60, 2, (int) 0.2, "blfngl.LaserFire", "blfngl.RCWReload", GUN, 0.1F).setUnlocalizedName("LaserRCW").setCreativeTab(TabFalloutEnergy);
 	public static final Item pistolCompliance = new GunECell(511, 2, 30, 2, 1, "blfngl.LaserPistolFire", "blfngl.ComplianceReload", GUN).setUnlocalizedName("Compliance").setCreativeTab(TabFalloutEnergy);
 	public static final Item rifleLaser = new GunMFCell(512, 6, 24, 3, 1, "blfngl.LaserFire", "blfngl.LaserReload", GUN, -1.0F).setUnlocalizedName("LaserRifle").setCreativeTab(TabFalloutEnergy);
 	public static final Item pistol44 = new Gun44(513, 5, 6, 3, 3, "blfngl.44Fire", "blfngl.44Reload", GUN).setUnlocalizedName("Pistol44").setCreativeTab(TabFalloutPistol);
 	public static final Item pistol357 = new Gun357(514, 4, 6, 1, 2, "blfngl.357Fire", "blfngl.357Reload", GUN, -0.5F).setUnlocalizedName("Pistol357").setCreativeTab(TabFalloutPistol);
 	public static final Item pistol22 = new Gun22(515, 2, 12, 2, 1, "blfngl.Silenced22Fire", "blfngl.Silenced22Fire", GUN).setUnlocalizedName("Silenced22").setCreativeTab(TabFalloutPistol);
 	public static final Item rifleSniper = new Gun308(516, 11, 5, 3, 2, "blfngl.SniperFire", "blfngl.SniperReload", GUN).setUnlocalizedName("Sniper308").setCreativeTab(TabFalloutRifle);
 
 	public static final Item BotE = new WeapBotE(517, BOTE).setUnlocalizedName("BotE").setCreativeTab(TabFalloutWeap);
 	public static final Item BumperSword = new WeapBotE(518, EnumToolMaterial.EMERALD).setUnlocalizedName("BumperSword").setCreativeTab(TabFalloutWeap);
 	public static final Item Cleaver = new BaseMelee(519, CLEAVE).setUnlocalizedName("Cleaver").setCreativeTab(TabFalloutWeap);
 	public static final Item ComKnife = new BaseMelee(520, COMKNIFE).setUnlocalizedName("ComKnife").setCreativeTab(TabFalloutWeap);
 	public static final Item CosKnife = new BaseMelee(521, COS).setUnlocalizedName("CosKnife").setCreativeTab(TabFalloutWeap);
 	public static final Item CosClean = new BaseMelee(522, COS).setUnlocalizedName("CosClean").setCreativeTab(TabFalloutWeap);
 	public static final Item CosFire = (new WeapCosFire(523)).setUnlocalizedName("CosFire").setCreativeTab(TabFalloutWeap);
 	public static final Item FireAxe = new BaseMelee(524, EnumToolMaterial.IRON).setUnlocalizedName("FireAxe").setCreativeTab(TabFalloutWeap);
 	public static final Item Knife = new BaseMelee(525, EnumToolMaterial.STONE).setUnlocalizedName("Knife").setCreativeTab(TabFalloutWeap);
 	public static final Item Machete = new BaseMelee(526, EnumToolMaterial.IRON).setUnlocalizedName("Machete").setCreativeTab(TabFalloutWeap);
 	public static final Item Shishkebab = new WeapShishkebab(527, EnumToolMaterial.IRON).setUnlocalizedName("Shishkebab").setCreativeTab(TabFalloutWeap);
 	public static final Item StraightRazor = new BaseMelee(528, EnumToolMaterial.STONE).setUnlocalizedName("StraightRazor").setCreativeTab(TabFalloutWeap);
 	public static final Item Switchblade = new BaseMelee(529, EnumToolMaterial.STONE).setUnlocalizedName("Switchblade").setCreativeTab(TabFalloutWeap);
 	public static final Item PintSizedKnife = new BaseMelee(530, PINT).setUnlocalizedName("PintSizedKnife").setCreativeTab(TabFalloutWeap);
 
 	public static final Item BallisticFist = new WeapBallisticFist(531, EnumToolMaterial.EMERALD).setUnlocalizedName("BallisticFist").setCreativeTab(TabFalloutWeap);
 	public static final Item BladedGauntlet = new BaseMelee(532, EnumToolMaterial.IRON).setUnlocalizedName("BladedGauntlet").setCreativeTab(TabFalloutWeap);
 	public static final Item BoxingGloves = new BaseMelee(533, EnumToolMaterial.WOOD).setUnlocalizedName("BoxingGloves").setCreativeTab(TabFalloutWeap);
 	public static final Item BrassKnuckles = new BaseMelee(534, EnumToolMaterial.STONE).setUnlocalizedName("BrassKnuckles").setCreativeTab(TabFalloutWeap);
 	public static final Item DisplacerGlove = new WeapDisplacerGlove(535, PFIST).setUnlocalizedName("DisplacerGlove").setCreativeTab(TabFalloutWeap);
 	public static final Item GoldenGloves = new BaseMelee(536, EnumToolMaterial.GOLD).setUnlocalizedName("GoldenGloves").setCreativeTab(TabFalloutWeap);
 	public static final Item PowerFist = new WeapPowerFist(537, EnumToolMaterial.EMERALD).setUnlocalizedName("PowerFist").setCreativeTab(TabFalloutWeap);
 	public static final Item SatFist = new WeapPowerFist(538, EnumToolMaterial.EMERALD).setUnlocalizedName("SatFist").setCreativeTab(TabFalloutWeap);
 	public static final Item SatHeatFist = new WeapSatHeatFist(539, PFIST).setUnlocalizedName("SatHeatFist").setCreativeTab(TabFalloutWeap);
 	public static final Item SciGlove = new WeapSciGlove(540, SCI).setUnlocalizedName("SciGlove").setCreativeTab(TabFalloutWeap);
 	public static final Item ZapGlove = new WeapZapGlove(541, EnumToolMaterial.IRON).setUnlocalizedName("ZapGlove").setCreativeTab(TabFalloutWeap);
 
 	public static final Item chunkTungsten = (new BaseItem(400)).setUnlocalizedName("TungstenRod").setCreativeTab(TabFalloutMisc);
 	public static final Item ingotTungsten = (new BaseItem(401)).setUnlocalizedName("TungstenIngot").setCreativeTab(TabFalloutMisc);
 	public static final Item chunkTechnetium = (new BaseItem(402)).setUnlocalizedName("TechnetiumChunk").setCreativeTab(TabFalloutMisc);
 	public static final Item ingotTechnetium = (new BaseItem(403)).setUnlocalizedName("TechnetiumIngot").setCreativeTab(TabFalloutMisc);
 	public static final Item chunkSilicon = (new BaseItem(404)).setUnlocalizedName("SiliconChunk").setCreativeTab(TabFalloutMisc);
 	public static final Item barSilicon = (new BaseItem(405)).setUnlocalizedName("SiliconBar").setCreativeTab(TabFalloutMisc);
 	public static final Item tungstenFilter = (new BaseItem(406)).setUnlocalizedName("TungstenFilter").setCreativeTab(TabFalloutMisc);
 	public static final Item chunkAsbestos = (new BaseItem(407)).setUnlocalizedName("AsbestosChunk").setCreativeTab(TabFalloutMisc);
 	public static final Item barAsbestos = (new BaseItem(408)).setUnlocalizedName("AsbestosBar").setCreativeTab(TabFalloutMisc);
 	public static final Item ingotUranium = (new BaseItem(409)).setUnlocalizedName("UraniumIngot").setCreativeTab(TabFalloutMisc);
 	public static final Item tungstenPlate = (new BaseItem(410)).setUnlocalizedName("TungstenPlate").setCreativeTab(TabFalloutMisc);
 	public static final Item technetiumPlate = (new BaseItem(411)).setUnlocalizedName("TechnetiumPlate").setCreativeTab(TabFalloutMisc);
 	public static final Item RAP = (new BaseItem(412)).setUnlocalizedName("RAP").setCreativeTab(CreativeTabs.tabMaterials).setCreativeTab(TabFalloutMisc);
 	public static final Item cellMF = (new BaseItem(413)).setUnlocalizedName("MFCell").setCreativeTab(TabFalloutAmmo);
 	public static final Item MFPack = (new BaseItem(414)).setUnlocalizedName("MFPack").setCreativeTab(TabFalloutMisc);
 	public static final Item carbon = (new BaseItem(415)).setUnlocalizedName("Carbon").setCreativeTab(TabFalloutMisc);
 	public static final Item teflon = (new BaseItem(416)).setUnlocalizedName("Teflon").setCreativeTab(TabFalloutMisc);
 	public static final Item polyethylene = (new BaseItem(417)).setUnlocalizedName("Polyethylene").setCreativeTab(TabFalloutMisc);
 	public static final Item bottleCap = (new BaseItem(418)).setUnlocalizedName("BottleCap").setCreativeTab(TabFalloutMisc);
 	public static final Item fan = (new BaseItem(419)).setUnlocalizedName("Fan").setCreativeTab(TabFalloutMisc);
 	public static final Item gear = (new BaseItem(420)).setUnlocalizedName("Gear").setCreativeTab(TabFalloutMisc);
 	public static final Item gyro = (new BaseItem(421)).setUnlocalizedName("Gyro").setCreativeTab(TabFalloutMisc);
 	public static final Item MASM = (new BaseItem(422)).setUnlocalizedName("MASM").setCreativeTab(TabFalloutMisc);
 	public static final Item TXMF = (new BaseItem(423)).setUnlocalizedName("TXMF").setCreativeTab(TabFalloutMisc);
 	public static final Item card = (new BaseItem(424)).setUnlocalizedName("Card").setCreativeTab(TabFalloutMisc);
 	public static final Item deck = (new BaseItem(425)).setUnlocalizedName("Deck").setCreativeTab(TabFalloutMisc);
 	public static final Item cardboard = (new BaseItem(426)).setUnlocalizedName("Cardboard").setCreativeTab(TabFalloutMisc);
 	public static final Item tinCan = (new BaseItem(427)).setUnlocalizedName("TinCan").setCreativeTab(TabFalloutMisc);
 	public static final Item chunkSaturnite = (new BaseItem(428)).setUnlocalizedName("SatRod").setCreativeTab(TabFalloutMisc);
 	public static final Item ingotSaturnite = (new BaseItem(429)).setUnlocalizedName("SatIngot").setCreativeTab(TabFalloutMisc);
 	public static final Item saturniteAlloy = (new BaseItem(430)).setUnlocalizedName("SatAlloy").setCreativeTab(TabFalloutMisc);
 	public static final Item syringeEmpty = (new ItemSyringe(431)).setUnlocalizedName("EmptySyringe").setCreativeTab(TabFalloutMisc);
 	public static final Item syringeBloody = (new BaseItem(432)).setUnlocalizedName("BloodSyringe").setCreativeTab(TabFalloutMisc);
 	public static final Item powerTorso  = (new BaseItem(433)).setUnlocalizedName("PTorso").setCreativeTab(TabFalloutMisc);
 	public static final Item powerShoulders  = (new BaseItem(434)).setUnlocalizedName("PShoulders").setCreativeTab(TabFalloutMisc);
 	public static final Item powerChest = (new BaseItem(435)).setUnlocalizedName("PChest").setCreativeTab(TabFalloutMisc);
 	public static final Item upgradeT45 = (new BaseItem(436)).setUnlocalizedName("T45Upgrade").setCreativeTab(TabFalloutMisc);
 	public static final Item upgradeEnclave = (new BaseItem(437)).setUnlocalizedName("EnclaveUpgrade").setCreativeTab(TabFalloutMisc);	
 	public static final Item upgradeT51 = (new BaseItem(438)).setUnlocalizedName("T51Upgrade").setCreativeTab(TabFalloutMisc);
 	public static final Item upgradeHellfire = (new BaseItem(439)).setUnlocalizedName("HellfireUpgrade").setCreativeTab(TabFalloutMisc);
 	public static final Item upgradeWinterized = (new BaseItem(440)).setUnlocalizedName("WinterizedUpgrade").setCreativeTab(TabFalloutMisc);
 	public static final Item nukaBottle = (new BaseItem(441)).setUnlocalizedName("NukaBottle").setCreativeTab(TabFalloutMisc);
 	public static final Item abraxo = (new BaseItem(442)).setUnlocalizedName("Abraxo").setCreativeTab(TabFalloutMisc);
 	public static final Item a22LR = (new BaseItem(443)).setUnlocalizedName("22LRRound").setCreativeTab(TabFalloutAmmo);
 	public static final Item a308 = (new BaseItem(444)).setUnlocalizedName("308Round").setCreativeTab(TabFalloutAmmo);
 	public static final Item a357 = (new BaseItem(445)).setUnlocalizedName("357Round").setCreativeTab(TabFalloutAmmo);
 	public static final Item a44 = (new BaseItem(446)).setUnlocalizedName("44Round").setCreativeTab(TabFalloutAmmo);
 	public static final Item scrapMetal = (new BaseItem(447)).setUnlocalizedName("ScrapMetal").setCreativeTab(TabFalloutMisc);
 	public static final Item cellEnergy = (new BaseItem(448)).setUnlocalizedName("ECell").setCreativeTab(TabFalloutAmmo);
 	public static final Item homemadeFuel = (new BaseItem(449)).setUnlocalizedName("HomemadeFuel").setCreativeTab(TabFalloutAmmo);
 	public static final Item cellElectron = (new BaseItem(450)).setUnlocalizedName("EPack").setCreativeTab(TabFalloutAmmo);
 	public static final Item leatherBelt = (new BaseItem(451)).setUnlocalizedName("LeatherBelt").setCreativeTab(TabFalloutMisc);
 	public static final Item bottleCapMine = (new ItemThrowingExplosive(452, 5, 6, 3, 3, "random.bow", "blfngl.44Reload", GUN)).setUnlocalizedName("BottleCapMine").setCreativeTab(TabFalloutMisc);
 	public static final Item grenadeFrag = (new ItemThrowingExplosive(453, 5, 6, 3, 3, "random.bow", "blfngl.44Reload", GUN)).setUnlocalizedName("FragGrenade").setCreativeTab(TabFalloutMisc);
 	public static final Item lunchbox = (new BaseItem(454)).setUnlocalizedName("Lunchbox").setCreativeTab(TabFalloutMisc);
 	public static final Item sensorModule = (new BaseItem(455)).setUnlocalizedName("SensorModule").setCreativeTab(TabFalloutMisc);
 	public static final Item cherrybomb = (new BaseItem(456)).setUnlocalizedName("Cherrybomb").setCreativeTab(TabFalloutMisc);
 	public static final Item nightstalkerBlood = (new BaseItem(457)).setUnlocalizedName("NightstalkerBlood").setCreativeTab(TabFalloutMisc);
 	public static final Item jetInhaler = (new BaseItem(458)).setUnlocalizedName("JetInhaler").setCreativeTab(TabFalloutMisc);
 	public static final Item wonderglue = (new BaseItem(459)).setUnlocalizedName("Wonderglue").setCreativeTab(TabFalloutMisc);
 	public static final Item turpentine = (new BaseItem(460)).setUnlocalizedName("Turpentine").setCreativeTab(TabFalloutMisc);
 	public static final Item geckoHide = (new BaseItem(461)).setUnlocalizedName("GeckoHide").setCreativeTab(TabFalloutMisc);
 
 	//============================================Food and Chems==========================================================
 
 	public static final Item Stimpak = new ItemStimpak(462).setUnlocalizedName("Stimpak").setCreativeTab(TabFalloutFood);
 	public static final Item SuperStimpak = new ItemSuperStimpak(463).setUnlocalizedName("SuperStimpak").setCreativeTab(TabFalloutFood);
 	public static final Item BCMAC = new BaseFood(464, 5, 0.3F, false).setUnlocalizedName("BCMAC").setCreativeTab(TabFalloutFood);
 	public static final Item Cram = new BaseFood(465, 5, 0.3F, false).setUnlocalizedName("Cram").setCreativeTab(TabFalloutFood);
 	public static final Item Instamash = new BaseFood(466, 5, 0.3F, false).setUnlocalizedName("Instamash").setCreativeTab(TabFalloutFood);
 	public static final Item PorkBeans = new BaseFood(467, 5, 0.3F, false).setUnlocalizedName("PorkBeans").setCreativeTab(TabFalloutFood);
 	public static final Item Salisbury = new BaseFood(468, 5, 0.3F, false).setUnlocalizedName("Salisbury").setCreativeTab(TabFalloutFood);
 	public static final Item YumYum = new BaseFood(469, 5, 0.3F, false).setUnlocalizedName("YumYum").setCreativeTab(TabFalloutFood);
 	public static final Item Psycho = new ItemPsycho(470).setUnlocalizedName("Psycho").setCreativeTab(TabFalloutFood);
 	public static final Item MedEx = new ItemMedEx(471).setUnlocalizedName("MedEx").setCreativeTab(TabFalloutFood);
 	public static final Item Slasher = new ItemSlasher(472).setUnlocalizedName("Slasher").setCreativeTab(TabFalloutFood);
 	public static final Item Hydra = new ItemHydra(473).setUnlocalizedName("Hydra").setCreativeTab(TabFalloutFood);
 	public static final Item NukaCola = new BaseFood(474, 7, 0.3F, false).setUnlocalizedName("NukaCola").setCreativeTab(TabFalloutFood);
 	public static final Item HealingPowder = new ItemHealingPowder(475).setUnlocalizedName("HealingPowder").setCreativeTab(TabFalloutFood);
 	public static final Item bBarrelCactus = new BaseFood(476, 8, 0.3F, false).setUnlocalizedName("bBarrelCactus").setCreativeTab(TabFalloutFood);
 	public static final Item bBrocFlower = new BaseFood(477, 8, 0.3F, false).setUnlocalizedName("bBrocFlower").setCreativeTab(TabFalloutFood);
 	public static final Item bXanderRoot = new ItembXanderRoot(478, 8, 0.3F, false).setUnlocalizedName("bXanderRoot").setCreativeTab(TabFalloutFood);
 	public static final Item AntEgg = new BaseFood(479, 5, 0.3F, false).setUnlocalizedName("AntEgg").setCreativeTab(TabFalloutFood);
 	public static final Item AntMeat = new BaseFood(480, 6, 0.3F, true).setUnlocalizedName("AntMeat").setCreativeTab(TabFalloutFood);
 	public static final Item BighornerSteak = new BaseFood(481, 10, 0.3F, true).setUnlocalizedName("BighornerSteak").setCreativeTab(TabFalloutFood);
 	public static final Item BloodSausage = new BaseFood(482, 16, 0.3F, true).setUnlocalizedName("BloodSausage").setCreativeTab(TabFalloutFood);
 	public static final Item BlackBloodSausage = new BaseFood(483, 20, 0.3F, true).setUnlocalizedName("BlackBloodSausage").setCreativeTab(TabFalloutFood);
 	public static final Item BighornerMeat = new BaseFood(484, 4, 0.3F, true).setUnlocalizedName("BighornerMeat").setCreativeTab(TabFalloutFood);
 	public static final Item BrahminMeat = new BaseFood(485, 3, 0.3F, true).setUnlocalizedName("BrahminMeat").setCreativeTab(TabFalloutFood);
 	public static final Item BrahminSteak = new BaseFood(486, 8, 0.3F, true).setUnlocalizedName("BrahminSteak").setCreativeTab(TabFalloutFood);
 	public static final Item BrahminWellington = new BaseFood(487, 14, 0.3F, true).setUnlocalizedName("BrahminWellington").setCreativeTab(TabFalloutFood);
 	public static final Item FireAntFricasse = new BaseFood(488, 12, 0.3F, false).setPotionEffect(Potion.fireResistance.id, 60, 0, 1F).setUnlocalizedName("FireAntFricasse").setCreativeTab(TabFalloutFood);
 	public static final Item CaravanLunch = new BaseFood(489, 20, 0.3F, false).setUnlocalizedName("CaravanLunch").setCreativeTab(TabFalloutFood);
 	public static final Item DandyApples = new BaseFood(490, 5, 0.3F, false).setUnlocalizedName("DandyApples").setCreativeTab(TabFalloutFood);
 	public static final Item NightstalkerTail = new BaseFood(491, 6, 0.3F, false).setUnlocalizedName("NightstalkerTail").setCreativeTab(TabFalloutFood);
 	public static final Item TrailMix = new BaseFood(492, 12, 0.3F, false).setUnlocalizedName("TrailMix").setCreativeTab(TabFalloutFood);
 	public static final Item SugarBombs = new BaseFood(493, 8, 0.3F, false).setUnlocalizedName("SugarBombs").setCreativeTab(TabFalloutFood);
 	public static final Item PotatoChips = new BaseFood(494, 5, 0.3F, false).setUnlocalizedName("PotatoChips").setCreativeTab(TabFalloutFood);
 	public static final Item SunsetSass = new BaseDrink(495, 5, 0.3F, false).setUnlocalizedName("SunsetSass").setCreativeTab(TabFalloutFood);
 	public static final Item bBananaYucca = new BaseFood(496, 6, 0.3F, false).setUnlocalizedName("bBananaYucca").setCreativeTab(TabFalloutFood);
 	public static final Item bCaveFungus = new ItembCaveFungus(497, 6, 0.3F, false).setUnlocalizedName("bCaveFungus").setCreativeTab(TabFalloutFood);
 	public static final Item bBuffaloGourd = new BaseFood(498, 4, 0.3F, false).setUnlocalizedName("bBuffaloGourd").setCreativeTab(TabFalloutFood);
 	public static final Item bJalapeno = new BaseFood(499, 4, 0.3F, false).setUnlocalizedName("bJalapeno").setCreativeTab(TabFalloutFood);
 	public static final Item Jet = new ItemJet(500).setUnlocalizedName("Jet").setCreativeTab(TabFalloutFood);
 	public static final Item Rocket = new ItemRocket(501).setUnlocalizedName("Rocket").setCreativeTab(TabFalloutFood);
 	public static final Item NukaQuartz = new ItemNukaQuartz(502, 1, false).setUnlocalizedName("NukaQuartz").setCreativeTab(TabFalloutFood);
 	public static final Item NukaCold = new BaseDrink(503, 1, 0.3F, false).setUnlocalizedName("NukaCold").setCreativeTab(TabFalloutFood);
 	public static final Item NukaVictory = new ItemNukaVictory(504, 1, false).setPotionEffect(Potion.digSpeed.id, 60, 0, 1F).setPotionEffect(Potion.moveSpeed.id, 60, 0, 1F).setUnlocalizedName("NukaVictory").setCreativeTab(TabFalloutFood);
 	public static final Item WeaponBinding = new ItemWeaponBinding(505, 1, false).setUnlocalizedName("WeaponBinding").setCreativeTab(TabFalloutFood);
 	public static final Item Vodka = new BaseDrink(506, 1, 0.3F, false).setPotionEffect(Potion.damageBoost.id, 60, 0, 1F).setPotionEffect(Potion.confusion.id, 60, 0, 1F).setPotionEffect(Potion.poison.id, 60, 0 , 1F).setUnlocalizedName("Vodka").setCreativeTab(TabFalloutFood);
 	public static final Item StealthBoy = new ItemStealthBoy(507).setUnlocalizedName("StealthBoy").setCreativeTab(TabFalloutFood);
 	public static final Item Turbo = new ItemTurbo(508).setUnlocalizedName("Turbo").setCreativeTab(TabFalloutFood);
 
 	//============================================Armor==========================================================
 
 	public static final Item T45Helm = (new ArmorBaseT45(542, T45POWER, 1, 0)).setUnlocalizedName("T45Helm").setCreativeTab(TabFalloutArmor);
 	public static final Item T45Chest = (new ArmorBaseT45(543, T45POWER, 1, 1)).setUnlocalizedName("T45Chest").setCreativeTab(TabFalloutArmor);
 	public static final Item T45Legs = (new ArmorBaseT45(544, T45POWER, 1, 2)).setUnlocalizedName("T45Legs").setCreativeTab(TabFalloutArmor);
 	public static final Item T45Boots = (new ArmorBaseT45(545, T45POWER, 1, 3)).setUnlocalizedName("T45Boots").setCreativeTab(TabFalloutArmor);
 
 	public static final Item T51Helm = (new ArmorBaseT51(546, T51POWER, 1, 0)).setUnlocalizedName("T51Helm").setCreativeTab(TabFalloutArmor);
 	public static final Item T51Chest = (new ArmorBaseT51(547, T51POWER, 1, 1)).setUnlocalizedName("T51Chest").setCreativeTab(TabFalloutArmor);
 	public static final Item T51Legs = (new ArmorBaseT51(548, T51POWER, 1, 2)).setUnlocalizedName("T51Legs").setCreativeTab(TabFalloutArmor);
 	public static final Item T51Boots = (new ArmorBaseT51(549, T51POWER, 1, 3)).setUnlocalizedName("T51Boots").setCreativeTab(TabFalloutArmor);
 
 	public static final Item G1Helm = (new ArmorBaseGecko1(550, EnumArmorMaterial.CHAIN, 1, 0)).setUnlocalizedName("G1Helm").setCreativeTab(TabFalloutArmor);
 	public static final Item G1Chest = (new ArmorBaseGecko1(551, EnumArmorMaterial.CHAIN, 1, 1)).setUnlocalizedName("G1Chest").setCreativeTab(TabFalloutArmor);
 	public static final Item G1Legs = (new ArmorBaseGecko1(552, EnumArmorMaterial.CHAIN, 1, 2)).setUnlocalizedName("G1Legs").setCreativeTab(TabFalloutArmor);
 	public static final Item G1Boots = (new ArmorBaseGecko1(553, EnumArmorMaterial.CHAIN, 1, 3)).setUnlocalizedName("G1Boots").setCreativeTab(TabFalloutArmor);
 
 	public static final Item HellHelm = (new ArmorBaseHellfire(554, HELLFIRE, 1, 0)).setUnlocalizedName("HellHelm").setCreativeTab(TabFalloutArmor);
 	public static final Item HellChest = (new ArmorBaseHellfire(555, HELLFIRE, 1, 1)).setUnlocalizedName("HellChest").setCreativeTab(TabFalloutArmor);
 	public static final Item HellLegs = (new ArmorBaseHellfire(556, HELLFIRE, 1, 2)).setUnlocalizedName("HellLegs").setCreativeTab(TabFalloutArmor);
 	public static final Item HellBoots = (new ArmorBaseHellfire(557, HELLFIRE, 1, 3)).setUnlocalizedName("HellBoots").setCreativeTab(TabFalloutArmor);
 
 	//Skins creds: BillSkins
 	public static final Item WinterHelm = (new ArmorBaseWinter(558, WINTER, 1, 0)).setUnlocalizedName("WinterHelm").setCreativeTab(TabFalloutArmor);
 	public static final Item WinterChest = (new ArmorBaseWinter(559, WINTER, 1, 1)).setUnlocalizedName("WinterChest").setCreativeTab(TabFalloutArmor);
 	public static final Item WinterLegs = (new ArmorBaseWinter(560, WINTER, 1, 2)).setUnlocalizedName("WinterLegs").setCreativeTab(TabFalloutArmor);
 	public static final Item WinterBoots = (new ArmorBaseWinter(561, WINTER, 1, 3)).setUnlocalizedName("WinterBoots").setCreativeTab(TabFalloutArmor);
 
 	public static final Item EnclaveHelm = (new ArmorBaseEnclave(562, ENCLAVE, 1, 0)).setUnlocalizedName("EnclaveHelm").setCreativeTab(TabFalloutArmor);
 	public static final Item EnclaveChest = (new ArmorBaseEnclave(563, ENCLAVE, 1, 1)).setUnlocalizedName("EnclaveChest").setCreativeTab(TabFalloutArmor);
 	public static final Item EnclaveLegs = (new ArmorBaseEnclave(564, ENCLAVE, 1, 2)).setUnlocalizedName("EnclaveLegs").setCreativeTab(TabFalloutArmor);
 	public static final Item EnclaveBoots = (new ArmorBaseEnclave(565, ENCLAVE, 1, 3)).setUnlocalizedName("EnclaveBoots").setCreativeTab(TabFalloutArmor);
 
 	//============================================Blocks=========================================================
 
 	public static final BlockOre TungstenOre = (new BlockTungstenOre(170, 0, Material.rock));
 	public static final BlockOre TechnetiumOre = (new BlockTechnetiumOre(171, 1, Material.rock));
 	public static final BlockOre SaturniteOre = (new BlockSaturniteOre(172, 2, Material.rock));
 	public static final BlockFlower BrocFlower = (BlockFlower) new BlockBrocFlower(174, 3).setUnlocalizedName("BrocFlower");
 	public static final BlockOre BPGlass = (new BlockBPGlass(173, 4, Material.rock));
 	public static final BlockFlower XanderRoot = (BlockFlower) new BlockXanderRoot(175 ,5).setUnlocalizedName("XanderRoot");
 	public static final BlockOre UraniumOre = (new BlockUraniumOre(176,7, Material.rock));
 	public static final BlockOre SiliconDeposit = (new BlockSiliconDeposit(177, 6, Material.rock));
 	public static final BlockOre CarbonDeposit = (new BlockCarbonDeposit(178, 9, Material.rock));
 	public static final BlockOre AsbestosDeposit = (new BlockAsbestosDeposit(179, 10, Material.rock));
 	public static final BlockFlower BarrelCactus = (BlockFlower) new BlockBarrelCactus(180, 11).setUnlocalizedName("BarrelCactus");
 	public static final BlockFlower BananaYucca = (BlockFlower) new BlockBananaYucca(181, 15).setUnlocalizedName("BananaYucca");
 	public static final BlockFlower CaveFungus = (BlockFlower) new BlockCaveFungus(182, 29).setUnlocalizedName("CaveFungus");
 	public static final BlockFlower BuffaloGourd = (BlockFlower) new BlockBuffaloGourd(183, 13).setUnlocalizedName("BuffaloGourd");
 	public static final BlockFlower Jalapeno = (BlockFlower) new BlockJalapeno(184, 14).setUnlocalizedName("Jalapeno");
 	//public static final Block benchReload = new BlockReloadBench(185).setCreativeTab(CreativeTabs.tabBlock);
 	public static final BlockOre blockAlien = (BlockOre) new BlockAlien(185, 15, Material.rock).setUnlocalizedName("AlienBlock");
 
 	public static final BiomeGenBase Wasteland = (new BiomeWasteland(100)).setColor(16421912).setBiomeName("Wasteland").setDisableRain().setTemperatureRainfall(2.0F, 0.0F).setMinMaxHeight(0.1F, 0.2F);
 
 	//TODO Version 1.7
 	public static final Item grip = new BaseItem(566).setUnlocalizedName("Grip").setCreativeTab(TabFalloutPistol);
 	public static final Item barrel = new BaseItem(567).setUnlocalizedName("Barrel").setCreativeTab(TabFalloutPistol);
 	public static final Item stockWood = new BaseItem(568).setUnlocalizedName("Stock").setCreativeTab(TabFalloutPistol);
 	public static final Item magnifier = new BaseItem(569).setUnlocalizedName("Magnifier").setCreativeTab(TabFalloutPistol);
 	public static final Item hammer = new BaseItem(570).setUnlocalizedName("Hammer").setCreativeTab(TabFalloutPistol);
 	public static final Item gasTank = new BaseItem(571).setUnlocalizedName("GasTank").setCreativeTab(TabFalloutMisc);
 
 	//TODO Version 1.7.1
 	//KEY: ID, damage, clip size, reload time, fire time, fire sound, reload sound
 	public static final Item riflePlasma = new GunMFCell(572, 7, 24, 2, 2, "blfngl.PlasmaRifleFire", "Blfngl.PlasmaRifleReload", GUN, 3).setUnlocalizedName("PlasmaRifle").setCreativeTab(TabFalloutEnergy);
 	public static final Item magRail = new BaseItem(573).setUnlocalizedName("MagRail").setCreativeTab(TabFalloutMisc);
 	public static final Item pistol10mm = new Gun10mm(574, 3, 12, 1, 1, "blfngl.10MMFire", "blfngl.10MMReload", GUN).setUnlocalizedName("10mmPistol").setCreativeTab(TabFalloutPistol);
 	public static final Item a10mm = new BaseItem(575).setUnlocalizedName("10mmRound").setCreativeTab(TabFalloutAmmo);
 	public static final Item shiskebabFlaming = new WeapFlamingShish(576).setUnlocalizedName("Shishkebabf").setCreativeTab(TabFalloutWeap);
 	public static final Item rifleGauss = new GunGauss(577, 12, 1, 4, 3, "blfngl.GaussFire", "blfngl.GaussReload", GUN).setUnlocalizedName("GaussRifle").setCreativeTab(TabFalloutEnergy);
 	public static final Item bubbleGum = new BaseFood(578, 1, 0.3F, false).setUnlocalizedName("BubbleGum").setCreativeTab(TabFalloutFood);
 	public static final Item beer = new BaseDrink(579, 2, 0.3F, false).setUnlocalizedName("Beer").setCreativeTab(TabFalloutFood);
 	public static final Item fiendStew = new BaseFood(580, 12, 0.3F, false).setUnlocalizedName("FiendStew").setCreativeTab(TabFalloutFood);
 	public static final Item shotgunCombat = new GunGauge12(581, 7, 12, 2, 2, "blfngl.ShotgunFire", "blfngl.ShotgunReload", GUN).setUnlocalizedName("CombatShotgun").setCreativeTab(TabFalloutRifle);
 	public static final Item shotgunRiot = new GunGauge12(582, 7, 12, 2, 1, "blfngl.HuntingShotgunFire", "blfngl.ShotgunReload", GUN).setUnlocalizedName("RiotShotgun").setCreativeTab(TabFalloutRifle);
 	public static final Item aGauge12 = new BaseItem(583).setUnlocalizedName("Gauge12").setCreativeTab(TabFalloutMisc);
 	public static final Item powderRifle = new BaseItem(584).setUnlocalizedName("PowderRifle").setCreativeTab(TabFalloutMisc);
 	public static final Item primerRifle = new BaseItem(585).setUnlocalizedName("PrimerRifle").setCreativeTab(TabFalloutMisc);
 	public static final Item lead = new BaseItem(586).setUnlocalizedName("Lead").setCreativeTab(TabFalloutMisc);
 	public static final Item powderPistol = new BaseItem(587).setUnlocalizedName("PowderPistol").setCreativeTab(TabFalloutMisc);
 	public static final Item primerPistol = new BaseItem(588).setUnlocalizedName("PrimerPistol").setCreativeTab(TabFalloutMisc);
 
 	//TODO version 1.7.2
 	//KEY: ID, damage, clip size, reload time, fire time, fire sound, reload sound
 	public static final Item rifleTriBeam = new GunTripleMFCell(589, 10, 24, 2, 4, "blfngl.TriBeamFire", "Blfngl.TriBeamReload", GUN).setUnlocalizedName("TriBeamRifle").setCreativeTab(TabFalloutEnergy);
 	public static final Item rifleMultiplas = new GunTripleMFCell(590, 13, 30, 3, 4, "blfngl.MultiplasFire", "Blfngl.PlasmaRifleReload", GUN).setUnlocalizedName("MultiplasRifle").setCreativeTab(TabFalloutEnergy);
 	public static final Item rifleLAER = new GunMFCell(591, 8, 20, 3, 2, "blfngl.PulseFire", "Blfngl.PlasmaRifleReload", GUN, 3).setUnlocalizedName("LAER").setCreativeTab(TabFalloutEnergy);
 	public static final Item rifleHolo = new GunMFCell(592, 13, 4, 2, 3, "blfngl.HoloFire", "Blfngl.PlasmaRifleReload", GUN, 3).setUnlocalizedName("Holorifle").setCreativeTab(TabFalloutEnergy);
 	public static final Item minigun = new Gun5mm(593, 3, 240, 4, (int) 0.3, "blfngl.MinigunFire", "Blfngl.MinigunReload", GUN, -0.3F).setUnlocalizedName("Minigun").setCreativeTab(TabFalloutHeavy);
 	public static final Item cyberdog = new Gun357(594, 3, 50, 4, (int) 0.5, "blfngl.357Fire", "Blfngl.MinigunReload", GUN, -0.3F).setUnlocalizedName("CyberdogGun").setCreativeTab(TabFalloutHeavy);
 	public static final Item FIDO = new Gun44(595, 4, 50, 4, (int) 0.5, "blfngl.44Fire", "Blfngl.MinigunReload", GUN).setUnlocalizedName("FIDO").setCreativeTab(TabFalloutHeavy);
 	public static final Item plasmaCaster = new GunMFCell(596, 14, 10, 3, 4, "blfngl.PlasmaCasterFire", "Blfngl.MinigunReload", GUN, 3).setUnlocalizedName("PlasmaCaster").setCreativeTab(TabFalloutHeavy);
 	public static final Item missileLauncher = new GunMissile(597, 14, 1, 4, 2, "blfngl.MissileFire", "Blfngl.MissileReload", GUN).setUnlocalizedName("MissileLauncher").setCreativeTab(TabFalloutHeavy);
 	public static final Item redGlare = new GunRocket(598, 5, 13, 3, 2, "blfngl.RedGlareFire", "Blfngl.RedGlareReload", GUN).setUnlocalizedName("RedGlare").setCreativeTab(TabFalloutHeavy);
 	public static final Item pistol556 = new Gun556(599, 5, 5, 2, 1, "blfngl.Pistol556Fire", "Blfngl.Pistol556Reload", GUN, -0.5F).setUnlocalizedName("556Pistol").setCreativeTab(TabFalloutPistol);
 	public static final Item pistolThatGun = new Gun556(600, 6, 7, 3, 1, "blfngl.Pistol556Fire", "Blfngl.Pistol556Reload", GUN, 0.5F).setUnlocalizedName("ThatGun").setCreativeTab(TabFalloutPistol);
 	public static final Item pistol127 = new Gun127(601, 7, 7, 2, 1, "blfngl.44Fire", "Blfngl.9mmReload", GUN).setUnlocalizedName("127Pistol").setCreativeTab(TabFalloutPistol);
 	public static final Item pistolLaser = new GunECell(602, 4, 24, 3, 1, "blfngl.LaserPistolFire", "Blfngl.PlasmaRifleReload", GUN).setUnlocalizedName("LaserPistol").setCreativeTab(TabFalloutEnergy);
 	public static final Item pistolDefender = new GunECell(603, 6, 32, 2, 2, "blfngl.DefenderFire", "Blfngl.PlasmaPistolReload", GUN).setUnlocalizedName("PlasmaDefender").setCreativeTab(TabFalloutEnergy);
 	public static final Item pistolPlasma = new GunECell(604, 5, 32, 2, 1, "blfngl.PlasmaPistolFire", "Blfngl.PlasmaPistolReload", GUN).setUnlocalizedName("PlasmaPistol").setCreativeTab(TabFalloutEnergy);
 	public static final Item teslaCannon = new GunTesla(605, 13, 5, 4, 3, "blfngl.TeslaFire", "Blfngl.TeslaReload", GUN).setUnlocalizedName("TeslaCannon").setCreativeTab(TabFalloutHeavy);
 	public static final Item rifleRecharger = new GunRecharger(606, 3, 7, 3, 2, "blfngl.RechargerFire", "Blfngl.PlasmaRifleReload", GUN).setUnlocalizedName("RechargerRifle").setCreativeTab(TabFalloutEnergy);
 	public static final Item pistolRecharger = new GunRecharger(607, 4, 20, 3, 1, "blfngl.RechargerFire", "Blfngl.PlasmaRifleReload", GUN).setUnlocalizedName("RechargerPistol").setCreativeTab(TabFalloutEnergy);
 	public static final Item aRocket = new BaseItem(608).setUnlocalizedName("AmmoRocket").setCreativeTab(TabFalloutAmmo);
 	public static final Item aMissile = new BaseItem(609).setUnlocalizedName("Missile").setCreativeTab(TabFalloutAmmo);
 	public static final Item a127 = new BaseItem(610).setUnlocalizedName("127mmRound").setCreativeTab(TabFalloutAmmo);
 	public static final Item a556 = new BaseItem(611).setUnlocalizedName("556mmRound").setCreativeTab(TabFalloutAmmo);
 	public static final Item a5mm = new BaseItem(612).setUnlocalizedName("5mmRound").setCreativeTab(TabFalloutAmmo);
 
 	//KEY: ID, damage, clip size, reload time, fire time, fire sound, reload sound
 	public static final Item shotgunHunting = new GunGauge12(613, 13, 7, 3, 4, "blfngl.HuntingShotgunFire", "Blfngl.HuntingShotgunReload", GUN).setUnlocalizedName("HuntingShotgun").setCreativeTab(TabFalloutShotgun);
 	public static final Item shotgunDinnerBell = new GunGauge12(614, 13, 7, 3, 4, "blfngl.HuntingShotgunFire", "Blfngl.HuntingShotgunReload", GUN).setUnlocalizedName("DinnerBell").setCreativeTab(TabFalloutShotgun);
 	public static final Item pistol9mm = new Gun9mm(615, 2, 13, 3, 1, "blfngl.9mmFire", "Blfngl.9mmReload", GUN).setUnlocalizedName("9mmPistol").setCreativeTab(TabFalloutPistol);
 	public static final Item pistolAlienBlaster = new GunAlien(616, 13, 10, 2, 1, "blfngl.AlienBlasterFire", "Blfngl.AlienBlasterReload", GUN).setUnlocalizedName("AlienBlaster").setCreativeTab(TabFalloutPistol);
 	public static final Item rifleHunting = new Gun308(617, 6, 24, 3, 3, "blfngl.HuntingRifleFire", "Blfngl.HuntingRifleReload", GUN).setUnlocalizedName("HuntingRifle").setCreativeTab(TabFalloutRifle);
 	public static final Item a9mm = new BaseItem(618).setUnlocalizedName("9mmRound").setCreativeTab(TabFalloutAmmo);
 	public static final Item aAlien = new BaseItem(619).setUnlocalizedName("AlienCells").setCreativeTab(TabFalloutAmmo);
 	public static final Item pistolLucky = new Gun357(620, 6, 6, 1, 2, "blfngl.357Fire", "Blfngl.357Reload", GUN, -0.5F).setUnlocalizedName("Lucky").setCreativeTab(TabFalloutPistol);
 	public static final Item pistol45Auto = new Gun45(621, 5, 7, 2, 1, "blfngl.45Fire", "Blfngl.9mmReload", GUN).setUnlocalizedName("45AutoPistol").setCreativeTab(TabFalloutPistol);
 	public static final Item pistolLightDarkness = new Gun45(622, 7, 7, 2, 1, "blfngl.45Fire", "Blfngl.9mmReload", GUN).setUnlocalizedName("LightDarkness").setCreativeTab(TabFalloutPistol);
 	public static final Item pistolMaria = new Gun9mm(623, 5, 13, 2, 2, "blfngl.9mmFire", "Blfngl.9mmReload", GUN).setUnlocalizedName("Maria").setCreativeTab(TabFalloutPistol);
 	public static final Item pistolHuntingRevolver = new GunGovt(624, 8, 5, 3, 3, "blfngl.HuntingFire", "Blfngl.HuntingReload", GUN).setUnlocalizedName("HuntingRevolver").setCreativeTab(TabFalloutPistol);
 	public static final Item pistolRangerSequoia = new GunGovt(625, 10, 5, 3, 3, "blfngl.RangerSequoiaFire", "Blfngl.HuntingReload", GUN).setUnlocalizedName("RangerSequoia").setCreativeTab(TabFalloutPistol);
 	public static final Item pistolPolice = new Gun357(626, 6, 6, 3, 2, "blfngl.357Fire", "Blfngl.357Reload", GUN, -0.5F).setUnlocalizedName("PolicePistol").setCreativeTab(TabFalloutPistol);
 
 	//KEY: ID, damage, clip size, reload time, fire time, fire sound, reload sound, zoom distance
 	public static final Item rifleAutomatic = new Gun308(627, 8, 20, 3, (int) 0.5, "blfngl.AutomaticRifleFire", "Blfngl.PlasmaRifleReload", GUN).setUnlocalizedName("AutomaticRifle").setCreativeTab(TabFalloutRifle);
 	public static final Item rifleBattle = new Gun308(628, 6, 8, 2, 3, "blfngl.BattleRifleFire", "Blfngl.BattleRifleReload", GUN).setUnlocalizedName("BattleRifle").setCreativeTab(TabFalloutRifle);
 	public static final Item rifleThisMachine = new Gun308(629, 7, 8, 2, 3, "blfngl.BattleRifleFire", "Blfngl.BattleRifleReload", GUN).setUnlocalizedName("ThisMachine").setCreativeTab(TabFalloutRifle);
 	public static final Item rifleBBGun = new GunBB(630, 1, 100, 3, 1, "blfngl.BBGunFire", "Blfngl.BBGunReload", GUN).setUnlocalizedName("BBGun").setCreativeTab(TabFalloutRifle);
 	public static final Item rifleAbileneKid = new GunBB(631, 2, 100, 3, 1, "blfngl.BBGunFire", "Blfngl.BBGunReload", GUN).setUnlocalizedName("ABBGun").setCreativeTab(TabFalloutRifle);
 	public static final Item rifleBrushGun = new GunGovt(632, 8, 6, 3, 2, "blfngl.BrushGunFire", "Blfngl.RepeaterReload", GUN).setUnlocalizedName("BrushGun").setCreativeTab(TabFalloutRifle);
 	public static final Item rifleMedicineStick = new GunGovt(633, 9, 6, 3, 2, "blfngl.BrushGunFire", "Blfngl.RepeaterReload", GUN).setUnlocalizedName("MedicineStick").setCreativeTab(TabFalloutRifle);
 	public static final Item rifleCowboyRepeater = new Gun357(634, 4, 7, 3, 2, "blfngl.CowboyRepeaterFire", "Blfngl.RepeaterReload", GUN, -0.7F).setUnlocalizedName("CowboyRepeater").setCreativeTab(TabFalloutRifle);
 	public static final Item rifleLongueCarabine = new Gun357(635, 5, 11, 3, 2, "blfngl.BrushGunFire", "Blfngl.RepeaterReload", GUN, -0.8F).setUnlocalizedName("LongueCarabine").setCreativeTab(TabFalloutRifle);
 	public static final Item riflePaciencia = new Gun308(636, 7, 3, 3, 2, "blfngl.HuntingRifleFire", "Blfngl.HuntingRifleReload", GUN).setUnlocalizedName("Paciencia").setCreativeTab(TabFalloutRifle);
 	public static final Item rocketCanister = new BaseItem(637).setUnlocalizedName("RocketCanister").setCreativeTab(TabFalloutAmmo);
 	public static final Item a45Auto = new BaseItem(638).setUnlocalizedName("45AutoRound").setCreativeTab(TabFalloutAmmo);
 	public static final Item aBB = new BaseItem(639).setUnlocalizedName("BB").setCreativeTab(TabFalloutAmmo);
 	public static final Item aGovt = new BaseItem(640).setUnlocalizedName("GovtRound").setCreativeTab(TabFalloutAmmo);
 	public static final Item stockMetal = new BaseItem(641).setUnlocalizedName("MetalStock").setCreativeTab(TabFalloutMisc);
 
 	//TODO Version 1.7.3
 	public static final Item pipboy = new ItemPipboy(642).setUnlocalizedName("Pipboy").setCreativeTab(TabFalloutMisc);
 	//KEY: ID, damage, clip size, reload time, fire time, fire sound, reload sound
 	public static final Item rifleAntiMateriel = new Gun50MG(643, 20, 8, (int) 2.5, 4, "blfngl.MaterielFire", "Blfngl.MaterielReload", GUN).setUnlocalizedName("AntiMaterielRifle").setCreativeTab(TabFalloutRifle);
 	public static final Item a50MG = new BaseItem(644).setUnlocalizedName("50MG").setCreativeTab(TabFalloutAmmo);
 	public static final Item rifleAssaultCarbine = new Gun5mm(645, 6, 24, 2, (int) 0.5, "blfngl.AssaultFire", "Blfngl.AssaultReload", GUN, -0.5F).setUnlocalizedName("AssaultCarbine").setCreativeTab(TabFalloutRifle);
 	public static final Item rifleLMG = new Gun556(646, 6, 90, (int) 2.2, (int) 0.5, "blfngl.LMGFire", "Blfngl.LMGReload", GUN, -0.5F).setUnlocalizedName("LMG").setCreativeTab(TabFalloutRifle);
 	public static final Item rifleBozar = new Gun556(647, 7, 30, (int) 2.2, (int) 0.5, "blfngl.LMGFire", "Blfngl.LMGReload", GUN, -1.5F).setFull3D().setUnlocalizedName("Bozar").setCreativeTab(TabFalloutRifle);
 	public static final Item rifleMarksmanCarbine = new Gun556(648, 7, 20, (int) 2.5, 1, "blfngl.MarksmanFire", "Blfngl.MarksmanReload", GUN, -1.5F).setUnlocalizedName("MarksmanCarbine").setCreativeTab(TabFalloutRifle);
 	public static final Item rifleAllAmerican = new Gun556(649, 8, 24, (int) 2.5, 1, "blfngl.MarksmanFire", "Blfngl.MarksmanReload", GUN, -1.5F).setUnlocalizedName("AllAmerican").setCreativeTab(TabFalloutRifle);
 	public static final Item rifleService = new Gun556(650, 3, 20, 2, (int) 0.7, "blfngl.AutomaticRifleFire", "Blfngl.MarksmanReload", GUN, -0.5F).setUnlocalizedName("ServiceRifle").setCreativeTab(TabFalloutRifle);
 	public static final Item rifleSurvival = new Gun127(651, 7, 10, 2, (int) 0.7, "blfngl.AutomaticRifleFire", "Blfngl.MarksmanReload", GUN).setUnlocalizedName("SurvivalistsRifle").setCreativeTab(TabFalloutRifle);
 	public static final Item rifleChristine = new Gun308(652, 15, 5, 2, 3, "blfngl.ChristineFire", "Blfngl.ChristineReload", GUN).setUnlocalizedName("ChristinesRifle").setCreativeTab(TabFalloutRifle);
 	public static final Item rifleGobi = new Gun308(653, 14, 6, 2, 3, "blfngl.SniperFire", "Blfngl.SniperReload", GUN).setUnlocalizedName("GobiRifle").setCreativeTab(TabFalloutRifle);
 	public static final Item rifleTrailCarbine = new Gun44(654, 10, 8, 2, 2, "blfngl.TrailCarbineFire", "Blfngl.RepeaterReload", GUN).setUnlocalizedName("TrailCarbine").setCreativeTab(TabFalloutRifle);
 	public static final Item rifleVarmint = new Gun556(655, 3, 5, (int) 2.2, 2, "blfngl.VarmintRifleFire", "Blfngl.PlasmaRifleReload", GUN, -0.5F).setUnlocalizedName("VarmintRifle").setCreativeTab(TabFalloutRifle);
 	public static final Item rifleRatslayer = new Gun556(656, 5, 5, (int) 2.2, 2, "blfngl.RatslayerFire", "Blfngl.PlasmaRifleReload", GUN, -1.5F).setUnlocalizedName("Ratslayer").setCreativeTab(TabFalloutRifle);
 
 	public static final Item smg45Auto = new Gun45(657, 4, 30, (int) 2.2, (int) 0.5, "blfngl.45Fire", "Blfngl.AssaultReload", GUN).setUnlocalizedName("45AutoSub").setCreativeTab(TabFalloutSMG);
 	public static final Item smg9mm = new Gun9mm(658, 2, 30, (int) 2.7, (int) 0.5, "blfngl.9mmFire", "Blfngl.9mmReload", GUN).setUnlocalizedName("9mmSub").setCreativeTab(TabFalloutSMG);
 	public static final Item smgVance = new Gun9mm(659, 4, 60, (int) 2.2, (int) 0.5, "blfngl.9mmFire", "Blfngl.9mmReload", GUN).setUnlocalizedName("VancesSub").setCreativeTab(TabFalloutSMG);
 	public static final Item smg10mm = new Gun10mm(660, 3, 30, (int) 2.7, (int) 0.5, "blfngl.10MMFire", "Blfngl.10MMReload", GUN).setUnlocalizedName("10mmSub").setCreativeTab(TabFalloutSMG);
 	public static final Item smgSleepytyme = new Gun10mm(661, 5, 40, (int) 2.7, (int) 0.5, "blfngl.SleepytymeFire", "Blfngl.10MMReload", GUN).setUnlocalizedName("Sleepytyme").setCreativeTab(TabFalloutSMG);
 	public static final Item smg127 = new Gun127(662, 4, 21, 2, (int) 0.5, "blfngl.357Fire", "Blfngl.127SmgReload", GUN).setUnlocalizedName("127Sub").setCreativeTab(TabFalloutSMG);
 	public static final Item smgNailGun = new GunNail(663, 2, 90, (int) 2.7, (int) 0.3, "blfngl.NailgunFire", "Blfngl.9mmReload", GUN).setUnlocalizedName("NailGun").setCreativeTab(TabFalloutSMG);
 	public static final Item smg22 = new Gun22(664, 1, 180, (int) 2.3, (int) 0.5, "blfngl.Silenced22Fire", "Blfngl.10MMReload", GUN).setUnlocalizedName("22Sub").setCreativeTab(TabFalloutSMG);
 	public static final Item aNail = new BaseItem(665).setUnlocalizedName("Nails").setCreativeTab(TabFalloutAmmo);
 	public static final Item CZ57 = new Gun5mm(666, 5, 120, 4, (int) 0.3, "blfngl.MinigunFire", "Blfngl.MinigunReload", GUN, -0.3F).setUnlocalizedName("CZ57").setCreativeTab(TabFalloutHeavy);
 	public static final Item pistolWeathered10mm = new Gun10mm(667, 5, 12, (int) 1.3, 1, "blfngl.10mmFire", "Blfngl.10MMReload", GUN).setUnlocalizedName("Weathered10mm").setCreativeTab(TabFalloutPistol);
 
 	//KEY: ID, damage, clip size, reload time, fire time, fire sound, reload sound
 	public static final Item shotgunCaravan = new GunGauge20(668, 5, 2, (int) 1.5, 1, "blfngl.CaravanShotgunFire", "Blfngl.CaravanShotgunReload", GUN).setUnlocalizedName("CaravanShotgun").setCreativeTab(TabFalloutShotgun);
 	public static final Item shotgunSturdyCaravan = new GunGauge20(669, 7, 2, (int) 1.5, 1, "blfngl.CaravanShotgunFire", "Blfngl.CaravanShotgunReload", GUN).setUnlocalizedName("SturdyCaravanShotgun").setCreativeTab(TabFalloutShotgun);
 	public static final Item shotgunLever = new GunGauge20(670, 6, 5, (int) 2.5, 2, "blfngl.LeverShotgunFire", "Blfngl.LeverShotgunReload", GUN).setUnlocalizedName("LeverActionShotgun").setCreativeTab(TabFalloutShotgun);
 	public static final Item shotgunSawed = new GunGauge12(671, 8, 1, (int) 3.1, 2, "blfngl.HuntingShotgunFire", "Blfngl.LeverShotgunReload", GUN).setUnlocalizedName("SawedOffShotgun").setCreativeTab(TabFalloutShotgun);
 	public static final Item shotgunBigBoomer = new GunGauge12(672, 10, 1, (int) 3.1, 2, "blfngl.HuntingShotgunFire", "Blfngl.LeverShotgunReload", GUN).setUnlocalizedName("BigBoomer").setCreativeTab(TabFalloutShotgun);
 	public static final Item shotgunSingle = new GunGauge20(673, 4, 1, 2, 2, "blfngl.LeverShotgunFire", "Blfngl.LeverShotgunReload", GUN).setUnlocalizedName("SingleShotgun").setCreativeTab(TabFalloutShotgun);
 	public static final Item aGauge20 = new BaseItem(674).setUnlocalizedName("20Gauge").setCreativeTab(TabFalloutAmmo);
 
 	public static final Item rifleChineseAssault = new Gun556(675, 4, 24, (int) 2.2, (int) 0.5, "blfngl.ChineseAssaultFire", "Blfngl.ChineseAssaultReload", GUN, -0.5F).setUnlocalizedName("ChineseAssaultRifle").setCreativeTab(TabFalloutShotgun);
 	public static final Item pistolFlareGun = new GunFuel(676, 1, 1, (int) 2.3, 2, "blfngl.FlareGunFire", "Blfngl.FlareGunReload", GUN).setUnlocalizedName("FlareGun").setCreativeTab(TabFalloutShotgun);
 	public static final Item pistolPewPew = new GunECell(677, 6, 2, 3, 1, "blfngl.LaserFire", "Blfngl.LaserPistolReload", GUN).setUnlocalizedName("PewPew").setCreativeTab(TabFalloutShotgun);
 	public static final Item pistolPulseGun = new GunECell(678, 3, 5, 2, 3, "blfngl.PulseFire", "Blfngl.PulseGunReload", GUN).setUnlocalizedName("PulseGun").setCreativeTab(TabFalloutShotgun);
 	public static final Item pistolHyperbreederAlpha = new GunRecharger(679, 4, 10, 3, 1, "blfngl.RechargerFire", "Blfngl.LeverShotgunReload", GUN).setUnlocalizedName("HyperbreederAlpha").setCreativeTab(TabFalloutShotgun);
 	public static final Item pistolSonicEmitter = new GunECell(680, 4, 24, 2, 2, "blfngl.PulseFire", "Blfngl.PulseGunReload", GUN).setUnlocalizedName("SonicEmitter").setCreativeTab(TabFalloutShotgun);
 
 	public static final Item fixer = new ItemFixer(681).setUnlocalizedName("Fixer").setCreativeTab(TabFalloutFood);
 
 	public static final Item rifleYCS186 = new GunMFCell(682, 14, 1, (int) 2.3, (int) 2.5, "blfngl.GaussFire", "Blfngl.GaussReload", GUN, -3.5F).setUnlocalizedName("YCS186").setCreativeTab(TabFalloutShotgun);
 	public static final Item rifleAdvancedLAER = new GunMFCell(683, 10, 15, 3, (int) 1.5, "blfngl.PulseFire", "Blfngl.PlasmaRifleReload", GUN, -1.5F).setUnlocalizedName("AdvancedLAER").setCreativeTab(TabFalloutShotgun);
 	public static final Item rifleAER14 = new GunMFCell(684, 10, 12, (int) 2.5, 1, "blfngl.LaserFire", "Blfngl.LaserReload", GUN, -1.0F).setUnlocalizedName("AER14Proto").setCreativeTab(TabFalloutShotgun);
 
 	public static final Item mod357LongBarrel = new BaseItem(685).setUnlocalizedName("357LongBarrel").setCreativeTab(TabFalloutParts);
 	public static final Item mod357HDCylinder = new BaseItem(686).setUnlocalizedName("357HDCylinder").setCreativeTab(TabFalloutParts);
 	public static final Item mod45APHDSlide = new BaseItem(688).setUnlocalizedName("45APHDSlide").setCreativeTab(TabFalloutParts);
 	public static final Item mod45APSilencer = new BaseItem(689).setUnlocalizedName("45APSilencer").setCreativeTab(TabFalloutParts);
 	public static final Item mod9mmPistolExtMag = new BaseItem(690).setUnlocalizedName("9mmPistolExtMag").setCreativeTab(TabFalloutParts);
 	public static final Item mod10mmPistolExtenderMags = new BaseItem(691).setUnlocalizedName("10mmPistolExtMag").setCreativeTab(TabFalloutParts);
 	public static final Item mod10mmPistolSilencer = new BaseItem(692).setUnlocalizedName("10mmPistolSilencer").setCreativeTab(TabFalloutParts);
 	public static final Item mod127mmPistolSilencer = new BaseItem(693).setUnlocalizedName("127mmPistolsilencer").setCreativeTab(TabFalloutParts);
 	public static final Item modHuntingRevolver6Shot = new BaseItem(694).setUnlocalizedName("HuntingRevolver6Shot").setCreativeTab(TabFalloutParts);
 
 	public static final Item modAntiMaterielSupressor = new BaseItem(696).setUnlocalizedName("AntiMatSupressor").setCreativeTab(TabFalloutParts);
 	public static final Item modAntiMaterielCustomBolt = new BaseItem(697).setUnlocalizedName("AntiMatBolt").setCreativeTab(TabFalloutParts);
 	public static final Item modAssaultCarbineExtendedMags = new BaseItem(698).setUnlocalizedName("AssaultCarbineExtMag").setCreativeTab(TabFalloutParts);
 	public static final Item modAssaultCarbineForgedReciever = new BaseItem(699).setUnlocalizedName("AssaultCarbineReciever").setCreativeTab(TabFalloutParts);
 	public static final Item modAssaultCarbineLightBolt = new BaseItem(700).setUnlocalizedName("AssaultCarbineBolt").setCreativeTab(TabFalloutParts);
 	public static final Item modAutomaticRifleInternals = new BaseItem(701).setUnlocalizedName("AutoRifleInt").setCreativeTab(TabFalloutParts);
 	public static final Item modBrushGunForgedReciever = new BaseItem(702).setUnlocalizedName("BrushGunReciever").setCreativeTab(TabFalloutParts);
 	public static final Item modCowboyRepeaterCustomAction = new BaseItem(703).setUnlocalizedName("CowboyAction").setCreativeTab(TabFalloutParts);
 	public static final Item modCowboyRepeaterLongTube = new BaseItem(704).setUnlocalizedName("CowboyTube").setCreativeTab(TabFalloutParts);
 	public static final Item modHuntingRifleExtendedMag = new BaseItem(705).setUnlocalizedName("HuntingRifleExtMag").setCreativeTab(TabFalloutParts);
 	public static final Item modHuntingRifleCustomAction = new BaseItem(706).setUnlocalizedName("HuntingRifleAction").setCreativeTab(TabFalloutParts);
 	public static final Item modLMGExpandedDrums = new BaseItem(707).setUnlocalizedName("LMGExpDrum").setCreativeTab(TabFalloutParts);
 	public static final Item modServiceRifleForgedReciever = new BaseItem(708).setUnlocalizedName("ServiceReciever").setCreativeTab(TabFalloutParts);
 	public static final Item modServiceRifleCustomSprings = new BaseItem(709).setUnlocalizedName("ServiceSprings").setCreativeTab(TabFalloutParts);
 	public static final Item modSniperSupressor = new BaseItem(710).setUnlocalizedName("SniperSupressor").setCreativeTab(TabFalloutParts);
 	public static final Item modSniperCFParts = new BaseItem(711).setUnlocalizedName("SniperCF").setCreativeTab(TabFalloutParts);
 	public static final Item modVarmintNightScope = new BaseItem(712).setUnlocalizedName("VarmintScope").setCreativeTab(TabFalloutParts);
 	public static final Item modVarmintExtendedMags = new BaseItem(713).setUnlocalizedName("VarmintExtMag").setCreativeTab(TabFalloutParts);
 	public static final Item modVarmintSilencer = new BaseItem(714).setUnlocalizedName("VarmintSilencer").setCreativeTab(TabFalloutParts);
 
 	public static final Item superConductor = new BaseItem(715).setUnlocalizedName("SuperConductor").setCreativeTab(TabFalloutParts);
 	public static final Item plasmaChamber = new BaseItem(716).setUnlocalizedName("PlasmaChamber").setCreativeTab(TabFalloutParts);
 	public static final Item electroMagnet = new BaseItem(717).setUnlocalizedName("ElectroMagnet").setCreativeTab(TabFalloutParts);
 	public static final Item magnetClaws = new BaseItem(718).setUnlocalizedName("MagnetClaws").setCreativeTab(TabFalloutParts);
 	public static final Item particleDiverter = new BaseItem(719).setUnlocalizedName("ParticleDiverter").setCreativeTab(TabFalloutParts);
 	public static final Item carbonFiberHousing = new BaseItem(720).setUnlocalizedName("CFHousing").setCreativeTab(TabFalloutParts);
 	public static final Item energyRifleBarrel = new BaseItem(721).setUnlocalizedName("ERBarrel").setCreativeTab(TabFalloutParts);
 	public static final Item drum = new BaseItem(722).setUnlocalizedName("Drum").setCreativeTab(TabFalloutParts);
 	public static final Item cylinder = new BaseItem(723).setUnlocalizedName("Cylinder").setCreativeTab(TabFalloutParts);
 
 	@PreInit
 	public void preInit(FMLPreInitializationEvent event)
 	{
 		ModMetadata var2 = event.getModMetadata();
 		var2.authorList = Arrays.asList(new String[] {"Blfngl"});
 		var2.autogenerated = false;
 		var2.modId = "fallout";
 		var2.name = "The Fallout Mod";
 		var2.description = "A mod in which most of the items from all of the Fallout games will be added." +
 				" Most everything will be there, but this will take a very long time, mainly because the Fallout universe is so huge. " +
 				"I am 100% dedicated to this mod and one day it will be completed. After looking around for a Fallout mod and finding some, " +
 				"but none that were fully completed or updated to the current version, I was very disappointed and hoped for one to be updated eventually. " +
 				"Alas, none of that was true. So I took matters into my own hands and this was the product.";
 		var2.version = "1.7.3";
 		var2.credits = "Mojang, MinecraftForge and everyone that uses this mod :)";
 		var2.logoFile = "/blfngl/fallout/textures/blflogo.png";
 		var2.url = "http://www.minecraftforum.net/topic/1667023-/";
		System.out.println("Blfngl's Fallout Mod Loaded. </3.");
 
 		proxy.registerSoundHandler();
 	}
 
 	@Init
 	public void init(FMLInitializationEvent event)
 	{
 		RecordHandler.init();
 
 		BlockHandler.init();
 
 		BiomeHandler.init();
 
 		GunHandler.init();
 
 		ItemHandler.init();
 
 		WeaponHandler.init();
 
 		FoodHandler.init();
 
 		ArmorHandler.init();
 
 		ThrowingHandler.init();
 
 		EntityHandler.init();
 
 		LanguageRegistry.addName(RecordHandler.Track1, "Blue Moon");
 		LanguageRegistry.addName(RecordHandler.Track2, "Hallo Mr X");
 		LanguageRegistry.addName(RecordHandler.Track3, "Strahlende Trompete");
 		LanguageRegistry.addName(RecordHandler.Track4, "Ain't That A Kick In The Head");
 		LanguageRegistry.addName(RecordHandler.Track5, "American Swing");
 		LanguageRegistry.addName(RecordHandler.Track6, "Big Iron");
 		LanguageRegistry.addName(RecordHandler.Track7, "Cobwebs And Rainbows");
 		LanguageRegistry.addName(RecordHandler.Track8, "Home On The Wastes");
 		LanguageRegistry.addName(RecordHandler.Track9, "It's A Sin To Tell A Lie");
 		LanguageRegistry.addName(RecordHandler.Track10, "It's A Sin");
 		LanguageRegistry.addName(RecordHandler.Track11, "Jingle Jangle Jingle");
 		LanguageRegistry.addName(RecordHandler.Track12, "In The Shadow Of The Valley");
 		LanguageRegistry.addName(RecordHandler.Track13, "Sleepy Town Blues");
 		LanguageRegistry.addName(RecordHandler.Track14, "Stars Of The Midnight Ranger");
 		LanguageRegistry.addName(RecordHandler.Track15, "Streets Of New Reno");
 		LanguageRegistry.addName(RecordHandler.Track16, "Why Don't You Do Right");
 
 		//TODO DimensionManager.registerProviderType(FalloutMain.dimensionAlien, DimensionAlienHandler.class, true);
 		//TODO DimensionManager.registerDimension(FalloutMain.dimensionAlien , FalloutMain.dimensionAlien);
 
 		proxy.registerRenderers();
 	}
 
 	@PostInit
 	public static void postInit(FMLPostInitializationEvent event)
 	{
 
 	}
 }
