 package manager;
 
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JFrame;
 import javax.swing.Timer;
 
 import DeviceGraphicsDisplay.DeviceGraphicsDisplay;
 import DeviceGraphicsDisplay.FeederGraphicsDisplay;
 import DeviceGraphicsDisplay.GantryGraphicsDisplay;
import DeviceGraphicsDisplay.LaneGraphicsDisplay;
 import Networking.Client;
 import Networking.Request;
 import Utils.Constants;
 
 public class GantryRobotManager extends Client implements ActionListener {
 	// Window dimensions
	private static final int WINDOW_WIDTH = 400;
 	private static final int WINDOW_HEIGHT = 700;
 
 	// Create a new timer
 	private Timer timer;
 	
 	/**
 	 * Constructor
 	 */
 	public GantryRobotManager() {
 		super();
 		clientName = Constants.GANTRY_ROBOT_MNGR_CLIENT;
		offset = -800;
 		
 		initStreams();
 		initGUI();
 		initDevices();
 	}
 
 	/**
 	 * Forward network requests to devices processing
 	 * @param req incoming request
 	 */
 	public void receiveData(Request req) {
 		devices.get(req.getTarget()).receiveData(req);
 	}
 	
 	/**
 	 * Initialize the GUI and start the timer.
 	 */
 	public void initGUI() {
 		timer = new Timer(Constants.TIMER_DELAY, this);
 		timer.start();
 	}
 	
 	/**
 	 * Initialize the devices
 	 */
 	public void initDevices() {
 		// TODO - add GantryRobotManager devices here
 		
 		for (int i = 0; i < Constants.FEEDER_COUNT; i++) {
 			addDevice(Constants.FEEDER_TARGET + i, new FeederGraphicsDisplay(this, i));
 		}
 		
		for (int i = 0; i < Constants.LANE_COUNT; i++) {
			addDevice(Constants.LANE_TARGET + i, new LaneGraphicsDisplay(this, i));
		}
 		
		addDevice(Constants.GANTRY_ROBOT_TARGET, new GantryGraphicsDisplay(this));
 	}
 	
 	/**
 	 * Main method sets up the JFrame
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		JFrame frame = new JFrame();
 		Client.setUpJFrame(frame, WINDOW_WIDTH, WINDOW_HEIGHT, "Gantry Robot Manager");
 		
 		GantryRobotManager mngr = new GantryRobotManager();
 		frame.add(mngr);
 		mngr.setVisible(true);
 		frame.validate();
 	}
 	
 	/**
 	 * This function intercepts requests and calls client's sendData if the request is a DONE request.
 	 * @req Request to be sent.
 	 */
 	@Override
 	public void sendData(Request req) {
 		if (!req.getCommand().endsWith(Constants.DONE_SUFFIX)) {
 			super.sendData(req);
 		}
 	}
 	
 	/**
 	 * This function handles painting of graphics
 	 */
 	public void paintComponent(Graphics gg) {
 		Graphics2D g = (Graphics2D) gg;
 		g.drawImage(Constants.CLIENT_BG_IMAGE, 0, 0, this);
 		
 		for(DeviceGraphicsDisplay device : devices.values()) {
 			device.draw(this, g);
 		}
 	}
 
 	/**
 	 * This function handles action events.
 	 */
 	public void actionPerformed(ActionEvent ae) {
 		repaint();
 	}
 }
