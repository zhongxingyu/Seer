 package TransactionManager;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import LockManager.LockManager;
import ResImpl.Trace;
 import ResInterface.ResourceManager;
 
 /**
  * This class used to create a transaction that is passed from the client to the RM (or Middleware)
  * @author nic
  *
  */
 public class Transaction {
 	
 	private final int TRANSACTION_ID;
 	//array list of operations this transaction is responsible for
 	private ArrayList<Operation> operations;
 	private LockManager lm;
 	
 	public Transaction(int id, LockManager l)
 	{
 		TRANSACTION_ID = id;
 		operations = new ArrayList<Operation>();
 		lm = l;
 	}
 	
 	/**
 	 * Returns an arraylist of key strings which is used to obtain the locks necessary to carry out this transaction.
 	 * @return ArrayList<String> of Keys for the objects that need to be modified for this transaction to occur.
 	 */
 	public ArrayList<String> getKeys()
 	{
 		return null;
 	}
 	
 	/**
 	 * Returns ID of this transaction
 	 * @return int ID for this transaction
 	 */
 	public int getID()
 	{
 		return TRANSACTION_ID;
 	}
 	
 	/**
 	 * Adds an operation to this transaction
 	 * @param o
 	 */
 	public boolean addOperation(ResourceManager r, OP_CODE op, HashMap<String, Object> args, ArrayList<String> keys)
 	{
 		//create operation and add to operation queue
		Operation o = createOperation(TRANSACTION_ID, r, op, args, keys);
 		operations.add(o);
 		
 		//attempt to acquire necessary locks and execute transaction. This returns true 
 		//if the operation was able to successfully obtain locks execute (locally!)
 		return o.execute();
 	}
 	
 	public int addOperationIntReturn(ResourceManager r, OP_CODE op, HashMap<String, Object> args, ArrayList<String> keys)
 	{
 		//create operation and add to operation queue
		Operation o = createOperation(TRANSACTION_ID, r, op, args, keys);
 		operations.add(o);
 		
 		//attempt to acquire necessary locks and execute transaction. This returns true 
 		//if the operation was able to successfully obtain locks execute (locally!)
 		return o.executeIntReturn();
 	}
 	
 	//this method creates an operation - replaces an operation if the key and the 
 	//OP_CODE are the same
 	public Operation createOperation(int id, ResourceManager r, OP_CODE op, HashMap<String, Object> args, ArrayList<String> keys)
 	{
 		Operation return_value;
 		boolean create_new = true;
 		for (Operation o: operations)
 		{
 			boolean same_operation = true;
 			//if this is the same type of operation
 			//if (o.getOPCODE() == op)
 			//{
 				ArrayList<String> o_keys = o.getKeys();
 				//check if all the keys are the same
 				for (String k : keys)
 				{
 					if (!(o_keys.contains(k)))
 					{
 						same_operation = false;
 					}
 				}
 			//}
 			//if this is the same operation, create new operation with the same op_id
 			if(same_operation)
 			{
 				create_new = false;
 				return new Operation(TRANSACTION_ID, r, op, args, lm, o.getOpID());
 			}
 		}
 		//if this has been reached then this is a new unique operation, and we should
 		//create a new one
 		return new Operation(TRANSACTION_ID, r, op, args, lm);
 	}
 	
 	/**
 	 * Commits this transaction
 	 * @return True once the commit succeeds (we assume that once commit is called 
 	 * that it will succeed so we don't check if anything failed
 	 */
 	public boolean commit()
 	{		
 		for (Operation o : operations)
 		{
 			o.commit();
 		}
 		lm.UnlockAll(TRANSACTION_ID);
 		return true;
 	}
 	
 	/**
 	 * Aborts this transaction
 	 */
 	public void abort()
 	{
 		for (Operation o : operations)
 		{
 			o.abort();
 		}
 		lm.UnlockAll(TRANSACTION_ID);
 	}
 }
