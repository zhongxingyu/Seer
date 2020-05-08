 package suite.stm;
 
 import java.util.HashSet;
 import java.util.Set;
 import java.util.concurrent.atomic.AtomicInteger;
 import java.util.concurrent.atomic.AtomicReference;
 
 import suite.stm.Stm.AbortException;
 import suite.stm.Stm.DeadlockException;
 import suite.stm.Stm.Memory;
 import suite.stm.Stm.Transaction;
 import suite.stm.Stm.TransactionException;
 import suite.stm.Stm.TransactionManager;
 import suite.stm.Stm.TransactionStatus;
 
 /**
  * Implements software transactional memory by locking.
  * 
  * FIXME read after read, value might be changed.
  * 
  * FIXME read after write, confused versioning.
  * 
  * TEST T0 read A, T1 write A, T1 write B, T0 read B
  * 
  * @author ywsing
  */
 public class ObstructionFreeStm implements TransactionManager {
 
 	private static final int nullTimestamp = -1;
 
 	private AtomicInteger clock = new AtomicInteger();
 
 	private class ObstructionFreeTransaction implements Transaction {
 		private volatile TransactionStatus status = TransactionStatus.ACTIVE;
 		private volatile ObstructionFreeTransaction waitingFor;
 		private int readTimestamp = nullTimestamp;
 		private int writeTimestamp = nullTimestamp;
 		private Set<ObstructionFreeMemory<?>> readMemories = new HashSet<>();
 
 		public void commit() throws AbortException {
 			boolean isCommit = true;
 
 			// If some touched values are discovered to be modified between our
 			// read/write time, we must abort
 			for (ObstructionFreeMemory<?> memory : readMemories)
 				isCommit &= memory.owner.get() == this || readTimestamp >= memory.timestamp0 || memory.timestamp0 >= writeTimestamp;
 
 			if (isCommit)
 				setStatus(TransactionStatus.COMMITTED);
 			else
 				throw new AbortException();
 		}
 
 		public void rollback() {
 			setStatus(TransactionStatus.ABORTED);
 		}
 
 		private int readTimestamp() {
 			if (readTimestamp == nullTimestamp)
 				readTimestamp = clock.getAndIncrement();
 			return readTimestamp;
 		}
 
 		private int writeTimestamp() {
 			readTimestamp(); // Read before write
 
 			if (writeTimestamp == nullTimestamp)
 				writeTimestamp = clock.getAndIncrement();
 			return writeTimestamp;
 		}
 
 		private synchronized void setStatus(TransactionStatus status1) {
 			status = status1;
 			notifyAll();
 		}
 
 		private void waitForCompletion() throws InterruptedException {
 			if (status == TransactionStatus.ACTIVE)
 				synchronized (this) {
 					while (status == TransactionStatus.ACTIVE)
 						wait();
 				}
 		}
 	}
 
 	private class ObstructionFreeMemory<T> implements Memory<T> {
 		private AtomicReference<ObstructionFreeTransaction> owner = new AtomicReference<>();
 		private volatile int timestamp0;
 		private volatile T value0;
 		private volatile int timestamp1;
 		private volatile T value1;
 
 		/**
 		 * Read would obtain the value between two checks of the owner.
 		 * 
 		 * If the owner or its status found out be changed, read needs to be
 		 * performed again.
 		 * 
 		 * Timestamp checking is done to avoid reading too up-to-date data.
 		 */
 		public T read(Transaction transaction) throws AbortException {
 			ObstructionFreeTransaction ourTransaction = (ObstructionFreeTransaction) transaction;
 
 			while (true) {
 				ObstructionFreeTransaction theirTransaction0 = owner.get();
 				TransactionStatus theirStatus0 = theirTransaction0 != null ? theirTransaction0.status : null;
 				long timestamp;
 				T value;
 
 				if (theirTransaction0 != ourTransaction && theirStatus0 != TransactionStatus.COMMITTED) {
 					timestamp = timestamp0;
 					value = value0;
 				} else {
 					timestamp = timestamp1;
 					value = value1;
 				}
 
 				ObstructionFreeTransaction theirTransaction1 = owner.get();
 				TransactionStatus theirStatus1 = theirTransaction1 != null ? theirTransaction1.status : null;
 
 				// Retry if owner or owner status changed
 				if (theirTransaction0 == theirTransaction1 && theirStatus0 == theirStatus1) {
 					int readTimestamp = ourTransaction.readTimestamp();
 
					if (theirTransaction0 == ourTransaction || timestamp <= readTimestamp) {
						ourTransaction.readMemories.add(this);
 						return value;
					} else
 						throw new AbortException();
 				}
 			}
 		}
 
 		/**
 		 * Write would obtain the owner right.
 		 * 
 		 * If someone else is the owner, the write would block until that owner
 		 * completes. Simple waiting hierarchy is implemented in transaction
 		 * class to detect deadlocks.
 		 * 
 		 * Timestamp checking is done to avoid changing too up-to-date data.
 		 */
 		public void write(Transaction transaction, T t) throws InterruptedException, TransactionException {
 			ObstructionFreeTransaction ourTransaction = (ObstructionFreeTransaction) transaction;
 			ObstructionFreeTransaction theirTransaction;
 
 			// Makes ourself the owner
 			while ((theirTransaction = owner.get()) != ourTransaction)
 				if (theirTransaction != null) {
 
 					// Waits until previous owner complete
 					ObstructionFreeStm.this.wait(ourTransaction, theirTransaction);
 
 					if (owner.compareAndSet(theirTransaction, ourTransaction)) {
 						if (theirTransaction.status == TransactionStatus.COMMITTED) {
 							timestamp0 = timestamp1;
 							value0 = value1;
 						}
 
 						break;
 					}
 				} else if (owner.compareAndSet(theirTransaction, ourTransaction))
 					break;
 
 			int writeTimestamp = ourTransaction.writeTimestamp();
 
 			if (timestamp0 <= writeTimestamp) {
 				timestamp1 = writeTimestamp;
 				value1 = t;
 			} else
 				throw new AbortException();
 		}
 	}
 
 	private void wait(ObstructionFreeTransaction source, ObstructionFreeTransaction target) throws InterruptedException,
 			DeadlockException {
 		synchronized (ObstructionFreeStm.class) {
 			ObstructionFreeTransaction root = target;
 
 			// Detect waiting cycles and abort if deadlock is happening
 			while (root != null)
 				if (root != source)
 					root = root.waitingFor;
 				else {
 					source.setStatus(TransactionStatus.ABORTED);
 					throw new DeadlockException();
 				}
 
 			source.waitingFor = target;
 		}
 
 		try {
 			target.waitForCompletion();
 		} finally {
 			source.waitingFor = null;
 		}
 	}
 
 	@Override
 	public Transaction createTransaction() {
 		return new ObstructionFreeTransaction();
 	}
 
 	@Override
 	public <T> Memory<T> createMemory(Class<T> clazz) {
 		return new ObstructionFreeMemory<>();
 	}
 
 }
