 package TetrisBattleBot.Testing;
 
 import java.awt.*;
 import TetrisBattleBot.WebsiteInteraction;
 import TetrisBattleBot.WebsiteInteraction.MoveType;
 
 
 public class TestKeys {
     public static void main(String[] args) throws java.awt.AWTException {
 	Robot robot = new Robot();
         Runtime rt = Runtime.getRuntime();
 	
	for (MoveType mt : MoveType.values()) {
	    if (mt == MoveType.CLICK) continue;
	    if (mt == MoveType.CLICK) continue;
	    robot.delay(400); 
	    WebsiteInteraction.pressKey(mt , rt);
 	}
     }
 }
