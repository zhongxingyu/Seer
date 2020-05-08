 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edu.wpi.first.wpilibj.templates;
 import edu.wpi.first.wpilibj.*;
 import edu.wpi.first.wpilibj.DriverStationLCD.Line;
 import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
 
 /**
  *
  * @author Philip2
  */
 public class Shooter {
 
 
    
      /**************
     * Shooter code
     * *************/
    Team3373 team;
    
     public Shooter(Team3373 t){
        team = t;
    }
    
    public double RPMtarget(double a){ //defines target based on input. Appeaers to be better than speed increase. can probbaly be used in place of a bunch of code.
       if (team.shootA){
            team.target = ((team.RPMModifier * team.ShooterSpeedScale) + team.currentRPMT2) * a;
        } else if (team.shootB){
            team.target = (( -team.RPMModifier * team.ShooterSpeedScale) + team.currentRPMT2) * a;
        }
    return team.target;
    }
     
     public void shootInit() { //to be called at beginning of teleoperated
        team.LCD = DriverStationLCD.getInstance();
        team.LCD.updateLCD();
        team.target = team.ShooterSpeedMax;
    }
        
        /******************
         * Initialization *
         * ****************/
  public void Start(){ //Initialization code, used to turn motors on and off
        if (team.shootStart){
            team.StageTwoTalon.set(team.idle);
            team.StageOneTalon.set(team.idle * team.stageOneScaler);
            team.LCD.println(Line.kUser1, 7, "On");
            team.LCD.updateLCD();
        } else if (team.shootBack){
            team.StageTwoTalon.set(team.off);
            team.StageOneTalon.set(team.off * team.stageOneScaler);
            team.LCD.println(Line.kUser1, 7, "Off");
            team.LCD.updateLCD();
        }
  }
        /*********************
         * Increase/Decrease *
         * *******************/
 public void speedIncrease(){ //increases speed by amount/second designated. Needs the per second part
        
           team.StageTwoTalon.set(RPMtarget(1));
           team.StageOneTalon.set(RPMtarget(team.stageOneScaler));
            team.LCD.println(Line.kUser2, 1, "Adding " + team.target + "RPM");
            team.LCD.updateLCD();
  }
        
  public void speedDecrease() { //decrease speed by set number. Works like speedIncrease() in reverse
            //This code is used to subtrack the current speed of Stage 2
            team.StageTwoTalon.set(team.target);
            team.StageOneTalon.set(team.target *.5);       
           team.LCD.println(Line.kUser2, 1, "Removing " + team.RPMModifier + "RPM.");
            team.LCD.updateLCD();
            if (team.StageTwoTalon.get() <= 0){
                team.StageTwoTalon.set(team.off);
                //if the speed is less than 0, turn off
            }
        }
 
 public void percentageAdd() { //adds 5% to the scaler of stage one       
            team.stageOneScaler += 0.05;
            //changes stage1 percentage of stage2 adds 5%
            team.LCD.println(Line.kUser6, 1, "Adding 5% to Stage One Percentile");
            team.LCD.updateLCD();
        } 
 
 public void percentageSubtract() {//reduces percentage, subtracts 5%. i.e. 45% - 40%
            team.stageOneScaler -= 0.05;
            //changes stage1 percentage of stage2 subtracts 5%
            team.LCD.println(Line.kUser6, 1, "Subtracting 5% to Stage One Percentile");
            team.LCD.updateLCD();
        }
        
        
 public void shooterPrint() { // prints different variables. NYI
         if (RPMtarget(1) > team.currentRPMT2) {
            team.LCD.println(Line.kUser5, 1, "Accelerating");
            team.LCD.updateLCD();
        } else if (RPMtarget(1) < team.currentRPMT2) {
            team.LCD.println(Line.kUser5, 1, "Decelerating");
            team.LCD.updateLCD();
        }
        team.LCD.println(Line.kUser1, 1, "Motors ");
        team.LCD.updateLCD();
        team.LCD.println(Line.kUser3, 1, "Stage One Speed Percentile: " + (team.currentRPMT1/team.currentRPMT2)*100 + "%");
        team.LCD.updateLCD();
        team.LCD.println(Line.kUser4, 1, "Target Speed: " + (RPMtarget(1)) + "RPM");
        team.LCD.updateLCD();        
     }
 }
