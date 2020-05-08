 package com.askcs.webservices;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 
 public class ReconnectingAskPort implements AskPortType {
 
 	private AskPortType askPort;
 	private String authKey="";
 	private Method[] methods = AskPortType.class.getMethods();
 	
 	public ReconnectingAskPort(AskPortType askPort){
 		this.askPort=askPort;
 	}
 	
 	@Override
 	public StringResponse startSession(String authKey) {
 		this.authKey=authKey;
 		return askPort.startSession(authKey);
 	}
 	public StringResponse startSession() {
 		return askPort.startSession(this.authKey);
 	}
 
 	@Override
 	public StringResponse getErrorMessage(int error, String service) {
 		return askPort.getErrorMessage(error, service);
 	}
 	
 	private Method getMethod(String methodName) throws NoSuchMethodException{
 		for (Method method:methods){
 			if (method.getName() == methodName) return method;
 		}
 		throw new NoSuchMethodException();
 	}
 	
 	public Object wrapper(String methodName, Object... arguments){
 		int count = 0;
 		return wrapper(count,methodName,arguments);
 	}
 	
 	public Object wrapper(int count, String methodName, Object... arguments){
 		if (count > 5){
 			System.out.println("Giving up after many tries!");
 			return null;
 		}
 		Object result=null;
 		try {
 			Method method = getMethod(methodName);
 			Object res =  method.invoke(askPort, arguments);
 			Method error = res.getClass().getMethod("getError");
 			if ( (Integer)error.invoke(res) == 101){
 				this.startSession();
 				res = method.invoke(askPort, arguments);
 			}
 			return res;
 		} catch (IllegalArgumentException e) {
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			e.printStackTrace();
 		} catch (InvocationTargetException e) {
 			if (e.getCause().getCause().getClass().getName().equals("java.net.SocketTimeoutException")){
				System.out.println("Timeout, re-trying "+count);
				return wrapper(count++,methodName,arguments);
 			} else {
 				e.printStackTrace();
 			}
 		} catch (SecurityException e) {
 			e.printStackTrace();
 		} catch (NoSuchMethodException e) {
 			e.printStackTrace();
 		}
 		return result;
 	}
 
 	@Override
 	public StringResponse createNode(String sessionID, String nodeUUID,
 			TupleArray data) {
 		return (StringResponse) wrapper("createNode",sessionID,nodeUUID,data);
 	}
 
 	@Override
 	public BoolResponse removeNode(String sessionID, String nodeUUID) {
 		return (BoolResponse) wrapper("removeNode",sessionID,nodeUUID);
 	}
 
 	@Override
 	public TupleArrayResponse getNodeData(String sessionID, String nodeUUID) {
 		return (TupleArrayResponse) wrapper("getNodeData",sessionID,nodeUUID);
 	}
 
 	@Override
 	public BoolResponse setNodeData(String sessionID, String nodeUUID,
 			TupleArray data) {
 		return (BoolResponse) wrapper("setNodeData",sessionID,nodeUUID,data);
 	}
 
 	@Override
 	public StringArrayResponse getNodeContainers(String sessionID,
 			String nodeUUID) {
 		return (StringArrayResponse) wrapper("getNodeContainers",sessionID,nodeUUID);
 	}
 
 	@Override
 	public StringArrayResponse getNodeContainersByTag(String sessionID,
 			String nodeUUID, String tag) {
 		return (StringArrayResponse) wrapper("getNodeContainersByTag",sessionID,nodeUUID,tag);
 	}
 
 	@Override
 	public StringArrayResponse getNodeMembers(String sessionID, String nodeUUID) {
 		return (StringArrayResponse) wrapper("getNodeMembers",sessionID,nodeUUID);
 	}
 
 	@Override
 	public StringArrayResponse getNodeMembersByTag(String sessionID,
 			String nodeUUID, String tag) {
 		return (StringArrayResponse) wrapper("getNodeMembersByTag",sessionID,nodeUUID,tag);
 	}
 
 	@Override
 	public BoolResponse attachNode(String sessionID, String parUUID,
 			String memUUID) {
 		return (BoolResponse) wrapper("attachNode",sessionID,parUUID,memUUID);
 	}
 
 	@Override
 	public BoolResponse detachNode(String sessionID, String parUUID,
 			String memUUID) {
 		return (BoolResponse) wrapper("detachNode",sessionID,parUUID,memUUID);
 	}
 
 	@Override
 	public StringArrayResponse getServices(String sessionID, String nodeUUID) {
 		return (StringArrayResponse) wrapper("getServices",sessionID,nodeUUID);
 	}
 
 	@Override
 	public IdArrayResponse getAddresses(String sessionID, String nodeUUID) {
 		return (IdArrayResponse) wrapper("getAddresses",sessionID,nodeUUID);
 	}
 
 	@Override
 	public StringArrayResponse getResources(String sessionID, String nodeUUID) {
 		return (StringArrayResponse) wrapper("getResources",sessionID,nodeUUID);
 	}
 
 	@Override
 	public ResourceDataArrayResponse getResourcesData(String sessionID,
 			String nodeUUID) {
 		return (ResourceDataArrayResponse) wrapper("getResourcesData",sessionID,nodeUUID);
 	}
 
 	@Override
 	public StringResponse createAddress(String sessionID, String addrUUID,
 			String nodeUUID, String type, TupleArray data) {
 		return (StringResponse) wrapper("createAddress",sessionID,addrUUID,nodeUUID,type,data);
 	}
 
 	@Override
 	public BoolResponse removeAddress(String sessionID, String addrUUID) {
 		return (BoolResponse) wrapper("removeAddress",sessionID,addrUUID);
 	}
 
 	@Override
 	public TupleArrayResponse getAddressData(String sessionID, String addrUUID) {
 		return (TupleArrayResponse) wrapper("getAddressData",sessionID,addrUUID);
 	}
 
 	@Override
 	public BoolResponse setAddressData(String sessionID, String addrUUID,
 			TupleArray data) {
 		return (BoolResponse) wrapper("setAddressData",sessionID,addrUUID,data);
 	}
 
 	@Override
 	public StringArrayResponse getAlgorithms(String sessionID) {
 		return (StringArrayResponse) wrapper("getAlgorithms",sessionID);
 	}
 
 	@Override
 	public StringResponse createService(String sessionID, String srvUUID,
 			String name, String direction) {
 		return (StringResponse) wrapper("createService",sessionID,srvUUID,name,direction);
 	}
 
 	@Override
 	public BoolResponse removeService(String sessionID, String srvUUID) {
 		return (BoolResponse) wrapper("removeService",sessionID,srvUUID);
 	}
 
 	@Override
 	public ServiceDataResponse getServiceData(String sessionID, String srvUUID) {
 		return (ServiceDataResponse) wrapper("getServiceData",sessionID,srvUUID);
 	}
 
 	@Override
 	public BoolResponse setServiceData(String sessionID, String srvUUID,
 			String name, String direction) {
 		return (BoolResponse) wrapper("setServiceData",sessionID,srvUUID,name,direction);
 	}
 
 	@Override
 	public StringResponse addServiceLayerToService(String sessionID,
 			String srvUUID, String sLayerUUID, String algorithm,
 			String description, int availType, String stateGroupUUID,
 			int filterDirection, float filterValue, boolean gauge,
 			String resourceTag) {
 		return (StringResponse) wrapper("addServiceLayerToService",sessionID,srvUUID,sLayerUUID,algorithm,description,availType,stateGroupUUID,filterDirection,filterValue,gauge,resourceTag);
 	}
 
 	@Override
 	public BoolResponse removeServiceLayer(String sessionID, String sLayerUUID) {
 		return (BoolResponse) wrapper("removeServiceLayer",sessionID,sLayerUUID);
 	}
 
 	@Override
 	public ServiceDataResponse getServiceLayerData(String sessionID,
 			String sLayerUUID) {
 		return (ServiceDataResponse) wrapper("getServiceLayerData",sessionID,sLayerUUID);
 	}
 
 	@Override
 	public BoolResponse setServiceLayerData(String sessionID,
 			String sLayerUUID, String algorithm, String description,
 			int availType, String stateGroupUUID, int filterDirection,
 			float filterValue, boolean gauge, String resourceTag) {
 		return (BoolResponse) wrapper("setServiceLayerData",sessionID,sLayerUUID,algorithm,description,availType,stateGroupUUID,filterDirection,filterValue,gauge,resourceTag);
 	}
 
 	@Override
 	public LabelArrayResponse getLegend(String sessionID, String planboard,
 			String nodeUUID) {
 		return (LabelArrayResponse) wrapper("getLegend",sessionID,planboard,nodeUUID);
 	}
 
 	@Override
 	public StringResponse createUserState(String sessionID, String stateUUID,
 			String planboard, String name, String colour) {
 		return (StringResponse) wrapper("createUserState",sessionID,stateUUID,planboard,name,colour);
 	}
 
 	@Override
 	public BoolResponse removeUserState(String sessionID, String stateUUID,
 			String planboard) {
 		return (BoolResponse) wrapper("removeUserState",sessionID,stateUUID,planboard);
 	}
 
 	@Override
 	public StateDataResponse getUserStateData(String sessionID,
 			String stateUUID, String planboard) {
 		return (StateDataResponse) wrapper("getUserStateData",sessionID,stateUUID,planboard);
 	}
 
 	@Override
 	public BoolResponse setUserStateData(String sessionID, String stateUUID,
 			String planboard, String name, String colour) {
 		return (BoolResponse) wrapper("setUserStateData",sessionID,stateUUID,planboard,name,colour);
 	}
 
 	@Override
 	public StringArrayResponse getUserStateContainers(String sessionID,
 			String stateUUID) {
 		return (StringArrayResponse) wrapper("getUserStateContainers",sessionID,stateUUID);
 	}
 
 	@Override
 	public StringResponse createStateGroup(String sessionID, String sgrUUID,
 			String name) {
 		return (StringResponse) wrapper("createStateGroup",sessionID,sgrUUID,name);
 	}
 
 	@Override
 	public BoolResponse removeStateGroup(String sessionID, String sgrUUID) {
 		return (BoolResponse) wrapper("removeStateGroup",sessionID,sgrUUID);
 	}
 
 	@Override
 	public StringResponse getStateGroupName(String sessionID, String sgrUUID) {
 		return (StringResponse) wrapper("getStateGroupName",sessionID,sgrUUID);
 	}
 
 	@Override
 	public BoolResponse setStateGroupName(String sessionID, String sgrUUID,
 			String name) {
 		return (BoolResponse) wrapper("setStateGroupName",sessionID,sgrUUID,name);
 	}
 
 	@Override
 	public StringArrayResponse getStateGroupMembers(String sessionID,
 			String sgrUUID) {
 		return (StringArrayResponse) wrapper("getStateGroupMembers",sessionID,sgrUUID);
 	}
 
 	@Override
 	public BoolResponse attachUserState(String sessionID, String sgrUUID,
 			String stateUUID) {
 		return (BoolResponse) wrapper("attachUserState",sessionID,sgrUUID,stateUUID);
 	}
 
 	@Override
 	public BoolResponse detachUserState(String sessionID, String sgrUUID,
 			String stateUUID) {
 		return (BoolResponse) wrapper("detachUserState",sessionID,sgrUUID,stateUUID);
 	}
 
 	@Override
 	public SlotDataArrayResponse getSlots(String sessionID, String planboard,
 			String nodeUUID, int start, int end) {
 		return (SlotDataArrayResponse) wrapper("getSlots",sessionID,planboard,nodeUUID,start,end);
 	}
 
 	@Override
 	public BoolResponse createSlot(String sessionID, String planboard,
 			String nodeUUID, int start, int end, String label, String method,
 			String text) {
 		return (BoolResponse) wrapper("createSlot",sessionID,planboard,nodeUUID,start,end,label,method,text);
 	}
 
 	@Override
 	public BoolResponse removeSlot(String sessionID, String planboard,
 			String nodeUUID, int start, int end, String label) {
 		return (BoolResponse) wrapper("removeSlot",sessionID,planboard,nodeUUID,start,end,label);
 	}
 
 	@Override
 	public StringArrayResponse getBoxTypes(String sessionID) {
 		return (StringArrayResponse) wrapper("getBoxTypes",sessionID);
 	}
 
 	@Override
 	public TupleArrayResponse getBoxFields(String sessionID, String type) {
 		return (TupleArrayResponse) wrapper("getBoxFields",sessionID,type);
 	}
 
 	@Override
 	public StringResponse createBox(String sessionID, String boxUUID,
 			String type, TupleArray data) {
 		return (StringResponse) wrapper("createBox",sessionID,boxUUID,type,data);
 	}
 
 	@Override
 	public BoolResponse removeBox(String sessionID, String boxUUID) {
 		return (BoolResponse) wrapper("removeBox",sessionID,boxUUID);
 	}
 
 	@Override
 	public TupleArrayResponse getBoxData(String sessionID, String boxUUID) {
 		return (TupleArrayResponse) wrapper("getBoxData",sessionID,boxUUID);
 	}
 
 	@Override
 	public BoolResponse setBoxData(String sessionID, String boxUUID,
 			String type, TupleArray data) {
 		return (BoolResponse) wrapper("setBoxData",sessionID,boxUUID,type,data);
 	}
 
 	@Override
 	public StringResponse createResource(String sessionID, String resUUID,
 			String type, String ownUUID, String name, String description,
 			String category, String tag, String value) {
 		return (StringResponse) wrapper("createResource",sessionID,resUUID,type,ownUUID,name,description,category,tag,value);
 	}
 
 	@Override
 	public BoolResponse removeResource(String sessionID, String resUUID) {
 		return (BoolResponse) wrapper("removeResource",sessionID,resUUID);
 	}
 
 	@Override
 	public ResourceDataResponse getResourceData(String sessionID, String resUUID) {
 		return (ResourceDataResponse) wrapper("getResourceData",sessionID,resUUID);
 	}
 
 	@Override
 	public ResourceDataResponse getResourceDataByTag(String sessionID,
 			String ownUUID, String tag, String type) {
 		return (ResourceDataResponse) wrapper("getResourceDataByTag",sessionID,ownUUID,tag,type);
 	}
 
 	@Override
 	public BoolResponse setResourceData(String sessionID, String resUUID,
 			String ownUUID, String name, String description, String category,
 			String tag, String value) {
 		return (BoolResponse) wrapper("setResourceData",sessionID,resUUID,ownUUID,name,description,category,tag,value);
 	}
 
 	@Override
 	public StringResponse getDownloadURL(String sessionID, String resUUID) {
 		return (StringResponse) wrapper("getDownloadURL",sessionID,resUUID);
 	}
 
 	@Override
 	public StringResponse getUploadURL(String sessionID, String resUUID) {
 		return (StringResponse) wrapper("getUploadURL",sessionID,resUUID);
 	}
 
 	@Override
 	public StringArrayResponse getJobTypes(String sessionID) {
 		return (StringArrayResponse) wrapper("getJobTypes",sessionID);
 	}
 
 	@Override
 	public TupleArrayResponse getJobFields(String sessionID, String type) {
 		return (TupleArrayResponse) wrapper("getJobFields",sessionID,type);
 	}
 
 	@Override
 	public StringResponse createJob(String sessionID, String jobUUID,
 			String type, String desc, int interval, TupleArray params) {
 		return (StringResponse) wrapper("createJob",sessionID,jobUUID,type,desc,interval,params);
 	}
 
 	@Override
 	public BoolResponse removeJob(String sessionID, String jobUUID) {
 		return (BoolResponse) wrapper("removeJob",sessionID,jobUUID);
 	}
 
 	@Override
 	public JobDataResponse getJobData(String sessionID, String jobUUID) {
 		return (JobDataResponse) wrapper("getJobData",sessionID,jobUUID);
 	}
 
 	@Override
 	public BoolResponse setJobData(String sessionID, String jobUUID,
 			String desc, int interval, TupleArray params) {
 		return (BoolResponse) wrapper("setJobData",sessionID,jobUUID,desc,interval,params);
 	}
 
 	@Override
 	public BoolResponse attachJobToAgent(String sessionID, String jobUUID,
 			String nodeUUID) {
 		return (BoolResponse) wrapper("attachJobToAgent",sessionID,jobUUID,nodeUUID);
 	}
 
 	@Override
 	public BoolResponse detachJobFromAgent(String sessionID, String jobUUID,
 			String nodeUUID) {
 		return (BoolResponse) wrapper("detachJobFromAgent",sessionID,jobUUID,nodeUUID);
 	}
 
 	@Override
 	public StringArrayResponse getJobAgents(String sessionID, String jobUUID) {
 		return (StringArrayResponse) wrapper("getJobAgents",sessionID,jobUUID);
 	}
 
 	@Override
 	public TransitionResponse getTransition(String sessionID, String jobUUID,
 			String nodeUUID, int range) {
 		return (TransitionResponse) wrapper("getTransition",sessionID,jobUUID,nodeUUID,range);
 	}
 
 	@Override
 	public BoolResponse setTransition(String sessionID, String jobUUID,
 			String nodeUUID, int range, String nextJobUUID, int length,
 			int overlap) {
 		return (BoolResponse) wrapper("setTransition",sessionID,jobUUID,nodeUUID,range,nextJobUUID,length,overlap);
 	}
 
 	@Override
 	public BoolResponse removeTransition(String sessionID, String jobUUID,
 			String nodeUUID, int range) {
 		return (BoolResponse) wrapper("removeTransition",sessionID,jobUUID,nodeUUID,range);
 	}
 
 	@Override
 	public IntResponse getRate(String sessionID, String algorithm,
 			String parentUUID, String childUUID, String peerUUID) {
 		return (IntResponse) wrapper("getRate",sessionID,algorithm,parentUUID,childUUID,peerUUID);
 	}
 
 	@Override
 	public BoolResponse setRate(String sessionID, String algorithm,
 			String parentUUID, String childUUID, String peerUUID, float rate) {
 		return (BoolResponse) wrapper("setRate",sessionID,algorithm,parentUUID,childUUID,peerUUID,rate);
 	}
 
 	@Override
 	public BoolResponse triggerRequest(String sessionID, String fromUUID,
 			String toUUID, String boxUUID, String type) {
 		return (BoolResponse) wrapper("triggerRequest",sessionID,fromUUID,toUUID,boxUUID,type);
 	}
 
 }
