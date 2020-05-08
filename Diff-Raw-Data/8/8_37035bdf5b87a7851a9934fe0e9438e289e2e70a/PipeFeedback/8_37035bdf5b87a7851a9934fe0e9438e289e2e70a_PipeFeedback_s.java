 package pleocmd.pipe;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import pleocmd.pipe.cvt.Converter;
 import pleocmd.pipe.data.Data;
 import pleocmd.pipe.in.Input;
 import pleocmd.pipe.out.Output;
 
 /**
  * Fully synchronized class that provided feedback about a {@link Pipe} that is
  * currently running or has recently run.
  * 
  * @author oliver
  */
 public final class PipeFeedback {
 
 	private final long startTime;
 
 	private long stopTime;
 
 	private int dataInputCount;
 
 	private int dataConvertedCount;
 
 	private int dataOutputCount;
 
 	private final ArrayList<Throwable> temporaryErrors;
 
 	private final ArrayList<Throwable> permanentErrors;
 
 	private int interruptionCount;
 
 	private int dropCount;
 
 	private long behindCount;
 
 	private long behindMax;
 
 	private long behindSum;
 
 	/**
 	 * Constructs a new {@link PipeFeedback}.<br>
 	 * Must only be called from {@link Pipe}.
 	 */
 	PipeFeedback() {
 		String s1;
 		assert (s1 = new Throwable().getStackTrace()[1].getClassName())
 				.equals(Pipe.class.getName()) : s1;
 		startTime = System.currentTimeMillis();
 		temporaryErrors = new ArrayList<Throwable>();
 		permanentErrors = new ArrayList<Throwable>();
 	}
 
 	/**
 	 * @return time at which the {@link Pipe} has been started
 	 */
 	public synchronized long getStartTime() {
 		return startTime;
 	}
 
 	/**
 	 * @return time at which the {@link Pipe} has been stopped or <b>0</b> if
 	 *         the pipe is still running
 	 */
 	public synchronized long getStopTime() {
 		return stopTime;
 	}
 
 	/**
 	 * @return time which has elapsed since the {@link Pipe} has been started if
 	 *         it's currently running or the time it has run otherwise.
 	 */
 	public synchronized long getElapsed() {
 		return (stopTime == 0 ? System.currentTimeMillis() : stopTime)
 				- startTime;
 	}
 
 	/**
 	 * @return number of {@link Data} read from an {@link Input}
 	 */
 	public synchronized int getDataInputCount() {
 		return dataInputCount;
 	}
 
 	/**
 	 * @return number of {@link Data} passed to a {@link Converter}
 	 */
 	public synchronized int getDataConvertedCount() {
 		return dataConvertedCount;
 	}
 
 	/**
 	 * @return number of {@link Data} written to an {@link Output}
 	 */
 	public synchronized int getDataOutputCount() {
 		return dataOutputCount;
 	}
 
 	/**
 	 * @return a list of all {@link Exception}s that have been thrown and which
 	 *         only caused temporary failure (i.e. affected only one
 	 *         {@link Data})
 	 */
 	public synchronized List<Throwable> getTemporaryErrors() {
 		return Collections.unmodifiableList(temporaryErrors);
 	}
 
 	/**
 	 * @return a list of all {@link Exception}s that have been thrown and which
 	 *         only caused permanent failure (i.e. possibly affected more than
 	 *         one {@link Data})
 	 */
 	public synchronized List<Throwable> getPermanentErrors() {
 		return Collections.unmodifiableList(permanentErrors);
 	}
 
 	/**
 	 * @return number of {@link Data} that have been interrupted because a
 	 *         {@link Data} with a higher priority has to be executed
 	 */
 	public synchronized int getInterruptionCount() {
 		return interruptionCount;
 	}
 
 	/**
 	 * @return number of {@link Data} that have been dropped because a
 	 *         {@link Data} with a higher priority is currently been executed or
 	 *         has been queued for execution
 	 */
 	public synchronized int getDropCount() {
 		return dropCount;
 	}
 
 	public synchronized long getBehindCount() {
 		return behindCount;
 	}
 
 	public synchronized long getBehindMax() {
 		return behindMax;
 	}
 
 	public synchronized long getBehindSum() {
 		return behindSum;
 	}
 
 	public synchronized long getBehindAverage() {
 		return behindCount == 0 ? 0 : behindSum / behindCount;
 	}
 
 	synchronized void stopped() {
 		stopTime = System.currentTimeMillis();
 		temporaryErrors.trimToSize();
 		permanentErrors.trimToSize();
 	}
 
 	/**
 	 * Called from {@link Pipe} if a {@link Data} has been read from an
 	 * {@link Input}.
 	 */
 	synchronized void incDataInputCount() {
 		++dataInputCount;
 	}
 
 	/**
	 * Called from {@link Pipe} if a {@link Data} is about to be send to a
 	 * {@link Converter}.
 	 */
 	synchronized void incDataConvertedCount() {
 		++dataConvertedCount;
 	}
 
 	/**
	 * Called from {@link Pipe} if a {@link Data} is about to be send to an
 	 * {@link Output}.
 	 */
 	synchronized void incDataOutputCount() {
 		++dataOutputCount;
 	}
 
 	synchronized void addError(final Throwable t, final boolean permanent) {
 		if (permanent)
 			permanentErrors.add(t);
 		else
 			temporaryErrors.add(t);
 	}
 
 	synchronized void incInterruptionCount() {
 		++interruptionCount;
 	}
 
 	synchronized void incDropCount() {
 		++dropCount;
 	}
 
 	synchronized void incDropCount(final int increment) {
 		dropCount = increment;
 	}
 
 	synchronized void incBehindCount(final long behind) {
 		++behindCount;
 		if (behindMax < behind) behindMax = behind;
 		behindSum += behind;
 	}
 
 	@Override
 	public synchronized String toString() {
 		// TODO add number of read, converted and output data
 		return String.format("Pipe %s %d milliseconds, has read %d, "
 				+ "converted %d and written %d data(s), encountered %d "
 				+ "temporary and %d permanent error(s), "
 				+ "output has been %d time(s) interrupted "
 				+ "due to high-priority data and a data block "
 				+ "has %d time(s) been dropped due to low-priority and "
				+ "it was time(s) behind (average %d, max %d, sum %d).",
 				stopTime == 0 ? "is running since" : "has run", getElapsed(),
 				dataInputCount, dataConvertedCount, dataOutputCount,
 				temporaryErrors.size(), permanentErrors.size(),
 				interruptionCount, dropCount, behindCount, getBehindAverage(),
 				behindMax, behindSum);
 	}
 
 }
