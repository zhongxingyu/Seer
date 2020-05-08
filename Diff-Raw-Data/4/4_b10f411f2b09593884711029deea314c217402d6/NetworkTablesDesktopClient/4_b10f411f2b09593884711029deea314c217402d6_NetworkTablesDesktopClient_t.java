 import edu.wpi.first.smartdashboard.robot.Robot;
 import edu.wpi.first.wpilibj.networktables.NetworkTable;
 import edu.wpi.first.wpilibj.networktables2.*;
 
 
 public class NetworkTablesDesktopClient {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		new NetworkTablesDesktopClient();
 		NetworkTablesDesktopClient.run();
 
 	}
 
 	static void run() {
		NetworkTable.setIPAddress("10.25.57.2"); //sets our team number
		NetworkTable table = NetworkTable.getTable("SmartDashboard"); 
 		
 		while (true) {
 			try {
 				Thread.sleep(1000);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 			
 			table.putNumber("Q", 11); //puts the number "11" into a key called "Q"
 			
 			try {
 				Thread.sleep(1000);
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			
 			System.out.println(table.getNumber("Q")); //Gets the value "Q" that we put in earlier
 		}
 		
 	}
 
 }
