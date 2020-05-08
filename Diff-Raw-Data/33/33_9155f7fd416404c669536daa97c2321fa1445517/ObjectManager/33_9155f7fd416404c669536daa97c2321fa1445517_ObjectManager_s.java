 package ch.hsr.objectCaching.rmiWithCacheServer;
 
 import java.util.HashMap;
 
 import ch.hsr.objectCaching.account.Account;
 import ch.hsr.objectCaching.dto.ObjectRequest;
 import ch.hsr.objectCaching.dto.ObjectRequestResponse;
 
 public class ObjectManager {
	
 	private HashMap<Integer, Object> objectMap = new HashMap<Integer, Object>();
 
	public ObjectRequestResponse processObjectRequest(
			ObjectRequest objectRequest) {
 		Integer objectID = objectRequest.getObjectID();
 		Object requestedObject = objectMap.get(objectID);
 		ObjectRequestResponse response = new ObjectRequestResponse();
 		response.setRequestedObject(requestedObject);
 		return response;
 	}
 
	public void addAccount(Account account) {
		// TODO Auto-generated method stub
		
 	}
	
	
 }
