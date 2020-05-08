 package cz.nuc.wheelgo;
 
 import java.io.StringReader;
 import java.lang.reflect.Type;
 import java.util.List;
 
 import javax.ws.rs.Consumes;
 import javax.ws.rs.FormParam;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Unmarshaller;
 import javax.xml.transform.stream.StreamSource;
 
 import com.google.appengine.api.datastore.Entity;
 import com.google.appengine.api.datastore.EntityNotFoundException;
 import com.google.appengine.api.datastore.Key;
 import com.google.appengine.api.datastore.KeyFactory;
 import com.google.appengine.api.datastore.Text;
 import com.google.appengine.api.taskqueue.Queue;
 import com.google.appengine.api.taskqueue.QueueFactory;
 import com.google.appengine.api.taskqueue.TaskOptions;
 import com.google.appengine.api.taskqueue.TaskOptions.Method;
 import com.google.gson.Gson;
 import com.google.gson.reflect.TypeToken;
 
 @Path("/api/")
 public class NavigatorService {
 
    @GET
    @Produces("application/json")
    @Path("hello")
    public String hello() {
        return "It works!";
    }
	
 	@POST
 	@Produces({ MediaType.APPLICATION_JSON})
 	@Consumes({ MediaType.APPLICATION_JSON})
 	@Path("/navigate")
 	public String navigate(@QueryParam("latFrom") String latFrom,
 			@QueryParam("longFrom") String longFrom,
 			@QueryParam("latTo") String latTo,
 			@QueryParam("longTo") String longTo, String params) throws JAXBException {
 
 		Double latFromDouble;
 		Double longFromDouble;
 		Double latToDouble;
 		Double longToDouble;
 		try {
 			latFromDouble = Double.parseDouble(latFrom);
 			longFromDouble = Double.parseDouble(longFrom);
 			latToDouble = Double.parseDouble(latTo);
 			longToDouble = Double.parseDouble(longTo);
 			createBackendTask(latFromDouble, longFromDouble, latToDouble,
 					longToDouble, params);
 		} catch (Exception e) {
 			e.printStackTrace();
 			return "";
 		}
 
 		Gson gson = new Gson();
 		NavigationParameters paramsObject = gson.fromJson(params, NavigationParameters.class);
 		/*
 		List<Location> locationsToAvoid;  
 		Type listType = new TypeToken<List<Location>>(){}.getType();
 		locationsToAvoid = gson.fromJson(params, listType);
 		*/
 
 		if (paramsObject == null)
 		{
 			return "" + NavigationTask.generateCode(latFromDouble, longFromDouble,
 							latToDouble, longToDouble);
 		}
 		else
 		{
 			return "" + NavigationTask.generateCode(latFromDouble, longFromDouble,
 					latToDouble, longToDouble, paramsObject.locationsToAvoid);
 		}
 
 	}
 
 	@GET
 	@Produces({ MediaType.APPLICATION_JSON })
 	@Path("/result")
 	public Response getPath(@QueryParam("id") String id) 
 			throws Exception {
 		Key k = KeyFactory.createKey("path", Long.valueOf(id));
 		String marshalled = null;
 		try {
 			Entity result = Util.getDatastoreServiceInstance().get(k);
 			marshalled = ((Text) result.getProperty("path")).getValue();
 		}
 		catch(EntityNotFoundException e)
 		{
 			return Response.status(500).entity("Path is not ready yet").build();
 		}
 
 		/*JAXBContext context = JAXBContext.newInstance(JaxbList.class);
 		Unmarshaller um = context.createUnmarshaller();
 		JaxbList<NavigationNode> response = (JaxbList<NavigationNode>) um.unmarshal(new StreamSource(new StringReader(marshalled)));*/
 
 		return Response.ok(marshalled).build();
 	}
 
 	private void createBackendTask(Double latFrom, Double longFrom,
 			Double latTo, Double longTo, String params) {
 		Queue queue = QueueFactory.getDefaultQueue();
 		TaskOptions taskOptions = TaskOptions.Builder.withUrl("/findPath")
 				.param("latFrom", latFrom.toString())
 				.param("longFrom", longFrom.toString())
 				.param("latTo", latTo.toString())
 				.param("longTo", longTo.toString())
 				.param("gsonParameters", params).method(Method.POST);
 		queue.add(taskOptions);
 	}
 }
