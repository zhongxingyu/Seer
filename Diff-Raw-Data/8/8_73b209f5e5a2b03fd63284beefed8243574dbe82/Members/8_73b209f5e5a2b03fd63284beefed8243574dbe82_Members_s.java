 package edu.psu.sweng.ff.client;
 
 import java.net.URI;
 
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.MultivaluedMap;
 
 import com.google.gson.Gson;
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.ClientResponse;
 import com.sun.jersey.api.client.WebResource;
 import com.sun.jersey.api.client.ClientResponse.Status;
 import com.sun.jersey.core.util.MultivaluedMapImpl;
 
 import edu.psu.sweng.ff.common.Member;
 
 public class Members {
 
 	private final static String TOKEN_HEADER = "X-UserToken";
 	
 	private final static String BASE_URL = "http://www.amphibian.com/ff_server/resource/member/";
 
 	private static String userToken;
 	
 	private static Client c = Client.create();
 	
 	public static String authenticate(String username, String password) {
 		
 		String url = BASE_URL + "authenticate";
 		
 		MultivaluedMap f = new MultivaluedMapImpl();
 		f.add("username", username);
 		f.add("password", password);
 		WebResource r = c.resource(url);
 		ClientResponse response = r.type(MediaType.APPLICATION_FORM_URLENCODED).entity(f).post(ClientResponse.class);
 
 		String token = null;
 		if (response.getStatus() == Status.OK.getStatusCode()) {
 			MultivaluedMap headers = response.getHeaders();
 			token = (String) headers.getFirst(TOKEN_HEADER);
 			userToken = token;
 		} else {
 			System.err.println(response);
 		}
 		
 		return token;
 		
 	}
 	
 	public static void setUserToken(String t) {
 		userToken = t;
 	}
 
 	public static Member getTokenOwner() {
 		
 		String url = BASE_URL;
 	
 		WebResource r = c.resource(url);
 		ClientResponse response = r.header(TOKEN_HEADER, userToken)
 				.accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
 		
 		Member m = null;
 		if (response.getStatus() == Status.OK.getStatusCode()) {
 			String json = response.getEntity(String.class);
 			Gson gson = new Gson();
 			m = gson.fromJson(json, Member.class);
 		} else {
 			System.err.println(response);
 		}
 		
 		return m;
 	
 	}
 
 	public static Member getByUserName(String un) {
 	
 		String url = BASE_URL + un;
 	
 		WebResource r = c.resource(url);
 		ClientResponse response = r.header(TOKEN_HEADER, userToken)
 				.accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
 		
 		Member m = null;
 		if (response.getStatus() == Status.OK.getStatusCode()) {
 			String json = response.getEntity(String.class);
 			Gson gson = new Gson();
 			m = gson.fromJson(json, Member.class);
 		} else {
 			System.err.println(response);
 		}
 		
 		return m;
 	
 	}
 
 	public static Member getByUserId(String id) {
 		
 		String url = BASE_URL + "id/" + id;
 	
 		WebResource r = c.resource(url);
 		ClientResponse response = r.header(TOKEN_HEADER, userToken)
 				.accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
 		
 		Member m = null;
 		if (response.getStatus() == Status.OK.getStatusCode()) {
 			String json = response.getEntity(String.class);
 			Gson gson = new Gson();
 			m = gson.fromJson(json, Member.class);
 		} else {
 			System.err.println(response);
 		}
 		
 		return m;
 	
 	}
 
 	public static Member update(Member m) {
 		
 		String url = BASE_URL + "id/" + m.getId();
 
 		Gson gson = new Gson();
 		String json = gson.toJson(m);
 
 		WebResource r = c.resource(url);
 		ClientResponse response = r.header(TOKEN_HEADER, userToken)
 				.type(MediaType.APPLICATION_JSON).entity(json).put(ClientResponse.class);
 		if (response.getStatus() == Status.OK.getStatusCode()) {
 			return m;
 		} else {
 			System.err.println(response);
 		}
 		return null;
 		
 	}
 
 	public static Member create(Member m) {
 		
 		String url = BASE_URL;
 		
 		Gson gson = new Gson();
 		String json = gson.toJson(m);
 		
 		WebResource r = c.resource(url);
 		ClientResponse response = r.entity(json)
 				.type(MediaType.APPLICATION_JSON).post(ClientResponse.class);
 		URI u = response.getLocation();
 		String uri = u.toString();
 		String id = uri.substring(uri.lastIndexOf('/') + 1);
 
 		m = getByUserId(id);
		
 		return m;
 		
 	}
 
 	
 }
