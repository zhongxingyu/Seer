 
 package distributedTransactions;
 
 import junit.framework.TestCase;
 import distributedTransactions.LockManager;
 
 
 public class LockManagerTest extends TestCase {
     class Callback implements LockCallback {
         private boolean blocked = true;
         public void resourceIsAvailable() {
             blocked = false;
         }
         public boolean isBlocked() {
             return blocked;
         }
     }
     private Callback callback;
 
     private LockManager topic_create() {
        callback = new Callback();
         Resource resource = new Resource();
         LockManager lockM = new LockManager(resource);
         return lockM;
     }
     private LockManager topic_hasLock() {
         LockManager lockM = topic_create();
         lockM.lock(callback);
         return lockM;
     }
     private LockManager topic_releasedLock() {
         LockManager lockM = topic_create();
         lockM.lock(callback);
         lockM.release();
         return lockM;
     }
     private LockManager topic_callbackTriggered() {
         LockManager lockM = topic_create();
         lockM.lock(callback);
         lockM.lock(callback);
         lockM.release();
         return lockM;
     }
 
     public final void test_createLockM() {
         assertNotNull(topic_create());
     }
 
     public final void test_acquireLock() {
         assertTrue(topic_create().lock(callback));
     }
 
     public final void test_releaseNonExistingLock() {
        assertFalse(topic_create().release());
     }
 
     public final void test_releaseExistingLock() {
         assertTrue(topic_hasLock().release());
     }
 
     public final void test_waitInQueue() {
         assertFalse(topic_hasLock().lock(callback));
     }
 
     public final void test_releaseAlreadyReleasedLock() {
         assertFalse(topic_releasedLock().release());
     }
 
     public final void test_acquireAndReleaseLock() {
         assertFalse(topic_releasedLock().release());
     }
 
     public final void test_wakedByCallback() {
         topic_callbackTriggered();
         assertFalse(callback.isBlocked());
     }
 }
 
