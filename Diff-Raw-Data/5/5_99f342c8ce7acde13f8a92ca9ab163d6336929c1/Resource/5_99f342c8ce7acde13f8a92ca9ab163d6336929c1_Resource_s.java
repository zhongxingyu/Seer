 /**
  * 
  */
 package io.spire.api;
 
 import io.spire.api.Api.APIDescriptionModel.APISchemaModel;
 import io.spire.request.Request.RequestType;
 import io.spire.request.Request;
 import io.spire.request.RequestData;
 import io.spire.request.RequestFactory;
 import io.spire.request.Response;
 import io.spire.request.ResponseException;
 
 import java.io.IOException;
 import java.lang.reflect.Constructor;
 import java.util.HashMap;
 import java.util.Map;
 
 
 /**
  * @author jorge
  *
  */
 public abstract class Resource {
 	
 	protected ResourceModel model;
 	protected APISchemaModel schema;
 	
 	
 	/**
 	 * 
 	 */
 	public Resource() {
 	}
 	
 	public Resource(APISchemaModel schema) {
 		this.schema = schema;
		this.model = new ResourceModel(new HashMap<String, Object>());
 		this.initialize();
 	}
 	
 	public Resource(ResourceModel model, APISchemaModel schema) {
 		this.schema = schema;
 		this.model = model;
 		this.initialize();
 	}
 	
 	/**
 	 * This is called automatically by the resource constructor
 	 * to initialize any other internal properties.
 	 * Derived classes should override this method
 	 */
 	protected abstract void initialize();
 	
 	@SuppressWarnings("unchecked")
 	protected ResourceModel getResourceModel(String resourceName){
 		Map<String, Object> rawModel = model.getProperty(resourceName, Map.class);
 		if(rawModel == null){
 			rawModel = new HashMap<String, Object>();
 			model.setProperty(resourceName, rawModel);
 		}
 		return new ResourceModel(rawModel);
 	}
 	
 	public static class ResourceModel implements ResourceModelInterface {
 		private Map<String, Object> rawModel;
 		
 		public ResourceModel(Map<String, Object> data){
 			this.rawModel = data;
 			if(this.rawModel == null){
 				this.rawModel = new HashMap<String, Object>();
 			}
 		}
 
 		@Override
 		public <T>T getProperty(String propertyName, Class<T> type){
 			T t = null;
 			try{
 				t = type.cast(rawModel.get(propertyName));
 			}catch (Exception e) {
 				//e.printStackTrace();
 			}
 			return t;
 		}
 		
 		@Override
 		public void setProperty(String propertyName, Object data){
 			rawModel.put(propertyName, data);
 		}
 		
 		@SuppressWarnings("unchecked")
 		public ResourceModel getResourceMapCollection(String resourceName){
 			HashMap<String, Object> rawModelCollection = new HashMap<String, Object>();
 			Map<String, Object> resources = this.getProperty(resourceName, Map.class);
 			for (Map.Entry<String, Object> resource : resources.entrySet()) {
 				String name = (String)resource.getKey();
 				Map<String, Object> rawData = (Map<String, Object>)resource.getValue();
 				ResourceModel rawModel = new ResourceModel(rawData);
 				rawModelCollection.put(name, rawModel);
 			}
 			return new ResourceModel(rawModelCollection);
 		}
 		
 		@SuppressWarnings("unchecked")
 		public <T> Map<String, T> getMapCollection(String resourceName, Class<T> T, APISchemaModel schema) throws RuntimeException{
 			HashMap<String, T> mapCollection = new HashMap<String, T>();
 			Constructor<T> constructorT;
 			try {
 				constructorT = T.getConstructor(ResourceModel.class, APISchemaModel.class);
 			} catch (NoSuchMethodException e) {
 				e.printStackTrace();
 				return null;
 			}
 			Map<String, Object> resources = this.getProperty(resourceName, Map.class);
 			for (Map.Entry<String, Object> resource : resources.entrySet()) {
 				String name = (String)resource.getKey();
 				Map<String, Object> rawData = (Map<String, Object>)resource.getValue();
 				ResourceModel rawModel = new ResourceModel(rawData);
 				try{
 					T t = constructorT.newInstance(rawModel, schema);
 					mapCollection.put(name, t);
 				}catch (Exception e) {
 					e.printStackTrace();
 					return null;
 				}
 			}
 			return mapCollection;
 		}
 	}
 	
 	public String getUrl(){
 		return model.getProperty("url", String.class);
 	}
 	
 	public void setURL(String url){
 		model.setProperty("url", url);
 	}
 	
 	public abstract String getResourceName();
 	
 	public String getMediaType(){
 		String resourceName = this.getResourceName();
 		return schema.getMediaType(resourceName);
 	}
 	
 	public String getCapability(){
 		return model.getProperty("capability", String.class);
 	}
 	
 	public void setCapability(String capability){
 		model.setProperty("capability", capability);
 	}
 	
 	public String getKey(){
 		return model.getProperty("key", String.class);
 	}
 	
 	public String getType(){
 		return model.getProperty("type", String.class);
 	}
 	
 	public String getName(){
 		return model.getProperty("name", String.class);
 	}
 	
 	public void setName(String name){
 		model.setProperty("name", name);
 	}
 	
 	@SuppressWarnings("unchecked")
 	public void get() throws ResponseException, IOException{
 		RequestData data = RequestFactory.createRequestData();
 		data.method = RequestType.HTTP_GET;
 		data.url = model.getProperty("url", String.class);
 		data.headers.put("Authorization", "Capability " + model.getProperty("capability", String.class));
 		data.headers.put("Accept", this.getMediaType());
 		
 		Request request = RequestFactory.createRequest(data);
 		Response response = request.send();
 		if(!response.isSuccessStatusCode())
 			throw new ResponseException(response, "Error getting " + getResourceName());
 		
 		model.rawModel = response.parseAs(HashMap.class);
 		this.initialize();
 	}
 
 	@SuppressWarnings("unchecked")
 	public void update() throws ResponseException, IOException{
 		RequestData data = RequestFactory.createRequestData();
 		data.method = RequestType.HTTP_PUT;
 		data.url = model.getProperty("url", String.class);
 		
 		data.headers.put("Authorization", "Capability " + model.getProperty("capability", String.class));
 		data.headers.put("Accept", this.getMediaType());
 		data.headers.put("Content-Type", this.getMediaType());
 		
 		data.body = model.rawModel;
 		
 		Request request = RequestFactory.createRequest(data);
 		Response response = request.send();
 		if(!response.isSuccessStatusCode())
 			throw new ResponseException(response, "Error updating " + getResourceName());
 		
 		model.rawModel = response.parseAs(HashMap.class);
 		this.initialize();
 	}
 	
 	public void delete() throws ResponseException, IOException{
 		RequestData data = RequestFactory.createRequestData();
 		data.method = RequestType.HTTP_DELETE;
 		data.url = model.getProperty("url", String.class);
 		
 		data.headers.put("Authorization", "Capability " + model.getProperty("capability", String.class));
 		data.headers.put("Accept", this.getMediaType());
 		data.headers.put("Content-Type", this.getMediaType());
 		
 		Request request = RequestFactory.createRequest(data);
 		Response response = request.send();
 		if(!response.isSuccessStatusCode())
 			throw new ResponseException(response, "Error deleting " + getResourceName());
 	}
 }
