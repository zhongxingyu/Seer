 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 /*
  * Fragment.java
  *
  * Copyright (c) 2009 FooBrew, Inc.
  */
 package org.j2free.jsp.tags.cache;
 
 import java.util.concurrent.locks.ReentrantLock;
 
 /**
  *
  * @author ryan
  */
 public class Fragment {
 
    private static final int MAX_CONTENT_WAIT = 30;
    private static final int MAX_LOCK_HOLD    = 60;
 
     private String content;
     private String condition;
     
     private long   timeout;
     private long   lockWait;
 
     private long   locked;
     private long   updated;
 
     private final ReentrantLock updateLock = new ReentrantLock();
 
     /**
      *
      * @param condition An optional condition upon creation of the Fragment;
      *        if the condition supplied to tryAcquireLock does not match this
      *        condition, then the cache considers itself in need of update.
      *
      * @param timeout The timeout for this cached Fragment
      */
     public Fragment(String condition, long timeout) {
         this(null,condition,timeout);
     }
 
     /**
      *
      * @param content An start value for the content of this Fragment
      * @param condition An optional condition upon creation of the Fragment;
      *        if the condition supplied to tryAcquireLock does not match this
      *        condition, then the cache considers itself in need of update.
      *
      * @param timeout The timeout for this cached Fragment
      */
     public Fragment(String content, String condition, long timeout) {
 
         this.content   = content;
         this.condition = (condition != null && condition.equals("")) ? null : condition;
         this.timeout   = timeout;
         this.lockWait  = MAX_LOCK_HOLD;
 
         this.updated   = System.currentTimeMillis();
 
         updateLock.lock();
         this.locked    = System.currentTimeMillis();
     }
 
     /**
      * If content is null, this method will block until content is available.
      * Otherwise, it will return content immediately, even if currently locked
      * for update.
      *
      * @return the content
      */
     public synchronized String getWhenAvailable() throws InterruptedException {
         while (content == null)
             wait();
 
         return content;
     }
 
     public synchronized String get() {
         return content;
     }
 
     /**
      * @return true if the content has expired, otherwise false
      */
     public synchronized boolean isExpired() {
         return (System.currentTimeMillis() - updated) > timeout;
     }
 
     /**
      * @return true if the Fragment is locked and the lockWait has passed, otherwise false
      */
     public synchronized boolean isLockExpired() {
         return updateLock.isLocked() && (System.currentTimeMillis() - locked) > lockWait;
     }
 
     /**
      * Atomic bundling of a few tasks:
      *  (a) Check if this fragment is expired
      *  (b) Check if the condition under which this fragment was created has expired
      *  (c) Check that no other Thread currently has a lock-for-update on this Fragment
      *  - If (a || b) && c then lock this Fragment for update by the calling Thread
      *    and return true; otherwise return false.
      *
      * @param The current condition
      * @return true if this fragment has been locked for update by the
      *         calling thread, otherwise false
      */
     public synchronized boolean tryAcquire(final String condition) {
 
         // If the caller Thread is already the owner, just return true
         if (updateLock.isHeldByCurrentThread())
             return true;
 
         // If this Fragment is currently locked for update by a different Thread
         // and the lock has not expired, return false.
         if (updateLock.isLocked())
             return false;
 
         // Check the conditions for update
         boolean condChanged = this.condition != null && !condition.equals(this.condition);
 
         // If either of the conditions have changed, lock the lock, otherwise just return false
         boolean success = (isExpired() || condChanged) ? updateLock.tryLock() : false;
 
         if (success)
             locked = System.currentTimeMillis();
 
         return success;
     }
 
     /**
      *  Checks that the caller Thread owns the update lock; if so
      *  updates the content and notifies other threads that may be
      *  waiting to get the content, otherwise returns false.
      *
      *  @param content the content to set
      *  @param condition the current condition
      */
     public synchronized boolean tryUpdateAndRelease(String content, String condition) {
 
         // Make sure the caller owns the lock
         if (!updateLock.isHeldByCurrentThread())
             return false;
 
         this.content   = content;
         this.condition = (condition != null && condition.equals("")) ? null : condition;
         this.updated   = System.currentTimeMillis();
 
         // Notify all, because Threads may be waiting on getWhenAvailable()
         notifyAll();
 
         // Unlock the lock for update
         updateLock.unlock();
 
         return true;
     }
 
     /**
      *  Attemtps to release the lock if the current thread holds it,
      *  otherwise does nothing.
      */
     public synchronized void tryRelease() {
 
         if (!updateLock.isHeldByCurrentThread())
             return;
 
         updateLock.unlock();
     }
 }
