 package javaUtilities;
 
 import java.io.*;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.locks.*;
 
 /**
  * A reentrant read-write lock allowing at most one designated upgradable thread
  * that can switch between reading and writing. Other readers can acquire the
  * lock while the thread with the upgradable lock is downgraded. The upgradable
  * thread blocks while upgrading if other threads hold read locks.
  * <p>
  * A thread can initially acquire the lock in any of three modes: read,
  * upgradable, and write. A thread acquiring an upgradable lock starts in the
  * downgraded state. All locks and unlocks are nested. This means that a thread
  * cannot acquire a write lock, then a read lock, and then release the write
  * lock without releasing the read lock. Calls to downgrade must be matched by
  * calls to upgrade. Calls to upgrade and downgrade can be interleaved with
  * calls to lock and unlock in any order, as long as the thread has an
  * upgradable or write lock when upgrading or downgrading. A thread with only a
  * read lock cannot acquire an upgradable or write lock. Any thread with an
  * upgradable or write lock can acquire the lock again in any of the three
  * modes. Acquiring a read lock after an upgradable or write lock has no effect,
  * though it still must be released.
  * <p>
  * This class allows {@linkplain Condition condition} waits for threads that
  * hold write locks or have upgraded.
  * <p>
  * This lock allows a running thread to acquire the lock without waiting in the
  * queue, unless the thread is acquiring a read lock, and it detects a thread
  * waiting to upgrade or acquire a write lock at the front of the queue.
  * <p>
  * This class is {@linkplain Serializable serializable}. It is always
  * deserialized in the fully unlocked state.
  */
 public final class UpgradableLock implements Serializable {
   /*
    * This class stores each thread's lock holds in a thread local variable. It
    * uses a subclass of AbstractQueuedSynchronizer (mySync) to manage the queue
    * and store the number of threads with each type of lock hold. For every call
    * to a public method, this class first uses the thread local state to
    * determine whether the state of mySync must change. Then it delegates to
    * mySync and updates the thread local state on success.
    */
   
   private static final long serialVersionUID = 0L;
   
   private static final long MIN_TIMEOUT = -1L;
   private static final long NO_WAIT = -2L;
   private static final long NO_TIMEOUT = -3L;
   
   private final Sync mySync = new Sync();
   private final ThreadLocal<ThreadState> myThreadState = new ThreadLocal<ThreadState>() {
     @Override
     protected ThreadState initialValue() {
       return ThreadState.newState();
     }
   };
   
   /**
    * The modes used to acquire the lock.
    */
   public static enum Mode {
     READ,
     UPGRADABLE,
     WRITE
   }
 
   /**
    * Thrown when a thread attempts to acquire or upgrade the lock when the lock
    * already has the maximum number of holds.
    */
   public static final class TooManyHoldsException extends RuntimeException {
     private static final long serialVersionUID = 0L;
 
     TooManyHoldsException(String aMessage) {
       super(aMessage);
     }
   }
   
   /**
    * Lock state that applies to the current thread. It stores the numbers
    * and types of holds that the thread currently has.
    */
   private static final class ThreadState {
     private static final int NO_WRITE_LOCK = -1;
     private static final ThreadState NEW = new ThreadState(FirstHold.NONE, 0, 0, NO_WRITE_LOCK);
     
     private final FirstHold myFirstHold;
     private final int myUpgradeCount;
     private final int myHoldCount;
     private final int myFirstWriteLock;
     
     private static enum FirstHold {
       NONE,
       READ,
       UPGRADABLE_OR_WRITE;
     }
 
     static ThreadState newState() {
       // reuse instance for efficiency
       return NEW;
     }
     
     private ThreadState(FirstHold aFirstHold, int aUpgrades, int aHolds, int aFirstWrite) {
       myFirstHold = aFirstHold;
       myUpgradeCount = aUpgrades;
       myHoldCount = aHolds;
       myFirstWriteLock = aFirstWrite;
     }
 
     boolean acquiredReadFirst() {
       return myFirstHold == FirstHold.READ;
     }
     
     boolean isUnlocked() {
       return myHoldCount == 0;
     }
     
     /**
      * Returns {@code true} if the thread holds only a read lock or a downgraded
      * upgradable lock.
      */
    boolean canWrite() {
      return myFirstHold == FirstHold.UPGRADABLE_OR_WRITE &&
          (myUpgradeCount > 0 || myFirstWriteLock != NO_WRITE_LOCK);
    }

     boolean isDowngraded() {
      return myUpgradeCount == 0;
     }
 
     ThreadState incrementWrite() {
       int mNewHolds = incrementHolds();
       int mFirstWrite = (myFirstWriteLock == NO_WRITE_LOCK) ? mNewHolds : myFirstWriteLock;
       return new ThreadState(FirstHold.UPGRADABLE_OR_WRITE, myUpgradeCount, mNewHolds, mFirstWrite);
     }
 
     ThreadState incrementUpgradable() {
       int mNewHolds = incrementHolds();
       return new ThreadState(FirstHold.UPGRADABLE_OR_WRITE, myUpgradeCount, mNewHolds, myFirstWriteLock);
     }
 
     ThreadState incrementRead() {
       FirstHold mFirst = myFirstHold == FirstHold.NONE ? FirstHold.READ : myFirstHold;
       int mNewHolds = incrementHolds();
       return new ThreadState(mFirst, myUpgradeCount, mNewHolds, myFirstWriteLock);
     }
 
     ThreadState decrementHolds() {
       int mFirstWrite = (myFirstWriteLock == myHoldCount) ? NO_WRITE_LOCK : myFirstWriteLock;
       int mNewHolds = myHoldCount - 1;
       return new ThreadState(myFirstHold, myUpgradeCount, mNewHolds, mFirstWrite);
     }
 
     ThreadState upgrade() {
       if (myUpgradeCount == Integer.MAX_VALUE) {
         throw new TooManyHoldsException("Too many upgrades");
       }
       return new ThreadState(myFirstHold, myUpgradeCount + 1, myHoldCount, myFirstWriteLock);
     }
 
     ThreadState downgrade() {
       return new ThreadState(myFirstHold, myUpgradeCount - 1, myHoldCount, myFirstWriteLock);
     }
     
     private int incrementHolds() {
       if (myHoldCount == Integer.MAX_VALUE) {
         throw new TooManyHoldsException("Too many holds");
       }
       return myHoldCount + 1;
     }
   }
 
   private static final class Sync extends AbstractQueuedSynchronizer {
     /*
      * This class uses its int state to maintain a count of threads with
      * read/downgraded locks and a count of threads with write/upgraded
      * locks. It does not keep track of the number of holds per thread or which
      * holds allow upgrading or downgrading. The lowest bit stores the number of
      * writer threads, and the rest of the state stores the number of reader
      * threads.
      */
     
     private static final int MAX_READ_HOLDS = Integer.MAX_VALUE >>> 1;
     
     /* Arguments passed to methods of AbstractQueuedSynchronizer
      * 
      * The values for acquiring and releasing a write lock must equal the lock
      * state before releasing a write lock for conditions to work.
      */
     private static final int LOCK_WRITE = calcState(1, 0);
     private static final int UNLOCK_WRITE = calcState(1, 0);
     private static final int UPGRADE = 0;
     private static final int DOWNGRADE = 0;
     private static final int UNUSED = 0;
     
     boolean acquire(boolean aIsUpgrade, boolean aInterruptible, long aTime, TimeUnit aUnit) throws InterruptedException {
       int mArg = aIsUpgrade ? UPGRADE : LOCK_WRITE;
       if (aTime == NO_WAIT) {
         return tryAcquire(mArg);
       } else if (aTime == NO_TIMEOUT) {
         if (aInterruptible) {
           acquireInterruptibly(mArg); 
         } else acquire(mArg);
         return true;
       } else return tryAcquireNanos(mArg, aUnit.toNanos(aTime));
     }
     
     boolean acquireShared(boolean aInterruptible, long aTime, TimeUnit aUnit) throws InterruptedException {
       if (aTime == NO_WAIT) {
         return tryAcquireShared(UNUSED) >= 0;
       } else if (aTime == NO_TIMEOUT) {
         if (aInterruptible) {
           acquireSharedInterruptibly(UNUSED); 
         } else acquireShared(UNUSED);
         return true;
       } else return tryAcquireSharedNanos(UNUSED, aUnit.toNanos(aTime));
     }
     
     void releaseRead() {
       releaseShared(UNUSED);
     }
     
     void releaseWrite() {
       release(UNLOCK_WRITE);
     }
     
     void downgrade() {
       release(DOWNGRADE);
     }
 
     @Override
     protected boolean tryAcquire(int aArg) {
       boolean mIsUpgrade = aArg == UPGRADE;
       int mState = getState();
       if (hasWriteHold(mState)) return false;
       int mReadHolds = getReadHolds(mState);
       int mNewReadHolds = mIsUpgrade ? mReadHolds  - 1 : mReadHolds;
       if (mNewReadHolds == 0) {
         int mNewState = calcState(1, 0);
         if (compareAndSetState(mState, mNewState)) {
           setExclusiveOwnerThread(Thread.currentThread());
           return true;
         }
       }
       return false;
     }
 
     @Override
     protected boolean tryRelease(int aArg) {
       if (!isHeldExclusively()) return false;
       setExclusiveOwnerThread(null);
       boolean mIsDowngrade = aArg == DOWNGRADE;
       int mNewReadHolds = mIsDowngrade ? 1 : 0;
       int mNewState = calcState(0, mNewReadHolds);
       setState(mNewState);
       return true;
     }
     
     @Override
     protected int tryAcquireShared(int aUnused) {
       // prevent writer threads from starving
       if (isFirstQueuedThreadExclusive()) return -1;
       int mState = getState();
       if (hasWriteHold(mState)) return -1;
       int mReadHolds = getReadHolds(mState);
       if (mReadHolds == MAX_READ_HOLDS) throw new TooManyHoldsException("Too many holds");
       int mNewState = calcState(0, mReadHolds + 1);
       return compareAndSetState(mState, mNewState) ? 1 : -1;
     }
 
     @Override
     protected boolean tryReleaseShared(int aUnused) {
       int mState, mNewState;
       do {
         mState = getState();
         int mReadHolds = getReadHolds(mState);
         mNewState = calcState(0, mReadHolds - 1);
       } while (!compareAndSetState(mState, mNewState));
       return true;
     }
     
     @Override
     protected boolean isHeldExclusively() {
       return Thread.currentThread().equals(getExclusiveOwnerThread());
     }
     
     private static boolean hasWriteHold(int aState) {
       return (aState & 1) == 1;
     }
     
     private static int getReadHolds(int aState) {
       return aState >>> 1;
     }
 
     private static int calcState(int aWriteHolds, int aReadHolds) {
       return (aReadHolds << 1) | aWriteHolds;
     }
     
     private boolean isFirstQueuedThreadExclusive() {
       Thread mFirst = getFirstQueuedThread();
       return getExclusiveQueuedThreads().contains(mFirst);
     }
     
     @Override
     public String toString() {
       int mState = getState();
       String mMessage;
       if (hasWriteHold(mState)) {
         mMessage = "1 write/upgraded thread";
       } else {
         int mReadHolds = getReadHolds(mState);
         if (mReadHolds == 0) mMessage = "unlocked";
         else {
           mMessage = mReadHolds + " read/downgraded thread"
               + (mReadHolds > 1 ? "s" : "");
         }
       }
       return "[" + mMessage + "]";
     }
   }
   
   /**
    * Acquires the lock in the given locking mode.
    * 
    * @throws IllegalMonitorStateException
    *         if the thread already holds a read lock and is trying for an
    *         upgradable or write lock.
    */
   public void lock(Mode aMode) {
     try {
       lockInternal(aMode, false, NO_TIMEOUT, TimeUnit.SECONDS);
     } catch (InterruptedException e) {
       throw new AssertionError();
     }
   }
   
   /**
    * Acquires the lock while allowing
    * {@linkplain Thread#interrupt interruption}.
    * 
    * @see UpgradableLock#lock(Mode)
    * @throws InterruptedException
    *         if the thread is interrupted before or while waiting for the lock.
    */
   public void lockInterruptibly(Mode aMode) throws InterruptedException {
     lockInternal(aMode, true, NO_TIMEOUT, TimeUnit.SECONDS);
   }
   
   /**
    * Acquires the lock only if it is currently
    * available and returns {@code true} if it succeeds.
    * 
    * @see UpgradableLock#lock(Mode)
    */
   public boolean tryLock(Mode aMode) {
     try {
       return lockInternal(aMode, false, NO_WAIT, TimeUnit.SECONDS);
     } catch (InterruptedException e) {
       throw new AssertionError();
     }
   }
   
   /**
    * Tries to acquire the lock within the given time limit and returns
    * {@code true} if it succeeds.
    * 
    * @see UpgradableLock#lock(Mode)
    * @throws InterruptedException
    *         if the thread is interrupted before or while waiting for the lock.
    */
   public boolean tryLock(Mode aMode, long aTime, TimeUnit aUnit) throws InterruptedException {
     if (aTime < 0) aTime = MIN_TIMEOUT;
     return lockInternal(aMode, true, aTime, aUnit);
   }
   
   /**
    * Releases the thread's latest hold on the lock.
    * 
    * @throws IllegalMonitorStateException if the thread does not hold the lock.
    */
   public void unlock() {
     ThreadState mOld = myThreadState.get();
     if (mOld.isUnlocked()) {
       throw new IllegalMonitorStateException("Cannot unlock lock that was not held");
     }
    boolean mWasWrite = mOld.canWrite();
     ThreadState mNew = mOld.decrementHolds();
     if (mNew.isUnlocked()) {
      if (mWasWrite) mySync.releaseWrite();
      else mySync.releaseRead();
       myThreadState.remove();
       return;
    } else if (mWasWrite && !mNew.canWrite()) {
       mySync.downgrade();
     }
     myThreadState.set(mNew);
   }
   
   /**
    * Upgrades the thread's hold on the lock. This has no effect if the thread
    * holds a write lock or has already upgraded.
    * 
    * @throws IllegalMonitorStateException
    *         if the thread does not already hold the lock in upgradable or write
    *         mode.
    */
   public void upgrade() {
     try {
       upgradeInternal(false, NO_TIMEOUT, TimeUnit.SECONDS);
     } catch (InterruptedException e) {
       throw new AssertionError();
     }
   }
 
   /**
    * Upgrades the thread's hold on the lock while allowing
    * {@linkplain Thread#interrupt interruption}.
    * @see UpgradableLock#upgrade()
    * @throws InterruptedException
    *         if the thread is interrupted before or while waiting to upgrade.
    */
   public void upgradeInterruptibly() throws InterruptedException {
     upgradeInternal(true, NO_TIMEOUT, TimeUnit.SECONDS);
   }
 
   /**
    * Upgrades the thread's hold on the lock only if there are no current readers
    * and returns {@code true} if it succeeds.
    * @see UpgradableLock#upgrade()
    */
   public boolean tryUpgrade() {
     try {
       return upgradeInternal(false, NO_WAIT, TimeUnit.SECONDS);
     } catch (InterruptedException e) {
       throw new AssertionError();
     }
   }
 
   /**
    * Tries to upgrade the thread's hold on the lock for the given amount of time
    * and returns {@code true} if it succeeds.
    * 
    * @see UpgradableLock#upgrade()
    * @throws InterruptedException
    *         if the thread is interrupted before or while waiting to upgrade.
    */
   public boolean tryUpgrade(long aTime, TimeUnit aUnit) throws InterruptedException {
     return upgradeInternal(true, aTime, aUnit);
   }
   
   /**
    * Downgrades the thread's hold on the lock. This allows other reader threads
    * to acquire the lock, if the thread has no unmatched upgrades and does not
    * hold a write lock.
    * 
    * @throws IllegalMonitorStateException
    *         if the thread has not upgraded.
    */
   public void downgrade() {
     ThreadState mOld = myThreadState.get();
     if (mOld.isUnlocked()) {
       throw new IllegalMonitorStateException("Cannot downgrade without lock");
     }
     if (mOld.acquiredReadFirst()) {
       throw new IllegalMonitorStateException("Cannot upgrade or downgrade from read");
     }
     if (mOld.isDowngraded()) {
       throw new IllegalMonitorStateException("Cannot downgrade without upgrade");
     }
     ThreadState mNew = mOld.downgrade();
    if (!mNew.canWrite()) {
       mySync.downgrade();
     }
     myThreadState.set(mNew);
   }
   
   /**
    * Returns a new {@link Condition} object which can be used for waiting and
    * notifying other threads. {@linkplain Condition#await() await()} and
    * {@linkplain Condition#signal() signal()} can only be called by a thread
    * holding the lock associated with the condition in write or upgraded mode.
    */
   public Condition newCondition() {
     return mySync.new ConditionObject();
   }
   
   private boolean lockInternal(Mode aMode, boolean aInterruptible, long aTime, TimeUnit aUnit) throws InterruptedException {
     switch (aMode) {
       case READ: return readLockInternal(aInterruptible, aTime, aUnit);
       case UPGRADABLE: return upgradableLockInternal(aInterruptible, aTime, aUnit);
       case WRITE: return writeLockInternal(aInterruptible, aTime, aUnit);
       default: throw new AssertionError();
     }
   }
   
   private boolean readLockInternal(boolean aInterruptible, long aTime, TimeUnit aUnit) throws InterruptedException {
     ThreadState mOld = myThreadState.get();
     ThreadState mNew = mOld.incrementRead();
     if (mOld.isUnlocked()) {
       if (!mySync.acquireShared(aInterruptible, aTime, aUnit)) return false;
     }
     myThreadState.set(mNew);
     return true;
   }
   
   private boolean writeLockInternal(boolean aInterruptible, long aTime, TimeUnit aUnit) throws InterruptedException {
     ThreadState mOld = myThreadState.get();
     if (mOld.acquiredReadFirst()) {
       throw new IllegalMonitorStateException("Cannot upgrade from read");
     }
     ThreadState mNew = mOld.incrementWrite();
     if (mOld.isUnlocked()) {
       if (!mySync.acquire(false, aInterruptible, aTime, aUnit)) return false;
    } else if (!mOld.canWrite()) {
       if (!mySync.acquire(true, aInterruptible, aTime, aUnit)) return false;
     }
     myThreadState.set(mNew);
     return true;
   }
   
   private boolean upgradableLockInternal(boolean aInterruptible, long aTime, TimeUnit aUnit) throws InterruptedException {
     ThreadState mOld = myThreadState.get();
     if (mOld.acquiredReadFirst()) {
       throw new IllegalMonitorStateException("Cannot upgrade from read");
     }
     ThreadState mNew = mOld.incrementUpgradable();
     if (mOld.isUnlocked()) {
       if (!mySync.acquireShared(aInterruptible, aTime, aUnit)) return false;
     }
     myThreadState.set(mNew);
     return true;
   }
   
   private boolean upgradeInternal(boolean aInterruptible, long aTime, TimeUnit aUnit) throws InterruptedException {
     ThreadState mOld = myThreadState.get();
     if (mOld.isUnlocked()) {
       throw new IllegalMonitorStateException("Cannot upgrade without lock");
     }
     if (mOld.acquiredReadFirst()) {
       throw new IllegalMonitorStateException("Cannot upgrade from read");
     }
     ThreadState mNew = mOld.upgrade();
    if (!mOld.canWrite()) {
       if (!mySync.acquire(true, aInterruptible, aTime, aUnit)) return false;
     }
     myThreadState.set(mNew);
     return true;
   }
   
   @Override
   public String toString() {
     return "UpgradableLock" + mySync.toString();
   }
   
   private Object writeReplace() {
     return new SerializationProxy();
   }
   
   private void readObject(ObjectInputStream aOis) throws InvalidObjectException {
     throw new InvalidObjectException("Expecting serialization proxy");
   }
   
   /*
    * A new upgradable lock must be created after deserialization to allow all
    * fields to be final while avoiding serializing the thread local state. A
    * serialization proxy makes this easier.
    */
   private static final class SerializationProxy implements Serializable {
     private static final long serialVersionUID = 0L;
     
     private Object readResolve() {
       return new UpgradableLock();
     }
   }
 }
