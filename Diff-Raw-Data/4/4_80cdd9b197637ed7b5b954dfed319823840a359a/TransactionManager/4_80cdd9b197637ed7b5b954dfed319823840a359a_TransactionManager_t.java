 package TransactionManager;
 
 import java.io.Serializable;
 import java.rmi.RemoteException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.ScheduledFuture;
 import java.util.concurrent.TimeUnit;
 
 import javax.transaction.InvalidTransactionException;
 
 import LockManager.LockManager;
 import ResInterface.ResourceManager;
 
 /**
  * This class is responsible for organizing all transactions in the system. It handles starting, committing, and aborting
  * transactions.
  * @author nic
  *
  */
 public class TransactionManager implements Serializable {
 
	/**
	 * 
	 */
	private static final long serialVersionUID = 2654814320951132999L;
 	//this table manages all transaction objects, mapped by id
 	private Hashtable<String, Transaction> transaction_table;
 	//TODO Write transaction_table to disk
 	private static int transaction_id_counter = 0;
 	private static LockManager lm;
 	private final ScheduledExecutorService scheduler;
 	private Hashtable<String, ScheduledFuture<Boolean>> scheduledFutures;
 	private long secondsToLive;
 
 	public TransactionManager()
 	{
 		transaction_table = new Hashtable<String, Transaction>();
 		lm = new LockManager();
 		
 		//set up scheduler object
 		//TODO need to shut this down on exit
 		scheduler = Executors.newScheduledThreadPool(1000);
 		scheduledFutures = new Hashtable<String, ScheduledFuture<Boolean>>();
 		secondsToLive = 300;
 	}
 	
 	/**
 	 * Synchronized method that starts a new transaction
 	 * @return Transaction ID
 	 */
 	public synchronized int start()
 	{
 		Transaction t = new Transaction(transaction_id_counter, lm);
 		int to_return = t.getID();
 		transaction_id_counter++;
 		transaction_table.put("" + to_return, t);
 		
 		//start TIL timer for this transaction
 		TransactionTimer tt = new TransactionTimer(to_return, this);
 		ScheduledFuture<Boolean> scheduledFuture = scheduler.schedule(tt, secondsToLive, TimeUnit.SECONDS);
 		scheduledFutures.put("" + to_return, scheduledFuture);
 		return to_return;
 	}
 	
 	public synchronized boolean commit(int transaction_id) throws InvalidTransactionException, TransactionAbortedException
 	{
 		Transaction t = transaction_table.get("" + transaction_id);
 		if (t == null)
 		{
 			throw new TransactionAbortedException();
 		}
 		boolean return_value = t.commit();
 		transaction_table.remove("" + transaction_id);
 		scheduledFutures.get("" + transaction_id).cancel(false);
 		scheduledFutures.remove("" + transaction_id);
 		return return_value;
 	}
 	
 	public void abort(int transaction_id) throws TransactionAbortedException
 	{
 		Transaction t = transaction_table.get("" + transaction_id);
 		if (t == null)
 		{
 			throw new TransactionAbortedException();
 		}
 		t.abort();
 		transaction_table.remove("" + transaction_id);
 		scheduledFutures.get("" + transaction_id).cancel(false);
 		scheduledFutures.remove("" + transaction_id);
 	}
 	
 	/**
 	 * Method to initiate voting phase of two phase commit
 	 * @param transactionID
 	 * @return
 	 */
 	public boolean prepare(int transactionID) throws RemoteException, TransactionAbortedException, InvalidTransactionException
 	{
 		boolean allYes = transaction_table.get("" + transactionID).startVotingProcess();
 		
 		if(allYes)
 		{
 			return this.commit(transactionID);
 		}
 		else
 		{
 			this.abort(transactionID);
 			return false;
 		}
 	}
 	
 	/**
 	 * Method used to add an operation to a transaction
 	 * @param transaction_id
 	 * @return
 	 */
 	public boolean addOperation(int transaction_id, ResourceManager r, OP_CODE op, HashMap<String, Object> args, ArrayList<String> keys)
 	{
 		return transaction_table.get("" + transaction_id).addOperation(r, op, args, keys);
 	}
 	
 	public int addOperationIntReturn(int transaction_id, ResourceManager r, OP_CODE op, HashMap<String, Object> args, ArrayList<String> keys)
 	{
 		return transaction_table.get("" + transaction_id).addOperationIntReturn(r, op, args, keys);
 	}
 
 	public String addOperationStringReturn(int transaction_id, ResourceManager r, OP_CODE op, HashMap<String, Object> args, ArrayList<String> keys)
 	{
 		return transaction_table.get("" + transaction_id).addOperationStringReturn(r, op, args, keys);
 	}
 }
