 package net.pdp7.tvguide.spring.web;
 
 import org.springframework.social.connect.Connection;
 import org.springframework.social.connect.web.SignInAdapter;
 import org.springframework.web.context.request.NativeWebRequest;
 
 public class SimpleSignInAdapter implements SignInAdapter {
 
 	protected final SessionService sessionService;
 
 	public SimpleSignInAdapter(SessionService sessionService) {
 		this.sessionService = sessionService;
 	}
 	
	@Override
 	public String signIn(String userId, Connection<?> connection, NativeWebRequest request) {
 		sessionService.signIn(Long.parseLong(userId), request);
 		return null;
 	}
 }
