 package fyresmodjam;
 
 import net.minecraftforge.common.Configuration;
 import cpw.mods.fml.common.registry.TickRegistry;
 import cpw.mods.fml.relauncher.Side;
 
 public class CommonProxy {
 	public void register() {
 		//TickRegistry.registerTickHandler(new CommonTickHandler(), Side.SERVER);
 	}
 
 	public void sendPlayerMessage(String message) {}
 
 	public void loadFromConfig(Configuration config) {
		ModjamMod.itemID = config.getItem("item_ids", ModjamMod.itemID + 4096).getInt() - 4096;
 		ModjamMod.blockID = config.getBlock("block_ids",ModjamMod. blockID).getInt();
 		
 		ModjamMod.pillarGlow = config.get(config.CATEGORY_GENERAL, "pillar_glow", ModjamMod.pillarGlow).getBoolean(ModjamMod.pillarGlow);
 		
 		ModjamMod.pillarGenChance = config.get(config.CATEGORY_GENERAL, "pillar_gen_difficulty", ModjamMod.pillarGenChance).getInt();
 		ModjamMod.maxPillarsPerChunk = config.get(config.CATEGORY_GENERAL, "max_pillars_per_chunk", ModjamMod.maxPillarsPerChunk).getInt();
 		ModjamMod.towerGenChance = config.get(config.CATEGORY_GENERAL, "tower_gen_difficulty", ModjamMod.towerGenChance).getInt();
 		ModjamMod.trapGenChance = config.get(config.CATEGORY_GENERAL, "trap_gen_difficulty", ModjamMod.trapGenChance).getInt();
 		ModjamMod.mushroomReplaceChance = config.get(config.CATEGORY_GENERAL, "mushroom_replace_difficulty", ModjamMod.mushroomReplaceChance).getInt();
 		
 		ModjamMod.spawnTraps = !(config.get(config.CATEGORY_GENERAL, "disable_traps", !ModjamMod.spawnTraps).getBoolean(!ModjamMod.spawnTraps));
 		ModjamMod.spawnTowers = config.get(config.CATEGORY_GENERAL, "spawn_towers", ModjamMod.spawnTowers).getBoolean(ModjamMod.spawnTowers);
 		ModjamMod.spawnRandomPillars = config.get(config.CATEGORY_GENERAL, "spawn_random_pillars", ModjamMod.spawnRandomPillars).getBoolean(ModjamMod.spawnRandomPillars);
 		ModjamMod.disableDisadvantages = config.get(config.CATEGORY_GENERAL, "disable_disadvantages", ModjamMod.disableDisadvantages).getBoolean(ModjamMod.disableDisadvantages);
 		ModjamMod.versionChecking = config.get(config.CATEGORY_GENERAL, "version_checking", ModjamMod.versionChecking).getBoolean(ModjamMod.versionChecking);
 		
 		ModjamMod.showAllPillarsInCreative = config.get(config.CATEGORY_GENERAL, "show_all_pillars_in_creative", ModjamMod.showAllPillarsInCreative).getBoolean(ModjamMod.showAllPillarsInCreative);
 		
 		ModjamMod.enableMobKillStats = config.get(config.CATEGORY_GENERAL, "enable_mob_kill_stats", ModjamMod.enableMobKillStats).getBoolean(ModjamMod.enableMobKillStats);
 		ModjamMod.enableWeaponKillStats = config.get(config.CATEGORY_GENERAL, "enable_weapon_kill_stats", ModjamMod.enableWeaponKillStats).getBoolean(ModjamMod.enableWeaponKillStats);
 		
 		ModjamMod.trapsBelowGroundOnly = config.get(config.CATEGORY_GENERAL, "traps_below_ground_only", ModjamMod.trapsBelowGroundOnly).getBoolean(ModjamMod.trapsBelowGroundOnly);
 	}
 }
