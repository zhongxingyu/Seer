 package grouppractical.client;
 
 import grouppractical.client.commands.ClientType;
 import grouppractical.client.commands.Command;
 import grouppractical.client.commands.CommandParser;
 import grouppractical.client.commands.CommandType;
 import grouppractical.client.commands.ConnectCommand;
 import grouppractical.client.commands.MPositionCommand;
 import grouppractical.client.commands.RStatusCommand;
 import grouppractical.server.CommandListener;
 import grouppractical.server.MultiServerThread;
 import grouppractical.utils.map.HorizontalNode;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.Socket;
 import java.net.UnknownHostException;
 
 import javax.swing.event.EventListenerList;
 
 public class ClientConnectionThread extends Thread implements CommandListener {
 	private final Socket socket;
 	private final PrintWriter out;
 	private final BufferedReader in;
 	private final EventListenerList listeners;
 	
 	/**
 	 * Constructs a new ClientConnectionThread
 	 * @param host Hostname of server to connect to
 	 * @throws UnknownHostException Thrown if the specified server hostname can't be found
 	 * @throws IOException Thrown if there was an error connecting to server port at the specified host
 	 */
 	public ClientConnectionThread(String host, ClientType clientType) throws UnknownHostException, IOException {
 		super("ClientConnectionThread");
 		this.socket = new Socket(host, MultiServerThread.PORT);
 		this.out = new PrintWriter(socket.getOutputStream(), true);
 		this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
 		this.listeners = new EventListenerList();
 		enqueueCommand(new ConnectCommand(clientType));
 	}
 	/**
 	 * Closes the exception to the server
 	 */
 	public void close() {
 		out.close();
 		try {
 			in.close();
 		} catch (IOException e) { }
 	}
 	
 	@Override
 	public void run() {
 		boolean loop = true;
 		CommandParser cmdparse = new CommandParser();
 		while (loop) {
 			boolean word = false;
 			while (!word) {
 				try {
 					cmdparse.enqueue((char)in.read());
 				} catch (IOException e) {
 					System.err.println(e.toString());
 					close();
 					return;
 				} catch (InterruptedException e) {
 					close();
 					return;
 				}
 				
 				while (!cmdparse.isOutputEmpty()) {
 					Command cmd = cmdparse.dequeue();
 					switch (cmd.getCommandType()) {
 					case MINITIALISE:
 						for (MapListener l : listeners.getListeners(MapListener.class))
 							l.updateMap(new HorizontalNode(0,1000,0,1000));
 					case MPOSITION:
 						for (MapListener l : listeners.getListeners(MapListener.class))
 							l.updateMapPosition(((MPositionCommand)cmd).getPosition());
 					case RSTATUS:
 						for (RobotListener l : listeners.getListeners(RobotListener.class)) {
 							RStatusCommand rcmd = (RStatusCommand)cmd;
 							l.updateAngle(rcmd.getAngleDegrees(), rcmd.getAngleRadians());
 							l.updateRobotPosition(rcmd.getPosition());
 							l.updateVoltage(rcmd.getVoltage());
 						}
 					case RLOCK:
 					case RUNLOCK:
 						for (RobotListener l : listeners.getListeners(RobotListener.class))
 							l.updateLocked(cmd.getCommandType() == CommandType.RLOCK);
 					}
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Adds a RobotListener, which will be called when the robot is updated
 	 * @param l RobotListener to add
 	 */
 	public void addRobotListener(RobotListener l) {
 		listeners.add(RobotListener.class, l);
 	}
 	/**
 	 * Removes a RobotListener
 	 * @param l RobotListener to remove
 	 */
 	public void removeRobotListener(RobotListener l) {
 		listeners.remove(RobotListener.class, l);
 	}
 	/**
 	 * Adds a MapListener, which will be called when the map is updated
 	 * @param l MapListener to add
 	 */
 	public void addMapListener(MapListener l) {
 		listeners.add(MapListener.class, l);
 	}
 	/**
 	 * Removes a MapListener
 	 * @param l MapListener to remove
 	 */
 	public void removeMapListener(MapListener l) {
 		listeners.remove(MapListener.class, l);
 	}
 
 	@Override
 	public void enqueueCommand(Command cmd) {
 		if (cmd != null)
 			out.println(cmd.serialize());
 	}
 }
