 package org.usfirst.frc1318.autonomous.macros;
 
 import org.usfirst.frc1318.FRC2013.shared.ReferenceData;
 import org.usfirst.frc1318.autonomous.AutoTask;
 
 import edu.wpi.first.wpilibj.Timer;
 
/**
 * This macro should lower the loading shoot so that frisbees can be loaded into
 * the robot.
 * 
 * this is accomplished by going forward and then backwards quickly, as the 
 * shoot is not actuated.
 * 
 * This macro takes 1 second
 * 
 * @author Graham
 *
 */
 public class LowerLoadingShoot implements AutoTask{
 	int state = 0;
 	boolean hasInit = false;
 	boolean hasFinished = false;
 	double lastTime;
 	
	double forwardDuration = .4; //sec
	double reverseDuration = .6; //sec
 	
 	ReferenceData ref;
 
 	public void init() {
 		ref = ReferenceData.getInstance();
 		hasInit = true;
 	}
 
 
 	public void run() {
 		//TODO
 		/*
 		 * set last time
 		 * 
 		 * go forward
 		 * 
 		 * set last time
 		 * 
 		 * go reverse
 		 */
 		switch(state){
 		case 0:
			lastTime = Timer.getFPGATimestamp();
 			state++;
 		case 1:
 			forward();
 			break;
 		case 2:
			lastTime = Timer.getFPGATimestamp();
 			state++;
 		case 3:
 			reverse();
 			break;
 		default:
 			System.out.println("We've got a problem! -- org.usfirst.frc1318.autonomous.macros.LowerLoadingShoot");
 		}
 		
 	}
 
 	private void forward(){
		if(Timer.getFPGATimestamp() - this.lastTime < this.forwardDuration){
 			ReferenceData.getInstance().getUserInputData().setJoystickLeft(.31);
 			ReferenceData.getInstance().getUserInputData().setJoystickRight(.31);
 			ReferenceData.getInstance().getUserInputData().setJoystickX(0);
 			ReferenceData.getInstance().getUserInputData().setJoystickY(.31);
 		}else{
 			state = 2;
 		}
 	}
 	
 	private void reverse() {
		if(Timer.getFPGATimestamp() - this.lastTime < this.reverseDuration){
 			ReferenceData.getInstance().getUserInputData().setJoystickLeft(-1);
 			ReferenceData.getInstance().getUserInputData().setJoystickRight(-1);
 			ReferenceData.getInstance().getUserInputData().setJoystickX(0);
 			ReferenceData.getInstance().getUserInputData().setJoystickY(-1);
 		}else{
 			ReferenceData.getInstance().getUserInputData().setJoystickLeft(0);
 			ReferenceData.getInstance().getUserInputData().setJoystickRight(0);
 			ReferenceData.getInstance().getUserInputData().setJoystickX(0);
 			ReferenceData.getInstance().getUserInputData().setJoystickY(0);
 			hasFinished = true;
 		}
 	}
 	
 	public void cancel() {
 		// TODO Auto-generated method stub
 		
 	}
 
 
 	public boolean hasFinished() {
 		return hasFinished;
 	}
 
 
 	public boolean hasInitalized() {
 		return hasInit;
 	}
 
 }
