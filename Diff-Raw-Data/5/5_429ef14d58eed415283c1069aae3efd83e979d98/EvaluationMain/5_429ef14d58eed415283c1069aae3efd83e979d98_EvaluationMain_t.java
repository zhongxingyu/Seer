 package ai;
 
 import ui.Coordinate;
 
 public class EvaluationMain implements AI {
 
 	static int border = 5;
 	static int ansX = 0, ansY = 0;
 
 	public double search(boolean[][] yours, boolean[][] enemys, int times) {
 		if (times == 0)
 			return 0;
 		if (Determine.canMoveNum(yours,enemys)==0){
			return (-9999999.99-search(enemys, yours, times - 1));
 		}
 		int current = EvaluationSituation.Score(yours, enemys);
 		int x, y;
 		double max = -9999999.99;
 		int deltaMobility[][] = new int[8][8];
 		int deltaStableDiscs[][] = new int[8][8];
 		int deltaPotentialMobility[][] = new int[8][8];
 		int gainATempo[][] = new int[8][8];
 		int gainNum;
 		boolean[][] enemysCanMove = new boolean[8][8];
 		double value[][] = new double[8][8];
 		for (x = 0; x < 8; x++) {
 			for (y = 0; y < 8; y++) {
 				gainNum = Determine.judge(new Coordinate(x, y), yours, enemys).length;
 				if (gainNum == 0)
 					continue;
 				yours[x][y] = true;
 				int i, j;
 				for (i = 0; i < 8; i++) {
 					for (j = 0; j < 8; j++) {
 						enemysCanMove[x][y] = (Determine.judge(new Coordinate(
 								x, y), enemys, yours).length > 0);
 					}
 				}
 				int yourMobility = EvaluationMobility.Score(yours, enemys);
 				int enemysMobility = EvaluationMobility.Score(enemys, yours);
 				deltaMobility[x][y] = yourMobility - enemysMobility;
 				int yourStableDiscs = EvaluationStableDiscs.Num(yours, enemys);
 				int enemysStableDiscs = EvaluationStableDiscs
 						.Num(enemys, yours);
 				deltaStableDiscs[x][y] = yourStableDiscs - enemysStableDiscs;
 				int yourPotentialMobility = EvaluationPotentialMobility.Score(
 						yours, enemys);
 				int enemysPotentialMobility = EvaluationPotentialMobility
 						.Score(enemys, yours);
 				deltaPotentialMobility[x][y] = yourPotentialMobility
 						- enemysPotentialMobility;
 				int yourGainATempo = EvaluationGainATempo.pass(yours, enemys);
 				int enemysGainATempo = EvaluationGainATempo.pass(enemys, yours);
 				gainATempo[x][y] = yourGainATempo - enemysGainATempo * 3;
 				value[x][y] = (double) deltaMobility[x][y]
 						* (64 - (double) current) / 4
 						+ (double) (deltaStableDiscs[x][y] * (6 * current * current - 16))
 						+ (double) deltaPotentialMobility[x][y]
 						* (double) (64 - current) / 12
 						+ (double) gainATempo[x][y] * 1250 + (double) gainNum
 						/ 2 + (double) EvaluationScore.Score(yours) - 2.4
 						* (double) EvaluationScore.Score(enemysCanMove);
 				if (times % 2 == 1) {
 					value[x][y] -= search(enemys, yours, times - 1);
 				}
 				yours[x][y] = false;
 			}
 		}
 		max = -9999999.99;
 		int tmpX = 0, tmpY = 0;
 		for (x = 0; x < 8; x++) {
 			for (y = 0; y < 8; y++) {
 				if (Determine.judge(new Coordinate(x, y), yours, enemys).length == 0)
 					continue;
 				if (max < value[x][y]) {
 					max = value[x][y];
 					tmpX = x;
 					tmpY = y;
 				}
 			}
 		}
 		if (times == border) {
 			ansX = tmpX;
 			ansY = tmpY;
 			return 0;
 		} else {
 			if (times % 2 == 1)
 				return (max);
 			else {
 				if (!yours[tmpX][tmpY]) {
 					yours[tmpX][tmpY] = true;
					max -= search(enemys, yours, times - 1);
 					yours[tmpX][tmpY] = false;
 					return max;
 				} else{
 					return -search(enemys, yours, times - 1)-9999999.99;
 				}
 			}
 		}
 	}
 
 	@Override
 	public Coordinate move(boolean[][] yours, boolean[][] enemys) {
 		boolean restoreYours[][] = new boolean[8][8];
 		boolean restoreEnemys[][] = new boolean[8][8];
 		int li, lj;
 		for (li = 0; li < 8; li++) {
 			for (lj = 0; lj < 8; lj++) {
 				restoreYours[li][lj] = yours[li][lj];
 				restoreEnemys[li][lj] = enemys[li][lj];
 			}
 		}
 		search(yours, enemys, border);
 		int tmX = ansX;
 		int tmY = ansY;
 		if ((yours[tmX][tmY])
 				|| (enemys[tmX][tmY])
 				|| (Determine.judge(new Coordinate(tmX, tmY), yours, enemys).length == 0)) {
 			for (li = 0; li < 8; li++) {
 				for (lj = 0; lj < 8; lj++) {
 					if ((!yours[li][lj])
 							&& (!enemys[li][lj])
 							&& (Determine.judge(new Coordinate(li, lj), yours,
 									enemys).length > 0)) {
 						ansX = li;
 						ansY = lj;
 					}
 					/*
 					 * if((restoreYours[li][lj]!=yours[li][lj])||(restoreEnemys[li
 					 * ][lj]!=enemys[li][lj])) System.out.println("Error");
 					 */
 				}
 			}
 		}
 		return new Coordinate(ansX, ansY);
 	}
 
 }
