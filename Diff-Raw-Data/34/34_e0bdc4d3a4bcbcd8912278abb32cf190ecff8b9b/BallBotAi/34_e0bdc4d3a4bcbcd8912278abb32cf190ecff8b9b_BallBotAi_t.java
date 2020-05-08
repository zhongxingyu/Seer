 package rsmg.model.ai;
 
 import java.util.List;
 
 import rsmg.model.object.unit.BallBot;
 import rsmg.model.object.unit.Enemy;
 import rsmg.model.object.unit.MiniBallBot;
 import rsmg.util.Vector2d;
 
 public class BallBotAi implements Ai {
 	private BallBot enemy;
 	private List<Ai> enemyAiList;
 	private static int TRAVELSPEED = 20;
 	private static double ANGLES_PER_SECOND = 1;
 	private static int AGGRORANGE = 300;
 	private double angle;
 	private double cooldown;
 	
 	/**
 	 * Create a BallBot AI.
 	 * @param enemy Reference to the enemy it should control.
 	 * @param enemyAiList The list of enemies that are harmful to the character.
 	 */
 	public BallBotAi(BallBot enemy, List<Ai> enemyAiList) {
 		this.enemy = enemy;
 		this.enemyAiList = enemyAiList;
 		enemy.setVelocity(new Vector2d(TRAVELSPEED, TRAVELSPEED));
 		angle = (Math.PI/4);
 	}
 	
 	@Override
 	public void update(double delta, double playerX, double playerY) {
 		angle += ANGLES_PER_SECOND*delta;
 		
 		enemy.setVelocityX(Math.cos(angle)*TRAVELSPEED);
 		enemy.setVelocityY(Math.sin(angle)*TRAVELSPEED);
 		
 		if (shouldShoot(delta) && AggroRange.charInAggroRange(playerX, playerY, enemy, AGGRORANGE)) {
 			spawnEnemy();
 		}
 	}
 
 	private boolean shouldShoot(double delta) {
 		cooldown += delta;
 		// Every 0.8 second it gets the chance to shoot.
 		if (cooldown > 0.8) {
 			cooldown = 0;
			return true;
 		}
 		return false;
 	}
 	
 	private void spawnEnemy() {
 		enemyAiList.add(new RocketBotAi(new MiniBallBot(enemy.getX(), enemy.getY())));
 	}
 
 	@Override
 	public Enemy getEnemy() {
 		return enemy;
 	}
 	
 }
