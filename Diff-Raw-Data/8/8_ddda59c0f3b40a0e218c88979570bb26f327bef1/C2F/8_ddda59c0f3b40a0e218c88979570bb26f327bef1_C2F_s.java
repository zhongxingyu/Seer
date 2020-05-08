 package demo.services;
 
 import javax.ws.rs.GET;
 import javax.ws.rs.HeaderParam;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.codehaus.jettison.json.JSONException;
 
 import demo.domain.DemoVO;
 
 @Path("c2f")
 public class C2F {
 
     /** The log. */
     private static Log LOG = LogFactory.getLog(C2F.class.getName());
 
     @GET
     @Produces(MediaType.APPLICATION_JSON)
     @HeaderParam("Access-Control-Allow-Origin: *")
     public Response convertFtoC() throws JSONException {
 
 	DemoVO jsonObject = new DemoVO();
 
 	float fahrenheit;
 	float celsius = 0.0f;
 	fahrenheit = ((celsius * 9) / 5) + 32;
 
 	jsonObject.setCelsius(celsius);
 	jsonObject.setFahrenheit(fahrenheit);
 
 	LOG.debug(celsius + "C --> " + fahrenheit + "F");
 
	return Response.status(200).entity("" + jsonObject).build();
     }
 
     @Path("{c}")
     @GET
     @Produces(MediaType.APPLICATION_JSON)
     @HeaderParam("Access-Control-Allow-Origin: *")
     public Response convertFtoCfromInput(@PathParam("c") float c)
 	    throws JSONException {
 
 	DemoVO jsonObject = new DemoVO();
 	float fahrenheit;
 	float celsius = c;
 	fahrenheit = ((celsius * 9) / 5) + 32;
 
 	jsonObject.setCelsius(celsius);
 	jsonObject.setFahrenheit(fahrenheit);
	
 	LOG.debug(celsius + "C --> " + fahrenheit + "F");
 
	return Response.status(200).entity("" + jsonObject).build();
     }
 }
