 /*
  * FRC Team 53:  The Alien Cow Abductors
  * 2012 FRC Competition "Rebound Rumble"
  * Released under GNU GPL v. 3 or later
  */
 package edu.wpi.first.wpilibj.templates;
 
 import edu.wpi.first.wpilibj.*;
 import edu.wpi.first.wpilibj.camera.AxisCamera;
 import edu.wpi.first.wpilibj.image.ColorImage;
 import edu.wpi.first.wpilibj.image.ParticleAnalysisReport;
 
 /**
  *
  * @author Team53
  */
 public class Robo2012 extends IterativeRobot {
 
     RobotDrive drive;
     Joystick stick;
     AxisCamera camera;
     ImageProcessing imageProc;
     ParticleAnalysisReport target;
     Physics physics;
     Launcher launcher;
     Jaguar bridgeArm;
     Jaguar launchTurn;
     Jaguar collectMotor;
     AnalogChannel autoPot;
     AnalogChannel telePot;
     GyroX gyro;
     Messager msg;
     Controls controls;
     Ultrasonic ultrasonic;
     
     boolean isShooting = false;
     int shots = 0;
     double distanceFromTarget;
 
     public void robotInit() {
         Timer.delay(10);
         //left front, left back, right front, right back
         drive = new RobotDrive(
                 RoboMap.MOTOR1, RoboMap.MOTOR2, RoboMap.MOTOR3, RoboMap.MOTOR4);
 
         stick = new Joystick(RoboMap.JOYSTICK1);
         controls = new Controls(stick);
         msg = new Messager();
         camera = AxisCamera.getInstance();
         camera.writeBrightness(30);
         camera.writeResolution(AxisCamera.ResolutionT.k640x480);
         imageProc = new ImageProcessing();
         physics = new Physics();
         bridgeArm = new Jaguar(RoboMap.BRIDGE_MOTOR);
         launchTurn = new Jaguar(RoboMap.LAUNCH_TURN);
         collectMotor = new Jaguar(RoboMap.COLLECT_MOTOR);
         launcher = new Launcher();
         gyro = new GyroX(RoboMap.GYRO, drive);
         autoPot = new AnalogChannel(RoboMap.AUTO_POT);
         telePot = new AnalogChannel(RoboMap.TELO_POT);
         ultrasonic = new Ultrasonic(
                 RoboMap.ULTRASONIC_PING, RoboMap.ULTRASONIC_ECHO);
         ultrasonic.setDistanceUnits(Ultrasonic.Unit.kInches);
         ultrasonic.setEnabled(true);
         msg.printLn("FRC 2012");
     }
 
     public void autonomousInit() {
         isShooting = true;
     }
 
     public void autonomousPeriodic() {
         if (camera.freshImage()) {
             try {
                 ParticleAnalysisReport[] parts = imageProc.getTheParticles(camera);
                 ParticleAnalysisReport topTarget = ImageProcessing.getTopMost(parts);
                 
                 msg.printLn("Pixels = " + topTarget.boundingRectHeight);                
                 ColorImage img = camera.getImage();
                 double p = (img.getWidth()/2) - topTarget.center_mass_y;
                 double angle = p/Physics.LAMBDA;
                 gyro.turnAngle(angle);
                 if(isShooting){
                     Timer.delay(3);
                     
                     launcher.shoot(topTarget.boundingRectHeight, 10);
                     //load and shoot again
                     shots++;
                     if (shots == 2) {
                         isShooting = false;
                     }
                 }
 
             } catch (Exception e) {
                 e.printStackTrace();
                 msg.printLn("ERROR!!! Cannot Fetch Image");
             }
         }
     }
 
     public void teleopPeriodic() {
 
         drive.mecanumDrive_Cartesian(
                 stick.getX(), stick.getY(), MathX.pow(stick.getZ(), 3), 0);
         
         gyro.refreshGyro();
         
         distanceFromTarget = ultrasonic.pidGet();        
         
         if (controls.button2()) {
             gyro.turnToAngle(0);
         }
         
         //motor to collect the balls off the ground
         collectMotor.set(.5);
         
         //motor to control lazy susan for launcher
         if (controls.button10()) {
             launchTurn.set(.25);
         } else if (controls.button11()) {
             launchTurn.set(-.25);
         } else {
             launchTurn.set(0);
        }
        
         // motor to lower bridge arm
         if (controls.button6()) {
             bridgeArm.set(.75);
        } else if (controls.button7()) {
             bridgeArm.set(-.5);
         } else {
             bridgeArm.set(0);
         }
 
         //Select the target to aim at 
         if (controls.FOV_Left()) {
             target = imageProc.leftT;
             isShooting = true;
         } else if (controls.FOV_Right()) {
             target = imageProc.rightT;
             isShooting = true;
         } else if (controls.FOV_Top()) {
             target = imageProc.topT;
             isShooting = true;
         } else if (controls.FOV_Bottom()) {
             target = imageProc.bottomT;
             isShooting = true;
         }
 
         // Have the camera scan for targets
         if (camera.freshImage()) {
             try {
                 imageProc.getTheParticles(camera);
                 //int pixelHeight = imageProc.getImgHeight(imageProc.party);
                 // msg.printLn("Pixels = " + pixelHeight);
                 msg.printLn(imageProc.orginizeParticles(imageProc.party, imageProc.getTotalXCenter(imageProc.party), imageProc.getTotalXCenter(imageProc.party)));
 
                 // if a target is selected, aim and shoot the ball
                 if (isShooting) {
                     // launcher.shoot(pixelHeight);
                     /*
                      * physics.setP(imageProc.getImgHeight(imageProc.party));
                      * physics.calculateInfo(); double velocity =
                      * physics.calculateLaunchVelocity(); msg.printLn("" +
                      * velocity); physics.pushInfoToDashboard(); msg.printLn(""
                      * + imageProc.getTotalXCenter(imageProc.party));
                      * msg.printLn("" +
                      * imageProc.getTotalYCenter(imageProc.party));
                      */
                     isShooting = false;
                 }
             } catch (Exception e) {
                 e.printStackTrace();
                 msg.printLn("ERROR!!! Cannot Fetch Image");
             }
         }
     }
 }
