 package at.fhv.audioracer.simulator.world.pivot;
 
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.URL;
 import java.util.concurrent.Executor;
 import java.util.concurrent.Executors;
 
 import javax.naming.OperationNotSupportedException;
 
 import org.apache.pivot.beans.BXML;
 import org.apache.pivot.beans.BXMLSerializer;
 import org.apache.pivot.beans.Bindable;
 import org.apache.pivot.util.Resources;
 import org.apache.pivot.wtk.Alert;
 import org.apache.pivot.wtk.Application;
 import org.apache.pivot.wtk.Component;
 import org.apache.pivot.wtk.DesktopApplicationContext;
 import org.apache.pivot.wtk.Display;
 import org.apache.pivot.wtk.MessageType;
 import org.apache.pivot.wtk.Window;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import at.fhv.audioracer.communication.world.WorldNetwork;
 import at.fhv.audioracer.core.model.Map;
 import at.fhv.audioracer.server.Main;
 import at.fhv.audioracer.server.pivot.ServerView;
 import at.fhv.audioracer.simulator.world.SimulationController;
 import at.fhv.audioracer.ui.pivot.MapComponent;
 import at.fhv.audioracer.ui.util.awt.RepeatingReleasedEventsFixer;
 
 import com.esotericsoftware.kryonet.Client;
 
 public class WorldSimulatorWindow extends Window implements Application, Bindable {
 	
 	@BXML
 	private MapComponent _map;
 	
 	private Window _window;
 	private static final Logger _logger = LoggerFactory.getLogger(WorldSimulatorWindow.class);
 	public static final Executor executor = Executors.newSingleThreadExecutor();
 	
 	private static final int DEFAULT_MAP_SIZE_X = 300;
 	private static final int DEFAULT_MAP_SIZE_Y = 300;
 	
 	private static Client _cameraClient;
 	private static Object _lock = new Object();
 	
 	// private static ArrayList<CarCommunicationProxy> _carList = new ArrayList<CarCommunicationProxy>();
 	
 	@Override
 	public void initialize(org.apache.pivot.collections.Map<String, Object> namespace, URL location, Resources resources) {
 		System.out.println("initialize()");
 		try {
 			SimulationController.getInstance().setUp(_map, new Map(DEFAULT_MAP_SIZE_X, DEFAULT_MAP_SIZE_Y));
 		} catch (OperationNotSupportedException e1) {
 			String msg = "Couldn't initialize the map.";
 			Alert.alert(MessageType.ERROR, msg, this);
 			// We should probably disable some of the GUI's functionality
 			_logger.error(msg, e1);
 		}
 		
 		new Thread(new Runnable() {
 			
 			@Override
 			public void run() {
 				try {
 					Thread.sleep(1000);
 					while (true) {
 						SimulationController.getInstance().update();
 					}
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 			}
 		}).start();
 		
 	}
 	
 	@Override
 	public void startup(Display display, org.apache.pivot.collections.Map<String, String> properties) throws Exception {
 		System.out.println("startup()");
 		
 		BXMLSerializer bxml = new BXMLSerializer();
 		_window = (Window) bxml.readObject(WorldSimulatorWindow.class, "window.bxml");
 		_window.open(display);
 	}
 	
 	@Override
 	public void suspend() throws Exception {
 	}
 	
 	@Override
 	public void resume() throws Exception {
 	}
 	
 	@Override
 	public boolean shutdown(boolean optional) throws Exception {
 		return false;
 	}
 	
 	public void setView(Component view) {
 		_window.setContent(view);
 	}
 	
 	public static void main(String[] args) {
 		
 		_logger.info("Starting AudioRacer WorldSimulator");
 		_logger.info("Initializing server");
 		new Thread(new Runnable() {
 			@Override
 			public void run() {
 				String[] args = new String[1];
 				args[0] = "--gui=false";
 				Main.start(args);
 			}
 		}).start();
 		new RepeatingReleasedEventsFixer().install();
 		DesktopApplicationContext.main(ServerView.class, args);
 		
 		// ugly but it works for the moment ....
 		try {
 			Thread.sleep(2000);
 		} catch (InterruptedException e) {
 			
 		}
 		
 		_logger.info("Starting simulator");
 		new RepeatingReleasedEventsFixer().install();
 		DesktopApplicationContext.main(WorldSimulatorWindow.class, args);
 		
 		try {
 			
 			// Test purpose only
 			startCameraClient();
 			
 			while (true) {
 				try {
 					synchronized (_lock) {
 						_lock.wait();
 					}
 				} catch (InterruptedException e) {
 					// Restore the interrupted status
 					Thread.currentThread().interrupt();
 				}
 			}
 			
 		} catch (Exception e) {
 			_logger.error("Exception caught during startup!", e);
 			
 			if (_cameraClient != null) {
 				_cameraClient.close();
 			}
 			// TODO: Are this all connections we need to close?
 		}
 	}
 	
 	private static void startCameraClient() throws IOException {
 		_cameraClient = new Client(30 * 81920, 20 * 2048);
 		_cameraClient.start();
 		
 		WorldNetwork.register(_cameraClient);
 		
 		_cameraClient.connect(1000, InetAddress.getLoopbackAddress(), WorldNetwork.CAMERA_SERVICE_PORT);
 		
 	}
 	
 	public static Client getCameraClient() {
 		return _cameraClient;
 	}
 	
 }
