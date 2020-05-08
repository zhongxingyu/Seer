 package examplefuncsplayer;
 //adding a comment to test git
 //new comment!
 import battlecode.common.*;
 import static battlecode.common.GameConstants.*;
 
 public class RobotPlayer implements Runnable {
 
 	private final RobotController myRC;
 
     public RobotPlayer(RobotController rc) {
         myRC = rc;
     }
     ComponentType[] testBuilding ={ComponentType.ANTENNA,ComponentType.ARMORY,ComponentType.BEAM,
     		                       ComponentType.BLASTER,ComponentType.BUG,ComponentType.BUILDING_MOTOR,
     		                       ComponentType.BUILDING_SENSOR,ComponentType.CONSTRUCTOR,ComponentType.DISH,
     		                       ComponentType.DROPSHIP,ComponentType.DUMMY,ComponentType.FACTORY,
     		                       ComponentType.FLYING_MOTOR,ComponentType.HAMMER,ComponentType.HARDENED,
     		                       ComponentType.IRON,ComponentType.JUMP,ComponentType.LARGE_MOTOR,ComponentType.MEDIC,
     		                       ComponentType.MEDIUM_MOTOR,ComponentType.NETWORK,ComponentType.PLASMA,ComponentType.PLATING,
     		                       ComponentType.PROCESSOR,ComponentType.RADAR,ComponentType.SATELLITE,ComponentType.SHIELD,
     		                       ComponentType.SIGHT,ComponentType.SMALL_MOTOR,ComponentType.TELESCOPE};
     Boolean tried = false;
 	public void run() {
 		ComponentController [] components = myRC.newComponents();
 		System.out.println(java.util.Arrays.toString(components));
 		System.out.flush();
 		if(myRC.getChassis()==Chassis.BUILDING){
 			System.out.println(components[1].componentClass()+ " length "+components.length);
 			runBuilder((MovementController)components[0],(BuilderController)components[2]);
 		}
 		else
 			runMotor((MovementController)components[0]);
 	}
 
 	public void testit(MovementController m) {
 		m.withinRange(myRC.getLocation());
 	}
 
 	public void runBuilder(MovementController motor, BuilderController builder) {
 	
 		while (true) {
             try {
 
 				myRC.yield();
 
 				if(!motor.canMove(myRC.getDirection()))
 					motor.setDirection(myRC.getDirection().rotateRight());
 				else if(myRC.getTeamResources()>=2*Chassis.LIGHT.cost)
 					for(int i=1; i<testBuilding.length; i++){
 						try{
 							builder.build(testBuilding[i],myRC.getLocation().add(myRC.getDirection()),RobotLevel.ON_GROUND);
 							System.out.println(testBuilding[i].name());
 							myRC.yield();
						}catch(IllegalArgumentException e){
							System.out.println("catching an error");
							myRC.yield();
						}catch(GameActionException e){
							System.out.println(testBuilding[i].name());
 							myRC.yield();
 						}
 					}
 				//builder.build(Chassis.LIGHT,myRC.getLocation().add(myRC.getDirection()));
             } catch (Exception e) {
                 System.out.println("caught exception:");
                 e.printStackTrace();
             }
         }
 	}
 
     public void runMotor(MovementController motor) {
         
         while (true) {
             try {
                 /*** beginning of main loop ***/
                 while (motor.isActive()) {
                     myRC.yield();
                 }
                 
                 if (motor.canMove(myRC.getDirection())) {
                     //System.out.println("about to move");
                     motor.moveForward();
                 } else {
                     motor.setDirection(myRC.getDirection().rotateRight());
                 }
 
                 /*** end of main loop ***/
             } catch (Exception e) {
                 System.out.println("caught exception:");
                 e.printStackTrace();
             }
         }
     }
 }
