 package dinaBOT.navigation;
 
 //leJOS imports
 import lejos.nxt.*;
 import java.lang.Math;
 
 //dinaBOT imports
 import dinaBOT.mech.*;
 import dinaBOT.sensor.*;
 import dinaBOT.util.DinaList;
 
 /**
  *
  *
  * @author Stepan Salenikovich, Severin Smith
  * @see Navigation
  * @see Navigator
  * @see Pathing
  * @see Astar
  * @see USSensorListener
  * @see USSensor
  * @version 3
 */
 public class Map implements MechConstants, USSensorListener {
 
 	Odometer odo;
 
 	int[][] map;
 	int X, Y;
 
 	double nodeDist;
 	int threshold;
 
 	int[] high_Readings;
 	int[] low_Readings;
 
 	boolean newObstacle;
 
 	DinaList<MapListener> listeners;
 
 	boolean stop;
 
 	// square map constructor 
 	public Map(Odometer odo, int rez, int threshold, double nodeDist) {
 		this(odo, rez, rez, threshold, nodeDist);
 	}
 	
 	//rectangular map constructor
 	public Map(Odometer odo, int rezX, int rezY , int threshold, double nodeDist) {
 		this.odo = odo;
 		this.X = rezX;
 		this.Y = rezY;
 
 		this.threshold = threshold;
 		this.map = new int[X][Y];
 
 		this.nodeDist = nodeDist;
 
 		this.newObstacle = false;
 
 		this.start();
 
 		listeners = new DinaList<MapListener>();
 
 		//initialize border
 		for(int x = 0; x < X; x++) map[x][0] = map[x][Y-1] = WALL;
 		for(int y = 0; y < Y; y++) map[0][y] = map[X-1][y] = WALL;
 
 
 		low_Readings = new int[] {255,255,255,255,255,255,255,255};
 		high_Readings = new int[] {255,255,255,255,255,255,255,255};
 
 		USSensor.high_sensor.registerListener(this);
 		USSensor.low_sensor.registerListener(this);
 	}
 
 	public void registerListener(MapListener listener) {
 		listeners.add(listener);
 	}
 
 	void notifyListeners(int x, int y) {
 		for(int i = 0;i < listeners.size();i++) {
 			listeners.get(i).newObstacle(x,y);
 		}
 	}
 
 	public double[] getUSCoord(int distance) {
 		double[] pos = new double[3];
 		double[] coord = new double[2];
 
 		pos = odo.getPosition();
 
 		coord[0] = Math.cos(pos[2])*distance + pos[0];
 		coord[1] = Math.sin(pos[2])*distance + pos[1];
 
 		// make sure there are no negative coords
 		if (coord[0] < 0) {
 			coord[0] = 0;
 		} else if (coord[0] > (X - 1) * nodeDist) coord[0] = (X -1)*nodeDist;
 		if (coord[1] < 0) {
 			coord[1] = 0;
 		} else if (coord[1] > (Y - 1) * nodeDist) coord[1] = (Y -1)*nodeDist;
 
 		return coord;
 
 	}
 
 	public double[] getUSCoord(int distance, double angle) {
 		double[] pos = new double[3];
 		double[] coord = new double[2];
 
 		pos = odo.getPosition();
 
 		pos[2] = angle;
 
 		coord[0] = Math.cos(pos[2])*distance + pos[0];
 		coord[1] = Math.sin(pos[2])*distance + pos[1];
 
 		// make sure there are no negative coords
 		if (coord[0] < 0) {
 			coord[0] = 0;
 		} else if (coord[0] > (X - 1) * nodeDist) coord[0] = (X -1)*nodeDist;
 		if (coord[1] < 0) {
 			coord[1] = 0;
 		} else if (coord[1] > (Y - 1) * nodeDist) coord[1] = (Y -1)*nodeDist;
 
 		return coord;
 
 	}
 
 	public boolean checkUSCoord(double distance, double angle) {
 		double[] pos = new double[3];
 		double[] coord = new double[2];
 
 		pos = odo.getPosition();
 
 		pos[2] = angle;
 
 		coord[0] = Math.cos(pos[2])*distance + pos[0];
 		coord[1] = Math.sin(pos[2])*distance + pos[1];
 
 		// make sure there are no negative coords
 		if (coord[0] < 0
 					|| coord[0] > (X - 1) * nodeDist
 					|| coord[1] < 0
 					|| coord[1] > (Y - 1) * nodeDist) {
 						return false;
 		} else if(map[(int)Math.round(coord[0]/UNIT_TILE)][(int)Math.round(coord[1]/UNIT_TILE)] > 0) {
 			return false;
 		} else {
 			return true;
 		}
 
 	}
 	
 	public int[] getNode(double[] coord) {
 		int[] node = new int[2];
 
 		node[0] = (int)Math.round(coord[0]/nodeDist);
 		node[1] = (int)Math.round(coord[1]/nodeDist);
 
 		return node;
 	}
 	
 	public boolean checkNodeBounds(int[] node) {
 		if( node[0] < 0 || node[0] > X - 1
 						|| node[1] < 0
 						|| node[1] > Y - 1) return false;
 		else return true;
 	}
 	
 	public boolean checkNode(int[] node) {
 		if(!checkNodeBounds(node)) return false;
 		else if ( map[node[0]][node[1]] > DANGER ) return false;
 		else return true;
 	}
 	
 	public int nodeValue(int[] node) {
 		if(!checkNodeBounds(node)) return -1;
 		else return map[node[0]][node[1]];
 	}
 	
 	public int coordValue(double[] coord) {
 		int[] node = getNode(coord);
 		if(!checkNodeBounds(node)) return -1;
 		else return map[node[0]][node[1]];
 	}
 
 	public int[][] getMap() {
 
 		// FIX THIS SO IT RETURNS A COPY OF THE ARRAY
 
 		return map;
 	}
 
 	public int[] getRez() {
 		return new int[] {X,Y};
 	}
 
 	public void newValues(int[] new_values, USSensor sensor) { //This is only called by the high sensor because we didn't register with the low one
 		double[] coord = new double[2];
 		int[] node = new int[2];
 		int distance = 255;
 		int[] curr_node = new int[2];	//node at which the robot is currently at
 		double[] curr_coord = new double[2];	// coordinate at which the robot is currently at
 		int minLow, minHigh;
 
 		// checks stop bool
 		if(stop) return;
 
 		// only care about high US sensor values
 		if(sensor == USSensor.low_sensor) low_Readings = new_values;
 		else if (sensor == USSensor.high_sensor) high_Readings = new_values;
 		else return; //should never happen
 
 		minLow = low_Readings[0];
 		minHigh = high_Readings[0];
 
 		distance = high_Readings[0];
 		/*if(minLow < minHigh
 					&& minHigh < threshold
 					&& (minHigh - minLow) < 2) {
 
 			distance = high_Readings[0];
 
 		} else distance = 255;
 		*/
 
 		// if ostacle distance is close enough, mark appropriate node
 		if (distance < threshold) {
 
 			// get abs. coords from relative distance
 			coord = getUSCoord(distance);
 			curr_coord = getUSCoord(0);
 
 			// get node associated with coords
 			node = getNode(coord);
 			curr_node = getNode(curr_coord);
 
 
 
 			// if obstacle is not detected as in current node
			//if(!((node[0] == curr_node[0]) && (node[1] == curr_node[1]))) {
 
 
 				// mark map with obstacle
 				/* 10 = WALL
 				 * 5 = DROP ZONE
 				 * 3 = OBSTACLE
 				 * 2 = DANGER ZONE
 				 * 0 = clear
 				 * 20 = drop off area corner?? -not yet decided
 				 */
				if(map[node[0]][node[1]] < OBSTACLE) {
 					Sound.twoBeeps();
 					
 					//mark obstacle
 					map[node[0]][node[1]] = OBSTACLE;
 					
 					//mark danger zone(s) all those adjacent to obstacle except the one behind the obstacle
 					if(checkNode(new int[] {node[0] + 1, node[1]})) map[node[0] + 1][node[1]] = DANGER;
 					if(checkNode(new int[] {node[0] - 1, node[1]})) map[node[0] - 1][node[1]] = DANGER;
 					if(checkNode(new int[] {node[0], node[1] + 1})) map[node[0]][node[1] + 1] = DANGER;
 					if(checkNode(new int[] {node[0], node[1] - 1})) map[node[0]][node[1] - 1] = DANGER;
 					
 					notifyListeners(node[0], node[1]);
 				}
			//}
 		} else {
 			// mark nodes within threshold as clear
 			/*distance = threshold;
 			for(int i = 2; threshold >= UNIT_TILE; i++) {
 
 				coord = getUSCoord(distance);
 				node = getNode(coord);
 
 				if(map[node[0]][node[1]] > 0) {
 					map[node[0]][node[1]] = 0;
 				}
 
 				distance = threshold/i;
 			}*/
 		}
 
 	}
 
 	//Do not pass reference
 	public synchronized boolean editMap(int x, int y, int value) {
 		this.map[x][y] = value;
 
 		return true;	//sucess
 	}
 
 	private synchronized void newObstacleSet(boolean set) {
 		newObstacle = set;
 	}
 
 	public synchronized boolean obstacleCheck() {
 		if(newObstacle) {
 			newObstacle = false;
 			return true;
 		} else return false;
 	}
 
 	public synchronized void stop() {
 		stop = true;
 	}
 
 	public synchronized void start() {
 		stop = false;
 	}
 	
 	public void reset() {
 		this.stop();
 		for(int x = 0; x < X; x++) {
 			for(int y = 0; y < Y; y++) {
 				if(this.map[x][y] != DROP_ZONE && this.map[x][y] != WALL) this.map[x][y] = 0;
 			}
 		}
 		this.start();
 	}
 
 }
