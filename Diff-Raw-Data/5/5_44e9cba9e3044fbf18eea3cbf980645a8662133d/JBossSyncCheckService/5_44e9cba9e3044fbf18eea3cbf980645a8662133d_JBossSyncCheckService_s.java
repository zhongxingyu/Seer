 /*
  * JBoss, Home of Professional Open Source
  * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
  * as indicated by the @authors tag. All rights reserved.
  */
 package org.jboss.planet.service;
 
 import java.util.List;
 import java.util.Timer;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.annotation.PostConstruct;
 import javax.annotation.Resource;
 import javax.ejb.Singleton;
 import javax.ejb.Startup;
 import javax.ejb.Timeout;
 import javax.ejb.TimerConfig;
 import javax.ejb.TimerService;
 import javax.ejb.TransactionAttribute;
 import javax.ejb.TransactionAttributeType;
 import javax.inject.Inject;
 import javax.inject.Named;
 
 import org.apache.http.auth.AuthScope;
 import org.apache.http.auth.UsernamePasswordCredentials;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.jboss.planet.model.PostStatus;
 
 /**
  * Service for checking not synced content to search.jboss.org. It's {@link Timer} based checker and thus runs in
  * separate thread
  * 
  * @author Libor Krzyzanek
  */
 @Named
 @Singleton
 @Startup
 @TransactionAttribute(TransactionAttributeType.NEVER)
 public class JBossSyncCheckService {
 
 	@Resource
 	private TimerService timerService;
 
 	@Inject
 	private Logger log;
 
 	@Inject
 	private JBossSyncService jbossSyncService;
 
 	@Inject
 	private PostService postService;
 
 	@Inject
 	private ConfigurationService configurationService;
 
 	@PostConstruct
 	public void start() {
 		// First update fired 10 minutes after server startup
 		int startupInMin = 10;
		log.log(Level.INFO, "Initializating first new content to sync check in {0} min", startupInMin);
 		timerService.createSingleActionTimer(startupInMin * 60 * 1000, new TimerConfig(null, false));
 	}
 
 	private void initTimer() {
 		int intervalInSec = configurationService.getConfiguration().getUpdateInterval();
		log.log(Level.INFO, "Initializating next content to sync in {0} min", intervalInSec / 60);
 		timerService.createSingleActionTimer(intervalInSec * 1000, new TimerConfig(null, false));
 	}
 
 	@Timeout
 	public void checkPostsToSync() {
 		log.log(Level.INFO, "Sync to jboss.org started via {0}", configurationService.getConfiguration()
 				.getSyncServer());
 		// Getting only IDs to avoid "no session" on lazy initialization
 		List<Integer> postsToSync = postService.find(PostStatus.CREATED);
 
 		DefaultHttpClient httpClient = new DefaultHttpClient();
 
 		httpClient.getCredentialsProvider().setCredentials(
 				new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT),
 				new UsernamePasswordCredentials(configurationService.getConfiguration().getSyncUsername(),
 						configurationService.getConfiguration().getSyncPassword()));
 
 		int syncSucc = 0;
 		int syncFail = 0;
 		for (Integer id : postsToSync) {
 			if (syncFail > 20 && syncSucc == 0) {
 				// Something is probably badly configured. Let's skip this update.
 				log.log(Level.SEVERE, "20 attempts sync to server failed. Aborting.");
 				break;
 			}
 			boolean success = jbossSyncService.syncPost(id, httpClient);
 			if (success) {
 				syncSucc++;
 			} else {
 				syncFail++;
 			}
 		}
 		httpClient.getConnectionManager().shutdown();
 
 		log.log(Level.INFO, "Sync completed. Posts pushed: {0}, failed: {1}", new Integer[] { syncSucc, syncFail });
 
 		initTimer();
 	}
 
 }
