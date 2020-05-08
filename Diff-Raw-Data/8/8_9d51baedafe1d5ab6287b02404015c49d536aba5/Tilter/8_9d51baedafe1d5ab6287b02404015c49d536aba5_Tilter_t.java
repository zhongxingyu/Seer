 package storm2013.subsystems;
 
 import edu.wpi.first.wpilibj.ADXL345_I2C;
 import edu.wpi.first.wpilibj.DigitalInput;
 import edu.wpi.first.wpilibj.Jaguar;
 import edu.wpi.first.wpilibj.command.Subsystem;
 import edu.wpi.first.wpilibj.livewindow.LiveWindow;
 import edu.wpi.first.wpilibj.livewindow.LiveWindowSendable;
 import edu.wpi.first.wpilibj.tables.ITable;
 import storm2013.RobotMap;
 import storm2013.utilities.LimitSwitchedMotor;
 import storm2013.utilities.TrigLUT;
 
 /**
  *
  * @author Joe
  */
 public class Tilter extends Subsystem {
     private static final double UP_SPEED_DEFAULT   = 0.6,
                                 DOWN_SPEED_DEFAULT = -0.6;
     private static final double UP_SIGN    = -1;
     
     private static final boolean UP_LIMIT_ON_VALUE   = true,
                                  DOWN_LIMIT_ON_VALUE = true;
     
     private Jaguar _motor = new Jaguar(RobotMap.PORT_MOTOR_TILTER);
     private DigitalInput _topLimitSwitch    = new DigitalInput(RobotMap.PORT_LIMIT_TILTER_TOP),
                          _bottomLimitSwitch = new DigitalInput(RobotMap.PORT_LIMIT_TILTER_BOTTOM);
     private LimitSwitchedMotor _limitedMotor = new LimitSwitchedMotor(_motor,
                                                                       _topLimitSwitch, UP_LIMIT_ON_VALUE,
                                                                       _bottomLimitSwitch, DOWN_LIMIT_ON_VALUE);
     private ADXL345_I2C _accelerometer = new ADXL345_I2C(RobotMap.MODULE_SENSOR_ACCELEROMETER,
                                                          ADXL345_I2C.DataFormat_Range.k2G);
     private LiveWindowSendable _angleSensor = new LiveWindowSendable() {
         private ITable _table;
         
         public void updateTable() {
             if(_table != null) {
                 _table.putNumber("Value", getAngle());
             }
         }
 
         public void startLiveWindowMode() {}
 
         public void stopLiveWindowMode() {}
 
         public void initTable(ITable table) {
             _table = table;
         }
 
         public ITable getTable() {
             return _table;
         }
 
         public String getSmartDashboardType() {
             return "Gyro";
         }
     };
 
     public Tilter() {
         LiveWindow.addActuator("Tilter", "Motor", _motor);
         LiveWindow.addActuator("Tilter", "Angle", _angleSensor);
     }
 
     public void initDefaultCommand() {}
     
     public void move(double speed) {
         _limitedMotor.set(UP_SIGN*speed);
     }
     
     public void moveUp() {
        move(UP_SPEED_DEFAULT);
     }
     public void stop() {
        move(0);
     }
     public void moveDown() {
        move(DOWN_SPEED_DEFAULT);
     }
     
     public double getAngle() {
         double y = Math.abs(_accelerometer.getAcceleration(ADXL345_I2C.Axes.kY)), // g*sin(theta)
                z = Math.abs(_accelerometer.getAcceleration(ADXL345_I2C.Axes.kZ)); // g*cos(theta)
         // assuming y-z axis is forward-back and up-down
         return Math.toDegrees(TrigLUT.atan(y/z));
     }
 }
