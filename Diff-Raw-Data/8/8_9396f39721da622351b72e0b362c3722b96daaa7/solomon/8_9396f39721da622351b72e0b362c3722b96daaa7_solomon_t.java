 package itc;
 import itc.CTactic;
 import itc.AI;
 import itc.tactic.CTactic_ea0;
 import robocode.*;
 import java.sql.Time;
 
 
 /**
  * Solomon - a robot by (your name here)
  */
 public class solomon extends Robot
 {
 	private byte status;
 	public byte getStatus() {
 		return status;
 	}
 	public void setStatus(byte status) {
 		this.status = status;
 	}
 
 	private int currentTacticIndex;
 	private CTactic tacticLibrary[][];
 	
 	private int healthBeforeTactic;
 	
 	private final int E_AGGRESSIVE = 85;
 	private final int AGGRESSIVE= 65;
 	private final int DEFENSIVE = 45;
 	//private final int E_DEFENSIVE = 25;
 	
 	public Time gameTimer = new Time (System.currentTimeMillis()); 
 	public Time gameTimer2 = new Time (System.currentTimeMillis()); 
 
 	
 	public solomon()
 	{
 		status = 0;
 		currentTacticIndex = 0;
 		tacticLibrary = new CTactic[3][3];
 		
 		tacticLibrary[0][0] = new CTactic_ea0();
 		
 		healthBeforeTactic = 0;
 		
 		
 	}
 		
 	/**
 	 * run: Solomon's default behavior
 	 */
 
 	public void run() {
 						
 		while(true) {
 			
 		
 			
 			status = this.assessHealth();
 			currentTacticIndex = AI.pickTactic(status, currentTacticIndex, tacticLibrary);			
 			healthBeforeTactic = (int) this.getEnergy();
 			
			while(gameTimer.getSeconds()< 90000)
 			{
 				tacticLibrary[status][currentTacticIndex].run_(this);
 			}	
 			
 			AI.gaugeTactic(healthBeforeTactic, (int)this.getEnergy());
 			
 		}
 	}
 
 	/**
 	 * onScannedRobot: What to do when you see another robot
 	 */
 	public void onScannedRobot(solomon s, ScannedRobotEvent e) {
 		
 		tacticLibrary[status][currentTacticIndex].onScannedRobot_(this, e);
 		
 	}
 
 	/**
 	 * onHitByBullet: What to do when you're hit by a bullet
 	 */
 	public void onHitByBullet(HitByBulletEvent e) {
 
 		tacticLibrary[status][currentTacticIndex].onHitByBullet_(this, e);
 		
 	}
 	
 	
 	/**
 	 * assess the health of the solomon and returns a status number
 	 * which will be used as an index in acessing the 2D array of tactics
 	 * @return
 	 */
 	private byte assessHealth()
 	{
 		int health = (int) this.getEnergy();
 		byte status = 0;
 		
 		if(health >= E_AGGRESSIVE)
 		{
 			status = 0;
 		}
 		else if((health >= AGGRESSIVE)&&(health < E_AGGRESSIVE))
 		{
 			status = 1;
 		}
 		else if((health >= DEFENSIVE)&&(health < AGGRESSIVE))
 		{
 			status = 2;
 		}
 		else
 		{
 			status = 3;
 		}
 		
 		return status;
 	}
 	
 	
 }
