 package team161;
 
 import battlecode.common.*;
 
 public class SoldierCode {
 	private static MapLocation myLoc;
 	private static MapLocation target;
 	private static Direction prev;
 	private static Message msg;
 	private static Bug b;
 	private static MapLocation enemyHQ;
 	private static MapLocation myHQ;
 	private static Team myTeam;
 	private static Team enemyTeam;
 	private static int generators = 0;
     private static int suppliers = 0;
     private static MapLocation spawnSpot = null;
     private static int shield = 20;
 	private static boolean getShield = false;
 	private static RobotController rc;
 	private static boolean canCapture;
 
 
     public static void soldierRun(RobotController rc_) throws GameActionException {
     	rc = rc_;
 		enemyHQ = rc.senseEnemyHQLocation();
 		myHQ = rc.senseHQLocation();
 		myTeam = rc.getTeam();
 		enemyTeam = myTeam.opponent();
     	msg = new Message(rc);
     	b = new Bug(enemyHQ, rc);
     	
     	while (true)
     	{
 			myLoc = rc.getLocation();
 			target = enemyHQ;
     		
 			rc.setIndicatorString(0, "");
 			rc.setIndicatorString(1, "");
     		
     		//DATA
 	    	Robot[] enemy = rc.senseNearbyGameObjects(rc.getRobot().getClass(), myLoc, RobotType.SOLDIER.sensorRadiusSquared, enemyTeam);
 	    	//Message HQ with location of FRIENDLY ROBOT who sees enemy robots.
 	    	if (enemy.length > 0)
 	    		msg.send(Action.ENEMY, myLoc);
 //	    	Robot[] friends = rc.senseNearbyGameObjects(rc.getRobot().getClass(), myLoc, RobotType.SOLDIER.sensorRadiusSquared, myTeam);
 
 	    	if (rc.isActive())
 	    	{
 	    		int msgCount = -1;
 	    		boolean blankMsg = false;
 	    		boolean distress = false;
 	    		boolean attack = false;
 	    		boolean rush = false;
 	    		boolean canCapture = true;
 				msg.reset();
 	    		
 	    		while (true) {
     				msg.receive(msgCount);
 	    			if (msg.action == null)
 	    			{
 	    				if (blankMsg == true) break;
 	    				blankMsg = true;
 	    			}
 	    			else 
 	    			{
 	    				if (msg.action == Action.DISTRESS) distress = true;
 	    				if (msg.action == Action.RUSH) {
 		    				rush = true;
 		    				break;
 		    			}
 		    			else if (msg.action == Action.GEN_SUP)
 		    			{
 		    				generators = msg.location.x;
 		    				suppliers = msg.location.y;
 		    			}
 		    			else if (msg.action == Action.RALLY_AT) target = msg.location;
 		    			else if (msg.action == Action.ATTACK)
 		    			{
 		    				target = enemyHQ; //prolly target should be msg.location
 		    				attack = true;
 		    			}
 		    			else if (msg.action == Action.CAPTURING) canCapture = false;
 		    			else if (msg.action == Action.DONT_CAP)
 		    			{
 		    				spawnSpot = msg.location;
 		    				rc.setIndicatorString(0, "DUDE I HEARD YA");
 		    			}
 	    			}
 	    			msgCount--;
     			}
     			if (rush && !(distress)) rush();
     			else {
 		    		if (distress == true)
 		    		{
 		    			attack = true;
 		    			if (myLoc.distanceSquaredTo(enemyHQ) < 25)
 		    				target = enemyHQ;
 		    			else
 		    				target = myHQ;
 		    		}
 		    		if (attack == true) {
 		    			//if (!attackMode())
 		    				travelMode();
 		    		} else if (!attackMode())
 		    			if (!mineMode())
 		    				if (!colonizeMode())
 		    					travelMode();
     			}
 	    		rc.yield();
 	    	}
     	}
     }
     
     
     /*---------------RUSH CODE------------------*/
     
     private static void rush() throws GameActionException
     {
     	//while (true) {
 			msg.reset();
 			if (rc.getLocation().isAdjacentTo(enemyHQ)) msg.send(Action.KILLING, enemyHQ);
 			msg.receive(-1);
 			if (msg.action == Action.CAP_SHIELD ){//&& b.target.equals(rc.senseEnemyHQLocation())) {
 				MapLocation[] encamps = rc.senseEncampmentSquares(rc.getLocation(), 100, Team.NEUTRAL);
 				for (MapLocation encamp: encamps)
 					if (encamp.distanceSquaredTo(myHQ) < 400 && rc.senseNearbyGameObjects(Robot.class, encamp, 25, rc.getTeam().opponent()).length == 0) {
 						b.target = encamp;
 						getShield = true;
 						break;
 					}
 			} else if (msg.action == Action.RALLY_AT && shield > 0) {
 				System.out.println("rally at " + msg.location);
 				if (rc.getLocation().distanceSquaredTo(msg.location) <= 2) {
 					shield --;
 				}
 				b.target = msg.location;
 				getShield = false;
 			} else {
 				b.target = rc.senseEnemyHQLocation();
 				getShield = false;
 			}
 			if (rc.isActive()) {
 				if (rc.getLocation().equals(b.target) && getShield) {
 					msg.reset();
 					rc.captureEncampment(RobotType.SHIELDS);
 					msg.send(Action.CAP_SHIELD, rc.getLocation());
 					System.out.println("GOT HERE SOLDIERCODE");
 				}
 				if (shield <= 0) {b.shieldGo(); System.out.println("shit goes down" + shield);}
 				else b.go();
 				rc.setIndicatorString(0, "target " + b.target + " getShield " + getShield);
 			}
 		//}
     }
     /*-----------------RUSH END------------------*/
     
     /*-----------------ATTACK CODE------------------*/
     private static boolean attackMode() throws GameActionException {
     	Robot[] enemies = rc.senseNearbyGameObjects(Robot.class, 25, rc.getTeam().opponent());
     	for (Robot enemy: enemies) {
     		RobotInfo info = rc.senseRobotInfo(enemy);
     		if(info.roundsUntilMovementIdle > 1) {
     			b.target = info.location; //perhaps want to keep track of the same robot.
         		b.go();
     			return true;
     		}
     	}
     	return false;
     }
     
     /*-----------------ATTACK END-------------------*/
     
     
 	/*--------------TRAVEL CODE--------------------*/
 	
 	private static void travelMode() throws GameActionException
 	{
 		if (myLoc.equals(target)) return;
 		b.target = target;
 		b.go();
 		prev = b.prev;
		Robot[] enemies = rc.senseNearbyGameObjects(Robot.class, GameConstants.sensorRadiusSquared, enemyTeam);
		for (Robot enemy : enemies) if (rc.senseRobotInfo(enemy).RobotType == RobotType.ARTILLERY) {
 		    msg.reset();
		    msg.send(Action.SEEART, myLoc);
 		}
 		/*Direction dir = myLoc.directionTo(target);
 		if (!rc.canMove(dir)) dir = randomDir(10);
 		Team t = rc.senseMine(myLoc.add(dir));
 		if (t == Team.NEUTRAL || t == enemyTeam) rc.defuseMine(myLoc.add(dir));
 		else if (dir != Direction.NONE) {
 			rc.move(dir);
 			prev = dir;
 			//rc.setIndicatorString(1, "target " + target + " moved in direction " + dir);
 		} else rc.setIndicatorString(1, "target " + target + " something is wrong");*/
 	}
 	
     /*private static Direction randomDir(int depth) {
     	if (depth == 0) return Direction.NONE;
         Direction dir = Direction.values()[(int)(Math.random()*8)];
         if(rc.canMove(dir)) return dir;
         else return randomDir(depth-1);
     }*/
 
 	/*--------------TRAVEL CODE END--------------------*/
     
 
     
 	/*--------------COLONIZE CODE--------------------*/
 
     private static boolean colonizeMode() throws GameActionException {
 		//rc.setIndicatorString(1, "colonize mode");
     	if (rc.getRobot().getID() % 3 != 0)
     		return false;
     	if (!myLoc.equals(spawnSpot) && rc.senseEncampmentSquare(myLoc)) { //if encamp is already ours, can't move there.
     		if (prev != null) {
     			for (int i = 0; i <= 1; i++) {
     				Direction dir = Direction.values()[(prev.ordinal()+i)%8];
                                 if (rc.senseEncampmentSquare(myLoc.add(dir))) {
                                     smartDefuseMine(dir);
     					if (rc.canMove(dir)) {
     						rc.move(dir);
     						return true;
     					}
     				}
     				dir = Direction.values()[(prev.ordinal()-i+8)%8];
     				if (rc.senseEncampmentSquare(myLoc.add(dir))) {
                                     smartDefuseMine(dir);
     					if (rc.canMove(dir)) {
     						rc.move(dir);
     						return true;
     					}
     				}
     			}
     		}
     		if (rc.getLocation().equals(spawnSpot)) return false;
     		//if (!safe(100)) return false;
         	capture();
         	return true;
     	}
     	MapLocation[] encamps = rc.senseEncampmentSquares(myLoc, 80, Team.NEUTRAL);
     	if (encamps.length == 0) return false;
     	if (encamps.length != 0) rc.setIndicatorString(1, "# neutral encampments > 0");
     	MapLocation encampTarget = encamps[(int)Math.random()*encamps.length];
     	if (!encampTarget.equals(spawnSpot)) target = encampTarget;
     	return false;
     }
     private static boolean surrounded(MapLocation loc) {
     	int sides = 0;
     	for (int i = 0; i < 8; i++)
     		if (rc.senseEncampmentSquare(loc.add(Direction.values()[i]))) sides++;
     	if (sides > 7) return true;
     	return false;
     }
     private static boolean capture() throws GameActionException{
     	if (rc.senseCaptureCost() > rc.getTeamPower()) return false;
     	myLoc = rc.getLocation();
     	if (rc.senseCaptureCost() * 2 > rc.getTeamPower()) rc.captureEncampment(RobotType.GENERATOR);
     	else if (myLoc.directionTo(enemyHQ) == myLoc.directionTo(myHQ).opposite() && !surrounded(myLoc)) rc.captureEncampment(RobotType.ARTILLERY);
     	else if (myLoc.distanceSquaredTo(RobotPlayer.myHQ) < 64 && (myLoc.directionTo(enemyHQ) == myLoc.directionTo(myHQ).opposite()
                   || myLoc.directionTo(enemyHQ) == myLoc.directionTo(myHQ).opposite().rotateLeft()
                   || myLoc.directionTo(enemyHQ) == myLoc.directionTo(myHQ).opposite().rotateRight()
                   || myLoc.directionTo(myHQ) == myLoc.directionTo(enemyHQ).opposite().rotateLeft()
                   || myLoc.directionTo(myHQ) == myLoc.directionTo(enemyHQ).opposite().rotateRight()))
     	    rc.captureEncampment(RobotType.ARTILLERY);
         else if (myLoc.distanceSquaredTo(enemyHQ) < 64) rc.captureEncampment(RobotType.ARTILLERY);
         //else if (RobotPlayer.myHQ.directionTo(myLoc) == RobotPlayer.myHQ.directionTo(RobotPlayer.enemyHQ)
         //	  && RobotPlayer.enemyHQ.directionTo(myLoc) == RobotPlayer.enemyHQ.directionTo(RobotPlayer.myHQ))
         //	rc.captureEncampment(RobotType.ARTILLERY);
         else if (generators >= 3 * suppliers) rc.captureEncampment(RobotType.SUPPLIER);
         else rc.captureEncampment(RobotType.GENERATOR);
     	msg.send(Action.CAPTURING, myLoc);
         return true;
     }
     
 	/*--------------COLONIZE CODE END--------------------*/
 
 	/*--------------MINE CODE--------------------*/
     
     private static boolean mineMode() throws GameActionException {
 		//rc.setIndicatorString(0, "mine mode");
 
     	if (rc.senseMine(myLoc) != null) return false;
     	if (rc.hasUpgrade(Upgrade.PICKAXE))
     	{
     		if (rc.getLocation().distanceSquaredTo(rc.senseHQLocation()) < 25 && pickaxeMineField() && safe(16))
     		{
     			rc.layMine();
     			return true;
     		}
     		else if (pickaxeMineField()  && (myLoc.directionTo(enemyHQ) == myLoc.directionTo(myHQ).opposite()
                                                || myLoc.directionTo(enemyHQ) == myLoc.directionTo(myHQ).opposite().rotateLeft()
                                                || myLoc.directionTo(enemyHQ) == myLoc.directionTo(myHQ).opposite().rotateRight()
                                                || myLoc.directionTo(myHQ) == myLoc.directionTo(enemyHQ).opposite().rotateLeft()
                                                || myLoc.directionTo(myHQ) == myLoc.directionTo(enemyHQ).opposite().rotateRight()))
     		{
     			rc.layMine();
     			return true;
     		}    	
     	}
     	else if (rc.getLocation().distanceSquaredTo(rc.senseHQLocation()) < 25 && /*sparseMineField()*/ safe(36) && !rc.senseEncampmentSquare(myLoc))
     	{
     		rc.layMine();
     		return true;
     	}
     	return false;
     }
     private static boolean safe(int dist) {
     	if (rc.senseNearbyGameObjects(Robot.class, dist, rc.getTeam().opponent()).length > 0) return false;
     	return true;
     }
     private static boolean sparseMineField() {
     	if (rc.getLocation().x % 2 == 0 && rc.getLocation().y % 2 == 0) return true;
     	else return false;
     }
     private static boolean pickaxeMineField() {
     	int a = rc.getLocation().x % 5;
     	int b = rc.getLocation().y % 5;
     	if (b == a*2 % 5) return true;
     	else return false;
     }
     private static boolean pickaxeSparseMineField() {
     	int a = rc.getLocation().x % 4;
     	int b = rc.getLocation().y % 4;
     	if((a+b) % 4 == 0 && a == b) return true;
     	return false;
     }
     private static void smartDefuseMine(Direction dir) throws GameActionException{
         Team t = rc.senseMine(myLoc.add(dir));
         if (t == Team.NEUTRAL || t == enemyTeam) {
             rc.defuseMine(myLoc.add(dir));
             rc.yield();rc.yield();rc.yield();rc.yield();rc.yield();rc.yield();rc.yield();rc.yield();rc.yield();rc.yield(); rc.yield();rc.yield();
         }
     }
     
 	/*--------------MINE CODE END--------------------*/
 }
