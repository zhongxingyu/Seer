 /**
  ******************************************************************************
  *                                Application.java                            *
  ****************************************************************************** 
  * (Overview)
  * 
  *  @author simple-developer
  *  @since 5 Jul 2012
  * 
  * (Description)
  */
 package operations;
 
 import java.util.ArrayList;
 
 import gnu.io.CommPortIdentifier;
 
 import models.Command;
 import views.Window;
 import operations.ConfigReader;
 
 public class Application {
 	
 	public static boolean connected;
 	public static Command[] commands;
 	
 	public static void main (String[] args) {
 		Window mainWindow = new Window();
 		mainWindow.setVisible(true);
 		
 		//Start a status requester.
		(new Thread(new StatusRequester("AT+CSQ\r", 10000))).start();
 		
 		listCommands();
 		listDevices();
 	}
 	
 	public static void write (String message) {
 		SerialInterface.write(message);
 		Window.writeToMonitor("WRITE: " + message);
 	}
 	
 	public static String read () {
 		return SerialInterface.read();
 	}
 	
 	public static void listCommands () {
 		commands = ConfigReader.read();
 		for(int i = 0; i < commands.length; i++) {
 			Window.listCommand(commands[i].getName());
 		}
 	}
 	
 	public static void listDevices () {
 		@SuppressWarnings("unchecked")
 		java.util.Enumeration<CommPortIdentifier> portEnum = CommPortIdentifier.getPortIdentifiers();
 		ArrayList<String> devices = new ArrayList<String>();
 		//Create the list of devices
         while (portEnum.hasMoreElements()) {
             CommPortIdentifier portIdentifier = portEnum.nextElement();
             devices.add(portIdentifier.getName());
         }
         //Then add them in reverse order to the combo box.
         for(int i = devices.size() - 1; i >= 0; i--) {
         	Window.listDevice(devices.get(i));
         }
 	}
 	
 	public static void deviceConfig () {
 		//Turn on incoming call information
 		SerialInterface.write("AT+CLIP=1");
 	}
 	
 	/**
 	 * Sends a status request command to the device at a interval
 	 * defined in the constructor.
 	 * @author simple-developer
 	 *
 	 */
 	public static class StatusRequester implements Runnable {
 		private String command;
 		private int interval;
 		
 		public StatusRequester (String command, int interval) {
 			this.command = command;
 			this.interval = interval;
 		}
 		
 		public void run() {
 			while(true) {
 				try {
 					write(this.command);
 					Thread.sleep(interval);
 				} catch (InterruptedException e) {
 					System.out.println(e.getMessage());
 				}
 				if(!Application.connected) {
 					break;
 				}
 			}
 		}
 		
 	}
         
 }
