 package org.jboss.fit.systemintegration.actions;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.jboss.fit.systemintegration.pojo.Order;
 import org.jboss.soa.esb.actions.AbstractActionLifecycle;
 import org.jboss.soa.esb.helpers.ConfigTree;
 import org.jboss.soa.esb.http.HttpRequest;
 import org.jboss.soa.esb.message.Body;
 import org.jboss.soa.esb.message.Message;
 
 public class ProcessHttpMessageAction extends AbstractActionLifecycle {
 	
 	private ConfigTree config;
 	
 	public ProcessHttpMessageAction(ConfigTree _config) {
 		this.config = _config;
 	}
 
 	public Message processMessage(Message message) {
 		HttpRequest request = HttpRequest.getRequest(message);		
 		Map<String, String[]> params = request.getQueryParams();
 		Body body = message.getBody();		
 		HashMap msgMap = new HashMap();
 		Order o = new Order();
 		o.setId(Long.parseLong(params.get("id")[0]));
 		o.setColor(params.get("color")[0]);
 		msgMap.put("order", o);
		body.add(msgMap);
 		
 		return message;
 	}
 	
 }
