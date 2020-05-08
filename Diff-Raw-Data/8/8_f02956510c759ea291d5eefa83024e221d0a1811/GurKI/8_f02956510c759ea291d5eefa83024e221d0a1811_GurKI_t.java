 package gdp.racetrack;
 
 import java.util.AbstractMap.SimpleImmutableEntry;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 
 public class GurKI implements AI, Runnable {
 
 	private class Position extends Point {
 		Position(final Point point) {
 			super(point.getX(), point.getY());
 		}
 
 		Position(final int x, final int y) {
 			super(x, y);
 		}
 
 		Position translocate(final Velocity Velocity) {
 			return new Position(add(Velocity));
 		}
 
 		Velocity sub(final Position position) {
 			return new Velocity(getX() - position.getX(), getY() - position.getY());
 		}
 	}
 
 	private final Velocity restvelocity = new Velocity(0, 0);
 
 	private class Velocity extends Vec2D {
 		Velocity(final int x, final int y) {
 			super(x, y);
 		}
 
 		Velocity(final Vec2D vector) {
 			super(vector.x, vector.y);
 		}
 
 		Velocity accelerate(final Acceleration acceleration) {
 			return new Velocity(x + acceleration.x, y + acceleration.y);
 		}
 	}
 
 	public final Acceleration[] offsets = new Acceleration[] { new Acceleration(-1, -1), new Acceleration(-1, 0), new Acceleration(-1, 1), new Acceleration(0, -1),
 			new Acceleration(0, 1), new Acceleration(1, -1), new Acceleration(1, 0), new Acceleration(1, 1) };
 
 	private class Acceleration extends Vec2D {
 		Acceleration(final int x, final int y) {
 			super(x, y);
 		}
 	}
 
 	final Configuration nullconfig = new Configuration(null, (Velocity) null); // The limbo you are in before you choose a starting point
 
 	@SuppressWarnings("serial")
 	private class Configuration extends SimpleImmutableEntry<Position, Velocity> {
 		Configuration(final Position p, final Velocity v) {
 			super(p, v);
 		}
 
 		Configuration(final Position p, final Position p2) {
 			super(p, p2.sub(p));
 		}
 
 		public Configuration decelerate(final Acceleration offset) {
 			return new Configuration(getKey(), getValue().accelerate(new Acceleration(-offset.x, -offset.y)));
 		}
 	}
 
 	private final HashSet<Position> track = new HashSet<Position>();
 	private final Game game;
 
 	public GurKI(final Game game) {
 		this.game = game;
 		final Thread analyzer = new Thread(this);
 		analyzer.setPriority(Thread.MIN_PRIORITY);
 		analyzer.start();
 	}
 
 	HashMap<Configuration, Double> utility;
 	final HashSet<Configuration> winconfigs = new HashSet<Configuration>();
 	final HashMap<Configuration, HashSet<Configuration>> backward = new HashMap<Configuration, HashSet<Configuration>>();
 	final HashMap<Configuration, HashSet<Configuration>> forward = new HashMap<Configuration, HashSet<Configuration>>();
 
 	@Override
 	public void run() {
 		final Position[] map = new Position[game.getMap().getSize().x * game.getMap().getSize().y];
 		final Vec2D mapsize = game.getMap().getSize();
 		utility = new HashMap<Configuration, Double>((int) Math.pow(mapsize.x*mapsize.y,1.5));
 		for (int x = 0; x < mapsize.x; x++)
 			for (int y = 0; y < mapsize.y; y++) {
 				final Position position = new Position(x, y);
 				map[game.getMap().getSize().y * x + y] = position;
 				switch (game.getMap().getPointType(position)) {
 				case START:
 					final Configuration startconfig = new Configuration(position, restvelocity);
 					utility.put(startconfig, 1.0);
 					addToGraph(nullconfig, startconfig);
 				case TRACK:
 					track.add(position);
 				default:
 				}
 			}
 		for (final Position positionfrom : track)
 			for (final Position positionto : map) {
 				final Turn turn = game.getRule().getTurnResult(positionfrom, positionto);
 				final Configuration postturnconfig = new Configuration(new Position(turn.getNewPosition()), new Velocity(turn.getNewVelocity()));
 				if (turn.isTurnAllowed()) {
 					double util = 0;
 					final Configuration decidedpreturnconfig = new Configuration(positionfrom, positionto);
 					for (final Acceleration offset : offsets) {
 						final Configuration undecidedpreturnconfig = decidedpreturnconfig.decelerate(offset);
 						addToGraph(undecidedpreturnconfig, postturnconfig);
 						if (turn.crossFinishLine() && turn.isPathValid()) {
 							winconfigs.add(postturnconfig);
 							util += 1000;
 						} else {
 							util++;
 						}
 					}
 					if (util != 0)
 						utility.put(postturnconfig, util);
 				}
 			}
 		// Remind me to remove this once the map starts changing during runtime:
 		final LinkedList<Configuration> checkforunreachability = new LinkedList<Configuration>(forward.keySet());
 		checkforunreachability.remove(nullconfig); // Is this dirty?
 		while (!checkforunreachability.isEmpty()) {
 			final Configuration config = checkforunreachability.pop();
 			if (!backward.containsKey(config)) {
 				checkforunreachability.addAll(forward.get(config));
 				winconfigs.remove(config);
 				forward.remove(config);
 			}
 		}
 		while (true) {
 			final HashMap<Configuration, Double> newtility = new HashMap<Configuration, Double>(utility.size());
 			for (final Configuration config : utility.keySet()) {
 				HashSet<Configuration> hashSet = backward.get(config);
 				if(hashSet==null)
 					continue;
 				final double addend = utility.get(config) / hashSet.size() / 2; // Yes, I know, this is the core of the whole AI and this line is not thought
 															// through at all.
 				for (final Configuration fromconfig : backward.get(config))
 					utiladd(newtility, addend, fromconfig);
 			}
 			for (final Configuration winconfig : winconfigs)
 				utiladd(newtility, 1000.0, winconfig);
 			utility = newtility;
 		}
 	}
 
 	private void utiladd(final HashMap<Configuration, Double> newtility, final double addend, Configuration fromconfig) {
 		Double util = newtility.get(fromconfig);
 		if (util == null)
 			newtility.put(fromconfig, addend);
 		else
 			newtility.put(fromconfig, util + addend);
 	}
 
 	private void addToGraph(final Configuration fromconfig, final Configuration newconfig) {
 		addToMapSet(newconfig, fromconfig, backward);
 		addToMapSet(fromconfig, newconfig, forward);
 	}
 
 	private void addToMapSet(final Configuration keyconfig, final Configuration valueconfig, final HashMap<Configuration, HashSet<Configuration>> setmap) {
 		HashSet<Configuration> set = setmap.get(keyconfig);
 		if (set == null) {
 			set = new HashSet<Configuration>();
 			setmap.put(keyconfig, set);
 		}
 		set.add(valueconfig);
 	}
 
 	public class GurBot extends Player {
 		Configuration config = nullconfig;
 
 		@Override
 		protected void onUpdateVelocity(final Vec2D oldVelocity, final Vec2D newVelocity) {
 			config = new Configuration(config.getKey(), new Velocity(newVelocity));
 		}
 
 		@Override
 		protected void onUpdatePosition(final Point oldPos, final Point newPos) {
 			config = new Configuration(new Position(newPos), config.getValue());
 		}
 
 		@Override
 		public Point turn() {
 			final HashSet<Configuration> choices = new HashSet<Configuration>();
 			for (final Turn turn : game.getRule().getAllowedTurns(this))
				choices.add(new Configuration(new Position(turn.getDestination()), new Velocity(turn.getNewVelocity())));
			Point newPosition=chooseNextConfig(choices);
			for (final Turn turn : game.getRule().getAllowedTurns(this))
				if(turn.getDestination().equals(newPosition))
					return turn.getNewPosition();
			throw new RuntimeException();
 //			return chooseNextConfig(forward.get(config));
 		}
 
 		@Override
 		protected Point chooseStart(final List<Point> possiblePositions) {
 			final HashSet<Configuration> choices = new HashSet<Configuration>();
 			for (final Point startpoint : possiblePositions)
 				choices.add(new Configuration(new Position(startpoint), restvelocity));
 			return chooseNextConfig(choices);
 		}
 
 		private Point chooseNextConfig(final Collection<Configuration> choices) {
 			double utilitysum = 0;
 			for (final Configuration nextconfig : choices)
 				if(utility.get(nextconfig)!=null)
 					utilitysum += utility.get(nextconfig);
 			double select = Math.random() * utilitysum;
 			for (final Configuration nextconfig : choices)
 				if(utility.get(nextconfig)!=null)
 					if ((select -= utility.get(nextconfig)) <= 0)
 						return nextconfig.getKey();
 			assert 1 == 0;
 			return null;
 		}
 //		@Override
 //		public Point turn() {
 //			return position.translocate(velocity.accelerate(offsets[(int) (Math
 //					.random() * 8)]));
 //		}
 	}
 
 	@Override
 	public boolean isDifficultySupported() {
 		return false;
 	}
 
 	@Override
 	public Player createBot(final Difficulty difficulty) {
 		return new GurBot();
 	}
 }
