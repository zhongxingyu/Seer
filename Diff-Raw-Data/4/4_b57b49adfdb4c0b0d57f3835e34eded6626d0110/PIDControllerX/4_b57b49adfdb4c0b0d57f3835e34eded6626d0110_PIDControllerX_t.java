 package program.control;
 
 import program.main.MathUtils;
 
 public class PIDControllerX {
 	
 	private double kp, ki, kd, setpoint;
 	private double lastError, totalError;
 	private boolean firstRun;
 	
 	private boolean capOutput = false;
 	private double minOutput, maxOutput;
 	
 	public PIDControllerX(double kp, double ki, double kd) {
 		this.kp = kp;
 		this.ki = ki;
 		this.kd = kd;
 		this.lastError = 0;
 		this.totalError = 0;
 		this.firstRun = true;
 	}
 	
 	public PIDControllerX(double kp, double ki, double kd, double setpoint) {
 		this(kp, ki, kd);
 		this.setpoint = setpoint;
 	}
 	
 	public void setPIDConstants(double kp, double ki, double kd) {
 		this.kp = kp;
 		this.ki = ki;
 		this.kd = kd;
 	}
 	
 	public void setSetpoint(double setpoint) {
 		this.setpoint = setpoint;
 	}
 	
 	public void setOutputCaps(double minOutput, double maxOutput) {
 		capOutput = true;
 		this.minOutput = minOutput;
 		this.maxOutput = maxOutput;
 	}
	public void enableOutputCaps(boolean enable) {
		if (enable) capOutput = true;
 		else capOutput = false;
 	}
 	
 	public double getOutput(double input) {
 		double error = setpoint - input;
 		double correction;
 		
 		totalError += error;
 		
 		if (firstRun) {
 			correction = kp * error;
 			firstRun = false;
 		} else {
 			double de = error - lastError;
 			correction = kp * error + ki * totalError + kd * de;
 		}
 		
 		lastError = error;
 		
 		if (capOutput) {
 			correction = MathUtils.clamp(correction, minOutput, maxOutput);
 		}
 		
 		return correction;	
 	}
 	
 	public void reset() {
 		this.firstRun = true;
 		this.lastError = 0;
 		this.totalError = 0;
 	}
 	
 	public double getKp() { return kp; }
 	
 	public double getKi() { return ki; }
 	
 	public double getKd() { return kd; }
 
 }
