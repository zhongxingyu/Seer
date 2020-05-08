 package com.jpii.navalbattle.game.turn;
 
 import com.jpii.navalbattle.data.Constants;
 import com.jpii.navalbattle.game.NavalGame;
 import com.jpii.navalbattle.game.entity.MoveableEntity;
 import com.jpii.navalbattle.game.entity.PortEntity;
 
 public class DamageCalculator {
 	
 	public static void doPrimaryDamage(MoveableEntity deal, MoveableEntity take){
 		Player player = NavalGame.getManager().getTurnManager().getTurn().getPlayer();
 		deal.usePrimary();
 		
 		if(calculateDeflect(take)){
 			NavalGame.getManager().getTurnManager().findPlayer(take).addscore(Constants.DEFLECT_SHOT_SCORE);
 		} else if(take.takeDamage(calculatePrimaryDamage(deal, take))){
 			player.addscore(Constants.HIT_SHIP_SCORE);
 			if(take == null || take.isDisposed()){
 				player.addscore(Constants.SINK_SHIP_SCORE);
 			}
 		}
 	}
 	
 	public static void doPrimaryDamage(MoveableEntity deal, PortEntity take){
 		Player player = NavalGame.getManager().getTurnManager().getTurn().getPlayer();
 		deal.usePrimary();
 		
		if(calculateDeflect(take)) {
 			if(take.takeDamage(calculatePrimaryDamage(deal,take))) {
 				player.addscore(Constants.DESTROY_PORT_SCORE);
 				NavalGame.getManager().getTurnManager().removeEntity(take);
 				player.addEntity(take);
 			}
 		}
 	}
 	
 	public static void doSecondaryDamage(MoveableEntity deal, MoveableEntity take){		
 		Player player = NavalGame.getManager().getTurnManager().getTurn().getPlayer();
 		deal.useSecondary();
 		
 		if(calculateDeflect(take)){
 			NavalGame.getManager().getTurnManager().findPlayer(take).addscore(Constants.DEFLECT_SHOT_SCORE);
 		} else if(take.takeDamage(calculatePrimaryDamage(deal, take))) {
 			player.addscore(Constants.HIT_SHIP_SCORE);
 			if(take == null || take.isDisposed()){
 				player.addscore(Constants.SINK_SHIP_SCORE);
 			}
 		}
 	}
 	
 	public static void doSecondaryDamage(MoveableEntity deal, PortEntity take){
 		Player player = NavalGame.getManager().getTurnManager().getTurn().getPlayer();
 		deal.useSecondary();
 		
		if(calculateDeflect(take)) {
 			if(take.takeDamage(calculateSecondaryDamage(deal,take))){
 				player.addscore(Constants.DESTROY_PORT_SCORE);
 				NavalGame.getManager().getTurnManager().removeEntity(take);
 				player.addEntity(take);
 			}
 		}
 	}
 	
 	private static int calculatePrimaryDamage(MoveableEntity deal, MoveableEntity take) {
 		byte attackerClass = deal.getHandle();
 		byte attackedClass = take.getHandle();
 		
 		if(attackerClass == 31) { // Battleship
 			if(attackedClass == 31) { // Battleship
 				return getRandomNumber(150,250);
 			} else if(attackedClass == 21) { // Aircraft carrier
 				return getRandomNumber(150,200);
 			} else if(attackedClass == 11) { // Submarine
 				return getRandomNumber(200,300);
 			}
 		} else if(attackerClass == 21) { // Aircraft carrier
 			if(attackedClass == 31) { // Battleship
 				return getRandomNumber(300,400);
 			} else if(attackedClass == 21) { // Aircraft carrier
 				return getRandomNumber(200,300);
 			} else if(attackedClass == 11) { // Submarine
 				return getRandomNumber(100,150);
 			}
 		} else if(attackerClass == 11) { // Submarine
 			if(attackedClass == 31) { // Battleship
 				return getRandomNumber(100,400);
 			} else if(attackedClass == 21) { // Aircraft carrier
 				return getRandomNumber(200,500);
 			} else if(attackedClass == 11) { // Submarine
 				return getRandomNumber(100,300);
 			}
 		}
 		
 		return 100;
 	}
 	
 	public static int calculateSecondaryDamage(MoveableEntity deal, MoveableEntity take) {
 		byte attackerClass = deal.getHandle();
 		byte attackedClass = take.getHandle();
 		
 		if(attackerClass == 31) { // Battleship
 			if(attackedClass == 31) { // Battleship
 				return getRandomNumber(150,250) * 2;
 			} else if(attackedClass == 21) { // Aircraft carrier
 				return getRandomNumber(150,200) * 2;
 			} else if(attackedClass == 11) { // Submarine
 				return getRandomNumber(200,300) * 2;
 			}
 		} else if(attackerClass == 21) { // Aircraft carrier
 			if(attackedClass == 31) { // Battleship
 				return getRandomNumber(300,400) * 2;
 			} else if(attackedClass == 21) { // Aircraft carrier
 				return getRandomNumber(200,300) * 2;
 			} else if(attackedClass == 11) { // Submarine
 				return getRandomNumber(100,150) * 2;
 			}
 		} else if(attackerClass == 11) { // Submarine
 			if(attackedClass == 31) { // Battleship
 				return getRandomNumber(100,400) * 2;
 			} else if(attackedClass == 21) { // Aircraft carrier
 				return getRandomNumber(200,500) * 2;
 			} else if(attackedClass == 11) { // Submarine
 				return getRandomNumber(100,300) * 2;
 			}
 		}
 		
 		return 200;
 	}
 	
 	private static int calculatePrimaryDamage(MoveableEntity deal, PortEntity take) {
 		byte attackerClass = deal.getHandle();
 		
 		if(attackerClass == 31) { // Battleship
 			return getRandomNumber(300,500);
 		} else if(attackerClass == 21) { // Aircraft carrier
 			return getRandomNumber(300,500);
 		} else if(attackerClass == 11) { // Submarine
 			return getRandomNumber(300,500);
 		}
 		
 		return 100;
 	}
 	
 	private static int calculateSecondaryDamage(MoveableEntity deal, PortEntity take) {
 		byte attackerClass = deal.getHandle();
 		
 		if(attackerClass == 31) { // Battleship
 			return getRandomNumber(300,500) * 2;
 		} else if(attackerClass == 21) { // Aircraft carrier
 			return getRandomNumber(300,500) * 2;
 		} else if(attackerClass == 11) { // Submarine
 			return getRandomNumber(300,500) * 2;
 		}
 		
 		return 200;
 	}
 	
 	private static boolean calculateDeflect(MoveableEntity e) {
 		byte attackedClass = e.getHandle();
 		
 		if(attackedClass == 11) { // Submarine
 			return (Constants.SUBMARINE_DEFLECT_CHANCE >= getRandomNumber(1,100));
 		} else if(attackedClass == 21) { // Aircraft carrier
 			return (Constants.CARRIER_DEFLECT_CHANCE >= getRandomNumber(1,100));
 		} else if(attackedClass == 31) { // Battleship
 			return (Constants.BATTLESHIP_DEFLECT_CHANCE >= getRandomNumber(1,100));
 		}
 		
 		return false;
 	}
 	
 	private static boolean calculateDeflect(PortEntity e) {
 		return (Constants.PORT_DEFLECT_CHANCE >= getRandomNumber(1,100));
 	}
 	
 	private static int getRandomNumber(int min, int max) {
 		return min + (int)(Math.random()*max); 
 	}
 }
