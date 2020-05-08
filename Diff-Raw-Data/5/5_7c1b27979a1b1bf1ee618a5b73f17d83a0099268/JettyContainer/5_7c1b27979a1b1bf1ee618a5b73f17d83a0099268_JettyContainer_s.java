 package com.warspite.insulae.jetty;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.warspite.common.cli.CliListener;
 import com.warspite.common.cli.annotations.Cmd;
 import com.warspite.common.servlets.sessions.SessionKeeper;
 
 import com.warspite.insulae.database.InsulaeDatabase;
 
 public class JettyContainer implements CliListener {
 	public final static int DEFAULT_PORT = 80;
 	
 	private final Logger logger = LoggerFactory.getLogger(getClass());
 	private final SessionKeeper sessionKeeper;
 	private JettyRunner jettyRunner = null;
 
 	private final InsulaeDatabase db;
 
 	public JettyContainer(final SessionKeeper sessionKeeper, final InsulaeDatabase db) {
 		this.sessionKeeper = sessionKeeper;
 		this.db = db;
 	}
 	
	@Cmd(name="start",description="Start Jetty server, listening on <port>.")
 	public void start(Integer port) throws InterruptedException {
 		synchronized(this) {
 			if(jettyRunner != null)
 				stop();
 
 			jettyRunner = new JettyRunner(port, sessionKeeper, db);
 			jettyRunner.start();
 
 			while(!jettyRunner.isOnline()) {
 				logger.debug("Waiting for Jetty server to come online...");
 				Thread.sleep(250);
 				
 				if(jettyRunner.hasAborted()) {
 					logger.info("The Jetty server has aborted, giving up waiting.");
 					return;
 				}
 			}
 			logger.info("Jetty server is online.");
 		}
 	}
 
	@Cmd(name="stop",description="Stop Jetty server")
 	public void stop() throws InterruptedException {
 		synchronized(this) {
 			if(jettyRunner == null)
 				return;
 
 			jettyRunner.setHalt(true);
 			
 			while(jettyRunner.isOnline()) {
 				logger.info("Waiting for server shutdown...");
 				Thread.sleep(250);
 			}
 
 			logger.info("Server is offline, exiting.");
 			
 			jettyRunner = null;
 		}
 	}
 }
