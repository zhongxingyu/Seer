 package com.glowman.spaceunit.game.balance;
 
import android.util.Log;
 import com.glowman.spaceunit.game.mapObject.enemy.EnemyTypeENUM;
 import com.glowman.spaceunit.game.strategy.GameStrategy;
 
 /**
  *
  */
 public class RespawnFrequencyCollector {
 
 	//TODO game balance here
 	public static float getFrequency(String enemyType, int gameType, float timeState) {
 		float result;
 		if (enemyType == EnemyTypeENUM.ASTEROID) {
 			if (gameType == GameStrategy.RUN_GAME) {
 				if (timeState < 3) { //чтобы вначале небыло простоя
 					result = .1f;
 				} else {
 					result =  0.1f * (timeState / 200); //after 200 sec -- 0.1 frequency
 				}
 			} else {
 				if (timeState < 3) {
 					result = .1f;
 				} else {
					result = 0.1f * (timeState / 200);
 				}
 			}
 		}
 		else if (enemyType == EnemyTypeENUM.MINE) {
 			result = .1f * (timeState / 1000);
 		}
 		else if (enemyType == EnemyTypeENUM.CRAZY_MINE) {
 			result = .1f * (timeState / 1500);
 		}
 		else if (enemyType == EnemyTypeENUM.ALIEN) {
 			result = .1f * (timeState / 3000);
 		}
 		else {
 			throw new Error("unknown enemy : " + enemyType);
 		}
 
 		return result;
 	}
 }
