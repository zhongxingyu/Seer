 import java.net.URI;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.MultivaluedMap;
 import javax.ws.rs.core.UriBuilder;
 
 import org.codehaus.jettison.json.JSONArray;
 import org.codehaus.jettison.json.JSONObject;
 
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.WebResource;
 import com.sun.jersey.api.client.config.ClientConfig;
 import com.sun.jersey.api.client.config.DefaultClientConfig;
 import com.sun.jersey.core.util.MultivaluedMapImpl;
 
 public class Test_con {
 	public static void main(String arg[]) {
 		ClientConfig config = new DefaultClientConfig();
 
 		Client client = Client.create(config);
 		WebResource service = client.resource(getBaseURI());
 
 //		System.out.println("getdss "
 //				+ service.path("rest").path("dsg").path("getdss?jsoncallback=?")
 //						.accept(MediaType.APPLICATION_JSON).get(String.class));
 //		System.out.println("getgeodss "
 //				+ service.path("rest").path("dsg").path("getgeodss")
 //						.accept(MediaType.APPLICATION_JSON).get(String.class));
 //		System.out.println("getstadss "
 //				+ service.path("rest").path("dsg").path("getstadss")
 //						.accept(MediaType.APPLICATION_JSON).get(String.class));
 //		System.out.println("getds/RegionInfo2 "
 //				+ service.path("rest").path("dsg").path("/getds/RegionInfo2")
 //						.accept(MediaType.APPLICATION_JSON).get(String.class));
 //		System.out.println("getgeods/RegionInfo3 "
 //				+ service.path("rest").path("dsg").path("getgeods/RegionInfo3")
 //						.accept(MediaType.APPLICATION_JSON).get(String.class));
 //		System.out.println("getstads/FilteredByCT_Domain "
 //				+ service.path("rest").path("dsg")
 //						.path("/getstads/FilteredByCT_Domain")
 //						.accept(MediaType.APPLICATION_JSON).get(String.class));
 //		System.out.println("getdsfds/RegionInfo2 "
 //				+ service.path("rest").path("dsg")
 //						.path("/getdsfds/RegionInfo2")
 //						.accept(MediaType.APPLICATION_JSON).get(String.class));
 
 		// System.out.println(service.path("rest").path("dsg").path("hello")
 		// .accept(MediaType.APPLICATION_JSON).get(String.class));
 		// System.out.println(service.path("rest").path("dsg").path("jsonp")
 		// .accept(MediaType.APPLICATION_JSON).get(String.class));
 		//
 		System.out.println(".........................");
 		JSONObject job = new JSONObject();
 		JSONArray fields = new JSONArray();
 		fields.put("SiteName");
 		fields.put("Latitude");
 		fields.put("Longitude");
 //		try {
 //			job.put("fields", fields);
 //			ClientResponse response = service.path("rest").path("dsp")
 //					.path("getds").path("RegionInfo2")
 //					.accept(MediaType.APPLICATION_JSON)
 //					.post(ClientResponse.class, job);
 //			System.out.println(response.getEntity(String.class));
 //			URI  u =new URI("http://houfeng:8080/jerseyWebServiceTest/services/hello/test_get2");   
 //			    System.out.println(u);   
 
 		
 		
 			   WebResource resource = client.resource(service.path("rest").path("dsg").path("getdsa")
 						 .accept(MediaType.APPLICATION_JSON).get(String.class));   
 			  MultivaluedMap<String, String>  params = new MultivaluedMapImpl();   
 			    params.add("dataset", "slot_Imsi_All");   
 			   String result = resource.queryParams(params).get(String.class);   
 			    System.out.println(result);
 //			ClientResponse response2 = service.path("rest").path("up")
 //					.path("adduser")
 //					.accept(MediaType.APPLICATION_JSON)
 //					.post(ClientResponse.class, job);
 //			System.out.println(response2.getEntity(String.class));
 //		} 
 		// service.path("rest").path("dsd").path("/rmds/sa").delete();
 		// ClientResponse response =
 		// service.path("rest").path("dsu").path("/upds/a/b")
 		// .accept(MediaType.APPLICATION_JSON).put(ClientResponse.class, job);
 		// System.out.println(response.getStatus());
 
 
 		// System.out.println(service.path("rest").path("dsd").path("/rmds/a")
 		// .accept(MediaType.APPLICATION_JSON).get(String.class));
 	}
 
 	private static URI getBaseURI() {
 		return UriBuilder.fromUri("http://10.1.1.55:8082/mdap").build();
 	}
 
 }
