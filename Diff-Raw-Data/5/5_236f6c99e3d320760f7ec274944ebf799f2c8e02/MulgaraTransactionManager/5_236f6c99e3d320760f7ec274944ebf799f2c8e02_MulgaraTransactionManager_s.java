 /*
  * The contents of this file are subject to the Open Software License
  * Version 3.0 (the "License"); you may not use this file except in
  * compliance with the License. You may obtain a copy of the License at
  * http://www.rosenlaw.com/OSL3.0.htm
  *
  * Software distributed under the License is distributed on an "AS IS"
  * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
  * the License for the specific language governing rights and limitations
  * under the License.
  *
  * This file is an original work developed by Netymon Pty Ltd
  * (http://www.netymon.com, mailto:mail@netymon.com). Portions created
  * by Netymon Pty Ltd are Copyright (c) 2006 Netymon Pty Ltd.
  * All Rights Reserved.
  */
 
 package org.mulgara.resolver;
 
 // Java2 packages
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.locks.Condition;
 import java.util.concurrent.locks.ReentrantLock;
 import javax.transaction.SystemException;
 import javax.transaction.Transaction;
 import javax.transaction.TransactionManager;
 import javax.transaction.xa.XAResource;
 
 // Third party packages
 import org.apache.log4j.Logger;
 
 // Local packages
 import org.mulgara.query.MulgaraTransactionException;
 import org.mulgara.server.Session;
 import org.mulgara.transaction.TransactionManagerFactory;
 
 /**
  * Manages transactions within Mulgara.
  *
  * see http://mulgara.org/confluence/display/dev/Transaction+Architecture
  *
  * Maintains association between Answer's and TransactionContext's.
  * Manages tracking the ownership of the write-lock.
  * Maintains the write-queue and any timeout algorithm desired.
  * Provides new/existing TransactionContext's to DatabaseSession on request.
  *    Note: Returns new context unless Session is currently in a User Demarcated Transaction.
  * 
  *
  * @created 2006-10-06
  *
  * @author <a href="mailto:andrae@netymon.com">Andrae Muys</a>
  *
  * @version $Revision: $
  *
  * @modified $Date: $
  *
  * @maintenanceAuthor $Author: $
  *
  * @company <A href="mailto:mail@netymon.com">Netymon Pty Ltd</A>
  *
  * @copyright &copy;2006 <a href="http://www.netymon.com/">Netymon Pty Ltd</a>
  *
  * @licence Open Software License v3.0</a>
  */
 
 public class MulgaraTransactionManager {
   /** Logger.  */
   private static final Logger logger =
     Logger.getLogger(MulgaraTransactionManager.class.getName());
 
   // Write lock is associated with a session.
   private Session currentWritingSession;
   private MulgaraTransaction userTransaction;
   private boolean autoCommit;
 
   private ReentrantLock mutex;
   private Condition writeLockCondition;
   private Condition reserveCondition;
   private Thread reservingThread;
 
   /** Set of sessions whose transactions have been rolledback.*/
   private Set<Session> failedSessions;
 
   /**
    * Map from transaction to initiating session.
    * FIXME: This is only required for checking while we wait for 1-N.
    *        Remove once 1-N is implemented.
    */
   private Map<MulgaraTransaction, Session> sessions;
 
   /**
    * Map from initiating session to set of transactions.
    * Used to clean-up transactions upon session close.
    */
   private Map<Session, Set<MulgaraTransaction>> transactions;
 
   /** Map of threads to active transactions. */
   private Map<Thread, MulgaraTransaction> activeTransactions;
 
   private final TransactionManager transactionManager;
 
   public MulgaraTransactionManager(TransactionManagerFactory transactionManagerFactory) {
     this.currentWritingSession = null;
     this.userTransaction = null;
     this.autoCommit = true;
     this.mutex = new ReentrantLock();
     this.writeLockCondition = this.mutex.newCondition();
     this.reserveCondition = this.mutex.newCondition();
     this.reservingThread = null;
 
     this.failedSessions = new HashSet<Session>();
     this.sessions = new HashMap<MulgaraTransaction, Session>();
     this.transactions = new HashMap<Session, Set<MulgaraTransaction>>();
     this.activeTransactions = new HashMap<Thread, MulgaraTransaction>();
 
     this.transactionManager = transactionManagerFactory.newTransactionManager();
   }
 
   /**
    * Allows DatabaseSession to initiate/obtain a transaction.
    * <ul>
    * <li>If the Session holds the write lock, return the current Write-Transaction.</li>
    * <li>If the Session does not hold the write lock and requests a read-only transaction,
    *     create a new ro-transaction object and return it.</li>
    * <li>If the Session does not hold the write lock and requests a read-write transaction,
    *     obtain the write-lock, create a new transaction object and return it.</li>
    * </ul>
    */
   public MulgaraTransaction getTransaction(DatabaseSession session, boolean write) throws MulgaraTransactionException {
     acquireMutex();
     try {
       if (session == currentWritingSession) {
         return userTransaction;
       } 
 
       try {
         MulgaraTransaction transaction = write ?
             obtainWriteLock(session) :
             new MulgaraTransaction(this, session.newOperationContext(false));
 
         sessions.put(transaction, session);
 
         if (!transactions.containsKey(session)) {
           transactions.put(session, new HashSet<MulgaraTransaction>());
         }
         transactions.get(session).add(transaction);
 
         return transaction;
       } catch (MulgaraTransactionException em) {
         throw em;
       } catch (Exception e) {
         throw new MulgaraTransactionException("Error creating transaction", e);
       }
     } finally {
       releaseMutex();
     }
   }
 
 
   /** 
    * Obtains the write lock.
    * Must hold readMutex on entry - but will drop readMutex if
    */
   private MulgaraTransaction obtainWriteLock(DatabaseSession session)
       throws MulgaraTransactionException {
    while (currentWritingSession != null && !writeLockReserved()) {
       try {
         writeLockCondition.await();
       } catch (InterruptedException ei) {
         throw new MulgaraTransactionException("Interrupted while waiting for write lock", ei);
       }
     }
 
     try {
       currentWritingSession = session;
       userTransaction = new MulgaraTransaction(this, session.newOperationContext(true));
       return userTransaction;
     } catch (Throwable th) {
       releaseWriteLock();
       throw new MulgaraTransactionException("Error while obtaining write-lock", th);
     }
   }
 
   private void releaseWriteLock() {
     // Calling this method multiple times is safe as the lock cannot be obtained
     // between calls as this method is private, and all calling methods are
     // synchronized.
     currentWritingSession = null;
     userTransaction = null;
     writeLockCondition.signal();
   }
 
   public void commit(DatabaseSession session) throws MulgaraTransactionException {
     acquireMutex();
     try {
       reserveWriteLock();
       if (failedSessions.contains(session)) {
         throw new MulgaraTransactionException("Attempting to commit failed exception");
       } else if (session != currentWritingSession) {
         throw new MulgaraTransactionException(
             "Attempting to commit while not the current writing transaction");
       }
 
       setAutoCommit(session, true);
       setAutoCommit(session, false);
     } finally {
       releaseMutex();
     }
   }
 
 
   /**
    * This is an explicit, user-specified rollback.
    * 
    * This needs to be distinguished from an implicit rollback triggered by failure.
    */
   public void rollback(DatabaseSession session) throws MulgaraTransactionException {
     acquireMutex();
     try {
       reserveWriteLock();
       if (session == currentWritingSession) {
         try {
           userTransaction.execute(new TransactionOperation() {
             public void execute() throws MulgaraTransactionException {
               userTransaction.explicitRollback();
             }
           });
           if (userTransaction != null) {
             // transaction referenced by something - need to explicitly end it.
             userTransaction.abortTransaction("Rollback failed",
                 new MulgaraTransactionException("Rollback failed to terminate write transaction"));
           }
         } finally {
           failedSessions.add(session);
           releaseWriteLock();
           setAutoCommit(session, false);
         }
       } else if (failedSessions.contains(session)) {
         failedSessions.remove(session);
         setAutoCommit(session, false);
       } else {
         throw new MulgaraTransactionException(
             "Attempt to rollback while not in the current writing transaction");
       }
     } finally {
       releaseMutex();
     }
   }
 
   public void setAutoCommit(DatabaseSession session, boolean autoCommit)
       throws MulgaraTransactionException {
     acquireMutex();
     try {
       if (session == currentWritingSession && failedSessions.contains(session)) {
         userTransaction.abortTransaction("Session failed and transaction not finalized",
             new MulgaraTransactionException("Failed Session in setAutoCommit"));
       }
 
       if (session == currentWritingSession || failedSessions.contains(session)) {
         if (autoCommit) {
           // AutoCommit off -> on === branch on current state of transaction.
           if (session == currentWritingSession) {
             // Within active transaction - commit and finalise.
             try {
               runWithoutMutex(new TransactionOperation() {
                 public void execute() throws MulgaraTransactionException {
                   userTransaction.execute(new TransactionOperation() {
                     public void execute() throws MulgaraTransactionException {
                       userTransaction.dereference();
                       userTransaction.commitTransaction();
                     }
                   });
                 }
               });
             } finally {
               releaseWriteLock();
               this.autoCommit = true;
             }
           } else if (failedSessions.contains(session)) {
             // Within failed transaction - cleanup.
             failedSessions.remove(session);
           }
         } else {
           logger.info("Attempt to set autocommit false twice");
           // AutoCommit off -> off === no-op. Log info.
         }
       } else {
         if (autoCommit) {
           // AutoCommit on -> on === no-op.  Log info.
           logger.info("Attempting to set autocommit true without setting it false");
         } else {
           // AutoCommit on -> off == Start new transaction.
           userTransaction = getTransaction(session, true);
           userTransaction.reference();
           this.autoCommit = false;
         }
       }
     } finally {
       releaseMutex();
     }
   }
 
   public void rollbackCurrentTransactions(Session session) throws MulgaraTransactionException {
     acquireMutex();
     try {
       try {
         if (failedSessions.contains(session)) {
           failedSessions.remove(session);
           return;
         }
 
         Throwable error = null;
 
         try {
           if (session == currentWritingSession) {
             logger.warn("Terminating session while holding writelock:" + session + ": " + userTransaction);
             userTransaction.execute(new TransactionOperation() {
                 public void execute() throws MulgaraTransactionException {
                   throw new MulgaraTransactionException("Terminating session while holding writelock");
                 }
             });
           }
         } catch (Throwable th) {
           error = th;
         }
 
         final Throwable trigger = new MulgaraTransactionException("trigger rollback");
 
         if (transactions.containsKey(session)) {
           for (MulgaraTransaction transaction : transactions.get(session)) {
             try {
               transaction.execute(new TransactionOperation() {
                 public void execute() throws MulgaraTransactionException {
                   throw new MulgaraTransactionException("Rolling back transactions due to session close");
                 }
               });
             } catch (MulgaraTransactionException em) {
               // ignore.
             } catch (Throwable th) {
               if (error == null) {
                 error = th;
               }
             }
           }
         }
 
         if (error != null) {
           if (error instanceof MulgaraTransactionException) {
             throw (MulgaraTransactionException)error;
           } else {
             throw new MulgaraTransactionException("Error in rollback on session close", error);
           }
         }
       } finally {
         if (transactions.containsKey(session)) {
           logger.error("Error in transaction rollback due to session close - aborting");
           abortCurrentTransactions(session);
         }
       }
     } finally {
       releaseMutex();
     }
   }
 
   private void abortCurrentTransactions(Session session) throws MulgaraTransactionException {
     acquireMutex();
     try {
       try {
         Throwable error = null;
         for (MulgaraTransaction transaction : transactions.get(session)) {
           try {
             transaction.abortTransaction("Transaction still valid on session close", new Throwable());
           } catch (Throwable th) {
             try {
               if (error == null) {
                 error = th;
               }
             } catch (Throwable throw_away) {}
           }
         }
 
         if (error != null) {
           if (error instanceof MulgaraTransactionException) {
             throw (MulgaraTransactionException)error;
           } else {
             throw new MulgaraTransactionException("Error in rollback on session close", error);
           }
         }
       } finally {
         if (session == currentWritingSession) {
           logger.error("Failed to abort write-transaction on session close - Server restart required");
         }
       }
     } finally {
       releaseMutex();
     }
   }
 
   //
   // Transaction livecycle callbacks.
   //
 
   public Transaction transactionStart(MulgaraTransaction transaction) throws MulgaraTransactionException {
     acquireMutex();
     try {
       try {
         logger.info("Beginning Transaction");
         if (activeTransactions.get(Thread.currentThread()) != null) {
           throw new MulgaraTransactionException(
               "Attempt to start transaction in thread with exiting active transaction.");
         } else if (activeTransactions.containsValue(transaction)) {
           throw new MulgaraTransactionException("Attempt to start transaction twice");
         }
 
         transactionManager.begin();
         Transaction jtaTrans = transactionManager.getTransaction();
 
         activeTransactions.put(Thread.currentThread(), transaction);
 
         return jtaTrans;
       } catch (Exception e) {
         throw new MulgaraTransactionException("Transaction Begin Failed", e);
       }
     } finally {
       releaseMutex();
     }
   }
 
   public void transactionResumed(MulgaraTransaction transaction, Transaction jtaXA) 
       throws MulgaraTransactionException {
     acquireMutex();
     try {
       if (activeTransactions.get(Thread.currentThread()) != null) {
         throw new MulgaraTransactionException(
             "Attempt to resume transaction in already activated thread");
       } else if (activeTransactions.containsValue(transaction)) {
         throw new MulgaraTransactionException("Attempt to resume active transaction");
       }
       
       try {
         transactionManager.resume(jtaXA);
         activeTransactions.put(Thread.currentThread(), transaction);
       } catch (Exception e) {
         throw new MulgaraTransactionException("Resume Failed", e);
       }
     } finally {
       releaseMutex();
     }
   }
 
   public Transaction transactionSuspended(MulgaraTransaction transaction)
       throws MulgaraTransactionException {
     acquireMutex();
     try {
       try {
         if (transaction != activeTransactions.get(Thread.currentThread())) {
           throw new MulgaraTransactionException(
               "Attempt to suspend transaction from outside thread");
         }
 
         if (autoCommit && transaction == userTransaction) {
           logger.error("Attempt to suspend write transaction without setting AutoCommit Off");
           throw new MulgaraTransactionException(
               "Attempt to suspend write transaction without setting AutoCommit Off");
         }
 
         Transaction xa = transactionManager.suspend();
         activeTransactions.remove(Thread.currentThread());
 
         return xa;
       } catch (Throwable th) {
         logger.error("Attempt to suspend failed", th);
         try {
           transactionManager.setRollbackOnly();
         } catch (Throwable t) {
           logger.error("Attempt to setRollbackOnly() failed", t);
         }
         throw new MulgaraTransactionException("Suspend failed", th);
       }
     } finally {
       releaseMutex();
     }
   }
 
   public void transactionComplete(MulgaraTransaction transaction) {
     acquireMutex();
     try {
       if (transaction == userTransaction) {
         releaseWriteLock();
       }
 
       activeTransactions.remove(Thread.currentThread());
       Session session = (Session)sessions.get(transaction);
       sessions.remove(transaction);
       transactions.remove(session);
     } finally {
       releaseMutex();
     }
   }
 
   public void transactionAborted(MulgaraTransaction transaction) {
     acquireMutex();
     try {
       try {
         // Make sure this cleans up the transaction metadata - this transaction is DEAD!
         if (transaction == userTransaction) {
           failedSessions.add(currentWritingSession);
         }
         transactionComplete(transaction);
       } catch (Throwable th) {
         // FIXME: This should probably abort the entire server after logging the error!
         logger.error("Error managing transaction abort", th);
       }
     } finally {
       releaseMutex();
     }
   }
 
   public void setTransactionTimeout(int transactionTimeout) {
     try {
       transactionManager.setTransactionTimeout(transactionTimeout);
     } catch (SystemException es) {
       logger.warn("Unable to set transaction timeout: " + transactionTimeout, es);
     }
   }
 
   private void acquireMutex() {
     mutex.lock();
   }
 
   private void reserveWriteLock() throws MulgaraTransactionException {
     if (!mutex.isHeldByCurrentThread()) {
       throw new IllegalStateException("Attempt to set modify without holding mutex");
     }
 
     if (Thread.currentThread().equals(reservingThread)) {
       return;
     }
 
     while (reservingThread != null) {
       try {
         reserveCondition.await();
       } catch (InterruptedException ei) {
         throw new MulgaraTransactionException("Thread interrupted while reserving write lock", ei);
       }
     }
     reservingThread = Thread.currentThread();
   }
 
   private boolean writeLockReserved() {
    return reservingThread == null || Thread.currentThread().equals(reservingThread);
   }
 
   private void releaseMutex() {
     if (!mutex.isHeldByCurrentThread()) {
       throw new IllegalStateException("Attempt to release mutex without holding mutex");
     }
 
     if (mutex.getHoldCount() == 1 && Thread.currentThread().equals(reservingThread)) {
       reservingThread = null;
       reserveCondition.signal();
     }
 
     mutex.unlock();
   }
 
   private void runWithoutMutex(TransactionOperation proc) throws MulgaraTransactionException {
     if (!mutex.isHeldByCurrentThread()) {
       throw new IllegalStateException("Attempt to run procedure without holding mutex");
     }
     int holdCount = mutex.getHoldCount();
     for (int i = 0; i < holdCount; i++) {
       mutex.unlock();
     }
 
     proc.execute();
 
     for (int i = 0; i < holdCount; i++) {
       mutex.lock();
     }
   }
 }
