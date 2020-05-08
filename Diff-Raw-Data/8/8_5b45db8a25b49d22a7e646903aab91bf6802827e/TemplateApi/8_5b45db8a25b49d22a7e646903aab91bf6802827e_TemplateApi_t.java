 package xdi2.connector.template.api;
 
 import java.io.IOException;
import java.net.URLEncoder;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.http.HttpException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class TemplateApi {
 
 	private static final Logger log = LoggerFactory.getLogger(TemplateApi.class);
 
 	private String appId;
 	private String appSecret;
 	private HttpClient httpClient;
 
 	public TemplateApi() {
 
 		this.appId = null;
 		this.appSecret = null;
 		this.httpClient = new DefaultHttpClient();
 	}
 
 	public void init() {
 
 	}
 
 	public void destroy() {
 
 		this.httpClient.getConnectionManager().shutdown();
 	}
 
 	public void startOAuth(HttpServletRequest request, HttpServletResponse response) throws IOException {
 
 		String clientId = this.getAppId();
 		String redirectUri = uriWithoutQuery(request.getRequestURL().toString());
 		String scope = "email";
 
 		// TODO: here write code to start the oauth flow
 
 		log.debug("Starting OAuth...");
 
 		StringBuffer location = new StringBuffer("https://www.yoursite.com/oauth/?");
		location.append("client_id=" + URLEncoder.encode(clientId, "UTF-8"));
		location.append("&redirect_uri=" + URLEncoder.encode(redirectUri, "UTF-8"));
		location.append("&scope=" + URLEncoder.encode(scope, "UTF-8"));
 
 		// done
 
 		log.debug("Redirecting to " + location.toString());
 		response.sendRedirect(location.toString());
 	}
 
 	public String exchangeCodeForAccessToken(HttpServletRequest request) throws IOException, HttpException {
 
 		String code = null;
 
 		log.debug("Exchanging Code '" + code + "'");
 
 		// TODO: here write code to exchange an authorization code for an access token
 
 		String accessToken = null;
 		
 		// done
 
 		log.debug("Access Token: " + accessToken);
 		return accessToken;
 	}
 
 	public void revokeAccessToken(String accessToken) throws IOException, JSONException {
 
 		if (accessToken == null) throw new NullPointerException();
 
 		log.debug("Revoking Access Token '" + accessToken + "'");
 
 		// TODO: here write code to revoke an access token
 
 		// done
 
 		log.debug("Access token revoked.");
 	}
 
 	public JSONObject getUser(String accessToken) throws IOException, JSONException {
 
 		if (accessToken == null) throw new NullPointerException();
 		
 		log.debug("Retrieving User for Access Token '" + accessToken + "'");
 
 		// TODO: here write code to retrieve a user with an access token
 
 		JSONObject user = new JSONObject();
 		user.put("first_name", "Test Value first_name");
 		user.put("last_name", "Test Value last_name");
 		user.put("gender", "Test Value gender");
 		user.put("email", "Test Value email");
 
 		// done
 
 		log.debug("User: " + user);
 		return user;
 	}
 
 	private static String uriWithoutQuery(String url) {
 
 		return url.contains("?") ? url.substring(url.indexOf("?")) : url;
 	}
 
 	public String getAppId() {
 
 		return this.appId;
 	}
 
 	public void setAppId(String appId) {
 
 		this.appId = appId;
 	}
 
 	public String getAppSecret() {
 
 		return this.appSecret;
 	}
 
 	public void setAppSecret(String appSecret) {
 
 		this.appSecret = appSecret;
 	}
 }
