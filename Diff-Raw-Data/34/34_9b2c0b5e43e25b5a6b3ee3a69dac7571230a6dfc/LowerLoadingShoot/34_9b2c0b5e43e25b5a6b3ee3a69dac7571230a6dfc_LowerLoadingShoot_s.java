 package org.usfirst.frc1318.autonomous.macros;
 
 import org.usfirst.frc1318.FRC2013.shared.ReferenceData;
 import org.usfirst.frc1318.autonomous.AutoTask;
 
 import edu.wpi.first.wpilibj.Timer;
 
 public class LowerLoadingShoot implements AutoTask{
 	int state = 0;
 	boolean hasInit = false;
 	boolean hasFinished = false;
 	double lastTime;
 	
	double forwardDuration = .2; //sec
	double reverseDuration = .5; //sec
 	
 	ReferenceData ref;
	Timer timer;
 
 	public void init() {
 		ref = ReferenceData.getInstance();
		timer = new Timer();
		timer.start();
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
			lastTime = timer.get();
 			state++;
 		case 1:
 			forward();
 			break;
 		case 2:
			lastTime = timer.get();
 			state++;
 		case 3:
 			reverse();
 			break;
 		default:
 			System.out.println("We've got a problem! -- org.usfirst.frc1318.autonomous.macros.LowerLoadingShoot");
 		}
 		
 	}
 
 	private void forward(){
		if(timer.get() - this.lastTime < this.forwardDuration){
 			ReferenceData.getInstance().getUserInputData().setJoystickLeft(.31);
 			ReferenceData.getInstance().getUserInputData().setJoystickRight(.31);
 			ReferenceData.getInstance().getUserInputData().setJoystickX(0);
 			ReferenceData.getInstance().getUserInputData().setJoystickY(.31);
 		}else{
 			state = 2;
 		}
 	}
 	
 	private void reverse() {
		if(timer.get() - this.lastTime < this.reverseDuration){
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
