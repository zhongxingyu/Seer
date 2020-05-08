 package at.fhv.audioracer.simulator.world;
 
 import java.awt.image.BufferedImage;
 import java.io.IOException;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.imageio.ImageIO;
 import javax.naming.OperationNotSupportedException;
 
 import org.apache.log4j.Logger;
 
 import at.fhv.audioracer.communication.world.ICamera;
 import at.fhv.audioracer.communication.world.ICarClient;
 import at.fhv.audioracer.communication.world.ICarClientManager;
 import at.fhv.audioracer.communication.world.WorldNetwork;
 import at.fhv.audioracer.core.model.Car;
 import at.fhv.audioracer.core.model.Map;
 import at.fhv.audioracer.core.util.Direction;
 import at.fhv.audioracer.core.util.Position;
 import at.fhv.audioracer.server.CarClientManager;
 import at.fhv.audioracer.simulator.world.impl.CarClient;
 import at.fhv.audioracer.ui.pivot.MapComponent;
 
 import com.esotericsoftware.kryonet.Client;
 import com.esotericsoftware.kryonet.rmi.ObjectSpace;
 import com.esotericsoftware.kryonet.rmi.RemoteObject;
 
 // TODO: This class name may change.
 /**
  * Takes care of the initialization of the logic of the simulator.
  * 
  * @author lumannnn
  * 
  */
 public class SimulationController {
 	
 	private static final int TIMEOUT = 1000;
 	
 	private static final int HOST = 4711;
 	
 	private static final Logger logger = Logger.getLogger(SimulationController.class);
 	
 	private static SimulationController _instance;
 	
 	private ICarClientManager _carClientManager;
 	private ICamera _camera;
 	
 	private MapComponent _map;
 	
 	private int _carId;
 	
 	private List<ICarClient> _carClients;
 	
 	private Position _lastCarPos;
 	private static float TRANSLATE_BY = 2;
 	
 	private SimulationController() {
 		_carId = 0;
 		_lastCarPos = new Position(0, 0);
 		_carClients = new LinkedList<ICarClient>();
 		
 		// establish the connections
 		_carClientManager = CarClientManager.getInstance();
 		_camera = connectCamera();
 	}
 	
 	public static SimulationController getInstance() {
 		if (_instance == null) {
 			_instance = new SimulationController();
 		}
 		
 		return _instance;
 	}
 	
 	public void setUp(MapComponent mapComponent, Map map) throws OperationNotSupportedException {
 		setMap(mapComponent);
 		getMapComponent().setMap(map);
 	}
 	
 	public void setMap(MapComponent map) {
 		if (map == null) {
 			throw new IllegalArgumentException("map must not be null");
 		}
 		_map = map;
 	}
 	
 	public MapComponent getMapComponent() {
 		return _map;
 	}
 	
 	public void addCar() {
 		try {
 			startCarClient();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public void removeCar() {
 		stopCarClient();
 	}
 	
 	private Map getMap() {
 		return getMapComponent().getMap();
 	}
 	
 	public void update() {
 		// int x = getMap().getMap().getSizeX();
 		// int y = getMap().getMap().getSizeY();
 		// System.out.println("MapSize: " + x + ", " + y);
 		
 	}
 	
 	// helper methods
 	
 	private ICamera connectCamera() {
 		Client client = new Client();
 		client.start();
 		WorldNetwork.register(client);
 		
 		// get the ICamera from the server
 		ICamera camera = ObjectSpace.getRemoteObject(client, WorldNetwork.CAMERA_SERVICE, ICamera.class);
 		((RemoteObject) camera).setTransmitExceptions(false); // disable exception transmitting
 		
 		return camera;
 	}
 	
	private void translateLastCarPosX(float x) {
 		_lastCarPos = new Position(_lastCarPos.getPosX() + x, 0);
 	}
 	
 	private void startCarClient() throws IOException {
 		// add car to map
 		BufferedImage image = ImageIO.read(MapComponent.class.getResource("car-red.png"));
 		Car car = new Car(_carId++, _lastCarPos, new Direction(90), image);
		translateLastCarPosX(TRANSLATE_BY);
 		getMap().addCar(car);
 		
 		ICarClient carClient = new CarClient();
 		((CarClient) carClient).setCar(car); // this only needs to be done in the simulation
 		
 		_carClientManager.connect(carClient);
 		_carClients.add(carClient);
 		
 		logger.info("added car with id: " + (_carId - 1));
 	}
 	
 	private void stopCarClient() {
 		if (_carId > 0) {
			translateLastCarPosX(-TRANSLATE_BY);
 			getMap().removeCar(--_carId);
 			logger.info("removed car with id: " + (_carId));
 		}
 	}
 	
 }
