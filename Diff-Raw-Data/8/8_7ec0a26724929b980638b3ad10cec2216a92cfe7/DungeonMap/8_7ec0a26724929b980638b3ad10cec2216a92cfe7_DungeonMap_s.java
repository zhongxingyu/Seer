 package orig;
 
 import game.OnScreenChar;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.Queue;
 import java.util.Stack;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.state.StateBasedGame;
 import org.newdawn.slick.util.pathfinding.PathFindingContext;
 import org.newdawn.slick.util.pathfinding.TileBasedMap;
 
 import util.DungeonMapGenerator.TileData;
 import util.DungeonMapGenerator.TileType;
 import util.General.Direction;
 import util.General.GridPoint;
 
 public class DungeonMap implements TileBasedMap, Serializable { 
 	
 	private static final long serialVersionUID = -6764222487499645397L;
 
 	//getting around
 	int mapID;
 	
 	//the map's properties
 	Square[][] squares;
 	int level; //the height, above sea level. 0 is usually the surface
 	boolean outside; //true if the ceiling is the sky, false otherwise
 	transient Planet planet;
 	
 	//these only exist so that a quick description can be given of the room, without iterating through the squares array to find the most commmon elements.
 	Element wallsbase;
 	Element floorsbase;
 	
 	// this image map is for rendering special details, like doors, stairs, etc?
 	// maybe not even needed
 	HashMap<TileType, String> extras;
 	
 	HashSet<GridPoint> doorList;
 	private HashSet<GridPoint> stairList;
 	private HashSet<GridPoint> playerSpawns;
 	
 	// List of creatures active on the map, not including player?
 	HashSet<OnScreenChar> monsters;
 	
 	public DungeonMap(){ //completely random constructor, probably not necessary.
 	}
 	
 	public DungeonMap(Planet p, boolean outer) {
 		//for now, there is no random generation
 		this.planet = p;
 		floorsbase = p.getLand();
 		outside = outer;
 		if(!outer){
 			wallsbase = floorsbase; 
 		}
 		else{
 			
 		}
 	}
 	
 	public DungeonMap(String mapPath, Planet p, boolean outer) {
 		this(p, outer);
 	}
 	
 	public void reveal(int radius, int x, int y){
 		for(Square[] sq: squares) {
 			for(Square s: sq) {
 				s.setNonvisible();
 			}
 		}
 		// for added detail, we'll only illuminate walls that are next to floor tiles
 		for(int i=(x-radius); i<= x+radius; i++ ){
 			for(int j=(y-radius); j<= y+radius; j++){
 				if (isVisible(i, j, x, y, radius)) {
 					squares[i][j].setVisible();
 				}
 			}
 		}
 	}
 	
 	public boolean isVisible(int i, int j, int x, int y, int radius) {
 		int dx = Math.abs(i-x);
 		int dy = Math.abs(j-y);
 		// 4 is just something random I chose to make it work
 		// This way, it doesn't look as deformed (try changing it to 0 and seeing for yourself)
 		if (Math.sqrt(dx*dx + dy*dy + 4) > radius)
 			return false;
 		// ok, for funsies, lets do this
 		// we need to "draw a line" from (i, j) to (x, y)
 		// check if it hits any squares that are impassable
 		// if so, then don't set visible
 		// else, set visible.
 		if (!validCoordinates(i, j))
 			return false; // not on the map.
 		boolean chanceToSee = false;
 		//squares[i][j].setVisible();
 		if (isPassable(i, j) || (dx <= 1 && dy <= 1)) {
 			chanceToSee = true;
 		} else {
 	OUT:	for (int k = i-1; k <= i+1; ++k) {
 				for (int m = j-1; m <= j+1; ++m) {
 					if (validCoordinates(k, m) && isSeeThrough(k, m)) {
 						chanceToSee = true;
 						break OUT;
 					}
 				}
 			}
 		}
 		
 		if(chanceToSee) {
 			int interferenceLimit = 0; // okay if XXX things in the way
 			int interferenceCount = 0;
 			//System.out.printf("\tThere is a chance we can see (%d, %d) from (%d, %d)!\n", i, j, x, y);
 			// we'll need to do the line thing.
 			// I'll say that square (i, j) is visible from square (x, y)
 			// if on the line between their centers, all the squares that are hit are "see through"
 			// for now, "passable" will be used to mean "see through"
 			// but we can easily add a property to square, so "glass" walls and such can be "see through"
 			double curX = i + 0.5;
 			double curY = j + 0.5;
 			double m = radius*1000.0*(y - j);
 			if (x - i != 0)
 				m = (double)(y - j)/(double)(x - i);
 			// if we move X enough to hit the next square, has the Y coord past a boundary?
 			int xChange = (x - i < 0 ? -1 : 1);
 			int yChange = (y - j < 0 ? -1 : 1);
 			double xBound = curX + 0.5 * xChange;
 			double yBound = curY + 0.5 * yChange;
 			int sqX = i;
 			int sqY = j;
 			// to deal with m = inf
 			while (!(sqX == x && sqY == y)) {
 				// verify that square[sqX][sqY] is "see through"
 				//System.err.printf("\tcurX=%.3f, curY=%.3f, m=%.3f, xBound=%.3f, yBound=%.3f\n", curX, curY, m, xBound, yBound);
 				//System.err.printf("\t\tChecking that %d, %d is passable\n", sqX, sqY);
 				if (!(sqX == i && sqY == j) && !isSeeThrough(sqX, sqY)) {
 					//System.err.printf("\t\tIt is not...\n");
 					++interferenceCount;
 					if (interferenceCount > interferenceLimit) {
 						chanceToSee = false; 
 						break;
 					}
 				}
 				// if we move curX to reach xBound, has curY surpassed yBound?
 				double movement = xBound - curX;
 				double yTemp = curY + m*movement;
 				if (yTemp*yChange < yBound*yChange) {
 					// we moved the xcoord.
 					curX = xBound;
 					curY = yTemp;
 					sqX += xChange;
 					xBound += xChange;
 				} else if (Math.abs(yTemp - yBound) < 0.0000001) {
 					// they are pretty much equal, so assume we go through corner here
 					sqX += xChange;
 					sqY += yChange;
 					curX = xBound;
 					curY = yBound;
 					xBound += xChange;
 					yBound += yChange;
 				} else { // yTemp far surpassed the bound
 					movement = yBound - curY;
 					curX = curX + movement/m;
 					curY = yBound;
 					sqY += yChange;
 					yBound += yChange;
 				}
 			}
 		}
 		return chanceToSee;
 	}
 	
 	private boolean isSeeThrough(int x, int y) {
 		return this.squares[x][y].isSeeThrough();
 	}
 
 	public DungeonMap(int[][] mapDetails, TileData data) {
 		this.wallsbase = data.getWall();
 		this.floorsbase = data.getFloor();
 		this.squares = new Square[mapDetails.length][mapDetails[0].length];
 		this.doorList = new HashSet<GridPoint>();
 		this.stairList = new HashSet<GridPoint>();
 		this.playerSpawns = new HashSet<GridPoint>();
 		this.monsters = new HashSet<OnScreenChar>();
 		
 		for (int x = 0; x < mapDetails.length; ++x) {
 			for (int y = 0; y < mapDetails.length; ++y) {
 				// several checks, to decide which types of squares to initialize
 				if ((mapDetails[x][y] & TileType.DOOR.flag) != 0) {
 					// this is a door
 					Door d = new Door(true, this.floorsbase);
 					d.setImageName(data.getMap().get(TileType.DOOR));
 					// detect the direction.
 					d.setDirection(Direction.getDirection(x, y));
 					this.doorList.add(new GridPoint(x, y));
 					this.squares[x][y] = d;
 				} else if ((mapDetails[x][y] & (TileType.UPSTAIRS.flag + TileType.DOWNSTAIRS.flag)) != 0) {
 					// there is a stair case here.
 					Stairs s = new Stairs(true, this.floorsbase);
 					if ((mapDetails[x][y] & TileType.UPSTAIRS.flag) != 0) {
 						s.setDirection(Direction.HIGHER);
 						s.setImageName(data.getMap().get(TileType.UPSTAIRS));
 					} else {
 						s.setDirection(Direction.LOWER);
 						s.setImageName(data.getMap().get(TileType.DOWNSTAIRS));
 					}
 					this.stairList.add(new GridPoint(x, y));
 					this.squares[x][y] = s;
 				} else if ((mapDetails[x][y] & TileType.PLAYERSPAWN.flag) != 0) {
 					playerSpawns.add(new GridPoint(x, y));
 					// It'll look like a upstaircase!
 					// but it'll be a wall!
 					Square sq = new Square(false, this.wallsbase);
 					sq.setImageName("upstairs");
 					sq.setSeeThrough();
 					this.squares[x][y] = sq;
 				} else if ((mapDetails[x][y] & TileType.FLOOR.flag) != 0) {
 				
 					// this square is simply a floor.
 					// this ordering also means that if a detail location has both FLOOR and WALL flags, it will be a floor
 					Square sq = new Square(true, this.floorsbase);
 					this.squares[x][y] = sq;
 				} else  {
 					// this is not anything special, so it must be a wall
 					Square sq = new Square(false, this.wallsbase);
 					this.squares[x][y] = sq;
 				}
 			}
 		}
 		
 	}
 	
 	public DungeonMap(boolean[][] wallFloor, Element wbase, Element fbase) {
 		this.wallsbase = wbase;
 		this.floorsbase = fbase;
 		this.squares = new Square[wallFloor.length][wallFloor[0].length];
 		for (int x = 0; x < wallFloor.length; ++x) {
 			for (int y = 0; y < wallFloor[x].length; ++y) {
 				// might want to look into changing this way of initializing squares
 				boolean passable = wallFloor[x][y];
 				Element cons = (wallFloor[x][y] ? fbase : wbase);
 				Square sq = new Square(passable, cons);
 				this.squares[x][y] = sq;
 			}
 		}
 	}
 	
 	/**
 	 * This method attempts to place a creature in a particular square on the grid
 	 * @param x X coordinate of location to put creature
 	 * @param y Y coordinate of location to put creature
 	 * @param c reference to the Creature
 	 * @param walking Whether or not we walked onto this square, or were simply placed there.
 	 * @return whether the operation was successful or not
 	 */
 	public boolean putOnScreenChar(int x, int y, OnScreenChar c, boolean walking) {
 		if (validCoordinates(x, y)) {
 			GridPoint gp = new GridPoint(x, y);
 			if (doorList.contains(gp) && c.isPlayer() && walking) {
 				// need to update maps!
 				Door door = this.getDoor(gp);
 				gp.mirror(squares.length, squares[0].length);
 				planet.moveMap(door.getDirection(), c, gp);
 				return true;
 			} else if (stairList.contains(gp) && c.isPlayer() && walking) {
 				Stairs stairs = this.getStairs(gp);
 				planet.moveMap(stairs.getDirection(), c, gp);
 				return true;
 			}
 			if (squares[x][y].getOnScreenChar() == null) {
 				squares[x][y].setOnScreenChar(c);
 				c.setPosition(x, y);
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	/**
 	 * This method attempts to remove a creature from a particular square
 	 * @param x X coordinate on the grid
 	 * @param y Y coordinate on the grid
 	 * @return successful - meaning whether the x,y coordinates were valid
 	 */
 	public boolean removeOnScreenChar(int x, int y) {
 		if (validCoordinates(x, y)) {
 			squares[x][y].setOnScreenChar(null);
 			return true;
 		}
 		return false;
 	}
 	
 	public boolean isPassable(int xCoor, int yCoor) {
 		// validate coordinates
 		if (!validCoordinates(xCoor, yCoor))
 			return false; // tell caller they can't move off the grid, maybe throw exception
 		if (squares[xCoor][yCoor].getOnScreenChar() != null){
 			return false; // cannot walk there if another creature is already there
 		}
 		return squares[xCoor][yCoor].isPassable();
 	}
 
 	private boolean validCoordinates(int xCoor, int yCoor) {
 		return (xCoor >= 0 && xCoor < squares.length && yCoor >= 0 && yCoor < squares[0].length);
 	}
 	
 	public void update(int sightRadius, int xPos, int yPos) {
 		Iterator<OnScreenChar> it = monsters.iterator();
 		while (it.hasNext()) {
 			OnScreenChar osc = it.next();
 			if (!osc.isPlayer()) {
 				osc.move(sightRadius, this);
 			}
 		}
 		this.reveal(sightRadius+3, xPos, yPos);
 	}
 
 	public void render(GameContainer container, StateBasedGame sbg, Graphics g, int xPos, int yPos, int screenX, int screenY) {
 		int xUpper = (xPos+screenX > squares.length) ? squares.length : xPos+screenX;
 		int yUpper = (yPos+screenY > squares[0].length) ? squares[0].length : yPos+screenY;
 		int xLower = (xPos < 0) ? 0 : xPos;
 		int yLower = (yPos < 0) ? 0 : yPos;
 		for (int x = xLower; x < xUpper; ++x) {
 			for (int y = yLower; y < yUpper; ++y) {
 				squares[x][y].render(x-xPos, y-yPos, xPos, yPos, g);
 			}
 		}
 	}
 
 	@Override
 	public boolean blocked(PathFindingContext arg0, int arg1, int arg2) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public float getCost(PathFindingContext arg0, int arg1, int arg2) {
 		return 0;
 	}
 
 	@Override
 	public int getHeightInTiles() {
 		return squares[0].length;
 	}
 
 	public int getWidthInTiles() {
 		return squares.length;
 	}
 
 	@Override
 	public void pathFinderVisited(int arg0, int arg1) {
 	}
 	
 //prototype Attack function (should just pass to creature/Item at each of the points in attack pattern) 
 //remove if it is already implemented - Michael
 	public void attack(Attack a) {
 		//handle attacks and Attack results
 	}
 
 	public HashSet<GridPoint> getDoorList() {
 		return this.doorList;
 	}
 
 	public Door getDoor(GridPoint nextPt) {
 		Square s = this.squares[nextPt.getX()][nextPt.getY()];
 		if (s instanceof Door)
 			return (Door)s;
 		return null;
 	}
 
 	public void setPlanet(Planet plan) {
 		this.planet = plan;
 	}
 
 	public HashSet<GridPoint> getStairList() {
 		return this.stairList;
 	}
 
 	public Stairs getStairs(GridPoint nextPt) {
 		Square s = this.squares[nextPt.getX()][nextPt.getY()];
 		if (s instanceof Stairs)
 			return (Stairs)s;
 		return null;
 	}
 	
 	public HashSet<GridPoint> getPlayerSpawnPoints() {
 		return this.playerSpawns;
 	}
 	
 	/**
 	 * This method encapsulates moving a creature from one square to the next
 	 * However, if the to location contains a creature, this method will
 	 * invoke an attack.
 	 * @param fromX
 	 * @param fromY
 	 * @param toX
 	 * @param toY
 	 * @param aggressive Indicates whether the monster is intending to attack
 	 */
 	public void attackMove(int fromX, int fromY, int toX, int toY, boolean aggressive) {
 		if (isCreature(toX, toY) && isCreature(fromX, fromY)) {
 			if (aggressive) {
 				// do attack stuff
 				System.err.printf("Aha! I have you now!\n");
 			}
 		} else if(isCreature(fromX, fromY)){
 			OnScreenChar osc = this.squares[fromX][fromY].c;
 			removeOnScreenChar(fromX, fromY);
 			putOnScreenChar(toX, toY, osc, true);
 		}
 	}
 	
 	/**
 	 * This method is what an OSC calls to tell the map to move it to the
 	 * location it desires, in the best way possible?
 	 * This method ignores checks such as, should the monster get the shortest
 	 * path? Does the monster forget? Etc.
 	 * Does not ignore bound checks.
 	 * @param startX
 	 * @param startY
 	 * @param targetX
 	 * @param targetY
 	 */
 	public void moveOSC(int startX, int startY, int targetX, int targetY) {
 		if (!validCoordinates(startX, startY) || !validCoordinates(targetX, targetY))
 			return; // invalid coords
 		OnScreenChar osc = squares[startX][startY].getOnScreenChar();
 		//System.out.printf("start:(%d,%d), target:(%d,%d)\n", startX, startY, targetX, targetY);
 		// currently only 1 strategy: smart
 		HashMap<GridPoint, GridPoint> prev = new HashMap<GridPoint, GridPoint>();
 		Direction[] dirs = {Direction.LEFT, Direction.RIGHT, Direction.UP, Direction.DOWN, Direction.UPLEFT, Direction.UPRIGHT, Direction.DOWNLEFT, Direction.DOWNRIGHT};
 		Queue<GridPoint> queue = new LinkedList<GridPoint>();
 		queue.add(new GridPoint(startX, startY));
 OUT:	while (!queue.isEmpty()) {
 			GridPoint cur = queue.poll();
 			//System.err.printf("Checking %s\n", cur);
 			for (int i = 0; i < dirs.length; ++i) {
 				int nextX = cur.getX() + dirs[i].getX();
 				int nextY = cur.getY() + dirs[i].getY();
 				GridPoint next = new GridPoint(nextX, nextY);
 				if (validCoordinates(nextX, nextY)) {
 					if (isPassable(nextX, nextY)) {
 						if (!prev.containsKey(next)) {
 							prev.put(next, cur);
 							queue.add(next);
 						}
 					} else if (nextX == targetX && nextY == targetY) {
 						prev.put(next, cur);
 						break OUT;
 					}
 				}
 			}
 		}
 		GridPoint target = new GridPoint(targetX, targetY);
 		if (!prev.containsKey(target)) {
 			//System.err.printf("Prev did not contain target\n");
 			// uhh, impossible to reach target from start
 			return;
 		}
 		GridPoint curPoint = target;
 		Stack<GridPoint> path = new Stack<GridPoint>();
 		while (prev.containsKey(curPoint)) {
 			path.push(curPoint);
 			curPoint = prev.get(curPoint);
 		}
 		osc.setPathPlan(path);
 		return;
 	}
 
 	/**
 	 * This method identifies the squares around the given position and checks
 	 * what can be identified. Currently only gets the enemies and items
 	 * @param radius How far around the creature that it can detect
 	 * @param onScreenChar The creature
 	 * @return
 	 */
 	// TODO: include Michael's detects(OSC) method
 	public ArrayList<ArrayList<GridPoint>> detectArea(int radius, OnScreenChar onScreenChar) {
 		int xPos = onScreenChar.getX();
 		int yPos = onScreenChar.getY();
 		ArrayList<GridPoint> enemies = new ArrayList<GridPoint>();
 		ArrayList<GridPoint> items = new ArrayList<GridPoint>();
 		for (int x = xPos - radius; x <= xPos + radius; ++x) {
 			for (int y = yPos - radius; y <= yPos + radius; ++y) {
 				if (x == xPos && y == yPos)
 					continue; // ignore self.
 				if (isVisible(x, y, xPos, yPos, radius)) {
 					if (isCreature(x, y) && isPlayer(x, y)) { //TODO: and can detect creature, enemy check
 						enemies.add(new GridPoint(x, y));
 					}
 					if (containsItem(x, y)) {
 						items.add(new GridPoint(x, y));
 					}
 				}
 			}
 		}
 		ArrayList<ArrayList<GridPoint>> ret = new ArrayList<ArrayList<GridPoint>>();
 		ret.add(enemies);
 		ret.add(items);
 		return ret;
 	}
 	
 	private boolean containsItem(int x, int y) {
 		return validCoordinates(x, y) && this.squares[x][y].containsItem();
 	}
 
 	private boolean isPlayer(int x, int y) {
 		return isCreature(x, y) && this.squares[x][y].c.isPlayer();
 	}
 
 	private boolean isCreature(int x, int y) {
 		return validCoordinates(x, y) && this.squares[x][y].c != null;
 	}
 	
 	/**
 	 * This method takes an OSC and finds some place on the map to add it.
 	 * Not really something I expect to use a lot, mainly for testing?
 	 * @param osc
 	 */
 	public void addOnScreenchar(OnScreenChar osc) {
 		boolean done = false;
 		while (!done) {
 			int x = (int)(Math.random()*this.squares.length);
 			int y = (int)(Math.random()*this.squares[0].length);
 			if (isPassable(x, y)) {
 				this.monsters.add(osc);
 				putOnScreenChar(x, y, osc, false);
 				done = true;
 			}
 		}
 		
 	}
 }
