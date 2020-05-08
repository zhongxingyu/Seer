 package edu.agh.tunev.model.cellular.grid;
 
 import java.awt.Point;
 import java.awt.geom.Point2D;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Vector;
 
 import edu.agh.tunev.model.Common;
 import edu.agh.tunev.model.cellular.NeighbourIndexException;
 import edu.agh.tunev.model.cellular.agent.Person;
 import edu.agh.tunev.world.Exit;
 import edu.agh.tunev.world.Physics;
 
 public final class Cell {
 
 	/** side of a cell represented by square */
 	public final static double CELL_SIZE = 0.25;
 
 	/** Physics coefficient useful for static field value evaluation */
 	private final static double PHYSICS_COEFF = 0.0; // TODO: set
 
 	/** Distance coefficient useful for static field value evaluation */
 	private final static double DIST_COEFF = 1; // TODO: set
 
 	private final Board board;
 	private final Point position;
 	private Person person = null;
 	private Physics physics = null;
 	private Double staticFieldVal;
 	private Double distToExit;
 
 	public Cell(Point _position, Board _board) {
 		this.position = _position;
 		this.board = _board;
 		calculateDistToExit();
 	}
 
 	/**
 	 * <pre>
 	 * Sets new physics data.
 	 *  Calculates distance to the nearest exit.
 	 *  Evaluates the static field value.
 	 * </pre>
 	 * 
 	 * @param phys
 	 *            current physics data for this cell
 	 */
 	public void update(Physics phys) {
 		setPhysics(phys);
 		calculateDistToExit();
 		evaluateStaticFieldVal();
 	}
 
 	/**
 	 * Checks if a cell is occupied by {@code Person}.
 	 * 
 	 * @return
 	 */
 	public boolean isOccupied() {
 		return (person != null);
 	}
 
 	/**
 	 * Removes {@code Person} reference.
 	 */
 	public void release() {
 		setPerson(null);
 	}
 
 	/**
 	 * Discreet to continuous dimensions.
 	 * 
 	 * @param d
 	 * @return continuous dimensions based on a cell index
 	 */
 	public static Point2D.Double d2c(Point d) {
 		return new Point2D.Double((0.5 + d.x) * CELL_SIZE, (0.5 + d.y)
 				* CELL_SIZE);
 	}
 
 	/**
 	 * Continuous to discrete dimensions.
 	 * 
 	 * @param c
 	 * @return index of a cell based on continuous dimensions
 	 */
 	public static Point c2d(Point2D.Double c) {
 		return new Point((int) Math.round(Math.floor(c.x / CELL_SIZE)),
 				(int) Math.round(Math.floor(c.y / CELL_SIZE)));
 	}
 
 	/**
 	 * <pre>
 	 * A snippet mapping position to neighbour index required in AllowedCfgs.
 	 * Indexes:
 	 *  0   1   2
 	 *  3       4
 	 *  5   6   7
 	 * 
 	 * @param c
 	 * @return
 	 */
 	public static int positionToIndex(Cell baseCell, Cell neighbourCell) {
 		Point posCell = baseCell.getPosition();
 		Point posOth = neighbourCell.getPosition();
 
 		// every row index has triple value
 		// abs is to invert indexing (top to bottom)
 		int index = (posOth.x - posCell.x + 1) + 3
 				* Math.abs(posOth.y - posCell.y - 1);
 
 		// omits the central cell
 		if (index >= 5)
 			--index;
 
 		return index;
 	}
 
 	/**
 	 * Finds cell's neighbours in Moore's neighbourhood.
 	 * 
 	 * @param cell
 	 * @return
 	 */
 	public List<Cell> getNeighbours() {
 		List<Cell> neighbours = new ArrayList<Cell>();
 
 		for (int i = position.y - 1; i <= position.y + 1; ++i)
 			for (int j = position.x - 1; j <= position.x + 1; ++j) {
 				Cell c = board.getCellAt(new Point(j, i));
 				if (c != null && !c.equals(this))
 					neighbours.add(c);
 			}
 
 		return neighbours;
 	}
 
 	/**
 	 * Finds cell's neighbours occupied by a {@code Person}.
 	 * 
 	 * @return
 	 */
 	public List<Cell> getOccupiedNeighbours() {
 		List<Cell> neighbours = getNeighbours();
 		List<Cell> occupiedNeighbours = new ArrayList<Cell>();
 
 		for (Cell c : neighbours) {
 			if (c.isOccupied())
 				occupiedNeighbours.add(c);
 		}
 
 		return occupiedNeighbours;
 	}
 
 	public List<Cell> getRow(int neighbourIndex, int range) throws NeighbourIndexException {
 		List<Cell> row = new ArrayList<Cell>();
 		row.add(this);
 		int yIncSign = getYAxisIncSign(neighbourIndex);
 		int xIncSign = getXAxisIncSign(neighbourIndex);
 		
 		for(int iy = 1; iy <= range; iy += yIncSign * 1)
 			for(int ix = 1; ix <= range; ix += xIncSign * 1){
 				Point pos = new Point(position.x + ix, position.y + iy);
 				Cell rowCell = board.getCellAt(pos);
 				
 				if(rowCell != null)
 					row.add(rowCell);
 			}
 		
 		return row;
 
 	}
 
	public static int getYAxisIncSign(int neighbourIndex)
 			throws NeighbourIndexException {
 		if (0 > neighbourIndex && neighbourIndex > 7)
 			throw new NeighbourIndexException();
 
		return (int) Math.signum(Math.floor(2 - (neighbourIndex / 2.0)));
 	}
 
	public static int getXAxisIncSign(int neighbourIndex) throws NeighbourIndexException {
 		if (0 > neighbourIndex && neighbourIndex > 7)
 			throw new NeighbourIndexException();
 		
 		if(neighbourIndex >= 4)
 			++neighbourIndex;
 		
 		return (neighbourIndex % 3) - 1;
 	}
 
 	// TODO: change formula
 	private void evaluateStaticFieldVal() {
 		staticFieldVal = PHYSICS_COEFF
 				* (physics.get(Physics.Type.TEMPERATURE)) + distToExit;
 	}
 
 	/**
 	 * Calculates distance to the nearest exit.
 	 * 
 	 */
 	private void calculateDistToExit() {
 		Vector<Exit> exits = board.getExits();
 		Double dist = Double.MAX_VALUE;
 		Point2D.Double realPosition = getRealPosition();
 
 		for (Exit e : exits) {
 			Point2D closestPoint = Common.getClosestPointOnSegment(e.p1, e.p2,
 					realPosition);
 			Double currDist = realPosition.distance(closestPoint);
 
 			if (currDist < dist) {
 				dist = currDist;
 			}
 		}
 
 		this.distToExit = dist;
 	}
 
 	public Point2D.Double getRealPosition() {
 		return d2c(position);
 	}
 
 	public Person getPerson() {
 		return person;
 	}
 
 	public void setPerson(Person person) {
 		this.person = person;
 	}
 
 	public Physics getPhysics() {
 		return physics;
 	}
 
 	public void setPhysics(Physics physics) {
 		this.physics = physics;
 	}
 
 	public Board getBoard() {
 		return board;
 	}
 
 	public Point getPosition() {
 		return position;
 	}
 
 	public Double getStaticFieldVal() {
 		return staticFieldVal;
 	}
 
 }
