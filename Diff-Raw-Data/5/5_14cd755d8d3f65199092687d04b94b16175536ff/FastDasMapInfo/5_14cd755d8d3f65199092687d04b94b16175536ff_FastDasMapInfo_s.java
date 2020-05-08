 package agents;
 
 import java.util.PriorityQueue;
 import java.util.ArrayList;
 import java.util.Comparator;
 import java.util.LinkedList;
 import java.util.ListIterator;
 import java.util.Random;
 import java.lang.IllegalStateException;
 import java.lang.IllegalArgumentException;
 
 import pplanning.simviewer.model.GridCell;
 import pplanning.simviewer.model.GridDomain;
 import pplanning.simviewer.model.GridCoord;
 
 import au.rmit.ract.planning.pathplanning.entity.ComputedPlan;
 
 /**
  * A class to store metadata about the current map.
  *
  * Direct access to CellInfo instances cannot be provided to ensure that a cell
  * is always present in the open queue when it is in the open set.
  *
  * Nodes that are in the open set cannot be altered. Later this will probably
  * need to be changed, but for standard A* this is fine.
  *
  * I have used assertions instead of exceptions for speed (since we can disable
  * them on run).
  */
 public class FastDasMapInfo implements Comparator<GridCell> {
 
 	// Used in the case of tiebreakers after f and h.
 	static Random rand = new Random(System.nanoTime());
 	// Added this data for debugging - probably not useful in our actual algorithm,
 	// is just to aid analysis.
 	GridDomain map;
 
 	private int closedCount = 0;
 
 	// Priority queues for open and pruned sets.
 	private PriorityQueue<GridCell> openQueue;
 	private PriorityQueue<GridCell> prunedQueue;
 
 	// Cell properties.
 	private CellSetMembership[][] sets;
 	private GridCell[][]          parents;
 	private float[][]             gCosts;
 	private float[][]             hCosts;
 	private int[][]               dCheapestRaws;
 	private float[][]             dCheapestWithErrors;
 	private int[][]               dErrors;
 	private int[][]               expansionNumbers;
 	private int[][]               cumulativeErrors;
 	private int[][]               depths;
 
 	private final float EPSILON = 0.001f; // used for floating point comparisons
 	private final int INITIAL_QUEUE_CAPACITY = 11;
 
 	public FastDasMapInfo(GridDomain map) {
 		this.map = map;
 		int width = map.getWidth();
 		int height = map.getHeight();
 
 		this.sets             		= new CellSetMembership[width][height];
 		this.gCosts           		= new float[width][height];
 		this.hCosts           		= new float[width][height];
 		this.parents          		= new GridCell[width][height];
 		this.dCheapestRaws    		= new int[width][height];
 		this.dCheapestWithErrors 	= new float[width][height];
 		this.dErrors          		= new int[width][height];
 		this.expansionNumbers 		= new int[width][height];
 		this.cumulativeErrors 		= new int[width][height];
 		this.depths           		= new int[width][height];
 
 		// Initialize queues for open and pruned sets.
 		this.openQueue = new PriorityQueue<GridCell>(INITIAL_QUEUE_CAPACITY, this);
 		this.prunedQueue = new PriorityQueue<GridCell>(INITIAL_QUEUE_CAPACITY, this);
 	}
 
 	public ComputedPlan computePlan(GridCell goal)
 	{
 
 		//Trace.print("Generating new incumbent plan...");
 
 		ComputedPlan plan = new ComputedPlan();
 
 		GridCell cell = goal;
 		while (cell != null) {
 			plan.prependStep(cell);
 			cell = getParent(cell);
 		}
 
 		plan.setCost(getGCost(goal));
 		return plan;
 	}
 
 	public void addStartCell(GridCell cell, float hCost, int dCheapestRaw) {
 		// Start cell has zero gCost, and no parent.
 		add(cell, 0f, hCost, dCheapestRaw, 0, null);
 	}
 
 	/**
 	 * Add cell to open set. This will fail if cell has already been added.
 	 * @param cell            the cell
 	 * @param gCost           the cost to get to the cell
 	 * @param hCost           the heuristic estimate to get to the goal
 	 * @param dCheapestRaw    the estimated goaldepth from the cell
 	 * @param expansionNumber the number of expansions performed before this cell
 	 *                        was generated
 	 * @param parent          the previous cell in a path
 	 */
 	public void add(GridCell cell, float gCost, float hCost, int dCheapestRaw,
 			int expansionNumber, GridCell parent)
 	{
 		// Should only be called when no info exists for node.
 		CellSetMembership prevSet = getSetMembership(cell);
 		if (prevSet != CellSetMembership.NONE) {
 			throw new IllegalArgumentException("Cannot add cell " + cell +
 					", which has already been added as to the " + prevSet + ".");
 		}
 
 		int x = cell.getCoord().getX();
 		int y = cell.getCoord().getY();
 
 		// Set cell properties
 		gCosts[x][y] = gCost;
 		hCosts[x][y] = hCost;
 		
 		dCheapestRaws[x][y] = dCheapestRaw;
 		expansionNumbers[x][y] = expansionNumber;
 
 		// Calculate depth data based on parent
 		parents[x][y] = parent;
 		if (parent != null) {
 			depths[x][y] = getDepth(parent) + 1;
			dErrors[x][y] = dCheapestRaw - getDCheapestRaw(parent) + 1;
			cumulativeErrors[x][y] = getCumulativeError(parent);
 			dCheapestWithErrors[x][y] = calculateDCheapestWithError(cell);
 		} else {
 			dCheapestWithErrors[x][y] = dCheapestRaw;
 		}
 
 		// Add to open set.
 		sets[x][y] = CellSetMembership.OPEN;
 		openQueue.offer(cell);
 	}
 
 	public void reopenCell(GridCell cell, int expansionCount) {
 		int x = cell.getCoord().getX();
 		int y = cell.getCoord().getY();
 
 		// Update expansion number
 		expansionNumbers[x][y] = expansionCount;
 
 		// Add to open set
 		sets[x][y] = CellSetMembership.OPEN;
 		openQueue.offer(cell);
 	}
 
 	/**
 	 * Returns true if there are no cells in the open set.
 	 * @return true if there are no cells in the open set
 	 */
 	public boolean isOpenEmpty() {
 		return (openQueue.size() == 0);
 	}
 
 	/**
 	 * Returns the number of cells in the open set.
 	 * @return the number of cells in the open set
 	 */
 	public int openCount() {
 		return openQueue.size();
 	}
 
 	/*
 	 * This function just points out that we need a structure for closed list.
 	 * It is only used for debugging of the output of closed list.
 	 */
 	public int closedCount() {
 		return closedCount ;
 	}
 
 	/**
 	 * Move cheapest open cell to the closed set and return it.
 	 * @return the cell formerly the cheapest from the open set
 	 */
 	public GridCell closeCheapestOpen()
 	{
 		GridCell cell = openQueue.poll();
 
 		if (cell == null) {
 			throw new IllegalStateException(
 					"Open set is empty - cannot close cheapest open cell");
 		}
 
 		GridCoord gc = cell.getCoord();
 		sets[gc.getX()][gc.getY()] = CellSetMembership.CLOSED;
 		closedCount++;
 
 		return cell;
 	}
 
 	/**
 	 * Move cell from closed list to pruned list. A cell should always be removed
 	 * from the open list before being pruned.
 	 * @param cell the cell to be pruned.
 	 */
 	public void pruneCell(GridCell cell) {
 		if (getSetMembership(cell) != CellSetMembership.CLOSED) {
 			throw new IllegalStateException("Cannot prune cell " + cell +
 					" - not in closed set.");
 		}
 
 		// Remove from open set.
 		closedCount--;
 
 		// Set set attribute to pruned.
 		GridCoord gc = cell.getCoord();
 		sets[gc.getX()][gc.getY()] = CellSetMembership.PRUNED;
 
 		// Add to pruned priority queue.
 		prunedQueue.offer(cell);
 
 	}
 
 	public boolean isPrunedEmpty()
 	{
 		return prunedQueue.isEmpty();
 	}
 
 	/**
 	 * Return a selection of pruned states to the open set. The number of states
 	 * moved is the number that is estimated can be explored with the given
 	 * number of expansions.
 	 * @param expansions the number of expansions to limit the number of nodes
 	 *                   recovered
 	 */
 	public void recoverPrunedStates(int expansionCount) {
 		// the sum of d^cheapest for each cell reopened
 		int dSum = 0;
 		int count = 0;
 
 		while (dSum < expansionCount && prunedQueue.size() > 0) {
 			GridCell cell = prunedQueue.poll();
 
 			dSum += getDCheapestWithError(cell);
 
 			// Set set attribute to opened.
 			GridCoord gc = cell.getCoord();
 			sets[gc.getX()][gc.getY()] = CellSetMembership.OPEN;
 
 			// Add to opened priority queue.
 			openQueue.offer(cell);
 			count++;
 //			System.out.println("Recovering " + cell);
 		}
 //		System.out.println("Recovered " + count + " nodes");
 	}
 
 	public GridCell getParent(GridCell cell) {
 		GridCoord gc = cell.getCoord();
 		return parents[gc.getX()][gc.getY()];
 	}
 
 	public void setParent(GridCell cell, GridCell parent) {
 		GridCoord gc = cell.getCoord();
 		parents[gc.getX()][gc.getY()] = parent;
 	}
 
 	public float getFCost(GridCell cell) {
 		return getGCost(cell) + getHCost(cell);
 	}
 
 	public float getGCost(GridCell cell) {
 		GridCoord gc = cell.getCoord();
 		return gCosts[gc.getX()][gc.getY()];
 	}
 
 	private void setQueuedGCost(GridCell cell, float gCost, PriorityQueue<GridCell> queue) {
 		// Remove node from priority queue. (Changing its g cost in place would
 		// cause the heaps to become unsorted.)
 		boolean wasPresent = queue.remove(cell);
 
 		// Ensure that it was indeed in the open set.
 		if (wasPresent == false) {
 			throw new IllegalArgumentException("Cell was not found priority queue!");
 		}
 
 		// Update g cost.
 		GridCoord gc = cell.getCoord();
 		gCosts[gc.getX()][gc.getY()] = gCost;
 
 		// Reinsert node sorted.
 		queue.offer(cell);
 	}
 
 	public void setGCost(GridCell cell, float gCost) {
 		switch (getSetMembership(cell)) {
 			case CLOSED: {
 				GridCoord gc = cell.getCoord();
 				gCosts[gc.getX()][gc.getY()] = gCost;
 				break;
 			}
 			case OPEN: {
 				setQueuedGCost(cell, gCost, openQueue);
 				break;
 			}
 			case PRUNED: {
 				setQueuedGCost(cell, gCost, prunedQueue);
 				break;
 			}
 			case NONE: {
 				throw new IllegalArgumentException("Cell is not in a set.");
 			}
 			default: {
 				assert false;
 			}
 		}
 	}
 
 	/**
 	 * Get heuristic cost estimate from this cell to the goal.
 	 * NOTE: We don't need write access to this for this assignment.
 	 * @param cell the cell h is estimated from
 	 */
 	public float getHCost(GridCell cell) {
 		GridCoord gc = cell.getCoord();
 		return hCosts[gc.getX()][gc.getY()];
 	}
 
 	private int getDepth(GridCell cell) {
 		GridCoord gc = cell.getCoord();
 		return depths[gc.getX()][gc.getY()];
 	}
 
 	private int getDCheapestRaw(GridCell cell) {
 		GridCoord gc = cell.getCoord();
 		return dCheapestRaws[gc.getX()][gc.getY()];
 	}
 
 	private int getCumulativeError(GridCell cell) {
 		GridCoord gc = cell.getCoord();
 		return cumulativeErrors[gc.getX()][gc.getY()];
 	}
 	
 	public float calculateDCheapestWithError(GridCell cell)
 	{
 		float avgError = getAverageError(cell);
 		float dCheapestWithError;
 		
 		if (avgError < 1.0f - EPSILON) 
 		{
 			float dCheapest = (float) getDCheapestRaw(cell);
 			 dCheapestWithError = dCheapest / (1.0f - avgError);
 		}
 		else
 			dCheapestWithError = Float.POSITIVE_INFINITY;
 		
 		//System.out.println("calculate dCheapestWithError = " + dCheapestWithError);
 		return(dCheapestWithError);
 		
 	}
 
 	// TODO: consider caching this value
 	public float getDCheapestWithError(GridCell cell) {
 
 		GridCoord gc = cell.getCoord();
 		return(dCheapestWithErrors[gc.getX()][gc.getY()]);
 	}
 
 	/**
 	 * Get average single step error.
 	 * TODO: consider caching this value
 	 */
 	private float getAverageError(GridCell cell) {
 		if (getDepth(cell) == 0) {
 			return 0;
 		}
 		return (float)(getCumulativeError(cell) / getDepth(cell));
 	}
 
 	public int getExpansionNumber(GridCell cell) {
 		GridCoord gc = cell.getCoord();
 		return expansionNumbers[gc.getX()][gc.getY()];
 	}
 
 	/**
 	 * Get set that this cell is currently in.
 	 * @param cell the cell to check
 	 * @return the set that the cell is in, or null if it has not been added.
 	 */
 	public CellSetMembership getSetMembership(GridCell cell) {
 		GridCoord gc = cell.getCoord();
 		CellSetMembership set = sets[gc.getX()][gc.getY()];
 		return set == null ? CellSetMembership.NONE : set;
 	}
 
 	/**
 	 * Has this cell been added yet?
 	 * @param cell the cell to check
 	 * @return true if the cell belongs to any set, otherwise false
 	 */
 	public boolean cellExists(GridCell cell) {
 		GridCoord gc = cell.getCoord();
 		return (sets[gc.getX()][gc.getY()] != null);
 	}
 
 	/**
 	 * Check if cell is in open set.
 	 * @param cell the cell to check
 	 * @return true if cell is in open set, otherwise false
 	 */
 	public boolean isOpen(GridCell cell) {
 		return getSetMembership(cell) == CellSetMembership.OPEN;
 	}
 
 	/**
 	 * Check if cell is in closed set.
 	 * @param cell the cell to check
 	 * @return true if cell is in closed set, otherwise false
 	 */
 	public boolean isClosed(GridCell cell) {
 		return getSetMembership(cell) == CellSetMembership.CLOSED;
 	}
 
 	/**
 	 * Check if cell is in pruned set.
 	 * @param cell the cell to check
 	 * @return true if cell is in pruned set, otherwise false
 	 */
 	public boolean isPruned(GridCell cell) {
 		return getSetMembership(cell) == CellSetMembership.PRUNED;
 	}
 
 	/* -- GRID CELL COMPARATOR -- */
 
 	/**
 	 * Perform an approximate comparison of two floating point values.
 	 * @param a value 1
 	 * @param b value 2
 	 * @return 0, -1 or 1 if a is equal to, less than or greater than b respectively
 	 */
 	private int compareFloat(float a, float b) {
 		if (Math.abs(a - b) < EPSILON) {
 			return 0;
 		}
 		return (a > b) ? 1 : -1;
 	}
 
 	/**
 	 * Perform an approximate comparison of two floating point values. Compares
 	 * cells on their f cost, breaking ties on h.
 	 * @param a grid cell 1
 	 * @param b grid cell 2
 	 * @return 0, -1 or 1 if a is equal to, less than or greater than b respectively
 	 */
 	public int compare(GridCell a, GridCell b) {
 
 		// Compare total cost estimate.
 		int fCompare = compareFloat(getFCost(a), getFCost(b));
 		if (fCompare != 0) {
 
 			return fCompare;
 		}
 		//System.out.println("TIE BREAK on F" + a + b);
 
 		// Break ties on heuristic estimate.
 		int hCompare = compareFloat(getHCost(a), getHCost(b));
 		if (hCompare != 0)
 		{
 
 			return hCompare;
 		}
 //		System.out.println("TIE BREAK on H" + a + b);
 		
 
 		int min = 1;
 		int max = 2;		
 		int randomNum = rand.nextInt(max - min + 1) + min;
 		//System.out.println(randomNum);
 		if (randomNum == 1)
 			return(1);
 		else
 			return(-1);
 		
 	}
 
 	public boolean equals() { return false; }
 
 	/* -- DEBUG -- */
 
 	/** Return an ArrayList of all the GridCells currently in the closed set. */
 	public ArrayList<GridCell> getClosedArrayList() {
 		ArrayList<GridCell> closed = new ArrayList<GridCell>(closedCount);
 		for (int x = 0; x < map.getWidth(); x++) {
 			for (int y = 0; y < map.getHeight(); y++) {
 				if (sets[x][y] == CellSetMembership.CLOSED) {
 					closed.add(map.getCell(x, y));
 				}
 			}
 		}
 		return closed;
 	}
 
 	/** Return an ArrayList of all GridCells currently in the open set. */
 	public ArrayList<GridCell> getOpenArrayList() {
 		return new ArrayList<GridCell>(openQueue);
 	}
 
 	/** Return an ArrayList of all GridCells currently in the pruned set. */
 	public ArrayList<GridCell> getPrunedArrayList() {
 		return new ArrayList<GridCell>(prunedQueue);
 	}
 }
