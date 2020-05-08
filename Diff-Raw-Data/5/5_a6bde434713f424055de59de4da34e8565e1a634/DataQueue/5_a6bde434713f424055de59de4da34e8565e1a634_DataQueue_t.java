 package pleocmd.pipe.data;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import pleocmd.Log;
 import pleocmd.StandardInput;
 import pleocmd.exc.FormatException;
 import pleocmd.exc.InternalException;
import pleocmd.pipe.PipePart;
 import pleocmd.pipe.cvt.Converter;
 import pleocmd.pipe.in.Input;
 import pleocmd.pipe.out.Output;
 
 /**
  * Provides a FiFo implemented as a ring-buffer which passes {@link Data} from
  * {@link Input} / {@link Converter} thread to the {@link Output} thread in a
  * thread-safe manner with priority support.
  * 
  * @author oliver
  * @see StandardInput
  */
 public final class DataQueue {
 
 	/**
 	 * Will be used if the queue is empty and there is a {@link #get()} waiting
 	 * to indicate that the queue is currently accepting everything without any
 	 * side effects.
 	 */
 	private static final int PRIO_UNDEFINED = Byte.MAX_VALUE;
 
 	/**
 	 * Initial ringbuffer size after {@link #resetCache()}.
 	 */
 	private static final int RB_DEFAULT = 16;
 
 	/**
 	 * This array represents a ring buffer.
 	 */
 	private Data[] buffer;
 
 	/**
 	 * The position of the next byte to read from {@link #buffer}.
 	 */
 	private int readPos;
 
 	/**
 	 * The position of the next byte to write to {@link #buffer}.
 	 */
 	private int writePos;
 
 	/**
 	 * The priority of all the {@link Data}s in the queue.<br>
 	 * This is {@link #PRIO_UNDEFINED} if the queue is empty and there is a
 	 * {@link #get()} waiting or there never was any {@link #get()} (since the
 	 * last {@link #resetCache()}).<br>
 	 * So it's defined for an empty queue only if the last {@link #get()} is
 	 * still being processed by the Output-Thread.
 	 */
 	private byte priority;
 
 	/**
 	 * Only true if the cache has been closed, i.e. the remaining data in
 	 * {@link #buffer} can still be read, but no new data can be put into the
 	 * {@link #buffer} and if {@link #readPos} catches up {@link #writePos}
 	 * {@link #get()} throws an {@link IOException}.
 	 */
 	private boolean closed;
 
 	private int sizeBeforeClear;
 
 	/**
 	 * Creates a new, empty and opened {@link DataQueue}.
 	 */
 	public DataQueue() {
 		resetCache();
 	}
 
 	/**
 	 * Appends a "close" to the ring buffer.<br>
 	 * The remaining Data in the ring buffer can still be {@link #get()} but no
 	 * new data can be {@link #put(Data)} into it. After no more data is
 	 * available {@link #get()} throws an {@link IOException}.<br>
 	 * Has no effect if the {@link DataQueue} is already closed.
 	 */
 	public synchronized void close() {
 		Log.detail("Sending close to ring-buffer '%s'", this);
 		closed = true;
 		notify();
 	}
 
 	/**
 	 * Clears and (if currently closed) reopens the queue.<br>
 	 * All data in the ring buffer not yet read will be lost.
 	 */
 	public synchronized void resetCache() {
 		Log.detail("Resetting ring-buffer '%s'", this);
 		buffer = new Data[RB_DEFAULT];
 		readPos = 0;
 		writePos = 0;
 		closed = false;
 		priority = PRIO_UNDEFINED;
 		sizeBeforeClear = 0;
 		Log.detail("Reset ring-buffer '%s'", this);
 		notify();
 	}
 
 	/**
 	 * Reads one {@link Data} from the ring buffer.<br>
 	 * Blocks until the {@link Data} is available.<br>
 	 * Should only be called from the Output thread.
 	 * 
 	 * @return the next {@link Data} or <b>null</b> if no more {@link Data} is
 	 *         available and the {@link DataQueue} has been {@link #close()}d
 	 * @throws InterruptedException
 	 *             if waiting for the next data block has been interrupted
 	 */
 	public Data get() throws InterruptedException {
 		Log.detail("Trying to read in '%s'", this);
 		boolean first = true;
 		synchronized (this) {
 			while (true) {
 				// check if read catches up write?
 				if (readPos != writePos) break;
 				if (first) {
 					// queue empty and waiting in get(), so:
 					priority = PRIO_UNDEFINED;
 					Log.detail("Queue empty and waiting => "
 							+ "undefined priority in '%s'", this);
 				}
 				// if queue closed, we return null to signal end of pipe
 				if (closed) return null;
 				first = false;
 				// block until data available
 				wait();
 			}
 			final Data res = buffer[readPos];
 			Log.detail("Read from %03d '%s' in '%s'", readPos, res, this);
 			readPos = (readPos + 1) % buffer.length;
 			return res;
 		}
 	}
 
 	/**
 	 * The return value of {@link DataQueue#put(Data)}.
 	 * 
 	 * @author oliver
 	 */
 	public enum PutResult {
 		/**
 		 * The {@link Data} has been put into the queue.
 		 */
 		Put,
 		/**
 		 * The queue has been cleared because the new {@link Data} has a higher
 		 * priority as the current {@link Data}s in the queue.
 		 */
 		ClearedAndPut,
 		/**
 		 * The new {@link Data} has silently been dropped.
 		 */
 		Dropped
 	}
 
 	/**
 	 * Puts one {@link Data} into the ringbuffer, so it can be read by
 	 * {@link #get()}.<br>
 	 * If {@link Data}'s priority is lower than the one of the current elements
 	 * in the queue, the new {@link Data} will silently be dropped. <br>
 	 * If {@link Data}'s priority is higher than the one of the current elements
 	 * in the queue, the queue is cleared before inserting the new {@link Data}. <br>
 	 * Should only be called from the Input/Converter thread.
 	 * 
 	 * @param data
 	 *            data to put into the ring buffer
 	 * @return a {@link PutResult}
 	 * @throws IOException
 	 *             if the {@link DataQueue} has been {@link #close()}d.
 	 */
 	public synchronized PutResult put(final Data data) throws IOException {
 		if (closed) throw new IOException("DataQueue is closed");
 
 		boolean hasBeenCleared = false;
 		if (priority != PRIO_UNDEFINED && data.getPriority() < priority) {
 			// silently drop the new Data
 			Log.detail("Dropped '%s' in '%s'", data, this);
 			return PutResult.Dropped;
 		}
 		if (priority != PRIO_UNDEFINED && data.getPriority() > priority) {
 			// fast-clearing of the queue
 			sizeBeforeClear = (writePos - readPos) % buffer.length;
 			if (sizeBeforeClear < 0) sizeBeforeClear += buffer.length;
 			int i = readPos;
 			while (i != writePos) {
				final PipePart org = buffer[i].getOrigin();
				if (org != null) org.getFeedback().incDropCount();
 				i = (i + 1) % buffer.length;
 			}
 			readPos = writePos;
 			Log.detail("Cleared '%s' because of '%s'", this, data);
 			hasBeenCleared = true;
 		}
 
 		buffer[writePos] = data;
 		priority = data.getPriority();
 		Log.detail("Put at %03d '%s' in '%s'", writePos, data, this);
 		writePos = (writePos + 1) % buffer.length;
 		if (writePos == readPos) {
 			// we need to increase our ring buffer:
 			// we "insert space" between the current write and
 			// read position so writePos stays the same while
 			// readPos moves.
 			final Data[] newbuf = new Data[buffer.length * 2];
 			readPos += newbuf.length - buffer.length;
 			System.arraycopy(buffer, 0, newbuf, 0, writePos);
 			System.arraycopy(buffer, writePos, newbuf, readPos, buffer.length
 					- writePos);
 			buffer = newbuf;
 			Log.detail("Increased buffer in '%s'", this);
 		}
 
 		notify();
 		return hasBeenCleared ? PutResult.ClearedAndPut : PutResult.Put;
 	}
 
 	/**
 	 * @return the number of {@link Data}s which were in the queue immediately
 	 *         before the queue has been cleared due to a high-priority
 	 *         {@link Data} in {@link #put(Data)}.<br>
 	 *         Is <b>0></b> if the queue has never been cleared since the last
 	 *         {@link #resetCache()}.
 	 */
 	public int getSizeBeforeClear() {
 		return sizeBeforeClear;
 	}
 
 	@Override
 	public String toString() {
 		return String.format("cap: %d, read: %d, write: %d, lastPrio: %d",
 				buffer == null ? -1 : buffer.length, readPos, writePos,
 				priority);
 	}
 
 	public synchronized List<Data> getAll() {
 		final List<Data> res = new ArrayList<Data>();
 		int i = readPos;
 		while (i != writePos) {
 			System.err.println(buffer[i]);
 			i = (i + 1) % buffer.length;
 		}
 		if (closed) try {
 			res.add(Data.createFromAscii("QUEUE CLOSED"));
 		} catch (final IOException e) {
 			throw new InternalException(e);
 		} catch (final FormatException e) {
 			throw new InternalException(e);
 		}
 		return res;
 	}
 
 }
