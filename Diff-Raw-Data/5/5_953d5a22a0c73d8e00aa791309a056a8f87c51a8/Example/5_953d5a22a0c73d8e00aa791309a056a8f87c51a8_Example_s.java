 package com.conwet.samson;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import javax.xml.datatype.DatatypeFactory;
 import javax.xml.datatype.Duration;
 
 import org.w3c.dom.Node;
 
 import com.conwet.samson.jaxb.ContextAttribute;
 import com.conwet.samson.jaxb.ContextAttributeList;
 import com.conwet.samson.jaxb.ContextElement;
 import com.conwet.samson.jaxb.ContextElementResponse;
 import com.conwet.samson.jaxb.ContextRegistration;
 import com.conwet.samson.jaxb.ContextResponse;
 import com.conwet.samson.jaxb.ContextResponseList;
 import com.conwet.samson.jaxb.EntityId;
 import com.conwet.samson.jaxb.EntityIdList;
 import com.conwet.samson.jaxb.NotifyConditionType;
 import com.conwet.samson.jaxb.ObjectFactory;
 import com.conwet.samson.jaxb.RegisterContextResponse;
 import com.conwet.samson.jaxb.StatusCode;
 import com.conwet.samson.jaxb.SubscribeResponse;
 import com.conwet.samson.jaxb.UpdateActionType;
 
 
 public class Example {
 	
 	private QueryBroker querier;
 	private String id;
 	private String type;
 	private ObjectFactory factory;
 	
 	public Example() {
 		
 		querier = QueryFactory.newQuerier("130.206.80.195", 1026);
 		id = "11bc309bb577b7e6008814dafa003e91";
 		type = "Observation";
 		factory = new ObjectFactory();
 	}
 	
 	/**
 	 * Creates the mapping between names and values
 	 */
 	private Map<String, String> createMapping() {
 
 		Map<String, String> map = new HashMap<>();
 		
 		map.put("time", "2013-02-18 16:29");
 		map.put("provider", "Maria");
 		map.put("imageType", "Tree");
 		map.put("imageFile", "AppleTree.jpg");
 		map.put("imageComment", "This is a nice picture of an apple tree");
 		map.put("idOoI", "7476c494408ec56c6e27086260000614");
 		map.put("longitude", "16.508095566880247");
 //		map.put("imageURL", null);
 		map.put("latitude", "47.9808748641014");
 		map.put("address", "Reisenbachhof 119, 2440, Austria");
 		map.put("label", "4151");
 		map.put("species", "Malus domestica");
 		map.put("commonName", "Apple Tree");
 		map.put("height", "5 m.");
 		map.put("crownDiameter", "9 m.");
 		map.put("trunkDiameter", "90 cm.");
 		map.put("plantingYear", "2000");
 //		map.put("reportTime", null);
 //		map.put("reportProvider", null);
 //		map.put("reportImageType", null);
 //		map.put("reportImageQuality", null);
 //		map.put("reportComment", null);
 //		map.put("reportTreeStatus", null);
 //		map.put("reportBarkbeetle", null);
 //		map.put("reportDamage", null);
 		
 		return map;
 	}
 	
 	private void printEntity(EntityId entity) {
 		
 		System.out.println("*** Entity ***");
 		System.out.println("Type: " + entity.getType());
 		System.out.println("ID: " + entity.getId());
 		System.out.println("isPattern: " + entity.isIsPattern());
 		System.out.println();
 	}
 	
 	private void printAttributeList(ContextAttributeList cxtAttrList) {
 		
 		System.out.println("*** Attributes ***");
 		
 		for (ContextAttribute cxtAttr : cxtAttrList.getContextAttribute()) {
 			
 			System.out.println("name: " + cxtAttr.getName() +
 								", contextValue: " + extractNodeValue(
 													cxtAttr.getContextValue()));
 		}
 		
 		System.out.println();
 	}
 	
 	private void printStatusCode(StatusCode status) {
 		
 		System.out.println("*** Status ***");
 		System.out.println("Code: " + status.getCode());
 		System.out.println("ReasonPhrase: " + status.getReasonPhrase());
 		System.out.println("Details: " + extractNodeValue(status.getDetails()));
 		System.out.println();
 	}
 	
 	private String extractNodeValue(Object obj) {
 		
 		if (obj instanceof Node) {
 			
 			return ((Node) obj).getTextContent();
 		}
 		
 		return String.valueOf(obj);
 	}
 	
 	public void registerContext() throws Exception {
 				
 		// build the registration
 		ContextRegistration cxtReg = factory.createContextRegistration();
 		EntityIdList entityList = factory.createEntityIdList();
 		entityList.getEntityId().add(querier.newEntityId(type, id, false));
 		cxtReg.setEntityIdList(entityList);
 		cxtReg.setProvidingApplication("http://wirecloud.conwet.fi.upm.es");
 		
 		Duration duration = DatatypeFactory.newInstance().newDuration("PT24H");
 		
 		// sent it
 		RegisterContextResponse response = querier.registerContext(cxtReg, duration);
 		
 		// print the response
 		System.out.println("Duration: " + response.getDuration());
 		System.out.println("RegistrationId: " + response.getRegistrationId());
 	}
 	
 	public void updateContext() throws Exception {
 		
 		// populate the attribute list
 		ContextAttributeList attrList = factory.createContextAttributeList();
 		
 		for (Entry<String, String> entry : createMapping().entrySet()) {
 			
 			ContextAttribute attribute = factory.createContextAttribute();
 			attribute.setName(entry.getKey());
 			attribute.setContextValue(entry.getValue());
 			
 			attrList.getContextAttribute().add(attribute);
 		}
 		
 		// create the contextElement
 		ContextElement cxtElem = factory.createContextElement();
 		cxtElem.setEntityId(querier.newEntityId(type, id, false));
 		cxtElem.setContextAttributeList(attrList);
 		
 		// send it
 		ContextResponse cxtResp = querier.updateContext(cxtElem, UpdateActionType.APPEND);
 		
 		// print the response, usually there is only one element
 		ContextElementResponse elemResp = cxtResp.getContextResponseList()
 												.getContextElementResponse().get(0);
 		// status code information
 		StatusCode status = elemResp.getStatusCode();
 		printStatusCode(status);
 		
 		ContextElement cxtElemResp = elemResp.getContextElement();
 		
 		// entity info
 		EntityId entity = cxtElemResp.getEntityId();
 		printEntity(entity);
 		
 		if (status.getCode() == 200) {
 			
 			// attribute info
 			printAttributeList(cxtElemResp.getContextAttributeList());
 		}
 	}
 	
 	public void queryContext() throws Exception {
 		
 		ContextResponse response = querier.queryContext(querier.newEntityId(type, id, false));
 		
 		if (response.getErrorCode() != null) {
 			
 			System.err.println("*** E R R O R ***");
 			printStatusCode(response.getErrorCode());
 			
 		} else {
 			
 			ContextResponseList cxtResp = response.getContextResponseList();
 			
 			for (ContextElementResponse resp : cxtResp.getContextElementResponse()) {
 				
 				System.out.println("---------------");
 				printStatusCode(resp.getStatusCode());
 				
 				ContextElement elem = resp.getContextElement();
 				printEntity(elem.getEntityId());
 				printAttributeList(elem.getContextAttributeList());
 			}
 		}
 	}
 	
 	public String subscribeContext() throws Exception {
 		
 		EntityIdList entityList = factory.createEntityIdList();
 		entityList.getEntityId().add(querier.newEntityId(type, id, false));
 		List<String> condList = new ArrayList<>();
 		condList.add("provider");
 		Duration duration = DatatypeFactory.newInstance().newDuration("PT24H");
 		
 		SubscribeResponse resp = querier.subscribe(entityList.getEntityId(), condList,
 											"http://localhost:4455/notify", duration,
 											NotifyConditionType.ONCHANGE);
 		
 		System.out.println("subscribe context resp: " + resp);
 		return resp.getSubscriptionId();
 	}
 	
 	public void subscribeUpdate(String subscriptionID) throws Exception {
 		
 		List<EntityId> idList = new ArrayList<>();
 		idList.add(querier.newEntityId(type, id, false));
 		
 		List<String> condList = new ArrayList<>();
 		condList.add("latitude");
 		condList.add("longitude");
 		
 		Duration duration = DatatypeFactory.newInstance().newDuration("PT12H");
 		
 		SubscribeResponse resp = querier.subscribeUpdate(subscriptionID, idList,
 								condList, duration, NotifyConditionType.ONCHANGE);
 		
 		System.out.println("subscribe update: " + resp);
 	}
 	
 	public static void main(String[] args) throws Exception {
 		
 		Example example = new Example();
 		example.registerContext();
 		String subID = example.subscribeContext();
		// TODO Fix subscribeUpdate 
//		example.subscribeUpdate(subID);
 		example.updateContext();
 		example.queryContext();
 	}
 }
