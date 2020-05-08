 package TetrisBattleBot.Testing;
 
 import java.awt.*;
 import TetrisBattleBot.WebsiteInteraction;
 import TetrisBattleBot.WebsiteInteraction.MoveType;
 
 
 public class TestKeys {
     public static void main(String[] args) throws java.awt.AWTException {
 	Robot robot = new Robot();
         Runtime rt = Runtime.getRuntime();
 	
	for (int i = 0; i < 5; i++) {
	     robot.delay(1000);
	     WebsiteInteraction.pressKey(MoveType.LEFT , rt);
 	}
     }
 }
