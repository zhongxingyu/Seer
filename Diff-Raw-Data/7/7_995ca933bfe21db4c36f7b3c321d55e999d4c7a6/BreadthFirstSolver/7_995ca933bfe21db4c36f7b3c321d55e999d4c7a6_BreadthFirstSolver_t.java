 package edu.berkeley.gamesman.solver;
 
 import java.math.BigInteger;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.List;
 
 import edu.berkeley.gamesman.core.Configuration;
 import edu.berkeley.gamesman.core.Database;
 import edu.berkeley.gamesman.core.Game;
 import edu.berkeley.gamesman.core.PrimitiveValue;
 import edu.berkeley.gamesman.core.Record;
 import edu.berkeley.gamesman.core.RecordFields;
 import edu.berkeley.gamesman.core.Solver;
 import edu.berkeley.gamesman.core.WorkUnit;
 import edu.berkeley.gamesman.util.DebugFacility;
 import edu.berkeley.gamesman.util.Pair;
 import edu.berkeley.gamesman.util.Task;
 import edu.berkeley.gamesman.util.Util;
 
 /**
  * A solver designed with puzzles in mind that will do a breadth first
  * search from all of the starting positions until every reachable position
  * has been hit. It is assumed that every starting position is a WIN and that
  * the puzzle is reversible.
  * @author Patrick Horn (with Jeremy Fleischman watching)
  */
 public class BreadthFirstSolver extends Solver {
 
 	Configuration conf;
 
 	@Override
 	public WorkUnit prepareSolve(Configuration config, Game<Object> game) {
 		conf = config;
 		int maxRemoteness = Integer.parseInt(conf.getProperty("gamesman.solver.maxRemoteness", "-1"));
 		if (maxRemoteness <= 0) {
 			maxRemoteness = Integer.MAX_VALUE;
 		}
 		return new BreadthFirstWorkUnit<Object>(game, db, maxRemoteness);
 	}
 
 	class BreadthFirstWorkUnit<T> implements WorkUnit {
 
 		final private Game<T> game;
 		final private Database database;
 		final private int maxRemoteness;
 		
 		BreadthFirstWorkUnit(Game<T> g, Database db, int maxRemoteness) {
 			game = g;
 			database = db;
 			this.maxRemoteness = maxRemoteness;
 		}
 		
 		public void conquer() {
 			HashSet<BigInteger> seen = new HashSet<BigInteger>();
 			BigInteger maxHash = game.lastHash();
 			BigInteger numPositionsInLevel = BigInteger.ZERO;
 			for (T s : game.startingPositions()) {
 				BigInteger hash = game.stateToHash(s);
 				seen.add(hash);
 				database.putRecord(hash, new Record(conf,game.primitiveValue(s)));
 				numPositionsInLevel = numPositionsInLevel.add(BigInteger.ONE);
 			}
			BigInteger numPositionsSeen = numPositionsInLevel;
 			int remoteness = 0;
 			Task solveTask = Task.beginTask(String.format("BFS solving \"%s\"", game.describe()));
 			solveTask.setTotal(maxHash);
 			solveTask.setProgress(0);
 			while (!numPositionsInLevel.equals(BigInteger.ZERO) && remoteness < maxRemoteness) {
				Util.debug(DebugFacility.Solver, "Number of states at remoteness " + remoteness + ": " + numPositionsInLevel);
 				numPositionsInLevel = BigInteger.ZERO;
 				for (BigInteger hash : Util.bigIntIterator(maxHash)) {
 					if (seen.contains(hash)) {
 						Record rec = database.getRecord(hash);
 						if (rec.get(RecordFields.Remoteness) == remoteness) {
 							for (Pair<String,T> child : game.validMoves(game.hashToState(hash))) {
 								BigInteger childhash = game.stateToHash(child.cdr);
 								if (!seen.contains(childhash)) {
 									Record childrec = new Record(conf, PrimitiveValue.Win);
 									childrec.set(RecordFields.Remoteness, remoteness + 1);
 									database.putRecord(childhash, childrec);
 									seen.add(childhash);
 									numPositionsInLevel = numPositionsInLevel.add(BigInteger.ONE);
 									numPositionsSeen = numPositionsSeen.add(BigInteger.ONE);
 									if(numPositionsSeen.remainder(BigInteger.valueOf(100000)).equals(BigInteger.ZERO))
 										solveTask.setProgress(numPositionsSeen);
 								}
 							}
 						}
 					}
 				}
 				remoteness += 1;
 			}
 			solveTask.complete();
 			Util.debug(DebugFacility.Solver, "Solving finished!!! Max remoteness is "+(remoteness-1)+". Total positions seen = "+numPositionsSeen);
 		}
 
 		public List<WorkUnit> divide(int num) {
 			WorkUnit wu = this;
 			return Arrays.asList(wu);
 		}
 		
 	}
 	
 }
