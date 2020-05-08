 package current;
 
 import current.behaviors.*;
 import battlecode.common.*;
 
 //## !! ONLY ACTING EVERY 2 ROUNDS; WTF? !!
 // Some robots glitch at round 178, regardless of map : 10000 bytecodes, receive 0 packet that validates (??)
 // When reading from 62462; others probably only don't glitch because they're on their off round, other bug
 public class SoldierRobot extends BaseRobot {
 	
 	public Behavior currentBehavior;
 	public Behavior prevBehavior;
 	
 	public CombatBehavior combatBehavior;
 	public CaptureBehavior captureBehavior;
 	public SwarmBehavior swarmBehavior; // This should maybe be a slow creep, not a blind rush; or that could be a different behavior
 	public FortifyBehavior fortifyBehavior;
 	public ScoutBehavior scoutBehavior;
 	public TravelBehavior travelBehavior;
 
 	
 	SoldierRobot(RobotController rc) throws GameActionException{
 		super(rc);
 		combatBehavior = new CombatBehavior(this);
         captureBehavior = new CaptureBehavior(this);
         swarmBehavior = new SwarmBehavior(this);
         fortifyBehavior = new FortifyBehavior(this);
         scoutBehavior = new ScoutBehavior(this);
         travelBehavior = new TravelBehavior(this);
         currentBehavior = scoutBehavior; //default
         readMessages(comm.getSticky(2)); // Initialization message
 	}
 
 
 	public void run() throws GameActionException{
		super.run();
 		if (rc.isActive()) {
 			readAllMessages(); // in BaseRobot
 			
 			currentBehavior.checkBehaviorChange();
 			currentBehavior.run();
 			//## use remaining bytecodes if we don't need to conserve power
 		}
 	}
 	
 	// Return the length of the message
 	@Override
 	protected int processMessage(char[] data, int startIdx){
 		System.out.println("Recieved message: header "+(int)data[startIdx]);
 		switch(data[startIdx]){
 		case 0:
 			// HACK: this seems to get called ~40 times/round (when we have no data?), so break the message read
 			// loop when it happens. Doesn't appear to mess with proper messages.
 			return data.length; 
 					
 		case 1:  // Subscribe
 			subscribe(data[startIdx+1]);
 			break;
 			
 		case 2: // Fortify: default (headquarters, radius 4, two directions)
 			setBehavior(fortifyBehavior);
 			fortifyBehavior.buildFort(HQ, 2, HQ.directionTo(rc.senseEnemyHQLocation()).rotateLeft().rotateLeft(), 3);
 			break;
 			
 		case 3: // Rush: default (enemy HQ)
 			setBehavior(travelBehavior);
 			travelBehavior.destination = eHQ;
 			//## set combat, movement mode?
 			break;
 			
 		default:
 			System.out.println("Unrecognised message"); //DEBUG
 		}
 		return 3; // Minimum message length
 	}
 	
 	
 	public void setBehavior(Behavior b){
 		rc.setIndicatorString(1, b.getClass().getName()); //DEBUG
 		prevBehavior = currentBehavior;
 		currentBehavior = b;
 	}
 }
