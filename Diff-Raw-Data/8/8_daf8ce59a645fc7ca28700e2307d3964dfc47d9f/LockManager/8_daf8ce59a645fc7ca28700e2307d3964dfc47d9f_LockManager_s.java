 /**
  * Class LockManager
  * 
  * @author Yaxing Chen (16929794)
  */
 package nyu.ads.conctrl.site;
 
 import java.util.*;
 import nyu.ads.conctrl.entity.*;
 
 public class LockManager {
 	
	private HashMap<Integer, Integer> locks;	// current locks: "resource"=>"transaction id"
 	
 	public LockManager() {
		locks = new HashMap<Integer, Integer>(); 
 	}
 	
	public ConflictRespEnty lock(int transacId, int res) {
 		if(locks.containsKey(res)) {
 			return new ConflictRespEnty(locks.get(res), transacId, res);
 		}
 		else{
 			locks.put(res, transacId);
 			return null;
 		}
 	}
 	
 	public void unlock(String res) {
 		locks.remove(res);
 	} 
 	
 	public void clearTransacLocks(int transacId) {
 		/*
 		 * remove all locks of certain transaction 
 		 */
 	}
 	
 	public void clearLocks() {
 		locks.clear();
 	}
 }
