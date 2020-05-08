 package com.kdcloud.gwt.client.rest;
 
 import java.util.ArrayList;
 
 import org.restlet.client.resource.ClientProxy;
 import org.restlet.client.resource.Get;
 import org.restlet.client.resource.Put;
 import org.restlet.client.resource.Result;
 
 import com.kdcloud.server.entity.Dataset;
 
 public interface UserDataResourceProxy extends ClientProxy {
 	
 	@Get
 	public void list(Result<ArrayList<Dataset>> callback);
 	
 	@Put
	public void createDataset(String name, String description, Result<Long> callback);
 
 }
