 package com.github.bcap.dht.server.handler;
 
import com.github.bcap.dht.message.request.Request;
 import com.github.bcap.dht.message.request.StoreRequest;
import com.github.bcap.dht.message.response.Response;
 import com.github.bcap.dht.message.response.StoreResponse;
import com.github.bcap.dht.node.Identifier;
 import com.github.bcap.dht.node.Node;
 
public class StoreRequestHandler extends RequestHandler {
 
 	@Override
	public Response handleImpl(Node node, Request request) {
		StoreRequest store = (StoreRequest) request;
		Identifier key = store.getIdentifier();
		byte[] data = store.getData();
		node.getDataStorage().put(key, data);
		return new StoreResponse();
 	}
 
 }
