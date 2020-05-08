 package edu.berkeley.gamesman.solver;
 
 import java.math.BigInteger;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.concurrent.BrokenBarrierException;
 import java.util.concurrent.CyclicBarrier;
 
 import edu.berkeley.gamesman.core.Configuration;
 import edu.berkeley.gamesman.core.Record;
 import edu.berkeley.gamesman.core.Game;
 import edu.berkeley.gamesman.core.Solver;
 import edu.berkeley.gamesman.core.TieredGame;
 import edu.berkeley.gamesman.core.WorkUnit;
 import edu.berkeley.gamesman.util.DebugFacility;
 import edu.berkeley.gamesman.util.Pair;
 import edu.berkeley.gamesman.util.Task;
 import edu.berkeley.gamesman.util.Util;
 
 /**
  * TierSolver documentation stub
  * 
  * @author Steven Schlansker
  * @param <T> The state type for the game
  * 
  */
 public final class TierSolver<T> extends Solver {
 
 	protected TieredGame<T> myGame;
 	protected Configuration conf;
 
 	@Override
 	public WorkUnit prepareSolve(Configuration inconf, Game<Object> game) {
 
 		myGame = Util.checkedCast(game);
 		tier = myGame.numberOfTiers() - 1;
 		offset = myGame.hashOffsetForTier(tier);
 		updater = new TierSolverUpdater();
 		conf = inconf;
 
 		return new TierSolverWorkUnit();
 	}
 
 	protected void solvePartialTier(TieredGame<T> game, BigInteger start,
 			BigInteger end, TierSolverUpdater t) {
 		BigInteger current = start.subtract(BigInteger.ONE);
 
 		while (current.compareTo(end) < 0) {
 			current = current.add(BigInteger.ONE);
 
 			if (current.mod(BigInteger.valueOf(10000)).compareTo(
 					BigInteger.ZERO) == 0)
 				t.calculated(10000);
 
 			T state = game.hashToState(current);
 
			Collection<Pair<String,T>> children = game.validMoves(state);

			if (children.size() == 0) {
				Record prim = new Record(conf, game.primitiveValue(state));

				db.putRecord(current, prim);
			} else {
 				ArrayList<Record> vals = new ArrayList<Record>(children.size());

 				for (Pair<String,T> child : children) {
 					vals.add(db.getRecord(game.stateToHash(child.cdr)));
 				}
 
 				Record newVal = Record.combine(conf, vals);
 				db.putRecord(current, newVal);
 			}
 		}
 	}
 
 	protected int nextIndex = 0;
 	protected TierSolverUpdater updater;
 	protected BigInteger offset;
 	protected CyclicBarrier barr;
 	protected int tier;
 
 	private boolean needs2Sync;
 
 	protected Pair<BigInteger, BigInteger> nextSlice() {
 		if (needs2Sync) {
 			Util.debug(DebugFacility.Threading,"Thread waiting to tier-sync");
 			try {
 				if (barr != null)
 					barr.await();
 			} catch (InterruptedException e) {
 				Util.fatalError(
 						"TierSolver thread was interrupted while waiting!", e);
 			} catch (BrokenBarrierException e) {
 				Thread.yield();
 				return nextSlice(); // Retry
 			}
 		}
 
 		synchronized (this) {
 			if (tier < 0)
 				return null;
 			final BigInteger step = BigInteger.valueOf(1000);
 			BigInteger ret = offset, end;
 			offset = offset.add(step);
 			end = ret.add(step);
 			if (end.compareTo(myGame.lastHashValueForTier(tier)) >= 0) {
 				Util.debug(DebugFacility.Threading,"Reached end of tier");
 				end = myGame.lastHashValueForTier(tier);
 				tier--;
 				if (tier >= 0)
 					offset = myGame.hashOffsetForTier(tier);
 				needs2Sync = true;
 			}
 			return new Pair<BigInteger, BigInteger>(ret, end);
 		}
 	}
 
 	private final class TierSolverWorkUnit implements WorkUnit {
 
 		private int index;
 
 		TierSolverWorkUnit() {
 
 			// if(!(g instanceof TieredGame))
 			// Util.fatalError("Attempted to use tiered solver on non-tiered game");
 			this.index = nextIndex++;
 			barr = new CyclicBarrier(nextIndex);
 		}
 
 		@SuppressWarnings("synthetic-access")
 		public void conquer() {
 			Util.debug(DebugFacility.Solver,"Started the solver... (" + index + ")");
 			Thread.currentThread().setName(
 					"Solver (" + index + "): " + myGame.toString());
 
 			Pair<BigInteger, BigInteger> slice;
 			while ((slice = nextSlice()) != null) {
 				Util.debug(DebugFacility.Threading,"Beginning to solve slice " + slice + " in thread "
 						+ index);
 				solvePartialTier(myGame, slice.car, slice.cdr, updater);
 				db.flush();
 			}
 
 			synchronized (TierSolver.this) {
 				if (barr != null) {
 					updater.complete();
 					barr.reset();
 					barr = null;
 				}
 			}
 		}
 
 		public List<WorkUnit> divide(int num) {
 			ArrayList<WorkUnit> arr = new ArrayList<WorkUnit>(num);
 			arr.add(this);
 			for (int i = 1; i < num; i++)
 				arr.add(new TierSolverWorkUnit());
 			return arr;
 		}
 	}
 
 	private final class TierSolverUpdater {
 
 		private BigInteger total = BigInteger.ZERO;
 		private Task t;
 		private long lastUpdate = 0;
 
 		TierSolverUpdater() {
 			t = Task.beginTask("Tier solving \"" + myGame.toString() + "\"");
 			t.setTotal(myGame.lastHashValueForTier(myGame.numberOfTiers() - 1));
 		}
 
 		synchronized void calculated(int howMuch) {
 			total = total.add(BigInteger.valueOf(howMuch));
 			if (t != null && System.currentTimeMillis() - lastUpdate > 1000) {
 				t.setProgress(total);
 				lastUpdate = System.currentTimeMillis();
 			}
 		}
 
 		public synchronized void complete() {
 			if (t != null)
 				t.complete();
 			t = null;
 		}
 	}
 }
