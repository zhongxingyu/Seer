 package ly.xstream;
 
 import java.util.HashMap;
 
 import org.apache.commons.codec.binary.Base64;
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpHost;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.methods.*;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.AbstractHttpMessage;
 import org.apache.http.protocol.HTTP;
 import org.apache.http.util.EntityUtils;
 
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 
 /** A Client for accessing the RESTfull API of http://x-stream.ly
  * @author Brian Willard
  * @version 1.0 
  */
 public class ResfullClient {
 	
 	private static final String XSTREAMLY_HOST = "secure.x-stream.ly";
 	private static final int XSTREAMLY_PORT = 443;
 	private static final String XSTREAMLY_PROTOCAL = "https";
 
 	
 	private String appKey;
 	private DefaultHttpClient httpClient;
 	private HttpHost httpHost;
 	private String auth;
 	
 	/** Create a new resfull client
 	 * 
 	 * @param appKey			You application key, this is the GUID that identifies your application it can be found at the top of the page at https://secure.x-stream.ly/console.html
 	 * @param emailAddress		The e-mail address you use to log into the service with
 	 * @param emailAddress		The password you use to log into the service with
 	 */
 	public ResfullClient(final String appKey, final String emailAddress, final String password){
 		this.appKey = appKey;
 				
 		httpClient = new DefaultHttpClient();
 		byte[] byteData = (emailAddress+":"+password).getBytes();
 		auth = "Basic "+Base64.encodeBase64String(byteData);
 		
 		/*httpClient.getCredentialsProvider().setCredentials(
 				new AuthScope(XSTREAMLY_HOST ,XSTREAMLY_PORT ),
 				new UsernamePasswordCredentials(emailAddress,password));*/
 		
 		httpHost = new HttpHost(XSTREAMLY_HOST,XSTREAMLY_PORT,XSTREAMLY_PROTOCAL);
 	}
 	
 	/** Sends a message (non-persisted) on the channel and with the event you specify
 	 * 
 	 * @param channel			The channel the message will be sent on
 	 * @param eventName			The name of the event that will be generated
 	 * @param data				an object containing the data you want to be sent
 	 */
 	public void send(String channel, String eventName,Object data) throws Exception{
 		send(channel,eventName,data,false);
 	}
 	
 	/** Sends a message (non-persisted) on the channel and with the event you specify
 	 * 
 	 * @param channel			The channel the message will be sent on
 	 * @param eventName			The name of the event that will be generated
 	 * @param data				an object containing the data you want to be sent
 	 */
 	public void send(String channel, String eventName,Object data,Boolean persisted) throws Exception{
 		HttpPost post = new HttpPost ("/api/v1.1/" + appKey + "/channels/" + channel + "/events/" + eventName);
 		
 		authorizeRequest(post);
 		
 		Gson gson = new Gson();
 		String json;
 		
 		if(data instanceof String){
 			json = data.toString();
 		} else {
 			json = gson.toJson(data);
 		}
 		
 		
 		StringEntity entity = new StringEntity(json, HTTP.UTF_8);
 		entity.setContentType("application/json");
 		post.setEntity(entity);
 			
 		HttpResponse response = httpClient.execute(httpHost, post);
 		
 		validateResponse(response);
 	}
 	
 	/** Registers a call back so whenever X-Stream.ly receives a message
 	 * a message with a specified channel and endpoint it will post a
 	 * message to an endpoint that specified
 	 * 
 	 * @param channel			The channel to listen for messages on
 	 * @param eventName			The name of the event to listen on, if null all events will be sent to the endpoint
 	 * @param endPoint			The endpoint that X-Stream.ly should post a message to
 	 * @param secret			A secret that will be sent along will all messages to that you know the message came from us
 	 * 
 	 * @return					The key of the callback that was created
 	 */
 	public String setCallback(String channel, String endPoint, String secret, String eventName) throws Exception{
 		HttpPost post = new HttpPost ("/api/v1.1/" + appKey + "/feeds/out/custom");
 		
 		authorizeRequest(post);
 		
 		String json = "{\"channel\":\""+channel+"\", \"endpoint\":\""+endPoint+"\", \"secret\":\""+secret+"\", \"event\": \""+eventName+"\"}";
 		
 		StringEntity entity = new StringEntity(json, HTTP.UTF_8);
 		entity.setContentType("application/json");
 		post.setEntity(entity);
 		
 		HttpResponse response = httpClient.execute(httpHost, post);
 		
 		validateResponse(response);
 		
 		return response.toString();
 	}
 	
 	/** Removes a callback that was set with setCallback
 	 * 
 	 * @param key			The key of the callback, it can be obtained via getCallbacks
 	 */
 	public void removeCallback(String key) throws Exception{
 		genericDelete("/api/v1.1/" + appKey + "/feeds/out/custom/"+key);
 	}
 	
 	/** Lists all active callbacks
 	 */
 	public Callbacks getCallbacks() throws Exception{
 		return genericGet("/api/v1.1/" + appKey + "/feeds/out/custom/",Callbacks.class);
 	}
 	
 	/** Lists all channels that currently have a connected client
 	 */
 	public Channels activeChannels() throws Exception{
 		return genericGet("/api/v1.1/" + appKey + "/activeChannels/",Channels.class);
 	}
 	
 	/** Returns an array of historical connection data
 	 */
 	public UsageData usageConnections() throws Exception{
 		return genericGet("/usage/connections/",UsageData.class);
 	}
 	
 	/** Returns an array of historical message usage data
 	 */
 	public UsageData usageMessages() throws Exception{
 		return genericGet("/usage/messages/",UsageData.class);
 	}
 	
 	/** Returns all the currently valid security tokens your account has
 	 */
 	public Tokens getTokens() throws Exception{
 		return genericGet("/api/v1.1/" + appKey + "/security/",Tokens.class);
 	}
 	
 	/** Creates a new security token that can be given to client to allow them to
 	 * interact with X-Stream.ly
 	 * 
 	 * @param canRead			If the security token allows a user to read data
 	 * @param canWrite			If the security token allows a user to write data
 	 * @param channel			The channel the user will be able to interact with, if null they have access to all channels
 	 * @param event				The event the user will be able to interact with, if null they have access to all events
 	 * @param source			The IP address this security token is valid for, if left null it will be valid for all IP addresses
 	 * @param isPrivate			If true the security token can access both private and public channels, if false the security toekn only grants access to public messages
 	 * 
 	 * @return					The key of the token that was created
 	 */
 	public String createToken(Boolean canRead,Boolean canWrite,String channel, String event, String source, Boolean isPrivate) throws Exception{
 		HttpPost post = new HttpPost ("/api/v1.1/" + appKey + "/security");
 		
 		authorizeRequest(post);
 		
 		HashMap<String,String> data = new HashMap<String,String>();
 		
 		if(canRead && canWrite){
 			//don't set anything
 		} else if(canRead){
 			data.put("action", "read|view");
 		}else if(canRead){
 			data.put("action", "write|view");
 		} else {
 			data.put("action", "view");
 		}
 		
 		if(null!=channel){
 			data.put("channel", channel);
 		}
 		
 		if(null!=event){
			data.put("event", event);
 		}
 		
 		if(null!=source){
			data.put("source", source);
 		}
 		
 		data.put("private", isPrivate.toString());
 		
 		Gson gson = new Gson();
 		String json = gson.toJson(data);
 		
 		StringEntity entity = new StringEntity(json, HTTP.UTF_8);
 		entity.setContentType("application/json");
 		post.setEntity(entity);
 		
 		HttpResponse response = httpClient.execute(httpHost, post);
 		
 		return validateResponse(response);
 	}
 	
 	/** Removes a token that was set with createToken
 	 * 
 	 * @param key				The key of the createToken, it can be obtained via getTokens
 	 */
 	public void deleteToken(String token) throws Exception{
 		genericDelete("/api/v1.1/" + appKey + "/security/"+token);
 	}
 	
 	private void genericDelete(String url) throws Exception{
 		HttpDelete delete  = new HttpDelete(url);
 		
 		authorizeRequest(delete);
 		
 		HttpResponse response = httpClient.execute(httpHost, delete);
 		
 		validateResponse(response);
 	}
 	
 	private <T> T genericGet(String url,Class<T> cls) throws Exception{
 		HttpGet get  = new HttpGet(url);
 		
 		authorizeRequest(get);
 		
 		HttpResponse response = httpClient.execute(httpHost, get);
 		
 		String json = validateResponse(response);
 			
 		GsonBuilder builder = new GsonBuilder();
 		builder.registerTypeAdapter(UsageDataPoint.class, new UsageDataPointDeserializer());
 		
 		Gson gson = builder.create();
 		
 		return gson.fromJson(json,cls);
 	}
 	
 	private void authorizeRequest(AbstractHttpMessage request){
 		request.setHeader("Authorization", auth);
 	}
 	
 	private String validateResponse(HttpResponse response) throws Exception{
 		HttpEntity entity = response.getEntity();
 		String content = EntityUtils.toString(entity);
 		EntityUtils.consume(entity);
 		int statusCode = response.getStatusLine().getStatusCode();
 		if(statusCode!=202 && statusCode !=200){
 			throw new Exception("Failed to send message "+response.toString());
 		}
 		
 		return content;
 	}
 }
