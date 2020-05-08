 /**
  * MoveArm: An autonomous stage that moves the arm to a specific location
  */
 
 package org.usfirst.frc348.auton;
 
 import org.usfirst.frc348.JagBot;
import org.usfirst.frc348.auton.Stage;
 import edu.wpi.first.wpilibj.SmartDashboard;
 import edu.wpi.first.wpilibj.can.CANTimeoutException;
 
 public class MoveArm implements Stage {
     protected JagBot bot;
     protected boolean error = false;
     protected int position;
     
     public MoveArm(JagBot bot, int position) {
 	this.bot = bot;
 	this.position = position;
     }
     
     protected int timer;
     public void enter() {
 	bot.arm.setMagicMode();
 	timer = 0;
     }
     
     public void periodic() {
     timer += 1;
 	try {
 	    bot.arm.moveToPosition(position);
 	} catch (CANTimeoutException e) {
 	    e.printStackTrace();
 	    error = true;
 	}
 	SmartDashboard.log(bot.arm.pid.getError(), "PID error");
     }
     
     public void exit() {
 	// No cleaning up
     }
     
     public boolean isDone() {
 	return bot.arm.pid.getError() < 0.5 && timer > 10;
     }
     
     public boolean isError() {
 	return error;
     }
     
 }
