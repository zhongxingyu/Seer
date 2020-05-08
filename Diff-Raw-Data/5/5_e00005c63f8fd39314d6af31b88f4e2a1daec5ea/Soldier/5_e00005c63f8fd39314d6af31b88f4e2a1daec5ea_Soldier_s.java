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
 	public int width, height;
 
 	public Soldier(RobotController passedRc) {
 		rc = passedRc;
 		//		strat = inStrat;
 		//		strat=0;
 
 		myTeam = rc.getTeam();
 		otherTeam = myTeam.opponent();
 		id = rc.getRobot().getID();
 		hq = rc.senseHQLocation();
 		enemyHq = rc.senseEnemyHQLocation();
 		genDir=id%8;
 		strat=id%3;
 		run();
 		width = rc.getMapWidth();
 		height = rc.getMapHeight();
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
 				rc.captureEncampment(pickEncampment());
 			}
 			else
 				strat=1;
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
 			boolean moved=false;
 			for(int i=0;i<5;i++)
 			{
 				dir=Direction.values()[(genRan+i+8)%8];
 				if(rc.canMove(dir))
 				{
 					move(dir);
 					moved=true;
 					break;
 				}
 			}
 			if(!(moved))
 				strat=1;
 		}
 
 	}
 
 	public void runDefender() throws GameActionException{
 		if((id%3)==2 && (round%400)>=200)
 		{
 			strat=2;
 		}
 		else{
 		if (rc.isActive()) {
 			if(RobotPlayer.debug)
 				System.out.println("defending");
 			MapLocation target;
 			Direction dir = Direction.NONE;
     		if(rc.senseEncampmentSquare(currentLoc) && rc.getTeamPower() > rc.senseCaptureCost()){
     				rc.captureEncampment(pickEncampment());
     		}
     		else {
     			target = hq.add(hq.directionTo(enemyHq));
     			dir = currentLoc.directionTo(target);
     			if (rc.senseMine(currentLoc) == null && currentLoc.distanceSquaredTo(hq) < 64 && Math.random()<0.5) {
     				// Lay a mine 
     				if(RobotPlayer.debug)
     					System.out.println("mining");
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
     		
     		target = rc.getLocation().add(dir);
 			if(!dir.equals(Direction.NONE) && !dir.equals(Direction.OMNI) && rc.canMove(dir)){
 				if(rc.senseMine(target) == null || rc.senseMine(target).equals(rc.getTeam())) {
 					rc.move(dir);
 					rc.setIndicatorString(0, "Last direction moved: "+dir.toString());
 				}
 				else if(!rc.senseMine(target).equals(rc.getTeam())){
 					rc.defuseMine(target);
 				}
 			}
 			else{
 				do{
 					dir = Direction.values()[(int)(Math.random()*8)];
 				}
 				while(!rc.canMove(dir) || (dir.equals(Direction.NONE) || dir.equals(Direction.OMNI)));
 
 				target = rc.getLocation().add(dir);
 				if(rc.canMove(dir) && (rc.senseMine(target) == null || rc.senseMine(target).equals(rc.getTeam()))) {
 					rc.move(dir);
 					rc.setIndicatorString(0, "Last direction moved: "+dir.toString());
 				}
 				else if(!rc.senseMine(target).equals(rc.getTeam())){
 					rc.defuseMine(target);
 				}
 			}
 		}
 		}
 	}
 
 	public void runAttacker() throws GameActionException{
 		if(rc.senseEncampmentSquare(rc.getLocation()))
 		{
 			if(rc.getTeamPower()>rc.senseCaptureCost())
 			{
 				rc.captureEncampment(RobotType.ARTILLERY);
 
 			}
 			else
 				strat=1;
 			
 		}	
 		else if((round)%400<200)
 		{
 			MapLocation[] encamps=rc.senseEncampmentSquares(currentLoc,6,Team.NEUTRAL);
 			for(MapLocation m:encamps)
 			{
 				
 			}
 			MapLocation base=hq;
 			Direction dir=currentLoc.directionTo(base);
			if(Math.random()<.5 && !(rc.senseMine(currentLoc).equals(myTeam)))
 			{
 				rc.layMine();
 			}
 			else if(rc.canMove(dir))
 			{
 				move(dir);
 			}
 			else
 			{
 				Direction dirt;
 				for(int i=0;i<8;i++)
 				{
 					dirt=Direction.values()[(dir.ordinal()+i+8)%8];
 					if(rc.canMove(dirt))
 					{
 						move(dirt);
 
 						break;
 					}
 				}
 			}
 		}
 		else
 		{
 //			MapLocation goal=enemyHq;
 //			Direction dir=currentLoc.directionTo(goal);
 //			if(rc.canMove(dir))
 //			{
 //				move(dir);
 //			}
 //			else
 //			{
 //				Direction dirt;
 //				for(int i=0;i<8;i++)
 //				{
 //					dirt=Direction.values()[(dir.ordinal()+i+8)%8];
 //					if(rc.canMove(dirt))
 //					{
 //						move(dirt);
 //
 //						break;
 //					}
 //				}
 //			}
 			strat=1;
 		}
 	}
 	
 	public RobotType pickEncampment() throws GameActionException{
 		int power = (int)rc.getTeamPower();
 		int homeDist = currentLoc.distanceSquaredTo(hq);
 		int targetDist = currentLoc.distanceSquaredTo(enemyHq);
 		int hypotenuse = hq.distanceSquaredTo(enemyHq);
 		if(homeDist + targetDist > hypotenuse){
 			if(targetDist < hypotenuse/9){
 				return RobotType.ARTILLERY;
 			}
 			else if(power < 160){
 				return RobotType.GENERATOR;
 			}
 			else{
 				return RobotType.SUPPLIER;
 			}
 		}
 		else{
 			int nearbyArtillery = 0;
 			for(MapLocation l : rc.senseEncampmentSquares(currentLoc, 64, Team.NEUTRAL)){
				if(rc.canSenseSquare(l) && rc.senseRobotInfo((Robot)rc.senseObjectAtLocation(l)).type.equals(RobotType.ARTILLERY)){
 					nearbyArtillery++;
 				}
 			}
 			int nearbyFriendly = rc.senseNearbyGameObjects(Robot.class, 14, myTeam).length;
 			int nearbyEnemy = rc.senseNearbyGameObjects(Robot.class, 64, otherTeam).length;
 			if(nearbyArtillery > 0){
 				return RobotType.SHIELDS;
 			}
 			else if(homeDist < hypotenuse/9){
 				if(nearbyFriendly > 3 && nearbyEnemy > 3){
 					return RobotType.MEDBAY;
 				}
 				else if(power < 150){
 					return RobotType.GENERATOR;
 				}
 				else
 					return RobotType.ARTILLERY;
 			}
 			else{
 				return RobotType.ARTILLERY;
 			}
 		}
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
