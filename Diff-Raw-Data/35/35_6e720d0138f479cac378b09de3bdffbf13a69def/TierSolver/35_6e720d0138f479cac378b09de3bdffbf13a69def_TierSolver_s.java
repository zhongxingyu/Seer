 package edu.berkeley.gamesman.solver;
 
 import java.io.IOException;
 import java.util.concurrent.CountDownLatch;
 
 import edu.berkeley.gamesman.core.*;
 import edu.berkeley.gamesman.database.Database;
 import edu.berkeley.gamesman.database.DatabaseHandle;
 import edu.berkeley.gamesman.game.TierGame;
 import edu.berkeley.gamesman.game.util.TierState;
 import edu.berkeley.gamesman.util.DebugFacility;
 import edu.berkeley.gamesman.util.Progressable;
 import edu.berkeley.gamesman.util.Util;
 import edu.berkeley.gamesman.util.qll.Pool;
 import edu.berkeley.gamesman.util.qll.Factory;
 
 /**
  * @author DNSpies
  */
 public class TierSolver extends Solver {
 	protected class TierSolveTask implements Runnable {
 		protected final long firstRecordIndex, numRecords;
 		protected final DatabaseHandle myReadHandle, myWriteHandle;
 		protected Configuration conf;
 		protected TierGame myGame;
 		protected TierState currentState;
 		private Record currentValue;
 		protected TierState[] childStates;
 		protected Record[] childRecords;
 
 		public TierSolveTask(long firstRecordIndex, long numRecords) {
 			this.firstRecordIndex = firstRecordIndex;
 			this.numRecords = numRecords;
 			myReadHandle = db.getHandle(true);
 			myWriteHandle = db.getHandle(false);
 		}
 
 		@Override
 		public final void run() {
 			prepareSolve();
 			solvePartialTier();
 			confPool.release(conf);
 			tasksFinished.countDown();
 		}
 
 		public void prepareSolve() {
 			conf = confPool.get();
 			myGame = (TierGame) conf.getGame();
 			currentState = myGame.hashToState(firstRecordIndex);
 			myGame.setState(currentState);
 			currentValue = myGame.newRecord();
 			childStates = myGame.newStateArray(myGame.maxChildren());
 			childRecords = myGame.newRecordArray(myGame.maxChildren());
 			try {
 				db.seek(myWriteHandle, firstRecordIndex);
 			} catch (IOException e) {
 				throw new Error(e);
 			}
 		}
 
 		public void solvePartialTier() {
 			long hash = firstRecordIndex;
 			int modCount = 0;
 			for (long trial = 0; trial < numRecords; trial++) {
 				if (modCount == Solver.STEP_SIZE) {
 					if (failed != null)
 						break;
 					if (progress != null)
 						synchronized (progress) {
 							progress.progress();
 						}
 					addFinished(modCount);
 					modCount = 0;
 				}
 				myGame.getState(currentState);
 				Value v = myGame.primitiveValue();
 				if (v == Value.UNDECIDED) {
 					evaluateAndFetchChildren(currentValue);
 					store(hash, currentValue);
 				} else {
 					currentValue.value = v;
 					currentValue.remoteness = 0;
 					store(hash, currentValue);
 				}
 				if (trial < numRecords - 1)
 					myGame.nextHashInTier();
 				hash++;
 				modCount++;
 			}
 			addFinished(modCount);
 			if (progress != null)
 				synchronized (progress) {
 					progress.progress();
 				}
 		}
 
 		protected void evaluateAndFetchChildren(Record currentValue) {
 			int numChildren = myGame.validMoves(childStates);
 			for (int i = 0; i < numChildren; i++) {
 				try {
 					long recordLong = db.readRecord(myReadHandle,
 							myGame.stateToHash(childStates[i]));
 					myGame.longToRecord(childStates[i], recordLong,
 							childRecords[i]);
 					childRecords[i].previousPosition();
 				} catch (IOException e) {
 					throw new Error(e);
 				}
 			}
 			currentValue.set(myGame.combine(childRecords, numChildren));
 		}
 
 		protected void store(long recordIndex, Record currentValue) {
 			long recordNum = myGame.recordToLong(currentValue);
 			try {
 				db.writeNextRecord(myWriteHandle, recordNum);
 			} catch (IOException e) {
 				throw new Error(e);
 			}
 		}
 
 	}
 
 	protected final long minSplitSize;
 	protected int currentTier;
 	protected TierGame myGame;
 	protected CountDownLatch tasksFinished = new CountDownLatch(0);
 	protected long[] splits = new long[0];
 	protected int currentSplit;
 	protected final int preferredSplits;
 	final Pool<Configuration> confPool = new Pool<Configuration>(
 			new Factory<Configuration>() {
 
 				@Override
 				public Configuration newObject() {
 					return conf.cloneAll();
 				}
 
 				@Override
 				public void reset(Configuration t) {
 				}
 			});
 	private long firstHash;
 	private long numHashes;
 	private final boolean wholeGame;
 	private final Progressable progress;
 	private volatile long recordsFinished;
 
 	public TierSolver(Configuration conf, Database db) {
 		super(conf, db);
 		myGame = (TierGame) conf.getGame();
 		currentTier = myGame.numberOfTiers();
 		minSplitSize = conf.getLong("gamesman.minimum.split.size", 4096);
 		preferredSplits = conf.getInteger("gamesman.splits",
 				conf.getInteger("gamesman.threads", 1));
 		wholeGame = true;
 		progress = null;
 	}
 
 	private void addFinished(long hashes) {
 		recordsFinished += hashes;
 		Util.debug(DebugFacility.SOLVER, (wholeGame ? "Tier " + currentTier
 				+ " " : "")
 				+ recordsFinished * 10000 / numHashes / 100F + "% complete");
 	}
 
 	public TierSolver(Configuration conf, Database db, int tier,
 			long firstHash, long numHashes, Progressable progress) {
 		super(conf, db);
 		myGame = (TierGame) conf.getGame();
 		currentTier = tier;
 		minSplitSize = conf.getLong("gamesman.minimum.split.size", 4096);
 		preferredSplits = conf.getInteger("gamesman.splits",
 				conf.getInteger("gamesman.threads", 1));
 		this.firstHash = firstHash;
 		this.numHashes = numHashes;
 		wholeGame = false;
 		splits = Util.getSplits(firstHash, numHashes, preferredSplits,
 				minSplitSize);
 		currentSplit = 0;
 		tasksFinished = new CountDownLatch(splits.length - 1);
 		this.progress = progress;
 	}
 
 	@Override
 	public Runnable nextAvailableJob() {
 		if (failed != null)
 			return null;
 		if (currentSplit >= splits.length - 1) {
 			if (currentTier == 0 || !wholeGame)
 				return null;
 			boolean interrupted;
 			do {
 				interrupted = false;
 				try {
 					tasksFinished.await();
 				} catch (InterruptedException e) {
					interrupted = true;
					e.printStackTrace();
 				}
 			} while (interrupted);
 			decrTier();
 		}
 		return nextJob();
 	}
 
 	protected Runnable nextJob() {
 		long firstRecordIndex = splits[currentSplit], numRecords = splits[currentSplit + 1]
 				- firstRecordIndex;
 		currentSplit++;
 		return getSolveTask(firstRecordIndex, numRecords);
 	}
 
 	protected TierSolveTask getSolveTask(long firstRecordIndex, long numRecords) {
 		return new TierSolveTask(firstRecordIndex, numRecords);
 	}
 
 	protected void decrTier() {
 		currentTier--;
 		System.out.println("Solving tier " + currentTier);
 		currentSplit = 0;
 		firstHash = myGame.hashOffsetForTier(currentTier);
 		numHashes = myGame.numHashesForTier(currentTier);
 		recordsFinished = 0L;
 		splits = Util.getSplits(firstHash, numHashes, preferredSplits,
 				minSplitSize);
 		tasksFinished = new CountDownLatch(splits.length - 1);
 	}
 
 }
