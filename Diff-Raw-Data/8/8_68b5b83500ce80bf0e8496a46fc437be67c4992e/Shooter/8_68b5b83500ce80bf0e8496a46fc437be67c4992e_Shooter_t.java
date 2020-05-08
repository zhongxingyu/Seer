 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edu.wpi.first.wpilibj.templates;
 
 import edu.wpi.first.wpilibj.AnalogChannel;
 import edu.wpi.first.wpilibj.DigitalInput;
 import edu.wpi.first.wpilibj.DriverStationLCD;
 import edu.wpi.first.wpilibj.DriverStationLCD.Line;
 import edu.wpi.first.wpilibj.Relay;
 import edu.wpi.first.wpilibj.Talon;
 /**
  *
  * @author RoboHawks
  */
 public class Shooter {
        Relay shootSpike = new Relay(3);
         Talon stageOneTalon = new Talon(5); //Shooter Talons, spin the wheels
         Talon stageTwoTalon = new Talon(6); //Shooter Talons, spin the wheels
         DigitalInput shootLimit = new DigitalInput(8);
        
         double percentageScaler = 0.5;
         double stageTwoVoltageOut =  0.0;
         double stageOneVoltageOut =  0.0;
        
         int elevatorStatus = 0;
         boolean limit = shootLimit.get();            
 
     public void start(){
         percentageScaler = 0.5;
         stageTwoVoltageOut =  0.1;
         stageOneVoltageOut =  stageTwoVoltageOut * percentageScaler;
         stageOneTalon.set(stageOneVoltageOut);
         stageTwoTalon.set(stageTwoVoltageOut);
     }
     public void increaseSpeed(){ //increases stage 2 by 1/10 of possible speed
         stageTwoVoltageOut += 0.1;
         if (stageTwoVoltageOut >= 1){ stageTwoVoltageOut = 1;}
         stageOneVoltageOut = stageTwoVoltageOut * percentageScaler;
         stageOneTalon.set(stageOneVoltageOut);
         stageTwoTalon.set(stageTwoVoltageOut);
     }
     public void decreaseSpeed(){//decreases stage 2 by 1/10 of possible speed
         stageTwoVoltageOut -=  0.1;
         if (stageTwoVoltageOut <= 0){ stageTwoVoltageOut = 0;}
         stageOneVoltageOut =  stageTwoVoltageOut * percentageScaler;
         stageOneTalon.set(stageOneVoltageOut);
         stageTwoTalon.set(stageTwoVoltageOut);
     }
     public void increasePercentage(){//increases percentage of what stage 2 is multiplyied by to get 1
         percentageScaler += 0.05;
         if (percentageScaler >= 1){percentageScaler = 1;}
         stageOneVoltageOut =  stageTwoVoltageOut * percentageScaler;
         stageOneTalon.set(stageOneVoltageOut);
         stageTwoTalon.set(stageTwoVoltageOut);
     }
     public void decreasePercentage(){//decreases percentage of what stage 2 is multiplyied by to get 1
         percentageScaler -= 0.05;
         if (percentageScaler <= 0){percentageScaler = 0;}
         stageOneVoltageOut = stageTwoVoltageOut * percentageScaler;
         stageOneTalon.set(stageOneVoltageOut);
         stageTwoTalon.set(stageTwoVoltageOut);
     }
     public void stop(){
         stageOneVoltageOut = 0.0;
         percentageScaler = 0.5;
         stageTwoVoltageOut = stageOneVoltageOut * percentageScaler;
         
         stageOneTalon.set(stageOneVoltageOut);
         stageTwoTalon.set(stageTwoVoltageOut);
     }
     public void printLCD(DriverStationLCD LCD){
         double Scaler = 5936; //converts voltage to RPM for display purposes only
         double speedOne = stageOneTalon.get();
         String speed1 = Double.toString(speedOne);
         double speedTwo = stageTwoTalon.get();
         String speed2 = Double.toString(speedTwo);
         LCD.println(Line.kUser3, 1, ((stageOneTalon.get()/stageTwoTalon.get()) *100) + "                       %");
         LCD.println(Line.kUser4, 1,"S1:" + speed1);
         LCD.println(Line.kUser2, 1,"S2:" + speed2);
         LCD.println(Line.kUser1, 1, "RPM1: " + (speedOne * Scaler));
         LCD.println(Line.kUser2, 1, "RPM2: " + (speedTwo * Scaler));
         LCD.updateLCD();
     }
     public void shoot(){
         final Thread thread = new Thread(new Runnable() {
         public void run(){
             limit = shootLimit.get();
            System.out.println(limit);
            while(limit){
                stageOneTalon.set(0.5);
                 limit = shootLimit.get();
             }
              shootSpike.set(Relay.Value.kOff); 
             } 
         });
             thread.start();
     }
     public void loadFrisbee(){
         final Thread thread = new Thread(new Runnable() {
         public void run(){
             shootSpike.set(Relay.Value.kReverse);
                try{
                Thread.sleep(500);
                }
                catch(InterruptedException e){}
             shootSpike.set(Relay.Value.kOff);   
             } 
         });
             thread.start();
     }
 }
