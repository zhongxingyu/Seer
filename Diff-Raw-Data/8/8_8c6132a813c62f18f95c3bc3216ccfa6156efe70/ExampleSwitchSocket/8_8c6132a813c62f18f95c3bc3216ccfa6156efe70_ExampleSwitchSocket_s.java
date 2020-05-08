 import com.tinkerforge.BrickletRemoteSwitch;
 import com.tinkerforge.IPConnection;
 
 public class ExampleSwitchSocket {
 	private static final String host = "localhost";
 	private static final int port = 4223;
 	private static final String UID = "XYZ"; // Change to your UID
 
 	// Note: To make the example code cleaner we do not handle exceptions. Exceptions you
 	//       might normally want to catch are described in the documentation
 	public static void main(String args[]) throws Exception {
 		IPConnection ipcon = new IPConnection(); // Create IP connection
 		BrickletRemoteSwitch rs = new BrickletRemoteSwitch(UID, ipcon); // Create device object
 
 		ipcon.connect(host, port); // Connect to brickd
 		// Don't use device before ipcon is connected
 
		// Switch socket with house code 42 and receiver code 23 on
		rs.switchSocket((short)42, (short)23, BrickletRemoteSwitch.SWITCH_TO_ON);
 
 		System.console().readLine("Press key to exit\n");
 	}
 }
