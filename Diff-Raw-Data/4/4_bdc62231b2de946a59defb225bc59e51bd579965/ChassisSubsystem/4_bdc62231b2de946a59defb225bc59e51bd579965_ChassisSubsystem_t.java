 package robot.subsystems;
 
 import edu.wpi.first.wpilibj.Encoder;
 import edu.wpi.first.wpilibj.RobotDrive;
 import edu.wpi.first.wpilibj.Victor;
 import edu.wpi.first.wpilibj.command.Subsystem;
 import robot.Constants;
 import robot.EncoderAverager;
 import robot.OutputStorage;
 import robot.Pneumatic;
 import robot.commands.CommandBase;
 import robot.commands.TeleopDriveCommand;
 import robot.parsable.ParsablePIDController;
 
 public class ChassisSubsystem extends Subsystem {
 
     public static final double INCHES_PER_ENCODER_COUNT = 92.5 / 1242.5385;
     public static final double PID_DRIVE_PERCENT_TOLERANCE = 10.0;
     public static final double PID_GYRO_ABSOLUTE_TOLERANCE = 2.0;
     public static final double PID_COUNT_ABSOLUTE_TOLERANCE = 10.0;
     Victor vicLeft = new Victor(Constants.LEFT_MOTOR_CHANNEL);
     Victor vicRight = new Victor(Constants.RIGHT_MOTOR_CHANNEL);
     Encoder encLeft = new Encoder(Constants.ENC_LEFT_ONE, Constants.ENC_LEFT_TWO, true);
     Encoder encRight = new Encoder(Constants.ENC_RIGHT_ONE, Constants.ENC_RIGHT_TWO, true);
     EncoderAverager encAverager = new EncoderAverager(encLeft, true, encRight, false, true); //Left is negative, use counts
     public Pneumatic shifterPneumatic; //Pneumatics are initialized in CommandBase.java
     OutputStorage leftOutputStorage = new OutputStorage();
     OutputStorage rightOutputStorage = new OutputStorage();
     RobotDrive robotDrive = new RobotDrive(leftOutputStorage, rightOutputStorage);
     ParsablePIDController pidLeft = new ParsablePIDController("pidleft", 0.001, 0.0005, 0.0, encLeft, vicLeft);
     ParsablePIDController pidRight = new ParsablePIDController("pidright", 0.001, 0.0005, 0.0, encRight, vicRight);
     public ParsablePIDController pidGyro = new ParsablePIDController("pidgyro", 0.08, 0.0, 0.0, CommandBase.positioningSubsystem.positionGyro, new OutputStorage());
     public ParsablePIDController pidCount = new ParsablePIDController("pidcount", 0.02, 0.0, 0.0, encAverager, new OutputStorage());
 
     public ChassisSubsystem() {
         encLeft.setPIDSourceParameter(Encoder.PIDSourceParameter.kRate);
         encRight.setPIDSourceParameter(Encoder.PIDSourceParameter.kRate);
 
         encLeft.start();
         encRight.start();
 
         pidLeft.setInputRange(-Constants.CHASSIS_MAX_HIGH_ENCODER_RATE.get(), Constants.CHASSIS_MAX_HIGH_ENCODER_RATE.get());
         pidRight.setInputRange(-Constants.CHASSIS_MAX_HIGH_ENCODER_RATE.get(), Constants.CHASSIS_MAX_HIGH_ENCODER_RATE.get());
        pidGyro.setInputRange(-Double.MAX_VALUE, Double.MAX_VALUE);
        pidCount.setInputRange(-Double.MAX_VALUE, Double.MAX_VALUE);
 
         pidLeft.setOutputRange(-1.0, 1.0);
         pidRight.setOutputRange(-1.0, 1.0);
         pidGyro.setOutputRange(-1.0, 1.0);
         pidCount.setOutputRange(-0.95, 0.95);
 
         pidLeft.setPercentTolerance(PID_DRIVE_PERCENT_TOLERANCE);
         pidRight.setPercentTolerance(PID_DRIVE_PERCENT_TOLERANCE);
         pidGyro.setAbsoluteTolerance(PID_GYRO_ABSOLUTE_TOLERANCE);
         pidCount.setAbsoluteTolerance(PID_COUNT_ABSOLUTE_TOLERANCE);
     }
 
     public void initDefaultCommand() {
         setDefaultCommand(new TeleopDriveCommand());
     }
 
     public void disable() {
         disablePID();
     }
 
     public void enable() {
         encLeft.reset();
         encRight.reset();
         enablePID();
     }
 
     public boolean isEnabledPID() {
         return pidLeft.isEnable() && pidRight.isEnable();
     }
 
     public boolean isEnabledPIDGyro() {
         return pidGyro.isEnable();
     }
 
     public void disablePID() {
         if (pidLeft.isEnable() || pidRight.isEnable()) {
             pidLeft.disable();
             pidRight.disable();
         }
     }
 
     public void enablePID() {
         if (!pidLeft.isEnable() || !pidRight.isEnable()) {
             pidLeft.enable();
             pidRight.enable();
         }
     }
 
     public void disablePIDGyro() {
         if (pidGyro.isEnable()) {
             pidGyro.disable();
         }
     }
 
     public void enablePIDGyro() {
         if (!pidGyro.isEnable()) {
             pidGyro.enable();
         }
     }
 
     public void disablePIDCount() {
         if (pidCount.isEnable()) {
             pidCount.disable();
         }
     }
 
     public void enablePIDCount() {
         if (!pidCount.isEnable()) {
             pidCount.enable();
         }
     }
 
     public void drive(double speed, double rotation) {
         robotDrive.arcadeDrive(speed, -rotation);
         if (isEnabledPID()) {
             if (getHighGear()) {
                 //High gear
                 pidLeft.setSetpoint(leftOutputStorage.get() * Constants.CHASSIS_MAX_HIGH_ENCODER_RATE.get());
                 pidRight.setSetpoint(rightOutputStorage.get() * Constants.CHASSIS_MAX_HIGH_ENCODER_RATE.get());
             } else {
                 //Low gear
                 pidLeft.setSetpoint(leftOutputStorage.get() * Constants.CHASSIS_MAX_LOW_ENCODER_RATE.get());
                 pidRight.setSetpoint(rightOutputStorage.get() * Constants.CHASSIS_MAX_LOW_ENCODER_RATE.get());
             }
         } else {
             vicLeft.set(leftOutputStorage.get());
             vicRight.set(rightOutputStorage.get());
         }
     }
 
     public void pidGyroRelativeSetpoint(double relativeAngle) {
         pidGyro.setSetpoint(CommandBase.positioningSubsystem.positionGyro.getAngle() + relativeAngle);
     }
     
     public void pidGyroAbsoluteSetpoint(double absAngle) {
         pidGyro.setSetpoint(absAngle);
     }
 
     public void pidCountRelativeSetpoint(double relativeCounts) {
         pidCount.setSetpoint(encAverager.get() + relativeCounts);
     }
 
     public boolean pidGyroOnTarget() {
         return pidGyro.onTarget();
     }
 
     public boolean pidCountOnTarget() {
         return pidCount.onTarget();
     }
 
     public void shift(boolean value) {
         shifterPneumatic.set(value);
     }
 
     public boolean getHighGear() {
         return shifterPneumatic.get();
     }
 
     public double getAverageRate() {
         return encAverager.getRate();
     }
 
     public int getAverageDistance() {
         //Right counts - left counts because left counts are negative
         return encAverager.get(); //Average rate
     }
 
     public boolean isMoving() {
         //If we are above the threshold then we are moving
         return Math.abs(getAverageRate()) > Constants.CHASSIS_ENCODER_MOVEMENT_THRESHOLD.get();
     }
 
     public void resetEncoders() {
         encLeft.reset();
         encRight.reset();
     }
 
     public void print() {
         System.out.println("[" + this.getName() + "]");
         System.out.println("encLeftRate: " + encLeft.getRate() + " encRightRate: " + encRight.getRate());
         System.out.println("encLeft: " + encLeft.get() + " encRight: " + encRight.get());
         System.out.println("averageEncoderRate: " + getAverageRate());
         System.out.println("averageEncoderDistance: " + getAverageDistance());
         System.out.println("PIDEnabled: " + isEnabledPID());
         System.out.println("LeftOutputStorage: " + leftOutputStorage.get() + " RightOutputStorage: " + rightOutputStorage.get());
         System.out.println("PIDLeft output: " + pidLeft.get() + " PIDRight output: " + pidRight.get());
         System.out.println("PIDLeft setpoint: " + pidLeft.getSetpoint() + " PIDRight setpoint: " + pidRight.getSetpoint());
         System.out.println("PIDGyro setpoint: " + pidGyro.getSetpoint() + " output: " + pidGyro.get());
         System.out.println("PIDCount setpoint: " + pidCount.getSetpoint() + " output: " + pidCount.get());
     }
 }
