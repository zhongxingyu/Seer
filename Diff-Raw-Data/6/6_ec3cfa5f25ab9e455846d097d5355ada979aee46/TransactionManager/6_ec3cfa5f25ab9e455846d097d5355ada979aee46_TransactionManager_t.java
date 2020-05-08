 package TransactionManager;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.rmi.RemoteException;
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.Set;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.ScheduledFuture;
 import java.util.concurrent.TimeUnit;
 
 import javax.transaction.InvalidTransactionException;
 
 import LockManager.LockManager;
 import ResImpl.CrashType;
 import ResImpl.Trace;
 import ResInterface.ResourceManager;
 
 /**
  * This class is responsible for organizing all transactions in the system. It handles starting, committing, and aborting
  * transactions.
  * @author nic
  *
  */
 public class TransactionManager {
 
 	//this table manages all transaction objects, mapped by id
 	private Hashtable<String, Transaction> transaction_table;
 	private static int transaction_id_counter;
 	private static LockManager lm;
 	private static ScheduledExecutorService scheduler;
 	private static Hashtable<String, ScheduledFuture<Boolean>> scheduledFutures;
 	private long secondsToLive;
 	
 	private CrashType crashType;
 	private ResourceManager serverToCrash;
 	private String crashServerName;
 
 	private static ResourceManager middleware;
 	//private static ResourceManager
 	private static ResourceManager flights;
 	private static ResourceManager cars;
 	private static ResourceManager hotels;
 
 
 	public TransactionManager(ResourceManager f, ResourceManager c,
 			ResourceManager h, ResourceManager mw)
 	{
 		transaction_table = new Hashtable<String, Transaction>();
 		lm = new LockManager();
 		
 		this.crashType = null;
 		this.serverToCrash = null;
 		this.crashServerName = null;
 		
 		//set up scheduler object
 		scheduler = Executors.newScheduledThreadPool(1000);
 		scheduledFutures = new Hashtable<String, ScheduledFuture<Boolean>>();
 		secondsToLive = 300;
 		flights = f;
 		cars = c;
 		hotels = h;
 		middleware = mw;
 		transaction_id_counter = 0;
 	}
 	
 	/**
 	 * Synchronized method that starts a new transaction
 	 * @return Transaction ID
 	 * @throws RemoteException 
 	 */
 	public synchronized int start() throws RemoteException
 	{
 		Transaction t = new Transaction(transaction_id_counter, lm);
		t.setCrashFlags(crashType, serverToCrash);
 		int to_return = t.getID();
 		transaction_id_counter++;
 		transaction_table.put("" + to_return, t);
 		Trace.info("Transaction.start(): creating new transaction with ID: " + to_return);
 		
 		//start TIL timer for this transaction
 		TransactionTimer tt = new TransactionTimer(to_return, this);
 		ScheduledFuture<Boolean> scheduledFuture = scheduler.schedule(tt, secondsToLive, TimeUnit.SECONDS);
 		scheduledFutures.put("" + to_return, scheduledFuture);
 		
 		//flush transactions to disk
 		flushToDisk();
 		
 		return to_return;
 	}
 	
 	public synchronized boolean commit(int transaction_id) throws TransactionAbortedException, RemoteException
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
 		flushToDisk();
 		return return_value;
 	}
 	
 	public void abort(int transaction_id) throws TransactionAbortedException, RemoteException
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
 		flushToDisk();
 	}
 	
 	/**
 	 * Method to initiate voting phase of two phase commit
 	 * @param transactionID
 	 * @return
 	 */
 	public boolean prepare(int transactionID) throws RemoteException, TransactionAbortedException, InvalidTransactionException
 	{
 		if(crashType == CrashType.BEFORE_VOTE_REQUEST)
 			serverToCrash.crash(crashServerName);
 		
 		Transaction toCommit = transaction_table.get("" + transactionID);
 		toCommit.setCrashFlags(crashType, serverToCrash);
 		
 		boolean allYes = toCommit.startVotingProcess();
 		
 		// crash after voting but before decision
 		if(crashType == CrashType.TM_BEFORE_DECISION)
 			serverToCrash.crash(crashServerName);
 		
 		if(allYes)
 		{
 			Trace.info("Voting process returned all YES. Committing transaction");
 
 			if(crashType == CrashType.AFTER_VOTE_RETURN_BEFORE_COMMIT_REQUEST)
 				serverToCrash.crash(crashServerName);
 			
 			if(crashType == CrashType.TM_BEFORE_DECISION_SENT)
 				serverToCrash.crash(crashServerName);
 
 			return this.commit(transactionID);
 		}
 		else
 		{
 			Trace.info("Voting process returned at least one NO. Aborting transaction");
 			this.abort(transactionID);
 			return false;
 		}
 	}
 	
 	/**
 	 * Method used to add an operation to a transaction
 	 * @param transaction_id
 	 * @return
 	 * @throws RemoteException 
 	 */
 	public boolean addOperation(int transaction_id, ResourceManager r, OP_CODE op, Hashtable<String, Object> args, ArrayList<String> keys) throws RemoteException
 	{
 		boolean to_return = transaction_table.get("" + transaction_id).addOperation(r, op, args, keys);
 		flushToDisk();
 		Trace.info("TransactionTable size: " + transaction_table.size());
 		return to_return;
 	}
 	
 	public int addOperationIntReturn(int transaction_id, ResourceManager r, OP_CODE op, Hashtable<String, Object> args, ArrayList<String> keys) throws RemoteException
 	{
 		int to_return = transaction_table.get("" + transaction_id).addOperationIntReturn(r, op, args, keys);
 		flushToDisk();
 		Trace.info("TransactionTable size: " + transaction_table.size());
 		return to_return;
 	}
 
 	public String addOperationStringReturn(int transaction_id, ResourceManager r, OP_CODE op, Hashtable<String, Object> args, ArrayList<String> keys) throws RemoteException
 	{
 		String to_return = transaction_table.get("" + transaction_id).addOperationStringReturn(r, op, args, keys);
 		flushToDisk();
 		Trace.info("TransactionTable size: " + transaction_table.size());
 		return to_return;
 	}
 	
 	public void setCrashFlags(ResourceManager toCrash, CrashType type, String serverName)
 	{
 		this.crashType = type;
 		this.serverToCrash = toCrash;
 		this.crashServerName = serverName;
 		
 		try
 		{
			if(!crashServerName.equals("middleware"))
				toCrash.setCrashFlags(serverName, type);
			
 		} catch (RemoteException e){
 			e.printStackTrace();
 		}
 	}
 	
 	   /**
      * This method is called whenever something is committed/aborted in order to flush changes to disk; 
      */
     public synchronized void flushToDisk()
     {   
     	try {
 	    	//retrieve master record file (if it doesn't exist, create it and write out string)
 	        String masterPath = "/home/2011/nwebst1/comp512/data/trxn_manager/master_record.loc";
 			String newLocation = "/home/2011/nwebst1/comp512/data/trxn_manager";
 	        
 	        File masterFile = new File(masterPath);
 	        
 	        //if master doesn't exist, create it and write default path
 	        if (!masterFile.exists())
 	        {
 	        	//create master record file
 	        	masterFile.getParentFile().getParentFile().mkdir();
 	        	masterFile.getParentFile().mkdir();
 	        	masterFile.createNewFile();
 	        	
 	        	//create default string
 	        	newLocation = "/home/2011/nwebst1/comp512/data/trxn_manager/dataA/";
 				Trace.info("NEW MASTERFILE LOCATION: " + newLocation);
 
 	        	FileOutputStream fos = new FileOutputStream(masterFile);
 	        	ObjectOutputStream oos = new ObjectOutputStream(fos);
 	        	oos.writeObject(newLocation);
 	        	fos.close();
 	        	oos.close();
 	        }
 	        //otherwise, read in string file path for master record location
 	        else
 	        {
 	        	FileInputStream fis = new FileInputStream(masterFile);
 	        	ObjectInputStream ois = new ObjectInputStream(fis);
 	        	String dataPath = (String) ois.readObject();
 	        	fis.close();
 	        	ois.close();
 	        	
 	        	//update master record				
 				String[] masterPathArray = dataPath.split("/");
 				String data_location = masterPathArray[masterPathArray.length - 1];
 				
 				if (data_location.equals("dataA"))
 				{
 					newLocation = newLocation + "/dataB/";
 				}
 				else
 				{
 					newLocation = newLocation + "/dataA/";
 				}
 				
 				Trace.info("NEW MASTERFILE LOCATION: " + newLocation);
 				
 				//write new location to master_record.loc
 				masterFile = new File(masterPath);
 				FileOutputStream fos = new FileOutputStream(masterFile);
 		    	ObjectOutputStream oos = new ObjectOutputStream(fos);
 				oos.writeObject(newLocation);
 				fos.close();
 				oos.close();
 	        }
        
 	    	//create file path for data for TM
         	String filePathTM = newLocation + "transaction_manager.data";
         	
         	//create file objects so that we can write data to disk
 	    	File tm_file = new File(filePathTM);
 	    	
 	    	//if file doesn't exist, then create it
     		if (!tm_file.exists())
     		{
     			tm_file.getParentFile().mkdir();
     			tm_file.createNewFile();
     		}
 			
     		//write TM data to disk
     		FileOutputStream fos = new FileOutputStream(tm_file);
     		ObjectOutputStream oos = new ObjectOutputStream(fos);
     		//write transaction table
     		oos.writeObject(transaction_table);
     		fos.close();
     		oos.close();
 					
 			
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 		}  	
 	}
 	
 	/**
 	 * Method called from MW when booting up in order to read transaction data from main memory
 	 * @param file File path to transaction manager data
 	 * @throws IOException 
 	 * @throws ClassNotFoundException 
 	 */
 	@SuppressWarnings("unchecked")
 	public void readFromDisk() throws ClassNotFoundException, IOException
 	{
 		/**
 		 * Read in data about transactions in progress
 		 */
 	    String masterPath = "/home/2011/nwebst1/comp512/data/trxn_manager/master_record.loc";
 	    File f = new File(masterPath);
 	    //if Master Record doesn't exist we ignore all other file reads
 	    if (f.exists())
 	    {
 	    	//get path to master record
 	    	FileInputStream fis = new FileInputStream(masterPath);
 	    	ObjectInputStream ois = new ObjectInputStream(fis);
 	    	String masterRecordPath = (String) ois.readObject();
 	    	fis.close();
 	    	ois.close();
 	    	
 	    	//get path to data for TM
 	    	String filePathTM = masterRecordPath + "transaction_manager.data";
 	    	
 	    	
 	    	//create file objects for these data files
 	      	File file = new File(filePathTM);
 	    	
 			Trace.info("Reading TransactionManager data back into main memory..."
 					+ "\n" + file);
 		
 			fis = new FileInputStream(file);
 			ois = new ObjectInputStream(fis);
 			
 			transaction_table = (Hashtable<String, Transaction>) ois.readObject();
 			Trace.info("transaction_table size: " + transaction_table.size());
 			
 			//find max key (transaction ID) and use to reset transaction_id_counter
 			//at the same time, reset timers on transactions
 			int max = 0; 
 			Set<String> keys = transaction_table.keySet();
 			for (String key : keys)
 			{			
 				int trxnID = transaction_table.get(key).getID();
 				Trace.info("transaction ID: " + trxnID);
 				if (trxnID > max)
 				{
 					max = trxnID;
 				}
 				TransactionTimer tt = new TransactionTimer(trxnID, this);
 				ScheduledFuture<Boolean> scheduledFuture = scheduler.schedule(tt, secondsToLive, TimeUnit.SECONDS);
 				scheduledFutures.put("" + trxnID, scheduledFuture);
 			}
 			transaction_id_counter = max + 1;
 			
 			//reset all locks
 			//reexecute all operations on RMs
 			keys = transaction_table.keySet();
 			int maxOPID = 0;
 			for (String key : keys)
 			{
 				Transaction t = transaction_table.get(key);
 				int thisTrxnOpMax = t.reexecute(lm, flights, cars, hotels, middleware);
 				if (thisTrxnOpMax > maxOPID)
 				{
 					maxOPID = thisTrxnOpMax;
 				}
 			}
 			
 			//reset operation counter
 			//TODO is this just printing weirdly or is it actually not working?
 			Trace.info("Setting new starting max op count to: " + (maxOPID + 1));
 			Operation.setOpCount(maxOPID + 1);
 			
 			fis.close();
 			ois.close();
 	    }
 	    
 	    /**
 		 * Read in data about any transactions that need rollbacks/aborts to be done
 		 */
 	    masterPath = "/home/2011/nwebst1/comp512/data/rollbacks/master_record.loc";
 	    f = new File(masterPath);
 	    //if Master Record doesn't exist we ignore all other file reads
 	    if (f.exists())
 	    {
 	    	//get path to master record
 	    	FileInputStream fis = new FileInputStream(masterPath);
 	    	ObjectInputStream ois = new ObjectInputStream(fis);
 	    	String masterRecordPath = (String) ois.readObject();
 	    	fis.close();
 	    	ois.close();
 	    	
 	    	//get path to data for TM
 	    	String filePathTM = masterRecordPath + "rollbacks.data";
 	    	
 	    	
 	    	//create file objects for these data files
 	      	File file = new File(filePathTM);
 	    	
 			Trace.info("Reading rollbacks data back into main memory..."
 					+ "\n" + file);
 		
 			fis = new FileInputStream(file);
 			ois = new ObjectInputStream(fis);
 			
 			int trxn_id = ois.readInt();
 			ArrayList<Operation> rollback = (ArrayList<Operation>) ois.readObject();
 			ArrayList<Operation> abort = (ArrayList<Operation>) ois.readObject();
 					
 			Trace.info("Transaction " + trxn_id + " is rolling back and aborting as necessary.");
 			for (Operation o : rollback)
 			{
 				o.rollback();
 			}
 			for (Operation o : abort)
 			{
 				o.abort();
 			}
 			transaction_table.remove("" + trxn_id);
 			
 			fis.close();
 			ois.close();
 	    }
 	}
 }
