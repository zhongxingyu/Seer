 package storm.modules;
 
 
 
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 import edu.wpi.first.wpilibj.*;
 import edu.wpi.first.wpilibj.CounterBase.EncodingType;
 import storm.RobotState;
 import storm.interfaces.IShooter;
 //import storm.utility.Print;
 /**
  *
  * @author Storm
  */
 
 public class Shooter implements IShooter
 {
 
     //Joystick shootJoystick;
     SpeedController shooterMotor,
 	    transferMotor;
     //PIDController motorController;
     DigitalInput ready,
 	    hallEffect;
     Counter counter;
     Joystick shootJoystick;
     boolean shooting,
 	    readyTripped,
             continuousShooting,
             preShooting;
     boolean btn7;
     double motorSpeed,
 	    calculatedMotorSpeed,
 	    wantedRPM,
             wantedRPMold,
             wantedDistance,
 	    period,
 	    RPMcurrent,
 	    RPMdifference,
 	    RPMthreshold,
 	    RPMchange,
 	    RPMold;
     int state,
 	    timeDifference,
 	    currentTime,
 	    goodRangeCount,
 	    debugCounter,
 	    modFactor;
        
     public Shooter(int shooterMotorChannel,int transferMotorChannel, int IRready, int hallEffectSensor)
     {    
         shooterMotor = new Jaguar(shooterMotorChannel);
         transferMotor = new Victor(transferMotorChannel);
         ready = new DigitalInput(IRready);
         hallEffect = new DigitalInput(hallEffectSensor);
 	readyTripped = false;
         continuousShooting = false;
         preShooting = false;
         shooting = false;
         wantedRPMold = -1;
         wantedDistance = 0;
         debugCounter = 0;
         modFactor = 10;
         counter = new Counter(EncodingType.k1X, hallEffect, hallEffect, false);
         counter.clearDownSource();
         counter.setUpSourceEdge(true, false);
     }
     
     public void startShoot(boolean useTable, double distance)
     {
 	shootJoystick = RobotState.joystickShoot;
 //        System.out.println("startShoot " + distance);
         counter.start();
         state = 0;
         shooting = true;
 	goodRangeCount = 0;
 	startTime = System.currentTimeMillis();
         wantedDistance = distance;
     }
     
     long startTime = -1;
 
     public void doShoot()
     {
 	/*if (!shooting) return;
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
 	Print.getInstance().setLine(2, "Motor Speed: " + motorSpeed);*/
 
         // set motor speed, check when ready, move ball into shooter, stop once IR sensor is clear
        
         setRPM(wantedDistance);
 //        System.out.println("Debug 4" + "Shoot: " + shooting + " PreShoot: " + preShooting);
 	if (!shooting && !preShooting)
 	{
            /* period = counter.getPeriod();
             debugCounter ++;
             if (RPMcurrent > 1200) modFactor = 5;
             else modFactor = 10;
             if (debugCounter % modFactor != 0)
             {
                 return;
             }
 	
             if (Double.isInfinite(period) || period <= 0)
             {
                 return;
             }
             RPMcurrent = 60/period;*/
             
             return;
         }
 //        System.out.println("State: " + state);
 	switch (state)
 	{
 	    case 0:
 		transferMotor.set(-1);
 		startTime = System.currentTimeMillis();
                 if (continuousShooting) return;
 		state ++;		
 		break;
 	    case 1:
 		if (!ready.get() == true)
 		{
 		    transferMotor.set(0);
                     if (preShooting) endShoot();
 		    state ++;}
 		break ;
 	    case 2:
 		if (checkRPM() == true)
 		{
 		    state ++;
 		}
 		break;
 	    case 3:
 		transferMotor.set(-1);
 		if (!ready.get() == false)
 		{
 		    startTime = System.currentTimeMillis();
 		    state ++;
 		}
 		break;
 	    case 4:
 		if ((System.currentTimeMillis() - startTime) >= 3000)
 		{
 		    state ++;
 		}
 		break;
 	    case 5:
 		RobotState.BALL_CONTAINMENT_COUNT --;                
                 endShoot();
 		break;
 	    default:
 		break;
 	}
     }
 
     private double setMotorSpeed(double distance) {
         //convert distance into rpm into motor speed value  
 	//wantedRPM = 333.33*distance + 850.63 ;
 	wantedRPM = 46.209*distance*distance - 190.39*distance + 2469.3;
 	calculatedMotorSpeed = .0003*wantedRPM + 0.0457;
         if (Double.isNaN(distance))
 	    {
             wantedRPM = 3181;
             return 1;
 	    }
 	return calculatedMotorSpeed;
     }
     
     private boolean checkRPM()
     {
         if ((System.currentTimeMillis() - startTime) >= 3000)
 	{
             return true;
 	}
         if (goodRangeCount > 15)
 	{
 	    return true;
 	}else return false;
     }
 
     private void setRPM(double distance) 
     {            
         //wantedRPM = 333.33*distance + 850.63 ;
 	wantedRPM = 46.209*distance*distance - 190.39*distance + 2469.3;
 
        // System.out.println(System.currentTimeMillis() + " RPMWZ:" + wantedRPM + " RPMC: " + RPMcurrent + " RPMD: " + RPMdifference + " MTRSpd: " + motorSpeed +  " GRC: " + goodRangeCount+ " RPMCge: " + RPMchange);
         //System.out.println("setRPM DDistance: " + distance);
         if (distance == 0) 
 	{
             wantedRPM = 0;
             calculatedMotorSpeed = 0;
             motorSpeed = 0;
         }
         else if(wantedRPMold != wantedRPM)
         {
             calculatedMotorSpeed = .0003*wantedRPM + 0.0457;
             if (Double.isNaN(distance))
             {
                 wantedRPM = 3181;
                 calculatedMotorSpeed = 1;
             }        
             motorSpeed = calculatedMotorSpeed;
         }
         wantedRPMold = wantedRPM;
         //System.out.println("Debug 1");
         shooterMotor.set(motorSpeed);
 	period = counter.getPeriod();
         //System.out.println("Debug A 2");	
 	debugCounter ++;
 	if (debugCounter % modFactor != 0)
 	{
 	    return;
 	}	
 	if (Double.isInfinite(period) || period <= 0)
 	{
 	    //System.out.println("Infinite, period: " + period);
 	    return;
 	}	
 	RPMcurrent = 60/period;
 	if (RPMcurrent > 3500) return;
 	if (RPMcurrent > 1200) modFactor = 5;
 	else modFactor = 10;
         if(RPMcurrent > 3200 && motorSpeed == 1)
 	{
             return;
         }
 //        System.out.println("Debug B 2");
 	RPMthreshold = wantedRPM / 50;
 	RPMchange = RPMold - RPMcurrent;
 	RPMold = RPMcurrent;
 	if (Math.abs(RPMchange) > 100)
 	{
 //	    System.out.println(System.currentTimeMillis() + " RPMx:" + wantedRPM + " RPMC: " + RPMcurrent + " RPMD: " + RPMdifference + " MTRSpd: " + motorSpeed + " GRC: " + goodRangeCount + " RPMCge: " + RPMchange);
 	    return;
 	}
 	/*Print.getInstance().setLine(1, "RPM: " + RPMcurrent);
 	Print.getInstance().setLine(4, "RPM difference: " + RPMdifference);
 	Print.getInstance().setLine(0, "???????");*/
 //        System.out.println("Debug 2");
 	RPMdifference = wantedRPM - RPMcurrent;
 	motorSpeed += .00002*RPMdifference;
 	if (motorSpeed <0) motorSpeed = 0;
 	if (motorSpeed >1) motorSpeed = 1;
 	shooterMotor.set(motorSpeed);
 	if (Math.abs(RPMdifference) < RPMthreshold)
 	{
 	    goodRangeCount ++;
 	}else goodRangeCount = 0;
 	//System.out.println("goodRangeCount:" + goodRangeCount);
 //	System.out.println(System.currentTimeMillis() + " RPMW:" + wantedRPM + " RPMC: " + RPMcurrent + " RPMD: " + RPMdifference + " MTRSpd: " + motorSpeed +  " GRC: " + goodRangeCount+ " RPMCge: " + RPMchange);
     }
     
     public void endShoot()
     {
 //        System.out.println("endShoot");
         state = 0;
 	shooterMotor.set(0);
 	transferMotor.set(0);
         wantedDistance = 0;
 	shooting = false;
 	preShooting = false;
         continuousShooting = false;
     }
 
     public boolean isShooting()
     {
         return shooting;
     }
 
     public double getRPM()
     {
         return RPMcurrent;
     }
 
     public void setContinuousShoot(boolean continuousShoot)
     {
 //        System.out.println("continuousShoot");
         if (preShooting) return;
         continuousShooting = continuousShoot;
         state = 0;
     }
 
     public void preShoot() 
     {
   //      System.out.println("preShoot");
         if (continuousShooting) return;
         preShooting = true;
         state = 0;
         transferMotor.set(-1);
         wantedDistance = 0;
     }
 
     public void warmUp() {
	wantedDistance = 8;
	wantedRPM = 3000;
     }
 }
 
 
