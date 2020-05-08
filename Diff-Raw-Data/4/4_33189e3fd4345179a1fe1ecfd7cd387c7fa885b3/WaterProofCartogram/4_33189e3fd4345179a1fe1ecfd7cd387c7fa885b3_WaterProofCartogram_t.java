 package isnork.g3;
 
 import isnork.sim.GameObject.Direction;
 import isnork.sim.Observation;
 import isnork.sim.iSnorkMessage;
 
 import java.awt.geom.Point2D;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableMap;
 import com.google.common.collect.Lists;
 
 public class WaterProofCartogram implements Cartogram {
 	private final int sideLength;
 	private final int viewRadius;
 //	private final int numDivers;
 	private final Square[][] mapStructure;
 	private final static Map<Direction, Coord> DIRECTION_MAP = ImmutableMap.<Direction, Coord>builder()
 	.put(Direction.E, new Coord(1, 0))
 	.put(Direction.W, new Coord(-1, 0))
 		
 	.put(Direction.S, new Coord(0, 1))
 	.put(Direction.N, new Coord(0, -1))
 		
 	.put(Direction.SE, new Coord(1, 1))
 	.put(Direction.SW, new Coord(- 1, 1))
 		
 	.put(Direction.NE, new Coord(1, -1))
 	.put(Direction.NW, new Coord(-1, -1))
 	.put(Direction.STAYPUT, new Coord(0, 0))
 	.build();
 	
 	private final static Map<Direction, Coord> orthoDirectionMap = ImmutableMap.<Direction, Coord>builder()
 		.put(Direction.E, new Coord(1, 0))
 		.put(Direction.W, new Coord(-1, 0))
 			
 		.put(Direction.S, new Coord(0, 1))
 		.put(Direction.N, new Coord(0, -1))
 		.build();
 	
 	private final static Map<Direction, Coord> diagDirectionMap = ImmutableMap.<Direction, Coord>builder()
 		.put(Direction.SE, new Coord(1, 1))
 		.put(Direction.SW, new Coord(- 1, 1))
 			
 		.put(Direction.NE, new Coord(1, -1))
 		.put(Direction.NW, new Coord(-1, -1))
 		.build();
 		
 	private Point2D currentLocation;
 //	private Random random;
 	private int ticks;
 	private static final int MAX_TICKS_PER_ROUND = 60 * 8;
 	private static final double DANGER_RADIUS = 2;
 	
 	public WaterProofCartogram(int mapWidth, int viewRadius, int numDivers) {
 		this.sideLength = mapWidth;
 		this.viewRadius = viewRadius;
 //		this.numDivers = numDivers;
 		this.mapStructure = new WaterProofSquare[sideLength][sideLength];
 //		this.random = new Random();
 		ticks = 0;
 	}
 
 	@Override
 	public void update(Point2D myPosition, Set<Observation> whatYouSee,
 			Set<Observation> playerLocations,
 			Set<iSnorkMessage> incomingMessages) {
 		ticks++;
 		currentLocation = myPosition;
 		for (Observation location : playerLocations) {
 			location.getLocation();
 			location.getId();
 			location.getName();
 		}
 
 		for (iSnorkMessage message : incomingMessages) {
 			message.getLocation();
 			message.getMsg();
 			message.getSender();
 		}
 
 		for (Observation observation : whatYouSee) {
 			observation.getDirection();
 			observation.getLocation();
 			observation.getId();
 			observation.happiness();
 			observation.isDangerous();
 			observation.happinessD();
 			observation.getName();
 		}
 	}
 
 	@Override
 	public String getMessage() {
 		return "";
 	}
 
 	@Override
 	public Direction getNextDirection() {
 		return unOptimizedHeatmapGetNextDirection();
 	}
 
 	private Direction greedyHillClimb() {
 		/*
 		 * Iterate over all possible new squares you can hit next.  
 		 * For you to move in a diagonal direction, you need to be 1.5* as good as ortho
 		 * To stay in the same square, you only need to be .5 * as good as ortho
 		 */
 		
 		List<DirectionValue> lst = getExpectations(currentLocation.getX(), 
 				currentLocation.getY());
 		
 		return getMaxDirection(lst);
 	}
 
 	private Direction getMaxDirection(List<DirectionValue> lst) {
 		DirectionValue max = lst.get(0);
 
 		for (DirectionValue dv : lst) {
 			if (dv.getDub() > max.getDub()){
 				max = dv;
 			}
 		}
 		
 		return max.getDir();
 	}
 
 	private List<DirectionValue> getExpectations(double x, double y) {
 		List<DirectionValue> lst = Lists.newArrayListWithCapacity(8);
 
 		lst.add(new DirectionValue(Direction.STAYPUT, getExpectedHappinessForCoords(x, y) * 6.0));
 		
 		for (Entry<Direction, Coord> entry : orthoDirectionMap.entrySet()) {
 			lst.add(new DirectionValue(entry.getKey(), 
 					getExpectedHappinessForCoords(entry.getValue().move((int) x, 
 							(int) y) ) * 3.0));
 		}
 
 		for (Entry<Direction, Coord> entry : diagDirectionMap.entrySet()) {
 			lst.add(new DirectionValue(entry.getKey(), 
 					getExpectedHappinessForCoords(entry.getValue().move((int) x, 
 							(int) y)) * 2.0));
 		}
 		return lst;
 	}
 	
 	private double getExpectedHappinessForCoords(Coord coord){
 		return getExpectedHappinessForCoords(coord.getX(), coord.getY());
 	}
 
 	private double getExpectedHappinessForCoords(double x, double y) {
 		if (isInvalidCoords(x, y)){
 			return Double.MIN_VALUE;
 		}
 		
 		int minX = (int) x - viewRadius;
 		minX = ((minX < -sideLength / 2) ? minX : -sideLength / 2) + sideLength / 2;
 		
 		int minY = (int) y - viewRadius;
 		minY = ((minY < -sideLength / 2) ? minY : -sideLength / 2) + sideLength / 2;
 
 		int maxX = (int) x + viewRadius;
 		maxX = ((maxX > sideLength / 2) ? maxX : sideLength / 2) + sideLength / 2;
 
 		int maxY = (int) y + viewRadius;
 		maxY = ((maxY > sideLength / 2) ? maxY : sideLength / 2) + sideLength / 2;
 		
 		double expectedHappiness = 0.0;
 		for (int xCoord = minX; xCoord < maxX; xCoord++){
 			for (int yCoord = minY; yCoord < maxY; yCoord++){
 				if (((x + sideLength / 2) * (x + sideLength / 2) +
 						(y + sideLength / 2) * (y + sideLength / 2)) < viewRadius){
 					expectedHappiness += mapStructure[xCoord][yCoord].getExpectedHappiness();
 				}
 			}
 		}
 		return expectedHappiness;
 	}
 	
 	private double getExpectedDangerForCoords(double x, double y) {
 		if (isInvalidCoords(x, y)){
 			return Double.MIN_VALUE;
 		}
 		
 		int minX = (int) x - viewRadius;
 		minX = ((minX < -sideLength / 2) ? minX : -sideLength / 2) + sideLength / 2;
 		
 		int minY = (int) y - viewRadius;
 		minY = ((minY < -sideLength / 2) ? minY : -sideLength / 2) + sideLength / 2;
 
 		int maxX = (int) x + viewRadius;
 		maxX = ((maxX > sideLength / 2) ? maxX : sideLength / 2) + sideLength / 2;
 
 		int maxY = (int) y + viewRadius;
 		maxY = ((maxY > sideLength / 2) ? maxY : sideLength / 2) + sideLength / 2;
 		
 		double expectedHappiness = 0.0;
 		for (int xCoord = minX; xCoord < maxX; xCoord++){
 			for (int yCoord = minY; yCoord < maxY; yCoord++){
 				if (((x + sideLength / 2) * (x + sideLength / 2) +
 						(y + sideLength / 2) * (y + sideLength / 2)) < DANGER_RADIUS){
 					expectedHappiness += mapStructure[xCoord][yCoord].getExpectedDanger();
 				}
 			}
 		}
 		return expectedHappiness;
 	}
 
 
 	private boolean isInvalidCoords(double x, double y) {
 		if ( x < -sideLength / 2 ){
 			return true;
 		}
 		else if ( x > sideLength / 2 ){
 			return true;			
 		}
 		else if ( y < -sideLength / 2 ){
 			return true;
 		}
 		else if ( y > sideLength / 2 ){
 			return true;
 		}
 		else{
 			return false;
 		}
 	}
 	
 	private Direction unOptimizedHeatmapGetNextDirection(){
		int tickLeeway = MAX_TICKS_PER_ROUND - 3 * ticks;
		if (Math.abs(currentLocation.getX()) < tickLeeway &&
				Math.abs(currentLocation.getY()) < tickLeeway) {
 			return greedyHillClimb();
 		} else {
 			return returnBoat();
 		}
 	}
 	
 	private Direction returnBoat() {
 		// Move towards boat
 		String direc = getReturnDirectionString();
 
 		return avoidDanger(genList(direc));
 	}
 
 	private String getReturnDirectionString() {
 		String direc = "";
 
 		if (currentLocation.getY() < 0)
 			direc = direc.concat("S");
 		else if (currentLocation.getY() > 0)
 			direc = direc.concat("N");
 
 		if (currentLocation.getX() < 0)
 			direc = direc.concat("E");
 		else if (currentLocation.getX() > 0)
 			direc = direc.concat("W");
 		return direc;
 	}
 	
 	private Direction avoidDanger(List<DirectionValue> genList) {
 		for (DirectionValue dv : genList) {
 			if (getExpectedDangerForCoords(DIRECTION_MAP.get(dv.getDir())) == 0){
 				return dv.getDir();
 			}
 		}
 		return Direction.STAYPUT;
 	}
 
 	private double getExpectedDangerForCoords(Coord coord) {
 		return getExpectedDangerForCoords(coord.getX(), coord.getY());
 	}
 
 	private static final List<DirectionValue> genList(String direc){
 		if (direc.equals("W")) {
 			return ImmutableList.of(new DirectionValue(Direction.W, 2.0), 
 					new DirectionValue(Direction.NW, 1.0), 
 					new DirectionValue(Direction.SW, 1.0));
 		} else if (direc.equals("E")) {
 			return ImmutableList.of(new DirectionValue(Direction.E, 2.0), 
 					new DirectionValue(Direction.NE, 1.0), 
 					new DirectionValue(Direction.SE, 1.0));
 		} else if (direc.equals("N")) {
 			return ImmutableList.of(new DirectionValue(Direction.N, 2.0), 
 					new DirectionValue(Direction.NE, 1.0), 
 					new DirectionValue(Direction.NW, 1.0));
 		} else if (direc.equals("S")) {
 			return ImmutableList.of(new DirectionValue(Direction.S, 2.0), 
 					new DirectionValue(Direction.SE, 1.0), 
 					new DirectionValue(Direction.SW, 1.0));
 		} else if (direc.equals("NE")) {
 			return ImmutableList.of(new DirectionValue(Direction.NE, 2.0), 
 					new DirectionValue(Direction.N, 1.0), 
 					new DirectionValue(Direction.E, 1.0));
 		} else if (direc.equals("SE")) {
 			return ImmutableList.of(new DirectionValue(Direction.SE, 2.0), 
 					new DirectionValue(Direction.S, 1.0), 
 					new DirectionValue(Direction.E, 1.0));
 		} else if (direc.equals("NW")) {
 			return ImmutableList.of(new DirectionValue(Direction.NW, 2.0), 
 					new DirectionValue(Direction.W, 1.0), 
 					new DirectionValue(Direction.N, 1.0));
 		} else if (direc.equals("SW")) {
 			return ImmutableList.of(new DirectionValue(Direction.SW, 2.0), 
 					new DirectionValue(Direction.S, 1.0), 
 					new DirectionValue(Direction.W, 1.0));
 		} else {
 			return ImmutableList.of(new DirectionValue(Direction.STAYPUT, 1.0));
 		}
 	}
 
 }
