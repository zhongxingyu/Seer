 package blfngl.fallout.handler;
 
 import net.minecraft.block.Block;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import blfngl.fallout.FalloutMain;
import blfngl.fallout.gun.Gun556;
import blfngl.fallout.gun.GunECell;
import blfngl.fallout.gun.GunFuel;
import blfngl.fallout.gun.GunGauge12;
import blfngl.fallout.gun.GunGauge20;
import blfngl.fallout.gun.GunMFCell;
import blfngl.fallout.gun.GunRecharger;
import blfngl.fallout.item.BaseItem;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 
 public class GunHandler extends FalloutMain
 {
 	public static void init()
 	{
 		LanguageRegistry.addName(incinerator, "Incinerator (bugged)");
 		GameRegistry.addRecipe(new ItemStack(incinerator), new Object [] {"XXX", " *&", " &&", 'X', Block.netherrack, '*', Item.ingotIron, '&', ingotTungsten});
 		LanguageRegistry.addName(rifleLaserRCW, "Laser RCW");
 		GameRegistry.addRecipe(new ItemStack(rifleLaserRCW), new Object [] {"X*^", "#&%", 'X', energyRifleBarrel, '*', magRail, '^', stockWood, '#', drum, '&', grip, '%', cellElectron});
 		LanguageRegistry.addName(pistolCompliance, "Compliance Regulator");
 		GameRegistry.addRecipe(new ItemStack(pistolCompliance), new Object [] {" X ", "X*X", " X ", 'X', Block.slowSand, '*', pistolLaser});
 		LanguageRegistry.addName(rifleLaser, "AER7 Laser Rifle");
 		GameRegistry.addRecipe(new ItemStack(rifleLaser), new Object [] {"$X*", "!&#", '!', carbonFiberHousing, 'X', energyRifleBarrel, '$', magnifier, '*', stockMetal, '&', ingotTungsten, '#', grip});
 		LanguageRegistry.addName(pistol357, ".357 Magnum");
 		GameRegistry.addRecipe(new ItemStack(pistol357), new Object [] {"*X^", " !#", "  &", '!', cylinder, '#', Item.ingotIron, '*', barrel, 'X', ingotTungsten, '&', grip, '^', hammer});
 		LanguageRegistry.addName(pistol44, ".44 Magnum");
 		GameRegistry.addRecipe(new ItemStack(pistol44), new Object [] {"*X^", " !X", "  &", '!', cylinder, '*', barrel, 'X', ingotTungsten, '&', grip, '^', hammer});
 		LanguageRegistry.addName(pistol22, "Silenced .22 Pistol");
 		GameRegistry.addRecipe(new ItemStack(pistol22), new Object [] {"*XX", " %X", "  &", '*', barrel, 'X', barSilicon, '%', chunkTungsten, '&', grip});
 		LanguageRegistry.addName(rifleSniper, "Sniper Rifle");
 		GameRegistry.addRecipe(new ItemStack(rifleSniper), new Object [] {"XX*", " #&", " %&", 'X', barrel, '*', stockMetal, '&', ingotTechnetium, '%', grip, '#', tungstenPlate});
 
 		LanguageRegistry.addName(rifleTriBeam, "Tri-Beam Laser Rifle");
 		GameRegistry.addRecipe(new ItemStack(rifleTriBeam), new Object [] {"X*^", " &%", " # ", 'X', magnifier, '*', magRail, '^', Item.redstoneRepeater, '&', grip, '%', stockMetal, '#', cellMF});
 		LanguageRegistry.addName(rifleMultiplas, "Multiplas Rifle");
 		//GameRegistry.addRecipe(new ItemStack(rifleMultiplas), new Object [] {});
 		LanguageRegistry.addName(rifleLAER, "LAER");
 		LanguageRegistry.addName(rifleHolo, "Holorifle");
 		LanguageRegistry.addName(minigun, "Minigun");
 		LanguageRegistry.addName(cyberdog, "K9000 Cyberdog Gun");
 		LanguageRegistry.addName(FIDO, "FIDO");
 		LanguageRegistry.addName(plasmaCaster, "Plasma Caster");
 		LanguageRegistry.addName(missileLauncher, "Missile Launcher");
 		LanguageRegistry.addName(redGlare, "Red Glare");
 		LanguageRegistry.addName(pistol556, "5.56mm Pistol");
 		LanguageRegistry.addName(pistolThatGun, "That Gun");
 		LanguageRegistry.addName(pistol127, "12.7mm Pistol");
 		LanguageRegistry.addName(pistolLaser, "Laser Pistol");
 		LanguageRegistry.addName(pistolDefender, "Plasma Defender");
 		LanguageRegistry.addName(pistolPlasma, "Plasma Pistol");
 		LanguageRegistry.addName(teslaCannon, "Tesla Cannon");
 		LanguageRegistry.addName(rifleRecharger, "Recharger Rifle");
 		LanguageRegistry.addName(pistolRecharger, "Recharger Pistol");
 		LanguageRegistry.addName(aRocket, "Rocket");
 		LanguageRegistry.addName(aMissile, "Missile");
 		LanguageRegistry.addName(a127, "12.7mm Rounds");
 		LanguageRegistry.addName(a556, "5.56mm Rounds");
 		LanguageRegistry.addName(a5mm, "5mm Rounds");
 		LanguageRegistry.addName(shotgunHunting, "Hunting Shotgun");
 		LanguageRegistry.addName(shotgunDinnerBell, "Dinner Bell");
 		LanguageRegistry.addName(pistol9mm, "9mm Pistol");
 		LanguageRegistry.addName(pistolAlienBlaster, "Alien Blaster");
 		LanguageRegistry.addName(rifleHunting, "Hunting Rifle");
 		LanguageRegistry.addName(a9mm, "9mm Rounds");
 		LanguageRegistry.addName(aAlien, "Alien Power Cells");
 		LanguageRegistry.addName(pistolLucky, "Lucky");
 		LanguageRegistry.addName(pistol45Auto, ".45 Auto Pistol");
 		LanguageRegistry.addName(pistolLightDarkness, "A Light in Shining Darkness");
 		LanguageRegistry.addName(pistolMaria, "Maria");
 		LanguageRegistry.addName(pistolHuntingRevolver, "Hunting Revolver");
 		LanguageRegistry.addName(pistolRangerSequoia, "Ranger Sequoia");
 		LanguageRegistry.addName(pistolPolice, "Police Pistol");
 		LanguageRegistry.addName(rifleAutomatic, "Automatic Rifle");
 		LanguageRegistry.addName(rifleBattle, "Battle Rifle");
 		LanguageRegistry.addName(rifleThisMachine, "This Machine");
 		LanguageRegistry.addName(rifleBBGun, "BB Gun");
 		LanguageRegistry.addName(rifleAbileneKid, "Abilene Kid BB Gun");
 		LanguageRegistry.addName(rifleBrushGun, "Brush Gun");
 		LanguageRegistry.addName(rifleMedicineStick, "Medicine Stick");
 		LanguageRegistry.addName(rifleCowboyRepeater, "Cowboy Repeater");
 		LanguageRegistry.addName(rifleLongueCarabine, "La Longue Carabine");
 		LanguageRegistry.addName(riflePaciencia, "Paciencia");
 		LanguageRegistry.addName(rocketCanister, "Rocket Canister");
 		LanguageRegistry.addName(a45Auto, ".45 Auto Rounds");
 		LanguageRegistry.addName(aBB, "BBs");
 		LanguageRegistry.addName(aGovt, ".40-.70 Govt. Rounds");
 		LanguageRegistry.addName(stockMetal, "Metal Stock");
 
 		LanguageRegistry.addName(rifleAntiMateriel, "Anti-Materiel Rifle");
 		LanguageRegistry.addName(a50MG, ".50MG");
 		LanguageRegistry.addName(rifleAssaultCarbine, "Assault Carbine");
 		LanguageRegistry.addName(rifleLMG, "Light Machine Gun");
 		LanguageRegistry.addName(rifleBozar, "Bozar");
 		LanguageRegistry.addName(rifleMarksmanCarbine, "Marksman Carbine");
 		LanguageRegistry.addName(rifleAllAmerican, "All American");
 		LanguageRegistry.addName(rifleService, "Service Rifle");
 		LanguageRegistry.addName(rifleSurvival, "Survivalist's Rifle");
 		LanguageRegistry.addName(rifleChristine, "Christine's CoS Silencer Rifle");
 		LanguageRegistry.addName(rifleGobi, "Gobi Campaign Scout Rifle");
 		LanguageRegistry.addName(rifleTrailCarbine, "Trail Carbine");
 		LanguageRegistry.addName(rifleVarmint, "Varmint Rifle");
 		LanguageRegistry.addName(rifleRatslayer, "Ratslayer");
 		LanguageRegistry.addName(smg45Auto, ".45 Auto Submachine Gun");
 		LanguageRegistry.addName(smg9mm, "9mm Submachine Gun");
 		LanguageRegistry.addName(smgVance, "Vance's 9mm Submachine Gun");
 		LanguageRegistry.addName(smg10mm, "10mm Submachine Gun");
 		LanguageRegistry.addName(smgSleepytyme, "Sleepytyme");
 		LanguageRegistry.addName(smg127, "12.7mm Submachine Gun");
 		LanguageRegistry.addName(smgNailGun, "H&H Tools Nail Gun");
 		LanguageRegistry.addName(smg22, "Silenced .22 SMG");
 		LanguageRegistry.addName(aNail, "Nails");
 		LanguageRegistry.addName(CZ57, "CZ57 Avenger");
 		LanguageRegistry.addName(pistolWeathered10mm, "Weathered 10mm Pistol");
 
 		LanguageRegistry.addName(riflePlasma, "Plasma Rifle");
 		LanguageRegistry.addName(pistol10mm, "10mm Pistol");
 		LanguageRegistry.addName(a10mm, "10mm Rounds");
 		LanguageRegistry.addName(rifleGauss, "Gauss Rifle");
 		LanguageRegistry.addName(shotgunCombat, "Combat Shotgun");
 		LanguageRegistry.addName(shotgunRiot, "Riot Shotgun");
 		LanguageRegistry.addName(aGauge12, "12 Gauge Rounds");
 
 		LanguageRegistry.addName(rifleYCS186, "YCS/186");
 		LanguageRegistry.addName(rifleAdvancedLAER, "Elijah's Advanced LAER");
 		LanguageRegistry.addName(rifleAER14, "AER14 Prototype");
 
 		LanguageRegistry.addName(shotgunCaravan, "Caravan Shotgun");
 		LanguageRegistry.addName(shotgunSturdyCaravan, "Sturdy Caravan Shotgun");
 		LanguageRegistry.addName(shotgunLever, "Lever Action Shotgun");
 		LanguageRegistry.addName(shotgunSawed, "Sawed Off Shotgun");
 		LanguageRegistry.addName(shotgunBigBoomer, "Big Boomer");
 		LanguageRegistry.addName(shotgunSingle, "Single Shotgun");
 
 		LanguageRegistry.addName(rifleChineseAssault, "Chinese Assault Rifle");
 		LanguageRegistry.addName(pistolFlareGun, "Flare Gun");
 		LanguageRegistry.addName(pistolPewPew, "Pew Pew");
 		LanguageRegistry.addName(pistolPulseGun, "Pulse Gun");
 		LanguageRegistry.addName(pistolHyperbreederAlpha, "MF Hyperbreeder Alpha");
 		LanguageRegistry.addName(pistolSonicEmitter, "Sonic Emitter - Revalation");
 	}
 }
