 /**
  * ZeroArm: An autonomous stage that lowers the arm until it triggers the limit switch
  */
 
 package org.usfirst.frc348.auton;
 
 import org.usfirst.frc348.JagBot;

 import edu.wpi.first.wpilibj.can.CANTimeoutException;
 
 public class ZeroArm implements Stage {
     boolean error = false;
     protected JagBot bot;
     
     public ZeroArm(JagBot bot) {
 	this.bot = bot;
     }
     
     public void enter() {
     	bot.arm.setManualMode();
     }
     
     public void periodic() {
 	try {
 	    bot.arm.setManualMode();
 	    bot.arm.manualMove(5);
 	} catch (CANTimeoutException e) { 
 	    e.printStackTrace();
 	    error = true;
 	}
 	bot.arm.close();
     }
     
     public void exit() {
 	// No cleaning up
     }
     
     public boolean isDone() {
 	return bot.arm.atBottom();
     }
     
     public boolean isError() {
 	return error;
     }
     
 }
