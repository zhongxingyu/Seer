 package org.crescentschool.robotics.competition.subsystems;
 
 import edu.wpi.first.wpilibj.CANJaguar;
 import edu.wpi.first.wpilibj.Gyro;
 import edu.wpi.first.wpilibj.Victor;
 import edu.wpi.first.wpilibj.can.CANTimeoutException;
 import edu.wpi.first.wpilibj.command.Subsystem;
 import org.crescentschool.robotics.competition.PID.PIDController;
 import org.crescentschool.robotics.competition.commands.KajDrive;
 import org.crescentschool.robotics.competition.constants.ElectricalConstants;
 
 /**
  *
  */
 public class DriveTrain extends Subsystem {
     Gyro gyro;
     private static DriveTrain instance = null;
     CANJaguar jagRightMaster;
     Victor victorRightSlaveMid;
     Victor victorRightSlaveBack;
     PIDController rightPIDControl;
     CANJaguar jagLeftMaster;
     Victor victorLeftSlaveMid;
     Victor victorLeftSlaveBack;
     PIDController leftPIDControl;
     int driveMode = 1;
     private boolean canError = false;
 
     public void initDefaultCommand() {
 
         // Set the default command for a subsystem here.
         setDefaultCommand(new KajDrive());
 
     }
 
     public static DriveTrain getInstance() {
         if (instance == null) {
             instance = new DriveTrain();
         }
         return instance;
     }
 
     DriveTrain() {
         try {
             gyro = new Gyro(1);
             
             victorRightSlaveMid = new Victor(ElectricalConstants.victorRightSlaveFront);
             victorRightSlaveBack = new Victor(ElectricalConstants.victorRightSlaveBack);
             victorLeftSlaveMid = new Victor(ElectricalConstants.victorLeftSlaveFront);
             victorLeftSlaveBack = new Victor(ElectricalConstants.victorLeftSlaveBack);
             System.out.println("left");
             jagLeftMaster = new CANJaguar(ElectricalConstants.jagLeftMaster);
             System.out.println("right");
             jagRightMaster = new CANJaguar(ElectricalConstants.jagRightMaster);
             System.out.println("done");
             jagRightMaster.configNeutralMode(CANJaguar.NeutralMode.kCoast);
             jagLeftMaster.configNeutralMode(CANJaguar.NeutralMode.kCoast);
 
         } catch (CANTimeoutException ex) {
             canError = true;
             handleCANError();
             ex.printStackTrace();
         }
     }
     public Gyro getGyro() {
         return gyro;
     }
 
     void initVBus() {
         try {
             driveMode = 1;
             jagRightMaster.changeControlMode(CANJaguar.ControlMode.kPercentVbus);
             jagLeftMaster.changeControlMode(CANJaguar.ControlMode.kPercentVbus);
             jagRightMaster.enableControl();
             jagLeftMaster.enableControl();
         } catch (CANTimeoutException ex) {
             canError = true;
             handleCANError();
             ex.printStackTrace();
         }
     }
     void initPosition() {
         try {
             driveMode = 2;
             jagRightMaster.changeControlMode(CANJaguar.ControlMode.kSpeed);
             jagLeftMaster.changeControlMode(CANJaguar.ControlMode.kSpeed);
             jagRightMaster.setPositionReference(CANJaguar.PositionReference.kQuadEncoder);
             jagLeftMaster.setPositionReference(CANJaguar.PositionReference.kQuadEncoder);
             jagRightMaster.configEncoderCodesPerRev(256);
             jagLeftMaster.configEncoderCodesPerRev(256);
             jagRightMaster.changeControlMode(CANJaguar.ControlMode.kVoltage);
             jagLeftMaster.changeControlMode(CANJaguar.ControlMode.kVoltage);
             jagRightMaster.enableControl(0);
             jagLeftMaster.enableControl(0);
         } catch (CANTimeoutException ex) {
             canError = true;
             handleCANError();
             ex.printStackTrace();
         }
     }
 
     void syncSlaves() {
         try {
             victorRightSlaveMid.set(jagRightMaster.getOutputVoltage() / jagRightMaster.getBusVoltage());
             victorRightSlaveBack.set(jagRightMaster.getOutputVoltage() / jagRightMaster.getBusVoltage());
             victorLeftSlaveMid.set(jagLeftMaster.getOutputVoltage() / jagLeftMaster.getBusVoltage());
             victorLeftSlaveBack.set(jagLeftMaster.getOutputVoltage() / jagLeftMaster.getBusVoltage());
         } catch (CANTimeoutException ex) {
             canError = true;
             handleCANError();
             ex.printStackTrace();
         }
     }
 
     public void setLeftVBus(double power) {
         if (driveMode != 1) {
             initVBus();
         }
         try {
             jagLeftMaster.setX(-power);
         } catch (CANTimeoutException e) {
             canError = true;
             handleCANError();
             e.printStackTrace();
         }
         syncSlaves();
     }
 
     public void setRightVBus(double power) {
         if (driveMode != 1) {
             initVBus();
         }
         try {
             jagRightMaster.setX(power);
         } catch (CANTimeoutException e) {
             canError = true;
             handleCANError();
             e.printStackTrace();
         }
         syncSlaves();
     }
     public void setAngle(double angle){
       gyro.reset();
      setRightVBus(angle*-(12/180.0));
      setLeftVBus(angle*(12/180.0));
     }
     public void handleCANError() {
         if (canError) {
             System.out.println("CAN Error!");
             try {
                 Thread.sleep(500);
             } catch (InterruptedException ex) {
                 ex.printStackTrace();
             }
             canError = false;
             switch (driveMode) {
                 case 1:
                     initVBus();
                     break;
                 case 2:
                     initPosition();
                     break;
             }
         }
     }
 }
