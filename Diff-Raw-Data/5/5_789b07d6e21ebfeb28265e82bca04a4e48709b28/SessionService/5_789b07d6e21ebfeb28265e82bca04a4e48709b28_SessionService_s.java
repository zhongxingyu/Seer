 package com.rsbry.jcr.service;
 
 import javax.jcr.LoginException;
 import javax.jcr.Repository;
 import javax.jcr.RepositoryException;
 import javax.jcr.Session;
 import javax.jcr.SimpleCredentials;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.stereotype.Service;
 
 import com.rsbry.jcr.servlet.startup.JackrabbitRepositoryFactory;
 
 @Service("sessionService")
 public class SessionService {
 	private static final Logger log = LoggerFactory
 			.getLogger(SessionService.class);
 
 	public Session getSession(String username, String password)
 			throws LoginException, RepositoryException {
		log.debug("Creating a new session");
 		Repository repository = JackrabbitRepositoryFactory.getRepository();
 		Session session = repository.login(new SimpleCredentials(username,
 				password.toCharArray()));
 
 		return session;
 	}
 
 	public void closeSession(Session session) {
 		try {
 			session.logout();
			log.debug("Session closed");
 		} catch (Exception e) {
 			log.error("Unable to close session", e);
 			throw new RuntimeException(e);
 		}
 	}
 }
