 package com.slauson.asteroid_dasher.status;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.slauson.asteroid_dasher.R;
 
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.content.res.Resources;
 
 /**
  * Achievements
  * @author Josh Slauson
  *
  */
 public class Achievements {
 	
 	// constants
 	public static final int LOCAL_DESTROY_ASTEROIDS_DASH_NUM_1 = 3;
 	public static final int LOCAL_DESTROY_ASTEROIDS_DASH_NUM_2 = 5;
 	public static final int LOCAL_DESTROY_ASTEROIDS_DASH_NUM_3 = 10;
 	
 	public static final int LOCAL_DESTROY_ASTEROIDS_DRILL_NUM_1 = 5;
 	public static final int LOCAL_DESTROY_ASTEROIDS_DRILL_NUM_2 = 10;
 	public static final int LOCAL_DESTROY_ASTEROIDS_DRILL_NUM_3 = 15;
 	
 	public static final int LOCAL_DESTROY_ASTEROIDS_MAGNET_NUM_1 = 10;
 	public static final int LOCAL_DESTROY_ASTEROIDS_MAGNET_NUM_2 = 25;
 	public static final int LOCAL_DESTROY_ASTEROIDS_MAGNET_NUM_3 = 50;
 
 	public static final int LOCAL_DESTROY_ASTEROIDS_BLACK_HOLE_NUM_1 = 10;
 	public static final int LOCAL_DESTROY_ASTEROIDS_BLACK_HOLE_NUM_2 = 25;
 	public static final int LOCAL_DESTROY_ASTEROIDS_BLACK_HOLE_NUM_3 = 50;
 	
 	public static final int LOCAL_DESTROY_ASTEROIDS_BUMPER_NUM_1 = 10;
 	public static final int LOCAL_DESTROY_ASTEROIDS_BUMPER_NUM_2 = 25;
 	public static final int LOCAL_DESTROY_ASTEROIDS_BUMPER_NUM_3 = 50;
 	
 	public static final int LOCAL_DESTROY_ASTEROIDS_BOMB_NUM_1 = 10;
 	public static final int LOCAL_DESTROY_ASTEROIDS_BOMB_NUM_2 = 20;
 	public static final int LOCAL_DESTROY_ASTEROIDS_BOMB_NUM_3 = 30;
 
 	public static final int LOCAL_DASH_ACTIVATE_DROPS_NUM = 1;
 	public static final int LOCAL_SMALL_DASH_DESTROY_ASTEROIDS_NUM = 2;
 	public static final int LOCAL_SLOW_LONG_TIME = 60;
 	public static final int LOCAL_INVULNERABILITY_PASS_THROUGH_NUM = 10;
 	public static final int LOCAL_BLACK_HOLE_TRIFECTA_NUM = 3;
 	public static final int LOCAL_MAGNET_HOLD_IN_PLACE_NUM = 10;
 	public static final int LOCAL_OTHER_STAY_IN_PLACE_TIME = 20;
 	public static final int LOCAL_OTHER_POINTS_NUM_1 = 5000;
 	public static final int LOCAL_OTHER_POINTS_NUM_2 = 10000;
 	public static final int LOCAL_OTHER_POINTS_NUM_3 = 20000;
 	public static final int LOCAL_OTHER_REANIMATION_NUM = 3;
 	
 	public static final int LOCAL_PLAYTIME_1 = 60;
 	public static final int LOCAL_PLAYTIME_2 = 300;
 	public static final int LOCAL_PLAYTIME_3 = 600;
 	
 	public static final int GLOBAL_DESTROY_ASTEROIDS_NUM_1 = 100;
 	public static final int GLOBAL_DESTROY_ASTEROIDS_NUM_2 = 500;
 	public static final int GLOBAL_DESTROY_ASTEROIDS_NUM_3 = 1000;
 	
 	public static final int GLOBAL_DESTROY_ASTEROIDS_TOTAL_NUM_1 = 500;
 	public static final int GLOBAL_DESTROY_ASTEROIDS_TOTAL_NUM_2 = 2500;
 	public static final int GLOBAL_DESTROY_ASTEROIDS_TOTAL_NUM_3 = 5000;
 	
 	public static final int GLOBAL_PLAYTIME_1 = 600;
 	public static final int GLOBAL_PLAYTIME_2 = 3600;
 	public static final int GLOBAL_PLAYTIME_3 = 18000;
 	
 	public static final int GLOBAL_PLAY_COUNT_NUM = 100;
 	
 	public static final int GLOBAL_NUM_POWERUPS = 8;
 	public static final int GLOBAL_NUM_UPGRADES = 36;
 	public static final int GLOBAL_NUM_ACHIEVEMENTS = 62;
 	
 	
 	// local achievments - destroy asteroids
 	public static Achievement localDestroyAsteroidsWithDash1 = new Achievement("achievement_local_destroy_asteroids_with_dash_1", R.drawable.icon_dash, LOCAL_DESTROY_ASTEROIDS_DASH_NUM_1);
 	public static Achievement localDestroyAsteroidsWithDash2 = new Achievement("achievement_local_destroy_asteroids_with_dash_2", R.drawable.icon_dash, LOCAL_DESTROY_ASTEROIDS_DASH_NUM_2);
 	public static Achievement localDestroyAsteroidsWithDash3 = new Achievement("achievement_local_destroy_asteroids_with_dash_3", R.drawable.icon_dash, LOCAL_DESTROY_ASTEROIDS_DASH_NUM_3);
 	
 	public static Achievement localDestroyAsteroidsWithDrill1 = new Achievement("achievement_local_destroy_asteroids_with_drill_1", R.drawable.icon_drill, LOCAL_DESTROY_ASTEROIDS_DRILL_NUM_1);
 	public static Achievement localDestroyAsteroidsWithDrill2 = new Achievement("achievement_local_destroy_asteroids_with_drill_2", R.drawable.icon_drill, LOCAL_DESTROY_ASTEROIDS_DRILL_NUM_2);
 	public static Achievement localDestroyAsteroidsWithDrill3 = new Achievement("achievement_local_destroy_asteroids_with_drill_3", R.drawable.icon_drill, LOCAL_DESTROY_ASTEROIDS_DRILL_NUM_3);
 	
 	public static Achievement localDestroyAsteroidsWithMagnet1 = new Achievement("achievement_local_destroy_asteroids_with_magnet_1", R.drawable.icon_magnet, LOCAL_DESTROY_ASTEROIDS_MAGNET_NUM_1);
 	public static Achievement localDestroyAsteroidsWithMagnet2 = new Achievement("achievement_local_destroy_asteroids_with_magnet_2", R.drawable.icon_magnet, LOCAL_DESTROY_ASTEROIDS_MAGNET_NUM_2);
 	public static Achievement localDestroyAsteroidsWithMagnet3 = new Achievement("achievement_local_destroy_asteroids_with_magnet_3", R.drawable.icon_magnet, LOCAL_DESTROY_ASTEROIDS_MAGNET_NUM_3);
 	
 	public static Achievement localDestroyAsteroidsWithBlackHole1 = new Achievement("achievement_local_destroy_asteroids_with_black_hole_1", R.drawable.icon_black_hole, LOCAL_DESTROY_ASTEROIDS_BLACK_HOLE_NUM_1);
 	public static Achievement localDestroyAsteroidsWithBlackHole2 = new Achievement("achievement_local_destroy_asteroids_with_black_hole_2", R.drawable.icon_black_hole, LOCAL_DESTROY_ASTEROIDS_BLACK_HOLE_NUM_2);
 	public static Achievement localDestroyAsteroidsWithBlackHole3 = new Achievement("achievement_local_destroy_asteroids_with_black_hole_3", R.drawable.icon_black_hole, LOCAL_DESTROY_ASTEROIDS_BLACK_HOLE_NUM_3);
 	
 	public static Achievement localDestroyAsteroidsWithBumper1 = new Achievement("achievement_local_destroy_asteroids_with_bumper_1", R.drawable.icon_bumper, LOCAL_DESTROY_ASTEROIDS_BLACK_HOLE_NUM_1);
 	public static Achievement localDestroyAsteroidsWithBumper2 = new Achievement("achievement_local_destroy_asteroids_with_bumper_2", R.drawable.icon_bumper, LOCAL_DESTROY_ASTEROIDS_BLACK_HOLE_NUM_2);
 	public static Achievement localDestroyAsteroidsWithBumper3 = new Achievement("achievement_local_destroy_asteroids_with_bumper_3", R.drawable.icon_bumper, LOCAL_DESTROY_ASTEROIDS_BLACK_HOLE_NUM_3);
 	
 	public static Achievement localDestroyAsteroidsWithBomb1 = new Achievement("achievement_local_destroy_asteroids_with_bomb_1", R.drawable.icon_bomb, LOCAL_DESTROY_ASTEROIDS_BOMB_NUM_1);
 	public static Achievement localDestroyAsteroidsWithBomb2 = new Achievement("achievement_local_destroy_asteroids_with_bomb_2", R.drawable.icon_bomb, LOCAL_DESTROY_ASTEROIDS_BOMB_NUM_2);
 	public static Achievement localDestroyAsteroidsWithBomb3 = new Achievement("achievement_local_destroy_asteroids_with_bomb_3", R.drawable.icon_bomb, LOCAL_DESTROY_ASTEROIDS_BOMB_NUM_3);
 
 	// local achievements - powerups
 	public static Achievement localDashActivateDrops = new Achievement("achievement_local_dash_activate_drops", R.drawable.icon_dash, LOCAL_DASH_ACTIVATE_DROPS_NUM);
 	public static Achievement localSmallDashDestroy = new Achievement("achievement_local_small_dash_destroy", R.drawable.icon_small, LOCAL_SMALL_DASH_DESTROY_ASTEROIDS_NUM);
 	public static Achievement localSlowLongTime = new Achievement("achievement_local_slow_long_time", R.drawable.icon_slow, LOCAL_SLOW_LONG_TIME);
 	public static Achievement localInvulnerabilityPassThrough = new Achievement("achievement_local_invulnerability_pass_through", R.drawable.icon_invulnerability, LOCAL_INVULNERABILITY_PASS_THROUGH_NUM);
 	public static Achievement localDrillUseMaximumTime = new Achievement("achievement_local_drill_use_maximum_time", R.drawable.icon_drill);
 	public static Achievement localBlackHoleTrifecta = new Achievement("achievement_local_black_hole_trifecta", R.drawable.icon_black_hole, LOCAL_BLACK_HOLE_TRIFECTA_NUM);
 	public static Achievement localMagnetHoldInPlace = new Achievement("achievement_local_magnet_hold_in_place", R.drawable.icon_magnet, LOCAL_MAGNET_HOLD_IN_PLACE_NUM);
 	public static Achievement localBumperBetween = new Achievement("achievement_local_bumper_between", R.drawable.icon_bumper);
 	public static Achievement localBombDropBomb = new Achievement("achievement_local_bomb_activate_bomb", R.drawable.icon_bomb);
 
 	// local achievements - other
 	public static Achievement localOtherStayInPlace = new Achievement("achievement_local_other_stay_in_place", R.drawable.icon_general, LOCAL_OTHER_STAY_IN_PLACE_TIME);
 	public static Achievement localOtherPoints1 = new Achievement("achievement_local_other_points_1", R.drawable.icon_general, LOCAL_OTHER_POINTS_NUM_1);
 	public static Achievement localOtherPoints2 = new Achievement("achievement_local_other_points_2", R.drawable.icon_general, LOCAL_OTHER_POINTS_NUM_2);
 	public static Achievement localOtherPoints3 = new Achievement("achievement_local_other_points_3", R.drawable.icon_general, LOCAL_OTHER_POINTS_NUM_3);
 	public static Achievement localOtherReanimation = new Achievement("achievement_local_other_reanimation", R.drawable.icon_general, LOCAL_OTHER_REANIMATION_NUM);
 	
 	// local achievements - playtime
 	public static Achievement localPlaytime1 = new Achievement("achievement_local_playtime_1", R.drawable.icon_slow, LOCAL_PLAYTIME_1/60);
 	public static Achievement localPlaytime2 = new Achievement("achievement_local_playtime_2", R.drawable.icon_slow, LOCAL_PLAYTIME_2/60);
 	public static Achievement localPlaytime3 = new Achievement("achievement_local_playtime_3", R.drawable.icon_slow, LOCAL_PLAYTIME_3/60);
 
 	// global achievements - destroy asteroids
 	public static Achievement globalDestroyAsteroidsWithDash1 = new Achievement("achievement_global_destroy_asteroids_with_dash_1", R.drawable.icon_dash, GLOBAL_DESTROY_ASTEROIDS_NUM_1, GlobalStatistics.ID_ASTEROIDS_DESTROYED_BY_DASH);
 	public static Achievement globalDestroyAsteroidsWithDash2 = new Achievement("achievement_global_destroy_asteroids_with_dash_2", R.drawable.icon_dash, GLOBAL_DESTROY_ASTEROIDS_NUM_2, GlobalStatistics.ID_ASTEROIDS_DESTROYED_BY_DASH);
 	public static Achievement globalDestroyAsteroidsWithDash3 = new Achievement("achievement_global_destroy_asteroids_with_dash_3", R.drawable.icon_dash, GLOBAL_DESTROY_ASTEROIDS_NUM_3, GlobalStatistics.ID_ASTEROIDS_DESTROYED_BY_DASH);
 	
 	public static Achievement globalDestroyAsteroidsWithDrill1 = new Achievement("achievement_global_destroy_asteroids_with_drill_1", R.drawable.icon_drill, GLOBAL_DESTROY_ASTEROIDS_NUM_1, GlobalStatistics.ID_ASTEROIDS_DESTROYED_BY_DRILL);
 	public static Achievement globalDestroyAsteroidsWithDrill2 = new Achievement("achievement_global_destroy_asteroids_with_drill_2", R.drawable.icon_drill, GLOBAL_DESTROY_ASTEROIDS_NUM_2, GlobalStatistics.ID_ASTEROIDS_DESTROYED_BY_DRILL);
 	public static Achievement globalDestroyAsteroidsWithDrill3 = new Achievement("achievement_global_destroy_asteroids_with_drill_3", R.drawable.icon_drill, GLOBAL_DESTROY_ASTEROIDS_NUM_3, GlobalStatistics.ID_ASTEROIDS_DESTROYED_BY_DRILL);
 	
 	public static Achievement globalDestroyAsteroidsWithMagnet1 = new Achievement("achievement_global_destroy_asteroids_with_magnet_1", R.drawable.icon_magnet, GLOBAL_DESTROY_ASTEROIDS_NUM_1, GlobalStatistics.ID_ASTEROIDS_DESTROYED_BY_MAGNET);
 	public static Achievement globalDestroyAsteroidsWithMagnet2 = new Achievement("achievement_global_destroy_asteroids_with_magnet_2", R.drawable.icon_magnet, GLOBAL_DESTROY_ASTEROIDS_NUM_2, GlobalStatistics.ID_ASTEROIDS_DESTROYED_BY_MAGNET);
 	public static Achievement globalDestroyAsteroidsWithMagnet3 = new Achievement("achievement_global_destroy_asteroids_with_magnet_3", R.drawable.icon_magnet, GLOBAL_DESTROY_ASTEROIDS_NUM_3, GlobalStatistics.ID_ASTEROIDS_DESTROYED_BY_MAGNET);
 	
 	public static Achievement globalDestroyAsteroidsWithBlackHole1 = new Achievement("achievement_global_destroy_asteroids_with_black_hole_1", R.drawable.icon_black_hole, GLOBAL_DESTROY_ASTEROIDS_NUM_1, GlobalStatistics.ID_ASTEROIDS_DESTROYED_BY_BLACK_HOLE);
 	public static Achievement globalDestroyAsteroidsWithBlackHole2 = new Achievement("achievement_global_destroy_asteroids_with_black_hole_2", R.drawable.icon_black_hole, GLOBAL_DESTROY_ASTEROIDS_NUM_2, GlobalStatistics.ID_ASTEROIDS_DESTROYED_BY_BLACK_HOLE);
 	public static Achievement globalDestroyAsteroidsWithBlackHole3 = new Achievement("achievement_global_destroy_asteroids_with_black_hole_3", R.drawable.icon_black_hole, GLOBAL_DESTROY_ASTEROIDS_NUM_3, GlobalStatistics.ID_ASTEROIDS_DESTROYED_BY_BLACK_HOLE);
 	
 	public static Achievement globalDestroyAsteroidsWithBumper1 = new Achievement("achievement_global_destroy_asteroids_with_bumper_1", R.drawable.icon_bumper, GLOBAL_DESTROY_ASTEROIDS_NUM_1, GlobalStatistics.ID_ASTEROIDS_DESTROYED_BY_BUMPER);
 	public static Achievement globalDestroyAsteroidsWithBumper2 = new Achievement("achievement_global_destroy_asteroids_with_bumper_2", R.drawable.icon_bumper, GLOBAL_DESTROY_ASTEROIDS_NUM_2, GlobalStatistics.ID_ASTEROIDS_DESTROYED_BY_BUMPER);
 	public static Achievement globalDestroyAsteroidsWithBumper3 = new Achievement("achievement_global_destroy_asteroids_with_bumper_3", R.drawable.icon_bumper, GLOBAL_DESTROY_ASTEROIDS_NUM_3, GlobalStatistics.ID_ASTEROIDS_DESTROYED_BY_BUMPER);
 	
 	public static Achievement globalDestroyAsteroidsWithBomb1 = new Achievement("achievement_global_destroy_asteroids_with_bomb_1", R.drawable.icon_bomb, GLOBAL_DESTROY_ASTEROIDS_NUM_1, GlobalStatistics.ID_ASTEROIDS_DESTROYED_BY_BOMB);
 	public static Achievement globalDestroyAsteroidsWithBomb2 = new Achievement("achievement_global_destroy_asteroids_with_bomb_2", R.drawable.icon_bomb, GLOBAL_DESTROY_ASTEROIDS_NUM_2, GlobalStatistics.ID_ASTEROIDS_DESTROYED_BY_BOMB);
 	public static Achievement globalDestroyAsteroidsWithBomb3 = new Achievement("achievement_global_destroy_asteroids_with_bomb_3", R.drawable.icon_bomb, GLOBAL_DESTROY_ASTEROIDS_NUM_3, GlobalStatistics.ID_ASTEROIDS_DESTROYED_BY_BOMB);
 	
 	public static Achievement globalDestroyAsteroidsTotal1 = new Achievement("achievement_global_destroy_asteroids_total_1", R.drawable.icon_general, GLOBAL_DESTROY_ASTEROIDS_TOTAL_NUM_1, GlobalStatistics.ID_ASTEROIDS_DESTROYED_TOTAL);
 	public static Achievement globalDestroyAsteroidsTotal2 = new Achievement("achievement_global_destroy_asteroids_total_2", R.drawable.icon_general, GLOBAL_DESTROY_ASTEROIDS_TOTAL_NUM_2, GlobalStatistics.ID_ASTEROIDS_DESTROYED_TOTAL);
 	public static Achievement globalDestroyAsteroidsTotal3 = new Achievement("achievement_global_destroy_asteroids_total_3", R.drawable.icon_general, GLOBAL_DESTROY_ASTEROIDS_TOTAL_NUM_3, GlobalStatistics.ID_ASTEROIDS_DESTROYED_TOTAL);
 
 	// global achievements - playtime/play count
 	public static Achievement globalPlaytime1 = new Achievement("achievement_global_playtime_1", R.drawable.icon_slow, GLOBAL_PLAYTIME_1/60, GlobalStatistics.ID_TIME_PLAYED);
 	public static Achievement globalPlaytime2 = new Achievement("achievement_global_playtime_2", R.drawable.icon_slow, GLOBAL_PLAYTIME_2/60, GlobalStatistics.ID_TIME_PLAYED);
 	public static Achievement globalPlaytime3 = new Achievement("achievement_global_playtime_3", R.drawable.icon_slow, GLOBAL_PLAYTIME_3/60, GlobalStatistics.ID_TIME_PLAYED);
 	
 	public static Achievement globalPlayCount = new Achievement("achievement_global_play_count", R.drawable.icon_general, GLOBAL_PLAY_COUNT_NUM, GlobalStatistics.ID_TIMES_PLAYED);
 	
 	// global achievements - unlocking/purchasing
 	public static Achievement globalUnlockAllPowerups = new Achievement("achievement_global_unlock_all_powerups", R.drawable.icon_general, GLOBAL_NUM_POWERUPS);
 	public static Achievement globalPurchaseAllUpgrades = new Achievement("achievement_global_purchase_all_upgrades", R.drawable.icon_general, GLOBAL_NUM_UPGRADES);
 	public static Achievement globalUnlockAllAchievements = new Achievement("achievement_global_unlock_all_achievements", R.drawable.icon_general, GLOBAL_NUM_ACHIEVEMENTS);
 		
 	// list of local achievements unlocked during current playthrough
 	public static ArrayList<Achievement> localAchievements = new ArrayList<Achievement>();
 
 	private static boolean initialized = false;
 	
 	// list of all achievements
 	private static ArrayList<Achievement> achievements = new ArrayList<Achievement>();
 	
 	// populate list of all achievements
 	static {
 		
 		// other
 		achievements.add(localOtherStayInPlace);
 		achievements.add(localOtherPoints1);
 		achievements.add(localOtherPoints2);
 		achievements.add(localOtherPoints3);
 		achievements.add(localOtherReanimation);
 		
 		// dash
 		achievements.add(localDestroyAsteroidsWithDash1);
 		achievements.add(localDestroyAsteroidsWithDash2);
 		achievements.add(localDestroyAsteroidsWithDash3);
 		achievements.add(localDashActivateDrops);
 		achievements.add(globalDestroyAsteroidsWithDash1);
 		achievements.add(globalDestroyAsteroidsWithDash2);
 		achievements.add(globalDestroyAsteroidsWithDash3);
 
 		// small
 		achievements.add(localSmallDashDestroy);
 		
 		// slow
 		achievements.add(localSlowLongTime);
 		
 		// invulnerability
 		achievements.add(localInvulnerabilityPassThrough);
 		
 		// drill
 		achievements.add(localDestroyAsteroidsWithDrill1);
 		achievements.add(localDestroyAsteroidsWithDrill2);
 		achievements.add(localDestroyAsteroidsWithDrill3);
 		achievements.add(localDrillUseMaximumTime);
 		achievements.add(globalDestroyAsteroidsWithDrill1);
 		achievements.add(globalDestroyAsteroidsWithDrill2);
 		achievements.add(globalDestroyAsteroidsWithDrill3);
 		
 		// magnet
 		achievements.add(localDestroyAsteroidsWithMagnet1);
 		achievements.add(localDestroyAsteroidsWithMagnet2);
 		achievements.add(localDestroyAsteroidsWithMagnet3);
 		achievements.add(localMagnetHoldInPlace);
 		achievements.add(globalDestroyAsteroidsWithMagnet1);
 		achievements.add(globalDestroyAsteroidsWithMagnet2);
 		achievements.add(globalDestroyAsteroidsWithMagnet3);
 
 		// black hole
 		achievements.add(localDestroyAsteroidsWithBlackHole1);
 		achievements.add(localDestroyAsteroidsWithBlackHole2);
 		achievements.add(localDestroyAsteroidsWithBlackHole3);
 		achievements.add(localBlackHoleTrifecta);
 		achievements.add(globalDestroyAsteroidsWithBlackHole1);
 		achievements.add(globalDestroyAsteroidsWithBlackHole2);
 		achievements.add(globalDestroyAsteroidsWithBlackHole3);
 
 		// bumper
 		achievements.add(localDestroyAsteroidsWithBumper1);
 		achievements.add(localDestroyAsteroidsWithBumper2);
 		achievements.add(localDestroyAsteroidsWithBumper3);
 		achievements.add(localBumperBetween);
 		achievements.add(globalDestroyAsteroidsWithBumper1);
 		achievements.add(globalDestroyAsteroidsWithBumper2);
 		achievements.add(globalDestroyAsteroidsWithBumper3);
 
 		// bomb
 		achievements.add(localDestroyAsteroidsWithBomb1);
 		achievements.add(localDestroyAsteroidsWithBomb2);
 		achievements.add(localDestroyAsteroidsWithBomb3);
 		achievements.add(localBombDropBomb);
 		achievements.add(globalDestroyAsteroidsWithBomb1);
 		achievements.add(globalDestroyAsteroidsWithBomb2);
 		achievements.add(globalDestroyAsteroidsWithBomb3);
 		
 		// total
 		achievements.add(globalDestroyAsteroidsTotal1);
 		achievements.add(globalDestroyAsteroidsTotal2);
 		achievements.add(globalDestroyAsteroidsTotal3);
 
 		// playtime
 		achievements.add(localPlaytime1);
 		achievements.add(localPlaytime2);
 		achievements.add(localPlaytime3);
 		achievements.add(globalPlaytime1);
 		achievements.add(globalPlaytime2);
 		achievements.add(globalPlaytime3);
 		
 		// play count
 		achievements.add(globalPlayCount);
 		
 		// unlocking/purchasing stuff
 		achievements.add(globalUnlockAllPowerups);
 		achievements.add(globalPurchaseAllUpgrades);
 		achievements.add(globalUnlockAllAchievements);
 	}
 	
 	/**
 	 * Checks global achievements based on GlobalStatistics
 	 */
 	public static void checkGlobalAchievements() {
 		
 		Statistics globalStatistics = GlobalStatistics.getInstance();
 		
 		// destroy asteroids - dash
 		if (globalStatistics.asteroidsDestroyedByDash >= GLOBAL_DESTROY_ASTEROIDS_NUM_1) {
 			unlockLocalAchievement(globalDestroyAsteroidsWithDash1);
 		}
 		if (globalStatistics.asteroidsDestroyedByDash >= GLOBAL_DESTROY_ASTEROIDS_NUM_2) {
 			unlockLocalAchievement(globalDestroyAsteroidsWithDash2);
 		}
 		if (globalStatistics.asteroidsDestroyedByDash >= GLOBAL_DESTROY_ASTEROIDS_NUM_3) {
 			unlockLocalAchievement(globalDestroyAsteroidsWithDash3);
 		}
 		
 		// destroy asteroids - drill
 		if (globalStatistics.asteroidsDestroyedByDrill >= GLOBAL_DESTROY_ASTEROIDS_NUM_1) {
 			unlockLocalAchievement(globalDestroyAsteroidsWithDrill1);
 		}
 		if (globalStatistics.asteroidsDestroyedByDrill >= GLOBAL_DESTROY_ASTEROIDS_NUM_2) {
 			unlockLocalAchievement(globalDestroyAsteroidsWithDrill2);
 		}
 		if (globalStatistics.asteroidsDestroyedByDrill >= GLOBAL_DESTROY_ASTEROIDS_NUM_3) {
 			unlockLocalAchievement(globalDestroyAsteroidsWithDrill3);
 		}
 
 		// destroy asteroids - magnet
 		if (globalStatistics.asteroidsDestroyedByMagnet >= GLOBAL_DESTROY_ASTEROIDS_NUM_1) {
 			unlockLocalAchievement(globalDestroyAsteroidsWithMagnet1);
 		}
 		if (globalStatistics.asteroidsDestroyedByMagnet >= GLOBAL_DESTROY_ASTEROIDS_NUM_2) {
 			unlockLocalAchievement(globalDestroyAsteroidsWithMagnet2);
 		}
 		if (globalStatistics.asteroidsDestroyedByMagnet >= GLOBAL_DESTROY_ASTEROIDS_NUM_3) {
 			unlockLocalAchievement(globalDestroyAsteroidsWithMagnet3);
 		}
 
 		// destroy asteroids - black hole
 		if (globalStatistics.asteroidsDestroyedByBlackHole >= GLOBAL_DESTROY_ASTEROIDS_NUM_1) {
 			unlockLocalAchievement(globalDestroyAsteroidsWithBlackHole1);
 		}
 		if (globalStatistics.asteroidsDestroyedByBlackHole >= GLOBAL_DESTROY_ASTEROIDS_NUM_2) {
 			unlockLocalAchievement(globalDestroyAsteroidsWithBlackHole2);
 		}
 		if (globalStatistics.asteroidsDestroyedByBlackHole >= GLOBAL_DESTROY_ASTEROIDS_NUM_3) {
 			unlockLocalAchievement(globalDestroyAsteroidsWithBlackHole3);
 		}
 
 		// destroy asteroids - bumper
 		if (globalStatistics.asteroidsDestroyedByBumper >= GLOBAL_DESTROY_ASTEROIDS_NUM_1) {
 			unlockLocalAchievement(globalDestroyAsteroidsWithBumper1);
 		}
 		if (globalStatistics.asteroidsDestroyedByBumper >= GLOBAL_DESTROY_ASTEROIDS_NUM_2) {
 			unlockLocalAchievement(globalDestroyAsteroidsWithBumper2);
 		}
 		if (globalStatistics.asteroidsDestroyedByBumper >= GLOBAL_DESTROY_ASTEROIDS_NUM_3) {
 			unlockLocalAchievement(globalDestroyAsteroidsWithBumper3);
 		}
 
 		// destroy asteroids - bomb
 		if (globalStatistics.asteroidsDestroyedByBomb >= GLOBAL_DESTROY_ASTEROIDS_NUM_1) {
 			unlockLocalAchievement(globalDestroyAsteroidsWithBomb1);
 		}
 		if (globalStatistics.asteroidsDestroyedByBomb >= GLOBAL_DESTROY_ASTEROIDS_NUM_2) {
 			unlockLocalAchievement(globalDestroyAsteroidsWithBomb2);
 		}
 		if (globalStatistics.asteroidsDestroyedByBomb >= GLOBAL_DESTROY_ASTEROIDS_NUM_3) {
 			unlockLocalAchievement(globalDestroyAsteroidsWithBomb3);
 		}
 		
 		// calculate total number of asteroids destroyed
 		int asteroidsDestroyedTotal = globalStatistics.asteroidsDestroyedByBomb +
 				globalStatistics.asteroidsDestroyedByBumper +
 				globalStatistics.asteroidsDestroyedByDash +
 				globalStatistics.asteroidsDestroyedByDrill +
 				globalStatistics.asteroidsDestroyedByMagnet +
 				globalStatistics.asteroidsDestroyedByBlackHole;
 
 		// destroy asteroids - total
 		if (asteroidsDestroyedTotal >= GLOBAL_DESTROY_ASTEROIDS_TOTAL_NUM_1) {
 			unlockLocalAchievement(globalDestroyAsteroidsTotal1);
 		}
 		if (asteroidsDestroyedTotal >= GLOBAL_DESTROY_ASTEROIDS_TOTAL_NUM_2) {
 			unlockLocalAchievement(globalDestroyAsteroidsTotal2);
 		}
 		if (asteroidsDestroyedTotal >= GLOBAL_DESTROY_ASTEROIDS_TOTAL_NUM_3) {
 			unlockLocalAchievement(globalDestroyAsteroidsTotal3);
 		}
 		
 		//  playtime
 		if (globalStatistics.timePlayed >= GLOBAL_PLAYTIME_1) {
 			unlockLocalAchievement(globalPlaytime1);
 		}
 		if (globalStatistics.timePlayed >= GLOBAL_PLAYTIME_2) {
 			unlockLocalAchievement(globalPlaytime2);
 		}
 		if (globalStatistics.timePlayed >= GLOBAL_PLAYTIME_3) {
 			unlockLocalAchievement(globalPlaytime3);
 		}
 		
 		// playcount
 		if (globalStatistics.timesPlayed >= GLOBAL_PLAY_COUNT_NUM) {
 			unlockLocalAchievement(globalPlayCount);
 		}
 		
 		// unlock all powerups/upgrades
 		Upgrades.checkAchievements();
 		
 		// unlock all achievements
 		int i;
		for (i = 0; i < achievements.size(); i++) {
 			if (!achievements.get(i).getValue()) {
 				break;
 			}
 		}
		if (i == achievements.size()) {
 			unlockLocalAchievement(globalUnlockAllAchievements);
 		}
 	}
 	
 	/**
 	 * Loads achievement values from application preferences
 	 * @param sharedPreferences preferences to load from
 	 */
 	public static void load(SharedPreferences sharedPreferences) {
 		for (Achievement achievement : achievements) {
 			achievement.load(sharedPreferences);
 		}
 		
 		initialized = true;
 	}
 	
 	/**
 	 * Saves achievement values to application preferencesEditor
 	 * @param sharedPreferencesEditor preferencesEditor to save to
 	 */
 	public static void save(SharedPreferences.Editor sharedPreferencesEditor) {
 		for (Achievement achievement : achievements) {
 			achievement.save(sharedPreferencesEditor);
 		}
 	}
 	
 	/**
 	 * Returns true if the achievements were initialized from application preferences
 	 * @return true if the achievements were initialized from application preferences
 	 */
 	public static boolean initialized() {
 		return initialized;
 	}
 	
 	/**
 	 * Adds achievement to list of local achievements if its not already unlocked
 	 * @param achievement achievement to add
 	 * @return true if achievement is newly unlocked
 	 */
 	public static boolean unlockLocalAchievement(Achievement achievement) {
 		
 		// make sure its not already unlocked
 		if (!achievement.getValue()) {
 			achievement.unlock();
 			localAchievements.add(achievement);
 			return true;
 		}
 		
 		return false;
 	}
 
 	/**
 	 * Removes all achievements from list of local achievements
 	 */
 	public static void resetLocalAchievements() {
 		localAchievements.clear();
 	}
 	
 	/**
 	 * Returns list of locked achievements
 	 * @return list of locked achievements
 	 */
 	public static List<Achievement> getLockedAchievements() {
 		ArrayList<Achievement> lockedAchievements = new ArrayList<Achievement>();
 		
 		for (Achievement achievement : achievements) {
 			if (!achievement.getValue()) {
 				lockedAchievements.add(achievement);
 			}
 		}
 		return lockedAchievements;
 	}
 	
 	/**
 	 * Returns list of unlocked achievements
 	 * @return list of unlocked achievements
 	 */
 	public static List<Achievement> getUnlockedAchievements() {
 		ArrayList<Achievement> unlockedAchievements = new ArrayList<Achievement>();
 		
 		for (Achievement achievement : achievements) {
 			if (achievement.getValue()) {
 				unlockedAchievements.add(achievement);
 			}
 		}
 		return unlockedAchievements;
 	}
 
 	/**
 	 * Loads resources for achievements
 	 * @param resources resources to use
 	 * @param packageName package name
 	 */
 	public static void loadResources(Resources resources, String packageName) {
 		for (Achievement achievement : achievements) {
 			achievement.loadResources(resources, packageName);
 		}
 	}
 
 	/**
 	 * Returns list of all achievements
 	 * @return list of all achievements
 	 */
 	public static List<Achievement> getAchievements() {
 		return achievements;
 	}
 
 	/**
 	 * Resets all achievements
 	 * @param sharedPreferencesEditor preferences to save to
 	 */
 	public static void reset(Editor sharedPreferencesEditor) {
 		
 		for (Achievement achievement : achievements) {
 			achievement.reset();
 		}
 		
 		save(sharedPreferencesEditor);
 	}
 	
 	/**
 	 * Returns completion percentage of achievements unlocked
 	 * @return completion percentage of achievements unlocked
 	 */
 	public static float completionPercentage() {
 		
 		int numAchievementsUnlocked = 0;
 		
 		for (Achievement achievement : achievements) {
 			if (achievement.getValue()) {
 				numAchievementsUnlocked++;
 			}
 		}
 		
 		return 1.f*numAchievementsUnlocked/achievements.size();
 	}
 }
