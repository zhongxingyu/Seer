 package itc;
 
 import java.util.*;
 import robocode.HitByBulletEvent;
 import robocode.ScannedRobotEvent;
 
 
 public class CTactic {
 
 	
	//This is the sucess threshold that a tactic most to be labelled as effective 
 	protected final byte GAUGING_THRESHOLD = 60; 
 	//protected List gaugingList = new ArrayList();
 	protected List<Byte> gaugingList  = new ArrayList<Byte>();
 
	private Random r = new Random();
 	
 	public void run_(solomon s)
 	{
 		
 	}
 	
 
 	public void onScannedRobot_(solomon s, ScannedRobotEvent e)
 	{
 		
 	}
 	
 	
 	public void onHitByBullet_(solomon s, HitByBulletEvent e)
 	{
 		
 	}
 	
 	
 	/**
 	 * Uses the distance of the enemy robot to figure out how
 	 * much energy to expend when firing. The bias varies, depending on the
 	 * robot's status. If it's very aggressive, it'll be more likely
 	 * to fire with full strength. If very defensive, it'll almost never
 	 * do that, and so on, so forth.
 	 * 
 	 * @param s
 	 * @param enemyDist
 	 */
 	protected void fire(solomon s, double enemyDist) {
 		// TODO: This can be simplified (probably very easily so it doesn't require a case statement);
 	
 		// Case statement to pick bias.
 		int bias = 0;
 		switch (s.getStatus()) {
 			case 0 :
 				bias = 400;
 				break;
 			case 1 :
 				bias = 300;
 				break;
 			case 2 :
 				bias = 200;
 				break;
 			case 3 :
 				bias = 100;
 				break;
 			default :
 				break;
 		}
 		
 		double firePower = 0;
 		
 		// This if statement is so that if the enemy is more than 500 units away, it won't even bother with the bias
 		// or if it's closer than 50 units, it'll go straight to full power.
 		if (enemyDist > 500)
 		{
 			firePower = 0.1;
 		}
 		else if (enemyDist < 50)
 		{
 			firePower = 5.0;
 		}
 		else
 		{
 			firePower = bias/enemyDist;
 		}
 		
 		s.fire(firePower);
 	}
 	
 	
 	/**
 	 * Calculates the efficiency of the tactic using the gaugingList
 	 * and returns true or false
 	 * @return
 	 */
 	public boolean isGoodTactic(int status)
 	{
 		boolean result = false;
 		double sumOfGauging = 0;		
 		double sumOfElements = (double)gaugingList.size();
 		
 		for(int i = 0; i < sumOfElements; i++)
 		{
 		  sumOfGauging += gaugingList.get(i);
 		}
 		
 		if(sumOfElements != 0)
 		{
 		   sumOfGauging = (sumOfGauging/sumOfElements)*100;
 		}
 		else
 		{
 			sumOfGauging = 100;
 		}
 		
 		if(sumOfGauging > GAUGING_THRESHOLD)
 		{
 			result = true;
 		}
 		
 		System.out.println("sumOfGauging = " + sumOfGauging);
 		
 		return result;
 	}
 	
 	// Returns a random number.
 	protected double getRandom()
 	{
 		return r.nextDouble();
 	}
 	// Returns a number, between zero and input.
 	protected double getRandom(int n)
 	{
 		return (double)(r.nextInt(n));
 	}
 	
 	// What follows are radian translations of calculations that return degrees. Done for compatability with Math.*;
 	protected void turnGunRightRadians(solomon s, double q) {
 		s.turnGunRight((q/180)*Math.PI);
 	}
 
 	protected double getHeadingRadians(solomon s) 
 	{
 		return (s.getHeading() * (Math.PI/180));
 	}
 	
 	protected double getGunHeadingRadians(solomon s) 
 	{
 		return (s.getGunHeading() * (Math.PI/180));
 	}
 }
