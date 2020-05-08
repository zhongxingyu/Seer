 package ResImpl;
 
 import ResInterface.ResourceManager;
 import LockManager.*;
 import java.util.HashMap;
 import java.util.HashSet;
 
 public class TransactionManagerManager
 {
 	private HashMap<Integer, TransactionItem<ResourceManager>> transactionTouch;
 	private AngelOfDeath grim;
 	private LockManager lm;
 	private int nextTrxnId;
 
 	public TransactionManagerManager()
 	{
 		transactionTouch = new HashMap();
         lm = new LockManager();
 		grim = new AngelOfDeath(this);
		grim.start();
 		nextTrxnId = 0;
 	}
 
 	public int start()
 	{
 		synchronized(transactionTouch)
 		{
 			transactionTouch.put(nextTrxnId, new TransactionItem());
 		}
 		return nextTrxnId++;
 	}
 
 	public void enlist(int t, ResourceManager rm)
 	{
 		synchronized(transactionTouch)
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
 	}
 
 	public void abort(int trxnId)
 	{	// actually call the other Managers for the aborts
 		synchronized(transactionTouch)
 		{
 			for(ResourceManager rm : transactionTouch.get(trxnId))
 			{
 				try {
 					rm.abort(trxnId);
 				} catch (Exception e)
 				{
 	                System.out.println("EXCEPTION:");
 	                System.out.println(e.getMessage());
 	                e.printStackTrace();
 				}
 			}
 			transactionTouch.remove(trxnId);
 		}
 		lm.UnlockAll(trxnId);
 	}
 
 	public void commit(int trxnId)
 	{	// actually call the other Managers for the commits
 		synchronized(transactionTouch)
 		{
 			for(ResourceManager rm : transactionTouch.get(trxnId))
 			{
 				try {
 					rm.commit(trxnId);
 				} catch (Exception e)
 				{
 	                System.out.println("EXCEPTION:");
 	                System.out.println(e.getMessage());
 	                e.printStackTrace();
 				}
 			}
 			transactionTouch.remove(trxnId);
 		}
 		lm.UnlockAll(trxnId);
 	}
 
	public boolean lock(int trxnId, String strData, int lockType, ResourceManager rm)
	throws DeadlockException, TransactionAbortedException
 	{
 		if(rm == null)
 			Trace.info("   !!!!! Hey, you didn't change the RM to stop being null when sent to lock.");
 		synchronized(transactionTouch)
 		{
			if(!transactionTouch.containsKey(trxnId))
				throw new TransactionAbortedException(trxnId, "Transaction Timed Out");
 			transactionTouch.get(trxnId).updateDeathClock();
 			if(lockType == LockManager.WRITE)
 				enlist(trxnId, rm);
 		}
 		return lm.Lock(trxnId, strData, lockType);
 	}
 
 	public void cleanTrxns()
 	{
 		synchronized(transactionTouch)
 		{
 			for(Integer trxn : transactionTouch.keySet())
 				if(transactionTouch.get(trxn).getTTL() < System.currentTimeMillis())
 					abort(trxn);
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
