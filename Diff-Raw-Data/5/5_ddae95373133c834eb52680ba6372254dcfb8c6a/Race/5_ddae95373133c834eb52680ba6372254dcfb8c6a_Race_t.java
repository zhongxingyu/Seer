 package com.car.model;
 
 import java.util.ArrayList;
 import java.util.BitSet;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 import java.util.Map;
 
 import com.badlogic.gdx.math.MathUtils;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.World;
 import com.car.ai.SeekWaypointSensorIntelligence;
 import com.car.ai.WayPointsLine;
 import com.car.listener.Car2dContactListener;
 import com.car.model.Car.CarColor;
 import com.car.utils.Constants;
 import com.car.utils.TiledMapHelper;
 
 public class Race {
 	// Carros da corrida
 	private List<Car> cars = new ArrayList<Car>();	
 	// Carro do jogador
 	private Car player;
 	
 	// Carro para qual a camera deve manter o foco, criado apenas para facilitar testes da IA
 	private Car focusCar;
 	private World world;	
 	private WayPointsLine wayPointsLine;
 	private List<Checkpoint> checkpoints;
 	
 	// Constantes dos sensores
 	private float wallSensorRange;
 	private float wayPointRange;
 	
 	// Total de voltas
 	private int totalLaps;
 	
 	private boolean raceFinished = false;
 	
 	public Race(TiledMapHelper tiledHelper){				
 		this.world = new World(new Vector2(0, 0), false);
 		world.setContactListener(new Car2dContactListener());
 		wayPointsLine = new WayPointsLine(tiledHelper, world);
 		checkpoints = createCheckPoints(tiledHelper,world);
 		this.wallSensorRange = tiledHelper.getWallSensorRange();
 		this.wayPointRange = tiledHelper.getWayPointRange();
 		this.totalLaps = tiledHelper.getTotalLaps();
 		loadRaceCars(tiledHelper);
 		createRaceWalls(tiledHelper, world);
 	}
 
 
 	private List<Checkpoint> createCheckPoints(TiledMapHelper tiledHelper,
 			World world) {
 		List<Checkpoint> checkpoints = new ArrayList<Checkpoint>();
 		Map<Integer,List<Vector2>> checkpointsTiled = tiledHelper.getCheckPointsTiled();
 		for(Integer i : checkpointsTiled.keySet()){
 			checkpoints.add(new Checkpoint(this,checkpointsTiled.get(i),i));
 			checkpoints.get(i).makeItPhysical(world);
 		}
 		return checkpoints;
 	}
 
 	private void loadRaceCars(TiledMapHelper tiledHelper) {
 		Map<Integer, CarPosition> carPositions = tiledHelper.getRacePositions();
 		Car lastComputerCar = null;
 		for(Integer position: carPositions.keySet()){			
 			CarPosition carPos = carPositions.get(position);
 			if(position == Constants.CAR_PLAYER_INITIAL_POSITION){
 				// Player
				player = new Car(this, carPos,CarColor.value(position-1));
 				cars.add(player);
 			}
 			else{
				Car computer = new Car(this, carPos, CarColor.value(position-1),new SeekWaypointSensorIntelligence());
 				cars.add(computer);
 				lastComputerCar = computer;
 			}
 		}
 		
 		//foco da camera
 		//focusCar = lastComputerCar;
 		focusCar = player;
 	}	
 	
 	private void createRaceWalls(TiledMapHelper tiledHelper, World world) {
 		Wall insideWall = new Wall(this, tiledHelper.getInsideTrackLine(), Wall.WallType.INSIDE);
 		Wall outsideWall = new Wall(this, tiledHelper.getOutsideTrackLine(), Wall.WallType.OUTSIDE);
 		Wall boundaryWall = new Wall(this, tiledHelper.getBoudaryLimitsLine(), Wall.WallType.BOUNDARY);
 		
 		insideWall.createWallInPhysicalWorld(world);
 		outsideWall.createWallInPhysicalWorld(world);
 		boundaryWall.createWallInPhysicalWorld(world);
 	}
 
 	public float getPlayerX(){
 		return player.getBody().getPosition().x;
 	}
 	
 	public float getPlayerY(){
 		return player.getBody().getPosition().y;
 	}
 	
 	public float getFocusCarX(){
 		return focusCar.getBody().getPosition().x;
 	}
 	
 	public float getFocusCarY(){
 		return focusCar.getBody().getPosition().y;
 	}
 	
 	public World getWorld(){
 		return world;
 	}
 
 	public float getPlayerAngleInDegrees() {
 		return player.getBody().getAngle() * MathUtils.radiansToDegrees;
 	}
 
 	public void update(float timeStep, int velocityIterations, int positionIterations, BitSet playerControls) {
 		for(Car car: cars){
 			car.updateSensors();
 			BitSet controls = playerControls;
 			if(car.getType() == Car.CarType.COMPUTER){
 				controls = car.getCarNextControls();
 			}
 			
 			car.update(controls);			
 		}
 		
 		world.step(timeStep, velocityIterations, positionIterations);				
 	}
 
 	public void updatePositions() {
 		Collections.sort(getCars(), new Comparator<Car>() {
 
 			@Override
 			public int compare(Car c1, Car c2) {
 				if(c1.getLap() != c2.getLap()){
 					return c2.getLap() - c1.getLap();
 				}
 				if(c1.getLastCheckpointIndex() != c2.getLastCheckpointIndex()){
 					return c2.getLastCheckpointIndex() - c1.getLastCheckpointIndex();
 				}
 				if(c1.getLastCheckpointTime() < c2.getLastCheckpointTime()){
 					return -1;
 				}
 				else if(c2.getLastCheckpointTime() < c1.getLastCheckpointTime()){
 					return 1;
 				}				
 				return 0;
 			}
 		});
 		
 		printRacePositions();
 		
 	}
 	
 	private void printRacePositions() {
 		System.out.println("-----------------------------------------");
 		for(int i=0; i<cars.size(); i++){
 			Car c = cars.get(i);
 			if(c.getType() == Car.CarType.PLAYER){
 				System.out.println("Place " + (i+1) +" = " + c);
 			}
 		}
 		System.out.println("-----------------------------------------");
 	}
 
 
 	public List<Car> getCars() {		
 		return cars;
 	}
 
 	public List<Checkpoint> getCheckpoints() {
 		return checkpoints;
 	}
 
 	public WayPointsLine getWayPointsLine() {		
 		return wayPointsLine;
 	}
 	
 	public float getWallSensorRange() {
 		return wallSensorRange;
 	}
 
 	public float getWayPointRange() {
 		return wayPointRange;
 	}
 
 	public int getTotalLaps() {
 		return totalLaps;
 	}
 
 
 	public void finishRace() {
 		this.raceFinished = true;
 	}
 
 
 	public boolean isRaceFinished() {
 		return raceFinished;
 	}
 
 	
 	
 }
