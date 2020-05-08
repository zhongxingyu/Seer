 package org.daisy.pipeline.client.http;
 
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.io.UnsupportedEncodingException;
 import java.util.Map;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.mime.MultipartEntity;
 import org.apache.http.entity.mime.content.FileBody;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.daisy.pipeline.client.Pipeline2WS;
 import org.daisy.pipeline.client.Pipeline2WSException;
 import org.daisy.pipeline.client.Pipeline2WSResponse;
 import org.daisy.pipeline.client.Pipeline2WSLogger;
 import org.daisy.pipeline.utils.XML;
 import org.restlet.Client;
 import org.restlet.data.MediaType;
 import org.restlet.data.Protocol;
 import org.restlet.data.Status;
 import org.restlet.representation.Representation;
 import org.restlet.resource.ClientResource;
 import org.restlet.resource.ResourceException;
 import org.w3c.dom.Document;
 
 /**
  * Implementation of DP2HttpClient that uses Restlet as the underlying HTTP client.
  * 
  * @author jostein
  */
 public class DP2HttpClientImpl implements DP2HttpClient {
 	
 	private static Client client = new Client(Protocol.HTTP); // TODO: add support for HTTPS WS
 	
 	public Pipeline2WSResponse get(String endpoint, String path, String username, String secret, Map<String,String> parameters) throws Pipeline2WSException {
 		String url = Pipeline2WS.url(endpoint, path, username, secret, parameters);
 		if (endpoint == null) {
 			return new Pipeline2WSResponse(503, "Endpoint is not set", "Please provide a Pipeline 2 endpoint.", null, null);
 		}
 		
 		ClientResource resource = new ClientResource(url);
 		resource.setNext(client);
 		Representation representation = null;
 		InputStream in = null;
 		boolean error = false;
 		try {
 			representation = resource.get();
 			if (representation != null)
 				in = representation.getStream();
 			
 		} catch (ResourceException e) {
 			// Unauthorized etc.
 			error = true;
 		} catch (IOException e) {
 			e.printStackTrace();
 			error = true;
 		}
 		
 		Status status = resource.getStatus();
 		
 		if (error) {
 			try {
 				if (status != null && status.getCode() >= 1000) {
 					in = new ByteArrayInputStream("Could not communicate with the Pipeline 2 framework.".getBytes("utf-8"));
 				} else {
 					in = new ByteArrayInputStream("An unknown problem occured while communicating with the Pipeline 2 framework.".getBytes("utf-8"));
 				}
 	        } catch(UnsupportedEncodingException e) {
 	            throw new Pipeline2WSException("Unable to create error body string as stream", e);
 	        }
 		}
 		
 		Pipeline2WSResponse response = new Pipeline2WSResponse(status.getCode(), status.getName(), status.getDescription(), representation==null?null:representation.getMediaType()==null?null:representation.getMediaType().toString(), in);
 		if (Pipeline2WS.logger().logsLevel(Pipeline2WSLogger.LEVEL.DEBUG)) {
 			try {
				if (representation != null && representation.getMediaType() == MediaType.APPLICATION_ALL_XML) {
 					Pipeline2WS.logger().debug("---- Received: ----\n"+response.asText());
 				} else {
 					Pipeline2WS.logger().debug("---- Received: "+representation.getMediaType()+" ("+representation.getSize()+" bytes) ----");
 				}
 			} catch (Exception e) {
 				Pipeline2WS.logger().error("---- Received: ["+e.getClass()+": "+e.getMessage()+"] ----");
 				StringWriter sw = new StringWriter();
 				PrintWriter pw = new PrintWriter(sw);
 				e.printStackTrace(pw);
 				Pipeline2WS.logger().error(sw.toString());
 			}
 		}
 		return response;
 	}
 	
 	public Pipeline2WSResponse postXml(String endpoint, String path, String username, String secret, Document xml) throws Pipeline2WSException {
 		String url = Pipeline2WS.url(endpoint, path, username, secret, null);
 		
 		if (Pipeline2WS.logger().logsLevel(Pipeline2WSLogger.LEVEL.DEBUG)) {
 			Pipeline2WS.logger().debug("URL: ["+url+"]");
 			Pipeline2WS.logger().debug(XML.toString(xml));
 		}
 		
 		ClientResource resource = new ClientResource(url);
 		Representation representation = null;
 		try {
 			representation = resource.post(XML.toString(xml));
 		} catch (org.restlet.resource.ResourceException e) {
 			throw new Pipeline2WSException(e.getMessage(), e);
 		}
 		
 		InputStream in = null;
 		if (representation != null) {
 			try {
 				in = representation.getStream();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		
 		Status status = resource.getStatus();
 		
 		return new Pipeline2WSResponse(status.getCode(), status.getName(), status.getDescription(), representation==null?null:representation.getMediaType().toString(), in);
 	}
 	
 	public Pipeline2WSResponse postMultipart(String endpoint, String path, String username, String secret, Map<String,File> parts) throws Pipeline2WSException {
 		String url = Pipeline2WS.url(endpoint, path, username, secret, null);
 		
 		HttpClient httpclient = new DefaultHttpClient();
 		HttpPost httppost = new HttpPost(url);
 		
 		MultipartEntity reqEntity = new MultipartEntity();
 		for (String partName : parts.keySet()) { 
 			reqEntity.addPart(partName, new FileBody(parts.get(partName)));
 		}
 		httppost.setEntity(reqEntity);
 		
 		HttpResponse response = null;
 		try {
 			response = httpclient.execute(httppost);
 		} catch (ClientProtocolException e) {
 			throw new Pipeline2WSException("Error while POSTing.", e);
 		} catch (IOException e) {
 			throw new Pipeline2WSException("Error while POSTing.", e);
 		}
 		HttpEntity resEntity = response.getEntity();
 		
 		InputStream bodyStream = null;
 		try {
 			bodyStream = resEntity.getContent();
 		} catch (IOException e) {
 			throw new Pipeline2WSException("Error while reading response body", e); 
 		}
 		
 		Status status = Status.valueOf(response.getStatusLine().getStatusCode());
 		
 		return new Pipeline2WSResponse(status.getCode(), status.getName(), status.getDescription(), response.getFirstHeader("Content-Type").getValue(), bodyStream);
 	}
 	
 }
