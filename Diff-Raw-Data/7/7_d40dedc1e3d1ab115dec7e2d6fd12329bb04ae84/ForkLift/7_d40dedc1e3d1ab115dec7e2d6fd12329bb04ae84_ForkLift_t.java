 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package edu.wpi.first.wpilibj.templates.Components;
 
 import edu.wpi.first.wpilibj.templates.Components.XboxGamepad;
 import edu.wpi.first.wpilibj.Encoder;
 import edu.wpi.first.wpilibj.Jaguar;
 import edu.wpi.first.wpilibj.templates.Components.XboxGamepad.Button;
 import edu.wpi.first.wpilibj.templates.Components.XboxGamepad.Stick;
 import edu.wpi.first.wpilibj.templates.Controls;
 
 /**
  *
  * @author Dennis Shen
  */
 public class ForkLift {
     public Jaguar ForkLiftMotor1;
     public Jaguar ForkLiftMotor2;
     public Encoder ForkLiftEncoder;
     double LowSettingEncodervalue;//Arbitrary encoder value for the Low Height 
     double MidSettingEncodervalue;//Arbitrary encoder value for the Mid Height
     double HighSettingEncodervalue;//Arbitrary encoder value for the High Height
 
     
     private int controllerType;
     private Stick stick;
     private Button buttonUp;
     private Button buttonDown;
     
     
     /*random channel for the motor. Change to whatever is configured
         * random encoder for the forklift
         */
     public ForkLift(Encoder NewEncoder)
     {
         ForkLiftMotor1 = new Jaguar(5);
         ForkLiftMotor2 = new Jaguar(6);
         ForkLiftEncoder = NewEncoder;
         ForkLiftEncoder.start();
         ForkLiftEncoder.reset();
 
     }
 
     public ForkLift()
     {
         ForkLiftMotor1 = new Jaguar(5);
         ForkLiftMotor2 = new Jaguar(6);
     }
     /*
      * The method for the motor to lift the drawerslide to the low height setting
      * starts the motor at an arbitrary speed, when the encoder hits a test value,
      * the motor stops, and the encoder resets.
      * The same method is used for all other "Drive"
      */
     public void LowDrive()
     {
         if(ForkLiftEncoder.get()-LowSettingEncodervalue>3)
         {
             while(ForkLiftEncoder.get()-LowSettingEncodervalue>3)
             {
                 ForkLiftMotor1.set(-.75);
                 ForkLiftMotor2.set(-.75);//speed is arbitrary - can change if needed test value
             }
         }
         else if(ForkLiftEncoder.get()-LowSettingEncodervalue<-3)
         {
             while(ForkLiftEncoder.get()-LowSettingEncodervalue<-3)
             {
                 ForkLiftMotor1.set(.75);
                 ForkLiftMotor2.set(.75);//speed is arbitrary - can change if needed test value
             }
         }
         ForkLiftMotor1.set(0);
         ForkLiftMotor2.set(0);
     }
     public void MidDrive()
     {
         if(ForkLiftEncoder.get()-MidSettingEncodervalue>3)
         {
             while(ForkLiftEncoder.get()-MidSettingEncodervalue>3)
             {
                 ForkLiftMotor1.set(-.75);
                 ForkLiftMotor2.set(-.75);//speed is arbitrary - can change if needed test value
             }
         }
         else if(ForkLiftEncoder.get()-MidSettingEncodervalue<-3)
         {
             while(ForkLiftEncoder.get()-MidSettingEncodervalue<-3)
             {
                 ForkLiftMotor1.set(.75);
                 ForkLiftMotor2.set(.75);//speed is arbitrary - can change if needed test value
             }
         }
         ForkLiftMotor1.set(0);
         ForkLiftMotor2.set(0);
     }
     public void HighDrive()
     {
         if(ForkLiftEncoder.get()-HighSettingEncodervalue>3)
         {
             while(ForkLiftEncoder.get()-HighSettingEncodervalue>3)
             {
                 ForkLiftMotor1.set(-.75);
                 ForkLiftMotor2.set(-.75);//speed is arbitrary - can change if needed test value
             }
         }
         else if(ForkLiftEncoder.get()-HighSettingEncodervalue<-3)
         {
             while(ForkLiftEncoder.get()-HighSettingEncodervalue<-3)
             {
                 ForkLiftMotor1.set(.75);
                 ForkLiftMotor2.set(.75);//speed is arbitrary - can change if needed test value
             }
         }
         ForkLiftMotor1.set(0);
         ForkLiftMotor2.set(0);
     }
 
     public void update()
     {
         if(controllerType == Controls.STICK){
             double speed = stick.getStickY() * Math.abs(stick.getStickY());
 
             ForkLiftMotor1.set(speed);
             ForkLiftMotor2.set(speed * -1);
         }
         else if(controllerType == Controls.BUTTON){
 
             if(buttonUp.isPressed()){
                 ForkLiftMotor1.set(1);
                 ForkLiftMotor2.set(-1);
             }

             else if(buttonDown.isPressed()){
                 ForkLiftMotor1.set(-1);
                 ForkLiftMotor2.set(1);
             }
 
            else{
                ForkLiftMotor1.set(0);
                ForkLiftMotor2.set(0);
            }

         }
     }
 
     public void setController(XboxGamepad.Stick st){
         stick = st;
         controllerType = Controls.STICK;
     }
 
     public void setController(XboxGamepad.Button up, XboxGamepad.Button down){
         buttonUp = up;
         buttonDown = down;
         controllerType = Controls.BUTTON;
     }
 }
