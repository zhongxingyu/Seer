 package com.frc4343.robot2;
 
 import edu.wpi.first.wpilibj.Compressor;
 import edu.wpi.first.wpilibj.DriverStationLCD.Line;
 import edu.wpi.first.wpilibj.IterativeRobot;
 import edu.wpi.first.wpilibj.Joystick;
 import edu.wpi.first.wpilibj.RobotDrive;
 
 public class RobotTemplate extends IterativeRobot {
     Logger logger = new Logger();
     Joystick joystick = new Joystick(1);
     Joystick joystick2 = new Joystick(2);
     RobotDrive robotDrive = new RobotDrive(1, 2);
     Piston climbingPiston = new Piston((byte) 3, (byte) 4, true);
     Compressor compressor = new Compressor(1, 1);
     FiringSystem firingSystem = new FiringSystem(this);

     double axisCompensation = 0.5;

     // Button mappings
     final byte EXTEND_CLIMBING_PISTONS = 3;
     final byte RETRACT_CLIMBING_PISTONS = 2;
 
     private void resetRobot() {
         compressor.start();
         // Reset the climber piston to its initial position.
         climbingPiston.extend();
         // Initialize the firing system.
         firingSystem.switchMode();
     }
 
     public void teleopInit() {
         resetRobot();
     }
 
     public void autonomousInit() {
         resetRobot();
     }
 
     public void teleopPeriodic() {
         firingSystem.run();
 
         /*
          // This combines the axes in order to allow for both joysticks to control the robot's movement.
          // One of the joysticks will be made less sensitive to allow for precision control.
          double sumXAxes = joystick2.getAxis(Joystick.AxisType.kY) + (joystick.getAxis(Joystick.AxisType.kY) * 0.5);
          double sumYAxes = -joystick2.getAxis(Joystick.AxisType.kX) * axisCompensation + ((-joystick.getAxis(Joystick.AxisType.kX) * axisCompensation) * 0.4);
 
          // Floor the values of the combined joysticks in case they are above 1 or below -1.
          sumXAxes = sumXAxes > 1 ? 1 : sumXAxes;
          sumXAxes = sumXAxes < -1 ? -1 : sumXAxes;
          sumYAxes = sumYAxes > 1 ? 1 : sumYAxes;
          sumYAxes = sumYAxes < -1 ? -1 : sumYAxes;
 
          robotDrive.arcadeDrive(sumXAxes, sumYAxes);*/
 
         // The previous code *allegedly* did not allow for the y axis on the second joystick to function.
         // This new code will arcade drive twice (once for each joystick) to allow for precision control.
         robotDrive.arcadeDrive(joystick.getAxis(Joystick.AxisType.kX), joystick.getAxis(Joystick.AxisType.kY));
         robotDrive.arcadeDrive(joystick2.getAxis(Joystick.AxisType.kX) * axisCompensation, joystick2.getAxis(Joystick.AxisType.kY) * axisCompensation);
 
         climbingHandler();
 
         // Print the debug output the the DriverStation console.
         printConsoleOutput();
     }
 
     public void autonomousPeriodic() {
         firingSystem.run();
 
         // Print the debug output the the DriverStation console.
         printConsoleOutput();
     }
 
     private void climbingHandler() {
         if (joystick.getRawButton(EXTEND_CLIMBING_PISTONS)) {
             climbingPiston.extend();
         } else if (joystick.getRawButton(RETRACT_CLIMBING_PISTONS)) {
             climbingPiston.retract();
         }
     }
 
     private void printConsoleOutput() {
         // Clears driverStation text.
         logger.clearWindow();
         // Prints State of Frisbee
         logger.printLine(Line.kUser1, "Firing System State: " + firingSystem.getState());
         // Print the speed.
         logger.printLine(Line.kUser2, "Launcher Speed: " + (byte) (firingSystem.getLauncherSpeed() * 100) + "%");
         // Prints State of Launcher Motor
         logger.printLine(Line.kUser3, "Launcher Motor: " + (firingSystem.getLauncherMotorState() ? "ON" : "OFF"));
         // Prints State of Launcher Motor
         logger.printLine(Line.kUser4, "Indexer Motor: " + (firingSystem.getIndexerMotorState() ? "ON" : "OFF"));
         // Print the tank pressurization state.
         logger.printLine(Line.kUser5, "Tanks Full: " + (compressor.getPressureSwitchValue() ? "YES" : "NO"));
         // Updates the output window.
         logger.updateLCD();
     }
 
     public Joystick getJoystick(int joystickNumber) {
         if (joystickNumber == 1) {
             return joystick;
         } else if (joystickNumber == 2) {
             return joystick2;
         } else {
             return null;
         }
     }
 }
