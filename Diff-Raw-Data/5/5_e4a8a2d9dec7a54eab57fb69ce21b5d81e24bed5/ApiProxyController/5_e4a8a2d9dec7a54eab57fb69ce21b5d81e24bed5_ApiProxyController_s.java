 package se.ryttargardskyrkan.cordate.controller;
 
 import java.io.IOException;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.http.Header;
 import org.apache.http.HttpResponse;
 import org.apache.http.auth.AuthenticationException;
 import org.apache.http.auth.UsernamePasswordCredentials;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpDelete;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpPut;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.auth.BasicScheme;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 
 import se.ryttargardskyrkan.cordate.model.UserSession;
 
 @Controller
 public class ApiProxyController {
 	
 	@Autowired
 	private UserSession userSession;
 	
	private HttpClient httpClient;
 	private String baseUrl;
 	
 	public ApiProxyController() {
		this.httpClient = new DefaultHttpClient();
 		this.baseUrl = "http://localhost:9000";
 	}
 
 	@RequestMapping(value="/api/**", produces = "application/json;charset=utf-8")
 	public void proxyPost(HttpServletRequest request, @RequestBody String requestBody, HttpServletResponse response) throws ClientProtocolException, IOException, AuthenticationException {
 		String requestURI = request.getRequestURI().replaceAll("/cordate", "");
 		
 		String username = userSession.getUsername();
 		String password = userSession.getPassword();
 		UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(username, password);
 		
 		HttpResponse remoteResponse = null;
 		if ("GET".equals(request.getMethod())) {
 			HttpGet httpGet = new HttpGet(baseUrl + requestURI);
 			httpGet.addHeader(new BasicScheme().authenticate(credentials, httpGet));
 			remoteResponse = httpClient.execute(httpGet);
 		} else if ("POST".equals(request.getMethod())) {
 			HttpPost httpPost = new HttpPost(baseUrl + requestURI);
 			httpPost.setEntity(new StringEntity(requestBody, "application/json", "UTF-8"));
 			httpPost.addHeader(new BasicScheme().authenticate(credentials, httpPost));
 			remoteResponse = httpClient.execute(httpPost);
 		} else if ("PUT".equals(request.getMethod())) {
 			HttpPut httpPut = new HttpPut(baseUrl + requestURI);
 			httpPut.setEntity(new StringEntity(requestBody, "application/json", "UTF-8"));
 			httpPut.addHeader(new BasicScheme().authenticate(credentials, httpPut));
 			remoteResponse = httpClient.execute(httpPut);
 		} else if ("DELETE".equals(request.getMethod())) {
 			HttpDelete httpDelete = new HttpDelete(baseUrl + requestURI);
 			httpDelete.addHeader(new BasicScheme().authenticate(credentials, httpDelete));
 			remoteResponse = httpClient.execute(httpDelete);
 		}
 				
 		// Copying status
 		response.setStatus(remoteResponse.getStatusLine().getStatusCode());
 		
 		// Copying headers
 		Header[] remoteHeaders = remoteResponse.getAllHeaders();
 		if (remoteHeaders != null) {
 			for (Header remoteHeader : remoteHeaders) {
 				response.addHeader(remoteHeader.getName(), remoteHeader.getValue());
 			}
 		}
 		
 		// Copying response body
 		IOUtils.copy(remoteResponse.getEntity().getContent(), response.getOutputStream());
 		response.getOutputStream().flush();
 		response.getOutputStream().close();
 	}
 }
