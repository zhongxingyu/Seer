 package org.usfirst.frc348;
 
 import edu.wpi.first.wpilibj.CANJaguar;
 import edu.wpi.first.wpilibj.PIDSource;
 import edu.wpi.first.wpilibj.Servo;
 import edu.wpi.first.wpilibj.DigitalInput;
 import edu.wpi.first.wpilibj.SmartDashboard;
 import edu.wpi.first.wpilibj.can.CANTimeoutException;
 import edu.wpi.first.wpilibj.PIDController;
 
 public class Arm implements PIDSource {
     public CANJaguar jag;
     protected Servo servo;
     protected DigitalInput limit, banner;
     public PIDController pid;
     protected double target;
     protected boolean magicMode = true, placing = false;
 
     // Position to move the arm to based off of the controller
    protected static double positions[] = {-1, 1.87, 1.680, 0.915, 0.700, -2};
 
     public Arm(int jagID, int servoPort, int limitPort, int bannerPort) throws CANTimeoutException {
     	jag = Utils.getJaguar(jagID);
     	jag.configEncoderCodesPerRev(360);
     	jag.setSpeedReference(CANJaguar.SpeedReference.kQuadEncoder);
     	jag.setPositionReference(CANJaguar.PositionReference.kQuadEncoder);
     	
     	servo = new Servo(servoPort);
     	limit = new DigitalInput(limitPort);
     	banner = new DigitalInput(bannerPort);
     	
     	pid = new PIDController(-4, 0, 0, this, jag);
     	pid.enable();
     }
 
     public void setManualMode() {
 	if (magicMode) {
 	    magicMode = false;
 	    pid.disable();	    
 	}
     }
 
     public void setMagicMode() {
 	if (!magicMode) {
 	    magicMode = true;
 	    pid.enable();
 	}
     }
 
     public void periodic() {
 	if (limit.get()) {
	    pid.setOutputRange(-1, 0);
 	    zeroEncoder();
 	} else {
	    pid.setOutputRange(-1, 1);
 	}
     }
     
     public void manualMove(int pos) throws CANTimeoutException {
 	double out = 0;
 	if (pos == 1) {
 	    out = -1;
 	} else if (pos == 2) {
 	    out = -.5;
 	} else if (pos == 4 && !limit.get()) {
 	    out = .25;
 	} else if (pos == 5 && !limit.get()) {
 	    out = .75;
 	}
 	jag.setX(out);
     }
 
     public void moveToPosition(int pos) throws CANTimeoutException {
 	placing = false;
 	pid.setSetpoint(positions[pos]);
     }
 
     public void placePiece() {
 	if (!placing) {
 	    placing = true;
 	    open();
 	    pid.setSetpoint(pid.getSetpoint() - 0.3);
 	}
     }
 
     public double getArmPosition() {
 	try {
 	    return jag.getPosition() - zero;
 	} catch (CANTimeoutException e) { e.printStackTrace(); return zero; }
     }
 
     public boolean atPole() {
 	return banner.get();
     }
 
     public boolean atBottom() {
 	return limit.get();
     }
 
     private double zero = 0;
     public void zeroEncoder() {
     	try {
 	    zero = jag.getPosition();
 	} catch (CANTimeoutException e) { e.printStackTrace(); }
     }
 
     public void open() {
 	servo.set(0);
     }
 
     public void close() {
 	servo.set(1);
     }
 
     public void updateDashboard() {
 	    SmartDashboard.log(getArmPosition(), "Arm");
 	    SmartDashboard.log(servo.getPosition() < 0.25, "Grabber");
 	    SmartDashboard.log(limit.get(), "Limit");
     }
 
     public double pidGet() {
 	periodic();
 	return getArmPosition();
     }
 }
