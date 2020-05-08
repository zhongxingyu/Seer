 package edu.berkeley.gamesman.solver;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.concurrent.BrokenBarrierException;
 import java.util.concurrent.CyclicBarrier;
 
 import edu.berkeley.gamesman.core.*;
 import edu.berkeley.gamesman.util.*;
 
 /**
  * TierSolver documentation stub
  * 
  * @author Steven Schlansker
  * @param <T>
  *            The state type for the game
  */
 public class TierSolver<T> extends Solver {
 
 	/**
 	 * The number of positions to go through between each update/reset
 	 */
 	private int split, minRecordsInSplit, maxRecordsInSplit;
 
 	private int count;
 
 	private long[] starts;
 
 	boolean hadooping;
 
 	@Override
 	public WorkUnit prepareSolve(Configuration inconf) {
 
 		tier = Util.<TieredGame<T>, Game<?>> checkedCast(inconf.getGame())
 				.numberOfTiers();
 		updater = new TierSolverUpdater();
 		hadooping = false;
 		flusher.run();
 		needs2Reset = false;
 		return new TierSolverWorkUnit(inconf);
 	}
 
 	protected void solvePartialTier(Configuration conf, long start,
 			long hashes, TierSolverUpdater t, Database inRead, Database inWrite) {
 		long current = start;
 		TieredGame<T> game = Util.checkedCast(conf.getGame());
 		for (long i = 0L; i < hashes; i++) {
 			if (current % STEP_SIZE == 0)
 				t.calculated(STEP_SIZE);
 
 			T state = game.hashToState(current);
 
 			PrimitiveValue pv = game.primitiveValue(state);
 
 			if (pv.equals(PrimitiveValue.UNDECIDED)) {
 				assert Util.debug(DebugFacility.SOLVER,
 						"Primitive value for state " + current
 								+ " is undecided");
 				Collection<Pair<String, T>> children = game.validMoves(state);
 				ArrayList<Record> vals = new ArrayList<Record>(children.size());
 				for (Pair<String, T> child : children) {
 					vals.add(inRead.getRecord(game.stateToHash(child.cdr)));
 				}
 				Record[] theVals = new Record[vals.size()];
 				Record newVal = game.combine(vals.toArray(theVals), 0,
 						theVals.length);
 				inWrite.putRecord(current, newVal);
 			} else {
 				Record prim = game.newRecord();
 				prim.value = pv;
 				assert Util.debug(DebugFacility.SOLVER,
 						"Primitive value for state " + current + " is " + prim);
 				inWrite.putRecord(current, prim);
 			}
 			++current;
 		}
 		assert Util.debug(DebugFacility.THREADING,
 				"Reached end of partial tier at " + (start + hashes));
 	}
 
 	protected int nextIndex = 0;
 
 	protected TierSolverUpdater updater;
 
 	protected CyclicBarrier barr;
 
 	private final Runnable flusher = new Runnable() {
 		public void run() {
 			if (writeDb != null)
 				writeDb.flush();
 			--tier;
 			needs2Sync = false;
 			if (tier < 0)
 				updater.complete();
 			else {
 				if (barr != null)
 					needs2Reset = true;
 				long fullStart = Util.<TieredGame<T>, Game<?>> checkedCast(
 						conf.getGame()).hashOffsetForTier(tier);
 				long fullSize = Util.<TieredHasher<T>, Hasher<?>> checkedCast(
 						conf.getHasher()).numHashesForTier(tier);
 				starts = Util.groupAlignedTasks(split, fullStart, fullSize,
 						conf.recordsPerGroup);
 			}
 		}
 	};
 
 	protected int tier;
 
 	private volatile boolean needs2Sync = false;
 
 	private volatile boolean needs2Reset = false;
 
 	protected Pair<Long, Long> nextSlice(Configuration conf) {
 		while (true) {
 			if (needs2Sync) {
 				if (hadooping) {
 					return null;
 				}
 				if (barr == null)
 					flusher.run();
 				else {
 					assert Util.debug(DebugFacility.THREADING,
 							"Thread waiting to tier-sync");
 					try {
 						barr.await();
 						synchronized (this) {
 							if (needs2Reset) {
 								needs2Reset = false;
 								barr.reset();
 							}
 						}
 					} catch (InterruptedException e) {
 						Util
 								.fatalError(
 										"TierSolver thread was interrupted while waiting!",
 										e);
 					} catch (BrokenBarrierException e) {
 						Util.fatalError("Barrier Broken", e);
 					}
 				}
 			}
 			synchronized (this) {
 				if (!needs2Sync) {
 					if (tier < 0) {
 						return null;
 					}
 					Pair<Long, Long> slice = new Pair<Long, Long>(
 							starts[count], starts[count + 1] - starts[count]);
 					if (count < starts.length - 2) {
 						++count;
 					} else {
 						count = 0;
 						needs2Sync = true;
 					}
 					return slice;
 				}
 			}
 		}
 	}
 
 	private final class TierSolverWorkUnit implements WorkUnit {
 
 		private int index;
 
 		Configuration conf;
 
 		Pair<Long, Long> thisSlice;
 
 		TierSolverWorkUnit(Configuration conf) {
 			this.conf = conf;
 			this.index = nextIndex++;
 		}
 
 		public void conquer() {
 			assert Util.debug(DebugFacility.SOLVER, "Started the solver... ("
 					+ index + ")");
 			Thread.currentThread().setName(
 					"Solver (" + index + "): " + conf.getGame().toString());
 			Pair<Long, Long> slice;
 			while ((slice = nextSlice(conf)) != null) {
 				thisSlice = slice;
				assert Util.debug(DebugFacility.THREADING, "Solving "
						+ conf.getGame() + ": " + slice.car + "-"
 						+ (slice.car + slice.cdr));
 				if (hadooping) {
 					try {
 						Database myWrite = writeDb.beginWrite(tier, slice.car,
 								slice.car + slice.cdr);
 						solvePartialTier(conf, slice.car, slice.cdr, updater,
 								readDb, myWrite);
 						writeDb.endWrite(tier, myWrite, slice.car, slice.car
 								+ slice.cdr);
 					} catch (Util.FatalError e) {
 						e.printStackTrace(System.out);
 						throw e;
 					} catch (Throwable e) {
 						e.printStackTrace(System.out);
 						throw new RuntimeException(e);
 					}
 				} else
 					solvePartialTier(conf, slice.car, slice.cdr, updater,
 							readDb, writeDb);
 			}
 			if (barr != null)
 				try {
 					barr.await();
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				} catch (BrokenBarrierException e) {
 					e.printStackTrace();
 				}
 		}
 
 		public List<WorkUnit> divide(int num) {
 			ArrayList<WorkUnit> arr = new ArrayList<WorkUnit>(num);
 			arr.add(this);
 			for (int i = 1; i < num; i++)
 				arr.add(new TierSolverWorkUnit(conf.cloneAll()));
 			if (hadooping || num == 1)
 				barr = null;
 			else
 				barr = new CyclicBarrier(num, flusher);
 			return arr;
 		}
 
 		@Override
 		public String toString() {
 			String str = "WorkUnit " + index + "; slice is ";
 			if (thisSlice != null) {
 				str += "[" + thisSlice.car + "-" + thisSlice.cdr + "]";
 			} else {
 				str += "null";
 			}
 			return str;
 		}
 	}
 
 	final class TierSolverUpdater {
 
 		private long total = 0;
 
 		private Task t;
 
 		TierSolverUpdater() {
 			TieredGame<T> myGame = Util.checkedCast(conf.getGame());
 			t = Task.beginTask("Tier solving \"" + myGame.describe() + "\"");
 			t.setTotal(myGame.numHashes());
 		}
 
 		synchronized void calculated(int howMuch) {
 			total += howMuch;
 			if (t != null) {
 				t.setProgress(total);
 			}
 		}
 
 		public void complete() {
 			if (t != null)
 				t.complete();
 			t = null;
 		}
 	}
 
 	@Override
 	public void initialize(Configuration conf) {
 		super.initialize(conf);
 		minRecordsInSplit = conf.getInteger("gamesman.minSplit", 0);
 		maxRecordsInSplit = conf.getInteger("gamesman.maxSplit", 0);
 		split = conf.getInteger("gamesman.split", conf.getInteger(
 				"gamesman.threads", 1));
 	}
 
 	/**
 	 * @param conf
 	 *            The configuration object
 	 * @param tier
 	 *            The tier
 	 * @param startHash
 	 *            The tier to solve
 	 * @param endHash
 	 *            The range in the given tier to solve
 	 * @return A WorkUnit for solving solveSpace
 	 */
 	public WorkUnit prepareSolve(Configuration conf, int tier, long startHash,
 			long endHash) {
 		updater = new TierSolverUpdater();
 		this.tier = tier;
 		if (split <= 0) {
 			split = 1;
 		}
 		if (minRecordsInSplit > 0) {
 			if ((endHash - startHash) / split < minRecordsInSplit) {
 				System.out.println("Too few records "
 						+ ((endHash - startHash) / split) + " in " + split
 						+ " splits for tier " + tier);
 				;
 				split = (int) ((endHash - startHash) / minRecordsInSplit);
 				if (split <= 0) {
 					split = 1;
 				}
 				System.out.println("Setting to " + split + " splits ("
 						+ ((endHash - startHash) / split) + ")");
 			}
 		}
 		if (maxRecordsInSplit > 0) {
 			if ((endHash - startHash) / split > maxRecordsInSplit) {
 				System.out.println("Too many records "
 						+ ((endHash - startHash) / split) + " in " + split
 						+ " splits for tier " + tier);
 				split = (int) ((endHash - startHash) / maxRecordsInSplit);
 				if (split <= 0) {
 					split = 1;
 				}
 				System.out.println("Setting to " + split + " splits ("
 						+ ((endHash - startHash) / split) + ")");
 			}
 		}
 		starts = Util.groupAlignedTasks(split, startHash, endHash - startHash,
 				conf.recordsPerGroup);
 		hadooping = true;
 		return new TierSolverWorkUnit(conf);
 	}
 }
