 package de.unihalle.sim.main;
 
 import java.io.FileNotFoundException;
 import java.util.List;
 
 import org.mitre.sim.Simulation;
 
 import com.google.common.collect.Lists;
 
 import de.unihalle.sim.entities.Bee;
 import de.unihalle.sim.entities.BeeHive;
 import de.unihalle.sim.entities.Flower;
 import de.unihalle.sim.entities.Meadow;
 import de.unihalle.sim.entities.PositionedEntity;
 import de.unihalle.sim.entities.Bee.BeeFactory;
 import de.unihalle.sim.entities.BeeHive.BeeHiveFactory;
 import de.unihalle.sim.entities.Flower.FlowerFactory;
 import de.unihalle.sim.util.Position;
 import de.unihalle.sim.util.TimeUtil;
 
 public class BeeSimulation extends Simulation {
 
 	private static final long serialVersionUID = 1L;
 
 	private static final int SIMULATION_PACE = 0;
 	private static final double SIMULATION_TIME = TimeUtil.months(12);
 
 	private Environment _environment = new Environment(-500, 500, -500, 500);
 	private List<EventListener> _listeners = Lists.newArrayList();
 	private static InputData _inputData = new InputData();
 
 	private BeeFactory _beeFactory;
 	private BeeHiveFactory _hiveFactory;
 	private FlowerFactory _flowerFactory;
 	private int _hiveGroups;
 	private int _hivesPerGroup;
 
 	public BeeSimulation(int hiveGroups, int hivesPerGroup) {
 		_hiveGroups = hiveGroups;
 		_hivesPerGroup = hivesPerGroup;
 		_beeFactory = Bee.createFactory(this);
 		_hiveFactory = BeeHive.createFactory(this);
 		_flowerFactory = Flower.createFactory(this);
 	}
 
 	@Override
 	public void initialize() {
 		setTimeLast(SIMULATION_TIME);
 		setPace(SIMULATION_PACE);
 		createFlowers();
 		createHives();
 	}
 
 	private void createHives() {
 		int groupNumbers = _hiveGroups;
 		int groupFixed = 0;
 		int groupSize = _hivesPerGroup;
 		int numbersOfGroupsSet = (int) Math.ceil(Math.sqrt(groupNumbers));
 
 		int dimensionX = Math.abs(_environment.getMaxX()) + Math.abs(_environment.getMinX());
 		int dimensionY = Math.abs(_environment.getMaxY()) + Math.abs(_environment.getMinY());
 
 		int pixelX = Math.round(dimensionX / numbersOfGroupsSet);
 		int pixelY = Math.round(dimensionY / numbersOfGroupsSet);
 
 		for (int y = pixelY; y <= dimensionY; y += pixelY) {
 			for (int x = pixelX; x <= dimensionX; x += pixelX) {
 
 				if (groupFixed < groupNumbers) {
 
 					int groupCount = 0;
 					int groupDimension = (int) Math.ceil(Math.sqrt(groupSize));
 
 					for (int xg = 0; xg < groupDimension; xg++)
 						for (int yg = 0; yg < groupDimension; yg++) {
 							if (groupCount < groupSize) {
 								registerHive(Position.createFromCoordinates((x - dimensionX / 2) - pixelX / 2 + xg,
 										(y - dimensionY / 2) - pixelY / 2 + yg), _inputData.getNumberOfBeesPerHive(),
 										"Hive" + groupFixed + "_" + groupCount);
 								groupCount++;
 							}
 						}
 				}
 				groupFixed++;
 			}
 		}
 	}
 
 	private void createFlowers() {
 		for (int i = 0; i < _inputData.getNumberOfFlowers(); i++) {
 			registerFlower("Flower" + i);
 		}
 		register(new Meadow(this), "Meadow");
 	}
 
 	@Override
 	public void simulationComplete() {
 		info("Simulation complete.");
 		for (EventListener e : _listeners) {
 			e.close();
 		}
 	}
 
 	// do not call this method getEnvironment as tortuga will crash
 	public Environment environment() {
 		return _environment;
 	}
 
	// do not call this method getInputData as tortuga will crash
 	public static InputData inputData() {
 		return _inputData;
 	}
 
 	public void stopSimulation() {
 		simulationComplete();
 		System.exit(0);
 	}
 
 	public void notifyListeners(PositionedEntity entity) {
 		for (EventListener e : _listeners) {
 			e.notify(entity);
 		}
 	}
 
 	public void addEventListener(EventListener e) {
 		_listeners.add(e);
 	}
 
 	private void registerHive(Position pos, int capacity, String name) {
 		BeeHive newHive = _hiveFactory.createHiveAtPosition(pos, capacity);
 		register(newHive, name);
 		_environment.addHive(newHive);
 	}
 
 	private void registerFlower(String name) {
 		Flower currentFlower = _flowerFactory.createFlower();
 		_environment.addFlower(currentFlower);
 	}
 
 	public static void main(String[] args) throws Exception {
 
 		BeeCommandLineParser arguments = BeeCommandLineParser.parse(args);
 
 		BeeSimulation simulation = new BeeSimulation(arguments.getNumberOfGroups(), arguments.getGroupSize());
 
 		if (arguments.showGui()) {
 			simulation._environment = new Environment(-20, 30, -20, 30);
 			simulation.addEventListener(new VisualisationEventListener(simulation));
 		}
 
 		if (arguments.generateReport()) {
 			try {
 				simulation.addEventListener(new ReportEventListener("report.csv", simulation));
 			} catch (FileNotFoundException e) {
 				e.printStackTrace();
 			}
 		}
 
 		if (arguments.showControls()) {
 			simulation.setVisible(true);
 		} else {
 			simulation.run();
 		}
 	}
 
	// do not call this method getBeeFactory as tortuga will crash
 	public BeeFactory beeFactory() {
 		return _beeFactory;
 	}
 
 }
