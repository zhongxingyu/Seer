 package isnork.g6;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.PriorityQueue;
 import java.util.Random;
 import java.util.Set;
 
 import isnork.sim.GameObject.Direction;
 import isnork.sim.SeaLifePrototype;
 
 import java.awt.geom.Point2D;
 
 public class BalancedStrategy extends Strategy {
 	public static final Point2D boatLocation = new Point2D.Double(0,0);
 
 	Point2D lastDestination;
 	Destination currentDestination;
 	int lastDestinationId;
 	int acceptableDanger;
 	DangerAvoidance dangerAvoid;
 	boolean stayAtBoat;
 	Node nextMove;
 	Point2D nextPosition;
 	double timeBackToBoat;
 	Random r = new Random();
 	
 
 	public BalancedStrategy(Set<SeaLifePrototype> seaLifePossibilites,
 			int penalty, int d, int r, int n, NewPlayer p) {
 		super(seaLifePossibilites, penalty, d, r, n, p);
 
 		acceptableDanger = 0;
 		lastDestinationId = 100;
 		currentDestination = new Destination(new Point2D.Double(0,0), 0, 100, true);
 		dangerAvoid = new DangerAvoidance();
 		stayAtBoat = false;
 		player.possibleDestinations = new LinkedList<Destination>();
 		player.currentPath.add(new Node(Direction.STAYPUT, player.minutesLeft));
 	}
 
 	@Override
 	public Direction nextMove() {
 		if (stayAtBoat && player.currentPosition.distance(boatLocation) == 0)
 		{
 			return Direction.STAYPUT;
 		}
 		
 		if (CoordinateCalculator.getLoadSpiralDestinations())
 		{
 			addSpiralDataToDestinations();
 		}
 		
 		LinkedList<Node> safePath = null;
 		while (safePath == null)
 		{
 			if (player.possibleDestinations.isEmpty())
 			{
 				CoordinateCalculator.updateCoordMap();
 				addSpiralDataToDestinations();
 			}
 		
 			if (player.destination == null 
 					|| player.currentPosition.distance(player.destination) == 0 
 					|| player.currentPath.isEmpty()
 					|| currentDestination.getPriority() < 0)
 			{
 				updateDestination();
 				updatePath();
 			}
 		
 			nextMove = player.currentPath.getFirst();
 			nextPosition = new Point2D.Double(nextMove.getDirection().getDx() + player.currentPosition.getX(),
 												nextMove.getDirection().getDy() + player.currentPosition.getY());
 			timeBackToBoat = (PathManager.computeDiagonalSpaces(nextPosition, boatLocation) * 3) 
 					+ (PathManager.computeAdjacentSpaces(nextPosition, boatLocation) * 2)
 					+ NewPlayer.dangerAvoidTravelTime; // computes how much time it would take to get back to the boat
 									// if I move where I'm intending to move
 
 			if (!stayAtBoat)
 			{
 				stayAtBoat = determineIfEndGame();
 				if (stayAtBoat)
 				{
 					updatePathForEndGame();
 				}
 			}
 			
 			if (timeBackToBoat < player.minutesLeft - 3)
 			{
 				if (dangerAvoid.isLocationDangerous(player.whatISee, nextPosition))
 				{
 					safePath = dangerAvoid.buildSafePath(player);
 					if (safePath != null)
 					{
 						updatePathToAvoidDanger(safePath);
 					}
 				}
 			}
			else
			{
				safePath = new LinkedList<Node>();
			}
 		}
 
 		nextMove = player.currentPath.pop();
 		if (validDirection(nextMove.getDirection()))
 		{
 			return nextMove.getDirection();
 		}
 		else
 		{
 			return Direction.STAYPUT;
 		}
 	}
 
 	private Destination getHighestPriorityDestination()
 	{
 		int hp = -1000;
 		Destination highestPriority = new Destination(new Point2D.Double(0,0), 0, 100, true);
 		for (Destination d : player.possibleDestinations)
 		{
 			if (d.getPriority() > hp)
 			{
 				highestPriority = d;
 				hp = d.getPriority();
 			}
 		}
 		
 		player.possibleDestinations.remove(highestPriority);
 		
 		return highestPriority;
 	}
 	
 	private void updateDestination() {
 		lastDestinationId = currentDestination.getId();
 		currentDestination = getHighestPriorityDestination();
 		player.destination = currentDestination.getDestination();
 	}
 	
 	private void updatePath() {
 		if(currentDestination.getId() <= 0 && lastDestinationId <= 0)
 		{
 			player.currentPath = PathManager.buildOrthogonalPath(player.currentPosition, player.destination, player.minutesLeft);
 		}
 		else
 		{
 			player.currentPath = PathManager.buildPath(player.currentPosition, player.destination, player.minutesLeft);
 		}
 
 	}
 	
 	private boolean validDirection(Direction d)
 	{
 		if (d == null)
 		{
 			return false;
 		}
 		else
 		{
 			nextPosition = new Point2D.Double(d.getDx() + player.currentPosition.getX(),
 					d.getDy() + player.currentPosition.getY());
 			if (pastTheWall(nextPosition))
 			{
 				return false;
 			}
 			else
 			{
 				return true;
 			}
 		}
 	}
 
 	private boolean determineIfEndGame() {
 		int travelTime = 0;
 		
 		if (player.getWhatPlayerSaw(player.getId()).seenAllCreatures(seaLifePossibilites.size()))
 		{
 			return true;
 		}
 
 		if (nextMove.getDirection().isDiag())
 		{
 			travelTime = 3;
 		}
 		else if (!nextMove.getDirection().isDiag() && nextMove.getDirection() != Direction.STAYPUT)
 		{
 			travelTime = 2;
 		}
 		
 		
 		if (timeBackToBoat > player.minutesLeft - travelTime)
 		{
 			return true;
 		}
 		else
 		{
 			return false;
 		}
 	}
 	
 	private void updatePathForEndGame()
 	{
 		player.destination = boatLocation;
 		player.currentPath = PathManager.buildPath(player.currentPosition, boatLocation, player.minutesLeft);
 	}
 
 //	public void createNextPath() {
 /*		if (player.minutesLeft == 8 * 60 - 1)
 		{
 			lastDestination = boatLocation;
 			destinationId = player.getId();
 			player.destination = CoordinateCalculator.coordMap.get(player.getId());
 			System.out.println("Player " + player.getId() + " is heading to an initial destination of " + player.destination);
 		}*/
 /*		else if (something to see)
 		{
 			go to that something;
 		}*/
 /*		else
 		{*/
 //			Point2D nextDest = determineNextDestination();
 //			Point2D nextDest = new Point2D.Double(r.nextInt() % (d + 1), r.nextInt() % (d + 1));
 /*			destinationId -= 1;
 			if (destinationId == n * -1)
 			{
 				destinationId = 0;
 			}
 			if (destinationId == player.getId())
 			{
 				CoordinateCalculator.updateCoordMap(CoordinateCalculator.nextIteration);
 			}
 			Point2D nextDest = CoordinateCalculator.coordMap.get(destinationId);
 			System.out.println("Player " + player.getId() + " is heading to a destination of " + player.destination);
 			lastDestination = player.destination;
 			player.destination = nextDest;
 		}
 		player.currentPath = PathManager.buildPath(player.currentPosition, player.destination, player.minutesLeft);
 	}*/
 	
 	private void updatePathToAvoidDanger(LinkedList<Node> safePath)
 	{
 		if (safePath == null)
 		{
 			System.out.println("NULL LINKED LIST");
 			return;
 		}
 /*		Point2D position = player.currentPosition;
 		double x = position.getX();
 		double y = position.getY();
 		for (Node n : safePath)
 		{
 			x += n.getDirection().getDx();
 			y += n.getDirection().getDy();
 		}
 		position.setLocation(x, y);
 		
 		if (player.destination == null)
 		{
 			updateDestination();
 		}
 		LinkedList<Node> directPath = PathManager.buildPath(position, player.destination, player.minutesLeft);
 		safePath.addAll(directPath);
 */		
 		player.currentPath = safePath;
 	}
 	
 	private void addSpiralDataToDestinations()
 	{
 		Collection<Destination> toRemove = new ArrayList<Destination>();
 		for (Destination d : player.possibleDestinations)
 		{
 			if (d.getPriority() <= 0)
 			{
 				toRemove.add(d);
 			}
 		}
 		player.possibleDestinations.removeAll(toRemove);
 				
 		int i = player.getId();
 		player.possibleDestinations.add(CoordinateCalculator.coordMap.get(i));
 		--i;
 		if (i <= n * -1)
 		{
 			i = 0;
 		}
 
 		while (i != player.getId())
 		{
 			if (i % 4 == player.getId() % 4)
 			{
 				Destination d = CoordinateCalculator.coordMap.get(i);
 				player.possibleDestinations.add(d);
 			}
 			--i;
 			if (i <= n * -1)
 			{
 				i = 0;
 			}
 		}
 	}
 	
 /*	private Point2D determineInitialDestination()
 	{
 		Point2D destination = new Point2D.Double();
 		// destination determined by player id
 		switch(Math.abs(player.getId()) % 8)
 		{
 		case 0:
 			destination.setLocation(0, d);
 			break;
 		case 1:
 			destination.setLocation(0, -1 * d);
 			break;
 		case 2:
 			destination.setLocation(d, 0);
 			break;
 		case 3:
 			destination.setLocation(-1 * d, 0);
 			break;
 		case 4:
 			destination.setLocation(d, d);
 			break;
 		case 5:
 			destination.setLocation(-1 * d, -1 * d);
 			break;
 		case 6:
 			destination.setLocation(-1 * d, d);
 			break;
 		case 7:
 			destination.setLocation(d, -1 * d);
 			break;
 		}
 		
 //		System.out.println("Destination for player with id " + player.getId() + " is " + destination);
 		return destination;
 	}*/
 	
 	private Point2D determineNextDestination()
 	{
 		Point2D nextDest;
 		// if player is headed to the middle (and presumably is there now)
 		if (player.destination.distance(boatLocation) == 0)
 		{
 			nextDest = getDestinationCounterClockwiseFrom(lastDestination);
 		}
 		// else if player is headed away from the middle, they should head back
 		else
 		{
 			nextDest = boatLocation;
 		}
 		
 		return nextDest;
 	}
 	
 	private Point2D getDestinationCounterClockwiseFrom(Point2D p)
 	{
 		Point2D p2 = new Point2D.Double();
 		if (p.distance(0,d) == 0)
 		{
 			p2.setLocation(d, d);
 		}
 		else if (p.distance(d, d) == 0)
 		{
 			p2.setLocation(d, 0);
 		}
 		else if (p.distance(d, 0) == 0)
 		{
 			p2.setLocation(d, -1 * d);
 		}
 		else if (p.distance(d, -1 * d) == 0)
 		{
 			p2.setLocation(0, -1 * d);
 		}
 		else if (p.distance(0, -1 * d) == 0)
 		{
 			p2.setLocation(-1 * d, -1 * d);
 		}
 		else if (p.distance(-1 * d, -1 * d) == 0)
 		{
 			p2.setLocation(-1 * d, 0);
 		}
 		else if (p.distance(-1 * d, 0) == 0)
 		{
 			p2.setLocation(-1 * d, d);
 		}
 		else if (p.distance(-1 * d, d) == 0)
 		{
 			p2.setLocation(0, d);
 		}
 		
 		return p2;
 	}
 		
 /*	private Direction determineNextDirection()
 	{
 		// make a list of where all the dangerous creatures are
 		List<Direction> dangerousDir = new ArrayList<Direction>();
 		Object[] whatISee = player.whatISee.toArray();
 		for (Object obj : whatISee)
 		{
 			// for each creature I see, check if it's dangerous
 			Observation o = (Observation)obj;
 			if (o.isDangerous()) // && o.happiness() > acceptableDanger)
 			{
 				// if it is, add what direction it's in to the list
 				dangerousDir.add(getDirectionFromPoints(player.currentPosition, o.getLocation()));
 			}
 		}
 		
 		// if surrounded by danger, don't move
 		if (dangerousDir.size() == 8)
 		{
 			return Direction.STAYPUT;
 		}
 		// test the direction from pattern against danger
 //		Direction nextDir = getNextDirInPattern();
 		while (dangerousDir.contains(nextDir))
 		{
 			// while it's dangerous, try a different direction
 			nextDir = moveClockwise(nextDir, 1);
 		}
 		
 		// return final direction decision
 		return nextDir;
 	}*/
 	
 /*	private Direction getNextDirInPattern()
 	{
 		if (atTheWall(player.currentPosition))
 		{
 			preferredDirection = moveClockwise(preferredDirection, 4);
 		}
 		if (atBoat(player.currentPosition))
 		{
 			preferredDirection = moveClockwise(preferredDirection, 1);
 		}
 		return preferredDirection;
 	}*/
 	
 /*	private boolean atBoat(Point2D p)
 	{
 		if (p.getX() == 0 && p.getY() == 0)
 		{
 			return true;
 		}
 		else
 		{
 			return false;
 		}
 	}*/
 	
 	private boolean pastTheWall(Point2D p)
 	{
 		if (Math.abs(p.getX()) > d || Math.abs(p.getY()) > d)
 		{
 			return true;
 		}
 		else
 		{
 			return false;
 		}
 	}
 	
 /*	private Direction moveClockwise(Direction d, int numSteps)
 	{
 		if (numSteps == 0 || d == Direction.STAYPUT)
 		{
 			return d;
 		}
 		else
 		{
 			switch (d)
 			{
 			case N:
 				d = Direction.NE;
 				break;
 			case NE:
 				d = Direction.E;
 				break;
 			case E:
 				d =  Direction.SE;
 				break;
 			case SE:
 				d = Direction.S;
 				break;
 			case S:
 				d = Direction.SW;
 				break;
 			case SW:
 				d = Direction.W;
 				break;
 			case W:
 				d = Direction.NW;
 				break;
 			case NW:
 				d = Direction.N;
 				break;
 			default:
 				d = Direction.STAYPUT;
 				break;
 			}
 			return moveClockwise(d, numSteps - 1);
 		}
 	}*/
 	
 /*	private Direction getDirectionFromPoints(Point2D from, Point2D to)
 	{
 		double deltax = from.getX() - to.getX();
 		double deltay = from.getY() - to.getY();
 		
 		Direction d = Direction.STAYPUT;
 		
 		if (deltax == 0 && deltay > 0)
 		{
 			d = Direction.N;
 		}
 		else if (deltax == 0 && deltay < 0)
 		{
 			d = Direction.S;
 		}
 		else if (deltax > 0 && deltay == 0)
 		{
 			d = Direction.W;
 		}
 		else if (deltax < 0 && deltay == 0)
 		{
 			d = Direction.E;
 		}
 		else if (deltax > 0 && deltay > 0)
 		{
 			d = Direction.NW;
 		}
 		else if (deltax > 0 && deltay < 0)
 		{
 			d = Direction.SW;
 		}
 		else if (deltax < 0 && deltay > 0)
 		{
 			d = Direction.NE;
 		}
 		else if (deltax < 0 && deltay < 0)
 		{
 			d = Direction.SE;
 		}
 
 		return d;
 	}*/
 
 }
