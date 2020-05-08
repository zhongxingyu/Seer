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
 import battlecode.common.RobotLevel;
 import battlecode.common.RobotType;
 
 public class ArchonBrain extends RobotBrain implements RadioListener {
 	protected final static double NODE_DETECTION_RADIUS_SQ = 16;
 	protected final static double INITIAL_ROBOT_FLUX = 30;
 	protected ArchonState[] stateStack;
 	protected int stateStackTop;
 
 	protected enum ArchonState {
 		LOITERING, MOVING, BUILDING, SPREADING, REFUELING, FLEEING
 	}
 
 	protected ArchonState state;
 	protected MapLocation nodeBuildLocation;
 	protected PowerNode targetPowerNode;
 	
 	protected boolean fluxTransferQueued = false;
 	protected MapLocation fluxTransferLoc = null;
 	protected RobotLevel fluxTransferLevel = null;
 	protected double fluxTransferAmount = 0.0;
 	private int spreadingCooldown;
 	
 	public ArchonBrain(BaseRobot r) {
 		super(r);
 		
 		r.getRadio().addListener(this, ClaimNodeMessage.type);
 		r.getRadio().addListener(this, LowFluxMessage.type);
 		this.initCooldowns();
 		this.initStateStack();
 	}
 
 	
 
 	@Override
 	public void think() {
 		this.updateCooldowns();
 		
 
 		switch(this.getState()) {
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
 		}
 	}
 	
 	protected void refuel() {
 		GameObject go;
 		try {
 			go = this.r.getRc().senseObjectAtLocation(this.r.getRc().getLocation().add(this.r.getRc().getDirection()), RobotLevel.ON_GROUND);
 			if(go!=null) {
 				this.r.getRc().transferFlux(fluxTransferLoc, fluxTransferLevel, fluxTransferAmount);
 			}
 		} catch (GameActionException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		this.popState();
 	}
 	
 	
 	protected void loiter() {
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
     
     this.popState();
     this.pushState(ArchonState.MOVING);
     System.out.println("Archon loitering->moving");
     this.nodeBuildLocation = nearNodeLoc;
     this.move();
 	}
 	
 	protected void spread() {
 		System.out.println("Trying to spread!");
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
     		System.out.println("Computed centroid to be " + centroid);
     	}
     }
     if(nearbyArchons > 0) {
     	// check we can move
 	    if(!rc.isMovementActive()) {
 		    centroid = new MapLocation((int)((float)centroid.x / nearbyArchons), (int)((float)centroid.y / nearbyArchons));
 		    System.out.println("Computed centroid to be " + centroid);
 		    System.out.println("I'm at " + myLoc);
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
     	this.spreadingCooldown = 3;
     	this.popState();
     }
 	}
 	
 
 
 	protected void build() {
 		NavController nav = this.r.getNav();
 		RobotController rc = this.r.getRc();
 		try {
 			if(nav.isAtTarget()) {
 				
 				r.getRadio().addMessageToTransmitQueue(new MessageAddress(AddressType.BROADCAST), new ClaimNodeMessage());
 				
 				Direction nodeDirection = rc.getLocation().directionTo(nodeBuildLocation);
 				if(rc.getDirection() != nodeDirection) {
 					if(!rc.isMovementActive()) {
 						rc.setDirection(nodeDirection);
 						return;
 					}			
 				}
 				if(rc.getFlux() >= RobotType.TOWER.spawnCost) {
 					if(!rc.isMovementActive()) {
 						GameObject targetContent = rc.senseObjectAtLocation(nodeBuildLocation, RobotLevel.ON_GROUND);
 						if(targetContent == null) {
 							rc.spawn(RobotType.TOWER);					
 						}
 						this.popState();
 						this.pushState(ArchonState.LOITERING);
 						System.out.println("Archon building->loitering");
 						return;
 					}
 
 				}
 			} else {
 				nav.doMove();
 			}
 		} catch (GameActionException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	protected void move() {
 		
 		this.spawnRobotIfPossible();
 		
 		NavController nav = this.r.getNav();
 		nav.doMove();
 		if(nav.isAtTarget()) {
 			this.popState();
 			this.pushState(ArchonState.BUILDING);
 			System.out.println("Archon moving->building");			
 			build();
 		}
 	}	
 	
 	protected void queueFluxTransfer(MapLocation loc, RobotLevel level, double amount) {
 		this.fluxTransferLoc = loc;
 		this.fluxTransferLevel = level;
 		this.fluxTransferAmount = amount;
		this.fluxTransferQueued = true;
 	}
 	
 	protected boolean spawnRobotIfPossible() {
 		
 		if(!r.getRc().isMovementActive() && r.getRc().getFlux() > RobotType.SOLDIER.spawnCost + INITIAL_ROBOT_FLUX) {
 			
 			if(r.getRc().canMove(r.getRc().getDirection())) {
 				try {
 					r.getRc().spawn(RobotType.SOLDIER);
 					this.queueFluxTransfer(this.r.getRc().getLocation().add(this.r.getRc().getDirection()), RobotLevel.ON_GROUND, 0.9*INITIAL_ROBOT_FLUX);;
 					return true;
 				} catch (GameActionException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
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
 			this.popState();
 			this.pushState(ArchonState.LOITERING);
 		}
 		
 		if(msg.msg.getType()==LowFluxMessage.type) {
 			LowFluxMessage lowFluxMessage = (LowFluxMessage) msg.msg;
 			
 			if(this.r.getRc().getLocation().distanceSquaredTo(lowFluxMessage.loc)<=2) {
 				
 				
 				// do a transfer.
 				if(this.getState() != ArchonState.REFUELING) {
 					double amountToTransfer = INITIAL_ROBOT_FLUX*0.75;
 					
 					if(amountToTransfer > this.r.getRc().getFlux()) {
 						amountToTransfer = this.r.getRc().getFlux();
 					}
 					
 					this.queueFluxTransfer(lowFluxMessage.loc, lowFluxMessage.level, amountToTransfer);
 					System.out.println("Archon refueling!");
 					this.pushState(ArchonState.REFUELING);
 				}
 			}
 		}
 		
 	}
 	
 	// Helper stuffs
 	
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
 	}
 	
 	protected void initCooldowns() {
 		this.spreadingCooldown = 0;
 	}
 
 	// Stack state stuffs
 
 	protected ArchonState getState() {
 		if(this.stateStackTop < 0) {
 			return ArchonState.LOITERING;
 		}
 		return this.stateStack[this.stateStackTop];
 	}
 	
 	protected void pushState(ArchonState state) {
 		this.stateStackTop++;
 		// Check to see if we need to expand the stack size
 		if(this.stateStack.length <= this.stateStackTop) {
 			ArchonState[] newStack = new ArchonState[this.stateStack.length*2];
 			System.out.println("State stack top: "  + this.stateStackTop);
 			System.out.print("Growing stack: [ ");
 			for(int i = 0; i < this.stateStack.length; ++i) {
 				System.out.print(" " +  this.stateStack[i]  + " ");
 				newStack[i] = this.stateStack[i];
 			}
 			System.out.print(" ]\n");
 			this.stateStack = newStack;
 		}
 		this.stateStack[this.stateStackTop] = state;
 	}
 
 	protected ArchonState popState() {
 		ArchonState state = this.getState();
 		if(this.stateStackTop >= 0) {
 			this.stateStackTop--;	
 		}
 		// Push the default loitering state if somehow we popped the last state
 		return state;
 	}
 	
 	protected void initStateStack() {
 		this.stateStack = new ArchonState[10];
 		this.stateStackTop = -1;
 		this.pushState(ArchonState.LOITERING);
 	}
 	
 }
