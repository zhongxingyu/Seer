 /**
  * 
  */
 package com.xplenty;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.commons.codec.binary.Base64;
 
 import com.fasterxml.jackson.core.JsonParseException;
 import com.fasterxml.jackson.core.type.TypeReference;
 import com.fasterxml.jackson.databind.JsonMappingException;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.ClientResponse;
 import com.sun.jersey.api.client.ClientResponse.Status;
 import com.sun.jersey.api.client.UniformInterfaceException;
 import com.sun.jersey.api.client.WebResource;
 import com.sun.jersey.api.client.config.ClientConfig;
 import com.sun.jersey.api.client.config.DefaultClientConfig;
 import com.xplenty.exceptions.AuthFailedException;
 import com.xplenty.exceptions.PaymentRequiredException;
 import com.xplenty.exceptions.XPlentyInternalAPIException;
 import com.xplenty.exceptions.XPlentyInternalServerException;
 import com.xplenty.model.Cluster;
 import com.xplenty.model.ClusterPlan;
 import com.xplenty.model.Job;
 
 /**
  * @author Yuriy Kovalek
  *
  */
 public class XPlentyAPI {
 	private static final String BASE_URL = "https://api-staging.xplenty.com";	
 	private static final String API_PATH = "api";
 	
 	private static final String CLUSTER_PLANS = "cluster_plans";
 	private static final String CLUSTERS = "clusters";
 	private static final String JOBS = "jobs";
 	
 	private String ACCOUNT_NAME;
 	private String API_KEY;	
 	private Client client;
 	private ObjectMapper mapper = new ObjectMapper();
 	
 	public XPlentyAPI(String accountName, String apiKey) {
 		ACCOUNT_NAME = accountName;
 		API_KEY = apiKey;
 		
 		ClientConfig config = new DefaultClientConfig();
 		client = Client.create(config);
 	}
 
 	public List<ClusterPlan> listClusterPlans() {
 		try {
 			WebResource resource = client.resource(getMethodURL(CLUSTER_PLANS));
 			ClientResponse response = resource
 										.accept("application/vnd.xplenty+json")
 										.header("Authorization", "Basic " + base64(API_KEY))
 										.get(ClientResponse.class);
 			
 			validate(response);
 			
 			//HTTP 200 OK
 			String json = response.getEntity(String.class);
 			List<ClusterPlan> plans = mapper.readValue(json, new TypeReference<List<ClusterPlan>>() {});
 			return plans;
 		} catch (UnsupportedEncodingException e) {
 			throw new XPlentyInternalAPIException("Something went wrong with JDK Strings", e);
 		} catch (JsonParseException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (JsonMappingException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return new ArrayList<ClusterPlan>();
 	}
 
 	public List<Cluster> listClusters() {
 		try {
 			WebResource resource = client.resource(getMethodURL(CLUSTERS));
 			ClientResponse response = resource
 										.accept("application/vnd.xplenty+json")
 										.header("Authorization", "Basic " + base64(API_KEY))
 										.get(ClientResponse.class);
 			validate(response);
 			String json = response.getEntity(String.class);
 			List<Cluster> clusters = mapper.readValue(json, new TypeReference<List<Cluster>>(){});
 			return clusters;
 		} catch (UnsupportedEncodingException e) {
 			throw new XPlentyInternalAPIException("Something went wrong with JDK Strings", e);
 		} catch (JsonParseException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (JsonMappingException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return new ArrayList<Cluster>();
 	}
 	
 	/**
 	 * 
 	 * @param cluster
 	 * @return
 	 */
 //	public Cluster createCluster(Cluster cluster) {
 //		cluster[plan_id]
 //	}
 	
 	public Cluster clusterInformation(long clusterId) {
 		try {
 			WebResource resource = client.resource(getMethodURL(CLUSTERS + "/" + clusterId));
 			ClientResponse response = resource
 										.accept("application/vnd.xplenty+json")
 										.header("Authorization", "Basic " + base64(API_KEY))
 										.get(ClientResponse.class);
 			validate(response);
 			Cluster cluster = response.getEntity(Cluster.class);		
 			return cluster;
 		} catch (UnsupportedEncodingException e) {
 			throw new XPlentyInternalAPIException("Something went wrong with JDK Strings", e);
 		}
 	}
 	
 	public List<Job> listJobs() {
 		try {
 			WebResource resource = client.resource(getMethodURL(JOBS));
 			ClientResponse response = resource
 										.accept("application/vnd.xplenty+json")
 										.header("Authorization", "Basic " + base64(API_KEY))
 										.get(ClientResponse.class);
 			validate(response);
 			String json = response.getEntity(String.class);
 			List<Job> jobs = mapper.readValue(json, new TypeReference<List<Job>>() {});		
 			return jobs;
 		} catch (UnsupportedEncodingException e) {
 			throw new XPlentyInternalAPIException("Something went wrong with JDK Strings", e);
 		} catch (JsonParseException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (JsonMappingException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return new ArrayList<Job>();
 	}
 	
	public Job jobInformation(long jobId) {
 		try {
			WebResource resource = client.resource(getMethodURL(JOBS + "/" + jobId));
 			ClientResponse response = resource
 										.accept("application/vnd.xplenty+json")
 										.header("Authorization", "Basic " + base64(API_KEY))
 										.get(ClientResponse.class);
 			validate(response);
 			Job job = response.getEntity(Job.class);
 			return job;
 		} catch (UnsupportedEncodingException e) {
 			throw new XPlentyInternalAPIException("Something went wrong with JDK Strings", e);
 		}
 	}
 	
 	private void validate(ClientResponse response) {
 		if (response.getClientResponseStatus() == Status.UNAUTHORIZED) {
 			throw new AuthFailedException("Server declined authorization.", response.getStatus(), response.getEntity(String.class), API_KEY);
 		}
 		if (response.getClientResponseStatus() == Status.PAYMENT_REQUIRED) {
 			throw new PaymentRequiredException("Server denied request", response.getStatus(), response.getEntity(String.class));
 		}
 		if (response.getClientResponseStatus() == Status.NOT_ACCEPTABLE
 			|| response.getClientResponseStatus() == Status.UNSUPPORTED_MEDIA_TYPE
 			|| response.getClientResponseStatus() == Status.INTERNAL_SERVER_ERROR) {
 			throw new XPlentyInternalServerException(response.getClientResponseStatus().getReasonPhrase(), response.getStatus(), response.getEntity(String.class));
 		}
 		if (response.getClientResponseStatus() != Status.OK
 			&& response.getClientResponseStatus() != Status.CREATED) {
 			//TODO throw generic exception
 		}
 	}
 	
 	private String getMethodURL(String methodName) {
 		return BASE_URL + "/" + ACCOUNT_NAME + "/" + API_PATH + "/" + methodName;
 	}
 	
 	private String base64(String apiKey) throws UnsupportedEncodingException {
 		return Base64.encodeBase64String((API_KEY + ":").getBytes("UTF-8"));
 	}
 	
 	public static void main (String[] args) throws UniformInterfaceException, UnsupportedEncodingException {
 		XPlentyAPI api = new XPlentyAPI("javasdk", "V4eyfgNqYcSasXGhzNxS");
 //		List<ClusterPlan> plans = api.listClusterPlans();
 //		System.out.println(plans);
 //		List<Cluster> clusters = api.listClusters();
 //		System.out.println(clusters);
 //		Cluster cluster = api.clusterInformation(71);
 //		System.out.println(cluster);
 		List<Job> jobs = api.listJobs();
 		System.out.println(jobs);
 	}
 }
