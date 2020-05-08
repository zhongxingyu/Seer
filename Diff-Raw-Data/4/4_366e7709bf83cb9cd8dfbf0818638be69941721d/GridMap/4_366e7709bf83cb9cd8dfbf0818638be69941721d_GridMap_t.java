 package uk.ac.kcl.inf._5ccs2seg.Data;
 
 /**
  * The grid representation of the map stored in a 2D array (0 - Unexplored; 1 -
  * Free; 2 - Occupied; 3 - Garbage)
  * 
  * @author Adrian Bocai, John Murray
  */
 public class GridMap {
 
 	private final int[][] grid;
 	private final static int maxX = 200;
 	private final static int maxY = 200;
 	private final int cellsPerMeter = 4;
 
 	public GridMap() {
 
 		this.grid = new int[maxY][maxX];
 
 		// initialise grid to unexplored
 		for (int y = 0; y < maxY; y++) {
 			for (int x = 0; x < maxX; x++) {
 				grid[y][x] = 0;
 			}
 		}
 		/*this.grid = new int[][]{{0,2,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
 				{2,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
 				{2,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
 				{2,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
 				{2,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
 				{2,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
 				{2,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
 				{2,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
 				{2,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,2},
 				{2,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0}};*/
 	}
 
 	/**
 	 * Set the status of a cell, give array indexes
 	 * 
 	 * @param i
 	 *            - index of the row
 	 * @param j
 	 *            - index of the column
 	 * @param value
 	 *            - the status of that cell expressed as an integer
 	 */
 	public synchronized void setSts(int x, int y, int value) {
 		grid[y][x] = value;
 	}
 
 	/**
 	 * Set the status of a cell, give player/stage coordinates
 	 * 
 	 * @param x
 	 *            - x coordinate of cell
 	 * @param y
 	 *            - y coordinate of cell
 	 * @param value
 	 */
 	public synchronized void setSts(double x, double y, int value) {
 		int[] arr = coordToArrayIndexCalc(x, y);
 		grid[arr[1]][arr[0]] = value;
 	}
 
 	/**
 	 * Retrieves the status of a cell, by its array indexes
 	 * 
 	 * @param i
 	 *            - index of the row
 	 * @param j
 	 *            - index of the column
 	 * @return status of cell expressed as integer
 	 */
 	public synchronized int getSts(int x, int y) {
 		return grid[y][x];
 	}
 
 	/**
 	 * Retrieves the status of a cell, by its player/stage coordinates
 	 * 
 	 * @param x
 	 *            - x coordinate of cell
 	 * @param y
 	 *            - y coordinate of cell
 	 * @return status of cell expressed as integer
 	 */
 	public synchronized int getSts(double x, double y) {
 		int[] arr = coordToArrayIndexCalc(x, y);
 		//arr[1] is y, arr[0] is x
 		return grid[arr[1]][arr[0]];
 	}
 
 	/**
 	 * Returns the indexes of a cell that the coordinates reside in
 	 * 
 	 * @param x
 	 *            - x coordinate of cell
 	 * @param y
 	 *            - y coordinate of cell
 	 * @return an array with the coordinates of the cell
 	 */
 	public int[] coordToArrayIndexCalc(double x, double y) {
 		int[] indexes = new int[2];
 		int tempX = (int)(x * cellsPerMeter) + (maxX / 2) ;
 		int tempY = (int)(-y * cellsPerMeter) + (maxY / 2);
 		indexes[0] = tempX;
 		indexes[1] = tempY;
 		return indexes;
 	}
 	
 	/**
 	 * Returns the coordinates of a particular cell.
 	 * Assumes that whatever calls this method knows the array is [x,y]
 	 * 
 	 * @param x
 	 *            - x coordinate of cell
 	 * @param y
 	 *            - y coordinate of cell
 	 * @return an array with the coordinates of the cell in [x,y] format
 	 */
 	public double[] arrayIndexToCoordCalc(int x, int y) {
 		double[] indexes = new double[2];
		double tempX = ((x - (maxX / 2)) / cellsPerMeter);
		double tempY = (-(y- (maxY / 2)) / cellsPerMeter) ;
 		indexes[0] = tempX;
 		indexes[1] = tempY;
 		return indexes;
 	}
 
 	public int getMaxX() {
 		return maxX;
 	}
 
 	public int getMaxY() {
 		return maxY;
 	}
 
 	public int[][] getMap(){
 		return grid;
 	}
 }
