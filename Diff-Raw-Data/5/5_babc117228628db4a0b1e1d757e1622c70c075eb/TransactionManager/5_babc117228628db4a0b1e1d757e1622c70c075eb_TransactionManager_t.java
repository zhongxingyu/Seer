 package ResImpl;
 
 import LockManager.*;
 import java.util.HashMap;
 import java.util.ArrayList;
 
 class TransactionManager
 {
 	private HashMap<Integer, ArrayList<Transaction>> writes;
 	
 	public TransactionManager()
 	{
 		writes = new HashMap();
 	}
 
 	public void addCreate(int id, String key)
 	{
 		writes.get(id).add(0, new Transaction(id, key, Transaction.Action.CREATE));
 	}
 
 	public void addBook(int id, String key, int custId)
 	{
        Trace.info("TM: adding a book" + key + "," + writes.get(id).size());
 		writes.get(id).add(0, new Transaction(id, key, Transaction.Action.BOOK, custId));
 	}
 
 	public void addStock(int id, String key, int amount)
 	{
 		writes.get(id).add(0, new Transaction(id, key, Transaction.Action.STOCK, amount));
 	}
 
 	public void addDelete(int id, String key, int numDeleted, int price)
 	{
 		writes.get(id).add(0, new Transaction(id, key, Transaction.Action.DELETE, numDeleted, price));
 	}
 
 	public void addUnbook(int id, String key, int custId, int price)
 	{
         if (!writes.containsKey(id))
             System.out.println("it wasnt there, id was " + id);
         if (writes.get(id) == null) 
             System.out.println("it was a null, id was " + id);
 		writes.get(id).add(0, new Transaction(id, key, Transaction.Action.UNBOOK, custId, price));
 	}
 
 	public void addUpdate(int id, String key, int amount, int price)
 	{
 		writes.get(id).add(0, new Transaction(id, key, Transaction.Action.UPDATE, amount, price));
 	}
 
 	public ArrayList<Transaction> getTrxns(int trxnId)
 	{
		if (!writes.containsKey(trxnId))
 			return new ArrayList();
 		return writes.get(trxnId);
 	}
 
 	public void start(int trxnId)
 	{
         if (!writes.containsKey(trxnId))
             writes.put(trxnId, new ArrayList());
     }
 
 	public void abort(int trxnId)
 	{
 		writes.remove(trxnId);
 	}
 
 	public void commit(int trxnId)
 	{	// DONE
 		writes.remove(trxnId);
 	}
 
 	public boolean shutdown()
 	{
 		// writes.clear();
 		return true;
 	}
 }
