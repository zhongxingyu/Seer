 package storm.modules;
 
 
 
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 import edu.wpi.first.wpilibj.*;
 import edu.wpi.first.wpilibj.CounterBase.EncodingType;
 import storm.RobotState;
 import storm.interfaces.IShooter;
 import storm.utility.Print;
 /**
  *
  * @author Storm
  */
 
 public class Shooter implements IShooter {
 
     //Joystick shootJoystick;
     SpeedController shooterMotor, transferMotor;
     DigitalInput ready, hallEffect;
     Counter counter;
     Joystick shootJoystick;
     boolean shooting,
 	    readyTripped;
     boolean btn7;
     double motorSpeed,
 	    calculatedMotorSpeed,
 	    wantedRPM,
 	    period,
 	    RPMcurrent,
 	    RPMdifference,
 	    RPMthreshold,
 	    RPMold;
     int state,
 	    timeDifference,
 	    currentTime,
 	    goodRangeCount,
 	    debugCounter;
        
     public Shooter(int shooterMotorChannel,int transferMotorChannel, int IRready, int hallEffectSensor) {
         
         shooterMotor = new Jaguar(shooterMotorChannel);
         transferMotor = new Victor(transferMotorChannel);
         ready = new DigitalInput(IRready);
         hallEffect = new DigitalInput(hallEffectSensor);
 	readyTripped = false;
         counter = new Counter(EncodingType.k1X, hallEffect, hallEffect, false);
         counter.clearDownSource();
         counter.setUpSourceEdge(true, false);
     }
     
     public void startShoot(double distance) {
 	shootJoystick = RobotState.joystickShoot;
 
 	motorSpeed = getMotorSpeed(distance);
         counter.start();
         state = 0;
 	debugCounter = 0;
         shooting = true;
 	goodRangeCount = 0;
 	startTime = System.currentTimeMillis();
 
     }
     
     long startTime = -1;
 
     public void doShoot() {
 	if (!shooting) return;
 	checkRPM();
 	transferMotor.set(-1);
 	if (shootJoystick.getRawButton(7) && !btn7) {
 	    btn7 = true;
 	    state ++;
 	    if (state> 3) state = 0;
 	    switch (state){
 		case 0: motorSpeed = getMotorSpeed(2.5);		
 		    break;
 		case 1: motorSpeed = getMotorSpeed(5);
 		    break;
 		case 2: motorSpeed = getMotorSpeed(3);
 		    break;
 		case 3: motorSpeed = getMotorSpeed(7);
 		    break;
 	    }
 	    shooterMotor.set(motorSpeed);
 	    startTime = System.currentTimeMillis();
 
 	} else if (!shootJoystick.getRawButton(7) && btn7) {
 	    btn7 = false;
 	}
 	Print.getInstance().setLine(2, "Motor Speed: " + motorSpeed);
 
         // set motor speed, check when ready, move ball into shooter, stop once IR sensor is clear
 	/*if (!shooting) return;
 	switch (state){
 	    case 0:
 		transferMotor.set(-1);
 		shooterMotor.set(motorSpeed);
 		startTime = System.currentTimeMillis();
 		state ++;
 		
 		break;
 	    case 1:
 		if (!ready.get() == true){
 		    transferMotor.set(0);
 		    state ++;}
 		break ;
 	    case 2:
 		if (checkRPM() == true){
 		    state ++;
 		}
 		break;
 	    case 3:
 		transferMotor.set(-1);
 		if (!ready.get() == false) {
 		    startTime = System.currentTimeMillis();
 		    state ++;
 		}
 		break;
 	    case 4:
 		if ((System.currentTimeMillis() - startTime) >= 2000){
 		    state ++;
 		}
 		break;
 	    case 5:
 		transferMotor.set(0);
 		shooterMotor.set(0);
 		shooting = false;
 		state = 0;
 		RobotState.BALL_CONTAINMENT_COUNT --;
 		break;
 	    default:
 		break;
 	}*/
     }
 
     private double getMotorSpeed(double distance) {
         //convert distance into rpm into motor speed value
 	wantedRPM = 333.33*distance + 850.63 ;
 	calculatedMotorSpeed = .0003*wantedRPM + 0.0457;
 	Print.getInstance().setLine(3, "Wanted RPM: " + wantedRPM);
 	return calculatedMotorSpeed;
     }
     
     private boolean checkRPM(){
 	//check what the current RPM is
 	
 	period = counter.getPeriod();
 	Print.getInstance().setLine(5, "Ctr:" + counter.get() + "Prd:" + period);
 	//System.out.println("Prd:" + period);
 	if ((System.currentTimeMillis() - startTime) >= 10000)
 	{
 	    return true;
 	}
 	debugCounter ++;
 	if (debugCounter % 5 != 0)
 	{
 	    return false;
 	}
 	
 	if (Double.isInfinite(period) || period <= 0)
 	{
 	    System.out.println("Infinite, period: " + period);
 	    return false;
 	}
 	
 	RPMcurrent = 60/period;
 	RPMold = RPMcurrent;
 	RPMthreshold = wantedRPM / 100;
 	Print.getInstance().setLine(1, "RPM: " + RPMcurrent);
 		
 	Print.getInstance().setLine(4, "RPM difference: " + RPMdifference);
 	Print.getInstance().setLine(0, "???????");
 	
 	/*if (RPMcurrent >= wantedRPM)
 	{
 	    if (RPMdifference > 0){
 		//do nothing
 	    }else {
 		motorSpeed += .0003*RPMdifference;
 		shooterMotor.set(motorSpeed);
 		Print.getInstance().setLine(0, "motor speed-: " + motorSpeed);
 	    }
 	    
 	    
 	}else {
 	    if (RPMdifference < 0){
 		//do nothing
 	    }else {
 		motorSpeed += .0003*RPMdifference;
 		shooterMotor.set(motorSpeed);
 	    	Print.getInstance().setLine(0, "motor speed+: " + motorSpeed);
 	    }
 	}*/
	RPMdifference = wantedRPM - RPMcurrent;
 	motorSpeed += .0003*RPMdifference;
 	if (motorSpeed <0) motorSpeed = 0;
 	if (motorSpeed >1) motorSpeed = 1;
 	shooterMotor.set(motorSpeed);
 	Print.getInstance().setLine(0, "motor speed-: " + motorSpeed);
 	    	
 	if (Math.abs(RPMdifference) < RPMthreshold){
 	    goodRangeCount ++;
 	}else goodRangeCount = 0;
 	//System.out.println("goodRangeCount:" + goodRangeCount);
	System.out.println("RPMW:" + wantedRPM + " RPMC: " + RPMcurrent + " RPMD: " + RPMdifference + " MTRSpd: " + motorSpeed + " PRD: " + period);	
 
 	if(goodRangeCount > 10){
 	    return true;
 	}else return false;
     }
     
     private void endShoot(){
 	shooterMotor.set(0);
 	transferMotor.set(0);
 	state = 0;
 	shooting = false;
 	
     }
 
     public boolean isShooting() {
         return shooting;
     }
 
 }
