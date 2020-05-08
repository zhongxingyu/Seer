 package team035.brains;
 
 import team035.messages.LowFluxMessage;
 import team035.messages.MessageAddress;
 import team035.messages.MessageWrapper;
 import team035.messages.MoveOrderMessage;
 import team035.messages.RobotInfosMessage;
 import team035.messages.SRobotInfo;
 import team035.modules.NavController;
 import team035.modules.RadioListener;
 import team035.robots.BaseRobot;
 import battlecode.common.Clock;
 import battlecode.common.Direction;
 import battlecode.common.GameActionException;
 import battlecode.common.MapLocation;
 import battlecode.common.RobotController;
 import battlecode.common.RobotInfo;
 import battlecode.common.RobotLevel;
 import battlecode.common.RobotType;
 
 public class SoldierBrain extends RobotBrain implements RadioListener {
 
 	public enum SoldierState {
 		HOLD,
 		MOVE,
 		ATTACK,
 		SEEK_TARGET,
 		LOST,
 		LOW_FLUX,
 		OUT_OF_FLUX,
 		WAIT
 	}
 
 	protected SoldierState state;
 
 	protected final static double PLENTY_OF_FLUX_THRESHOLD = 20.0;
 	protected final static double LOW_FLUX_THRESHOLD = 10.0;
 	protected final static double OUT_OF_FLUX_THRESHOLD = 5.0;
 	protected final static double LOST_THRESHOLD = 10;
 	
 	protected int turnsHolding = 0;
 	protected int turnsSinceLastOutOfFluxMessage = 0;
 	
 	public SoldierBrain(BaseRobot r) {
 		super(r);
 
 		state = SoldierState.WAIT;
 
 		r.getRadio().addListener(this, MoveOrderMessage.type);
 		r.getRadio().addListener(this, LowFluxMessage.type);
 		r.getRadio().addListener(this, RobotInfosMessage.type);
 	}
 
 	@Override
 	public void think() {
 		// do some global environmental state checks in order of precedence.
 		if(r.getCache().numEnemyRobotsInAttackRange > 0) {
 			this.state = SoldierState.ATTACK;
 		} else if(this.r.getRc().getFlux() < OUT_OF_FLUX_THRESHOLD) {
 			this.r.getRadio().setEnabled(false);
 			this.r.getRadar().setEnabled(false);
 			this.state = SoldierState.OUT_OF_FLUX;
 			// turn the radio off, shutdown the radar
 		} else if(this.r.getRc().getFlux() < SoldierBrain.LOW_FLUX_THRESHOLD) {
 			this.state = SoldierState.LOW_FLUX;
 		} else if(r.getCache().numRemoteRobots > 0) {
 			
 			// we only get here if we don't see any robots ourselves, but 
 			// OTHER people see robots to attack. But we prefer to transition
 			// into ATTACK if we can.
 			System.out.println("Setting state to SEEK_TARGET");
 			this.state = SoldierState.SEEK_TARGET;
 		}
 
 
 		System.out.println("state: " + this.state);
 		
 		this.displayState();
 
 		switch(this.state) {
 		case WAIT:
 			// this is the more serious do nothing command
 			if(Clock.getRoundNum() > ArchonBrain.ATTACK_TIMING) {
 				// Break out just in case we missed a message
 				this.state = SoldierState.HOLD;
 			}
 			break;
 		case HOLD:
 			// do nothing! we're waiting for someone to tell us where to go.
 			turnsHolding++;
 			
 			if(turnsHolding > LOST_THRESHOLD) {
 				this.state = SoldierState.LOST;
 			}
 			
 			break;
 		case MOVE:
 			turnsHolding = 0;
 			
 			NavController nav = this.r.getNav();
 			if(nav.isAtTarget()) {
 				System.out.println("Soldier reached target");
 				System.out.println("Target: " + nav.getTarget());
 				System.out.println("Location: " + this.r.getRc().getLocation());
 				this.state = SoldierState.HOLD;
 			} else {
 				nav.doMove();
 			}
 			break;
 		case SEEK_TARGET:
 			
 			//if(r.getRc().isMovementActive()) break;
 			
 			// we don't see anyone bad, but others near us do. 
 			// so, turn towards the nearest target.
 			
 			// maybe eventually we should use the centroid code that
 			// archons use for spreading to aim at the center of detected mass?
 			int d2 = 100000;
 			SRobotInfo target = null;
 			for(SRobotInfo enemy : r.getCache().getRemoteRobots()) {
 				if(enemy==null) break;
 				int distanceToRobot = enemy.location.distanceSquaredTo(r.getRc().getLocation());
 				
 				if(distanceToRobot < d2) {
 					d2 = distanceToRobot;
 					target = enemy;
 				}
 			}
 			
 			if(target==null) {
 				// this shouldn't happen - we won't be in this
 				System.out.println("Seeking enemy with no valid targets!");
 				System.out.println("Remote enemies length: " + r.getCache().getRemoteRobots().length);
 				System.out.println("Remote enemies number: " + r.getCache().numRemoteRobots);
 				this.state = SoldierState.HOLD;
 				break;
 			}
 									// state if there are no enemies
 			
 			// move towards the target
 			this.r.getNav().setTarget(target.location, 0);
 			this.state = SoldierState.MOVE;
 			nav = this.r.getNav();
 			if(nav.isAtTarget()) this.state = SoldierState.HOLD;
 			nav.doMove();
 			
 			break;
 		case ATTACK:
 			
 			if(r.getCache().numEnemyRobotsInAttackRange==0) {
 				this.state = SoldierState.MOVE;
 			}
 			
 			if(r.getRc().isAttackActive()) {
 				return;
 			}
 			RobotInfo[] enemies = r.getCache().getEnemyRobots();
 			
 			// now target selection is really dumb - pick the first one in range
 			for(RobotInfo enemy : enemies) {
 				if(r.getRc().canAttackSquare(enemy.location)) {
 					// skip if it's a tower and there are other baddies around or if
 					// it's a tower not connected to the graph
 					if((enemy.type == RobotType.TOWER && r.getCache().numEnemyAttackRobotsInRange > 0) ||
 							isInvulnerableTower(enemy)) {
 						continue;
 					}
 					RobotLevel l = RobotLevel.ON_GROUND;
 					if(enemy.type == RobotType.SCOUT)
 						l = RobotLevel.IN_AIR;
  
 					
 					try {
 						// face the enemy if possible
 						RobotController rc = this.r.getRc();
 						Direction towardsEnemy = rc.getLocation().directionTo(enemy.location);
 						if(rc.getDirection() != towardsEnemy) {
 							if(!rc.isMovementActive()) {
 								rc.setDirection(towardsEnemy);
 							}
 						}
 						// BLAM!
 						r.getRc().attackSquare(enemy.location, l);
 						break;
 					} catch (GameActionException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 					
 				}
 			}
 			
 			break;
 		case LOST:
 			MapLocation archonLoc = this.r.getRc().senseAlliedArchons()[0];
 			System.out.println("Archon loc = " + archonLoc);
 			System.out.println("My loc = " + this.r.getRc().getLocation());
 			this.r.getNav().setTarget(archonLoc, false);
 			this.state = SoldierState.MOVE;
 			turnsHolding = 0;
 			break;
 		case LOW_FLUX:
 			// if we're in low flux mode, we want to path to our nearest archon
 			// and then ask it for flux.
 			if(this.r.getRc().getFlux() > SoldierBrain.LOW_FLUX_THRESHOLD) {
 				System.out.println("someone refueled me!");
 				this.state = SoldierState.HOLD;
 				break;
 			}
 			
 			MapLocation nearestFriendlyArchon = this.r.getCache().getNearestFriendlyArchon();
 			
 			if(this.r.getRc().getLocation().distanceSquaredTo(nearestFriendlyArchon)<=2) {
 				System.out.println("there's an archon nearby that can refuel me!");
 				r.getRadio().addMessageToTransmitQueue(new MessageAddress(MessageAddress.AddressType.ROBOT_TYPE, RobotType.ARCHON), new LowFluxMessage(this.r.getRc().getRobot(), this.r.getRc().getLocation(), RobotLevel.ON_GROUND));				
 			} else {
 				
 				System.out.println("in low flux mode, moving to " + nearestFriendlyArchon);
 
 				this.r.getNav().setTarget(nearestFriendlyArchon, true);
 				// limp towards archon!
 				//this.r.getNav().setTarget(target.location, 0);
 				this.state = SoldierState.MOVE;
 				nav = this.r.getNav();
 				if(nav.isAtTarget()) this.state = SoldierState.HOLD;
 				nav.doMove();				
 			}
 			break;
 			
 		case OUT_OF_FLUX:
 			// check to see if our flux level is back up.
 			if(this.r.getRc().getFlux() > LOW_FLUX_THRESHOLD) {
 				// what should we transition into? move?
 				this.r.getRadio().setEnabled(true);
 				this.r.getRadar().setEnabled(true);				
 				this.state = SoldierState.LOST;
 				// turn the radar+radio back on, etc.
 			}
 			
 //			if(turnsSinceLastOutOfFluxMessage >= 30) {
 //				r.getRadio().addMessageToTransmitQueue(new MessageAddress(MessageAddress.AddressType.BROADCAST), new LowFluxMessage(this.r.getRc().getRobot(), this.r.getRc().getLocation(), RobotLevel.ON_GROUND));
 //				turnsSinceLastOutOfFluxMessage = 0;
 //			}
 //			turnsSinceLastOutOfFluxMessage++;
 //			break;
 			break;
 		}
 	}
 
 	protected void displayState() {
 		String stateString = "NONE";
 		switch(this.state) {
 		case ATTACK:
 			stateString = "ATTACK";
 			break;
 		case HOLD:
 			stateString = "HOLD";
 			break;
 		case LOST:
 			stateString = "LOST";
 			break;
 		case LOW_FLUX:
 			stateString = "LOW_FLUX";
 			break;
 		case MOVE:
 			stateString = "MOVE";
 			break;
 		case OUT_OF_FLUX:
 			stateString = "OUT_OF_FLUX";
 			break;
 		case SEEK_TARGET:
 			stateString = "SEEK_TARGET";
 			break;
 		}
 		this.r.getRc().setIndicatorString(0, stateString);
 	}
 	
 	
 	@Override
 	public void handleMessage(MessageWrapper msg) {
 		if(msg.msg.getType()==MoveOrderMessage.type) {
 			MoveOrderMessage mom = (MoveOrderMessage) msg.msg;
 			// if we get a move order message, update our move destination.
 
 			System.out.println("updating move target to: " + mom.moveTo);
 			this.r.getNav().setTarget(mom.moveTo, 3);
 			
			if(this.state==SoldierState.HOLD) {
 				this.state = SoldierState.MOVE;
 			}
 		} else if (msg.msg.getType()==LowFluxMessage.type) {
 			
 		} else if (msg.msg.getType()==RobotInfosMessage.type) {
 			RobotInfosMessage rlm = (RobotInfosMessage) msg.msg;
 
 			if(!rlm.friendly) {
 				for(SRobotInfo r : rlm.robots) {
 					this.r.getCache().addRemoteRobot(r);
 				}
 			}
 		}
 	}
 	
 	public boolean isInvulnerableTower(RobotInfo robot) {
 		if(robot.type == RobotType.TOWER) {
 			MapLocation[] towerLocs = this.r.getCache().senseCapturablePowerNodes();
 			MapLocation robotLoc = robot.location;
 			for(MapLocation loc : towerLocs) {
 				if(loc.equals(robotLoc)) {
 					return false;
 				}			
 			}
 			return true;
 		}
 		return false;
 	}
 }
