 package com.gmail.at.zhuikov.aleksandr.driveddoc.servlet;
 
 import java.io.IOException;
 
 import javax.inject.Inject;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
 import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl;
 import com.google.api.client.extensions.appengine.auth.oauth2.AbstractAppEngineAuthorizationCodeServlet;
 import com.google.api.client.extensions.appengine.auth.oauth2.AppEngineCredentialStore;
 import com.google.api.client.extensions.appengine.http.UrlFetchTransport;
 import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
 import com.google.api.client.http.GenericUrl;
 import com.google.api.client.json.JsonFactory;
 import com.google.drive.samples.dredit.CredentialManager;
 
 public abstract class AuthorizationCodeServlet extends AbstractAppEngineAuthorizationCodeServlet {
 
 	private static final long serialVersionUID = 1L;
 	
 	protected JsonFactory jsonFactory;
 	
 	@Inject
 	public AuthorizationCodeServlet(JsonFactory jsonFactory) {
 		this.jsonFactory = jsonFactory;
 	}
 	
 	@Override
 	protected String getRedirectUri(HttpServletRequest req)
 			throws ServletException, IOException {
 		GenericUrl url = new GenericUrl(req.getRequestURL().toString());
		url.setRawPath("/api/oauth2callback");
 		return url.build();
 	}
 
 	@Override
 	protected AuthorizationCodeFlow initializeFlow() throws IOException {
 		return new GoogleAuthorizationCodeFlow.Builder(new UrlFetchTransport(),
 				jsonFactory, "610309933249.apps.googleusercontent.com",
 				"YDq0zPizR0rJANUBlgbzlb_4",
 				CredentialManager.SCOPES)
 				.setCredentialStore(new AppEngineCredentialStore()).build();
 	}
 	
 	@Override
 	protected void onAuthorization(HttpServletRequest req,
 			HttpServletResponse resp,
 			AuthorizationCodeRequestUrl authorizationUrl)
 			throws ServletException, IOException {
 		authorizationUrl.setState(req.getParameter("state"));
 		super.onAuthorization(req, resp, authorizationUrl);
 	}
 }
