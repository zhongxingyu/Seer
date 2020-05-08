 package edu.wpi.first.wpilibj.templates.subsystems;
 
 import edu.wpi.first.wpilibj.command.PIDSubsystem;
 import edu.wpi.first.wpilibj.Encoder;
 import edu.wpi.first.wpilibj.templates.RobotMap;
 import edu.wpi.first.wpilibj.templates.commands.Aim;
 import edu.wpi.first.wpilibj.templates.commands.ManYaw;
 import edu.wpi.first.wpilibj.CANJaguar;
<<<<<<< HEAD
 import edu.wpi.first.wpilibj.DigitalInput;
=======
 import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
 
>>>>>>> c18ac081f463d139af8336a377b78ae6cfe8dd96
 /**
  * A Subsystem extending PIDSubystem that contains methods for controlling the ShooterYaw
  * @author Gillie, Lauren, and Emma
  */
 public class ShooterYaw extends PIDSubsystem {
     // Put methods for controlling this subsystem
     // here. Call these from Commands.
 
     public static final int MaxAngle = 90;
     public static final int MinAngle = -90;
     private Encoder encoder = new Encoder(RobotMap.yawEncoderA, RobotMap.yawEncoderB);
     private CANJaguar yawMotor;
 
     
     private final DigitalInput AutoYawSwitch = new DigitalInput(RobotMap.autoYawSwitch);
 
 
     public ShooterYaw() {
         super(0.27, 0.0, 0.0);
         setSetpointRange(MaxAngle, MinAngle);
         setSetpoint(0);
         //positive is couterclockwise as seen from above
         encoder.setDistancePerPulse(0.0833750);
         encoder.start();
         enable();
 
         try {
             yawMotor = new CANJaguar(RobotMap.yawMotor);
         } catch (Exception e) {
             System.out.println(e);
         }
     }
 
     public void setSetpoint(double setpoint) {
         if (setpoint > MaxAngle) {
             setpoint = MaxAngle;
         } else if (setpoint < MinAngle) {
             setpoint = MinAngle;
         }
         getPIDController().setSetpoint(setpoint);
     }
 
     public void initDefaultCommand() {
         // Set the default command for a subsystem here.
         //setDefaultCommand(new MySpecialCommand());
         // setDefaultCommand(new Aim(1));
     }
 
     protected double returnPIDInput() {
         SmartDashboard.putDouble("YawSetpoint", getSetpoint());
         SmartDashboard.putDouble("encoderAngle", encoder.getDistance());
 
         
             return -encoder.getDistance();
             //for sensor
         
     }
 
     protected void usePIDOutput(double output) {
         //for motor
         try {
             yawMotor.setX(output);
         } catch (Exception e) {
         }
     }
 
     public boolean atSetpoint() {
         return Math.abs(getPosition() - getSetpoint()) < 5;
     }
     
     public boolean getSwitch(){
         if(AutoYawSwitch.get() == true){
             return true;
         }else{
             return false;  
         }
             
     }
 }
