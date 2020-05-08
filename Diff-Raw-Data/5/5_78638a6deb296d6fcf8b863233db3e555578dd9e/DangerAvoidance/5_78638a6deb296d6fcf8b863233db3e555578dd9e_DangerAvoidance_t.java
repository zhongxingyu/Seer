 
 package isnork.g6;
 
 import isnork.g6.NewPlayer.Level;
 import isnork.sim.GameEngine;
 import isnork.sim.GameObject.Direction;
 import isnork.sim.Observation;
 import isnork.sim.Player;
 import isnork.sim.SeaLifePrototype;
 
 import java.awt.geom.Point2D;
 import java.util.ArrayList;
 import java.util.Comparator;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.PriorityQueue;
 import java.util.Random;
 import java.util.Set;
 
 public class DangerAvoidance {
 
 	private static final int DEPTH = 4;
 	private static final double HIGH_NUMBER = 9999999999.99;
 
 	private static final int EAST = 0;
 	private static final int NORTHEAST = 45;
 	private static final int NORTH = 90;
 	private static final int NORTHWEST = 135;
 	private static final int WEST = 180;
 	private static final int SOUTHWEST = 225;
 	private static final int SOUTH = 270;
 	private static final int SOUTHEAST = 315;
 
 	public LinkedList<Node> buildSafePath(NewPlayer p) {
 		System.out.println("Player " + p.getId() + " sees Danger!");
 		DangerMap dm = null;
 		Point2D np = null;
 		try {
 			dm = new DangerMap(NewPlayer.r, p.currentPosition, p.whatISee);
 			findDanger(dm, p, p.currentPosition, NewPlayer.r);
 			np = dm.safestPoint(p.currentPosition, p.destination);
 		} catch (Exception e) {
 			np = p.currentPosition;
 		}
 		LinkedList<Node> l = PathManager.buildPath(p.currentPosition, np, p.minutesLeft);
 		LinkedList<Node> m = PathManager.buildPath(np, p.destination, p.minutesLeft);
 		l.addAll(m);
 		return l;
 	}
 
 	public void findDanger(DangerMap dm, NewPlayer p, Point2D curPnt, int r) {
 		Set<Observation> whatIsee = p.whatISee;
 		for (Observation o : whatIsee) {
 			if (o.isDangerous()) {
 				Iterator<SeaLifePrototype> it = NewPlayer.seaLifePossibilites.iterator();
 				while (it.hasNext()) {
 					SeaLifePrototype sl = (SeaLifePrototype) it.next();
 					if (sl.getName().equals(o.getName())) {
 						double sHappy = sl.getHappinessD();
 						Point2D sP = o.getLocation();
 						Direction sd = o.getDirection();
 						int sX = (int) sP.getX();
 						int sY = (int) sP.getY();
 						// System.out.println("Seeing " + o.getName() + " at " +
 						// sX + "," + sY);
 						if (!(sl.getSpeed() > 0) || sd == null) {
 							dm.addValue(sHappy, sX, sY);
 							// System.out.println(o.getName() + " is static");
 						} else {
 							calcPositions(dm, sd, 0, 100.0, sX, sY, sHappy);
 						}
 					}
 				}
 			}
 		}
 	}
 
 	private void calcPositions(DangerMap dm, Direction sD, int r, double chance, int sX, int sY, double sHappy) {
 		if (r > NewPlayer.r) {
 			return;
 		}
 		ArrayList<Direction> dirs = Direction.allBut(Direction.STAYPUT);
 		for (Direction d : dirs) {
 			if (d.getDegrees() == sD.getDegrees()) {
 				dm.addValue((sHappy * chance), sX + d.getDx(), sY + d.getDy());
 				calcPositions(dm, sD, (r + 1), (sHappy * (0.79 * chance)), sX + d.getDx(), sY + d.getDy(), sHappy);
 			} else {
 				dm.addValue((sHappy * (0.03 * chance)), sX + d.getDx(), sY + d.getDy());
 				calcPositions(dm, sD, (r + 1), (sHappy * (0.03 * chance)), sX + d.getDx(), sY + d.getDy(), sHappy);
 			}
 		}
 	}
 
 	public class DangerMap {
 		private double[][] dm;
 		Set<Observation> whatISee;
 
 		public Point2D safestPoint(Point2D cur, Point2D dest) {
 			Comparator<DangerNode> comparator = new StringLengthComparator();
 			PriorityQueue<DangerNode> dp = new PriorityQueue<DangerNode>((NewPlayer.r + 1) * (NewPlayer.r + 1), comparator);
 			for (int i = 0; i < dm.length; i++) {
 				for (int j = 0; j < dm.length; j++) {
 					dp.add(new DangerNode(new Point2D.Double((double) toBoard(i), (double) toBoard(j)), dm[i][j]));
 				}
 			}
 			double bestCount = Double.MAX_VALUE;
 			DangerNode bestD = dp.peek();
 			int i = 0;
 			while (!dp.isEmpty() && i++ < 100) {
 				DangerNode dn = dp.remove();
 				double count = testPoint(cur, dn.p) + testPoint(dn.p, dest);
 				if (count < bestCount) {
 					bestD = dn;
 					bestCount = count;
 				}
 			}
 			// System.out.println(cur + " to " + bestD.p + " to " + dest +
 			// " with danger " + bestCount);
 			return bestD.p;
 		}
 
 		public double testPoint(Point2D c, Point2D d) {
 			double count = 0;
 			double deltax = d.getX() - c.getX();
 			double deltay = d.getY() - c.getY();
 
 			if (d.distance(c) == 0) {
 				return count;
 			}
 
 			while (deltax != 0 || deltay != 0) {
 				if (deltax > 0 && deltay == 0) {
 					deltax -= 1;
 				} else if (deltax < 0 && deltay == 0) {
 					deltax += 1;
 				} else if (deltax == 0 && deltay > 0) {
 					deltay -= 1;
 				} else if (deltax == 0 && deltay < 0) {
 					deltay += 1;
 				} else if (deltax > 0 && deltay > 0) {
 					deltax -= 1;
 					deltay -= 1;
 				} else if (deltax > 0 && deltay < 0) {
 					deltax -= 1;
 					deltay += 1;
 				} else if (deltax < 0 && deltay > 0) {
 					deltax += 1;
 					deltay -= 1;
 				} else if (deltax < 0 && deltay < 0) {
 					deltax += 1;
 					deltay += 1;
 				}
 				double counter = dm[toMap((int) (d.getX() - deltax))][toMap((int) (d.getY() - deltay))];
 				// System.out.println((int) (d.getX() - deltax) + "," + (int)
 				// (d.getY() - deltay) + " danger: " + counter);
 				count += counter;
 			}
 			return count;
 		}
 
 		public class DangerNode {
 			public Point2D p;
 			public double danger;
 
 			public DangerNode(Point2D p, double danger) {
 				this.p = p;
 				this.danger = danger;
 			}
 		}
 
 		public class StringLengthComparator implements Comparator<DangerNode> {
 			@Override
 			public int compare(DangerNode x, DangerNode y) {
 				if (x.danger < y.danger) {
 					return -1;
 				}
 				if (x.danger > y.danger) {
 					return 1;
 				}
 				return 0;
 			}
 		}
 
 		public DangerMap(int r, Point2D curPnt, Set<Observation> whatISee) {
 			this.whatISee = whatISee;
 			int x = (int) curPnt.getX();
 			int y = (int) curPnt.getY();
 			dm = new double[NewPlayer.d * 2 + 1][NewPlayer.d * 2 + 1];
 			for (int i = 0; i < dm.length; i++) {
 				for (int j = 0; j < dm.length; j++) {
 					if (Math.sqrt((x - toBoard(i)) * (x - toBoard(i)) + (y - toBoard(j)) * (y - toBoard(j))) >= r) {
 						dm[i][j] = Double.MAX_VALUE;
 					}
 				}
 			}
 		}
 
 		public int toBoard(int q) {
 			return q -= NewPlayer.d;
 		}
 
 		public int toMap(int q) {
 			return q += NewPlayer.d;
 		}
 
 		public void addValue(double v, int x, int y) {
 			if (!illegalMove(new Point2D.Double(x, y))) {
 				dm[toMap(x)][toMap(y)] += v;
 			}
 			if (!illegalMove(new Point2D.Double(x + 1, y))) {
 				dm[toMap(x + 1)][toMap(y)] += v;
 			}
 			if (!illegalMove(new Point2D.Double(x, y + 1))) {
 				dm[toMap(x)][toMap(y + 1)] += v;
 			}
 			if (!illegalMove(new Point2D.Double(x - 1, y))) {
 				dm[toMap(x - 1)][toMap(y)] += v;
 			}
 			if (!illegalMove(new Point2D.Double(x, y - 1))) {
 				dm[toMap(x)][toMap(y - 1)] += v;
 			}
 			if (!illegalMove(new Point2D.Double(x + 1, y + 1))) {
 				dm[toMap(x + 1)][toMap(y + 1)] += v;
 			}
 			if (!illegalMove(new Point2D.Double(x - 1, y - 1))) {
 				dm[toMap(x - 1)][toMap(y - 1)] += v;
 			}
 			if (!illegalMove(new Point2D.Double(x + 1, y - 1))) {
 				dm[toMap(x + 1)][toMap(y - 1)] += v;
 			}
 			if (!illegalMove(new Point2D.Double(x - 1, y + 1))) {
 				dm[toMap(x - 1)][toMap(y + 1)] += v;
 			}
 		}
 
 		public void print() {
 			for (int i = 0; i < dm.length; i++) {
 				for (int j = 0; j < dm.length; j++) {
 					if (dm[i][j] < Double.MAX_VALUE) {
 						System.out.println(toBoard(i) + "," + toBoard(j) + ":" + dm[i][j] + "  ");
 					}
 				}
 			}
 		}
 	}
 
 	/*
 	 * public LinkedList<Node> buildSafePath(NewPlayer p) { LinkedList<Node> l =
 	 * new LinkedList<Node>(); Set<Observation> whatISee = p.whatISee; Point2D
 	 * currentPosition = p.currentPosition; int m = p.minutesLeft; Node n =
 	 * p.currentPath.peek();
 	 * 
 	 * 
 	 * TODO - randomize the choice of direction find if its an illegal move add
 	 * multiple nodes on
 	 * 
 	 * 
 	 * for (Node nextD: getRandomListofDirs(n, m)) { Point2D pnt =
 	 * pointFromCurrPandDir(currentPosition, nextD.getDirection()); if
 	 * (!isLocationDangerous(whatISee, pnt) && !illegalMove(pnt)) {
 	 * l.add(nextD); Node lst = getOppositeDirection(nextD, m);
 	 * p.currentPath.addLast(lst); return l; } } // Keep going the way you are
 	 * l.addFirst(n); p.currentPath.removeFirst(); return l; }
 	 * 
 	 * 
 	 * public LinkedList<Node> buildSafePath (NewPlayer p) {
 	 * LinkedList<Direction> dl = buildSafePath(p.whatISee,
 	 * p.currentPath.peekFirst().getDirection(), p.currentPosition);
 	 * LinkedList<Node> l = new LinkedList<Node>(); for (Direction d: dl) {
 	 * l.add(new Node(d, p.minutesLeft)); } return l; }
 	 * 
 	 * 
 	 * private ArrayList<Node> getRandomListofDirs(Node n, int m) {
 	 * ArrayList<Node> a = new ArrayList<Node>();
 	 * a.add(goAdjacentCounterClockwise(n, m)); a.add(goCounterClockwise(n, m));
 	 * a.add(goAdjacentClockwise(n, m)); a.add(goClockwise(n, m));
 	 * a.add(getOppositeDirection(n, m)); Random r = new Random(); for (int i =
 	 * 0; i < 7; i++) { int first = r.nextInt(a.size()); int second =
 	 * r.nextInt(a.size()); Node d = a.get(first); a.set(first, a.get(second));
 	 * a.set(second, d); } return a; }
 	 * 
 	 * public LinkedList<Direction> buildSafePath(Set<Observation> whatISee,
 	 * Direction d, Point2D currentPosition) { LinkedList<Direction> newL = new
 	 * LinkedList<Direction>(); ArrayList<Direction> directionOptions =
 	 * Direction.allBut(d); Direction bestDirection = null; Point2D bestPoint =
 	 * null; for (Direction nextD : directionOptions) { double newPosX =
 	 * currentPosition.getX() + nextD.getDx(); double newPosY =
 	 * currentPosition.getY() + nextD.getDy(); Point2D newPoint = new
 	 * Point2D.Double(newPosX, newPosY); if (!atTheWall(newPoint) &&
 	 * !isLocationDangerous(whatISee, newPoint)) { if (bestPoint == null) {
 	 * bestPoint = newPoint; bestDirection = nextD; } else { if
 	 * (tilesAway(currentPosition, newPoint) < tilesAway(currentPosition,
 	 * bestPoint)) { bestPoint = newPoint; bestDirection = nextD; } } } } if
 	 * (bestDirection == null) { Random r = new Random(); Direction
 	 * randomDirection =
 	 * directionOptions.get(r.nextInt(Direction.values().length));
 	 * newL.add(randomDirection); double newPosX = currentPosition.getX() +
 	 * randomDirection.getDx(); double newPosY = currentPosition.getY() +
 	 * randomDirection.getDy(); Point2D randomPoint = new
 	 * Point2D.Double(newPosX, newPosY); LinkedList<Direction> temp =
 	 * buildSafePath(whatISee, randomDirection, randomPoint); for (Direction
 	 * tmpD : temp) { newL.add(tmpD); } return newL; } else {
 	 * newL.add(bestDirection); return newL; } }
 	 */
 
 	/*
 	 * Attempt 6 - DFS
 	 * 
 	 * public LinkedList<Node> buildSafePath(NewPlayer player) {
 	 * System.out.println("FOUND DANGER!!"); LinkedList<Node> l = new
 	 * LinkedList<Node>(); int depth = 0; Point2D curr = player.currentPosition;
 	 * Point2D dest = player.destination; System.out.println("I'm at " + curr +
 	 * " trying to get to " + dest); int tilesAway = tilesAway(curr, dest); Node
 	 * n = new Node(Direction.STAYPUT, player.minutesLeft);
 	 * n.setAssumedPoint(curr); l.add(n);
 	 * System.out.println("Searching to depth: " + tilesAway); recursive_dfs(l,
 	 * depth, DEPTH, player, tilesAway); l.removeFirst(); return l; }
 	 * 
 	 * private boolean recursive_dfs(LinkedList<Node> l, int depth, int path,
 	 * NewPlayer player, int tilesAway) { boolean b =
 	 * !isLocationDangerous(player.whatISee, l.peekLast().getAssumedPoint()); if
 	 * (b && l.size() > 3) { System.out.println("Made it to goal at " +
 	 * player.destination); return true; } boolean success = false; Point2D
 	 * currP = l.peekLast().getAssumedPoint();
 	 * System.out.println("New Node at: " + currP); if (path > depth &&
 	 * !success) { for (Direction nextD : getRandomDirectionArray()) { Point2D
 	 * nextP = getPointFromDirectionandPosition(currP, nextD); if
 	 * (!illegalMove(nextP)) { System.out.println("Direction " + nextD + " at "
 	 * + nextP + " is not an illegal move"); Node tmp = new Node(nextD,
 	 * player.minutesLeft); tmp.setAssumedPoint(nextP); l.addLast(tmp);
 	 * System.out.println("depth is " + depth + " can go " + (path - depth) +
 	 * " further"); success = recursive_dfs(l, depth + 1, path, player,
 	 * tilesAway); if (success) { return success; } } } } return false; }
 	 */
 
 	public Node getOppositeDirection(Node n, int minuteCreated) {
 		int deg = n.getDirection().getDegrees();
 		switch (deg) {
 			case EAST:
 				return new Node(Direction.W, minuteCreated);
 			case WEST:
 				return new Node(Direction.E, minuteCreated);
 			case NORTH:
 				return new Node(Direction.S, minuteCreated);
 			case SOUTH:
 				return new Node(Direction.N, minuteCreated);
 			case NORTHEAST:
 				return new Node(Direction.SW, minuteCreated);
 			case SOUTHWEST:
 				return new Node(Direction.NE, minuteCreated);
 			case NORTHWEST:
 				return new Node(Direction.SE, minuteCreated);
 			case SOUTHEAST:
 				return new Node(Direction.NW, minuteCreated);
 			default:
 				return new Node(Direction.STAYPUT, minuteCreated);
 		}
 	}
 
 	public Node goClockwise(Node n, int minuteCreated) {
 		int deg = n.getDirection().getDegrees();
 		switch (deg) {
 			case EAST:
 				return new Node(Direction.S, minuteCreated);
 			case WEST:
 				return new Node(Direction.N, minuteCreated);
 			case NORTH:
 				return new Node(Direction.E, minuteCreated);
 			case SOUTH:
 				return new Node(Direction.W, minuteCreated);
 			case NORTHEAST:
 				return new Node(Direction.SE, minuteCreated);
 			case SOUTHWEST:
 				return new Node(Direction.NW, minuteCreated);
 			case NORTHWEST:
 				return new Node(Direction.NE, minuteCreated);
 			case SOUTHEAST:
 				return new Node(Direction.SW, minuteCreated);
 			default:
 				return new Node(Direction.STAYPUT, minuteCreated);
 		}
 	}
 
 	public Node goCounterClockwise(Node n, int minuteCreated) {
 		int deg = n.getDirection().getDegrees();
 		switch (deg) {
 			case EAST:
 				return new Node(Direction.N, minuteCreated);
 			case WEST:
 				return new Node(Direction.S, minuteCreated);
 			case NORTH:
 				return new Node(Direction.W, minuteCreated);
 			case SOUTH:
 				return new Node(Direction.E, minuteCreated);
 			case NORTHEAST:
 				return new Node(Direction.NW, minuteCreated);
 			case SOUTHWEST:
 				return new Node(Direction.SE, minuteCreated);
 			case NORTHWEST:
 				return new Node(Direction.SW, minuteCreated);
 			case SOUTHEAST:
 				return new Node(Direction.NE, minuteCreated);
 			default:
 				return new Node(Direction.STAYPUT, minuteCreated);
 		}
 	}
 
 	public Node goAdjacentCounterClockwise(Node n, int minuteCreated) {
 		int deg = n.getDirection().getDegrees();
 		switch (deg) {
 			case EAST:
 				return new Node(Direction.NE, minuteCreated);
 			case WEST:
 				return new Node(Direction.SW, minuteCreated);
 			case NORTH:
 				return new Node(Direction.NW, minuteCreated);
 			case SOUTH:
 				return new Node(Direction.SE, minuteCreated);
 			case NORTHEAST:
 				return new Node(Direction.N, minuteCreated);
 			case SOUTHWEST:
 				return new Node(Direction.S, minuteCreated);
 			case NORTHWEST:
 				return new Node(Direction.W, minuteCreated);
 			case SOUTHEAST:
 				return new Node(Direction.E, minuteCreated);
 			default:
 				return new Node(Direction.STAYPUT, minuteCreated);
 		}
 	}
 
 	public Node goAdjacentClockwise(Node n, int minuteCreated) {
 		int deg = n.getDirection().getDegrees();
 		switch (deg) {
 			case EAST:
 				return new Node(Direction.SE, minuteCreated);
 			case WEST:
 				return new Node(Direction.NW, minuteCreated);
 			case NORTH:
 				return new Node(Direction.NE, minuteCreated);
 			case SOUTH:
 				return new Node(Direction.SW, minuteCreated);
 			case NORTHEAST:
 				return new Node(Direction.E, minuteCreated);
 			case SOUTHWEST:
 				return new Node(Direction.W, minuteCreated);
 			case NORTHWEST:
 				return new Node(Direction.N, minuteCreated);
 			case SOUTHEAST:
 				return new Node(Direction.S, minuteCreated);
 			default:
 				return new Node(Direction.STAYPUT, minuteCreated);
 		}
 	}
 
 	public ArrayList<Direction> getRandomDirectionArray() {
 		ArrayList<Direction> arrL = Direction.allBut(Direction.STAYPUT);
 		try {
 			Random r = new Random();
 			for (int i = 0; i < 7; i++) {
 				int first = r.nextInt(arrL.size());
 				int second = r.nextInt(arrL.size());
 				Direction d = arrL.get(first);
 				arrL.set(first, arrL.get(second));
 				arrL.set(second, d);
 			}
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return arrL;
 	}
 
 	/* Is a point on the board dangerous? */
 	public boolean isLocationDangerous(Set<Observation> whatISee, Point2D pos) {
 		Iterator<Observation> itr = whatISee.iterator();
 		while (itr.hasNext()) {
 			Observation o = itr.next();
 			if (o.isDangerous()) {
				if (o.getLocation().distance(pos) <= 4) {
 					System.out.println("Static Danger at " + o.getLocation());
 					return true;
 				}
 
 			}
 		}
 		return false;
 	}
 
 	public double getDanger(Set<Observation> whatISee, Point2D pos) {
 		double danger = 0;
 		Iterator<Observation> itr = whatISee.iterator();
 		while (itr.hasNext()) {
 			Observation o = itr.next();
			if (o.isDangerous() && o.getLocation().distance(pos) <= 4) {
 				danger += o.happiness() * 2;
 			}
 		}
 		return danger;
 	}
 
 	public boolean constantDanger(Set<Observation> whatISee, Point2D pos) {
 		Iterator<Observation> itr = whatISee.iterator();
 		while (itr.hasNext()) {
 			Observation o = itr.next();
 			if (o.isDangerous() && o.getDirection().equals(Direction.STAYPUT)) {
 				if (tilesAway(pos, o.getLocation()) <= 2) {
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 
 	/* Returns the next direction that isn't dangerous, illegal or staying place */
 	public Direction getDirection(Set<Observation> whatISee, Direction d, Point2D currentPosition) {
 		ArrayList<Direction> directionOptions = Direction.allBut(d);
 		for (Direction nextD : directionOptions) {
 			Point2D newPoint = pointFromCurrPandDir(currentPosition, nextD);
 			if (!illegalMove(newPoint) && !isLocationDangerous(whatISee, newPoint)) {
 				if (!(nextD.getDx() == 0 && nextD.getDy() == 0)) {
 					return nextD;
 				}
 			}
 		}
 		return null;
 	}
 
 	/* get a point from the direction you're going and current position */
 	private Point2D pointFromCurrPandDir(Point2D currentPosition, Direction nextD) {
 		double newPosX = currentPosition.getX() + nextD.getDx();
 		double newPosY = currentPosition.getY() + nextD.getDy();
 		Point2D newPoint = new Point2D.Double(newPosX, newPosY);
 		return newPoint;
 	}
 
 	public int tilesAway(Point2D me, Point2D them) {
 		return ((int) PathManager.computeTotalSpaces(me, them));
 	}
 
 	/*
 	 * Methods to see where you are relative to a static position
 	 */
 
 	public static boolean atBoat(Point2D p) {
 		if (p.getX() == 0 && p.getY() == 0) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	public static boolean atTheWall(Point2D p) {
 		if (Math.abs(p.getX()) == NewPlayer.d || Math.abs(p.getY()) == NewPlayer.d) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	public static boolean illegalMove(Point2D p) {
 		if (Math.abs(p.getX()) > (NewPlayer.d) || Math.abs(p.getY()) > (NewPlayer.d)) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 }
