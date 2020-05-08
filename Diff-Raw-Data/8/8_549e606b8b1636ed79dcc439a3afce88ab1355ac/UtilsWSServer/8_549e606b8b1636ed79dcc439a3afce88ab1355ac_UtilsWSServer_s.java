 package org.bioinfo.infrared.ws.server.rest.utils;
 
 import java.io.IOException;
 import java.net.URL;
 import java.util.Arrays;
 
 import javax.ws.rs.DefaultValue;
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.UriInfo;
 
 import org.bioinfo.http.client.HttpGetRequest;
 import org.bioinfo.http.client.HttpPostRequest;
 import org.bioinfo.http.client.HttpRequest;
 import org.bioinfo.infrared.ws.server.rest.GenericRestWSServer;
 import org.bioinfo.infrared.ws.server.rest.exception.VersionException;
 
 import com.sun.jersey.api.client.ClientResponse.Status;
 
 @Path("/{version}/utils")
 @Produces("text/plain")
 public class UtilsWSServer extends GenericRestWSServer {
 
 //	public UtilsWSServer(@Context UriInfo uriInfo) throws VersionException, IOException {
 //		super(uriInfo);
 //	}
 	
 	public UtilsWSServer(@PathParam("version") String version, @Context UriInfo uriInfo) throws VersionException, IOException {
 		super(version, uriInfo);
 	}
 	
 //	public UtilsWSServer(@PathParam("version") String version, @PathParam("species") String species, @Context UriInfo uriInfo) throws VersionException, IOException {
 //		super(version, species, uriInfo);
 //	}
 	
 	@GET
 	@Path("/proxy")
 	public Response proxy(@QueryParam("url") String url, @DefaultValue("get") @QueryParam("method") String method) {
 		try {
 			System.out.println(url);
 			HttpRequest req = null;
 			if("post".equalsIgnoreCase(method)) {
 				req = new HttpPostRequest(new URL(url));				
 			} else {
 				req = new HttpGetRequest(new URL(url));
 			}
 			// It will not work for multipart-form post,
 			// only for GET and 'standard' POST
 //			for(Object key: MapUtils.getKeys(request.getParameterMap())) {
 //				if (!"url".equalsIgnoreCase((String) key)) {				
 //					req.addParameter((String) key, request.getParameter((String) key));
 //				}	
 //			}
 			System.out.println("1");
 			String data = req.doCall();
 			System.out.println("2");
 			if(data == null) {
 				data = "ERROR: could not call " + url;
 			}
 			System.out.println(data);
 			return generateResponse("", Arrays.asList(data));
 		} catch (Exception e) {
 			e.printStackTrace();
 			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
 		}
 	}
 	
 }
