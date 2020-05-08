 package ResImpl;
 
 import ResInterface.ResourceManager;
 import LockManager.*;
 import java.util.Hashtable;
 import java.util.HashSet;
 import java.util.Enumeration;
 
 public class TransactionManagerManager
 {
     private Hashtable<Integer, TransactionItem<ResourceManager>> transactionTouch;
     private AngelOfDeath grim;
     private LockManager lm;
     private int nextTrxnId;
 
     public TransactionManagerManager()
     {
         transactionTouch = new Hashtable();
         lm = new LockManager();
         grim = new AngelOfDeath(this);
         grim.start();
         nextTrxnId = 0;
     }
 
     public synchronized int start()
     {
         transactionTouch.put(nextTrxnId, new TransactionItem());
         return nextTrxnId++;
     }
 
     public synchronized void enlist(int t, ResourceManager rm)
     {
         if(!transactionTouch.containsKey(t))
             transactionTouch.put(t, new TransactionItem());
         transactionTouch.get(t).add(rm);
         try {
             rm.enlist(t);
         }
         catch (Exception e) {
             System.out.println("BNOOOOOOO");
         }
     }
 
     public synchronized void abort(int trxnId)
     {	// actually call the other Managers for the aborts
        for (ResourceManager rm : transactionTouch.get(trxnId)) {
             try {
                 rm.abort(trxnId);
             } catch (Exception x)
             {
                 System.out.println("EXCEPTION:");
                 System.out.println(x.getMessage());
                 x.printStackTrace();
             }
         }
         transactionTouch.remove(trxnId);
         lm.UnlockAll(trxnId);
     }
 
     public synchronized void commit(int trxnId)
     {	// actually call the other Managers for the commits
        for (ResourceManager rm : transactionTouch.get(trxnId)) {
             try {
                 rm.commit(trxnId);
             } catch (Exception x)
             {
                 System.out.println("EXCEPTION:");
                 System.out.println(x.getMessage());
                 x.printStackTrace();
             }
         }
         transactionTouch.remove(trxnId);
         lm.UnlockAll(trxnId);
     }
 
     public synchronized boolean lock(int trxnId, String strData, int lockType, ResourceManager rm)
         throws DeadlockException, TransactionAbortedException
         {
             if(rm == null)
                 Trace.info("   !!!!! Hey, you didn't change the RM to stop being null when sent to lock.");
             if(!transactionTouch.containsKey(trxnId))
                 throw new TransactionAbortedException(trxnId, "Transaction Timed Out");
             transactionTouch.get(trxnId).updateDeathClock();
             if(lockType == LockManager.WRITE)
                 enlist(trxnId, rm);
             return lm.Lock(trxnId, strData, lockType);
         }
 
     public synchronized void cleanTrxns()
     {
         Integer trxn;
         for (Enumeration e = transactionTouch.keys(); e.hasMoreElements();) {
             trxn = (Integer)e.nextElement();
             if(transactionTouch.get(trxn).getTTL() < System.currentTimeMillis()) {
                 abort(trxn);
                 System.out.println("transaction " + trxn + " was culled ");
             }
         }
     }
 }
 
 class TransactionItem<V> extends HashSet<V>
 {
     public static final long TTL = 10 * 1000;
     private long ttl;
     public TransactionItem()
     {
         super();
         updateDeathClock();
     }
 
     public long getTTL()
     {return ttl;}
 
     public void updateDeathClock()
     {ttl = TTL + System.currentTimeMillis();}
 }
 
 class AngelOfDeath extends Thread
 {
     private TransactionManagerManager tmm;
     public AngelOfDeath(TransactionManagerManager tmm)
     {this.tmm = tmm;}
 
     public void run()
     {
         while(true)
         {
             tmm.cleanTrxns();
             try {
                 Thread.sleep(1000);
             } catch(InterruptedException e) {
                 break;
             }
         }
     }
 }
