 package co.geomati.api;
 
 import static junit.framework.Assert.assertEquals;
 
 import java.io.IOException;
 import java.util.List;
 
 import javax.ws.rs.client.Entity;
 import javax.ws.rs.core.Response;
 
 import org.codehaus.jackson.JsonGenerationException;
 import org.codehaus.jackson.map.JsonMappingException;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.junit.Test;
 
 import co.geomati.olakease.persistence.Identifiable;
 
 public abstract class AbstractResourceManagementTest<TYPE extends Identifiable>
 		extends AbstractAPITest {
 
 	@Test
 	public void testPostResource() throws Exception {
 		postResource();
 	}
 
 	private TYPE postResource() throws IOException, JsonGenerationException,
 			JsonMappingException {
 		TYPE resource = createResourceToPost();
 		return postResource(getPath(), resource, getResourceClass());
 	}
 
 	protected abstract TYPE createResourceToPost() throws IOException;
 
 	protected <LOCAL_TYPE> LOCAL_TYPE postResource(String path,
 			LOCAL_TYPE resource, Class<LOCAL_TYPE> returnClass)
 			throws IOException, JsonGenerationException, JsonMappingException {
 		Response response = target(path).request().post(
 				Entity.json(new ObjectMapper().writeValueAsString(resource)));
 		assertEquals(200, response.getStatus());
 		LOCAL_TYPE postedResource = new ObjectMapper().readValue(
 				response.readEntity(String.class), returnClass);
 		return postedResource;
 	}
 
 	protected abstract String getPath();
 
 	@Test
 	public void testPostResourceWrongParameterTypes() throws Exception {
 		Response response = target(getPath()).request().post(
 				Entity.json("{\"id\":\"a\"}"));
 		assertEquals(400, response.getStatus());
 	}
 
 	@Test
 	public void testGetResources() throws Exception {
 		testPostResource();
 
 		String response = target(getPath()).request().get(String.class);
 		@SuppressWarnings("unchecked")
 		List<TYPE> resources = new ObjectMapper().readValue(response,
 				List.class);
 		assertEquals(1, resources.size());
 	}
 
 	@Test
 	public void testGetResource() throws Exception {
 		TYPE postedResource = postResource();
 		int resourceId = postedResource.getId();
 
 		String getResponse = target(getPath() + "/" + resourceId).request()
 				.get(String.class);
 		TYPE resource = new ObjectMapper().readValue(getResponse,
 				getResourceClass());
 		assertEquals(resourceId, resource.getId());
 		checkResourceIsEqualPosted(resource);
 	}
 
 	protected abstract void checkResourceIsEqualPosted(TYPE resource);
 
 	@Test
 	public void testPutResource() throws Exception {
 		TYPE message = createResourceToPost();
 		TYPE postedResource = postResource(getPath(), message,
 				getResourceClass());
 		int resourceId = postedResource.getId();
 
 		modifyResource(message);
		message.setId(resourceId);
 		Response putResponse = target(getPath()).request().put(
 				Entity.json(new ObjectMapper().writeValueAsString(message)));
 		assertEquals(200, putResponse.getStatus());
 
 		String getResponse = target(getPath() + "/" + resourceId).request()
 				.get(String.class);
 		TYPE updatedResource = new ObjectMapper().readValue(getResponse,
 				getResourceClass());
 		assertEquals(resourceId, updatedResource.getId());
 		checkResourceIsModified(updatedResource);
 	}
 
 	protected abstract void checkResourceIsModified(TYPE updatedResource);
 
 	protected abstract void modifyResource(TYPE resource) throws IOException;
 
 	protected abstract Class<TYPE> getResourceClass();
 
 }
