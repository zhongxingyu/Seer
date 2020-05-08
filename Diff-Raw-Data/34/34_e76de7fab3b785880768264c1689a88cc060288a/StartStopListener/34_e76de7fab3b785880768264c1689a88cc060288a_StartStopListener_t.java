 /*
  *  Licensed to the Apache Software Foundation (ASF) under one
  *  or more contributor license agreements.  See the NOTICE file
  *  distributed with this work for additional information
  *  regarding copyright ownership.  The ASF licenses this file
  *  to you under the Apache License, Version 2.0 (the
  *  "License"); you may not use this file except in compliance
  *  with the License.  You may obtain a copy of the License at
  *  
  *    http://www.apache.org/licenses/LICENSE-2.0
  *  
  *  Unless required by applicable law or agreed to in writing,
  *  software distributed under the License is distributed on an
  *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  *  KIND, either express or implied.  See the License for the
  *  specific language governing permissions and limitations
  *  under the License. 
  *  
  */
 package org.apache.directory.intalio.embed.webapp;
 
 import java.io.File;
 
 import javax.servlet.ServletContext;
 import javax.servlet.ServletContextEvent;
 import javax.servlet.ServletContextListener;
 
 import org.apache.directory.server.core.DefaultDirectoryService;
 import org.apache.directory.server.core.DirectoryService;
 import org.apache.directory.server.ldap.LdapService;
 import org.apache.directory.server.protocol.shared.SocketAcceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 
 /**
  * A Servlet context listener to start and stop ApacheDS.
  * 
  * @author <a href="mailto:dev@directory.apache.org">Apache Directory
  *         Project</a>
  */
 public class StartStopListener implements ServletContextListener {
 
 	public static final String PARTITIONS_JSON = "/partitions.json";
	Logger logger = LoggerFactory.getLogger(this.getClass());
	
 	private DirectoryService directoryService;
 
 	private SocketAcceptor socketAcceptor;
 	private LdapService ldapService;
 
	static {
		System.out.println("static initialiser called");
	}

 	/**
 	 * Startup ApacheDS embedded.
 	 */
 	public void contextInitialized(ServletContextEvent evt) {
		logger.info("### Starting ApacheDS ###");
 		try {
 			directoryService = new DefaultDirectoryService();
 			directoryService.setShutdownHookEnabled(true);
 			directoryService.getChangeLog().setEnabled(false);
			// Added by Veresh
			String _allowAnonymousAccess = evt.getServletContext()
					.getInitParameter("ldap.allowanonymousaccess");
			boolean allowAnonymousAccess = Boolean
					.parseBoolean(_allowAnonymousAccess);
			
			directoryService.setAllowAnonymousAccess(allowAnonymousAccess);
			
 			directoryService.startup();
 
 			socketAcceptor = new SocketAcceptor(null);
 			ldapService = new LdapService();
 			ldapService.setSocketAcceptor(socketAcceptor);
 			ldapService.setDirectoryService(directoryService);
 
 			PartitionHandler ph = new PartitionHandler(directoryService);
 			ph.init(PARTITIONS_JSON);
 
 			// Set LDAP port to 10389
 			int port = 10389;
 			String value = evt.getServletContext()
 					.getInitParameter("ldap.port");
 			port = Integer.parseInt(value);
 			ldapService.setIpPort(port);
 
 			// Determine an appropriate working directory
 			ServletContext servletContext = evt.getServletContext();
 			File workingDir = (File) servletContext
 					.getAttribute("javax.servlet.context.tempdir");
 			directoryService.setWorkingDirectory(workingDir);
			
			logger.debug("Apache DS starting with : " + directoryService);
			
 			directoryService.startup();
 			ldapService.start();
			logger.debug("Is Apache DS started ? " + directoryService.isStarted());
			
 			// Store directoryService in context to provide it to servlets etc.
 			servletContext.setAttribute(DirectoryService.JNDI_KEY,
 					directoryService);
 
 		} catch (Exception e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	/**
 	 * Shutdown ApacheDS embedded.
 	 */
 	public void contextDestroyed(ServletContextEvent evt) {
 		try {
 			ldapService.stop();
 			directoryService.shutdown();
 		} catch (Exception e) {
 			throw new RuntimeException(e);
 		}
 	}
 }
