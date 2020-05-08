 package ch.hsr.objectCaching.rmiWithCacheClient;
 
 import java.io.IOException;
 import java.util.HashMap;
 
 import ch.hsr.objectCaching.account.Account;
 import ch.hsr.objectCaching.dto.MethodCall;
 import ch.hsr.objectCaching.dto.ObjectRequest;
 import ch.hsr.objectCaching.dto.ObjectRequestResponse;
 import ch.hsr.objectCaching.dto.ObjectUpdate;
 import ch.hsr.objectCaching.dto.RMIException;
 import ch.hsr.objectCaching.dto.ReturnValue;
 import ch.hsr.objectCaching.util.ConcurrencyControl;
 
 public class ObjectCache {
 	
 	private MessageManager messageManager;
 	private HashMap<Integer, Object> objectCache;
 	private ConcurrencyControl concurrencyControl;
 	
 	public ObjectCache()
 	{
 		objectCache = new HashMap<Integer, Object>();
 		concurrencyControl = new ConcurrencyControl();
 	}
 
 	public void setMessageManager(MessageManager messageManager) {
 		this.messageManager = messageManager;
 	}
 
 	public Object getObject(int objectID) 
 	{
 		if(objectCache.containsKey(objectID))
 		{
 			concurrencyControl.updateReadVersionOfClient(objectID);
 			return objectCache.get(objectID);
 		}
 		else
 		{
 			sendObjectRequest(objectID);
 			Object receivedObject = receiveObject(objectID);
 			concurrencyControl.updateReadVersionOfClient(objectID);
 			return receivedObject;
 		}
 		
 	}
 
 	private Object receiveObject(int objectID) {
 		Object retVal = null;
 		try {
 			ObjectRequestResponse receivedObject =  messageManager.receiveObject();
 			objectCache.put(objectID, receivedObject.getRequestedObject());
 			retVal = receivedObject.getRequestedObject();
 			concurrencyControl.updateWriteVersion(objectID, receivedObject.getObjectVersion());
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 		}
 		return retVal;
 	}
 
 	private void sendObjectRequest(int objectID) {
 		ObjectRequest temp = new ObjectRequest();
 		temp.setObjectID(objectID);
 		messageManager.sendMessageCall(temp);
 	}
 	
 	public void addObject(int objectID, Account account)
 	{
 		objectCache.put(objectID, account);
 	}
 
 	public ReturnValue processMethodWithSideEffect(MethodCall methodCall) {
 		if(!concurrencyControl.isWriteConsistent(methodCall.getObjectID())){
 			ReturnValue returnValue = new ReturnValue();
 			returnValue.setException(new RMIException());
 			return returnValue;
 		}
		methodCall.setObjectVersion(concurrencyControl.getReadMap().get(methodCall.getObjectID()));
 		messageManager.sendMessageCall(methodCall);
 		return messageManager.receiveReturnValue();
 	}
 
 	public void listenForUpdates() {
 		try {
 			ObjectUpdate objectUpdate;
 			while((objectUpdate = messageManager.receiveUpdate()) != null){
 				if(objectUpdate.getObject() == null){
 					break;
 				}
 			int objectID = objectUpdate.getObjectID();
 			concurrencyControl.updateWriteVersion(objectID);
 			objectCache.put(objectID, objectUpdate.getObject());
 			
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public void startUpdateObjectThread(){
 		new Thread(new CacheUpdateThread(this)).start();
 	}
 
 }
