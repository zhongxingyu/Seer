 package maze;
 
 import java.awt.Point;
 import java.awt.geom.Point2D;
 import java.io.Serializable;
 import draw.StdDraw;
 
 /*************************************************************************
  * Maze.java
  * 
  * @Author:			Devin Barry
  * @Date:			23.10.2012
  * @LastModified: 	29.10.2012
  * 
  * The maze class contains the data structures and methods for generating
  * and solving a maze.
  *************************************************************************/
 public class Maze implements Serializable {
 	
 	/**
 	 * Randomly generated serialVersionUID. Used during serialisation
 	 * of this class.
 	 */
 	private static final long serialVersionUID = 242017991465217780L;
 
 	//This the size of the maze. The maze is always square
 	public int size;
 
 	// These are the data structures for the maze
 	// Each two dimensional array is the size of the maze + 2
 	private boolean[][] north; // is there a wall to north of cell i, j
 	private boolean[][] east;
 	private boolean[][] south;
 	private boolean[][] west;
 
 	// this is used for generation algorithm and for the solving algorithm
 	private boolean[][] visited;
 	
 	// These are only used during maze solving
 	private boolean foundTarget;
 	private Point target;
 
 	//This is determines how fast the solver solves during draw mode
 	private static final int SOLVER_SPEED = 30; // lower is faster
 
 	// centrePoint is the location of the centre of the maze
 	private Point2D.Double centrePoint;
 	private int centreSize; //the size of square at the centre
 	//the start and end points for the centre square
 	private int centreMin, centreMax;
 	
 	//contains the positions of all the motion sensors
 	MotionSensors ms;
 	
 	//contains the positions of all the switches
 	private Point[] switches;
 	
 	
 
 	/**
 	 * The constructor for Maze only takes the size
 	 * of the maze.
 	 * 
 	 * @param size
 	 */
 	public Maze(int size) {
 		this.size = size;
 		target = new Point(size, 1);
 
 		visited = new boolean[size + 2][size + 2];
 		north = new boolean[size + 2][size + 2];
 		east = new boolean[size + 2][size + 2];
 		south = new boolean[size + 2][size + 2];
 		west = new boolean[size + 2][size + 2];
 		
 		init();
 		calculateCentre();
 		createSwitches(); //needs to be done after centre is created
 		
 		 //create 15 motion sensors, but don't create any in the centre
 		ms = new MotionSensors(size, 15, centreMin, centreMax);
 		//System.out.println(ms); //print motion sensor coordinates
 		
 		createSubmarineMaze();//generates maze for the submarine
 	}
 
 	
 	/**
 	 * Performs calculations to find the centre point of the maze
 	 * and to calculate how big the square in the middle should be.
 	 * This square is always one fifth of the width/height of the
 	 * maze.
 	 */
 	private void calculateCentre() {
 		// The size of the centre is presently one 5th of the maze size
 		centreSize = size / 5;
 		// System.out.println("centreSize is: " + centreSize);
 
 		// Min and max are the start points of the centre
 		int min = (int) Math.ceil((centreSize * 2.0));
 		int max = min + centreSize;
 		min++; // this adjusts for the border
 		max++;
 		// System.out.println("min is: " + min);
 		// System.out.println("max is: " + max);
 
 		// centre is the centre point of the maze
 		double centre = (centreSize / 2.0) + min;
 		// System.out.println("centre is: " + centre);
 		centrePoint = new Point2D.Double(centre, centre);
 		
 		//set the min and max points for the centre
 		centreMin = min;
 		centreMax = max;
 	}
 
 	/**
 	 * Initialise the data structures of the maze before attempting
 	 * to generate it
 	 */
 	private void init() {
 		// Initialise border cells as already visited
 		for (int x = 0; x < size + 2; x++) {
 			visited[x][0] = true;
 			visited[x][size + 1] = true;
 		}
 		for (int y = 0; y < size + 2; y++) {
 			visited[0][y] = true;
 			visited[size + 1][y] = true;
 		}
 		
 		
 		// Initialise all walls as present
 		for (int x = 0; x < size + 2; x++) {
 			for (int y = 0; y < size + 2; y++) {
 				north[x][y] = true;
 				east[x][y] = true;
 				south[x][y] = true;
 				west[x][y] = true;
 			}
 		}
 	}
 	
 	/**
 	 * Sets the positions of the 4 power switches
 	 */
 	private void createSwitches() {
 		switches = new Point[4];
 		switches[0] = new Point(centreMin, centreMin);
 		switches[1] = new Point(centreMin, centreMax-1);
 		switches[2] = new Point(centreMax-1, centreMin);
 		switches[3] = new Point(centreMax-1, centreMax-1);
 	}
 	
 	/**
 	 * The maze used on the submarine has a block in the
 	 * centre which contains no maze. This method creates
 	 * a full maze throughout the window and then cuts a
 	 * square out of the centre.
 	 * 
 	 * The maze generation algorithm starts to generate
 	 * the maze from the lower left corner.
 	 */
 	private void createSubmarineMaze() {
 		generate(1, 1); // generate the maze
 
 		// delete centre
 		for (int i = centreMin; i < centreMax; i++) {
 			for (int j = centreMin; j < centreMax; j++) {
 				//walls exist from both sides, ensure that both sides are drawn
 				if (j != centreMax-1) north[i][j] = false;
 				if (j != centreMin) south[i][j] = false;
 			}
 		}
 		for (int i = centreMin; i < centreMax; i++) {
 			for (int j = centreMin; j < centreMax; j++) {
 				if (i != centreMax-1) east[i][j] = false;
 				if (i != centreMin) west[i][j] = false;
 			}
 		}
 	
 		/*
 		 * draw centre
 		 * 
 		 * // delete some random walls for (int i = 0; i < N * 100; i++) { int x
 		 * = (int) (1 + Math.random() * (N - 1)); int y = (int) (1 +
 		 * Math.random() * (N - 1)); north[x][y] = south[x][y + 1] = false; }
 		 * 
 		 * // add some random walls for (int i = 0; i < N * 1; i++) { int x =
 		 * (int) (N / 2 + Math.random() * (N / 2)); int y = (int) (N / 2 +
 		 * Math.random() * (N / 2)); east[x][y] = west[x + 1][y] = true; }
 		 */
 	}
 
 	
 	/**
 	 * Recursive algorithm for generating the maze
 	 * Calls itself until all squares have been visited
 	 * @param x
 	 * @param y
 	 */
 	private void generate(int x, int y) {
 		visited[x][y] = true;
 
 		// while there is an unvisited neighbour
 		while (!visited[x][y + 1] || !visited[x + 1][y] || !visited[x][y - 1]
 				|| !visited[x - 1][y]) {
 			// pick random neighbour
 			while (true) {
 				double r = Math.random();
 				if (r < 0.25 && !visited[x][y + 1]) {
 					north[x][y] = south[x][y + 1] = false;
 					generate(x, y + 1);
 					break;
 				} else if (r >= 0.25 && r < 0.50 && !visited[x + 1][y]) {
 					east[x][y] = west[x + 1][y] = false;
 					generate(x + 1, y);
 					break;
 				} else if (r >= 0.5 && r < 0.75 && !visited[x][y - 1]) {
 					south[x][y] = north[x][y - 1] = false;
 					generate(x, y - 1);
 					break;
 				} else if (r >= 0.75 && r < 1.00 && !visited[x - 1][y]) {
 					west[x][y] = east[x - 1][y] = false;
 					generate(x - 1, y);
 					break;
 				}
 			}
 		}
 	}
 
 	/**
 	 * This method draws the walls, centre, power switches
 	 * and sensors of the maze on its JFrame
 	 */
 	public void draw() {	
 		// Draw the centre of the maze
 		Point2D.Double centrePoint = GetCentrePoint();
 		// if size is a factor of 10, this will be a multiple of 2
 		int centreSize = getCentreSize();
 		StdDraw.setPenColor(StdDraw.BOOK_LIGHT_BLUE);
 		StdDraw.filledSquare(centrePoint.getX(), centrePoint.getY(), (centreSize / 2)-0.05);
 		//Draw all the power switches
 		drawPowerSwitch(switches[0].x, switches[0].y);
 		drawPowerSwitch(switches[1].x, switches[1].y);
 		drawPowerSwitch(switches[2].x, switches[2].y);
 		drawPowerSwitch(switches[3].x, switches[3].y);
 		
 		
 		// Get the data from the maze
 		boolean south[][] = getSouth();
 		boolean north[][] = getNorth();
 		boolean east[][] = getEast();
 		boolean west[][] = getWest();
 		boolean sensor[][] = ms.getMotionSensors();
 
 		//Draw the maze and motion sensors
 		for (int x = 1; x <= size; x++) {
 			for (int y = 1; y <= size; y++) {
 				StdDraw.setPenColor(StdDraw.BLACK);
 				if (south[x][y]) {
 					StdDraw.line(x, y, x + 1, y);
 				}
 				if (north[x][y]) {
 					StdDraw.line(x, y + 1, x + 1, y + 1);
 				}
 				if (west[x][y]) {
 					StdDraw.line(x, y, x, y + 1);
 				}
 				if (east[x][y]) {
 					StdDraw.line(x + 1, y, x + 1, y + 1);
 				}
 				if (sensor[x][y]) {
 					StdDraw.setPenColor(StdDraw.MAGENTA);
 					StdDraw.filledTriangle(x + 0.5, y + 0.4, 0.5);
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Draw a power switch in the maze block specified by x, y
 	 * @param x
 	 * @param y
 	 */
 	private void drawPowerSwitch(int x, int y) {
 		StdDraw.setPenColor(StdDraw.WHITE);
 		StdDraw.filledSquare(x + 0.5, y + 0.5, 0.5);
 		StdDraw.setPenColor(StdDraw.RED);
 		StdDraw.setPenRadius(.007);
 		StdDraw.arc(x + 0.5, y + 0.5, 0.45, 120, 60);
 		StdDraw.line(x + 0.5, y + 0.5, x + 0.5, y + 0.95);
 		StdDraw.setPenRadius();
 	}
 
 	/**
 	 * solve the maze using depth first search. This algorithm
 	 * is recursive and continues to call itself as it hunts
 	 * the maze for the destination point.
 	 * 
 	 * If draw is true this algorithm with draw its results in
 	 * real time on the maze. False for a quick solve.
 	 * 
 	 * @param x
 	 * @param y
 	 * @param draw
 	 */
 	private void solve(int x, int y, boolean draw) {
 		// these are the walls of the maze, maze solver should not go beyond
 		// walls
 		// if (x == 0 || y == 0 || x == N + 1 || y == N + 1) {
 		// return;
 		// }
 
 		// if we have already visited this position
 		// or if deep levels of the recursion have found the target
 		if (foundTarget || visited[x][y])
 			return;
 
 		// this position is new, mark it as visited
 		visited[x][y] = true;
 
 		if (draw) {
 			// Draw a blue dot to show we have been here
 			StdDraw.setPenColor(StdDraw.BLUE);
 			StdDraw.filledCircle(x + 0.5, y + 0.5, 0.25);
 			StdDraw.show(SOLVER_SPEED);
 		}
 
 		// if we have reached the maze solve target
 		if (x == target.x && y == target.y) {
 			foundTarget = true;
 		}
 
 		// Try go another place
 		if (!north[x][y])
 			solve(x, y + 1, draw);
 		if (!east[x][y])
 			solve(x + 1, y, draw);
 		if (!south[x][y])
 			solve(x, y - 1, draw);
 		if (!west[x][y])
 			solve(x - 1, y, draw);
 
 		if (foundTarget)
 			return;
 
 		if (draw) {
 			// Draw a grey dot to show we have backtracked
 			StdDraw.setPenColor(StdDraw.GRAY);
 			StdDraw.filledCircle(x + 0.5, y + 0.5, 0.25);
 			StdDraw.show(SOLVER_SPEED);
 		}
 	}
 
 	/**
 	 * Solve the maze starting from Point <start> and ending at Point <end>
 	 * 
 	 * @param start
 	 * @param end
 	 */
 	public boolean solve(Point2D.Double start, Point2D.Double end, boolean draw) {
 		return solve(new Point((int) start.getX(), (int) start.getY()),
 				new Point((int) end.getX(), (int) end.getY()), draw);
 	}
 
 	/**
 	 * Solve the maze starting from Point <start> and ending at Point <end>
 	 * 
 	 * @param start
 	 * @param end
 	 */
 	public boolean solve(Point start, Point2D.Double end, boolean draw) {
 		return solve(start, new Point((int) end.getX(), (int) end.getY()), draw);
 	}
 
 	/**
 	 * Solve the maze starting from Point <start> and ending at Point <end>
 	 * 
 	 * @param start
 	 * @param end
 	 */
 	public boolean solve(Point start, Point end, boolean draw) {
 		// Clear the visited array for solving
 		for (int x = 1; x <= size; x++) {
 			for (int y = 1; y <= size; y++) {
 				visited[x][y] = false;
 			}
 		}
 		foundTarget = false;
 		target = new Point(end);
 		solve(start.x, start.y, draw);
 		return foundTarget;
 	}
 	
 	
 /****************************************************************************
 	Getter Methods
 *****************************************************************************/
 	
 	public boolean[][] getNorth() {
 		return north;
 	}
 
 	public boolean[][] getSouth() {
 		return south;
 	}
 
 	public boolean[][] getEast() {
 		return east;
 	}
 
 	public boolean[][] getWest() {
 		return west;
 	}
 
 	public boolean[][] getVisited() {
 		return visited;
 	}
 
 	public Point2D.Double GetCentrePoint() {
 		// don't allow external sources to modify this Point
 		return new Point2D.Double(centrePoint.getX(), centrePoint.getY());
 	}
 
 	public int getCentreSize() {
 		return centreSize;
 	}
 
 	/**
 	 * Returns whether a wall exists in the direction given from the coordinates
 	 * (x,y)
 	 * 
 	 * @param direction
 	 * @param x
 	 * @param y
 	 * @return boolean whether a wall is at the position
 	 */
 	public boolean getWall(String direction, int x, int y) {
		if (x < size && x >= 0 && y < size && y >= 0) {
 			if (direction.equalsIgnoreCase("north")) {
 				return north[x][y];
 			}
 			if (direction.equalsIgnoreCase("south")) {
 				return south[x][y];
 			}
 			if (direction.equalsIgnoreCase("east")) {
 				return east[x][y];
 			}
 			if (direction.equalsIgnoreCase("west")) {
 				return west[x][y];
 			}
 			System.out.println("Error with getWall - invalid direction!");
 		}
 		else {
 			System.out.println("Error with getWall - index out of bounds!");
 		}
 		return false;
 	}
 	
 	/**
 	 * Returns whether a sensor exists at the given coordinates
 	 * (x,y)
 	 * 
 	 * @param x
 	 * @param y
 	 * @return boolean whether a sensor is at the position
 	 */
 	public boolean getSensor(int x, int y) {
		if (x < size && x >= 0 && y < size && y >= 0) {
 			boolean sensor[][] = ms.getMotionSensors();
 			return sensor[x][y];
 		}
 		else {
 			System.out.println("Error with getSensor - index out of bounds!");
 		}
 		return false;
 	}
 	
 	/**
 	 * Returns whether a switch exists at the given coordinates
 	 * (x,y)
 	 * 
 	 * @param direction
 	 * @param x
 	 * @param y
 	 * @return boolean whether a switch is at the position
 	 */
 	public boolean getSwitch(int x, int y) {
		if (x < size && x >= 0 && y < size && y >= 0) {
 			if (switches[0].x == x && switches[0].y == y) return true;
 			if (switches[1].x == x && switches[1].y == y) return true;
 			if (switches[2].x == x && switches[2].y == y) return true;
 			if (switches[3].x == x && switches[3].y == y) return true;
 			return false;
 		}
 		else {
 			System.out.println("Error with getSwitch - index out of bounds!");
 		}
 		return false;
 	}
 	
 	
 }
