 package com.gmail.at.zhuikov.aleksandr.driveddoc.servlet;
 
 import java.io.IOException;
 
 import javax.inject.Inject;
 import javax.inject.Singleton;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
 import com.google.api.client.auth.oauth2.AuthorizationCodeResponseUrl;
 import com.google.api.client.auth.oauth2.Credential;
 import com.google.api.client.extensions.appengine.auth.oauth2.AbstractAppEngineAuthorizationCodeCallbackServlet;
 import com.google.api.client.extensions.appengine.auth.oauth2.AppEngineCredentialStore;
 import com.google.api.client.extensions.appengine.http.UrlFetchTransport;
 import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
 import com.google.api.client.http.GenericUrl;
 import com.google.api.client.json.JsonFactory;
 import com.google.drive.samples.dredit.CredentialManager;
 
 @Singleton
 public class AuthorizationCodeCallbackServlet extends
 		AbstractAppEngineAuthorizationCodeCallbackServlet {
 
 	private static final long serialVersionUID = 1L;
 	
 	@Inject
 	JsonFactory jsonFactory;
 	
 	@Override
 	protected void onSuccess(HttpServletRequest req, HttpServletResponse resp,
 			Credential credential) throws ServletException, IOException {
 		req.getRequestDispatcher("/").forward(req, resp);
 	}
 
 	@Override
 	protected void onError(HttpServletRequest req, HttpServletResponse resp,
 			AuthorizationCodeResponseUrl errorResponse)
 			throws ServletException, IOException {
 		// handle error
 	}
 
 	@Override
 	protected String getRedirectUri(HttpServletRequest req)
 			throws ServletException, IOException {
 		GenericUrl url = new GenericUrl(req.getRequestURL().toString());
		url.setRawPath("/app/oauth2callback");
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
 }
