 package edu.berkeley.gamesman.solver;
 
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.TimeUnit;
 
 import edu.berkeley.gamesman.core.Configuration;
 import edu.berkeley.gamesman.database.Database;
 import edu.berkeley.gamesman.util.Util;
 
 /**
  * A Solver is responsible for solving a Game and storing the result to a
  * Database
  * 
  * @author David Spies
  */
 public abstract class Solver {
 
 	private class RunWrapper implements Runnable {
 		private final Runnable inner;
 
 		private RunWrapper(Runnable inner) {
 			this.inner = inner;
 		}
 
 		@Override
 		public void run() {
 			try {
 				inner.run();
 			} catch (Error e) {
 				failed = e;
				mainThread.interrupt();
 			}
 		}
 
 	}
 
 	/**
 	 * The number of positions to go through between each update/reset
 	 */
 	public static final int STEP_SIZE = 10000000;
 	protected final int nThreads;
 
 	protected Database db;
 	protected Configuration conf;
 	protected volatile Error failed;
	private Thread mainThread;
 
 	/**
 	 * Set the Database to use for this solver
 	 * 
 	 * @param conf
 	 *            The configuration object
 	 */
 	public Solver(Configuration conf, Database db) {
 		this.db = db;
 		this.conf = conf;
 		nThreads = conf.getInteger("gamesman.threads", 1);
 	}
 
 	private final Runnable getNextJob() {
 		if (failed == null) {
 			Runnable nextJob = nextAvailableJob();
 			if (nextJob == null)
 				return null;
 			else
 				return new RunWrapper(nextJob);
 		} else
 			return null;
 	}
 
 	public abstract Runnable nextAvailableJob();
 
 	public final void solve() {
 		System.out.println("Beginning solve for " + conf.getGame().describe()
 				+ " using " + getClass().getSimpleName());
		mainThread = Thread.currentThread();
 		long startTime = System.currentTimeMillis();
 		ExecutorService solverService = Executors.newFixedThreadPool(nThreads);
 		Runnable nextJob = null;
 		while (true) {
 			nextJob = getNextJob();
 			if (failed != null || nextJob == null)
 				break;
 			else
 				solverService.execute(nextJob);
 		}
 		solverService.shutdown();
 		if (failed != null) {
 			System.out.println("Solve failed");
 			throw failed;
 		}
 		while (!solverService.isTerminated()) {
 			try {
 				solverService.awaitTermination(20, TimeUnit.SECONDS);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 		System.out.println("Solve completed in "
 				+ Util.millisToETA(System.currentTimeMillis() - startTime));
 	}
 }
