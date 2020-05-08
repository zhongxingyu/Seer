 package squirtlesquad;
 
 import battlecode.common.*;
 
 public class Soldier extends Unit {
 
 	public int[] dirOffsets = {4,-3,3,-2,2,-1,1,0};
 	public int round = -1;
 	public int strat = 0;
 	public RobotController rc = null;
 	public int id;
 	public MapLocation rallyPoint, enemyHq, hq, currentLoc;
 	public boolean engaged = false;
 	public Team otherTeam, myTeam;
 	public int genDir;
 
 	public Soldier(RobotController passedRc, int inStrat) {
 		rc = passedRc;
 //		strat = inStrat;
 //		strat=0;
		strat=id%2;
 		myTeam = rc.getTeam();
 		otherTeam = myTeam.opponent();
 		id = rc.getRobot().getID();
 		hq = rc.senseHQLocation();
 		enemyHq = rc.senseEnemyHQLocation();
 		genDir=id%8;
 		run();
 	}
 
 	@Override
 	public void run() {
 		while (true) {
 			try {
 				if (rc.isActive()) {
 					round = Clock.getRoundNum();
 					currentLoc = rc.getLocation();
					switch(strat){
 					case 0:
 						runExplorer();
 						break;
 					case 1:
 						runDefender();
 						break;
 					case 2:
 						runAttacker();
 						break;
 					}
 				}
 				rc.yield();
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	public void runExplorer() throws GameActionException{
 		MapLocation[] encamps=rc.senseEncampmentSquares(currentLoc,6,Team.NEUTRAL);
 		boolean ready=false;
 
 		if(rc.senseEncampmentSquare(rc.getLocation()))
 		{
 			if(rc.getTeamPower()>rc.senseCaptureCost())
 			{
 				if(rc.getTeamPower()<160)
 					rc.captureEncampment(RobotType.GENERATOR);
 				else
 					rc.captureEncampment(RobotType.SUPPLIER);
 
 			}
 			else
 				rc.suicide();
 		}
 
 		else
 		{
 			Direction dir;
 			boolean looking=true;
 			int counter=0;
 			while(looking)
 			{
 				if(encamps.length>counter)
 				{
 					dir=rc.getLocation().directionTo(encamps[0]);
 					if(rc.canMove(dir))
 					{
 						move(dir);
 						looking=false;
 					}
 					counter++;
 				}
 				else 
 				{
 					looking=false;
 					ready=true;
 				}
 
 			}
 
 
 		}	
 		if(ready)
 		{
 			Direction dir;
 			int genRan=genDir+(int)(Math.random()*5)-2;
 			
 			for(int i=0;i<8;i++)
 			{
 				dir=Direction.values()[(genRan+i+8)%8];
 				if(rc.canMove(dir))
 				{
 					move(dir);
 					break;
 				}
 			}
 			
 		}
 
 	}
 
 	public void runDefender() throws GameActionException{
 		if (rc.isActive()) {
     		if(rc.senseEncampmentSquare(rc.getLocation()) && rc.getTeamPower() > rc.senseCaptureCost()){
     			RobotType[] far = {RobotType.GENERATOR, RobotType.SUPPLIER, RobotType.SUPPLIER, RobotType.ARTILLERY};
     			RobotType[] close = {RobotType.ARTILLERY};
     			RobotType[] mid = {RobotType.ARTILLERY};
     			int hqDist = currentLoc.distanceSquaredTo(rc.senseEnemyHQLocation());
     			int totalDist = rc.senseHQLocation().distanceSquaredTo(rc.senseEnemyHQLocation());
     			if(hqDist < totalDist/3 && rc.getTeamPower() > rc.senseCaptureCost()){
     				rc.captureEncampment(close[(int)(Math.random()*close.length)]);
     			}
     			else if(hqDist < totalDist*2/3 && rc.getTeamPower() > rc.senseCaptureCost()){
     				rc.captureEncampment(mid[(int)(Math.random()*mid.length)]);
     			}
     			else if(rc.getTeamPower() > rc.senseCaptureCost()){
     				rc.captureEncampment(far[(int)(Math.random()*far.length)]);
     			}
     		}
     		else {
     			MapLocation target;
     			Direction dir;
     			target = hq.add(hq.directionTo(enemyHq));
     			dir = currentLoc.directionTo(target);
     			if (rc.senseMine(currentLoc) == null && Math.random()<0.5) {
     				// Lay a mine 
     				//							if(rc.senseMine(rc.getLocation())==null)
     				rc.layMine();
     				return;
     			}
     			else if(rc.senseMineLocations(hq, 2, myTeam).length < 8){
     				for(Direction d : Direction.values()){
     					if(rc.senseMine(hq.add(d))==null){
     						System.out.println("mine found missing in direction " + dir);
     						dir = currentLoc.directionTo(hq.add(d));
     						break;
     					}
     				}
     			}
     		}
     	}
 	}
 
 	public void runAttacker(){
 
 	}
 	public void move(Direction dir) throws GameActionException
 	{
 		MapLocation[] mines=rc.senseNonAlliedMineLocations(rc.getLocation(), 2);
 		MapLocation loc=currentLoc.add(dir);
 		boolean die=false;
 		for(MapLocation m:mines)
 		{	
 			if(m.equals(loc))
 			{
 				die=true;
 			}
 		}
 		if(!(die)) {
 			rc.move(dir);
 			rc.setIndicatorString(0, "Last direction moved: "+dir.toString());
 		}
 		else
 		{
 			rc.defuseMine(loc);
 		}
 	}
 }
