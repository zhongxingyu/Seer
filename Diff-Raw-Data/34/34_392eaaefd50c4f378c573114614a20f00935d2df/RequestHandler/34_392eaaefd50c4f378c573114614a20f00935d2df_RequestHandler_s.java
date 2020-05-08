 package com.github.bcap.dht.server.handler;
 
 import java.util.Date;
 
 import com.github.bcap.dht.message.request.Request;
 import com.github.bcap.dht.message.response.Response;
 import com.github.bcap.dht.node.Contact;
 import com.github.bcap.dht.node.Node;
 import com.github.bcap.dht.server.Server;
 
public abstract class RequestHandler <Req extends Request, Resp extends Response> {
 	
	public Resp handle(Server server, Req request) throws RequestHandlerException {
 		Contact destination = request.getDestination();
 		Node node = server.getNode(destination);
 
 		if (node == null)
 			throw new RequestHandlerException("Received request is intended for a node with id " + destination.asIdentifier() + " that is not managed by this server");
 
		Resp response = this.handleImpl(node, request);
 		
 		response.setSource(new Contact(node.getValue(), server.getIp(), server.getPort()));
 		response.setDestination(request.getSource());
 
 		response.setConversationId(request.getConversationId());
 		response.setIssuedDate(new Date());
 		
 		return response;
 	}
 	
	public abstract Resp handleImpl(Node node, Req request) throws RequestHandlerException;
 }
