 package Testing;
 
 import java.io.File;
 
 import game.FourInARowGame;
 import general.Config;
 import io.FileMonitor;
 
 public class MDE_FileMonitorTest {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		//FileMonitor my_monitor = new FileMonitor(new File("C:\\Users\\Michi\\Desktop\\test.txt"));
 		//FileMonitor my_monitor = new FileMonitor(null);
 		
 		///Users/apfelbaum24/Desktop
		FourInARowGame testGame = new FourInARowGame(Config.SETCOUNT, "/Users/apfelbaum24/Dropbox/WI-Projekt INTERN/test"); // C:\\Users\\Michi\\Desktop
 		
 		testGame.startNewSet();
 		//testGame.getCurrentSet().getWinningPlayer();
 		//my_monitor.startMonitoring();
 		
 		//my_monitor.setFilePath(new File("C:\\Users\\Michi\\Desktop\\test.txt"));
 	}
 
 }
