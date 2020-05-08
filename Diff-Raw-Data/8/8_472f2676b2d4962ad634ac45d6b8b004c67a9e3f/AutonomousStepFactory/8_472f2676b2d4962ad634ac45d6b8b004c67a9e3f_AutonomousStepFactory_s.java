 package com.edinarobotics.zephyr.autonomous;
 
 import com.edinarobotics.utils.autonomous.*;
 import com.edinarobotics.zephyr.Zephyr;
 
 /**
  *
  */
 public class AutonomousStepFactory {
     private Zephyr robot;
     
     public AutonomousStepFactory(Zephyr robot){
         this.robot = robot;
     }
     
     public AutonomousStep getShooterFireStep(double shooterSpeed, int numShots){
         final double PISTON_SET_DELAY = 1;
         final double BALL_LOAD_DELAY = 2.5;
         //Steps to bring the piston up and down
         AutonomousStep[] fireSteps = new AutonomousStep[7];
         fireSteps[0] = new ShooterPistonControlStep(true, robot);
         fireSteps[1] = new IdleWaitStep(PISTON_SET_DELAY, robot);
         fireSteps[2] = new ShooterPistonControlStep(false, robot);
         fireSteps[3] = new IdleWaitStep(PISTON_SET_DELAY, robot);
         fireSteps[4] = new SetConveyorStep(true, robot);
         fireSteps[5] = new IdleWaitStep(BALL_LOAD_DELAY, robot);
         fireSteps[6] = new SetConveyorStep(false, robot);
         
        AutonomousStep[] steps = new AutonomousStep[4];
         //Start the shooter
         steps[0] = shooterVoltageEstimateStep(shooterSpeed, 3, 3);
         //Fire the piston several times
        steps[2] = new ForLoopStep(new SequentialAutonomousStepGroup(fireSteps), numShots);
         //Stop the shooter
        steps[3] = new SetShooterSpeedStep(0, robot);
         
         return new SequentialAutonomousStepGroup(steps);
     }
     
     public AutonomousStep shooterVoltageEstimateStep(double targetVelocity, double estimate1Delay, double estimate2Delay){
         final double ITER_1_FRACTION = 0.75;
         AutonomousStep[] warmupSteps  = new AutonomousStep[4];
         warmupSteps[0] = new ShooterVoltagePWMStep(targetVelocity, robot);
         warmupSteps[1] = new IdleWaitStep(estimate1Delay, robot);
         warmupSteps[2] = new ShooterVoltagePWMStep(targetVelocity, robot);
         warmupSteps[3] = new IdleWaitStep(estimate2Delay,robot);
         return new SequentialAutonomousStepGroup(warmupSteps);
     }
 }
