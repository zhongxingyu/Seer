 package org.usfirst.frc1318;
 
 import org.usfirst.frc1318.calculators.TBKickerCalculator;
import org.usfirst.frc1318.components.GamePadRunner;
 import org.usfirst.frc1318.components.TBGamePadReader;
 import org.usfirst.frc1318.components.TBSolenoidRunner;
 import org.usfirst.frc1318.components.TBThroughBeamReader;
 
 public class MainIterativeComponentRobot extends IterativeComponentRobot {
 
 	public BotVector currentRobotComponents() {
 		return helloBot();
 	}
 
 	private static BotVector helloBot() {
 		BotVector botVector = new BotVector();
 		return botVector;
 	}
 	
 	private static BotVector pneumaticTestBed() {
 		BotVector botVector = new BotVector();
 		botVector.add(new TBGamePadReader());
 		botVector.add(new TBKickerCalculator());
 		botVector.add(new TBSolenoidRunner());
 		botVector.add(new TBThroughBeamReader());
 		
 		return botVector;
 	}
 }
