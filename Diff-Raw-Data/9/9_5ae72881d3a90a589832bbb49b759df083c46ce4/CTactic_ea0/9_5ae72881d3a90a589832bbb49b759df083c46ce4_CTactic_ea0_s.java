 package itc.tactic;
 import itc.*;
 import java.util.Random;
 import robocode.*;
 
 
 /**
  * (Extrememly?) aggressive tactic.
  * 
  * Rams the opponent, then moves as far away as possible, firing as it retreats.
  * Then rams the enemy again.
  * Implements linear targeting and bullet strength modulation.
  * 
  * Some open-source code used for linear targeting implementation.
  * 
  * @author C00138612
  *
  */
 
 public class CTactic_ea0 extends CTactic 
 {	
 	@Override
 	public void run_(solomon s)
 	{
 		
 			s.turnRight(180);
 	
 		
 	}
 
 	@Override
 	public void onScannedRobot_(solomon s, ScannedRobotEvent e)
 	{
 		
		s.fire(1);
 		
 	
 	}
 	
 	
 	private void turnGunRightRadians(solomon s, double q) {
 		s.turnGunRight((q/180)*Math.PI);
 	}
 
 	private double getHeadingRadians(solomon s) 
 	{
 		return (s.getHeading() * (Math.PI/180));
 	}
 	
 	private double getGunHeadingRadians(solomon s) 
 	{
 		return (s.getGunHeading() * (Math.PI/180));
 	}
 
 	
 	
 	Random r = new Random();
 	
 	@Override
 	public void onHitByBullet_(solomon s, HitByBulletEvent e)
 	{
 		s.turnRight((double)r.nextInt(360));
 		s.ahead((double)r.nextInt(150));
 	}
 }
