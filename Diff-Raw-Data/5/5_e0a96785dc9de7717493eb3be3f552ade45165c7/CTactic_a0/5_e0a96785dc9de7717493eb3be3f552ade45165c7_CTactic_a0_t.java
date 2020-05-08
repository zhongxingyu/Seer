 package itc.tactic;
 
 import robocode.HitByBulletEvent;
 import robocode.ScannedRobotEvent;
 import itc.CTactic;
 import itc.solomon;
 import robocode.util.*;
 import java.awt.geom.*;
 
 public class CTactic_a0 extends CTactic {
 	@Override
 	public void run_(solomon s)
 	{
		s.turnRight(360);
 	}
 
 	@Override
 	public void onScannedRobot_(solomon s, ScannedRobotEvent e)
 	{
 		double bulletPower = Math.min(3.0,s.getEnergy());
 		double myX = s.getX();
 		double myY = s.getY();
 		double absoluteBearing = getHeadingRadians(s) + e.getBearingRadians();
 		double enemyX = s.getX() + e.getDistance() * Math.sin(absoluteBearing);
 		double enemyY = s.getY() + e.getDistance() * Math.cos(absoluteBearing);
 		double enemyHeading = e.getHeadingRadians();
 		double oldEnemyHeading = enemyHeading;
 		double enemyHeadingChange = enemyHeading - oldEnemyHeading;
 		double enemyVelocity = e.getVelocity();
 		double enemyDist = e.getDistance();
 
 		double deltaTime = 0;
 		double battleFieldHeight = s.getBattleFieldHeight(), 
 		       battleFieldWidth = s.getBattleFieldWidth();
 		double predictedX = enemyX, predictedY = enemyY;
 		while((++deltaTime) * (20.0 - 3.0 * bulletPower) < 
 		      Point2D.Double.distance(myX, myY, predictedX, predictedY)){		
 			predictedX += Math.sin(enemyHeading) * enemyVelocity;
 			predictedY += Math.cos(enemyHeading) * enemyVelocity;
 			enemyHeading += enemyHeadingChange;
 			if(	predictedX < 18.0 
 				|| predictedY < 18.0
 				|| predictedX > battleFieldWidth - 18.0
 				|| predictedY > battleFieldHeight - 18.0){
 
 				predictedX = Math.min(Math.max(18.0, predictedX), 
 				    battleFieldWidth - 18.0);	
 				predictedY = Math.min(Math.max(18.0, predictedY), 
 				    battleFieldHeight - 18.0);
 				break;
 			}
 		}
 		double theta = Utils.normalAbsoluteAngle(Math.atan2(
 		    predictedX - s.getX(), predictedY - s.getY()));
 
 		turnRadarRightRadians(s, Utils.normalRelativeAngle(
 		    absoluteBearing - getRadarHeadingRadians(s)));
 		turnGunRightRadians(s, Utils.normalRelativeAngle(
 		    theta - getGunHeadingRadians(s)));
 		
 		
 		fire(s, enemyDist);
 	   
	   s.ahead(100);
 	}
 	
 	@Override
 	public void onHitByBullet_(solomon s, HitByBulletEvent e)
 	{
 		
 	}
 }
