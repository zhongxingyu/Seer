 package grouppractical.client.demo;
 
 import grouppractical.client.commands.ClientType;
 import grouppractical.client.simple.SimpleApplication;
 
 import java.io.File;
 
 public class DemoApplication extends SimpleApplication {
 	public static void main(String[] args) {
 		String host = args.length > 0 ? args[0] : "127.0.0.1";
 		new DemoApplication(host,"Demo Client");
 	}
 	
 	protected final RobotStatus robotStatus;
 	protected final MapImage mapImage;
 	
 	/**
 	 * Constructs new client application
 	 * @param host Host name of server
 	 * @param name Friendly name of this client
 	 */
 	public DemoApplication(String host, String name) {
 		super(host, name, ClientType.MAPPER);
 		
 		//Creates new robot status window and map window
 		this.robotStatus = new RobotStatus(25);
 		conn.addRobotListener(robotStatus);
		
 		this.mapImage = new MapImage(new File("mapout.png"), conn, console);
		
 	}
 }
