 package Panda.Odometry;
 
 import lcmtypes.*;
 import lcm.lcm.*;
 import april.util.TimeUtil;
 import Panda.sensors.*;
 
 public class PandaDrive
 {
     static final boolean DEBUG = true;
 
     static final float KP = 0.1F;
     static final float STOP = 0.0F;
     static final float MAX_SPEED = 0.5F;
     static final float MIN_SPEED = -0.5F;
     static final float REG_SPEED = 0.4F;
    static final float TPM_RIGHT = 3.164F;
    static final float TPM_LEFT = 3.258F;
 
 
     
     LCM lcm;
     MotorSubscriber ms;
 	PIMUSubscriber ps;
 
     diff_drive_t msg;
     
     float leftSpeed;
     float rightSpeed;
     
     public PandaDrive(){
         
         try{
 			System.out.println("Hello World!");
 			
             ms = new MotorSubscriber();
 			ps = new PIMUSubscriber();
 			// Get an LCM Object
             lcm = LCM.getSingleton();
             
 			// Create a diff_drive message
 			msg = new diff_drive_t();
 			
 			// Set motors enabled
 			// False means Enabled (lcm weirdness)
 			msg.left_enabled = false;
 			msg.right_enabled = false;
 
             leftSpeed = REG_SPEED;
             rightSpeed = REG_SPEED;
 		}
 		// Thread.sleep throws things
 		// Java forces you to be ready to catch them.
 		catch(Throwable t) {
 			System.out.println("Error: Exception thrown");
 		}
     }
 
     public void Stop(){
         // Add Timestamp (lcm really cares about this)
         msg.utime = TimeUtil.utime();
         msg.left = STOP;
         msg.right = STOP;
         lcm.publish("DIFF_DRIVE", msg);
     }
     
     
 	public void turn(double angle) {
 		// gyro derivatives are positive
 		
 		double angle_turned = 0;
 
 		while (angle_turned < angle) {
 				msg.utime = TimeUtil.utime();
 
 				// right turn if angle is negative
 				if (angle <=0 ) { 
 					msg.left = 0.25F;
 					msg.right = -0.25F;
 					
 				}
 				// left turn
 				else {
 					msg.left = -0.25F;
 					msg.right = 0.25F;
 
 				}
 
 				lcm.publish ("10_DIFF_DRIVE", msg);
 
 				
 		}	
 
 	}
 
 	public void turnRight() {
 		// gyro derivatives are negative
 		
 		msg.utime = TimeUtil.utime();
 		msg.left = 0.25F;
 		msg.right = -0.25F;
 		lcm.publish ("10_DIFF_DRIVE", msg);
 
 
 	}
 
 
     public void driveForward (double distance) {
         //Needs to be called in a loop
         //Left encoder: 128.27 ticks/inch
         //Right encoder: 124.571 ticks/inch
         float KError = KP*(ms.getREncoder() - ms.getLEncoder());
         leftSpeed = speedCheck(leftSpeed + KError);
         rightSpeed = speedCheck(rightSpeed + KError);
         double distanceTraveled = 0;
 
 		float lastLeftEncoder, lastRightEncoder;
 		float curLeftEncoder, curRightEncoder;
 		float leftDistance, rightDistance;
 		double[] pimuDerivs = new double[2];
 
         // Add Timestamp (lcm really cares about this)
 		lastLeftEncoder = ms.getLEncoder ();
 		lastRightEncoder = ms.getREncoder ();
 		System.out.println ("last encoders " + lastLeftEncoder + " " + lastRightEncoder);
 
 
 		while (distanceTraveled < distance) {
 		
 			pimuDerivs = ps.getPIMUDerivs ();
 			System.out.printf ("gyro derivs " + pimuDerivs[0] + " "  + pimuDerivs[1]);
 			/*
 			if (pimuDerivs[0] > 3) {
 				
 			}
 			*/
 
 			msg.utime = TimeUtil.utime();
 			msg.left = 0.5F;
 			msg.right = 0.5F;
 
 			lcm.publish("10_DIFF_DRIVE", msg);
 
 
 			curLeftEncoder = ms.getLEncoder ();
 			curRightEncoder = ms.getREncoder ();
 			System.out.println ("current encoders " + curLeftEncoder + " " + curRightEncoder);
 			
 			
 
 			// if encoder values are too low, disregard change in distance 
 			if (lastLeftEncoder < 5 || lastRightEncoder < 5) {
 				lastLeftEncoder = curLeftEncoder;
 				lastRightEncoder = curRightEncoder;
 				continue;
 			}
 
 			leftDistance = (curLeftEncoder - lastLeftEncoder) / TPM_LEFT;
 			rightDistance = (curRightEncoder - lastRightEncoder) / TPM_RIGHT;
 			distanceTraveled += (leftDistance + rightDistance) / 2;
 			
 			lastLeftEncoder = curLeftEncoder;
 			lastRightEncoder = curRightEncoder;
 
 			if (DEBUG){
 				System.out.println ("delta distances " + leftDistance + " " + rightDistance);
 				System.out.println ("distance traveled " + distanceTraveled);
 
 				//System.out.print("Left wheel speed: ");
 				//System.out.print(leftSpeed);
 				//System.out.print("   Right wheel speed: ");
 				//System.out.println(rightSpeed);
 			}
 		}
 /*
 		msg.utime = TimeUtil.utime();
 		msg.left = STOP;
 		msg.right = STOP; 
 		lcm.publish("10_DIFF_DRIVE", msg);
 */
 
     }
     
     private float speedCheck (float speed){
         //If speed is greater then max
         if (speed > MAX_SPEED)
             return MAX_SPEED;
         if (speed < MIN_SPEED)
             return MIN_SPEED;
         return speed;
     }
     
 }
