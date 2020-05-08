 package team035.brains;
 
 import java.util.Random;
 
 import team035.messages.ClaimNodeMessage;
 import team035.messages.LowFluxMessage;
 import team035.messages.MessageAddress;
 import team035.messages.MessageAddress.AddressType;
 import team035.messages.MessageWrapper;
 import team035.messages.MoveOrderMessage;
 import team035.modules.NavController;
 import team035.modules.RadioListener;
 import team035.modules.StateCache;
 import team035.robots.BaseRobot;
 import battlecode.common.Clock;
 import battlecode.common.Direction;
 import battlecode.common.GameActionException;
 import battlecode.common.GameConstants;
 import battlecode.common.GameObject;
 import battlecode.common.MapLocation;
 import battlecode.common.PowerNode;
 import battlecode.common.RobotController;
 import battlecode.common.RobotInfo;
 import battlecode.common.RobotLevel;
 import battlecode.common.RobotType;
 import battlecode.common.TerrainTile;
 
 public class ArchonBrain extends RobotBrain implements RadioListener {
 	protected final static double NODE_DETECTION_RADIUS_SQ = 16;
 	protected final static double INITIAL_ROBOT_FLUX = 30;
 	protected final static int BUILDING_COOLDOWN_VALUE = 8;
 	protected final static int SPREADING_COOLDOWN_VALUE = 3;
 	protected final static int REFUEL_FLUX = 20;
 	protected final static int REFUEL_THRESHOLD = 10;
 	protected final static int MOVE_FAIL_COUNTER = 100;
	public final static int ATTACK_TIMING = 1000;
 	protected ArchonState[] stateStack;
 	protected int stateStackTop;
 
 	protected enum ArchonState {
 		LOITERING, MOVING, BUILDING, SPREADING, REFUELING, FLEEING, EVADING, 
 		BUILDUP
 	}
 
 	
 	protected ArchonState state;
 	protected MapLocation nodeBuildLocation;
 	protected PowerNode targetPowerNode;
 	
 	protected boolean fluxTransferQueued = false;
 	protected MapLocation fluxTransferLoc = null;
 	protected RobotLevel fluxTransferLevel = null;
 	protected double fluxTransferAmount = 0.0;
 	protected int spreadingCooldown;
 	protected int buildingCooldown;
 	protected int moveFailCooldown;
 	
 	public ArchonBrain(BaseRobot r) {
 		super(r);
 		
 		r.getRadio().addListener(this, ClaimNodeMessage.type);
 		r.getRadio().addListener(this, LowFluxMessage.type);
 		
 		r.getRadar().setEnemyTargetBroadcast(true);
 		this.initCooldowns();
 		this.initStateStack();
 		this.pushState(ArchonState.BUILDUP);
 	}
 
 	
 
 	@Override
 	public void think() {
 		this.updateCooldowns();
 		this.scanForEnemies();
 		
 		this.displayState();
 		switch(this.getState()) {
 		case BUILDUP:
 			buildup();
 			break;
 		case LOITERING:
 			loiter();
 			break;
 		case BUILDING:
 			build();
 			break;
 		case MOVING:
 			move();
 			break;
 		case SPREADING:
 			spread();
 			break;
 		case REFUELING:
 			refuel();
 			break;
 		case EVADING:
 			evade();
 			break;
 		}
 	}
 	
 	protected void displayState() {
 		String stateString = "NONE";
 		switch(this.getState()) {
 		case BUILDING:
 			stateString = "BUILDING";
 			break;
 		case EVADING:
 			stateString = "EVADING";
 			break;
 		case FLEEING:
 			stateString = "FLEEING";
 			break;
 		case LOITERING:
 			stateString = "LOITERING";
 			break;
 		case MOVING:
 			stateString = "MOVING";
 			break;
 		case REFUELING:
 			stateString = "REFUELING";
 			break;
 		case SPREADING:
 			stateString = "SPREADING";
 			break;
 		case BUILDUP:
 			stateString = "BUILDUP";
 			break;
 		}
 		
 		this.r.getRc().setIndicatorString(0, stateString);
 	}
 	
 	protected void buildup() {
     if(this.isNearArchons() && spreadingCooldown == 0) {
     	System.out.println("Archon loitering->spreading");
     	this.pushState(ArchonState.SPREADING);
     	spread();
     	return;
     }
     	
     if(Clock.getRoundNum() > ATTACK_TIMING) {
     	System.out.println("Triggering attack!");
     	this.popState();
     	return;
     }
     
     RobotController rc = this.r.getRc();
     if(canSpawn()) {
     	spawnRobotIfPossible();	
     } else if(!rc.isMovementActive()) {
     	MapLocation myLoc = rc.getLocation();
     	for(Direction heading: Direction.values()) {
     		if(heading != Direction.NONE && heading != Direction.OMNI) {
     			if(canSpawn(heading)) {
     				try {
       				rc.setDirection(heading);
       				return;
     				} catch (GameActionException e) {
     					e.printStackTrace();
     					return;
     				}
     			}
     		}
     	}
     	// we're crowded in, so send a move command
     	MapLocation closeNode = getRandomCapturableNode();
       this.r.getRadio().addMessageToTransmitQueue(new MessageAddress(MessageAddress.AddressType.BROADCAST), new MoveOrderMessage(closeNode));
     }
 	}
 	
 	protected void evade() {
 		if(spawnRobotIfPossible()) {
 			return;
 		}
 		
 		RobotController rc = this.r.getRc();
 		RobotInfo[] enemies = r.getCache().getEnemyRobots();
 		MapLocation centroid = new MapLocation(0,0);
     for(RobotInfo enemy: enemies) {
     	MapLocation enemyLoc = enemy.location; 
     	centroid = centroid.add(enemyLoc.x, enemyLoc.y);
     }
     centroid = new MapLocation(centroid.x / enemies.length, centroid.y / enemies.length);
     if(!rc.isMovementActive()) {
     	try {
 	    	Direction enemyHeading = rc.getLocation().directionTo(centroid);
 	    	if(enemyHeading != Direction.NONE && enemyHeading != Direction.OMNI) {
 	    		if(rc.getDirection() != enemyHeading) {
 	    			rc.setDirection(enemyHeading);
 	    			return;
 	    		}
 	    		if(rc.canMove(enemyHeading.opposite())) {
 	    			rc.moveBackward();
 	    			return;
 	    		}
 	    		
 	    	}
     	} catch (GameActionException e) {
     		e.printStackTrace();    		
     	}
     }
 	}
 
 
 
 	protected void scanForEnemies() {
 		if(r.getCache().numEnemyAttackRobotsInRange > 0) {
 			if(this.getState() != ArchonState.EVADING &&
 				this.getState() != ArchonState.REFUELING) {
 				this.pushState(ArchonState.EVADING);
 			}
 		}
 		else {
 			if(this.getState() == ArchonState.EVADING) {
 				this.popState();
 			}
 		}
 	}
 
 
 
 	protected void refuel() {
 		GameObject go;
 		try {
 			//go = this.r.getRc().senseObjectAtLocation(this.r.getRc().getLocation().add(this.r.getRc().getDirection()), RobotLevel.ON_GROUND);
 			go = this.r.getRc().senseObjectAtLocation(fluxTransferLoc, fluxTransferLevel);
 			if(go!=null) {
 				System.out.println("Refueled a robot!");
 				this.r.getRc().transferFlux(fluxTransferLoc, fluxTransferLevel, fluxTransferAmount);
 			} else {
 				System.out.println("Refuel failed!");
 			}
 		} catch (GameActionException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		this.popState();
 	}
 	
 	
 	protected void loiter() {
 		MapLocation nearestNode = this.getNearestCapturableNode();
 		if(nearestNode.isAdjacentTo(this.r.getRc().getLocation())
 				&& this.buildingCooldown == 0) {
 			this.pushState(ArchonState.BUILDING);
 			build();
 			return;
 		}
 		
 		if(refuelRobotsIfPossible() ) {
 			return;
 		}
 		
     if(this.isNearArchons() && spreadingCooldown == 0) {
     	System.out.println("Archon loitering->spreading");
     	this.pushState(ArchonState.SPREADING);
     	spread();
     	return;
     }
     
     // If we can sense a place to build a tower, grab it
     MapLocation nearNodeLoc = getRandomCapturableNode();
 
     this.r.getNav().setTarget(nearNodeLoc, true);
     this.r.getRadio().addMessageToTransmitQueue(new MessageAddress(MessageAddress.AddressType.BROADCAST), new MoveOrderMessage(r.getNav().getTarget()));
     
     this.pushState(ArchonState.MOVING);
     System.out.println("Archon loitering->moving");
     this.move();
 	}
 	
 	protected void spread() {
     StateCache cache = r.getCache();
     RobotController rc = r.getRc();
     MapLocation myLoc = rc.getLocation();		
     boolean spreadBlocked = false;
     
     MapLocation[] archons = cache.getFriendlyArchonLocs();
     MapLocation centroid = new MapLocation(0,0);
     int nearbyArchons = 0;
     for(MapLocation archonLoc: archons) {
     	if(archonLoc == myLoc) {
     		continue;
     	}
     	if(myLoc.distanceSquaredTo(archonLoc) <= GameConstants.PRODUCTION_PENALTY_R2) {
     		nearbyArchons++;
     		centroid = centroid.add(archonLoc.x, archonLoc.y);
     	}
     }
     if(nearbyArchons > 0) {
     	// check we can move
 	    if(!rc.isMovementActive()) {
 		    centroid = new MapLocation((int)((float)centroid.x / nearbyArchons), (int)((float)centroid.y / nearbyArchons));
 		    Direction towardsCentroid = myLoc.directionTo(centroid);
 		    Direction awayFromCentroid = towardsCentroid.opposite();
 		    if(towardsCentroid == Direction.OMNI || towardsCentroid == Direction.NONE) {
 		    	spreadBlocked = true;
 		    } else {
 			    try {
 				    if(rc.getDirection() != awayFromCentroid &&
 				    	 rc.getDirection() != towardsCentroid) {
 				    	rc.setDirection(awayFromCentroid);
 				    	return;
 				    }
 				    
 				    // if we're facing a good direction do move!
 				    if(rc.getDirection() == awayFromCentroid) {
 				    	if(rc.canMove(awayFromCentroid)) {
 				    		rc.moveForward();
 				    		return;
 				    	} else {
 				    		spreadBlocked = true;
 				    	}
 				    } 
 				    
 				    if(rc.getDirection() == towardsCentroid) {
 				    	if(rc.canMove(awayFromCentroid)) {
 					    	rc.moveBackward();
 					    	return;
 				    	} else {
 				    		spreadBlocked = true;
 				    	}
 				    }
 			    } catch (GameActionException e) {
 			    	e.printStackTrace();
 			    	spreadBlocked = true;
 			    }
 		    }
 	    }
 
     } else {
     	// there were no nearby archons any more!
     	System.out.println("Archon done spreading!");
     	this.popState();
     }
     // something went wrong, so fail and don't try again for 3 turns.	
     if(spreadBlocked) {
     	System.out.println("Spread blocked! Starting cooldown.");
     	this.spreadingCooldown = SPREADING_COOLDOWN_VALUE;
     	this.popState();
     }
 	}
 	
 
 
 	protected void build() {
 		NavController nav = this.r.getNav();
 		RobotController rc = this.r.getRc();
 		try {
 			MapLocation nearestNode = this.getNearestCapturableNode();
 			if(nearestNode.isAdjacentTo(rc.getLocation())) {
 				r.getRadio().addMessageToTransmitQueue(new MessageAddress(AddressType.BROADCAST), new ClaimNodeMessage(this.nodeBuildLocation));
 				
 				Direction nodeDirection = rc.getLocation().directionTo(nearestNode);
 				if(rc.getDirection() != nodeDirection) {
 					if(!rc.isMovementActive()) {
 						rc.setDirection(nodeDirection);
 						return;
 					}			
 				}
 				GameObject targetContent = rc.senseObjectAtLocation(nearestNode, RobotLevel.ON_GROUND);
 				if(targetContent == null) {
 					if(rc.getFlux() >= RobotType.TOWER.spawnCost) {
 						if(!rc.isMovementActive()) {
 							rc.spawn(RobotType.TOWER);					
 						}
 					}
 				} else {
 					// this either means we built it or someone messed with us
 					this.popState();
 					System.out.println("Archon building->loitering");
 					return;
 				}
 			} else {
 				this.popState();
 			}
 		} catch (GameActionException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	protected void move() {
 		if(this.spawnRobotIfPossible()) {
 			return;
 		}
 		
 		NavController nav = this.r.getNav();
 		nav.doMove();
 		if(nav.isAtTarget() || moveFailCooldown <= 0) {
 			this.popState();
 			if(moveFailCooldown <= 0) {
 				System.out.println("Move Failed!");
 			}
 		}
 	}	
 	
 	protected void queueFluxTransfer(MapLocation loc, RobotLevel level, double amount) {
 		this.fluxTransferLoc = loc;
 		this.fluxTransferLevel = level;
 		this.fluxTransferAmount = amount;
 
 		// push refueling onto the stack
 		System.out.println("Queuing refuel");
 		this.pushState(ArchonState.REFUELING);
 	}
 	
 	protected boolean spawnRobotIfPossible() {
 
 		if(!r.getRc().isMovementActive() && r.getRc().getFlux() > RobotType.SOLDIER.spawnCost + INITIAL_ROBOT_FLUX) {
 			try {			
 				if(r.getRc().canMove(r.getRc().getDirection()) &&
 						r.getRc().senseObjectAtLocation(this.r.getRc().getLocation().add(this.r.getRc().getDirection()), 
 								RobotLevel.POWER_NODE) == null) {
 
 					r.getRc().spawn(RobotType.SOLDIER);
 					this.queueFluxTransfer(this.r.getRc().getLocation().add(this.r.getRc().getDirection()), RobotLevel.ON_GROUND, 0.9*INITIAL_ROBOT_FLUX);
 					System.out.println("Spawned a robot.");
 					return true;
 				}
 			} catch (GameActionException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 
 		}
 		return false;
 	}
 	
 	// Helper stuffs
 	protected PowerNode getNearestAlliedNode() {
 		StateCache cache = r.getCache();		
 		RobotController rc = r.getRc();
 		PowerNode[] alliedNodes = cache.senseAlliedPowerNodes();
 		PowerNode nearest = null;
 		MapLocation myLoc = rc.getLocation();
 		double bestDistance = Double.MAX_VALUE;
 		for(PowerNode node : alliedNodes) {
 			double distance = node.getLocation().distanceSquaredTo(myLoc);
 			if(distance < bestDistance) {
 				nearest = node;
 				bestDistance = distance;
 			}
 		}	
 		return nearest;
 	}
 
 	@Override
 	public void handleMessage(MessageWrapper msg) {
 		if(msg.msg.getType()==ClaimNodeMessage.type) {
 			ClaimNodeMessage claimMessage = (ClaimNodeMessage) msg.msg;
 			MapLocation claimLocation = claimMessage.loc;
 			if(this.getState() == ArchonState.LOITERING || 
 				 this.getState() == ArchonState.MOVING) {
 				this.buildingCooldown = BUILDING_COOLDOWN_VALUE;
 			}
 		}
 		
 		if(msg.msg.getType()==LowFluxMessage.type) {
 			LowFluxMessage lowFluxMessage = (LowFluxMessage) msg.msg;
 			System.out.println("Received a refueling request!");
 			
 			if(this.r.getRc().getLocation().distanceSquaredTo(lowFluxMessage.loc)<=2) {
 				
 				
 				// do a transfer.
 				if(this.getState() != ArchonState.REFUELING) {
 					double amountToTransfer = REFUEL_FLUX;
 					
 					if(amountToTransfer > this.r.getRc().getFlux()) {
 						amountToTransfer = this.r.getRc().getFlux();
 					}
 					
 					this.queueFluxTransfer(lowFluxMessage.loc, lowFluxMessage.level, amountToTransfer);
 					System.out.println("Archon refueling!");
 					this.pushState(ArchonState.REFUELING);
 				}
 			} else {
 				System.out.println("Requester was out of range.");
 			}
 		}
 		
 
 		
 	}
 	
 	// Helper stuffs
 	
 	protected boolean canSpawn(Direction heading) {
 		RobotController rc = this.r.getRc();
 		try {
 			return r.getRc().canMove(heading) &&
 					 r.getRc().senseObjectAtLocation(this.r.getRc().getLocation().add(heading), RobotLevel.POWER_NODE) == null;
 		} catch (GameActionException e) {
 			e.printStackTrace();
 			return false;
 		}		
 	}
 	
 	protected boolean canSpawn() {
 		return canSpawn(this.r.getRc().getDirection());
 	}
 	
 	protected boolean refuelRobotsIfPossible() {
 		RobotController rc = this.r.getRc();
 		StateCache cache = this.r.getCache();
 		RobotInfo[] nearBots = cache.getFriendlyRobots();
 		for(RobotInfo robot : nearBots) {
 			if(this.r.getRc().getFlux() >= REFUEL_FLUX &&
 				robot.type != RobotType.ARCHON &&
 				robot.type != RobotType.TOWER &&
 				robot.flux < REFUEL_THRESHOLD) {
 				this.queueFluxTransfer(robot.location, RobotLevel.ON_GROUND,  REFUEL_FLUX);
 				this.r.getNav().setTarget(robot.location, true);
 				this.pushState(ArchonState.MOVING);
 				return true;
 			}
 		}
 		return false;
 		
 	}
 	
 	protected boolean isNearArchons() {
     StateCache cache = r.getCache();
     RobotController rc = r.getRc();
     MapLocation myLoc = rc.getLocation();		
     
     MapLocation[] archons = cache.getFriendlyArchonLocs();
     for(MapLocation archonLoc: archons) {
     	if(archonLoc == myLoc) {
     		continue;
     	}
     	if(myLoc.distanceSquaredTo(archonLoc) <= GameConstants.PRODUCTION_PENALTY_R2) {
     		return true;
     	}
     }
 		return false;
 	}
 	
 	protected MapLocation getNearestCapturableNode() {
 		StateCache cache = r.getCache();		
 		RobotController rc = r.getRc();
 		MapLocation[] capturableNodes = cache.senseCapturablePowerNodes();
 		MapLocation nearest = null;
 		MapLocation myLoc = rc.getLocation();
 		double bestDistance = Double.MAX_VALUE;
 		for(MapLocation loc : capturableNodes) {
 			double distance = loc.distanceSquaredTo(myLoc);
 			if(distance < bestDistance) {
 				nearest = loc;
 				bestDistance = distance;
 			}
 		}	
 		return nearest;
 	}
 	
 	// Helper stuffs
 	protected MapLocation getRandomCapturableNode() {
 		StateCache cache = r.getCache();		
 		RobotController rc = r.getRc();
 		MapLocation[] capturableNodes = cache.senseCapturablePowerNodes();
 		Random rng = new Random(this.r.getRc().getRobot().getID());
 		return capturableNodes[rng.nextInt(capturableNodes.length)];
 	}
 	
 	
 	// Cooldown stuffs
 	
 	protected void updateCooldowns() {
 		if(spreadingCooldown > 0) {
 			spreadingCooldown--;
 		}
 		if(buildingCooldown > 0) {
 			buildingCooldown--;
 		}
 		if(moveFailCooldown > 0 && this.getState() == ArchonState.MOVING) {
 			moveFailCooldown--;
 		}
 	}
 	
 	protected void initCooldowns() {
 		this.spreadingCooldown = 0;
 		this.buildingCooldown = 0;
 		this.moveFailCooldown = 0;
 	}
 
 	// Stack state stuffs
 
 	protected ArchonState getState() {
 		if(this.stateStackTop < 0) {
 			return ArchonState.LOITERING;
 		}
 		return this.stateStack[this.stateStackTop];
 	}
 	
 	protected void pushState(ArchonState state) {
 		if(this.stateStackTop < 0) {
 			this.stateStackTop = 0;
 		} else {
 			this.stateStackTop++;
 		}
 		
 		if(state == ArchonState.MOVING) {
 			this.moveFailCooldown = MOVE_FAIL_COUNTER;
 		}
 		
 		// Check to see if we need to expand the stack size
 		if(this.stateStackTop >= this.stateStack.length) {
 			ArchonState[] newStack = new ArchonState[this.stateStack.length*2];
 			for(int i = 0; i < this.stateStack.length; ++i) {
 				newStack[i] = this.stateStack[i];
 			}
 			this.stateStack = newStack;
 			System.out.println("Grew stack:");
 			printStack();
 		}
 		this.stateStack[this.stateStackTop] = state;
 		printStack();
 	}
 
 	protected ArchonState popState() {
 		ArchonState state = this.getState();
 		if(this.stateStackTop >= 0) {
 			this.stateStackTop--;	
 		}
 		printStack();
 		// Push the default loitering state if somehow we popped the last state
 		return state;
 	}
 	
 	protected void initStateStack() {
 		this.stateStack = new ArchonState[10];
 		this.stateStackTop = -1;
 		this.pushState(ArchonState.LOITERING);
 	}
 	
 	protected void printStack() {
 		System.out.println("State stack top: "  + this.stateStackTop);
 		System.out.print("Stack: [ ");
 		for(int i = 0; i <= this.stateStackTop; ++i) {
 			System.out.print(" " +  this.stateStack[i]  + " ");
 		}
 		System.out.print(" ]\n");
 	}
 	
 }
