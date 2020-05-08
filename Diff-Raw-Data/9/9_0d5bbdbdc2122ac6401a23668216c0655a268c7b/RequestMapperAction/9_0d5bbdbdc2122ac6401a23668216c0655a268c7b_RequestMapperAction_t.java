 package org.jboss.fit.systemintegration.actions;
 
 import java.util.Map;
 
 import java.util.HashMap;
 import org.jboss.soa.esb.actions.AbstractActionLifecycle;
 import org.jboss.soa.esb.helpers.ConfigTree;
 import org.jboss.soa.esb.message.Message;
 
 public class RequestMapperAction extends AbstractActionLifecycle {
 	
 	private ConfigTree config;
 	
 	public RequestMapperAction(ConfigTree _config) {
 		this.config = _config;
 	}
 	
 	public Message process(Message message) {
		Map msgMap = (Map)message.getBody().get("orderMap");
 		
 		msgMap.put("message", msgMap.get("order").toString());
 		
 		message.getBody().add(msgMap);
 		
 		return message;
 	}
 
 }
